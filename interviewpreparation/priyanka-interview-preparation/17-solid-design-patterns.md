# Java — SOLID Principles & Design Patterns

Interview Q&A for a **Full Stack Developer (Java + Spring Boot, 5 years)**. This is the round where interviewers separate "writes code" from "designs code". Examples are tied to your real projects — **RoboGebra** (AI math explanation engine, quiz module), **EasyVisa** (document portal), **Subsea** (Schedule-Manager).

> **How this gets asked:** rarely as "define SRP". Usually as *"Which SOLID principle does this code violate?"*, *"Which design pattern have you actually used?"*, or *"How would you design X so adding a new type doesn't break existing code?"* Prepare **one real story per principle/pattern** — that is what scores.

---

## Table of contents

- [Part 1 — SOLID Principles](#part-1--solid-principles)
- [Part 2 — Design Patterns: fundamentals](#part-2--design-patterns-fundamentals)
- [Part 3 — Creational patterns](#part-3--creational-patterns)
- [Part 4 — Structural patterns](#part-4--structural-patterns)
- [Part 5 — Behavioural patterns](#part-5--behavioural-patterns)
- [Part 6 — Patterns in the JDK, Spring & Angular](#part-6--patterns-in-the-jdk-spring--angular)
- [Part 7 — Scenario & "spot the violation" questions](#part-7--scenario--spot-the-violation-questions)
- [Part 8 — Rapid-fire one-liners](#part-8--rapid-fire-one-liners)

---

# Part 1 — SOLID Principles

### Q: What is SOLID and why does it matter?

SOLID is five object-oriented design principles (coined by Robert C. Martin) that keep code **easy to change** rather than merely working:

| Letter | Principle | One-line meaning |
|---|---|---|
| **S** | Single Responsibility | A class should have only one reason to change |
| **O** | Open/Closed | Open for extension, closed for modification |
| **L** | Liskov Substitution | A subtype must be usable anywhere its parent is, without surprises |
| **I** | Interface Segregation | Many small, focused interfaces beat one fat interface |
| **D** | Dependency Inversion | Depend on abstractions, not concrete implementations |

**Why it matters (say this):** *"Following SOLID means a new requirement usually means adding a class, not editing five existing ones — which keeps the regression surface small and unit tests easy, because dependencies can be mocked."*

---

### Q: Explain the Single Responsibility Principle (SRP) with an example.

**A class should have only one reason to change.** "Responsibility" = one *actor* / one axis of change, not "one method".

**Violation** — this class changes if the tax rules change, if the DB schema changes, *or* if the email template changes. Three reasons:

```java
class InvoiceService {
    double calculateTax(Invoice inv) { /* tax rules */ }
    void saveToDatabase(Invoice inv) { /* JDBC */ }
    void sendEmail(Invoice inv)      { /* SMTP */ }
}
```

**Fixed** — separate the axes of change:

```java
class TaxCalculator      { double calculate(Invoice inv) { ... } }
class InvoiceRepository  { void save(Invoice inv) { ... } }
class InvoiceNotifier    { void notifyCustomer(Invoice inv) { ... } }

@Service
class InvoiceService {                          // now only orchestrates
    private final TaxCalculator tax;
    private final InvoiceRepository repo;
    private final InvoiceNotifier notifier;
    // constructor injection...

    void process(Invoice inv) {
        inv.setTax(tax.calculate(inv));
        repo.save(inv);
        notifier.notifyCustomer(inv);
    }
}
```

**Your project answer (RoboGebra):**
> *"Our first cut of the explanation engine had one `ExplanationService` that built the AI prompt, called the model, parsed the step-by-step response, and did the Tamil/English translation. Any prompt tweak risked breaking translation. We split it into `PromptBuilder`, `AiClient`, `ExplanationParser` and `TranslationService` — the service just orchestrates. After that, adding bilingual output only touched one class."*

> ⚠️ **Common trap:** SRP does *not* mean "one method per class". Don't over-split — that creates anaemic classes and a maze of indirection. The test is: *"can I name two different people/reasons that would ask me to change this class?"*

---

### Q: Explain the Open/Closed Principle (OCP).

**Software entities should be open for extension but closed for modification.** You add behaviour by adding new code, not editing tested code.

**Violation** — every new payment/question type means editing this method and re-testing it:

```java
class QuestionEvaluator {
    boolean evaluate(Question q, String answer) {
        if (q.getType().equals("MCQ"))          { /* ... */ }
        else if (q.getType().equals("NUMERIC")) { /* ... */ }
        else if (q.getType().equals("MATCH"))   { /* ... */ }   // keeps growing
        return false;
    }
}
```

**Fixed** — polymorphism (this is Strategy + OCP together):

```java
interface QuestionEvaluator {
    boolean supports(QuestionType type);
    boolean evaluate(Question q, String answer);
}

@Component
class McqEvaluator implements QuestionEvaluator {
    public boolean supports(QuestionType t) { return t == QuestionType.MCQ; }
    public boolean evaluate(Question q, String answer) { /* ... */ }
}

@Service
class EvaluationService {
    private final List<QuestionEvaluator> evaluators;   // Spring injects ALL implementations

    EvaluationService(List<QuestionEvaluator> evaluators) { this.evaluators = evaluators; }

    boolean evaluate(Question q, String answer) {
        return evaluators.stream()
            .filter(e -> e.supports(q.getType()))
            .findFirst()
            .orElseThrow(() -> new UnsupportedQuestionTypeException(q.getType()))
            .evaluate(q, answer);
    }
}
```

Now a new question type = **one new `@Component`**. `EvaluationService` is never touched.

> 💡 **High-scoring detail:** *"Spring injecting a `List<T>` or `Map<String, T>` of all beans implementing an interface is the practical way to do OCP in Spring Boot."* Interviewers love this.

---

### Q: Explain the Liskov Substitution Principle (LSP).

**Objects of a subclass must be replaceable by their superclass without breaking correctness.** A subclass may not weaken guarantees the parent made.

**The classic violation:**

```java
class Rectangle {
    protected int width, height;
    void setWidth(int w)  { this.width = w; }
    void setHeight(int h) { this.height = h; }
    int area() { return width * height; }
}

class Square extends Rectangle {                 // "a square IS-A rectangle" — mathematically true
    @Override void setWidth(int w)  { this.width = w; this.height = w; }
    @Override void setHeight(int h) { this.width = h; this.height = h; }
}

// Client code that works for Rectangle silently breaks for Square:
void resize(Rectangle r) {
    r.setWidth(5);
    r.setHeight(4);
    assert r.area() == 20;   // fails for Square — area is 16
}
```

**A more realistic Java one:**

```java
class ReadOnlyDocumentStore implements DocumentStore {
    public Document get(String id) { ... }
    public void save(Document d) {
        throw new UnsupportedOperationException();   // ❌ LSP violation
    }
}
```

Any caller holding a `DocumentStore` now has to know the concrete type — the abstraction lies. (This is exactly why `Arrays.asList()` returning a fixed-size list that throws on `add()` is a famous LSP smell in the JDK.)

**Rules a subclass must respect:**
- **Preconditions may not be strengthened** (don't reject inputs the parent accepted)
- **Postconditions may not be weakened** (must still deliver what the parent promised)
- **Invariants must be preserved**
- **Don't throw new checked exceptions** the parent didn't declare

**Fix:** prefer composition or split the interface (`ReadableStore` / `WritableStore` — which is ISP).

---

### Q: Explain the Interface Segregation Principle (ISP).

**No client should be forced to depend on methods it doesn't use.** Prefer several small role-based interfaces over one fat one.

**Violation:**

```java
interface DocumentOperations {
    void upload(MultipartFile f);
    void download(String id);
    void delete(String id);
    void digitallySign(String id);
    void ocrExtract(String id);
}

class SimpleAttachmentHandler implements DocumentOperations {
    public void digitallySign(String id) { throw new UnsupportedOperationException(); } // ❌
    public void ocrExtract(String id)    { throw new UnsupportedOperationException(); } // ❌
    // ...
}
```

**Fixed:**

```java
interface DocumentStorage { void upload(MultipartFile f); void download(String id); }
interface Signable        { void digitallySign(String id); }
interface OcrCapable      { void ocrExtract(String id); }

class SimpleAttachmentHandler implements DocumentStorage { ... }
class VisaFormHandler implements DocumentStorage, Signable, OcrCapable { ... }
```

**Your project answer (EasyVisa):**
> *"In the EasyVisa document portal, different panel types supported different capabilities — some documents needed attorney signature, some were plain uploads. Rather than one interface with `UnsupportedOperationException` holes, we kept a small `DocumentStorage` contract and mixed in capability interfaces. It also made the Angular side simpler, because the API contract per document type was honest."*

> 💡 ISP and LSP are cousins: a fat interface **forces** implementations to throw `UnsupportedOperationException`, which is an LSP violation. Segregating the interface removes the cause.

---

### Q: Explain the Dependency Inversion Principle (DIP).

Two parts:
1. **High-level modules should not depend on low-level modules; both should depend on abstractions.**
2. **Abstractions should not depend on details; details should depend on abstractions.**

**Violation** — the service is welded to MongoDB and to one notifier:

```java
@Service
class StudyListService {
    private final MongoStudyListRepository repo = new MongoStudyListRepository(); // ❌ concrete + new
    private final EmailNotifier notifier = new EmailNotifier();                   // ❌
}
```
Can't unit-test without a database, and swapping storage means editing the service.

**Fixed** — depend on interfaces, inject via constructor:

```java
public interface StudyListRepository { StudyList save(StudyList s); Optional<StudyList> findById(String id); }
public interface Notifier { void notify(String userId, String message); }

@Service
public class StudyListService {
    private final StudyListRepository repo;
    private final Notifier notifier;

    public StudyListService(StudyListRepository repo, Notifier notifier) {   // constructor injection
        this.repo = repo;
        this.notifier = notifier;
    }
}
```

The **interface belongs to the high-level module** (the domain), and the MongoDB adapter implements it — that is the "inversion": the arrow of dependency now points from the low-level detail *toward* the domain, not the other way.

---

### Q: What's the difference between Dependency Inversion, Dependency Injection, and IoC?

Very common follow-up — get this crisp:

| Term | What it is |
|---|---|
| **Dependency Inversion (DIP)** | A **design principle** — depend on abstractions |
| **Inversion of Control (IoC)** | A **broader pattern** — the framework controls flow/object creation instead of your code ("don't call us, we'll call you") |
| **Dependency Injection (DI)** | A **technique/implementation of IoC** — dependencies are handed to an object (constructor / setter / field) rather than created by it |

> *"Spring's `ApplicationContext` is an IoC container; `@Autowired`/constructor injection is DI; and the reason we code to interfaces so it works cleanly is DIP."*

---

### Q: Which injection type should you use and why?

**Constructor injection.** Reasons:
- Dependencies can be `final` → **immutable**, thread-safe
- **Fails fast** at startup if a dependency is missing
- Makes the class **testable without Spring** (`new StudyListService(mockRepo, mockNotifier)`)
- Exposes SRP violations — a 9-argument constructor screams "this class does too much"
- Field injection (`@Autowired` on a field) hides dependencies and can't be set in a plain unit test

Since Spring 4.3, **`@Autowired` is optional if the class has a single constructor.**

---

### Q: Where does SOLID conflict with reality?

Good senior-level answer:
> *"SOLID is a set of heuristics, not laws. Applying OCP everywhere leads to speculative abstraction — interfaces with exactly one implementation forever. My rule is the **rule of three**: hard-code the first case, note the duplication on the second, abstract on the third, when I actually know what varies. Premature abstraction is harder to remove than duplication."*

---

# Part 2 — Design Patterns: fundamentals

### Q: What is a design pattern? What are the categories?

A **reusable, named solution to a recurring design problem** — a template, not copy-paste code. The Gang of Four (GoF) catalogued 23 in three categories:

| Category | Concern | Key patterns |
|---|---|---|
| **Creational** | *How objects are created* | Singleton, Factory Method, Abstract Factory, Builder, Prototype |
| **Structural** | *How objects are composed* | Adapter, Decorator, Facade, Proxy, Composite, Bridge, Flyweight |
| **Behavioural** | *How objects communicate* | Strategy, Observer, Template Method, Chain of Responsibility, Command, Iterator, State, Mediator, Visitor |

**Memory hook:** Creational = *creation*, Structural = *composition*, Behavioural = *communication*.

---

# Part 3 — Creational patterns

### Q: Explain the Singleton pattern. How do you make it thread-safe?

**Ensures exactly one instance exists and gives a global access point.** Used for caches, config holders, connection pools, loggers.

**Broken (not thread-safe)** — two threads can both pass the null check:

```java
class Config {
    private static Config instance;
    private Config() {}
    public static Config getInstance() {
        if (instance == null) instance = new Config();   // ❌ race condition
        return instance;
    }
}
```

**1. Eager initialization** — simplest, thread-safe by classloader guarantee. Use when creation is cheap:

```java
class Config {
    private static final Config INSTANCE = new Config();
    private Config() {}
    public static Config getInstance() { return INSTANCE; }
}
```

**2. Double-checked locking with `volatile`** — lazy + thread-safe:

```java
class Config {
    private static volatile Config instance;      // volatile is MANDATORY
    private Config() {}
    public static Config getInstance() {
        if (instance == null) {                   // 1st check — no lock, fast path
            synchronized (Config.class) {
                if (instance == null) {           // 2nd check — under lock
                    instance = new Config();
                }
            }
        }
        return instance;
    }
}
```

> **Why `volatile`?** `new Config()` is not atomic — allocate, run constructor, assign reference. Without `volatile`, the JIT/CPU may reorder so the reference is assigned *before* the constructor finishes; another thread then sees a non-null but **partially constructed** object. `volatile` forbids that reordering and guarantees visibility across threads.

**3. Bill Pugh / holder idiom** — lazy, thread-safe, no synchronization cost. **Best classic approach:**

```java
class Config {
    private Config() {}
    private static class Holder { static final Config INSTANCE = new Config(); }
    public static Config getInstance() { return Holder.INSTANCE; }   // class loaded on first call
}
```

**4. Enum singleton** — Joshua Bloch's recommendation; the only one immune to reflection and serialization attacks:

```java
public enum Config {
    INSTANCE;
    public void load() { ... }
}
```

---

### Q: How can a Singleton be broken, and how do you defend it?

| Attack | Defence |
|---|---|
| **Reflection** — `constructor.setAccessible(true)` | Throw from the constructor if the instance already exists; or use an `enum` |
| **Serialization** — deserializing creates a new object | Implement `readResolve()` returning the instance; or use an `enum` |
| **Cloning** | Override `clone()` to throw `CloneNotSupportedException` |
| **Multiple classloaders** | Each classloader gets its own instance — usually acceptable, else load explicitly |

---

### Q: Is a Spring `@Service` bean a Singleton pattern?

**No — it's singleton *scope*, which is different.** Great question to nail:

| GoF Singleton | Spring singleton scope |
|---|---|
| One instance **per JVM/classloader** | One instance **per ApplicationContext** |
| Enforced by a private constructor | Managed by the container; the class stays a normal POJO |
| Hard to test/mock | Trivially mockable — it's just a class |
| Global static access | Injected, so dependencies are explicit |

Two contexts in one JVM → two instances of the same `@Service`. Also: **singleton beans are not automatically thread-safe** — keep them stateless, because one instance serves all concurrent requests. Mutable instance fields on a `@Service` are a classic production bug.

---

### Q: Explain the Factory Method pattern.

**Defines an interface for creating an object but lets subclasses / a factory decide which concrete class to instantiate.** It decouples the caller from `new`.

```java
public interface ExplanationGenerator { Explanation generate(MathProblem p); }

class AlgebraExplanationGenerator   implements ExplanationGenerator { ... }
class GeometryExplanationGenerator  implements ExplanationGenerator { ... }
class CalculusExplanationGenerator  implements ExplanationGenerator { ... }

@Component
public class ExplanationGeneratorFactory {
    private final Map<Topic, ExplanationGenerator> registry;

    // Spring injects every implementation, keyed by bean name — or build the map explicitly
    public ExplanationGeneratorFactory(List<ExplanationGenerator> generators) {
        this.registry = generators.stream()
            .collect(Collectors.toMap(ExplanationGenerator::topic, Function.identity()));
    }

    public ExplanationGenerator forTopic(Topic topic) {
        var gen = registry.get(topic);
        if (gen == null) throw new IllegalArgumentException("No generator for " + topic);
        return gen;
    }
}
```

**Benefit:** callers depend on the interface only; adding a new topic doesn't touch the caller (**OCP**).

> **Simple Factory vs Factory Method:** a "simple factory" is a single class with a `switch` — not a GoF pattern, but common and fine. **Factory Method** puts creation behind an overridable method so subclasses choose the type.

---

### Q: Factory Method vs Abstract Factory?

| | Factory Method | Abstract Factory |
|---|---|---|
| Creates | **One** product | A **family** of related products |
| Mechanism | Inheritance (override a method) | Composition (a factory object with several create methods) |
| Example | `createExplanation()` | `UiFactory` → `createButton()`, `createCheckbox()` for Material vs Ionic themes |

```java
interface ReportFactory {                 // Abstract Factory — a family
    Header  createHeader();
    Body    createBody();
    Footer  createFooter();
}
class PdfReportFactory  implements ReportFactory { ... }
class ExcelReportFactory implements ReportFactory { ... }
```
Guarantees you never mix a PDF header with an Excel footer.

---

### Q: Explain the Builder pattern and when to use it.

**Constructs a complex object step by step**, so you avoid telescoping constructors and get readable, validated construction.

Use when: many parameters, several optional, and/or you want immutability.

```java
public final class QuizConfig {
    private final String topic;          // required
    private final int questionCount;     // required
    private final Difficulty difficulty; // optional
    private final boolean bilingual;     // optional
    private final Duration timeLimit;    // optional

    private QuizConfig(Builder b) {
        this.topic = b.topic;
        this.questionCount = b.questionCount;
        this.difficulty = b.difficulty;
        this.bilingual = b.bilingual;
        this.timeLimit = b.timeLimit;
    }

    public static Builder builder(String topic, int questionCount) {
        return new Builder(topic, questionCount);
    }

    public static class Builder {
        private final String topic;
        private final int questionCount;
        private Difficulty difficulty = Difficulty.MEDIUM;   // sensible defaults
        private boolean bilingual = false;
        private Duration timeLimit = Duration.ofMinutes(10);

        Builder(String topic, int questionCount) {
            this.topic = topic;
            this.questionCount = questionCount;
        }

        public Builder difficulty(Difficulty d) { this.difficulty = d; return this; }  // fluent
        public Builder bilingual(boolean b)     { this.bilingual = b;  return this; }
        public Builder timeLimit(Duration d)    { this.timeLimit = d;  return this; }

        public QuizConfig build() {
            if (questionCount <= 0) throw new IllegalArgumentException("questionCount must be > 0");
            return new QuizConfig(this);   // validate once, then create immutable object
        }
    }
}

QuizConfig cfg = QuizConfig.builder("Quadratic Equations", 10)
                           .difficulty(Difficulty.HARD)
                           .bilingual(true)
                           .build();
```

**In the JDK / ecosystem:** `StringBuilder`, `Stream.Builder`, `Calendar.Builder`, `UriComponentsBuilder`, `SpringApplicationBuilder`, Lombok's `@Builder`.

> **Builder vs Factory:** Factory answers *"which type do I create?"* in one call. Builder answers *"how do I assemble this one type?"* over several calls.

---

### Q: Explain the Prototype pattern.

**Creates new objects by cloning an existing instance** rather than constructing from scratch — useful when construction is expensive (e.g. an object built from a DB/network call).

```java
public class QuizTemplate implements Cloneable {
    private String name;
    private List<Question> questions;

    @Override
    public QuizTemplate clone() {
        QuizTemplate copy = new QuizTemplate();
        copy.name = this.name;
        copy.questions = new ArrayList<>(this.questions);  // deep-ish copy of the list
        return copy;
    }
}
```

> ⚠️ Know **shallow vs deep copy**: Java's default `Object.clone()` is shallow — nested mutable objects are shared, so mutating the copy corrupts the original. In real code, prefer a **copy constructor** or a `record` + `with...` style over `Cloneable`, which is widely considered a broken API.

Spring's `@Scope("prototype")` is *scope*, not this pattern — it means "a new bean per injection/lookup".

---

# Part 4 — Structural patterns

### Q: Explain the Adapter pattern.

**Converts one interface into another that clients expect** — lets incompatible types work together. Also called Wrapper.

```java
// Third-party math solver with an interface we don't control
class LegacySolver {
    String solveExpression(String latex) { ... }
}

// What our domain wants
public interface MathSolver { Solution solve(MathProblem problem); }

// Adapter
@Component
public class LegacySolverAdapter implements MathSolver {
    private final LegacySolver legacy = new LegacySolver();

    @Override
    public Solution solve(MathProblem problem) {
        String raw = legacy.solveExpression(problem.toLatex());   // translate the call
        return SolutionParser.parse(raw);                         // translate the result
    }
}
```

**Real use:** wrapping a vendor SDK so your domain isn't polluted by their types, and so swapping vendors touches one class. In the JDK: `Arrays.asList()`, `InputStreamReader` (bytes → chars).

---

### Q: Explain the Decorator pattern. How is it different from inheritance?

**Adds responsibilities to an object dynamically at runtime by wrapping it**, without changing its class.

```java
public interface Notifier { void send(String userId, String message); }

@Component
class EmailNotifier implements Notifier {
    public void send(String userId, String message) { /* SMTP */ }
}

// Decorators wrap and delegate
class LoggingNotifier implements Notifier {
    private final Notifier delegate;
    LoggingNotifier(Notifier delegate) { this.delegate = delegate; }
    public void send(String userId, String message) {
        log.info("Sending to {}", userId);
        delegate.send(userId, message);
        log.info("Sent");
    }
}

class RetryingNotifier implements Notifier {
    private final Notifier delegate;
    RetryingNotifier(Notifier delegate) { this.delegate = delegate; }
    public void send(String userId, String message) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try { delegate.send(userId, message); return; }
            catch (TransientException e) { if (attempt == 3) throw e; }
        }
    }
}

Notifier notifier = new LoggingNotifier(new RetryingNotifier(new EmailNotifier()));
```

| Decorator | Inheritance |
|---|---|
| **Runtime** composition | **Compile-time**, fixed |
| Combine freely (log + retry + cache) | Class explosion (`LoggingRetryingEmailNotifier`) |
| Wraps an instance | Extends a class |

**In the JDK:** `java.io` is the textbook case — `new BufferedReader(new InputStreamReader(new FileInputStream(f)))`. Also `Collections.unmodifiableList()`.

---

### Q: Explain the Facade pattern.

**Provides one simplified entry point to a complex subsystem.**

```java
@Service
public class QuizFacade {                 // one call for the controller
    private final QuestionSelector selector;
    private final EvaluationService evaluator;
    private final ExplanationService explanations;
    private final ProgressTracker progress;

    public QuizResult submit(String studentId, QuizSubmission submission) {
        var scored = evaluator.evaluate(submission);
        var walkthrough = explanations.forIncorrect(scored.getWrongAnswers());
        progress.record(studentId, scored);
        return new QuizResult(scored, walkthrough);
    }
}
```

The controller doesn't need to know about four collaborators or their ordering. **Facade vs Adapter:** Facade *simplifies* an interface you own; Adapter *converts* an interface you don't.

---

### Q: Explain the Proxy pattern and its types.

**A stand-in object that controls access to a real object**, with the same interface.

| Type | Purpose |
|---|---|
| **Virtual** | Lazy-load an expensive object (Hibernate lazy-loading proxies) |
| **Protection** | Access control / authorization checks |
| **Remote** | Local stand-in for a remote object (RMI, Feign clients) |
| **Caching** | Return a cached result instead of calling the real service |

```java
class CachingSolverProxy implements MathSolver {
    private final MathSolver real;
    private final Map<String, Solution> cache = new ConcurrentHashMap<>();

    public Solution solve(MathProblem p) {
        return cache.computeIfAbsent(p.getKey(), k -> real.solve(p));
    }
}
```

> 🔥 **This is the #1 Spring internals question.** `@Transactional`, `@Cacheable`, `@Async` and Spring Security all work by wrapping your bean in a **proxy** (JDK dynamic proxy if it implements an interface, CGLIB subclass otherwise). Consequence: **a self-invocation bypasses the proxy** — calling `this.someTransactionalMethod()` from inside the same class skips the transaction entirely, because the call never leaves the object to pass through the proxy. Also, `@Transactional` on a `private` or `final` method doesn't work for the same reason.

**Proxy vs Decorator:** structurally identical — the *intent* differs. Decorator **adds** behaviour; Proxy **controls access** to the same behaviour.

---

### Q: Explain the Composite pattern.

**Treats individual objects and compositions of objects uniformly through a common interface** — for tree structures.

```java
interface StudyNode {
    int totalTopics();
}

class Topic implements StudyNode {                       // leaf
    public int totalTopics() { return 1; }
}

class StudyList implements StudyNode {                   // composite
    private final List<StudyNode> children = new ArrayList<>();
    public void add(StudyNode node) { children.add(node); }
    public int totalTopics() {
        return children.stream().mapToInt(StudyNode::totalTopics).sum();  // recursive
    }
}
```

Clients call `totalTopics()` without caring whether it's a leaf or a branch. Real-world: the Subsea **category/sub-category** tree, file systems, Angular component trees, org charts.

---

# Part 5 — Behavioural patterns

### Q: Explain the Strategy pattern.

**Defines a family of interchangeable algorithms and makes them swappable at runtime.** The single most-used pattern in Spring apps.

```java
public interface DiscountStrategy { BigDecimal apply(BigDecimal amount); }

@Component("student")  class StudentDiscount  implements DiscountStrategy { ... }
@Component("annual")   class AnnualDiscount   implements DiscountStrategy { ... }
@Component("none")     class NoDiscount       implements DiscountStrategy {
    public BigDecimal apply(BigDecimal amount) { return amount; }    // Null Object pattern
}

@Service
public class PricingService {
    private final Map<String, DiscountStrategy> strategies;   // Spring injects bean-name → bean

    public PricingService(Map<String, DiscountStrategy> strategies) { this.strategies = strategies; }

    public BigDecimal price(BigDecimal base, String planCode) {
        return strategies.getOrDefault(planCode, strategies.get("none")).apply(base);
    }
}
```

**Strategy vs Factory:** Factory decides **which object to create**; Strategy decides **which algorithm to run**. They're often used together — a factory returns the strategy.

**Strategy vs State:** structurally identical. Strategy is chosen by the **client**; State transitions are driven **by the object itself** as its internal state changes.

---

### Q: Explain the Observer pattern.

**One-to-many dependency: when the subject changes state, all registered observers are notified automatically.**

```java
public interface QuizCompletedListener { void onQuizCompleted(QuizResult result); }

@Service
public class QuizService {
    private final List<QuizCompletedListener> listeners;   // injected by Spring

    public void complete(QuizResult result) {
        save(result);
        listeners.forEach(l -> l.onQuizCompleted(result));  // notify all
    }
}
```

**Idiomatic Spring version — application events:**

```java
@Service
class QuizService {
    private final ApplicationEventPublisher publisher;

    void complete(QuizResult result) {
        save(result);
        publisher.publishEvent(new QuizCompletedEvent(result));   // publisher knows nothing about listeners
    }
}

@Component
class ProgressUpdater {
    @EventListener
    void on(QuizCompletedEvent event) { /* update the dashboard analytics */ }
}

@Component
class BadgeAwarder {
    @Async
    @EventListener
    void on(QuizCompletedEvent event) { /* award badges off the request thread */ }
}
```

Adding a new reaction to quiz completion = **one new listener**, `QuizService` untouched (OCP again).

**Elsewhere:** RxJS `Observable`/`Subject` in Angular (your `Study List` real-time sync), DOM event listeners, `PropertyChangeListener`. Java's `java.util.Observer` was **deprecated in Java 9** — don't cite it as current practice.

---

### Q: Explain the Template Method pattern.

**A base class defines the skeleton of an algorithm and lets subclasses override specific steps** without changing the overall structure.

```java
public abstract class DocumentImportJob {

    public final ImportResult run(File file) {      // final — the skeleton can't be altered
        validate(file);
        var parsed = parse(file);                   // varies by subclass
        var records = transform(parsed);            // varies by subclass
        persist(records);
        return audit(records);
    }

    protected abstract ParsedData parse(File file);
    protected abstract List<Record> transform(ParsedData data);

    protected void validate(File file) { /* shared default */ }   // hook — overridable
    private void persist(List<Record> records) { /* shared */ }
    private ImportResult audit(List<Record> r)  { /* shared */ }
}

class CsvImportJob  extends DocumentImportJob { ... }
class ExcelImportJob extends DocumentImportJob { ... }
```

**Template Method vs Strategy:** Template Method uses **inheritance** and fixes the algorithm's *structure* at compile time; Strategy uses **composition** and swaps the whole algorithm at runtime. Prefer Strategy when you can (favour composition over inheritance), Template Method when the steps genuinely share a fixed order.

**In Spring:** `JdbcTemplate`, `RestTemplate`, `TransactionTemplate` are literally named after it — they handle the boilerplate (open connection, handle exceptions, close) and call your callback for the varying part.

---

### Q: Explain the Chain of Responsibility pattern.

**Passes a request along a chain of handlers until one handles it.** The sender doesn't know which handler will respond.

```java
public interface ValidationHandler {
    void setNext(ValidationHandler next);
    void handle(VisaApplication app);
}

public abstract class AbstractValidator implements ValidationHandler {
    private ValidationHandler next;
    public void setNext(ValidationHandler next) { this.next = next; }
    protected void passOn(VisaApplication app) { if (next != null) next.handle(app); }
}

class DocumentCompletenessValidator extends AbstractValidator {
    public void handle(VisaApplication app) {
        if (app.getDocuments().isEmpty()) throw new ValidationException("Documents missing");
        passOn(app);
    }
}
class EligibilityValidator extends AbstractValidator { ... }
class AttorneySignatureValidator extends AbstractValidator { ... }
```

**Real-world:** Servlet `Filter` chains, Spring Security's filter chain, Angular HTTP interceptors, logging levels, approval workflows.

---

### Q: Explain the Command pattern.

**Encapsulates a request as an object**, so you can queue it, log it, pass it around, and support undo.

```java
public interface Command {
    void execute();
    void undo();
}

class AddTopicCommand implements Command {
    private final StudyList list; private final Topic topic;
    public void execute() { list.add(topic); }
    public void undo()    { list.remove(topic); }
}

class CommandHistory {
    private final Deque<Command> history = new ArrayDeque<>();
    void run(Command c)  { c.execute(); history.push(c); }
    void undoLast()      { if (!history.isEmpty()) history.pop().undo(); }
}
```

**In the JDK:** `Runnable` is a Command — that's why an `ExecutorService` can queue and schedule work. Also Angular/NgRx **actions** are essentially commands.

---

### Q: Explain the State pattern.

**Lets an object alter its behaviour when its internal state changes — it appears to change class.** Replaces sprawling `if/switch` on a status field.

```java
interface ApplicationState {
    ApplicationState submit(VisaApplication app);
    ApplicationState approve(VisaApplication app);
}

class DraftState implements ApplicationState {
    public ApplicationState submit(VisaApplication app)  { return new SubmittedState(); }
    public ApplicationState approve(VisaApplication app) { throw new IllegalStateException("Cannot approve a draft"); }
}

class SubmittedState implements ApplicationState {
    public ApplicationState submit(VisaApplication app)  { throw new IllegalStateException("Already submitted"); }
    public ApplicationState approve(VisaApplication app) { return new ApprovedState(); }
}
```

Illegal transitions become impossible instead of being guarded by scattered `if`s.

---

### Q: Explain the Iterator pattern.

**Provides sequential access to elements of a collection without exposing its internal representation.** You use it every day: `Iterable`/`Iterator`, the enhanced for-loop, and `Stream`. Custom implementations matter when you want to hide the backing structure (array vs linked list vs paged API) from the caller.

---

# Part 6 — Patterns in the JDK, Spring & Angular

### Q: Name design patterns used in the JDK.

| Pattern | JDK example |
|---|---|
| Singleton | `Runtime.getRuntime()`, `Desktop.getDesktop()` |
| Factory Method | `Calendar.getInstance()`, `NumberFormat.getInstance()`, `Optional.of()`, `List.of()` |
| Builder | `StringBuilder`, `Stream.Builder`, `Calendar.Builder` |
| Prototype | `Object.clone()` |
| Adapter | `Arrays.asList()`, `InputStreamReader` |
| Decorator | `java.io` streams, `Collections.unmodifiableList()` |
| Proxy | `java.lang.reflect.Proxy`, RMI |
| Observer | `PropertyChangeListener`, Swing listeners, Flow API (`java.util.concurrent.Flow`) |
| Strategy | `Comparator` passed to `Collections.sort()` |
| Template Method | `AbstractList`, `AbstractMap`, `InputStream.read()` |
| Command | `Runnable`, `Callable` |
| Iterator | `Iterator`, `Iterable` |
| Flyweight | `Integer.valueOf()` cache (−128..127), String pool |

---

### Q: Which design patterns does Spring use?

**This is asked in almost every Spring interview.**

| Pattern | Where in Spring |
|---|---|
| **Singleton** | Default bean scope |
| **Prototype** | `@Scope("prototype")` |
| **Factory** | `BeanFactory`, `ApplicationContext`, `FactoryBean<T>` |
| **Proxy** | `@Transactional`, `@Cacheable`, `@Async`, AOP, Spring Security, Hibernate lazy loading |
| **Template Method** | `JdbcTemplate`, `RestTemplate`, `TransactionTemplate`, `MongoTemplate` |
| **Observer** | `ApplicationEvent` / `@EventListener` / `ApplicationEventPublisher` |
| **Strategy** | Injecting different implementations of an interface; `Resource` loading strategies |
| **Front Controller** | `DispatcherServlet` |
| **MVC** | The whole Spring MVC layering |
| **Chain of Responsibility** | Servlet filters, Spring Security filter chain, `HandlerInterceptor` |
| **Adapter** | `HandlerAdapter`, `MessageConverter` |
| **Dependency Injection / IoC** | The core container |
| **DAO / Repository** | `@Repository`, Spring Data JPA / Mongo repositories |
| **DTO** | Request/response objects at the API boundary |

---

### Q: What patterns show up in your Angular/Ionic work?

Good to have ready, since your role is full stack:

- **Singleton** — `providedIn: 'root'` services (one instance app-wide)
- **Observer** — RxJS `Observable`, `Subject`, `BehaviorSubject`; your real-time Study List sync
- **Dependency Injection** — Angular's injector, `@Injectable`, injection tokens
- **Decorator** — literally `@Component`, `@Injectable`, `@Input`; also HTTP interceptors wrapping requests
- **Chain of Responsibility** — `HTTP_INTERCEPTORS` chain (auth token → error handling → retry)
- **Facade** — a facade service over an NgRx store so components don't touch actions/selectors directly
- **Strategy** — Angular Formly: the field `type` selects which component renders — exactly the strategy you used in EasyVisa's dynamic forms
- **Command** — NgRx actions dispatched to reducers/effects

> 💡 The Formly point is a strong, genuine answer: *"Formly is essentially a strategy/factory pattern — a JSON config picks the field type, and a registry maps that type to a component. In EasyVisa I registered custom field types for our nested reactive forms, which meant new form fields were config changes rather than template changes."*

---

### Q: What are DTO, DAO/Repository, and why not expose entities directly?

- **DTO (Data Transfer Object)** — a flat object for carrying data across a boundary (API request/response). No business logic.
- **DAO / Repository** — encapsulates data access so the service layer doesn't know about SQL/Mongo queries. (Strictly, Repository is a DDD concept working with aggregates; DAO is table-oriented — but interviewers usually use them interchangeably.)

**Why not return JPA entities from controllers:**
- Leaks the DB schema into the API contract — a column rename becomes a breaking API change
- Risk of exposing sensitive fields (password hash, internal flags)
- Lazy-loading proxies blow up during JSON serialization (`LazyInitializationException`)
- Bidirectional relations cause infinite recursion in JSON
- You can't shape the response per endpoint

Use a mapper (MapStruct, or plain code) and Java `record`s for DTOs.

---

### Q: What is an anti-pattern? Name a few.

A common "solution" that looks reasonable but causes harm:

| Anti-pattern | Problem |
|---|---|
| **God Object / God Class** | One class does everything — SRP violation, untestable |
| **Singleton abuse** | Global mutable state, hidden dependencies, untestable |
| **Anaemic Domain Model** | Entities are just getters/setters; all logic sits in services |
| **Spaghetti / Big Ball of Mud** | No layering; everything depends on everything |
| **Magic numbers/strings** | Unnamed literals scattered through code |
| **Golden Hammer** | Forcing one favourite pattern onto every problem |
| **Premature optimization** | Complexity for unmeasured gains |
| **Circular dependencies** | Bean A needs B needs A — a design smell Spring will now reject by default |

---

# Part 7 — Scenario & "spot the violation" questions

### Q: Which SOLID principle does this violate, and how would you fix it?

```java
@Service
public class ReportService {
    public void generate(String type) {
        if (type.equals("PDF")) {
            new PdfWriter().write();
        } else if (type.equals("EXCEL")) {
            new ExcelWriter().write();
        }
        new SmtpMailer().send();
        System.out.println("Report generated");
    }
}
```

**Answer — three violations:**
1. **OCP** — a new format means editing this method. Fix with a `ReportWriter` interface + strategy/factory.
2. **DIP** — it `new`s concrete `PdfWriter`, `ExcelWriter`, `SmtpMailer`. Fix by injecting abstractions through the constructor.
3. **SRP** — it chooses a format, writes, emails, and logs. Fix by splitting responsibilities and using a logger, not `System.out`.

It's also untestable — you can't generate a report without sending a real email.

---

### Q: "Design a notification system that supports email and SMS today, and push/WhatsApp later, with retry and audit logging."

Walk through it out loud like this:

1. **`Notifier` interface** with `supports(Channel)` + `send(Notification)` → **Strategy**, satisfies OCP
2. **`EmailNotifier`, `SmsNotifier`** as `@Component`s; adding push = one new class
3. **`NotifierFactory`** injecting `List<Notifier>` and resolving by channel → **Factory**
4. **Retry and audit as decorators** wrapping any notifier → **Decorator**, keeps SRP intact
5. **`NotificationService` facade** so callers make one call → **Facade**
6. Trigger it from a domain event via `@EventListener` → **Observer**, decoupling the sender from the sending
7. **Constructor injection everywhere** → DIP + easily unit-tested with mocks

Then mention the trade-off: *"I'd start with the interface + two implementations, and only add the decorators when retry/audit are actually required — otherwise it's speculative."*

---

### Q: "You have a class with an 8-argument constructor. What's wrong?"

Likely an **SRP violation** — 8 collaborators means 8 reasons to change. Options:
- Group related dependencies into a cohesive collaborator
- Extract a facade for a subgroup
- Split the class along its axes of change
- If they're *values* rather than dependencies, use a **Builder** or a parameter object/`record`

---

### Q: "Have you actually used a design pattern in your work?"

Never say "not really". Use one of these (all defensible from your resume):

> **Strategy/Factory — RoboGebra:** *"The quiz module supports several question types with different evaluation rules. Instead of a growing switch, I defined an evaluator interface, made each type a Spring component, and let Spring inject the full list. Adding a new question type became a single new class with no changes to the evaluation service."*

> **Facade — RoboGebra:** *"Quiz submission touched scoring, AI explanation generation, and progress tracking. I put a facade service in front so the controller made one call — it kept the controller thin and let me change the internal ordering without touching the API layer."*

> **Strategy — EasyVisa:** *"The document portal had multiple accordion panels with different upload rules. We drove them from configuration with a handler per document type rather than conditionals in the component."*

> **Observer — RoboGebra (Angular side):** *"Real-time Study List sync used RxJS `BehaviorSubject` in a shared service — the components subscribe and update reactively, which is the Observer pattern; I used `takeUntil` on a destroy subject to avoid leaks."*

> **Adapter — Subsea:** *"We wrapped Kendo UI Grid interactions behind our own component API so the rest of the app didn't depend on the vendor's types."*

**Format for the answer: Problem → Pattern → Result.** Keep it to 30–45 seconds.

---

# Part 8 — Rapid-fire one-liners

| Question | Answer |
|---|---|
| SRP in one line | One class, one reason to change |
| OCP in one line | Add new classes, don't edit tested ones |
| LSP in one line | A subclass must not surprise code written for the parent |
| ISP in one line | No client forced to depend on methods it doesn't use |
| DIP in one line | Depend on interfaces, inject them |
| DI vs IoC | DI is one way of achieving IoC |
| Best injection type | Constructor — immutable, fail-fast, testable |
| Why `volatile` in double-checked locking | Prevents seeing a partially constructed object due to instruction reordering |
| Safest singleton | `enum` (reflection- and serialization-proof) |
| Best lazy singleton without locks | Bill Pugh static holder idiom |
| Spring singleton vs GoF singleton | Per ApplicationContext vs per JVM; container-managed vs private constructor |
| Are singleton beans thread-safe? | No — keep them stateless |
| Factory vs Builder | Factory picks *which* object; Builder assembles *one* object step by step |
| Factory Method vs Abstract Factory | One product vs a family of related products |
| Strategy vs State | Client chooses the strategy; the object drives its own state transitions |
| Strategy vs Template Method | Composition + runtime swap vs inheritance + fixed skeleton |
| Decorator vs Proxy | Decorator adds behaviour; Proxy controls access |
| Adapter vs Facade | Adapter converts an interface; Facade simplifies a subsystem |
| Composite is for | Tree structures treated uniformly |
| Chain of Responsibility in Spring | Security filter chain, servlet filters, interceptors |
| Why `@Transactional` fails on self-invocation | The call never passes through the proxy |
| Pattern behind `JdbcTemplate` | Template Method |
| Pattern behind `DispatcherServlet` | Front Controller |
| Pattern behind `@Cacheable` | Proxy (AOP) |
| Pattern behind RxJS `Observable` | Observer |
| Pattern behind Angular `@Component` | Decorator |
| Favour composition over inheritance because | Inheritance is compile-time and fragile; composition is runtime and flexible |
| God class | Anti-pattern violating SRP |

---

## ⏱️ Two-day drill

**Day 1 — SOLID:** write out each principle with a bad→good code pair from memory. Then do Part 7's "spot the violation". Be able to state DI vs IoC vs DIP without hesitating.

**Day 2 — Patterns:** memorize the Spring patterns table (Part 6) and be fluent in Singleton (all four variants + why `volatile`), Factory, Builder, Strategy, Observer, Decorator, Proxy, Template Method. Then rehearse your three real project stories from Part 7 out loud, 45 seconds each.

> **The single highest-yield thing here:** knowing that `@Transactional` / `@Cacheable` / `@Async` are **proxies**, and that self-invocation bypasses them. It's asked constantly, and most 5-year candidates get it wrong.
