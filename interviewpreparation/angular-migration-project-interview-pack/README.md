# Backoffice UI — Angular Version Migration — Project Interview Pack

**Target:** 5-year Full-Stack Developer (Angular + Java) — project-understanding round
**Idea:** Every answer is tied to the **real Backoffice migration project** — so when an interviewer asks *"in the Angular migration you mentioned, what challenges did you face?"*, you don't freeze. You have a strategy, concrete breaking changes, code, and stories.

---

## How to use this pack

- Each file = one interview angle. Read the **Question → Answer** structure and say answers **out loud**.
- Memorize the *flow and the reasons*, not the code character-for-character. Interviewers reward "why", not recitation.
- The one file that wins this round is `01-migration-strategy-deep-dive.md` — that is exactly where interviewers push. Know it cold.
- 30 minutes before an interview, read **only** `00-ONE-PAGE-CHEATSHEET.md`.

## The sections

| # | File | Covers |
|---|------|--------|
| 0 | `00-ONE-PAGE-CHEATSHEET.md` | 20 must-know facts + one-liner answers for a 30-min pre-interview glance |
| 1 | `01-migration-strategy-deep-dive.md` | **THE core file** — why never jump majors, incremental `ng update`, the `update.angular.io` checklist, build+test+commit-per-version discipline, bisecting, rollback, driving four repos |
| 2 | `02-breaking-changes-per-version.md` | Version-by-version reference of the real breaking changes 2→16 and 9/4→16: Ivy, RxJS 6→7, `entryComponents`, `ViewChild` static flag, differential loading, TS bumps, strict mode, `angular.json` schema, standalone/signals |
| 3 | `03-angular-internals.md` | What interviewers probe AFTER a migration answer: View Engine vs Ivy, change detection, zone.js, RxJS operators, lifecycle hooks, modules vs standalone — anchored to "this is what I had to understand to migrate safely" |
| 4 | `04-testing-and-stars.md` | Seamless-transition testing strategy + 3-4 **STAR stories** (RxJS 6→7, Ivy-incompatible library, SSG-plugin-in-host, "how did you make it seamless") + Git workflow + "what I'd do differently" |

## Key project facts (know these cold)

- **Backoffice UI Angular Version Migration** — **Feb 2024 – Mar 2024** (focused ~2-month effort).
- Migrated **four repositories to Angular 16**:
  - **1 Ticketing web application** — **Angular 2 → 16**.
  - **3 SSG web-plugin app repos** — **Angular 9 and 4 → 16**.
- **Goal:** navigate years of breaking changes safely, verify nothing regressed via meticulous testing.
- **Payoff:** modern, supported, faster-building apps on **one** Angular version.
- **My role:** I **led and owned** the migrations across all four repos; QA validated. I was the person who understood the cross-version breaking changes and applied them consistently.
- **Core method:** never jump majors — **one major at a time** (`ng update` per step), **build + test after every step**, **one commit per version** so I could bisect and roll back. `update.angular.io` was my per-step checklist.
- **Headline breaking changes I handled:** **Ivy** default renderer (v9), **RxJS 6→7** (`toPromise()` → `firstValueFrom`/`lastValueFrom`, stricter typing), removed/deprecated APIs (`entryComponents`, `ViewChild` `static` flag), TypeScript strictness bumps, `angular.json` build-schema changes, third-party-library Ivy compatibility, and **SSG plugins loading inside a host environment**.

## Honesty note (important — read this)

This was a **focused ~2-month migration project, not a from-scratch product build.** Do not oversell it as a huge greenfield app. Frame it as what it actually was and what it actually proves:

> *"It was a focused migration — but a deep one. To take a repo from Angular 2 to 16 safely you have to understand Angular internals across every major version: the View Engine→Ivy switch, how change detection and zone.js work, RxJS's evolution, and how the build pipeline changed. My value was the **upgrade strategy and the Angular-internals knowledge** that let me do it without regressions across four repos."*

That framing is a strength: it signals **deep Angular-internals expertise + disciplined upgrade strategy + risk management**, which is exactly what a 5-year Angular engineer should own. Interviewers respect "here's the focused scope + here's the depth it required" far more than an inflated story you can't defend.
