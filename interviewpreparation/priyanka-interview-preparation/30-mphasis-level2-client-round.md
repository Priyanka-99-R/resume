# 🔵 Mphasis — Level 2 + Client Technical Round

> ✅ **You cleared Level 1.** The 6 Aug 2026 round (10 backend questions) went well enough that they're scheduling you forward. That is the first time in six companies you've been moved to a *next* round by a tier-1 service company. Read that as evidence, not luck.
>
> 🎯 **What HR told you:**
> > *"Java only mostly. With Angular, what you have used in recent projects they'll ask. Basics of Angular not mandatory."*
>
> This file decodes that sentence, then prepares both remaining rounds.

📎 **Read alongside:** [26 — Companies: Questions Actually Asked § Mphasis](./26-companies-asked-questions.md#-mphasis--technical-round-6-aug-2026) — the 10 questions they already asked you. **L2 interviewers read the L1 feedback form.** They will re-probe those exact threads, one level deeper. That section is not revision — it's the syllabus.

---

## 📖 Table of contents

| Part | What it covers |
|---|---|
| [0 — Decoding the HR message](#0--decoding-the-hr-message) | What "Java only mostly" and "basics not mandatory" actually mean |
| [A — Level 2: the re-probes](#a--level-2-the-re-probes-of-your-l1-answers) | The follow-up to each of the 10 L1 questions |
| [B — Level 2: Java core depth](#b--level-2-java-core-depth) | Collections, HashMap internals, equals/hashCode, immutability, Streams |
| [C — Level 2: multithreading & concurrency](#c--level-2-multithreading--concurrency) | 🔴 **The biggest section — they asked 2 of 10 L1 questions here.** The two already asked, answered deeper; locks, `volatile`/atomics, thread pools, `CompletableFuture`, concurrent collections, `ThreadLocal`, **threading inside Spring Boot**, virtual threads, output programs |
| [D — Level 2: Spring & Spring Boot depth](#d--level-2-spring--spring-boot-depth) | Bean lifecycle, `@Transactional`, AOP, auto-configuration, REST design |
| [E — Level 2: JPA / Hibernate](#e--level-2-jpa--hibernate) | N+1, lazy loading, caching, locking |
| [F — Level 2: microservices, one level down](#f--level-2-microservices-one-level-down) | Feign vs WebClient, gateway, config server, tracing, JWT |
| [G — SQL: your live gap](#g--sql-your-live-gap) | The queries that get asked, plus indexes and ACID |
| [H — Live coding](#h--live-coding-what-to-expect) | What they'll make you type |
| [I — System design, lite](#i--system-design-lite) | The 5-minute answer structure |
| [J — Angular, the way THEY will ask it](#j--angular-the-way-they-will-ask-it) | Project-anchored, not theory |
| [K — The client technical round](#k--the-client-technical-round) | 🔴 Different round, different game. 17 questions in 3 buckets — project deep dive · scenario/troubleshooting · **client interaction & delivery** (the bucket candidates skip) |
| [L — Logistics & fit questions](#l--logistics--fit-questions) | Notice period, location, why leaving |
| [M — Your 48-hour plan](#m--your-48-hour-plan) | What to do with the time you have |
| [N — One-page cheat sheet](#n--one-page-cheat-sheet) | Print this |

---

# 0 — Decoding the HR message

### "Java only mostly"

Level 2 is a **backend depth round**. L1 was breadth — ten topics, one question each, mostly definitions. L2 at Mphasis is where they pick **three or four** of those and push until you stop knowing. Expect fewer questions, longer each, with the interviewer following your answer wherever it goes.

**The single biggest change from L1 to L2:** L1 rewards *knowing*. L2 rewards *having done*. Every answer needs a project sentence in it.

### "With Angular, what you have used in recent projects they'll ask"

🔴 **This is the most useful thing HR said.** It tells you the *form* of the Angular questions:

| ❌ What they will NOT ask | ✅ What they WILL ask |
|---|---|
| "What are Angular lifecycle hooks?" | "Walk me through a component you built recently." |
| "What is `BehaviorSubject`?" | "How did the dashboard get its data?" |
| "Explain lazy loading." | "How did you structure the RoboGebra app — modules? routing?" |
| "What is a reactive form?" | "You mention Formly on EasyVisa — what was that for?" |

So the preparation is **not** flashcards. It's being able to narrate three features you built, with the Angular decisions inside the narration. → [Part J](#j--angular-the-way-they-will-ask-it)

### "Basics of Angular not mandatory"

Two readings, and you must prepare for both:

- **The generous reading (likely correct):** they won't fail you on Angular theory. The role is Java-weighted.
- **The trap reading:** "not mandatory" ≠ "not asked." If you answer *"I built the dashboard in Angular"* and they say *"how did you handle the subscription cleanup?"* and you blank, that's a bad moment — because **you claimed it on your resume**. Anything you say you built, you own the details of.

> 💡 **The rule:** don't study Angular broadly. Study **your own Angular** narrowly and completely.

---

# A — Level 2: the re-probes of your L1 answers

They have your L1 sheet. For each of the 10 questions you were asked, here is the **next** question. This is the highest-yield section in the file.

| L1 question (already asked ✅) | 🔴 The L2 re-probe |
|---|---|
| 1. What is microservices? | *"How would you split RoboGebra into services? Where do you draw the boundary?"* |
| 2. Design patterns in microservices | *"Explain Saga with a concrete failure. What compensates what?"* |
| 3. `@CircuitBreaker` | *"What values did you set for the threshold and why? What happens to in-flight requests when it opens?"* |
| 4. Circuit-breaker dependency | *"Why does AOP matter here? What else in Spring breaks the same way?"* |
| 5. Thread-safe Singleton | *"Now make it a Spring bean. Is a Spring singleton the same as a Java singleton?"* |
| 6. Actuator | *"How do you secure it in production? What do you actually monitor?"* |
| 7. Association vs aggregation | *"Show me composition in your own codebase. Why not inheritance?"* |
| 8. SQL group-by query | *"Add an index. How would you know the query is slow?"* |
| 9. Deadlock | *"You have a hung production app. Walk me through diagnosing it."* |
| 10. API performance | *"Give me a number. What was slow, what did you change, what did it become?"* |

Now the answers to the five that are hardest.

---

### A1. "How would you split RoboGebra into services?"

Don't invent an architecture. Describe yours, then reason about it.

> *"RoboGebra runs as separate Spring Boot services — the split is by business capability. **User/Auth** owns accounts and JWT issuing. **Content** owns the study lists and question bank. **Quiz** owns attempts and scoring. **Reminders** owns the scheduling and notification dispatch. **AI Explanation** wraps the math explanation engine, because it's the only service with a completely different scaling profile — it's slow, it's expensive per call, and it fails differently from everything else."*
>
> *"The boundary test I'd use is: **does this thing change for its own reasons, and does it own data nobody else writes?** Reminders qualifies — its schema is scheduling data, and a change to reminder logic never touches quiz scoring. What I'd resist splitting is Quiz and Content, because a quiz attempt constantly needs question data; that split would turn one query into a chatty network call for every question."*

🔴 **The senior close:**
> *"And I'd be honest that the AI service is the one that justified the split on its own. If I were starting over with a small team I'd build a modular monolith with those boundaries as packages, and extract the AI service first — because you can always split a well-structured monolith, but you can't easily un-split a distributed system."*

---

### A2. "Explain Saga with a concrete failure"

Use an order flow — it's the canonical example and it's clean.

```
Orchestrated saga — Order across 3 services

  1. Order Service      → CREATE order (status = PENDING)
  2. Payment Service    → charge card              ✅
  3. Inventory Service  → reserve stock            ❌ OUT OF STOCK
  ────────────────────────────────────────────────────────
  Compensate, in reverse:
  4. Payment Service    → REFUND the charge        ← compensating txn
  5. Order Service      → status = CANCELLED
```

> *"The key point is that a compensating transaction is **not a rollback** — the payment genuinely happened and is visible in the ledger. You're issuing a *new* business transaction that semantically undoes it. That's why compensations have to be designed per step, and why some steps are hard to compensate — you can't un-send an email, so you send a correction."*

**The three details that separate you from a memorised answer:**

| Detail | Say this |
|---|---|
| **Idempotency** | *"Every step must be idempotent, because the orchestrator will retry. I'd key each step by the saga id so a repeated 'reserve stock' doesn't reserve twice."* |
| **Isolation** | *"Sagas have no isolation — mid-saga, another transaction can read the half-done state. You handle it with a status field like PENDING that the rest of the system knows not to trust."* |
| **Choreography vs orchestration** | *"Choreography = services react to each other's events; less coupling, but nobody can answer 'where is this order?'. Orchestration = a coordinator drives it; one more component, but the flow is explicit and traceable. For anything with money in it I choose orchestration."* |

---

### A5. 🔴 "Is a Spring singleton the same as a Java singleton?"

**No — and this is a favourite L2 question because most people say yes.**

| | **Java singleton (GoF)** | **Spring singleton scope** |
|---|---|---|
| Scope of uniqueness | One per **classloader / JVM** | One per **Spring container** |
| Enforced by | Private constructor | The container's bean registry |
| Can you make another? | No (that's the point) | ✅ Yes — `new MyBean()` works fine |
| Two contexts in one JVM | Still one instance | **Two instances**, one per context |
| Lifecycle | Yours | Container's — `@PostConstruct` / `@PreDestroy` |
| Testability | Poor — global state, hard to mock | Good — inject a mock |

> *"A Spring singleton is a **container-scoped** singleton, not a JVM-wide one. The class has a public constructor; nothing stops you calling `new`. If an app has two `ApplicationContext`s — which happens in tests, or with a parent/child web context — you get two instances of the 'singleton'."*

🔥 **The follow-up that always comes next: "What's the danger of singleton beans?"**

> *"Singleton beans are shared across all request threads, so **any mutable instance field is a race condition**. A `private List<String> cache` on a `@Service` is a bug in production and works perfectly on your laptop. Keep beans stateless — state lives in method parameters or local variables."*

🔥 **And the one after that: "How do you inject a prototype bean into a singleton?"**

> *"Naively it doesn't work — the prototype is injected once, at singleton creation, so you get the same instance forever. The fixes are `@Lookup` method injection, injecting an `ObjectProvider<T>` and calling `getObject()` per use, or a scoped proxy with `@Scope(value = "prototype", proxyMode = TARGET_CLASS)`."*

```java
@Service
public class ReportService {

    @Autowired
    private ObjectProvider<ReportBuilder> builderProvider;   // ✅ fresh one per call

    public Report build() {
        return builderProvider.getObject().build();
    }
}
```

---

### A9. 🔴 "Production app is hung. Walk me through diagnosing it."

This is a **scenario question**, and scenario questions are the whole game in L2 and the client round. Answer as a procedure, not a fact.

> *"First I'd confirm what 'hung' means — is it all requests, or one endpoint? Is CPU at 0% or 100%? **0% CPU with requests piling up says lock contention or an exhausted thread pool. 100% CPU says an infinite loop or GC thrashing.** That one observation splits the problem in half."*
>
> *"Then I take a **thread dump** — `jstack <pid>`, or `/actuator/threaddump` if it's exposed. Take three, ten seconds apart: if the same threads are in the same stack frames across all three, they're genuinely stuck, not just busy. The JVM detects Java-monitor deadlocks itself, so `jstack` prints a `Found one Java-level deadlock` section naming the two threads and the locks."*
>
> *"What I usually find in a Spring Boot app isn't a classic deadlock — it's the **connection pool**. All HikariCP connections are held by threads waiting on a slow downstream call, so every new request blocks on `getConnection()`. The stack trace shows threads parked in `HikariPool.getConnection`. The real fix isn't a bigger pool — it's a timeout on the downstream call, which is exactly what the circuit breaker from L1 Q3 is for."*

| Symptom | Likely cause | Tool |
|---|---|---|
| 0% CPU, requests queue | Deadlock / pool exhaustion | `jstack`, `/actuator/threaddump` |
| 100% CPU, no progress | GC thrashing or infinite loop | `jstat -gc`, GC logs, heap dump |
| Memory climbing, then OOM | Leak — unbounded cache/collection | heap dump + Eclipse MAT |
| Slow but working | N+1 queries, missing index | slow query log, `EXPLAIN`, tracing |

> 🔴 **Close by connecting it back:** *"And the reason I'd want Actuator and distributed tracing already in place is precisely this moment — diagnosing it after the fact with no instrumentation is guesswork."*

---

### A10. 🔴 "Give me a number" — API performance

You answered this at L1 in general terms. At L2 they want **one concrete before/after**. Pick one and rehearse it with numbers.

**The RoboGebra dashboard story:**

> *"The learning dashboard was making one call per widget — progress, streak, recommended list, recent quizzes — four sequential round trips from a mobile client, and it felt slow on first load. Two changes: on the backend I added an aggregate endpoint so the dashboard is one call, and on the frontend I moved the remaining parallel calls into `forkJoin` instead of nested subscribes. The backend query itself had an N+1 — the study-list fetch was lazily loading each item — so I added a `JOIN FETCH`. First paint went from roughly two seconds to under half a second on the same device."*

**The structure to reuse for any perf answer:**

```
① What was slow, and how did you KNOW      → measurement, not vibes
② What was the actual cause                 → N+1 / round trips / no index / no cache
③ What you changed                          → specific
④ The number                                → before → after
⑤ What you'd do next if it grew             → caching, pagination, CDN
```

**The backend/frontend split they asked at L1 — have both halves ready:**

| Backend | Frontend |
|---|---|
| Fix N+1 (`JOIN FETCH`, `@EntityGraph`) | Lazy-load routes; smaller initial bundle |
| Add the right index; check `EXPLAIN` | `OnPush` change detection |
| Pagination instead of returning everything | `trackBy` on `*ngFor` |
| Cache (`@Cacheable` / Redis) for hot reads | `debounceTime` + `switchMap` on search |
| Return a DTO, not the whole entity graph | Cache HTTP responses; `shareReplay` |
| Async the non-critical work (`@Async`, queue) | Image sizing, compression, CDN |
| Connection-pool sizing, gzip, HTTP keep-alive | `forkJoin` instead of sequential calls |

---

# B — Level 2: Java core depth

L1 didn't test core Java at all — no collections, no OOP internals, no streams. **That is exactly why L2 will.**

### B1. 🔴 HashMap internals — the most-asked L2 Java question anywhere

> *"A `HashMap` is an array of buckets. On `put`, it takes the key's `hashCode()`, applies a spreading function — `h ^ (h >>> 16)` — to mix the high bits down, then indexes with `hash & (n-1)`, which works because the capacity is always a power of two. If the bucket is empty it stores the node; if not, it walks the chain comparing `hash` first and then `equals`. **Since Java 8, once a bucket has 8 entries and the table is at least 64 slots, that chain converts to a red-black tree**, so worst-case lookup goes from O(n) to O(log n) — that was the fix for hash-collision DoS attacks."*

| Property | Value |
|---|---|
| Default capacity | 16 |
| Load factor | 0.75 → resizes at 12 entries |
| Resize | Doubles capacity, rehashes |
| Treeify threshold | 8 (and table ≥ 64) |
| Untreeify | 6 |
| Null keys | ✅ one, always in bucket 0 |
| Thread-safe? | ❌ — use `ConcurrentHashMap` |

🔥 **"What if you use a mutable object as a key?"**
> *"You lose the entry. The hash is computed at insert time and stored in the node. Mutate a field that `hashCode()` depends on and the key now hashes to a different bucket — `get()` looks in the new bucket and finds nothing, but `size()` still counts it. It's a silent memory leak. Keys should be immutable; that's why `String` and the wrappers are the usual choice."*

🔥 **"What breaks if you override `equals` but not `hashCode`?"**
> *"Two objects that are `equals` can land in different buckets, so a `HashMap` treats them as different keys and a `HashSet` accepts both as distinct. The contract is: equal objects **must** have equal hash codes. The reverse isn't required — unequal objects may collide, that's fine."*

```java
// The contract, in one block
a.equals(b)  ⟹  a.hashCode() == b.hashCode()      // REQUIRED
a.hashCode() == b.hashCode()  ⟹̸  a.equals(b)      // NOT required (collision)
```

### B2. Collections — the comparison table they ask from

| | `ArrayList` | `LinkedList` | `HashMap` | `LinkedHashMap` | `TreeMap` | `HashSet` | `ConcurrentHashMap` |
|---|---|---|---|---|---|---|---|
| Backing | Array | Doubly-linked list | Bucket array | Buckets + linked list | Red-black tree | HashMap | Bucket array + CAS |
| Get by index / key | O(1) | O(n) | O(1) avg | O(1) | O(log n) | O(1) | O(1) |
| Insert at end | O(1) amortised | O(1) | — | — | — | — | — |
| Insert in middle | O(n) shift | O(1) *if you have the node* | — | — | — | — | — |
| Order | Insertion | Insertion | ❌ none | ✅ insertion or access | ✅ sorted | ❌ none | ❌ none |
| Nulls | ✅ | ✅ | 1 key, many values | ✅ | ❌ null key | ✅ one | ❌ neither |
| Thread-safe | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

> 🔥 **"`ArrayList` vs `LinkedList` — which do you use?"** → *"`ArrayList`, almost always. `LinkedList`'s O(1) insert assumes you already hold the node — via an iterator. If you're calling `add(index, e)`, it still walks there in O(n), and it loses on cache locality and memory (an object header plus two pointers per element). The honest answer is that `LinkedList` is rarely the right choice outside of a `Deque`."*

> 🔥 **`Hashtable` vs `ConcurrentHashMap`** → *"`Hashtable` synchronizes every method on the whole map, so it's one thread at a time. `ConcurrentHashMap` locks per bucket (CAS plus a `synchronized` on the bin head since Java 8) so reads are lock-free and writes only contend on the same bucket. `Hashtable` is legacy — never use it."*

> 🔥 **`fail-fast` vs `fail-safe`** → *"`ArrayList`'s iterator is fail-fast: it holds a `modCount` and throws `ConcurrentModificationException` if the list is structurally modified during iteration — including by your own code inside the loop. `ConcurrentHashMap`'s iterator is fail-safe: it iterates a snapshot view and never throws, but may not reflect the newest writes. To remove during iteration, use `iterator.remove()` or `removeIf`."*

### B3. `String` — pool, immutability, and the output trap

```java
String a = "java";
String b = "java";
String c = new String("java");
String d = c.intern();

a == b          // true   — both point at the SAME pooled literal
a == c          // false  — new String() forces a fresh heap object
a.equals(c)     // true   — same characters
a == d          // true   — intern() returns the pooled reference
```

> *"String literals live in the **string pool**, which since Java 7 sits in the heap, not PermGen. Identical literals are the same object. `new String("java")` deliberately creates a second object — which is why `==` on strings is a bug and `.equals` is the answer."*

🔥 **"Why is `String` immutable?"** → *"Four reasons: the pool only works if nobody can mutate a shared literal; the hash is cached, which makes `String` a fast `HashMap` key; it's inherently thread-safe; and security — a filename or connection URL validated and then mutated by another thread would be a real vulnerability."*

🔥 **`StringBuilder` vs `StringBuffer`** → *"Same API. `StringBuffer` is synchronized, `StringBuilder` isn't. Use `StringBuilder` — the synchronization is almost never useful, since a builder is nearly always local to one method. Concatenating in a loop with `+` creates a new `String` each iteration — O(n²); that's the classic answer."*

### B4. Immutability — how to write an immutable class

```java
public final class Money {                       // ① final — can't be subclassed

    private final String currency;               // ② all fields final + private
    private final List<String> tags;

    public Money(String currency, List<String> tags) {
        this.currency = currency;
        this.tags = new ArrayList<>(tags);       // ③ DEFENSIVE COPY in
    }

    public List<String> getTags() {
        return List.copyOf(tags);                // ④ DEFENSIVE COPY out
    }
    // ⑤ no setters
}
```

> 🔴 *"Points ③ and ④ are the ones people miss — without them the caller still holds a reference to the internal list and can mutate it. And in Java 17, a **`record`** gives you final fields, a constructor, `equals`/`hashCode`/`toString` for free — but records are only *shallowly* immutable, so a record holding a `List` still needs a defensive copy in a compact constructor."*

> 🔗 More Java 17: **[12 — Java 17 Features](./12-java17-features.md)**

### B5. Streams — what they'll ask beyond "what is a stream"

| Question | The answer in one line |
|---|---|
| Intermediate vs terminal | Intermediate returns a stream and is **lazy**; terminal triggers execution. No terminal op = nothing runs. |
| `map` vs `flatMap` | `map` is 1→1; `flatMap` is 1→many, flattening nested streams (`List<List<T>>` → `List<T>`). |
| `findFirst` vs `findAny` | Same in sequential; in parallel `findAny` returns whichever finishes first — faster, non-deterministic. |
| Can you reuse a stream? | ❌ `IllegalStateException: stream has already been operated upon or closed`. |
| `Collectors.toMap` danger | Throws on duplicate keys unless you pass a merge function; also NPEs on a null value, unlike `HashMap`. |
| Parallel streams — when? | Large data, CPU-bound, no shared mutable state. They use the common ForkJoinPool, so **one slow parallel stream can starve every other one in the JVM**. Rarely worth it in a web app where the container already gives you thread-per-request. |
| `reduce` vs `collect` | `reduce` for immutable accumulation (sum); `collect` for mutable containers (list, map) — far more efficient for building collections. |

```java
// The three they ask you to write live
Map<String, List<Employee>> byDept =
    emps.stream().collect(groupingBy(Employee::getDept));

Map<String, Double> avgByDept =
    emps.stream().collect(groupingBy(Employee::getDept, averagingDouble(Employee::getSalary)));

Optional<Employee> topPaid =
    emps.stream().max(Comparator.comparingDouble(Employee::getSalary));

// Highest-paid per department — the Altimetrik question, and the SQL RANK() question in Java form
Map<String, Optional<Employee>> topPerDept =
    emps.stream().collect(groupingBy(Employee::getDept,
                          maxBy(Comparator.comparingDouble(Employee::getSalary))));
```

> 🔗 **[22 — Java Streams Coding Problems](./22-java-streams-coding-problems.md)** — type these, don't read them.

### B6. Exceptions

| | Checked | Unchecked |
|---|---|---|
| Extends | `Exception` | `RuntimeException` |
| Compiler forces handling | ✅ | ❌ |
| Examples | `IOException`, `SQLException` | `NullPointerException`, `IllegalArgumentException` |
| Use for | Recoverable, caller can act | Programming errors |

🔥 **"`throw` vs `throws`, `final`/`finally`/`finalize`"** — quick-fire, know them cold.
🔥 **"Can `finally` be skipped?"** → *"Yes — `System.exit()`, JVM crash, or the thread being killed. Also, a `return` inside `finally` swallows an exception thrown in `try`, which is a real bug. Prefer try-with-resources."*
🔥 **"Custom exception in Spring Boot?"** → `@ControllerAdvice` + `@ExceptionHandler` returning a `ProblemDetail`/error DTO with the right status. **Say this** — it connects core Java to the framework, which is what L2 rewards.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)   // @Valid failures
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", msg, Instant.now()));
    }
}
```

> 🔗 Predict-the-output practice: **[23 — Java Output Tricky Questions](./23-java-output-tricky-questions.md)**. L1 didn't ask any — but Codeboard did, and a second interviewer may. Do 15 of them the night before.

---

# C — Level 2: multithreading & concurrency

> 🔴 **Treat this as the most important technical section in the file.** Mphasis asked **two** multithreading questions at L1 out of ten — *"write a thread-safe Singleton"* and *"what is deadlock"*. That's 20% of the round on concurrency. **An interviewer who asks two multithreading questions at L1 is a concurrency interviewer**, and L2 will go further, not elsewhere.

| Sub-section | What |
|---|---|
| [C0](#c0--the-two-questions-they-already-asked-answered-deeper) | 🔴 The two they **already asked** — deeper, with every follow-up |
| [C1](#c1-creating-threads--the-lifecycle) | Creating threads · the lifecycle |
| [C2](#c2-synchronized--the-monitor) | `synchronized` and the monitor |
| [C3](#c3-volatile-vs-synchronized-vs-atomic) | `volatile` vs `synchronized` vs Atomic |
| [C4](#c4-synchronized-vs-reentrantlock) | `synchronized` vs `ReentrantLock` |
| [C5](#c5-executorservice--thread-pools) | `ExecutorService` and thread pools |
| [C6](#c6-completablefuture--parallel-calls-the-microservices-link) | `CompletableFuture` |
| [C7](#c7-concurrent-collections) | Concurrent collections |
| [C8](#c8-coordination-utilities--producerconsumer) | `CountDownLatch`, `Semaphore`, producer–consumer |
| [C9](#c9-threadlocal) | `ThreadLocal` — and the leak |
| [C10](#c10-multithreading-inside-spring-boot--the-question-they-will-actually-ask) | 🔴 Multithreading **inside Spring Boot** |
| [C11](#c11-virtual-threads-java-21) | Virtual threads (Java 21) |
| [C12](#c12-predict-the-output--multithreading) | Predict-the-output programs |
| [C13](#c13-rapid-fire) | Rapid-fire table |

---

## C0 — the two questions they already asked, answered deeper

### 🔴 Asked at L1: *"Write a program for a thread-safe Singleton class"*

You answered this. **L2 will not ask it again — it will ask what's underneath it.** There are five follow-ups, and they get progressively harder.

**The answer itself, one more time, in its best form:**

```java
public class ConfigManager {

    private ConfigManager() { }

    private static class Holder {                              // not loaded until first use
        private static final ConfigManager INSTANCE = new ConfigManager();
    }

    public static ConfigManager getInstance() {
        return Holder.INSTANCE;
    }
}
```

> *"Bill Pugh / static holder. The inner class isn't initialised until `getInstance()` first touches it, so it's **lazy**; and the JVM guarantees class initialisation is thread-safe because the classloader holds a lock during `<clinit>`, so it's **thread-safe with zero synchronization** on every subsequent call."*

---

**🔥 Follow-up 1 — "Why exactly is double-checked locking broken without `volatile`?"**

This is the single most likely L2 concurrency question, because it's the one that separates people who memorised the pattern from people who understand the memory model.

```java
instance = new Singleton();
```

> *"That one line is three bytecode-level steps:*
> 1. *allocate memory for the object,*
> 2. *run the constructor to initialise the fields,*
> 3. *assign the reference to `instance`.*
>
> *The JVM and the CPU are both allowed to **reorder 2 and 3**, because within a single thread the result is indistinguishable — that's the 'as-if-serial' rule. But another thread doing the first, unsynchronized `if (instance == null)` check can now see a **non-null reference to a half-constructed object** — fields still at their defaults. It uses it, and you get a null field or a zero where there shouldn't be one, intermittently, usually in production and never on your laptop."*
>
> *"`volatile` fixes it two ways: it forbids that reordering (it inserts memory barriers around the write), and it guarantees the write is visible to other threads immediately rather than sitting in a CPU cache. This is why DCL was genuinely, famously broken in Java 1.4 — the pre-Java-5 memory model didn't give `volatile` those guarantees. Java 5's JSR-133 fixed the model, not the pattern."*

```java
public class Singleton {
    private static volatile Singleton instance;    // 🔴 remove `volatile` and this is subtly broken

    public static Singleton getInstance() {
        if (instance == null) {                    // ① cheap check, no lock — the fast path
            synchronized (Singleton.class) {
                if (instance == null) {            // ② the real check, under the lock
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

> 🔥 **"Why two checks?"** → *"The outer one is a performance optimisation — once initialised, 99.99% of calls skip the lock entirely. The inner one is the correctness check — two threads can both pass the outer check simultaneously, and only one may create the instance."*

---

**🔥 Follow-up 2 — "What is the happens-before relationship?"**

The concept underneath all of it. Be able to say this:

> *"Happens-before is the JVM's ordering guarantee: if action A happens-before action B, then everything A did is **visible** to B. Without such a relationship, there is no guarantee at all that one thread ever sees another thread's write — the JIT and the CPU cache are both free to hide it indefinitely."*

**The relationships worth naming:**

| Rule | Guarantee |
|---|---|
| Program order | Within one thread, statements happen-before later ones |
| **Monitor lock** | Unlocking a monitor happens-before any later lock of the *same* monitor |
| **`volatile`** | A write to a volatile happens-before every later read of it |
| **`Thread.start()`** | Everything before `start()` happens-before the new thread's first action |
| **`Thread.join()`** | Everything in the thread happens-before `join()` returns |
| **`final` fields** | Correctly-constructed final fields are visible without synchronization |

> 🔴 *"The practical consequence: `synchronized` isn't only about mutual exclusion — it's also about **visibility**. Two threads that never actually run at the same time can still see stale data if neither synchronizes."*

---

**🔥 Follow-up 3 — "Can your singleton be broken?"** *(you have this from L1 — keep it)*

| Attack | Bill Pugh / DCL | `enum` |
|---|---|---|
| Reflection (`setAccessible(true)`) | ✅ broken | ❌ JVM forbids reflective enum construction |
| Serialization | ✅ broken (new instance on deserialize) | ❌ safe |
| Cloning | ✅ if `Cloneable` | ❌ safe |
| Two classloaders | ✅ broken | ✅ broken |

```java
public enum ConfigManager {          // ✅ safe against all three, for free
    INSTANCE;
    public String get(String key) { return props.get(key); }
}
```

---

**🔥 Follow-up 4 — 🔴 "Now make it a Spring bean. Is that the same thing?"**

> **No.** → the full answer is in [**A5**](#a5--is-a-spring-singleton-the-same-as-a-java-singleton). Container-scoped, not JVM-scoped; public constructor; two contexts give two instances.

---

**🔥 Follow-up 5 — "Is your singleton thread-safe once it's created?"**

🔴 **This one catches almost everyone.** They're testing whether you understand that *creating* the instance safely says nothing about *using* it safely.

```java
public class CounterSingleton {
    private static class Holder { static final CounterSingleton I = new CounterSingleton(); }
    public static CounterSingleton getInstance() { return Holder.I; }

    private int count = 0;                     // 🔴 shared mutable state

    public void increment() { count++; }       // ❌ NOT thread-safe — read-modify-write
}
```

> *"The **construction** is thread-safe. The **object** isn't. Every thread in the application shares this one instance, so any mutable field on it is contended. `count++` is three operations — read, add, write — and two threads interleaving lose an increment. The fixes are `AtomicInteger`, a `synchronized` method, or best of all making the singleton **stateless** so the question doesn't arise. That's exactly the rule for Spring `@Service` beans too."*

---

### 🔴 Asked at L1: *"What is deadlock in multithreading?"*

You gave the definition and the code. **Here is everything they can ask next.**

**🔥 Follow-up 1 — "How would you detect it in a running system?"** → the full procedure is in [**A9**](#a9--production-app-is-hung-walk-me-through-diagnosing-it). Short version: three thread dumps ten seconds apart; `jstack` prints a `Found one Java-level deadlock:` section naming both threads and both locks.

```
Found one Java-level deadlock:
=============================
"T1":  waiting to lock monitor 0x00007f9 (object 0x000000076ab, a java.lang.Object),
       which is held by "T2"
"T2":  waiting to lock monitor 0x00007f8 (object 0x000000076aa, a java.lang.Object),
       which is held by "T1"
```

> ⚠️ **The honest caveat that earns credit:** *"`jstack` only detects deadlocks on **intrinsic monitors and `ReentrantLock`**. A deadlock built out of `Semaphore`s, or a logical one where two threads are waiting on each other's `Future`, won't be reported — you have to read the stacks yourself."*

**🔥 Follow-up 2 — "Can you get a deadlock with a database?"**

🔴 **Say yes — this is the version that actually happens in the projects you've worked on.**

```sql
-- Transaction A               -- Transaction B
UPDATE account SET … WHERE id=1;   UPDATE account SET … WHERE id=2;   -- each holds a row lock
UPDATE account SET … WHERE id=2;   UPDATE account SET … WHERE id=1;   -- 💥 each wants the other's
```

> *"Exactly the same circular wait, but on database row locks instead of Java monitors. The difference is that the database **detects it and picks a victim** — MySQL/InnoDB rolls one transaction back with error 1213, `Deadlock found when trying to get lock`. So it doesn't hang forever, it fails one transaction. The fix is the same principle: touch rows in a consistent order — for a transfer, always lock the lower account id first — plus a retry on the deadlock error, since it's transient by nature."*

**🔥 Follow-up 3 — "Show me the fix"** — both, in order of preference:

```java
// ✅ FIX 1 — GLOBAL LOCK ORDERING. Break the circular-wait condition.
public void transfer(Account from, Account to, BigDecimal amount) {
    Account first  = from.getId() < to.getId() ? from : to;   // 🔑 always the same order
    Account second = from.getId() < to.getId() ? to : from;

    synchronized (first) {
        synchronized (second) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}
```

> ⚠️ *"And if the two ids can be equal — a self-transfer — that's a re-entrant lock on the same object, which is fine in Java because `synchronized` is reentrant. But it's worth guarding explicitly anyway."*

```java
// ✅ FIX 2 — tryLock with timeout. Break the no-preemption condition.
if (lockA.tryLock(1, TimeUnit.SECONDS)) {
    try {
        if (lockB.tryLock(1, TimeUnit.SECONDS)) {
            try { /* work */ }
            finally { lockB.unlock(); }
        } else {
            // back off, release everything, retry with a randomised delay
        }
    } finally { lockA.unlock(); }        // 🔴 unlock in finally, ALWAYS
}
```

> 🔴 **The random back-off matters:** *"If two threads both fail, both release and both retry immediately at the same interval, you get a **livelock** — they keep colliding politely forever. Randomised back-off breaks the symmetry."*

**🔥 Follow-up 4 — "What are the other concurrency hazards?"** — know all four by name, they're a common quick-fire:

| Hazard | What it is | Symptom |
|---|---|---|
| **Deadlock** | Circular wait on locks | Hangs forever, 0% CPU |
| **Livelock** | Threads keep reacting to each other, no progress | 100% CPU, nothing completes |
| **Starvation** | A thread never gets the resource — low priority, or unfair locks | One thread hangs, others fine |
| **Race condition** | Result depends on interleaving | Wrong data, intermittent, unreproducible |

> *"The distinction I'd draw is that a deadlock gives you **no result**, and a race condition gives you a **wrong result** — which is worse, because nothing looks broken."*

**🔥 Follow-up 5 — "Where have you actually hit a concurrency issue?"**

Have an honest answer ready. If you haven't hit a classic deadlock, say what you *have* hit:

> *"I haven't debugged a textbook two-lock deadlock in production. What I have dealt with is the shape that actually shows up in a Spring Boot app — **the reminder scheduler running on more than one instance**, where a `@Scheduled` job on two replicas picked up the same due reminders and sent duplicate notifications. That's a race on shared rows rather than a lock cycle, and the fix was making the claim atomic — a conditional update so only one instance can transition a row from PENDING to SENDING, `SELECT … FOR UPDATE SKIP LOCKED` being the other option — plus making the send idempotent so a retry can't double-deliver."*

🔴 **That answer is much stronger than inventing a deadlock story**, because it's specific, it's yours, and it shows you understand that in real systems the concurrency is *distributed*, not just in-JVM.

---

### C1. Creating threads & the lifecycle

**Four ways, and which one to say you use:**

```java
// ① extend Thread — rarely, because it burns your one inheritance slot
class Worker extends Thread { public void run() { } }
new Worker().start();

// ② implement Runnable — the classic answer
Runnable r = () -> System.out.println("work");
new Thread(r).start();

// ③ Callable + ExecutorService — returns a value AND can throw a checked exception
Future<Integer> f = pool.submit(() -> 42);

// ④ CompletableFuture — composable, non-blocking → C6
```

> *"`Runnable` over extending `Thread`, because Java has single inheritance and a task isn't a kind of thread — it's a job that a thread runs. But in real code I don't create threads directly at all; I submit tasks to an `ExecutorService`."*

🔥 **`Runnable` vs `Callable`** — asked constantly:

| | `Runnable` | `Callable<T>` |
|---|---|---|
| Method | `void run()` | `T call()` |
| Returns a value | ❌ | ✅ |
| Can throw a **checked** exception | ❌ | ✅ |
| Since | 1.0 | 1.5 |

🔥 **`start()` vs `run()`** — 🔴 **the classic trick question:**
> *"`start()` asks the JVM to create a new OS thread which then calls `run()`. Calling `run()` directly is just an ordinary method call on the **current** thread — the code executes, everything looks fine, and there is no concurrency at all. And calling `start()` twice throws `IllegalThreadStateException`: a thread is not reusable, which is precisely the reason thread pools exist."*

**The lifecycle — `Thread.State`:**

```
        start()              scheduler picks it
 NEW ─────────────► RUNNABLE ◄──────────────────┐
                      │  │  │                    │
   waiting for a lock │  │  └─ sleep(n) / wait(n)/ join(n) ─► TIMED_WAITING
                      │  └──── wait() / join() / park() ────► WAITING
                      ▼                                       (notify/notifyAll → back)
                   BLOCKED  ──── lock acquired ──────────────┘
                      │
                run() returns
                      ▼
                 TERMINATED
```

| State | Meaning | Where you see it in a thread dump |
|---|---|---|
| `NEW` | Created, `start()` not called | — |
| `RUNNABLE` | Running or ready to run | Normal work |
| `BLOCKED` | 🔴 Waiting to enter a `synchronized` block | **Lock contention / deadlock** |
| `WAITING` | `wait()`, `join()`, `LockSupport.park()` — no timeout | Waiting on a condition |
| `TIMED_WAITING` | Same, with a timeout | `sleep`, `poll(timeout)`, `Future.get(timeout)` |
| `TERMINATED` | Finished | — |

> 🔴 *"`BLOCKED` versus `WAITING` is the distinction that matters when you read a thread dump. **`BLOCKED` means it wants a monitor someone else holds** — many `BLOCKED` threads on the same lock is contention, and a cycle of them is a deadlock. `WAITING` usually means the thread is idle by design, like a pool thread waiting on its queue."*

**Daemon threads:** *"`setDaemon(true)` before `start()`. The JVM exits when only daemon threads remain — so background workers are daemons, but anything that must finish its work must not be, or it gets killed mid-flight at shutdown."*

**Interruption — the right way to stop a thread:**
```java
// ❌ Thread.stop() is deprecated and unsafe — it can leave objects half-modified.
// ✅ Cooperative interruption:
while (!Thread.currentThread().isInterrupted()) {
    try {
        queue.take();                          // blocking calls throw InterruptedException
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();    // 🔴 RESTORE the flag — catching it clears it
        break;
    }
}
```
> 🔴 *"Catching `InterruptedException` **clears** the interrupt flag. If you swallow it without restoring, code further up the stack never learns the thread was asked to stop — the classic 'why won't this app shut down' bug. Either rethrow it or restore the flag."*

---

### C2. `synchronized` — the monitor

> *"Every Java object has an intrinsic lock, a **monitor**. `synchronized` acquires it on entry and releases it on exit — including when an exception is thrown, which is one of its advantages over an explicit lock."*

```java
public synchronized void m() { }          // locks on `this`
public static synchronized void s() { }   // 🔴 locks on MyClass.class — a DIFFERENT lock
public void block() {
    synchronized (lockObject) { }         // locks on a specific object — preferred
}
```

🔴 **The trap they ask:** *"An instance method and a static method that are both `synchronized` on the same class **do not exclude each other** — one holds the instance monitor, the other holds the class monitor. Two different locks. People assume `synchronized` is one global thing and it isn't."*

**Why a private final lock object beats `synchronized(this)`:**
> *"`synchronized` on `this` means the lock is public — any outside code holding a reference to your object can lock on it and block your methods. A `private final Object lock = new Object()` is encapsulated. Same reason you don't synchronize on a `String` literal: it's pooled, so unrelated classes may share the same monitor."*

**Reentrancy:** *"Intrinsic locks are reentrant — a thread that holds a lock can acquire it again, which is what makes a synchronized method calling another synchronized method on the same object safe rather than an instant self-deadlock."*

**`wait()` / `notify()` — and the two rules:**
```java
synchronized (lock) {
    while (!condition) {        // 🔴 while, NOT if
        lock.wait();            // releases the monitor and parks
    }
    // condition is true and we hold the lock
}

synchronized (lock) {
    condition = true;
    lock.notifyAll();           // 🔴 notifyAll, not notify
}
```
| Rule | Why |
|---|---|
| **`while`, not `if`** | Spurious wakeups are permitted by the spec, and another thread may consume the condition between the notify and your re-acquiring the lock |
| **`notifyAll`, not `notify`** | `notify` wakes one *arbitrary* waiter — if it's waiting on a different condition, the right thread is never woken and you get a lost-wakeup hang |
| Must hold the monitor | Otherwise `IllegalMonitorStateException` |

🔥 **`wait()` vs `sleep()`** — one of the most-asked:

| | `wait()` | `sleep()` |
|---|---|---|
| Declared on | `Object` | `Thread` (static) |
| Releases the lock | ✅ **yes** | ❌ **no** — keeps holding it |
| Needs `synchronized` | ✅ | ❌ |
| Woken by | `notify`/`notifyAll`/timeout | Time elapsing / interrupt |

> 🔴 *"The one-sentence version: **`sleep` holds the lock, `wait` gives it up.** A `sleep` inside a synchronized block blocks every other thread for the whole duration — which is exactly how you turn a slow call into an outage."*

---

### C3. `volatile` vs `synchronized` vs `Atomic`

| | Visibility | Atomicity | Blocks | Cost |
|---|---|---|---|---|
| `volatile` | ✅ | ❌ | ❌ | Cheapest |
| `AtomicInteger` etc. | ✅ | ✅ (CAS) | ❌ (lock-free spin) | Cheap, contention-sensitive |
| `synchronized` / `Lock` | ✅ | ✅ | ✅ | Most expensive |

> *"`volatile count++` is still broken, because `++` is read-modify-write — three operations, and two threads interleave and lose an update. **`volatile` fixes visibility, not atomicity.** For a counter you want `AtomicInteger.incrementAndGet()`."*

**Where `volatile` alone is exactly right — the stop flag:**
```java
private volatile boolean running = true;      // 🔴 remove volatile → possible infinite loop

public void run() { while (running) { work(); } }
public void stop() { running = false; }
```
> *"Without `volatile`, the JIT is allowed to hoist the read out of the loop — it sees nothing in the loop modifying `running` — and the thread spins forever even after another thread sets it false. It's not a theoretical optimisation; it happens once the loop is hot enough to be compiled."*

**How CAS works — say this, it's the mechanism behind the whole `java.util.concurrent` package:**
> *"Compare-and-swap is a single CPU instruction: 'if this memory location still holds the value I read, replace it — otherwise tell me you failed.' `AtomicInteger.incrementAndGet` is a loop around it: read, compute, CAS, retry if someone beat you. It's **optimistic** — no lock, no thread parking, no context switch. The trade-off is that under heavy contention the retry loop burns CPU, so at very high contention a lock can actually win. `LongAdder` exists for exactly that case: it spreads the count over multiple cells and sums on read."*

| Atomic class | Use |
|---|---|
| `AtomicInteger` / `AtomicLong` | Counters, sequence numbers |
| `AtomicBoolean` | One-shot flags — `compareAndSet(false, true)` to elect a single winner |
| `AtomicReference<T>` | Lock-free swap of an immutable object |
| `LongAdder` | High-contention counters — faster than `AtomicLong` |

---

### C4. `synchronized` vs `ReentrantLock`

| `synchronized` | `ReentrantLock` |
|---|---|
| Language keyword | Class in `java.util.concurrent.locks` |
| Auto-released on exit/exception | 🔴 **You must `unlock()` in a `finally`** |
| No timeout | ✅ `tryLock(1, SECONDS)` — the deadlock escape hatch |
| Not interruptible | ✅ `lockInterruptibly()` |
| No fairness option | ✅ optional FIFO fairness (`new ReentrantLock(true)`) |
| One wait-set | ✅ multiple `Condition`s |
| Can't query it | ✅ `isLocked()`, `getHoldCount()` |

```java
private final ReentrantLock lock = new ReentrantLock();

lock.lock();
try   { /* work */ }
finally { lock.unlock(); }        // 🔴 if you forget this, the lock is held forever
```

> *"I'd default to `synchronized` — it's simpler and the JVM optimises it well (biased/thin locks). I reach for `ReentrantLock` when I specifically need a **timeout**, an **interruptible** acquire, **fairness**, or multiple conditions. The cost is that you own the unlock, and forgetting it in a `finally` is a permanent hang."*

🔥 **`ReadWriteLock`** → *"`ReentrantReadWriteLock` allows many concurrent readers but an exclusive writer. Right for a read-heavy cache — but if writes are frequent, readers starve or the overhead outweighs it, and `StampedLock` with optimistic reads is the faster modern option."*

---

### C5. `ExecutorService` & thread pools

> *"Creating threads directly doesn't scale — each one costs about a megabyte of stack and an OS context switch, and unbounded threads will OOM the JVM. An `ExecutorService` gives you a fixed pool that pulls tasks off a queue, so threads are reused."*

```java
ExecutorService pool = Executors.newFixedThreadPool(10);

Future<Integer> f = pool.submit(() -> compute());     // Callable → value + checked exceptions
Integer result = f.get();                             // ⚠️ BLOCKS until done

pool.shutdown();                                      // no new tasks; finish what's queued
if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
    pool.shutdownNow();                               // interrupt the stragglers
}
```

| | `execute(Runnable)` | `submit(Callable/Runnable)` |
|---|---|---|
| Returns | void | `Future<T>` |
| Return value | ❌ | ✅ |
| Exception | Goes to the uncaught-exception handler | 🔴 **Swallowed** until you call `get()` |

> 🔴 **A genuine production trap worth saying out loud:** *"An exception inside a task submitted with `submit()` is captured in the `Future` and disappears silently if nobody calls `get()`. The task looks like it succeeded; there's nothing in the logs. Either call `get()`, or wrap the task body in a try/catch that logs."*

**The factory methods, and why you shouldn't use them:**

| Factory | What it really is | Risk |
|---|---|---|
| `newFixedThreadPool(n)` | n threads, **unbounded** `LinkedBlockingQueue` | 🔴 Queue grows until OOM — no back-pressure |
| `newCachedThreadPool()` | 0..`Integer.MAX_VALUE` threads, `SynchronousQueue` | 🔴 Unbounded **threads** under load |
| `newSingleThreadExecutor()` | 1 thread, unbounded queue | Serialises work; same queue risk |
| `newScheduledThreadPool(n)` | Delayed / periodic tasks | The `@Scheduled` engine |

> 🔴 *"In production I'd construct a `ThreadPoolExecutor` explicitly rather than use the factories, because both common ones are unbounded in one direction or the other. A **bounded queue plus an explicit rejection policy** is what gives you back-pressure instead of a slow-motion OOM."*

```java
new ThreadPoolExecutor(
    10, 20,                                   // core, max
    60L, TimeUnit.SECONDS,                    // keep-alive for threads above core
    new ArrayBlockingQueue<>(500),            // 🔴 BOUNDED
    new ThreadFactoryBuilder().setNameFormat("reminder-%d").build(),   // named → readable dumps
    new ThreadPoolExecutor.CallerRunsPolicy() // back-pressure: the submitter runs it
);
```

🔥 **"How does it decide to create a thread?"** — 🔴 **the order surprises people:**
> *"Below core size → create a new thread. At core size → **queue the task**. Only when the queue is *full* does it create threads up to max. So with an unbounded queue, `maximumPoolSize` is dead code — the pool never grows past core, because the queue never fills."*

| Rejection policy | Behaviour |
|---|---|
| `AbortPolicy` (default) | Throws `RejectedExecutionException` |
| `CallerRunsPolicy` | ✅ The submitting thread runs it — natural back-pressure |
| `DiscardPolicy` / `DiscardOldestPolicy` | Silently drops — dangerous |

**Pool sizing:** *"CPU-bound → roughly `Runtime.getRuntime().availableProcessors()`; more threads than cores just adds context switching. IO-bound → higher, because threads are mostly parked waiting; the rough formula is `cores × (1 + wait/compute)`. And I'd measure rather than trust the formula."*

---

### C6. `CompletableFuture` — parallel calls, the microservices link

```java
CompletableFuture<Profile>    p = CompletableFuture.supplyAsync(() -> userClient.get(id), pool);
CompletableFuture<List<Quiz>> q = CompletableFuture.supplyAsync(() -> quizClient.get(id), pool);

Dashboard dash = p.thenCombine(q, Dashboard::new)
                  .orTimeout(2, TimeUnit.SECONDS)
                  .exceptionally(ex -> Dashboard.partial())    // graceful degradation
                  .join();
```

> *"This is the backend equivalent of `forkJoin` in RxJS — two independent downstream calls in parallel instead of sequentially, so the latency is the slower of the two rather than the sum. That's exactly the dashboard-aggregation problem from [A10](#a10--give-me-a-number--api-performance), solved on the server side."*

| Method | Meaning |
|---|---|
| `supplyAsync` / `runAsync` | Start async — with, or without, a result |
| `thenApply` | `map` — transform the result |
| `thenCompose` | `flatMap` — when the function itself returns a `CompletableFuture` |
| `thenCombine` | Merge two independent futures |
| `allOf` / `anyOf` | Wait for all / the first |
| `exceptionally` / `handle` | Fallback on failure / handle both outcomes |
| `orTimeout` (Java 9+) | Fail after a duration |

> 🔴 **Two things to say:** *"First — the `Async` variants without an explicit executor use the **common ForkJoinPool**, which is shared JVM-wide and sized to cores minus one. Putting blocking IO on it starves parallel streams and every other user of it, so I always pass my own pool. Second — `join()` and `get()` are blocking; if you block on every call you've written synchronous code with extra ceremony. The value is in composing and blocking once, at the edge."*

🔥 **`thenApply` vs `thenCompose`** → *"`thenApply` is 1→1. `thenCompose` flattens — use it when the mapper returns a `CompletableFuture`, otherwise you end up with `CompletableFuture<CompletableFuture<T>>`. Identical to `map` vs `flatMap`, and to `map` vs `switchMap` in RxJS."*

---

### C7. Concurrent collections

| Instead of | Use | Why |
|---|---|---|
| `HashMap` | `ConcurrentHashMap` | Per-bucket locking; lock-free reads |
| `ArrayList` | `CopyOnWriteArrayList` | Read-heavy, write-rare (listener lists) |
| `Collections.synchronizedList` | Usually the above | One global lock; and **iteration still needs manual synchronization** |
| `Hashtable` / `Vector` | Anything else | Legacy, whole-object lock |
| A hand-rolled queue | `BlockingQueue` | Blocking put/take — producer/consumer for free |

> *"`ConcurrentHashMap` since Java 8 uses CAS to insert into an empty bin and `synchronized` on the bin head otherwise, so writes only contend when two threads hit the **same bucket**, and reads are lock-free because the nodes' value and next fields are `volatile`. `Hashtable` synchronizes every method on the whole map — one thread at a time, always."*

🔴 **The trap: individually atomic ≠ atomic together.**
```java
// ❌ NOT atomic — two separate operations; another thread can slip between them
if (!map.containsKey(k)) map.put(k, v);

// ✅ Atomic
map.putIfAbsent(k, v);
map.computeIfAbsent(k, key -> expensiveLoad(key));
map.merge(k, 1, Integer::sum);                       // a thread-safe counter
```

> *"Every method on `ConcurrentHashMap` is atomic on its own, but a **check-then-act sequence isn't**. That's the most common misuse — people assume the collection being 'thread-safe' makes their compound operation safe, and it doesn't. Same reason a `synchronizedList` still needs an explicit `synchronized` block around iteration."*

**`CopyOnWriteArrayList`:** *"Every write copies the whole backing array, so writes are O(n) — but reads need no lock at all and its iterator never throws `ConcurrentModificationException` because it iterates the snapshot. Right for a listener list read constantly and written at startup; wrong for anything write-heavy."*

---

### C8. Coordination utilities & producer–consumer

| Utility | One-line purpose |
|---|---|
| `CountDownLatch` | Wait for N things to finish. **One-shot** — can't be reset |
| `CyclicBarrier` | N threads wait for each other, then all proceed. **Reusable** |
| `Semaphore` | Limit concurrent access to N permits — a rate limiter / connection pool |
| `BlockingQueue` | Hand work between threads with blocking put/take |
| `Exchanger` | Two threads swap objects |
| `Phaser` | A flexible, multi-phase barrier |

```java
// CountDownLatch — the standard "wait for all the parallel calls" pattern
CountDownLatch latch = new CountDownLatch(3);
for (Task t : tasks) pool.submit(() -> { try { t.run(); } finally { latch.countDown(); } });
latch.await(5, TimeUnit.SECONDS);       // 🔴 always the timed version in production
```

> 🔴 *"`countDown()` goes in a `finally`. If a task throws and skips it, the latch never reaches zero and `await()` blocks forever — which is why I use the timed `await` as a second line of defence."*

**Producer–consumer with `BlockingQueue` — a very common "write this" request:**

```java
BlockingQueue<Order> queue = new ArrayBlockingQueue<>(100);   // bounded → back-pressure

// Producer
new Thread(() -> {
    while (running) queue.put(nextOrder());       // BLOCKS when full — this IS the back-pressure
}).start();

// Consumer
new Thread(() -> {
    while (running) process(queue.take());        // BLOCKS when empty
}).start();
```

> *"The whole point is that `put` and `take` handle the waiting — no `wait`/`notify`, no lost wakeups, no spurious-wakeup loop. And using a **bounded** queue is deliberate: an unbounded one lets a fast producer outrun the consumer until it exhausts memory. Blocking the producer is the feature."*

**Graceful shutdown — the poison-pill pattern:**
```java
queue.put(POISON_PILL);      // consumer sees it, stops, and re-inserts it for its siblings
```

**Semaphore — limiting concurrency against a fragile downstream:**
```java
private final Semaphore permits = new Semaphore(5);   // at most 5 concurrent AI calls

permits.acquire();
try   { return aiClient.explain(problem); }
finally { permits.release(); }                        // 🔴 finally, always
```
> *"This is the **bulkhead** pattern from the microservices question at L1 — capping how many threads can be tied up by one slow dependency so it can't exhaust the whole pool."*

---

### C9. `ThreadLocal`

> *"A `ThreadLocal` gives each thread its own copy of a variable. Spring uses it heavily — the security context, the transaction's `EntityManager`, and the request context are all thread-locals, which is how `@Transactional` knows which transaction you're in without passing it through every method signature."*

```java
private static final ThreadLocal<SimpleDateFormat> FORMAT =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
```

🔴 **The two things they're testing:**

1. **`SimpleDateFormat` is not thread-safe** — a shared static instance silently produces wrong dates under load. *"Which is why in modern code I'd use `DateTimeFormatter` from `java.time`, which is immutable and thread-safe, and skip the ThreadLocal entirely."*
2. **`ThreadLocal` leaks in a thread pool.** *"Pool threads live forever, so a value set during one request is still there for the next request on that same thread — a stale-data bug and a memory leak. You must `remove()` in a `finally`. This is also why `ThreadLocal` state doesn't survive an `@Async` hop unless you propagate it explicitly."*

```java
try { CONTEXT.set(user); doWork(); }
finally { CONTEXT.remove(); }        // 🔴 mandatory on pooled threads
```

---

### C10. Multithreading inside Spring Boot — 🔴 the question they will actually ask

Pure-Java concurrency is the textbook half. **The half that shows experience is knowing where threads live in a Spring app** — and this is where an L2 interviewer for a Java/Spring role will take it.

**① "Is a Spring `@Service` thread-safe?"**
> *"The bean is a singleton shared by every request thread, and Tomcat handles each request on its own thread — so **any mutable instance field on a `@Service` is a race condition**. Keep beans stateless: state belongs in method parameters and local variables, which are per-thread on the stack. A `private List<String> items` field on a service is the single most common concurrency bug in Spring codebases, and it works perfectly with one user testing on a laptop."*

**② `@Async`**
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean("notificationExecutor")
    public Executor executor() {
        ThreadPoolTaskExecutor e = new ThreadPoolTaskExecutor();
        e.setCorePoolSize(5);
        e.setMaxPoolSize(10);
        e.setQueueCapacity(100);              // bounded
        e.setThreadNamePrefix("notify-");     // readable thread dumps
        e.initialize();
        return e;
    }
}

@Async("notificationExecutor")
public CompletableFuture<Void> sendReminder(Long userId) { … }
```

| 🔴 `@Async` trap | Why |
|---|---|
| **Self-invocation** | Proxy-based — calling it from the same class runs it synchronously, silently |
| **Must be `public`** | Private/final methods can't be proxied |
| **Return type** | `void` or `CompletableFuture<T>`. A plain `String` return is always `null` |
| **Exceptions in `void` methods** | Vanish — supply an `AsyncUncaughtExceptionHandler` |
| **No default executor** | Without a configured one, older Boot used `SimpleAsyncTaskExecutor`, which **creates a new thread per call** and doesn't pool at all |
| **Nothing propagates** | Security context, transaction and MDC do **not** cross the thread boundary automatically |

> *"It's the same AOP-proxy family of traps as `@Transactional` and `@CircuitBreaker` — which is the point I made at L1 about `spring-boot-starter-aop`. One mechanism, three annotations, the same silent failure."*

**③ 🔴 "Does `@Transactional` work inside `@Async`?"** — an excellent discriminator question.
> *"Not the caller's transaction. The transaction is bound to the calling thread via a ThreadLocal, and the async method runs on a **different** thread — so it starts its own transaction, or none. The practical consequence is a real bug: you commit, fire an async job, and the async job can't see the data if it started before the commit landed. The fix is `@TransactionalEventListener(phase = AFTER_COMMIT)` so the async work is only triggered once the transaction has actually committed."*

**④ `@Scheduled` — and the multi-instance problem**
> *"`@Scheduled` runs on a single-threaded scheduler by default, so one long task delays every other scheduled job in the app — you configure a `ThreadPoolTaskScheduler` to fix that. But the bigger issue is that **in production you run more than one instance, and every instance fires the same job**. That's exactly the duplicate-reminder problem I described — you need ShedLock, a database-level claim with `FOR UPDATE SKIP LOCKED`, or an external scheduler."*

**⑤ The database is where your real concurrency lives**
> *"Honestly, in a web application most of the concurrency isn't threads I create — it's **many requests hitting the same rows**. Two users updating the same record is handled with optimistic locking, a `@Version` column that fails the second commit with `OptimisticLockException` and lets me retry. And the thread pool that matters most in practice is the **HikariCP connection pool** — more Tomcat threads than DB connections means threads queue on `getConnection()`, which is what a hung Spring Boot app usually turns out to be."*

---

### C11. Virtual threads (Java 21)

Worth one paragraph — it signals you're current, and Mphasis works on modernisation programmes.

> *"Java 21 made virtual threads final. They're lightweight threads scheduled by the JVM rather than the OS, so you can have millions of them; when one blocks on IO it **unmounts** from its carrier thread instead of holding an OS thread hostage. The point is that the thread-per-request model becomes cheap again — you get the scalability of reactive code while writing plain blocking code that's readable and debuggable. In Spring Boot 3.2 it's one property: `spring.threads.virtual.enabled=true`."*
>
> *"Two caveats: don't **pool** virtual threads — they're cheap to create, pooling defeats the purpose; and `synchronized` blocks used to pin a virtual thread to its carrier, so `ReentrantLock` was preferred in hot paths."*

---

### C12. Predict-the-output — multithreading

L1 didn't ask output questions, but Codeboard did, and these are the concurrency ones that show up.

```java
// ① What prints?
Thread t = new Thread(() -> System.out.println("A"));
t.run();                                       // ← run(), not start()
System.out.println("B");
```
> **`A` then `B`, always, on the main thread.** `run()` is an ordinary method call — no new thread at all.

```java
// ② What prints?
Thread t = new Thread(() -> System.out.println("A"));
t.start();
t.start();
```
> **`A`, then `IllegalThreadStateException`.** A thread can't be restarted.

```java
// ③ What's the final count?
class Counter { int count = 0; void inc() { count++; } }
// 2 threads × 10,000 increments each
```
> **Something ≤ 20000, non-deterministic.** `count++` is read-modify-write. Marking it `volatile` does **not** fix it — you need `AtomicInteger` or `synchronized`.

```java
// ④ Does this terminate?
static boolean running = true;                 // no volatile
new Thread(() -> { while (running) { } }).start();
Thread.sleep(1000);
running = false;
```
> **Possibly never.** With no `volatile` and an empty loop body, the JIT can hoist the read out of the loop. It often terminates in `-Xint` and hangs once compiled — which is exactly why it's a good interview question.

```java
// ⑤ What prints?
synchronized (this) { Thread.sleep(1000); }
```
> Nothing about output — the point is: **`sleep` does not release the lock.** Every other thread waiting on that monitor is blocked for the full second.

```java
// ⑥ HashMap under concurrent writes?
```
> *"Lost updates, and on Java 7 an infinite loop in `get()` because the resize could create a circular linked list — a genuine production hang, and one of the reasons the Java 8 rewrite happened. Java 8 removed the cycle, but it's still unsafe: entries are still lost."*

> 🔗 More: **[23 — Java Predict the Output](./23-java-output-tricky-questions.md)**

---

### C13. Rapid-fire

| Question | Answer |
|---|---|
| Process vs thread | Process = own memory space; threads share the heap within one process (and have their own stack) |
| What's per-thread vs shared? | 🔴 **Stack, program counter and locals are per-thread; heap and static fields are shared.** That's why only object state races |
| `start()` vs `run()` | New thread vs plain method call on the current thread |
| `Runnable` vs `Callable` | No return / no checked exceptions vs both |
| `wait()` vs `sleep()` | `wait` releases the lock (and needs `synchronized`); `sleep` holds it |
| `wait` in `while`, not `if`? | Spurious wakeups + another thread may consume the condition first |
| `notify` vs `notifyAll` | `notify` wakes one arbitrary waiter → lost-wakeup risk. Prefer `notifyAll` |
| Thread states | NEW · RUNNABLE · BLOCKED · WAITING · TIMED_WAITING · TERMINATED |
| `BLOCKED` vs `WAITING` | Waiting for a **monitor** vs waiting for a **condition** |
| Daemon thread | JVM exits when only daemons remain; `setDaemon` before `start()` |
| Stop a thread | Cooperative interruption — `Thread.stop()` is deprecated and unsafe |
| Caught `InterruptedException`? | 🔴 Restore the flag — catching it clears it |
| `volatile` guarantees | Visibility + no reordering. **Not** atomicity |
| Atomic classes | CAS — lock-free, optimistic retry loop |
| `synchronized` static vs instance | 🔴 **Different locks** — class monitor vs instance monitor. They don't exclude each other |
| Reentrant? | Yes — `synchronized` and `ReentrantLock` both |
| `ConcurrentHashMap` vs `Hashtable` | Per-bucket CAS/sync vs one lock for the whole map |
| Check-then-act on a concurrent map | 🔴 Not atomic — use `putIfAbsent` / `computeIfAbsent` / `merge` |
| Executor `submit` exception | Swallowed into the `Future` until `get()` is called |
| Thread pool growth order | core → **queue** → max. Unbounded queue = max never used |
| `CountDownLatch` vs `CyclicBarrier` | One-shot, one waiter set vs reusable, threads wait for each other |
| Deadlock's four conditions | Mutual exclusion · hold-and-wait · no preemption · **circular wait** |
| Deadlock fix | 🔴 Global lock ordering; or `tryLock` with timeout + randomised back-off |
| Detect a deadlock | 3 thread dumps 10s apart; `jstack` prints `Found one Java-level deadlock` |
| Deadlock vs livelock vs starvation | No progress + no CPU · no progress + full CPU · one thread never scheduled |
| `ThreadLocal` in a pool | 🔴 Must `remove()` in a `finally` — pool threads outlive the request |
| Is a Spring `@Service` thread-safe? | Only if it's stateless. Singleton bean + mutable field = race |
| `@Async` + `@Transactional` | Different thread → **not** the caller's transaction |
| Virtual threads | Java 21, JVM-scheduled, unmount on blocking IO. Don't pool them |

---

# D — Level 2: Spring & Spring Boot depth

### D1. IoC and DI

> *"Inversion of Control means the framework creates and wires objects instead of my code calling `new`. Dependency Injection is how it does it. The practical benefit is testability — my service takes a `PaymentGateway` interface in its constructor, so in a unit test I pass a mock and never touch the network."*

🔥 **"Constructor vs field vs setter injection — which and why?"** — 🔴 **an extremely common L2 question.**

> *"**Constructor injection**, always. Three reasons: the dependencies can be `final`, so the object is immutable and fully valid the moment it exists; a missing dependency fails at startup rather than with an NPE at 2 a.m.; and I can construct it in a test with plain `new`, no Spring at all. Field injection with `@Autowired` hides dependencies, can't be `final`, and needs reflection to test. Since Spring 4.3 a single constructor doesn't even need `@Autowired`."*
>
> *"One honest caveat: constructor injection makes a **circular dependency** fail at startup, where field injection would have quietly allowed it. People treat that as a downside — it's the feature. A cycle means the design is wrong."*

### D2. Bean scopes and lifecycle

| Scope | One bean per… |
|---|---|
| `singleton` (default) | container |
| `prototype` | injection / `getBean()` call |
| `request` | HTTP request |
| `session` | HTTP session |
| `application` | ServletContext |

```
Instantiate → populate properties → *Aware callbacks → BeanPostProcessor.before
→ @PostConstruct → afterPropertiesSet() → BeanPostProcessor.after → 🟢 READY
→ @PreDestroy → destroy()
```

> ⚠️ *"Spring does **not** call `@PreDestroy` on prototype beans — the container hands the instance over and forgets it. If a prototype holds a resource, you close it yourself."*

### D3. `@Component` vs `@Bean` vs `@Configuration`

| | `@Component` (+`@Service`/`@Repository`) | `@Bean` |
|---|---|---|
| Where | On your own class | On a method in a `@Configuration` class |
| Detected by | Component scanning | Explicit method call |
| Use for | Classes you own | 🔴 **Third-party classes you can't annotate** |

> *"`@Repository` additionally translates vendor-specific persistence exceptions into Spring's `DataAccessException` hierarchy — it's not just a documentation marker."*

🔥 **"Why is `@Configuration` different from `@Component` for `@Bean` methods?"**
> *"`@Configuration` classes are CGLIB-proxied, so calling one `@Bean` method from another returns the **existing** singleton rather than creating a second instance. With plain `@Component` (`proxyBeanMethods = false` semantics) that same call is an ordinary Java call and you get a duplicate object. It's the kind of thing that produces two connection pools and a very confusing bug."*

### D4. 🔴 `@Transactional` — the most re-probed Spring topic

**Propagation:**

| Value | Behaviour |
|---|---|
| `REQUIRED` (default) | Join the existing transaction, or start one |
| `REQUIRES_NEW` | 🔴 Suspend the current one, always start a new one — commits independently |
| `SUPPORTS` | Join if one exists, else run non-transactionally |
| `MANDATORY` | Must already be in one, else throw |
| `NEVER` / `NOT_SUPPORTED` | Throw / suspend |
| `NESTED` | Savepoint inside the current transaction |

> *"`REQUIRES_NEW` is what you use for audit logging — you want the audit row to survive even if the business transaction rolls back."*

**Isolation levels — and what each prevents:**

| Level | Dirty read | Non-repeatable read | Phantom read |
|---|---|---|---|
| `READ_UNCOMMITTED` | ❌ possible | ❌ | ❌ |
| `READ_COMMITTED` (Postgres/Oracle default) | ✅ prevented | ❌ | ❌ |
| `REPEATABLE_READ` (**MySQL default**) | ✅ | ✅ | ❌ |
| `SERIALIZABLE` | ✅ | ✅ | ✅ |

🔴 **The three traps — know all three, they're the question:**

```java
// TRAP 1 — SELF-INVOCATION. The proxy is bypassed; there is NO transaction.
@Service
public class OrderService {
    public void placeOrder() {
        this.save();              // ❌ plain Java call — @Transactional does nothing
    }
    @Transactional
    public void save() { }
}

// TRAP 2 — @Transactional only rolls back on RuntimeException / Error by default.
@Transactional                                    // ❌ IOException will NOT roll back
public void process() throws IOException { }

@Transactional(rollbackFor = Exception.class)     // ✅
public void process2() throws IOException { }

// TRAP 3 — a private / final method can't be proxied. Silently no transaction.
@Transactional
private void save() { }                           // ❌ never transactional
```

> *"All three are the same root cause: `@Transactional` is implemented with **AOP proxies**. Anything that bypasses the proxy — self-invocation, a private or final method, a missing `spring-boot-starter-aop` — silently disables it. And it's *silent*: the code runs, the data isn't protected. Exactly the same failure mode as `@CircuitBreaker`, which is why I mentioned AOP in the L1 round."*

> 🔗 Deep dive: **[24 — ORM / JPA / Hibernate](./24-orm-jpa-hibernate.md)**

### D5. Spring Boot auto-configuration

> *"`@SpringBootApplication` is three annotations: `@SpringBootConfiguration`, `@ComponentScan`, and `@EnableAutoConfiguration`. Auto-configuration reads `META-INF/spring/…AutoConfiguration.imports` from every jar on the classpath and applies each configuration **conditionally** — `@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`. So if `spring-boot-starter-data-jpa` is present and a `DataSource` isn't already defined, Boot configures one from your properties."*
>
> *"`@ConditionalOnMissingBean` is the important one — it means **defining your own bean silently wins**. That's how Boot stays out of your way. And `/actuator/conditions` shows exactly which auto-configurations matched and which were skipped and why — that's the first place I look when a bean I expected isn't there."*

**Excluding one:** `@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)`

### D6. REST API design — likely in both rounds

| Topic | The answer |
|---|---|
| Status codes | 200 OK · **201 Created** (+ `Location`) · 204 No Content · 400 Bad Request · 401 unauthenticated · **403 authenticated but not allowed** · 404 · 409 Conflict · 422 · 500 |
| `PUT` vs `PATCH` | PUT replaces the whole resource and is idempotent; PATCH is partial. |
| Idempotency | GET/PUT/DELETE idempotent; POST is not. For payments, use an `Idempotency-Key` header. |
| Versioning | URI (`/api/v1/...`) — simplest and most common. Also header or content negotiation. |
| Validation | `@Valid` on the `@RequestBody` + JSR-380 (`@NotNull`, `@Size`, `@Email`), handled in `@RestControllerAdvice`. |
| 🔴 DTO vs entity | **Never return the entity.** It leaks the schema, drags lazy proxies into serialisation (`LazyInitializationException` / infinite recursion on bidirectional relations), and couples your API to your DB. Map to a DTO or a record. |
| Pagination | `Pageable` → `Page<T>` with `?page=0&size=20&sort=name,asc`. |
| `@RequestParam` vs `@PathVariable` | Path variable identifies the resource; request param filters/sorts/pages. |
| Filter vs Interceptor | Filter is Servlet-level, sees every request incl. static, no Spring context. Interceptor is Spring MVC, knows the handler — right place for auth checks and logging with controller context. |

---

# E — Level 2: JPA / Hibernate

They asked association vs aggregation at L1 — an OOP question that sits right next to entity mapping. Expect JPA at L2.

### E1. 🔴 The N+1 problem — the single most asked ORM question

```java
List<Department> depts = repo.findAll();           // 1 query
for (Department d : depts) {
    d.getEmployees().size();                       // N more queries — one per department
}
```

**Three fixes:**

```java
// ① JOIN FETCH — one query, best for a targeted case
@Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.employees")
List<Department> findAllWithEmployees();

// ② @EntityGraph — declarative, works with derived queries + Pageable
@EntityGraph(attributePaths = "employees")
List<Department> findAll();

// ③ Batch fetching — N+1 becomes N/batch_size + 1
@BatchSize(size = 25)
private List<Employee> employees;
```

> ⚠️ *"`JOIN FETCH` plus `Pageable` is a trap — Hibernate can't paginate a joined result set in SQL, so it pulls everything into memory and pages there, logging `HHH000104: firstResult/maxResults specified with collection fetch; applying in memory`. On a large table that's an OOM. For paginated fetches use `@EntityGraph` or a two-query approach: page the IDs, then fetch the collection for those IDs."*

**How you'd notice it:** `spring.jpa.show-sql=true`, or better, `p6spy` / Hibernate statistics — *"I'd count the queries, not eyeball the logs."*

### E2. `FetchType` and `LazyInitializationException`

| | `LAZY` | `EAGER` |
|---|---|---|
| Default for | `@OneToMany`, `@ManyToMany` | `@ManyToOne`, `@OneToOne` |
| Loads | On first access, via proxy | With the parent |

> *"`LazyInitializationException` means you touched a lazy association after the persistence context closed — typically in the controller or during JSON serialisation. The **wrong** fix is `spring.jpa.open-in-view=true` (it's on by default and I'd turn it off — it holds the DB connection for the whole request) or switching to EAGER (which reintroduces N+1 everywhere). The **right** fix is to fetch what you need inside the transaction with `JOIN FETCH`, and return a DTO."*

### E3. Quick-fire JPA

| Question | Answer |
|---|---|
| Entity states | Transient → Managed → Detached → Removed |
| `save` vs `saveAndFlush` | `save` may defer the SQL to commit; `saveAndFlush` pushes it now (so you can read it back in the same transaction) |
| `getReferenceById` vs `findById` | Returns a lazy proxy without hitting the DB (throws on access if missing) vs an actual `SELECT` |
| First-level cache | The persistence context / `EntityManager` — always on, per transaction. Same entity twice in one transaction = one query. |
| Second-level cache | Across sessions, opt-in — EhCache/Hazelcast/Redis |
| `CascadeType` | ALL, PERSIST, MERGE, REMOVE, REFRESH, DETACH. ⚠️ `REMOVE` on `@ManyToMany` deletes the other side's rows — almost always a bug |
| `orphanRemoval` vs `CascadeType.REMOVE` | Orphan removal deletes a child *removed from the collection*; cascade remove only fires when the parent is deleted |
| Optimistic vs pessimistic locking | `@Version` column, fails at commit with `OptimisticLockException` — right for low contention. `SELECT … FOR UPDATE` blocks others — right for high contention/money |
| Bidirectional relation + JSON | Infinite recursion → `@JsonIgnore` / `@JsonManagedReference` — or just return a DTO |

---

# F — Level 2: microservices, one level down

### F1. How do services talk to each other?

| | `RestTemplate` | `WebClient` | `FeignClient` |
|---|---|---|---|
| Style | Blocking | Non-blocking, reactive | Declarative, blocking |
| Status | 🔴 **In maintenance** since Spring 5 | Recommended | Spring Cloud OpenFeign |
| Code | Verbose | Fluent | An interface + annotations |

```java
@FeignClient(name = "inventory-service", fallback = InventoryFallback.class)
public interface InventoryClient {
    @GetMapping("/stock/{sku}")
    StockResponse getStock(@PathVariable String sku);
}
```

> *"Feign gives you the cleanest call site — it's just an interface, and it integrates with service discovery and Resilience4j. `WebClient` is what I'd use where I need real concurrency or streaming. `RestTemplate` still works but new code shouldn't use it."*

🔥 **"Sync or async between services?"** → *"Synchronous REST is fine when the caller genuinely needs the answer to continue. But every sync hop couples availability — if A calls B calls C, A is only as available as the weakest link. Anything that doesn't need an immediate answer should be an **event on a queue** — Kafka or RabbitMQ. On RoboGebra the reminder dispatch is exactly that shape: the quiz service doesn't need to wait for a notification to be sent."*

### F2. The supporting cast

| Component | What it does | Typical tech |
|---|---|---|
| **API Gateway** | Single entry: routing, auth, rate limiting, CORS, SSL termination | Spring Cloud Gateway |
| **Service Discovery** | Services register and find each other by name, not host | Eureka, Consul |
| **Config Server** | Externalised, per-environment config, refreshable | Spring Cloud Config |
| **Distributed tracing** | One trace id across all hops | Micrometer Tracing + Zipkin |
| **Log aggregation** | Search all services' logs in one place | ELK / Loki |
| **Message broker** | Async, decoupled communication | Kafka, RabbitMQ |

### F3. Security — likely if the client is BFSI/insurance (Mphasis's core domains)

> *"Auth at the gateway, stateless downstream. The user logs in and gets a **JWT**; the gateway validates the signature and forwards the token. Each service can then authorise from the claims without a call back to the auth service, which is what makes it stateless and horizontally scalable."*

| Question | Answer |
|---|---|
| JWT parts | `header.payload.signature`, base64url-encoded, dot-separated |
| Is it encrypted? | 🔴 **No — it's signed, not encrypted.** Anyone can decode the payload. Never put secrets in it. |
| Why a short expiry + refresh token? | You can't revoke a JWT once issued, so you keep the access token short-lived (~15 min) and revoke at the refresh step |
| Where to store it in Angular? | `httpOnly` cookie is safest (XSS can't read it, but needs CSRF protection); `localStorage` is convenient and XSS-readable |
| Attaching it in Angular | An `HttpInterceptor` that adds the `Authorization: Bearer` header — 🔴 **you've built this, say so** |
| Spring Security basics | `SecurityFilterChain` bean, `authorizeHttpRequests`, a JWT filter before `UsernamePasswordAuthenticationFilter`, `@PreAuthorize("hasRole('ADMIN')")` for method-level |

---

# G — SQL: your live gap

🔴 **SQL appeared for the first time in six rounds — at Mphasis, at L1.** They asked a `GROUP BY` with a join. That thread is now open and L2 will pull it. This is the section to over-prepare.

> 🔗 **This section is the compact version. The full treatment — every join, the whole salary-question family, window functions, NULL traps, indexing, 18 predict-the-output programs and 15 drills — is now in [36 — SQL: The Complete Interview File](./36-sql-interview-questions.md).**

### G1. Joins — the diagram to have in your head

| Join | Returns |
|---|---|
| `INNER` | Only matching rows on both sides |
| `LEFT` | All of the left + matches (NULLs where none) |
| `RIGHT` | All of the right + matches |
| `FULL OUTER` | Everything from both (not in MySQL — emulate with `UNION`) |
| `CROSS` | Cartesian product |
| `SELF` | A table joined to itself — employee → manager |

```sql
-- Self join: every employee with their manager's name
SELECT e.emp_name AS employee, m.emp_name AS manager
FROM   employee e
LEFT JOIN employee m ON m.emp_id = e.manager_id;   -- LEFT, so the CEO still appears
```

### G2. The queries that actually get asked

```sql
-- ① Nth-highest salary (handles ties)
SELECT salary FROM (
  SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) rnk FROM employee
) t WHERE rnk = 2;

-- ② Highest-paid per department
SELECT * FROM (
  SELECT e.*, RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) rnk FROM employee e
) t WHERE rnk = 1;

-- ③ Find duplicates
SELECT email, COUNT(*) FROM users GROUP BY email HAVING COUNT(*) > 1;

-- ④ Delete duplicates, keep the lowest id
DELETE u1 FROM users u1 JOIN users u2 ON u1.email = u2.email AND u1.id > u2.id;

-- ⑤ Departments with NO employees
SELECT d.* FROM dept d LEFT JOIN employee e ON e.dept_id = d.dept_id WHERE e.emp_id IS NULL;

-- ⑥ Employees earning above their department average
SELECT emp_name, salary FROM (
  SELECT e.*, AVG(salary) OVER (PARTITION BY dept_id) avg_sal FROM employee e
) t WHERE salary > avg_sal;

-- ⑦ Running total
SELECT order_date, amount,
       SUM(amount) OVER (ORDER BY order_date ROWS UNBOUNDED PRECEDING) AS running_total
FROM orders;
```

### G3. `RANK` vs `DENSE_RANK` vs `ROW_NUMBER` — asked constantly

| salary | `ROW_NUMBER` | `RANK` | `DENSE_RANK` |
|---|---|---|---|
| 900 | 1 | 1 | 1 |
| 800 | 2 | **2** | **2** |
| 800 | 3 | **2** | **2** |
| 700 | 4 | **4** ← gap | **3** ← no gap |

### G4. Indexes — the natural follow-up to "make it faster"

> *"An index is usually a B-tree that lets the engine seek instead of scanning. The cost is slower writes and disk space, so you index what you filter, join and sort on — not everything."*

| Concept | Say this |
|---|---|
| Clustered vs non-clustered | Clustered *is* the table's physical order — one per table (the PK by default). Non-clustered is a separate structure pointing back. |
| Composite index order | 🔴 **Leftmost prefix rule** — an index on `(a, b)` helps `WHERE a` and `WHERE a AND b`, but **not** `WHERE b` alone. |
| Covering index | Contains every column the query needs, so it never touches the table — "index-only scan". |
| 🔴 When an index is ignored | A function on the column (`WHERE YEAR(created) = 2026`), a leading wildcard (`LIKE '%x'`), an implicit type cast, or low selectivity (a `gender` column). **Rewrite as a range: `WHERE created >= '2026-01-01' AND created < '2027-01-01'`.** |
| How do you know? | `EXPLAIN` / `EXPLAIN ANALYZE` — look for `type: ALL` (full scan) vs `ref`/`range`, and the `rows` estimate. |

### G5. ACID, and `DELETE` vs `TRUNCATE` vs `DROP`

**Atomicity · Consistency · Isolation · Durability** — be able to give a one-line example of each.

| | `DELETE` | `TRUNCATE` | `DROP` |
|---|---|---|---|
| Type | DML | DDL | DDL |
| `WHERE` | ✅ | ❌ | ❌ |
| Rollback | ✅ | ❌ (auto-commits) | ❌ |
| Resets AUTO_INCREMENT | ❌ | ✅ | — |
| Table survives | ✅ | ✅ | ❌ |

**Normalization:** 1NF atomic columns → 2NF no partial dependency on part of a composite key → 3NF no transitive dependency. *"And denormalise deliberately for read performance — a reporting table that duplicates a department name is a legitimate trade, not a mistake."*

---

# H — Live coding: what to expect

L1 made you write a thread-safe Singleton. **L2 will make you write something.** Type these, out loud, before the round.

| Problem | Where |
|---|---|
| Second-highest salary — SQL **and** Streams | [G2](#g2--the-queries-that-actually-get-asked) · [22](./22-java-streams-coding-problems.md) |
| Group employees by department / average salary | [B5](#b5-streams--what-theyll-ask-beyond-what-is-a-stream) |
| First non-repeating character in a string | [09](./09-coding-problems.md) |
| Reverse a string / check palindrome without library methods | [09](./09-coding-problems.md) |
| Count word/char frequency with a `Map` | [09](./09-coding-problems.md) |
| Remove duplicates from a list — with and without Streams | [22](./22-java-streams-coding-problems.md) |
| Find a missing number in 1..n | [09](./09-coding-problems.md) |
| Fibonacci / factorial, iterative + recursive | [09](./09-coding-problems.md) |
| Producer–consumer with `BlockingQueue` | [C8](#c8-coordination-utilities--producerconsumer) |
| Thread-safe counter / two threads incrementing | [C3](#c3-volatile-vs-synchronized-vs-atomic) · [C12](#c12-predict-the-output--multithreading) |
| Print odd/even numbers alternately with 2 threads | `wait`/`notifyAll` — [C2](#c2-synchronized--the-monitor) |
| Thread-safe Singleton *(already asked — they may ask you to extend it)* | [26 § Mphasis Q5](./26-companies-asked-questions.md) |

🔴 **How to behave during live coding — this matters as much as the code:**
1. **Restate the problem** and confirm one edge case before typing. ("Empty list — return empty or throw?")
2. **Narrate as you type.** Silence reads as being stuck.
3. **Brute force first, then improve.** A working O(n²) you then optimise beats a broken clever answer.
4. **State the complexity** unprompted at the end.
5. **Name one edge case you'd test**: null, empty, duplicates, single element.

---

# I — System design, lite

If L2 includes design, it will be a 10-minute conversation, not a whiteboard marathon. Use this skeleton and you'll never be lost:

```
① CLARIFY     — who uses it, how many, read-heavy or write-heavy? (60 seconds, always)
② API         — 3 or 4 endpoints. Makes it concrete immediately.
③ DATA MODEL  — the 3 main tables/entities and the key relationships.
④ HIGH LEVEL  — client → gateway → services → DB. Draw the boxes.
⑤ SCALE       — where's the bottleneck? cache / index / queue / replicas.
⑥ TRADE-OFFS  — what you chose NOT to do, and why.
```

**The one to rehearse: "design the reminder/notification system"** — because it's *your* feature, so you can't be caught out.

> *"Endpoints: create a reminder, list mine, cancel one. The table is `reminder(id, user_id, scheduled_at, type, status, payload)` with an index on `(status, scheduled_at)` since that's the only query the dispatcher makes. The naive version is a `@Scheduled` job polling every minute for `status = PENDING AND scheduled_at <= now()`."*
>
> *"That's fine at small scale, and it breaks in two predictable ways. **First, multiple instances** — every replica picks the same rows and the user gets three notifications. You fix it with `SELECT … FOR UPDATE SKIP LOCKED`, or ShedLock, or by moving to a queue. **Second, the polling itself** becomes a hot query as the table grows, which is why you'd move due reminders onto a delay queue and have the DB hold only the source of truth."*
>
> *"And delivery has to be **idempotent** — dedupe on `(reminder_id, channel)` — because at-least-once delivery is the only thing a queue actually promises."*

That answer demonstrates scale thinking, concurrency, indexing and idempotency in ninety seconds, all anchored in something you genuinely built.

---

# J — Angular, the way THEY will ask it

> 🎯 **HR's exact framing: "what you have used in recent projects."** So prepare **four narrations**, not a syllabus. Each one is: *what the feature was → what I used → why → what went wrong.*

### J1. RoboGebra — the personalized learning dashboard

> *"The dashboard shows a student's progress, streak, recommended study list and recent quiz results. It's an Angular + Ionic app. The page is a smart container component that calls a facade service; the individual cards are dumb presentational components taking `@Input`s, which made them easy to test and reuse. The four calls originally ran sequentially — I moved them into a `forkJoin` so they go in parallel, and later collapsed them into one aggregate endpoint on the Java side. State that several components needed — the current student profile — sits in a `BehaviorSubject` in a service, so any component subscribing gets the current value immediately rather than waiting for the next emission."*

**Follow-ups they'll ask, and your answers:**

| Follow-up | Answer |
|---|---|
| *"How do you unsubscribe?"* | *"`async` pipe in the template wherever possible — Angular handles it. Where I subscribe in TypeScript, `takeUntil(this.destroy$)` with a `Subject` completed in `ngOnDestroy`. In newer code, `takeUntilDestroyed()`. HTTP calls complete on their own, but a `BehaviorSubject` never does — that's the actual leak."* |
| *"What if one of the `forkJoin` calls fails?"* | 🔴 *"`forkJoin` fails the whole thing — you get nothing, not three out of four. So I add `catchError` on the individual streams to return a fallback, and the dashboard degrades gracefully instead of going blank."* |
| *"`Subject` vs `BehaviorSubject`?"* | *"`BehaviorSubject` holds a current value and replays it to new subscribers, and needs an initial value. `Subject` only emits to whoever is already listening. For shared state you want `BehaviorSubject`, because a component created later still needs the current user."* |
| *"Why `OnPush`?"* | *"Default change detection re-checks every binding on every event in the tree. `OnPush` only re-checks when an `@Input` reference changes, an event fires in the component, or an `async` pipe emits — which is why immutable updates matter. On the dashboard's list of cards that was a visible improvement."* |

### J2. The Angular 16 migration — four repositories

🔴 **This is your strongest Angular story, because almost nobody has done it.** It shows ownership, risk management and framework depth all at once.

> *"I led an upgrade of four repositories to Angular 16. The approach was version-by-version with `ng update`, never skipping — Angular only supports one major at a time and jumping introduces failures you can't attribute. For each repo: upgrade, run the build, run the tests, fix what broke, commit, then the next version. The hard part wasn't Angular itself — it was third-party libraries that hadn't released a compatible version, where I had to either wait, find a replacement, or pin. And RxJS 6→7 was its own migration, with `toPromise()` deprecated in favour of `firstValueFrom` and the older operator import paths gone."*

**Have ready:** what actually broke, and how you verified nothing regressed (*"the test suite plus a manual pass on the critical flows, and we shipped it repo by repo rather than all four at once so a rollback was small"*).

> 🔗 Full detail: **[angular-migration-project-interview-pack](../angular-migration-project-interview-pack/)**

### J3. EasyVisa — Formly and nested reactive forms

> *"The visa application forms were long, deeply nested and varied by visa type, so hard-coding each one wasn't viable. I used **Formly**, which builds the form from a JSON config, and wrote custom field types for the ones it didn't cover — an image cropper for document uploads, and a couple of domain-specific inputs. Underneath it's a reactive form: `FormGroup` containing nested `FormGroup`s and `FormArray`s for the repeated sections, so I could add and remove address blocks dynamically."*

| Follow-up | Answer |
|---|---|
| *"Reactive vs template-driven?"* | *"Reactive — the model is defined in TypeScript, so it's synchronous, unit-testable without the DOM, and supports dynamic controls and custom validators. Template-driven is fine for a login form; anything with conditional fields should be reactive."* |
| *"Custom validator?"* | *"A function returning `ValidationErrors | null`. For async — checking an ID against the server — an `AsyncValidatorFn` returning an Observable, with `debounceTime` so it doesn't fire on every keystroke."* |
| *"`FormArray`?"* | *"For a repeated group — a variable number of dependants or addresses. You push and remove `FormGroup`s at runtime and the form value stays a clean array."* |

### J4. Subsea — .NET + Kendo grid

> *"Schedule Manager, with .NET Core APIs and a Kendo UI grid on the Angular side — category and subcategory management with server-side paging and filtering on the grid."*

Keep this one short unless they follow up. **But mention .NET exists in your background** — Mphasis staffs both, and it can only help.

### J5. The small handful of Angular theory to still know

Even in a "basics not mandatory" round, these come up because they're one step from a project answer. **They were each asked in 2–4 previous rounds** — [see § most-repeated](./26-companies-asked-questions.md#-most-repeated-questions-across-all-companies).

| Topic | Have one sentence |
|---|---|
| Lifecycle hooks | asked in **4** rounds — `ngOnInit`, `ngOnChanges`, `ngOnDestroy`, `ngAfterViewInit` and when each fires |
| Component communication | asked in **4** rounds — `@Input`/`@Output`, service + `BehaviorSubject`, `@ViewChild`, route params |
| Observable vs Promise | lazy + multi-value + cancellable vs eager + single-value |
| `switchMap` vs `mergeMap` vs `concatMap` vs `exhaustMap` | cancel previous / all in parallel / queue in order / ignore while busy — **`switchMap` for search-as-you-type, `exhaustMap` for a submit button** |
| Lazy loading | `loadChildren` / `loadComponent` — smaller initial bundle; the cost is a delay on first navigation |
| Standalone components | Angular 14+, no NgModule — the default in 17+. Relevant because you migrated to 16 |
| Signals | Angular 16 introduced them — know what they are and that they're an alternative to `Zone.js`-based change detection |
| Interceptors | JWT attachment, error handling, loaders — you've built this |

> 🔗 **[04 — Angular](./04-angular.md)** · **[20 — RxJS Operators](./20-rxjs-operators.md)** · **[25 — Angular Binding & Forms](./25-angular-binding-forms.md)**

---

# K — The client technical round

🔴 **This is a different round with a different objective. Most candidates prepare for it as "another tech round" and that's the mistake.**

### K1. Who's in the room and what they're deciding

| | Mphasis L2 | **Client round** |
|---|---|---|
| Interviewer | Mphasis senior engineer / architect | **The end customer's tech lead or manager** |
| Deciding | "Is she technically good enough to hire?" | **"Do I want this person on my project on Monday?"** |
| Style | Structured Q&A, depth probes | Conversational, scenario-led, project-focused |
| Weight on theory | High | **Low** |
| Weight on "have you done this" | Medium | 🔴 **Very high** |
| What sinks you | Not knowing | **Vagueness about your own work** |

> 💡 **The mental switch:** L2 asks *"do you know Spring?"*. The client asks *"if I give you a ticket on Monday, will it come back done?"* Every answer should demonstrate **ownership, communication and independence** — not just knowledge.

### K2. The questions they will actually ask

> 📌 **Corroborated against reported Mphasis client-round questions** — the round splits into three buckets, and candidates report the same shapes repeatedly:
>
> | Bucket | What it's testing |
> |---|---|
> | **A — Project & resume deep dive** | Is the resume real? Architecture end to end, your contribution vs the team's, bottlenecks you solved, what you'd redesign |
> | **B — Technical & scenario** | Structured troubleshooting of a live production issue, advanced SQL, framework internals for your stack, handling tight deadlines and changing requirements |
> | **C — Client interaction & delivery** | Communicating blockers to non-technical stakeholders, conflicting priorities between the vendor's process and the client's asks |
>
> 🔴 **Bucket C is the one candidates don't prepare, and it's the one that's specific to a client round.** Bucket A and B they'd get in any interview; C is why this round exists.

---

## 🅰️ Bucket A — project & resume deep dive

**① "Walk me through your current project / your project architecture end to end."** — 🔴 **the most important two minutes of the round.**

Structure it as: **what the product does → the architecture → what *you* own → one hard thing you solved.**

> *"RoboGebra is an AI-driven math learning platform — students work through problems and get step-by-step explanations, in Tamil and English. The client is Angular and Ionic so it runs as a web app and on mobile; the backend is Java with Spring Boot and MySQL, split into services, with an AI explanation engine behind its own service. I own the learner-facing side end to end — the personalized dashboard, the study list and quiz modules, and the study reminder system, which means both the Angular components and the REST APIs behind them."*
>
> *"The hardest piece was the real-time computation and visualization — as the student edits an expression, the graph updates instantly while the AI explanation is fetched remotely. I split it: local computation renders immediately, and the remote call is debounced and `switchMap`ped so an in-flight request is cancelled when the input changes again. Without that we were flooding the AI backend with a request per keystroke."*

🔴 **Have the architecture as a drawable picture, because "end to end" means they want the boxes:**

```
  Ionic / Angular client  ──HTTPS──►  API layer (Spring Boot, JWT-secured)
                                          │
                        ┌─────────────────┼──────────────────┐
                        ▼                 ▼                  ▼
                  Content / Quiz     Reminders          AI Explanation
                        │                 │                  │
                        └──────► MySQL ◄──┘            (external model API)
                                                    scheduled dispatch ──► notifications
```
Then say **where you sit on that diagram**: *"I own the learner-facing path — the Angular components and the APIs behind the dashboard, study list, quiz and reminders."*

---

**② "What was your specific contribution vs the team's?"** — 🔴 **they are explicitly checking for resume inflation.** Be precise and volunteer the boundary. *"I owned the dashboard, study list, quiz and reminder modules end to end — Angular and the Spring Boot APIs. The AI explanation engine itself was another developer's; I integrated against it and built the bilingual rendering on the client."*

> **Honesty here buys more credibility than a bigger claim.** The interviewer has seen candidates claim whole architectures; a clean, confident boundary is rare and it reads as senior.

---

**③ "What challenges or bottlenecks did you face, and how did you solve them?"** — have **two** ready, one performance and one behavioural/logic.

| | Story |
|---|---|
| **Performance** | The dashboard's four sequential calls + an N+1 on the study list → aggregate endpoint, `forkJoin`, `JOIN FETCH`. ~2s → under 0.5s. → [A10](#a10--give-me-a-number--api-performance) |
| **Design under a constraint** | Real-time computation + AI explanation: instant local compute, then a `debounceTime` + `switchMap` remote call so an in-flight request is cancelled on the next keystroke — otherwise we flooded the AI backend with one request per character |

**Structure each one:** *what was wrong → how I knew → what I tried → what actually fixed it → the measurable result.*

---

**④ 🔴 "If you could redesign that module differently, what approach would you take?"**

This question is a **maturity test**, and it's the one people fumble — they either defend the original design (reads as inflexible) or trash it (reads as if they didn't think it through the first time). **The right answer does three things: name a real limitation, propose a concrete alternative, and say what the original constraint was.**

> *"Two things. The reminder dispatch is a `@Scheduled` job polling the table, which was the right call for the scale and timeline we had — but it doesn't survive running on multiple instances cleanly, and the polling query gets hot as the table grows. I'd move due reminders onto a **delay queue** and keep the database purely as the source of truth, so the dispatcher isn't polling at all and horizontal scaling is free rather than something I have to defend with a lock."*
>
> *"The second is that the learner APIs grew feature by feature, so the dashboard needed four calls before I aggregated them. If I started again I'd design the **client's screen contract first** and shape the endpoints to it — effectively a BFF — instead of exposing entity-shaped endpoints and stitching them together on the client."*
>
> *"What I wouldn't change is keeping the AI explanation behind its own service. That boundary earned itself — it fails differently and scales differently from everything else."*

🔴 **That last sentence is the one that lands.** Naming something you'd *keep*, with a reason, proves you're evaluating rather than just apologising.

---

## 🅱️ Bucket B — technical & scenario

**⑤ 🔴 "Walk me through your structured approach to troubleshooting a live production issue."**

They want a **process**, not an anecdote. Answer in named steps — it's the single clearest signal of someone who has actually been on a production call.

| Step | What you say |
|---|---|
| **1. Assess impact** | *"How many users, which flow, is it total or degraded? That decides urgency and who needs to know."* |
| **2. Communicate immediately** | *"A short factual update to the client/lead before I start digging — 'checkout is failing for ~10% of users, investigating, next update in 30 minutes.' Silence is what damages trust, not the bug."* |
| **3. Mitigate before diagnosing** | 🔴 *"Restore service first — roll back the release, disable the feature flag, scale out. Root cause can wait; the outage can't."* |
| **4. Narrow it down** | *"What changed? Last deploy, config change, data volume, an upstream dependency. Then logs, the trace id across services, `/actuator/health`, DB slow query log, thread dump if it's hung."* |
| **5. Fix and verify** | *"Fix, verify in a lower environment where possible, deploy, and confirm with the same metric that showed the problem."* |
| **6. Prevent recurrence** | *"A regression test and, usually more importantly, the alert that would have caught it before the client did. Then a short RCA write-up."* |

> 💡 **The line to close on:** *"The order matters more than the tooling — communicate, mitigate, then diagnose. The instinct is to jump straight to the interesting technical problem, and that's the mistake."*

**Pair it with a real story** — [C0's](#-asked-at-l1-what-is-deadlock-in-multithreading) duplicate-reminder incident works: symptom (users got the same reminder three times), first hypothesis, actual cause (the scheduler firing on every replica), the fix (atomic claim + idempotent send), and the prevention (an alert on duplicate sends per user per day).

---

**⑥ "Deep questions on your core stack."** Expect the client's own priorities here — **advanced SQL** ([Part G](#g--sql-your-live-gap)), **framework internals** ([Parts D](#d--level-2-spring--spring-boot-depth), [E](#e--level-2-jpa--hibernate)), CI/CD and cloud if the JD mentions them. Know your AWS/Azure honestly: EC2, S3, Lambda, RDS, CloudWatch — and say where the boundary of your experience is rather than being caught at it.

---

**⑦ 🔴 "You're on a tight deadline / the client changes requirements mid-sprint. What do you do?"**

The wrong answers: *"I work extra hours"* (unsustainable, and it tells them you'll hide problems) and *"I refuse, it's out of scope"* (unworkable for a services engagement).

> *"First I'd understand what's actually driving the change and how urgent it really is — sometimes there's a demo date behind it and only part of it is needed by then. Then I'd make the trade-off explicit rather than silently absorbing it: 'this is roughly three days; if it comes into this sprint, either X moves out or we go a few days beyond — which do you prefer?' That's a decision for the lead and the client to make, not for me to make quietly by working weekends."*
>
> *"If it genuinely can't move, I'd look for a smaller version that delivers the value now — the core path working, the edge cases in the next sprint — and be explicit that it's a first cut, not the finished thing. What I won't do is silently cut testing and let it surface as a defect later, because that costs more than the delay would have."*

🔴 **The principle to name:** *"Scope, time, quality — you can't hold all three. My job is to make sure the person who owns the priority is the one choosing which one gives."*

---

## 🅲 Bucket C — client interaction & delivery *(the one candidates skip)*

**⑧ 🔴 "How do you communicate a technical blocker or delay to a non-technical stakeholder?"**

> *"Three things, in this order: **impact, then cause in plain language, then options with a recommendation.** I lead with what it means for them, not the technical detail. So — 'the reports feature won't be ready for Thursday's demo' first, then 'the third-party data feed is returning a different format than documented', then 'we can either demo with sample data on Thursday and go live Monday, or push the demo — I'd suggest the first'."*
>
> *"And I raise it **as soon as I know it's a risk**, not when it becomes a fact. A stakeholder can plan around a delay they hear about a week early; they can't do anything with one they hear about the morning it's due. No jargon, no blame, and always at least one option — arriving with only a problem puts the whole thing on them."*

| ❌ | ✅ |
|---|---|
| *"The API is throwing a 500 because of a serialization issue in the DTO mapper."* | *"The reports page won't load — an integration with their data feed is failing. Fix is about a day."* |
| Reporting it on the due date | Flagging it as a **risk** the moment you see it |
| Problem with no options | Two options and a recommendation |
| *"The other team broke it"* | *"Here's where it's blocked and what I need to unblock it"* |

---

**⑨ 🔴 "How do you handle conflicting priorities between internal Mphasis guidelines/process and a direct client request?"**

This is a **loyalty and judgment test**, and it's specific to working at a services company. They want to know you won't go rogue for the client, and won't hide behind process either.

> *"My default is that the client's priority is the priority — that's the point of the engagement. But I wouldn't just quietly bypass our process, because the process usually exists for a reason someone else owns — code review, security review, the release calendar."*
>
> *"So practically: I'd take the request seriously and immediately, and if honouring it conflicts with something internal, I raise it with my Mphasis lead the same day rather than deciding on my own. Most of the time it's not a real conflict once someone with the full picture looks at it — an approval gets expedited, or the client's need is met a slightly different way."*
>
> *"The one place I'd hold firm is anything touching security, data handling or a compliance control, where 'the client asked for it directly' isn't enough on its own — that has to go through the right approval. Everything else I'd treat as a scheduling conversation, not a standoff."*

🔴 **The sentence that answers what they're really asking:** *"What I wouldn't do is either of the two failure modes — go around my own organisation to please the client, or hide behind process and tell the client no. Both of those end up on my lead's desk anyway, just later and worse."*

---

**⑩ "Have you worked directly with clients or onshore teams?"** → say yes if you have, with a specific example — a demo you ran, a requirement you clarified directly, a standup with an onshore team. **Mphasis is a services company: the client is buying communication as much as code**, and this round exists because someone got burned by an engineer who couldn't do that part.

If your client exposure has been limited, say so honestly and pivot to evidence of the underlying skill: *"Most client contact went through our lead, but I've presented my modules in sprint demos and worked directly with the BA on requirements, so I'm comfortable being the person explaining the work."*

---

**⑪ "What do you do when you disagree with a technical decision?"** → *"Make the case once, with reasoning and preferably a small proof — then commit to the decision either way. What I won't do is quietly build it my way, or agree in the meeting and complain afterwards."*

---

**⑫ "How do you approach a ticket you don't understand?"** → *"Read the ticket and the surrounding code first and try to reproduce it, so that when I do ask, I'm asking something specific rather than 'I don't understand this'. I'd rather spend twenty minutes clarifying than two days building the wrong thing."*

---

**⑬ "How do you estimate?"** → *"Break it into the pieces I actually know — API, component, tests, integration — estimate each, and add buffer for the unknowns and for review and QA feedback. And I re-check against the estimate mid-way; if it's slipping I raise it then. Nobody minds an estimate being wrong. They mind finding out on the due date."*

---

**⑭ "Tell me about your agile process."** → sprint length, ceremonies (standup, planning, grooming, retro), Jira, story points, definition of done, PR review on Bitbucket/GitHub. 🔴 **Have your real numbers**: team size, sprint length, release cadence. Vagueness here reads as not having been close to the process.

---

**⑮ "Are you comfortable with our stack / picking something new up?"** → yes, with **evidence rather than enthusiasm**: *"I've moved between Angular+Grails, Angular+.NET and Angular+Java across three projects, and I led an Angular version migration across four repositories. Picking up a new stack is something I've actually done, not just something I'm willing to do."*

---

**⑯ "How do you write tests?"** → be honest and specific. Jasmine/Karma for Angular components (you wrote these on EasyVisa), JUnit + Mockito for services. *"I mock the repository and assert behaviour, and I make a point of testing the edge cases — nulls, empty collections, the error path — because that's where the bugs actually are."*

---

**⑰ "Where do you see yourself / why this role?"** → growth, scale, enterprise exposure. **Name the client's domain if you know it** — *"and this is a much larger platform than I've worked on, which is exactly the exposure I'm looking for."*

### K3. Red flags to avoid — each one has sunk a client round

| ❌ Don't say | ✅ Say instead |
|---|---|
| *"I only did the frontend part."* | *"I owned the frontend and the APIs behind it; the AI engine was another team's."* |
| *"My manager assigned it, I just did it."* | *"I picked it up in planning and owned it through to release."* |
| *"We used microservices"* (with no detail) | Name the services and why they're split that way. |
| Badmouthing your current company | *"I've learnt a lot; I'm looking for larger scale."* |
| *"I don't know"* and stopping | *"I haven't used that directly. What I have done that's closest is… and I'd approach it by…"* |
| Vague numbers | *"Team of six, two-week sprints, released every sprint."* |
| *"I'd just put in extra hours to hit the date."* | *"I'd make the trade-off explicit and let the lead choose what moves."* |
| *"The client asked, so I'd just do it."* | *"I'd take it seriously and raise the process conflict with my lead the same day."* |
| Explaining a blocker in jargon | Impact first, in their language, with two options |
| Defending your design as flawless | Name one real limitation — and one thing you'd deliberately keep |

### K4. 🔴 Questions YOU ask — never skip this

Not asking questions reads as not being interested. Have four ready; ask two or three.

1. *"What does the team look like, and where would I fit in it?"*
2. *"What's the current state of the codebase — greenfield, or maintaining and extending something existing?"*
3. *"What would you want me to have accomplished in the first three months?"*
4. *"What's the biggest technical challenge the team is facing right now?"*
5. *"How do the onshore and offshore teams work together day to day?"*

> 💡 Question 4 is the best one — the answer tells you exactly what they need, and asking it makes you sound like someone thinking about the *work*, not the *job*.

---

# L — Logistics & fit questions

Have these answers ready and consistent — HR will cross-check them against what you told them earlier.

| Question | The approach |
|---|---|
| **Notice period** | State it exactly. If negotiable, say by how much. Don't guess. |
| **Why are you leaving?** | Growth and scale. Never money first, never negative about Provility. |
| **Current & expected CTC** | Know your number before the call. Give a range with a reason ("based on my experience and the market for full-stack Angular/Java"). |
| **Location / onsite days** | Know the client's location and be clear about what you can do. |
| **Any other offers in progress?** | Honest but brief. It's leverage, not a threat. |
| **Total experience** | 4+ years, since Oct 2021 at Provility. Consistent everywhere. |
| **Are you okay with a support/maintenance project?** | Say yes if you are — many client projects are. *"I'd want some feature work too, but I'm comfortable with production support."* |

---

# M — Your 48-hour plan

> Adjust to the time you actually have. **Do it in this order — it's sorted by return on effort.**

### Day 1 — the re-probes and the gaps

| Time | What |
|---|---|
| 90 min | 🔴 **[Part A](#a--level-2-the-re-probes-of-your-l1-answers)** — the 10 re-probes. Say every answer **out loud**. This is the highest-yield block in the file. |
| 60 min | 🔴 **[Part G — SQL](#g--sql-your-live-gap)**. Type the 7 queries. It's your newest gap and they've already asked once. |
| 60 min | **[Part D4 — `@Transactional`](#d4--transactional--the-most-re-probed-spring-topic)** + **[Part E1 — N+1](#e1--the-n1-problem--the-single-most-asked-orm-question)**. The two most-asked Spring/JPA topics anywhere. |
| 45 min | **[Part B1–B2](#b1--hashmap-internals--the-most-asked-l2-java-question-anywhere)** — HashMap internals, collections table. |
| 30 min | Rehearse **[K2 ① — "walk me through your project"](#k2-the-questions-they-will-actually-ask)** until it's smooth. Time it. Aim for 2 minutes. |

### Day 2 — coding, Angular, and the client round

| Time | What |
|---|---|
| 60 min | **Type** 5 problems from **[Part H](#h--live-coding-what-to-expect)**. In an IDE, no autocomplete help, out loud. |
| 60 min | 🔴 **[Part C](#c--level-2-multithreading--concurrency)** — multithreading. Start with **[C0](#c0--the-two-questions-they-already-asked-answered-deeper)** (the two they already asked) and **[C10](#c10-multithreading-inside-spring-boot--the-question-they-will-actually-ask)** (threading in Spring), then the [C13 rapid-fire](#c13-rapid-fire). |
| 45 min | **[Part J](#j--angular-the-way-they-will-ask-it)** — narrate all four project stories out loud. Not read — *narrate*. |
| 60 min | 🔴 **[Part K](#k--the-client-technical-round)** — the client round. Rehearse ① the architecture walkthrough, ④ what you'd redesign, ⑤ the production-troubleshooting process, and **the whole of [Bucket C](#-bucket-c--client-interaction--delivery-the-one-candidates-skip)** — the delivery/communication questions are the ones candidates don't prepare. Pick your 3 questions to ask. |
| 30 min | 15 problems from **[23 — Predict the Output](./23-java-output-tricky-questions.md)**. |
| 20 min | **[Part L](#l--logistics--fit-questions)** — get your numbers straight. |

### The morning of

Read only **[Part N](#n--one-page-cheat-sheet)** and **[Part A's table](#a--level-2-the-re-probes-of-your-l1-answers)**. Nothing new. New material on the day creates doubt, not knowledge.

---

# N — One-page cheat sheet

### 🎯 The three rules

1. **Three sentences, always.** Definition → *something from my project* → the trade-off or failure mode. This is the exact gap the [pattern analysis](./26-companies-asked-questions.md#-what-the-pattern-says-across-5-rejections) says has been losing you rounds — the answers were right, the second sentence was missing.
2. **Everything routes back to RoboGebra.** They can't check your theory against reality, but they can hear whether you've done it.
3. **"I haven't used that" is a complete, safe answer** — as long as it's followed by *"the closest I've done is…"*. Bluffing is what actually fails a round.

### ⚡ The facts to have instant

| | |
|---|---|
| Circuit breaker | Resilience4j + 🔴 `spring-boot-starter-aop`. CLOSED → OPEN → HALF_OPEN |
| Thread-safe singleton | Bill Pugh static holder. DCL needs `volatile` (reordering → partially constructed object) |
| Spring singleton ≠ Java singleton | Per-**container**, not per-JVM. Mutable fields on a bean = race condition |
| Deadlock | 4 Coffman conditions; fix = **global lock ordering** or `tryLock` with timeout |
| Diagnose a hang | 3 thread dumps 10s apart; 0% CPU = locks/pool, 100% = GC or loop |
| HashMap | 16 / 0.75 / treeify at 8 with table ≥ 64. Mutable key = lost entry |
| `equals`/`hashCode` | Equal objects **must** have equal hashes; the converse isn't required |
| `@Transactional` traps | Self-invocation · rolls back only on RuntimeException · private/final methods |
| N+1 | `JOIN FETCH` / `@EntityGraph` / `@BatchSize`. ⚠️ `JOIN FETCH` + `Pageable` pages in memory |
| Constructor injection | `final` fields · fails fast at startup · testable with plain `new` |
| SQL `LEFT JOIN` + `COUNT` | 🔴 `COUNT(column)` not `COUNT(*)` — otherwise an empty department returns 1 |
| `WHERE` vs `HAVING` | Rows before grouping vs groups after aggregation |
| `RANK` vs `DENSE_RANK` | Gaps after a tie vs no gaps |
| Index ignored when | Function on the column · leading `%` · implicit cast · low selectivity |
| `volatile` | Visibility only — `count++` is still broken. Use `AtomicInteger` |
| `forkJoin` fails | 🔴 One error kills all of them → `catchError` per stream |
| Unsubscribe | `async` pipe · `takeUntil(destroy$)` · `takeUntilDestroyed()` |
| JWT | Signed, **not encrypted** — never put secrets in the payload |

### 🔵 Client round — the five answers to have ready

| Question | The shape of the answer |
|---|---|
| Architecture end to end | Draw the boxes → then say **where you sit on the diagram** |
| Your contribution vs the team's | Volunteer the **boundary**. "I owned X and Y; Z was another developer's" |
| What would you redesign? | One real limitation + a concrete alternative + **one thing you'd keep, and why** |
| Production issue — your process | 🔴 **Assess → communicate → mitigate → diagnose → fix → prevent.** In that order |
| Blocker to a non-technical stakeholder | **Impact → plain-language cause → two options + a recommendation.** Raise it as a *risk*, early |
| Client request vs internal process | Client's priority is the priority, but **escalate to your lead the same day** rather than going around either side. Hold firm only on security/compliance |

### 🗣️ Lines worth having word-perfect

- *"You can always split a well-structured monolith; you can't easily un-split a distributed system."*
- *"It's proxy-based, so anything that bypasses the proxy silently disables it — and silent is what makes it dangerous."*
- *"`volatile` fixes visibility, not atomicity."*
- *"A compensating transaction isn't a rollback — it's a new business transaction that undoes the previous one."*
- *"I owned that feature end to end — the Angular components and the APIs behind them."*
- *"Communicate, mitigate, then diagnose. The instinct is to jump straight to the interesting technical problem, and that's the mistake."*
- *"Scope, time, quality — you can't hold all three. My job is to make sure the person who owns the priority is the one choosing which one gives."*
- *"What I wouldn't do is go around my own organisation to please the client, or hide behind process and tell the client no."*
- *"I haven't used that directly. The closest I've done is ___, and I'd approach it by ___."*

### 🚫 The five things not to do

1. Don't answer in one sentence and stop.
2. Don't claim anything on your resume you can't take three follow-ups on.
3. Don't go silent while coding — narrate.
4. Don't say *"we"* when you mean *"I"*, or *"I"* when you mean *"we"*.
5. Don't skip asking them questions at the end.

---

> ✅ **You reached round two.** In six companies that hasn't happened before — the L1 answers were good enough. The work now is depth on the follow-up, which is the one thing the [pattern analysis](./26-companies-asked-questions.md#-what-the-pattern-says-across-5-rejections) has been pointing at all along.
>
> 📝 **After each round, log every question in [26](./26-companies-asked-questions.md) the same day** — append to the Mphasis section, don't start a new file.
