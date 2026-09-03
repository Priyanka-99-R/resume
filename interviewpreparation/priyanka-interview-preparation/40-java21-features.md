# ☕ Java 21 — Version-Specific Features (Easy Version)

> **Why this file exists:** Java 21 is the **current LTS most companies are migrating to**, and *"what's new in 21?"* is now a standard follow-up to any Java 17 question. **Virtual threads** in particular come up constantly.
>
> 🔵 **Your honest position:** RoboGebra is on **Java 17 / Spring Boot 3.2**. So this is *"studied, not shipped"* — and the answer that scores is **what you'd use it for and what the migration would cost**, not a claim you've run it in production. There's a ready-made script for that at the end.

Every topic follows the same shape — **Easiest way to remember → old way vs new way → real-world example → diagram → interview trap → Interview-ready answer → Easy memory box.**

---

## Contents

1. [The LTS timeline — where 21 sits](#the-lts-timeline--where-21-sits)
2. [Virtual Threads ⭐⭐ the headline](#1-virtual-threads--the-headline-feature)
3. [Pattern Matching for `switch` ⭐](#2-pattern-matching-for-switch-final-in-21)
4. [Record Patterns ⭐](#3-record-patterns-final-in-21)
5. [Sequenced Collections ⭐](#4-sequenced-collections--the-one-nobody-knows)
6. [Structured Concurrency & Scoped Values (preview)](#5-structured-concurrency--scoped-values-preview)
7. [Generational ZGC](#6-generational-zgc)
8. [What came after 21](#what-came-after-21)
9. [Java 17 vs 21 — the answer table](#java-17-vs-21--the-comparison-they-want)
10. [Your answer scripts](#your-answer-scripts)
11. [Quick Revision Sheet](#quick-revision-sheet)

---

## The LTS timeline — where 21 sits

```
   8 ────── 11 ────── 17 ────── 21 ────── 25          ← LTS ⭐
2014       2018      2021      2023      2025

⭐ Java 21 (Sept 2023) is the LTS most enterprises are on or moving to.
   Java 25 (Sept 2025) is the newest LTS.
```

```
Spring Boot 3.x  → requires Java 17 minimum, runs happily on 21 ⭐
Virtual threads  → Spring Boot 3.2 supports them with ONE property ⭐
```

⭐ **The line to say:** *"17 was the boilerplate release — records, sealed types, pattern matching. **21 is the concurrency release** — virtual threads are the biggest change to how Java handles I/O since NIO."*

#### Easy memory

```
LTS: 8 → 11 → 17 → 21 ⭐ → 25
Java 17 = less BOILERPLATE ⭐ | Java 21 = CONCURRENCY ⭐
Spring Boot 3.2 + Java 21 → virtual threads with one property ⭐
```

---

## 1. Virtual Threads — the headline feature ⭐⭐

> Deep dive with the thread-pool context: **[32 — Multithreading, Part 16](./32-multithreading.md)**.

The easiest way to remember:

```
A PLATFORM thread  = 1 Java thread : 1 OS thread   → ~1 MB → thousands max
A VIRTUAL thread   = scheduled by the JVM          → ~hundreds of bytes → MILLIONS ⭐

And when it BLOCKS on I/O it UNMOUNTS from its carrier thread
instead of holding an OS thread hostage. ⭐⭐
```

### The problem it solves

```
THREAD-PER-REQUEST with platform threads:
   10,000 concurrent requests → 10,000 OS threads → ~10 GB → dead 💥
   So Tomcat caps at ~200 threads, and request 201 QUEUES 🐢

The "solution" before 21 was REACTIVE (WebFlux):
   ✅ scales beautifully
   ❌ but your code becomes Mono/Flux chains, stack traces become unreadable,
      and one blocking JDBC call ruins the whole thing 💥
```

```
Java 21's answer: keep the SIMPLE blocking code, make the threads cheap ⭐⭐
```

### The code

```java
// one virtual thread, directly
Thread.startVirtualThread(() -> doWork());

// a virtual thread PER TASK — the normal way ⭐
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (var task : tasks) executor.submit(task);
}   // close() waits for all of them — try-with-resources ⭐

// Spring Boot 3.2 — literally one property ⭐⭐
// application.properties
spring.threads.virtual.enabled=true
```

```
PLATFORM THREADS                    VIRTUAL THREADS
   req1 → thread1 → 😴 waiting        req1 ─┐
   req2 → thread2 → 😴 waiting        req2 ─┼→ a few CARRIER threads ⚡
   ...                                req3 ─┘   (each UNMOUNTS while waiting)
   req201 → no thread left 💥         req10000 → fine ✅
```

Real-world idea: **a restaurant.**

```
PLATFORM → each waiter stands at ONE table until the meal ends 😴
           20 waiters = 20 tables, maximum.

VIRTUAL  → a waiter takes the order, walks away, and comes back when
           the food is ready ⚡ — 20 waiters serve 200 tables ⭐
```

### ⚠️ The three caveats — say these, they're what separate real knowledge

```
1. DON'T POOL virtual threads ⭐⭐
      They're cheap to CREATE — pooling defeats the entire purpose.
      newVirtualThreadPerTaskExecutor(), never newFixedThreadPool.

2. They do NOT speed up CPU-BOUND work ⭐
      The gain is from UNMOUNTING while blocked on I/O.
      CPU work still needs a core. Use platform threads / parallel streams there.

3. PINNING ⭐
      In Java 21, a virtual thread inside a `synchronized` block could be
      PINNED to its carrier — it couldn't unmount, so the benefit was lost.
      → ReentrantLock was preferred in hot paths.
      (Later JDKs improved this, but on 21 it's the correct caveat to state.)
```

⭐ Also worth knowing: **thread-local variables become expensive** with millions of threads — which is exactly why **Scoped Values** were introduced alongside them.

### Interview-ready answer

> Virtual threads, finalised in Java 21, are lightweight threads scheduled by the JVM rather than the operating system. A platform thread maps one-to-one onto an OS thread and costs around a megabyte of stack, so a server caps out in the low thousands; a virtual thread costs a few hundred bytes, so millions are viable. The key behaviour is that when a virtual thread blocks on I/O it unmounts from its carrier thread instead of holding an OS thread, which makes the simple thread-per-request model scale like reactive code while keeping ordinary blocking code that's readable and debuggable. In Spring Boot 3.2 it's a single property. The caveats are that you should never pool them, they don't help CPU-bound work, and on 21 a `synchronized` block could pin a virtual thread to its carrier, so `ReentrantLock` was preferred in hot paths.

#### Easy memory

```
Virtual thread = JVM-scheduled, ~hundreds of bytes, MILLIONS possible ⭐
UNMOUNTS on blocking I/O ⭐⭐ (a waiter who walks away 🍽️)

Executors.newVirtualThreadPerTaskExecutor() | spring.threads.virtual.enabled=true ⭐

⚠️ DON'T POOL them ⭐ | no help for CPU-bound ⭐ | synchronized can PIN ⭐
Blocking code that scales like reactive — without the Mono/Flux ⭐
```

---

## 2. Pattern Matching for `switch` (final in 21) ⭐

The easiest way to remember:

```
switch can now match on TYPE, not just on a value. ⭐
```

```java
// BEFORE — the if/else instanceof ladder 😩
static String describe(Object o) {
    if (o instanceof Integer i)      return "int " + i;
    else if (o instanceof String s)  return "string of length " + s.length();
    else if (o == null)              return "null";
    else                             return "something else";
}

// JAVA 21 ⭐
static String describe(Object o) {
    return switch (o) {
        case Integer i      -> "int " + i;
        case String s       -> "string of length " + s.length();
        case null           -> "null";               // ⭐ null is a CASE now
        default             -> "something else";
    };
}
```

### Three things that are new

```
1. TYPE PATTERNS       case Integer i ->            ⭐
2. `case null`         a switch no longer throws NPE on null ⭐
3. GUARDS with `when`  case Integer i when i > 100 -> ⭐
```

```java
return switch (shape) {
    case Circle c when c.radius() > 10 -> "big circle";   // ⭐ a guarded pattern
    case Circle c                      -> "small circle";
    case Square s                      -> "square";
};
```

### ⭐ The killer combination — sealed + switch = compiler-checked exhaustiveness

```java
sealed interface Shape permits Circle, Square, Triangle { }

String area(Shape s) {
    return switch (s) {
        case Circle c   -> "πr²";
        case Square sq  -> "side²";
        case Triangle t -> "½bh";
    };                    // ⭐ NO default needed — the compiler knows the full list
}
```

```
Add a fourth shape to the `permits` list
      ↓
EVERY switch that doesn't handle it FAILS TO COMPILE ⭐⭐
      ↓
You find it at BUILD time, not at 2 a.m. in production
```

⚠️ **The `default` trap:** if you add a `default` branch, you *lose* that exhaustiveness check — the new type silently falls into `default`. **For a sealed type, omit `default` on purpose.**

Real-world idea: a **postal sorting machine** that knows every valid destination. Add a new city to the list and it refuses to run until someone tells it which bin that city goes in.

#### Easy memory

```
switch on TYPE ⭐ | case null ⭐ | `when` guards ⭐

SEALED + switch = compiler-checked EXHAUSTIVENESS ⭐⭐
   → a new subtype BREAKS THE BUILD, not production

⚠️ Adding `default` DESTROYS the exhaustiveness check 💥 — omit it deliberately
Versions: instanceof pattern = 16 · switch patterns + record patterns = 21 ⭐
```

---

## 3. Record Patterns (final in 21) ⭐

The easiest way to remember:

```
Record patterns DESTRUCTURE a record right in the pattern —
you get the components as variables, without calling the accessors. ⭐
```

```java
record Point(int x, int y) { }
record Line(Point start, Point end) { }
```

```java
// BEFORE
if (o instanceof Point p) {
    int x = p.x();          // manual extraction
    int y = p.y();
    System.out.println(x + y);
}

// JAVA 21 ⭐
if (o instanceof Point(int x, int y)) {      // ⭐ destructured
    System.out.println(x + y);
}
```

### It NESTS — which is where it gets powerful ⭐

```java
// pull all four numbers out of a nested structure, in one pattern ⭐
if (o instanceof Line(Point(var x1, var y1), Point(var x2, var y2))) {
    double length = Math.hypot(x2 - x1, y2 - y1);
}
```

And combined with `switch`:

```java
String describe(Object o) {
    return switch (o) {
        case Point(int x, int y) when x == y -> "on the diagonal";   // ⭐ pattern + guard
        case Point(int x, int y)             -> "point " + x + "," + y;
        case Line(Point s, Point e)          -> "line from " + s + " to " + e;
        default                              -> "unknown";
    };
}
```

Real-world idea: **unpacking a parcel in one move.** Instead of opening the box, taking out the inner box, and then taking out the item, you state the shape you expect and everything lands in your hand already labelled.

⭐ The three features are designed to work **together**:

```
RECORDS (16)  →  the data shape
SEALED  (17)  →  the closed set of shapes
PATTERNS (21) →  matching on them EXHAUSTIVELY ⭐

That trio is Java's answer to what other languages call
ALGEBRAIC DATA TYPES — say that phrase and it lands ⭐
```

#### Easy memory

```
if (o instanceof Point(int x, int y))  ⭐ — destructure in the pattern
NESTS: Line(Point(var x1, var y1), Point(var x2, var y2)) ⭐
Works inside switch, with `when` guards ⭐

records + sealed + patterns = ALGEBRAIC DATA TYPES ⭐ (say this)
```

---

## 4. Sequenced Collections — the one nobody knows ⭐

The easiest way to remember:

```
Before 21, "get the first element" was a DIFFERENT method for every collection.
Java 21 gave them all ONE interface. ⭐
```

### The problem — genuinely embarrassing 😬

```java
list.get(0);                              // List
deque.getFirst();                         // Deque
sortedSet.first();                        // SortedSet
linkedHashSet.iterator().next();          // LinkedHashSet — no method at all! 😩

// and the LAST element:
list.get(list.size() - 1);                // List
deque.getLast();                          // Deque
sortedSet.last();                         // SortedSet
// LinkedHashSet — you had to iterate the WHOLE thing 💥
```

```
Four collections that all have a clear order… and four different APIs ⭐
```

### The Java 21 fix

```java
interface SequencedCollection<E> extends Collection<E> {
    void addFirst(E e);      void addLast(E e);
    E getFirst();            E getLast();          // ⭐ the same everywhere
    E removeFirst();         E removeLast();
    SequencedCollection<E> reversed();             // ⭐ a REVERSED VIEW
}
```

```java
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
list.getFirst();        // "a"  ⭐ works on List now
list.getLast();         // "c"  ⭐
list.reversed();        // [c, b, a] — a VIEW, not a copy ⭐

LinkedHashSet<String> set = new LinkedHashSet<>(List.of("x", "y"));
set.getFirst();         // "x"  ⭐ finally

LinkedHashMap<String,Integer> map = new LinkedHashMap<>();
map.firstEntry();  map.lastEntry();  map.reversed();     // ⭐ SequencedMap
```

```
New interfaces:
   SequencedCollection ⭐  → List, Deque, LinkedHashSet, SortedSet
   SequencedSet
   SequencedMap ⭐         → LinkedHashMap, SortedMap
```

⭐ **`reversed()` returns a VIEW, not a copy** — so it's O(1) and changes write through. Say that; it's the detail that shows you read the API rather than the headline.

Real-world idea: **four remote controls that all had a different "power" button.** Java 21 didn't add a new feature — it put the button in the same place on all of them.

⚠️ Why it matters practically:

```
LinkedHashSet is exactly what you use for "unique, in insertion order" ⭐
   → before 21, getting its last element meant iterating the whole set 💥
```

#### Easy memory

```
SequencedCollection ⭐ → getFirst() getLast() addFirst() addLast() reversed()
SequencedMap ⭐        → firstEntry() lastEntry() reversed()

The pain it removes: list.get(0) vs deque.getFirst() vs sortedSet.first() 😩
reversed() is a VIEW, not a copy ⭐ (O(1), writes through)
Biggest win: LinkedHashSet finally has getFirst/getLast ⭐
```

---

## 5. Structured Concurrency & Scoped Values (preview)

⚠️ **Both were PREVIEW in Java 21** — mention them as "the direction of travel", not as something you'd ship.

### Structured Concurrency

```
The idea: if a task splits into subtasks, they should live and DIE together —
like a block scope, but for threads. ⭐
```

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Supplier<User>  user  = scope.fork(() -> fetchUser(id));      // ⭐ subtasks
    Supplier<Order> order = scope.fork(() -> fetchOrder(id));

    scope.join();                 // wait for both
    scope.throwIfFailed();        // ⭐ if EITHER failed, the other is CANCELLED

    return new Dashboard(user.get(), order.get());
}   // nothing outlives the block ⭐
```

```
The problem it fixes ⭐
   With an ExecutorService, if one of two parallel calls fails, the other
   keeps running — wasting work and leaking a thread. Nothing ties them
   together, and the stack trace doesn't show the relationship 💥

Structured concurrency makes the RELATIONSHIP explicit — one fails,
the sibling is cancelled, and the scope can't be left open ⭐
```

Real-world idea: **a group booking.** If the flight can't be confirmed, you don't want the hotel quietly booking itself anyway.

### Scoped Values

```
A safer, cheaper replacement for ThreadLocal ⭐ — immutable, and
automatically bounded to a scope instead of leaking in a pooled thread.
```

```java
private static final ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();

ScopedValue.where(CURRENT_USER, user).run(() -> handleRequest());
// inside: CURRENT_USER.get()  ⭐ — and it's gone when the block ends
```

⭐ **Why it exists:** with **millions of virtual threads**, a `ThreadLocal` per thread is expensive and easy to leak — you must `remove()` it in a `finally`. Scoped values are immutable and self-clearing.

#### Easy memory

```
STRUCTURED CONCURRENCY (preview) ⭐ — subtasks live and DIE together
   StructuredTaskScope.ShutdownOnFailure → one fails, the sibling is CANCELLED
   (a group booking 🧳 — no hotel without the flight)

SCOPED VALUES (preview) ⭐ — an immutable, self-clearing ThreadLocal
   Exists BECAUSE virtual threads make ThreadLocal expensive and leaky ⭐

⚠️ Both were PREVIEW in 21 — say "the direction of travel", not "we use it"
```

---

## 6. Generational ZGC

```
ZGC = the ultra-low-pause collector (sub-millisecond, even on huge heaps).
Java 21 made it GENERATIONAL — so it also exploits "most objects die young". ⭐
```

```
Before: ZGC scanned the whole heap every cycle → low pause, high CPU cost
After:  young objects are collected separately ⭐ → same tiny pauses,
        much less CPU and memory overhead
```

```bash
-XX:+UseZGC -XX:+ZGenerational
```

⭐ Enough to say: *"G1 is still the default; ZGC is for latency-critical services with large heaps, and Java 21 made it generational, which removed most of its overhead."*

#### Easy memory

```
G1 = the DEFAULT ⭐ | ZGC = sub-millisecond pauses, big heaps
Java 21 → GENERATIONAL ZGC ⭐ (young/old split → far less overhead)
```

---

## What came after 21

⚠️ Be careful and brief here — knowing the *shape* is enough, and over-claiming is risky.

```
Java 22 (Mar 2024)  unnamed variables & patterns FINAL (`_`)
                    Foreign Function & Memory API final
Java 23 (Sep 2024)  ⭐ STRING TEMPLATES WITHDRAWN — previewed in 21/22,
                    then REMOVED for redesign
Java 24 (Mar 2025)  further preview work
Java 25 (Sep 2025)  ⭐ the next LTS after 21
```

⭐ **The String Templates story is a genuinely good thing to mention:**

```java
// this was PREVIEW in 21 — and it is NOT in the language today ⚠️
String s = STR."Hello \{name}";
```

> *"String templates were previewed in 21 but withdrawn in 23 for redesign — so I wouldn't build on them. It's a good example of why I check whether a feature is preview or final before adopting it."*

⭐ That sentence shows you **verify** rather than repeat blog posts — and it protects you from confidently describing a feature that no longer exists.

#### Easy memory

```
22 → unnamed variables `_` final | 23 → STRING TEMPLATES WITHDRAWN ⭐ | 25 → next LTS
"I check preview vs final before adopting" ⭐
```

---

## Java 17 vs 21 — the comparison they want

| | **Java 17** (2021 LTS) | **Java 21** (2023 LTS) |
|---|---|---|
| Theme | less **boilerplate** ⭐ | **concurrency** ⭐ |
| Records | ✅ (16) | ✅ |
| Sealed classes | ✅ (17) | ✅ |
| Pattern matching `instanceof` | ✅ (16) | ✅ |
| Pattern matching `switch` | preview | ✅ **final** ⭐ |
| Record patterns | ❌ | ✅ **final** ⭐ |
| **Virtual threads** | ❌ | ✅ **final** ⭐⭐ |
| **Sequenced collections** | ❌ | ✅ ⭐ |
| Structured concurrency | ❌ | preview |
| Scoped values | ❌ | preview |
| Generational ZGC | ❌ | ✅ |

### The one-sentence answer ⭐

> *"Java 17 was the boilerplate release — records, sealed types, pattern matching for `instanceof`. **Java 21 is the concurrency release**: virtual threads make thread-per-request cheap again, and it finalised pattern matching for `switch` and record patterns, which together with records and sealed types give you exhaustive, compiler-checked domain modelling. The small one people forget is sequenced collections — `getFirst()` and `getLast()` finally work the same way everywhere."*

---

## Your Answer Scripts

### 🗣️ "Are you on Java 21?" — the honest answer ⭐

> *"Our backend is Java 17 on Spring Boot 3.2, so 21 is something I've studied rather than shipped. What I'd move for is **virtual threads** — we have a push-notification fan-out where each device is a blocking FCM call, and I currently manage that with a dedicated bounded thread pool. On 21 that's a virtual-thread executor and the pool tuning largely goes away. Spring Boot 3.2 already supports it with a single property, so the migration cost is low — the real work is auditing for `synchronized` blocks in hot paths, because on 21 those can pin a virtual thread to its carrier."*

⭐ **That answer is strong** because it names a *real* problem in your own code that Java 21 would solve, and it names the migration risk.

### 🗣️ "What would you use virtual threads for?"

> *"I/O-bound, thread-per-request work — REST endpoints that call other services or a database, and fan-out work like notifications. Not CPU-bound work, because unmounting only helps while you're blocked. And I wouldn't pool them; the whole point is that creating one is cheap."*

### 🗣️ "Should we migrate from 17 to 21?"

> *"The upgrade itself is usually cheap — 21 is largely source-compatible with 17 and Spring Boot 3 already supports both. I'd frame it by what you get: virtual threads if you're I/O-bound and hitting thread-pool limits, and pattern matching plus record patterns if you have domain modelling that's currently a chain of `instanceof`. If neither of those is a real pain, 17 is still supported and there's no urgency — I'd rather move for a reason than for the version number."*

⭐ **That is a technical-lead answer** — it treats a version bump as a cost/benefit decision, not a default.

---

## Quick Revision Sheet

```
LTS: 8 → 11 → 17 → 21 ⭐ → 25
Java 17 = less BOILERPLATE | Java 21 = CONCURRENCY ⭐

VIRTUAL THREADS ⭐⭐ (the headline)
  JVM-scheduled, ~hundreds of bytes, MILLIONS possible
  UNMOUNT on blocking I/O ⭐ → thread-per-request is cheap again
  Executors.newVirtualThreadPerTaskExecutor()
  Spring Boot 3.2: spring.threads.virtual.enabled=true ⭐
  ⚠️ DON'T POOL ⭐ | no CPU-bound gain ⭐ | synchronized can PIN ⭐

PATTERN MATCHING for switch ⭐ (final in 21)
  case Integer i -> · case null -> · case X x when guard ->
  SEALED + switch = compiler-checked EXHAUSTIVENESS ⭐⭐
  ⚠️ adding `default` DESTROYS that check 💥

RECORD PATTERNS ⭐ (final in 21)
  if (o instanceof Point(int x, int y))  — and it NESTS
  records + sealed + patterns = ALGEBRAIC DATA TYPES ⭐

SEQUENCED COLLECTIONS ⭐ (the one nobody knows)
  getFirst() getLast() addFirst() addLast() reversed()
  SequencedCollection · SequencedSet · SequencedMap
  reversed() is a VIEW, not a copy ⭐
  Biggest win: LinkedHashSet finally has getFirst/getLast ⭐

PREVIEW in 21
  Structured concurrency ⭐ — subtasks live and DIE together 🧳
  Scoped values ⭐ — immutable ThreadLocal replacement (needed BECAUSE of
                     millions of virtual threads)

ALSO  Generational ZGC ⭐ (G1 is still the default)
AFTER 22 unnamed `_` final · 23 STRING TEMPLATES WITHDRAWN ⭐ · 25 next LTS

🔵 YOUR POSITION ⭐
  "We're on Java 17 / Spring Boot 3.2 — 21 is studied, not shipped.
   What I'd move for is virtual threads: our push fan-out is blocking FCM
   calls managed with a bounded pool, and on 21 that's a virtual-thread
   executor. The migration risk is `synchronized` in hot paths — pinning."
```

---

**Related files:** [12 — Java 17 Features](./12-java17-features.md) · [32 — Multithreading (virtual threads in context)](./32-multithreading.md) · [05 — Core Java](./05-java.md) · [33 — Interface vs Abstract Class (sealed types)](./33-interface-vs-abstract-class.md) · [39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)
