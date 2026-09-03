# 🔴 Interface vs Abstract Class — Easy Version

Asked at **Virtusa Round 2 (Aug 2026)** and in almost every Java OOP round.

It looks like a definition question, but it isn't. The interviewer wants to know two things:
1. Can you **choose** between them for a real design?
2. Do you know what **Java 8 and 9 changed**?

> **The mistake that costs the question:** answering with the old textbook line *"interfaces can't have method bodies."* That has been wrong since 2014. Interfaces have had `default` and `static` methods since Java 8 and `private` methods since Java 9. Say the old line and the interviewer quietly downgrades everything you say afterwards.

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

- [Part 0 — The 60-second answer](#part-0--the-60-second-answer)
- [Part 1 — The one-picture difference](#part-1--the-one-picture-difference)
- [Part 2 — The comparison table](#part-2--the-comparison-table)
- [Part 3 — Interfaces after Java 8 and 9](#part-3--interfaces-after-java-8-and-9)
- [Part 4 — Abstract classes in depth](#part-4--abstract-classes-in-depth)
- [Part 5 — When to choose which (the real question)](#part-5--when-to-choose-which-the-real-question)
- [Part 6 — Diamond problem & multiple inheritance](#part-6--diamond-problem--multiple-inheritance)
- [Part 7 — Functional interfaces](#part-7--functional-interfaces)
- [Part 8 — Real examples from the JDK, Spring and your projects](#part-8--real-examples-from-the-jdk-spring-and-your-projects)
- [Part 9 — Related concepts they drift into](#part-9--related-concepts-they-drift-into)
- [Part 10 — Predict the output](#part-10--predict-the-output)
- [Part 11 — Rapid-fire table](#part-11--rapid-fire-table)
- [Part 12 — Drills](#part-12--drills)
- [Part 13 — The C#/.NET version](#part-13--the-cnet-version)

---

# Part 0 — The 60-second answer

The easiest way to remember the whole topic:

```
Abstract class → "IS-A"   → share CODE and STATE     → extends    (only ONE)
Interface      → "CAN-DO" → share a CONTRACT         → implements (MANY)
```

#### What to actually say

> *"Both are abstraction tools, but they answer different questions. An **abstract class** models an **'is-a'** relationship and exists to **share state and common implementation** across closely related subclasses — it can have constructors, instance fields and any access modifier, and a class can extend only one.*
>
> *An **interface** models a **capability** — 'can-do' — and a class can implement **many**. Since Java 8 an interface can carry `default` and `static` method bodies, and since Java 9 `private` helper methods, so 'no implementation' is no longer the difference. What an interface still cannot have is **instance state** — its fields are implicitly `public static final` constants — and it has **no constructor**.*
>
> *The practical rule I use: **interface for the contract, abstract class for shared code.** If I catch myself wanting a field or a constructor, that's an abstract class. If I need a type that several unrelated classes can adopt, that's an interface. I default to interfaces, because Java gives you only one superclass and spending it is a big commitment."*

---

# Part 1 — The one-picture difference

Use **birds** for the whole topic. It makes both answers obvious.

```
        Bird (abstract class)
        ────────────────────
        String name;      ← shared STATE  (only an abstract class can do this)
        int weight;
        void eat() {...}  ← shared CODE
        abstract void fly();
                 │
          ┌──────┴──────┐
          ↓             ↓
       Sparrow        Eagle
```

Every bird **IS-A** bird, they all eat the same way, and they all have a name and a weight.
Shared state + shared code + a real family = **abstract class.**

Now, swimming:

```
    interface Swimmable
        ↑      ↑      ↑
        │      │      │
      Duck   Fish   Human      ← completely unrelated classes
```

A fish is not a bird, and a human is not a fish. Swimming is an **ability**, not a family.
A capability adopted by unrelated types = **interface.**

And `Duck` needs both:

```java
class Duck extends Bird implements Swimmable, Quackable {
//         └── ONE parent      └── MANY capabilities
}
```

```
extends    → only 1 allowed (your one and only inheritance slot)
implements → as many as you like
```

Real-world idea:

```
Abstract class → your FAMILY: you get the surname and the family property.
                 You can only be born into one family.

Interface      → a CERTIFICATE: swimming, driving, first aid.
                 You can hold as many as you want, and so can strangers.
```

---

# Part 2 — The comparison table

| | **Interface** | **Abstract class** |
|---|---|---|
| Keyword | `interface` / `implements` | `abstract class` / `extends` |
| Multiple inheritance | ✅ implement many | ❌ extend exactly one |
| Instance fields (state) | ❌ — fields are implicitly `public static final` | ✅ any fields, any modifier |
| Constructor | ❌ never | ✅ (runs via `super()` from the subclass) |
| Method bodies | ✅ `default`, `static` (8), `private` (9) | ✅ concrete + abstract methods |
| Access modifiers | methods implicitly `public` | `public`, `protected`, `private`, default |
| `final` methods | ❌ | ✅ (this is how a template method locks the algorithm) |
| Can be a lambda target | ✅ if it has exactly 1 abstract method | ❌ never |
| Relationship it models | **CAN-DO** (capability) | **IS-A** (family) |
| Typical use | contract, plugin point, capability | shared code + shared state |

⭐ The two rows that decide almost every real design question: **instance fields** and **multiple inheritance.**

```
Do I need a FIELD or a CONSTRUCTOR?           → abstract class
Do unrelated classes need this ability?       → interface
Might the implementer already extend something? → interface
```

---

# Part 3 — Interfaces after Java 8 and 9

### Q: What can an interface contain today?

The easiest way to remember — **six things**:

```
1. constants        (implicitly public static final)
2. abstract methods (implicitly public abstract)
3. default methods  (Java 8)  → inherited body, overridable
4. static methods   (Java 8)  → utility, called on the interface
5. private methods  (Java 9)  → shared helper for default methods
6. private static   (Java 9)  → shared helper for static methods
```

All six in one example:

```java
public interface PaymentProcessor {

    // 1. constant — implicitly public static final (no other option)
    int MAX_RETRIES = 3;

    // 2. abstract method — implicitly public abstract
    PaymentResult process(Payment p);

    // 3. DEFAULT method (Java 8) — an inheritable implementation
    default PaymentResult processWithRetry(Payment p) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            PaymentResult r = process(p);
            if (r.isSuccess()) return r;
            log("retry " + i);                    // calls the private helper
        }
        return PaymentResult.failed();
    }

    // 4. STATIC method (Java 8) — belongs to the type, NOT inherited
    static PaymentProcessor noop() {
        return p -> PaymentResult.skipped();
    }

    // 5. PRIVATE method (Java 9) — shared logic, hidden from implementers
    private void log(String msg) {
        System.out.println("[pay] " + msg);
    }

    // 6. private static (Java 9) — helper for static methods
    private static boolean valid(Payment p) {
        return p != null && p.amount() > 0;
    }
}
```

What it still **cannot** have:

```
❌ instance fields (state)        → int retryCount;   ← not allowed
❌ constructors
❌ instance initialiser blocks
❌ final or synchronized default methods
```

---

### Q: Why were default methods added?

The easiest way to remember:

```
To add NEW methods to an old interface WITHOUT breaking every class
that already implements it.
```

Java 8 needed to add `stream()`, `forEach()` and `removeIf()` to `Collection` and `Iterable`.

```
Adding an ABSTRACT method to Collection
        ↓
ArrayList, LinkedList, HashSet, TreeSet, Vector,
+ every custom Collection ever written, worldwide
        ↓
ALL of them stop compiling 💥
```

```java
default Stream<E> stream() {                     // ✅ has a body
    return StreamSupport.stream(spliterator(), false);
}
```

```
Old classes → inherit the body → still compile ✅
New classes → may override it  ✅
```

Real-world idea: your **mobile operator** adds a new free service. Existing customers get it automatically without changing their SIM. Anyone who wants a different version can configure their own.

> ⚠️ **The follow-up:** *"So are default methods just multiple inheritance?"*
>
> *"Multiple inheritance of **behaviour**, yes. Not of **state** — interfaces still cannot hold instance fields, and duplicated state is what made C++-style multiple inheritance genuinely hard. That is the deliberate line Java drew."*

---

### Q: Can a default method be `final` or `synchronized`?

**No — neither.**

```java
interface I {
    final default void m() { }          // ❌ compile error
    synchronized default void n() { }   // ❌ compile error
}
```

```
final  → forbids overriding, but the whole point of a default method is
         that implementers CAN override it
synchronized → locking belongs to the object, and the interface doesn't own one
```

Also: a default method **cannot override `equals()`, `hashCode()` or `toString()`**. Those come from `Object`, and the class-wins rule means the interface version could never be used anyway.

---

### Q: What is a private interface method for?

```
To share code BETWEEN two default methods without exposing it publicly.
```

```java
interface Report {

    default String daily()  { return build("DAILY"); }
    default String weekly() { return build("WEEKLY"); }

    private String build(String type) {          // Java 9 ⭐ hidden helper
        return "Report[" + type + "]";
    }
}
```

Before Java 9 that helper had to be `public default build(...)`, which then appeared on **every implementing class's public API** — leaking an internal detail to the whole world.

```
Java 8: helper must be public default → pollutes every implementer 😩
Java 9: helper can be private        → clean public API ✅
```

---

# Part 4 — Abstract classes in depth

### Q: Can an abstract class have a constructor if it can't be instantiated?

**Yes — and this is a favourite trick question.**

The constructor is not there to create an abstract object. It runs as part of the **subclass's** construction chain, to initialise the shared fields.

```java
abstract class Vehicle {

    protected final String vin;            // shared state — impossible in an interface

    protected Vehicle(String vin) {        // ✅ constructor allowed
        this.vin = vin;
    }

    abstract void start();                 // each subclass decides

    void honk() {                          // shared behaviour
        System.out.println("beep");
    }
}

class Car extends Vehicle {
    Car(String vin) { super(vin); }        // must call it
    void start() { System.out.println("ignition"); }
}
```

```java
new Vehicle("x");     // ❌ compile error: Vehicle is abstract; cannot be instantiated
new Car("x");         // ✅
```

What actually happens:

```
new Car("VIN123")
      ↓
Car()  → super("VIN123")
      ↓
Vehicle()  ← the abstract class's constructor RUNS here ⭐
      ↓ sets vin
back to Car()
```

Real-world idea: a **house foundation**. You cannot live in a foundation on its own, but every house built on it uses it — and it must be laid first.

---

### Q: Can an abstract class have zero abstract methods?

**Yes.**

```java
abstract class Base {
    void m() { System.out.println("works"); }     // no abstract methods at all
}
```

`abstract` on a class means only one thing: **"you cannot instantiate me directly."** It says nothing about whether the methods have bodies.

This is useful when the type only makes sense as a base — Spring's `AbstractApplicationContext` is a real example.

---

### Q: Can an abstract method be `private`, `static`, or `final`?

**None of the three** — each is a self-contradiction:

| Combination | Why it fails |
|---|---|
| `private abstract` | private is not inherited → nothing could ever implement it |
| `static abstract` | static methods are hidden, not overridden — polymorphism doesn't apply |
| `final abstract` | `final` forbids overriding; `abstract` demands it |

The easy way to remember:

```
abstract means "SOMEONE ELSE must write this."
private / static / final all mean "nobody else can." → contradiction 💥
```

---

### Q: What happens if a subclass doesn't implement all abstract methods?

The subclass must itself be declared **`abstract`**, passing the obligation further down.

```java
abstract class A { abstract void m(); abstract void n(); }

class B extends A {          // ❌ compile error
    void m() { }             // n() is missing
}

abstract class B extends A { // ✅ B stays abstract, C must finish the job
    void m() { }
}

class C extends B {
    void n() { }             // ✅ now everything is implemented
}
```

```
Implement ALL abstract methods  → concrete class ✅
Implement SOME                  → the subclass must be abstract too
```

---

### Q: Can an abstract class implement an interface without implementing its methods?

**Yes** — it can leave them abstract for its own subclasses. This is the **skeletal implementation** (or adapter) pattern, and the JDK uses it everywhere.

```java
interface List<E> { /* ~25 methods */ }

abstract class AbstractList<E> implements List<E> {
    // implements ~23 of them using get() and size()
    public abstract E get(int index);
    public abstract int size();
}

class MyList<E> extends AbstractList<E> {
    public E get(int i) { ... }      // only TWO methods to write ✅
    public int size()   { ... }
}
```

```
Interface       → the full contract (25 methods)
Abstract class  → does the boring 23 for you
Concrete class  → writes only the 2 that actually differ ⭐
```

---

# Part 5 — When to choose which (the real question)

### Q: When would you use an abstract class over an interface?

| Choose an **abstract class** when… | Choose an **interface** when… |
|---|---|
| Subclasses share **state** (fields) | Unrelated classes need the **same capability** |
| You want to share **constructor logic** | You need a class to have **multiple** types |
| You need **`protected`** members or a `final` method | You want loose coupling and easy testing |
| The relationship is genuinely **is-a** | The relationship is **can-do** (`Serializable`, `Comparable`, `Runnable`) |
| You're writing a **template method** with fixed steps | You're defining a **plugin point** with several implementations |
| The hierarchy is yours and closely related | Implementers may already extend something else |

The quick decision tree:

```
Do I need an instance FIELD or a CONSTRUCTOR?
        │
   YES ─┴─→ abstract class
        │
    NO ─┴─→ Might the implementer already extend something?
                    │
               YES ─┴─→ interface
                    │
                NO ─┴─→ interface anyway (it's cheaper to change later)
```

---

### The Template Method — where an abstract class truly earns its place ⭐

This is the single strongest example to give.

```java
abstract class ReportGenerator {

    // final: the ALGORITHM is fixed; only the steps vary
    public final Report generate(Query q) {
        var data = fetch(q);              // subclass decides
        var body = format(data);          // subclass decides
        audit(q);                         // shared, identical for everyone
        return new Report(body);
    }

    protected abstract List<Row> fetch(Query q);
    protected abstract String format(List<Row> rows);

    private void audit(Query q) { /* shared implementation + shared state */ }
}
```

```
      generate()   ← FINAL: nobody can change the order of the steps
          │
    ┌─────┼─────┬────────┐
    ↓     ↓     ↓        ↓
 fetch  format  audit  return
 (child)(child)(shared)
```

You **cannot** express this cleanly with an interface:

```
generate() must be final     → interfaces can't have final methods ❌
audit() uses shared private state → interfaces can't have state ❌
```

Real-world idea: a **recipe**. The steps and their order are fixed — chop, cook, garnish. Each chef decides *what* to chop and *how* to garnish, but nobody is allowed to garnish before cooking.

---

### 🔵 In your RoboGebra project — you made BOTH choices, correctly ⭐⭐

> This is the strongest possible answer to *"when would you use which?"* — because you have one of each, and the reason is textbook.

#### You chose an ABSTRACT CLASS — `security/AbstractServiceAuthenticationFilter.java`

```java
public abstract class AbstractServiceAuthenticationFilter extends OncePerRequestFilter {

    protected final RequestMappingHandlerMapping handlerMapping;   // ⭐ SHARED STATE
    protected final ExceptionResponseWriter responseWriter;
    protected final UserDetailsService userDetailsService;

    protected AbstractServiceAuthenticationFilter(...) { ... }      // ⭐ CONSTRUCTOR

    // the ALGORITHM is written ONCE here (doFilterInternal) — a TEMPLATE METHOD ⭐

    protected abstract Class<? extends Annotation> getAnnotationClass();  // subclass decides
    protected abstract String getHeaderName();
    protected abstract String getExpectedToken();
    protected abstract String getFilterName();
    protected abstract UserRole getUserRole();

    protected String extractToken(HttpServletRequest r) {           // ⭐ overridable HOOK
        return r.getHeader(getHeaderName());
    }
    protected void additionalValidation(HttpServletRequest r) { }   // ⭐ default: nothing
}
```

**Subclasses:** `CrmServiceAuthenticationFilter`, `WebhookAuthorizationFilter` — about thirty lines each.

```
WHY an abstract class here:
   ✅ shared STATE (three collaborators)     → an interface can't hold these ⭐
   ✅ a CONSTRUCTOR to initialise them       → an interface has none ⭐
   ✅ one shared ALGORITHM, five varying steps → template method ⭐
   ✅ `protected` members                     → interfaces are all public ⭐
```

#### You chose an INTERFACE — `common/service/event/EventTrackingService.java`

```java
public interface EventTrackingService {          // ⭐ pure contract, NO state
    void trackUserLogin(User user);
    void trackPaymentEvent(SubscriptionInstanceWithItems subscription);
    void trackSolutionEvent(String userId, ExerciseSolutionDTO dto);
}

@Service @Profile({"local","dev"})
public class LoggingEventTrackingService implements EventTrackingService { ... }

@Service @Profile({"prod"})
public class PostHogEventTrackingService implements EventTrackingService { ... }
```

```
WHY an interface here:
   ✅ the two implementations share NO code at all ⭐
      (one writes a log line, the other posts JSON to PostHog)
   ✅ nothing to inherit — only a contract to honour
   ✅ swappable by environment, and trivial to stub in a test ⭐
```

### 🗣️ The answer to give — 45 seconds ⭐

> *"I've used both on the same codebase and the deciding factor was exactly the textbook one — shared state and shared code.*
>
> *The service-authentication filters are an **abstract class**. CRM auth and webhook auth follow an identical algorithm: check whether the handler method carries a particular annotation, read a token from a header, compare it against configuration, and build the security context. Only five things differ — which annotation, which header, which expected token, the filter name for logging, and which role to authenticate as. So the algorithm is written once in `doFilterInternal` as a template method, the base class holds the three collaborators all subclasses need, and each subclass is about thirty lines of five overrides. There are also two optional hooks with defaults — token extraction and extra validation — for the subclasses that need them.*
>
> *The analytics tracker is an **interface**, because the logging implementation and the PostHog implementation share nothing at all. There's no code to inherit — just a contract, with the implementation chosen by Spring profile.*
>
> *So the rule I actually apply: if I catch myself wanting a field or a constructor, it's an abstract class. If it's only a contract, it's an interface."*

⭐ That answer works because it names **the concrete reason for each**, not a preference.

---

### Q: Why does modern design favour interfaces?

Four reasons, in order of how convincing they sound in an interview:

```
1. Single inheritance is SCARCE
   → spend it on an abstract class and the class can never extend anything else

2. TESTABILITY
   → mocking an interface is trivial; a base class drags its constructor
     and its state into every test

3. Dependency Inversion (the D in SOLID)
   → depend on abstractions. Spring wires interfaces; @Transactional and AOP
     originally used JDK dynamic proxies, which require one

4. Composition over inheritance
   → deep class hierarchies become rigid; interfaces + delegation don't
```

> ⚠️ **Don't over-swing.** *"Always use interfaces"* is also a wrong answer.
>
> An interface with exactly one implementation, created purely "for the pattern" (`UserService` + `UserServiceImpl`), is ceremony, not design.
>
> Say this instead: *"I add the interface when there's a real second implementation, a real test seam, or a module boundary."* That nuance reads as experience.

---

### Q: Can you use both together?

Yes — and this is the best answer to "which one?", because the JDK itself does it:

```java
interface List<E> { ... }                                   // the contract
abstract class AbstractList<E> implements List<E> { ... }   // skeletal shared code
class ArrayList<E> extends AbstractList<E> { ... }          // concrete
```

```
    List (interface)          ← callers depend on THIS
         ↑
    AbstractList (abstract)   ← optional convenience base
         ↑
    ArrayList (concrete)
```

> 💡 **Say this:** *"Interface for the contract, abstract class for the convenience base. Callers depend on the interface; implementers can extend the base if it helps them — or ignore it entirely."*

---

# Part 6 — Diamond problem & multiple inheritance

### Q: Why doesn't Java allow multiple class inheritance?

The **diamond problem**:

```
             A
          m()  state
          ↙        ↘
        B            C
      m()           m()
          ↘        ↙
             D          ← which m()?  and TWO copies of A's state? 💥
```

```
Ambiguity  → which m() does D inherit?
Worse      → D would hold TWO copies of A's fields
```

C++ solves it with virtual inheritance and a lot of complexity. Java removed the ambiguity by allowing only **one superclass**, then restored the useful half — multiple **types** — through interfaces, which have no state to duplicate.

Real-world idea: you can have only **one** biological family (state you inherit), but any number of **certificates** (capabilities).

---

### Q: Java 8 default methods brought the diamond back. How is it resolved?

**Compile error — you must disambiguate explicitly.**

```java
interface A { default String hello() { return "A"; } }
interface B { default String hello() { return "B"; } }

class C implements A, B {
    // ❌ without this: "class C inherits unrelated defaults for hello() from types A and B"
    @Override
    public String hello() {
        return A.super.hello();      // ✅ the special InterfaceName.super syntax
    }
}
```

```
   A.hello()        B.hello()
        ↘            ↙
             C   ← the compiler REFUSES to guess; you must choose
```

Note the syntax carefully — it is `A.super.hello()`, not `super.hello()`.

---

### Q: What if a class inherits the same method from a superclass and an interface?

**"Class wins."** Always.

```java
class Base { public String hi() { return "class"; } }
interface I { default String hi() { return "interface"; } }

class C extends Base implements I { }

new C().hi();     // "class" ⭐
```

The rule is deliberate: adding a `default` method to an interface must never silently change the behaviour of an existing class.

**The three resolution rules, in order:**

```
1. The most specific CLASS wins over any interface       ⭐
2. The most SPECIFIC INTERFACE wins (a sub-interface beats its parent)
3. Otherwise → compile error → you must override and choose
```

---

# Part 7 — Functional interfaces

### Q: What is a functional interface?

The easiest way to remember:

```
Functional interface = EXACTLY ONE abstract method (SAM = Single Abstract Method)
                     → so it can be the target of a lambda
```

`default`, `static` and `private` methods do **not** count, and neither do public `Object` methods like `equals`.

```java
@FunctionalInterface                 // optional, but the compiler then ENFORCES SAM
interface Validator<T> {

    boolean validate(T t);                                  // the ONE abstract method

    default Validator<T> and(Validator<T> other) {          // doesn't break SAM ✅
        return t -> this.validate(t) && other.validate(t);
    }
}
```

```java
Validator<String> notBlank = s -> s != null && !s.isBlank();
Validator<String> maxLen   = s -> s.length() <= 50;

boolean ok = notBlank.and(maxLen).validate(input);
```

**The built-ins to be able to name:**

```
Function<T,R>   Supplier<T>   Consumer<T>   Predicate<T>
BiFunction      UnaryOperator BinaryOperator
Runnable        Callable      Comparator
```

> ⚠️ **Trap:** *"Can an abstract class be a lambda target?"*
>
> **No.** Lambdas only target functional **interfaces**, even if the abstract class happens to have exactly one abstract method. That is a concrete, technical reason to prefer an interface for a single-method contract.

---

### Q: What is a marker interface?

```
Marker interface = an interface with NO members at all,
                   used purely to TAG a type.
```

```java
public interface Serializable { }      // completely empty
```

```java
class Employee implements Serializable { }   // "I am safe to serialise"
```

The JVM and libraries then check `instanceof Serializable` at runtime.

```
Serializable  → the JVM checks it during object serialisation
Cloneable     → Object.clone() checks it, else CloneNotSupportedException
RandomAccess  → Collections.binarySearch() picks a faster algorithm for lists
```

Real-world idea: a **"Fragile" sticker** on a parcel. It carries no instructions — it just marks the box so the handler behaves differently.

Modern code usually prefers **annotations** for tagging, but `Serializable` remains an interface because the JVM itself checks it.

---

# Part 8 — Real examples from the JDK, Spring and your projects

| Example | Which | Why |
|---|---|---|
| `Comparable`, `Runnable`, `Serializable` | interface | a capability adopted by unrelated types |
| `List` / `AbstractList` / `ArrayList` | both | contract + skeletal base + concrete |
| `HttpServlet` | abstract class | shares request-dispatch logic; you override `doGet`/`doPost` |
| `Thread` | class | …which is exactly why `Runnable` is preferred — extending `Thread` burns your inheritance slot |
| Spring `JpaRepository` | interface | Spring Data **generates** the implementation at runtime |
| Spring `OncePerRequestFilter` | abstract class | shared "run once per request" plumbing around your hook |
| `WebMvcConfigurer` | interface (all defaults) | implement only the callbacks you care about — this replaced the old adapter class |

That last row is a great one to mention: Spring **deleted** `WebMvcConfigurerAdapter` once Java 8 default methods existed, because the adapter class was only ever a workaround for interfaces having no bodies.

---

### Your project answers — have one of each ready

> **RoboGebra (interface):** *"The explanation engine had to support more than one AI provider. I defined an `ExplanationProvider` interface with a single `explain(Problem)` method and had two implementations behind it. The service depends on the interface, Spring injects the configured one, and tests inject a stub — no mocking framework needed. Adding a provider is a new class, not an edit to existing code."*

> **EasyVisa (abstract class):** *"Document validators shared real state and real steps — file-size limits, the audit trail, the error-collection list. I made an `AbstractDocumentValidator` with a `final validate()` template method that ran the shared checks and then called an abstract `validateSpecific()`. The passport, photo and bank-statement validators each implemented only that one method. Template method — the shared part genuinely needed fields, so an interface would have meant copy-pasting it into every validator."*

Notice the shape of both answers: **what the problem was → which one I chose → why the other one wouldn't work.** That last clause is what scores.

---

# Part 9 — Related concepts they drift into

### Q: Abstraction vs encapsulation?

```
Abstraction   → hides COMPLEXITY → design level → interface / abstract class → WHAT
Encapsulation → hides DATA       → code level   → private fields + getters   → HOW
```

> *"An interface abstracts; `private` encapsulates. `List` is abstraction — I don't know or care that it's an array underneath. `ArrayList`'s private `elementData` field is encapsulation."*

That single sentence answers the question completely.

---

### Q: Overloading vs overriding?

```
Overloading = same NAME, different PARAMETERS → same class  → COMPILE time
Overriding  = same SIGNATURE, child rewrites  → parent/child → RUNTIME
```

| | Overloading | Overriding |
|---|---|---|
| Same class? | Yes (or inherited) | Subclass |
| Signature | **different** parameters | **identical** signature |
| Return type | can differ freely | same or **covariant** |
| Binding | **compile-time** (static) | **runtime** (dynamic) |
| Access modifier | anything | cannot be **more restrictive** |
| Exceptions | anything | no new or broader **checked** exceptions |
| `static`/`private`/`final` | overloadable | not overridable (static = **hiding**) |

⭐ Trap: you cannot overload by changing **only** the return type — Java picks the method from the arguments you pass.

---

### Q: What are sealed classes/interfaces (Java 17)?

```
sealed = an abstraction that controls EXACTLY who may implement it. 🎟️
```

```java
public sealed interface Shape permits Circle, Square, Triangle { }
public record Circle(double r) implements Shape { }
```

Two wins:

```
1. Nobody outside the `permits` list can implement it
2. A switch over Shape is EXHAUSTIVE — the compiler proves every case is
   covered, so no `default` is needed ⭐
```

Ideal for closed domain models — a result type, a state machine, a fixed set of payment methods. See **[12 — Java 17](./12-java17-features.md)**.

```
public → open ground | final → locked room | sealed → invitation only 🎟️
```

---

# Part 10 — Predict the output

**Q1**
```java
interface A { default void hi() { System.out.println("A"); } }
interface B { default void hi() { System.out.println("B"); } }
class C implements A, B { }
```
<details><summary>Answer</summary>

**Compile error** — `class C inherits unrelated defaults for hi() from types A and B`.

The compiler will not guess. Fix it by overriding `hi()` in C, optionally delegating with `A.super.hi()`.
</details>

**Q2**
```java
class P { public String who() { return "class"; } }
interface I { default String who() { return "interface"; } }
class C extends P implements I { }
System.out.println(new C().who());
```
<details><summary>Answer</summary>

`class`

**Class wins over interface — always.** The rule exists so that adding a `default` method to an interface can never silently change an existing class's behaviour.
</details>

**Q3**
```java
interface Config { int TIMEOUT = 30; }
class Impl implements Config {
    void change() { TIMEOUT = 60; }
}
```
<details><summary>Answer</summary>

**Compile error** — interface fields are implicitly `public static final`, so `TIMEOUT` cannot be reassigned.

This is the "interfaces can't hold state" rule showing up in practice.
</details>

**Q4** ⭐ (the nastiest one, and a great one to raise proactively)
```java
abstract class Animal {
    Animal() { System.out.println("Animal ctor"); speak(); }
    abstract void speak();
}
class Dog extends Animal {
    private String sound = "woof";
    void speak() { System.out.println(sound); }
}
new Dog();
```
<details><summary>Answer</summary>

```
Animal ctor
null
```

Why? The construction order is:

```
new Dog()
   ↓
Dog() calls super() implicitly
   ↓
Animal() runs → prints "Animal ctor" → calls speak()
   ↓
speak() is OVERRIDDEN, so Dog.speak() runs
   ↓
but Dog's field initialisers have NOT run yet → sound is still null
   ↓
prints null
   ↓
NOW `sound = "woof"` executes
```

**Never call an overridable method from a constructor.** This is a genuinely nasty real-world bug.
</details>

**Q5**
```java
abstract class A { abstract void m(); }
class B extends A { }
```
<details><summary>Answer</summary>

**Compile error** — `B is not abstract and does not override abstract method m()`.

Either implement `m()` in B, or declare `B` abstract as well.
</details>

**Q6**
```java
interface I {
    static void s()  { System.out.println("static"); }
    default void d() { System.out.println("default"); }
}
class C implements I { }
C c = new C();
c.d();
c.s();
```
<details><summary>Answer</summary>

`c.d()` prints `default`.

`c.s()` is a **compile error** — interface `static` methods are **not inherited** by implementing classes. You must call `I.s()`.

⭐ This differs from class static methods, which *are* inherited by subclasses.
</details>

**Q7**
```java
@FunctionalInterface
interface F {
    void a();
    void b();
}
```
<details><summary>Answer</summary>

**Compile error** — `@FunctionalInterface` requires exactly one abstract method, and F has two.

Without the annotation it would compile fine, but it still could not be used as a lambda target.
</details>

---

# Part 11 — Rapid-fire table

| Question | Answer |
|---|---|
| Multiple inheritance? | Interface ✅ many · abstract class ❌ one |
| Instance fields? | Interface ❌ (constants only) · abstract class ✅ |
| Constructor? | Interface ❌ · abstract class ✅ (runs via `super()`) |
| Method bodies in an interface? | ✅ `default`/`static` (8), `private` (9) |
| Can an abstract class have no abstract methods? | ✅ — `abstract` only means "not instantiable" |
| Can an abstract method be private/static/final? | ❌ all three — contradictions |
| Interface field modifiers? | implicitly `public static final` |
| Interface method modifiers? | implicitly `public abstract` (unless default/static/private) |
| Can an interface extend another? | ✅ and **multiple** — `interface A extends B, C` |
| Can an abstract class implement an interface? | ✅ without implementing its methods |
| Diamond with defaults? | compile error → override, use `A.super.m()` |
| Class vs interface method conflict? | **class wins** |
| Are interface static methods inherited? | ❌ — call `Interface.method()` |
| Lambda target? | functional **interface** only, never an abstract class |
| Functional interface? | exactly one abstract method (SAM) |
| Marker interface? | empty interface used as a tag — `Serializable` |
| Can a default method be final/synchronized? | ❌ neither |
| Why prefer interfaces? | one-superclass scarcity, testability, DIP, loose coupling |
| Why still use abstract classes? | shared **state** + constructor + template method + `final` methods |
| Rule of thumb | **Interface = contract / can-do · Abstract class = shared code / is-a** |

---

# Part 12 — Drills

Cover the answers and say them out loud.

**1. You need a `Bird` and a `Plane` to both be flyable. Interface or abstract class?**
<details><summary>Answer</summary>

**Interface `Flyable`.** The classes are completely unrelated and `Bird` almost certainly already extends `Animal`. A "can-do" capability across unrelated hierarchies is the textbook interface case.
</details>

**2. Three payment gateways share retry logic, a timeout field and an audit log, but each calls a different API. Design it.**
<details><summary>Answer</summary>

**Both.**

```java
interface PaymentGateway {                      // the contract callers depend on
    PaymentResult pay(Payment p);
}

abstract class AbstractPaymentGateway implements PaymentGateway {
    private final int timeout;                   // shared STATE
    public final PaymentResult pay(Payment p) {  // shared retry + audit
        ...
        return callProvider(p);                  // the only varying step
    }
    protected abstract PaymentResult callProvider(Payment p);
}
```

Template method + Dependency Inversion. The shared part needs a field, so it has to be an abstract class; the contract stays an interface so callers and tests stay decoupled.
</details>

**3. Can you add a method to a published interface without breaking implementers? How?**
<details><summary>Answer</summary>

**Yes — add it as a `default` method** with a sensible body. That is exactly why Java 8 added them, so `Collection.stream()` could ship.

Adding an **abstract** method breaks every implementer.
</details>

**4. Why can't interfaces have constructors?**
<details><summary>Answer</summary>

Constructors exist to initialise **instance state**, and interfaces have none.

There is also no single super-interface chain to order construction — a class can implement many interfaces, so which constructor would run, and in what order?
</details>

**5. Your teammate creates an interface for every service class, each with exactly one `*Impl`. Push back or accept?**
<details><summary>Answer</summary>

**Push back, politely and with nuance.**

Ceremony interfaces double the file count for no seam. Modern Spring proxies concrete classes with CGLIB, and Mockito mocks concrete classes fine.

Add the interface when there is a second implementation, a module or API boundary, or a genuine test seam.
</details>

**6. Give one thing an abstract class can do that an interface never can, and vice versa.**
<details><summary>Answer</summary>

**Abstract class only:** hold mutable instance state (and run a constructor to initialise it); also `final` methods and `protected` members.

**Interface only:** be implemented by a class that already extends something else — participating in multiple inheritance of type. Also: be a lambda target.
</details>

**7. An interface has a `default` method and the implementing class extends a parent that has the same method. Which runs?**
<details><summary>Answer</summary>

**The parent class's method.** Class always wins over interface.
</details>

---

# Part 13 — The C#/.NET version

Your log shows .NET rounds too (**[29 — ASP.NET MVC](./29-dotnet-mvc-interview-questions.md)**). The same question in C# has different details, and mixing them up is a visible error:

| | Java | C# |
|---|---|---|
| Default methods in interfaces | Java 8 (`default`) | C# 8 (default interface members) |
| Virtual by default? | methods **are** virtual unless `final` | methods are **non-virtual** unless `virtual`; overriding needs `override` |
| Explicit implementation | ❌ | ✅ `void IFoo.Bar() { }` — resolves name clashes cleanly |
| Diamond disambiguation | `A.super.m()` | explicit interface implementation |
| Constants in interfaces | implicit `public static final` | not allowed pre-C# 8 (`static readonly` on a class instead) |
| Abstract keyword | `abstract` | `abstract`, and the subclass must use `override` |

The biggest one to keep straight: **in Java every method is virtual by default; in C# you must opt in with `virtual`.**

---

## Quick Revision Sheet

```
Abstract class → IS-A   → shared STATE + CODE → extends (ONE)   → Bird
Interface      → CAN-DO → CONTRACT            → implements (MANY) → Swimmable

Interface today: constants, abstract, default (8), static (8), private (9)
Interface never: instance fields, constructors, final/synchronized defaults

Abstract class CAN have: constructor ✅, zero abstract methods ✅, final methods ✅
abstract + private/static/final = contradiction ❌

Diamond → compile error → override → A.super.hello()
Class vs interface → CLASS WINS ⭐
Interface static methods are NOT inherited → call I.s()

Functional interface = 1 abstract method (SAM) → lambda target
                       (an abstract class can NEVER be a lambda target)
Marker interface = empty tag → Serializable

Template method → the reason abstract classes still exist:
   public final generate() { fetch(); format(); audit(); }

Choose: need a FIELD or CONSTRUCTOR? → abstract class. Otherwise → interface.
Best answer: "Interface for the contract, abstract class for the shared base."
```

---

**Related files:** [05 — Core Java](./05-java.md) · [17 — SOLID & Design Patterns](./17-solid-design-patterns.md) · [12 — Java 17 (sealed)](./12-java17-features.md) · [23 — Predict the Output](./23-java-output-tricky-questions.md)
