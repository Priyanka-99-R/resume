# 3. Backend (Grails / Groovy) — Interview Q&A tied to EasyVisa

**Honest framing (say this):** I was the **frontend engineer**. I owned the Angular side and **consumed** the Grails/Groovy REST APIs — I read and understood the backend, coordinated the API contracts, and occasionally worked in it, but I did not write Grails day-to-day. So my answers are *"here's what Grails is, here's how our backend was shaped, and here's exactly how I consumed it,"* not "I'm a Grails expert." That honesty is a feature, not a weakness — it shows I understood the whole system and knew my boundary.

**Stack:** Grails (Groovy) backend, GORM over Hibernate to **PostgreSQL**, plus **Neo4j** for graph relationships (see `04-database-postgres-neo4j.md`). REST/JSON endpoints consumed by the Angular app.

---

## Q1. What is Grails?

**Answer.** Grails is a **full-stack web framework built on Groovy** (a JVM language), following **convention over configuration** — the Rails philosophy on the JVM. Under the hood it's **built on Spring Boot and Hibernate**, so you get Spring's dependency injection and Hibernate's ORM, but with far less boilerplate because Grails wires sensible defaults by convention.

Key ideas:
- **Groovy** — a dynamic (and optionally typed) JVM language; less ceremony than Java, interoperates with Java libraries.
- **Convention over configuration** — put a controller in `grails-app/controllers`, name it `CaseController`, and it's auto-mapped to `/case` routes without XML/annotation wiring.
- **GORM** — Grails' Object Relational Mapping, its data-access layer (over Hibernate for SQL, with a Neo4j implementation too).
- **Built on Spring** — services are Spring beans, injected by name.

**In EasyVisa.** The backend was Grails/Groovy exposing REST endpoints. From my seat, the important thing was that it produced clean JSON resources — cases, applicants, petitioners, documents — that my Angular services consumed.

---

## Q2. What is GORM and how does persistence work?

**Answer.** **GORM** is Grails' ORM. You define **domain classes** (like `Case`, `Applicant`) and GORM maps them to database tables and gives you dynamic finders and CRUD without writing SQL. It's Hibernate-backed for relational stores.

A domain class looks like:

```groovy
class VisaCase {
    String caseNumber
    String status
    Date filedOn

    static hasMany = [applicants: Applicant, documents: Document]  // relationships
    static constraints = {
        caseNumber blank: false, unique: true
        status inList: ['DRAFT', 'FILED', 'APPROVED', 'REJECTED']
    }
}
```

GORM then gives you dynamic finders and CRUD for free:

```groovy
VisaCase.findByCaseNumber('EV-2023-001')
VisaCase.findAllByStatus('FILED')
def c = new VisaCase(caseNumber: 'EV-2023-002', status: 'DRAFT').save()
```

**In EasyVisa (honest depth).** I understood the domain model — cases have many applicants/documents, applicants link to petitioners — because I had to consume it and agree on the JSON shape. The `hasMany`/relationship modeling on the Postgres side is what my nested Formly forms mirrored on the frontend. I wasn't authoring GORM constraints daily, but I knew what came from Postgres via GORM versus what came from Neo4j.

---

## Q3. How do controllers and REST endpoints work in Grails?

**Answer.** A **controller** is a Groovy class in `grails-app/controllers` whose action methods handle requests. Grails maps them by convention (`CaseController` → `/case`), and you can expose REST resources with `@Resource` or explicit URL mappings. Actions render JSON with `respond` or `render ... as JSON`.

```groovy
class CaseController {
    CaseService caseService          // Spring injects the service by name

    def show(Long id) {
        def visaCase = caseService.getCase(id)
        respond visaCase             // content-negotiated → JSON for our Angular client
    }

    def save() {
        def visaCase = caseService.create(request.JSON)
        respond visaCase, status: CREATED
    }
}
```

Business logic lived in **services** (`grails-app/services`), which are transactional Spring beans injected into controllers — the same layering idea as Spring (controller → service → domain/repository).

**In EasyVisa.** The controllers produced the REST resources my Angular feature services called — `GET /api/cases/:id`, `POST /api/documents`, etc. My job was agreeing the request/response contract with the backend devs and consuming it.

---

## Q4. How did the Angular frontend consume these APIs?

**Answer.** Through Angular **feature services** wrapping `HttpClient`, returning typed Observables, with a shared **HTTP interceptor** for auth headers and global error handling. This is the part I owned fully.

**In EasyVisa:**

```typescript
@Injectable({ providedIn: 'root' })
export class CaseService {
  constructor(private http: HttpClient) {}

  getCase(id: string): Observable<VisaCase> {
    return this.http.get<VisaCase>(`/api/cases/${id}`).pipe(
      catchError(err => { this.notify(err); return EMPTY; })
    );
  }

  saveDraft(id: string, model: unknown): Observable<void> {
    return this.http.put<void>(`/api/cases/${id}/draft`, model);
  }
}
```

- **Typed models** — TS interfaces mirroring the Grails JSON, so the compiler caught contract drift.
- **Interceptor** — attached the auth token and centralized 401/global error handling so components never saw raw HTTP errors.
- **Uploads** — `POST /api/documents` with `FormData` + `reportProgress` (see `01-frontend.md`).

Coordinating these contracts with the Grails team was a big part of my work — we agreed field names, status codes, and error shapes up front so frontend and backend moved in parallel.

---

## Q5. Groovy vs Java — what's different?

**Answer (honest, high level).** Groovy is a **JVM language** that's more concise and dynamic than Java: optional typing, no boilerplate getters/setters, closures, built-in collection sugar, GStrings (`"Hello $name"`), and easy JSON handling. It's Java-compatible — Groovy can call Java libraries and vice versa. Grails leans on Groovy's dynamic nature for its dynamic finders and conventions.

```groovy
// Groovy — concise
def names = cases.findAll { it.status == 'FILED' }.collect { it.caseNumber }
```

**In EasyVisa.** I could read Groovy comfortably and understood the concise idioms in the controllers/services, which mattered when debugging an API mismatch. I'd be honest in an interview that Java/Groovy backend wasn't my primary lane — my depth is Angular/TypeScript — but I understood the code well enough to consume it correctly and to reason about where a bug lived (frontend vs API vs data).

---

## Q6. How was global exception / error handling done, end to end?

**Answer.** Two layers:
- **Backend (Grails):** services threw domain exceptions; the framework mapped them to appropriate HTTP status codes with a JSON error body (`{ message, code }`). Grails supports centralized error handling via `URLMappings` error controllers / exception handlers, analogous to Spring's `@ControllerAdvice`.
- **Frontend (Angular) — my part:** a shared **HTTP interceptor** caught failures globally, and per-call `catchError` mapped errors to user-friendly messages so a raw 4xx/5xx never reached a component.

**In EasyVisa.** From the frontend, the contract that mattered was: errors come back as JSON with a `message` I can show. My interceptor handled auth/session-expiry globally, and services `catchError`'d to a notification service for consistent toasts:

```typescript
// consistent user-facing errors, raw HTTP never reaches the component
uploadDocument(file: File, caseId: string): Observable<UploadState> {
  return this.http.post<UploadState>('/api/documents', this.toFormData(file, caseId))
    .pipe(catchError(err => of({ status: 'error', message: err.error?.message ?? 'Upload failed' })));
}
```

---

## Q7. Interface vs abstract class (they ask this in a backend/OO context)

**Answer.**
- **Interface** — a pure **contract**: method signatures, no implementation (default methods aside), no instance state. A class can implement **many**. Use it to say "this type can do X."
- **Abstract class** — a partial implementation: it can have **implemented methods and state**, and defines a base for subclasses. A class extends **one**. Use it to share common behavior among related types.

Rule of thumb: **interface for a capability/contract across unrelated types; abstract class for shared behavior among related types.**

**In EasyVisa (TypeScript angle).** On the frontend I used **TS interfaces** constantly to type the Grails JSON contracts and the Formly configs — e.g. `interface VisaCase`, `interface FormlyFieldConfig`-shaped models — because they're compile-time contracts with zero runtime cost. Where I had shared component behavior (base upload panel logic), a base **class** was the right tool. That's the exact interface-vs-class distinction, applied in my real code.

---

## Q8. try / catch / finally — and the RxJS equivalent

**Answer.**
- **`try`** — wrap code that might throw.
- **`catch`** — handle the exception (log, remap, recover).
- **`finally`** — always runs, thrown or not — for cleanup (close resources, stop a spinner).

```typescript
try {
  const parsed = JSON.parse(rawDraft);
  this.model = parsed;
} catch (e) {
  this.notify('Could not load saved draft');
} finally {
  this.loading = false;      // always stop the spinner
}
```

**In EasyVisa.** In async/observable code the equivalent is **`catchError`** (the catch) and **`finalize`** (the finally):

```typescript
this.caseService.getCase(id).pipe(
  catchError(err => { this.notify(err); return EMPTY; }),  // catch
  finalize(() => this.loading = false)                     // finally — always runs
).subscribe(c => this.case = c);
```

I used `try/catch/finally` for synchronous risky code (parsing saved draft JSON) and `catchError`/`finalize` for the HTTP/observable flows, which is the more common case in an Angular app.

---

### Rapid-fire recap

| Topic | EasyVisa reality |
|-------|------------------|
| What is Grails | Groovy, convention-over-config, built on Spring + Hibernate |
| GORM | domain classes → tables, dynamic finders, `hasMany` relationships (Postgres) |
| Controllers | Groovy classes, convention-mapped, `respond` JSON, service layer |
| How I consumed it | Angular feature services + `HttpClient`, typed models, interceptor |
| Contract work | agreed field names/status codes/error shapes with the Grails team |
| Groovy vs Java | concise, dynamic JVM language; I read it, consumed it, not my main lane |
| Error handling | backend JSON `{message}` + frontend interceptor/`catchError` |
| Interface vs abstract | contract vs shared behavior; I used TS interfaces for API/Formly types |
| try/catch/finally | sync risky code; `catchError`/`finalize` for observables |
| My honest boundary | owned the Angular frontend; consumed the Grails/Neo4j backend |
