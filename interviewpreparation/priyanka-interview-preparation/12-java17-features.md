# Java 17 — Version-Specific Features (Interview Q&A)

> **Berribot target stack lists Java 17.** Your resume says "Java" — be ready for "what's new in Java 17 / since Java 8" questions. This file covers the headline features from Java 9 → 17, focused on what interviewers actually ask, with code.

---

## Java 17 Basics

### Q: Why is Java 17 important?
Java 17 is an **LTS (Long-Term Support)** release (Sept 2021), like Java 8, 11, and 21. LTS versions get extended support, so companies standardize on them. Java 17 bundles years of improvements (records, sealed classes, pattern matching, switch expressions, text blocks) into a stable, supported release.

### Q: What is an LTS release?
A **Long-Term Support** version that receives security patches and updates for many years, making it safe for production. Non-LTS releases (e.g., 12–16, 18–20) are short-lived feature previews. Enterprises usually run **8 → 11 → 17 → 21**.

### Q: How is Java 17 different from Java 8 (the big talking point)?
| Java 8 | Java 17 |
|--------|---------|
| Lambdas, Streams, Optional introduced | Plus records, sealed classes, pattern matching |
| `switch` statements only | `switch` **expressions** with arrow syntax |
| String concatenation | **Text blocks** for multi-line strings |
| Verbose POJOs | **Records** remove boilerplate |
| `instanceof` + cast | **Pattern matching** for `instanceof` |
| Manual modularity | **Module system (JPMS)** since Java 9 |

---

## Records (Java 16)

### Q: What is a record?
An immutable data-carrier class. The compiler auto-generates the constructor, `getters` (accessor methods), `equals()`, `hashCode()`, and `toString()`. Perfect for **DTOs**.
```java
public record Employee(Long id, String name, String dept) {}

// Usage
Employee e = new Employee(1L, "Priyanka", "Engineering");
e.name();        // accessor (no "get" prefix)
e.toString();    // Employee[id=1, name=Priyanka, dept=Engineering]
```

### Q: What are the rules/limits of records?
- Fields are **final** (immutable); record is implicitly `final`.
- Can add static fields, methods, and a **compact constructor** for validation:
```java
public record Employee(Long id, String name) {
    public Employee {                 // compact constructor
        if (name == null) throw new IllegalArgumentException("name required");
    }
}
```
- Cannot extend another class (but can implement interfaces).

### Q: Record vs Lombok `@Data`?
Records are a **native language feature** (no library) and are immutable by design. Lombok is a third-party annotation processor that generates getters/setters for mutable classes. For immutable DTOs, records are cleaner; for JPA entities (which need a no-arg constructor and mutability), you still use regular classes/Lombok.

---

## Sealed Classes (Java 17)

### Q: What are sealed classes?
They restrict **which classes can extend or implement** a class/interface — giving you a controlled, known hierarchy.
```java
public sealed interface Shape permits Circle, Square, Rectangle {}

public final class Circle implements Shape { }
public final class Square implements Shape { }
public non-sealed class Rectangle implements Shape { }  // reopened for extension
```
Each permitted subclass must be `final`, `sealed`, or `non-sealed`. Great with pattern-matching `switch` — the compiler knows all cases.

---

## Switch Expressions (Java 14)

### Q: What are switch expressions?
`switch` can now **return a value** using arrow syntax — no fall-through, no `break`, more concise.
```java
String size = switch (day) {
    case MONDAY, FRIDAY, SUNDAY -> "Busy";
    case TUESDAY               -> "Light";
    default                    -> {
        yield "Normal";       // yield returns a value from a block
    }
};
```
Benefits: no accidental fall-through, exhaustive checks, returns a value directly.

---

## Pattern Matching for instanceof (Java 16)

### Q: What is pattern matching for `instanceof`?
Combines the type check and cast into one step, removing boilerplate.
```java
// Old
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.length());
}
// Java 16+
if (obj instanceof String s) {
    System.out.println(s.length());   // s is already cast
}
```

### Q: What is pattern matching for switch? (preview in 17, final in 21)
Match on type directly in a switch — powerful with sealed classes:
```java
String describe(Shape shape) {
    return switch (shape) {
        case Circle c    -> "Circle r=" + c.radius();
        case Square s    -> "Square side=" + s.side();
        case Rectangle r -> "Rectangle";
    };  // exhaustive — no default needed with sealed types
}
```

---

## Text Blocks (Java 15)

### Q: What are text blocks?
Multi-line string literals using `"""`, ideal for JSON, SQL, HTML — no `\n` or `+` concatenation.
```java
String json = """
    {
        "name": "Priyanka",
        "role": "Full Stack Developer"
    }
    """;

String sql = """
    SELECT id, name FROM employees
    WHERE dept = 'Engineering'
    """;
```

---

## Other Notable Features (Java 9–17)

### Q: What is `var` (local variable type inference, Java 10)?
Lets the compiler infer the local variable type. Only for **local** variables with an initializer — improves readability, not dynamic typing.
```java
var list = new ArrayList<String>();   // inferred ArrayList<String>
var count = 10;                       // int
```

### Q: What is the Java Module System / JPMS (Java 9)?
A way to group packages into **modules** with explicit dependencies and exported APIs via `module-info.java`. Improves encapsulation and lets you build smaller runtimes (`jlink`). Most apps still use the classpath, but it's good to know.

### Q: What collection factory methods were added (Java 9)?
Immutable collections in one line:
```java
List<String> list = List.of("a", "b", "c");
Map<String, Integer> map = Map.of("x", 1, "y", 2);
Set<Integer> set = Set.of(1, 2, 3);
```

### Q: What Stream/Optional improvements came after Java 8?
- `Stream.takeWhile()` / `dropWhile()` (Java 9), `Stream.ofNullable()`, `iterate` with predicate.
- `Optional.ifPresentOrElse()`, `Optional.or()`, `Optional.stream()` (Java 9).
- `Collectors.teeing()` (Java 12), `String` methods `strip()`, `isBlank()`, `lines()`, `repeat()` (Java 11/12).

### Q: What are "helpful NullPointerExceptions" (Java 14)?
The JVM now tells you **exactly which variable was null** in an NPE message (e.g., `Cannot invoke "String.length()" because "name" is null`), making debugging far easier.

### Q: What is the new HTTP Client (Java 11)?
A modern, built-in `java.net.http.HttpClient` supporting HTTP/2, async (`CompletableFuture`), and WebSockets — replacing the old `HttpURLConnection`.
```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.example.com")).build();
HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
```

### Q: Can you run a single-file Java program now (Java 11)?
Yes — `java Hello.java` runs a source file directly without a separate `javac` step (great for scripts/learning).

---

## Likely Berribot Q&A

### Q: What Java 17 features would you use in a Spring Boot microservice?
- **Records** for DTOs / API request-response objects and immutable value objects.
- **Text blocks** for inline JSON test data or native SQL queries.
- **Switch expressions** for clean mapping logic (status → action).
- **Sealed classes + pattern matching** for modeling a closed set of domain events.
- **`var`** for concise local variables in service methods.

### Q: How would you migrate a Java 8 project to Java 17?
1. Update the JDK and build tool (Maven/Gradle) `source/target` to 17.
2. Check for **removed APIs** (e.g., some `javax` → `jakarta` namespace changes in newer Spring Boot 3.x, deprecated `Nashorn` JS engine, security manager).
3. Update dependencies (Spring Boot 3.x requires Java 17).
4. Run the full test suite; fix compilation warnings.
5. Gradually adopt new features (records for DTOs, etc.) — migration first, modernization second.

> **Honesty tip:** If you've mostly used Java 8/11 features, say: *"I've worked extensively with Java 8 features — Streams, lambdas, Optional, functional interfaces. I've been upskilling on Java 17 — records, sealed classes, pattern matching, and switch expressions — and I'm comfortable using them."*
