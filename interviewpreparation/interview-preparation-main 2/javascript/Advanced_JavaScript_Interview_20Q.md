# 20 Advanced JavaScript Interview Questions & Answers
### For 5+ Years Senior Full-Stack / Frontend Developer
### Target: PwC, Deloitte, Cognizant, Capgemini, Product Companies

---

## Q1. Explain the Event Loop. How does JavaScript handle asynchronous operations despite being single-threaded?

**Summary:**
JavaScript is single-threaded — it has one call stack. But it handles async operations using the Event Loop, which coordinates between the Call Stack, Web APIs (or Node APIs), the Callback Queue (macro-tasks), and the Microtask Queue. Microtasks (Promises) always execute before macro-tasks (setTimeout).

**How It Works:**

```
┌─────────────────────────────────────────────────┐
│                  CALL STACK                      │
│  (Executes one function at a time — LIFO)       │
└────────────────────┬────────────────────────────┘
                     │
                     │ When stack is empty, Event Loop checks:
                     ▼
┌──────────────────────────────────────────────────┐
│  1. MICROTASK QUEUE (highest priority)           │
│     → Promise.then(), queueMicrotask(),          │
│       MutationObserver                           │
│     → ALL microtasks drain before moving on      │
├──────────────────────────────────────────────────┤
│  2. MACRO-TASK QUEUE (one at a time)             │
│     → setTimeout, setInterval, setImmediate,     │
│       I/O callbacks, UI rendering                │
│     → Picks ONE, then checks microtasks again    │
└──────────────────────────────────────────────────┘
```

**Predict the Output (Classic Interview Question):**
```javascript
console.log('1');

setTimeout(() => console.log('2'), 0);

Promise.resolve().then(() => console.log('3'));

Promise.resolve().then(() => {
  console.log('4');
  setTimeout(() => console.log('5'), 0);
});

console.log('6');

// Output: 1, 6, 3, 4, 2, 5
// Why:
// 1 → synchronous, runs immediately
// 6 → synchronous, runs immediately
// 3 → microtask (Promise), runs after all sync code
// 4 → microtask (Promise), runs after all sync code
// 2 → macro-task (setTimeout), runs after all microtasks
// 5 → macro-task (setTimeout queued inside microtask), runs last
```

**Real-World Impact:**
```javascript
// ❌ This blocks the UI for 5 seconds
function processLargeArray(arr) {
  arr.forEach(item => heavyComputation(item)); // blocks call stack
}

// ✅ Yield to event loop — keeps UI responsive
async function processLargeArray(arr) {
  for (let i = 0; i < arr.length; i++) {
    heavyComputation(arr[i]);
    if (i % 1000 === 0) {
      await new Promise(resolve => setTimeout(resolve, 0));
      // Yields to event loop — browser can repaint, handle clicks
    }
  }
}
```

**Common Mistakes:**
- Thinking `setTimeout(fn, 0)` runs immediately — it goes to the macro-task queue and runs after all microtasks.
- Infinite microtask loop — if a `.then()` keeps scheduling new microtasks, the macro-task queue (and UI rendering) starves.
- Assuming `async/await` makes JavaScript multi-threaded — it's still single-threaded; `await` just yields to the event loop.

**Closing:**
The Event Loop is why JavaScript can be single-threaded yet non-blocking. Understanding the microtask vs macro-task priority is essential for debugging race conditions and performance issues in production.

---

## Q2. What are Closures? How do they work in real applications?

**Summary:**
A closure is a function that remembers the variables from its outer scope even after the outer function has returned. It's not a feature you "use" — it's how JavaScript scoping works. Every callback, event handler, and factory function relies on closures.

**How It Works:**
```javascript
function createCounter(initialValue) {
  let count = initialValue; // This variable lives on after createCounter returns

  return {
    increment: () => ++count,    // closure — accesses 'count'
    decrement: () => --count,    // closure — accesses 'count'
    getCount: () => count         // closure — accesses 'count'
  };
}

const counter = createCounter(10);
console.log(counter.increment()); // 11
console.log(counter.increment()); // 12
console.log(counter.getCount());  // 12

// 'count' is NOT accessible from outside — true private state
// counter.count → undefined
```

**Real-World Uses:**

**1. Data Privacy / Encapsulation:**
```javascript
function createAuthModule() {
  let token = null; // Private — no external access

  return {
    login: async (credentials) => {
      const response = await fetch('/api/login', {
        method: 'POST',
        body: JSON.stringify(credentials)
      });
      const data = await response.json();
      token = data.token; // Only these methods can modify token
    },
    getToken: () => token,
    logout: () => { token = null; }
  };
}

const auth = createAuthModule();
// auth.token → undefined (truly private)
// auth.getToken() → only way to access
```

**2. Function Factories:**
```javascript
function createMultiplier(factor) {
  return (number) => number * factor; // 'factor' remembered via closure
}

const double = createMultiplier(2);
const triple = createMultiplier(3);

console.log(double(5));  // 10
console.log(triple(5));  // 15
```

**3. Event Handlers with State:**
```javascript
function setupClickTracker(buttonId) {
  let clickCount = 0; // Persists across clicks via closure

  document.getElementById(buttonId).addEventListener('click', () => {
    clickCount++;
    console.log(`Button clicked ${clickCount} times`);
  });
}
```

**The Classic Loop Bug:**
```javascript
// ❌ All callbacks log 3 (closure shares the same 'i')
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 100);
}
// Output: 3, 3, 3 — because 'var' is function-scoped, not block-scoped

// ✅ Fix 1: Use 'let' (block-scoped — each iteration gets its own 'i')
for (let i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 100);
}
// Output: 0, 1, 2

// ✅ Fix 2: IIFE creates a new closure per iteration
for (var i = 0; i < 3; i++) {
  ((j) => {
    setTimeout(() => console.log(j), 100);
  })(i);
}
// Output: 0, 1, 2
```

**Common Mistakes:**
- Memory leaks — closures retain references to outer variables. If a closure holds a reference to a large object, it can't be garbage collected.
- Stale closures in React — `useEffect` capturing an old state value because the closure was created before the update.
- Over-using closures for encapsulation when ES2022 `#private` fields exist.

**Closing:**
Closures are the foundation of JavaScript patterns — modules, factories, callbacks, memoization. If you understand closures, you understand JavaScript scoping. The loop bug with `var` is still the #1 closure interview question.

---

## Q3. What is the difference between var, let, and const? Explain hoisting and the Temporal Dead Zone.

**Summary:**
`var` is function-scoped and hoisted (initialized as `undefined`). `let` and `const` are block-scoped and hoisted but NOT initialized — accessing them before declaration throws a `ReferenceError` (the Temporal Dead Zone). `const` prevents reassignment but doesn't make objects immutable.

**Comparison:**

| Feature | `var` | `let` | `const` |
|---------|-------|-------|---------|
| Scope | Function | Block `{}` | Block `{}` |
| Hoisted | Yes (as `undefined`) | Yes (but TDZ) | Yes (but TDZ) |
| Re-declaration | Allowed | Error | Error |
| Reassignment | Allowed | Allowed | Error |
| On `window` (global) | Yes | No | No |

**Hoisting Behavior:**
```javascript
// 'var' — hoisted and initialized as undefined
console.log(a); // undefined (not ReferenceError!)
var a = 10;

// What JS actually does:
var a;           // hoisted to top, initialized as undefined
console.log(a);  // undefined
a = 10;

// 'let' — hoisted but NOT initialized (Temporal Dead Zone)
console.log(b); // ❌ ReferenceError: Cannot access 'b' before initialization
let b = 20;

// 'const' — same TDZ behavior as let
console.log(c); // ❌ ReferenceError
const c = 30;
```

**Temporal Dead Zone (TDZ):**
```javascript
{
  // TDZ for 'x' starts here ─────────────────┐
  console.log(typeof x); // ❌ ReferenceError   │ TDZ
  // The variable exists (hoisted) but          │
  // you can't access it until declaration      │
  let x = 42; // ◄── TDZ ends here ────────────┘
  console.log(x); // ✅ 42
}
```

**const — Not Truly Immutable:**
```javascript
const user = { name: 'John', age: 30 };
user.age = 31;      // ✅ Works — object properties CAN be modified
user = {};           // ❌ TypeError — reassignment not allowed

const arr = [1, 2, 3];
arr.push(4);         // ✅ Works — array contents CAN be modified
arr = [5, 6];        // ❌ TypeError — reassignment not allowed

// For true immutability:
const frozen = Object.freeze({ name: 'John', age: 30 });
frozen.age = 31;     // Silently fails (or TypeError in strict mode)
// Note: Object.freeze is shallow — nested objects are NOT frozen
```

**My Rule in Production:**
```
const  → default choice for everything (95% of declarations)
let    → only when reassignment is genuinely needed (loop counters, accumulators)
var    → never. Zero use cases in modern JavaScript.
```

**Closing:**
`const` by default, `let` when needed, `var` never. The TDZ exists because `let`/`const` are hoisted (the engine knows they exist) but not initialized (you can't use them yet). This catches bugs that `var` silently hides.

---

## Q4. Explain Prototypal Inheritance. How does the prototype chain work?

**Summary:**
JavaScript uses prototypal inheritance — objects inherit directly from other objects via the prototype chain. When you access a property, JS looks at the object first, then walks up the `__proto__` chain until it finds the property or reaches `null`. Every function has a `prototype` property, and every object has a `__proto__` link.

**How the Prototype Chain Works:**
```javascript
const animal = {
  eat() { return 'eating'; }
};

const dog = Object.create(animal); // dog.__proto__ = animal
dog.bark = function() { return 'woof'; };

const puppy = Object.create(dog); // puppy.__proto__ = dog

puppy.bark();  // 'woof'  — found on dog
puppy.eat();   // 'eating' — found on animal (walked up the chain)
puppy.fly();   // undefined → TypeError — not found anywhere

// Chain: puppy → dog → animal → Object.prototype → null
```

**Visualizing the Chain:**
```
puppy
  │
  └── __proto__ → dog
                    │  bark()
                    └── __proto__ → animal
                                     │  eat()
                                     └── __proto__ → Object.prototype
                                                       │  toString(), hasOwnProperty()
                                                       └── __proto__ → null (end)
```

**Constructor Functions + Prototype:**
```javascript
function Person(name) {
  this.name = name; // instance property
}

Person.prototype.greet = function() {
  return `Hi, I'm ${this.name}`; // shared across ALL Person instances
};

const john = new Person('John');
const jane = new Person('Jane');

john.greet(); // "Hi, I'm John"
jane.greet(); // "Hi, I'm Jane"

// Both share the SAME greet function (memory efficient)
john.greet === jane.greet; // true
john.__proto__ === Person.prototype; // true
```

**ES6 Classes — Syntactic Sugar Over Prototypes:**
```javascript
class Animal {
  constructor(name) {
    this.name = name;
  }
  speak() {
    return `${this.name} makes a sound`;
  }
}

class Dog extends Animal {
  bark() {
    return `${this.name} barks`;
  }
}

const rex = new Dog('Rex');
rex.bark();  // "Rex barks" — found on Dog.prototype
rex.speak(); // "Rex makes a sound" — found on Animal.prototype

// Under the hood:
rex.__proto__ === Dog.prototype;                // true
Dog.prototype.__proto__ === Animal.prototype;    // true
```

**Common Mistakes:**
- Modifying `Object.prototype` — affects every object in the application.
- Confusing `__proto__` (instance link) with `prototype` (function property). `__proto__` is what the engine follows; `prototype` is what `new` uses to set `__proto__`.
- Thinking `class` is fundamentally different from prototypes — it's syntactic sugar. Same mechanism underneath.

**Closing:**
Prototypal inheritance is JavaScript's core object model. Classes are cleaner syntax, but understanding the prototype chain is essential for debugging `this`, extending built-ins, and understanding how frameworks work internally.

---

## Q5. Explain `this` keyword in JavaScript. How does it behave in different contexts?

**Summary:**
`this` is determined by HOW a function is called, not WHERE it's defined. It has different values in global context, object methods, constructors, arrow functions, and event handlers. Arrow functions inherit `this` from their enclosing scope — they don't have their own `this`.

**The Rules (in priority order):**

```javascript
// Rule 1: 'new' binding — this = newly created object
function Person(name) { this.name = name; }
const p = new Person('John'); // this = {} (new object)

// Rule 2: Explicit binding — call/apply/bind
function greet() { return `Hi, ${this.name}`; }
greet.call({ name: 'John' });  // this = { name: 'John' }
greet.apply({ name: 'Jane' }); // this = { name: 'Jane' }
const bound = greet.bind({ name: 'Bob' });
bound(); // this = { name: 'Bob' }

// Rule 3: Implicit binding — object method
const user = {
  name: 'John',
  greet() { return `Hi, ${this.name}`; }
};
user.greet(); // this = user → "Hi, John"

// Rule 4: Default binding — standalone function
function standalone() { return this; }
standalone(); // this = window (browser) or global (Node)
              // this = undefined (strict mode)
```

**Arrow Functions — Lexical `this`:**
```javascript
const team = {
  name: 'Engineering',
  members: ['Alice', 'Bob', 'Charlie'],

  // ❌ Regular function — 'this' is NOT the team object inside callback
  showMembers() {
    this.members.forEach(function(member) {
      console.log(`${member} belongs to ${this.name}`);
      // this = window/undefined — NOT team!
    });
  },

  // ✅ Arrow function — inherits 'this' from showMembers (which is team)
  showMembersFixed() {
    this.members.forEach((member) => {
      console.log(`${member} belongs to ${this.name}`);
      // this = team ✅ — arrow function captures enclosing 'this'
    });
  }
};
```

**The Most Common Interview Trap:**
```javascript
const obj = {
  name: 'Object',
  getName: function() { return this.name; },
  getNameArrow: () => this.name  // ❌ arrow at object level — 'this' is global!
};

obj.getName();      // 'Object' ✅
obj.getNameArrow(); // undefined ❌ — arrow function's 'this' is the enclosing scope (global)

// Extracting method loses 'this'
const fn = obj.getName;
fn(); // undefined (or error in strict mode) — 'this' is now global

// Fix: bind it
const boundFn = obj.getName.bind(obj);
boundFn(); // 'Object' ✅
```

**Cheat Sheet:**

| Call Style | `this` Value |
|-----------|-------------|
| `func()` | `window` / `undefined` (strict) |
| `obj.func()` | `obj` |
| `func.call(ctx)` / `apply` | `ctx` |
| `func.bind(ctx)()` | `ctx` |
| `new Func()` | New empty object |
| `() => {}` | Enclosing scope's `this` (lexical) |
| Event handler | The DOM element |
| Class method | The instance |

**Closing:**
`this` is about call-site, not definition-site — except for arrow functions. When in doubt: arrow functions for callbacks, `bind` for extracted methods. This is the #1 most misunderstood JavaScript concept.

---

## Q6. What are Promises? Explain Promise chaining, Promise.all, Promise.race, Promise.allSettled, and Promise.any.

**Summary:**
A Promise represents a future value — it's either pending, fulfilled, or rejected. I use `.then()` for chaining, `Promise.all` when all must succeed, `Promise.allSettled` when I need all results regardless of failure, `Promise.race` for timeout patterns, and `Promise.any` for the first success.

**Promise States:**
```
                  ┌─── fulfilled (resolved with value)
Pending ──────────┤
                  └─── rejected (rejected with error)

Once settled (fulfilled or rejected), a Promise NEVER changes state.
```

**Promise Chaining:**
```javascript
fetchUser(userId)
  .then(user => fetchOrders(user.id))       // returns a new Promise
  .then(orders => calculateTotal(orders))    // returns a new Promise
  .then(total => console.log(`Total: $${total}`))
  .catch(error => console.error('Failed:', error)) // catches ANY error above
  .finally(() => hideLoadingSpinner());             // runs regardless
```

**Promise Static Methods:**

```javascript
const userApi = fetch('/api/users').then(r => r.json());
const orderApi = fetch('/api/orders').then(r => r.json());
const statsApi = fetch('/api/stats').then(r => r.json());

// Promise.all — ALL must succeed (fails fast on first rejection)
const [users, orders, stats] = await Promise.all([userApi, orderApi, statsApi]);
// If ANY fails → catch block, other results lost
// Use case: Dashboard that needs ALL data to render

// Promise.allSettled — get ALL results (success or failure)
const results = await Promise.allSettled([userApi, orderApi, statsApi]);
// results = [
//   { status: 'fulfilled', value: [...users] },
//   { status: 'rejected',  reason: Error('500') },
//   { status: 'fulfilled', value: [...stats] }
// ]
// Use case: Dashboard that renders available data, shows error for failed parts

// Promise.race — first to settle wins (fulfilled OR rejected)
const result = await Promise.race([
  fetch('/api/data'),
  new Promise((_, reject) => setTimeout(() => reject('Timeout'), 5000))
]);
// Use case: Timeout pattern — cancel if API takes > 5s

// Promise.any — first to SUCCEED wins (ignores rejections)
const fastest = await Promise.any([
  fetch('https://cdn1.example.com/data'),
  fetch('https://cdn2.example.com/data'),
  fetch('https://cdn3.example.com/data')
]);
// Returns the FIRST successful response — ignores failed CDNs
// Throws AggregateError only if ALL fail
// Use case: Fastest CDN/mirror selection
```

**Comparison:**

| Method | Resolves When | Rejects When | Use Case |
|--------|-------------|-------------|----------|
| `Promise.all` | ALL fulfilled | ANY rejects | Parallel fetch, need all |
| `Promise.allSettled` | ALL settled | Never rejects | Partial success OK |
| `Promise.race` | FIRST settled | FIRST settled (if reject) | Timeout |
| `Promise.any` | FIRST fulfilled | ALL reject | Fastest source |

**Closing:**
`Promise.all` for "need everything," `Promise.allSettled` for "get what you can," `Promise.race` for timeouts, `Promise.any` for redundancy. These are essential for optimizing concurrent API calls in production.

---

## Q7. What is the difference between == and ===? Explain type coercion.

**Summary:**
`===` is strict equality — compares value AND type, no conversion. `==` is loose equality — performs type coercion before comparing. I use `===` exclusively in production. The coercion rules of `==` are unintuitive and a bug magnet.

**Type Coercion Rules with ==:**
```javascript
// String vs Number → String converted to Number
'5' == 5       // true  ('5' → 5)
'' == 0        // true  ('' → 0)

// Boolean vs anything → Boolean converted to Number first
true == 1      // true  (true → 1)
false == 0     // true  (false → 0)
true == '1'    // true  (true → 1, '1' → 1)

// null and undefined
null == undefined  // true  (special rule)
null == 0          // false (null only equals undefined and null)
null == ''         // false

// Object vs Primitive → Object calls valueOf()/toString()
[1] == 1       // true  ([1].toString() → '1' → 1)
[] == false     // true  ([].toString() → '' → 0, false → 0)
[] == ![]       // true  ([] → 0, ![] → false → 0)  🤯
```

**The Insanity of ==:**
```javascript
'' == '0'       // false
0 == ''         // true
0 == '0'        // true
// Transitivity broken: if 0=='', and 0=='0', then ''=='0'? NO!

false == 'false'   // false
false == '0'       // true
false == undefined // false
false == null      // false
```

**=== (Strict) — No Surprises:**
```javascript
'5' === 5       // false (different types)
true === 1      // false (different types)
null === undefined // false (different types)
[] === false    // false
NaN === NaN     // false (special case — use Number.isNaN())
```

**My Rule:**
```javascript
// ALWAYS use ===
if (value === null || value === undefined) { }

// Or use nullish check:
if (value == null) { } // This is the ONLY acceptable use of ==
// because null == undefined is true, and null == 0 is false
// It's a clean "is null or undefined" check
```

**Closing:**
`===` always. The only time I use `==` is `value == null` to check both null and undefined in one expression. Everything else is strict equality. This prevents an entire category of subtle bugs.

---

## Q8. What are Generators and Iterators? How are they used in real-world JavaScript?

**Summary:**
Generators are functions that can pause execution and resume later using `yield`. They return an Iterator — an object with a `next()` method. They're the foundation of `async/await` and are used for lazy evaluation, custom iteration, and controlling async flows.

**How Generators Work:**
```javascript
function* numberGenerator() {
  console.log('Start');
  yield 1;                // Pauses here, returns { value: 1, done: false }
  console.log('After 1');
  yield 2;                // Pauses here, returns { value: 2, done: false }
  console.log('After 2');
  yield 3;                // Pauses here, returns { value: 3, done: false }
  console.log('End');
  return 'finished';      // { value: 'finished', done: true }
}

const gen = numberGenerator();
gen.next(); // 'Start'    → { value: 1, done: false }
gen.next(); // 'After 1'  → { value: 2, done: false }
gen.next(); // 'After 2'  → { value: 3, done: false }
gen.next(); // 'End'      → { value: 'finished', done: true }
gen.next(); //            → { value: undefined, done: true }
```

**Real-World Use 1 — Lazy Infinite Sequences:**
```javascript
function* idGenerator(prefix = 'ID') {
  let id = 1;
  while (true) {
    yield `${prefix}-${id++}`;
  }
}

const userIds = idGenerator('USR');
userIds.next().value; // 'USR-1'
userIds.next().value; // 'USR-2'
// Generates on demand — never creates an array of 1 million IDs in memory

// Pagination
function* paginate(items, pageSize) {
  for (let i = 0; i < items.length; i += pageSize) {
    yield items.slice(i, i + pageSize);
  }
}

const pages = paginate(allProducts, 20);
pages.next().value; // first 20 products
pages.next().value; // next 20 products
```

**Real-World Use 2 — Async Generators (Streaming Data):**
```javascript
async function* fetchPages(baseUrl) {
  let page = 1;
  while (true) {
    const response = await fetch(`${baseUrl}?page=${page}`);
    const data = await response.json();
    if (data.items.length === 0) return; // no more pages
    yield data.items;
    page++;
  }
}

// Consume lazily
for await (const items of fetchPages('/api/products')) {
  renderProducts(items);
  // Fetches next page only when current iteration completes
}
```

**The Iterator Protocol:**
```javascript
// Any object with Symbol.iterator is iterable (works with for...of)
const range = {
  from: 1,
  to: 5,
  [Symbol.iterator]() {
    let current = this.from;
    const last = this.to;
    return {
      next() {
        return current <= last
          ? { value: current++, done: false }
          : { done: true };
      }
    };
  }
};

for (const num of range) console.log(num); // 1, 2, 3, 4, 5
[...range]; // [1, 2, 3, 4, 5] — spread works too
```

**Closing:**
Generators power lazy evaluation, custom iterables, and async streaming. They're the mechanism behind `async/await` (transpiled code uses generators). In production, I use async generators for paginated API consumption and infinite scroll.

---

## Q9. What is the difference between shallow copy and deep copy? How do you deep clone an object?

**Summary:**
Shallow copy copies the top-level properties — nested objects are still shared references. Deep copy recursively copies everything — no shared references. I use `structuredClone()` (modern, built-in) for deep copy and spread operator for shallow copy.

**The Problem:**
```javascript
const original = {
  name: 'John',
  address: { city: 'Mumbai', zip: '400001' },
  hobbies: ['reading', 'coding']
};

// SHALLOW COPY — nested objects are shared
const shallow = { ...original };
shallow.name = 'Jane';          // ✅ doesn't affect original
shallow.address.city = 'Delhi'; // ❌ CHANGES original.address.city too!
console.log(original.address.city); // 'Delhi' — corrupted!

// DEEP COPY — everything is independent
const deep = structuredClone(original);
deep.address.city = 'Delhi';    // ✅ doesn't affect original
console.log(original.address.city); // 'Mumbai' — safe!
```

**Shallow Copy Methods:**
```javascript
// All of these are SHALLOW
const copy1 = { ...original };
const copy2 = Object.assign({}, original);
const copy3 = Array.from(originalArray);
const copy4 = [...originalArray];
```

**Deep Copy Methods:**
```javascript
// 1. structuredClone() — Modern, recommended (ES2022+)
const deep1 = structuredClone(original);
// ✅ Handles: nested objects, arrays, Date, Map, Set, RegExp, ArrayBuffer
// ❌ Cannot handle: functions, DOM nodes, symbols, prototype chain

// 2. JSON.parse(JSON.stringify()) — Old workaround
const deep2 = JSON.parse(JSON.stringify(original));
// ❌ Loses: Date (→ string), undefined, functions, Map, Set, RegExp
// ❌ Fails: circular references
// ❌ Converts: Infinity/NaN → null

// 3. lodash cloneDeep — handles everything
import { cloneDeep } from 'lodash-es';
const deep3 = cloneDeep(original);
```

**Comparison:**

| Method | Deep? | Functions | Date | Circular | Performance |
|--------|:-----:|:---------:|:----:|:--------:|:-----------:|
| Spread `{...}` | No | Copies ref | Copies ref | N/A | Fastest |
| `JSON.parse(stringify)` | Yes | Lost | Becomes string | Errors | Slow |
| `structuredClone()` | Yes | Error | Preserved | Handles | Fast |
| `lodash.cloneDeep` | Yes | Copies | Preserved | Handles | Medium |

**Closing:**
`structuredClone()` is the standard answer for deep copy in 2024+. Spread/`Object.assign` for shallow copy of flat objects. JSON trick is outdated — it silently corrupts Dates and drops functions. Always know which level of copying your code needs.

---

## Q10. Explain async/await. How does error handling work? What are common pitfalls?

**Summary:**
`async/await` is syntactic sugar over Promises — it makes async code read like synchronous code. An `async` function always returns a Promise. `await` pauses execution until the Promise settles. Error handling uses `try/catch` blocks.

**How It Works:**
```javascript
// Promise chain
function getUserOrders(userId) {
  return fetchUser(userId)
    .then(user => fetchOrders(user.id))
    .then(orders => orders.filter(o => o.status === 'active'))
    .catch(error => console.error(error));
}

// async/await — same logic, much cleaner
async function getUserOrders(userId) {
  try {
    const user = await fetchUser(userId);
    const orders = await fetchOrders(user.id);
    return orders.filter(o => o.status === 'active');
  } catch (error) {
    console.error(error);
    return [];
  }
}
```

**Pitfall 1 — Sequential When You Need Parallel:**
```javascript
// ❌ SLOW — waits for each to finish before starting next (6 seconds total)
async function loadDashboard() {
  const users = await fetchUsers();    // 2 seconds
  const orders = await fetchOrders();  // 2 seconds
  const stats = await fetchStats();    // 2 seconds
  return { users, orders, stats };
}

// ✅ FAST — all three run in parallel (2 seconds total)
async function loadDashboard() {
  const [users, orders, stats] = await Promise.all([
    fetchUsers(),
    fetchOrders(),
    fetchStats()
  ]);
  return { users, orders, stats };
}
```

**Pitfall 2 — await in Loops:**
```javascript
// ❌ SLOW — processes one at a time
async function processOrders(orderIds) {
  for (const id of orderIds) {
    await processOrder(id); // waits for each before starting next
  }
}

// ✅ FAST — all in parallel (with concurrency limit)
async function processOrders(orderIds) {
  await Promise.all(orderIds.map(id => processOrder(id)));
}

// ✅ CONTROLLED — parallel with concurrency limit
async function processWithLimit(items, limit, fn) {
  const results = [];
  for (let i = 0; i < items.length; i += limit) {
    const batch = items.slice(i, i + limit);
    const batchResults = await Promise.all(batch.map(fn));
    results.push(...batchResults);
  }
  return results;
}
```

**Pitfall 3 — Unhandled Rejections:**
```javascript
// ❌ Unhandled — if fetchUser rejects, it becomes an unhandled rejection
async function getUser() {
  const user = await fetchUser(123); // if this rejects, no catch
  return user;
}
getUser(); // calling without .catch() or try/catch

// ✅ Always handle errors
getUser().catch(err => console.error(err));
// OR
try { const user = await getUser(); } catch(e) { console.error(e); }
```

**Pitfall 4 — forEach doesn't await:**
```javascript
// ❌ forEach doesn't respect await — fires all at once, doesn't wait
[1, 2, 3].forEach(async (id) => {
  await processItem(id); // these run concurrently, not sequentially
});
console.log('Done'); // Runs BEFORE processing finishes!

// ✅ Use for...of for sequential
for (const id of [1, 2, 3]) {
  await processItem(id);
}
console.log('Done'); // Runs AFTER all processing
```

**Closing:**
`async/await` makes async code readable, but know the pitfalls: don't sequentially `await` independent operations, don't use `await` inside `forEach`, and always handle rejections. These are the most common async bugs in production code.

---

## Q11. What is Event Delegation? Why is it important for performance?

**Summary:**
Event delegation is attaching a single event listener to a parent element instead of individual listeners on each child. It works because of event bubbling — events propagate from the target up to the root. It's essential for performance with large lists and dynamically added elements.

**The Problem Without Delegation:**
```javascript
// ❌ 10,000 listeners — one per row
document.querySelectorAll('.table-row').forEach(row => {
  row.addEventListener('click', handleRowClick);
});
// Problem 1: 10,000 listeners consume memory
// Problem 2: Dynamically added rows DON'T have listeners
```

**With Event Delegation:**
```javascript
// ✅ 1 listener on the parent — handles all children
document.querySelector('.table-body').addEventListener('click', (event) => {
  const row = event.target.closest('.table-row');
  if (!row) return; // click wasn't on a row

  const rowId = row.dataset.id;
  handleRowClick(rowId);
});
// ✅ 1 listener instead of 10,000
// ✅ Dynamically added rows automatically work
// ✅ Less memory, better performance
```

**How Event Bubbling Enables This:**
```
Click on <span> inside a <td> inside a <tr>:

Capturing Phase (top-down):  document → table → tbody → tr → td → span
Target Phase:                span (event.target)
Bubbling Phase (bottom-up):  span → td → tr → tbody → table → document
                                              ^^^^^^
                                    Our listener here catches it
```

**Real-World Example — Dynamic Todo List:**
```javascript
// One listener handles add, delete, toggle for all items
document.querySelector('#todo-list').addEventListener('click', (event) => {
  const target = event.target;

  if (target.matches('.delete-btn')) {
    const item = target.closest('.todo-item');
    item.remove();
  }

  if (target.matches('.toggle-checkbox')) {
    const id = target.closest('.todo-item').dataset.id;
    toggleTodo(id);
  }
});

// Add new items dynamically — they automatically respond to clicks
function addTodo(text) {
  const html = `<li class="todo-item" data-id="${Date.now()}">
    <input type="checkbox" class="toggle-checkbox" />
    <span>${text}</span>
    <button class="delete-btn">×</button>
  </li>`;
  document.querySelector('#todo-list').insertAdjacentHTML('beforeend', html);
}
```

**Closing:**
Event delegation is a must for any list-based UI. One listener instead of thousands, dynamic elements handled automatically. Angular and React use delegation internally — `addEventListener` on the root, not on individual elements.

---

## Q12. What is Debouncing and Throttling? When do you use each?

**Summary:**
Debouncing delays execution until the user stops triggering the event. Throttling limits execution to at most once per time interval. Debounce for search inputs (wait until user stops typing), throttle for scroll/resize (run at most every 200ms).

**Debounce — "Wait until they stop":**
```javascript
function debounce(fn, delay) {
  let timeoutId;
  return function(...args) {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => fn.apply(this, args), delay);
  };
}

// User types: H-e-l-l-o (each keystroke)
// Without debounce: 5 API calls
// With debounce(300ms): 1 API call (300ms after last keystroke)

const searchInput = document.querySelector('#search');
const debouncedSearch = debounce(async (query) => {
  const results = await fetch(`/api/search?q=${query}`).then(r => r.json());
  renderResults(results);
}, 300);

searchInput.addEventListener('input', (e) => debouncedSearch(e.target.value));
```

**Throttle — "At most once per interval":**
```javascript
function throttle(fn, limit) {
  let inThrottle = false;
  return function(...args) {
    if (!inThrottle) {
      fn.apply(this, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
}

// User scrolls continuously
// Without throttle: 100+ scroll events per second
// With throttle(200ms): At most 5 executions per second

window.addEventListener('scroll', throttle(() => {
  updateScrollProgress();
  checkInfiniteScroll();
}, 200));
```

**Timeline Comparison:**
```
Events:     ──X─X─X─X─X─X────────X─X─X──────
Time:       0  1  2  3  4  5      8  9  10

Debounce (3s):
                              ──X          ──X
                              (fires 3s     (fires 3s
                               after last)   after last)

Throttle (3s):
            ──X─────────X──────────X──────────
              (first)   (every 3s)  (every 3s)
```

| Pattern | Fires When | Use Case |
|---------|-----------|----------|
| Debounce | After silence period | Search input, form validation, resize end |
| Throttle | At regular intervals | Scroll position, mouse move, window resize |

**Closing:**
Debounce for "react when they stop" (search), throttle for "react at intervals" (scroll). Both prevent performance disasters from rapid-fire events. I implement these in every project with user input.

---

## Q13. What are WeakMap and WeakSet? How do they differ from Map and Set?

**Summary:**
`WeakMap` and `WeakSet` hold "weak" references to objects — they don't prevent garbage collection. If no other reference to the key/value exists, it's automatically garbage collected. They're used for metadata storage, caching, and private data associated with objects without causing memory leaks.

**Key Differences:**

| Feature | Map / Set | WeakMap / WeakSet |
|---------|-----------|-------------------|
| Keys | Any type | Objects only (no primitives) |
| GC | Prevents GC of keys | Allows GC of keys |
| Iterable | Yes (`for...of`, `.size`) | No (can't iterate, no `.size`) |
| Use case | General collections | Metadata, caching, private data |

**Why It Matters — Memory Leaks:**
```javascript
// ❌ Map — retains reference, prevents garbage collection
const cache = new Map();

function processUser(user) {
  cache.set(user, expensiveComputation(user));
  // Even if 'user' is no longer referenced anywhere else,
  // the Map keeps it alive → memory leak!
}

// ✅ WeakMap — allows garbage collection
const cache = new WeakMap();

function processUser(user) {
  if (cache.has(user)) return cache.get(user);

  const result = expensiveComputation(user);
  cache.set(user, result);
  return result;
  // When 'user' is no longer referenced elsewhere,
  // both the key and value are garbage collected automatically
}
```

**Real-World Use 1 — Private Data:**
```javascript
const privateData = new WeakMap();

class Person {
  constructor(name, ssn) {
    this.name = name;
    privateData.set(this, { ssn });  // truly private, not on the object
  }

  getSSN(authToken) {
    if (!isAuthorized(authToken)) throw new Error('Unauthorized');
    return privateData.get(this).ssn;
  }
}

const john = new Person('John', '123-45-6789');
john.ssn;                  // undefined — not a property
privateData.get(john).ssn; // accessible only through the WeakMap
// When 'john' is garbage collected, the private data is too
```

**Real-World Use 2 — DOM Element Metadata:**
```javascript
const elementData = new WeakMap();

function trackElement(element, metadata) {
  elementData.set(element, metadata);
}

// When the element is removed from DOM and no longer referenced,
// the WeakMap entry is automatically cleaned up
// No manual cleanup needed — no memory leak!
```

**Closing:**
`WeakMap` is for associating data with objects without preventing their garbage collection. I use it for caching, private data, and DOM metadata. If you need to iterate or check size, use regular `Map`. If you need automatic memory management, use `WeakMap`.

---

## Q14. Explain the Module System in JavaScript — CommonJS vs ES Modules.

**Summary:**
CommonJS (`require`/`module.exports`) is Node.js's original module system — synchronous, dynamic, no tree-shaking. ES Modules (`import`/`export`) is the standard — static, asynchronous, tree-shakable. Modern projects use ES Modules everywhere. CommonJS is legacy but still common in Node.js libraries.

**Comparison:**

```javascript
// CommonJS (CJS) — Node.js default
const express = require('express');          // dynamic, synchronous
const { readFile } = require('fs');
module.exports = { myFunction };
module.exports = MyClass;

// ES Modules (ESM) — modern standard
import express from 'express';              // static, analyzed at compile time
import { readFile } from 'fs';
export function myFunction() {}
export default class MyClass {}
```

| Feature | CommonJS | ES Modules |
|---------|----------|------------|
| Syntax | `require()` / `module.exports` | `import` / `export` |
| Loading | Synchronous | Asynchronous |
| Timing | Runtime (dynamic) | Compile time (static) |
| Tree-shaking | Not possible | Built-in support |
| Conditional import | `if (cond) require('x')` | Top-level only (dynamic `import()` for conditional) |
| `this` at top level | `module.exports` | `undefined` |
| File extension | `.js` (default) | `.mjs` or `"type": "module"` in package.json |
| Browser support | No (needs bundler) | Yes (native `<script type="module">`) |

**Why ES Modules Win:**
```javascript
// Tree-shaking — bundler removes unused exports
import { debounce } from 'lodash-es';
// Only 'debounce' code is bundled — not all of lodash

// With CommonJS:
const { debounce } = require('lodash');
// Entire lodash library is bundled — no tree-shaking
```

**Dynamic Import (Code Splitting):**
```javascript
// Lazy load a heavy module only when needed
async function showChart() {
  const { Chart } = await import('chart.js'); // loaded on demand
  const chart = new Chart(canvas, config);
}

// Angular/React use this for lazy routing
loadComponent: () => import('./heavy-feature.component')
```

**Closing:**
ES Modules are the standard — static analysis enables tree-shaking and better tooling. Use `import/export` for everything. CommonJS is legacy — you'll encounter it in older Node.js code and libraries.

---

## Q15. What are Proxy and Reflect? How are they used?

**Summary:**
`Proxy` creates a wrapper around an object that intercepts fundamental operations — get, set, delete, function calls. `Reflect` provides the default implementations of those operations. Together, they enable reactive systems, validation, logging, and Vue.js-style reactivity.

**How Proxy Works:**
```javascript
const user = { name: 'John', age: 30 };

const proxy = new Proxy(user, {
  get(target, property) {
    console.log(`Accessing ${property}`);
    return Reflect.get(target, property);
  },

  set(target, property, value) {
    console.log(`Setting ${property} = ${value}`);
    if (property === 'age' && (typeof value !== 'number' || value < 0)) {
      throw new TypeError('Age must be a positive number');
    }
    return Reflect.set(target, property, value);
  },

  deleteProperty(target, property) {
    if (property === 'name') {
      throw new Error('Cannot delete name');
    }
    return Reflect.deleteProperty(target, property);
  }
});

proxy.name;        // logs "Accessing name" → 'John'
proxy.age = 31;    // logs "Setting age = 31" → works
proxy.age = -5;    // ❌ TypeError: Age must be a positive number
delete proxy.name; // ❌ Error: Cannot delete name
```

**Real-World Use 1 — Reactive State (How Vue.js Works):**
```javascript
function reactive(obj, onChange) {
  return new Proxy(obj, {
    set(target, key, value) {
      const oldValue = target[key];
      const result = Reflect.set(target, key, value);
      if (oldValue !== value) {
        onChange(key, value, oldValue); // Notify UI to re-render
      }
      return result;
    }
  });
}

const state = reactive({ count: 0, name: 'App' }, (key, newVal, oldVal) => {
  console.log(`${key} changed: ${oldVal} → ${newVal}`);
  renderUI(); // trigger re-render
});

state.count = 1; // "count changed: 0 → 1" → UI updates
state.name = 'Dashboard'; // "name changed: App → Dashboard" → UI updates
```

**Real-World Use 2 — API Validation Layer:**
```javascript
function createValidatedModel(schema) {
  return new Proxy({}, {
    set(target, prop, value) {
      if (schema[prop]) {
        const { type, required, max } = schema[prop];
        if (required && (value === null || value === undefined)) {
          throw new Error(`${prop} is required`);
        }
        if (type && typeof value !== type) {
          throw new TypeError(`${prop} must be ${type}, got ${typeof value}`);
        }
        if (max && value > max) {
          throw new RangeError(`${prop} cannot exceed ${max}`);
        }
      }
      return Reflect.set(target, prop, value);
    }
  });
}

const user = createValidatedModel({
  name: { type: 'string', required: true },
  age: { type: 'number', max: 150 }
});

user.name = 'John'; // ✅
user.age = 200;     // ❌ RangeError: age cannot exceed 150
user.name = 42;     // ❌ TypeError: name must be string
```

**Closing:**
`Proxy` is the mechanism behind Vue 3's reactivity, form validation libraries, and ORM frameworks. `Reflect` ensures you call the default behavior correctly. They're powerful metaprogramming tools — use them for cross-cutting concerns like validation, logging, and reactivity.

---

## Q16. What is the difference between call, apply, and bind?

**Summary:**
All three set the `this` value for a function. `call` invokes immediately with individual arguments. `apply` invokes immediately with an array of arguments. `bind` returns a new function with `this` permanently bound — doesn't invoke immediately.

**Comparison:**
```javascript
function greet(greeting, punctuation) {
  return `${greeting}, ${this.name}${punctuation}`;
}

const user = { name: 'John' };

// call — invoke with individual args
greet.call(user, 'Hello', '!');     // "Hello, John!"

// apply — invoke with array of args
greet.apply(user, ['Hello', '!']);   // "Hello, John!"

// bind — returns NEW function, doesn't invoke
const boundGreet = greet.bind(user);
boundGreet('Hello', '!');            // "Hello, John!"
// boundGreet can be called later, passed as callback, etc.
```

| Method | Invokes? | Args Format | Returns |
|--------|:--------:|-------------|---------|
| `call` | Yes | `fn.call(ctx, a, b, c)` | Function result |
| `apply` | Yes | `fn.apply(ctx, [a, b, c])` | Function result |
| `bind` | No | `fn.bind(ctx, a, b)` | New bound function |

**Real-World Uses:**

```javascript
// 1. Borrowing methods
const numbers = { 0: 'a', 1: 'b', 2: 'c', length: 3 };
const arr = Array.prototype.slice.call(numbers); // ['a', 'b', 'c']
// Modern: Array.from(numbers)

// 2. bind in event handlers
class Button {
  constructor(label) {
    this.label = label;
    // Without bind: 'this' in handleClick would be the DOM element
    this.handleClick = this.handleClick.bind(this);
  }
  handleClick() {
    console.log(`${this.label} clicked`);
  }
}

// 3. Partial application with bind
function multiply(a, b) { return a * b; }
const double = multiply.bind(null, 2); // pre-fill 'a' as 2
double(5); // 10
double(8); // 16

// 4. apply with Math.max (spread is modern alternative)
const nums = [3, 1, 4, 1, 5, 9];
Math.max.apply(null, nums); // 9
Math.max(...nums);          // 9 (modern)
```

**Closing:**
`call` and `apply` for immediate invocation with explicit `this`. `bind` for creating reusable functions with fixed `this` — most common in class methods passed as callbacks. In modern code, arrow functions often replace `bind`.

---

## Q17. What are JavaScript Design Patterns you use in production?

**Summary:**
The patterns I use most in production: Module/Revealing Module for encapsulation, Singleton for shared services, Observer for event systems, Strategy for dynamic behavior, and Factory for object creation. These aren't academic — they're the backbone of every well-structured JavaScript application.

**1. Singleton — Single Shared Instance:**
```javascript
class Database {
  static #instance = null;

  constructor() {
    if (Database.#instance) {
      return Database.#instance;
    }
    this.connection = this.connect();
    Database.#instance = this;
  }

  connect() {
    console.log('Connected to database');
    return { /* connection object */ };
  }
}

const db1 = new Database(); // 'Connected to database'
const db2 = new Database(); // no connection — returns existing instance
db1 === db2; // true

// Modern approach — just use a module (modules are singletons by default)
// db.js
let connection = null;
export function getConnection() {
  if (!connection) connection = createConnection();
  return connection;
}
```

**2. Observer — Event-Driven Communication:**
```javascript
class EventEmitter {
  #listeners = new Map();

  on(event, callback) {
    if (!this.#listeners.has(event)) {
      this.#listeners.set(event, []);
    }
    this.#listeners.get(event).push(callback);
    return () => this.off(event, callback); // return unsubscribe function
  }

  off(event, callback) {
    const cbs = this.#listeners.get(event);
    if (cbs) this.#listeners.set(event, cbs.filter(cb => cb !== callback));
  }

  emit(event, ...args) {
    (this.#listeners.get(event) || []).forEach(cb => cb(...args));
  }
}

// Usage
const bus = new EventEmitter();
const unsub = bus.on('user:login', (user) => {
  console.log(`Welcome ${user.name}`);
});
bus.emit('user:login', { name: 'John' }); // "Welcome John"
unsub(); // cleanup
```

**3. Strategy — Swappable Algorithms:**
```javascript
const sortStrategies = {
  price_asc:  (a, b) => a.price - b.price,
  price_desc: (a, b) => b.price - a.price,
  name:       (a, b) => a.name.localeCompare(b.name),
  rating:     (a, b) => b.rating - a.rating,
};

function sortProducts(products, strategy) {
  return [...products].sort(sortStrategies[strategy]);
}

sortProducts(products, 'price_asc');
sortProducts(products, 'rating');
// Adding a new sort? Just add to the object — no if/else modification
```

**4. Factory — Flexible Object Creation:**
```javascript
class NotificationFactory {
  static create(type, message) {
    switch (type) {
      case 'email': return new EmailNotification(message);
      case 'sms':   return new SmsNotification(message);
      case 'push':  return new PushNotification(message);
      default: throw new Error(`Unknown type: ${type}`);
    }
  }
}

const notification = NotificationFactory.create('email', 'Hello');
notification.send();
```

**5. Pub/Sub — Decoupled Communication:**
```javascript
// Used in Angular services, React context, Redux, and event-driven architectures
// Same as Observer but with a central mediator (topic-based)

const pubsub = {
  topics: {},
  subscribe(topic, fn) {
    this.topics[topic] = this.topics[topic] || [];
    this.topics[topic].push(fn);
  },
  publish(topic, data) {
    (this.topics[topic] || []).forEach(fn => fn(data));
  }
};
```

**Closing:**
These patterns appear in every framework: Angular's DI is Factory + Singleton, RxJS Subject is Observer, strategy pattern replaces if/else chains, modules are revealing module pattern. Knowing when to apply each is what separates a senior developer from someone who just writes code.

---

## Q18. What are ES2020–ES2024 features every senior developer should know?

**Summary:**
Modern JavaScript has added powerful features since ES2020 that I use daily: optional chaining, nullish coalescing, private class fields, `structuredClone`, `at()`, top-level `await`, `Object.groupBy`, and more. These aren't "nice to have" — they eliminate entire categories of boilerplate and bugs.

**ES2020:**
```javascript
// Optional Chaining (?.)
const city = user?.address?.city;             // undefined if any is null/undefined
const firstItem = arr?.[0];                   // safe array access
const result = obj?.method?.();               // safe method call

// Nullish Coalescing (??)
const port = config.port ?? 3000;
// Only uses 3000 if port is null/undefined (NOT for 0 or '')
// Different from ||: config.port || 3000 would replace 0 with 3000!

// Promise.allSettled
const results = await Promise.allSettled([api1(), api2(), api3()]);

// BigInt — for numbers larger than Number.MAX_SAFE_INTEGER
const huge = 9007199254740991n + 1n; // works correctly
```

**ES2021:**
```javascript
// Logical Assignment
user.name ??= 'Anonymous';    // assign if null/undefined
user.role ||= 'user';         // assign if falsy
user.flags &&= processFlags(); // assign if truthy

// String.replaceAll
const clean = 'a.b.c'.replaceAll('.', '/'); // 'a/b/c'

// Numeric Separators
const billion = 1_000_000_000;
const bytes = 0xFF_FF_FF;
```

**ES2022:**
```javascript
// Private class fields and methods
class User {
  #password;         // truly private field
  #email;

  constructor(email, password) {
    this.#email = email;
    this.#password = this.#hash(password);
  }

  #hash(value) {     // private method
    return crypto.subtle.digest('SHA-256', value);
  }

  authenticate(password) {
    return this.#hash(password) === this.#password;
  }
}

const u = new User('john@example.com', 'secret');
u.#password; // ❌ SyntaxError — truly private

// Object.hasOwn — replaces obj.hasOwnProperty()
Object.hasOwn(user, 'name'); // true

// Array.at() — negative indexing
const arr = [1, 2, 3, 4, 5];
arr.at(-1);  // 5 (last element)
arr.at(-2);  // 4 (second to last)

// Top-level await (in modules)
const config = await fetch('/config.json').then(r => r.json());
export default config;

// structuredClone — native deep clone
const deep = structuredClone(original);

// Error cause
throw new Error('Failed to load user', { cause: originalError });
```

**ES2023:**
```javascript
// Array find from last
const arr = [1, 2, 3, 4, 5, 3];
arr.findLast(x => x === 3);       // 3 (from index 5, not 2)
arr.findLastIndex(x => x === 3);  // 5

// Immutable array methods (return new array, don't mutate)
const sorted = arr.toSorted((a, b) => a - b);     // [1, 2, 3, 3, 4, 5]
const reversed = arr.toReversed();                  // [3, 5, 4, 3, 2, 1]
const spliced = arr.toSpliced(1, 2, 99);           // [1, 99, 4, 5, 3]
const replaced = arr.with(0, 100);                  // [100, 2, 3, 4, 5, 3]
// Original 'arr' is UNCHANGED in all cases
```

**ES2024:**
```javascript
// Object.groupBy
const people = [
  { name: 'John', dept: 'Engineering' },
  { name: 'Jane', dept: 'Marketing' },
  { name: 'Bob', dept: 'Engineering' },
];

const grouped = Object.groupBy(people, p => p.dept);
// { Engineering: [{John}, {Bob}], Marketing: [{Jane}] }
// No more lodash.groupBy!

// Promise.withResolvers
const { promise, resolve, reject } = Promise.withResolvers();
// Cleaner than: new Promise((res, rej) => { ... })
```

**Closing:**
Optional chaining + nullish coalescing alone eliminated hundreds of lines of null-checking boilerplate in my projects. `toSorted()`/`toReversed()` promote immutability. `structuredClone` replaced JSON tricks. These features are production essentials, not nice-to-haves.

---

## Q19. How does JavaScript garbage collection work? What causes memory leaks?

**Summary:**
JavaScript uses automatic garbage collection with a "mark-and-sweep" algorithm. The engine periodically identifies objects that are no longer reachable from the root (global scope, call stack) and frees their memory. Memory leaks happen when objects remain reachable but are no longer needed — the GC can't free them because they're technically still in use.

**How Mark-and-Sweep Works:**
```
Step 1: Start from roots (global, call stack, closures)
Step 2: Mark all objects reachable from roots
Step 3: Sweep (free) all unmarked objects

Roots
  │
  ├── window/global
  ├── Currently executing function's local variables
  ├── Closures capturing variables
  └── Event listener references

Reachable = alive. Unreachable = garbage collected.
```

**Top 5 Memory Leak Patterns:**

**1. Forgotten Event Listeners:**
```javascript
// ❌ Listener holds reference to component, prevents GC
function createWidget() {
  const widget = { data: new Array(1000000) };
  window.addEventListener('resize', () => {
    updateWidget(widget); // closure retains 'widget'
  });
  return widget;
}
// Even if you lose reference to widget, the listener keeps it alive

// ✅ Fix — remove listener when done
function createWidget() {
  const widget = { data: new Array(1000000) };
  const handler = () => updateWidget(widget);
  window.addEventListener('resize', handler);

  return {
    ...widget,
    destroy() { window.removeEventListener('resize', handler); }
  };
}
```

**2. Forgotten Timers:**
```javascript
// ❌ Interval keeps running after component is gone
const id = setInterval(() => {
  const el = document.getElementById('status');
  if (el) el.textContent = new Date().toISOString();
  // Even if #status element is removed, interval keeps running
  // 'el' reference prevents GC of removed DOM elements
}, 1000);

// ✅ Fix — clear when no longer needed
clearInterval(id);
```

**3. Closures Retaining Large Objects:**
```javascript
// ❌ Closure holds reference to 'hugeData' long after it's needed
function processData() {
  const hugeData = fetchHugeDataset(); // 500MB
  return function getStats() {
    return hugeData.length; // only needs .length, but retains entire object
  };
}
const getStats = processData(); // hugeData is NEVER garbage collected

// ✅ Fix — extract only what's needed
function processData() {
  const hugeData = fetchHugeDataset();
  const length = hugeData.length; // copy the value
  // hugeData can now be GC'd after processData() returns
  return function getStats() { return length; };
}
```

**4. Detached DOM Elements:**
```javascript
// ❌ Variable holds reference to removed DOM element
let button = document.getElementById('submit');
document.body.removeChild(button.parentElement);
// 'button' variable still references the removed element → can't be GC'd

// ✅ Fix — null the reference
button = null; // allow GC
```

**5. Growing Collections in Long-Lived Scopes:**
```javascript
// ❌ Module-level array grows forever
const eventLog = [];
export function logEvent(event) {
  eventLog.push({ ...event, timestamp: Date.now() });
  // Never trimmed → grows until OOM
}

// ✅ Fix — cap the size
export function logEvent(event) {
  eventLog.push({ ...event, timestamp: Date.now() });
  if (eventLog.length > 10000) eventLog.splice(0, 5000);
}
```

**Detecting Leaks — Chrome DevTools:**
```
1. Memory tab → Take Heap Snapshot
2. Perform the action (navigate pages, open/close modals)
3. Take another Heap Snapshot
4. Compare: "Objects allocated between Snapshot 1 and 2"
5. Filter by "Detached" — shows DOM nodes that should be GC'd
6. Allocation Timeline — shows memory growth over time
```

**Closing:**
GC handles reachable/unreachable objects automatically. Memory leaks happen when objects stay reachable but aren't needed — event listeners, timers, closures, and global references are the usual suspects. Always clean up in `destroy`/`unmount` lifecycle hooks.

---

## Q20. What are Web Workers? How do you handle CPU-intensive tasks in JavaScript?

**Summary:**
Web Workers run JavaScript in a separate background thread — they don't block the main thread (UI). I use them for CPU-intensive tasks like data processing, image manipulation, encryption, and large dataset sorting. Communication happens via `postMessage` — no shared memory (except `SharedArrayBuffer`).

**How Web Workers Work:**
```
Main Thread (UI)                    Worker Thread (Background)
┌─────────────────────┐            ┌─────────────────────┐
│ Event Loop           │            │ Separate Event Loop  │
│ DOM access ✅         │            │ DOM access ❌         │
│ UI rendering         │            │ CPU-intensive tasks  │
│                      │            │                      │
│  postMessage() ──────┼───────────▶│  onmessage()        │
│  onmessage()   ◀─────┼────────────│  postMessage()      │
└─────────────────────┘            └─────────────────────┘
```

**Basic Example:**
```javascript
// main.js
const worker = new Worker('worker.js');

worker.postMessage({ data: largeArray, operation: 'sort' });

worker.onmessage = (event) => {
  console.log('Sorted:', event.data);
  renderResults(event.data);
};

worker.onerror = (error) => {
  console.error('Worker error:', error.message);
};

// worker.js
self.onmessage = (event) => {
  const { data, operation } = event.data;

  if (operation === 'sort') {
    const sorted = data.sort((a, b) => a - b); // heavy CPU work
    self.postMessage(sorted);
  }
};
```

**Real-World Example — CSV Parsing Without Freezing UI:**
```javascript
// main.js
async function processCSV(file) {
  showLoadingSpinner();

  const worker = new Worker(new URL('./csv-worker.js', import.meta.url));
  const text = await file.text();

  return new Promise((resolve, reject) => {
    worker.onmessage = ({ data }) => {
      if (data.type === 'progress') {
        updateProgressBar(data.percent);
      } else if (data.type === 'complete') {
        hideLoadingSpinner();
        resolve(data.result);
        worker.terminate(); // cleanup
      }
    };
    worker.onerror = reject;
    worker.postMessage({ csv: text });
  });
}

// csv-worker.js
self.onmessage = ({ data }) => {
  const rows = data.csv.split('\n');
  const results = [];

  for (let i = 0; i < rows.length; i++) {
    results.push(parseRow(rows[i]));

    // Report progress every 10%
    if (i % Math.floor(rows.length / 10) === 0) {
      self.postMessage({
        type: 'progress',
        percent: Math.round((i / rows.length) * 100)
      });
    }
  }

  self.postMessage({ type: 'complete', result: results });
};
```

**Inline Worker (No Separate File):**
```javascript
// Create worker from a function — no separate file needed
function createInlineWorker(fn) {
  const blob = new Blob(
    [`self.onmessage = function(e) { (${fn.toString()})(e) }`],
    { type: 'application/javascript' }
  );
  return new Worker(URL.createObjectURL(blob));
}

const worker = createInlineWorker((event) => {
  const result = event.data.map(n => n * n);
  self.postMessage(result);
});
```

**When to Use Workers:**
```
✅ Good candidates for Web Workers:
  - Sorting/filtering 100K+ records
  - CSV/Excel parsing
  - Image processing (canvas, pixel manipulation)
  - Cryptographic operations
  - Complex calculations (statistics, ML inference)
  - JSON parsing of very large payloads
  - Search indexing

❌ NOT suitable:
  - DOM manipulation (no access)
  - Simple API calls (use async/await)
  - Tasks under 50ms (overhead not worth it)
```

**Worker Limitations:**
```
No access to: document, window, DOM, parent scope
Has access to: fetch, setTimeout, IndexedDB, WebSocket, importScripts
Communication: postMessage only (data is cloned, not shared)
Exception: SharedArrayBuffer + Atomics for shared memory (advanced)
```

**Closing:**
Web Workers keep the UI responsive during heavy computation. I use them for data processing that would otherwise freeze the browser — CSV parsing, large array operations, image processing. The `postMessage` overhead is negligible compared to the benefit of a responsive UI.

---

## Quick Reference Summary

| # | Topic | Key Takeaway |
|---|-------|-------------|
| 1 | Event Loop | Microtasks (Promises) before macro-tasks (setTimeout) |
| 2 | Closures | Function + its outer scope — private state, factories |
| 3 | var/let/const | `const` default, `let` for reassignment, `var` never |
| 4 | Prototypal Inheritance | Objects inherit from objects via `__proto__` chain |
| 5 | `this` keyword | Determined by call-site; arrow functions inherit lexically |
| 6 | Promises | `all` (all succeed), `allSettled` (all results), `race` (first), `any` (first success) |
| 7 | == vs === | `===` always; `==` only for `value == null` check |
| 8 | Generators | Pause/resume with `yield`; lazy evaluation, async iteration |
| 9 | Shallow vs Deep Copy | `structuredClone()` for deep, spread for shallow |
| 10 | async/await | Parallel with `Promise.all`, never `await` in `forEach` |
| 11 | Event Delegation | One listener on parent, works for dynamic children |
| 12 | Debounce/Throttle | Debounce = wait for stop; Throttle = at most once per interval |
| 13 | WeakMap/WeakSet | Weak references for metadata/caching without memory leaks |
| 14 | CJS vs ESM | ESM for tree-shaking and modern code; CJS is legacy |
| 15 | Proxy/Reflect | Intercept object operations — validation, reactivity, logging |
| 16 | call/apply/bind | `call` (args), `apply` (array), `bind` (returns new function) |
| 17 | Design Patterns | Singleton, Observer, Strategy, Factory, Pub/Sub |
| 18 | ES2020-2024 | `?.`, `??`, `#private`, `structuredClone`, `toSorted`, `Object.groupBy` |
| 19 | Garbage Collection | Mark-and-sweep; leaks from listeners, timers, closures, globals |
| 20 | Web Workers | Background threads for CPU-intensive tasks; `postMessage` API |
