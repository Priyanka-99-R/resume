# 🟡 Tech Mahindra — Angular Developer Round (Aug 2026) · RESULT PENDING

**Every question below was actually asked in this round**, in roughly this order. Result is pending — so this file has two jobs: **(a)** be the answer key for the follow-up/next round if they call you back, and **(b)** feed the recall log, because these same questions recur everywhere (Observables vs Promises has now been asked in **4** rounds; interceptors in **3**).

> **Read the shape of this round:** it was a **breadth sweep** — Angular state → NgRx → TypeScript → HTTP layer → core JavaScript → HTML/CSS. Short questions, many topics, no deep coding. That kind of round is won by **crisp 3-sentence answers with one concrete example each**, not by long explanations. If you get a second round with them, expect the opposite: they'll pick 3 of these and drill.


> ## 🔵 Answer these with YOUR CODE, not with definitions ⭐
>
> The questions in this file repeat across companies. What changes the outcome is **finishing each answer with one concrete thing from your own codebase**:
>
> ```
> Lifecycle hooks (5 rounds ⭐) → "ngOnDestroy in 100 components, 426 takeUntil"
> Interceptors (4 rounds ⭐)    → the FormData Content-Type trap + cancelRequests$
> Observable vs Promise (4 ⭐)  → "switchMap 246 times — the auth interceptor and every search"
> Data binding (4 rounds)      → 252 @Input, and OnPush on the 6 heavy components
> HashMap internals (3 rounds) → the N+1 aggregation fix (same reasoning, in Mongo)
> Lazy loading (3 rounds)      → 27 route resolvers, access-ruler resolvers
> ```
>
> **Definition → one thing from my code → one trade-off.** Three sentences. That is what five years sounds like.
> Full inventory: **[39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)**.


---

## The questions they asked

| # | Question | Section |
|---|---|---|
| 1 | State management | [→](#1-state-management-in-angular) |
| 2 | NgRx and NgRx features | [→](#2-ngrx-and-its-features) |
| 3 | Any idea of TypeScript? | [→](#3-any-idea-of-typescript) |
| 4 | HTTP interceptor and its benefits | [→](#4-http-interceptor-and-its-benefits) |
| 5 | String interpolation | [→](#5-string-interpolation) |
| 6 | Observables and Promises | [→](#6-observables-vs-promises) |
| 7 | HttpClient — features & benefits | [→](#7-httpclient--features--benefits) |
| 8 | Callback, callback hell | [→](#8-callback-and-callback-hell) |
| 9 | Event capturing and event bubbling | [→](#9-event-bubbling-vs-event-capturing) |
| 10 | ES5 vs ES6 | [→](#10-es5-vs-es6) |
| 11 | Arrow functions | [→](#11-arrow-functions) |
| 12 | OOP in TypeScript | [→](#12-oop-in-typescript) |
| 13 | Modules — how, and types | [→](#13-modules--how-and-what-types) |
| 14 | `unknown` type and `never` type | [→](#14-unknown-vs-never-vs-any-vs-void) |
| 15 | HTML and CSS | [→](#15-html--css) |

---

## 1. State management in Angular

**The answer they want is a ladder, not one tool.** Start by naming the levels, then say where you land.

> *"State management is deciding **who owns data** and **how changes propagate**. In Angular I think of it as four levels:*
>
> *1. **Component state** — a plain field in the component. Fine for anything not shared: a toggle, a form's local flag.*
> *2. **Parent–child** — `@Input()` down, `@Output()` up. Fine for one or two levels.*
> *3. **A shared service with a `BehaviorSubject`** (or a `signal` in newer Angular) — this is the workhorse. The service owns the state, exposes it as a read-only `Observable` via `asObservable()`, and any component subscribes. It covers the large majority of apps.*
> *4. **NgRx** — a single immutable store with actions/reducers/effects, for genuinely app-wide state with complex flows and a need for traceability.*
>
> *My default is level 3, and I move to NgRx when several unrelated features read and write the same state, or when I need time-travel debugging and a strict audit of what changed."*

**The service-with-subject pattern — be ready to write it:**

```typescript
@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly items$ = new BehaviorSubject<CartItem[]>([]);
  readonly cart$ = this.items$.asObservable();          // read-only outward
  readonly total$ = this.cart$.pipe(
    map(items => items.reduce((s, i) => s + i.price * i.qty, 0))
  );

  add(item: CartItem): void {
    this.items$.next([...this.items$.value, item]);      // new array — immutability
  }
}
```

**Why `BehaviorSubject` specifically:** it holds a current value, so a component subscribing *later* (e.g. a lazily-loaded route) immediately receives the latest state instead of waiting for the next emission. `Subject` would leave it blank.

> 💡 **Modern add-on that scores:** *"On Angular 16+ I'd consider **signals** for this — `signal()` for the state, `computed()` for derived values. It's synchronous, needs no subscription or `async` pipe, and works with zoneless change detection. RxJS still wins for anything async or stream-shaped, which is why the two are designed to interoperate via `toSignal`/`toObservable`."*

> ⚠️ **Honesty position:** your NgRx is from **EasyVisa**, not RoboGebra. Say it that way — see [21 — NgRx](./21-ngrx.md).

---

## 2. NgRx and its features

> *"NgRx is Redux for Angular — a single immutable **store** as the source of truth, changed only by dispatching **actions**, applied by **pure reducers**, read through memoized **selectors**, with side effects isolated in **effects**. The whole point is that state changes are traceable: every change has a named action, so the DevTools show you exactly what happened, in order, and you can replay it."*

**The building blocks — name all five plus the extras:**

| Feature | What it is |
|---|---|
| **Store** | One immutable state tree for the whole app; an `Observable` of state |
| **Actions** | Plain objects `{ type, payload }` describing *what happened* (not what to do) |
| **Reducers** | **Pure** functions `(state, action) => newState`. No mutation, no HTTP, no `Date.now()` |
| **Selectors** | Query the store; **memoized** — recompute only when their inputs change |
| **Effects** | Where the impure work lives — HTTP, routing, storage. Listen for an action, do the work, dispatch a result action |
| **`@ngrx/entity`** | Normalised collections — `EntityAdapter` gives `addMany`/`upsertOne`/`selectAll` for free |
| **`@ngrx/store-devtools`** | Time-travel debugging in the browser |
| **`@ngrx/component-store`** | Local, component-scoped store — NgRx patterns without the global store |
| **`@ngrx/signals` (SignalStore)** | The newer signal-based store |
| **`@ngrx/router-store`** | Puts router state in the store |

**The data flow — draw this if there's a whiteboard:**

```
Component ──dispatch(action)──▶ Reducer ──▶ new State ──▶ Selector ──▶ Component (async pipe)
                    │                                                        ▲
                    └──────▶ Effect ──▶ HTTP/API ──▶ dispatch(successAction)─┘
```

**Naming convention worth quoting:** `'[Source] Event'` — e.g. `'[Visa Page] Load Documents'`, `'[Documents API] Load Documents Success'`. Actions describe **events that happened**, not commands, which is why one event can be handled by several reducers.

```typescript
export const loadDocs = createAction('[Visa Page] Load Documents', props<{ id: string }>());
export const loadDocsSuccess = createAction('[Docs API] Load Success', props<{ docs: Doc[] }>());

export const reducer = createReducer(initialState,
  on(loadDocs, s => ({ ...s, loading: true })),                 // new object, never mutate
  on(loadDocsSuccess, (s, { docs }) => ({ ...s, docs, loading: false }))
);

loadDocs$ = createEffect(() => this.actions$.pipe(
  ofType(loadDocs),
  switchMap(({ id }) => this.api.getDocs(id).pipe(
    map(docs => loadDocsSuccess({ docs })),
    catchError(err => of(loadDocsFailure({ error: err.message })))   // inner catch — keeps the effect alive
  ))
));
```

> ⚠️ **The two traps they follow up with:**
> 1. **`catchError` must be on the inner observable.** Put it on the outer pipe and the error kills the effect stream permanently — the effect stops listening and that feature silently dies.
> 2. **Reducers must be pure.** No `Math.random()`, no `new Date()`, no service calls, no mutation — otherwise time-travel and memoization both break.

**When NOT to use NgRx** (say this unprompted — it reads as judgement):
> *"For a small or medium app it's a lot of boilerplate for state a service with a `BehaviorSubject` would hold fine. I'd use it when several features share state, when the async flows are complex enough that traceability matters, or on a big team that needs one enforced pattern."*

---

## 3. "Any idea of TypeScript?"

An invitation to demonstrate range in 45 seconds. Don't answer "yes".

> *"Yes — all my Angular work is TypeScript. It's a **typed superset of JavaScript** that compiles down to JS; the types exist only at compile time, so there's no runtime cost and no runtime checking. What it buys is catching errors at build time and, honestly just as much, the editor experience — autocomplete, safe rename, find-usages across a large codebase.*
>
> *Day to day I use: interfaces and type aliases for API models, **union and literal types** for finite states, **generics** for reusable services, **enums**, the **utility types** — `Partial`, `Pick`, `Omit`, `Record`, `Readonly` — optional chaining and nullish coalescing, and **strict mode** with `strictNullChecks` on, which is what actually removes the whole class of undefined bugs."*

**Have these ready if they probe:**

```typescript
// interface vs type
interface User { id: number; name: string; }            // extendable, declaration merging, best for objects
type Status = 'draft' | 'submitted' | 'approved';       // unions, primitives, tuples — types only

// generics
function first<T>(arr: T[]): T | undefined { return arr[0]; }

// utility types
type UserPatch = Partial<User>;             // all optional — perfect for a PATCH payload
type UserCard  = Pick<User, 'id' | 'name'>;
type NoId      = Omit<User, 'id'>;
type ById      = Record<number, User>;

// type guard — narrowing
function isUser(x: unknown): x is User {
  return typeof x === 'object' && x !== null && 'id' in x;
}
```

> ⚠️ **The follow-up:** *"Does TypeScript validate an HTTP response at runtime?"* — **No.** `http.get<User[]>()` is a compile-time assertion only; if the API returns something else, TypeScript never notices. For real validation you need runtime checks (a guard, or a library like zod). That answer separates people who understand types from people who trust them.

---

## 4. HTTP interceptor and its benefits

> *"An interceptor sits in the middle of Angular's HTTP pipeline. Every request from `HttpClient` passes through the chain of interceptors on the way out and every response on the way back, so it's the one place to put **cross-cutting concerns** instead of repeating them in every service."*

**Functional interceptor (Angular 15+, the current style):**

```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).token;
  // requests are IMMUTABLE — you must clone to modify
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) inject(Router).navigate(['/login']);
      return throwError(() => err);
    })
  );
};

// registration
bootstrapApplication(App, {
  providers: [provideHttpClient(withInterceptors([authInterceptor, loadingInterceptor]))]
});
```

**Class-based (older / NgModule apps) — know both:**

```typescript
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
  }
}
// providers: [{ provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }]   ← multi:true is essential
```

**The benefits — list them, that's literally the question:**

| Benefit | Example |
|---|---|
| **Auth** | Attach the JWT/bearer token to every outgoing request |
| **Centralised error handling** | Map 401 → logout, 403 → forbidden page, 5xx → toast |
| **Loading indicator** | Increment a counter on request, decrement on response |
| **Logging / correlation ids** | Add a request id header, log timings |
| **Base URL / headers** | Prefix the API URL, set `Content-Type`, add locale |
| **Retry** | `retry({ count: 2, delay: 1000 })` on idempotent calls |
| **Caching** | Return a cached response without hitting the network |
| **Mocking** | Short-circuit to fake data during development |
| **DRY** | None of the above is repeated in 30 services |

> ⚠️ **Three follow-ups they ask:**
> 1. **Order matters** — interceptors run in registration order on the request and in **reverse** order on the response.
> 2. **`req` is immutable** — you must `req.clone()`; mutating it directly does nothing.
> 3. **`multi: true`** — omit it in the class-based form and your interceptor **replaces** the whole array instead of joining it.
> 4. Interceptors only see traffic from **`HttpClient`** — not `fetch`, not `XMLHttpRequest`, not a third-party SDK.

---

## 5. String interpolation

> *"Interpolation is Angular's one-way binding from the component class to the template, using double curly braces: `{{ expression }}`. Angular evaluates the expression in the component's context, converts it with `toString()`, and inserts it into the DOM — and it re-evaluates on every change-detection cycle."*

```html
<h1>Hello {{ user.name }}</h1>
<p>Total: {{ price * qty | currency:'INR' }}</p>
<p>{{ user?.address?.city ?? 'Not provided' }}</p>
<img [src]="avatarUrl">                      <!-- property binding, not interpolation -->
<img src="{{ avatarUrl }}">                  <!-- works for strings, but [src] is preferred -->
```

**Points that turn a 10-second answer into a good one:**

- It is **one-way**, component → template. It's shorthand for a property binding to `textContent`.
- Angular **sanitizes** the output, so interpolated values are escaped — `{{ '<b>hi</b>' }}` renders as literal text, not bold. That's built-in XSS protection.
- **Template expressions are restricted:** no assignments, no `new`, no `++`/`--`, no bitwise ops, no `;`. Deliberately — they must be side-effect free.
- **Keep them cheap.** `{{ getTotal() }}` calls the method on **every** change-detection run, many times a second. Use a property, a pure pipe, or a memoized value instead — a real performance answer they like hearing.
- The delimiters are configurable via `interpolation: ['[[', ']]']` in `@Component` — rarely needed, but a nice detail.

**The four binding types, since interpolation is one of them:**

| Syntax | Direction | Name |
|---|---|---|
| `{{ value }}` | Component → View | Interpolation |
| `[prop]="value"` | Component → View | Property binding |
| `(event)="handler()"` | View → Component | Event binding |
| `[(ngModel)]="value"` | Both | Two-way (banana-in-a-box) |

---

## 6. Observables vs Promises

**Asked in 4 separate rounds now. This must be automatic.**

| | **Promise** | **Observable** |
|---|---|---|
| Values | **One** value (or one error) | **Zero to many**, over time |
| Execution | **Eager** — starts the moment it's created | **Lazy** — nothing runs until `subscribe()` |
| Cancellable | ❌ | ✅ `unsubscribe()` — cancels the in-flight XHR |
| Operators | `.then`/`.catch` chaining only | 100+ RxJS operators: `map`, `filter`, `switchMap`, `debounceTime`, `retry`… |
| Retry | Manual — you must re-call the function | `retry()` / `retryWhen()` built in |
| Native? | ✅ JavaScript | ❌ RxJS library (Angular ships it) |
| Multicast | Always single-cast to its `.then`s | Unicast by default; multicast via `Subject`/`share()` |
| Angular uses it for | — | `HttpClient`, router events, forms `valueChanges`, `@Output` `EventEmitter` |

> **The 20-second answer:** *"A Promise is a single future value, eager and not cancellable. An Observable is a **stream** of zero-to-many values, lazy — nothing happens until you subscribe — cancellable via unsubscribe, and composable with RxJS operators. That's why Angular's `HttpClient` returns an Observable even though an HTTP call yields one response: you get cancellation and operators like `retry`, `debounceTime` and `switchMap` for free."*

**The killer demonstration — typeahead search:**

```typescript
this.searchControl.valueChanges.pipe(
  debounceTime(300),                 // wait for a pause in typing
  distinctUntilChanged(),            // ignore "same text again"
  switchMap(term => this.api.search(term)),   // CANCELS the previous in-flight request
  takeUntilDestroyed()               // auto-unsubscribe on destroy
).subscribe(results => this.results = results);
```

> *"With Promises this needs manual timers and flags to discard stale responses, and you still can't cancel the request — so a slow earlier response can overwrite a newer one. `switchMap` solves that in one word."*

**Converting between them:** `firstValueFrom(obs$)` / `lastValueFrom(obs$)` (RxJS 7+, replacing the deprecated `toPromise()`), and `from(promise)` the other way.

> ⚠️ **Follow-up: "what if you forget to unsubscribe?"** — memory leak: the subscription keeps the component alive and the callback keeps firing after navigation. Fixes: the **`async` pipe** (unsubscribes automatically — the best answer), `takeUntilDestroyed()`, `takeUntil(this.destroy$)`, or a manual `unsubscribe()` in `ngOnDestroy`. **`HttpClient` observables complete after one emission**, so those self-unsubscribe — but `valueChanges`, router events and `interval` never complete.

---

## 7. HttpClient — features & benefits

> *"`HttpClient` is Angular's HTTP API from `@angular/common/http` — a wrapper over `XMLHttpRequest` that returns Observables. It replaced the old `Http` service in Angular 4.3."*

**The features — this is the answer they're scoring:**

| Feature | Why it matters |
|---|---|
| **Returns Observables** | Cancellable, retryable, composable with RxJS |
| **Automatic JSON parsing** | No `res.json()` step — and the body is typed |
| **Typed responses** | `http.get<User[]>(url)` gives you `Observable<User[]>` |
| **Interceptors** | Auth, logging, errors, caching — centrally (see §4) |
| **`HttpErrorResponse`** | Structured errors with `status`, `statusText`, `error`, plus client-vs-server distinction |
| **Request/response headers & params** | Immutable `HttpHeaders` / `HttpParams` builders |
| **Progress events** | `reportProgress: true` + `observe: 'events'` → upload/download progress bars |
| **`observe: 'response'`** | Access the full response — status code, headers — not just the body |
| **Testing** | `HttpClientTestingModule` + `HttpTestingController` — flush fake responses, assert requests |
| **XSRF/CSRF protection** | Built in — reads the `XSRF-TOKEN` cookie, sends `X-XSRF-TOKEN` |
| **`provideHttpClient(withFetch())`** | Fetch backend option, needed for SSR |

```typescript
@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);

  getUsers(page: number): Observable<User[]> {
    const params = new HttpParams().set('page', page).set('size', 20);
    return this.http.get<User[]>('/api/users', { params }).pipe(
      retry({ count: 2, delay: 500 }),
      catchError(this.handle)
    );
  }

  upload(file: File): Observable<HttpEvent<unknown>> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post('/api/files', form, { reportProgress: true, observe: 'events' });
  }

  private handle(err: HttpErrorResponse) {
    const msg = err.error instanceof ProgressEvent
      ? 'Network error'                       // client-side / connection failure
      : `Server ${err.status}: ${err.error?.message}`;
    return throwError(() => new Error(msg));
  }
}
```

> ⚠️ **The trap:** *"I called `http.get()` but nothing happened."* — Observables are **lazy**; without a `subscribe()` (or an `async` pipe) the request is **never sent**. Guaranteed follow-up, and it connects straight back to §6.

---

## 8. Callback and callback hell

> *"A callback is just a function passed to another function to be run later — the original way of handling async work in JavaScript. **Callback hell** is what happens when async steps depend on each other: each one nests inside the previous one, and you get a pyramid that's hard to read, hard to error-handle, and hard to change."*

```javascript
// 💣 callback hell — the "pyramid of doom"
getUser(id, (err, user) => {
  if (err) return handle(err);
  getOrders(user.id, (err, orders) => {
    if (err) return handle(err);                    // error handling repeated at EVERY level
    getInvoice(orders[0].id, (err, invoice) => {
      if (err) return handle(err);
      sendEmail(invoice, (err) => {
        if (err) return handle(err);
        console.log('done');
      });
    });
  });
});
```

**Three real problems, not just "ugly":** error handling is duplicated at every level; you cannot `return` a value out of the pyramid; and *inversion of control* — you hand your continuation to someone else's library and trust it to call you exactly once.

**The three fixes, in historical order:**

```javascript
// 1. Promises — flat chain, ONE catch
getUser(id)
  .then(user => getOrders(user.id))
  .then(orders => getInvoice(orders[0].id))
  .then(invoice => sendEmail(invoice))
  .then(() => console.log('done'))
  .catch(handle);                          // one handler for the whole chain

// 2. async/await — reads like synchronous code (ES2017)
async function run(id) {
  try {
    const user    = await getUser(id);
    const orders  = await getOrders(user.id);
    const invoice = await getInvoice(orders[0].id);
    await sendEmail(invoice);
    console.log('done');
  } catch (e) { handle(e); }
}

// 3. RxJS — in Angular, the same thing with cancellation
this.getUser(id).pipe(
  switchMap(user => this.getOrders(user.id)),
  switchMap(orders => this.getInvoice(orders[0].id)),
  catchError(handle)
).subscribe();
```

> 💡 **Add the parallelism point** — it's what senior answers include: *"Sequential `await`s are only right when each step needs the previous one. For independent calls, `await Promise.all([a(), b()])` — or `forkJoin` in RxJS — so they run concurrently instead of adding up their latencies."*

---

## 9. Event bubbling vs event capturing

> *"DOM events travel in **three phases**: **capturing** from `window` down to the target, then the **target** phase, then **bubbling** back up from the target to `window`. By default `addEventListener` listens in the **bubbling** phase; pass `true` (or `{ capture: true }`) as the third argument to listen while capturing."*

```
              ┌──────────── window ────────────┐
   CAPTURE ↓  │  document → html → body → div  │  ↑ BUBBLE
              │            → BUTTON            │
              └──────────── target ────────────┘
```

```html
<div id="outer">
  <button id="inner">Click</button>
</div>
```
```javascript
outer.addEventListener('click', () => console.log('outer bubble'));
outer.addEventListener('click', () => console.log('outer capture'), true);
inner.addEventListener('click', () => console.log('inner'));

// Clicking the button logs:  outer capture → inner → outer bubble
```

**The controls:**

| Method | Effect |
|---|---|
| `e.stopPropagation()` | Stops travelling further up (or down) the tree |
| `e.stopImmediatePropagation()` | Also stops **other listeners on the same element** |
| `e.preventDefault()` | Cancels the default browser action (form submit, link navigation) — **does not** stop propagation |
| `e.target` | The element that was actually clicked |
| `e.currentTarget` | The element whose listener is running (`this`) |

**Why it matters practically — event delegation:**

```javascript
// ✅ ONE listener for a 1000-row table, and it works for rows added later
document.getElementById('table').addEventListener('click', e => {
  const row = e.target.closest('tr');
  if (row) select(row.dataset.id);
});
```

> ⚠️ **The Angular tie-in they may ask for:** *"`(click)` in an Angular template listens in the **bubbling** phase. For a click-outside directive I use `@HostListener('document:click', ['$event'])` and check `contains(e.target)`. And in a nested clickable card I call `$event.stopPropagation()` on the inner button so the card's own handler doesn't also fire."*
>
> Note: **`focus` and `blur` do not bubble** (use `focusin`/`focusout`), and neither do `mouseenter`/`mouseleave` (use `mouseover`/`mouseout`).

---

## 10. ES5 vs ES6

> *"ES6 — ES2015 — was the biggest release in JavaScript's history, and everything since has been yearly and incremental. The headline changes are `let`/`const`, arrow functions, classes, template literals, destructuring, spread/rest, default parameters, Promises, modules and `Map`/`Set`."*

| Area | ES5 | ES6 (2015) |
|---|---|---|
| Variables | `var` — function-scoped, hoisted as `undefined` | `let` / `const` — **block-scoped**, TDZ |
| Functions | `function` expressions | **Arrow functions**, lexical `this` |
| Classes | Constructor functions + prototypes | `class`, `extends`, `super`, `static` |
| Strings | `'a' + b + 'c'` | Template literals `` `a${b}c` `` + multiline |
| Objects/arrays | Index access | **Destructuring** `const { a, b } = obj` |
| Args | `arguments`, `apply` | **Spread/rest** `...args`, default params |
| Async | Callbacks | **Promises** |
| Modules | IIFE / CommonJS / globals | **`import` / `export`** (native ES modules) |
| Collections | Objects & arrays only | `Map`, `Set`, `WeakMap`, `WeakSet` |
| Iteration | `for`, `forEach` | `for...of`, iterators, generators |
| Object literals | verbose | Shorthand `{ x, y }`, computed keys `{ [k]: v }` |

```javascript
// the three var/let/const behaviours they test
console.log(a); var a = 1;      // undefined  (hoisted, initialised to undefined)
console.log(b); let b = 1;      // ❌ ReferenceError — Temporal Dead Zone

for (var i = 0; i < 3; i++) setTimeout(() => console.log(i));   // 3 3 3
for (let i = 0; i < 3; i++) setTimeout(() => console.log(i));   // 0 1 2  ← per-iteration binding
```

> ⚠️ **`const` trap:** `const` prevents **reassignment**, not mutation. `const a = [1]; a.push(2);` is legal; `a = []` is not. Use `Object.freeze()` for shallow immutability.

**Post-ES6 worth naming** (shows you didn't stop in 2015): `async/await` (ES2017), optional chaining `?.` and nullish coalescing `??` (ES2020), `Object.entries`/`Array.flat` (2017/2019), `Array.at()` (2022). Full drills in **[19 — JS Variables Made Easy](./19-javascript-variables-easy.md)**.

---

## 11. Arrow functions

> *"Arrow functions are a shorter function syntax, but the real difference isn't the syntax — it's that they **don't have their own `this`**. They inherit `this` lexically from the enclosing scope at definition time, which is why they solved the entire `var self = this` / `.bind(this)` problem."*

```javascript
const add = (a, b) => a + b;              // implicit return
const square = x => x * x;                // single param, no parens needed
const make = () => ({ id: 1 });           // returning an object literal needs parentheses
const noop = () => {};
```

**Differences from a regular function — the checklist:**

| | Regular function | Arrow function |
|---|---|---|
| `this` | **Dynamic** — depends on how it's called | **Lexical** — from the enclosing scope, fixed |
| `arguments` object | ✅ | ❌ — use rest `(...args)` |
| Can be a constructor (`new`) | ✅ | ❌ TypeError |
| `prototype` property | ✅ | ❌ |
| Hoisted (declarations) | ✅ | ❌ (it's an expression) |
| Usable as an object method | ✅ | ⚠️ `this` won't be the object |
| Usable as a generator | ✅ | ❌ no `yield` |

**The `this` demonstration — the actual interview question:**

```javascript
const timer = {
  seconds: 0,
  startBad() {
    setInterval(function () { this.seconds++; }, 1000);   // ❌ this = window/undefined
  },
  startGood() {
    setInterval(() => { this.seconds++; }, 1000);         // ✅ this = timer
  }
};

// ⚠️ but the reverse trap — arrows are WRONG for object methods:
const counter = {
  count: 0,
  inc: () => { this.count++; }     // ❌ `this` is the enclosing (module/window) scope, not counter
};
```

> **The Angular angle:** *"Arrow functions are why callbacks in Angular components work without binding — inside `subscribe(res => this.data = res)`, `this` is still the component. Using `function(res) { this.data = res }` there is a classic bug."*

---

## 12. OOP in TypeScript

> *"TypeScript adds real OO structure on top of JavaScript's prototypal model — classes with typed members, access modifiers, interfaces, abstract classes, generics. All four pillars are expressible."*

```typescript
// ── ENCAPSULATION ───────────────────────────────────────────
class Account {
  private balance = 0;                       // compile-time private
  #pin = '0000';                             // JS-native private field — enforced at RUNTIME
  protected readonly id: string;             // visible to subclasses, never reassignable
  public owner: string;

  // parameter properties — declares AND assigns in one line
  constructor(id: string, owner: string) { this.id = id; this.owner = owner; }

  get available(): number { return this.balance; }        // getter
  deposit(amount: number): void {
    if (amount <= 0) throw new Error('invalid');           // the invariant lives with the data
    this.balance += amount;
  }
}

// ── ABSTRACTION ─────────────────────────────────────────────
abstract class Shape {
  abstract area(): number;                    // must be implemented
  describe(): string { return `area = ${this.area()}`; }   // shared
}

// ── INHERITANCE ─────────────────────────────────────────────
class Circle extends Shape {
  constructor(private r: number) { super(); }
  area(): number { return Math.PI * this.r ** 2; }
}

// ── POLYMORPHISM ────────────────────────────────────────────
const shapes: Shape[] = [new Circle(2), new Square(3)];
shapes.forEach(s => console.log(s.area()));   // each runs its own implementation

// ── INTERFACES: contracts, and structural typing ────────────
interface Persistable { save(): Promise<void>; }
class Doc implements Persistable { async save() {} }
```

**Points that lift this answer:**

- **`private` vs `#private`:** `private` is erased at compile time — the field is still reachable from plain JS. `#field` is genuinely private at runtime. Say this and you've clearly used both.
- **Structural typing ("duck typing"):** TypeScript matches on **shape**, not name. An object literal with the right members satisfies an interface without `implements`. This is the biggest difference from Java/C#.
- **A class can implement many interfaces but extend one class** — same rule as Java (see [33](./33-interface-vs-abstract-class.md)).
- **No method overloading with real bodies** — you declare overload signatures and write **one** implementation that handles all of them.
- **Angular ties in:** components and services *are* classes; DI is constructor injection; `implements OnInit` is an interface contract; `@Input`/`@Output` are decorators — which is metaprogramming, another OO tool.

---

## 13. Modules — how, and what types

Answer **both** meanings, because "modules" is ambiguous between JS and Angular — and asking which they mean is itself a good move.

### (a) JavaScript / TypeScript modules

> *"Any file with a top-level `import` or `export` is a module — it gets its own scope instead of leaking into the global namespace."*

```typescript
// named exports — many per file
export interface User { id: number; }
export const MAX = 10;
export function load() {}

// default export — one per file
export default class ApiClient {}

// importing
import ApiClient, { User, MAX as LIMIT } from './api';
import * as Api from './api';                     // namespace import
const mod = await import('./heavy');              // dynamic import → code splitting
```

| Module system | Syntax | Where |
|---|---|---|
| **ES Modules (ESM)** | `import` / `export` | The standard — browsers and modern Node |
| **CommonJS** | `require` / `module.exports` | Classic Node.js |
| **AMD** | `define()` | Legacy browser (RequireJS) |
| **UMD** | Wrapper for all of them | Legacy libraries |

**ESM vs CommonJS — the difference to state:** ESM is **static** (imports resolved at parse time), which enables **tree-shaking** — the bundler drops unused exports. CommonJS is dynamic (`require` can be conditional), so it can't be tree-shaken reliably.

**Named vs default:** prefer **named** exports — better autocomplete, safe renaming, and no arbitrary renaming at each import site.

### (b) Angular modules

> *"`NgModule` was Angular's compilation and DI container: `declarations` for components/directives/pipes, `imports` for other modules, `providers` for services, `exports` for what you make public, and `bootstrap` in the root module."*

| Type | Purpose |
|---|---|
| **Root** (`AppModule`) | Bootstraps the app |
| **Feature** | Groups one domain area (`OrdersModule`) |
| **Shared** | Common components/pipes/directives re-exported to features |
| **Core** | Singleton services, imported once by the root |
| **Routing** | `RouterModule.forRoot()` / `.forChild()` per feature |
| **Lazy-loaded** | Loaded on navigation → smaller initial bundle |

```typescript
// lazy loading — the practical benefit
{ path: 'orders', loadChildren: () => import('./orders/orders.module').then(m => m.OrdersModule) }
```

> 💡 **Say the modern part — it shows you're current:** *"From Angular 14/15 onwards, **standalone components** removed the need for NgModules. A component declares its own `imports` array, routes use `loadComponent`, and providers are configured functionally with `provideHttpClient`/`provideRouter` in `bootstrapApplication`. New apps default to standalone; NgModules are still supported for existing codebases, and my earlier projects use them."*

```typescript
@Component({
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule],   // per-component, explicit
  template: `...`
})
export class OrderListComponent {}
```

> ⚠️ **`forRoot()` vs `forChild()`:** `forRoot()` registers the singleton providers and must be called **once** in the root; a lazy module calling `forRoot()` creates a **second instance** of those services — the classic "why do I have two copies of my service?" bug.

---

## 14. `unknown` vs `never` (vs `any`, `void`)

**They asked about `unknown` and `never` specifically — a deliberate depth check. Answer with the whole set; it's a short, high-scoring answer.**

### `unknown` — the type-safe `any`

> *"`unknown` means 'a value whose type I don't know yet'. Anything is assignable **to** `unknown`, but `unknown` is assignable **to nothing** without first narrowing it — so the compiler forces you to check before you use it. It's the safe counterpart of `any`, which switches type checking off entirely."*

```typescript
let a: any = 'hello';
a.toFixed();                   // ✅ compiles — 💥 crashes at runtime. any = no checking

let u: unknown = 'hello';
u.toFixed();                   // ❌ compile error: 'u' is of type 'unknown'
if (typeof u === 'number') u.toFixed();      // ✅ narrowed → allowed

// where it belongs: values crossing a trust boundary
function parse(json: string): unknown { return JSON.parse(json); }   // JSON.parse returns `any` — bad
try { /* ... */ } catch (e: unknown) {                               // catch is unknown under strict
  if (e instanceof HttpErrorResponse) console.log(e.status);
}
```

### `never` — the type with no values

> *"`never` is the type of something that **never produces a value** — a function that always throws or never returns, or a branch the compiler has proved is unreachable. It's the empty set, so it's assignable to every type and nothing is assignable to it."*

```typescript
function fail(msg: string): never { throw new Error(msg); }          // never returns
function loop(): never { while (true) {} }

type A = string & number;      // never — impossible intersection
type B = Exclude<'a'|'b', 'a'|'b'>;   // never

// ⭐ the practical use — EXHAUSTIVENESS CHECKING
type Status = 'draft' | 'sent' | 'paid';

function label(s: Status): string {
  switch (s) {
    case 'draft': return 'Draft';
    case 'sent':  return 'Sent';
    case 'paid':  return 'Paid';
    default:
      const exhaustive: never = s;      // ✅ compiles today
      return exhaustive;                // add 'void' to Status → this line FAILS TO COMPILE
  }
}
```

> 💡 **This example is the answer.** *"Adding a new value to the union breaks the build at exactly the places that need updating, instead of silently falling through at runtime. That's `never` earning its keep."*

### The full comparison

| Type | Meaning | Assign anything to it? | Assign it to anything? | Use it for |
|---|---|---|---|---|
| **`any`** | Turn off checking | ✅ | ✅ | Escape hatch — avoid; `noImplicitAny` flags it |
| **`unknown`** | Unknown-but-typed | ✅ | ❌ (narrow first) | JSON, `catch`, third-party boundaries |
| **`never`** | No value possible | ❌ | ✅ (to everything) | Throwing functions, exhaustiveness checks |
| **`void`** | Returns nothing useful | only `undefined`/`null` | ❌ | Function return type |
| **`null`/`undefined`** | Absence | — | — | With `strictNullChecks`, must be in the union |

> ⚠️ **`void` vs `never`:** a `void` function **returns** — it just gives you nothing. A `never` function **never returns at all** (throws or loops forever). `() => void` completes; `() => never` doesn't.

---

## 15. HTML & CSS

The round closed here — general, and probably a quick check rather than depth. Have these five ready ([02 — HTML & CSS](./02-html-css.md) has the full set):

**Semantic HTML:** `<header>`, `<nav>`, `<main>`, `<article>`, `<section>`, `<aside>`, `<footer>` — they describe *meaning*, not appearance. Benefits: accessibility (screen readers navigate by landmark), SEO, and readable markup. `<div>` is what you use when nothing semantic fits.

**The box model:** content → padding → border → margin. `box-sizing: border-box` makes `width` include padding and border, which is why almost every project sets `*, *::before, *::after { box-sizing: border-box; }`.

**Positioning:** `static` (default) · `relative` (offset from its normal spot, still occupies space) · `absolute` (removed from flow, positioned against the nearest positioned ancestor) · `fixed` (against the viewport) · `sticky` (relative until it hits a threshold, then fixed).

**Flexbox vs Grid:** Flexbox is **one-dimensional** — a row or a column (navbars, toolbars, centring). Grid is **two-dimensional** — rows and columns together (page layouts, card grids). They compose: a Grid page with Flexbox inside each cell.

**Specificity:** inline (1000) > id (100) > class/attribute/pseudo-class (10) > element/pseudo-element (1). `!important` overrides everything and is a smell. Equal specificity → the later rule wins.

**Responsive:** `@media (max-width: 768px)`, relative units (`rem`, `%`, `vw`, `clamp()`), `<meta name="viewport" content="width=device-width, initial-scale=1">`, mobile-first (write the small layout, then add `min-width` queries).

---

## 📌 After this round — what to do

1. **Add these 15 to the recall log** ✅ (done — see [26 — Companies](./26-companies-asked-questions.md)).
2. **The repeats are now undeniable:** Observables vs Promises (4 rounds), interceptors (3), state management/NgRx (3), lifecycle hooks (4). These are not "possible" questions — they are **guaranteed**. They should be word-perfect.
3. **If Tech Mahindra calls back**, expect depth on 3 of these, not breadth. Most likely drilled: **NgRx effects with a real flow**, **switchMap vs mergeMap**, and **a live component**. Prepare [21 — NgRx](./21-ngrx.md) and [20 — RxJS](./20-rxjs-operators.md).
4. **The pattern from your log holds:** you clear the breadth sweep and lose the second question. For each of the 15 above, rehearse the answer **plus one trade-off or failure mode** — that second sentence is the whole difference.

---

**Related files:** [04 — Angular](./04-angular.md) · [20 — RxJS](./20-rxjs-operators.md) · [21 — NgRx](./21-ngrx.md) · [03 — TypeScript](./03-typescript.md) · [01 — JavaScript](./01-javascript.md) · [19 — JS Variables](./19-javascript-variables-easy.md) · [02 — HTML & CSS](./02-html-css.md) · [26 — Companies log](./26-companies-asked-questions.md)
