# Robogebra — Project-Based Interview Pack

**Target:** Senior / 5+ years Full-Stack (Angular · Java 17 · Spring Boot · MongoDB · Microservices · AWS)
**Idea:** Every answer is tied to **real code in the Robogebra project** — so when an interviewer asks *"where did you use X in your real project?"*, you have a concrete file, pattern, and story.

---

## How to use this pack

- Each file = one interview area. Read the **Question → Answer → "In Robogebra"** structure.
- The **"In Robogebra"** block is your talking point — memorize the *flow*, not the code character-for-character.
- Say answers **out loud**. If asked to go deeper, you can reference the file path.

## The sections

**Core areas (your requested format):**

| # | File | Covers |
|---|------|--------|
| 1 | `01-frontend.md` | Angular — memory leaks/`takeUntil`, RxJS, change detection, zone.js vs zoneless, DI, guards, resolvers, interceptors, forms, state, performance |
| 2 | `02-backend.md` | REST API design, controllers, layering, request→auth lifecycle, the "email already exists" flow |
| 3 | `03-java.md` | OOP (Entity↔Model↔DTO), Collections & ArrayList, Streams, Java 17 features |
| 4 | `04-spring.md` | Spring Boot, IoC/DI, Spring Data, `@Transactional`, validation, config, `@Async`/scheduling |
| 5 | `05-database.md` | MongoDB — documents, indexes, `@DBRef`, repositories, the precedence-query pattern |
| 6 | `06-microservices.md` | Architecture, portal↔CRM communication, service boundaries, whiteboard diagram |
| 7 | `07-aws.md` | Cognito (3 pools), S3, SES/SNS/Lambda, signup + JWT auth |

**Deep dives (the details interviewers push on):**

| # | File | Covers |
|---|------|--------|
| 8 | `08-error-handling.md` | Global `@ControllerAdvice`, all custom exceptions, error-code enums, 3rd-party error remap, filter-level errors |
| 9 | `09-auth-deep-dive.md` | **Authentication** (10-filter chain, dual JWT, 3 Cognito pools) + **Authorization** (`@PermissionContext`, role/permission model, `validateAsTeacher`) |
| 10 | `10-annotations-reference.md` | Every annotation used (Spring/Lombok/Mongo/validation/Swagger) + your **custom** `@interface`s |

**Extra topics to crack the interview:**

| # | File | Covers |
|---|------|--------|
| 11 | `11-testing.md` | JUnit 5 + Mockito + **Testcontainers** (backend), Jasmine/Karma (frontend), test strategy |
| 12 | `12-system-design.md` | Whiteboard walkthroughs: solution-visibility, S3 upload, notifications, scaling |
| 13 | `13-performance-caching.md` | Caffeine cache, Mongo indexing, pagination, `@DBRef` N+1, frontend perf |
| 14 | `14-behavioral-pitch.md` | 60-sec intro, project pitch, **STAR stories from real features**, questions to ask, Git/CI-CD |

## Key project facts (know these cold)

- **robogebra-web** — student/teacher web app — **Angular 16.2**, NgModule, **zone.js** (not zoneless), no NgRx (BehaviorSubject-based state)
- **robogebra-backoffice** — admin/ops UI — Angular 16.2
- **robogebra-mobile** — **Ionic 8** (Angular + Capacitor), Angular 18.2
- **robogebra-portal** — main backend — **Spring Boot 3.2, Java 17, Maven, MongoDB** (`robogebra_cms_db`)
- **robogebra-admin-portal** — second Spring Boot backend for AI/content generation
- **robogebra-crm** — **Node.js/Express** microservice (institutes, coupons, subscriptions), MongoDB (`robogebra-coupon-mgmt`)
- **robogebra-crm-web** — **React 18 + Vite**
- **robogebra-website** — static (Parcel)
- **Auth** — AWS **Cognito** (3 user pools: user / email-user / institute-user) + **JWT** + OAuth2 (Google/LinkedIn)
- **Architecture** — microservices hybrid; portal (3000) ↔ CRM (3001) over REST

## Honesty note (important for senior interviews)

The project is **Angular 16 with zone.js** — it is **not** zoneless and does **not** use signals or standalone components yet. When asked about zoneless/signals, answer as: *"We're on Angular 16 with zone.js and OnPush-where-needed; here's how zoneless/signals work and how I'd migrate."* Interviewers respect the honest "here's our reality + here's the modern direction" answer far more than pretending.
