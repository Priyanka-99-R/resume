# JavaScript — Interview Q&A

> Lateral hire (4+ years) Full Stack Developer prep. Concise but complete answers with code examples.

## Fundamentals

### Q: What is the difference between `var`, `let`, and `const`?
- `var`: function-scoped, hoisted and initialized to `undefined`, can be re-declared and re-assigned. Attaches to `window` when declared globally.
- `let`: block-scoped, hoisted but in the **Temporal Dead Zone (TDZ)** until declared, can be re-assigned but not re-declared in the same scope.
- `const`: block-scoped, must be initialized at declaration, cannot be re-assigned. The binding is constant, **not the value** — object/array contents can still mutate.

```js
function demo() {
  if (true) {
    var a = 1;   // function scoped
    let b = 2;   // block scoped
    const c = 3; // block scoped
  }
  console.log(a); // 1
  // console.log(b); // ReferenceError
}

const obj = { n: 1 };
obj.n = 2; // OK — mutating, not re-assigning
// obj = {}; // TypeError
```

### Q: What is hoisting?
Hoisting is JavaScript moving **declarations** to the top of their scope during the compile phase. `var` declarations are hoisted and initialized to `undefined`. Function declarations are hoisted entirely (callable before definition). `let`/`const` are hoisted but stay in the TDZ — accessing them before declaration throws. Function **expressions** and arrow functions are not hoisted as callable.

```js
console.log(x);   // undefined (var hoisted)
var x = 5;

foo();            // "hi" (function declaration hoisted)
function foo() { console.log("hi"); }

// bar();         // ReferenceError (TDZ)
let bar = () => {};
```

### Q: Explain scope in JavaScript.
Scope determines variable accessibility. Types:
- **Global scope**: accessible everywhere.
- **Function scope**: variables declared inside a function (`var`).
- **Block scope**: `{ }` blocks for `let`/`const`.
- **Lexical scope**: inner functions access variables of their outer (enclosing) functions, determined by where code is written, not where it's called. This is the basis of closures.

```js
let g = "global";
function outer() {
  let o = "outer";
  function inner() {
    console.log(g, o); // can read both via lexical scope
  }
  inner();
}
```

### Q: What are the data types in JavaScript?
**Primitives** (immutable, copied by value): `string`, `number`, `bigint`, `boolean`, `undefined`, `null`, `symbol`.
**Non-primitive** (copied by reference): `object` (includes arrays, functions, dates).

```js
typeof "a";        // "string"
typeof 1;          // "number"
typeof 10n;        // "bigint"
typeof true;       // "boolean"
typeof undefined;  // "undefined"
typeof null;       // "object"  (historical bug)
typeof Symbol();   // "symbol"
typeof {};         // "object"
typeof function(){};// "function"
```

### Q: Difference between `==` and `===`?
- `==` (loose equality) compares **after type coercion**.
- `===` (strict equality) compares value **and** type, no coercion. Prefer `===`.

```js
0 == "0";     // true  (coerced)
0 === "0";    // false (different types)
null == undefined;  // true
null === undefined; // false
NaN === NaN;        // false (use Number.isNaN)
```

### Q: What is type coercion?
Automatic conversion of values from one type to another. **Implicit** happens during operations (`+`, `==`, template strings); **explicit** is manual (`Number()`, `String()`). The `+` operator favors string concatenation if either operand is a string; other arithmetic operators coerce to numbers.

```js
"5" + 1;   // "51"  (number -> string)
"5" - 1;   // 4     (string -> number)
true + 1;  // 2
[] + {};   // "[object Object]"
Number(""); // 0
```

### Q: What are truthy and falsy values?
**Falsy** values (coerce to `false`): `false`, `0`, `-0`, `0n`, `""`, `null`, `undefined`, `NaN`. **Everything else is truthy** — including `"0"`, `"false"`, `[]`, `{}`, and functions.

```js
if ([]) console.log("empty array is truthy"); // logs
if (!"") console.log("empty string is falsy"); // logs
Boolean("0"); // true
```

## Functions & Closures

### Q: What is a closure? Give an example.
A closure is a function that **retains access to its lexical scope** even after the outer function has returned. It "remembers" the variables of the environment in which it was created. Used for data privacy, factories, and stateful functions.

```js
function counter() {
  let count = 0;             // private state
  return function () {
    count++;
    return count;
  };
}
const inc = counter();
inc(); // 1
inc(); // 2  -- count persists in the closure
```

### Q: What is an IIFE?
**Immediately Invoked Function Expression** — a function defined and executed at once. Historically used to create a private scope and avoid polluting the global namespace (before block scope / modules existed).

```js
(function () {
  const secret = "hidden";
  console.log("runs immediately");
})();

// arrow version
(() => console.log("iife"))();
```

### Q: What are higher-order functions?
Functions that take other functions as arguments and/or return functions. `map`, `filter`, `reduce`, `forEach` are built-in examples.

```js
const nums = [1, 2, 3, 4];
const doubled = nums.map(n => n * 2);          // [2,4,6,8]
const evens = nums.filter(n => n % 2 === 0);   // [2,4]
const sum = nums.reduce((acc, n) => acc + n, 0); // 10

function withLogging(fn) {              // returns a function
  return (...args) => {
    console.log("calling with", args);
    return fn(...args);
  };
}
```

### Q: What is currying?
Transforming a function with multiple arguments into a sequence of functions each taking a single argument. Enables partial application and reusable specialized functions.

```js
const add = a => b => c => a + b + c;
add(1)(2)(3); // 6

const multiply = a => b => a * b;
const double = multiply(2);
double(5); // 10
```

### Q: Explain `call`, `apply`, and `bind`.
All set the `this` context of a function.
- `call(thisArg, arg1, arg2)`: invokes immediately, args listed individually.
- `apply(thisArg, [args])`: invokes immediately, args as an array.
- `bind(thisArg, ...args)`: returns a **new function** with `this` bound (does not invoke).

```js
const person = { name: "Priya" };
function greet(greeting, punct) {
  return `${greeting}, ${this.name}${punct}`;
}
greet.call(person, "Hi", "!");      // "Hi, Priya!"
greet.apply(person, ["Hello", "."]);// "Hello, Priya."
const bound = greet.bind(person, "Hey");
bound("?");                          // "Hey, Priya?"
```

### Q: Difference between arrow functions and normal functions?
- **`this` binding**: arrow functions have no own `this` — they inherit it lexically from the enclosing scope. Normal functions get `this` based on how they're called.
- Arrows have **no `arguments` object** (use rest params), cannot be used as **constructors** (no `new`), and have no `prototype`.
- Arrows can't be used as object methods that need dynamic `this`.

```js
const obj = {
  val: 42,
  normal: function () { return this.val; }, // this = obj
  arrow: () => this.val,                     // this = outer (undefined)
};
obj.normal(); // 42
obj.arrow();  // undefined
```

### Q: How is `this` determined in JavaScript?
Depends on the call site:
1. **Default**: global object (or `undefined` in strict mode).
2. **Implicit**: the object before the dot (`obj.method()` → `obj`).
3. **Explicit**: `call`/`apply`/`bind`.
4. **`new`**: the newly created instance.
5. **Arrow function**: inherited lexically (none of the above apply).

```js
function show() { return this; }
const o = { show };
o.show();          // o (implicit)
show.call({a:1});  // {a:1} (explicit)
```

## Objects & Prototypes

### Q: What is prototypal inheritance?
Objects inherit properties/methods from other objects via the **prototype** link. When a property isn't found on an object, the engine walks up the prototype chain until found or `null` is reached.

```js
const animal = {
  eat() { return "eating"; },
};
const dog = Object.create(animal);
dog.bark = () => "woof";
dog.eat();  // "eating"  (inherited)
dog.bark(); // "woof"
```

### Q: What is the prototype chain?
The series of links between objects via `[[Prototype]]` (accessible as `__proto__` / `Object.getPrototypeOf`). Lookups traverse this chain. It ends at `Object.prototype`, whose prototype is `null`.

```js
const arr = [1, 2];
// arr -> Array.prototype -> Object.prototype -> null
arr.hasOwnProperty("length"); // method found on Object.prototype
Object.getPrototypeOf(arr) === Array.prototype; // true
```

### Q: What does `Object.create` do?
Creates a new object with the specified prototype object. Useful for clean prototypal inheritance without constructor functions.

```js
const proto = { greet() { return "hi"; } };
const obj = Object.create(proto);
obj.greet();                       // "hi"
Object.getPrototypeOf(obj) === proto; // true
Object.create(null);               // object with no prototype
```

### Q: Difference between shallow copy and deep copy?
- **Shallow copy**: copies top-level properties; nested objects are still shared by reference. (`{...obj}`, `Object.assign`, `Array.slice`).
- **Deep copy**: recursively copies all nested levels — fully independent. (`structuredClone`, recursion, or `JSON.parse(JSON.stringify())` with limitations).

```js
const original = { a: 1, nested: { b: 2 } };

const shallow = { ...original };
shallow.nested.b = 99;
original.nested.b; // 99  -- shared reference!

const deep = structuredClone(original);
deep.nested.b = 5;
original.nested.b; // unchanged
```

## Asynchronous JavaScript

### Q: What is a callback and what is callback hell?
A **callback** is a function passed to another function to run later (often after async work). **Callback hell** ("pyramid of doom") is deeply nested callbacks that are hard to read and maintain. Solved by Promises / async-await.

```js
// callback hell
getUser(id, (user) => {
  getOrders(user, (orders) => {
    getDetails(orders[0], (details) => {
      console.log(details); // deeply nested
    });
  });
});
```

### Q: What is a Promise and what are its states?
A Promise represents the eventual result of an async operation. States: **pending** → **fulfilled** (resolved) or **rejected**. It's settled once fulfilled or rejected and cannot change again.

```js
const p = new Promise((resolve, reject) => {
  setTimeout(() => resolve("done"), 100);
});
p.then(val => console.log(val))
 .catch(err => console.error(err))
 .finally(() => console.log("cleanup"));
```

### Q: What is promise chaining?
Returning a value or another promise from `.then` so the next `.then` receives it, flattening async sequences. Errors propagate to the nearest `.catch`.

```js
fetch("/user")
  .then(res => res.json())
  .then(user => fetch(`/orders/${user.id}`))
  .then(res => res.json())
  .then(orders => console.log(orders))
  .catch(err => console.error(err)); // catches any step
```

### Q: What is async/await?
Syntactic sugar over promises for writing async code that reads synchronously. `async` functions always return a promise; `await` pauses until the awaited promise settles. Use `try/catch` for error handling.

```js
async function loadOrders(id) {
  try {
    const user = await fetch(`/user/${id}`).then(r => r.json());
    const orders = await fetch(`/orders/${user.id}`).then(r => r.json());
    return orders;
  } catch (err) {
    console.error("failed", err);
  }
}
```

### Q: Difference between `Promise.all`, `Promise.race`, and `Promise.allSettled`?
- **`Promise.all`**: resolves when **all** fulfill (array of results); rejects fast if **any** rejects.
- **`Promise.race`**: settles as soon as the **first** promise settles (fulfilled or rejected).
- **`Promise.allSettled`**: waits for **all** to settle; never rejects — returns `{status, value/reason}` for each.
- (`Promise.any`: first **fulfilled**; rejects only if all reject.)

```js
const a = Promise.resolve(1);
const b = Promise.reject("err");
const c = Promise.resolve(3);

Promise.all([a, c]).then(console.log);       // [1, 3]
Promise.race([a, b]).then(console.log);      // 1 (a is faster/first)
Promise.allSettled([a, b]).then(console.log);
// [{status:"fulfilled",value:1},{status:"rejected",reason:"err"}]
```

### Q: Explain the event loop.
JavaScript is single-threaded. The **call stack** runs synchronous code. Async callbacks wait in queues. The **event loop** continuously checks: if the stack is empty, it processes all **microtasks** (promises, `queueMicrotask`), then **one macrotask** (`setTimeout`, I/O, events), repeating. This enables non-blocking concurrency.

```js
console.log("1");
setTimeout(() => console.log("2"), 0); // macrotask
Promise.resolve().then(() => console.log("3")); // microtask
console.log("4");
// Output: 1, 4, 3, 2
```

### Q: Difference between microtask and macrotask queues?
- **Microtasks**: promise callbacks, `queueMicrotask`, `MutationObserver`. Drained **completely** after each task, before rendering.
- **Macrotasks**: `setTimeout`, `setInterval`, I/O, UI events. One processed per loop iteration.
Microtasks always run before the next macrotask, even `setTimeout(0)`.

```js
setTimeout(() => console.log("macro"), 0);
Promise.resolve().then(() => console.log("micro"));
// micro then macro
```

### Q: Difference between `setTimeout` and `setInterval`?
- `setTimeout(fn, delay)`: runs `fn` **once** after the delay.
- `setInterval(fn, delay)`: runs `fn` **repeatedly** every delay until `clearInterval`.
Delays are minimums, not guarantees (depends on stack/queue). A recursive `setTimeout` is often preferred over `setInterval` for guaranteed gaps between executions.

```js
const t = setTimeout(() => console.log("once"), 1000);
clearTimeout(t);

let n = 0;
const i = setInterval(() => {
  if (++n === 3) clearInterval(i);
  console.log("tick", n);
}, 1000);
```

## ES6+ Features

### Q: What is destructuring?
Extracting values from arrays or properties from objects into distinct variables, with support for defaults, renaming, and nesting.

```js
const [first, , third = 0] = [1, 2];     // first=1, third=0
const { name, age: years = 18 } = { name: "Priya" };
// name="Priya", years=18

function print({ id, role = "user" }) {  // in params
  console.log(id, role);
}
```

### Q: Explain spread and rest operators.
Both use `...`.
- **Spread**: expands an iterable/object into individual elements (copying, merging, passing args).
- **Rest**: collects multiple elements into a single array/object (function params, destructuring).

```js
// spread
const merged = [...[1, 2], ...[3, 4]];   // [1,2,3,4]
const clone = { ...{ a: 1 }, b: 2 };     // {a:1, b:2}
Math.max(...[5, 9, 2]);                  // 9

// rest
function sum(...nums) { return nums.reduce((a, b) => a + b, 0); }
const [head, ...tail] = [1, 2, 3];       // head=1, tail=[2,3]
```

### Q: What are template literals?
Backtick strings supporting interpolation (`${}`), multi-line strings, and tagged templates.

```js
const name = "Priya";
const msg = `Hello ${name}, total = ${2 + 3}`;
const multi = `line1
line2`;
```

### Q: What are default parameters?
Function parameters with fallback values used when the argument is `undefined`.

```js
function greet(name = "Guest", greeting = "Hi") {
  return `${greeting}, ${name}`;
}
greet();          // "Hi, Guest"
greet("Priya");   // "Hi, Priya"
```

### Q: How do ES modules (import/export) work?
Modules have their own scope. Use `export` to expose and `import` to consume. **Named exports** (multiple per file) and a single **default export**. Imports are hoisted and live (read-only bindings).

```js
// math.js
export const PI = 3.14;
export function add(a, b) { return a + b; }
export default function multiply(a, b) { return a * b; }

// app.js
import multiply, { PI, add } from "./math.js";
import * as math from "./math.js";
```

### Q: Difference between `Map`/`Set` and objects/arrays?
- **`Map`**: key–value pairs with **any** key type (objects, functions), maintains insertion order, has `.size`, easily iterable. Better for frequent additions/removals.
- **`Set`**: collection of **unique** values; great for deduplication and membership checks.

```js
const m = new Map();
m.set("a", 1).set({ id: 1 }, "obj key");
m.get("a"); // 1
m.size;     // 2

const s = new Set([1, 1, 2, 3]);
[...s];        // [1, 2, 3]
s.has(2);      // true
```

### Q: What is optional chaining (`?.`)?
Safely accesses nested properties/methods that may be `null`/`undefined`, short-circuiting to `undefined` instead of throwing.

```js
const user = { profile: { address: null } };
user.profile?.address?.city;   // undefined (no error)
user.getName?.();              // undefined if method absent
user.list?.[0];               // safe index access
```

### Q: What is the nullish coalescing operator (`??`)?
Returns the right operand only when the left is `null` or `undefined` (unlike `||`, which triggers on any falsy value like `0` or `""`).

```js
const count = 0;
count || 10;  // 10  (0 is falsy)
count ?? 10;  // 0   (0 is not null/undefined)

const name = null ?? "Guest"; // "Guest"
```

### Q: What are generators?
Functions (`function*`) that can pause and resume using `yield`, producing values lazily on demand. Calling returns an iterator; `.next()` advances execution. Useful for lazy sequences, custom iterators, and (historically) async flows.

```js
function* idGenerator() {
  let id = 1;
  while (true) yield id++;
}
const gen = idGenerator();
gen.next().value; // 1
gen.next().value; // 2

function* range(start, end) {
  for (let i = start; i < end; i++) yield i;
}
[...range(1, 4)]; // [1, 2, 3]
```

## Advanced

### Q: Difference between debounce and throttle (with implementation)?
- **Debounce**: delays execution until a pause of `delay` ms after the last call. Good for search input, resize-end.
- **Throttle**: ensures execution at most once per `delay` interval. Good for scroll, mousemove.

```js
function debounce(fn, delay) {
  let timer;
  return function (...args) {
    clearTimeout(timer);
    timer = setTimeout(() => fn.apply(this, args), delay);
  };
}

function throttle(fn, limit) {
  let inThrottle = false;
  return function (...args) {
    if (!inThrottle) {
      fn.apply(this, args);
      inThrottle = true;
      setTimeout(() => (inThrottle = false), limit);
    }
  };
}
```

### Q: What is memoization? Implement it.
Caching the results of expensive function calls keyed by arguments, returning the cached result on repeat inputs.

```js
function memoize(fn) {
  const cache = new Map();
  return function (...args) {
    const key = JSON.stringify(args);
    if (cache.has(key)) return cache.get(key);
    const result = fn.apply(this, args);
    cache.set(key, result);
    return result;
  };
}

const slowSquare = n => { /* heavy */ return n * n; };
const fastSquare = memoize(slowSquare);
fastSquare(4); // computes
fastSquare(4); // cached
```

### Q: Implement a deep clone function.
Recursively copy nested structures. (`structuredClone` is the built-in modern option, but interviewers often want the manual version.)

```js
function deepClone(value) {
  if (value === null || typeof value !== "object") return value;
  if (value instanceof Date) return new Date(value);
  if (Array.isArray(value)) return value.map(deepClone);
  return Object.fromEntries(
    Object.entries(value).map(([k, v]) => [k, deepClone(v)])
  );
}

const a = { x: 1, nested: { y: 2 }, arr: [1, 2] };
const b = deepClone(a);
b.nested.y = 99;
a.nested.y; // 2 (independent)
```

### Q: Implement a generic curry function.
Collect arguments until the original function's arity is satisfied, then invoke.

```js
function curry(fn) {
  return function curried(...args) {
    if (args.length >= fn.length) return fn.apply(this, args);
    return (...next) => curried.apply(this, [...args, ...next]);
  };
}

const add = (a, b, c) => a + b + c;
const cAdd = curry(add);
cAdd(1)(2)(3);   // 6
cAdd(1, 2)(3);   // 6
cAdd(1)(2, 3);   // 6
```

### Q: What is event delegation?
Attaching a single listener to a common ancestor instead of many child listeners, relying on event bubbling. The handler inspects `event.target`. Improves performance and handles dynamically added elements.

```js
document.getElementById("list").addEventListener("click", (e) => {
  if (e.target.matches("li")) {
    console.log("clicked", e.target.textContent);
  }
});
// works even for <li> added later
```

### Q: Explain event bubbling and capturing.
DOM event propagation has three phases:
1. **Capturing** (top → target): listeners with `{capture: true}`.
2. **Target**: the event reaches the actual element.
3. **Bubbling** (target → top): default phase for most handlers.
Use `stopPropagation()` to halt and `event.target` (origin) vs `event.currentTarget` (handler's element).

```js
parent.addEventListener("click", () => console.log("parent"), true);  // capture
child.addEventListener("click", () => console.log("child"));          // bubble
// click child -> "parent" (capture) then "child" (target/bubble)
child.addEventListener("click", e => e.stopPropagation());
```

## Tricky Output-Based Questions

### Q: What is the output?
```js
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 0);
}
```
**Output:** `3 3 3`. `var` is function-scoped, so all callbacks share the same `i`, which is `3` by the time the timeouts run. Fix with `let` (block-scoped, new binding per iteration) → prints `0 1 2`.

### Q: What is the output?
```js
console.log(1);
setTimeout(() => console.log(2), 0);
Promise.resolve().then(() => console.log(3));
console.log(4);
```
**Output:** `1 4 3 2`. Synchronous logs (`1`, `4`) run first. The microtask queue (promise → `3`) drains before macrotasks (`setTimeout` → `2`).

### Q: What is the output?
```js
console.log(typeof null);
console.log(0.1 + 0.2 === 0.3);
console.log([] + []);
console.log(!!"false");
```
**Output:** `"object"`, `false`, `""`, `true`.
- `typeof null` is `"object"` (legacy bug).
- Floating-point: `0.1 + 0.2` = `0.30000000000000004`.
- `[] + []` coerces both arrays to empty strings → `""`.
- The non-empty string `"false"` is truthy → `!!` gives `true`.

### Q: What is the output?
```js
const obj = {
  name: "Priya",
  regular: function () { return this.name; },
  arrow: () => this.name,
};
console.log(obj.regular());
console.log(obj.arrow());
```
**Output:** `"Priya"`, `undefined`. `regular` is called as a method so `this` is `obj`. The `arrow` function has no own `this` — it inherits from the enclosing (module/global) scope where `this.name` is `undefined`.

### Q: What is the output?
```js
let a = { n: 1 };
let b = a;
a.n = 2;
a = { n: 3 };
console.log(b.n);
```
**Output:** `2`. `b` references the original object. `a.n = 2` mutates that shared object (so `b.n` becomes `2`). Re-assigning `a` to a new object doesn't affect `b`, which still points to the first object.
