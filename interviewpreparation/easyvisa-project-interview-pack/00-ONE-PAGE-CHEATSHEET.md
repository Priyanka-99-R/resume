# EasyVisa — One-Page Cheat Sheet

*30-minute pre-interview glance. 20 must-know facts, each with a one-liner I can say out loud.*

---

## The pitch (memorize this verbatim)
> "EasyVisa is a visa case management platform for immigration attorneys — managing applications, documents, and the relationships between cases, applicants, and petitioners. I was the frontend engineer on an Angular app talking to a Grails/Groovy backend, with PostgreSQL for transactional data and Neo4j for the graph of relationships. I owned the document portal and the dynamic Formly forms — I made huge, conditional, repeatable visa forms into JSON configuration instead of hand-coded components."

---

## The 20 facts

1. **What it is** — visa case management for immigration attorneys. Cases, applicants, petitioners, documents. Nov 2021 – Jan 2024.
2. **My role** — frontend engineer, team of ~6–8. I owned the **document portal** and **dynamic forms**, and built shared components.
3. **Stack in one breath** — Angular front end, Grails/Groovy back end, PostgreSQL + Neo4j.
4. **My signature achievement** — **JSON-driven, nested, repeatable Angular Formly forms with conditional logic.** "New forms became config, not code."
5. **Why Formly** — visa forms are huge, conditional, and repeatable (N applicants, each with nested address/employment history). Hand-coding is unmaintainable; Formly renders from a JSON field config.
6. **Formly nesting** — `fieldGroup` for nested groups, `fieldArray` for repeatable sections (add/remove N applicants).
7. **Formly conditional logic** — `expressions: { hide: 'model.hasEmployer === false' }` for show/hide; dynamic `required` the same way.
8. **Formly validation** — nested `FormGroup` aggregation + custom validators; errors bubble up so the **submit button disables** until the whole nested tree is valid.
9. **Reactive vs template forms** — I used **reactive forms** (form model is the source of truth), which Formly is built on. Template-driven doesn't scale to this complexity.
10. **Document portal** — accordion panels by document type; each panel has its own upload/progress/error state so one failure doesn't sink the others.
11. **Large uploads** — `HttpClient` with `reportProgress: true, observe: 'events'`, read `HttpEventType.UploadProgress` for a per-file progress bar. Next step would've been chunked/resumable.
12. **Image cropper** — customized an Angular image-cropper library to enforce aspect ratio/output size/format for applicant photos, then fed the output blob into the upload flow.
13. **Memory leaks** — `takeUntil(destroy$)` completed in `ngOnDestroy`, plus the **async pipe** in templates so subscriptions clean themselves up.
14. **Duplicate/rapid API calls** — `debounceTime` + `switchMap` so only the latest request wins and stale ones are cancelled (typeaheads, live validation).
15. **State** — feature services wrapping the Grails REST endpoints; **BehaviorSubject** for shared state; the Formly **model object is the single source of truth** for form state (made draft save/restore trivial).
16. **Component communication** — `@Input`/`@Output` for parent–child; shared service + `BehaviorSubject` for cross-tree.
17. **Backend** — **Grails**: Groovy-based, convention-over-configuration, built on Spring + Hibernate; **GORM** for persistence; controllers expose REST JSON. I **consumed** these APIs (I was frontend-focused).
18. **Why two databases** — **Postgres** = relational/transactional (case data, integrity, ACID). **Neo4j** = the graph of who-is-connected-to-whom (applicant ↔ petitioner ↔ case ↔ employer/family).
19. **Graph vs SQL** — "show everyone connected to this case, 3 hops out" is a natural Cypher traversal and a painful pile of self-joins in SQL. Graph DBs store relationships as first-class edges.
20. **Also did** — drove an **Angular version upgrade** (incremental `ng update`, fix breaking changes at each step), wrote **Jasmine/Karma unit tests** for all components/services, wrote **specs and user manuals**.

---

## Rapid-fire one-liners (if pushed)

| They ask… | I say… |
|-----------|--------|
| Lifecycle hooks | "`ngOnInit` to load, `ngOnChanges` for input changes, `ngOnDestroy` to complete my `destroy$` — I used all three in the upload panels." |
| Observable vs Subject vs BehaviorSubject | "Observable is a stream; Subject is a multicast stream you push into; BehaviorSubject remembers the last value — I used it for shared form/case state." |
| Prevent memory leaks | "`takeUntil(destroy$)` + async pipe — standard in every EasyVisa component." |
| Restrict multiple API calls | "`debounceTime` + `switchMap` — latest wins, stale cancelled." |
| Route guards / lazy loading | "`CanActivate` guarded attorney routes; feature modules lazy-loaded; resolver pre-fetched case data before the page opened." |
| Global exception handling | "Per-call `catchError` → user message, plus a shared HTTP interceptor for auth + global failures." |
| Interface vs abstract class | "Interface = a contract, no implementation, multiple; abstract class = partial implementation + shared state, single. I typed my Formly configs and API models with TS interfaces." |
| try/catch/finally | "`try` risky code, `catch` handle, `finally` always runs (cleanup). In RxJS the equivalent is `catchError` + `finalize`." |
| Why Neo4j | "Relationships are the domain. Traversing 'who's connected to this case' is native in a graph, painful in SQL joins." |
| Reactive vs template forms | "Reactive — model is source of truth, testable, scales to nested/dynamic. Formly is built on reactive forms." |

---

## Confidence anchors (say these, they're all true)
- "I owned the frontend end-to-end and I understood the whole system."
- "I was frontend-focused — I consumed the Grails REST APIs; Neo4j was mostly server-side, but I understand why we had it."
- "My proudest work is turning unmaintainable hand-coded visa forms into JSON-driven Formly config."
