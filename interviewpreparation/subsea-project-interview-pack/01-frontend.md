# 1. Front-End (Angular) — Interview Q&A tied to Subsea

**Stack:** Angular SPA talking to .NET Web API endpoints. Feature-module architecture, RxJS-heavy, service-based state, Kendo UI components. My module was **Schedule-Manager** — the schedule/equipment grids, the category/subcategory management, and the forms behind them.

> **How to use this:** every answer ends with an **"In Subsea, I…"** tie-in. That tie-in is the point — it's what turns a textbook answer into project understanding. Lead with the story, drop into code only if pushed.

---

## Q1. Explain Angular lifecycle hooks. Which did you actually use?

**Answer.** Lifecycle hooks are methods Angular calls at defined moments in a component's life. The ones that matter, in call order:

1. **`ngOnChanges(changes)`** — runs when an `@Input` value changes (before `ngOnInit` the first time). Good for reacting to a changed input.
2. **`ngOnInit()`** — runs once after the first `ngOnChanges`; where I do initialization — fetch data, set up subscriptions, build forms. *Not* the constructor, because inputs aren't bound yet in the constructor.
3. **`ngDoCheck()`** — custom change detection; rarely needed.
4. **`ngAfterViewInit()`** — after the component's view (and child views / `@ViewChild`) is initialized; where I touch a child component or DOM element that must exist first.
5. **`ngOnDestroy()`** — right before Angular destroys the component; where I clean up — complete subjects, unsubscribe — to prevent memory leaks.

**In Subsea.** In the Schedule-Manager grid component I used **`ngOnInit`** to build the reactive form for filters and fire the first grid load, and **`ngOnDestroy`** to complete my `destroy$` subject so the grid's data subscriptions didn't leak when the user navigated away. I used **`ngOnChanges`** in a couple of reusable child components — e.g. a grid-config child that had to re-render when the parent passed a new dataset config as an `@Input`.

```typescript
export class ScheduleGridComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.buildFilterForm();          // set up reactive form
    this.loadGrid(this.gridState);   // first server-side load
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();        // cleanup — no leaks
  }
}
```

---

## Q2. Parent–child component communication?

**Answer.** Three mechanisms depending on the relationship:

- **`@Input()`** — parent → child data binding.
- **`@Output() EventEmitter`** — child → parent events.
- **Shared service with a `BehaviorSubject`** — for siblings or components far apart in the tree that don't have a direct parent/child link.

**In Subsea.** When I refactored the grid config into a **reusable grid component**, the parent Schedule-Manager page passed the column/config and the current data page in via `@Input()`, and the grid emitted user actions (page change, sort, filter, row edit) back up via `@Output()` so the parent could re-query the .NET API:

```typescript
// reusable grid child
@Input() columns: GridColumn[];
@Input() data: GridDataResult;          // { data, total } from the server
@Output() stateChange = new EventEmitter<State>();   // paging/sort/filter changed

onDataStateChange(state: State): void {
  this.stateChange.emit(state);         // let the parent re-fetch server-side
}
```

```html
<app-schedule-grid [columns]="cols" [data]="pagedData"
                   (stateChange)="loadGrid($event)">
</app-schedule-grid>
```

For the **category/subcategory selection** shared across parts of the module I used a **service with a `BehaviorSubject`** so any component could read the currently-selected category reactively without prop-drilling it through inputs.

---

## Q3. Observable vs Subject vs BehaviorSubject — what's the difference and where did you use each?

**Answer.** This is really about *cold vs hot* and *whether there's a current value*:

| | Observable | Subject | BehaviorSubject |
|---|---|---|---|
| Multicast? | **No** — cold, runs per subscriber | **Yes** — hot, shared | **Yes** — hot, shared |
| Current value? | n/a | No | **Yes** — holds "the latest" |
| New subscriber gets… | its own execution | only values emitted *after* subscribing | **immediately gets the current value**, then future ones |
| Can I push into it? | No (`.next()` not available) | Yes (`.next()`) | Yes (`.next()`) |

- **Observable** — the default for things like `HttpClient` calls: cold, each subscribe triggers the request.
- **Subject** — a multicast event bus; new subscribers only see what's emitted *after* they subscribe. Good for "an event happened."
- **BehaviorSubject** — a Subject that remembers its last value and replays it to any new subscriber. Perfect for **state** because a component that subscribes late still gets the current value.

**In Subsea.** My grid data came from **Observables** (`HttpClient` calls to the .NET API). For the **category/subcategory state** I used a **`BehaviorSubject`** in a feature service — it held the currently selected category, and any component that subscribed (even one rendered later) immediately got the current selection so the subcategory dropdown could populate correctly:

```typescript
@Injectable({ providedIn: 'root' })
export class CategoryStateService {
  private selectedCategory$ = new BehaviorSubject<Category | null>(null);

  // expose read-only — consumers can't push into it
  readonly category$ = this.selectedCategory$.asObservable();

  selectCategory(cat: Category): void {
    this.selectedCategory$.next(cat);   // update the single source of truth
  }
}
```

**Follow-up: "Why BehaviorSubject and not a plain Subject here?"** Because a component might subscribe *after* the category was already chosen — with a plain Subject it would see nothing until the *next* change. BehaviorSubject replays the current value, so the UI is always consistent.

---

## Q4. How do you prevent memory leaks in Angular? *(the #1 RxJS question)*

**Answer.** Memory leaks in Angular almost always come from **RxJS subscriptions that outlive their component**. If you `subscribe()` and never unsubscribe, the stream keeps a reference to the component so it's never garbage-collected, and the callback keeps firing after the view is gone. I handle it in order of preference:

1. **`async` pipe** in the template — Angular subscribes and unsubscribes for you. Zero leak risk.
2. **`takeUntil(destroy$)`** for imperative subscriptions — a `Subject` I `.next()` + `.complete()` in `ngOnDestroy`.
3. Manual `Subscription.unsubscribe()` only for one-off cases.

**In Subsea.** The `takeUntil(destroy$)` pattern was my standard in Schedule-Manager. Every component that subscribed imperatively (form `valueChanges`, grid state streams) piped through it:

```typescript
this.filterForm.get('category')!.valueChanges
  .pipe(takeUntil(this.destroy$))
  .subscribe(categoryId => this.loadSubcategories(categoryId));
```

And where I could, I bound streams straight to the template with the **`async` pipe** so there was nothing to clean up manually:

```html
<kendo-dropdownlist [data]="subcategories$ | async"></kendo-dropdownlist>
```

**Follow-up: "Anything newer?"** In Angular 16+ there's `takeUntilDestroyed()` from `@angular/core/rxjs-interop`, which removes the boilerplate `destroy$` subject — that's the direction I'd move toward.

---

## Q5. How do you restrict / avoid multiple duplicate API calls? *(they've asked this)*

**Answer.** The classic case is a search box or a filter that fires an API call on every keystroke — you get a flood of requests and race conditions where a slow early response overwrites a fast later one. I fix it with a three-operator combo:

- **`debounceTime(ms)`** — wait until the user stops typing before calling.
- **`distinctUntilChanged()`** — skip the call if the value didn't actually change.
- **`switchMap()`** — **cancel the previous in-flight request** when a new value arrives, so only the latest wins (no race conditions).

```typescript
this.searchControl.valueChanges.pipe(
  debounceTime(300),            // wait for typing to settle
  distinctUntilChanged(),       // ignore no-op changes
  switchMap(term => this.scheduleService.search(term)),  // cancel stale request
  takeUntil(this.destroy$)
).subscribe(results => this.rows = results);
```

**In Subsea.** The grid's filter/search would otherwise hammer the .NET API as the user typed or rapidly changed filters. I used **`debounceTime` + `switchMap`** so rapid filter changes collapsed into a single, latest request — `switchMap` cancelling the stale grid query was important because otherwise an earlier, slower response could land *after* a newer one and show wrong data in the grid.

**Follow-up: "Why switchMap and not mergeMap here?"** Because I *want* to abandon the stale request — with `mergeMap` all requests run in parallel and any of them could resolve last and overwrite the grid. `switchMap` guarantees latest-wins. (`concatMap` would queue them, which is worse for search — you'd wait through stale calls.)

---

## Q6. Reactive forms vs template-driven forms — which did you use and why?

**Answer.**

- **Template-driven** — form logic lives in the HTML with `ngModel`; simple, quick, good for small forms; harder to unit-test and to do dynamic/cross-field logic.
- **Reactive** — form model defined in TypeScript (`FormBuilder`/`FormGroup`/`FormControl`); typed, testable, and it shines for dynamic behavior because I can subscribe to `valueChanges`, add/remove validators at runtime, and drive dependent fields.

I default to **reactive forms** for anything non-trivial.

**In Subsea.** The **cascading category/subcategory** filter was reactive precisely *because* it's dynamic — selecting a category has to reactively reload the subcategory options. I subscribed to the category control's `valueChanges` and reloaded dependents:

```typescript
this.filterForm = this.fb.group({
  category: [null, Validators.required],
  subcategory: [{ value: null, disabled: true }]   // disabled until a category is picked
});

this.filterForm.get('category')!.valueChanges.pipe(
  distinctUntilChanged(),
  switchMap(catId => this.categoryService.getSubcategories(catId)),  // cancel stale
  takeUntil(this.destroy$)
).subscribe(subs => {
  this.subcategories = subs;
  const subCtrl = this.filterForm.get('subcategory')!;
  subCtrl.reset();
  subs.length ? subCtrl.enable() : subCtrl.disable();  // enable only when there are options
});
```

That's the reactive-forms sweet spot — a template-driven version of this cascade would have been much messier.

---

## Q7. How did the cascading category/subcategory management work?

**Answer.** Categories and their subcategories were **data-driven, not hardcoded** — admins could add/edit them, and the UI had to reflect that hierarchy live. I modeled it as a **parent/child relationship** coming from the .NET API, and rendered **cascading selects**: choosing a category filters (and enables) its subcategories.

**In Subsea.** The key design choices:
- On category change I **only reload the dependent subcategories**, not the entire tree — cheaper and snappier.
- The subcategory control starts **disabled** and enables when a category with children is chosen.
- I used **`switchMap`** on the category `valueChanges` so if the user changed category quickly, stale subcategory fetches were cancelled.
- The whole thing was **reactive-form-driven**, so the selected category/subcategory flowed straight into the grid's server-side filter.

Because it was data-driven, adding a new category was a data change in the backend, not a code change — that was the whole point, and it's what made me pull the selector into a **reusable component** so other parts of the app could drop in the same cascade.

---

## Q8. How does Angular change detection work? Did you use OnPush?

**Answer.** Angular's default change detection dirty-checks the component tree whenever something *might* have changed — triggered by zone.js patching async APIs (events, timers, XHR). `ChangeDetectionStrategy.OnPush` narrows that: the component is only re-checked when an `@Input` **reference** changes, an event fires inside it, or an `async`-piped observable emits — a real perf win on large trees.

**In Subsea (honest).** The app ran mostly **default** change detection. Where it mattered for the grid, I leaned on the fact that **server-side data operations** meant each render only dealt with **one page of rows**, not the whole dataset — so change detection had far less to chew on. That's actually a bigger perf lever than OnPush here: I reduced *how much data* is in the view rather than only how often it's checked. Combined with the **`async` pipe** and **Kendo's virtual scrolling**, the grid stayed smooth.

**What I'd improve.** Heavy reusable grid/list components are good **OnPush** candidates — with the `async` pipe and immutable page updates, that would cut redundant checks further. That's a concrete perf story I can tell honestly.

---

## Q9. Route guards and lazy loading — did you use them?

**Answer.** **Route guards** (`CanActivate`) gate whether a route can be entered — typically an auth/role check that returns a boolean or `Observable<boolean>`. **Lazy loading** (`loadChildren`) means a feature module isn't bundled into the initial download — it loads on demand when you navigate to it, which shrinks the initial bundle and speeds up first load.

**In Subsea.** **Schedule-Manager was a lazy-loaded feature module** — it had its own routing module and shipped as its own chunk, so users who never opened it didn't pay for it on initial load:

```typescript
// app-routing.module.ts
{ path: 'schedule',
  loadChildren: () => import('./schedule-manager/schedule-manager.module')
                        .then(m => m.ScheduleManagerModule),
  canActivate: [AuthGuard] }
```

Protected routes went through a `CanActivate` guard that checked the user's auth state before activating the route (and redirected to login otherwise). Structurally, keeping Schedule-Manager as its own lazy module also reinforced the **modularity** work — it kept the module's dependencies from leaking into unrelated areas.

---

## Q10. How do you handle errors and show them to users? (global exception handling)

**Answer.** I never let raw HTTP errors reach the component. On the Angular side I use two layers:
1. An **`HttpInterceptor`** that catches failed responses centrally with `catchError` (and handles cross-cutting concerns like auth headers).
2. A global **`ErrorHandler`** for uncaught client-side errors.

Both funnel into a shared notification service so error UX is consistent, and the **.NET side** returns a **uniform JSON error shape** (not a raw 500 stack trace) via middleware / an exception filter — so the frontend always gets a predictable `{ message }` to show.

**In Subsea.** Error handling for Schedule-Manager went through a **shared HTTP interceptor** plus **per-call `catchError`** that mapped the backend's error into a user-facing toast:

```typescript
this.scheduleService.saveSchedule(payload).pipe(
  catchError((err: HttpErrorResponse) => {
    this.notify.error(err.error?.message ?? 'Could not save the schedule.');
    return EMPTY;                     // swallow so the stream completes cleanly
  }),
  takeUntil(this.destroy$)
).subscribe(() => this.notify.success('Saved.'));
```

So a validation failure on the .NET side came back as clean JSON, the interceptor/`catchError` picked it up, and the user saw a readable message instead of a broken grid. (The .NET side of this is in `03-backend-dotnet.md`.)

---

## Q11. How did you build reusable components?

**Answer.** I extract anything that gets copy-pasted into a **configurable shared component** driven by `@Input()`/`@Output()`, so behavior lives in one place — fix a bug once, everyone benefits.

**In Subsea.** Two big ones:
- **A reusable Kendo grid wrapper** — I pulled the repeated grid configuration (columns, server-side data-binding wiring, paging/sort/filter setup) out of individual pages into one component that takes the config and current data page as inputs and emits state changes. Other modules could drop in a consistent, server-side-bound grid instead of re-implementing Kendo setup each time.
- **The category/subcategory cascade selector** — packaged as a shared component so any screen needing that hierarchy reused it.

The payoff was consistency and speed: a new grid or a new place needing the category cascade became a **configuration change, not new bespoke code**. This was the core of my modularity/reusability refactor, and it also tightened the feature-module boundaries so Schedule-Manager didn't leak into unrelated areas.

---

## Q12. How did you manage state?

**Answer.** **Feature-scoped Angular services holding `BehaviorSubject`s** — the "service with a subject" pattern. A private `BehaviorSubject` holds the current value, exposed read-only via `asObservable()`, updated only through setter methods. Single source of truth, reactive consumers, no NgRx boilerplate — appropriate for the app's scale.

**In Subsea.** The selected category/subcategory and grid state lived in feature services with `BehaviorSubject`s. Components read via the exposed observable (with the `async` pipe or `takeUntil`) and updated through methods — they couldn't push into the subject directly, which kept the state encapsulated. Grid data itself came from the .NET API as Observables and was rendered with server-side state objects.

---

### Rapid-fire recap

| Topic | Subsea pattern |
|-------|----------------|
| Lifecycle hooks | `ngOnInit` (build form + first load), `ngOnDestroy` (complete `destroy$`), `ngOnChanges` (reusable child re-render) |
| Component comms | `@Input`/`@Output` on reusable grid; `BehaviorSubject` service for category state |
| Observable/Subject/BehaviorSubject | grid data = Observable; category **state** = `BehaviorSubject` (replays current value) |
| Memory leaks | `async` pipe first, else `takeUntil(destroy$)` completed in `ngOnDestroy` |
| Restrict duplicate calls | `debounceTime` + `distinctUntilChanged` + **`switchMap`** (cancel stale) |
| Forms | **reactive** — cascade needs dynamic `valueChanges`-driven behavior |
| Cascading dropdowns | category `valueChanges` → `switchMap` reload subcategories, enable/disable |
| Change detection | default; server-side paging keeps view data small; async pipe + virtual scroll |
| Guards / lazy load | `CanActivate` on protected routes; Schedule-Manager `loadChildren` chunk |
| Error handling | HTTP interceptor + `catchError` → toast; uniform JSON from .NET |
| Reusable components | shared Kendo grid wrapper + category cascade selector |
| State | service + `BehaviorSubject`, read-only via `asObservable()` |
