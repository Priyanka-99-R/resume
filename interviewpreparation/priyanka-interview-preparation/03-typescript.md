# TypeScript — Interview Q&A

> Lateral interview prep (4+ years, Angular developer). Focused, practical answers with TypeScript code examples.

---

## Basics

### Q: What is TypeScript and what are its advantages over JavaScript?

TypeScript is a strongly-typed **superset of JavaScript** developed by Microsoft. Any valid JS is valid TS. It adds static typing, interfaces, generics, enums, decorators and modern features that compile down to plain JavaScript.

Advantages:
- **Static type checking** catches errors at compile time, not runtime.
- **Better tooling** — autocomplete, refactoring, inline docs (IntelliSense).
- **Self-documenting** code via types and interfaces.
- **Safer refactoring** in large codebases (Angular apps especially).
- Supports modern ES features and **decorators** that Angular relies on.

```ts
function add(a: number, b: number): number {
  return a + b;
}
add(2, 3);       // OK
// add(2, "3");  // Compile-time error
```

### Q: How does TypeScript compile / how does it run?

TypeScript code (`.ts`) is **transpiled** to JavaScript (`.js`) by the TypeScript compiler (`tsc`), because browsers and Node only understand JS. Types are **erased** during compilation — they exist only at compile time and have no runtime cost. In Angular, the CLI (`ng build`) runs this compilation under the hood.

```bash
tsc app.ts        # produces app.js
tsc --watch       # recompile on change
```

### Q: What are the key tsconfig.json options you should know?

`tsconfig.json` configures the compiler. Important options:

```jsonc
{
  "compilerOptions": {
    "target": "ES2022",          // JS version to output
    "module": "ESNext",          // module system
    "strict": true,              // enables all strict type checks
    "noImplicitAny": true,       // error on implicit 'any'
    "strictNullChecks": true,    // null/undefined must be handled
    "outDir": "./dist",          // output folder
    "sourceMap": true,           // for debugging
    "experimentalDecorators": true // required by Angular
  },
  "include": ["src/**/*.ts"],
  "exclude": ["node_modules"]
}
```

`strict: true` is the most important — it turns on a family of checks (`strictNullChecks`, `noImplicitAny`, etc.).

### Q: What is the difference between `type` and `interface`?

Both describe the shape of an object. Differences:

- **interface** can be **re-opened / merged** (declaration merging); `type` cannot.
- **interface** is primarily for object shapes; **type** can alias **any** type — unions, primitives, tuples, etc.
- Both support extension (`extends` for interface, `&` for type).

```ts
interface User { name: string; }
interface User { age: number; }   // merged → { name, age }

type ID = string | number;        // union — only 'type' can do this
type Point = { x: number; y: number };
```

Rule of thumb: use `interface` for object/class contracts, `type` for unions and complex compositions.

### Q: Explain `any` vs `unknown` vs `never` vs `void`.

- **any** — opts out of type checking entirely (avoid it).
- **unknown** — type-safe `any`; you must narrow it before use.
- **never** — represents values that never occur (function that always throws or never returns).
- **void** — function returns nothing meaningful.

```ts
let a: any = 5;       a.foo();          // allowed (unsafe)
let u: unknown = 5;   // u.foo();       // Error until narrowed
if (typeof u === "string") u.trim();    // OK after narrowing

function fail(): never { throw new Error("x"); }
function log(): void { console.log("hi"); }
```

### Q: What is type inference?

TypeScript automatically **infers** types when you don't annotate them, based on the assigned value. This reduces boilerplate while keeping safety.

```ts
let count = 10;        // inferred as number
// count = "ten";      // Error
const names = ["a"];   // inferred string[]
```

You only need explicit annotations where inference can't help (function params, ambiguous cases).

### Q: What are literal types?

Literal types restrict a value to **specific exact values**, not just a broad type. Often combined with unions.

```ts
let dir: "left" | "right";
dir = "left";          // OK
// dir = "up";         // Error

type Status = 200 | 404 | 500;
```

Useful for Angular component inputs with a fixed set of allowed values.

### Q: What are union and intersection types?

- **Union (`|`)** — value can be **one of** several types.
- **Intersection (`&`)** — value must satisfy **all** types combined.

```ts
type Id = string | number;          // union

interface A { a: number; }
interface B { b: string; }
type AB = A & B;                    // intersection → { a, b }
const ab: AB = { a: 1, b: "x" };
```

### Q: What are tuples?

A tuple is a fixed-length array with a **known type at each position**.

```ts
let pair: [string, number] = ["age", 30];
pair[0].toUpperCase();   // string
pair[1].toFixed();       // number

// useState-style return
function useToggle(): [boolean, () => void] {
  return [true, () => {}];
}
```

### Q: What are enums?

Enums define a set of named constants. Numeric by default; can be string enums (preferred for readability/debugging).

```ts
enum Direction { Up, Down }       // 0, 1
enum Role { Admin = "ADMIN", User = "USER" }

let r: Role = Role.Admin;         // "ADMIN"
```

`const enum` inlines values at compile time for performance. Many teams use union literal types instead of enums to avoid extra generated code.

---

## Functions

### Q: How do you type functions in TypeScript?

You annotate parameters and the return type. You can also type a function variable using a function-type signature.

```ts
function greet(name: string): string {
  return `Hi ${name}`;
}

// function type
const multiply: (a: number, b: number) => number = (a, b) => a * b;
```

### Q: How do optional and default parameters work?

- **Optional** params use `?` and may be `undefined`.
- **Default** params provide a fallback value (and become optional).

```ts
function build(name: string, age?: number) {        // optional
  return age ? `${name} ${age}` : name;
}

function greet(name: string, greeting = "Hello") {  // default
  return `${greeting} ${name}`;
}
```

Optional params must come **after** required ones.

### Q: What are rest parameters?

Rest params collect remaining arguments into a typed array.

```ts
function sum(...nums: number[]): number {
  return nums.reduce((a, b) => a + b, 0);
}
sum(1, 2, 3);   // 6
```

### Q: What is function overloading?

Multiple function signatures for the same function name, with one implementation that handles all cases. Useful when return type depends on input type.

```ts
function parse(input: string): string[];
function parse(input: number): number;
function parse(input: string | number): string[] | number {
  return typeof input === "string" ? input.split("") : input * 2;
}

parse("ab");   // string[]
parse(5);      // number
```

---

## OOP

### Q: How do you define a class in TypeScript?

```ts
class User {
  name: string;
  constructor(name: string) {
    this.name = name;
  }
  greet(): string {
    return `Hi ${this.name}`;
  }
}
```

TypeScript adds access modifiers, parameter properties, abstract members and more on top of ES classes.

### Q: Explain the access modifiers: public, private, protected, readonly.

- **public** (default) — accessible everywhere.
- **private** — only within the class.
- **protected** — within the class and its subclasses.
- **readonly** — can be set only in declaration/constructor, then immutable.

```ts
class Account {
  public id: number;
  private pin: number;
  protected balance: number;
  readonly bank = "ABC";

  // parameter properties shorthand
  constructor(id: number, pin: number, balance: number) {
    this.id = id; this.pin = pin; this.balance = balance;
  }
}
```

Angular uses this for DI: `constructor(private http: HttpClient) {}` declares and assigns in one line.

### Q: What are abstract classes?

A base class that **cannot be instantiated** directly and can define abstract members that subclasses **must implement**. It can also contain shared concrete logic.

```ts
abstract class Shape {
  abstract area(): number;        // must be implemented
  describe() { return `Area: ${this.area()}`; }  // shared
}

class Circle extends Shape {
  constructor(private r: number) { super(); }
  area() { return Math.PI * this.r ** 2; }
}
```

### Q: Interface vs abstract class — when to use which?

| Interface | Abstract class |
|-----------|----------------|
| Only declarations, no implementation | Can have implemented methods + state |
| A class can implement **many** | A class extends **one** |
| Erased at compile time (no JS output) | Generates real JS |
| Pure contract | Shared base behavior |

Use an interface for a contract; use an abstract class when you want to share code among subclasses.

### Q: Difference between `implements` and `extends`?

- **extends** — inherit from a class (or interface from interface). Brings implementation.
- **implements** — promise to satisfy an interface's contract; no implementation inherited.

```ts
interface Logger { log(msg: string): void; }

class Base { id = 1; }
class Service extends Base implements Logger {
  log(msg: string) { console.log(msg); }
}
```

### Q: How do getters and setters work?

Accessors let you run logic when reading/writing a property using normal property syntax.

```ts
class Temp {
  private _c = 0;
  get celsius() { return this._c; }
  set celsius(v: number) {
    if (v < -273) throw new Error("too low");
    this._c = v;
  }
}
const t = new Temp();
t.celsius = 25;       // calls setter
console.log(t.celsius); // calls getter
```

### Q: What are static members?

Static properties/methods belong to the **class itself**, not instances. Accessed via the class name.

```ts
class MathUtil {
  static PI = 3.14;
  static square(n: number) { return n * n; }
}
MathUtil.square(4);   // 16, no instance needed
```

---

## Advanced Types

### Q: What are generics? Give an example.

Generics let you write **reusable, type-safe** code that works over multiple types while preserving the specific type. A type parameter (`<T>`) acts as a placeholder.

```ts
function identity<T>(value: T): T {
  return value;
}
identity<string>("hi");   // T = string
identity(10);             // T inferred as number

// generic class
class Box<T> {
  constructor(public content: T) {}
}
const b = new Box<number>(5);
```

Angular's `Observable<User>` and `HttpClient.get<User>()` are generics in action.

### Q: What are generic constraints?

Restrict a generic type to types that have certain properties, using `extends`.

```ts
function getLength<T extends { length: number }>(item: T): number {
  return item.length;
}
getLength("abc");      // OK
getLength([1, 2]);     // OK
// getLength(5);       // Error: number has no length

// keyof constraint
function getProp<T, K extends keyof T>(obj: T, key: K): T[K] {
  return obj[key];
}
```

### Q: Explain the common utility types: Partial, Required, Pick, Omit, Record, Readonly.

```ts
interface User { id: number; name: string; email: string; }

Partial<User>;   // all props optional → { id?, name?, email? }
Required<User>;  // all props required
Pick<User, "id" | "name">;   // { id, name }
Omit<User, "email">;         // { id, name }
Readonly<User>;  // all props immutable
Record<string, number>;      // { [key: string]: number }
```

- **Partial** — great for update/patch payloads.
- **Pick / Omit** — derive a smaller type.
- **Record** — build a map/dictionary type.
- **Readonly** — immutable copy.

### Q: What are mapped types?

They create new types by **transforming each property** of an existing type. This is how utility types like `Partial` are built.

```ts
type Optional<T> = { [K in keyof T]?: T[K] };
type Stringify<T> = { [K in keyof T]: string };

interface User { id: number; name: string; }
type UserStrings = Stringify<User>;   // { id: string; name: string }
```

### Q: What are conditional types?

Types that choose between two types based on a condition, using the ternary-like `T extends U ? X : Y`.

```ts
type IsString<T> = T extends string ? "yes" : "no";
type A = IsString<string>;   // "yes"
type B = IsString<number>;   // "no"

// extract non-null
type NonNullableX<T> = T extends null | undefined ? never : T;
```

### Q: What are type guards?

Runtime checks that **narrow** a type within a block. Built-in guards: `typeof`, `instanceof`, `in`. You can also write custom guards with a type predicate (`x is Type`).

```ts
function isString(x: unknown): x is string {
  return typeof x === "string";
}

function print(val: string | number) {
  if (isString(val)) val.toUpperCase();  // narrowed to string
  else val.toFixed(2);                    // number
}
```

### Q: Explain `typeof`, `keyof`, and `in`.

- **typeof** — get the type of a variable/value.
- **keyof** — get a union of an object type's keys.
- **in** — iterate keys in mapped types / check property existence at runtime.

```ts
const config = { url: "x", retries: 3 };
type Config = typeof config;        // { url: string; retries: number }
type Keys = keyof Config;           // "url" | "retries"

type Flags = { [K in Keys]: boolean };  // 'in' in a mapped type

if ("retries" in config) {}         // runtime 'in'
```

### Q: What is type assertion (`as`)?

Tells the compiler to treat a value as a specific type when **you** know more than it does. It does **no** runtime conversion.

```ts
const el = document.getElementById("app") as HTMLInputElement;
el.value = "hello";

const data = JSON.parse(str) as User;
```

Avoid overusing it — it bypasses type safety. Prefer type guards where possible.

### Q: What is the non-null assertion operator (`!`)?

`!` tells the compiler a value is **not null/undefined**, even though the type says it could be. Use only when you're certain.

```ts
function getName(user?: { name: string }) {
  return user!.name;   // assert user is defined
}

@ViewChild('ref') ref!: ElementRef;  // common in Angular
```

The `ref!: ElementRef` form is "definite assignment assertion" — promises it will be assigned later.

### Q: What are index signatures?

Allow objects with **dynamic keys** of a known type when you don't know the property names ahead of time.

```ts
interface StringMap {
  [key: string]: string;
}
const headers: StringMap = { "Content-Type": "application/json" };
headers["Authorization"] = "Bearer ...";
```

---

## Decorators, Declaration Files, Modules

### Q: What are decorators? How does Angular use them?

Decorators are special functions prefixed with `@` that **add metadata or modify** classes, methods, properties or parameters. They require `experimentalDecorators` in tsconfig. Angular is built around them:

```ts
@Component({               // class decorator
  selector: 'app-user',
  template: '...'
})
export class UserComponent {
  @Input() name!: string;      // property decorator
  @Output() saved = new EventEmitter();
  @ViewChild('ref') ref!: ElementRef;

  constructor(@Inject(TOKEN) private cfg: Config) {}  // parameter decorator
}

@Injectable({ providedIn: 'root' })   // marks a service for DI
export class UserService {}
```

The decorator metadata tells Angular how to instantiate and wire up the class.

### Q: What are declaration files (`.d.ts`)?

`.d.ts` files contain **only type declarations** (no implementation). They describe the shape of JavaScript libraries so TypeScript can type-check usage of plain JS packages. Many libs ship their own, or you install `@types/...` from DefinitelyTyped.

```ts
// types.d.ts
declare module "my-js-lib" {
  export function doThing(x: number): string;
}

declare const VERSION: string;   // ambient declaration
```

```bash
npm i -D @types/lodash   # community type definitions
```

### Q: Namespaces vs modules — what's the difference?

- **Modules** (ES Modules) — file-based; use `import`/`export`. This is the **standard, recommended** approach and what Angular uses.
- **Namespaces** — an older TS-internal way to group code under a single global name (`namespace X {}`). Mostly legacy; avoid in modern apps.

```ts
// module (preferred)
export class UserService {}
import { UserService } from './user.service';

// namespace (legacy)
namespace Utils {
  export function format() {}
}
Utils.format();
```

---

## Practical / Angular

### Q: How does TypeScript specifically help in Angular development?

- **Strongly typed DI** — constructor injection with typed services.
- **Typed HTTP** — `http.get<User[]>(url)` gives typed responses.
- **Typed Inputs/Outputs** and template type checking (with strict templates).
- **Interfaces/models** for API data ensure consistency across components.
- **Generics** in RxJS (`Observable<T>`, `BehaviorSubject<T>`).
- **Decorators** enable the entire component/service/module architecture.
- **Refactoring safety** across large enterprise apps — renames and signature changes are caught at compile time.

```ts
interface User { id: number; name: string; }

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private http: HttpClient) {}
  getUsers(): Observable<User[]> {
    return this.http.get<User[]>('/api/users');
  }
}
```

### Q: What are common TypeScript compiler errors and how do you fix them?

**1. `Object is possibly 'null' or 'undefined'`** (strictNullChecks)
```ts
const el = document.querySelector('.x');
// el.textContent = 'hi';        // Error
el?.textContent;                 // fix: optional chaining
if (el) el.textContent = 'hi';   // or narrow
```

**2. `Type 'string' is not assignable to type 'number'`** — wrong type assigned; fix the value or the declared type.

**3. `Parameter 'x' implicitly has an 'any' type`** (noImplicitAny) — add an explicit type:
```ts
function f(x: number) {}
```

**4. `Property 'foo' does not exist on type ...`** — typo, missing interface field, or needs a type assertion/guard.

**5. `Cannot find module '...' or its type declarations`** — install `@types/...` or add a `.d.ts` declaration.

**6. `Property has no initializer and is not definitely assigned`** (strictPropertyInitialization) — use the definite assignment `!`:
```ts
@Input() name!: string;
```

### Q: How do you type an HTTP response and handle optional API fields?

Define an interface mirroring the API and mark optional fields with `?`. Use `Partial` for patch payloads.

```ts
interface Product {
  id: number;
  name: string;
  description?: string;   // optional
}

updateProduct(id: number, changes: Partial<Product>) {
  return this.http.patch<Product>(`/api/products/${id}`, changes);
}
```

This keeps components, services and templates aligned on a single source of truth for the data shape.
