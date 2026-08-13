# 🔴 Codeboard Technology — "Java with Angular Ionic — Level 1 — Virtual Interview"
## ⚠️ SINGLE ROUND → ONBOARDING. This one call decides everything.

> **Candidate:** Priyanka R · 5 years · Full Stack (Angular + Ionic + Java/Spring Boot)
> **Round:** Level 1 — Virtual — **the only round.** HR confirmed: clear this → onboarding process.
> **Company:** Codeboard Technology Pvt Ltd, Chennai (Alandur) — IT services / staffing, hires Java and Angular developers, often for client projects.

---

## 0. 🔴 WHAT THEY ACTUALLY ASKED — the real question list (with answers)

> **This round has now happened.** These are the questions Codeboard actually asked. Everything below is answered in full — **revise this section first**, because the same interviewer and the same question bank get reused, and these topics are the ones this company cares about.
>
> **The Java round was output-based, not theory** — that's the lesson that cost the round. Full drill set in **[23 — Java "Predict the Output"](./23-java-output-tricky-questions.md)**.

| # | Asked | Type | Where it's covered in depth |
|---|---|---|---|
| 1 | Abstract class | Java theory | [05](./05-java.md) · [17](./17-solid-design-patterns.md) |
| 2 | Data hiding | Java theory | [05](./05-java.md) |
| 3 | `methodTest(null)` — overloading with `Object` vs `String` | **Output** | [23 — Q1](./23-java-output-tricky-questions.md) |
| 4 | Override that adds `throws IOException` | **Output** | [23 — Q2](./23-java-output-tricky-questions.md) |
| 5 | Ionic lifecycle hooks | Ionic | [15 — Q24](./15-ionic-level1.md) |
| 6 | Difference between Angular and Ionic lifecycle hooks | Ionic | [15 — Q24](./15-ionic-level1.md) |
| 7 | How do you handle offline support in Ionic | Ionic / design | below |

---

### Q1. What is an abstract class?

A class declared with the `abstract` keyword that **cannot be instantiated** and is meant to be extended. It can hold **both** abstract methods (no body, subclasses must implement them) and concrete methods with shared logic, plus constructors, instance fields and any access modifier.

```java
abstract class Shape {
    private final String name;               // state — an interface can't have this
    Shape(String name) { this.name = name; } // constructors are allowed

    abstract double area();                  // subclass MUST implement

    void describe() {                        // shared concrete behaviour
        System.out.println(name + " area = " + area());
    }
}

class Circle extends Shape {
    private final double r;
    Circle(double r) { super("Circle"); this.r = r; }
    @Override double area() { return Math.PI * r * r; }
}

// new Shape("x");   ❌ compile error — Shape is abstract
Shape s = new Circle(2);   // ✅ reference of abstract type, object of the concrete subclass
```

**Rules worth stating:**
- An abstract class **need not** have any abstract method — `abstract` alone is enough to block instantiation.
- A class with **even one** abstract method **must** be declared abstract.
- `abstract` cannot combine with `final` (nothing could extend it), `static`, or `private` (nothing could override it).
- A subclass that doesn't implement all inherited abstract methods must itself be abstract.

### 🔁 The guaranteed follow-up: abstract class vs interface

| | **Abstract class** | **Interface** |
|---|---|---|
| Inheritance | `extends` — **only one** | `implements` — **many** ✅ |
| Fields | Any instance state, any modifier | `public static final` constants only |
| Constructors | ✅ Yes | ❌ No |
| Method access | public / protected / default / private | public (plus `default`, `static`, `private` since Java 8/9) |
| Purpose | **"is-a"** — shared identity + shared code | **"can-do"** — a capability contract |
| Since Java 8 | — | `default` and `static` methods allow behaviour too |

> 💬 **Say this:** *"I use an abstract class when subclasses genuinely share state and partial implementation — a base entity or a template-method flow. I use an interface to define a capability that unrelated classes can implement, and because a class can implement many interfaces but extend only one class. Since Java 8, interfaces can carry `default` methods, so the real distinguisher isn't 'code vs no code' any more — it's **state and identity**: only an abstract class can hold instance fields and a constructor."*

---

### Q2. What is data hiding?

**Data hiding** is restricting direct access to an object's internal state so it can only be changed through controlled methods. In Java you do it by declaring fields **`private`** and exposing `public` getters/setters that enforce the rules.

```java
public class BankAccount {
    private double balance;                  // hidden — nobody can touch it directly

    public double getBalance() { return balance; }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive");
        balance += amount;                   // the invariant is enforced here
    }
}

// account.balance = -5000;   ❌ compile error — that's the whole point
```

**Why it matters:** the object protects its own invariants. Without it, any code anywhere can put the object into an invalid state, and you can never change the internal representation without breaking callers.

### 🔁 Follow-up: data hiding vs encapsulation vs abstraction

| Term | What it means | How |
|---|---|---|
| **Data hiding** | Restricting **access** to internal state | `private` fields |
| **Encapsulation** | **Bundling** data + the methods that operate on it into one unit, and hiding the internals | `private` fields + `public` methods |
| **Abstraction** | Hiding **complexity** — showing *what* an object does, not *how* | `abstract` classes / interfaces |

> 💬 **The one-liner:** *"Data hiding is the mechanism — `private` fields. Encapsulation is the design principle that bundles state with the behaviour that guards it. Abstraction hides complexity behind a contract. Data hiding is *how* you achieve encapsulation; abstraction is a different axis — it hides implementation, not data."*

### 🔁 Follow-up: access modifiers (have this table cold)

| Modifier | Same class | Same package | Subclass (other package) | Anywhere |
|---|---|---|---|---|
| `private` | ✅ | ❌ | ❌ | ❌ |
| *default* (no modifier) | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

---

### Q3. Output — `methodTest(null)` 🔴

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

### 📏 The rule
**Overloading is resolved at compile time, and the compiler picks the *most specific* applicable overload.** `null` is assignable to both `Object` and `String`, but `String` **is-a** `Object`, so `String` is more specific and wins.

### 🔁 Follow-ups they ask next
- *"What if you add `methodTest(Integer)`?"* → **Compile error: reference to methodTest is ambiguous.** `String` and `Integer` are siblings — neither is more specific.
- *"How do you force the `Object` version?"* → Cast it: `moe.methodTest((Object) null);`
- *"What if only `methodTest(Object)` existed?"* → `Calling object method` — one applicable method, no ambiguity.
- *"Overloading vs overriding?"* → **"Overloading is decided by the compiler from the reference type; overriding is decided by the JVM from the object type."**

---

### Q4. Output — override that adds `throws IOException` 🔴

```java
class SuperClass {
    void method() {
        System.out.println("SuperClass");
    }
}

class SubClass extends SuperClass {
    void method() throws IOException {        // ⚠️ here
        System.out.println("SubClass");
    }

    public static void main(String args[]) {
        SuperClass s = new SubClass();
        s.method();
    }
}
```

### ✅ Output
**Compile error** — *"overridden method does not throw java.io.IOException"*.

### 📏 The rule
**An overriding method cannot throw a broader or new *checked* exception than the method it overrides.** It may throw fewer, narrower, or none — and it may throw **any unchecked** exception freely. The parent's `method()` declares no checked exceptions, so the child cannot introduce `IOException`.

**Why the language does this:** callers are checked against the **reference type**. `SuperClass s` promises `method()` throws nothing; if the child could add a checked exception, that promise would be broken at runtime.

### 🔁 Follow-ups
- *"Make it compile — three ways."* → add `throws IOException` to the **parent**; or remove it from the child; or throw an **unchecked** exception (`RuntimeException`/`IOError`) instead.
- *"What if the parent declared `throws Exception` and the child `throws IOException`?"* → ✅ **Legal** — narrowing is allowed. Output: `SubClass`.
- *"And if the parent declares `throws Exception` and the child declares nothing, then you call `s.method()`?"* → **Compile error at the call site** — `unreported exception Exception` — because the *reference type* `SuperClass` declares it. Wrap in try/catch and it prints `SubClass`.
- *"Which method actually runs?"* → the child's, by dynamic dispatch — **if** it compiles.

> ⚠️ **Two more trip-wires in this exact program:** `IOException` needs `import java.io.IOException;`, and the version you were shown had `new SubClass()` written as `newSubClass()` — **always check whether it even compiles before you trace the output.** *(That's the golden habit at the top of [23](./23-java-output-tricky-questions.md).)*

---

### Q5 & Q6. Ionic lifecycle hooks — and how they differ from Angular's 🔴

**Both fire.** An Ionic page *is* an Angular component, so all Angular hooks run; Ionic **adds four more** that are driven by the **navigation stack**.

| Ionic hook | When it fires |
|---|---|
| `ionViewWillEnter` | Just **before** the page becomes active — **every time** you enter it |
| `ionViewDidEnter` | After the enter transition finishes — the page is visible and interactive |
| `ionViewWillLeave` | Just before you navigate away |
| `ionViewDidLeave` | After the leave transition finishes |

### 🔴 The actual difference — and the reason the question is asked

**Ionic keeps pages alive in the DOM.** When you push page B on top of page A, A is *not* destroyed — it stays in the nav stack. So when you come back to A:

- `ngOnInit` / `ngOnDestroy` — **do NOT run again.** They fire once, at creation and destruction.
- `ionViewWillEnter` / `ionViewDidEnter` — **run every single time** the page is entered.

| | Angular hooks | Ionic hooks |
|---|---|---|
| Driven by | **Component** creation & destruction | **Navigation stack** entry & exit |
| `ngOnInit` on re-entry | ❌ Doesn't re-fire | `ionViewWillEnter` ✅ fires every time |
| Available in | Any Angular app | Ionic pages only (`@ionic/angular`) |
| Use for | One-time setup: DI, form building, static config | Refreshing data, starting/stopping timers, subscriptions tied to visibility |

```ts
export class StudyListPage implements OnInit {
  ngOnInit()          { this.buildForm(); }        // once — one-time setup
  ionViewWillEnter()  { this.loadItems(); }        // every entry — data stays fresh ✅
  ionViewDidEnter()   { this.chart.render(); }     // after the animation — measure/animate safely
  ionViewWillLeave()  { this.timer?.unsubscribe(); } // stop work the user can't see
}
```

> 💬 **Say this:** *"An Ionic page is an Angular component, so all the Angular hooks fire — but Ionic caches pages in the navigation stack instead of destroying them, so `ngOnInit` does **not** run again when the user navigates back. That's why anything that must be fresh on every entry — reloading a list after an edit — goes in `ionViewWillEnter`. One-time setup stays in `ngOnInit`. On RoboGebra that exact distinction caused a bug where an edited item still showed stale data on the list page until I moved the reload into `ionViewWillEnter`."*

**Two more marks if you add them:**
- **`ionViewCanEnter` / `ionViewCanLeave` were Ionic 3 only** — removed in Ionic 4+. Use **Angular route guards** (`CanActivate` / `CanDeactivate`) instead.
- `@ionic/angular` also exposes **`ionViewWillUnload`**, and Angular's `ngOnDestroy` fires when the page is finally popped off the stack.

---

### Q7. How do you handle offline support in Ionic? 🔴

> ⚠️ **Answer honestly about what RoboGebra actually does** — don't claim a full offline-sync engine if it isn't there. The safe, strong framing: *"Here's what we do today, and here's how I'd extend it."*

Answer it as **five layers** — this structure alone reads as senior:

**1. Detect connectivity**
```ts
import { Network } from '@capacitor/network';

const status = await Network.getStatus();          // { connected, connectionType }
Network.addListener('networkStatusChange', s => this.online$.next(s.connected));
```
`@capacitor/network` works on device **and** web. (`navigator.onLine` alone is unreliable — it only says the interface is up, not that the internet is reachable.)

**2. Store data locally — pick the right tool for the size**

| Need | Use |
|---|---|
| Small key-values (token, user prefs, last-sync timestamp) | **`@capacitor/preferences`** |
| Larger structured data, cached lists | **`@ionic/storage-angular`** (IndexedDB / SQLite driver) |
| Big relational datasets, querying offline | **SQLite** (`@capacitor-community/sqlite`) |
| Files, images, documents | **`@capacitor/filesystem`** |
| ❌ Not `localStorage` on native | The OS can clear it, and it's synchronous + size-limited |

**3. Cache reads — serve from cache, refresh in the background**
An **HTTP interceptor** that stores successful `GET` responses and replays them when offline (stale-while-revalidate):
```ts
intercept(req: HttpRequest<any>, next: HttpHandler) {
  if (req.method !== 'GET') return next.handle(req);
  return next.handle(req).pipe(
    tap(res => res instanceof HttpResponse && this.cache.set(req.urlWithParams, res.body)),
    catchError(err => this.cache.has(req.urlWithParams)
      ? of(new HttpResponse({ body: this.cache.get(req.urlWithParams) }))   // offline → cached
      : throwError(() => err))
  );
}
```

**4. Queue writes — the outbox pattern**
While offline, don't fail the user's action: **write to a local queue, update the UI optimistically**, and replay the queue when the connection returns.
```ts
async submit(answer: Answer) {
  await this.store.enqueue(answer);      // persisted outbox
  this.applyOptimistically(answer);      // UI updates immediately
}
// on reconnect
this.online$.pipe(filter(Boolean), switchMap(() => this.flushQueue())).subscribe();
```
Each queued item carries a **client-generated id / idempotency key** so a replayed request can't create duplicates, plus a retry count with backoff.

**5. Sync & conflict resolution**
Track a `lastSyncedAt` timestamp and pull deltas. When the same record changed on both sides, decide the policy explicitly: **last-write-wins** by timestamp (simple), **server-wins** (safest), or **version numbers with a merge prompt** (best UX, most work). Say which one and why.

**Plus the two UX points that make the answer complete:**
- **Tell the user** — an offline banner, disabled submit buttons, and a "3 items pending sync" indicator. Silent failure is the worst outcome.
- **Web/PWA builds** get a **service worker** (`ng add @angular/pwa`) for precached app shell and assets; native builds already bundle assets locally.

> 💬 **Full answer, out loud (~50 s):** *"Five layers. First, detect connectivity with the Capacitor Network plugin and expose it as an observable. Second, persist locally — Capacitor Preferences for small key-values like the token and last-sync time, Ionic Storage or SQLite for larger cached datasets. Third, cache reads through an HTTP interceptor so `GET`s are served from the cache when we're offline and refreshed in the background. Fourth, queue writes in an outbox with idempotency keys and optimistic UI, then replay with retry and backoff on reconnect. Fifth, sync with a `lastSyncedAt` delta pull and an explicit conflict policy — I'd default to last-write-wins by server timestamp and escalate to versioning if the data justified it. And I'd always surface the state in the UI — an offline banner and a pending-sync count — because silent failures destroy trust."*

---



When there's a single round, the interviewer is not screening you for the *next* round — they are making the **hire/no-hire decision live, on that call**. That changes three things:

| Multi-round interview | ⚠️ Single decisive round |
|---|---|
| Technical only; HR comes later | **Everything gets compressed into one call** — tech + project + behavioural + salary + notice period |
| A weak answer is recoverable later | A weak answer on your *headline* skill can end it — no second chance |
| Depth in one area | **Breadth across the whole JD** — Java, Angular, Ionic, DB, and how you work |
| 30–45 min | Usually **60–90 min**, sometimes with a decision-maker on the call |
| "Let's see how they do next round" | They need **confidence to commit today** — so your job is to remove doubt, not just answer correctly |

### The one mindset shift
> Don't just *answer* questions. **Close.** Every answer should reduce their risk in saying yes: name the tech, name the project, name what you owned, and say what you'd do on day one.

### Likely time split of a 60–75 minute single round
| Minutes | What happens | Your goal |
|---|---|---|
| 0–5 | Intro, small talk, "tell me about yourself" | Land the 60-second script — **this sets the frame for the whole call** |
| 5–20 | Project deep-dive (RoboGebra) | Prove you actually built it — modules, decisions, a challenge |
| 20–40 | Java + Spring Boot questions | **Your riskiest stretch** — be crisp, no rambling |
| 40–55 | Angular + Ionic questions | **Your strongest stretch** — be specific, this is where you win |
| 55–65 | Maybe a small coding/logic problem or SQL | Think out loud before writing |
| 65–75 | Notice period, CTC, location, your questions | Be prepared with exact numbers — hesitation here delays the offer |

> 💡 If they skip the coding problem, that's normal for a single-round services hire — they're weighting project experience and communication more heavily.

---

## 2. Your stack map — where you're strong, where to patch

| Area | Your evidence | Confidence | Action before the call |
|---|---|---|---|
| **Angular** | 5 yrs, 4 projects, v16 migration, Formly, NgRx, reactive forms | 🟢 Strong | Revise [04-angular.md](./04-angular.md) — RxJS operators, lifecycle, change detection |
| **Ionic** | RoboGebra (Oct 2024 – present) | 🟡 Real but recent | Read [15-ionic-level1.md](./15-ionic-level1.md) **twice** — it's in the job title |
| **Java** | Java + Spring Boot backend work | 🟡 Medium | [05-java.md](./05-java.md) — collections, HashMap, streams, OOP. **Highest-risk area** |
| **Spring Boot** | REST APIs on RoboGebra; Grails/Groovy on EasyVisa | 🟡 Medium | [06-spring-boot.md](./06-spring-boot.md) — annotations, layers, JPA |
| **TypeScript/JS** | Daily driver | 🟢 Strong | [01](./01-javascript.md) / [03](./03-typescript.md) — quick skim |
| **DB** | MySQL, Postgres, Neo4j, MongoDB | 🟡 Medium | Joins, indexes, basic queries + [13-mongodb.md](./13-mongodb.md) |
| **Coding** | — | 🟡 | [09-coding-problems.md](./09-coding-problems.md) — practise 5 out loud |

> ⚠️ **The honest risk:** your resume is front-end-heavy, but the role is titled *Java with* Angular Ionic. In a single round they cannot "check Java later." Give Java the most revision time — it is the single biggest thing standing between you and the offer.

---

## 3. Your opening answer — memorize this

**"Tell me about yourself"** *(≈60 seconds — never exceed 90)*

> "I'm Priyanka, a Full Stack Developer with **5 years of experience**, currently at Provility Software Solutions in Chennai. I work primarily with **Angular and Ionic on the front end and Java with Spring Boot on the back end**.
>
> My current project is **RoboGebra**, an AI-driven math learning platform built with **Ionic + Angular, Java and MongoDB**. I own several modules end to end — a personalized learning dashboard with progress analytics, a Study Reminder system with scheduling and notification delivery backed by REST APIs, the Study List module with real-time sync, and an interactive quiz module integrated with an AI explanation engine that produces step-by-step solutions in both Tamil and English.
>
> Before that I worked on **Subsea**, a marine operations platform where I built a Schedule-Manager module with Angular and .NET APIs; a **four-repository Angular version migration to v16**; and **EasyVisa**, a visa management system for attorneys where I built the document portal with Angular Formly and nested reactive forms.
>
> So my strength is **end-to-end ownership** — API design through to responsive UI — and this role lines up almost exactly with what I do today, which is why I'm interested."

**Why this works in a one-round setting:** it names the exact JD stack in sentence one, leads with the Ionic+Java project, shows 4 projects of breadth, and closes on fit — so the interviewer starts the call already leaning yes.

---

## 4. The 25 questions most likely to come up (with short answers)

### ☕ Java (expect 8–10 questions)

**1. Four pillars of OOP with a real example?**
Encapsulation (private fields + getters/setters), Inheritance (`extends`, code reuse), Polymorphism (overloading = compile-time, overriding = runtime), Abstraction (abstract class / interface hides implementation). *Example: a `NotificationService` interface with email/push implementations chosen at runtime.*

**2. Abstract class vs Interface?**
Abstract class: can have state, constructors, and both abstract and concrete methods; single inheritance; use for an "is-a" hierarchy with shared code. Interface: a contract; multiple implementation allowed; since Java 8 can have `default`/`static` methods, since Java 9 `private` methods. Interfaces for capability, abstract classes for shared base behaviour.

**3. `ArrayList` vs `LinkedList`?**
ArrayList = dynamic array → O(1) random access, O(n) insert/delete in the middle. LinkedList = doubly linked nodes → O(1) insert/delete given the node, O(n) access. **ArrayList wins for most real cases; LinkedList only for heavy middle insertion.**

**4. How does `HashMap` work internally?** *(near-guaranteed)*
Array of buckets; `hashCode()` → hash → bucket index; collisions chained in a linked list, converting to a **balanced tree above 8 entries in one bucket** (Java 8+); `equals()` distinguishes keys within a bucket. Default capacity 16, load factor 0.75, resizes by doubling. O(1) average, O(log n) worst case.

**5. `equals()` and `hashCode()` contract?**
Equal objects **must** produce equal hash codes. Override one → override the other, or objects get lost in HashMap/HashSet.

**6. `String` vs `StringBuilder` vs `StringBuffer`?**
String is immutable (string pool). StringBuilder is mutable, **not** thread-safe (faster). StringBuffer is mutable and synchronized. Use StringBuilder for concatenation in loops.

**7. Checked vs unchecked exceptions?**
Checked (`IOException`, `SQLException`) — compile-time enforced, catch or declare. Unchecked (`NullPointerException`, `IllegalArgumentException`) — extend `RuntimeException`, programming errors. `Error` (e.g. `OutOfMemoryError`) shouldn't be caught.

**8. Java 8 features you've used?**
Lambdas, Streams, functional interfaces, `Optional`, default methods, method references, new Date/Time API.
```java
List<String> names = employees.stream()
    .filter(e -> e.getSalary() > 50000)
    .map(Employee::getName)
    .sorted()
    .collect(Collectors.toList());
```

**9. `==` vs `.equals()`?** `==` compares references (or primitive values); `.equals()` compares content when overridden.

**10. What is `Optional` and why?** A container that may or may not hold a value — makes "no result" explicit and avoids NPEs. Use `orElse`, `orElseThrow`, `map`, `ifPresent`. Don't use it for fields or parameters.

### 🌱 Spring Boot (3–5 questions)

**11. Spring Boot vs Spring?**
Spring Boot = Spring + **auto-configuration + starter dependencies + embedded Tomcat + Actuator**. Removes XML/boilerplate so you get a runnable JAR with minimal setup.

**12. `@SpringBootApplication` =** `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`.

**13. `@Controller` vs `@RestController`?** `@RestController` = `@Controller` + `@ResponseBody` — methods return data (JSON), not view names.

**14. Explain a REST API you built.** Use your **Study Reminder APIs** — your resume literally says GET/POST/PUT/DELETE.
```java
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {
    private final ReminderService service;
    ReminderController(ReminderService service) { this.service = service; }

    @GetMapping                 List<Reminder> all() { return service.findAll(); }
    @GetMapping("/{id}")        Reminder one(@PathVariable String id) { return service.findById(id); }
    @PostMapping                @ResponseStatus(HttpStatus.CREATED)
                                Reminder create(@Valid @RequestBody Reminder r) { return service.save(r); }
    @PutMapping("/{id}")        Reminder update(@PathVariable String id, @RequestBody Reminder r) { return service.update(id, r); }
    @DeleteMapping("/{id}")     @ResponseStatus(HttpStatus.NO_CONTENT)
                                void delete(@PathVariable String id) { service.delete(id); }
}
```
**15. Dependency injection types?** Constructor (recommended — immutable, testable, catches circular deps), setter, field (`@Autowired` on a field — discouraged).

**16. Global exception handling?** `@RestControllerAdvice` + `@ExceptionHandler` returning a consistent error body with the correct HTTP status.

### 🅰️ Angular (6–8 questions)

**17. Lifecycle hooks in order?**
`ngOnChanges` → `ngOnInit` → `ngDoCheck` → `ngAfterContentInit` → `ngAfterContentChecked` → `ngAfterViewInit` → `ngAfterViewChecked` → `ngOnDestroy`.

**18. Template-driven vs Reactive forms?**
Template-driven: `ngModel`, logic in the template, simple forms. Reactive: `FormGroup`/`FormControl` in the class — synchronous, testable, dynamic. *"I used reactive forms heavily on EasyVisa, including nested forms and custom Angular Formly components."*

**19. Observable vs Promise?**
Promise = single value, eager, not cancellable. Observable = stream of 0..n values, lazy, cancellable, with operators (`map`, `filter`, `switchMap`, `debounceTime`).

**20. `switchMap` vs `mergeMap` vs `concatMap` vs `exhaustMap`?**
`switchMap` cancels the previous inner observable (**typeahead/search**), `mergeMap` runs all concurrently, `concatMap` queues in order, `exhaustMap` ignores new emissions while one is in flight (**login double-click**).

**21. Preventing subscription memory leaks?**
`async` pipe (best), `takeUntil(this.destroy$)` with a `Subject` completed in `ngOnDestroy`, or manual `unsubscribe()`.

**22. What is a service, and why `providedIn: 'root'`?**
A singleton class for shared logic/state/HTTP, injected via DI. `providedIn: 'root'` registers it app-wide and keeps it **tree-shakable**.

**23. Lazy loading — what and why?**
Feature modules loaded on demand via `loadChildren`, shrinking the initial bundle. *"Every page in the Ionic app is lazy loaded by default."*

**24. The Angular migration — how did you approach it?** *(it's on your resume, they will ask)*
> "We migrated four repositories to Angular 16 — the Ticketing app from v2 and three SSG plugin repos from v9/v4. Approach: upgrade **one major version at a time** using `ng update` and the official update guide, fixing breaking changes at each step — RxJS 6→7 imports, `HttpModule`→`HttpClientModule`, stricter TypeScript, deprecated APIs — then updating third-party libraries to compatible versions and regression-testing module by module. The v2→16 repo was hardest: we effectively rebuilt the build and config layer and migrated the module structure incrementally so features kept working throughout."

### 📱 Ionic (4–6 questions — full set in [15-ionic-level1.md](./15-ionic-level1.md))

**25. The four they almost always ask:**
- *What is Ionic and how is it different from Angular?* → Ionic is the mobile UI toolkit + native bridge; Angular is the framework underneath. An Ionic-Angular app **is** an Angular app.
- *`ngOnInit` vs `ionViewWillEnter`?* → pages are cached in the nav stack, so `ionViewWillEnter` fires on **every** entry — use it to refresh data.
- *Cordova vs Capacitor?* → Capacitor is the modern runtime: Promise API, native projects committed to git, PWA support; Cordova is in maintenance mode.
- *How do you access camera/notifications?* → install a Capacitor plugin, `npx cap sync`, call the Promise API; guard with `Capacitor.isNativePlatform()`.

---

## 5. Coding round — the 5 to have cold

Practise **saying your approach out loud before writing** — reported interviews at this company focus on your *thought process*, not just the output.

1. **Reverse a string** (and reverse the word order in a sentence)
2. **Palindrome check**
3. **Find duplicates / first non-repeating character** — HashMap
4. **Second largest element** in an array — single pass, no sorting
5. **FizzBuzz** / count vowels / sum of digits

One Java 8 stream question — *"group employees by department"*:
```java
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));
```
One SQL — *"second highest salary"*:
```sql
SELECT MAX(salary) FROM employee WHERE salary < (SELECT MAX(salary) FROM employee);
```

> 💡 They may share a CoderPad/HackerRank link or ask you to type in a shared doc **without autocomplete**. Practise writing a Java class with `public static void main` by hand.

---

## 6. Project walkthrough — the 3-minute RoboGebra script

They will say *"walk me through your current project."* In a single round this is the **highest-weight answer of the call.** Structure: **What → Stack → My modules → A challenge → Impact.**

> **What:** RoboGebra is an AI-driven math learning platform for students — it explains problems step by step instead of just giving the answer.
> **Stack:** Ionic + Angular front end, Java/Spring Boot REST APIs, MongoDB — running as both a web app and a mobile app through Capacitor.
> **My modules:** the personalized learning dashboard (progress analytics, learning timelines, short and detailed answers); the Study Reminder system (flexible scheduling, notification delivery, full CRUD REST APIs); the Study List module (create/organize/track goals with real-time sync); the quiz module (dynamic questions, instant evaluation, solution walkthroughs); and the bilingual Tamil/English AI explanation views with formula breakdowns.
> **Challenge:** *(pick one and tell it in STAR — e.g. keeping the Study List consistent across screens without flicker, or rendering step-by-step math reliably.)*
> **Impact:** students got consistent reminders and a clear view of their progress, which was the retention goal for that release.

**Be ready for the follow-ups:** *"Why MongoDB and not MySQL?"* · *"How did you secure the APIs?"* (JWT + Spring Security) · *"How did the Angular app talk to the Java service?"* (HttpClient service layer + interceptor, DTOs, error handling) · *"What was hard about Ionic specifically?"* (lifecycle caching, Shadow DOM styling, device-only plugin code) · *"What would you do differently?"* (have one honest answer — it signals seniority).

---

## 7. ⭐ Closing the single round — the last 10 minutes

This is what most candidates waste. With no second round, the ending *is* the decision.

### When they ask "Do you have any questions?" — never say no
Pick 2–3:
1. "Is this role for an internal product or a client project — and which domain?"
2. "How is the team split between the Angular/Ionic front end and the Java services?"
3. "Is the mobile app on Capacitor or Cordova, and which Ionic/Angular versions is the project on?"
4. "What would success in the first three months look like?"
5. "Since this is the only technical round — is there anything about my background you'd like me to clarify before we finish?" ← **this one is gold in a single-round interview.** It surfaces doubts while you can still fix them.

### Your closing statement (say it, don't wait to be asked)
> "Thank you — this has been a good conversation. Just to close from my side: the stack here is exactly what I work on today — Angular and Ionic on the front end, Java and Spring Boot behind it — so there's no ramp-up on the technology. I'm genuinely interested, my notice period is **[X]**, and I can start the onboarding formalities as soon as you need. Is there anything else you need from me?"

### If you get stuck on a question
Never bluff — reported interviewers here probe *how you think*.
> "I haven't worked on that directly. My understanding is *[whatever you do know]*, and if I hit it on the job I'd *[how you'd find out]*. Where I have done something close is *[nearest real experience]*."

That answer scores better than a confident wrong one, and it protects your credibility on everything else you said.

---

## 8. 📋 Onboarding readiness — have this ready BEFORE the call

Since clearing this round goes straight to onboarding, HR may ask for details **on the same call or within hours**. Fumbling here delays or weakens your offer.

### Numbers to have on a sticky note in front of you
| Item | Your answer |
|---|---|
| Current CTC (fixed + variable) | ______ |
| Expected CTC | ______ *(give a number, not "as per company standards")* |
| Notice period | ______ *(and whether buyout/early release is possible)* |
| Earliest joining date | ______ |
| Current location / work mode preference | Chennai / ______ |
| Reason for change | Growth + full-stack ownership — **never** criticize your current employer |
| Any other offers in hand | Be honest but don't bluff a fake offer |

**On expected CTC:** state a number with a small range and anchor it on scope, e.g. *"Based on my 5 years across Angular, Ionic and Java, and the end-to-end ownership in this role, I'm looking at ₹__–__ LPA fixed. I'm open to discussing the overall structure."* If they push first with "what's your expectation?", answering with a number is fine — deflecting twice in a one-round process just stalls things.

### Documents typically requested at onboarding (Indian IT)
Keep scanned PDFs in one folder so you can send within the hour:
- [ ] Updated resume (the one you applied with)
- [ ] Aadhaar + PAN
- [ ] Passport-size photo
- [ ] All education certificates + marksheets (B.Tech, 12th, 10th)
- [ ] Offer letter, appointment letter & increment/promotion letters from Provility (and any earlier employer)
- [ ] **Last 3 months' payslips**
- [ ] **Form 16** / last year's ITR
- [ ] Bank statement showing salary credits (last 3–6 months)
- [ ] Relieving & experience letters from previous employers
- [ ] UAN / PF number
- [ ] Two professional references (name, designation, official email, phone) — **ask them first**

### After the call — same day
- [ ] Send a short thank-you email to HR/the interviewer (4–5 lines: thanks, one thing you enjoyed discussing, reconfirm interest and notice period)
- [ ] Ask HR for the **expected timeline for the offer** and what documents to prepare
- [ ] Do **not** resign until you have the written offer letter in hand

---

## 9. Virtual interview logistics ✅

- [ ] Test **camera, mic and the meeting link 15 minutes early**; keep a phone hotspot as backup
- [ ] Quiet room, plain background, front lighting, laptop at eye level
- [ ] Keep only these open: this file, [15-ionic-level1.md](./15-ionic-level1.md), an editor for the coding round
- [ ] Resume printed or on a second screen — **they read from it**, so know every line of it
- [ ] Pen and paper for the coding problem
- [ ] Dress as for an office; join **2 minutes early**, not 10
- [ ] Speak slightly slower than normal — audio lag eats clarity
- [ ] Have your CTC/notice-period sticky note visible (Section 8)
- [ ] Budget **90 minutes** free after the start time — don't schedule anything behind it

---

## 10. Two-day crash plan

**Day 1 — Java first, because it's your gap**
- Morning: [05-java.md](./05-java.md) — OOP, collections, HashMap internals, exceptions, streams (2.5 h)
- Afternoon: [06-spring-boot.md](./06-spring-boot.md) — annotations, REST layers, DI, exception handling (1.5 h)
- Evening: [09-coding-problems.md](./09-coding-problems.md) — write the 5 problems in Java by hand (1 h)

**Day 2 — Angular + Ionic + delivery**
- Morning: [15-ionic-level1.md](./15-ionic-level1.md) — read fully, then answer Sections A, D, E from memory (1.5 h)
- Midday: [04-angular.md](./04-angular.md) — lifecycle, RxJS operators, forms, lazy loading (2 h)
- Afternoon: rehearse the **self-intro**, the **RoboGebra walkthrough** and the **closing statement** out loud, on camera, twice each (1 h)
- Evening: Section 4 rapid answers + fill in the Section 8 numbers + logistics checklist (45 min)

---

## 11. Final reminders

1. **One round = no recovery.** Prepare breadth, not depth — they'll touch Java, Angular, Ionic, DB and behavioural in one sitting.
2. **Lead every answer with your project.** "Yes — on RoboGebra we…" beats a textbook definition every time.
3. **Java is the gap.** Front-end fluency won't carry a role titled *Java with* Angular Ionic.
4. **Ionic is your edge.** Very few Angular developers can discuss `ionViewWillEnter`, `cap sync` and Shadow DOM theming. You can.
5. **Close the call deliberately** — ask the "anything you'd like me to clarify?" question, state your notice period, and confirm interest.
6. **Have your CTC, notice period and documents ready** — onboarding starts immediately after a yes.
7. **Numbers you own:** 5 years · 4 major projects · 4 repos migrated to Angular 16 · 5 modules on RoboGebra.

You've built exactly what this JD is asking for. Go and close it. 🚀
