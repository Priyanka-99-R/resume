# Java "Predict the Output" — 60 Tricky Programs with Answers

> 🔴 **Why this file exists:** in your last interview you were asked **output-based Java programs**, not theory. Two of them are broken down in full at the top of this file (Q1 and Q2). Everything after that is the same *style* of question, grouped by the trick being tested.

**How interviewers use these:** they don't want the answer only — they want *"why"*. For every program below, the format is:

1. **The code**
2. **The output** (or *compile error*)
3. **The rule** — the one sentence that makes it obvious
4. **The follow-up** they ask next

> 💡 **Golden habit:** before you answer any output question, ask yourself in this order —
> **(a) does it even compile?** → **(b) is anything resolved at *compile* time instead of runtime?** → **(c) then trace the runtime.**
> Most "tricky" Java questions are won at step (a) or (b). Candidates lose because they jump straight to (c).

---

## Easy memory — the 6 rules that decide 90% of these programs

Before you trace a single line, run through these. Most "tricky" programs are decided by one of them.

```
1. OVERLOADING is decided at COMPILE time by the REFERENCE type
   OVERRIDING  is decided at RUNTIME   by the OBJECT type              ⭐
      → methodTest(null) picks the MOST SPECIFIC type (String beats Object)

2. FIELDS and STATIC methods are NOT polymorphic — they use the REFERENCE type
   Only INSTANCE methods are polymorphic                                ⭐
      → Parent p = new Child();  p.field → PARENT's | p.method() → CHILD's

3. An OVERRIDE may not: widen `throws`, reduce visibility, or change the signature

4. String LITERALS are POOLED; `new String()` never is
   Compile-time constant folding ("hel" + "lo") is pooled;
   runtime concatenation is not                                         ⭐

5. Integer CACHE is -128..127. Mixing a wrapper with a primitive UNBOXES.
   Unboxing a null → NullPointerException                               ⭐

6. `finally` ALWAYS runs, and a `return` inside it WINS
   (and silently swallows the exception)                                ⭐
```

**The order to think in:**

```
(a) Does it even COMPILE?           ← most questions are won here
        ↓ yes
(b) Is anything resolved at COMPILE time rather than runtime?
        ↓ no
(c) NOW trace the runtime.
```

Candidates lose marks by jumping straight to (c).

---

## Table of contents

| # | Section | What it tests |
|---|---|---|
| [A](#a--the-two-questions-you-were-actually-asked) | **The 2 questions you were asked** | Overloading with `null`, overriding + checked exceptions |
| [B](#b--method-overloading-traps) | Overloading traps (Q3–Q10) | Compile-time resolution, most-specific rule, widening vs boxing vs varargs |
| [C](#c--overriding--polymorphism-traps) | Overriding & polymorphism (Q11–Q19) | Static binding vs dynamic binding, field hiding, covariant returns |
| [D](#d--initialization-order-traps) | Initialization order (Q20–Q23) | static block → instance block → constructor |
| [E](#e--string-traps) | String traps (Q24–Q30) | String pool, `==` vs `equals`, immutability, `intern()` |
| [F](#f--autoboxing--integer-cache-traps) | Autoboxing & Integer cache (Q31–Q36) | `-128..127` cache, unboxing NPE, ternary promotion |
| [G](#g--operator--numeric-traps) | Operators & numbers (Q37–Q44) | `i = i++`, integer division, float precision, compound assignment |
| [H](#h--trycatchfinally-traps) | try/catch/finally (Q45–Q50) | `finally` beating `return`, try-with-resources order |
| [I](#i--collections-traps) | Collections (Q51–Q57) | `remove(int)` vs `remove(Object)`, CME, `equals`/`hashCode` |
| [J](#j--static--thread-traps) | Static & threads (Q58–Q60) | Static on `null`, `run()` vs `start()` |
| [K](#k--rapid-fire-answer-table) | Rapid-fire table | 1-line answers for last-minute revision |
| [L](#l--20-self-test-drills-answers-hidden) | 20 self-test drills | Answers hidden — test yourself |

---

# A — The two questions you were actually asked

## Q1. Method overloading with `null`

```java
public class MethodOverloadingExample {

    public void methodTest(Object object) {
        System.out.println("Calling object method");
    }

    public void methodTest(String object) {
        System.out.println("Calling String method");
    }

    public static void main(String args[]) {
        MethodOverloadingExample moe = new MethodOverloadingExample();
        moe.methodTest(null);
    }
}
```

### ✅ Output

```
Calling String method
```

### The rule (say this exact sentence)

> **"Method overloading is resolved at *compile* time, and the compiler picks the *most specific* applicable method. `null` is assignable to both `Object` and `String`, but `String` is a subtype of `Object`, so `String` is more specific — so `methodTest(String)` wins."**

### Why "most specific" means `String`

`String` **IS-A** `Object`. So anything you can pass to `methodTest(String)` you could also pass to `methodTest(Object)` — but not the other way round. When one candidate can be forwarded to the other, the compiler picks the one lower in the hierarchy.

```
        Object          ← less specific (loses)
           ▲
           │
        String          ← more specific (WINS)
           ▲
           │
         null           ← null fits every reference type
```

### 🔥 The 4 follow-ups they ask next

**Follow-up 1 — "How do you force the `Object` version?"**
```java
moe.methodTest((Object) null);      // Calling object method
Object o = null;
moe.methodTest(o);                  // Calling object method
```
> The cast changes the **compile-time type** of the argument, which is the only thing overload resolution looks at.

**Follow-up 2 — "What if the two overloads were `String` and `Integer` instead?"**
```java
public void methodTest(Integer i) { }
public void methodTest(String s)  { }

moe.methodTest(null);   // ❌ COMPILE ERROR
// error: reference to methodTest is ambiguous
//        both method methodTest(Integer) and method methodTest(String) match
```
> **`String` and `Integer` are siblings** — neither is a subtype of the other, so there is no "most specific" one. Ambiguous → **compile error**, not a runtime error. Fix it with a cast: `moe.methodTest((String) null);`

**Follow-up 3 — "And with three overloads: `Object`, `String`, `StringBuilder`?"**
```java
public void methodTest(Object o)        { }
public void methodTest(String s)        { }
public void methodTest(StringBuilder s) { }

moe.methodTest(null);   // ❌ COMPILE ERROR — ambiguous
```
> `Object` is eliminated (both others beat it), but `String` vs `StringBuilder` are siblings → ambiguous.

**Follow-up 4 — "Is `String.valueOf(null)` the same trap?"**
```java
System.out.println(String.valueOf(null));   // ❌ throws NullPointerException at runtime
```
> Yes — the *same* rule bites you in the JDK. `String.valueOf` is overloaded for `Object`, `char[]`, `int`, … . `char[]` is more specific than `Object`, so the compiler picks **`valueOf(char[])`**, which does `new String(null)` → **NPE**.
> ```java
> System.out.println(String.valueOf((Object) null));   // prints "null" — safe
> ```
> Dropping this line in the interview makes you look like you've actually hit the bug in real code.

---

## Q2. Overriding + checked exception

```java
class SuperClass {
    void method() {
        System.out.println("SuperClass");
    }
}

class SubClass extends SuperClass {
    void method() throws IOException {      // ⚠️ broader checked exception
        System.out.println("SubClass");
    }

    public static void main(String args[]) {
        SuperClass s = new SubClass();
        s.method();
    }
}
```

### ✅ Output

```
❌ COMPILE ERROR

error: method() in SubClass cannot override method() in SuperClass
       overridden method does not throw java.io.IOException
```

> ⚠️ Note there are also **two typos** planted in the original snippet — `newSubClass()` should be `new SubClass()`, and `IOException` needs `import java.io.IOException;`. But even after fixing both, **it still does not compile**, and that is the point of the question.

### The rule (say this exact sentence)

> **"An overriding method may throw fewer or narrower checked exceptions than the method it overrides — never new or broader ones. It may throw any unchecked exception freely."**

### Why the language does this

Polymorphism must stay safe. The caller only sees the **compile-time type**:

```java
SuperClass s = new SubClass();
s.method();          // compiler checks SuperClass.method() → declares NO exceptions
                     // → caller writes no try/catch
                     // → but at runtime SubClass.method() could throw IOException
                     // → a checked exception would escape completely unhandled 💥
```
The compiler blocks this at the source: **you cannot widen the `throws` clause.**

### The complete legal / illegal table — memorise this

Super declares: `void method() throws IOException`

| Sub declares | Legal? | Why |
|---|---|---|
| `throws IOException` | ✅ | Same exception |
| `throws FileNotFoundException` | ✅ | **Narrower** (subclass of `IOException`) |
| *(no `throws` at all)* | ✅ | **Fewer** — always allowed |
| `throws IOException, SQLException` | ❌ | `SQLException` is **new** and checked |
| `throws Exception` | ❌ | **Broader** than `IOException` |
| `throws RuntimeException` | ✅ | **Unchecked** — never restricted |
| `throws Error` / `throws Throwable` | ✅ / ❌ | `Error` unchecked ✅ ; `Throwable` broader-checked ❌ |

Super declares: `void method()` *(nothing)*

| Sub declares | Legal? |
|---|---|
| *(nothing)* | ✅ |
| `throws IOException` | ❌ ← **your question** |
| `throws NullPointerException` | ✅ (unchecked) |
| `throws IllegalArgumentException` | ✅ (unchecked) |

### 🔥 The 3 follow-ups they ask next

**Follow-up 1 — "Make it compile."**
Three valid fixes; say the first one, then mention the other two:
```java
// Fix A — remove the throws from the subclass (best: super's contract is honoured)
class SubClass extends SuperClass {
    void method() { System.out.println("SubClass"); }
}

// Fix B — declare it in the SUPERCLASS too (widen the contract at the top)
class SuperClass {
    void method() throws IOException { System.out.println("SuperClass"); }
}
// ...then main must handle it:
public static void main(String[] args) throws IOException {
    SuperClass s = new SubClass();
    s.method();                      // prints "SubClass"
}

// Fix C — throw an UNCHECKED exception instead
class SubClass extends SuperClass {
    void method() throws RuntimeException { System.out.println("SubClass"); }
}
```

**Follow-up 2 — "If it did compile, what would print?"**
```java
SuperClass s = new SubClass();
s.method();       // "SubClass"
```
> **Overriding is resolved at runtime (dynamic dispatch)** by the *object's actual type*, not the reference type. This is the exact opposite of Q1 — say that contrast out loud, it's the connection between the two questions.

**Follow-up 3 — "Reverse it: super throws `IOException`, sub throws nothing. What does the caller do?"**
```java
class SuperClass { void method() throws IOException { } }
class SubClass extends SuperClass { void method() { } }

SuperClass s = new SubClass();
s.method();         // ❌ compile error at the CALL SITE: unhandled IOException
```
> The caller is checked against the **reference type** `SuperClass`, which declares `throws IOException` — so `try/catch` (or `throws`) is still required even though the real object never throws.

---

## 🎯 The one slide that answers both questions

| | **Q1 — Overloading** | **Q2 — Overriding** |
|---|---|---|
| Also called | Compile-time / **static** polymorphism | Runtime / **dynamic** polymorphism |
| Decided by | The **reference/declared type** of the argument | The **actual object** on the heap |
| Decided when | **Compile** time | **Run** time |
| Signature rule | Parameters must **differ** | Signature must be **identical** (or covariant return) |
| `throws` rule | No restriction | Cannot **widen** checked exceptions |
| Access modifier | No restriction | Cannot **reduce** visibility |
| `static` / `private` / `final` | Can be overloaded | **Cannot be overridden** |

> Memorise this line: **"Overload = compiler decides by the reference type. Override = JVM decides by the object type."** It answers roughly half of all Java output questions.

---

# B — Method overloading traps

## Q3. Widening vs autoboxing vs varargs

```java
public class Test {
    static void show(int x)      { System.out.println("int"); }
    static void show(long x)     { System.out.println("long"); }
    static void show(Integer x)  { System.out.println("Integer"); }
    static void show(int... x)   { System.out.println("varargs"); }

    public static void main(String[] a) {
        show(5);
    }
}
```

**✅ Output:** `int`

**The rule — the compiler tries three phases, in this order:**

```
Phase 1: exact match / WIDENING      (int → long → float → double)
Phase 2: AUTOBOXING                  (int → Integer → Object)
Phase 3: VARARGS                     (int → int...)
```

**"Widening beats boxing beats varargs."** It stops at the first phase that finds a match.

**Trace it:** delete the methods one at a time and the answer walks down the list:

| Methods present | Output |
|---|---|
| all four | `int` (exact) |
| remove `int` | `long` (widening) |
| remove `int`, `long` | `Integer` (boxing) |
| remove `int`, `long`, `Integer` | `varargs` (last resort) |

> **Follow-up: "why is widening preferred over boxing?"** — Backward compatibility. Varargs and autoboxing arrived in Java 5; code written before that had to keep behaving identically, so the old resolution rules run first.

---

## Q4. `Integer` vs `long` — the trap in the trap

```java
static void show(long x)    { System.out.println("long"); }
static void show(Integer x) { System.out.println("Integer"); }

show(5);
```

**✅ Output:** `long`

> Widening (`int` → `long`) is **phase 1**; boxing (`int` → `Integer`) is **phase 2**. Phase 1 wins. Candidates almost always guess `Integer` because it "looks closer" — it isn't.

**But reverse it:**
```java
static void show(long x)    { System.out.println("long"); }
static void show(Integer x) { System.out.println("Integer"); }

Integer i = 5;
show(i);        // "Integer" — an Integer cannot widen to long, only unbox then widen (2 steps, not allowed)
```

---

## Q5. `char` in overloading

```java
static void show(int x)    { System.out.println("int"); }
static void show(double x) { System.out.println("double"); }

show('a');
```

**✅ Output:** `int`

> `char` widens along `char → int → long → float → double`. `int` is the **nearest** widening target, so it wins.

**If you delete `show(int)`:** output becomes `double` (still legal widening).
**If only `show(Character)` and `show(int)` exist:** `int` wins (widening beats boxing again).

---

## Q6. Overloading is NOT decided by the object's real type

```java
class Parent { }
class Child extends Parent { }

public class Test {
    static void show(Parent p) { System.out.println("Parent version"); }
    static void show(Child c)  { System.out.println("Child version"); }

    public static void main(String[] a) {
        Parent p = new Child();      // reference type Parent, object type Child
        show(p);
    }
}
```

**✅ Output:** `Parent version`

**The rule:** overload resolution uses the **declared (compile-time) type** of the argument — `Parent`. The fact that the object is really a `Child` is irrelevant. *This is the single most important overloading fact, and it's the mirror image of Q1.*

```java
show((Child) p);       // "Child version" — cast changes the compile-time type
show(new Child());     // "Child version"
```

---

## Q7. Ambiguous `null` — sibling types

```java
static void show(String s)        { System.out.println("String"); }
static void show(StringBuilder b) { System.out.println("StringBuilder"); }

show(null);
```

**✅ Output:** ❌ **Compile error** — `reference to show is ambiguous`

> Neither type is a subtype of the other → no "most specific" candidate. **Fix:** `show((String) null);`

---

## Q8. Two varargs methods

```java
static void show(int... x)    { System.out.println("int varargs"); }
static void show(Object... x) { System.out.println("Object varargs"); }

show(1, 2);
```

**✅ Output:** `int varargs`

> Within the varargs phase, the most specific applicable one still wins. `show()` with **no arguments** would be ambiguous, and `show(1, "a")` picks `Object...`.

---

## Q9. Overloading on return type only

```java
static int  show() { return 1; }
static long show() { return 1L; }
```

**✅ Output:** ❌ **Compile error** — `method show() is already defined`

**The rule:** **return type is not part of the method signature.** Overloading requires a different *parameter list* (number, types, or order). This also means you can't overload by changing only `throws` or only the access modifier.

---

## Q10. Overloaded `println` and `char` arithmetic

```java
char a = 'A';
System.out.println(a);          // ?
System.out.println(a + 1);      // ?
System.out.println((char)(a + 1)); // ?
System.out.println("" + a + 1); // ?
```

**✅ Output:**
```
A
66
B
A1
```

> Line 1 picks `println(char)`. Line 2: `a + 1` promotes `char` to `int` → `66` → picks `println(int)`. Line 3: the cast forces `println(char)`. Line 4: string concatenation, no promotion.

---

# C — Overriding & polymorphism traps

## Q11. Static methods are **hidden**, not overridden

```java
class Parent {
    static void show() { System.out.println("Parent static"); }
}
class Child extends Parent {
    static void show() { System.out.println("Child static"); }
}

public class Test {
    public static void main(String[] a) {
        Parent p = new Child();
        p.show();
    }
}
```

**✅ Output:** `Parent static`

**The rule:** **static methods are bound at compile time by the reference type.** This is *method hiding*, not overriding — there is no dynamic dispatch for statics. Adding `@Override` to `Child.show()` is a **compile error**.

> **Follow-up: "how do you prove it's not overriding?"** — `p.show()` compiles down to a call on `Parent`. Also, the IDE warns *"static method should be accessed in a static way"* — you should really write `Parent.show()`.

---

## Q12. Fields are **hidden**, never polymorphic

```java
class Parent {
    String name = "Parent";
    String getName() { return name; }
}
class Child extends Parent {
    String name = "Child";
    String getName() { return name; }
}

public class Test {
    public static void main(String[] a) {
        Parent p = new Child();
        System.out.println(p.name);        // ?
        System.out.println(p.getName());   // ?
    }
}
```

**✅ Output:**
```
Parent
Child
```

**The rule:** **Variables are resolved at compile time by the reference type; methods are resolved at runtime by the object type.** A `Child` object physically contains *both* `name` fields; `p.name` reads the `Parent` one because `p` is declared as `Parent`.

> This is a favourite because it puts both rules in one 6-line program.

---

## Q13. Constructor calls an overridden method

```java
class Parent {
    Parent() {
        System.out.println("Parent constructor");
        print();
    }
    void print() { System.out.println("Parent print"); }
}

class Child extends Parent {
    int x = 5;

    Child() {
        System.out.println("Child constructor");
    }

    @Override
    void print() { System.out.println("Child print, x = " + x); }
}

new Child();
```

**✅ Output:**
```
Parent constructor
Child print, x = 0        ← ⚠️ x is 0, not 5!
Child constructor
```

**The rule:** the `Parent` constructor runs **before** `Child`'s field initialisers. `print()` dispatches dynamically to `Child.print()`, but `x` still holds its **default value `0`** because `int x = 5;` hasn't executed yet.

> **This is a real bug class** — the reason the rule *"never call an overridable method from a constructor"* exists (Effective Java, Item 19). Mentioning that book by name lands well.
>
> **Fix:** make `print()` `private`, `static`, or `final`, or move the call into a separate `init()` method the caller invokes after construction.

---

## Q14. Covariant return types

```java
class Parent {
    Object get() { return "parent"; }
}
class Child extends Parent {
    @Override
    String get() { return "child"; }        // narrower return type
}

Parent p = new Child();
System.out.println(p.get());
```

**✅ Output:** `child` — and **it compiles**.

**The rule:** since Java 5, an overriding method may return a **subtype** of the original return type (*covariant return*). `String` IS-A `Object`, so it's fine.

**But the reverse fails:**
```java
class Child extends Parent {
    Number get() { return 1; }     // ❌ compile error — Number is not a subtype of String
}
```
And **primitives have no covariance**: `int` overriding `long` is a compile error.

---

## Q15. Overriding cannot reduce visibility

```java
class Parent {
    public void show() { System.out.println("Parent"); }
}
class Child extends Parent {
    protected void show() { System.out.println("Child"); }
}
```

**✅ Output:** ❌ **Compile error** — `attempting to assign weaker access privileges; was public`

**The rule:** an override may **widen** access (`protected` → `public`) but never **narrow** it. Otherwise `((Parent) child).show()` would call a method you're not allowed to call.

**Legal direction:**
```java
class Parent { protected void show() { } }
class Child  { public    void show() { } }    // ✅ widening is fine
```

---

## Q16. `private` methods are not overridden

```java
class Parent {
    private void show() { System.out.println("Parent"); }
    void call() { show(); }
}
class Child extends Parent {
    void show() { System.out.println("Child"); }     // NOT an override — a brand new method
}

Parent p = new Child();
p.call();
```

**✅ Output:** `Parent`

**The rule:** `private` methods are **not inherited and not virtual** — `Parent.call()` is bound to `Parent.show()` at compile time. Putting `@Override` on `Child.show()` is a compile error.

> Same for `final` (cannot be overridden at all) and `static` (hidden — see Q11).

---

## Q17. `final` method

```java
class Parent { final void show() { } }
class Child extends Parent { void show() { } }
```

**✅ Output:** ❌ **Compile error** — `show() in Child cannot override show() in Parent; overridden method is final`

---

## Q18. Upcast / downcast and `ClassCastException`

```java
class Parent { }
class Child extends Parent { void childOnly() { } }

Parent p = new Parent();
Child c = (Child) p;         // compiles fine!
```

**✅ Output:** `Exception in thread "main" java.lang.ClassCastException: class Parent cannot be cast to class Child`

**The rule:** a **downcast compiles** (the compiler allows it because it *might* be valid) but is checked at runtime. Guard with `instanceof`:
```java
if (p instanceof Child c) {       // Java 16+ pattern matching — you're on Java 17 ✅
    c.childOnly();
}
```

---

## Q19. Interface default methods — the diamond

```java
interface A { default void show() { System.out.println("A"); } }
interface B { default void show() { System.out.println("B"); } }

class C implements A, B { }
```

**✅ Output:** ❌ **Compile error** — `class C inherits unrelated defaults for show() from types A and B`

**Fix — you must override and disambiguate:**
```java
class C implements A, B {
    @Override
    public void show() {
        A.super.show();      // explicitly choose A → prints "A"
    }
}
```

> `A.super.show()` is the special syntax for "call interface A's default method". Knowing that one token is a strong signal.

---

# D — Initialization order traps

## Q20. The full initialization order

```java
class Parent {
    static { System.out.println("1. Parent static block"); }
    { System.out.println("3. Parent instance block"); }
    Parent() { System.out.println("4. Parent constructor"); }
}

class Child extends Parent {
    static { System.out.println("2. Child static block"); }
    { System.out.println("5. Child instance block"); }
    Child() { System.out.println("6. Child constructor"); }
}

public class Test {
    public static void main(String[] a) {
        new Child();
        System.out.println("---");
        new Child();
    }
}
```

**✅ Output:**
```
1. Parent static block
2. Child static block
3. Parent instance block
4. Parent constructor
5. Child instance block
6. Child constructor
---
3. Parent instance block
4. Parent constructor
5. Child instance block
6. Child constructor
```

**The rule — memorise this order:**

```
ONCE per class, at class-load time:
  1. Parent static blocks + static fields   (top to bottom)
  2. Child  static blocks + static fields

EVERY time you call new:
  3. Parent instance blocks + instance fields
  4. Parent constructor body
  5. Child  instance blocks + instance fields
  6. Child  constructor body
```

> **Static blocks run only once** — note they don't reappear for the second `new Child()`. **Instance blocks run before the constructor body**, and are copied into *every* constructor.

---

## Q21. Instance block vs field initializer

```java
class Test {
    int x = printAndReturn("field initializer", 1);
    { printAndReturn("instance block", 2); }
    int y = printAndReturn("field after block", 3);

    Test() { System.out.println("constructor"); }

    static int printAndReturn(String s, int v) { System.out.println(s); return v; }

    public static void main(String[] a) { new Test(); }
}
```

**✅ Output:**
```
field initializer
instance block
field after block
constructor
```

**The rule:** field initialisers and instance blocks run **in the order they appear in the source**, all before the constructor **body**.

---

## Q22. `this()` and `super()`

```java
class Test {
    Test()        { this(10); System.out.println("no-arg"); }
    Test(int x)   { System.out.println("int-arg " + x); }

    public static void main(String[] a) { new Test(); }
}
```

**✅ Output:**
```
int-arg 10
no-arg
```

> **`this()` / `super()` must be the FIRST statement** in a constructor, and you can't have both. If you write neither, the compiler inserts an implicit `super();`.

**The classic compile error:**
```java
class Parent { Parent(int x) { } }         // no no-arg constructor!
class Child extends Parent { Child() { } } // ❌ error: constructor Parent in class Parent
                                           //    cannot be applied to given types
```

---

## Q23. Static initializer and `final` constants

```java
class Test {
    static final int A = 10;              // compile-time constant
    static int B = init();
    static { System.out.println("static block, B = " + B); }

    static int init() { System.out.println("init() called"); return 20; }

    public static void main(String[] a) { System.out.println("A = " + A + ", B = " + B); }
}
```

**✅ Output:**
```
init() called
static block, B = 20
A = 10, B = 20
```

> `static final int A = 10` is a **compile-time constant** — it's inlined at every use site and doesn't even trigger class loading. `B` needs a method call, so it initialises in source order, before the static block.

---

# E — String traps

## Q24. `==` vs `equals` and the string pool

```java
String s1 = "hello";
String s2 = "hello";
String s3 = new String("hello");
String s4 = s3.intern();

System.out.println(s1 == s2);        // ?
System.out.println(s1 == s3);        // ?
System.out.println(s1.equals(s3));   // ?
System.out.println(s1 == s4);        // ?
```

**✅ Output:**
```
true
false
true
true
```

**The rule:** string **literals** are interned in the **String Constant Pool** — identical literals share one object. `new String(...)` **always** allocates a fresh heap object. `intern()` returns the pooled instance.

```
   String Pool (in heap since Java 7)          Heap
   ┌──────────────────┐                 ┌──────────────────┐
   │  "hello"  ◄──────┼── s1, s2, s4    │  "hello"  ◄──────┼── s3
   └──────────────────┘                 └──────────────────┘
```

---

## Q25. Compile-time constant concatenation — the `final` twist

```java
String s1 = "hello";
String s2 = "hel" + "lo";                 // ?

String part = "hel";
String s3 = part + "lo";                  // ?

final String fpart = "hel";
String s4 = fpart + "lo";                 // ?

System.out.println(s1 == s2);
System.out.println(s1 == s3);
System.out.println(s1 == s4);
```

**✅ Output:**
```
true
false
true
```

**The rule:**
- `"hel" + "lo"` — both operands are literals → the **compiler folds it into `"hello"`** → pooled → `true`.
- `part + "lo"` — `part` is a variable, so concatenation happens **at runtime** (via `StringBuilder`/`invokedynamic`) → new heap object → `false`.
- `final String fpart = "hel"` — a **compile-time constant**, so the compiler folds it just like a literal → `true`.

> That third case is the one that separates people. The magic word is **"compile-time constant expression"**.

---

## Q26. Strings are immutable

```java
String s = "hello";
s.toUpperCase();
s.concat(" world");
s.replace('h', 'H');
System.out.println(s);
```

**✅ Output:** `hello`

**The rule:** every `String` method **returns a new String** — it never mutates the receiver. You must reassign:
```java
s = s.toUpperCase();     // HELLO
```

---

## Q27. `StringBuilder` has no `equals`

```java
StringBuilder a = new StringBuilder("abc");
StringBuilder b = new StringBuilder("abc");

System.out.println(a == b);                          // ?
System.out.println(a.equals(b));                     // ?
System.out.println(a.toString().equals(b.toString())); // ?
```

**✅ Output:**
```
false
false
true
```

> **`StringBuilder` does NOT override `equals()`** — it inherits `Object.equals`, which is reference equality. Always compare via `.toString()` (or `compareTo`, added in Java 11).

---

## Q28. `switch` on String and `null`

```java
String s = null;
switch (s) {
    case "a": System.out.println("A"); break;
    default:  System.out.println("default");
}
```

**✅ Output:** `NullPointerException`

> `switch` on a `String` internally calls `s.hashCode()`. **`default` does not catch `null`** — null-check before the switch. *(Java 21's pattern-matching `switch` allows an explicit `case null` — but on Java 17 you must guard.)*

---

## Q29. String concatenation in `println`

```java
System.out.println(1 + 2 + "3");      // ?
System.out.println("1" + 2 + 3);      // ?
System.out.println('a' + 'b');        // ?
System.out.println("" + 'a' + 'b');   // ?
System.out.println(1 + 2 + "" + 3 + 4); // ?
```

**✅ Output:**
```
33
123
195
ab
334
```

**The rule:** `+` is **left-associative**. Once one operand is a `String`, everything to the right becomes concatenation. Before that, it's arithmetic — and `char + char` promotes to `int` (`97 + 98 = 195`).

---

## Q30. `substring` and `equals`/`hashCode` contract

```java
String s = "hello world";
System.out.println(s.substring(6));       // ?
System.out.println(s.substring(0, 5));    // ?
System.out.println(s.substring(5, 5));    // ?
System.out.println(s.substring(12));      // ?
```

**✅ Output:**
```
world
hello
              ← empty string
StringIndexOutOfBoundsException
```

> `substring(begin, end)` is **begin-inclusive, end-exclusive**. `substring(5,5)` is legal and returns `""`. `substring(s.length())` is legal (returns `""`); anything beyond throws.

---

# F — Autoboxing & Integer cache traps

## Q31. The Integer cache — the #1 asked output question

```java
Integer a = 127, b = 127;
Integer c = 128, d = 128;
int    e = 128;

System.out.println(a == b);     // ?
System.out.println(c == d);     // ?
System.out.println(c == e);     // ?
System.out.println(c.equals(d)); // ?
```

**✅ Output:**
```
true
false
true
true
```

**The rule:** `Integer.valueOf()` — which autoboxing calls — **caches objects for −128 to +127**. Within that range, boxing the same value returns the *same object*, so `==` is true. Outside it, each boxing creates a new object.

Line 3 is the twist: **`c == e` compares `Integer` with `int`, so `c` is UNBOXED** and it becomes a numeric comparison → `true`.

> **Cached types:** `Boolean` (both), `Byte` (all), `Character` (0–127), `Short` & `Integer` & `Long` (−128..127). `Float` and `Double` are **never** cached.
>
> **Rule for real code:** never use `==` on wrapper types. Always `.equals()` or unbox explicitly.

---

## Q32. Unboxing `NullPointerException`

```java
Map<String, Boolean> map = new HashMap<>();
boolean flag = map.get("missing");
```

**✅ Output:** `NullPointerException`

> `map.get()` returns `null` (a `Boolean`), and assigning to `boolean` triggers **unboxing of null** → NPE. The stack trace points at a line with no visible method call, which is why it's confusing in production.

**Same trap, harder:**
```java
Integer count = null;
if (count == 0) { }      // NPE — count is unboxed to compare with int 0
if (count == null) { }   // ✅ fine — comparing two references, no unboxing
```

---

## Q33. Ternary operator numeric promotion — the sneaky one

```java
Map<String, Boolean> map = new HashMap<>();
Boolean b = (map != null) ? map.get("missing") : false;
System.out.println(b);
```

**✅ Output:** `NullPointerException` — even though the target is a `Boolean`!

**The rule:** the two branches of the ternary are `Boolean` and `boolean`. The compiler applies **binary numeric promotion**, making the *whole expression's type* `boolean` — so `map.get("missing")` gets **unboxed** → NPE, before it can ever be re-boxed into `b`.

**Fix:** make both branches the same reference type: `: Boolean.FALSE`.

---

## Q34. Ternary with `Integer` and `Double`

```java
Object o = true ? new Integer(1) : new Double(2.0);
System.out.println(o);
```

**✅ Output:** `1.0`  ← not `1`!

> Same rule as Q33. `Integer` and `Double` both unbox, binary numeric promotion makes the expression type **`double`**, so `1` becomes `1.0` and is re-boxed as a `Double`. The `false` branch never even runs, yet it changed the result's type.

---

## Q35. `equals` across wrapper types

```java
Integer i = 1;
Long    l = 1L;
Short   s = 1;

System.out.println(i.equals(l));   // ?
System.out.println(i.equals(s));   // ?
System.out.println(i.equals(1));   // ?
System.out.println(l.equals(1));   // ?
```

**✅ Output:**
```
false
false
true
false
```

**The rule:** `Integer.equals` starts with `if (obj instanceof Integer)` — a `Long` or `Short` fails the type check immediately, **regardless of numeric value**. Line 4 is the classic bug: the literal `1` boxes to `Integer`, so `Long(1).equals(Integer(1))` is `false`. Write `l.equals(1L)`.

---

## Q36. The `Set<Short>` remove trap

```java
Set<Short> set = new HashSet<>();
for (short i = 0; i < 100; i++) {
    set.add(i);
    set.remove(i - 1);
}
System.out.println(set.size());
```

**✅ Output:** `100`  ← not `1`!

**The rule:** `i - 1` promotes `short` to **`int`**, so it boxes to an **`Integer`**, and `remove(Object)` looks for an `Integer` in a set full of `Short`s — never matching. Nothing is ever removed.

**Fix:** `set.remove((short)(i - 1));`

---

# G — Operator & numeric traps

## Q37. `i = i++`

```java
int i = 0;
i = i++;
System.out.println(i);      // ?

int j = 0;
j = ++j;
System.out.println(j);      // ?

int k = 0;
k = k++ + ++k;
System.out.println(k);      // ?
```

**✅ Output:**
```
0
1
2
```

**The rule for `i = i++`:** Java evaluates in this order —
```
1. Read i's current value (0)  → save it as the result of i++
2. Increment i                 → i is now 1
3. ASSIGN the saved value (0) back to i  → i is 0 again ❌
```
The assignment **overwrites** the increment. `++i` (pre-increment) returns the *new* value, so `j = ++j` gives 1.

For `k = k++ + ++k`: `k++` yields `0` (k→1), `++k` yields `2` (k→2), so `0 + 2 = 2` is assigned.

---

## Q38. Integer division and modulo with negatives

```java
System.out.println(10 / 3);      // ?
System.out.println(10 % 3);      // ?
System.out.println(-10 / 3);     // ?
System.out.println(-10 % 3);     // ?
System.out.println(10 / 3.0);    // ?
```

**✅ Output:**
```
3
1
-3
-1
3.3333333333333335
```

> Integer division **truncates toward zero** (not floor). The sign of `%` follows the **dividend**, so `-10 % 3` is `-1`, not `2`. *(Use `Math.floorMod(-10, 3)` → `2` if you want the mathematical modulo.)*

---

## Q39. Floating-point precision

```java
System.out.println(0.1 + 0.2);              // ?
System.out.println(0.1 + 0.2 == 0.3);       // ?
System.out.println(1.03 - 0.42);            // ?
```

**✅ Output:**
```
0.30000000000000004
false
0.6100000000000001
```

**The rule:** `double`/`float` are IEEE-754 **binary** fractions — `0.1` has no exact binary representation. **Never use `double` for money.** Use `BigDecimal` (with the *String* constructor):
```java
new BigDecimal("0.1").add(new BigDecimal("0.2"));   // exactly 0.3
new BigDecimal(0.1)                                 // ⚠️ WRONG — captures the binary error
```
For comparisons, use an epsilon: `Math.abs(a - b) < 1e-9`.

---

## Q40. Division by zero — int vs double

```java
System.out.println(1 / 0);        // ?
System.out.println(1.0 / 0);      // ?
System.out.println(-1.0 / 0);     // ?
System.out.println(0.0 / 0.0);    // ?
System.out.println(1 % 0);        // ?
```

**✅ Output:**
```
ArithmeticException: / by zero
Infinity
-Infinity
NaN
ArithmeticException: / by zero
```

> **Integer** division by zero throws; **floating-point** division by zero returns `Infinity`/`NaN` and never throws.

---

## Q41. `NaN` comparisons

```java
double nan = 0.0 / 0.0;
System.out.println(nan == nan);                       // ?
System.out.println(Double.valueOf(nan).equals(nan));  // ?
System.out.println(Double.isNaN(nan));                // ?
```

**✅ Output:**
```
false
true
true
```

> **`NaN != NaN`** by IEEE-754 rule — but `Double.equals` compares the raw bits, so it says `true`. This inconsistency is why `Double` behaves oddly as a `HashMap` key. Always test with `Double.isNaN()`.

---

## Q42. Compound assignment hides a cast

```java
byte b = 10;
b = b + 5;      // ?
b += 5;         // ?
```

**✅ Output:**
- `b = b + 5;` → ❌ **compile error**: `incompatible types: possible lossy conversion from int to byte`
- `b += 5;` → ✅ compiles, `b` becomes `15`

**The rule:** `b + 5` promotes to `int`, and `int` won't fit into `byte` without a cast. But **compound assignment operators (`+=`, `-=`, `*=`, …) contain an implicit cast** — `b += 5` is really `b = (byte)(b + 5)`.

**The dangerous consequence:**
```java
byte b = 127;
b += 1;
System.out.println(b);       // -128  ← silent overflow, no warning!
```

---

## Q43. Integer overflow

```java
System.out.println(Integer.MAX_VALUE + 1);      // ?
System.out.println(Math.abs(Integer.MIN_VALUE)); // ?
int seconds = 24 * 60 * 60 * 1000 * 1000;
System.out.println(seconds);                     // ?
```

**✅ Output:**
```
-2147483648
-2147483648
500654080
```

> Java integers **wrap around silently**. `Math.abs(Integer.MIN_VALUE)` is negative because `+2147483648` doesn't exist in `int`. The third line is the famous microseconds-per-day bug — fix with `24L * 60 * 60 * 1000 * 1000`.
>
> Java 8+ gives you `Math.addExact()` / `multiplyExact()`, which throw `ArithmeticException` on overflow instead of wrapping.

---

## Q44. Short-circuit vs non-short-circuit

```java
int i = 0;
if (false && (i++ > 0)) { }
System.out.println(i);        // ?

int j = 0;
if (false & (j++ > 0)) { }
System.out.println(j);        // ?

String s = null;
if (s != null && s.length() > 0) { }    // ?
if (s != null &  s.length() > 0) { }    // ?
```

**✅ Output:**
```
0
1
(no exception)
NullPointerException
```

> `&&` and `||` **short-circuit** — the right side isn't evaluated if the result is already known. `&` and `|` **always evaluate both sides**. That's why `s != null && s.length() > 0` is safe and `&` is not.

---

# H — try/catch/finally traps

## Q45. `finally` overrides `return`

```java
static int test() {
    try {
        return 1;
    } finally {
        return 2;
    }
}
System.out.println(test());
```

**✅ Output:** `2`

**The rule:** a `return` inside `finally` **discards** the `try` block's return value — and **swallows any pending exception too**:
```java
static int test2() {
    try {
        throw new RuntimeException("boom");
    } finally {
        return 2;          // exception silently disappears! 💀
    }
}
test2();      // returns 2, no exception
```
> Say: *"Never `return` or `throw` from a `finally` block — most static analysers (SonarQube, SpotBugs) flag it as a bug."*

---

## Q46. `finally` mutating the return value

```java
static int test() {
    int x = 1;
    try {
        return x;
    } finally {
        x = 2;
        System.out.println("finally ran, x = " + x);
    }
}
System.out.println(test());
```

**✅ Output:**
```
finally ran, x = 2
1
```

**The rule:** `return x` **evaluates and saves** `x` (value `1`) onto the stack *before* `finally` runs. Reassigning `x` afterwards changes the variable, not the already-captured return value.

> ⚠️ **But with a mutable object it's different:**
> ```java
> static List<String> test() {
>     List<String> list = new ArrayList<>();
>     try { return list; }
>     finally { list.add("added"); }     // mutates the same object the caller gets
> }
> test();     // ["added"]  ← the mutation IS visible
> ```
> The *reference* is captured, but the object it points to is still mutable.

---

## Q47. Execution order of try / catch / finally

```java
try {
    System.out.println("try");
    throw new RuntimeException("x");
} catch (RuntimeException e) {
    System.out.println("catch");
} finally {
    System.out.println("finally");
}
System.out.println("after");
```

**✅ Output:**
```
try
catch
finally
after
```

**When does `finally` NOT run?** Only three cases — worth naming all three:
1. `System.exit(0)` inside the `try`
2. The JVM crashes / is killed
3. The thread is killed or enters an infinite loop / deadlock

```java
try { System.exit(0); } finally { System.out.println("never printed"); }
```

---

## Q48. Catch order — subclass must come first

```java
try {
    throw new FileNotFoundException();
} catch (IOException e) {
    System.out.println("IOException");
} catch (FileNotFoundException e) {
    System.out.println("FileNotFoundException");
}
```

**✅ Output:** ❌ **Compile error** — `exception FileNotFoundException has already been caught`

**The rule:** catch blocks are tested **top to bottom**, so a **subclass must be caught before its superclass** — otherwise the later block is unreachable, and Java makes unreachable catch a compile error.

**Correct order → output `FileNotFoundException`:**
```java
catch (FileNotFoundException e) { ... }   // most specific FIRST
catch (IOException e)           { ... }
catch (Exception e)             { ... }   // most general LAST
```

---

## Q49. try-with-resources — closing order

```java
class Resource implements AutoCloseable {
    private final String name;
    Resource(String name) { this.name = name; System.out.println("open " + name); }
    public void close() { System.out.println("close " + name); }
}

try (Resource a = new Resource("A"); Resource b = new Resource("B")) {
    System.out.println("body");
} finally {
    System.out.println("finally");
}
```

**✅ Output:**
```
open A
open B
body
close B          ← REVERSE order of declaration
close A
finally          ← resources close BEFORE finally
```

**The rule:** resources are closed in the **reverse order** they were declared (like a stack), and **before** any `catch` or `finally` block runs.

---

## Q50. `throw` in both try and finally — suppressed exceptions

```java
try (Resource r = new ResourceThatThrowsOnClose()) {
    throw new RuntimeException("from body");
} catch (Exception e) {
    System.out.println(e.getMessage());
    System.out.println(Arrays.toString(e.getSuppressed()));
}
```

**✅ Output:**
```
from body
[java.lang.RuntimeException: from close]
```

**The rule:** with try-with-resources, if both the body and `close()` throw, the **body's exception wins** and the close exception is attached as a **suppressed exception** (`getSuppressed()`). With an old-style `finally`, the second exception would simply **destroy** the first — which is exactly the bug try-with-resources was introduced to fix.

---

# I — Collections traps

## Q51. `remove(int)` vs `remove(Object)` — the classic

```java
List<Integer> list = new ArrayList<>(Arrays.asList(10, 20, 30));
list.remove(1);
System.out.println(list);                       // ?

List<Integer> list2 = new ArrayList<>(Arrays.asList(10, 20, 30));
list2.remove(Integer.valueOf(20));
System.out.println(list2);                      // ?
```

**✅ Output:**
```
[10, 30]
[10, 30]
```
…**but for completely different reasons.**

**The rule:** `List` has **two** `remove` methods — `remove(int index)` and `remove(Object o)`. With a `List<Integer>`, the literal `1` is an `int`, so **`remove(int)` wins (no boxing needed — remember Q3: exact match beats boxing)** and it removes **index 1** (the value `20`). To remove *by value*, box explicitly: `remove(Integer.valueOf(20))` or `remove((Integer) 20)`.

> This is Q1's rule showing up in the JDK's own API. Connect the two out loud — it shows you understand the principle rather than memorising trivia.

---

## Q52. `Arrays.asList` is fixed-size

```java
List<String> list = Arrays.asList("a", "b", "c");
list.set(0, "z");        // ?
list.add("d");           // ?
list.remove("a");        // ?
```

**✅ Output:**
- `set(0, "z")` → ✅ works, list becomes `[z, b, c]`
- `add("d")` → `UnsupportedOperationException`
- `remove("a")` → `UnsupportedOperationException`

**The rule:** `Arrays.asList` returns a **fixed-size view backed by the array** — you can *replace* elements but not resize. `List.of(...)` (Java 9+) is **fully immutable** — even `set` throws.

| | `set` | `add`/`remove` | `null` allowed |
|---|---|---|---|
| `Arrays.asList(...)` | ✅ | ❌ UOE | ✅ |
| `List.of(...)` | ❌ UOE | ❌ UOE | ❌ NPE |
| `new ArrayList<>(Arrays.asList(...))` | ✅ | ✅ | ✅ |

---

## Q53. `ConcurrentModificationException`

```java
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
for (String s : list) {
    if (s.equals("b")) list.remove(s);
}
System.out.println(list);
```

**✅ Output:** `ConcurrentModificationException`

**The rule:** the for-each loop uses an `Iterator` that tracks a `modCount`. Structurally modifying the list behind the iterator's back makes `next()` throw — **fail-fast** behaviour.

**Three correct fixes:**
```java
list.removeIf(s -> s.equals("b"));                  // ✅ best, Java 8+

Iterator<String> it = list.iterator();              // ✅ classic
while (it.hasNext()) { if (it.next().equals("b")) it.remove(); }

for (int i = list.size() - 1; i >= 0; i--) { ... }  // ✅ backwards index loop
```

> 🪤 **The nastiest variant:** removing the **second-to-last** element does *not* throw — the `hasNext()` check (`cursor != size`) happens to become true and the loop exits early with no exception. Undefined-ish behaviour, which is why CME is documented as *best-effort*.
> ```java
> List<String> l = new ArrayList<>(List.of("a","b","c"));
> for (String s : l) { if (s.equals("b")) l.remove(s); }   // NO exception, prints [a, c]
> ```

---

## Q54. `HashSet` without `equals`/`hashCode`

```java
class Person {
    String name;
    Person(String name) { this.name = name; }
}

Set<Person> set = new HashSet<>();
set.add(new Person("Priya"));
set.add(new Person("Priya"));
System.out.println(set.size());
```

**✅ Output:** `2`

**The rule:** `HashSet` deduplicates using `hashCode()` **and** `equals()`. Without overriding them, `Object`'s identity-based versions are used, so two distinct objects are always "different".

**Fix (Java 17 — a `record` gives you both for free):**
```java
record Person(String name) { }     // auto-generates equals, hashCode, toString → size() = 1
```

> **The contract:** *equal objects must have equal hash codes.* Overriding only `equals` and not `hashCode` is the classic bug — the set puts them in different buckets and `equals` is never even called.

---

## Q55. Mutating a key after inserting it

```java
Map<List<String>, String> map = new HashMap<>();
List<String> key = new ArrayList<>(List.of("a"));
map.put(key, "value");

key.add("b");                              // mutate the key AFTER insertion

System.out.println(map.get(key));          // ?
System.out.println(map.size());            // ?
System.out.println(map.containsKey(key));  // ?
```

**✅ Output:**
```
null
1
false
```

**The rule:** the entry was stored in the bucket for the **old** hash. Changing the key changes its `hashCode()`, so lookups now search the wrong bucket — the entry is **stranded**: still counted in `size()`, but unreachable.

> This is the argument for **immutable keys**. `String` is the ideal `HashMap` key precisely because it's immutable and caches its hash.

---

## Q56. `HashMap` vs `TreeMap` vs `LinkedHashMap` — null and ordering

```java
Map<String, Integer> hm = new HashMap<>();
hm.put("b", 2); hm.put("a", 1); hm.put(null, 0);
System.out.println(hm);                      // ?

Map<String, Integer> tm = new TreeMap<>();
tm.put("b", 2); tm.put("a", 1);
System.out.println(tm);                      // ?
tm.put(null, 0);                             // ?

Map<String, Integer> lhm = new LinkedHashMap<>();
lhm.put("b", 2); lhm.put("a", 1);
System.out.println(lhm);                     // ?
```

**✅ Output:**
```
{null=0, a=1, b=2}     ← HashMap order is NOT guaranteed (this is just how these hash)
{a=1, b=2}             ← TreeMap: sorted by key
NullPointerException   ← TreeMap must compare the key, and you can't compare null
{b=2, a=1}             ← LinkedHashMap: insertion order
```

| | Order | Null key | Null values |
|---|---|---|---|
| `HashMap` | none (don't rely on it) | ✅ one | ✅ |
| `LinkedHashMap` | insertion (or access) | ✅ one | ✅ |
| `TreeMap` | sorted | ❌ NPE | ✅ |
| `Hashtable` | none | ❌ NPE | ❌ NPE |

> ⚠️ **Never claim "HashMap prints in random order"** — it's *unspecified*, and small String keys often look sorted by accident. Say **"no ordering guarantee"**.

---

## Q57. Array covariance — `ArrayStoreException`

```java
Object[] objects = new String[3];
objects[0] = "hello";        // fine
objects[1] = 42;             // ?
```

**✅ Output:** `ArrayStoreException: java.lang.Integer`

**The rule:** **arrays are covariant** (`String[]` IS-A `Object[]`) but **generics are not** (`List<String>` is NOT a `List<Object>`). Array covariance is unsound, so the JVM has to check every store at runtime.

```java
List<Object> list = new ArrayList<String>();   // ❌ compile error — generics caught it at COMPILE time
```
> Generics chose compile-time safety; arrays chose runtime checks. That contrast is a strong closing line.

---

# J — Static & thread traps

## Q58. Static method on a `null` reference

```java
class Test {
    static void hello() { System.out.println("hello"); }

    public static void main(String[] a) {
        Test t = null;
        t.hello();
    }
}
```

**✅ Output:** `hello` — **no NullPointerException**

**The rule:** a static method is resolved from the **compile-time type of the reference**, not the object. The compiler rewrites `t.hello()` into `Test.hello()` and the null value is never dereferenced.

> Same story for a static field: `t.staticField` works on a null reference. (An *instance* field or method would throw NPE.)

---

## Q59. `run()` vs `start()`

```java
public class Test {
    public static void main(String[] a) {
        Thread t = new Thread(() ->
            System.out.println("running in " + Thread.currentThread().getName()));

        t.run();      // ?
        t.start();    // ?
    }
}
```

**✅ Output:**
```
running in main            ← run() is just an ordinary method call!
running in Thread-0        ← start() creates a new OS thread
```

**The rule:** **`start()` creates a new thread and the JVM calls `run()` on it. Calling `run()` yourself executes it on the current thread** — no concurrency at all.

**Follow-up: "what if you call `start()` twice?"**
```java
t.start();
t.start();     // IllegalThreadStateException — a Thread can never be restarted
```

---

## Q60. `static` counter shared across instances

```java
class Counter {
    static int staticCount = 0;
    int instanceCount = 0;

    Counter() { staticCount++; instanceCount++; }
}

new Counter(); new Counter(); Counter c = new Counter();
System.out.println(c.staticCount + " " + c.instanceCount);
System.out.println(Counter.staticCount);
```

**✅ Output:**
```
3 1
3
```

> One `staticCount` exists **per class**; one `instanceCount` exists **per object**. This is the simplest way to demonstrate the difference, and it's a common warm-up question.

---

# K — Rapid-fire answer table

Cover the right column and go down the list. Target: **under 4 minutes**, no hesitation.

| Program | Output | The one-line reason |
|---|---|---|
| `methodTest(null)` with `Object`/`String` | `String` version | Most **specific** type wins |
| `methodTest(null)` with `String`/`Integer` | **Compile error** | Ambiguous — siblings |
| Sub overrides with broader checked exception | **Compile error** | Can't **widen** `throws` |
| Sub overrides with `RuntimeException` | Compiles | Unchecked is unrestricted |
| Sub overrides with `protected` (super `public`) | **Compile error** | Can't reduce visibility |
| `Parent p = new Child(); p.staticMethod()` | Parent's | Static = **hidden**, compile-time bound |
| `Parent p = new Child(); p.field` | Parent's | Fields are **not** polymorphic |
| `Parent p = new Child(); p.method()` | Child's | Methods **are** polymorphic |
| Constructor calls overridden method | Child's method, fields **= 0** | Super ctor runs before child fields |
| `show(5)` with `int`/`long`/`Integer`/`int...` | `int` | Widening > boxing > varargs |
| `s1 == s2` for two `"hello"` literals | `true` | String pool |
| `"hello" == new String("hello")` | `false` | `new` always allocates |
| `"hel" + "lo" == "hello"` | `true` | Compile-time constant folding |
| `part + "lo" == "hello"` (non-final var) | `false` | Runtime concatenation |
| `Integer 127 == 127` / `128 == 128` | `true` / `false` | Cache is **−128..127** |
| `Integer c = 128; int e = 128; c == e` | `true` | Mixing wrapper+primitive → **unboxes** |
| `boolean b = map.get(missing)` | **NPE** | Unboxing `null` |
| `x ? map.get(k) : false` (Boolean/boolean) | **NPE** | Ternary promotion unboxes |
| `true ? Integer : Double` | `1.0` | Ternary numeric promotion |
| `i = i++` | `0` | Assignment overwrites the increment |
| `0.1 + 0.2` | `0.30000000000000004` | IEEE-754 binary fractions |
| `1/0` vs `1.0/0` | Exception vs `Infinity` | int throws, double doesn't |
| `NaN == NaN` | `false` | IEEE-754 rule |
| `byte b=10; b = b+5;` | **Compile error** | Promotes to `int` |
| `byte b=10; b += 5;` | `15` | `+=` has an **implicit cast** |
| `try{return 1;} finally{return 2;}` | `2` | `finally`'s return wins |
| `try{return x;} finally{x=2;}` | `1` | Return value captured first |
| `catch(IOException)` before `catch(FNFE)` | **Compile error** | Subclass must come first |
| try-with-resources close order | **Reverse** of declaration | Stack (LIFO), before `finally` |
| `list.remove(1)` on `List<Integer>` | removes **index** 1 | `remove(int)` beats `remove(Object)` |
| `Arrays.asList(...).add(x)` | **UOE** | Fixed-size array view |
| Remove during for-each | **CME** | Fail-fast iterator (`modCount`) |
| `HashSet` of 2 equal objects, no `equals` | size `2` | Identity `equals`/`hashCode` |
| `TreeMap.put(null, v)` | **NPE** | Must compare the key |
| `Object[] o = new String[3]; o[1] = 42;` | **ArrayStoreException** | Arrays are covariant |
| `nullRef.staticMethod()` | Runs fine | Statics don't dereference |
| `t.run()` vs `t.start()` | `main` vs `Thread-0` | `run()` is a plain method call |
| `t.start()` twice | **IllegalThreadStateException** | Threads can't restart |
| `String.valueOf(null)` | **NPE** | Picks `valueOf(char[])` |
| `System.exit(0)` inside `try` | `finally` **skipped** | JVM dies |

---

# L — 20 self-test drills (answers hidden)

Write your answer down **before** expanding. Aim for 17/20.

### Drill 1
```java
public class T {
    void f(Object o) { System.out.println("Object"); }
    void f(String s) { System.out.println("String"); }
    void f(Integer i){ System.out.println("Integer"); }

    public static void main(String[] a) {
        new T().f(null);
    }
}
```
<details><summary>👉 Answer</summary>

**Compile error — ambiguous.** `Object` is eliminated, but `String` and `Integer` are siblings with no most-specific candidate.
*(If `Integer` were removed, the answer would be `String`.)*
</details>

### Drill 2
```java
class A { void f() throws Exception { System.out.println("A"); } }
class B extends A { void f() { System.out.println("B"); } }

public static void main(String[] args) {
    A a = new B();
    a.f();
}
```
<details><summary>👉 Answer</summary>

**Compile error at the call site** — `unreported exception Exception; must be caught or declared to be thrown`.
The override (removing `throws`) is perfectly legal, but the *caller* is checked against the **reference type** `A`, which declares `throws Exception`.
Fix: wrap in `try/catch`, or add `throws Exception` to `main`. Then it prints **`B`**.
</details>

### Drill 3
```java
class A { A() { print(); } void print() { System.out.println("A"); } }
class B extends A {
    String s = "hello";
    void print() { System.out.println("B: " + s.length()); }
}
new B();
```
<details><summary>👉 Answer</summary>

**`NullPointerException`.** `A`'s constructor calls the overridden `B.print()` before `s = "hello"` runs, so `s` is still `null` and `s.length()` throws. *(Compare Q13 where the field was an `int` and quietly gave `0` — with an object it blows up.)*
</details>

### Drill 4
```java
Integer a = 1000, b = 1000;
System.out.println(a == b);
System.out.println(a.equals(b));
System.out.println(a.intValue() == b.intValue());
```
<details><summary>👉 Answer</summary>

```
false
true
true
```
1000 is outside the `-128..127` cache → two distinct objects.
</details>

### Drill 5
```java
String a = "java";
String b = "ja";
String c = b + "va";
final String d = "ja";
String e = d + "va";
System.out.println((a == c) + " " + (a == e));
```
<details><summary>👉 Answer</summary>

**`false true`** — `b` is a normal variable (runtime concatenation), `d` is `final` and initialised with a literal, making it a **compile-time constant** the compiler folds into `"java"`.
</details>

### Drill 6
```java
static int f() {
    int x = 0;
    try { x = 1; return x; }
    catch (Exception e) { x = 2; return x; }
    finally { x = 3; System.out.println("finally x=" + x); }
}
System.out.println(f());
```
<details><summary>👉 Answer</summary>

```
finally x=3
1
```
`return x` captured the value `1` before `finally` ran.
</details>

### Drill 7
```java
List<Integer> l = new ArrayList<>(List.of(1, 2, 3));
l.remove(2);
System.out.println(l);
```
<details><summary>👉 Answer</summary>

**`[1, 2]`** — `remove(int index)` wins over `remove(Object)`, so index **2** (value `3`) is removed. `l.remove(Integer.valueOf(2))` would give `[1, 3]`.
</details>

### Drill 8
```java
System.out.println(Math.min(Double.MIN_VALUE, 0.0d));
```
<details><summary>👉 Answer</summary>

**`0.0`** — `Double.MIN_VALUE` is the smallest **positive** value (`4.9E-324`), *not* the most negative one. For that you'd want `-Double.MAX_VALUE`.
*(By contrast `Integer.MIN_VALUE` really is the most negative int — the inconsistency is the trap.)*
</details>

### Drill 9
```java
class A { static void f() { System.out.println("A.f"); } }
class B extends A { static void f() { System.out.println("B.f"); } }

A a = new B();
a.f();
((B) a).f();
B.f();
```
<details><summary>👉 Answer</summary>

```
A.f
B.f
B.f
```
Static binding follows the **reference type** every time.
</details>

### Drill 10
```java
System.out.println('a' + 'b' + "c");
System.out.println("c" + 'a' + 'b');
```
<details><summary>👉 Answer</summary>

```
195c
cab
```
Left-associative: `'a'+'b'` is int arithmetic (195) before the String appears; in the second line the String comes first, so everything is concatenation.
</details>

### Drill 11
```java
Set<String> set = new HashSet<>();
set.add("a"); set.add("b"); set.add("a");
System.out.println(set.size());

List<String> list = new ArrayList<>();
list.add("a"); list.add("b"); list.add("a");
System.out.println(list.size());
System.out.println(list.indexOf("a") + " " + list.lastIndexOf("a"));
```
<details><summary>👉 Answer</summary>

```
2
3
0 2
```
`Set` deduplicates (String has proper `equals`/`hashCode`); `List` allows duplicates.
</details>

### Drill 12
```java
public class T {
    static int x = getX();
    static { System.out.println("static block"); }
    static int getX() { System.out.println("getX"); return 5; }
    public static void main(String[] a) { System.out.println("main, x=" + x); }
}
```
<details><summary>👉 Answer</summary>

```
getX
static block
main, x=5
```
Static fields and static blocks run **in source order** at class load, before `main`.
</details>

### Drill 13
```java
int[] arr = new int[3];
String[] strs = new String[3];
boolean[] flags = new boolean[3];
System.out.println(arr[0] + " " + strs[0] + " " + flags[0]);
System.out.println(arr[3]);
```
<details><summary>👉 Answer</summary>

```
0 null false
ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3
```
Array elements get **default values** (`0`, `null`, `false`) — unlike local variables, which must be explicitly initialised or the code won't compile.
</details>

### Drill 14
```java
Object o = "hello";
if (o instanceof Integer) System.out.println("Integer");
else if (o instanceof String s) System.out.println("String of length " + s.length());
```
<details><summary>👉 Answer</summary>

**`String of length 5`** — Java 16+ **pattern matching for `instanceof`** binds `s` automatically, no cast needed. You're on **Java 17**, so use this in interviews ([18 — Technical Versions](./18-robogebra-technical-versions.md)).
</details>

### Drill 15
```java
StringBuilder sb = new StringBuilder("abc");
modify(sb);
System.out.println(sb);

static void modify(StringBuilder s) {
    s.append("def");
    s = new StringBuilder("xyz");
    s.append("!");
}
```
<details><summary>👉 Answer</summary>

**`abcdef`** — Java is **pass-by-value of the reference**. `append` mutates the shared object (visible to the caller), but reassigning `s` only changes the local copy of the reference (invisible to the caller).
*This is the answer to "is Java pass-by-value or pass-by-reference?" — always **pass-by-value**.*
</details>

### Drill 16
```java
public class T {
    public static void main(String[] args) {
        System.out.println(args.length);
        System.out.println(args[0]);
    }
}
// run as: java T
```
<details><summary>👉 Answer</summary>

```
0
ArrayIndexOutOfBoundsException
```
With no command-line arguments, `args` is an **empty array**, not `null`. (In C it would include the program name; in Java it does not.)
</details>

### Drill 17
```java
interface Shape { default String name() { return "shape"; } }
interface Square extends Shape { default String name() { return "square"; } }
class MySquare implements Shape, Square { }

System.out.println(new MySquare().name());
```
<details><summary>👉 Answer</summary>

**`square`** — no ambiguity error here because `Square` **extends** `Shape`, so it's the more specific interface and wins. *(Compare Q19, where `A` and `B` were unrelated → compile error.)*
</details>

### Drill 18
```java
try {
    int[] arr = new int[2];
    arr[5] = 10;
    System.out.println("A");
} catch (ArrayIndexOutOfBoundsException e) {
    System.out.println("B");
} catch (Exception e) {
    System.out.println("C");
} finally {
    System.out.println("D");
}
System.out.println("E");
```
<details><summary>👉 Answer</summary>

```
B
D
E
```
Only the **first matching** catch block runs — `catch` is not a fall-through switch.
</details>

### Drill 19
```java
class A {
    void f(int x)  { System.out.println("A.int"); }
}
class B extends A {
    void f(long x) { System.out.println("B.long"); }
}
new B().f(5);
```
<details><summary>👉 Answer</summary>

**`A.int`** — `B.f(long)` **overloads** rather than overrides, and `B` inherits `f(int)` from `A`. Both are candidates; **widening comes second to an exact match**, so `f(int)` wins.
*Overload resolution searches the whole inherited hierarchy, not just the declaring class.*
</details>

### Drill 20
```java
class Parent {
    Parent() { System.out.println("Parent()"); }
    Parent(int x) { System.out.println("Parent(int)"); }
}
class Child extends Parent {
    Child() { System.out.println("Child()"); }
    Child(int x) { this(); System.out.println("Child(int)"); }
}
new Child(5);
```
<details><summary>👉 Answer</summary>

```
Parent()
Child()
Child(int)
```
`Child(int)` calls `this()` first → `Child()` runs, which has an implicit `super()` → `Parent()` runs first of all. Note `Parent(int)` is **never** called.
</details>

---

## 🎤 How to answer these in the actual interview

**1. Never blurt the answer.** Say the *rule* first, then the answer:
> *"This is overload resolution, which happens at compile time — the compiler picks the most specific applicable method. `String` is more specific than `Object`, so it prints `Calling String method`."*

**2. Always check "does it compile?" first.** Roughly a third of these questions are compile errors, and candidates who trace the runtime immediately walk straight past them.

**3. Name the error precisely.** *"Compile error"* is worth one point. *"Compile error — overridden method does not throw IOException, because you can't widen the throws clause of an override"* is worth five.

**4. Volunteer the fix.** After every wrong-looking program, say how you'd make it correct. That's what turns a quiz into a conversation.

**5. Connect the questions.** Both of the programs you were asked test the same axis — **compile-time vs runtime binding**. Pointing that out ("these are really the same question from two directions") is a senior-level move.

**6. If you genuinely don't know:** *"I'd guess X because of [rule], but I'd want to run it to be sure — this is exactly the kind of thing I'd write a quick unit test for."* Honest reasoning beats a confident wrong answer.

---

## Quick Revision Sheet — the traps grouped by memory hook

```
OVERLOADING (compile time) ⭐
  null → the MOST SPECIFIC type wins (String beats Object)
  two SIBLING types (String/Integer) → AMBIGUOUS → compile error
  resolution order: WIDENING > BOXING > VARARGS                    ⭐
  return type ALONE can never overload

OVERRIDING (runtime) ⭐
  cannot WIDEN throws | cannot REDUCE visibility | return must be covariant
  static  → HIDDEN, not overridden → the REFERENCE type wins
  fields  → NOT polymorphic       → the REFERENCE type wins        ⭐
  private/final/static → not overridable
  calling an overridable method from a CONSTRUCTOR → child fields are still 0/null 💥

INITIALISATION ORDER
  static block → instance block → constructor
  parent FIRST, always (super() runs before the child's field initialisers) ⭐

STRING
  literals are POOLED → "a" == "a" is true
  new String() → always a new object → == is false
  "hel" + "lo"        → folded at COMPILE time → pooled → true      ⭐
  part + "lo" (non-final var) → RUNTIME concat → not pooled → false
  String is IMMUTABLE → s.concat("x") does NOTHING unless you assign it

AUTOBOXING
  Integer cache = -128..127 → == true inside, false outside          ⭐
  wrapper == primitive → the wrapper is UNBOXED → compares VALUES → true
  unboxing NULL → NullPointerException                               ⭐
  ternary numeric promotion: true ? Integer : Double → 1.0

OPERATORS / NUMBERS
  i = i++      → 0  (the assignment overwrites the increment)        ⭐
  0.1 + 0.2    → 0.30000000000000004 (IEEE-754)
  1/0 throws | 1.0/0 → Infinity | NaN == NaN → false
  byte b = b + 5;  → compile error (promoted to int)
  byte b += 5;     → fine (+= has an IMPLICIT cast)                  ⭐

try / catch / finally
  finally ALWAYS runs; a return in finally WINS and SWALLOWS the exception 💥
  the return VALUE is captured BEFORE finally runs (primitives)
  catch order must be SPECIFIC → GENERAL, else compile error
  try-with-resources closes in REVERSE order, BEFORE catch/finally    ⭐
  System.exit(0) → finally is SKIPPED

COLLECTIONS
  list.remove(1) on List<Integer> → removes INDEX 1 (remove(int) wins) ⭐
  Arrays.asList(...).add() → UnsupportedOperationException
  removing during a for-each → ConcurrentModificationException → use removeIf ⭐
  HashSet of "equal" objects without equals/hashCode → size 2
  TreeMap.put(null, v) → NPE (it must compare the key)
  Object[] o = new String[3]; o[1] = 42; → ArrayStoreException

STATIC / THREADS
  nullRef.staticMethod() → RUNS FINE (statics don't dereference)      ⭐
  t.run()  → "main"      | t.start() → "Thread-0"                     ⭐
  t.start() twice → IllegalThreadStateException
  String.valueOf(null) → NPE (it picks valueOf(char[]))
```

---

## Related files in this pack

- **[05 — Core Java](./05-java.md)** — the theory behind all of this: OOP, collections internals, HashMap, multithreading
- **[12 — Java 17 Features](./12-java17-features.md)** — records, sealed classes, pattern matching (use `instanceof` patterns in your answers)
- **[17 — SOLID & Design Patterns](./17-solid-design-patterns.md)** — Liskov Substitution is the *principle* behind the Q2 `throws` rule
- **[22 — Java Streams: 20 Coding Problems](./22-java-streams-coding-problems.md)** — the coding half of the Java round
- **[18 — RoboGebra Technical Versions](./18-robogebra-technical-versions.md)** — your real stack: Java 17, Spring Boot 3.2.0

> 🔁 **Drill routine:** cover the answers, work through Section K's rapid-fire table daily (under 4 min), and do 5 of the Section L drills on paper each morning. These questions repeat across companies almost verbatim — the same 40 programs cover the vast majority of Java output rounds.
