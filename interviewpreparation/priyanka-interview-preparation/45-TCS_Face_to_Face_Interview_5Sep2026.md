# 🟦 TCS --- Java + Spring Face-to-Face Round (Sept 2026)

> 📅 **Attended: 5 Sept 2026**
>
> 📝 **Round focus:** Java 17, Java 8, Spring annotations, transactions,
> sync/async, collections, and one Java 8 Streams live-coding question.

------------------------------------------------------------------------

## 📋 Questions at a glance

  ----------------------------------------------------------------------------------------
  \#                      Question exactly as asked                Area
  ----------------------- ---------------------------------------- -----------------------
  1                       **What are the latest features we are    Java / Project
                          using?**                                 

  2                       **Java 17 features**                     Java

  3                       **Java 8 features**                      Java

  4                       **Component annotations**                Spring

  5                       **`@Transactional` annotation**          Spring

  6                       **Synchronous and asynchronous process** Java / Spring

  7                       **HashMap and LinkedHashSet**            Collections

  8                       🔴                                       Java Streams
                          `ArrayList = {"name", 1, "test", "2"}`   
                          --- print only numbers using Java 8      
                          Streams. Output: `1,2`                   
  ----------------------------------------------------------------------------------------

------------------------------------------------------------------------

# Part 1 --- Java

## 1. What are the latest features we are using?

**Interview-ready answer:**

> "We are using **Java 17**. I am familiar with features such as
> **Records, Pattern Matching for `instanceof`, Switch Expressions, Text
> Blocks and Sealed Classes**. Along with that, we use Java 8 features
> such as **Streams, Lambdas, Optional and method references**."

### 🧠 Easy memory

``` text
PROJECT → JAVA VERSION → FEATURES → USAGE
```

> ⚠️ Mention only features you actually use or can explain in a
> follow-up.

------------------------------------------------------------------------

## 2. Java 17 features

### The five to remember

``` text
R → Record
P → Pattern Matching
S → Switch Expression
T → Text Blocks
S → Sealed Classes
```

### Record

``` java
record Employee(String name, int age) {}
```

**Memory:** `Record = data + less boilerplate`

### Pattern Matching for `instanceof`

``` java
if (obj instanceof String str) {
    System.out.println(str.length());
}
```

**Memory:** `CHECK + CAST together`

### Switch Expression

``` java
String result = switch (day) {
    case 1 -> "Monday";
    case 2 -> "Tuesday";
    default -> "Unknown";
};
```

**Memory:** `Switch → returns a value`

### Text Blocks

Use `"""` for multiline strings.

**Memory:** `Text Block = multiline string`

### Sealed Classes

``` java
sealed class Payment permits CardPayment, UpiPayment {
}
```

**Memory:** `Sealed = control inheritance`

### 🧠 Java 17 memory box

``` text
Record → Less boilerplate
Pattern → Check + Cast
Switch → Return value
Text Block → Multiline
Sealed → Control inheritance
```

------------------------------------------------------------------------

## 3. Java 8 features

### 🧠 Easy memory --- Java 8 = `L.S.I.O.D.`

``` text
L → Lambda
S → Stream API
I → Interface default/static methods
O → Optional
D → Date/Time API
```

Also remember:

-   **Functional Interface** → one abstract method
-   **Method Reference** → `::`
-   **CompletableFuture** → asynchronous programming

### Interview-ready answer

> "The major Java 8 features I use are Lambda expressions, Stream API,
> Functional Interfaces, Optional, Method References, default/static
> methods in interfaces, and the Date/Time API."

------------------------------------------------------------------------

# Part 2 --- Spring

## 4. Component annotations

``` text
@Component
    |
    ├── @Controller
    ├── @Service
    └── @Repository
```

  Annotation          Purpose
  ------------------- ------------------------------
  `@Component`        General Spring-managed bean
  `@Controller`       MVC / web controller
  `@Service`          Business logic
  `@Repository`       Database / persistence layer
  `@RestController`   REST API controller

### Flow

``` text
REQUEST → CONTROLLER → SERVICE → REPOSITORY → DATABASE
```

### Important

``` text
@RestController = @Controller + @ResponseBody
```

### 🧠 Easy memory

**C → S → R → DB**

------------------------------------------------------------------------

## 5. `@Transactional` annotation

**One line:** `@Transactional` is used when multiple database operations
should execute as **one transaction**.

``` java
@Transactional
public void transfer() {
    debit();
    credit();
}
```

### Flow

``` text
BEGIN → DEBIT → CREDIT → SUCCESS?
                         YES → COMMIT
                         NO  → ROLLBACK
```

### Interview-ready answer

> "I use `@Transactional` at the service layer when multiple database
> operations need to execute as one unit. If everything succeeds, it
> commits. If an applicable exception occurs, it rolls back."

By default, Spring rolls back for unchecked exceptions such as
`RuntimeException` and `Error`.

``` java
@Transactional(rollbackFor = Exception.class)
```

### 🧠 Easy memory

**ALL SUCCESS → COMMIT \| FAILURE → ROLLBACK**

------------------------------------------------------------------------

## 6. Synchronous vs Asynchronous process

### Synchronous

``` text
Task A → WAIT → Task B → WAIT → Task C
```

**Memory:** `SYNC = WAIT`

### Asynchronous

``` text
MAIN PROCESS → Continue
             ↘ Async Task
```

Spring example:

``` java
@Async
public void sendEmail() {
    // send email
}
```

Enable async support:

``` java
@EnableAsync
```

Java 8 also provides `CompletableFuture`.

### 🧠 Easy memory

``` text
SYNC  = WAIT
ASYNC = DON'T WAIT FOR TASK COMPLETION
```

------------------------------------------------------------------------

# Part 3 --- Collections

## 7. HashMap vs LinkedHashSet

> ⚠️ `HashMap` is a **Map**. `LinkedHashSet` is a **Set**.

                `HashMap`                       `LinkedHashSet`
  ------------- ------------------------------- ---------------------------
  Stores        Key + Value                     Value only
  Uniqueness    Keys are unique                 Values are unique
  Order         No guaranteed iteration order   Maintains insertion order
  Main method   `put(key, value)`               `add(value)`
  Interface     `Map`                           `Set`

### HashMap

``` java
Map<Integer, String> map = new HashMap<>();
map.put(1, "Java");
map.put(2, "Spring");
```

**Memory:** `HashMap = KEY → VALUE`

### LinkedHashSet

``` java
Set<Integer> set = new LinkedHashSet<>();
set.add(20);
set.add(10);
set.add(20);
set.add(30);
```

Result:

``` text
20, 10, 30
```

**Memory:** `LinkedHashSet = UNIQUE + INSERTION ORDER`

> If the interviewer meant **HashMap vs LinkedHashMap**, both are Maps.
> `LinkedHashMap` maintains insertion/access order; `HashMap` does not
> guarantee iteration order.

------------------------------------------------------------------------

# Part 4 --- Live Coding

## 8. 🔴 Print only numbers using Java 8 Streams

### Question

``` java
List<Object> list = Arrays.asList("name", 1, "test", "2");
```

Expected:

``` text
[1, 2]
```

### Understand the input

``` text
"name" → String
1      → Integer
"test" → String
"2"    → String
```

Using only:

``` java
.filter(x -> x instanceof Integer)
```

would return only `1`, because `"2"` is a String.

## ✅ Recommended Java 8 Stream solution

``` java
import java.util.*;
import java.util.stream.*;

public class Main {
    public static void main(String[] args) {

        List<Object> list =
                Arrays.asList("name", 1, "test", "2");

        List<Integer> result = list.stream()
                .map(Object::toString)
                .filter(s -> s.chars().allMatch(Character::isDigit))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        System.out.println(result);
    }
}
```

### Output

``` text
[1, 2]
```

### Step-by-step

``` text
MIXED LIST
   ↓
TO STRING
   ↓
KEEP DIGITS
   ↓
PARSE INT
   ↓
COLLECT
   ↓
[1, 2]
```

  Stream step                       Meaning
  --------------------------------- -----------------------------------
  `.map(Object::toString)`          Convert every item to String
  `.filter(...isDigit)`             Keep only numeric values
  `.map(Integer::parseInt)`         Convert numeric String to Integer
  `.collect(Collectors.toList())`   Collect into a List

### 🧠 Easy memory box

**STRING → DIGIT → INT → COLLECT**

### What to say before coding

> "First I convert the mixed objects to strings, filter the values
> containing only digits, convert those strings to integers, and collect
> them into a list."

------------------------------------------------------------------------

# 🎯 TCS --- 1-Minute Revision

  Question                Remember
  ----------------------- -------------------------------------------------
  Latest features         Java 17 + features actually used/familiar with
  Java 17                 Record → Pattern → Switch → Text Block → Sealed
  Java 8                  L → S → I → O → D
  Component annotations   Controller → Service → Repository
  `@Transactional`        COMMIT / ROLLBACK
  Sync                    WAIT
  Async                   DON'T WAIT
  HashMap                 KEY → VALUE
  LinkedHashSet           UNIQUE + INSERTION ORDER
  Coding                  STRING → DIGIT → INT → COLLECT

## 🔴 Coding pattern to practice

``` text
map → filter → map → collect
```

Type the live-coding problem several times without looking at the
answer. Remember the **pattern**, not every character.
