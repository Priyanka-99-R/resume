# Core Java & OOP — 47 Interview Q&A (Senior-Level, Interview Perspective)

---

## Q1. What is Java?

**Summary:** Java is a high-level, object-oriented, platform-independent language that compiles to bytecode and runs on the JVM — making it one of the most widely adopted languages for enterprise backend systems.

**Practical Explanation:** In real projects, Java gives us strong typing, mature ecosystem, and write-once-run-anywhere capability. It's not just a language — it's a full ecosystem with frameworks like Spring Boot, build tools like Maven/Gradle, and a massive community.

**When/Why Used:** We use Java extensively in microservices, REST APIs, banking systems, e-commerce platforms — anywhere you need reliability, scalability, and long-term maintainability.

**Real-World Scenario:** In my projects, we've built payment processing microservices in Java using Spring Boot. The strong type system catches bugs at compile time, and the JVM's performance tuning capabilities handle millions of transactions daily.

**Common Mistakes:** Developers sometimes confuse Java with JavaScript — they're completely different. Also, people think Java is slow — modern JVMs with JIT compilation make it extremely performant.

**Closing:** Java remains the backbone of enterprise development, and its ecosystem maturity is unmatched for building scalable backend systems.

---

## Q2. Why is Java platform-independent?

**Summary:** Java achieves platform independence through bytecode — the compiler produces `.class` files that any JVM on any OS can execute, following the "Write Once, Run Anywhere" principle.

**Practical Explanation:** When we compile Java code, `javac` doesn't produce machine-specific binary — it produces bytecode. The JVM installed on each platform interprets or JIT-compiles that bytecode into native machine code at runtime. So the same `.jar` file runs on Linux, Windows, or Mac without recompilation.

**When/Why Used:** This is critical in enterprise environments where development happens on Mac/Windows but production runs on Linux servers. We build locally, run the same artifact in Docker containers on Linux in production — zero platform issues.

**Real-World Scenario:** In CI/CD pipelines, we build the JAR once on a Jenkins agent (could be any OS), and deploy the same artifact across dev, staging, and production environments running different Linux distributions. No recompilation needed.

**Common Mistakes:** People say "Java is platform-independent" — technically, Java code is platform-independent, but the JVM itself is platform-dependent. Each OS needs its own JVM installation.

**Closing:** Platform independence is one of Java's strongest value propositions and a key reason enterprises standardize on it for cross-environment deployments.

---

## Q3. What is JVM?

**Summary:** The JVM (Java Virtual Machine) is the runtime engine that executes Java bytecode, manages memory, performs garbage collection, and applies runtime optimizations like JIT compilation.

**Practical Explanation:** The JVM is not just an interpreter — it's a sophisticated runtime. It loads classes via the ClassLoader, verifies bytecode, allocates memory in Heap and Stack, runs the Garbage Collector, and uses Just-In-Time (JIT) compilation to convert hot bytecode paths into optimized native code.

**When/Why Used:** Every Java application runs on the JVM. As senior developers, we need to understand JVM internals for performance tuning — setting heap sizes (`-Xmx`, `-Xms`), choosing GC algorithms, analyzing thread dumps and heap dumps.

**Real-World Scenario:** In production, we've tuned JVM parameters like `-Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200` to optimize a high-throughput order processing service. Understanding JVM memory model helped us diagnose an OutOfMemoryError caused by a connection pool leak.

**Common Mistakes:** Confusing JVM, JRE, and JDK. Also, not monitoring JVM metrics in production — GC pause times, heap usage, thread counts — is a common oversight that leads to production incidents.

**Closing:** Deep JVM knowledge separates senior engineers from juniors — it's essential for writing performant, production-ready Java applications.

---

## Q4. JDK vs JRE?

**Summary:** JDK is the full development kit (compiler + tools + JRE), while JRE is just the runtime environment needed to run Java applications.

**Practical Explanation:** JDK includes `javac` (compiler), `jdb` (debugger), `jconsole`, `jvisualvm`, and other development/monitoring tools plus the JRE. JRE includes the JVM and standard class libraries — everything needed to run compiled Java programs, but nothing to develop them.

**When/Why Used:** On developer machines, we install JDK because we need to compile and debug. On production servers or Docker images, we traditionally needed only JRE — though with modern containerized deployments, we often use slim JDK images or custom runtimes via `jlink`.

**Real-World Scenario:** In our Dockerfiles, we use multi-stage builds — JDK in the build stage to compile, and a minimal JRE-based image for the runtime stage. This reduces our container image size from ~400MB to ~150MB.

**Common Mistakes:** Since Java 11, Oracle stopped shipping standalone JRE. Many developers don't realize this and keep looking for JRE downloads. With modern Java, you use JDK everywhere or create custom runtimes using `jlink`.

**Closing:** Understanding this distinction matters for optimizing build pipelines, Docker images, and production deployments.

---

## Q5. What is OOP?

**Summary:** OOP is a programming paradigm where we model software around objects that encapsulate data and behavior, making code modular, reusable, and easier to maintain.

**Practical Explanation:** Instead of writing procedural code with functions and data scattered everywhere, OOP lets us bundle related state and behavior into objects. A `User` object has fields like `name`, `email` and methods like `updateProfile()`, `deactivate()`. This mirrors how we think about real-world entities.

**When/Why Used:** Every Java enterprise application uses OOP. Domain-Driven Design, design patterns, Spring's dependency injection — all built on OOP principles. It helps large teams work on the same codebase by establishing clear boundaries between components.

**Real-World Scenario:** In an e-commerce system, we model `Order`, `Product`, `Customer`, `Payment` as objects. Each has its own state and behavior. When requirements change — say adding a discount feature — we modify the `Order` class without breaking `Payment` logic. That's OOP in action.

**Common Mistakes:** Over-engineering with too many abstractions. Not every piece of code needs to be an object — utility functions, simple data transformations can be functional. Also, anemic domain models where objects are just data holders with no behavior defeat the purpose of OOP.

**Closing:** OOP is the foundation of Java development — mastering it means writing code that scales with team size and project complexity.

---

## Q6. Four pillars of OOP?

**Summary:** The four pillars are Encapsulation, Inheritance, Polymorphism, and Abstraction — they work together to create modular, maintainable, and extensible software.

**Practical Explanation:**
- **Encapsulation** — Hide internal state, expose controlled access via methods.
- **Inheritance** — Reuse common behavior by extending parent classes.
- **Polymorphism** — Same interface, different implementations at runtime.
- **Abstraction** — Define "what" without "how" — hide complexity behind interfaces.

**When/Why Used:** These aren't academic concepts — we use them daily. Spring's `@Service` classes encapsulate business logic. We inherit from base classes for common functionality. We program to interfaces (polymorphism) so we can swap implementations. We abstract away infrastructure concerns behind clean APIs.

**Real-World Scenario:** Consider a notification system: `NotificationService` interface (Abstraction) with `EmailNotification` and `SMSNotification` implementations (Polymorphism). Each implementation encapsulates its own connection details (Encapsulation). Both extend `BaseNotification` for common retry logic (Inheritance).

**Common Mistakes:** Favoring inheritance over composition is the most common mistake. "Prefer composition over inheritance" — use inheritance only for true "is-a" relationships, not for code reuse.

**Closing:** These four pillars are the language of every design discussion in Java — you'll use them in every code review and architecture decision.

---

## Q7. What is encapsulation?

**Summary:** Encapsulation is binding data and methods together in a class while controlling access through access modifiers — it protects object integrity and reduces coupling.

**Practical Explanation:** We make fields `private` and expose `public` getters/setters (or better, only the methods that make business sense). This way, the internal representation can change without affecting external code. It's not just about getters/setters — it's about controlling how state changes happen.

**When/Why Used:** Every well-designed Java class uses encapsulation. In Spring services, we encapsulate business rules. In domain objects, we ensure state transitions are valid. It prevents other parts of the code from putting an object into an invalid state.

**Real-World Scenario:** A `BankAccount` class has a private `balance` field. Instead of a `setBalance()` method, we expose `deposit(amount)` and `withdraw(amount)` methods that validate the amount, check for sufficient funds, and maintain audit logs. No one can directly set balance to a negative value.

**Common Mistakes:** Blindly generating getters and setters for every field defeats encapsulation. If you expose `setBalance()`, you've broken encapsulation. Also, returning mutable internal collections from getters — always return defensive copies or unmodifiable views.

**Closing:** True encapsulation is about exposing behavior, not data — it's the first line of defense for maintaining object integrity.

---

## Q8. What is inheritance?

**Summary:** Inheritance allows a child class to acquire fields and methods from a parent class, enabling code reuse and establishing "is-a" relationships.

**Practical Explanation:** Using `extends`, a subclass inherits all non-private members of the parent. It can override methods to provide specialized behavior. Java supports single inheritance for classes (one parent only) but multiple inheritance through interfaces.

**When/Why Used:** We use inheritance when there's a genuine "is-a" relationship. Framework classes often use it — extending `HttpServlet`, `WebSecurityConfigurerAdapter`, or `AbstractController`. It's useful for template method patterns where the parent defines the algorithm skeleton.

**Real-World Scenario:** In a payment gateway, `PaymentProcessor` is the base class with common logic like logging, validation, and error handling. `StripeProcessor` and `PayPalProcessor` extend it, overriding only the `processPayment()` method with provider-specific integration code.

**Common Mistakes:** Overusing inheritance for code reuse when composition is better. Deep inheritance hierarchies (more than 2-3 levels) become hard to understand and maintain. Also, forgetting that constructors are NOT inherited — you must explicitly call `super()`.

**Closing:** Use inheritance judiciously — it creates tight coupling between parent and child. When in doubt, prefer composition over inheritance.

---

## Q9. What is polymorphism?

**Summary:** Polymorphism means one interface, multiple implementations — it lets us write flexible code that works with different types through a common contract.

**Practical Explanation:** There are two types:
- **Compile-time (method overloading):** Same method name, different parameter lists. Resolved at compile time.
- **Runtime (method overriding):** Subclass provides its own implementation of a parent method. Resolved at runtime via dynamic dispatch.

**When/Why Used:** Runtime polymorphism is the cornerstone of flexible design. Spring's dependency injection is built on it — we code to interfaces, and Spring injects the concrete implementation. Strategy pattern, Factory pattern — all rely on polymorphism.

**Real-World Scenario:** We have a `PaymentGateway` interface with `charge()` method. `StripeGateway` and `RazorpayGateway` implement it differently. The service layer just calls `paymentGateway.charge(amount)` — it doesn't know or care which implementation is running. We can switch payment providers by changing a configuration, not code.

```java
PaymentGateway gateway = paymentGatewayFactory.getGateway(region);
gateway.charge(order.getAmount()); // Polymorphic call
```

**Common Mistakes:** Confusing overloading with overriding. Also, using `instanceof` checks everywhere instead of leveraging polymorphism — if you're writing `if (obj instanceof X)` chains, you probably need polymorphism instead.

**Closing:** Polymorphism is what makes Java code truly extensible — master it, and you'll write code that's open for extension but closed for modification.

---

## Q10. What is abstraction?

**Summary:** Abstraction is hiding complex implementation details and exposing only the essential behavior through interfaces or abstract classes — it lets users interact with "what" something does, not "how."

**Practical Explanation:** We define contracts (interfaces/abstract classes) that specify what operations are available without revealing internal logic. The caller doesn't need to know the database query, the HTTP call, or the algorithm — just the method signature and what it returns.

**When/Why Used:** Every layered architecture uses abstraction. Controller doesn't know how the service works. Service doesn't know how the repository queries the DB. `JdbcTemplate`, `RestTemplate`, `JpaRepository` — all abstractions hiding complex infrastructure code.

**Real-World Scenario:** `UserRepository` interface has `findByEmail(String email)`. The caller doesn't care if it's backed by MySQL, PostgreSQL, MongoDB, or an in-memory cache. During testing, we swap in a mock implementation. In production, Spring Data JPA generates the actual implementation.

**Common Mistakes:** Creating abstractions too early (YAGNI) or creating leaky abstractions where implementation details bleed through. Also, abstracting for the sake of abstraction when there's only one implementation and no foreseeable second one.

**Closing:** Good abstraction is the hallmark of clean architecture — it makes code testable, swappable, and maintainable at scale.

---

## Q11. Interface vs abstract class?

**Summary:** Interface defines a pure contract with support for multiple inheritance; abstract class provides partial implementation with shared state — choose based on whether you need a contract or a base template.

**Practical Explanation:**
| Feature | Interface | Abstract Class |
|---------|-----------|----------------|
| Multiple inheritance | Yes | No |
| Fields | Only constants (`static final`) | Can have instance fields/state |
| Constructors | No | Yes |
| Methods | Abstract + default + static (Java 8+) | Abstract + concrete |
| Access modifiers | Public by default | Any access modifier |

**When/Why Used:** Use interfaces when you want to define a capability contract — `Serializable`, `Comparable`, `Runnable`. Use abstract classes when you have shared state or a template method pattern — common fields and partial implementation that subclasses build on.

**Real-World Scenario:** In Spring, `CrudRepository` is an interface — it defines CRUD operations. But `AbstractRoutingDataSource` is an abstract class — it has state (data source map) and a template method where subclasses only override `determineCurrentLookupKey()`.

**Common Mistakes:** Since Java 8 added default methods to interfaces, the line blurred. But interfaces still can't hold mutable state. Don't use abstract classes just for code sharing — if there's no shared state, use interface with default methods. Also, don't implement multiple interfaces with conflicting default methods without resolving the diamond problem.

**Closing:** In modern Java, I default to interfaces and only reach for abstract classes when I need shared mutable state or constructor logic.

---

## Q12. What is immutability?

**Summary:** An immutable object is one whose state cannot be modified after creation — once constructed, it stays the same for its entire lifetime.

**Practical Explanation:** Immutability means no setters, no state changes, no surprises. You create the object with all its data, and it never changes. If you need a "modified" version, you create a new object. `String`, `Integer`, `LocalDate` — all immutable in Java.

**When/Why Used:** Immutable objects are inherently thread-safe (no synchronization needed), safe as HashMap keys, and easier to reason about. In functional programming style, microservices, and concurrent applications, immutability is a first-class design choice.

**Real-World Scenario:** DTOs (Data Transfer Objects) and Value Objects in DDD should be immutable. A `Money` value object with `amount` and `currency` — once created, it never changes. `money.add(otherMoney)` returns a new `Money` instance.

**Common Mistakes:** Thinking immutability means just removing setters — you also need to handle mutable fields like `Date` or `List` via defensive copies. Also, forgetting that `final` on a reference only prevents reassignment, not mutation of the object itself.

**Closing:** Immutability eliminates an entire class of bugs — I make objects immutable by default and only make them mutable when there's a clear reason.

---

## Q13. Why is String immutable?

**Summary:** String is immutable in Java for security, thread-safety, String Pool caching, and hashcode consistency — it's a deliberate design decision that impacts the entire JVM ecosystem.

**Practical Explanation:**
- **Security:** Strings hold class names, file paths, DB URLs, passwords. If mutable, code could alter these after security checks.
- **Thread-safety:** Immutable Strings are inherently safe to share across threads without synchronization.
- **String Pool:** JVM caches Strings in a pool. If "hello" is shared by 100 references and someone mutates it, all 100 break.
- **Hashcode caching:** String caches its hashcode. Since the value never changes, the hashcode is computed once and reused — critical for HashMap performance.

**When/Why Used:** Every Java application uses Strings extensively. Understanding immutability explains why `String.concat()` returns a new String, why we use `StringBuilder` for loops, and why Strings are safe as HashMap keys.

**Real-World Scenario:** In a multi-threaded web server, the same String URL path is shared across multiple request-handling threads. Because it's immutable, there's no race condition — no thread can corrupt it for another.

**Common Mistakes:** Concatenating Strings in loops (`str += "data"`) creates thousands of intermediate String objects — use `StringBuilder`. Also, thinking `new String("hello")` and `"hello"` are the same — the `new` keyword bypasses the String Pool.

**Closing:** String immutability is a performance and security cornerstone of the JVM — understanding it is essential for writing efficient Java code.

---

## Q14. How to create an immutable class?

**Summary:** Make the class `final`, fields `private final`, no setters, initialize everything via constructor, and return defensive copies for mutable fields.

**Practical Explanation:** The recipe:
1. Declare the class as `final` — prevents subclass from breaking immutability.
2. Make all fields `private` and `final`.
3. No setter methods.
4. Initialize all fields via constructor.
5. For mutable fields (like `List`, `Date`), make defensive copies in constructor and getter.

```java
public final class Employee {
    private final String name;
    private final List<String> skills;

    public Employee(String name, List<String> skills) {
        this.name = name;
        this.skills = new ArrayList<>(skills); // defensive copy
    }

    public String getName() { return name; }

    public List<String> getSkills() {
        return Collections.unmodifiableList(skills); // defensive copy
    }
}
```

**When/Why Used:** Value objects in DDD, configuration objects, cache keys, DTOs shared across threads. Java 16+ Records (`record Employee(String name, List<String> skills)`) simplify this but still need defensive copies for mutable fields.

**Common Mistakes:** Forgetting defensive copies is the #1 mistake. If you accept a `List` in constructor and store the reference directly, the caller can still modify it externally. Same for returning mutable fields from getters.

**Closing:** Immutable classes are a senior-level design skill — getting defensive copies right is what separates solid code from subtle bugs in production.

---

## Q15. String vs StringBuilder?

**Summary:** `String` is immutable — every modification creates a new object. `StringBuilder` is mutable and efficient for repeated string manipulations.

**Practical Explanation:** When you do `str = str + "data"`, Java creates a new String object each time. In a loop of 10,000 iterations, that's 10,000 temporary objects for GC. `StringBuilder` modifies its internal `char[]` buffer in-place — one object, no garbage.

**When/Why Used:** Use `String` for fixed values, constants, and simple assignments. Use `StringBuilder` when building strings dynamically — in loops, constructing SQL queries, building log messages, generating HTML/JSON.

**Real-World Scenario:**
```java
// BAD — O(n^2) and creates n String objects
String result = "";
for (String item : items) {
    result += item + ",";
}

// GOOD — O(n) and single object
StringBuilder sb = new StringBuilder();
for (String item : items) {
    sb.append(item).append(",");
}
```

**Common Mistakes:** Using `StringBuilder` for simple concatenations like `"Hello " + name` is overkill — the compiler optimizes this anyway. Also, not pre-sizing the StringBuilder when the final size is known — `new StringBuilder(1024)` avoids internal resizing.

**Closing:** This is a classic performance question — knowing when to use each is a practical skill that shows up in code reviews daily.

---

## Q16. StringBuilder vs StringBuffer?

**Summary:** `StringBuilder` is non-synchronized and faster; `StringBuffer` is synchronized and thread-safe — in modern Java, we almost always use `StringBuilder`.

**Practical Explanation:** `StringBuffer` was the original (Java 1.0) — every method is `synchronized`, adding overhead even in single-threaded scenarios. `StringBuilder` (Java 1.5) is the drop-in replacement without synchronization. Same API, better performance.

**When/Why Used:** Use `StringBuilder` in 99% of cases — most string building is local to a method and doesn't cross thread boundaries. If you truly need thread-safe string building (rare), consider other concurrency patterns rather than `StringBuffer`.

**Real-World Scenario:** In a request-scoped service method building a response string, `StringBuilder` is the right choice — the object lives entirely within one thread. I haven't used `StringBuffer` in production in years.

**Common Mistakes:** Using `StringBuffer` "just to be safe" — unnecessary synchronization hurts performance. Also, thinking `StringBuffer` makes string operations atomic — individual method calls are synchronized, but a sequence of `append()` calls is not atomic without external synchronization.

**Closing:** `StringBuilder` is the default choice — `StringBuffer` is essentially a legacy class that exists for backward compatibility.

---

## Q17. What is equals()?

**Summary:** `equals()` is the method we override to define logical/content equality between objects — as opposed to reference equality (`==`).

**Practical Explanation:** The default `equals()` from `Object` class checks reference equality (same as `==`). We override it to compare actual content. For example, two `User` objects with the same `userId` should be "equal" even if they're different instances in memory.

**When/Why Used:** Critical for Collections — `HashMap.get()`, `List.contains()`, `Set.add()` all rely on `equals()`. Without proper `equals()`, your objects won't behave correctly in any collection.

**Real-World Scenario:**
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return Objects.equals(userId, user.userId);
}
```

**Common Mistakes:**
- Overriding `equals()` without overriding `hashCode()` — violates the contract and breaks HashMaps.
- Using `instanceof` instead of `getClass()` — can break symmetry with subclasses.
- Forgetting null checks.
- Not making it reflexive, symmetric, transitive, and consistent.

**Closing:** Always override `equals()` and `hashCode()` together — I use Lombok's `@EqualsAndHashCode` or IDE generation to avoid manual errors.

---

## Q18. hashCode() contract?

**Summary:** The hashCode contract states that if two objects are equal via `equals()`, they must return the same `hashCode()` — this is critical for hash-based collections to function correctly.

**Practical Explanation:** The contract:
1. If `a.equals(b)` is `true`, then `a.hashCode() == b.hashCode()` must be `true`.
2. If `a.equals(b)` is `false`, hashCodes can still collide (but shouldn't for performance).
3. `hashCode()` must be consistent — same object returns same hashCode across calls (unless fields used in computation change).

**When/Why Used:** `HashMap`, `HashSet`, `Hashtable` use `hashCode()` to determine the bucket. If equal objects have different hashCodes, a `HashSet` could contain "duplicates" and `HashMap.get()` might not find a value you just put in.

**Real-World Scenario:** I once debugged a production issue where objects were being stored in a `HashSet` but `.contains()` returned `false`. The root cause was `equals()` was overridden but `hashCode()` was not — the set checked the wrong bucket every time.

**Common Mistakes:** Using mutable fields in `hashCode()` calculation — if the field changes after insertion into a `HashMap`, the object becomes unreachable (it's in the wrong bucket). Always use immutable fields like `id`.

**Closing:** Violating the hashCode contract is one of the most insidious bugs in Java — it causes silent data loss in collections. Always override both `equals()` and `hashCode()`.

---

## Q19. == vs equals()?

**Summary:** `==` compares references (are they the same object in memory?), while `equals()` compares logical content (are they meaningfully the same?).

**Practical Explanation:**
- `==` for primitives compares values. For objects, it checks if both references point to the exact same memory location.
- `equals()` compares object content — what we define as "equality" by overriding the method.

**When/Why Used:** Use `==` for primitives (`int`, `boolean`) and for null checks. Use `equals()` for all object comparisons — Strings, DTOs, entities.

**Real-World Scenario:**
```java
String a = new String("hello");
String b = new String("hello");
System.out.println(a == b);      // false — different objects
System.out.println(a.equals(b)); // true — same content

String c = "hello";
String d = "hello";
System.out.println(c == d);      // true — String Pool reuse
```

**Common Mistakes:** Comparing Strings with `==` — works sometimes due to String Pool interning but fails unpredictably. Comparing wrapper types like `Integer` with `==` — works for cached values (-128 to 127) but fails for larger values. Always use `equals()`.

**Closing:** This is a classic interview question because getting it wrong causes real production bugs — especially with String and Integer comparisons.

---

## Q20. What is the final keyword?

**Summary:** `final` prevents modification — final variables can't be reassigned, final methods can't be overridden, and final classes can't be extended.

**Practical Explanation:**
- **Final variable:** Value assigned once. For references, the reference can't change, but the object itself can still be mutated.
- **Final method:** Subclasses cannot override it — used to lock down critical behavior.
- **Final class:** Cannot be subclassed — `String`, `Integer`, `Math` are all final.

**When/Why Used:** Constants (`public static final`), ensuring immutability, preventing inheritance of sensitive classes, and effectively-final variables in lambdas. In Spring, `@Service` beans are typically stored in final fields via constructor injection.

**Real-World Scenario:**
```java
@Service
public class OrderService {
    private final OrderRepository orderRepository; // final — assigned once via constructor
    private final PaymentGateway paymentGateway;

    public OrderService(OrderRepository orderRepository, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }
}
```

**Common Mistakes:** Thinking `final List<String> list` means the list can't be modified — only the reference is final, you can still `list.add()`. For true immutability, use `Collections.unmodifiableList()` or `List.of()`.

**Closing:** `final` is a key tool for writing predictable, thread-safe code — I use it on almost every field and method parameter.

---

## Q21. What is the static keyword?

**Summary:** `static` means the member belongs to the class itself, not to any specific instance — it's shared across all objects of that class.

**Practical Explanation:** Static fields are shared — one copy for all instances. Static methods can be called without creating an object. Static blocks run once when the class is loaded. Static inner classes don't hold a reference to the outer class.

**When/Why Used:** Utility methods (`Math.max()`, `Collections.sort()`), constants (`static final`), factory methods (`LocalDate.now()`), singleton patterns, and counters shared across instances.

**Real-World Scenario:**
```java
public class AppConstants {
    public static final String API_VERSION = "v2";
    public static final int MAX_RETRY_COUNT = 3;
}

// Utility class
public class DateUtils {
    public static LocalDate parseDate(String date) { ... }
}
```

**Common Mistakes:** Overusing static — static mutable state is essentially a global variable and makes testing/mocking difficult. Static methods can't be overridden (only hidden), breaking polymorphism. Also, accessing instance variables from static context — compilation error.

**Closing:** Use static for truly class-level concerns — constants, utilities, and factory methods. Avoid static mutable state as it hurts testability and concurrency.

---

## Q22. What is a constructor?

**Summary:** A constructor is a special method invoked at object creation time to initialize the object's state — it has the same name as the class and no return type.

**Practical Explanation:** Constructors ensure objects are born in a valid state. If no constructor is defined, Java provides a default no-arg constructor. Once you define any constructor, the default disappears. Constructors can call other constructors via `this()` or parent constructors via `super()`.

**When/Why Used:** Every object creation uses a constructor. In Spring, constructor injection is the recommended DI approach — dependencies are passed via constructor, stored in `final` fields, ensuring the bean is fully initialized.

**Real-World Scenario:**
```java
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection — Spring recommended approach
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
}
```

**Common Mistakes:** Heavy logic in constructors — constructors should initialize, not perform business logic. Throwing checked exceptions from constructors makes the class hard to use. Also, forgetting to call `super()` when the parent class doesn't have a no-arg constructor.

**Closing:** Constructors are the gatekeepers of object validity — well-designed constructors guarantee that objects are always in a usable state from the moment they're created.

---

## Q23. Constructor overloading?

**Summary:** Constructor overloading means defining multiple constructors with different parameter lists, giving callers flexibility in how they create objects.

**Practical Explanation:** Each constructor has a different signature (number, type, or order of parameters). They can chain to each other using `this()` — the most specific constructor does the actual initialization, others delegate to it.

**When/Why Used:** Providing convenience constructors with default values, backward compatibility when adding new fields, and the telescoping constructor pattern (though Builder pattern is preferred for many parameters).

**Real-World Scenario:**
```java
public class Notification {
    private final String recipient;
    private final String message;
    private final Priority priority;

    public Notification(String recipient, String message) {
        this(recipient, message, Priority.NORMAL); // delegates
    }

    public Notification(String recipient, String message, Priority priority) {
        this.recipient = recipient;
        this.message = message;
        this.priority = priority;
    }
}
```

**Common Mistakes:** Telescoping constructors become unreadable with many parameters — use Builder pattern instead. Also, `this()` must be the first statement in the constructor. And remember, `this()` and `super()` cannot be used together in the same constructor.

**Closing:** Constructor overloading provides flexibility, but for classes with more than 3-4 parameters, switch to the Builder pattern for readability.

---

## Q24. What is garbage collection?

**Summary:** Garbage Collection (GC) is the JVM's automatic memory management — it identifies and reclaims memory occupied by objects that are no longer reachable, freeing developers from manual memory management.

**Practical Explanation:** The GC tracks object references. When no live thread can reach an object (no reference chain from GC roots), it's eligible for collection. The GC runs in its own threads, and during collection, it may cause "stop-the-world" pauses where application threads freeze.

**When/Why Used:** GC runs automatically — but as senior developers, we tune it. We choose GC algorithms, set heap sizes, monitor GC logs, and optimize code to reduce GC pressure (e.g., object pooling, avoiding unnecessary allocations).

**Real-World Scenario:** In a high-throughput microservice handling 10K requests/second, we noticed latency spikes every 30 seconds. GC logs revealed Full GC pauses of 500ms. We switched from Parallel GC to G1GC, tuned `-XX:MaxGCPauseMillis=50`, and the spikes disappeared.

**Common Mistakes:** Calling `System.gc()` — it's only a suggestion, not guaranteed, and can cause worse pauses. Creating excessive short-lived objects in hot loops increases GC pressure. Also, not enabling GC logs in production — `-Xlog:gc*` is essential for diagnosis.

**Closing:** You don't manage memory manually in Java, but you absolutely need to understand GC to build high-performance, low-latency applications.

---

## Q25. Types of GC?

**Summary:** Java provides multiple GC algorithms — Serial, Parallel, CMS, G1, and ZGC — each optimized for different workload characteristics like throughput, latency, or heap size.

**Practical Explanation:**
- **Serial GC:** Single-threaded, for small applications. `-XX:+UseSerialGC`
- **Parallel GC:** Multi-threaded, maximizes throughput. Default in Java 8. `-XX:+UseParallelGC`
- **CMS (Concurrent Mark-Sweep):** Low-pause, deprecated in Java 14. Concurrent collection.
- **G1 GC:** Region-based, balanced throughput/latency. Default since Java 9. `-XX:+UseG1GC`
- **ZGC:** Ultra-low latency (<10ms pauses even on TB heaps). `-XX:+UseZGC`
- **Shenandoah:** Similar to ZGC, concurrent compaction. Red Hat developed.

**When/Why Used:** G1 is the default and works well for most applications. ZGC for latency-critical systems (trading platforms, real-time APIs). Parallel GC for batch processing where throughput matters more than pause times.

**Real-World Scenario:** For our real-time pricing engine, we use ZGC with a 16GB heap — pause times stay under 2ms even under heavy load. For our nightly batch ETL jobs, we use Parallel GC to maximize throughput.

**Common Mistakes:** Not choosing the right GC for your workload. Running a latency-sensitive API with Parallel GC causes long stop-the-world pauses. Also, not monitoring GC metrics — use tools like JVisualVM, GCViewer, or Grafana with Micrometer.

**Closing:** GC tuning is a critical production skill — the right GC choice can mean the difference between smooth operations and customer-facing latency issues.

---

## Q26. What is a memory leak?

**Summary:** A memory leak in Java occurs when objects are no longer needed but still referenced, preventing the GC from reclaiming them — eventually leading to OutOfMemoryError.

**Practical Explanation:** Even with GC, Java can have memory leaks. If you hold references to objects you no longer need — in static collections, caches without eviction, unclosed resources, listener registrations — those objects accumulate and eat heap space.

**When/Why Used:** Memory leak detection is a critical production debugging skill. Tools like JProfiler, VisualVM, Eclipse MAT, and heap dump analysis (`jmap -dump:live,format=b,file=heap.hprof <pid>`) are essential.

**Real-World Scenario:** We had a service running out of memory every 3 days. Heap dump analysis revealed a `static HashMap` used as a cache that grew unbounded — keys were session IDs, never removed after session expiry. Fix: replaced with `ConcurrentHashMap` with TTL-based eviction (or Caffeine/Guava cache).

**Common Mistakes:**
- `static` collections that grow without bounds.
- Not closing resources (`Connection`, `InputStream`, `ResultSet`) — use try-with-resources.
- Inner classes holding implicit references to outer class instances.
- Registering listeners/callbacks and never deregistering them.
- `ThreadLocal` variables not cleaned up — especially dangerous in thread pools.

**Closing:** Memory leaks in Java are subtle — they don't crash immediately but degrade over days. Knowing how to analyze heap dumps is a must-have skill.

---

## Q27. Stack vs Heap?

**Summary:** Stack stores method-local variables and call frames (fast, LIFO, per-thread); Heap stores objects and instance variables (shared across threads, managed by GC).

**Practical Explanation:**
- **Stack:** Each thread has its own stack. Stores primitive local variables, method parameters, and object references. Automatically cleaned when the method returns. Fixed size — `StackOverflowError` if too deep (recursion).
- **Heap:** Shared memory area. All objects (`new` keyword) live here. Managed by Garbage Collector. `OutOfMemoryError` if full.

**When/Why Used:** Understanding this is critical for debugging — `StackOverflowError` means infinite recursion, `OutOfMemoryError` means heap exhaustion or memory leak. Also essential for understanding thread safety — stack is thread-safe by nature (thread-local), heap is shared and needs synchronization.

**Real-World Scenario:** A recursive method without proper base case caused `StackOverflowError` in production. The fix was switching to iterative approach. Separately, a large file upload storing the entire content as a byte array caused `OutOfMemoryError: Java heap space` — fixed by using streaming/chunked processing.

**Common Mistakes:** Thinking all variables live on the stack — only local primitives and references live on the stack; the actual objects are on the heap. Also, the JVM's escape analysis can sometimes allocate objects on the stack if they don't escape the method — but that's JVM optimization, not something to code for.

**Closing:** Stack vs Heap is foundational for understanding JVM memory, threading, and debugging production memory issues.

---

## Q28. Checked vs unchecked exception?

**Summary:** Checked exceptions must be handled at compile time (declared or caught); unchecked exceptions (RuntimeExceptions) occur at runtime and don't require explicit handling.

**Practical Explanation:**
- **Checked:** `IOException`, `SQLException`, `ClassNotFoundException` — the compiler forces you to handle them. They represent recoverable conditions.
- **Unchecked:** `NullPointerException`, `IllegalArgumentException`, `ArrayIndexOutOfBoundsException` — programming errors, not expected to be caught everywhere.
- **Error:** `OutOfMemoryError`, `StackOverflowError` — JVM-level, not meant to be caught.

**When/Why Used:** In modern Java (especially with Spring), the trend is toward unchecked exceptions. Spring wraps checked exceptions (like `SQLException`) into unchecked ones (`DataAccessException`). Custom business exceptions should extend `RuntimeException`.

**Real-World Scenario:**
```java
// Custom unchecked business exception
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long orderId) {
        super("Order not found: " + orderId);
    }
}
// Handled globally via @ControllerAdvice
```

**Common Mistakes:** Catching `Exception` or `Throwable` — too broad, hides bugs. Empty catch blocks — silently swallowing errors. Also, wrapping every checked exception with `throws` up the call chain without handling — defeats the purpose.

**Closing:** Use unchecked exceptions for business errors, handle checked exceptions close to where they occur, and always have a global exception handler in your API layer.

---

## Q29. Try-with-resources?

**Summary:** Try-with-resources (Java 7+) automatically closes resources implementing `AutoCloseable` at the end of the try block — eliminating resource leak bugs.

**Practical Explanation:** Before Java 7, we needed verbose try-finally blocks to close resources. Now, any `AutoCloseable` resource declared in the try parentheses is automatically closed, even if an exception occurs. The close happens in reverse order of declaration.

**When/Why Used:** Every time you work with I/O: file streams, database connections, HTTP connections, `BufferedReader`, `PreparedStatement`. Especially critical in production to prevent connection pool exhaustion.

**Real-World Scenario:**
```java
// Modern approach
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    while (rs.next()) {
        // process results
    }
} // All three resources auto-closed in reverse order

// vs Old approach — error-prone, verbose
Connection conn = null;
try {
    conn = dataSource.getConnection();
    // ...
} finally {
    if (conn != null) conn.close(); // Could itself throw!
}
```

**Common Mistakes:** Resources declared outside the try block won't be auto-closed. Also, suppressed exceptions — if both the try body and the close method throw, the close exception is suppressed (accessible via `getSuppressed()`). Forgetting to make custom resources implement `AutoCloseable`.

**Closing:** Try-with-resources is a must-use pattern — any resource that needs closing should go in a try-with-resources block. No exceptions.

---

## Q30. What is Optional?

**Summary:** `Optional` is a container object (Java 8+) that may or may not hold a value — it forces callers to handle the absence of a value explicitly, reducing `NullPointerException` bugs.

**Practical Explanation:** Instead of returning `null` (which the caller might forget to check), return `Optional<User>`. The caller is forced to handle the empty case — `orElse()`, `orElseThrow()`, `ifPresent()`, `map()`.

**When/Why Used:** Method return types where "no result" is a valid outcome. Spring Data's `findById()` returns `Optional<Entity>`. Not meant for method parameters, fields, or collections (use empty collection instead).

**Real-World Scenario:**
```java
public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
}

// Caller
User user = userService.findByEmail(email)
    .orElseThrow(() -> new UserNotFoundException(email));

// Or with functional chaining
String city = userService.findByEmail(email)
    .map(User::getAddress)
    .map(Address::getCity)
    .orElse("Unknown");
```

**Common Mistakes:** `optional.get()` without checking `isPresent()` — defeats the purpose and can still throw `NoSuchElementException`. Using Optional for fields or method parameters — it's not `Serializable` and adds overhead. Returning `Optional.of(null)` — throws NPE; use `Optional.ofNullable()`.

**Closing:** Optional is a powerful tool for API design — use it for return types to make the "null case" impossible to ignore.

---

## Q31. What is Stream API?

**Summary:** The Stream API (Java 8+) provides a declarative, functional way to process collections — filter, map, reduce, collect — with support for parallel execution.

**Practical Explanation:** Streams don't store data — they're pipelines. You create a stream from a collection, apply intermediate operations (lazy), and trigger execution with a terminal operation. They support method chaining, lambda expressions, and can be parallelized with `.parallelStream()`.

**When/Why Used:** Any time you process collections — filtering lists, transforming data, aggregating results, grouping. Replaces verbose for-loops with concise, readable code.

**Real-World Scenario:**
```java
// Find active premium users' emails
List<String> emails = users.stream()
    .filter(User::isActive)
    .filter(u -> u.getPlan() == Plan.PREMIUM)
    .map(User::getEmail)
    .sorted()
    .collect(Collectors.toList());

// Group orders by status
Map<OrderStatus, List<Order>> ordersByStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::getStatus));
```

**When/Why Used:** Data transformation, filtering, aggregation, reporting. Cleaner than nested loops and conditionals.

**Common Mistakes:** Reusing a stream after terminal operation — streams are one-use. Using `parallelStream()` without understanding — it uses the common ForkJoinPool and can hurt performance for I/O-bound or small collections. Side effects in stream operations (modifying external state in `forEach`) violate the functional contract.

**Closing:** Stream API is essential for modern Java — it makes collection processing concise, readable, and potentially parallelizable.

---

## Q32. map vs flatMap?

**Summary:** `map()` transforms each element one-to-one; `flatMap()` transforms each element into a stream and flattens the results into a single stream — it's a map + flatten operation.

**Practical Explanation:**
- `map(x -> f(x))` — transforms each element. If `f(x)` returns a collection/Optional, you get a stream of collections.
- `flatMap(x -> f(x))` — transforms each element into a stream, then merges all resulting streams into one.

**When/Why Used:** `flatMap` when dealing with nested structures — list of lists, Optional of Optional, one-to-many relationships.

**Real-World Scenario:**
```java
// map — one-to-one: User -> name
List<String> names = users.stream()
    .map(User::getName)
    .collect(Collectors.toList());

// flatMap — one-to-many: User -> List<Order> -> flat stream of orders
List<Order> allOrders = users.stream()
    .flatMap(user -> user.getOrders().stream())
    .collect(Collectors.toList());

// Optional flatMap
Optional<String> city = user
    .flatMap(User::getAddress)  // returns Optional<Address>
    .map(Address::getCity);     // returns Optional<String>
```

**Common Mistakes:** Using `map` when you need `flatMap` — results in `Stream<Stream<T>>` or `Optional<Optional<T>>` instead of the flattened version. Not understanding that `flatMap` expects a function that returns a Stream (or Optional in Optional's case).

**Closing:** Once you internalize the difference, you'll naturally reach for `flatMap` whenever you see nested structures — it's a pattern that shows up constantly in real code.

---

## Q33. Intermediate vs terminal operations?

**Summary:** Intermediate operations (filter, map, sorted) are lazy and return a new Stream; terminal operations (collect, forEach, count) trigger actual processing and produce a result.

**Practical Explanation:**
- **Intermediate:** `filter()`, `map()`, `flatMap()`, `sorted()`, `distinct()`, `limit()`, `peek()` — they build up the pipeline but nothing executes yet.
- **Terminal:** `collect()`, `forEach()`, `count()`, `reduce()`, `findFirst()`, `anyMatch()`, `toList()` — they trigger the pipeline and produce the final result.

**When/Why Used:** Laziness is a performance feature — intermediate operations can be fused and short-circuited. For example, `stream.filter(...).findFirst()` stops processing as soon as the first match is found, not after filtering the entire collection.

**Real-World Scenario:**
```java
// Nothing executes until collect() is called
Stream<User> pipeline = users.stream()
    .filter(u -> u.isActive())      // intermediate — lazy
    .map(u -> u.getEmail())         // intermediate — lazy
    .sorted();                       // intermediate — lazy

// Now everything executes
List<String> result = pipeline.collect(Collectors.toList()); // terminal
```

**Common Mistakes:** Using `peek()` for side effects in production code — it's meant for debugging. Forgetting the terminal operation — the stream never executes. Calling multiple terminal operations on the same stream — `IllegalStateException`.

**Closing:** Understanding lazy evaluation is key to writing efficient stream pipelines — the JVM optimizes the execution path based on terminal operation needs.

---

## Q34. What is a lambda?

**Summary:** A lambda is a concise, anonymous function that implements a functional interface — it brings functional programming to Java, making code more expressive and compact.

**Practical Explanation:** Instead of writing a full anonymous inner class to implement a single-method interface, you write `(parameters) -> expression`. The compiler infers the functional interface from context. Lambdas can capture effectively-final variables from the enclosing scope.

**When/Why Used:** Everywhere in modern Java — Stream API operations, event handlers, `Comparator`, `Runnable`, `Callable`, Spring's callback-based APIs, and custom functional interfaces.

**Real-World Scenario:**
```java
// Before lambdas
Collections.sort(users, new Comparator<User>() {
    @Override
    public int compare(User a, User b) {
        return a.getName().compareTo(b.getName());
    }
});

// With lambdas
users.sort((a, b) -> a.getName().compareTo(b.getName()));

// Even cleaner with method reference
users.sort(Comparator.comparing(User::getName));
```

**Common Mistakes:** Lambdas with too much logic — if it's more than 2-3 lines, extract it to a named method and use a method reference. Modifying external variables — lambdas can only capture effectively-final variables. Also, exception handling in lambdas is verbose — checked exceptions don't play well with functional interfaces.

**Closing:** Lambdas are the backbone of modern Java — they're not just syntactic sugar, they enable a functional programming style that makes code cleaner and more composable.

---

## Q35. Functional interface?

**Summary:** A functional interface has exactly one abstract method and serves as the target type for lambdas and method references — annotated with `@FunctionalInterface` for safety.

**Practical Explanation:** Java's built-in functional interfaces in `java.util.function`:
- `Function<T, R>` — takes T, returns R (`map`)
- `Predicate<T>` — takes T, returns boolean (`filter`)
- `Consumer<T>` — takes T, returns void (`forEach`)
- `Supplier<T>` — takes nothing, returns T (factory)
- `BiFunction<T, U, R>` — takes T and U, returns R

**When/Why Used:** Every lambda needs a functional interface as its target type. Custom business logic that should be pluggable — validation rules, transformation strategies, callback handlers.

**Real-World Scenario:**
```java
@FunctionalInterface
public interface PricingStrategy {
    BigDecimal calculatePrice(Product product, Customer customer);
}

// Usage
PricingStrategy premiumPricing = (product, customer) ->
    product.getBasePrice().multiply(BigDecimal.valueOf(0.9));

PricingStrategy regularPricing = (product, customer) ->
    product.getBasePrice();
```

**Common Mistakes:** Adding more than one abstract method — breaks the contract. Confusing default methods with abstract methods — an interface with one abstract method and multiple default methods is still a functional interface. Not using `@FunctionalInterface` annotation — it's optional but prevents accidental addition of a second abstract method.

**Closing:** Functional interfaces are the bridge between OOP and functional programming in Java — understanding the built-in ones makes Stream API and lambda usage second nature.

---

## Q36. Default methods?

**Summary:** Default methods (Java 8+) allow adding method implementations in interfaces without breaking existing implementations — they solved the interface evolution problem.

**Practical Explanation:** Before Java 8, adding a method to an interface broke every implementing class. Default methods provide a body using the `default` keyword. Implementing classes can override them or use the default. This enabled adding `stream()`, `forEach()` to `Collection` interface without breaking millions of existing classes.

**When/Why Used:** API evolution (adding new methods to published interfaces), providing common behavior that most implementations share, and enabling multiple inheritance of behavior (not state).

**Real-World Scenario:**
```java
public interface Auditable {
    default String getAuditLog() {
        return "Modified at: " + getModifiedDate() + " by: " + getModifiedBy();
    }
    LocalDateTime getModifiedDate();
    String getModifiedBy();
}
// All entities implementing Auditable get getAuditLog() for free
```

**Common Mistakes:** Diamond problem — if a class implements two interfaces with the same default method, it must override and resolve the conflict explicitly. Default methods can't access instance state (no fields in interfaces). Don't abuse them to make interfaces look like abstract classes.

**Closing:** Default methods were a game-changer for Java's evolution — they let the JDK team evolve core interfaces like `Collection` and `Map` without breaking backward compatibility.

---

## Q37. What is multithreading?

**Summary:** Multithreading is concurrent execution of two or more threads within a single process, enabling better CPU utilization and responsive applications.

**Practical Explanation:** A thread is a lightweight unit of execution within a process. Multithreading lets us perform multiple tasks simultaneously — handling multiple HTTP requests, background processing, parallel computations. In Java, we create threads via `Thread` class, `Runnable`, `Callable`, or (preferred) using `ExecutorService`.

**When/Why Used:** Web servers (Tomcat uses thread pools to handle concurrent requests), async processing (sending emails in background), parallel data processing, scheduled tasks, and responsive UIs.

**Real-World Scenario:**
```java
// Modern approach with ExecutorService
ExecutorService executor = Executors.newFixedThreadPool(10);
Future<Report> reportFuture = executor.submit(() -> generateReport());
Future<Stats> statsFuture = executor.submit(() -> calculateStats());

// Both run concurrently
Report report = reportFuture.get();
Stats stats = statsFuture.get();

// Even better with CompletableFuture
CompletableFuture.supplyAsync(() -> fetchUserData())
    .thenApply(data -> processData(data))
    .thenAccept(result -> saveResult(result));
```

**Common Mistakes:** Creating raw threads instead of using thread pools — leads to unbounded thread creation and OOM. Not handling thread safety — shared mutable state without synchronization causes race conditions. Also, not handling `InterruptedException` properly — either re-interrupt the thread or propagate it.

**Closing:** Multithreading is powerful but complex — modern Java provides higher-level abstractions like `CompletableFuture` and virtual threads (Java 21) that make concurrent programming safer and simpler.

---

## Q38. Process vs Thread?

**Summary:** A process is an independent program with its own memory space; a thread is a lightweight unit of execution within a process that shares the process's memory.

**Practical Explanation:**
| Aspect | Process | Thread |
|--------|---------|--------|
| Memory | Separate address space | Shares heap within process |
| Communication | IPC (sockets, pipes) — expensive | Direct shared memory — fast |
| Creation cost | Heavy (new memory space) | Lightweight |
| Crash impact | Isolated — one crash doesn't affect others | One thread crash can kill entire process |
| Context switch | Expensive | Cheaper |

**When/Why Used:** In microservices, each service is a separate process (JVM) — isolation and independent deployment. Within a service, we use threads for concurrent request handling, background tasks, and parallel processing.

**Real-World Scenario:** A Spring Boot application is one process (one JVM). Tomcat's thread pool (default 200 threads) handles concurrent HTTP requests within that process. Each request runs on a separate thread, sharing the application's heap memory — that's why `@Service` beans must be thread-safe.

**Common Mistakes:** Thinking more threads always means better performance — too many threads cause excessive context switching. Not understanding that thread-shared memory requires synchronization. Also, confusing process-level isolation (microservices) with thread-level concurrency (within one service).

**Closing:** Understanding process vs thread is fundamental for designing microservice architectures and for writing correct concurrent code within each service.

---

## Q39. What is synchronization?

**Summary:** Synchronization controls concurrent access to shared resources, ensuring only one thread can execute a critical section at a time — preventing race conditions and data corruption.

**Practical Explanation:** The `synchronized` keyword acquires a monitor lock on an object. Only one thread can hold the lock at a time — others block until it's released. Can be applied to methods or blocks. Java also provides `java.util.concurrent` locks (`ReentrantLock`, `ReadWriteLock`) for more fine-grained control.

**When/Why Used:** When multiple threads access and modify shared mutable state — counters, shared caches, connection pools. In Spring, singleton beans are shared across threads, so any mutable state in them needs synchronization.

**Real-World Scenario:**
```java
// synchronized method
public synchronized void incrementCounter() {
    this.count++;
}

// Better — synchronized block (finer granularity)
public void updateCache(String key, String value) {
    synchronized(this.cache) {
        cache.put(key, value);
    }
}

// Best — use concurrent collections
private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
```

**Common Mistakes:** Over-synchronizing — locking entire methods when only a few lines need protection. Synchronizing on the wrong object. Deadlocks from inconsistent lock ordering. Also, not considering `ConcurrentHashMap`, `AtomicInteger`, and other `java.util.concurrent` alternatives that are often better than manual synchronization.

**Closing:** Synchronization is essential but should be used surgically — prefer concurrent utilities from `java.util.concurrent` over raw `synchronized` blocks.

---

## Q40. volatile keyword?

**Summary:** `volatile` ensures visibility of a variable's changes across threads — when one thread writes to a volatile variable, all other threads immediately see the updated value.

**Practical Explanation:** Without `volatile`, threads may cache variable values in CPU registers/caches. Thread A updates `flag = true`, but Thread B might still see `flag = false` from its local cache. `volatile` forces reads/writes directly to main memory, establishing a happens-before relationship.

**When/Why Used:** Simple flags for thread signaling (stop flags), double-checked locking in singleton pattern, status indicators. Not a replacement for synchronization — `volatile` guarantees visibility but NOT atomicity.

**Real-World Scenario:**
```java
public class GracefulShutdown {
    private volatile boolean running = true;

    public void run() {
        while (running) {  // Without volatile, thread might never see the change
            processNextTask();
        }
    }

    public void shutdown() {
        running = false;  // Visible to the run() thread immediately
    }
}
```

**Common Mistakes:** Thinking `volatile` makes operations atomic — `volatile int count; count++` is NOT thread-safe because `count++` is read-modify-write (three operations). Use `AtomicInteger` for that. Also, overusing volatile when `synchronized` or `Atomic*` classes are more appropriate.

**Closing:** `volatile` is a lightweight synchronization mechanism for visibility — use it for flags and status variables, but reach for `AtomicInteger` or `synchronized` when you need atomicity.

---

## Q41. Deadlock?

**Summary:** Deadlock is a situation where two or more threads are permanently blocked, each waiting for a lock held by the other — creating a circular dependency that never resolves.

**Practical Explanation:** Classic scenario: Thread A holds Lock 1, needs Lock 2. Thread B holds Lock 2, needs Lock 1. Neither can proceed. Deadlocks require four conditions: mutual exclusion, hold-and-wait, no preemption, and circular wait. Eliminating any one prevents deadlock.

**When/Why Used:** Deadlock prevention is a critical concern in multi-threaded applications. We need to design lock acquisition strategies carefully.

**Real-World Scenario:**
```java
// DEADLOCK-PRONE
public void transferMoney(Account from, Account to, BigDecimal amount) {
    synchronized(from) {           // Thread 1: locks accountA
        synchronized(to) {         // Thread 1: waits for accountB
            // transfer
        }
    }
}
// Thread 1: transfer(A, B) — locks A, waits for B
// Thread 2: transfer(B, A) — locks B, waits for A → DEADLOCK

// FIX — consistent lock ordering
public void transferMoney(Account from, Account to, BigDecimal amount) {
    Account first = from.getId() < to.getId() ? from : to;
    Account second = from.getId() < to.getId() ? to : from;
    synchronized(first) {
        synchronized(second) {
            // transfer — always locks lower ID first
        }
    }
}
```

**Common Mistakes:** Not establishing a consistent lock ordering strategy. Using nested `synchronized` blocks carelessly. Not using `tryLock()` with timeout from `ReentrantLock` which can detect potential deadlocks. Also, not monitoring for deadlocks — `jstack` and `ThreadMXBean.findDeadlockedThreads()` are diagnostic tools.

**Closing:** Deadlocks are among the hardest bugs to reproduce and debug — prevention through design (consistent ordering, timeouts, avoiding nested locks) is far better than detection.

---

## Q42. What is serialization?

**Summary:** Serialization is converting a Java object into a byte stream for storage or transmission, and deserialization is the reverse — reconstructing the object from bytes.

**Practical Explanation:** Implement `Serializable` (marker interface), and Java can convert your object graph to bytes. Used for persisting objects to files, sending over network, caching (Redis, Memcached), and session replication in distributed systems.

**When/Why Used:** Distributed caching (Redis stores serialized Java objects), JMS message queues, HTTP session persistence across server restarts, deep copying objects. Though in modern systems, JSON/Protobuf serialization is preferred over Java's native serialization.

**Real-World Scenario:**
```java
public class User implements Serializable {
    private static final long serialVersionUID = 1L; // Version control
    private String name;
    private transient String password; // Won't be serialized
}

// Modern alternative — JSON serialization with Jackson
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(user);
User deserialized = mapper.readValue(json, User.class);
```

**Common Mistakes:** Not defining `serialVersionUID` — if the class changes, deserialization of old data fails with `InvalidClassException`. Serializing sensitive data (passwords, tokens) without marking them `transient`. Also, Java's native serialization has known security vulnerabilities — deserialization attacks can execute arbitrary code. Prefer JSON/Protobuf.

**Closing:** While Java's native serialization still exists, modern applications favor JSON (Jackson) or Protobuf — they're faster, cross-language, and more secure.

---

## Q43. transient keyword?

**Summary:** `transient` marks a field to be excluded from serialization — the field won't be written to the byte stream and will have its default value upon deserialization.

**Practical Explanation:** When an object is serialized, `transient` fields are skipped. Upon deserialization, these fields get default values (`null` for objects, `0` for numbers, `false` for boolean). Useful for sensitive data, derived/computed fields, and non-serializable fields.

**When/Why Used:** Passwords, security tokens, cached/computed values, logger references, database connections — anything that shouldn't be persisted or transmitted, or can't be serialized.

**Real-World Scenario:**
```java
public class UserSession implements Serializable {
    private String username;
    private String authToken;
    private transient String rawPassword;       // Security — never serialize
    private transient Logger log = LoggerFactory.getLogger(this.getClass()); // Not serializable
    private transient BigDecimal cachedBalance;  // Can be recomputed
}
```

**Common Mistakes:** Forgetting that `transient` fields are `null`/default after deserialization — need to reinitialize them, typically in a `readObject()` method or a post-deserialization hook. Also, `static` fields are never serialized regardless of `transient` — `transient` on a static field is redundant.

**Closing:** `transient` is a simple but important modifier — it's your tool for controlling exactly what gets serialized and protecting sensitive data.

---

## Q44. Cloneable?

**Summary:** `Cloneable` is a marker interface that indicates an object can be cloned via `Object.clone()` — but it's a broken API, and modern Java avoids it in favor of copy constructors or factory methods.

**Practical Explanation:** To make a class cloneable: implement `Cloneable` and override `clone()` method. Without implementing `Cloneable`, calling `clone()` throws `CloneNotSupportedException`. The default `clone()` does a shallow copy — primitive fields are copied, but object references still point to the same objects.

**When/Why Used:** Rarely used in modern Java. Copy constructors, static factory methods, or serialization-based copying are preferred. However, you may encounter it in legacy codebases.

**Real-World Scenario:**
```java
// Legacy approach (avoid)
public class Order implements Cloneable {
    @Override
    public Order clone() {
        try {
            Order copy = (Order) super.clone();
            copy.items = new ArrayList<>(this.items); // Deep copy mutable fields
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Can't happen
        }
    }
}

// Modern approach (preferred)
public class Order {
    public Order(Order other) { // Copy constructor
        this.id = other.id;
        this.items = new ArrayList<>(other.items);
    }
}
```

**Common Mistakes:** Relying on default shallow copy for objects with mutable fields — changes to cloned object's mutable fields affect the original. Not overriding `clone()` to do deep copy. Joshua Bloch (Effective Java) calls `Cloneable` "a broken API" — prefer copy constructors.

**Closing:** Know `Cloneable` for interviews and legacy code, but in your own code, always use copy constructors or builder patterns for object copying.

---

## Q45. Shallow vs deep copy?

**Summary:** Shallow copy copies field values (references point to same objects); deep copy recursively copies all referenced objects — creating a completely independent clone.

**Practical Explanation:**
- **Shallow copy:** Creates a new object, copies primitive fields, but reference fields still point to the same objects as the original. Modifying a mutable field in the copy affects the original.
- **Deep copy:** Creates a new object AND recursively clones all referenced objects. The copy is completely independent.

**When/Why Used:** Deep copy is needed when you want a fully independent copy — undo/redo operations, caching original state before modifications, passing objects to untrusted code.

**Real-World Scenario:**
```java
class Department {
    String name;
    List<Employee> employees;

    // Shallow copy — employees list is shared
    Department shallowCopy() {
        Department copy = new Department();
        copy.name = this.name;
        copy.employees = this.employees; // Same list reference!
        return copy;
    }

    // Deep copy — employees list is independent
    Department deepCopy() {
        Department copy = new Department();
        copy.name = this.name;
        copy.employees = this.employees.stream()
            .map(Employee::new) // Copy constructor for each employee
            .collect(Collectors.toList());
        return copy;
    }
}
```

**Common Mistakes:** Assuming `clone()` does deep copy — it's shallow by default. Not deep-copying nested mutable objects. Using serialization for deep copy works but is slow and requires `Serializable`. Forgetting that `String` is immutable so it doesn't need deep copying, but `Date` and collections do.

**Closing:** Always be explicit about whether you need shallow or deep copy — subtle bugs from shared mutable references are hard to track down in production.

---

## Q46. What is reflection?

**Summary:** Reflection allows inspecting and modifying class structure, methods, fields, and constructors at runtime — it's the backbone of frameworks like Spring, Hibernate, and JUnit.

**Practical Explanation:** Using `java.lang.reflect` API, you can discover class metadata, invoke methods dynamically, access private fields, create instances without `new`, and inspect annotations — all at runtime. The `Class` object is the entry point.

**When/Why Used:** Frameworks use reflection extensively — Spring's dependency injection, Hibernate's ORM mapping, Jackson's JSON serialization, JUnit's test discovery. As application developers, we rarely use it directly, but understanding it helps debug framework behavior.

**Real-World Scenario:**
```java
// How Spring roughly works behind the scenes
Class<?> clazz = Class.forName("com.example.UserService");
Object instance = clazz.getDeclaredConstructor().newInstance();

// Inject dependencies
Field field = clazz.getDeclaredField("userRepository");
field.setAccessible(true);  // Access private field
field.set(instance, repositoryInstance);

// How JUnit discovers test methods
Arrays.stream(clazz.getDeclaredMethods())
    .filter(m -> m.isAnnotationPresent(Test.class))
    .forEach(m -> m.invoke(instance));
```

**Common Mistakes:** Using reflection in application code when regular OOP would suffice — it's slow, breaks encapsulation, and loses compile-time safety. `setAccessible(true)` bypasses access modifiers, which is a security concern. Also, reflection doesn't play well with refactoring — method/field name changes won't be caught at compile time.

**Closing:** Reflection is a power tool — essential for framework developers, but application developers should understand it for debugging rather than using it directly.

---

## Q47. What is a package?

**Summary:** A package is a namespace that organizes related classes and interfaces into logical groups — it prevents naming conflicts and controls access through package-level visibility.

**Practical Explanation:** Packages map to directory structures (`com.example.service` → `com/example/service/`). They provide namespace isolation (two classes with the same name in different packages), access control (package-private is the default access), and logical organization of large codebases.

**When/Why Used:** Every Java project uses packages. Standard conventions:
- `com.company.project.controller` — REST controllers
- `com.company.project.service` — Business logic
- `com.company.project.repository` — Data access
- `com.company.project.model` — Domain entities
- `com.company.project.config` — Configuration classes
- `com.company.project.exception` — Custom exceptions

**Real-World Scenario:** In a Spring Boot microservice:
```
com.example.orderservice
├── controller/    → OrderController.java
├── service/       → OrderService.java
├── repository/    → OrderRepository.java
├── model/         → Order.java, OrderItem.java
├── dto/           → OrderRequest.java, OrderResponse.java
├── exception/     → OrderNotFoundException.java
└── config/        → AppConfig.java, SecurityConfig.java
```

**Common Mistakes:** Default (package-private) access is often overlooked — classes and methods without explicit access modifiers are only visible within the same package. This is actually useful for hiding implementation details. Also, avoid circular package dependencies — if `service` depends on `repository` and `repository` depends on `service`, your architecture has a problem.

**Closing:** Good package structure reflects clean architecture — it's the first thing I look at when joining a new project to understand the codebase organization.

---

*End of Core Java & OOP — 47 Interview Q&A*
