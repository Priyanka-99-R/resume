# 4. Spring / Spring Boot — Interview Q&A tied to Robogebra

**Stack:** Spring Boot 3.2, Java 17, Maven, Spring Data MongoDB, Spring Security. Package layout is **domain-driven**: `com.robogebra.cms.domain.<feature>.{controller, service, repository, model}` with 60+ sub-domains.

---

## Q1. Explain IoC and Dependency Injection. How do you inject in this project?

**Answer.** **Inversion of Control** = the framework creates and wires objects, not me. **Dependency Injection** is how it hands collaborators in. I use **constructor injection with `final` fields** — the recommended style: dependencies are explicit, immutable, and the class is easy to unit-test (just `new` it with mocks). No field `@Autowired`.

**In Robogebra.** Lombok `@AllArgsConstructor` generates the constructor over the `final` fields, so Spring injects them (`SolutionVisibilityService`):

```java
@Service
@AllArgsConstructor
@Transactional
public class SolutionVisibilityService {
    private final SolutionVisibilityPersistenceService solutionVisibilityPersistenceService;
    private final StudentEnrollmentService studentEnrollmentService;
    private final ExerciseItemService exerciseItemService;
    private final ExerciseService exerciseService;
    private final ChapterService chapterService;
}
```

**Follow-up: constructor vs field injection?** Constructor injection makes dependencies mandatory + immutable (`final`), fails fast if a bean is missing, and needs no Spring to test. Field injection hides dependencies and can't be `final`. That's why the whole codebase uses constructor injection.

**Follow-up: bean scopes?** Default **singleton** (one per context) — fine because our services are stateless. `prototype`/`request`/`session` exist for stateful cases.

---

## Q2. What are the stereotype annotations and how is your app layered?

**Answer.** `@Component` is the base; `@Service`, `@Repository`, `@Controller`/`@RestController` are specializations that also convey intent (and `@Repository` adds persistence-exception translation). My layering is **Controller → Service → Persistence/Repository → Entity**, with DTOs at the edge.

**In Robogebra.**
- `@RestController` `SolutionVisibilityController` — HTTP only, no logic.
- `@Service` `SolutionVisibilityService` — business logic.
- `@Service` `SolutionVisibilityPersistenceService` — thin wrapper over the repo (isolates DB access).
- `@Repository` `SolutionVisibilityRepository extends MongoRepository` — data access.

That extra **persistence-service** layer between service and repository is a deliberate choice: it keeps all Mongo calls in one place and lets the business service stay focused.

---

## Q3. How do you build REST APIs? Show a controller.

**Answer.** `@RestController` + `@RequestMapping` base path; method-level `@GetMapping/@PostMapping/@PatchMapping`; `@PathVariable` for URL params, `@RequestBody` + `@Valid` for the payload, and `@RequestAttribute` to read the authenticated user that the security filter placed on the request. I return DTOs, never entities.

**In Robogebra** (`SolutionVisibilityController`):

```java
@AllArgsConstructor
@RestController
@RequestMapping("/api/solution-visibility")
public class SolutionVisibilityController {

    @PatchMapping("/status")
    public List<SolutionVisibilityDTO> updateStatus(
            @RequestAttribute(ApplicationConstants.INSTITUTE_USER_ATTRIBUTE) InstituteUser instituteUser,
            @Valid @RequestBody SolutionVisibilityUpdateDTO request) {
        return service.updateStatus(instituteUser, request.getLevel(), request.getTargetId(),
                                    request.getKlassIds(), request.getStatus())
                      .stream().map(SolutionVisibility::toDTO).toList();
    }

    @GetMapping("/exercise-items/{exerciseItemId}/solution-access")
    public StudentSolutionAccessDTO solutionAccess(
            @RequestAttribute(name = ApplicationConstants.INSTITUTE_USER_ATTRIBUTE, required = false) InstituteUser instituteUser,
            @PathVariable String exerciseItemId) {
        boolean allowed = Objects.isNull(instituteUser)
            || service.isSolutionEnabledForStudent(instituteUser, exerciseItemId);
        return StudentSolutionAccessDTO.builder().exerciseItemId(exerciseItemId).accessAllowed(allowed).build();
    }
}
```

Note `required = false` on the second `@RequestAttribute` — anonymous users have no `InstituteUser`, so the endpoint degrades gracefully instead of failing.

---

## Q4. How do you handle exceptions globally?

**Answer.** A **custom unchecked exception** carrying an HTTP status + message, plus a **`@ControllerAdvice`** with `@ExceptionHandler`s that turn exceptions into a consistent JSON error body. Specific handlers first, a catch-all `Exception` last. Business code just `throw`s; the advice formats + logs.

**In Robogebra.** Custom exception:

```java
@Getter
public class APIException extends RuntimeException {
    private final HttpStatus status;
    private final String errorMessage;
    public APIException(HttpStatus status, String errorMessage) {
        super(errorMessage); this.status = status; this.errorMessage = errorMessage;
    }
}
```

Global advice (`ExceptionControllerAdvice`):

```java
@ControllerAdvice
@Slf4j
public class ExceptionControllerAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)   // @Valid failures → 400
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex, WebRequest req) { ... }

    @ExceptionHandler(APIException.class)                      // our business errors → its status
    public ResponseEntity<ErrorMessageModel> handleAPIException(APIException ex, WebRequest req) {
        return ResponseEntity.status(ex.getStatus()).body(createErrorMessage(ex.getStatus(), ex.getMessage(), req));
    }

    @ExceptionHandler(Exception.class)                         // catch-all → 500 (no stack leak)
    public ResponseEntity<ErrorMessageModel> global(Exception ex, WebRequest req) { ... }
}
```

This is exactly how the **"email already exists"** error surfaces: the service throws `new APIException(HttpStatus.BAD_REQUEST, "An account with this email already exists.")`, the advice catches it and returns a clean `{status, message, timestamp, path}`.

---

## Q5. How does validation work?

**Answer.** Bean Validation (Jakarta) — annotate DTO fields (`@NotNull`, `@NotEmpty`, `@Email`…) with custom messages, then `@Valid` on the `@RequestBody` triggers it. Failures throw `MethodArgumentNotValidException`, which my advice maps to a 400 with the field message.

**In Robogebra** (`SolutionVisibilityUpdateDTO`):

```java
public class SolutionVisibilityUpdateDTO {
    @NotNull(message = "level is required")            private SolutionVisibilityLevel level;
    @NotNull(message = "targetId is required")         private String targetId;
    @NotEmpty(message = "at least one klassId is required") private List<String> klassIds;
    @NotNull(message = "status is required")           private SolutionVisibilityStatus status;
}
```
```java
public ... updateStatus(@Valid @RequestBody SolutionVisibilityUpdateDTO request) { ... }
```

---

## Q6. How do you manage transactions?

**Answer.** `@Transactional` — declarative, AOP-proxy-based. Class-level for write services so each public method runs in a transaction (commit on success, rollback on runtime exception). `@Transactional(readOnly = true)` on query methods as an optimization hint.

**In Robogebra.** Class-level on write services:

```java
@Service @AllArgsConstructor @Transactional
public class SolutionVisibilityService { ... }   // updateStatus() is transactional
```

Read-only on a query (`RefreshTokenService`):

```java
@Transactional(readOnly = true)
public String getRefreshToken(String userId) {
    return refreshTokenRepository.findByUserId(userId).map(...).orElse(null);
}
```

**Note (MongoDB caveat):** Mongo multi-document transactions need a replica set. `@Transactional` still gives a consistent boundary; for most single-document writes Mongo is atomic per-document anyway.

---

## Q7. Explain Spring Boot auto-configuration & `@SpringBootApplication`.

**Answer.** `@SpringBootApplication` = `@Configuration` + `@ComponentScan` + `@EnableAutoConfiguration`. Auto-config inspects the classpath and properties and wires sensible beans — e.g. seeing Spring Data MongoDB + a `spring.data.mongodb.uri` auto-creates the `MongoTemplate`/repositories. I override with my own `@Bean`s or `@ConditionalOnProperty` when needed.

**In Robogebra** — a conditional bean (`CacheConfig`) that swaps the cache manager based on a property:

```java
@Configuration @EnableCaching
public class CacheConfig {
    @Bean @ConditionalOnProperty(prefix = "cache", name = "enabled", havingValue = "true")
    public CacheManager cacheManager() { /* Caffeine caches */ }

    @Bean @ConditionalOnProperty(prefix = "cache", name = "enabled", havingValue = "false")
    public CacheManager noOpCacheManager() { return new NoOpCacheManager(); }
}
```

---

## Q8. How is configuration / profiles handled?

**Answer.** `application.yml` with environment-variable placeholders and defaults, `@Value`/`@ConfigurationProperties` to bind, and profile files (`application-dev.yml`) for per-env values.

**In Robogebra** (`application.yml`) — Mongo URI, OAuth2, JWT, env overrides:

```yaml
spring:
  data:
    mongodb: { uri: "mongodb://localhost:27017/robogebra_cms_db", auto-index-creation: true }
  security:
    oauth2:
      client:
        registration:
          google: { client-id: ${GOOGLE_SSO_CLIENT_ID}, client-secret: ${GOOGLE_SSO_SECRET} }
auth:
  jwt: { secret: ${AUTH_JWT_SECRET:robogebra-sso-secret-2026}, expiry: 86400000, issuer: ${ISSUER:robogebra-auth} }
```

`${VAR:default}` = read env var, fall back to default — the pattern that keeps secrets out of the repo.

---

## Q9. `@Async` and scheduled jobs?

**Answer.** `@Async` runs a method on a thread pool (fire-and-forget) — good for I/O side-effects that shouldn't block the request. For scheduled work we use **Quartz** `Job`s (and Spring's `@Scheduled` cron elsewhere).

**In Robogebra.** Notifications are async so signup/payment responses aren't blocked (`NotificationServiceFactory`):

```java
@Async public void sendWelcomeNotification(User user) { getNotificationService(user.getUserName())...; }
@Async public void sendOtp(String userName, String otp) { getNotificationService(userName).sendOTP(userName, otp); }
```

Scheduled subscription activation (`InstituteSubscriptionActivationJob implements org.quartz.Job`):

```java
@Override public void execute(JobExecutionContext ctx) {
    instituteSubscriptionSchedulerService.handleUpdateScheduledToActiveSubscriptions();
}
```

That factory method also shows the **Factory pattern** — it returns WhatsApp vs Email notification service based on whether the username is a phone number.

---

## Q10. What design patterns show up in the codebase?

**Answer.**
- **Builder** — Lombok `@Builder` on entities/DTOs/models (fluent, immutable-ish construction).
- **Repository** — `MongoRepository` interfaces abstract data access.
- **Service layer** — orchestration between controllers and repos.
- **DTO** — decouple API from persistence.
- **Factory** — `NotificationServiceFactory.getNotificationService(username)` picks the channel.
- **Template method** — `AbstractServiceAuthenticationFilter` (base algorithm + overridable hooks).
- **Strategy** — pluggable auth services resolved per username/token type.

---

### Rapid-fire recap

| Topic | Robogebra example | File |
|-------|-------------------|------|
| DI / IoC | `@AllArgsConstructor` + `final` | `SolutionVisibilityService.java` |
| REST | `@RestController`, `@PathVariable`, `@RequestAttribute` | `SolutionVisibilityController.java` |
| Exceptions | `APIException` + `@ControllerAdvice` | `ExceptionControllerAdvice.java` |
| Validation | `@NotNull`/`@NotEmpty` + `@Valid` | `SolutionVisibilityUpdateDTO.java` |
| Transactions | `@Transactional`, `readOnly` | services |
| Auto-config | `@Bean` + `@ConditionalOnProperty` | `CacheConfig.java` |
| Config | `${VAR:default}` in yml | `application.yml` |
| Async/Sched | `@Async`, Quartz `Job` | `NotificationServiceFactory.java` |
| Patterns | Builder/Repository/Factory/Template | across domains |
