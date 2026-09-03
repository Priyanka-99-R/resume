# Java 17 — Version-Specific Features (Easy Version)

> **Why this file matters:** almost every interview asks *"What's new since Java 8?"* or *"What Java 17 features have you used?"*
> Every topic below follows the same shape — **Easiest way to remember → Old way vs New way → Real-world example → Diagram → Interview trap → Interview-ready answer → Easy memory box.**

---

## Contents

1. [Java 17 Basics (LTS, Java 8 vs 17)](#java-17-basics)
2. [Records](#records-java-16)
3. [Sealed Classes](#sealed-classes-java-17)
4. [Switch Expressions](#switch-expressions-java-14)
5. [Pattern Matching](#pattern-matching-for-instanceof-java-16)
6. [Text Blocks](#text-blocks-java-15)
7. [Other Features (Java 9–17)](#other-notable-features-java-917)
8. [Interview Scenarios](#likely-interview-scenarios)

---

## Java 17 Basics

### Q: Why is Java 17 important?

The easiest way to remember:

```
Java 17 = an LTS (Long-Term Support) release,
          just like Java 8, 11 and 21.

Companies stay on LTS versions because they get security patches for YEARS.
```

#### The LTS timeline

```
   8 ────── 11 ────── 17 ────── 21          ← LTS (companies use these) ⭐
2014       2018      2021      2023

     9,10   12–16     18–20     22,23        ← non-LTS (6 months each, skipped)
```

Real-world idea: **phone operating systems.**

```
LTS release      → an Android version that gets security updates for 5 years
Non-LTS release  → a beta build that is replaced in 6 months
```

A bank will never run its production system on something that stops receiving patches in six months. That is the entire reason LTS matters.

#### What Java 17 actually bundles

Java 17 is not one big feature — it is **everything from Java 9 to 17 packaged into one supported release**:

```
Java  9 → modules, List.of(), takeWhile
Java 10 → var
Java 11 → HttpClient, String.strip(), run a .java file directly
Java 14 → switch expressions, helpful NullPointerExceptions
Java 15 → text blocks
Java 16 → records, pattern matching for instanceof
Java 17 → sealed classes                              ⭐ LTS
```

Also important: **Spring Boot 3.x requires Java 17 as a minimum.** That is why so many companies moved in 2023–24, and why interviewers now expect you to know it.

#### Interview-ready answer

> Java 17 is a Long-Term Support release from September 2021, in the same family as Java 8, 11 and 21. LTS versions receive security patches and updates for many years, so enterprises standardise on them, while the non-LTS releases in between are short-lived. Java 17 is significant because it bundles everything introduced from Java 9 onwards — records, sealed classes, pattern matching, switch expressions and text blocks — into one stable, supported release. It also became a practical requirement because Spring Boot 3 needs Java 17 as a minimum.

#### Easy memory

```
LTS = 8 → 11 → 17 → 21   (long support, companies use these) ⭐
Non-LTS = everything else (6 months, skipped)

Java 17 = Java 9…17 features + long support
Spring Boot 3 REQUIRES Java 17
```

---

### Q: How is Java 17 different from Java 8?

The easiest way to remember — **Java 8 gave us functional programming, Java 17 removed boilerplate.**

```
Java 8  → HOW we process data   → lambdas, streams, Optional
Java 17 → HOW we write classes  → records, sealed, pattern matching, text blocks
```

#### The comparison table

| Topic | Java 8 | Java 17 |
|---|---|---|
| DTO class | 60 lines of getters/equals/hashCode | `record Employee(Long id, String name) {}` — 1 line |
| `switch` | statement, needs `break` | **expression**, returns a value, arrow syntax |
| Multi-line String | `"line1\n" + "line2\n"` | **text block** with `"""` |
| Type check | `instanceof` + explicit cast | **pattern matching**: `if (o instanceof String s)` |
| Restricting subclasses | not possible | **sealed classes** |
| Local variables | `ArrayList<String> list = new ArrayList<>();` | `var list = new ArrayList<String>();` |
| Immutable list | `Collections.unmodifiableList(...)` | `List.of("a","b")` |
| NullPointerException | "NullPointerException" (which one?!) | tells you the **exact variable** |
| HTTP calls | `HttpURLConnection` (painful) | built-in `HttpClient` with HTTP/2 |

#### See the difference in one example

**Java 8 DTO:**

```java
public class Employee {
    private final Long id;
    private final String name;

    public Employee(Long id, String name) {
        this.id = id; this.name = name;
    }
    public Long getId() { return id; }
    public String getName() { return name; }

    @Override public boolean equals(Object o) { /* 10 lines */ }
    @Override public int hashCode() { return Objects.hash(id, name); }
    @Override public String toString() { /* 3 lines */ }
}
```

**Java 17:**

```java
public record Employee(Long id, String name) {}
```

Same functionality. 25 lines became 1.

#### Interview-ready answer

> Java 8 was about functional programming — it introduced lambdas, the Stream API, `Optional` and functional interfaces, which changed how we process data. Java 17 is mainly about reducing boilerplate and improving language expressiveness. Records replace verbose DTO classes, sealed classes let us define a closed hierarchy, pattern matching removes the cast after `instanceof`, switch expressions return values with no fall-through, and text blocks make multi-line JSON or SQL readable. There are also practical improvements like `var`, immutable collection factories such as `List.of()`, helpful NullPointerException messages that name the exact variable, and a modern built-in `HttpClient`.

#### Easy memory

```
Java 8  = functional  → lambda, stream, Optional     (HOW we process)
Java 17 = less code   → record, sealed, pattern, """  (HOW we write)

25-line DTO  →  record Employee(Long id, String name) {}
```

---

## Records (Java 16)

### Q: What is a record?

The easiest way to remember:

```
record = a class whose ONLY job is to carry data.

Java writes the constructor, getters, equals(), hashCode() and toString() for you.
```

#### The pain it removes

```java
// BEFORE — the classic DTO
public class Employee {
    private final Long id;
    private final String name;
    private final String dept;

    public Employee(Long id, String name, String dept) { ... }

    public Long getId() { ... }
    public String getName() { ... }
    public String getDept() { ... }

    @Override public boolean equals(Object o) { ... }
    @Override public int hashCode() { ... }
    @Override public String toString() { ... }
}
```

```java
// AFTER
public record Employee(Long id, String name, String dept) {}
```

#### What the compiler generates for you

```
record Employee(Long id, String name, String dept)
        │
        ├── private final Long id;            ← fields (final!)
        ├── private final String name;
        ├── private final String dept;
        │
        ├── Employee(Long, String, String)    ← canonical constructor
        │
        ├── id()      name()      dept()      ← accessors (NO "get" prefix ⭐)
        │
        ├── equals()      ← compares ALL fields
        ├── hashCode()    ← uses ALL fields
        └── toString()    ← Employee[id=1, name=Priyanka, dept=Engineering]
```

Usage:

```java
Employee e = new Employee(1L, "Priyanka", "Engineering");

e.name();          // "Priyanka"   ← note: name(), NOT getName() ⭐
e.toString();      // Employee[id=1, name=Priyanka, dept=Engineering]

Employee e2 = new Employee(1L, "Priyanka", "Engineering");
e.equals(e2);      // true — value-based equality, for free ✅
```

Real-world idea: an **ID card**.

```
An ID card only CARRIES information — name, number, photo.
It has no behaviour, and nobody edits it after printing.

That is exactly a record: data in, never changed.
```

#### Rules and limits ⭐

```
✅ Fields are implicitly private FINAL  → immutable
✅ The record class itself is implicitly FINAL → cannot be extended
✅ CAN implement interfaces
✅ CAN have static fields and static methods
✅ CAN have extra instance methods
✅ CAN have a compact constructor for validation

❌ CANNOT extend another class (it already extends java.lang.Record)
❌ CANNOT add instance fields outside the header
❌ CANNOT have setters (it is immutable)
```

#### The compact constructor — for validation

```java
public record Employee(Long id, String name) {

    public Employee {                                  // no parameter list! ⭐
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        name = name.trim();                            // you can normalise too
    }
}
```

```
new Employee(1L, "  Priya  ")
        ↓
compact constructor runs → validates + trims
        ↓
fields are assigned automatically ✅
```

You can also add methods:

```java
public record Employee(Long id, String firstName, String lastName) {

    public String fullName() {                    // extra behaviour ✅
        return firstName + " " + lastName;
    }

    public static Employee of(String first, String last) {   // factory ✅
        return new Employee(null, first, last);
    }
}
```

#### Record vs Lombok `@Data` ⭐

| | record | Lombok `@Data` |
|---|---|---|
| What it is | native Java language feature | third-party annotation processor |
| Extra dependency | ❌ none | ✅ needs the Lombok jar + IDE plugin |
| Mutability | immutable only | mutable (generates setters) |
| Accessors | `name()` | `getName()` |
| Inheritance | cannot extend a class | can |
| Best for | DTOs, API request/response, value objects | JPA entities, mutable beans |

#### Where NOT to use a record — JPA entities ⚠️

```java
@Entity
public record Employee(Long id, String name) {}    // ❌ does NOT work
```

Hibernate needs a **no-arg constructor**, **mutable fields** and proxy support — a record has none of those. So:

```
DTO / API response / value object → record ✅
JPA @Entity                       → normal class (with Lombok) ✅
```

That distinction is exactly what interviewers probe.

#### Interview-ready answer

> A record, finalised in Java 16, is a special class designed to carry immutable data. We declare the components in the header and the compiler generates the private final fields, the canonical constructor, accessor methods, and `equals()`, `hashCode()` and `toString()` based on all components. The accessors are named after the component, so it is `name()` rather than `getName()`. Records are implicitly final and cannot extend another class, though they can implement interfaces and can have static members, extra methods, and a compact constructor for validation. I use records for DTOs, API request and response objects and value objects. I don't use them for JPA entities, because Hibernate requires a no-argument constructor and mutable fields, so those remain normal classes.

#### Easy memory

```
record = data carrier → an ID card 🪪

record Employee(Long id, String name) {}
   → final fields + constructor + id()/name() + equals + hashCode + toString  FREE

Accessor is name(), NOT getName()  ⭐
Implicitly FINAL, immutable, cannot extend a class
Compact constructor → validation: public Employee { ... }

✅ DTO / API response      ❌ JPA @Entity (needs no-arg ctor + mutability)
```

---

## Sealed Classes (Java 17)

### Q: What are sealed classes?

The easiest way to remember:

```
sealed = "ONLY these specific classes are allowed to extend me."

It's a guest list. 🎟️
```

```java
public sealed interface Shape permits Circle, Square, Rectangle {}

public final class Circle implements Shape { }
public final class Square implements Shape { }
public non-sealed class Rectangle implements Shape { }   // reopened
```

Anyone else who tries:

```java
public class Triangle implements Shape { }   // ❌ compile error — not on the list
```

#### The problem it solves

Before sealed classes you had only two extremes:

```
public class Shape { }    → ANYONE can extend it → you lose control
final class Shape { }     → NOBODY can extend it → too restrictive
```

`sealed` is the middle ground: **a controlled, known set of subclasses.**

```
              ┌──────────────────┐
              │  sealed Shape    │
              │  permits ...     │  ← the guest list 🎟️
              └────────┬─────────┘
          ┌────────────┼────────────┐
          ↓            ↓            ↓
       Circle       Square      Rectangle
       (final)      (final)     (non-sealed)
                                     ↓
                              anyone may extend
                              Rectangle again
```

Real-world idea: a **private wedding**.

```
public class     → open ground: anyone walks in
final class      → locked room: nobody enters
sealed ... permits → invitation only 🎟️
```

Another real example: payment methods in your app are exactly UPI, Card and NetBanking. There is no fourth. Sealing that hierarchy documents the rule *and* enforces it.

#### Every permitted subclass must choose one of three words ⭐

```java
final class Circle implements Shape { }        // 1. nobody extends me
sealed class Square implements Shape           // 2. only MY list may extend me
        permits SmallSquare { }
non-sealed class Rectangle implements Shape { }// 3. I'm open again to everyone
```

```
final      → the chain STOPS here
sealed     → the chain continues, but still controlled
non-sealed → the chain is reopened to everyone
```

If you write none of the three, it will not compile.

#### The real payoff — exhaustive switch ⭐⭐

This is the reason sealed classes exist:

```java
String describe(Shape shape) {
    return switch (shape) {
        case Circle c    -> "Circle radius " + c.radius();
        case Square s    -> "Square side " + s.side();
        case Rectangle r -> "Rectangle";
    };                       // ✅ NO default needed
}
```

The compiler knows the complete list of subtypes, so it can prove the switch covers everything.

And if someone later adds a fourth shape:

```java
public sealed interface Shape permits Circle, Square, Rectangle, Triangle {}
```

```
→ every switch that doesn't handle Triangle now FAILS TO COMPILE ⭐

You find the problem at BUILD time, not at 2 a.m. in production.
```

Without sealed classes you'd need a `default` branch, which silently swallows the new case.

#### Where the permitted classes must live

```
Same file            → the `permits` clause can be omitted entirely
Same package         → allowed
Same named module    → allowed
```

#### Interview-ready answer

> Sealed classes, finalised in Java 17, let a class or interface restrict which types are allowed to extend or implement it, using the `permits` clause. Before this we only had `public`, which lets anyone extend, or `final`, which lets nobody — sealed gives a controlled middle ground where the hierarchy is known and closed. Every permitted subclass must be declared `final`, `sealed` or `non-sealed`, so the author explicitly decides whether the hierarchy continues. The main benefit is exhaustive pattern matching: because the compiler knows all the subtypes, a switch over a sealed type does not need a `default` branch, and if a new subtype is added later, every switch that fails to handle it becomes a compile error rather than a runtime bug. I would use it to model a closed domain set, such as the payment types or event types supported by a service.

#### Easy memory

```
sealed = a GUEST LIST 🎟️  → "only these classes may extend me"

public → open ground | final → locked room | sealed → invitation only

sealed interface Shape permits Circle, Square, Rectangle {}
Each child MUST be: final | sealed | non-sealed

⭐ Payoff: exhaustive switch with NO default →
   adding a new subtype breaks the BUILD, not production.
```

---

## Switch Expressions (Java 14)

### Q: What are switch expressions?

The easiest way to remember:

```
Old switch → a STATEMENT → does something → needs break → can fall through 💥
New switch → an EXPRESSION → RETURNS a value → arrow syntax → no fall-through ✅
```

#### The old pain

```java
String size;
switch (day) {
    case MONDAY:
    case FRIDAY:
    case SUNDAY:
        size = "Busy";
        break;              // ← forget this and the bug is silent 💥
    case TUESDAY:
        size = "Light";
        break;
    default:
        size = "Normal";
}
```

Three problems: you must declare the variable first, you must repeat `break`, and forgetting one `break` causes fall-through that is very hard to spot.

#### The new way

```java
String size = switch (day) {
    case MONDAY, FRIDAY, SUNDAY -> "Busy";       // multiple labels ⭐
    case TUESDAY                -> "Light";
    default                     -> "Normal";
};                                                // ← note the semicolon
```

```
Old: switch DOES something → assigns a variable → needs break
New: switch RETURNS something → assign it directly → no break ✅
```

Real-world idea: a **vending machine**.

```
Old switch → you press a button, and hope the right item drops
             (if the mechanism doesn't stop, more items keep falling = fall-through)
New switch → you press a button and it HANDS you exactly one item ✅
```

#### Multi-line branches — use `yield`

```java
int result = switch (grade) {
    case "A" -> 100;
    case "B" -> 80;
    default -> {
        System.out.println("Unknown grade, defaulting");
        yield 0;                       // ⭐ `yield` returns from a BLOCK
    }
};
```

```
Single expression → ->  value          (no yield needed)
Block { }         → ->  { ...; yield value; }
```

Note: `yield` returns from the *switch*; `return` would exit the whole method.

#### Exhaustiveness ⭐

When switching over an enum, the compiler now checks that every constant is handled:

```java
enum Status { NEW, IN_PROGRESS, DONE }

String label = switch (status) {
    case NEW         -> "Just created";
    case IN_PROGRESS -> "Working";
    case DONE        -> "Finished";
};                            // ✅ no default needed — all cases covered
```

Add a fourth enum constant later and this stops compiling — exactly what you want.

#### You can still use the old syntax with the new form

```java
switch (day) {
    case MONDAY -> System.out.println("Start");    // arrow, no return value
    default     -> System.out.println("Other");
}
```

Arrow syntax alone already removes fall-through, even when you don't return a value.

#### Interview-ready answer

> Switch expressions, finalised in Java 14, allow `switch` to return a value instead of only performing statements. They use arrow syntax, which removes fall-through entirely, so no `break` is needed, and multiple labels can be combined in a single case such as `case MONDAY, FRIDAY ->`. For multi-line branches we use `yield` to return the value from the block. Another benefit is exhaustiveness checking: when switching over an enum or a sealed type, the compiler verifies that every case is covered, so no `default` branch is needed and adding a new constant later produces a compile error instead of a silent runtime bug. Overall it removes the two classic switch problems — forgotten `break` statements and uninitialised result variables.

#### Easy memory

```
Old switch → statement → break → fall-through bugs 💥
New switch → EXPRESSION → returns a value → no break ✅

String s = switch (day) {
    case MONDAY, FRIDAY -> "Busy";     ← multiple labels
    default -> { yield "Normal"; }     ← yield for a BLOCK
};

Enum/sealed → EXHAUSTIVE → no default needed → new constant breaks the build ⭐
```

---

## Pattern Matching for instanceof (Java 16)

### Q: What is pattern matching for `instanceof`?

The easiest way to remember:

```
It merges the TYPE CHECK and the CAST into one step.

if (obj instanceof String s)     ← s is already a String, ready to use
```

#### Old vs new

```java
// OLD — you say "String" three times 😩
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.length());
}

// NEW — Java 16 ✅
if (obj instanceof String s) {
    System.out.println(s.length());     // s is already cast
}
```

```
instanceof String s
              │  └── the new variable, already cast ⭐
              └───── the type being tested
```

Real-world idea: a **security check at an airport**.

```
Old: guard verifies you're a passenger → then a SECOND person issues your pass
New: the guard verifies AND hands you the pass in one step ✅
```

#### Where it really shines — equals()

```java
// OLD
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Employee)) return false;
    Employee other = (Employee) o;            // extra line
    return id == other.id;
}

// NEW
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    return o instanceof Employee other && id == other.id;   // one line ✅
}
```

#### Scope rules — the clever part ⭐

The variable exists exactly where the check is guaranteed true:

```java
if (obj instanceof String s) {
    s.length();          // ✅ available here
}
s.length();              // ❌ not available here

if (!(obj instanceof String s)) {
    return;              // s is NOT available here...
}
s.length();              // ✅ ...but IS available after, because we returned!
```

That last pattern is very useful for guard clauses.

You can also combine conditions:

```java
if (obj instanceof String s && s.length() > 5) {     // ✅ s usable immediately
    System.out.println(s.toUpperCase());
}
```

#### Pattern matching for switch (preview in 17, final in 21)

```java
String describe(Object obj) {
    return switch (obj) {
        case Integer i -> "Integer: " + i;
        case String s  -> "String of length " + s.length();
        case null      -> "It was null";          // null can be a case ⭐
        default        -> "Something else";
    };
}
```

With sealed types it becomes exhaustive:

```java
String area(Shape shape) {
    return switch (shape) {
        case Circle c    -> "π r² = " + (Math.PI * c.radius() * c.radius());
        case Square s    -> "side² = " + (s.side() * s.side());
        case Rectangle r -> "l × b";
    };                     // no default — the compiler knows the full list ✅
}
```

Java 21 adds **record patterns**, which destructure the object:

```java
if (obj instanceof Employee(Long id, String name)) {     // Java 21
    System.out.println(name);        // fields extracted directly
}
```

#### Interview-ready answer

> Pattern matching for `instanceof`, finalised in Java 16, combines the type test and the cast into a single expression: `if (obj instanceof String s)` both checks the type and declares `s` already cast, which removes the redundant cast line. The binding variable's scope is limited to where the compiler can prove the check succeeded, so it also works after a negated check that returns early, and it can be combined with further conditions using `&&`. The most common practical use is simplifying `equals()` implementations. This feature is the foundation for pattern matching in `switch`, which was a preview in Java 17 and finalised in Java 21, and which becomes exhaustive when used with sealed types.

#### Easy memory

```
if (obj instanceof String s)  →  check + cast in ONE step ✅

Old: instanceof → then (String) obj → 3 mentions of String 😩
New: one line, `s` is ready to use

Scope: available only where the check is proven true
       (also after `if (!(o instanceof X x)) return;`)

equals(): return o instanceof Employee e && id == e.id;   ⭐
Java 21: switch pattern matching + record patterns
```

---

## Text Blocks (Java 15)

### Q: What are text blocks?

The easiest way to remember:

```
Text block = a multi-line String written between triple quotes """

No more \n, no more + concatenation, no more escaped quotes.
```

#### The old horror

```java
String json = "{\n" +
              "    \"name\": \"Priyanka\",\n" +
              "    \"role\": \"Full Stack Developer\"\n" +
              "}";
```

You cannot read that, and every `\"` is a chance to make a mistake.

#### The new way

```java
String json = """
    {
        "name": "Priyanka",
        "role": "Full Stack Developer"
    }
    """;
```

It looks exactly like the JSON it produces. Quotes need no escaping at all.

#### SQL becomes readable too

```java
String sql = """
    SELECT e.id, e.name, d.dept_name
    FROM employees e
    JOIN departments d ON e.dept_id = d.id
    WHERE e.salary > 50000
    ORDER BY e.name
    """;
```

Compare with the old version:

```java
String sql = "SELECT e.id, e.name, d.dept_name " +
             "FROM employees e " +
             "JOIN departments d ON e.dept_id = d.id " +
             "WHERE e.salary > 50000 " +
             "ORDER BY e.name";
```

Notice the classic bug in the old style: forget one trailing space and you get `...e.nameFROM employees...`.

Real-world idea: **typing a letter in Notepad versus writing it as one long line with `\n` symbols.**

#### Indentation rules — the part people get wrong ⭐

Java removes the **common leading whitespace** automatically:

```java
String s = """
        Hello
        World
        """;
```

```
The CLOSING """ decides the left margin.

        Hello        ← 8 spaces
        World        ← 8 spaces
        """          ← 8 spaces → strip 8 from every line

Result: "Hello\nWorld\n"     (no leading spaces ✅)
```

Move the closing quotes left and you keep indentation:

```java
String s = """
        Hello
        World
""";
// Result: "        Hello\n        World\n"   ← 8 spaces kept
```

#### Two useful escapes

```java
// \  → join lines (suppress the newline)
String s = """
    This is one \
    single line""";
// "This is one single line"

// \s → keep a trailing space (which would otherwise be stripped)
String t = """
    Name:\s
    """;
```

#### Where you'll use it in real work

```
✅ JSON test fixtures in unit tests
✅ Native SQL queries in @Query or JdbcTemplate
✅ HTML / XML templates
✅ Multi-line log or error messages
```

#### Interview-ready answer

> Text blocks, finalised in Java 15, are multi-line string literals delimited by triple quotes. They remove the need for `\n` escapes and string concatenation, and double quotes inside them need no escaping, which makes embedded JSON, SQL, HTML and XML far more readable. Java automatically strips the common leading indentation, and the position of the closing delimiter determines the left margin, so we can keep the code indented without that indentation ending up in the string. There are also two escapes specific to text blocks: a backslash at the end of a line joins it with the next, and `\s` preserves a trailing space. In practice I use them for native SQL queries and for JSON payloads in tests.

#### Easy memory

```
"""  →  multi-line String, no \n, no +, no escaped quotes ✅

String json = """
    { "name": "Priyanka" }
    """;

Indentation: the CLOSING """ sets the left margin ⭐
\  at line end → join lines
\s             → keep a trailing space

Use for: JSON in tests, native SQL, HTML
```

---

## Other Notable Features (Java 9–17)

### Q: What is `var` (local variable type inference, Java 10)?

The easiest way to remember:

```
var = "compiler, YOU work out the type."

Java is still statically typed — the type is fixed, just not typed out by hand.
```

```java
var list = new ArrayList<String>();     // inferred: ArrayList<String>
var count = 10;                         // inferred: int
var name = "Priyanka";                  // inferred: String
```

It shines when the type is long and repeated:

```java
// Before
Map<String, List<Employee>> byDept = new HashMap<String, List<Employee>>();

// After
var byDept = new HashMap<String, List<Employee>>();
```

#### The important trap ⭐ — `var` is NOT JavaScript's `var`

```java
var x = 10;
x = "hello";        // ❌ compile error — x is permanently an int
```

```
JavaScript var → the TYPE can change at runtime  (dynamic)
Java var       → the type is fixed at COMPILE time (static) ✅
```

#### Where it is NOT allowed

```java
var x;                          // ❌ no initialiser → nothing to infer
var y = null;                   // ❌ null has no type
private var field = 10;         // ❌ fields not allowed
void m(var param) { }           // ❌ parameters not allowed
var m() { return 10; }          // ❌ return types not allowed
```

```
✅ Local variables WITH an initialiser
✅ for / for-each loop variables
✅ try-with-resources
❌ fields, parameters, return types, no-initialiser, null
```

#### When to use it (readability rule)

```java
var employees = employeeService.findAll();   // ✅ clear from the name
var x = process();                            // ❌ what type is this?
```

Rule: use `var` when the right-hand side already makes the type obvious.

#### Interview-ready answer

> `var`, introduced in Java 10, is local variable type inference — the compiler infers the type from the initialiser. Java remains statically and strongly typed; the type is fixed at compile time and simply not written out, so it is completely different from JavaScript's `var`. It can only be used for local variables that have an initialiser, including loop variables and try-with-resources, and not for fields, method parameters, return types, or with `null`. I use it where the right-hand side already makes the type obvious, such as `var employees = service.findAll()`, and I avoid it where it hides useful information.

#### Easy memory

```
var = compiler infers the type → still STATIC, still fixed ✅
var x = 10;  then  x = "hi";  → ❌ compile error

✅ local variables with an initialiser, loops, try-with-resources
❌ fields, parameters, return types, var x; , var y = null;

Use it when the type is obvious from the right-hand side.
```

---

### Q: What are the Java 9 collection factory methods?

The easiest way to remember:

```
List.of() / Set.of() / Map.of() → create an IMMUTABLE collection in one line.
```

```java
List<String> list = List.of("a", "b", "c");
Set<Integer> set  = Set.of(1, 2, 3);
Map<String, Integer> map = Map.of("x", 1, "y", 2);

// more than 10 map entries:
Map<String, Integer> big = Map.ofEntries(
    Map.entry("a", 1),
    Map.entry("b", 2));
```

#### The old way

```java
List<String> list = new ArrayList<>();
list.add("a"); list.add("b"); list.add("c");
list = Collections.unmodifiableList(list);       // 4 lines 😩
```

#### The traps ⭐

```java
List<String> list = List.of("a", "b");

list.add("c");            // 💥 UnsupportedOperationException — truly immutable
List.of("a", null);       // 💥 NullPointerException — nulls are not allowed
Set.of(1, 1);             // 💥 IllegalArgumentException — duplicates not allowed
```

Also note:

```java
Arrays.asList("a","b")    → fixed SIZE, but set() works, nulls allowed
List.of("a","b")          → fully IMMUTABLE, no nulls ⭐
```

That difference is a favourite follow-up question.

#### Easy memory

```
List.of() Set.of() Map.of()  →  one-line IMMUTABLE collections ✅

add()      → UnsupportedOperationException 💥
null       → NullPointerException 💥
duplicates in Set.of() → IllegalArgumentException 💥

Arrays.asList → fixed size but MUTABLE elements | List.of → fully immutable
```

---

### Q: What Stream and Optional improvements came after Java 8?

```java
// STREAMS
Stream.takeWhile(p)      // Java 9 — take while true, then STOP
Stream.dropWhile(p)      // Java 9 — skip while true, then take the rest
Stream.ofNullable(x)     // Java 9 — empty stream if null
Stream.iterate(1, i -> i < 100, i -> i * 2)   // Java 9 — with a condition ⭐
stream.toList()          // Java 16 — shorter than collect(Collectors.toList())

// OPTIONAL
optional.ifPresentOrElse(v -> use(v), () -> handleEmpty());   // Java 9 ⭐
optional.or(() -> Optional.of(fallback));                     // Java 9
optional.stream()                                             // Java 9

// STRING
" hi ".strip()           // Java 11 — Unicode-aware trim()
"".isBlank()             // Java 11 — empty or only whitespace
"a\nb".lines()           // Java 11 — a Stream of lines
"ab".repeat(3)           // Java 11 — "ababab"

// COLLECTORS
Collectors.teeing(...)   // Java 12 — two collectors at once
```

#### takeWhile vs filter — the difference that gets asked ⭐

```java
List.of(1, 2, 3, 10, 4, 5).stream()
    .filter(n -> n < 5)      // [1, 2, 3, 4]  → checks EVERY element
    .toList();

List.of(1, 2, 3, 10, 4, 5).stream()
    .takeWhile(n -> n < 5)   // [1, 2, 3]     → STOPS at 10 ⭐
    .toList();
```

```
filter    → keeps every matching element, scans the whole stream
takeWhile → stops at the FIRST failure (needs sorted/ordered data)
```

#### Easy memory

```
Java 9  → takeWhile / dropWhile / ofNullable / ifPresentOrElse / or()
Java 11 → strip() isBlank() lines() repeat()
Java 16 → stream.toList()   ⭐ (shortest form)

filter = check ALL | takeWhile = STOP at the first failure
```

---

### Q: What are "helpful NullPointerExceptions" (Java 14)?

The easiest way to remember:

```
The JVM now names the EXACT variable that was null.
```

```java
employee.getAddress().getCity().toUpperCase();
```

**Before Java 14:**

```
Exception in thread "main" java.lang.NullPointerException
    at Main.main(Main.java:10)
```

Which one was null — the employee, the address, or the city? You had to guess or add print statements.

**Java 14+:**

```
java.lang.NullPointerException:
  Cannot invoke "String.toUpperCase()" because the return value of
  "Address.getCity()" is null                              ⭐
```

Now you know instantly: `getCity()` returned null.

Enabled by default since Java 15 (in 14 it needed `-XX:+ShowCodeDetailsInExceptionMessages`).

#### Easy memory

```
Java 14+ NPE tells you WHICH variable was null ⭐

"Cannot invoke String.toUpperCase() because the return value of
 Address.getCity() is null"

Saves hours of debugging chained calls.
```

---

### Q: What is the new HTTP Client (Java 11)?

The easiest way to remember:

```
A modern, built-in HTTP client — HTTP/2, async, WebSocket —
replacing the painful old HttpURLConnection.
```

```java
HttpClient client = HttpClient.newHttpClient();

HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.example.com/employees"))
        .header("Content-Type", "application/json")
        .GET()
        .build();

// synchronous
HttpResponse<String> response =
        client.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println(response.statusCode());
System.out.println(response.body());

// asynchronous ⭐
client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(HttpResponse::body)
      .thenAccept(System.out::println);
```

```
Old HttpURLConnection → 30+ lines, manual streams, HTTP/1.1 only 😩
New HttpClient        → builder style, HTTP/2, async, WebSocket ✅
```

Note: in Spring Boot projects you'd normally still use `RestTemplate` or, preferably, `WebClient` — but knowing the built-in client exists is a good point.

#### Easy memory

```
Java 11 HttpClient → HTTP/2 + async (CompletableFuture) + WebSocket
send()      → blocking
sendAsync() → non-blocking ⭐
Replaces HttpURLConnection.
```

---

### Q: What is the Java Module System / JPMS (Java 9)?

The easiest way to remember:

```
Modules = packages grouped together, declaring what they EXPORT
          and what they REQUIRE.
```

```java
// module-info.java
module com.myapp.service {
    requires com.myapp.data;          // what I need
    exports com.myapp.service.api;    // what others may use
}
```

```
Before: classpath → EVERY public class is visible to everyone
After : module    → only EXPORTED packages are visible ✅
```

Real-world idea: an **office building**.

```
Classpath → all doors unlocked, anyone walks into any room
Modules   → each department declares which rooms are open to visitors
```

Benefits: strong encapsulation, explicit dependencies, and `jlink` can build a small custom runtime containing only the modules you use (useful for containers).

Be honest in the interview: most business applications still run on the classpath. Knowing what it is and why it exists is enough.

#### Easy memory

```
module-info.java → requires (what I need) + exports (what I share)
Benefit: real encapsulation + smaller runtimes via jlink
Reality: most apps still use the classpath.
```

---

### Q: Can you run a single-file Java program (Java 11)?

```bash
# Before
javac Hello.java
java Hello

# Java 11+
java Hello.java        # ✅ compiles in memory and runs
```

Great for scripts, quick tests and learning — no `.class` file is produced.

---

## Likely Interview Scenarios

### Q: What Java 17 features would you use in a Spring Boot microservice?

Answer it feature by feature, tied to real code:

```
Records          → DTOs, API request/response objects, value objects
Text blocks      → native SQL in @Query, JSON payloads in tests
Switch expressions → status → action mapping, cleanly and exhaustively
Sealed + pattern matching → a closed set of domain events or payment types
var              → concise local variables inside service methods
List.of()/Map.of() → immutable constants and test fixtures
Optional.ifPresentOrElse → cleaner service-layer null handling
```

Concrete example:

```java
// DTO
public record EmployeeResponse(Long id, String name, String dept) {}

// Controller
@GetMapping("/{id}")
public EmployeeResponse get(@PathVariable Long id) {
    Employee e = service.findById(id);
    return new EmployeeResponse(e.getId(), e.getName(), e.getDept());
}

// Query with a text block
@Query(value = """
    SELECT * FROM employees
    WHERE dept = :dept AND salary > :min
    """, nativeQuery = true)
List<Employee> search(String dept, double min);

// Status mapping with a switch expression
String action = switch (order.status()) {
    case NEW        -> "Send confirmation email";
    case SHIPPED    -> "Send tracking link";
    case DELIVERED  -> "Request a review";
    case CANCELLED  -> "Process the refund";
};
```

---

### 🔵 In your RoboGebra code

```
robogebra-portal → Java 17, Spring Boot 3.2.0
   26 files use `public record`   (payment DTOs, exception payloads, service value objects)
   e.g. common/exception/QuizConflictException.java
        domain/payment/service/RazorPayService.java
   92 × @Valid with jakarta.validation-api 3.0.2   ← jakarta, NOT javax ⭐
```

> 🗣️ *"We're on Java 17 and Spring Boot 3.2, so records are used for DTOs and small value objects — the payment service uses them for its request and response payloads. What we deliberately don't do is make a persistence model a record, because the Mongo mapper, like Hibernate, needs a no-arg constructor and mutable fields."*

That single sentence answers **"do you use records?"** *and* **"where would you not use one?"** at the same time. → [39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)

---

### Q: How would you migrate a Java 8 project to Java 17?

Give it as a **sequence**, not a list of facts:

```
1. Update the JDK and set source/target (or `release`) to 17 in Maven/Gradle

2. Fix the REMOVED things ⭐
      - javax.*  →  jakarta.*   (if moving to Spring Boot 3)
      - Nashorn JavaScript engine — removed
      - SecurityManager — deprecated
      - some internal sun.misc.* APIs are now blocked (strong encapsulation)

3. Upgrade dependencies
      - Spring Boot 3.x requires Java 17
      - Lombok, Hibernate, Mockito all need version bumps

4. Run the FULL test suite; fix warnings

5. THEN modernise gradually
      - records for DTOs, switch expressions, text blocks
```

```
⭐ The key sentence: "Migrate first, modernise second."
   Get it compiling and green on 17 before rewriting any code style.
```

The most common real blocker is the `javax` → `jakarta` package rename, because it touches every entity, servlet and validation annotation.

---

### Honesty tip for the interview

If you have mostly used Java 8 or 11, say this — it is far stronger than pretending:

> *"I've worked extensively with Java 8 features — streams, lambdas, `Optional` and functional interfaces — and I've been upskilling on Java 17. I'm comfortable with records, sealed classes, pattern matching for `instanceof`, switch expressions and text blocks, and I understand where each one fits, for example records for DTOs but not for JPA entities."*

That answer shows both real experience and current knowledge.

---

## Quick Revision Sheet

```
LTS: 8 → 11 → 17 → 21     |  Spring Boot 3 REQUIRES Java 17

RECORD (16)     record Employee(Long id, String name) {}
                → final fields + ctor + name() + equals/hashCode/toString FREE
                → accessor is name(), not getName()
                → ✅ DTO  ❌ JPA entity

SEALED (17)     sealed interface Shape permits Circle, Square {}
                → child must be final | sealed | non-sealed
                → payoff: EXHAUSTIVE switch with no default

SWITCH EXPR (14) String s = switch (day) { case MON -> "Busy"; };
                → returns a value, no break, no fall-through, `yield` in blocks

PATTERN MATCH (16)  if (obj instanceof String s)   → check + cast in one

TEXT BLOCK (15) """ multi-line """ → closing """ sets the margin

var (10)        local variables only, needs an initialiser, still STATIC typing

Java 9   List.of() / Map.of()  (immutable, no nulls)
         takeWhile / dropWhile / ifPresentOrElse
Java 11  HttpClient, strip(), isBlank(), lines(), repeat(), java Hello.java
Java 14  helpful NullPointerExceptions (names the null variable) ⭐
Java 16  stream.toList()

MIGRATION: migrate first, modernise second. Biggest blocker = javax → jakarta.
```

---

## ➡️ Next: Java 21

> **"And what's new in Java 21?"** is now the standard follow-up to every Java 17 question. Java 21 is the **current LTS** most companies are migrating to.
>
> ```
> Java 17 = less BOILERPLATE ⭐   (records, sealed, pattern matching)
> Java 21 = CONCURRENCY ⭐⭐      (VIRTUAL THREADS)
>           + pattern matching for switch and record patterns FINALISED
>           + sequenced collections
> ```
>
> **→ [40 — Java 21 Features](./40-java21-features.md)** ⭐

---

## Related files

- [05-java.md](05-java.md) — Core Java (OOP, collections, streams, threads)
- [FLASHCARDS-12-java17.md](FLASHCARDS-12-java17.md) — quick recall cards for this file
- [06-spring-boot.md](06-spring-boot.md) — Spring Boot (uses records and text blocks heavily)
- [22-java-streams-coding-problems.md](22-java-streams-coding-problems.md) — stream coding problems
