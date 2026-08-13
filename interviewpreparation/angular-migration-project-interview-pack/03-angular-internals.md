# 03 — Angular Internals (what they probe AFTER the migration answer)

Once you give a solid migration answer, interviewers test whether you *actually understand the internals* the migration touched — "you mentioned Ivy, what is it?", "you mentioned change detection, how does it work?". This file is those follow-ups. Every answer is anchored to *"this is what I had to understand to migrate safely."*

---

## Q1. View Engine vs Ivy — what actually changed in v9?

**Answer.** They're two generations of Angular's **compilation + rendering pipeline**.

- **View Engine** (pre-v9) compiled components into a shared, monolithic-ish factory format. Downsides: larger bundles, weaker tree-shaking, harder-to-read errors, and the compiler had global knowledge requirements.
- **Ivy** (default v9) compiles each component into **self-contained, locally-scoped instructions**. Benefits:
  - **Better tree-shaking** — unused Angular features get dropped, so **smaller bundles**.
  - **Faster incremental builds** — locality means recompiling one component doesn't need the whole world.
  - **Better debugging** — readable stack traces, `ng.` debug APIs in the console.
  - **AOT everywhere** — Ivy made **Ahead-of-Time** compilation the practical default, even in dev.

**Why it mattered for my migration:** Ivy was the highest-risk hop because **libraries built for View Engine** had to be made Ivy-compatible. Angular shipped **`ngcc` (Angular Compatibility Compiler)** to transform View-Engine library builds into Ivy format at install time, which bridged most libs — but any library that was truly incompatible had to be upgraded, replaced, or isolated. Understanding *why* Ivy could break a library (it consumes different compiled metadata) is what let me triage each dependency instead of guessing.

**AOT vs JIT (the natural sub-follow-up):** **JIT** compiles templates in the browser at runtime (bigger, slower start, ships the compiler); **AOT** compiles at build time (smaller, faster start, template errors caught at build, no compiler shipped). Ivy made AOT cheap enough to be the default, which is a big reason builds got faster.

---

## Q2. How does Angular change detection work?

**Answer.** Angular keeps a **component tree**. When something *might* have changed, it runs a **dirty check** top-down over that tree, comparing each binding's current value to its previous value and updating the DOM where they differ.

- **What triggers a check?** Asynchronous events — DOM events, timers (`setTimeout`/`setInterval`), and XHR/`fetch`. Angular knows about them because of **zone.js** (next question).
- **Default strategy** checks the whole tree on every trigger — simple, but can be wasteful on large trees.
- **`OnPush`** narrows checking: a component is only checked when an **`@Input` reference changes**, an **event fires inside it**, an **observable it uses via `async` emits**, or you explicitly `markForCheck()`. Combined with immutable data and the `async` pipe, that cuts redundant checks.

**Why it mattered for my migration:** **Ivy subtly changed change-detection timing** in a few cases (e.g. when queries resolve, expression-changed-after-checked situations). A handful of components that "happened to work" under View Engine surfaced `ExpressionChangedAfterItHasBeenCheckedError` or timing issues under Ivy, and I had to understand the CD lifecycle to fix them properly (move the update into the right lifecycle hook, or trigger detection deliberately) rather than paper over them.

---

## Q3. What is zone.js and why does Angular use it?

**Answer.** **zone.js** monkey-patches asynchronous browser APIs (`addEventListener`, `setTimeout`, `Promise`, XHR, etc.). By wrapping them, it can tell Angular *"an async task just finished — something may have changed, run change detection."* That's the "magic" that lets you mutate a field in a click handler and see the view update without telling Angular anything.

- **Trade-off:** zone.js adds bundle weight and patches globals, and it triggers CD even when nothing relevant changed.
- **Where it's going:** **zoneless** change detection (stable in Angular 19; experimental earlier) drops the zone.js polyfill and relies on **signals**, the `async` pipe, and explicit `markForCheck()` instead — smaller and more predictable.

**Why it mattered for my migration:** each major pins a compatible **zone.js** version, so `ng update` bumped it per step — I had to keep it in the compatible range. And landing on Angular 16 with `takeUntilDestroyed()`/signals available sets up a *future* zoneless move, which is the honest "here's our reality + modern direction" story.

---

## Q4. Which RxJS operators/behaviors changed, and how did that surface in the migration?

**Answer.** The big shifts were **RxJS 5→6** (v6) and **6→7** (v12):

- **5→6:** operators became **pipeable** (`.pipe(map(), filter())`) instead of prototype-patched (`obs.map().filter()`), with new import paths (`rxjs`, `rxjs/operators`). This touched *every* stream in the codebase.
- **6→7:** **`toPromise()` deprecated** → `firstValueFrom`/`lastValueFrom`; **stricter typing** on operator overloads (`combineLatest`, `merge`); some subtle completion-semantics tightening.

Operators I actually rely on and why (the interviewer will ask):
- **`switchMap`** — cancel the previous inner stream when a new value arrives ("latest wins": search, token refresh).
- **`mergeMap`** — run inner streams in parallel (independent writes).
- **`concatMap`** — queue inner streams in order (order matters).
- **`combineLatest`** — derive a value from the latest of several streams.
- **`debounceTime`** — rate-limit rapid input.
- **`catchError`** — recover or rethrow inside a stream.
- **`takeUntil`** — complete a subscription on a `destroy$` signal (leak prevention).

**Why it mattered for my migration:** the RxJS changes were the most *pervasive* mechanical edits — they weren't in one file, they were everywhere a stream existed. Understanding the operator semantics is what let me convert them correctly instead of blindly find-replacing (e.g. knowing `firstValueFrom` throws on empty completion whereas `toPromise` resolved `undefined` — a real behavior difference).

---

## Q5. Explain the Angular lifecycle hooks — and did the migration touch them?

**Answer.** In order:
- **`ngOnChanges`** — on `@Input` changes (before first `ngOnInit`).
- **`ngOnInit`** — once, after first inputs set — init logic, initial data fetch.
- **`ngDoCheck`** — every CD run (custom change detection).
- **`ngAfterContentInit` / `ngAfterContentChecked`** — projected content ready/checked.
- **`ngAfterViewInit` / `ngAfterViewChecked`** — view & child views ready/checked — safe to read `@ViewChild`.
- **`ngOnDestroy`** — cleanup: unsubscribe, clear timers.

**Why it mattered for my migration:** the **`ViewChild` static flag** (v8/9) is *directly* about lifecycle timing — `static: true` resolves the query before CD so it's available in **`ngOnInit`**; `static: false` resolves after, available in **`ngAfterViewInit`**. Getting that wrong gives you an `undefined` ref. And the Ivy CD-timing fixes were about moving work into the correct hook. So the lifecycle wasn't academic — it's the exact mechanism behind two of my breaking changes.

---

## Q6. NgModules vs standalone components — where did you land?

**Answer.**
- **NgModules** (`@NgModule`) are the classic packaging unit: declarations, imports, providers, exports. Every component belongs to exactly one module.
- **Standalone components** (preview v14, **stable v15**) drop the module — a component declares its own `imports` directly (`standalone: true`), so you can bootstrap and route without `NgModule` boilerplate. This is the modern default direction and pairs with `provideRouter`, `provideHttpClient`, etc.

**Where we landed (honest):** the apps finished the migration as **NgModule apps on Angular 16** — the goal was "safely on a supported version, nothing regressed," not a re-architecture. I deliberately **did not** rewrite to standalone mid-migration because that's a second, separate risk. My recommended next step was: adopt **standalone components**, **signals**, and **`takeUntilDestroyed()`** incrementally on Angular 16, since 16 already ships all three.

That's the strong answer: I know the modern architecture, I can articulate the migration path to it, and I made a *disciplined* call not to conflate two migrations.

---

## Q7. What made the bundles smaller / builds faster after the migration? (the payoff)

**Answer.** Concretely:
- **Ivy's tree-shaking + AOT-by-default** → smaller production bundles and faster builds.
- **Newer TypeScript** per major → faster type-checking and better incremental compilation.
- **Angular 16's esbuild-based dev server** (preview) → dramatically faster dev rebuilds vs the old webpack pipeline.
- **Dropping IE11** (v13) → no legacy/differential bundle overhead.

So "modern, supported, faster-building apps on one Angular version" isn't a slogan — it maps to specific mechanisms: Ivy, AOT, newer TS, esbuild, and shedding legacy targets.
