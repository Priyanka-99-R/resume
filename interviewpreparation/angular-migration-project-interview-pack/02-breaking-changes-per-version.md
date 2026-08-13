# 02 — Breaking Changes, Version by Version (reference)

The real breaking changes I crossed going **Angular 2 → 16** (Ticketing) and **9/4 → 16** (SSG plugins). For each: **what it was**, **why it broke**, **how I fixed it**. Interviewers love when you can name the version a change landed in — it proves you lived it, not read a blog.

> Mental model: the migration wasn't one big change, it was ~14 small, well-documented steps. Below are the ones that actually cost me time.

---

## Angular 2 → 6 (the early, awkward hops — Ticketing app)

### Angular 2 → 4 (no Angular 3)
- **What:** Angular skipped 3 to align the router's version with the rest of the framework. `ng update` treats 2→4 as one step.
- **Why it broke:** template `<template>` tag deprecated in favor of `<ng-template>`; some animation imports moved to `@angular/animations`.
- **Fix:** ran the schematic, replaced `<template>` with `<ng-template>`, moved animation imports.

### Angular 5
- **What:** build optimizer + AOT-by-default direction; `HttpModule` deprecated in favor of **`HttpClientModule`**; the old `Http` service replaced by `HttpClient`.
- **Why it broke:** `HttpClient` returns parsed JSON directly (no `.json()` call), and interceptors work differently.
- **Fix:** migrated `Http` → `HttpClient`, removed `.map(res => res.json())` calls, moved cross-cutting logic into `HttpInterceptor`s.

### Angular 6 — the workspace restructure
- **What:** **`.angular-cli.json` → `angular.json`** (new architect/builder schema); `ng update` and `ng add` introduced; RxJS **5 → 6** with `rxjs-compat`; `providedIn: 'root'` tree-shakable providers.
- **Why it broke:** RxJS 6 moved to **pipeable operators** and new import paths (`rxjs/operators`), and prototype-patched operators (`.map`, `.filter`) were gone.
- **Fix:** ran the RxJS migration, converted chained operators to `.pipe(map(), filter())`, updated imports, and dropped `rxjs-compat` once clean. Hand-verified the generated `angular.json`.

```ts
// before (RxJS 5, patched prototype)
import 'rxjs/add/operator/map';
obs.map(x => x * 2).filter(x => x > 0);

// after (RxJS 6, pipeable)
import { map, filter } from 'rxjs/operators';
obs.pipe(map(x => x * 2), filter(x => x > 0));
```

---

## Angular 7 → 8

### Angular 8 — `ViewChild`/`ContentChild` static flag
- **What:** the **`static` resolver flag became required** on `@ViewChild`/`@ContentChild` queries.
- **Why it broke:** Angular needed to know whether to resolve the query *before* change detection (`static: true`, available in `ngOnInit`) or *after* (`static: false`, available in `ngAfterViewInit`). Ambiguity had to be made explicit.
- **Fix:** the v8 schematic added the flag automatically; I audited each one — `static: true` only where I actually read the ref in `ngOnInit`, otherwise `static: false`.

```ts
// v8 required the flag explicitly
@ViewChild('chart', { static: false }) chart!: ElementRef; // read in ngAfterViewInit
@ViewChild('form',  { static: true  }) form!: NgForm;      // read in ngOnInit
```

### Angular 8 — differential loading
- **What:** the CLI began emitting **two bundle sets** — modern ES2015+ and legacy ES5 — chosen per browser via `browserslist` + `tsconfig` `target`.
- **Why it broke:** nothing broke functionally, but build config and output changed; later (as IE died) this was removed again.
- **Fix:** confirmed `browserslist`/`tsconfig.json` targets were sane for the Backoffice's supported browsers; mostly a build-config verification.

---

## Angular 9 — the big one: Ivy

### Ivy becomes the default renderer
- **What:** **Ivy** replaced **View Engine** as the default compilation/rendering pipeline in v9.
- **Why it broke:** Ivy = better tree-shaking, smaller bundles, faster builds, better debugging — but **libraries compiled for View Engine** (or shipping non-Ivy-compatible metadata) could fail. Ivy also changed some **change-detection timing** and made a few previously-tolerated patterns error.
- **Fix:** made sure **every dependency was Ivy-compatible** before the hop (upgrade / replace / isolate — see `04`). Ran `ngcc` (the Angular compatibility compiler) which converts View-Engine libs to Ivy at install time. Fixed the handful of components whose CD timing shifted.

### `entryComponents` deprecated
- **What:** with Ivy, dynamically-created components no longer need to be pre-declared, so **`entryComponents` became unnecessary** (deprecated v9, **removed v13**).
- **Why it broke:** the array was now dead code, and keeping it produced deprecation warnings.
- **Fix:** removed `entryComponents` arrays; dynamic component creation via `ViewContainerRef.createComponent()` just works under Ivy.

```ts
// before (View Engine) — had to pre-register
@NgModule({ entryComponents: [DialogComponent] })
// after (Ivy) — not needed; createComponent resolves it directly
this.vcr.createComponent(DialogComponent);
```

### `ViewChild` static defaults to false
- **What:** in v9 the `static` flag became **optional again, defaulting to `false`**.
- **Fix:** removed the now-redundant `{ static: false }` I'd added in v8, kept `{ static: true }` only where needed.

---

## Angular 10 → 12

### Angular 10
- **What:** stricter project setup option; `browserslist` config consolidation; some TS `strict` warnings.
- **Fix:** mechanical config alignment; fixed newly-surfaced type warnings.

### Angular 11 → 12 — RxJS 6 → 7
- **What:** Angular moved to **RxJS 7** (default around v12). **`toPromise()` deprecated**; **stricter typing** on operator overloads; `TimeoutError`/`combineLatest` signature tweaks.
- **Why it broke:** `toPromise()` had ambiguous semantics on empty completion, so it was deprecated in favor of **`firstValueFrom`/`lastValueFrom`**. Tighter overloads flagged loosely-typed `map`/`reduce` chains.
- **Fix:** replaced every `toPromise()` and added explicit types where overloads got strict.

```ts
// before (RxJS 6, deprecated)
const data = await this.http.get('/api/tickets').toPromise();

// after (RxJS 7)
import { firstValueFrom } from 'rxjs';
const data = await firstValueFrom(this.http.get('/api/tickets'));
// use lastValueFrom when you want the final emitted value of a finite stream
```

### Angular 12 — strict mode + Ivy-only
- **What:** **strict mode on by default** for new projects; **View Engine deprecated for libraries** (Ivy-only direction); production build by default; `@angular/flex-layout` and similar started aging out.
- **Why it broke:** stricter TS/templates surfaced null-safety and type errors that were previously silent.
- **Fix:** progressively tightened `tsconfig` (`strict`, `strictTemplates`), fixed null checks and template type mismatches. I turned strict flags on **incrementally** rather than all at once so the error volume stayed manageable.

```jsonc
// tsconfig.json — tightened over several hops, not all at once
{
  "compilerOptions": { "strict": true },
  "angularCompilerOptions": { "strictTemplates": true }
}
```

---

## Angular 13 — View Engine removed

- **What:** **View Engine deleted entirely** (Ivy-only); **IE11 support dropped**; `entryComponents` **removed**; libraries must ship Ivy (Partial Ivy / `ngcc` still bridged older ones); Node/TS bumps.
- **Why it broke:** any lingering View-Engine-only dependency or `entryComponents` reference now hard-failed.
- **Fix:** by the time I hit 13 I'd already Ivy-cleared the dependencies at v9-12 and removed `entryComponents`, so this hop was mostly confirming no View-Engine-only lib remained. Dropped IE11 from `browserslist`.

---

## Angular 14 → 16 (the modern era)

### Angular 14
- **What:** **standalone components (preview)**; **typed reactive forms** (`FormControl<T>`); `inject()` function; optional `NgModules`.
- **Why it broke:** typed forms changed `FormGroup`/`FormControl` generics — untyped usages needed types or the `Untyped*` shims.
- **Fix:** left forms as `UntypedFormGroup` where a full retype was risky, typed the simpler ones. Did **not** rewrite to standalone — noted it as future work.

### Angular 15
- **What:** **standalone components stable**; `NgOptimizedImage`; MDC-based Angular Material; router/guards can be plain functions.
- **Fix:** verified Material components still rendered after the MDC swap (some CSS/DOM differences); no forced standalone rewrite.

### Angular 16 — our destination
- **What:** **signals (developer preview)**; **`takeUntilDestroyed()`** and `DestroyRef` (`@angular/core/rxjs-interop`); **required inputs**; `@angular/cli` **esbuild-based dev server** (preview); non-destructive hydration for SSR.
- **Why it mattered:** this is the modern, supported baseline we wanted — faster builds, and the door open to signals/standalone next.
- **Fix / landing state:** apps landed as **NgModule apps on Angular 16**, RxJS 7, strict where feasible. I flagged **standalone migration + signals + `takeUntilDestroyed()`** (to drop the `destroy$` Subject boilerplate) as the recommended next step.

```ts
// Angular 16 — the cleanup I recommended as follow-up work
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

constructor() {
  this.service.stream$
    .pipe(takeUntilDestroyed())   // no more manual destroy$ Subject
    .subscribe(v => this.handle(v));
}
```

---

## One-line recap table (drill these)

| Version | Headline change | Why it broke | How I fixed it |
|---|---|---|---|
| 2→4 | `<template>`→`<ng-template>` | tag deprecated | schematic + replace |
| 5 | `Http`→`HttpClient` | new API, no `.json()` | migrate calls + interceptors |
| 6 | `angular.json` + RxJS 5→6 | pipeable operators, new imports | `.pipe()`, RxJS migration, verify config |
| 8 | `ViewChild` static flag required | CD-timing ambiguity | set `static` true/false per use |
| 8 | differential loading | build output changed | verify browserslist/target |
| 9 | **Ivy default** | View-Engine libs incompatible | Ivy-clear deps, ngcc |
| 9 | `entryComponents` deprecated | Ivy makes it dead code | remove arrays |
| 12 | RxJS 6→7, strict mode | `toPromise` deprecated, tighter types | `firstValueFrom`, add types, tighten tsconfig incrementally |
| 13 | View Engine removed, IE11 dropped | no VE fallback | confirm all deps Ivy, drop IE11 |
| 14 | typed forms, standalone preview | form generics changed | type or `Untyped*` shims |
| 15 | standalone stable, Material MDC | Material DOM/CSS changed | verify UI |
| 16 | signals preview, `takeUntilDestroyed` | (destination) | land on 16, flag next steps |
