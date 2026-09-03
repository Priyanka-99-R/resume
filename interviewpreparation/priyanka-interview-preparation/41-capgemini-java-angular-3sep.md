# 🟦 Capgemini — Java + Spring + Angular round (Sept 2026)

> 📅 **Attended: early Sept 2026** *(edit the exact date here — the file is named `3sep` as a placeholder)*. 🟡 **Result pending.**
>
> 📝 **You walked out with all 10 questions but did NOT write the last program.** That program is **Q10** below, solved four ways with the Stream API — **type it, don't read it.** It is a twist on *"first non-repeating character"*, which **Altimetrik already asked you** ([26 §Altimetrik Q3](./26-companies-asked-questions.md#3-first-non-repeating-character)). That family is now on its **2nd round**.
>
> **Shape of the round:** ~10 questions — **2 Java core → 4 Spring / architecture → 3 Angular → 1 Java Streams live-coding.**
> **The signal:** this was a **breadth sweep**, not a depth round. Nothing here goes below the API you use daily — but the last question was live coding, and **that is where the round is decided**.

---

## 📋 The 10 questions at a glance

| # | Question exactly as asked | Area | Answered below |
|---|---|---|---|
| 1 | **Java 8 new features** | Java ⭐ | [→](#1-java-8-new-features) |
| 2 | **HashMap and LinkedHashMap** | Collections ⭐ | [→](#2-hashmap-vs-linkedhashmap-and-treemap) |
| 3 | **Setter injection and constructor injection** | Spring DI ⭐ | [→](#3-setter-injection-vs-constructor-injection) |
| 4 | **`@Component` and `@Bean`** | Spring ⭐ | [→](#4-component-vs-bean) |
| 5 | **How do microservices load values?** | Spring Boot config | [→](#5-how-do-microservices-load-values) |
| 6 | **Monolithic and microservices** | Architecture ⭐ | [→](#6-monolithic-vs-microservices) |
| 7 | **Components in Angular** | Angular ⭐ | [→](#7-components-in-angular) |
| 8 | **How do you create a new component?** | Angular CLI | [→](#8-how-do-you-create-a-new-component) |
| 9 | **`ngOnChanges` and `ngDoCheck`** | Angular ⭐ | [→](#9-ngonchanges-vs-ngdocheck) |
| 10 | 🔴 `String name = "Priyanka"` — **second non-repeating character from the END**, using Stream API | Java Streams 🔴 | [→](#10--live-coding--second-non-repeating-character-from-the-end) |

⭐ = already asked in an earlier round (repeat). 🔴 = live coding. **7 of the 10 are repeats** — the bank really is ~25 questions.

---

# Part 1 — Java core

## 1. Java 8 new features

**One line:** Java 8 is the release where Java became **functional** — lambdas let you pass behaviour as a value, and everything else (streams, method references, `Optional`, default methods) exists to make that useful.

### The list — say it in five groups, not as 15 bullets

| Group | What's in it |
|---|---|
| **1. Lambdas & functional interfaces** ⭐ | `(a, b) -> a + b` · `@FunctionalInterface` (exactly one abstract method) · the built-in set: `Function`, `Predicate`, `Consumer`, `Supplier`, `BiFunction`, `UnaryOperator` |
| **2. Stream API** ⭐ | `filter`/`map`/`sorted`/`distinct`/`limit` (intermediate, **lazy**) → `collect`/`forEach`/`reduce`/`count`/`anyMatch` (terminal, **eager**) · `Collectors.groupingBy`/`toMap`/`joining`/`partitioningBy` · `parallelStream()` |
| **3. Interface changes** | **`default` methods** (add a method without breaking every implementer — this is how `Collection.stream()` was added) and **`static` methods** in interfaces |
| **4. `Optional`** | A container that is either present or empty — `of`/`ofNullable`/`empty`, `map`, `filter`, `orElse`/`orElseGet`/`orElseThrow`. **A return type, not a field type** |
| **5. New Date/Time API** (`java.time`) ⭐ | `LocalDate`, `LocalTime`, `LocalDateTime`, `ZonedDateTime`, `Duration`, `Period`, `DateTimeFormatter` — **immutable and thread-safe**, unlike `Date`/`Calendar`/`SimpleDateFormat` |

**The smaller ones worth naming if they push:** method references (`Employee::getName`), `CompletableFuture` (async composition), `StringJoiner`, `Arrays.parallelSort`, `Map` helpers (`getOrDefault`, `computeIfAbsent`, `merge`, `forEach`), repeatable annotations, and **PermGen → Metaspace** (a JVM change, not a language one).

```java
// the whole release in one snippet
List<String> names = employees.stream()                       // Stream API
        .filter(e -> e.getSalary() > 50_000)                  // lambda + Predicate
        .map(Employee::getName)                               // method reference
        .sorted()
        .collect(Collectors.toList());

Optional<Employee> top = employees.stream()                   // Optional
        .max(Comparator.comparingDouble(Employee::getSalary));

String name = top.map(Employee::getName).orElse("none");      // no null check

LocalDate joined = LocalDate.of(2021, 6, 1);                  // java.time
long years = ChronoUnit.YEARS.between(joined, LocalDate.now());
```

### Why it matters — the sentence that upgrades the answer

> 🗣️ "Before 8, you said *how* to loop. After 8, you say *what* you want and the library decides how — which is what makes `parallelStream()` a one-word change instead of a rewrite. **Default methods are the quiet hero**: without them, adding `stream()` to `Collection` would have broken every implementation of `List` on earth."

### ⚠️ The three traps

1. **Streams are lazy.** `list.stream().filter(...)` with no terminal operation does **nothing** — not one element is touched.
2. **A stream is single-use.** Re-using one → `IllegalStateException: stream has already been operated upon or closed`.
3. **`parallelStream()` is not free.** It uses the shared **common ForkJoinPool**; for small collections, or inside a web request already running on a thread pool, it is usually **slower**. Say *"I reach for it only for large, CPU-bound, stateless work"*.

> 🧠 **Easy memory box — Java 8 = "L.S.I.O.D."**
> **L**ambdas · **S**treams · **I**nterface default/static methods · **O**ptional · **D**ate-Time API.

🔗 Deeper: **[05 — Core Java](./05-java.md)** · **[22 — Java Streams: 20 coding problems](./22-java-streams-coding-problems.md)** · follow-up they often ask next: **[12 — Java 17](./12-java17-features.md)** and **[40 — Java 21](./40-java21-features.md)**.

---

## 2. HashMap vs LinkedHashMap (and TreeMap)

> They said *"HashMap and LinkedHashMap"* — answer it as **one family with one difference**, then let them pull you deeper.

**One line:** all three are `Map`s; they differ **only in iteration order** and what that order costs you.

```
HashMap        →  NO order guarantee        →  O(1) get/put   →  fastest, least memory
LinkedHashMap  →  INSERTION order (or access order)  →  O(1)   →  + 2 pointers per entry
TreeMap        →  SORTED by key             →  O(log n)       →  Red-Black tree, key must be Comparable
```

### The comparison table

| | `HashMap` | `LinkedHashMap` | `TreeMap` |
|---|---|---|---|
| **Order** | None (looks random, and **changes on resize**) | **Insertion order** by default; **access order** if `accessOrder=true` | **Sorted** by natural order or a `Comparator` |
| **Structure** | Array of buckets + linked list → **tree at 8** | HashMap **+ a doubly-linked list** through all entries | Red-Black tree |
| **get / put** | O(1) average | O(1) average | O(log n) |
| **null key** | ✅ one | ✅ one | ❌ `NullPointerException` |
| **Memory** | Lowest | +2 references (`before`/`after`) per entry | Node overhead |
| **Thread-safe** | ❌ | ❌ | ❌ (use `ConcurrentHashMap` / `ConcurrentSkipListMap`) |

### How `HashMap` actually works (the follow-up, every time)

```
put("Priyanka", 1)
   │
   ├─ hash = key.hashCode() ^ (h >>> 16)      // spread the high bits
   ├─ index = hash & (n - 1)                  // n is a POWER OF 2 → cheap modulo
   │
   └─ bucket[index]
         ├─ empty        → put the node here
         ├─ same key     → replace the value  (equals() decides)
         └─ collision    → append to the linked list
                             └─ list length ≥ 8 AND table ≥ 64 → TREEIFY (Red-Black tree, O(log n))
                                (if table < 64 it RESIZES instead of treeifying)

Default capacity 16 · load factor 0.75 → resize (double) at 12 entries · resize rehashes everything
```

### `LinkedHashMap` — the one thing that makes it special

It is literally `HashMap` **plus** a doubly-linked list stitched through the entries, so iteration is a walk down that list:

```java
Map<Character, Integer> m = new LinkedHashMap<>();
m.put('P', 1); m.put('r', 2); m.put('i', 3);
// iteration → P, r, i   ALWAYS. A HashMap gives no such promise.
```

**And the killer use case — an LRU cache in 6 lines:**

```java
class LruCache<K, V> extends LinkedHashMap<K, V> {
    private final int max;
    LruCache(int max) { super(16, 0.75f, true); this.max = max; }   // true = ACCESS order
    @Override protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > max;                                        // evict least-recently-used
    }
}
```

> 🗣️ **The sentence that lands it:** "I use `HashMap` by default. I switch to `LinkedHashMap` the moment **order is part of the answer** — building a response the UI renders in order, or counting characters where I have to report the *first* one. And **Q10 in this round is exactly that case**: a `HashMap` would have scrambled the character order and broken the answer."

### ⚠️ The traps worth volunteering

1. **A mutable key is a lost entry.** Put an object in, change a field used by `hashCode()`, and `get()` looks in the wrong bucket. Keys should be **immutable** — `String`, boxed types, records.
2. **`equals`/`hashCode` come as a pair.** Override one without the other and lookups silently fail.
3. **`HashMap` under concurrency** — in Java 7 a resize could create an **infinite loop**; since 8 it "only" loses data. Either way: `ConcurrentHashMap`.
4. **Treeify needs `Comparable`-ish keys.** Without a natural order it falls back to comparing class names/identity hashes — still O(log n), but the point is it isn't magic.

🔗 Full depth (asked at Virtusa R2 and Altimetrik): **[31 — HashMap Internals](./31-hashmap-internals.md)**.

---

# Part 2 — Spring & architecture

## 3. Setter injection vs constructor injection

**One line:** **constructor injection = the dependency is mandatory**; **setter injection = the dependency is optional or replaceable**. Spring's own docs recommend **constructor injection** for almost everything.

```java
// ✅ CONSTRUCTOR INJECTION — the default you should be using
@Service
public class OrderService {
    private final PaymentClient payment;      // final = can never be null, never reassigned
    private final OrderRepository repo;

    // @Autowired is OPTIONAL since Spring 4.3 when there is exactly ONE constructor
    public OrderService(PaymentClient payment, OrderRepository repo) {
        this.payment = payment;
        this.repo = repo;
    }
}

// SETTER INJECTION — for genuinely optional dependencies
@Service
public class ReportService {
    private Notifier notifier = new NoOpNotifier();          // sensible default

    @Autowired(required = false)
    public void setNotifier(Notifier notifier) { this.notifier = notifier; }
}
```

### The comparison

| | **Constructor** ✅ | **Setter** |
|---|---|---|
| Dependency is… | **Mandatory** | **Optional / changeable** |
| Fields can be `final` | ✅ → **immutable, thread-safe** | ❌ |
| Missing bean fails… | **At startup** (fail fast) ⭐ | At startup too, unless `required=false` — but the object exists half-built |
| Object is always valid | ✅ You cannot construct it wrong | ❌ Valid only *after* every setter has run |
| Testing without Spring | ✅ `new OrderService(mockA, mockB)` | Needs `new` + n setter calls |
| **Circular dependency** | 💥 Fails loudly at startup ⭐ | Silently "works" — it hides a design smell |
| Too many dependencies | Painfully obvious → tells you the class does too much ⭐ | Easy to hide 12 of them |

### The third one they may name: **field injection**

```java
@Autowired private PaymentClient payment;   // ❌ don't
```
**Why it's discouraged:** the field can't be `final`; the dependency is invisible from the outside (nothing in the public API says the class needs it); you cannot construct the object in a plain unit test without reflection; and it makes it effortless to accumulate ten dependencies without noticing. IntelliJ literally warns *"Field injection is not recommended."*

### The circular-dependency answer (the follow-up)

```
A needs B, B needs A

Constructor injection → BeanCurrentlyInCreationException at STARTUP.
                        Spring cannot build either one first. ⭐ Good — it made you look.
Setter injection      → Spring creates both, then wires them. It "works",
                        and the bad design survives to production.

Since Spring Boot 2.6 circular references are DISALLOWED by default
(spring.main.allow-circular-references=true re-enables them — a plaster, not a fix).

The real fix: extract the shared logic into a third bean, or use an event.
```

> 🗣️ **Interview-ready answer:** "I use **constructor injection everywhere** — it lets me make the fields `final`, so the object can never exist in a half-built state, and a missing bean blows up at startup instead of as a `NullPointerException` in production. Since Spring 4.3 I don't even need `@Autowired` when there's one constructor. I keep **setter injection for genuinely optional dependencies** with a sensible default. And I treat a **circular dependency as a design signal**, not something to configure around."

🔗 More Spring wiring depth (`@Qualifier` vs `@Primary`, scanning across packages): **[06 — Spring Boot](./06-spring-boot.md)** · **[37 Q3–Q7](./37-altimetrik-fullstack-java-angular-31aug.md)**.

---

## 4. `@Component` vs `@Bean`

**One line:** **`@Component` = "Spring, build this class for me"** (you own the class). **`@Bean` = "I'll build the object, you manage it"** (you don't own the class).

```java
// @Component — on YOUR OWN class, found by component scanning
@Service                                   // @Service IS a @Component
public class OrderService { ... }

// @Bean — on a METHOD inside a @Configuration class, for a class you can't annotate
@Configuration
public class AppConfig {

    @Bean                                  // bean name = the METHOD name → "restClient"
    public RestClient restClient(RestClient.Builder builder) {
        return builder.baseUrl("https://api.example.com")
                      .defaultHeader("X-App", "robogebra")
                      .build();
    }

    @Bean
    @ConditionalOnMissingBean              // conditional creation — impossible with @Component
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule())
                                 .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
```

| | `@Component` (and `@Service`/`@Repository`/`@Controller`) | `@Bean` |
|---|---|---|
| Applied to | A **class** | A **method** |
| Who constructs it | **Spring** (via the constructor) | **You**, in the method body |
| Discovered by | **Component scanning** (`@ComponentScan`) | Declared inside a `@Configuration` class |
| Bean name | Class name, decapitalised → `orderService` | **Method name** → `restClient` |
| Third-party classes | ❌ You can't add an annotation to a JAR | ✅ **The whole reason `@Bean` exists** |
| Conditional / multiple instances | Awkward | ✅ Easy — `if`s, `@Conditional`, two methods returning the same type |

### When to use which — the decision line

```
Is it a class in MY source tree?
   YES → @Component / @Service / @Repository / @Controller
   NO  → @Bean in a @Configuration class

Do I need to configure it, or create several of the same type?
   YES → @Bean (a method can take arguments, branch, read properties)
```

### The stereotype family (they usually ask this next)

`@Service`, `@Repository` and `@Controller` are all **meta-annotated with `@Component`** — Spring treats them identically for scanning. The difference is intent, *except* **`@Repository`, which is the only one with real behaviour**: it enables **persistence exception translation** (a vendor `SQLException` becomes Spring's `DataAccessException`).

### ⚠️ The `proxyBeanMethods` trap ⭐

```java
@Configuration                    // proxyBeanMethods = true by default → CGLIB proxy
public class AppConfig {
    @Bean public A a() { return new A(); }
    @Bean public B b() { return new B(a()); }   // a() does NOT run twice —
}                                               // the proxy returns the SAME singleton ⭐
```
With `@Configuration(proxyBeanMethods = false)` (or `@Component` instead of `@Configuration`) that call is a **plain Java call** and you get **two different `A` instances**. Volunteer this — it's the one detail that shows you've been bitten by it.

🔗 **[06 — Spring Boot](./06-spring-boot.md)** · **[37 Q4 & Q6](./37-altimetrik-fullstack-java-angular-31aug.md)**.

---

## 5. How do microservices load values?

> The question is **externalised configuration**: *how does one build of the service get the right URLs, credentials and toggles in dev, QA and prod?*

**One line:** **one artefact, many environments** — the jar never changes, only what is injected into it.

### The four ways to read a value

```java
// 1. @Value — a single value, with a default after the colon
@Value("${payment.timeout-ms:5000}")
private int timeoutMs;

// 2. @ConfigurationProperties — TYPE-SAFE binding of a whole block ✅ preferred
@ConfigurationProperties(prefix = "payment")
@Validated
public record PaymentProperties(@NotBlank String baseUrl,
                                @Positive int timeoutMs,
                                boolean retryEnabled) { }

// 3. Environment — programmatic lookup
@Autowired Environment env;
String url = env.getProperty("payment.base-url");

// 4. @Profile — a whole BEAN only exists in some environments
@Bean @Profile("!prod")
public DataSeeder devSeeder() { return new DataSeeder(); }
```

```yaml
# application.yml — shared defaults
payment:
  base-url: http://localhost:8081
  timeout-ms: 5000
---
# application-prod.yml — overrides for prod only
payment:
  base-url: https://payments.internal
  timeout-ms: 2000
```

Activate with `--spring.profiles.active=prod` or `SPRING_PROFILES_ACTIVE=prod`.

### The precedence order — say it as "later wins" ⭐

```
HIGHEST ─── command-line args        --payment.timeout-ms=2000
       ├──  OS environment variables  PAYMENT_TIMEOUT_MS=2000     ← relaxed binding
       ├──  application-{profile}.yml (outside the jar)
       ├──  application.yml           (outside the jar)
       ├──  application-{profile}.yml (inside the jar)
       ├──  application.yml           (inside the jar)
LOWEST ─── @Value / @ConfigurationProperties defaults
```

**Relaxed binding** is the detail worth naming: `payment.timeout-ms` in YAML is the same property as `PAYMENT_TIMEOUT_MS` as an env var. That is what makes Docker and Kubernetes work without touching the jar.

### At microservice scale — the part that makes it a *microservices* answer ⭐

```
              ┌───────────────────────┐
              │  Git repo (config)    │   dev.yml · qa.yml · prod.yml
              └───────────┬───────────┘
                          │
              ┌───────────▼───────────┐
              │  Spring Cloud Config  │   or Consul / etcd / K8s ConfigMap
              │        Server         │
              └───────────┬───────────┘
        ┌─────────────────┼─────────────────┐
   ┌────▼────┐       ┌────▼────┐       ┌────▼────┐
   │ order   │       │ payment │       │  user   │      each pulls its own config
   │ service │       │ service │       │ service │      at STARTUP
   └─────────┘       └─────────┘       └─────────┘

Refresh without a restart:  @RefreshScope + POST /actuator/refresh
Refresh EVERY instance:     Spring Cloud Bus (Kafka/Rabbit) → one broadcast
Secrets:                    NEVER in git → Vault / AWS Secrets Manager / SSM Parameter Store
Kubernetes:                 ConfigMap → env vars or a mounted file; Secret for credentials
```

> 🗣️ **Interview-ready answer:** "The build artefact is identical in every environment — what changes is what's injected. Defaults live in `application.yml`, environment differences in `application-{profile}.yml`, and anything that differs per deployment comes in as an **environment variable**, which wins over the file because of the precedence order. I bind config with **`@ConfigurationProperties` rather than scattered `@Value`s**, so it's type-safe and validated at startup. At scale you centralise it in a **Config Server backed by git** — you get versioned, auditable config and `@RefreshScope` for changing a value without a redeploy. **Secrets never go in git or in `application.yml`** — those come from Vault or AWS Secrets Manager."

### ⚠️ Traps

- **`@Value` in a constructor works; `@Value` on a field read from a constructor is still `null`** — field injection happens *after* construction.
- **`@ConfigurationProperties` needs to be registered** — `@EnableConfigurationProperties`, `@ConfigurationPropertiesScan`, or `@Component` on the class.
- **Angular's `environment.ts` is compiled into the bundle and is PUBLIC.** Never put a secret there. (Repeat of the same lesson in [38 — IQVIA](./38-iqvia-technical-lead-prep.md).)

🔗 **[06 — Spring Boot](./06-spring-boot.md)** · **[37 Q14](./37-altimetrik-fullstack-java-angular-31aug.md)** (dev/QA/prod, asked 4 days earlier — **the same question, twice in one week**).

---

## 6. Monolithic vs microservices

**One line:** a **monolith is one deployable unit**; **microservices are many independently deployable units, each owning its own data.**

```
MONOLITH                              MICROSERVICES
┌──────────────────────────┐          ┌────────┐ ┌────────┐ ┌────────┐
│  UI · Orders · Payments  │          │ Order  │ │Payment │ │  User  │
│  Users · Reports         │          │  svc   │ │  svc   │ │  svc   │
│                          │          └───┬────┘ └───┬────┘ └───┬────┘
│      ONE database        │              │DB        │DB        │DB
└──────────────────────────┘          (own schema each — no shared tables)
   one build · one deploy               n builds · n deploys · a network in between
   a method call                        an HTTP call or a Kafka message ⭐
```

| | **Monolith** | **Microservices** |
|---|---|---|
| Deployment | One artefact | One per service |
| Data | One schema, **real ACID transactions** ⭐ | **Database per service** → eventual consistency, **Saga** instead of `@Transactional` |
| Calls between modules | In-process — fast, always succeeds | Over a network — **can be slow, can fail, can arrive twice** ⭐ |
| Scaling | Scale the **whole** app | Scale **only the hot service** |
| A bad release | Takes everything down | Blast radius is one service (if you added a **circuit breaker**) |
| Team fit | Small team, one codebase | Many teams that must deploy independently ⭐ |
| Tech choice | One stack | Per service |
| Debugging | A stack trace | **Distributed tracing** (correlation ID across services) |
| Ops cost | Low | **High** — CI/CD per service, gateway, discovery, monitoring, on-call |

### The answer that separates you from a blog post ⭐

> 🗣️ "Microservices are an **organisational** solution before they're a technical one — you split when independent teams need to deploy independently. The cost is real: every in-process method call becomes a network call that can be slow, fail, or arrive twice, and a database transaction becomes a **Saga with compensating actions**. So the sequence I believe in is **modular monolith first** — clean module boundaries, one deployment — and you extract a service when there's a concrete reason: a different scaling profile, a different release cadence, or a different team owning it."

### ⚠️ The one you must get right about your own project 🔴

> **RoboGebra is a MODULAR MONOLITH — say so.**
> There is **no Eureka, no Feign, no circuit breaker and no Kafka** in your codebase. Claiming "microservices" survives exactly one follow-up (*"how do your services discover each other?"*) and then costs you the round.
> **The honest version is the stronger answer:** *"RoboGebra is a Spring Boot modular monolith on MongoDB — the modules are separated by package and by service boundary, but it deploys as one unit, because the team is small and the release cadence is shared. I've designed against microservice patterns and I can talk through them, but I'd be overstating it to say I run a fleet in production."*

### The anti-pattern to name

**The distributed monolith:** services split across the network but still deployed together, still sharing a database, still requiring a coordinated release. **All of the cost, none of the benefit.** Naming this makes you sound like someone who has seen it.

🔗 **[08 — Microservices Basics](./08-microservices-basics.md)** · failure vocabulary (timeout · circuit breaker · retry+backoff · idempotency key · outbox · DLQ · compensating transaction): **[37 Q11–Q13](./37-altimetrik-fullstack-java-angular-31aug.md)**.

---

# Part 3 — Angular

## 7. Components in Angular

**One line:** a component is **one piece of the screen plus the logic that drives it** — a TypeScript class with an `@Component` decorator that binds a **template** (what the user sees) to **state** (what the class holds).

```typescript
@Component({
  selector: 'app-student-card',        // the tag: <app-student-card>
  templateUrl: './student-card.component.html',
  styleUrls: ['./student-card.component.scss'],
  // standalone: true,                 // Angular 14+; NgModule-based in your project
})
export class StudentCardComponent implements OnInit, OnChanges {
  @Input() student!: Student;                       // data IN from the parent
  @Output() selected = new EventEmitter<string>();  // event OUT to the parent

  constructor(private studentService: StudentService) {}   // DI only — inputs are undefined here

  ngOnInit(): void { /* inputs are ready — safe to call the API */ }

  onClick(): void { this.selected.emit(this.student.id); }
}
```

### The four files the CLI creates

```
student-card/
├── student-card.component.ts      the class  — state + behaviour
├── student-card.component.html    the template — what renders
├── student-card.component.scss    styles — SCOPED to this component by default ⭐
└── student-card.component.spec.ts the unit test
```

### The `@Component` metadata worth naming

| Property | What it does |
|---|---|
| `selector` | The tag / attribute / class the component matches |
| `template` / `templateUrl` | Inline HTML vs an external file |
| `styles` / `styleUrls` | Scoped CSS |
| `providers` | A **new instance** of a service **per component instance** ⭐ |
| `changeDetection` | `Default` vs **`OnPush`** (only re-check on input *reference* change, event, or async pipe) |
| `encapsulation` | `Emulated` (default — Angular adds `_ngcontent` attributes) · `None` (leaks globally ⚠️) · `ShadowDom` |
| `animations`, `host`, `imports` (standalone) | |

### An app is a **tree** of components

```
AppComponent
 ├── HeaderComponent
 ├── RouterOutlet
 │    └── StudentListComponent
 │         └── StudentCardComponent  ×N     ← @Input() down, @Output() up
 └── FooterComponent
```

**Component communication** (asked in **4 separate rounds** now):
`@Input()` parent→child · `@Output()` + `EventEmitter` child→parent · a **shared service with a `BehaviorSubject`** for unrelated components · `@ViewChild` for direct access · route params / `NgRx` store for app-wide state.

> ⚠️ **Honesty guard for your projects:** you use the **NgModule** style with `declarations`, not standalone components (**2 usages**), and you have **no signals** (0 usages). Say *"I know standalone and signals are the direction Angular is going, and I've read into them — the codebase I work in is NgModule-based on Angular 16"*. That reads as current; claiming daily use does not survive a follow-up.

🔗 **[04 — Angular](./04-angular.md)** · **[39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)**.

---

## 8. How do you create a new component?

**The answer they want first — the CLI:**

```bash
ng generate component student-card
ng g c student-card                 # short form
```

**What the CLI does for you (say all four — this is the actual answer):**

```
1. creates the folder  src/app/student-card/
2. creates 4 files     .ts · .html · .scss · .spec.ts
3. adds @Component     with selector 'app-student-card' and the correct file paths
4. REGISTERS it        → adds StudentCardComponent to `declarations` in the nearest NgModule ⭐
                          (or, with --standalone, makes it standalone instead)
```

**Then you use it** by putting its selector in a parent template: `<app-student-card [student]="s" (selected)="onSelect($event)"></app-student-card>`

### The flags worth knowing

| Flag | Why |
|---|---|
| `--dry-run` / `-d` | **Show what it would create without creating it** — always safe |
| `--skip-tests` | No `.spec.ts` |
| `--inline-template` / `-t`, `--inline-style` / `-s` | Small components in one file |
| `--flat` | No folder |
| `--module=admin` | Register it in a **specific** module (matters once you have lazy-loaded feature modules) |
| `--standalone` | Angular 14+; **default from Angular 17** |
| `--change-detection=OnPush` | Sets `OnPush` from the start |
| `--prefix=admin` | `<admin-student-card>` instead of `app-` |
| `ng g c features/students/student-card` | Path = folder structure |

### Doing it by hand (they sometimes ask "and without the CLI?")

Create the `.ts` with an `@Component` decorator, create the template, then **add the class to `declarations`** in the NgModule — and to `exports` if another module needs it. **Forgetting that registration is the #1 cause of** `'app-student-card' is not a known element`.

> ⚠️ **The classic error and its three causes:** `'app-x' is not a known element` →
> **(1)** not declared in any module · **(2)** declared in module A but used in module B **without being `exported`** · **(3)** a typo in the selector. For a standalone component the cause is different: it's missing from the consumer's `imports` array.

🔗 **[04 — Angular](./04-angular.md)** · **[25 — Binding & Forms](./25-angular-binding-forms.md)**.

---

## 9. `ngOnChanges` vs `ngDoCheck`

> ⭐ **Second round running** — Photon asked this exact pair ([26 §Photon Q6](./26-companies-asked-questions.md#6-ngonchanges-vs-ngdocheck-)).

**One line:** **`ngOnChanges` fires only when an `@Input()`'s *reference* changes. `ngDoCheck` fires on *every single* change-detection cycle** — which is why one is cheap and the other is dangerous.

| | `ngOnChanges` | `ngDoCheck` |
|---|---|---|
| **When** | Before `ngOnInit`, then **every time an `@Input` reference changes** | **Every change-detection run** — a click, a timer, an HTTP response, anywhere in the app |
| **Fires with no `@Input`s?** | ❌ Never | ✅ Always |
| **Argument** | `changes: SimpleChanges` — `previousValue`, `currentValue`, `firstChange()` | None — **you** do the comparing |
| **Cost** | Cheap | ⚠️ **Can run hundreds of times a second** |
| **Use it for** | React to a new input value | Detect a change Angular **cannot see**: a mutated object/array |

```typescript
export class StudentCardComponent implements OnChanges, DoCheck {
  @Input() student!: Student;
  private lastName = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['student'] && !changes['student'].firstChange) {
      this.reload(changes['student'].currentValue);   // new REFERENCE arrived
    }
  }

  ngDoCheck(): void {
    if (this.student?.name !== this.lastName) {       // catches a MUTATION
      this.lastName = this.student.name;              // ngOnChanges never fired for this
    }
  }
}
```

### The whole point — why `ngDoCheck` exists ⭐

```
Parent does:  this.student = { ...this.student, name: 'New' };   // NEW object
              → reference changed → ngOnChanges FIRES ✅

Parent does:  this.student.name = 'New';                          // SAME object, mutated
              → reference identical → ngOnChanges does NOT fire ❌
              → the template still updates (default CD re-reads bindings)
                but your ngOnChanges logic never ran
              → ngDoCheck is where you catch it ⭐
```

**Angular compares inputs with `===` (reference equality), not deep equality.** That single fact answers the whole question.

### The hook order

```
constructor
   ↓
ngOnChanges       ← first, and again on every input change
   ↓
ngOnInit          ← once
   ↓
ngDoCheck         ← after every ngOnChanges, and on every CD cycle after that
   ↓
ngAfterContentInit → ngAfterContentChecked
   ↓
ngAfterViewInit    → ngAfterViewChecked
   ↓
ngOnDestroy
```

### ⚠️ The rules that keep `ngDoCheck` safe

1. **Never do heavy work in it** — no API calls, no deep clones, no `JSON.stringify` of a big object. It runs on every cycle.
2. **Never mutate state that's bound to the template** in it → `ExpressionChangedAfterItHasBeenCheckedError` in dev mode.
3. Use **`KeyValueDiffers`** (objects) / **`IterableDiffers`** (arrays) instead of hand-rolled comparison — that's what Angular's own `NgClass`/`NgForOf` use.
4. **The better fix is usually not to need it:** treat inputs as **immutable** — replace the object instead of mutating it — and then `ngOnChanges` + `OnPush` handle everything, faster.

> 🗣️ **The closing line:** "In practice I try never to need `ngDoCheck`. If I'm reaching for it, it usually means a parent is mutating an object it passed down — and the cleaner fix is to make the update immutable, which also lets me turn on `OnPush`."

🔗 **[04 — Angular](./04-angular.md)** · **[26 §Photon Q6](./26-companies-asked-questions.md)** · change detection depth: **[38 — IQVIA](./38-iqvia-technical-lead-prep.md)**.

---

# Part 4 — The live-coding question

## 10. 🔴 Live coding — second non-repeating character from the END

> **This is the one you didn't write.** Here it is in the **simplest possible way — 3 steps, Stream API.** No tricks. **Type it until you can write it without looking.** It is a twist on *"first non-repeating character"*, which **Altimetrik already asked** — the family is now on its 2nd round, so learn it as a pattern, not as one answer.

### The question, restated

```java
String name = "Priyanka";
// find the SECOND non-repeating character counting from the END
```

### First — work it out on paper (10 seconds)

```
"Priyanka"  →  P  r  i  y  a  n  k  a

How many times each one comes:
   P=1   r=1   i=1   y=1   a=2   n=1   k=1

Characters that come only ONE time:   P  r  i  y  n  k
Now count from the END:               k  = 1st
                                      n  = 2nd   ⭐

ANSWER = n
```

**Say this out loud before you type anything.** It takes ten seconds and it makes the code obvious.

---

## ✅ THE PROGRAM — the simple one (write this)

**Three steps. That's all it is.**
**Step 1 — count each character. Step 2 — keep the ones that came 1 time. Step 3 — pick the 2nd from the last.**

```java
import java.util.*;
import java.util.stream.*;

public class Main {
    public static void main(String[] args) {

        String name = "Priyanka";

        // STEP 1 — count how many times each character comes
        Map<Character, Long> count = name.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(c -> c, LinkedHashMap::new, Collectors.counting()));
        // {P=1, r=1, i=1, y=1, a=2, n=1, k=1}

        // STEP 2 — keep only the characters whose count is 1
        List<Character> single = count.entrySet().stream()
                .filter(e -> e.getValue() == 1)
                .map(e -> e.getKey())
                .collect(Collectors.toList());
        // [P, r, i, y, n, k]

        // STEP 3 — take the 2nd one from the END
        System.out.println(single.get(single.size() - 2));
    }
}
```

**Output:**
```
n
```

### Line-by-line, in plain words

| Line | What it does, simply |
|---|---|
| `name.chars()` | Break the word into its characters |
| `.mapToObj(c -> (char) c)` | `chars()` gives numbers — turn them back into letters |
| `Collectors.groupingBy(c -> c, ..., counting())` | **Group the same letters together and count them** → `{P=1, r=1, ... a=2, ...}` |
| `LinkedHashMap::new` | **Keep them in the order they appeared** ⭐ (a normal `HashMap` mixes up the order — then "2nd from the end" means nothing) |
| `.filter(e -> e.getValue() == 1)` | Keep only the letters that came **one** time |
| `.map(e -> e.getKey())` | I want the **letter**, not the count |
| `.collect(Collectors.toList())` | Put them in a list → `[P, r, i, y, n, k]` |
| `single.size() - 2` | Last position is `size - 1`, so **2nd from the end is `size - 2`** ⭐ |

> 🧠 **Easy memory box — 3 steps:**
> **① count → ② keep count == 1 → ③ take `size - 2`.**
> That's the whole program. If you remember only one thing, remember `size - 2`.

### 🔴 The one mistake to avoid

```java
Collectors.groupingBy(c -> c, Collectors.counting())          // ❌ plain HashMap — order is mixed up
Collectors.groupingBy(c -> c, LinkedHashMap::new, counting()) // ✅ keeps P, r, i, y, n, k order
```

**`LinkedHashMap` is the whole trick.** Without it the list can come out as `[r, P, k, i, y, n]` and your answer is wrong. If they ask *"why LinkedHashMap?"* — *"because the question asks about position, and `HashMap` has no order."*

---

## If they ask for more

### "Do it without the list"

Same idea, but reverse the word first and just skip one:

```java
Map<Character, Long> count = name.chars().mapToObj(c -> (char) c)
        .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

String reverse = new StringBuilder(name).reverse().toString();   // "aknayirP"

char answer = reverse.chars()
        .mapToObj(c -> (char) c)
        .filter(c -> count.get(c) == 1)      // k, n, y, i, r, P
        .skip(1)                             // skip k (that's the 1st)
        .findFirst()                         // n  ⭐
        .get();

System.out.println(answer);   // n
```

**Simple idea:** *2nd from the end of the word = 2nd from the start of the reversed word.*

### "Now make it the 3rd one" (they usually do)

Change one number — make it a method:

```java
static char nthFromEnd(String s, int n) {
    Map<Character, Long> count = s.chars().mapToObj(c -> (char) c)
            .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

    return new StringBuilder(s).reverse().toString().chars()
            .mapToObj(c -> (char) c)
            .filter(c -> count.get(c) == 1)
            .skip(n - 1)                     // ⭐ only this line changes
            .findFirst()
            .get();
}

nthFromEnd("Priyanka", 1);   // k
nthFromEnd("Priyanka", 2);   // n   ⭐ the asked answer
nthFromEnd("Priyanka", 3);   // y
```

### "Now do it without the Stream API"

```java
static char secondFromEnd(String s) {
    Map<Character, Integer> count = new LinkedHashMap<>();
    for (char c : s.toCharArray()) {
        count.put(c, count.getOrDefault(c, 0) + 1);      // count
    }

    int found = 0;
    for (int i = s.length() - 1; i >= 0; i--) {          // go backwards
        char c = s.charAt(i);
        if (count.get(c) == 1) {
            found++;
            if (found == 2) return c;                    // the 2nd one → n
        }
    }
    return ' ';
}
```

### The questions they ask after

| They ask | You say (short and simple) |
|---|---|
| **How fast is it?** | "**O(n)** — one pass to count, one pass to find. I go through the word twice, not more." |
| **Why count first?** | "I can't tell if the first letter repeats until I've seen the last letter. So counting has to finish before I can pick." |
| **Why `LinkedHashMap`?** | "Because the question is about **position**, and a normal `HashMap` doesn't keep order." |
| **Is it case sensitive?** | "Yes — `P` and `p` would be different. I'd ask you if that's wanted; if not I'd do `name.toLowerCase()` first." *(Here it doesn't change the answer.)* |
| **What if there is no 2nd one?** | "`single.size() - 2` would fail, so I'd check `if (single.size() >= 2)` first, or return an `Optional`." |
| **First non-repeating instead?** | "Same program — just take `single.get(0)`." **That's the Altimetrik version of this question.** |


### ❌ The one version NOT to write

```java
name.chars().filter(c -> name.chars().filter(x -> x == c).count() == 1)   // ⚠️ slow
```

It works, but it **goes through the whole word again for every single letter** — that's O(n²). The 3-step version above goes through it twice. If it slips out, just say *"that's slow, let me count once first"* and fix it.

---

> 🧠 **The whole family is the same 3 steps.** Count → keep `count == 1` → pick the position they asked for:
>
> | Question | Change only step 3 |
> |---|---|
> | First non-repeating | `single.get(0)` |
> | Last non-repeating | `single.get(single.size() - 1)` |
> | **2nd from the end** ⭐ | `single.get(single.size() - 2)` |
> | 2nd from the start | `single.get(1)` |
> | First **repeating** | change step 2 to `e.getValue() > 1`, then `get(0)` |


🔗 More of this family: **[22 — Java Streams: 20 coding problems](./22-java-streams-coding-problems.md)** · **[09 — Coding Problems](./09-coding-problems.md)** · **[26 §Altimetrik Q3](./26-companies-asked-questions.md#3-first-non-repeating-character)**.

---

# 🎯 What this round tells you

**1. It was a breadth sweep — and 7 of the 10 were repeats.** Java 8, HashMap, DI, `@Component`/`@Bean`, monolith-vs-microservices, Angular components, `ngOnChanges`/`ngDoCheck` — every one is already in your log. **The bank is ~25 questions and you have now seen it eleven times.**

**2. The same question came back four days later.** Altimetrik (31 Aug) asked *"dev/QA/prod — different DBs and properties, how is that handled?"*; Capgemini asked *"how do microservices load values?"* — **the same answer**. Likewise `@Component` vs `@Bean` at both. Re-reading [37](./37-altimetrik-fullstack-java-angular-31aug.md) was worth marks in this round.

**3. The round is decided by the coding question.** Nine theory answers you already knew, then one program. **That is the pattern across Altimetrik, Photon, Codeboard and now Capgemini.** Theory gets you to the coding question; the coding question decides the outcome. From now on, **type one Streams problem a day** from [22](./22-java-streams-coding-problems.md) — reading them is not the same skill.

**4. Say the answer out loud before you type it.** For Q10 the paper work is ten seconds (`P r i y n k` → `k` → **`n`**) and it makes the code obvious. Going straight to the keyboard is what makes these questions feel hard.

## ⚠️ Mark your own fumbles — do this TODAY

| Q | Topic | ✅ answered well / ⚠️ fumbled / ❌ blank |
|---|---|---|
| 1 | Java 8 features | |
| 2 | HashMap vs LinkedHashMap | |
| 3 | Setter vs constructor injection | |
| 4 | `@Component` vs `@Bean` | |
| 5 | How microservices load values | |
| 6 | Monolith vs microservices | |
| 7 | Angular components | |
| 8 | Creating a component | |
| 9 | `ngOnChanges` vs `ngDoCheck` | |
| 10 | 🔴 Second non-repeating from the end | ❌ **not written — now solved above; type it** |

---

**Related files:** [26 — Companies (the recall log)](./26-companies-asked-questions.md) · [05 — Core Java](./05-java.md) · [31 — HashMap Internals](./31-hashmap-internals.md) · [06 — Spring Boot](./06-spring-boot.md) · [08 — Microservices](./08-microservices-basics.md) · [04 — Angular](./04-angular.md) · [22 — Java Streams](./22-java-streams-coding-problems.md) · [09 — Coding Problems](./09-coding-problems.md) · [37 — Altimetrik (31 Aug)](./37-altimetrik-fullstack-java-angular-31aug.md)
