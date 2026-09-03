# Core Java — Interview Q&A (Easy Version)

> **How to use this file:** every question follows the same shape —
> **Easiest way to remember → Simple explanation → Real-world example → Diagram → Interview trap → Interview-ready answer → Easy memory box.**
> Read the "Easy memory" box the night before the interview. Read the full answer during study time.

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

## Contents

1. [OOP Concepts](#oop-concepts)
2. [Core Java](#core-java)
3. [Collections](#collections)
4. [Exceptions](#exceptions)
5. [Java 8+ Features](#java-8-features)
6. [Multithreading](#multithreading)
7. [Memory & Garbage Collection](#memory--garbage-collection)

---

## OOP Concepts

### Q: What are the four pillars of OOP? Give a short example of each.

The easiest way to remember them is the word **A PIE**:

```
A → Abstraction
P → Polymorphism
I → Inheritance
E → Encapsulation
```

Think of an **ATM machine**. That one machine explains all four pillars.

---

#### 1. Encapsulation — "Hide the data, give a safe door"

Encapsulation means keeping the data **private** and giving controlled access through methods.

```java
class Account {

    private double balance;          // hidden — nobody can touch directly

    public double getBalance() {     // safe door to READ
        return balance;
    }

    public void deposit(double amount) {   // safe door to WRITE
        if (amount > 0) {                  // rule protects the data
            balance = balance + amount;
        }
    }
}
```

Why is this good?

```java
Account a = new Account();

a.balance = -5000;    // ❌ Not allowed — field is private
a.deposit(-5000);     // ✅ Allowed to call, but our rule rejects it
```

Real-world idea: **ATM machine**

```
        You
         │
         │ (you can only press buttons)
         ↓
   ┌──────────────┐
   │  ATM screen  │  ← public methods
   │  Withdraw    │
   │  Balance     │
   └──────┬───────┘
          │
          ↓
   ┌──────────────┐
   │  CASH BOX    │  ← private data
   │  ₹50,00,000  │
   └──────────────┘
```

You can never open the cash box directly. You must use the buttons.

**Encapsulation = private data + public methods**

---

#### 2. Abstraction — "Show what it does, hide how it does it"

```java
interface Payment {
    void pay(double amount);
}
```

The caller only knows `pay()`. They don't know if it is UPI, card, or net banking.

Real-world idea: **Driving a car**

```
Driver presses ACCELERATOR
         ↓
   ┌───────────────────────┐
   │ fuel injection        │
   │ spark plug timing     │  ← hidden from driver
   │ piston movement       │
   └───────────────────────┘
         ↓
     Car moves
```

The driver only needs the pedal. They don't need engine knowledge.

**Abstraction = hide complexity, show only the necessary part**

---

#### 3. Inheritance — "Child gets parent's property"

```java
class Payment {
    void validate() {
        System.out.println("Common validation");
    }
}

class UpiPayment extends Payment {
    void sendToBank() {
        System.out.println("UPI request sent");
    }
}
```

Now:

```java
UpiPayment u = new UpiPayment();

u.validate();     // came free from parent
u.sendToBank();   // its own method
```

Diagram:

```
        Payment
        validate()
            │
     ┌──────┴──────┐
     ↓             ↓
UpiPayment    CardPayment
sendToBank()  swipeCard()
```

Real-world idea: You inherit your family surname. You didn't create it, you received it.

**Inheritance = reuse the parent's code**

---

#### 4. Polymorphism — "One name, many behaviours"

```java
Payment p;

p = new UpiPayment();
p.pay(500);            // UPI logic runs

p = new CardPayment();
p.pay(500);            // Card logic runs
```

Same line `p.pay(500)` gives different behaviour.

Real-world idea: The word **"run"**

```
"run"  →  a person runs        (legs)
"run"  →  a program runs       (CPU)
"run"  →  a nose runs          (cold 🙂)
```

Same word, different meaning based on context.

**Polymorphism = same method call, different behaviour**

---

#### All four together

```java
// ABSTRACTION — contract only
interface Payment {
    void pay(double amount);
}

// ENCAPSULATION — private data + safe access
class Wallet {
    private double balance;
    public double getBalance() { return balance; }
}

// INHERITANCE + POLYMORPHISM
class UpiPayment implements Payment {
    public void pay(double amount) { System.out.println("UPI paid " + amount); }
}

class CardPayment implements Payment {
    public void pay(double amount) { System.out.println("Card paid " + amount); }
}

Payment p = new UpiPayment();   // polymorphic reference
p.pay(500);
```

#### Interview-ready answer

> The four pillars of OOP are Encapsulation, Abstraction, Inheritance and Polymorphism. Encapsulation means hiding the internal data using private fields and exposing controlled access through getters and setters — like an ATM where you can only use the buttons, not the cash box. Abstraction means showing only the required behaviour and hiding the implementation, which we achieve using interfaces and abstract classes. Inheritance lets a child class reuse the parent class's code. Polymorphism means the same method call behaves differently depending on the actual object — for example a `Payment` reference can point to `UpiPayment` or `CardPayment` and `pay()` behaves accordingly.

#### 🔵 In your RoboGebra project — all four pillars in ONE example ⭐

**File:** `common/service/event/EventTrackingService.java` + its two implementations

```java
// ABSTRACTION — the contract. Callers see only WHAT, never HOW ⭐
public interface EventTrackingService {
    void trackUserLogin(User user);
    void trackPaymentEvent(SubscriptionInstanceWithItems subscription);
    void trackSolutionEvent(String userId, ExerciseSolutionDTO dto);
}

// POLYMORPHISM — two implementations, chosen by ENVIRONMENT ⭐
@Service
@Profile({"local", "dev"})
@ConditionalOnProperty(prefix = "posthog", name = "enabled",
                       havingValue = "false", matchIfMissing = true)
public class LoggingEventTrackingService implements EventTrackingService { ... }

@Service
@Profile({"prod"})
@ConditionalOnProperty(prefix = "posthog", name = "enabled", havingValue = "true")
public class PostHogEventTrackingService implements EventTrackingService { ... }
```

```java
// ENCAPSULATION — private state, controlled access
@Document(collection = "exercise_solution")
public class ExerciseSolutionEntity {
    @Id private String id;                          // private ⭐
    @Field("step_output_json") private Object stepOutputJson;
    // Lombok @Getter/@Setter generate the controlled accessors
}

// INHERITANCE — a shared base with three subclasses
public abstract class AbstractServiceAuthenticationFilter extends OncePerRequestFilter { ... }
public class CrmServiceAuthenticationFilter extends AbstractServiceAuthenticationFilter { ... }
public class WebhookAuthorizationFilter    extends AbstractServiceAuthenticationFilter { ... }
```

> 🗣️ **Say it like this:** *"Our analytics layer is the clearest example. `EventTrackingService` is the abstraction — services depend on the interface and never know where events go. There are two implementations chosen by profile: in dev it just logs, in production it posts to PostHog. That's polymorphism doing real work — the calling code is identical in both environments, and nobody needs a PostHog key on their laptop."*

⭐ Note it's `@Profile` **plus** `@ConditionalOnProperty` — so it's environment-driven *and* individually switchable. That's a nice detail to add.

#### Easy memory

```
A PIE

Abstraction     → hide HOW        (car pedal)
Polymorphism    → many FORMS      (word "run")
Inheritance     → get from PARENT (surname)
Encapsulation   → hide DATA       (ATM cash box)
```

---

### Q: What is the difference between abstraction and encapsulation?

The easiest way to remember:

```
Abstraction    → hides COMPLEXITY  (design level)
Encapsulation  → hides DATA        (code level)
```

People confuse these two constantly, so use one single example — a **mobile phone**.

#### Abstraction

You tap the "Call" button. You don't know about towers, signals, or network protocols.

```
     You tap "Call"
          ↓
 ┌────────────────────┐
 │ tower handshake    │
 │ signal encoding    │  ← hidden complexity
 │ network routing    │
 └────────────────────┘
          ↓
     Call connects
```

In code:

```java
interface Phone {
    void call(String number);      // WHAT it does
}
```

Nobody sees HOW.

#### Encapsulation

Inside the phone, your contacts are stored privately. Apps can't directly open that storage — they must ask permission.

```java
class Phone {

    private String pin = "1234";      // DATA is hidden

    public boolean unlock(String input) {
        return pin.equals(input);
    }
}
```

Nobody can read the `pin` directly.

#### Side-by-side

| | Abstraction | Encapsulation |
|---|---|---|
| Hides | Complexity / implementation | Data / state |
| Level | Design level | Implementation level |
| Achieved by | interface, abstract class | private fields + getters/setters |
| Question it answers | **WHAT** does it do? | **HOW** is data protected? |
| Example | `interface Payment { pay(); }` | `private double balance;` |

#### Interview-ready answer

> Abstraction is about hiding complexity and exposing only the essential behaviour — it is solved at the design level using interfaces and abstract classes. Encapsulation is about hiding the data itself by making fields private and giving controlled access through getters and setters — it is solved at the implementation level. In short, abstraction hides complexity, encapsulation hides data.

#### 🔵 In your RoboGebra project

```
ABSTRACTION  → EventTrackingService — a service calls trackUserLogin(user)
               and has NO IDEA whether that logs to a file or posts to PostHog ⭐

ENCAPSULATION→ ExerciseSolutionEntity — every field is private; the cached
               `hash`, the ids and the AI payload are only reachable through
               accessors, so nothing outside can put the object in a bad state ⭐
```

> 🗣️ *"In our codebase the analytics interface is abstraction — it hides the entire implementation behind three method names. The entities are encapsulation — private fields with controlled accessors. One hides complexity, the other hides data."*

#### Easy memory

```
Abstraction   → "WHAT it does"   → interface
Encapsulation → "Data is safe"   → private + getter/setter
```

---

### Q: Abstract class vs interface — when do you use which?

The easiest way to remember:

```
Abstract class  →  "IS-A" relationship  →  common CODE + common STATE
Interface       →  "CAN-DO" ability     →  only a CONTRACT
```

#### Simple story

Think about **birds**.

```
        Bird (abstract class)
        - name, weight   ← shared STATE
        - eat()          ← shared CODE
        - fly()          ← abstract, each bird differs
              │
       ┌──────┴──────┐
       ↓             ↓
    Sparrow        Eagle
```

Every bird IS-A bird, and every bird eats the same way — so `Bird` is an **abstract class**.

Now, a bird **can swim**, and a fish **can swim** too — but a fish is not a bird. Swimming is an **ability**, not a family.

```
interface Swimmable  →  Duck, Fish, Human   (unrelated classes)
```

That is an **interface**.

#### Code

```java
abstract class Bird {

    String name;                       // ✅ state allowed

    Bird(String name) {                // ✅ constructor allowed
        this.name = name;
    }

    void eat() {                       // ✅ shared code
        System.out.println(name + " is eating");
    }

    abstract void fly();               // each child decides
}

interface Swimmable {                  // pure ability
    void swim();
}

class Duck extends Bird implements Swimmable {

    Duck() { super("Duck"); }

    void fly()  { System.out.println("Duck flies short distance"); }
    public void swim() { System.out.println("Duck swims"); }
}
```

Note: **one** `extends`, but **many** `implements`.

```
Duck
 ├── extends Bird          (only ONE parent allowed)
 └── implements Swimmable, Quackable, Sellable  (MANY allowed)
```

#### Full comparison

| Aspect | Abstract class | Interface |
|---|---|---|
| Multiple inheritance | ❌ only one | ✅ many |
| Instance fields | ✅ allowed | ❌ only `public static final` constants |
| Constructor | ✅ yes | ❌ no |
| Method types | concrete + abstract | abstract + `default` + `static` (Java 8), `private` (Java 9) |
| Access modifiers | any (`private`, `protected`…) | methods implicitly `public` |
| Keyword to use | `extends` | `implements` |
| Best for | shared code + shared state | contract / capability |

#### Interview-ready answer

> Use an abstract class when the classes share common state and common code and there is a clear "is-a" relationship — for example `Bird` with a shared `eat()` method and a `name` field. Use an interface when you only want to define a contract or a capability that unrelated classes can implement, such as `Swimmable` or `Comparable`. A class can extend only one abstract class but can implement many interfaces, so interfaces also solve the multiple-inheritance need. Since Java 8, interfaces can have default and static methods, but they still cannot hold instance state.

#### 🔵 In your RoboGebra project — you use BOTH, for exactly the right reasons ⭐⭐

**Abstract class:** `security/AbstractServiceAuthenticationFilter.java`

```java
public abstract class AbstractServiceAuthenticationFilter extends OncePerRequestFilter {

    protected final RequestMappingHandlerMapping handlerMapping;   // ⭐ SHARED STATE
    protected final ExceptionResponseWriter responseWriter;        //    (impossible in an interface)
    protected final UserDetailsService userDetailsService;

    protected AbstractServiceAuthenticationFilter(                 // ⭐ CONSTRUCTOR
            RequestMappingHandlerMapping handlerMapping,
            ExceptionResponseWriter responseWriter,
            UserDetailsService userDetailsService) { ... }

    // ===== subclasses MUST implement =====
    protected abstract Class<? extends Annotation> getAnnotationClass();
    protected abstract String getHeaderName();
    protected abstract String getExpectedToken();
    protected abstract String getFilterName();
    protected abstract UserRole getUserRole();

    // ===== optional HOOKS with a default ⭐ =====
    protected String extractToken(HttpServletRequest request) {
        return request.getHeader(getHeaderName());          // sensible default
    }
    protected void additionalValidation(HttpServletRequest request) {
        // default: nothing
    }
}
```

**Subclass:**

```java
@Component
@Order(3)
public class CrmServiceAuthenticationFilter extends AbstractServiceAuthenticationFilter {

    private final CrmApiProperties crmApiProperties;

    public CrmServiceAuthenticationFilter(..., CrmApiProperties crmApiProperties) {
        super(handlerMapping, responseWriter, userDetailsService);   // ⭐ super(...)
        this.crmApiProperties = crmApiProperties;
    }

    @Override protected Class<? extends Annotation> getAnnotationClass() { return CrmServiceAPI.class; }
    @Override protected String getHeaderName()    { return ApplicationConstants.CRM_SERVICE_TOKEN; }
    @Override protected String getExpectedToken() { return crmApiProperties.getCrmServiceToken(); }
    @Override protected String getFilterName()    { return "CRM-Service"; }
}
```

**Interface:** `EventTrackingService` — a pure contract, two implementations, no shared state.

> 🗣️ **The answer that shows judgement:** *"We use both, and the reason is exactly the textbook one. The service-authentication filters share **state and behaviour** — the handler mapping, the response writer, and the whole 'read the header, compare the token, build the security context' algorithm. That's an abstract class, and it's a template method: `doFilterInternal` is written once, and each subclass supplies five small pieces — which annotation to look for, which header, which expected token. CRM auth and webhook auth are two subclasses of about thirty lines each. The analytics tracker, by contrast, shares **nothing** — the logging version and the PostHog version have no common code — so that's an interface."*

⭐ **The rule, from your own code:** *"If I catch myself wanting a field or a constructor, that's an abstract class. If it's just a contract, it's an interface."*

⭐ And note the **optional hooks** — `extractToken()` and `additionalValidation()` have defaults that subclasses *may* override. That's another thing an interface couldn't give you before Java 8 default methods.

#### Easy memory

```
Abstract class → IS-A  → Duck IS-A Bird     → extends  (only 1)
Interface      → CAN-DO → Duck CAN swim     → implements (many)

Need shared FIELDS or a CONSTRUCTOR? → abstract class
Need MULTIPLE types / pure contract? → interface
```

---

### Q: What are default and static methods in interfaces?

The easiest way to remember:

```
default method → interface gives a READY-MADE body that children inherit
static  method → utility that belongs to the INTERFACE itself
```

#### Why were they added? (This is the real interview point)

Imagine you wrote an interface used by 500 classes in production:

```java
interface Payment {
    void pay(double amount);
}
```

Now you want to add `refund()`.

```java
interface Payment {
    void pay(double amount);
    void refund(double amount);     // ❌ 500 classes break instantly
}
```

Every implementing class fails to compile. This is exactly what happened to Java 8 — they wanted to add `stream()` to the `Collection` interface without breaking the whole world.

Solution: **default method**.

```java
interface Payment {

    void pay(double amount);

    default void refund(double amount) {          // ✅ nothing breaks
        System.out.println("Refund not supported");
    }
}
```

```
Before Java 8                After Java 8
─────────────                ────────────
add method                   add DEFAULT method
    ↓                             ↓
all children break ❌        children inherit body ✅
```

#### default method — example

```java
interface Payment {

    void pay(double amount);

    default void receipt() {
        System.out.println("Payment receipt generated");
    }
}

class UpiPayment implements Payment {
    public void pay(double amount) {
        System.out.println("UPI paid " + amount);
    }
    // receipt() comes free
}
```

```java
UpiPayment u = new UpiPayment();
u.pay(500);
u.receipt();      // Payment receipt generated
```

A child can also **override** it if it wants its own behaviour.

#### static method — example

```java
interface Payment {

    void pay(double amount);

    static boolean isValid(double amount) {      // helper utility
        return amount > 0;
    }
}
```

Called on the **interface name**, not on the object:

```java
Payment.isValid(500);     // ✅
u.isValid(500);           // ❌ compile error
```

```
default method → belongs to the OBJECT  → obj.method()
static  method → belongs to the INTERFACE → Interface.method()
```

#### The diamond problem trap

```java
interface A {
    default void hello() { System.out.println("A"); }
}

interface B {
    default void hello() { System.out.println("B"); }
}

class C implements A, B {      // ❌ which hello() should C use?
}
```

Java cannot decide, so it forces **you** to decide:

```java
class C implements A, B {

    public void hello() {
        A.super.hello();       // explicitly pick one
    }
}
```

```
       A.hello()      B.hello()
            \            /
             \          /
              ↓        ↓
                 C  ← must override and choose
```

#### Interview-ready answer

> Default and static methods were introduced in Java 8 so that interfaces could evolve without breaking existing implementations. A default method has a body inside the interface, so all implementing classes automatically inherit it and can optionally override it — this is how `stream()` was added to the `Collection` interface without breaking existing code. A static method is a utility that belongs to the interface itself and is called using the interface name. If a class implements two interfaces that have the same default method, it causes an ambiguity, and the class must override the method and choose one explicitly using `InterfaceName.super.method()`.

#### Easy memory

```
default → gives a FREE body      → obj.method()      → can override
static  → gives a HELPER utility → Interface.method() → cannot override

Reason they exist: add new methods WITHOUT breaking old classes.
```

---

### Q: Overloading vs overriding?

The easiest way to remember:

```
Overloading = Same method name, DIFFERENT parameters
Overriding  = Child class CHANGES the parent's implementation
```

#### 1. Method Overloading

Suppose you have a payment application where customers can pay in different ways.

```java
class Payment {

    void pay(int amount) {
        System.out.println("Pay amount");
    }

    void pay(int amount, String currency) {
        System.out.println("Pay with currency");
    }

    void pay(int amount, String currency, String coupon) {
        System.out.println("Pay with coupon");
    }
}
```

All methods have the **same name**, but **different parameters**:

```
pay(int)
pay(int, String)
pay(int, String, String)
```

You can call:

```java
Payment p = new Payment();

p.pay(1000);
p.pay(1000, "INR");
p.pay(1000, "INR", "SAVE10");
```

Real-world idea — a **calculator**:

```
add(10, 20)
add(10, 20, 30)
add(10.5, 20.5)
```

Same operation, different inputs.

**Overloading = same name + different parameters**

#### 2. Method Overriding

```java
class Payment {
    void pay() {
        System.out.println("Processing payment");
    }
}

class UpiPayment extends Payment {
    @Override
    void pay() {
        System.out.println("Processing UPI payment");
    }
}

class CardPayment extends Payment {
    @Override
    void pay() {
        System.out.println("Processing Card payment");
    }
}
```

Now:

```java
Payment p = new UpiPayment();
p.pay();
```

Output:

```
Processing UPI payment
```

The child class **replaced** the parent's implementation.

```
             Payment
              pay()
                │
        ────────┴────────
        ↓               ↓
   UpiPayment      CardPayment
      pay()            pay()
        ↓                ↓
   UPI logic        Card logic
```

**Overriding = same method + child provides different behaviour**

#### Main difference

| Overloading | Overriding |
|---|---|
| Same method name | Same method signature |
| Different parameters | Same parameters |
| Usually within the same class | Parent–child relationship |
| Inheritance not required | Inheritance required |
| Compile-time polymorphism | Runtime polymorphism |
| Return type alone cannot overload | Return type must be same or covariant |
| `@Override` not used | `@Override` recommended |

#### Very common interview trap

**Can we overload only by changing the return type?**

No. ❌

```java
int calculate() {
    return 10;
}

double calculate() {      // ❌ compile error
    return 10.5;
}
```

Because both have `calculate()` with exactly the same parameter list. Java decides which method to call by looking at the **arguments you pass**, not the return type — so it cannot tell them apart.

#### Overriding rules to remember

```
Same signature                      ✅ must
Return type same or covariant       ✅ (child return type allowed)
Access modifier NOT more restrictive
  public  → public        ✅
  public  → protected     ❌
Cannot throw BROADER checked exceptions
private / static / final methods    ❌ cannot be overridden
```

#### Interview-ready answer

> Method overloading means having multiple methods with the same name but different parameter lists. It is resolved at compile time, so it is compile-time polymorphism — for example `pay(amount)` and `pay(amount, currency)`. Method overriding happens when a child class provides its own implementation of a method already defined in the parent class with the same signature. It is resolved at runtime based on the actual object, so it is runtime polymorphism — for example `UpiPayment` and `CardPayment` both overriding `pay()` from `Payment`. We cannot overload a method by changing only the return type, because Java resolves overloads using the parameter list.

#### 🔵 In your RoboGebra project — overriding, and why `@Override` matters ⭐

**File:** `security/CrmServiceAuthenticationFilter.java`

```java
public class CrmServiceAuthenticationFilter extends AbstractServiceAuthenticationFilter {

    @Override protected Class<? extends Annotation> getAnnotationClass() { return CrmServiceAPI.class; }
    @Override protected String getHeaderName()    { return ApplicationConstants.CRM_SERVICE_TOKEN; }
    @Override protected String getExpectedToken() { return crmApiProperties.getCrmServiceToken(); }
    @Override protected String getFilterName()    { return "CRM-Service"; }
}
```

```
The PARENT calls getHeaderName() — but at RUNTIME it resolves to the
CRM subclass's version, or the Webhook subclass's version. ⭐

That is RUNTIME polymorphism doing the actual work:
one algorithm in the base class, five decisions per subclass ⭐
```

> 🗣️ *"Every one of those is an override, and `@Override` is doing real work — if someone renames `getHeaderName` in the abstract class, the subclass fails to compile instead of silently becoming a new unused method."*

⚠️ Note these are `protected`, not `public` — an override **cannot reduce visibility**, so a subclass could widen them to `public`, but never narrow them to `private`.

#### Easy memory

```
OverLOADing → LOAD different parameters → same class    → compile time
OverRIDing  → RIDE over the parent      → child class   → runtime
```

---

### Q: What do `super` and `this` mean?

The easiest way to remember:

```
this  → THIS object   (current class)
super → SUPERior      (parent class)
```

#### `this` — points to the current object

The most common use is when the parameter name and the field name are the same:

```java
class Employee {

    private String name;

    Employee(String name) {
        this.name = name;      // this.name = field, name = parameter
    }
}
```

Without `this`:

```java
Employee(String name) {
    name = name;      // ❌ assigning parameter to itself — field stays null
}
```

```
Constructor parameter:  name = "Priya"
Object field:           this.name = null

this.name = name;
     ↓         ↓
   field   parameter
```

Other uses of `this`:

```java
class Employee {

    private String name;
    private int age;

    Employee() {
        this("Unknown", 0);        // 1️⃣ call another constructor
    }

    Employee(String name, int age) {
        this.name = name;          // 2️⃣ refer to a field
        this.age = age;
    }

    Employee getSelf() {
        return this;               // 3️⃣ return the current object
    }
}
```

#### `super` — points to the parent object

```java
class Payment {

    void pay() {
        System.out.println("Common validation done");
    }
}

class UpiPayment extends Payment {

    @Override
    void pay() {
        super.pay();                          // run parent logic first
        System.out.println("UPI payment done");
    }
}
```

Output:

```
Common validation done
UPI payment done
```

Diagram:

```
UpiPayment.pay()
      │
      │ super.pay()
      ↓
 Payment.pay()   ← parent code runs
      │
      ↓ back
UPI specific code runs
```

Real-world idea: You are cooking your own recipe, but first you follow your mother's base gravy — `super.makeGravy()` — then add your own twist.

#### `super()` in constructors

```java
class Payment {
    Payment() {
        System.out.println("Payment constructor");
    }
}

class UpiPayment extends Payment {
    UpiPayment() {
        super();                       // implicit — Java adds it for you
        System.out.println("UPI constructor");
    }
}
```

Output:

```
Payment constructor
UPI constructor
```

Parent is **always** built first — you cannot build the first floor before the ground floor.

```
new UpiPayment()
      ↓
Payment()  ← parent constructor runs FIRST
      ↓
UpiPayment()
```

#### Interview trap

```java
class Child extends Parent {
    Child() {
        System.out.println("hi");
        super();          // ❌ compile error
    }
}
```

`this()` and `super()` **must be the very first statement** in a constructor, and you cannot use both in the same constructor.

#### Interview-ready answer

> `this` refers to the current object. We use it to distinguish a field from a parameter with the same name, to call another constructor of the same class using `this(...)`, and to return the current instance. `super` refers to the immediate parent class. We use `super.method()` to call the parent's version of an overridden method, `super.field` to access a parent field, and `super(...)` to call the parent constructor. Both `this(...)` and `super(...)` must be the first statement in a constructor, and the parent constructor always runs before the child constructor.

#### 🔵 In your RoboGebra project — `super(...)` in a real constructor chain ⭐

**File:** `security/CrmServiceAuthenticationFilter.java`

```java
public CrmServiceAuthenticationFilter(
        RequestMappingHandlerMapping handlerMapping,
        ExceptionResponseWriter responseWriter,
        UserDetailsService userDetailsService,
        CrmApiProperties crmApiProperties) {

    super(handlerMapping, responseWriter, userDetailsService);   // ⭐ FIRST statement
    this.crmApiProperties = crmApiProperties;                    // ⭐ then my own field
}
```

```
new CrmServiceAuthenticationFilter(...)
      ↓
super(...) → AbstractServiceAuthenticationFilter's constructor runs FIRST ⭐
      ↓      (sets handlerMapping, responseWriter, userDetailsService)
      ↓
back here → this.crmApiProperties = ...
```

⭐ This is also the **Spring dependency-injection** pattern: the subclass takes *everything* the parent needs **plus** its own dependency, and passes the parent's up through `super(...)`.

> 🗣️ *"The base filter needs three collaborators and each subclass needs one of its own, so the subclass constructor takes all four and passes three up with `super(...)` — which has to be the first statement, because the parent must be fully built before the child touches anything."*

#### Easy memory

```
this  → current object → this.name = name;      → this(...) same class ctor
super → parent object  → super.pay();           → super(...) parent ctor

Both must be the FIRST line in a constructor.
Parent constructor ALWAYS runs first.
```

---

### Q: Explain constructors and constructor chaining.

The easiest way to remember:

```
Constructor = the method that runs automatically when you say `new`
Its job     = set the object's starting values
```

#### Rules of a constructor

```
1. Name must be EXACTLY the class name
2. NO return type (not even void)
3. Runs automatically on `new`
4. If you write none, Java gives a free default constructor
```

```java
class Employee {

    String name;

    Employee() {                      // constructor
        System.out.println("Object created");
    }
}

Employee e = new Employee();          // prints: Object created
```

If it had a return type it would just be a normal method:

```java
void Employee() { }     // ⚠️ this is a METHOD named Employee, not a constructor
```

#### Types of constructors

```java
class Employee {

    String name;
    int age;

    Employee() {                       // 1️⃣ no-arg / default
        name = "Unknown";
        age = 0;
    }

    Employee(String name, int age) {   // 2️⃣ parameterised
        this.name = name;
        this.age = age;
    }

    Employee(Employee other) {         // 3️⃣ copy constructor
        this.name = other.name;
        this.age = other.age;
    }
}
```

#### The default constructor trap ⭐

```java
class Employee {
    Employee(String name) { }      // you wrote a parameterised one
}

Employee e = new Employee();       // ❌ compile error
```

Java gives a free no-arg constructor **only if you write no constructor at all**. The moment you write one, the free gift is withdrawn.

```
No constructor written  → Java adds Employee() for free ✅
Any constructor written → Java adds NOTHING ❌
```

This is exactly why Hibernate/JPA entities need an explicit no-arg constructor.

#### Constructor chaining

Constructor chaining means **one constructor calls another**, so you write the setup logic only once.

Two kinds:

```
this(...)  → chain WITHIN the same class
super(...) → chain to the PARENT class
```

**Chaining with `this()`:**

```java
class Employee {

    String name;
    int age;
    String dept;

    Employee() {
        this("Unknown", 0, "General");        // calls the 3-arg one
    }

    Employee(String name) {
        this(name, 0, "General");             // calls the 3-arg one
    }

    Employee(String name, int age, String dept) {   // the REAL work happens here
        this.name = name;
        this.age  = age;
        this.dept = dept;
    }
}
```

Flow:

```
new Employee()
     ↓
this("Unknown", 0, "General")
     ↓
Employee(String, int, String)   ← actual assignment happens once
```

Benefit: if tomorrow you add validation, you add it in **one** place, not three.

**Chaining with `super()`:**

```java
class Person {
    Person() { System.out.println("1. Person"); }
}

class Employee extends Person {
    Employee() { System.out.println("2. Employee"); }
}

class Manager extends Employee {
    Manager() { System.out.println("3. Manager"); }
}

new Manager();
```

Output:

```
1. Person
2. Employee
3. Manager
```

```
new Manager()
     ↓
Manager()  → calls super() implicitly
     ↓
Employee() → calls super() implicitly
     ↓
Person()   → calls super() → Object()
     ↓
Now bodies run BOTTOM-UP:  Person → Employee → Manager
```

Real-world idea: building a house — foundation → ground floor → first floor. You can never build the first floor first.

#### Interview trap

```java
Employee() {
    System.out.println("hi");
    this("a", 1);        // ❌ must be the FIRST statement
}
```

Also, you cannot call `this()` and `super()` in the same constructor.

#### Interview-ready answer

> A constructor is a special block that has the same name as the class, has no return type, and runs automatically when an object is created using `new`. Its job is to initialise the object's state. If we do not write any constructor, Java provides a default no-argument constructor, but once we write any constructor ourselves, that default is no longer provided. Constructor chaining means one constructor calls another — `this(...)` chains to another constructor in the same class to avoid duplicate initialisation code, and `super(...)` chains to the parent constructor. Both must be the first statement in the constructor, and the parent constructor always executes before the child constructor.

#### 🔵 In your RoboGebra project — constructor overloading, done well ⭐

**File:** `common/exception/ResourceNotFoundException.java`

```java
public class ResourceNotFoundException extends RuntimeException {

    // 1️⃣ the CONVENIENT one — builds a consistent message ⭐
    public ResourceNotFoundException(String entityType, Object id) {
        super(MessageFormat.format("The {0} with {1} not found.", entityType, id));
    }

    // 2️⃣ the FLEXIBLE one — a free-form message
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

```java
throw new ResourceNotFoundException("Exercise", exerciseId);
      // → "The Exercise with 64f3a1b2 not found."   ⭐ consistent, every time
```

**File:** `common/exception/APIException.java` — carrying extra state on the exception:

```java
@Getter
public class APIException extends RuntimeException {
    private final HttpStatus status;        // ⭐ extra business data
    private final String errorMessage;

    public APIException(HttpStatus status, String errorMessage) {
        super(errorMessage);                 // ⭐ always pass it to the parent
        this.status = status;
        this.errorMessage = errorMessage;
    }
}
```

> 🗣️ *"Two constructors on the same exception — one takes an entity type and an id and formats a consistent message, which is what almost every call site uses; the other takes a free-form message for the odd case. `APIException` goes further and carries an `HttpStatus`, so the exception itself knows what status code it should become — the advice just reads it rather than mapping by type."*

⭐ **12 custom exceptions** in the codebase: `ResourceNotFoundException`, `APIException`, `CognitoException`, `DatabaseException`, `ExternalApiException`, `SolutionExecutionException`, `QuizConflictException`, `EmailSendingException`, `RestrictedAPIAccessException`, `FirebaseMessageException`, `JsonException`, `FileNotFoundException` — **all extending `RuntimeException`**, i.e. unchecked, the Spring style.

#### Easy memory

```
Constructor  = class name + no return type + runs on `new`
Free default = ONLY if you wrote zero constructors
this(...)    = same class      → avoid duplicate code
super(...)   = parent class    → parent built first
Must be the FIRST line.
```

---

### Q: Static vs instance members?

The easiest way to remember:

```
static   → belongs to the CLASS  → ONE copy shared by everyone
instance → belongs to the OBJECT → EVERY object gets its own copy
```

#### Real-world example — a school

```
School name : "St. Mary's"     ← same for ALL students  → static
Student name: "Priya", "Ravi"  ← different per student   → instance
```

```java
class Student {

    static String schoolName = "St. Mary's";   // ONE copy for all
    String name;                               // one copy PER object

    Student(String name) {
        this.name = name;
    }
}
```

```java
Student s1 = new Student("Priya");
Student s2 = new Student("Ravi");

System.out.println(s1.name);          // Priya
System.out.println(s2.name);          // Ravi
System.out.println(Student.schoolName);  // St. Mary's
```

Now change the school name once:

```java
Student.schoolName = "St. Xavier's";

System.out.println(s1.schoolName);   // St. Xavier's
System.out.println(s2.schoolName);   // St. Xavier's  ← both changed!
```

Diagram:

```
        ┌───────────── CLASS AREA ─────────────┐
        │  schoolName = "St. Mary's"  (static) │
        └───────────────┬──────────────────────┘
                        │ shared by all
        ┌───────────────┴───────────────┐
        ↓                               ↓
   ┌──────────┐                    ┌──────────┐
   │ s1       │                    │ s2       │
   │ name=    │                    │ name=    │
   │ "Priya"  │                    │ "Ravi"   │
   └──────────┘                    └──────────┘
        HEAP (each object has its own name)
```

#### A practical counter example

```java
class Counter {

    static int totalVisitors = 0;    // shared
    int myId;                        // personal

    Counter() {
        totalVisitors++;
        myId = totalVisitors;
    }
}

new Counter();  new Counter();  new Counter();

System.out.println(Counter.totalVisitors);   // 3
```

This is exactly how a website visitor counter works.

#### Static methods

```java
class MathUtil {
    static int square(int x) {
        return x * x;
    }
}

MathUtil.square(5);       // no object needed ✅
```

That is why `main` is static — the JVM must call it **before** any object exists.

```java
public static void main(String[] args)
```

#### The #1 interview trap ⭐

**Can a static method access an instance variable?**

No. ❌

```java
class Test {

    int a = 10;             // instance
    static int b = 20;      // static

    static void show() {
        System.out.println(b);   // ✅ fine
        System.out.println(a);   // ❌ compile error
    }
}
```

Why? Because a static method can run when **no object exists**, so there is no `a` to read.

```
static method runs → object may NOT exist → whose `a` should it print? → error
```

But the reverse works fine:

```java
void display() {            // instance method
    System.out.println(a);  // ✅
    System.out.println(b);  // ✅ static is always available
}
```

```
instance method → can access BOTH ✅
static method   → can access ONLY static ❌
```

#### Static block

Runs **once**, when the class is first loaded — before any object or `main`.

```java
class Config {

    static String url;

    static {
        url = "jdbc:mysql://localhost/db";
        System.out.println("Static block runs once");
    }
}
```

Order of execution:

```
Class loaded
     ↓
static block
     ↓
object created
     ↓
instance block
     ↓
constructor
```

#### Interview-ready answer

> Static members belong to the class, so only one copy exists and it is shared by all objects — for example a school name shared by all student objects, or a visitor counter. Instance members belong to each object, so every object gets its own copy. Static members are accessed using the class name and are loaded when the class is loaded, which is why `main` is static — the JVM calls it before any object exists. A static method cannot directly access instance variables because the object may not exist yet, but an instance method can access both. A static block runs only once when the class is loaded and is generally used for one-time initialisation.

#### 🔵 In your RoboGebra project — `static final` constants and enums ⭐

```java
// A static constant used as a compile-time qualifier ⭐
public class AsyncConfig {
    /** @Async needs a COMPILE-TIME CONSTANT, so this must be `static final`. */
    public static final String NOTIFICATION_TASK_EXECUTOR = "notificationTaskExecutor";
}

@Async(AsyncConfig.NOTIFICATION_TASK_EXECUTOR)     // ⭐ only a constant is legal here
public void sendPush(...) { }
```

⭐ **That's a genuinely good `static final` example** — the annotation *requires* a compile-time constant, so it physically cannot be an instance field.

```java
// enum = a fixed set of constants, with state and behaviour ⭐
@AllArgsConstructor
@Getter
public enum Feature {
    SOLUTION("solution"),
    AI_ASSISTANT("ai-assistant"),
    EXPLORE("explore");

    final String collectionName;      // ⭐ each constant carries data
}
```

```java
// a static grouping constant, used by the interceptor for a membership test ⭐
public static final ErrorCode[] MultiUserErrorCodes = {
    ErrorCode.MULTI_USER_LOGIN_EXCEEDED,
    ErrorCode.SESSION_FORCEFULLY_REMOVED
};
```

> 🗣️ *"`NOTIFICATION_TASK_EXECUTOR` has to be `static final` because `@Async` takes a compile-time constant — that's a case where the language forces the decision rather than style. And our enums carry data: `Feature` holds the collection name for each feature, so the mapping lives with the constant instead of in a switch somewhere."*

#### Easy memory

```
static   → CLASS  → one copy   → School name   → ClassName.member
instance → OBJECT → many copies→ Student name  → object.member

static method  → can use ONLY static   ❌ instance
instance method→ can use BOTH          ✅
```

---

### Q: Difference between final, finally, and finalize?

The easiest way to remember:

```
final    → a KEYWORD   → "cannot change"
finally  → a BLOCK     → "always runs"
finalize → a METHOD    → "before garbage collection" (deprecated)
```

They sound similar but have nothing to do with each other — that's why interviewers love this question.

#### 1. `final` — cannot be changed

It works in three places:

```java
final int MAX = 100;        // 1️⃣ variable → value cannot change
MAX = 200;                  // ❌ compile error
```

```java
class Payment {
    final void validate() { }    // 2️⃣ method → cannot be overridden
}

class UpiPayment extends Payment {
    void validate() { }          // ❌ compile error
}
```

```java
final class Constants { }        // 3️⃣ class → cannot be extended

class MyConstants extends Constants { }   // ❌ compile error
```

Real-world examples in the JDK:

```
String   → final class  (that is why nobody can break String's behaviour)
Integer  → final class
```

**The famous final trap ⭐**

```java
final List<String> list = new ArrayList<>();

list.add("Priya");        // ✅ ALLOWED — we changed the CONTENT
list = new ArrayList<>(); // ❌ NOT allowed — we changed the REFERENCE
```

```
final list ──────→ [ ArrayList ]
       ↑                  ↑
       │                  │
  reference LOCKED   contents FREE to change
```

Real-world idea: your house address is fixed (final), but you can still change the furniture inside.

#### 2. `finally` — always runs

Used with try-catch. It runs whether an exception happens or not.

```java
try {
    int result = 10 / 0;             // exception!
} catch (ArithmeticException e) {
    System.out.println("Error caught");
} finally {
    System.out.println("Connection closed");
}
```

Output:

```
Error caught
Connection closed
```

Even with a `return`:

```java
int test() {
    try {
        return 1;
    } finally {
        System.out.println("finally runs");   // still prints!
    }
}
```

```
try  → runs
       ↓
catch → runs only IF exception
       ↓
finally → ALWAYS runs ✅
```

Real-world idea: You go to a restaurant. You may enjoy the food (try) or find a problem (catch), but you **must pay the bill** (finally) either way.

The only cases where `finally` doesn't run: `System.exit(0)`, JVM crash, or power failure.

#### 3. `finalize()` — before garbage collection

An old method the GC called before destroying an object.

```java
protected void finalize() throws Throwable {
    System.out.println("Object is being destroyed");
}
```

Why it is **deprecated since Java 9**:

```
- No guarantee it will ever run
- No guarantee WHEN it will run
- Slows down garbage collection
- Can accidentally resurrect the object
```

Modern replacement:

```java
try (Connection con = getConnection()) {
    // use it
}   // auto-closed here — reliable ✅
```

#### Comparison table

| | final | finally | finalize() |
|---|---|---|---|
| Type | keyword | block | method |
| Used with | variable / method / class | try-catch | object cleanup |
| Purpose | prevent change | always execute | pre-GC cleanup |
| When it runs | compile time check | after try/catch | before GC (maybe never) |
| Status | actively used | actively used | deprecated (Java 9+) |

#### Interview-ready answer

> `final` is a keyword. A final variable cannot be reassigned, a final method cannot be overridden, and a final class cannot be extended — `String` is a good example of a final class. `finally` is a block used with try-catch that always executes, whether an exception occurs or not, so it is used to release resources like connections. `finalize()` was a method called by the garbage collector before destroying an object, but it is deprecated since Java 9 because there is no guarantee it runs or when it runs. Today we use try-with-resources or `Cleaner` instead. A common trap is that a final reference to a collection can still have its contents modified — only the reference is locked, not the object's state.

#### Easy memory

```
final    → KEYWORD → "cannot change"      → final int MAX = 10;
finally  → BLOCK   → "always runs"        → pay the restaurant bill
finalize → METHOD  → "before GC"          → deprecated, don't use

final List → add() ✅   |   list = new ✅❌ (not allowed)
```

---
## Core Java

### Q: JDK vs JRE vs JVM?

The easiest way to remember:

```
JDK → for DEVELOPERS  (write + compile + run)
JRE → for USERS       (only run)
JVM → the ENGINE      (actually executes)
```

They sit inside each other like boxes:

```
┌──────────────────────────────────────────┐
│ JDK  (Java Development Kit)              │
│  javac, javadoc, jar, debugger           │
│  ┌────────────────────────────────────┐  │
│  │ JRE (Java Runtime Environment)     │  │
│  │  library classes: String, List...  │  │
│  │  ┌──────────────────────────────┐  │  │
│  │  │ JVM (Java Virtual Machine)   │  │  │
│  │  │  class loader                │  │  │
│  │  │  memory areas                │  │  │
│  │  │  interpreter + JIT           │  │  │
│  │  └──────────────────────────────┘  │  │
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

Real-world idea — a **restaurant**:

```
JVM → the CHEF        (actually cooks the food)
JRE → the KITCHEN     (chef + gas + utensils + ingredients)
JDK → the FULL SETUP  (kitchen + recipe books + knives to create new dishes)
```

A customer only needs the kitchen to produce food. A chef who invents new recipes needs the full setup.

#### What each one does

**JVM** — runs the `.class` bytecode.

```
Loads the class → verifies bytecode → allocates memory → executes → garbage collects
```

**JRE** = JVM + the standard library (`java.lang`, `java.util`…). If you only want to *run* a Java app, JRE is enough.

**JDK** = JRE + development tools:

```
javac    → compiler
java     → launcher
jar      → packaging
javadoc  → documentation
jdb      → debugger
```

#### Practical check

```bash
javac Hello.java     # needs JDK  (compiler)
java Hello           # needs JRE  (runtime)
```

If you only installed the JRE, `javac` gives "command not found" — a very common real-life mistake.

#### Interview-ready answer

> The JVM is the engine that actually executes Java bytecode — it loads classes, verifies them, manages memory and performs garbage collection, and it is platform-specific. The JRE is the JVM plus the standard Java class libraries, and it is what you need to simply run a Java application. The JDK is the JRE plus development tools such as the `javac` compiler, `jar` and `javadoc`, and it is what a developer installs. So JDK contains JRE, and JRE contains JVM.

#### Easy memory

```
JDK = JRE + compiler tools     → developer
JRE = JVM + libraries          → user who only runs
JVM = the executing engine     → chef

JDK ⊃ JRE ⊃ JVM
```

---

### Q: How does Java achieve platform independence?

The easiest way to remember:

```
"Write Once, Run Anywhere"

Java code → BYTECODE (same everywhere) → JVM (different for each OS)
```

#### The full flow

```
   Hello.java          ← your source code (same everywhere)
        │
        │ javac  (compiler)
        ↓
   Hello.class         ← BYTECODE (same everywhere) ⭐
        │
        ├──────────────┬──────────────┐
        ↓              ↓              ↓
   JVM for        JVM for        JVM for
   Windows        Linux          Mac
        ↓              ↓              ↓
   machine code   machine code   machine code
```

The **bytecode is the same everywhere**. Only the JVM is different for each operating system.

#### Compare with C

```
C program
    ↓
compiled on Windows → Windows .exe → runs ONLY on Windows ❌

Java program
    ↓
compiled anywhere → .class bytecode → runs on ANY OS with a JVM ✅
```

Real-world idea: A **movie file** (`.mp4`) is the same everywhere. You just need the right **player** for your device — VLC on Windows, VLC on Mac. The file doesn't change; the player does.

#### The important nuance interviewers look for

```
Java LANGUAGE → platform INDEPENDENT ✅
JVM           → platform DEPENDENT   ✅ (you download a different JVM per OS)
```

So Java is platform independent *because* the JVM is platform dependent.

#### Interview-ready answer

> Java achieves platform independence through bytecode. The `javac` compiler does not produce machine code — it produces `.class` bytecode, which is identical on every platform. That bytecode is then executed by the JVM, and the JVM itself is platform-specific, so there is a separate JVM implementation for Windows, Linux and Mac. This is what "Write Once, Run Anywhere" means. So the Java program is platform independent precisely because the JVM is platform dependent.

#### Easy memory

```
.java → javac → .class BYTECODE (same everywhere) → JVM (different per OS) → runs

Movie file = same    |    Player = different per device
```

---

### Q: String vs StringBuilder vs StringBuffer?

The easiest way to remember:

```
String        → IMMUTABLE  → cannot change
StringBuilder → MUTABLE    → fast, NOT thread-safe   ⭐ use this normally
StringBuffer  → MUTABLE    → slow, thread-safe
```

#### The core problem with String

```java
String s = "Hello";
s = s + " World";
```

You think you changed `s`. You didn't — Java created a **brand new object**.

```
"Hello"        ← old object, now garbage
"Hello World"  ← new object
        ↑
        s now points here
```

Now imagine a loop:

```java
String s = "";
for (int i = 0; i < 10000; i++) {
    s = s + i;         // ❌ creates 10,000 objects!
}
```

```
Loop 1 → new object
Loop 2 → new object
Loop 3 → new object
...
Loop 10000 → new object

Memory: 💥   Speed: 🐢
```

With StringBuilder:

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 10000; i++) {
    sb.append(i);      // ✅ ONE object, modified in place
}
String result = sb.toString();
```

```
ONE object → appended 10,000 times

Memory: 🙂   Speed: 🚀
```

Real-world idea: **String** is like writing on stone — to fix a mistake you carve a whole new stone. **StringBuilder** is like writing on a whiteboard — you just keep adding.

#### StringBuilder vs StringBuffer

Both are mutable. The only difference is thread safety.

```java
// StringBuffer — every method is synchronized
public synchronized StringBuffer append(String s) { ... }

// StringBuilder — no synchronization
public StringBuilder append(String s) { ... }
```

```
StringBuffer   → LOCKS the object → safe with many threads → slower 🐢
StringBuilder  → no lock          → single thread only     → faster 🚀
```

Real-world idea:

```
StringBuffer  → a bank locker: one person at a time, safe but slow
StringBuilder → your own diary: only you use it, so no lock needed
```

99% of real code uses **StringBuilder** — string building usually happens inside one method, on one thread.

#### Comparison table

| | String | StringBuilder | StringBuffer |
|---|---|---|---|
| Mutable? | ❌ No | ✅ Yes | ✅ Yes |
| Thread-safe? | ✅ (because immutable) | ❌ No | ✅ Yes |
| Speed | slow in loops | **fastest** | slower |
| Since | 1.0 | Java 5 | 1.0 |
| Stored in | String pool (literals) | heap | heap |
| Use when | fixed text, keys, constants | building text in one thread | building text shared by threads |

#### Interview trap

```java
String a = "Hello";
String b = "Hello";
System.out.println(a == b);          // true  → same pooled object

String c = new String("Hello");
System.out.println(a == c);          // false → new object in heap
System.out.println(a.equals(c));     // true  → same content
```

#### Interview-ready answer

> `String` is immutable, so every modification creates a new object — concatenating inside a loop creates thousands of objects and hurts performance. `StringBuilder` and `StringBuffer` are mutable, so they modify the same object using an internal character array. The difference between them is that `StringBuffer`'s methods are synchronized, which makes it thread-safe but slower, while `StringBuilder` is not synchronized and is therefore faster. In practice we use `StringBuilder` for string building inside a single thread, `StringBuffer` only when the same buffer is shared across threads, and `String` for fixed values like constants and map keys.

#### Easy memory

```
String        → stone carving  → immutable → new object every change
StringBuilder → whiteboard     → mutable   → FAST   → single thread  ⭐
StringBuffer  → bank locker    → mutable   → SAFE   → multi thread

Loop + concatenation? → ALWAYS StringBuilder
```

---

### Q: Why are Strings immutable and what is the string pool?

The easiest way to remember:

```
Immutable = once created, the value can NEVER change
String pool = a special area in the heap that REUSES string literals
```

#### What is the String pool?

```java
String a = "Priya";
String b = "Priya";
String c = "Priya";
```

Java does **not** create three objects.

```
       STRING POOL (inside heap)
      ┌─────────────────────┐
      │     "Priya"         │  ← only ONE object
      └──────────△──────────┘
          ┌──────┼──────┐
          │      │      │
          a      b      c
```

```java
System.out.println(a == b);    // true — same object
```

But with `new`:

```java
String d = new String("Priya");
```

```
   STRING POOL              HEAP
  ┌───────────┐        ┌───────────┐
  │  "Priya"  │        │  "Priya"  │  ← separate NEW object
  └─────△─────┘        └─────△─────┘
        │                    │
      a, b, c                d
```

```java
System.out.println(a == d);        // false — different objects
System.out.println(a.equals(d));   // true  — same content
```

You can push it into the pool manually:

```java
String e = d.intern();
System.out.println(a == e);        // true
```

#### Why must String be immutable? (4 solid reasons)

**Reason 1 — the pool would be dangerous otherwise**

Because `a`, `b` and `c` share one object, if `b` could change the value, `a` and `c` would silently change too:

```java
b = "Ravi";       // if String were mutable...
System.out.println(a);   // ...a would also become "Ravi" 💥
```

Immutability makes sharing safe.

**Reason 2 — security ⭐ (say this one in the interview)**

```java
void connect(String url) {
    checkPermission(url);       // security check passes
    openConnection(url);        // if String were mutable, someone could
}                               // change url between these two lines!
```

That is exactly why usernames, URLs, file paths and class names are Strings.

**Reason 3 — HashMap keys stay valid**

```java
map.put(name, "Developer");
```

The key's `hashCode` is calculated once. If the String could change, the hash would change, and the entry would be lost forever in the wrong bucket.

```
"Priya" → hash 500 → bucket 500 → stored

if it changed to "Ravi" → hash 900
     ↓
map.get() looks in bucket 900 → NOT FOUND 💥
```

That is why String is the most common HashMap key — its hash never changes.

**Reason 4 — thread safety for free**

Nothing can change, so many threads can share one String with no locks.

#### Interview trap

```java
String s = "Hello";
s.concat(" World");
System.out.println(s);        // Hello   ← NOT "Hello World"!
```

`concat` returns a **new** String. You must assign it:

```java
s = s.concat(" World");
System.out.println(s);        // Hello World ✅
```

Same trap with `replace()`, `toUpperCase()`, `trim()` — they all return new Strings.

#### Interview-ready answer

> Strings are immutable in Java, meaning once a String object is created its value can never be changed — any modification method like `concat` or `toUpperCase` returns a new String. The string pool is a special area in the heap where string literals are stored and reused, so `String a = "Priya"` and `String b = "Priya"` point to the same object, which saves a lot of memory. Immutability is required for this pooling to be safe, and it also gives security — a String used for a URL or file path cannot be modified after a security check. It also keeps HashMap keys valid because the hash code never changes, and it makes Strings thread-safe by default. When we use `new String("Priya")`, a separate object is created in the heap outside the pool, and `intern()` can push it into the pool.

#### Easy memory

```
Literal "Priya"      → String POOL  → reused → == is true
new String("Priya")  → HEAP object  → new    → == is false

Immutable because:
1. Pool sharing must be safe
2. SECURITY (url can't change after the check)  ⭐
3. HashMap key hash stays valid
4. Thread-safe for free

Trap: s.concat(" World");  ← does nothing unless you assign it back
```

---

### Q: `==` vs `equals()`?

The easiest way to remember:

```
==       → compares the ADDRESS  (are they the SAME object?)
equals() → compares the CONTENT  (do they LOOK the same?)
```

#### Real-world idea — twins

```
Two identical twins:

==        → "Are these the same person?"   → NO  (two people)
equals()  → "Do they look the same?"       → YES (same face)
```

#### Simple example

```java
String a = new String("Priya");
String b = new String("Priya");

System.out.println(a == b);        // false
System.out.println(a.equals(b));   // true
```

```
a ──→ ┌─────────┐
      │ "Priya" │   address: 100
      └─────────┘

b ──→ ┌─────────┐
      │ "Priya" │   address: 200
      └─────────┘

==       compares 100 vs 200 → false
equals() compares "Priya" vs "Priya" → true
```

#### For primitives, `==` is correct

```java
int a = 10;
int b = 10;
System.out.println(a == b);        // true ✅ — primitives have no address
```

Primitives store the actual value, so `==` compares values. There is no `equals()` for primitives.

```
Primitive → use ==
Object    → use equals()
```

#### The default `equals()` trap ⭐

```java
class Employee {
    int id;
    Employee(int id) { this.id = id; }
}

Employee e1 = new Employee(101);
Employee e2 = new Employee(101);

System.out.println(e1.equals(e2));      // false ❗
```

Why false? Because `Object`'s default `equals()` is literally:

```java
public boolean equals(Object obj) {
    return (this == obj);        // it just does ==
}
```

So unless **you** override `equals()`, it behaves exactly like `==`.

Fix it:

```java
class Employee {

    int id;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Employee other)) return false;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

Now `e1.equals(e2)` is `true`. ✅

`String` already overrides `equals()` — that is why `"a".equals("a")` works out of the box.

#### The Integer cache trap ⭐⭐

```java
Integer a = 100, b = 100;
System.out.println(a == b);      // true

Integer c = 200, d = 200;
System.out.println(c == d);      // false ❗
```

Java caches Integer objects from **-128 to 127**. Inside that range the same object is reused; outside it, new objects are created.

```
-128 ────────── 127   → cached → == true
128 and above         → new    → == false
```

So never use `==` on wrapper objects. Use `.equals()` or `Objects.equals()`.

#### Null safety

```java
String s = null;

s.equals("Priya");         // ❌ NullPointerException
"Priya".equals(s);         // ✅ false  — constant first
Objects.equals(s, "Priya");// ✅ false  — safest
```

#### Interview-ready answer

> `==` compares references, meaning it checks whether two variables point to the same object in memory, and for primitives it compares the actual values. `equals()` compares content, but only if the class overrides it — the default implementation in `Object` simply does a `==` check, which is why two `Employee` objects with the same id return false unless we override `equals()`. Classes like `String` and the wrapper classes already override `equals()`. A common trap is comparing `Integer` objects with `==`: values from -128 to 127 come from the Integer cache and compare as equal, while larger values do not. So the rule is: use `==` for primitives and `equals()` for objects, preferably `Objects.equals()` for null safety.

#### Easy memory

```
==       → same OBJECT?   → address   → twins: same person?
equals() → same CONTENT?  → value     → twins: same face?

primitive → ==
object    → equals()

Default equals() in Object == ==   → must OVERRIDE it
Integer cache: -128 to 127 → == true (trap!)
"literal".equals(var)  → null-safe
```

---

### Q: Explain the equals() and hashCode() contract.

The main rule you need to remember is:

```
If two objects are EQUAL by equals(), they MUST have the SAME hashCode.

But the same hashCode does NOT mean the objects are equal.
```

#### Simple example

```java
Employee e1 = new Employee(101, "Priya");
Employee e2 = new Employee(101, "Priya");
```

They are two different objects in memory:

```
e1 ──→ Employee(101, "Priya")
e2 ──→ Employee(101, "Priya")
```

But logically we consider them equal, because the id and name match.

```java
e1.equals(e2);      // true
```

And if `equals()` says true, the hash codes must match:

```java
e1.hashCode() == e2.hashCode();     // MUST be true
```

#### Why do we need both? (HashMap is the answer)

```java
Map<Employee, String> map = new HashMap<>();
map.put(e1, "Developer");
```

What HashMap does when storing:

```
e1
 ↓
hashCode()
 ↓
find the BUCKET
 ↓
store e1 there
```

What it does when reading:

```java
map.get(e2);
```

```
e2
 ↓
hashCode()      ← STEP 1: WHERE to look
 ↓
find the bucket
 ↓
equals()        ← STEP 2: WHICH entry is the match
 ↓
return the value
```

So the easy way to remember is:

```
hashCode() → WHERE to search   (which bucket / which street)
equals()   → WHICH one matches (which house on that street)
```

Real-world idea: a **postal address**.

```
PIN code    → tells the postman which AREA        → hashCode()
House name  → tells him WHICH house in that area  → equals()
```

Many houses share a PIN code (collision), but the house name identifies the exact one.

#### What happens if I override only equals()? 💥

```java
e1.equals(e2);      // true

// but hashCode() was not overridden:
e1.hashCode();      // 100
e2.hashCode();      // 500
```

```
HashMap

Bucket 100
   ↓
   e1  ← the entry actually lives here

Bucket 500
   ↓
   HashMap searches here using e2 → EMPTY → returns null 💥
```

The postman goes to the wrong PIN code, so he never finds the house — even though the house name is correct.

**This is why: if you override `equals()`, you MUST override `hashCode()`.**

#### Correct implementation

```java
class Employee {

    private int id;
    private String name;

    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;                       // same object

        if (!(obj instanceof Employee other)) return false; // null + type check

        return id == other.id
            && Objects.equals(name, other.name);            // compare fields
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);                      // SAME fields
    }
}
```

The golden rule is that both methods must use the **same fields**:

```
equals()    → id + name
hashCode()  → id + name      ← must match
```

#### The contract rules

For `equals()`:

```
Reflexive   → a.equals(a) is true
Symmetric   → a.equals(b) true  ⇒  b.equals(a) true
Transitive  → a=b and b=c  ⇒  a=c
Consistent  → same data → same result every time
Null        → a.equals(null) is always false
```

For `hashCode()`:

```
a.equals(b) == true
        ↓
a.hashCode() == b.hashCode()      MUST be true ✅

Same hashCode
        ↓
equals() true?                    NOT guaranteed ❌  (hash collision)
```

A hash collision simply means two different objects landed in the same bucket. That is normal and HashMap handles it using `equals()`.

#### Practical warning — mutable keys

```java
Employee e = new Employee(101, "Priya");
map.put(e, "Developer");

e.setId(102);              // hash changed!

map.get(e);                // null 💥 — it's in the OLD bucket
```

This is why immutable objects (especially `String`) make the best map keys.

#### Interview-ready answer

> The contract states that if two objects are equal according to `equals()`, they must return the same hash code. However, two objects having the same hash code do not have to be equal, because hash collisions are possible. This matters for `HashMap` and `HashSet`: `hashCode()` decides which bucket to search, and `equals()` identifies the exact matching entry within that bucket. If we override `equals()` without overriding `hashCode()`, two logically equal objects can produce different hash codes, so `HashMap` looks in the wrong bucket and fails to find the entry. Therefore whenever we override `equals()`, we must also override `hashCode()` using the same fields. We should also avoid using mutable objects as keys, because changing a field changes the hash code and the entry becomes unreachable.

#### Easy memory

```
hashCode() → WHERE?  → find the bucket  → PIN code
equals()   → WHICH?  → find the object  → house name

Golden rule:
equals() TRUE  →  hashCode SAME  ✅ (guaranteed)
hashCode SAME  →  equals() TRUE? ❌ (not guaranteed — collision)

Override equals() → ALWAYS override hashCode()
Use the SAME fields in both.
```

---

### Q: What is autoboxing and unboxing?

The easiest way to remember:

```
Autoboxing = Primitive  →  Wrapper Object
Unboxing   = Wrapper Object  →  Primitive
```

#### First — what are wrapper classes?

| Primitive | Wrapper class |
|---|---|
| `int` | `Integer` |
| `long` | `Long` |
| `double` | `Double` |
| `float` | `Float` |
| `boolean` | `Boolean` |
| `char` | `Character` |
| `byte` | `Byte` |
| `short` | `Short` |

```java
int number  = 10;        // primitive — just a value
Integer obj = 10;        // wrapper object — a real object
```

#### 1. Autoboxing

Java automatically converts a primitive into its wrapper object.

```java
int a = 10;
Integer b = a;                     // autoboxing
```

Behind the scenes Java writes:

```java
Integer b = Integer.valueOf(a);
```

```
int
 ↓
10
 ↓
AUTOBOXING
 ↓
Integer object
```

#### Where you see it every day — Collections

```java
List<int> numbers;                       // ❌ not allowed
List<Integer> numbers = new ArrayList<>();  // ✅
```

Collections can only store **objects**, not primitives. So:

```java
numbers.add(10);
```

```
10 → int
     ↓
 AUTOBOXING
     ↓
Integer object
     ↓
stored in ArrayList
```

#### 2. Unboxing

The opposite direction.

```java
Integer a = 10;
int b = a;                    // unboxing
```

Behind the scenes:

```java
int b = a.intValue();
```

```
Integer
   ↓
  10
   ↓
UNBOXING
   ↓
 int
```

#### Both together

```java
int a = 100;

Integer b = a;      // AUTOBOXING
int c = b;          // UNBOXING
```

```
int a = 100
     ↓
 AUTOBOXING
     ↓
Integer b = 100
     ↓
 UNBOXING
     ↓
int c = 100
```

Real-world idea: a **gift box**.

```
A chocolate (primitive)
     ↓ put it in a gift box
Boxed chocolate (wrapper)     ← autoboxing
     ↓ open the box
Chocolate again (primitive)   ← unboxing
```

Why box it at all? Because the "courier service" (collections, generics) only accepts boxed items.

#### Interview problem 1 — NullPointerException ⭐

```java
Integer number = null;
int value = number;         // 💥 NullPointerException
```

Why? Unboxing internally calls:

```java
number.intValue();
```

but `number` is `null`, so it is effectively:

```java
null.intValue();      // ❌
```

Real-life version of this bug:

```java
Integer count = map.get("missing-key");   // returns null
int total = count + 1;                    // 💥 NPE
```

Always null-check before unboxing:

```java
int total = (count != null) ? count + 1 : 1;   // ✅
```

#### Interview problem 2 — the `==` cache trap ⭐

```java
Integer a = 100, b = 100;
System.out.println(a == b);      // true

Integer a2 = 200, b2 = 200;
System.out.println(a2 == b2);    // false
```

`Integer.valueOf()` uses a cache guaranteed for **-128 to 127**, so those values reuse the same object.

```
-128 ─────────── 127     → same cached object → == true
outside that range       → brand new objects  → == false
```

Never compare wrapper values with `==`:

```java
a.equals(b);                // ✅
Objects.equals(a, b);       // ✅ null-safe
```

#### Performance note

```java
Long sum = 0L;                       // ❌ wrapper in a loop
for (long i = 0; i < 1_000_000; i++) {
    sum += i;                        // box + unbox one MILLION times
}
```

Use the primitive `long` instead — this is a classic performance bug.

#### Interview-ready answer

> Autoboxing is the automatic conversion of a primitive into its corresponding wrapper class, for example `int` to `Integer`, and internally it calls `Integer.valueOf()`. Unboxing is the reverse conversion from a wrapper object back to a primitive, and it internally calls `intValue()`. Java performs these conversions automatically, and we see them constantly when working with collections, because collections store objects and not primitives. The two things to be careful about are unboxing a null wrapper, which throws a NullPointerException, and comparing wrappers with `==`, because only values from -128 to 127 come from the Integer cache. We should also avoid wrapper types inside hot loops because the repeated boxing hurts performance.

#### Easy memory

```
Primitive → AUTOBOXING → Wrapper Object     (chocolate → gift box)
Wrapper   → UNBOXING   → Primitive          (gift box → chocolate)

int a = 10;
Integer b = a;   // Autoboxing
int c = b;       // Unboxing

⚠️ Integer x = null; int y = x;  → NullPointerException
⚠️ Integer == Integer            → only -128..127 is true
```

---

### Q: Is Java pass-by-value or pass-by-reference?

**Java is ALWAYS pass-by-value.** ✅

This is a favourite interview question because objects make Java *look* like pass-by-reference, but technically it is not.

The easiest way to remember:

```
Primitive → a copy of the VALUE is passed
Object    → a copy of the REFERENCE is passed
```

#### 1. Primitive example

```java
public class Test {

    static void change(int x) {
        x = 50;
    }

    public static void main(String[] args) {
        int a = 10;
        change(a);
        System.out.println(a);
    }
}
```

Output:

```
10
```

Why?

```
main()
a = 10
  │
  │ copy the value
  ↓
change()
x = 10   →  x = 50   (only the copy changed)

a is still 10
```

#### 2. What happens with objects?

```java
class Employee {
    String name;
}

static void changeName(Employee emp) {
    emp.name = "Ravi";
}

public static void main(String[] args) {
    Employee e = new Employee();
    e.name = "Priya";

    changeName(e);
    System.out.println(e.name);
}
```

Output:

```
Ravi
```

You might think: "The original object changed, so Java must be pass-by-reference." **No — it is still pass-by-value.**

What actually happened: Java copied the **reference value**, so both variables point to the same object.

```
e ────────────┐
              ↓
         ┌──────────────┐
         │ Employee     │
         │ name="Priya" │
         └──────────────┘
              ↑
emp ──────────┘
```

So `emp.name = "Ravi"` modifies that one shared object:

```
e ────────────┐
              ↓
         ┌─────────────┐
         │ Employee    │
         │ name="Ravi" │
         └─────────────┘
              ↑
emp ──────────┘
```

That is why `e.name` becomes `"Ravi"`.

#### 3. The proof that Java is pass-by-value ⭐

Change the **reference itself** inside the method:

```java
static void changeEmployee(Employee emp) {
    emp = new Employee();          // point the COPY somewhere else
    emp.name = "Ravi";
}

public static void main(String[] args) {
    Employee e = new Employee();
    e.name = "Priya";

    changeEmployee(e);
    System.out.println(e.name);
}
```

Output:

```
Priya
```

Before:

```
e ──────┐
        ↓
   Employee
   "Priya"
        ↑
emp ────┘
```

After `emp = new Employee()`:

```
e ─────────→ Employee
             "Priya"      ← original untouched

emp ───────→ Employee
             "Ravi"       ← only the copy moved
```

If Java were pass-by-reference, `e` would now point to the new object and print "Ravi". It prints "Priya", which proves only a **copy** of the reference was passed.

#### Real-world idea — a house key

```
You have a house key.
You give your FRIEND A DUPLICATE key (a copy of the reference).

Friend paints the house           → you see the change ✅ (same house)
Friend throws away his key and
buys a different house            → YOUR house is unchanged ✅
```

#### The two cases to remember

```
1. MODIFY the object          emp.name = "Ravi";
   → caller sees the change ✅

2. REASSIGN the parameter     emp = new Employee();
   → caller sees nothing    ❌
```

#### Interview trap

If the interviewer asks: *"Are objects passed by reference in Java?"* — do not say yes.

Say:

> No. Java is always pass-by-value. When passing an object, Java passes a copy of the object's reference value. Therefore we can modify the object through that copied reference, but reassigning the parameter does not change the caller's reference.

#### Interview-ready answer

> Java is always pass-by-value. For primitive types, a copy of the value is passed, so changes inside the method do not affect the caller. For objects, a copy of the reference value is passed. Because the original reference and the copied reference point to the same object, changes to the object's state are visible to the caller — for example setting `emp.name`. However, if we reassign the parameter to a new object inside the method, the caller's reference is unaffected, which proves that Java passed a copy of the reference rather than the reference itself.

#### Easy memory

```
PRIMITIVE
int a = 10  →  copy of 10  →  method

OBJECT
Employee e ──→ Object
      │ copy of the reference
      ↓
    emp ─────→ SAME Object

Modify the object   → caller sees it     ✅
Reassign the param  → caller sees nothing ❌

One line: "Java is always pass-by-value; for objects, the value copied is the reference."
```

---

### Q: What are wrapper classes and why are they needed?

The easiest way to remember:

```
Wrapper class = an OBJECT version of a primitive
Reason        = Java collections and generics only work with OBJECTS
```

#### The 8 wrappers

```
byte    → Byte
short   → Short
int     → Integer      ⭐
long    → Long
float   → Float
double  → Double
char    → Character
boolean → Boolean
```

#### Why do we need them? (4 reasons)

**1. Collections only store objects**

```java
List<int> list;                        // ❌ not allowed
List<Integer> list = new ArrayList<>();// ✅
```

**2. A primitive cannot be null**

```java
int    age1 = null;     // ❌ compile error
Integer age2 = null;    // ✅ allowed
```

This matters hugely in databases:

```
DB column `age` is NULL
      ↓
int    → cannot represent it (0 would be a WRONG value)
Integer → null means "not provided" ✅
```

That is why JPA entities use `Integer`, `Long`, `Double` — not primitives.

```java
@Entity
class Employee {
    @Id
    private Long id;          // ✅ null before saving
    private Integer age;      // ✅ null allowed
}
```

If `id` were `long`, a new unsaved entity would have id `0` instead of "no id yet".

**3. Useful helper methods**

```java
Integer.parseInt("100");        // String → int
Integer.MAX_VALUE;              // 2147483647
Integer.MIN_VALUE;
Integer.toBinaryString(10);     // "1010"
Integer.valueOf("100");
Double.parseDouble("10.5");
Boolean.parseBoolean("true");
Character.isDigit('5');
```

A primitive `int` has no methods at all — `10.parseInt()` is meaningless.

**4. Generics need objects**

```java
Map<String, Integer> scores = new HashMap<>();   // ✅
Map<String, int> scores;                         // ❌
```

#### Primitive vs wrapper

| | Primitive | Wrapper |
|---|---|---|
| Stores | actual value | object reference |
| Memory | stack (as a local) | heap |
| Default value | `0`, `false` | `null` |
| Can be null | ❌ | ✅ |
| Size | small (4 bytes for int) | larger (object header + value) |
| Methods | none | many |
| Used in collections | ❌ | ✅ |
| Speed | faster | slower |

#### When to use which

```
Use PRIMITIVE  → loop counters, calculations, performance-critical code
Use WRAPPER    → collections, generics, JPA entities, DTO fields that can be null
```

#### Interview trap

```java
Integer count = null;
if (count > 0) { }        // 💥 NullPointerException (unboxing null)
```

Always null-check wrapper fields coming from a database or an API.

#### Interview-ready answer

> Wrapper classes are the object representations of the eight primitive types — `Integer` for `int`, `Double` for `double` and so on. They are needed because Java collections and generics only work with objects, so we cannot write `List<int>`. They also allow null, which is important when mapping database columns or API fields that may be missing — that is why JPA entities use `Integer` and `Long` rather than primitives. In addition, wrappers provide useful utility methods like `Integer.parseInt()` and constants like `Integer.MAX_VALUE`. Java converts between them automatically through autoboxing and unboxing, but we must be careful with null wrappers because unboxing null throws a NullPointerException.

#### Easy memory

```
Wrapper = primitive wearing an OBJECT costume

Needed for: 1. Collections/Generics  (List<Integer>)
            2. null support          (DB / JPA)
            3. Utility methods       (Integer.parseInt)

int → fast, cannot be null
Integer → object, CAN be null → NPE risk when unboxing
```

---
## Collections

### Q: Describe the Collections framework hierarchy.

The easiest way to remember:

```
Collection  → stores SINGLE values     (List, Set, Queue)
Map         → stores KEY–VALUE pairs   (NOT part of Collection!)
```

#### The full picture

```
                    Iterable  (interface)
                        │
                    Collection
                        │
        ┌───────────────┼───────────────┐
        ↓               ↓               ↓
      List            Set             Queue
        │               │               │
   ┌────┼────┐     ┌────┼─────┐    ┌────┴─────┐
   ↓    ↓    ↓     ↓    ↓     ↓    ↓          ↓
Array Linked Vector Hash Linked Tree Priority  Deque
List  List         Set  HashSet Set  Queue      │
                                            ArrayDeque


                     Map   (SEPARATE hierarchy)
                      │
        ┌─────────────┼─────────────┬────────────┐
        ↓             ↓             ↓            ↓
    HashMap     LinkedHashMap   TreeMap      Hashtable
        │
   ConcurrentHashMap
```

⭐ **Map does NOT extend Collection.** This is a favourite interview question. The reason is simple: `Collection` methods like `add(E e)` take a single element, but a Map needs `put(K key, V value)` — two values. The shapes don't match, so Map was kept separate.

#### What each one is for

```
List  → ORDERED, allows DUPLICATES, index access
        → shopping cart (2 kg rice twice is fine)

Set   → NO duplicates
        → Aadhaar numbers, email IDs

Queue → FIFO processing
        → ticket counter line

Map   → key → value lookup
        → dictionary: word → meaning
```

#### Quick code

```java
List<String> list = new ArrayList<>();
list.add("Apple");
list.add("Apple");          // ✅ duplicate allowed
System.out.println(list);   // [Apple, Apple]

Set<String> set = new HashSet<>();
set.add("Apple");
set.add("Apple");           // ignored
System.out.println(set);    // [Apple]

Map<String, Integer> map = new HashMap<>();
map.put("Apple", 100);
map.put("Apple", 200);      // replaces the old value
System.out.println(map);    // {Apple=200}

Queue<String> q = new LinkedList<>();
q.offer("A"); q.offer("B");
System.out.println(q.poll());  // A  (first in, first out)
```

#### Interview-ready answer

> The Collections framework starts with the `Iterable` interface, which `Collection` extends. `Collection` has three main branches — `List` for ordered collections that allow duplicates, `Set` for unique elements, and `Queue` for FIFO processing. Common implementations are `ArrayList` and `LinkedList` for List, `HashSet`, `LinkedHashSet` and `TreeSet` for Set, and `PriorityQueue` and `ArrayDeque` for Queue. `Map` is a separate hierarchy and does not extend `Collection`, because a Map stores key-value pairs while `Collection` is designed around single elements. `HashMap`, `LinkedHashMap`, `TreeMap` and `ConcurrentHashMap` are the main Map implementations.

#### Easy memory

```
Iterable → Collection → List / Set / Queue
Map      → SEPARATE (put needs TWO values, add needs ONE)

List  → duplicates OK, ordered   → shopping cart
Set   → unique only              → Aadhaar numbers
Queue → FIFO                     → ticket line
Map   → key → value              → dictionary
```

---

### Q: List vs Set vs Map?

The easiest way to remember with one real-world example — a **classroom**:

```
List → attendance sheet in roll-number order (order matters, a name can repeat)
Set  → the set of unique subjects offered   (no repeats)
Map  → roll number → student name          (lookup by key)
```

#### Comparison

| | List | Set | Map |
|---|---|---|---|
| Duplicates | ✅ allowed | ❌ not allowed | keys ❌, values ✅ |
| Order | ✅ insertion order | depends on type | depends on type |
| Index access | ✅ `get(0)` | ❌ | ❌ (get by key) |
| Null | many nulls OK | one null (HashSet) | one null key (HashMap) |
| Main use | ordered items | uniqueness | key-based lookup |

#### Code showing the difference clearly

```java
// LIST — keeps everything, in order
List<String> list = new ArrayList<>();
list.add("Priya");
list.add("Ravi");
list.add("Priya");
System.out.println(list);        // [Priya, Ravi, Priya]
System.out.println(list.get(0)); // Priya  (index access)

// SET — silently drops duplicates
Set<String> set = new HashSet<>();
set.add("Priya");
set.add("Ravi");
set.add("Priya");                 // ignored
System.out.println(set);          // [Priya, Ravi]
System.out.println(set.size());   // 2

// MAP — key is unique, value can repeat
Map<Integer, String> map = new HashMap<>();
map.put(101, "Priya");
map.put(102, "Ravi");
map.put(101, "Meena");            // REPLACES Priya
System.out.println(map);          // {101=Meena, 102=Ravi}
System.out.println(map.get(101)); // Meena
```

#### The classic real use — removing duplicates

```java
List<String> names = List.of("Priya", "Ravi", "Priya", "Meena");

Set<String> unique = new HashSet<>(names);
System.out.println(unique);        // [Priya, Ravi, Meena]
```

One line, duplicates gone.

#### Which one do I pick?

```
Do I need to keep the ORDER and allow repeats?  → List
Do I only care about UNIQUE values?             → Set
Do I need to look something up BY A KEY?        → Map
```

#### Interview-ready answer

> A `List` is an ordered collection that allows duplicates and supports index-based access, so we use it when the order matters — for example items in a shopping cart. A `Set` does not allow duplicates and is used when we care only about uniqueness, such as a set of email IDs; it also has no index access. A `Map` stores key-value pairs where keys must be unique but values can repeat, and it is used for lookups such as employee id to employee name. Internally `HashSet` is backed by a `HashMap`, and `Map` is not part of the `Collection` hierarchy.

#### Easy memory

```
List → [Priya, Ravi, Priya]     → order + duplicates → get(0)
Set  → [Priya, Ravi]            → unique only        → no index
Map  → {101=Priya, 102=Ravi}    → key → value        → get(101)

Remove duplicates instantly: new HashSet<>(list)
```

---

### Q: ArrayList vs LinkedList?

The easiest way to remember:

```
ArrayList  → an ARRAY   → FAST to READ    (get by index)
LinkedList → a CHAIN    → FAST to INSERT/DELETE in the middle
```

#### How they store data

**ArrayList** — one continuous block, like seats in a cinema hall:

```
index:   0        1        2        3
      ┌────────┬────────┬────────┬────────┐
      │ Priya  │ Ravi   │ Meena  │ Arun   │
      └────────┴────────┴────────┴────────┘

get(2) → jump straight to seat 2 → O(1) ⚡
```

**LinkedList** — separate nodes joined by links, like a train:

```
┌───────┐    ┌───────┐    ┌───────┐    ┌───────┐
│ Priya │───→│ Ravi  │───→│ Meena │───→│ Arun  │
└───────┘←───└───────┘←───└───────┘←───└───────┘

get(2) → walk: Priya → Ravi → Meena → O(n) 🐢
```

#### Insertion in the middle

**ArrayList** must shift everything to the right:

```
Insert "Kumar" at index 1

Before: [Priya][Ravi][Meena][Arun]
                 ← ← ←  shift all of them
After:  [Priya][Kumar][Ravi][Meena][Arun]

Cost: O(n) 🐢
```

Real-world idea: in a cinema hall, inserting one person in the middle of a row means everyone slides over.

**LinkedList** just rewires two links:

```
Priya ───→ Ravi

           Kumar

Priya ───→ Kumar ───→ Ravi     (only 2 links changed)

Cost: O(1) once you're at the position ⚡
```

Real-world idea: adding a coach to a train — you just unhook and rehook.

#### ArrayList resizing

```java
List<String> list = new ArrayList<>();   // capacity 10
```

When it fills up:

```
Old array (10) FULL
      ↓
create a NEW array of 15  (roughly 1.5×)
      ↓
copy all elements
      ↓
old array becomes garbage
```

If you know the size in advance, avoid the copies:

```java
new ArrayList<>(1000);      // ✅ pre-sized
```

#### Big-O table

| Operation | ArrayList | LinkedList |
|---|---|---|
| `get(index)` | **O(1)** ⚡ | O(n) 🐢 |
| `add()` at end | O(1) amortised | O(1) |
| `add()` at middle | O(n) (shifting) | O(1)* |
| `remove()` at middle | O(n) (shifting) | O(1)* |
| Memory per element | low | higher (2 extra pointers) |

*after you have reached the node — reaching it is O(n).

#### The honest real-world answer ⭐

> In practice, **ArrayList wins almost always.**

Even for middle insertion, `LinkedList` must first *walk* to the position, which is O(n), and ArrayList's shifting uses `System.arraycopy()`, which is extremely fast at the CPU level. ArrayList is also cache-friendly because its data sits together in memory.

```
Use ArrayList  → 95% of the time (default choice)
Use LinkedList → when you need a Queue/Deque
                 (addFirst / removeFirst) — heavy work at the ENDS
```

#### Interview-ready answer

> `ArrayList` is backed by a dynamic array, so index-based access is O(1), but inserting or removing in the middle is O(n) because elements have to be shifted, and it also resizes by creating a larger array and copying. `LinkedList` is a doubly-linked list, so adding or removing at a known position is O(1) since we only rewire pointers, but accessing an element by index is O(n) because it must traverse from the start, and each node needs extra memory for two pointers. In practice I use `ArrayList` as the default because reads dominate in most applications and it is cache-friendly; I use `LinkedList` mainly when I need `Deque` or queue behaviour with frequent additions and removals at the ends.

#### Easy memory

```
ArrayList  → cinema seats → get(2) instantly ⚡ → shifting is costly
LinkedList → train coaches → walk to find 🐢   → rewiring is cheap

Read a lot?              → ArrayList  (default ⭐)
Add/remove at the ends?  → LinkedList (as a Deque)
```

---

### Q: How does HashMap work internally?

The easiest way to remember:

```
put() → hashCode() decides the BUCKET → equals() checks for a duplicate key
get() → hashCode() finds the BUCKET   → equals() finds the exact entry
```

> A much deeper version of this topic (with resizing, treeify, load factor and Java 7 vs 8 differences) is in **[31-hashmap-internals.md](31-hashmap-internals.md)**.

#### The structure

A HashMap is an **array of buckets**. Each bucket holds a linked list (or a tree) of entries.

```
       Node[] table          (default size 16)

index 0  → null
index 1  → null
index 2  → [ "Priya" | 500 ] → null
index 3  → null
...
index 5  → [ "Ravi" | 700 ] → [ "Kumar" | 800 ] → null
                                    ↑
                            two keys landed in the
                            same bucket = COLLISION
...
index 15 → null
```

#### What happens on `put("Priya", 500)`

```
1. hash = key.hashCode()                     → e.g. 78453
2. spread the bits: hash ^ (hash >>> 16)     → better distribution
3. index = hash & (n - 1)                    → same as hash % 16, but faster
4. Is that bucket empty?
      YES → store the node ✅
      NO  → collision:
              compare keys with equals()
                 same key    → REPLACE the value
                 different   → append to the list/tree
```

Why `hash & (n-1)` instead of `%`? Because the table size is always a power of 2, so bitwise AND gives the same result and is much faster.

#### What happens on `get("Priya")`

```
"Priya"
   ↓
hashCode()  → find the bucket        ← STEP 1: WHERE
   ↓
walk that bucket
   ↓
equals()    → find the exact key     ← STEP 2: WHICH
   ↓
return 500
```

Real-world idea — a **library**:

```
Book's subject → tells you which SHELF   → hashCode()
Book's title   → finds the exact BOOK    → equals()
```

#### Collision handling — Java 8 improvement ⭐

```
Java 7 : collisions → LINKED LIST only        → worst case O(n) 🐢
Java 8 : collisions → linked list, and once a
         bucket has MORE THAN 8 entries and the
         table is ≥ 64, it converts to a
         RED-BLACK TREE                        → worst case O(log n) ⚡
```

```
Bucket 5:  A → B → C → D → E → F → G → H → I      (list, 9 entries)
                       ↓ treeify
                     balanced tree → O(log n)
```

If the bucket shrinks back to 6 entries, it un-trees back to a list.

#### Load factor and resizing

```
Default capacity     = 16
Default load factor  = 0.75

Resize threshold = 16 × 0.75 = 12 entries
```

When the 13th entry arrives:

```
capacity 16 → 32  (doubles)
      ↓
ALL entries are REHASHED into the new table
```

Resizing is expensive, so if you know the size in advance:

```java
new HashMap<>(1000);     // avoids repeated resizing ✅
```

Why 0.75? It is the balance point — a smaller value wastes memory, a larger value causes too many collisions.

#### Time complexity

```
Best / average case → O(1)      ⚡ (good hashCode, few collisions)
Worst case Java 7   → O(n)      🐢 (all keys in one bucket)
Worst case Java 8   → O(log n)  🙂 (treeified bucket)
```

#### Interview traps

**1. Mutable key**

```java
Employee e = new Employee(101);
map.put(e, "Developer");

e.setId(102);          // hash changed
map.get(e);            // null 💥 — the entry is in the old bucket
```

Use immutable keys — this is why `String` is the most common key.

**2. Null key**

```java
map.put(null, "value");   // ✅ allowed — always stored in bucket 0
```

HashMap allows **one** null key and many null values. `Hashtable` and `ConcurrentHashMap` allow none.

**3. Not thread-safe**

Two threads resizing at the same time can corrupt the map (in Java 7 it could even cause an infinite loop). Use `ConcurrentHashMap` for concurrent access.

#### Interview-ready answer

> Internally a `HashMap` is an array of buckets, default size 16. When we call `put`, it takes the key's `hashCode()`, applies a spreading function to mix the high bits, and computes the bucket index using `hash & (n-1)`. If the bucket is empty the entry is stored directly; if not, it is a collision, and it uses `equals()` to check whether the same key already exists — if yes it replaces the value, otherwise it appends the entry to that bucket. In Java 8, once a bucket has more than 8 entries and the table size is at least 64, the bucket converts from a linked list to a red-black tree, improving the worst case from O(n) to O(log n). The default load factor is 0.75, so at 12 entries the table doubles to 32 and everything is rehashed. Lookups are O(1) on average. Keys should be immutable, because changing a field after insertion changes the hash code and makes the entry unreachable.

#### Easy memory

```
Array of buckets (16) + load factor 0.75 → resize at 12 → double to 32

put: hashCode → bucket → equals → replace or append
get: hashCode → WHERE  → equals → WHICH

Library: subject = shelf (hashCode) | title = book (equals)

Java 8: bucket > 8 entries (table ≥ 64) → red-black TREE → O(log n)
One null key allowed. Not thread-safe → use ConcurrentHashMap.
```

---

### Q: HashMap vs Hashtable vs ConcurrentHashMap?

The easiest way to remember:

```
HashMap           → FAST,  not thread-safe        → single thread ⭐
Hashtable         → SLOW,  locks the WHOLE map    → legacy, don't use
ConcurrentHashMap → FAST + thread-safe            → multi-thread ⭐
```

#### The locking difference — this is the whole answer

**Hashtable** locks the entire map:

```
Thread 1 wants bucket 2  ──┐
Thread 2 wants bucket 9  ──┤ ALL wait for ONE lock
Thread 3 wants bucket 14 ──┘

        ┌──────────────────────────────┐
        │ 🔒 ENTIRE MAP LOCKED          │
        └──────────────────────────────┘

Only ONE thread works at a time → very slow 🐢
```

Real-world idea: a bank with 10 counters but only **one** key for the whole building — everyone queues.

**ConcurrentHashMap** locks only the bucket being written:

```
Thread 1 → bucket 2  🔒  works ✅
Thread 2 → bucket 9  🔒  works ✅   (at the same time!)
Thread 3 → bucket 14 🔒  works ✅

Different buckets → no waiting → fast ⚡
```

Real-world idea: the same bank, but each counter has its own lock. Only two people wanting the *same* counter have to wait.

```
Java 7 → segment locking (16 segments)
Java 8 → per-bucket CAS + synchronized on the first node (even finer)
```

Reads in `ConcurrentHashMap` need **no lock at all**.

#### Comparison table

| | HashMap | Hashtable | ConcurrentHashMap |
|---|---|---|---|
| Thread-safe | ❌ No | ✅ Yes | ✅ Yes |
| Locking | none | whole map | per bucket |
| Performance | fastest (1 thread) | slowest | fast (many threads) |
| null key | ✅ one | ❌ NPE | ❌ NPE |
| null value | ✅ many | ❌ NPE | ❌ NPE |
| Iterator | fail-fast | fail-fast (enumerator) | fail-safe (weakly consistent) |
| Since | 1.2 | 1.0 (legacy) | 1.5 |
| Use it? | ✅ single thread | ❌ never | ✅ multi thread |

#### Why does ConcurrentHashMap not allow null?

```java
map.get("key");     // returns null
```

In a concurrent map that is ambiguous:

```
null means → the key is absent?
null means → the key exists with a null value?
```

In a single-threaded HashMap you can resolve it with `containsKey()`, but in a concurrent map another thread could change the answer between the two calls. So nulls were simply banned.

#### What about `Collections.synchronizedMap()`?

```java
Map<String, String> m = Collections.synchronizedMap(new HashMap<>());
```

This just wraps every method in a lock on the whole map — the same problem as Hashtable. Use `ConcurrentHashMap` instead.

#### Interview-ready answer

> `HashMap` is not synchronized, so it is the fastest option but unsafe if multiple threads modify it. `Hashtable` is the legacy thread-safe version, but it synchronizes every method on the entire map, so only one thread can operate at a time and it becomes a bottleneck. `ConcurrentHashMap` is the modern thread-safe option: in Java 8 it locks only the individual bucket being written using CAS and synchronization on the first node, and reads require no locking at all, so many threads can work in parallel. Also, `HashMap` allows one null key and multiple null values, while `Hashtable` and `ConcurrentHashMap` do not allow nulls at all, because in a concurrent map a null return value would be ambiguous. In real projects I use `HashMap` for single-threaded code and `ConcurrentHashMap` for shared caches or counters.

#### Easy memory

```
HashMap           → no lock       → fastest → single thread ⭐
Hashtable         → ONE big lock  → slow    → legacy ❌
ConcurrentHashMap → bucket lock   → fast    → multi thread ⭐

Bank: one key for the whole bank (Hashtable)
      vs one lock per counter (ConcurrentHashMap)

null: HashMap ✅ | Hashtable ❌ | ConcurrentHashMap ❌
```

---

### Q: HashSet vs TreeSet?

The easiest way to remember:

```
HashSet  → NO order      → FAST      → O(1)      ⭐ default
TreeSet  → SORTED order  → slower    → O(log n)
LinkedHashSet → INSERTION order      → O(1)
```

#### See it immediately

```java
Set<String> hashSet = new HashSet<>();
hashSet.add("Zebra");
hashSet.add("Apple");
hashSet.add("Mango");
System.out.println(hashSet);      // [Apple, Zebra, Mango]  ← random-looking

Set<String> treeSet = new TreeSet<>();
treeSet.add("Zebra");
treeSet.add("Apple");
treeSet.add("Mango");
System.out.println(treeSet);      // [Apple, Mango, Zebra]  ← SORTED ✅

Set<String> linked = new LinkedHashSet<>();
linked.add("Zebra");
linked.add("Apple");
linked.add("Mango");
System.out.println(linked);       // [Zebra, Apple, Mango]  ← insertion order
```

#### How they store data

```
HashSet  → backed by a HashMap → buckets → no order

TreeSet  → backed by a TreeMap → red-black TREE

                Mango
               /      \
          Apple        Zebra

         (always kept sorted)
```

Real-world idea:

```
HashSet  → clothes thrown into a cupboard: finding one is instant if you know it,
           but there is no order
TreeSet  → a dictionary: always alphabetical, but inserting a new word means
           finding the right place
```

#### Comparison table

| | HashSet | LinkedHashSet | TreeSet |
|---|---|---|---|
| Order | none | insertion order | **sorted** |
| Speed | O(1) ⚡ | O(1) | O(log n) |
| Internally | HashMap | HashMap + linked list | TreeMap (red-black tree) |
| null allowed | ✅ one | ✅ one | ❌ NPE |
| Needs Comparable | ❌ | ❌ | ✅ yes |
| Extra methods | — | — | `first()`, `last()`, `headSet()`, `tailSet()`, `ceiling()` |

#### TreeSet's bonus methods

```java
TreeSet<Integer> marks = new TreeSet<>(List.of(45, 78, 92, 60));

marks.first();          // 45  (lowest)
marks.last();           // 92  (highest)
marks.headSet(70);      // [45, 60]   → below 70
marks.tailSet(70);      // [78, 92]   → 70 and above
marks.ceiling(70);      // 78  → smallest value ≥ 70
marks.floor(70);        // 60  → largest value ≤ 70
```

Very useful for range queries — "all students who scored above 70".

#### TreeSet traps ⭐

**1. No null**

```java
TreeSet<String> ts = new TreeSet<>();
ts.add(null);       // 💥 NullPointerException
```

Because it must **compare** elements, and it can't compare null.

**2. Custom objects must be comparable**

```java
TreeSet<Employee> set = new TreeSet<>();
set.add(new Employee(101, "Priya"));    // 💥 ClassCastException
```

Fix it either way:

```java
// Option A — implement Comparable
class Employee implements Comparable<Employee> {
    public int compareTo(Employee o) {
        return Integer.compare(this.id, o.id);
    }
}

// Option B — pass a Comparator
TreeSet<Employee> set =
    new TreeSet<>(Comparator.comparing(Employee::getName));
```

**3. TreeSet uses `compareTo()`, not `equals()`, to decide duplicates**

```java
// if compareTo() returns 0, TreeSet treats them as the SAME element
```

#### Interview-ready answer

> `HashSet` is backed by a `HashMap` and stores elements with no ordering, giving O(1) add, remove and contains, so it is the default choice for uniqueness. `TreeSet` is backed by a `TreeMap` which is a red-black tree, so elements are always kept in sorted order, but operations are O(log n). `TreeSet` also does not allow null and requires elements to be comparable, either by implementing `Comparable` or by passing a `Comparator`. In return it gives useful navigation methods like `first()`, `last()`, `headSet()` and `ceiling()` for range queries. If I only need uniqueness I use `HashSet`; if I need sorted output I use `TreeSet`; and if I need to preserve insertion order I use `LinkedHashSet`.

#### Easy memory

```
HashSet       → cupboard   → no order      → O(1)     ⭐
LinkedHashSet → same, but remembers the order it went in
TreeSet       → dictionary → SORTED        → O(log n)

TreeSet: no null ❌, needs Comparable/Comparator, gives first()/last()/ceiling()
```

---

### Q: Comparable vs Comparator?

The easiest way to remember:

```
Comparable → the class sorts ITSELF   → ONE natural order   → compareTo()
Comparator → an OUTSIDE sorter        → MANY orders         → compare()
```

#### The story

You have an `Employee` class. The "natural" order is by id — that belongs inside the class.

```java
class Employee implements Comparable<Employee> {

    int id;
    String name;
    double salary;

    @Override
    public int compareTo(Employee other) {
        return Integer.compare(this.id, other.id);   // natural order = id
    }
}
```

```java
Collections.sort(list);      // uses compareTo() automatically
```

But tomorrow the manager wants sorting by salary, and HR wants sorting by name. You can't have three `compareTo()` methods — so you write **Comparators**.

```java
Comparator<Employee> bySalary = Comparator.comparingDouble(e -> e.salary);
Comparator<Employee> byName   = Comparator.comparing(e -> e.name);

list.sort(bySalary);
list.sort(byName);
```

```
                Employee
                   │
        compareTo() → ONE natural order (id)
                   │
   ┌───────────────┼────────────────┐
   ↓               ↓                ↓
bySalary        byName          byAgeDesc     ← MANY comparators
```

Real-world idea: **Amazon product listing.**

```
Default listing order        → Comparable  (built into the product)
"Sort by: Price / Rating /   → Comparator  (you choose at runtime)
 Newest / Popularity"
```

#### Modern Comparator syntax (use this in the interview) ⭐

```java
// single field
list.sort(Comparator.comparing(Employee::getName));

// descending
list.sort(Comparator.comparing(Employee::getSalary).reversed());

// multi-level: department, then salary descending
list.sort(Comparator.comparing(Employee::getDept)
                    .thenComparing(Comparator.comparing(Employee::getSalary).reversed()));

// null-safe
list.sort(Comparator.nullsFirst(Comparator.comparing(Employee::getName)));
```

This `thenComparing` chain is a strong thing to show — it means "sort by department; if the department is the same, sort by salary descending".

#### What does the return value mean?

```java
compareTo(other)

negative  → this comes BEFORE other
zero      → they are equal
positive  → this comes AFTER other
```

Easy way to remember ascending order:

```java
return this.id - other.id;        // ⚠️ works, but can overflow
return Integer.compare(this.id, other.id);   // ✅ safe
```

For descending, just flip it:

```java
return Integer.compare(other.id, this.id);
```

#### Comparison table

| | Comparable | Comparator |
|---|---|---|
| Package | `java.lang` | `java.util` |
| Method | `compareTo(T o)` | `compare(T a, T b)` |
| Where the logic lives | inside the class | outside the class |
| Number of orders | one (natural) | many |
| Modifies the class? | ✅ yes | ❌ no |
| Sort call | `Collections.sort(list)` | `list.sort(comparator)` |
| Use when | there is one obvious order | you need several orders, or you can't edit the class |

#### The key practical point

If the class is from a library and you **cannot edit it**, `Comparable` is impossible — `Comparator` is your only option. That is a great point to mention.

#### Interview-ready answer

> `Comparable` is implemented by the class itself and defines its single natural ordering through `compareTo()` — for example an `Employee` sorted by id by default. `Comparator` is a separate object that defines an alternative ordering through `compare()`, so we can have many of them, such as sorting employees by salary, by name or by joining date. `Comparable` requires modifying the class, so if the class comes from a third-party library, `Comparator` is the only option. Since Java 8 we usually write comparators using `Comparator.comparing()` with method references, and chain them with `thenComparing()` and `reversed()` for multi-level sorting.

#### Easy memory

```
Comparable → "I can compare MYSELF" → compareTo()  → 1 natural order  → java.lang
Comparator → "I compare OTHERS"     → compare(a,b) → many orders      → java.util

Amazon: default listing = Comparable | "Sort by price" = Comparator

Comparator.comparing(Employee::getSalary).reversed()
          .thenComparing(Employee::getName)
```

---

### Q: Fail-fast vs fail-safe iterators?

The easiest way to remember:

```
Fail-fast → works on the ORIGINAL   → modification → throws ConcurrentModificationException 💥
Fail-safe → works on a COPY         → modification → no exception, but you may see stale data
```

#### The famous crash

```java
List<String> list = new ArrayList<>(List.of("A", "B", "C"));

for (String s : list) {
    if (s.equals("B")) {
        list.remove(s);        // 💥 ConcurrentModificationException
    }
}
```

#### Why does it happen? — `modCount`

Every ArrayList/HashMap keeps a counter of structural changes:

```
list created         → modCount = 0
iterator created     → expectedModCount = 0

list.remove("B")     → modCount = 1

next loop step → iterator checks:
      modCount (1) != expectedModCount (0)
                    ↓
      throw ConcurrentModificationException 💥
```

Real-world idea: You are reading a book. Someone tears out a page while you are reading. The iterator refuses to continue with a book that changed underneath it — better to fail loudly than to give wrong results.

#### The correct fixes

```java
// 1. Use the iterator's own remove()  ⭐
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("B")) {
        it.remove();                   // ✅ updates expectedModCount too
    }
}

// 2. removeIf() — cleanest, Java 8
list.removeIf(s -> s.equals("B"));     // ✅

// 3. Collect into a new list
List<String> result = list.stream()
                          .filter(s -> !s.equals("B"))
                          .toList();   // ✅

// 4. Use a concurrent collection
List<String> safe = new CopyOnWriteArrayList<>(list);
```

#### Fail-safe iterators

```java
Map<String, String> map = new ConcurrentHashMap<>();
map.put("A", "1");
map.put("B", "2");

for (String key : map.keySet()) {
    map.put("C", "3");         // ✅ no exception
}
```

`CopyOnWriteArrayList` literally copies the array on every write:

```
Write happens
     ↓
create a NEW copy of the array
     ↓
the running iterator keeps reading the OLD snapshot → safe, but slightly stale
```

That is why `CopyOnWriteArrayList` is used for read-heavy, write-rare data (like a listener list or a config cache) — writes are expensive.

`ConcurrentHashMap`'s iterator is "weakly consistent": it never throws, and it may or may not show changes made after it started.

#### Comparison

| | Fail-fast | Fail-safe |
|---|---|---|
| Works on | the original collection | a copy / snapshot |
| On modification | `ConcurrentModificationException` | no exception |
| Memory | low | higher (copies) |
| Data freshness | always current | may be stale |
| Examples | `ArrayList`, `HashMap`, `HashSet`, `Vector` | `CopyOnWriteArrayList`, `ConcurrentHashMap` |

#### Important nuance ⭐

Fail-fast is **best effort** — it is not guaranteed. It is a debugging aid, not a synchronization mechanism. Never write code that depends on catching `ConcurrentModificationException`.

#### Interview-ready answer

> Fail-fast iterators operate directly on the collection and keep an internal `modCount`. If the collection is structurally modified while iterating, the count no longer matches what the iterator expected and it throws a `ConcurrentModificationException` — this is the behaviour of `ArrayList`, `HashMap` and `HashSet`. Fail-safe iterators work on a copy or a snapshot of the data, so modification during iteration does not throw, but the iterator may not reflect the latest changes — `CopyOnWriteArrayList` and `ConcurrentHashMap` behave this way. To remove elements safely while iterating I use `iterator.remove()` or, more commonly, `removeIf()`. It is also worth noting that fail-fast behaviour is best-effort and should not be relied on for correctness.

#### Easy memory

```
Fail-fast → ORIGINAL → modCount mismatch → 💥 ConcurrentModificationException
            (ArrayList, HashMap, HashSet)

Fail-safe → COPY     → no crash, maybe stale data
            (CopyOnWriteArrayList, ConcurrentHashMap)

Fix: it.remove()  |  list.removeIf(...)  ⭐
Someone tears a page out of the book you're reading → fail-fast complains
```

---
## Exceptions

### Q: Checked vs unchecked exceptions?

The easiest way to remember:

```
Checked   → the COMPILER forces you to handle it   → external problems
Unchecked → the compiler does NOT force you        → programming mistakes
```

#### The hierarchy first

```
                 Throwable
                     │
        ┌────────────┴────────────┐
        ↓                         ↓
     Error                    Exception
  (don't catch)                    │
  OutOfMemoryError      ┌──────────┴──────────┐
  StackOverflowError    ↓                     ↓
                   RuntimeException      All others
                   (UNCHECKED)           (CHECKED)
                        │                     │
              NullPointerException      IOException
              ArithmeticException       SQLException
              ArrayIndexOutOfBounds     FileNotFoundException
              ClassCastException        ClassNotFoundException
              NumberFormatException
```

⭐ One line rule: **everything under `RuntimeException` (and `Error`) is unchecked; everything else under `Exception` is checked.**

#### Checked — the compiler blocks you

```java
FileReader fr = new FileReader("data.txt");    // ❌ won't compile
```

You must either handle it:

```java
try {
    FileReader fr = new FileReader("data.txt");
} catch (FileNotFoundException e) {
    System.out.println("File missing");
}
```

or declare it:

```java
void readFile() throws FileNotFoundException {
    FileReader fr = new FileReader("data.txt");
}
```

Why does Java force this? Because the file genuinely **might** be missing — it is an *expected* external failure and your program should have a plan for it.

#### Unchecked — the compiler stays silent

```java
String s = null;
s.length();            // compiles fine, crashes at runtime 💥
```

```java
int[] arr = new int[3];
arr[5] = 10;           // compiles fine, crashes at runtime 💥
```

The compiler doesn't force handling because these are **bugs in your code**, not external conditions. The fix is not a try-catch — the fix is a null check or a correct index.

#### Real-world idea

```
CHECKED   → going out in the monsoon
            You KNOW it may rain → carry an umbrella (compiler insists)
            → server down, file missing, network failure

UNCHECKED → you trip over your own feet
            No umbrella helps → just walk properly (fix the code)
            → null pointer, wrong index, divide by zero
```

#### Comparison

| | Checked | Unchecked |
|---|---|---|
| Checked at | compile time | runtime |
| Must handle? | ✅ yes | ❌ no |
| Parent | `Exception` | `RuntimeException` |
| Cause | external / environment | programming bug |
| Examples | `IOException`, `SQLException` | `NullPointerException`, `ArithmeticException` |
| Recoverable? | usually yes | usually means fix the code |

#### Errors — never catch these

```java
try {
    recursion();       // infinite recursion
} catch (StackOverflowError e) { }    // ❌ bad practice
```

`Error` means the JVM itself is in trouble (`OutOfMemoryError`, `StackOverflowError`). You cannot meaningfully recover, so let it crash.

#### Modern practice ⭐ (great point for a lead-level interview)

Spring and most modern frameworks prefer **unchecked** exceptions:

```java
public class ResourceNotFoundException extends RuntimeException {   // unchecked
    public ResourceNotFoundException(String msg) { super(msg); }
}
```

Why? Checked exceptions force every caller in the chain to either catch or declare, which pollutes method signatures:

```java
void a() throws SQLException { b(); }
void b() throws SQLException { c(); }
void c() throws SQLException { ... }      // noise all the way up
```

That is why Spring wraps `SQLException` into the unchecked `DataAccessException`.

#### Interview-ready answer

> Checked exceptions are checked at compile time, and the compiler forces us to either handle them with try-catch or declare them with `throws`. They extend `Exception` but not `RuntimeException`, and they represent expected external problems such as a missing file or a database failure — `IOException` and `SQLException` are examples. Unchecked exceptions extend `RuntimeException` and are not verified at compile time; they usually indicate programming errors such as `NullPointerException` or `ArrayIndexOutOfBoundsException`, and the right fix is to correct the code rather than catch the exception. `Error` is a third category representing serious JVM problems like `OutOfMemoryError`, which we should not catch. In modern applications, especially with Spring, we generally prefer custom unchecked exceptions because checked exceptions force every layer to declare them and clutter the API.

#### 🔵 In your RoboGebra project — all 12 exceptions are UNCHECKED ⭐

```
common/exception/
   APIException              ExternalApiException      QuizConflictException
   ResourceNotFoundException CognitoException          EmailSendingException
   DatabaseException         SolutionExecutionException RestrictedAPIAccessException
   FirebaseMessageException  JsonException             FileNotFoundException

⭐ EVERY ONE of them extends RuntimeException — none is checked.
```

```java
public class ResourceNotFoundException extends RuntimeException { ... }   // unchecked ⭐

@Getter
public class APIException extends RuntimeException {
    private final HttpStatus status;      // ⭐ carries the status it should become
}
```

> 🗣️ *"Every custom exception in our codebase is unchecked. That's deliberate and it's the Spring convention — a checked exception forces every layer in between to either catch it or declare it, which clutters signatures for something the controller advice is going to handle centrally anyway. `APIException` even carries its own `HttpStatus`, so the advice reads the status off the exception instead of mapping by type."*

⭐ Note we also **wrap** third-party checked exceptions rather than propagate them — `JsonException` and `CognitoException` exist so a Jackson or AWS failure doesn't leak its own exception type into our service layer.

#### Easy memory

```
CHECKED   → compiler FORCES you  → outside problem → IOException, SQLException
            (monsoon → carry an umbrella)

UNCHECKED → compiler is silent   → your bug        → NullPointer, ArrayIndex
            (you tripped → fix your walking)

ERROR     → JVM is dying         → don't catch     → OutOfMemory, StackOverflow

Rule: under RuntimeException = unchecked. Everything else = checked.
```

---

### Q: `throw` vs `throws`?

The easiest way to remember:

```
throw   → ACTUALLY throws one exception   → inside the method body   → one object
throws  → WARNS that it may throw         → in the method signature  → many types
```

Note the `s`: `throws` is a **warning** (like a signboard), `throw` is the **action**.

#### `throw` — the action

```java
void withdraw(double amount) {

    if (amount > balance) {
        throw new IllegalArgumentException("Insufficient balance");   // ACTION
    }

    balance -= amount;
}
```

```
throw + ONE object + inside the body
```

#### `throws` — the declaration

```java
void readFile() throws IOException, SQLException {      // WARNING
    // ...
}
```

```
throws + one or MORE class names + in the signature
```

It tells the caller: "be prepared, this can fail."

#### Both together

```java
void validateAge(int age) throws InvalidAgeException {   // ← declaration

    if (age < 18) {
        throw new InvalidAgeException("Age must be 18+");   // ← action
    }
}
```

Caller side:

```java
try {
    validateAge(15);
} catch (InvalidAgeException e) {
    System.out.println(e.getMessage());
}
```

Flow:

```
validateAge(15)
      ↓
 throw new InvalidAgeException   ← thrown here
      ↓
 method stops immediately
      ↓
 caller's catch block            ← handled here
```

#### Real-world idea — a medicine bottle

```
throws → the WARNING LABEL on the bottle: "may cause drowsiness"
throw  → you actually FEEL drowsy
```

The label warns; the effect is the action.

#### Comparison

| | throw | throws |
|---|---|---|
| Purpose | actually throw it | declare the possibility |
| Where | inside the method body | in the method signature |
| Followed by | an exception **object** | exception **class** names |
| How many | exactly one | one or many (comma-separated) |
| Example | `throw new IOException();` | `void m() throws IOException` |

#### Interview traps

**1. You can throw only a Throwable**

```java
throw new String("error");     // ❌ compile error
```

**2. Unchecked exceptions do not need `throws`**

```java
void m() {
    throw new RuntimeException("boom");    // ✅ no `throws` required
}
```

**3. Code after `throw` is unreachable**

```java
throw new RuntimeException("x");
System.out.println("hi");      // ❌ unreachable statement — compile error
```

**4. An overriding method cannot declare broader checked exceptions**

```java
class Parent { void m() throws IOException { } }

class Child extends Parent {
    void m() throws Exception { }        // ❌ Exception is broader than IOException
}
```

#### Interview-ready answer

> `throw` is used inside a method body to actually throw a single exception object — for example `throw new IllegalArgumentException("Insufficient balance")`. `throws` is used in the method signature to declare that the method may throw one or more exception types, so the caller knows it must handle or propagate them. `throw` takes an instance, `throws` takes class names. Only checked exceptions need to be declared with `throws`; unchecked exceptions can be thrown without declaring them. Also, an overriding method cannot declare broader checked exceptions than the parent method.

#### Easy memory

```
throw  → ACTION      → throw new IOException();          → 1 object → in body
throws → WARNING (s) → void m() throws IOException, SQLException → in signature

Medicine bottle: LABEL = throws | actual drowsiness = throw
```

---

### Q: What is try-with-resources?

The easiest way to remember:

```
try-with-resources = Java CLOSES the resource for you, automatically
```

Introduced in Java 7. It replaced the ugly old `finally` cleanup code.

#### The old painful way

```java
BufferedReader br = null;
try {
    br = new BufferedReader(new FileReader("data.txt"));
    System.out.println(br.readLine());
} catch (IOException e) {
    e.printStackTrace();
} finally {
    if (br != null) {           // null check
        try {
            br.close();          // close can ITSELF throw!
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

Eleven lines of noise just to close one file — and everyone forgets the null check.

#### The new way

```java
try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
    System.out.println(br.readLine());
} catch (IOException e) {
    e.printStackTrace();
}
// br.close() is called AUTOMATICALLY ✅
```

```
try ( resource )
      ↓
  use it
      ↓
block ends (normally OR by exception)
      ↓
close() is called automatically ✅
```

#### Real-world idea

```
Old way → you must remember to switch off the tap after washing your hands
New way → a sensor tap that closes by itself the moment you walk away
```

#### Multiple resources

```java
try (Connection con = getConnection();
     PreparedStatement ps = con.prepareStatement(SQL);
     ResultSet rs = ps.executeQuery()) {

    while (rs.next()) {
        System.out.println(rs.getString("name"));
    }
}
```

Closing order is **reverse** of creation — exactly what you want:

```
open : Connection → Statement → ResultSet
close: ResultSet  → Statement → Connection    ← reverse ✅
```

#### The requirement — AutoCloseable

Any class works as long as it implements `AutoCloseable`:

```java
class MyResource implements AutoCloseable {

    void use() { System.out.println("using resource"); }

    @Override
    public void close() {
        System.out.println("resource closed automatically");
    }
}
```

```java
try (MyResource r = new MyResource()) {
    r.use();
}
```

Output:

```
using resource
resource closed automatically
```

#### Suppressed exceptions ⭐ (a strong point to mention)

What if the body throws *and* `close()` throws?

```
body throws    → "Read failed"       ← this one is reported (primary)
close() throws → "Close failed"      ← this one is SUPPRESSED, not lost
```

```java
catch (Exception e) {
    System.out.println(e.getMessage());                  // Read failed
    for (Throwable t : e.getSuppressed()) {
        System.out.println("Suppressed: " + t.getMessage());  // Close failed
    }
}
```

In the old `finally` style, the close exception would **overwrite** the real one and you would lose the actual cause — a classic debugging nightmare.

#### Java 9 improvement

```java
BufferedReader br = new BufferedReader(new FileReader("data.txt"));

try (br) {          // ✅ Java 9+: use an existing effectively-final variable
    System.out.println(br.readLine());
}
```

#### Interview-ready answer

> Try-with-resources was introduced in Java 7 and automatically closes any resource declared in the try parentheses, as long as the resource implements `AutoCloseable`. It replaces the old pattern of closing in a `finally` block with null checks and nested try-catch. Multiple resources can be declared, and they are closed in reverse order of creation. Another important benefit is suppressed exceptions: if the try block throws and `close()` also throws, the original exception is reported and the close exception is attached as suppressed, so the real cause is never lost — in the old style the close exception would hide the actual error. Since Java 9 we can also use an already-declared effectively-final variable inside the parentheses.

#### 🔵 In your RoboGebra project — try-with-resources, with TWO resources ⭐

**File:** `common/importer/ClassPathFileReader.java`

```java
try (InputStream inputStream = getClass().getResourceAsStream(filePath);
     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {   // ⭐ two

    if (inputStream.available() <= 0) {
        throw new RuntimeException(filePath + " Not Available ");
    }
    String fileContents = readAllLines(reader);
    return new CSVReader(new StringReader(fileContents));

} catch (Exception e) {
    throw new RuntimeException(e);      // ⭐ the CAUSE is preserved
}
```

```
Both resources are closed AUTOMATICALLY, in REVERSE order: ⭐
   open : InputStream → BufferedReader
   close: BufferedReader → InputStream
```

Also in `common/service/FileService.java`:

```java
try (InputStream inputStream = resource.getInputStream()) { ... }
try (OutputStream os = new FileOutputStream(tempPdfFile)) { ... }
```

> 🗣️ *"The CSV importer opens two resources in one try-with-resources — the stream and the reader — and both close automatically in reverse order. The old `finally` version needed a null check and a nested try just to close them, and it would have masked the real exception if `close()` also threw."*

#### Easy memory

```
try (Resource r = ...) { }   → close() runs AUTOMATICALLY ✅
Needs: implements AutoCloseable
Multiple resources → closed in REVERSE order
Body + close both throw → body wins, close becomes SUPPRESSED (not lost)

Sensor tap: closes by itself.
```

---

### Q: How do you create a custom exception?

The easiest way to remember:

```
extends RuntimeException  → UNCHECKED → no forced handling  ⭐ modern choice
extends Exception         → CHECKED   → caller MUST handle
```

#### Why create one at all?

Compare these two:

```java
throw new Exception("error");                    // ❌ meaningless
throw new InsufficientBalanceException(
        "Balance 500, requested 2000");          // ✅ tells the full story
```

A custom exception makes your logs and your API responses readable, and lets callers catch **exactly** the failure they care about.

#### Unchecked custom exception (most common)

```java
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);        // ⭐ keeps the original cause
    }
}
```

Usage:

```java
public Employee getEmployee(Long id) {
    return repo.findById(id)
        .orElseThrow(() ->
            new ResourceNotFoundException("Employee not found with id " + id));
}
```

#### Checked custom exception

```java
public class InsufficientBalanceException extends Exception {

    private final double balance;
    private final double requested;

    public InsufficientBalanceException(double balance, double requested) {
        super("Balance is " + balance + " but " + requested + " was requested");
        this.balance = balance;
        this.requested = requested;
    }

    public double getShortfall() {         // extra business info ⭐
        return requested - balance;
    }
}
```

The nice part: you can carry **extra data** on the exception, not just a message.

```java
catch (InsufficientBalanceException e) {
    System.out.println("Add " + e.getShortfall() + " more");
}
```

#### Always keep the cause ⭐

```java
try {
    jdbc.query(...);
} catch (SQLException e) {
    throw new DataAccessException("Failed to load employee " + id, e);
    //                                                            ↑
    //                                              never drop this!
}
```

```
With cause:
  DataAccessException: Failed to load employee 101
  Caused by: SQLException: Connection timed out     ← the REAL problem ✅

Without cause:
  DataAccessException: Failed to load employee 101  ← debugging blind 💥
```

#### Real Spring Boot usage — the pattern interviewers like

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(new ErrorResponse(404, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(new ErrorResponse(500, "Something went wrong"));
    }
}
```

```
Service throws ResourceNotFoundException
        ↓
@RestControllerAdvice catches it
        ↓
Client receives a clean 404 JSON, not a Java stack trace ✅
```

#### Checklist for a good custom exception

```
✅ Name ends with "Exception"
✅ Name says WHAT went wrong (ResourceNotFoundException, not MyException)
✅ Provide a (String message) constructor
✅ Provide a (String message, Throwable cause) constructor
✅ Add business fields if they help the caller
✅ Unchecked unless the caller can genuinely recover
```

#### Interview-ready answer

> A custom exception is created by extending `RuntimeException` for an unchecked exception or `Exception` for a checked one. I usually extend `RuntimeException`, because forcing every layer to declare a checked exception clutters the API — this is also the approach Spring takes. I always provide two constructors: one taking a message and one taking a message and a cause, because preserving the cause is essential for debugging. I may also add business fields, for example an `InsufficientBalanceException` carrying the balance and the requested amount. In a Spring Boot application these custom exceptions are then handled centrally in a `@RestControllerAdvice` class so the client receives a clean error response with the correct HTTP status instead of a stack trace.

#### 🔵 In your RoboGebra project — the real global handler ⭐

**File:** `common/exception/handler/ExceptionControllerAdvice.java`

```java
@ControllerAdvice
@Slf4j
public class ExceptionControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)          // ⭐ @Valid failures
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex,
                                                            WebRequest request) {
        log.error("Validation exception: {}", ex.getMessage(), ex);   // ⭐ LOG the real cause
        String message = findErrorMessageForValidation(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(createErrorMessage(HttpStatus.BAD_REQUEST, message, request));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessageModel> handleResourceNotFoundException(...) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(...);
    }

    @ExceptionHandler(CognitoException.class)          // ⭐ AWS failures, translated
    @ExceptionHandler(NoHandlerFoundException.class)   // ⭐ unmapped URLs → a clean 404
    // + MaxUploadSizeExceededException, ConstraintViolationException,
    //   MongoWriteException, InvalidOTPException, CouponValidationException …
}
```

```
Every handler returns the SAME ErrorMessageModel shape ⭐
   → the client parses one error format, never a Java stack trace
```

⭐ **And the second layer most people don't know about:**

```
@ControllerAdvice only covers exceptions thrown INSIDE the MVC dispatch.

Anything thrown in a SECURITY FILTER happens BEFORE the DispatcherServlet,
so the advice never sees it and the client gets a bare 500 💥
      ↓
which is why we ALSO have security/FilterChainExceptionHandler ⭐
```

> 🗣️ *"One `@ControllerAdvice` handles the whole application and every handler returns the same error model, so clients parse one shape. It also translates third-party failures — a Cognito `UserNotFoundException` or a `MongoWriteException` becomes our error format rather than leaking AWS or Mongo types to the client. And because the advice only covers the MVC dispatch, we have a separate filter-chain handler for exceptions thrown before the DispatcherServlet."*

#### Easy memory

```
extends RuntimeException → unchecked ⭐ (Spring style)
extends Exception        → checked (caller must handle)

ALWAYS:  super(message)  and  super(message, cause)   ← keep the cause!
Name it after the PROBLEM: ResourceNotFoundException

Spring: throw in service → @RestControllerAdvice → clean JSON error
```

---

### Q: Describe the exception hierarchy.

The easiest way to remember:

```
Throwable
   ├── Error       → JVM problems  → DON'T catch
   └── Exception   → app problems  → DO handle
         ├── RuntimeException → UNCHECKED (your bug)
         └── everything else  → CHECKED   (external problem)
```

#### Full diagram

```
                      Object
                        │
                    Throwable                 ← root of everything throwable
                        │
        ┌───────────────┴────────────────┐
        ↓                                ↓
      Error                          Exception
   (unchecked)                           │
        │                    ┌───────────┴───────────┐
        │                    ↓                       ↓
        │            RuntimeException          IOException
        │              (UNCHECKED)             SQLException
        │                    │                 ClassNotFoundException
        │                    │                 InterruptedException
        │                    │                     (CHECKED)
        │                    │
  OutOfMemoryError    NullPointerException
  StackOverflowError  ArithmeticException
  NoClassDefFoundError ArrayIndexOutOfBoundsException
  AssertionError      ClassCastException
                      NumberFormatException
                      IllegalArgumentException
                      IllegalStateException
                      ConcurrentModificationException
                      UnsupportedOperationException
```

#### The three groups in plain words

```
ERROR
  → the JVM itself is broken
  → OutOfMemoryError, StackOverflowError
  → you cannot fix it in code → let it crash

CHECKED EXCEPTION
  → the outside world failed
  → file missing, DB down, network dropped
  → compiler FORCES you to plan for it

UNCHECKED (RuntimeException)
  → your code has a bug
  → null, bad index, bad cast
  → compiler stays silent → fix the code
```

Real-world idea — a **restaurant**:

```
ERROR              → the building is on fire 🔥      → evacuate, don't "handle" it
CHECKED EXCEPTION  → an ingredient is out of stock  → have a backup plan
UNCHECKED          → the chef forgot the salt       → fix the chef, not the recipe
```

#### Catch order matters ⭐

```java
try {
    // ...
} catch (Exception e) {              // ❌ too broad, FIRST
} catch (IOException e) {            // ❌ unreachable → compile error
}
```

Always go **specific → general**:

```java
try {
    // ...
} catch (FileNotFoundException e) {   // most specific
} catch (IOException e) {             // broader
} catch (Exception e) {               // most general — last
}
```

```
Specific  ↑
          │  catch order
General   ↓
```

#### Multi-catch (Java 7)

```java
try {
    // ...
} catch (IOException | SQLException e) {     // same handling for both
    log.error("Data access failed", e);
}
```

The variable `e` is implicitly `final` in multi-catch.

#### Useful Throwable methods

```java
e.getMessage();        // "File not found"
e.getCause();          // the underlying exception
e.printStackTrace();   // full trace (use a logger in production!)
e.getStackTrace();     // array of stack frames
e.getSuppressed();     // exceptions suppressed by try-with-resources
```

#### Interview-ready answer

> `Throwable` is the root of the hierarchy and has two direct children, `Error` and `Exception`. `Error` represents serious JVM-level problems such as `OutOfMemoryError` and `StackOverflowError`, which an application cannot meaningfully recover from, so we should not catch them. `Exception` splits into two groups: `RuntimeException` and its subclasses, which are unchecked and usually indicate programming bugs like `NullPointerException` or `ArrayIndexOutOfBoundsException`, and all other exceptions, which are checked and represent external failures like `IOException` and `SQLException`. When writing catch blocks we must order them from the most specific to the most general, otherwise the code will not compile, and since Java 7 we can combine unrelated exception types in a single multi-catch block.

#### Easy memory

```
Throwable
 ├── Error     → JVM dying   → don't catch   → building on fire 🔥
 └── Exception
      ├── RuntimeException → UNCHECKED → your bug     → chef forgot salt
      └── others           → CHECKED   → outside      → out of stock

catch order: SPECIFIC first, GENERAL last (else compile error)
multi-catch: catch (IOException | SQLException e)
```

---

### Q: What happens with finally and a return statement?

The easiest way to remember:

```
finally ALWAYS runs — even after return.
And if finally has its OWN return, it WINS (and swallows exceptions). ⚠️
```

#### Case 1 — return in try, print in finally

```java
static int test() {
    try {
        return 1;
    } finally {
        System.out.println("finally runs");
    }
}
```

Output:

```
finally runs
1
```

The return value `1` is computed and **held**, then `finally` runs, then the value is returned.

```
return 1
   ↓
value 1 is saved aside
   ↓
finally block runs
   ↓
NOW 1 is actually returned
```

#### Case 2 — return in BOTH ⚠️

```java
static int test() {
    try {
        return 1;
    } finally {
        return 2;        // ⚠️ overrides!
    }
}
```

Output:

```
2
```

The `finally` return completely replaces the try's return.

```
try     → return 1  (discarded)
finally → return 2  ← WINS
```

#### Case 3 — the dangerous one: finally swallows the exception 💥

```java
static int test() {
    try {
        throw new RuntimeException("Something failed!");
    } finally {
        return 99;       // ⚠️ exception DISAPPEARS
    }
}

System.out.println(test());     // 99 — no exception at all!
```

The exception is silently destroyed. The caller never learns anything went wrong. This is why **you should never put `return` inside `finally`** — most linters flag it as a bug.

```
try     → throws Exception 💥
finally → return 99
              ↓
   Exception is SWALLOWED — nobody ever sees it ❌
```

#### Case 4 — modifying a primitive in finally does nothing

```java
static int test() {
    int x = 1;
    try {
        return x;          // the VALUE 1 is copied out right here
    } finally {
        x = 99;            // too late — the return value was already fixed
    }
}
```

Output:

```
1
```

Because the return value was already copied. But with an **object**, mutation IS visible:

```java
static List<String> test() {
    List<String> list = new ArrayList<>();
    try {
        return list;            // the REFERENCE is copied
    } finally {
        list.add("added");      // ✅ same object → visible!
    }
}

System.out.println(test());     // [added]
```

```
Primitive → the VALUE is already copied      → change is invisible
Object    → the REFERENCE is copied          → mutation IS visible
```

This is the same "pass-by-value" idea again.

#### When does `finally` NOT run?

```
System.exit(0)      → JVM shuts down
JVM crash           → power cut, kill -9
Infinite loop in try
Thread killed at OS level
```

```java
try {
    System.exit(0);
} finally {
    System.out.println("never printed");   // ❌
}
```

#### Interview-ready answer

> `finally` always executes, even when the `try` block has a return statement — the return value is evaluated and held first, then `finally` runs, and then the value is returned. If `finally` itself contains a return statement, it overrides the try block's return, and worse, it silently swallows any exception thrown in the try block, so the caller never sees the failure. For that reason returning from `finally` is considered a bug and should be avoided. Also, modifying a primitive local inside `finally` does not change the already-captured return value, whereas mutating an object is visible because the reference points to the same object. The only cases where `finally` does not run are `System.exit()`, a JVM crash, or the thread being killed.

#### Easy memory

```
return in try, print in finally  → finally runs FIRST, then returns  ✅
return in BOTH                   → finally's return WINS             ⚠️
exception + return in finally    → exception SWALLOWED               💥 never do this
primitive changed in finally     → return value unchanged (already copied)
object mutated in finally        → change IS visible (same reference)

finally skipped only by: System.exit(), JVM crash
```

---
## Java 8+ Features

### Q: What are lambda expressions?

The easiest way to remember:

```
Lambda = a method WITHOUT a name, written in one line

(parameters) -> { body }
```

#### The before-and-after that explains everything

**Before Java 8** — 5 lines just to say "print hello":

```java
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};
```

**With a lambda** — 1 line:

```java
Runnable r = () -> System.out.println("Hello");
```

All the ceremony (`new Runnable()`, `@Override`, `public void run()`) was noise. The only real logic was one line.

```
Anonymous class → 5 lines of boilerplate + 1 line of logic
Lambda          → 1 line of logic ✅
```

#### Another everyday example — sorting

```java
// Before
Collections.sort(names, new Comparator<String>() {
    public int compare(String a, String b) {
        return a.compareTo(b);
    }
});

// Lambda
Collections.sort(names, (a, b) -> a.compareTo(b));

// Method reference (even shorter)
Collections.sort(names, String::compareTo);
```

#### Lambda syntax variations

```java
() -> System.out.println("hi")            // no parameter

x -> x * 2                                // one parameter (brackets optional)

(x, y) -> x + y                           // two parameters

(int x, int y) -> x + y                   // explicit types (rarely needed)

(x, y) -> {                               // multi-line needs braces + return
    int sum = x + y;
    return sum;
}
```

```
Single expression  → no braces, no `return` (it returns automatically)
Multiple lines     → braces + explicit `return`
```

#### Where can a lambda be used? — functional interfaces only ⭐

A lambda can only be assigned to an interface that has **exactly one abstract method**.

```java
@FunctionalInterface
interface Calculator {
    int operate(int a, int b);          // exactly ONE abstract method
}
```

```java
Calculator add      = (a, b) -> a + b;
Calculator multiply = (a, b) -> a * b;

System.out.println(add.operate(10, 5));       // 15
System.out.println(multiply.operate(10, 5));  // 50
```

The `@FunctionalInterface` annotation is optional but useful — the compiler will error if someone later adds a second abstract method.

```
Interface with 1 abstract method  → lambda works ✅
Interface with 2 abstract methods → lambda impossible ❌
```

#### Real-world usage you will actually write

```java
// Filtering a list
employees.stream()
         .filter(e -> e.getSalary() > 50000)
         .forEach(e -> System.out.println(e.getName()));

// Sorting
employees.sort((a, b) -> a.getName().compareTo(b.getName()));

// Iterating a map
map.forEach((key, value) -> System.out.println(key + " = " + value));

// Starting a thread
new Thread(() -> System.out.println("running")).start();

// Removing elements
list.removeIf(name -> name.startsWith("A"));
```

#### Interview trap — variable capture

A lambda can only use local variables that are **final or effectively final**:

```java
int count = 0;
Runnable r = () -> System.out.println(count);   // ✅ count is never reassigned

count = 5;                                       // ❌ now the lambda won't compile
```

Why? The lambda may run later, on another thread, after the method has already ended — so Java copies the value and requires it to be stable.

Instance fields have no such restriction:

```java
class Test {
    int count = 0;                                   // field, not a local
    Runnable r = () -> System.out.println(count);    // ✅ fine
}
```

#### `this` inside a lambda

```java
class Test {
    void m() {
        Runnable anon = new Runnable() {
            public void run() {
                System.out.println(this);   // the ANONYMOUS class object
            }
        };

        Runnable lam = () -> System.out.println(this);   // the OUTER Test object ⭐
    }
}
```

A lambda does not create its own `this` — another point that shows real understanding.

#### Interview-ready answer

> A lambda expression is an anonymous function introduced in Java 8, written as parameters, an arrow, and a body. It lets us pass behaviour as an argument instead of writing verbose anonymous inner classes — for example `Runnable r = () -> System.out.println("Hello")` replaces five lines with one. A lambda can only be used where a functional interface is expected, meaning an interface with exactly one abstract method, such as `Runnable`, `Comparator`, `Predicate` or `Function`. Lambdas can only capture local variables that are final or effectively final, because the lambda may execute later, possibly on another thread. Unlike an anonymous inner class, a lambda does not have its own `this` — `this` refers to the enclosing instance.

#### Easy memory

```
Lambda = nameless method → (params) -> body

() -> print("hi")        no param
x  -> x * 2              one param
(x, y) -> x + y          two params
(x, y) -> { ...; return; }  multi-line

Works ONLY with a FUNCTIONAL INTERFACE (exactly 1 abstract method)
Captured locals must be final / effectively final
```

---

### Q: What are the built-in functional interfaces (Predicate / Function / Consumer / Supplier)?

The easiest way to remember by **what goes in and what comes out**:

```
Predicate<T>  → T in  → boolean out   → "is it true?"       → filter
Function<T,R> → T in  → R out         → "transform it"      → map
Consumer<T>   → T in  → NOTHING out   → "just use it"       → forEach
Supplier<T>   → nothing in → T out    → "give me one"       → factory / lazy
```

Picture it:

```
Predicate:   [T] ──→ ( test )  ──→ true/false
Function:    [T] ──→ ( apply ) ──→ [R]
Consumer:    [T] ──→ ( accept ) ──→  ✗ nothing
Supplier:     ✗   ──→ ( get )    ──→ [T]
```

#### 1. Predicate<T> — asks a yes/no question

```java
Predicate<Integer> isEven = n -> n % 2 == 0;

isEven.test(10);      // true
isEven.test(7);       // false
```

Used by `filter`:

```java
List<Integer> evens = numbers.stream()
                             .filter(n -> n % 2 == 0)
                             .toList();
```

It can be combined:

```java
Predicate<String> notEmpty = s -> !s.isEmpty();
Predicate<String> shortStr = s -> s.length() < 10;

Predicate<String> valid = notEmpty.and(shortStr);       // AND
Predicate<String> either = notEmpty.or(shortStr);       // OR
Predicate<String> invalid = notEmpty.negate();          // NOT
```

Real-world idea: a **security guard** — "Do you have an ID card?" Yes or no.

#### 2. Function<T, R> — converts one thing into another

```java
Function<String, Integer> length = s -> s.length();

length.apply("Priya");     // 5
```

Used by `map`:

```java
List<String> names = employees.stream()
                              .map(e -> e.getName())     // Employee → String
                              .toList();
```

Chaining:

```java
Function<Integer, Integer> doubleIt = x -> x * 2;
Function<Integer, Integer> addTen  = x -> x + 10;

doubleIt.andThen(addTen).apply(5);    // (5*2)+10 = 20   → double FIRST
doubleIt.compose(addTen).apply(5);    // (5+10)*2 = 30   → addTen FIRST
```

Real-world idea: a **juicer** — apples go in, juice comes out.

#### 3. Consumer<T> — takes it and returns nothing

```java
Consumer<String> print = s -> System.out.println(s);

print.accept("Hello");     // Hello
```

Used by `forEach`:

```java
list.forEach(name -> System.out.println(name));

map.forEach((k, v) -> System.out.println(k + "=" + v));   // BiConsumer
```

Real-world idea: a **dustbin** — you put things in, nothing comes back out.

#### 4. Supplier<T> — gives you something, asks for nothing

```java
Supplier<String> greeting = () -> "Hello World";

greeting.get();      // Hello World
```

The important real use is **lazy evaluation**:

```java
// ❌ getDefault() runs even when the value EXISTS
optional.orElse(getDefault());

// ✅ getDefault() runs ONLY when the value is missing
optional.orElseGet(() -> getDefault());
```

```
orElse    → always computes the fallback (eager)
orElseGet → computes it only if needed  (lazy) ⭐
```

Also used for exceptions:

```java
repo.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Not found"));
```

Real-world idea: a **water tap** — you don't give it anything, you just get water when you open it.

#### Summary table

| Interface | Method | Input | Output | Stream usage |
|---|---|---|---|---|
| `Predicate<T>` | `test()` | T | `boolean` | `filter()` |
| `Function<T,R>` | `apply()` | T | R | `map()` |
| `Consumer<T>` | `accept()` | T | void | `forEach()` |
| `Supplier<T>` | `get()` | — | T | `orElseGet()`, `generate()` |
| `BiFunction<T,U,R>` | `apply()` | T, U | R | `reduce()`, `merge()` |
| `UnaryOperator<T>` | `apply()` | T | T | `replaceAll()` |
| `BinaryOperator<T>` | `apply()` | T, T | T | `reduce()` |

#### All four in one stream

```java
List<String> result = employees.stream()
    .filter(e -> e.getSalary() > 50000)     // Predicate
    .map(e -> e.getName())                  // Function
    .peek(n -> System.out.println(n))       // Consumer
    .toList();

String value = Optional.ofNullable(input)
                       .orElseGet(() -> "default");   // Supplier
```

#### Interview-ready answer

> Java 8 added a set of standard functional interfaces in `java.util.function` so we don't have to define our own for common cases. `Predicate<T>` takes a value and returns a boolean, and it is what `filter()` uses. `Function<T,R>` takes one type and returns another, which is what `map()` uses. `Consumer<T>` takes a value and returns nothing, used by `forEach()`. `Supplier<T>` takes nothing and returns a value, used for lazy evaluation such as `orElseGet()` and `orElseThrow()`. There are also `BiFunction` for two inputs, and `UnaryOperator` and `BinaryOperator` where the input and output types are the same. Predicates and Functions can also be combined using `and`, `or`, `negate`, `andThen` and `compose`.

#### Easy memory

```
Predicate → true/false → filter()   → security guard: "ID card?" ✅/❌
Function  → convert    → map()      → juicer: apple → juice
Consumer  → take only  → forEach()  → dustbin: goes in, nothing out
Supplier  → give only  → orElseGet()→ tap: nothing in, water out

orElse    = eager  (always computes)
orElseGet = lazy   (computes only when needed) ⭐
```

---

### Q: Explain the Stream API with filter / map / collect / reduce examples.

The easiest way to remember:

```
Stream = a PIPELINE that processes a collection step by step

source → filter (keep some) → map (change each) → collect/reduce (final result)
```

#### The picture

```
   List of Employees
         │
         ↓
   ┌──────────┐
   │  filter  │   keep only salary > 50000     (Predicate)
   └────┬─────┘
        ↓
   ┌──────────┐
   │   map    │   Employee → name              (Function)
   └────┬─────┘
        ↓
   ┌──────────┐
   │  sorted  │   arrange them
   └────┬─────┘
        ↓
   ┌──────────┐
   │ collect  │   put into a List              (terminal)
   └────┬─────┘
        ↓
   List of names ✅
```

Real-world idea: a **fruit juice factory conveyor belt**.

```
Basket of fruits
   → filter: throw away rotten ones
   → map: squeeze each into juice
   → collect: fill the bottles
```

Nothing moves on the belt until you switch the machine on — that switch is the **terminal operation**.

#### Before vs after

```java
// Old way
List<String> names = new ArrayList<>();
for (Employee e : employees) {
    if (e.getSalary() > 50000) {
        names.add(e.getName());
    }
}
Collections.sort(names);

// Stream way
List<String> names = employees.stream()
        .filter(e -> e.getSalary() > 50000)
        .map(Employee::getName)
        .sorted()
        .toList();
```

Same result, but the stream version reads like the requirement sentence.

#### filter — keep what you want

```java
List<Integer> evens = numbers.stream()
                             .filter(n -> n % 2 == 0)
                             .toList();

// numbers = [1,2,3,4,5,6]  →  [2,4,6]
```

#### map — transform every element

```java
List<String> upper = names.stream()
                          .map(String::toUpperCase)
                          .toList();

// [priya, ravi] → [PRIYA, RAVI]
```

`flatMap` flattens nested structures:

```java
List<List<String>> nested = List.of(List.of("a","b"), List.of("c","d"));

List<String> flat = nested.stream()
                          .flatMap(List::stream)
                          .toList();          // [a, b, c, d]
```

```
map     → 1 element → 1 element
flatMap → 1 element → MANY elements, flattened into one stream
```

#### collect — build the final result

```java
// to a List
List<String> list = stream.collect(Collectors.toList());
List<String> list2 = stream.toList();                    // Java 16+ ⭐

// to a Set
Set<String> set = stream.collect(Collectors.toSet());

// to a Map
Map<Integer, String> map = employees.stream()
        .collect(Collectors.toMap(Employee::getId, Employee::getName));

// joining into one String
String joined = names.stream().collect(Collectors.joining(", "));
// "Priya, Ravi, Meena"

// GROUPING — very common in interviews ⭐
Map<String, List<Employee>> byDept = employees.stream()
        .collect(Collectors.groupingBy(Employee::getDept));

// counting per group
Map<String, Long> countByDept = employees.stream()
        .collect(Collectors.groupingBy(Employee::getDept, Collectors.counting()));

// average salary per department
Map<String, Double> avgByDept = employees.stream()
        .collect(Collectors.groupingBy(Employee::getDept,
                 Collectors.averagingDouble(Employee::getSalary)));

// partition into true / false groups
Map<Boolean, List<Employee>> highLow = employees.stream()
        .collect(Collectors.partitioningBy(e -> e.getSalary() > 50000));
```

#### reduce — squeeze everything into ONE value

```java
// sum
int sum = numbers.stream().reduce(0, (a, b) -> a + b);
int sum2 = numbers.stream().mapToInt(Integer::intValue).sum();   // simpler ✅

// maximum
Optional<Integer> max = numbers.stream().reduce(Integer::max);

// total salary
double total = employees.stream()
                        .mapToDouble(Employee::getSalary)
                        .sum();
```

How reduce works step by step:

```
numbers = [1, 2, 3, 4]     identity = 0

0 + 1 = 1
1 + 2 = 3
3 + 3 = 6
6 + 4 = 10   ← final result
```

#### Other useful operations

```java
.distinct()                     // remove duplicates
.sorted()                       // natural order
.sorted(Comparator.comparing(Employee::getSalary).reversed())
.limit(5)                       // first 5
.skip(2)                        // skip the first 2
.count()                        // how many
.anyMatch(e -> e.getAge() > 60) // boolean
.allMatch(...)  .noneMatch(...)
.findFirst()                    // Optional
.max(Comparator.comparing(Employee::getSalary))
```

#### A realistic combined example

```java
// Top 3 highest-paid employees in the IT department, as names
List<String> top3 = employees.stream()
        .filter(e -> "IT".equals(e.getDept()))
        .sorted(Comparator.comparing(Employee::getSalary).reversed())
        .limit(3)
        .map(Employee::getName)
        .toList();
```

Read it top to bottom — it is literally the sentence above.

#### Important behaviour — lazy evaluation ⭐

```java
employees.stream()
         .filter(e -> {
             System.out.println("filtering " + e.getName());
             return e.getSalary() > 50000;
         });
// prints NOTHING — no terminal operation!
```

Nothing runs until a terminal operation (`collect`, `forEach`, `count`, `reduce`…) is called.

#### Two more rules

```
1. A stream can be consumed only ONCE
       Stream<String> s = list.stream();
       s.forEach(...);
       s.forEach(...);       // 💥 IllegalStateException

2. Streams do NOT modify the source collection — they produce a new result
```

#### Interview-ready answer

> The Stream API, introduced in Java 8, lets us process collections in a declarative pipeline instead of writing explicit loops. A pipeline has a source, zero or more intermediate operations like `filter`, `map`, `sorted`, `distinct` and `limit`, and one terminal operation such as `collect`, `reduce`, `forEach` or `count`. Intermediate operations are lazy — nothing executes until the terminal operation runs, which allows optimisations like short-circuiting. `filter` takes a `Predicate` and keeps matching elements, `map` takes a `Function` and transforms each element, `collect` gathers the result into a collection, and `reduce` combines everything into a single value. `Collectors.groupingBy` is especially useful, for example grouping employees by department and counting or averaging within each group. A stream can only be consumed once and it never modifies the source collection.

#### 🔵 In your RoboGebra project — `groupingBy` in production ⭐

**File:** `common/service/scheduler/CleanupExpiredSessionsJob.java`

```java
List<UserActiveSessionEntity> allSessions = userActiveSessionRepository.findAll();

// group sessions by user, so each user's sessions can be processed as a batch ⭐
Map<String, List<UserActiveSessionEntity>> sessionsByCognitoId = allSessions.stream()
        .collect(Collectors.groupingBy(UserActiveSessionEntity::getCognitoId));

for (Map.Entry<String, List<UserActiveSessionEntity>> entry : sessionsByCognitoId.entrySet()) {
    ...
}
```

**File:** `domain/institutionlearning/service/InstitutionChapterLessonService.java` — the **three-argument** form:

```java
Map<String, List<InstitutionChapterLessonEntity>> byLesson = rows.stream()
        .collect(Collectors.groupingBy(
                InstitutionChapterLessonEntity::getInstitutionLessonId,
                LinkedHashMap::new,              // ⭐ PRESERVE insertion order
                Collectors.toList()));
```

```java
List<String> klassIds = lessonRows.stream()
        .map(InstitutionChapterLessonEntity::getKlassId)
        .filter(Objects::nonNull)                 // ⭐ null-safe before collecting
        ...
```

⭐ **The `LinkedHashMap::new` is the detail worth pointing at** — a plain `groupingBy` returns a `HashMap` with no ordering, so the lessons would come back in an arbitrary order and the UI would look random on every request.

> 🗣️ *"We use `groupingBy` a lot — the session-cleanup job groups sessions by user so each user is processed as a batch instead of one row at a time. Where the order matters we use the three-argument form with `LinkedHashMap::new`, because the default `HashMap` would scramble the lesson order between requests."*

⭐ **847 stream usages** across the backend.

#### Easy memory

```
source → filter (keep) → map (change) → sorted → collect / reduce (result)

filter  → Predicate → keeps some
map     → Function  → changes each
flatMap → flattens nested lists
collect → toList / toSet / toMap / joining / groupingBy ⭐
reduce  → many values → ONE value

LAZY: nothing runs until the terminal operation.
A stream is used ONCE only.
```

---

### Q: What are method references?

The easiest way to remember:

```
Method reference = an even SHORTER lambda,
when the lambda does nothing but call one existing method.

ClassName::methodName
```

#### The idea in one comparison

```java
list.forEach(s -> System.out.println(s));    // lambda
list.forEach(System.out::println);           // method reference ✅
```

The lambda's whole body was just "call println with s". So why repeat `s` twice? Just point at the method.

```
s -> System.out.println(s)
              ↓ same thing
   System.out::println
```

#### The 4 types

**1. Static method** → `ClassName::staticMethod`

```java
list.stream().map(s -> Integer.parseInt(s));   // lambda
list.stream().map(Integer::parseInt);          // reference ✅
```

**2. Instance method of a particular object** → `object::method`

```java
Printer p = new Printer();

list.forEach(s -> p.print(s));      // lambda
list.forEach(p::print);             // reference ✅
```

**3. Instance method of an arbitrary object of a type** → `ClassName::instanceMethod` ⭐

This is the confusing one, so look carefully:

```java
list.stream().map(s -> s.toUpperCase());   // lambda
list.stream().map(String::toUpperCase);    // reference ✅
```

Here `String` is the **type**, and the method is called **on each element**.

```
s -> s.toUpperCase()
     ↑
   the parameter itself becomes the receiver

String::toUpperCase
```

Same idea with your own class:

```java
employees.stream().map(Employee::getName);      // e -> e.getName()
employees.sort(Comparator.comparing(Employee::getSalary));
```

**4. Constructor** → `ClassName::new`

```java
Supplier<ArrayList<String>> s = ArrayList::new;   // () -> new ArrayList<>()

list.stream()
    .map(Employee::new)                            // name -> new Employee(name)
    .toList();

stream.collect(Collectors.toCollection(TreeSet::new));
```

#### Cheat sheet

| Type | Syntax | Lambda equivalent |
|---|---|---|
| Static | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| Instance of a specific object | `System.out::println` | `s -> System.out.println(s)` |
| Instance of the parameter | `String::toUpperCase` | `s -> s.toUpperCase()` |
| Constructor | `Employee::new` | `n -> new Employee(n)` |

#### When you CANNOT use it

```java
// ❌ extra logic → keep the lambda
list.stream().map(s -> s.toUpperCase() + "!");

// ❌ multiple statements
list.forEach(s -> { validate(s); save(s); });

// ❌ argument order changed
list.stream().map(s -> compare(b, s));
```

Rule: if the lambda body is **exactly one existing method call with the same arguments**, use a method reference. Otherwise keep the lambda.

#### Interview-ready answer

> A method reference is shorthand for a lambda whose body only calls one existing method. There are four kinds: a reference to a static method like `Integer::parseInt`, a reference to an instance method of a particular object like `System.out::println`, a reference to an instance method of an arbitrary object of a type like `String::toUpperCase` where the stream element becomes the receiver, and a constructor reference like `Employee::new`. They make stream pipelines more readable — for example `map(Employee::getName)` instead of `map(e -> e.getName())`. If the lambda does anything more than a single method call with the same arguments, we keep the lambda instead.

#### Easy memory

```
Lambda body = ONE existing method call?  → use a method reference

Integer::parseInt      static
System.out::println    specific object
String::toUpperCase    the parameter itself is the receiver ⭐
Employee::new          constructor

s -> System.out.println(s)   ≡   System.out::println
e -> e.getName()             ≡   Employee::getName
```

---

### Q: What is Optional and why use it?

The easiest way to remember:

```
Optional = a BOX that may contain a value, or may be empty.

It forces the caller to think about "what if there is nothing?"
```

#### The problem it solves

```java
Employee e = repo.findById(101);
System.out.println(e.getName());        // 💥 NullPointerException if not found
```

`NullPointerException` is famously called the "billion dollar mistake". The method signature `Employee findById(...)` tells you **nothing** about the possibility of null — so callers forget to check.

```java
Optional<Employee> e = repo.findById(101);   // ⭐ the signature itself warns you
```

Now the compiler makes you deal with the empty case.

Real-world idea: a **gift box**.

```
┌────────────┐          ┌────────────┐
│  🎁 gift   │          │  (empty)   │
└────────────┘          └────────────┘
 Optional.of(x)       Optional.empty()

Either way, you receive a BOX — you must open it to find out.
```

#### Creating an Optional

```java
Optional<String> a = Optional.of("Priya");           // never null, else NPE
Optional<String> b = Optional.ofNullable(maybeNull); // null → empty ✅
Optional<String> c = Optional.empty();               // deliberately empty
```

#### Using it — the RIGHT way ⭐

```java
// 1. default value
String name = optional.orElse("Unknown");

// 2. lazy default (computed only when empty)
String name = optional.orElseGet(() -> loadDefault());

// 3. throw a meaningful exception  ← most common in Spring
Employee e = repo.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Employee " + id + " not found"));

// 4. do something only if present
optional.ifPresent(emp -> System.out.println(emp.getName()));

// 5. if present ... else ... (Java 9)
optional.ifPresentOrElse(
    emp -> System.out.println(emp.getName()),
    ()  -> System.out.println("Not found"));

// 6. transform safely
String name = optional.map(Employee::getName).orElse("Unknown");

// 7. filter
optional.filter(emp -> emp.getSalary() > 50000)
        .ifPresent(emp -> System.out.println("High earner"));
```

The `map` chain is the real power — no null checks anywhere:

```java
String city = Optional.ofNullable(employee)
        .map(Employee::getAddress)
        .map(Address::getCity)
        .orElse("Unknown");
```

Compare with the old code:

```java
String city = "Unknown";
if (employee != null) {
    if (employee.getAddress() != null) {
        if (employee.getAddress().getCity() != null) {
            city = employee.getAddress().getCity();
        }
    }
}
```

Three nested ifs become one readable chain.

#### The WRONG way ❌

```java
if (optional.isPresent()) {
    System.out.println(optional.get());     // ❌ you just recreated the null check
}
```

This gains nothing over `if (x != null)`. Use `ifPresent`, `map`, `orElse`, `orElseThrow` instead.

```java
optional.get();      // ❌ never call this without checking — throws NoSuchElementException
```

#### Where NOT to use Optional ⭐

```java
class Employee {
    private Optional<String> name;      // ❌ never as a field (not serializable, wasteful)
}

void process(Optional<String> name) {   // ❌ never as a method parameter
}

Optional<List<String>> getNames();      // ❌ return an EMPTY LIST instead
```

Correct usage:

```
✅ As a RETURN TYPE when a value may legitimately be absent
❌ As a field
❌ As a method parameter
❌ For collections — return an empty list instead
```

#### Real Spring Data usage

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
}

public Employee getByEmail(String email) {
    return repo.findByEmail(email)
               .orElseThrow(() -> new ResourceNotFoundException("No user: " + email));
}
```

#### Interview-ready answer

> `Optional` is a container introduced in Java 8 that may or may not hold a non-null value. Its purpose is to make the absence of a value explicit in the method signature so callers cannot silently forget a null check — this is why Spring Data repositories return `Optional<Employee>` from `findById`. We create it with `Optional.of`, `ofNullable` or `empty`, and consume it with `orElse`, `orElseGet`, `orElseThrow`, `ifPresent` and `map`. The `map` chain is especially useful for nested objects, replacing several nested null checks with one readable chain. The anti-pattern is calling `isPresent()` followed by `get()`, because that is just a null check again. Optional is intended as a return type, not as a field or a method parameter, and for collections we return an empty collection rather than an Optional.

#### 🔵 In your RoboGebra project — `orElseThrow` everywhere ⭐

```java
// domain/solutionstepfeedback/service/SolutionStepFeedbackService.java
SolutionStepFeedback feedback = repository.findById(request.getSolutionStepFeedbackId())
        .orElseThrow(() -> new ResourceNotFoundException(
                "Solution Step Feedback", request.getSolutionStepFeedbackId()));   // ⭐

// security/GoogleAuthSuccessHandler.java
User user = userRepository.findByGoogleId(googleId)
        .orElseThrow(() -> new RuntimeException("User not found after OAuth: " + googleId));

// common/service/FileService.java
String extension = extractExtension(fileName)
        .orElseThrow(() -> new IllegalArgumentException("Invalid file name or extension."));
```

```
The pattern, everywhere:

repository.findById(id)          → returns Optional<T> ⭐ (Spring Data)
        .orElseThrow(() -> new ResourceNotFoundException("Exercise", id))
                                 ↓
        caught by @ControllerAdvice → a clean 404 with our error model ⭐
```

> 🗣️ *"Spring Data repositories return `Optional`, and we always resolve it with `orElseThrow` and a domain exception rather than `.get()` or an `isPresent()` check. That gives one line at the call site and a correct 404 from the controller advice — the absence of a record is handled once, at the boundary, instead of null-checked in every service."*

⭐ Note the **lazy** supplier form: `orElseThrow(() -> new ...)` builds the exception **only when the value is missing**. `orElseThrow(new ...)` would construct it every single time.

#### Easy memory

```
Optional = a gift BOX: may hold a value, may be empty

CREATE : of() | ofNullable() ⭐ | empty()
USE    : orElse() | orElseGet() | orElseThrow() ⭐ | ifPresent() | map()

❌ isPresent() + get()   → that's just a null check again
❌ field / parameter     → RETURN TYPE only
❌ Optional<List>        → return an empty list instead
```

---

### Q: Why were default methods added in Java 8?

The easiest way to remember:

```
To add NEW methods to old interfaces WITHOUT breaking the millions of
classes that already implement them.
```

#### The exact problem Java faced

Java 8 wanted to add `stream()` to every collection:

```java
list.stream()...
set.stream()...
```

`stream()` had to go into the `Collection` interface. But:

```java
interface Collection<E> {
    Stream<E> stream();          // ❌ new ABSTRACT method
}
```

```
Collection is implemented by:
  ArrayList, LinkedList, HashSet, TreeSet, Vector, ...
  + thousands of custom classes in every company's codebase
        ↓
  ALL of them would fail to compile 💥
```

That is called "breaking backward compatibility" — completely unacceptable for Java.

#### The solution

```java
interface Collection<E> {

    default Stream<E> stream() {                    // ✅ has a BODY
        return StreamSupport.stream(spliterator(), false);
    }
}
```

```
Old classes → inherit the default implementation → still compile ✅
New classes → can override it if they want       ✅
```

That single feature is what made the whole Stream API possible.

#### Simple demonstration

```java
interface Payment {

    void pay(double amount);                 // abstract

    default void receipt() {                 // NEW method, safe to add
        System.out.println("Receipt generated");
    }
}

class UpiPayment implements Payment {
    public void pay(double amount) {
        System.out.println("UPI " + amount);
    }
    // did not write receipt() — inherits it for free ✅
}
```

Real-world idea: a **mobile network operator** adds a new free service (caller tunes). Existing customers get it automatically without changing their SIM card. Anyone who wants a custom version can configure their own.

#### The diamond problem it creates

```java
interface A { default void hello() { System.out.println("A"); } }
interface B { default void hello() { System.out.println("B"); } }

class C implements A, B { }        // ❌ compile error: which hello()?
```

```
   A.hello()      B.hello()
        \            /
         ↓          ↓
             C   ← ambiguous
```

You must resolve it:

```java
class C implements A, B {
    public void hello() {
        A.super.hello();          // pick one explicitly ⭐
    }
}
```

#### Resolution rules (nice to know)

```
1. The CLASS wins over any interface
2. The more SPECIFIC interface wins over its parent interface
3. Otherwise → you must override and choose
```

#### Does this mean Java now has multiple inheritance?

```
Multiple inheritance of BEHAVIOUR  → ✅ yes (default methods have bodies)
Multiple inheritance of STATE      → ❌ no  (interfaces still have no instance fields)
```

That is exactly why the classic diamond problem is still avoided — there is no duplicated state, only a method-choice question that the compiler makes you answer.

#### Interview-ready answer

> Default methods were added in Java 8 to allow interfaces to evolve without breaking existing implementations. Java wanted to add `stream()` to the `Collection` interface, but adding an abstract method would have broken every class implementing `Collection`, including thousands of custom classes. By giving the method a default implementation in the interface, existing classes inherit it automatically and still compile, while new classes can override it. The trade-off is the diamond problem: if a class implements two interfaces that declare the same default method, the compiler reports an ambiguity and the class must override the method and pick one using `InterfaceName.super.method()`. Java allows multiple inheritance of behaviour this way, but still not of state, since interfaces cannot have instance fields.

#### Easy memory

```
Reason: add stream() to Collection WITHOUT breaking existing classes

New ABSTRACT method  → all implementers break 💥
New DEFAULT method   → all implementers inherit it ✅

Diamond: A.hello() + B.hello() → C must override → A.super.hello()

Behaviour inheritance ✅ | State inheritance ❌
```

---

### Q: Intermediate vs terminal stream operations?

The easiest way to remember:

```
Intermediate → returns a STREAM   → LAZY   → nothing happens yet
Terminal     → returns a RESULT   → EAGER  → the whole pipeline RUNS
```

#### The picture

```
list.stream()
    .filter(...)      ← intermediate  (just recorded)
    .map(...)         ← intermediate  (just recorded)
    .sorted()         ← intermediate  (just recorded)
    .collect(...)     ← TERMINAL ⚡ everything actually executes NOW
```

Real-world idea: a **washing machine**.

```
Put in clothes        }
Add detergent         }  ← setting knobs (intermediate)
Select the wash cycle }
Press START           ← terminal — only now does anything happen ⚡
```

You can turn the knobs all day; without START nothing washes.

#### Proof of laziness

```java
List<String> names = List.of("Priya", "Ravi", "Meena");

names.stream()
     .filter(n -> {
         System.out.println("filtering " + n);
         return n.startsWith("P");
     });

System.out.println("Done");
```

Output:

```
Done
```

Not a single "filtering" line printed — there is no terminal operation.

Now add one:

```java
names.stream()
     .filter(n -> { System.out.println("filtering " + n); return n.startsWith("P"); })
     .toList();
```

Output:

```
filtering Priya
filtering Ravi
filtering Meena
```

#### Short-circuiting — why laziness is powerful ⭐

```java
List<String> names = List.of("Priya", "Ravi", "Meena", "Arun");

Optional<String> first = names.stream()
     .filter(n -> { System.out.println("checking " + n); return n.startsWith("R"); })
     .findFirst();
```

Output:

```
checking Priya
checking Ravi          ← STOPS here! Meena and Arun are never checked ✅
```

It processes **element by element**, not stage by stage. A `for` loop with a `break` does the same thing, but the stream does it automatically.

```
Priya → filter → no
Ravi  → filter → YES → findFirst is satisfied → STOP ⚡
Meena → never touched
Arun  → never touched
```

#### The lists

**Intermediate (lazy, return a Stream):**

```
filter()      keep matching elements
map()         transform each element
flatMap()     flatten nested structures
distinct()    remove duplicates
sorted()      sort
peek()        look at elements (debugging)
limit(n)      keep the first n        ← short-circuiting
skip(n)       drop the first n
takeWhile() / dropWhile()   (Java 9)
```

**Terminal (eager, return a result):**

```
collect()     build a collection
toList()      Java 16+
forEach()     do something with each
reduce()      combine into one value
count()       how many
min() / max() Optional
findFirst() / findAny()     ← short-circuiting
anyMatch() / allMatch() / noneMatch()   ← short-circuiting
sum() / average()   (on IntStream etc.)
```

#### The "stream is used once" rule

```java
Stream<String> s = names.stream();

s.forEach(System.out::println);      // ✅ works
s.forEach(System.out::println);      // 💥 IllegalStateException:
                                     //    stream has already been operated upon
```

After the terminal operation the stream is closed. Create a fresh one if you need it again.

#### Interview-ready answer

> Intermediate operations such as `filter`, `map`, `sorted`, `distinct` and `limit` return another stream, so they can be chained, and they are lazy — they simply build the pipeline and do not execute anything. Terminal operations such as `collect`, `forEach`, `reduce`, `count`, `findFirst` and `anyMatch` return a result rather than a stream and trigger the execution of the entire pipeline. Because of laziness, streams process elements one at a time through the whole chain and can short-circuit: `findFirst` stops as soon as a match is found, so the remaining elements are never evaluated. A stream can only be consumed once — calling a second terminal operation on the same stream throws `IllegalStateException`.

#### Easy memory

```
Intermediate → returns a STREAM → LAZY  → filter, map, sorted, distinct, limit
Terminal     → returns a RESULT → RUNS  → collect, forEach, count, reduce, findFirst

Washing machine: turning the knobs = intermediate | pressing START = terminal ⚡

Short-circuit: findFirst / anyMatch / limit stop early
One stream = ONE terminal operation only
```

---
## Multithreading

> A much deeper version of this section (thread pools, CompletableFuture, atomic classes, producer-consumer) is in **[32-multithreading.md](32-multithreading.md)**.

### Q: Thread vs Runnable — which is better?

The easiest way to remember:

```
Runnable is better ✅

extends Thread   → uses up your ONE inheritance slot
implements Runnable → keeps it free, and separates the TASK from the WORKER
```

#### Two ways to create a thread

```java
// Way 1 — extend Thread
class MyThread extends Thread {
    public void run() {
        System.out.println("Running: " + Thread.currentThread().getName());
    }
}
new MyThread().start();

// Way 2 — implement Runnable ⭐
class MyTask implements Runnable {
    public void run() {
        System.out.println("Running: " + Thread.currentThread().getName());
    }
}
new Thread(new MyTask()).start();

// Way 2 with a lambda (Runnable is a functional interface)
new Thread(() -> System.out.println("Running")).start();
```

#### Why is Runnable better? (3 reasons)

**Reason 1 — Java allows only ONE parent class**

```java
class MyThread extends Thread { }              // slot used up

class MyService extends BaseService
                extends Thread { }             // ❌ impossible
```

But:

```java
class MyService extends BaseService implements Runnable { }   // ✅ both!
```

**Reason 2 — separation of concerns**

```
Thread    = the WORKER (who runs it)
Runnable  = the TASK   (what to run)
```

Real-world idea: a **delivery company**.

```
extends Thread     → every parcel is its own delivery boy (wasteful)
implements Runnable→ parcels (tasks) are given to a pool of delivery boys (threads)
```

**Reason 3 — reusable with thread pools ⭐ (the strongest reason)**

```java
ExecutorService pool = Executors.newFixedThreadPool(5);

pool.submit(new MyTask());       // ✅ pools accept Runnable/Callable
pool.submit(() -> doWork());
```

Thread pools cannot accept a `Thread` subclass as a task. In real Spring Boot applications you almost never write `new Thread()` — you submit tasks to an executor. So `Runnable` is the only practical choice.

#### The #1 interview trap — `start()` vs `run()` ⭐⭐

```java
MyThread t = new MyThread();

t.run();       // ❌ just a normal method call — runs on the MAIN thread!
t.start();     // ✅ creates a NEW thread, which then calls run()
```

```java
public static void main(String[] args) {
    Thread t = new Thread(() ->
        System.out.println("Inside: " + Thread.currentThread().getName()));

    t.run();     // Inside: main        ← no new thread at all
    t.start();   // Inside: Thread-0    ← real thread ✅
}
```

```
start()
   ↓
JVM creates a NEW thread stack
   ↓
the new thread calls run()

run()
   ↓
just a plain method call on the CURRENT thread ❌
```

**Second trap — calling `start()` twice**

```java
t.start();
t.start();     // 💥 IllegalThreadStateException
```

A thread cannot be restarted once it has finished.

#### Comparison

| | extends Thread | implements Runnable |
|---|---|---|
| Inheritance slot | used up ❌ | free ✅ |
| Multiple interfaces | limited | ✅ |
| Works with thread pools | ❌ | ✅ |
| Lambda-friendly | ❌ | ✅ |
| Design | task = thread (coupled) | task separate from thread ✅ |
| Recommended | ❌ | ✅ |

#### Interview-ready answer

> There are two ways to create a thread: extending the `Thread` class or implementing `Runnable`. Implementing `Runnable` is preferred for three reasons. First, Java supports only single inheritance, so extending `Thread` consumes the one available parent class, whereas a class can implement `Runnable` along with extending something else. Second, it separates the task from the worker — `Runnable` represents what to do and `Thread` represents who runs it. Third and most importantly, `Runnable` works with `ExecutorService` and thread pools, which is how threading is actually done in production, since creating raw threads is expensive. Also, `Runnable` is a functional interface so it can be written as a lambda. An important point is that we must call `start()` and not `run()` — calling `run()` directly executes the code on the current thread without creating a new one.

#### Easy memory

```
implements Runnable ⭐  → inheritance slot FREE + works with thread pools + lambda
extends Thread      ❌  → wastes the single-inheritance slot

start() → NEW thread ✅        run() → same thread ❌
start() twice → IllegalThreadStateException 💥

Thread = worker | Runnable = task
```

---

### Q: What is the thread lifecycle?

The easiest way to remember the 5 states:

```
NEW → RUNNABLE → RUNNING → WAITING/BLOCKED → TERMINATED
```

#### The diagram

```
        new Thread()
             ↓
        ┌─────────┐
        │   NEW   │   object created, not started
        └────┬────┘
             │ start()
             ↓
        ┌───────────┐
        │ RUNNABLE  │  ← ready, waiting for the CPU
        └────┬──────┘
             │ the thread scheduler picks it
             ↓
        ┌───────────┐
        │  RUNNING  │  ← run() is executing
        └──┬──┬──┬──┘
           │  │  │
           │  │  └──────→ ┌──────────┐  yield() / time slice over
           │  │           │ RUNNABLE │  goes back to the queue
           │  │           └──────────┘
           │  │
           │  └─────────→ ┌──────────────────┐
           │              │ WAITING /        │  sleep(), wait(), join()
           │              │ TIMED_WAITING /  │  waiting for a lock
           │              │ BLOCKED          │
           │              └────────┬─────────┘
           │                       │ notify() / timeout / lock free
           │                       └──────→ back to RUNNABLE
           │ run() finishes
           ↓
    ┌──────────────┐
    │ TERMINATED   │  dead — cannot be restarted ❌
    └──────────────┘
```

#### The states in plain words

```
NEW            → Thread t = new Thread();   (created, not started)
RUNNABLE       → t.start() called; ready and waiting for CPU time
RUNNING        → the CPU picked it; run() is executing
BLOCKED        → waiting to ENTER a synchronized block (lock held by someone else)
WAITING        → wait(), join() with no timeout — waits indefinitely
TIMED_WAITING  → sleep(1000), wait(1000), join(1000) — waits for a fixed time
TERMINATED     → run() finished, or an exception killed it
```

> Note: officially the JVM has six `Thread.State` values — `NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, `TIMED_WAITING`, `TERMINATED`. "RUNNING" is a sub-state of `RUNNABLE` — the JVM does not distinguish "ready" from "actually on the CPU". Mentioning this scores points.

#### Real-world idea — a doctor's clinic

```
NEW           → you decide to visit the doctor (not left home yet)
RUNNABLE      → you're in the waiting room with a token
RUNNING       → you're inside, being examined
BLOCKED       → the room is occupied by another patient
TIMED_WAITING → nurse says "come back in 10 minutes"
WAITING       → "wait until we call your name" (no fixed time)
TERMINATED    → consultation over, you go home (you can't reuse the token)
```

#### Seeing states in code

```java
Thread t = new Thread(() -> {
    try { Thread.sleep(1000); } catch (InterruptedException e) { }
});

System.out.println(t.getState());    // NEW

t.start();
System.out.println(t.getState());    // RUNNABLE

Thread.sleep(100);
System.out.println(t.getState());    // TIMED_WAITING

t.join();
System.out.println(t.getState());    // TERMINATED
```

#### BLOCKED vs WAITING ⭐ (favourite follow-up)

```
BLOCKED → wants to ENTER a synchronized block, but another thread holds the lock
          → wakes up automatically when the lock is released
          → the thread did NOT choose this

WAITING → called wait() / join() and voluntarily gave up the lock
          → needs notify() / notifyAll() to wake up
          → the thread CHOSE to wait
```

#### sleep() vs wait() — another classic ⭐

| | `sleep()` | `wait()` |
|---|---|---|
| Class | `Thread` | `Object` |
| Releases the lock? | ❌ **NO** (keeps holding it) | ✅ **YES** |
| Wakes up by | timeout | `notify()` / `notifyAll()` / timeout |
| Must be inside synchronized? | ❌ no | ✅ yes |
| Used for | pausing | inter-thread communication |

The key point: **`sleep()` keeps the lock, `wait()` releases it.**

#### Interview-ready answer

> A thread starts in the NEW state when the object is created. Calling `start()` moves it to RUNNABLE, where it waits for the thread scheduler to give it CPU time; when scheduled it is effectively running. From there it can move to BLOCKED if it is waiting to acquire a lock held by another thread, to WAITING if it calls `wait()` or `join()` with no timeout, or to TIMED_WAITING if it calls `sleep()` or a timed `wait()`. When those conditions end it returns to RUNNABLE. Once `run()` completes or an uncaught exception occurs, the thread becomes TERMINATED and cannot be restarted — calling `start()` again throws `IllegalThreadStateException`. Technically the JVM defines six states, with RUNNING being part of RUNNABLE.

#### Easy memory

```
NEW → RUNNABLE → RUNNING → (BLOCKED / WAITING / TIMED_WAITING) → TERMINATED

BLOCKED → waiting for a LOCK   (forced)
WAITING → called wait()/join() (chosen)

sleep() → keeps the lock  🔒
wait()  → releases it     🔓

TERMINATED is final — a thread can never be restarted.
```

---

### Q: What does `synchronized` do?

The easiest way to remember:

```
synchronized = only ONE thread at a time can be inside.

It gives MUTUAL EXCLUSION (one at a time)
     and VISIBILITY (changes become visible to other threads).
```

#### The problem it solves — race condition

```java
class Counter {
    int count = 0;

    void increment() {
        count++;          // NOT atomic!
    }
}
```

Run it with 1000 threads and you expect 1000. You get 987, or 993 — a different wrong number every time.

Why? `count++` is actually **three** operations:

```
1. READ   count from memory   (say 5)
2. ADD    1                   (6)
3. WRITE  back to memory      (6)
```

Two threads can interleave:

```
Thread A: reads 5
Thread B: reads 5          ← both read the SAME value
Thread A: writes 6
Thread B: writes 6         ← one increment is LOST 💥

Expected 7, got 6
```

#### The fix

```java
class Counter {
    private int count = 0;

    synchronized void increment() {      // one thread at a time ✅
        count++;
    }
}
```

```
Thread A → 🔒 acquires the lock → count 5→6 → 🔓 releases
Thread B → waits...            → 🔒 → count 6→7 → 🔓

Result is always correct ✅
```

Real-world idea: a **single toilet with one key**.

```
Person A takes the key   🔑 → goes in → comes out → returns the key
Person B waits outside → then takes the key

Nobody can be inside at the same time.
```

#### Three ways to use it

**1. Synchronized method** — locks the whole object (`this`):

```java
synchronized void increment() {
    count++;
}
```

**2. Synchronized block** — locks only what you need ⭐ better:

```java
void process() {
    readConfig();                     // no lock needed — stays parallel
    
    synchronized (this) {             // lock ONLY the critical part
        count++;
    }
    
    sendEmail();                      // no lock needed
}
```

```
Method-level  → locks the ENTIRE method → slower  🐢
Block-level   → locks only the risky lines → faster ⚡
```

**3. Static synchronized** — locks the CLASS, not the object:

```java
static synchronized void update() {    // lock is Counter.class
    staticCount++;
}
```

Important difference:

```java
Counter c1 = new Counter();
Counter c2 = new Counter();

c1.increment();    // locks c1
c2.increment();    // locks c2  → these run in PARALLEL (different locks!)

Counter.update();  // locks the CLASS → all objects share this one lock
```

#### The two guarantees ⭐

```
1. MUTUAL EXCLUSION → only one thread inside at a time
2. VISIBILITY       → on release, changes are flushed to main memory,
                      so the next thread sees the latest values
```

The second one is often forgotten but is just as important.

#### Common mistakes

```java
// ❌ Locking on a String literal — it's pooled and SHARED globally!
synchronized ("lock") { }

// ❌ Locking on a changing reference
private Integer lock = 0;
synchronized (lock) { lock++; }     // the lock object CHANGES → broken

// ✅ Correct — a dedicated, final, private lock object
private final Object lock = new Object();
synchronized (lock) { }
```

#### Performance note

```java
// ❌ Whole method locked for 5 seconds
synchronized void process() {
    callSlowApi();        // 5 seconds
    count++;              // 1 microsecond
}

// ✅ Lock only the microsecond
void process() {
    callSlowApi();
    synchronized (lock) { count++; }
}
```

Modern alternatives:

```java
AtomicInteger count = new AtomicInteger();
count.incrementAndGet();          // lock-free, uses CAS — faster ⚡

ConcurrentHashMap                 // instead of synchronized HashMap
ReentrantLock                     // when you need tryLock / timeout / fairness
```

#### Interview-ready answer

> `synchronized` ensures that only one thread at a time can execute a block of code guarded by a particular lock. It solves race conditions — for example `count++` is not atomic because it is a read, an increment and a write, so two threads can read the same value and one update is lost. `synchronized` gives two guarantees: mutual exclusion, so only one thread is inside at a time, and visibility, so when a thread releases the lock its changes are flushed to main memory and become visible to the next thread. A synchronized instance method locks the object, a static synchronized method locks the class object, and a synchronized block lets us lock only the critical section, which performs much better. We should always lock on a private final object, never on a String literal or a changing reference. For simple counters `AtomicInteger` is a faster lock-free alternative, and `ReentrantLock` is used when we need `tryLock` or timeouts.

#### Easy memory

```
synchronized = ONE key to ONE toilet 🔑 → one person at a time

Gives: 1. MUTUAL EXCLUSION  2. VISIBILITY

instance method → locks THIS object
static method   → locks the CLASS
block           → locks only the critical lines ⭐ (fastest)

count++ is NOT atomic → read + add + write

Lock on: private final Object lock = new Object();  ✅
Never on: a String literal or a changing reference ❌
```

---

### Q: What is the `volatile` keyword?

The easiest way to remember:

```
volatile = "always read from MAIN memory, never from the CPU cache"

It guarantees VISIBILITY.
It does NOT guarantee ATOMICITY. ⚠️
```

#### The problem — CPU caching

Each CPU core has its own cache. A thread may read a stale copy forever.

```
        MAIN MEMORY
        flag = false
              │
      ┌───────┴────────┐
      ↓                ↓
  CPU Core 1       CPU Core 2
  cache:           cache:
  flag = false     flag = false

Thread A sets flag = true  → writes to ITS cache
Thread B keeps reading its OWN cache → still sees false → INFINITE LOOP 💥
```

The famous broken code:

```java
class Task implements Runnable {

    private boolean running = true;      // ❌ not volatile

    public void run() {
        while (running) {                // may loop FOREVER
            // work
        }
        System.out.println("Stopped");
    }

    public void stop() {
        running = false;                 // another thread may never see this
    }
}
```

The fix is one word:

```java
private volatile boolean running = true;    // ✅
```

```
        MAIN MEMORY
        running = false ⭐
              ↑ write
      ┌───────┴────────┐
   Thread A          Thread B
   writes DIRECTLY   reads DIRECTLY
   to main memory    from main memory

No caching → the change is seen immediately ✅
```

Real-world idea: a **shared office whiteboard**.

```
Without volatile → everyone keeps a personal notepad copy → they go stale
With volatile    → everyone must look at the whiteboard every time ✅
```

#### The big trap — volatile does NOT make things atomic ⭐⭐

```java
volatile int count = 0;

void increment() {
    count++;          // ❌ STILL BROKEN with 1000 threads
}
```

Why? `count++` is read + add + write — three steps. `volatile` guarantees each read and each write sees main memory, but another thread can still slip in between the read and the write.

```
Thread A: reads 5 (from main memory ✅)
Thread B: reads 5 (from main memory ✅)
Thread A: writes 6
Thread B: writes 6          ← one increment still LOST 💥
```

```
volatile → fixes VISIBILITY  ✅
volatile → does NOT fix ATOMICITY ❌
```

For a counter, use:

```java
AtomicInteger count = new AtomicInteger();
count.incrementAndGet();       // ✅ atomic AND visible
```

or `synchronized`.

#### When IS volatile enough?

```
✅ A simple flag written by one thread, read by others   (running = false)
✅ A reference published after being fully constructed
✅ Where only visibility matters, not read-modify-write

❌ Counters, or anything of the form  x = x + 1
❌ Anything needing several fields updated together
```

#### volatile vs synchronized

| | volatile | synchronized |
|---|---|---|
| Visibility | ✅ | ✅ |
| Atomicity | ❌ | ✅ |
| Blocks threads? | ❌ never | ✅ yes |
| Performance | very fast | slower |
| Works on | variables only | methods and blocks |
| Use for | flags | compound actions |

#### Double-checked locking — the classic use

```java
class Singleton {

    private static volatile Singleton instance;    // volatile is ESSENTIAL

    static Singleton getInstance() {
        if (instance == null) {                    // check 1 (no lock)
            synchronized (Singleton.class) {
                if (instance == null) {            // check 2 (with lock)
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

Without `volatile`, another thread could see a **partially constructed** object, because `new Singleton()` involves allocate → construct → assign, and the JVM is allowed to reorder those steps. `volatile` forbids that reordering.

#### Interview-ready answer

> `volatile` guarantees visibility: a volatile variable is always read from and written to main memory instead of a thread's local CPU cache, so a change made by one thread is immediately visible to all others. The classic use is a boolean stop flag — without `volatile` a worker thread may cache the flag and loop forever. However, `volatile` does not provide atomicity, so `count++` is still unsafe because it is a read-modify-write sequence and two threads can interleave; for that we need `AtomicInteger` or `synchronized`. `volatile` also prevents instruction reordering, which is why it is required in the double-checked locking singleton — without it another thread could observe a partially constructed object. So the rule is: use `volatile` for simple flags, and `synchronized` or atomic classes for compound updates.

#### Easy memory

```
volatile = read/write DIRECTLY from MAIN memory (skip the CPU cache)

✅ VISIBILITY      → the stop flag is seen immediately
❌ ATOMICITY       → count++ is STILL broken ⚠️

Whiteboard (volatile) vs personal notepad (cached) 

Flag         → volatile ✅
Counter      → AtomicInteger ✅
Multi-step   → synchronized ✅

Also needed in double-checked-locking singletons (prevents reordering).
```

---

### Q: Explain `wait()` and `notify()`.

The easiest way to remember:

```
wait()      → "I'll pause and RELEASE the lock; wake me when things change"
notify()    → "Hey, something changed — one of you can wake up"
notifyAll() → "Everyone wake up and re-check"  ⭐ safer
```

These are how two threads **talk to each other**.

#### The classic example — producer / consumer

```java
class SharedQueue {

    private final Queue<Integer> queue = new LinkedList<>();
    private final int CAPACITY = 5;

    public synchronized void produce(int item) throws InterruptedException {

        while (queue.size() == CAPACITY) {     // while, NOT if ⭐
            wait();                            // box full → pause + release lock
        }

        queue.add(item);
        System.out.println("Produced " + item);

        notifyAll();                           // tell consumers there is data
    }

    public synchronized int consume() throws InterruptedException {

        while (queue.isEmpty()) {
            wait();                            // box empty → pause + release lock
        }

        int item = queue.poll();
        System.out.println("Consumed " + item);

        notifyAll();                           // tell producers there is space
        return item;
    }
}
```

#### The flow

```
PRODUCER                          CONSUMER
   │                                 │
   │ queue is FULL                   │
   │ wait() 🔓 (releases the lock)   │
   │                                 │ takes the lock 🔒
   │                                 │ consumes an item
   │                                 │ notifyAll() 📢
   │ ← wakes up                      │ releases the lock 🔓
   │ re-checks the while condition   │
   │ produces ✅                     │
```

Real-world idea: a **restaurant kitchen counter**.

```
Chef (producer)  → puts a plate on the counter → rings the bell 🔔 (notify)
Waiter (consumer)→ waiting near the counter (wait)
                 → hears the bell → picks up the plate

Counter full? → chef waits
Counter empty?→ waiter waits
```

#### Three rules you MUST state ⭐

**Rule 1 — they must be called inside a `synchronized` block**

```java
void m() {
    wait();          // 💥 IllegalMonitorStateException
}

synchronized void m() throws InterruptedException {
    wait();          // ✅
}
```

Because `wait()` releases the lock — you cannot release a lock you never held.

**Rule 2 — always use `while`, never `if`** ⭐⭐

```java
if (queue.isEmpty()) {  wait();  }       // ❌ dangerous
while (queue.isEmpty()) { wait(); }      // ✅ correct
```

Why? Two reasons:

```
1. SPURIOUS WAKEUP — the JVM is allowed to wake a thread with no notify at all
2. notifyAll() wakes EVERY waiting thread, but by the time this thread gets
   the lock, another thread may already have taken the item
```

With `if`, the thread continues believing the condition is still true and crashes. With `while`, it re-checks and goes back to waiting.

**Rule 3 — prefer `notifyAll()` over `notify()`**

```
notify()    → wakes ONE random waiting thread
              → it might wake the WRONG kind of thread (another producer
                instead of a consumer) → everyone sleeps forever = deadlock 💥

notifyAll() → wakes all of them; each re-checks its condition → safe ✅
```

#### wait() vs sleep() ⭐ (asked constantly)

| | `wait()` | `sleep()` |
|---|---|---|
| Defined in | `Object` | `Thread` |
| Releases the lock? | ✅ **YES** | ❌ **NO** |
| Wakes up on | `notify()` / `notifyAll()` / timeout | timeout only |
| Requires synchronized? | ✅ yes | ❌ no |
| Purpose | thread communication | pause execution |

The one line to remember: **`wait()` releases the lock, `sleep()` holds on to it.**

#### The modern replacement

In real code you rarely write `wait`/`notify` today:

```java
BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(5);

queue.put(item);       // blocks automatically when full  ✅
int item = queue.take();  // blocks automatically when empty ✅
```

`BlockingQueue` does all of the above internally. Mentioning this shows practical maturity.

#### Interview-ready answer

> `wait()`, `notify()` and `notifyAll()` are defined on `Object` and are used for communication between threads. `wait()` makes the current thread pause and release the lock it holds, so another thread can proceed; `notify()` wakes one waiting thread and `notifyAll()` wakes all of them. They must be called inside a synchronized block, otherwise we get `IllegalMonitorStateException`, because a thread can only release a monitor it owns. The condition must always be checked in a `while` loop rather than an `if`, because of spurious wakeups and because another thread may consume the condition before this thread re-acquires the lock. I generally prefer `notifyAll()` since `notify()` can wake the wrong thread and cause the application to hang. The key difference from `sleep()` is that `wait()` releases the lock while `sleep()` keeps it. In modern code I would use `BlockingQueue` or the `Condition` interface instead of writing wait/notify manually.

#### Easy memory

```
wait()      → pause + RELEASE the lock 🔓
notify()    → wake ONE  (risky)
notifyAll() → wake ALL  ⭐ safer

Kitchen: chef puts a plate → rings the bell (notify) → waiter picks it up

3 RULES:
1. Must be inside synchronized  (else IllegalMonitorStateException)
2. Use WHILE, never IF          (spurious wakeups)
3. Prefer notifyAll()

wait() releases the lock 🔓  |  sleep() keeps it 🔒
Modern code: use BlockingQueue instead.
```

---

### Q: What is a deadlock and how do you avoid it?

The easiest way to remember:

```
Deadlock = two threads each holding a lock the other one needs,
           so both wait FOREVER. 🔒↔🔒
```

#### The picture

```
Thread 1                          Thread 2
   │                                 │
   │ holds Lock A 🔒                 │ holds Lock B 🔒
   │                                 │
   │ WANTS Lock B  ──────────────────┤ (held by Thread 2)
   │                                 │
   ├────────────────────── WANTS Lock A (held by Thread 1)
   │                                 │
   ↓                                 ↓
 waits forever                  waits forever   💀
```

Real-world idea — **two people, one plate of food**.

```
Person A grabs the SPOON  🥄 and waits for the FORK
Person B grabs the FORK   🍴 and waits for the SPOON

Neither will let go. Nobody eats. Forever.
```

#### Code that deadlocks

```java
class DeadlockDemo {

    private final Object lockA = new Object();
    private final Object lockB = new Object();

    public void method1() {
        synchronized (lockA) {                 // takes A
            System.out.println("Thread 1: holding A");
            sleep(100);
            synchronized (lockB) {             // wants B  ← waits forever
                System.out.println("Thread 1: holding A and B");
            }
        }
    }

    public void method2() {
        synchronized (lockB) {                 // takes B
            System.out.println("Thread 2: holding B");
            sleep(100);
            synchronized (lockA) {             // wants A  ← waits forever
                System.out.println("Thread 2: holding B and A");
            }
        }
    }
}
```

The bug is simply that the two methods take the locks in **opposite order**.

#### The 4 conditions (all must hold)

```
1. Mutual exclusion → the resource can be held by only one thread
2. Hold and wait    → a thread holds one lock while asking for another
3. No preemption    → a lock cannot be forcibly taken away
4. Circular wait    → T1 waits for T2, T2 waits for T1   ← easiest to break ⭐
```

Break any one and the deadlock disappears.

#### Solution 1 — LOCK ORDERING ⭐ (the main answer)

Make every thread take the locks in the **same order**.

```java
public void method1() {
    synchronized (lockA) {          // A first
        synchronized (lockB) { }    // then B
    }
}

public void method2() {
    synchronized (lockA) {          // A first  ← was B before ✅
        synchronized (lockB) { }    // then B
    }
}
```

```
Both threads: A → B     (never B → A)
Circular wait is impossible ✅
```

Back to the dinner table: agree that **everyone picks up the spoon first, then the fork**. Now the second person simply waits for the spoon and nobody is stuck holding half a set.

For dynamic objects (like two bank accounts), order by a unique id:

```java
void transfer(Account from, Account to, double amount) {

    Account first  = from.getId() < to.getId() ? from : to;
    Account second = from.getId() < to.getId() ? to : from;

    synchronized (first) {
        synchronized (second) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}
```

This is the classic bank-transfer deadlock question.

#### Solution 2 — `tryLock()` with a timeout

```java
ReentrantLock lockA = new ReentrantLock();
ReentrantLock lockB = new ReentrantLock();

if (lockA.tryLock(1, TimeUnit.SECONDS)) {
    try {
        if (lockB.tryLock(1, TimeUnit.SECONDS)) {
            try {
                // safe work
            } finally { lockB.unlock(); }
        } else {
            // couldn't get B → give up and RELEASE A ✅
        }
    } finally { lockA.unlock(); }
}
```

This breaks the "hold and wait" condition — the thread refuses to wait forever.

#### Solution 3 — avoid nested locks entirely

```java
// ❌ nested
synchronized (a) { synchronized (b) { } }

// ✅ one at a time
synchronized (a) { doPartOne(); }
synchronized (b) { doPartTwo(); }
```

Also: never call an unknown/external method while holding a lock — it might take another lock.

#### How do you DETECT a deadlock?

```bash
jps                 # find the process id
jstack <pid>        # thread dump → prints "Found one Java-level deadlock"
```

Or use JConsole / VisualVM → Threads → **Detect Deadlock**.

Programmatically:

```java
ThreadMXBean bean = ManagementFactory.getThreadMXBean();
long[] ids = bean.findDeadlockedThreads();
```

Mentioning `jstack` is a strong practical point.

#### Interview-ready answer

> A deadlock happens when two or more threads each hold a lock the other needs, so all of them wait forever. It requires four conditions: mutual exclusion, hold-and-wait, no preemption, and circular wait. The most practical fix is to break the circular wait by enforcing a consistent global lock ordering — if every thread acquires lock A before lock B, a cycle can never form. For dynamic resources such as two bank accounts, we order the locks by a unique identifier like the account id. Another approach is to use `ReentrantLock.tryLock()` with a timeout, so a thread that cannot get the second lock releases the first instead of waiting forever. We should also avoid nested locks where possible and never call external code while holding a lock. In production, deadlocks are diagnosed with a thread dump using `jstack`, which explicitly reports Java-level deadlocks.

#### Easy memory

```
Deadlock = T1 holds A wants B | T2 holds B wants A → both wait FOREVER 💀
           (spoon & fork: neither person lets go)

4 conditions: mutual exclusion + hold-and-wait + no preemption + CIRCULAR WAIT

FIX #1 ⭐ SAME LOCK ORDER everywhere (A then B, always)
FIX #2    tryLock(timeout) → give up instead of waiting
FIX #3    avoid nested locks

DETECT: jstack <pid>  →  "Found one Java-level deadlock"
```

---

### Q: What is ExecutorService and why use thread pools?

The easiest way to remember:

```
Thread pool = a fixed team of REUSABLE threads.

Creating a new thread per task is expensive → reuse them instead.
```

#### The problem with `new Thread()`

```java
for (int i = 0; i < 10000; i++) {
    new Thread(() -> handleRequest()).start();     // ❌ 10,000 threads!
}
```

```
Each thread costs:
  ~1 MB of stack memory
  OS-level creation time
  CPU time for context switching

10,000 threads → ~10 GB of memory → OutOfMemoryError 💥
```

Real-world idea: a **restaurant**.

```
Without a pool → hire a NEW waiter for every customer, fire him after ❌
With a pool    → keep 5 waiters; each serves customer after customer ✅
```

#### The solution

```java
ExecutorService pool = Executors.newFixedThreadPool(5);

for (int i = 0; i < 10000; i++) {
    pool.submit(() -> handleRequest());     // 10,000 tasks, only 5 threads
}

pool.shutdown();
```

```
        TASK QUEUE
   [t1][t2][t3][t4] ... [t10000]
              │
              ↓ tasks are picked up
   ┌────┬────┬────┬────┬────┐
   │ T1 │ T2 │ T3 │ T4 │ T5 │   ← only 5 threads, reused forever
   └────┴────┴────┴────┴────┘
```

#### The types of pools

```java
// 1. Fixed — N threads, extra tasks queue up  ⭐ most common
ExecutorService fixed = Executors.newFixedThreadPool(10);

// 2. Cached — creates threads as needed, reuses idle ones, kills after 60s
ExecutorService cached = Executors.newCachedThreadPool();

// 3. Single — exactly one thread, tasks run in order
ExecutorService single = Executors.newSingleThreadExecutor();

// 4. Scheduled — for delayed / repeating tasks
ScheduledExecutorService sch = Executors.newScheduledThreadPool(3);
sch.scheduleAtFixedRate(() -> cleanup(), 0, 1, TimeUnit.HOURS);

// 5. Work-stealing (Java 8) — uses ForkJoinPool
ExecutorService ws = Executors.newWorkStealingPool();
```

#### submit() vs execute()

```java
pool.execute(() -> doWork());                    // Runnable, returns nothing

Future<String> f = pool.submit(() -> "result");  // Callable, returns a Future ⭐
String value = f.get();                          // blocks until the result is ready
```

```
execute() → fire and forget → an exception is just printed
submit()  → gives you a Future → the exception is stored and rethrown by get()
```

⚠️ A trap: with `submit()`, if you never call `get()`, exceptions are **silently swallowed**.

#### Shutting down properly ⭐

```java
pool.shutdown();                    // no new tasks; finish the current ones

if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
    pool.shutdownNow();             // interrupt running tasks
}
```

```
shutdown()     → polite: finish what's running, accept nothing new ✅
shutdownNow()  → forceful: interrupt everything, return pending tasks
no shutdown    → the JVM never exits 💥 (non-daemon threads keep it alive)
```

#### Production-grade configuration ⭐

`Executors.newFixedThreadPool()` uses an **unbounded** queue, which can silently grow until you run out of memory. Serious code uses the constructor directly:

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
        5,                                   // core pool size
        10,                                  // maximum pool size
        60L, TimeUnit.SECONDS,               // idle keep-alive
        new ArrayBlockingQueue<>(100),       // BOUNDED queue ✅
        new ThreadPoolExecutor.CallerRunsPolicy());   // back-pressure when full
);
```

How many threads?

```
CPU-bound work → threads ≈ number of cores
I/O-bound work → many more (they spend time waiting on the network/DB)
```

#### In Spring Boot

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

@Async
public void sendEmail(String to) { ... }     // runs on the pool ✅
```

#### Interview-ready answer

> `ExecutorService` is the high-level API for managing threads through a thread pool. Creating a thread per task is expensive — each thread costs around a megabyte of stack plus OS-level creation and context-switching overhead — so with thousands of tasks the application would run out of memory. A thread pool keeps a fixed set of reusable threads and queues the tasks, so resources stay bounded. The common types are `newFixedThreadPool` for a fixed number of workers, `newCachedThreadPool` for many short-lived tasks, `newSingleThreadExecutor` for sequential execution, and `newScheduledThreadPool` for periodic work. `execute()` accepts a `Runnable` and returns nothing, while `submit()` accepts a `Callable` and returns a `Future` we can use to get the result or the exception. We must always call `shutdown()`, otherwise the JVM will not exit. In production I prefer configuring `ThreadPoolExecutor` directly with a bounded queue and a rejection policy, because the `Executors` factory methods use unbounded queues that can hide memory problems. In Spring Boot this is configured as a `ThreadPoolTaskExecutor` bean and used with `@Async`.

#### Easy memory

```
new Thread() per task → 1 MB each → OutOfMemory 💥
Thread pool → 5 reusable waiters serving 10,000 customers ✅

newFixedThreadPool(n)   ⭐ most common
newCachedThreadPool()   many short tasks
newSingleThreadExecutor() sequential
newScheduledThreadPool() timers

execute() → Runnable → nothing back
submit()  → Callable → Future.get() ⭐ (exceptions hide without get()!)

ALWAYS shutdown() — else the JVM never exits.
Production: ThreadPoolExecutor + BOUNDED queue + rejection policy.
```

---

### Q: Callable vs Runnable?

The easiest way to remember:

```
Runnable → returns NOTHING     → cannot throw a checked exception
Callable → RETURNS a value     → CAN throw a checked exception  ⭐
```

#### The interfaces side by side

```java
@FunctionalInterface
interface Runnable {
    void run();                       // no return, no throws
}

@FunctionalInterface
interface Callable<V> {
    V call() throws Exception;        // returns V, and CAN throw ⭐
}
```

#### Runnable — fire and forget

```java
Runnable task = () -> System.out.println("Sending email...");

pool.execute(task);        // nothing comes back
```

Use it for: logging, sending a notification, cleanup — anything where you don't need an answer.

#### Callable — gives you an answer

```java
Callable<Integer> task = () -> {
    Thread.sleep(1000);
    return 42;                       // ✅ returns a value
};

Future<Integer> future = pool.submit(task);

// ... do other work meanwhile ...

Integer result = future.get();       // blocks until ready
System.out.println(result);          // 42
```

```
submit(Callable)
      ↓
returns a Future immediately (a "receipt")   ← doesn't block ✅
      ↓
you keep working
      ↓
future.get()  ← NOW it blocks until the answer is ready
```

Real-world idea: a **dry cleaner**.

```
Runnable → you drop off clothes and walk away, expecting nothing back
Callable → you drop off clothes and get a TOKEN (Future)
           → later you show the token and collect the clothes (future.get())
```

#### Exception handling — the important difference

```java
Runnable r = () -> {
    throw new IOException();       // ❌ compile error — checked exception
};

Callable<String> c = () -> {
    throw new IOException();       // ✅ allowed
};
```

With `Callable`, the exception is stored and rethrown when you call `get()`:

```java
try {
    future.get();
} catch (ExecutionException e) {
    System.out.println("Task failed: " + e.getCause());   // the real exception ⭐
}
```

Note it is wrapped in `ExecutionException` — use `getCause()` to reach the original.

#### Useful Future methods

```java
future.get();                        // blocks forever until done
future.get(5, TimeUnit.SECONDS);     // blocks with a timeout ⭐ safer
future.isDone();                     // finished? (true even if it failed)
future.cancel(true);                 // try to cancel / interrupt
future.isCancelled();
```

Always prefer the timeout version in production — a plain `get()` can hang your thread forever.

#### Running many tasks at once

```java
List<Callable<Integer>> tasks = List.of(
    () -> compute(1),
    () -> compute(2),
    () -> compute(3));

List<Future<Integer>> results = pool.invokeAll(tasks);   // waits for ALL

for (Future<Integer> f : results) {
    System.out.println(f.get());
}

Integer fastest = pool.invokeAny(tasks);   // returns the FIRST completed result
```

#### The modern upgrade — CompletableFuture ⭐

`Future.get()` blocks, which wastes a thread. `CompletableFuture` (Java 8) is non-blocking and chainable:

```java
CompletableFuture.supplyAsync(() -> fetchUser(id))
                 .thenApply(user -> user.getName())
                 .thenAccept(name -> System.out.println(name))
                 .exceptionally(e -> { log.error("failed", e); return null; });
```

You can also combine several calls:

```java
CompletableFuture<String> a = CompletableFuture.supplyAsync(() -> callServiceA());
CompletableFuture<String> b = CompletableFuture.supplyAsync(() -> callServiceB());

CompletableFuture.allOf(a, b).join();     // both run in PARALLEL ⚡
```

This is very common in microservices — calling three services in parallel instead of one after another.

#### Comparison

| | Runnable | Callable |
|---|---|---|
| Since | Java 1.0 | Java 5 |
| Method | `run()` | `call()` |
| Returns | void | a value `V` |
| Checked exceptions | ❌ | ✅ |
| Submitted with | `execute()` or `submit()` | `submit()` only |
| Result via | — | `Future` |

#### Interview-ready answer

> `Runnable` has a `run()` method that returns nothing and cannot throw checked exceptions, so it is used for fire-and-forget tasks. `Callable` was added in Java 5; its `call()` method returns a value and is allowed to throw checked exceptions. When we submit a `Callable` to an `ExecutorService` we get back a `Future`, which acts like a receipt — the submission returns immediately and we call `future.get()` later to retrieve the result, which blocks until the task completes. If the task threw an exception, `get()` rethrows it wrapped in an `ExecutionException`, and we use `getCause()` to see the original. In production I always use the timeout version of `get()` so a hung task cannot block the caller indefinitely. For more complex flows I prefer `CompletableFuture`, which is non-blocking and lets me chain and combine asynchronous calls — very useful when calling multiple microservices in parallel.

#### Easy memory

```
Runnable → run()  → void   → no checked exceptions → execute()
Callable → call() → VALUE  → CAN throw            → submit() → Future ⭐

Dry cleaner: Runnable = drop and leave | Callable = get a TOKEN (Future)

future.get()                  → blocks forever
future.get(5, SECONDS)        → safer ⭐
exception → ExecutionException → use e.getCause()

Modern: CompletableFuture → non-blocking, chainable, parallel service calls
```

---

### Q: `synchronized` vs `Lock` (ReentrantLock)?

The easiest way to remember:

```
synchronized  → simple, automatic, but you CANNOT give up waiting
ReentrantLock → more code, but gives tryLock, timeout, fairness, interruptibility ⭐
```

#### The core limitation of synchronized

```java
synchronized (lock) {
    // if another thread holds this lock, I wait FOREVER.
    // I cannot time out. I cannot check. I cannot be interrupted.
}
```

`ReentrantLock` removes all of those limitations.

#### Basic ReentrantLock usage

```java
private final ReentrantLock lock = new ReentrantLock();

public void increment() {
    lock.lock();                 // acquire
    try {
        count++;
    } finally {
        lock.unlock();           // ⚠️ MUST be in finally
    }
}
```

⚠️ The biggest danger: forgetting `unlock()`. With `synchronized` the JVM releases the lock automatically, even if an exception is thrown. With `ReentrantLock`, if you forget the `finally`, the lock is **never released** and the application freezes.

```
synchronized  → lock released automatically ✅ (even on exception)
ReentrantLock → YOU must unlock() in a finally block ⚠️
```

#### Feature 1 — `tryLock()`: don't wait if it's busy

```java
if (lock.tryLock()) {                     // returns immediately
    try {
        doWork();
    } finally { lock.unlock(); }
} else {
    System.out.println("Busy, I'll do something else");   // ✅ no waiting
}
```

With a timeout:

```java
if (lock.tryLock(2, TimeUnit.SECONDS)) {  // wait max 2 seconds
    try { doWork(); } finally { lock.unlock(); }
} else {
    log.warn("Could not acquire lock in 2s");
}
```

This is the main tool for **avoiding deadlocks**.

Real-world idea: a **restaurant table**.

```
synchronized  → you stand at the door waiting for a table, forever
tryLock()     → you ask "is a table free?" → if not, you go to another restaurant ✅
```

#### Feature 2 — interruptible waiting

```java
lock.lockInterruptibly();     // another thread can interrupt this waiting
```

With `synchronized`, a waiting thread cannot be interrupted at all — you cannot cancel it.

#### Feature 3 — fairness

```java
ReentrantLock fair = new ReentrantLock(true);    // FIFO order
```

```
Unfair (default) → whoever grabs it first wins → faster, but a thread can starve
Fair (true)      → longest-waiting thread wins → no starvation, but slower
```

Real-world idea: a proper queue at a ticket counter (fair) versus a crowd pushing in (unfair).

#### Feature 4 — multiple conditions ⭐

`synchronized` has only one wait-set. `ReentrantLock` can have several:

```java
private final ReentrantLock lock = new ReentrantLock();
private final Condition notFull  = lock.newCondition();
private final Condition notEmpty = lock.newCondition();

public void produce(int item) throws InterruptedException {
    lock.lock();
    try {
        while (queue.size() == CAPACITY) {
            notFull.await();               // producers wait HERE
        }
        queue.add(item);
        notEmpty.signalAll();              // wake only the CONSUMERS ⭐
    } finally { lock.unlock(); }
}

public int consume() throws InterruptedException {
    lock.lock();
    try {
        while (queue.isEmpty()) {
            notEmpty.await();              // consumers wait HERE
        }
        int item = queue.poll();
        notFull.signalAll();               // wake only the PRODUCERS ⭐
        return item;
    } finally { lock.unlock(); }
}
```

```
synchronized + notifyAll() → wakes EVERYONE, including the wrong threads
Lock + 2 Conditions        → wakes exactly the right group ✅ much more efficient
```

#### Bonus — ReadWriteLock

```java
ReadWriteLock rw = new ReentrantReadWriteLock();

rw.readLock().lock();      // MANY readers at the same time ✅
rw.writeLock().lock();     // only ONE writer, and no readers
```

Perfect for a cache that is read constantly and written rarely.

#### Comparison

| | synchronized | ReentrantLock |
|---|---|---|
| Since | 1.0 | Java 5 |
| Unlock | automatic ✅ | manual, in `finally` ⚠️ |
| tryLock / timeout | ❌ | ✅ |
| Interruptible | ❌ | ✅ |
| Fairness option | ❌ | ✅ |
| Multiple conditions | ❌ (one wait-set) | ✅ |
| Read/write separation | ❌ | ✅ (`ReadWriteLock`) |
| Code readability | simpler ✅ | more verbose |
| Performance | equal in modern JVMs | equal |

Note: since Java 6, `synchronized` is heavily optimised (biased locking, lock coarsening), so performance is **not** a reason to choose one over the other. Choose based on **features**.

#### Which one do I use?

```
Default → synchronized  (simple, safe, less code) ⭐
Switch to ReentrantLock ONLY when you need:
    tryLock / timeout   (deadlock avoidance)
    interruptible lock
    fairness
    multiple conditions
    read-write separation
```

#### Interview-ready answer

> `synchronized` is the built-in keyword: the JVM acquires and releases the monitor automatically, so the lock is always released even if an exception is thrown, and the code stays simple. `ReentrantLock`, added in Java 5, is an explicit lock that must be released manually in a `finally` block, but it offers capabilities `synchronized` does not have. It supports `tryLock()` with an optional timeout, which lets a thread give up instead of waiting forever and is a standard way to avoid deadlocks. It supports interruptible locking, an optional fairness policy that prevents thread starvation, and multiple `Condition` objects, so in a producer-consumer scenario we can signal only the consumers instead of waking every waiting thread. There is also `ReentrantReadWriteLock`, which allows many concurrent readers but exclusive writers, which suits read-heavy caches. Since Java 6 both perform similarly, so I default to `synchronized` for simplicity and use `ReentrantLock` only when I need one of those extra features.

#### Easy memory

```
synchronized  → automatic unlock ✅ → simple → DEFAULT ⭐
ReentrantLock → manual unlock in finally ⚠️ → but gives:

   tryLock()          → don't wait → deadlock avoidance ⭐
   lockInterruptibly()→ can be cancelled
   new ReentrantLock(true) → fairness, no starvation
   newCondition()     → wake only the right group
   ReadWriteLock      → many readers, one writer

Restaurant: synchronized = wait at the door forever
            tryLock()    = "no table? I'll go elsewhere" ✅

⚠️ lock.lock(); try { ... } finally { lock.unlock(); }
```

---

## Memory & Garbage Collection

### Q: Stack vs heap memory?

The easiest way to remember:

```
STACK → per THREAD  → method calls + local variables + references → auto-cleaned
HEAP  → SHARED      → all OBJECTS                                 → cleaned by GC
```

#### The picture

```java
void m() {
    int x = 5;
    Person p = new Person("Priya");
}
```

```
        STACK (per thread)              HEAP (shared by all threads)
   ┌──────────────────────┐        ┌────────────────────────────┐
   │ m() frame            │        │                            │
   │   x = 5              │        │   ┌────────────────────┐   │
   │   p ──────────────────────────────→│ Person             │   │
   │                      │        │   │ name = "Priya"     │   │
   ├──────────────────────┤        │   └────────────────────┘   │
   │ main() frame         │        │                            │
   └──────────────────────┘        └────────────────────────────┘

  the VALUE 5 and the REFERENCE p       the OBJECT itself
  live on the stack                     lives on the heap
```

⭐ The key sentence: **the reference is on the stack, the object is on the heap.**

#### How the stack works — LIFO

```java
void a() { b(); }
void b() { c(); }
void c() { System.out.println("done"); }
```

```
Push:                    Pop (in reverse):
┌──────┐                 ┌──────┐
│ c()  │ ← top           │      │
├──────┤                 ├──────┤
│ b()  │                 │ b()  │
├──────┤                 ├──────┤
│ a()  │                 │ a()  │
├──────┤                 ├──────┤
│ main │                 │ main │
└──────┘                 └──────┘
```

When a method returns, its whole frame is popped — instantly, with no GC involved.

Real-world idea: a **stack of plates**. The last plate placed is the first one removed.

#### Comparison

| | Stack | Heap |
|---|---|---|
| Stores | method frames, locals, references | all objects, instance fields, arrays |
| Shared? | ❌ one per thread | ✅ shared by all threads |
| Cleanup | automatic on method return | garbage collector |
| Speed | very fast (just move a pointer) | slower (allocation + GC) |
| Size | small (~512 KB–1 MB per thread) | large (megabytes to gigabytes) |
| Thread-safe? | ✅ naturally (private) | ❌ needs synchronization |
| Error when full | `StackOverflowError` | `OutOfMemoryError` |
| Order | LIFO | no order |

#### The two errors ⭐

**StackOverflowError — infinite recursion**

```java
void recurse() {
    recurse();          // never returns
}
```

```
┌──────────┐
│ recurse()│
│ recurse()│
│ recurse()│  ← frames pile up
│ ...      │
└──────────┘  💥 StackOverflowError
```

**OutOfMemoryError — too many live objects**

```java
List<int[]> list = new ArrayList<>();
while (true) {
    list.add(new int[1_000_000]);    // the list keeps them REACHABLE
}
// 💥 OutOfMemoryError: Java heap space
```

Note the important detail: the GC cannot free them **because the list still references them**. That is exactly what a memory leak is.

#### Where do static and String literals live?

```
Java 7 and earlier → PermGen
Java 8+            → METASPACE (class metadata, outside the heap, grows dynamically)

Static variables   → in the heap, with the class object
String pool        → in the HEAP since Java 7 ⭐ (was PermGen before)
```

That is why `-XX:MaxPermSize` no longer exists — Java 8 replaced PermGen with Metaspace and removed a whole class of `OutOfMemoryError: PermGen space` failures.

#### Useful JVM flags

```bash
-Xms512m           # initial heap size
-Xmx2g             # maximum heap size
-Xss1m             # stack size per thread
-XX:+HeapDumpOnOutOfMemoryError    # dump for analysis ⭐
```

#### Interview-ready answer

> The stack is per-thread memory that stores method frames, local primitive variables and object references. It works in LIFO order, and when a method returns its frame is popped automatically, so it is very fast and needs no garbage collection. It is also naturally thread-safe because each thread has its own stack. The heap is shared across all threads and stores every object and its instance fields; it is managed by the garbage collector and requires synchronization when objects are shared. The key point is that a reference lives on the stack while the object it points to lives on the heap. Deep or infinite recursion causes `StackOverflowError`, while too many reachable objects cause `OutOfMemoryError`. Since Java 8, class metadata lives in Metaspace instead of PermGen, and the String pool has been part of the heap since Java 7.

#### Easy memory

```
STACK → per THREAD → locals + references → LIFO → auto-cleaned → StackOverflowError
HEAP  → SHARED     → OBJECTS             → GC   → slower       → OutOfMemoryError

Person p = new Person();
       ↑                ↑
   STACK (reference)  HEAP (object)

Stack of plates: last in, first out.
Java 8: PermGen → METASPACE. String pool → heap (since Java 7).
```

---

### Q: How does garbage collection work?

The easiest way to remember:

```
GC automatically deletes objects that are NO LONGER REACHABLE from a GC root.

"Not reachable" — not "not used". ⭐
```

#### What counts as reachable?

```
GC ROOTS:
  - local variables on any thread's stack
  - static fields
  - active threads
  - JNI references
```

```
GC Root (a local variable)
    │
    ↓
  Object A ────→ Object B
                     │
                     ↓
                 Object C          ← all three are REACHABLE ✅


  Object X ←──→ Object Y           ← they reference each other,
                                      but NOTHING points to them from a root
                                      → both are GARBAGE ✅ collected
```

⭐ That last point is worth saying: Java uses **reachability**, not reference counting, so circular references are collected correctly. (Older languages using reference counting leaked memory here.)

#### Example

```java
Person p = new Person("Priya");     // reachable
p = null;                           // now unreachable → eligible for GC ✅

Person a = new Person("A");
Person b = new Person("B");
a.friend = b;
b.friend = a;                       // circular
a = null;
b = null;                           // BOTH are collected ✅ (unreachable)
```

#### Generational GC — "most objects die young"

Studies showed that the vast majority of objects become garbage almost immediately (temporary variables, DTOs, strings inside a loop). So the heap is split:

```
┌──────────────── HEAP ────────────────────────────────┐
│                                                      │
│  YOUNG GENERATION            OLD GENERATION          │
│  ┌──────┬──────┬──────┐      ┌────────────────────┐  │
│  │ Eden │  S0  │  S1  │      │   Tenured          │  │
│  └──────┴──────┴──────┘      └────────────────────┘  │
│   new objects  survivors      long-lived objects     │
│                                                      │
│   MINOR GC (frequent, fast)   MAJOR GC (rare, slow)  │
└──────────────────────────────────────────────────────┘

     METASPACE (outside the heap) → class metadata
```

#### The life of an object

```
1. new Object()  →  born in EDEN

2. Eden is full  →  MINOR GC runs
                    living objects → moved to Survivor S0
                    dead objects   → deleted ✅

3. Next Minor GC →  S0 survivors → moved to S1 (age +1)

4. After surviving ~15 rounds → PROMOTED to the OLD generation

5. Old generation fills up → MAJOR / FULL GC (slow, "stop the world")
```

Real-world idea: an **office probation system**.

```
Eden      → new joiners (many leave quickly)
Survivor  → those who survive the first review
Old gen   → permanent employees (rarely reviewed, but reviewing them takes long)
```

#### Mark and Sweep

```
MARK   → walk from every GC root and mark everything reachable
SWEEP  → delete everything not marked
COMPACT→ slide the survivors together to remove fragmentation
```

```
Before:  [A][ ][B][ ][ ][C][ ]      ← fragmented
After :  [A][B][C][ ][ ][ ][ ]      ← compacted, allocation is fast again
```

#### The collectors

```
Serial GC      → one thread → tiny apps
Parallel GC    → multiple threads → throughput-focused (Java 8 default)
CMS            → concurrent, low pause → deprecated
G1 GC          → region-based, predictable pauses → DEFAULT since Java 9 ⭐
ZGC / Shenandoah → pauses under 10 ms even with huge heaps (Java 11+)
```

```bash
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
```

#### Can I force garbage collection?

```java
System.gc();          // ❌ only a REQUEST — the JVM may ignore it completely
```

Never rely on it. Calling it in production is considered a bug, because it can trigger an expensive Full GC.

#### Memory leaks in Java ⭐ (great practical point)

Java has a GC, but leaks still happen — whenever you keep an unwanted reference:

```java
// 1. A static collection that only ever grows
static List<Session> sessions = new ArrayList<>();   // nothing is ever removed 💥

// 2. Unclosed resources
Connection con = getConnection();   // never closed → use try-with-resources ✅

// 3. Listeners never unregistered
button.addListener(listener);       // never removed

// 4. Mutable keys in a HashMap
map.put(employee, data);
employee.setId(99);                 // the entry becomes unreachable but never freed
```

How to investigate:

```bash
jmap -dump:live,format=b,file=heap.hprof <pid>    # take a heap dump
# then analyse in Eclipse MAT or VisualVM
```

#### Interview-ready answer

> Garbage collection automatically reclaims heap memory occupied by objects that are no longer reachable from GC roots such as thread stacks, static fields and active threads. It uses reachability rather than reference counting, which is why objects that reference each other in a cycle are still collected once nothing points to them from a root. The heap is generational, based on the observation that most objects die young: new objects are allocated in Eden, and a Minor GC moves survivors into the survivor spaces; objects that survive enough cycles are promoted to the old generation, which is cleaned by a much slower Major or Full GC. The algorithm is mark-sweep-compact — mark everything reachable, sweep the rest, and compact to avoid fragmentation. G1 is the default collector since Java 9, and ZGC and Shenandoah offer very low pause times for large heaps. We cannot force collection — `System.gc()` is only a hint. Java can still have memory leaks when we hold unwanted references, for example in a static collection that is never cleared or an unclosed resource, so I use try-with-resources and analyse heap dumps with a tool like Eclipse MAT when memory grows unexpectedly.

#### Easy memory

```
GC deletes what is UNREACHABLE from a GC root (not "unused") ⭐
Circular references ARE collected (reachability, not counting)

HEAP: [Eden | S0 | S1]  →  [Old Generation]
      Minor GC (fast)       Major/Full GC (slow, stop-the-world)

Object life: Eden → Survivor → (survive ~15 rounds) → Old gen
Algorithm  : MARK → SWEEP → COMPACT
Default GC : G1 (Java 9+) | ZGC/Shenandoah for very low pause

System.gc() = only a REQUEST, never rely on it

LEAKS: static collections, unclosed resources, listeners, mutable map keys
Debug : jmap heap dump → Eclipse MAT
```

---

## Quick Revision Sheet — read this the night before

```
OOP
  A PIE: Abstraction, Polymorphism, Inheritance, Encapsulation
  Overloading = same name, different params  → compile time
  Overriding  = child changes parent's body  → runtime
  Abstract class = IS-A + shared state | Interface = CAN-DO + contract
  final (can't change) | finally (always runs) | finalize (deprecated)

CORE
  JDK ⊃ JRE ⊃ JVM
  String immutable → pool, security, hash-key safety, thread-safe
  StringBuilder (fast, 1 thread) | StringBuffer (safe, many threads)
  == address | equals() content  → default equals() IS ==
  equals() true → hashCode MUST match. hashCode same → equals NOT guaranteed.
  Autoboxing int→Integer | Unboxing Integer→int (null → NPE!)
  Java is ALWAYS pass-by-value (for objects, the value is the reference)

COLLECTIONS
  List (dup, ordered) | Set (unique) | Map (key→value, NOT a Collection)
  ArrayList = read fast | LinkedList = a Deque
  HashMap: hashCode→bucket, equals→entry; load factor 0.75; treeify > 8
  HashMap (1 thread) | ConcurrentHashMap (many) | Hashtable (never)
  Comparable = 1 natural order | Comparator = many orders
  Fail-fast → ConcurrentModificationException → use removeIf()

EXCEPTIONS
  Checked = compiler forces (IOException) | Unchecked = your bug (NPE)
  throw = action | throws = declaration
  try-with-resources → AutoCloseable → reverse close order → suppressed exceptions
  Never return from finally (it swallows exceptions)

JAVA 8
  Lambda → functional interface (1 abstract method)
  Predicate(filter) Function(map) Consumer(forEach) Supplier(orElseGet)
  Stream: intermediate = LAZY | terminal = RUNS
  Optional → return type only; orElseThrow ⭐; never isPresent()+get()
  default methods → added stream() to Collection without breaking anyone

THREADS
  implements Runnable > extends Thread
  start() = new thread | run() = same thread ❌
  synchronized → mutual exclusion + visibility
  volatile → visibility ONLY (count++ still broken)
  wait() releases the lock 🔓 | sleep() keeps it 🔒 | always use while + notifyAll
  Deadlock → fix with consistent LOCK ORDERING; detect with jstack
  ExecutorService → reuse threads; always shutdown()
  Callable → returns a value via Future

MEMORY
  Stack = per thread, references, StackOverflowError
  Heap  = shared, objects, OutOfMemoryError
  GC = reachability from GC roots; Eden→Survivor→Old; G1 by default
  System.gc() is only a hint
```

---

## Related files

- [31-hashmap-internals.md](31-hashmap-internals.md) — HashMap deep dive
- [32-multithreading.md](32-multithreading.md) — concurrency deep dive
- [33-interface-vs-abstract-class.md](33-interface-vs-abstract-class.md) — with real design examples
- [12-java17-features.md](12-java17-features.md) — records, sealed classes, pattern matching
- [40-java21-features.md](40-java21-features.md) — ⭐ **virtual threads**, pattern matching for `switch`, record patterns, sequenced collections
- [22-java-streams-coding-problems.md](22-java-streams-coding-problems.md) — stream coding questions
- [23-java-output-tricky-questions.md](23-java-output-tricky-questions.md) — "what is the output?" traps
- [17-solid-design-patterns.md](17-solid-design-patterns.md) — SOLID and design patterns
