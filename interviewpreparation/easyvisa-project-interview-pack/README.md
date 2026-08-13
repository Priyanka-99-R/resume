# EasyVisa — Project-Based Interview Pack

**Target:** Full-Stack Developer, 5 years (Angular · Grails/Groovy · PostgreSQL · Neo4j · Angular Formly)
**Idea:** Every answer is anchored in **real work I did on EasyVisa** — so when an interviewer asks *"where did you actually use X?"*, I have a concrete feature, pattern, and story instead of a textbook definition.

---

## How to use this pack

- Each file = one interview area. Read the **Question → Answer → "In EasyVisa"** structure.
- The **"In EasyVisa"** block is the talking point — memorize the *flow and the decision*, not the code character-for-character.
- Say the answers **out loud**. Interviewers fail me on "project understanding," so the goal is: no matter what they ask, I bring it back to a real EasyVisa feature — the document portal, the Formly forms, the image cropper, the Postgres/Neo4j split.
- The golden rule: **every technical answer ends with "In EasyVisa, I…"** That single habit is what turns a generic candidate into someone who obviously built the thing.

## The sections

| # | File | Covers |
|---|------|--------|
| 0 | `00-ONE-PAGE-CHEATSHEET.md` | 20 must-know facts + one-liner answers for a 30-min pre-interview glance |
| 1 | `01-frontend.md` | Angular deep dive — lifecycle hooks, component communication, RxJS (Observable/Subject/BehaviorSubject), memory leaks/`takeUntil`/async pipe, `switchMap`/debounce, the document-upload progress code, the image cropper, reusable components, change detection |
| 2 | `02-forms-deep-dive.md` | **The signature file** — Angular Formly JSON-driven forms, `fieldGroup` nesting, `fieldArray` repeatable sections, conditional show/hide, dynamic validation, nested reactive-form state, validation bubbling, reactive vs template-driven |
| 3 | `03-backend-grails-groovy.md` | Grails/Groovy for someone who used it and is rusty — what Grails is, GORM, controllers, REST endpoints, how the Angular app consumed them (honest: I was frontend-focused) |
| 4 | `04-database-postgres-neo4j.md` | **Why two databases** — Postgres for relational/transactional, Neo4j for graph relationships, nodes/relationships, basic Cypher, graph vs relational, "who's connected to this case" |
| 5 | `05-behavioral-and-stars.md` | 60-sec pitch, my role, architecture to draw, 3–4 STAR stories, "what I'd do differently," collaboration/Agile |

## Key project facts (know these cold)

- **EasyVisa** — visa **case management platform for immigration attorneys**. Manage applications, documents, and the relationships between cases, applicants, and petitioners. **Nov 2021 – Jan 2024.**
- **Frontend** — **Angular** (I owned this). NgModule architecture, RxJS-heavy, reactive forms, **Angular Formly** for dynamic forms.
- **Backend** — **Grails / Groovy** (convention-over-configuration, built on Spring + Hibernate). Exposed REST endpoints I consumed.
- **Databases** — **PostgreSQL** for relational/transactional data + **Neo4j** graph DB for the relationships between applicants ↔ petitioners ↔ cases.
- **My modules** — the full **document portal** (accordion upload panels, `HttpClient` progress uploads), **JSON-driven dynamic/nested/repeatable Formly forms with conditional logic** (my signature achievement), a **customized image cropper** for applicant photos, **reusable components**, an **Angular version upgrade**, and **unit tests** (Jasmine/Karma) for all UI components/services + specs/user manuals.
- **Team** — ~6–8 (frontend, Grails/Groovy backend devs, QA, lead). I was the frontend engineer owning the document portal and dynamic forms.
- **The one thing to remember** — if I only get to say one thing, it's the **JSON-driven Formly forms**: visa forms are huge, conditional, and repeatable, and I made them *configuration instead of code*.

## Honesty note (important — this is what makes it credible)

I was a **frontend engineer**. I owned the Angular side end-to-end, and I **consumed** the Grails/Groovy REST APIs — I did not write the backend day to day, and Neo4j/Cypher was primarily server-side. So my backend and graph-DB answers are framed as *"here's how it worked, here's how I consumed it, and here's what I understand about why it was built that way"* — not as a Grails or Neo4j expert. Interviewers respect a confident "I owned the frontend, I understood the whole system, and here's the boundary of what I did myself" far more than someone who overclaims and then can't go deep. Where I'd improve the system (chunked uploads, backend-served form JSON), I say so — owning trade-offs reads as senior.
