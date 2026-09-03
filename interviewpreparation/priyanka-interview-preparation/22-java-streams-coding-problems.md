# Java 8 Streams — 20 Coding Problems (Easy Version)

The 20 questions that come up again and again in Java interviews, each with a **simple, readable solution** you can write on a whiteboard.

Every problem follows the same shape:
**Easiest way to remember → the code → how it works step by step → the trap → Easy memory box.**

> **Your context:** the RoboGebra backend is **Java 17**, and 198 files already use `.stream()` ([18 — Technical Versions](./18-robogebra-technical-versions.md)). So `.toList()` (Java 16+) is available to you — but I've shown `Collectors.toList()` too, since some interviewers still expect it.

---

## Before you start — 4 things to know

### 1. A stream pipeline has three parts

```java
list.stream()                      // SOURCE
    .filter(n -> n > 10)           // INTERMEDIATE (lazy, returns a Stream)
    .map(n -> n * 2)               // INTERMEDIATE
    .collect(Collectors.toList()); // TERMINAL (triggers execution, returns a result)
```

```
    SOURCE          INTERMEDIATE          TERMINAL
   list.stream() → filter → map → sorted → collect
                   └── lazy, nothing runs ──┘   └─ everything runs HERE ⚡
```

Real-world idea: a **washing machine**. Loading clothes, adding detergent and choosing the cycle are all just settings (intermediate). Pressing **START** is the terminal operation — only then does anything happen.

### 2. Nothing runs until the terminal operation

Intermediate operations are **lazy** — they only build the pipeline.

### 3. A stream can only be consumed ONCE

Reuse it and you get `IllegalStateException: stream has already been operated upon or closed`.

### 4. Streams don't modify the source

They produce a **new** result. The original list is untouched.

---

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
| [8](#8-find-the-frequency-of-each-element-in-a-list) | Element frequency | `groupingBy` + `counting` |
| [9](#9-remove-duplicates-from-a-list) | Remove duplicates | `distinct` |
| [10](#10-reverse-a-string-using-stream-operations) | Reverse a string | `reduce` |
| [11](#11-find-the-second-highest-number) | Second highest | `distinct` + `skip` |
| [12](#12-check-if-two-strings-are-anagrams) | Anagrams | `chars().sorted()` |
| [13](#13-group-objects-by-a-specific-field) | Group by field | `groupingBy` |
| [14](#14-partition-a-list-into-even-and-odd-numbers) | Partition even/odd | `partitioningBy` |
| [15](#15-find-the-longest-string-in-a-list) | Longest string | `max(comparingInt)` |
| [16](#16-check-if-all-elements-match-a-condition) | Match conditions | `allMatch` / `anyMatch` / `noneMatch` |
| [17](#17-find-any--first-element) | Find any / first | `findFirst` / `findAny` |
| [18](#18-convert-a-stream-to-a-list-or-set) | Stream → List/Set | `toList` / `toSet` |
| [19](#19-join-a-list-of-strings-with-a-delimiter) | Join strings | `Collectors.joining` |
| [20](#20-flatten-a-list-of-lists) | Flatten nested lists | `flatMap` |

---

## 1. Count character occurrences in a string

The easiest way to remember:

```
"Group by the character, then count each group."

groupingBy(identity(), counting())
```

```java
String str = "programming";

Map<Character, Long> counts = str.chars()              // IntStream of char codes
    .mapToObj(c -> (char) c)                            // IntStream → Stream<Character>
    .collect(groupingBy(identity(), counting()));

System.out.println(counts);
// {p=1, a=1, r=2, g=2, i=1, m=2, n=1, o=1}
```

#### How it works

```
"programming"
      ↓ .chars()
IntStream: 112, 114, 111, 103, ...     ← char CODES, not characters
      ↓ .mapToObj(c -> (char) c)
Stream<Character>: p, r, o, g, r, a, m, m, i, n, g
      ↓ groupingBy(identity(), counting())
{p=1, r=2, o=1, g=2, a=1, m=2, i=1, n=1}
```

`identity()` just means "group by the element itself".

Real-world idea: **sorting a bag of mixed coins into piles**, then counting each pile.

#### Keep insertion order — use a `LinkedHashMap`

```java
Map<Character, Long> ordered = str.chars()
    .mapToObj(c -> (char) c)
    .collect(groupingBy(identity(), LinkedHashMap::new, counting()));
// {p=1, r=2, o=1, g=2, a=1, m=2, i=1, n=1}
```

#### Count one specific character

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
> `map.merge(c, 1, Integer::sum)` is the shorter modern version. **Know both** — some interviewers specifically ask for the loop.

#### Easy memory

```
str.chars() → mapToObj(c -> (char) c) → groupingBy(identity(), counting())

⚠️ chars() gives an IntStream (codes), so you MUST mapToObj
⭐ Need order? → groupingBy(identity(), LinkedHashMap::new, counting())
Loop version: map.merge(c, 1, Integer::sum)
```

---

## 2. Find the first non-repeated character

The easiest way to remember:

```
Count every character (KEEPING ORDER), then take the first one whose count is 1.
```

```java
String str = "swiss";

Character first = str.chars()
    .mapToObj(c -> (char) c)
    .collect(groupingBy(identity(), LinkedHashMap::new, counting()))  // keep order! ⭐
    .entrySet().stream()
    .filter(e -> e.getValue() == 1L)
    .map(Map.Entry::getKey)
    .findFirst()
    .orElse(null);

System.out.println(first);   // w
```

#### How it works

```
"swiss"
   ↓ count, keeping insertion order
{s=3, w=1, i=1}
   ↓ filter value == 1
{w=1, i=1}
   ↓ findFirst
w  ✅
```

**The trick:** `LinkedHashMap::new` is essential. A plain `HashMap` has **no ordering**, so "first" would be meaningless — you might get `i` instead of `w`.

Real-world idea: a **queue of people**. You need the first person wearing a unique badge colour — so the queue order must be preserved.

#### One-pass alternative (better for very long strings… or is it?)

```java
Character first = str.chars()
    .mapToObj(c -> (char) c)
    .filter(c -> str.indexOf(c) == str.lastIndexOf(c))   // appears exactly once
    .findFirst()
    .orElse(null);
```

> ⚠️ Mention the trade-off out loud: this version is **O(n²)** because of the repeated `indexOf` scans. The map version is **O(n)**. Interviewers like candidates who notice.

#### Easy memory

```
LinkedHashMap is MANDATORY here (HashMap has no order → wrong answer) ⭐

count → filter(count == 1) → findFirst

Alternative: indexOf(c) == lastIndexOf(c)  → simpler but O(n²)
```

---

## 3. Identify duplicate elements in a list

The easiest way to remember:

```
Set.add() returns FALSE when the element is already there.
So !seen.add(n) is TRUE exactly for duplicates.
```

```java
List<Integer> nums = List.of(1, 2, 3, 2, 4, 5, 1, 6, 3);

Set<Integer> seen = new HashSet<>();
List<Integer> duplicates = nums.stream()
    .filter(n -> !seen.add(n))      // add() returns false if already present
    .distinct()                      // in case something appears 3+ times
    .collect(toList());

System.out.println(duplicates);   // [2, 1, 3]
```

#### How it works

```
1 → seen.add(1) = true  → !true = false → dropped
2 → seen.add(2) = true  → dropped
3 → seen.add(3) = true  → dropped
2 → seen.add(2) = FALSE → !false = TRUE → KEPT ✅ duplicate
```

Real-world idea: a **guest list at a party**. You tick each name as it arrives; if a name is already ticked, that person is a repeat.

#### Frequency-based version (cleaner, no shared mutable state)

```java
List<Integer> duplicates = nums.stream()
    .collect(groupingBy(identity(), counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey)
    .collect(toList());
```

> **Follow-up: "which is better?"** The **second**.
>
> The `Set.add()` trick **mutates an external variable**, which silently breaks with parallel streams. Say that — it's exactly the kind of detail that separates candidates.

#### Easy memory

```
Trick:  .filter(n -> !seen.add(n))     ← add() returns false for a duplicate

Better: groupingBy(identity(), counting()) → filter(count > 1) → map(getKey)
        (no external mutation → parallel-safe ⭐)
```

---

## 4. Find max and min using Streams

The easiest way to remember:

```
Need ONE thing?  → max() / min()
Need EVERYTHING? → summaryStatistics()  ⭐ one pass, all five values
```

```java
List<Integer> nums = List.of(45, 12, 89, 7, 63);

// With Optional
Optional<Integer> max = nums.stream().max(Comparator.naturalOrder());
Optional<Integer> min = nums.stream().min(Comparator.naturalOrder());
System.out.println(max.get() + " " + min.get());     // 89 7

// With IntStream — no Optional boxing
int maxVal = nums.stream().mapToInt(Integer::intValue).max().getAsInt();   // 89
int minVal = nums.stream().mapToInt(Integer::intValue).min().getAsInt();   // 7

// ⭐ Best: ONE pass, everything at once
IntSummaryStatistics stats = nums.stream().mapToInt(Integer::intValue).summaryStatistics();
System.out.println(stats.getMax());      // 89
System.out.println(stats.getMin());      // 7
System.out.println(stats.getSum());      // 216
System.out.println(stats.getAverage());  // 43.2
System.out.println(stats.getCount());    // 5
```

```
FIVE separate streams          ONE summaryStatistics()
  max  → pass 1                  → pass 1 → max, min, sum, average, count ✅
  min  → pass 2
  sum  → pass 3
  avg  → pass 4
  count→ pass 5   🐢
```

Real-world idea: instead of walking through the whole class list five times to find the topper, the failer, the total, the average and the headcount — you walk **once** and note all five as you go.

#### On objects

```java
Employee highestPaid = employees.stream()
    .max(Comparator.comparingDouble(Employee::salary))
    .orElseThrow();
```

> 🎯 **`summaryStatistics()` is the answer that impresses.** It computes max, min, sum, average and count in **one pass** instead of five separate streams.

#### Easy memory

```
max(Comparator.naturalOrder())  → Optional
mapToInt(...).max().getAsInt()  → primitive, no Optional boxing
summaryStatistics()             → max + min + sum + average + count in ONE pass ⭐

Objects: max(Comparator.comparingDouble(Employee::salary))
```

---

## 5. Sort a list of objects by a field

The easiest way to remember:

```
Comparator.comparing(Employee::field)
          .reversed()              ← descending
          .thenComparing(...)      ← tie-breaker
```

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

```
thenComparing = "if the first field is EQUAL, use this next"

HR    → Arun
IT    → Divya (85000)   ← salary descending WITHIN the department
IT    → Priya (75000)
Sales → Karthik
```

Real-world idea: a **class ranking**. Sort by marks; if two students have the same marks, sort by name.

> ⚠️ **`sorted()` does NOT modify the original list** — it returns a new stream.
> `list.sort(...)` mutates in place. Interviewers ask this.

#### Easy memory

```
sorted(Comparator.comparing(Employee::getName))
sorted(Comparator.comparingDouble(Employee::salary).reversed())     ← descending
      .thenComparing(...)                                           ← tie-breaker
Comparator.nullsLast(...)                                           ← null-safe

sorted()   → NEW stream, original untouched ✅
list.sort()→ mutates in place ⚠️
```

---

## 6. Convert a List into a Map

The easiest way to remember:

```
toMap(keyFunction, valueFunction)

⚠️ AND ALWAYS think about the third argument — the merge function.
```

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
```

```
Priya → key "IT"  → stored
Divya → key "IT"  → 💥 IllegalStateException: Duplicate key IT
```

```java
// ✅ Supply a merge function to say what to do on a collision
Map<String, String> byDept = employees.stream()
    .collect(toMap(Employee::department, Employee::name,
                   (existing, replacement) -> existing + ", " + replacement));
// {IT=Priya, Divya, HR=Arun, Sales=Karthik}
```

```
(a, b) -> a      // keep the FIRST
(a, b) -> b      // keep the LAST
(a, b) -> a + ", " + b   // combine them
```

Real-world idea: two people arrive with the **same house number**. The merge function is your rule: *"keep the first", "keep the newest", or "put both on the nameplate".*

#### Choose the map type (4-argument form)

```java
Map<Integer, String> sorted = employees.stream()
    .collect(toMap(Employee::id, Employee::name, (a, b) -> a, TreeMap::new));
```

> ⚠️ **`toMap` also throws a NullPointerException if a VALUE is null** — a real gotcha that surprises people. `groupingBy` doesn't have that problem.

#### Easy memory

```
toMap(key, value)                       → 💥 on duplicate keys
toMap(key, value, (a,b) -> a)           → ✅ first wins
toMap(key, value, (a,b) -> b)           → last wins
toMap(key, value, merge, TreeMap::new)  → choose the map type

⚠️ Duplicate key → IllegalStateException
⚠️ Null VALUE    → NullPointerException
```

---

## 7. Sum and average of a list of numbers

The easiest way to remember:

```
mapToInt / mapToDouble first → then .sum() or .average()
(this drops the Integer boxing and gives you the primitive methods)
```

```java
List<Integer> nums = List.of(10, 20, 30, 40, 50);

int sum = nums.stream().mapToInt(Integer::intValue).sum();                     // 150
double avg = nums.stream().mapToInt(Integer::intValue).average().orElse(0.0);  // 30.0

// With reduce
int sum2 = nums.stream().reduce(0, Integer::sum);                              // 150

// With a collector
double avg2 = nums.stream().collect(averagingInt(Integer::intValue));          // 30.0
int sum3 = nums.stream().collect(summingInt(Integer::intValue));               // 150
```

#### How `reduce` works, step by step

```
[10, 20, 30, 40, 50]      identity = 0

0  + 10 = 10
10 + 20 = 30
30 + 30 = 60
60 + 40 = 100
100+ 50 = 150   ← final result
```

#### On an object field

```java
double totalSalary = employees.stream().mapToDouble(Employee::salary).sum();
double avgSalary   = employees.stream().mapToDouble(Employee::salary).average().orElse(0);
```

> **Why does `average()` return `OptionalDouble`?** An **empty list has no average** — dividing by zero is undefined. `.orElse(0.0)` handles it. Saying this shows you think about edge cases.

#### Easy memory

```
sum     → mapToInt(...).sum()
average → mapToInt(...).average().orElse(0.0)   ← OptionalDouble! empty list has no average ⭐
reduce  → reduce(0, Integer::sum)

Objects: mapToDouble(Employee::salary).sum()
```

---

## 8. Find the frequency of each element in a list

The easiest way to remember:

```
Same recipe as problem 1: groupingBy(identity(), counting())
```

```java
List<String> items = List.of("apple", "banana", "apple", "orange", "banana", "apple");

Map<String, Long> frequency = items.stream()
    .collect(groupingBy(identity(), counting()));

System.out.println(frequency);
// {banana=2, orange=1, apple=3}
```

#### Sorted by count, highest first

```java
LinkedHashMap<String, Long> sorted = frequency.entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue,
                   (a, b) -> a, LinkedHashMap::new));
// {apple=3, banana=2, orange=1}
```

⚠️ `LinkedHashMap::new` again — collecting into a plain `HashMap` would **throw away** the sorting you just did.

#### The most frequent element

```java
String mostCommon = frequency.entrySet().stream()
    .max(Map.Entry.comparingByValue())
    .map(Map.Entry::getKey)
    .orElse(null);   // apple
```

Real-world idea: counting **votes**. Group the ballots by candidate, count each pile, then take the biggest pile.

#### Easy memory

```
frequency  → groupingBy(identity(), counting())
sort by count → .sorted(Map.Entry.comparingByValue().reversed())
                + collect into LinkedHashMap ⭐ (else the order is lost)
most common  → .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
```

---

## 9. Remove duplicates from a list

The easiest way to remember:

```
distinct()  → keeps ORDER, uses equals() + hashCode()
new HashSet<>(list) → loses order
new LinkedHashSet<>(list) → keeps order
```

```java
List<Integer> nums = List.of(1, 2, 3, 2, 4, 1, 5);

List<Integer> unique = nums.stream().distinct().collect(toList());
// [1, 2, 3, 4, 5]  — order preserved ✅

// Via a Set (order NOT preserved)
Set<Integer> set = new HashSet<>(nums);

// Order preserved with a Set
Set<Integer> ordered = new LinkedHashSet<>(nums);
```

### ⚠️ Duplicates of **objects** — the important follow-up

```
distinct() uses equals() and hashCode().

Without them, two objects with IDENTICAL fields are treated as different. 💥
```

```java
// Records generate equals/hashCode automatically ✅
// A normal class MUST override both, or distinct() does nothing useful
```

**Distinct by ONE field (no `equals`/`hashCode` needed):**

```java
List<Employee> uniqueByDept = employees.stream()
    .collect(toMap(Employee::department, identity(), (a, b) -> a))
    .values().stream()
    .collect(toList());
```

```
toMap with (a, b) -> a  → "if this department already has an employee, keep the first"
       ↓
.values()               → one employee per department ✅
```

**Or with a TreeSet comparator:**

```java
List<Employee> byName = employees.stream()
    .collect(collectingAndThen(
        toCollection(() -> new TreeSet<>(Comparator.comparing(Employee::name))),
        ArrayList::new));
```

#### Easy memory

```
distinct()                → order kept, needs equals()+hashCode() ⭐
new HashSet<>(list)       → order lost
new LinkedHashSet<>(list) → order kept

Distinct by ONE field:
   toMap(Employee::department, identity(), (a,b) -> a).values()
```

---

## 10. Reverse a string using Stream operations

The easiest way to remember:

```
reduce normally APPENDS  (a + b).
Swap it to (b + a) and each new character goes IN FRONT → reversed.
```

```java
String str = "interview";

String reversed = str.chars()
    .mapToObj(c -> String.valueOf((char) c))
    .reduce("", (a, b) -> b + a);         // prepend each character
System.out.println(reversed);   // weivretni
```

#### How it works

```
""  + i  →  "i"
i   → "n" + "i"  → "ni"
n   → "t" + "ni" → "tni"
...                        each new letter jumps to the FRONT
```

#### The honest answer — say this too ⭐

```java
String reversed = new StringBuilder(str).reverse().toString();   // ✅ simplest and fastest
```

> 🎯 **How to handle this in an interview:** *"Streams can do it with `reduce`, but string concatenation inside a reduce is O(n²) because each step creates a new String. In real code I'd use `StringBuilder.reverse()` — O(n)."*
>
> Showing you know when **not** to use a stream is a plus, not a minus.

#### Reverse the WORDS instead of the characters

```java
String s = "Java is fun";
String result = Arrays.stream(s.split(" "))
    .reduce((a, b) -> b + " " + a)
    .orElse("");
// fun is Java
```

#### Easy memory

```
Characters: .reduce("", (a, b) -> b + a)        ← b + a = prepend = reverse
Words:      .reduce((a, b) -> b + " " + a)

⚠️ O(n²) — in REAL code use: new StringBuilder(str).reverse().toString()  ⭐
   Saying this out loud SCORES points.
```

---

## 11. Find the second highest number

The easiest way to remember:

```
distinct() → sorted(reverseOrder()) → skip(1) → findFirst()

⚠️ distinct() is the whole question. Forget it and you get the duplicate.
```

```java
List<Integer> nums = List.of(45, 12, 89, 7, 89, 63);

Optional<Integer> secondHighest = nums.stream()
    .distinct()                             // ⭐ essential — 89 appears twice
    .sorted(Comparator.reverseOrder())
    .skip(1)
    .findFirst();

System.out.println(secondHighest.orElse(-1));   // 63
```

#### How it works

```
[45, 12, 89, 7, 89, 63]
        ↓ distinct()
[45, 12, 89, 7, 63]
        ↓ sorted(reverseOrder())
[89, 63, 45, 12, 7]
        ↓ skip(1)
[63, 45, 12, 7]
        ↓ findFirst()
63  ✅
```

**The trap:** without `distinct()`, the sorted list is `[89, 89, 63, ...]`, so `skip(1).findFirst()` gives **89** — the duplicate. Interviewers plant a duplicate specifically to test this.

Real-world idea: the **silver medallist**. If two athletes tie for gold, the second person on the list is still gold — you must remove the tie first.

#### Second highest salary (a classic SQL-style question in Java form)

```java
Optional<Employee> second = employees.stream()
    .sorted(Comparator.comparingDouble(Employee::salary).reversed())
    .skip(1)
    .findFirst();
```

#### Nth highest, generalised

```java
static Optional<Integer> nthHighest(List<Integer> list, int n) {
    return list.stream().distinct()
               .sorted(Comparator.reverseOrder())
               .skip(n - 1)
               .findFirst();
}
```

#### Easy memory

```
distinct() → sorted(reverseOrder()) → skip(1) → findFirst()

⚠️ FORGET distinct() AND YOU FAIL THE QUESTION ⭐
Nth highest → skip(n - 1)
```

---

## 12. Check if two strings are anagrams

The easiest way to remember:

```
Anagram = same letters, different order.
So: SORT both strings and compare. (Or compare their letter counts.)
```

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

```
"listen" → sorted → "eilnst"
"silent" → sorted → "eilnst"      ← identical ✅
```

Real-world idea: two bags of **Scrabble tiles**. Line the tiles up alphabetically; if both rows look identical, they're anagrams.

#### Frequency-map version (O(n) instead of O(n log n))

```java
static Map<Character, Long> freq(String s) {
    return s.toLowerCase().chars()
            .mapToObj(c -> (char) c)
            .collect(groupingBy(identity(), counting()));
}

boolean isAnagram = freq(a).equals(freq(b));
```

#### One-liner without streams — mention it as the practical answer

```java
char[] x = a.toCharArray(), y = b.toCharArray();
Arrays.sort(x); Arrays.sort(y);
boolean isAnagram = Arrays.equals(x, y);
```

> ⚠️ Always **check the lengths first** as a fast exit, and decide out loud whether case and spaces matter. **Stating your assumptions is half the marks.**

#### Easy memory

```
Sort both → compare        → O(n log n)
Count both → compare maps  → O(n) ⭐

Fast exit: if (a.length() != b.length()) return false;
Ask out loud: case-sensitive? spaces? ⭐
```

---

## 13. Group objects by a specific field

The easiest way to remember:

```
groupingBy(classifier)                    → Map<Key, List<T>>
groupingBy(classifier, downstream)        → Map<Key, whatever-you-want> ⭐
```

That **second argument** is the most valuable thing in the entire Streams API.

```java
// Simple grouping
Map<String, List<Employee>> byDept = employees.stream()
    .collect(groupingBy(Employee::department));
// {IT=[Priya, Divya], HR=[Arun], Sales=[Karthik]}

// Count per group
Map<String, Long> countByDept = employees.stream()
    .collect(groupingBy(Employee::department, counting()));
// {IT=2, HR=1, Sales=1}

// Only the NAMES per group
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

// Sorted output (TreeMap)
Map<String, List<Employee>> sortedGroups = employees.stream()
    .collect(groupingBy(Employee::department, TreeMap::new, toList()));

// TWO-LEVEL grouping — department, then age band
Map<String, Map<String, List<Employee>>> nested = employees.stream()
    .collect(groupingBy(Employee::department,
             groupingBy(e -> e.age() > 30 ? "Senior" : "Junior")));
```

#### The picture

```
                 employees
                     │
        groupingBy(department)
                     │
      ┌──────────────┼──────────────┐
      ↓              ↓              ↓
     "IT"           "HR"          "Sales"
   [Priya,          [Arun]        [Karthik]
    Divya]
      │
      └── downstream collector decides what each group BECOMES:
            counting()          → 2
            mapping(name,toList)→ [Priya, Divya]
            averagingDouble()   → 80000.0
            maxBy(salary)       → Divya
```

Real-world idea: sorting a **pile of exam papers into subject stacks**, and then — for each stack — either counting them, averaging the marks, or pulling out just the topper.

> 🎯 **`groupingBy` with a downstream collector is the single most valuable Streams API to know.** `counting()`, `mapping()`, `averagingX()`, `summingX()`, `maxBy()`, `toSet()` and `joining()` all work as the second argument.

#### Easy memory

```
groupingBy(Employee::department)                          → Map<String, List<Employee>>
groupingBy(dept, counting())                              → Map<String, Long>
groupingBy(dept, mapping(Employee::name, toList()))       → Map<String, List<String>>
groupingBy(dept, averagingDouble(Employee::salary))       → Map<String, Double>
groupingBy(dept, maxBy(comparing(salary)))                → Map<String, Optional<Employee>>
groupingBy(dept, TreeMap::new, toList())                  → sorted keys
groupingBy(dept, groupingBy(ageBand))                     → two levels ⭐
```

---

## 14. Partition a list into even and odd numbers

The easiest way to remember:

```
partitioningBy = groupingBy with EXACTLY two keys: true and false.
```

```java
List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

Map<Boolean, List<Integer>> partitioned = nums.stream()
    .collect(partitioningBy(n -> n % 2 == 0));

System.out.println(partitioned.get(true));    // [2, 4, 6, 8, 10]  — even
System.out.println(partitioned.get(false));   // [1, 3, 5, 7, 9]   — odd
```

```
        [1..10]
           │
   partitioningBy(n % 2 == 0)
      ┌────┴────┐
    true      false
 [2,4,6,8,10] [1,3,5,7,9]
```

Real-world idea: a **pass/fail split** of one exam. There are always exactly two buckets, even if one is empty.

#### With a downstream collector

```java
Map<Boolean, Long> counts = nums.stream()
    .collect(partitioningBy(n -> n % 2 == 0, counting()));
// {false=5, true=5}
```

### `partitioningBy` vs `groupingBy` — expect this question

| | `partitioningBy` | `groupingBy` |
|---|---|---|
| Keys | always exactly **two**: `true` and `false` | any number |
| Key type | `Boolean` | anything |
| Empty groups | **both keys always present**, even if empty | missing keys are simply absent |
| Argument | a `Predicate` | a classifier `Function` |
| Performance | slightly faster for a boolean split | general purpose |

> 🎯 The **"both keys always present"** point is the detail most candidates miss.
>
> ```java
> List.<Integer>of().stream().collect(partitioningBy(n -> n > 0));
> // {false=[], true=[]}      ← both keys, even on an empty list ⭐
> ```

#### Easy memory

```
partitioningBy(predicate) → Map<Boolean, List<T>>
   .get(true)  → matched
   .get(false) → didn't match

⭐ BOTH keys ALWAYS exist, even when empty (groupingBy would omit them)
Two buckets only → use groupingBy if you need more
```

---

## 15. Find the longest string in a list

The easiest way to remember:

```
max(Comparator.comparingInt(String::length))
```

```java
List<String> words = List.of("java", "streams", "interview", "code");

Optional<String> longest = words.stream()
    .max(Comparator.comparingInt(String::length));
System.out.println(longest.get());   // interview

// Shortest
Optional<String> shortest = words.stream().min(Comparator.comparingInt(String::length));
// code
```

#### ALL strings of maximum length (handles ties) ⭐

```java
int maxLen = words.stream().mapToInt(String::length).max().orElse(0);

List<String> allLongest = words.stream()
    .filter(w -> w.length() == maxLen)
    .collect(toList());
```

```
max() returns ONE element.
Real data has TIES. → two passes: find the max length, then filter by it.
```

#### Longest, with alphabetical order breaking ties

```java
Optional<String> longestThenAlpha = words.stream()
    .max(Comparator.comparingInt(String::length)
                   .thenComparing(Comparator.reverseOrder()));
```

> 💡 The **ties** version is worth volunteering unprompted — `max()` returns only one element, and real data has ties.

#### Easy memory

```
longest  → max(comparingInt(String::length))
shortest → min(comparingInt(String::length))

TIES ⭐ → find maxLen first, then filter(w -> w.length() == maxLen)
         (volunteer this — it shows you think about real data)
```

---

## 16. Check if all elements match a condition

The easiest way to remember:

```
allMatch  → EVERY element passes?
anyMatch  → at least ONE passes?
noneMatch → ZERO pass?

All three SHORT-CIRCUIT (they stop as soon as the answer is known). ⚡
```

```java
List<Integer> nums = List.of(2, 4, 6, 8, 10);

boolean allEven   = nums.stream().allMatch(n -> n % 2 == 0);   // true
boolean anyOver5  = nums.stream().anyMatch(n -> n > 5);        // true
boolean noneNeg   = nums.stream().noneMatch(n -> n < 0);       // true

// On objects
boolean allInIT       = employees.stream().allMatch(e -> e.department().equals("IT"));
boolean anyHighEarner = employees.stream().anyMatch(e -> e.salary() > 80000);
```

### ⚠️ The empty-stream trap — a favourite trick question ⭐

```java
List<Integer> empty = List.of();

empty.stream().allMatch(n -> n > 100);    // true   ⚠️ !!
empty.stream().anyMatch(n -> n > 100);    // false
empty.stream().noneMatch(n -> n > 100);   // true
```

**Why is `allMatch` true on an empty stream?**

```
It's VACUOUS TRUTH.

"All elements are > 100" means "there is NO element that is ≤ 100."
An empty list has no elements at all → nothing violates it → TRUE ✅
```

Real-world idea: *"Every student in this empty classroom passed."* There's nobody who failed, so technically the statement holds. Mathematically correct, and it surprises almost everyone.

#### Easy memory

```
allMatch  → all pass    → EMPTY stream = TRUE ⚠️⚠️ (vacuous truth)
anyMatch  → one passes  → EMPTY stream = false
noneMatch → none pass   → EMPTY stream = true

All three SHORT-CIRCUIT ⚡ (stop as soon as the answer is decided)
```

---

## 17. Find any / first element

The easiest way to remember:

```
findFirst → the FIRST in order   (costs coordination on a parallel stream)
findAny   → WHICHEVER is quickest (faster in parallel)

On a SEQUENTIAL stream they behave the same.
```

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
| Sequential stream | first element | usually the first |
| **Parallel stream** | **first in encounter order** (costs coordination) | **whatever finishes first** — faster |
| Use when | order matters | order doesn't matter |

> 💬 *"On a sequential stream they behave the same. On a parallel stream `findFirst` has to respect encounter order, which costs synchronisation, whereas `findAny` returns whichever thread finds a match first — so `findAny` is the faster choice when you genuinely don't care which element you get."*

Real-world idea: **four people searching a library for any copy of a book.** `findAny` = "shout as soon as you find one". `findFirst` = "we must have the copy from the lowest shelf number", so everyone must wait and compare.

#### Handling the `Optional` — know all four

```java
opt.get()                          // ❌ throws if empty — avoid
opt.orElse("default")              // default value
opt.orElseGet(() -> compute())     // LAZY default — only computed if empty ⭐
opt.orElseThrow(() -> new NotFoundException("no match"))
opt.ifPresent(v -> System.out.println(v));
```

#### Easy memory

```
findFirst → order guaranteed  | findAny → fastest, parallel-friendly
Same behaviour on a sequential stream ⭐

Optional: orElse (eager) | orElseGet (lazy) ⭐ | orElseThrow | ifPresent
NEVER just call .get()
```

---

## 18. Convert a Stream to a List or Set

The easiest way to remember:

```
.toList()          → Java 16+, shortest, but IMMUTABLE ⚠️
collect(toList())  → MUTABLE ArrayList
```

```java
List<Integer> nums = List.of(5, 3, 8, 1, 9, 3);

// Java 16+ — shortest (⚠️ returns an UNMODIFIABLE list)
List<Integer> list1 = nums.stream().filter(n -> n > 3).toList();

// Classic — returns a mutable ArrayList
List<Integer> list2 = nums.stream().filter(n -> n > 3).collect(toList());

// Set (duplicates removed, unordered)
Set<Integer> set = nums.stream().collect(toSet());

// Specific implementations
List<Integer>    arrayList = nums.stream().collect(toCollection(ArrayList::new));
Set<Integer>     linkedSet = nums.stream().collect(toCollection(LinkedHashSet::new)); // keeps order
TreeSet<Integer> treeSet   = nums.stream().collect(toCollection(TreeSet::new));       // sorted

// Array
Integer[] arr  = nums.stream().toArray(Integer[]::new);
int[]     ints = nums.stream().mapToInt(Integer::intValue).toArray();
```

> ⚠️ **The `.toList()` gotcha**
>
> ```java
> List<Integer> l = nums.stream().toList();
> l.add(10);        // 💥 UnsupportedOperationException
> ```
>
> `stream().toList()` (Java 16+) returns an **immutable** list. Use `collect(toList())` when you need to modify the result. **Your codebase is Java 17, so this is a live issue.**

#### Easy memory

```
.toList()             → Java 16+, IMMUTABLE ⚠️ (add() throws)
collect(toList())     → mutable ArrayList ✅
collect(toSet())      → duplicates removed, no order
toCollection(LinkedHashSet::new) → unique + order kept ⭐
toCollection(TreeSet::new)       → unique + sorted
toArray(Integer[]::new)          → array
```

---

## 19. Join a list of strings with a delimiter

The easiest way to remember:

```
Collectors.joining(", ")

No transformation needed? → just use String.join(", ", list)
```

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

// Joining a FIELD from objects
String allNames = employees.stream()
    .map(Employee::name)
    .collect(joining(" | "));
// Priya | Arun | Divya | Karthik

// Transform, then join
String upper = names.stream()
    .map(String::toUpperCase)
    .sorted()
    .collect(joining(", "));
// ARUN, DIVYA, PRIYA
```

> 💡 Mention `String.join()` for a plain list — it's clearer than a stream when there's no transformation. **Reaching for a stream where a one-liner exists is a small red flag.**

#### Easy memory

```
joining(", ")              → "a, b, c"
joining(", ", "[", "]")    → "[a, b, c]"
String.join(", ", list)    → same thing, NO stream needed ⭐

Objects: .map(Employee::name).collect(joining(" | "))
```

---

## 20. Flatten a list of lists

The easiest way to remember:

```
map     → 1 element → 1 element
flatMap → 1 element → MANY elements, all merged into one stream ⭐
```

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

#### The picture

```
[[1,2,3], [4,5], [6,7,8,9]]
        │
   .map(List::stream)         → Stream<Stream<Integer>>   ❌ still nested
   .flatMap(List::stream)     → Stream<Integer>           ✅ flattened

[1, 2, 3, 4, 5, 6, 7, 8, 9]
```

Real-world idea: you have **three boxes of chocolates**. `map` gives you three boxes; `flatMap` **tips them all onto one table** so you can see every chocolate at once.

### `map` vs `flatMap` — the question behind the question

| | `map` | `flatMap` |
|---|---|---|
| Transforms | one value → one value | one value → **a stream of values** |
| On `List<List<Integer>>` | `Stream<Stream<Integer>>` (still nested) | `Stream<Integer>` (flattened) |
| Think of it as | transform | transform **+ flatten** |

#### Real-world uses

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

#### Easy memory

```
map     → 1 → 1        → Stream<Stream<T>> if the element is a list ❌
flatMap → 1 → MANY     → Stream<T> ✅

flatMap(List::stream)                     ← flatten lists
flatMap(s -> Arrays.stream(s.split(" "))) ← sentences → words

Three boxes of chocolates → tip them all onto ONE table 🍫
```

---

## 🎁 Bonus problems that often follow

**Sum of even numbers:**
```java
int sum = nums.stream().filter(n -> n % 2 == 0).mapToInt(Integer::intValue).sum();
```

**First 5 elements / skip the first 5:**
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
```
m a d a m
↑       ↑   compare 0 and 4
  ↑   ↑     compare 1 and 3
    ↑       middle — no need to check ✅
```

**Count words in a sentence:**
```java
long words = Arrays.stream(sentence.split("\\s+")).filter(w -> !w.isBlank()).count();
```

**Squares of the first 10 numbers:**
```java
List<Integer> squares = IntStream.rangeClosed(1, 10).map(i -> i * i).boxed().toList();
```
```
range(1, 10)       → 1..9   (end EXCLUSIVE)
rangeClosed(1, 10) → 1..10  (end INCLUSIVE) ⭐
boxed()            → IntStream → Stream<Integer>
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
| Take / skip N | `limit(n)` / `skip(n)` |
| Count | `count()` |
| Sum / average | `mapToInt(...).sum()` / `.average()` |
| Max / min | `max(cmp)` / `min(cmp)` |
| All stats at once | `summaryStatistics()` ⭐ |
| Group | `groupingBy(fn)` |
| Group + aggregate | `groupingBy(fn, counting())` ⭐ |
| Split in two by predicate | `partitioningBy(pred)` |
| List → Map | `toMap(key, value, merge)` |
| Join strings | `joining(", ")` |
| Any / all / none match | `anyMatch` / `allMatch` / `noneMatch` |
| Get one element | `findFirst()` / `findAny()` |
| To List (immutable) | `.toList()` — Java 16+ |
| To List (mutable) | `collect(toList())` |
| To Set | `collect(toSet())` |
| To a specific collection | `collect(toCollection(TreeSet::new))` |
| Combine into one value | `reduce(identity, accumulator)` |
| Peek at values (debug only) | `peek` |

---

## ⚠️ The 12 traps to remember

```
1.  A stream is SINGLE-USE          → reusing it throws IllegalStateException
2.  toMap throws on DUPLICATE KEYS  → always consider a merge function
3.  toMap throws NPE on a null VALUE→ groupingBy doesn't
4.  stream().toList() is IMMUTABLE  → collect(toList()) is mutable
5.  allMatch on an EMPTY stream = TRUE  → vacuous truth ⭐
6.  distinct() on objects needs equals() + hashCode()
7.  sorted() returns a NEW stream   → list.sort() mutates in place
8.  findFirst vs findAny only differ on PARALLEL streams
9.  Don't mutate external state in a lambda → breaks with parallel streams
10. average() returns OptionalDouble → handle the empty case
11. Don't force a stream where a loop is clearer (StringBuilder.reverse beats reduce)
12. parallelStream() is NOT automatically faster → only for large + CPU-heavy work
```

---

## 🗣️ How to answer these in the room

```
1. RESTATE the problem and confirm edge cases out loud
      "Should this be case-sensitive? What if the list is empty?"

2. SAY YOUR APPROACH before you write
      "I'll group by the character and count each group."

3. WRITE it, then TRACE one example through the pipeline

4. VOLUNTEER the complexity
      "This is O(n), one pass."

5. OFFER the alternative
      "There's a non-stream version with a for loop and map.merge, if you'd prefer."
```

> **The most valuable habit:** name a trade-off unprompted.
>
> *"I'd use `StringBuilder` here rather than `reduce`, because string concatenation in a reduce is O(n²)."*
>
> That one sentence marks you as someone who has written production Java, not just practised puzzles.

---

### Related files
- [05-java.md](./05-java.md) — OOP, collections, HashMap internals, Java 8 basics
- [12-java17-features.md](./12-java17-features.md) — records, sealed classes, pattern matching
- [09-coding-problems.md](./09-coding-problems.md) — 35+ general DSA problems in JS and Java

> ⭐ **Before the round:** the one-page recall version of this file is **[42 — Java + Streams Final Memory Sheet](./42-java-streams-final-memory-sheet.md)** — all 32 problems, one line each, then the code for every one.
