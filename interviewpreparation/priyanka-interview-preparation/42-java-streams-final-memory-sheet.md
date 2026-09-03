# Java Coding + Streams — Final Easy-Memory Sheet

| # | Question | Logic Snippet | What to Think | 🧠 Easy Memory |
|---|---|---|---|---|
| 1 | Reverse String | `for(i=len-1; i>=0; i--)` | Read characters from last to first | **BACKWARD → ADD** |
| 2 | Reverse Words | `split(" ")` + backward loop | Split into words, read backwards | **SPLIT → BACKWARD → JOIN** |
| 3 | Palindrome | `str.equals(reverse)` | Reverse string and compare | **REVERSE → COMPARE** |
| 4 | Anagram | `chars().sorted().toArray()` | Sort both strings and compare | **SORT BOTH → EQUALS** |
| 5 | First Non-Repeating | `indexOf(c) == lastIndexOf(c)` | Same first/last index means appears once | **UNIQUE → FIRST** |
| 6 | First Non-Repeating from End | `reverse → filter → findFirst` | Reverse first, then find unique | **REVERSE → UNIQUE → FIRST** |
| 7 | Second Non-Repeating from End | `reverse → filter → skip(1) → findFirst` | Skip first unique character | **REVERSE → UNIQUE → SKIP 1 → FIRST** |
| 8 | Missing Number | `expected - actual` | Expected sum minus array sum | **EXPECTED − ACTUAL** |
| 9 | Second Largest | `distinct().sorted(reverse).skip(1)` | Remove duplicate → descending → skip highest | **DISTINCT → DESC → SKIP 1 → FIRST** |
| 10 | Find Duplicates | `filter(n -> !seen.add(n))` | `add()` false means already exists | **!seen.add = DUPLICATE** |
| 11 | Two Sum | `need = target - nums[i]` | Search previously seen need | **TARGET − CURRENT = NEED** |
| 12 | Character Frequency | `groupingBy(c -> c, counting())` | Put same characters together and count | **GROUP → COUNT** |
| 13 | Maximum / Minimum | `mapToInt().max()/min()` | Convert to numbers then max/min | **NUMBER → MAX/MIN** |
| 14 | Sort Objects | `sorted(Comparator.comparing(...))` | Tell Java which field to compare | **SORT → FIELD** |
| 15 | List → Map | `toMap(KEY, VALUE)` | Choose map key and value | **KEY → VALUE** |
| 16 | Sum / Average | `mapToInt().sum()/average()` | Convert to numeric stream and calculate | **NUMBER → SUM/AVG** |
| 17 | Element Frequency | `groupingBy(n -> n, counting())` | Same numbers together + count | **GROUP → COUNT** |
| 18 | Remove Duplicates | `distinct()` | Keep unique values | **REMOVE = DISTINCT** |
| 19 | Reverse Using Stream | `range → reverse index → joining` | Generate indexes and read backwards | **RANGE → BACKWARD → JOIN** |
| 20 | Group Objects | `groupingBy(Employee::getDepartment)` | Same department together | **GROUP BY FIELD** |
| 21 | Even / Odd | `partitioningBy(n -> n%2==0)` | Condition creates true/false groups | **2 GROUPS → PARTITION** |
| 22 | Longest String | `max(comparingInt(String::length))` | Compare strings by length | **MAX → LENGTH** |
| 23 | All Match | `allMatch(condition)` | Every element must pass | **ALL → allMatch** |
| 24 | Any Match | `anyMatch(condition)` | At least one must pass | **ANY → anyMatch** |
| 25 | None Match | `noneMatch(condition)` | No element should pass | **NONE → noneMatch** |
| 26 | Find First | `filter(...).findFirst()` | Filter then take first | **FILTER → FIRST** |
| 27 | Find Any | `filter(...).findAny()` | Filter then take any | **FILTER → ANY** |
| 28 | Stream → List | `collect(Collectors.toList())` | Collect result into List | **COLLECT → LIST** |
| 29 | Stream → Set | `collect(Collectors.toSet())` | Collect result into Set | **COLLECT → SET** |
| 30 | Join Strings | `Collectors.joining(", ")` | Put delimiter between strings | **STRINGS → JOIN** |
| 31 | Flatten Lists | `flatMap(Collection::stream)` | Open inner lists into one stream | **NESTED → FLAT** |
| 32 | Highest Paid / Department | `toMap(dept, employee, keepHigher)` | Same dept? Keep employee with higher salary | **DEPT → COMPARE → KEEP HIGHER** |

---

# The Logic Snippets You Actually Need to Remember

## 1. Reverse String — Normal

```java
String reverse = "";

for (int i = str.length() - 1; i >= 0; i--) {
    reverse += str.charAt(i);
}
```

Think:

```
LAST → BACKWARD → ADD
```

## 2. Reverse Words — Normal

```java
String[] words = str.split(" ");
String result = "";

for (int i = words.length - 1; i >= 0; i--) {
    result += words[i] + " ";
}
```

Think:

```
SPLIT
  ↓
GO BACKWARD
  ↓
ADD WORDS
```

## 3. Palindrome — Normal

Use the same reverse logic:

```java
String reverse = "";

for (int i = str.length() - 1; i >= 0; i--) {
    reverse += str.charAt(i);
}

boolean result = str.equals(reverse);
```

Think:

```
REVERSE → SAME?
            ↓
          TRUE
```

## 4. Anagram

```java
int[] a = str1.chars().sorted().toArray();
int[] b = str2.chars().sorted().toArray();

boolean result = Arrays.equals(a, b);
```

Think:

```
listen → sort → eilnst
silent → sort → eilnst
                  ↓
                SAME
```

🧠 **SORT BOTH → EQUALS**

## 5. First Non-Repeating

```java
Character result = str.chars()
    .mapToObj(c -> (char) c)
    .filter(c -> str.indexOf(c) == str.lastIndexOf(c))
    .findFirst()
    .orElse(null);
```

Important logic:

```java
str.indexOf(c) == str.lastIndexOf(c)
```

If first position and last position are the same, the character occurs only once.

🧠 **UNIQUE → FIRST**

## 6. First Non-Repeating from END

```java
String reverse =
    new StringBuilder(str).reverse().toString();

Character result = reverse.chars()
    .mapToObj(c -> (char) c)
    .filter(c -> str.indexOf(c) == str.lastIndexOf(c))
    .findFirst()
    .orElse(null);
```

🧠

```
REVERSE → UNIQUE → FIRST
```

## 7. Second Non-Repeating from END

Same program — add only:

```java
.skip(1)
```

```java
Character result = reverse.chars()
    .mapToObj(c -> (char) c)
    .filter(c -> str.indexOf(c) == str.lastIndexOf(c))
    .skip(1)
    .findFirst()
    .orElse(null);
```

🧠

```
FIRST  → findFirst
SECOND → skip(1) → findFirst
THIRD  → skip(2) → findFirst
```

Very useful connection.

## 8. Missing Number

```java
int n = nums.length;

int expected = n * (n + 1) / 2;
int actual = Arrays.stream(nums).sum();

int missing = expected - actual;
```

🧠

```
What SHOULD be there
        -
What IS there
        =
MISSING
```

## 9. Second Largest

```java
Integer result = list.stream()
    .distinct()
    .sorted(Comparator.reverseOrder())
    .skip(1)
    .findFirst()
    .orElse(null);
```

Think:

```
[10,40,20,40,30]

DISTINCT
↓
10,40,20,30

DESC
↓
40,30,20,10

SKIP 1
↓
30
```

🧠 **DISTINCT → DESC → SKIP 1 → FIRST**

## 10. Find Duplicates

```java
Set<Integer> seen = new HashSet<>();

Set<Integer> duplicate = list.stream()
    .filter(n -> !seen.add(n))
    .collect(Collectors.toSet());
```

Key:

```java
seen.add(n)
```

```
true  → first time
false → already exists
```

Therefore:

```java
!seen.add(n)
```

means duplicate.

🧠 **FALSE ADD = DUPLICATE**

## 11. Two Sum

```java
Map<Integer, Integer> map = new HashMap<>();

for (int i = 0; i < nums.length; i++) {

    int need = target - nums[i];

    if (map.containsKey(need))
        return new int[]{ map.get(need), i };

    map.put(nums[i], i);
}
```

Don't memorize the whole code first.

Memorize:

```
TARGET = 9

Current = 2
Need = 9 - 2 = 7

Current = 7
Need = 9 - 7 = 2

Have we already seen 2?
YES
→ ANSWER
```

🧠 **NEED = TARGET − CURRENT**

## 12 & 17 Are the SAME FAMILY

**Character Frequency**

```java
str.chars()
   .mapToObj(c -> (char) c)
   .collect(Collectors.groupingBy(
       c -> c,
       Collectors.counting()
   ));
```

**Number Frequency**

```java
list.stream()
    .collect(Collectors.groupingBy(
        n -> n,
        Collectors.counting()
    ));
```

Both are:

```
SAME VALUES TOGETHER
        ↓
      GROUP
        ↓
      COUNT
```

🧠 **FREQUENCY = GROUP + COUNT**

## 13 & 16 Are the SAME FAMILY ⭐

Start with:

```java
list.stream()
    .mapToInt(Integer::intValue)
```

Then choose:

```java
.sum()
```

or:

```java
.max().orElse(0)
```

or:

```java
.min().orElse(0)
```

or:

```java
.average().orElse(0)
```

🧠

```
             mapToInt
                |
      ┌─────────┼─────────┐
      ↓         ↓         ↓
     SUM       MAX       MIN
                \
               AVERAGE
```

So don't memorize four programs.

Remember:

```
NUMBER CALCULATION = mapToInt
```

## 14. Sort Objects

```java
employees.stream()
    .sorted(
        Comparator.comparing(Employee::getSalary)
    )
    .collect(Collectors.toList());
```

Change only the field:

```java
Employee::getSalary
Employee::getName
Employee::getAge
```

🧠 **SORT OBJECT → COMPARING(FIELD)**

## 15. List → Map

```java
employees.stream()
    .collect(Collectors.toMap(
        Employee::getId,
        Employee::getName
    ));
```

Think:

```
ID       NAME
↓         ↓
KEY     VALUE

1   →   Priya
2   →   Arun
```

🧠 **toMap(KEY, VALUE)**

## 18. Remove Duplicates

```java
list.stream()
    .distinct()
    .collect(Collectors.toList());
```

Don't confuse:

```
FIND duplicates
→ !seen.add()

REMOVE duplicates
→ distinct()
```

This distinction is worth memorizing.

## 19. Reverse String Using Stream

```java
String result = IntStream.range(0, str.length())
    .mapToObj(i -> str.charAt(str.length() - 1 - i))
    .map(String::valueOf)
    .collect(Collectors.joining());
```

Key:

```java
str.length() - 1 - i
```

means read from the end.

🧠 **RANGE → BACKWARD → JOIN**

## 20. Group Objects

```java
employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment
    ));
```

Result:

```
IT    → [Priya, Divya]
HR    → [Arun]
Sales → [Karthik]
```

🧠 **GROUP OBJECT → groupingBy(FIELD)**

## 21. Even / Odd

```java
list.stream()
    .collect(Collectors.partitioningBy(
        n -> n % 2 == 0
    ));
```

Think:

```
Condition
n % 2 == 0

       ↓

TRUE          FALSE
 ↓              ↓
EVEN           ODD
```

🧠 **TWO GROUPS → partitioningBy**

## 22. Longest String

```java
list.stream()
    .max(
        Comparator.comparingInt(String::length)
    )
    .orElse(null);
```

Think:

```
Java       → 4
Angular    → 7
SpringBoot → 10
              ↑
             MAX
```

🧠 **MAX → LENGTH**

## 23, 24, 25 — Remember Together

```java
list.stream().allMatch(n -> n > 5);
list.stream().anyMatch(n -> n > 5);
list.stream().noneMatch(n -> n > 5);
```

🧠

```
ALL?  → allMatch
ANY?  → anyMatch
NONE? → noneMatch
```

That's one family, not three programs.

## 26 & 27 — Remember Together

First:

```java
list.stream()
    .filter(n -> n > 20)
    .findFirst()
    .orElse(null);
```

Any:

```java
list.stream()
    .filter(n -> n > 20)
    .findAny()
    .orElse(null);
```

🧠

```
CONDITION
   ↓
FILTER
   ↓
FIRST / ANY
```

## 28 & 29 — Remember Together

List:

```java
stream.collect(Collectors.toList());
```

Set:

```java
stream.collect(Collectors.toSet());
```

🧠

```
Want LIST? → toList
Want SET?  → toSet
```

## 30. Join Strings

```java
list.stream()
    .collect(Collectors.joining(", "));
```

```
Java
Spring
Angular

   ↓ joining(", ")

Java, Spring, Angular
```

🧠 **STRINGS → JOINING**

## 31. Flatten List of Lists

```java
list.stream()
    .flatMap(Collection::stream)
    .collect(Collectors.toList());
```

Think:

```
[[1,2], [3,4], [5,6]]

          ↓ flatMap

[1,2,3,4,5,6]
```

🧠 **NESTED → OPEN → ONE LIST**

## 32. Highest-Paid Employee per Department ⭐

Use the simpler `toMap` version:

```java
Map<String, Employee> result = employees.stream()
    .collect(Collectors.toMap(
        Employee::getDepartment,
        e -> e,
        (e1, e2) ->
            e1.getSalary() > e2.getSalary() ? e1 : e2
    ));
```

Don't look at this as one big code.

Break it into 3 things:

```java
Employee::getDepartment
```

means:

```
KEY = Department
```

Then:

```java
e -> e
```

means:

```
VALUE = Employee
```

Then:

```java
(e1, e2) ->
    e1.getSalary() > e2.getSalary() ? e1 : e2
```

means:

```
Same department?
       ↓
Compare both salaries
       ↓
Keep higher employee
```

Example:

```
Priya → IT → 75,000
Divya → IT → 85,000

Same key = IT
     ↓
Priya vs Divya
     ↓
85,000 is higher
     ↓
IT → Divya
```

🧠 **DEPT → EMPLOYEE → COMPARE → KEEP HIGHER**

---

# ⭐ FINAL MEMORY — Memorize Only This

Before the interview, revise this block:

```
Reverse String
→ BACKWARD → ADD

Reverse Words
→ SPLIT → BACKWARD → JOIN

Palindrome
→ REVERSE → COMPARE

Anagram
→ SORT BOTH → EQUALS

First Non-Repeat
→ UNIQUE → FIRST

From End
→ REVERSE → UNIQUE → FIRST

Second From End
→ REVERSE → UNIQUE → SKIP 1 → FIRST

Missing Number
→ EXPECTED - ACTUAL

Second Highest
→ DISTINCT → DESC → SKIP 1 → FIRST

Find Duplicate
→ !seen.add()

Two Sum
→ TARGET - CURRENT = NEED

Frequency
→ GROUP + COUNT

Max/Min/Sum/Avg
→ mapToInt

Sort Object
→ comparing(FIELD)

List → Map
→ toMap(KEY, VALUE)

Remove Duplicate
→ distinct()

Group Object
→ groupingBy(FIELD)

Even/Odd
→ partitioningBy

Longest String
→ MAX → LENGTH

Condition
→ allMatch / anyMatch / noneMatch

Find
→ FILTER → FIRST / ANY

Stream → Collection
→ toList / toSet

Join Strings
→ joining

Nested Lists
→ flatMap

Highest Salary / Dept
→ DEPT → EMPLOYEE → COMPARE → KEEP HIGHER
```

---

# The most important connections

Instead of 32 questions, mentally reduce them to about 9 families:

```
STRING     → Reverse / Palindrome / Anagram
FIND       → filter / findFirst
UNIQUE     → distinct / seen.add
NUMBER     → mapToInt
SORT       → sorted / Comparator
GROUP      → groupingBy / counting
MAP        → toMap
CONDITION  → match / partition
NESTED     → flatMap
```

---

**Related files:** [22 — Java Streams: 20 Coding Problems](./22-java-streams-coding-problems.md) · [41 — Capgemini](./41-capgemini-java-angular-3sep.md) · [37 — Altimetrik](./37-altimetrik-fullstack-java-angular-31aug.md) · [09 — Coding Problems](./09-coding-problems.md)
