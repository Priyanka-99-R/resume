# JavaScript — Interview Q&A (Easy Version)

> Every question follows the same shape — **Easiest way to remember → simple explanation → real-world example → diagram → interview trap → Easy memory box.**
>
> 📘 If `var` / `let` / `const`, hoisting or the loop problem feel shaky, read **[19 — JavaScript Variables Made Easy](./19-javascript-variables-easy.md)** first — it teaches those from zero.

---

## Contents

1. [Fundamentals](#fundamentals)
2. [Functions & Closures](#functions--closures)
3. [Objects & Prototypes](#objects--prototypes)
4. [Asynchronous JavaScript](#asynchronous-javascript)
5. [ES6+ Features](#es6-features)
6. [Advanced](#advanced)
7. [Tricky Output-Based Questions](#tricky-output-based-questions)
8. [Quick Revision Sheet](#quick-revision-sheet)

---

## Fundamentals

### Q: `var` vs `let` vs `const`?

The easiest way to remember:

```
var   → FUNCTION-scoped, hoisted as undefined, re-declarable   ⚠️ old
let   → BLOCK-scoped, can be reassigned                        ✅
const → BLOCK-scoped, CANNOT be reassigned                     ✅ default choice ⭐
```

| | `var` | `let` | `const` |
|---|---|---|---|
| Scope | **function** ⚠️ | block `{}` | block `{}` |
| Re-declare in the same scope | ✅ | ❌ | ❌ |
| Re-assign | ✅ | ✅ | ❌ |
| Hoisted | ✅ as `undefined` ⚠️ | ✅ but in the **TDZ** ⭐ | ✅ but in the TDZ |
| Adds to `window` | ✅ ⚠️ | ❌ | ❌ |

```js
if (true) {
  var a = 1;
  let b = 2;
}
console.log(a);   // 1          ⚠️ var LEAKED out of the block
console.log(b);   // ❌ ReferenceError — let stayed inside ✅
```

⭐ **The `const` trap everyone falls into:**

```js
const user = { name: 'Priya' };
user.name = 'Ravi';        // ✅ ALLOWED — the CONTENTS changed
user = { name: 'Ravi' };   // ❌ TypeError — the BINDING is locked
```

```
const locks the BOX, not what's INSIDE it. ⭐
```

Real-world idea: your **house address is fixed** (`const`), but you can rearrange the furniture inside.

**The rule to state:** *"`const` by default, `let` when the value genuinely changes, `var` never."*

#### Easy memory

```
var → FUNCTION scope, leaks, hoisted as undefined ⚠️ never use
let → block scope, reassignable
const ⭐ → block scope, binding LOCKED — but objects/arrays are still mutable ⭐

const = a fixed ADDRESS 🏠, movable furniture
```

---

### Q: What is hoisting?

The easiest way to remember:

```
Before running your code, JavaScript SCANS it and moves every
DECLARATION to the top of its scope. The ASSIGNMENT stays put. ⭐
```

```js
console.log(x);   // undefined   ← not an error! ⚠️
var x = 5;

console.log(y);   // ❌ ReferenceError: Cannot access 'y' before initialization
let y = 5;
```

What JavaScript actually does:

```
YOU WROTE                    WHAT RUNS
─────────                    ─────────
console.log(x);              var x;              ← DECLARATION hoisted ⭐
var x = 5;                   console.log(x);     → undefined
                             x = 5;              ← ASSIGNMENT stays ⭐
```

```
var  → hoisted AND initialised to undefined  → prints undefined ⚠️
let/const → hoisted but NOT initialised      → the TEMPORAL DEAD ZONE ⭐
            → a ReferenceError, which is much better than a silent undefined
```

Function declarations are hoisted **completely**:

```js
greet();                        // ✅ works!
function greet() { return 'hi'; }

greet2();                       // ❌ TypeError: greet2 is not a function
var greet2 = function () {};    // only `var greet2` was hoisted ⚠️
```

Real-world idea: a **guest list read out before the party.** Every name is registered up front, but the guests only arrive at their own time. `var` guests are registered as "unknown person"; `let` guests are registered but barred from entering until their moment.

#### Easy memory

```
Hoisting = DECLARATIONS move up, ASSIGNMENTS stay ⭐

var       → undefined (silent, dangerous) ⚠️
let/const → TDZ → ReferenceError ⭐ (better — it fails loudly)
function declaration → FULLY hoisted, callable before its definition ⭐
function EXPRESSION  → not callable early
```

---

### Q: Explain scope in JavaScript.

```
GLOBAL   → visible everywhere
FUNCTION → visible inside that function (what `var` respects) ⭐
BLOCK    → visible inside { } (what let/const respect) ⭐
LEXICAL  → an inner function can see the OUTER function's variables ⭐
```

```js
const global = 'g';

function outer() {
  const outerVar = 'o';

  function inner() {
    const innerVar = 'i';
    console.log(global, outerVar, innerVar);   // ✅ can see ALL THREE ⭐
  }

  console.log(innerVar);    // ❌ ReferenceError — can't look INWARD
}
```

```
      GLOBAL
   ┌──────────────────────┐
   │  outer()             │
   │  ┌────────────────┐  │
   │  │  inner()       │  │  inner looks OUT ⬆️  ✅
   │  └────────────────┘  │  outer looks IN  ⬇️  ❌
   └──────────────────────┘
```

⭐ **Lexical scope means "decided by where the code is WRITTEN, not where it's CALLED."** That single sentence is also the foundation of closures.

Real-world idea: **rooms in a house.** From inside a bedroom you can see the corridor and the front door. From the corridor you cannot see inside the bedroom.

#### Easy memory

```
global → function ⭐ (var) → block ⭐ (let/const)
LEXICAL scope = decided by WHERE THE CODE IS WRITTEN ⭐
Inner sees OUT ⬆️ | outer cannot see IN ⬇️
→ this is what makes closures possible ⭐
```

---

### Q: What are the data types in JavaScript?

```
PRIMITIVES (7) — stored BY VALUE, immutable
   string · number · boolean · null · undefined · symbol · bigint

REFERENCE (1)  — stored BY REFERENCE
   object  (which includes arrays, functions, dates, Map, Set) ⭐
```

```js
// PRIMITIVE — copied by value
let a = 1;  let b = a;  b = 2;
console.log(a);        // 1 ✅ unaffected

// REFERENCE — copied by reference ⭐
let x = { n: 1 };  let y = x;  y.n = 2;
console.log(x.n);      // 2 ⚠️ they point at the SAME object
```

```
PRIMITIVE:  a = [1]   b = [1]     two separate boxes ✅
REFERENCE:  x ──┐
                ├──▶ { n: 2 }     one object, two labels ⭐
            y ──┘
```

⭐ **`null` vs `undefined` — the classic follow-up:**

```
undefined → JavaScript's "nothing" — a variable declared but never assigned
null      → YOUR "nothing" — you deliberately set it to empty ⭐
```

```js
typeof undefined   // "undefined"
typeof null        // "object"   ⚠️ a legendary language BUG, kept for compatibility ⭐
null == undefined  // true   (loose)
null === undefined // false  (strict) ⭐
```

#### Easy memory

```
7 PRIMITIVES (by value) + object (by reference) ⭐
string number boolean null undefined symbol bigint

undefined = JS didn't set it | null = YOU set it to empty ⭐
typeof null === "object" ⚠️ a famous bug ⭐
```

---

### Q: `==` vs `===`?

The easiest way to remember:

```
==  → compares AFTER converting the types  (loose)   ⚠️
=== → compares the VALUE **and** the TYPE  (strict)  ✅ always use this ⭐
```

```js
5 == '5'        // true   ⚠️ '5' is converted to 5
5 === '5'       // false  ✅ different types

0 == false      // true   ⚠️
0 === false     // false  ✅

null == undefined   // true  ⚠️
null === undefined  // false ✅

[] == false     // true   ⚠️⚠️ (!)
```

```
==  runs a CONVERSION first → surprising results 💥
=== compares as-is → predictable ⭐
```

⭐ The one legitimate use of `==`:

```js
if (x == null) { }     // true for BOTH null and undefined — a deliberate idiom ⭐
```

#### Easy memory

```
== → converts types first ⚠️ | === → value AND type ✅ ⭐
ALWAYS use === (and !==)
The one exception: x == null catches null AND undefined ⭐
```

---

### Q: What is type coercion?

```
Coercion = JavaScript automatically converting one type into another. ⭐
```

```js
'5' + 3        // "53"    ⚠️ + with a string means CONCATENATION ⭐
'5' - 3        // 2       ⭐ - has no string meaning → both become numbers
'5' * '2'      // 10
1 + true       // 2       (true → 1)
'abc' - 1      // NaN
[] + {}        // "[object Object]"
```

⭐ **The rule that explains almost all of it:**

```
`+` is the ONLY operator with a string meaning.
   → if EITHER side is a string, `+` CONCATENATES ⭐

Every other arithmetic operator converts both sides to NUMBERS ⭐
```

```
'5' + 3  → one side is a string → CONCATENATE → "53"
'5' - 3  → `-` has no string meaning → both to numbers → 2 ⭐
```

Real-world idea: `+` is a word that means two things — "add" and "join". The other operators only mean one thing, so there's no ambiguity.

**How to avoid it:** always use `===`, and convert explicitly with `Number(x)` and `String(x)`.

#### Easy memory

```
Coercion = automatic type conversion ⭐

'5' + 3 = "53"  ← `+` CONCATENATES if either side is a string ⭐
'5' - 3 = 2     ← every OTHER operator converts to NUMBER ⭐

Avoid it: === , Number(x), String(x)
```

---

### Q: What are truthy and falsy values?

⭐ There are exactly **8 falsy values** — memorise the list, and everything else is truthy.

```
false · 0 · -0 · 0n · "" · null · undefined · NaN
```

```js
if ('0')       {}   // ✅ TRUTHY  — a non-empty string ⚠️
if ([])        {}   // ✅ TRUTHY  — an empty array is truthy! ⚠️⭐
if ({})        {}   // ✅ TRUTHY  — an empty object too ⚠️
if ('false')   {}   // ✅ TRUTHY  — a non-empty string ⚠️
```

⭐ **The `[]` and `{}` case is the trap** — people expect an empty array to be falsy.

```js
if (arr) { }              // ⚠️ ALWAYS true — even for []
if (arr.length) { }       // ✅ what you actually meant ⭐
```

And the `||` vs `??` consequence:

```js
const port = input || 3000;   // ⚠️ 0 and "" fall through to 3000 💥
const port = input ?? 3000;   // ✅ only null/undefined do ⭐
```

#### Easy memory

```
8 FALSY: false 0 -0 0n "" null undefined NaN ⭐
EVERYTHING else is truthy — including [] and {} ⚠️⭐

Check an array with arr.length, not arr ⭐
|| replaces 0 and "" 💥 | ?? only replaces null/undefined ⭐
```

---

## Functions & Closures

### Q: What is a closure?

⭐ **The most-asked JavaScript question. Have this memorised word for word.**

The easiest way to remember:

```
A closure = a function that REMEMBERS the variables from where it was
            CREATED, even after that outer function has finished. ⭐
```

```js
function counter() {
  let count = 0;                    // ⭐ this survives!

  return function () {
    count++;
    return count;
  };
}

const inc = counter();     // counter() has FINISHED and returned
inc();   // 1
inc();   // 2              ⭐ `count` is still alive
inc();   // 3
```

```
counter() runs and RETURNS
      ↓
Normally `count` would be garbage collected 🗑️
      ↓
BUT the returned function still REFERENCES it
      ↓
so JavaScript keeps `count` alive, in a private "backpack" ⭐
```

Real-world idea: a **backpack.** The inner function carries a backpack containing the variables it needs. Wherever you take the function, the backpack goes with it.

Another: **a locker key.** The room (`counter()`) is closed, but you still hold the key to one locker inside it, so its contents remain yours — and nobody else's.

#### Why closures matter — three real uses

```js
// 1. PRIVATE data — there is no other way to make a truly private variable ⭐
function bankAccount(initial) {
  let balance = initial;                       // ⭐ nobody outside can touch it
  return {
    deposit: (amt) => (balance += amt),
    getBalance: () => balance
  };
}
const acc = bankAccount(100);
acc.balance;        // undefined ✅ genuinely inaccessible
acc.getBalance();   // 100

// 2. Function factories
const multiply = (x) => (y) => x * y;
const double = multiply(2);   double(5);   // 10 ⭐

// 3. What makes DEBOUNCE and THROTTLE work ⭐
```

⚠️ **The memory-leak caveat to volunteer:**

```
A closure KEEPS its captured variables alive.
Capture a huge array or a DOM node in a long-lived callback
   → it can never be garbage collected 💥
```

#### Interview-ready answer

> A closure is a function that retains access to variables from the scope in which it was created, even after that outer function has returned. JavaScript keeps those variables alive because the inner function still references them. The main uses are data privacy — a variable inside a closure genuinely cannot be reached from outside, which is how the module pattern works — function factories, and utilities like debounce and throttle that need to remember state between calls. The trade-off is memory: a closure keeps whatever it captured alive, so capturing a large object in a long-lived handler can prevent it being garbage collected.

#### Easy memory

```
Closure = a function + the VARIABLES it was born with 🎒 (a backpack) ⭐

Uses: PRIVATE data ⭐ | function factories | debounce/throttle
Cost: captured variables can never be garbage collected ⚠️

"The inner function remembers, even after the outer one has finished." ⭐
```

---

### Q: What is an IIFE?

```
IIFE = Immediately Invoked Function Expression —
       a function that runs the instant it's defined. ⭐

(function () { ... })();
```

```js
(function () {
  const secret = 'hidden';     // ⭐ never touches the global scope
  console.log('runs immediately');
})();

(() => { })();                 // arrow version
```

```
Why it existed: before ES6 modules and `let`, this was the ONLY way
to avoid polluting the global scope. ⭐
```

⚠️ Be honest about relevance:

```
Modern code uses ES MODULES (each file has its own scope) and `let`/`const`.
IIFEs are mostly historical — you'll meet them in older libraries and bundles. ⭐
```

#### Easy memory

```
IIFE = (function(){ ... })();  → runs immediately ⭐
Purpose: private scope BEFORE modules and let/const existed
Today: ES modules do the same job — IIFEs are mostly legacy ⭐
```

---

### Q: What are higher-order functions?

```
A higher-order function TAKES a function as an argument,
or RETURNS a function. ⭐
```

```js
// takes a function
[1, 2, 3].map(n => n * 2);            // ⭐ map, filter, reduce, forEach, sort

// returns a function
const multiply = (x) => (y) => x * y;
```

The four you must be able to write cold:

```js
const nums = [1, 2, 3, 4, 5];

nums.map(n => n * 2);                          // [2,4,6,8,10]  — TRANSFORM ⭐
nums.filter(n => n % 2 === 0);                 // [2,4]         — KEEP some ⭐
nums.reduce((acc, n) => acc + n, 0);           // 15            — COMBINE ⭐
nums.forEach(n => console.log(n));             // undefined     — side effects only
```

```
map    → same LENGTH, different values ⭐
filter → same VALUES, fewer of them ⭐
reduce → MANY values → ONE value ⭐
forEach→ returns NOTHING (can't be chained) ⚠️
```

⭐ `reduce` traced step by step, since that's the one people fumble:

```
[1,2,3,4,5]   acc starts at 0
0+1=1 → 1+2=3 → 3+3=6 → 6+4=10 → 10+5=15 ✅
```

#### Easy memory

```
HOF = takes and/or returns a FUNCTION ⭐
map (transform) | filter (keep) | reduce (combine to one) ⭐ | forEach (side effect)
forEach returns undefined → you cannot chain it ⚠️
```

---

### Q: What is currying?

```
Currying = turning f(a, b, c) into f(a)(b)(c) —
           one argument at a time. ⭐
```

```js
// normal
const add = (a, b, c) => a + b + c;
add(1, 2, 3);              // 6

// curried ⭐
const addC = (a) => (b) => (c) => a + b + c;
addC(1)(2)(3);             // 6
```

Why it's useful — **partial application**:

```js
const multiply = (x) => (y) => x * y;

const double = multiply(2);      // ⭐ pre-fill the first argument
const triple = multiply(3);

double(5);    // 10
triple(5);    // 15
```

```
You've already used this shape:
   RxJS operators! map(fn) returns a function that takes the source ⭐
```

Real-world idea: a **coffee order taken in stages** — size, then milk, then sugar. At each step you have a partially completed order you can reuse.

#### Easy memory

```
Currying: f(a,b,c) → f(a)(b)(c) ⭐ — one argument at a time
Benefit: PARTIAL APPLICATION → multiply(2) becomes `double` ⭐
You already use it: every RxJS operator is curried ⭐
```

---

### Q: Explain `call`, `apply` and `bind`.

The easiest way to remember:

```
call  → invoke NOW, arguments as a LIST      f.call(obj, a, b) ⭐
apply → invoke NOW, arguments as an ARRAY    f.apply(obj, [a, b]) ⭐
bind  → do NOT invoke; return a NEW function f.bind(obj)  ⭐
```

⭐ The memory hook: **"C for Comma, A for Array, B for Bind (later)."**

```js
const person = { name: 'Priya' };
function greet(greeting, punctuation) {
  return `${greeting}, ${this.name}${punctuation}`;
}

greet.call(person,  'Hello', '!');       // "Hello, Priya!"   — comma-separated ⭐
greet.apply(person, ['Hello', '!']);     // "Hello, Priya!"   — an array ⭐
const bound = greet.bind(person);        // ⭐ returns a function, doesn't run it
bound('Hello', '!');                     // "Hello, Priya!"
```

All three do the same thing: **set what `this` means inside the function.**

⭐ Where `bind` actually matters — the classic callback bug:

```js
class Timer {
  constructor() { this.seconds = 0; }
  start() {
    setInterval(function () {
      this.seconds++;        // ❌ `this` is NOT the Timer 💥
    }, 1000);
  }
}
```

```js
setInterval(this.tick.bind(this), 1000);   // ✅ fix 1
setInterval(() => this.seconds++, 1000);   // ✅ fix 2 — an arrow function ⭐
```

#### Easy memory

```
call  → NOW, Comma-separated args ⭐
apply → NOW, Array of args ⭐
bind  → LATER, returns a bound function ⭐

All three set `this`.
Modern fix for callbacks: an ARROW FUNCTION (no own `this`) ⭐
```

---

### Q: Arrow functions vs normal functions?

⭐ **The one difference that matters: `this`.**

```
Normal function → gets its OWN `this`, decided by HOW IT'S CALLED ⭐
Arrow function  → has NO own `this`; it inherits from where it was WRITTEN ⭐
```

```js
const obj = {
  name: 'Priya',
  regular() { return this.name; },        // ✅ "Priya" — `this` is obj
  arrow: () => this.name,                 // ❌ undefined — `this` is the outer scope ⭐
};
```

| | Normal | Arrow |
|---|---|---|
| own `this` | ✅ (dynamic) | ❌ inherits (lexical) ⭐ |
| `arguments` object | ✅ | ❌ (use rest `...args`) |
| Usable as a constructor (`new`) | ✅ | ❌ |
| Can be hoisted | ✅ (declarations) | ❌ |
| Best for | object methods, constructors | **callbacks** ⭐ |

```
Use an ARROW for callbacks (it keeps the surrounding `this`) ⭐
Use a NORMAL function for object methods (it needs its own `this`) ⭐
```

Real-world idea: a normal function is a **contractor** who works for whoever hired them (`this` depends on the caller). An arrow function is a **family member** who always belongs to the same household, wherever they go.

#### Easy memory

```
Arrow = NO own `this` — it inherits from where it was WRITTEN ⭐
No `arguments`, no `new`, not hoisted

CALLBACKS → arrow ⭐ | OBJECT METHODS → normal function ⭐
```

---

### Q: How is `this` determined?

⭐ Check the **call site**, in this order:

```
1. `new Foo()`         → `this` = the NEW object            (new binding)
2. f.call/apply/bind   → `this` = what you passed  ⭐       (explicit)
3. obj.method()        → `this` = obj             ⭐        (implicit — the DOT)
4. plain f()           → `this` = undefined (strict) / window (sloppy) ⚠️
5. arrow function      → `this` = the ENCLOSING scope ⭐    (lexical — no own this)
```

```js
function show() { return this; }

show();                     // undefined (strict) / window ⚠️
obj.show();                 // obj ⭐ — the DOT decides
show.call({ a: 1 });        // { a: 1 } ⭐
new show();                 // the new object
```

⭐ **The "lost `this`" bug — the one that appears in real code:**

```js
const obj = { name: 'Priya', greet() { return this.name; } };

const fn = obj.greet;       // ⚠️ detached from the object
fn();                       // undefined 💥 — no dot, no `this`
```

```
"NO DOT, NO THIS." ⭐
```

Fix: `obj.greet.bind(obj)` or `() => obj.greet()`.

#### Easy memory

```
Check the CALL SITE, in order:
new ⭐ → call/apply/bind ⭐ → obj.method() (the DOT ⭐) → plain call (undefined)
Arrow → the ENCLOSING scope, always ⭐

"NO DOT, NO THIS." ⭐  (detaching a method loses `this`)
```

---

## Objects & Prototypes

### Q: What is prototypal inheritance?

```
Every object has a hidden link to another object — its PROTOTYPE.
If a property isn't found, JavaScript looks UP that chain. ⭐
```

```js
const animal = { eats: true };
const dog = Object.create(animal);      // dog's prototype is animal ⭐
dog.barks = true;

dog.barks;   // true  — found on dog
dog.eats;    // true  ⭐ — NOT on dog; found on the prototype
```

```
   dog { barks: true }
     │ __proto__
     ▼
   animal { eats: true }
     │ __proto__
     ▼
   Object.prototype  (toString, hasOwnProperty…)
     │ __proto__
     ▼
    null  ← the end of the chain ⭐
```

Real-world idea: **asking your family for something.** You check your own pocket, then your parent, then your grandparent — until someone has it, or nobody does.

⭐ The point that distinguishes JavaScript from Java:

```
Java       → classes are BLUEPRINTS; objects are built from them
JavaScript → objects inherit DIRECTLY from other OBJECTS ⭐
             `class` is just SYNTACTIC SUGAR over prototypes ⭐
```

#### Easy memory

```
Every object has a __proto__ link → JS looks UP the chain ⭐
Object.create(parent) sets it explicitly
Chain ends at Object.prototype → null ⭐

`class` in JS is SUGAR over prototypes — not real class inheritance ⭐
```

---

### Q: What is the prototype chain?

```
The prototype chain = the lookup path JavaScript walks when it
                      can't find a property on the object itself. ⭐
```

```js
const arr = [1, 2, 3];
arr.map(...)      // not on arr → found on Array.prototype ⭐
arr.toString()    // not on Array.prototype → found on Object.prototype ⭐
arr.foo           // walked to null → undefined ⭐
```

```
arr → Array.prototype → Object.prototype → null
```

⭐ **This is why it matters practically:**

```
1. Every array shares ONE copy of map/filter/reduce ⭐
      → not 1,000 copies for 1,000 arrays
2. A LONG chain means a slower lookup
3. It's why `hasOwnProperty` exists ⭐
```

```js
const obj = { a: 1 };
'toString' in obj;                     // true ⚠️ — inherited from the prototype
obj.hasOwnProperty('toString');        // false ✅ — not its OWN property ⭐
Object.hasOwn(obj, 'toString');        // false ✅ modern form
```

#### Easy memory

```
Property lookup walks UP: object → prototype → … → null ⭐
Methods live ONCE on the prototype, shared by every instance ⭐
`in` includes INHERITED properties ⚠️ | hasOwnProperty checks OWN only ⭐
```

---

### Q: What does `Object.create` do?

```
Object.create(proto) makes a NEW object whose prototype is `proto`. ⭐
```

```js
const parent = { greet() { return 'hello'; } };
const child = Object.create(parent);
child.greet();                       // "hello" ⭐ inherited

Object.create(null);                 // ⭐ an object with NO prototype at all
```

⭐ `Object.create(null)` is genuinely useful:

```js
const map = {};                      // ⚠️ inherits toString, constructor…
map['toString'];                     // a function! not undefined 💥

const safeMap = Object.create(null); // ⭐ a truly empty dictionary
safeMap['toString'];                 // undefined ✅
```

Use it whenever an object is being used as a **pure dictionary** with user-supplied keys.

#### Easy memory

```
Object.create(proto) → a new object with that PROTOTYPE ⭐
Object.create(null) → NO prototype → a safe dictionary ⭐
   ({} inherits toString and can collide with user keys 💥)
```

---

### Q: Shallow copy vs deep copy?

```
SHALLOW → copies the TOP level; nested objects are still SHARED ⚠️
DEEP    → copies EVERYTHING, recursively ✅ ⭐
```

```js
const original = { name: 'Priya', address: { city: 'Chennai' } };

// SHALLOW
const shallow = { ...original };
shallow.name = 'Ravi';                    // ✅ original.name unchanged
shallow.address.city = 'Mumbai';
original.address.city;                    // "Mumbai" 💥 SHARED! ⭐

// DEEP ⭐
const deep = structuredClone(original);   // modern, built-in ⭐
deep.address.city = 'Delhi';
original.address.city;                    // "Mumbai" ✅ unaffected
```

```
SHALLOW copy
   shallow.name ──── its own ✅
   shallow.address ──┐
                     ├──▶ the SAME nested object 💥
   original.address ─┘
```

The deep-copy options:

```
structuredClone(obj)          ⭐ built-in, handles Date/Map/Set/circular refs
JSON.parse(JSON.stringify(x)) ⚠️ loses Date, undefined, functions; breaks on circular refs
lodash cloneDeep              ✅ if you already have lodash
```

⚠️ The `JSON` trick is the one people quote — know its limits:

```js
JSON.parse(JSON.stringify({ d: new Date(), u: undefined, f: () => {} }));
// → { d: "2026-09-02T..." }   ⚠️ Date became a STRING, u and f VANISHED 💥
```

#### Easy memory

```
SHALLOW: { ...obj } or Object.assign → nested objects are SHARED ⚠️ ⭐
DEEP: structuredClone(obj) ⭐ (built-in)

⚠️ JSON.parse(JSON.stringify()) loses Date/undefined/functions + circular refs 💥
Immutable updates in Angular/NgRx need a NEW reference ⭐
```

---

## Asynchronous JavaScript

### Q: What is a callback and what is callback hell?

```
Callback = a function passed into another function, to be called LATER. ⭐
Callback hell = callbacks nested inside callbacks until the code
                slides off the right of the screen. 🔺
```

```js
getUser(1, (user) => {
  getOrders(user.id, (orders) => {
    getOrderDetails(orders[0].id, (details) => {
      getShipping(details.id, (shipping) => {
        console.log(shipping);          // 💥 the "pyramid of doom"
      });
    });
  });
});
```

```
Problems:
   unreadable (the pyramid 🔺)
   error handling repeated at EVERY level ⚠️
   impossible to run steps in parallel
```

Fixed with promises / async-await:

```js
const user = await getUser(1);
const orders = await getOrders(user.id);
const details = await getOrderDetails(orders[0].id);   // ⭐ flat and readable
```

#### Easy memory

```
Callback = "call me back when you're done" ⭐
Callback hell = nesting → the PYRAMID OF DOOM 🔺 + repeated error handling
Fix: Promises → async/await ⭐ (flat, one try/catch)
```

---

### Q: What is a Promise and what are its states?

```
A Promise = an object representing a value that will exist LATER. ⭐

THREE states, and once settled it NEVER changes again ⭐
```

```
   PENDING  ──resolve()──▶  FULFILLED ✅
      │
      └──────reject()───▶  REJECTED ❌

Once settled → IMMUTABLE ⭐ (it can never flip back)
```

```js
const promise = new Promise((resolve, reject) => {
  setTimeout(() => resolve('done'), 1000);
});

promise
  .then(v => console.log(v))        // fulfilled
  .catch(e => console.error(e))     // rejected
  .finally(() => console.log('always'));   // ⭐ runs either way
```

Real-world idea: an **online order.**

```
PENDING   → the order is placed, still shipping 📦
FULFILLED → it arrived ✅
REJECTED  → it was cancelled ❌

Either way, it's DECIDED. It can't un-arrive. ⭐
```

⭐ Promises are **eager** — the executor runs the moment you create it (unlike an RxJS Observable, which is lazy). That contrast is a very common follow-up. → [20 — RxJS](./20-rxjs-operators.md)

#### Easy memory

```
3 states: PENDING → FULFILLED ✅ / REJECTED ❌ — settled is FINAL ⭐
.then / .catch / .finally ⭐

Promise = EAGER (runs immediately) | Observable = LAZY ⭐
```

---

### Q: What is promise chaining?

```
Each .then() RETURNS a new promise, so they can be chained flat
instead of nested. ⭐
```

```js
getUser(1)
  .then(user => getOrders(user.id))         // ⭐ RETURN the promise
  .then(orders => getDetails(orders[0]))
  .then(details => console.log(details))
  .catch(err => console.error(err));        // ⭐ ONE catch for the whole chain
```

⚠️ **The #1 chaining bug — forgetting `return`:**

```js
.then(user => { getOrders(user.id); })     // ❌ nothing is returned
.then(orders => console.log(orders));      // → undefined 💥
```

```
No `return` → the next .then() receives undefined ⭐
(An arrow function with braces needs an explicit `return`.)
```

⭐ And the big win: **a single `.catch()` at the end handles an error from any step** — no repeated error handling.

#### Easy memory

```
.then() returns a NEW promise → chain flat, not nested ⭐
ONE .catch() at the end covers EVERY step ⭐

⚠️ Forgetting `return` inside a .then() → the next step gets undefined 💥
```

---

### Q: What is `async` / `await`?

```
async/await = SYNTAX SUGAR over promises.
              It makes async code READ like synchronous code. ⭐
```

```js
async function loadData() {
  try {
    const user    = await getUser(1);          // ⭐ "pause here"
    const orders  = await getOrders(user.id);
    return orders;
  } catch (err) {                               // ⭐ ONE try/catch for all of it
    console.error(err);
  }
}
```

```
async  → the function ALWAYS returns a PROMISE ⭐
await  → pause until the promise settles (only inside an async function)
try/catch → replaces .catch() ⭐
```

⭐ **The performance trap that gets asked:**

```js
// ❌ SEQUENTIAL — 3 seconds
const a = await getA();      // 1s
const b = await getB();      // 1s
const c = await getC();      // 1s

// ✅ PARALLEL — 1 second ⭐
const [a, b, c] = await Promise.all([getA(), getB(), getC()]);
```

```
await in a loop / one after another = the SUM of the times 🐢
Promise.all = the SLOWEST of them ⚡ ⭐
```

**Only `await` sequentially when the next call genuinely needs the previous result.**

#### Easy memory

```
async → always returns a PROMISE ⭐ | await → pause until settled
try/catch replaces .catch() ⭐

⚠️ Sequential awaits = the SUM of the times 🐢
✅ Independent calls → Promise.all → the SLOWEST one ⚡ ⭐
```

---

### Q: `Promise.all` vs `race` vs `allSettled` vs `any`?

```
all        → ALL succeed → an array of results.  ONE fails → the WHOLE thing fails ⚠️
allSettled → waits for ALL, reports each outcome. NEVER rejects ⭐
race       → the FIRST to SETTLE wins (success OR failure) ⭐
any        → the first to SUCCEED (ignores failures)
```

```js
Promise.all([a, b, c]);          // ⚠️ one rejection kills everything
Promise.allSettled([a, b, c]);   // ⭐ [{status:'fulfilled',value}, {status:'rejected',reason}]
Promise.race([fetchData, timeout(5000)]);   // ⭐ a TIMEOUT pattern
Promise.any([mirror1, mirror2]);            // the first that works
```

```
all         ──▶ needs EVERY result (a dashboard where all data is required)
allSettled  ──▶ show what you CAN, report what failed ⭐ (partial dashboard)
race        ──▶ TIMEOUTS ⭐
any         ──▶ the first working mirror/CDN
```

⭐ `race` for a timeout is the pattern worth showing:

```js
const timeout = (ms) => new Promise((_, rej) => setTimeout(() => rej('timeout'), ms));
await Promise.race([fetch('/api/slow'), timeout(5000)]);   // ⭐
```

#### Easy memory

```
all ⭐        → all-or-nothing (one failure kills it ⚠️)
allSettled ⭐ → never rejects → partial results
race ⭐       → the first to SETTLE → TIMEOUT pattern
any           → the first to SUCCEED
```

---

### Q: Explain the event loop.

⭐ **The most-asked "senior JavaScript" question.**

```
JavaScript is SINGLE-THREADED — one call stack, one thing at a time.
The event loop is what lets it still handle async work. ⭐
```

```
   ┌──────────────┐
   │  CALL STACK  │  ← runs your synchronous code
   └──────┬───────┘
          │ empty?
          ▼
   ┌──────────────────────────┐
   │  MICROTASK QUEUE ⭐       │  promises, queueMicrotask
   │  (drained COMPLETELY     │  ← ALWAYS runs first ⭐
   │   before any macrotask)  │
   └──────┬───────────────────┘
          │ now empty?
          ▼
   ┌──────────────────────────┐
   │  MACROTASK QUEUE         │  setTimeout, setInterval, I/O, UI events
   │  (ONE per loop turn) ⭐   │
   └──────────────────────────┘
```

The rule in one line:

```
Run all SYNC code → drain ALL microtasks ⭐ → take ONE macrotask → repeat
```

```js
console.log(1);                                   // sync
setTimeout(() => console.log(2), 0);              // MACROtask
Promise.resolve().then(() => console.log(3));     // MICROtask ⭐
console.log(4);                                   // sync

// Output: 1 4 3 2 ⭐
```

```
1, 4  → synchronous, straight down the stack
3     → microtask — drained BEFORE any timer ⭐
2     → macrotask — last, even with a 0 ms delay ⭐
```

Real-world idea: a **doctor's clinic.**

```
Current patient       → the call stack
EMERGENCY queue ⭐    → microtasks: cleared COMPLETELY before anyone else
Normal appointments   → macrotasks: ONE at a time, then check emergencies again ⭐
```

⚠️ And the consequence to volunteer:

```
An infinite microtask chain STARVES the macrotask queue —
timers and UI events never run, and the page freezes 💥
```

#### Interview-ready answer

> JavaScript is single-threaded with one call stack, so the event loop is what allows it to handle asynchronous work. Synchronous code runs on the stack first. When the stack is empty, the event loop drains the entire microtask queue — promise callbacks and `queueMicrotask` — and only then takes a single task from the macrotask queue, such as a `setTimeout` callback or an I/O event, before checking microtasks again. That ordering is why a promise callback always runs before a `setTimeout(…, 0)`. It also means a long synchronous function or an endless chain of microtasks blocks rendering entirely.

#### Easy memory

```
SYNC → drain ALL microtasks ⭐ → ONE macrotask → repeat

MICROtask ⭐ = promises, queueMicrotask, MutationObserver  (the EMERGENCY queue)
MACROtask   = setTimeout, setInterval, I/O, UI events

1 4 3 2 ⭐ — memorise that output
setTimeout(fn, 0) still runs AFTER every promise ⭐
```

---

### Q: Microtask vs macrotask queues?

```
MICROTASK ⭐  → promise .then/.catch/.finally, await, queueMicrotask, MutationObserver
              → the queue is drained COMPLETELY every turn

MACROTASK   → setTimeout, setInterval, setImmediate, I/O, UI events
              → exactly ONE is taken per loop turn ⭐
```

```
The whole difference in one line:
   microtasks are drained ALL AT ONCE ⭐
   macrotasks are taken ONE AT A TIME ⭐
```

```js
setTimeout(() => console.log('macro 1'), 0);
Promise.resolve().then(() => {
  console.log('micro 1');
  Promise.resolve().then(() => console.log('micro 2'));   // ⭐ added DURING the drain
});
setTimeout(() => console.log('macro 2'), 0);

// micro 1, micro 2, macro 1, macro 2 ⭐
// note: micro 2 was queued during the drain and STILL ran before any timer ⭐
```

#### Easy memory

```
Microtasks ⭐ drained COMPLETELY (even ones added mid-drain)
Macrotasks — ONE per turn ⭐

Promises ALWAYS beat setTimeout, even setTimeout(fn, 0) ⭐
Infinite microtasks → the UI freezes 💥
```

---

### Q: `setTimeout` vs `setInterval`?

```
setTimeout  → run ONCE after a delay
setInterval → run REPEATEDLY every interval ⚠️
```

```js
const id = setTimeout(fn, 1000);   clearTimeout(id);
const id2 = setInterval(fn, 1000); clearInterval(id2);   // ⭐ MUST be cleared
```

⚠️ **The `setInterval` problem, and the better pattern:**

```
setInterval fires on schedule regardless of whether the previous
run has FINISHED.
      ↓
An HTTP call that takes 3s on a 1s interval → calls PILE UP 💥
```

```js
// ✅ a self-scheduling timeout — the next run starts only after this one ends ⭐
function poll() {
  fetchData().finally(() => setTimeout(poll, 1000));
}
```

Also: the delay is a **minimum, not a guarantee** — a busy stack delays the callback.

#### Easy memory

```
setTimeout = ONCE | setInterval = REPEAT (always clearInterval! ⚠️)
⚠️ setInterval doesn't wait for the previous run → overlapping calls 💥
✅ Self-scheduling setTimeout for polling ⭐
The delay is a MINIMUM, not a guarantee ⭐
```

---

## ES6+ Features

### Q: What is destructuring?

```
Destructuring = pull values OUT of an object or array into variables. ⭐
```

```js
// OBJECT — matched by KEY NAME ⭐
const { name, age, city = 'Chennai' } = user;        // with a default
const { name: fullName } = user;                      // renamed ⭐
const { address: { city } } = user;                   // nested

// ARRAY — matched by POSITION ⭐
const [first, second] = [1, 2, 3];
const [a, , c] = [1, 2, 3];                           // skip one
const [head, ...tail] = [1, 2, 3];                    // rest ⭐

// swap with no temp variable ⭐
let x = 1, y = 2;
[x, y] = [y, x];

// in a function signature ⭐
function greet({ name, age = 0 }) { }
```

```
OBJECT → by NAME  ⭐ (order doesn't matter)
ARRAY  → by POSITION ⭐ (order is everything)
```

⚠️ The trap:

```js
const { name } = null;      // 💥 TypeError: Cannot destructure property of null
const { name } = obj ?? {}; // ✅ safe ⭐
```

#### Easy memory

```
Object → by NAME ⭐ { name, age = 0 } | rename: { name: fullName }
Array  → by POSITION ⭐ [a, , c] | rest: [head, ...tail]
Swap: [x, y] = [y, x] ⭐
⚠️ Destructuring null throws → use `?? {}` ⭐
```

---

### Q: Spread vs rest?

⭐ **Same three dots, opposite jobs — decided by WHERE they appear.**

```
SPREAD → EXPANDS one thing into many   (used where VALUES go) ⭐
REST   → COLLECTS many into one array  (used where NAMES go) ⭐
```

```js
// SPREAD — expand
const arr2 = [...arr1, 4];                  // copy + add
const merged = { ...obj1, ...obj2 };        // merge (later wins ⭐)
Math.max(...numbers);                        // array → arguments

// REST — collect
function sum(...nums) { }                    // ⭐ in a SIGNATURE
const [first, ...others] = arr;              // ⭐ in a PATTERN
const { name, ...rest } = obj;               // everything else ⭐
```

```
In a CALL or a LITERAL   → SPREAD (expanding) ⭐
In a SIGNATURE or PATTERN → REST (collecting) ⭐
```

⭐ Spread is the standard **immutable update**, which is exactly what NgRx reducers and `OnPush` need:

```js
const updated = { ...state, loading: true };   // ⭐ a NEW object, new reference
```

⚠️ But it's a **shallow** copy — nested objects are still shared.

#### Easy memory

```
SPREAD ⭐ expands: [...arr] { ...obj } fn(...args)
REST ⭐ collects: function f(...args) | const [a, ...rest] | { x, ...rest }

Position decides which it is ⭐
Spread = the immutable-update tool (NgRx, OnPush) — but SHALLOW ⚠️
```

---

### Q: What are template literals?

```js
const name = 'Priya';
const greeting = `Hello, ${name}!`;                    // ⭐ interpolation

const multiline = `line 1
line 2`;                                                // ⭐ real newlines

const expr = `Total: ${price * qty}`;                   // any expression
```

```
Backticks ` → interpolation ${} + real multi-line strings ⭐
```

Tagged templates (rarer, but worth recognising):

```js
sql`SELECT * FROM users WHERE id = ${id}`;    // the tag function can escape the value ⭐
```

#### Easy memory

```
Backticks ` + ${expression} ⭐ + genuine multi-line
Tagged templates: tag`text ${v}` → the function controls the interpolation
```

---

### Q: What are default parameters?

```js
function greet(name = 'Guest', greeting = 'Hello') {
  return `${greeting}, ${name}`;
}
greet();                    // "Hello, Guest"
greet('Priya');             // "Hello, Priya"
greet(undefined, 'Hi');     // "Hi, Guest"  ⭐ undefined triggers the default
greet(null);                // "Hello, null" ⚠️ null does NOT ⭐
```

```
Only `undefined` triggers a default. `null` does not. ⭐
```

⭐ Defaults can reference earlier parameters:

```js
function makeUser(name, role = `${name}_user`) { }    // ⭐ evaluated at CALL time
```

#### Easy memory

```
function f(a = 1) — only `undefined` triggers it ⚠️ null does NOT ⭐
Defaults are evaluated at CALL time and can use earlier parameters ⭐
```

---

### Q: How do ES modules work?

```js
// export
export const name = 'Priya';
export function greet() {}
export default class User {}          // ⭐ ONE default per file

// import
import User, { name, greet } from './user';      // default + named
import * as utils from './utils';                 // namespace
const mod = await import('./heavy');              // ⭐ DYNAMIC import (lazy!)
```

```
NAMED exports  → many per file → braces on import → the name must MATCH ⭐
DEFAULT export → ONE per file  → no braces → you may RENAME it freely ⭐
```

⭐ Two properties that matter:

```
1. Each module has its OWN SCOPE — no global pollution ⭐
2. Imports are STATIC → bundlers can TREE-SHAKE unused exports ⭐
   → which is why `import { x }` beats importing a whole namespace
```

`import()` returns a promise — this is exactly what Angular's `loadChildren` uses for lazy routes.

#### Easy memory

```
export / export default (ONE) / import { } / import X / import * as ⭐
Own scope per file ⭐ | STATIC imports → TREE-SHAKING ⭐
Dynamic import() → a Promise → Angular's lazy loading ⭐
```

---

### Q: `Map`/`Set` vs objects/arrays?

```
Map → keys of ANY type ⭐, keeps INSERTION ORDER, has .size ⭐
Set → UNIQUE values ⭐
```

```js
const map = new Map();
map.set('a', 1);
map.set(42, 'num');            // ⭐ a number key
map.set(objKey, 'obj');        // ⭐ an OBJECT as a key — impossible with {} ⭐
map.get(objKey);  map.size;  map.has('a');

const set = new Set([1, 2, 2, 3]);
set.size;                       // 3 ⭐ duplicates removed
[...new Set(arr)];              // ⭐ the one-line dedupe
```

| | Object | Map |
|---|---|---|
| Key types | string / symbol only ⚠️ | **anything** ⭐ |
| Order | mostly insertion (integer keys reorder ⚠️) | guaranteed insertion ⭐ |
| Size | `Object.keys(o).length` | `.size` ⭐ |
| Prototype keys | inherits `toString` etc. ⚠️ | clean ⭐ |
| Frequent add/delete | slower | optimised ⭐ |

⭐ Use a **Map** when keys aren't strings, or when you add and remove often. Use an **object** for plain records and JSON.

#### Easy memory

```
Map ⭐ → ANY key type, insertion order, .size, no prototype keys
Set ⭐ → unique values → [...new Set(arr)] dedupes in one line ⭐

Object → string keys only, inherits prototype keys ⚠️, JSON-friendly
```

---

### Q: What is optional chaining (`?.`)?

```js
user?.address?.city             // undefined instead of a TypeError ⭐
user?.getName?.()               // safe METHOD call ⭐
arr?.[0]                        // safe INDEX access ⭐
```

```
Before:  user && user.address && user.address.city   😩
After:   user?.address?.city                          ✅ ⭐
```

⚠️ The nuance:

```
?. short-circuits ONLY on null or undefined.
It does NOT protect against 0, "" or false. ⭐
```

#### Easy memory

```
?. → returns undefined instead of throwing ⭐
Forms: a?.b | a?.[0] | a?.() ⭐
Short-circuits on null/undefined ONLY ⭐
```

---

### Q: What is nullish coalescing (`??`)?

```
??  → a fallback ONLY for null or undefined ⭐
||  → a fallback for ANY falsy value (0, "", false, NaN too) ⚠️
```

```js
const count = 0;
count || 10;      // 10  ⚠️ WRONG — 0 is a valid count! 💥
count ?? 10;      // 0   ✅ ⭐

const name = '';
name || 'Guest';  // "Guest" ⚠️
name ?? 'Guest';  // ""      ✅ ⭐
```

⭐ This is the single most useful small feature in modern JavaScript — it fixes a bug class that `||` silently created for years.

Also:

```js
config.retries ??= 3;      // ⭐ assign only if null/undefined
```

#### Easy memory

```
?? ⭐ → only null/undefined | || ⚠️ → any falsy (0 "" false)

count = 0 → count || 10 gives 10 💥 | count ?? 10 gives 0 ✅ ⭐
??= assigns only when nullish
```

---

### Q: What are generators?

```
A generator can PAUSE and RESUME. `yield` hands a value out and freezes
the function until you ask for the next one. ⭐
```

```js
function* idGenerator() {
  let id = 1;
  while (true) {
    yield id++;              // ⭐ pause here, hand out a value
  }
}

const gen = idGenerator();
gen.next().value;   // 1
gen.next().value;   // 2      ⭐ resumed exactly where it stopped
```

```
Normal function → runs to the end, once ▶️
Generator       → runs → PAUSES at yield ⏸ → resumes on next() ⭐
```

Real-world idea: a **ticket dispenser.** It gives you one ticket and waits; it doesn't print the whole roll.

Where they're actually used:

```
Infinite sequences without infinite memory ⭐
Lazy iteration over a huge or paged dataset ⭐
Redux-Saga (the async model NgRx Effects replaces)
```

⚠️ Be honest: generators are rare in day-to-day Angular work — `async/await` covers most of what they were used for.

#### Easy memory

```
function* + yield ⭐ → pause ⏸ and resume ▶️ (a ticket dispenser 🎫)
gen.next() → { value, done }
Uses: infinite/lazy sequences, Redux-Saga
Rare in Angular — async/await covers most cases ⭐
```

---

## Advanced

### Q: Debounce vs throttle?

⭐ **Asked constantly, and confused constantly.**

```
DEBOUNCE → wait for a PAUSE, then run ONCE ⭐   → search, resize, autosave
THROTTLE → run at most ONCE per interval ⭐     → scroll, mousemove
```

```
Events:   x x x x x        x x x        x

DEBOUNCE: ..............R  ........R  ....R
          (runs only after the events STOP) ⭐

THROTTLE: R....R....R....  R....R...  R
          (runs on a fixed rhythm, ignoring the rest) ⭐
```

```js
function debounce(fn, delay) {
  let timer;                                    // ⭐ a CLOSURE holds the timer
  return (...args) => {
    clearTimeout(timer);                        // ⭐ cancel the previous
    timer = setTimeout(() => fn(...args), delay);
  };
}

function throttle(fn, limit) {
  let waiting = false;
  return (...args) => {
    if (waiting) return;                        // ⭐ ignore
    fn(...args);
    waiting = true;
    setTimeout(() => (waiting = false), limit);
  };
}
```

Real-world ideas:

```
DEBOUNCE → a LIFT: it waits until nobody else gets in, THEN closes ⭐ 🛗
THROTTLE → a BUS: it leaves every 10 minutes regardless of who arrives ⭐ 🚌
```

```
Search box    → DEBOUNCE ⭐ (don't search per keystroke)
Scroll handler→ THROTTLE ⭐ (must respond, but not 100×/second)
```

⭐ Note both are built on **closures** — the timer variable survives between calls. That ties two interview questions together.

#### Easy memory

```
DEBOUNCE ⭐ = wait for the PAUSE, run ONCE 🛗 (a lift) → SEARCH, resize, autosave
THROTTLE ⭐ = at most once per interval 🚌 (a bus)    → SCROLL, mousemove

Both use a CLOSURE to hold the timer ⭐
RxJS: debounceTime / throttleTime ⭐
```

---

### Q: What is memoization?

```
Memoization = CACHE a function's result, keyed by its arguments,
              so the same input never recomputes. ⭐
```

```js
function memoize(fn) {
  const cache = new Map();                       // ⭐ a closure holds the cache
  return (...args) => {
    const key = JSON.stringify(args);
    if (cache.has(key)) return cache.get(key);   // ⭐ a HIT — no work
    const result = fn(...args);
    cache.set(key, result);
    return result;
  };
}

const slowSquare = (n) => { /* expensive */ return n * n; };
const fast = memoize(slowSquare);
fast(5);   // computed
fast(5);   // ⭐ instant — from the cache
```

```
Only safe for PURE functions ⭐
   (same input → same output, no side effects)
An impure function would serve a stale result 💥
```

⭐ Where you already meet it: **NgRx `createSelector` is memoisation** → [21 — NgRx](./21-ngrx.md), and Angular's **pure pipes** are the same idea.

⚠️ The cache grows forever unless you bound it (an LRU).

#### Easy memory

```
Memoize = cache results by ARGUMENTS ⭐ (a closure holds the Map)
Only for PURE functions ⭐ | the cache grows forever ⚠️ (bound it)
You already use it: NgRx createSelector + Angular pure pipes ⭐
```

---

### Q: Implement a deep clone.

```js
function deepClone(obj, seen = new WeakMap()) {
  if (obj === null || typeof obj !== 'object') return obj;   // primitives
  if (obj instanceof Date) return new Date(obj);              // ⭐ special types
  if (obj instanceof Map)  return new Map([...obj].map(([k, v]) => [k, deepClone(v, seen)]));
  if (seen.has(obj)) return seen.get(obj);                    // ⭐ CIRCULAR refs

  const clone = Array.isArray(obj) ? [] : {};
  seen.set(obj, clone);
  for (const key of Object.keys(obj)) clone[key] = deepClone(obj[key], seen);
  return clone;
}
```

⭐ The three things that make this a *good* answer rather than a naive one:

```
1. Handling Date / Map / Set — a naive clone turns a Date into {} 💥
2. A WeakMap for CIRCULAR references — otherwise infinite recursion 💥
3. Saying "in production I'd use structuredClone()" ⭐
```

```js
const copy = structuredClone(obj);      // ⭐ built-in, handles all of the above
```

#### Easy memory

```
Recurse + handle Date/Map/Set + a WeakMap for CIRCULAR refs ⭐
Production: structuredClone(obj) ⭐
⚠️ JSON.parse(JSON.stringify()) loses Date/undefined/functions 💥
```

---

### Q: Implement a generic curry function.

```js
function curry(fn) {
  return function curried(...args) {
    if (args.length >= fn.length) return fn(...args);          // ⭐ enough args → run
    return (...next) => curried(...args, ...next);             // ⭐ else collect more
  };
}

const add = (a, b, c) => a + b + c;
const c = curry(add);
c(1)(2)(3);      // 6
c(1, 2)(3);      // 6 ⭐ any grouping works
c(1)(2, 3);      // 6
```

```
fn.length = the number of DECLARED parameters ⭐ — that's the trick ⭐
```

#### Easy memory

```
curry: collect args until args.length >= fn.length ⭐
fn.length = the declared parameter count ⭐
c(1)(2)(3) and c(1,2)(3) both work
```

---

### Q: What is event delegation?

```
Event delegation = attach ONE listener to a PARENT instead of
                   one per child, and use event.target. ⭐
```

```js
// ❌ 1,000 listeners
document.querySelectorAll('.item').forEach(el =>
  el.addEventListener('click', handler));

// ✅ ONE listener ⭐
document.querySelector('#list').addEventListener('click', (e) => {
  const item = e.target.closest('.item');       // ⭐ closest() handles inner elements
  if (item) handleClick(item.dataset.id);
});
```

```
✅ ONE listener instead of 1,000 → far less memory ⭐
✅ Works for elements added LATER ⭐⭐ (no re-binding after a render)
```

⭐ That second point is the real reason: dynamically added rows work automatically.

It relies on **bubbling** — the click travels up from the child to the parent.

#### Easy memory

```
ONE listener on the PARENT + e.target.closest() ⭐
✅ less memory | ✅ works for elements added LATER ⭐⭐
Relies on BUBBLING ⭐
```

---

### Q: Explain event bubbling and capturing.

```
CAPTURING → the event travels DOWN, window → target   (phase 1)
TARGET    → it reaches the element                     (phase 2)
BUBBLING  → it travels UP, target → window  ⭐         (phase 3, the DEFAULT)
```

```
        window
          │  ⬇️ CAPTURE          ⬆️ BUBBLE
        document
          │
         div
          │
       button  ← the TARGET
```

```js
el.addEventListener('click', fn);          // BUBBLING (default) ⭐
el.addEventListener('click', fn, true);    // CAPTURING ⭐
```

```
e.stopPropagation()  → stop it travelling further ⭐
e.preventDefault()   → stop the DEFAULT behaviour (form submit, link navigation) ⭐
```

⚠️ Those two are **different things** and get confused constantly:

```
stopPropagation → the event stops moving, but the default STILL happens ⭐
preventDefault  → the default is cancelled, but the event KEEPS bubbling ⭐
```

#### Easy memory

```
CAPTURE ⬇️ → TARGET → BUBBLE ⬆️ (the default) ⭐
addEventListener(type, fn, true) → capture phase ⭐

stopPropagation = stop TRAVELLING ⭐ | preventDefault = stop the DEFAULT ACTION ⭐
Bubbling is what makes event delegation possible ⭐
```

---

## Tricky Output-Based Questions

⭐ Your rounds have been output-based — treat these as the JavaScript equivalent of [23 — Java Predict the Output](./23-java-output-tricky-questions.md).

### Q1
```js
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 0);
}
```
**Output:** `3 3 3`

```
`var` is FUNCTION-scoped → all three callbacks share ONE `i`
The loop finishes (i = 3) BEFORE any timer fires ⭐
Fix: `let` → block-scoped → a NEW binding per iteration → 0 1 2 ⭐
```

### Q2
```js
console.log(1);
setTimeout(() => console.log(2), 0);
Promise.resolve().then(() => console.log(3));
console.log(4);
```
**Output:** `1 4 3 2` ⭐

```
1, 4 → synchronous
3    → MICROtask — drained before any macrotask ⭐
2    → MACROtask — last, even at 0 ms ⭐
```

### Q3
```js
console.log(typeof null);
console.log(0.1 + 0.2 === 0.3);
console.log([] + []);
console.log(!!"false");
```
**Output:** `"object"`, `false`, `""`, `true`

```
typeof null === "object"     → a legacy language bug ⭐
0.1 + 0.2 = 0.30000000000000004 → IEEE-754 binary fractions ⭐
[] + []  → both coerce to "" → "" ⭐
!!"false" → a NON-EMPTY string is truthy ⭐
```

### Q4
```js
const obj = {
  name: "Priya",
  regular: function () { return this.name; },
  arrow: () => this.name,
};
console.log(obj.regular());
console.log(obj.arrow());
```
**Output:** `"Priya"`, `undefined`

```
regular → called with a DOT → `this` is obj ⭐
arrow   → NO own `this` → inherits the module/global scope → undefined ⭐
```

### Q5
```js
let a = { n: 1 };
let b = a;
a.n = 2;
a = { n: 3 };
console.log(b.n);
```
**Output:** `2`

```
b points at the ORIGINAL object
a.n = 2   → MUTATES the shared object → b.n is 2 ⭐
a = {n:3} → REASSIGNS `a` only → b still points at the first object ⭐
```

⭐ Exactly the same idea as Java's pass-by-value → [05 — Core Java](./05-java.md).

---

## Quick Revision Sheet

```
FUNDAMENTALS
  var (function scope, leaks ⚠️) | let | const ⭐ (locks the BINDING, not contents)
  Hoisting: declarations move up; var → undefined ⚠️, let/const → TDZ ⭐
  LEXICAL scope = where the code is WRITTEN ⭐ → the basis of closures
  7 primitives (by value) + object (by reference) ⭐
  typeof null === "object" ⚠️ | == converts ⚠️ | === always ⭐
  8 FALSY: false 0 -0 0n "" null undefined NaN ⭐ ([] and {} are TRUTHY! ⚠️)

FUNCTIONS
  CLOSURE ⭐⭐ = a function + the variables it was born with 🎒
     → private data, factories, debounce/throttle | cost: they stay in memory
  HOF: map (transform) filter (keep) reduce (combine) ⭐
  Currying: f(a)(b)(c) → partial application ⭐
  call (Comma) | apply (Array) | bind (later) ⭐
  ARROW: no own `this` ⭐, no arguments, no new → use for CALLBACKS
  `this`: new → call/bind → obj.method() (the DOT) → undefined | arrow = lexical
     "NO DOT, NO THIS" ⭐

PROTOTYPES
  Lookup walks UP: obj → prototype → … → null ⭐
  `class` is SUGAR over prototypes ⭐ | Object.create(null) = a safe dictionary ⭐
  SHALLOW { ...obj } shares nested objects ⚠️ | DEEP: structuredClone ⭐

ASYNC
  Promise: PENDING → FULFILLED/REJECTED, settled is FINAL ⭐ | EAGER (Observable = lazy)
  Chaining: forgetting `return` gives undefined 💥 | ONE .catch covers the chain ⭐
  async/await: sequential awaits = the SUM 🐢 → Promise.all = the SLOWEST ⚡ ⭐
  all (all-or-nothing) | allSettled ⭐ | race (TIMEOUT ⭐) | any
  EVENT LOOP ⭐⭐: sync → ALL microtasks → ONE macrotask → repeat
     → "1 4 3 2" ⭐ | setTimeout(fn,0) still loses to a promise ⭐

ES6+
  Destructure: object by NAME ⭐, array by POSITION ⭐
  SPREAD expands / REST collects — position decides ⭐
  Map (any key ⭐) | Set (unique ⭐ → [...new Set(arr)])
  ?. safe access ⭐ | ?? only null/undefined ⭐ (|| also catches 0 and "" 💥)
  Modules: own scope + STATIC imports → tree-shaking ⭐ | import() = lazy

ADVANCED
  DEBOUNCE ⭐ wait for the pause 🛗 (search) | THROTTLE ⭐ fixed rhythm 🚌 (scroll)
  Memoize = cache by args ⭐ (= NgRx createSelector)
  Event delegation ⭐ = ONE parent listener → works for elements added LATER ⭐⭐
  Bubbling ⬆️ (default) | stopPropagation ≠ preventDefault ⭐
```

---

**Related files:** [19 — JavaScript Variables Made Easy](./19-javascript-variables-easy.md) · [03 — TypeScript](./03-typescript.md) · [04 — Angular](./04-angular.md) · [20 — RxJS](./20-rxjs-operators.md) · [09 — Coding Problems](./09-coding-problems.md)
