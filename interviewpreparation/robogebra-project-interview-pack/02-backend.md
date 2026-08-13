# 2. Back-End (API design & request lifecycle) — Q&A tied to Robogebra

This section is the *backend thinking* layer — how a request flows end to end, how the layers are organized, status codes, and the two flows you asked about: the **auth/JWT chain** and **"email already exists"**. (Java-language and Spring-annotation depth are in `03-java.md` / `04-spring.md`.)

**Stack:** Spring Boot 3.2 monolithic `robogebra-portal` (port 3000), domain-driven packages, MongoDB.

---

## Q1. Walk me through the lifecycle of an authenticated request in your backend.

**Answer.** Filters → controller → service → repository → DB, then DTO back out:

1. Request hits the **security filter chain**. `JwtAuthenticationFilter` (a `OncePerRequestFilter`) extracts the `Bearer` token, validates it (SSO or Cognito), loads the user, sets the Spring `SecurityContext`, and **stashes the user on the request as an attribute**.
2. The **controller** method runs; it reads that user via `@RequestAttribute` and validates the `@RequestBody` with `@Valid`.
3. The **service** does business logic + authorization (`teacher.validateAsTeacher()`), calling other services/repositories.
4. The **repository** (Spring Data Mongo) reads/writes documents.
5. The result is mapped to a **DTO** and serialized to JSON. Any exception is caught by the `@ControllerAdvice` and turned into a consistent error body.

**In Robogebra** — the controller reading the filter-provided user:

```java
public List<SolutionVisibilityDTO> updateStatus(
    @RequestAttribute(ApplicationConstants.INSTITUTE_USER_ATTRIBUTE) InstituteUser instituteUser,
    @Valid @RequestBody SolutionVisibilityUpdateDTO request) { ... }
```

The `instituteUser` was never sent by the client — the **filter** put it there after authenticating. That decoupling (filter authenticates, controller consumes) is the key design idea.

---

## Q2. Explain the auth/JWT validation chain in detail. *(you asked how auth connects)*

**Answer.** A single `OncePerRequestFilter` centralizes authentication so no controller repeats it. Steps: extract token → pick validation path (our own SSO JWT vs AWS Cognito) → load the user → build a Spring `Authentication` and set it in `SecurityContextHolder` → put the user (and institute) on request attributes → continue the chain. On token errors it writes a clean error response instead of letting a 500 escape.

**In Robogebra** (`security/JwtAuthenticationFilter.java`):

```java
@Component @Order(6) @AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Override protected boolean shouldNotFilter(HttpServletRequest req) {
      return hasAnnotationWithBypassJwtAuth(req);        // skip @BypassJwtAuth endpoints (webhooks, CRM)
  }

  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) {
    try {
      String token = authService.getTokenFromRequest(req);
      if (needTokenValidation(req, token)) {
        JwtAuth jwtAuth = authService.isSSOToken(token)
            ? authService.getAuthDetailsBySSOToken(token)   // our JWT (issuer check)
            : authService.getAuthDetailsByToken(token);      // AWS Cognito validation

        UserDetails user = userDetailsService.loadUserByUsername(getUserName(jwtAuth));
        var authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        req.setAttribute(ApplicationConstants.LOGGED_USER_ATTRIBUTE, jwtAuth);
        addInstituteAttributes(req, jwtAuth.getId());        // sets INSTITUTE_USER_ATTRIBUTE (or null)
      }
      chain.doFilter(req, res);
    } catch (CognitoException e) { responseWriter.writeCognitoException(res, req, e); }
  }
}
```

Cognito token validation itself (`AuthService.getAuthDetailsByToken`) maps Cognito SDK exceptions to HTTP statuses — `NotAuthorizedException → 401`, `TooManyRequestsException → 400` — so auth failures are *typed*, not generic 500s.

**Why this design?** One place owns authentication; controllers stay clean; the `@BypassJwtAuth` annotation lets service-to-service calls (CRM, webhooks) skip user auth and use a service token instead. That's the standard senior answer: *cross-cutting concern → filter, not scattered checks.*

---

## Q3. Walk through "user signs up with an email/phone that already exists." *(you asked for this)*

**Answer.** The signup endpoint delegates to a service that tries to **find-or-create** the user. If the account already exists (and is already a full Cognito account), it throws a business exception with **HTTP 400** and a human message; the global advice turns that into a clean JSON error the frontend shows as a toast. It also handles the legitimate case of *linking* Cognito to an existing SSO (Google/LinkedIn) user.

**In Robogebra.**

Controller (`auth/controller/AuthController.java`) — thin, delegates + documents the 400:

```java
@PostMapping("/create-user")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad Request") }) // duplicate
public ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequestDTO dto) {
    authService.createUser(dto.toModel());
    return new ResponseEntity<>("Successfully send OTP to the " + dto.getUserName(), HttpStatus.OK);
}
```

Service (`auth/service/AuthService.java`) — find-or-create + duplicate detection:

```java
private User resolveOrCreateUser(String userName, ExternalUser externalUser, UserRole role, String referral) {
    return userPersistenceService.findByUserName(userName)
        .map(entity -> linkCognitoToExistingUser(entity, externalUser.getExternalId(), userName))
        .orElseGet(() -> createNewUserWithDefaults(externalUser, userName, role, referral));
}

private User linkCognitoToExistingUser(UserEntity user, String cognitoId, String userName) {
    if (isSsoUser(user) && user.getCognitoId() == null) {          // valid: link Cognito to Google/LinkedIn user
        userPersistenceService.linkCognitoAccount(user.getId(), cognitoId);
        return user.toModel();
    }
    // DUPLICATE: real account already exists
    throw new APIException(HttpStatus.BAD_REQUEST, "An account with this email already exists.");
}
```

Global advice returns the structured error → frontend `NotificationService` shows the toast (frontend side in `01-frontend.md` Q14).

**Nice detail to mention:** the check is `findByUserName(...).map(link).orElseGet(create)` — the whole find-or-create is expressed with `Optional`, no null branches. And it distinguishes *"link to existing SSO user"* (allowed) from *"true duplicate"* (rejected) — a real product nuance, not just a blind uniqueness check.

---

## Q4. How do you design your REST resources and status codes?

**Answer.** Noun-based, hierarchical URLs (`/api/solution-visibility/exercise-items/{id}/solution-access`), correct verbs (GET read, POST create, PATCH partial update), DTOs in/out, and meaningful statuses: 200 OK, 400 validation/business error, 401 unauthenticated, 403 forbidden, 404 missing, 500 unexpected. Errors share one shape `{status, message, timestamp, path}`.

**In Robogebra.** URLs encode the hierarchy Book→Chapter→Exercise→ExerciseItem; the visibility check is nested under its item: `GET /api/solution-visibility/exercise-items/{exerciseItemId}/solution-access`. `PATCH /status` is a partial update (not PUT) because we only change status for selected classes.

---

## Q5. How do you keep the service layer clean and testable?

**Answer.** Thin controllers, business logic in services, DB access behind a persistence service/repository, immutable constructor-injected deps. Authorization lives in the service (`validateAsTeacher()`), not the controller, so it's enforced regardless of entry point. That separation means I can unit-test a service by `new`-ing it with mocked collaborators.

**In Robogebra** — the visibility service validates role + target existence before doing work, then streams the update per class:

```java
public List<SolutionVisibility> updateStatus(InstituteUser teacher, SolutionVisibilityLevel level,
                                             String targetId, List<String> klassIds, SolutionVisibilityStatus status) {
    teacher.validateAsTeacher();                 // authZ in the service
    validateTargetExists(level, targetId);       // business validation
    return klassIds.stream().distinct()
            .map(k -> updateStatusForKlass(teacher, k, targetId, status))
            .collect(Collectors.toList());
}
```

---

### Rapid-fire recap

| Topic | Robogebra example | File |
|-------|-------------------|------|
| Request lifecycle | filter → controller → service → repo → DTO | `JwtAuthenticationFilter.java` |
| Auth chain | JWT/Cognito → SecurityContext → `@RequestAttribute` | `JwtAuthenticationFilter.java`, `AuthService.java` |
| Duplicate signup | find-or-create + `APIException(400)` | `AuthService.java` |
| REST/status codes | nested nouns, PATCH, uniform error body | `SolutionVisibilityController.java` |
| Clean layering | authZ in service, thin controller | `SolutionVisibilityService.java` |
