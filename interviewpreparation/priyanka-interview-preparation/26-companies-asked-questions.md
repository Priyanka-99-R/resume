# 📋 Companies — Questions Actually Asked (with answers)

> 🎯 **This is your recall log.** Every question below was **actually asked in a real round** — not a prediction. Source: your `altemertic,infoysys,Interview_Questions.pdf` (Infosys · Altimetrik · Photon Interactive) plus the Codeboard Technology round.
>
> **How to use it:** read the question, cover the answer, say it out loud, then check. Before any interview, re-read the company's section — and read **[§ Most-repeated](#-most-repeated-questions-across-all-companies)** every single time, because those questions came up at *four different companies*.
>
> ➕ **Keep appending.** After every round, add the company, the date, and the questions **the same day** while you still remember them. That's what makes this file compound.

---

## 📊 Track record — every company attended so far

| # | Company | Round(s) | Outcome | Questions logged |
|---|---|---|---|---|
| 1 | **Virtusa** | Online assessment | ❌ Not selected | ⚠️ *not logged — [add them](#-virtusa--online-assessment)* |
| 2 | **Infosys** | Technical | ❌ Not selected | ✅ 6 → [§](#-infosys--technical-round) |
| 3 | **Altimetrik** | Level 1 (Java) + Angular round | ❌ Not selected | ✅ 24 → [§](#-altimetrik--level-1-round-java) |
| 4 | **Photon Interactive** | 2 rounds (Frontend + Backend) | ❌ Not selected | ✅ 46 → [§](#-photon-interactive--round-1-frontend) |
| 5 | **Codeboard Technology** | Level 1 (single round → onboarding) | ❌ Not selected | ✅ 7 → [§](#-codeboard-technology--level-1-single-round) |
| 6 | **Mphasis** | L1 Technical (Java/Spring/microservices) — *6 Aug 2026* | 🟢 **CLEARED → L2 + client round scheduled** | ✅ 10 → [§](#-mphasis--technical-round-6-aug-2026) |
| 7 | **Virtusa** | **Round 2** (Java core) — *~18 Aug 2026* | ❌ Not cleared | ✅ 4 → [§](#-virtusa--round-2-java-core-aug-2026) |
| 8 | **Tech Mahindra** | Angular Developer — *~22 Aug 2026* | 🟡 **RESULT PENDING** | ✅ 15 → [§](#-tech-mahindra--angular-developer-round-aug-2026) |
| 9 | **Deloitte (USI)** | Fullstack Java + Angular · AI&E–EaaS · SWE II — *25 Aug 2026* | 🟡 **RESULT PENDING** | → prep: [35](./35-deloitte-fullstack-java-angular-25aug.md) |
| 10 | **Altimetrik** | **Fullstack Java + Angular (2nd attempt)** — *31 Aug 2026* | ❌ Not cleared | ✅ 22 → [§](#-altimetrik--fullstack-java--angular-round-31-aug-2026) |
| 11 | **IQVIA** | 🆕 **Angular/Ionic Technical Lead** — *coming days* | 🔵 **SCHEDULED** | → prep: **[38](./38-iqvia-technical-lead-prep.md)** |
| 12 | **Capgemini** | 🆕 **Java + Spring + Angular** — *Sept 2026* | 🟡 **RESULT PENDING** | ✅ 10 → [§](#-capgemini--java--spring--angular-round-sept-2026) · full answers: **[41](./41-capgemini-java-angular-3sep.md)** |
| | | | | **144 logged** |

> **Read this as data, not as a verdict.** Nine companies, **144 questions**, and the **same topics keep repeating**. That's genuinely good news: the question bank is small and knowable. See **[§ What the pattern says](#-what-the-pattern-says-across-6-rejections)** below.

## Companies logged

| Company | Round | Qs | Section |
|---|---|---|---|
| **Virtusa** | Online assessment | — | [→](#-virtusa--online-assessment) |
| **Infosys** | Technical | 6 | [→](#-infosys--technical-round) |
| **Altimetrik** | Level 1 (Java) | 13 | [→](#-altimetrik--level-1-round-java) |
| **Altimetrik** | Angular round | 11 | [→](#-altimetrik--angular-round) |
| **Photon Interactive** | Round 1 — Frontend | 35 | [→](#-photon-interactive--round-1-frontend) |
| **Photon Interactive** | Round 2 — Backend | 11 | [→](#-photon-interactive--round-2-backend) |
| **Codeboard Technology** | Level 1 (single round) | 7 | [→](#-codeboard-technology--level-1-single-round) |
| **Mphasis** | Technical — Java/Spring/microservices | 10 | [→](#-mphasis--technical-round-6-aug-2026) |
| **Virtusa** | 🆕 Round 2 — Java core (HashMap, threads, interface vs abstract) | 4 | [→](#-virtusa--round-2-java-core-aug-2026) |
| **Tech Mahindra** | Angular Developer — full breadth sweep | 15 | [→](#-tech-mahindra--angular-developer-round-aug-2026) |
| **Altimetrik** | 🆕 **Fullstack Java + Angular (31 Aug 2026)** — Spring depth + Angular + Streams | 22 | [→](#-altimetrik--fullstack-java--angular-round-31-aug-2026) |
| **Capgemini** | 🆕 **Java + Spring + Angular (Sept 2026)** — breadth sweep + 1 Streams program | 10 | [→](#-capgemini--java--spring--angular-round-sept-2026) |
| | | **144** | |

---

## 🔍 What the pattern says (across 6 rejections)

**1. The question bank is small and it repeats.** Lifecycle hooks came up in **4** rounds. Component communication in **4**. `Subject` vs `BehaviorSubject`, unsubscribing, and reactive-vs-template forms in **2–3** each. You are not being asked 93 different things — you're being asked roughly **25 things, six times**. Master those 25 and most of a round is already won.

**2. The failures cluster in three places, not everywhere:**

| Gap | Evidence | Fix |
|---|---|---|
| 🔴 **Output-based Java** — *"what does this print?"* not *"what is X?"* | Codeboard asked 2 output programs and that round ended there | **[23 — Predict the Output](./23-java-output-tricky-questions.md)** — 60 programs. Do these before **every** Java round |
| 🔴 **Live coding under pressure** | Altimetrik: Stream API salary problem, first non-repeating char. Photon: build a standalone login form live | **[22 — Streams](./22-java-streams-coding-problems.md)** + **[09 — Coding Problems](./09-coding-problems.md)** — *type* them, don't read them |
| 🔴 **The "why / what breaks" follow-up** | Photon asked *"what if forkJoin fails"*, *"disadvantages of lazy loading"*, *"root + component provider — error?"* | Every answer needs a **second sentence**: the trade-off, the failure mode, or what you'd do differently |

**2b. 🆕 A round can be 100% backend.** Mphasis (6 Aug 2026) asked **zero Angular questions** — ten questions of Java, Spring Boot, microservices, multithreading and SQL. Your resume reads full-stack, so you cannot treat the Java side as the part you revise last. And **SQL appeared for the first time in six rounds** — joins, `GROUP BY`/`HAVING` and window functions are now a live gap.

**3. Breadth is not your problem — depth on the follow-up is.** You clearly answered enough to reach 2 rounds at Photon and a final round at Codeboard. The rounds are being lost on the *second* question, not the first.

> 💡 **The habit that changes outcomes:** for every answer, say the definition, then **one concrete thing from your own project**, then **one trade-off or failure mode**. Three sentences. That's what "4+ years" sounds like versus "read the docs".

**4. Log every round the same day.** This file only exists because you kept the Infosys/Altimetrik/Photon list. Five companies of question data is a real asset — most candidates walk out and forget by evening.

---

# 🟪 Virtusa — Online Assessment

> ⚠️ **Not logged yet.** You attended a Virtusa **online assessment** and weren't selected, but the questions weren't recorded.
> 
> ➡️ The **later Virtusa Round 2 (Java core) IS logged** — see [§ Virtusa — Round 2](#-virtusa--round-2-java-core-aug-2026).

**Add whatever you can still recall**, even partially — section counts, topics, difficulty, time limits. Even *"3 coding questions, 45 minutes, one on arrays"* is worth writing down, because Virtusa reuses assessment platforms across drives.

Things worth capturing for any online assessment:

| Capture | Why it matters |
|---|---|
| **Sections & timing** (aptitude / logical / verbal / MCQ / coding) | Tells you how to pace next time — most OA failures are time, not knowledge |
| **Number of coding questions + difficulty** | Easy/medium DSA vs framework-specific |
| **Topics of the coding questions** | Arrays, strings, HashMap, streams — that's the drill list |
| **MCQ topics** (Java? Angular? SQL? OOP?) | Sets what to revise |
| **Was it proctored / camera on / tab-switch locked?** | Practical prep for the next one |
| **Platform** (HackerRank, HackerEarth, Mettl, Codility…) | Practise on that exact platform's editor |

> 💬 **For online assessments specifically:** the usual killer is **time management, not difficulty** — attempt every question you can solve fast first, and never sink 20 minutes into one hard problem while three easy ones go unattempted. Drill on **[09 — Coding Problems](./09-coding-problems.md)** with a timer running.

---


> ## 🔵 Answer these with YOUR CODE, not with definitions ⭐
>
> The questions in this file repeat across companies. What changes the outcome is **finishing each answer with one concrete thing from your own codebase**:
>
> ```
> Lifecycle hooks (5 rounds ⭐) → "ngOnDestroy in 100 components, 426 takeUntil"
> Interceptors (4 rounds ⭐)    → the FormData Content-Type trap + cancelRequests$
> Observable vs Promise (4 ⭐)  → "switchMap 246 times — the auth interceptor and every search"
> Data binding (4 rounds)      → 252 @Input, and OnPush on the 6 heavy components
> HashMap internals (3 rounds) → the N+1 aggregation fix (same reasoning, in Mongo)
> Lazy loading (3 rounds)      → 27 route resolvers, access-ruler resolvers
> ```
>
> **Definition → one thing from my code → one trade-off.** Three sentences. That is what five years sounds like.
> Full inventory: **[39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)**.


## 🔁 Most-repeated questions across ALL companies

> **If you prepare nothing else, prepare these.** The count is how many separate rounds asked it.

| Asked in | Question | Answer lives in |
|---|---|---|
| **5 rounds** 🆕 | **Angular lifecycle hooks / `ngOnInit` vs constructor** | [04](./04-angular.md) · [37 Q15](./37-altimetrik-fullstack-java-angular-31aug.md) · Ionic variant in [15](./15-ionic-level1.md) |
| **4 rounds** | **HTTP interceptors** 🆕 | [34 Q4](./34-techmahindra-angular-round.md) · [37 Q17](./37-altimetrik-fullstack-java-angular-31aug.md) |
| **4 rounds** | **Component communication** | [04](./04-angular.md) · below |
| **4 rounds** | **Observables vs Promises** | [20 — RxJS](./20-rxjs-operators.md) · [34 Q6](./34-techmahindra-angular-round.md) |
| **4 rounds** | **Data binding / forms** 🆕 | [25 — Binding & Forms](./25-angular-binding-forms.md) · [37 Q20](./37-altimetrik-fullstack-java-angular-31aug.md) |
| **3 rounds** | **Observable / Subject / BehaviorSubject** | [20 — RxJS](./20-rxjs-operators.md) |
| **3 rounds** 🆕 | **Highest salary per department** (Streams **and** SQL) | [22](./22-java-streams-coding-problems.md) · [36](./36-sql-interview-questions.md) · [37 Q22](./37-altimetrik-fullstack-java-angular-31aug.md) |
| **3 rounds** | **HashMap internals** | [31](./31-hashmap-internals.md) |
| **3 rounds** 🆕 | **Lazy loading (+ its disadvantages)** | [04](./04-angular.md) · [37 Q21](./37-altimetrik-fullstack-java-angular-31aug.md) |
| **3 rounds** 🆕 | **Microservices — communication & failure handling** | [08](./08-microservices-basics.md) · [37 Q11–13](./37-altimetrik-fullstack-java-angular-31aug.md) |
| **2 rounds** | **Reactive vs template-driven forms** | [25 — Binding & Forms](./25-angular-binding-forms.md) |
| **2 rounds** | **Interface vs abstract class** | [05](./05-java.md) · [33](./33-interface-vs-abstract-class.md) |
| **2 rounds** | **Unsubscribing / memory leaks** | [20](./20-rxjs-operators.md) |
| **2 rounds** | **Global exception handling** | [06](./06-spring-boot.md) · below |
| **2 rounds** | **Preventing duplicate API calls on rapid clicks** | below (Altimetrik Q9) · [37 Q18](./37-altimetrik-fullstack-java-angular-31aug.md) (refresh stampede — same technique) |
| **3 rounds** 🆕 | **Spring stereotypes / DI wiring** (`@Component` vs `@Bean`, constructor vs setter) | [06](./06-spring-boot.md) · [37 Q3–7](./37-altimetrik-fullstack-java-angular-31aug.md) · [41 Q3–4](./41-capgemini-java-angular-3sep.md) |
| **2 rounds** 🆕 | **`ngOnChanges` vs `ngDoCheck`** | [04](./04-angular.md) · Photon Q6 below · [41 Q9](./41-capgemini-java-angular-3sep.md) |
| **2 rounds** 🆕 | **Non-repeating character** (first / second-from-the-end) | [22](./22-java-streams-coding-problems.md) · Altimetrik Q3 below · [41 Q10](./41-capgemini-java-angular-3sep.md) |
| **2 rounds** 🆕 | **Externalised config — dev/QA/prod, how values are loaded** | [06](./06-spring-boot.md) · [37 Q14](./37-altimetrik-fullstack-java-angular-31aug.md) · [41 Q5](./41-capgemini-java-angular-3sep.md) |

---

# 🟦 Infosys — Technical Round

*Frontend-weighted round: JS basics, Angular, CSS.*

### 1. How do you declare a variable?
**JavaScript/TypeScript:** `var` (function-scoped, hoisted as `undefined`), `let` (block-scoped, TDZ), `const` (block-scoped, must be initialized, binding can't be reassigned — but object contents can still change).
```ts
let count: number = 0;
const API = 'https://...';
```
**Java:** `type name = value;` — `int count = 0;` (Java 10+ also allows `var count = 0;` for local variables only, inferred at compile time).
> ✅ **Default to `const`, use `let` when you must reassign, never `var`.** Full depth: **[19 — JS Variables Made Easy](./19-javascript-variables-easy.md)**.

### 2. Angular lifecycle hooks
In firing order:
`ngOnChanges` → `ngOnInit` → `ngDoCheck` → `ngAfterContentInit` → `ngAfterContentChecked` → `ngAfterViewInit` → `ngAfterViewChecked` → `ngOnDestroy`

| Hook | Use it for |
|---|---|
| `ngOnChanges` | React to `@Input` changes (gives previous + current value) |
| `ngOnInit` | One-time init — API calls, building forms |
| `ngDoCheck` | Custom/deep change detection — expensive, use rarely |
| `ngAfterViewInit` | Access `@ViewChild` — the view exists now |
| `ngOnDestroy` | **Unsubscribe**, clear timers, remove listeners |

> ⚠️ `ngOnChanges` runs **before** `ngOnInit`, and only for **template-bound** inputs whose **reference** changed.

### 3. How do components communicate?
| Relationship | Mechanism |
|---|---|
| Parent → Child | `@Input()` |
| Child → Parent | `@Output()` + `EventEmitter` |
| Parent → Child (direct call) | `@ViewChild()` |
| Siblings / unrelated | **A shared service** with a `BehaviorSubject` |
| Across routes | Route params / query params / router state |
| App-wide | **NgRx store** (EasyVisa) |
| Content projection | `@ContentChild` + `<ng-content>` |

### 4. How do you change text colour on click **without using classes**?
**Style binding** — bind the style property directly:
```html
<p [style.color]="textColor" (click)="textColor = 'red'">Click me</p>
```
Also acceptable: `[ngStyle]="{ color: textColor }"`, or `Renderer2` when you need to touch the DOM safely:
```ts
constructor(private el: ElementRef, private renderer: Renderer2) {}
onClick() { this.renderer.setStyle(this.el.nativeElement, 'color', 'red'); }
```
> 💬 *"I'd use `[style.color]` — it's declarative and stays inside Angular's rendering. `Renderer2` if I needed it to work with server-side rendering or a non-DOM renderer."* Full detail: **[25 §B](./25-angular-binding-forms.md)**.

### 5. Box model
Every element is a box of four layers, inside out: **content → padding → border → margin**.
```css
* { box-sizing: border-box; }   /* width now INCLUDES padding + border */
```
- `content-box` (default): `width` = content only; padding and border are **added** on top.
- `border-box`: `width` = content + padding + border — far easier to reason about, which is why resets set it globally.
- **Margins collapse** vertically between siblings; padding never collapses.

### 6. Positioning
| `position` | Behaviour |
|---|---|
| `static` | Default. `top/left/...` do nothing |
| `relative` | Offset from its normal spot; **still occupies its original space** |
| `absolute` | Removed from flow; positioned against the nearest **positioned ancestor** |
| `fixed` | Removed from flow; positioned against the **viewport** — doesn't scroll |
| `sticky` | `relative` until it hits a threshold, then behaves like `fixed` |

> ⚠️ The classic follow-up: *"`absolute` relative to what?"* → the nearest ancestor with a `position` other than `static`; otherwise the initial containing block. Full CSS depth: **[02 — HTML & CSS](./02-html-css.md)**.

---

# 🟨 Altimetrik — Level 1 Round (Java)

### 1. Stream API — highest salary per department 🔴
```java
Map<String, Optional<Employee>> topPerDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary))));

// cleaner — just the salary value, no Optional:
Map<String, Double> maxSalary = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.collectingAndThen(
            Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary)),
            e -> e.map(Employee::getSalary).orElse(0.0))));

// or with reducing:
Map<String, Optional<Employee>> r = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment,
        Collectors.reducing(BinaryOperator.maxBy(
            Comparator.comparingDouble(Employee::getSalary)))));
```
> ⚠️ **`maxBy` returns an `Optional`** — the follow-up is always "how do you get rid of the Optional?" → `collectingAndThen`. More: **[22 — Java Streams (20 problems)](./22-java-streams-coding-problems.md)**.

### 2. When was `forEach` introduced?
**Java 8 (2014)** — `Iterable.forEach(Consumer)` and `Stream.forEach(Consumer)`, both enabled by lambdas and `default` methods on interfaces.
> ⚠️ If the interviewer meant JavaScript: `Array.prototype.forEach` came in **ES5 (2009)**. Ask which one they mean — it shows precision.
> **Bonus:** `forEach` vs `forEachOrdered` — on a parallel stream `forEach` gives no order guarantee; `forEachOrdered` preserves encounter order.

### 3. First non-repeating character
```java
public static Character firstNonRepeating(String s) {
    Map<Character, Integer> counts = new LinkedHashMap<>();     // LinkedHashMap preserves order
    for (char c : s.toCharArray()) counts.merge(c, 1, Integer::sum);
    return counts.entrySet().stream()
                 .filter(e -> e.getValue() == 1)
                 .map(Map.Entry::getKey)
                 .findFirst().orElse(null);
}
// "swiss" → 'w'
```
> 📏 **The whole trick is `LinkedHashMap`** — a plain `HashMap` loses insertion order, so "first" becomes meaningless. Say that out loud; it's what they're testing. **O(n)** time, **O(k)** space.

### 4. `ArrayList` vs `LinkedList`
| | `ArrayList` | `LinkedList` |
|---|---|---|
| Backing | Dynamic **array** | **Doubly-linked** nodes |
| `get(i)` | **O(1)** ✅ | O(n) |
| `add`/`remove` at end | Amortized O(1) | O(1) |
| `add`/`remove` in middle | O(n) — shifting | O(1) *once you're at the node* — but O(n) to find it |
| Memory | Compact, cache-friendly | Extra prev/next pointers per node |
| Implements | `List`, `RandomAccess` | `List` + **`Deque`** |

### 5. For searching, which would you use and why? 🔴
**`ArrayList`.** Two reasons:
1. **O(1) random access** — and if the list is sorted, `Collections.binarySearch` gives **O(log n)**, which needs random access to work at all. On a `LinkedList` binary search degrades to O(n) per probe.
2. **Cache locality** — the array is one contiguous block, so scanning it is dramatically faster in practice than chasing pointers, even for the same O(n) linear search.

> 💬 **Best answer:** *"`ArrayList` for searching. If it's sorted I get O(log n) with binary search, which needs random access; and even for a linear scan the contiguous memory is far more cache-friendly than following node pointers. And honestly, if searching is the main operation I'd question the `List` entirely — a `HashMap`/`HashSet` gives O(1) lookup."* ← **that last line is what gets you the mark.**

### 6. Converting to a text block
**Text blocks** — Java 15 (preview in 13/14). Triple quotes, no escaping, natural formatting:
```java
// before
String json = "{\n  \"name\": \"Priyanka\",\n  \"role\": \"Developer\"\n}";

// after — text block
String json = """
        {
          "name": "Priyanka",
          "role": "Developer"
        }
        """;
```
**Rules:** the opening `"""` must be followed by a newline; **incidental leading whitespace is stripped** based on the least-indented line (the closing delimiter's position matters); `\` at end of line joins lines; `\s` keeps a trailing space. More: **[12 — Java 17 Features](./12-java17-features.md)**.

### 7. `HashMap` vs `ConcurrentHashMap`
| | `HashMap` | `ConcurrentHashMap` |
|---|---|---|
| Thread-safe | ❌ | ✅ |
| Null key/values | 1 null key, many null values | ❌ **Neither** (ambiguous with `get` returning null) |
| Locking | — | **Bucket/node-level** CAS + `synchronized` (Java 8+); *segments* pre-Java-8 |
| Concurrent modification | `ConcurrentModificationException` | **Fail-safe** iterator — weakly consistent, no exception |
| Performance | Fastest single-threaded | Scales across threads |
| vs `Hashtable` | — | `Hashtable` locks the **whole map** — legacy, don't use |

### 8. Internal working of `HashMap` 🔴
1. `hash(key)` — `key.hashCode()` **XOR (h >>> 16)** to spread high bits into the low bits.
2. Bucket index = `(n - 1) & hash` — works because capacity is always a **power of 2**.
3. Collision → the entry is appended to that bucket's **linked list**.
4. **Treeify:** when a bucket exceeds **8** nodes *and* capacity ≥ **64**, the list becomes a **red-black tree** → O(log n) instead of O(n). It untreeifies below **6**.
5. **Resize:** when `size > capacity × loadFactor` (**0.75**), capacity **doubles** and entries are rehashed.
6. Default capacity **16**; lookup is **O(1)** average, **O(log n)** worst case since Java 8.

> ⚠️ **The follow-up:** *"What if you use a mutable object as a key?"* → mutate it after insertion and its hash changes, so the entry is stranded in the wrong bucket and becomes unreachable. **Keys must be immutable** with consistent `equals`/`hashCode`. *(Same trap as JPA entities in [24 §L Q9](./24-orm-jpa-hibernate.md).)*

### 9. Immutable class in Java
A class whose state cannot change after construction. Built-in examples: **`String`**, all wrapper types (`Integer`, `Long`, `Double`…), `BigDecimal`, `BigInteger`, `LocalDate`/`LocalDateTime`, `UUID`, records (shallowly).
**Benefits:** inherently **thread-safe** (no synchronization needed), safe as `HashMap` keys and in `Set`s, freely cacheable and shareable, no defensive copying by callers.

### 10. Mutable class
State can change after construction: `StringBuilder`, `StringBuffer`, `ArrayList`, `HashMap`, `Date`, and any normal POJO with setters.
> 💬 *"`String` is immutable, which is why every 'modification' creates a new object — that's exactly why you use `StringBuilder` inside a loop instead of `+=`."*

### 11. Criteria for an immutable class 🔴 (they want the checklist)
1. Declare the **class `final`** — or make all constructors private with a static factory — so nobody can subclass and add mutability.
2. Make all fields **`private final`**.
3. **No setters** — and no method that changes state.
4. **Initialize everything in the constructor.**
5. **Defensive copy on the way in** — copy any mutable argument before storing it.
6. **Defensive copy on the way out** — never return a reference to a mutable field.
7. Don't let `this` escape during construction.

```java
public final class Employee {                       // 1
    private final String name;                      // 2
    private final Date joinDate;                    // mutable type!
    private final List<String> skills;

    public Employee(String name, Date joinDate, List<String> skills) {
        this.name = name;
        this.joinDate = new Date(joinDate.getTime());        // 5 defensive copy IN
        this.skills = List.copyOf(skills);                   // 5 unmodifiable copy
    }

    public String getName() { return name; }
    public Date getJoinDate() { return new Date(joinDate.getTime()); }  // 6 copy OUT
    public List<String> getSkills() { return skills; }        // already unmodifiable
}
```
> ⚠️ **Steps 5 and 6 are what separate a real answer from a memorized one.** Without them the class *looks* immutable but the caller still holds a live reference to the `Date`.

### 12. `@Controller` vs `@RestController`
- **`@Controller`** — returns a **view name** resolved by a `ViewResolver` (JSP/Thymeleaf). To return data you must add `@ResponseBody` per method.
- **`@RestController`** = `@Controller` + **`@ResponseBody` on every method** — the return value is serialized straight to JSON. This is what you use for REST APIs.
```java
@RestController @RequestMapping("/api/users")
public class UserController {
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> get(@PathVariable Long id) { ... }
}
```

### 13. `PUT` vs `PATCH`
| | `PUT` | `PATCH` |
|---|---|---|
| Semantics | **Full replacement** of the resource | **Partial update** — only the sent fields |
| Payload | The complete representation | Just the changed fields |
| Idempotent | ✅ Yes | ⚠️ **Not guaranteed** (e.g. `{"op":"increment"}`) |
| Missing fields | Get **overwritten/nulled** | Left untouched |

> 💬 *"`PUT` replaces the whole resource and is idempotent — sending it ten times leaves the same state. `PATCH` sends only the delta and isn't necessarily idempotent. The practical trap is using `PUT` with a partial body and silently wiping the fields you didn't send."*
> ⚠️ **Exactly the same bug as `form.value` dropping disabled controls — see [25 §H](./25-angular-binding-forms.md).**

---

# 🟨 Altimetrik — Angular Round

### 1. Angular component communication
→ Same as **[Infosys Q3](#3-how-do-components-communicate)** above. Lead with `@Input`/`@Output`, then shared service with `BehaviorSubject` for unrelated components.

### 2. Angular lifecycle hooks
→ Same as **[Infosys Q2](#2-angular-lifecycle-hooks)** above.

### 3. How do you initialize an Observable?
```ts
// 1. from scratch
const obs$ = new Observable<number>(subscriber => {
  subscriber.next(1);
  subscriber.next(2);
  subscriber.complete();
  return () => console.log('teardown on unsubscribe');
});

// 2. creation operators
of(1, 2, 3);                       // emits values synchronously, then completes
from([1, 2, 3]);                   // from array / Promise / iterable
fromEvent(button, 'click');        // from a DOM event
interval(1000); timer(500, 1000);
EMPTY; NEVER; throwError(() => new Error('x'));

// 3. from a Subject (expose it read-only)
private state$ = new BehaviorSubject<User | null>(null);
readonly user$ = this.state$.asObservable();

// 4. what you use 99% of the time
this.http.get<User[]>('/api/users');   // HttpClient returns a cold Observable
```
> ⚠️ **Observables are lazy** — nothing runs until `subscribe()` (or the `async` pipe). Full guide: **[20 — RxJS](./20-rxjs-operators.md)**.

### 4. Reactive forms vs template-driven forms
Model in the class, synchronous, immutable, typed, `FormArray` for dynamic fields, unit-testable without the DOM — **vs** model built implicitly by directives in the template, asynchronous, fine for simple forms.
→ **Full table + everything they can follow up with: [25 §F](./25-angular-binding-forms.md).**

### 5. Formly
**Angular Formly** renders a form from a **JSON field config** instead of hand-written templates — you describe fields as data and it builds the reactive `FormGroup` for you.
```ts
fields: FormlyFieldConfig[] = [
  { key: 'firstName', type: 'input', props: { label: 'First name', required: true } },
  { key: 'employer', type: 'input',
    expressions: { hide: '!model.employed', 'props.required': 'model.employed' } },
];
```
`fieldGroup` = nesting, `fieldArray` = repeatable sections, `expressions` = conditional show/hide and dynamic required.
> ⚠️ **Attribution: Formly is EasyVisa, NOT RoboGebra.** Your story: *"Visa forms were huge, conditional and repeatable — N applicants each with nested address and employment history. Hardcoding was unmaintainable, so a new section became a config change instead of a new component."* ([25 §K](./25-angular-binding-forms.md))

### 6. What is an Observable?
A **stream of values over time** that you subscribe to. Key properties vs a Promise:

| | Observable | Promise |
|---|---|---|
| Values | **Many** over time | Exactly one |
| Execution | **Lazy** — starts on subscribe | **Eager** — starts immediately |
| Cancellable | ✅ `unsubscribe()` | ❌ |
| Operators | 100+ (`map`, `switchMap`, `retry`…) | `.then` chaining only |

### 7. `Subject` vs `BehaviorSubject` 🔴
| | `Subject` | `BehaviorSubject` |
|---|---|---|
| Initial value | ❌ None | ✅ **Required** |
| Late subscriber gets | Only values emitted **after** subscribing | **The current value immediately**, then subsequent ones |
| Read current value | ❌ | ✅ `.getValue()` |
| Use for | Events — "a button was clicked", notifications | **State** — current user, cart, filters |

Also: **`ReplaySubject(n)`** replays the last *n* values to every new subscriber; **`AsyncSubject`** emits only the final value, and only on complete.
> 💬 *"For shared state I always use `BehaviorSubject` — a component that subscribes late still gets the current value instead of sitting empty until the next change."*

### 8. In the Angular migration you mentioned, what challenges did you face?
Your real story (**Angular 16 migration**):
> *"I went version by version with `ng update` rather than jumping, because Angular only supports one-major-at-a-time. The main challenges were: third-party libraries that had no compatible release yet, so I had to check every peer dependency and in a couple of cases replace or wait; **RxJS 6 → 7** deprecations — `toPromise()` removed in favour of `firstValueFrom`, and the safe-subscribe overloads; stricter TypeScript and template type-checking surfacing real bugs that had been hidden; and Ivy/`ngcc` build changes. I did it on a branch with the full test suite as the safety net, upgraded one major at a time, and ran the app after each step so I always knew which version broke what."*
> ✅ Adjust to your real details — but **keep the structure**: one major at a time → dependency compatibility → RxJS/TS breaking changes → tests as the net.

### 9. If a button is clicked multiple times triggering multiple API calls, how do you restrict them? 🔴
**The best RxJS answer is `exhaustMap`** — it ignores new clicks while a request is in flight:
```ts
fromEvent(this.btn.nativeElement, 'click').pipe(
  exhaustMap(() => this.api.save(this.form.value)),   // ✅ ignores clicks until this completes
  takeUntilDestroyed(this.destroyRef)
).subscribe();
```
**Know all four and when each is right:**

| Operator | Behaviour | Right for |
|---|---|---|
| **`exhaustMap`** | **Ignore new** while one is running | ✅ **Submit / save buttons** — no duplicate writes |
| `switchMap` | **Cancel the previous**, keep the latest | Type-ahead search, filters |
| `concatMap` | **Queue** them, run in order | Ordered writes that all must happen |
| `mergeMap` | Run all in parallel | Independent parallel work |

**Also mention the non-RxJS layers** — a complete answer covers all three:
1. **Disable the button** while in flight: `[disabled]="form.invalid || (loading$ | async)"`.
2. **`throttleTime(1000)`** / `debounceTime(300)` to rate-limit at the source.
3. **Server-side idempotency key** — because a determined user (or a flaky network retry) can still double-submit. *That last point is the senior answer.*

### 10. In the frontend, if the request body is missing, how do you handle the error?
Three layers:
1. **Prevent it** — don't let an invalid form submit: `if (this.form.invalid) { this.form.markAllAsTouched(); return; }`, and send `getRawValue()` so disabled controls aren't dropped ([25 §H](./25-angular-binding-forms.md)).
2. **Handle the server's `400`** — the backend replies with field errors; map them onto the form:
```ts
this.api.save(dto).pipe(
  catchError((err: HttpErrorResponse) => {
    if (err.status === 400) {
      Object.entries(err.error.errors ?? {}).forEach(([field, msg]) =>
        this.form.get(field)?.setErrors({ server: msg }));
    }
    this.toast.error('Please correct the highlighted fields');
    return EMPTY;                       // swallow — the UI already told the user
  })
).subscribe();
```
3. **Global fallback** — an HTTP interceptor catches anything unhandled so the user never sees a silent failure (next question).

### 11. Global exception handling
**Angular — two mechanisms, mention both:**
```ts
// 1. HTTP errors → interceptor
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler) {
    return next.handle(req).pipe(
      catchError((err: HttpErrorResponse) => {
        if (err.status === 401) this.auth.logout();
        if (err.status >= 500) this.toast.error('Something went wrong, please try again');
        return throwError(() => err);
      })
    );
  }
}

// 2. Everything else (runtime JS errors) → a global ErrorHandler
@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  handleError(error: unknown) { this.logger.send(error); }
}
// providers: [{ provide: ErrorHandler, useClass: GlobalErrorHandler }]
```
**Spring Boot side** (they often ask both):
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(new ApiError(404, ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalid(MethodArgumentNotValidException ex) { ... }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex) { ... }   // catch-all last
}
```
→ **[06 — Spring Boot](./06-spring-boot.md)**

---

# 🟥 Photon Interactive — Round 1 (Frontend)

*35 questions — the deepest Angular round you've had. Routing, RxJS and change detection dominate.*

### 1. Angular 16 features
**Signals** (developer preview) · `computed()` / `effect()` · **`takeUntilDestroyed()`** and `DestroyRef` · **required inputs** `@Input({ required: true })` · **input transforms** · standalone APIs matured (`ng new --standalone`) · **non-destructive full-app hydration** for SSR · esbuild-based dev server (preview) · self-closing tags in templates · `bootstrapApplication` improvements.
> ⚠️ **RoboGebra web is on 16.2** — so these are the features you can legitimately claim.

### 2. Latest changes in Angular
- **v17** — new **built-in control flow** `@if` / `@for` / `@switch`; **deferrable views** `@defer`; esbuild/Vite builder becomes default; SSR made a first-class `ng new` option; new docs site (angular.dev).
- **v18** — **zoneless change detection** (experimental), Material 3, **event replay** for SSR, signal APIs stabilizing, `@angular/build`.
- **v17.1/17.2** — **signal inputs** `input()`, `output()`, and **`model()`** for signal-based two-way binding.
> ✅ Your Ionic app is **Angular 18.2**, so you can speak to v17/v18 features from real code — say which app you mean.

### 3. Zone and zoneless 🔴
**Zone.js** monkey-patches every async browser API — `setTimeout`, `addEventListener`, XHR/fetch, promises — so that when any of them completes, Angular knows *something might have changed* and runs **change detection across the whole component tree**.
**Zoneless** (v18, experimental) removes zone.js entirely: change detection is triggered **explicitly** by signals, the `async` pipe, `markForCheck()`, and event bindings.
```ts
bootstrapApplication(App, { providers: [provideExperimentalZonelessChangeDetection()] });
```
**Why it matters:** smaller bundle (no zone.js ~13 kB), no more "CD ran 40 times because a `setInterval` ticked", better interop with third-party async libraries, and faster, more predictable rendering.
> 💬 *"Zone.js is how Angular currently knows when to check for changes — it patches async APIs. Zoneless flips it to explicit notification via signals, which is more precise and lets Angular skip the whole tree."*

### 4. What is a component and how do components communicate?
A **component** = a TypeScript class with `@Component` (template + styles + selector) that controls a piece of the screen. Communication → **[Infosys Q3](#3-how-do-components-communicate)**.

### 5. Lifecycle hooks
→ **[Infosys Q2](#2-angular-lifecycle-hooks)**.

### 6. `ngOnChanges` vs `ngDoCheck` 🔴
| | `ngOnChanges` | `ngDoCheck` |
|---|---|---|
| Fires when | A **template-bound `@Input`** changes by **reference** | **Every** change detection cycle |
| Gives you | `SimpleChanges` — previous + current + `firstChange` | Nothing — you check yourself |
| Cost | Cheap | ⚠️ **Expensive** — runs constantly |
| Use for | Reacting to input changes | Detecting changes Angular **can't** see — a mutated array, a deep object property |

```ts
ngDoCheck() {
  if (this.items.length !== this.prevLength) {   // Angular missed the push()
    this.prevLength = this.items.length;
    this.recalculate();
  }
}
```
> 💬 *"`ngOnChanges` only fires when the input **reference** changes, so mutating an array in place is invisible to it. `ngDoCheck` catches that — but it runs on every cycle, so I'd rather fix the root cause with immutable updates than pay that cost."*

### 7. Memory leak issue
The main causes in an Angular app:
1. **Unclosed subscriptions** (the big one) — `valueChanges`, `route.params`, `interval`, WebSockets, a service `Subject`.
2. **DOM event listeners** added manually and never removed.
3. **`setInterval` / `setTimeout`** not cleared in `ngOnDestroy`.
4. **Detached DOM nodes** still referenced by a closure or a service.
5. **Long-lived services holding component references** (e.g. pushing `this` into a service array).

### 8, 9 & 10. What happens if you **don't** unsubscribe — and how does it behave in the browser? 🔴

*They asked this three ways, so give a layered answer:*

**What happens technically:** the subscription holds a reference to the component's callback, so **the component instance can never be garbage collected** even after Angular destroys it. The stream keeps emitting into a dead component.

**How it behaves in the app:**
- The callback keeps running against a **destroyed component** — updating properties nobody renders, and often throwing `Cannot read property of undefined` when it touches something already torn down.
- **Duplicates multiply**: navigate to a page and back five times and you now have five live subscriptions — so one button click fires **five** HTTP requests, or a toast appears five times.
- Timer/WebSocket streams keep **network and CPU work running** for screens the user left.

**How it behaves in the browser:**
- **Heap grows monotonically** — visible in DevTools → Memory as a rising sawtooth that never returns to baseline; the detached component shows up in a heap snapshot as a **detached DOM tree**.
- Increasing **CPU usage and jank** as more zombie subscribers process every emission.
- Eventually the tab becomes unresponsive or the browser kills it — **worst on mobile / Ionic**, where memory limits are much tighter.

**The fixes (say all four):**
```ts
// 1. async pipe — best, unsubscribes automatically
users$ = this.service.getUsers();          // <div *ngIf="users$ | async as users">

// 2. takeUntilDestroyed (Angular 16+) — cleanest imperative option
this.service.data$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe();

// 3. takeUntil with a destroy Subject — the classic pattern
private destroy$ = new Subject<void>();
ngOnDestroy() { this.destroy$.next(); this.destroy$.complete(); }
this.x$.pipe(takeUntil(this.destroy$)).subscribe();

// 4. manual — Subscription.add() then unsubscribe() in ngOnDestroy
```
> ⚠️ **`HttpClient` calls complete after one emission**, so they self-unsubscribe — the leaks come from **infinite** streams: `valueChanges`, `route.params`, `interval`, `fromEvent`, `Subject`s. Say this and you sound like you've actually debugged one. Full detail: **[20 — RxJS](./20-rxjs-operators.md)**.

### 11. Component communication
→ asked twice in the same round. **[Infosys Q3](#3-how-do-components-communicate)**.

### 12. Service provided in root **and** also privately in a component — error or not? 🔴
**No error. It works — and you get TWO separate instances.**

Angular's injector is **hierarchical**. A component-level `providers: [MyService]` creates a **new instance in that component's own injector**, which **shadows** the root one for that component and all of its children. The rest of the app keeps using the root singleton.

```ts
@Injectable({ providedIn: 'root' })   // instance A — app-wide singleton
export class CartService {}

@Component({
  selector: 'app-widget',
  providers: [CartService],           // instance B — this component + its children only
})
export class WidgetComponent {
  constructor(private cart: CartService) {}   // gets B, NOT A
}
```
> ⚠️ **The bug this causes:** shared state silently stops being shared — the widget updates its own cart and the header never sees it. When it's deliberate it's useful (per-component state isolation, like NgRx `ComponentStore`); when it's accidental it's a nasty debugging session.
> 💬 *"It won't throw. Angular resolves from the nearest injector upward, so the component gets its own instance and everything else gets the root one — two instances, not one. If I actually wanted a singleton I'd remove the component-level provider."*

### 13. `HttpClient` — why does Angular use it and what are its advantages?
1. Returns **Observables** — cancellable, retryable, composable with RxJS operators.
2. **Typed responses** — `http.get<User[]>()`.
3. **Interceptors** — one place for auth headers, error handling, logging, caching, loaders.
4. **Automatic JSON** parsing (and `HttpErrorResponse` on failure).
5. **Testability** — `HttpClientTestingModule` + `HttpTestingController` to assert requests without a server.
6. **Progress events** for uploads/downloads, plus typed `HttpParams`/`HttpHeaders` (immutable).
7. Built-in **XSRF** protection support.

### 14. Routing design for home, admin, login, user, product, productList, productDetail 🔴
```ts
export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'home',  component: HomeComponent },

  { path: 'products',
    children: [
      { path: '',    component: ProductListComponent },      // /products
      { path: ':id', component: ProductDetailComponent,      // /products/42
        resolve: { product: productResolver } },
    ]},

  { path: 'user',
    canActivate: [authGuard],
    loadChildren: () => import('./user/user.routes').then(m => m.USER_ROUTES) },

  { path: 'admin',
    canActivate: [authGuard, roleGuard('ADMIN')],            // ⚠️ role check here
    loadChildren: () => import('./admin/admin.routes').then(m => m.ADMIN_ROUTES) },

  { path: '**', component: NotFoundComponent },              // wildcard LAST
];
```
**The four points they're grading:**
- `productList` and `productDetail` are **parent/child** with `:id` — not two flat routes.
- **`pathMatch: 'full'`** on the empty redirect, and the **wildcard route last** (routes match top-down, first match wins).
- **Lazy-load** the `admin` and `user` feature areas — they aren't needed on first paint.
- Guards on the route, not sprinkled inside components.

### 15, 16, 17 & 18. Security after login — where do you configure it, and how do you protect a URL?
**You configure it on the route, not in the component.** Attach guards in the route config so the component is never even constructed for an unauthorized user.
```ts
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService), router = inject(Router);
  return auth.isLoggedIn()
    ? true
    : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

export const roleGuard = (role: string): CanActivateFn => () =>
  inject(AuthService).hasRole(role) || inject(Router).createUrlTree(['/forbidden']);
```
Then: **`canActivate`** on the parent (`/admin`), **`canActivateChild`** for every child, **`canMatch`** to stop the lazy chunk from even downloading, and an **HTTP interceptor** attaching the JWT to every request.

> 🔴 **The point that wins this question:** *"Guards are UX, not security — anyone can edit the JS bundle and reach the route. The **API must enforce authorization server-side** on every endpoint; the guard just stops the user seeing a screen that would fail anyway. On the Spring side that's Spring Security with role checks or `@PreAuthorize`."* **Say this.** Most candidates don't.

### 19. Which component can activate? (Route Guards)
The guard types: **`CanActivate`** (enter a route), **`CanActivateChild`** (enter any child), **`CanDeactivate`** (leave — "you have unsaved changes"), **`CanMatch`** (whether a route config matches at all — replaces the old `CanLoad`), **`Resolve`** (prefetch data).
> ⚠️ **Angular 15+ prefers *functional* guards** (`CanActivateFn` with `inject()`) over class-based ones — class guards are deprecated. Mentioning that shows currency.

### 20. When would you use a Resolver?
When the route **must not activate until the data is there** — so the component renders once, fully populated, with no empty flash and no `*ngIf="data"` scaffolding. Typical: a detail page where a missing record should redirect to 404 *before* navigating.
```ts
export const productResolver: ResolveFn<Product> = route =>
  inject(ProductService).getById(route.paramMap.get('id')!);
// component: this.product = this.route.snapshot.data['product'];
```

### 21. Does every component need a Resolver?
**No — and usually you shouldn't.** A resolver **blocks navigation** until the request finishes, so the app feels frozen on a slow network with no feedback. Prefer navigating immediately and showing a skeleton/spinner (`data$ | async`), and reserve resolvers for cases where partial rendering is genuinely wrong.
> 💬 *"I use a resolver when the page is meaningless without the data or when a 404 should stop the navigation. Otherwise I navigate straight away and render a loading state — it feels faster even though the total time is the same."*

### 22. Lazy loading
Loading a feature's JavaScript **only when its route is visited**, instead of shipping everything in the initial bundle.
```ts
{ path: 'admin', loadChildren: () => import('./admin/admin.routes').then(m => m.ADMIN_ROUTES) }
{ path: 'settings', loadComponent: () => import('./settings.component').then(m => m.SettingsComponent) }
```
**Benefits:** smaller initial bundle → faster first paint/TTI; users never download features they don't use.

### 23. Disadvantages of lazy loading 🔴 *(the one most candidates fumble)*
1. **Delay on first navigation** to that route — the chunk downloads then; on a slow connection the user sees a blank pause. *(Mitigate with `PreloadAllModules` or a custom preloading strategy.)*
2. **More HTTP requests / chunk waterfalls** — many small chunks can be slower than one, especially pre-HTTP/2.
3. **Duplicated code across chunks** if shared dependencies aren't hoisted properly, which can *increase* total download size.
4. **Accidental multiple service instances** — a service provided in a lazy module gets its own injector instance instead of the root singleton. ⚠️ *(Same hierarchy issue as Q12 — use `providedIn: 'root'`.)*
5. **More complexity** — routing config, shared modules, guards (`canMatch`) all get harder to reason about.
6. **Errors surface late** — a broken lazy chunk only fails when someone navigates there, so it can escape testing.

### 24. RxJS
A library for **reactive programming with Observables** — treating events, HTTP responses and state as **streams** you compose with operators (`map`, `filter`, `switchMap`, `combineLatest`, `catchError`, `retry`). Angular uses it for `HttpClient`, router events, `valueChanges`, and `EventEmitter`. → **[20](./20-rxjs-operators.md)**

### 25. Reactive pattern
Programming with **asynchronous data streams and declarative composition**: instead of imperatively calling and storing, you describe how data flows and transforms, and the framework pushes values through it. It's **push-based** (the producer tells you) rather than pull-based (you ask). Benefits: less state to track, cancellation for free, and complex async coordination becomes a readable pipeline.

### 26. Observable vs Observer
- **Observable** = the **producer** — the stream that emits values.
- **Observer** = the **consumer** — the object with `next`, `error`, `complete` callbacks that you pass to `subscribe()`.
```ts
const observer = { next: v => console.log(v), error: e => console.error(e), complete: () => console.log('done') };
observable$.subscribe(observer);
```
`Subject` is unusual in being **both** — it's an Observable *and* an Observer, which is why you can `.next()` into it.

### 27. Hot vs Cold Observable 🔴
| | **Cold** | **Hot** |
|---|---|---|
| Producer | Created **per subscriber** | **Shared** by all subscribers |
| Late subscriber | Gets the **whole sequence** from the start | Gets **only what's emitted from now on** |
| Analogy | A movie on demand — everyone starts at 0:00 | A live broadcast — you join mid-stream |
| Examples | `HttpClient.get()`, `of`, `from`, `interval` | `Subject`, `BehaviorSubject`, `fromEvent(document,'click')` |

> ⚠️ **The practical consequence:** `http.get()` is **cold**, so **each subscription fires its own HTTP request** — subscribe three times and you make three calls. `shareReplay(1)` (or `share()`) makes it hot and shares one response.
```ts
readonly config$ = this.http.get<Config>('/api/config').pipe(shareReplay(1));  // one request, many subscribers
```

### 28. Two independent APIs needed in a single subscription — which operator?
**`forkJoin`** — waits for **all** to complete, then emits one combined array/object. It's the RxJS `Promise.all`.
```ts
forkJoin({
  user: this.http.get<User>('/api/user'),
  orders: this.http.get<Order[]>('/api/orders'),
}).subscribe(({ user, orders }) => { ... });   // both requests fire in parallel
```
**Alternatives and when they're right:** `combineLatest` when the sources keep emitting and you want the latest of each (it emits only after **every** source has emitted at least once); `zip` to pair emissions by index. For **one-shot HTTP calls, `forkJoin` is the answer.**

### 29. If you use `forkJoin`, what happens if one API call fails? 🔴
**The entire `forkJoin` errors immediately and you lose *all* results** — including the responses that already succeeded. Only the `error` callback runs; `next` never fires.

**Fix — catch inside each inner observable so the failure becomes a value:**
```ts
forkJoin({
  user:   this.http.get<User>('/api/user').pipe(catchError(() => of(null))),
  orders: this.http.get<Order[]>('/api/orders').pipe(catchError(() => of([]))),
}).subscribe(({ user, orders }) => { /* runs even if one failed */ });
```
> 📏 **The rule:** `catchError` **inside** the inner pipe = partial success. `catchError` **outside**, on the `forkJoin` itself = you still lose everything, you just handle the error gracefully. **Where you put it is the whole question.**

### 30. State management libraries
**NgRx** (Redux pattern — store/actions/reducers/selectors/effects), **NgRx ComponentStore** (local, per-component), **NgRx SignalStore**, **NGXS** (less boilerplate, decorator-based), **Akita/Elf**, or simply a **service with a `BehaviorSubject`** for smaller apps.
> ⚠️ **Attribution: NgRx = EasyVisa, not RoboGebra.** ([21 — NgRx](./21-ngrx.md))

### 31. localStorage / sessionStorage — why use a state management library instead? 🔴
They solve **different problems**: storage is **persistence**, a store is **runtime state**.

| | localStorage / sessionStorage | State library (NgRx) |
|---|---|---|
| Reactive | ❌ **Not reactive** — nothing re-renders when it changes | ✅ Selectors push updates to every subscriber |
| Data types | **Strings only** — manual `JSON.stringify/parse` | Real typed objects |
| API | **Synchronous** — blocks the main thread | Async-friendly, effects for side effects |
| Scope | Per origin, survives reload (`session` = per tab) | In-memory, lost on reload |
| Tooling | None | **Redux DevTools** — time-travel, action log |
| Structure | Ad-hoc keys, no single source of truth | Immutable single source of truth |
| Security | ⚠️ Readable by any script — **XSS-exposed** | In memory only |

> 💬 *"They're complementary, not alternatives. The store is the single reactive source of truth the UI renders from; storage is where I persist a slice of it — a token or user preferences — so it survives a reload. I'd hydrate the store from storage on bootstrap rather than reading storage from components. And I wouldn't put anything sensitive in localStorage because any XSS can read it."*

### 32. Side effects
Anything outside pure state updates — HTTP calls, routing, localStorage, toasts, logging. In NgRx they live in **Effects**: listen for an action, do the async work, dispatch a success/failure action. Reducers stay **pure**.
```ts
loadUsers$ = createEffect(() => this.actions$.pipe(
  ofType(UserActions.load),
  switchMap(() => this.api.getUsers().pipe(
    map(users => UserActions.loadSuccess({ users })),
    catchError(error => of(UserActions.loadFailure({ error })))   // ⚠️ INNER catchError
  ))
));
```
> ⚠️ **If `catchError` is on the outer pipe, the effect dies after the first error and never runs again.** Guaranteed follow-up. ([21](./21-ngrx.md))

### 33. Angular 17 features
**Built-in control flow** `@if` / `@else` / `@for` (with mandatory `track`) / `@switch` — no `CommonModule` import, better type narrowing, faster; **deferrable views** `@defer (on viewport)` for lazy-loading a *component* declaratively; **esbuild/Vite** application builder by default (much faster builds); **SSR + hydration** as a first-class `ng new` option; standalone by default; angular.dev docs.
```html
@if (user(); as u) { <p>{{ u.name }}</p> } @else { <app-login /> }
@for (item of items(); track item.id) { <li>{{ item.name }}</li> } @empty { <li>None</li> }
@defer (on viewport) { <app-heavy-chart /> } @placeholder { <div>Loading…</div> }
```

### 34. Standalone component: username + password form, submit disabled until typed 🔴
```ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: true,                                   // ← standalone: no NgModule
  imports: [CommonModule, ReactiveFormsModule],       // ← imports live on the component
  template: `
    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <input formControlName="username" placeholder="Username">
      <div class="error" *ngIf="isInvalid('username')">Username is required</div>

      <input type="password" formControlName="password" placeholder="Password">
      <div class="error" *ngIf="isInvalid('password')">Minimum 6 characters</div>

      <button type="submit" [disabled]="form.invalid">Login</button>
    </form>
  `,
})
export class LoginComponent {
  form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  constructor(private fb: FormBuilder) {}

  isInvalid(name: string): boolean {
    const c = this.form.get(name);
    return !!c && c.invalid && (c.dirty || c.touched);
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    console.log(this.form.getRawValue());
  }
}
```
**What they're checking:** `standalone: true` + `imports` on the component (no NgModule), `ReactiveFormsModule` imported, **`[disabled]="form.invalid"`** for the button, and validators declared in the class. → **[25](./25-angular-binding-forms.md)**

### 35. During sign-up, how do you check if the user already exists? 🔴
An **async validator** on the email/username control:
```ts
userExists(): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    if (!control.value) return of(null);
    return timer(400).pipe(                                   // debounce keystrokes
      switchMap(() => this.api.checkUsername(control.value)),  // cancels the previous check
      map(exists => (exists ? { userExists: true } : null)),
      catchError(() => of(null)),                             // don't block on API failure
      first()                                                 // ⚠️ MUST complete
    );
  };
}
// username: ['', [Validators.required], [this.userExists()]]   ← 3rd argument
```
```html
<div *ngIf="form.get('username')?.pending">Checking…</div>
<div *ngIf="form.get('username')?.hasError('userExists')">That username is taken</div>
<button [disabled]="form.invalid || form.pending">Sign up</button>
```
**The four things they're grading:** async validators go in the **third** argument; the observable **must complete** (`first()`) or the control is stuck `PENDING` forever; debounce so you don't hit the API per keystroke; and — **the senior point** — *"the client check is a UX convenience; the real uniqueness constraint is a **unique index in the database** and a `409 Conflict` from the API, because two users can sign up in the same second."*

---

# 🟥 Photon Interactive — Round 2 (Backend)

### 1 & 2. Is your application built on microservices? How many services?
> ✅ **Honest RoboGebra answer:** *"It's a microservice-style platform rather than a textbook microservices architecture. There are separate deployable services — the main Spring Boot backend, a separate admin Spring Boot service on port 4000, and a Node/Express CRM — plus the Angular web app, the Ionic mobile app and a React CRM UI, across about eight repos. Inside the main backend it's modular — around 70 domain modules each with controller, service, repository and model layers — but they deploy together, so I'd call that a modular monolith with a couple of satellite services rather than fine-grained microservices."*
> ⚠️ **Don't oversell.** Saying "modular monolith + satellite services" and being right beats claiming 20 microservices and collapsing on the follow-up ("how do you handle distributed transactions?"). → **[18](./18-robogebra-technical-versions.md)** · **[08](./08-microservices-basics.md)**

### 3. Interface and abstract class — real-time examples 🔴
**Abstract class — a shared base with state and partial implementation:**
```java
abstract class PaymentProcessor {                   // shared state + template method
    protected final AuditLog log;
    PaymentProcessor(AuditLog log) { this.log = log; }

    public final void process(Payment p) {          // template method — fixed flow
        validate(p);
        doCharge(p);                                // varies per subclass
        log.record(p);
    }
    protected void validate(Payment p) { /* shared */ }
    protected abstract void doCharge(Payment p);
}
class CardProcessor extends PaymentProcessor { protected void doCharge(Payment p) { ... } }
class UpiProcessor  extends PaymentProcessor { protected void doCharge(Payment p) { ... } }
```
**Interface — a capability that unrelated classes can implement:**
```java
interface Exportable { byte[] toCsv(); }            // Invoice, Report and User can all be Exportable
// Real-world: List, Comparable, Runnable, JpaRepository, Angular's OnInit
```
> 💬 *"Abstract class when subclasses genuinely share state and a flow — my payment processors share the audit log and the template method, and differ only in how they charge. Interface when the classes are unrelated but share a capability, or when I need multiple inheritance of type. Since Java 8 interfaces can have `default` methods, so the real line is **state and constructors** — only an abstract class has those."* → **[16 §0 Q1](./16-codeboard-technology-level1.md)**

### 4. Create a Java string array with strings and numbers
```java
String[] mixed = { "Priyanka", "42", "Angular", "2026" };   // numbers as text
System.out.println(mixed[1]);                                // "42" — a String
int n = Integer.parseInt(mixed[1]);                          // convert when needed
```
**If they mean genuinely mixed types** (the likely trap):
```java
Object[] mixed = { "Priyanka", 42, 3.14, true };             // Object[] holds anything
for (Object o : mixed) System.out.println(o + " : " + o.getClass().getSimpleName());
```
> 📏 **The point:** a `String[]` can only ever hold `String`s — Java arrays are typed and **covariant**, so `Object[] a = new String[2]; a[0] = 42;` compiles but throws **`ArrayStoreException`** at runtime. Mention that and you've answered the question behind the question.

### 5, 6 & 7. Create an interface, create an object of it, try/catch
```java
interface Greeting {
    String greet(String name);          // implicitly public abstract
    default String hello() { return greet("World"); }   // Java 8 default method
}
```
**"Create an object of that interface" — the trap.** You **cannot instantiate an interface** (`new Greeting()` ❌). Three legal ways:
```java
// 1. A concrete implementing class
class English implements Greeting { public String greet(String n) { return "Hello " + n; } }
Greeting g1 = new English();

// 2. An anonymous inner class — this is what "new Greeting() { }" really means
Greeting g2 = new Greeting() {
    @Override public String greet(String n) { return "Hi " + n; }
};

// 3. A lambda — only for a FUNCTIONAL interface (exactly one abstract method)
Greeting g3 = n -> "Hey " + n;
```
> 💬 *"You can't instantiate an interface directly — you instantiate something that implements it. `new Greeting() { ... }` looks like instantiating the interface but it's actually an anonymous class. And because `Greeting` has a single abstract method, it's a functional interface, so a lambda works too."*

**try/catch:**
```java
try {
    int result = 10 / divisor;
} catch (ArithmeticException e) {           // most specific FIRST
    log.error("Divide by zero", e);
} catch (Exception e) {                     // broadest LAST
    log.error("Unexpected", e);
} finally {
    // always runs
}
```
⚠️ Catching a **broader** exception before a narrower one is a **compile error** (unreachable catch block).

### 8. `finally` block
Runs **whether or not** an exception is thrown, and **even if the try or catch block `return`s** — it's for cleanup: closing connections, files, streams, releasing locks.
**The only cases it doesn't run:** `System.exit()`, a JVM crash, or the thread being killed.

### 9. What happens if you remove the `catch` block? 🔴
**`try`–`finally` without a `catch` is perfectly legal and compiles.** What changes:
- The **`finally` block still runs** (cleanup is guaranteed).
- Then the exception **propagates up** to the caller — this method does **not** handle it.
- If nothing up the stack handles it, the thread terminates and the stack trace is printed.
```java
try {
    riskyOperation();
} finally {
    connection.close();          // ✅ still runs, then the exception keeps propagating
}
```
⚠️ **`try` completely alone — no `catch` and no `finally` — IS a compile error.** You need at least one of them.
> ⚠️ **The killer follow-up:** *"What if `finally` has a `return`?"* → it **swallows the exception entirely** and the method returns normally. Never put a `return` in `finally`. *(See [23 §H](./23-java-output-tricky-questions.md).)*

### 10. If the try block succeeds, what is the purpose of `finally`?
**Its purpose isn't the failure path — it's the guarantee.** `finally` runs on **every** exit path: success, exception, and even an early `return` from inside `try`. That's what makes it the right place for cleanup, because you write the release **once** instead of duplicating it in the happy path and every catch block.
```java
Connection conn = null;
try {
    conn = ds.getConnection();
    return query(conn);           // ⚠️ even this return doesn't skip finally
} finally {
    if (conn != null) conn.close();   // runs after the return value is computed
}
```
> ✅ **Land it here:** *"In modern Java I'd use **try-with-resources** instead — anything implementing `AutoCloseable` gets closed automatically, in reverse order, and it correctly handles an exception thrown by `close()` itself by adding it as a **suppressed** exception. That's cleaner than a manual `finally`."*
```java
try (Connection conn = ds.getConnection();
     PreparedStatement ps = conn.prepareStatement(SQL)) {
    return query(ps);
}   // both closed automatically, ps first
```

### 11. Spring Boot validation
```java
// 1. Annotate the DTO (jakarta.validation — Boot 3 uses jakarta, not javax)
public record UserDto(
    @NotBlank(message = "Name is required")            String name,
    @Email  @NotBlank                                  String email,
    @Min(18) @Max(120)                                 int age,
    @Size(min = 8, message = "Min 8 characters")       String password,
    @Pattern(regexp = "^\\d{10}$")                     String phone,
    @NotNull @Valid                                    Address address   // @Valid = cascade to nested
) {}

// 2. Trigger it with @Valid on the controller parameter
@PostMapping("/users")
public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto dto) { ... }

// 3. Turn the failure into a clean response
@RestControllerAdvice
public class ValidationHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handle(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);        // 400 with field-level messages
    }
}
```
**Points that earn marks:**
- Needs **`spring-boot-starter-validation`** on the classpath — it's not in `starter-web` since Boot 2.3.
- **`@Valid` vs `@Validated`** — `@Valid` is the JSR-380 standard (method params, nested objects); **`@Validated`** is Spring's, and it's what enables **validation groups** and class-level validation on `@Service` beans.
- **`@NotNull` vs `@NotEmpty` vs `@NotBlank`** — not null / not null and size>0 / not null and contains non-whitespace. ⚠️ A guaranteed follow-up.
- The failure is **`MethodArgumentNotValidException`** for `@RequestBody`, but **`ConstraintViolationException`** for `@RequestParam`/`@PathVariable` on a `@Validated` class — handle both.
- **Custom validator:** a `@Constraint` annotation + a `ConstraintValidator<A, T>` implementation.

---

# 🟩 Codeboard Technology — Level 1 (single round)

*Single round → onboarding. **The Java portion was output-based, not theory** — that's the lesson that cost it.*

| # | Question | Answer |
|---|---|---|
| 1 | Abstract class | **[16 §0 Q1](./16-codeboard-technology-level1.md)** — + abstract vs interface table |
| 2 | Data hiding | **[16 §0 Q2](./16-codeboard-technology-level1.md)** — + encapsulation vs abstraction + access modifiers |
| 3 | Output: `methodTest(null)` | **`Calling String method`** — most-specific overload wins at compile time |
| 4 | Output: override adding `throws IOException` | **Compile error** — can't widen the checked-exception clause |
| 5 | Ionic lifecycle hooks | `ionViewWillEnter` / `DidEnter` / `WillLeave` / `DidLeave` |
| 6 | Angular vs Ionic lifecycle hooks | Ionic **caches pages** in the nav stack → `ngOnInit` doesn't re-fire; `ionViewWillEnter` does |
| 7 | Offline support in Ionic | 5 layers: detect → store → cache reads → queue writes → sync |

> 🔴 **Full answers with follow-ups: [16 §0 — What they actually asked](./16-codeboard-technology-level1.md).**
> 🔴 **The real lesson:** they asked *"what does this print?"*, not *"what is polymorphism?"*. **Do [23 — Java Predict the Output](./23-java-output-tricky-questions.md) before every Java round from now on.**

---

# 🔵 Mphasis — Technical Round (6 Aug 2026)

> 🟢 **RESULT: CLEARED.** Mphasis is moving her forward to **Level 2** plus a **client technical round** — the first time in six companies a next round has been offered.
>
> 🎯 **Prep for those two rounds: [30 — Mphasis Level 2 + Client Round](./30-mphasis-level2-client-round.md).** HR's brief: *"Java only mostly. With Angular, what you have used in recent projects they'll ask. Basics of Angular not mandatory."*
>
> 🔴 **The 10 questions below are the L2 syllabus, not just history** — L2 interviewers read the L1 feedback form and re-probe the same threads one level deeper. File 30 Part A lists the expected follow-up to each one.

*Backend-weighted **Java + Spring Boot + microservices** round — 10 questions. Mostly **theory**, plus **one coding problem** (thread-safe Singleton) and **one SQL query**. Notably: **no Angular at all**, and **no predict-the-output** — a different shape from Codeboard.*

| # | Question | Type |
|---|---|---|
| 1 | What is Microservices? | Theory |
| 2 | Design patterns in microservices | Theory 🔴 |
| 3 | CircuitBreaker annotation | Theory |
| 4 | Which dependency is added for circuit breaker? | Theory ⚠️ |
| 5 | Write a program for a thread-safe Singleton class | **Coding** 🔴 |
| 6 | Spring Boot Actuator | Theory |
| 7 | Difference between Association and Aggregation in OOP | Theory |
| 8 | Total employees + average salary per department | **SQL** 🔴 |
| 9 | What is deadlock in multithreading? | Theory |
| 10 | How do you improve API performance in backend and frontend? | Theory / experience |

---

### 1. What is Microservices?

> *"Microservices is an architectural style where an application is built as a set of **small, independently deployable services**, each owning one business capability and its own database, communicating over lightweight protocols — usually REST or messaging. The opposite is a monolith, where everything ships as one unit."*

**The second sentence — from your own project:**
> *"On RoboGebra we had 8 separate repositories with independent Spring Boot services, so a change to one service could be deployed without redeploying the whole platform."*

**The third sentence — the trade-off. Say this unprompted:**
> *"The cost is real though: distributed transactions instead of a single `@Transactional`, network calls that can fail, eventual consistency, and much harder debugging and monitoring. For a small team or an unclear domain a well-structured monolith is usually the better first move — you split it once you know where the seams actually are."*

| Monolith | Microservices |
|---|---|
| One deployable unit | Many, deployed independently |
| One shared database | **Database per service** |
| In-process method calls | Network calls (REST / gRPC / message queue) |
| Scale the whole app | Scale only the hot service |
| Simple to debug & test | Needs distributed tracing, log aggregation |
| One tech stack | Polyglot possible |
| ACID transactions | **Saga / eventual consistency** |

> 🔗 Full depth: **[08 — Microservices Basics](./08-microservices-basics.md)**. Photon Round 2 asked the same thing (*"is your application built on microservices, how many services?"*) — this is now a **2-round repeat**.

---

### 2. Design patterns in microservices 🔴

**The ones to name — group them, don't list randomly. Grouping is what sounds senior.**

| Group | Pattern | What it solves |
|---|---|---|
| **Decomposition** | Decompose by business capability / subdomain | How to split the monolith |
| | **Strangler Fig** | Migrating a monolith incrementally instead of a big-bang rewrite |
| **Data** | **Database per Service** | Each service owns its schema — no shared tables |
| | **Saga** (choreography or orchestration) | Distributed transactions without 2PC; compensating actions on failure |
| | **CQRS** | Separate read and write models when their needs diverge |
| | **Event Sourcing** | Store the sequence of events, not just current state |
| | **API Composition / Aggregator** | Join data that lives in several services |
| **Communication** | **API Gateway** | One entry point: routing, auth, rate limiting, SSL termination |
| | **BFF (Backend for Frontend)** | A tailored gateway per client — web vs mobile |
| | **Service Discovery** (Eureka, Consul) | Services find each other without hard-coded hosts |
| **Reliability** | **Circuit Breaker** | Stop calling a failing service; fail fast *(→ Q3)* |
| | **Retry + Timeout + Backoff** | Handle transient failures without hammering |
| | **Bulkhead** | Isolate thread pools so one slow service can't exhaust all threads |
| | **Sidecar / Ambassador** | Cross-cutting concerns as a separate process (service mesh) |
| **Observability** | **Log Aggregation** (ELK) | One place to search logs across services |
| | **Distributed Tracing** (Sleuth/Micrometer + Zipkin) | Follow one request across services via a correlation id |
| | **Health Check API** | Load balancers know who's alive *(→ Q6, Actuator)* |
| **Config** | **Externalized Configuration** (Spring Cloud Config) | Config outside the artefact, per environment |

**If they want depth, pick Saga — it's the one they're usually fishing for:**

> *"Saga handles a transaction spanning services, because you can't hold a database transaction across a network. It's a sequence of local transactions where each one publishes an event triggering the next, and if a step fails you run **compensating transactions** to undo the earlier ones. Two flavours: **choreography**, where services react to each other's events — simple but the flow is implicit and hard to trace; and **orchestration**, where a central coordinator drives the steps — easier to reason about but it's another component to run. For an order flow across Order, Payment and Inventory I'd choose orchestration, because 'what state is this order in?' has to be answerable."*

---

### 3. CircuitBreaker annotation

```java
@Service
public class InventoryClient {

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackStock")
    @Retry(name = "inventoryService")
    @TimeLimiter(name = "inventoryService")
    public String getStock(String sku) {
        return restTemplate.getForObject("http://inventory/stock/" + sku, String.class);
    }

    // ⚠️ Must have the SAME return type + the SAME parameters + a Throwable as the last param
    private String fallbackStock(String sku, Throwable t) {
        return "UNAVAILABLE";
    }
}
```

> *"`@CircuitBreaker` is from **Resilience4j** — `io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker`. It wraps the method so that when the failure rate crosses a threshold, the breaker **opens** and subsequent calls fail immediately into the fallback method instead of waiting on a dead service. That protects the caller's threads — without it, one slow downstream service exhausts your thread pool and takes your service down too."*

**The three states — draw them if there's a whiteboard:**

```
        failure rate > threshold
CLOSED ─────────────────────────► OPEN
  ▲                                 │  (all calls fail fast → fallback)
  │                                 │  after waitDurationInOpenState
  │      test calls succeed         ▼
  └────────────── HALF_OPEN ◄───────┘
                     │  test calls fail
                     └──────────────► OPEN
```

| State | Behaviour |
|---|---|
| **CLOSED** | Normal — calls pass through, failures are counted |
| **OPEN** | Calls fail immediately into the fallback; the downstream service is left alone to recover |
| **HALF_OPEN** | After a wait, a few trial calls are permitted — succeed → CLOSED, fail → OPEN again |

**Configuration lives in `application.yml`, not the annotation:**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      inventoryService:
        slidingWindowSize: 10
        failureRateThreshold: 50          # % of failures that opens the breaker
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
        registerHealthIndicator: true     # surfaces in /actuator/health  → Q6
```

**⚖️ The legacy answer they may be testing for:** *"The older Netflix option was **Hystrix** — `@HystrixCommand(fallbackMethod = "...")` with `@EnableCircuitBreaker` on the main class. **Hystrix has been in maintenance mode since 2018** and Spring Cloud removed it, so Resilience4j is the current answer. If a codebase still has Hystrix, that's a migration item."*

---

### 4. Which dependency is added for circuit breaker? ⚠️

**Spring Boot 3 + Resilience4j (the current answer):**
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>   <!-- ...-spring-boot2 for Boot 2.x -->
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>     <!-- 🔴 REQUIRED -->
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId> <!-- to see breaker state -->
</dependency>
```

**Or the Spring Cloud starter, which pulls in the above:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

> 🔴 **The detail that earns the mark:** *"`spring-boot-starter-aop` is mandatory and it's the thing people miss. The annotations are implemented with **AOP proxies** — without it the app starts fine, the annotation is silently ignored, and there's no circuit breaker at all. Same class of failure as a `@Transactional` that does nothing."*

**And the second AOP trap, which is a classic follow-up:**
> *"Because it's proxy-based, **calling an annotated method from within the same class bypasses the proxy** and the breaker never fires. `this.getStock()` is a plain Java call. The fix is to move the annotated method into a separate bean and inject it — exactly the same self-invocation problem as `@Transactional`."*

> 🔗 That self-invocation trap is covered in **[24 — ORM/JPA §@Transactional](./24-orm-jpa-hibernate.md)**.

---

### 5. Write a program for a thread-safe Singleton class 🔴 *(coding)*

**Lead with this one — it's the best answer, and short:**

```java
// ✅ BILL PUGH / static holder — lazy, thread-safe, no synchronization cost
public class Singleton {

    private Singleton() { }

    private static class Holder {
        private static final Singleton INSTANCE = new Singleton();
    }

    public static Singleton getInstance() {
        return Holder.INSTANCE;
    }
}
```

> *"The inner class isn't loaded until `getInstance()` is called for the first time, so it's **lazy**. And the JVM guarantees class initialization is **thread-safe** — the classloader holds a lock — so there's no `synchronized` in my code and no cost on every subsequent call. This is my default."*

**If they specifically ask for double-checked locking:**

```java
public class Singleton {

    private static volatile Singleton instance;      // 🔴 volatile is ESSENTIAL

    private Singleton() { }

    public static Singleton getInstance() {
        if (instance == null) {                      // 1st check — no lock, fast path
            synchronized (Singleton.class) {
                if (instance == null) {              // 2nd check — under the lock
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

> 🔴 **Why `volatile`? This is the whole question.** *"`new Singleton()` is three steps — allocate memory, run the constructor, assign the reference. Without `volatile` the JIT is allowed to **reorder** steps 2 and 3, so another thread can see a non-null reference pointing at a **partially constructed object** and use it. `volatile` forbids that reordering and guarantees visibility across threads. Double-checked locking was famously broken in Java before the Java 5 memory model fixed `volatile`."*

**The other approaches — know the trade-offs:**

```java
// Eager — thread-safe (class init), but built even if never used
public class Singleton {
    private static final Singleton INSTANCE = new Singleton();
    private Singleton() { }
    public static Singleton getInstance() { return INSTANCE; }
}

// Synchronized method — correct but slow: EVERY call takes the lock, forever
public static synchronized Singleton getInstance() {
    if (instance == null) instance = new Singleton();
    return instance;
}

// ✅ ENUM — Joshua Bloch's recommendation in Effective Java
public enum Singleton {
    INSTANCE;
    public void doWork() { }
}
```

**🔥 The follow-up they ask next: "Can your singleton be broken?"**

| Attack | Breaks Bill Pugh / DCL? | Breaks enum? |
|---|---|---|
| **Reflection** — `setAccessible(true)` on the private constructor | ✅ yes | ❌ no — the JVM forbids reflective enum instantiation |
| **Serialization** — deserializing creates a new instance | ✅ yes | ❌ no |
| **Cloning** | ✅ if it implements `Cloneable` | ❌ no |
| **Multiple classloaders** | ✅ yes | ✅ yes |

```java
// Reflection breaking it:
Constructor<Singleton> c = Singleton.class.getDeclaredConstructor();
c.setAccessible(true);
Singleton second = c.newInstance();       // 💥 a second instance exists

// Defences:
private Singleton() {
    if (Holder.INSTANCE != null) throw new IllegalStateException("Use getInstance()");
}
protected Object readResolve() { return getInstance(); }   // serialization
```

> **The closing line:** *"Enum handles reflection, serialization and cloning for free, which is why Effective Java recommends it. The reason people still use the holder pattern is that an enum can't extend a class and can't take constructor arguments — so for a singleton that needs injected dependencies I'd let **Spring** manage it instead, since a Spring bean is singleton-scoped by default and this whole problem disappears."*

That last sentence is the senior move: it connects the puzzle back to how you'd actually build it.

---

### 6. Spring Boot Actuator

> *"Actuator adds **production-ready endpoints** for monitoring and managing a running app — health, metrics, environment, beans, mappings, log levels — without writing any of it yourself. In a microservices setup it's what a load balancer or Kubernetes probes to decide whether an instance is alive."*

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

| Endpoint | Shows |
|---|---|
| `/actuator/health` | UP/DOWN — plus DB, disk, Redis, **circuit breaker** sub-checks |
| `/actuator/info` | Build version, git commit |
| `/actuator/metrics` | JVM memory, threads, HTTP request timings, connection pool |
| `/actuator/env` | ⚠️ All config properties — **leaks secrets** |
| `/actuator/beans` | Every bean in the context |
| `/actuator/mappings` | All request mappings — useful for "why is my endpoint 404?" |
| `/actuator/loggers` | Read **and change log levels at runtime**, no restart |
| `/actuator/threaddump` | 🔴 Thread dump — **this is how you diagnose a deadlock** *(→ Q9)* |
| `/actuator/heapdump` | ⚠️ Downloads the heap — leaks everything in memory |
| `/actuator/prometheus` | Metrics in Prometheus format (via Micrometer) |

```yaml
management:
  endpoints.web.exposure.include: health,info,metrics,prometheus
  endpoint.health.show-details: when-authorized
```

**Two points that earn marks:**
- *"By default **only `/health` and `/info` are exposed over HTTP** — the rest are opt-in. That's a deliberate secure default."*
- *"**`/env` and `/heapdump` must never be publicly reachable** — they expose connection strings, passwords and everything in memory. I'd put actuator on a separate management port and behind auth. It's a real breach vector, not a theoretical one."*

**Custom health check — a likely follow-up:**
```java
@Component
public class PaymentGatewayHealth implements HealthIndicator {
    @Override
    public Health health() {
        return gateway.ping()
            ? Health.up().withDetail("latencyMs", 12).build()
            : Health.down().withDetail("reason", "timeout").build();
    }
}
```

> 🔗 **[06 — Spring Boot](./06-spring-boot.md)**

---

### 7. Difference between Association and Aggregation in OOP

**The key sentence:** *"Association is the general relationship; **aggregation and composition are two specialised kinds of association**, and they differ by who owns the lifecycle."*

| | **Association** | **Aggregation** | **Composition** |
|---|---|---|---|
| Relationship | "uses-a" / general link | **"has-a"**, weak ownership | **"part-of"**, strong ownership |
| Lifecycle | Fully independent | Part **survives** the whole | Part **dies with** the whole |
| UML | plain line | hollow ◇ diamond | filled ◆ diamond |
| Example | Teacher ↔ Student | Department has Employees | House has Rooms / Car has Engine |
| Who creates the part? | Neither owns it | Passed in from outside | Created **inside** the owner |

```java
// ── ASSOCIATION — two independent classes that just interact
class Teacher {
    void teach(Student s) { }        // no ownership either way
}

// ── AGGREGATION — Department HAS employees, but they outlive it
class Department {
    private List<Employee> employees;

    Department(List<Employee> employees) {   // 🔑 passed IN — created elsewhere
        this.employees = employees;
    }
}
// Delete the Department → the Employee objects still exist and can join another one.

// ── COMPOSITION — the Engine cannot exist without the Car
class Car {
    private final Engine engine = new Engine();   // 🔑 created INSIDE, owned

    class Engine { }
}
// Destroy the Car → the Engine goes with it. Nothing else holds a reference.
```

> 🔴 **The discriminator to say out loud:** *"The test I use is — **can the part exist on its own after the whole is destroyed?** If yes it's aggregation, if no it's composition. And practically, look at **who calls `new`**: if the object is passed into the constructor it's aggregation; if the owner creates it internally, it's composition."*

**The likely follow-up — "which do you prefer?"**
> *"Composition, generally — 'favour composition over inheritance'. Inheritance is a compile-time IS-A that you can't change; composition is a runtime HAS-A that you can swap, which makes it far more testable. That's the Strategy pattern, and it's the reason Spring's constructor injection is composition rather than a class hierarchy."*

> 🔗 **[17 — SOLID & Design Patterns](./17-solid-design-patterns.md)**

---

### 8. Total employees and average salary per department 🔴 *(SQL)*

> 🔗 **Full depth on this and every other SQL question → [36 — SQL: The Complete Interview File](./36-sql-interview-questions.md).**

**Given:**
```
dept     ( dept_id, dept_name )
employee ( emp_id, emp_name, salary, dept_id )
```

**The answer:**
```sql
SELECT  d.dept_id,
        d.dept_name,
        COUNT(e.emp_id)                    AS total_employees,
        ROUND(AVG(e.salary), 2)            AS average_salary
FROM    dept d
LEFT JOIN employee e ON e.dept_id = d.dept_id
GROUP BY d.dept_id, d.dept_name
ORDER BY d.dept_name;
```

> 🔴 **Three things here separate a junior answer from a mid-level one. Say them:**

**① `LEFT JOIN`, not `INNER JOIN`.**
> *"With an inner join, a department with **zero employees disappears from the result entirely**. The question says 'total employees per department', and zero is a valid answer — so I use a left join from `dept`."*

**② `COUNT(e.emp_id)`, not `COUNT(*)`.**
> *"This is the subtle one. With a `LEFT JOIN`, an empty department still produces one row with all-NULL employee columns. **`COUNT(*)` counts that row and returns 1**, which is wrong. `COUNT(column)` skips NULLs and correctly returns **0**."*

**③ Every non-aggregated column must be in `GROUP BY`.**
> *"`dept_name` is selected but not aggregated, so it has to be in the `GROUP BY`. MySQL is lenient about this by default; Postgres, SQL Server and MySQL in `ONLY_FULL_GROUP_BY` mode reject it."*

**Bonus:** `AVG` ignores NULLs, so an empty department returns `NULL` for average salary. If they want `0`:
```sql
COALESCE(ROUND(AVG(e.salary), 2), 0) AS average_salary
```

---

#### 🔥 The follow-ups they ask after this query — have these ready

**"Only departments with more than 5 employees?"** → `HAVING`, not `WHERE`:
```sql
GROUP BY d.dept_id, d.dept_name
HAVING COUNT(e.emp_id) > 5;
```
> *"`WHERE` filters **rows before** grouping; `HAVING` filters **groups after** aggregation. You can't put an aggregate in `WHERE` because it doesn't exist yet at that point."*

**"Employees earning more than their department's average"** → correlated subquery:
```sql
SELECT e.emp_name, e.salary, d.dept_name
FROM   employee e
JOIN   dept d ON d.dept_id = e.dept_id
WHERE  e.salary > (SELECT AVG(e2.salary)
                   FROM   employee e2
                   WHERE  e2.dept_id = e.dept_id);
```
…or the window-function version, which is faster and scores better:
```sql
SELECT emp_name, salary, dept_id
FROM ( SELECT e.*, AVG(salary) OVER (PARTITION BY dept_id) AS dept_avg
       FROM   employee e ) t
WHERE salary > dept_avg;
```

**"Second-highest salary"** — asked in a huge share of rounds:
```sql
SELECT MAX(salary) FROM employee
WHERE salary < (SELECT MAX(salary) FROM employee);          -- classic

SELECT DISTINCT salary FROM employee ORDER BY salary DESC LIMIT 1 OFFSET 1;   -- MySQL/Postgres

SELECT salary FROM (                                         -- handles ties correctly
    SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) rnk FROM employee
) t WHERE rnk = 2;
```

**"Highest-paid employee per department"**:
```sql
SELECT dept_id, emp_name, salary FROM (
    SELECT e.*, RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) rnk
    FROM   employee e
) t WHERE rnk = 1;
```

> 🔗 The Java Streams equivalent of that last one — *highest salary per department* — was **Altimetrik's Q1** and is in **[22 — Streams](./22-java-streams-coding-problems.md)**. Worth noticing that **the same problem gets asked in both SQL and Streams form.**

---

### 9. What is deadlock in multithreading?

> *"A deadlock is when two or more threads are each **holding a lock the other one needs**, so none of them can ever proceed. The application doesn't crash — it just hangs, which is what makes it nasty to diagnose."*

```java
public class DeadlockDemo {

    private static final Object LOCK_A = new Object();
    private static final Object LOCK_B = new Object();

    public static void main(String[] args) {

        new Thread(() -> {
            synchronized (LOCK_A) {                 // T1 takes A
                sleep(100);
                synchronized (LOCK_B) {             // ...then wants B
                    System.out.println("T1 done");
                }
            }
        }, "T1").start();

        new Thread(() -> {
            synchronized (LOCK_B) {                 // T2 takes B  ← OPPOSITE ORDER
                sleep(100);
                synchronized (LOCK_A) {             // ...then wants A
                    System.out.println("T2 done");
                }
            }
        }, "T2").start();
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
// Neither line ever prints. The JVM hangs.
```

**The four Coffman conditions — all four must hold, so breaking any one prevents deadlock:**

| # | Condition | How you break it |
|---|---|---|
| 1 | **Mutual exclusion** — a resource is held exclusively | Use immutable data / lock-free structures |
| 2 | **Hold and wait** — hold one lock while requesting another | Acquire everything at once, or nothing |
| 3 | **No preemption** — a lock can't be forcibly taken | **`tryLock` with a timeout** |
| 4 | **Circular wait** — A waits for B, B waits for A | 🔴 **Global lock ordering** — the standard fix |

**The two fixes, in order of preference:**

```java
// ✅ FIX 1 — LOCK ORDERING. Every thread takes locks in the same global order.
synchronized (LOCK_A) { synchronized (LOCK_B) { } }   // both threads, same order
```
> *"Lock ordering is the fix I'd reach for first — if every thread acquires in the same order, a cycle is impossible. For dynamic objects you can order by something stable like `System.identityHashCode`."*

```java
// ✅ FIX 2 — tryLock with a timeout, so a thread gives up instead of waiting forever
if (lockA.tryLock(1, TimeUnit.SECONDS)) {
    try {
        if (lockB.tryLock(1, TimeUnit.SECONDS)) {
            try { /* work */ } finally { lockB.unlock(); }
        } else { /* back off and retry */ }
    } finally { lockA.unlock(); }
}
```

**🔥 "How would you detect one in production?" — this is the follow-up, and it's where Q6 pays off:**
> *"Take a **thread dump** — `jstack <pid>`, or `/actuator/threaddump` if Actuator is on. The JVM literally prints `Found one Java-level deadlock:` and names the two threads and the locks. JConsole and VisualVM show it visually, and programmatically `ThreadMXBean.findDeadlockedThreads()` returns the thread ids — you can wire that into a health check."*

**"Deadlock vs livelock vs starvation?" — the standard trio follow-up:**

| | What happens |
|---|---|
| **Deadlock** | Threads blocked forever, waiting on each other. **CPU idle.** |
| **Livelock** | Threads keep responding to each other and never progress — like two people stepping aside in a corridor. **CPU busy**, no work done. |
| **Starvation** | A thread never gets scheduled or never wins the lock, because higher-priority/greedier threads keep taking it. |

> **The closing line that scores:** *"Honestly, the best prevention is not writing the locks at all — use `java.util.concurrent`: `ExecutorService`, `ConcurrentHashMap`, `BlockingQueue`, atomic classes. Most deadlocks come from hand-rolled `synchronized` blocks that a higher-level utility would have handled."*

> 🔗 **[05 — Core Java §Multithreading](./05-java.md)**

---

### 10. How do you improve API performance — backend and frontend?

> ⭐ **Answer this in layers and lead with "measure first."** That framing alone separates you from a candidate reciting a list.

**Open with:**
> *"First I'd measure rather than guess — where is the time actually going? Server processing, database, network, or rendering? I'd use Actuator metrics or APM on the backend and the browser Network/Performance tab on the frontend. Then I'd fix in this order, because that's roughly the order of impact."*

#### Backend

| Layer | What I'd do |
|---|---|
| 🔴 **Database** | Fix **N+1** with `JOIN FETCH` / `@EntityGraph`; **add indexes** on filtered and joined columns; **paginate** instead of returning everything; check the execution plan |
| **Payload** | Return **DTOs / projections**, not full entities — stop over-fetching columns and lazy relations |
| **Caching** | `@Cacheable` + Redis for hot reads; HTTP caching with **ETag / `Cache-Control`** so the client skips the call entirely |
| **Connections** | Tune the **HikariCP** pool — an undersized pool looks exactly like a slow database |
| **Concurrency** | `@Async` / non-blocking for independent work; move long jobs to a **queue** and return `202 Accepted` |
| **Chattiness** | Batch endpoints so the client makes 1 call instead of 20 |
| **Transport** | Gzip/Brotli compression; HTTP/2 |
| **Resilience** | Circuit breaker + timeouts *(→ Q3)* — a hung downstream call is a performance problem, not just a reliability one |

#### Frontend *(your strength — spend time here)*

| Technique | Why |
|---|---|
| 🔴 **`debounceTime` + `distinctUntilChanged`** | Stop firing a request per keystroke on search |
| 🔴 **`switchMap`** | **Cancels** the in-flight request when a new one starts — no wasted work, no out-of-order responses |
| **`shareReplay(1)`** | Multiple subscribers share one HTTP call instead of triggering several |
| **`forkJoin`** | Fire independent calls **in parallel** instead of sequentially |
| **HTTP interceptor cache** | Serve repeat GETs from memory |
| **Pagination / virtual scrolling** | Never render 10,000 rows |
| **`trackBy` in `*ngFor`** | Angular reuses DOM nodes instead of rebuilding the list |
| **`OnPush` change detection** | Cuts change-detection work dramatically on large trees |
| **Lazy loading + code splitting** | Smaller initial bundle → faster first paint |
| **Optimistic UI / skeletons** | *Perceived* performance — often the one users actually notice |

> 💡 **Close with something concrete from your own work:** *"On the Angular side I've used `debounceTime` with `switchMap` to stop a search box firing a request per keystroke — that's the same fix as the duplicate-API-call-on-rapid-clicks problem. And the biggest backend win I've seen is almost always the data layer: one N+1 or one missing index costs more than every micro-optimisation put together."*

> 🔗 **[20 — RxJS](./20-rxjs-operators.md)** · **[24 — ORM/JPA §N+1](./24-orm-jpa-hibernate.md)** · Altimetrik Q9 above (duplicate calls on rapid clicks) is the **same technique** — that's now a **2-round repeat**.

---

### 📌 What this round tells you

1. **It was a backend round with no Angular at all.** Five of ten questions were microservices/Spring (Q1–Q4, Q6). Your resume reads full-stack, but some companies will drill **only** the Java side — so Java/Spring depth can't be the part you revise last.
2. **Two of ten were hands-on** — a coding problem (Singleton) and a SQL query. Both are *writing*, not talking. **Type them, don't read them.**
3. **SQL appeared for the first time in six rounds.** Nothing in this pack covered it before today. If more rounds ask, that's a gap worth its own file — the joins/`GROUP BY`/`HAVING`/window-function set above is the core of it.
4. **Q3 and Q4 are the same topic split in two** — concept, then *"which dependency?"*. Expect that pattern: they check whether you've actually wired it up or only read about it. The `spring-boot-starter-aop` answer is the tell.
5. **Q10 was your best opportunity in the round** — an open question where you can lead with your real Angular experience. Prepare the layered answer; open-ended questions are where you can out-answer a backend-only candidate.

---

# 🟪 Virtusa — Round 2 (Java core), Aug 2026

> ❌ **Not cleared.** A **Java-fundamentals** round — no Angular, no framework. It went deep on **one topic** rather than broad, which is the opposite of the Tech Mahindra round below. Full answers: **[31 — HashMap Internals](./31-hashmap-internals.md)**, **[32 — Multithreading](./32-multithreading.md)**, **[33 — Interface vs Abstract Class](./33-interface-vs-abstract-class.md)**.

### 1. How does HashMap internally work?
The 5 beats they wanted: **`Node<K,V>[] table`** (array of buckets) → **`hash()` spreads** with `h ^ (h >>> 16)` → index via **`(n - 1) & hash`** (power-of-two capacity makes `&` a fast modulo) → each bucket is a **linked list of `Node`s** chained by `next` → **treeified at 8** nodes if the table is ≥ 64 → **resize doubles at load factor 0.75**. Average O(1), worst O(log n). → **[31 §Part 0](./31-hashmap-internals.md)**

### 2. What is a bucket/bin in a HashMap?
**One slot of the internal array — `table[i]`.** Not a class. It holds either `null`, the head `Node` of a linked list, or the root `TreeNode` of a red-black tree. → **[31 §Part 2](./31-hashmap-internals.md)**

### 3. What is the internal structure of a bucket?
A chain of `Node<K,V>` objects: `final int hash` (cached — computed once), `final K key`, `V value`, `Node<K,V> next`. The `next` pointers *are* the linked list; there is no separate list object. After treeification the nodes become `TreeNode`s, which keep the `next`/`prev` links **as well as** the tree pointers — which is how a bin can be untreeified cheaply on resize. → **[31 §Part 1](./31-hashmap-internals.md)**

### 4. Does a bucket contain a linked list?
**Yes by default — but not always.** A singly linked list of `HashMap.Node` objects (⚠️ **not** a `java.util.LinkedList` — that answer sounds right and is wrong). Since Java 8, a bucket holding **8** nodes in a table of length **≥ 64** converts to a **red-black tree** — O(n) → O(log n), which was added both for performance and as a defence against hash-collision DoS. It reverts to a list at **6** during a resize; the 8/6 gap prevents convert-revert thrashing. → **[31 §Part 5](./31-hashmap-internals.md)**

> **Also probed in this round:** multithreading and **interface vs abstract class** — hence files **[32](./32-multithreading.md)** and **[33](./33-interface-vs-abstract-class.md)**.

> 💬 **What this round says:** Virtusa drilled **one** topic to the floor. That is the "second question" pattern from §What the pattern says, in its purest form — the opening question is easy and the round is decided by the **third** follow-up. HashMap internals has now been asked at **Altimetrik, Mphasis L1 and Virtusa R2** — it is the single most-repeated Java question in this log.

---

# 🟨 Tech Mahindra — Angular Developer round, Aug 2026

> 🟡 **RESULT PENDING.** A **breadth sweep** — 15 short questions across Angular, TypeScript, the HTTP layer, core JavaScript and HTML/CSS. Full answers to every one: **[34 — Tech Mahindra Angular Round](./34-techmahindra-angular-round.md)**.

| # | Question | Notes |
|---|---|---|
| 1 | State management | Answer as a **ladder**: component → `@Input`/`@Output` → service + `BehaviorSubject` → NgRx |
| 2 | NgRx and its features | Store, actions, reducers, selectors, effects + `@ngrx/entity`, DevTools, ComponentStore |
| 3 | "Any idea of TypeScript?" | Don't answer "yes" — 45 seconds of range: interfaces, generics, unions, utility types, strict mode |
| 4 | HTTP interceptor and its benefits | Auth, errors, loading, logging, retry, caching + **clone the request**, order, `multi: true` |
| 5 | String interpolation | `{{ }}`, one-way, sanitized, no side-effects; **don't call methods in templates** |
| 6 | Observables vs Promises | ⭐ **4th round in a row** — stream/lazy/cancellable/operators vs single/eager/uncancellable |
| 7 | HttpClient — features & benefits | Observables, typed JSON, interceptors, `HttpErrorResponse`, progress events, testing, XSRF |
| 8 | Callback, callback hell | Pyramid of doom → Promises → async/await → RxJS; add `Promise.all` for independent calls |
| 9 | Event capturing and bubbling | 3 phases, `addEventListener(..., true)`, `stopPropagation` vs `preventDefault`, delegation |
| 10 | ES5 vs ES6 | `let`/`const`, arrows, classes, template literals, destructuring, spread, Promises, modules |
| 11 | Arrow functions | The point is **lexical `this`** — plus no `arguments`, not a constructor, wrong for object methods |
| 12 | OOP in TypeScript | 4 pillars + `private` vs `#private`, **structural typing**, one class / many interfaces |
| 13 | Modules — how, and types | Both meanings: ESM/CommonJS/AMD/UMD **and** Angular root/feature/shared/core/routing/lazy + standalone |
| 14 | `unknown` type and `never` type | `unknown` = type-safe `any` (narrow before use); `never` = no value → **exhaustiveness check** |
| 15 | HTML and CSS | Semantics, box model, positioning, Flexbox vs Grid, specificity, responsive |

> 💬 **What this round says:** a **breadth** round is won with crisp 3-sentence answers, not long ones — and it confirms the repeat list. **Observables vs Promises has now been asked in 4 rounds; interceptors in 3; state management/NgRx in 3.** Those aren't "likely" questions any more, they're certainties. If Tech Mahindra calls back, expect the inverse: 3 of these drilled deep, most likely NgRx effects, `switchMap` vs `mergeMap`, and a live component.

---

# 🟨 Altimetrik — Fullstack Java + Angular round (31 Aug 2026)

> 🆕 **Attended Sun 31 Aug 2026 · result pending. This is your SECOND Altimetrik round** — the first (Level 1 Java + Angular, 24 questions) is [§ above](#-altimetrik--level-1-round-java).
> 📄 **Every one of these 22 questions is answered in full in → [37 — Altimetrik Fullstack Java + Angular (31 Aug)](./37-altimetrik-fullstack-java-angular-31aug.md).**
>
> **Shape:** **2 Java (records + pattern matching)** → **10 Spring/Spring Boot** → 2 microservices → 1 config/environments → 6 Angular → 1 Java Streams live-coding. Not a breadth sweep — a *depth* round on Spring wiring.

| # | Question | Notes |
|---|---|---|
| 1 | 🔴 **What is a `record`? Why are we using it?** | **The opening question.** Transparent immutable data carrier (Java 16) — compiler generates ctor, `name()` accessors, `equals`/`hashCode`/`toString`; implicitly `final`; compact constructor for validation. **Why:** kills DTO boilerplate, immutable/thread-safe/safe map key, correct `equals` for free, signals intent. Traps: **shallow immutability**, `name()` not `getName()`, **can't be a JPA `@Entity`**; pairs with sealed types + pattern matching |
| 2 | Pattern matching | `instanceof` pattern (**16**), `switch` patterns + `when` guards + record patterns (**21**); killer combo = `sealed` + switch → compiler-checked exhaustiveness |
| 3 | Spring Boot annotations | Answer in **groups** (bootstrap / stereotype / DI / web / validation / JPA / config / test), close with "they're metadata — proxies do the work, which is why self-invocation breaks `@Transactional`" |
| 4 | `@Component` and `@Service` | All stereotypes **are** `@Component`; **`@Repository` is the only one with real behaviour** (exception translation); `@Service` = intent + AOP pointcut |
| 5 | Multiple packages — how does Spring read all the annotations? | Default scan = main class's package + sub-packages → `scanBasePackages`, **`scanBasePackageClasses`** (refactor-safe), filters; **plus `@EntityScan` + `@EnableJpaRepositories`**, and auto-configuration for a shared jar |
| 6 | Reading the bean annotations (`@Bean`) | Stereotype = Spring builds it (your classes) · `@Bean` = you build it (third-party); bean name = method name; the `proxyBeanMethods` trap; bean lifecycle order |
| 7 | `@Autowired`, `@Qualifier`, `@Primary` | Resolution is **by type first** → `NoUniqueBeanDefinitionException`. Precedence: **`@Qualifier` > `@Primary` > bean name**. Constructor injection; inject `Map`/`List` of all impls |
| 8 | `CrudRepository` and `JpaRepository` | `JpaRepository` → `PagingAndSortingRepository` → `CrudRepository`; `List` vs `Iterable`, paging, `deleteAllInBatch`, `saveAndFlush`; `SimpleJpaRepository` behind a proxy |
| 9 | Pagination methods | `Page<T> findByX(..., Pageable)`; `PageRequest.of(page, size, Sort)`; Boot binds `Pageable` from `?page&size&sort`; **OFFSET degrades → keyset paging**; pagination + `JOIN FETCH` = in-memory paging |
| 10 | What classes are created in pagination | `Pageable` · **`PageRequest`** · `Sort`/`Sort.Order`/`Direction` · `Page`/**`PageImpl`** · `Slice`/`SliceImpl` · `Window`/`ScrollPosition`. ⭐ **`Page` runs 2 queries (incl. `COUNT`), `Slice` runs 1** |
| 11 | Communication in microservices | Sync (REST, `WebClient`/`RestClient`, **Feign**, gRPC) vs async (**Kafka**/Rabbit/SQS) + discovery, gateway, config server, Resilience4j, tracing, Saga. Rule: *sync when I need the answer now, async when B just needs to eventually know* |
| 12 | How do you handle error cascading? | Latency problem before an error problem: **timeouts → circuit breaker → retry w/ backoff → bulkhead → fallback**, `@RestControllerAdvice` + `ProblemDetail` + trace ID |
| 13 | ⭐ Two microservices, network failure — A errors, how do you inform B? | **The sharpest question.** Caller can't tell "never arrived" from "response lost" → idempotency key. To *reliably* inform B: **transactional outbox** + Kafka + idempotent consumer + **DLQ**, compensating transaction if it can't complete |
| 14 | Dev/QA/Prod properties, DB changed per environment | `application-{profile}.yml` + `SPRING_PROFILES_ACTIVE`; **one jar, many environments** — env vars/CLI override; `@Profile` beans; Config Server / ConfigMaps; secrets in Vault; **Flyway + `ddl-auto: validate` in prod** |
| 15 | ⭐ `ngOnInit` and constructor | Constructor = **DI only, `@Input`s are `undefined`**; `ngOnInit` = after first `ngOnChanges`, inputs available → API calls. Full hook order. **5th round running** |
| 16 | Decorator | Metadata function: class / property / method / parameter. `@Injectable({providedIn:'root'})` = tree-shakable; Angular 16+ moving to `input()`/`output()` signal functions |
| 17 | ⭐ Interceptor | Functional `HttpInterceptorFn` + `withInterceptors`; **`req.clone()` — requests are immutable**, `multi: true`, order reverses on the response. **4th round running** |
| 18 | Token expired — how do you notify the user? | 401 in the interceptor → **silent refresh** → toast + redirect to `/login` with `returnUrl`; proactive `exp` decode → "session expiring" dialog; auth guard; **gate the refresh stampede** with a `BehaviorSubject` |
| 19 | How do you redirect to a page? | `routerLink` · `router.navigate`/`navigateByUrl` (+`replaceUrl`) · `redirectTo` with **`pathMatch: 'full'`** (else infinite loop) · guard returning a **`UrlTree`** · `window.location` for external |
| 20 | ⭐ Angular core — data binding | 4 types: interpolation · property · event · two-way; `[(x)]` = `[x]` + `(xChange)`; don't call methods in templates; signals version |
| 21 | ⭐ Lazy loading | `loadChildren`/`loadComponent`; **never also import the lazy module eagerly**; `PreloadAllModules` / custom strategy; disadvantages: first-nav delay, duplicated deps, per-module service instance, `ChunkLoadError`; `@defer` |
| 22 | 🔴 **Highest-paid employee per department → department + name** | `groupingBy` + `collectingAndThen(maxBy(...), e -> e.map(Employee::getName))`, or the cleaner 3-arg `toMap` + `BinaryOperator.maxBy`. **Ties → `maxBy` keeps one.** Output: `IT -> Peter`, `Sales -> Liza` |

> 💬 **What this round says:** Altimetrik tested **Spring wiring you can only know from building** — scanning across packages, `@Qualifier` vs `@Primary`, the repository hierarchy, the *classes* inside pagination. And **Q22 is the third time you've been asked highest-salary-per-department** (Altimetrik R1 in Streams, Mphasis in SQL, here again). Together with lifecycle hooks (**5 rounds**), interceptors (**4**), data binding (**4**) and lazy loading (**3**), the repeat list is now undeniable. The new muscle to build is **microservice failure vocabulary**: timeout · circuit breaker · retry+backoff · idempotency key · **transactional outbox** · DLQ · compensating transaction.

---

# 🟦 Capgemini — Java + Spring + Angular round (Sept 2026)

> 🆕 **Attended early Sept 2026 · result pending.** A **breadth sweep**, not a depth round: 2 Java core → 4 Spring/architecture → 3 Angular → 1 Streams live-coding.
> 📄 **All 10 questions are answered in full in → [41 — Capgemini Java + Spring + Angular](./41-capgemini-java-angular-3sep.md).**
>
> ⚠️ **Q10 was not written in the round** — the program is now solved four ways in [41 §Q10](./41-capgemini-java-angular-3sep.md#10--live-coding--second-non-repeating-character-from-the-end). **Type it.**
> 🔁 **7 of the 10 were repeats** of questions already in this file, and **two of them (`@Component` vs `@Bean`, and dev/QA/prod config) had been asked at Altimetrik four days earlier.**

| # | Question | Notes |
|---|---|---|
| 1 | **Java 8 new features** | Answer in **5 groups**: lambdas + functional interfaces · **Stream API** · interface `default`/`static` methods · `Optional` · `java.time`. Traps: streams are **lazy**, a stream is **single-use**, `parallelStream()` uses the shared common ForkJoinPool. Memory hook: **L.S.I.O.D.** |
| 2 | **HashMap and LinkedHashMap** | Same family, one difference — **order**. `HashMap` none · `LinkedHashMap` **insertion order** (`HashMap` + a doubly-linked list; `accessOrder=true` + `removeEldestEntry` = **LRU cache**) · `TreeMap` sorted, O(log n). Internals: hash → `& (n-1)` → bucket → list → **treeify at 8 with table ≥ 64**, load factor 0.75. ⚠️ mutable keys, `equals`/`hashCode` as a pair |
| 3 | **Setter injection and constructor injection** | **Constructor = mandatory** (fields `final`, fail-fast at startup, testable with plain `new`, `@Autowired` optional since 4.3); **setter = optional** with a default. **Field injection ❌.** ⭐ Circular deps: constructor → `BeanCurrentlyInCreationException` at startup (good); setter hides it — and Boot **2.6+ disallows circular refs by default** |
| 4 | **`@Component` and `@Bean`** | `@Component` = **class**, Spring builds it, found by scanning, name = decapitalised class · `@Bean` = **method** in `@Configuration`, **you** build it, name = method name — **the only way to register a third-party class**, and the way to do conditional/multiple instances. ⭐ The **`proxyBeanMethods`** trap: calling one `@Bean` method from another returns the *same* singleton |
| 5 | **How do microservices load values?** | Externalised config — **one artefact, many environments**. `@Value` · **`@ConfigurationProperties` (type-safe, validated) ✅** · `Environment` · `@Profile`. ⭐ **Precedence: CLI args > env vars > external `application-{profile}.yml` > external `application.yml` > the ones in the jar > defaults**, with **relaxed binding** (`payment.timeout-ms` == `PAYMENT_TIMEOUT_MS`). At scale: **Spring Cloud Config Server on git** + `@RefreshScope` + Cloud Bus; K8s ConfigMap/Secret; **secrets in Vault / AWS Secrets Manager, never in git** |
| 6 | **Monolithic and microservices** | One deployable + one DB vs many deployables + **database per service**. Microservices are an **organisational** answer first; the cost is that a method call becomes a network call (slow · fails · arrives twice) and `@Transactional` becomes a **Saga**. Name the **distributed monolith** anti-pattern. 🔴 **RoboGebra is a MODULAR MONOLITH** — no Eureka, Feign, circuit breaker or Kafka. Say so |
| 7 | **Components in Angular** | `@Component` decorator + class = one piece of screen + its logic. The 4 files, the metadata (`selector`, `providers` = **new instance per component**, `changeDetection: OnPush`, `encapsulation`), the component **tree**, and communication: `@Input` ↓ · `@Output`/`EventEmitter` ↑ · shared service + `BehaviorSubject` · `@ViewChild`. ⚠️ Honesty guard: your codebase is **NgModule-based, no signals** |
| 8 | **How to create a new component** | `ng g c student-card` → **4 files + it registers the class in the nearest NgModule's `declarations`** (say that fourth step — it's the actual answer). Flags: `--dry-run`, `--skip-tests`, `--module`, `--standalone`, `--change-detection=OnPush`, `--flat`. ⚠️ `'app-x' is not a known element` = not declared · declared but not **`exported`** · typo |
| 9 | ⭐ **`ngOnChanges` and `ngDoCheck`** | `ngOnChanges` = only when an **`@Input` reference** changes, gets `SimpleChanges` (`previousValue`/`currentValue`/`firstChange()`); `ngDoCheck` = **every change-detection cycle**, no arguments, you do the comparing. ⭐ **Angular compares inputs with `===`** — so a *mutated* object never fires `ngOnChanges`, and that's why `ngDoCheck` exists. Keep it cheap; better still, make inputs **immutable** and use `OnPush`. **2nd round running** (Photon asked it too) |
| 10 | 🔴 **`String name = "Priyanka"` — 2nd non-repeating character from the END, using Stream API** | **Answer: `n`.** Counts: `a=2`, everything else 1 → non-repeating in order `P r i y n k` → from the end: `k`, then **`n`**. **Stream API in 3 simple steps:** **① count** each character — `groupingBy(c -> c, LinkedHashMap::new, counting())` → `{P=1, r=1, i=1, y=1, a=2, n=1, k=1}` · **② keep count == 1** → `[P, r, i, y, n, k]` · **③ take `single.get(single.size() - 2)`** → **`n`**. ⭐ **`LinkedHashMap` is the whole trick** — a plain `HashMap` loses the order and "2nd from the end" stops meaning anything. **O(n).** ⚠️ **Not written in the round** — [simple solution, explained line by line](./41-capgemini-java-angular-3sep.md#-the-program--the-simple-one-write-this) |

> 💬 **What this round says:** nine questions you already had answers for, then **one program that decides the outcome**. That is now the pattern at Altimetrik, Photon, Codeboard **and** Capgemini. Theory gets you to the coding question; the coding question gets you the offer. **Type one problem a day from [22 — Java Streams](./22-java-streams-coding-problems.md)** — reading them is a different skill from writing them under a shared screen.

---

## 🔵 Next up — IQVIA (Angular / Ionic Technical Lead)

> **They gave you the syllabus.** Ten areas, from Angular/Signals to Technical-Lead behaviour to AI-assisted development — all prepared in **[38 — IQVIA Full Prep Pack](./38-iqvia-technical-lead-prep.md)**.
> **~75% of that checklist is frontend**, which is your strongest ground, plus a lead layer (code review, mentoring, incidents, tech debt, stakeholders) that is about judgement rather than syntax. **Log every question here the same day.**

---

## ➕ Template — add your next round here

```markdown
# 🔵 <Company> — <Round name> (<date>)

### 1. <Question exactly as asked>
<answer>

### 2. ...
```

**Log it the same day.** Note not just the question but:
- **which ones you fumbled** (mark them ⚠️ — those are your next study list),
- **the follow-up** they asked after your answer (that's where the real signal is),
- whether it was **theory or output/coding**.

---

## ✅ Pre-interview 20-minute revision path

1. **[§ Most-repeated](#-most-repeated-questions-across-all-companies)** — always, every time.
2. The section for **this company** if you've interviewed there before.
3. If it's a **Java** round → **[23 — Predict the Output](./23-java-output-tricky-questions.md)**.
4. If it's an **Angular** round → lifecycle hooks, component communication, `Subject` vs `BehaviorSubject`, unsubscribing, forms.
5. If it's an **Ionic** round → `ionViewWillEnter` vs `ngOnInit`, Capacitor, offline support.
6. If it's a **Spring** round → `@Controller` vs `@RestController`, validation, global exception handling, `PUT` vs `PATCH`.
6b. If it's a **Spring depth** round (Altimetrik-style wiring questions) → **[37 Parts 3](./37-altimetrik-fullstack-java-angular-31aug.md)** — component scanning across packages, `@Qualifier` vs `@Primary`, `CrudRepository` vs `JpaRepository`, the pagination classes.
7. If it's a **microservices/backend** round → Mphasis §Q1–Q4 + Q6 (microservices, patterns, circuit breaker + its dependency, Actuator), thread-safe Singleton, deadlock.
8. If **SQL** is likely → **[36 — SQL: The Complete Interview File](./36-sql-interview-questions.md)** — read **Part 0** (the two questions you were actually asked) and **Part 24** (the cheat sheet). Locally: Mphasis §Q8 below — `LEFT JOIN` + `COUNT(column)`, `WHERE` vs `HAVING`, second-highest salary, `RANK()`/`PARTITION BY`.

---

**Related files:** [16 — Codeboard §0](./16-codeboard-technology-level1.md) · [23 — Java Output](./23-java-output-tricky-questions.md) · [25 — Binding & Forms](./25-angular-binding-forms.md) · [20 — RxJS](./20-rxjs-operators.md) · [22 — Streams](./22-java-streams-coding-problems.md) · [15 — Ionic](./15-ionic-level1.md)
