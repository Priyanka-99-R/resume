# 00 — One-Page Cheat Sheet (read 30 min before the interview)

**Project:** Backoffice UI Angular Version Migration · Feb–Mar 2024 · I led/owned it, QA validated.
**Scope:** 4 repos → **Angular 16**. 1 Ticketing app (**Angular 2 → 16**) + 3 SSG web-plugin apps (**Angular 9 and 4 → 16**).
**Goal:** cross years of breaking changes safely, prove nothing regressed. **Payoff:** modern, supported, faster-building apps on one version.

## The 20 facts + one-liner answers

1. **What was the project?** "Migrating four Backoffice repos to Angular 16 — a Ticketing app from Angular 2, and three SSG plugin apps from Angular 9 and 4."
2. **Your role?** "I led and owned the migrations across all four repos; QA regression-tested each app."
3. **Biggest strategic rule?** "Never jump majors. Angular only supports **one-major-at-a-time** upgrades, so I went 9→10→11…→16."
4. **The per-step tool?** "`ng update @angular/core@N @angular/cli@N` — it runs the migration **schematics** that auto-fix a lot of code."
5. **Your checklist?** "`update.angular.io` — you pick from-version and to-version and it lists every change per step."
6. **Discipline per step?** "**Build + run tests after every single version hop**, not just at the end — it localizes any breakage to one hop."
7. **Git discipline?** "**One commit per version**, branch per repo, PR to the owning team. Granular commits = documentation + I can `git bisect` and roll back one step."
8. **Ivy (v9)?** "Ivy became the **default renderer** in v9 — smaller bundles, better tree-shaking, faster builds. Main risk: libraries built for the old **View Engine** had to be Ivy-compatible."
9. **RxJS 6→7?** "Stricter typing and **`toPromise()` deprecated** → I moved to **`firstValueFrom`/`lastValueFrom`**."
10. **`toPromise()` fix (say the code):** "`await firstValueFrom(this.http.get(...))` instead of `await this.http.get(...).toPromise()`."
11. **`entryComponents`?** "Ivy made it unnecessary — dynamic components no longer need pre-declaration, so I removed the `entryComponents` arrays (fully removed in v13)."
12. **`ViewChild` static flag (v8/9)?** "v8 made `{ static: true|false }` **required**; v9 defaulted it to `false`. I set `static: true` only where I read the ref in `ngOnInit`."
13. **Differential loading (v8)?** "v8 shipped modern + legacy bundles via `browserslist`/`tsconfig` targets; later removed as browsers modernized — just meant build-config changes."
14. **TypeScript bumps?** "Each major requires a newer TS. Stricter compiler + **strict templates** surfaced previously-silent type errors I had to fix."
15. **`angular.json` schema?** "The build schema changed across versions (builder targets, `polyfills` moved to an array, budgets); `ng update` migrated most of it, I reconciled the rest."
16. **Third-party libs?** "I checked each dependency's Angular compatibility **before each hop** — upgrade it, swap for a maintained alternative, or pin-and-isolate. Ivy compat was the big filter at v9."
17. **SSG plugins — special?** "They're **embedded plugins loaded inside a host SSG environment**, so I had to verify the upgraded bundles still **bootstrap/mount** correctly in the host and don't clash with its runtime — testing the integration boundary mattered as much as in-app behavior."
18. **How 'seamless'?** "Build+test each step, smoke-test critical flows, QA full regression, and I added test coverage to risky areas **before** upgrading them."
19. **Standalone components / signals?** "Standalone stabilized in v15, signals arrived in v16. We landed on 16 as NgModule apps; I know how to adopt standalone/signals as the next step."
20. **What you'd do differently?** "Strengthen automated tests **before** starting, and script the per-step build/test loop in **CI** so each bump is gated automatically."

## Version map (memorize the milestones, not every number)
- **v8** — `ViewChild` static flag required · differential loading introduced.
- **v9** — **Ivy default** · `entryComponents` deprecated · static flag defaults to false.
- **RxJS 7** (with Angular ~12) — `toPromise()` deprecated · stricter types.
- **v12** — strict mode default for new projects · Ivy-only (View Engine deprecated for libs).
- **v13** — View Engine removed · IE11 dropped · `entryComponents` removed.
- **v14** — standalone components (preview) · typed reactive forms.
- **v15** — **standalone stable** · `NgOptimizedImage`.
- **v16** — **signals (preview)** · `takeUntilDestroyed()` · required inputs · esbuild dev server (preview).

## If you freeze, say this first
> "It was a focused migration across four repos to Angular 16. The core challenge was doing multi-version jumps safely — I never jumped majors, I upgraded one major at a time with `ng update`, built and tested after every step, and kept one commit per version so I could roll back. The specific breaking changes I handled were Ivy at v9, the RxJS 6-to-7 migration, and removed APIs like `entryComponents`."

Then let them pick a thread. You have a file for each.
