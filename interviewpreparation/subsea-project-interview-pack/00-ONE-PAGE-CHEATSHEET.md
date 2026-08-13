# Subsea — One-Page Cheat Sheet · Priyanka R

**Stack:** Angular SPA · **.NET (C#) Web API** · **Docker** · **Azure** · **Kendo UI Grid** · SQL
**My module:** **Schedule-Manager** — owned front-to-back (Angular UI + .NET endpoints). Team ~5–6. Apr–Sep 2024.
**Signature win:** moved Kendo Grid to **server-side** paging/sort/filter (`DataSourceRequest`) → large datasets stayed fast. Won the client-vs-server debate by **demoing the lag**.

**Request flow:** Angular component → feature service (`HttpClient`) → **.NET Web API controller** (`[HttpGet]`) → `ToDataSourceResult(request)` → SQL paging → one page of rows back → Kendo Grid renders. Errors → HTTP interceptor + per-call `catchError` → user-facing toast.

---

### 20 facts to know cold

1. **Project:** marine equipment & operations platform — track/schedule subsea assets & operations.
2. **My ownership:** Schedule-Manager module, both sides (Angular + .NET), plus production bug fixes.
3. **Signature:** client-side Kendo Grid got sluggish as rows grew → I pushed paging/sort/filter to the .NET API. Only **one page crosses the wire**.
4. **The Kendo pattern:** TS `toDataSourceRequestString(state)` → querystring → C# `[DataSourceRequest] DataSourceRequest` → `.ToDataSourceResult(request)` → `DataSourceResult { Data, Total }`.
5. **Why server-side wins:** client-side loads *all* rows into the browser (memory + slow sort/filter); server-side does it in SQL and returns just the visible page.
6. **Virtual scrolling** + requesting only displayed columns = extra responsiveness on top of server-side ops.
7. **Cascading dropdowns:** category → subcategory, data-driven parent/child. On category change I reactively reload dependent subcategories, not the whole tree.
8. **Reactive forms** (`FormBuilder`/`FormGroup`) for the cascade — I subscribe to the category control's `valueChanges` to drive the subcategory list.
9. **Restrict duplicate API calls:** `debounceTime` + `distinctUntilChanged` + **`switchMap`** (switchMap **cancels** the previous in-flight request — latest wins).
10. **Memory leaks:** unclosed subscriptions outlive the component. Fix = **`async` pipe** first, else **`takeUntil(destroy$)`** completed in `ngOnDestroy`.
11. **Lifecycle hooks (order):** `ngOnChanges` → `ngOnInit` → `ngDoCheck` → `ngAfterViewInit` → `ngOnDestroy`. I init data in `ngOnInit`, clean up in `ngOnDestroy`.
12. **Component comms:** `@Input` (parent→child), `@Output EventEmitter` (child→parent), **shared service + `BehaviorSubject`** (siblings/cross-tree).
13. **Observable vs Subject vs BehaviorSubject:** Observable = cold, per-subscriber; Subject = multicast, no initial value; **BehaviorSubject** = multicast **with a current value** (perfect for state — my dropdown selections).
14. **Route guards:** `CanActivate` gates protected routes; **lazy loading** via `loadChildren` so Schedule-Manager ships as its own chunk.
15. **Global exception handling:** Angular `ErrorHandler` + HTTP interceptor on the frontend; **middleware / exception filter** on the .NET side → uniform JSON error, not a 500 stack trace.
16. **Interface vs abstract class (C#):** interface = pure contract, multiple allowed, no implementation; **abstract class** = shared base with some implemented + some abstract members, single inheritance.
17. **`try/catch/finally`:** `try` runs risky code, `catch` handles the exception, **`finally` always runs** (cleanup — close/dispose) whether or not it threw.
18. **DI in .NET:** register services in `Startup`/`Program` (`AddScoped`/`AddSingleton`/`AddTransient`), inject via constructor — same idea as Angular's `providedIn: 'root'`.
19. **Docker:** same image on my machine, CI, and Azure → killed "works on my machine." Dockerfile builds the .NET API image; Azure hosts the containers.
20. **Reusable refactor:** pulled repeated Kendo grid config + category selectors into shared components → other modules reuse instead of copy-paste.

---

### One-liner answers (if put on the spot)

- **"Difference: switchMap vs mergeMap?"** switchMap cancels the previous (search/latest-wins — I used it to stop stacked grid/filter calls); mergeMap runs all in parallel; concatMap queues in order.
- **"How stop double API calls?"** `debounceTime` to wait for the user to stop, `distinctUntilChanged` to skip identical values, `switchMap` to cancel the stale request.
- **"Reactive vs template forms?"** Reactive = defined in TS, typed, testable, great for dynamic/cross-field logic (my cascade); template = simple, HTML-driven, fine for small forms.
- **"BehaviorSubject vs Subject?"** BehaviorSubject remembers the last value and replays it to new subscribers — a late subscriber still gets current state. Subject only sees values emitted *after* it subscribes.
- **"Client vs server-side grid?"** Client = everything in the browser, dies on big data; server = paging/sort/filter in SQL, returns one page. I moved Subsea's grid to server-side and demoed the difference.
- **"Interface vs abstract class?"** Interface = *can-do* contract, multiple; abstract class = *is-a* base with shared code, single. Interface for a capability, abstract class for a common base.

---

### If stuck (say it, don't freeze)

> *"I haven't gone deep on that specific internal — my understanding is X. The closest thing I actually built in Subsea is [real example]. I'd spike it to confirm."*

Pull the question toward the **frontend / Schedule-Manager / Kendo Grid** where you're strong, then bridge back. Honest framing: *"I'm Angular-emphasis full-stack — I owned Schedule-Manager's UI and its .NET endpoints end to end, not a C# runtime specialist."*

**HTTP:** 200 OK · 201 Created · 400 validation · 401 not-logged-in · 403 forbidden · 404 missing · 409 conflict · 500 server error.
**Rules:** tie every answer to Subsea · think out loud · never bluff · speak slower than feels natural.
