# Subsea — Project-Based Interview Pack

**Target:** 5-year Full-Stack Developer (Angular · .NET / C# Web API · Docker · Azure · Kendo UI)
**Idea:** Every answer is anchored in the **real Subsea project** — so when an interviewer asks *"where did you use X in your actual project?"*, you have a concrete feature, pattern, and story. No abstract theory; everything lands back on Schedule-Manager, the Kendo Grid, and the category/subcategory work you actually built.

> **Why this pack exists:** You keep failing on "project understanding." The fix is not more theory — it's being able to take *any* technical question and route it through Subsea. Every answer here ends with an **"In Subsea, I…"** tie-in. Memorize the *flow* of those tie-ins, not the code character-for-character.

---

## How to use this pack

- Each file = one interview area. Read the **Question → Answer → "In Subsea"** structure.
- The **"In Subsea"** block is your talking point — say it **out loud** until it's natural.
- If an interviewer pushes deeper, walk them through the code snippet — but lead with the *story* (what problem, what you did, what the result was).
- The signature file is **`02-kendo-grid-deep-dive.md`** — the server-side grid performance story is your strongest differentiator. Know it cold.
- Day before: read `00-ONE-PAGE-CHEATSHEET.md` and `05-behavioral-and-stars.md`.

## The sections

| # | File | Covers |
|---|------|--------|
| 0 | `00-ONE-PAGE-CHEATSHEET.md` | 20 must-know facts + one-liner answers for a 30-min pre-interview glance |
| 1 | `01-frontend.md` | Angular — lifecycle hooks, component communication, RxJS (Observable/Subject/BehaviorSubject), memory leaks + `takeUntil`/async pipe, restricting duplicate API calls (`switchMap`/`debounceTime`), cascading dropdowns (reactive forms), change detection, reusable components |
| 2 | `02-kendo-grid-deep-dive.md` | **THE signature file.** Kendo UI Grid — client-side vs server-side binding, `DataSourceRequest`/`toDataSourceRequestString` (TS + C#), paging/sorting/filtering/grouping, virtual scrolling, large-dataset performance |
| 3 | `03-backend-dotnet.md` | .NET Web API / C# — controllers, `[HttpGet]`/`[HttpPost]`, `DataSourceRequest` on the server, interface vs abstract class, `try/catch/finally`, global exception handling, dependency injection in .NET |
| 4 | `04-docker-azure-devops.md` | Docker (reproducible builds, "works on my machine" solved, Dockerfile/compose), Azure hosting containers, CI/CD, local/CI/prod environment parity |
| 5 | `05-behavioral-and-stars.md` | 60-sec pitch, your role, architecture walkthrough (what to draw), STAR stories (server-side grid debate, production bug methodology, dynamic category management, modularity refactor), "what I'd do differently," Agile/collaboration |

## Key project facts (know these cold)

- **Subsea** — marine equipment & operations management platform; track and schedule subsea assets and operations. **Apr 2024 – Sep 2024.**
- **Stack:** **Angular** SPA frontend · **.NET (C#) Web API** backend · **Docker** containers · **Azure** hosting · **Kendo UI** (Grid).
- **Your role:** Full-stack with an **Angular emphasis**, team of ~5–6. You **owned the Schedule-Manager module front to back** — Angular UI *plus* the .NET endpoints — and you were one of the people who picked up production defects.
- **Architecture:** Angular SPA → REST calls → .NET Web API services → SQL. Containerized with Docker, running on Azure. Schedule-Manager is a feature module with its own routing, services, and Kendo-based grid components.
- **Signature achievement:** moved the **Kendo Grid from client-side to server-side data operations** (paging/sorting/filtering done in the .NET API via `DataSourceRequest`), so large schedule/equipment datasets stayed responsive. You **won a client-vs-server-side debate by demonstrating the lag** with realistic data volume.
- **Dynamic category/subcategory management:** data-driven parent/child hierarchy, cascading dropdowns — choosing a category reactively filters its subcategories. Admins could add/edit categories; the UI reflected it live.
- **Refactor:** pulled repeated grid config and category selectors into **shared, reusable components**; tightened feature-module boundaries.

## Honesty note (important — this is what senior interviewers reward)

You are **Angular-emphasis full-stack**, not a deep C# internals guru. That is a *strength* if you frame it honestly:

> *"I owned Schedule-Manager end to end — the Angular UI and the .NET endpoints behind it. My depth is on the Angular/frontend side; on the .NET side I built the Web API controllers, the `DataSourceRequest` handling, and the data flow. I can tell you exactly how what I built worked, and where I'd bring in a backend specialist for deep runtime/perf tuning."*

Interviewers respect *"here's what I built and how it worked, here's the edge of my depth"* far more than someone who bluffs C# garbage-collection internals. When you don't know something: say your understanding, tie it to the closest real Subsea thing you did, and say you'd confirm it. **Never bluff.**
