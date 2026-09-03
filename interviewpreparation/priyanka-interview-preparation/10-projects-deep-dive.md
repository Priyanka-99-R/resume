# Projects — Deep-Dive Interview Q&A (Easy Version)

> Experience / "project difficulty" round. Interviewers cross-question on real decisions, trade-offs and concrete technical detail. Speak in the first person, give numbers where you can, and always close a "how did you solve it" answer with the **outcome**.
>
> ⚠️ **Corrected in this version:** RoboGebra's database is **MongoDB**, not MySQL (verified: 104 `@Document` classes, 27 `@Aggregation` pipelines). Two answers below still said MySQL — a follow-up like *"how did you model the joins?"* would have exposed it. 🔵 blocks now cite the **real files**.

---

## 🧠 The easiest way to answer ANY project question — CAPS

```
C — CONTEXT   what the product does, in ONE sentence
A — ACTION    what I BUILT (name the modules) ⭐
P — PROBLEM   one hard thing, and the trade-off I chose ⭐⭐
S — SCALE     numbers, versions, real details ⭐
```

⭐ **The P is what actually gets you hired.** Anyone can list features. Only someone who built it can explain what was *hard* and why they chose one option over another.

```
WEAK   "I built the quiz module using Angular and Java."
                ↑ this is a resume line read aloud 💥

STRONG "I built the quiz module. The hard part was the live graph —
        recomputing on every slider tick janked the UI and hammered
        the AI endpoint, so I split it into two streams: local maths
        redraws instantly, and the AI insight is debounced 400ms with
        switchMap so a stale response can't overwrite a newer one." ⭐
```

### The rule ⭐

```
Every project answer needs ONE hard problem you can go three questions deep on.

They will ask:  "why?" → "what else did you consider?" → "what broke?"
If you only have the feature list, you run out at question one 💥
```

Real-world idea: **a cooking show.** Nobody wants the ingredients read out. They want the moment the sauce split and what you did about it.

#### Easy memory

```
CAPS ⭐  Context (1 line) → Action (modules) → PROBLEM + trade-off ⭐⭐ → Scale (numbers)

Have ONE hard problem per project, deep enough for THREE follow-ups ⭐
Always finish with the OUTCOME.
```

---

## 1) RoboGebra — AI-Driven Math Learning Platform

**Project pitch:** RoboGebra is an AI-driven math learning platform that helps students learn step-by-step, with personalized dashboards and bilingual (Tamil/English) explanations. I work as a frontend-focused full-stack engineer building the Angular + Ionic client and the supporting Java/**MongoDB** REST services. I own features like the personalized learning dashboard, the Study Reminder system, the Study List module, the AI math explanation engine integration, the interactive Quiz module, and real-time math computation with live graph visualization. My work directly improved learner retention through scheduled reminders and made complex problems easier to grasp via instant, interactive feedback.

### Q: What was your exact role and the team size?
I was the primary frontend engineer (Angular/Ionic) and also wrote backend REST endpoints in **Java 17 / Spring Boot 3.2 with MongoDB** for my modules. The team was around 6–7: a couple of frontend devs, two backend devs, a designer, a QA, and a lead/PM. I owned the dashboard, reminders, study list, quiz, and the real-time computation features end to end — UI, the API contract, and the persistence layer for those.

### Q: Describe the overall architecture.
It's a hybrid mobile/web app: Angular as the framework with Ionic for the mobile/cross-platform UI layer, talking over REST to **Java 17 / Spring Boot 3.2 services backed by MongoDB** (Spring Data MongoDB, not JPA). The AI explanation/quiz generation lives behind a service that the Java layer proxies, so the client never calls the model directly — that keeps keys server-side and lets us cache/normalize responses. On the frontend I used a service-per-domain pattern (DashboardService, ReminderService, StudyListService, QuizService), RxJS for async streams, and a shared state layer for cross-component data like the current learner profile.

### 🔵 The architecture, with the real numbers ⭐

```
[ Angular 16.2 web ]  [ Ionic 8.7 / Angular 18.2 mobile ]  [ Admin portal ]  [ React CRM ]
         │                        │                              │                │
         └────────────────────────┴──────────┬───────────────────┴────────────────┘
                                             ▼  REST + JWT
                        [ Spring Boot 3.2 · Java 17 ]
                          110 @RestController · 207 @Service
                          85 @Repository · 104 @Document ⭐
                                             │
     ┌───────────────┬───────────────────────┼──────────────────┬─────────────────┐
     ▼               ▼                       ▼                  ▼                 ▼
[ MongoDB ]   [ AWS S3 ] ⭐          [ AWS Cognito ] ⭐   [ Firebase FCM ]   [ Razorpay ]
 documents     assets +               auth, 3 pools       push               payments
 27 pipelines  PRESIGNED URLs         + JWKS validation   (bounded pool ⭐)

Local dev: LocalStack (S3 + Cognito in Docker) ⭐   CI/CD: Jenkins + Docker ⭐
Scheduled work: Quartz
```

> 🗣️ **Say it like this:** *"Four clients — the Angular web app, the Ionic mobile app, an admin portal and a React CRM — all talk to one Spring Boot API over REST with JWT. Persistence is MongoDB through Spring Data. The AI engine sits behind our own service, so the client never holds a key and we can cache and normalise the responses. S3 holds assets and serves them via presigned URLs; Cognito is the identity provider; Firebase does push; Razorpay does payments."*

⚠️ And be precise if they probe the architecture style:

> *"It's a **modular monolith**, not microservices — one Spring Boot deployment organised into domain packages that each own their services and repositories. The boundaries are drawn, but we haven't paid the operational cost of splitting it."*

→ [08 — Microservices](./08-microservices-basics.md) · [39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)

### Q: The real-time computation and graph visualization sounds expensive. How did you keep it performant?
The problem: when a student drags a slider or edits a value, we recompute the expression and redraw the graph plus fetch AI insights. Naively that fires a request and a re-render on every keystroke/drag tick, which janks the UI and hammers the AI endpoint. I split it into two streams — local math computation (instant, runs in the browser) and the AI insight call (debounced).

```ts
// Local recompute is instant; AI insight is debounced + cancellable
this.valueChange$.pipe(
  // redraw the graph immediately from the local math engine
  tap(v => this.redrawGraph(this.computeLocally(v))),
  debounceTime(400),
  distinctUntilChanged(),
  switchMap(v => this.quizApi.getAiInsight(v).pipe(
    catchError(() => of(null)) // never let one failed insight break the canvas
  ))
).subscribe(insight => this.insight = insight);
```

`switchMap` cancels the in-flight AI request when a newer value arrives, `debounceTime` collapses bursts, and `distinctUntilChanged` skips no-op changes. I also memoized the local compute and used `trackBy` / `OnPush` so the graph component only re-renders when its inputs actually change. Result: the graph feels instant and AI calls dropped by a large margin.

### Q: How did you implement the bilingual (Tamil/English) rendering? Any gotchas?
Explanations come back as structured content (steps + formulas), so I keep the language as a key rather than baking translated strings into components. The AI engine returns both language variants (or we request a specific one), and the UI switches via a language signal/observable. The gotchas were font rendering and math: Tamil glyphs needed a font that supports the script, and mixing Tamil prose with LaTeX-style formulas meant I had to render the formula segments separately (MathJax/KaTeX) from the localized prose so the RTL/script handling didn't mangle the math. I segmented each step into "text" and "formula" tokens and rendered them in their own spans.

### Q: How does the Study Reminder system work, and what REST design did you use?
It's flexible scheduling with notifications. I exposed a standard REST resource for reminders with full CRUD:

```java
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {
    @GetMapping            // list a user's reminders
    @PostMapping           // create
    @PutMapping("/{id}")   // update schedule/status
    @DeleteMapping("/{id}")// remove
}
```

A reminder stores a schedule (one-off or recurring), the target study item, and a status. A scheduler checks due reminders and pushes notifications. On the client I optimistically update the list on POST/PUT/DELETE and reconcile if the server rejects. Adding consistent reminders measurably improved retention because learners came back on cadence.

### Q: How did you handle the Study List real-time sync?
The Study List lets learners create/organize/track goals and needs to reflect changes immediately across views (and devices). I kept a single source of truth in a shared service exposing a `BehaviorSubject` of the list; any mutation goes through that service so every subscribed component updates instantly. For cross-device sync, mutations hit the API and the service refreshes/merges so an edit on mobile shows on web. Reordering and completion toggles are optimistic with rollback on error.

### Q: How do you manage state across these modules?
For most feature state I use RxJS services with `BehaviorSubject`s (a lightweight store pattern) rather than pulling in NgRx — the app's state is feature-scoped, so a store-per-domain keeps it simple and testable. Shared/global concerns (current learner, language, auth) live in singleton root-provided services. Components subscribe via the `async` pipe so subscriptions are cleaned up automatically.

### 🔵 The real state-management answer ⭐

**File:** `robogebra-mobile/src/app/core/services/user-summary.service.ts` — **122 `BehaviorSubject` usages**, **zero NgRx**.

```ts
@Injectable({ providedIn: 'root' })                        // ⭐ app-wide singleton
export class UserSummaryService {
    private userSummarySubject$ = new BehaviorSubject<UserSummaryGroupDTO>(null);

    getUserSummarySubject(): Observable<UserSummaryGroupDTO> {
        return this.userSummarySubject$.asObservable();     // ⭐ READ-ONLY outside
    }
    getUserSummary(): UserSummaryGroupDTO {
        return this.userSummarySubject$.getValue();         // ⭐ SYNCHRONOUS read
    }
}
```

> 🗣️ *"State is `BehaviorSubject`-backed services rather than NgRx — that was a deliberate choice. `BehaviorSubject` gave us the two things we actually needed: a current value on subscribe, so a lazily-created page or modal still gets the logged-in user, and a synchronous `getValue()` for callers that can't subscribe — our institute-code HTTP interceptor, for instance. NgRx would have meant actions, reducers, effects and selectors for state that a handful of components read. I'd reach for it when state is shared across many unrelated features and I need time-travel debugging."*

⭐ Note the subject is **private** and only `asObservable()` is exposed — so no component can call `.next()` and mutate global state from anywhere. Say that; it's an encapsulation point most people miss.

→ [21 — NgRx](./21-ngrx.md)

### Q: How do you handle API errors and loading states?
Every API call goes through typed service methods that return Observables; components render explicit loading/empty/error states rather than blank screens. I use a `catchError` to map server errors into a user-facing message and, where safe, fall back (e.g., the AI insight failing should not break the graph — see the snippet above). An HTTP interceptor centralizes auth headers, 401 handling, and a global error toast so I don't repeat that logic per call.

### Q: How did you test this?
Unit tests for the services (Jasmine/Karma) with `HttpTestingController` to assert the right requests and to simulate error responses, and component tests for the dashboard/quiz logic. For the RxJS-heavy real-time code I tested the debounce/switchMap behavior with marble-style tests so I could prove old AI requests get cancelled. QA covered end-to-end flows on device.

### Q: What would you do differently?
I'd introduce a proper state library (NgRx Component Store or Signals-based store) earlier — once several modules needed to share the learner/profile state, the hand-rolled subjects got repetitive. I'd also add response caching for AI explanations at the service layer from day one, since identical problems get explained repeatedly and that's wasted model cost and latency.

### 🔵 Your three strongest "hard problem" stories ⭐

These are verified in your code. **Pick one and be ready to go three questions deep.**

#### 1. The N+1 query — and the fix ⭐⭐

**File:** `domain/exerciseitem/repository/ExerciseItemRepository.java` (27 `@Aggregation` pipelines)

```java
@Aggregation(pipeline = {
        "{ '$match': { 'exercise.$id': { '$in': ?0 } } }",     // ⭐ ?0 = the method parameter
        "{ '$sort':  { 'displayOrder': 1 } }"
})
List<ExerciseItemEntity> findAllByExerciseIds(List<ObjectId> exerciseIds);
```

> *"Loading a chapter's exercises and then fetching each exercise's items one at a time is one query plus N — and `@DBRef` makes it very easy to fall into, because Spring Data resolves each reference with its own query. A chapter with fifty exercises meant fifty-one round trips. I replaced it with a single aggregation pipeline that matches on all the exercise ids at once with `$in` and sorts by display order. It's the same fix as a JPA `JOIN FETCH`, expressed as a pipeline. What I took from it is that a document database doesn't remove the N+1 problem — it just changes what it looks like."*

#### 2. The push-notification thread pool ⭐⭐

**File:** `config/AsyncConfig.java`

```java
@Bean(NOTIFICATION_TASK_EXECUTOR)
public ThreadPoolTaskExecutor notificationTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(500);                     // BOUNDED ⭐
    executor.setThreadNamePrefix("push-");              // readable thread dumps ⭐
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());  // backpressure ⭐
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);
    return executor;
}
```

> *"We had one shared `@Async` pool, and push fan-out was starving it — a single announcement touches every teacher, and each device is a blocking FCM round trip, so analytics events queued behind it. I gave push its own executor with a bounded 500-item queue and a caller-runs rejection policy, so a backlog slows the submitter down instead of growing an unbounded queue until the heap gives out. A late push beats an OutOfMemoryError. The subtle part was that declaring any executor bean makes Spring Boot's auto-configured `applicationTaskExecutor` back off — it's `@ConditionalOnMissingBean(Executor.class)` — so every unqualified `@Async` silently moved onto the notification pool. I had to rebuild Boot's default from its own builder."*

⭐ That last sentence is genuinely senior. It demonstrates the auto-configuration answer *and* a real production trap in one story.

#### 3. The HTTP interceptor ⭐

**File:** `core/interceptors/header-authorization.interceptor.ts`

> *"One interceptor handles the token, the 401 refresh cycle and cancellation. Three details in it are worth calling out. `HttpRequest` is immutable, so you must `clone()` — mutating it silently does nothing. We skip the `/auth` endpoints, or login would need a token to get a token. And we deliberately don't set `Content-Type` when the body is `FormData`, because the browser has to generate the multipart boundary itself — setting it manually breaks every file upload. There's also a `cancelRequests$` subject, so logout aborts every in-flight request at once; otherwise a slow response from the previous session can land after the user has signed out."*

⭐ **The FormData detail is the one that makes interviewers sit up** — it's a real bug most people have shipped without ever diagnosing.

→ [39 — RoboGebra Code Examples](./39-robogebra-code-examples.md) for all three in full.

### Q: How did you collaborate?
Agile with two-week sprints, Jira for stories/bugs, Git with feature branches and PR reviews. I'd pick up a story, agree the API contract with the backend dev, build behind a feature branch, get a review, and demo in sprint review.

---

## 2) Subsea — Marine Equipment & Operations Management

**Project pitch:** Subsea is a marine equipment and operations management platform used to track and schedule subsea assets and operations. I worked on the Angular frontend with a .NET API backend, containerized with Docker and deployed on Azure. My main contributions were building the Schedule-Manager module with its .NET APIs, implementing complex grids with Kendo UI, adding dynamic category/subcategory management, and fixing production bugs. I also refactored parts of the architecture to be more modular and reusable.

### Q: What was your role and team size?
Full-stack with an Angular emphasis on a team of roughly 5–6. I owned the Schedule-Manager module front to back — Angular UI plus the .NET endpoints — and I was one of the people who picked up production defects.

### Q: Describe the architecture.
Angular SPA, .NET Web API services, containerized with Docker and running on Azure. The frontend talked to REST endpoints; Docker gave us consistent local/CI/prod environments, and Azure hosted the containers. The Schedule-Manager was a feature module with its own routing, services, and Kendo-based grid components.

### Q: You used Kendo UI Grid with complex/large data. What was the challenge and how did you solve it?
The grids showed large schedule/equipment datasets with sorting, filtering, grouping, and inline editing, and the default client-side binding got sluggish as rows grew. I moved the grid to server-side data operations — paging, sorting, and filtering done in the .NET API rather than loading everything into the browser:

```ts
// Push paging/sort/filter to the server via Kendo DataSourceRequest
loadGrid(state: State) {
  const query = toDataSourceRequestString(state); // Kendo helper -> querystring
  return this.http.get<GridDataResult>(`/api/schedule?${query}`);
}
```

```csharp
// .NET side: let Kendo's DataSourceRequest do paging/sort/filter in the DB query
[HttpGet]
public DataSourceResult Get([DataSourceRequest] DataSourceRequest request)
    => _db.Schedules.ToDataSourceResult(request); // translates to SQL paging
```

That meant only one page of rows crossed the wire. Combined with virtual scrolling on the grid and only requesting the columns we display, the grid stayed responsive even with large datasets.

### Q: How did the dynamic category/subcategory management work?
Categories and their subcategories were data-driven, not hardcoded — admins could add/edit them and the UI had to reflect that hierarchy live. I modeled it as a parent/child relationship from the API and rendered cascading selects where choosing a category filters its subcategories. On change I reload dependent dropdowns reactively rather than re-fetching the whole tree.

### Q: How did Docker fit into your workflow?
Docker gave us a reproducible environment — the same image ran on my machine, CI, and Azure, which killed the "works on my machine" class of bugs, especially for the .NET runtime and dependencies. I worked with the Dockerfile/compose setup to run the API and its dependencies locally so I could integrate the frontend against a realistic backend.

### Q: Tell me about a production bug you fixed.
A common pattern was grid/state defects where filters or edits didn't persist correctly after a reload. My approach was reproduce-first: get exact repro steps from the report, reproduce locally against the same data shape, narrow it to frontend vs API by inspecting the network calls, fix at the right layer, and add a guard/test so it didn't regress. Doing it methodically rather than guessing is what kept fixes from bouncing back.

### Q: How did you handle state and API calls here?
Feature-scoped Angular services wrapping the .NET endpoints, Observables consumed with the async pipe, and the grid driven by server-side state objects. Error handling went through a shared interceptor plus per-call `catchError` mapping to user-facing messages.

### Q: What did you refactor for modularity/reusability?
I pulled repeated grid configuration and category/subcategory selectors into shared components and helpers so other modules could reuse them instead of copy-pasting Kendo setup. I also tightened the feature module boundaries so Schedule-Manager didn't leak dependencies into unrelated areas.

### Q: How did you test?
Unit tests for services and component logic, and manual/QA verification of the grid operations and schedule flows. For bug fixes I added regression coverage for the specific failing case.

### Q: What would you do differently?
I'd standardize the grid data-binding pattern across the app from the start — we had a mix of client- and server-bound grids, and unifying on server-side operations earlier would have prevented the performance issues. I'd also add more automated integration tests around the .NET endpoints.

### Q: How did you collaborate?
Agile sprints, Jira, Git PRs, and close coordination with the backend on the .NET API contracts since I was building both sides of Schedule-Manager.

---

## 3) Backoffice UI — Angular Version Migration

**Project pitch:** This was a focused migration project to bring four Backoffice repositories up to Angular 16. I upgraded a Ticketing application from Angular 2 to 16 and three SSG web-plugin apps from Angular 9 and 4 up to 16. The work was about navigating years of breaking changes safely and verifying nothing regressed through thorough testing. The payoff was modern, supported, faster-building apps on a single Angular version.

### Q: What was your role and team size?
I led/owned the migrations across the four repos. It was a small effort — me driving the upgrades with the original feature teams and QA validating their apps. I was the person who understood the cross-version breaking changes and applied them consistently.

### Q: What's your strategy for a multi-version jump like Angular 2/9 to 16?
I never jump straight across — Angular only officially supports one-major-at-a-time upgrades. I went incrementally (e.g., 9 → 10 → 11 … → 16), using `ng update` per step which runs the migration schematics, building and running tests after each hop, and committing each version as its own checkpoint so I could bisect if something broke. The `update.angular.io` guide was my checklist for each step.

### Q: What were the actual breaking changes you had to handle?
A lot. Key ones:
- **Ivy** became the default renderer (v9) — some libraries that relied on View Engine or non-Ivy-compatible metadata needed updates or replacement; I had to ensure all dependencies were Ivy-compatible.
- **RxJS 6 → 7** — typing changes and the move to fully pipeable operators; I had to remove any remaining deprecated operator imports and fix `toPromise()` usage (deprecated, later replaced with `firstValueFrom`/`lastValueFrom`).
- **Deprecated/removed APIs** — e.g. the `entryComponents` array became unnecessary with Ivy, `ViewChild`'s `static` flag semantics changed (v8/9), and various TypeScript strictness bumps surfaced type errors.
- **TypeScript and tooling version bumps** at each step, plus changes to the build (`angular.json`) schema and eventual move toward the newer build pipeline.
- **Strict mode and stricter templates** flagged previously-silent issues.

### Q: How did RxJS 6→7 specifically bite you?
Mostly typing and deprecated patterns. `toPromise()` was deprecated, so I migrated to `firstValueFrom`/`lastValueFrom`. Some operator overloads got stricter so a few `map`/`reduce` chains that were loosely typed needed explicit types. The fix was mechanical but had to be done carefully across the codebases.

```ts
// before (RxJS 6, deprecated)
const data = await this.http.get('/api/tickets').toPromise();

// after (RxJS 7)
import { firstValueFrom } from 'rxjs';
const data = await firstValueFrom(this.http.get('/api/tickets'));
```

### Q: How did you make the transition "seamless"? What was the testing strategy?
Build + test after every single version step, not just at the end — that localizes any breakage to one hop. I ran the existing unit tests, did smoke testing of the critical flows in each app, and leaned on QA for full regression on the Ticketing and plugin apps. Where tests were thin, I added coverage around the riskiest areas before upgrading so I'd notice regressions. Keeping each version in its own commit meant I could always roll back one step.

### Q: The three plugin apps were SSG web plugins — anything special?
They were embedded plugins, so I had to make sure the upgraded Angular bundles still loaded correctly inside the host SSG environment and didn't clash with the host's runtime. Verifying the integration boundary (how the plugin is bootstrapped/mounted) was as important as the in-app behavior.

### Q: How did you handle third-party libraries that weren't compatible?
I checked each dependency's Angular compatibility before each hop. If a library lagged, I either upgraded it to a compatible version, found a maintained alternative, or temporarily pinned and isolated it until a compatible release existed. Ivy compatibility was the big filter at v9.

### Q: What would you do differently?
I'd add or strengthen automated tests before starting the migration, since strong test coverage is what makes a big upgrade safe and fast. I'd also script the per-step build/test loop in CI so each version bump is gated automatically rather than verified by hand.

### Q: How did you manage this in Git?
A branch per repo, one commit per major version step with a clear message, and PRs reviewed by the owning team before merge. The granular commits doubled as documentation of exactly what changed at each version.

---

## 4) EasyVisa — Visa Management for Attorneys

**Project pitch:** EasyVisa is a visa case management platform for immigration attorneys — managing applications, documents, and the relationships between cases, applicants, and petitioners. I worked on the Angular frontend against a Grails/Groovy backend with PostgreSQL and Neo4j for graph relationships. I built the document portal module with accordion upload panels, created dynamic forms with Angular Formly including nested reactive forms, customized an image cropper, drove an Angular version upgrade, and built reusable components with unit tests and documentation. My work made complex, document-heavy visa workflows manageable for attorneys.

### Q: What was your role and team size?
Frontend engineer on a team of around 6–8 (frontend, Grails/Groovy backend devs, QA, lead). I owned the document portal and the dynamic forms work, and I contributed shared/reusable components used across the app.

### Q: Why Neo4j alongside Postgres — what's the architecture?
Postgres held the core relational/transactional data, while Neo4j modeled the relationships — applicants, petitioners, cases, family/employer links — because visa cases are fundamentally a graph of related entities. Querying "who is connected to this case and how" is natural in a graph and painful with deep relational joins. The Grails/Groovy backend exposed REST endpoints to the Angular frontend; I consumed those, and where the data was relationship-shaped (e.g., showing related parties on a case) it was backed by Neo4j traversals on the server.

### Q: The dynamic nested Formly forms sound hard. Walk me through a challenge.
Visa forms are huge, conditional, and repeatable — a section can have N applicants, each with nested address/employment histories, and fields appear/disappear based on earlier answers. Hardcoding that is unmaintainable, so I used Angular Formly to render forms from a JSON field config, with `fieldGroup` for nesting and `fieldArray` for repeatable sections:

```ts
const fields: FormlyFieldConfig[] = [{
  key: 'applicants',
  type: 'repeat',                 // custom repeating section
  fieldArray: {
    fieldGroup: [
      { key: 'firstName', type: 'input', props: { label: 'First name', required: true } },
      { key: 'hasEmployer', type: 'checkbox', props: { label: 'Currently employed?' } },
      {
        key: 'employer', type: 'input', props: { label: 'Employer' },
        expressions: { hide: 'model.hasEmployer === false' } // conditional visibility
      }
    ]
  }
}];
```

The hard parts were conditional logic (Formly `expressions` for show/hide and dynamic required), keeping nested reactive form state valid as sections were added/removed, and validation bubbling up from nested groups to the submit button. I solved validation by relying on Formly's nested `FormGroup` aggregation and writing custom validators for the visa-specific rules, then surfacing errors at the right nesting level.

### Q: Tell me about the document portal and large uploads.
Attorneys upload many documents per case, organized in accordion panels by document type. Large files were the issue — uploading naively blocks the UI and risks timeouts. I used `HttpClient` with `reportProgress` to stream progress per file, allowed concurrent-but-throttled uploads, and gave each panel its own progress/error state so one failed upload didn't sink the others:

```ts
this.http.post('/api/documents', formData, {
  reportProgress: true, observe: 'events'
}).subscribe(event => {
  if (event.type === HttpEventType.UploadProgress) {
    this.progress = Math.round(100 * event.loaded / (event.total ?? 1));
  } else if (event.type === HttpEventType.Response) {
    this.markUploaded();
  }
});
```

For very large files the right move is chunked/resumable upload, which I flagged as the next improvement.

### Q: You customized an image cropper — why and how?
Certain documents/photos (e.g., applicant photos) had to meet specific dimension/aspect requirements, so I wrapped and customized an Angular image-cropper component to enforce the aspect ratio, output size, and format we needed, and wired its output into the upload flow. The customization was mostly constraining the crop box and post-processing the output blob before upload.

### Q: How did you handle state, APIs, and errors?
Feature services wrapping the Grails REST endpoints, Observables with the async pipe, and reactive forms as the source of truth for form state. Errors were handled per-call with `catchError` mapping to user messages plus a shared interceptor for auth and global failures. For the forms, I kept the Formly model as the single state object so save/restore (drafts) was straightforward.

### Q: You built reusable components — give an example and the benefit.
I extracted things like the upload-panel, the Formly field types, and common form controls into shared, configurable components. The benefit was consistency and speed: a new form section or document type became a config change rather than new bespoke code, and bugs got fixed once in the shared component.

### Q: How did you test, and what about the version upgrade?
I wrote unit tests (Jasmine/Karma) for the components and form logic and documented the reusable pieces so other devs could adopt them. For the Angular upgrade I followed the incremental `ng update` approach, building and testing at each step and fixing breaking changes/deprecations as they surfaced.

### Q: What would you do differently?
I'd move large uploads to chunked/resumable from the start, and I'd consider a schema-driven approach where the form JSON configs come from the backend so non-developers could adjust forms without a release. I'd also add more integration tests around the conditional form logic, since that's where regressions hide.

### Q: How did you collaborate?
Agile with Jira and Git PR reviews, and tight coordination with the Grails/Groovy team on REST contracts and on what data came from Postgres vs Neo4j.

---

## General project & experience questions

### Q: What was the most challenging project and why?
RoboGebra's real-time computation-and-visualization feature, because it combined frontend performance (instant graph updates), async orchestration (debounced, cancellable AI calls), and bilingual rendering of mixed prose and formulas. Getting it to feel instant while not flooding the AI backend forced careful RxJS design — separating the instant local compute from the debounced/`switchMap`-cancelled remote call.

### Q: How do you approach building a new feature end to end?
Clarify the requirement and acceptance criteria, agree the API contract with the backend early, sketch the component/service breakdown, build behind a feature branch with the happy path plus loading/empty/error states, write unit tests as I go, self-review the diff, open a PR, address review comments, and demo in sprint review. I try to ship a thin vertical slice first, then iterate.

### Q: How do you debug a production issue?
Reproduce first — get exact steps and the affected data shape, reproduce locally. Then isolate the layer using the network tab and logs (frontend vs API vs data), form a hypothesis, fix at the correct layer, and add a regression test so it can't silently come back. I avoid guess-and-deploy; methodical reproduction is what stops fixes from bouncing.

### Q: What do you look for in code reviews?
Correctness first, then readability and maintainability: clear naming, proper unsubscription/async-pipe usage, error/loading handling, no obvious performance traps (unnecessary re-renders, client-side data loads that should be server-side), and adequate tests. I give specific, kind, actionable comments and distinguish blocking issues from nits.

### Q: How do you ensure code quality?
Strong typing, small focused components/services, the async pipe and OnPush to avoid subscription leaks and extra renders, unit tests for logic, linting/formatting in CI, and reusing shared components instead of duplicating. Consistent patterns across the codebase matter as much as any single clever fix.

### Q: Describe your Agile/Scrum process.
Two-week sprints with planning, daily standups, sprint review/demo, and retro. Work tracked in Jira as stories/bugs with estimates, code in Git with feature branches and PR reviews. Standup is where I flag blockers early.

### Q: How do you handle changing requirements mid-sprint?
I clarify the change with the PM/stakeholder, assess impact on scope and the sprint commitment, and raise it transparently rather than silently absorbing it. Small changes I fold in; larger ones get re-estimated and either swapped for something or moved to the next sprint. Building things data/config-driven (like the Formly forms) also made many "changes" cheap.

### Q: What was your biggest technical decision and the trade-off?
On EasyVisa, choosing Angular Formly with JSON-driven config for the visa forms instead of hand-coding each form. The trade-off was a steeper initial learning curve and some complexity in custom field types versus huge long-term savings — new forms and conditional logic became configuration, not new components. For the scale of forms there, it clearly paid off.

### Q: How do you handle merge conflicts and what's your Git workflow?
Feature branches off main, small frequent commits, regular rebases/merges from main to keep the branch current so conflicts stay small. When conflicts happen I resolve them by understanding both sides' intent — not blindly picking one — re-run the build and tests, and if it's non-trivial I check with the other author. PRs are reviewed before merge.

### Q: How do you estimate tasks?
I break a story into concrete sub-tasks, estimate each based on complexity and unknowns (relative sizing / story points), pad for testing and review, and flag risky areas as needing a spike. I'd rather surface uncertainty up front than commit to a number I can't hit. Past similar work calibrates the estimate.

### Q: What's your experience with CI/CD and deployment?
On Subsea we used Docker for reproducible builds and deployed containers to Azure, so the same image ran locally, in CI, and in prod — which eliminated environment drift. Generally I work with pipelines that lint, build, and run tests on every PR so problems are caught before merge, and I treat a green pipeline as a gate to deploy.

### Q: Tell me about a disagreement on a technical approach.
On grid performance in Subsea there was a question of client-side vs server-side data operations. I argued for server-side paging/sorting/filtering because client-side wouldn't scale with the dataset. Rather than just assert it, I demonstrated the lag with a realistic data volume, we agreed on the server-side approach, and the grid stayed responsive. I focus on evidence and the user impact, and I commit to the decision once it's made even if it isn't the one I proposed.

---

## Quick Revision Sheet — the four projects, one page

```
CAPS ⭐  Context → Action (modules) → PROBLEM + trade-off ⭐⭐ → Scale (numbers)
Every project needs ONE hard problem, deep enough for THREE follow-ups ⭐

────────────────────────────────────────────────────────────────────────────
1. ROBOGEBRA ⭐ FLAGSHIP          Oct 2024 – now
   Angular 16 web · Ionic 8.7 / Angular 18.2 mobile · Java 17 / Spring Boot 3.2
   MONGODB ⭐ (NOT MySQL!) · AWS S3 + Cognito · Firebase · Razorpay · Quartz
   BUILT: dashboard · Study List · quiz · AI Tamil/English explanations
          + THE IONIC MOBILE APP (iOS/Android) ⭐ ← lead with this
   HARD PROBLEM (pick one):
      • N+1 → one $match/$in aggregation instead of 51 queries ⭐⭐
      • push thread pool → bounded queue + CallerRunsPolicy ⭐⭐
        (+ the @ConditionalOnMissingBean auto-config trap ⭐)
      • interceptor → clone() · FormData Content-Type ⭐ · cancelRequests$
   STATE: BehaviorSubject services, NOT NgRx — a deliberate choice ⭐
   STYLE: MODULAR MONOLITH, not microservices ⭐

────────────────────────────────────────────────────────────────────────────
2. SUBSEA                          Apr 2024 – Sep 2024
   Angular · .NET · Docker · Azure · Kendo UI Grid
   BUILT: Schedule-Manager module + the .NET APIs · category/sub-category mgmt
   HARD PROBLEM: large Kendo grid performance → virtualisation + server paging
   STORY: learning .NET quickly, coming from Java ⭐

────────────────────────────────────────────────────────────────────────────
3. BACKOFFICE MIGRATION ⭐          Feb 2024 – Mar 2024
   FOUR repos → Angular 16 (Ticketing v2→16, three SSG plugins v9/v4→16)
   HARD PROBLEM: RxJS 6→7 breaking changes · incompatible third-party libs
   STORY: this is your LEADERSHIP / OWNERSHIP answer ⭐
   LINE: "the hard part is never Angular — it's the third-party libraries" ⭐

────────────────────────────────────────────────────────────────────────────
4. EASYVISA                        Nov 2021 – Jan 2024
   Angular · Grails/Groovy · Postgres · Neo4j · Angular FORMLY ⭐
   BUILT: document portal (accordion uploads) · nested reactive forms
          · custom Formly field types · customised image cropper · unit tests
   HARD PROBLEM: dynamic nested Formly forms + large file uploads
   STORY: "above and beyond" — reusable components the whole team adopted ⭐
   LINE: "a new form field became a CONFIG change, not a release" ⭐
   ⚠️ NgRx and Formly are EASYVISA, not RoboGebra ⭐

────────────────────────────────────────────────────────────────────────────
THE THREE THINGS TO NEVER GET WRONG ⭐
  1. RoboGebra = MONGODB (not MySQL) ⭐
  2. RoboGebra = modular MONOLITH (no Eureka/Feign/circuit breaker) ⭐
  3. NgRx + Formly = EasyVisa · signals + standalone = NOT adopted yet ⭐
```

---

**Related files:** [00 — Self-Introduction](./00-self-introduction.md) · [11 — HR & Behavioural](./11-hr-behavioral.md) · [39 — RoboGebra Code Examples](./39-robogebra-code-examples.md) · [18 — RoboGebra Technical Versions](./18-robogebra-technical-versions.md)
