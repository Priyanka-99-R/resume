# 3. Java (Core) — Interview Q&A tied to Robogebra

**Stack:** Java 17, Maven, in `robogebra-portal` (main backend) and `robogebra-admin-portal`.

---

## Q1. Explain the four OOP principles with real examples from your project.

**Answer (one-liners first, then a real Robogebra example for each):**

- **Encapsulation** — bundle data + behavior, hide internals; expose intent.
- **Abstraction** — program to a contract, hide the "how".
- **Inheritance** — share a skeleton via a base class.
- **Polymorphism** — same call, different behavior at runtime.

### 1) Encapsulation — `SolutionVisibility` model

Fields are private; callers get **behavior**, not raw state. The status comparison is hidden behind `isEnabled()`, and conversions are methods on the object — nobody outside reaches in:

```java
// domain/solutionvisibility/model/SolutionVisibility.java
public class SolutionVisibility {
    private String klassId;
    private String targetId;
    private SolutionVisibilityStatus status;          // internal representation, hidden

    public boolean isEnabled() { return this.status == SolutionVisibilityStatus.ENABLED; }
    public SolutionVisibilityEntity toEntity() { return SolutionVisibilityEntity.builder()...build(); }
    public SolutionVisibilityDTO  toDTO()    { return SolutionVisibilityDTO.builder()...build(); }
}
```

At the **architecture level**, the **Entity ↔ Model ↔ DTO** trio is encapsulation too: Entity = MongoDB shape, Model = business logic, DTO = API shape. Each hides its concern, so the DB schema, internal logic, and public contract can each change independently. Same idea in `InstituteUser.validateAsTeacher()` — the role rule is encapsulated on the object, not duplicated by every caller.

### 2) Abstraction — repository & service interfaces

I program to contracts and let Spring supply the implementation. `SolutionVisibilityRepository` is just an **interface** — I declare *what* I want (`findByKlassIdAndTargetId`), Spring Data generates the *how*:

```java
// repository/SolutionVisibilityRepository.java
@Repository
public interface SolutionVisibilityRepository extends MongoRepository<SolutionVisibilityEntity, String> {
    Optional<SolutionVisibilityEntity> findByKlassIdAndTargetId(String klassId, String targetId);
    List<SolutionVisibilityEntity> findByKlassIdInAndTargetIdIn(List<String> klassIds, List<String> targetIds);
}
```

Same with the `NotificationService` interface — callers depend on the abstraction, never on Email/SMS/WhatsApp concretely (see polymorphism below).

### 3) Inheritance — service-auth filter hierarchy

All three service-to-service auth filters share one base that extends Spring's `OncePerRequestFilter`, so they inherit the whole validation flow and only fill in specifics:

```java
// security/AbstractServiceAuthenticationFilter.java
public abstract class AbstractServiceAuthenticationFilter extends OncePerRequestFilter {
    protected abstract Class<? extends Annotation> getAnnotationClass();
    protected abstract String  getHeaderName();
    protected abstract String  getExpectedToken();
    protected abstract UserRole getUserRole();
    protected void additionalValidation(HttpServletRequest req) { /* default no-op hook */ }
    // doFilterInternal(): applies? → validateTokenExists → validateToken → additionalValidation → authenticate
}

// CrmServiceAuthenticationFilter, InternalServiceAuthenticationFilter, WebhookAuthorizationFilter
//   each `extends AbstractServiceAuthenticationFilter` and inherits the algorithm
```

That's a **template method** pattern: the base owns the algorithm, subclasses supply the steps.

### 4) Polymorphism — notification channel resolved at runtime

`NotificationService` is an interface with `EmailNotificationService`, `SmsNotificationService`, `WhatsAppNotificationService` implementations. A **factory** returns the right one by looking at the username, and the caller invokes `sendOTP(...)` **without knowing which concrete type** it holds — the correct override runs at runtime:

```java
// domain/notification/service/NotificationServiceFactory.java
@Async
public void sendOtp(String userName, String otp) {
    getNotificationService(userName).sendOTP(userName, otp);   // polymorphic call
}
private NotificationService getNotificationService(String username) {
    return username.matches(RegexPatternUtils.USER_NAME_PHONE_NUMBER_PATTERN)
        ? whatsAppNotificationService   // phone → WhatsApp impl
        : emailNotificationService;     // else  → Email impl
}
```

Add a new channel later → implement the interface, no caller changes. Method-level polymorphism also shows in every `toModel()`/`toEntity()` override and the abstract filter's `getUserRole()` per subclass.

**Two-line summary to say:** *"Encapsulation is our Entity/Model/DTO objects hiding state behind methods like `isEnabled()`; abstraction is our repository/service interfaces; inheritance is the `AbstractServiceAuthenticationFilter` base the CRM/internal/webhook filters extend; polymorphism is the `NotificationService` interface where a factory picks WhatsApp vs Email and the caller just calls `sendOTP()`."*

---

## Q2. Collections — where do you use List/Map/Set and ArrayList?

**Answer.** `List`/`ArrayList` for ordered collections (query results, ids); `Map`/`HashMap` for lookups and grouping; `Set` for uniqueness. I favor the `List` *interface* as the declared type (program to interface) and immutable `List.of(...)` for fixed small lists.

**In Robogebra.** A real `Map` built by grouping, then used for fast lookup — the solution-visibility access check (`SolutionVisibilityService.java`):

```java
// Group DB rows into a Map<targetId, rows> for O(1) precedence lookup
Map<String, List<SolutionVisibilityEntity>> rowsByTarget =
    solutionVisibilityPersistenceService.findByKlassIdsAndTargetIds(klassIds, precedence)
        .stream()
        .collect(Collectors.groupingBy(SolutionVisibilityEntity::getTargetId));

// Immutable ordered List for the precedence walk (most specific first)
List<String> precedence = List.of(exerciseItemId, exerciseId, chapterId);
for (String targetId : precedence) {
    List<SolutionVisibilityEntity> rows = rowsByTarget.get(targetId);
    if (rows != null && !rows.isEmpty())
        return rows.stream().anyMatch(r -> r.getStatus() == SolutionVisibilityStatus.ENABLED);
}
```

This is a great "why this data structure" story: I fetch all candidate rows in **one** DB call, `groupingBy` into a `Map` so each precedence lookup is O(1), and walk an **ordered immutable `List`** so the most-specific rule wins. Alternative (a DB call per level, or nested loops over a `List`) would be N queries / O(n²).

**Follow-up: `ArrayList` vs `LinkedList`?** `ArrayList` = array-backed, O(1) random access, cache-friendly — my default. `LinkedList` only wins for frequent head/middle inserts. In practice I've never needed `LinkedList` here; stream `.collect(Collectors.toList())` gives an `ArrayList`.

**Follow-up: `HashMap` internals?** Array of buckets; key `hashCode()` → bucket, `equals()` resolves collisions; since Java 8 a bucket becomes a balanced tree past 8 entries (O(log n) worst case). That's why entity keys need correct `hashCode`/`equals`.

---

## Q3. Java Streams — show real pipelines and explain them.

**Answer.** Streams give me declarative, composable data processing. Key pieces: `map` (transform), `filter` (keep), `flatMap` (flatten nested), `distinct`, terminal `collect`/`toList`/`anyMatch`.

**In Robogebra.**

**map + distinct + collect** — apply a visibility update to each class, de-duped (`SolutionVisibilityService`):

```java
return klassIds.stream()
        .distinct()
        .map(klassId -> updateStatusForKlass(teacher, klassId, targetId, status))
        .collect(Collectors.toList());
```

**anyMatch** — short-circuit "is it enabled for *any* of the student's classes":

```java
return rows.stream().anyMatch(r -> r.getStatus() == SolutionVisibilityStatus.ENABLED);
```

**flatMap (nested streams)** — build caches from a grouped config (`CacheConfig`):

```java
List<CaffeineCache> caches = cacheProperties.getManager().stream()
    .flatMap(group -> group.getCacheNames().stream()
        .map(name -> new CaffeineCache(name, Caffeine.newBuilder()
            .expireAfterWrite(group.getTtlMinutes(), TimeUnit.MINUTES)
            .maximumSize(group.getMaxSize()).build())))
    .toList();
```

**map on a `Page`** — convert entities to models while preserving pagination (`SolutionStepFeedbackService`):

```java
return repository.findByReviewedUser_Id(userId, pageable).map(SolutionStepFeedbackEntity::toModel);
```

**Follow-up: intermediate vs terminal / laziness?** `map`/`filter`/`flatMap` are lazy intermediates — nothing runs until a terminal (`collect`, `toList`, `anyMatch`, `forEach`) pulls values. `anyMatch` also **short-circuits**, so it stops at the first match.

---

## Q4. Which Java 17 features do you use, and where?

**Answer.** We're on **Java 17 LTS**. The ones I actually use:

**Switch expressions (arrow syntax)** — no fall-through, exhaustive, can return a value. Dispatch by enum without break-bugs (`SolutionVisibilityService`):

```java
switch (level) {
    case CHAPTER       -> chapterService.validateExistence(targetId);
    case EXERCISE      -> exerciseService.validateExistence(targetId);
    case EXERCISE_ITEM -> {
        if (Boolean.FALSE.equals(exerciseItemService.validateExerciseItem(targetId)))
            throw new APIException(HttpStatus.NOT_FOUND, "Exercise item not found.");
    }
}
```

As a **value** (`ExceptionControllerAdvice`), mapping an error type to an HTTP status — note grouped cases:

```java
HttpStatus status = switch (ex.getErrorType()) {
    case INVALID_COUPON                                   -> HttpStatus.BAD_REQUEST;
    case SERVICE_UNAVAILABLE, LAMBDA_ERROR, NETWORK_ERROR -> HttpStatus.SERVICE_UNAVAILABLE;
    case PARSING_ERROR                                    -> HttpStatus.INTERNAL_SERVER_ERROR;
};
```

**`Optional`** — no null-checks; express "maybe absent" in the type (`RefreshTokenService`):

```java
return refreshTokenRepository.findByUserId(userId)
        .map(UserRefreshTokenEntity::getRefreshToken)
        .orElse(null);
// elsewhere: .orElseThrow(() -> new ResourceNotFoundException("Solution Step Feedback", id));
// and: repository.findByUserId(userId).ifPresent(entity -> { ... });
```

**`.toList()`** (Java 16+) — a concise, immutable terminal that replaces `Collectors.toList()` (`SolutionVisibilityController`):

```java
return service.updateStatus(...).stream().map(SolutionVisibility::toDTO).toList();
```

**`var`** for local inference, **text blocks** for multi-line strings/JSON, and I understand **records** (immutable data carriers) and **sealed classes** (restricted hierarchies) even where our older DTOs still use Lombok classes.

**Follow-up: records vs Lombok?** A `record` is a concise, *immutable* data class with auto `equals/hashCode/toString`. Our DTOs use Lombok `@Builder`/`@Getter/@Setter` because they're **mutable** (builders, framework deserialization). If I were writing new immutable DTOs I'd reach for `record`.

---

## Q5. `equals()`/`hashCode()`, immutability, `final` — quick senior checks.

**Answer.**
- **`equals`/`hashCode` contract** — equal objects must have equal hash codes; override both together. Critical for `HashMap`/`HashSet` keys (see Q2 grouping).
- **Immutability** — `final` fields + no setters → thread-safe, cacheable. Our injected dependencies are all `private final` (constructor injection), which is immutability applied to services.
- **`final`** — on fields (assign once), params (no reassignment), classes/methods (no override).

**In Robogebra.** Every service holds `private final` collaborators:

```java
private final SolutionVisibilityPersistenceService solutionVisibilityPersistenceService;
private final StudentEnrollmentService studentEnrollmentService;
```

`final` + Lombok `@AllArgsConstructor` = constructor injection with immutable, non-reassignable dependencies — safe to share across threads.

---

### Rapid-fire recap

| Topic | Robogebra example | File |
|-------|-------------------|------|
| OOP (Entity↔Model↔DTO) | `toEntity()/toModel()/toDTO()` | `SolutionVisibility.java` |
| Abstraction/Polymorphism | abstract filter + template method | `AbstractServiceAuthenticationFilter.java` |
| Collections/Map | `groupingBy` + `List.of` precedence | `SolutionVisibilityService.java` |
| Streams | `map/distinct/collect`, `anyMatch`, `flatMap` | service, `CacheConfig.java` |
| Java 17 switch | arrow + value-returning switch | service, `ExceptionControllerAdvice.java` |
| Optional | `map/orElse/orElseThrow/ifPresent` | `RefreshTokenService.java` |
| Immutability | `private final` deps | every `@Service` |
