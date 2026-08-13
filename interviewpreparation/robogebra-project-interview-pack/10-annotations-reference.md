# 10. Annotations Reference — every annotation you use, with a real file

A quick-recall census of the annotations actually used in Robogebra. Interviewers love *"what does this annotation do and where did you use it?"* — this is your answer sheet. The **custom `@interface` annotations** at the end are your differentiator.

---

## Backend — Spring Core / DI

| Annotation | What it does | Used in |
|---|---|---|
| `@RestController` | `@Controller` + `@ResponseBody` — JSON REST endpoints | `SolutionVisibilityController` |
| `@Service` | business-logic bean | `SolutionVisibilityService` |
| `@Repository` | data-access bean (+ exception translation) | `ExerciseSolutionRepository` |
| `@Component` | generic Spring bean | `JwtAuthenticationFilter` |
| `@Configuration` | declares bean config | `CacheConfig`, `MailConfig` |
| `@Bean` | factory method → managed bean | `OpenApiConfig`, `CacheConfig` |
| `@Autowired` | inject dependency (we prefer constructor) | `WebMvcConfig` |
| `@Value` | inject a property value | `GoogleAuthSuccessHandler` |
| `@ConfigurationProperties` | bind a config block to a class | `UsageLimitConfiguration` |
| `@Qualifier` / `@Primary` | pick among multiple beans | `FilterChainExceptionHandler` / `CognitoConfig` |
| `@Profile` | bean only for a profile (local/prod) | `CognitoConfig` |
| `@Order` | filter/interceptor/bean ordering | `JwtAuthenticationFilter` (6), `PermissionInterceptor` (7) |
| `@ConditionalOnProperty` | bean only if property set | `CacheConfig`, `WebClientConfig` |
| `@EnableCaching` / `@EnableAsync` | turn on caching / async | `CacheConfig` / `RobogebraApplication` |

## Backend — Web / REST

| Annotation | What it does | Used in |
|---|---|---|
| `@RequestMapping` | base path for a controller | `SolutionStepFeedbackController` |
| `@GetMapping` / `@PostMapping` / `@PutMapping` / `@PatchMapping` / `@DeleteMapping` | map HTTP verbs | across controllers |
| `@PathVariable` | bind URL segment | `/exercise-items/{id}/...` |
| `@RequestParam` | bind query/form param | feedback/analytics controllers |
| `@RequestBody` | bind JSON body | `updateStatus(...)` |
| `@RequestAttribute` | bind value set by a filter (the authed user!) | `@RequestAttribute(INSTITUTE_USER_ATTRIBUTE)` |
| `@DateTimeFormat` | parse date params | `ParentAnalyticsController` |

## Backend — Validation

| Annotation | What it does | Used in |
|---|---|---|
| `@Valid` / `@Validated` | trigger bean validation | `@Valid @RequestBody ...` |
| `@NotNull` / `@NotEmpty` / `@NotBlank` | presence rules | `SolutionVisibilityUpdateDTO`, `CreateInstituteRequestDTO` |
| `@Size` / `@Min` / `@Max` | length/range | `AdminSignUpRequestDTO`, `CacheProperties` |
| `@Email` / `@Pattern` | format rules | `ParentOtpVerifyRequestDTO`, `CreateInstituteRequestDTO` |

## Backend — Spring Data MongoDB

| Annotation | What it does | Used in |
|---|---|---|
| `@Document` | class → Mongo collection | `ExerciseSolutionEntity` |
| `@Id` | primary key `_id` | every entity |
| `@Field` | custom BSON field name | entities |
| `@Indexed` | single-field index | `SolutionVisibilityEntity` |
| `@CompoundIndex(es)` | multi-field / unique index | `ExerciseEntity`, `SolutionVisibilityEntity` |
| `@DBRef` | reference another document | `ExerciseEntity.book/chapter` |
| `@CreatedDate` / `@LastModifiedDate` | audit timestamps | entities |
| `@Query` / `@Aggregation` | custom query / pipeline | `ExerciseRepository` |
| `@Transactional` | transactional boundary | write services |

## Backend — Lombok

| Annotation | What it does | Used in |
|---|---|---|
| `@Getter` / `@Setter` | accessors | DTOs/entities everywhere |
| `@Builder` | fluent builder | models/DTOs |
| `@AllArgsConstructor` | all-args ctor (→ constructor injection) | services |
| `@NoArgsConstructor` | no-arg ctor (deserialization) | DTOs/entities |
| `@RequiredArgsConstructor` | ctor for `final` fields | `GoogleAuthSuccessHandler` |
| `@Data` | getters+setters+equals+hashCode+toString | `BitlyProperties` |
| `@Slf4j` | `log` field for SLF4J | filters/services |

## Backend — Exceptions, Async, Swagger

| Annotation | What it does | Used in |
|---|---|---|
| `@ControllerAdvice` + `@ExceptionHandler` | global error handling | `ExceptionControllerAdvice` |
| `@Async` | run on a thread pool | `NotificationService` |
| `@Tag` / `@Operation` / `@ApiResponse` / `@Schema` | OpenAPI/Swagger docs | controllers |

---

## Backend — CUSTOM annotations (your differentiator)

These are project-defined `@interface`s — mentioning them signals real senior ownership.

**Authorization / auth-bypass:**

```java
@Target(ElementType.METHOD) @Retention(RUNTIME)
public @interface PermissionContext { Permission[] userPermission(); }   // endpoint permission gate

@Target(ElementType.ANNOTATION_TYPE) @Retention(RUNTIME)
public @interface BypassJwtAuth { }                                       // meta-annotation
```

- `@PermissionContext(userPermission={...})` — declares required permissions; enforced by `PermissionInterceptor`.
- `@BypassJwtAuth` — meta-annotation; marks `@CrmServiceAPI`, `@InternalServiceAPI`, `@WebhookSecured` so those endpoints skip user-JWT and use a service token instead.
- `@BookAccessRequired` — gates endpoints behind subscription/book access (enforced by `BookAccessRestrictionFilter`).

**Custom cross-cutting behavior:**
- `@FeatureUsageQuota` — endpoint counts against a plan quota (`FeatureUsageLimitFilter`).
- `@PreferredLanguage` — apply the user's language (`PreferredLanguageFilter`).
- `@NotificationEnabled` — method triggers a notification.

**Custom validators (Bean Validation with `@Constraint`):**

```java
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Target(ElementType.TYPE) @Retention(RUNTIME)
public @interface PasswordMatches { String message() default "Password does not match"; ... }
```

- `@PasswordMatches` — class-level cross-field check (password == confirm).
- `@ValidUserName` — field-level username format validator.

**Talking point:** *"We built custom annotations to keep cross-cutting concerns declarative — `@PermissionContext` for authz, `@BypassJwtAuth` for service auth, `@FeatureUsageQuota` for plan limits, and `@PasswordMatches`/`@ValidUserName` custom validators. Each is backed by an interceptor, filter, or `ConstraintValidator`, so controllers stay clean and the intent is readable at the endpoint."*

---

## Frontend — Angular decorators

| Decorator | What it does | Used in |
|---|---|---|
| `@Component` | component (selector/template/styles) | `app.component.ts` |
| `@NgModule` | module (declarations/imports/providers) | `app.module.ts` |
| `@Injectable` | injectable service (`providedIn:'root'`) | `auth.service.ts`, interceptors |
| `@Directive` | custom directive | `graph-pan-zoom.directive.ts` |
| `@Input` / `@Output` | parent→child data / child→parent events | feature components (modals) |
| `@ViewChild` / `@ViewChildren` | reference child elements/components | `app.component.ts`, `solution-step.component.ts` |
| `@HostListener` | listen to host/window DOM events | `app.component.ts` |
| `@Inject` / `@Optional` | DI token / optional dependency | `app.component.ts`, `chapter-detail-modal` |

---

### 30-second recall

- **DI:** `@Service/@Repository/@RestController` + constructor injection (`@AllArgsConstructor`).
- **REST:** `@GetMapping/@PostMapping/@PatchMapping` + `@PathVariable/@RequestBody/@RequestAttribute`.
- **Validation:** `@Valid` + `@NotNull/@NotEmpty/@Email/@Pattern` (+ custom `@PasswordMatches`).
- **Mongo:** `@Document/@Id/@Indexed/@CompoundIndex/@DBRef/@CreatedDate`.
- **Errors:** `@ControllerAdvice` + `@ExceptionHandler`.
- **Custom:** `@PermissionContext`, `@BypassJwtAuth`, `@FeatureUsageQuota`.
- **Angular:** `@Component/@Injectable/@Input/@Output/@ViewChild/@HostListener`.
