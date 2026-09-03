# 🟨 Altimetrik — Fullstack Java + Angular round (31 Aug 2026)

> 📅 **Attended: Sunday 31 Aug 2026.** ❌ **Not cleared.**
> 
> ⚠️ **Read this as a study list, not a post-mortem.** The round was lost on **Spring wiring depth** — component scanning across packages, `@Qualifier` vs `@Primary`, the repository hierarchy, the *classes* inside pagination. Those are now fully answered below and in **[06 — Spring Boot](./06-spring-boot.md)**. **Every one of these 22 questions will be asked again somewhere else** — lifecycle hooks are on their 5th round, interceptors on their 4th.
> 🔁 **Second time at Altimetrik.** Your first attempt (Level 1 Java + an Angular round, **24 questions**) is logged in **[26 — Companies §Altimetrik](./26-companies-asked-questions.md#-altimetrik--level-1-round-java)**. This round was a different shape: **Spring Boot depth first, Angular second, one Streams coding problem to close.**
>
> **Shape of the round:** ~22 questions — **2 Java (records + pattern matching) → 10 Spring/Spring Boot → 2 microservices → 1 config/environments → 6 Angular → 1 Java Streams live-coding.**
> **The signal:** they went *deep on Spring wiring* (component scanning across packages, `@Qualifier` vs `@Primary`, `CrudRepository` vs `JpaRepository`, pagination classes) — this was not a breadth sweep, it was an "does she actually build with Spring Boot or just use it?" round.

---

---

## 🧠 The lesson from this round — in one page

> This round was lost on **Spring wiring depth**. Not on effort, and not on Angular. Here is exactly what "depth" meant, so the same gap can't happen twice.

```
What a BREADTH round asks          What THIS round asked ⭐
──────────────────────────         ───────────────────────
"What is @Autowired?"              "Two beans of the same type —
                                    what EXACTLY happens, and what's the
                                    precedence between @Qualifier,
                                    @Primary and the bean name?" ⭐

"What is a repository?"            "CrudRepository vs JpaRepository —
                                    what does the hierarchy give you,
                                    and what does findAll() RETURN?" ⭐

"How does pagination work?"        "Name the CLASSES involved." ⭐
                                    (Pageable · PageRequest · Sort ·
                                     Page/PageImpl · Slice/SliceImpl)

"Do you use Spring Boot?"          "Multiple packages — how does Spring
                                    read all the annotations?" ⭐
                                    (scanBasePackageClasses, @EntityScan,
                                     @EnableJpaRepositories, auto-config)
```

### The pattern to recognise ⭐

```
A DEPTH round is testing one thing:

   "Has she BUILT with this, or has she USED it?" ⭐

The tell is that the questions go one level BELOW the API you type every day —
into the classes behind it, the precedence rules, the return types.
```

Real-world idea: **anyone can drive a car. This round asked what happens when you press the clutch.**

### How to answer a depth question when you're not sure ⭐

```
1. Give the SHAPE confidently — what you DO know
2. Name the CLASS or the RULE if you can reach it
3. Say honestly where your certainty ends ⭐
4. Say how you'd check it

"Pagination gives you a Page, which carries the content plus the total —
 so it runs a second COUNT query, whereas Slice runs one and only knows
 whether there's a next page. The concrete classes are Pageable and
 PageRequest, and I believe PageImpl is the implementation — that last
 one I'd confirm in the source rather than guess." ⭐
```

⭐ **That answer scores far better than a confident wrong class name.** Depth rounds punish bluffing much harder than they punish a bounded "I'd check that."

### The three things to drill from this round

```
1. @Qualifier > @Primary > bean name ⭐ (and by TYPE first → NoUniqueBeanDefinition)
2. CrudRepository → PagingAndSortingRepository → JpaRepository ⭐
      JpaRepository.findAll() returns List; CrudRepository returns Iterable ⭐
3. Page (2 queries, has the total ⭐) vs Slice (1 query, hasNext only) ⭐
```

→ All three are now written up in **[06 — Spring Boot](./06-spring-boot.md)**.

---

## 📋 The 22 questions at a glance

| # | Question | Area | Answered below |
|---|---|---|---|
| 1 | **What is a `record`?** — and why are we using it? | Java 16/17 ⭐ | [→](#1-what-is-a-record--and-why-are-we-using-it) |
| 2 | Pattern matching | Java 17 | [→](#2-pattern-matching) |
| 3 | Spring Boot annotations | Spring Boot | [→](#3-spring-boot-annotations) |
| 4 | `@Component` and `@Service` annotation | Spring | [→](#4-component-vs-service-annotation) |
| 5 | "I have multiple packages — I want Spring to read all the annotations" | Spring | [→](#5-multiple-packages--how-does-spring-find-all-the-annotations) |
| 6 | Reading the bean / Spring annotations — `@Bean` | Spring | [→](#6-bean-vs-stereotype-annotations) |
| 7 | `@Autowired`, `@Qualifier` and `@Primary` | Spring DI | [→](#7-autowired-qualifier-and-primary) |
| 8 | `CrudRepository` and `JpaRepository` | Spring Data | [→](#8-crudrepository-vs-jparepository-and-the-whole-hierarchy) |
| 9 | Pagination methods | Spring Data | [→](#9-pagination-methods) |
| 10 | What classes are created in pagination | Spring Data | [→](#10-what-classes-are-involved-in-pagination) |
| 11 | Communication in microservices | Microservices | [→](#11-communication-in-microservices) |
| 12 | How do you handle error **cascading**? | Microservices | [→](#12-how-do-you-handle-error-cascading) |
| 13 | Two microservices, network failure — A errors, how do you inform B? | Microservices ⭐ | [→](#13-two-microservices-a-network-failure-in-between--a-errors-how-do-you-inform-b) |
| 14 | Dev / QA / Prod — different DBs and properties, how is that handled? | Spring Boot | [→](#14-dev--qa--prod--different-properties-and-a-different-db-per-environment) |
| 15 | `ngOnInit` and constructor | Angular ⭐ | [→](#15-ngoninit-vs-constructor) |
| 16 | Decorator | Angular | [→](#16-decorators) |
| 17 | Interceptor | Angular ⭐ | [→](#17-interceptor) |
| 18 | Token expired — how do you notify the user? | Angular / Auth | [→](#18-the-token-has-expired--how-do-you-notify-the-user) |
| 19 | How would you redirect to a page? | Angular | [→](#19-how-do-you-redirect-to-a-page) |
| 20 | Angular core — what is data binding? | Angular ⭐ | [→](#20-data-binding) |
| 21 | Lazy loading | Angular ⭐ | [→](#21-lazy-loading) |
| 22 | Highest-paid employee in each department → department + employee name | Java Streams 🔴 | [→](#22-live-coding--highest-paid-employee-in-each-department) |

⭐ = already asked in earlier rounds (repeat). 🔴 = live coding.

---

# Part 1 — Java: the opening question

## 1. What is a `record`? — and why are we using it?

> 🔴 **This was the very first question of the round.** They asked *"what is a Record"*, then immediately *"why are we using this?"* — i.e. **what problem does it solve that a normal class didn't**. Treat it as one question with two halves and answer both without being prompted.

**One line:** a **record** (Java 16, previewed in 14/15) is a **transparent carrier for immutable data** — you declare the components and the compiler generates the constructor, the accessors, `equals`, `hashCode` and `toString` for you.

```java
public record Employee(int id, String name, int age, String department,
                       String city, double salary, String gender) { }
```

**That one line replaces ~80 lines of boilerplate.** The compiler generates:
- a **canonical constructor** `Employee(int, String, int, String, String, double, String)`
- **accessors named after the components** — `id()`, `name()`, `salary()` — ⚠️ **not** `getId()`/`getName()`
- **`equals`/`hashCode`** based on **all** components (value semantics)
- **`toString`** → `Employee[id=1, name=Abraham, age=29, ...]`
- `private final` fields — the record is **shallowly immutable**

### Why are we using it? — the four reasons

| Reason | What to say |
|---|---|
| **1. Kills boilerplate** | "A DTO used to be a constructor, seven getters, `equals`, `hashCode` and `toString` — all of it noise the reader has to scan past. The record *is* the data model, in one line." |
| **2. Immutability by default** | "Fields are `final`. That makes it **thread-safe**, safe to use as a **`HashMap` key**, and safe to pass around without defensive copies — no caller can mutate my object behind my back." |
| **3. Correct `equals`/`hashCode`, free** | "Hand-written `equals`/`hashCode` are a classic bug source — someone adds a field and forgets to update them. The record can't drift out of sync." |
| **4. It signals *intent*** | "A record says *this type is nothing but its data*. A class says *this type has behaviour*. That's semantic information for every future reader — and the compiler enforces it." |

> 🎯 **The sentence that lands it:** "Records are Java's answer to the **anaemic data class**. Lombok's `@Value` solved the same problem with annotation processing; records solve it **in the language**, so there's no build-time magic and the semantics are guaranteed."

### Where you'd actually use one (give a real example)

**DTOs / API request-response objects · value objects (`Money`, `Coordinates`, `PatientId`) · records as map keys · a multi-value return from a method · events published to Kafka · `@ConfigurationProperties` binding.**

```java
// REST DTO — validation annotations work fine on components
public record CreateEmployeeRequest(
    @NotBlank String name,
    @Min(18) int age,
    @NotBlank String department,
    @Positive double salary) { }

@PostMapping("/employees")
public EmployeeResponse create(@Valid @RequestBody CreateEmployeeRequest req) { ... }
```

```java
// returning two values without inventing a throwaway class
record SalaryStats(double max, double avg) {}
```

### Compact constructor — validation & normalisation

```java
public record Employee(int id, String name, double salary) {
    public Employee {                                  // compact form — no parameter list, no assignment
        if (salary < 0) throw new IllegalArgumentException("salary must be >= 0");
        name = name.trim();                            // normalise BEFORE the fields are assigned
    }
    public Employee(int id, String name) { this(id, name, 0); }   // extra constructor must delegate
    public String initials() { return name.substring(0, 1); }     // extra methods are allowed
    public static Employee empty() { return new Employee(0, "", 0); }  // static members allowed
}
```

### The rules / restrictions — the depth follow-up

| Rule | Detail |
|---|---|
| **Implicitly `final`** | A record **cannot be extended**, and cannot extend another class (it already extends `java.lang.Record`) |
| **Can implement interfaces** | ✅ `record Point(int x, int y) implements Comparable<Point>` |
| **No additional instance fields** | All state must be a component — that's what "transparent" means |
| Static fields/methods | ✅ Allowed |
| Accessors can be overridden | ✅ but keep the contract honest |
| Nested records | ✅ Implicitly `static` |
| **Local records** | ✅ Declare one inside a method (Java 16+) — great for a stream pipeline's intermediate tuple |

### ⚠️ The three traps worth volunteering

1. **Shallow immutability.** `record Team(String name, List<Player> players)` — the *reference* is final, but the **list is still mutable**. Defensive-copy in the compact constructor: `players = List.copyOf(players);`
2. **Accessors are `name()`, not `getName()`.** Frameworks expecting the JavaBean convention can trip. Jackson handles records natively (2.12+), and Spring Boot binds them fine — but an old library might not.
3. **JPA `@Entity` cannot be a record.** JPA needs a no-arg constructor, non-final fields and mutability for proxies/dirty checking. **Use records for DTOs and projections, entities stay classes.** ✅ Spring Data *does* support records as **interface/DTO projections**:
   ```java
   record EmployeeView(String name, double salary) {}
   @Query("select new com.x.EmployeeView(e.name, e.salary) from Employee e")
   List<EmployeeView> findViews();
   ```

### Records + pattern matching = the reason they exist

```java
sealed interface Shape permits Circle, Square {}
record Circle(double radius) implements Shape {}
record Square(double side)   implements Shape {}

double area(Shape s) {
    return switch (s) {
        case Circle(double r) -> Math.PI * r * r;      // record DEconstruction pattern
        case Square(double a) -> a * a;
    };                                                  // no default needed — sealed ⇒ exhaustive
}
```
> 🗣️ **Close on this:** "Records, sealed types and pattern matching were designed together — records make data transparent so patterns can take it apart, and sealed types let the compiler prove the switch is exhaustive. That's Java getting **algebraic data types**, and it's why records are more than a Lombok replacement."

### If they meant `this` instead

If the follow-up *"why are we using this"* was about the **`this` keyword**: `this` is a reference to the current object — used to disambiguate a field from a parameter (`this.name = name`), to chain constructors (`this(...)`, must be the first statement), to return the current object for a fluent API, and to pass the current object to a collaborator. It doesn't exist in a `static` context, and in a **TypeScript/JS arrow function `this` is lexical** — which is the Angular version of the same question.

---

# Part 2 — Java (continued)

## 2. Pattern matching

**One line:** pattern matching lets you **test a type and bind a variable in the same expression**, so the cast disappears.

**a) `instanceof` pattern — Java 16, final**
```java
// before
if (obj instanceof String) { String s = (String) obj; if (s.length() > 5) {...} }

// after
if (obj instanceof String s && s.length() > 5) { ... }   // s is in scope here
```
The binding variable `s` has **flow scope** — it exists only where the compiler can prove the test passed. That's why `&&` works and `||` doesn't.

**b) Pattern matching for `switch` — Java 21, final** (preview in 17/19/20)
```java
static String describe(Object o) {
    return switch (o) {
        case null        -> "nothing";              // switch can now handle null explicitly
        case Integer i when i > 100 -> "big int " + i;   // guarded pattern
        case Integer i   -> "int " + i;
        case String s    -> "string of length " + s.length();
        default          -> "something else";
    };
}
```
- `when` is the **guard** (it was `&&` in the early previews).
- Cases are tested **in order**, so a more specific pattern must come first or you get a *"dominated by a preceding label"* compile error.
- Without an explicit `case null`, a `switch` on a pattern **throws `NullPointerException`** — old switch semantics.

**c) Record patterns — Java 21** — destructuring:
```java
record Point(int x, int y) {}
record Line(Point a, Point b) {}

if (obj instanceof Line(Point(var x1, var y1), Point(var x2, var y2))) {
    // x1, y1, x2, y2 are all bound — nested destructuring
}
```

**d) Where it pays off:** `sealed interface Shape permits Circle, Square {}` + a pattern `switch` = **exhaustiveness checked by the compiler**, no `default` needed. Add a new subtype and every switch that doesn't handle it fails to compile. That's the real selling point — say it.

> 🗣️ **Say the version numbers.** `instanceof` pattern = **16**, switch patterns + record patterns = **21**, text blocks = **15**, records + sealed = **16/17**. Getting versions right is a cheap credibility win. Full file: **[12 — Java 17 Features](./12-java17-features.md)**.

---

# Part 3 — Spring Boot (the bulk of the round)

## 3. Spring Boot annotations

Answer this as **groups**, never as a flat list — a list sounds memorized, groups sound used.

**Bootstrapping**
| Annotation | What it does |
|---|---|
| `@SpringBootApplication` | The meta-annotation = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan` |
| `@EnableAutoConfiguration` | Boot configures beans based on what's on the classpath |
| `@ComponentScan` | Where to look for stereotypes (see [Q5](#5-multiple-packages--how-does-spring-find-all-the-annotations)) |
| `@Configuration` + `@Bean` | Java config — you construct the bean yourself |

**Stereotypes (scanned into the context)**
`@Component` · `@Service` · `@Repository` · `@Controller` · `@RestController` (= `@Controller` + `@ResponseBody`)

**Injection**
`@Autowired` · `@Qualifier` · `@Primary` · `@Value` · `@ConfigurationProperties` · `@Lazy` · `@Scope`

**Web layer**
`@RequestMapping` · `@GetMapping` / `@PostMapping` / `@PutMapping` / `@PatchMapping` / `@DeleteMapping` · `@PathVariable` · `@RequestParam` · `@RequestBody` · `@ResponseStatus` · `@CrossOrigin`

**Validation & errors**
`@Valid` / `@Validated` · `@NotNull` `@NotBlank` `@Size` `@Email` · `@ControllerAdvice` / `@RestControllerAdvice` · `@ExceptionHandler`

**Data / JPA**
`@Entity` · `@Table` · `@Id` · `@GeneratedValue` · `@Column` · `@OneToMany` / `@ManyToOne` / `@ManyToMany` · `@JoinColumn` · `@Query` · `@Transactional` · `@Modifying`

**Config & profiles**
`@Profile` · `@PropertySource` · `@ConditionalOnProperty` / `@ConditionalOnMissingBean` / `@ConditionalOnClass`

**Testing**
`@SpringBootTest` · `@WebMvcTest` · `@DataJpaTest` · `@MockBean` · `@TestConfiguration`

**Async / scheduling / caching / resilience**
`@EnableAsync` + `@Async` · `@EnableScheduling` + `@Scheduled` · `@EnableCaching` + `@Cacheable` / `@CacheEvict` · `@CircuitBreaker` / `@Retry` (Resilience4j)

> 💡 **Close with the mechanism, not more names:** "All of these are just metadata — the work is done by `BeanPostProcessor`s and proxies. That's why `@Transactional` or `@Async` **doesn't work on a self-invocation or a private method**: the call never leaves the object, so it never goes through the proxy."

---

## 4. `@Component` vs `@Service` annotation

**The honest technical answer first:** `@Service`, `@Repository` and `@Controller` are all **`@Component` under the hood** — they're meta-annotated with it. To the component scanner they behave identically: one scanned bean each.

```java
@Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME)
@Component                         // ← this is the whole difference, plus one behaviour below
public @interface Service { ... }
```

**So why do they exist?**

| Annotation | Layer | Anything extra? |
|---|---|---|
| `@Component` | Generic — anything Spring should manage | — |
| `@Service` | Business logic | **No extra behaviour** — pure intent/readability. AOP pointcuts can target it |
| `@Repository` | Data access | ✅ **Real behaviour:** `PersistenceExceptionTranslationPostProcessor` converts vendor exceptions (`SQLException`, Hibernate's) into Spring's `DataAccessException` hierarchy |
| `@Controller` | Web / MVC | ✅ Handler methods are detected by `RequestMappingHandlerMapping` |
| `@RestController` | REST | ✅ `@Controller` + `@ResponseBody` on every method |

> 🎯 **The three sentences that win this question:**
> 1. "They're all `@Component` — the scanner treats them the same."
> 2. "**`@Repository` is the one with real behaviour** — exception translation."
> 3. "`@Service` is intent. It documents the layer, it gives me a clean AOP pointcut (`@within(org.springframework.stereotype.Service)`), and on a team it stops business logic drifting into the controller."

⚠️ **Common follow-up:** *"Can I put `@Service` on my repository interface?"* — It would work (it's a component), but you'd lose exception translation and confuse every reader. With Spring Data you don't annotate the repository interface at all — the proxy is created by `@EnableJpaRepositories` / auto-configuration.

---

## 5. Multiple packages — how does Spring find all the annotations?

*They asked: "I have multiple packages in Spring, I want all of them read for annotations."*

**The default rule:** `@SpringBootApplication` implies `@ComponentScan` with **no arguments**, which scans **the package of the main class and every sub-package**. So the fix 90% of the time is just: *put the main class in the root package.*

```
com.priyanka.app          ← ApplicationMain.java lives here
 ├── controller
 ├── service
 ├── repository
 └── config                ← all scanned automatically
```

**When packages are outside that root**, four options:

**1. `scanBasePackages` on the main class — the usual answer**
```java
@SpringBootApplication(scanBasePackages = {"com.priyanka.app", "com.priyanka.common", "com.acme.shared"})
public class ApplicationMain { }
```

**2. `@ComponentScan` separately** (same effect, more explicit)
```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.priyanka.app", "com.acme.shared"})
public class ApplicationMain { }
```

**3. `basePackageClasses` — the type-safe version, and the one to name**
```java
@SpringBootApplication(scanBasePackageClasses = {ApplicationMain.class, SharedMarker.class})
```
> 💡 This is **refactor-safe** — renaming the package won't silently break the scan the way a `String` would. Mentioning it is a real differentiator.

**4. Filters, when you want only some of them**
```java
@ComponentScan(basePackages = "com.acme",
  includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = MyStereotype.class),
  excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,      pattern = "com\\.acme\\.legacy\\..*"))
```

**Two things component scan does *not* cover — say these and the answer is complete:**

| Also outside the main package | The annotation |
|---|---|
| JPA **entities** | `@EntityScan("com.acme.domain")` |
| Spring Data **repositories** | `@EnableJpaRepositories("com.acme.repo")` (or `@EnableMongoRepositories`) |

**And for a shared library jar — the *right* way:** don't scan someone else's package at all. Ship an **auto-configuration**: a `@AutoConfiguration` class listed in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (Boot 3; it was `spring.factories` in Boot 2). That's how every starter works, and it's the answer that shows you've built a shared module, not just consumed one.

> ⚠️ **Cost of over-scanning:** scanning `com` or a huge tree slows startup and can pull in beans you didn't want. Keep the roots tight.

---

## 6. `@Bean` vs stereotype annotations

*They asked: "read the bean Spring annotations" — i.e. how a bean gets into the container.*

**Two routes into the ApplicationContext:**

| | Stereotype (`@Component`/`@Service`/…) | `@Bean` in a `@Configuration` class |
|---|---|---|
| Who constructs it | Spring, by scanning + calling the constructor | **You**, in the method body |
| Applies to | **Your own classes** you can annotate | **Third-party classes** you can't annotate (`ObjectMapper`, `RestTemplate`, `DataSource`) |
| Bean name | Class name, decapitalized (`orderService`) | **The method name** |
| Conditional creation | Awkward | Easy — `if/else` inside the method, `@Conditional*` on it |
| Multiple beans of same type | One class = one bean | ✅ Several methods → several beans |

```java
@Configuration
public class AppConfig {

    @Bean                                   // bean name = "restTemplate"
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.setConnectTimeout(Duration.ofSeconds(2))
                      .setReadTimeout(Duration.ofSeconds(5))
                      .build();
    }

    @Bean @Primary
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    }
}
```

**The bean lifecycle** (a very common follow-up — know the order):
> instantiate → populate dependencies → `BeanNameAware`/`BeanFactoryAware` → `BeanPostProcessor.postProcessBeforeInitialization` → `@PostConstruct` → `InitializingBean.afterPropertiesSet()` / `initMethod` → **`postProcessAfterInitialization` (this is where the AOP proxy is created)** → *bean is ready* → … → `@PreDestroy` → `DisposableBean.destroy()`.

**Scopes:** `singleton` (default, one per context) · `prototype` (new instance per lookup — **and Spring does not destroy it**) · web: `request`, `session`, `application`, `websocket`.

> ⚠️ **The classic trap:** a `@Configuration` class is itself CGLIB-proxied, so calling `restTemplate()` from another `@Bean` method returns **the same singleton**, not a new object. Change it to `@Configuration(proxyBeanMethods = false)` and you get a *fresh* object each call. Knowing this distinguishes real usage from tutorial knowledge.

---

## 7. `@Autowired`, `@Qualifier` and `@Primary`

**How Spring resolves an injection point:** **by type first**. If exactly one bean matches → done. If **more than one** matches → `NoUniqueBeanDefinitionException`, unless you disambiguate.

```java
public interface PaymentService { void pay(BigDecimal amt); }

@Service("card") class CardPaymentService  implements PaymentService { }
@Service          class UpiPaymentService  implements PaymentService { }
```

**Three ways to disambiguate:**

**1. `@Primary` — the default winner**
```java
@Service @Primary
class UpiPaymentService implements PaymentService { }   // injected whenever nothing else is specified
```
Global: "when in doubt, use this one."

**2. `@Qualifier` — the explicit pick at the injection point**
```java
@Service
public class CheckoutService {
    private final PaymentService payment;
    public CheckoutService(@Qualifier("card") PaymentService payment) { this.payment = payment; }
}
```
Local: "at *this* place, use that one." **`@Qualifier` beats `@Primary`.**

**3. Field name = bean name (the fallback nobody mentions)** — if the parameter/field is named `upiPaymentService`, Spring matches it by name. Works, but it's fragile: rename the variable and the wiring changes.

**A custom qualifier is the clean version** (no magic strings):
```java
@Qualifier("card") @Retention(RUNTIME) public @interface CardPayment {}

@Service @CardPayment class CardPaymentService implements PaymentService {}
...
public CheckoutService(@CardPayment PaymentService payment) { ... }
```

**Injecting *all* of them** — worth saying, it's how strategy patterns are actually built:
```java
@Service
public class PaymentRouter {
    private final Map<String, PaymentService> byName;   // key = bean name
    private final List<PaymentService> all;             // ordered by @Order / Ordered
    public PaymentRouter(Map<String, PaymentService> byName, List<PaymentService> all) { ... }
}
```

> 🎯 **Precedence, in one line:** `@Qualifier` > `@Primary` > matching bean name > `NoUniqueBeanDefinitionException`.

**On `@Autowired` itself:**
- **Constructor injection is the answer** — since Spring 4.3 `@Autowired` is **optional** on a single constructor. It gives you `final` fields, guaranteed non-null dependencies, and a class you can `new` in a unit test without Spring.
- **Field injection** (`@Autowired` on a field) is discouraged: untestable without reflection, hides a growing dependency list, can't be `final`. IntelliJ literally warns on it.
- `@Autowired(required = false)` or `Optional<Foo>` / `ObjectProvider<Foo>` for optional deps.
- **`@Resource` (JSR-250) matches by *name* first**, `@Autowired` by *type* first, `@Inject` (JSR-330) behaves like `@Autowired`. That comparison is a frequent follow-up.

---

## 8. `CrudRepository` vs `JpaRepository` (and the whole hierarchy)

**The hierarchy — draw it, don't list it:**

```
Repository<T,ID>                    (marker — no methods)
      ↑
CrudRepository<T,ID>                save, saveAll, findById, existsById, findAll,
      ↑                             findAllById, count, deleteById, delete, deleteAll
ListCrudRepository<T,ID>            (Spring Data 3+) same but returns List instead of Iterable
      ↑
PagingAndSortingRepository<T,ID>    findAll(Sort), findAll(Pageable)
      ↑
JpaRepository<T,ID>                 + JPA-specific: flush(), saveAndFlush(), saveAllAndFlush(),
                                      deleteAllInBatch(), deleteAllByIdInBatch(),
                                      getReferenceById(), and List-returning findAll()
```

| | `CrudRepository` | `JpaRepository` |
|---|---|---|
| Persistence tech | **Technology-agnostic** (JPA, Mongo, Redis, Cassandra…) | **JPA only** |
| `findAll()` returns | `Iterable<T>` | ✅ `List<T>` |
| Paging & sorting | ❌ | ✅ (inherited from `PagingAndSortingRepository`) |
| Batch delete | ❌ (`deleteAll` = one delete per row) | ✅ `deleteAllInBatch()` — **a single SQL statement** |
| Flush control | ❌ | ✅ `flush()`, `saveAndFlush()` |
| Lazy proxy | ❌ | ✅ `getReferenceById()` (the old `getOne`) |

**What to actually say:**
> "`JpaRepository` extends `PagingAndSortingRepository` which extends `CrudRepository`, so it's a superset. I use `JpaRepository` on a JPA project because I want `List` returns, `Pageable` and batch deletes. I'd deliberately drop down to `CrudRepository` if I wanted to keep the door open to switching the persistence technology, or to keep the API surface small so nobody calls `deleteAllInBatch()` by accident."

**Where do the implementations come from?** You never write one. `@EnableJpaRepositories` (auto-configured by Boot) creates a **JDK dynamic proxy** per interface, backed by `SimpleJpaRepository`; `RepositoryFactorySupport` resolves each method to either a **derived query** (parsed from the method name) or a declared `@Query`.

**The three query styles:**
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // 1. derived query — parsed from the method name
    List<Employee> findByDepartmentAndSalaryGreaterThanOrderBySalaryDesc(String dept, double salary);

    // 2. JPQL
    @Query("select e from Employee e where e.department = :dept and e.salary > :min")
    List<Employee> search(@Param("dept") String dept, @Param("min") double min);

    // 3. native SQL
    @Query(value = "select * from employee where salary > ?1", nativeQuery = true)
    List<Employee> rawSearch(double min);

    @Modifying @Transactional            // required for update/delete @Query
    @Query("update Employee e set e.salary = e.salary * 1.1 where e.department = :d")
    int giveRaise(@Param("d") String dept);
}
```
> ⚠️ **`@Modifying` needs `clearAutomatically`/`flushAutomatically` awareness** — a bulk JPQL update **bypasses the persistence context**, so entities already loaded are stale. That's the depth follow-up. More: **[24 — ORM / JPA / Hibernate](./24-orm-jpa-hibernate.md)**.

---

## 9. Pagination methods

**Why paginate at all (lead with this):** `findAll()` on a 2-million-row table loads 2 million entities into heap and serializes them to the client. Pagination pushes `LIMIT`/`OFFSET` into SQL, so the DB returns 20 rows.

**The method signature is the whole answer:**
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Page<Employee>  findByDepartment(String department, Pageable pageable);   // + a COUNT query
    Slice<Employee> findBySalaryGreaterThan(double salary, Pageable pageable); // no COUNT query
    List<Employee>  findByCity(String city, Pageable pageable);               // just the window
}
```

**Controller:**
```java
@GetMapping("/employees")
public Page<EmployeeDto> list(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "salary,desc") String[] sort) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("salary")));
    return repo.findAll(pageable).map(mapper::toDto);   // Page.map keeps the metadata
}
```
> 💡 **Boot resolves it for you.** Just declare `Pageable pageable` as a controller parameter — `PageableHandlerMethodArgumentResolver` binds `?page=0&size=20&sort=salary,desc` automatically. Add `@PageableDefault(size = 20, sort = "salary", direction = DESC)` for defaults. Saying this shows you've shipped it.

**Sorting**
```java
Sort.by("salary").descending().and(Sort.by("name").ascending());
PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "salary"));
```

**The methods you read off a `Page`:**
`getContent()` · `getTotalElements()` · `getTotalPages()` · `getNumber()` (current page) · `getSize()` · `getNumberOfElements()` · `hasNext()` / `hasPrevious()` · `isFirst()` / `isLast()` · `nextPageable()` · `map(Function)`.

> ⚠️ **Two traps worth naming — this is where the follow-up goes:**
> 1. **`OFFSET` gets slower the deeper you go.** `LIMIT 20 OFFSET 1000000` still makes the DB walk a million rows. For deep paging / infinite scroll use **keyset (cursor) pagination**: `where (salary, id) < (:lastSalary, :lastId) order by salary desc, id desc limit 20`.
> 2. **Pagination + `JOIN FETCH` on a collection = `HHH000104: firstResult/maxResults specified with collection fetch; applying in memory`** — Hibernate loads *everything* and pages in memory. Fix: page the IDs first, then fetch the collection for those IDs (two queries), or use `@EntityGraph`/`@BatchSize`.
> 3. **Always sort by something unique** (add `id` as a tiebreaker) or rows can repeat/disappear across pages.

---

## 10. What classes are involved in pagination?

*The literal question they asked: "what are the classes created in pagination".*

| Type | Kind | Role |
|---|---|---|
| **`Pageable`** | interface | The **request**: page number, page size, sort. `Pageable.unpaged()` for "no paging" |
| **`PageRequest`** | class — implements `Pageable` | The standard implementation. `PageRequest.of(page, size)` / `of(page, size, Sort)` |
| **`Sort`** | class | Sort spec — a list of `Sort.Order` (property + `Direction` + null handling) |
| **`Sort.Order`** / **`Sort.Direction`** | class / enum | One property's direction; `ASC` / `DESC` |
| **`Page<T>`** | interface (extends `Slice<T>`) | The **response with total count** — content + `totalElements` + `totalPages` |
| **`PageImpl<T>`** | class — implements `Page` | The concrete result Spring Data returns (and what you `new` when paging a list manually) |
| **`Slice<T>`** | interface | Content + **`hasNext()` only, no total** — one query instead of two |
| **`SliceImpl<T>`** | class | Concrete `Slice` |
| **`Window<T>` / `ScrollPosition`** | Spring Data 3.1+ | Keyset/offset **scrolling** API — the modern deep-paging answer |
| **`PagedModel` / `PagedResourcesAssembler`** | Spring HATEOAS | Wraps a `Page` with paging links for a REST API |
| `PageableHandlerMethodArgumentResolver` | class | The MVC resolver that builds `Pageable` from query params |

```java
Pageable pageable = PageRequest.of(0, 20, Sort.by("salary").descending());
Page<Employee> page = repo.findAll(pageable);

page.getTotalElements();     // 137   ← costs an extra SELECT COUNT(*)
page.getTotalPages();        // 7
page.getNumber();            // 0     ← pages are ZERO-indexed
page.hasNext();              // true
```

> 🎯 **The differentiator answer: `Page` vs `Slice`.**
> "`Page` runs **two** queries — the window and a `COUNT(*)` — because it has to report `totalElements`. `Slice` runs **one**: it fetches `size + 1` rows and uses the extra row to answer `hasNext()`. So a numbered pager needs `Page`; an infinite-scroll feed should use `Slice` and skip the count entirely. On a big table that count query is often the slowest part of the request."

⚠️ **`page` is zero-indexed.** UIs are one-indexed. That off-by-one is a real bug and a favourite follow-up.

---

## 11. Communication in microservices

**Split it into two axes first — that framing *is* the answer:**

### Synchronous (request/response, caller waits)

| Option | Notes |
|---|---|
| **REST over HTTP** | Default. Simple, debuggable, cache-friendly |
| **`RestTemplate`** | ⚠️ **In maintenance mode** — say this, it dates you otherwise |
| **`WebClient`** | Reactive, non-blocking, works fine in a blocking app via `.block()`; supports timeouts and retries natively |
| **`RestClient`** | Spring 6.1+ — the synchronous, fluent replacement for `RestTemplate` |
| **OpenFeign** | Declarative: an interface + `@FeignClient`. Integrates with Eureka + Resilience4j |
| **gRPC** | Binary, HTTP/2, contract-first with protobuf. Fast + strongly typed; harder to debug |
| **GraphQL** | One endpoint, client picks the fields — good for a BFF |

```java
@FeignClient(name = "order-service", fallback = OrderClientFallback.class)
public interface OrderClient {
    @GetMapping("/orders/{id}")
    OrderDto getOrder(@PathVariable Long id);
}
```

### Asynchronous (event/message, caller doesn't wait)

**Kafka / RabbitMQ / SQS / SNS** — publish an event, consumers react.
- **Decoupling**: the producer doesn't know who consumes.
- **Resilience**: the broker buffers when the consumer is down.
- **Cost**: eventual consistency, ordering and idempotency become *your* problem.

```java
@KafkaListener(topics = "order-created", groupId = "billing")
public void onOrderCreated(OrderCreatedEvent event) { ... }
```

### The supporting cast (name these — it's what "microservices experience" sounds like)
- **Service discovery** — Eureka / Consul / Kubernetes DNS, so you call `http://order-service` not an IP.
- **API Gateway** — Spring Cloud Gateway: routing, auth, rate limiting, one entry point.
- **Config Server** — centralized configuration ([Q14](#14-dev--qa--prod--different-properties-and-a-different-db-per-environment)).
- **Resilience4j** — circuit breaker, retry, timeout, bulkhead, rate limiter ([Q13](#13-two-microservices-a-network-failure-in-between--a-errors-how-do-you-inform-b)).
- **Distributed tracing** — Micrometer Tracing + Zipkin/Jaeger; one trace ID across every hop.
- **Saga** — distributed transactions by compensation (choreography via events, or orchestration via a coordinator), because there is no 2-phase commit across services.

> 🎯 **The one-sentence rule to close on:** "**Synchronous when I need an answer to serve this request; asynchronous when I just need the other service to eventually know.** Every synchronous call I add is another thing that can take my service down with it — which is exactly the failure they asked about next."

More: **[08 — Microservices Basics](./08-microservices-basics.md)**.

---

## 12. How do you handle error cascading?

*Asked as "how to error case casting" — this is **cascading failure**: A is slow/down, B blocks on A, B's threads pile up, B goes down, and it walks up the chain.*

**The mechanism to describe:** service A hangs → B's calls to A never return → B's thread pool fills with blocked threads → B stops serving *unrelated* requests → B's callers time out → the whole graph is down because of one leaf. That's a cascading failure, and the cure is **failing fast instead of waiting**.

**The five defences, in order of importance:**

**1. Timeouts — the non-negotiable one.**
> "A call without a timeout is an unbounded resource leak. The default `RestTemplate` has *no* timeout — it waits forever."
```java
@Bean
RestClient restClient(RestClient.Builder b) {
    var f = new SimpleClientHttpRequestFactory();
    f.setConnectTimeout(Duration.ofSeconds(2));
    f.setReadTimeout(Duration.ofSeconds(3));
    return b.requestFactory(f).build();
}
```

**2. Circuit breaker** — after N% failures the breaker **opens** and calls fail instantly without touching the network. **CLOSED → OPEN → HALF_OPEN** (a few trial calls) → back to CLOSED or OPEN.
```java
@CircuitBreaker(name = "orderService", fallbackMethod = "orderFallback")
@Retry(name = "orderService")
@TimeLimiter(name = "orderService")
public OrderDto getOrder(Long id) { return orderClient.getOrder(id); }

private OrderDto orderFallback(Long id, Throwable t) {
    log.warn("order-service unavailable, serving cached/degraded response", t);
    return OrderDto.unavailable(id);        // degrade, don't die
}
```
```yaml
resilience4j.circuitbreaker.instances.orderService:
  slidingWindowSize: 20
  failureRateThreshold: 50
  waitDurationInOpenState: 10s
  permittedNumberOfCallsInHalfOpenState: 3
```

**3. Retry — with exponential backoff + jitter, and only for *idempotent* operations.**
> ⚠️ "Naive retries make a cascade **worse** — you're adding load to a service that's already failing. Retry only safe operations, cap the attempts, and put the retry *inside* the circuit breaker."

**4. Bulkhead** — isolate thread pools/semaphores per downstream, so a hang on the reporting service can't consume the threads that serve checkout. (`@Bulkhead(name = "...")`)

**5. Fallback / graceful degradation** — cached data, a default, a partial response, or a clear "temporarily unavailable" flag in the payload. **The user gets a degraded feature, not a 500 page.**

**Plus the plumbing that makes errors legible:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ProblemDetail> handle(ServiceUnavailableException e) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        pd.setProperty("traceId", MDC.get("traceId"));
        return ResponseEntity.status(503).body(pd);          // RFC 7807
    }
}
```
Use **`ProblemDetail`** (RFC 7807, built into Spring 6) and propagate a **trace ID** on every hop, so one failure can be followed across five services.

> 🗣️ **The sentence that lands it:** "Cascading failure is a **latency** problem before it's an error problem. Slow is worse than down, because down at least fails fast. So: timeout everything, open a breaker when a dependency misbehaves, and degrade the feature instead of the service."

---

## 13. Two microservices, a network failure in between — A errors, how do you inform B?

**This was the sharpest question of the round.** Start by making the question precise, because the right answer depends on which one they mean:

> "Two readings — do you mean **A calls B and the call fails**, so how does A find out and cope? Or **A has failed and B needs to know about it**? Let me take both."

### Reading A — the call fails: how does the caller cope?

The caller **cannot distinguish** these three cases from a timeout:
1. the request never arrived,
2. it arrived and B processed it but the response was lost,
3. B genuinely errored.

That is the fundamental point, and stating it is what separates a real answer from a textbook one. Consequences:

| Concern | What you do |
|---|---|
| Detect | Connect + read **timeouts** (never infinite) |
| Recover | **Retry with backoff + jitter** — but only if the operation is **idempotent** |
| Make it safe to retry | **Idempotency key** — the caller sends `Idempotency-Key: <uuid>`, B stores it and returns the original result on a repeat. This is how payment APIs do it |
| Stop hammering | **Circuit breaker** — fail fast while B is unhealthy |
| Keep serving | **Fallback**: cached value, default, or a partial response |
| Don't lose the work | Write the intent to an **outbox** / queue and complete it later |
| See it | Trace ID + metrics + an alert when the breaker opens |

### Reading B — how do you *reliably* inform the other service?

**The honest answer: a synchronous HTTP call is the wrong tool for "tell B something reliably", because the network is exactly the thing that failed.** You need to make the message survive the failure:

**1. Asynchronous messaging (Kafka / RabbitMQ / SQS).** A publishes an event; the broker persists it; B consumes when it's healthy. The network failure becomes a delay, not a lost message. Consumers must be **idempotent** (dedupe on event ID) because brokers are at-least-once.

**2. The Transactional Outbox pattern — the answer that wins this question.**
> "The dangerous case is: A commits its DB transaction and then the publish fails, so A's state says one thing and B never hears about it. You can't put a DB commit and a broker publish in one atomic transaction. So A writes the event into an **`outbox` table in the same local transaction** as the business change. A separate relay (a poller, or CDC with Debezium) reads that table and publishes to the broker, retrying until it succeeds. Atomicity is preserved with a single local transaction, and the message is guaranteed to go out **at least once**."

```java
@Transactional
public void placeOrder(Order order) {
    orderRepo.save(order);                                  // business state
    outboxRepo.save(new OutboxEvent("OrderPlaced", toJson(order)));  // same transaction
}   // both commit or neither does — the relay publishes later
```

**3. Dead Letter Queue** — a message that fails repeatedly goes to a DLQ instead of blocking the partition, and gets alerted on and replayed after a fix.

**4. If it must stay synchronous:** propagate a meaningful failure — `503` with `Retry-After`, a `ProblemDetail` body with the trace ID, and an error code B can branch on. Never swallow it and return `200`.

**5. Saga / compensation** — if A succeeded and B can't be reached at all, run the **compensating transaction** (cancel the reservation, refund the payment) so the system converges back to a consistent state. There's no 2PC across services; you get consistency by compensation.

**6. Health + observability** — Actuator health endpoints, the breaker's state exposed as a metric, tracing across the hop, and an alert on breaker-open so a human knows.

> 🎯 **The 30-second version if they want it short:**
> "I'd set timeouts and wrap the call in a circuit breaker with a fallback, so A degrades instead of hanging. For actually *informing* B I wouldn't rely on a synchronous call at all — I'd publish an event through Kafka using the **transactional outbox** pattern, so the event is written in the same DB transaction as the business change and a relay retries until the broker accepts it. B consumes it idempotently when it's back, failures land in a DLQ, and anything already committed that can't complete gets a compensating transaction."

---

## 14. Dev / QA / Prod — different properties and a different DB per environment

*Asked as: "QA and prod and dev all the properties — one environment's DB data has been changed, how is that handled in Spring Boot? Different properties in different environments."*

**Level 1 — profile-specific property files (the baseline answer).**

```
src/main/resources/
 ├── application.yml            ← common to every environment
 ├── application-dev.yml
 ├── application-qa.yml
 └── application-prod.yml
```
```yaml
# application.yml
spring:
  application.name: employee-service
  profiles.active: ${SPRING_PROFILES_ACTIVE:dev}
  jpa.open-in-view: false
```
```yaml
# application-qa.yml
spring:
  datasource:
    url: jdbc:postgresql://qa-db:5432/employees
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa.hibernate.ddl-auto: validate
logging.level.root: INFO
```
Activate it — **never by editing the file**: `--spring.profiles.active=qa`, or `SPRING_PROFILES_ACTIVE=qa` as an env var, or `-Dspring.profiles.active=qa`.

**Level 2 — profile-specific *beans*.**
```java
@Bean @Profile("dev")               DataSource h2()   { ... }
@Bean @Profile({"qa","prod"})       DataSource pg()   { ... }
@Bean @Profile("!prod")             SeedDataLoader seed() { ... }   // never seed prod
```
Also `@ConfigurationProperties` for typed config, and profile groups:
`spring.profiles.group.prod: prod,monitoring,cloud`.

**Level 3 — the ordering rule (this is the real answer to "how is it handled").**
Spring Boot's **externalized configuration order** — later wins:
> defaults in code → `application.yml` → `application-{profile}.yml` → **OS environment variables** → **command-line arguments** → config server / Vault.

So you never rebuild the artifact per environment. **One jar, many environments** — the environment supplies the values. `SPRING_DATASOURCE_URL` as an env var overrides everything in the jar (relaxed binding maps `SPRING_DATASOURCE_URL` → `spring.datasource.url`).

**Level 4 — centralized config for microservices.**
**Spring Cloud Config Server** backed by a git repo (`employee-service-qa.yml`), so config is versioned, auditable and shared across services; `/actuator/refresh` or Spring Cloud Bus re-reads it **without a restart**, and `@RefreshScope` beans get rebuilt. In Kubernetes the equivalent is **ConfigMaps + Secrets**.

**Level 5 — secrets and schema, the two things a senior adds:**
- **Secrets never live in the repo.** Vault / AWS Secrets Manager / K8s Secrets / env vars. `spring.datasource.password` in git is an instant red flag.
- **"The DB data/schema changed" → schema migrations.** **Flyway** or **Liquibase**, versioned SQL in source control, applied automatically on startup in order, so dev/QA/prod converge to the same schema. And **`spring.jpa.hibernate.ddl-auto`**: `update` in dev at most, **`validate` in QA/prod, never `update` or `create-drop`** — that's the answer to "the DB changed and now things break".

> 🎯 **Summary sentence:** "Same artifact everywhere. Profiles pick the property file, environment variables override anything environment-specific, secrets come from a secret manager, and **Flyway** keeps the schema identical across dev, QA and prod so a change in one environment is a versioned migration, not a surprise."

---

# Part 4 — Angular

## 15. `ngOnInit` vs constructor

*Repeat — lifecycle hooks have now come up in **five** separate rounds.*

| | `constructor` | `ngOnInit` |
|---|---|---|
| Whose | **TypeScript/JS** — runs when the class is instantiated | **Angular** — a lifecycle hook (`OnInit`) |
| When | At creation, **before** inputs are set | **After the first `ngOnChanges`**, i.e. once `@Input`s are bound |
| Use for | **Dependency injection only** — assigning services to fields | Initialization: API calls, subscriptions, form setup, reading `@Input`s |
| `@Input()` values | ❌ `undefined` | ✅ available |
| Runs how often | Once | Once |
| Testable | Hard to control | ✅ Called explicitly in tests, easy to control |

```ts
@Component({ selector: 'app-employee', standalone: true, templateUrl: './employee.html' })
export class EmployeeComponent implements OnInit, OnDestroy {
  @Input() employeeId!: number;
  employee?: Employee;
  private destroy$ = new Subject<void>();

  constructor(private employeeService: EmployeeService) {
    // console.log(this.employeeId) → undefined. DI only.
  }

  ngOnInit(): void {
    this.employeeService.get(this.employeeId)        // employeeId is set now
      .pipe(takeUntil(this.destroy$))
      .subscribe(e => this.employee = e);
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }
}
```

**The "why does it matter" sentence:** "Putting an HTTP call in the constructor makes the component hard to test and — more importantly — it runs before `@Input`s exist, so `employeeId` is `undefined` and you fetch the wrong thing. Separating construction from initialization is exactly why the hook exists."

**Full hook order** (worth having ready — it's the guaranteed follow-up):
`constructor` → `ngOnChanges` → `ngOnInit` → `ngDoCheck` → `ngAfterContentInit` → `ngAfterContentChecked` → `ngAfterViewInit` → `ngAfterViewChecked` → (repeat the Check hooks on every CD cycle) → `ngOnDestroy`.

> 💡 **Modern add-on:** with signals, `inject()` in a field initializer replaces constructor injection, and `effect()` / `afterNextRender()` cover cases that used to need `ngAfterViewInit`. `DestroyRef` + `takeUntilDestroyed()` replaces the `destroy$` subject. Mentioning that shows you're current.

---

## 16. Decorators

**Definition:** a decorator is a **function that attaches metadata to a class, property, method or parameter**. Angular's compiler reads that metadata to know how to build and wire things. In TypeScript they're `@`-prefixed; Angular's own are plain functions that store metadata (historically via `reflect-metadata`, now compiled ahead-of-time by the Angular compiler).

**The four kinds, with Angular's:**

| Kind | Angular examples |
|---|---|
| **Class** | `@Component`, `@Directive`, `@Pipe`, `@Injectable`, `@NgModule` |
| **Property** | `@Input`, `@Output`, `@ViewChild`, `@ViewChildren`, `@ContentChild`, `@HostBinding` |
| **Method** | `@HostListener` |
| **Parameter** | `@Inject`, `@Optional`, `@Self`, `@SkipSelf`, `@Host` |

```ts
@Component({
  selector: 'app-counter',
  standalone: true,
  template: `<button (click)="inc()">{{ count }}</button>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CounterComponent {
  @Input() start = 0;
  @Output() changed = new EventEmitter<number>();
  @HostBinding('class.active') isActive = false;
  @HostListener('mouseenter') onEnter() { this.isActive = true; }
}
```

**A custom decorator — good to have if they push:**
```ts
export function LogTime() {
  return function (target: any, key: string, descriptor: PropertyDescriptor) {
    const original = descriptor.value;
    descriptor.value = function (...args: any[]) {
      const t0 = performance.now();
      const result = original.apply(this, args);
      console.log(`${key} took ${performance.now() - t0}ms`);
      return result;
    };
    return descriptor;
  };
}
```

> ⚠️ Two precision points that read as senior:
> - **`@Injectable({ providedIn: 'root' })`** is what makes a service **tree-shakable** — if nothing injects it, it's dropped from the bundle. That's better than listing it in a module's `providers`.
> - Angular's decorators are TypeScript's **legacy/experimental** decorators (`experimentalDecorators`), not the newer ECMAScript stage-3 proposal — and Angular 16+ is moving away from them anyway (`input()`, `output()`, `viewChild()` **signal functions** replace `@Input`/`@Output`/`@ViewChild`).

---

## 17. Interceptor

*Repeat — interceptors have now been asked in **four** rounds.*

**Definition:** an `HttpInterceptor` sits in the `HttpClient` pipeline and can inspect or transform **every outgoing request and every incoming response**, in one place, without touching a single service.

**Functional interceptor (Angular 15+, the modern form):**
```ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token;

  // requests are IMMUTABLE — you must clone
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    retry({ count: 2, delay: 1000 }),
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) auth.handleExpiredToken();     // see Q18
      return throwError(() => err);
    })
  );
};

bootstrapApplication(AppComponent, {
  providers: [provideHttpClient(withInterceptors([authInterceptor, loadingInterceptor]))]
});
```

**Class-based (the older form, still very common in real codebases):**
```ts
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req.clone({ setHeaders: { Authorization: `Bearer ${this.token}` } }));
  }
}
// NgModule: { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
```

**What they're used for — the list they want:**
| Use | How |
|---|---|
| **Auth** | Attach the JWT to every request |
| **Error handling** | Central `catchError` → toast, log, redirect on 401/403 |
| **Loading spinner** | Increment a counter on request, decrement on finalize |
| **Logging / timing** | Log method, URL, duration, status |
| **Retry** | `retry({ count, delay })` on transient failures |
| **Caching** | Return a cached `HttpResponse` for GETs |
| **Headers** | Correlation ID, locale, tenant, API version |
| **URL rewriting** | Prefix a relative URL with the environment's API base |

> ⚠️ **The three details that separate a used-it answer from a read-it one:**
> 1. **`HttpRequest` is immutable — you *must* `req.clone()`.** Mutating it does nothing.
> 2. **`multi: true`** — without it your interceptor *replaces* the array instead of appending.
> 3. **Order matters and it's asymmetric:** interceptors run in registration order on the way **out**, and in **reverse** order on the way **back**. So put the auth interceptor before the logging one if you want the header logged.

---

## 18. The token has expired — how do you notify the user?

**Answer as a flow, not a feature.** Three signals that a token expired, and a chosen response for each:

**1. Detect it reactively — the 401 in an interceptor** (the primary mechanism):
```ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const toast = inject(ToastService);

  return next(addToken(req, auth.token)).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        return auth.refreshToken().pipe(                    // 2. try a silent refresh first
          switchMap(newToken => next(addToken(req, newToken))),
          catchError(() => {                                // refresh also failed → really expired
            auth.logout();
            toast.warn('Your session has expired. Please sign in again.');
            router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
            return EMPTY;
          })
        );
      }
      return throwError(() => err);
    })
  );
};
```

**2. Detect it proactively — decode the JWT `exp` claim.** The token carries `exp` (seconds since epoch), so the client knows when it dies without asking the server:
```ts
const { exp } = jwtDecode<{ exp: number }>(token);
const msLeft = exp * 1000 - Date.now();
timer(msLeft - 60_000).subscribe(() => this.warnSessionExpiring());   // warn 1 min before
```
That gives you the **"Your session expires in 60 seconds — [Stay signed in]"** dialog, which is the *good* UX answer.

**3. Guard the route** — `CanActivate` checks validity before navigation, so an expired user never reaches a protected page and never sees a broken screen:
```ts
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService); const router = inject(Router);
  return auth.isTokenValid()
    ? true
    : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
```

**Handle the refresh stampede — the follow-up that catches people out:**
> "If five requests fail with 401 at the same moment, a naive interceptor fires five refresh calls. I gate it with a `BehaviorSubject<string|null>` + `isRefreshing` flag: the first 401 triggers the refresh, the rest **wait on the subject** with `filter(t => t !== null), take(1), switchMap(retry)`. Same technique as the duplicate-click problem Altimetrik asked me last time."

**What the user should actually see (say this — it's the part they asked):**
- A **non-blocking toast/snackbar**: *"Your session has expired. Please sign in again."*
- Redirect to `/login` **with `returnUrl`**, so after re-login they land back where they were — never on the home page.
- **Preserve unsaved work** (draft in a store/`sessionStorage`) so re-authenticating doesn't lose their form.
- Clear the stale token from storage so nothing retries with it.

> 🔐 **Security second sentence:** "Access tokens should be short-lived (~15 min) with a refresh token in an **httpOnly, Secure, SameSite cookie** rather than `localStorage`, because `localStorage` is readable by any XSS. And the client-side `exp` check is UX only — the **server** is the authority; it must reject the expired token regardless."

---

## 19. How do you redirect to a page?

**Five ways — name the one you'd use and why.**

**1. Declarative, in the template:**
```html
<a routerLink="/employees" [queryParams]="{ page: 1 }" routerLinkActive="active">Employees</a>
<a [routerLink]="['/employee', employee.id]">Details</a>
```

**2. Programmatically, in a component/service:**
```ts
private router = inject(Router);

this.router.navigate(['/employee', id]);                          // array = segments
this.router.navigate(['../sibling'], { relativeTo: this.route }); // relative
this.router.navigateByUrl('/employees?page=2#top');               // absolute URL string
this.router.navigate(['/login'], { queryParams: { returnUrl }, replaceUrl: true });
```
> `replaceUrl: true` replaces the history entry — use it on a login redirect so **Back** doesn't return to the dead page.

**3. In the route config — a redirect route:**
```ts
export const routes: Routes = [
  { path: '',    redirectTo: '/dashboard', pathMatch: 'full' },   // pathMatch is required here
  { path: 'employees', loadComponent: () => import('./employees/list').then(m => m.ListComponent) },
  { path: '**', component: NotFoundComponent },                    // wildcard LAST
];
```
> ⚠️ **`pathMatch: 'full'` on the empty-path redirect** — with the default `'prefix'`, `''` matches *every* URL and you get an infinite redirect loop. That's a classic interview trap.

**4. From a guard — returning a `UrlTree` (the preferred modern way):**
```ts
return auth.isLoggedIn() ? true : router.createUrlTree(['/login']);
```
Cleaner than calling `router.navigate()` *and* returning `false`, because it cancels the current navigation atomically.

**5. Leaving the app entirely:** `window.location.href = '...'` — only for an external URL or an OAuth handoff; it does a full page reload and destroys the SPA state.

> 💡 **Second sentence:** "I'd add a `CanDeactivate` guard on forms so a redirect can't silently discard unsaved changes, and I subscribe to `Router.events` (`NavigationStart`/`NavigationEnd`) for a global loading bar and analytics."

---

## 20. Data binding

*Repeat topic.* **Definition:** data binding synchronizes the **component class** and the **template**, so you never touch the DOM by hand.

**The four types — this is the whole answer, learn the table:**

| Type | Syntax | Direction | Example |
|---|---|---|---|
| **Interpolation** | `{{ }}` | Component → View | `<h1>{{ employee.name }}</h1>` |
| **Property binding** | `[prop]` | Component → View | `<img [src]="imageUrl">` `<button [disabled]="!form.valid">` |
| **Event binding** | `(event)` | View → Component | `<button (click)="save()">` `(input)="onInput($event)"` |
| **Two-way binding** | `[(ngModel)]` / `[(x)]` | ↔ Both | `<input [(ngModel)]="name">` |

**Two-way binding is not magic — say this:**
```html
<!-- these two are exactly equivalent — "banana in a box" is just sugar -->
<input [(ngModel)]="name">
<input [ngModel]="name" (ngModelChange)="name = $event">
```
And it works on **your own** component if you follow the naming convention: an `@Input() x` plus an `@Output() xChange` gives you `[(x)]`.
```ts
@Input() count = 0;
@Output() countChange = new EventEmitter<number>();   // must be <inputName>Change
// parent: <app-counter [(count)]="total"></app-counter>
```

**Precision points:**
- `[disabled]="x"` binds the **DOM property**; `disabled="{{x}}"` sets the **attribute** — different things. For attributes with no DOM property, use `[attr.colspan]`, plus `[class.active]`, `[style.width.px]`.
- **Interpolation is sanitized** — Angular escapes HTML, which is your default XSS protection.
- ❌ **Don't call methods in a template** (`{{ getTotal() }}`) — it re-runs on **every** change-detection cycle. Use a pure pipe, a precomputed field, `async` on an observable, or a **`computed()` signal**.
- `[(ngModel)]` needs `FormsModule` and belongs to **template-driven** forms; reactive forms use `formControlName` instead — don't mix them on one control.

> 💡 **Modern closer:** "With **signals** (Angular 16+) the read side becomes `{{ count() }}`, `model()` gives a two-way signal input, and change detection can be **zoneless** — Angular knows exactly which signal changed instead of checking the whole tree." Full file: **[25 — Angular Binding & Forms](./25-angular-binding-forms.md)**.

---

## 21. Lazy loading

*Repeat topic.* **Definition:** loading a feature's JavaScript **only when the user navigates to it**, instead of shipping everything in the initial bundle. The Angular CLI splits each lazy route into its own **chunk**.

**Standalone components (Angular 15+, current):**
```ts
export const routes: Routes = [
  { path: 'employees',
    loadChildren: () => import('./employees/employees.routes').then(m => m.EMPLOYEE_ROUTES) },

  { path: 'profile',
    loadComponent: () => import('./profile/profile.component').then(m => m.ProfileComponent) },
];
```

**NgModule style (older codebases):**
```ts
{ path: 'admin', loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule) }
```
> ⚠️ **Never also import the lazy module in `AppModule`** — that eagerly bundles it and silently kills the lazy loading. That's the single most common real-world mistake, and a great thing to volunteer.

**Preloading — the nuance that shows depth:**
```ts
provideRouter(routes, withPreloading(PreloadAllModules))
```
> "Plain lazy loading trades a fast first paint for a delay on the *first* navigation into the feature. `PreloadAllModules` gets both: boot with the small bundle, then quietly download the rest while the user is idle. For a big app I'd write a **custom preloading strategy** that preloads only routes flagged `data: { preload: true }`, so the admin module isn't downloaded for regular users."

**Benefits:** smaller initial bundle → faster **FCP/TTI**, less parse/execute on mobile, per-feature caching (deploying one feature invalidates one chunk), and it maps cleanly onto team boundaries.

**Disadvantages — always give one, this exact follow-up was asked at Photon:**
- A **delay on first navigation** into the feature (mitigate with preloading + a loading indicator).
- **Duplicated dependencies** across chunks if shared code isn't factored into a shared module/library.
- **A service `providedIn` a lazy module gets its own instance** in that module's injector — not the root singleton you might expect.
- Harder debugging: a chunk can 404 after a redeploy (`ChunkLoadError`) — handle it with an error handler that reloads the page.

**Beyond routes:**
- `@defer` blocks (Angular 17+): `@defer (on viewport) { <app-heavy /> } @placeholder { ... }` — deferred loading at the **template** level, no route needed.
- `ngx-loadable`/dynamic `import()` for a heavy library (charting, PDF) loaded on demand.
- Images: `<img ngSrc="..." loading="lazy">` with `NgOptimizedImage`.

---

# Part 5 — Live coding

## 22. Live coding — highest-paid employee in each department

> ⚠️ **This is the *same* problem Altimetrik asked you in the first round** (see [26 §Altimetrik Q1](./26-companies-asked-questions.md#-altimetrik--level-1-round-java)) — and the SQL version was asked at Mphasis. **It is now a 3-round repeat. Type it from memory until it's automatic.**

**The given data:**
```java
List<Employee> employees = Arrays.asList(
    new Employee(1, "Abraham", 29, "IT",    "Mumbai",    20000, "Male"),
    new Employee(2, "Mary",    27, "Sales", "Chennai",   25000, "Female"),
    new Employee(3, "Joe",     28, "IT",    "Chennai",   22000, "Male"),
    new Employee(4, "John",    29, "Sales", "Gurgaon",   29000, "Male"),
    new Employee(5, "Liza",    25, "Sales", "Bangalore", 32000, "Female"),
    new Employee(6, "Peter",   27, "IT",    "Mumbai",    31500, "Male"),
    new Employee(7, "Harry",   30, "IT",    "Kochi",     21000, "Male"));
```

**Required output:** department → employee **name** (not the whole object).

```
IT    -> Peter
Sales -> Liza
```

### ✅ The answer to write (department → name, no `Optional`)

```java
Map<String, String> topEarnerByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.collectingAndThen(
            Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary)),
            emp -> emp.map(Employee::getName).orElse("N/A"))));

topEarnerByDept.forEach((dept, name) -> System.out.println(dept + " -> " + name));
// IT    -> Peter
// Sales -> Liza
```

**Why `collectingAndThen`:** `maxBy` returns `Optional<Employee>`, because a group *could* be empty in the general contract. `collectingAndThen` applies a finisher to unwrap it — and since we only need the name, we `map(Employee::getName)` in the same step. **Say this out loud while you type it; it's the exact follow-up they asked last time.**

### Variants — have these ready, they *will* push

**Keep the whole employee object:**
```java
Map<String, Employee> topByDept = employees.stream()
    .collect(Collectors.toMap(
        Employee::getDepartment,
        Function.identity(),
        BinaryOperator.maxBy(Comparator.comparingDouble(Employee::getSalary))));
```
> 💡 **This is the cleanest version** — a 3-arg `toMap` with a merge function, no `Optional` anywhere. Naming it as the alternative is a strong move.

**Just the max salary per department:**
```java
Map<String, Double> maxSalary = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment,
             Collectors.summarizingDouble(Employee::getSalary)))   // count/sum/min/avg/max in one pass
    .entrySet().stream()
    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getMax()));

// or simply:
Map<String, Optional<Double>> m = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment,
             Collectors.mapping(Employee::getSalary, Collectors.maxBy(Double::compare))));
```

**Handling ties (two people on the same top salary) — the sharpest follow-up:**
```java
Map<String, List<String>> allTopEarners = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment))
    .entrySet().stream()
    .collect(Collectors.toMap(Map.Entry::getKey, e -> {
        double max = e.getValue().stream().mapToDouble(Employee::getSalary).max().orElse(0);
        return e.getValue().stream()
                .filter(emp -> emp.getSalary() == max)
                .map(Employee::getName)
                .toList();
    }));
```
> ⚠️ `maxBy` keeps **only one** on a tie (the first encountered). Volunteering that shows you thought past the happy path.

**Sorted output / `TreeMap` (grouping's 3-arg form):**
```java
Map<String, String> sorted = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment, TreeMap::new,
        Collectors.collectingAndThen(
            Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary)),
            e -> e.map(Employee::getName).orElse("N/A"))));
```

**The pre-Java-8 version, in case they ask "without streams":**
```java
Map<String, Employee> top = new HashMap<>();
for (Employee e : employees) {
    Employee current = top.get(e.getDepartment());
    if (current == null || e.getSalary() > current.getSalary()) top.put(e.getDepartment(), e);
}
```

**The SQL version (asked at Mphasis — same problem, different language):**
```sql
SELECT department, name FROM (
  SELECT department, name, salary,
         RANK() OVER (PARTITION BY department ORDER BY salary DESC) AS rnk
  FROM employee
) t WHERE rnk = 1;          -- RANK keeps ties, ROW_NUMBER keeps exactly one
```

> 📊 **Complexity:** O(n) single pass, O(d) space where d = number of departments. Say it — it takes two seconds and it's free credibility.

🔗 More of the same family: **[22 — Java Streams: 20 coding problems](./22-java-streams-coding-problems.md)** · **[36 — SQL §the salary questions](./36-sql-interview-questions.md)**.

---

# 🎯 What this round tells you

**1. Altimetrik goes deep on Spring wiring, not Spring trivia.** Component scanning across packages, `@Qualifier` vs `@Primary`, the repository hierarchy, the *classes* involved in pagination — none of these can be answered from a "top 50 Spring Boot questions" blog. They're what you know from building. **Study [06 — Spring Boot](./06-spring-boot.md) at the mechanism level.**

**2. The same problem came back.** Highest-paid-employee-per-department was asked at Altimetrik R1, in SQL at Mphasis, and again here. Three rounds. Along with lifecycle hooks (**5 rounds**), interceptors (**4**), data binding/forms (**4**) and lazy loading (**3**). **The bank really is ~25 questions.**

**3. Microservices questions are now failure-mode questions.** Not "what is a microservice" but *"the network fails — what happens?"* The vocabulary that answers those: **timeout, circuit breaker, retry with backoff, idempotency key, transactional outbox, DLQ, compensating transaction.** Have all seven ready.

**3b. They opened on Java 16/17 language features.** Records first, pattern matching second — modern-Java questions are now an opener, not a bonus. **[12 — Java 17 Features](./12-java17-features.md)** is no longer optional.

**4. Angular was asked at breadth, Spring at depth.** With a "Fullstack Java + Angular" title, the Java/Spring side is where the round is won or lost. Revise in that order.

## ⚠️ Mark your own fumbles

Go back through the 22 and tag each one **✅ answered well / ⚠️ fumbled / ❌ blank** — *today, while you still remember*. The ⚠️ and ❌ rows are your study list for the next round.

| Q | Topic | Self-rating |
|---|---|---|
| 1–2 | **Records**, pattern matching | |
| 3–7 | Spring annotations, scanning, DI | |
| 8–10 | Spring Data, pagination | |
| 11–13 | Microservices communication & failure | |
| 14 | Profiles & environments | |
| 15–21 | Angular | |
| 22 | Streams coding | |

---

**Related files:** [26 — Companies (the recall log)](./26-companies-asked-questions.md) · [06 — Spring Boot](./06-spring-boot.md) · [08 — Microservices](./08-microservices-basics.md) · [24 — ORM/JPA](./24-orm-jpa-hibernate.md) · [04 — Angular](./04-angular.md) · [25 — Binding & Forms](./25-angular-binding-forms.md) · [20 — RxJS](./20-rxjs-operators.md) · [22 — Java Streams](./22-java-streams-coding-problems.md) · [12 — Java 17](./12-java17-features.md) · [36 — SQL](./36-sql-interview-questions.md)
