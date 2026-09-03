# Java — SOLID Principles & Design Patterns (Easy Version)

Interview Q&A for a **Full Stack Developer (Java + Spring Boot, 5 years)**. This is the round where interviewers separate "writes code" from "designs code". Examples are tied to your real projects — **RoboGebra** (AI math explanation engine, quiz module), **EasyVisa** (document portal), **Subsea** (Schedule-Manager).

> **How this gets asked:** rarely as "define SRP". Usually as *"Which SOLID principle does this code violate?"*, *"Which design pattern have you actually used?"*, or *"How would you design X so adding a new type doesn't break existing code?"*
> Prepare **one real story per principle/pattern** — that is what scores.

Every topic below follows the same shape:
**Easiest way to remember → bad code → good code → real-world example → diagram → Easy memory box.**

---

> ## 🔵 About the "In your RoboGebra project" blocks
>
> Throughout this file, after an interview answer you'll see a **🔵 In your RoboGebra project** block containing **real code from your own repositories** (`/Users/safi/workspace/robogebra-workspace`).
>
> ```
> Use them like this ⭐
>    1. Give the DEFINITION           (the interview answer above)
>    2. Then "in our codebase…"       ← the 🔵 block ⭐
>    3. Then ONE trade-off or trap
> ```
>
> **Three sentences.** That is the difference between someone who read the docs and someone who has shipped it. Full index: **[39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)**.

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
- [Quick Revision Sheet](#quick-revision-sheet)

---

# Part 1 — SOLID Principles

### Q: What is SOLID and why does it matter?

The easiest way to remember — the **word SOLID itself**:

```
S → Single Responsibility  → ONE job per class
O → Open/Closed            → ADD new code, don't EDIT old code
L → Liskov Substitution    → the child must not SURPRISE you
I → Interface Segregation  → many SMALL interfaces, not one fat one
D → Dependency Inversion   → depend on INTERFACES, not concrete classes
```

| Letter | Principle | One-line meaning |
|---|---|---|
| **S** | Single Responsibility | a class should have only one reason to change |
| **O** | Open/Closed | open for extension, closed for modification |
| **L** | Liskov Substitution | a subtype must be usable anywhere its parent is, with no surprises |
| **I** | Interface Segregation | many small, focused interfaces beat one fat interface |
| **D** | Dependency Inversion | depend on abstractions, not concrete implementations |

#### Why it matters — the sentence to say

> *"Following SOLID means a new requirement usually means **adding a class, not editing five existing ones** — which keeps the regression surface small and unit tests easy, because dependencies can be mocked."*

```
WITHOUT SOLID              WITH SOLID
─────────────              ──────────
New requirement            New requirement
      ↓                          ↓
edit 5 tested classes      ADD 1 new class ✅
      ↓                          ↓
re-test everything         old code untouched → nothing to re-test
      ↓
something else breaks 💥
```

#### Easy memory

```
S O L I D
One job | Add don't edit | No surprises | Small interfaces | Use interfaces

The payoff: a new feature = a NEW CLASS, not an edit to five old ones ⭐
```

---

### Q: Explain the Single Responsibility Principle (SRP).

The easiest way to remember:

```
A class should have only ONE REASON TO CHANGE.

Test: "Can I name two different PEOPLE who would ask me to change this class?"
      If yes → it's doing too much.
```

#### The violation

```java
class InvoiceService {
    double calculateTax(Invoice inv) { /* tax rules */ }
    void saveToDatabase(Invoice inv) { /* JDBC */ }
    void sendEmail(Invoice inv)      { /* SMTP */ }
}
```

Three completely different people can force a change here:

```
The ACCOUNTANT changes the tax rules      → edit this class
The DBA       changes the schema          → edit this class
MARKETING     changes the email template  → edit this class

THREE reasons to change = SRP violated 💥
```

And they collide: a marketing tweak to the email risks breaking the tax calculation, because they live in the same file and the same tests.

#### The fix — separate the axes of change

```java
class TaxCalculator      { double calculate(Invoice inv) { ... } }
class InvoiceRepository  { void save(Invoice inv) { ... } }
class InvoiceNotifier    { void notifyCustomer(Invoice inv) { ... } }

@Service
class InvoiceService {                          // now it only ORCHESTRATES
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

```
BEFORE                          AFTER
┌──────────────────┐            ┌───────────────┐
│ InvoiceService   │            │ TaxCalculator │ ← accountant's changes
│  - tax           │    →       ├───────────────┤
│  - database      │            │ InvoiceRepo   │ ← DBA's changes
│  - email         │            ├───────────────┤
└──────────────────┘            │ InvoiceNotifier│ ← marketing's changes
   3 reasons to change          └───────────────┘
                                 1 reason each ✅
```

Real-world idea: a **restaurant**.

```
Bad  → one person cooks, serves, bills and cleans 😩
       Change the billing system → the cooking stops

Good → chef, waiter, cashier, cleaner ✅
       Change the billing system → only the cashier is affected
```

#### Your project answer (RoboGebra)

> *"Our first cut of the explanation engine had one `ExplanationService` that built the AI prompt, called the model, parsed the step-by-step response, and did the Tamil/English translation. Any prompt tweak risked breaking translation. We split it into `PromptBuilder`, `AiClient`, `ExplanationParser` and `TranslationService` — the service just orchestrates. After that, adding bilingual output only touched one class."*

> ⚠️ **Common trap:** SRP does **not** mean "one method per class". Over-splitting creates anaemic classes and a maze of indirection. The real test is the "two different people" question above.

#### Easy memory

```
SRP = ONE reason to change ⭐

Test: can TWO different people ask me to change this class?
      accountant (tax) + DBA (schema) + marketing (email) = 3 reasons 💥

Restaurant: chef | waiter | cashier — not one person doing all of it
⚠️ Not "one method per class" — don't over-split
```

---

### Q: Explain the Open/Closed Principle (OCP).

The easiest way to remember:

```
OPEN for extension   → you CAN add new behaviour
CLOSED for modification → by ADDING code, never by EDITING tested code

The smell that tells you it's violated: a growing if/else or switch. ⭐
```

#### The violation

```java
class QuestionEvaluator {
    boolean evaluate(Question q, String answer) {
        if (q.getType().equals("MCQ"))          { /* ... */ }
        else if (q.getType().equals("NUMERIC")) { /* ... */ }
        else if (q.getType().equals("MATCH"))   { /* ... */ }   // keeps growing 💥
        return false;
    }
}
```

```
New question type
      ↓
EDIT this method
      ↓
re-test EVERY existing type (you touched their file)
      ↓
risk of breaking working code 💥
```

#### The fix — polymorphism (Strategy + OCP together)

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
    private final List<QuestionEvaluator> evaluators;   // ⭐ Spring injects ALL implementations

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

```
                EvaluationService
                       │ (never changes again ✅)
        ┌──────────────┼──────────────┬──────────────┐
        ↓              ↓              ↓              ↓
  McqEvaluator  NumericEvaluator MatchEvaluator  NEW TYPE
                                                  ← just add a @Component
```

A new question type = **one new `@Component`**. `EvaluationService` is never touched again.

Real-world idea: a **power socket**. You don't rewire the house to plug in a new appliance — the socket is a fixed contract, and any device that fits can be added.

> 💡 **High-scoring detail:** *"Spring injecting a `List<T>` or `Map<String, T>` of all beans implementing an interface is the practical way to do OCP in Spring Boot."* Interviewers love this.

#### Easy memory

```
OCP = ADD new classes, never EDIT tested ones ⭐

Smell: a growing if/else or switch on a "type" field 💥
Fix:   an interface + one @Component per type
       + Spring injects List<Interface> ⭐

Power socket: plug in a new device, don't rewire the house.
```

---

### Q: Explain the Liskov Substitution Principle (LSP).

The easiest way to remember:

```
A CHILD must work ANYWHERE the PARENT works — with no surprises.

If you need an `instanceof` check to use a subclass safely, LSP is broken. ⭐
```

#### The classic violation — Square extends Rectangle

```java
class Rectangle {
    protected int width, height;
    void setWidth(int w)  { this.width = w; }
    void setHeight(int h) { this.height = h; }
    int area() { return width * height; }
}

class Square extends Rectangle {              // "a square IS-A rectangle" — mathematically true
    @Override void setWidth(int w)  { this.width = w; this.height = w; }
    @Override void setHeight(int h) { this.width = h; this.height = h; }
}
```

```java
void resize(Rectangle r) {
    r.setWidth(5);
    r.setHeight(4);
    assert r.area() == 20;   // ✅ Rectangle → 20
                             // 💥 Square    → 16  (setHeight also changed the width!)
}
```

```
Caller wrote code that works for Rectangle
      ↓ passes a Square instead
Silently WRONG answer — no exception, no warning 💥
```

⭐ The lesson: **"is-a" in mathematics is not the same as "is-a" in behaviour.**

#### A more realistic Java one

```java
class ReadOnlyDocumentStore implements DocumentStore {
    public Document get(String id) { ... }

    public void save(Document d) {
        throw new UnsupportedOperationException();   // ❌ LSP violation
    }
}
```

```
The interface PROMISES save() works.
This implementation BREAKS that promise.
      ↓
Every caller must now know the CONCRETE type → the abstraction LIES 💥
```

This is exactly why `Arrays.asList()` — which returns a fixed-size list that throws on `add()` — is a famous LSP smell inside the JDK itself.

#### The rules a subclass must respect

```
1. PRECONDITIONS may not be STRENGTHENED
      parent accepts any int → child must not reject negatives
2. POSTCONDITIONS may not be WEAKENED
      parent guarantees a sorted list → child must still sort
3. INVARIANTS must be preserved
      parent guarantees balance ≥ 0 → child must too
4. No NEW CHECKED EXCEPTIONS the parent didn't declare
```

Real-world idea: **a job replacement.**

```
Your manager hires a replacement for you.
If the replacement refuses half your tasks, or does them differently
and breaks things — the substitution FAILED. That is LSP. ⭐
```

**Fix:** prefer composition, or split the interface (`ReadableStore` / `WritableStore` — which is ISP).

#### Easy memory

```
LSP = the child must work anywhere the parent works, with NO SURPRISES ⭐

Violation smells:
   throw new UnsupportedOperationException()  💥
   an instanceof check before using a subclass 💥

Square extends Rectangle → setHeight also changes the width → wrong area
Arrays.asList().add()    → the JDK's own LSP smell

Fix: composition, or split the interface (→ ISP)
```

---

### Q: Explain the Interface Segregation Principle (ISP).

The easiest way to remember:

```
No class should be FORCED to implement methods it doesn't need.

Many SMALL interfaces > one FAT interface. ⭐
```

#### The violation

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

```
A simple attachment doesn't need signing or OCR.
But the fat interface FORCES it to implement them.
      ↓
It throws UnsupportedOperationException
      ↓
which is ALSO an LSP violation 💥
```

#### The fix — split by capability

```java
interface DocumentStorage { void upload(MultipartFile f); void download(String id); }
interface Signable        { void digitallySign(String id); }
interface OcrCapable      { void ocrExtract(String id); }

class SimpleAttachmentHandler implements DocumentStorage { ... }                    // ✅ 2 methods
class VisaFormHandler implements DocumentStorage, Signable, OcrCapable { ... }      // ✅ all of them
```

```
FAT interface                    SEGREGATED
┌────────────────────┐           ┌───────────────┐
│ upload             │           │DocumentStorage│ ← everyone
│ download           │    →      ├───────────────┤
│ delete             │           │ Signable      │ ← only those who sign
│ digitallySign  ❌  │           ├───────────────┤
│ ocrExtract     ❌  │           │ OcrCapable    │ ← only those who OCR
└────────────────────┘           └───────────────┘
```

Real-world idea: a **restaurant menu**.

```
Bad  → ONE menu forcing every customer to order a starter, main,
       dessert AND drink 😩
Good → separate menus; you order only what you want ✅
```

Or: a printer/scanner/fax **all-in-one interface** forces a simple printer to implement `fax()` and throw.

#### Your project answer (EasyVisa)

> *"In the EasyVisa document portal, different panel types supported different capabilities — some documents needed attorney signature, some were plain uploads. Rather than one interface with `UnsupportedOperationException` holes, we kept a small `DocumentStorage` contract and mixed in capability interfaces. It also made the Angular side simpler, because the API contract per document type was honest."*

> 💡 **ISP and LSP are cousins:** a fat interface *forces* implementations to throw `UnsupportedOperationException`, which is an LSP violation. Segregating the interface removes the cause.

#### Easy memory

```
ISP = don't force a class to implement methods it doesn't use ⭐

Smell: UnsupportedOperationException in an implementation 💥
Fix:   split into small CAPABILITY interfaces
       (Signable, OcrCapable — mix them in as needed)

ISP violated → causes an LSP violation. Fixing ISP fixes both ⭐
```

---

### Q: Explain the Dependency Inversion Principle (DIP).

The easiest way to remember:

```
Depend on INTERFACES, not on concrete classes.

The smell: the word `new` inside a service. ⭐
```

Two formal parts:

```
1. High-level modules must not depend on low-level modules.
   BOTH should depend on abstractions.
2. Abstractions must not depend on details.
   Details must depend on abstractions.
```

#### The violation

```java
@Service
class StudyListService {
    private final MongoStudyListRepository repo = new MongoStudyListRepository(); // ❌ concrete + new
    private final EmailNotifier notifier = new EmailNotifier();                   // ❌
}
```

```
Problems:
   - you cannot unit-test it without a real MongoDB 💥
   - switching to Postgres means EDITING this service
   - switching to SMS means EDITING this service
```

#### The fix

```java
public interface StudyListRepository {
    StudyList save(StudyList s);
    Optional<StudyList> findById(String id);
}
public interface Notifier { void notify(String userId, String message); }

@Service
public class StudyListService {
    private final StudyListRepository repo;
    private final Notifier notifier;

    public StudyListService(StudyListRepository repo, Notifier notifier) {   // constructor injection ⭐
        this.repo = repo;
        this.notifier = notifier;
    }
}
```

#### Why it is called "INVERSION" ⭐ — the part people can't explain

```
BEFORE (normal dependency)
   StudyListService  ──depends on──▶  MongoRepository
   (high level)                        (low level)

AFTER (inverted)
   StudyListService  ──depends on──▶  StudyListRepository (interface)
                                              ▲
                                              │ implements
                                       MongoRepository
                                       (low level now depends UPWARD)
```

```
The INTERFACE belongs to the DOMAIN (the high-level module).
The database adapter implements it.

So the arrow of dependency now points FROM the detail TOWARD the domain.
That reversal is the "inversion". ⭐
```

Real-world idea: a **wall socket**.

```
Your laptop doesn't depend on "the Chennai power plant".
Both depend on the SOCKET STANDARD.
Change the power source (solar, generator) — the laptop never changes ✅
```

#### Easy memory

```
DIP = depend on INTERFACES, inject them ⭐

Smell: `new ConcreteClass()` inside a service 💥
Fix:   an interface + constructor injection

"Inversion" = the interface belongs to the DOMAIN;
              the database adapter depends on the DOMAIN, not vice versa ⭐

Wall socket: laptop and power plant both depend on the STANDARD.
```

---

#### 🔵 In your RoboGebra project — DIP, for real ⭐

**Files:** `common/service/event/EventTrackingService.java` + two implementations

```java
// The ABSTRACTION belongs to the DOMAIN ⭐
public interface EventTrackingService {
    void trackUserLogin(User user);
    void trackPaymentEvent(SubscriptionInstanceWithItems subscription);
}

// The DETAILS depend on the abstraction — the arrow points UPWARD ⭐
@Service @Profile({"local","dev"})
@ConditionalOnProperty(prefix="posthog", name="enabled", havingValue="false", matchIfMissing=true)
public class LoggingEventTrackingService implements EventTrackingService { ... }

@Service @Profile({"prod"})
@ConditionalOnProperty(prefix="posthog", name="enabled", havingValue="true")
public class PostHogEventTrackingService implements EventTrackingService { ... }
```

```java
// Every caller depends on the INTERFACE — never on PostHog ⭐
@Service
public class UserService {
    private final EventTrackingService eventTracking;      // ⭐ the abstraction
    public UserService(EventTrackingService eventTracking) { ... }   // constructor injection
}
```

```
WITHOUT DIP                          WITH DIP ⭐
UserService ──▶ PostHogClient        UserService ──▶ EventTrackingService (interface)
                                                            ▲
   ❌ can't test without a key                              │ implements
   ❌ dev machines need PostHog                     PostHogEventTrackingService
   ❌ swapping vendors = edit                       (the DETAIL depends on the DOMAIN ⭐)
      every call site
```

> 🗣️ *"Our analytics is the cleanest DIP example in the codebase. Services depend on `EventTrackingService`, never on PostHog. In dev the implementation just logs, in production it posts to PostHog, and the switch is a Spring profile plus a property. The practical payoff is that nobody needs a PostHog key on their laptop, tests inject a stub with no mocking framework, and if we changed analytics vendor tomorrow it's one new class — no call site changes."*

⭐ And note **where the interface lives**: in `common/service/event`, with the domain — **not** in a vendor package. That's the "inversion": the low-level detail depends on the high-level module's contract, not the other way round.

### Q: What's the difference between Dependency Inversion, Dependency Injection, and IoC?

A very common follow-up — get this crisp:

```
DIP = the PRINCIPLE   → "depend on abstractions"
IoC = the PATTERN     → "the framework controls the flow, not your code"
DI  = the TECHNIQUE   → "dependencies are handed to you"
```

| Term | What it is |
|---|---|
| **Dependency Inversion (DIP)** | a **design principle** — depend on abstractions |
| **Inversion of Control (IoC)** | a **broader pattern** — the framework controls flow and object creation ("don't call us, we'll call you") |
| **Dependency Injection (DI)** | a **technique**, one implementation of IoC — dependencies are handed to an object rather than created by it |

```
DIP (principle: use interfaces)
  ↑ makes it work cleanly
DI (technique: constructor injection)
  ↑ is one way of doing
IoC (pattern: the container is in charge)
```

> *"Spring's `ApplicationContext` is an IoC container; `@Autowired` / constructor injection is DI; and the reason we code to interfaces so it works cleanly is DIP."*

#### Easy memory

```
DIP = PRINCIPLE  (use interfaces)
IoC = PATTERN    (container is in charge)
DI  = TECHNIQUE  (constructor injection)

One sentence: "ApplicationContext = IoC, constructor injection = DI,
               coding to interfaces = DIP." ⭐
```

---

### Q: Which injection type should you use and why?

```
CONSTRUCTOR injection. ⭐
```

```
1. Dependencies can be `final` → IMMUTABLE, thread-safe
2. FAILS FAST at startup if a dependency is missing
3. TESTABLE without Spring:
      new StudyListService(mockRepo, mockNotifier);   ✅
4. EXPOSES SRP violations — a 9-argument constructor screams
   "this class does too much" ⭐ (field injection HIDES that)
```

Since Spring 4.3, **`@Autowired` is optional** if the class has a single constructor.

```
Field injection  ❌ can't be final | hides dependencies | needs reflection to test
Setter injection 🟡 only for genuinely optional dependencies
Constructor      ✅ everything above
```

#### Easy memory

```
CONSTRUCTOR injection ⭐
final ✅ | fail-fast ✅ | testable without Spring ✅ | exposes bloat ✅
@Autowired not needed for a single constructor (4.3+)
```

---

### Q: Where does SOLID conflict with reality?

A genuinely senior answer:

> *"SOLID is a set of heuristics, not laws. Applying OCP everywhere leads to speculative abstraction — interfaces with exactly one implementation forever. My rule is the **rule of three**: hard-code the first case, note the duplication on the second, abstract on the third, when I actually know what varies. Premature abstraction is harder to remove than duplication."*

```
1st case → just write it
2nd case → notice the duplication, don't abstract yet
3rd case → NOW abstract — you finally know what actually varies ⭐
```

⭐ Why this scores: it shows you can *apply judgement*, not just recite principles. An interface with one implementation forever is ceremony, not design.

#### Easy memory

```
RULE OF THREE ⭐
1st → write it | 2nd → notice it | 3rd → abstract it

"Premature abstraction is harder to remove than duplication."
```

---

# Part 2 — Design Patterns: fundamentals

### Q: What is a design pattern? What are the categories?

The easiest way to remember:

```
A design pattern = a NAMED, REUSABLE SOLUTION to a recurring design problem.
                   A template — not copy-paste code.
```

The Gang of Four (GoF) catalogued 23, in three categories:

```
CREATIONAL  → how objects are CREATED
STRUCTURAL  → how objects are COMPOSED
BEHAVIOURAL → how objects COMMUNICATE
```

| Category | Concern | Key patterns |
|---|---|---|
| **Creational** | *how objects are created* | Singleton, Factory Method, Abstract Factory, Builder, Prototype |
| **Structural** | *how objects are composed* | Adapter, Decorator, Facade, Proxy, Composite, Bridge, Flyweight |
| **Behavioural** | *how objects communicate* | Strategy, Observer, Template Method, Chain of Responsibility, Command, Iterator, State, Mediator, Visitor |

Real-world idea: patterns are like **recipes**. "Biryani" is a named approach with known steps — but every cook adapts it to their kitchen. You don't copy a photograph of biryani; you follow the method.

#### Easy memory

```
Creational  = CREATION    (Singleton, Factory, Builder, Prototype)
Structural  = COMPOSITION (Adapter, Decorator, Facade, Proxy, Composite)
Behavioural = COMMUNICATION (Strategy, Observer, Template, Chain, Command, State)

C-S-B: Create → Compose → Communicate ⭐
```

---

# Part 3 — Creational patterns

### Q: Explain the Singleton pattern. How do you make it thread-safe?

The easiest way to remember:

```
Singleton = EXACTLY ONE instance for the whole application,
            with a global access point.
```

Used for caches, configuration holders, connection pools and loggers.

Real-world idea: the **Prime Minister of a country**. There is exactly one, and everybody refers to the same one.

#### The broken version (not thread-safe)

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

```
Thread A: instance == null? YES
Thread B: instance == null? YES     ← both passed the check!
Thread A: creates instance #1
Thread B: creates instance #2       → TWO singletons 💥
```

#### 1. Eager initialisation — simplest, thread-safe by the classloader

```java
class Config {
    private static final Config INSTANCE = new Config();
    private Config() {}
    public static Config getInstance() { return INSTANCE; }
}
```

Use when creation is cheap. Class loading is guaranteed thread-safe by the JVM.

#### 2. Double-checked locking with `volatile` — lazy + thread-safe

```java
class Config {
    private static volatile Config instance;      // ⭐ volatile is MANDATORY
    private Config() {}

    public static Config getInstance() {
        if (instance == null) {                   // 1st check — no lock, fast path ⚡
            synchronized (Config.class) {
                if (instance == null) {           // 2nd check — under the lock
                    instance = new Config();
                }
            }
        }
        return instance;
    }
}
```

> **Why `volatile`?** ⭐ `new Config()` is not atomic — it is *allocate memory*, *run the constructor*, *assign the reference*. Without `volatile`, the JIT or CPU may reorder so the reference is assigned **before** the constructor finishes. Another thread then sees a non-null but **partially constructed** object and uses it.

```
Allowed reordering WITHOUT volatile:
   1. allocate memory
   3. assign the reference    ← instance is now NON-NULL...
   2. run the constructor     ← ...but the object isn't ready 💥

Thread B: if (instance == null) → false → uses a half-built object 💥
```

#### 3. Bill Pugh / holder idiom — lazy, thread-safe, no locking ⭐ best classic

```java
class Config {
    private Config() {}
    private static class Holder { static final Config INSTANCE = new Config(); }
    public static Config getInstance() { return Holder.INSTANCE; }
}
```

The inner class isn't loaded until `getInstance()` is first called — so it is lazy — and class loading gives thread safety for free, with **no synchronization cost at all**.

#### 4. Enum singleton — Joshua Bloch's recommendation ⭐ safest

```java
public enum Config {
    INSTANCE;
    public void load() { ... }
}
```

The only version immune to reflection **and** serialization attacks.

#### How to rank them out loud

```
Eager   → cheap object, always needed
DCL     → the classic interview answer (volatile is the point ⭐)
Holder  → lazy + fast + no locks → the best classic approach ⭐
Enum    → the SAFEST of all ⭐
```

#### Easy memory

```
Singleton = exactly ONE instance (the Prime Minister 🇮🇳)

Eager | DCL (volatile MANDATORY ⭐) | Bill Pugh holder ⭐ | enum (safest) ⭐

volatile in DCL prevents seeing a HALF-CONSTRUCTED object
(allocate → assign → construct is a legal reordering 💥)
```

---

### Q: How can a Singleton be broken, and how do you defend it?

```
1. REFLECTION      → constructor.setAccessible(true) → a second instance 💥
2. SERIALIZATION   → each deserialize creates a new object 💥
3. CLONING         → clone() makes a copy 💥
4. MULTIPLE CLASSLOADERS → one instance per classloader
```

| Attack | Defence |
|---|---|
| **Reflection** | throw from the constructor if the instance already exists; or use an `enum` |
| **Serialization** | implement `readResolve()` returning the existing instance; or use an `enum` |
| **Cloning** | override `clone()` to throw `CloneNotSupportedException` |
| **Multiple classloaders** | usually acceptable; otherwise load explicitly |

```
⭐ Only ENUM is immune to all of them, for free.
```

#### Easy memory

```
Reflection | Serialization | Cloning | Classloaders

enum defeats ALL of them without any extra code ⭐
Otherwise: guard the constructor + readResolve() + throw from clone()
```

---

### Q: Is a Spring `@Service` bean a Singleton pattern?

```
NO — it is singleton SCOPE, which is different. ⭐
```

| GoF Singleton | Spring singleton scope |
|---|---|
| one instance **per JVM/classloader** | one instance **per ApplicationContext** ⭐ |
| enforced by a private constructor | managed by the container; the class stays a normal POJO |
| hard to test or mock | trivially mockable — it's just a class |
| global static access | injected, so dependencies are explicit |

```
Two ApplicationContexts in one JVM → TWO instances of the same @Service ⭐
```

And the point that matters in production:

```
⚠️ Singleton beans are NOT automatically thread-safe.

ONE instance serves ALL concurrent requests, so a mutable instance
field on a @Service is a classic production bug.
→ Keep them STATELESS.
```

#### Easy memory

```
Spring singleton = per ApplicationContext (not per JVM) ⭐
Container-managed POJO → easily mockable (unlike GoF Singleton)

⚠️ NOT thread-safe by itself → keep @Service beans STATELESS
```

---

### Q: Explain the Factory Method pattern.

The easiest way to remember:

```
Factory = "you tell me WHAT you want; I decide WHICH class to create."

It removes `new` from the caller.
```

```java
public interface ExplanationGenerator { Explanation generate(MathProblem p); }

class AlgebraExplanationGenerator   implements ExplanationGenerator { ... }
class GeometryExplanationGenerator  implements ExplanationGenerator { ... }
class CalculusExplanationGenerator  implements ExplanationGenerator { ... }

@Component
public class ExplanationGeneratorFactory {
    private final Map<Topic, ExplanationGenerator> registry;

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

```
Caller:  factory.forTopic(ALGEBRA)
              ↓
         the factory looks up the registry
              ↓
    returns AlgebraExplanationGenerator ✅

The caller NEVER writes `new`, and never knows the concrete class.
```

Real-world idea: a **pizza counter**. You say "one margherita"; the kitchen decides which chef and which oven. You never walk in and start making it yourself.

**Benefit:** callers depend only on the interface, so adding a new topic doesn't touch the caller — that is **OCP**.

> **Simple Factory vs Factory Method:** a "simple factory" is one class with a `switch` — not a GoF pattern, but common and perfectly fine. **Factory Method** puts creation behind an overridable method so subclasses decide the type.

#### Easy memory

```
Factory = the caller says WHAT, the factory decides WHICH class ⭐
Removes `new` from the caller → satisfies OCP

Pizza counter: you order, the kitchen decides who cooks it 🍕
Spring version: inject List<Interface> → build a Map<Type, Impl> registry ⭐
```

---

### Q: Factory Method vs Abstract Factory?

The easiest way to remember:

```
Factory Method  → creates ONE product
Abstract Factory→ creates a FAMILY of related products ⭐
```

| | Factory Method | Abstract Factory |
|---|---|---|
| Creates | **one** product | a **family** of related products |
| Mechanism | inheritance (override a method) | composition (a factory object with several create methods) |
| Example | `createExplanation()` | `UiFactory` → `createButton()`, `createCheckbox()` |

```java
interface ReportFactory {                 // Abstract Factory — a FAMILY
    Header  createHeader();
    Body    createBody();
    Footer  createFooter();
}
class PdfReportFactory   implements ReportFactory { ... }
class ExcelReportFactory implements ReportFactory { ... }
```

```
PdfReportFactory   → PDF header  + PDF body  + PDF footer   ✅ consistent
ExcelReportFactory → Excel header + Excel body + Excel footer ✅

You can NEVER accidentally mix a PDF header with an Excel footer ⭐
```

Real-world idea: a **furniture set**. An "Antique" factory gives you an antique chair, table *and* sofa — you can't end up with a modern chair beside an antique table.

#### Easy memory

```
Factory Method   → ONE product       → createButton()
Abstract Factory → a FAMILY          → createButton() + createCheckbox() + createDialog()

Guarantee: you never mix a PDF header with an Excel footer ⭐
Furniture set: all pieces match.
```

---

### Q: Explain the Builder pattern and when to use it.

The easiest way to remember:

```
Builder = construct a complex object STEP BY STEP,
          instead of one constructor with 8 arguments.
```

#### The problem it solves — telescoping constructors

```java
new QuizConfig("Algebra", 10);
new QuizConfig("Algebra", 10, HARD);
new QuizConfig("Algebra", 10, HARD, true);
new QuizConfig("Algebra", 10, HARD, true, Duration.ofMinutes(20));   // 💥 which is which?
```

```
new QuizConfig("Algebra", 10, HARD, true, false, true, 20, false)
                                     ↑     ↑     ↑          ↑
                            what do these booleans even mean? 😩
```

#### The Builder

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
        return new Builder(topic, questionCount);        // required args here ⭐
    }

    public static class Builder {
        private final String topic;
        private final int questionCount;
        private Difficulty difficulty = Difficulty.MEDIUM;    // sensible defaults ⭐
        private boolean bilingual = false;
        private Duration timeLimit = Duration.ofMinutes(10);

        Builder(String topic, int questionCount) {
            this.topic = topic;
            this.questionCount = questionCount;
        }

        public Builder difficulty(Difficulty d) { this.difficulty = d; return this; }  // fluent ⭐
        public Builder bilingual(boolean b)     { this.bilingual = b;  return this; }
        public Builder timeLimit(Duration d)    { this.timeLimit = d;  return this; }

        public QuizConfig build() {
            if (questionCount <= 0) throw new IllegalArgumentException("questionCount must be > 0");
            return new QuizConfig(this);       // validate ONCE, then create an immutable object ⭐
        }
    }
}
```

```java
QuizConfig cfg = QuizConfig.builder("Quadratic Equations", 10)
                           .difficulty(Difficulty.HARD)
                           .bilingual(true)
                           .build();
```

```
Every line NAMES what it sets ✅
Optional fields have defaults ✅
Validation happens ONCE, in build() ✅
The result is IMMUTABLE ✅
```

Real-world idea: **Subway.** You don't order "a sandwich(bread, cheese, veg, sauce, toasted)". You walk the counter saying *bread → cheese → veggies → sauce → done*, and you can skip anything.

**In the JDK / ecosystem:** `StringBuilder`, `Stream.Builder`, `Calendar.Builder`, `UriComponentsBuilder`, `SpringApplicationBuilder`, Lombok's `@Builder`.

> **Builder vs Factory:** Factory answers *"which type do I create?"* in one call. Builder answers *"how do I assemble this one type?"* over several calls.

#### Easy memory

```
Builder = build STEP BY STEP → .difficulty().bilingual().build() ⭐

Use when: many parameters, several optional, and you want immutability
Validate inside build() | each setter returns `this` (fluent)

Subway sandwich 🥪 — add what you want, skip what you don't
Factory = WHICH type (1 call) | Builder = HOW to assemble (many calls)
```

---

### Q: Explain the Prototype pattern.

The easiest way to remember:

```
Prototype = create a new object by COPYING an existing one,
            instead of building it from scratch.
```

Useful when construction is expensive — e.g. an object assembled from a database or a network call.

```java
public class QuizTemplate implements Cloneable {
    private String name;
    private List<Question> questions;

    @Override
    public QuizTemplate clone() {
        QuizTemplate copy = new QuizTemplate();
        copy.name = this.name;
        copy.questions = new ArrayList<>(this.questions);   // copy the list, not just the reference
        return copy;
    }
}
```

Real-world idea: **photocopying a filled form.** Rather than writing every field again, you copy the completed one and change the name.

#### ⚠️ Shallow vs deep copy — the real question here

```
SHALLOW copy (Object.clone() by default)
   copy.list ──┐
               ├──▶ the SAME ArrayList object
   original.list ┘

   → copy.list.add(x) also changes the ORIGINAL 💥

DEEP copy
   copy.list ──▶ a NEW ArrayList with copied contents ✅
```

> In real code, prefer a **copy constructor** or a `record` with `with...` methods over `Cloneable`, which is widely considered a broken API (it doesn't even declare `clone()`).

Note: Spring's `@Scope("prototype")` is a **scope**, not this pattern — it just means "a new bean per injection or lookup".

#### Easy memory

```
Prototype = COPY an existing object (photocopy a filled form 📄)

⚠️ SHALLOW copy shares nested objects → mutating the copy corrupts the original 💥
Prefer a COPY CONSTRUCTOR over Cloneable (which is a broken API)

Spring @Scope("prototype") is a SCOPE, not this pattern.
```

---
# Part 4 — Structural patterns

### Q: Explain the Adapter pattern.

The easiest way to remember:

```
Adapter = a PLUG CONVERTER. 🔌

It makes an incompatible interface fit the one your code expects.
```

```java
// A third-party math solver whose interface we DON'T control
class LegacySolver {
    String solveExpression(String latex) { ... }
}

// What our domain WANTS
public interface MathSolver { Solution solve(MathProblem problem); }

// The adapter
@Component
public class LegacySolverAdapter implements MathSolver {

    private final LegacySolver legacy = new LegacySolver();

    @Override
    public Solution solve(MathProblem problem) {
        String raw = legacy.solveExpression(problem.toLatex());   // translate the CALL
        return SolutionParser.parse(raw);                         // translate the RESULT
    }
}
```

```
Our code                 ADAPTER                  Third-party
─────────                ───────                  ───────────
MathSolver.solve()  →  translates in  →  legacy.solveExpression()
Solution            ←  translates out ←  a raw String
```

Real-world idea: an **Indian-to-European plug adapter**. Neither the laptop nor the wall socket changes — the adapter sits between them.

**Real use:** wrapping a vendor SDK so your domain isn't polluted by their types, and so swapping vendors touches exactly one class.

**In the JDK:** `Arrays.asList()`, `InputStreamReader` (bytes → chars).

#### Easy memory

```
Adapter = a plug converter 🔌 — makes an incompatible interface fit

Wrap the vendor SDK → your domain stays clean → swapping vendors
touches ONE class ⭐

JDK: Arrays.asList(), InputStreamReader
```

---

### Q: Explain the Decorator pattern. How is it different from inheritance?

The easiest way to remember:

```
Decorator = WRAP an object to add behaviour, at RUNTIME,
            without changing its class.
```

```java
public interface Notifier { void send(String userId, String message); }

@Component
class EmailNotifier implements Notifier {
    public void send(String userId, String message) { /* SMTP */ }
}

// Decorators WRAP and DELEGATE
class LoggingNotifier implements Notifier {
    private final Notifier delegate;
    LoggingNotifier(Notifier delegate) { this.delegate = delegate; }

    public void send(String userId, String message) {
        log.info("Sending to {}", userId);
        delegate.send(userId, message);          // pass it on ⭐
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
```

```java
Notifier notifier = new LoggingNotifier(new RetryingNotifier(new EmailNotifier()));
```

```
   LoggingNotifier
        wraps
   RetryingNotifier
        wraps
     EmailNotifier      ← the real work

send() → log → retry loop → actual SMTP → back out through the wrappers ✅
```

Real-world idea: **dressing in layers.** Shirt → sweater → raincoat. Each layer adds a capability, and you choose the combination each morning. You don't buy a single "ShirtSweaterRaincoat" garment.

#### Decorator vs inheritance

| Decorator | Inheritance |
|---|---|
| **runtime** composition ✅ | **compile-time**, fixed |
| combine freely (log + retry + cache) | class explosion: `LoggingRetryingEmailNotifier` 💥 |
| wraps an instance | extends a class |

```
With inheritance, 3 features = 2³ = 8 classes to cover every combination 💥
With decorators, 3 wrappers = every combination for free ✅
```

**In the JDK:** `java.io` is the textbook case —

```java
new BufferedReader(new InputStreamReader(new FileInputStream(f)))
//   ↑ adds buffering  ↑ adds char decoding  ↑ the real file
```

Also `Collections.unmodifiableList()`.

#### Easy memory

```
Decorator = WRAP and DELEGATE, at runtime ⭐

new LoggingNotifier(new RetryingNotifier(new EmailNotifier()))

Layers of clothing 🧥 — mix and match, no class explosion
JDK: java.io streams, Collections.unmodifiableList()
```

---

### Q: Explain the Facade pattern.

The easiest way to remember:

```
Facade = ONE simple front door to a complicated system behind it.
```

```java
@Service
public class QuizFacade {                 // ONE call for the controller

    private final QuestionSelector selector;
    private final EvaluationService evaluator;
    private final ExplanationService explanations;
    private final ProgressTracker progress;

    public QuizResult submit(String studentId, QuizSubmission submission) {
        var scored      = evaluator.evaluate(submission);
        var walkthrough = explanations.forIncorrect(scored.getWrongAnswers());
        progress.record(studentId, scored);
        return new QuizResult(scored, walkthrough);
    }
}
```

```
BEFORE                              AFTER
Controller                          Controller
   ├─▶ EvaluationService               │
   ├─▶ ExplanationService              ▼
   ├─▶ ProgressTracker             QuizFacade.submit() ⭐
   └─▶ QuestionSelector                │
   (must know all 4, and the           ├─▶ EvaluationService
    correct ORDER to call them)        ├─▶ ExplanationService
                                       └─▶ ProgressTracker
```

Real-world idea: a **hotel reception**. You ask reception for a taxi, laundry and a wake-up call. You never phone the taxi company, the laundry room and the operator yourself.

**Facade vs Adapter:**

```
Facade  → SIMPLIFIES an interface you OWN
Adapter → CONVERTS an interface you DON'T own ⭐
```

#### Easy memory

```
Facade = one simple FRONT DOOR to a complex subsystem 🏨 (hotel reception)

Controller makes ONE call; the facade orchestrates the four services
Facade SIMPLIFIES (yours) | Adapter CONVERTS (someone else's) ⭐
```

---

### Q: Explain the Proxy pattern and its types.

The easiest way to remember:

```
Proxy = a STAND-IN with the SAME interface, that CONTROLS ACCESS
        to the real object.
```

| Type | Purpose |
|---|---|
| **Virtual** | lazy-load an expensive object (Hibernate lazy-loading proxies) |
| **Protection** | access control / authorization checks |
| **Remote** | a local stand-in for a remote object (RMI, Feign clients) |
| **Caching** | return a cached result instead of calling the real service |

```java
class CachingSolverProxy implements MathSolver {
    private final MathSolver real;
    private final Map<String, Solution> cache = new ConcurrentHashMap<>();

    public Solution solve(MathProblem p) {
        return cache.computeIfAbsent(p.getKey(), k -> real.solve(p));
    }
}
```

```
Caller ──▶ PROXY ──▶ real object
             │
             ├── is it cached?  → return it, never call the real one ⚡
             ├── are you allowed? → check first
             └── is it loaded?  → load it now (lazy)
```

Real-world idea: a **personal secretary**. You call the CEO; the secretary answers, decides whether to put you through, and sometimes answers your question herself from her notes.

---

#### 🔥 This is the #1 Spring internals question

```
@Transactional, @Cacheable, @Async and Spring Security ALL work by
wrapping your bean in a PROXY. ⭐

JDK dynamic proxy → if your class implements an interface
CGLIB subclass    → otherwise
```

```
Spring injects the PROXY, not your class:

Caller → [ PROXY: begin transaction ] → YourService.method() → [ PROXY: commit ]
```

**The consequence everyone gets wrong:**

```java
@Service
class OrderService {
    public void a() {
        this.b();          // ❌ the call NEVER leaves the object
    }                      //    → it never passes through the proxy
                           //    → @Transactional is completely IGNORED 💥
    @Transactional
    public void b() { }
}
```

```
External call:  caller → PROXY → a()        ✅ proxy applies
Internal call:  a() → this.b()              ❌ proxy is BYPASSED 💥
```

Also, for the same reason, `@Transactional` on a **private** or **final** method does nothing.

**Proxy vs Decorator:** structurally identical — only the *intent* differs.

```
Decorator → ADDS behaviour
Proxy     → CONTROLS ACCESS to the same behaviour ⭐
```

#### Easy memory

```
Proxy = a stand-in that CONTROLS ACCESS (a secretary 💼)
Types: virtual (lazy) | protection | remote | caching

🔥 @Transactional / @Cacheable / @Async / Security = PROXIES ⭐
   JDK dynamic proxy (with an interface) | CGLIB (without)

⚠️ SELF-INVOCATION this.method() bypasses the proxy → the annotation is IGNORED
⚠️ private/final methods too
```

---

### 🔵 In your RoboGebra code — proxies you actually rely on

```
robogebra-portal:
   16 × @Cacheable   → a PROXY intercepts and returns the cached value
  130 × @Transactional → a PROXY begins/commits the transaction
   12 × @Async        → a PROXY hands the call to a thread pool
```

**File:** `domain/exercisesolution/service/ExerciseSolutionService.java`

```java
@Cacheable(value = CacheNames.EXERCISE_SOLUTION)
public ExerciseSolutionDTO getSolution(String exerciseItemId) { ... }

@Cacheable(value = CacheNames.EXERCISE_SOLUTION, key = "'admin-' + #exerciseItemId")
public ExerciseSolutionDTO getAdminSolution(String exerciseItemId) { ... }
```

> 🗣️ *"Solution content is expensive to assemble and almost never changes, so it's cached. Note the custom key on the admin variant — two methods caching into the same region need distinct keys, or the admin view would serve the student's cached payload. And because `@Cacheable` is proxy-based, calling it from inside the same bean bypasses the cache entirely — the same self-invocation trap as `@Transactional`."*

→ [39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)

---

### Q: Explain the Composite pattern.

The easiest way to remember:

```
Composite = treat ONE thing and a GROUP of things the SAME way.
            (For TREE structures.)
```

```java
interface StudyNode {
    int totalTopics();
}

class Topic implements StudyNode {                       // LEAF
    public int totalTopics() { return 1; }
}

class StudyList implements StudyNode {                   // COMPOSITE
    private final List<StudyNode> children = new ArrayList<>();

    public void add(StudyNode node) { children.add(node); }

    public int totalTopics() {
        return children.stream().mapToInt(StudyNode::totalTopics).sum();  // recursive ⭐
    }
}
```

```
              StudyList  (composite)
                  │
        ┌─────────┼─────────┐
        ↓         ↓         ↓
     Topic    StudyList   Topic       ← a group can contain groups
                  │
             ┌────┴────┐
             ↓         ↓
           Topic     Topic

totalTopics() works IDENTICALLY on a leaf and on a branch ✅
```

Real-world idea: **folders on your computer.** A folder can contain files *and* other folders. "Calculate the size" works the same way on both — you never write different code for a file and a folder.

**Real uses:** the Subsea **category / sub-category** tree, file systems, Angular component trees, org charts, menu structures.

#### Easy memory

```
Composite = leaf and group share ONE interface → TREE structures ⭐

Folders 📁 — a folder holds files AND folders; "size" works on both
Recursive: the composite delegates to its children and sums the result
```

---

# Part 5 — Behavioural patterns

### Q: Explain the Strategy pattern.

The easiest way to remember:

```
Strategy = a family of INTERCHANGEABLE ALGORITHMS,
           swappable at RUNTIME.

The single most-used pattern in Spring applications. ⭐
```

```java
public interface DiscountStrategy { BigDecimal apply(BigDecimal amount); }

@Component("student")  class StudentDiscount implements DiscountStrategy { ... }
@Component("annual")   class AnnualDiscount  implements DiscountStrategy { ... }
@Component("none")     class NoDiscount      implements DiscountStrategy {
    public BigDecimal apply(BigDecimal amount) { return amount; }    // ⭐ Null Object pattern
}

@Service
public class PricingService {
    private final Map<String, DiscountStrategy> strategies;   // Spring injects bean-name → bean ⭐

    public PricingService(Map<String, DiscountStrategy> strategies) {
        this.strategies = strategies;
    }

    public BigDecimal price(BigDecimal base, String planCode) {
        return strategies.getOrDefault(planCode, strategies.get("none")).apply(base);
    }
}
```

```
                PricingService
                      │ (never changes ✅)
        ┌─────────────┼─────────────┐
        ↓             ↓             ↓
StudentDiscount  AnnualDiscount  NoDiscount

New discount type → add ONE @Component → nothing else changes ⭐
```

Real-world idea: **Google Maps travel modes.**

```
"Get me from A to B"
   → by car    🚗
   → by bike   🚲
   → walking   🚶
   → by train  🚆

Same GOAL, different ALGORITHM, chosen at the moment you tap it ⭐
```

⭐ Note the `NoDiscount` class — a strategy that does nothing. That is the **Null Object pattern**, and it removes every `if (discount != null)` check from the codebase. Worth mentioning.

**Strategy vs Factory:**

```
Factory  → decides WHICH OBJECT to CREATE
Strategy → decides WHICH ALGORITHM to RUN

They are often used together — a factory returns the strategy.
```

**Strategy vs State:**

```
Structurally IDENTICAL.
Strategy → chosen by the CLIENT ("sort by price")
State    → the OBJECT changes its own state ("draft → submitted → approved") ⭐
```

#### Easy memory

```
Strategy = interchangeable ALGORITHMS, swapped at runtime ⭐

Google Maps: car 🚗 / bike 🚲 / walk 🚶 — same goal, different algorithm
Spring: inject Map<String, Strategy> → look it up by key ⭐
Null Object (NoDiscount) removes every null check

Factory = WHICH OBJECT | Strategy = WHICH ALGORITHM
Strategy = client chooses | State = the object drives itself
```

---

#### 🔵 In your RoboGebra project — Strategy, chosen by environment ⭐

```java
public interface EventTrackingService { void trackUserLogin(User user); }   // the strategy

@Service @Profile({"local", "dev"})
public class LoggingEventTrackingService implements EventTrackingService { ... }   // strategy A

@Service @Profile({"prod"})
public class PostHogEventTrackingService implements EventTrackingService { ... }   // strategy B
```

```
Classic Strategy → the CLIENT picks the algorithm at runtime
Your variant     → SPRING picks it, from the active profile ⭐

Same pattern, and the selection logic lives in configuration
instead of in an if/else ⭐
```

> 🗣️ *"It's Strategy with the selection delegated to the framework — the profile decides which implementation is in the context, so there's no factory and no conditional anywhere in the business code."*

⭐ Compare that with the classic runtime-selected form, which is also in your stack — the **Angular Formly** field registry on EasyVisa, where a JSON `type` picks the component. Same pattern, different selector.

### Q: Explain the Observer pattern.

The easiest way to remember:

```
Observer = one-to-many.
           When the SUBJECT changes, all registered LISTENERS are told
           automatically.
```

```java
public interface QuizCompletedListener { void onQuizCompleted(QuizResult result); }

@Service
public class QuizService {
    private final List<QuizCompletedListener> listeners;   // injected by Spring

    public void complete(QuizResult result) {
        save(result);
        listeners.forEach(l -> l.onQuizCompleted(result));  // notify ALL ⭐
    }
}
```

#### The idiomatic Spring version — application events ⭐

```java
@Service
class QuizService {
    private final ApplicationEventPublisher publisher;

    void complete(QuizResult result) {
        save(result);
        publisher.publishEvent(new QuizCompletedEvent(result));   // ⭐ knows NOTHING about listeners
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

```
       QuizService (the publisher)
              │  publishEvent(QuizCompletedEvent)
              ↓
    ┌─────────┼──────────┬──────────────┐
    ↓         ↓          ↓              ↓
Progress   Badge     EmailSender    NEW LISTENER
Updater    Awarder                  ← just add a @Component ⭐

The publisher never learns their names.
```

Real-world idea: a **YouTube channel.**

```
The creator uploads ONE video 📹
      ↓
EVERY subscriber gets a notification automatically 🔔

The creator doesn't call each subscriber individually,
and doesn't even know who they are.
```

Adding a new reaction to quiz completion = **one new listener**, `QuizService` untouched. That is **OCP** again.

**Elsewhere:** RxJS `Observable` / `Subject` in Angular (your Study List real-time sync), DOM event listeners, `PropertyChangeListener`.

⚠️ Java's `java.util.Observer` was **deprecated in Java 9** — don't cite it as current practice.

#### Easy memory

```
Observer = one-to-many; the subject notifies all listeners ⭐

YouTube: upload once 📹 → every subscriber is notified 🔔
Spring: ApplicationEventPublisher + @EventListener (publisher knows nobody) ⭐
Angular: RxJS Observable / BehaviorSubject

⚠️ java.util.Observer is DEPRECATED (Java 9)
```

---

### Q: Explain the Template Method pattern.

The easiest way to remember:

```
Template Method = the PARENT fixes the STEPS and their ORDER;
                  the CHILD fills in specific steps.

The skeleton method is `final` so nobody can reorder it. ⭐
```

```java
public abstract class DocumentImportJob {

    public final ImportResult run(File file) {      // ⭐ final — the skeleton can't be altered
        validate(file);
        var parsed  = parse(file);                  // varies by subclass
        var records = transform(parsed);            // varies by subclass
        persist(records);
        return audit(records);
    }

    protected abstract ParsedData parse(File file);
    protected abstract List<Record> transform(ParsedData data);

    protected void validate(File file) { /* shared default */ }   // a HOOK — overridable
    private void persist(List<Record> records) { /* shared */ }
    private ImportResult audit(List<Record> r)  { /* shared */ }
}

class CsvImportJob   extends DocumentImportJob { ... }
class ExcelImportJob extends DocumentImportJob { ... }
```

```
       run()   ← FINAL: the ORDER is locked
         │
   ┌─────┼─────┬─────────┬───────┐
   ↓     ↓     ↓         ↓       ↓
validate parse transform persist audit
 shared  CHILD  CHILD    shared  shared
```

Real-world idea: a **recipe**. The steps and their order are fixed — chop, cook, garnish. Each chef decides *what* to chop and *how* to garnish, but nobody is allowed to garnish before cooking.

**Template Method vs Strategy:**

```
Template Method → INHERITANCE  → the structure is fixed at compile time
Strategy        → COMPOSITION  → the whole algorithm is swapped at runtime

Prefer Strategy when you can (favour composition over inheritance);
use Template Method when the steps genuinely share a FIXED ORDER. ⭐
```

**In Spring:** `JdbcTemplate`, `RestTemplate`, `TransactionTemplate` are literally named after it — they handle the boilerplate (open the connection, handle exceptions, close it) and call *your* callback for the varying part.

#### Easy memory

```
Template Method = the parent fixes the STEPS + ORDER (final method);
                  the child fills in the varying steps ⭐

Recipe 🍲 — chop → cook → garnish, in that order, always
Spring: JdbcTemplate / RestTemplate / TransactionTemplate ⭐

vs Strategy: inheritance + fixed skeleton | composition + runtime swap
```

---

#### 🔵 In your RoboGebra project — Template Method, in the security layer ⭐⭐

**File:** `security/AbstractServiceAuthenticationFilter.java`

```java
public abstract class AbstractServiceAuthenticationFilter extends OncePerRequestFilter {

    // ===== the SKELETON — written once, identical for every subclass ⭐ =====
    //  doFilterInternal(): is this endpoint annotated? → extract the token
    //                      → compare it → additionalValidation() → build the
    //                        SecurityContext → continue the chain

    // ===== the VARYING steps — each subclass supplies five ⭐ =====
    protected abstract Class<? extends Annotation> getAnnotationClass();
    protected abstract String getHeaderName();
    protected abstract String getExpectedToken();
    protected abstract String getFilterName();
    protected abstract UserRole getUserRole();

    // ===== HOOKS with sensible defaults — override only if you need to ⭐ =====
    protected String extractToken(HttpServletRequest r) { return r.getHeader(getHeaderName()); }
    protected void additionalValidation(HttpServletRequest r) { /* default: none */ }
}
```

```
       doFilterInternal()   ← the ALGORITHM and its ORDER, fixed ⭐
              │
   ┌──────────┼──────────┬─────────────┬──────────────┐
   ↓          ↓          ↓             ↓              ↓
annotation? header?   token?   additionalValidation  build context
 (CHILD)    (CHILD)   (CHILD)     (hook)              (shared)
```

**Two subclasses, ~30 lines each:** `CrmServiceAuthenticationFilter`, `WebhookAuthorizationFilter`.

> 🗣️ *"That's a template method. Both service-auth filters follow the same steps in the same order — check the annotation, read the header, compare the token, authenticate — so the algorithm is written once in the base class and each subclass supplies five small decisions. It also has two optional hooks with defaults, `extractToken` and `additionalValidation`, for the subclass that needs Bearer parsing. You couldn't express this with an interface: the base class needs three injected collaborators and a constructor."*

⭐ And it sits inside another pattern: the whole thing is a **Chain of Responsibility** — Spring Security's filter chain, with `@Order(3)` deciding this filter's position.

### Q: Explain the Chain of Responsibility pattern.

The easiest way to remember:

```
Chain of Responsibility = pass the request along a LINE of handlers
                          until one of them deals with it.

The sender doesn't know WHO will handle it. ⭐
```

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
        passOn(app);                                   // ⭐ hand it on
    }
}
class EligibilityValidator         extends AbstractValidator { ... }
class AttorneySignatureValidator   extends AbstractValidator { ... }
```

```
Request
   ↓
[ DocumentCompleteness ] → passes → [ Eligibility ] → passes → [ Signature ] → ✅
                            │
                        or STOPS here if it fails 💥
```

Real-world idea: a **leave approval chain.**

```
Your request → Team Lead → Manager → HR → Director

Each one either approves and passes it up, or stops it.
You submit ONCE and don't need to know who finally signs it ⭐
```

**Real-world in code:** Servlet `Filter` chains, Spring Security's filter chain, Angular HTTP interceptors, logging levels, approval workflows.

#### Easy memory

```
Chain of Responsibility = pass it along until someone handles it ⭐

Leave approval: Lead → Manager → HR → Director
Spring: the SECURITY FILTER CHAIN, servlet filters, HandlerInterceptor
Angular: HTTP_INTERCEPTORS (auth → error → retry)
```

---

### Q: Explain the Command pattern.

The easiest way to remember:

```
Command = wrap a REQUEST as an OBJECT,
          so you can queue it, log it, pass it around — and UNDO it. ⭐
```

```java
public interface Command {
    void execute();
    void undo();
}

class AddTopicCommand implements Command {
    private final StudyList list;
    private final Topic topic;

    public void execute() { list.add(topic); }
    public void undo()    { list.remove(topic); }        // ⭐ this is the payoff
}

class CommandHistory {
    private final Deque<Command> history = new ArrayDeque<>();

    void run(Command c) { c.execute(); history.push(c); }
    void undoLast()     { if (!history.isEmpty()) history.pop().undo(); }
}
```

```
run(AddTopic)   → history: [AddTopic]
run(RemoveTopic)→ history: [RemoveTopic, AddTopic]
undoLast()      → pops RemoveTopic → calls undo() ✅
```

Real-world idea: a **restaurant order slip.** The waiter writes the order on paper. That slip can be queued, passed to the kitchen, re-read, logged — and cancelled.

Or simply: **Ctrl+Z** in any editor. Every action is an object with an `undo()`.

**In the JDK:** `Runnable` **is** a Command — that is exactly why an `ExecutorService` can queue and schedule work. Also, Angular/NgRx **actions** are essentially commands.

#### Easy memory

```
Command = a request wrapped as an OBJECT → queue it, log it, UNDO it ⭐

Ctrl+Z ↩️ | a restaurant order slip 🧾
JDK: Runnable IS a Command → that's why executors can queue it ⭐
Angular: NgRx actions
```

---

### Q: Explain the State pattern.

The easiest way to remember:

```
State = the object CHANGES ITS BEHAVIOUR when its internal state changes.
        It appears to change class.

It replaces a sprawling if/switch on a `status` field. ⭐
```

```java
interface ApplicationState {
    ApplicationState submit(VisaApplication app);
    ApplicationState approve(VisaApplication app);
}

class DraftState implements ApplicationState {
    public ApplicationState submit(VisaApplication app)  { return new SubmittedState(); }
    public ApplicationState approve(VisaApplication app) {
        throw new IllegalStateException("Cannot approve a draft");     // ⭐ impossible, by design
    }
}

class SubmittedState implements ApplicationState {
    public ApplicationState submit(VisaApplication app)  { throw new IllegalStateException("Already submitted"); }
    public ApplicationState approve(VisaApplication app) { return new ApprovedState(); }
}
```

```
  DRAFT ──submit──▶ SUBMITTED ──approve──▶ APPROVED
    │                   │                     │
 approve ❌          submit ❌            everything ❌
 (illegal)          (illegal)

Illegal transitions become IMPOSSIBLE, instead of being guarded
by scattered `if` statements all over the codebase ⭐
```

Real-world idea: a **traffic light.** Red can only become green; green can only become amber. The light itself decides the next state — you don't hand it a strategy.

#### Easy memory

```
State = behaviour changes with internal state; illegal transitions
        become IMPOSSIBLE ⭐

Replaces: if (status == "DRAFT") ... else if (status == "SUBMITTED") ...
Traffic light 🚦 — the object drives its OWN transitions

vs Strategy: identical structure | Strategy = the CLIENT chooses
                                 | State    = the OBJECT transitions itself
```

---

### Q: Explain the Iterator pattern.

The easiest way to remember:

```
Iterator = walk through a collection WITHOUT knowing how it stores things.
```

```java
for (String s : list) { ... }        // you never see the array or the linked nodes ⭐
```

```
ArrayList  → internally an array
LinkedList → internally nodes with pointers
TreeSet    → internally a red-black tree

The for-each loop looks IDENTICAL for all three ✅
```

You use it every day: `Iterable` / `Iterator`, the enhanced for-loop, and `Stream`.

Custom implementations matter when you want to hide the backing structure — an array, a linked list, or a **paged API** — from the caller. A well-written iterator can even fetch the next page of a REST API transparently as you loop.

Real-world idea: a **TV remote's channel button.** You press "next" without knowing whether the channels are stored in an array, a list or a database.

#### Easy memory

```
Iterator = sequential access WITHOUT exposing the internal structure ⭐

for (String s : list)  — identical for ArrayList, LinkedList and TreeSet
Custom use: hide paging — the iterator fetches the next API page transparently
```

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
| Observer | `PropertyChangeListener`, Swing listeners, the Flow API |
| Strategy | `Comparator` passed to `Collections.sort()` ⭐ |
| Template Method | `AbstractList`, `AbstractMap`, `InputStream.read()` |
| Command | `Runnable`, `Callable` |
| Iterator | `Iterator`, `Iterable` |
| Flyweight | the `Integer.valueOf()` cache (−128..127), the String pool ⭐ |

The three easiest to say confidently:

```
Comparator                    → STRATEGY  ⭐
new BufferedReader(new ...)   → DECORATOR ⭐
Integer cache / String pool   → FLYWEIGHT ⭐
```

---

### Q: Which design patterns does Spring use?

**This is asked in almost every Spring interview — memorise this table.**

| Pattern | Where in Spring |
|---|---|
| **Singleton** | the default bean scope |
| **Prototype** | `@Scope("prototype")` |
| **Factory** | `BeanFactory`, `ApplicationContext`, `FactoryBean<T>` |
| **Proxy** ⭐ | `@Transactional`, `@Cacheable`, `@Async`, AOP, Spring Security, Hibernate lazy loading |
| **Template Method** ⭐ | `JdbcTemplate`, `RestTemplate`, `TransactionTemplate`, `MongoTemplate` |
| **Observer** | `ApplicationEvent` / `@EventListener` / `ApplicationEventPublisher` |
| **Strategy** | injecting different implementations of an interface; `Resource` loading |
| **Front Controller** ⭐ | `DispatcherServlet` |
| **MVC** | the whole Spring MVC layering |
| **Chain of Responsibility** ⭐ | servlet filters, the Spring Security filter chain, `HandlerInterceptor` |
| **Adapter** | `HandlerAdapter`, `MessageConverter` |
| **Dependency Injection / IoC** | the core container |
| **DAO / Repository** | `@Repository`, Spring Data JPA / Mongo repositories |
| **DTO** | request/response objects at the API boundary |

The five to say if you only get one sentence:

```
DispatcherServlet   → Front Controller
JdbcTemplate        → Template Method
@Transactional      → PROXY ⭐
Security filters    → Chain of Responsibility
@EventListener      → Observer
```

---

### Q: What patterns show up in your Angular/Ionic work?

Good to have ready, since your role is full stack:

```
Singleton    → providedIn: 'root' services (one instance app-wide)
Observer     → RxJS Observable / Subject / BehaviorSubject
               → your real-time Study List sync ⭐
DI           → Angular's injector, @Injectable, injection tokens
Decorator    → literally @Component, @Injectable, @Input
               → also HTTP interceptors wrapping requests
Chain of Resp→ the HTTP_INTERCEPTORS chain (auth token → error → retry) ⭐
Facade       → a facade service over an NgRx store, so components
               don't touch actions and selectors directly
Strategy     → Angular Formly: the field `type` selects which component renders ⭐
Command      → NgRx actions dispatched to reducers and effects
```

> 💡 The Formly point is a strong, genuine answer: *"Formly is essentially a strategy/factory pattern — a JSON config picks the field type, and a registry maps that type to a component. In EasyVisa I registered custom field types for our nested reactive forms, which meant new form fields were **config changes rather than template changes**."*

---

### Q: What are DTO, DAO/Repository, and why not expose entities directly?

```
DTO  = a flat object for carrying data across a BOUNDARY (API request/response).
       No business logic.
DAO / Repository = encapsulates DATA ACCESS, so the service layer never
       sees SQL or Mongo queries.
```

(Strictly, a Repository is a DDD concept working with aggregates, and a DAO is table-oriented — but interviewers usually use them interchangeably.)

#### Why not return JPA entities from controllers — five reasons

```
1. It LEAKS the DB schema into the API contract
      → a column rename becomes a BREAKING API change 💥
2. Risk of exposing SENSITIVE fields (password hash, internal flags)
3. LAZY-LOADING proxies blow up during JSON serialisation
      → LazyInitializationException
4. BIDIRECTIONAL relations cause INFINITE RECURSION in JSON
      → Employee → Department → employees → Employee → ... 💥
5. You cannot SHAPE the response per endpoint
```

```
   DATABASE                        API
┌──────────────┐             ┌──────────────┐
│ User entity  │ ──mapper──▶ │ UserDto      │
│ password 🔒  │  ✗ never    │ (only what   │
│ orders(lazy) │  ✗ never    │  the API     │
└──────────────┘             │  needs)      │
                             └──────────────┘
```

Use a mapper (MapStruct, or plain code) and Java `record`s for DTOs.

---

### Q: What is an anti-pattern? Name a few.

```
An anti-pattern = a common "solution" that LOOKS reasonable but causes harm.
```

| Anti-pattern | Problem |
|---|---|
| **God Object / God Class** | one class does everything — SRP violation, untestable |
| **Singleton abuse** | global mutable state, hidden dependencies, untestable |
| **Anaemic Domain Model** | entities are just getters/setters; all logic sits in services |
| **Spaghetti / Big Ball of Mud** | no layering; everything depends on everything |
| **Magic numbers/strings** | unnamed literals scattered through the code |
| **Golden Hammer** | forcing one favourite pattern onto every problem ⭐ |
| **Premature optimization** | complexity for unmeasured gains |
| **Circular dependencies** | bean A needs B needs A — Spring now rejects this by default ⭐ |

⭐ The **Golden Hammer** is worth naming, because it shows judgement: *"knowing patterns is less valuable than knowing when NOT to use one."*

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

**Answer — three violations. Say all three:**

```
1. OCP 💥 — a new format means EDITING this method
      Fix: a ReportWriter interface + strategy/factory

2. DIP 💥 — it `new`s concrete PdfWriter, ExcelWriter, SmtpMailer
      Fix: inject ABSTRACTIONS through the constructor

3. SRP 💥 — it chooses a format, writes, emails AND logs
      Fix: split the responsibilities; use a logger, not System.out
```

```
And it is UNTESTABLE:
   you cannot generate a report without sending a REAL email 💥
```

The refactored shape:

```java
@Service
public class ReportService {
    private final Map<ReportType, ReportWriter> writers;   // OCP + DIP
    private final Mailer mailer;                           // DIP

    public ReportService(List<ReportWriter> writers, Mailer mailer) { ... }

    public void generate(ReportType type) {
        writers.get(type).write();          // add a format = add a @Component ✅
        mailer.send();
    }
}
```

---

### Q: "Design a notification system that supports email and SMS today, and push/WhatsApp later, with retry and audit logging."

Walk through it **out loud, in this order**:

```
1. A `Notifier` interface with supports(Channel) + send(Notification)
      → STRATEGY, satisfies OCP

2. EmailNotifier, SmsNotifier as @Components
      → adding push = ONE new class ✅

3. A NotifierFactory injecting List<Notifier>, resolving by channel
      → FACTORY

4. Retry and audit as DECORATORS wrapping any notifier
      → DECORATOR, keeps SRP intact ⭐

5. A NotificationService FACADE so callers make one call
      → FACADE

6. Trigger it from a domain event via @EventListener
      → OBSERVER, decoupling the sender from the sending ⭐

7. Constructor injection everywhere
      → DIP + easily unit-tested with mocks
```

```
  DomainEvent
       ↓ @EventListener (Observer)
  NotificationService (Facade)
       ↓
  NotifierFactory (Factory)
       ↓
  AuditingNotifier → RetryingNotifier → EmailNotifier   (Decorators + Strategy)
```

Then — and this is the part that scores — **mention the trade-off**:

> *"I'd start with the interface and two implementations, and only add the decorators when retry and audit are actually required — otherwise it's speculative."*

---

### Q: "You have a class with an 8-argument constructor. What's wrong?"

```
Almost certainly an SRP violation —
8 collaborators means 8 REASONS TO CHANGE. ⭐
```

Options:

```
1. GROUP related dependencies into one cohesive collaborator
2. EXTRACT a facade for a subgroup
3. SPLIT the class along its axes of change
4. If they are VALUES rather than dependencies
      → use a BUILDER or a parameter object / record
```

⭐ The bonus point: *"This is actually an argument **for** constructor injection — field injection would have hidden the problem completely."*

---

### Q: "Have you actually used a design pattern in your work?"

**Never say "not really."** Use one of these — all defensible from your resume.

Format: **Problem → Pattern → Result.** 30–45 seconds.

> **Strategy/Factory — RoboGebra:** *"The quiz module supports several question types with different evaluation rules. Instead of a growing switch, I defined an evaluator interface, made each type a Spring component, and let Spring inject the full list. Adding a new question type became a single new class with no changes to the evaluation service."*

> **Facade — RoboGebra:** *"Quiz submission touched scoring, AI explanation generation, and progress tracking. I put a facade service in front so the controller made one call — it kept the controller thin and let me change the internal ordering without touching the API layer."*

> **Strategy — EasyVisa:** *"The document portal had multiple accordion panels with different upload rules. We drove them from configuration with a handler per document type rather than conditionals in the component."*

> **Observer — RoboGebra (Angular side):** *"Real-time Study List sync used an RxJS `BehaviorSubject` in a shared service — the components subscribe and update reactively, which is the Observer pattern; I used `takeUntil` on a destroy subject to avoid leaks."*

> **Adapter — Subsea:** *"We wrapped Kendo UI Grid interactions behind our own component API so the rest of the app didn't depend on the vendor's types."*

---

# Part 8 — Rapid-fire one-liners

| Question | Answer |
|---|---|
| SRP in one line | one class, one reason to change |
| OCP in one line | add new classes, don't edit tested ones |
| LSP in one line | a subclass must not surprise code written for the parent |
| ISP in one line | no client forced to depend on methods it doesn't use |
| DIP in one line | depend on interfaces, inject them |
| DI vs IoC | DI is one way of achieving IoC |
| Best injection type | constructor — immutable, fail-fast, testable |
| Why `volatile` in double-checked locking | prevents seeing a partially constructed object due to reordering ⭐ |
| Safest singleton | `enum` (reflection- and serialization-proof) |
| Best lazy singleton without locks | the Bill Pugh static holder idiom |
| Spring singleton vs GoF singleton | per ApplicationContext vs per JVM; container-managed vs private constructor |
| Are singleton beans thread-safe? | **No** — keep them stateless ⭐ |
| Factory vs Builder | Factory picks *which* object; Builder assembles *one* object step by step |
| Factory Method vs Abstract Factory | one product vs a family of related products |
| Strategy vs State | the client chooses the strategy; the object drives its own state transitions |
| Strategy vs Template Method | composition + runtime swap vs inheritance + fixed skeleton |
| Decorator vs Proxy | Decorator **adds** behaviour; Proxy **controls access** |
| Adapter vs Facade | Adapter **converts** an interface; Facade **simplifies** a subsystem |
| Composite is for | tree structures treated uniformly |
| Chain of Responsibility in Spring | the security filter chain, servlet filters, interceptors |
| Why `@Transactional` fails on self-invocation | the call never passes through the proxy ⭐ |
| Pattern behind `JdbcTemplate` | Template Method |
| Pattern behind `DispatcherServlet` | Front Controller |
| Pattern behind `@Cacheable` | Proxy (AOP) |
| Pattern behind RxJS `Observable` | Observer |
| Pattern behind Angular `@Component` | Decorator |
| Pattern behind `Comparator` | Strategy |
| Pattern behind the Integer cache | Flyweight |
| Favour composition over inheritance because | inheritance is compile-time and fragile; composition is runtime and flexible |
| God class | an anti-pattern violating SRP |
| Rule of three | hard-code, notice, then abstract — on the third case ⭐ |

---

## Quick Revision Sheet

```
SOLID
  S  one reason to change      → accountant + DBA + marketing = 3 reasons 💥
  O  add, don't edit           → smell: a growing if/else on a "type" field
                                 fix: interface + @Component + inject List<T> ⭐
  L  no surprises              → smell: UnsupportedOperationException / instanceof
                                 Square extends Rectangle breaks area()
  I  small interfaces          → split by CAPABILITY (Signable, OcrCapable)
  D  depend on interfaces      → smell: `new` inside a service
                                 the interface belongs to the DOMAIN ⭐

  DIP = principle | IoC = pattern | DI = technique
  Rule of three: hard-code → notice → abstract ⭐

CREATIONAL
  Singleton  eager | DCL (volatile MANDATORY ⭐) | Bill Pugh holder ⭐ | enum (safest)
             Spring singleton = per ApplicationContext, NOT thread-safe ⚠️
  Factory    caller says WHAT, factory decides WHICH class (pizza counter 🍕)
  Abstract Factory  a FAMILY of products (never mix PDF header + Excel footer)
  Builder    step by step, fluent, validate in build() (Subway 🥪)
  Prototype  copy an object ⚠️ shallow vs deep

STRUCTURAL
  Adapter    plug converter 🔌 — convert someone else's interface
  Decorator  WRAP and delegate at runtime (layers of clothing 🧥) — java.io
  Facade     one front door 🏨 (hotel reception)
  Proxy 🔥   controls access — @Transactional/@Cacheable/@Async are PROXIES ⭐
             self-invocation this.method() BYPASSES the proxy 💥
  Composite  leaf + group share one interface → trees 📁

BEHAVIOURAL
  Strategy ⭐ interchangeable algorithms (Google Maps: car/bike/walk)
              Spring: inject Map<String, Strategy>
  Observer    one-to-many (YouTube 🔔) — ApplicationEventPublisher + @EventListener
  Template    parent fixes the ORDER (final method), child fills steps (recipe 🍲)
              JdbcTemplate / RestTemplate
  Chain       pass it along (leave approval) — Security filter chain ⭐
  Command     request as an OBJECT → queue, log, UNDO (Ctrl+Z ↩️) — Runnable
  State       the object drives its own transitions (traffic light 🚦)
  Iterator    walk without exposing the structure

SPRING PATTERNS (memorise)
  DispatcherServlet → Front Controller | JdbcTemplate → Template Method
  @Transactional → PROXY ⭐ | Security filters → Chain | @EventListener → Observer

THE #1 THING
  @Transactional / @Cacheable / @Async are PROXIES,
  and SELF-INVOCATION bypasses them. ⭐
  Most 5-year candidates get this wrong.
```

---

## ⏱️ Two-day drill

**Day 1 — SOLID:** write out each principle with a bad→good code pair from memory. Then do Part 7's "spot the violation". Be able to state DI vs IoC vs DIP without hesitating.

**Day 2 — Patterns:** memorise the Spring patterns table (Part 6) and be fluent in Singleton (all four variants **and** why `volatile`), Factory, Builder, Strategy, Observer, Decorator, Proxy and Template Method. Then rehearse your three real project stories from Part 7 out loud, 45 seconds each.

> **The single highest-yield thing here:** knowing that `@Transactional` / `@Cacheable` / `@Async` are **proxies**, and that self-invocation bypasses them. It's asked constantly, and most 5-year candidates get it wrong.

---

**Related files:** [05 — Core Java](./05-java.md) · [06 — Spring Boot](./06-spring-boot.md) · [33 — Interface vs Abstract Class](./33-interface-vs-abstract-class.md) · [32 — Multithreading (singleton, proxies)](./32-multithreading.md)
