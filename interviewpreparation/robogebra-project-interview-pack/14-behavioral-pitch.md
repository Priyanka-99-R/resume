# 14. Behavioral, Project Pitch & Delivery — tied to Robogebra

Every interview opens and closes here. Prepare these until they're smooth. Fill `[...]` with your specifics (years, team size, etc.).

---

## 1. 60-second self-introduction (template)

> "I'm Priyanka, a full-stack engineer with [X] years building web applications, primarily **Angular on the front end and Java/Spring Boot with MongoDB on the back end**. Currently I work on **Robogebra**, an ed-tech learning platform for students and coaching institutes. I work across the stack — the **Angular student/teacher web app** and the **Spring Boot backend** — on features like solution management, a teacher-controlled **solution-visibility** system, authentication with AWS Cognito, and subscription flows. I care about clean, layered code, and I've owned features end to end: from the MongoDB data model and REST API through to the Angular UI. I'm looking for a role where I can keep growing as a senior full-stack engineer."

Keep it: who → stack → current project → what you own → what you want.

---

## 2. 60-second project pitch (Robogebra)

> "Robogebra is a learning platform with a two-sided market — individual students and coaching institutes. Architecturally it's a **microservices hybrid**: a Spring Boot **portal** is the monolithic learning core (books → chapters → exercises → solutions), a separate **CRM** service handles institutes/coupons/subscriptions, and there are multiple frontends — an Angular web app, an Ionic mobile app, and a React CRM. Data is in **MongoDB**, auth is **AWS Cognito with JWT** across three user pools, and we use S3 for assets. I've worked mostly on the portal and the Angular app — recently I built the **solution-visibility feature**, where teachers control when their classes can see solutions at chapter, exercise, or question level."

---

## 3. STAR stories (use REAL features you built)

**STAR = Situation, Task, Action, Result.** Two solid stories beat ten vague ones.

### Story A — "A challenging feature you built" → Solution Visibility
- **S:** Teachers wanted to stop students seeing solutions before they'd attempted problems, but control needed to work at three levels — whole chapter, an exercise, or a single question.
- **T:** Design and build it end-to-end — data model, backend rules, and the UI — without slowing down the student's solution page.
- **A:** Modeled it as one `solution_visibility` document per *(class, target)* with a **unique compound index** so enable/disable is a clean upsert. For the student check I resolved a **precedence** — question → exercise → chapter, most-specific wins, default open — and did it in **one** MongoDB query by fetching all candidate rows and grouping in memory, so no extra latency. On the frontend I added a lock indicator with a hover tooltip and reused the existing per-item control.
- **R:** Teachers can now gate solutions per class at any level; the access check is a single round-trip; students outside an institute are never affected. It shipped cleanly and the precedence design made later extension (exercise-level gating) a small change.

### Story B — "A tricky bug / production issue" → (adapt to a real one)
- Pick a real incident: e.g. a **token-expiry** causing failed requests. **A:** added silent token refresh in the HTTP interceptor (`switchMap` retry of the original request with the new token) + session-expired handling. **R:** users stopped getting random 401s; sessions refresh transparently.

### Story C — "Disagreement / ownership"
- A design call where you pushed for the layered Entity↔Model↔DTO separation (or the extra persistence-service layer) for maintainability, explained the trade-off, and the team adopted it.

*Prep 3–4 short stories; you can flex them to "challenge / conflict / failure / proud-of / learned-something."*

---

## 4. Common behavioral questions — crisp answers

- **Biggest strength:** owning features end-to-end across the stack + writing clean, layered code others can maintain.
- **Weakness (honest + improving):** "I've been deep in Angular 16 with zone.js; I'm actively learning signals/zoneless and standalone components to stay current." (True for your project — see `01-frontend.md`.)
- **How do you handle tight deadlines:** scope to the critical path, communicate trade-offs early, ship a solid MVP, iterate.
- **How do you keep learning:** building features that stretch me (auth, caching), reading the framework docs, and code review.
- **Why leaving / why this role:** growth into senior full-stack ownership and [company-specific reason].

---

## 5. Senior "ownership" talking points (sprinkle these)

- "I think in **trade-offs** — e.g. MongoDB gives us schema flexibility for varied content but not multi-doc ACID, so money flows stay in one service."
- "I design for **failure** — timeouts/retries on external calls, graceful degradation if the AI service is down."
- "I keep **cross-cutting concerns declarative** — custom annotations (`@PermissionContext`, `@FeatureUsageQuota`) backed by filters/interceptors."
- "I care about the **whole request lifecycle** — from the Angular guard/interceptor to the Spring filter chain to the Mongo index."

---

## 6. Git & delivery (be ready for the "how do you ship" question)

- **Branching:** feature branches off `main`/`develop`, small PRs, code review before merge.
- **CI/CD:** the repo has a **`.github` pipeline** and a **Dockerfile** — build + tests run on every PR; nothing merges red; containerized deploys.
- **Environments:** `application-dev.yml` / env vars (`${VAR:default}`) keep the same artifact deployable across dev/prod with different secrets.
- **Quality gate:** integration tests (Testcontainers) + frontend specs run in CI (see `11-testing.md`).

---

## 7. Smart questions to ask the interviewer

- "How is the team structured, and how do features move from idea to production?"
- "What does the testing / CI culture look like here?"
- "What are the biggest technical challenges the team is facing right now?"
- "What does growth look like for a senior engineer on this team?"
- "How do you balance shipping speed with code quality / tech debt?"

---

## 8. Logistics (have answers ready)

- **Notice period / availability:** [your notice].
- **Salary expectation:** give a researched range; "based on my experience and the market for senior full-stack roles, I'm looking at [range], but I'm open to discussing the whole package."
- **Location / remote:** [your preference].

---

### The night-before checklist

- [ ] 60-sec intro + project pitch, smooth without notes
- [ ] 3 STAR stories ready (solution-visibility is your strongest)
- [ ] Can whiteboard the architecture (`06-microservices.md`) and the S3 upload (`12-system-design.md`)
- [ ] Can explain memory leaks / `takeUntil` (`01`), auth chain + `@PermissionContext` (`09`), error handling (`08`)
- [ ] Honest on zoneless/signals, test coverage, Mongo trade-offs — reality + direction
- [ ] 2–3 questions ready for them
