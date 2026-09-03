# 🔴 TOMORROW — DELOITTE · Fullstack Java with Angular · Tue 25 Aug 2026, 3:00–4:00 PM IST

**Company:** **Deloitte USI**
**Role:** `USI-EH27 – Consulting Services – AI&E – EaaS – SWE – Software Engineer II – Fullstack Java with Angular`
**Format:** Virtual · **60 minutes** · Software Engineer II (mid-level: ~3–6 yrs)

> **Decoding the requisition** — don't say any of this out loud, just use it:
> - **USI** = **Deloitte U.S.–India**, the India delivery arm serving Deloitte US engagements. Expect a **structured, rubric-scored** round — the interviewer works down a checklist across Java, Spring, Angular, SQL and communication, and scores each. **Breadth beats one deep dive**: a decent answer on ten topics scores higher than a brilliant answer on three and blanks elsewhere. Never go silent — always give the part you do know.
> - **Consulting Services / EaaS (Engineering-as-a-Service)** = you'd be **staffed onto client projects**, sometimes client-facing. So they screen hard for **client-readiness**: clear structured communication, explaining technical work to someone outside your team, and flexibility across the stack. **Say once, explicitly: *"I'm comfortable moving between frontend and backend depending on what the project needs."*** That sentence is written for this role.
> - **AI&E = Deloitte's "AI & Engineering" service line.** Your **RoboGebra AI explanation engine is directly on-theme** — a Spring Boot service that builds prompts, calls an AI model and parses structured output. **Lead your intro with it.** Most candidates for a Java+Angular role have nothing AI-adjacent; you do. This is your single biggest differentiator tomorrow.
> - **Software Engineer II** = they expect you to code well and hold opinions, **not** to architect systems. Fundamentals + one solid project story wins it. Don't over-reach into system design you haven't done.

### 📋 What Deloitte's process usually looks like

| Stage | What it is |
|---|---|
| **Round 1 — Technical (this one)** | 60 min. Java + Spring + Angular fundamentals, some SQL, a project walkthrough. Sometimes light live coding. |
| **Round 2 — Technical / Project deep-dive** | Deeper on your projects, design choices, "why did you do it that way", occasionally a hands-on task. |
| **Round 3 — Managerial / Behavioural** | STAR questions: conflict, deadline pressure, learning something new, working with a client. Deloitte weights this **genuinely** — it isn't a formality. |
| **HR** | Notice period, compensation, location/hybrid, documents. |

> 💡 **So tomorrow's job is to survive the checklist and be easy to talk to.** Deloitte rejects far more people for vague communication than for a missing framework detail. Answer in **structure** — *"There are three ways to do this… I'd use the second one, because…"* — and you'll sound exactly like the person they're hiring.

---

# ⏱️ Your plan — tonight and tomorrow morning

**Do not open new topics tomorrow.** Everything below is revision of what's already in this pack. New material the morning of an interview only creates doubt.

## 🌙 TONIGHT (24 Aug) — 90 minutes max, then sleep

| Time | Do | File |
|---|---|---|
| 30 min | **Say your self-intro out loud 5 times.** Not read — *said*, timed to 60–75 seconds. Record it once on your phone and listen back. | [00](./00-self-introduction.md) |
| 30 min | **RoboGebra walkthrough out loud** — problem → your role → stack → one hard thing you solved → the outcome. 2 minutes flat. | [10](./10-projects-deep-dive.md), [18](./18-robogebra-technical-versions.md) |
| 20 min | Skim the **Most-repeated questions** table. Just read it. | [26](./26-companies-asked-questions.md) |
| 10 min | **Set up the room:** test the meeting link, camera, mic, lighting, charger, backup mobile hotspot. Keep water and a notepad on the desk. | — |
| | **Sleep by 11.** A tired brain fails exactly where you've been failing — the second follow-up question. | |

## ☀️ TOMORROW MORNING (25 Aug) — the concentrated block

| Slot | Topic | What exactly | File |
|---|---|---|---|
| **07:00–08:00** | 🔴 **Java core — the guaranteed block** | HashMap internals (say the 90-second answer aloud 3×), `equals`/`hashCode` contract, ArrayList vs LinkedList, `String` immutability & the pool, `==` vs `equals` | **[31](./31-hashmap-internals.md)** Part 0 + Parts 1–2 · [05](./05-java.md) |
| **08:00–08:30** | ☕ **Break — eat properly** | Not at your desk. | |
| **08:30–09:15** | 🔴 **OOP + interface vs abstract** | The 60-second answer, the table, "when would you use which", the diamond rule | **[33](./33-interface-vs-abstract-class.md)** Parts 0, 1, 4 |
| **09:15–10:00** | 🔴 **Streams + Java 8** | `map`/`filter`/`collect`, `groupingBy`, `Optional`, functional interfaces. **Type 3 problems, don't read them.** | [22](./22-java-streams-coding-problems.md) |
| **10:00–10:45** | 🔴 **Spring Boot** | IoC/DI, `@Component` vs `@Service` vs `@Repository`, annotation-to-REST mapping, `@Transactional`, exception handling with `@ControllerAdvice`, auto-configuration in one sentence | [06](./06-spring-boot.md) |
| **10:45–11:00** | Break | | |
| **11:00–11:45** | 🔴 **Angular — the four that always come** | Lifecycle hooks (asked in **4** rounds), component communication (**4**), Observables vs Promises (**4**), services & DI | [04](./04-angular.md) · **[34](./34-techmahindra-angular-round.md)** §6 |
| **11:45–12:30** | 🟡 **RxJS + interceptors + forms** | `switchMap` vs `mergeMap` vs `concatMap`, `async` pipe & unsubscribing, HTTP interceptor + benefits, reactive vs template-driven | [20](./20-rxjs-operators.md) · [25](./25-angular-binding-forms.md) · [34](./34-techmahindra-angular-round.md) §4 |
| **12:30–13:15** | 🍽️ **Lunch — properly, away from the screen** | | |
| **13:15–13:45** | 🟡 **SQL + microservices** | Joins, `GROUP BY` vs `HAVING`, indexing, one window function; then REST vs microservices, service discovery, one resilience pattern | **[36](./36-sql-interview-questions.md)** Parts 0, 4, 6, 24 · [08](./08-microservices-basics.md) |
| **13:45–14:15** | 🔴 **Multithreading — 30 min, high yield** | Thread vs Runnable, `start()` vs `run()`, `synchronized` vs `volatile`, deadlock + how you'd prevent and detect it | **[32](./32-multithreading.md)** Parts 0, 4, 5, 9 |
| **14:15–14:40** | 🎯 **Dry run** | Say your intro, your project story, and 5 rapid-fire answers out loud. Then **stop revising.** | |
| **14:40–14:55** | 🧘 **Log in early** | Join 5 minutes before. Camera on. Notepad, water, resume printout, JD open in a tab. | |
| **15:00** | 🚀 | | |

> **The rule for the last 20 minutes: no new information.** Confidence beats one extra fact. Every round you've lost was lost on delivery of the *second* answer, not on a missing fact.

---

# 🎯 The 25 questions most likely to come (from YOUR own log)

These are ranked by how often they've actually been asked across your six logged rounds. **If you can answer these 25 fluently, you have covered most of a 60-minute round.**

## Java (expect 6–8)

1. **How does HashMap work internally?** → the 5 beats: `Node[] table` · `hash()` spread + `(n-1) & hash` · bucket = **linked list of Nodes** · **tree at 8 (table ≥ 64)** · **resize at 0.75**. [31](./31-hashmap-internals.md)
2. **`equals()` and `hashCode()` contract — what breaks if you override only one?** → equal objects land in different buckets → `get` returns null, duplicates in a `Set`.
3. **ArrayList vs LinkedList** → array-backed, O(1) random access, O(n) middle insert vs node-based, O(1) insert *if you already hold the node*, O(n) traversal to reach it. In practice ArrayList wins almost always (cache locality).
4. **String immutability / String pool / `==` vs `equals`** → `new String("a") != "a"`; `.intern()`; why immutability makes String a safe map key and thread-safe.
5. **Interface vs abstract class** → contract vs shared code; and Java 8 default methods. [33](./33-interface-vs-abstract-class.md)
6. **Java 8 features / Streams** → lambdas, functional interfaces, Stream API, `Optional`, default methods, `java.time`. Then a live one: group employees by department, find the second-highest salary. [22](./22-java-streams-coding-problems.md)
7. **`final` vs `finally` vs `finalize`** · **checked vs unchecked exceptions** · **overloading vs overriding**.
8. **Multithreading:** `synchronized` vs `volatile`; what is a deadlock and how do you prevent it (**lock ordering**). [32](./32-multithreading.md)

## Spring Boot (expect 4–6)

9. **What is Spring Boot / how is it different from Spring?** → auto-configuration, starters, embedded server, no XML, production-ready actuator.
10. **IoC and Dependency Injection** → the container owns object creation; **constructor injection** is preferred (immutable, testable, catches circular deps at startup).
11. **`@Component` vs `@Service` vs `@Repository` vs `@Controller`** → all stereotypes; `@Repository` adds exception translation; the others are semantic.
12. **How do you build a REST API?** → `@RestController`, `@GetMapping`/`@PostMapping`, `@PathVariable` vs `@RequestParam` vs `@RequestBody`, `ResponseEntity` with proper status codes.
13. **Exception handling** → `@ControllerAdvice` + `@ExceptionHandler` returning a consistent error DTO. Say *"one place, consistent shape, correct status codes."*
14. **`@Transactional`** → the three traps: **self-invocation** doesn't work (proxy), only **unchecked** exceptions roll back by default (`rollbackFor` for checked), and it needs a public method.
15. **Is a Spring singleton the same as a Java singleton?** → No: one per **ApplicationContext**, not per JVM. And it's only thread-safe if it's **stateless**.

## Angular (expect 6–8)

16. **Lifecycle hooks** (your #1 repeat — asked in 4 rounds) → order: `ngOnChanges` → `ngOnInit` → `ngDoCheck` → `ngAfterContentInit/Checked` → `ngAfterViewInit/Checked` → `ngOnDestroy`. Say *what you actually use each for.*
17. **Component communication** (4 rounds) → `@Input`/`@Output`, a shared service with `BehaviorSubject`, `@ViewChild`, and the store for app-wide.
18. **Observables vs Promises** (4 rounds) → single/eager/uncancellable vs stream/lazy/cancellable/operators. Close with the typeahead `switchMap` example. [34](./34-techmahindra-angular-round.md) §6
19. **Services & DI / `providedIn: 'root'`** → tree-shakable singleton; provider scope; the lazy-module `forRoot()` double-instance trap.
20. **Reactive vs template-driven forms** → reactive: defined in the class, testable, dynamic, scales; TD: simple, `ngModel`, small forms. Plus `setValue` vs `patchValue`.
21. **RxJS operators** → `map`, `filter`, `switchMap` (cancel previous — search), `mergeMap` (parallel), `concatMap` (ordered), `debounceTime`, `catchError`, `forkJoin`.
22. **Change detection** → default checks the whole tree; `OnPush` only on input reference change / event / async pipe. Mention immutability as the enabler.
23. **Unsubscribing / memory leaks** → `async` pipe first, then `takeUntilDestroyed()` / `takeUntil`.
24. **State management / NgRx** → the four-level ladder; NgRx = store/actions/reducers/selectors/effects. Your honesty position: **NgRx = EasyVisa.** [21](./21-ngrx.md)

## Cross-cutting (expect 2–4)

25. **SQL:** a join + `GROUP BY … HAVING` question, and *"how would you speed up a slow query?"* [36](./36-sql-interview-questions.md) → index the filtered/joined columns, check the execution plan, avoid `SELECT *`, watch the N+1.
    **Microservices:** why split, how services talk (REST/Feign/messaging), service discovery, one resilience pattern (circuit breaker), and **one honest sentence about the scale you actually worked at.**

---

# 🗣️ Your opening — the first 3 minutes decide the tone

**"Tell me about yourself"** — 60–75 seconds, in this order:

1. **Now:** *"I'm a full-stack developer with 5 years, working primarily with Angular on the frontend and Java with Spring Boot on the backend."*
2. **The AI&E hook (say this — it's the role's own theme):** *"My most recent work is RoboGebra, an AI-powered math explanation engine — a Spring Boot service that builds prompts, calls an AI model, parses the step-by-step explanation and serves it to an Angular and Ionic frontend, with bilingual output."*
3. **Range:** *"Before that I worked on EasyVisa, a document portal with reactive forms and NgRx, and Subsea's schedule manager. So I've moved between frontend-heavy and backend-heavy work depending on what the project needed."*
4. **Close on fit:** *"This role is full-stack Java with Angular on client engagements under Deloitte's AI&E group, which is exactly the mix I've been doing — and the AI side is where I want to keep growing. That's why I'm interested."*

> ⚠️ **Do not** start with your college, your hometown, or a chronological life story. Start with what you do now.

**When they ask about a project, use this shape every time — four beats, two minutes:**
> **Context** (what the product does, who uses it) → **Your role** (what *you* built, first person singular) → **One hard problem** and how you solved it → **Outcome** (a number if you have one; an honest qualitative result if you don't).

---

# 🛡️ The three habits that decide this round

Your own log says you clear the first question and lose the follow-up. Fix that with three mechanical habits:

### 1. Every answer gets a **second sentence**
> **Definition → one concrete thing from your project → one trade-off or failure mode.**

*"An interceptor sits in the HTTP pipeline and lets you handle cross-cutting concerns centrally. In EasyVisa I used one to attach the JWT and redirect to login on a 401. The catch is that ordering matters — request order is registration order, response order is reversed — and it only sees traffic that goes through `HttpClient`."*

**That is what "4+ years" sounds like.** One sentence sounds like documentation.

### 2. Say "I don't know" **cleanly**, then bridge
> *"I haven't used Kafka in production. I know it's a distributed log with topics and partitions and that consumers track offsets, and the closest thing I've built is an async notification flow with `@Async` and a bounded thread pool. I'd pick it up quickly, but I'd rather be accurate than guess."*

That answer **scores**. Bluffing loses the whole round because the interviewer stops trusting everything else you said.

### 3. Own the scale honestly
Don't inflate numbers. *"Our traffic was modest — a few hundred concurrent users — so I haven't tuned for very high load, but here's how I'd approach it…"* is a **strong** answer for Software Engineer II. Consulting firms screen hard for candidates who over-claim, because they'll be in front of a client.

---

# 🤝 The Deloitte behavioural layer — 10 minutes of this round is not technical

Even in a technical round, a Deloitte interviewer scores **communication and fit**. Have short **STAR** answers ready (Situation → Task → Action → Result), 60–90 seconds each. Full versions in **[11 — HR & Behavioural](./11-hr-behavioral.md)**.

| Likely question | Your material |
|---|---|
| *"Tell me about a challenging problem you solved."* | The hardest technical thing in RoboGebra — parsing inconsistent AI output into reliable step-by-step explanations, and how you made it deterministic |
| *"A time you had a disagreement with a teammate."* | Pick a **technical** disagreement resolved with data, not a personality clash. End with what you learned |
| *"A tight deadline — how did you handle it?"* | Scope negotiation: what you cut, what you protected, how you communicated it early |
| *"Something new you had to learn fast."* | Ionic/Capacitor, or MongoDB — say **how** you learned it, not just that you did |
| *"Why Deloitte?"* | Consulting exposure across clients and domains, the AI&E service line matching where you want to grow (your last project was an AI engine), and the scale/structure to learn from senior engineers |
| *"Where do you see yourself in 3 years?"* | Deeper full-stack ownership, moving toward leading a module and mentoring — with AI-integrated engineering as the direction |
| *"Why are you leaving your current role?"* | Forward-looking only: scope, scale, technology. **Never** criticise your current employer |

> ⚠️ **The client-facing tell they listen for:** when you explain RoboGebra, can you describe it **without jargon first** and then add the technical detail? Practise the one-sentence version: *"It's a learning app that explains maths problems step by step in the student's own language, using an AI model behind a Spring Boot service."* Say that, then go technical. That's the consulting communication pattern.

---

# 💬 Questions to ask them (have 3 ready — never say "no questions")

1. *"What does the delivery model look like — would I be staffed on one client project, or moving across engagements?"*
2. *"How is the work split between frontend and backend on the current team — is there a preference for where I'd start?"*
3. *"Given this is under AI&E, is there AI/ML-adjacent work on the roadmap for this team? My last project was an AI explanation engine, so that's an area I'd like to grow in."*
4. *"What does the first 90 days look like for a Software Engineer II here?"*
5. *"What are the next steps and the expected timeline?"* ← **always close with this one.**

---

# 📝 One-page cheat sheet — read at 14:45, nothing else

| Topic | The line |
|---|---|
| **HashMap** | `Node[] table` · `h ^ (h>>>16)` · `(n-1) & hash` · bucket = linked list of Nodes · tree at **8** if table ≥ **64** · resize at **0.75** (16→12 entries) |
| **equals/hashCode** | Equal ⇒ same hash. Override one only → entry unreachable, duplicates in a Set |
| **ArrayList vs LinkedList** | Array + O(1) index vs nodes + O(1) insert-at-node; ArrayList wins in practice |
| **String** | Immutable, pooled, `==` compares references, safe as a map key |
| **Interface vs abstract** | Contract/can-do/many vs shared state+code/is-a/one. Java 8: `default`+`static`; Java 9: `private` |
| **`synchronized` vs `volatile`** | Lock = atomicity+visibility+ordering; volatile = visibility+ordering only, **doesn't fix `i++`** |
| **Deadlock** | 4 conditions; prevent with **consistent lock ordering** or `tryLock` timeout; detect with `jstack` |
| **DI** | Constructor injection — immutable, testable, circular deps caught at startup |
| **`@Transactional`** | Self-invocation fails · only unchecked rolls back by default · must be public |
| **Spring singleton** | Per ApplicationContext, not per JVM; safe only if **stateless** |
| **REST codes** | 200 · 201 created · 204 no content · 400 bad request · 401 unauth · 403 forbidden · 404 · 409 conflict · 500 |
| **Lifecycle hooks** | `ngOnChanges` → `ngOnInit` → `ngDoCheck` → AfterContent → AfterView → `ngOnDestroy` |
| **Observable vs Promise** | Stream/lazy/cancellable/operators vs single/eager/not cancellable |
| **switchMap vs mergeMap** | switchMap **cancels** the previous (search); mergeMap runs all in parallel (independent writes) |
| **Unsubscribe** | `async` pipe > `takeUntilDestroyed()` > `takeUntil(destroy$)` |
| **OnPush** | Checks only on input **reference** change, events, async pipe → needs immutable updates |
| **Interceptor** | Auth · errors · loading · logging · retry. Clone the request (immutable), order matters, `multi: true` |
| **N+1** | Detect in the SQL log → fix with `JOIN FETCH` / `@EntityGraph` / batch size |
| **Slow query** | Execution plan → index the filter/join columns → avoid `SELECT *` → check for N+1 |
| **Your honesty lines** | NgRx = **EasyVisa** · MongoDB = **ODM not ORM** · .NET = **Subsea Web API** · scale = modest, and that's fine |

---

# ✅ Final checklist — tonight

- [ ] Meeting link tested, camera + mic checked **in that app**
- [ ] Laptop charger plugged in · **mobile hotspot ready as backup**
- [ ] Resume printed or open in a tab — they *will* ask about something on it
- [ ] The JD open in a tab (the requisition text above)
- [ ] Quiet room booked, door closed, phone on silent
- [ ] Water, notepad, pen
- [ ] Intro rehearsed **out loud** 5 times
- [ ] RoboGebra story rehearsed out loud, 2 minutes
- [ ] 3 questions to ask them, written on the notepad
- [ ] Asleep by 11 PM

> **After the round tomorrow — while it's still fresh, the same evening:** write every question they asked into **[26 — Companies log](./26-companies-asked-questions.md)**. That file is the reason you now know Observables vs Promises has come up four times. Six rounds of data is already an asset; seven is better.

**You've got this. Breadth is not your problem — you reach second rounds. Tomorrow, just add the second sentence to every answer.**

---

**Related:** [00 Intro](./00-self-introduction.md) · [11 HR & Behavioural](./11-hr-behavioral.md) · [05 Java](./05-java.md) · [06 Spring Boot](./06-spring-boot.md) · [04 Angular](./04-angular.md) · [31 HashMap](./31-hashmap-internals.md) · [32 Multithreading](./32-multithreading.md) · [33 Interface vs Abstract](./33-interface-vs-abstract-class.md) · [26 Companies log](./26-companies-asked-questions.md)
