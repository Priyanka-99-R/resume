# Core Java — Interview Q&A

A focused set of Core Java questions and answers for a Wipro lateral (4+ years) backend / full-stack role. Answers are concise and oriented toward day-to-day Spring Boot development.

---

## OOP Concepts

### Q: What are the four pillars of OOP? Give a short example of each.

**Encapsulation, Abstraction, Inheritance, Polymorphism.**

- **Encapsulation** — bundling data with the methods that operate on it, hiding internal state via private fields and public getters/setters.
- **Abstraction** — exposing *what* an object does while hiding *how* (interfaces/abstract classes).
- **Inheritance** — a subclass reuses and extends a parent type.
- **Polymorphism** — one interface, many implementations (overriding / overloading).

```java
// Encapsulation
class Account {
    private double balance;                       // hidden state
    public double getBalance() { return balance; }
    public void deposit(double amt) {             // controlled mutation
        if (amt > 0) balance += amt;
    }
}

// Abstraction
interface Payment { void pay(double amount); }

// Inheritance + Polymorphism
class UpiPayment implements Payment {
    public void pay(double amount) { /* UPI logic */ }
}
class CardPayment implements Payment {
    public void pay(double amount) { /* card logic */ }
}

Payment p = new UpiPayment();   // polymorphic reference
p.pay(500);
```

---

### Q: What is the difference between abstraction and encapsulation?

- **Abstraction** is about *design* — hiding complexity and exposing only essential behaviour. Achieved with interfaces and abstract classes.
- **Encapsulation** is about *implementation* — hiding the internal data/state by restricting access (private fields + accessors).

In short: abstraction hides **complexity**, encapsulation hides **data**. Abstraction is solved at the design level; encapsulation at the data-protection level.

---

### Q: Abstract class vs interface — when do you use which?

| Aspect | Abstract class | Interface |
|---|---|---|
| Multiple inheritance | No (single) | Yes (multiple) |
| Fields | Instance fields allowed | Only `public static final` constants |
| Constructors | Yes | No |
| Methods | Concrete + abstract | Abstract + `default` + `static` (Java 8+), `private` (Java 9+) |
| Access modifiers | Any | Methods implicitly `public` |

Use an **abstract class** when you have shared state/common code and a clear "is-a" hierarchy. Use an **interface** when you only define a contract or need a type to mix in multiple behaviours.

```java
abstract class Shape {
    String name;                         // shared state
    abstract double area();              // contract
    void describe() { System.out.println(name + " area=" + area()); }
}

interface Drawable { void draw(); }      // pure contract
```

---

### Q: What are default and static methods in interfaces?

Introduced in Java 8 so interfaces could evolve without breaking implementers.

- **default** — a method with a body that implementing classes inherit (and may override).
- **static** — a utility method that belongs to the interface itself, called as `Interface.method()`.

```java
interface Calculator {
    int apply(int a, int b);

    default int applyTwice(int a, int b) {       // inherited, overridable
        return apply(apply(a, b), b);
    }

    static Calculator adder() {                  // factory utility
        return (a, b) -> a + b;
    }
}
```

If a class implements two interfaces with the same default method, it must override it to resolve the conflict (the "diamond" problem).

---

### Q: Overloading vs overriding?

- **Overloading** — same method name, different parameter list, in the *same* class. Resolved at **compile time** (static binding). Return type alone cannot distinguish overloads.
- **Overriding** — subclass redefines a superclass method with the *same* signature. Resolved at **runtime** (dynamic dispatch). Use `@Override`.

```java
class Printer {
    void print(int x) {}                  // overload
    void print(String x) {}               // overload
}

class Base { void run() {} }
class Derived extends Base {
    @Override void run() {}               // override
}
```

Overriding rules: same signature, return type covariant or same, access not more restrictive, cannot throw broader checked exceptions.

---

### Q: What do `super` and `this` mean?

- **`this`** — reference to the current object; used to access fields/methods and to call another constructor (`this(...)`) in the same class.
- **`super`** — reference to the parent class; used to call the parent constructor (`super(...)`) or access an overridden parent method/field.

```java
class Vehicle {
    int wheels;
    Vehicle(int w) { this.wheels = w; }
}
class Car extends Vehicle {
    String model;
    Car(String model) {
        super(4);                 // parent constructor
        this.model = model;       // current field
    }
}
```

---

### Q: Explain constructors and constructor chaining.

A **constructor** initializes a new object; it has the class name and no return type. If you write none, the compiler adds a default no-arg constructor.

**Chaining** is invoking one constructor from another:
- `this(...)` — call another constructor in the **same** class.
- `super(...)` — call a **parent** constructor (must be the first statement).

```java
class User {
    String name;
    int age;
    User() { this("Unknown", 0); }            // this-chaining
    User(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

The constructor call always travels up to `Object` first, so parent state is initialized before the child.

---

### Q: Static vs instance members?

- **static** — belong to the class, shared across all objects, loaded once. Accessed via the class name. Cannot use `this` or directly access instance members.
- **instance** — belong to each object, one copy per instance.

```java
class Counter {
    static int total;        // shared
    int id;                  // per object

    Counter() { id = ++total; }
}
```

`static` blocks run once at class-load; instance blocks run before each constructor body.

---

### Q: Difference between final, finally, and finalize?

- **`final`** — a keyword/modifier: a `final` variable is a constant, a `final` method can't be overridden, a `final` class can't be extended.
- **`finally`** — a block that always executes after try/catch, used for cleanup.
- **`finalize()`** — a method (deprecated since Java 9) the GC *may* call before reclaiming an object. Unreliable — never depend on it; use try-with-resources/`Cleaner` instead.

```java
final int MAX = 100;                      // final

try { risky(); }
catch (Exception e) { log(e); }
finally { closeResources(); }             // finally
```

---

## Core Java

### Q: JDK vs JRE vs JVM?

- **JVM** (Java Virtual Machine) — the abstract engine that loads, verifies, and executes bytecode; provides platform independence.
- **JRE** (Java Runtime Environment) — JVM + core libraries needed to *run* Java apps.
- **JDK** (Java Development Kit) — JRE + development tools (`javac`, `jar`, `javadoc`, debugger) needed to *build* Java apps.

Containment: **JDK ⊃ JRE ⊃ JVM**.

---

### Q: How does Java achieve platform independence?

Source `.java` is compiled by `javac` into **bytecode** (`.class`), which is platform-neutral. Each OS has its own JVM that interprets/JIT-compiles that same bytecode to native instructions. Hence **"write once, run anywhere"** — the bytecode is portable; only the JVM is platform-specific.

---

### Q: String vs StringBuilder vs StringBuffer?

| | String | StringBuilder | StringBuffer |
|---|---|---|---|
| Mutability | Immutable | Mutable | Mutable |
| Thread-safe | Yes (immutable) | No | Yes (synchronized) |
| Performance | Slow for concat | Fastest | Slower than builder |

Use **String** for constants, **StringBuilder** for single-threaded concatenation in loops, **StringBuffer** only when multiple threads mutate the same buffer.

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 5; i++) sb.append(i);
String result = sb.toString();   // "01234"
```

---

### Q: Why are Strings immutable and what is the string pool?

Strings are immutable so they can be safely shared, cached, used as HashMap keys, and remain secure (e.g., in class loading, network connections). Once created, a String's value never changes — any "modification" creates a new object.

The **string pool** is a special area in the heap where string literals are interned and reused. Two identical literals point to the same pooled object; `new String("x")` forces a separate heap object.

```java
String a = "hello";
String b = "hello";
System.out.println(a == b);              // true  (same pool object)

String c = new String("hello");
System.out.println(a == c);              // false (new heap object)
System.out.println(a == c.intern());     // true  (interned to pool)
```

---

### Q: == vs equals()?

- **`==`** compares **references** (whether two variables point to the same object) for objects, or raw values for primitives.
- **`equals()`** compares **logical equality** as defined by the class. `Object.equals` defaults to `==`, but classes like `String` and the wrappers override it for value comparison.

```java
Integer x = 1000, y = 1000;
System.out.println(x == y);          // false (different objects)
System.out.println(x.equals(y));     // true  (same value)
```

---

### Q: Explain the equals() and hashCode() contract.

1. If `a.equals(b)` is true, then `a.hashCode() == b.hashCode()` **must** be true.
2. Equal hashCodes do **not** require equal objects (collisions are allowed).
3. Both must be consistent across multiple calls.

If you override `equals`, you must override `hashCode`, otherwise hash-based collections (HashMap, HashSet) break.

```java
class Point {
    int x, y;
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point p)) return false;
        return x == p.x && y == p.y;
    }
    @Override public int hashCode() { return Objects.hash(x, y); }
}
```

---

### Q: What is autoboxing and unboxing?

Automatic conversion between primitives and their wrapper objects.

- **Autoboxing** — `int` → `Integer`.
- **Unboxing** — `Integer` → `int`.

```java
List<Integer> nums = new ArrayList<>();
nums.add(5);              // autobox int -> Integer
int n = nums.get(0);     // unbox Integer -> int
```

Watch out: unboxing a `null` wrapper throws `NullPointerException`, and the `Integer` cache (-128..127) can make `==` behave unexpectedly.

---

### Q: Is Java pass-by-value or pass-by-reference?

Java is **always pass-by-value**. For objects, the *value of the reference* (the pointer) is copied. So you can mutate the object's internal state through the copied reference, but reassigning the parameter does not affect the caller's variable.

```java
void mutate(StringBuilder sb) { sb.append("X"); } // affects caller's object
void reassign(StringBuilder sb) { sb = new StringBuilder("new"); } // no effect outside
```

---

### Q: What are wrapper classes and why are they needed?

Wrapper classes (`Integer`, `Long`, `Double`, `Boolean`, `Character`, etc.) wrap primitives in objects. Needed because generics and collections work only with objects (`List<Integer>`, not `List<int>`), and wrappers provide utility methods (`Integer.parseInt`, `Integer.MAX_VALUE`) and allow `null`. They are immutable and cached for small values.

---

## Collections

### Q: Describe the Collections framework hierarchy.

`Iterable` → `Collection` splits into **List**, **Set**, **Queue**. `Map` is a separate root (not a `Collection`).

- **List**: `ArrayList`, `LinkedList`, `Vector`
- **Set**: `HashSet`, `LinkedHashSet`, `TreeSet`
- **Queue/Deque**: `PriorityQueue`, `ArrayDeque`, `LinkedList`
- **Map**: `HashMap`, `LinkedHashMap`, `TreeMap`, `Hashtable`, `ConcurrentHashMap`

---

### Q: List vs Set vs Map?

- **List** — ordered, indexed, allows duplicates.
- **Set** — no duplicates; may be unordered (HashSet) or ordered (TreeSet/LinkedHashSet).
- **Map** — key→value pairs, unique keys.

```java
List<String> list = new ArrayList<>();   // [a, a, b] allowed
Set<String>  set  = new HashSet<>();     // {a, b}
Map<String,Integer> map = new HashMap<>(); // {a=1, b=2}
```

---

### Q: ArrayList vs LinkedList?

| | ArrayList | LinkedList |
|---|---|---|
| Backing | Dynamic array | Doubly linked list |
| Random access (get) | O(1) | O(n) |
| Insert/delete at ends | Amortized O(1) / O(n) middle | O(1) at ends |
| Memory | Less overhead | Extra node pointers |

Use **ArrayList** for read-heavy/index access (the common default). Use **LinkedList** only for frequent add/remove at the ends or as a Deque/Queue.

---

### Q: How does HashMap work internally?

- It stores entries in an array of **buckets**. The key's `hashCode()` is spread (`hash = h ^ (h >>> 16)`) and `& (n-1)` gives the bucket index.
- On collision, entries form a **linked list** in the bucket. Since Java 8, when a bucket exceeds **8** entries (and capacity ≥ 64) it **treeifies** into a red-black tree → O(log n) lookups instead of O(n).
- `get`/`put` use `hashCode()` to find the bucket, then `equals()` to find the exact key.
- Default capacity **16**, load factor **0.75**; exceeding `capacity * loadFactor` triggers a **resize** (doubling + rehash).

```java
Map<String,Integer> m = new HashMap<>();
m.put("a", 1);            // bucket = hash("a") & 15
m.get("a");               // hash -> bucket -> equals match
```

---

### Q: HashMap vs Hashtable vs ConcurrentHashMap?

| | HashMap | Hashtable | ConcurrentHashMap |
|---|---|---|---|
| Thread-safe | No | Yes (method-level sync) | Yes (fine-grained) |
| Null key/values | 1 null key, many null values | None | None |
| Performance | Fastest single-thread | Slow (whole-map lock) | Best concurrent |
| Legacy | No | Yes (avoid) | No |

`ConcurrentHashMap` locks only segments/bins (CAS + bin-level synchronization), so reads are mostly lock-free and writes don't block the whole map. Prefer it over `Hashtable` for concurrency.

---

### Q: HashSet vs TreeSet?

- **HashSet** — backed by a HashMap; O(1) operations; **no ordering**; allows one `null`.
- **TreeSet** — backed by a red-black tree (`NavigableSet`); O(log n); **sorted** by natural order or a `Comparator`; no `null`; offers `first()`, `last()`, `ceiling()`, `floor()`.

Use HashSet for fast membership checks, TreeSet when you need sorted/range queries.

---

### Q: Comparable vs Comparator?

- **Comparable** — natural ordering, implemented by the class itself via `compareTo()`. One sort order.
- **Comparator** — external ordering, defined separately via `compare()`. Multiple sort orders, doesn't modify the class.

```java
class Emp implements Comparable<Emp> {
    int salary; String name;
    public int compareTo(Emp o) { return Integer.compare(salary, o.salary); } // natural
}

// Comparator: sort by name, then salary
Comparator<Emp> byName = Comparator.comparing((Emp e) -> e.name)
                                   .thenComparingInt(e -> e.salary);
list.sort(byName);
```

---

### Q: Fail-fast vs fail-safe iterators?

- **Fail-fast** — throw `ConcurrentModificationException` if the collection is structurally modified during iteration (uses a `modCount`). Examples: `ArrayList`, `HashMap`. They iterate over the original.
- **Fail-safe** — iterate over a **copy/snapshot**, so they don't throw on modification (but may not reflect recent changes). Examples: `CopyOnWriteArrayList`, `ConcurrentHashMap`.

```java
for (String s : list) {
    list.remove(s);          // ConcurrentModificationException (fail-fast)
}
// Use iterator.remove() or a concurrent collection instead.
```

---

## Exceptions

### Q: Checked vs unchecked exceptions?

- **Checked** — extend `Exception` (not `RuntimeException`); checked at compile time; must be caught or declared with `throws`. E.g., `IOException`, `SQLException`.
- **Unchecked** — extend `RuntimeException`; not enforced by the compiler; usually programming bugs. E.g., `NullPointerException`, `IllegalArgumentException`.
- **Errors** — `OutOfMemoryError`, `StackOverflowError`; serious, not meant to be caught.

---

### Q: throw vs throws?

- **`throw`** — actually throws an exception instance (a statement).
- **`throws`** — declares in a method signature that it may throw exceptions (delegates handling to the caller).

```java
void validate(int age) throws IllegalAccessException {   // throws (declaration)
    if (age < 18) throw new IllegalAccessException("Minor"); // throw (action)
}
```

---

### Q: What is try-with-resources?

A construct (Java 7+) that auto-closes resources implementing `AutoCloseable`, eliminating manual `finally` close. Resources close in reverse order of declaration, even on exception.

```java
try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
    return br.readLine();
}   // br.close() called automatically
```

This avoids resource leaks and suppressed-exception bugs common with manual `finally`.

---

### Q: How do you create a custom exception?

Extend `Exception` (checked) or `RuntimeException` (unchecked). In Spring Boot, custom unchecked exceptions are common and mapped to HTTP responses via `@ControllerAdvice`/`@ExceptionHandler`.

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String msg) { super(msg); }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
@ExceptionHandler(ResourceNotFoundException.class)
public ErrorResponse handle(ResourceNotFoundException ex) {
    return new ErrorResponse(ex.getMessage());
}
```

---

### Q: Describe the exception hierarchy.

`Throwable` is the root, split into:
- **`Error`** — JVM-level, unrecoverable (`OutOfMemoryError`, `StackOverflowError`).
- **`Exception`** — application-level.
  - **Checked** — direct subclasses of `Exception` (e.g., `IOException`).
  - **Unchecked** — `RuntimeException` and its subclasses (e.g., `NullPointerException`).

---

### Q: What happens with finally and a return statement?

`finally` always runs, even if `try`/`catch` has a `return`. If `finally` itself returns, it **overrides** the value from try/catch (an anti-pattern — avoid returning from `finally`).

```java
int test() {
    try { return 1; }
    finally { return 2; }   // returns 2, swallows the 1
}
```

`finally` does not run only for `System.exit()` or JVM crash.

---

## Java 8+ Features

### Q: What are lambda expressions?

Anonymous functions that provide a concise implementation of a functional interface (single abstract method). They reduce boilerplate of anonymous classes.

```java
Runnable r = () -> System.out.println("run");
Comparator<Integer> c = (a, b) -> a - b;
list.forEach(item -> System.out.println(item));
```

---

### Q: What are the built-in functional interfaces (Predicate/Function/Consumer/Supplier)?

From `java.util.function`:

- **Predicate\<T\>** — `boolean test(T)` — a condition.
- **Function\<T,R\>** — `R apply(T)` — transforms input to output.
- **Consumer\<T\>** — `void accept(T)` — consumes, no return.
- **Supplier\<T\>** — `T get()` — supplies a value, no input.

```java
Predicate<Integer> isEven = n -> n % 2 == 0;
Function<String,Integer> len = String::length;
Consumer<String> printer = System.out::println;
Supplier<LocalDate> today = LocalDate::now;
```

Also `BiFunction`, `UnaryOperator`, `BinaryOperator`.

---

### Q: Explain the Stream API with filter/map/collect/reduce examples.

A Stream is a pipeline for processing collections functionally (not a data structure). Operations are **lazy** until a terminal op runs.

```java
List<Employee> emps = ...;

// filter + map + collect
List<String> names = emps.stream()
    .filter(e -> e.getSalary() > 50000)
    .map(Employee::getName)
    .collect(Collectors.toList());

// reduce — total salary
double total = emps.stream()
    .map(Employee::getSalary)
    .reduce(0.0, Double::sum);

// grouping
Map<String, List<Employee>> byDept = emps.stream()
    .collect(Collectors.groupingBy(Employee::getDept));
```

---

### Q: What are method references?

Shorthand for lambdas that just call an existing method. Four kinds:

```java
Function<String,Integer> a = Integer::parseInt;     // static
Supplier<String> b = str::toUpperCase;              // instance of particular object
Function<String,Integer> c = String::length;        // instance of arbitrary object
Supplier<ArrayList<String>> d = ArrayList::new;     // constructor
```

---

### Q: What is Optional and why use it?

`Optional<T>` is a container that may or may not hold a value, designed to avoid `NullPointerException` and make "absence" explicit in APIs.

```java
Optional<User> user = repo.findById(id);

String name = user.map(User::getName)
                  .orElse("Guest");

user.ifPresent(u -> log(u));

User u = repo.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Not found"));
```

Prefer `orElseGet`/`orElseThrow`/`map`; avoid `get()` without a presence check. Don't use Optional for fields or parameters.

---

### Q: Why were default methods added in Java 8?

To allow interfaces to add new methods **without breaking** existing implementations — critical for evolving the JDK (e.g., adding `stream()`, `forEach()` to `Collection`). Implementers inherit the default behaviour automatically.

---

### Q: Intermediate vs terminal stream operations?

- **Intermediate** — return a new Stream, are **lazy**, chainable: `filter`, `map`, `sorted`, `distinct`, `limit`, `peek`.
- **Terminal** — trigger execution and produce a result/side-effect, **consume** the stream: `collect`, `forEach`, `reduce`, `count`, `findFirst`, `anyMatch`.

Without a terminal operation, no intermediate operation executes (lazy evaluation). A stream cannot be reused after a terminal op.

---

## Multithreading

### Q: Thread vs Runnable — which is better?

`Runnable` is preferred because:
- Java allows only single class inheritance — extending `Thread` blocks extending anything else.
- `Runnable` separates the task from the thread mechanism and works with thread pools / `ExecutorService`.

```java
Runnable task = () -> System.out.println("running");
new Thread(task).start();   // preferred

class MyThread extends Thread {   // less flexible
    public void run() { /* ... */ }
}
```

---

### Q: What is the thread lifecycle?

**NEW** → **RUNNABLE** (ready/running) → **RUNNING**; can go to **BLOCKED** (waiting for a monitor lock), **WAITING** (`wait()`, `join()`), or **TIMED_WAITING** (`sleep(ms)`, `wait(ms)`), then back to RUNNABLE, finally **TERMINATED** when `run()` completes.

---

### Q: What does synchronized do?

Provides mutual exclusion and visibility: only one thread can hold an object's monitor lock at a time, preventing race conditions. Can be applied to a method or a block.

```java
class Counter {
    private int count;
    public synchronized void increment() { count++; }   // method-level

    private final Object lock = new Object();
    public void dec() {
        synchronized (lock) { count--; }                // block-level
    }
}
```

Block-level locking is preferred for finer granularity.

---

### Q: What is the volatile keyword?

`volatile` guarantees **visibility** — reads/writes go directly to main memory, so changes by one thread are immediately seen by others. It prevents instruction reordering for that variable. It does **not** provide atomicity for compound operations (`count++` is still unsafe).

```java
private volatile boolean running = true;   // visible across threads
public void stop() { running = false; }
```

Use `volatile` for simple flags; use `AtomicInteger`/locks for compound updates.

---

### Q: Explain wait() and notify().

Inter-thread communication methods on `Object`, used inside a `synchronized` block on that object's monitor.

- **`wait()`** — releases the lock and suspends the thread until notified.
- **`notify()` / `notifyAll()`** — wake one / all waiting threads.

```java
synchronized (queue) {
    while (queue.isEmpty()) queue.wait();   // always wait in a loop
    process(queue.poll());
}
// producer:
synchronized (queue) { queue.add(item); queue.notifyAll(); }
```

Always call `wait()` in a loop to guard against spurious wakeups.

---

### Q: What is a deadlock and how do you avoid it?

A deadlock occurs when two or more threads each hold a lock the other needs, so all wait forever.

```java
// Thread 1: lock A then B   | Thread 2: lock B then A  -> deadlock
```

Avoid by: acquiring locks in a **consistent global order**, using `tryLock()` with timeouts, reducing lock scope, or using higher-level concurrency utilities (`java.util.concurrent`).

---

### Q: What is ExecutorService and why use thread pools?

`ExecutorService` manages a pool of reusable threads, decoupling task submission from execution. Thread pools avoid the cost of creating threads per task and cap concurrency.

```java
ExecutorService pool = Executors.newFixedThreadPool(4);
Future<Integer> f = pool.submit(() -> compute());
Integer result = f.get();        // blocks for result
pool.shutdown();
```

Common factories: `newFixedThreadPool`, `newCachedThreadPool`, `newSingleThreadExecutor`, `newScheduledThreadPool`. For control, configure `ThreadPoolExecutor` directly.

---

### Q: Callable vs Runnable?

| | Runnable | Callable\<V\> |
|---|---|---|
| Method | `void run()` | `V call()` |
| Return value | No | Yes |
| Checked exceptions | Cannot throw | Can throw |
| Used with | `Thread`, executor | `executor.submit()` → `Future` |

```java
Callable<Integer> task = () -> 42;
Future<Integer> future = pool.submit(task);
int value = future.get();
```

---

### Q: synchronized vs Lock (ReentrantLock)?

| | synchronized | Lock |
|---|---|---|
| Acquire/release | Implicit (block scope) | Explicit `lock()`/`unlock()` |
| Try / timeout | No | `tryLock(timeout)` |
| Interruptible | No | `lockInterruptibly()` |
| Fairness | No | Configurable |
| Read/write split | No | `ReadWriteLock` |

```java
ReentrantLock lock = new ReentrantLock();
lock.lock();
try { /* critical section */ }
finally { lock.unlock(); }   // always unlock in finally
```

`Lock` is more flexible; `synchronized` is simpler and sufficient for most cases.

---

## Memory & Garbage Collection

### Q: Stack vs heap memory?

- **Stack** — per-thread; stores method frames, local primitives, and object **references**. LIFO, fast, auto-freed when a method returns. Overflow → `StackOverflowError`.
- **Heap** — shared across threads; stores all **objects** and instance fields. Managed by the garbage collector. Exhaustion → `OutOfMemoryError`.

```java
void m() {
    int x = 5;                 // x on stack
    Person p = new Person();   // reference on stack, object on heap
}
```

---

### Q: How does garbage collection work?

The GC automatically reclaims heap memory occupied by objects that are no longer **reachable** from GC roots (stack references, static fields, etc.).

- The heap is generational: **Young** (Eden + 2 Survivor spaces) and **Old/Tenured**.
- Most objects die young → **Minor GC** cleans the Young gen; surviving objects are promoted to Old gen → **Major/Full GC**.
- Uses mark-and-sweep (and compaction). Modern collectors: **G1** (default), **ZGC**, **Shenandoah** for low pauses.
- You cannot force GC; `System.gc()` is only a hint. Prevent leaks by clearing references and using try-with-resources.

---
