# 5. Behavioral, Pitch & STAR Stories — Subsea

**Everything here is first-person and true to what you built.** The interviewer is testing *project understanding* — these are the answers that prove you actually lived this project. Say them out loud until they're natural. Lead with the story, not the tech.

---

## The 60-second pitch (memorize this)

> *"Subsea is a marine equipment and operations management platform — it's used to track and schedule subsea assets and operations. I worked on it from April to September 2024. The frontend is Angular, the backend is a .NET C# Web API, containerized with Docker and deployed on Azure, and we used Kendo UI for the data grids.*
>
> *My main ownership was the **Schedule-Manager** module, which I built front to back — the Angular UI and the .NET endpoints behind it. Inside that I did four things I'm proud of: I moved our Kendo grids from client-side to **server-side data operations** so large datasets stayed fast; I built **dynamic category and subcategory management** with cascading dropdowns; I **refactored repeated grid and selector code into reusable components**; and I regularly **fixed production bugs**. The team was around five or six people, and I was the full-stack developer with an Angular emphasis who owned Schedule-Manager end to end."*

**If they want it shorter (15 sec):** *"Subsea is a marine asset scheduling platform — Angular front end, .NET Web API back end, Docker on Azure, Kendo grids. I owned the Schedule-Manager module end to end, and my signature win was moving the grids to server-side paging/sorting/filtering so they stayed fast on large data."*

---

## Your role (be precise, don't overclaim)

- **Title/scope:** Full-stack developer, **Angular emphasis**, team of ~5–6.
- **Owned:** the **Schedule-Manager** feature module — Angular UI *and* its .NET Web API endpoints.
- **Also did:** picked up **production defects**, and drove a **modularity/reusability refactor**.
- **Honest boundary:** deep C# runtime internals and deep Azure infrastructure were not my domain — I owned my module's code, both sides, and the containerized run of it.

---

## Architecture walkthrough — what to draw on the whiteboard

Draw this left to right and narrate it:

```
[ Angular SPA ]  --REST-->  [ .NET C# Web API ]  -->  [ SQL DB ]
  Schedule-Manager            Schedule endpoints          schedules,
  - Kendo grids               - [HttpGet] DataSourceRequest  equipment,
  - cascading dropdowns       - ToDataSourceResult(IQueryable) categories
  - reactive forms            - global exception middleware
        |                            |
        +--- both containerized with Docker ---+
                        |
                  [ Azure hosting ]
              (same image: local / CI / prod)
```

**Narration:** *"The Angular SPA makes REST calls to .NET Web API services. My Schedule-Manager grids sent their state — page, sort, filter — to a `[HttpGet]` endpoint that used Kendo's `DataSourceRequest` to page in SQL and return just one page. Everything's containerized with Docker, so the same image runs locally, in CI, and on Azure. The category/subcategory data is a parent/child hierarchy the frontend renders as cascading dropdowns."*

---

## STAR Story 1 — The server-side grid performance debate *(your best story)*

**Situation.** Subsea's Kendo grids showed large schedule and equipment datasets with sorting, filtering, grouping, and inline editing. They were bound **client-side** — the whole dataset loaded into the browser — and as the data grew, the grid got noticeably sluggish on every sort and filter. There was a disagreement on the team about whether it was worth the effort to change the approach.

**Task.** I believed we should move data operations **server-side** — do paging, sorting, and filtering in the .NET API rather than in the browser — but I needed to convince the team, not just assert it.

**Action.** Rather than argue in the abstract, I **demonstrated the problem**: I loaded the grid with a **realistic data volume** and let everyone *see* the lag on client-side sort/filter, then showed a **server-side version** staying responsive on the same data. Then I built it properly: the Angular grid serialized its state with Kendo's `toDataSourceRequestString`, the .NET controller took a `[DataSourceRequest]` and called `ToDataSourceResult` over an `IQueryable`, so paging/sort/filter composed into a **single SQL query** and only one page of rows crossed the wire. I added **virtual scrolling** and made sure we only requested the columns we displayed.

**Result.** The grid stayed responsive even on large datasets, the team agreed on server-side, and I owned it end to end — both the Angular wiring and the .NET side. **What I take from it:** I win technical arguments with **evidence and user impact**, not volume — and once the team commits to a decision, I commit fully, even if it's not the one I proposed.

*(Full technical detail in `02-kendo-grid-deep-dive.md`.)*

---

## STAR Story 2 — Production bug-fix methodology

**Situation.** I was one of the people who picked up production defects. A common pattern was **grid/state bugs** — filters or inline edits not persisting correctly after a reload.

**Task.** Fix them so they *stayed* fixed, without the fix bouncing back.

**Action.** My approach is **reproduce-first, never guess-and-deploy**:
1. Get the **exact repro steps** and the affected **data shape** from the report.
2. **Reproduce locally** against the same data shape — Docker made this reliable because local matched prod.
3. **Isolate the layer** — use the browser network tab to decide frontend vs .NET API.
4. **Fix at the correct layer** — for the grid-state bugs, that usually meant preserving the grid's state object (page/sort/filter) across the edit-and-reload cycle instead of letting it reset.
5. **Add a guard/regression test** so the specific failure can't silently come back.

**Result.** Fixes were durable instead of whack-a-mole. **What I take from it:** methodical reproduction is what stops fixes from bouncing — the discipline of reproducing before touching code is more valuable than being fast.

---

## STAR Story 3 — Dynamic category/subcategory management

**Situation.** Subsea needed **data-driven** categories and subcategories — admins could add or edit them, and the UI had to reflect that hierarchy live. Hardcoding categories was a non-starter.

**Task.** Build a UI where choosing a category filters its subcategories, driven entirely by data from the API.

**Action.** I modeled it as a **parent/child relationship** from the .NET API and built **cascading reactive-form dropdowns**. Selecting a category subscribes to the control's `valueChanges`, uses **`switchMap`** to reload just the dependent subcategories (cancelling any stale request), and enables the subcategory control only when there are children. I reloaded *only* the dependent dropdown, not the whole tree, to keep it snappy — and I packaged the cascade as a **reusable component** so other screens could reuse it.

**Result.** Adding a new category became a **data change, not a code change**, and the cascade was reused across the module. **What I take from it:** building things **data/config-driven** makes future "changes" cheap — a lot of would-be code changes became configuration.

*(Full technical detail in `01-frontend.md` Q6–Q7.)*

---

## STAR Story 4 — Modularity / reusability refactor

**Situation.** Across Schedule-Manager (and beyond) there was **repeated code** — the same Kendo grid configuration and category-selector setup copy-pasted in multiple places — which meant bugs had to be fixed in several spots.

**Task.** Reduce duplication and make the module cleaner and more reusable.

**Action.** I extracted the repeated pieces into **shared, configurable components**: a **reusable grid component** (columns + server-side binding + paging/sort/filter, driven by `@Input`/`@Output`) and the **category/subcategory cascade selector**. I also **tightened the feature-module boundaries** so Schedule-Manager didn't leak dependencies into unrelated areas, and kept it as a **lazy-loaded module**.

**Result.** New grids and new places needing the category cascade became a **configuration change instead of new bespoke code**, and a bug got fixed once in the shared component rather than everywhere. The module was more maintainable and its boundaries were cleaner.

---

## "What would you do differently?"

**Answer.** *"I'd standardize the grid data-binding pattern across the whole app from the start. We ended up with a mix of client- and server-bound grids, and if we'd unified on server-side operations from day one we'd have avoided the performance issues entirely rather than fixing them after they showed up. I'd also add more automated integration tests around the .NET endpoints — we leaned on unit tests and manual QA, and stronger integration coverage would have caught grid-state regressions earlier."*

This is a strong answer because it's **honest, specific, and shows judgment** — it's the general version of your best win.

---

## Agile / collaboration

**Answer.** *"We worked in Agile sprints with Jira for stories and bugs and Git PRs for code review. Because I was building **both sides** of Schedule-Manager, I coordinated closely on the **.NET API contracts** — agreeing the request/response shapes early so the Angular and backend work fit together. Standup is where I raised blockers early, and I treated a green CI pipeline as the gate before merging or shipping."*

---

## How do you handle a disagreement on technical approach?

**Answer.** *"The grid client-vs-server debate is my go-to example. I argued for server-side operations, but instead of just asserting it I **demonstrated the lag** on realistic data so the trade-off was visible. We aligned on server-side, and I committed to it fully. I focus on evidence and user impact, and once the team decides, I own the decision even if it wasn't originally mine."*

---

## How do you approach building a feature end to end?

**Answer.** *"Clarify the requirement and acceptance criteria, agree the **API contract** with the backend early — which for Subsea I often owned both ends of — then sketch the component/service breakdown. I build behind a feature branch with the happy path plus loading/empty/error states, add tests as I go, self-review the diff, open a PR, and demo in sprint review. I like to ship a thin vertical slice first, then iterate."*

---

## Questions to ask the interviewer (shows seniority)

- "How are you handling data-heavy views today — client-side or server-side grid operations?"
- "What does your CI/CD gate look like before a deploy?"
- "How is state management handled across the frontend — services, or a store like NgRx?"
- "Where does the team feel its biggest technical debt is right now?"

---

### One-line reminders before you walk in

- **Tie every answer back to Subsea.** The interviewer wants project understanding — give it to them unprompted.
- **Lead with the story, drop to code only if pushed.**
- **Your best card is the server-side grid debate** — measurement beat opinion.
- **Be honest about depth** — Angular-emphasis full-stack who owned a module both sides. That's a strength, not an apology.
- **Speak slower than feels natural.** Nerves make you rush.
