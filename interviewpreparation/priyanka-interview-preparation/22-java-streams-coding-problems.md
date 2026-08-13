# Java 8 Streams — 20 Coding Problems with Answers

The 20 questions that come up again and again in Java interviews, each with a **simple, readable solution** you can write on a whiteboard.

> **Your context:** the RoboGebra backend is **Java 17**, and 198 files already use `.stream()` ([18 — Technical Versions](./18-robogebra-technical-versions.md)). So `.toList()` (Java 16+) is available to you — but I've shown `Collectors.toList()` too, since some interviewers still expect it.

---

## Before you start — 4 things to know

**1. A stream pipeline has three parts:**
```java
list.stream()                    // SOURCE
    .filter(n -> n > 10)         // INTERMEDIATE (lazy, returns a Stream)
    .map(n -> n * 2)             // INTERMEDIATE
    .collect(Collectors.toList()); // TERMINAL (triggers execution, returns a result)
```

**2. Nothing runs until the terminal operation.** Intermediate operations are lazy — they just build the pipeline.

**3. A stream can only be consumed once.** Reuse it and you get `IllegalStateException: stream has already been operated upon or closed`.

**4. Streams don't modify the source.** They produce a new result.

**Static imports that make the code readable** (assume these in every answer):
```java
import java.util.*;
import java.util.stream.*;
import static java.util.stream.Collectors.*;
import static java.util.function.Function.identity;
```

**The sample class used throughout:**
```java
record Employee(int id, String name, String department, double salary, int age) {}
// (Java 17 record — or a normal class with getters if the interviewer prefers)
```

---

## Table of contents

| # | Problem | Key API |
|---|---|---|
| [1](#1-count-character-occurrences-in-a-string) | Count character occurrences | `groupingBy` + `counting` |
| [2](#2-find-the-first-non-repeated-character) | First non-repeated character | `LinkedHashMap` + `findFirst` |
| [3](#3-identify-duplicate-elements-in-a-list) | Find duplicates | `Set.add()` trick |
| [4](#4-find-max-and-min-using-streams) | Max and min | `max` / `min` / `summaryStatistics` |
| [5](#5-sort-a-list-of-objects-by-a-field) | Sort objects by field | `Comparator.comparing` |
| [6](#6-convert-a-list-into-a-map) | List → Map | `Collectors.toMap` |
| [7](#7-sum-and-average-of-a-list-of-numbers) | Sum and average | `mapToInt` |
| [8](#8-frequency-of-each-element-in-a-list) | Element frequency | `groupingBy` + `counting` |
| [9](#9-remove-duplicates-from-a-list) | Remove duplicates | `distinct` |
| [10](#10-reverse-a-string-using-streams) | Reverse a string | `reduce` |
| [11](#11-find-the-second-highest-number) | Second highest | `distinct` + `skip` |
| [12](#12-check-if-two-strings-are-anagrams) | Anagrams | `chars().sorted()` |
| [13](#13-group-objects-by-a-field) | Group by field | `groupingBy` |
| [14](#14-partition-a-list-into-even-and-odd) | Partition even/odd | `partitioningBy` |
| [15](#15-find-the-longest-string-in-a-list) | Longest string | `max(comparingInt)` |
| [16](#16-check-if-all-elements-match-a-condition) | Match conditions | `allMatch` / `anyMatch` / `noneMatch` |
| [17](#17-find-any--first-element) | Find any / first | `findFirst` / `findAny` |
| [18](#18-convert-a-stream-to-a-list-or-set) | Stream → List/Set | `toList` / `toSet` |
| [19](#19-join-a-list-of-strings-with-a-delimiter) | Join strings | `Collectors.joining` |
| [20](#20-flatten-a-list-of-lists) | Flatten nested lists | `flatMap` |

---

## 1. Count character occurrences in a string

```java
String str = "programming";

Map<Character, Long> counts = str.chars()              // IntStream of char codes
    .mapToObj(c -> (char) c)                            // IntStream → Stream<Character>
    .collect(groupingBy(identity(), counting()));

System.out.println(counts);
// {p=1, a=1, r=2, g=2, i=1, m=2, n=1, o=1}
```

**How it works:** `str.chars()` gives an `IntStream`, so you cast back to `char`. `groupingBy(identity(), counting())` says *"group by the character itself, and count each group."*

**Keep insertion order** — use a `LinkedHashMap`:
```java
Map<Character, Long> ordered = str.chars()
    .mapToObj(c -> (char) c)
    .collect(groupingBy(identity(), LinkedHashMap::new, counting()));
// {p=1, r=2, o=1, g=2, a=1, m=2, i=1, n=1}
```

**Count one specific character:**
```java
long count = str.chars().filter(c -> c == 'g').count();   // 2
```

> **Follow-up: "without streams?"**
> ```java
> Map<Character, Integer> map = new HashMap<>();
> for (char c : str.toCharArray()) {
>     map.put(c, map.getOrDefault(c, 0) + 1);
> }
> ```
> `map.merge(c, 1, Integer::sum)` is the shorter modern version. Know both.

---

## 2. Find the first non-repeated character

```java
String str = "swiss";

Character first = str.chars()
    .mapToObj(c -> (char) c)
    .collect(groupingBy(identity(), LinkedHashMap::new, counting()))  // keep order!
    .entrySet().stream()
    .filter(e -> e.getValue() == 1L)
    .map(Map.Entry::getKey)
    .findFirst()
    .orElse(null);

System.out.println(first);   // w
```

**The trick:** `LinkedHashMap::new` is essential. A plain `HashMap` has no ordering, so "first" would be meaningless.

**One-pass alternative (better for very long strings):**
```java
Character first = str.chars()
    .mapToObj(c -> (char) c)
    .filter(c -> str.indexOf(c) == str.lastIndexOf(c))   // appears exactly once
    .findFirst()
    .orElse(null);
```
> ⚠️ Mention the trade-off: this version is O(n²) because of the `indexOf` scans. The map version is O(n). Interviewers like candidates who notice.

---

## 3. Identify duplicate elements in a list

```java
List<Integer> nums = List.of(1, 2, 3, 2, 4, 5, 1, 6, 3);

Set<Integer> seen = new HashSet<>();
List<Integer> duplicates = nums.stream()
    .filter(n -> !seen.add(n))      // add() returns false if already present
    .distinct()                      // in case something appears 3+ times
    .collect(toList());

System.out.println(duplicates);   // [2, 1, 3]
```

**The trick:** `Set.add()` returns `false` when the element is already there. So `!seen.add(n)` is `true` exactly for duplicates.

**Frequency-based version (cleaner, no shared mutable state):**
```java
List<Integer> duplicates = nums.stream()
    .collect(groupingBy(identity(), counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey)
    .collect(toList());
```

> **Follow-up: "which is better?"** The second. The `Set.add()` trick mutates an external variable, which breaks with parallel streams. Say that — it's exactly the kind of detail that separates candidates.

---

## 4. Find max and min using Streams

```java
List<Integer> nums = List.of(45, 12, 89, 7, 63);

// With Optional
Optional<Integer> max = nums.stream().max(Comparator.naturalOrder());
Optional<Integer> min = nums.stream().min(Comparator.naturalOrder());
System.out.println(max.get() + " " + min.get());     // 89 7

// With IntStream — no Optional boxing
int maxVal = nums.stream().mapToInt(Integer::intValue).max().getAsInt();   // 89
int minVal = nums.stream().mapToInt(Integer::intValue).min().getAsInt();   // 7

// ⭐ Best: one pass, everything at once
IntSummaryStatistics stats = nums.stream().mapToInt(Integer::intValue).summaryStatistics();
System.out.println(stats.getMax());      // 89
System.out.println(stats.getMin());      // 7
System.out.println(stats.getSum());      // 216
System.out.println(stats.getAverage());  // 43.2
System.out.println(stats.getCount());    // 5
```

**On objects:**
```java
Employee highestPaid = employees.stream()
    .max(Comparator.comparingDouble(Employee::salary))
    .orElseThrow();
```

> 🎯 **`summaryStatistics()` is the answer that impresses.** It computes max, min, sum, average and count in **one pass** instead of five separate streams.

---

## 5. Sort a list of objects by a field

```java
List<Employee> employees = List.of(
    new Employee(1, "Priya",  "IT",    75000, 28),
    new Employee(2, "Arun",   "HR",    55000, 35),
    new Employee(3, "Divya",  "IT",    85000, 30),
    new Employee(4, "Karthik","Sales", 65000, 26)
);

// Ascending
List<Employee> bySalary = employees.stream()
    .sorted(Comparator.comparingDouble(Employee::salary))
    .collect(toList());

// Descending
List<Employee> desc = employees.stream()
    .sorted(Comparator.comparingDouble(Employee::salary).reversed())
    .collect(toList());

// Multiple fields: department ascending, then salary descending
List<Employee> multi = employees.stream()
    .sorted(Comparator.comparing(Employee::department)
                      .thenComparing(Comparator.comparingDouble(Employee::salary).reversed()))
    .collect(toList());

// Null-safe on a nullable field
Comparator.comparing(Employee::name, Comparator.nullsLast(Comparator.naturalOrder()));
```

> ⚠️ **`sorted()` does not modify the original list** — it returns a new stream. `list.sort(...)` mutates in place. Interviewers ask this.

---

## 6. Convert a List into a Map

```java
Map<Integer, String> idToName = employees.stream()
    .collect(toMap(Employee::id, Employee::name));
// {1=Priya, 2=Arun, 3=Divya, 4=Karthik}

// Whole object as the value
Map<Integer, Employee> byId = employees.stream()
    .collect(toMap(Employee::id, identity()));
```

### ⚠️ The duplicate-key trap — the #1 follow-up

```java
// ❌ Throws IllegalStateException: Duplicate key IT
Map<String, String> byDept = employees.stream()
    .collect(toMap(Employee::department, Employee::name));

// ✅ Supply a merge function to say what to do on collision
Map<String, String> byDept = employees.stream()
    .collect(toMap(Employee::department, Employee::name,
                   (existing, replacement) -> existing + ", " + replacement));
// {IT=Priya, Divya, HR=Arun, Sales=Karthik}

// Keep the first / keep the last
(a, b) -> a      // first wins
(a, b) -> b      // last wins
```

**Choose the map type (4-argument form):**
```java
Map<Integer, String> sorted = employees.stream()
    .collect(toMap(Employee::id, Employee::name, (a, b) -> a, TreeMap::new));
```

> ⚠️ **`toMap` also throws NPE if a value is null** — a real gotcha. `groupingBy` doesn't have that problem.

---

## 7. Sum and average of a list of numbers

```java
List<Integer> nums = List.of(10, 20, 30, 40, 50);

int sum = nums.stream().mapToInt(Integer::intValue).sum();              // 150
double avg = nums.stream().mapToInt(Integer::intValue).average().orElse(0.0);  // 30.0

// With reduce
int sum2 = nums.stream().reduce(0, Integer::sum);                      // 150

// With a collector
double avg2 = nums.stream().collect(averagingInt(Integer::intValue));  // 30.0
int sum3 = nums.stream().collect(summingInt(Integer::intValue));       // 150
```

**On an object field:**
```java
double totalSalary = employees.stream().mapToDouble(Employee::salary).sum();
double avgSalary   = employees.stream().mapToDouble(Employee::salary).average().orElse(0);
```

> **Why `average()` returns `OptionalDouble`:** an empty list has no average. `.orElse(0.0)` handles it. Saying this shows you think about edge cases.

---

## 8. Find the frequency of each element in a list

```java
List<String> items = List.of("apple", "banana", "apple", "orange", "banana", "apple");

Map<String, Long> frequency = items.stream()
    .collect(groupingBy(identity(), counting()));

System.out.println(frequency);
// {banana=2, orange=1, apple=3}
```

**Sorted by count, highest first:**
```java
LinkedHashMap<String, Long> sorted = frequency.entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue,
                   (a, b) -> a, LinkedHashMap::new));
// {apple=3, banana=2, orange=1}
```

**The most frequent element:**
```java
String mostCommon = frequency.entrySet().stream()
    .max(Map.Entry.comparingByValue())
    .map(Map.Entry::getKey)
    .orElse(null);   // apple
```

---

## 9. Remove duplicates from a list

```java
List<Integer> nums = List.of(1, 2, 3, 2, 4, 1, 5);

List<Integer> unique = nums.stream().distinct().collect(toList());
// [1, 2, 3, 4, 5]  — order preserved

// Via a Set (order NOT preserved)
Set<Integer> set = new HashSet<>(nums);

// Order preserved with a Set
Set<Integer> ordered = new LinkedHashSet<>(nums);
```

### ⚠️ Duplicates of **objects** — the important follow-up

`distinct()` uses `equals()` and `hashCode()`. Without them, two objects with identical fields are treated as different.

```java
// Records generate equals/hashCode automatically ✅
// A normal class must override both, or distinct() won't work

// Distinct by ONE field (no equals/hashCode needed):
List<Employee> uniqueByDept = employees.stream()
    .collect(toMap(Employee::department, identity(), (a, b) -> a))
    .values().stream()
    .collect(toList());

// Or with a TreeSet comparator
List<Employee> byName = employees.stream()
    .collect(collectingAndThen(
        toCollection(() -> new TreeSet<>(Comparator.comparing(Employee::name))),
        ArrayList::new));
```

---

## 10. Reverse a string using Stream operations

```java
String str = "interview";

// Stream way
String reversed = str.chars()
    .mapToObj(c -> String.valueOf((char) c))
    .reduce("", (a, b) -> b + a);         // prepend each character
System.out.println(reversed);   // weivretni
```

**How it works:** `reduce` normally appends (`a + b`). Swapping to `b + a` puts each new character *in front*, reversing the string.

**The honest answer — say this too:**
```java
String reversed = new StringBuilder(str).reverse().toString();   // ✅ simplest and fastest
```

> 🎯 **How to handle this in an interview:** *"Streams can do it with `reduce`, but string concatenation in a reduce is O(n²) because each step creates a new String. In real code I'd use `StringBuilder.reverse()` — O(n)."* Showing you know when **not** to use a stream is a plus, not a minus.

**Reverse the words instead of the characters:**
```java
String s = "Java is fun";
String result = Arrays.stream(s.split(" "))
    .reduce((a, b) -> b + " " + a)
    .orElse("");
// fun is Java
```

---

## 11. Find the second highest number

```java
List<Integer> nums = List.of(45, 12, 89, 7, 89, 63);

Optional<Integer> secondHighest = nums.stream()
    .distinct()                             // ⭐ essential — 89 appears twice
    .sorted(Comparator.reverseOrder())
    .skip(1)
    .findFirst();

System.out.println(secondHighest.orElse(-1));   // 63
```

**The trap:** without `distinct()`, the answer would be `89` — the duplicate. Interviewers plant a duplicate specifically to test this.

**Second highest salary (a classic SQL-style question in Java form):**
```java
Optional<Employee> second = employees.stream()
    .sorted(Comparator.comparingDouble(Employee::salary).reversed())
    .skip(1)
    .findFirst();
```

**Nth highest, generalized:**
```java
static Optional<Integer> nthHighest(List<Integer> list, int n) {
    return list.stream().distinct()
               .sorted(Comparator.reverseOrder())
               .skip(n - 1)
               .findFirst();
}
```

---

## 12. Check if two strings are anagrams

```java
String a = "listen", b = "silent";

boolean isAnagram = sortChars(a).equals(sortChars(b));

static String sortChars(String s) {
    return s.replaceAll("\\s", "").toLowerCase()
            .chars()
            .sorted()
            .mapToObj(c -> String.valueOf((char) c))
            .collect(joining());
}

System.out.println(isAnagram);   // true
```

**Frequency-map version (O(n) instead of O(n log n)):**
```java
static Map<Character, Long> freq(String s) {
    return s.toLowerCase().chars()
            .mapToObj(c -> (char) c)
            .collect(groupingBy(identity(), counting()));
}

boolean isAnagram = freq(a).equals(freq(b));
```

**One-liner without streams — mention it as the practical answer:**
```java
char[] x = a.toCharArray(), y = b.toCharArray();
Arrays.sort(x); Arrays.sort(y);
boolean isAnagram = Arrays.equals(x, y);
```

> ⚠️ Always **check the lengths first** as a fast exit, and decide out loud whether case and spaces matter. Stating your assumptions is half the marks.

---

## 13. Group objects by a specific field

```java
// Simple grouping
Map<String, List<Employee>> byDept = employees.stream()
    .collect(groupingBy(Employee::department));
// {IT=[Priya, Divya], HR=[Arun], Sales=[Karthik]}

// Count per group
Map<String, Long> countByDept = employees.stream()
    .collect(groupingBy(Employee::department, counting()));
// {IT=2, HR=1, Sales=1}

// Only the names per group
Map<String, List<String>> namesByDept = employees.stream()
    .collect(groupingBy(Employee::department, mapping(Employee::name, toList())));
// {IT=[Priya, Divya], HR=[Arun], Sales=[Karthik]}

// Average salary per group
Map<String, Double> avgByDept = employees.stream()
    .collect(groupingBy(Employee::department, averagingDouble(Employee::salary)));
// {IT=80000.0, HR=55000.0, Sales=65000.0}

// Total salary per group
Map<String, Double> totalByDept = employees.stream()
    .collect(groupingBy(Employee::department, summingDouble(Employee::salary)));

// Highest earner per group
Map<String, Optional<Employee>> topByDept = employees.stream()
    .collect(groupingBy(Employee::department,
             maxBy(Comparator.comparingDouble(Employee::salary))));

// Sorted output
Map<String, List<Employee>> sortedGroups = employees.stream()
    .collect(groupingBy(Employee::department, TreeMap::new, toList()));

// Two-level grouping — department, then age band
Map<String, Map<String, List<Employee>>> nested = employees.stream()
    .collect(groupingBy(Employee::department,
             groupingBy(e -> e.age() > 30 ? "Senior" : "Junior")));
```

> 🎯 **`groupingBy` with a downstream collector is the single most valuable Streams API to know.** `counting()`, `mapping()`, `averagingX()`, `summingX()`, `maxBy()`, `toSet()`, `joining()` all work as the second argument.

---

## 14. Partition a list into even and odd numbers

```java
List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

Map<Boolean, List<Integer>> partitioned = nums.stream()
    .collect(partitioningBy(n -> n % 2 == 0));

System.out.println(partitioned.get(true));    // [2, 4, 6, 8, 10]  — even
System.out.println(partitioned.get(false));   // [1, 3, 5, 7, 9]   — odd
```

**With a downstream collector:**
```java
Map<Boolean, Long> counts = nums.stream()
    .collect(partitioningBy(n -> n % 2 == 0, counting()));
// {false=5, true=5}
```

### `partitioningBy` vs `groupingBy` — expect this question

| | `partitioningBy` | `groupingBy` |
|---|---|---|
| Keys | Always exactly **two**: `true` and `false` | Any number |
| Key type | `Boolean` | Anything |
| Empty groups | **Both keys always present**, even if empty | Missing keys simply absent |
| Argument | A `Predicate` | A classifier `Function` |
| Performance | Slightly faster for a boolean split | General purpose |

> 🎯 The "both keys always present" point is the detail most candidates miss. `partitioningBy` on an empty list still gives you `{false=[], true=[]}`.

---

## 15. Find the longest string in a list

```java
List<String> words = List.of("java", "streams", "interview", "code");

Optional<String> longest = words.stream()
    .max(Comparator.comparingInt(String::length));
System.out.println(longest.get());   // interview

// Shortest
Optional<String> shortest = words.stream().min(Comparator.comparingInt(String::length));
// code

// ALL strings of maximum length (handles ties)
int maxLen = words.stream().mapToInt(String::length).max().orElse(0);
List<String> allLongest = words.stream()
    .filter(w -> w.length() == maxLen)
    .collect(toList());

// Longest, with alphabetical order breaking ties
Optional<String> longestThenAlpha = words.stream()
    .max(Comparator.comparingInt(String::length)
                   .thenComparing(Comparator.reverseOrder()));
```

> 💡 The **ties** version is worth volunteering — `max()` returns only one element, and real data has ties.

---

## 16. Check if all elements match a condition

```java
List<Integer> nums = List.of(2, 4, 6, 8, 10);

boolean allEven  = nums.stream().allMatch(n -> n % 2 == 0);   // true
boolean anyOver5 = nums.stream().anyMatch(n -> n > 5);        // true
boolean noneNeg  = nums.stream().noneMatch(n -> n < 0);       // true

// On objects
boolean allInIT      = employees.stream().allMatch(e -> e.department().equals("IT"));
boolean anyHighEarner = employees.stream().anyMatch(e -> e.salary() > 80000);
```

### ⚠️ The empty-stream trap — a favourite trick question

```java
List<Integer> empty = List.of();

empty.stream().allMatch(n -> n > 100);    // true   ⚠️ !!
empty.stream().anyMatch(n -> n > 100);    // false
empty.stream().noneMatch(n -> n > 100);   // true
```

**Why is `allMatch` true on an empty stream?** It's *vacuous truth* — there is no element that violates the condition, so the statement holds. Mathematically correct, and it surprises almost everyone.

> 💡 All three are **short-circuiting** — they stop as soon as the answer is decided, rather than scanning the whole stream.

---

## 17. Find any / first element

```java
List<String> names = List.of("Priya", "Arun", "Divya", "Karthik");

Optional<String> first = names.stream().findFirst();          // Priya
Optional<String> any   = names.stream().findAny();            // usually Priya (sequential)

Optional<String> firstD = names.stream()
    .filter(n -> n.startsWith("D"))
    .findFirst();                                              // Divya

String result = names.stream()
    .filter(n -> n.startsWith("Z"))
    .findFirst()
    .orElse("Not found");                                      // Not found
```

### `findFirst` vs `findAny`

| | `findFirst` | `findAny` |
|---|---|---|
| Sequential stream | First element | Usually the first |
| **Parallel stream** | **First in encounter order** (costs coordination) | **Whatever finishes first** — faster |
| Use when | Order matters | Order doesn't matter |

> 💬 *"On a sequential stream they behave the same. On a parallel stream `findFirst` has to respect encounter order, which costs synchronisation, whereas `findAny` returns whichever thread finds a match first — so `findAny` is the faster choice when you genuinely don't care which element you get."*

**Handling the `Optional` — know all four:**
```java
opt.get()                          // ❌ throws if empty — avoid
opt.orElse("default")              // default value
opt.orElseGet(() -> compute())     // lazy default — only computed if empty
opt.orElseThrow(() -> new NotFoundException("no match"))
opt.ifPresent(v -> System.out.println(v));
```

---

## 18. Convert a Stream to a List or Set

```java
List<Integer> nums = List.of(5, 3, 8, 1, 9, 3);

// Java 16+ — shortest (⚠️ returns an UNMODIFIABLE list)
List<Integer> list1 = nums.stream().filter(n -> n > 3).toList();

// Classic — returns a mutable ArrayList
List<Integer> list2 = nums.stream().filter(n -> n > 3).collect(toList());

// Set (duplicates removed, unordered)
Set<Integer> set = nums.stream().collect(toSet());

// Specific implementations
List<Integer>  arrayList = nums.stream().collect(toCollection(ArrayList::new));
Set<Integer>   linkedSet = nums.stream().collect(toCollection(LinkedHashSet::new)); // keeps order
TreeSet<Integer> treeSet = nums.stream().collect(toCollection(TreeSet::new));       // sorted

// Array
Integer[] arr  = nums.stream().toArray(Integer[]::new);
int[]     ints = nums.stream().mapToInt(Integer::intValue).toArray();
```

> ⚠️ **The `.toList()` gotcha** — `stream().toList()` (Java 16+) returns an **immutable** list. Calling `.add()` on it throws `UnsupportedOperationException`. Use `collect(toList())` when you need to modify the result. Your codebase is Java 17, so this is a live issue.

---

## 19. Join a list of strings with a delimiter

```java
List<String> names = List.of("Priya", "Arun", "Divya");

String joined = names.stream().collect(joining(", "));
// Priya, Arun, Divya

// With prefix and suffix
String bracketed = names.stream().collect(joining(", ", "[", "]"));
// [Priya, Arun, Divya]

// Simplest of all — no stream needed
String simple = String.join(", ", names);
// Priya, Arun, Divya

// Joining a field from objects
String allNames = employees.stream()
    .map(Employee::name)
    .collect(joining(" | "));
// Priya | Arun | Divya | Karthik

// Transform then join
String upper = names.stream()
    .map(String::toUpperCase)
    .sorted()
    .collect(joining(", "));
// ARUN, DIVYA, PRIYA
```

> 💡 Mention `String.join()` for a plain list — it's clearer than a stream when there's no transformation. Reaching for a stream where a one-liner exists is a small red flag.

---

## 20. Flatten a list of lists

```java
List<List<Integer>> nested = List.of(
    List.of(1, 2, 3),
    List.of(4, 5),
    List.of(6, 7, 8, 9)
);

List<Integer> flat = nested.stream()
    .flatMap(List::stream)          // each inner List becomes a Stream, all merged
    .collect(toList());

System.out.println(flat);   // [1, 2, 3, 4, 5, 6, 7, 8, 9]
```

### `map` vs `flatMap` — the question behind the question

| | `map` | `flatMap` |
|---|---|---|
| Transforms | One value → one value | One value → **a stream of values** |
| On `List<List<Integer>>` | `Stream<Stream<Integer>>` (still nested) | `Stream<Integer>` (flattened) |
| Think of it as | Transform | Transform **+ flatten** |

**Real-world uses:**
```java
// All words from a list of sentences
List<String> sentences = List.of("Java is fun", "Streams are powerful");
List<String> words = sentences.stream()
    .flatMap(s -> Arrays.stream(s.split(" ")))
    .collect(toList());
// [Java, is, fun, Streams, are, powerful]

// All skills across all employees, deduplicated and sorted
List<String> allSkills = employees.stream()
    .flatMap(e -> e.skills().stream())
    .distinct()
    .sorted()
    .collect(toList());

// Flatten a Map's values
Map<String, List<Employee>> byDept = ...;
List<Employee> everyone = byDept.values().stream()
    .flatMap(List::stream)
    .collect(toList());

// Unique characters across a list of words
List<Character> chars = words.stream()
    .flatMap(w -> w.chars().mapToObj(c -> (char) c))
    .distinct()
    .collect(toList());
```

---

## 🎁 Bonus problems that often follow

**Sum of even numbers:**
```java
int sum = nums.stream().filter(n -> n % 2 == 0).mapToInt(Integer::intValue).sum();
```

**First 5 elements / skip first 5:**
```java
nums.stream().limit(5).toList();
nums.stream().skip(5).toList();
```

**Check if a string is a palindrome:**
```java
String s = "madam";
boolean isPalindrome = IntStream.range(0, s.length() / 2)
    .allMatch(i -> s.charAt(i) == s.charAt(s.length() - 1 - i));
```

**Count words in a sentence:**
```java
long words = Arrays.stream(sentence.split("\\s+")).filter(w -> !w.isBlank()).count();
```

**Squares of the first 10 numbers:**
```java
List<Integer> squares = IntStream.rangeClosed(1, 10).map(i -> i * i).boxed().toList();
```

**Employees with salary > 60000, names sorted:**
```java
List<String> names = employees.stream()
    .filter(e -> e.salary() > 60000)
    .map(Employee::name)
    .sorted()
    .toList();
```

**Merge two lists and remove duplicates:**
```java
List<Integer> merged = Stream.concat(a.stream(), b.stream()).distinct().toList();
```

**Find common elements (intersection):**
```java
List<Integer> common = a.stream().filter(b::contains).distinct().toList();
```

---

## 📋 API quick reference

| Need | Use |
|---|---|
| Transform each element | `map` |
| Transform + flatten | `flatMap` |
| Keep matching elements | `filter` |
| Remove duplicates | `distinct` |
| Sort | `sorted(Comparator...)` |
| Take/skip N | `limit(n)` / `skip(n)` |
| Count | `count()` |
| Sum/average | `mapToInt(...).sum()` / `.average()` |
| Max/min | `max(cmp)` / `min(cmp)` |
| All stats at once | `summaryStatistics()` |
| Group | `groupingBy(fn)` |
| Group + aggregate | `groupingBy(fn, counting())` |
| Split in two by predicate | `partitioningBy(pred)` |
| List → Map | `toMap(key, value, merge)` |
| Join strings | `joining(", ")` |
| Any/all/none match | `anyMatch` / `allMatch` / `noneMatch` |
| Get one element | `findFirst()` / `findAny()` |
| To List (immutable) | `.toList()` — Java 16+ |
| To List (mutable) | `collect(toList())` |
| To Set | `collect(toSet())` |
| To specific collection | `collect(toCollection(TreeSet::new))` |
| Combine into one value | `reduce(identity, accumulator)` |
| Peek at values (debug only) | `peek` |

---

## ⚠️ Traps to remember

1. **A stream is single-use.** Reusing one throws `IllegalStateException`.
2. **`toMap` throws on duplicate keys** — always consider a merge function.
3. **`toMap` throws NPE on a null value.** `groupingBy` doesn't.
4. **`stream().toList()` is immutable** (Java 16+); `collect(toList())` is mutable.
5. **`allMatch` on an empty stream is `true`** — vacuous truth.
6. **`distinct()` on objects needs `equals`/`hashCode`.**
7. **`sorted()` returns a new stream**; `list.sort()` mutates in place.
8. **`findFirst` vs `findAny`** only differ on parallel streams.
9. **Don't mutate external state inside a lambda** — it breaks with parallel streams.
10. **`average()` returns `OptionalDouble`** — handle the empty case.
11. **Don't force a stream where a loop is clearer.** `StringBuilder.reverse()` beats a `reduce`.
12. **`parallelStream()` is not automatically faster** — only worth it for large datasets with CPU-heavy work.

---

## 🗣️ How to answer these in the room

1. **Restate the problem** and confirm edge cases out loud — *"Should this be case-sensitive? What if the list is empty?"*
2. **Say your approach before you write** — *"I'll group by the character and count each group."*
3. **Write it, then trace one example** through the pipeline.
4. **Volunteer the complexity** — *"This is O(n), one pass."*
5. **Offer the alternative** — *"There's a non-stream version with a `for` loop and `map.merge`, if you'd prefer that."*

> **The most valuable habit:** name a trade-off unprompted. *"I'd use `StringBuilder` here rather than `reduce`, because string concatenation in a reduce is O(n²)."* That one sentence marks you as someone who has written production Java, not just practised puzzles.

---

### Related files
- [05-java.md](./05-java.md) — OOP, collections, HashMap internals, Java 8 basics
- [12-java17-features.md](./12-java17-features.md) — records, sealed classes, pattern matching
- [09-coding-problems.md](./09-coding-problems.md) — 35+ general DSA problems in JS and Java
