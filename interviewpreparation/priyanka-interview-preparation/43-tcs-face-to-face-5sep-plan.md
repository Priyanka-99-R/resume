# 🔴 TCS — Face-to-Face, Saturday 5 Sept 2026 · The 24-Hour Plan

> **THE ACTIVE FILE.** You have **today (Friday 4 Sept) plus Saturday morning**. That's roughly **12 usable hours**, not more.
>
> **Your own words:** weak on **Java, Spring, database, microservices, coding, and JavaScript definitions / output questions**. Strong on **Angular**.
>
> So this plan is **deliberately unbalanced**: about **75% backend + coding**, **10% JavaScript**, **10% project + HR**, and only **5% Angular** — because Angular is the one thing you don't need to buy back.

---

## ⚠️ Read this first — the two rules that decide tomorrow

```
1. You cannot learn backend in one day. You CAN learn to ANSWER it.
   Aim for a clean, correct 4-sentence answer on 25 topics —
   NOT a deep answer on 5 topics and silence on the rest. ⭐

2. TCS face-to-face = PAPER coding. No IDE, no autocomplete, no red underline.
   Every program you practise today, WRITE BY HAND. 💥
```

---

## What a TCS face-to-face actually looks like (4+ years, lateral)

| Round | Who | What they do | Your risk |
|---|---|---|---|
| **TR — Technical** | A delivery-side senior dev | Opens with **"explain your project"**, then drills wherever *you* opened the door. Java core → Spring → DB → 1 coding problem on paper | 🔴 **Highest** — this is the round you must survive |
| **MR — Managerial** | Project/delivery manager | Ownership, conflict, deadlines, production issues, why you're leaving | 🟡 Answerable with STAR |
| **HR** | HR | Notice period, relocation, CTC, why TCS | 🟢 Low — but don't get careless |

Usually **all three on the same day**, back to back. Pace your energy.

> ⭐ **The single most important fact:** in TCS the panel drills **what you say**, not a fixed list. Every sentence you speak is a door. Say "microservices" and you'll get 15 minutes of microservices. **Open the doors you can defend.**

---

# 📅 FRIDAY — the full day

> Times assume a ~9am start. **Starting later? Do the ⭐ blocks and skip the rest.** They're marked.

## ⭐ Block 1 · 09:00–10:30 · Java core definitions

**File: [05 — Core Java](./05-java.md), [33 — Interface vs Abstract](./33-interface-vs-abstract-class.md), [31 — HashMap Internals](./31-hashmap-internals.md)**

Don't read the whole files. Get a **4-sentence spoken answer** for each of these:

```
OOP 4 pillars + a real example of each from YOUR project
Overloading vs Overriding
Interface vs Abstract class (+ what Java 8 changed)      ← asked at Virtusa R2 ⭐
== vs .equals()  |  equals() + hashCode() contract
String vs StringBuilder vs StringBuffer  |  why String is immutable
Collections: List vs Set vs Map, ArrayList vs LinkedList
HashMap internal working — bucket, hash, treeify at 8    ← asked at Virtusa R2 ⭐
Checked vs Unchecked exception, try-with-resources, finally
final vs finally vs finalize
```

**Say every one out loud.** If you can't say it in 4 sentences, you don't have it yet.

## ⭐ Block 2 · 10:30–12:00 · Coding — ON PAPER

**File: [42 — Final Easy-Memory Sheet](./42-java-streams-final-memory-sheet.md) ⭐ then [22 — Streams: 20 Problems](./22-java-streams-coding-problems.md)**

Read the 32-row table in **42** once. Then **close the laptop** and hand-write these six:

```
1. Reverse a string           → BACKWARD → ADD
2. Palindrome                 → REVERSE → COMPARE
3. First non-repeating char   → UNIQUE → FIRST
4. Second largest number      → DISTINCT → DESC → SKIP 1 → FIRST
5. Character frequency        → GROUP → COUNT
6. Highest paid per dept      → DEPT → COMPARE → KEEP HIGHER
```

> 💡 **Why these six:** they cover the string family, the unique family, the sort family and the group family. Nearly every coding question they can ask is a variation of one of them.

**Then say the FINAL MEMORY block in 42 out loud twice.** That block is the whole point of the file.

## ☕ 12:00–12:30 · Break. Actually stop.

## ⭐ Block 3 · 12:30–14:00 · Spring Boot

**File: [06 — Spring & Spring Boot](./06-spring-boot.md)** · web layer: **[44 — Spring MVC](./44-spring-mvc.md)**

> 🌐 If they go to the **web layer** — *"explain the Spring MVC request flow"* is the single most likely Spring question after *"what is Spring Boot"* — read **44** and be able to draw:
> `Request → DispatcherServlet → HandlerMapping → HandlerAdapter → Controller → HttpMessageConverter → Response` (🧠 **D-M-A-C-C-R**), plus `@PathVariable` vs `@RequestParam` vs `@RequestBody` and `@RestControllerAdvice`.

The eight things TCS asks, in this order:

```
1. What IS Spring Boot? (DI + auto-config + starters + embedded server)  ⭐
2. IoC and Dependency Injection — in your own words
3. Constructor vs setter vs field injection — and why constructor wins   ⭐
4. @Component vs @Service vs @Repository vs @Controller
5. @Component vs @Bean  (class vs method — who constructs it)
6. @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
7. How a REST API works: @RestController, @GetMapping, @PathVariable vs @RequestParam,
   @RequestBody, ResponseEntity, status codes
8. Exception handling: @ControllerAdvice + @ExceptionHandler                ⭐
```

> These exact questions came up at **Capgemini (3 Sept)** and **Altimetrik (31 Aug)** — see **[41](./41-capgemini-java-angular-3sep.md)** and **[37](./37-altimetrik-fullstack-java-angular-31aug.md)**. You have the answers already written.

## 🍽️ 14:00–14:45 · Lunch

## ⭐ Block 4 · 14:45–16:00 · SQL / Database

**File: [36 — SQL: The Complete Interview File](./36-sql-interview-questions.md)**

SQL shows up in **every single round** you've attended. Learn to *write* these on paper:

```
1. SELECT + WHERE + ORDER BY + LIMIT
2. GROUP BY + COUNT / AVG / SUM
3. WHERE vs HAVING          ← the classic "do you actually know SQL" test ⭐
4. INNER vs LEFT JOIN       ← draw the two tables while you explain
5. Employees per department + average salary per department  ← asked at Mphasis ⭐
6. SECOND-HIGHEST SALARY — two ways (subquery, and DENSE_RANK)  ⭐⭐
7. Find duplicate rows
```

Also be ready to say: **primary vs foreign key · normalization in one sentence · index (what it speeds up and what it costs) · DELETE vs TRUNCATE vs DROP · ACID**.

## Block 5 · 16:00–17:15 · Microservices — and the honesty guard 🔴

**File: [08 — Microservices Basics](./08-microservices-basics.md)**

```
What is a microservice? Monolith vs microservices — cost table, not slogans
How do services talk? REST (sync) vs messaging/Kafka (async)
Service discovery · API gateway
Circuit breaker — WHY it exists, Resilience4j    ← asked at Mphasis ⭐
Config: @Value vs @ConfigurationProperties, config server
```

> 🔴 **THE HONESTY GUARD — read this twice.**
>
> **RoboGebra is a modular monolith, not microservices.** If you claim microservices experience, the very next question is *"how did you handle distributed transactions?"* and the round is over.
>
> **Say this instead:** *"RoboGebra is a modular monolith — around 110 controllers in one deployable, and honestly that was the right call for our team size. I've studied microservices patterns and I can talk through circuit breakers, discovery and the saga pattern, but I haven't run them in production. What I have done is the boundary work — separating the notification thread pool, keeping modules independent — which is the part that would make a split possible."*
>
> **That answer wins.** It's honest, it shows judgement, and it *still* proves you know the concepts. See **[39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)** and **[41](./41-capgemini-java-angular-3sep.md)** for the same guard.

## ☕ 17:15–17:45 · Break

## Block 6 · 17:45–18:45 · JavaScript definitions + output questions

**Files: [19 — JS Variables Made Easy](./19-javascript-variables-easy.md) ⭐, [01 — JavaScript](./01-javascript.md), [23 — Java Output Questions](./23-java-output-tricky-questions.md)**

You said definitions and output questions are weak. These are **memorisable** — highest return per minute today:

```
var vs let vs const        (function vs block scope, TDZ, const locks the BOX)  ⭐
Hoisting — what actually gets hoisted
Closure — in one sentence + one example
== vs ===
Arrow function vs normal function (lexical this)
Promise vs Observable  |  async/await
map vs filter vs reduce  |  forEach vs map
Event bubbling vs capturing
```

Then do **10 output questions** from 23 and 01. Predict out loud *before* reading the answer — that's the whole exercise.

## Block 7 · 18:45–19:45 · Your project — the round-opener ⭐⭐

**Files: [10 — Projects Deep Dive](./10-projects-deep-dive.md), [39 — RoboGebra Code Examples](./39-robogebra-code-examples.md), [00 — Self Introduction](./00-self-introduction.md)**

The TR round **opens here**. Get the **60-second architecture answer** in **39** word-perfect, then be ready for:

```
Why did you choose that stack?
What was YOUR piece of it? (say "I built", never "I helped with")  ⭐
Hardest bug you fixed and how you found it
How did you improve performance? (the thread pool · the aggregation N+1 fix)
Team size, your role, how work reached you
```

> ⚠️ **Two traps from your own notes:** RoboGebra uses **MongoDB, not JPA** — say **ODM**, not ORM. And **signals are not used** — don't claim them.

## 🍽️ 19:45–20:30 · Dinner. Away from the laptop.

## Block 8 · 20:30–21:30 · Managerial + HR

**File: [11 — HR & Behavioural](./11-hr-behavioral.md)**

Write **four STAR answers in your own words** (five lines each, not an essay):

```
A production issue you handled
A disagreement with a teammate or lead
A deadline you were going to miss — what you did
Something you taught yourself quickly
```

Plus, ready to say: **why you're leaving · why TCS · notice period · expected CTC · relocation**.

> **Why TCS —** keep it concrete: scale of projects, structured delivery, long-term client work, and clear growth for a full-stack developer. Don't say "good company".

## Block 9 · 21:30–22:00 · Write your one-page sheet — BY HAND ✍️

One side of one page. Nothing else:

```
Self-intro           — 4 lines
Project architecture — 6 lines
The 6 code patterns from 42 — memory phrases only
SQL: GROUP BY/HAVING · JOIN · 2nd-highest salary
Spring: the 8 answers, one line each
The microservices honesty line
3 questions to ask them
```

Handwriting it **is** the revision. And it's what you re-read in the cab tomorrow.

## 😴 22:00 — SLEEP. This is part of the plan. 🔴

A TCS face-to-face is three rounds and several hours. **A tired brain fails the TR round far more often than a missing topic does.** Do not study past 22:00. Do not restart at 1am.

---

# 🌅 SATURDAY MORNING — 90 minutes, revision only

> **Learn nothing new.** New material this morning only shakes your confidence.

| Time | What | File |
|---|---|---|
| 06:30–07:00 | The **FINAL MEMORY block** — read it, then say it with the page covered | [42](./42-java-streams-final-memory-sheet.md) ⭐ |
| 07:00–07:20 | **Self-intro out loud 3×** + the 60-second project answer | [00](./00-self-introduction.md) · [39](./39-robogebra-code-examples.md) |
| 07:20–07:45 | The **most-repeated questions** across all 9 companies | [26](./26-companies-asked-questions.md) ⭐ |
| 07:45–08:00 | Your handwritten page. Nothing else. | ✍️ |

Then **stop**. Eat properly. Leave early.

---

# 🎯 The 20 questions most likely to be asked

Ranked from **your own 144-question log** — these repeat across companies, so TCS will very likely hit them too.

| # | Question | Where the answer is |
|---|---|---|
| 1 | Tell me about yourself | [00](./00-self-introduction.md) |
| 2 | Explain your project architecture | [39](./39-robogebra-code-examples.md) ⭐ |
| 3 | OOP concepts with examples from your project | [05](./05-java.md) |
| 4 | Interface vs abstract class | [33](./33-interface-vs-abstract-class.md) ⭐ |
| 5 | How does HashMap work internally? | [31](./31-hashmap-internals.md) ⭐ |
| 6 | Java 8 features | [12](./12-java17-features.md) · [41](./41-capgemini-java-angular-3sep.md) |
| 7 | Streams — write a program | [42](./42-java-streams-final-memory-sheet.md) ⭐⭐ |
| 8 | What is Spring Boot / why use it | [06](./06-spring-boot.md) ⭐ |
| 9 | Dependency injection + which type and why | [06](./06-spring-boot.md) ⭐ |
| 10 | Spring Boot annotations | [06](./06-spring-boot.md) · [37](./37-altimetrik-fullstack-java-angular-31aug.md) |
| 11 | How do you handle exceptions in REST? | [06](./06-spring-boot.md) · [44](./44-spring-mvc.md) |
| 12 | REST API design + status codes (401 vs 403) | [06](./06-spring-boot.md) · [44](./44-spring-mvc.md) · [38](./38-iqvia-technical-lead-prep.md) |
| 13 | SQL: average salary per department | [36](./36-sql-interview-questions.md) ⭐ |
| 14 | SQL: second-highest salary | [36](./36-sql-interview-questions.md) ⭐⭐ |
| 15 | Joins + WHERE vs HAVING | [36](./36-sql-interview-questions.md) |
| 16 | What are microservices / vs monolith | [08](./08-microservices-basics.md) 🔴 *honesty guard* |
| 17 | Circuit breaker | [08](./08-microservices-basics.md) · [37](./37-altimetrik-fullstack-java-angular-31aug.md) |
| 18 | var vs let vs const, closure, hoisting | [19](./19-javascript-variables-easy.md) ⭐ |
| 19 | Angular: components, lifecycle, data binding | [04](./04-angular.md) *(your strength — 20 min is enough)* |
| 20 | Why are you leaving / why TCS | [11](./11-hr-behavioral.md) |

---

# 🗣️ The three habits that decide the round

```
1. THE SECOND SENTENCE.
   Everyone gets the definition out. The second sentence — where it's used
   in YOUR project — is what separates 4 years from 1 year repeated 4 times. ⭐

2. A CLEAN "I DON'T KNOW".
   "I haven't used that directly. What I do know is X, and I'd approach it by Y."
   Then STOP. Never invent. A clean no costs you one question;
   a bluff that unravels costs you the round. 💥

3. NEVER GO SILENT.
   Think out loud on the coding problem. TCS panels score the approach,
   not just the final code. Silence reads as "no idea".
```

---

## ⏱️ Emergency version — if you only have 4 hours

Do these five, in this order, and nothing else:

```
1. 42 — the table + the FINAL MEMORY block, then 3 programs on paper   (60 min) ⭐
2. 06 — the 8 Spring answers                                          (50 min) ⭐
3. 36 — GROUP BY/HAVING, JOIN, second-highest salary                  (50 min) ⭐
4. 39 — the 60-second project answer, out loud until it's fluent      (40 min) ⭐
5. 00 + 11 — self-intro, why leaving, why TCS                         (40 min)
```

---

## 🎒 The night before — pack it now

```
□ 3 printed copies of your resume        □ Photo ID
□ Pen + a blank notepad (you WILL write code on paper)
□ Your handwritten one-page sheet
□ Offer letter / payslips if they asked for documents
□ Route checked + leave 45 min earlier than you think
□ Water. Eat before you go — it's a long day.
```

## 💬 Three questions to ask them

```
1. What does the team's tech stack look like day to day, and where would I start?
2. How is the work split between backend and frontend on this project?
3. What does growth look like for a full-stack developer here over the next two years?
```

---

**Related files:** [42 — Final Memory Sheet](./42-java-streams-final-memory-sheet.md) · [26 — Questions Actually Asked](./26-companies-asked-questions.md) · [05 Java](./05-java.md) · [06 Spring Boot](./06-spring-boot.md) · [36 SQL](./36-sql-interview-questions.md) · [08 Microservices](./08-microservices-basics.md) · [19 JS Variables](./19-javascript-variables-easy.md) · [39 RoboGebra Code](./39-robogebra-code-examples.md) · [00 Self-Introduction](./00-self-introduction.md) · [11 HR](./11-hr-behavioral.md)
