# 🎯 IQVIA — Full Prep Pack (Angular / Ionic Technical Lead)

> 📅 **Interview: coming days.** This file is built **directly from the 10 areas they told you to be comfortable with** — nothing more, nothing less. Every area is a Part below, in their order.
>
> ⚠️ **Read this first.** You were rejected at Altimetrik today (**31 Aug** — the 22 questions are logged in **[37](./37-altimetrik-fullstack-java-angular-31aug.md)**). That round was lost on **Spring depth**, not on effort. IQVIA is a *different shape*: the checklist they gave you is **~75% frontend (Angular / TS / RxJS / Ionic)** — which is your actual strongest ground — plus a **Technical Lead** layer that is about judgement, not syntax. **This one plays to you. Prepare it in the order below and you walk in with an advantage you did not have today.**

---

## 🏥 Know who IQVIA is — 60 seconds that changes the round

IQVIA is a **healthcare data + clinical research company** (formed from the IMS Health / Quintiles merger). Their software handles **clinical trial data, patient data and real-world health evidence**. That single fact should colour half your answers:

| Because it's healthcare data… | …say this, unprompted |
|---|---|
| **PHI/PII everywhere** | "I'd never log a patient identifier — I log a correlation ID and the record's surrogate key" |
| **HIPAA / GDPR** | Consent, data minimisation, right to erasure, data residency, pseudonymisation |
| **FDA-regulated systems (21 CFR Part 11 / GxP)** | **Audit trails, e-signatures, validated releases** — "in a regulated system I can't just hotfix prod; the change goes through a controlled, documented release" |
| **Trials run offline / on tablets in clinics** | This is *exactly* why they want **Ionic + Capacitor** — offline storage, sync, network handling |
| **Long-lived systems** | Technical debt, upgrade paths, code quality — the Tech Lead section |

> 🗣️ **Use it once, early:** "I understand this is healthcare data, so I'd treat auditability and PHI handling as functional requirements, not afterthoughts." That one sentence marks you as a lead rather than a senior developer.

---

## ✅ Readiness self-audit — tick these off as you go

Rate each **✅ can teach it / ⚠️ can define it / ❌ blank**. Study the ❌ and ⚠️ rows first.

| # | Area | Part | Your rating |
|---|---|---|---|
| 1 | **Angular** — components → Signals → performance/leaks | [Part 1](#part-1--angular) | |
| 2 | **TypeScript / JavaScript** | [Part 2](#part-2--typescript--javascript) | |
| 3 | **RxJS / NgRx** | [Part 3](#part-3--rxjs--ngrx) | |
| 4 | **Ionic + Capacitor** | [Part 4](#part-4--ionic--capacitor) | |
| 5 | **REST + Security** | [Part 5](#part-5--rest--security) | |
| 6 | **MongoDB + PostgreSQL/SQL** | [Part 6](#part-6--mongodb--postgresql--sql) | |
| 7 | **Microservices** | [Part 7](#part-7--microservices) | |
| 8 | **Git + CI/CD + Cloud** | [Part 8](#part-8--git--cicd--cloud) | |
| 9 | **Technical Lead questions** | [Part 9](#part-9--technical-lead-questions) | |
| 10 | **AI-assisted development** | [Part 10](#part-10--ai-assisted-development) | |

> 💡 **Areas 9 and 10 are where this job is won.** Every candidate can define a `BehaviorSubject`. Far fewer can describe a production incident they led, or how they review AI-generated code. **Do not leave those two to the night before.**

---

## 🧭 The three habits that decide the round

The pattern across your 10 logged rounds is consistent: **you answer question one and lose on question two.** Fix that mechanically.

**1. Every answer is three sentences.**
> **Definition** → **one concrete thing from your own project** → **one trade-off or failure mode.**
> *"An interceptor sits in the HttpClient pipeline and can transform every request and response. On RoboGebra I used one to attach the JWT and centralise 401 handling. The gotcha is that `HttpRequest` is immutable — you must `clone()` — and interceptors run in reverse order on the response."*

**2. Say "I don't know" cleanly, then bridge.**
> *"I haven't used Saga orchestration in production. I've read how it works — compensating transactions instead of a distributed commit — and the closest thing I've built is X."* A clean "I don't know" costs you almost nothing. Bluffing costs you the round.

**3. Lead answers, not just developer answers.** For a Tech Lead role, half of every technical answer should include *how you'd get a team to do it*: the convention, the lint rule, the code-review checklist, the ADR. Say **"on my team I'd…"**, not just "you can…".

---

# Part 1 — Angular

*Their list: components, lifecycle hooks, data binding, DI, services, routing, guards, resolvers, lazy loading, interceptors, forms, decorators, change detection, Signals, performance and memory leaks.*

## 1.1 Components

A component = **a class with a decorator + a template + styles**, the unit of UI. Key config: `selector`, `template`/`templateUrl`, `styles`, `standalone`, `changeDetection`, `providers`, `imports` (standalone), `encapsulation`.

**Component communication — asked in 4 of your rounds. Answer as a ladder:**

| Direction | Mechanism |
|---|---|
| Parent → Child | `@Input()` (or the new `input()` signal) |
| Child → Parent | `@Output() EventEmitter` (or `output()`) |
| Parent → Child (imperative) | `@ViewChild` to call a method |
| Content projection | `<ng-content select="[header]">` |
| Unrelated components | **A shared service with a `BehaviorSubject` / signal** |
| App-wide | **NgRx store** |

```ts
@Component({
  selector: 'app-patient-card',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h3>{{ patient().name }}</h3>
    <button (click)="select.emit(patient().id)">Select</button>
    <ng-content select="[actions]"></ng-content>`,
})
export class PatientCardComponent {
  patient = input.required<Patient>();          // Angular 17+ signal input
  select  = output<string>();                   // signal output
}
```

**Smart vs presentational** — the architecture answer a lead is expected to give: container components inject services and hold state; presentational components take `@Input`s, emit `@Output`s, are `OnPush` and are trivially testable. *"I enforce that split in review, because it's what keeps change detection cheap and components reusable."*

## 1.2 Lifecycle hooks

**Order — memorise it:**
```
constructor → ngOnChanges → ngOnInit → ngDoCheck →
ngAfterContentInit → ngAfterContentChecked →
ngAfterViewInit → ngAfterViewChecked →
   (DoCheck / AfterContentChecked / AfterViewChecked repeat every CD cycle)
→ ngOnDestroy
```

| Hook | Fires | Use it for |
|---|---|---|
| `constructor` | On instantiation | **DI only** — `@Input`s are `undefined` here |
| `ngOnChanges(changes)` | Before `ngOnInit` and on every `@Input` change | React to input changes; `changes.x.firstChange`/`previousValue` |
| `ngOnInit` | Once, after first `ngOnChanges` | API calls, subscriptions, form setup |
| `ngDoCheck` | Every CD cycle | Custom dirty checking — **expensive, avoid** |
| `ngAfterContentInit/Checked` | After `<ng-content>` projected | `@ContentChild` access |
| `ngAfterViewInit/Checked` | After the view + children render | `@ViewChild` DOM access, third-party libs (charts) |
| `ngOnDestroy` | On destruction | **Unsubscribe, clear timers, remove listeners** |

> ⚠️ **The trap they love:** changing a bound value in `ngAfterViewInit` throws **`ExpressionChangedAfterItHasBeenCheckedError`** in dev mode. Fixes: move it to `ngOnInit`, `cdr.detectChanges()`, or `queueMicrotask`. Knowing *why* (dev mode runs a second verification pass) is the senior answer.
>
> 💡 **Modern note:** signals + `effect()`, `afterNextRender()`/`afterRenderEffect()` and `DestroyRef` are replacing several of these hooks.

## 1.3 Data binding

| Type | Syntax | Direction |
|---|---|---|
| Interpolation | `{{ value }}` | Class → View |
| Property | `[prop]="value"` | Class → View |
| Event | `(click)="fn($event)"` | View → Class |
| Two-way | `[(ngModel)]` / `[(x)]` | Both |

`[(x)]` is **sugar** for `[x]` + `(xChange)` — implement `@Input() x` + `@Output() xChange` (exact naming) and your own component supports it. Also `[attr.aria-label]`, `[class.active]`, `[style.width.px]`.

> ❌ **Never call a method in a template** (`{{ getTotal() }}`) — it re-runs on every CD cycle. Use a pure pipe, a precomputed field, `async`, or a `computed()` signal. **This is a performance answer disguised as a binding answer — say it.**

## 1.4 Dependency injection & services

**What it is:** you declare what you need in the constructor; Angular's **hierarchical injector** supplies it. That's what makes services swappable and components testable.

```ts
@Injectable({ providedIn: 'root' })      // tree-shakable app-wide singleton
export class PatientService {
  private http = inject(HttpClient);     // inject() — Angular 14+, works in field initializers
}
```

**Provider scopes:**
| Where | Effect |
|---|---|
| `providedIn: 'root'` | One singleton, **tree-shakable** (dropped if unused) |
| `providers: []` on a component | **A new instance per component instance** |
| `providers: []` in a lazy-loaded route | One instance for that lazy injector — *not* the root singleton |
| `providedIn: 'platform' / 'any'` | Across apps on the page / per lazy module |

**Injector hierarchy:** element injector → parent element injectors → module/environment injector → root → platform → null (then `NullInjectorError: No provider for X`).

**Modifiers:** `@Optional()` `@Self()` `@SkipSelf()` `@Host()`.

**Provider types** — the part that shows depth:
```ts
{ provide: ApiUrl,       useValue: 'https://api.iqvia.internal' }
{ provide: LoggerService, useClass: ProdLoggerService }
{ provide: AuthService,   useExisting: OAuthService }        // alias, same instance
{ provide: CONFIG, useFactory: (env: Env) => new Config(env), deps: [Env] }
```
**`InjectionToken`** for anything that isn't a class (config objects, primitives) — `new InjectionToken<AppConfig>('app.config')`.

> 🎯 **Lead framing:** "DI is what lets me swap a real `PatientApiService` for a mock in tests without touching the component, and it's how I'd stub the native layer when the same Ionic code runs in a browser."

## 1.5 Routing, guards, resolvers

```ts
export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },   // pathMatch REQUIRED here
  {
    path: 'trials',
    canActivate: [authGuard],
    canMatch: [featureFlagGuard],                             // decides if the route matches at all
    loadChildren: () => import('./trials/trials.routes').then(m => m.TRIAL_ROUTES),
  },
  {
    path: 'patient/:id',
    loadComponent: () => import('./patient/patient.component').then(m => m.PatientComponent),
    resolve: { patient: patientResolver },
    canDeactivate: [unsavedChangesGuard],
    data: { roles: ['INVESTIGATOR'] },
  },
  { path: '**', component: NotFoundComponent },               // wildcard LAST
];
```

**The guard types — know all five:**
| Guard | Question it answers |
|---|---|
| `CanActivate` | May the user open this route? |
| `CanActivateChild` | …and its children? |
| `CanDeactivate` | May they leave? (**unsaved-changes prompt**) |
| `CanMatch` | Should this route even match? (**best for role/feature-flag routing — the lazy chunk isn't downloaded if it doesn't match**) |
| `CanLoad` *(deprecated → use `CanMatch`)* | May the lazy bundle load? |

```ts
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService), router = inject(Router);
  return auth.isAuthenticated()
    ? true
    : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
```
> 💡 **Return a `UrlTree`, don't `navigate()` + `return false`** — it cancels the navigation atomically.

**Resolvers** — fetch data *before* the route activates, so the component never renders empty:
```ts
export const patientResolver: ResolveFn<Patient> = (route) =>
  inject(PatientService).get(route.paramMap.get('id')!).pipe(
    catchError(() => { inject(Router).navigate(['/not-found']); return EMPTY; })
  );
// component: patient = this.route.snapshot.data['patient'];
```
> ⚠️ **The trade-off to volunteer:** a resolver **blocks navigation** — the old page stays on screen while it loads, which feels like a frozen app on a slow clinic network. *"I use resolvers for small, essential data and a skeleton/loading state for everything else."* That answer alone is worth the question.

**Reading params:** `paramMap`/`queryParamMap` as **observables** (component reused when only the id changes) vs `snapshot` (one-shot). Getting that distinction right is a common trap.

## 1.6 Lazy loading

```ts
{ path: 'reports', loadChildren: () => import('./reports/reports.routes').then(m => m.ROUTES) }
{ path: 'profile', loadComponent: () => import('./profile.component').then(m => m.ProfileComponent) }
```
- **Preloading:** `provideRouter(routes, withPreloading(PreloadAllModules))`, or a **custom strategy** that preloads only routes flagged `data: { preload: true }`.
- ⚠️ **Never also eagerly import the lazy module/component** — it silently defeats the split.
- **Disadvantages** (always give one): first-navigation delay, duplicated deps across chunks, a service `providedIn` a lazy scope gets its **own instance**, and `ChunkLoadError` after a redeploy (handle it by reloading the page).
- **`@defer` (v17+)** does the same at *template* level: `@defer (on viewport) { <app-chart/> } @placeholder { … } @loading { … }`.

## 1.7 Interceptors

```ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const authReq = req.clone({ setHeaders: { Authorization: `Bearer ${auth.token}` } });
  return next(authReq).pipe(
    timeout(30_000),
    retry({ count: 2, delay: (err, n) => timer(500 * 2 ** n) }),   // backoff
    catchError((e: HttpErrorResponse) => handle(e)),
  );
};
provideHttpClient(withInterceptors([correlationIdInterceptor, authInterceptor, errorInterceptor]));
```
**Uses:** auth · central error handling · loading indicator · logging/timing · retry · caching · correlation ID · API base URL.
**Three details that prove you've used them:** requests are **immutable → `clone()`**; class-based needs **`multi: true`**; order is **registration order out, reverse order back**.

## 1.8 Forms

| | Template-driven | Reactive |
|---|---|---|
| Module | `FormsModule` | `ReactiveFormsModule` |
| Source of truth | The template | **The TS class** |
| Structure | `ngModel` | `FormGroup`/`FormControl`/`FormArray` |
| Validation | Directives | Functions — **unit-testable** |
| Async / dynamic | Painful | Natural |
| Use for | Tiny forms | **Everything real** |

```ts
form = this.fb.nonNullable.group({
  email:    ['', [Validators.required, Validators.email], [this.uniqueEmailValidator()]],
  password: ['', [Validators.required, Validators.minLength(8)]],
  confirm:  [''],
  doses: this.fb.array<FormGroup>([]),                       // FormArray for dynamic rows
}, { validators: passwordsMatch });                          // cross-field validator

// custom sync validator
export const passwordsMatch: ValidatorFn = (g: AbstractControl) =>
  g.get('password')!.value === g.get('confirm')!.value ? null : { mismatch: true };

// async validator (debounced server check)
uniqueEmail(): AsyncValidatorFn {
  return c => timer(300).pipe(switchMap(() => this.api.checkEmail(c.value)),
                              map(taken => taken ? { taken: true } : null));
}
```
**Know:** control states (`pristine/dirty`, `touched/untouched`, `valid/invalid`, `pending`), `updateOn: 'blur'|'submit'` (a real performance lever on big forms), `setValue` (all controls, strict) vs `patchValue` (partial), `valueChanges`/`statusChanges` as observables, `markAllAsTouched()` before showing errors on submit, and **typed forms** (Angular 14+).
> 💬 Your resume has **Angular Formly** — mention it here: *"For the clinical-form use case I'd push toward schema-driven forms (Formly / JSON-defined), because the forms change per study and you don't want a release for every field change."* For IQVIA that's an on-the-nose answer.

## 1.9 Decorators

Metadata functions in four flavours: **class** (`@Component`, `@Directive`, `@Pipe`, `@Injectable`, `@NgModule`), **property** (`@Input`, `@Output`, `@ViewChild`, `@HostBinding`), **method** (`@HostListener`), **parameter** (`@Inject`, `@Optional`, `@Self`, `@SkipSelf`).
> Angular uses TypeScript's *legacy* decorators, and v16+ is moving to the **signal-based functions** `input()`, `output()`, `viewChild()`, `model()` instead.

## 1.10 Change detection — the topic that separates senior from mid

**How it works:** Angular runs CD **top-down from the root** whenever something *might* have changed. Historically **Zone.js** monkey-patches async APIs (events, `setTimeout`, XHR) and tells Angular "something happened — check everything".

**`Default` vs `OnPush`:**
| | `Default` | `OnPush` |
|---|---|---|
| When the component is checked | Every cycle, always | Only when: an **`@Input` reference** changes · an **event fires inside it** · an **`async` pipe emits** · `markForCheck()` is called · **a signal it reads changes** |

```ts
@Component({ changeDetection: ChangeDetectionStrategy.OnPush, ... })
```
> ⚠️ **`OnPush` compares `@Input`s by reference.** `this.items.push(x)` will **not** trigger it; `this.items = [...this.items, x]` will. **That is the #1 real-world `OnPush` bug — say it and you've answered the follow-up before it's asked.**

**The escape hatches:**
```ts
private cdr = inject(ChangeDetectorRef);
this.cdr.markForCheck();     // mark this component + ancestors dirty for the NEXT cycle
this.cdr.detectChanges();    // run CD on this subtree NOW
this.cdr.detach();           // opt out entirely (very hot components) — you drive it yourself
```
**Running work outside Angular** (the classic performance fix):
```ts
this.zone.runOutsideAngular(() => {
  // mousemove / animation loop / chart tick — no CD per frame
  requestAnimationFrame(loop);
});
this.zone.run(() => this.state = done);   // re-enter only when the UI must update
```
> 🎯 **The modern closer:** "With **signals**, Angular knows exactly which components read the value that changed, so it can do **fine-grained** updates instead of dirty-checking a tree — and that's what makes **zoneless** (`provideZonelessChangeDetection()`) possible."

## 1.11 Signals

**What:** a reactive primitive — a value that knows who reads it, so Angular can update precisely what changed.

```ts
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `{{ fullName() }} — {{ visits().length }} visits`,
})
export class PatientComponent {
  // inputs / outputs / queries as signals
  patient = input.required<Patient>();
  saved   = output<Patient>();

  first = signal('Ada');
  last  = signal('Lovelace');
  fullName = computed(() => `${this.first()} ${this.last()}`);   // lazy + memoised

  visits = signal<Visit[]>([]);

  constructor() {
    effect(() => console.log('patient changed', this.patient().id));  // side effects
  }

  rename(n: string) {
    this.first.set(n);                       // set  — replace
    this.visits.update(v => [...v, newVisit]); // update — derive from current
  }
}
```

| Primitive | Purpose |
|---|---|
| `signal(v)` | Writable state — `.set()`, `.update()`, `()` to read |
| `computed(fn)` | **Derived, lazy, memoised**; recomputes only when a dependency changed |
| `effect(fn)` | Side effects when dependencies change (logging, localStorage, DOM) — **not** for deriving state |
| `input()` / `input.required()` / `model()` | Signal-based `@Input` / two-way |
| `output()` | Signal-era `@Output` |
| `viewChild()` / `contentChild()` | Signal queries |
| `linkedSignal()` (v19) | Writable state that resets from a source |
| `resource()` / `httpResource()` (v19/20, experimental) | Async data as a signal |
| `toSignal()` / `toObservable()` | The **RxJS interop bridge** |

**Signals vs RxJS — the question they will ask:**
> "Signals are for **synchronous state** — 'what is the value right now', with glitch-free derived values. RxJS is for **asynchronous streams over time** — HTTP, websockets, debouncing, cancellation, retry. In practice I use RxJS for the pipeline and `toSignal()` at the edge to feed the template."

```ts
readonly results = toSignal(
  toObservable(this.query).pipe(
    debounceTime(300), distinctUntilChanged(),
    switchMap(q => this.api.search(q)),
  ), { initialValue: [] });
```
> 💡 **Benefits to name:** no `async` pipe boilerplate, no manual unsubscribe (signals clean up with the injection context), fine-grained CD, and **zoneless** apps. `effect()` misuse — writing state from an effect — is the anti-pattern to flag.

## 1.12 Performance

Have **eight** ready; anyone can name two.

1. **`OnPush` everywhere** + immutable updates.
2. **`trackBy` / the new `@for (item of items; track item.id)`** — without it Angular destroys and rebuilds every DOM node on each change.
3. **Lazy load routes**, `@defer` heavy blocks, preload intelligently.
4. **Never call functions in templates**; use pure pipes / `computed()`.
5. **Virtual scrolling** (`cdk-virtual-scroll-viewport`) for long clinical lists.
6. **`runOutsideAngular`** for high-frequency events; **debounce/throttle** inputs and scroll.
7. **Bundle discipline** — `source-map-explorer` / `ng build --stats-json`, budgets in `angular.json` that **fail the CI build**, tree-shakable providers, drop moment.js-style heavy deps.
8. **Network** — cache GETs in an interceptor, `shareReplay(1)` for shared data, pagination instead of loading everything, gzip/brotli, HTTP/2.
9. **Rendering** — `NgOptimizedImage`, SSR/hydration if it's a public app, `@defer (on viewport)`.
10. **Signals / zoneless** for fine-grained updates.

> 🗣️ **Lead-level answer:** "I don't optimise by instinct — I profile first with the **Angular DevTools profiler** and Chrome Performance, find whether it's change detection, bundle size or the network, and fix that. Then I add a **CI budget** so the regression can't come back."

## 1.13 Memory leaks — say all five sources

| Leak | Fix |
|---|---|
| **Unclosed subscriptions** | `async` pipe · `takeUntilDestroyed()` · `takeUntil(destroy$)` in `ngOnDestroy` · `take(1)`/`first()` |
| **`setInterval` / `setTimeout`** | `clearInterval` in `ngOnDestroy` |
| **Manual DOM listeners** (`addEventListener`, third-party libs, charts, maps) | Remove/`destroy()` in `ngOnDestroy` |
| **A long-lived service holding component references** (or an ever-growing cache/`BehaviorSubject` array) | Clear it; bound the cache |
| **Detached DOM / global registries** | Destroy plugin instances; unregister |

```ts
export class LiveVitalsComponent {
  private destroyRef = inject(DestroyRef);
  ngOnInit() {
    this.socket.vitals$
      .pipe(takeUntilDestroyed(this.destroyRef))   // Angular 16+ — no destroy$ boilerplate
      .subscribe(v => this.vitals.set(v));
  }
}
```
> 🔎 **How you'd *find* one:** Chrome DevTools **Memory** tab → heap snapshot, navigate away and back a few times, snapshot again, and compare **Detached HTMLElements** / retained component instances. Saying *how you diagnose* it, not just how you avoid it, is the lead answer.
>
> ⚠️ **Note:** `HttpClient` calls complete on their own, so they don't leak in the same way — but an in-flight request can still call back into a destroyed component. `takeUntilDestroyed` handles both.

🔗 Deeper: **[04 — Angular](./04-angular.md)** · **[25 — Binding & Forms](./25-angular-binding-forms.md)**

---

# Part 2 — TypeScript / JavaScript

*Their list: var/let/const, arrow functions, callbacks/promises, event bubbling/capturing, ES6, interfaces/classes, OOP, generics, any/unknown/never/void, modules, destructuring, spread/rest.*

## 2.1 `var` / `let` / `const`

| | `var` | `let` | `const` |
|---|---|---|---|
| Scope | **Function** | Block | Block |
| Hoisting | Hoisted, initialised `undefined` | Hoisted into the **TDZ** → `ReferenceError` | Same |
| Redeclare | ✅ | ❌ | ❌ |
| Reassign | ✅ | ✅ | ❌ (**the binding**, not the contents) |
| On `globalThis` | ✅ | ❌ | ❌ |

```ts
const user = { name: 'Ada' };
user.name = 'Grace';   // ✅ allowed — const freezes the binding, not the object
// user = {};          // ❌ TypeError
```
**The classic output question:**
```js
for (var i = 0; i < 3; i++) setTimeout(() => console.log(i));  // 3 3 3
for (let i = 0; i < 3; i++) setTimeout(() => console.log(i));  // 0 1 2  (new binding per iteration)
```
> 🗣️ Rule: **`const` by default, `let` when you must reassign, `var` never.**

## 2.2 Arrow functions

**The point is lexical `this`** — an arrow doesn't have its own `this`, it closes over the enclosing scope. That's why callbacks inside a class work without `.bind(this)`.

Also: no `arguments` (use rest `...args`), **not constructible** (`new` throws), no `prototype`, can't be a generator, and **implicit return** without braces (`x => ({a: 1})` needs parens for an object literal).
> ❌ **Don't use an arrow as an object method** (`this` won't be the object) or as a prototype method you intend to be overridable.

## 2.3 Callbacks, promises, async/await, the event loop

**Callback hell → Promises → async/await → RxJS** is the arc to narrate.

```js
// Promise states: pending → fulfilled | rejected  (settled, immutable after)
const data = await Promise.all([getPatient(id), getVisits(id)]);   // fail fast, all must succeed
await Promise.allSettled(tasks);   // never rejects — status per item
await Promise.race(tasks);         // first to SETTLE (resolve or reject)
await Promise.any(tasks);          // first to FULFIL; AggregateError if all reject
```
```ts
async function load() {
  try { return await this.api.get(); }
  catch (e) { log(e); throw e; }
  finally { this.loading = false; }
}
```
**Event loop — the answer that always earns points:**
> "The call stack runs synchronous code. Async callbacks go to queues. After the stack empties, the loop drains **all microtasks** (promise callbacks, `queueMicrotask`, `MutationObserver`) **before** the next **macrotask** (`setTimeout`, `setInterval`, I/O, UI events). That's why a promise callback beats a `setTimeout(0)`."

```js
console.log('1');
setTimeout(() => console.log('2'));
Promise.resolve().then(() => console.log('3'));
console.log('4');
// 1 4 3 2
```
**Sequential vs parallel** — a real interview differentiator:
```ts
const a = await getA(); const b = await getB();      // ❌ sequential — 2× latency
const [a, b] = await Promise.all([getA(), getB()]);  // ✅ parallel
```

## 2.4 Event bubbling & capturing

Three phases: **capture (window → target)** → **target** → **bubble (target → window)**.
```js
el.addEventListener('click', fn);         // bubble phase (default)
el.addEventListener('click', fn, true);   // capture phase
```
| Method | Does |
|---|---|
| `stopPropagation()` | Stops the event travelling further |
| `stopImmediatePropagation()` | …and stops other listeners on the **same** element |
| `preventDefault()` | Cancels the default action (form submit, link) — **does not stop propagation** |

**Event delegation** — one listener on the parent instead of 500 on rows; uses `event.target` and works for dynamically added elements. In Angular this is what makes `(click)` on a `@for` row cheap.
> ⚠️ `focus`/`blur` don't bubble (use `focusin`/`focusout`); `event.target` = what was clicked, `event.currentTarget` = what the listener is on.

## 2.5 ES6+ essentials

`let`/`const` · arrow functions · template literals · **destructuring** · **spread/rest** · default parameters · classes · **modules** · Promises · `Map`/`Set`/`WeakMap` · `Symbol` · generators · optional chaining `?.` · nullish coalescing `??` · logical assignment `??=` · `Object.entries/values/fromEntries` · `Array.flat/flatMap/at/includes` · `String.padStart/replaceAll` · top-level `await` · `structuredClone`.

## 2.6 Destructuring, spread & rest

```ts
const { name, age = 0, address: { city } = {}, ...rest } = patient;   // rename, default, nested, rest
const [first, , third = 'n/a'] = list;
function f({ id, active = true }: Opts) {}                            // destructured params

const merged = { ...defaults, ...overrides };        // spread — later wins
const copy   = [...items, newItem];                  // immutable append (OnPush-safe!)
function sum(...nums: number[]) {}                   // rest — collects
```
> ⚠️ **Spread is a *shallow* copy.** `{...patient}` shares the nested `address` object. Deep copy: `structuredClone(obj)` (modern) or a library. This exact point connects to `OnPush` and NgRx immutability — link them out loud.

## 2.7 Interfaces, classes & OOP in TypeScript

```ts
interface Identifiable { id: string; }
interface Auditable { createdAt: Date; createdBy: string; }

abstract class BaseEntity implements Identifiable {
  protected constructor(public readonly id: string) {}
  abstract describe(): string;                       // subclasses must implement
}

class Patient extends BaseEntity implements Auditable {
  #ssn: string;                                      // true JS private (runtime)
  constructor(id: string, private name: string,      // parameter properties
              public createdAt = new Date(), public createdBy = 'system', ssn = '') {
    super(id); this.#ssn = ssn;
  }
  describe() { return `${this.name} (${this.id})`; }  // polymorphism
  get initials() { return this.name[0]; }
}
```

**`type` vs `interface`** — a guaranteed question:
| `interface` | `type` |
|---|---|
| **Declaration merging** (reopens) | ❌ |
| `extends` | Intersections `&` |
| Objects/classes only | **Unions, tuples, primitives, mapped & conditional types** |
| Better error messages, preferred for public APIs | Use when you need unions or type computation |

**The 4 OOP pillars, in TS terms:** encapsulation (`private`/`#`, getters) · inheritance (`extends`) · polymorphism (override / interface implementations) · abstraction (`abstract`, interfaces).
> 🎯 **The senior point: TypeScript is *structurally* typed.** Two unrelated types match if their shapes match — unlike Java/C# nominal typing. And `private`/`readonly` are **compile-time only**; `#field` is enforced at runtime.
>
> **Composition over inheritance** — say it: in Angular you compose services via DI far more than you extend classes.

## 2.8 Generics

```ts
function first<T>(arr: T[]): T | undefined { return arr[0]; }

interface ApiResponse<T> { data: T; total: number; page: number; }

// constraint
function pluck<T, K extends keyof T>(obj: T, key: K): T[K] { return obj[key]; }

class Repository<T extends { id: string }> {
  private items = new Map<string, T>();
  save(item: T): void { this.items.set(item.id, item); }
  find(id: string): T | undefined { return this.items.get(id); }
}

// conditional + mapped + infer
type Unwrap<T> = T extends Observable<infer U> ? U : T;
type Optional<T> = { [K in keyof T]?: T[K] };
```
**Built-in utility types to name:** `Partial` `Required` `Readonly` `Pick` `Omit` `Record` `Exclude` `Extract` `NonNullable` `ReturnType` `Parameters` `Awaited`.
> 🗣️ **Why generics:** "Type safety without duplication — one `ApiResponse<T>` instead of one per endpoint, and the compiler catches the mismatch instead of a runtime error in a clinical form."

## 2.9 `any` / `unknown` / `never` / `void`

| Type | Meaning | Rule |
|---|---|---|
| **`any`** | Turn the checker off | ❌ Avoid — one `any` leaks through your whole call chain |
| **`unknown`** | Type-safe `any` — **you must narrow before use** | ✅ Use at every boundary: API responses, `JSON.parse`, `catch (e: unknown)` |
| **`never`** | A value that can never occur — a function that never returns | **Exhaustiveness checking** |
| **`void`** | The function returns nothing meaningful | Return type only |
| `null`/`undefined` | Absence | Turn on `strictNullChecks` |

```ts
function handle(e: unknown) {
  if (e instanceof HttpErrorResponse) console.log(e.status);   // narrowed → safe
}

type Status = 'active' | 'closed' | 'pending';
function label(s: Status): string {
  switch (s) {
    case 'active':  return 'Active';
    case 'closed':  return 'Closed';
    case 'pending': return 'Pending';
    default: const _exhaustive: never = s; return _exhaustive;   // add a status → compile error
  }
}
```
> 🎯 **That exhaustiveness trick is the answer to "what's `never` for".** Land it.

## 2.10 Modules

**ES Modules (ESM)** — the standard: `export` / `export default` / `import` / `import * as` / **dynamic `import()`** (this is what powers Angular lazy loading). Static, tree-shakable, hoisted, always strict mode, top-level `await`.
**CommonJS** — Node's `require`/`module.exports`, dynamic, not tree-shakable.
Also **AMD** and **UMD** (legacy/browser bundles).
In Angular "module" also means **NgModule** (`declarations`/`imports`/`exports`/`providers`) — root, feature, shared, core, routing — increasingly replaced by **standalone components**. **Answer both senses; that's exactly what Tech Mahindra asked.**

> ⚠️ **Barrel files (`index.ts`)** are convenient but can defeat tree-shaking and create circular imports. Naming that is a lead-level detail.

🔗 Deeper: **[01 — JavaScript](./01-javascript.md)** · **[03 — TypeScript](./03-typescript.md)** · **[19 — JS Variables](./19-javascript-variables-easy.md)** · **[34 — Tech Mahindra round](./34-techmahindra-angular-round.md)**

---

# Part 3 — RxJS / NgRx

*Their list: Observable vs Promise, Subject types, hot/cold, switchMap/mergeMap/concatMap/exhaustMap, forkJoin, error handling, unsubscribe/memory leaks, Store/Action/Reducer/Effect/Selector.*

## 3.1 Observable vs Promise — asked in 4 of your rounds

| | Promise | Observable |
|---|---|---|
| Values | **One**, ever | **Zero to many**, over time |
| Execution | **Eager** — starts at creation | **Lazy** — nothing runs until `subscribe()` |
| Cancellable | ❌ | ✅ `unsubscribe()` (Angular aborts the XHR) |
| Operators | `.then` chaining only | **200+ operators** — map, filter, debounce, retry, combine |
| Retry | Manual | `retry()` / `retryWhen` |
| Multicast | Always one result, shared | Unicast by default; multicast with `Subject`/`share()` |
| Sync possible | Never (always async) | Can emit synchronously |
| Native | ✅ | Library (RxJS) |

> 🗣️ **The one-liner:** "A promise is a **single future value you can't cancel**; an observable is a **cancellable stream you compose**. Angular uses observables because a typeahead needs debouncing and cancellation, and a promise can't do either."
>
> Convert: `firstValueFrom(obs$)` / `lastValueFrom(obs$)`; `from(promise)`.

## 3.2 Subject types

| Type | Behaviour | Use for |
|---|---|---|
| **`Subject`** | Multicast; **late subscribers get nothing** | Event bus, "something happened" notifications |
| **`BehaviorSubject`** | **Requires an initial value**; replays the **latest** to new subscribers; `.value` | ⭐ **State in a service** — current user, selected trial |
| **`ReplaySubject(n, time?)`** | Replays the last *n* (optionally within a time window) | Caching recent events, late-joining logs |
| **`AsyncSubject`** | Emits **only the last value, and only on `complete()`** | Rare — a one-shot computation result |

```ts
@Injectable({ providedIn: 'root' })
export class SessionService {
  private user$ = new BehaviorSubject<User | null>(null);
  readonly currentUser$ = this.user$.asObservable();   // expose read-only — never the Subject
  setUser(u: User) { this.user$.next(u); }
}
```
> 🎯 **Lead detail:** always expose `.asObservable()` (or a `readonly` signal) so consumers can't `next()` into your state. That's an encapsulation answer inside an RxJS question.

## 3.3 Hot vs cold

**Cold** = the producer is created *per subscriber* → each subscriber gets its **own independent execution**. `HttpClient` is cold — **subscribe twice, the request fires twice.**
**Hot** = one shared producer, subscribers share the same values, and late subscribers miss what already happened. `Subject`, DOM events, websockets.

```ts
const data$ = this.http.get('/api/trials').pipe(shareReplay({ bufferSize: 1, refCount: true }));
// now multiple subscribers share ONE request and late ones get the cached value
```
> ⚠️ **`shareReplay(1)` without `refCount: true` keeps the source alive forever** — that's a real, subtle memory leak. Naming it is a strong senior signal.

## 3.4 The four flattening operators — know when, not just what

| Operator | Behaviour | Canonical use |
|---|---|---|
| **`switchMap`** | **Cancels** the previous inner observable | ⭐ **Typeahead / search, route param → fetch** — you only want the latest |
| **`mergeMap`** (`flatMap`) | Runs all in **parallel**, order not guaranteed | Independent parallel work, e.g. uploading N files |
| **`concatMap`** | **Queues** — one at a time, in order | ⭐ **Writes that must not reorder** — a sequence of PATCHes, offline sync queue |
| **`exhaustMap`** | **Ignores** new ones while one is in flight | ⭐ **Login / submit button — kills double-submit** |

```ts
this.search.valueChanges.pipe(
  debounceTime(300),
  distinctUntilChanged(),
  switchMap(q => this.api.search(q)),        // cancels the stale request
  takeUntilDestroyed(this.destroyRef),
).subscribe(r => this.results.set(r));
```
> ⚠️ **`switchMap` on a POST is a bug** — cancelling a write leaves the server state unknown. Use `concatMap` or `exhaustMap` for writes. **That sentence is the whole question.**

## 3.5 Combination & creation

| Operator | Behaviour |
|---|---|
| **`forkJoin`** | Waits for **all to complete**, emits the last of each — **like `Promise.all`**. ⚠️ **If any one errors, you get nothing**; if one never completes, it never emits |
| `combineLatest` | Emits whenever **any** source emits (after all have emitted at least once) |
| `zip` | Pairs emissions by index |
| `withLatestFrom` | Primary stream drives; samples the others |
| `merge` / `concat` | Interleave / run in sequence |
| `startWith`, `of`, `from`, `timer`, `interval`, `fromEvent`, `EMPTY`, `NEVER`, `throwError`, `defer` | Creation |

```ts
forkJoin({
  patient: this.api.getPatient(id),
  visits:  this.api.getVisits(id).pipe(catchError(() => of([]))),   // ⭐ isolate the failure
}).subscribe(({ patient, visits }) => …);
```
> 🎯 **"What if one forkJoin call fails?"** was asked at Photon. The answer is exactly the line above: **`catchError` on the inner observable** so one non-critical failure doesn't lose the whole page.

## 3.6 Error handling

```ts
this.api.get().pipe(
  timeout(30_000),
  retry({ count: 3, delay: (err, n) => n < 3 && err.status >= 500 ? timer(1000 * 2 ** n) : throwError(() => err) }),
  catchError(err => {
    this.toast.error('Could not load trials');
    return of([]);                       // recover with a fallback → stream CONTINUES
    // or: return throwError(() => new AppError(err));   // rethrow → stream DIES
  }),
  finalize(() => this.loading.set(false)),   // runs on complete, error AND unsubscribe
).subscribe();
```
> ⚠️ **An error terminates the observable.** In a long-lived stream (a `valueChanges` typeahead), put the `catchError` **inside** the `switchMap` — otherwise one failed search kills the whole subscription and the box stops working. **This is the single most common real RxJS bug — say it.**
```ts
switchMap(q => this.api.search(q).pipe(catchError(() => of([]))))   // ✅ inner catch
```
Also: `EMPTY` (complete silently) vs `of([])` (emit a fallback) vs `throwError`. Plus Angular's `ErrorHandler` for a global net.

## 3.7 Unsubscribing & memory leaks

**In order of preference:**
1. **`async` pipe** — subscribes and unsubscribes for you. *"If I can put it in the template, I do."*
2. **`takeUntilDestroyed(destroyRef)`** (Angular 16+) — cleanest imperative option.
3. `takeUntil(this.destroy$)` + `destroy$.next()` in `ngOnDestroy` — the classic. ⚠️ **`takeUntil` must be the last operator** in the pipe.
4. `take(1)` / `first()` for one-shot streams.
5. `Subscription`/`subscription.add()` + `unsubscribe()` in `ngOnDestroy`.

> **Which streams actually leak?** Long-lived ones — `Subject`s in services, `valueChanges`, `router.events`, `fromEvent`, websockets, `interval`. `HttpClient` completes on its own, but the callback can still fire into a dead component.
>
> 🎯 **Lead answer:** "I make this a review rule, not a memory exercise: `async` pipe by default, and the `rxjs-angular/prefer-takeuntil` ESLint rule fails the build if someone subscribes without teardown."

## 3.8 NgRx — Store / Action / Reducer / Effect / Selector

**The flow (draw this):**
```
Component --dispatch--> Action --> Reducer --(new state)--> Store --Selector--> Component
                          │
                          └--> Effect --(side effect: HTTP)--> new Action --> Reducer
```

| Piece | Role | Rule |
|---|---|---|
| **Store** | Single immutable state tree | One source of truth |
| **Action** | A **unique event** describing *what happened* | Name it `[Source] Event`, e.g. `[Trials Page] Load Trials` — **not** a command |
| **Reducer** | `(state, action) => newState` | **Pure. Synchronous. Immutable.** No HTTP, no `Date.now()`, no mutation |
| **Effect** | Listens to actions, does the side effect, dispatches a result action | Where **all** async lives |
| **Selector** | Queries + derives state; **memoised** | Components never reach into raw state |

```ts
// actions
export const TrialsActions = createActionGroup({
  source: 'Trials API',
  events: {
    'Load Trials': emptyProps(),
    'Load Trials Success': props<{ trials: Trial[] }>(),
    'Load Trials Failure': props<{ error: string }>(),
  },
});

// reducer
export const trialsReducer = createReducer(
  initialState,
  on(TrialsActions.loadTrials,        s => ({ ...s, loading: true, error: null })),
  on(TrialsActions.loadTrialsSuccess, (s, { trials }) => ({ ...s, loading: false, trials })),
  on(TrialsActions.loadTrialsFailure, (s, { error })  => ({ ...s, loading: false, error })),
);

// effect
loadTrials$ = createEffect(() => this.actions$.pipe(
  ofType(TrialsActions.loadTrials),
  switchMap(() => this.api.getTrials().pipe(
    map(trials => TrialsActions.loadTrialsSuccess({ trials })),
    catchError(err => of(TrialsActions.loadTrialsFailure({ error: err.message }))),   // ⭐ INSIDE
  )),
));

// selectors
export const selectTrialsState = createFeatureSelector<TrialsState>('trials');
export const selectAllTrials   = createSelector(selectTrialsState, s => s.trials);
export const selectActive      = createSelector(selectAllTrials, ts => ts.filter(t => t.active));
```

> ⚠️ **The single most-asked NgRx trap:** `catchError` must be **inside** the inner `switchMap`. If it's on the outer `actions$` pipe, the first failure **kills the effect forever** and the feature silently stops working until a page reload. If you say only one thing about NgRx, say this.

**Also know:** `@ngrx/entity` (`EntityAdapter` for normalised collections), **Redux DevTools** time-travel, `@ngrx/component-store` (local, per-component state — no global boilerplate), **NgRx Signal Store** (the modern signal-based option), and meta-reducers (logging, hydration).

**"When would you NOT use NgRx?"** — the mature answer they want:
> "NgRx is a lot of ceremony. For a small app or state used by one feature, a service with a `BehaviorSubject` or a `signal` is simpler and I'd argue for it in review. I reach for NgRx when state is **shared across unrelated features, survives navigation, needs debuggable/time-travellable history, or has complex async coordination** — which in a clinical app is real: a study context that a dozen screens read from."

🔗 Deeper: **[20 — RxJS Operators](./20-rxjs-operators.md)** · **[21 — NgRx](./21-ngrx.md)**

---

# Part 4 — Ionic + Capacitor

*Their list: architecture, Angular vs Ionic lifecycle, navigation, native-device access, Capacitor/plugins, Android/iOS builds, permissions, storage, network handling, mobile performance.*

> 💡 **This is your differentiator.** Most Angular candidates can't answer this section at all, and it's on their list — which means the role involves a mobile/tablet app (very likely **clinical data capture in the field**). Prepare it *properly*.

## 4.1 Ionic architecture

**The layers:**
```
Your Angular app
   └─ Ionic Framework — UI components (Web Components built with Stencil) + navigation + theming
        └─ Capacitor  — the native bridge (JS ↔ native runtime)
             └─ iOS (WKWebView) / Android (Chrome WebView) / Web (PWA)
```
- **One codebase, three targets** (iOS, Android, web/PWA) — that's the value proposition.
- Ionic components are **standard web components**, so they're framework-agnostic (Angular, React, Vue).
- **Theming via CSS custom properties** — `--ion-color-primary`, dark mode, platform-adaptive styling (`ios` / `md` modes).
- **Cordova vs Capacitor:** Capacitor is the modern successor — **native projects are source-controlled artefacts you can open in Xcode/Android Studio**, plugins are simpler, and it works in the browser as a PWA. Cordova plugins are still usable through Capacitor's compatibility layer.

## 4.2 Angular vs Ionic lifecycle — the question they will ask

**The reason both exist:** Ionic **keeps pages in the DOM** when you navigate forward (so the back transition is instant and state is preserved). So `ngOnInit` does **not** run again when you come back — but `ionViewWillEnter` does.

| Angular | Ionic | Fires |
|---|---|---|
| `ngOnInit` | — | **Once**, when the component is created |
| — | `ionViewWillEnter` | **Every time** the page is about to become active ⭐ |
| — | `ionViewDidEnter` | After the enter transition finishes (animations, focus, chart init) |
| — | `ionViewWillLeave` | Before leaving (pause timers, save draft) |
| — | `ionViewDidLeave` | After leaving |
| `ngOnDestroy` | — | When the page is actually destroyed (popped off the stack) |

```ts
export class VisitListPage {
  ngOnInit()          { this.setupForm(); }          // one-time setup
  ionViewWillEnter()  { this.refreshVisits(); }      // ⭐ re-fetch on every return
  ionViewWillLeave()  { this.saveDraft(); }
}
```
> 🎯 **The bug story that proves you've shipped Ionic:** "The classic symptom is *'the list doesn't refresh when I press back'*. The fetch was in `ngOnInit`, which only ran once because the page was cached. Moving it to `ionViewWillEnter` fixes it." Tell it as a story — it's far more convincing than the table.

Also know **`ion-router-outlet`** vs Angular's `router-outlet` (it's what enables the stack + transitions), and `NavController` `forward`/`back`/`root` **navigation direction**.

## 4.3 Navigation

- **Angular Router is the primary mechanism** — `routerLink`, `router.navigate`, guards, lazy loading all work as normal.
- **`NavController`** for direction-aware navigation: `navigateForward` (push animation), `navigateBack`, `navigateRoot` (reset the stack — use after login/logout).
- **Modals / popovers / alerts / action sheets / toasts** are *not* routes — they're controller-driven overlays:
```ts
const modal = await this.modalCtrl.create({ component: VisitFormPage, componentProps: { id } });
await modal.present();
const { data, role } = await modal.onWillDismiss();      // get data back
```
- **Tabs** have their own nested outlets and their **own navigation stack per tab**.
- **Hardware back button (Android)**: `platform.backButton.subscribeWithPriority(10, () => …)` — a real requirement to handle in a form-heavy app.
- **Deep links / universal links** via Capacitor's `App.addListener('appUrlOpen')`.

## 4.4 Capacitor, native access & plugins

```ts
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';
import { Geolocation } from '@capacitor/geolocation';
import { Preferences } from '@capacitor/preferences';
import { Network } from '@capacitor/network';
import { Capacitor } from '@capacitor/core';

const photo = await Camera.getPhoto({
  quality: 70, resultType: CameraResultType.Uri, source: CameraSource.Camera,
});

if (Capacitor.isNativePlatform()) { /* device-only path */ }
```
**Core plugins to name:** Camera · Geolocation · Filesystem · Preferences · Network · Push Notifications · Local Notifications · Device · App · Haptics · Share · Splash Screen · Status Bar · Browser · Clipboard · Barcode scanning (community).

**The CLI flow — know it cold:**
```bash
ionic build                 # or: ng build
npx cap add ios android     # once
npx cap sync                # copy web assets + install native deps  (copy + update)
npx cap open ios            # Xcode
npx cap open android        # Android Studio
npx cap run android -l --external   # live reload on a device
```
> ⚠️ **`npx cap sync` after every web build and every plugin install** — "I changed the code but the app didn't update" is almost always a missed sync.

**Writing a custom plugin** (mention it if they push): implement the Swift/Kotlin side, register it, and call it through a typed TS interface — needed when a device (a clinical instrument, a barcode reader, an SDK) has no community plugin.

## 4.5 Permissions

```ts
const status = await Geolocation.checkPermissions();
if (status.location !== 'granted') {
  const req = await Geolocation.requestPermissions();
  if (req.location !== 'granted') { this.showRationale(); return; }
}
```
- Declare them: **`AndroidManifest.xml`** (`<uses-permission>`) and **`Info.plist`** (`NSCameraUsageDescription` etc. — **iOS rejects the build/crashes without a usage string**).
- **Android 6+ requests at runtime**; iOS prompts once — **if the user denies, you cannot re-prompt**, you must deep-link them to Settings.
- **Ask in context, not at launch** — request the camera when they tap "add photo", with a rationale screen. That's a UX answer inside a technical question, and leads are expected to give it.
- Android 13+ split notification and media permissions; scoped storage changed file access.

## 4.6 Storage — pick deliberately, this matters for healthcare

| Option | Use | Notes |
|---|---|---|
| **`@capacitor/preferences`** | Small key/value (settings, flags) | Native `UserDefaults`/`SharedPreferences`. **Not encrypted** |
| **SQLite** (`@capacitor-community/sqlite`) | ⭐ **Structured offline data — the clinical-trial answer** | Real queries, large datasets, **SQLCipher encryption** available |
| **IndexedDB / Ionic Storage** | Larger web-friendly structured data | Ionic Storage picks the best available driver |
| **Filesystem** | Photos, documents, exports | `Directory.Data` (private) vs `Cache` |
| **Secure storage / Keychain / Keystore** | ⭐ **Tokens, credentials, PHI** | The *only* right place for a refresh token on device |
| ❌ `localStorage` | Avoid for anything sensitive or important | Can be cleared by the OS; not encrypted; synchronous |

> 🎯 **The IQVIA-shaped answer:** "For an app capturing patient data offline I'd use **SQLite with encryption at rest** for the records, **secure storage / Keychain** for tokens, and I'd keep **nothing identifying** in `localStorage` or in logs. On Android I'd also set `android:allowBackup="false"` so PHI doesn't end up in a cloud backup."

## 4.7 Network handling & offline

```ts
Network.addListener('networkStatusChange', s => this.online.set(s.connected));
const status = await Network.getStatus();     // { connected, connectionType }
```
**The offline-first pattern — describe it as a design, not an API:**
1. **Write locally first** (SQLite) with a `pending` flag → the UI is instant and works with no signal.
2. **A sync queue** processes pending records when connectivity returns — `concatMap` so order is preserved, exponential backoff on failure.
3. **Idempotency:** every record carries a **client-generated UUID** so a retry can't create duplicates. ⭐ *This is the detail that proves you've really built offline sync.*
4. **Conflict resolution:** last-write-wins vs server-wins vs a merge/flag-for-review — **in a clinical system you flag for review rather than silently overwrite**.
5. **UI honesty:** an offline banner, per-record sync status, and never a spinner that hangs forever — every request gets a timeout.

## 4.8 Mobile performance

- **Virtual scroll** (`ion-virtual-scroll` / CDK) for long lists; `trackBy`/`track`.
- **`OnPush` + signals** — a mid-range Android tablet has a fraction of a laptop's CPU.
- **Images:** resize/compress *before* upload (`quality: 70`), lazy-load, cache.
- **Startup time:** lazy routes, small initial bundle, hide the splash screen only when the first view is ready, avoid heavy work in `APP_INITIALIZER`.
- **Avoid layout thrash / heavy shadows & blur** in scrolling lists; prefer CSS transforms (GPU) for animation.
- **Debounce inputs**, batch DB writes in a transaction.
- **Test on a real low-end device**, not just the simulator — and profile with Chrome DevTools remote debugging (`chrome://inspect`) for Android, Safari Web Inspector for iOS.
- **Battery/network:** don't poll; use push or a backoff; respect low-power mode.

🔗 Deeper: **[15 — Ionic Level 1](./15-ionic-level1.md)** · **[16 — Codeboard round](./16-codeboard-technology-level1.md)**

---

# Part 5 — REST + Security

*Their list: HTTP methods/status codes, authN vs authZ, JWT/access/refresh, interceptors, expired-token handling, CORS, API error handling.*

## 5.1 HTTP methods

| Method | Safe? | Idempotent? | Notes |
|---|---|---|---|
| `GET` | ✅ | ✅ | Never changes state; cacheable |
| `POST` | ❌ | ❌ | Create / non-idempotent action |
| `PUT` | ❌ | ✅ | **Full replace** — send the whole resource |
| `PATCH` | ❌ | ❌* | **Partial update** |
| `DELETE` | ❌ | ✅ | Deleting twice = same end state (2nd returns 404/204) |

> **Idempotent** = the same request repeated leaves the same *state* (not the same response). It's what makes **retries safe** — which links straight to Part 7's circuit breakers. `PUT` vs `PATCH` was asked at Mphasis.

## 5.2 Status codes

| Code | Meaning |
|---|---|
| **200 / 201 / 202 / 204** | OK / Created (+`Location`) / Accepted (async) / No Content |
| **301 / 302 / 304** | Moved permanently / Found / **Not Modified** (ETag caching) |
| **400** | Bad request — malformed/validation |
| **401 Unauthorized** | ⭐ Actually *unauthenticated* — **who are you?** → log in / refresh |
| **403 Forbidden** | Authenticated but **not allowed** — don't retry, don't log out |
| **404 / 409 / 410** | Not found / Conflict (version, duplicate) / Gone |
| **422** | Unprocessable entity — semantic validation failure |
| **429** | Too many requests — respect `Retry-After` |
| **500 / 502 / 503 / 504** | Server error / Bad gateway / **Unavailable** / **Gateway timeout** |

> 🎯 **The distinction they check: 401 vs 403.** 401 → the client should authenticate (refresh the token). 403 → authenticated fine, just not permitted (show "no access", **don't** trigger a token refresh loop). Getting that wrong causes a real, common bug.

## 5.3 Authentication vs authorization

> "**Authentication is *who are you*; authorization is *what may you do*.** AuthN comes first and produces an identity; authZ is a decision about that identity — a role, a permission, an ownership check."

- **AuthN:** username/password, SSO/**SAML**, **OAuth2 / OIDC**, MFA, biometrics on mobile.
- **AuthZ:** **RBAC** (roles) vs **ABAC** (attributes — *"this investigator may only see patients at their own site"*, which is exactly the healthcare pattern), scopes/claims.
- ⚠️ **Frontend authorization is UX only.** Hiding a button is not security — **the API must enforce it**. Say this; leads are expected to.

## 5.4 JWT, access & refresh tokens

**Structure:** `header.payload.signature`, base64url-encoded, **signed not encrypted → anyone can read the payload. Never put PHI or secrets in a JWT.**
**Claims:** `iss`, `sub`, `aud`, `exp`, `iat`, `nbf`, `jti` + your roles/scopes.

| | Access token | Refresh token |
|---|---|---|
| Lifetime | Short — 5–15 min | Long — hours/days |
| Sent | On **every** API call (`Authorization: Bearer …`) | Only to the token endpoint |
| Stored | In memory (safest) | ⭐ **httpOnly, Secure, SameSite cookie** |
| Purpose | Access resources | Get a new access token silently |

> ⚠️ **`localStorage` is readable by any XSS.** The pragmatic position to state: *"Refresh token in an httpOnly cookie, access token in memory. If a project insists on `localStorage`, then a short expiry, a strict CSP and rigorous output escaping are non-negotiable compensating controls."* Also: **refresh-token rotation** + reuse detection, and a **`jti` denylist** for logout, because a stateless JWT can't otherwise be revoked. That last point — *"JWTs can't be revoked, which is the real trade-off against sessions"* — is the mature answer.

## 5.5 Expired-token handling (they listed this explicitly)

The full flow, which is also a great whiteboard answer:

```ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService), router = inject(Router), toast = inject(ToastService);

  return next(withToken(req, auth.accessToken)).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status !== 401 || req.url.includes('/refresh')) return throwError(() => err);

      return auth.refresh().pipe(                       // 1. try a silent refresh
        switchMap(t => next(withToken(req, t))),        // 2. replay the original request
        catchError(() => {                              // 3. refresh failed → really expired
          auth.logout();
          toast.warn('Your session has expired. Please sign in again.');
          router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
          return EMPTY;
        }));
    }));
};
```
**Four layers to describe:**
1. **Proactive** — decode `exp` and warn *before* it expires: *"Your session expires in 60 seconds — [Stay signed in]"*. In a clinical form, losing 20 minutes of typed data to a silent logout is a serious defect.
2. **Reactive** — the 401 interceptor above.
3. **Route guard** — an expired user never reaches a protected page.
4. **UX** — toast + `returnUrl` redirect + **preserve unsaved work** (draft in the store / SQLite) + clear the stale token.

> ⚠️ **The refresh stampede:** five parallel 401s must not fire five refreshes. Gate it with `isRefreshing` + a `BehaviorSubject<string|null>`; the rest wait with `filter(t => !!t), take(1), switchMap(replay)`. **Volunteer this — it's the follow-up.**

## 5.6 CORS

**What it is:** a **browser** security mechanism. The **same-origin policy** blocks JS from reading a response from a different origin (scheme + host + port) unless the **server** opts in with `Access-Control-Allow-*` headers.

- **Preflight:** for anything beyond a "simple" request (custom headers like `Authorization`, `PUT`/`PATCH`/`DELETE`, non-form content types), the browser first sends an **`OPTIONS`** request. The server must answer with `Allow-Origin`, `Allow-Methods`, `Allow-Headers`, `Max-Age`.
- **Credentials:** `withCredentials: true` requires `Access-Control-Allow-Credentials: true` and a **specific origin — `*` is rejected**. That combination is the most common CORS bug.
- **Fixes, ranked:** configure it on the **server/gateway** (correct) → a **dev proxy** (`proxy.conf.json` in the Angular CLI) → `@CrossOrigin` per controller. ❌ Never "allow all origins" in production.
- 🗣️ **The line that shows you understand it:** *"CORS is enforced by the browser, not the server — Postman doesn't hit it, which is why the classic report is 'it works in Postman but not in the app'. And it isn't a security control on the API: the API still has to authenticate and authorize every request."*
- **On mobile:** a Capacitor app runs from `capacitor://localhost` / `https://localhost`, so it's cross-origin too — configure the server, or use `CapacitorHttp` which bypasses the WebView.

## 5.7 API error handling

**On the client:**
```ts
export const errorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(catchError((e: HttpErrorResponse) => {
    const msg =
      e.status === 0   ? 'Network unavailable — your work is saved locally'
    : e.status === 400 ? extractValidation(e)
    : e.status === 403 ? 'You do not have access to this study'
    : e.status === 429 ? 'Too many requests, retrying shortly'
    : e.status >= 500  ? 'Something went wrong on our side'
    :                    'Unexpected error';
    inject(ToastService).error(msg);
    inject(LoggerService).log({ url: req.url, status: e.status, correlationId: req.headers.get('X-Correlation-Id') });
    return throwError(() => e);
  }));
```
> ⚠️ **`status === 0` means the request never reached the server** (offline, DNS, CORS, cert) — distinguish it, because the user-facing message and the retry behaviour are completely different.

**On the server:** `@RestControllerAdvice` + `@ExceptionHandler` returning **`ProblemDetail` (RFC 7807)** with a stable machine-readable `type`/error code, a human `detail`, and a **correlation/trace ID**. Never leak stack traces or SQL to the client.

**Also name:** input validation on **both** sides, output escaping (XSS), parameterised queries (SQL injection), **rate limiting**, HTTPS/HSTS everywhere, **CSP** headers, secrets in a vault not in `environment.ts` (⚠️ **anything in an Angular bundle is public**), dependency scanning (`npm audit`, Snyk, OWASP Dependency-Check), and the **OWASP Top 10** as the checklist you review against.

🔗 Deeper: **[06 — Spring Boot](./06-spring-boot.md)** · **[37 Q17–18](./37-altimetrik-fullstack-java-angular-31aug.md)**

---

# Part 6 — MongoDB + PostgreSQL / SQL

*Their list: CRUD, indexes, aggregation, embedding vs referencing, joins, WHERE vs HAVING, GROUP BY, subqueries, transactions/ACID, pagination, 2nd/Nth highest salary, highest salary by department.*

> 🔴 **SQL has now appeared in Mphasis, Deloitte's rubric and this list. It is no longer optional.** The complete file is **[36 — SQL](./36-sql-interview-questions.md)** — this Part is the condensed, exam-ready version.

## 6.1 SQL — WHERE vs HAVING & GROUP BY

**Logical execution order — memorise it; it explains half the traps:**
```
FROM → JOIN → WHERE → GROUP BY → HAVING → SELECT → DISTINCT → ORDER BY → LIMIT
```

| | `WHERE` | `HAVING` |
|---|---|---|
| Filters | **Individual rows** | **Groups** |
| Runs | **Before** `GROUP BY` | **After** `GROUP BY` |
| Aggregates | ❌ Can't use them | ✅ That's the point |
| Column aliases from SELECT | ❌ (SELECT hasn't run yet) | ❌ in standard SQL (MySQL is lenient) |

```sql
SELECT department, COUNT(*) AS headcount, AVG(salary) AS avg_salary
FROM   employee
WHERE  active = true            -- row filter, BEFORE grouping
GROUP  BY department
HAVING COUNT(*) > 5             -- group filter, AFTER grouping
ORDER  BY avg_salary DESC;
```
> 🎯 **The performance sentence:** "Put a condition in `WHERE` whenever you can — it removes rows *before* grouping, so there's less to aggregate. `HAVING` is only for conditions on the aggregate itself."

**Aggregates + NULL:** `COUNT(*)` counts rows including NULLs; `COUNT(col)` **skips NULLs**; `AVG`/`SUM` **ignore NULLs** (so `AVG` ≠ `SUM/COUNT(*)` when NULLs exist). Every non-aggregated `SELECT` column must be in `GROUP BY` (Postgres enforces it strictly).

## 6.2 Joins

| Join | Returns |
|---|---|
| `INNER` | Only matching rows on both sides |
| `LEFT` | All left rows + matches (NULLs where none) |
| `RIGHT` | Mirror of LEFT |
| `FULL OUTER` | Everything from both sides |
| `CROSS` | Cartesian product |
| Self join | A table joined to itself (employee → manager) |

```sql
-- departments and their headcount, INCLUDING empty departments
SELECT d.name, COUNT(e.id) AS headcount        -- ⭐ COUNT(e.id), not COUNT(*)
FROM   department d
LEFT   JOIN employee e ON e.dept_id = d.id
GROUP  BY d.name;
```
> ⚠️ **Two traps that get asked:** (1) `COUNT(*)` on a LEFT JOIN returns **1** for an empty department, not 0 — count the joined column. (2) A condition on the right table belongs in **`ON`**, not `WHERE` — putting it in `WHERE` turns the outer join back into an inner join.

**Anti-join:** `LEFT JOIN … WHERE b.id IS NULL`, or `NOT EXISTS`. ⚠️ **`NOT IN` with a NULL in the subquery returns *no rows*** — three-valued logic. That's a favourite.

## 6.3 The salary questions — they named these explicitly

**Second / Nth highest salary — know at least three ways and their trade-offs:**
```sql
-- 1. Window function — best, handles ties explicitly
SELECT DISTINCT salary FROM (
  SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk FROM employee
) t WHERE rnk = 2;

-- 2. Correlated subquery — portable, no window functions needed
SELECT MAX(salary) FROM employee
WHERE salary < (SELECT MAX(salary) FROM employee);

-- 3. LIMIT / OFFSET — simple, but N-1 offset and ties are wrong
SELECT DISTINCT salary FROM employee ORDER BY salary DESC LIMIT 1 OFFSET 1;

-- 4. NOT IN
SELECT MAX(salary) FROM employee WHERE salary NOT IN (SELECT MAX(salary) FROM employee);
```
> 🎯 **The differentiator: `RANK` vs `DENSE_RANK` vs `ROW_NUMBER`.** With salaries 100, 100, 90: `ROW_NUMBER` → 1,2,3; `RANK` → 1,1,3 (gap); `DENSE_RANK` → 1,1,2 (no gap). **"Second highest" means `DENSE_RANK = 2` if you mean the second distinct salary, `ROW_NUMBER = 2` if you mean the second person.** Ask which they mean — that question *is* the senior answer.

**Highest salary per department — asked 3 times already, and it's on their list:**
```sql
SELECT department, name, salary FROM (
  SELECT department, name, salary,
         RANK() OVER (PARTITION BY department ORDER BY salary DESC) AS rnk
  FROM employee
) t WHERE rnk = 1;                       -- RANK keeps ties; ROW_NUMBER picks exactly one
```
```sql
-- the pre-window-function version
SELECT e.department, e.name, e.salary
FROM   employee e
JOIN  (SELECT department, MAX(salary) AS mx FROM employee GROUP BY department) m
  ON   e.department = m.department AND e.salary = m.mx;
```
**And the Java Streams version** (they asked this at Altimetrik today — have both ready):
```java
Map<String, String> top = employees.stream().collect(groupingBy(Employee::getDepartment,
    collectingAndThen(maxBy(comparingDouble(Employee::getSalary)),
                      e -> e.map(Employee::getName).orElse("N/A"))));
```

**Employees earning above their department's average** — the other classic:
```sql
SELECT e.* FROM employee e
WHERE e.salary > (SELECT AVG(salary) FROM employee x WHERE x.department = e.department);
```

## 6.4 Subqueries

- **Scalar** (returns one value) · **row** · **table** (in `FROM`, a derived table) · **correlated** (references the outer query — runs per outer row, so it's the slow one).
- `IN` vs `EXISTS` vs `JOIN`: `EXISTS` short-circuits on the first match and is usually best for "does a related row exist"; `JOIN` when you need columns from the other table; `IN` is fine for a small static list — but watch the **`NOT IN` + NULL** bomb.
- **CTEs** (`WITH x AS (…)`) for readability, and **recursive CTEs** for hierarchies (employee → manager chain, or a study → site → subject tree).

## 6.5 Indexes

**What:** usually a **B-tree** giving O(log n) lookup instead of a full scan.
**Create:** `CREATE INDEX idx_emp_dept_salary ON employee(dept_id, salary DESC);`

| Concept | Point to make |
|---|---|
| **Composite index & leftmost prefix** | `(a, b, c)` serves `a`, `a,b`, `a,b,c` — **not `b` alone**. Column order matters |
| **Covering index** | The index contains every column the query needs → **index-only scan**, no table lookup (Postgres: `INCLUDE`) |
| **Sargability** | `WHERE YEAR(created) = 2026` can't use the index; `WHERE created >= '2026-01-01' AND created < '2027-01-01'` can. Same for a leading `LIKE '%x'` |
| **Selectivity** | An index on a boolean/gender column is usually useless |
| **Cost** | Indexes **slow down writes** and consume storage — every insert maintains every index |
| **Postgres extras** | Partial (`WHERE active`), expression, **GIN** for JSONB/full-text, BRIN for huge append-only tables |
| **Read the plan** | `EXPLAIN ANALYZE` — Seq Scan vs Index Scan, the row estimate vs actual, and where the time really goes |

> 🗣️ **"Make this query faster" is really: `EXPLAIN ANALYZE` first, then index/rewrite, then re-measure.** Never guess.

## 6.6 Transactions & ACID

**ACID:** **Atomicity** (all or nothing) · **Consistency** (constraints hold before and after) · **Isolation** (concurrent transactions don't corrupt each other) · **Durability** (committed = survives a crash).

**Isolation levels & the anomalies they prevent:**
| Level | Dirty read | Non-repeatable read | Phantom |
|---|---|---|---|
| Read Uncommitted | ✅ possible | ✅ | ✅ |
| **Read Committed** (Postgres default) | ❌ | ✅ | ✅ |
| **Repeatable Read** (MySQL InnoDB default) | ❌ | ❌ | ✅* |
| Serializable | ❌ | ❌ | ❌ |

**In Spring:** `@Transactional` — and the three traps worth naming: **self-invocation bypasses the proxy**; it only rolls back on **unchecked** exceptions by default (`rollbackFor = Exception.class` for checked); and `readOnly = true` is a real optimisation hint. **Optimistic locking** (`@Version`) vs **pessimistic** (`SELECT … FOR UPDATE`) — in a clinical system, optimistic with a "this record changed, review the conflict" prompt is usually right.

## 6.7 Pagination (SQL side)

```sql
SELECT * FROM employee ORDER BY salary DESC, id DESC LIMIT 20 OFFSET 40;   -- offset paging
SELECT * FROM employee WHERE (salary, id) < (:lastSalary, :lastId)         -- ⭐ keyset paging
ORDER BY salary DESC, id DESC LIMIT 20;
```
> ⚠️ **Always add a unique tiebreaker to `ORDER BY`** or rows repeat/vanish between pages. **OFFSET degrades linearly** — at page 50,000 the DB still walks every skipped row, so deep paging / infinite scroll wants **keyset**. In Spring Data: `Page` (2 queries incl. `COUNT`) vs `Slice` (1 query) — see **[37 Q9–10](./37-altimetrik-fullstack-java-angular-31aug.md)**.

## 6.8 MongoDB

**CRUD:**
```js
db.patients.insertOne({ _id, name, siteId, visits: [] });
db.patients.find({ siteId: 'S1', age: { $gte: 18 } }).sort({ name: 1 }).skip(20).limit(10);
db.patients.updateOne({ _id }, { $set: { status: 'active' }, $push: { visits: visit } });
db.patients.deleteOne({ _id });
```
Operators to know: `$eq $ne $gt $gte $in $nin $and $or $exists $regex` · update: `$set $unset $inc $push $pull $addToSet` · `upsert: true`.

**Aggregation pipeline** — the equivalent of GROUP BY, and they will ask for it:
```js
db.employees.aggregate([
  { $match:  { active: true } },                                  // ⭐ filter FIRST (uses indexes)
  { $group:  { _id: '$department', maxSalary: { $max: '$salary' },
               avgSalary: { $avg: '$salary' }, count: { $sum: 1 } } },
  { $sort:   { maxSalary: -1 } },
  { $project:{ _id: 0, department: '$_id', maxSalary: 1, count: 1 } },
  { $limit:  10 },
]);
```
Stages: `$match $group $project $sort $limit $skip $unwind $lookup` (**the closest thing to a join**) `$facet $addFields $count $out`.
> 🎯 **Say this:** "`$match` and `$sort` go as early as possible so the pipeline can use indexes — once you've `$group`ed, the index is gone."

**Embedding vs referencing — a genuine design question, answer it as trade-offs:**

| | **Embed** (subdocuments) | **Reference** (store an ObjectId) |
|---|---|---|
| Read | ✅ One query, no join | Needs `$lookup` or a second query |
| Write | Whole document rewritten | Update independently |
| Use when | 1-to-few · data read together · doesn't grow unbounded · not shared | 1-to-many/many-to-many · unbounded growth · shared across documents · updated independently |
| Example | A patient's address, a visit's vitals | Patient → Study; Study → Sites |

> ⚠️ **The constraint that decides it: the 16 MB document limit** and unbounded arrays. *"I'd embed the vitals inside a visit because they're always read together and bounded, but reference the study, because thousands of patients share it and it's updated independently."*

**Also know:** indexes (single, compound — **same leftmost-prefix rule**, text, TTL for expiring data), the **`_id`/ObjectId**, schema-on-read + **schema validation rules**, replica sets (HA) vs sharding (horizontal scale), and **multi-document ACID transactions since 4.0** (so "Mongo has no transactions" is now a wrong answer).

**SQL vs Mongo — the answer a lead gives:** "It's not about which is better, it's about the access pattern. Postgres for relational integrity, joins across many entities, and strong constraints — which is most clinical data. Mongo for flexible/evolving documents read as a unit, e.g. per-study forms whose shape differs. And Postgres `JSONB` covers a lot of the middle ground, so 'we need flexible fields' isn't automatically a reason to leave the relational database."

🔗 Deeper: **[36 — SQL (complete)](./36-sql-interview-questions.md)** · **[13 — MongoDB](./13-mongodb.md)** · **[24 — ORM/JPA](./24-orm-jpa-hibernate.md)**

---

# Part 7 — Microservices

*Their list: REST vs messaging, service discovery, API gateway, timeout/retry/circuit breaker, Resilience4j, Kafka basics, network failures, Saga, distributed logging/tracing, database-per-service.*

> These are the **exact** questions Altimetrik asked today (Q11–13). You now have the answers — this is a re-run you can win. Full versions in **[37 Part 3](./37-altimetrik-fullstack-java-angular-31aug.md)**.

## 7.1 REST vs messaging

| | Synchronous REST | Asynchronous messaging |
|---|---|---|
| Coupling | Caller knows the callee, waits | Producer doesn't know consumers |
| Failure | Callee down → caller fails/blocks | Broker buffers; consumer catches up |
| Consistency | Immediate | **Eventual** |
| Complexity | Low | Ordering, idempotency, duplicates are yours |
| Use when | You need the answer to serve **this** request | The other service just needs to **eventually** know |

Sync options: `RestClient` (Spring 6.1+) / `WebClient` (reactive) / **OpenFeign** (declarative) / gRPC. ⚠️ **`RestTemplate` is in maintenance mode** — don't name it as your default.

## 7.2 Service discovery & API gateway

- **Discovery:** services register with **Eureka / Consul** (or Kubernetes DNS + Services) so you call `http://patient-service` instead of an IP. Client-side (Ribbon/Spring Cloud LoadBalancer) vs server-side (a load balancer) discovery.
- **API Gateway** (Spring Cloud Gateway, Kong, AWS API Gateway): **one entry point** — routing, authN/authZ, rate limiting, TLS termination, request aggregation, CORS, logging. Keeps cross-cutting concerns out of every service.
- **BFF (Backend for Frontend)** — worth naming for a mobile-heavy role: a gateway shaped for the mobile client so the tablet makes one call instead of six on a bad clinic connection.

## 7.3 Timeout / retry / circuit breaker / Resilience4j

**The failure they're testing: a cascading failure.** A is slow → B's threads all block on A → B dies → B's callers die. **Slow is worse than down**, because down at least fails fast.

```java
@CircuitBreaker(name = "patientSvc", fallbackMethod = "fallback")
@Retry(name = "patientSvc")
@TimeLimiter(name = "patientSvc")
@Bulkhead(name = "patientSvc")
public PatientDto get(String id) { return client.get(id); }

private PatientDto fallback(String id, Throwable t) {
    return cache.get(id).orElse(PatientDto.unavailable(id));    // degrade, don't die
}
```
```yaml
resilience4j.circuitbreaker.instances.patientSvc:
  slidingWindowSize: 20
  failureRateThreshold: 50
  waitDurationInOpenState: 10s
  permittedNumberOfCallsInHalfOpenState: 3
```
**Circuit breaker states: CLOSED → (failure rate exceeded) → OPEN → (wait) → HALF_OPEN → CLOSED or back to OPEN.**

**Resilience4j modules — name all six:** CircuitBreaker · Retry · **RateLimiter** · **Bulkhead** (isolate thread pools so one slow dependency can't consume them all) · TimeLimiter · Cache. *(It replaced Hystrix, which is retired.)*

> ⚠️ **Retries must be bounded, backed off with jitter, and only on idempotent operations** — naive retries make a cascade worse by adding load to a service that's already failing.

## 7.4 Kafka basics

**Model:** producers write to a **topic**, which is split into **partitions**; each partition is an **ordered, append-only log**. Consumers join a **consumer group**; each partition is read by exactly **one consumer in the group**, which is how you scale. **Offsets** track position, so a consumer can replay.

| Concept | Point |
|---|---|
| **Partition key** | Same key → same partition → **ordering guaranteed per key** (e.g. per patient), not globally |
| **Delivery** | At-least-once by default → **consumers must be idempotent** (dedupe on event ID) |
| **Retention** | Time/size based — Kafka keeps messages after consumption, so you can replay |
| **Consumer group** | Adding consumers beyond the partition count adds nothing |
| **DLQ** | Poison messages go to a dead-letter topic instead of blocking the partition |
| Kafka vs RabbitMQ | Kafka = a durable, replayable log for high-throughput event streaming; Rabbit = a broker for routing/work queues with per-message ack |

```java
@KafkaListener(topics = "visit-recorded", groupId = "reporting")
public void on(VisitRecorded e) { … }        // must be idempotent
```

## 7.5 Network failures — the sharpest question, asked today

**The core insight to state:** a caller **cannot tell** "the request never arrived" from "it was processed but the response was lost" from "it genuinely failed". Everything follows from that:

| Need | Answer |
|---|---|
| Detect | Connect + read **timeouts** — never infinite |
| Recover | Retry with **backoff + jitter** |
| Make retries safe | ⭐ **Idempotency key** — the caller sends a UUID; the server returns the original result on a repeat |
| Stop the bleeding | **Circuit breaker** + fallback |
| Don't lose the work | ⭐ **Transactional outbox** |
| Poison messages | **DLQ** |
| Undo what can't complete | **Compensating transaction (Saga)** |
| See it | Correlation/trace ID, breaker-state metrics, alerts |

**Transactional outbox — the answer that wins the question:**
> "You can't atomically commit a DB transaction *and* publish to a broker. So I write the event into an **`outbox` table in the same local transaction** as the business change; a relay (a poller or CDC via Debezium) publishes it and retries until the broker accepts. Atomicity comes from one local transaction, and delivery is at-least-once — so the consumer must be idempotent."

## 7.6 Saga

**Why:** there's no two-phase commit across services, so a business transaction spanning services is a **sequence of local transactions, each with a compensating action**.

| Style | How | Trade-off |
|---|---|---|
| **Choreography** | Services react to each other's events | Decoupled, but the flow is implicit and hard to follow |
| **Orchestration** | A coordinator drives each step | Explicit and debuggable, but the orchestrator is a new component to own |

> **Give a concrete compensation, not a definition** — that's the follow-up that catches people: *"Enrol subject → reserve a kit → schedule the first visit. If scheduling fails, the compensation is 'release the kit reservation' and 'mark the enrolment pending' — not a rollback, because the kit reservation was already committed in another service's database."*

## 7.7 Distributed logging & tracing

- **Correlation/trace ID** generated at the gateway, propagated on every hop (`traceparent`/W3C Trace Context), and logged in **every** log line — via MDC in Spring, and an Angular interceptor that sends `X-Correlation-Id` so a **user-reported bug can be traced from the tap to the SQL query**.
- **Micrometer Tracing (formerly Sleuth) + Zipkin/Jaeger** for traces; **ELK / OpenSearch / Splunk / CloudWatch** for centralised logs; **Prometheus + Grafana** for metrics; **Actuator** for `/health`, `/metrics`, `/info`.
- **Structured (JSON) logs** — greppable and queryable, not free text.
- ⚠️ **Healthcare: never log PHI.** Log identifiers and correlation IDs, not names, dates of birth or diagnoses. **Say this at IQVIA.**
- **The three pillars:** logs (what happened) · metrics (how much/how fast) · traces (where the time went).

## 7.8 Database per service

**The rule:** each service **owns its data**; no other service touches its tables. That's what makes independent deployment and independent scaling real.

**Consequences to name (this is the whole answer):**
- ❌ **No cross-service joins** → **API composition** (call both and join in code) or **CQRS** (a read model built from events).
- ❌ **No distributed transactions** → **Saga**.
- ⚠️ **Data duplication is expected and fine** — the order service keeps a copy of the customer name it needs; it's eventually consistent.
- ✅ Each service can pick the right store (Postgres here, Mongo there).
- **The pragmatic caveat a lead adds:** *"A shared database is the classic distributed monolith — but I've also seen teams split too early. I'd start with a modular monolith and clean module boundaries, and extract a service when there's a real reason: independent scaling, independent release cadence, or a separate team."* That answer signals judgement, which is what a lead interview is for.

🔗 Deeper: **[08 — Microservices](./08-microservices-basics.md)** · **[37 Q11–13](./37-altimetrik-fullstack-java-angular-31aug.md)** · **[30 — Mphasis L2](./30-mphasis-level2-client-round.md)**

---

# Part 8 — Git + CI/CD + Cloud

*Their list: merge/rebase, branching/conflicts, PR/code review, CI/CD basics, AWS fundamentals, deployment, logs, environment configuration.*

## 8.1 Merge vs rebase

| | `merge` | `rebase` |
|---|---|---|
| History | Preserves it; adds a merge commit | **Rewrites** — replays your commits on top |
| Result | True but noisy graph | Linear, readable |
| Safety | Always safe | ⚠️ **Never rebase a branch others have pulled** |

```bash
git checkout feature && git rebase main        # replay my work on top of latest main
git rebase --continue / --abort
git merge --no-ff feature                      # keep the feature grouping visible
git pull --rebase                              # avoid pointless merge commits on pull
git cherry-pick <sha>                          # one commit onto another branch
git revert <sha>                               # ✅ undo on a shared branch (new commit)
git reset --hard <sha>                         # ⚠️ local only — rewrites history
git stash / git stash pop
git log --oneline --graph --all
git bisect start                               # binary-search for the commit that broke it
```
> 🗣️ **The team policy answer (what a lead is really being asked):** "**Rebase locally to keep my own branch clean, merge to integrate.** The rule I set is: never rebase anything that's been pushed and shared, and use `--force-with-lease` rather than `--force` if I must update my own PR branch. On main we squash-merge so each PR is one revertable commit, with the ticket ID in the message."

## 8.2 Branching & conflict resolution

- **Strategies:** **GitHub Flow** (main + short-lived feature branches + PR — my default) · **Git Flow** (develop/release/hotfix — heavier, but fits **regulated release trains**, which may well be IQVIA's world) · **trunk-based** with feature flags.
- **Protection:** no direct pushes to main, required reviews, required green CI, linear history, signed commits.
- **Conflicts:** they happen where two branches changed the same lines. `git status` → open the `<<<<<<< / ======= / >>>>>>>` markers → **understand both sides** (`git log -p`, ask the other author) → resolve → `git add` → `git rebase --continue`. Use `git mergetool`, `git diff --ours/--theirs`, and **rerere** for repeats.
- **How you *prevent* them:** small PRs, short-lived branches, rebase/pull often, agree file ownership, and don't let a branch live for three weeks. **That prevention answer is the lead-level part.**

## 8.3 PR & code review process

**What I'd put in place (say it as a process, with a why):**
1. **Small PRs** — under ~400 lines. Review quality falls off a cliff beyond that.
2. **A PR template**: what changed, why, how it was tested, screenshots, ticket link, risk/rollback.
3. **CI gates before a human looks**: lint, format, unit tests + coverage threshold, build, bundle budget, dependency/security scan.
4. **At least one approval**; a second for security-, data- or migration-touching changes.
5. **Review for**: correctness · edge cases and error paths · tests · readability/naming · security (authZ, injection, PHI in logs) · performance · public API design. **Formatting is the linter's job, not the reviewer's.**
6. **Comment style:** ask questions rather than issue commands; label **blocking** vs **nit**; approve with minor comments instead of blocking on taste.
7. **SLA:** review within one working day, so PRs don't rot and conflict.
8. **Definition of done:** merged, deployed to a test environment, ticket updated.

## 8.4 CI/CD

**Pipeline stages:**
```
commit → lint + unit tests → build → SAST/dependency scan → package (Docker) →
deploy to DEV → integration/e2e tests → deploy to QA (approval) → UAT →
deploy to PROD (approval) → smoke tests → monitor
```
**Concretely for your stack:** `npm ci` → `ng lint` → `ng test --watch=false --code-coverage` → `ng build --configuration=production` → `mvn verify` → Docker image tagged with the commit SHA → push to a registry → deploy.

| Term | Meaning |
|---|---|
| **CI** | Every commit is merged and verified automatically |
| **Continuous Delivery** | Every green build is *releasable*; deployment is a button |
| **Continuous Deployment** | Every green build goes to prod automatically |
| **Blue/green** | Two environments, switch traffic — instant rollback |
| **Canary** | 5% of traffic first, watch the metrics, then ramp |
| **Rolling** | Replace instances gradually (the Kubernetes default) |
| **Feature flag** | Deploy the code dark, enable it separately — decouples deploy from release |

**Tools:** GitHub Actions / GitLab CI / Jenkins / Azure DevOps · Docker · Kubernetes/ECS · Terraform · SonarQube · Snyk/Dependabot.
> ⚠️ **Regulated-industry nuance worth raising at IQVIA:** "In a validated (GxP) system, continuous deployment straight to production usually isn't allowed — releases are controlled and documented. So I'd aim for **continuous delivery with an approval gate**, full traceability from ticket → commit → build → deployed artefact, and automated evidence capture rather than manual sign-off screenshots." Very few candidates will say this.

## 8.5 AWS fundamentals

| Service | One line |
|---|---|
| **EC2** | Virtual machines |
| **S3** | Object storage — static Angular builds, documents, exports; versioning + lifecycle + SSE-KMS encryption |
| **CloudFront** | CDN in front of S3 — ⭐ the standard way to host an Angular SPA |
| **RDS / Aurora** | Managed Postgres/MySQL — backups, Multi-AZ, read replicas |
| **DocumentDB / DynamoDB** | Managed Mongo-compatible / managed NoSQL key-value |
| **Lambda** | Serverless functions — event-driven, no servers to run |
| **API Gateway** | Managed REST entry point — throttling, auth, keys |
| **ECS / EKS / Fargate** | Containers — orchestrated / Kubernetes / serverless containers |
| **ELB (ALB/NLB)** | Load balancing + TLS termination |
| **IAM** | Users, roles, policies — **least privilege**; use roles, never long-lived keys |
| **CloudWatch** | Logs, metrics, alarms, dashboards |
| **SQS / SNS / EventBridge / MSK** | Queue / pub-sub / event bus / managed Kafka |
| **Secrets Manager / Parameter Store / KMS** | Secrets and encryption keys |
| **VPC, subnets, security groups** | Network isolation — **the DB is in a private subnet, never public** |
| **Route 53 / ACM** | DNS / TLS certificates |
| **Regions & AZs** | ⭐ **Data residency** — for GDPR, EU patient data stays in an EU region |

**A typical deployment for your stack:** Angular/Ionic build → **S3 + CloudFront** (with the SPA fallback rewriting 404 → `index.html`); Spring Boot in a Docker image → **ECS Fargate** behind an **ALB**; **RDS Postgres** in a private subnet; **Secrets Manager** for credentials; logs and alarms in **CloudWatch**.

## 8.6 Logs & environment configuration

- **Environments:** dev → QA → staging/UAT → prod, ideally identical in shape.
- **One artefact, many environments** — the build is promoted, not rebuilt. Configuration comes from **environment variables / Parameter Store / Secrets Manager / Spring profiles**, never from a rebuilt bundle.
- **Angular caveat:** `environment.ts` is compiled **into a public bundle** — ⚠️ **it is not a place for secrets**, only public endpoints. Runtime config via a fetched `config.json` is often better, because it lets one build serve every environment.
- **Spring:** `application-{profile}.yml` + `SPRING_PROFILES_ACTIVE`; env vars and CLI args override the file (relaxed binding maps `SPRING_DATASOURCE_URL` → `spring.datasource.url`).
- **Schema:** **Flyway/Liquibase** migrations in source control; **`ddl-auto: validate` in QA/prod, never `update`**.
- **Logging:** structured JSON, levels used properly (`ERROR` = someone must act), **correlation IDs**, centralised aggregation, retention policy — and **no PHI/PII/tokens in logs, ever**. Alerting on error rate and latency, not on log volume.

🔗 Deeper: **[07 — AWS Basics](./07-aws-basics.md)** · **[37 Q14](./37-altimetrik-fullstack-java-angular-31aug.md)**

---

# Part 9 — Technical Lead questions

*Their list: code reviews, mentoring, architecture decisions, sprint estimation/planning, production incidents, technical debt, disagreements, stakeholder communication, code quality.*

> 🔴 **This Part is the difference between "good senior developer" and "hire as a lead". Every answer here uses STAR: Situation → Task → Action → Result — and the *Result must have a number in it*.**
>
> ⚠️ **The blanks marked `<…>` are for you to fill from RoboGebra / Subsea / Backoffice Migration / EasyVisa.** Write your real numbers in before the interview — a specific number is what makes a story believable. **Do this today; don't improvise it in the room.**

## 9.1 The universal answer shape

Every lead question has the same skeleton:
> **1. My principle** (one sentence) → **2. How I apply it in practice** (the mechanism) → **3. A specific example from my work** (STAR + number) → **4. What I'd do differently / how I'd scale it.**

Practise saying **step 3 out loud** for each of the nine topics. That's the whole preparation.

## 9.2 Code reviews

**Principle:** "Code review is for **correctness, shared understanding and raising the floor** — not for enforcing my personal style. Anything a machine can check should be checked by a machine."

**Practice:**
- Automate style: Prettier + ESLint + Checkstyle/Spotless run in CI, so reviews are about **logic, not commas**.
- A checklist: does it do what the ticket says · edge cases and error paths · tests for the new behaviour · naming/readability · security (authZ, injection, **PHI in logs**) · performance (N+1, unbounded queries, CD churn) · public API/contract changes · migration/rollback.
- **Small PRs (<400 lines)** and a **one-working-day review SLA**.
- **Comment style:** label **[blocking]** vs **[nit]**; ask *"what happens if this is null?"* rather than *"add a null check"*; approve with nits rather than blocking on taste.
- **Praise in review too** — it's how you set the standard positively and keep juniors submitting.

> 🗣️ **STAR:** "On `<project>`, PRs were sitting for `<3–4 days>` and arriving as `<1000+>`-line changes, so review was rubber-stamping. I introduced a PR template, a 400-line guideline, and moved formatting into CI. Review turnaround dropped to `<under a day>` and we caught `<N>` real defects in the first month that would have reached QA."

## 9.3 Mentoring developers

**Principle:** "My job is to make the person independent, not to solve their ticket."

**Practice:** pair on the first task of a new area · **explain the *why*, not just the fix** · give them the whole vertical slice (API → UI → tests), not just the CSS · review their PRs with questions rather than corrections · a weekly 1:1 that isn't a status meeting · let them lead a design discussion and back them publicly · a "you own this module" assignment to build confidence.

> 🗣️ **STAR:** "A junior on `<project>` kept getting `<change-detection / subscription-leak>` bugs. Instead of fixing them in review, I ran a 30-minute session on `<OnPush and immutable updates>`, added the pattern to our team guide, and had them present it back to the team. Their PRs stopped hitting that class of bug, and in `<N>` months they owned the `<feature>` module end to end."

⚠️ Also prepare **"a mentee who wasn't improving"** — the answer is: be specific about the gap, agree a concrete goal with a timeline, follow up weekly, involve their manager early, and be honest rather than kind-in-the-short-term.

## 9.4 Architecture decisions

**Principle:** "I decide against **requirements and constraints**, not against what's fashionable — and I write the decision down."

**Practice:**
- Name the **non-functional drivers**: users, data volume, latency, offline, compliance, team skills, timeline, cost.
- **2–3 options with trade-offs**, then a recommendation. Never one option.
- **ADRs (Architecture Decision Records)** in the repo: context, options, decision, consequences. *"So in a year, the person asking 'why is it like this' has an answer — that matters more in a long-lived regulated product than anywhere else."*
- **Prototype/spike** when the risk is real; decide reversible things quickly and irreversible things carefully ("one-way vs two-way doors").
- Prefer the **boring** technology unless there's a reason.

> 🗣️ **STAR (use a real one):** "On `<project>` we had to decide between `<NgRx and a service-with-signals>` for state. I listed the drivers — `<team size, shared state across features, debuggability>` — prototyped both on one feature, and chose `<X>` because `<reason>`. I wrote it up as an ADR. `<Result: onboarding time / bug rate / delivery>`."

**Have one for each:** monolith vs microservices · NgRx vs simpler state · SQL vs NoSQL · Ionic vs native · library vs build-it-yourself.

## 9.5 Sprint estimation & planning

**Practice:** story points for **relative complexity + uncertainty** (not hours) · **planning poker** so the whole team's information surfaces · **break anything above ~8 points down** — a big number means we don't understand it yet · include testing, review, deployment and the unknowns in the estimate · track **velocity over 3+ sprints** and plan from the average, not from the best sprint · leave **~20% capacity** for support, bugs and tech debt · a clear **Definition of Ready** and **Definition of Done**.

**"How do you handle a sprint you're going to miss?"** — a favourite:
> "**Raise it as soon as I know, not at the review.** I re-forecast, present options to the PO — cut scope, extend, add help, or ship behind a flag — and let them choose. What I don't do is quietly let the team burn out or drop quality to make a date, because that just moves the cost into the next sprint."

**"An estimate you got badly wrong?"** — have one, own it, and say what you changed: *"We estimated `<the migration>` at `<2 sprints>` and it took `<4>` because `<the legacy API had undocumented behaviour>`. Since then I insist on a **timeboxed spike** before estimating anything that touches a system nobody on the team has worked in."*

## 9.6 Production incidents

**The structure — say it as a sequence, it reads as experience:**
1. **Assess impact** — who's affected, how badly, is data at risk. **In a clinical system, patient-data integrity outranks uptime.**
2. **Communicate early** — a status message to stakeholders before they ask, and a single incident channel.
3. **Mitigate before diagnosing** — **roll back / flip the feature flag / scale up** first. *"Restore service, then find the cause — the cause can wait, the users can't."*
4. **Diagnose** — logs by correlation ID, traces, metrics, recent deploys ("what changed?" is the highest-yield first question).
5. **Fix and verify.**
6. **Blameless post-mortem** — timeline, root cause (5 whys), and **action items with owners and dates**.
7. **Prevent** — the monitoring/alert/test that would have caught it, added as a ticket that actually gets scheduled.

> 🗣️ **STAR:** "On `<project>`, `<symptom — e.g. the app started timing out for all users after a release>`. I `<rolled back within N minutes>` to restore service, then traced it to `<cause — e.g. an N+1 query introduced by a lazy association>`. The fix was `<what>`, and I added `<a query-count assertion / an alert on p95 latency>` so the same class of problem is caught before prod. Downtime was `<N>` minutes and it hasn't recurred."
>
> 💡 If you genuinely haven't led a prod incident, say so and give the closest real thing — a severe QA/UAT defect you triaged under pressure — then describe the process above as what you'd run. **Honest + structured beats invented.**

## 9.7 Technical debt

**Principle:** "Debt is a **trade-off, not a sin**. Sometimes taking it is right. What's not acceptable is taking it **silently**."

**Practice:** keep a **visible debt register** with impact (what it costs us per sprint) rather than "this is ugly" · **~20% of each sprint** allocated to it, agreed with the PO up front · **the boy-scout rule** for small things · tie the pitch to **business language** — *"this is why `<feature>` takes 3 days instead of 1 and why we had `<N>` regressions"* · **strangler-fig** incremental replacement rather than a big-bang rewrite · ⚠️ **resist rewrites** — they're usually a slower, riskier version of refactoring.

> 🗣️ **STAR:** "`<Legacy area>` had `<no tests / duplicated logic>`, so every change there caused regressions. Rather than a rewrite, I `<added characterisation tests, then extracted the shared logic module by module>` while we delivered features around it. Over `<N>` sprints, `<bug rate / change lead time>` improved by `<X>`."

## 9.8 Disagreements

**Principle:** "Disagree on the merits, decide on the data, then commit fully either way."

**Practice:** understand their position first and say it back in your own words · move from opinion to **evidence** — a benchmark, a spike, a prototype, a bug count · separate **reversible** decisions (just try it) from **expensive** ones (spend the time) · if it's still tied, **escalate to a decision-maker rather than letting it drift** · once decided, **support it publicly even if it wasn't your choice** · revisit later with evidence if you were right.

> 🗣️ **STAR:** "A colleague wanted `<approach A>`; I preferred `<B>`. Rather than argue in review, we timeboxed a `<1-day>` spike on both against our real `<data volume / device>`. `<A>` turned out `<measurably better/worse>` on `<metric>`, so we went with `<choice>` — including when that meant dropping my own preference. The point is the decision got made on evidence in a day instead of a week of debate."

⚠️ Also prepare **"a disagreement with your manager or a stakeholder"** — same structure, plus: raise it privately, present impact and options rather than resistance, and if they still choose differently, **document the risk and execute properly**.

## 9.9 Stakeholder communication

**Principle:** "Translate. Stakeholders don't need the architecture; they need **impact, options, and a date they can trust**."

**Practice:** lead with the **conclusion**, then the detail · talk in **business terms** (users affected, revenue/compliance risk, delivery date), never in framework names · give **options with trade-offs** rather than a flat "no" — *"we can have it by Friday without `<X>`, or fully by Wednesday"* · **surface bad news early** — the cost of a delay doubles once it's a surprise · write decisions down and confirm in one line after a call · manage expectations with ranges, not false precision.

> 🗣️ **STAR:** "On `<project>`, `<stakeholder>` wanted `<feature>` in `<timeframe>`. Instead of saying it was impossible, I broke it into `<a core slice that met the real deadline>` and `<the rest>`, explained the risk of the compressed option in terms of `<QA time / defect risk>`, and let them choose. We delivered `<the slice>` on time and `<the rest>` the following sprint."

## 9.10 Maintaining code quality

**The full stack of mechanisms — this is a "what do you actually put in place" question:**
| Layer | Mechanism |
|---|---|
| **Standards** | A written team guide (naming, folder structure, state rules, error handling) — short and actually maintained |
| **Automation** | Prettier + ESLint (+ `rxjs`/`@angular-eslint` rules), Checkstyle/Spotless, **pre-commit hooks** — style is never a review topic |
| **Tests** | Unit (Jest/Karma), component, integration, e2e (Cypress/Playwright); **a coverage threshold that fails the build**; tests required with every bug fix |
| **CI gates** | Build, tests, coverage, **bundle budget**, SonarQube quality gate, dependency & SAST scan |
| **Review** | Small PRs, checklist, required approval, one-day SLA |
| **Architecture** | ADRs, module boundaries, lint rules that ban cross-feature imports |
| **Observability** | Error tracking (Sentry), so quality is measured in production, not just in CI |
| **Culture** | The boy-scout rule, blameless post-mortems, and **the lead writing code too** — you can't set a standard you don't practise |

> 🎯 **The closing sentence for this whole Part:** "Quality isn't a phase or a person — it's the set of defaults the team works inside. My job as lead is to make the right thing the easy thing, and to make sure the team owns the standard rather than me policing it."

---

# Part 10 — AI-assisted development

*Their list: how you use Claude, Cursor and GitHub Copilot; where they improve productivity; how you review AI-generated code; security/privacy concerns; testing generated code; examples from actual development.*

> 💡 **This is a modern-lead question, and most candidates answer it badly in one of two ways:** either *"I don't really use it"* (reads as behind) or *"it writes most of my code"* (reads as reckless). **The winning position is: enthusiastic user, rigorous reviewer, clear on the boundaries — especially with healthcare data.** Prepare this like a technical topic, because they listed it like one.

## 10.1 How you use them — be specific per tool

| Tool | Shape | What it's genuinely good for |
|---|---|---|
| **GitHub Copilot** | Inline autocomplete in the editor | Boilerplate as you type — DTOs, mappers, test scaffolding, repetitive `*.spec.ts`, obvious next lines |
| **Cursor** | An AI-native editor with **codebase context** | Multi-file changes, "rename this concept everywhere", explaining an unfamiliar area, refactors across files |
| **Claude / Claude Code** | Conversational + agentic, long context | Design discussion and trade-offs, reviewing my own diff before I raise the PR, writing tests from a spec, explaining a stack trace, generating documentation/ADRs, migration plans |

> 🗣️ **The framing sentence:** "I treat them as a **very fast junior pair-programmer with excellent recall and no accountability**. They accelerate the parts of the job that are typing; they don't do the part that's deciding. The responsibility for anything that lands in a PR with my name on it is entirely mine."

## 10.2 Where they genuinely improve productivity

**High value:**
- **Boilerplate** — Angular components/services/specs, reducers/effects/selectors, DTOs, mappers, form scaffolding.
- **Tests** — first-draft unit tests and, crucially, **edge cases I hadn't thought of** ("what other cases should this cover?").
- **Understanding unfamiliar code** — "explain this module", "where is this state mutated?" — this is the biggest real win when joining a legacy codebase.
- **Translation work** — JS→TS, template-driven→reactive forms, RxJS→signals, an NgModule app→standalone, SQL→JPQL.
- **Regex, config, YAML, migrations, shell** — things that are quick to verify but slow to write.
- **Documentation, ADRs, PR descriptions, release notes.**
- **Rubber-ducking a design** — arguing with it surfaces holes in my own thinking.

**Low value / actively risky:**
- **Novel business logic** — clinical rules, eligibility criteria, anything domain-specific it can't know.
- **Anything security-critical** (auth flows, crypto, permission checks) — plausible-looking and subtly wrong is the worst combination.
- **Architecture decisions** — it doesn't know our team, constraints, deadline or regulator.
- **Anything where I couldn't tell whether the answer is wrong** — that's the real boundary.

## 10.3 How you review AI-generated code — say this as a checklist

> **"The rule I hold myself and my team to: I must be able to explain every line I commit. If I can't, it doesn't go in."**

1. **Read it fully** — never accept a suggestion I haven't read end to end.
2. **Check the API is real** — models hallucinate methods, options and package names. Verify against the actual docs/version. ⚠️ **"Slopsquatting"**: a hallucinated package name can be registered by an attacker — never install a dependency an AI suggested without checking it exists and is legitimate.
3. **Check the version matches ours** — AI answers skew to older Angular/RxJS idioms (`HttpModule`, deprecated operators, `subscribe(next, error)` overloads).
4. **Edge cases and error paths** — generated code is usually the happy path only: nulls, empties, failures, cancellation, concurrency.
5. **Security** — authorization checks, injection, secrets, PHI in logs, unsafe `innerHTML`.
6. **Performance** — N+1 queries, missing `trackBy`, subscriptions without teardown, unbounded queries.
7. **Consistency** — does it match our conventions and reuse our existing utilities, or has it invented a parallel way of doing the same thing? **This is the one that quietly rots a codebase.**
8. **Tests** — I write or verify the tests myself; I don't let the same generator both write the code and certify it.
9. **Licensing/provenance** — for anything that looks like a large verbatim block.

## 10.4 Security & privacy — the section that matters most at IQVIA

> 🔴 **Lead with the healthcare framing; it's exactly the judgement they're screening for.**

- **Never paste PHI/PII, patient data, credentials, keys or customer data into any AI tool.** If I need help with a query, I use **synthetic or anonymised** data and a schema, not real rows.
- **Know the deployment model:** an enterprise/business tier with **no training on your data**, or a self-hosted/VPC deployment, is a completely different risk profile from a personal consumer account. **Use the approved tool, on the approved account, and follow the company's AI policy** — in a regulated company that policy exists and is not optional.
- **Proprietary code is confidential too** — the same rule applies to source, not just data.
- **Supply chain:** verify suggested dependencies; keep `npm audit`/Snyk in CI.
- **Auditability:** in a GxP/validated system, generated code goes through exactly the same controlled review, test and release process as any other code — **there is no fast lane**. AI changes who types the code, not who's accountable for it.
- **Bias/correctness:** never let generated logic make a clinical or eligibility decision without human verification and a test.

> 🗣️ **The sentence to have ready:** "My rule is simple: **the AI can see the shape of the problem, never the patient's data.** Schema yes, synthetic data yes, real PHI never — and I'd make that an explicit team rule, not an assumption."

## 10.5 Testing generated code

- **Tests are the safety net that makes AI-assisted development responsible** — the faster code is produced, the more the tests matter.
- **I don't let the same prompt write both** the implementation and the test that certifies it, at least not without reading both critically — it will happily test the behaviour it *implemented*, including the bug.
- Good pattern: **write (or specify) the test first**, then let AI implement against it — the test is the spec, so correctness is verifiable rather than assumed.
- Use AI *for* testing: "what edge cases am I missing?", generating table-driven cases, building fixtures and mocks, and drafting e2e scenarios from acceptance criteria.
- **Coverage isn't correctness** — generated tests can be assertion-free or tautological (`expect(component).toBeTruthy()` and nothing else). Review that each test would actually **fail** if the behaviour broke.
- Everything still passes the same CI gates: lint, tests, coverage threshold, SonarQube, security scan.

## 10.6 Examples from actual development — ⚠️ prepare two, from your real work

**They asked for examples. Have two ready, 60 seconds each, in this shape:** *the task → what I asked the tool → what it produced → **what I had to fix** → the outcome.* **The "what I had to fix" part is the most important sentence** — it proves you reviewed rather than pasted.

**Example skeleton A — speed:**
> "On `<project>` I had to `<convert N template-driven forms to reactive / write specs for a service layer>`. I used `<Copilot/Claude>` to generate the first pass. It got the structure right but `<used a deprecated validator API / missed the cross-field validation / didn't handle the async case>`, which I rewrote. It turned roughly `<a day>` of typing into `<an hour>` of typing plus `<an hour>` of careful review — and the review is where the value of my time actually was."

**Example skeleton B — understanding:**
> "When I joined `<the Backoffice migration>`, I used `<Cursor/Claude>` to explain `<an unfamiliar legacy module>` and map where `<state was mutated / a config was read>`. It gave me a map in minutes that would have taken me `<a day>` of grepping. I then verified the key claims by reading the code myself, because it was `<confidently wrong about one thing — X>`."

> 🎯 **Close the whole section on the lead angle:** "As a lead I'd want a team position on this, not individual improvisation: an approved tool list, a written rule about what data can go in, AI-assisted code reviewed to the same standard as any other, and no relaxation of the CI gates. Used that way it's a genuine multiplier — especially for onboarding and for test coverage, which are usually the two things a team is short of."

---

# 📅 Study plan

**If you have ~5 days** (adjust proportionally — the *order* is what matters):

| Day | Focus | Deliverable by the end of the day |
|---|---|---|
| **1** | **Part 9 (Tech Lead) + Part 10 (AI)** | **Nine STAR stories written down with real numbers.** Do this first — it's the part you can't cram, and it's what differentiates you |
| **2** | **Part 1 (Angular)** — CD, Signals, performance, leaks, guards/resolvers | Explain `OnPush` + the immutable-update trap out loud, from memory |
| **3** | **Part 3 (RxJS/NgRx)** + **Part 2 (TS/JS)** | Draw the NgRx flow diagram from memory; say the four flattening operators with their use cases |
| **4** | **Part 4 (Ionic/Capacitor)** + **Part 5 (REST/Security)** | The `ionViewWillEnter` bug story; the token-refresh interceptor written from scratch |
| **5** | **Part 6 (SQL/Mongo)** + **Part 7 (Microservices)** + **Part 8 (Git/CI/AWS)** | Write the top-salary-per-department query **and** the Streams version without looking |

**Every day, before you start:** 15 minutes on **[26 — Companies §Most-repeated](./26-companies-asked-questions.md#-most-repeated-questions-across-all-companies)**.
**Every day, in the evening:** answer 5 questions **out loud, timed at 90 seconds each.** Reading is not preparing — you lose rounds on delivery, not on knowledge.

---

# 📄 One-page cheat sheet — read this 20 minutes before

**Angular**
`constructor` = DI only → `ngOnInit` = inputs are ready · `OnPush` fires on **input reference change / event / async pipe / signal** — ⚠️ `push()` won't trigger it, `[...arr, x]` will · `takeUntilDestroyed()` · `trackBy`/`track` · guards return a **`UrlTree`** · `CanMatch` for role-based lazy routes · resolvers **block navigation** · lazy: never eagerly import the same module · **`signal` / `computed` (lazy+memoised) / `effect` (side effects only)** · never call methods in templates.

**TS/JS**
`const` freezes the **binding**, not the object · arrow = **lexical `this`** · **microtasks before macrotasks** · `unknown` = narrow before use; `never` = **exhaustiveness check** · spread is a **shallow** copy · `type` for unions, `interface` for objects (declaration merging) · TS is **structurally** typed.

**RxJS/NgRx**
Promise = one, eager, uncancellable · Observable = many, lazy, cancellable · `BehaviorSubject` for state, expose `.asObservable()` · **`switchMap`** search / **`concatMap`** ordered writes / **`exhaustMap`** submit / **`mergeMap`** parallel · ⚠️ never `switchMap` a POST · `forkJoin` = `Promise.all`, `catchError` **inside** so one failure doesn't kill the page · **NgRx: `catchError` INSIDE the effect's inner pipe** or the effect dies forever · `shareReplay({refCount:true})`.

**Ionic**
`ngOnInit` runs once, **`ionViewWillEnter` runs every time you come back** (pages are cached — that's the "list doesn't refresh" bug) · `npx cap sync` after every build/plugin · SQLite (encrypted) for offline data, **Keychain/Keystore for tokens**, never `localStorage` for PHI · offline = local write + sync queue + **client-generated UUID for idempotency** + conflict policy · request permissions **in context**.

**REST/Security**
**401 = who are you (refresh) · 403 = not allowed (don't refresh)** · `PUT` full/idempotent vs `PATCH` partial · JWT is **signed, not encrypted — no PHI inside** · access token short + in memory, refresh token in an **httpOnly cookie** · **gate the refresh stampede** · CORS is enforced by the **browser** (works in Postman ≠ works in the app) · `status === 0` = never reached the server.

**SQL/Mongo**
`FROM→WHERE→GROUP BY→HAVING→SELECT→ORDER BY→LIMIT` · `WHERE` filters rows, `HAVING` filters groups · `COUNT(e.id)` not `COUNT(*)` on a LEFT JOIN · **`DENSE_RANK`** for Nth distinct salary, `RANK() … PARTITION BY dept` = 1 for top per department · **leftmost prefix**, sargability, `EXPLAIN ANALYZE` · ⚠️ `NOT IN` + NULL = no rows · Mongo: `$match` first, **embed if read together & bounded, reference if shared or unbounded**, 16 MB limit.

**Microservices**
Sync when I need the answer now, async when B just needs to eventually know · timeout → **circuit breaker (CLOSED/OPEN/HALF_OPEN)** → retry+backoff+jitter → bulkhead → fallback · retries only if **idempotent** → **idempotency key** · **transactional outbox** + idempotent consumer + **DLQ** · Saga = compensating transactions (give a concrete one) · Kafka: partition key ⇒ ordering per key, at-least-once · **trace ID on every hop, never log PHI**.

**Lead**
Principle → mechanism → **STAR with a number** → what I'd change · incidents: **mitigate first, diagnose second, blameless post-mortem** · debt: visible, quantified in business terms, ~20% per sprint, **strangler not rewrite** · disagreement: **timeboxed spike beats a long argument**, then commit either way · AI: fast junior pair-programmer, **I must be able to explain every line I commit, and the model never sees PHI**.

---

# ❓ Questions to ask them

Pick 3–4 — asking good questions is itself part of the assessment for a lead role.

1. "What does the **team** look like — how many engineers, and would I be leading or joining as a senior contributor first?"
2. "Is the product **Angular web, Ionic mobile, or both**? Do the clinical users work offline in the field?" *(shows you've read the stack for what it implies)*
3. "How much of the work is **new development** versus modernising an existing system?"
4. "What's the **release process** like — is this a validated/GxP system with controlled releases, and how does that shape CI/CD?" ⭐
5. "How does the team handle **PHI in non-production environments** — synthetic data, anonymisation?" ⭐
6. "What does the team currently use for **state management**, and is there an appetite to move toward signals?"
7. "Where does the team feel its **biggest technical debt** is right now?"
8. "Does the team have a position on **AI-assisted development** — approved tools, data rules?" ⭐ *(brings their own item back to them, as a lead would)*
9. "What would you want the person in this role to have achieved in their **first 90 days**?"
10. "What are the next steps and the timeline?"

> ⭐ = the ones that specifically mark you as thinking like a lead in a **regulated healthcare** company.

---

# 🧠 Before you walk in

**You have now been through 10 rounds and logged 134 questions. That is a real asset — most candidates walk out and forget by evening.** The Altimetrik result today is one data point, not a verdict: the topics they asked are all now written down and answered, and **this checklist is 75% the ground you're strongest on.**

**Three things to do the morning of:**
1. Read the **cheat sheet** above and **[26 §Most-repeated](./26-companies-asked-questions.md#-most-repeated-questions-across-all-companies)** — nothing else.
2. Say your **60-second self-introduction** out loud once → **[00 — Self Introduction](./00-self-introduction.md)**.
3. Pick your **three strongest STAR stories** from Part 9 and have them ready to reach for.

**In the room:** three-sentence answers · a clean "I don't know" when you don't · **"on my team I'd…"** for anything lead-shaped · and one sentence early that shows you understand this is healthcare data.

**After the round — the same day — log every question into [26](./26-companies-asked-questions.md).** That habit is why this file exists.

---

**Related files:** [26 — Companies (recall log)](./26-companies-asked-questions.md) · [37 — Altimetrik 31 Aug](./37-altimetrik-fullstack-java-angular-31aug.md) · [04 — Angular](./04-angular.md) · [03 — TypeScript](./03-typescript.md) · [01 — JavaScript](./01-javascript.md) · [20 — RxJS](./20-rxjs-operators.md) · [21 — NgRx](./21-ngrx.md) · [15 — Ionic](./15-ionic-level1.md) · [25 — Binding & Forms](./25-angular-binding-forms.md) · [36 — SQL](./36-sql-interview-questions.md) · [13 — MongoDB](./13-mongodb.md) · [08 — Microservices](./08-microservices-basics.md) · [07 — AWS](./07-aws-basics.md) · [06 — Spring Boot](./06-spring-boot.md) · [00 — Self Introduction](./00-self-introduction.md)
