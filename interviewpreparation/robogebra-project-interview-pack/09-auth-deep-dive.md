# 9. Authentication & Authorization (Deep Dive) — tied to Robogebra

The single most-probed senior area. Keep the distinction crisp: **Authentication = who are you** (JWT/Cognito, filter chain), **Authorization = what can you do** (`@PermissionContext` + role/permission model).

---

## PART A — AUTHENTICATION ("who are you")

## Q1. Describe your Spring Security setup.

**Answer.** **Stateless, JWT-based.** No server sessions, so **CSRF is disabled** and the session policy is `STATELESS`. CORS is configured via a bean. `/api/**` requires authentication; OAuth2/login/well-known endpoints are permitted. Auth happens in a **custom filter chain**, not the default username/password filter.

**In Robogebra** (`config/SecurityConfig.java`):

```java
http.csrf(AbstractHttpConfigurer::disable)
    .cors(c -> c.configurationSource(corsConfigurationSource))
    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(a -> a
        .requestMatchers("/.well-known/**", "/oauth2/**", "/login/**").permitAll()
        .requestMatchers("/api/**").authenticated()
        .anyRequest().permitAll())
    .oauth2Login(oauth -> oauth.successHandler(socialAuthSuccessHandler) ...);
```

**Why stateless?** Horizontal scaling — any instance can serve any request because identity travels in the token, not a sticky session. That's the microservices-friendly choice.

---

## Q2. Explain your security filter chain and its order.

**Answer.** A pipeline of `OncePerRequestFilter`s, each with a job and an explicit `@Order`. Service-to-service auth runs **before** user JWT auth, so trusted callers short-circuit. `JwtAuthenticationFilter` (Order 6) is the main user authenticator.

| Order | Filter | Authenticates |
|------:|--------|---------------|
| — | `FilterChainExceptionHandler` | catches filter exceptions (first) |
| 2 | `AnonymousContentAuthenticationFilter` | public/anonymous content |
| 3 | `CrmServiceAuthenticationFilter` | `CRM_SERVICE_ACCOUNT` (service token) |
| 4 | `InternalServiceAuthenticationFilter` | `INTERNAL_SERVICE_ACCOUNT` |
| 5 | `WebhookAuthorizationFilter` | webhooks (RevenueCat) — no SecurityContext |
| **6** | **`JwtAuthenticationFilter`** | **regular users (JWT/Cognito)** |
| 8 | `MultiLoginDetectionFilter` | concurrent-session enforcement |
| — | `BookAccessRestrictionFilter`, `FeatureUsageLimitFilter`, `PreferredLanguageFilter` | subscription/feature/lang |

---

## Q3. How does the JWT filter work, and how does the user reach the controller?

**Answer.** Extract `Bearer` token → pick validation path (our SSO JWT vs Cognito) → load user → set `SecurityContext` → **stash the user on the request as attributes** → continue. Controllers then read those attributes with `@RequestAttribute` — they never re-authenticate.

**In Robogebra** (`JwtAuthenticationFilter`):

```java
String token = authService.getTokenFromRequest(req);
JwtAuth jwtAuth = authService.isSSOToken(token)
    ? authService.getAuthDetailsBySSOToken(token)   // our HMAC JWT
    : authService.getAuthDetailsByToken(token);       // AWS Cognito (RSA/JWKS)

SecurityContextHolder.getContext().setAuthentication(
    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

req.setAttribute(LOGGED_USER_ATTRIBUTE, jwtAuth);
req.setAttribute(ACTIVE_USER_ID_ATTRIBUTE, jwtAuth.getId());
instituteUserService.findInstituteUser(userId).ifPresentOrElse(
    iu -> req.setAttribute(INSTITUTE_USER_ATTRIBUTE, iu),
    () -> req.setAttribute(INSTITUTE_USER_ATTRIBUTE, null));   // not an institute user
```

Controller consumes it:

```java
public ... updateStatus(@RequestAttribute(INSTITUTE_USER_ATTRIBUTE) InstituteUser instituteUser, ...) { ... }
```

---

## Q4. Two JWT types? Explain SSO JWT vs Cognito JWT.

**Answer.** We support both:

- **Our SSO JWT** (`JwtService`, JJWT) — signed **HMAC-SHA256** with a shared secret; issued after OAuth2/admin login; carries claims `userId, username, role` (+ institute claims for institute users).
- **AWS Cognito JWT** — signed **RSA256**; we validate the signature against Cognito's **JWKS** (`.well-known/jwks.json`), verifying issuer + expiry with a small clock-skew leeway.

**SSO token creation** (`JwtService`):

```java
return Jwts.builder().setSubject(userId)
    .claim("username", username).claim("role", role)
    .setIssuer("robogebra-auth").setIssuedAt(new Date())
    .setExpiration(new Date(System.currentTimeMillis() + expiry))
    .signWith(getKey(), SignatureAlgorithm.HS256).compact();
```

**Cognito validation** (`CognitoJwtValidator`): decode → read `iss` → pick the right pool → fetch JWKS → build RSA verifier (`acceptLeeway(60)`) → verify → return the Cognito subject id → look up our `User` by `cognitoId`.

**Why HMAC vs RSA?** HMAC (symmetric) is fine for tokens *we* issue and verify. Cognito uses RSA (asymmetric) so anyone can verify with the public JWKS without holding a secret — which is why we validate its tokens against the published key set.

---

## Q5. Why three Cognito user pools?

**Answer.** Multi-tenant isolation. `user` (B2C students/teachers), `email-user` (email signups), `institute-user` (B2B institute admins/teachers). The validator **resolves the pool from the token's issuer** at runtime. Separate pools = independent password/MFA policies, separate signup flows, and blast-radius isolation between consumer and institutional identities.

---

## Q6. How do service-to-service calls authenticate? (`@BypassJwtAuth`)

**Answer.** Trusted callers (CRM, internal jobs, webhooks) skip user-JWT via a **meta-annotation**. `@BypassJwtAuth` marks other annotations (`@CrmServiceAPI`, `@InternalServiceAPI`, `@WebhookSecured`); the JWT filter's `shouldNotFilter()` sees it and steps aside, and a dedicated service filter validates a **shared service token** header instead.

```java
@Target(ElementType.ANNOTATION_TYPE) @Retention(RUNTIME)
public @interface BypassJwtAuth { }          // meta-annotation

@BypassJwtAuth @Target(METHOD) @Retention(RUNTIME)
public @interface CrmServiceAPI { }           // put on CRM-callable endpoints
```

The shared base (`AbstractServiceAuthenticationFilter`) is a **template method**: check annotation applies → validate token exists → validate token value → optional extra check → authenticate as the service role. Each concrete filter supplies the header name, expected token, and `UserRole`.

---

## PART B — AUTHORIZATION ("what can you do")

## Q7. How is authorization enforced? (this is the differentiator)

**Answer.** Two layers — **defense in depth**:

1. **Declarative, endpoint-level** — `@PermissionContext(userPermission = {...})` on controller methods, enforced by a **`HandlerInterceptor`** (Order 7, right after JWT auth). It reads the required permissions from the annotation, loads the user's permissions from the DB (role→permission mapping, with role inheritance), and allows if the user has **any** required permission — else throws `SecurityPermissionException` (403).
2. **Imperative, in-code** — business rules call explicit checks like `instituteUser.validateAsTeacher()` when the rule is domain-specific.

**The annotation** (`common/annotation/PermissionContext.java`):

```java
@Target(ElementType.METHOD) @Retention(RUNTIME)
public @interface PermissionContext { Permission[] userPermission(); }
```

**The interceptor** (`security/PermissionInterceptor.java`, `@Order(7)`):

```java
public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
    if (shouldProceed(req, handler)) return true;                    // no annotation → skip
    JwtAuth user = (JwtAuth) req.getAttribute(LOGGED_USER_ATTRIBUTE);
    List<String> required = getRequiredPermissions(handler);         // from @PermissionContext
    List<String> userPerms = fetchUserPermissions(user.getUserRole()); // walk role hierarchy in DB
    if (userPerms.stream().noneMatch(required::contains))
        throw new SecurityPermissionException(req.getRequestURI());  // 403
    return true;
}
```

Usage on an endpoint:

```java
@PostMapping("/create-user")
@PermissionContext(userPermission = {Permission.AUTH_WRITE})
public ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequestDTO dto) { ... }
```

---

## Q8. Explain the role & permission model.

**Answer.** **Roles map to permissions in the DB** (`role`, `role_permission` collections). A `Permission` enum enumerates fine-grained rights (`AUTH_READ/WRITE`, `EXERCISE_READ/WRITE`, `EXECUTE_SOLUTION_READ`, …). A `UserRole` enum defines roles — and roles can **inherit** via a `derivedFrom` link, so the interceptor walks up the chain collecting permissions.

```java
public enum UserRole {
    ADMIN, USER, CONTENT_CREATOR, SUPER_ADMIN, CONTENT_REVIEWER, PARENT,
    INSTITUTE_ADMIN(USER), INSTITUTE_TEACHER(USER),      // inherit USER's permissions
    CRM_SERVICE_ACCOUNT, INTERNAL_SERVICE_ACCOUNT;       // service accounts
    private final UserRole derivedFrom;
}
```

So `INSTITUTE_TEACHER` gets every `USER` permission **plus** its own — I don't duplicate grants. Permissions live in data (not code), so changing what a role can do is a DB change, not a redeploy.

---

## Q9. Show an imperative authorization check.

**Answer.** For domain rules I check on the resolved `InstituteUser` (`domain/institute/model/InstituteUser.java`):

```java
public boolean isTeacherOrAdmin() { return isTeacher() || isInstituteAdmin(); }
public void validateAsTeacher() {
    if (!isTeacherOrAdmin())
        throw new APIException(HttpStatus.UNAUTHORIZED, "This API is accessible only to teachers.");
}
```

Used in the solution-visibility service before any write:

```java
teacher.validateAsTeacher();   // only teachers/admins may change visibility
```

That's the two-layer story: the interceptor guards the *endpoint by permission*; the service guards the *action by domain role*.

---

## Q10. Refresh tokens & session control?

**Answer.** After login we issue a short-lived access JWT + a **refresh token** (stored, and in an `HttpOnly Secure` cookie for SSO). The frontend interceptor silently swaps an expired access token using the refresh token (see `01-frontend.md` Q9). Concurrent logins are capped by `MultiLoginDetectionFilter` — a second device can force-remove the first session, surfaced to the client via error code `60002`.

---

## Summary — Authentication vs Authorization

| | Authentication | Authorization |
|---|---|---|
| Question | who are you? | what can you do? |
| Where | `JwtAuthenticationFilter` (Order 6) | `PermissionInterceptor` (Order 7) + in-code checks |
| Input | JWT (Cognito RSA / SSO HMAC) | user role → permissions (DB) |
| Output | `SecurityContext` + request attributes | allow / 403 |
| Data | Cognito (external) / `JwtService` | `role`, `role_permission` collections |
| Bypass | `@BypassJwtAuth` (service tokens) | endpoints without `@PermissionContext` |
| Annotation | — | `@PermissionContext(userPermission={...})` |

**One-line senior takeaway:** *"Stateless JWT authentication with dual token support (Cognito RSA + our SSO HMAC) across three Cognito pools, then defense-in-depth authorization — declarative `@PermissionContext` enforced by an interceptor over a DB-backed role/permission model with inheritance, plus explicit domain checks like `validateAsTeacher()`."*
