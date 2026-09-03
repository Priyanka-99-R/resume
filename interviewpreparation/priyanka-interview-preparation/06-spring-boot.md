# Spring & Spring Boot — Interview Q&A (Easy Version)

> **How to use this file:** every question follows the same shape —
> **Easiest way to remember → simple explanation → real-world example → diagram → interview trap → Interview-ready answer → Easy memory box.**
> The **Quick Revision Sheet** at the end is what you read the night before.

---

> ## 🔵 About the "In your RoboGebra project" blocks
>
> After an interview answer you'll see a **🔵 In your RoboGebra project** block with **real code from your own repositories**.
>
> ```
> DEFINITION → "in our codebase…" ⭐ → ONE trade-off
> ```
>
> Three sentences. That's the difference between reading the docs and shipping it. Index: **[39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)**.

---

## Contents

1. [Spring Core](#spring-core)
2. [Spring Boot](#spring-boot)
3. [REST APIs](#rest-apis)
4. [Spring Data JPA](#spring-data-jpa)
5. [Security & Misc](#security--misc)
6. [Reactive (Spring WebFlux)](#reactive-spring-webflux)
7. [Quick Revision Sheet](#quick-revision-sheet)

---

## Spring Core

### Q: What is the Spring Framework?

The easiest way to remember:

```
Spring = a framework that CREATES and CONNECTS your objects for you,
         so your classes stop saying `new`.
```

Real-world idea: **a wedding planner.**

```
Without a planner → YOU book the caterer, the decorator, the photographer,
                    and you must know each one's phone number 😩

With a planner    → you just say "I need a wedding".
                    The planner arranges everyone and hands it to you ✅
```

Spring is that planner. It is called the **IoC container**.

#### What Spring gives you

```
DI (Dependency Injection)  → objects are handed to you
AOP                        → logging/security/transactions without touching business code
Transactions               → @Transactional
Data access                → JDBC, JPA, MongoDB
MVC                        → REST controllers
Security                   → authentication + authorisation
```

**Key benefits:** loose coupling, testability (easy mocking), declarative transactions and security, and a huge ecosystem.

#### Interview-ready answer

> Spring is a lightweight open-source Java framework for enterprise applications. Its core idea is Inversion of Control — the framework manages object creation and wiring instead of our code doing it with `new`. On top of that it provides modules for dependency injection, AOP, declarative transactions, data access, MVC and security. The practical benefits are loose coupling and testability: because a class receives its dependencies instead of creating them, we can swap the implementation or pass a mock in a unit test without changing the class.

#### Easy memory

```
Spring = the WEDDING PLANNER for your objects 💍
It creates them, connects them, and hands them over.
Core = IoC + DI. Everything else (AOP, MVC, Data, Security) is built on that.
```

---

### Q: What is IoC (Inversion of Control) and DI (Dependency Injection)?

The easiest way to remember:

```
IoC = the IDEA   → "don't call us, we'll call you" — the container is in charge
DI  = the METHOD → the container INJECTS your dependencies
```

DI is *how* Spring achieves IoC. People use the two words interchangeably, but that one line separates them cleanly.

#### The code that shows it

```java
// ❌ Without DI — TIGHT coupling
class OrderService {
    private PaymentService payment = new PaymentService();   // hard-coded
}
```

Problems:

```
- OrderService is stuck with THIS PaymentService forever
- You cannot swap it for a test mock
- Changing PaymentService's constructor breaks OrderService
```

```java
// ✅ With DI — the container injects it
@Service
class OrderService {
    private final PaymentService payment;

    OrderService(PaymentService payment) {      // injected ✅
        this.payment = payment;
    }
}
```

```
Spring container
      │ creates PaymentService
      │ creates OrderService, PASSING the PaymentService in
      ↓
OrderService doesn't know or care HOW PaymentService was built ✅
```

Real-world idea: **a restaurant kitchen.**

```
Without DI → the chef grows his own vegetables 🥕 (tightly coupled to one farm)
With DI    → vegetables are DELIVERED to the kitchen.
             Change the supplier and the chef's recipe never changes ✅
```

#### Interview-ready answer

> Inversion of Control means the control over object creation and wiring is inverted — instead of a class creating its own dependencies with `new`, the Spring container creates them and hands them over. Dependency Injection is the pattern Spring uses to implement IoC: the container injects an object's dependencies through the constructor, a setter or a field. The benefit is loose coupling — `OrderService` doesn't know how `PaymentService` is built, so we can swap the implementation or inject a mock in tests without modifying `OrderService` at all.

#### Easy memory

```
IoC = the PRINCIPLE (container is in charge)
DI  = the TECHNIQUE (container injects the dependencies)

new PaymentService()  → tight coupling ❌
constructor parameter → injected, swappable, testable ✅
```

---

### Q: ApplicationContext vs BeanFactory?

The easiest way to remember:

```
BeanFactory        = the BASIC container (bare minimum)
ApplicationContext = BeanFactory + all the enterprise features ⭐ (what you actually use)
```

| BeanFactory | ApplicationContext |
|---|---|
| basic container, lazy init by default | superset of BeanFactory |
| no enterprise features | eager singleton init, events, i18n, AOP, annotations |
| rarely used directly | the standard choice in real apps |

```
ApplicationContext
   └── extends BeanFactory
        + event publishing
        + internationalisation (i18n)
        + annotation support
        + AOP integration
        + eager singleton creation ⭐
```

Real-world idea:

```
BeanFactory        → a basic phone: calls only
ApplicationContext → a smartphone: calls + camera + apps + internet ✅
```

The **eager vs lazy** difference matters practically:

```
BeanFactory        → creates a bean only when first requested (lazy)
ApplicationContext → creates all singletons at STARTUP (eager) ⭐
```

Eager is better: a misconfigured bean fails at **startup**, not at 2 a.m. when the first user hits that endpoint.

#### Interview-ready answer

> Both are IoC containers, but `BeanFactory` is the basic, low-level one with lazy initialisation and no enterprise features. `ApplicationContext` is a superset — it adds event publishing, internationalisation, annotation support, AOP integration, and it eagerly instantiates singleton beans at startup, so configuration errors surface immediately rather than at first use. In practice we always use `ApplicationContext`, and Spring Boot creates one for us automatically; `BeanFactory` is really just the underlying foundation.

#### Easy memory

```
BeanFactory        → basic phone → lazy
ApplicationContext → smartphone → EAGER singletons ⭐ → what you always use

Eager is better: broken config fails at STARTUP, not in production.
```

---

### Q: What are the bean scopes in Spring?

The easiest way to remember:

```
singleton (DEFAULT) → ONE object for the whole application ⭐
prototype           → a NEW object every time you ask
request / session   → web only
```

| Scope | How many instances |
|---|---|
| **singleton** (default) | one shared instance per container ⭐ |
| **prototype** | a new instance on every request for the bean |
| **request** | one per HTTP request (web only) |
| **session** | one per HTTP session (web only) |
| **application** | one per ServletContext |
| **websocket** | one per WebSocket session |

```java
@Service
@Scope("prototype")
public class ReportBuilder { }
```

Real-world idea:

```
singleton → the office PRINTER: one machine, everybody shares it
prototype → a sheet of PAPER: everyone gets their own, fresh ✅
session   → your shopping CART: one per logged-in user
request   → the BILL for one purchase: created and thrown away
```

#### The trap you must mention ⭐

```
A singleton bean is shared by EVERY concurrent HTTP request.

So a mutable instance field on a @Service is a THREAD-SAFETY BUG. 💥
```

```java
@Service
public class OrderService {
    private int counter;                // 💥 shared across all requests!
    private String currentUser;         // 💥 request A can see request B's user
}
```

```
Tomcat threads:  req1  req2  req3  req4
                   └─────┴─────┴─────┘
                          ↓
                 ONE OrderService object
                 private String currentUser;  ← whoever wrote last wins 💥
```

**Rule: keep `@Service` beans stateless.** Pass state as method parameters.

#### Interview-ready answer

> Spring has six scopes. The default is singleton — one shared instance per application context, not per JVM. Prototype creates a new instance every time the bean is requested. Then there are the web scopes: request, session, application and websocket. The important practical point is that because singleton beans are shared across all concurrent HTTP requests, any mutable instance field on a `@Service` is shared state and therefore a thread-safety bug — so I keep service beans stateless and pass state as method parameters.

#### Easy memory

```
singleton ⭐ default → ONE for everyone → printer
prototype           → NEW each time    → sheet of paper
request / session   → web only

⚠️ Singleton + mutable field = thread-safety bug. Keep @Service STATELESS.
```

---

### Q: Explain the Spring bean lifecycle.

The easiest way to remember:

```
CREATE → INJECT → INITIALISE → USE → DESTROY
```

```
1. Container INSTANTIATES the bean            (calls the constructor)
2. Populates DEPENDENCIES                     (DI happens here)
3. Aware interfaces called                    (BeanNameAware, ApplicationContextAware)
4. @PostConstruct  /  afterPropertiesSet()    ← your init hook ⭐
5. Bean is READY and used by the application
6. On shutdown: @PreDestroy / destroy()       ← your cleanup hook ⭐
```

```java
@Component
public class CacheManager {

    @PostConstruct
    public void init()    { /* warm up the cache — runs AFTER injection ⭐ */ }

    @PreDestroy
    public void cleanup() { /* flush the cache before shutdown */ }
}
```

Real-world idea: a **new employee.**

```
1. Hired                       → constructor
2. Given laptop + ID card      → dependencies injected
3. Induction / training        → @PostConstruct ⭐
4. Doing the actual job        → bean in use
5. Exit formalities, hand back → @PreDestroy
```

#### Why `@PostConstruct` and not the constructor?

```
Constructor    → runs BEFORE the dependencies are injected
                 (field injection isn't done yet → NullPointerException 💥)

@PostConstruct → runs AFTER everything is injected ✅
```

That is exactly why the hook exists.

#### Interview-ready answer

> The container first instantiates the bean by calling its constructor, then performs dependency injection, then calls any Aware interfaces, then the initialisation callback — `@PostConstruct`, or `afterPropertiesSet()` if the bean implements `InitializingBean`. At that point the bean is ready and used by the application. On context shutdown the destruction callback runs — `@PreDestroy` or `destroy()`. The reason we use `@PostConstruct` rather than the constructor for initialisation logic is that the constructor runs before dependency injection completes, so injected fields would still be null.

#### Easy memory

```
CREATE → INJECT → @PostConstruct ⭐ → USE → @PreDestroy

New employee: hired → laptop → induction → work → exit formalities

@PostConstruct runs AFTER injection (the constructor runs BEFORE it).
```

---

### Q: `@Component` vs `@Service` vs `@Repository` vs `@Controller`?

The easiest way to remember:

```
They are all @Component underneath.
The difference is INTENT — plus ONE real behaviour difference.
```

```
              @Component  (the parent — generic Spring bean)
                   │
      ┌────────────┼────────────┬──────────────┐
      ↓            ↓            ↓              ↓
  @Service    @Repository   @Controller   @RestController
  business    data access   web (views)   web (JSON)
  (semantic   ⭐ ADDS       returns a     = @Controller
   only)      exception     view name     + @ResponseBody
              translation
```

#### The one that actually does something ⭐

```java
@Repository
public class UserDao { }
```

`@Repository` adds **exception translation**: it converts vendor-specific JDBC/JPA exceptions into Spring's own `DataAccessException` hierarchy.

```
SQLException / HibernateException   (vendor-specific, checked)
          ↓ @Repository translates
DataAccessException                 (Spring's own, unchecked) ✅
```

Why that matters: your service layer doesn't have to change if you switch from Hibernate to JDBC.

The others (`@Service`, `@Controller`) are **semantic only** — they behave exactly like `@Component`.

Real-world idea: **job titles in a company.** Manager, Engineer and Accountant are all *employees*; the title tells you what they do. But the Accountant also has a real extra duty nobody else has.

#### Interview-ready answer

> All four are stereotype annotations and all are specialisations of `@Component`, so all of them make the class a Spring-managed bean. `@Component` is generic, `@Service` marks the business layer, `@Repository` marks the data-access layer, and `@Controller` marks a web MVC controller returning view names, with `@RestController` being `@Controller` plus `@ResponseBody`. The only one that adds real behaviour is `@Repository`, which enables exception translation — it converts JDBC or Hibernate exceptions into Spring's unchecked `DataAccessException` hierarchy, so the service layer isn't coupled to a specific persistence technology. The others are semantic, but using the specific one makes the layering obvious and lets AOP target a layer precisely.

#### Easy memory

```
All four = @Component underneath.

@Service     → business layer   (semantic only)
@Repository  → data layer       ⭐ ADDS exception translation → DataAccessException
@Controller  → returns a VIEW
@RestController → @Controller + @ResponseBody → returns JSON ⭐
```

---

### Q: `@Autowired` — field vs constructor vs setter injection? Which is best?

The easiest way to remember:

```
CONSTRUCTOR injection is the best. ⭐

Because it lets the field be `final`, makes dependencies obvious,
and makes the class testable without Spring.
```

```java
// ❌ FIELD injection — concise but bad
@Service
public class OrderService {
    @Autowired private PaymentService payment;      // cannot be final, hard to test
}

// 🟡 SETTER injection — fine for OPTIONAL dependencies
@Autowired
public void setPayment(PaymentService payment) { this.payment = payment; }

// ✅ CONSTRUCTOR injection — the recommended one
@Service
public class OrderService {
    private final PaymentService payment;          // final ✅
    private final InventoryService inventory;

    // @Autowired is optional for a SINGLE constructor (Spring 4.3+) ⭐
    public OrderService(PaymentService payment, InventoryService inventory) {
        this.payment = payment;
        this.inventory = inventory;
    }
}
```

#### Why constructor injection wins — four reasons

```
1. The field can be FINAL → immutable, cannot be reassigned by accident
2. The object is FULLY BUILT the moment it exists → no half-initialised bean
3. TESTABLE without Spring:
       new OrderService(mockPayment, mockInventory);       ✅
   With field injection you need reflection or @SpringBootTest 😩
4. It EXPOSES bad design: a constructor with 8 parameters
   screams "this class does too much" — field injection HIDES that ⭐
```

That fourth point is the one that impresses.

Real-world idea:

```
Constructor injection → a car built WITH its engine already inside ✅
Field injection       → a car rolled off the line, with someone
                        pushing the engine in through the window later 😬
```

#### Interview-ready answer

> There are three types — field, setter and constructor injection. Constructor injection is the recommended one for several reasons: the dependency can be declared `final`, so the object is immutable and fully initialised the moment it exists; the dependencies are explicit in the signature; and it makes unit testing trivial because we can just call the constructor with mocks, without starting a Spring context. Field injection is concise but hides dependencies, prevents `final`, and requires reflection to test. Setter injection is reasonable for genuinely optional dependencies. Since Spring 4.3, `@Autowired` is not even required when a class has a single constructor. A practical benefit is that constructor injection makes a class with too many dependencies visibly ugly, which is a useful design signal that field injection hides.

#### 🔵 In your RoboGebra project — constructor injection, without writing a constructor ⭐

**File:** `domain/robochat/controller/RoboChatController.java`

```java
@RestController
@RequestMapping("/api/robochat")
@AllArgsConstructor                          // ⭐ Lombok generates the constructor
public class RoboChatController {

    private final RoboChatService roboChatService;   // ⭐ final → immutable, injected
}
```

```
@AllArgsConstructor + `final` fields = CONSTRUCTOR INJECTION ⭐
   → no @Autowired anywhere (a single constructor doesn't need it, Spring 4.3+)
   → the field is `final`, so it can never be reassigned
   → the class is testable with `new RoboChatController(mockService)` ⭐
```

> 🗣️ *"We use constructor injection everywhere, but written as Lombok's `@AllArgsConstructor` with `final` fields — so you get immutability and testability without the boilerplate. There's no `@Autowired` in the codebase on a constructor, because with a single constructor Spring doesn't need it."*

⭐ **207 `@Service` classes and 110 `@RestController`s**, all wired this way.

⚠️ The trade-off to name: `@AllArgsConstructor` injects **every** field, so adding a field silently changes the constructor signature. On a class where that matters, write the constructor by hand — or use `@RequiredArgsConstructor`, which only takes the `final` ones.

#### Easy memory

```
CONSTRUCTOR injection ⭐
   final ✅ | fully built ✅ | testable without Spring ✅ | exposes too many deps ✅
   @Autowired NOT needed for a single constructor (4.3+)

Field injection  ❌ can't be final, needs reflection to test
Setter injection 🟡 optional dependencies only
```

---

### Q: What is `@Qualifier`?

The easiest way to remember:

```
Two beans of the SAME type → Spring can't choose → @Qualifier NAMES the one you want.
```

```java
@Component("smsSender")   class SmsSender   implements MessageSender { }
@Component("emailSender") class EmailSender implements MessageSender { }
```

```java
@Service
public class NotificationService {

    private final MessageSender sender;

    public NotificationService(@Qualifier("smsSender") MessageSender sender) {
        this.sender = sender;                            // ✅ no ambiguity
    }
}
```

Without it:

```
NoUniqueBeanDefinitionException:
   expected single matching bean but found 2: smsSender, emailSender 💥
```

```
MessageSender
   ├── smsSender
   └── emailSender
          ↑
   @Qualifier("smsSender") points at exactly one ✅
```

Real-world idea: you ask for "**a driver**" and two drivers step forward. `@Qualifier` is you saying "**Ravi**, specifically."

#### Easy memory

```
2+ beans of one type → NoUniqueBeanDefinitionException 💥
Fix: @Qualifier("beanName")  ← names the exact bean
```

---

### Q: What is `@Primary`?

The easiest way to remember:

```
@Primary = "if nobody says otherwise, use ME."

@Primary  → the DEFAULT
@Qualifier→ the EXCEPTION
```

```java
@Bean
@Primary
public MessageSender emailSender() { return new EmailSender(); }

@Bean
public MessageSender smsSender()  { return new SmsSender(); }
```

```java
// gets emailSender automatically ✅
public NotificationService(MessageSender sender) { }

// explicitly asks for the other one
public AlertService(@Qualifier("smsSender") MessageSender sender) { }
```

```
@Primary   → set the default once, in ONE place
@Qualifier → override it where needed
```

Real-world idea: `@Primary` is your **default payment card**. `@Qualifier` is choosing a different card at checkout.

⚠️ If `@Primary` and `@Qualifier` are both present, **`@Qualifier` wins** — the explicit request always beats the default.

#### Easy memory

```
@Primary   → the default bean (used when nothing is specified)
@Qualifier → the explicit choice → BEATS @Primary ⭐

Default card vs "no, use this other card today"
```

---

### Q: `@Configuration` and `@Bean`?

The easiest way to remember:

```
@Component → "Spring, YOU create this class"          (your own classes)
@Bean      → "I'LL create it, you just manage it"     (third-party classes ⭐)
```

You cannot put `@Component` on `RestTemplate` — it's not your code. So:

```java
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();          // I build it, Spring manages it ✅
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper m = new ModelMapper();
        m.getConfiguration().setSkipNullEnabled(true);   // custom setup
        return m;
    }
}
```

```
Your own class      → @Component / @Service / @Repository
Third-party class   → @Bean inside a @Configuration class ⭐
Needs custom setup  → @Bean (you control the construction)
```

Real-world idea: `@Component` is hiring a full-time employee. `@Bean` is bringing in a machine you bought elsewhere and registering it as company property.

#### The `@Configuration` bonus detail ⭐

```java
@Configuration
class AppConfig {
    @Bean A a() { return new A(b()); }
    @Bean B b() { return new B(); }        // called twice?
}
```

`@Configuration` classes are **CGLIB-proxied**, so calling `b()` inside `a()` returns the **same singleton**, not a new object. With plain `@Component` on the config class (a "lite" mode), you would get two different `B` instances. That is a nice detail to know.

#### Easy memory

```
@Configuration = a class that DEFINES beans
@Bean          = a method that RETURNS a bean

Use @Bean for: third-party classes (RestTemplate, ModelMapper) + custom construction
@Configuration is CGLIB-proxied → calling another @Bean method returns the SAME singleton ⭐
```

---

### Q: What is AOP (Aspect-Oriented Programming)?

The easiest way to remember:

```
AOP = pull the REPEATED code (logging, security, transactions) OUT of
      every method, and apply it from OUTSIDE.
```

#### The problem

```java
public void createOrder() {
    log.info("start");            // repeated
    checkSecurity();              // repeated
    beginTransaction();           // repeated

    // ...the 3 lines that are actually the business logic...

    commit();                     // repeated
    log.info("end");              // repeated
}
```

That noise appears in **every single method** of the application. These are called **cross-cutting concerns** — they cut across all layers.

```
                 Controller
                 Service        ← logging cuts across ALL of them
                 Repository     ← security cuts across ALL of them
                                ← transactions cut across ALL of them
```

#### The AOP solution

```java
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.app.service.*.*(..))")
    public Object logTime(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = jp.proceed();                 // run the real method
        log.info("{} took {} ms", jp.getSignature(), System.currentTimeMillis() - start);
        return result;
    }
}
```

Now **every** service method is timed, and no business method contains a single line of logging code.

```
Caller
  ↓
PROXY  ← @Around advice runs here (before)
  ↓
Real method (pure business logic ✅)
  ↓
PROXY  ← @Around advice runs here (after)
  ↓
Caller
```

Real-world idea: **CCTV cameras in a shop.**

```
Without AOP → every employee must also write down who entered 😩
With AOP    → cameras at the doors record everything.
              Employees just do their job ✅
```

#### The vocabulary

```
Aspect    → the module (the LoggingAspect class)
Advice    → the action: @Before, @After, @AfterReturning, @AfterThrowing, @Around
Pointcut  → WHERE it applies: execution(* com.app.service.*.*(..))
JoinPoint → the actual point in execution (the method being called)
```

⭐ **`@Transactional` is itself implemented with AOP** — that is the sentence that connects this to everything else, and it explains the self-invocation trap below.

#### The self-invocation trap ⭐

```java
@Service
public class OrderService {

    public void a() {
        this.b();          // ❌ AOP does NOT apply — bypasses the proxy!
    }

    @Transactional
    public void b() { }
}
```

Because AOP works via a **proxy**, an internal `this.` call never leaves the object, so the proxy never sees it. The same trap applies to `@Async` and `@Cacheable`.

#### Interview-ready answer

> AOP lets us modularise cross-cutting concerns — logging, security, transactions, metrics — that would otherwise be repeated in every class. Instead of putting that code inside every method, we write an Aspect containing Advice, and a Pointcut expression that says where it applies. Spring implements this with proxies: when a bean is advised, Spring wraps it in a proxy that runs the advice before or after the real method. `@Transactional` and `@Async` are themselves built on AOP. The important consequence of the proxy model is self-invocation: if a method calls another method of the same class using `this`, the call doesn't pass through the proxy, so the annotation has no effect.

#### Easy memory

```
AOP = CCTV cameras 📹 — watch everything without changing the shop staff

Aspect (the class) | Advice (@Before/@After/@Around) | Pointcut (where) | JoinPoint
@Transactional and @Async ARE AOP ⭐

⚠️ SELF-INVOCATION: this.method() bypasses the proxy → the annotation is ignored
```

---

## Spring Boot

### Q: What is Spring Boot and what are its advantages over Spring?

The easiest way to remember:

```
Spring      = a box of parts you must assemble yourself 🧰
Spring Boot = the same parts, PRE-ASSEMBLED, with a working engine ✅
```

Spring Boot is an **opinionated** layer on top of Spring: it makes sensible default choices so you don't configure everything by hand.

| | Spring | Spring Boot |
|---|---|---|
| Configuration | lots of XML / Java config | **auto-configuration** ✅ |
| Dependencies | pick every jar and version yourself | **starters** — curated bundles ✅ |
| Server | deploy a WAR to an external Tomcat | **embedded** server, `java -jar` ✅ |
| Time to first endpoint | hours | minutes |
| Monitoring | build your own | **Actuator** built in ✅ |

Real-world idea:

```
Spring      → buying flour, sugar, eggs and baking the cake yourself
Spring Boot → a ready cake mix: add water and bake ✅
              (and you can still add your own ingredients)
```

That last part matters: Boot never *stops* you overriding a default.

#### Interview-ready answer

> Spring Boot is an opinionated layer on top of the Spring Framework that removes boilerplate configuration and lets us build stand-alone, production-ready applications quickly. Its main features are auto-configuration, which configures beans based on what is on the classpath; starter dependencies, which bundle compatible libraries so we don't manage versions; an embedded server, so the application runs as an executable jar with `java -jar` instead of being deployed to an external Tomcat; and Actuator, which provides health and metrics endpoints out of the box. Importantly it is convention over configuration, not a restriction — if we define our own bean, Boot backs off and ours wins.

#### Easy memory

```
Spring Boot = ready CAKE MIX 🍰

Auto-configuration | Starters | Embedded server | No XML | Actuator
Opinionated, but YOUR bean always overrides the default ⭐
```

---

### Q: How does auto-configuration work?

The easiest way to remember:

```
Boot LOOKS at your classpath and asks:
"Is this library present? Did the developer already define this bean?"
If yes / no → it configures it FOR you.
```

```
Application starts
      ↓
@SpringBootApplication → @EnableAutoConfiguration
      ↓
reads META-INF/spring/...AutoConfiguration.imports   (the list of candidates)
      ↓
for each candidate, checks the @Conditional annotations:
      @ConditionalOnClass        → is this class on the classpath?
      @ConditionalOnMissingBean  → did the developer NOT define one? ⭐
      @ConditionalOnProperty     → is this property set?
      ↓
if all conditions pass → the bean is created automatically ✅
```

A concrete example:

```
You add spring-boot-starter-data-jpa + the H2 driver
      ↓
@ConditionalOnClass(DataSource.class)      ✅ present
@ConditionalOnMissingBean(DataSource.class) ✅ you defined none
      ↓
Boot creates a DataSource, an EntityManagerFactory and a TransactionManager for you
```

⭐ `@ConditionalOnMissingBean` is the key to the whole design: **your bean always wins.** Define your own `DataSource` and Boot silently steps aside.

Real-world idea: **a smart home.**

```
It detects you own a TV        → sets up the remote
It detects you own an AC       → sets up the thermostat
You already installed your own → it leaves yours alone ✅
```

#### How to debug it (a great practical point)

```properties
debug=true
```

This prints the **auto-configuration report** at startup: every candidate, and whether it matched or not and *why*. Mentioning this shows real experience.

#### Interview-ready answer

> Auto-configuration is driven by `@EnableAutoConfiguration`, which is part of `@SpringBootApplication`. At startup, Spring Boot reads a list of auto-configuration classes from `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` and applies each one conditionally using annotations such as `@ConditionalOnClass`, `@ConditionalOnMissingBean` and `@ConditionalOnProperty`. So if the JPA starter and a database driver are on the classpath and we have not defined a `DataSource` ourselves, Boot configures one. The `@ConditionalOnMissingBean` condition is what makes it safe — our own beans always take precedence. If I need to see what was applied or why something did not activate, I run with `debug=true`, which prints the auto-configuration report.

#### Easy memory

```
@EnableAutoConfiguration → reads AutoConfiguration.imports → applies @Conditional checks

@ConditionalOnClass       → library present?
@ConditionalOnMissingBean → you didn't define one? ⭐ (YOUR bean always wins)
@ConditionalOnProperty    → property set?

Debug it with:  debug=true   → prints the auto-configuration report ⭐
```

---

### Q: What are Spring Boot starters?

The easiest way to remember:

```
A starter = ONE dependency that pulls in a whole working set of libraries,
            with COMPATIBLE versions already chosen for you.
```

```xml
<!-- ONE line... -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

```
...brings in:
   spring-web + spring-webmvc
   embedded Tomcat
   Jackson (JSON)
   validation
   logging
   — all at versions tested together ✅
```

Without starters you would list ~15 dependencies and hand-check every version for conflicts.

The ones to be able to name:

```
spring-boot-starter-web         REST + embedded Tomcat + Jackson
spring-boot-starter-data-jpa    Hibernate + Spring Data + JDBC
spring-boot-starter-security    authentication + authorisation
spring-boot-starter-validation  @Valid, @NotNull, @Email
spring-boot-starter-test        JUnit 5 + Mockito + AssertJ + MockMvc
spring-boot-starter-actuator    health, metrics, info endpoints
spring-boot-starter-webflux     reactive stack
```

Real-world idea: a **thali / combo meal**. You order one thing and get rice, dal, curry, curd and sweet — already chosen to go together.

#### Easy memory

```
Starter = a COMBO MEAL 🍱 — one dependency, a whole working stack, versions matched

web | data-jpa | security | validation | test | actuator | webflux
```

---

### Q: What does `@SpringBootApplication` consist of?

The easiest way to remember:

```
@SpringBootApplication = 3 annotations in one:

@Configuration + @EnableAutoConfiguration + @ComponentScan
```

```
@SpringBootApplication
        │
        ├── @Configuration          → this class can define beans
        ├── @EnableAutoConfiguration→ turn on auto-configuration ⭐
        └── @ComponentScan          → scan THIS package and below for components
```

```java
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

#### The trap ⭐ — package placement

```
com.app                 ← put App.java HERE
  ├── controller        ✅ scanned
  ├── service           ✅ scanned
  └── repository        ✅ scanned

com.other.util          ❌ NOT scanned → "no qualifying bean of type..." 💥
```

`@ComponentScan` starts at the **main class's own package** and works downwards. This is the single most common "why isn't my bean found?" bug. Fix it by moving the class, or by using `@ComponentScan(basePackages = "...")`.

#### Interview-ready answer

> `@SpringBootApplication` is a meta-annotation combining three: `@Configuration`, which allows the class to define beans; `@EnableAutoConfiguration`, which turns on Spring Boot's auto-configuration; and `@ComponentScan`, which scans the main class's package and its sub-packages for components. A practical consequence is that the main class should sit in the root package of the application — if a component lives outside that package tree it will not be picked up, which is the most common cause of a "no qualifying bean" error at startup.

#### Easy memory

```
@SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan

⚠️ Main class must be in the ROOT package — scanning goes DOWNWARDS only
```

---

### Q: `application.properties` vs `application.yml`?

The easiest way to remember:

```
Same job, different format.
.properties → FLAT key=value
.yml        → INDENTED, hierarchical (less repetition)
```

```properties
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/shop
spring.datasource.username=root
```

```yaml
server:
  port: 8081
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shop
    username: root
```

```
.properties → you repeat "spring.datasource." on every line
.yml        → you write it once and nest ✅
```

⚠️ Two things to remember about YAML:

```
1. INDENTATION MATTERS — and you must use SPACES, never tabs 💥
2. If BOTH files exist, .properties WINS for the same key
```

#### Easy memory

```
.properties → flat, no indentation risk
.yml        → nested, cleaner for deep config, but tabs BREAK it ⚠️

Both present → .properties wins ⭐
```

---

### Q: What are Spring profiles?

The easiest way to remember:

```
Profiles = different SETTINGS for different ENVIRONMENTS
           (dev / test / prod) — with no code change.
```

```
application.properties           ← common settings, always loaded
application-dev.properties       ← loaded only when the dev profile is active
application-prod.properties      ← loaded only when the prod profile is active
```

```properties
# application-dev.properties
spring.datasource.url=jdbc:h2:mem:testdb
logging.level.root=DEBUG

# application-prod.properties
spring.datasource.url=jdbc:postgresql://prod-db:5432/shop
logging.level.root=WARN
```

Activate it:

```bash
java -jar app.jar --spring.profiles.active=prod
# or an env var: SPRING_PROFILES_ACTIVE=prod
```

#### Profiles on beans too ⭐

```java
@Service
@Profile("dev")
public class MockPaymentService implements PaymentService { }

@Service
@Profile("prod")
public class RealPaymentService implements PaymentService { }
```

```
dev  → the mock is loaded, no real money moves ✅
prod → the real one is loaded ✅
```

Real-world idea: the **same play, different stages.** Same script; the lighting and props change for the rehearsal hall versus the real theatre.

#### Easy memory

```
application-{profile}.properties       → per-environment settings
--spring.profiles.active=prod          → activate
@Profile("dev") on a bean              → load that bean only in dev ⭐

Common config in application.properties + the profile file layered on top
```

---

### Q: How does the embedded server work, and how do you change it?

The easiest way to remember:

```
The server is INSIDE your jar.

Old way → build a WAR, install Tomcat, deploy into it 😩
Boot    → java -jar app.jar   ← done ✅
```

```
app.jar
 ├── your classes
 ├── your dependencies
 └── Tomcat itself ⭐
```

That is what makes Spring Boot suitable for containers and microservices — the image just runs the jar.

Switching to Jetty or Undertow means **excluding Tomcat and adding the other starter**:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <exclusions><exclusion>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
  </exclusion></exclusions>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

Auto-configuration then detects Jetty on the classpath and configures it instead.

#### Easy memory

```
Server is packaged INSIDE the jar → java -jar app.jar ✅
Default = Tomcat
Change it = EXCLUDE spring-boot-starter-tomcat + ADD jetty/undertow starter
Why it matters: Docker/microservices — no external server to install
```

---

### Q: How do you change the server port?

```properties
server.port=8082
```

```bash
java -jar app.jar --server.port=9090     # command line wins over the file ⭐
```

```properties
server.port=0     # a RANDOM free port — very useful in integration tests
```

⭐ Worth knowing the **precedence order** (highest first):

```
command-line arguments  >  environment variables  >  application-{profile}.properties
                                                  >  application.properties
```

#### Easy memory

```
server.port=8082          in the file
--server.port=9090        on the command line → WINS ⭐
server.port=0             random free port (great for tests)
```

---

### 🔵 In your RoboGebra code — auto-configuration back-off, for real

**File:** `robogebra-portal/.../config/AsyncConfig.java`

The best possible proof that you understand `@ConditionalOnMissingBean`:

```java
/**
 * Boot's own applicationTaskExecutor, declared BY HAND.
 * It has to be. TaskExecutorConfiguration is @ConditionalOnMissingBean(Executor.class),
 * so declaring ANY executor backs the auto-configured one out entirely — and then
 * every unqualified @Async silently resolves to the notification pool.
 */
@Bean(name = {"applicationTaskExecutor", "taskExecutor"})
@Lazy
public ThreadPoolTaskExecutor applicationTaskExecutor(ThreadPoolTaskExecutorBuilder builder) {
    return builder.build();
}
```

> 🗣️ *"Auto-configuration backing off isn't just theory — it bit us. The moment we declared a second executor bean, Boot's `applicationTaskExecutor` disappeared and every unqualified `@Async` in the app silently started using our notification pool. We had to rebuild Boot's default from its own builder to keep the `spring.task.execution.*` properties working."*

See **[39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)** for the whole story.

---

### Q: What is Spring Boot Actuator?

The easiest way to remember:

```
Actuator = the DASHBOARD of your running application.
```

```
/actuator/health     → is the app alive? is the DB reachable? ⭐
/actuator/metrics    → memory, CPU, request counts, response times
/actuator/info       → build version, git commit
/actuator/env        → all configuration properties
/actuator/loggers    → view AND CHANGE log levels at runtime ⭐
/actuator/beans      → every bean in the context
/actuator/mappings   → every URL mapping
```

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```properties
management.endpoints.web.exposure.include=health,info,metrics
```

⚠️ Only `health` and `info` are exposed over HTTP by default — the rest must be opted in, and should be secured, because `/actuator/env` reveals configuration.

Real-world idea: the **dashboard of a car** — fuel, temperature, warning lights. You don't open the bonnet to check whether the engine is running.

Where it's actually used:

```
Kubernetes / load balancer → hits /actuator/health to decide if the pod is alive ⭐
Prometheus + Grafana       → scrape /actuator/prometheus for graphs
Production incident        → change a log level to DEBUG WITHOUT a restart ⭐
```

#### Easy memory

```
Actuator = the app's DASHBOARD 🚗

/health ⭐ (used by k8s / load balancers) | /metrics | /info | /loggers ⭐ | /env

Only health + info exposed by default → opt in with
management.endpoints.web.exposure.include=...
⚠️ SECURE it — /env leaks configuration
```

---

### Q: What is `CommandLineRunner`?

The easiest way to remember:

```
CommandLineRunner = "run this code ONCE, right after the app has started."
```

```java
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository repo;
    public DataSeeder(UserRepository repo) { this.repo = repo; }

    @Override
    public void run(String... args) {
        repo.save(new User("admin"));       // runs once, after startup ✅
    }
}
```

```
SpringApplication.run(...)
      ↓
context created, all beans ready
      ↓
CommandLineRunner.run()  ⭐ your code here
      ↓
application is serving requests
```

```
CommandLineRunner  → run(String... args)              → raw arguments
ApplicationRunner  → run(ApplicationArguments args)   → parsed arguments ⭐
```

Used for: seeding test data, warming a cache, printing startup banners, running a one-off migration.

Real-world idea: **switching on the lights and unlocking the door** just before the shop opens.

#### Easy memory

```
CommandLineRunner → run() executes ONCE after the context is ready
ApplicationRunner → the same, but with PARSED arguments

Use for: seed data, cache warm-up, startup checks
```

---
## REST APIs

### Q: `@RestController` vs `@Controller`?

The easiest way to remember:

```
@Controller     → returns a VIEW NAME (an HTML page)   → JSP / Thymeleaf
@RestController → returns DATA (JSON)                  → REST API ⭐

@RestController = @Controller + @ResponseBody
```

```java
@Controller
public class PageController {
    @GetMapping("/home")
    public String home() {
        return "home";          // → looks for home.html / home.jsp
    }
}

@RestController
public class UserController {
    @GetMapping("/api/users/1")
    public User get() {
        return new User(1L, "Priya");   // → {"id":1,"name":"Priya"} ✅
    }
}
```

```
@Controller     → "home"  → the view resolver finds a template → HTML page
@RestController → object  → Jackson converts it → JSON in the response body
```

⚠️ The classic bug: using `@Controller` for an API and getting a **404 for a view named after your data**, because Spring tried to find a template called "Priya".

Real-world idea:

```
@Controller     → a waiter who brings you a plated dish 🍽️ (a full page)
@RestController → a takeaway counter handing you a packed box 📦 (raw data)
```

#### Easy memory

```
@RestController = @Controller + @ResponseBody ⭐  → JSON
@Controller alone → view name → HTML page

Using @Controller for an API → it tries to find a TEMPLATE → 404 💥
```

---

### Q: What are the request mapping annotations?

The easiest way to remember:

```
GET    → READ
POST   → CREATE
PUT    → FULL update
PATCH  → PARTIAL update
DELETE → REMOVE
```

```java
@RestController
@RequestMapping("/api/users")            // base path for the whole class ⭐
public class UserController {

    @GetMapping("/{id}")   public User get(@PathVariable Long id)   { ... }
    @GetMapping            public List<User> all()                   { ... }
    @PostMapping           public User create(@RequestBody User u)   { ... }
    @PutMapping("/{id}")   public User update(@PathVariable Long id,
                                              @RequestBody User u)   { ... }
    @PatchMapping("/{id}") public User patch(...)                    { ... }
    @DeleteMapping("/{id}")public void delete(@PathVariable Long id) { ... }
}
```

```
@RequestMapping(method = RequestMethod.GET)   ← the old, verbose way
@GetMapping                                   ← the shortcut ✅ (Spring 4.3+)
```

#### PUT vs PATCH — the follow-up ⭐

```
PUT   → send the WHOLE object; missing fields get wiped
        { "name": "Priya" }  → email becomes NULL 💥

PATCH → send ONLY what changed; everything else is untouched
        { "name": "Priya" }  → only the name changes ✅
```

#### 🔵 In your RoboGebra project — a real controller, annotation by annotation ⭐

**File:** `domain/robochat/controller/RoboChatController.java`

```java
@RestController                                        // ⭐ = @Controller + @ResponseBody
@RequestMapping("/api/robochat")                       // ⭐ base path for the class
@AllArgsConstructor                                    // constructor injection
@Tag(name = "RoboChat", description = "RoboChat AI Assistant")   // ⭐ OpenAPI/Swagger
public class RoboChatController {

    private final RoboChatService roboChatService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)   // ⭐ SSE
    @PermissionContext(userPermission = {Permission.ROBOCHAT_WRITE})              // ⭐ custom
    public Flux<ServerSentEvent<String>> chat(
            @Valid @RequestBody RoboChatMessageRequestDTO dto,                    // ⭐ validated body
            @RequestAttribute(ACTIVE_USER_ID_ATTRIBUTE) String activeUserId,      // ⭐ set by a FILTER
            @RequestAttribute(LOGGED_USER_ATTRIBUTE) JwtAuth loggedUser) {
        return roboChatService.streamChat(activeUserId, loggedUser, dto);
    }

    @GetMapping("/conversations/{conversationId}")
    @PermissionContext(userPermission = {Permission.ROBOCHAT_READ})
    public Object getConversation(@PathVariable String conversationId, ...) { ... }

    @DeleteMapping("/conversations/{conversationId}")
    public Object deleteConversation(@PathVariable String conversationId, ...) { ... }
}
```

⭐ **Four things in there worth pointing at:**

```
1. @RequestAttribute ⭐ — the user id is NOT a path or query param.
   A security filter validated the JWT and put it on the request, so the
   controller can NEVER be tricked into acting on someone else's id 💥
   → a userId in the URL would be an authorisation bug waiting to happen ⭐

2. @PermissionContext ⭐ — a CUSTOM annotation. Permissions are declared
   on the endpoint and enforced centrally, not with an if-check in the method.

3. produces = TEXT_EVENT_STREAM_VALUE + Flux<ServerSentEvent<String>> ⭐
   The AI chat STREAMS tokens back as they're generated, so the user sees
   the answer appear rather than waiting for the whole response.
   (This is the one place we use WebFlux inside an otherwise MVC app.)

4. @Valid @RequestBody ⭐ — validation happens before the method body runs;
   a failure becomes MethodArgumentNotValidException → the ControllerAdvice → 400
```

**The DTO it validates:**

```java
@Data
public class RoboChatMessageRequestDTO {
    @NotBlank                       // ⭐ not null, not empty, not whitespace
    private String message;
    private String conversationId;  // optional — a new conversation if absent
    private String attachmentId;
}
```

> 🗣️ *"The detail I'd point at is `@RequestAttribute`. The active user id comes from a security filter that validated the JWT, not from the URL — so a caller can't pass someone else's id and read their conversations. And the chat endpoint returns a `Flux<ServerSentEvent>` because the AI response streams token by token; that's the only place we use WebFlux in an otherwise servlet-based app."*

#### Easy memory

```
@GetMapping | @PostMapping | @PutMapping | @PatchMapping | @DeleteMapping
@RequestMapping("/api/users") on the CLASS = the base path ⭐

PUT   = replace the WHOLE thing (missing fields are wiped)
PATCH = update only the fields you send
```

---

### Q: `@PathVariable` vs `@RequestParam` vs `@RequestBody`?

The easiest way to remember by **where the data sits in the request**:

```
/users/42          ← @PathVariable  (part of the PATH)
/users?status=new  ← @RequestParam  (after the ? — QUERY string)
{ "name": "Priya"} ← @RequestBody   (the JSON BODY)
```

```
GET /api/users/42?detailed=true
              ↑        ↑
     @PathVariable   @RequestParam

POST /api/users
Body: {"name":"Priya","email":"p@x.com"}
        ↑
   @RequestBody
```

```java
@GetMapping("/users/{id}")
public User get(@PathVariable Long id,
                @RequestParam(defaultValue = "false") boolean detailed) { ... }

@PostMapping("/users")
public User create(@RequestBody @Valid UserDto dto) { ... }
```

#### When to use which

```
@PathVariable → identifies WHICH resource   → /users/42, /orders/99/items/3
@RequestParam → filters / options / paging  → ?status=active&page=2&size=20
@RequestBody  → the data being sent         → POST / PUT payloads
```

Real-world idea: a **postal address**.

```
House number (path)     → WHICH house      → @PathVariable
"deliver in the evening"→ an INSTRUCTION   → @RequestParam
The parcel contents     → the actual DATA  → @RequestBody
```

⚠️ Two traps:

```
@RequestParam is REQUIRED by default → a missing param = 400 Bad Request
   Fix: @RequestParam(required = false) or defaultValue = "..."

Only ONE @RequestBody per method — a request has only one body.
```

#### Easy memory

```
/users/42          → @PathVariable  → WHICH resource
?status=active     → @RequestParam  → filter/option (required by default ⚠️)
JSON body          → @RequestBody   → the data (only ONE per method)
```

---

### Q: What is `ResponseEntity` and why use it?

The easiest way to remember:

```
Returning an object  → you only control the BODY (status is always 200)
ResponseEntity       → you control STATUS + HEADERS + BODY ⭐
```

```java
// Plain return → always 200 OK
@PostMapping("/users")
public User create(@RequestBody UserDto dto) {
    return service.create(dto);           // 200 — but it should be 201! ❌
}

// ResponseEntity → full control ✅
@PostMapping("/users")
public ResponseEntity<User> create(@RequestBody @Valid UserDto dto) {
    User saved = service.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED)          // 201 ✅
                         .header("Location", "/api/users/" + saved.getId())
                         .body(saved);
}
```

```
ResponseEntity
   ├── STATUS  → 200 / 201 / 204 / 404 ...
   ├── HEADERS → Location, Cache-Control, custom headers
   └── BODY    → the object
```

Common shortcuts:

```java
ResponseEntity.ok(user);                       // 200 with a body
ResponseEntity.status(HttpStatus.CREATED).body(user);   // 201
ResponseEntity.noContent().build();            // 204, no body
ResponseEntity.notFound().build();             // 404
ResponseEntity.badRequest().body(error);       // 400
```

A very common real pattern:

```java
return repo.findById(id)
           .map(ResponseEntity::ok)                       // found → 200 + body
           .orElse(ResponseEntity.notFound().build());    // absent → 404 ✅
```

Real-world idea: a plain return is handing over the parcel. `ResponseEntity` is the **full courier slip** — parcel + delivery status + tracking details.

#### Easy memory

```
ResponseEntity<T> = STATUS + HEADERS + BODY ⭐

ok() | status(CREATED).body() | noContent() | notFound() | badRequest()

POST that creates something should return 201 + a Location header — not 200.
```

---

### Q: Which HTTP status codes should a REST API use?

The easiest way to remember by **the first digit**:

```
2xx → SUCCESS       "here you go"
3xx → REDIRECT      "look over there"
4xx → CLIENT error  "YOUR mistake" ⭐
5xx → SERVER error  "MY mistake"  ⭐
```

The ones you must know:

| Code | Meaning | When |
|---|---|---|
| **200** OK | success | successful GET / PUT |
| **201** Created | resource created | successful POST ⭐ |
| **204** No Content | success, no body | successful DELETE |
| **400** Bad Request | invalid input | validation failure |
| **401** Unauthorized | **not logged in** | missing/invalid token ⭐ |
| **403** Forbidden | **logged in, not allowed** | wrong role ⭐ |
| **404** Not Found | resource missing | bad id |
| **409** Conflict | duplicate / version clash | email already exists |
| **500** Internal Server Error | unexpected crash | an unhandled exception |

#### The 401 vs 403 trap ⭐ (asked constantly)

```
401 UNAUTHORIZED → "I don't know WHO you are"      → log in first
403 FORBIDDEN    → "I know who you are, and NO"    → you lack the role
```

Real-world idea: a **nightclub**.

```
401 → you have no ID → the bouncer can't identify you
403 → your ID is valid, but you're not on the VIP list 🚫
```

#### Easy memory

```
2xx success | 4xx YOUR fault | 5xx MY fault

200 OK | 201 Created (POST) ⭐ | 204 No Content (DELETE)
400 bad input | 401 NOT LOGGED IN | 403 LOGGED IN BUT NOT ALLOWED ⭐
404 missing | 409 duplicate | 500 crash

401 = who are you? | 403 = I know you, still no.
```

---

### Q: How do you handle exceptions globally?

The easiest way to remember:

```
@RestControllerAdvice = ONE place that catches exceptions from EVERY controller.
```

#### The problem

```java
// ❌ try/catch repeated in every single controller method 😩
@GetMapping("/{id}")
public ResponseEntity<?> get(@PathVariable Long id) {
    try {
        return ResponseEntity.ok(service.find(id));
    } catch (ResourceNotFoundException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.status(500).body("error");
    }
}
```

#### The solution

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseEntity.badRequest().body(new ApiError(400, msg));
    }

    @ExceptionHandler(Exception.class)            // the safety net ⭐
    public ResponseEntity<ApiError> handleAll(Exception ex) {
        log.error("Unexpected error", ex);        // LOG the real cause
        return ResponseEntity.status(500)
                .body(new ApiError(500, "Something went wrong"));   // hide internals
    }
}
```

Now the controller is clean:

```java
@GetMapping("/{id}")
public User get(@PathVariable Long id) {
    return service.find(id);        // just throw — the advice handles it ✅
}
```

```
Controller throws ResourceNotFoundException
        ↓
@RestControllerAdvice catches it
        ↓
Client receives:  404  { "code": 404, "message": "User 42 not found" } ✅
                  (not a Java stack trace)
```

Real-world idea: a **hospital reception**. Instead of every doctor handling paperwork for every complaint, one reception desk handles all of them, consistently.

⭐ Two points that score:

```
1. NEVER leak a stack trace to the client — it exposes class names,
   framework versions and sometimes SQL. Log it, return a generic message.
2. Handlers are matched MOST SPECIFIC first, so a catch-all
   @ExceptionHandler(Exception.class) is a safe last resort.
```

#### Easy memory

```
@RestControllerAdvice + @ExceptionHandler(X.class) = ONE global error handler ⭐

Service THROWS → advice CATCHES → clean JSON + correct status code
Always add a catch-all @ExceptionHandler(Exception.class) → log it, return a generic 500
Never send a stack trace to the client.
```

---

### 🔵 In your RoboGebra code — exception handling has a second layer ⭐

**Files:** `common/exception/handler/ExceptionControllerAdvice.java` **and** `security/FilterChainExceptionHandler.java`

```
Request
   ↓
[ SECURITY FILTER CHAIN ]  ← an exception thrown HERE never reaches @ControllerAdvice 💥
   ↓
[ DispatcherServlet ]
   ↓
[ Controller ]             ← @RestControllerAdvice only covers from HERE onwards
```

> 🗣️ *"`@ControllerAdvice` only catches exceptions thrown inside the MVC dispatch. Anything thrown in a **security filter** happens before the DispatcherServlet, so the advice never sees it and the client gets a bare 500 with no JSON body. We added a dedicated `FilterChainExceptionHandler` in the filter chain for exactly that gap."*

⭐ Most candidates have never thought about this. It is a very strong thing to volunteer.

---

### Q: How do you do request validation?

The easiest way to remember:

```
Annotate the DTO  +  put @Valid on the controller parameter.
Spring does the rest.
```

```java
public class UserDto {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email")
    private String email;

    @Min(value = 18, message = "Must be 18 or older")
    private int age;
}
```

```java
@PostMapping("/users")
public User create(@RequestBody @Valid UserDto dto) { ... }
//                              ↑ WITHOUT this, the annotations do NOTHING ⚠️
```

```
Request arrives
      ↓
@Valid triggers the Bean Validation checks
      ↓
   FAIL → MethodArgumentNotValidException thrown
      ↓  caught by @RestControllerAdvice
   400 Bad Request + a readable message ✅

   PASS → your method body runs
```

The annotations worth naming:

```
@NotNull    → not null (but "" is allowed)
@NotEmpty   → not null AND not empty
@NotBlank   → not null, not empty, not just whitespace  ⭐ (use this for Strings)
@Size(min, max) | @Min | @Max | @Email | @Pattern(regexp)
@Past | @Future  (dates)
@Valid on a nested object → validates it too ⭐
```

⚠️ Requires `spring-boot-starter-validation` — it stopped being included automatically in Boot 2.3+, which catches people out.

#### The `@NotNull` vs `@NotBlank` trap ⭐

```
@NotNull  → ""       passes ❌  (it's not null!)
@NotEmpty → " "      passes ❌  (a space is not empty!)
@NotBlank → " "      FAILS  ✅  ← what you actually want for Strings
```

#### 🔵 In your RoboGebra project — validation, end to end ⭐

```java
// 1. the DTO declares the rules
@Data
public class RoboChatMessageRequestDTO {
    @NotBlank                    // ⭐ jakarta.validation — NOT javax (Boot 3) ⭐
    private String message;
    private String conversationId;
}

// 2. the controller TRIGGERS them
@PostMapping("/chat")
public Flux<ServerSentEvent<String>> chat(@Valid @RequestBody RoboChatMessageRequestDTO dto, ...) { }
//                                        ↑ without @Valid, the annotations do NOTHING ⚠️

// 3. a failure is handled ONCE, centrally
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex,
                                                        WebRequest request) {
    log.error("Validation exception: {}", ex.getMessage(), ex);
    String message = findErrorMessageForValidation(ex);      // ⭐ pull out the field error
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                         .body(createErrorMessage(HttpStatus.BAD_REQUEST, message, request));
}
```

```
92 × @Valid across the controllers ⭐
jakarta.validation-api 3.0.2 — jakarta, NOT javax ⭐ (Spring Boot 3)
```

> 🗣️ *"Validation is declared on the DTO and triggered with `@Valid` on the parameter — both are needed; the annotations do nothing on their own. The failure surfaces as `MethodArgumentNotValidException`, which the controller advice turns into a 400 with our standard error model, so no controller contains validation plumbing. And on Boot 3 the imports are `jakarta.validation`, not `javax` — that rename was the biggest cost of our Boot 2 to 3 migration."*

#### Easy memory

```
DTO gets the annotations + controller parameter gets @Valid ⭐ (both needed!)

Failure → MethodArgumentNotValidException → handle it in @RestControllerAdvice → 400

For Strings use @NotBlank (not @NotNull — "" would pass)
Needs the spring-boot-starter-validation dependency ⚠️
```

---

### 🔵 In your RoboGebra code — validation at scale

```
92 × @Valid across the controllers
spring-boot-starter-validation + jakarta.validation-api 3.0.2
```

⚠️ Note **`jakarta`**, not `javax` — the portal is Spring Boot **3.2.0** on **Java 17**. That package rename is the biggest cost of a Boot 2 → 3 migration. → [12 — Java 17](./12-java17-features.md)

---

### Q: What is idempotency in REST?

The easiest way to remember:

```
Idempotent = doing it ONCE or TEN times gives the SAME final result.
```

```
GET    → idempotent ✅  reading twice changes nothing
PUT    → idempotent ✅  setting name="Priya" twice = still "Priya"
DELETE → idempotent ✅  deleting an already-deleted record = still deleted
POST   → NOT idempotent ❌  each call creates a NEW record
```

```
POST /orders  called 3 times  →  3 ORDERS created 💥
PUT  /users/1 called 3 times  →  1 user, same final state ✅
```

Real-world idea: a **light switch**.

```
Pressing "switch OFF" ten times → the light is still just off ✅  (idempotent)
Pressing "buy a ticket" ten times → ten tickets 💥               (not idempotent)
```

#### Why this matters in real life ⭐

```
User clicks "Pay" → the network times out → they click again
       ↓
The first request DID succeed
       ↓
The customer is charged TWICE 💥
```

The fix is an **idempotency key**:

```
POST /payments
Idempotency-Key: 7f3e9a2b-...

Server: have I seen this key before?
    YES → return the ORIGINAL response, don't charge again ✅
    NO  → process it, and store the key with the result
```

This is exactly what Stripe and Razorpay do.

#### Easy memory

```
Idempotent = same result whether called once or many times

GET ✅ | PUT ✅ | DELETE ✅ | POST ❌

Make POST safe with an Idempotency-Key header ⭐ (payments!)
Light switch: "off" ten times = still off.
```

---

### Q: What are some REST API best practices?

```
1. NOUNS, plural, for resources
      ✅ /api/users              /api/users/42/orders
      ❌ /api/getUser            /api/deleteUserById

2. Let the HTTP METHOD be the verb — never put an action in the URL
      ✅ DELETE /users/42
      ❌ GET /users/deleteUser?id=42

3. Correct STATUS CODES + a consistent error body

4. PAGINATION, filtering and sorting on every collection
      /api/users?page=0&size=20&sort=name,asc
      (returning 100,000 rows in one response is a real production incident)

5. VERSION the API           → /api/v1/users

6. STATELESS — no server session; use a token on every request

7. Use DTOs — NEVER expose entities directly ⭐

8. HTTPS everywhere
```

The one they probe: **why not expose entities?**

```
Entity exposed directly:
   - leaks your DB schema to the world
   - a client could POST {"role":"ADMIN"} and change a field you never
     intended to expose (over-posting) 💥
   - lazy collections blow up during JSON serialisation
   - any DB column rename becomes a BREAKING API change
```

#### Easy memory

```
Nouns + plural | HTTP verb = the action | correct status codes
Pagination ⭐ | versioning | stateless | DTOs not entities ⭐ | HTTPS

❌ /api/getUserById/5     ✅ GET /api/v1/users/5
```

---

### Q: How do you version a REST API?

```
1. URI versioning     → /api/v1/users        ⭐ most common, most visible
2. Header versioning  → Accept: application/vnd.app.v1+json
3. Request parameter  → /api/users?version=1
```

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller { }

@RestController
@RequestMapping("/api/v2/users")
public class UserV2Controller { }
```

| | URI | Header |
|---|---|---|
| Visible in the browser | ✅ | ❌ |
| Easy to test / curl | ✅ | harder |
| Cacheable separately | ✅ | trickier |
| "Purity" | mixes version into the resource identity | cleaner in theory |

**Why version at all?** Because you cannot force every mobile app on every phone to update at the same moment. v1 must keep working while v2 rolls out.

#### Easy memory

```
/api/v1/users  ⭐ URI versioning — most common, visible, easy to test
Alternatives: an Accept header, or a query parameter

Reason: old mobile clients cannot be force-updated. v1 must keep working.
```

---

## Spring Data JPA

### Q: What is Spring Data JPA?

The easiest way to remember:

```
You write an INTERFACE. Spring writes the IMPLEMENTATION at runtime. ⭐
```

```java
public interface UserRepository extends JpaRepository<User, Long> { }
```

That is the whole file — and you already have:

```
save()  saveAll()  findById()  findAll()  findAllById()
delete()  deleteById()  count()  existsById()  flush()
+ pagination and sorting
```

```
Old DAO (JDBC)                  Spring Data JPA
──────────────                  ───────────────
open connection                 public interface UserRepository
write SQL                            extends JpaRepository<User, Long> { }
set parameters
execute
loop the ResultSet               ← that's it ✅
map to objects
close everything
~50 lines PER TABLE 😩
```

```
Your interface
      ↓
Spring creates a PROXY at startup
      ↓
the proxy implements every method for you ✅
```

Real-world idea: you write the **menu**; the kitchen appears and cooks it.

#### Easy memory

```
Declare an INTERFACE → Spring generates the implementation (a runtime proxy) ⭐
You get CRUD + paging + sorting + derived queries for free.
```

---

### Q: `JpaRepository` vs `CrudRepository` vs `PagingAndSortingRepository`?

The easiest way to remember — **each one adds to the previous**:

```
        Repository                (marker, nothing in it)
             ↑
      CrudRepository              save, findById, findAll, delete, count
             ↑
  PagingAndSortingRepository      + Pageable, Sort
             ↑
      JpaRepository               + flush(), saveAndFlush(), batch deletes,
                                    findAll() returns List (not Iterable) ⭐
```

| | CrudRepository | PagingAndSorting | JpaRepository ⭐ |
|---|---|---|---|
| Basic CRUD | ✅ | ✅ | ✅ |
| Pagination / sorting | ❌ | ✅ | ✅ |
| `flush()`, batch delete | ❌ | ❌ | ✅ |
| `findAll()` returns | `Iterable<T>` | `Iterable<T>` | **`List<T>`** ✅ |

```java
public interface UserRepository extends JpaRepository<User, Long> { }   // just use this ⭐
```

⭐ The small detail that shows you've used it: `JpaRepository.findAll()` returns a **`List`**, while `CrudRepository.findAll()` returns an `Iterable` — which you cannot `.stream()` or `.size()` directly.

#### Easy memory

```
Crud → PagingAndSorting → JpaRepository (each ADDS to the one before)
Just extend JpaRepository ⭐
JpaRepository.findAll() → List | CrudRepository.findAll() → Iterable
```

---

### Q: Explain `@Entity`, `@Id` and `@GeneratedValue`.

The easiest way to remember:

```
@Entity         → this CLASS  = a TABLE
@Id             → this FIELD  = the PRIMARY KEY
@GeneratedValue → who creates the id value
```

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;
}
```

```
class User            →  table `users`
field id              →  column id  (PRIMARY KEY, AUTO_INCREMENT)
field name            →  column name NOT NULL VARCHAR(100)
```

#### The generation strategies

```
IDENTITY  → the DATABASE auto-increments   → MySQL ⭐
SEQUENCE  → a database SEQUENCE object     → PostgreSQL / Oracle ⭐
AUTO      → the provider picks             → default
TABLE     → a separate table holds counters → slow, avoid
```

⭐ The detail worth knowing:

```
IDENTITY disables JDBC BATCH INSERTS.

Hibernate must ask the DB for the id after EVERY insert, so it cannot
batch 1,000 inserts into one round trip. SEQUENCE can (it pre-fetches ids).
```

That is why high-volume systems on PostgreSQL prefer `SEQUENCE`.

#### Entity requirements

```
✅ a NO-ARG constructor (Hibernate needs it to instantiate via reflection) ⭐
✅ not final; fields not final
✅ an @Id
```

That no-arg constructor requirement is exactly **why a `record` cannot be a JPA entity**.

#### Easy memory

```
@Entity = table | @Id = primary key | @GeneratedValue = who makes the id

IDENTITY → MySQL auto-increment (⚠️ blocks batch inserts)
SEQUENCE → Postgres/Oracle (batch-friendly ⭐)

Entities need a NO-ARG constructor → so a record can NEVER be an @Entity ⭐
```

---

### Q: What are derived query methods?

The easiest way to remember:

```
Spring READS THE METHOD NAME and writes the SQL for you.
```

```java
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByStatus(String status);
    User       findByEmail(String email);
    List<User> findByAgeGreaterThanAndStatus(int age, String status);
    List<User> findByNameContainingIgnoreCase(String name);
    long       countByStatus(String status);
    boolean    existsByEmail(String email);
    List<User> findTop5ByOrderBySalaryDesc();
}
```

How the name is parsed:

```
findBy  Age GreaterThan  And  Status
  │      │       │        │      │
 verb  field  operator  join   field
        ↓
SELECT * FROM users WHERE age > ? AND status = ?
```

The keywords worth remembering:

```
And / Or
GreaterThan / LessThan / Between
Like / Containing / StartingWith / EndingWith
IgnoreCase
IsNull / IsNotNull
In / NotIn
OrderBy...Asc / Desc
Top / First    →  findTop5By...
Distinct
```

Real-world idea: you write the **request in plain English** and it becomes SQL.

⚠️ Two traps:

```
1. A TYPO in a field name fails at STARTUP, not at runtime ✅
      "No property 'nmae' found for type User" — actually a GOOD thing:
      you find out immediately, not in production.

2. Long names become unreadable:
      findByStatusAndAgeGreaterThanAndDepartmentInOrderByNameAsc(...)
      → at that point switch to @Query. ⭐
```

#### Easy memory

```
The METHOD NAME becomes the query ⭐

findBy | countBy | existsBy | deleteBy
And/Or | GreaterThan | Between | Containing | IgnoreCase | OrderBy | Top5

Typo → fails at STARTUP (good!)
Name too long → switch to @Query
```

---

### Q: When and how do you use `@Query` (JPQL vs native)?

The easiest way to remember:

```
JPQL   → works on ENTITY names and FIELD names   (User, u.status)
Native → works on TABLE names and COLUMN names   (users, status)
```

```java
// JPQL — uses the ENTITY name and its fields
@Query("SELECT u FROM User u WHERE u.status = :status")
List<User> findActive(@Param("status") String status);

// NATIVE SQL — uses the real table and columns
@Query(value = "SELECT * FROM users WHERE age > ?1", nativeQuery = true)
List<User> findOlderThan(int age);

// MODIFYING query — needs BOTH annotations ⭐
@Modifying
@Transactional
@Query("UPDATE User u SET u.status = :s WHERE u.id = :id")
int updateStatus(@Param("id") Long id, @Param("s") String s);
```

```
JPQL:   SELECT u FROM User u      ← "User" is the CLASS, "u.status" is the FIELD
Native: SELECT * FROM users       ← "users" is the TABLE, "status" is the COLUMN
```

| | JPQL | Native SQL |
|---|---|---|
| Works on | entities and fields | tables and columns |
| Database portable | ✅ | ❌ tied to one DB |
| DB-specific features | ❌ | ✅ (window functions, hints) |
| Returns entities | ✅ automatically | needs mapping |

⚠️ The `@Modifying` trap:

```
Writing an UPDATE/DELETE @Query WITHOUT @Modifying
     → "Not supported for DML operations" 💥

And @Modifying queries BYPASS the persistence context, so entities
already loaded in memory keep the OLD values.
Fix: @Modifying(clearAutomatically = true) ⭐
```

#### Easy memory

```
@Query("SELECT u FROM User u ...")                    → JPQL (entity + fields)
@Query(value = "SELECT * FROM users", nativeQuery=true) → real SQL

UPDATE/DELETE → needs @Modifying + @Transactional ⭐
@Modifying bypasses the persistence context → clearAutomatically = true
```

---

### Q: Explain JPA relationships.

The easiest way to remember:

```
@OneToOne    → 1 ↔ 1     User ↔ Profile
@OneToMany   → 1 → many  Department → Employees
@ManyToOne   → many → 1  Employee → Department   ⭐ owns the FK
@ManyToMany  → many ↔ many  Students ↔ Courses (needs a join table)
```

```java
@Entity
public class Department {
    @Id @GeneratedValue private Long id;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<Employee> employees = new ArrayList<>();
}

@Entity
public class Employee {
    @Id @GeneratedValue private Long id;

    @ManyToOne(fetch = FetchType.LAZY)          // ⭐ always set LAZY here
    @JoinColumn(name = "department_id")          // this side holds the FK ⭐
    private Department department;
}
```

#### The concept that decides everything — the OWNING side ⭐

```
The side with the FOREIGN KEY COLUMN is the OWNER.
Only the OWNER's changes are written to the database.

@JoinColumn  → "I own it, I hold the FK"        ← Employee (many side)
mappedBy     → "I'm the mirror, HE owns it"     ← Department (one side)
```

```
   departments                employees
   ┌────┬──────┐             ┌────┬──────┬───────────────┐
   │ id │ name │             │ id │ name │ department_id │ ← the FK lives HERE
   └────┴──────┘             └────┴──────┴───────────────┘
                                                    ↑
                                    so Employee is the OWNING side
```

The classic bug this causes:

```java
department.getEmployees().add(employee);   // only touched the NON-owning side
save(department);
// → nothing is saved 💥 the FK column is never set
```

```java
employee.setDepartment(department);        // ✅ set the OWNING side
save(employee);
```

Best practice — set **both** sides with a helper method:

```java
public void addEmployee(Employee e) {
    employees.add(e);
    e.setDepartment(this);       // ⭐ keeps the object graph consistent
}
```

#### Easy memory

```
@ManyToOne  = the MANY side = holds the FK = the OWNING side ⭐ @JoinColumn
@OneToMany  = the ONE side  = mappedBy = the mirror

Only the OWNING side's changes are persisted.
Always set BOTH sides with a helper method.
```

---

### Q: Lazy vs Eager loading?

The easiest way to remember:

```
EAGER → load it NOW, along with the parent
LAZY  → load it ONLY when the code actually touches it ⭐
```

```
Defaults (worth memorising — they're asked directly):

@ManyToOne   → EAGER  ⚠️ (change this to LAZY!)
@OneToOne    → EAGER  ⚠️
@OneToMany   → LAZY   ✅
@ManyToMany  → LAZY   ✅
```

⭐ The memory hook: **"To-One is Eager, To-Many is Lazy."**

```java
@ManyToOne(fetch = FetchType.LAZY)      // ⭐ almost always override the default
private Department department;
```

#### Why EAGER hurts

```
SELECT * FROM employees                 (you asked for employees)
        ↓ @ManyToOne is EAGER
SELECT * FROM departments WHERE id = 1  ← extra query you never asked for
SELECT * FROM departments WHERE id = 2
...                                      ← the N+1 problem 💥
```

And EAGER is **contagious**: if `Department` eagerly loads `Company`, and `Company` eagerly loads `Address`, one `findById` can drag in half the database.

#### But LAZY has its own trap ⭐

```java
@Transactional
public Employee get(Long id) { return repo.findById(id).orElseThrow(); }

// later, OUTSIDE the transaction:
employee.getDepartment().getName();     // 💥 LazyInitializationException
```

```
Inside @Transactional  → the session is open  → the lazy proxy can load ✅
After the method ends  → the session is CLOSED → the proxy cannot load 💥
```

Real-world idea:

```
EAGER → ordering the entire restaurant menu when you sat down 🍽️😩
LAZY  → ordering each dish when you actually want it ✅
        (but the kitchen closes at 10pm — order after that and you get nothing 💥)
```

The correct fixes:

```
✅ JOIN FETCH or @EntityGraph  → fetch what you need WITHIN the transaction
✅ Map to a DTO inside the transactional service method
❌ spring.jpa.open-in-view=true  → hides the problem and causes N+1 in production
```

#### Easy memory

```
"To-One is EAGER, To-Many is LAZY" ⭐  → always override @ManyToOne to LAZY

EAGER → extra queries you never asked for (and it's contagious)
LAZY  → LazyInitializationException outside the transaction 💥

Fix: JOIN FETCH / @EntityGraph, and map to a DTO inside the service.
```

---

### Q: What is the N+1 query problem and how do you fix it?

The easiest way to remember:

```
1 query to load the parents
+ N queries, one per parent, to load its children
= N + 1 queries  💥
```

```java
List<Department> depts = repo.findAll();          // 1 query

for (Department d : depts) {
    d.getEmployees().size();                      // 1 query EACH 💥
}
```

```
SELECT * FROM departments                  ← 1 query
SELECT * FROM employees WHERE dept_id = 1  ← query 2
SELECT * FROM employees WHERE dept_id = 2  ← query 3
SELECT * FROM employees WHERE dept_id = 3  ← query 4
...
100 departments → 101 QUERIES 💥
```

Real-world idea: going to the market and coming home **101 times** — once for the shopping list, then once for each item — instead of buying everything in one trip.

#### The fixes

```java
// 1. JOIN FETCH — one query, explicit ⭐
@Query("SELECT DISTINCT d FROM Department d JOIN FETCH d.employees")
List<Department> findAllWithEmployees();

// 2. @EntityGraph — declarative, works with derived queries ⭐
@EntityGraph(attributePaths = "employees")
List<Department> findAll();

// 3. Batch fetching — turns N queries into N/size queries
@BatchSize(size = 25)
private List<Employee> employees;
// or globally: spring.jpa.properties.hibernate.default_batch_fetch_size=25
```

```
Before: 1 + 100 queries 💥
After : 1 query with a JOIN ✅
```

⚠️ Note the `DISTINCT` in the JOIN FETCH — a join multiplies parent rows by the number of children, so without it you get duplicate `Department` objects.

#### How to detect it

```properties
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
```

Then watch the log during one request. Seeing 101 SELECTs is unmistakable. Mentioning that you *look* for it is a strong practical signal.

#### Interview-ready answer

> The N+1 problem happens when we load N parent entities with one query, and then accessing a lazy association triggers one additional query per parent — so 100 departments becomes 101 queries. I fix it by fetching the association up front: either `JOIN FETCH` in a JPQL query, or `@EntityGraph` on the repository method, which works with derived queries too. For cases where a join isn't practical, Hibernate's batch fetching with `@BatchSize` reduces N queries to N divided by the batch size. I detect it by enabling SQL logging and watching the query count for a single request. With `JOIN FETCH` on a collection I also use `DISTINCT`, because the join duplicates the parent rows.

#### Easy memory

```
N+1 = 1 query for parents + 1 query PER parent for children 💥
      (going to the market 101 times)

FIX ⭐ JOIN FETCH (use DISTINCT!) | @EntityGraph | @BatchSize

Detect with: spring.jpa.show-sql=true
```

---

### Q: What is `@Transactional` and how does propagation work?

The easiest way to remember:

```
@Transactional = "ALL of this succeeds, or NONE of it does."

Success  → COMMIT
Exception→ ROLLBACK
```

```java
@Transactional
public void transfer(Long from, Long to, BigDecimal amt) {
    accountRepo.debit(from, amt);      // step 1
    accountRepo.credit(to, amt);       // step 2
}                                      // both commit, or both roll back ✅
```

```
Without @Transactional:
   debit succeeds  💰 −1000
   credit CRASHES  💥
   → the money has vanished 💥

With @Transactional:
   debit succeeds
   credit crashes → EVERYTHING rolls back → the money is safe ✅
```

Real-world idea: **a bank transfer.** It cannot be half done.

#### Propagation — what happens when a transaction calls another

```
REQUIRED (default) ⭐ → join the existing one, or create a new one
REQUIRES_NEW         → ALWAYS a new one; suspend the current
SUPPORTS             → join if one exists, otherwise run without
MANDATORY            → must already be inside one, else throw
NESTED               → a savepoint inside the current transaction
NEVER / NOT_SUPPORTED→ refuse / suspend
```

The one that matters in practice:

```
methodA() @Transactional
     ↓ calls
methodB() @Transactional(REQUIRED)      → SAME transaction
          B fails → A rolls back too ✅

methodB() @Transactional(REQUIRES_NEW)  → SEPARATE transaction
          B commits independently → an audit log survives even if A fails ⭐
```

That is the classic use of `REQUIRES_NEW`: **audit logging** you want to keep even when the business operation fails.

#### The three traps ⭐

**1. Rollback happens only on unchecked exceptions**

```java
@Transactional
public void save() throws IOException {
    repo.save(x);
    throw new IOException();       // ❌ CHECKED → NO ROLLBACK, it commits! 💥
}

@Transactional(rollbackFor = Exception.class)    // ✅ the fix
```

**2. Self-invocation bypasses the proxy** (the same AOP rule as `@Async`)

```java
public void a() {
    this.b();          // ❌ @Transactional on b() is IGNORED
}

@Transactional
public void b() { }
```

**3. It must be `public`** — a proxy cannot intercept a private or protected method.

#### Interview-ready answer

> `@Transactional` declaratively wraps a method in a database transaction — it commits when the method returns normally and rolls back when a runtime exception propagates. It is implemented with an AOP proxy. Propagation controls what happens when a transactional method calls another one: `REQUIRED` is the default and joins the existing transaction, while `REQUIRES_NEW` suspends the current transaction and starts an independent one, which is useful for audit logging that should survive a business rollback. There are three traps worth knowing: by default only unchecked exceptions trigger a rollback, so a checked exception commits unless we specify `rollbackFor`; self-invocation through `this` bypasses the proxy so the annotation has no effect; and the method must be public.

#### Easy memory

```
@Transactional = all-or-nothing (a bank transfer) ⭐

REQUIRED ⭐ default → join the existing transaction
REQUIRES_NEW       → independent transaction (audit logs) ⭐

⚠️ CHECKED exceptions do NOT roll back → rollbackFor = Exception.class
⚠️ this.method()   → the proxy is bypassed → the annotation is ignored
⚠️ must be public
```

---

### Q: How do you implement pagination?

The easiest way to remember:

```
Pass a Pageable in  →  get a Page back.
```

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByStatus(String status, Pageable pageable);
}
```

```java
Pageable pageable = PageRequest.of(0, 20, Sort.by("name").ascending());
//                                ↑page  ↑size   ← page is 0-BASED ⚠️

Page<User> page = userRepository.findByStatus("active", pageable);

List<User> content = page.getContent();
long total         = page.getTotalElements();
int  totalPages    = page.getTotalPages();
boolean hasNext    = page.hasNext();
```

```
Spring generates:
   SELECT * FROM users WHERE status='active' ORDER BY name LIMIT 20 OFFSET 0
   SELECT COUNT(*) FROM users WHERE status='active'     ← the second query ⭐
```

That second COUNT query is what gives you `getTotalElements()`.

#### `Page` vs `Slice` vs `List` ⭐

```
Page<T>  → content + TOTAL COUNT   → 2 queries → needed for "Page 3 of 47"
Slice<T> → content + hasNext only  → 1 query   → perfect for INFINITE SCROLL ⚡
List<T>  → just the content        → 1 query   → no paging metadata
```

If your UI is infinite scroll, `Slice` skips an expensive `COUNT(*)` on a large table.

From a controller, Spring can build it automatically:

```java
@GetMapping("/users")
public Page<User> list(Pageable pageable) {       // ?page=0&size=20&sort=name,asc
    return repo.findAll(pageable);
}
```

Real-world idea: **Google search results.** You never receive all 4 million results — you get 10, plus "next".

#### Easy memory

```
PageRequest.of(page, size, Sort.by("name"))   ← page is 0-BASED ⚠️
Page<T> = content + total (2 queries: data + COUNT)
Slice<T> = content + hasNext (1 query) → infinite scroll ⭐

Controllers accept Pageable directly: ?page=0&size=20&sort=name,asc
```

---

## Security & Misc

### Q: Spring Security basics — authentication vs authorization?

The easiest way to remember:

```
AuthentiCation → WHO are you?          → login  → 401 if it fails
AuthoriZation  → WHAT may you do?      → roles  → 403 if it fails
```

```
        Request
           ↓
   ┌───────────────────┐
   │ AUTHENTICATION    │  who are you?      → fail → 401 Unauthorized
   └────────┬──────────┘
            ↓
   ┌───────────────────┐
   │ AUTHORIZATION     │  are you allowed?  → fail → 403 Forbidden
   └────────┬──────────┘
            ↓
       Your controller ✅
```

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(c -> c.disable())                       // stateless API → CSRF not needed
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())            // ⭐ always end with this
        .httpBasic(Customizer.withDefaults());
    return http.build();
}
```

Spring Security is a **chain of filters** sitting in front of your application:

```
Request → [ CORS ] → [ CSRF ] → [ Auth filter ] → [ Authorization ] → Controller
```

⚠️ Order matters: rules are matched **top to bottom**, first match wins. Put `anyRequest()` **last**, or it swallows everything below it.

Real-world idea: an **office building**.

```
Authentication → your ID card opens the front door        (who you are)
Authorization  → that card does NOT open the server room  (what you may do)
```

#### Easy memory

```
AuthentiCation = who? → login → 401
AuthoriZation  = what? → roles → 403 ⭐

SecurityFilterChain bean → permitAll / hasRole / authenticated
Rules match TOP-DOWN → put anyRequest() LAST ⚠️
```

---

### Q: Explain the JWT authentication flow.

The easiest way to remember:

```
JWT = a signed ID card the client carries on EVERY request.
      The server keeps NO session. ⭐
```

```
1. POST /login  { username, password }
        ↓
2. Server validates → creates a SIGNED token → returns it
        ↓
3. Client stores it and sends it every time:
        Authorization: Bearer eyJhbGci...
        ↓
4. A filter validates the SIGNATURE + EXPIRY on each request
        ↓
5. Sets the SecurityContext → the controller runs ✅
   (no server-side session at all)
```

#### The three parts of a JWT

```
eyJhbGciOiJIUzI1NiJ9  .  eyJzdWIiOiIxMjMifQ  .  SflKxwRJSMeKKF2QT4f
   HEADER                    PAYLOAD                SIGNATURE
   algorithm                 claims: user id,       proves it wasn't
                             roles, expiry          tampered with ⭐
```

⚠️ The point everyone gets wrong:

```
The payload is BASE64-ENCODED, not ENCRYPTED.
Anyone can decode and READ it. 👀

→ NEVER put a password or sensitive data in a JWT.
→ The signature only proves it wasn't CHANGED, not that it's secret.
```

#### Why JWT for microservices ⭐

```
Session-based:  every server must share the session store
                → sticky sessions, or Redis, or you get logged out randomly

JWT:            the token carries the identity itself
                → ANY server instance can validate it independently ✅
                → scales horizontally with no shared state
```

Real-world idea: a **cinema ticket**. The staff don't phone the box office — the ticket itself is proof, and it's stamped so it can't be forged.

The trade-off to mention:

```
✅ Stateless, scalable, works across services
❌ You cannot easily REVOKE one before it expires
   → so keep access tokens SHORT-lived (15 min) + use a refresh token ⭐
```

#### Easy memory

```
login → signed JWT → client sends "Authorization: Bearer <token>" every request
       → filter checks signature + expiry → SecurityContext → controller

header . payload . signature   ← payload is READABLE (base64, not encrypted) ⚠️
Stateless ⭐ any instance can validate it — but you can't revoke it early
→ short-lived access token + refresh token
```

---

### Q: DTO vs Entity — why separate them?

The easiest way to remember:

```
Entity = the DATABASE shape
DTO    = the API shape

Never let the outside world see your database. ⭐
```

```java
@Entity
public class User {
    @Id private Long id;
    private String name;
    private String email;
    private String password;         // 💥 NEVER send this out
    private String role;             // 💥 a client must not be able to set this
    @OneToMany private List<Order> orders;    // 💥 lazy → serialisation blows up
}

public record UserDto(Long id, String name, String email) { }   // exactly what the API needs ✅
```

#### The five reasons — say two or three

```
1. SECURITY        → password, internal flags and salary never leave the server
2. OVER-POSTING    → a client POSTs {"role":"ADMIN"} and privilege-escalates 💥
3. DECOUPLING      → renaming a DB column shouldn't break every mobile client
4. LAZY LOADING    → serialising an entity touches lazy collections
                     → LazyInitializationException, or an accidental N+1
5. SHAPING         → the API can combine or flatten data the tables don't match
```

```
       DATABASE                    API
   ┌──────────────┐          ┌──────────────┐
   │ User entity  │  ──map──▶│ UserDto      │
   │ id           │          │ id           │
   │ name         │          │ name         │
   │ email        │          │ email        │
   │ password 🔒  │  ✗ never │              │
   │ role     🔒  │  ✗ never │              │
   │ orders (lazy)│  ✗ never │              │
   └──────────────┘          └──────────────┘
```

Map them manually, or with MapStruct:

```java
UserDto toDto(User u) {
    return new UserDto(u.getId(), u.getName(), u.getEmail());
}
```

Real-world idea: a **restaurant menu**. The customer sees dish names and prices — not the supplier invoices, the stock levels or the staff salaries.

#### Easy memory

```
Entity = DB shape | DTO = API shape ⭐

Why: security (no password) | over-posting ("role":"ADMIN") 💥 |
     decoupling | no lazy-loading blowups | API shaping

Records make perfect DTOs. Map with MapStruct or a simple method.
```

---

### Q: How do you handle CORS in Spring Boot?

The easiest way to remember:

```
CORS = the BROWSER refusing to let one website call a different domain's API,
       unless that API explicitly allows it.
```

```
Angular app at  http://localhost:4200
Spring API at   http://localhost:8080        ← a DIFFERENT origin
        ↓
Browser: "Blocked by CORS policy" 💥
```

⭐ The point that impresses: **CORS is a browser rule, not a server rule.** Postman and curl work perfectly, which is exactly why the error only ever appears in the browser and confuses people.

```java
// Per controller
@CrossOrigin(origins = "https://app.example.com")
@RestController
public class UserController { }

// Global — the better way ⭐
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://app.example.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

#### The preflight request ⭐

```
For anything beyond a simple GET, the browser first sends:

   OPTIONS /api/users
   Origin: http://localhost:4200
   Access-Control-Request-Method: POST
        ↓
   Server replies with the Access-Control-Allow-* headers
        ↓
   ONLY THEN does the browser send the real POST ✅
```

That is why you sometimes see **two** requests in the network tab for one call.

⚠️ With Spring Security you must **also** enable CORS there, or the security filter chain blocks the preflight before your MVC config is ever reached:

```java
http.cors(Customizer.withDefaults());     // ⭐ easily forgotten
```

Also: `allowedOrigins("*")` **cannot** be combined with `allowCredentials(true)` — use `allowedOriginPatterns` instead.

#### Easy memory

```
CORS = a BROWSER rule ⭐ (Postman/curl are unaffected)

@CrossOrigin on a controller, or a global WebMvcConfigurer ⭐
Preflight OPTIONS request happens first for non-simple requests

⚠️ With Spring Security also add: http.cors(...)
⚠️ "*" + allowCredentials(true) is illegal
```

---

## Reactive (Spring WebFlux)

### Q: Spring WebFlux vs Spring MVC?

The easiest way to remember:

```
Spring MVC     → ONE THREAD per request, and it BLOCKS while waiting
Spring WebFlux → a FEW threads handle THOUSANDS of requests, never blocking
```

```
SPRING MVC (thread per request)
  req1 → thread1 → waits 200ms for the DB 😴 (thread is stuck, doing nothing)
  req2 → thread2 → waits 😴
  ...
  req201 → NO THREAD LEFT → queued 💥      (Tomcat default is ~200 threads)

SPRING WEBFLUX (event loop)
  req1 → starts the DB call → thread is RELEASED ⚡
  req2 → starts the DB call → same thread reused
  ...
  the response arrives → a thread picks it up and finishes ✅
```

| | Spring MVC | Spring WebFlux |
|---|---|---|
| Model | thread per request, blocking | event loop, non-blocking |
| Built on | Servlet API | Project Reactor |
| Default server | Tomcat | Netty |
| Return types | `User`, `List<User>` | `Mono<User>`, `Flux<User>` |
| Learning curve | easy ✅ | steep |
| Debugging | normal stack traces | painful stack traces ⚠️ |
| Best for | most applications ⭐ | very high concurrency, streaming |

⚠️ The honest, senior answer:

```
WebFlux only helps if the WHOLE chain is non-blocking.

One blocking JDBC call inside a WebFlux app makes it SLOWER than MVC —
you've added complexity and blocked the small event-loop pool. 💥

(Real reactive data access needs R2DBC or reactive MongoDB, not JPA.)
```

Real-world idea: a **restaurant**.

```
MVC     → each waiter stands at ONE table until that meal is finished 😴
WebFlux → waiters take an order, move to the next table, and return
          when the food is ready ⚡ — far fewer waiters serve far more tables
```

#### Interview-ready answer

> Spring MVC uses a thread-per-request, blocking model on the Servlet API, so a thread is occupied for the whole request including the time it spends waiting on the database. Spring WebFlux is asynchronous and non-blocking, built on Project Reactor and running on Netty by default, so a small number of event-loop threads can handle a very large number of concurrent connections. WebFlux is worth it for high-concurrency, I/O-bound or streaming workloads, but only if the entire chain is non-blocking — a blocking JDBC call inside a WebFlux application will block the event loop and perform worse than MVC. For most CRUD applications, including mine, MVC is simpler and the right choice.

#### Easy memory

```
MVC     → thread per request, BLOCKS  → Tomcat  → simple ⭐ (default choice)
WebFlux → event loop, NON-blocking    → Netty   → Mono / Flux

Only worth it if EVERYTHING is non-blocking (R2DBC, not JPA) ⚠️
One blocking call ruins it.
```

---

### Q: `Mono` vs `Flux`?

The easiest way to remember:

```
Mono<T> → 0 or 1 item     ("one result, or none")
Flux<T> → 0 to N items    ("a stream of results")
```

```
Mono<User> ──────────[user]───────▶|      (one item, then complete)

Flux<User> ──[u1]──[u2]──[u3]──────▶|     (many items, then complete)
```

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public Mono<User> getById(@PathVariable String id) {
        return userService.findById(id);   // 0 or 1
    }

    @GetMapping
    public Flux<User> getAll() {
        return userService.findAll();      // 0..N, and it can STREAM
    }
}
```

```
Blocking equivalent          Reactive equivalent
──────────────────           ───────────────────
User                    →    Mono<User>
List<User>              →    Flux<User>
void                    →    Mono<Void>
Optional<User>          →    Mono<User> ⭐ (empty Mono = not found)
```

⭐ The critical property: **they are LAZY.**

```java
Mono<User> mono = service.findById("1");   // NOTHING has happened yet
mono.subscribe(...);                        // NOW it runs ⚡
```

```
"Nothing happens until you subscribe."
```

In a Spring controller, the **framework subscribes for you** — which is why returning a `Mono` just works.

Real-world idea:

```
Mono → ordering ONE coffee ☕
Flux → a subscription that delivers a coffee every morning ☕☕☕

Both are just the ORDER. Nothing is brewed until you actually place it (subscribe).
```

#### Easy memory

```
Mono<T> → 0 or 1  (a single result)     ← replaces User / Optional<User>
Flux<T> → 0 to N  (a stream)            ← replaces List<User>

LAZY ⭐ — nothing happens until subscribe()
In a controller, Spring subscribes for you.
```

---

## Quick Revision Sheet

Read this the night before.

```
SPRING CORE
  IoC = the principle | DI = the technique (container injects)
  ApplicationContext ⊃ BeanFactory (eager singletons at STARTUP ⭐)
  Scopes: singleton (default) ⭐ | prototype | request | session
     ⚠️ singleton + mutable field = thread-safety bug → keep @Service STATELESS
  Lifecycle: construct → inject → @PostConstruct ⭐ → use → @PreDestroy
  @Component/@Service/@Controller = semantic
  @Repository ⭐ ADDS exception translation → DataAccessException
  CONSTRUCTOR injection ⭐ (final, testable, exposes too many deps)
  @Qualifier names one bean | @Primary = default | @Qualifier BEATS @Primary
  @Bean = for third-party classes; @Configuration classes are CGLIB-proxied
  AOP = CCTV 📹 | Aspect/Advice/Pointcut/JoinPoint
     ⚠️ SELF-INVOCATION (this.method()) bypasses the proxy — kills @Transactional/@Async

SPRING BOOT
  @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
     ⚠️ main class must be in the ROOT package (scanning goes DOWN only)
  Auto-config = @ConditionalOnClass / OnMissingBean ⭐ (YOUR bean always wins)
     debug=true → prints the auto-configuration report ⭐
  Starters = combo meals 🍱 | embedded Tomcat → java -jar
  Profiles: application-{profile}.properties + --spring.profiles.active=prod
  Actuator: /health ⭐ /metrics /loggers — only health+info exposed by default
  CommandLineRunner → runs once after startup

REST
  @RestController = @Controller + @ResponseBody → JSON
  /users/42 = @PathVariable | ?x= = @RequestParam | JSON = @RequestBody
  ResponseEntity = STATUS + HEADERS + BODY ⭐  (POST should return 201 + Location)
  401 = who are you? | 403 = I know you, still NO ⭐
  @RestControllerAdvice + @ExceptionHandler = one global handler ⭐
  Validation: DTO annotations + @Valid on the parameter (BOTH needed ⚠️)
     @NotBlank for Strings (@NotNull lets "" through)
  Idempotent: GET/PUT/DELETE ✅ POST ❌ → use an Idempotency-Key for payments
  Version with /api/v1/...  | DTOs, never entities ⭐

JPA
  Declare an interface → Spring generates the implementation
  extend JpaRepository ⭐ (findAll returns List)
  @Entity/@Id/@GeneratedValue: IDENTITY (MySQL) | SEQUENCE (Postgres, batch-friendly)
     Entities need a NO-ARG constructor → a record can NEVER be an @Entity ⭐
  Derived queries: findByStatusAndAgeGreaterThan... (typo fails at STARTUP)
  @Query JPQL (entities) vs nativeQuery (tables); UPDATE needs @Modifying ⭐
  @ManyToOne = the FK side = the OWNING side ⭐ | @OneToMany = mappedBy = mirror
  "To-One is EAGER, To-Many is LAZY" ⭐ → always make @ManyToOne LAZY
  N+1 ⭐ → JOIN FETCH (with DISTINCT) | @EntityGraph | @BatchSize
  @Transactional = all-or-nothing; REQUIRED default, REQUIRES_NEW for audit logs
     ⚠️ CHECKED exceptions do NOT roll back → rollbackFor
     ⚠️ self-invocation + must be public
  Pagination: PageRequest.of(page, size, sort) — page is 0-BASED
     Page (2 queries, has total) vs Slice (1 query, infinite scroll) ⭐

SECURITY
  Authentication = who (401) | Authorization = what (403) ⭐
  JWT: header.payload.signature — payload is READABLE (base64, not encrypted) ⚠️
       stateless ⭐ but cannot be revoked → short-lived + refresh token
  CORS is a BROWSER rule ⭐ (Postman is fine); preflight OPTIONS;
       with Spring Security also add http.cors(...)

WEBFLUX
  MVC = thread per request, blocking ⭐ (default choice)
  WebFlux = event loop, non-blocking, Netty — only if EVERYTHING is non-blocking
  Mono = 0..1 | Flux = 0..N | LAZY — nothing happens until subscribe
```

---

## Related files

- [05-java.md](./05-java.md) — Core Java
- [24-orm-jpa-hibernate.md](./24-orm-jpa-hibernate.md) — JPA/Hibernate deep dive
- [08-microservices-basics.md](./08-microservices-basics.md) — microservices patterns
- [12-java17-features.md](./12-java17-features.md) — records for DTOs, text blocks for queries
- [32-multithreading.md](./32-multithreading.md) — `@Async`, thread pools, stateless beans
