# JavaScript Variables — `var`, `let`, `const` Made Easy

A slow, from-zero explanation with **real problems you can run**. If [01-javascript.md](./01-javascript.md) felt too compressed, start here — then go back to it.

> **Why this topic is worth a whole file:** `var` vs `let` vs `const` is asked in *every single* JavaScript screening round, from fresher to 5 years. And the follow-up — "what will this loop print?" — is the single most common trick question in the language. Get this airtight and you start every interview on the front foot.

---

---

## 🧠 The easiest way to remember this whole file — three pictures

### 1️⃣ Scope = ROOMS in a house 🏠

```
var   → ignores the walls. Declare it in a room, it LEAKS into the corridor ⚠️
let   → stays in its room ✅
const → stays in its room, and the nameplate is screwed on ✅
```

### 2️⃣ Hoisting = a GUEST LIST read out before the party 🎉

```
Every name is REGISTERED before anyone arrives (the declaration moves up).
But the guests arrive at their own time (the assignment stays put). ⭐

var guests   → registered as "unknown person" → you get `undefined` ⚠️
let/const    → registered but BARRED at the door until their moment
               → ReferenceError, which is BETTER (it fails loudly) ⭐
```

### 3️⃣ `const` = a fixed ADDRESS, movable furniture 🏠

```
const user = { name: 'Priya' };
user.name = 'Ravi';        ✅ rearranged the furniture
user = { name: 'Ravi' };   ❌ you cannot move the house ⭐
```

---

⭐ **Every question in this file is one of those three pictures.** The loop problem (§9) is picture 1. The TDZ (§8) is picture 2. The "but I changed the array!" confusion (§6) is picture 3.

#### Easy memory

```
1. SCOPE     var leaks out of blocks 🏠 | let/const stay in ⭐
2. HOISTING  declarations move up, assignments stay 🎉
             var → undefined ⚠️ | let/const → TDZ ReferenceError ⭐
3. const     locks the ADDRESS, not the CONTENTS 🏠 ⭐
```

---

## Table of contents

- [1. What a variable actually is](#1-what-a-variable-actually-is)
- [2. The 4 questions that define every declaration](#2-the-4-questions-that-define-every-declaration)
- [3. Scope — where can I see this variable?](#3-scope--where-can-i-see-this-variable)
- [4. `var` — the old one](#4-var--the-old-one)
- [5. `let` — the fixed one](#5-let--the-fixed-one)
- [6. `const` — the one everyone misunderstands](#6-const--the-one-everyone-misunderstands)
- [7. Hoisting — what really happens before your code runs](#7-hoisting--what-really-happens-before-your-code-runs)
- [8. The Temporal Dead Zone (TDZ)](#8-the-temporal-dead-zone-tdz)
- [9. 🔥 THE loop problem](#9--the-loop-problem--the-most-asked-question)
- [10. Real bugs from real code](#10-real-bugs-from-real-code)
- [11. Practice problems (with answers)](#11-practice-problems)
- [12. Output-based drills](#12-output-based-drills)
- [13. Interview Q&A](#13-interview-qa)
- [14. One-page cheat sheet](#14-one-page-cheat-sheet)

---

## 1. What a variable actually is

A variable is a **name that points to a value**.

```js
let price = 100;
```

Three separate things are happening:

| Step | Name | What happens |
|---|---|---|
| 1 | **Declaration** | JavaScript creates a name `price` in memory |
| 2 | **Initialization** | That name is given a starting value |
| 3 | **Assignment** | `100` is stored under that name |

You can split them:

```js
let price;          // declaration only  → price is `undefined`
price = 100;        // assignment
price = 200;        // re-assignment
```

Keep these three words separate in your head. **Every confusing thing about `var`/`let`/`const` comes down to *when* each step happens.**

---

## 2. The 4 questions that define every declaration

Whenever you see `var`, `let` or `const`, ask these four questions. That's the whole topic.

| # | Question | `var` | `let` | `const` |
|---|---|---|---|---|
| 1 | **What scope?** | Function | Block | Block |
| 2 | **Can I re-assign it?** | ✅ Yes | ✅ Yes | ❌ No |
| 3 | **Can I re-declare it in the same scope?** | ✅ Yes | ❌ No | ❌ No |
| 4 | **What if I use it before declaring?** | `undefined` | ❌ ReferenceError (TDZ) | ❌ ReferenceError (TDZ) |

Plus one bonus:

| 5 | **Does it attach to `window`?** | ✅ Yes (at top level) | ❌ No | ❌ No |

**If you memorize only one table in this file, memorize this one.** Every question in this document is just one of these five rows in disguise.

---

## 3. Scope — where can I see this variable?

**Scope = the region of code where a variable is visible.**

There are three kinds:

```js
let planet = "Earth";              // 1️⃣ GLOBAL scope — visible everywhere

function greet() {
  let city = "Chennai";            // 2️⃣ FUNCTION scope — only inside greet()

  if (true) {
    let street = "Anna Salai";     // 3️⃣ BLOCK scope — only inside these { }
    console.log(planet, city, street);  // ✅ all three visible
  }

  console.log(street);             // ❌ ReferenceError — street died at the }
}
```

### What is a "block"?

**Anything between `{` and `}`** that isn't a function body:

```js
if (x) { /* block */ }
for (...) { /* block */ }
while (x) { /* block */ }
{ /* even a bare block is a block */ }
```

**The one-line rule that explains everything:**

> 🔑 **`var` ignores blocks. `let` and `const` respect them.**

Watch it happen:

```js
function test() {
  if (true) {
    var a = 1;      // var: escapes the if-block, belongs to test()
    let b = 2;      // let: trapped inside the if-block
  }

  console.log(a);   // ✅ 1
  console.log(b);   // ❌ ReferenceError: b is not defined
}
```

**Say this in an interview:** *"`var` is function-scoped, so it leaks out of `if` and `for` blocks. `let` and `const` are block-scoped, so they're confined to the nearest pair of braces."*

---

## 4. `var` — the old one

`var` is the original JavaScript declaration (pre-2015). It has **three behaviours that cause bugs**.

### Problem 1 — it leaks out of blocks

```js
if (true) {
  var role = "admin";
}
console.log(role);        // "admin"  ← still alive outside the if!
```

You expected `role` to be temporary. It isn't.

### Problem 2 — it can be silently re-declared

```js
var user = "Priyanka";
// ... 200 lines later, a teammate writes:
var user = "Admin";       // ✅ No error. No warning. Your value is gone.

console.log(user);        // "Admin"
```

This is a real production bug pattern in large files. `let` makes it a hard error instead.

### Problem 3 — it pollutes `window`

```js
var appName = "RoboGebra";
console.log(window.appName);   // "RoboGebra"  ← now a global property

let appVersion = "1.0";
console.log(window.appVersion); // undefined  ← let stays clean
```

Two libraries both doing `var $ = ...` will overwrite each other.

### So when do you use `var`?

**You don't.** In modern code (ES6+, TypeScript, Angular) `var` has **no remaining use case**.

> 💬 **Interview answer:** *"I don't use `var` in new code. It's function-scoped, silently re-declarable, and attaches to `window`, which causes bugs that `let` and `const` prevent at compile time. I only need to understand it for reading legacy code."*

---

## 5. `let` — the fixed one

`let` (ES6, 2015) fixes all three `var` problems.

```js
let count = 0;
count = 1;          // ✅ re-assign — fine
count = count + 1;  // ✅ fine

let count = 5;      // ❌ SyntaxError: Identifier 'count' has already been declared
```

Block-scoped, as promised:

```js
let x = "outer";
{
  let x = "inner";        // ✅ different variable — this is "shadowing", not re-declaring
  console.log(x);         // "inner"
}
console.log(x);           // "outer"
```

> ⚠️ **Shadowing vs re-declaring.** Same name in a **different** scope = shadowing (allowed). Same name in the **same** scope = re-declaring (error with `let`).

### Use `let` when the value genuinely changes

```js
let attempts = 0;
let currentUser = null;
let total = 0;

for (let i = 0; i < items.length; i++) {   // i changes each iteration
  total += items[i].price;                  // total changes
}
```

---

## 6. `const` — the one everyone misunderstands

`const` means the **binding** can't be reassigned. It does **not** mean the value is frozen.

### Rule 1 — must be initialized immediately

```js
const rate = 0.18;    // ✅
const tax;            // ❌ SyntaxError: Missing initializer in const declaration
```

### Rule 2 — cannot be re-assigned

```js
const rate = 0.18;
rate = 0.20;          // ❌ TypeError: Assignment to constant variable.
```

### Rule 3 — ⚠️ objects and arrays CAN still be changed

**This is the #1 misunderstanding in JavaScript interviews.**

```js
const student = { name: "Priyanka", marks: 90 };

student.marks = 95;              // ✅ ALLOWED — changing what's *inside* the object
student.grade = "A";             // ✅ ALLOWED — adding a property
delete student.grade;            // ✅ ALLOWED

student = { name: "Someone" };   // ❌ TypeError — pointing the name at a NEW object
```

```js
const marks = [90, 85, 78];

marks.push(100);                 // ✅ ALLOWED → [90, 85, 78, 100]
marks[0] = 95;                   // ✅ ALLOWED
marks.length = 0;                // ✅ ALLOWED (empties it!)

marks = [1, 2, 3];               // ❌ TypeError
```

### 🧠 The mental model that makes this click

Think of a **variable as a label stuck on a box**:

```
   const student  ──────────►  ┌─────────────────┐
   (the label)                 │  name: Priyanka │
                               │  marks: 90      │   ← the box (the object)
                               └─────────────────┘

   const stops you MOVING THE LABEL to a different box.
   It does NOT stop you REARRANGING WHAT'S INSIDE the box.
```

- `student.marks = 95` → rearranging inside the box → ✅ fine
- `student = {...}` → moving the label to a new box → ❌ blocked by `const`

**Why does it work this way?** Because objects and arrays are stored **by reference**. The variable holds a *pointer to* the object, not the object itself. `const` protects the pointer, not the thing it points to.

> 💬 **Interview answer (say it exactly like this):** *"`const` prevents re-assignment of the binding, not mutation of the value. For an object or array, the reference is constant but the contents are still mutable. If I need real immutability I'd use `Object.freeze()`, or `readonly` / `ReadonlyArray<T>` in TypeScript."*

### If you actually want immutability

```js
const config = Object.freeze({ apiUrl: "https://api.robogebra.ai", retries: 3 });

config.retries = 5;              // silently ignored (throws in strict mode)
console.log(config.retries);     // 3
```

> ⚠️ `Object.freeze()` is **shallow** — nested objects are still mutable:
> ```js
> const settings = Object.freeze({ user: { name: "Priyanka" } });
> settings.user.name = "Changed";   // ✅ still works! freeze didn't go deep
> ```
> For deep freezing you recurse over the properties yourself.

### In TypeScript (your daily language)

```ts
const rate = 0.18;                              // value can't be reassigned

interface Config { readonly apiUrl: string; }   // property can't be reassigned
const c: Config = { apiUrl: "..." };
c.apiUrl = "x";                                 // ❌ compile error

const ids: ReadonlyArray<number> = [1, 2, 3];
ids.push(4);                                    // ❌ compile error
```

> 💡 TypeScript's `readonly` is **compile-time only** — it disappears at runtime. `Object.freeze()` is the runtime guarantee. Good detail to mention.

---

## 7. Hoisting — what really happens before your code runs

**Hoisting is the #2 most-asked question here.** Here's the honest explanation.

### The two-phase model

JavaScript does **not** run your code top-to-bottom in one pass. It runs each scope in **two phases**:

```
PHASE 1 — CREATION (before any line executes)
   → Scan the scope
   → Register every declared name in memory
   → var  names → set to `undefined`
   → let/const names → registered but marked "not initialized" (TDZ)
   → function declarations → fully stored, body and all

PHASE 2 — EXECUTION (now run line by line)
   → Assignments actually happen
```

### Why `var` prints `undefined` instead of crashing

```js
console.log(name);    // undefined  ← not an error!
var name = "Priyanka";
console.log(name);    // "Priyanka"
```

**What JavaScript effectively does:**

```js
var name = undefined;      // ← Phase 1 put this here
console.log(name);         // undefined
name = "Priyanka";         // ← Phase 2: only the assignment stayed put
console.log(name);         // "Priyanka"
```

> 🔑 **Only the *declaration* is hoisted, never the *assignment*.**

### Function declarations are hoisted completely

```js
greet();                      // ✅ "Hello!" — works before it's written

function greet() {
  console.log("Hello!");
}
```

### Function expressions are NOT

```js
greet();                      // ❌ TypeError: greet is not a function

var greet = function () {     // `greet` is hoisted as undefined... 
  console.log("Hello!");      // ...and you can't call undefined
};
```

```js
greet();                      // ❌ ReferenceError: Cannot access 'greet' before initialization

const greet = () => {         // const → TDZ, different error
  console.log("Hello!");
};
```

> 📌 **Notice the two different errors.** `TypeError` vs `ReferenceError` is a classic interview trap — see [drill 4](#12-output-based-drills).

---

## 8. The Temporal Dead Zone (TDZ)

Scary name, simple idea.

> **TDZ = the gap between when a `let`/`const` variable is *created* and when it's *initialized*. Touching it in that gap throws a `ReferenceError`.**

```js
{
  // ⬇️ TDZ for `x` starts here — x exists, but is unusable
  console.log(x);      // ❌ ReferenceError: Cannot access 'x' before initialization
  
  let x = 10;          // ⬆️ TDZ ends here
  
  console.log(x);      // ✅ 10
}
```

### Proof `let` really *is* hoisted

People often say "`let` isn't hoisted." That's wrong — and interviewers love catching it. Compare:

```js
console.log(a);      // ❌ ReferenceError: a is not defined     ← never declared anywhere
```
```js
console.log(b);      // ❌ ReferenceError: Cannot access 'b' before initialization
let b = 1;           //                    ↑ different message!
```

Two **different** error messages. The second one proves JavaScript already *knows* `b` exists — it's hoisted, just parked in the TDZ.

> 💬 **Interview answer:** *"`let` and `const` are hoisted, but unlike `var` they aren't initialized to `undefined`. They sit in the Temporal Dead Zone until the declaration line executes, and accessing them there throws a `ReferenceError`. That's deliberate — it turns a silent `undefined` bug into a loud error."*

### Why the TDZ is a *good* thing

```js
// With var — silent wrong answer:
console.log(discount);     // undefined
var discount = 0.10;
const price = 100 * (1 - discount);   // NaN — and no one notices until production

// With let — immediate, obvious crash:
console.log(discount);     // ❌ ReferenceError — you fix it in 5 seconds
let discount = 0.10;
```

---

## 9. 🔥 THE loop problem — the most asked question

If you learn one thing from this file, learn this. It comes up constantly.

### The problem

```js
for (var i = 1; i <= 3; i++) {
  setTimeout(function () {
    console.log(i);
  }, 1000);
}
```

**What most people say:** `1, 2, 3`
**What actually prints:** `4, 4, 4`

### Why — step by step

**Step 1.** `var i` is **function-scoped**, not block-scoped. So there is exactly **ONE** `i` shared by all three iterations.

```
   Iteration 1 ──┐
   Iteration 2 ──┼──►  the SAME single `i` in memory
   Iteration 3 ──┘
```

**Step 2.** `setTimeout` doesn't run the callback now. It schedules it for **later** and moves on.

**Step 3.** The loop finishes first. Trace it:

| Moment | `i` | What happens |
|---|---|---|
| Start | 1 | schedule callback #1, `i++` |
| | 2 | schedule callback #2, `i++` |
| | 3 | schedule callback #3, `i++` |
| | **4** | `4 <= 3` is false → **loop exits** |
| 1 second later | 4 | callback #1 runs → reads `i` → **4** |
| | 4 | callback #2 runs → **4** |
| | 4 | callback #3 runs → **4** |

By the time the callbacks run, the loop is long finished and the one shared `i` is stuck at `4`.

---

### ✅ Fix 1 — just use `let` (the modern answer)

```js
for (let i = 1; i <= 3; i++) {
  setTimeout(function () {
    console.log(i);
  }, 1000);
}
// 1, 2, 3 ✅
```

**Why this works:** `let` in a `for` loop creates a **brand-new binding for every iteration**. This is a special rule in the spec — the loop head is treated as its own block, and the value is copied forward each pass.

```
   Iteration 1 ──►  i = 1   (its own copy)
   Iteration 2 ──►  i = 2   (its own copy)
   Iteration 3 ──►  i = 3   (its own copy)
```

Each callback closes over its **own** `i`. Done.

### ✅ Fix 2 — an IIFE (the pre-ES6 answer)

```js
for (var i = 1; i <= 3; i++) {
  (function (copy) {
    setTimeout(function () {
      console.log(copy);
    }, 1000);
  })(i);              // pass the CURRENT value in as an argument
}
// 1, 2, 3 ✅
```

Each call to the immediately-invoked function creates a new function scope with its own `copy` parameter.

### ✅ Fix 3 — bind the value as an argument

```js
for (var i = 1; i <= 3; i++) {
  setTimeout(console.log, 1000, i);   // extra args to setTimeout are passed to the callback
}
// 1, 2, 3 ✅
```

> 💬 **Full interview answer:** *"It prints 4, 4, 4. `var` is function-scoped, so all three callbacks close over the same `i`, and by the time they run — asynchronously, after the loop — `i` is 4. Changing `var` to `let` fixes it, because `let` creates a fresh binding per iteration. Before ES6 you'd wrap it in an IIFE to capture the value."*

### ⚠️ The `const` variant

```js
for (const i = 0; i < 3; i++) { }
// ❌ TypeError: Assignment to constant variable.  (i++ can't reassign a const)

for (const item of items) { }
// ✅ Perfectly fine! for...of creates a NEW binding each iteration — nothing is reassigned
```

**`for...of` with `const` is the idiomatic modern loop.** Know the difference — it's a good follow-up question.

---

#### Easy memory — THE loop problem ⭐⭐

```
for (var i …)  setTimeout(() => log(i))   →  4, 4, 4  ⚠️
for (let i …)  setTimeout(() => log(i))   →  1, 2, 3  ✅ ⭐

WHY: `var` = ONE shared `i` for the whole loop.
     By the time the callbacks run, the loop has FINISHED and i is 4 ⭐

     `let` = a NEW binding EVERY iteration, so each callback
     captured its own copy ⭐
```

Real-world idea: **one shared whiteboard vs three separate notepads.** With `var`, all three callbacks read the same whiteboard — and by the time they look, it says 4. With `let`, each one wrote the number on its own notepad at the time.

```
The three fixes, in order of what an interviewer wants to hear:
  1. use `let`                      ⭐ the modern answer
  2. an IIFE per iteration          (the pre-ES6 answer — shows you know why)
  3. setTimeout's 3rd argument      (pass i in as an argument)
```

⭐ **Say why, not just what:** *"`var` is function-scoped, so all three closures captured the same variable. `let` is block-scoped and the spec creates a fresh binding per iteration, so each closure captured its own."*

---

## 10. Real bugs from real code

These are the shapes this topic takes in an Angular/TypeScript job — much more convincing than textbook examples.

### Bug 1 — event listeners in a loop

```js
const buttons = document.querySelectorAll(".chapter-btn");

// ❌ Every button alerts the LAST index
for (var i = 0; i < buttons.length; i++) {
  buttons[i].addEventListener("click", () => alert("Chapter " + i));
}

// ✅ Fix
for (let i = 0; i < buttons.length; i++) {
  buttons[i].addEventListener("click", () => alert("Chapter " + i));
}
```

Same root cause as the `setTimeout` problem: the callback runs *later*, and `var` gave everyone the same `i`.

### Bug 2 — mutating a `const` array shared across components

```ts
// shared.constants.ts
export const DEFAULT_FILTERS = ["algebra", "geometry"];

// some.component.ts
ngOnInit() {
  this.filters = DEFAULT_FILTERS;
  this.filters.push("calculus");    // ⚠️ mutates the SHARED array for the whole app!
}
```

`const` did **not** protect you — `push` mutates, it doesn't reassign. Every other component now sees `"calculus"`.

**Fixes:**
```ts
this.filters = [...DEFAULT_FILTERS];                        // copy before mutating
// or make the source genuinely safe:
export const DEFAULT_FILTERS: ReadonlyArray<string> = Object.freeze(["algebra", "geometry"]);
```

> 🎯 This is an excellent thing to mention in an interview: *"The `const` + shared-array mutation bug is one I actively watch for in Angular — a constant exported from a shared module gets mutated by one component and silently changes behaviour everywhere."*

### Bug 3 — `var` inside a `switch`

```js
switch (type) {
  case "quiz":
    var handler = quizHandler;    // leaks to the whole function
    break;
  case "lesson":
    var handler = lessonHandler;  // silent re-declaration — no error
    break;
}
```

With `let` you'd get `SyntaxError: Identifier 'handler' has already been declared` — because a `switch` body is **one single block**. The correct fix is braces per case:

```js
switch (type) {
  case "quiz": {
    let handler = quizHandler;    // ✅ own block
    break;
  }
  case "lesson": {
    let handler = lessonHandler;  // ✅ own block
    break;
  }
}
```

### Bug 4 — a `let` you meant to declare outside

```ts
let subscription;                              // declared outside ✅

ngOnInit() {
  this.data$.subscribe(d => {
    let subscription = somethingElse;          // ❌ shadows the outer one
  });
}

ngOnDestroy() {
  subscription.unsubscribe();                  // ❌ undefined — memory leak
}
```

Shadowing is legal, which makes this bug quiet. Always check whether you meant to declare a *new* variable or assign to an existing one.

### Bug 5 — accidental global (no keyword at all)

```js
function calculateTotal() {
  total = 0;              // ❌ no let/const/var → creates a GLOBAL variable
  // ...
}
```

In non-strict mode this silently creates `window.total`. In `"use strict"` (and every ES module, and all TypeScript) it throws `ReferenceError: total is not defined` — one more reason strict mode is on by default in modern code.

---

## 11. Practice problems

Try each one **before** reading the answer. Write down your prediction first.

---

### Problem 1

```js
function test() {
  console.log(a);
  console.log(b);
  var a = 1;
  let b = 2;
}
test();
```

<details><summary>👉 Answer</summary>

```
undefined
❌ ReferenceError: Cannot access 'b' before initialization
```

`var a` is hoisted and initialized to `undefined`, so line 1 prints `undefined`. `let b` is hoisted but sits in the TDZ, so line 2 throws — and execution stops there.
</details>

---

### Problem 2

```js
const arr = [1, 2, 3];
arr.push(4);
console.log(arr);

arr = [5, 6];
console.log(arr);
```

<details><summary>👉 Answer</summary>

```
[1, 2, 3, 4]
❌ TypeError: Assignment to constant variable.
```

`push` mutates the existing array — allowed. `arr = [5, 6]` re-assigns the binding — blocked.
</details>

---

### Problem 3

```js
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log("var:", i), 0);
}
for (let j = 0; j < 3; j++) {
  setTimeout(() => console.log("let:", j), 0);
}
```

<details><summary>👉 Answer</summary>

```
var: 3
var: 3
var: 3
let: 0
let: 1
let: 2
```

One shared `i` (ends at 3) vs a fresh `j` per iteration. Note all six run *after* both loops finish, because `setTimeout` is async even with a `0` delay.
</details>

---

### Problem 4

```js
var x = 10;

function show() {
  console.log(x);
  var x = 20;
}
show();
```

<details><summary>👉 Answer</summary>

```
undefined
```

The **inner** `var x` is hoisted to the top of `show()` and initialized to `undefined`. It shadows the global `x` for the entire function body — so `console.log(x)` reads the local one, not the global `10`. This is the classic hoisting-plus-shadowing trap.
</details>

---

### Problem 5

```js
let count = 0;
{
  let count = 10;
  count++;
}
console.log(count);
```

<details><summary>👉 Answer</summary>

```
0
```

The inner `count` is a completely separate variable that lives and dies inside the block. The outer one was never touched.
</details>

---

### Problem 6

```js
const student = { name: "Priyanka", skills: ["Angular"] };

student.skills.push("Java");
student.name = "Priyanka R";
Object.freeze(student);
student.name = "Changed";
student.skills.push("Spring");

console.log(student);
```

<details><summary>👉 Answer</summary>

```js
{ name: "Priyanka R", skills: ["Angular", "Java", "Spring"] }
```

Line by line: `push` and the `name` change happen before the freeze, so both apply. After `Object.freeze`, `student.name = "Changed"` is silently ignored. But **freeze is shallow** — the nested `skills` array is *not* frozen, so `push("Spring")` still works.
</details>

---

### Problem 7

```js
function counter() {
  var count = 0;
  return function () {
    count++;
    return count;
  };
}

const c1 = counter();
const c2 = counter();

console.log(c1(), c1(), c2());
```

<details><summary>👉 Answer</summary>

```
1 2 1
```

Each call to `counter()` creates a **new** function scope with its own `count`. `c1` and `c2` are independent closures. (`var` is fine here — it's function-scoped and there's one function scope per call.)
</details>

---

### Problem 8 — write the code

> Write a loop that prints `1` after 1 second, `2` after 2 seconds, and `3` after 3 seconds. Then write the same thing **without** using `let`.

<details><summary>👉 Answer</summary>

```js
// With let
for (let i = 1; i <= 3; i++) {
  setTimeout(() => console.log(i), i * 1000);
}

// Without let — IIFE
for (var i = 1; i <= 3; i++) {
  (function (n) {
    setTimeout(function () { console.log(n); }, n * 1000);
  })(i);
}
```
</details>

---

## 12. Output-based drills

Rapid-fire. Cover the right column and answer out loud.

| Code | Output |
|---|---|
| `console.log(a); var a = 1;` | `undefined` |
| `console.log(b); let b = 1;` | ❌ ReferenceError (TDZ) |
| `console.log(c);` (never declared) | ❌ ReferenceError: c is not defined |
| `let x = 1; let x = 2;` | ❌ SyntaxError: already declared |
| `var y = 1; var y = 2; console.log(y);` | `2` |
| `const z = {a:1}; z.a = 2; console.log(z.a);` | `2` |
| `const z = {a:1}; z = {a:2};` | ❌ TypeError |
| `if(true){var m=1} console.log(m);` | `1` |
| `if(true){let n=1} console.log(n);` | ❌ ReferenceError |
| `for(var i=0;i<3;i++){} console.log(i);` | `3` |
| `for(let j=0;j<3;j++){} console.log(j);` | ❌ ReferenceError |
| `const arr=[1]; arr.length=0; console.log(arr);` | `[]` |
| `var v=1; console.log(window.v);` | `1` |
| `let l=1; console.log(window.l);` | `undefined` |
| `f(); function f(){console.log("hi")}` | `hi` |
| `g(); var g=function(){};` | ❌ TypeError: g is not a function |
| `h(); const h=()=>{};` | ❌ ReferenceError (TDZ) |
| `const o=Object.freeze({a:1}); o.a=9; console.log(o.a);` | `1` |

---

## 13. Interview Q&A

### Q: Difference between `var`, `let` and `const`?

> *"Three differences that matter. **Scope** — `var` is function-scoped, so it leaks out of `if` and `for` blocks; `let` and `const` are block-scoped. **Re-assignment** — `var` and `let` can be reassigned, `const` can't. **Hoisting** — all three are hoisted, but `var` is initialized to `undefined` while `let` and `const` sit in the Temporal Dead Zone and throw a `ReferenceError` if you touch them early. `var` also attaches to `window` at the top level; `let` and `const` don't."*

### Q: Which one do you use by default?

> *"`const` by default, `let` when the value genuinely needs to change, and `var` never in new code. Defaulting to `const` documents intent — a reader can see at a glance which variables are stable — and it prevents accidental reassignment."*

### Q: Does `const` make a value immutable?

> *"No. `const` prevents reassigning the binding, not mutating the value. `const arr = [1,2]; arr.push(3)` is legal because the reference hasn't changed. For actual immutability you'd use `Object.freeze()` at runtime — though that's shallow — or `readonly` and `ReadonlyArray<T>` in TypeScript at compile time."*

### Q: Is `let` hoisted?

> *"Yes — this is a common misconception. `let` and `const` are hoisted, but not initialized. They're in the TDZ until their declaration executes. You can prove it: accessing an undeclared variable gives 'x is not defined', while accessing a `let` before its declaration gives 'Cannot access x before initialization' — a different error, which means the engine already knows the variable exists."*

### Q: Why does `for (var i...)` with `setTimeout` print the wrong values?

> *(See the full answer in [section 9](#9--the-loop-problem--the-most-asked-question).)*

### Q: What's the TDZ for, conceptually?

> *"It converts a silent bug into a loud one. With `var`, using a variable too early gives you `undefined`, which then quietly propagates as `NaN` or a wrong branch. With `let`, you get an immediate `ReferenceError` at the exact line."*

### Q: Can you re-declare a variable?

> *"`var` yes, silently. `let` and `const` no — it's a `SyntaxError` in the same scope. But **shadowing** in a nested scope is allowed for all three, and that's a different thing: a new variable with the same name in an inner block."*

---

## 14. One-page cheat sheet

```
┌──────────────────────────────────────────────────────────────────────┐
│                    var          let           const                  │
├──────────────────────────────────────────────────────────────────────┤
│  Scope             function     block         block                  │
│  Re-assign         ✅ yes       ✅ yes        ❌ no                   │
│  Re-declare        ✅ yes       ❌ no         ❌ no                   │
│  Hoisted           ✅ yes       ✅ yes        ✅ yes                  │
│  Initialized       undefined    ❌ TDZ        ❌ TDZ                  │
│  Use before decl.  undefined    ReferenceErr  ReferenceErr           │
│  Must initialize   ❌ no        ❌ no         ✅ YES                  │
│  On window         ✅ yes       ❌ no         ❌ no                   │
└──────────────────────────────────────────────────────────────────────┘

THE FOUR SENTENCES THAT COVER 90% OF QUESTIONS
  1.  var ignores blocks. let and const respect them.
  2.  All three are hoisted — var to `undefined`, let/const into the TDZ.
  3.  const locks the binding, not the contents.
  4.  `let` in a for-loop creates a fresh binding every iteration; `var` doesn't.

DAILY RULE
  const  →  default. Use it until the code forces you not to.
  let    →  only when the value is genuinely reassigned.
  var    →  never.
```

---

### Where to go next

- [01-javascript.md](./01-javascript.md) — closures, `this`, the event loop, promises (the loop problem sits right next to closures — study them together)
- [03-typescript.md](./03-typescript.md) — `readonly`, `as const`, and how TypeScript layers compile-time safety on top of all of this
- [09-coding-problems.md](./09-coding-problems.md) — practice problems where scope bugs actually bite
