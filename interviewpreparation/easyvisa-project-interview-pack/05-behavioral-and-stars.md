# 5. Behavioral, Pitch & STAR Stories — EasyVisa

**Purpose:** the human/story layer. The interviewers fail me on "project understanding," so the fix is to make every behavioral answer concrete — a real EasyVisa feature, a real decision, a real trade-off. Use STAR: **S**ituation, **T**ask, **A**ction, **R**esult.

---

## The 60-second project pitch (memorize)

> "EasyVisa is a visa case management platform for immigration attorneys — it manages applications, documents, and the relationships between cases, applicants, and petitioners. I was the frontend engineer, from late 2021 to early 2024, on a team of about six to eight. The app was Angular, talking to a Grails/Groovy backend, with PostgreSQL for transactional data and Neo4j as a graph database for the relationships between parties.
>
> I owned two things end-to-end. First, the **document portal** — accordion upload panels where attorneys upload the many documents a case needs, with per-file progress and isolated error handling using Angular's `HttpClient` event stream. Second — and this is the part I'm proudest of — the **dynamic forms**. Visa forms are enormous, conditional, and repeatable: a case can have many applicants, each with nested address and employment histories, and fields appear or disappear based on earlier answers. Instead of hand-coding each form, I built them with **Angular Formly, driven by JSON configuration** — so new forms and new conditional rules became *configuration, not code*. I also customized an image cropper for applicant photos, drove an Angular version upgrade, built reusable components, and wrote the unit tests and documentation. My work made complex, document-heavy visa workflows genuinely manageable for attorneys."

---

## My role (crisp version)

> "Frontend engineer owning the document portal and the dynamic Formly forms, plus shared/reusable components. I coordinated the REST API contracts with the Grails/Groovy backend team, consumed data from both Postgres and Neo4j through those APIs, wrote Jasmine/Karma unit tests for all my components and services, and documented specs and user manuals. I was frontend-focused — I understood the whole system but my hands-on ownership was the Angular side."

---

## The architecture to draw (whiteboard walkthrough)

Draw this left-to-right and narrate it:

```
[ Attorney browser ]
        |
        v
[ Angular SPA ]  ── document portal (upload panels, HttpClient progress)
   |    |         ── dynamic Formly forms (JSON config, nested, repeatable)
   |    |         ── feature services + HTTP interceptor (auth, errors)
   |    |
   |    |  REST / JSON (agreed contracts)
   v    v
[ Grails / Groovy backend ]  ── controllers → services → GORM
        |                \
        v                 v
 [ PostgreSQL ]        [ Neo4j ]
 records, cases,       graph of applicants ↔
 documents, status     petitioners ↔ cases
 (ACID, transactional) (relationship traversals)
```

Narration: "Attorneys use an Angular SPA. My two big modules are the document portal and the Formly-driven forms. The app talks to a Grails/Groovy backend over REST — controllers to services to GORM. Transactional records live in Postgres; the relationships between people and cases live in Neo4j because that's a graph problem. I owned the Angular layer and the API contracts."

---

## STAR story 1 — The Formly forms decision (my signature story)

- **S (Situation):** Visa forms are huge, conditional, and repeatable — a case can have N applicants, each with nested address/employment history, and fields appear/disappear based on earlier answers. The initial instinct on the team was to hand-code each form.
- **T (Task):** Build the forms so they were maintainable and could change as immigration requirements changed — without a code release and a new component for every form or rule.
- **A (Action):** I introduced **Angular Formly** and drove a **JSON-driven** approach: forms rendered from a `FormlyFieldConfig` array, `fieldGroup` for nesting, `fieldArray` for repeatable sections (including nested repeats — applicants containing address histories), and `expressions` for conditional show/hide and dynamic `required`. I wrote reusable custom field types (a `repeat` section type used everywhere) and custom validators for visa-specific rules, and I relied on reactive-forms validity aggregation so errors bubbled up and the submit button gated the whole nested tree.
- **R (Result):** New forms and new conditional rules became **configuration, not code** — dramatically faster to add and change, and far less bug-prone. Mid-sprint form changes went from "new component + tests" to "edit JSON." It was my biggest technical decision on the project and it clearly paid off at that scale of forms.
- **Trade-off I volunteer:** steeper learning curve and some complexity writing custom field types up front — worth it for the long-term savings; over-engineering if there'd only been two simple forms.

---

## STAR story 2 — The document upload challenge

- **S:** Attorneys upload many documents per case, some large. Naive uploads blocked the UI, risked timeouts, and if one file failed it could take down the whole batch's UX.
- **T:** Build an upload experience with per-file progress, isolated error handling, and no UI freezing.
- **A:** I used `HttpClient` with `reportProgress: true` and `observe: 'events'`, reading `HttpEventType.UploadProgress` to drive a per-file progress bar and `HttpEventType.Response` to mark completion. I organized uploads into **accordion panels by document type**, each with its **own progress/error state**, so one failed upload showed its error while the others kept going. Uploads were concurrent but throttled, and every subscription used `takeUntil(destroy$)` so navigating away mid-upload didn't leak.
- **R:** Attorneys got responsive, transparent uploads with clear per-file feedback, and a single failure no longer sank the case. The upload panel became a reusable component driven by a document-type config.
- **What I'd do differently:** for very large files I'd move to **chunked/resumable** uploads from the start — I flagged it as the next iteration; we shipped the progress-stream version first because it covered the real file sizes in use.

---

## STAR story 3 — Customizing the image cropper

- **S:** Certain documents — applicant photos especially — had strict dimension, aspect-ratio, and format requirements. Attorneys shouldn't have to pre-edit images in another tool.
- **T:** Enforce those photo constraints in-app and feed compliant images into the upload flow.
- **A:** I wrapped and **customized an Angular image-cropper library**: locked the crop box to the required aspect ratio, forced the output size/format, and **post-processed the output blob** before it entered the document upload pipeline. I wrapped it as a reusable component (and a Formly field type) so it could drop in anywhere.
- **R:** Photos were compliant by construction — no back-and-forth, no rejected documents for wrong dimensions — and the cropper became a reusable control across the app.

---

## STAR story 4 — A debugging story

- **S:** A conditional field in a nested, repeatable Formly section wasn't behaving — after adding/removing applicants, a field that should have been hidden (or its `required` rule) wasn't re-evaluating correctly, and the submit button's enabled state didn't match what the attorney saw.
- **T:** Find whether the bug was in my form config, in Formly's rebuild of the nested `FormArray`, or in a stale validation state — and fix it at the right layer.
- **A:** I reproduced it with exact steps, then isolated the layer: I inspected the underlying `FormGroup`/`FormArray` validity and the model as sections were added/removed, and traced it to how the conditional `expressions` and validators re-registered when the array rebuilt. I fixed it so hidden fields dropped their controls from validation and newly added entries registered validators immediately, then added a **unit test** asserting the show/hide and `required` behavior across add/remove so it couldn't silently regress.
- **R:** Submit state matched the visible form exactly, and the regression test locked it in. This is why I prioritized unit tests on the conditional logic — that's precisely where nested-dynamic-form bugs hide.
- **Method I'd emphasize:** reproduce first, isolate the layer (frontend vs API vs data via network tab and logs), fix at the correct layer, add a regression test — I avoid guess-and-deploy.

---

## "What would you do differently on EasyVisa?"

> "Three things. One, move large uploads to **chunked/resumable** from the start rather than as a follow-up. Two, serve the **form JSON configs from the backend** instead of shipping them in the frontend build — then non-developers could adjust forms without a release, fully realizing the 'forms as data' idea. Three, add more **integration tests around the conditional form logic**, since that's where regressions hide. I'd also move the heavy document-portal components to **OnPush** change detection for a cleaner perf story."

---

## Collaboration, Agile & Git

**Agile process:**
> "Two-week sprints — planning, daily standups, sprint review/demo, retro. Work tracked in Jira as stories and bugs, code in Git with feature branches and PR reviews. Standup is where I flagged blockers early, especially anything waiting on a backend API contract."

**Cross-team collaboration:**
> "The tightest coordination was with the Grails/Groovy team on the **REST contracts** — agreeing field names, status codes, and error shapes up front so frontend and backend moved in parallel — and on **what data came from Postgres vs Neo4j** so I knew which endpoint served record data versus relationship data."

**Code reviews:**
> "I look for correctness first, then maintainability: clear naming, proper unsubscription/async-pipe usage, error and loading states, no obvious perf traps, and adequate tests. Specific, kind, actionable comments, and I distinguish blocking issues from nits."

**Handling changing requirements:**
> "I clarify the change with the PM, assess impact on the sprint commitment, and raise it transparently rather than silently absorbing it. And on EasyVisa the config-driven Formly design made many 'changes' cheap — a new field or rule was JSON, not a new component."

**Biggest technical decision + trade-off:**
> "Choosing Angular Formly with JSON-driven config over hand-coding each visa form. The trade-off was a steeper learning curve and custom-field-type complexity versus huge long-term savings — forms and conditional logic became configuration. At that scale of forms, it clearly paid off."

**Disagreement handled well:**
> "When we debated hand-coding forms versus a config-driven approach, rather than just asserting, I showed the maintenance cost concretely — how many conditional, repeatable forms we'd have to build and re-build by hand — and how Formly turned each new rule into one line of config. We aligned on Formly, and I committed to owning the custom field types that made it work."

---

## Questions I can ask the interviewer (shows engagement)
- "How do you handle dynamic/complex forms — Formly, hand-coded, or a schema service?"
- "Do you use polyglot persistence like we did with Postgres + Neo4j, or a single store?"
- "What's your change-detection and performance strategy on large Angular trees?"
- "How is the frontend/backend API contract agreed and versioned on your team?"

---

## Final confidence anchors
- Lead with the **Formly JSON-driven forms** — it's my strongest, most unique story.
- Always land technical answers with **"In EasyVisa, I…"**.
- Be honest about the **frontend boundary** (consumed Grails/Neo4j) — it reads as senior, not weak.
- Own the **trade-offs** (chunked uploads, backend-served form JSON, OnPush) — owning what I'd improve is what separates 5-years from junior.
