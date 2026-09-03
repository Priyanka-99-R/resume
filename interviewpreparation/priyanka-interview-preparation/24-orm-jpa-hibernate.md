# ORM — JPA, Hibernate, Spring Data JPA (Easy Version) (+ ODM: MongoDB & Mongoose)

> 🎯 **Why this file exists:** "Have you worked with ORM?" is asked in almost every Java/Spring round, and the follow-ups (`persist` vs `merge`, N+1, `LazyInitializationException`, `@Transactional`) are where candidates get exposed.
> **Your situation is specific:** your current project **RoboGebra uses MongoDB — that is an ODM, not an ORM**. This file gives you (a) the honest one-liner so you never get caught claiming JPA on RoboGebra, and (b) full ORM depth so you can still answer every JPA/Hibernate question confidently.

Sections **B–K** follow the easy format — **Easiest way to remember → explanation → real-world example → diagram → trap → Easy memory box.**
Section **L** is already in quiz format (code → answer → rule → follow-up) and is kept that way, because that is exactly how it gets asked.

---

## Table of contents

| # | Section | What it covers |
|---|---|---|
| [A](#a--your-honest-position-read-this-first) | **Your honest position** | what you have really used — RoboGebra = ODM. Exact wording. |
| [B](#b--orm-fundamentals) | ORM fundamentals | what/why, impedance mismatch, ORM vs JDBC vs ODM |
| [C](#c--jpa-vs-hibernate-vs-spring-data-jpa) | JPA vs Hibernate vs Spring Data JPA | spec vs implementation vs abstraction (top-3 asked) |
| [D](#d--entity-mapping-reference) | Entity mapping | `@Entity`, ids, generators, `@Column`, `@Embeddable`, inheritance, enums |
| [E](#e--entity-lifecycle--the-persistence-context) | **Lifecycle & persistence context** | 4 states, dirty checking, flush, `persist` vs `merge` vs `save` — the heart of ORM |
| [F](#f--relationships) | Relationships | all 4, owning side, `mappedBy`, cascade, `orphanRemoval`, helpers |
| [G](#g--fetching-n1-and-lazyinitializationexception) | Fetching & N+1 | LAZY/EAGER defaults, N+1 with 4 fixes, `LazyInitializationException` |
| [H](#h--transactions) | Transactions | `@Transactional`, propagation, isolation, rollback rules, self-invocation |
| [I](#i--caching) | Caching | L1 vs L2 vs query cache |
| [J](#j--concurrency--locking) | Locking | optimistic `@Version`, pessimistic locks |
| [K](#k--equals-hashcode-lombok--dto-traps) | equals/hashCode & Lombok | the `@Data`-on-entity disaster, DTO vs entity |
| [L](#l--predict-the-output--orm-edition-20-programs) | 🔴 **Predict the output (20)** | same style as file 23, but ORM |
| [M](#m--odm-side--what-you-actually-use-mongodb--mongoose) | **ODM — what you actually use** | Spring Data MongoDB (RoboGebra), Mongoose (CRM) |
| [N](#n--schema-management--migrations) | Schema & migrations | `ddl-auto` values, Flyway/Liquibase |
| [O](#o--performance-checklist) | Performance checklist | the 12 things that make ORM slow |
| [P](#p--rapid-fire-answer-table) | Rapid-fire table | 1-line answers for last-minute revision |
| [Q](#q--12-self-test-drills-answers-hidden) | 12 drills | answers hidden — test yourself |
| [R](#r--answer-scripts-say-these-out-loud) | Answer scripts | word-for-word answers for the 6 likely questions |

---

# A — Your honest position (read this first)

### The truth about your stack

| Project | Persistence | Is it ORM? |
|---|---|---|
| **RoboGebra** (current) | **MongoDB** + Spring Data MongoDB (`MongoRepository`, `@Aggregation`) | ❌ **ODM** (Object–**Document** Mapper) |
| **RoboGebra CRM** (Node) | **Mongoose** on MongoDB | ❌ **ODM** |
| **EasyVisa** | Postgres + Neo4j behind a **Grails/Groovy** backend (GORM). You worked on the **Angular** side. | ⚠️ ORM existed, but **not your code** |
| **Subsea / Backoffice** | — | — |

> ⚠️ **There is an unused `spring-data-jpa` dependency in the RoboGebra POM** — but no JPA starter, no `DataSource`, and **no `@Entity` classes**. Do not say "we use JPA on RoboGebra." (Same warning as in [18 — RoboGebra Technical Stack](./18-robogebra-technical-versions.md), trap #5.)

### ✅ Say this when asked "Have you worked with ORM / Hibernate?"

> *"My hands-on object-mapping work is with **Spring Data MongoDB** on RoboGebra — that's an **ODM** rather than an ORM: `@Document` models, `MongoRepository` derived queries and `@Aggregation` pipelines. I'm solid on **JPA/Hibernate concepts** — entity lifecycle, the persistence context, relationship mapping, lazy loading and the N+1 problem, `@Transactional` — and I've worked against a Grails/GORM + Postgres backend on EasyVisa from the frontend side. I haven't shipped a large Hibernate codebase myself, but I can pick it up quickly because Spring Data's repository model is identical across both."*

**Why this works:** it's honest, it names the *concepts* (which is what they're actually testing), and it immediately gives you credibility on the part you *do* own.

### ❌ Never say

```
❌ "Yes, we use Hibernate on RoboGebra."
      → one follow-up ("show me an entity") and you're finished 💥

❌ "MongoDB is an ORM."
      → it's an ODM. This exact slip is a common junior tell.

❌ "Spring Data JPA and Spring Data MongoDB are the same thing."
      → same programming model, completely different engine —
        no persistence context, no lazy loading, no dirty checking in Mongo.
```

### If they push: "So you've never written an `@Entity`?"

> *"Not in production, no — my production persistence is document-based. I've written JPA entities while learning and I understand the mapping model well. What I'd want to be careful about on day one is the things that bite you at scale: lazy loading boundaries, N+1 queries, and transaction demarcation."*

⭐ Naming the *risks* is what a 4-year developer sounds like. Naming nothing is what a fresher sounds like.

---

# B — ORM fundamentals

### Q: What is ORM?

The easiest way to remember:

```
ORM = a TRANSLATOR between Java objects and database tables.

class  → table
field  → column
object reference → foreign key
```

```java
class User { Long id; String name; }        ORM        │ users        │
                                            ⇄          │ id | name    │
new User(1, "Priya")                                   │ 1  | Priya   │
```

Without ORM you write SQL, then manually convert every `ResultSet` row into an object. With ORM you work with objects and the framework writes the SQL.

Real-world idea: an **interpreter in a meeting.** You speak Java; the database speaks SQL. The ORM sits in the middle and translates both ways.

---

### Q: What problem does ORM solve? (the "impedance mismatch")

The easiest way to remember:

```
Objects and tables think DIFFERENTLY about the same data.
That gap is the "object–relational impedance mismatch".
```

The five disagreements:

| Mismatch | Objects | Relational tables |
|---|---|---|
| **Granularity** | many small classes (`Address` inside `User`) | fewer, wider tables |
| **Inheritance** | `Employee extends Person` | ❌ no inheritance at all |
| **Identity** | `==` (reference) *and* `equals()` | primary key only |
| **Associations** | directional references (`user.getOrders()`) | foreign keys — inherently bidirectional |
| **Navigation** | walk the graph: `a.getB().getC()` | joins; walking = N queries ⚠️ |

```
JAVA WORLD                       DATABASE WORLD
──────────                       ──────────────
inheritance ✅                    inheritance ❌
object references                foreign keys
a.getB().getC()  (free)          each hop = another QUERY 💥
```

⭐ That last row is the seed of the **N+1 problem** — in Java, walking a graph is free; in SQL, every hop is a round trip.

Real-world idea: translating **Tamil to English**. Both languages express the same ideas, but the grammar doesn't line up — some things need restructuring, and a literal word-for-word translation produces nonsense.

#### Easy memory

```
Impedance mismatch = objects and tables DISAGREE in 5 ways ⭐
Granularity | Inheritance | Identity | Associations | Navigation

Navigation is the dangerous one:
   a.getB().getC() is free in Java, but 2 QUERIES in SQL → N+1 💥
```

---

### Q: Advantages and disadvantages of ORM?

**Advantages**

```
✅ Removes JDBC boilerplate (Connection, PreparedStatement, ResultSet, mapping)
✅ DATABASE PORTABILITY — the dialect handles vendor SQL differences
✅ Built-in caching, dirty checking, lazy loading, transaction management
✅ Type safety + refactorability — rename a field, the compiler finds every usage
✅ Automatic SQL-injection protection (bind parameters)
```

**Disadvantages**

```
❌ HIDES THE SQL — it's easy to generate awful queries (N+1) without noticing ⭐
❌ Real learning curve; the "magic" bites when you don't know the persistence context
❌ Poor fit for BULK operations and heavy reporting → use native SQL or JDBC there
❌ Extra memory and CPU overhead vs plain JDBC
```

```
50 lines of JDBC per table          →  ORM: 1 entity + 1 repository interface ✅
                                       but the SQL is now INVISIBLE ⚠️
```

> 💬 **Great line to say:** *"ORM removes boilerplate, not the need to understand SQL. The two things I always check are the generated SQL in the logs and whether any collection access is causing N+1."*

#### Easy memory

```
✅ no boilerplate | portable | caching + dirty checking | type safe | no SQL injection
❌ HIDES the SQL (N+1) ⭐ | learning curve | bad for bulk/reporting | overhead

Line to say: "ORM removes boilerplate, not the need to understand SQL." ⭐
```

---

### Q: ORM vs JDBC vs ODM?

The easiest way to remember:

```
JDBC → you write SQL yourself           → rows and columns
ORM  → objects ↔ TABLES                 → JPA/Hibernate (relational)
ODM  → objects ↔ DOCUMENTS              → MongoDB/Mongoose ⭐ (what you use)
```

| | **JDBC** | **ORM (JPA/Hibernate)** | **ODM (Spring Data MongoDB / Mongoose)** |
|---|---|---|---|
| Data model | rows/columns | objects ↔ tables | objects ↔ **documents** (BSON/JSON) |
| You write | SQL + mapping code | entities + JPQL/derived queries | documents + derived queries/aggregations |
| Schema | you manage it | generated/validated from entities | schema-flexible (Mongoose can enforce it) |
| Joins | SQL joins | relationship mapping + fetch strategies | embed, or `$lookup` / `populate()` |
| Persistence context | none | **Yes** — L1 cache + dirty checking ⭐ | **No** — you must call `save()` ⭐ |
| Lazy loading | none | yes (proxies) | only `@DBRef(lazy = true)`; usually you embed instead |
| Transactions | manual | declarative `@Transactional` | needs a **replica set** (Mongo 4.0+) |

⭐ The two rows that matter most for you: Mongo has **no persistence context** and **no dirty checking** — so in Mongo you must always call `save()` explicitly, whereas in JPA an entity loaded inside a transaction updates itself.

#### Easy memory

```
JDBC → raw SQL | ORM → objects ↔ TABLES | ODM → objects ↔ DOCUMENTS ⭐

The two ODM differences to name:
   NO persistence context  → you MUST call save() ⭐
   NO dirty checking / lazy loading
```

---

# C — JPA vs Hibernate vs Spring Data JPA

> 🔴 **This is the single most-asked ORM question. Have the answer memorised.**

The easiest way to remember:

```
JPA             = the RULE BOOK      (specification — interfaces + annotations)
Hibernate       = the PLAYER         (implementation — actually writes the SQL)
Spring Data JPA = the COACH          (removes the boilerplate on top)
```

| | **JPA** | **Hibernate** | **Spring Data JPA** |
|---|---|---|---|
| What is it | a **specification** | an **implementation** of JPA | a **Spring abstraction on top of JPA** |
| Ships as | `jakarta.persistence-api` | `hibernate-core` | `spring-boot-starter-data-jpa` |
| Gives you | `EntityManager`, `@Entity`, JPQL | the engine + extras (`Session`, HQL, Criteria, L2 cache, `@BatchSize`) | **repository interfaces**, derived queries, pagination, auditing |
| Can run alone? | ❌ it's just an API | ✅ | ❌ needs a JPA provider |

#### The layering — draw this

```
  Your UserRepository (an interface you wrote)
            ↓
  Spring Data JPA        ← generates the implementation ⭐
            ↓
  JPA API (EntityManager)← the standard
            ↓
  Hibernate              ← generates the actual SQL ⭐
            ↓
  JDBC
            ↓
  Database
```

Real-world idea:

```
JPA       → the DRIVING RULES (the law says: drive on the left)
Hibernate → the CAR that actually drives
Spring Data JPA → the CHAUFFEUR who drives it for you ⭐
```

### ✅ The memorised answer

> *"**JPA is the specification** — the standard API and annotations. **Hibernate is the implementation** that actually generates the SQL; it's the default provider in Spring Boot. **Spring Data JPA sits on top** and removes the DAO boilerplate — I declare a repository interface and Spring generates the implementation at runtime. So the layering is: my repository → Spring Data JPA → JPA API → Hibernate → JDBC → database."*

---

### Q: Other JPA implementations?

**EclipseLink** (the reference implementation) and **OpenJPA**. Hibernate dominates in Spring.

---

### Q: `EntityManager` vs Hibernate `Session`?

```
Session        = Hibernate's NATIVE API
EntityManager  = the JPA STANDARD API

In modern Hibernate, Session EXTENDS EntityManager. ⭐
```

Prefer `EntityManager` for portability; unwrap when you need a Hibernate-only feature:

```java
Session session = entityManager.unwrap(Session.class);
```

---

### Q: `EntityManagerFactory` vs `EntityManager`?

```
EntityManagerFactory → HEAVY, THREAD-SAFE, ONE per application, made at startup
EntityManager        → LIGHT, NOT thread-safe, ONE per transaction/request ⭐
```

```
EntityManagerFactory  (created once at startup — expensive)
        │ creates
        ├── EntityManager (request 1)
        ├── EntityManager (request 2)
        └── EntityManager (request 3)   ← cheap, short-lived, one per transaction
```

Real-world idea: the **factory** builds cars; each **car** is driven by one person at a time.

⭐ Spring injects a **thread-bound proxy** of `EntityManager`, which is exactly why sharing one field in a singleton `@Service` is safe even though `EntityManager` itself is not thread-safe.

#### Easy memory

```
JPA = spec 📖 | Hibernate = implementation 🚗 | Spring Data JPA = chauffeur 🧑‍✈️

Layering: repository → Spring Data → JPA API → Hibernate → JDBC → DB ⭐

EntityManagerFactory = heavy, thread-safe, ONE
EntityManager        = light, NOT thread-safe, one per transaction ⭐
Session extends EntityManager
```

---

### ⚠️ `javax` vs `jakarta` (the Boot 3 trap — RoboGebra is Boot 3.2)

```
Spring Boot 2.x  →  import javax.persistence.Entity;
Spring Boot 3.x  →  import jakarta.persistence.Entity;   ✅
```

```
Oracle donated Java EE to the Eclipse Foundation,
but kept the "javax" TRADEMARK.
      ↓
Every package had to be renamed javax.* → jakarta.*
      ↓
Boot 3 upgrade = thousands of import changes 💥
```

> 💬 *"On Boot 3.2 all the persistence and validation imports are `jakarta.*`, not `javax.*` — that's the single biggest migration cost when upgrading from Boot 2."*

---

# D — Entity mapping reference

### Q: What makes a class a valid JPA entity?

The easiest way to remember — **five rules**:

```
1. @Entity annotation
2. an @Id
3. a public/protected NO-ARG CONSTRUCTOR  ⭐ (Hibernate uses reflection + proxies)
4. NOT final, and no final methods/fields ⭐ (proxies SUBCLASS it)
5. a top-level class (not an interface or enum)
```

⭐ Rules 3 and 4 together explain something people ask about constantly:

```
Why can't a `record` be a JPA entity?

records are FINAL, have no no-arg constructor, and are immutable
      → all three rules broken 💥
```

---

### The core annotations

```java
@Entity
@Table(name = "app_user",
       uniqueConstraints = @UniqueConstraint(columnNames = "email"),
       indexes = @Index(name = "idx_user_status", columnList = "status"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String name;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)          // ⚠️ NEVER ORDINAL — see below
    private Status status;

    private LocalDateTime createdAt;      // java.time needs no annotation ✅

    @Transient                            // NOT persisted
    private String fullDisplayName;

    @Lob
    private String description;           // CLOB / BLOB for byte[]

    protected User() { }                  // ⭐ the required no-arg constructor
}
```

| Annotation | Purpose |
|---|---|
| `@Entity` | marks the class as persistent |
| `@Table` | table name, schema, unique constraints, indexes |
| `@Id` | primary key |
| `@GeneratedValue` | how the PK is generated |
| `@Column` | column name, nullable, length, unique, precision, `insertable`/`updatable` |
| `@Transient` | the field is **not** persisted |
| `@Enumerated(EnumType.STRING)` | store the enum by name ⭐ |
| `@Lob` | large object (CLOB/BLOB) |
| `@Embedded` / `@Embeddable` | a value object flattened into the same table |
| `@ElementCollection` | a collection of basic/embeddable types in a side table |
| `@Version` | optimistic locking |
| `@CreatedDate` / `@LastModifiedDate` | Spring Data auditing |

---

### ⚠️ `@Enumerated(EnumType.ORDINAL)` — the classic bug

```
ORDINAL is the DEFAULT, and it stores the enum's POSITION, not its name.
```

```java
enum Status { ACTIVE, INACTIVE, DELETED }
//              0        1         2

DB now holds:  0, 1, 2
```

Six months later someone adds a value in the middle:

```java
enum Status { ACTIVE, PENDING, INACTIVE, DELETED }
//              0        1         2         3
```

```
Row with 1 used to mean INACTIVE
Row with 1 now means    PENDING   💥

EVERY existing row silently means something else.
No exception. No error. Just wrong data. 💥
```

```java
@Enumerated(EnumType.STRING)   // ✅ ALWAYS do this
private Status status;
```

Now the database stores `"ACTIVE"`, `"INACTIVE"` — reordering the enum can never corrupt anything.

#### Easy memory

```
Entity rules: @Entity + @Id + NO-ARG CONSTRUCTOR + not final + top-level ⭐
   → which is exactly why a `record` can NEVER be an @Entity

⚠️ @Enumerated: the DEFAULT is ORDINAL and it stores the POSITION.
   Insert one enum value in the middle → every existing row means
   something different 💥  → ALWAYS use EnumType.STRING ⭐
```

---

### Q: `@GeneratedValue` strategies?

| Strategy | How it works | Notes |
|---|---|---|
| `IDENTITY` | DB auto-increment column | simple; **disables JDBC batch inserts** ⚠️ |
| `SEQUENCE` | a database sequence | ✅ **preferred** on Postgres/Oracle; supports batching |
| `TABLE` | a separate table simulates a sequence | portable but slow (row locking) — avoid |
| `AUTO` | the provider picks | on Hibernate 6 + Boot 3 this usually means a sequence |
| `UUID` | Hibernate 6 generates a UUID | good for distributed ids; worse index locality |

#### Why does IDENTITY block batch inserts? ⭐

```
IDENTITY:
   Hibernate MUST know the id immediately after each insert,
   and only the DB can tell it.
        ↓
   INSERT → ask the DB for the id → INSERT → ask again → ...
        ↓
   1,000 inserts = 1,000 round trips 🐢

SEQUENCE:
   Hibernate asks for a BLOCK of 50 ids in ONE call,
   then batches 50 inserts into one round trip ⚡
```

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
@SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 50)
private Long id;
```

> 💬 *"`IDENTITY` is the easy default but it kills batch inserts — Hibernate needs the generated id immediately, so it can't queue the statements. On Postgres I'd use `SEQUENCE` with an `allocationSize` so it fetches a block of ids at once."*

#### Easy memory

```
IDENTITY → MySQL auto-increment → simple → ⚠️ NO batch inserts (1 round trip each)
SEQUENCE → Postgres/Oracle → ⭐ batch-friendly (allocationSize = 50)
TABLE    → slow, avoid | AUTO → provider decides | UUID → distributed ids
```

---

### Q: `@Embeddable` — when do you use it?

The easiest way to remember:

```
@Embeddable = a value object with NO IDENTITY of its own.
              It lives INSIDE the parent's table. ⭐
```

```java
@Embeddable
public class Address {
    private String street;
    private String city;
    private String zip;
}

@Entity
public class User {
    @Id @GeneratedValue private Long id;

    @Embedded private Address address;      // → street, city, zip columns on `user` ⭐

    @Embedded
    @AttributeOverride(name = "city", column = @Column(name = "billing_city"))
    private Address billingAddress;         // reuse the same class, different columns
}
```

```
   ONE table `user`
   ┌────┬──────┬────────┬──────┬─────┬──────────────┐
   │ id │ name │ street │ city │ zip │ billing_city │
   └────┴──────┴────────┴──────┴─────┴──────────────┘
                 └── from the embedded Address ──┘

No separate `address` table. No join. ✅
```

Real-world idea: an address written **on** an envelope, not on a separate card clipped to it. It has no life of its own — the envelope is thrown away, the address goes with it.

---

### Q: `@Embeddable` vs `@Entity`?

```
@Entity     → has its OWN identity, its OWN table, its OWN lifecycle
@Embeddable → has NONE of those; it is part of the owner and DIES with it ⭐
```

```
Is it meaningful on its own, and would you ever query it alone?
      YES → @Entity
      NO  → @Embeddable
```

---

### Q: Composite primary keys?

Two options — `@EmbeddedId` (preferred, cleaner) or `@IdClass`.

```java
@Embeddable
public class OrderItemId implements Serializable {   // must be Serializable + equals/hashCode ⭐
    private Long orderId;
    private Long productId;
}

@Entity
public class OrderItem {
    @EmbeddedId private OrderItemId id;
}
```

```
⚠️ The composite-key class MUST:
   implement Serializable
   override equals() and hashCode()
   have a no-arg constructor
```

---

### Q: Inheritance mapping strategies?

The easiest way to remember — **how many tables?**

```
SINGLE_TABLE    → ONE table for the whole hierarchy + a discriminator column (default)
JOINED          → parent table + one table PER CHILD, linked by joins
TABLE_PER_CLASS → one table per CONCRETE class, no sharing
@MappedSuperclass → NO table for the parent; it just shares fields ⭐
```

| Strategy | Tables | Pros | Cons |
|---|---|---|---|
| `SINGLE_TABLE` (**default**) | one + discriminator | fastest — no joins | subclass columns must be **nullable** ⚠️ |
| `JOINED` | parent + child tables | normalised, no nulls | every read is a join 🐢 |
| `TABLE_PER_CLASS` | one per concrete class | no joins for single-type reads | polymorphic queries need `UNION` |
| `@MappedSuperclass` | none for the parent | shares fields (`id`, `createdAt`) | the parent is **not** an entity — can't query it polymorphically |

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type")
public abstract class Payment { ... }

@Entity @DiscriminatorValue("CARD")
public class CardPayment extends Payment { ... }
```

```
SINGLE_TABLE `payment`
┌────┬──────────────┬────────┬───────────┬─────────────┐
│ id │ payment_type │ amount │ card_no   │ upi_id      │
├────┼──────────────┼────────┼───────────┼─────────────┤
│ 1  │ CARD         │ 500    │ 1234...   │ NULL   ⚠️   │
│ 2  │ UPI          │ 300    │ NULL   ⚠️ │ a@ybl       │
└────┴──────────────┴────────┴───────────┴─────────────┘
        ↑ the discriminator          ↑ nullable columns are the cost
```

⭐ **`@MappedSuperclass` is the one you'll actually use most** — a `BaseEntity` holding `id`, `createdAt` and `updatedAt`, shared by every entity without creating a table of its own.

#### Easy memory

```
SINGLE_TABLE ⭐ default → 1 table + discriminator → fast, but NULLABLE columns
JOINED                 → normalised → but every read is a JOIN
TABLE_PER_CLASS        → one per concrete class → polymorphic queries need UNION
@MappedSuperclass ⭐   → NO table — just shares id/createdAt (what you'll really use)
```

---

# E — Entity lifecycle & the persistence context

> 🔴 **This section is the heart of ORM.** If you understand the persistence context, most "tricky" JPA questions answer themselves.

### Q: What is the persistence context?

The easiest way to remember:

```
The persistence context = Hibernate's MEMORY of the entities it is
currently managing, for the duration of ONE transaction.
```

It is three things at once:

```
1. the FIRST-LEVEL CACHE  → always on, cannot be disabled ⭐
2. where DIRTY CHECKING happens → it auto-generates UPDATEs
3. an IDENTITY MAP → one DB row = exactly ONE Java object, inside that transaction
```

```
@Transactional
public void m() {
    User a = repo.findById(1L).get();     // SQL runs → object cached
    User b = repo.findById(1L).get();     // NO SQL — served from the L1 cache ⭐

    System.out.println(a == b);           // TRUE — the identity map ⭐
}
```

Real-world idea: a **shopping trolley**.

```
While you're in the shop (the transaction), the trolley holds
everything you've picked up.

You can put things in, take things out, change your mind —
nothing reaches the BILL until you go to the checkout (commit). ⭐
```

---

### Q: The four entity states — draw this

```
                    new User()
                        │
                    ┌───▼────────┐
                    │ TRANSIENT  │  not in the DB, not managed
                    └───┬────────┘
              persist() │              ▲ (garbage collected)
                    ┌───▼─────────┐    │
    ┌──────────────►│  MANAGED    │────┘
    │  merge()      │(persistent) │  in the persistence context → dirty checking ON ⭐
    │  find()       └──┬───────┬──┘
    │                  │       │ remove()
    │  detach()/clear()│       │
    │  tx commit       │   ┌───▼────────┐
    │                  │   │  REMOVED   │  scheduled for DELETE at flush
 ┌──┴─────────┐        │   └────────────┘
 │  DETACHED  │◄───────┘
 └────────────┘  was managed; the context closed — changes are NOT tracked ⚠️
```

| State | In the DB? | Tracked? | How you get there |
|---|---|---|---|
| **Transient (new)** | ❌ | ❌ | `new User()` |
| **Managed (persistent)** | ✅ (or will be at flush) | ✅ | `persist()`, `find()`, `merge()`, a JPQL result |
| **Detached** | ✅ | ❌ ⚠️ | the transaction ended, `detach()`, `clear()`, `close()` |
| **Removed** | ✅ until flush | ✅ | `remove()` |

Real-world idea: a **hospital patient.**

```
TRANSIENT → a person at home; the hospital has never seen them
MANAGED   → ADMITTED — every change is monitored and recorded ⭐
DETACHED  → DISCHARGED — still alive, but nobody is monitoring them ⚠️
REMOVED   → marked for discharge-and-delete at the end of the shift
```

⭐ The whole of "tricky JPA" comes down to one question: **is this entity MANAGED right now?**

```
MANAGED  → changes are saved automatically, no save() needed ✅
DETACHED → changes go NOWHERE 💥
```

---

### Q: What is dirty checking?

The easiest way to remember:

```
Dirty checking = Hibernate remembers what the entity looked like when
it was loaded, and at commit it compares. Anything different → UPDATE.

You NEVER call update(). ⭐
```

```java
@Transactional
public void renameUser(Long id, String newName) {
    User user = userRepository.findById(id).orElseThrow();
    user.setName(newName);          // managed → dirty checked
    // no save() needed — the UPDATE fires automatically at commit ✅
}
```

```
findById()  → Hibernate stores a SNAPSHOT:  {name: "Priya"}
setName()   → current state is now:         {name: "Ravi"}
commit      → compare snapshot vs current → they differ
            → UPDATE users SET name='Ravi' WHERE id=1 ✅
```

Real-world idea: a **hotel minibar.** Nobody watches you drink. At checkout they compare the fridge against the original list and bill you for the difference.

> 💬 **Interview gold:** *"Inside a transaction I don't need to call `save()` on an entity I loaded — it's managed, so dirty checking generates the UPDATE at flush. People add a redundant `save()` out of habit; it's harmless but shows they don't know the persistence context."*

#### Easy memory

```
Persistence context = L1 cache + dirty checking + identity map ⭐
                      (a shopping trolley 🛒 until checkout)

4 states: TRANSIENT → MANAGED → DETACHED / REMOVED
          (at home → admitted → discharged)

MANAGED  → changes saved automatically (dirty checking, hotel minibar 🍫)
DETACHED → changes go NOWHERE 💥

The one question behind every tricky JPA question:
   "Is this entity MANAGED right now?" ⭐
```

---

### Q: What is flush? When does it happen?

The easiest way to remember:

```
FLUSH  = send the pending SQL to the database  (but NOT commit) ⭐
COMMIT = make it permanent
```

```
flush  → the SQL runs, still inside the transaction → can still be rolled back
commit → flush + make it permanent ✅
```

Flush happens in three situations:

```
1. On transaction COMMIT
2. BEFORE a query whose result could be affected by pending changes
      (FlushModeType.AUTO — the default) ⭐
3. When you call flush() explicitly
```

⭐ Point 2 is clever and worth explaining: if you've changed a `User`'s status in memory and then run a query for "all active users", Hibernate flushes first so the query sees your change.

```java
@Transactional(readOnly = true)      // → FlushMode.MANUAL → NOTHING is ever flushed
```

That is exactly why **writes silently disappear inside a read-only transaction** — no exception, no error, just no UPDATE.

Real-world idea: **saving a document vs printing it.** Flush = writing to disk; commit = handing over the signed final copy.

---

### Q: `persist()` vs `merge()` vs `save()` — the most-asked lifecycle question

The easiest way to remember:

```
persist() → for a NEW object → the SAME object becomes managed
merge()   → for a DETACHED object → returns a DIFFERENT, managed COPY ⭐⭐
save()    → Spring Data → decides between the two for you
```

| | `persist(e)` | `merge(e)` | Spring Data `save(e)` |
|---|---|---|---|
| Argument state | must be **transient** (or already managed) | transient **or detached** | anything |
| Returns | `void` — **the argument becomes managed** | **a different, managed copy**; the argument stays detached ⚠️ | the managed instance |
| On a detached entity | throws `EntityExistsException` | copies the state onto a managed instance (SELECT then UPDATE) | delegates to `merge` |
| SQL | INSERT | SELECT + INSERT/UPDATE | `isNew()` ? `persist` : `merge` |

```java
// Spring Data's SimpleJpaRepository, essentially:
public <S extends T> S save(S entity) {
    if (entityInformation.isNew(entity)) { em.persist(entity); return entity; }
    else                                 { return em.merge(entity); }
}
```

#### ⚠️ The trap — merge returns a DIFFERENT object

```
detachedUser  ──merge()──▶  managedCopy   ← a DIFFERENT object!
      │                          │
   still DETACHED             MANAGED ✅
   changes here go            changes here are saved
   NOWHERE 💥
```

```java
User saved = repo.save(detachedUser);

detachedUser.setName("X");   // ❌ goes nowhere
saved.setName("X");          // ✅ this one is managed
```

**Rule: always keep working with the object `save()` RETURNS.**

Real-world idea: `merge` is like **photocopying a form onto the official letterhead.** The office keeps and processes the official copy; your original piece of paper is now irrelevant, no matter what you scribble on it.

---

### Q: How does Spring Data decide an entity is "new"?

```
By default: the @Id field is NULL (or 0 for a primitive) → it's new → persist()
Otherwise → merge()
```

⚠️ The consequence:

```
If YOU assign ids yourself (UUIDs, natural keys)
      → the id is never null
      → Spring thinks it already exists
      → it issues a pointless SELECT before EVERY insert 🐢
```

**Fix:** implement `Persistable<ID>` and override `isNew()`.

---

### Q: `find()` vs `getReference()`?

```
find()          → hits the DB IMMEDIATELY → returns the entity (or null)
getReference()  → returns a LAZY PROXY → no SQL until you touch a field ⭐
```

| | `find(User.class, 1L)` | `getReference(User.class, 1L)` |
|---|---|---|
| Hits the DB | **immediately** | not until you touch a non-id field |
| Returns | the entity (or `null`) | a **lazy proxy** |
| If the row is missing | `null` | throws `EntityNotFoundException` **later** ⚠️ |

The real use — setting a foreign key without loading the row:

```java
order.setUser(em.getReference(User.class, userId));   // ✅ no SELECT on user at all
```

```
find()         → SELECT * FROM users WHERE id=5   ← wasted, you only needed the FK
getReference() → no query at all; the INSERT just writes user_id = 5 ⚡
```

Spring Data equivalents: `findById()` → `find`, `getReferenceById()` → `getReference`.

---

### Q: `remove()` vs `deleteById()` vs bulk delete?

```
remove(entity)      → the entity must be MANAGED → DELETE at flush → cascades apply
deleteById(id)      → Spring does a findById FIRST, then remove → TWO statements ⚠️
deleteAllInBatch()  → ONE bulk DELETE ⚡ but BYPASSES the persistence context
   / @Modifying           and all cascades → loaded entities go STALE ⚠️
```

```
Deleting 1,000 rows:

deleteAll()        → 1,000 SELECTs + 1,000 DELETEs 🐢
deleteAllInBatch() → 1 DELETE ⚡ (but no cascade, no callbacks)
```

#### Easy memory

```
persist() → NEW → the SAME object becomes managed
merge()   → DETACHED → returns a DIFFERENT managed copy ⭐⭐ (use the RETURN value!)
save()    → id is null ? persist : merge

flush = send the SQL | commit = make it permanent
readOnly = true → FlushMode.MANUAL → writes SILENTLY vanish 💥

find() → SQL now | getReference() → lazy proxy, no SQL ⭐ (great for setting a FK)
deleteById → 2 statements | deleteAllInBatch → 1, but skips cascades ⚠️
```

---

# F — Relationships

### The four types

The easiest way to remember:

```
@OneToOne   → 1 ↔ 1      User ↔ Profile
@ManyToOne  → many → 1   Order → User      ⭐ ALWAYS the owning side (holds the FK)
@OneToMany  → 1 → many   User → Orders     ← the mirror (mappedBy)
@ManyToMany → many ↔ many Student ↔ Course (needs a join table)
```

```java
// ── @OneToOne ──────────────────────────────────────────
@Entity
public class User {
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "profile_id")           // owning side — holds the FK ⭐
    private Profile profile;
}
@Entity
public class Profile {
    @OneToOne(mappedBy = "profile")            // inverse side
    private User user;
}

// ── @ManyToOne / @OneToMany (the most common pair) ─────
@Entity
public class Order {
    @ManyToOne(fetch = FetchType.LAZY)         // ⚠️ ALWAYS override — the default is EAGER
    @JoinColumn(name = "user_id")              // owning side ⭐
    private User user;
}
@Entity
public class User {
    @OneToMany(mappedBy = "user",              // "user" = the FIELD NAME in Order ⭐
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();
}

// ── @ManyToMany ────────────────────────────────────────
@Entity
public class Student {
    @ManyToMany
    @JoinTable(name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<Course> courses = new HashSet<>();
}
@Entity
public class Course {
    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();
}
```

---

### Q: What is the "owning side" and why does it matter?

⭐ **This single concept causes more JPA bugs than anything else.**

```
The OWNING side is the one that HOLDS THE FOREIGN KEY COLUMN.

It is the ONLY side Hibernate looks at when WRITING to the database.
The inverse side (mappedBy) is READ-ONLY for persistence. ⭐
```

```
   users                     orders
┌────┬──────┐          ┌────┬───────┬─────────┐
│ id │ name │          │ id │ total │ user_id │ ← the FK lives HERE
└────┴──────┘          └────┴───────┴─────────┘
                                          ↑
                          so Order (@ManyToOne) is the OWNING side ⭐
```

```
@ManyToOne  → ALWAYS the owning side
mappedBy    → goes on the INVERSE side, and names the FIELD on the owning side
```

#### ⚠️ The #1 relationship bug

```java
user.getOrders().add(order);      // ❌ only touched the INVERSE side
userRepository.save(user);
// → nothing is saved. order.user_id is still NULL 💥
```

```java
order.setUser(user);              // ✅ set the OWNING side
orderRepository.save(order);
```

Real-world idea: **only the person holding the pen can sign the register.** You can shout your name from the queue (the inverse side) all day; unless the pen-holder writes it, nothing is recorded.

---

### ✅ Always write bidirectional helper methods

```java
public void addOrder(Order order) {
    orders.add(order);
    order.setUser(this);       // ⭐ keeps BOTH sides consistent
}
public void removeOrder(Order order) {
    orders.remove(order);
    order.setUser(null);
}
```

This one habit removes an entire class of bug permanently.

---

### Q: What happens if you use `@OneToMany` without `mappedBy`?

```
Hibernate assumes UNIDIRECTIONAL and silently creates an EXTRA JOIN TABLE
(user_orders) instead of using the FK on `order`. 💥
```

```
What you expected:        orders.user_id
What you got:             a third table `user_orders(user_id, order_id)` ⚠️
```

Almost always a mistake — either add `mappedBy`, or put `@JoinColumn` on the `@OneToMany`.

---

### Q: Cascade types?

```
Cascade = "when this operation happens to the PARENT, do it to the CHILDREN too."
```

| Cascade | Propagates |
|---|---|
| `PERSIST` | `persist()` → children inserted |
| `MERGE` | `merge()` |
| `REMOVE` | `remove()` → children deleted |
| `REFRESH` | `refresh()` |
| `DETACH` | `detach()` |
| `ALL` | all of the above |

```
⚠️ There is NO cascade by default.

CascadeType.ALL is FINE on a true parent-child composition (Order → OrderItem)
CascadeType.ALL is DANGEROUS on @ManyToOne / @ManyToMany 💥
```

```java
@ManyToOne(cascade = CascadeType.ALL)     // 💥 deleting ONE ORDER deletes the USER
private User user;

@ManyToMany(cascade = CascadeType.ALL)    // 💥 deleting ONE STUDENT deletes shared COURSES
private Set<Course> courses;
```

⭐ The rule: cascade **downwards** from a parent that truly owns its children — never upwards or sideways.

---

### Q: `CascadeType.REMOVE` vs `orphanRemoval = true`?

The easiest way to remember:

```
CascadeType.REMOVE → deletes children when the PARENT is deleted
orphanRemoval      → ALSO deletes a child REMOVED FROM THE COLLECTION ⭐
```

| | `CascadeType.REMOVE` | `orphanRemoval = true` |
|---|---|---|
| Parent deleted → children deleted | ✅ | ✅ |
| Child removed from the collection | ❌ (the FK is just orphaned) | ✅ |

```java
user.getOrders().remove(order);

// orphanRemoval = true      → DELETE FROM orders WHERE id = ? ✅
// CascadeType.REMOVE only   → nothing happens (or an FK constraint violation) 💥
```

Real-world idea:

```
CascadeType.REMOVE → the company shuts down, so all employees leave
orphanRemoval      → you FIRE one employee, and they cease to exist ⭐
```

---

### Q: `List` vs `Set` for collections?

```
Set  → no duplicates, and avoids MultipleBagFetchException ⭐
List → a "bag"; preserves DB order only if you add @OrderBy

RULE: use Set for @ManyToMany and anywhere you might fetch-join two collections ⭐
```

#### Easy memory

```
@ManyToOne = the FK side = the OWNING side ⭐ (@JoinColumn)
@OneToMany = mappedBy = the MIRROR (read-only for persistence)

⚠️ #1 BUG: adding to the inverse collection saves NOTHING
   → always write addOrder() helpers that set BOTH sides ⭐
⚠️ @OneToMany without mappedBy → a surprise extra JOIN TABLE 💥

Cascade: NONE by default | ALL is fine for Order→OrderItem,
         DANGEROUS on @ManyToOne / @ManyToMany 💥
orphanRemoval → also deletes a child removed from the collection ⭐
Use Set for @ManyToMany.
```

---

# G — Fetching, N+1, and LazyInitializationException

### Q: Lazy vs eager — and what are the defaults?

⭐ Memorise this with one phrase: **"To-One is EAGER, To-Many is LAZY."**

| Association | **Default fetch** | What you should use |
|---|---|---|
| `@ManyToOne` | **EAGER** ⚠️ | `LAZY` |
| `@OneToOne` | **EAGER** ⚠️ | `LAZY` |
| `@OneToMany` | LAZY ✅ | LAZY |
| `@ManyToMany` | LAZY ✅ | LAZY |

> 💬 **Say this:** *"The `-ToOne` associations default to EAGER, which is the wrong default — every `findById` silently drags in a join or an extra query. I set `fetch = FetchType.LAZY` on all associations and fetch what I need explicitly with a join fetch or an entity graph."*

#### How lazy actually works — proxies

```
LAZY field
    ↓
Hibernate returns a generated SUBCLASS (a proxy) instead of the real object
    ↓
the first time you touch a NON-ID property → the real SELECT fires ⭐
```

```
order.getUser()              → returns a proxy, NO query
order.getUser().getId()      → still NO query (the id is already known!) ⭐
order.getUser().getName()    → NOW the SELECT runs
```

⭐ That is also exactly why **entities can't be `final`** — Hibernate must be able to subclass them.

---

### Q: What is the N+1 problem?

```
1 query to load N parents
+ N queries, one per parent, for its association
= N + 1 queries 💥
```

```java
List<User> users = userRepository.findAll();       // 1 query
for (User u : users) {
    System.out.println(u.getOrders().size());      // N queries — one per user ❌
}
// 100 users → 101 queries
```

```
SELECT * FROM users                       ← 1
SELECT * FROM orders WHERE user_id = 1    ← 2
SELECT * FROM orders WHERE user_id = 2    ← 3
SELECT * FROM orders WHERE user_id = 3    ← 4
...
                                          ← 101 💥
```

Real-world idea: going to the market **101 times** — once for the list, then once per item — instead of buying everything in one trip.

---

### The 4 fixes

**1. `JOIN FETCH` (JPQL)** — one query, explicit

```java
@Query("select distinct u from User u left join fetch u.orders")
List<User> findAllWithOrders();
```

**2. `@EntityGraph`** — declarative; works with derived queries and pagination ⭐

```java
@EntityGraph(attributePaths = {"orders", "profile"})
List<User> findByStatus(String status);
```

**3. `@BatchSize`** — turns N queries into N/size queries using `IN (...)`

```java
@OneToMany(mappedBy = "user")
@BatchSize(size = 25)
private List<Order> orders;
// or globally: spring.jpa.properties.hibernate.default_batch_fetch_size=25
```

```
Without: 100 queries
With size 25: 4 queries  →  WHERE user_id IN (1,2,...,25) ⚡
```

**4. DTO projection** — fetch only the columns you need, no entities at all ⭐

```java
@Query("select new com.app.dto.UserSummary(u.id, u.name, count(o)) " +
       "from User u left join u.orders o group by u.id, u.name")
List<UserSummary> findSummaries();
```

> 💬 *"My default fix is an `@EntityGraph` because it composes with derived queries and pagination. For read-only screens I prefer a DTO projection — it avoids loading entities into the persistence context at all."*

---

### 🔵 In your RoboGebra code — the N+1 fix, in MongoDB ⭐

**File:** `robogebra-portal/.../domain/exerciseitem/repository/ExerciseItemRepository.java`
(**27 `@Aggregation` pipelines** across the codebase)

```java
@Repository
public interface ExerciseItemRepository extends MongoRepository<ExerciseItemEntity, String> {

    // derived query — Spring writes it from the METHOD NAME
    Optional<ExerciseItemEntity> findByExercise_IdAndIndex(String exerciseId, String index);

    // the N+1 fix: ONE pipeline instead of one query per exercise ⭐
    @Aggregation(pipeline = {
            "{ '$match': { 'exercise.$id': { '$in': ?0 } } }",
            "{ '$sort':  { 'displayOrder': 1 } }"
    })
    List<ExerciseItemEntity> findAllByExerciseIds(List<ObjectId> exerciseIds);
}
```

> 🗣️ *"N+1 shows up in Mongo too. Loading a chapter's exercises and then fetching each exercise's items one at a time is one query plus N. I replaced it with a single `@Aggregation` pipeline that `$match`es on `exercise.$id: { $in: [...] }` and sorts by `displayOrder` — one round trip instead of N. It's the same fix as a JPA `JOIN FETCH`, just expressed as a pipeline."*

⭐ This is your **best** N+1 answer because it is real, it is yours, and it shows the concept transfers across databases.

---

### ⚠️ Three N+1 gotchas worth naming

**`JOIN FETCH` + pagination**

```
join fetch on a COLLECTION + Pageable
      ↓
HHH000104: firstResult/maxResults specified with collection fetch;
           applying in memory
      ↓
Hibernate loads EVERY row and paginates in Java 💥
```

Fix: use `@EntityGraph` with `@BatchSize`, or do it in two queries (page the ids first, then fetch by those ids).

**`MultipleBagFetchException`**

```
Fetch-joining TWO `List` collections at once
      → MultipleBagFetchException: cannot simultaneously fetch multiple bags 💥

Fix: make them `Set`, or fetch one per query.
```

**Why `distinct` in a `JOIN FETCH`?**

```
A join to a collection MULTIPLIES the parent rows:

user 1 × 3 orders → 3 rows → 3 duplicate User objects in the result 💥

select DISTINCT u  → de-duplicates the OBJECT references ✅
```

(Hibernate 6 de-duplicates entity results automatically, so `distinct` is no longer required there — but say you know both.)

---

### Q: What is `LazyInitializationException`?

```
Accessing a LAZY association AFTER the persistence context has CLOSED. 💥
```

```java
@Transactional
public User getUser(Long id) { return userRepository.findById(id).orElseThrow(); }

// in the controller — the transaction has already committed
user.getOrders().size();   // ❌ could not initialize proxy - no Session
```

```
INSIDE @Transactional   → the session is OPEN   → the proxy can load ✅
after the method ends   → the session is CLOSED → the proxy is helpless 💥
```

Real-world idea: **the kitchen closes at 10 pm.** Order at 9:59 and you're fine. Order at 10:01 and the proxy has nobody to ask.

**Fixes, best first:**

```
1. RETURN A DTO from the service — the mapping happens INSIDE the transaction ✅ best
2. Fetch what you need with JOIN FETCH / @EntityGraph
3. Touch it inside the transactional method: Hibernate.initialize(user.getOrders())
4. spring.jpa.open-in-view=true — ON BY DEFAULT in Spring Boot ⚠️
```

⭐ About that fourth one:

```
open-in-view keeps the session open for the WHOLE HTTP request.
      ↓
It HIDES the problem — and pushes N+1 queries into the VIEW layer,
while holding a database connection for the entire request 💥

Best practice: spring.jpa.open-in-view=false, and fix the fetching properly.
```

> 💬 *"Boot logs a warning about `open-in-view` at startup for a reason — it hides lazy loading problems until production. I'd turn it off and return DTOs from the service layer."*

#### Easy memory

```
"To-One is EAGER, To-Many is LAZY" ⭐ → always override @ManyToOne to LAZY
Lazy works via PROXIES → which is why entities can't be final ⭐

N+1 = 1 + N queries (going to the market 101 times) 💥
FIX: JOIN FETCH (use distinct) | @EntityGraph ⭐ | @BatchSize | DTO projection

⚠️ JOIN FETCH + Pageable → paginates IN MEMORY (HHH000104)
⚠️ Two List fetch joins  → MultipleBagFetchException → use Set

LazyInitializationException = touching a lazy field after the session closed
   (the kitchen closed at 10pm 🍽️)
   Fix: return a DTO from the service ⭐ | open-in-view=false
```

---

# H — Transactions

### Q: What does `@Transactional` do?

```
Spring wraps the bean in a PROXY that:
   begins a transaction before the method
   COMMITS on a normal return
   ROLLS BACK on a runtime exception
```

```java
@Transactional
public void transfer(Long from, Long to, BigDecimal amount) {
    Account a = repo.findById(from).orElseThrow();
    Account b = repo.findById(to).orElseThrow();
    a.debit(amount);
    b.credit(amount);          // both UPDATEs flush at commit — all or nothing ✅
}
```

```
Without it: debit succeeds 💰−1000 → credit CRASHES 💥 → the money VANISHES
With it:    debit succeeds → credit crashes → EVERYTHING rolls back ✅
```

Real-world idea: **a bank transfer.** It cannot be half done.

---

### Q: Propagation levels?

| Propagation | Behaviour |
|---|---|
| **`REQUIRED`** (default) ⭐ | join the existing transaction, or start one |
| `REQUIRES_NEW` ⭐ | **suspend** the current one; always start a new independent transaction |
| `SUPPORTS` | join if one exists, otherwise run non-transactionally |
| `NOT_SUPPORTED` | suspend any transaction and run without one |
| `MANDATORY` | must already be in a transaction, else `IllegalTransactionStateException` |
| `NEVER` | must **not** be in a transaction, else an exception |
| `NESTED` | a savepoint inside the current transaction — an inner rollback doesn't kill the outer |

```
A (REQUIRED) calls B (REQUIRED)      → SAME transaction → B fails → A rolls back too
A (REQUIRED) calls B (REQUIRES_NEW)  → SEPARATE → B commits even if A rolls back ⭐
```

⭐ **The classic use of `REQUIRES_NEW`:** audit or log writes that must survive even when the business transaction rolls back.

---

### Q: Isolation levels?

The easiest way to remember by **what each one prevents**:

| Isolation | Prevents |
|---|---|
| `READ_UNCOMMITTED` | nothing (dirty reads possible) |
| `READ_COMMITTED` | dirty reads *(Postgres/Oracle default)* |
| `REPEATABLE_READ` | dirty + non-repeatable reads *(MySQL default)* |
| `SERIALIZABLE` | everything, including phantom reads — slowest |

**The three read phenomena — explain them with one example each:**

```
DIRTY READ
   You read a value another transaction wrote but hasn't COMMITTED.
   They roll back → you acted on data that never existed 💥

NON-REPEATABLE READ
   You read row 5 → someone UPDATES it → you read row 5 again
   → a DIFFERENT value. The same query, two answers.

PHANTOM READ
   You run "SELECT * WHERE age > 30" → 10 rows
   Someone INSERTS a new matching row
   You run it again → 11 rows. A new row APPEARED. 👻
```

```
non-repeatable = an EXISTING row CHANGED
phantom        = a NEW row APPEARED ⭐
```

---

### ⚠️ The rollback rule (asked constantly)

```
Spring rolls back on RuntimeException and Error ONLY.
NOT on checked exceptions. ⭐
```

```java
@Transactional
public void save() throws IOException {
    repo.save(x);
    throw new IOException();       // ❌ CHECKED → NO ROLLBACK → it COMMITS 💥
}

@Transactional(rollbackFor = Exception.class)   // ✅ the fix
public void save() throws IOException { ... }
```

⭐ And the second half of the trap:

```java
@Transactional
public void m() {
    try {
        somethingThatThrows();
    } catch (Exception e) {
        log.error("failed", e);     // ❌ you SWALLOWED it → Spring never sees it
    }                               //    → the transaction COMMITS 💥
}
```

---

### ⚠️ The self-invocation trap (the #1 `@Transactional` bug)

```java
@Service
public class OrderService {

    public void placeOrder() {
        saveOrder();          // ❌ an internal call — bypasses the proxy, NO transaction 💥
    }

    @Transactional
    public void saveOrder() { ... }
}
```

```
External call:  caller → PROXY → placeOrder()      ✅ the proxy is involved
Internal call:  placeOrder() → this.saveOrder()    ❌ never leaves the object
                                                      → the proxy never sees it 💥
```

Same reason `@Transactional` on a **`private`**, **`final`** or **`static`** method does nothing at all.

**Fixes:** move the method to another bean, self-inject, or use `AopContext.currentProxy()`.

---

### Q: `@Transactional(readOnly = true)` — what does it actually do?

```
1. Sets FlushMode.MANUAL → Hibernate SKIPS dirty checking
      → less memory, faster (no snapshots kept) ⚡
2. Hints the driver/DB that the transaction is read-only
```

Use it on every read-only service method.

```
⚠️ And it means ANY modification silently does NOT persist.
   No exception. No warning. The UPDATE simply never happens 💥
```

#### Easy memory

```
@Transactional = a PROXY: begin → commit on return → rollback on RuntimeException

REQUIRED ⭐ default (join) | REQUIRES_NEW ⭐ (independent — audit logs survive)
Isolation: READ_COMMITTED (Postgres) | REPEATABLE_READ (MySQL)
   non-repeatable = a row CHANGED | phantom = a NEW row APPEARED 👻

⚠️ CHECKED exceptions do NOT roll back → rollbackFor = Exception.class
⚠️ CATCHING the exception also prevents rollback 💥
⚠️ SELF-INVOCATION (this.method()) → no transaction at all 💥
⚠️ readOnly = true → writes SILENTLY vanish 💥
```

---

# I — Caching

The easiest way to remember:

```
L1 (first level)  → per TRANSACTION → ALWAYS ON, can't be disabled ⭐
L2 (second level) → per APPLICATION → OFF by default, opt-in
Query cache       → caches query → IDS → needs L2
```

| | **First-level (L1)** | **Second-level (L2)** | **Query cache** |
|---|---|---|---|
| Scope | `EntityManager` / transaction | app-wide, shared across sessions | query + parameters → **ids** |
| On by default | ✅ **always on, cannot be disabled** ⭐ | ❌ off | ❌ off |
| Provider | built in | EHCache, Caffeine, Infinispan, Hazelcast | needs L2 enabled |

```java
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Country { ... }
```

```
L2 is for READ-MOSTLY REFERENCE DATA: countries, currencies, config ✅
NEVER cache hot, frequently-written tables ❌ (invalidation costs more than it saves)
```

Real-world idea:

```
L1 → your own notepad during ONE meeting (thrown away afterwards)
L2 → the office noticeboard, shared by everyone, all day ⭐
```

---

### Q: How do you clear the L1 cache and why would you?

```
em.clear()        → detach EVERYTHING
em.detach(entity) → detach one
```

⭐ The real reason: **batch jobs.**

```java
for (int i = 0; i < items.size(); i++) {
    em.persist(items.get(i));
    if (i % 50 == 0) { em.flush(); em.clear(); }     // ⭐ or you will OOM
}
```

```
Without flush + clear:
   the persistence context holds ALL 100,000 entities in memory
   + a SNAPSHOT of each for dirty checking
      → OutOfMemoryError 💥
```

---

### Q: Hibernate cache vs Spring's `@Cacheable`?

```
Spring @Cacheable → caches METHOD RETURN VALUES (any bean, any type)
Hibernate L2      → caches ENTITIES and COLLECTIONS by id, inside the ORM
```

They are different layers and can both be on at once. RoboGebra uses `spring-boot-starter-cache` — that is the **Spring** one.

#### Easy memory

```
L1 = per transaction, ALWAYS ON ⭐ (your notepad)
L2 = app-wide, OPT-IN, for read-mostly reference data (the noticeboard)
Query cache = query → ids (needs L2)

BATCH JOBS: flush() + clear() every 50 rows, or you OOM ⭐
Spring @Cacheable = method return values ≠ Hibernate L2 = entities by id
```

---

# J — Concurrency & locking

### Q: Optimistic vs pessimistic locking?

The easiest way to remember:

```
OPTIMISTIC  → "conflicts are RARE" → no lock; DETECT the clash at commit ⭐
PESSIMISTIC → "conflicts are LIKELY" → LOCK the row up front
```

| | **Optimistic** | **Pessimistic** |
|---|---|---|
| Assumes | conflicts are rare | conflicts are likely |
| Mechanism | a `@Version` column checked on UPDATE | a DB row lock (`SELECT ... FOR UPDATE`) |
| Cost | no locks held — scales well ⚡ | holds locks; risk of deadlock and contention |
| On conflict | `OptimisticLockException` at commit → retry | the second transaction **waits** |
| Use for | web apps, long "think time" between read and write ⭐ | short critical sections: inventory, seat booking, balances |

#### How optimistic locking actually works

```java
@Entity
public class Product {
    @Id private Long id;
    @Version private Integer version;    // ⭐ Hibernate increments it on every UPDATE
    private int stock;
}
```

```sql
UPDATE product SET stock = ?, version = 2 WHERE id = ? AND version = 1
                                                        ↑ the guard ⭐
```

```
Both users load version 1
User A saves → version becomes 2 ✅
User B saves → WHERE version = 1 matches 0 ROWS
             → Hibernate sees 0 rows updated
             → OptimisticLockException 💥 → retry
```

⭐ Nothing was locked. The clash is simply *detected*, not prevented.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select p from Product p where p.id = :id")
Product findByIdForUpdate(@Param("id") Long id);
```

Real-world idea:

```
OPTIMISTIC  → editing a shared Google Doc: everyone edits freely,
              and a conflict is flagged if two people change the same line ⭐
PESSIMISTIC → checking a library book out: nobody else can touch it
              until you return it 📕
```

> 💬 *"For a checkout flow I'd use `@Version` optimistic locking with a retry, because the user has think time and holding a DB lock across that is unacceptable. For decrementing stock inside a short server-side transaction, pessimistic write is simpler and correct."*

#### Easy memory

```
OPTIMISTIC  → @Version column → UPDATE ... WHERE version = 1
              → 0 rows updated → OptimisticLockException → RETRY ⭐
              (Google Docs: edit freely, flag the conflict)

PESSIMISTIC → SELECT ... FOR UPDATE → others WAIT
              (library book: checked out, nobody else can touch it 📕)

Long user think time → OPTIMISTIC ⭐ | short server-side critical section → PESSIMISTIC
```

---

# K — equals, hashCode, Lombok & DTO traps

### Q: How should you implement `equals`/`hashCode` on an entity?

⭐ This is genuinely hard in JPA, and the reason is worth stating clearly:

```
An entity's id is NULL before it's saved, and NOT NULL afterwards.
So its hashCode CHANGES mid-life — which breaks every HashSet it's in. 💥
```

```
new User()  → id = null → hashCode = 31 → put in a HashSet → bucket 31
save()      → id = 42   → hashCode = 42
set.contains(user)      → looks in bucket 42 → NOT FOUND 💥
                          (the object is sitting in bucket 31, invisible forever)
```

**The rules:**

```
1. NEVER use a generated @Id in hashCode ⭐
2. Prefer a BUSINESS/NATURAL key (email, order number, a UUID you assign yourself)
3. No natural key? → a CONSTANT hashCode + an id-based equals
4. Use `instanceof`, not getClass() — Hibernate PROXIES are subclasses,
   so getClass() comparisons fail against a proxy ⭐
```

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User other)) return false;          // handles proxies ⭐
    return id != null && id.equals(other.getId());
}

@Override
public int hashCode() { return getClass().hashCode(); }    // stable across the id transition ⭐
```

A constant `hashCode` looks wrong, but it is correct here: it means every entity of that class lands in one bucket, and `equals` does the real work. Entity sets are almost always tiny, so the performance cost is nil — and correctness beats distribution.

---

### ⚠️ Lombok `@Data` on an entity — three bugs at once

```java
@Data                 // ❌ generates equals/hashCode/toString over ALL fields
@Entity
public class User {
    @OneToMany(mappedBy = "user") private List<Order> orders;
}
```

```
BUG 1 — equals/hashCode over ALL fields
   including the id (see above) and the lazy collections 💥

BUG 2 — toString() touches EVERY field
   → triggers lazy loading (a surprise query, or LazyInitializationException)
   → or INFINITE RECURSION on a bidirectional relationship:
        User.toString() → Order.toString() → User.toString() → StackOverflowError 💥

BUG 3 — @Setter on everything
   breaks the encapsulation of your invariants
```

✅ **Use `@Getter @Setter @NoArgsConstructor`** and write `equals`/`hashCode`/`toString` yourself — or at minimum `@ToString(exclude = "orders")`.

---

### Q: DTO vs Entity — why never return an entity from a controller?

```
1. LEAKS INTERNALS — password hashes, audit columns, the whole object graph
2. Serialisation TRIGGERS LAZY LOADING
      → LazyInitializationException, or an accidental N+1 during JSON writing 💥
3. COUPLES the API contract to the schema
      → a column rename becomes a BREAKING API change
4. Bidirectional relations cause INFINITE JSON RECURSION
      → the @JsonIgnore / @JsonManagedReference band-aid
```

✅ Map entity → DTO **inside the transactional service** (MapStruct, or by hand).

#### Easy memory

```
Entity equals/hashCode is HARD because the id is null → then not null ⭐
   → NEVER put a generated id in hashCode
   → constant hashCode + id-based equals
   → use `instanceof`, NOT getClass() (proxies are SUBCLASSES) ⭐

⚠️ Lombok @Data on an @Entity = 3 bugs:
   equals/hashCode over all fields | toString → lazy load / StackOverflow | setters
   → use @Getter @Setter @NoArgsConstructor instead ✅

Never return an entity from a controller → map to a DTO INSIDE the transaction ⭐
```

---

# L — Predict the output — ORM edition (20 programs)

> 🔴 **Your last interview was output-based.** These are the same *style* of question for ORM. For each: **the code → what happens → the rule → the follow-up.** Assume Spring Boot 3.2, Hibernate 6, `spring.jpa.show-sql=true`.

---

## Q1. Does this UPDATE the database?

```java
@Transactional
public void rename(Long id) {
    User user = userRepository.findById(id).orElseThrow();
    user.setName("Priyanka");
    // no save() call
}
```

### ✅ Answer
**Yes — an `UPDATE` is issued at commit.**

### 📏 The rule
An entity loaded inside a transaction is **managed**. Hibernate snapshots it and **dirty checks** at flush — `save()` is unnecessary.

### 🔁 Follow-up: *"And if you remove `@Transactional`?"*
`findById` opens and closes its own transaction (Spring Data methods are transactional), so `user` comes back **detached** — the `setName` is lost. **No UPDATE.**

---

## Q2. Same query twice

```java
@Transactional
public void test(Long id) {
    User a = userRepository.findById(id).orElseThrow();
    User b = userRepository.findById(id).orElseThrow();
    System.out.println(a == b);
    System.out.println("queries: check the log");
}
```

### ✅ Answer
```
true
```
and **only ONE `SELECT`** appears in the log.

### 📏 The rule
The persistence context is an **identity map** and the **first-level cache**: within one transaction, one row = one object instance. The second lookup is served from L1.

### 🔁 Follow-up: *"What if the two calls are in different transactions?"*
Two SELECTs, and `a == b` is **`false`** — different persistence contexts.

---

## Q3. `merge` returns a different object

```java
@Transactional
public void update(User detachedUser) {          // came from the request body, has an id
    detachedUser.setName("New Name");
    userRepository.save(detachedUser);
    System.out.println(detachedUser.getName());
    // ...later code uses detachedUser
}
```

### ✅ Answer
Prints `New Name`, and the DB **is** updated — but `detachedUser` itself is **still detached**. Any change you make to it *after* the `save()` line is **not** persisted.

### 📏 The rule
`save()` on an entity with an id delegates to **`merge()`**, which copies the state onto a **different, managed instance** and returns it. **Always use the returned value:**
```java
User managed = userRepository.save(detachedUser);   // ✅
managed.setStatus(ACTIVE);                          // this WILL persist
```

### 🔁 Follow-up: *"How many SQL statements does that `save()` produce?"*
Two — a `SELECT` (to load the current row into the context) then an `UPDATE`. `persist()` would be a single `INSERT`.

---

## Q4. Lazy access outside the transaction

```java
@Transactional
public User getUser(Long id) {
    return userRepository.findById(id).orElseThrow();
}

// controller
User user = service.getUser(1L);
System.out.println(user.getOrders().size());
```

### ✅ Answer
**`LazyInitializationException: could not initialize proxy – no Session`**
…**unless** `spring.jpa.open-in-view=true` (Boot's default), in which case it silently works — and fires an extra query in the view layer.

### 📏 The rule
A lazy proxy needs an **open** persistence context. The transaction ended when the service method returned.

### 🔁 Follow-up: *"Three ways to fix it?"*
Return a DTO / `JOIN FETCH` or `@EntityGraph` / initialize inside the transaction. *(Not "make it EAGER" — that's the answer they're hoping you don't give.)*

---

## Q5. Adding to the inverse side only

```java
@Transactional
public void addOrder(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    Order order = new Order("ORD-1");
    user.getOrders().add(order);      // orders is mappedBy = "user"
    // order.setUser(user)  ← missing
}
```

### ✅ Answer
With `cascade = ALL`: the order **is inserted**, but with `user_id = NULL`.
Without cascade: **nothing happens at all** (or a `TransientObjectException` on flush).

### 📏 The rule
Only the **owning side** (`@ManyToOne`, the FK holder) is used to write the relationship. `mappedBy` marks the inverse side, which Hibernate ignores when generating SQL.

### 🔁 Follow-up: *"How do you prevent this class of bug?"*
Bidirectional helper methods (`addOrder`/`removeOrder`) that always set both sides.

---

## Q6. Self-invocation

```java
@Service
public class UserService {
    public void register(User u) {
        save(u);                     // internal call
    }
    @Transactional
    public void save(User u) { repo.save(u); throw new RuntimeException("boom"); }
}
```

### ✅ Answer
The exception propagates — **but there is no rollback of `save`**, because `save()` ran **without a transaction** at all.

### 📏 The rule
`@Transactional` is proxy-based AOP. Calls from **inside** the same object never pass through the proxy. Same for `private`, `final` and `static` methods.

### 🔁 Follow-up: *"How do you fix it?"*
Move `save` into a separate bean, or self-inject the proxy, or `AopContext.currentProxy()`.

---

## Q7. Write inside a read-only transaction

```java
@Transactional(readOnly = true)
public void deactivate(Long id) {
    User user = userRepository.findById(id).orElseThrow();
    user.setActive(false);
}
```

### ✅ Answer
**Nothing is written.** No exception, no UPDATE — the change is silently lost.

### 📏 The rule
`readOnly = true` sets Hibernate's flush mode to **MANUAL**, so dirty checking never flushes. *(On some databases/drivers you'd instead get a "cannot execute UPDATE in a read-only transaction" error — but the classic Hibernate answer is silent no-op.)*

### 🔁 Follow-up: *"Then why use `readOnly` at all?"*
Skipping dirty checking saves memory and CPU on read paths, and it lets the driver route to a read replica.

---

## Q8. Checked exception and rollback

```java
@Transactional
public void process() throws IOException {
    repo.save(new Order("ORD-1"));
    throw new IOException("file missing");
}
```

### ✅ Answer
The order **is committed**. Spring rolls back on `RuntimeException`/`Error` only.

### 📏 The rule
Default rollback rule = unchecked exceptions. Add `@Transactional(rollbackFor = Exception.class)`.

### 🔁 Follow-up: *"And if you catch the exception inside the method?"*
No rollback either — Spring never sees it. (And if the transaction was already marked rollback-only by an inner call, the commit throws `UnexpectedRollbackException`.)

---

## Q9. Entity in a `HashSet` before it's saved

```java
@Entity
class User {
    @Id @GeneratedValue Long id;
    @Override public int hashCode() { return Objects.hash(id); }
    @Override public boolean equals(Object o) { ... id-based ... }
}

Set<User> set = new HashSet<>();
User u = new User();
set.add(u);                 // id == null → hashCode based on null
userRepository.save(u);     // id becomes 1
System.out.println(set.contains(u));
```

### ✅ Answer
```
false
```

### 📏 The rule
`HashSet` buckets by `hashCode` **at insertion time**. Persisting changes the id, so the hash changes and the object lands in a different bucket — it's lost. **Never use a generated id in `hashCode`.**

### 🔁 Follow-up: *"What do you use instead?"*
A business key, or a **constant** `hashCode` (`getClass().hashCode()`) with id-based `equals`.

---

## Q10. Bulk update and the stale entity

```java
@Transactional
public void run(Long id) {
    User user = userRepository.findById(id).orElseThrow();   // status = ACTIVE
    userRepository.deactivateAll();                          // @Modifying UPDATE user SET status='OFF'
    System.out.println(user.getStatus());
}
```

### ✅ Answer
```
ACTIVE
```
— the **stale** value, even though the DB now says `OFF`.

### 📏 The rule
`@Modifying` bulk queries go **straight to the database** and bypass the persistence context. The already-loaded entity is not refreshed.

### 🔁 Follow-up: *"Fix?"*
`@Modifying(clearAutomatically = true, flushAutomatically = true)`, or `em.refresh(user)`.

---

## Q11. `@Modifying` missing

```java
@Query("update User u set u.status = 'OFF' where u.id = :id")
void deactivate(@Param("id") Long id);
```

### ✅ Answer
Runtime exception: **`Not supported for DML operations`** / `InvalidDataAccessApiUsageException`.

### 📏 The rule
Any JPQL `update`/`delete` in a Spring Data repository needs **`@Modifying`** (and a transaction).

---

## Q12. `getReference` on a missing row

```java
@Transactional
public void test() {
    User u = em.getReference(User.class, 999L);   // no such row
    System.out.println("got proxy");
    System.out.println(u.getName());
}
```

### ✅ Answer
```
got proxy
```
then **`EntityNotFoundException`**.

### 📏 The rule
`getReference` returns an **uninitialized proxy** — no SQL yet. The database is hit on the first non-id property access, and that's where it fails. `find()` would have returned `null` immediately.

---

## Q13. `JOIN FETCH` duplicates

```java
@Query("select u from User u join fetch u.orders")
List<User> findAllWithOrders();
// 1 user with 3 orders
System.out.println(findAllWithOrders().size());
```

### ✅ Answer
**Hibernate 5:** `3` (the SQL join produces 3 rows → 3 references to the same `User`).
**Hibernate 6 / Boot 3:** `1` — entity results are de-duplicated automatically.

### 📏 The rule
A collection fetch join multiplies parent rows. Pre-Hibernate-6 you needed `select distinct u`. **Know both** — the interviewer's mental model is probably Hibernate 5.

---

## Q14. Two `List` fetch joins

```java
@Query("select u from User u join fetch u.orders join fetch u.addresses")
List<User> findAll();          // both are List
```

### ✅ Answer
**`MultipleBagFetchException: cannot simultaneously fetch multiple bags`** at startup.

### 📏 The rule
Two unordered `List`s ("bags") can't be fetch-joined in one query — the Cartesian product is ambiguous. **Fix:** make them `Set`, or fetch one and use `@BatchSize` for the other.

---

## Q15. `remove(int)` vs the collection you thought you had

```java
@Transactional
public void deleteOrder(Long userId, Order order) {
    User user = userRepository.findById(userId).orElseThrow();
    user.getOrders().remove(order);   // @OneToMany(mappedBy="user", cascade=ALL)  — no orphanRemoval
}
```

### ✅ Answer
**No `DELETE`.** The row stays with its `user_id` intact (or, if the FK is non-null and Hibernate nulls it, you get a constraint violation).

### 📏 The rule
`CascadeType.REMOVE` only cascades when the **parent** is removed. Removing a child *from the collection* needs **`orphanRemoval = true`**.

---

## Q16. `equals` against a proxy

```java
User a = em.find(User.class, 1L);
User b = em.getReference(User.class, 1L);
System.out.println(a.getClass() == b.getClass());
System.out.println(a.equals(b));
```

### ✅ Answer
```
false
```
then — with a `getClass()`-based `equals` — **`false`** (wrong!). With an `instanceof`-based `equals` — `true` ✅.

### 📏 The rule
A lazy proxy is a **generated subclass** (`User$HibernateProxy$xyz`), so `getClass()` never matches. Write `equals` with `instanceof`, or use `Hibernate.getClass(o)`.

---

## Q17. Enum ordinal drift

```java
public enum Status { ACTIVE, INACTIVE }        // stored with default @Enumerated → ORDINAL
// later someone changes it to:
public enum Status { PENDING, ACTIVE, INACTIVE }
```

### ✅ Answer
Every existing row silently shifts meaning: old `0` (`ACTIVE`) now reads as **`PENDING`**. No error, corrupted data.

### 📏 The rule
`@Enumerated`'s **default is `ORDINAL`**. Always write `@Enumerated(EnumType.STRING)`.

---

## Q18. Deleting a parent with 1000 children

```java
@Transactional
public void deleteUser(Long id) {
    userRepository.deleteById(id);     // @OneToMany(cascade = ALL) with 1000 orders
}
```

### ✅ Answer
**1002 statements** — 1 SELECT for the user, 1 SELECT for the orders, then **1000 individual DELETEs**.

### 📏 The rule
JPA cascade removal is **entity-by-entity** (it must run lifecycle callbacks). For bulk deletes use `@Modifying @Query("delete from Order o where o.user.id = :id")` or an `ON DELETE CASCADE` FK — and remember those bypass the persistence context.

---

## Q19. `@Transactional` on a `private` method

```java
@Service
public class ReportService {
    @Transactional
    private void generate() { ... }
}
```

### ✅ Answer
**No transaction.** It compiles, it runs, the annotation is silently ignored. *(Spring cannot proxy a private method; with class-based CGLIB proxies the same is true for `final` and `static`.)*

### 📏 The rule
Proxy-based AOP only intercepts **public**, **non-final**, **externally called** methods.

---

## Q20. Which entity state?

```java
@Transactional
public void states() {
    User u = new User("a@b.com");          // (1)
    em.persist(u);                          // (2)
    em.flush();
    em.detach(u);                           // (3)
    u.setName("changed");
    em.merge(u);                            // (4)
    em.remove(em.find(User.class, u.getId()));  // (5)
}
```

### ✅ Answer
| Point | State of `u` |
|---|---|
| (1) | **Transient** |
| (2) | **Managed** (INSERT queued) |
| (3) | **Detached** — `setName` is not tracked |
| (4) | `u` is **still detached**; the *returned* copy is managed and carries `"changed"` |
| (5) | The managed instance is **Removed** — DELETE at flush |

### 📏 The rule
`merge` never makes the argument managed. Memorize the state diagram in [section E](#e--entity-lifecycle--the-persistence-context).

---

# M — ODM side — what you actually use (MongoDB & Mongoose)

> This is **your** ground. Be fluent here — it's the honest counterweight to the JPA section.

### Spring Data MongoDB — mapping (RoboGebra)

```java
@Document(collection = "quiz_definition")
public class QuizDefinition {

    @Id
    private String id;                       // maps to _id (ObjectId as String)

    @Field("quiz_title")
    private String title;

    @Indexed(unique = true)
    private String slug;

    private List<Question> questions;        // ✅ EMBEDDED — no join needed

    @DBRef(lazy = true)                      // ⚠️ reference — extra query per document
    private Course course;

    @CreatedDate  private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    @Version private Long version;           // optimistic locking works in Mongo too
}
```

```java
public interface QuizRepository extends MongoRepository<QuizDefinition, String> {

    List<QuizDefinition> findByQuizDefinition_IdOrderByOrderAsc(String id);   // derived query

    @Query("{ 'status': ?0, 'score': { $gte: ?1 } }")
    List<QuizDefinition> findByStatusAndMinScore(String status, int score);

    @Aggregation(pipeline = {
        "{ $match: { courseId: ?0 } }",
        "{ $lookup: { from: 'question', localField: '_id', foreignField: 'quizId', as: 'questions' } }",
        "{ $sort: { order: 1 } }"
    })
    List<QuizWithQuestions> findQuizzesWithQuestions(String courseId);
}
```

### ⚠️ The three differences that catch people out

| | JPA / Hibernate | Spring Data MongoDB |
|---|---|---|
| **Dirty checking** | Automatic — change a managed entity, it updates | ❌ **None.** You must call `save()` explicitly |
| **Persistence context / L1 cache** | Yes | ❌ **None.** Two `findById` calls = two queries, two objects |
| **Lazy loading** | Proxies on every association | Only `@DBRef(lazy = true)`; the model answer is to **embed** instead |

> 💬 **Killer line:** *"The repository programming model is identical, but the engine isn't. In Mongo there's no persistence context — no dirty checking, no L1 cache — so I always call `save()` explicitly. And instead of solving N+1 with fetch joins, I solve it at the schema level by embedding, or with a `$lookup` in an `@Aggregation`."*

### Q: `MongoRepository` vs `MongoTemplate`?
`MongoRepository` = declarative, derived queries, great for CRUD. `MongoTemplate` = imperative, full control — dynamic criteria, partial updates (`$set` on one field instead of rewriting the whole document), bulk operations.

### Q: How do you avoid the N+1 equivalent in MongoDB?
1. **Embed** the child documents (the default answer — one read, one document).
2. **`$lookup`** in an aggregation pipeline (a server-side join).
3. Fetch the ids and do **one `findAllById`** batch query — never a loop of `findById`.
> ⚠️ **`@DBRef` is the N+1 machine of MongoDB** — each reference is a separate round trip. RoboGebra's `@Aggregation` pipelines exist precisely to avoid that.

### Mongoose (RoboGebra CRM — Node)

```js
const couponSchema = new mongoose.Schema({
  code:      { type: String, required: true, unique: true, uppercase: true },
  discount:  { type: Number, min: 0, max: 100 },
  customer:  { type: mongoose.Schema.Types.ObjectId, ref: 'Customer' },   // reference
  expiresAt: Date,
}, { timestamps: true });                    // createdAt / updatedAt

couponSchema.pre('save', function (next) {   // middleware / hook
  this.code = this.code.trim();
  next();
});

couponSchema.index({ code: 1, expiresAt: -1 });

const Coupon = mongoose.model('Coupon', couponSchema);

// "join" — populate resolves the reference (an extra query, like @DBRef)
const coupons = await Coupon.find({ active: true })
                            .populate('customer', 'name email')
                            .lean();        // lean() = plain JS objects, no Mongoose docs → much faster reads
```

| Mongoose concept | JPA equivalent |
|---|---|
| `Schema` | `@Entity` mapping metadata |
| `Model` | Repository/`EntityManager` |
| `populate()` | `JOIN FETCH` (but as an extra query) |
| `pre`/`post` hooks | `@PrePersist` / `@PostLoad` entity listeners |
| `lean()` | DTO projection (skip the managed-object overhead) |
| `timestamps: true` | `@CreatedDate` / `@LastModifiedDate` |
| Schema validators | Bean Validation (`@NotNull`, `@Size`) |

> 💬 *"So across the stack I've used two ODMs — Spring Data MongoDB on the Java side and Mongoose on the Node CRM — and the concepts map cleanly onto JPA: schema ↔ entity, model ↔ repository, populate ↔ fetch join, hooks ↔ entity listeners."*

### Q: ORM vs ODM — the summary answer
> *"An ORM maps objects to **rows across normalized tables**, so its hard problems are joins, lazy loading and N+1. An ODM maps objects to **documents**, so related data is usually embedded and read in a single round trip — the hard problems move to schema design, duplication and keeping denormalized copies consistent. JPA gives you a persistence context with dirty checking; Spring Data MongoDB doesn't, so writes are always explicit."*

---

# N — Schema management & migrations

### Q: `spring.jpa.hibernate.ddl-auto` values?

| Value | What it does | Use in |
|---|---|---|
| `none` | Nothing | ✅ **Production** |
| `validate` | Verifies the schema matches the entities, fails fast otherwise | ✅ Production (with Flyway) |
| `update` | Adds missing tables/columns; **never drops or alters** | Local only ⚠️ |
| `create` | Drops and recreates at startup | Local/tests |
| `create-drop` | `create`, plus drop on shutdown | Tests |

> ⚠️ **Never `update` in production** — it can't rename, drop, change a type, or apply a data migration, and it silently drifts from what you think the schema is.

### Q: Flyway vs Liquibase?
Both are **versioned migration** tools that keep an applied-migrations table and run pending scripts at startup.
- **Flyway** — plain SQL files (`V1__create_user.sql`), simple, DB-specific. Most common.
- **Liquibase** — changelogs in XML/YAML/JSON/SQL, database-agnostic, supports rollback.

✅ **Production setup:** `ddl-auto: validate` + Flyway. Hibernate proves the entities match; Flyway owns the schema.

---

# O — Performance checklist

The 12 things that actually make an ORM app slow:

1. **N+1 queries** — the single biggest cause. Fix with `@EntityGraph` / `JOIN FETCH` / `@BatchSize`.
2. **EAGER `-ToOne` associations** — set everything to `LAZY`.
3. **`open-in-view=true`** — turn it off; it hides N+1 in the view layer and holds connections.
4. **Loading entities for read-only screens** — use DTO projections instead.
5. **Missing `@Transactional(readOnly = true)`** on reads — you're paying for dirty checking you don't need.
6. **`IDENTITY` ids** — disables JDBC batching; use `SEQUENCE` with `allocationSize`.
7. **No batching configured** — `hibernate.jdbc.batch_size=50`, `order_inserts=true`, `order_updates=true`.
8. **Cascade deletes over large collections** — 1000 DELETEs; use a bulk query.
9. **Never clearing the persistence context in batch jobs** — `flush()` + `clear()` every N rows.
10. **Missing DB indexes** on FK and filter columns — ORM won't save you here.
11. **`select *` on wide tables with `@Lob` columns** — project only what you need.
12. **Not looking at the SQL** — turn on `show-sql` / `p6spy` / Hibernate statistics in dev.

```properties
spring.jpa.open-in-view=false
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.default_batch_fetch_size=25
logging.level.org.hibernate.SQL=DEBUG
```

---

# P — Rapid-fire answer table

| Question | One-line answer |
|---|---|
| ORM? | Maps Java objects to relational tables so you work with objects, not SQL |
| JPA vs Hibernate? | **Spec vs implementation**. Spring Data JPA is an abstraction on top of both |
| Persistence context? | The set of managed entities for a transaction — L1 cache + dirty checking + identity map |
| Entity states? | Transient → Managed → Detached / Removed |
| Dirty checking? | Hibernate compares an entity to its load-time snapshot at flush and auto-generates the UPDATE |
| `persist` vs `merge`? | `persist` = insert a transient entity, makes the argument managed. `merge` = copy a detached entity's state onto a **different** managed instance and return it |
| What does `save()` do? | `isNew()` ? `persist` : `merge` |
| Flush vs commit? | Flush writes SQL; commit ends the transaction. Flush ≠ commit |
| Default fetch types? | `-ToOne` = **EAGER** ⚠️, `-ToMany` = LAZY. Set everything to LAZY |
| N+1 problem? | 1 query for parents + N for children. Fix: `JOIN FETCH`, `@EntityGraph`, `@BatchSize`, DTO projection |
| `LazyInitializationException`? | Touching a lazy proxy after the session closed. Fix: DTO from the service, or fetch it eagerly for that query |
| Owning side? | The side holding the FK (`@ManyToOne`). `mappedBy` marks the **inverse** side, which is ignored on write |
| Cascade default? | **None** |
| `REMOVE` vs `orphanRemoval`? | `REMOVE` = when the parent is deleted. `orphanRemoval` = also when the child leaves the collection |
| L1 vs L2 cache? | L1 = per-EntityManager, always on. L2 = app-wide, off by default, needs a provider |
| `@Transactional` default rollback? | `RuntimeException` + `Error` only — **not** checked exceptions |
| Default propagation? | `REQUIRED` |
| Why doesn't `@Transactional` work? | Self-invocation, or a `private`/`final`/`static` method — the proxy is bypassed |
| `readOnly = true`? | Flush mode MANUAL → no dirty checking → writes silently don't persist |
| Optimistic vs pessimistic? | `@Version` check at UPDATE vs a DB row lock |
| `@Enumerated` default? | **ORDINAL** — always override with `STRING` |
| `ddl-auto` in production? | `validate` (or `none`) + Flyway. Never `update` |
| Lombok `@Data` on an entity? | Don't — broken `equals`/`hashCode`, and `toString` triggers lazy loads / infinite recursion |
| ORM vs ODM? | Objects↔**tables** (joins, lazy loading) vs objects↔**documents** (embedding, aggregation). No persistence context in Mongo |
| Do you use ORM at work? | *"ODM — Spring Data MongoDB on RoboGebra. I know JPA concepts; my production mapping work is document-based."* |

---

# Q — 12 self-test drills (answers hidden)

**1.** You load a `User` in a `@Transactional` method, change a field, and never call `save()`. Is the DB updated?

<details><summary>👉 Answer</summary>

**Yes** — dirty checking issues the UPDATE at commit. Without `@Transactional` on your method, the entity is detached and **nothing** happens.
</details>

**2.** `em.persist(user)` where `user` already has an id set and the row exists. What happens?

<details><summary>👉 Answer</summary>

`EntityExistsException` (or a `PersistenceException` at flush). Use `merge()` for detached entities.
</details>

**3.** How many SELECTs does `userRepository.findById(1L)` twice in one transaction produce?

<details><summary>👉 Answer</summary>

**One.** The second is served by the first-level cache, and the two references are `==` equal.
</details>

**4.** What's wrong with `@OneToMany(fetch = EAGER) private List<Order> orders;` on a user with 50 000 orders?

<details><summary>👉 Answer</summary>

Every single `findById(user)` loads 50 000 rows into memory. And eager collections can't be paginated. Use LAZY + an explicit fetch when needed.
</details>

**5.** `@Query("select u from User u join fetch u.orders")` with `Pageable`. What does Hibernate log?

<details><summary>👉 Answer</summary>

`HHH000104: firstResult/maxResults specified with collection fetch; applying in memory` — it loads **every** row and paginates in Java. Use `@EntityGraph` + `@BatchSize`, or page ids first.
</details>

**6.** Name the four entity states and one way into each.

<details><summary>👉 Answer</summary>

**Transient** (`new`), **Managed** (`persist`/`find`/`merge`), **Detached** (transaction ends / `detach` / `clear`), **Removed** (`remove`).
</details>

**7.** `@Transactional` method throws `IOException`. Rollback?

<details><summary>👉 Answer</summary>

**No.** Only `RuntimeException`/`Error` roll back by default. Add `rollbackFor = Exception.class`.
</details>

**8.** Why must a JPA entity not be `final`?

<details><summary>👉 Answer</summary>

Hibernate creates **proxy subclasses** for lazy loading. A final class (or final getter) can't be subclassed/overridden, so lazy loading silently degrades or fails.
</details>

**9.** You remove a child from `parent.getChildren()` and nothing is deleted. Why?

<details><summary>👉 Answer</summary>

`orphanRemoval = true` is missing. `CascadeType.REMOVE` only fires when the **parent** is deleted.
</details>

**10.** Does Spring Data MongoDB do dirty checking?

<details><summary>👉 Answer</summary>

**No.** There is no persistence context. You must call `save()` explicitly — this is the biggest behavioural difference from JPA and a great thing to volunteer in an interview.
</details>

**11.** What's the MongoDB equivalent of the N+1 problem and how do you avoid it?

<details><summary>👉 Answer</summary>

A loop of `findById`, or `@DBRef` resolution — one round trip per reference. Avoid it by **embedding**, by `$lookup` in an `@Aggregation`, or by one batched `findAllById`.
</details>

**12.** Interviewer: *"So you've used Hibernate in production?"* — your answer?

<details><summary>👉 Answer</summary>

*"Not Hibernate — my production persistence is **Spring Data MongoDB**, which is an ODM. I'm strong on the JPA concepts: entity lifecycle, persistence context, relationship mapping, lazy loading and N+1, `@Transactional`. The repository model is the same, so the transition is short."*
**Honest + specific + confident.** Never bluff this one — the follow-up is always "show me an entity."
</details>

---

# R — Answer scripts (say these out loud)

### 1. "Explain ORM in your own words." *(45 s)*
> *"ORM maps Java classes to database tables — class to table, field to column, object reference to foreign key — so instead of writing JDBC and converting `ResultSet` rows by hand, I work with objects and the framework generates the SQL. It solves the object-relational impedance mismatch: objects have inheritance, references and identity; tables have rows, foreign keys and primary keys. The trade-off is that it hides the SQL, so you have to know what it's generating — most ORM performance problems are N+1 queries that nobody looked at."*

### 2. "JPA vs Hibernate vs Spring Data JPA." *(30 s)*
> *"JPA is the specification — the API and annotations. Hibernate is the implementation that generates the SQL, and it's Spring Boot's default provider. Spring Data JPA sits on top and removes the DAO boilerplate: I declare a repository interface and Spring generates the implementation. So it's my repository → Spring Data JPA → JPA → Hibernate → JDBC → the database."*

### 3. "What's the persistence context?" *(40 s)*
> *"It's the set of entities the EntityManager is managing for the current transaction. It acts as a first-level cache, so within one transaction the same row always gives me the same object instance. It's also where dirty checking happens — Hibernate keeps a snapshot at load time and, at flush, compares it and generates the UPDATE. That's why I don't need to call `save()` on an entity I loaded inside a transaction."*

### 4. "Tell me about the N+1 problem." *(45 s)*
> *"It's when one query loads N parents and then each parent's lazy association triggers its own query — 100 users becomes 101 queries. I usually catch it by turning on SQL logging and watching the query count on a list endpoint. The fixes, in the order I'd reach for them: an `@EntityGraph` on the repository method because it composes with derived queries and pagination; a `JOIN FETCH` for a one-off query; `@BatchSize` to turn N queries into N-over-25 using `IN` clauses; or a DTO projection when the screen is read-only and doesn't need entities at all."*

### 5. "Have you used ORM on your current project?" *(35 s — the honest one)*
> *"RoboGebra is MongoDB, so it's an **ODM** rather than an ORM — Spring Data MongoDB with `@Document` models, `MongoRepository` derived queries, and `@Aggregation` pipelines where I need join-style reads without the round trips that `@DBRef` would cost. The repository programming model is identical to Spring Data JPA; what's different underneath is that there's no persistence context, so no dirty checking and no lazy loading — every write is an explicit `save()`. On the Node CRM side it's Mongoose, which is the same idea: schemas, models, `populate` for references, and pre-save hooks."*

### 6. "How would you design the persistence layer for a new module?" *(60 s)*
> *"I'd start from the read patterns rather than the tables. Entities with all associations LAZY, `open-in-view` off, and DTOs returned from the service layer so nothing lazy escapes the transaction. `@Transactional(readOnly = true)` on reads and a normal `@Transactional` on writes, with the transaction boundary at the service, never the controller. Sequence-based ids so JDBC batching works, `ddl-auto=validate` with Flyway owning the schema, and indexes on every foreign key and filter column. Then I'd watch the SQL log on the main list endpoints — the first thing that goes wrong in any ORM codebase is N+1."*

---

## ✅ Before the interview, make sure you can

- [ ] Say the **JPA vs Hibernate vs Spring Data JPA** answer in 30 seconds without hesitating
- [ ] Draw the **four entity states** and name a transition into each
- [ ] Explain **dirty checking** and why `save()` is unnecessary inside a transaction
- [ ] Explain **`persist` vs `merge` vs `save`** — including that `merge` returns a *different* object
- [ ] Explain the **N+1 problem** and give **four** fixes in preference order
- [ ] Explain **`LazyInitializationException`** and why `open-in-view=false` is the right default
- [ ] Name the **owning side** rule and why `mappedBy` collections don't write to the DB
- [ ] State the **default fetch types** (`-ToOne` = EAGER ⚠️) and the **default rollback rule** (unchecked only)
- [ ] Explain the **`@Transactional` self-invocation** trap
- [ ] Say the **honest RoboGebra = ODM** answer without sounding apologetic
- [ ] Explain **ORM vs ODM** in two sentences
- [ ] Answer at least 15 of the 20 output questions in [section L](#l--predict-the-output--orm-edition-20-programs) cold

---

---

## Quick Revision Sheet — read this the night before

```
YOUR HONEST LINE
  RoboGebra = MongoDB = ODM (not ORM). Say it first, then pivot to the CONCEPTS ⭐
  "Spring Data MongoDB is an ODM; I'm solid on JPA/Hibernate concepts."

LAYERING
  repository → Spring Data JPA → JPA API → Hibernate → JDBC → DB ⭐
  JPA = spec 📖 | Hibernate = implementation 🚗 | Spring Data = chauffeur 🧑‍✈️
  Boot 3 → jakarta.*, not javax.* ⚠️

ENTITY
  @Entity + @Id + NO-ARG CONSTRUCTOR + not final ⭐ (so a record can never be one)
  @Enumerated: the DEFAULT is ORDINAL → stores the POSITION → ALWAYS use STRING ⭐
  IDENTITY (MySQL, no batching ⚠️) | SEQUENCE (Postgres, batch-friendly ⭐)
  @MappedSuperclass = a BaseEntity with id/createdAt, no table of its own

PERSISTENCE CONTEXT ⭐ (the heart of it)
  = L1 cache + dirty checking + identity map  (a shopping trolley 🛒)
  4 states: TRANSIENT → MANAGED → DETACHED / REMOVED
  MANAGED  → changes auto-saved (dirty checking — the hotel minibar 🍫), no save() needed
  DETACHED → changes go NOWHERE 💥
  The question behind every tricky JPA question: "is it MANAGED right now?" ⭐

  persist() → NEW → the same object becomes managed
  merge()   → DETACHED → returns a DIFFERENT copy → USE THE RETURN VALUE ⭐⭐
  save()    → id null ? persist : merge
  flush = send the SQL | commit = make it permanent
  find() → SQL now | getReference() → lazy proxy, no SQL ⭐

RELATIONSHIPS
  @ManyToOne = the FK side = the OWNING side ⭐ | @OneToMany = mappedBy = the mirror
  ⚠️ #1 BUG: adding to the inverse collection saves NOTHING
     → helper methods that set BOTH sides ⭐
  ⚠️ @OneToMany without mappedBy → a surprise extra JOIN TABLE
  No cascade by default. ALL is fine for Order→OrderItem, DANGEROUS on @ManyToOne 💥
  orphanRemoval → also deletes a child removed from the collection

FETCHING
  "To-One is EAGER ⚠️, To-Many is LAZY" → override @ManyToOne to LAZY ⭐
  Lazy = proxies → which is why entities can't be final
  N+1 = 1 + N queries (the market 101 times) 💥
     FIX: JOIN FETCH (distinct) | @EntityGraph ⭐ | @BatchSize | DTO projection
  ⚠️ JOIN FETCH + Pageable → paginates IN MEMORY | two List fetches → MultipleBagFetch
  LazyInitializationException = touching lazy data after the session closed 🍽️
     → return a DTO from the service ⭐ | open-in-view=false

TRANSACTIONS
  A proxy: begin → commit on return → rollback on RuntimeException
  REQUIRED ⭐ (join) | REQUIRES_NEW ⭐ (independent — audit logs survive)
  ⚠️ CHECKED exceptions do NOT roll back → rollbackFor = Exception.class
  ⚠️ CATCHING the exception also prevents rollback
  ⚠️ SELF-INVOCATION this.method() → NO transaction at all 💥
  ⚠️ readOnly = true → writes SILENTLY vanish 💥
  Isolation: non-repeatable = a row CHANGED | phantom = a NEW row APPEARED 👻

CACHING
  L1 = per transaction, ALWAYS ON ⭐ | L2 = app-wide, opt-in, reference data only
  Batch jobs: flush() + clear() every 50 rows, or you OOM ⭐

LOCKING
  Optimistic @Version → UPDATE ... WHERE version = 1 → 0 rows → exception → retry ⭐
     (Google Docs — edit freely, flag the conflict)
  Pessimistic → SELECT ... FOR UPDATE → others wait (a library book 📕)

equals/hashCode
  NEVER put a generated id in hashCode (null → not null breaks HashSet) ⭐
  constant hashCode + id-based equals | use instanceof, NOT getClass() (proxies!)
  ⚠️ Lombok @Data on an entity = 3 bugs → use @Getter @Setter @NoArgsConstructor
  NEVER return an entity from a controller → map to a DTO inside the transaction ⭐
```

---

**Related files:** [06 — Spring & Spring Boot](./06-spring-boot.md) · [13 — MongoDB](./13-mongodb.md) · [18 — RoboGebra Technical Stack](./18-robogebra-technical-versions.md) · [23 — Java Predict the Output](./23-java-output-tricky-questions.md)
