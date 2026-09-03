# 🔵 RoboGebra Code — Real Examples for Every Interview Topic

> **Why this file exists:** the strongest possible answer to any interview question is *"here's the concept — and here's where I actually used it."*
> This file maps **every major interview topic to real code in your own workspace** at `/Users/safi/workspace/robogebra-workspace`.
>
> Every topic file in this pack now carries a **🔵 In your RoboGebra code** block pointing back here.

---

## The workspace at a glance

```
robogebra-workspace/
├── robogebra-portal/        ☕ Java 17 + Spring Boot 3.2.0 + MongoDB   ← the MAIN backend
├── robogebra-admin-portal/  ☕ Java + Spring Boot                       ← admin backend
├── robogebra-mobile/        📱 Angular 18.2 + Ionic 8.7 + Capacitor 6.2 ← the flagship app
├── robogebra-web/           🌐 Angular 16.2 + Material 16 + PrimeNG 16
├── robogebra-backoffice/    🌐 Angular 16.2 + PrimeNG 16 (TypeScript 4.9)
├── robogebra-crm/           🟢 Node + Express 4 + Mongoose 8            ← the ODM example
├── robogebra-crm-web/       ⚛️ React 18 + Vite + TanStack Query + Radix
└── robogebra-website/       marketing site
```

### The version table — memorise this ⭐

| Project | Stack | Versions |
|---|---|---|
| `robogebra-portal` | Java / Spring Boot | **Java 17**, **Spring Boot 3.2.0**, Spring Security, WebFlux, Quartz, Thymeleaf |
| | Data | **Spring Data MongoDB** (not JPA ⚠️) |
| | Integrations | AWS S3 + Cognito, Firebase Admin 9.3, Razorpay 1.4.3, iText/html2pdf, java-jwt 4.4 |
| `robogebra-mobile` | Angular / Ionic | **Angular 18.2**, **Ionic 8.7.5**, **Capacitor 6.2.1**, RxJS 7.8, TypeScript 5.4 |
| `robogebra-web` | Angular | **Angular 16.2**, Material 16, PrimeNG 16, RxJS 7.8, TypeScript 5.1 |
| `robogebra-backoffice` | Angular | **Angular 16.2**, PrimeNG 16.9, TypeScript **4.9** |
| `robogebra-crm` | Node | Express 4.21, **Mongoose 8.8**, jsonwebtoken 9 |
| `robogebra-crm-web` | React | React 18.3, Vite, TanStack Query 5, react-hook-form 7, Radix UI |

> ⚠️ **The trap to never fall into:** `spring-data-jpa` appears in the portal's `pom.xml`, but there is **no JPA starter, no `DataSource` and no `@Entity`** — the portal uses **MongoDB**. Say **ODM**, not ORM. (See [24 — ORM/JPA](./24-orm-jpa-hibernate.md) section A.)

### Codebase scale — good numbers to quote

```
robogebra-portal (Java)          robogebra-mobile (Angular/Ionic)
  110 × @RestController            163 components
  207 × @Service                    89 services
   85 × @Repository                 27 route resolvers
  104 × @Document  (MongoDB)         3 HTTP interceptors
  130 × @Transactional               4 route guards
   92 × @Valid
   27 × @Aggregation pipelines     RxJS operator usage:
   26 × Java record                  map 496 · filter 485 · takeUntil 426
   16 × @Cacheable                   catchError 338 · finalize 254
   12 × @Async                       switchMap 246 · BehaviorSubject 122
```

---

# Part 1 — Java & Spring Boot topics

## ☕ `@Async` + thread pools → [32 — Multithreading](./32-multithreading.md), [06 — Spring Boot](./06-spring-boot.md)

**File:** `robogebra-portal/src/main/java/com/robogebra/cms/config/AsyncConfig.java`

This is a **textbook-perfect** answer to "have you configured a thread pool?" — it does every single thing the interview answer says you should:

```java
@Bean(NOTIFICATION_TASK_EXECUTOR)
public ThreadPoolTaskExecutor notificationTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(500);                    // BOUNDED queue ⭐
    executor.setThreadNamePrefix("push-");             // named → readable thread dumps ⭐
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());  // backpressure ⭐
    executor.setWaitForTasksToCompleteOnShutdown(true);// graceful shutdown ⭐
    executor.setAwaitTerminationSeconds(30);
    return executor;
}
```

### 🗣️ How to tell this story (45 seconds)

> *"On RoboGebra we had one shared `@Async` pool, and push notification fan-out was starving it — a single announcement touches every teacher on the platform, and each device is a blocking FCM round trip, so PostHog analytics events would queue behind it. I gave push its own `ThreadPoolTaskExecutor` with a **bounded** 500-item queue and a `CallerRunsPolicy` rejection handler, so a backlog slows the submitter down instead of growing an unbounded queue until the heap gives out — a late push beats an OOM. I also had to declare Boot's own `applicationTaskExecutor` by hand, because `TaskExecutorConfiguration` is `@ConditionalOnMissingBean(Executor.class)` — the moment you declare any executor, Boot's auto-configured one backs out and every unqualified `@Async` silently resolves to your pool."*

⭐ That last sentence about `@ConditionalOnMissingBean` is a **genuinely senior** observation. It directly demonstrates the auto-configuration answer in [06 — Spring Boot](./06-spring-boot.md).

**Related concepts it proves you know:**

```
✅ core vs max pool size          ✅ bounded queue = backpressure
✅ rejection policies              ✅ named threads for thread dumps
✅ graceful shutdown               ✅ @ConditionalOnMissingBean / auto-config back-off ⭐
✅ why one shared pool is a risk (head-of-line blocking)
```

---

## ☕ `@Cacheable` → [06 — Spring Boot](./06-spring-boot.md), [17 — Design Patterns (Proxy)](./17-solid-design-patterns.md)

**File:** `robogebra-portal/.../domain/exercisesolution/service/ExerciseSolutionService.java` (16 usages across the codebase)

```java
@Cacheable(value = CacheNames.EXERCISE_SOLUTION)
public ExerciseSolutionDTO getSolution(String exerciseItemId) { ... }

@Cacheable(value = CacheNames.EXERCISE_SOLUTION, key = "'admin-' + #exerciseItemId")
public ExerciseSolutionDTO getAdminSolution(String exerciseItemId) { ... }
```

### 🗣️ How to use it

> *"Solution content is expensive to assemble and almost never changes, so it's cached with `@Cacheable`. Note the custom `key` on the admin variant — two methods caching into the same cache region need distinct keys or the admin view would serve the student's cached payload."*

⭐ Follow-up you should volunteer: *"`@Cacheable` is proxy-based like `@Transactional`, so calling it from inside the same bean bypasses the cache entirely."* → [17 — Proxy pattern](./17-solid-design-patterns.md).

---

## ☕ `@Transactional` → [06](./06-spring-boot.md), [24](./24-orm-jpa-hibernate.md)

**130 usages** across the portal.

⭐ **The interesting nuance for MongoDB:** transactions in MongoDB require a **replica set** (Mongo 4.0+). That is a great point to raise — it shows you know `@Transactional` isn't free everywhere.

> *"We're on MongoDB, so `@Transactional` needs a replica set to work at all — it's not the same free lunch as a relational database. Most of our write paths are single-document, which Mongo makes atomic by itself, so we reach for a multi-document transaction only where we genuinely span collections."*

---

## ☕ Java records → [12 — Java 17](./12-java17-features.md)

**26 files** use `public record`, e.g. `common/exception/QuizConflictException.java`, `domain/payment/service/RazorPayService.java`.

> *"We're on Java 17, so DTOs and small value objects are records — the payment service uses them for request/response payloads. What we don't do is make an entity a record, because the Mongo mapper (like Hibernate) needs a no-arg constructor and mutable fields."*

---

## ☕ Global exception handling → [06 — Spring Boot](./06-spring-boot.md)

**Files:**
- `common/exception/handler/ExceptionControllerAdvice.java`
- `security/FilterChainExceptionHandler.java` ⭐

⭐ The second file is the interesting one, and a **great** thing to bring up:

> *"`@ControllerAdvice` only catches exceptions thrown inside the MVC dispatch. Anything thrown in a **security filter** happens *before* the DispatcherServlet, so the advice never sees it and the client gets a bare 500. We have a dedicated `FilterChainExceptionHandler` in the filter chain for exactly that gap."*

That is a distinction most candidates have never thought about.

---

## ☕ Bean validation → [06 — Spring Boot](./06-spring-boot.md)

**92 usages** of `@Valid` across the controllers, with `spring-boot-starter-validation` and `jakarta.validation-api 3.0.2`.

⚠️ Note the **`jakarta`**, not `javax` — Boot 3. → [12 — Java 17](./12-java17-features.md) migration section.

---

## ☕ Spring Security + JWT → [06 — Spring Boot](./06-spring-boot.md)

**Files:** `config/SecurityConfig.java`, `security/` package, `java-jwt 4.4.0` + `jwks-rsa`, AWS **Cognito** for identity.

> *"Auth is JWT-based and stateless. We validate against Cognito's JWKS endpoint rather than a shared secret — `jwks-rsa` fetches and caches the public keys, so the API can verify a token without ever holding a signing key."*

---

# Part 2 — MongoDB / ODM topics

## 🍃 `@Document` + `MongoRepository` → [13 — MongoDB](./13-mongodb.md), [24 — ORM vs ODM](./24-orm-jpa-hibernate.md)

**104 `@Document` classes, 85 repositories.**

**File:** `domain/exerciseitem/repository/ExerciseItemRepository.java`

```java
@Repository
public interface ExerciseItemRepository extends MongoRepository<ExerciseItemEntity, String> {

    // derived query — Spring writes it from the METHOD NAME
    Optional<ExerciseItemEntity> findByExercise_IdAndIndex(String exerciseId, String index);

    // an aggregation PIPELINE where a derived query isn't enough ⭐
    @Aggregation(pipeline = {
            "{ '$match': { 'exercise.$id': { '$in': ?0 } } }",
            "{ '$sort':  { 'displayOrder': 1 } }"
    })
    List<ExerciseItemEntity> findAllByExerciseIds(List<ObjectId> exerciseIds);
}
```

### 🗣️ The N+1 story — in Mongo ⭐

This is your **best** N+1 answer, because it's real and it's yours:

> *"The classic N+1 shows up in Mongo too. Loading a chapter's exercises and then fetching each exercise's items one by one is one query plus N. I replaced that with a single `@Aggregation` pipeline that `$match`es on `exercise.$id: { $in: [...] }` and sorts by `displayOrder` — one round trip instead of N. That's the same fix as a JPA `JOIN FETCH`, just expressed as a pipeline."*

**27 `@Aggregation` pipelines** in the codebase — quote that number.

---

## 🍃 Mongoose (Node) → [13 — MongoDB](./13-mongodb.md)

**Files:** `robogebra-crm/src/models/` — `user.model.js`, `institute.model.js`, `transaction.model.js`, `coupon.model.js`, `affiliate.model.js`, `payout.model.js`, `webhook.model.js`, `counter.model.js`

Stack: Express 4.21 + **Mongoose 8.8** + jsonwebtoken 9, layered as `routes → controllers → services → models` with `middlewares/` and `validations/`.

> *"The CRM is a Node service using Mongoose. It's the same ODM idea as Spring Data MongoDB but with schema definitions in JavaScript, and it's where I'd point if someone asks whether I've worked outside Java."*

⭐ Note `counter.model.js` — that's the classic Mongo **auto-increment sequence** pattern, since Mongo has no `AUTO_INCREMENT`.

---

# Part 3 — Angular topics

## 🅰️ HTTP interceptors → [04 — Angular](./04-angular.md), [38 — IQVIA](./38-iqvia-technical-lead-prep.md)

**File:** `robogebra-mobile/src/app/core/interceptors/header-authorization.interceptor.ts`

This one file demonstrates **six** interview topics at once:

```typescript
@Injectable()
export class HeaderAuthorizationInterceptor implements HttpInterceptor, OnDestroy {

    private readonly cancelRequests$: Subject<void> = new Subject<void>();

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (this.isAuthEndpoint(req.url)) {          // 1. don't attach a token to /auth
            return this.invokeHttpCall(req, next);
        }

        let updatedHeader: HttpHeaders = req.headers;
        if (!req.headers.has('Content-Type') && !(req.body instanceof FormData)) {
            updatedHeader = updatedHeader.set("Content-Type", 'application/json');   // 2. FormData guard ⭐
        }

        return this.authService.getAuthDetailsObservable().pipe(
            switchMap((authDetails: AuthDetails) => {                                // 3. switchMap
                if (authDetails != null) {
                    updatedHeader = updatedHeader.set("Authorization", `Bearer ${authDetails.token}`);
                }
                const cloned = req.clone({ headers: updatedHeader });                // 4. IMMUTABLE clone ⭐
                return this.invokeHttpCall(cloned, next);
            })
        );
    }

    private invokeHttpCall(req, next): Observable<HttpEvent<any>> {
        return next.handle(req).pipe(
            takeUntil(this.cancelRequests$),                                         // 5. mass-cancel on logout ⭐
            catchError((error: HttpErrorResponse) => this.handleUnAuthResponse(error, req, next))  // 6. 401 → refresh
        );
    }
}
```

### 🗣️ The six things to say about it

```
1. HttpRequest is IMMUTABLE → you MUST req.clone(); mutating it does nothing ⭐
2. FormData guard — setting Content-Type manually BREAKS multipart file uploads,
   because the browser must generate the boundary itself ⭐⭐
3. switchMap because the token itself arrives asynchronously (from storage)
4. The auth endpoints are skipped, or login would need a token to get a token 🐔🥚
5. takeUntil(cancelRequests$) lets logout cancel every in-flight request at once ⭐
6. catchError centralises 401 → refresh-token → retry, in ONE place instead of
   in every service
```

⭐ Point 2 is the one that makes interviewers sit up — it's a real bug most people have shipped.

---

## 🅰️ Route guards → [04 — Angular](./04-angular.md)

**File:** `robogebra-mobile/src/app/core/guard/auth-guard.service.ts`

```typescript
canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.authService.isLoggedInObservable().pipe(
        tap(isLoggedIn => {
            if (!isLoggedIn) {
                this.authService.setRedirectUrl(state.url);   // ⭐ remember where they wanted to go
                this.router.navigate(['/' + AppRoutes.LOGIN]);
            }
        }),
        map(isLoggedIn => {
            if (!isLoggedIn) return false;

            if (this.authService.isLoggedInAsParent()) {       // ⭐ ROLE-based routing
                const targetPath = state.url.split('?')[0].replace(/^\//, '');
                const isAllowed = PARENT_ALLOWED_ROUTES.some(r =>
                    targetPath === r || targetPath.startsWith(r + '/'));
                if (!isAllowed) {
                    this.router.navigate(['/' + AppRoutes.PARENT_DASHBOARD]);
                    return false;
                }
            }
            return true;
        })
    );
}
```

### 🗣️ Two details worth volunteering

> *"The guard returns an `Observable<boolean>`, not a plain boolean, because login state itself is async — the router waits for it to emit. And it stores the requested URL before redirecting, so after login the user lands where they originally wanted instead of on a generic home page. On top of authentication we do role-based routing: a parent account is only allowed into the parent routes and is bounced to the dashboard otherwise."*

Other guards: `login-guard.service.ts` (keeps a logged-in user off `/login`), `public-preview-guard.service.ts`, `anonymous-solution-guard.service.ts`.

---

## 🅰️ RxJS in production → [20 — RxJS Operators](./20-rxjs-operators.md)

Real usage counts in `robogebra-mobile` (quote these — they prove it isn't textbook knowledge):

```
map 496 · filter 485 · takeUntil 426 · catchError 338 · finalize 254
switchMap 246 · BehaviorSubject 122 · forkJoin 22 · combineLatest 21
debounceTime 13 · distinctUntilChanged 9 · shareReplay 3 · takeUntilDestroyed 5
```

### `debounceTime` + `switchMap` — the type-ahead search ⭐

**File:** `features/pages/study-materials/study-materials.component.ts:288`

```typescript
this.loadVideosSubject$.pipe(
    takeUntil(this.destroy$),
    debounceTime(500),                       // ⭐ wait for the user to stop typing
    tap(() => (this.isLoading = true)),
    filter(() => {                            // ⭐ don't search on 1-2 characters
        const query = this.searchTerm?.trim() || '';
        return query.length === 0 || query.length >= 3;
    }),
    switchMap(() => this.studyMaterialService.search(query, tags, ...))  // ⭐ CANCELS the previous request
)
```

> *"This is the canonical search pipeline. `debounceTime(500)` stops us firing a request per keystroke, the `filter` avoids a pointless search on one or two characters, and `switchMap` cancels the previous in-flight request — which is what prevents the out-of-order-response bug where a slow query for 'ma' lands after a fast one for 'maths' and overwrites the correct results."*

⭐ That last clause is the **real reason** for `switchMap`, and it's what separates a memorised answer from an experienced one.

### `forkJoin` — parallel startup calls

**File:** `app.component.ts:166`

```typescript
this.isLoggedIn$().pipe(
    filter(Boolean),
    filter(() => !this.authService.isLoggedInAsParent()),
    switchMap(() => forkJoin({                       // ⭐ three calls IN PARALLEL
        userSummary:    this.userSummaryService.fetchUserSummary(),
        userProfile:    this.userService.fetchUserProfile(false),
        userPreference: this.userPreferenceService.fetchUserPreference(false)
    })),
    takeUntil(this.destroy$)
).subscribe({ next: async ({ userSummary, userProfile, userPreference }) => { ... } });
```

> *"Three independent calls after login. Sequentially that's the sum of the latencies; `forkJoin` makes it the slowest of the three. Note it's the **object** form of `forkJoin`, so the result destructures by name rather than by array index — much harder to get wrong."*

### `takeUntil(this.destroy$)` — 426 usages ⭐

The memory-leak answer, everywhere in the codebase:

```typescript
private readonly destroy$ = new Subject<void>();

ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
}
```

> *"Every manual subscription is unsubscribed with `takeUntil(this.destroy$)` — there are over 400 of them. `takeUntil` must be the **last** operator in the pipe, otherwise operators after it keep their own subscription alive. On Angular 16+ we've started using `takeUntilDestroyed()` instead, which removes the boilerplate."*

⭐ The "`takeUntil` must be last" rule is a classic follow-up.

---

## 🅰️ `BehaviorSubject` state services → [04](./04-angular.md), [21 — NgRx](./21-ngrx.md)

**File:** `core/services/user-summary.service.ts` (122 `BehaviorSubject` usages overall)

```typescript
@Injectable({ providedIn: 'root' })                        // ⭐ singleton, tree-shakable
export class UserSummaryService {
    private userSummarySubject$ = new BehaviorSubject<UserSummaryGroupDTO>(null);

    getUserSummarySubject(): Observable<UserSummaryGroupDTO> {
        return this.userSummarySubject$.asObservable();     // ⭐ expose read-only
    }

    getUserSummary(): UserSummaryGroupDTO {
        return this.userSummarySubject$.getValue();         // ⭐ synchronous read
    }
}
```

### 🗣️ The "why not NgRx?" answer ⭐

> *"We use `BehaviorSubject`-backed services rather than NgRx. `BehaviorSubject` gives the two things we actually needed — a current value on subscribe, and a synchronous `getValue()` for callers that can't subscribe, like the institute-code HTTP interceptor. NgRx would have added actions, reducers, effects and selectors for state that only a handful of components read. I'd reach for NgRx when state is shared across many unrelated features and I need time-travel debugging or strict traceability."*

⭐ Note the pattern of **exposing `asObservable()`** and keeping the subject private — that stops any component calling `.next()` and mutating global state from anywhere. Say that.

---

## 🅰️ `ChangeDetectionStrategy.OnPush` → [04 — Angular](./04-angular.md), [38 — IQVIA](./38-iqvia-technical-lead-prep.md)

Used in 6 components, on exactly the right ones — the heavy list and modal views:

```
features/pages/learning-progress/chapter-progress-list/    ← a long list
features/pages/learning-progress/chapter-detail-modal/
features/pages/settings/payments/
features/pages/books/exercise-solution/.../solution-step-ai-assistant-editor/
shared/video-splash-screen/
```

> *"`OnPush` is applied selectively rather than globally — on the long progress lists and the AI-assistant editor, where default change detection was re-rendering on every unrelated event. With `OnPush` the component only re-checks when an `@Input` reference changes, an event fires inside it, or an `async` pipe emits — which is also why we pass new object references instead of mutating in place."*

⭐ `trackBy` appears **55 times** — mention it alongside `OnPush` as the other half of list performance.

---

## 🅰️ Route resolvers → [04 — Angular](./04-angular.md)

**27 resolvers** in `core/resolvers/`: `book-list.resolver.ts`, `chapter.resolver.ts`, `exercise-context.resolver.ts`, `chapter-access-ruler.resolver.ts`, `children.resolver.ts` …

> *"We pre-fetch route data with resolvers so a page never renders in a half-loaded state. The access-ruler resolvers are the interesting ones — they decide what a user is allowed to see for that chapter or exercise before the route activates, so the component never has to render a 'you don't have access' flash."*

---

## 🅰️ Standalone components & modern Angular → [04](./04-angular.md), [38 — IQVIA](./38-iqvia-technical-lead-prep.md)

Be **honest and precise** here — it's a strength, not a weakness:

```
robogebra-mobile is Angular 18.2, but still largely NgModule-based:
   standalone: true       → 2 components  (migration started)
   inject()               → 30 usages     (modern DI, adopted)
   takeUntilDestroyed()   → 5 usages      (modern teardown, adopted)
   signal() / computed()  → 0             ⚠️ not adopted yet
```

> *"We're on Angular 18 but the app is still mostly NgModule-based — it grew from Angular 12 and a big-bang standalone migration was never worth the regression risk. We've adopted the pieces that are cheap and safe: `inject()` in newer code, `takeUntilDestroyed()` for teardown, and standalone for new components. Signals I've studied but haven't shipped — the migration path we'd take is signal inputs on leaf components first, since those are the lowest-risk."*

⭐ **This is the right answer.** Claiming full signals adoption invites one follow-up you can't back up. Naming the *migration strategy* is what a lead is expected to have.

---

## 🅰️ Angular forms → [25 — Angular Binding & Forms](./25-angular-binding-forms.md)

```
Validators.* → 67 usages
FormBuilder  → 2 usages     ⚠️ mostly template-driven / manual FormGroup
```

> *"Most forms are reactive with explicit `FormGroup`s and custom validators — 67 validator usages across the app. The parent-info form is the interesting one: it does an async availability check with `debounceTime` and `distinctUntilChanged` before validating."*

**File:** `features/pages/settings/profile/parent-info/parent-info.component.ts:365`

---

# Part 4 — Ionic & Capacitor topics

## 📱 Capacitor plugins → [15 — Ionic Level 1](./15-ionic-level1.md), [38 — IQVIA](./38-iqvia-technical-lead-prep.md)

**19 Capacitor plugins in production.** This is your strongest Ionic evidence — quote the list:

```
@capacitor/app                  app state, deep links, back button
@capacitor/browser              in-app browser
@capacitor/device               device info
@capacitor/haptics              tactile feedback
@capacitor/keyboard             keyboard events + resize behaviour
@capacitor/network              online/offline detection ⭐
@capacitor/preferences          native key-value storage ⭐
@capacitor/push-notifications   + @capacitor-firebase/messaging (FCM) ⭐
@capacitor/screen-orientation
@capacitor/splash-screen
@capacitor/status-bar
@capacitor-community/firebase-analytics
@capawesome/capacitor-app-update  in-app update prompts ⭐
capacitor-native-settings         deep-link into OS settings
capacitor-razorpay                native payments ⭐
```

### 🗣️ The story to tell

> *"The mobile app is Ionic 8 with Capacitor 6 and about nineteen native plugins. The ones with real complexity were push notifications — Capacitor Firebase messaging on the client and Firebase Admin on the Spring side, with a dedicated bounded thread pool for the fan-out — and native Razorpay for payments, because the web checkout flow doesn't survive an app-store review. We also use `@capacitor/network` for offline detection and `@capacitor/preferences` rather than `localStorage`, since WebView storage can be evicted by the OS."*

⭐ That last point — **`Preferences` over `localStorage` because the OS can evict WebView storage** — is a genuinely good, specific Ionic answer.

⭐ `@capawesome/capacitor-app-update` is another strong detail: it shows you thought about the fact that **users don't update apps**, unlike a web deploy.

---

# Part 5 — React (the CRM web)

## ⚛️ `robogebra-crm-web` → useful if anyone asks "only Angular?"

```
React 18.3 + Vite + TypeScript 5.8
TanStack Query 5      ← server-state caching (the React answer to a state library)
react-hook-form 7     ← forms
react-router-dom 6    ← routing
Radix UI + shadcn/ui  ← headless component primitives
```

> *"The CRM front end is React with Vite, TanStack Query and react-hook-form. I'm primarily an Angular developer, but I've worked across both — the mental model transfers directly: TanStack Query is doing what an NgRx entity store plus effects would do, and react-hook-form maps onto reactive forms."*

---

# Part 6 — Cross-cutting: what this codebase proves you can discuss

| Interview topic | Your evidence |
|---|---|
| Thread pools, `@Async`, backpressure | `AsyncConfig` — bounded queue + `CallerRunsPolicy` ⭐ |
| Spring auto-configuration | the `@ConditionalOnMissingBean(Executor.class)` back-off story ⭐ |
| Caching | 16 × `@Cacheable` with custom keys |
| N+1 and query optimisation | 27 `@Aggregation` pipelines replacing per-item lookups ⭐ |
| ORM vs ODM | Spring Data MongoDB + Mongoose — and knowing the difference ⭐ |
| Exception handling | `@ControllerAdvice` **plus** a filter-chain handler for pre-MVC errors ⭐ |
| Security / JWT | Cognito + JWKS validation, stateless |
| RxJS depth | 426 `takeUntil`, 246 `switchMap`, the debounce+switchMap search ⭐ |
| HTTP interceptors | token attach, FormData guard, mass-cancel, 401 refresh ⭐ |
| Route guards + resolvers | 4 guards, 27 resolvers, role-based routing |
| Change detection / performance | selective `OnPush` + 55 × `trackBy` |
| State management | `BehaviorSubject` services, and *why not NgRx* ⭐ |
| Ionic / Capacitor | 19 plugins, push, native payments, offline ⭐ |
| Migration judgement | Angular 18 on an NgModule codebase — a staged plan, not a big bang ⭐ |
| Polyglot | Java, TypeScript, Node/Express, React |

---

## ⚠️ Three things to be careful about

```
1. RoboGebra uses MONGODB, not JPA.
   The unused spring-data-jpa dependency in the pom is a TRAP.
   Say "Spring Data MongoDB — an ODM". ⭐

2. Signals are NOT used (0 usages). Don't claim them.
   Say what you've studied and what your migration plan would be. ⭐

3. Standalone components are barely adopted (2). The app is NgModule-based.
   Frame it as a deliberate risk decision, not as being behind. ⭐
```

---

## 🗣️ The 60-second "tell me about your architecture" answer

> *"RoboGebra is an AI-assisted maths learning platform. The backend is Java 17 on Spring Boot 3.2 with MongoDB — around 110 REST controllers and 200 services, using Spring Data MongoDB with aggregation pipelines where derived queries aren't enough. Auth is stateless JWT validated against AWS Cognito's JWKS; we use S3 for assets, Firebase for push, Razorpay for payments, and Quartz for scheduled jobs.*
>
> *On the front end there are three Angular applications plus a React CRM. The flagship is the Ionic 8 / Capacitor 6 mobile app on Angular 18, with about nineteen native plugins. State is managed with `BehaviorSubject`-backed services rather than NgRx, since the sharing is localised; route guards and resolvers handle access control and pre-fetching, and there's a single HTTP interceptor for token attachment and 401 refresh.*
>
> *The parts I'd call out as engineering rather than plumbing: separating the push-notification thread pool with a bounded queue and caller-runs backpressure, and replacing per-item lookups with `$match`/`$in` aggregation pipelines to kill an N+1."*

---

**Related files:** [05 Core Java](./05-java.md) · [06 Spring Boot](./06-spring-boot.md) · [13 MongoDB](./13-mongodb.md) · [24 ORM/JPA](./24-orm-jpa-hibernate.md) · [32 Multithreading](./32-multithreading.md) · [04 Angular](./04-angular.md) · [15 Ionic](./15-ionic-level1.md) · [20 RxJS](./20-rxjs-operators.md) · [18 RoboGebra Technical Versions](./18-robogebra-technical-versions.md)
