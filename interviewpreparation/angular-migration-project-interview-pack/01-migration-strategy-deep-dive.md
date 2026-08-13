# 01 — Migration Strategy Deep Dive (the core file)

**This is where interviewers push.** When they say *"what challenges did you face in the Angular migration?"*, they are really asking *"do you understand how to do a risky, multi-version upgrade without breaking production?"* Everything below is your answer.

**Context:** 4 Backoffice repos → Angular 16. Ticketing app **Angular 2 → 16**, three SSG plugin apps **Angular 9 and 4 → 16**. I led/owned it; QA validated.

---

## Q1. What was your overall strategy for a jump as big as Angular 2/9 to 16?

**Answer.** The single most important rule: **I never jump majors.** Angular only officially supports **one-major-at-a-time** upgrades — each major ships **migration schematics** that assume you're coming from the immediately previous major. If you skip versions, those schematics don't exist for your jump and you're hand-fixing years of changes at once with no safety net.

So the strategy was a disciplined loop, repeated once per major:

```
for each major version step (e.g. 9 → 10 → 11 → … → 16):
  1. Read the update.angular.io checklist for THIS step
  2. Check third-party libs are compatible with the target major
  3. ng update @angular/core@N @angular/cli@N   # runs the schematics
  4. Fix whatever the schematics couldn't auto-migrate
  5. Build            → must be green
  6. Run unit tests   → must be green
  7. Smoke-test critical flows
  8. Commit: "chore: upgrade to Angular N"   # one commit per version
```

The value of the loop is **localization of risk**: if something breaks, it broke in exactly one hop, against one commit, and I can bisect to it. A big-bang "2 → 16 in one branch" would have been an undebuggable mess.

---

## Q2. Why is jumping majors actually dangerous — what's the mechanism?

**Answer.** Three concrete reasons, not just "best practice":

1. **Schematics are per-step.** `ng update` runs code-mod migrations shipped *inside* each major, written to migrate from `N-1` to `N`. Angular 12's schematics know how to fix Angular-11 code, not Angular-4 code. Skip versions and you lose all automation.
2. **Peer-dependency math.** Each Angular major pins a compatible **TypeScript**, **RxJS**, **zone.js**, and Node range. Jumping straight to 16 forces TS/RxJS to their newest at once, so *every* type error and deprecation lands simultaneously with no way to tell which version introduced it.
3. **Debuggability.** With one-major-at-a-time, a regression is bounded by a single hop's changelog. With a big jump, a bug could come from any of a dozen releases.

I say it plainly in interviews: *"Incremental isn't ceremony — it's the only way to keep each failure attributable to one change."*

---

## Q3. Walk me through the `ng update` step. What does it actually do?

**Answer.** `ng update` is more than a package bump. For each step:

```bash
# check what's upgradable and to what version
ng update

# perform ONE major step (core + cli together, matched versions)
ng update @angular/core@12 @angular/cli@12
```

It does four things:
1. Bumps `@angular/*` and peer deps (TypeScript, RxJS, zone.js) to versions compatible with that major.
2. Runs **migration schematics** — automated code-mods. Examples I hit: the `ViewChild`/`ContentChild` static-flag migration, the RxJS-6→7 `toPromise` deprecation flags, the `entryComponents` removal, and `angular.json` schema updates.
3. Updates `angular.json`, `tsconfig`, and `polyfills` to the new expected shape.
4. Refuses to run on a dirty git tree — which is *why* the commit-per-version discipline pairs naturally with it.

The schematics do maybe 70-80% of the mechanical work. The remaining 20-30% — third-party incompatibilities, custom code the schematic can't reason about, template type errors — is the human part, and that's what I owned.

---

## Q4. What was your per-step checklist? How did you know what would break?

**Answer.** The **Angular Update Guide at `update.angular.io`** was my checklist. You select "from version", "to version", app complexity (basic/medium/advanced), and it emits a categorized list — **before you begin**, **during the update**, **after the update** — of every required and optional change for that specific hop. I ran it fresh for **each** step, not once for the whole journey.

Around that I kept a small manual checklist per repo:
- Dependency compatibility matrix (does each 3rd-party lib support the target major / is it Ivy-compatible?).
- Deprecation warnings from the previous build (fix them *before* they become removals).
- Known risky areas in that app (where tests were thin — I added coverage first).

---

## Q5. Why one commit per version? Isn't that a lot of commits?

**Answer.** Yes, and that's the point. **One commit per major version** gave me three things:

1. **`git bisect`** — if a regression surfaced only after several hops, I could bisect across the version commits to find exactly which major introduced it.
2. **One-step rollback** — if v13 caused a problem I couldn't immediately solve, I could revert to the v12 commit and keep the app shippable while I investigated, instead of losing the whole migration.
3. **Documentation for free** — each commit message ("upgrade to Angular 13: remove View Engine refs, drop entryComponents") is a precise record of what that version required. The owning team reviewing the PR could read the history hop-by-hop.

```
* chore: upgrade to Angular 16 (signals-ready, takeUntilDestroyed)
* chore: upgrade to Angular 15 (standalone available, NgOptimizedImage)
* chore: upgrade to Angular 14 (typed reactive forms)
* chore: upgrade to Angular 13 (remove View Engine, drop entryComponents)
* chore: upgrade to Angular 12 (RxJS 7, strict mode, Ivy-only libs)
* chore: upgrade to Angular 11
* chore: upgrade to Angular 10
  (base: Angular 9)
```

---

## Q6. How did you handle the fact that it was FOUR repos at different start versions?

**Answer.** I treated each repo as its own migration with its own branch, but reused one **playbook** across all four:

- **Ticketing app: Angular 2 → 16.** The longest chain. Note that Angular 2→4 is a valid `ng update` sequence (there was no Angular 3 — they skipped it to align router versioning), then 4→5→6…→16. The early hops (2→4→5) were the roughest because tooling was less mature and the CLI itself changed a lot (e.g. the move to `angular.json` from `.angular-cli.json` at v6).
- **Three SSG plugin apps: two starting at 9, one at 4.** The two on 9 already had Ivy as an option and were shorter chains; the one on 4 followed the long path like Ticketing.

Because I did them one after another, later repos were faster — I'd already solved the shared breaking changes (RxJS, Ivy libs, `angular.json` schema) once, so I applied the known fix immediately. Doing them sequentially rather than in parallel was deliberate: I built the playbook on the first repo and reused it.

---

## Q7. The v6 jump is famously awkward (`.angular-cli.json` → `angular.json`). How did that go?

**Answer.** Angular 6 restructured the workspace: `.angular-cli.json` became **`angular.json`** with a completely new **architect/builder** schema, and `ng update`/`ng add` were introduced. The v6 schematic migrated the config file automatically, but I verified the generated `angular.json` by hand — build options, `assets`, `styles`, `scripts`, and environment `fileReplacements` — because a mis-migrated builder config fails in ways that look like app bugs. This is exactly why "build after every step" matters: the v6 config change is caught by the very next `ng build`.

---

## Q8. What did the build/test checkpoint actually catch?

**Answer.** Real examples across the hops:
- **Build caught:** template type errors newly flagged by strict templates; removed API references (`entryComponents`, `Renderer` → `Renderer2`); `angular.json` schema mismatches; TS syntax the newer compiler rejected.
- **Unit tests caught:** RxJS typing/behavior changes (e.g. `toPromise` semantics, stricter operator overloads); DI changes; components whose lifecycle timing shifted with Ivy.
- **Smoke tests caught:** runtime issues that compile fine but behave differently — the SSG plugins mounting inside the host, and change-detection timing differences under Ivy.

The rule I repeat: *"a green build and green tests after every hop is the difference between a controlled migration and a debugging nightmare at the end."*

---

## Q9. What was your rollback plan if a version step went wrong?

**Answer.** Layered:
1. **Never merge a broken hop.** Each version lived on the migration branch and only advanced once build+test+smoke were green.
2. **One-commit revert.** Because each version was its own commit, I could `git revert`/reset back exactly one version and the app was shippable again on the previous major.
3. **Isolate, don't block.** If a single third-party library blocked a hop, I'd pin/isolate that library and continue rather than abandon the whole step (see the Ivy-library story in `04`).
4. **The owning team's PR review** was a final gate before anything hit their main branch.

---

## Q10. How long did it take, and how did you keep it from dragging?

**Answer.** It was a focused ~2-month effort (Feb–Mar 2024). What kept it moving:
- The **playbook reuse** across repos — I wasn't re-learning each breaking change.
- **`ng update` schematics** doing the bulk mechanical work.
- **Doing the shortest-chain repos and cheapest fixes first** to build momentum and confidence, then the long Angular-2 Ticketing chain.
- Not gold-plating: the goal was "safely on 16, nothing regressed," not "rewrite to standalone/signals." I landed the apps as **NgModule apps on 16** and noted standalone/signals as the *next* project.

---

## The 30-second version (if they just want the headline)

> "Four repos to Angular 16. Rule number one: never jump majors — Angular's migration schematics are per-step, so I went one major at a time with `ng update`, ran the `update.angular.io` checklist each hop, and built + tested after every single step. I committed one version per commit so any regression was attributable to one hop and I could bisect or roll back. The hard parts were Ivy at v9, the RxJS 6-to-7 migration, removed APIs like `entryComponents`, and making sure the three embedded SSG plugins still mounted inside their host environment."
