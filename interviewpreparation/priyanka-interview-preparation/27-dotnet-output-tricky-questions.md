# C# / .NET "Predict the Output" — 75 Tricky Programs with Answers

> 🔴 **Why this file exists:** your Java round was **output-based** (*"what does this print?"*), and .NET rounds are exactly the same — often worse, because C# has **more** places where the compiler decides something you assumed was decided at runtime. This is the .NET twin of **[23 — Java Predict the Output](./23-java-output-tricky-questions.md)**.

**The format for every program below:**

1. **The code**
2. **The output** (or *compile error*)
3. **The rule** — the one sentence that makes it obvious
4. **The follow-up** they ask next

> 💡 **Golden habit — ask these four in order, every time:**
> **(a) Does it compile?** → **(b) Is it a `struct` or a `class`?** (value vs reference decides half of all C# traps) → **(c) Was anything bound at *compile* time — overloads, `new` hiding, optional parameters?** → **(d) Only then trace the runtime.**
>
> Candidates lose these questions at (a), (b) and (c). They almost never lose them at (d).

> ⚠️ **You are coming from Java. That is an advantage and a trap.** ~40% of C# output questions are designed to punish a Java reflex. **Section A is the eight places where the same code prints something different in C# than in Java** — do that section first, twice.

---

## Table of contents

| # | Section | What it tests |
|---|---|---|
| [A](#a--the-8-places-c-prints-something-different-from-java) | **Java → C# flips (Q1–Q8)** | The 8 programs where your Java instinct gives the wrong answer |
| [B](#b--value-types-reference-types--boxing) | Value types, structs & boxing (Q9–Q16) | Copy semantics, defensive copies, unboxing rules |
| [C](#c--virtual-override-new--interfaces) | `virtual` / `override` / `new` (Q17–Q24) | Static vs dynamic binding, hiding, interface re-implementation |
| [D](#d--initialization-order) | Initialization order (Q25–Q29) | Field initializers, base ctors, static ctors, `beforefieldinit` |
| [E](#e--string-traps) | Strings (Q30–Q36) | Interning, `==` overload, immutability, `null` in concat |
| [F](#f--numbers--operators) | Numbers & operators (Q37–Q45) | `checked`/`unchecked`, `i = i++`, integer division, float precision |
| [G](#g--nullable--the-null-operators) | Nullable & null operators (Q46–Q50) | Lifted operators, `??`, `?.`, `null` comparisons that are *both* false |
| [H](#h--trycatchfinally--using) | `try`/`catch`/`finally`/`using` (Q51–Q56) | Why C# `finally` **can't** swallow a return, disposal order |
| [I](#i--closures-linq--iterators) | Closures, LINQ & iterators (Q57–Q64) | Loop capture, deferred execution, double enumeration, `yield` |
| [J](#j--asyncawait) | `async`/`await` (Q65–Q70) | Execution order, `.Result` deadlock, `async void`, `WhenAll` exceptions |
| [K](#k--collections--equality) | Collections & equality (Q71–Q75) | Modify-during-`foreach`, mutable keys, `record` vs `class` equality |
| [L](#l--rapid-fire-answer-table) | Rapid-fire table | 1-line answers for last-minute revision |
| [M](#m--20-self-test-drills-answers-hidden) | 20 self-test drills | Answers hidden — test yourself |
| [N](#n--how-to-answer-these-in-the-actual-interview) | How to answer in the room | The script that turns a quiz into a conversation |

---

# A — The 8 places C# prints something different from Java

These are the highest-value questions in the whole file. Every one of them is a program that a good Java developer gets **wrong**, which is exactly why interviewers who know you came from Java reach for them.

## Q1. Method overloading with `null` — the one you were already asked, in C#

```csharp
using System;

class OverloadDemo
{
    public void Test(object o) => Console.WriteLine("object method");
    public void Test(string s) => Console.WriteLine("string method");

    static void Main()
    {
        new OverloadDemo().Test(null);
    }
}
```

### ✅ Output

```
string method
```

### The rule (say this exact sentence)

> **"Overload resolution happens at *compile* time and the compiler picks the *most specific* applicable overload. `null` converts to both `object` and `string`, but `string` derives from `object`, so `string` is more specific and wins."**

**Same answer as Java, same reason.** Lead with that — *"this is identical to Java's rule"* — it shows you understand the principle, not a memorized fact.

### 🔥 The follow-ups

**Follow-up 1 — "Force the `object` version."**
```csharp
new OverloadDemo().Test((object)null);   // object method
object o = null;
new OverloadDemo().Test(o);              // object method
```
> The cast changes the argument's **compile-time type**, which is the only thing overload resolution looks at.

**Follow-up 2 — "What if the overloads were `string` and `int[]`?"**
```csharp
public void Test(string s)  { }
public void Test(int[] a)   { }

Test(null);   // ❌ COMPILE ERROR CS0121: the call is ambiguous
```
> `string` and `int[]` are **siblings** — neither converts to the other, so there is no "most specific" one. Fix with a cast.

**Follow-up 3 — "C# has something Java doesn't here. What?"**
```csharp
public void Test(object o)   => Console.WriteLine("object");
public void Test(dynamic d)  => Console.WriteLine("dynamic");
```
> `dynamic` moves overload resolution to **runtime**. If any argument is `dynamic`, the whole call is resolved at runtime against the actual types — the single biggest difference from Java's model. Mentioning `dynamic` unprompted is a strong signal.

---

## Q2. Field initializer vs base constructor — **C# and Java print opposite things**

```csharp
using System;

class Base
{
    public Base()
    {
        Console.WriteLine("Base ctor");
        Show();                       // virtual call from a constructor
    }
    public virtual void Show() { }
}

class Derived : Base
{
    private string name = "initialized";

    public Derived() => Console.WriteLine("Derived ctor");

    public override void Show() => Console.WriteLine($"name = {name ?? "null"}");
}

class Program
{
    static void Main() => new Derived();
}
```

### ✅ Output

```
Base ctor
name = initialized
Derived ctor
```

**The surprising part is not the order of the three lines — it's line 2.** `Show()` is called from inside the base constructor, before `Derived`'s constructor has run at all… and yet `name` is **already assigned**.

### The rule

> **"In C#, the derived class's *instance field initializers* run BEFORE the base constructor. In Java they run AFTER it."**

Full C# construction order:

```
1. Derived field initializers      ← C# does this FIRST
2. Base field initializers
3. Base constructor body
4. Derived constructor body        ← last
```

Java's order is `super()` first, *then* the subclass's field initializers. So **the identical program in Java prints `name = null`** — the classic "virtual call from constructor" bug. In C# the field is already assigned, so it prints `initialized`.

### 🔥 The follow-up

**"So calling a virtual method from a constructor is safe in C#?"**
> **No.** Field *initializers* have run, but the **derived constructor body has not**. Anything assigned in the ctor body is still `null`/`0`:
> ```csharp
> class Derived : Base
> {
>     private string name;                      // no initializer
>     public Derived() { name = "set in ctor"; }
>     public override void Show() => Console.WriteLine(name ?? "null");
> }
> // prints: null
> ```
> **The professional answer:** *"Never call a virtual method from a constructor in either language. C# just changes which half of the state is missing."*

---

## Q3. Methods are **not virtual by default**

```csharp
using System;

class Animal
{
    public void Speak() => Console.WriteLine("Animal speaks");
}

class Dog : Animal
{
    public void Speak() => Console.WriteLine("Dog barks");
}

class Program
{
    static void Main()
    {
        Animal a = new Dog();
        a.Speak();
    }
}
```

### ✅ Output

```
Animal speaks
```
*(plus compiler **warning CS0108**: "'Dog.Speak()' hides inherited member 'Animal.Speak()'. Use the new keyword if hiding was intended.")*

### The rule

> **"In C#, methods are non-virtual by default. Without `virtual` + `override`, `Dog.Speak` *hides* rather than overrides, so the call is bound to the compile-time type `Animal`."**

**In Java this prints `Dog barks`** — Java methods are virtual by default. This single difference is the most-asked C#-vs-Java question in existence.

### 🔥 The follow-ups

**Follow-up 1 — "Fix it."**
```csharp
class Animal { public virtual void Speak() => Console.WriteLine("Animal speaks"); }
class Dog : Animal { public override void Speak() => Console.WriteLine("Dog barks"); }
// now prints: Dog barks
```

**Follow-up 2 — "Does it compile without `new`?"**
> **Yes** — it's a *warning*, not an error. `new` only silences the warning; it changes nothing at runtime.

**Follow-up 3 — "Why did C# make this choice?"**
> *"Explicit opt-in to polymorphism. You can't accidentally override a base method that a later library version adds — the 'brittle base class' / fragile-inheritance problem. Java chose convenience, C# chose versioning safety."* That's a senior-level answer.

---

## Q4. `finally` cannot change the return value in C#

```csharp
using System;

class Program
{
    static int Test()
    {
        try { return 1; }
        finally { return 2; }      // ← what happens?
    }
    static void Main() => Console.WriteLine(Test());
}
```

### ✅ Output

```
❌ COMPILE ERROR CS0157: Control cannot leave the body of a finally clause
```

### The rule

> **"C# forbids `return`, `break`, `continue` or `goto` inside a `finally` block. Java allows it — and in Java it silently swallows the original return value and any in-flight exception."**

**The same program in Java compiles and prints `2`.** C# closed the hole at the language level.

### 🔥 The follow-up

**"Then can `finally` affect the returned value at all?"**
```csharp
static int Test()
{
    int x = 1;
    try { return x; }
    finally { x = 2; Console.WriteLine("finally runs"); }
}
// Output:
// finally runs
// 1
```
> **No.** The return value is **evaluated and copied** before `finally` runs. `x` becomes 2, but the value `1` was already captured. Same in Java for primitives.
>
> ⚠️ **But with a reference type, mutation IS visible:**
> ```csharp
> static List<int> Test()
> {
>     var list = new List<int> { 1 };
>     try { return list; }
>     finally { list.Add(2); }
> }
> // caller sees: [1, 2]  ← the reference was copied, the object was mutated
> ```

---

## Q5. `struct` — assignment copies

```csharp
using System;

struct PointS { public int X; }
class   PointC { public int X; }

class Program
{
    static void Main()
    {
        PointS s1 = new PointS { X = 1 };
        PointS s2 = s1;
        s2.X = 99;

        PointC c1 = new PointC { X = 1 };
        PointC c2 = c1;
        c2.X = 99;

        Console.WriteLine($"{s1.X}, {c1.X}");
    }
}
```

### ✅ Output

```
1, 99
```

### The rule

> **"A `struct` is a value type — assignment copies the whole value. A `class` is a reference type — assignment copies only the reference, so both variables see the same object."**

Java has **no** user-defined value types (until Valhalla ships), so there is no Java equivalent to get wrong — which is exactly why interviewers use it on Java converts.

### 🔥 The follow-up

**"Where else does that copy happen?"**
> **Everywhere a value moves:** passing to a method, returning from a method, storing in a field, boxing, and — the killer — **reading from a property or a `List<T>` indexer** (see Q11).

---

## Q6. `int.MaxValue + 1` — constants overflow at **compile** time

```csharp
using System;

class Program
{
    static void Main()
    {
        Console.WriteLine(int.MaxValue + 1);
    }
}
```

### ✅ Output

```
❌ COMPILE ERROR CS0220: The operation overflows at compile time in checked mode
```

### The rule

> **"Constant expressions are always evaluated in a *checked* context at compile time. Runtime arithmetic is *unchecked* by default and wraps silently."**

```csharp
int max = int.MaxValue;
Console.WriteLine(max + 1);              // -2147483648  ← wraps silently, no error

Console.WriteLine(unchecked(int.MaxValue + 1));   // -2147483648  ← explicitly allowed

checked
{
    int m = int.MaxValue;
    Console.WriteLine(m + 1);            // 💥 OverflowException at runtime
}
```

**Java prints `-2147483648` for all of these** — Java has no `checked`/`unchecked` and never errors on overflow.

### 🔥 The follow-up

**"How would you make overflow throw across a whole project?"**
> Set `<CheckForOverflowUnderflow>true</CheckForOverflowUnderflow>` in the `.csproj`. That flips the default for the entire assembly. Financial code should almost always turn it on.

---

## Q7. `foreach` closure capture — C# 5 changed the answer

```csharp
using System;
using System.Collections.Generic;

class Program
{
    static void Main()
    {
        var actions = new List<Action>();

        for (int i = 0; i < 3; i++)
            actions.Add(() => Console.Write(i));

        Console.WriteLine();

        foreach (var x in new[] { 0, 1, 2 })
            actions.Add(() => Console.Write(x));

        foreach (var a in actions) a();
    }
}
```

### ✅ Output

```
333012
```

### The rule

> **"A `for` loop has ONE loop variable for the whole loop, so all three lambdas capture the same `i` — which is 3 when they finally run. A `foreach` loop (C# 5 and later) creates a FRESH variable each iteration, so each lambda captures its own copy."**

- **Before C# 5**, `foreach` also printed `222` — this was one of the most complained-about behaviours in the language, and Microsoft made a **breaking change** to fix it.
- **Java** has no equivalent problem because it forces captured variables to be *effectively final* — the `for` version simply **doesn't compile** in Java.

### 🔥 The follow-ups

**Follow-up 1 — "Fix the `for` version."**
```csharp
for (int i = 0; i < 3; i++)
{
    int copy = i;                       // fresh variable per iteration
    actions.Add(() => Console.Write(copy));
}
// now prints 012
```

**Follow-up 2 — "Where does this bite you in real code?"**
> Registering event handlers or `Task`s inside a `for` loop:
> ```csharp
> for (int i = 0; i < 3; i++)
>     tasks.Add(Task.Run(() => Process(i)));   // 🐛 all three may process 3
> ```

---

## Q8. Optional parameter defaults come from the **compile-time** type

```csharp
using System;

class Base    { public virtual void Show(int x = 1) => Console.WriteLine($"Base {x}"); }
class Derived : Base { public override void Show(int x = 2) => Console.WriteLine($"Derived {x}"); }

class Program
{
    static void Main()
    {
        Base b = new Derived();
        b.Show();
    }
}
```

### ✅ Output

```
Derived 1
```

### The rule

> **"The *body* is chosen at runtime by the actual type (`Derived`). The *default value* is baked in at compile time from the static type (`Base`). You get `Derived`'s code with `Base`'s default."**

This is the single most surprising line in C#. Java has no optional parameters at all, so there's nothing to transfer.

### 🔥 The follow-ups

**Follow-up 1 — "What's the versioning danger?"**
> Default values are **compiled into the caller's assembly**. If a library changes `void Log(int level = 1)` to `= 2`, every caller keeps passing `1` until they are **recompiled**. Optional parameters are a **binary-compatibility hazard** in public APIs.

**Follow-up 2 — "What would you do instead in a public API?"**
> Use **overloads** — they're resolved against the callee's real signature:
> ```csharp
> public void Log() => Log(1);
> public void Log(int level) { }
> ```

---

> ✅ **Section A checkpoint.** If you can state the eight rules above cold — non-virtual by default, field-initializers-before-base-ctor, no `return` in `finally`, structs copy, constants are checked, `for` vs `foreach` capture, optional-parameter defaults, and `dynamic` — you already handle most of a .NET output round.

---

# B — Value types, reference types & boxing

## Q9. Boxing takes a snapshot

```csharp
int i = 5;
object o = i;      // boxing — copies the value onto the heap
i = 10;
Console.WriteLine(o);
```

### ✅ Output
```
5
```

### The rule
> **"Boxing copies the value into a new heap object. The box has no link back to the variable."**

### 🔥 Follow-up — "And unboxing?"
```csharp
object o = 5;
int a = (int)o;      // ✅ fine
long b = (long)o;    // 💥 InvalidCastException at runtime
```
> **Unboxing must name the *exact* original type.** You cannot unbox-and-widen in one step. Correct form: `long b = (int)o;` or `Convert.ToInt64(o)`.

---

## Q10. Two boxes are never `==`

```csharp
object a = 5;
object b = 5;
Console.WriteLine(a == b);
Console.WriteLine(a.Equals(b));
```

### ✅ Output
```
False
True
```

### The rule
> **"`==` on two `object` variables is *reference* comparison — boxing created two different objects. `Equals` is virtual and dispatches to `Int32.Equals`, which compares values."**

### 🔥 Follow-up — "And if they were `int` variables?"
```csharp
int a = 5, b = 5;
Console.WriteLine(a == b);   // True — no boxing, numeric == is a value comparison
```
> The **compile-time type decides which `==` is used**. This is the C# analogue of Java's `Integer` cache trap — except C# has no cache, so the answer is `False` for *every* value, not just outside `-128..127`.

---

## Q11. Mutating a struct inside a `List<T>` vs an array

```csharp
struct Point { public int X; }

var list  = new List<Point> { new Point { X = 1 } };
var array = new Point[]     { new Point { X = 1 } };

array[0].X = 99;      // line A
list[0].X  = 99;      // line B
```

### ✅ Output
```
❌ COMPILE ERROR on line B — CS1612:
   Cannot modify the return value of 'List<Point>.this[int]' because it is not a variable
```
*Line A compiles and works fine.*

### The rule
> **"An array indexer returns a *reference* to the slot, so you can assign into it. A `List<T>` indexer is a *property* — it returns a **copy** — and assigning to a copy is meaningless, so the compiler blocks it."**

### 🔥 Follow-up — "How do you actually update it?"
```csharp
var p = list[0];
p.X = 99;
list[0] = p;          // read-modify-write

// …or just don't do this. Make the struct immutable:
readonly struct Point { public int X { get; init; } }
```
> **The real answer interviewers want:** *"Mutable structs are a design smell. Microsoft's own guidance is to make structs immutable — this compile error is the language pushing back."*

---

## Q12. A `readonly` struct field silently mutates a copy

```csharp
using System;

struct Counter
{
    public int Count;
    public void Increment() => Count++;
}

class Program
{
    private readonly Counter _readonlyField = new Counter();
    private          Counter _normalField   = new Counter();

    static void Main()
    {
        var p = new Program();
        p._readonlyField.Increment();
        p._normalField.Increment();
        Console.WriteLine($"{p._readonlyField.Count}, {p._normalField.Count}");
    }
}
```

### ✅ Output
```
0, 1
```

### The rule
> **"Calling a method on a `readonly` struct field makes the compiler create a *defensive copy* first — the method mutates the copy and the copy is thrown away. The field never changes."**

### 🔥 Follow-up — "How would you catch this in review?"
> Two ways: mark the struct `readonly struct` (then a mutating method won't compile at all), or enable the analyzer for **CA1815 / defensive-copy** warnings. Mention that `readonly struct` also *removes* the hidden copies, so it's a performance win too.

---

## Q13. Passing a reference type "by reference"?

```csharp
using System;

class Program
{
    static void Modify(int[] arr)
    {
        arr[0] = 99;                  // mutation — visible to caller
        arr = new int[] { 7, 7, 7 };  // reassignment — NOT visible
        arr[1] = 42;
    }

    static void Main()
    {
        int[] a = { 1, 2, 3 };
        Modify(a);
        Console.WriteLine(string.Join(",", a));
    }
}
```

### ✅ Output
```
99,2,3
```

### The rule
> **"C# is *always* pass-by-value. For a reference type, the *reference* is passed by value — so mutating the object is visible, but reassigning the parameter is not."**

Identical to Java. Say *"same as Java"* — but be ready for the next question:

### 🔥 Follow-up — "Can C# do what Java can't here?"
```csharp
static void Modify(ref int[] arr) => arr = new int[] { 7, 7, 7 };

int[] a = { 1, 2, 3 };
Modify(ref a);
Console.WriteLine(string.Join(",", a));   // 7,7,7
```
> **Yes — `ref`.** It passes the *variable itself*, so reassignment is visible. Java has no equivalent. Also mention `out` (must be assigned before return, doesn't need initialising by the caller) and `in` (pass-by-reference, read-only, avoids copying large structs).

---

## Q14. `default` for struct vs class

```csharp
struct S { public int X; public string Name; }
class  C { public int X; public string Name; }

S s = default;
C c = default;

Console.WriteLine(s.X);
Console.WriteLine(s.Name ?? "null");
Console.WriteLine(c == null);
```

### ✅ Output
```
0
null
True
```

### The rule
> **"`default(struct)` is a real instance with every field zeroed — you can use it. `default(class)` is `null` — touching it throws."**

### 🔥 Follow-up — "So `new S()` and `default(S)` are the same?"
> They were identical before C# 10. Since **C# 10** a struct may declare an explicit parameterless constructor, and then `new S()` runs it while `default(S)` still just zeroes memory — they can differ. Also note **`default(S)` bypasses field initializers**, which is why struct field initializers were illegal for so long.

---

## Q15. Struct equality without an override

```csharp
struct P { public int X, Y; }

var a = new P { X = 1, Y = 2 };
var b = new P { X = 1, Y = 2 };
Console.WriteLine(a.Equals(b));
Console.WriteLine(a == b);
```

### ✅ Output
```
True
❌ COMPILE ERROR CS0019 on the second line:
   Operator '==' cannot be applied to operands of type 'P' and 'P'
```

### The rule
> **"`ValueType.Equals` gives structs *value* equality for free. But `==` is NOT generated — you must overload it yourself."**

### 🔥 Follow-up — "Any problem with the free `Equals`?"
> Yes — for a struct containing any reference-type field, the default `Equals` falls back to **reflection**, which is dramatically slow. Always override `Equals`, `GetHashCode` and `==`/`!=` on a struct you use in hot paths or as a dictionary key. (Or use a `record struct`, which generates all of it.)

---

## Q16. Interfaces box structs

```csharp
using System;

interface ICounter { void Increment(); int Value { get; } }

struct Counter : ICounter
{
    public int Value { get; private set; }
    public void Increment() => Value++;
}

class Program
{
    static void Main()
    {
        Counter c = new Counter();
        c.Increment();
        c.Increment();

        ICounter i = c;          // boxing!
        i.Increment();

        Console.WriteLine($"{c.Value}, {i.Value}");
    }
}
```

### ✅ Output
```
2, 3
```

### The rule
> **"Assigning a struct to an interface variable BOXES it. `i` is a separate heap copy — from that point the two evolve independently."**

### 🔥 Follow-up — "Where does this cost you in production?"
> Any `List<IShape>` of structs, any `foreach` over `IEnumerable` of a struct enumerator, any LINQ over a struct collection — each boxes and allocates. It's the standard answer to *"why did adding an interface slow down my hot loop?"*

---

# C — `virtual`, `override`, `new` & interfaces

## Q17. `override` vs `new` side by side

```csharp
using System;

class A { public virtual void Show() => Console.WriteLine("A"); }
class B : A { public override void Show() => Console.WriteLine("B"); }
class C : A { public new      void Show() => Console.WriteLine("C"); }

class Program
{
    static void Main()
    {
        A ab = new B();  ab.Show();
        A ac = new C();  ac.Show();
        C cc = new C();  cc.Show();
    }
}
```

### ✅ Output
```
B
A
C
```

### The rule
> **"`override` replaces the slot in the vtable — dynamic binding. `new` creates a brand-new, unrelated method — binding follows the *compile-time* type of the variable."**

### 🔥 Follow-up — "One sentence on when `new` is legitimate?"
> Almost never in your own code. Its real purpose is **versioning**: a base class you don't own adds a method with your method's name, and `new` lets you keep compiling without accidentally overriding it.

---

## Q18. `new` cannot be un-hidden by a further override

```csharp
using System;

class A { public virtual void M() => Console.WriteLine("A.M"); }
class B : A { public new virtual void M() => Console.WriteLine("B.M"); }
class C : B { public override void M() => Console.WriteLine("C.M"); }

A a = new C();
a.M();
```

### ✅ Output
```
A.M
```

### The rule
> **"`B.M` started a *new* virtual chain. `C.M` overrides `B`'s chain, not `A`'s. Through an `A` reference you still reach the original `A.M`."**

### 🔥 Follow-up — "And through a `B` reference?"
```csharp
B b = new C();
b.M();      // C.M
```

---

## Q19. `sealed override`

```csharp
class A { public virtual void M() { } }
class B : A { public sealed override void M() { } }
class C : B { public override void M() { } }
```

### ✅ Output
```
❌ COMPILE ERROR CS0239: 'C.M()' cannot override inherited member 'B.M()'
   because it is sealed
```

### The rule
> **"`sealed override` stops the virtual chain at that class. It's C#'s `final` on a method."**

### 🔥 Follow-up — "Can `C` still declare an `M`?"
> Yes, with `public new void M()` — hiding is always allowed, it's *overriding* that's sealed off.

---

## Q20. Interface re-implementation — the nastiest one in this section

```csharp
using System;

interface IShape { void Draw(); }

class Square : IShape
{
    public void Draw() => Console.WriteLine("Square.Draw");
}

class ColoredSquare : Square, IShape          // ← note: IShape is listed AGAIN
{
    public new void Draw() => Console.WriteLine("ColoredSquare.Draw");
}

class Program
{
    static void Main()
    {
        IShape  i = new ColoredSquare();  i.Draw();
        Square  s = new ColoredSquare();  s.Draw();
    }
}
```

### ✅ Output
```
ColoredSquare.Draw
Square.Draw
```

### The rule
> **"Re-listing the interface makes the derived class *re-implement* it — the interface map is rebuilt and now points at `ColoredSquare.Draw`. The class-level call still uses hiding rules and reaches `Square.Draw`."**

### 🔥 Follow-up — "And if `ColoredSquare` did NOT list `IShape` again?"
```csharp
class ColoredSquare : Square             // IShape not re-listed
{
    public new void Draw() => Console.WriteLine("ColoredSquare.Draw");
}
IShape i = new ColoredSquare();  i.Draw();   // Square.Draw  ← both calls now print Square.Draw
```
> **That's the whole trap:** the same class body behaves differently depending on one word in the base list. The correct fix is always `virtual`/`override`, never `new`.

---

## Q21. Explicit interface implementation is invisible on the class

```csharp
using System;

interface ILogger { void Log(); }

class FileLogger : ILogger
{
    void ILogger.Log() => Console.WriteLine("ILogger.Log");
}

class Program
{
    static void Main()
    {
        var f = new FileLogger();
        f.Log();
    }
}
```

### ✅ Output
```
❌ COMPILE ERROR CS1061: 'FileLogger' does not contain a definition for 'Log'
```

### The rule
> **"An explicit interface implementation has no access modifier and is NOT a member of the class — it's only reachable through the interface."**

```csharp
((ILogger)f).Log();          // ✅ ILogger.Log
ILogger l = new FileLogger();
l.Log();                     // ✅ ILogger.Log
```

### 🔥 Follow-up — "When would you use it?"
> Two real cases: (1) **name collisions** — a class implementing `IEnumerable<T>` and `IEnumerable` needs two `GetEnumerator`s; (2) **hiding infrastructure** from the public API, e.g. implementing `IDisposable` explicitly so `Dispose` doesn't appear on IntelliSense for a type you want people to use with `using` only.

---

## Q22. Abstract class calling an abstract method

```csharp
using System;

abstract class Shape
{
    protected Shape() { Console.WriteLine($"Area = {GetArea()}"); }
    public abstract double GetArea();
}

class Circle : Shape
{
    private readonly double _r;
    public Circle(double r) => _r = r;
    public override double GetArea() => 3.14 * _r * _r;
}

new Circle(2);
```

### ✅ Output
```
Area = 0
```

### The rule
> **"`_r` is assigned in the *derived constructor body*, which runs AFTER the base constructor. At the time the base ctor calls `GetArea()`, `_r` is still `0`."**

⚠️ Contrast with **Q2**: field *initializers* (`double _r = 2;`) would already have run. Constructor *bodies* have not. Know which half you're in.

### 🔥 Follow-up — "Give a design fix."
> Don't compute in the constructor — make it a lazy property or a `Create` factory that constructs then computes. Also: analyzer **CA2214** ("Do not call overridable methods in constructors") flags exactly this.

---

## Q23. Static methods are never polymorphic

```csharp
using System;

class A { public static void Who() => Console.WriteLine("A"); }
class B : A { public static new void Who() => Console.WriteLine("B"); }

A.Who();
B.Who();
// A a = new B(); a.Who();   ← what about this?
```

### ✅ Output
```
A
B
```
And the commented line is a **compile error CS0176**: *"Member 'A.Who()' cannot be accessed with an instance reference; qualify it with a type name instead."*

### The rule
> **"Static members are bound to the *type*, never to an instance. C# goes further than Java and forbids calling a static member through an instance reference at all."**

**In Java, `a.Who()` compiles** (with a warning) and prints `A`. C# rejects it outright — a small but well-loved gotcha for Java converts.

---

## Q24. Overload resolution beats inheritance

```csharp
using System;

class A { public void F(int x)  => Console.WriteLine("A.F(int)"); }
class B : A { public void F(long x) => Console.WriteLine("B.F(long)"); }

new B().F(5);
```

### ✅ Output
```
B.F(long)
```

### The rule
> **"C# searches for applicable methods starting at the MOST DERIVED type and stops at the first class that has *any* applicable candidate. `B` has one (`F(long)` via implicit widening), so `A.F(int)` is never even considered."**

⚠️ **This is the opposite of Java**, which merges the whole hierarchy into one candidate set and would pick the exact match `A.F(int)`. Same code, different language, different answer — a favourite of interviewers who know both.

### 🔥 Follow-up — "So how do I reach `A.F(int)`?"
```csharp
((A)new B()).F(5);   // A.F(int)
```

---

# D — Initialization order

## Q25. The full order, one program

```csharp
using System;

class Base
{
    static Base()  => Console.WriteLine("1. Base static ctor");
    public static int BaseStaticField = Print("0. Base static field");
    public int BaseInstanceField      = Print("3. Base instance field");
    public Base()  => Console.WriteLine("4. Base ctor body");
    static int Print(string s) { Console.WriteLine(s); return 0; }
}

class Derived : Base
{
    public int DerivedInstanceField = Print2("2. Derived instance field");
    public Derived() => Console.WriteLine("5. Derived ctor body");
    static int Print2(string s) { Console.WriteLine(s); return 0; }
}

class Program { static void Main() => new Derived(); }
```

### ✅ Output
```
2. Derived instance field
0. Base static field
1. Base static ctor
3. Base instance field
4. Base ctor body
5. Derived ctor body
```

### The rule — memorize this ladder

```
① Derived instance field initializers      ← FIRST (C#-specific!)
② Base static field initializers  }  triggered on first use of Base
③ Base static constructor         }
④ Base instance field initializers
⑤ Base constructor body
⑥ Derived constructor body                 ← LAST
```

### 🔥 Follow-up — "Why do the statics appear in the middle?"
> A static constructor runs **lazily, exactly once, on first use of the type**. Here the first use of `Base` is the implicit `base()` call — which happens *after* the derived field initializers. Move the trigger and the statics move with it.

---

## Q26. Static constructor runs exactly once

```csharp
using System;

class Config
{
    public static int Count;
    static Config()  { Count++; Console.WriteLine("static ctor"); }
    public Config()  { }
}

new Config(); new Config(); new Config();
Console.WriteLine(Config.Count);
```

### ✅ Output
```
static ctor
1
```

### The rule
> **"A static constructor runs at most once per AppDomain, on first instantiation *or* first static-member access — whichever comes first — and the CLR guarantees it is thread-safe."**

### 🔥 Follow-up — "So a static ctor is a valid singleton?"
> Yes — the classic **type-initializer singleton** is thread-safe with no locking:
> ```csharp
> public sealed class Singleton
> {
>     private static readonly Singleton _instance = new Singleton();
>     static Singleton() { }        // ← disables 'beforefieldinit', forcing lazy init
>     private Singleton() { }
>     public static Singleton Instance => _instance;
> }
> ```
> Mentioning **`beforefieldinit`** — that without an explicit static ctor the CLR may initialize the type *earlier* than you expect — is a strong senior signal.

---

## Q27. `const` vs `static readonly`

```csharp
// Assembly Lib.dll
public class Config
{
    public const           int MaxUsers = 100;
    public static readonly int MaxItems = 100;
}

// Assembly App.exe — compiled against Lib v1, Lib later rebuilt with 200
Console.WriteLine(Config.MaxUsers);
Console.WriteLine(Config.MaxItems);
```

### ✅ Output (after Lib is changed to 200 and only Lib is recompiled)
```
100      ← stale!
200
```

### The rule
> **"`const` is inlined into the *calling* assembly at compile time. `static readonly` is a real field read at runtime. Changing a public `const` in a library is a binary-breaking change."**

Same trap as optional parameters (Q8) — **compile-time baking**.

### 🔥 Follow-up — "When is `const` still right?"
> Only for values that are true forever and are part of the type's identity — `Math.PI`, `int.MaxValue`, `""`. Anything configurable should be `static readonly`.

---

## Q28. Field initializers can't reference `this`

```csharp
class Program
{
    private int a = 5;
    private int b = a + 1;
}
```

### ✅ Output
```
❌ COMPILE ERROR CS0236: A field initializer cannot reference the
   non-static field, method, or property 'Program.a'
```

### The rule
> **"Instance field initializers run before the object is fully formed, so they cannot reference *any* instance member — including each other. Order is not guaranteed to be usable."**

**Java allows this** (`int b = a + 1;` compiles and works if `a` is declared first). Another Java reflex to unlearn.

### 🔥 Follow-up — "Fix?"
> Move it into the constructor: `public Program() { b = a + 1; }`.

---

## Q29. Static field initializer order

```csharp
using System;

class Program
{
    static int A = B + 1;
    static int B = 10;
    static void Main() => Console.WriteLine($"A={A}, B={B}");
}
```

### ✅ Output
```
A=1, B=10
```

### The rule
> **"Static field initializers run in *textual order*. When `A` is initialized, `B` hasn't run yet, so `B` is still its default `0` — `A` becomes 1."**

Identical behaviour in Java. Swap the two lines and you get `A=11, B=10`.

---

# E — String traps

## Q30. `==` on strings is a value comparison

```csharp
string a = "hello";
string b = "hel" + "lo";
string c = new string("hello".ToCharArray());

Console.WriteLine(a == b);
Console.WriteLine(a == c);
Console.WriteLine((object)a == (object)b);
Console.WriteLine((object)a == (object)c);
```

### ✅ Output
```
True
True
True
False
```

### The rule
> **"`string` overloads `==` to compare *contents* — unlike Java, where `==` on strings is always reference comparison. Casting to `object` removes the overload and gives you back reference comparison."**

Line 3 is `True` because `"hel" + "lo"` is folded by the compiler into the literal `"hello"` and **interned** — same object. Line 4 is `False` because `new string(...)` allocates at runtime and is not interned.

### 🔥 The follow-ups

**Follow-up 1 — "Make line 4 print `True`."**
```csharp
Console.WriteLine((object)a == (object)string.Intern(c));   // True
```

**Follow-up 2 — "What about runtime concatenation?"**
```csharp
string x = "hel";
string y = x + "lo";                       // built at runtime, not interned
Console.WriteLine((object)a == (object)y); // False
Console.WriteLine(a == y);                 // True
```

**Follow-up 3 — "So should I ever use `==` on strings?"**
> Yes, for ordinal equality it's fine and fast. But for anything **culture-sensitive or case-insensitive**, use `string.Equals(a, b, StringComparison.OrdinalIgnoreCase)` — the explicit comparison type is what reviewers look for.

---

## Q31. Strings are immutable

```csharp
string s = "hello";
s.ToUpper();
s.Replace("h", "H");
Console.WriteLine(s);
```

### ✅ Output
```
hello
```

### The rule
> **"Every `string` method RETURNS a new string; none mutate. Ignoring the return value does nothing."**

### 🔥 Follow-up — "Fix it, then tell me about the loop version."
```csharp
s = s.ToUpper();      // HELLO
```
> And in a loop, `s += x` allocates a **new string every iteration** — O(n²) copying. Use `StringBuilder`. Interviewers love the follow-up *"at roughly what size does StringBuilder win?"* — the honest answer is *"a few concatenations in a loop; for 3–4 fixed concatenations the compiler turns `+` into a single `string.Concat` call and `StringBuilder` is actually slower."*

---

## Q32. `null` inside string concatenation

```csharp
string s = null;
Console.WriteLine("Value: " + s + "!");
Console.WriteLine($"Value: {s}!");
Console.WriteLine(s.Length);
```

### ✅ Output
```
Value: !
Value: !
💥 NullReferenceException
```

### The rule
> **"Concatenation and interpolation treat `null` as `string.Empty` — they never throw. Calling a *member* on a `null` string does throw."**

### 🔥 Follow-up — "And `Console.WriteLine(s)` on its own?"
> Prints an **empty line**, no exception — `Console.WriteLine(string)` handles `null`.

---

## Q33. `1 + 2 + "3" + 4 + 5`

```csharp
Console.WriteLine(1 + 2 + "3" + 4 + 5);
Console.WriteLine("1" + 2 + 3);
```

### ✅ Output
```
3345
123
```

### The rule
> **"`+` is left-associative. Before the first string appears you're doing arithmetic; from the first string onward everything becomes concatenation."**

```
1 + 2      → 3         (arithmetic)
3 + "3"    → "33"      (concat)
"33" + 4   → "334"
"334" + 5  → "3345"
```

Identical to Java — say so, it's a free point.

---

## Q34. `string.Empty`, `""` and `null`

```csharp
string a = "";
string b = string.Empty;
string c = null;

Console.WriteLine(a == b);
Console.WriteLine(a == c);
Console.WriteLine(string.IsNullOrEmpty(c));
Console.WriteLine(string.IsNullOrWhiteSpace("   "));
Console.WriteLine(c?.Length ?? -1);
```

### ✅ Output
```
True
False
True
True
-1
```

### The rule
> **"`""` and `string.Empty` are the same interned instance. `null` is not a string at all. `?.` short-circuits to `null`, and `??` supplies the fallback."**

---

## Q35. Comparing with `Equals` vs `CompareTo`

```csharp
string a = "STRASSE";
string b = "STRASSE";

Console.WriteLine(a.Equals(b));
Console.WriteLine(string.Equals("i", "I", StringComparison.OrdinalIgnoreCase));
Console.WriteLine("apple".CompareTo("Apple"));
```

### ✅ Output
```
True
True
-1        (on .NET Core / .NET 5+ with the ICU collation; culture-dependent)
```

### The rule
> **"`Equals` without a `StringComparison` is ordinal. `CompareTo` is *culture-sensitive*, so its result can differ between machines and .NET versions."**

### 🔥 Follow-up — "What's the safe habit?"
> **Always pass an explicit `StringComparison`.** Use `Ordinal` for identifiers, keys and paths; `CurrentCulture` only for text shown to a human. This is a real production-bug class (the famous "Turkish I" problem: in `tr-TR`, `"i".ToUpper()` is `"İ"`, not `"I"`).

---

## Q36. `switch` on a string, and no fall-through

```csharp
string s = "a";
switch (s)
{
    case "a":
        Console.WriteLine("A");
    case "b":
        Console.WriteLine("B");
        break;
}
```

### ✅ Output
```
❌ COMPILE ERROR CS0163: Control cannot fall through from one case label to another
```

### The rule
> **"C# forbids implicit fall-through — every non-empty `case` must end with `break`, `return`, `throw` or `goto case`. Java allows fall-through and it's a classic bug source."**

### 🔥 Follow-up — "How do you share a body between cases?"
```csharp
case "a":
case "b":                       // ✅ empty case labels CAN stack
    Console.WriteLine("A or B");
    break;

// or explicitly:
case "a": Console.WriteLine("A"); goto case "b";
case "b": Console.WriteLine("B"); break;
```

---

# F — Numbers & operators

## Q37. `i = i++`

```csharp
int i = 0;
i = i++;
Console.WriteLine(i);

int j = 0;
j = ++j;
Console.WriteLine(j);
```

### ✅ Output
```
0
1
```

### The rule
> **"`i++` returns the OLD value, then increments. The assignment then writes that old value back over the increment. `++j` increments first and returns the new value."**

Identical to Java. The trace:

```
i = i++      →   temp = i (0);  i = i + 1 (i is 1);  i = temp (i is 0)
```

### 🔥 Follow-up — "And `i = i++ + ++i;` starting from 1?"
```csharp
int i = 1;
i = i++ + ++i;      // (1) + (3) = 4
Console.WriteLine(i);   // 4
```
> Left to right: `i++` yields 1 (i becomes 2), `++i` makes i 3 and yields 3, sum 4 is assigned. **Say you'd never write this in real code** — that comment scores points.

---

## Q38. Integer division and modulo with negatives

```csharp
Console.WriteLine(5 / 2);
Console.WriteLine(-5 / 2);
Console.WriteLine(-5 % 2);
Console.WriteLine(5.0 / 2);
```

### ✅ Output
```
2
-2
-1
2.5
```

### The rule
> **"Integer division truncates *toward zero* (not toward negative infinity), and `%` takes the sign of the *dividend*."**

Same as Java. Contrast with Python, where `-5 // 2` is `-3`.

---

## Q39. Division by zero — int vs double

```csharp
int a = 0;
double b = 0;

Console.WriteLine(10.0 / 0);
Console.WriteLine(-10.0 / 0);
Console.WriteLine(0.0 / 0);
Console.WriteLine(10 / a);
Console.WriteLine(10 / 0);
```

### ✅ Output
```
∞          (prints "Infinity" on .NET Framework, "∞" on .NET Core 3.0+)
-∞
NaN
💥 DivideByZeroException
❌ COMPILE ERROR CS0020: Division by constant zero
```

### The rule
> **"Floating-point division by zero follows IEEE 754 and produces Infinity/NaN. Integer division by zero throws. Integer division by a *literal* zero doesn't even compile."**

### 🔥 Follow-up — "What is `double.NaN == double.NaN`?"
```csharp
Console.WriteLine(double.NaN == double.NaN);          // False
Console.WriteLine(double.NaN.Equals(double.NaN));     // True  ← !
Console.WriteLine(double.IsNaN(double.NaN));          // True
```
> **`==` follows IEEE 754 (NaN is unequal to everything, including itself). `Equals` deliberately breaks that so that `NaN` works as a dictionary key and sorts consistently.** This inconsistency is a favourite closing question.

---

## Q40. Floating-point precision

```csharp
Console.WriteLine(0.1 + 0.2);
Console.WriteLine(0.1 + 0.2 == 0.3);
Console.WriteLine(0.1m + 0.2m == 0.3m);
Console.WriteLine((0.1f + 0.2f) == 0.3f);
```

### ✅ Output
```
0.30000000000000004    (.NET Core 3.0+; older .NET Framework printed 0.3)
False
True
True
```

### The rule
> **"`double`/`float` are binary floating point — 0.1 and 0.2 have no exact binary representation. `decimal` is base-10 with 28–29 significant digits, so it represents them exactly."**

The `float` line is `True` only because `float`'s lower precision happens to round both sides to the same value — *"accidentally right, still wrong to rely on."*

### 🔥 Follow-up — "Rule for choosing?"
> **`decimal` for money and anything a human will audit** (it's also ~10× slower and can't do transcendental math). **`double` for science, graphics and measurement.** Never compare floats with `==` — use `Math.Abs(a - b) < epsilon`.

---

## Q41. Compound assignment hides a cast

```csharp
byte b = 10;
b = b + 5;
b += 5;
```

### ✅ Output
```
❌ COMPILE ERROR CS0266 on line 2: Cannot implicitly convert type 'int' to 'byte'
   (line 3 compiles fine)
```

### The rule
> **"`byte + int` promotes to `int`, and `int` won't implicitly narrow back to `byte`. But `b += 5` contains an *implicit cast* — the spec defines `b += x` as `b = (byte)(b + x)`."**

Identical to Java. And the sting:

```csharp
byte b = 250;
b += 10;
Console.WriteLine(b);   // 4  ← silently wraps, no warning
```

---

## Q42. `char` arithmetic

```csharp
Console.WriteLine('a' + 1);
Console.WriteLine((char)('a' + 1));
Console.WriteLine('a' + 'b');
Console.WriteLine("a" + 'b');
```

### ✅ Output
```
98
b
195
ab
```

### The rule
> **"`char` implicitly widens to `int` in arithmetic. Only an explicit cast back to `char` gives you a character. But `string + char` is concatenation, because `+` with a string operand always concatenates."**

Same as Java.

---

## Q43. Ternary operator type unification

```csharp
Console.WriteLine(true ? 1 : 2.0);
Console.WriteLine(true ? 1 : "two");
object o = true ? (object)1 : "two";
Console.WriteLine(o);
```

### ✅ Output
```
1
❌ COMPILE ERROR CS0173: Type of conditional expression cannot be determined
   because there is no implicit conversion between 'int' and 'string'
1
```

### The rule
> **"A ternary has ONE static type, computed from both branches even though only one runs. `int` widens to `double`, so the whole expression is `double` — and `1.0.ToString()` is `\"1\"`. `int` and `string` have no common type, so it doesn't compile."**

### 🔥 Follow-up — "Then why does `1` print, not `1.0`?"
> Because `double.ToString()` uses the shortest round-trippable form — `1.0` renders as `"1"`. The value **is** a `double`; only its rendering hides it. Prove it with `(true ? 1 : 2.0).GetType()` → `System.Double`.

---

## Q44. `++` on a property vs a field

```csharp
using System;

class Counter
{
    private int _c;
    public int Value { get => _c; set => _c = value; }
}

var c = new Counter();
c.Value++;
Console.WriteLine(c.Value);
```

### ✅ Output
```
1
```

### The rule
> **"`c.Value++` on a property compiles to `set_Value(get_Value() + 1)` — a get, an add, and a set. It works, but it is NOT atomic and it hides two method calls."**

### 🔥 Follow-up — "Why does that matter?"
> Two threads doing `c.Value++` can lose an update — the read-modify-write is not atomic. The fix is `Interlocked.Increment(ref _c)`, which needs a **field**, not a property. This is the classic *"why is my counter wrong under load?"* question.

---

## Q45. `as` vs cast vs `is`

```csharp
object o = "hello";

string s1 = o as string;
int?   n1 = o as int?;
Console.WriteLine(s1 ?? "null");
Console.WriteLine(n1?.ToString() ?? "null");

Console.WriteLine(o is string);
Console.WriteLine(null is object);

int i = (int)o;
```

### ✅ Output
```
hello
null
True
False
💥 InvalidCastException on the last line
```

### The rule
> **"`as` returns `null` on failure (reference and nullable types only) — a cast throws. `is` is a type test that is ALWAYS false for `null`, because `null` has no type."**

### 🔥 Follow-up — "Which do you use and why?"
> The modern form is pattern matching, one test instead of two:
> ```csharp
> if (o is string str) Console.WriteLine(str.Length);
> ```
> Using `is` followed by a cast does the type check **twice** — the pattern form does it once.

---

# G — Nullable & the null operators

## Q46. Both comparisons are false

```csharp
int? a = null;
int? b = 5;

Console.WriteLine(a > b);
Console.WriteLine(a < b);
Console.WriteLine(a >= b);
Console.WriteLine(a == b);
Console.WriteLine(a != b);
```

### ✅ Output
```
False
False
False
False
True
```

### The rule
> **"Lifted RELATIONAL operators (`< > <= >=`) return `false` if either operand is null — so `a > b` and `a <= b` can BOTH be false, breaking the usual logic. Lifted EQUALITY operators do work normally: null equals null, null differs from anything else."**

### 🔥 Follow-up — "Where does that cause a real bug?"
> `if (!(a > b))` is **not** the same as `if (a <= b)` when nulls are involved. Any validation written as a negated comparison silently changes meaning. Always null-check first:
> ```csharp
> if (a.HasValue && b.HasValue && a > b) { }
> ```

---

## Q47. Nullable arithmetic swallows everything

```csharp
int? a = null;
int? b = 5;

Console.WriteLine(a + b);
Console.WriteLine((a + b) ?? -1);
Console.WriteLine(a.GetValueOrDefault());
Console.WriteLine(a.Value);
```

### ✅ Output
```
           (blank line — the result is null)
-1
0
💥 InvalidOperationException: Nullable object must have a value
```

### The rule
> **"Any lifted arithmetic with a null operand yields null — nulls propagate through the whole expression. `.Value` on a null throws `InvalidOperationException`, NOT `NullReferenceException`."**

Naming the *right* exception is worth extra credit.

---

## Q48. `??` and `?.` short-circuit further than you think

```csharp
string s = null;

Console.WriteLine(s?.Length ?? -1);
Console.WriteLine(s?.ToUpper().Trim().Length ?? -1);

int[] arr = null;
Console.WriteLine(arr?[0] ?? -1);
```

### ✅ Output
```
-1
-1
-1
```

### The rule
> **"`?.` short-circuits the ENTIRE remaining chain, not just the next member. Once it hits null, `.ToUpper().Trim().Length` is never evaluated and the whole expression is null."**

### 🔥 Follow-up — "What's the type of `s?.Length`?"
> **`int?`**, not `int` — `?.` on a value-typed member always lifts the result to nullable. That's why `?? -1` compiles at all.

---

## Q49. `??=` and evaluation order

```csharp
using System;

class Program
{
    static int _calls;
    static string Get() { _calls++; return "value"; }

    static void Main()
    {
        string a = null;
        a ??= Get();
        a ??= Get();
        Console.WriteLine($"{a}, calls={_calls}");
    }
}
```

### ✅ Output
```
value, calls=1
```

### The rule
> **"`??=` assigns only if the left side is null, and the right side is NOT evaluated when the left side is non-null — it short-circuits like `&&`."**

### 🔥 Follow-up — "Compare with `a = a ?? Get();`"
> Behaviourally identical, but `??=` avoids re-assigning `a` to itself — which matters for properties with side-effecting setters and for thread safety.

---

## Q50. Nullable reference types are compile-time only

```csharp
#nullable enable

string NotNull(string? input) => input!;   // ! = null-forgiving

string result = NotNull(null);
Console.WriteLine(result.Length);
```

### ✅ Output
```
💥 NullReferenceException
```
*(plus a compile-time **warning** CS8625 on `NotNull(null)`)*

### The rule
> **"Nullable reference types are a *compile-time analysis* with warnings only. There is no runtime enforcement, and `!` just silences the analyser — it does not check anything."**

### 🔥 Follow-up — "How do you make the warnings bite?"
> `<WarningsAsErrors>nullable</WarningsAsErrors>` (or `<TreatWarningsAsErrors>`) in the `.csproj`. And note the boundary problem: **data from JSON, EF Core, or a non-annotated library can be null regardless of what the type says** — always validate at the edge.

---

# H — `try`/`catch`/`finally` & `using`

## Q51. `finally` always runs

```csharp
using System;

class Program
{
    static void Main()
    {
        try
        {
            Console.WriteLine("try");
            throw new Exception("boom");
        }
        catch (Exception e)
        {
            Console.WriteLine("catch: " + e.Message);
            return;
        }
        finally
        {
            Console.WriteLine("finally");
        }
    }
}
```

### ✅ Output
```
try
catch: boom
finally
```

### The rule
> **"`finally` runs before the method actually returns — even when `catch` contains `return`."**

### 🔥 Follow-up — "Is there anything that skips `finally`?"
> Yes, three things: `Environment.FailFast()`, a **`StackOverflowException`** (uncatchable since .NET 2.0 — the process dies), and killing the process. Also, an unhandled exception on a **background thread** tears down the process without unwinding other threads' `finally` blocks.

---

## Q52. Exception filters run BEFORE the stack unwinds

```csharp
using System;

class Program
{
    static bool Log(Exception e) { Console.WriteLine("filter"); return false; }

    static void Main()
    {
        try
        {
            try { throw new InvalidOperationException("x"); }
            catch (Exception e) when (Log(e)) { Console.WriteLine("inner catch"); }
            finally { Console.WriteLine("inner finally"); }
        }
        catch (Exception) { Console.WriteLine("outer catch"); }
    }
}
```

### ✅ Output
```
filter
inner finally
outer catch
```

### The rule
> **"An exception filter (`when`) is evaluated in the FIRST pass, before any `finally` block runs. Only if it returns true does the second pass unwind the stack into that `catch`."**

Note `"filter"` prints before `"inner finally"` — proof of the two-pass model. Java has no exception filters at all.

### 🔥 Follow-up — "What's a filter good for?"
> Two real uses: (1) logging without catching — `catch (Exception e) when (Log(e))` where `Log` returns `false`, so the exception continues with its stack trace **intact**; (2) conditional handling — `catch (SqlException e) when (e.Number == 1205)` for deadlock retries.

---

## Q53. `throw` vs `throw ex`

```csharp
try
{
    try { throw new Exception("original"); }
    catch (Exception ex) { throw ex; }        // ← A
    // catch (Exception)  { throw; }          // ← B
}
catch (Exception e) { Console.WriteLine(e.StackTrace); }
```

### ✅ Output
> **A** prints a stack trace starting at the `throw ex;` line — **everything below it is lost**.
> **B** preserves the original trace all the way to where the exception was created.

### The rule
> **"`throw ex;` RESETS the stack trace. `throw;` rethrows the original exception with its trace intact. Always use bare `throw;`."**

### 🔥 Follow-up — "And if you must wrap it?"
```csharp
catch (Exception ex) { throw new DataException("Failed to load user", ex); }
```
> Pass the original as the **inner exception** — never discard it. Also mention `ExceptionDispatchInfo.Capture(ex).Throw()` for rethrowing a captured exception from a different context (how `await` preserves traces).

---

## Q54. `using` disposal order

```csharp
using System;

class Res : IDisposable
{
    private readonly string _n;
    public Res(string n) { _n = n; Console.WriteLine($"open {_n}"); }
    public void Dispose() => Console.WriteLine($"dispose {_n}");
}

class Program
{
    static void Main()
    {
        using (var a = new Res("A"))
        using (var b = new Res("B"))
        {
            Console.WriteLine("body");
            throw new Exception("boom");
        }
    }
}
```

### ✅ Output
```
open A
open B
body
dispose B
dispose A
Unhandled exception: boom
```

### The rule
> **"`using` compiles to `try/finally`, so disposal happens even on exception — and nested `using`s dispose in REVERSE order, innermost first."**

### 🔥 Follow-up — "What about the `using` *declaration* form?"
```csharp
using var a = new Res("A");
using var b = new Res("B");
// disposed at the end of the enclosing scope, still B then A
```
> Same semantics, less nesting. C# 8+. And the trap: **`using` does nothing for `async` disposal** — you need `await using` with `IAsyncDisposable`, otherwise you can block a thread inside `Dispose`.

---

## Q55. Catch order

```csharp
try { throw new ArgumentNullException("x"); }
catch (Exception e)          { Console.WriteLine("Exception"); }
catch (ArgumentException e)   { Console.WriteLine("ArgumentException"); }
```

### ✅ Output
```
❌ COMPILE ERROR CS0160: A previous catch clause already catches all exceptions
   of this or of a super type ('Exception')
```

### The rule
> **"Catch blocks are tested top-down and must go MOST specific → LEAST specific. The compiler rejects an unreachable catch."**

Same as Java. Correct order:
```csharp
catch (ArgumentNullException) { }   // most specific
catch (ArgumentException)     { }
catch (Exception)             { }   // least specific
```

---

## Q56. Exception from a `finally` block

```csharp
try
{
    try { throw new Exception("original"); }
    finally { throw new Exception("from finally"); }
}
catch (Exception e) { Console.WriteLine(e.Message); }
```

### ✅ Output
```
from finally
```

### The rule
> **"An exception thrown in `finally` REPLACES the in-flight exception, and the original is lost with no inner-exception link."**

### 🔥 Follow-up — "So what's the guidance?"
> **Never let `finally` throw.** In cleanup code, wrap risky calls in their own `try/catch` and log. This is also why `Dispose()` implementations are expected never to throw.

---

# I — Closures, LINQ & iterators

## Q57. LINQ is deferred

```csharp
using System;
using System.Collections.Generic;
using System.Linq;

var numbers = new List<int> { 1, 2, 3 };
var query = numbers.Where(n => n > 1);

numbers.Add(4);

Console.WriteLine(string.Join(",", query));
```

### ✅ Output
```
2,3,4
```

### The rule
> **"LINQ query operators are LAZY — `Where` just builds a description. Nothing executes until you enumerate, and enumeration reads the collection *as it is at that moment*."**

### 🔥 Follow-up — "How do you force it?"
```csharp
var query = numbers.Where(n => n > 1).ToList();   // executes NOW
numbers.Add(4);
Console.WriteLine(string.Join(",", query));       // 2,3
```
> `ToList`, `ToArray`, `Count`, `First`, `Sum`, `Any` — the **terminal** operators execute immediately.

---

## Q58. Double enumeration runs the query twice

```csharp
using System;
using System.Linq;

int calls = 0;
var query = Enumerable.Range(1, 3).Select(x => { calls++; return x * 2; });

Console.WriteLine(query.Count());
Console.WriteLine(query.Sum());
Console.WriteLine($"lambda calls = {calls}");
```

### ✅ Output
```
3
12
lambda calls = 6
```

### The rule
> **"Every enumeration of a deferred query re-executes the whole pipeline. Two terminal operators = two full runs."**

### 🔥 Follow-up — "When is that a production incident?"
> When the source is a **database or an HTTP call** — you fire the query twice. Or when the projection has side effects. Or with `IEnumerable` over a network stream, where the second enumeration throws because the stream is consumed. **Rule: materialize with `ToList()` the moment you need the results more than once.**

---

## Q59. Closure capture in LINQ

```csharp
using System;
using System.Linq;
using System.Collections.Generic;

var funcs = new List<Func<int>>();
for (int i = 0; i < 3; i++) funcs.Add(() => i);

Console.WriteLine(string.Join(",", funcs.Select(f => f())));
```

### ✅ Output
```
3,3,3
```

### The rule
Same as **Q7** — the `for` loop has one `i`, hoisted into a single closure object, and it's `3` by the time the lambdas run.

### 🔥 Follow-up — "How does the compiler actually do this?"
> It generates a **display class** — a hidden compiler-generated class holding the captured variable — and rewrites `i` to be a field on one shared instance. Being able to say "display class" out loud is a strong signal you understand closures rather than memorizing the symptom.

---

## Q60. `First` vs `FirstOrDefault` vs `Single`

```csharp
using System;
using System.Linq;
using System.Collections.Generic;

var empty = new List<int>();
var two   = new List<int> { 1, 1 };

Console.WriteLine(empty.FirstOrDefault());
Console.WriteLine(two.First());
Console.WriteLine(empty.First());
Console.WriteLine(two.Single());
```

### ✅ Output
```
0
1
💥 InvalidOperationException: Sequence contains no elements
💥 InvalidOperationException: Sequence contains more than one element
```

### The rule
> **"`First` throws on empty. `FirstOrDefault` returns `default(T)` — which is `0` for `int`, not `null`. `Single` throws on empty AND on more than one."**

### 🔥 Follow-up — "What's the trap with `FirstOrDefault` on a value type?"
> You **cannot distinguish "not found" from "found the value 0"**. Use `FirstOrDefault()` on a nullable (`Cast<int?>().FirstOrDefault()`) or check `Any()` first. Same trap with `default(DateTime)` giving `01/01/0001` instead of null.

---

## Q61. `yield return` doesn't run until you enumerate

```csharp
using System;
using System.Collections.Generic;

class Program
{
    static IEnumerable<int> Gen()
    {
        Console.WriteLine("start");
        yield return 1;
        Console.WriteLine("middle");
        yield return 2;
        Console.WriteLine("end");
    }

    static void Main()
    {
        Console.WriteLine("before");
        var g = Gen();
        Console.WriteLine("after call");
        foreach (var x in g) Console.WriteLine(x);
    }
}
```

### ✅ Output
```
before
after call
start
1
middle
2
end
```

### The rule
> **"Calling an iterator method executes NOTHING — it just returns a state machine. The body runs one `yield` at a time, driven by `MoveNext()`."**

Notice `"start"` prints **after** `"after call"`.

### 🔥 Follow-up — "So where do you validate arguments in an iterator?"
```csharp
public static IEnumerable<int> Take(IEnumerable<int> src, int n)
{
    if (src == null) throw new ArgumentNullException(nameof(src));   // 🐛 thrown LAZILY
    return Impl(src, n);

    static IEnumerable<int> Impl(IEnumerable<int> s, int c) { foreach (var x in s) yield return x; }
}
```
> **Split it into a non-iterator wrapper + a private iterator**, exactly as shown. Otherwise the `ArgumentNullException` doesn't surface until the caller enumerates — possibly in a completely different stack frame. This is how the BCL implements every LINQ operator.

---

## Q62. Modifying the source while enumerating a LINQ query

```csharp
using System;
using System.Linq;
using System.Collections.Generic;

var list = new List<int> { 1, 2, 3 };
var query = list.Where(x => x > 1);

foreach (var x in query)
{
    Console.WriteLine(x);
    list.Add(99);
}
```

### ✅ Output
```
2
💥 InvalidOperationException: Collection was modified; enumeration operation may not execute
```

### The rule
> **"`List<T>`'s enumerator holds a version stamp. Any structural change bumps it and the next `MoveNext()` throws — the deferred LINQ query is enumerating that same live list."**

The C# analogue of Java's `ConcurrentModificationException`.

### 🔥 Follow-up — "Fixes?"
> Iterate a snapshot (`foreach (var x in query.ToList())`), collect-then-apply, loop backwards with a `for`, or use `ConcurrentBag`/`ConcurrentDictionary` for genuinely concurrent access.

---

## Q63. `OrderBy` is stable, `List.Sort` is not

```csharp
using System;
using System.Linq;
using System.Collections.Generic;

var items = new List<(string Name, int Group)>
{
    ("a", 1), ("b", 1), ("c", 1), ("d", 1), ("e", 1)
};

var linq = items.OrderBy(x => x.Group).Select(x => x.Name);
items.Sort((x, y) => x.Group.CompareTo(y.Group));

Console.WriteLine(string.Join("", linq));
Console.WriteLine(string.Join("", items.Select(x => x.Name)));
```

### ✅ Output
```
abcde        ← LINQ OrderBy is guaranteed stable
```
The `List.Sort` line prints **some permutation** — with all keys equal, introsort may reorder them arbitrarily. It is documented as **unstable**.

### The rule
> **"`Enumerable.OrderBy` is documented as a STABLE sort. `List<T>.Sort` and `Array.Sort` use introsort and are UNSTABLE — equal elements may be reordered."**

### 🔥 Follow-up — "Why care?"
> Anywhere you sort by one key and expect a previous sort to survive — e.g. "sort by date, then by priority" done in two passes. With an unstable sort the first pass is destroyed. Use `.OrderBy(...).ThenBy(...)` instead of two sorts.

---

## Q64. `GroupBy` and `ToDictionary` on duplicate keys

```csharp
using System;
using System.Linq;
using System.Collections.Generic;

var words = new[] { "apple", "avocado", "banana" };

var grouped = words.GroupBy(w => w[0]);
Console.WriteLine(grouped.Count());

var dict = words.ToDictionary(w => w[0]);
```

### ✅ Output
```
2
💥 ArgumentException: An item with the same key has already been added. Key: a
```

### The rule
> **"`GroupBy` collects duplicates into groups. `ToDictionary` requires keys to be UNIQUE and throws on the first duplicate."**

### 🔥 Follow-up — "How do you make `ToDictionary` safe?"
```csharp
var dict = words.GroupBy(w => w[0]).ToDictionary(g => g.Key, g => g.ToList());
// or, keeping only the first:
var dict2 = words.GroupBy(w => w[0]).ToDictionary(g => g.Key, g => g.First());
```

---

# J — `async`/`await`

## Q65. Execution order

```csharp
using System;
using System.Threading.Tasks;

class Program
{
    static async Task Main()
    {
        Console.WriteLine("1");
        var t = DoWork();
        Console.WriteLine("3");
        await t;
        Console.WriteLine("5");
    }

    static async Task DoWork()
    {
        Console.WriteLine("2");
        await Task.Delay(100);
        Console.WriteLine("4");
    }
}
```

### ✅ Output
```
1
2
3
4
5
```

### The rule
> **"An `async` method runs SYNCHRONOUSLY until the first `await` that actually needs to wait. So `2` prints before `3` — calling an async method is not the same as starting a thread."**

This is the single most-asked async output question.

### 🔥 Follow-up — "Where does the code after `await` run?"
> On a captured **SynchronizationContext** (UI thread in WPF/WinForms, request context in classic ASP.NET) or on the thread pool if there is none — which is the case in Console apps and ASP.NET Core. `ConfigureAwait(false)` opts out of capturing it.

---

## Q66. `.Result` deadlock

```csharp
// In a WinForms/WPF/classic-ASP.NET app:
public async Task<string> GetAsync()
{
    await Task.Delay(100);
    return "done";
}

public void Button_Click(object sender, EventArgs e)
{
    string s = GetAsync().Result;   // ← what happens?
    MessageBox.Show(s);
}
```

### ✅ Output
```
💀 DEADLOCK — the UI freezes forever
```
*(the exact same code in a **Console app** or **ASP.NET Core** completes fine)*

### The rule
> **"`.Result` blocks the UI thread. When `Task.Delay` completes, the continuation needs the captured SynchronizationContext — the UI thread — which is blocked waiting on `.Result`. Each waits for the other."**

### 🔥 The follow-ups

**Follow-up 1 — "Fix it."**
```csharp
public async void Button_Click(object sender, EventArgs e)   // async void is OK for event handlers
{
    string s = await GetAsync();
    MessageBox.Show(s);
}
```
> **"Async all the way down."** Never mix blocking and async.

**Follow-up 2 — "Why doesn't it deadlock in ASP.NET Core?"**
> ASP.NET Core removed the SynchronizationContext, so continuations go to the thread pool. It still **wastes a thread** and can cause pool starvation under load — it's a scalability bug rather than a deadlock.

**Follow-up 3 — "And `ConfigureAwait(false)`?"**
```csharp
await Task.Delay(100).ConfigureAwait(false);   // library code — don't capture the context
```
> It fixes this specific deadlock, but the real fix is not blocking. Rule: **library code uses `ConfigureAwait(false)`; application code doesn't need to.**

---

## Q67. `async void` exceptions can't be caught

```csharp
using System;
using System.Threading.Tasks;

class Program
{
    static async void Fire()  { await Task.Delay(10); throw new Exception("boom"); }
    static async Task FireT() { await Task.Delay(10); throw new Exception("boom"); }

    static async Task Main()
    {
        try { Fire(); }
        catch (Exception e) { Console.WriteLine("caught void: " + e.Message); }

        try { await FireT(); }
        catch (Exception e) { Console.WriteLine("caught task: " + e.Message); }

        await Task.Delay(200);
    }
}
```

### ✅ Output
```
caught task: boom
💥 …then the process CRASHES with the unhandled "boom" from Fire()
```

### The rule
> **"An `async void` method has no `Task` to carry the exception, so it is re-thrown on the SynchronizationContext and becomes an UNHANDLED exception that kills the process. `async Task` stores the exception in the task, where `await` rethrows it into your `catch`."**

Note the `try/catch` around `Fire()` catches **nothing** — the method returned at its first `await`, long before it threw.

### 🔥 Follow-up — "When is `async void` acceptable?"
> **Only for event handlers**, where the signature is fixed by the framework — and then the body should be wrapped in its own `try/catch`. Everything else returns `Task`.

---

## Q68. Sequential `await` vs `WhenAll`

```csharp
using System;
using System.Diagnostics;
using System.Threading.Tasks;

class Program
{
    static Task<int> Work(int ms) => Task.Delay(ms).ContinueWith(_ => ms);

    static async Task Main()
    {
        var sw = Stopwatch.StartNew();
        var a = await Work(1000);
        var b = await Work(1000);
        Console.WriteLine($"sequential: ~{sw.ElapsedMilliseconds}ms");

        sw.Restart();
        var t1 = Work(1000);
        var t2 = Work(1000);
        await Task.WhenAll(t1, t2);
        Console.WriteLine($"parallel: ~{sw.ElapsedMilliseconds}ms");
    }
}
```

### ✅ Output
```
sequential: ~2000ms
parallel: ~1000ms
```

### The rule
> **"`await` in sequence starts the second task only after the first completes. Starting both tasks first and awaiting `WhenAll` overlaps them."**

### 🔥 Follow-up — "What's the trap in a `foreach`?"
```csharp
foreach (var id in ids) await FetchAsync(id);              // 🐌 N × latency
var results = await Task.WhenAll(ids.Select(FetchAsync));   // ✅ overlapped
```
> But add the caveat interviewers wait for: *"…as long as the downstream can take the concurrency — otherwise throttle with `SemaphoreSlim` or `Parallel.ForEachAsync` with `MaxDegreeOfParallelism`."*

---

## Q69. `Task.WhenAll` and multiple exceptions

```csharp
using System;
using System.Threading.Tasks;

class Program
{
    static async Task Fail(string m) { await Task.Delay(10); throw new Exception(m); }

    static async Task Main()
    {
        var t = Task.WhenAll(Fail("A"), Fail("B"));
        try { await t; }
        catch (Exception e) { Console.WriteLine("caught: " + e.Message); }

        Console.WriteLine("inner count: " +
            (t.Exception?.InnerExceptions.Count ?? 0));
    }
}
```

### ✅ Output
```
caught: A
inner count: 2
```

### The rule
> **"`await` on a faulted task rethrows only the FIRST exception, to keep stack traces meaningful. All of them are still on `task.Exception`, which is an `AggregateException`."**

### 🔥 Follow-up — "How do you get all of them?"
```csharp
try { await t; }
catch { foreach (var e in t.Exception.InnerExceptions) Console.WriteLine(e.Message); }
```
> Compare with `.Wait()` / `.Result`, which throw the `AggregateException` **directly** — another visible difference between blocking and awaiting.

---

## Q70. `async` without `await`

```csharp
using System;
using System.Threading.Tasks;

class Program
{
    static async Task<int> NoAwait()
    {
        Console.WriteLine("running");
        return 42;
    }

    static async Task Main() => Console.WriteLine(await NoAwait());
}
```

### ✅ Output
```
running
42
```
*(plus compiler **warning CS1998**: "This async method lacks 'await' operators and will run synchronously")*

### The rule
> **"`async` without `await` compiles and works, but it runs completely synchronously while still paying for the state machine. The compiler warns you."**

### 🔥 Follow-up — "How do you write a synchronous implementation of an async interface method?"
```csharp
public Task<int> GetAsync() => Task.FromResult(42);        // ✅ no async, no state machine
public Task DoAsync()       => Task.CompletedTask;
```
> Also mention `ValueTask<T>` for hot paths that usually complete synchronously — it avoids the `Task` allocation entirely.

---

# K — Collections & equality

## Q71. Modify during `foreach`

```csharp
var list = new List<int> { 1, 2, 3 };
foreach (var x in list)
{
    if (x == 2) list.Remove(x);
}
```

### ✅ Output
```
💥 InvalidOperationException: Collection was modified; enumeration operation may not execute
```

### The rule
> **"`List<T>` keeps a `_version` counter; the enumerator snapshots it and throws on the next `MoveNext()` if it changed."**

### 🔥 Follow-up — "Which is the sneakiest way this passes silently?"
> Removing the **second-to-last** element: `MoveNext()` sees `index >= _size` and returns `false` *before* checking the version, so the loop just ends early with **no exception**. That's a silent data bug — worse than the crash.

Safe forms:
```csharp
list.RemoveAll(x => x == 2);                       // best
for (int i = list.Count - 1; i >= 0; i--) { }      // backwards
foreach (var x in list.ToList()) { }               // snapshot
```

---

## Q72. Mutating a dictionary key

```csharp
using System;
using System.Collections.Generic;

class Key
{
    public int Id;
    public override int GetHashCode() => Id.GetHashCode();
    public override bool Equals(object o) => o is Key k && k.Id == Id;
}

var k = new Key { Id = 1 };
var d = new Dictionary<Key, string> { [k] = "value" };

Console.WriteLine(d.ContainsKey(k));
k.Id = 2;
Console.WriteLine(d.ContainsKey(k));
Console.WriteLine(d.Count);
```

### ✅ Output
```
True
False
1
```

### The rule
> **"The dictionary placed the entry in the bucket for hash(1). Changing `Id` changes the hash, so lookup goes to the wrong bucket. The entry is still there — it's now UNREACHABLE."**

Same as Java's `HashMap`.

### 🔥 Follow-up — "The rule for keys?"
> **Dictionary keys must be immutable** — use `string`, `int`, a `readonly record struct`, or a class whose hash-relevant fields are `readonly`. And: *"if you override `Equals` you must override `GetHashCode`"* — equal objects must have equal hashes, or a `Dictionary`/`HashSet` will lose them.

---

## Q73. `record` vs `class` equality

```csharp
using System;

record   PersonR(string Name, int Age);
class    PersonC { public string Name; public int Age; }
readonly record struct PointR(int X, int Y);

var r1 = new PersonR("Ann", 30);
var r2 = new PersonR("Ann", 30);
var c1 = new PersonC { Name = "Ann", Age = 30 };
var c2 = new PersonC { Name = "Ann", Age = 30 };

Console.WriteLine(r1 == r2);
Console.WriteLine(r1.Equals(r2));
Console.WriteLine(ReferenceEquals(r1, r2));
Console.WriteLine(c1 == c2);
Console.WriteLine(r1);
```

### ✅ Output
```
True
True
False
False
PersonR { Name = Ann, Age = 30 }
```

### The rule
> **"A `record` gets value-based `Equals`, `GetHashCode`, `==`/`!=` and a readable `ToString` generated by the compiler. A plain `class` gets reference equality and a type-name `ToString`."**

`ReferenceEquals` is still `False` — a record is a **class** with value semantics, not a value type.

### 🔥 Follow-up — "What else does a record give you?"
> **`with` expressions** for non-destructive mutation, `Deconstruct`, and `init`-only setters:
> ```csharp
> var older = r1 with { Age = 31 };   // new instance, Name copied
> ```
> ⚠️ **Trap:** `with` is a **shallow** copy — a `List<T>` property is shared between the original and the copy.

---

## Q74. Array covariance

```csharp
object[] arr = new string[2];
arr[0] = "ok";
arr[1] = 42;
```

### ✅ Output
```
💥 ArrayTypeMismatchException at runtime on the last line
```
*(it compiles without warning)*

### The rule
> **"Arrays are covariant — `string[]` is assignable to `object[]` — but the runtime enforces the real element type on every write. It's a known design mistake in both C# and Java."**

### 🔥 Follow-up — "How did generics fix it?"
> `List<string>` is **not** assignable to `List<object>` — generic type parameters are invariant unless declared `out` (covariant, output-only, e.g. `IEnumerable<out T>`) or `in` (contravariant, input-only, e.g. `IComparer<in T>`). That's why `IEnumerable<string>` → `IEnumerable<object>` works but `IList<string>` → `IList<object>` doesn't.

---

## Q75. `Dictionary` ordering is not guaranteed

```csharp
using System;
using System.Collections.Generic;

var d = new Dictionary<string, int> { ["z"] = 1, ["a"] = 2, ["m"] = 3 };
foreach (var kv in d) Console.Write(kv.Key + " ");
```

### ✅ Output
```
z a m       ← usually insertion order… but this is NOT guaranteed
```

### The rule
> **"`Dictionary<K,V>` makes NO ordering guarantee. It happens to enumerate in insertion order when nothing has been removed, but a single `Remove` followed by an `Add` reuses the freed slot and scrambles it. Never rely on it."**

### 🔥 Follow-up — "What if I need order?"
> `SortedDictionary<K,V>` (key order, tree, O(log n)), `SortedList<K,V>` (key order, array, faster lookup / slower insert), or a `List<KeyValuePair<K,V>>` for true insertion order. Java's answer — `LinkedHashMap` — **has no direct BCL equivalent**, which is a good thing to know out loud.

---

# L — Rapid-fire answer table

> Cover the right column. Target: **under 5 minutes**, daily.

| # | Program | Output | Why (one line) |
|---|---|---|---|
| 1 | `Test(object)` / `Test(string)` with `Test(null)` | `string method` | Most-specific overload, compile-time |
| 2 | Virtual call from base ctor, derived field initialized | field **is** set | C# runs derived field initializers *before* base ctor |
| 3 | `Speak()` hidden without `virtual` | base version | Non-virtual by default → compile-time binding |
| 4 | `return` inside `finally` | ❌ CS0157 | C# forbids leaving a `finally` |
| 5 | `struct` assigned then mutated | original unchanged | Value types copy on assignment |
| 6 | `int.MaxValue + 1` (literal) | ❌ CS0220 | Constants are evaluated `checked` |
| 7 | Lambdas in `for` vs `foreach` | `333` then `012` | `for` has one variable; `foreach` a fresh one (C# 5+) |
| 8 | Optional param on `override` | derived body, **base** default | Defaults baked at compile time |
| 9 | Box, then change the original | old value | Boxing snapshots |
| 10 | `object a=5, b=5; a==b` | `False` | Two boxes, reference comparison |
| 11 | `list[0].X = 5` on `List<struct>` | ❌ CS1612 | Indexer is a property → returns a copy |
| 12 | Method call on a `readonly` struct field | no change | Defensive copy |
| 13 | Reassign an array parameter | caller unaffected | Reference passed *by value* |
| 14 | `default(struct)` vs `default(class)` | zeroed / `null` | Value vs reference |
| 15 | `structA == structB` | ❌ CS0019 | `==` isn't auto-generated for structs |
| 16 | struct → interface variable | boxes, diverges | Interface assignment boxes |
| 17 | `override` vs `new` via base ref | `B` / `A` | vtable slot vs new method |
| 18 | `new virtual` then `override` | `A.M` | A second, separate virtual chain |
| 19 | `override` a `sealed override` | ❌ CS0239 | Chain terminated |
| 20 | Interface re-listed + `new` | `ColoredSquare.Draw` | Interface re-implementation |
| 21 | Explicit interface impl on the class | ❌ CS1061 | Not a class member |
| 22 | Abstract method called from base ctor | `0` | Derived ctor **body** hasn't run |
| 23 | `instance.StaticMethod()` | ❌ CS0176 | C# forbids it (Java allows) |
| 24 | `B.F(long)` vs inherited `A.F(int)` | `B.F(long)` | Search stops at the most-derived hit |
| 25 | Full init order | derived fields → base statics → base fields → base ctor → derived ctor | Memorize the ladder |
| 26 | Three `new Config()` | static ctor once | Type initializer runs once |
| 27 | `const` vs `static readonly` across assemblies | stale / current | `const` is inlined into callers |
| 28 | `int b = a + 1;` as a field initializer | ❌ CS0236 | Can't touch instance members |
| 29 | `static int A = B + 1; static int B = 10;` | `A=1` | Textual order |
| 30 | `(object)a == (object)new string(...)` | `False` | Not interned |
| 31 | `s.ToUpper();` ignoring the result | unchanged | Strings are immutable |
| 32 | `"x" + null` | `"x"` | Concat treats null as empty |
| 33 | `1 + 2 + "3" + 4 + 5` | `3345` | Left-associative |
| 34 | `"" == string.Empty` | `True` | Same interned instance |
| 35 | `"apple".CompareTo("Apple")` | culture-dependent | Always pass a `StringComparison` |
| 36 | `switch` case without `break` | ❌ CS0163 | No implicit fall-through |
| 37 | `i = i++` | `0` | Post-increment returns the old value |
| 38 | `-5 / 2`, `-5 % 2` | `-2`, `-1` | Truncates toward zero; `%` takes the dividend's sign |
| 39 | `10.0/0` vs `10/a` | `∞` / 💥 | IEEE 754 vs `DivideByZeroException` |
| 40 | `0.1 + 0.2 == 0.3` | `False` | Binary floating point |
| 41 | `b = b + 5` on a `byte` | ❌ CS0266 (but `b += 5` is fine) | Compound assignment has a hidden cast |
| 42 | `'a' + 1` | `98` | `char` widens to `int` |
| 43 | `true ? 1 : "two"` | ❌ CS0173 | No common type |
| 44 | `property++` | works, not atomic | get + add + set |
| 45 | `null is object` | `False` | `null` has no type |
| 46 | `a > b` and `a < b` with `a == null` | both `False` | Lifted relational operators |
| 47 | `null + 5` (`int?`) | `null` | Nulls propagate |
| 48 | `s?.A().B().C` on null | `null`, whole chain | `?.` short-circuits everything after it |
| 49 | `a ??= Get()` twice | one call | Short-circuits |
| 50 | `input!` returning null | 💥 NRE | NRTs are compile-time only |
| 51 | `return` in `catch` + `finally` | `finally` still runs | Runs before the actual return |
| 52 | Exception filter + inner `finally` | filter first | Two-pass exception model |
| 53 | `throw ex;` | trace reset | Use bare `throw;` |
| 54 | Nested `using` | disposes B then A | Reverse order |
| 55 | `catch(Exception)` before `catch(ArgumentException)` | ❌ CS0160 | Unreachable catch |
| 56 | `throw` inside `finally` | replaces the original | Original exception is lost |
| 57 | LINQ then `Add` to source | new item included | Deferred execution |
| 58 | `Count()` then `Sum()` on a query | pipeline runs twice | Re-enumeration |
| 59 | Lambdas capturing `for`'s `i` | `3,3,3` | Shared display-class field |
| 60 | `empty.First()` | 💥 InvalidOperationException | `FirstOrDefault` returns `default(T)` |
| 61 | Calling an iterator method | nothing runs | State machine built lazily |
| 62 | `list.Add` inside a LINQ `foreach` | 💥 InvalidOperationException | Version stamp |
| 63 | `OrderBy` vs `List.Sort` | stable vs unstable | Introsort reorders equals |
| 64 | `ToDictionary` with duplicate keys | 💥 ArgumentException | Keys must be unique |
| 65 | Async method call before first real `await` | runs synchronously | `2` prints before `3` |
| 66 | `.Result` on a UI thread | 💀 deadlock | Blocked context can't run the continuation |
| 67 | `async void` throwing | process crash | No Task to carry the exception |
| 68 | `await` in a loop vs `WhenAll` | 2000ms vs 1000ms | Sequential vs overlapped |
| 69 | `WhenAll` with 2 failures | first message only | `await` rethrows the first; all are in `.Exception` |
| 70 | `async` with no `await` | runs sync + CS1998 | Pointless state machine |
| 71 | `Remove` inside `foreach` | 💥 (or silently short!) | Version check happens after the bounds check |
| 72 | Mutate a dictionary key | entry unreachable | Hash bucket no longer matches |
| 73 | `record` `==` | `True` | Compiler-generated value equality |
| 74 | `object[] a = new string[2]; a[1] = 42;` | 💥 ArrayTypeMismatchException | Array covariance |
| 75 | `Dictionary` enumeration order | unspecified | Never rely on it |

---

# M — 20 self-test drills (answers hidden)

> Cover the answer, commit out loud, then check. Do **five a day**.

### Drill 1
```csharp
struct S { public int X; }
var arr = new S[1];
arr[0].X = 5;
var copy = arr[0];
copy.X = 10;
Console.WriteLine(arr[0].X);
```
<details><summary>👉 Answer</summary>

**`5`** — `arr[0].X = 5` writes into the array slot directly, but `var copy = arr[0]` takes a **copy**, so mutating `copy` doesn't touch the array.
</details>

### Drill 2
```csharp
class A { public A() { Console.Write("A"); } }
class B : A { public B() : this(1) { Console.Write("B"); } public B(int x) { Console.Write("C"); } }
new B();
```
<details><summary>👉 Answer</summary>

**`ACB`** — `B()` chains to `B(int)` via `this(1)`. `B(int)` has an implicit `: base()`, so `A()` runs first (`A`), then `B(int)`'s body (`C`), then `B()`'s body (`B`).
</details>

### Drill 3
```csharp
Console.WriteLine("5" + 3 + 2);
Console.WriteLine(5 + 3 + "2");
```
<details><summary>👉 Answer</summary>

**`532`** and **`82`** — left-associative. First line: string from the start. Second line: `5+3=8` arithmetic, then concat.
</details>

### Drill 4
```csharp
int? x = null;
Console.WriteLine(x == null);
Console.WriteLine(x.HasValue);
Console.WriteLine(x.GetType());
```
<details><summary>👉 Answer</summary>

`True`, `False`, then 💥 **`NullReferenceException`** — calling `GetType()` on a null `Nullable<T>` boxes it, and boxing a null nullable produces a **real `null` reference**. Great trap: `int?` isn't a reference type, but boxing it can still give you `null`.
</details>

### Drill 5
```csharp
var list = new List<int> { 1, 2, 3, 4 };
list.Remove(3);
list.RemoveAt(0);
Console.WriteLine(string.Join(",", list));
```
<details><summary>👉 Answer</summary>

**`2,4`** — `Remove(3)` removes the **value** 3 → `[1,2,4]`; `RemoveAt(0)` removes the **index** 0 → `[2,4]`. (The `remove(int)` vs `remove(Object)` trap from Java is *explicit* in C#, which is why C# names them differently.)
</details>

### Drill 6
```csharp
static void Swap(int a, int b) { var t = a; a = b; b = t; }
static void SwapRef(ref int a, ref int b) { var t = a; a = b; b = t; }
int x = 1, y = 2;
Swap(x, y);    Console.Write($"{x}{y} ");
SwapRef(ref x, ref y); Console.Write($"{x}{y}");
```
<details><summary>👉 Answer</summary>

**`12 21`** — plain parameters are copies; `ref` passes the variable itself.
</details>

### Drill 7
```csharp
Console.WriteLine(typeof(int) == typeof(Int32));
Console.WriteLine(typeof(int?) == typeof(Nullable<int>));
Console.WriteLine(typeof(string) == typeof(String));
```
<details><summary>👉 Answer</summary>

**`True True True`** — `int`, `int?` and `string` are C# **aliases** for `System.Int32`, `System.Nullable<Int32>` and `System.String`. They're the same types, so `string.Empty` and `String.Empty` are also identical.
</details>

### Drill 8
```csharp
static class Ext { public static bool IsEmpty(this string s) => s == null || s.Length == 0; }
string s = null;
Console.WriteLine(s.IsEmpty());
```
<details><summary>👉 Answer</summary>

**`True`** — no `NullReferenceException`! An extension method is a **static call** (`Ext.IsEmpty(s)`), so `this` may legally be null. Only *instance* method calls null-check the receiver.
</details>

### Drill 9
```csharp
enum Color { Red = 1, Green, Blue }
Color c = (Color)10;
Console.WriteLine(c);
Console.WriteLine(Enum.IsDefined(typeof(Color), c));
Console.WriteLine((Color)0);
```
<details><summary>👉 Answer</summary>

**`10`**, **`False`**, **`0`** — casting an out-of-range value to an enum **never throws**. `ToString()` falls back to the number. And since no member has value 0, `(Color)0` and `default(Color)` also print `0` — which is why you should always define a `None = 0` member.
</details>

### Drill 10
```csharp
var a = new { X = 1, Y = 2 };
var b = new { X = 1, Y = 2 };
Console.WriteLine(a.Equals(b));
Console.WriteLine(ReferenceEquals(a, b));
Console.WriteLine(a.GetType() == b.GetType());
```
<details><summary>👉 Answer</summary>

**`True False True`** — anonymous types get compiler-generated **value equality** (like records), and two anonymous types with the same property names, types and order in the same assembly are the **same generated type**.
</details>

### Drill 11
```csharp
static int F(int x) { Console.Write("F"); return x; }
if (false && F(1) > 0) { }
if (false & F(2) > 0)  { }
```
<details><summary>👉 Answer</summary>

**`F`** (printed once) — `&&` short-circuits, `&` does not. The non-short-circuiting `&`/`|` still evaluate both operands.
</details>

### Drill 12
```csharp
var sb = new StringBuilder("a");
void Mutate(StringBuilder b) { b.Append("b"); b = new StringBuilder("z"); b.Append("!"); }
Mutate(sb);
Console.WriteLine(sb);
```
<details><summary>👉 Answer</summary>

**`ab`** — `Append` mutates the shared object; reassigning the parameter is invisible to the caller. Same as Q13.
</details>

### Drill 13
```csharp
Console.WriteLine(Math.Round(2.5));
Console.WriteLine(Math.Round(3.5));
Console.WriteLine(Math.Round(2.5, MidpointRounding.AwayFromZero));
```
<details><summary>👉 Answer</summary>

**`2`**, **`4`**, **`3`** — .NET's default is **banker's rounding** (round-half-to-even), which surprises almost everyone. It exists to avoid statistical bias when summing many rounded values. For money, pass `MidpointRounding.AwayFromZero` explicitly.
</details>

### Drill 14
```csharp
IEnumerable<int> Numbers() { for (int i = 0; i < 3; i++) yield return i; }
var n = Numbers();
Console.WriteLine(n.Count());
Console.WriteLine(n.Count());
```
<details><summary>👉 Answer</summary>

**`3`** and **`3`** — each `Count()` starts a **fresh** enumeration; the iterator restarts from the top. (Contrast with an `IEnumerator`, which is one-shot.)
</details>

### Drill 15
```csharp
class Node { public Node Next; }
var a = new Node();
a.Next = a;
Console.WriteLine(a.Next.Next.Next == a);
```
<details><summary>👉 Answer</summary>

**`True`** — self-reference; every hop lands back on `a`. Follow-up they'll ask: *"will the GC collect this?"* → **Yes.** .NET uses a **tracing** collector (mark-and-sweep from GC roots), not reference counting, so cycles unreachable from a root are collected.
</details>

### Drill 16
```csharp
try
{
    using var fs = new MemoryStream();
    throw new Exception("x");
}
catch (Exception e) { Console.WriteLine("caught " + e.Message); }
Console.WriteLine("done");
```
<details><summary>👉 Answer</summary>

**`caught x`** then **`done`** — the `using` declaration disposes the stream as the scope unwinds (before the `catch` body runs), and the exception is caught normally.
</details>

### Drill 17
```csharp
static T Max<T>(T a, T b) where T : IComparable<T> => a.CompareTo(b) > 0 ? a : b;
Console.WriteLine(Max(3, 5));
Console.WriteLine(Max("apple", "banana"));
Console.WriteLine(Max(3, 5.0));
```
<details><summary>👉 Answer</summary>

**`5`**, **`banana`**, then ❌ **compile error CS0411** on the third — type inference can't unify `int` and `double` into a single `T`. Fix: `Max<double>(3, 5.0)`.
</details>

### Drill 18
```csharp
object o = null;
Console.WriteLine(o?.ToString() ?? "null");
Console.WriteLine($"{o}");
Console.WriteLine(o + "x");
```
<details><summary>👉 Answer</summary>

**`null`**, **`""` (blank line)**, **`x`** — `?.` short-circuits; interpolation and `+` both convert null to empty. None of them throw.
</details>

### Drill 19
```csharp
var d = new Dictionary<string, int>();
d["a"] = 1;
d["a"] = 2;
d.Add("b", 1);
d.Add("b", 2);
```
<details><summary>👉 Answer</summary>

💥 **`ArgumentException: An item with the same key has already been added`** on the last line. **The indexer `d["a"] = 2` overwrites silently; `Add` throws on a duplicate key.** Knowing which one throws is the whole question. (`TryAdd` returns `false` instead of throwing.)
</details>

### Drill 20
```csharp
class A { public virtual string Name => "A"; }
class B : A { public override string Name => "B"; }
class C : B { public new string Name => "C"; }

A x = new C();
B y = new C();
C z = new C();
Console.WriteLine($"{x.Name} {y.Name} {z.Name}");
```
<details><summary>👉 Answer</summary>

**`B B C`** — `C.Name` uses `new`, so it only exists for compile-time type `C`. Through `A` or `B` you reach the virtual chain, whose most-derived override is `B.Name`. (Properties follow the exact same virtual/hiding rules as methods.)
</details>

---

# N — How to answer these in the actual interview

**1. Never blurt the answer. State the rule, then the answer.**
> *"This is overload resolution, which is a compile-time decision — the compiler picks the most specific applicable overload. `string` is more specific than `object`, so it prints `string method`."*

**2. Ask "does it compile?" first.** Roughly a quarter of these are compile errors, and candidates who start tracing the runtime walk straight past them.

**3. Ask "struct or class?" second.** In C# this single question decides Q5, Q9–Q16, Q71 and half the drills. Say it out loud — *"first thing I'm checking is whether `Point` is a struct"* — because that's the reasoning they're grading.

**4. Name the error precisely.** *"Compile error"* is worth one point. *"CS0157 — control cannot leave the body of a finally clause, because C# forbids `return` inside `finally`, unlike Java"* is worth five.

**5. Use your Java background deliberately.** When the answer differs, say so:
> *"In Java this would print `Dog barks` because methods are virtual by default. In C# they're non-virtual by default, so without `virtual`/`override` this binds to the compile-time type and prints `Animal speaks`."*
>
> That one sentence proves you understand the *mechanism*, not just this language. It is the strongest thing you can say in a .NET interview as a Java developer, and it turns your background from a liability into an asset.

**6. Volunteer the fix.** After every broken program, say how you'd correct it. That's what turns a quiz into a conversation.

**7. If you genuinely don't know:**
> *"My reasoning says X because of [rule], but this is exactly the kind of thing I'd verify with a quick unit test or on sharplab.io before relying on it."*
>
> Mentioning **[sharplab.io](https://sharplab.io)** — which shows the lowered C# and the IL — is a real signal. It's how you'd *actually* answer "does the compiler add a cast here?" in a code review.

---

## Related files in this pack

- **[23 — Java "Predict the Output"](./23-java-output-tricky-questions.md)** — the Java twin of this file; Section A above maps directly onto it
- **[17 — SOLID & Design Patterns](./17-solid-design-patterns.md)** — language-agnostic; the Liskov principle behind Q3 and Q17
- **[22 — Java Streams](./22-java-streams-coding-problems.md)** — every Stream problem there has a LINQ equivalent; `stream().filter().map().collect()` → `.Where().Select().ToList()`
- **[24 — ORM, JPA & Hibernate](./24-orm-jpa-hibernate.md)** — the .NET equivalent is **Entity Framework Core**; lazy loading, change tracking and N+1 are the same problems with different names
- **[26 — Companies: Questions Actually Asked](./26-companies-asked-questions.md)** — **log every .NET round here the same day**

> 🔁 **Drill routine:** Section A twice (it's where the marks are), then the Section L rapid-fire table daily under 5 minutes, then 5 of the Section M drills on paper each morning. **Type the programs — don't just read them.** Reading these gives you recognition; typing them gives you the answer under pressure.
