# 04 — Testing Strategy, STAR Stories, Git Workflow

The migration's whole promise was *"nothing regressed."* That promise is only as good as the testing behind it. This file is the testing strategy, the STAR stories interviewers ask for by name, the Git workflow, and "what I'd do differently."

---

## Part A — The seamless-transition testing strategy

### Q1. How did you make the transition "seamless" — what was the testing strategy?

**Answer.** Testing was layered around the per-step loop, not bolted on at the end:

1. **Build + run the full unit suite after *every* version hop** — not just at the end. This localizes any failure to a single major's changes.
2. **Smoke-test the critical flows** in each app after each hop — for Ticketing that's create/assign/resolve a ticket; for the plugins it's mount + core interaction inside the host.
3. **Add coverage to the riskiest areas *before* upgrading them** — where the existing tests were thin, I wrote tests first so I'd actually *notice* a regression instead of hoping.
4. **QA full regression** on the Ticketing app and the three plugin apps — QA validated each app end-to-end; I fixed what they found.
5. **Integration-boundary testing for the SSG plugins** — verifying the bootstrap/mount point inside the host, since that's where an embedded plugin breaks in ways unit tests can't see.
6. **One commit per version** so any regression was attributable and revertible.

The phrase I use: *"Seamless doesn't mean lucky — it means every hop was gated by build + tests + smoke, and QA regression on top."*

### Q2. What tooling did you test with?

**Answer.** **Jasmine + Karma** for unit/component tests (the Backoffice apps' existing setup). I ran the suite via `ng test` at each step. For the plugins I leaned harder on **manual smoke + QA integration testing** because the meaningful risk was the *host integration*, which is awkward to cover in Karma. I also treated the **build itself as a test** — strict templates and the newer TS compiler catch a whole class of regressions before any spec runs.

### Q3. Why add tests *before* upgrading rather than after?

**Answer.** Because a test written *after* a migration only proves the *new* behavior — it can't catch a regression you already introduced. A test written *before* captures the *known-good* behavior, so when the upgrade changes it, the test fails and tells you. For the riskiest, least-covered areas I wrote characterization tests first, then upgraded, then watched them.

---

## Part B — STAR stories (memorize the shape, tell them naturally)

### STAR 1 — The RxJS 6 → 7 migration

- **Situation:** Around the Angular 11→12 hops, the apps moved to RxJS 7. `toPromise()` was deprecated and operator overloads got stricter — and streams were used *everywhere* in the codebases.
- **Task:** Migrate all RxJS usage without changing behavior, across four repos.
- **Action:** I ran the RxJS migration schematic, then did the manual work it couldn't: replaced every `toPromise()` with `firstValueFrom`/`lastValueFrom`, and added explicit types where the stricter overloads flagged loosely-typed `map`/`reduce` chains. I was careful about a real semantic difference — `firstValueFrom` *throws* on empty completion whereas `toPromise` resolved `undefined` — so I checked each call site rather than blind find-replace.
- **Result:** All streams on RxJS 7, no behavior change, build and tests green. The edits were mechanical but had to be precise; doing them one repo at a time meant I'd solved the pattern once and applied it fast to the rest.

```ts
// before (RxJS 6, deprecated) — resolves undefined on empty stream
const data = await this.http.get('/api/tickets').toPromise();

// after (RxJS 7) — throws on empty; verified each call site expects a value
import { firstValueFrom } from 'rxjs';
const data = await firstValueFrom(this.http.get('/api/tickets'));
```

### STAR 2 — The Ivy-incompatible third-party library

- **Situation:** At Angular 9, Ivy became the default renderer. One of the apps depended on a library built for View Engine that wasn't cleanly Ivy-compatible.
- **Task:** Get past the v9 hop without that library blocking the whole migration or breaking the app.
- **Action:** I checked each dependency's Angular/Ivy compatibility *before* the hop and built a small compatibility matrix. For the blocking library I evaluated three options in order: **(1)** upgrade it to a version that shipped Ivy support; **(2)** swap it for a maintained, Ivy-compatible alternative; **(3)** if neither existed yet, pin the current version and isolate it so it didn't hold up the rest of the step. Angular's `ngcc` bridged most View-Engine libs automatically, so the truly-blocking cases were few.
- **Result:** Cleared v9 with all dependencies Ivy-compatible. By the time I reached v13 (where View Engine was fully removed) there was nothing left to fix, because I'd triaged every library at v9 rather than deferring it.

### STAR 3 — SSG plugins loading inside a host environment

- **Situation:** Three of the four repos weren't standalone apps — they were **SSG web plugins embedded and bootstrapped inside a host environment**. An upgrade that works in isolation can still fail when mounted in the host.
- **Task:** Upgrade the plugins to Angular 16 and guarantee they still load and behave correctly *inside* the host, not just on their own.
- **Action:** I treated the **integration boundary** — how the plugin is bootstrapped/mounted and how its Angular runtime coexists with the host — as a first-class test target, as important as in-app behavior. After each hop I verified the built plugin bundle still mounted cleanly in the host, didn't clash with the host's runtime, and that the critical in-plugin flows worked *in that context*. QA regression-tested the plugins inside the real host.
- **Result:** All three plugins upgraded to 16 and mounting correctly in the host, no runtime clashes. The lesson I state: for embedded/micro-frontend-style code, "it builds and unit-tests pass" isn't done — the integration boundary is where the real risk lives.

### STAR 4 — "How did you make a multi-version migration seamless / lead it across four repos?"

- **Situation:** Four repos on Angular 2, 4, and 9 needed to reach 16 without disrupting the feature teams that owned them.
- **Task:** Own the migrations, apply the cross-version breaking changes consistently, and hand back working apps.
- **Action:** I built one **playbook** — never jump majors, `ng update` per step, `update.angular.io` checklist, build+test+smoke each hop, one commit per version — and reused it across all four repos, sequentially so later repos benefited from fixes I'd already solved. I coordinated with each owning team on PR review and with QA on regression scope.
- **Result:** Four repos on a single, modern, supported Angular 16 with faster builds; QA-validated; and a granular commit history that documented exactly what each version required. I was the person who understood the cross-version changes and applied them consistently — which is exactly why it went smoothly.

---

## Part C — Git workflow

### Q4. How did you manage this in Git?

**Answer.**
- **A branch per repo** for the migration.
- **One commit per major version step**, with a clear message ("upgrade to Angular 13: remove View Engine refs, drop entryComponents").
- **PRs reviewed by the owning feature team** before merge.

The granular commits paid off three ways: **`git bisect`** to find which version introduced a regression, **one-step rollback** to the previous version if a hop went wrong, and **free documentation** — the history *is* the record of what each version required.

```
* chore: upgrade to Angular 16
* chore: upgrade to Angular 15
* chore: upgrade to Angular 14
* chore: upgrade to Angular 13 (remove View Engine, drop entryComponents)
* chore: upgrade to Angular 12 (RxJS 7, strict mode)
* ...
  (base branch)
```

---

## Part D — Reflection

### Q5. What would you do differently?

**Answer.** Two things:
1. **Strengthen automated tests *before* starting.** Strong coverage is what makes a big upgrade fast and safe; where tests were thin I added them mid-flight, and I'd rather have that cushion in place up front.
2. **Script the per-step build/test loop in CI** so every version bump is *gated automatically* — build, unit tests, and lint green as a merge condition — instead of me verifying each hop by hand. That turns the discipline into infrastructure so it can't be skipped under time pressure.

### Q6. What did this project prove about you?

**Answer.** That I understand Angular deeply enough to move a codebase across a decade of breaking changes without regressions — Ivy, RxJS's evolution, change detection, the build pipeline — and that I do risky work with **discipline**: incremental steps, gated checkpoints, attributable commits, and a rollback plan. It was a focused migration, but it's exactly the kind of deep-internals + risk-management work a senior Angular engineer should own.

---

## Rapid-fire answers (drill these out loud)

- **"Why not just go straight to 16?"** — Migration schematics are per-step; a big jump loses all automation and makes every failure unattributable.
- **"What broke at v9?"** — Ivy became default; View-Engine libraries had to be made Ivy-compatible; removed `entryComponents`.
- **"toPromise replacement?"** — `firstValueFrom` / `lastValueFrom`.
- **"How did you test?"** — Build + unit tests + smoke every hop, coverage on risky areas first, QA full regression, integration-boundary tests for the plugins.
- **"How did you roll back?"** — One commit per version; revert one hop; app stays shippable on the prior major.
- **"Plugins — what was special?"** — Embedded in a host SSG environment; verifying bootstrap/mount + no runtime clash mattered as much as in-app behavior.
- **"Where did you land?"** — NgModule apps on Angular 16; standalone + signals + `takeUntilDestroyed()` flagged as the next step.
