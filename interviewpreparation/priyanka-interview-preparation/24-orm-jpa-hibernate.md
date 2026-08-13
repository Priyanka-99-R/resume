# ORM — JPA, Hibernate, Spring Data JPA (+ ODM: MongoDB & Mongoose)

> 🎯 **Why this file exists:** "Have you worked with ORM?" is asked in almost every Java/Spring round, and the follow-ups (`persist` vs `merge`, N+1, `LazyInitializationException`, `@Transactional`) are where candidates get exposed.
> **Your situation is specific:** your current project **RoboGebra uses MongoDB — that is an ODM, not an ORM**. This file gives you (a) the honest one-liner so you never get caught claiming JPA on RoboGebra, and (b) full ORM depth so you can still answer every JPA/Hibernate question confidently.

---

## Table of contents

| # | Section | What it covers |
|---|---|---|
| [A](#a--your-honest-position-read-this-first) | **Your honest position** | What you have really used — RoboGebra = ODM. Exact wording. |
| [B](#b--orm-fundamentals) | ORM fundamentals | What/why, impedance mismatch, ORM vs JDBC vs ODM |
| [C](#c--jpa-vs-hibernate-vs-spring-data-jpa) | JPA vs Hibernate vs Spring Data JPA | Spec vs implementation vs abstraction (top-3 asked) |
| [D](#d--entity-mapping-reference) | Entity mapping | `@Entity`, ids, generators, `@Column`, `@Embeddable`, inheritance, enums, dates |
| [E](#e--entity-lifecycle--the-persistence-context) | **Lifecycle & persistence context** | 4 states, dirty checking, flush, `persist` vs `merge` vs `save` — the heart of ORM |
| [F](#f--relationships) | Relationships | All 4, owning side, `mappedBy`, cascade, `orphanRemoval`, bidirectional helpers |
| [G](#g--fetching-n1-and-lazyinitializationexception) | Fetching & N+1 | LAZY/EAGER defaults, N+1 with 4 fixes, `LazyInitializationException`, DTO projections |
| [H](#h--transactions) | Transactions | `@Transactional`, propagation, isolation, rollback rules, the self-invocation trap |
| [I](#i--caching) | Caching | L1 vs L2 vs query cache |
| [J](#j--concurrency--locking) | Locking | Optimistic `@Version`, pessimistic locks |
| [K](#k--equals-hashcode-lombok--dto-traps) | equals/hashCode & Lombok | The `@Data`-on-entity disaster, DTO vs entity |
| [L](#l--predict-the-output--orm-edition-20-programs) | 🔴 **Predict the output (20)** | Same style as file 23, but ORM. Your last round was output-based. |
| [M](#m--odm-side--what-you-actually-use-mongodb--mongoose) | **ODM — what you actually use** | Spring Data MongoDB (RoboGebra), Mongoose (CRM), ORM vs ODM table |
| [N](#n--schema-management--migrations) | Schema & migrations | `ddl-auto` values, Flyway/Liquibase |
| [O](#o--performance-checklist) | Performance checklist | The 12 things that make ORM slow |
| [P](#p--rapid-fire-answer-table) | Rapid-fire table | 1-line answers for last-minute revision |
| [Q](#q--12-self-test-drills-answers-hidden) | 12 drills | Answers hidden — test yourself |
| [R](#r--answer-scripts-say-these-out-loud) | Answer scripts | Word-for-word answers for the 6 likely questions |

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
- "Yes, we use Hibernate on RoboGebra." *(One follow-up — "show me an entity" — and you're finished.)*
- "MongoDB is an ORM." *(It's an ODM. This exact slip is a common junior tell.)*
- "Spring Data JPA and Spring Data MongoDB are the same thing." *(Same programming model, completely different engine — no persistence context, no lazy loading, no dirty checking in Mongo.)*

### If they push: "So you've never written an `@Entity`?"
> *"Not in production, no — my production persistence is document-based. I've written JPA entities while learning and I understand the mapping model well. What I'd want to be careful about on day one is the things that bite you at scale: lazy loading boundaries, N+1 queries, and transaction demarcation."*

Naming the *risks* is what a 4-year developer sounds like. Naming nothing is what a fresher sounds like.

---

# B — ORM fundamentals

### Q: What is ORM?

**Object–Relational Mapping** is a technique that maps **Java objects to relational database tables** — class → table, field → column, object reference → foreign key — so you work with objects instead of writing SQL and manually converting `ResultSet` rows into objects.

### Q: What problem does ORM solve? (the "impedance mismatch")

The object model and the relational model disagree in five ways:

| Mismatch | Objects | Relational tables |
|---|---|---|
| **Granularity** | Many small classes (`Address` inside `User`) | Fewer, wider tables |
| **Inheritance** | `Employee extends Person` | No inheritance |
| **Identity** | `==` (reference) and `equals()` | Primary key only |
| **Associations** | Directional references (`user.getOrders()`) | Foreign keys — inherently bidirectional |
| **Navigation** | Walk the graph: `a.getB().getC()` | Joins; walking = N queries |

ORM bridges these gaps.

### Q: Advantages and disadvantages of ORM?

**Advantages**
- Removes JDBC boilerplate (`Connection`, `PreparedStatement`, `ResultSet`, mapping code).
- **Database portability** — dialect handles vendor SQL differences.
- Built-in **caching**, **dirty checking**, **lazy loading**, **transaction management**.
- Type safety and refactor-ability — rename a field, the compiler finds usages.
- Automatic protection against SQL injection (bind parameters).

**Disadvantages**
- **Hides the SQL** — easy to generate awful queries (N+1) without noticing.
- Learning curve is real; the "magic" bites when you don't understand the persistence context.
- Poor fit for **bulk operations** and heavy reporting/analytics queries — use native SQL or JDBC there.
- Extra memory/CPU overhead vs plain JDBC.

> 💬 **Great line to say:** *"ORM removes boilerplate, not the need to understand SQL. The two things I always check are the generated SQL in the logs and whether any collection access is causing N+1."*

### Q: ORM vs JDBC vs ODM?

| | **JDBC** | **ORM (JPA/Hibernate)** | **ODM (Spring Data MongoDB / Mongoose)** |
|---|---|---|---|
| Data model | Rows/columns | Objects ↔ tables | Objects ↔ **documents** (BSON/JSON) |
| You write | SQL + mapping code | Entities + JPQL/derived queries | Documents + derived queries/aggregations |
| Schema | You manage it | Generated/validated from entities | Schema-flexible (Mongoose can enforce it) |
| Joins | SQL joins | Relationship mapping + fetch strategies | Embed, or `$lookup` / `populate()` |
| Persistence context | none | **Yes** — L1 cache + dirty checking | **No** — you must call `save()` |
| Lazy loading | none | Yes (proxies) | Only `@DBRef(lazy = true)`; usually you embed instead |
| Transactions | manual | Declarative `@Transactional` | Needs a **replica set** (Mongo 4.0+) |

---

# C — JPA vs Hibernate vs Spring Data JPA

> 🔴 **This is the single most-asked ORM question. Have the 3-line answer memorized.**

| | **JPA** | **Hibernate** | **Spring Data JPA** |
|---|---|---|---|
| What is it | A **specification** (interfaces + annotations) | An **implementation** of JPA (the most popular ORM) | A **Spring abstraction on top of JPA** |
| Ships as | `jakarta.persistence-api` | `hibernate-core` | `spring-boot-starter-data-jpa` |
| Gives you | `EntityManager`, `@Entity`, JPQL | The engine + extras (`Session`, HQL, Criteria, 2nd-level cache, `@BatchSize`) | **Repository interfaces**, derived queries, pagination, auditing |
| Can run alone? | No — it's just an API | Yes | No — needs a JPA provider (Hibernate by default) |

### ✅ The memorized answer

> *"**JPA is the specification** — the standard API and annotations. **Hibernate is the implementation** that actually generates the SQL; it's the default provider in Spring Boot. **Spring Data JPA sits on top** and removes the DAO boilerplate — I declare a repository interface and Spring generates the implementation at runtime. So the layering is: my repository → Spring Data JPA → JPA API → Hibernate → JDBC → database."*

### Q: Other JPA implementations?
**EclipseLink** (the reference implementation), **OpenJPA**. Hibernate dominates in Spring.

### Q: `EntityManager` vs Hibernate `Session`?
`Session` is Hibernate's native API; `EntityManager` is the JPA standard one. In modern Hibernate, `Session` **extends** `EntityManager`. Prefer `EntityManager` for portability; unwrap when you need Hibernate-only features:
```java
Session session = entityManager.unwrap(Session.class);
```

### Q: `EntityManagerFactory` vs `EntityManager`?
- **`EntityManagerFactory`** — heavyweight, thread-safe, one per persistence unit, created at startup.
- **`EntityManager`** — lightweight, **not thread-safe**, one per transaction/request. Spring injects a thread-bound proxy, which is why sharing it is safe in a Spring bean.

### ⚠️ `javax` vs `jakarta` (Boot 3 trap — RoboGebra is Boot 3.2)
Spring Boot **3.x** uses **Jakarta EE 9+**, so every import moved:
```java
// Boot 2.x
import javax.persistence.Entity;
// Boot 3.x  ✅
import jakarta.persistence.Entity;
```
> 💬 *"On Boot 3.2 all the persistence and validation imports are `jakarta.*`, not `javax.*` — that's the single biggest migration cost when upgrading from Boot 2."*

---

# D — Entity mapping reference

### Q: What makes a class a valid JPA entity?

1. Annotated with `@Entity`
2. Has an `@Id`
3. Has a **public/protected no-arg constructor** (Hibernate needs it for reflection/proxies)
4. Is **not final**, and no final methods/fields (proxies subclass it)
5. Is a top-level class (not an interface/enum)

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

    @Enumerated(EnumType.STRING)          // ⚠️ never ORDINAL
    private Status status;

    @Temporal(TemporalType.TIMESTAMP)     // only needed for legacy java.util.Date
    private Date legacyDate;

    private LocalDateTime createdAt;      // java.time needs no annotation

    @Transient                            // not persisted
    private String fullDisplayName;

    @Lob
    private String description;           // CLOB / BLOB for byte[]

    protected User() { }                  // required no-arg constructor
}
```

| Annotation | Purpose |
|---|---|
| `@Entity` | Marks the class as persistent |
| `@Table` | Table name, schema, unique constraints, indexes |
| `@Id` | Primary key |
| `@GeneratedValue` | How the PK is generated |
| `@Column` | Column name, nullable, length, unique, precision/scale, `insertable`/`updatable` |
| `@Transient` | Field is **not** persisted |
| `@Enumerated(EnumType.STRING)` | Store enum by name |
| `@Lob` | Large object (CLOB/BLOB) |
| `@Embedded` / `@Embeddable` | Value object flattened into the same table |
| `@ElementCollection` | Collection of basic/embeddable types in a side table |
| `@Version` | Optimistic locking |
| `@CreatedDate` / `@LastModifiedDate` | Spring Data auditing (needs `@EnableJpaAuditing` + `@EntityListeners(AuditingEntityListener.class)`) |

### ⚠️ `@Enumerated(EnumType.ORDINAL)` — the classic bug
`ORDINAL` (the **default**!) stores the enum's *position*. Reorder or insert an enum constant and every existing row silently means something else.
```java
@Enumerated(EnumType.STRING)   // ✅ always do this
private Status status;
```

### Q: `@GeneratedValue` strategies?

| Strategy | How it works | Notes |
|---|---|---|
| `IDENTITY` | DB auto-increment column | Simple; **disables JDBC batch inserts** because Hibernate must hit the DB per insert to get the id |
| `SEQUENCE` | Database sequence | ✅ **Preferred** on Postgres/Oracle; supports batching and pre-allocation |
| `TABLE` | A separate table simulates a sequence | Portable but slow (row locking) — avoid |
| `AUTO` | Provider picks | On Hibernate 6 + Boot 3 this usually means a sequence |
| `UUID` | Hibernate 6: generates a UUID | Good for distributed ids; worse index locality than a bigint |

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
@SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 50)
private Long id;
```
> 💬 *"`IDENTITY` is the easy default but it kills batch inserts — Hibernate needs the generated id immediately, so it can't queue the statements. On Postgres I'd use `SEQUENCE` with an `allocationSize` so it fetches a block of ids at once."*

### Q: `@Embeddable` — when do you use it?
For a **value object** with no identity of its own — it lives in the parent's table.
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
    @Embedded private Address address;                     // → street, city, zip columns on user

    @Embedded
    @AttributeOverride(name = "city", column = @Column(name = "billing_city"))
    private Address billingAddress;                        // reuse with different column names
}
```

### Q: `@Embeddable` vs `@Entity`?
An **entity** has its own identity, table and lifecycle; an **embeddable** has none — it's part of the owner and dies with it.

### Q: Composite primary keys?
Two options: **`@EmbeddedId`** (preferred, cleaner) or **`@IdClass`**.
```java
@Embeddable
public class OrderItemId implements Serializable {   // must be Serializable + equals/hashCode
    private Long orderId;
    private Long productId;
}

@Entity
public class OrderItem {
    @EmbeddedId private OrderItemId id;
}
```

### Q: Inheritance mapping strategies?

| Strategy | Tables | Pros | Cons |
|---|---|---|---|
| `SINGLE_TABLE` (**default**) | One table + discriminator column | Fastest — no joins | Subclass columns must be **nullable** |
| `JOINED` | Parent table + child tables | Normalized, no nulls | Every read is a join |
| `TABLE_PER_CLASS` | One table per concrete class | No joins for single-type reads | Polymorphic queries need `UNION` |
| `@MappedSuperclass` | No table for the parent | Shares fields (`id`, `createdAt`) | Parent is **not** an entity — can't query it polymorphically |

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type")
public abstract class Payment { ... }

@Entity @DiscriminatorValue("CARD")
public class CardPayment extends Payment { ... }
```

`@MappedSuperclass` is the one you'll use most in real projects — a `BaseEntity` with `id`, `createdAt`, `updatedAt`.

---

# E — Entity lifecycle & the persistence context

> 🔴 **This section is the heart of ORM.** If you understand the persistence context, most "tricky" JPA questions answer themselves.

### Q: What is the persistence context?

The persistence context is the **set of entity instances the `EntityManager` is currently managing**, scoped to a transaction. It is:
- the **first-level cache** (always on, can't be disabled),
- the place where **dirty checking** happens,
- an **identity map** — one row = exactly one object instance inside a transaction.

### Q: The four entity states — draw this

```
                    new User()
                        │
                    ┌───▼────────┐
                    │ TRANSIENT  │  not in DB, not managed
                    └───┬────────┘
              persist() │              ▲ (garbage collected)
                    ┌───▼────────┐     │
    ┌──────────────►│  MANAGED   │─────┘
    │  merge()      │ (persistent)│  in persistence context → dirty checking ON
    │  find()       └──┬───────┬─┘
    │                  │       │ remove()
    │  detach()/clear()│       │
    │  tx commit       │   ┌───▼────────┐
    │                  │   │  REMOVED   │  scheduled for DELETE at flush
 ┌──┴─────────┐        │   └────────────┘
 │  DETACHED  │◄───────┘
 └────────────┘  was managed, context closed — changes are NOT tracked
```

| State | In DB? | Tracked? | How you get there |
|---|---|---|---|
| **Transient (new)** | ❌ | ❌ | `new User()` |
| **Managed (persistent)** | ✅ (or will be at flush) | ✅ | `persist()`, `find()`, `merge()`, JPQL result |
| **Detached** | ✅ | ❌ | transaction ended, `detach()`, `clear()`, `close()` |
| **Removed** | ✅ until flush | ✅ | `remove()` |

### Q: What is dirty checking?

While an entity is **managed**, Hibernate keeps a **snapshot** of its loaded state. At flush time it compares the current state to the snapshot and generates an `UPDATE` for whatever changed — **you never call `update()`**.

```java
@Transactional
public void renameUser(Long id, String newName) {
    User user = userRepository.findById(id).orElseThrow();
    user.setName(newName);          // managed → dirty checked
    // no save() needed — UPDATE fires automatically at commit ✅
}
```

> 💬 **Interview gold:** *"Inside a transaction I don't need to call `save()` on an entity I loaded — it's managed, so dirty checking generates the UPDATE at flush. People add a redundant `save()` out of habit; it's harmless but shows they don't know the persistence context."*

### Q: What is flush? When does it happen?

**Flush** = synchronize the persistence context to the database (execute the pending INSERT/UPDATE/DELETE). It does **not** commit.

Flush happens:
1. On **transaction commit**,
2. Before a **query whose result could be affected** by pending changes (`FlushModeType.AUTO`, the default),
3. When you call `flush()` explicitly.

`FlushModeType.COMMIT` flushes only at commit. `@Transactional(readOnly = true)` makes Hibernate use `FlushMode.MANUAL` — **nothing is flushed**, which is exactly why writes silently disappear in a read-only transaction.

### Q: `persist()` vs `merge()` vs `save()` — the most-asked lifecycle question

| | `persist(e)` | `merge(e)` | Spring Data `save(e)` |
|---|---|---|---|
| Argument state | must be **transient** (or already managed) | transient **or detached** | anything |
| Return | `void` — **the argument becomes managed** | **a different, managed copy**; the argument stays detached | the managed instance |
| On detached entity | throws `EntityExistsException` / `PersistenceException` | copies state onto a managed instance (SELECT then UPDATE) | delegates to `merge` |
| Behaviour | INSERT | SELECT + INSERT/UPDATE | `isNew()` ? `persist` : `merge` |

```java
// Spring Data's SimpleJpaRepository, essentially:
public <S extends T> S save(S entity) {
    if (entityInformation.isNew(entity)) { em.persist(entity); return entity; }
    else                                 { return em.merge(entity); }
}
```

> ⚠️ **The trap:** `merge` returns a *different object*. `save()` on a detached entity → keep working with the **returned** instance, not the one you passed in. (See [Q3 in section L](#q3-merge-returns-a-different-object).)

### Q: How does Spring Data decide an entity is "new"?
By default: **the `@Id` field is `null`** (or `0` for primitives). If you assign ids yourself (UUIDs, natural keys), Spring thinks the entity already exists and issues a pointless `SELECT` before every insert — fix it by implementing `Persistable<ID>` and overriding `isNew()`.

### Q: `find()` vs `getReference()`?

| | `find(User.class, 1L)` | `getReference(User.class, 1L)` |
|---|---|---|
| Hits the DB | **Immediately** | Not until you touch a non-id field |
| Returns | The entity (or `null`) | A **lazy proxy** |
| If missing | `null` | throws `EntityNotFoundException` **later** |

`getReference()` is useful for setting a foreign key without loading the row:
```java
order.setUser(em.getReference(User.class, userId));   // no SELECT on user ✅
```
Spring Data equivalents: `findById()` → `find`, `getReferenceById()` → `getReference`.

### Q: `remove()` vs `deleteById()` vs bulk delete?
- `remove(entity)` — entity must be **managed**; DELETE at flush; cascades apply.
- `deleteById(id)` — Spring Data does a `findById` first, then `remove` (so it can cascade). Two statements.
- `deleteAllInBatch()` / `@Modifying @Query("delete from ...")` — **one** bulk DELETE, but it **bypasses the persistence context and cascades**. Fast, but entities already loaded go stale.

---

# F — Relationships

### The four types

```java
// ── @OneToOne ──────────────────────────────────────────
@Entity
public class User {
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "profile_id")           // owning side — holds the FK
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
    @ManyToOne(fetch = FetchType.LAZY)         // ⚠️ always override — default is EAGER
    @JoinColumn(name = "user_id")              // owning side
    private User user;
}
@Entity
public class User {
    @OneToMany(mappedBy = "user",              // inverse side — "user" is the FIELD in Order
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

### Q: What is the "owning side" and why does it matter?

The **owning side is the one that holds the foreign key** — and it is the **only** side Hibernate looks at when writing to the database. The inverse side is marked with `mappedBy` and is read-only for persistence purposes.

- `@ManyToOne` is **always** the owning side.
- `mappedBy` goes on the **inverse** side and names the **field on the owning side**.

> ⚠️ **The #1 relationship bug:** you add to the inverse collection, nothing is saved. (See [Q5 in section L](#q5-adding-to-the-inverse-side-only).)

### ✅ Always write bidirectional helper methods
```java
public void addOrder(Order order) {
    orders.add(order);
    order.setUser(this);       // keeps BOTH sides consistent
}
public void removeOrder(Order order) {
    orders.remove(order);
    order.setUser(null);
}
```

### Q: What happens if you use `@OneToMany` without `mappedBy`?
Hibernate assumes **unidirectional** and creates an extra **join table** (`user_orders`) instead of using the FK on `order`. Almost always a mistake — either add `mappedBy` or use `@JoinColumn` on the `@OneToMany`.

### Q: Cascade types?

| Cascade | Propagates |
|---|---|
| `PERSIST` | `persist()` → children inserted |
| `MERGE` | `merge()` |
| `REMOVE` | `remove()` → children deleted |
| `REFRESH` | `refresh()` |
| `DETACH` | `detach()` |
| `ALL` | all of the above |

**There is no cascade by default.** `CascadeType.ALL` is fine for a true parent-child composition (`Order` → `OrderItem`) and **dangerous** on `@ManyToOne`/`@ManyToMany` — deleting one order would delete the user, or one student would delete shared courses.

### Q: `CascadeType.REMOVE` vs `orphanRemoval = true`?

| | `CascadeType.REMOVE` | `orphanRemoval = true` |
|---|---|---|
| Deletes children when **parent is deleted** | ✅ | ✅ |
| Deletes a child **removed from the collection** | ❌ (FK is just orphaned) | ✅ |

```java
user.getOrders().remove(order);
// orphanRemoval = true      → DELETE FROM orders WHERE id = ?
// CascadeType.REMOVE only   → nothing happens (or FK constraint violation)
```

### Q: `List` vs `Set` for collections?
- `Set` avoids duplicates and avoids the `MultipleBagFetchException` when fetch-joining two collections.
- `List` (a "bag" without `@OrderColumn`) preserves DB order only if you add `@OrderBy`.
- **Rule:** use `Set` for `@ManyToMany` and wherever you might fetch-join two collections.

---

# G — Fetching, N+1, and LazyInitializationException

### Q: Lazy vs eager — and what are the defaults?

| Association | **Default fetch** | What you should use |
|---|---|---|
| `@ManyToOne` | **EAGER** ⚠️ | `LAZY` |
| `@OneToOne` | **EAGER** ⚠️ | `LAZY` |
| `@OneToMany` | LAZY ✅ | LAZY |
| `@ManyToMany` | LAZY ✅ | LAZY |

> 💬 **Say this:** *"The `-ToOne` associations default to EAGER, which is the wrong default — every `findById` silently drags in a join or an extra query. I set `fetch = FetchType.LAZY` on all associations and fetch what I need explicitly with a join fetch or an entity graph."*

**Lazy works via proxies:** Hibernate returns a generated subclass; the real SELECT fires the first time you touch a non-id property. That's why entities **can't be `final`**.

### Q: What is the N+1 problem?

You run **1** query to fetch N parents, then **N** more queries — one per parent — to fetch each parent's association.

```java
List<User> users = userRepository.findAll();       // 1 query
for (User u : users) {
    System.out.println(u.getOrders().size());      // N queries (one per user) ❌
}
// 100 users → 101 queries
```

### The 4 fixes

**1. `JOIN FETCH` (JPQL)** — one query
```java
@Query("select distinct u from User u left join fetch u.orders")
List<User> findAllWithOrders();
```

**2. `@EntityGraph`** — declarative, works with derived queries and pagination
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

**4. DTO projection** — fetch only the columns you need, no entities at all
```java
@Query("select new com.app.dto.UserSummary(u.id, u.name, count(o)) " +
       "from User u left join u.orders o group by u.id, u.name")
List<UserSummary> findSummaries();
```

> 💬 *"My default fix is an `@EntityGraph` because it composes with derived queries and pagination. For read-only screens I prefer a DTO projection — it avoids loading entities into the persistence context at all."*

### ⚠️ `JOIN FETCH` + pagination
Combining `join fetch` on a **collection** with `Pageable` makes Hibernate log `HHH000104: firstResult/maxResults specified with collection fetch; applying in memory` — it loads **every** row and paginates in Java. Fixes: use `@EntityGraph` with `@BatchSize`, or do it in two queries (page the ids first, then fetch by those ids).

### ⚠️ `MultipleBagFetchException`
Fetch-joining **two `List` collections** at once throws `MultipleBagFetchException: cannot simultaneously fetch multiple bags`. Fix: make them `Set`, or fetch one per query.

### ⚠️ Why `distinct` in a `JOIN FETCH`?
A join to a collection multiplies parent rows. `select distinct u` de-duplicates the **object** references. (Hibernate 6 de-duplicates entity results automatically, so `distinct` is no longer required there — but say you know both.)

### Q: What is `LazyInitializationException`?

Accessing a lazy association **after the persistence context is closed**.

```java
@Transactional
public User getUser(Long id) { return userRepository.findById(id).orElseThrow(); }

// in the controller — transaction already committed
user.getOrders().size();   // ❌ LazyInitializationException: could not initialize proxy - no Session
```

**Fixes (best first):**
1. **Return a DTO** from the service — the mapping happens inside the transaction. ✅ Best.
2. **Fetch what you need** with `JOIN FETCH` / `@EntityGraph`.
3. Touch the association inside the transactional method (`Hibernate.initialize(user.getOrders())`).
4. `spring.jpa.open-in-view=true` — **on by default in Spring Boot**, which is why many apps never see this. It keeps the session open for the whole request, hiding N+1 into the view layer and holding a DB connection longer. **Best practice: set `spring.jpa.open-in-view=false`** and fix the fetching properly.

> 💬 *"Boot logs a warning about `open-in-view` at startup for a reason — it hides lazy loading problems until production. I'd turn it off and return DTOs from the service layer."*

---

# H — Transactions

### Q: What does `@Transactional` do?

Spring wraps the bean in a **proxy** that begins a transaction before the method, commits on normal return, and rolls back on a runtime exception.

```java
@Transactional
public void transfer(Long from, Long to, BigDecimal amount) {
    Account a = repo.findById(from).orElseThrow();
    Account b = repo.findById(to).orElseThrow();
    a.debit(amount);
    b.credit(amount);          // both UPDATEs flush at commit — all or nothing
}
```

### Q: Propagation levels?

| Propagation | Behaviour |
|---|---|
| **`REQUIRED`** (default) | Join the existing transaction, or start one |
| `REQUIRES_NEW` | **Suspend** the current one, always start a new independent transaction |
| `SUPPORTS` | Join if one exists, otherwise run non-transactionally |
| `NOT_SUPPORTED` | Suspend any transaction and run without one |
| `MANDATORY` | Must already be in a transaction, else `IllegalTransactionStateException` |
| `NEVER` | Must **not** be in a transaction, else exception |
| `NESTED` | Savepoint inside the current transaction — inner rollback doesn't kill the outer |

**Classic use of `REQUIRES_NEW`:** audit/log writes that must survive even if the business transaction rolls back.

### Q: Isolation levels?

| Isolation | Prevents |
|---|---|
| `READ_UNCOMMITTED` | nothing (dirty reads possible) |
| `READ_COMMITTED` | dirty reads *(Postgres/Oracle default)* |
| `REPEATABLE_READ` | dirty + non-repeatable reads *(MySQL default)* |
| `SERIALIZABLE` | everything, including phantom reads — slowest |

**The three read phenomena:** *dirty read* = you read uncommitted data; *non-repeatable read* = the same row changes between two reads; *phantom read* = the same query returns new rows.

### ⚠️ The rollback rule (asked constantly)
Spring rolls back on **`RuntimeException` and `Error` only** — **not** on checked exceptions.
```java
@Transactional(rollbackFor = Exception.class)   // ✅ roll back on checked exceptions too
public void process() throws IOException { ... }
```

### ⚠️ The self-invocation trap (the #1 `@Transactional` bug)
```java
@Service
public class OrderService {
    public void placeOrder() {
        saveOrder();          // ❌ internal call — bypasses the proxy, NO transaction
    }
    @Transactional
    public void saveOrder() { ... }
}
```
Because the proxy only intercepts calls that come **from outside** the bean. Same reason `@Transactional` on **`private`**, **`final`**, or **`static`** methods does nothing.
**Fixes:** move the method to another bean, self-inject, or use `AopContext.currentProxy()`.

### Q: `@Transactional(readOnly = true)` — what does it actually do?
Sets `FlushMode.MANUAL` so Hibernate **skips dirty checking** (less memory, faster), and hints the driver/DB that it's read-only. Use it on every read-only service method.
⚠️ **And it means any modification you make silently doesn't persist** — see [Q7 in section L](#q7-write-inside-a-read-only-transaction).

---

# I — Caching

| | **First-level (L1)** | **Second-level (L2)** | **Query cache** |
|---|---|---|---|
| Scope | `EntityManager` / transaction | `EntityManagerFactory` / app-wide, shared across sessions | Query + parameters → **ids** |
| On by default | ✅ **Always on, can't disable** | ❌ Off | ❌ Off |
| Provider | built in | EHCache, Caffeine, Infinispan, Hazelcast | needs L2 enabled |
| Enabled by | — | `@Cacheable` on the entity + `hibernate.cache.use_second_level_cache=true` | `hibernate.cache.use_query_cache=true` + `setHint(QueryHints.CACHEABLE, true)` |

```java
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Country { ... }
```

**L2 is for read-mostly reference data** (countries, currencies, config). Don't cache hot, frequently-written tables.

### Q: How do you clear the L1 cache and why would you?
`em.clear()` (detach everything) or `em.detach(entity)`. In a **batch job** you flush and clear every N rows to stop the persistence context growing until you OOM:
```java
for (int i = 0; i < items.size(); i++) {
    em.persist(items.get(i));
    if (i % 50 == 0) { em.flush(); em.clear(); }
}
```

### Q: Hibernate cache vs Spring's `@Cacheable`?
Different layers. Spring's `@Cacheable` caches **method return values** (any bean, any type). Hibernate's L2 caches **entities and collections** by id inside the ORM. RoboGebra uses `spring-boot-starter-cache` — that's the **Spring** one.

---

# J — Concurrency & locking

### Q: Optimistic vs pessimistic locking?

| | **Optimistic** | **Pessimistic** |
|---|---|---|
| Assumes | Conflicts are rare | Conflicts are likely |
| Mechanism | `@Version` column checked on UPDATE | Database row lock (`SELECT ... FOR UPDATE`) |
| Cost | No locks held — scales well | Holds locks; risk of deadlock/contention |
| On conflict | `OptimisticLockException` at commit → retry | The second transaction **waits** |
| Use for | Web apps, long "think time" between read and write | Short critical sections: inventory, seat booking, balances |

```java
@Entity
public class Product {
    @Id private Long id;
    @Version private Integer version;    // Hibernate increments it on every UPDATE
    private int stock;
}
// UPDATE product SET stock=?, version=2 WHERE id=? AND version=1
// 0 rows updated → someone else changed it → OptimisticLockException
```

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select p from Product p where p.id = :id")
Product findByIdForUpdate(@Param("id") Long id);
```

> 💬 *"For a checkout flow I'd use `@Version` optimistic locking with a retry, because the user has think time and holding a DB lock across that is unacceptable. For decrementing stock inside a short server-side transaction, pessimistic write is simpler and correct."*

---

# K — equals, hashCode, Lombok & DTO traps

### Q: How should you implement `equals`/`hashCode` on an entity?

**The rules:**
1. **Never** use a generated `@Id` in `hashCode` — it's `null` before persist and changes after, so an entity put in a `HashSet` before saving becomes unfindable.
2. Prefer a **business/natural key** (email, order number, a UUID you assign yourself).
3. If there's no natural key: **constant `hashCode`** + id-based `equals`.
4. Use `instanceof` / `getClass()` carefully — Hibernate proxies are subclasses, so `getClass()` comparisons fail against proxies.

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User other)) return false;          // handles proxies
    return id != null && id.equals(other.getId());
}
@Override
public int hashCode() { return getClass().hashCode(); }    // stable across the id transition
```

### ⚠️ Lombok `@Data` on an entity — three bugs at once

```java
@Data                 // ❌ generates equals/hashCode/toString over ALL fields
@Entity
public class User {
    @OneToMany(mappedBy = "user") private List<Order> orders;
}
```
1. **`equals`/`hashCode` over all fields** — including the id (see above) and lazy collections.
2. **`toString()` touches every field** → triggers lazy loading, or an infinite recursion on a bidirectional relationship (`User.toString()` → `Order.toString()` → `User.toString()` → `StackOverflowError`).
3. **`@Setter` on everything** breaks encapsulation of your invariants.

✅ **Use `@Getter @Setter @NoArgsConstructor` and write `equals`/`hashCode`/`toString` yourself** — or `@ToString(exclude = "orders")`.

### Q: DTO vs Entity — why never return an entity from a controller?
- **Leaks internals** — password hashes, audit columns, the whole object graph.
- **Serialization triggers lazy loading** → `LazyInitializationException` or accidental N+1 during JSON writing.
- **Couples your API contract to your schema** — a column rename becomes a breaking API change.
- Bidirectional relationships cause **infinite JSON recursion** (the `@JsonIgnore` / `@JsonManagedReference` band-aid).

✅ Map entity → DTO **inside the transactional service** (MapStruct, or by hand).

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

**Related files:** [06 — Spring & Spring Boot](./06-spring-boot.md) · [13 — MongoDB](./13-mongodb.md) · [18 — RoboGebra Technical Stack](./18-robogebra-technical-versions.md) · [23 — Java Predict the Output](./23-java-output-tricky-questions.md)
