# TypeScript — Interview Q&A (Easy Version)

> Every question follows the same shape — **Easiest way to remember → simple explanation → real-world example → diagram → interview trap → Interview-ready answer → Easy memory box.**
>
> 🔵 **In your RoboGebra code** blocks point at real files. Full index: **[39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)**.

**Your real TypeScript versions:**

```
robogebra-mobile     → TypeScript 5.4  (Angular 18)
robogebra-web        → TypeScript 5.1  (Angular 16)
robogebra-backoffice → TypeScript 4.9  (Angular 16)
robogebra-crm-web    → TypeScript 5.8  (React 18)
```

---

## Contents

1. [Basics](#basics)
2. [Functions](#functions)
3. [OOP](#oop)
4. [Advanced Types](#advanced-types)
5. [Decorators, Declaration Files, Modules](#decorators-declaration-files-modules)
6. [Practical / Angular](#practical--angular)
7. [Quick Revision Sheet](#quick-revision-sheet)

---

## Basics

### Q: What is TypeScript and what are its advantages over JavaScript?

The easiest way to remember:

```
TypeScript = JavaScript + TYPES, checked at COMPILE time.

It finds your bugs BEFORE the user does. ⭐
```

```typescript
// JavaScript — the bug reaches production 💥
function getArea(w, h) { return w * h; }
getArea("5", 10);          // "5" * 10 = 50 … but getArea("a", 10) = NaN, silently

// TypeScript — the bug is caught while you type ⭐
function getArea(w: number, h: number): number { return w * h; }
getArea("5", 10);          // ❌ Argument of type 'string' is not assignable to 'number'
```

```
        JAVASCRIPT                     TYPESCRIPT
   write → ship → user hits it    write → ❌ RED SQUIGGLE → fix it ⭐
              💥 3am call                     ✅ never shipped
```

Real-world idea: **a spell-checker while you write** versus discovering the typo after the book is printed.

#### The advantages worth naming

```
✅ Errors at COMPILE time, not runtime ⭐
✅ IntelliSense / autocomplete — the IDE knows every property
✅ SAFE REFACTORING — rename a field and every usage is found ⭐
✅ Self-documenting — the signature says what it needs
✅ Modern JS features, compiled down for old browsers
✅ Interfaces, generics, enums, access modifiers
```

⭐ Two things to be precise about, because interviewers probe them:

```
1. TypeScript is a SUPERSET — every valid .js file is valid .ts ⭐
2. Types are ERASED at compile time. There is NO type checking at runtime. ⭐
   → the compiled output is plain JavaScript
```

That second point explains a lot later (why an interface can't be a DI token, why `instanceof` doesn't work on an interface).

#### Interview-ready answer

> TypeScript is a strongly typed superset of JavaScript developed by Microsoft that compiles down to plain JavaScript. Its main advantage is catching type errors at compile time instead of at runtime, which removes a whole class of production bugs. It also gives much better tooling — accurate autocomplete and safe refactoring, because the compiler can find every usage of a renamed symbol. It adds interfaces, generics, enums and access modifiers, which makes large codebases far easier to maintain. The key thing to remember is that types are erased at compile time — there's no runtime type checking, so the output is ordinary JavaScript.

#### Easy memory

```
TypeScript = JavaScript + TYPES at COMPILE time ⭐ (a spell-checker as you write)
✅ compile-time errors | IntelliSense | SAFE REFACTORING ⭐ | self-documenting

Superset: all valid JS is valid TS ⭐
Types are ERASED at compile time — NO runtime checking ⭐
```

---

### Q: How does TypeScript compile / how does it run?

```
.ts file
   ↓ tsc (the TypeScript compiler)
type CHECKING happens here ⭐ — then the types are THROWN AWAY
   ↓
.js file   ← plain JavaScript, no types at all
   ↓
browser / Node runs it
```

```typescript
// input.ts
let name: string = "Priya";

// output.js  ← the type annotation is GONE
var name = "Priya";
```

⭐ The consequence to state:

```
The browser NEVER sees TypeScript. It only runs the compiled JavaScript.

So a type error CANNOT crash your app at runtime —
but a wrong value from an API CAN, because nothing checks it at runtime. ⭐
```

That last clause matters: `response as User` is a *promise you make to the compiler*, not a validation.

#### Easy memory

```
.ts → tsc → type check ⭐ → types ERASED → plain .js → the browser runs that
The browser never sees TypeScript.
`as User` on an API response is a PROMISE, not a validation ⭐
```

---

### Q: What are the key `tsconfig.json` options?

```json
{
  "compilerOptions": {
    "target": "ES2022",           // which JS version to output
    "module": "ES2022",           // module system
    "strict": true,               // ⭐ turns on ALL the strict checks
    "noImplicitAny": true,        // an untyped parameter is an error
    "strictNullChecks": true,     // ⭐ null/undefined must be handled
    "sourceMap": true,            // debug .ts in the browser
    "outDir": "./dist",
    "esModuleInterop": true,
    "experimentalDecorators": true,   // required by Angular
    "paths": { "@app/*": ["src/app/*"] }   // ⭐ import aliases
  }
}
```

⭐ The two that actually change your day:

```
strict: true          → the master switch; turns on ~8 checks at once
strictNullChecks      → the single most valuable one ⭐
```

```typescript
// WITHOUT strictNullChecks
let name: string = null;        // allowed 😬 → a runtime NPE waiting to happen

// WITH strictNullChecks ⭐
let name: string = null;        // ❌ Type 'null' is not assignable to type 'string'
let name: string | null = null; // ✅ you must be EXPLICIT
```

#### 🔵 In your RoboGebra code — and the honest answer ⭐

`robogebra-mobile/tsconfig.json`:

```json
"strict": true,
"noImplicitAny": false,          ⚠️
"strictNullChecks": false,       ⚠️
"strictPropertyInitialization": false,  ⚠️
"strictTemplates": true          ✅
```

> 🗣️ *"`strict` is on but three of the sub-flags are switched back off — `strictNullChecks`, `noImplicitAny` and `strictPropertyInitialization`. That's a legacy decision: the app started on Angular 12 and turning `strictNullChecks` on today produces thousands of errors at once. `strictTemplates` **is** on, which catches the template-level type errors. If I were tightening it, I'd enable `strictNullChecks` folder by folder using a path-scoped config rather than flipping it globally, because it's the flag that actually prevents runtime null errors."*

⭐ That is exactly the right answer: **name the gap, explain the reason, and give the migration strategy.** Pretending it's fully strict invites one follow-up you can't survive.

#### Easy memory

```
target | module | strict ⭐ | strictNullChecks ⭐ | paths (aliases) | sourceMap
experimentalDecorators → required by Angular

strictNullChecks is the one that matters ⭐
Your answer: "strict is on, but strictNullChecks is off for legacy reasons —
I'd enable it folder by folder, not globally." ⭐
```

---

### Q: `type` vs `interface` — what's the difference?

The easiest way to remember:

```
interface → for OBJECT SHAPES, and it can be REOPENED (declaration merging) ⭐
type      → for ANYTHING (unions, primitives, tuples), but it CANNOT be reopened ⭐
```

```typescript
interface User { id: number; name: string; }
type UserT   = { id: number; name: string; };      // the same thing here
```

The differences that actually matter:

```typescript
// 1. UNIONS — only `type` can do this ⭐
type Status = 'active' | 'inactive' | 'pending';
type ID = string | number;
// interface Status = ...   ❌ impossible

// 2. DECLARATION MERGING — only `interface` ⭐
interface Window { myApp: string; }
interface Window { version: number; }
// → Window now has BOTH  ✅ (this is how you extend third-party types)

type A = { x: number };
type A = { y: number };     // ❌ Duplicate identifier

// 3. Extending
interface Admin extends User { role: string; }        // interface
type AdminT = UserT & { role: string };               // type — intersection
```

| | `interface` | `type` |
|---|---|---|
| Object shapes | ✅ | ✅ |
| Unions / primitives / tuples | ❌ | ✅ ⭐ |
| Declaration merging | ✅ ⭐ | ❌ |
| Extends | `extends` | `&` intersection |
| Error messages | usually clearer | can get noisy |

#### The rule to state

```
Use INTERFACE for object shapes and public API contracts (mergeable, clearer errors)
Use TYPE for unions, intersections, tuples, and mapped/conditional types ⭐
Be CONSISTENT within a codebase — that matters more than the choice.
```

#### Easy memory

```
interface → object shapes + MERGEABLE ⭐ (extend third-party types)
type      → unions, tuples, primitives, mapped/conditional ⭐ (not mergeable)

type Status = 'a' | 'b'   ← ONLY type can do this ⭐
interface Window {...}    ← declaration merging: ONLY interface ⭐
```

---

### Q: Explain `any` vs `unknown` vs `never` vs `void`.

The easiest way to remember:

```
any     → "I give up on type checking"  ⚠️ DANGEROUS
unknown → "I don't know YET — check me first" ✅ the SAFE any ⭐
void    → "returns NOTHING"
never   → "NEVER returns at all" (throws or loops forever)
```

```typescript
// any — switches OFF all checking 💥
let a: any = "hello";
a.toFixed(2);               // ✅ compiles… 💥 crashes at runtime

// unknown — you MUST narrow it first ⭐
let u: unknown = "hello";
u.toFixed(2);               // ❌ 'u' is of type 'unknown'
if (typeof u === 'number') { u.toFixed(2); }   // ✅ now it's safe

// void — no return value
function log(msg: string): void { console.log(msg); }

// never — this function NEVER finishes normally ⭐
function fail(msg: string): never { throw new Error(msg); }
function loop(): never { while (true) {} }
```

```
any     → the compiler LOOKS AWAY 🙈  → bugs walk straight through
unknown → the compiler BLOCKS you until you PROVE the type ✅ ⭐
void    → a function that returns nothing
never   → a function that never returns at all
```

Real-world idea:

```
any     → a security guard who waves everyone through 🙈
unknown → a guard who checks every ID before letting you in ✅
void    → the meeting ends with no decision
never   → the meeting never ends 🔁
```

⭐ **The interview point:** `unknown` is the type-safe replacement for `any`. If you must accept anything (a JSON parse, a third-party callback), use `unknown` and narrow it.

`never` also has a clever use — **exhaustiveness checking**:

```typescript
type Status = 'active' | 'inactive';

function label(s: Status): string {
  switch (s) {
    case 'active':   return 'Active';
    case 'inactive': return 'Inactive';
    default:
      const _exhaustive: never = s;    // ⭐ adding a 3rd status = COMPILE ERROR here
      return _exhaustive;
  }
}
```

#### Easy memory

```
any     → no checking ⚠️ (the guard waves you through 🙈)
unknown → must NARROW first ✅ the safe `any` ⭐
void    → returns nothing
never   → never returns (throw / infinite loop) — also EXHAUSTIVENESS checking ⭐

Rule: never use `any` when `unknown` will do ⭐
```

---

### Q: What is type inference?

```
Type inference = TypeScript works out the type for you, so you don't
                 have to annotate everything. ⭐
```

```typescript
let name = "Priya";        // inferred: string  ⭐
name = 42;                 // ❌ error — it's locked to string

const age = 30;            // inferred: 30  (a LITERAL type, because it's const) ⭐
let age2 = 30;             // inferred: number

function add(a: number, b: number) { return a + b; }   // return type inferred: number ⭐
```

```
let  → widened to the general type   ("Priya" → string)
const→ narrowed to the LITERAL type  ("Priya" → "Priya") ⭐
```

⭐ The style rule: **annotate the inputs, let the compiler infer the outputs.**

```typescript
// ❌ noisy — the compiler already knew
const names: string[] = ['a', 'b'].map((s: string): string => s.toUpperCase());

// ✅ annotate only what's needed
const names = ['a', 'b'].map(s => s.toUpperCase());
```

⚠️ One place you **should** annotate: a function's public return type, so an accidental change to the body becomes a compile error instead of silently changing the API.

#### Easy memory

```
Inference = TS works out the type from the value ⭐
let  → general type (string) | const → LITERAL type ("Priya") ⭐
Annotate INPUTS, let it infer OUTPUTS — but annotate PUBLIC return types ⭐
```

---

### Q: What are literal types?

```
A literal type restricts a value to EXACT values, not just "a string". ⭐
```

```typescript
let status: 'active' | 'inactive' | 'pending';
status = 'active';      // ✅
status = 'deleted';     // ❌ not assignable

type Dice = 1 | 2 | 3 | 4 | 5 | 6;
type Method = 'GET' | 'POST' | 'PUT' | 'DELETE';
```

```
string          → ANY string, including typos ("actve") 💥
'active'|'idle' → only these two ✅ + autocomplete in the IDE ⭐
```

⭐ This is often better than an enum, because it compiles to **nothing** — no runtime object is generated.

Real-world idea: a **dropdown vs a free-text box.** The dropdown makes a typo impossible.

#### Easy memory

```
Literal type = only these EXACT values ⭐
type Method = 'GET' | 'POST'   → typo becomes a compile error + autocomplete
Zero runtime cost (unlike an enum) ⭐
```

---

### Q: What are union and intersection types?

```
UNION        A | B  →  "EITHER A or B"    ⭐ (OR)
INTERSECTION A & B  →  "BOTH A and B"     ⭐ (AND)
```

```typescript
// UNION — either
type ID = string | number;
function print(id: ID) {
  if (typeof id === 'string') id.toUpperCase();   // ⭐ narrowing needed
  else id.toFixed(0);
}

// INTERSECTION — both, merged
interface Person { name: string; }
interface Employee { salary: number; }
type Staff = Person & Employee;                    // has BOTH ⭐

const s: Staff = { name: 'Priya', salary: 50000 };  // both required
```

```
Union  A | B  → you may only use what BOTH have, until you NARROW ⭐
Intersection A & B → you get EVERYTHING from both
```

⚠️ The counter-intuitive bit worth knowing:

```
A UNION makes the usable surface SMALLER (only the shared members)
An INTERSECTION makes it BIGGER (all members of both) ⭐
```

#### Easy memory

```
| = OR  (union)        → narrow it before using type-specific members ⭐
& = AND (intersection) → gets everything from both

Union = FEWER usable members | Intersection = MORE ⭐
```

---

### Q: What are tuples?

```
Tuple = an array with a FIXED LENGTH and a FIXED TYPE PER POSITION. ⭐
```

```typescript
let pair: [string, number] = ['Priya', 30];
pair = [30, 'Priya'];               // ❌ wrong order

let point: [number, number, number?] = [1, 2];        // optional third
let flexible: [string, ...number[]] = ['a', 1, 2, 3]; // rest element

// named tuple members (TS 4.0+) — much more readable ⭐
type Coord = [x: number, y: number];
```

```
Array  number[]        → any length, ALL the same type
Tuple  [string, number]→ EXACTLY 2, in THAT order ⭐
```

Where you meet them in real code:

```typescript
const [value, setValue] = useState(0);      // React — a tuple ⭐
Object.entries(obj);                         // [string, T][] — a tuple array
combineLatest([a$, b$]).subscribe(([a, b]) => ...);   // RxJS ⭐
```

#### Easy memory

```
Tuple = fixed LENGTH + fixed TYPE PER POSITION ⭐ → [string, number]
Named members: [x: number, y: number]
You've used them: useState, Object.entries, combineLatest ⭐
```

---

### Q: What are enums?

```
Enum = a set of NAMED CONSTANTS. It exists at RUNTIME (unlike a type). ⭐
```

```typescript
enum Status { ACTIVE, INACTIVE, PENDING }       // numeric: 0, 1, 2 ⚠️
enum Role   { ADMIN = 'ADMIN', USER = 'USER' }  // string enum ✅ preferred

const enum Direction { Up, Down }                // inlined, zero runtime cost ⭐
```

⚠️ **The numeric-enum trap — the same bug as JPA's `@Enumerated(ORDINAL)`:**

```typescript
enum Status { ACTIVE, INACTIVE }       // stored in the DB as 0, 1

// six months later, someone inserts a value:
enum Status { ACTIVE, PENDING, INACTIVE }
//              0        1         2
```

```
A row saved as 1 used to mean INACTIVE.
It now means PENDING. 💥
Every existing record silently changed meaning. No error.
```

**Always use string enums.** → the same rule as [24 — JPA `@Enumerated(STRING)`](./24-orm-jpa-hibernate.md).

#### 🔵 In your RoboGebra code — enums done right ⭐

**File:** `core/model/http-error.model.ts`

```typescript
export enum HttpErrorStatus {
    UNAUTHORIZED = 401,          // ⭐ EXPLICIT values, not positional
    FORBIDDEN = 403,
    TOO_MANY_REQUEST = 429
}

export enum ErrorCode {
    TOKEN_EXPIRED = 30000,
    TOKEN_INVALID = 30001,
    REFRESH_TOKEN_INVALID = 30002,
    SOLUTION_TRIAL_LIMIT_EXCEEDED = 50001,
    MULTI_USER_LOGIN_EXCEEDED = 60001,
    SESSION_FORCEFULLY_REMOVED = 60002
}

export const MultiUserErrorCodes: ErrorCode[] = [   // ⭐ a typed grouping constant
    ErrorCode.MULTI_USER_LOGIN_EXCEEDED,
    ErrorCode.SESSION_FORCEFULLY_REMOVED
];
```

> 🗣️ *"Our error codes are enums with **explicit** numeric values agreed with the backend — never positional, so inserting a new code can't shift the meaning of existing ones. They're also banded by domain: 30000s for tokens, 50000s for trial limits, 60000s for multi-user sessions, which makes an unknown code readable at a glance. And `MultiUserErrorCodes` is a typed constant array so the interceptor can test membership instead of listing conditions."*

Also `AppEndPoints` and `AppResolver` are enums — centralising every URL and resolver key so a rename is a compile error rather than a broken link.

#### Easy memory

```
Enum = named constants that EXIST at runtime ⭐
⚠️ Numeric enums are POSITIONAL — inserting a value shifts every meaning 💥
   (the same bug as JPA @Enumerated(ORDINAL))
→ ALWAYS use explicit values or string enums ⭐
Alternative with zero runtime cost: a union of literals
```

---

## Functions

### Q: How do you type functions?

```typescript
// parameter types + return type
function add(a: number, b: number): number { return a + b; }

// arrow function
const multiply = (a: number, b: number): number => a * b;

// a FUNCTION TYPE ⭐
type MathOp = (a: number, b: number) => number;
const divide: MathOp = (a, b) => a / b;     // params inferred from the type ⭐

// a callback parameter
function process(items: number[], cb: (item: number) => void): void {
  items.forEach(cb);
}
```

⭐ Note the two different arrows:

```
Type position:  (a: number) => number      ← a function TYPE
Value position: (a) => a * 2               ← an arrow FUNCTION
```

#### Easy memory

```
function f(a: T, b: T): R
type MathOp = (a: number, b: number) => number ⭐ — assign it and params are inferred
=> in a TYPE position means "returns"; in a VALUE position it's an arrow function
```

---

### Q: How do optional and default parameters work?

```typescript
function greet(name: string, greeting?: string): string {          // OPTIONAL
  return `${greeting ?? 'Hello'}, ${name}`;
}

function greet2(name: string, greeting: string = 'Hello'): string { // DEFAULT ⭐
  return `${greeting}, ${name}`;
}
```

```
optional  ?  → the type becomes `string | undefined` ⭐
default  = x → the type stays `string`, and it's optional at the call site ⭐
```

⚠️ **Optional parameters must come last:**

```typescript
function bad(a?: string, b: string) { }    // ❌ required cannot follow optional
```

⭐ And the `??` vs `||` detail that gets asked:

```typescript
greeting ?? 'Hello'     // only when null/undefined ⭐
greeting || 'Hello'     // ALSO replaces "" and 0 and false 💥
```

#### Easy memory

```
name?: string   → optional → type is `string | undefined` ⭐
name = 'x'      → default  → type stays `string`
Optional params must come LAST ⚠️
?? (null/undefined only) ⭐ vs || (also "" 0 false) 💥
```

---

### Q: What are rest parameters?

```typescript
function sum(...numbers: number[]): number {
  return numbers.reduce((t, n) => t + n, 0);
}
sum(1, 2, 3, 4);                       // 10

function log(prefix: string, ...args: unknown[]): void { }   // rest must be LAST ⭐
```

```
Rest ...x  → collects MANY arguments into ONE array (in a signature)
Spread ...x→ expands ONE array into MANY values (at a call site)
```

```typescript
const nums = [1, 2, 3];
sum(...nums);                          // SPREAD — same syntax, opposite job ⭐
```

#### Easy memory

```
...args in a SIGNATURE = REST (collect) — must be LAST ⭐
...args at a CALL SITE = SPREAD (expand)
Same three dots, opposite directions ⭐
```

---

### Q: What is function overloading?

```
Overloading = several SIGNATURES for ONE implementation, so the caller
              gets an exact return type instead of a union. ⭐
```

```typescript
function parse(value: string): string[];        // overload 1
function parse(value: number): number[];        // overload 2
function parse(value: string | number): any {   // ⭐ the IMPLEMENTATION (not callable)
  return typeof value === 'string' ? value.split('') : [value];
}

const a = parse('abc');   // typed string[] ⭐
const b = parse(123);     // typed number[] ⭐
```

```
WITHOUT overloads → the return type is `string[] | number[]`
                    → the caller has to narrow it every time 😩
WITH overloads    → the return type is exact ⭐
```

⚠️ Two rules:

```
1. The implementation signature is NOT callable — only the overloads are
2. The implementation must be COMPATIBLE with every overload
```

⭐ Unlike Java, TypeScript overloading is **purely a type-level trick** — the compiled JavaScript has exactly one function.

#### Easy memory

```
Several SIGNATURES + ONE implementation ⭐
The implementation signature is NOT callable
Purpose: an exact return type instead of a union
Unlike Java — it's compile-time only; JS has one function ⭐
```

---

## OOP

### Q: How do you define a class in TypeScript?

```typescript
class Employee {
  private id: number;
  public name: string;

  constructor(id: number, name: string) {
    this.id = id;
    this.name = name;
  }

  getDetails(): string { return `${this.id} — ${this.name}`; }
}
```

⭐ The **parameter property** shorthand — this is what real Angular code uses:

```typescript
class Employee {
  constructor(
    private id: number,          // ⭐ declares AND assigns the field
    public name: string,
    private readonly dept: string
  ) {}
}
```

```
An access modifier on a constructor parameter
      → TypeScript creates the field AND assigns it ⭐
      → which is exactly why Angular DI looks so clean:

constructor(private http: HttpClient) {}   ← declares this.http in one line ⭐
```

#### Easy memory

```
constructor(private http: HttpClient) {}
   ⭐ a PARAMETER PROPERTY — declares the field AND assigns it
   → this is why Angular DI is a one-liner
```

---

### Q: Explain the access modifiers.

```
public    → anywhere              (the DEFAULT)
private   → this class ONLY
protected → this class + SUBCLASSES ⭐
readonly  → set once (in the constructor), never reassigned ⭐
```

```typescript
class Account {
  public  name: string;
  private balance: number;          // only inside Account
  protected type: string;           // Account and its subclasses
  readonly id: number;              // ⭐ assign once, then locked

  constructor(id: number) { this.id = id; }
  changeId() { this.id = 5; }       // ❌ cannot assign to a read-only property
}
```

⚠️ **The critical caveat — `private` is COMPILE-TIME only:**

```typescript
const a = new Account(1);
a.balance;              // ❌ TypeScript error
(a as any).balance;     // ✅ works at runtime — the field is just there 💥
```

```
TypeScript `private` → erased at compile time → NOT real privacy ⭐
JavaScript `#field`  → TRUE runtime privacy ⭐
```

```typescript
class Account {
  #balance = 0;          // ⭐ genuinely private, enforced by the JS runtime
}
```

⭐ That distinction is a very good thing to volunteer.

And `readonly` has the same shallow-lock behaviour as Java's `final`:

```typescript
readonly items: string[] = [];
this.items.push('x');       // ✅ allowed — the CONTENTS aren't locked ⭐
this.items = [];            // ❌ the REFERENCE is locked
```

#### Easy memory

```
public (default) | private (this class) | protected (+ subclasses) | readonly (set once)

⚠️ TS `private` is COMPILE-TIME only → (obj as any).x reaches it 💥
   JS `#field` is REAL runtime privacy ⭐
readonly locks the REFERENCE, not the contents (like Java's final) ⭐
```

---

### Q: What are abstract classes?

```
abstract class = a base class you CANNOT instantiate;
                 it can hold shared code AND unimplemented methods. ⭐
```

```typescript
abstract class Shape {
  constructor(protected name: string) {}         // ⭐ constructors allowed

  abstract area(): number;                        // children MUST implement
  describe(): string { return `${this.name}: ${this.area()}`; }   // shared code ⭐
}

class Circle extends Shape {
  constructor(private r: number) { super('Circle'); }
  area(): number { return Math.PI * this.r ** 2; }
}

new Shape('x');    // ❌ Cannot create an instance of an abstract class
new Circle(5);     // ✅
```

Real-world idea: a **house foundation** — you can't live in it alone, but every house is built on one.

#### Easy memory

```
abstract class = cannot be instantiated; has shared CODE + shared STATE ⭐
abstract method → the child MUST implement it
Constructors and fields ARE allowed (unlike an interface)
```

---

### Q: Interface vs abstract class — when to use which?

```
interface      → a CONTRACT only; ERASED at compile time ⭐ (zero runtime cost)
abstract class → shared CODE + shared STATE; EXISTS at runtime ⭐
```

| | interface | abstract class |
|---|---|---|
| Runtime existence | ❌ **erased** ⭐ | ✅ exists |
| Implementation | ❌ (contract only) | ✅ concrete methods |
| Fields / state | ❌ | ✅ |
| Constructor | ❌ | ✅ |
| Multiple | ✅ `implements A, B` | ❌ one `extends` |
| Access modifiers | ❌ (all public) | ✅ |

⭐ The TypeScript-specific consequence of erasure:

```typescript
interface Logger { log(m: string): void; }

x instanceof Logger        // ❌ IMPOSSIBLE — interfaces don't exist at runtime ⭐
providers: [Logger]        // ❌ can't be an Angular DI token ⭐
```

```
→ which is exactly why Angular needs InjectionToken for interface-typed
  dependencies, and why an abstract class is sometimes used AS a DI token ⭐
```

```typescript
abstract class Logger { abstract log(m: string): void; }        // ⭐ usable as a token
providers: [{ provide: Logger, useClass: ConsoleLogger }];      // ✅ works
```

Deeper comparison (in Java terms): **[33 — Interface vs Abstract Class](./33-interface-vs-abstract-class.md)**.

#### Easy memory

```
interface → contract, ERASED ⭐ → no instanceof, NOT a DI token
abstract class → shared code + state, EXISTS at runtime → CAN be a DI token ⭐

That erasure is WHY Angular has InjectionToken ⭐
```

---

### Q: `implements` vs `extends`?

```
extends    → INHERIT the implementation (only ONE class) ⭐
implements → PROMISE to match a shape (as MANY as you like) ⭐
```

```typescript
class Animal { move() {} }
interface Swimmable { swim(): void; }
interface Flyable   { fly(): void; }

class Duck extends Animal implements Swimmable, Flyable {
  swim() {}
  fly() {}
}
```

```
extends    → you GET the code
implements → you PROMISE the shape, and must write it yourself ⭐
```

⭐ TypeScript nuance: you can `implements` a **class**, which takes only its *shape*, not its implementation:

```typescript
class B implements A { }    // ✅ B must have A's members, but inherits NO code ⭐
```

#### Easy memory

```
extends = INHERIT code (ONE) ⭐ | implements = PROMISE a shape (MANY) ⭐
You may `implements` a class — you get its SHAPE, not its code ⭐
```

---

### Q: How do getters and setters work?

```typescript
class Temperature {
  private _celsius = 0;

  get celsius(): number { return this._celsius; }

  set celsius(value: number) {
    if (value < -273) throw new Error('Below absolute zero');   // ⭐ VALIDATION
    this._celsius = value;
  }

  get fahrenheit(): number { return this._celsius * 9 / 5 + 32; }  // ⭐ COMPUTED
}
```

```typescript
const t = new Temperature();
t.celsius = 25;             // calls the SETTER (looks like a field ⭐)
console.log(t.fahrenheit);  // calls the GETTER — computed, never stored
```

```
Used like a PROPERTY, behaves like a METHOD ⭐
→ validation on write, computed values on read, without changing the call site
```

⚠️ In an Angular template, a getter runs on **every change detection cycle** — so keep it cheap, exactly like a method call in a template.

#### Easy memory

```
get x() / set x(v) → used like a FIELD, runs like a METHOD ⭐
Use for: validation on write, COMPUTED values on read
⚠️ A getter in an Angular template runs on every CD cycle — keep it cheap ⭐
```

---

### Q: What are static members?

```
static = belongs to the CLASS, not to any instance. ⭐
```

```typescript
class MathUtil {
  static readonly PI = 3.14159;
  static square(x: number): number { return x * x; }

  private static instance: MathUtil;
  static getInstance(): MathUtil {                    // ⭐ singleton factory
    return this.instance ??= new MathUtil();
  }
}

MathUtil.square(5);      // ✅ no object needed
```

#### 🔵 In your RoboGebra code — a static generic factory ⭐

**File:** `core/model/page-response.ts`

```typescript
export class PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;

    static fromJSON<U>(data: any, itemMapper: (item: any) => U): PageResponse<U> {   // ⭐
        const content = (data.content || []).map(itemMapper);
        return new PageResponse<U>(content, data.totalElements ?? 0, ...);
    }

    static empty<T>(size = 20, number = 0): PageResponse<T> {
        return new PageResponse<T>([], 0, 0, size, number);
    }
}
```

> 🗣️ *"`PageResponse<T>` wraps every paginated API response. The static `fromJSON` is a generic factory that takes a mapper function, so one class handles pages of books, exercises or study materials with full type safety. `empty()` gives components a safe initial value so they never have to null-check the list before the first load."*

⭐ That's **static + generics + a factory method + defensive defaults** in one small file.

#### Easy memory

```
static = belongs to the CLASS → MathUtil.square(5) ⭐
Common uses: constants, utilities, FACTORY methods, singletons
Your example: PageResponse<T>.fromJSON<U>(data, mapper) ⭐
```

---
## Advanced Types

### Q: What are generics?

The easiest way to remember:

```
Generics = a TYPE PLACEHOLDER, so ONE piece of code works with MANY types
           WITHOUT losing type safety. ⭐
```

#### The problem they solve

```typescript
// ❌ any — works with everything, and loses everything
function first(arr: any[]): any { return arr[0]; }
const n = first([1, 2, 3]);
n.toUpperCase();          // ✅ compiles… 💥 crashes at runtime

// ✅ generic — works with everything, KEEPS the type ⭐
function first<T>(arr: T[]): T { return arr[0]; }
const n = first([1, 2, 3]);      // typed number ⭐
n.toUpperCase();                  // ❌ caught at compile time ✅
```

```
any → the type is THROWN AWAY 💥
<T> → the type is CARRIED THROUGH ⭐
```

Real-world idea: a **shipping box.** The same box carries books or clothes — and the **label** tells you which, so you never open it expecting the wrong thing.

```typescript
// generic interface
interface ApiResponse<T> { data: T; status: number; message: string; }
const r: ApiResponse<User[]> = await http.get('/users');
r.data[0].name;                    // ⭐ fully typed all the way down

// generic class
class Box<T> {
  constructor(private value: T) {}
  get(): T { return this.value; }
}

// multiple type parameters
function pair<K, V>(key: K, value: V): [K, V] { return [key, value]; }
```

#### 🔵 In your RoboGebra code

**File:** `core/model/page-response.ts` — `PageResponse<T>` wraps every paginated response.
**File:** `core/model/public/public-list-item.model.ts` — `PublicListItem<T>` with `fromJSON<T>(row, mapFn)`.

> 🗣️ *"Every paginated endpoint returns `PageResponse<T>` — content, totalElements, totalPages, size and number. One generic class covers pages of books, exercises and study materials, and the static `fromJSON<U>` takes a mapper function so the item type is preserved end to end. Without generics we'd either duplicate the class per type or fall back to `any` and lose the safety at exactly the point where the data enters the app."*

#### Easy memory

```
Generics = a type PLACEHOLDER <T> → one implementation, MANY types, type-safe ⭐
any THROWS the type away 💥 | <T> CARRIES it through ⭐
Shipping box 📦 — same box, labelled contents

Your example: PageResponse<T> + static fromJSON<U>(data, mapper) ⭐
```

---

### Q: What are generic constraints?

```
A constraint says: "T can be anything — AS LONG AS it has this." ⭐
      <T extends SomeShape>
```

```typescript
// ❌ unconstrained — T might not have .length
function longest<T>(a: T, b: T): T {
  return a.length > b.length ? a : b;      // ❌ Property 'length' does not exist on type 'T'
}

// ✅ constrained ⭐
function longest<T extends { length: number }>(a: T, b: T): T {
  return a.length > b.length ? a : b;
}
longest('abc', 'de');        // ✅ strings have length
longest([1,2], [3]);         // ✅ arrays have length
longest(1, 2);               // ❌ numbers don't
```

The `keyof` constraint — the one interviewers love:

```typescript
function getProp<T, K extends keyof T>(obj: T, key: K): T[K] {   // ⭐
  return obj[key];
}

const user = { name: 'Priya', age: 30 };
getProp(user, 'name');       // typed string ⭐
getProp(user, 'email');      // ❌ 'email' is not a key of the object ⭐
```

```
K extends keyof T  → the key must actually EXIST on the object ⭐
T[K]               → the return type is the type of THAT property ⭐
```

⭐ That is genuinely impressive to write on a whiteboard: a typo in a property name becomes a **compile error**.

#### Easy memory

```
<T extends Shape> = "T can be anything, as long as it HAS this" ⭐
<T, K extends keyof T>(obj: T, key: K): T[K]  ← type-safe property access ⭐
   → a typo in the key name becomes a COMPILE ERROR
```

---

### Q: Explain the common utility types.

The easiest way to remember them **by what they do to an existing type**:

```
Partial<T>    → make every property OPTIONAL     ⭐ (PATCH updates)
Required<T>   → make every property REQUIRED
Readonly<T>   → make every property READ-ONLY
Pick<T, K>    → KEEP only these keys             ⭐
Omit<T, K>    → REMOVE these keys                ⭐ (drop the password!)
Record<K, V>  → build an object type from keys→values ⭐
```

```typescript
interface User { id: number; name: string; email: string; password: string; }

type UserUpdate  = Partial<User>;                 // every field optional ⭐
type UserPreview = Pick<User, 'id' | 'name'>;     // just those two ⭐
type SafeUser    = Omit<User, 'password'>;        // everything except password ⭐
type Frozen      = Readonly<User>;
type ById        = Record<number, User>;          // { [key: number]: User } ⭐
type Flags       = Record<'dev' | 'prod', boolean>;
```

Where each one actually earns its place:

```typescript
// PATCH endpoint — only the changed fields
updateUser(id: number, changes: Partial<User>) { }        ⭐

// API response — never leak the password hash
toDto(u: User): Omit<User, 'password'> { }                ⭐

// a lookup map built from an array
const byId: Record<number, User> = {};                     ⭐
```

Others worth naming:

```
ReturnType<typeof fn>   → the return type of a function ⭐
Parameters<typeof fn>   → its parameter types as a tuple
NonNullable<T>          → strips null | undefined ⭐
Awaited<T>              → unwraps a Promise
Exclude<T,U> / Extract<T,U>  → filter a union
```

⭐ **`Omit<User, 'password'>` is the strongest one to mention** — it connects directly to the DTO-vs-entity answer in [06 — Spring Boot](./06-spring-boot.md) and [24 — JPA](./24-orm-jpa-hibernate.md).

#### Easy memory

```
Partial ⭐ (all optional — PATCH) | Required | Readonly
Pick ⭐ (keep these) | Omit ⭐ (drop these — the password!) | Record<K,V> ⭐
ReturnType | Parameters | NonNullable | Awaited

Omit<User,'password'> = the DTO answer, in the type system ⭐
```

---

### Q: What are mapped types?

```
A mapped type LOOPS OVER the keys of a type and transforms each one. ⭐
```

```typescript
type Optional<T> = { [K in keyof T]?: T[K] };        // this IS Partial<T> ⭐
type Frozen<T>   = { readonly [K in keyof T]: T[K] };// this IS Readonly<T>
type Nullable<T> = { [K in keyof T]: T[K] | null };

// with a KEY REMAP (TS 4.1+) — generate getters ⭐
type Getters<T> = {
  [K in keyof T as `get${Capitalize<string & K>}`]: () => T[K]
};

type UserGetters = Getters<{ name: string; age: number }>;
// → { getName: () => string; getAge: () => number }  ⭐
```

```
[K in keyof T]  → "for every key K in T…" ⭐
as `get${...}`  → rename the key while mapping
```

⭐ The line to say: *"`Partial`, `Required` and `Readonly` aren't magic — they're one-line mapped types in the standard library."*

#### Easy memory

```
Mapped type = a LOOP over keys ⭐ → { [K in keyof T]: ... }
Partial/Required/Readonly are all one-line mapped types ⭐
Key remapping: [K in keyof T as `get${Capitalize<K>}`]
```

---

### Q: What are conditional types?

```
A conditional type is a TYPE-LEVEL if/else. ⭐
      T extends U ? X : Y
```

```typescript
type IsString<T> = T extends string ? true : false;
type A = IsString<'hello'>;    // true
type B = IsString<42>;         // false

// with `infer` — EXTRACT a type from inside another ⭐
type Unwrap<T> = T extends Promise<infer U> ? U : T;
type C = Unwrap<Promise<string>>;   // string ⭐

type ElementOf<T> = T extends (infer E)[] ? E : never;
type D = ElementOf<number[]>;       // number ⭐
```

```
extends here means "is assignable to", NOT class inheritance ⭐
infer U = "capture whatever type sits in that slot" ⭐
```

⭐ You will rarely write these yourself, but the library types you use every day are built from them — `ReturnType`, `Awaited`, `NonNullable`. Saying *that* is the right level of answer.

#### Easy memory

```
T extends U ? X : Y  = a type-level IF ⭐  (extends = "assignable to")
infer U = capture the inner type ⭐ → Unwrap<Promise<string>> = string
You rarely write them; ReturnType / Awaited / NonNullable ARE them ⭐
```

---

### Q: What are type guards?

```
A type guard NARROWS a broad type down to a specific one,
so the compiler lets you use type-specific members. ⭐
```

```typescript
// 1. typeof — for primitives
function print(v: string | number) {
  if (typeof v === 'string') v.toUpperCase();    // ⭐ narrowed to string
  else v.toFixed(2);                              // ⭐ narrowed to number
}

// 2. instanceof — for classes
if (err instanceof HttpErrorResponse) { err.status; }

// 3. `in` — for object shapes ⭐
type Dog = { bark(): void }; type Cat = { meow(): void };
function speak(a: Dog | Cat) {
  if ('bark' in a) a.bark(); else a.meow();       // ⭐
}

// 4. a CUSTOM type guard — `x is T` ⭐⭐
function isUser(obj: unknown): obj is User {
  return typeof obj === 'object' && obj !== null && 'id' in obj && 'name' in obj;
}

if (isUser(data)) { data.name; }                  // ⭐ narrowed to User

// 5. a DISCRIMINATED UNION — the cleanest of all ⭐⭐
type Result =
  | { status: 'success'; data: User }
  | { status: 'error';   message: string };

function handle(r: Result) {
  if (r.status === 'success') r.data;             // ⭐ TS knows which branch
  else r.message;
}
```

```
The `obj is User` return type is what makes a custom guard work —
without it the function just returns a boolean and narrows NOTHING ⭐
```

Real-world idea: **airport security narrowing "a passenger" down to "a business-class passenger with hand luggage only"** — once verified, different rules apply.

⭐ **Discriminated unions are the answer to give for API responses** — a shared literal field (`status`, `kind`, `type`) lets the compiler pick the branch for you, with no casts anywhere.

#### Easy memory

```
typeof (primitives) | instanceof (classes) | in (shapes)
CUSTOM: function isUser(x): x is User ⭐ — the `is` is what does the narrowing
DISCRIMINATED UNION ⭐⭐ — a shared literal field picks the branch, no casts
```

---

### Q: Explain `typeof`, `keyof` and `in`.

```
typeof → take the TYPE of an existing VALUE  ⭐ (value → type)
keyof  → take the KEYS of a type as a union  ⭐ (type → keys)
in     → loop over keys (mapped types), or check a property at runtime
```

```typescript
const config = { url: 'https://api.x', retries: 3 };
type Config = typeof config;             // ⭐ { url: string; retries: number }

type Keys = keyof Config;                 // ⭐ 'url' | 'retries'

type Copy = { [K in keyof Config]: Config[K] };   // `in` — a mapped type

if ('url' in config) { }                  // `in` — a runtime check
```

⭐ The combination is the classic idiom:

```typescript
function get<T, K extends keyof T>(obj: T, key: K): T[K] { return obj[key]; }
```

⚠️ Careful: **`typeof` in a type position is not the same as JavaScript's runtime `typeof`.**

```typescript
typeof config           // TYPE position → the object's type ⭐
typeof x === 'string'   // VALUE position → the JS operator
```

#### Easy memory

```
typeof → VALUE → TYPE ⭐   (type Config = typeof config)
keyof  → TYPE → its KEYS ⭐ (type Keys = keyof Config)
in     → loop over keys (mapped type) OR a runtime property check

Same word `typeof`, two different meanings by position ⭐
```

---

### Q: What is type assertion (`as`)?

```
`as` = "trust me, compiler, I know what this is."

It CHANGES NOTHING at runtime. It only silences the checker. ⭐
```

```typescript
const el = document.getElementById('x') as HTMLInputElement;
el.value;                                   // ✅ compiles

const data = JSON.parse(json) as User;      // ⚠️ a PROMISE, not a validation ⭐
```

```
`as` performs NO conversion and NO check.
If the value isn't really a User, it crashes LATER, somewhere else 💥
```

⭐ The right framing: *"`as` moves the risk from the compiler to me."*

```typescript
// ❌ dangerous — a lie the compiler believes
const user = apiResponse as User;

// ✅ verify first ⭐
if (isUser(apiResponse)) { /* now genuinely a User */ }
```

Also worth knowing:

```typescript
const config = { url: 'x' } as const;   // ⭐ `as const` — deep readonly + LITERAL types
// url is typed 'x', not string
```

#### Easy memory

```
`as` = "trust me" → NO runtime conversion, NO check ⭐
JSON.parse(x) as User is a PROMISE, not a validation 💥
Prefer a type GUARD over an assertion ⭐
`as const` → literal types + deep readonly
```

---

### Q: What is the non-null assertion operator (`!`)?

```
value!  = "I promise this is not null or undefined."
          Again: NO runtime check. ⭐
```

```typescript
const el = document.getElementById('x')!;      // "definitely exists"
el.focus();                                     // 💥 if it doesn't, crash at runtime

@ViewChild('input') input!: ElementRef;         // ⭐ "Angular WILL assign this"
```

```
!  → silences strictNullChecks
?. → SAFELY returns undefined instead ⭐
```

```typescript
user!.address.city      // 💥 crashes if user is null
user?.address?.city     // ✅ undefined, no crash ⭐
user?.address?.city ?? 'Unknown'   // ✅ with a fallback ⭐
```

⭐ The one legitimate use is **definite assignment** — `@ViewChild`, `@Input`, and fields Angular fills in later. Everywhere else, prefer `?.` and `??`.

#### Easy memory

```
!  = "trust me, not null" → NO runtime check ⭐
?. = optional chaining → returns undefined safely ⭐
?? = nullish coalescing → a fallback for null/undefined only

Legit use of !: @ViewChild / @Input definite assignment ⭐
Everywhere else: ?. and ??
```

---

### Q: What are index signatures?

```
An index signature says: "this object can have ANY key of this type,
                          and every value is of that type." ⭐
```

```typescript
interface StringMap { [key: string]: string; }

const headers: StringMap = { 'Content-Type': 'application/json' };
headers['Authorization'] = 'Bearer x';          // ✅ any string key

// mixing a known key with an index signature
interface Config {
  name: string;
  [key: string]: string | number;   // ⚠️ every declared property must FIT this type ⭐
}
```

⚠️ The trade-off:

```
Index signature → flexible, but you LOSE key checking:
   headers['Contnet-Type']     ← a typo compiles fine 💥

Record<'a'|'b', string> → only the known keys ✅ + autocomplete ⭐
```

⭐ The rule: use an index signature only when the keys are **genuinely unknown at compile time** (HTTP headers, a translation dictionary). Otherwise prefer `Record` with a literal union.

#### Easy memory

```
[key: string]: T = ANY key of that type ⭐
⚠️ You lose key checking — typos compile 💥
Known keys? → Record<'a'|'b', T> instead ⭐
Every declared property must fit the index signature's value type ⚠️
```

---

## Decorators, Declaration Files, Modules

### Q: What are decorators? How does Angular use them?

```
A decorator is a FUNCTION that attaches METADATA to a class,
method, property or parameter. ⭐
```

```typescript
@Component({ selector: 'app-x', template: '...' })   // class decorator
export class XComponent {
  @Input() data!: string;                             // property decorator
  @Output() changed = new EventEmitter<string>();
  @ViewChild('ref') ref!: ElementRef;
  @HostListener('click') onClick() {}                 // method decorator

  constructor(@Inject(TOKEN) private cfg: Config) {}  // parameter decorator
}
```

```
The decorator STORES metadata that Angular reads at runtime
   → "this class is a component, its selector is app-x,
      its template is …, and `data` is an input" ⭐
```

Enabling them:

```json
"experimentalDecorators": true      // ⭐ required by Angular
"emitDecoratorMetadata": true       // emits type info for DI
```

⚠️ Angular's decorators are the **legacy/experimental** proposal, not the TC39 Stage-3 decorators standardised in TypeScript 5. Knowing that distinction is a good detail.

Writing your own:

```typescript
function LogTime(target: any, key: string, descriptor: PropertyDescriptor) {
  const original = descriptor.value;
  descriptor.value = function (...args: any[]) {
    const t = performance.now();
    const r = original.apply(this, args);
    console.log(`${key} took ${performance.now() - t}ms`);
    return r;
  };
  return descriptor;
}
```

⭐ That is exactly the **Decorator pattern** from [17 — Design Patterns](./17-solid-design-patterns.md) — wrap and delegate.

#### Easy memory

```
Decorator = a function attaching METADATA ⭐
@Component @Directive @Pipe @Injectable @NgModule | @Input @Output @ViewChild @HostListener
Needs experimentalDecorators: true ⭐
Angular uses the LEGACY proposal, not TS 5 standard decorators ⚠️
Writing one = the Decorator PATTERN (wrap + delegate) ⭐
```

---

### Q: What are declaration files (`.d.ts`)?

```
A .d.ts file contains TYPES ONLY — no implementation.
It teaches TypeScript about JavaScript code. ⭐
```

```typescript
// my-lib.d.ts
declare module 'my-lib' {
  export function greet(name: string): string;
}

// globals
declare const APP_VERSION: string;
declare global {
  interface Window { dataLayer: any[]; }     // ⭐ typing a global you didn't create
}
```

```
A JS library with no types → every import is `any` 💥
      ↓
npm install --save-dev @types/lodash        ← community types from DefinitelyTyped ⭐
      ↓
full IntelliSense and checking ✅
```

⭐ Three facts worth stating:

```
1. @types/* packages come from DefinitelyTyped ⭐
2. Modern libraries SHIP their own .d.ts ("types" field in package.json)
3. Angular generates .d.ts when you build a library, so consumers get types ⭐
```

#### Easy memory

```
.d.ts = types WITHOUT implementation ⭐ → teaches TS about JS
npm i -D @types/lodash (DefinitelyTyped) ⭐
declare global { interface Window { … } } → type a global ⭐
```

---

### Q: Namespaces vs modules?

```
NAMESPACE → the OLD way (an internal object wrapper) → avoid in new code ⚠️
MODULE    → the STANDARD way (one file = one module, import/export) ⭐
```

```typescript
// ❌ namespace — legacy
namespace Utils {
  export function format(d: Date): string { return d.toISOString(); }
}
Utils.format(new Date());

// ✅ module — standard ES modules ⭐
// utils.ts
export function format(d: Date): string { return d.toISOString(); }
// consumer.ts
import { format } from './utils';
```

```
Namespace → one global object, NOT tree-shakable 💥
Module    → per-file scope, TREE-SHAKABLE ⭐, works with bundlers
```

⭐ **Tree-shaking is the deciding argument:** a bundler can remove an unused module export, but it cannot safely remove a property from a namespace object.

#### Easy memory

```
Namespace = legacy internal module ⚠️ NOT tree-shakable
Module ⭐ = one file, import/export, tree-shakable, standard ES

Angular uses MODULES everywhere. Namespaces only appear in old .d.ts files.
```

---

## Practical / Angular

### Q: How does TypeScript specifically help in Angular development?

Answer it **feature by feature** — a generic "types are good" answer scores nothing:

```
1. DECORATORS — @Component/@Injectable are TypeScript decorators ⭐
2. DI by TYPE — constructor(private http: HttpClient) works because the TYPE
                is emitted as metadata ⭐⭐
3. Typed HTTP — http.get<User[]>() types the whole chain
4. Typed reactive forms (v14+) — form.value is typed, not `any` ⭐
5. strictTemplates — type errors in the TEMPLATE become build errors ⭐
6. Safe refactoring — rename a model field and every template + class is found ⭐
7. Interfaces as API contracts between frontend and backend
```

⭐ Point 2 is the one that impresses:

```
constructor(private http: HttpClient) {}

Angular needs to know WHICH class to inject.
`emitDecoratorMetadata` writes the parameter's TYPE into the compiled output.
      ↓
Without TypeScript, Angular DI would need explicit @Inject() everywhere ⭐
```

That is also exactly why an **interface can't be a DI token** — it's erased, so there's no metadata to emit.

#### 🔵 In your RoboGebra code

```
strictTemplates: true ✅   → template type errors caught at build
strictNullChecks: false ⚠️ → the legacy gap (see the tsconfig question above)
89 services, 163 components, all with typed constructor DI
```

#### Easy memory

```
Decorators | DI by TYPE ⭐⭐ (emitDecoratorMetadata) | typed HttpClient
typed forms (v14+) | strictTemplates ⭐ | safe refactoring | API contracts

"Interfaces are erased → which is exactly why they can't be DI tokens." ⭐
```

---

### Q: What are common TypeScript compiler errors and how do you fix them?

```
"Object is possibly 'null'"                    → ?. or a guard, or ! if genuinely safe
"Property 'x' does not exist on type 'y'"      → wrong type, or narrow it first
"Type 'string' is not assignable to 'number'"  → a real bug — fix the type, don't cast
"Cannot find module 'x'"                       → npm i -D @types/x, or a .d.ts
"Parameter implicitly has an 'any' type"       → annotate it (noImplicitAny)
"Property has no initializer"                  → use ! (definite assignment) or a default
"Argument of type 'X | undefined'..."          → narrow it, or ?? a fallback
```

⭐ The habit to describe:

```
An error is the compiler DOING ITS JOB. ⭐
The fix is almost never `as any` — that just moves the crash to runtime.
```

```typescript
// ❌ the reflex that hides a real bug
const x = something as any;

// ✅ narrow, guard, or fix the model ⭐
if (something) { … }
```

#### Easy memory

```
"possibly null" → ?. / a guard | "does not exist" → narrow it
"not assignable" → a REAL bug, fix the type ⭐
"cannot find module" → @types/x
"no initializer" → ! or a default value

⚠️ `as any` doesn't fix an error — it MOVES it to runtime ⭐
```

---

### Q: How do you type an HTTP response and handle optional API fields?

```typescript
export interface User {
  id: number;
  name: string;
  email?: string;             // ⭐ optional — the API may omit it
  address?: { city?: string };
}

// typed request
getUser(id: number): Observable<User> {
  return this.http.get<User>(`/api/users/${id}`);       // ⭐ typed generic
}

// safe consumption ⭐
const city = user.address?.city ?? 'Unknown';
```

⚠️ **The honest caveat every interviewer likes to hear:**

```
http.get<User>() does NOT validate anything at runtime. ⭐

It's a PROMISE to the compiler. If the backend changes a field,
TypeScript will happily hand you an object that doesn't match. 💥
```

Options, in order of rigour:

```
1. Optional fields (?) + ?. + ??            ← the pragmatic default ⭐
2. A mapper/factory that builds the model   ← your PageResponse.fromJSON pattern ⭐
3. A runtime validator (zod, io-ts)          ← real validation at the boundary
```

#### 🔵 In your RoboGebra code — option 2, done properly ⭐

```typescript
static fromJSON<U>(data: any, itemMapper: (item: any) => U): PageResponse<U> {
    const content = (data.content || []).map(itemMapper);       // ⭐ defensive default
    return new PageResponse<U>(
        content,
        data.totalElements ?? 0,                                 // ⭐ ?? fallbacks
        data.totalPages ?? 0,
        data.size ?? 0,
        data.number ?? 0
    );
}
```

> 🗣️ *"We don't trust the raw JSON. Every paginated response goes through `PageResponse.fromJSON`, which takes a mapper for the item type and applies `?? 0` defaults for the paging fields. So a missing or renamed field degrades to a safe default at one boundary, instead of throwing somewhere deep in a template. That's the practical middle ground between blindly casting with `as` and pulling in a full runtime validator."*

⭐ **That is a very strong answer** — it shows you know `as` is a lie *and* you did something pragmatic about it.

#### Easy memory

```
http.get<User>(url) → typed, but NO runtime validation ⭐
Optional fields: email?: string → use ?. and ?? ⭐

⚠️ `as User` is a PROMISE, not a check 💥
Your pattern: a fromJSON MAPPER with ?? defaults at the boundary ⭐
Stricter: zod / io-ts for real runtime validation
```

---

## Quick Revision Sheet

```
BASICS
  TS = JS + types at COMPILE time; types are ERASED ⭐ (no runtime checking)
  strict ⭐ | strictNullChecks ⭐ (your app: OFF — be honest, give the migration plan)
  interface → object shapes + MERGEABLE ⭐ | type → unions/tuples ⭐
  any (no checking ⚠️) | unknown (must narrow ✅⭐) | void | never (+ exhaustiveness ⭐)
  let → general type | const → LITERAL type ⭐
  Union | = OR (narrow it) | Intersection & = AND
  Tuple = fixed length + type per position
  ⚠️ NUMERIC enums are positional → inserting a value shifts meanings 💥 → use strings ⭐

FUNCTIONS
  name?: T → `T | undefined` | name = x → default | optional comes LAST
  ?? (null/undefined) ⭐ vs || (also "" 0 false) 💥
  ...args = REST in a signature, SPREAD at a call site
  Overloads = many signatures, ONE implementation (compile-time only) ⭐

OOP
  constructor(private http: HttpClient) ⭐ = parameter property → Angular DI in one line
  ⚠️ TS `private` is compile-time only → (x as any).field reaches it 💥 | JS #field is real ⭐
  readonly locks the REFERENCE, not the contents
  interface ERASED ⭐ → no instanceof, NOT a DI token → hence InjectionToken ⭐
  abstract class EXISTS at runtime → CAN be a DI token ⭐
  extends = inherit code (ONE) | implements = promise a shape (MANY)

ADVANCED
  Generics <T> = one implementation, many types, type-safe ⭐ (PageResponse<T>)
  <T, K extends keyof T>(obj: T, key: K): T[K] ⭐ → typo = compile error
  Partial ⭐ | Pick | Omit ⭐ (drop the password) | Record<K,V> ⭐ | Readonly
  Mapped type = a loop over keys ⭐ | Conditional = T extends U ? X : Y + infer ⭐
  Type guards: typeof | instanceof | in | `x is T` ⭐ | DISCRIMINATED UNION ⭐⭐
  typeof → value→type ⭐ | keyof → type→keys ⭐
  `as` and `!` do NO runtime check ⭐ — prefer a guard, ?. and ??

ANGULAR
  DI works because emitDecoratorMetadata writes the parameter TYPE ⭐⭐
  strictTemplates catches template type errors at BUILD time ⭐
  http.get<User>() types the chain but validates NOTHING at runtime ⭐
  → your fromJSON mapper with ?? defaults is the pragmatic fix ⭐
```

---

**Related files:** [04 — Angular](./04-angular.md) · [01 — JavaScript](./01-javascript.md) · [33 — Interface vs Abstract Class](./33-interface-vs-abstract-class.md) · [17 — SOLID & Design Patterns](./17-solid-design-patterns.md) · [39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)
