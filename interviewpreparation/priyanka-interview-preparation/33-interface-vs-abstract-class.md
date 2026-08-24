# 🔴 Interface vs Abstract Class — Interview Questions

Asked at **Virtusa Round 2 (Aug 2026)** and a permanent fixture of every Java OOP round. It looks like a definition question and it isn't — the interviewer is checking whether you can **choose** between them for a design, and whether you know what **Java 8/9 changed**.

> **The mistake that costs the question:** answering with the pre-Java-8 textbook table ("interfaces can't have method bodies") — which has been wrong since 2014. Default and static methods (Java 8) and private methods (Java 9) all exist. Get that wrong and the interviewer quietly downgrades everything you say next.

---

## Table of contents

- [Part 0 — The 60-second answer](#part-0--the-60-second-answer)
- [Part 1 — The comparison table](#part-1--the-comparison-table)
- [Part 2 — Interfaces after Java 8 and 9](#part-2--interfaces-after-java-8-and-9)
- [Part 3 — Abstract classes in depth](#part-3--abstract-classes-in-depth)
- [Part 4 — When to choose which (the real question)](#part-4--when-to-choose-which-the-real-question)
- [Part 5 — Diamond problem & multiple inheritance](#part-5--diamond-problem--multiple-inheritance)
- [Part 6 — Functional interfaces](#part-6--functional-interfaces)
- [Part 7 — Real examples from the JDK, Spring and your projects](#part-7--real-examples-from-the-jdk-spring-and-your-projects)
- [Part 8 — Related concepts they drift into](#part-8--related-concepts-they-drift-into)
- [Part 9 — Predict the output](#part-9--predict-the-output)
- [Part 10 — Rapid-fire table](#part-10--rapid-fire-table)
- [Part 11 — Drills](#part-11--drills)
- [Part 12 — The C#/.NET version](#part-12--the-cnet-version)

---

# Part 0 — The 60-second answer

> *"Both are abstraction tools, but they answer different questions. An **abstract class** models an **'is-a'** relationship and exists to **share state and common implementation** across closely related subclasses — it can have constructors, instance fields, any access modifier, and a class can extend only one.*
>
> *An **interface** models a **capability or contract** — 'can-do' — and a class can implement **many**. Since Java 8 an interface can carry `default` and `static` method bodies, and since Java 9 `private` helper methods, so 'no implementation' is no longer the difference. What an interface still cannot have is **instance state** — its fields are implicitly `public static final` constants — and it has **no constructor**.*
>
> *The practical rule I use: **interface for the contract, abstract class for shared code.** If I catch myself wanting a field or a constructor, that's an abstract class. If I need a type that several unrelated classes can adopt, that's an interface. And I default to interfaces, because Java gives you only one superclass and spending it is a big commitment."*

---

# Part 1 — The comparison table

| | **Interface** | **Abstract class** |
|---|---|---|
| Keyword | `interface` / `implements` | `abstract class` / `extends` |
| Multiple inheritance | ✅ implement many | ❌ extend exactly one |
| Instance fields (state) | ❌ — fields are implicitly `public static final` | ✅ any fields, any modifier |
| Constructor | ❌ never | ✅ (called via `super()` by subclasses) |
| Method bodies | ✅ `default`, `static` (8), `private` (9) | ✅ concrete + abstract methods |
| Abstract methods | Implicitly `public abstract` | Must be marked `abstract` |
| Access modifiers on members | `public` by default; `private` allowed only for helpers (9+) | `public`, `protected`, `private`, default |
| Can be instantiated | ❌ | ❌ (but **can** have a constructor) |
| `main` method / static block | `static` methods ✅ (no static init block) | ✅ both |
| `final` methods | ❌ (a default method can always be overridden) | ✅ |
| Purpose | **Contract / capability** — "can-do" | **Shared base** — "is-a" |
| Evolution | Adding a `default` method doesn't break implementers | Adding a concrete method doesn't break subclasses |
| Coupling | Loose | Tighter (subclass is bound to the hierarchy) |
| Typical use | `Comparable`, `Runnable`, Spring `@Service` interfaces, repositories | `AbstractList`, `HttpServlet`, a template-method base |

---

# Part 2 — Interfaces after Java 8 and 9

### Q: What can an interface contain today?

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

    // 4. STATIC method (Java 8) — a utility that belongs to the type, not inherited
    static PaymentProcessor noop() { return p -> PaymentResult.skipped(); }

    // 5. PRIVATE method (Java 9) — shared logic between default methods, not exposed
    private void log(String msg) { System.out.println("[pay] " + msg); }

    // 6. private static (Java 9) — helper for static methods
    private static boolean valid(Payment p) { return p != null && p.amount() > 0; }
}
```

### Q: Why were default methods added?

**Backwards-compatible interface evolution.** Java 8 needed to add `stream()`, `forEach()`, `removeIf()` to `Collection`/`Iterable`. Adding an abstract method to `Collection` would have broken **every implementation ever written**, in the JDK and in the world. `default` let them ship a body so existing classes kept compiling untouched.

> ⚠️ **The follow-up:** *"So are default methods just a way to do multiple inheritance?"* — *"Multiple inheritance of **behaviour**, yes. Not of **state** — interfaces still can't hold instance fields, which is exactly what made C++-style multiple inheritance hard. That's the deliberate line Java drew."*

### Q: Can a default method be `final` or `synchronized`?

**No.** `default` methods cannot be `final` (implementers must be able to override) and cannot be `synchronized`. They also can't override `equals`, `hashCode` or `toString` — those come from `Object` and a class always wins over an interface.

### Q: What is a private interface method for?

Extracting shared code out of two `default` methods **without** exposing it in the public API. Before Java 9, that helper had to be `public default`, polluting every implementer's surface.

---

# Part 3 — Abstract classes in depth

### Q: Can an abstract class have a constructor if it can't be instantiated?

**Yes — and this is a favourite trick question.** The constructor isn't for creating an abstract instance; it runs as part of the **subclass's** construction chain via `super()`, initialising the shared fields.

```java
abstract class Vehicle {
    protected final String vin;                // shared state — impossible in an interface
    protected Vehicle(String vin) { this.vin = vin; }
    abstract void start();
    void honk() { System.out.println("beep"); }   // shared behaviour
}
class Car extends Vehicle {
    Car(String vin) { super(vin); }            // must call it
    void start() { System.out.println("ignition"); }
}
// new Vehicle("x")  ❌ compile error: Vehicle is abstract; cannot be instantiated
```

### Q: Can an abstract class have zero abstract methods?

**Yes.** `abstract` on a class only means *"not instantiable"*. A class with all concrete methods can still be abstract — useful when the type only makes sense as a base (e.g. `AbstractApplicationContext`).

### Q: Can an abstract method be `private`, `static`, or `final`?

**None of the three** — all are contradictions:

| Combination | Why it fails |
|---|---|
| `private abstract` | Private isn't inherited → nothing could ever implement it |
| `static abstract` | Static methods aren't overridden (they're hidden) |
| `final abstract` | `final` forbids overriding, `abstract` demands it |

### Q: What happens if a subclass doesn't implement all abstract methods?

The subclass must itself be declared **`abstract`**, deferring the obligation further down the hierarchy. Otherwise: compile error.

### Q: Can an abstract class implement an interface without implementing its methods?

**Yes** — it can leave them abstract for its subclasses. This is the **adapter/skeletal implementation** pattern: `AbstractList implements List` supplies most methods so a concrete list only needs `get(int)` and `size()`.

---

# Part 4 — When to choose which (the real question)

### Q: When would you use an abstract class over an interface?

| Choose an **abstract class** when… | Choose an **interface** when… |
|---|---|
| Subclasses share **state** (fields) | Unrelated classes need the **same capability** |
| You want to share **constructor logic** | You need a class to have **multiple** types |
| You need **`protected`** members or a `final` method | You want maximum **loose coupling** / testability |
| The relationship is genuinely **is-a** | The relationship is **can-do** (`Serializable`, `Comparable`, `Runnable`) |
| You're writing a **template method** with fixed steps | You're defining a **plugin point / SPI** with several implementations |
| The hierarchy is yours and closely related | Implementers may already extend something else |

**The classic Template Method — the strongest "abstract class earns its place" example:**

```java
abstract class ReportGenerator {
    // final: the ALGORITHM is fixed; only the steps vary
    public final Report generate(Query q) {
        var data = fetch(q);              // subclass decides
        var body = format(data);          // subclass decides
        audit(q);                         // shared, same for everyone
        return new Report(body);
    }
    protected abstract List<Row> fetch(Query q);
    protected abstract String format(List<Row> rows);
    private void audit(Query q) { /* shared implementation + shared state */ }
}
```

You cannot express this cleanly with an interface: `generate()` must be `final` (interfaces can't) and `audit()` uses shared private state (interfaces can't).

### Q: Why does modern design favour interfaces?

1. **Single inheritance is scarce** — burn it and the class can never extend anything else.
2. **Testability** — mock an interface trivially; a base class drags its constructor and state into every test.
3. **Dependency Inversion (the D in SOLID)** — depend on abstractions. Spring wires interfaces; `@Transactional` and AOP used JDK dynamic proxies which require one.
4. **Composition over inheritance** — deep class hierarchies are rigid; interfaces + delegation aren't.

> ⚠️ **Don't over-swing.** *"Always use interfaces"* is also a wrong answer — an interface with exactly one implementation created purely "for the pattern" (`UserServiceImpl`) is ceremony, not design. Say: *"I add the interface when there's a real second implementation, a real test seam, or a module boundary."* That nuance reads as experience.

### Q: Can you use both together?

Yes — the JDK's own pattern, and the best answer to "which one?":

```java
interface List<E> { ... }                        // the contract
abstract class AbstractList<E> implements List<E> { ... }   // skeletal shared implementation
class ArrayList<E> extends AbstractList<E> { ... }          // concrete
```

> 💡 **Say this:** *"Interface for the contract, abstract class for the convenience base. Callers depend on the interface; implementers can extend the base if it helps them — or ignore it entirely."*

---

# Part 5 — Diamond problem & multiple inheritance

### Q: Why doesn't Java allow multiple class inheritance?

The **diamond problem**: if `B` and `C` both extend `A` and override `m()`, and `D` extends both, which `m()` does `D` inherit? Worse, `D` would hold **two copies of A's state**. C++ solves it with virtual inheritance and complexity. Java removed the ambiguity by allowing only one superclass, then restored the useful part — multiple **types** — through interfaces.

### Q: Java 8 default methods brought the diamond back. How is it resolved?

**Compile error — you must disambiguate explicitly.**

```java
interface A { default String hello() { return "A"; } }
interface B { default String hello() { return "B"; } }

class C implements A, B {
    // ❌ without this: "class C inherits unrelated defaults for hello() from types A and B"
    @Override public String hello() {
        return A.super.hello();      // ✅ the special InterfaceName.super syntax
    }
}
```

### Q: What if a class inherits the same method from a superclass and an interface?

**"Class wins"** — the concrete class implementation always beats an interface `default`. That rule was deliberate: adding a `default` to an interface can never silently change an existing class's behaviour.

```java
class Base { public String hi() { return "class"; } }
interface I { default String hi() { return "interface"; } }
class C extends Base implements I { }
new C().hi();     // "class"
```

**The three resolution rules, in order:** (1) the most specific **class** wins over any interface, (2) the most **specific interface** wins (a sub-interface beats its parent), (3) otherwise the compiler refuses and you must override.

---

# Part 6 — Functional interfaces

### Q: What is a functional interface?

An interface with **exactly one abstract method** (SAM), so it can be a lambda or method-reference target. `default`, `static` and `private` methods don't count, and neither do public `Object` methods like `equals`.

```java
@FunctionalInterface                 // optional, but makes the compiler enforce SAM
interface Validator<T> {
    boolean validate(T t);                                  // the single abstract method
    default Validator<T> and(Validator<T> other) {          // doesn't break SAM
        return t -> this.validate(t) && other.validate(t);
    }
}

Validator<String> notBlank = s -> s != null && !s.isBlank();
Validator<String> maxLen   = s -> s.length() <= 50;
boolean ok = notBlank.and(maxLen).validate(input);
```

**The built-ins to name:** `Function<T,R>`, `Supplier<T>`, `Consumer<T>`, `Predicate<T>`, `BiFunction`, `UnaryOperator`, `Runnable`, `Callable`, `Comparator`.

> ⚠️ **Trap:** *"Can an abstract class be a lambda target?"* — **No.** Lambdas only target functional **interfaces**, even if the abstract class has exactly one abstract method. A concrete reason to prefer an interface for a single-method contract.

### Q: What is a marker interface?

An interface with **no members at all**, used purely to tag a type for runtime/tooling checks: `Serializable`, `Cloneable`, `RandomAccess`. Modern code usually prefers **annotations** for this, but `Serializable` is checked by the JVM itself, so it stays an interface.

---

# Part 7 — Real examples from the JDK, Spring and your projects

| Example | Which | Why |
|---|---|---|
| `Comparable`, `Runnable`, `Serializable` | interface | Capability adopted by unrelated types |
| `List` / `AbstractList` / `ArrayList` | both | Contract + skeletal base + concrete |
| `HttpServlet` | abstract class | Shares request-dispatch logic; you override `doGet`/`doPost` |
| `Thread` | class | ...which is why `Runnable` is preferred — extending `Thread` burns your inheritance |
| Spring `JpaRepository` | interface | Spring Data **generates** the implementation at runtime |
| Spring `OncePerRequestFilter` | abstract class | Shared "run once per request" plumbing around your hook |
| `WebMvcConfigurer` | interface (all defaults) | Implement only the callbacks you care about — replaced the old adapter class |

**Your project answers — have one ready:**

> **RoboGebra (interface):** *"The explanation engine had to support more than one AI provider. I defined an `ExplanationProvider` interface with a single `explain(Problem)` method and had two implementations behind it. The service depends on the interface, Spring injects the configured one, and tests inject a stub — no mocking framework needed. Adding a provider is a new class, not an edit to existing code."*

> **EasyVisa (abstract class):** *"Document validators shared real state and steps — file-size limits, the audit trail, the error-collection list. I made an `AbstractDocumentValidator` with a `final validate()` template method that ran the shared checks and then called an abstract `validateSpecific()`. Passport, photo and bank-statement validators each implemented only that one method. Template method — the shared part genuinely needed fields, so an interface would have meant copy-pasting it into every validator."*

---

# Part 8 — Related concepts they drift into

### Q: Abstraction vs encapsulation?

- **Abstraction** — *hiding complexity*, exposing only what matters. Design-level. Achieved with interfaces and abstract classes. *What* it does.
- **Encapsulation** — *hiding data*, bundling state with the methods that guard it. Implementation-level. Achieved with `private` fields + accessors. *How* it's protected.

> *"An interface abstracts; `private` encapsulates. `List` is abstraction — I don't know or care it's an array underneath. `ArrayList`'s private `elementData` is encapsulation."*

### Q: Overloading vs overriding?

| | Overloading | Overriding |
|---|---|---|
| Same class? | Yes (or inherited) | Subclass |
| Signature | **Different** parameters | **Identical** signature |
| Return type | Can differ freely | Same or **covariant** |
| Binding | **Compile-time** (static) | **Runtime** (dynamic) |
| Access modifier | Anything | Cannot be **more restrictive** |
| Exceptions | Anything | No new/broader **checked** exceptions |
| `static`/`private`/`final` | Overloadable | Not overridable (static = **hiding**) |

### Q: What are sealed classes/interfaces (Java 17)?

A middle ground: an abstraction that controls **exactly who may implement it**.

```java
public sealed interface Shape permits Circle, Square, Triangle { }
public record Circle(double r) implements Shape { }
```

Two wins: nobody outside the list can implement it, and a `switch` over `Shape` is **exhaustive** — the compiler proves you covered every case, no `default` needed. Ideal for closed domain models (a result type, a state machine). See **[12 — Java 17](./12-java17-features.md)**.

---

# Part 9 — Predict the output

**Q1**
```java
interface A { default void hi() { System.out.println("A"); } }
interface B { default void hi() { System.out.println("B"); } }
class C implements A, B { }
```
<details><summary>Answer</summary>**Compile error** — `class C inherits unrelated defaults for hi() from types A and B`. Fix by overriding `hi()` in C, optionally delegating with `A.super.hi()`.</details>

**Q2**
```java
class P { public String who() { return "class"; } }
interface I { default String who() { return "interface"; } }
class C extends P implements I { }
System.out.println(new C().who());
```
<details><summary>Answer</summary>`class` — **class wins over interface**, always.</details>

**Q3**
```java
interface Config { int TIMEOUT = 30; }
class Impl implements Config {
    void change() { TIMEOUT = 60; }
}
```
<details><summary>Answer</summary>**Compile error** — interface fields are implicitly `public static final`, so `TIMEOUT` cannot be reassigned. This is the "interfaces can't hold state" rule in practice.</details>

**Q4**
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
The superclass constructor runs **before** `Dog`'s field initialisers, so `sound` is still `null` when the overridden `speak()` is called. **Never call an overridable method from a constructor** — a genuinely nasty real-world bug, and a great answer to give proactively.
</details>

**Q5**
```java
abstract class A { abstract void m(); }
class B extends A { }
```
<details><summary>Answer</summary>**Compile error** — `B is not abstract and does not override abstract method m()`. Either implement `m()` or declare `B` abstract.</details>

**Q6**
```java
interface I {
    static void s() { System.out.println("static"); }
    default void d() { System.out.println("default"); }
}
class C implements I { }
C c = new C();
c.d();
c.s();
```
<details><summary>Answer</summary>`c.d()` prints `default`. `c.s()` is a **compile error** — interface `static` methods are **not inherited** by implementing classes; you must call `I.s()`. (Different from class static methods, which *are* inherited.)</details>

**Q7**
```java
@FunctionalInterface
interface F {
    void a();
    void b();
}
```
<details><summary>Answer</summary>**Compile error** — `@FunctionalInterface` requires exactly one abstract method; F has two. Without the annotation it compiles fine but can't be used as a lambda target.</details>

---

# Part 10 — Rapid-fire table

| Question | Answer |
|---|---|
| Multiple inheritance? | Interface ✅ many · abstract class ❌ one |
| Instance fields? | Interface ❌ (constants only) · abstract class ✅ |
| Constructor? | Interface ❌ · abstract class ✅ (runs via `super()`) |
| Method bodies in an interface? | ✅ `default`/`static` (8), `private` (9) |
| Can an abstract class have no abstract methods? | ✅ — `abstract` just means not instantiable |
| Can an abstract method be private/static/final? | ❌ all three — contradictions |
| Interface field modifiers? | Implicitly `public static final` |
| Interface method modifiers? | Implicitly `public abstract` (unless default/static/private) |
| Can an interface extend another? | ✅ and **multiple** — `interface A extends B, C` |
| Can an abstract class implement an interface? | ✅ without implementing its methods |
| Diamond with defaults? | Compile error → override, use `A.super.m()` |
| Class vs interface method conflict? | **Class wins** |
| Interface static methods inherited? | ❌ — call `Interface.method()` |
| Lambda target? | Functional **interface** only, never an abstract class |
| Functional interface? | Exactly one abstract method (SAM) |
| Marker interface? | Empty interface used as a tag — `Serializable` |
| Why prefer interfaces? | One-superclass scarcity, testability, DIP, loose coupling |
| Why still use abstract classes? | Shared **state** + shared constructor + template method + `final` methods |
| Rule of thumb | Interface = contract/can-do · Abstract class = shared code/is-a |

---

# Part 11 — Drills

1. You need a `Bird` and a `Plane` to both be flyable. Interface or abstract class?
<details><summary>Answer</summary>Interface `Flyable` — the classes are unrelated and `Bird` likely already extends `Animal`. "Can-do" capability across unrelated hierarchies is the textbook interface case.</details>

2. Three payment gateways share retry logic, a timeout field and an audit log, but each calls a different API. Design it.
<details><summary>Answer</summary>Both: an interface `PaymentGateway { PaymentResult pay(Payment p); }` as the contract callers depend on, plus `AbstractPaymentGateway implements PaymentGateway` holding the timeout field, retry loop and audit logging, with an abstract `callProvider()` hook. Concrete gateways extend the base. Template method + DIP.</details>

3. Can you add a method to a published interface without breaking implementers? How?
<details><summary>Answer</summary>Yes — add it as a `default` method with a sensible body. That's exactly why Java 8 added them (`Collection.stream()`). Adding an **abstract** method breaks every implementer.</details>

4. Why can't interfaces have constructors?
<details><summary>Answer</summary>Constructors initialise **instance state**, and interfaces have none. There's also no single "super-interface" chain to order construction — a class can implement many interfaces, so which constructor would run, and in what order?</details>

5. Your teammate creates an interface for every service class, each with exactly one `*Impl`. Push back or accept?
<details><summary>Answer</summary>Push back, politely and with nuance. Ceremony interfaces double the file count for no seam — modern Spring proxies concrete classes with CGLIB, and Mockito mocks concrete classes fine. Add the interface when there's a second implementation, a module/API boundary, or a genuine test seam.</details>

6. Give one thing an abstract class can do that an interface can never do, and vice versa.
<details><summary>Answer</summary>Abstract class only: hold mutable instance state (and run a constructor to initialise it); also `final` methods and `protected` members. Interface only: be implemented by a class that already extends something else — i.e. participate in multiple inheritance of type. Also: be a lambda target.</details>

---

# Part 12 — The C#/.NET version

Since your log shows .NET rounds too (**[29 — ASP.NET MVC](./29-dotnet-mvc-interview-questions.md)**), the same question in C# has different details — mixing them up is a visible error:

| | Java | C# |
|---|---|---|
| Default methods in interfaces | Java 8 (`default`) | C# 8 (default interface members) |
| Virtual by default? | Methods **are** virtual unless `final` | Methods are **non-virtual** unless `virtual`; override needs `override` |
| Explicit implementation | ❌ | ✅ `void IFoo.Bar() { }` — resolves name clashes cleanly |
| Diamond disambiguation | `A.super.m()` | Explicit interface implementation |
| Constants in interfaces | Implicit `public static final` | Not allowed pre-C# 8 (`static readonly` on a class instead) |
| Abstract keyword | `abstract` | `abstract`, and subclass must use `override` |

---

**Related files:** [05 — Core Java](./05-java.md) · [17 — SOLID & Design Patterns](./17-solid-design-patterns.md) · [12 — Java 17 (sealed)](./12-java17-features.md) · [23 — Predict the Output](./23-java-output-tricky-questions.md)
