# 🃏 Java 17 — Rapid-Recall Flashcards

> Cover the **A:** line, say your answer out loud, then check. Full detail in [12-java17-features.md](./12-java17-features.md).
> ⚡ = high-yield for Berribot. Target: name 5 Java 17 features + where you'd use them, cold.

---

**Q1 ⚡ Why is Java 17 important?**
A: It's an **LTS** release (Sept 2021), like 8/11/21. Bundles records, sealed classes, pattern matching, switch expressions, text blocks into a stable, supported version companies standardize on.

**Q2 What is an LTS release?**
A: Long-Term Support — years of security patches/updates, safe for production. Enterprises run 8 → 11 → 17 → 21. Non-LTS (12–16, 18–20) are short-lived preview releases.

**Q3 ⚡ Java 8 vs Java 17 headline differences?**
A: 8 = lambdas/streams/Optional, switch statements, verbose POJOs, instanceof+cast. 17 adds **records, sealed classes, pattern matching, switch expressions, text blocks** (+ JPMS from 9).

**Q4 ⚡ What is a record?**
A: Immutable data-carrier class. Compiler auto-generates constructor, accessors (`e.name()` — no "get"), `equals/hashCode/toString`. Ideal for **DTOs**. `public record Employee(Long id, String name){}`.

**Q5 Record rules/limits?**
A: Fields final; record implicitly final; can't extend a class (can implement interfaces). Supports static members + a **compact constructor** for validation.

**Q6 Record vs Lombok @Data?**
A: Record = native, immutable by design, no library. Lombok = 3rd-party processor for mutable classes. JPA entities need no-arg ctor + mutability → still regular class/Lombok.

**Q7 ⚡ What are sealed classes?**
A: Restrict **which classes may extend/implement** via `permits` → controlled, known hierarchy. Each permitted subclass must be `final`, `sealed`, or `non-sealed`. Pairs with pattern-matching switch (compiler knows all cases).

**Q8 ⚡ What are switch expressions?**
A: `switch` returns a value via arrow syntax — no fall-through, no `break`; use `yield` to return from a block. Exhaustive checks. `case MONDAY, FRIDAY -> "Busy";`.

**Q9 ⚡ Pattern matching for instanceof?**
A: Combines type check + cast: `if (obj instanceof String s) { s.length(); }` — `s` already cast, no boilerplate.

**Q10 Pattern matching for switch?**
A: Match on type in a switch (preview 17, final 21): `case Circle c -> ...`. With sealed types it's exhaustive → no `default` needed.

**Q11 ⚡ What are text blocks?**
A: Multi-line string literals with `"""` — no `\n`/`+`. Great for JSON, SQL, HTML.

**Q12 What is `var` (Java 10)?**
A: Local variable type inference — compiler infers type. Only for **locals with an initializer**. Readability, not dynamic typing. `var list = new ArrayList<String>();`.

**Q13 What is JPMS (Java 9)?**
A: Module system — group packages into modules with explicit dependencies/exported APIs via `module-info.java`. Better encapsulation + smaller runtimes (`jlink`). Most apps still use the classpath.

**Q14 Collection factory methods (Java 9)?**
A: Immutable one-liners: `List.of(...)`, `Map.of(...)`, `Set.of(...)`.

**Q15 Stream/Optional improvements after 8?**
A: `Stream.takeWhile/dropWhile/ofNullable`; `Optional.ifPresentOrElse/or/stream`; `Collectors.teeing`; String `strip/isBlank/lines/repeat`.

**Q16 Helpful NullPointerExceptions (Java 14)?**
A: NPE message names the exact null variable — e.g. `Cannot invoke "String.length()" because "name" is null`.

**Q17 New HTTP Client (Java 11)?**
A: Built-in `java.net.http.HttpClient` — HTTP/2, async via CompletableFuture, WebSockets. Replaces old HttpURLConnection.

**Q18 Single-file Java program (Java 11)?**
A: `java Hello.java` runs source directly — no separate `javac`. Good for scripts/learning.

**Q19 ⚡ Which Java 17 features in a Spring Boot microservice?**
A: **Records** for DTOs/request-response · **text blocks** for inline JSON/SQL · **switch expressions** for status→action mapping · **sealed + pattern matching** for closed domain-event sets · **`var`** for concise locals.

**Q20 ⚡ Migrate Java 8 → 17?**
A: 1) Bump JDK + build tool source/target to 17. 2) Check removed APIs (`javax`→`jakarta` in Spring Boot 3.x, Nashorn, SecurityManager). 3) Update deps (Spring Boot 3.x needs 17). 4) Run full test suite. 5) Adopt new features gradually — migrate first, modernize second.

**Q21 ⭐ Honesty framing line:**
A: "I've worked extensively with Java 8 — streams, lambdas, Optional, functional interfaces. I've been upskilling on Java 17 — records, sealed classes, pattern matching, switch expressions — and I'm comfortable using them."
