# RxJS — Interview Questions & Operator Guide

The deep-dive companion to the short RxJS section in [04-angular.md](./04-angular.md). Written for an **Angular developer with 5 years' experience** — every example is a real Angular scenario, not a toy.

> **Your stack context:** RoboGebra runs **RxJS 7.8** on Angular 16.2 (web/backoffice) and Angular 18.2 (mobile), with **service + `BehaviorSubject`** state — no NgRx. That's exactly the setup this file targets. See [18 — Technical Versions](./18-robogebra-technical-versions.md).

> **Why RxJS decides Angular interviews:** anyone can list lifecycle hooks. What separates a 2-year candidate from a 5-year one is `switchMap` vs `mergeMap`, why a `Subject` leaked memory, and what `shareReplay` actually does. Expect **20–30% of an Angular technical round** to be RxJS.

---

## Table of contents

- [⚡ The 60-second cheat sheet](#-the-60-second-cheat-sheet)
- [Part 1 — Fundamentals](#part-1--fundamentals)
- [Part 2 — Subjects](#part-2--subjects)
- [Part 3 — Creation operators](#part-3--creation-operators)
- [Part 4 — Transformation & the flattening four](#part-4--transformation--the-flattening-four)
- [Part 5 — Filtering operators](#part-5--filtering-operators)
- [Part 6 — Combination operators](#part-6--combination-operators)
- [Part 7 — Error handling](#part-7--error-handling)
- [Part 8 — Utility & multicasting](#part-8--utility--multicasting)
- [Part 9 — Unsubscribing & memory leaks](#part-9--unsubscribing--memory-leaks)
- [Part 10 — Real Angular scenarios](#part-10--real-angular-scenarios)
- [Part 11 — Common mistakes](#part-11--common-mistakes-interviewers-probe-for)
- [Part 12 — RxJS 7 changes](#part-12--rxjs-7-changes-you-must-know)
- [Part 13 — Output-based drills](#part-13--output-based-drills)
- [Part 14 — Rapid-fire](#part-14--rapid-fire)

---

## ⚡ The 60-second cheat sheet

| Question | Answer |
|---|---|
| **Observable vs Promise?** | Observable = multiple values, lazy, cancellable, operators. Promise = one value, eager, not cancellable |
| **Cold vs hot?** | Cold = new producer per subscriber (HTTP). Hot = shared producer (Subject, `fromEvent`) |
| **`Subject` vs `BehaviorSubject`?** | BehaviorSubject needs an initial value and replays the latest to new subscribers |
| **Search box operator?** | `debounceTime` → `distinctUntilChanged` → `switchMap` |
| **Save button operator?** | `exhaustMap` (ignore double-clicks) |
| **Ordered writes?** | `concatMap` |
| **Independent parallel calls?** | `mergeMap` |
| **Parallel HTTP, wait for all?** | `forkJoin` |
| **React whenever any input changes?** | `combineLatest` |
| **How do you unsubscribe?** | `async` pipe first; else `takeUntilDestroyed()` (Angular 16+) or `takeUntil(destroy$)` |
| **Cache an HTTP result?** | `shareReplay({ bufferSize: 1, refCount: true })` |
| **Retry a failed call?** | `retry({ count: 3, delay: 1000 })` (RxJS 7) |
| **Observable → Promise?** | `firstValueFrom()` / `lastValueFrom()` — `toPromise()` is deprecated |

---

# Part 1 — Fundamentals

### Q: What is RxJS and why does Angular use it?

RxJS is a library for **reactive programming with observable streams**. Angular uses it because most of what an app does is asynchronous *and repeated over time*: HTTP responses, router events, form value changes, DOM events, WebSocket messages.

Angular ships RxJS into `HttpClient`, `Router` (`events`, `params`, `queryParams`), reactive forms (`valueChanges`, `statusChanges`), and `EventEmitter`.

> 💬 *"A Promise models one future value. RxJS models a stream of values over time, with operators to transform, filter and combine those streams declaratively — and, crucially, to cancel them."*

---

### Q: Observable vs Promise?

| | Observable | Promise |
|---|---|---|
| Values | **Many** over time | Exactly **one** |
| Execution | **Lazy** — nothing runs until `.subscribe()` | **Eager** — runs the moment it's created |
| Cancellable | ✅ `.unsubscribe()` | ❌ No |
| Operators | ✅ 100+ (`map`, `filter`, `retry`…) | ❌ Only `.then`/`.catch`/`.finally` |
| Retry | ✅ Built in | ❌ Manual |
| Sync or async | Either | Always async |

```ts
// Promise — fires immediately, even if nobody uses it
const p = fetch('/api/quiz');

// Observable — nothing happens until you subscribe
const o$ = this.http.get('/api/quiz');
o$.subscribe();   // ← only NOW does the request go out
```

> 🎯 **The laziness point wins interviews.** *"Because observables are lazy, `this.http.get(...)` on its own sends no request. That's also why calling `subscribe()` twice fires two HTTP calls — a bug people hit constantly."*

---

### Q: What are the three Observer callbacks?

```ts
source$.subscribe({
  next:     value => console.log('value:', value),   // 0..n times
  error:    err   => console.error(err),             // 0 or 1 time — then stream is DEAD
  complete: ()    => console.log('done'),            // 0 or 1 time — then stream is DEAD
});
```

**Contract:** `next*` then **either** `error` **or** `complete`, never both, never anything after.

> ⚠️ Once a stream errors, it's finished. New values will not arrive. That's why an error inside a `valueChanges` pipe silently kills your form subscription forever — see [Part 7](#part-7--error-handling).

---

### Q: Cold vs hot observables?

**Cold** — the producer is created *per subscriber*. Everyone gets their own independent execution.

```ts
const cold$ = this.http.get('/api/chapters');
cold$.subscribe();   // HTTP request #1
cold$.subscribe();   // HTTP request #2  ← two separate calls!
```

**Hot** — one producer, shared by all subscribers. Late subscribers miss earlier values.

```ts
const hot$ = fromEvent(document, 'click');   // the clicks happen whether you subscribe or not
const subject$ = new Subject();              // Subjects are hot
```

**Turn cold into hot:** `share()`, `shareReplay()`.

> 💬 *"HTTP observables are cold and unicast — each subscribe re-runs the request. If two components need the same data I add `shareReplay({bufferSize:1, refCount:true})` so one request is shared and replayed."*

---

### Q: Unicast vs multicast?

- **Unicast** — one producer per subscriber (a plain `Observable`)
- **Multicast** — one producer, many subscribers (a `Subject`, or any observable through `share()`)

---

# Part 2 — Subjects

A **Subject is both an Observable and an Observer** — you can `subscribe()` to it *and* push values into it with `next()`.

### The four Subject types

| Type | Initial value | New subscriber gets | Typical use |
|---|---|---|---|
| **`Subject`** | ❌ None | Only values emitted **after** subscribing | Events, notifications, `destroy$` |
| **`BehaviorSubject`** | ✅ **Required** | The **latest** value immediately | ⭐ Shared state in services |
| **`ReplaySubject`** | ❌ None | The last **N** values (buffer) | History, late-joining components |
| **`AsyncSubject`** | ❌ None | Only the **final** value, on complete | Rare — one-shot results |

```ts
// Subject — late subscriber misses everything
const s = new Subject<number>();
s.next(1);
s.subscribe(v => console.log('A:', v));   // hears nothing yet
s.next(2);                                 // A: 2

// BehaviorSubject — late subscriber gets the current value
const b = new BehaviorSubject<number>(0);
b.next(1);
b.subscribe(v => console.log('B:', v));   // B: 1  ← immediately
b.next(2);                                 // B: 2

// ReplaySubject — late subscriber gets the buffer
const r = new ReplaySubject<number>(2);
r.next(1); r.next(2); r.next(3);
r.subscribe(v => console.log('R:', v));   // R: 2, R: 3
```

### ⭐ The service + BehaviorSubject pattern (your actual RoboGebra state management)

```ts
@Injectable({ providedIn: 'root' })
export class StudyListService {
  private studyListSubject = new BehaviorSubject<StudyItem[]>([]);

  // Expose as Observable so components can't call .next() from outside
  readonly studyList$ = this.studyListSubject.asObservable();

  get current(): StudyItem[] {
    return this.studyListSubject.value;      // synchronous read
  }

  addItem(item: StudyItem): void {
    this.studyListSubject.next([...this.current, item]);   // immutable update
  }
}
```

```html
<!-- component template — no manual subscription at all -->
<div *ngFor="let item of studyListService.studyList$ | async">{{ item.title }}</div>
```

> 💬 **Say this:** *"In RoboGebra we manage shared state with services holding a `BehaviorSubject`, exposed as a read-only observable via `asObservable()` so only the service can push. Components consume it with the `async` pipe, which also handles unsubscription. It gave us real-time sync across the Study List without the ceremony of NgRx."*

**Why `asObservable()`?** Without it, any component could call `service.studyListSubject.next(...)` and bypass your service logic — an encapsulation leak. Same reasoning as private fields in Java ([17 — SOLID](./17-solid-design-patterns.md)).

---

# Part 3 — Creation operators

| Operator | What it does | Example |
|---|---|---|
| **`of(...)`** | Emits the given values, then completes | `of(1, 2, 3)` |
| **`from(...)`** | Converts array / Promise / iterable to an Observable | `from([1,2,3])`, `from(promise)` |
| **`fromEvent`** | DOM events as a stream | `fromEvent(input, 'keyup')` |
| **`interval(n)`** | `0,1,2,…` every n ms, forever | `interval(1000)` |
| **`timer(d, p?)`** | Emits after delay `d`, then every `p` ms | `timer(0, 5000)` — poll immediately then every 5s |
| **`EMPTY`** | Completes immediately, emits nothing | Error-recovery fallback |
| **`NEVER`** | Never emits, never completes | Testing |
| **`throwError(() => e)`** | Errors immediately | Error simulation |
| **`defer(fn)`** | Creates the observable **per subscriber**, at subscribe time | Lazy/fresh values |
| **`range(s, c)`** | A range of numbers | `range(1, 5)` → 1,2,3,4,5 |

> ⚠️ **`of([1,2,3])` vs `from([1,2,3])`** — a favourite trick question.
> `of([1,2,3])` emits **one value: the array**. `from([1,2,3])` emits **three values: 1, 2, 3**.

```ts
// defer — why it exists
const now$   = of(Date.now());       // timestamp fixed at creation, same for every subscriber
const fresh$ = defer(() => of(Date.now()));   // evaluated at each subscribe
```

---

# Part 4 — Transformation & the flattening four

### Simple transformations

| Operator | Does |
|---|---|
| **`map`** | Transform each value — `map(u => u.name)` |
| **`scan`** | Like `reduce`, but emits the running accumulator every time |
| **`reduce`** | Emits only the final accumulated value on complete |
| **`toArray`** | Collects all values into one array on complete |
| **`bufferTime(n)`** | Groups values emitted within n ms into arrays |
| **`pairwise`** | Emits `[previous, current]` |

```ts
// scan — a running total, emits every step
of(1, 2, 3).pipe(scan((acc, v) => acc + v, 0)).subscribe(console.log);
// 1, 3, 6

// reduce — only the final value
of(1, 2, 3).pipe(reduce((acc, v) => acc + v, 0)).subscribe(console.log);
// 6
```

---

## 🔥 switchMap vs mergeMap vs concatMap vs exhaustMap

**This is the single most-asked RxJS question.** All four "flatten" an inner observable — they differ only in *what happens when a new outer value arrives while an inner one is still running*.

### The one-line rule for each

| Operator | When a new value arrives while inner is active | Memory hook |
|---|---|---|
| **`switchMap`** | **Cancels** the previous inner, switches to the new one | *"switch to the latest"* |
| **`mergeMap`** | **Runs both** concurrently, order not guaranteed | *"merge everything, all at once"* |
| **`concatMap`** | **Queues** the new one until the current finishes | *"concat = one after another"* |
| **`exhaustMap`** | **Ignores** the new one until the current finishes | *"exhaust the current first"* |

### Marble diagrams

Outer emits `a` then `b`; each inner takes time to produce `1` then `2`.

```
outer:      --a-------b------------------

switchMap:  ----a1--X   ← a's inner CANCELLED when b arrives
                    --b1--b2|
result:     ----a1------b1--b2|

mergeMap:   ----a1--a2|
                    --b1--b2|          ← both run at once
result:     ----a1--a2b1--b2|          (interleaved)

concatMap:  ----a1--a2|
                       --b1--b2|       ← b WAITS for a to finish
result:     ----a1--a2----b1--b2|      (strict order)

exhaustMap: ----a1--a2|
                    ✗                  ← b DISCARDED, a was still running
result:     ----a1--a2|
```

### When to use each — with the real Angular case

**`switchMap` — typeahead search** ⭐ most common
```ts
this.searchControl.valueChanges.pipe(
  debounceTime(300),
  distinctUntilChanged(),
  switchMap(term => this.api.search(term)),   // cancels the in-flight request
  takeUntilDestroyed(this.destroyRef)
).subscribe(results => this.results = results);
```
*Why:* if the user types "alge" then "algebra", the response for "alge" is worthless. `switchMap` cancels it — which for `HttpClient` **actually aborts the HTTP request**. It also prevents out-of-order responses overwriting newer results.

**`concatMap` — ordered writes**
```ts
this.saveQueue$.pipe(
  concatMap(change => this.api.saveProgress(change))
).subscribe();
```
*Why:* progress updates must hit the server in the order they happened. Never `switchMap` a save — you'd cancel real user data.

**`mergeMap` — independent parallel work**
```ts
from(fileList).pipe(
  mergeMap(file => this.api.upload(file), 3)   // 3 concurrent uploads max
).subscribe();
```
*Why:* uploads don't depend on each other and order doesn't matter. The second argument caps concurrency.

**`exhaustMap` — submit button / login**
```ts
fromEvent(this.submitBtn.nativeElement, 'click').pipe(
  exhaustMap(() => this.api.submitQuiz(this.answers))
).subscribe();
```
*Why:* an impatient user clicks Submit four times. `exhaustMap` ignores clicks 2–4 while the first request is in flight. No duplicate submissions, no disabled-button plumbing.

> 💬 **The answer that lands:** *"They all flatten inner observables; the difference is the cancellation strategy. `switchMap` cancels the previous — right for search, wrong for saves because you'd cancel a real write. `concatMap` queues and preserves order — right for saves. `mergeMap` runs everything in parallel — right for independent uploads. `exhaustMap` ignores new emissions while one is in flight — right for a submit button, since it kills double-submits for free."*

> ⚠️ **Danger:** using `switchMap` for a POST/PUT can cancel a request the server has already started processing, leaving you inconsistent. **Default to `concatMap` for writes and `switchMap` for reads.**

---

# Part 5 — Filtering operators

| Operator | What it does |
|---|---|
| **`filter(fn)`** | Only pass values matching the predicate |
| **`take(n)`** | Take the first n, then **complete** (auto-unsubscribes 🎉) |
| **`first()` / `last()`** | First/last value then complete (errors if empty — `first(pred, default)` avoids that) |
| **`takeUntil(notifier$)`** | Emit until `notifier$` emits — ⭐ the unsubscribe pattern |
| **`takeWhile(fn, inclusive?)`** | Emit while the predicate holds, then complete |
| **`skip(n)` / `skipWhile` / `skipUntil`** | Ignore values |
| **`debounceTime(ms)`** | Wait for a pause of `ms` — emits the **last** value ⭐ search |
| **`throttleTime(ms)`** | Emit the **first** value, then ignore for `ms` ⭐ scroll/resize |
| **`auditTime(ms)`** | Ignore for `ms`, then emit the **latest** value |
| **`distinctUntilChanged()`** | Skip if identical to the previous value |
| **`distinctUntilKeyChanged('id')`** | Same, comparing one key |
| **`sample(notifier$)`** | Emit the latest value whenever the notifier fires |

### debounceTime vs throttleTime — know the difference

```
input:          --a-b-c--------d-e--------

debounceTime:   ---------c---------e------   ← waits for silence, emits the LAST
throttleTime:   --a------------d----------   ← emits the FIRST, then blocks
```

- **`debounceTime`** — search box. You want the final query after they stop typing.
- **`throttleTime`** — scroll/resize/mousemove. You want regular sampling, not the end.

> 🎯 A crisp way to say it: *"Debounce waits for the user to stop; throttle limits the rate while they continue."*

### Why `distinctUntilChanged` matters

```ts
searchControl.valueChanges.pipe(
  debounceTime(300),
  distinctUntilChanged(),      // ← without this, typing "a", backspace, "a" refires the same search
  switchMap(t => this.api.search(t))
)
```
For objects it compares by **reference**, so pass a comparator: `distinctUntilChanged((a, b) => a.id === b.id)`.

---

# Part 6 — Combination operators

**Very commonly asked as "difference between forkJoin, combineLatest and zip".** Here's the table that separates them:

| Operator | Emits when | How many times | Needs completion? |
|---|---|---|---|
| **`forkJoin`** | **All** sources **complete** | **Once** — array/object of last values | ✅ Yes |
| **`combineLatest`** | **Any** source emits (after all have emitted ≥1) | **Many** | ❌ No |
| **`zip`** | All have emitted at the **same index** | Many — paired by index | ❌ No |
| **`merge`** | Any source emits — pass-through | Many | ❌ No |
| **`concat`** | Sequentially, after the previous completes | Many | ✅ Yes |
| **`withLatestFrom`** | **Source** emits (samples the others) | Many | ❌ No |
| **`race`** | Only the **first** source to emit; others unsubscribed | Many | ❌ No |
| **`startWith(v)`** | Prepends a value immediately | — | — |

### Marble comparison

```
A:  --1-----2--------3|
B:  ----a------b------|

forkJoin:       -----------------[3,b]|     ← ONE emission, on completion
combineLatest:  ----[1,a]-[2,a]-[2,b]-[3,b] ← every change
zip:            ----[1,a]--[2,b]|           ← paired by index
merge:          --1-a---2--b-----3|         ← flat pass-through
withLatestFrom  --------[2,a]----[3,b]      ← only when A emits
  (A source)
```

### `forkJoin` — parallel HTTP, wait for all ⭐

```ts
// Loading a RoboGebra dashboard: three independent calls in parallel
forkJoin({
  profile:  this.api.getProfile(),
  progress: this.api.getProgress(),
  timeline: this.api.getTimeline()
}).subscribe(({ profile, progress, timeline }) => {
  this.profile = profile;
  this.progress = progress;
  this.timeline = timeline;
});
```

> ⚠️ **Three `forkJoin` gotchas interviewers love:**
> 1. **If any source errors, the whole thing errors** and you lose the successful results. Guard each: `this.api.getProgress().pipe(catchError(() => of(null)))`.
> 2. **If a source completes without emitting, `forkJoin` emits nothing at all.**
> 3. **It never fires with a `Subject` or `valueChanges`** — those don't complete. Use `combineLatest` there.

### `combineLatest` — react to any change ⭐

```ts
// Re-filter whenever the search term, chapter or difficulty changes
combineLatest([
  this.searchTerm$,
  this.selectedChapter$,
  this.difficulty$
]).pipe(
  debounceTime(200),
  switchMap(([term, chapter, difficulty]) =>
    this.api.getExercises({ term, chapter, difficulty }))
).subscribe(list => this.exercises = list);
```

> ⚠️ **`combineLatest` emits nothing until *every* source has emitted at least once.** The usual fix is `startWith(initialValue)` on each — or use `BehaviorSubject`s, which always have a value. This is the #1 "why isn't my combineLatest firing?" bug.

### `withLatestFrom` — one stream drives

```ts
// Only when Save is clicked, grab whatever the form currently holds
fromEvent(saveBtn, 'click').pipe(
  withLatestFrom(this.form.valueChanges),
  exhaustMap(([_, formValue]) => this.api.save(formValue))
).subscribe();
```
Form changes alone don't trigger anything — only the click does.

### `merge` vs `concat`

```ts
merge(a$, b$);    // both start now, interleaved
concat(a$, b$);   // b$ doesn't start until a$ completes
```

---

# Part 7 — Error handling

### `catchError` — recover or rethrow

```ts
this.api.getQuiz(id).pipe(
  catchError(err => {
    console.error(err);
    return of(null);              // ✅ recover with a fallback value
    // return EMPTY;              // ✅ or emit nothing and complete quietly
    // return throwError(() => err); // ✅ or rethrow for an outer handler
  })
).subscribe(quiz => this.quiz = quiz);
```

**`of(fallback)` vs `EMPTY`:** `of(null)` emits one value then completes (your `next` handler runs). `EMPTY` completes with no value at all (your `next` handler never runs).

### 🔥 The most important error-handling question: WHERE do you put `catchError`?

```ts
// ❌ WRONG — one failed search kills the stream forever.
// The user types again and nothing happens.
this.search$.pipe(
  switchMap(term => this.api.search(term)),
  catchError(() => of([]))          // outer pipe — stream is dead after this fires
).subscribe();

// ✅ RIGHT — catch INSIDE the inner observable.
// The outer stream survives; the next keystroke still works.
this.search$.pipe(
  switchMap(term =>
    this.api.search(term).pipe(catchError(() => of([])))   // ← inside
  )
).subscribe();
```

> 💬 **This answer marks you as senior:** *"`catchError` placed in the outer pipe kills the source stream — an error is terminal, so no further values arrive. For a long-lived stream like `valueChanges`, I put `catchError` inside the `switchMap`'s inner observable so a failed request doesn't take the whole stream down."*

### `retry` — RxJS 7 syntax

```ts
this.api.getData().pipe(
  retry({ count: 3, delay: 1000 }),                     // 3 retries, 1s apart
  catchError(err => { this.toast.error('Failed'); return EMPTY; })
).subscribe();

// exponential backoff
retry({ count: 3, delay: (err, retryCount) => timer(retryCount * 1000) })
```
> ⚠️ `retryWhen` is **deprecated in RxJS 7** — use `retry({ delay })`.

### `finalize` — always runs

```ts
this.loading = true;
this.api.getData().pipe(
  finalize(() => this.loading = false)   // runs on complete, error, AND unsubscribe
).subscribe();
```
The RxJS equivalent of `finally`. Perfect for spinners.

---

# Part 8 — Utility & multicasting

| Operator | Purpose |
|---|---|
| **`tap`** | Side effects (logging, setting a flag) without changing the stream |
| **`delay(ms)`** | Time-shift emissions |
| **`finalize`** | Cleanup on complete/error/unsubscribe |
| **`share()`** | Multicast — one execution shared across subscribers |
| **`shareReplay({bufferSize, refCount})`** | Multicast + replay to late subscribers ⭐ caching |
| **`toArray()`** | Collect everything into one array |

### `tap` — debugging tool

```ts
this.api.getQuiz(id).pipe(
  tap(() => this.loading = true),
  tap(quiz => console.log('received', quiz)),
  map(quiz => quiz.questions),
  finalize(() => this.loading = false)
).subscribe();
```
`tap` never modifies values — if you're changing something, use `map`.

### `shareReplay` — caching an HTTP call ⭐

```ts
@Injectable({ providedIn: 'root' })
export class ConfigService {
  readonly config$ = this.http.get<AppConfig>('/api/config').pipe(
    shareReplay({ bufferSize: 1, refCount: true })
  );
}
```
Ten components subscribing → **one** HTTP request, and every late subscriber immediately gets the cached response.

> ⚠️ **The `refCount` trap — a genuinely senior detail.**
> `shareReplay(1)` (without `refCount`) keeps the source subscription alive **forever**, even after every subscriber has left. With an `interval` or a WebSocket, that's a permanent memory leak.
> `shareReplay({ bufferSize: 1, refCount: true })` unsubscribes from the source when the last subscriber leaves.
> **Rule:** `refCount: true` for long-lived/infinite sources; plain `shareReplay(1)` is acceptable for a one-shot HTTP call you genuinely want cached for the app's lifetime.

---

# Part 9 — Unsubscribing & memory leaks

### Q: What happens if you don't unsubscribe?

The subscription stays alive after the component is destroyed. The callback keeps running, keeps a reference to the dead component (so it's never garbage-collected), and can throw errors on destroyed views. With a route the user visits repeatedly, subscriptions **stack up** — 10 visits, 10 live subscriptions all firing.

### What you DON'T need to unsubscribe from

- `HttpClient` calls — they complete after one emission
- Anything ending in `take(1)`, `first()`, `takeUntil(...)`
- Anything consumed via the `async` pipe
- `ActivatedRoute` params **in a routed component** (Angular cleans these up) — but do unsubscribe in a non-routed child

### What you MUST unsubscribe from

- `Subject` / `BehaviorSubject` in services
- `valueChanges` / `statusChanges`
- `interval` / `timer` / `fromEvent`
- `Router.events`
- Anything infinite

---

### ✅ Pattern 1 — `async` pipe (best; use it whenever you can)

```ts
quiz$ = this.api.getQuiz(this.id);
```
```html
<div *ngIf="quiz$ | async as quiz">{{ quiz.title }}</div>
```
Angular subscribes and unsubscribes for you. No `ngOnDestroy` needed.

> ⚠️ Two `async` pipes on the same cold observable = two HTTP calls. Fix with `shareReplay(1)` or `*ngIf="x$ | async as x"` and reuse `x`.

### ✅ Pattern 2 — `takeUntilDestroyed()` (Angular 16+, the modern way)

```ts
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

export class QuizComponent {
  private destroyRef = inject(DestroyRef);

  ngOnInit() {
    this.service.data$.pipe(
      takeUntilDestroyed(this.destroyRef)     // destroyRef needed outside the injection context
    ).subscribe(d => this.data = d);
  }
}
```
In a **field initializer or constructor** you can omit the argument — it picks up the injection context automatically:
```ts
readonly data$ = this.service.data$.pipe(takeUntilDestroyed());
```
> 🎯 **Both your apps can use this** — Angular 16.2 (web/backoffice) and 18.2 (mobile). Mentioning it shows you're current.

### ✅ Pattern 3 — `takeUntil(destroy$)` (the classic, works everywhere)

```ts
export class QuizComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  ngOnInit() {
    this.service.data$.pipe(takeUntil(this.destroy$)).subscribe(d => this.data = d);
    this.form.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(v => this.onChange(v));
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();     // don't forget — completes the Subject itself
  }
}
```
> ⚠️ **`takeUntil` must be LAST in the pipe.** Any operator after it (especially `switchMap` or `shareReplay`) can create a new subscription that `takeUntil` no longer guards.

### Pattern 4 — collecting Subscriptions

```ts
private subs = new Subscription();

ngOnInit() {
  this.subs.add(this.a$.subscribe());
  this.subs.add(this.b$.subscribe());
}
ngOnDestroy() { this.subs.unsubscribe(); }   // unsubscribes all children
```
Fine, but more manual bookkeeping than `takeUntil`.

> 💬 **Interview answer:** *"My order of preference is: `async` pipe first, because Angular handles the lifecycle; `takeUntilDestroyed` when I need an imperative subscription — we're on Angular 16 and 18 so it's available; and `takeUntil` with a destroy Subject on anything older. I avoid manual `unsubscribe()` calls because they're easy to forget when a component grows."*

---

# Part 10 — Real Angular scenarios

Rehearse these — they're what "have you used RxJS in production?" is really asking.

### 1. Typeahead search (the classic — memorize it)

```ts
results$ = this.searchControl.valueChanges.pipe(
  debounceTime(300),                          // wait for a typing pause
  map(term => term?.trim() ?? ''),
  distinctUntilChanged(),                     // ignore no-op changes
  filter(term => term.length >= 2),           // don't search 1 character
  tap(() => this.loading = true),
  switchMap(term => this.api.search(term).pipe(
    catchError(() => of([])),                 // ← INSIDE, so the stream survives
    finalize(() => this.loading = false)
  )),
  takeUntilDestroyed()
);
```
Be ready to justify **every operator** — that's the real question.

### 2. Dependent dropdowns (chapter → exercise)

```ts
this.chapterControl.valueChanges.pipe(
  filter(Boolean),
  tap(() => this.exerciseControl.reset()),
  switchMap(chapterId => this.api.getExercises(chapterId)),
  takeUntilDestroyed()
).subscribe(list => this.exercises = list);
```

### 3. Polling with a stop condition

```ts
timer(0, 5000).pipe(                          // fire immediately, then every 5s
  switchMap(() => this.api.getSolutionStatus(jobId)),
  takeWhile(res => res.status === 'PROCESSING', true),   // `true` = emit the final one too
  takeUntilDestroyed()
).subscribe(res => this.status = res.status);
```
Real use: waiting on the AI solution pipeline to finish.

### 4. Prevent double-submit

```ts
this.submitClicks$.pipe(
  exhaustMap(() => this.api.submitQuiz(this.answers).pipe(
    catchError(err => { this.toast.error('Submit failed'); return EMPTY; })
  )),
  takeUntilDestroyed()
).subscribe(result => this.router.navigate(['/result', result.id]));
```

### 5. Parallel dashboard load with per-call fallback

```ts
forkJoin({
  profile:  this.api.getProfile(),
  progress: this.api.getProgress().pipe(catchError(() => of(null))),
  timeline: this.api.getTimeline().pipe(catchError(() => of([])))
}).pipe(takeUntilDestroyed())
  .subscribe(data => this.dashboard = data);
```
The `catchError` per source means one failing widget doesn't blank the whole dashboard.

### 6. Route param → data

```ts
quiz$ = this.route.paramMap.pipe(
  map(params => params.get('id')!),
  distinctUntilChanged(),
  switchMap(id => this.api.getQuiz(id)),
  shareReplay({ bufferSize: 1, refCount: true })
);
```
`switchMap` cancels the previous fetch when the user navigates quickly between quizzes.

### 7. Auth-token refresh in an HTTP interceptor

```ts
intercept(req: HttpRequest<any>, next: HttpHandler) {
  return next.handle(this.addToken(req)).pipe(
    catchError(err => {
      if (err.status === 401) {
        return this.auth.refreshToken().pipe(
          switchMap(newToken => next.handle(this.addToken(req, newToken))),
          catchError(() => { this.auth.logout(); return EMPTY; })
        );
      }
      return throwError(() => err);
    })
  );
}
```
Your `header-authorization.interceptor.ts` does this shape of work — good to reference.

---

# Part 11 — Common mistakes interviewers probe for

### ❌ 1. Nested subscribes

```ts
// WRONG — callback hell, no cancellation, hard to handle errors
this.route.params.subscribe(params => {
  this.api.getUser(params['id']).subscribe(user => {
    this.api.getOrders(user.id).subscribe(orders => { ... });
  });
});

// RIGHT
this.route.params.pipe(
  switchMap(p => this.api.getUser(p['id'])),
  switchMap(user => this.api.getOrders(user.id)),
  takeUntilDestroyed()
).subscribe(orders => ...);
```
> 🎯 **If you spot and name this, it reads as real experience.** *"A subscribe inside a subscribe is the RxJS equivalent of callback hell — you lose cancellation and centralised error handling. Flatten it with switchMap."*

### ❌ 2. Subscribing just to reassign a field

```ts
// Meh
ngOnInit() { this.api.getQuiz(id).subscribe(q => this.quiz = q); }

// Better — let the async pipe manage it
quiz$ = this.api.getQuiz(id);
```

### ❌ 3. Multiple subscriptions to a cold observable

```html
<div>{{ (user$ | async)?.name }}</div>
<div>{{ (user$ | async)?.email }}</div>   <!-- ❌ two HTTP calls -->

<ng-container *ngIf="user$ | async as user">   <!-- ✅ one -->
  <div>{{ user.name }}</div><div>{{ user.email }}</div>
</ng-container>
```

### ❌ 4. `catchError` in the outer pipe (see [Part 7](#part-7--error-handling))

### ❌ 5. `forkJoin` with a non-completing source
`forkJoin([this.mySubject$, ...])` never emits, because a Subject never completes. Use `combineLatest`, or `take(1)` each source.

### ❌ 6. `combineLatest` that never fires
Because one source hasn't emitted yet. Add `startWith(...)` or use `BehaviorSubject`.

### ❌ 7. Operators after `takeUntil`

### ❌ 8. `shareReplay(1)` on an infinite source without `refCount`

### ❌ 9. Mutating state inside a BehaviorSubject
```ts
this.subject.value.push(item);          // ❌ no emission, subscribers never update
this.subject.next([...this.subject.value, item]);   // ✅
```

---

# Part 12 — RxJS 7 changes you must know

You're on **RxJS 7.8** — know what changed from 6.

| Change | Old (RxJS 6) | New (RxJS 7) |
|---|---|---|
| Observable → Promise | `toPromise()` **deprecated** | **`firstValueFrom(obs$)`** / **`lastValueFrom(obs$)`** |
| Retry with delay | `retryWhen(...)` **deprecated** | **`retry({ count, delay })`** |
| Property extraction | `pluck('a','b')` deprecated | `map(x => x.a.b)` |
| `combineLatest(a, b)` | multiple args deprecated | `combineLatest([a, b])` |
| Bundle size | — | ~40% smaller, better tree-shaking |
| Types | — | Much stronger TypeScript inference |

```ts
// Why toPromise() was replaced: it resolved to `undefined` if the observable
// completed without emitting — silently. The new APIs are explicit:
const value = await firstValueFrom(this.api.getConfig());   // rejects if empty
const value = await lastValueFrom(this.api.getConfig());
const value = await firstValueFrom(src$, { defaultValue: null });  // no throw
```

> 💬 Dropping this in unprompted is a strong signal: *"We're on RxJS 7.8, so `toPromise()` is deprecated — we use `firstValueFrom`, which fails loudly on an empty stream instead of silently resolving to `undefined`."*

---

# Part 13 — Output-based drills

### Drill 1
```ts
of(1, 2, 3).pipe(map(x => x * 2), filter(x => x > 2)).subscribe(console.log);
```
<details><summary>Answer</summary>

`4`, `6` — 1→2 is filtered out (not `> 2`).
</details>

### Drill 2
```ts
const s = new Subject<number>();
s.subscribe(v => console.log('A', v));
s.next(1);
s.subscribe(v => console.log('B', v));
s.next(2);
```
<details><summary>Answer</summary>

```
A 1
A 2
B 2
```
B subscribed after `1` and a plain `Subject` doesn't replay.
</details>

### Drill 3
```ts
const b = new BehaviorSubject<number>(0);
b.next(1);
b.subscribe(v => console.log('X', v));
b.next(2);
```
<details><summary>Answer</summary>

```
X 1     ← the current value, immediately on subscribe
X 2
```
</details>

### Drill 4
```ts
of([1, 2, 3]).subscribe(console.log);
from([1, 2, 3]).subscribe(console.log);
```
<details><summary>Answer</summary>

```
[1, 2, 3]     ← of emits the array as ONE value
1
2
3             ← from emits each element
```
</details>

### Drill 5
```ts
const a$ = interval(1000).pipe(take(3));   // 0,1,2 at 1s,2s,3s
const b$ = interval(1500).pipe(take(2));   // 0,1  at 1.5s,3s
forkJoin([a$, b$]).subscribe(console.log);
```
<details><summary>Answer</summary>

`[2, 1]` at 3s — one emission with the **last** value of each, only once both complete.
</details>

### Drill 6
```ts
concat(of(1, 2), of(3, 4)).subscribe(console.log);
merge(of(1, 2), of(3, 4)).subscribe(console.log);
```
<details><summary>Answer</summary>

Both print `1 2 3 4` here, because `of` is synchronous. With async sources `merge` would interleave and `concat` would not — that's the real distinction, and a good thing to point out.
</details>

### Drill 7
```ts
of(1, 2, 3).pipe(
  tap(x => console.log('tap', x)),
  map(x => x * 10)
).subscribe(x => console.log('sub', x));
```
<details><summary>Answer</summary>

```
tap 1 / sub 10
tap 2 / sub 20
tap 3 / sub 30
```
Interleaved, not grouped — each value flows through the whole pipe before the next one starts.
</details>

### Drill 8
```ts
const src$ = of(1, 2, 3).pipe(tap(() => console.log('side effect')));
src$.subscribe();
src$.subscribe();
```
<details><summary>Answer</summary>

`side effect` prints **6 times** — cold observable, one full execution per subscriber. Add `share()` to make it 3.
</details>

---

# Part 14 — Rapid-fire

| Question | Answer |
|---|---|
| Observable vs Promise | Many values, lazy, cancellable vs one value, eager, not cancellable |
| Cold vs hot | Producer per subscriber vs shared producer |
| Subject vs BehaviorSubject | BehaviorSubject has an initial value and replays the latest |
| BehaviorSubject vs ReplaySubject | Latest one vs last N |
| `.value` on a Subject? | Only `BehaviorSubject` has it |
| `asObservable()` — why? | Stops consumers calling `.next()` — encapsulation |
| switchMap | Cancels previous — search/reads |
| mergeMap | All in parallel — independent uploads |
| concatMap | Queued, order preserved — writes |
| exhaustMap | Ignores new while busy — submit buttons |
| Never `switchMap` on a… | POST/PUT — you'd cancel a real write |
| forkJoin | Waits for all to **complete**, emits once |
| combineLatest | Emits on **any** change, after all emitted once |
| zip | Pairs by index |
| withLatestFrom | Source drives, samples the rest |
| debounceTime | Waits for a pause, emits the last — search |
| throttleTime | Emits the first, then blocks — scroll |
| distinctUntilChanged | Skips consecutive duplicates |
| take(1) | One value then completes — auto-unsubscribes |
| takeUntil | Emit until the notifier fires — must be **last** |
| catchError placement | **Inside** the inner observable for long-lived streams |
| retry (RxJS 7) | `retry({ count: 3, delay: 1000 })` |
| finalize | Runs on complete, error **and** unsubscribe |
| tap | Side effects only, never transforms |
| share vs shareReplay | Multicast vs multicast + replay to late subscribers |
| shareReplay trap | Needs `refCount: true` on infinite sources or it leaks |
| Best unsubscribe | `async` pipe → `takeUntilDestroyed()` → `takeUntil(destroy$)` |
| Nested subscribe | Anti-pattern — flatten with `switchMap` |
| toPromise() | Deprecated — `firstValueFrom` / `lastValueFrom` |
| retryWhen | Deprecated in RxJS 7 |
| Angular 16 unsubscribe helper | `takeUntilDestroyed()` from `@angular/core/rxjs-interop` |

---

## ⏱️ Two-day drill

**Day 1** — Parts 1, 2, 4, 9. Be able to draw the four marble diagrams for `switchMap`/`mergeMap`/`concatMap`/`exhaustMap` from memory and give the Angular use case for each. Write the typeahead pipeline from scratch, no notes.

**Day 2** — Parts 5–8 and 10. Memorize the forkJoin vs combineLatest vs zip table. Rehearse three real RoboGebra scenarios out loud (Study List `BehaviorSubject`, search with `switchMap`, dashboard `forkJoin`), 45 seconds each.

> **The three answers that most often decide the round:** the four flattening operators and when each is wrong; where `catchError` goes and why; and how you prevent subscription leaks. Everything else is detail.
