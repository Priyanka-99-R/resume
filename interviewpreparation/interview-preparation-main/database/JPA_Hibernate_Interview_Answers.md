# JPA / Hibernate – 20 Important Interview Questions (5+ Years)

---

# Part 1: Core Concept Questions

---

## Q1. What is the difference between JPA and Hibernate?

**Summary:**
JPA is a **specification** — a set of interfaces and annotations that define how Java objects map to relational databases. Hibernate is an **implementation** of that specification, the most popular one. Think of JPA as the interface and Hibernate as the concrete class.

**Concept:**
- **JPA (Jakarta Persistence API)** — defines the standard: `@Entity`, `@Table`, `@Id`, `EntityManager`, JPQL, Criteria API. It lives in `jakarta.persistence` (formerly `javax.persistence`).
- **Hibernate** — implements every JPA interface and adds its own features on top: `Session`, HQL, second-level caching, custom types, multi-tenancy, `@Formula`, `@Where`, `@BatchSize`, etc.

In practice, you code against JPA interfaces and use Hibernate-specific features only when JPA falls short.

**When & Why in Real Projects:**
In every Spring Boot project I've worked on, the dependency is `spring-boot-starter-data-jpa`, which bundles Hibernate as the default provider. I write JPA annotations and JPQL for 90% of the work. I drop into Hibernate-specific APIs for things like second-level caching (`@Cache`), batch fetching (`@BatchSize`), or `StatelessSession` for bulk operations.

**Real-World Example:**

```java
// JPA standard — portable across providers
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;
}

// Hibernate-specific — adds batch fetching not in JPA spec
@Entity
@BatchSize(size = 25)  // Hibernate-only
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)  // Hibernate-only
public class Product { ... }
```

**Common Mistakes:**
- Saying "JPA and Hibernate are the same thing" — JPA is the contract, Hibernate is one implementation. EclipseLink is another.
- Over-relying on Hibernate-specific features — this makes switching providers harder. Use JPA standard unless you genuinely need something Hibernate offers.

**Closing:**
I always code to the JPA standard first. When I need features like batch size hints, second-level caching, or `StatelessSession`, I reach for Hibernate-specific APIs. This gives me portability where it matters and power where I need it.

---

## Q2. What are the different entity states in JPA/Hibernate?

**Summary:**
An entity goes through four lifecycle states: **Transient**, **Managed (Persistent)**, **Detached**, and **Removed**. Understanding these is critical because Hibernate's behavior — dirty checking, lazy loading, cascade operations — depends entirely on what state your entity is in.

**Concept:**

```
   new Entity()         persist()/save()
  ┌───────────┐       ┌──────────────┐       close()/detach()/clear()
  │ TRANSIENT │──────▶│   MANAGED    │──────────────▶┌──────────┐
  └───────────┘       │ (Persistent) │               │ DETACHED │
                      └──────┬───────┘◀──────────────└──────────┘
                             │              merge()
                      remove()
                             │
                      ┌──────▼───────┐
                      │   REMOVED    │
                      └──────────────┘
```

- **Transient** — `new Order()`. Just a Java object. Hibernate doesn't know about it. No ID assigned. Not tracked.
- **Managed (Persistent)** — attached to the persistence context. Hibernate **tracks every change**. Any field modification is automatically detected (dirty checking) and synced to the DB at flush time. Lazy loading works.
- **Detached** — was managed, but the session/transaction has ended. Changes are **not tracked**. Lazy loading throws `LazyInitializationException`. To re-attach, use `merge()`.
- **Removed** — marked for deletion. Will be deleted from DB on next flush.

**When & Why in Real Projects:**
This matters daily. When a REST controller receives a DTO, converts it to an entity and passes it to the service layer — that entity is **transient**. After `repository.save()`, it's managed. After the transaction commits and the response is sent, it's detached. If a background thread tries to access a lazy field on that detached entity, you get `LazyInitializationException`.

**Real-World Example:**

```java
@Transactional
public void processOrder(OrderDTO dto) {
    Order order = new Order();          // TRANSIENT — not tracked
    order.setCustomerId(dto.getId());

    entityManager.persist(order);       // MANAGED — Hibernate tracks it now
    order.setStatus("PROCESSING");      // dirty checking detects this change
                                        // no need to call save() again

    // flush happens at transaction commit — INSERT + UPDATE in one go
}

// After @Transactional method returns:
// order is now DETACHED — changes won't be tracked anymore
```

**Common Mistakes:**
- Modifying a detached entity and expecting Hibernate to pick it up — you must `merge()` it back.
- Calling `merge()` on a new entity when you should use `persist()` — `merge()` copies state to a *new* managed instance, which can cause unexpected duplicate inserts.
- Accessing lazy associations on a detached entity — always ensure lazy data is loaded *within* the transaction boundary.

**Closing:**
I think of the persistence context as a "tracking scope." Inside it, Hibernate watches everything. Outside it, entities are just plain objects. Most bugs I've debugged — `LazyInitializationException`, unexpected updates, missing inserts — come from misunderstanding which state an entity is in.

---

## Q3. What is the difference between EntityManager and Session in Hibernate?

**Summary:**
`EntityManager` is the **JPA standard** interface for persistence operations. `Session` is **Hibernate's native** interface that extends `EntityManager` with additional features. In Spring Data JPA, you work with `EntityManager`; you unwrap to `Session` only when you need Hibernate-specific capabilities.

**Concept:**

| Feature | EntityManager (JPA) | Session (Hibernate) |
|---------|-------------------|-------------------|
| Standard | JPA specification | Hibernate-specific |
| Package | `jakarta.persistence` | `org.hibernate` |
| Persist | `persist()` | `save()`, `persist()` |
| Find | `find()` | `get()`, `load()` |
| Delete | `remove()` | `delete()`, `remove()` |
| Query | JPQL, Criteria API | HQL, Criteria, native SQL |
| Extras | — | `StatelessSession`, `ScrollableResults`, `@Filter` |

`Session` **is** an `EntityManager` — Hibernate's `SessionImpl` implements both interfaces.

**When & Why in Real Projects:**
I use `EntityManager` for 95% of operations — it's standard, portable, and sufficient. I unwrap to `Session` for:
- `StatelessSession` for bulk inserts (no dirty checking overhead).
- `ScrollableResults` for streaming large result sets.
- Hibernate-specific features like `@Filter`, `setFlushMode()`, `evict()`.

**Real-World Example:**

```java
@PersistenceContext
private EntityManager entityManager;

// JPA standard — works with any provider
public Order findOrder(Long id) {
    return entityManager.find(Order.class, id);
}

// Need Hibernate-specific feature — unwrap
public void bulkInsert(List<Order> orders) {
    Session session = entityManager.unwrap(Session.class);
    StatelessSession stateless = session.getSessionFactory().openStatelessSession();
    Transaction tx = stateless.beginTransaction();
    for (Order order : orders) {
        stateless.insert(order);  // no dirty checking, no first-level cache
    }
    tx.commit();
    stateless.close();
}
```

**Common Mistakes:**
- Injecting `Session` directly in Spring — use `@PersistenceContext EntityManager` and unwrap when needed.
- Using `Session.save()` when `EntityManager.persist()` is sufficient — `save()` returns the ID immediately (may force an INSERT), while `persist()` defers it.

**Closing:**
I code against `EntityManager` and unwrap to `Session` only for Hibernate-specific power features. This keeps my code portable while still leveraging Hibernate's full capabilities when needed.

---

## Q4. What is the difference between persist(), save(), merge(), and update()?

**Summary:**
These methods move entities between lifecycle states, but they behave very differently in terms of when the SQL executes, whether they return a managed instance, and how they handle detached entities. Getting this wrong causes duplicate inserts, detached entity exceptions, or silent data loss.

**Concept:**

| Method | API | Input State | Behavior | Returns |
|--------|-----|-------------|----------|---------|
| `persist()` | JPA | Transient | Makes entity managed. SQL deferred to flush. | `void` |
| `save()` | Hibernate | Transient | Makes entity managed. Returns generated ID (may trigger immediate INSERT). | `Serializable` (ID) |
| `merge()` | JPA | Detached or Transient | Copies state to a **new** managed instance. Original remains detached. | Managed copy |
| `update()` | Hibernate | Detached | Re-attaches the **same** entity. Throws if another instance with same ID is already managed. | `void` |

**When & Why in Real Projects:**

- **`persist()`** — use for new entities. I prefer this because it doesn't force immediate SQL.
- **`merge()`** — use for detached entities (e.g., entity from a previous transaction or deserialized from a DTO). Always use the **returned** instance.
- **`save()` / `update()`** — Hibernate-native. `save()` is used in legacy code. `update()` is rare — `merge()` is safer.

**Real-World Example:**

```java
@Transactional
public Order createOrder(OrderDTO dto) {
    Order order = mapper.toEntity(dto);
    entityManager.persist(order);      // TRANSIENT → MANAGED
    return order;                      // this instance IS the managed one
}

@Transactional
public Order updateOrder(OrderDTO dto) {
    Order detached = mapper.toEntity(dto);  // has an ID, but not managed
    Order managed = entityManager.merge(detached);  // returns NEW managed copy
    // detached is still detached — changes to it won't be tracked!
    return managed;  // always return the merged copy
}
```

**The `merge()` trap:**

```java
// BUG — common mistake
Order detached = mapper.toEntity(dto);
entityManager.merge(detached);
detached.setStatus("CONFIRMED");  // THIS CHANGE IS LOST — detached is not tracked

// CORRECT
Order managed = entityManager.merge(detached);
managed.setStatus("CONFIRMED");  // tracked by dirty checking
```

**Common Mistakes:**
- Ignoring the return value of `merge()` — the original object is NOT managed. The returned copy is.
- Using `persist()` on an entity with an existing ID — it throws `EntityExistsException` if that ID is already in the persistence context.
- Using `update()` when another instance with the same ID is already loaded — throws `NonUniqueObjectException`. `merge()` handles this gracefully.

**Closing:**
My rule: `persist()` for new entities, `merge()` for detached entities, and always use the return value of `merge()`. In Spring Data JPA, `repository.save()` internally calls `persist()` for new entities and `merge()` for existing ones — so it handles this for you.

---

## Q5. What is Lazy Loading and Eager Loading in JPA/Hibernate?

**Summary:**
Lazy loading defers fetching related entities until you actually access them. Eager loading fetches everything upfront in one shot. Lazy is the default for collections (`@OneToMany`, `@ManyToMany`), eager is the default for single associations (`@ManyToOne`, `@OneToOne`). Getting the fetch strategy wrong is the #1 cause of Hibernate performance issues.

**Concept:**
- **Lazy** (`FetchType.LAZY`) — Hibernate returns a **proxy** for the association. The actual SQL runs only when you call a method on it (e.g., `order.getItems().size()`). If the session is closed, you get `LazyInitializationException`.
- **Eager** (`FetchType.EAGER`) — Hibernate fetches the association immediately with the parent, typically via a JOIN. Every time you load the parent, the association comes with it — whether you need it or not.

**Defaults:**

| Annotation | Default Fetch |
|-----------|---------------|
| `@OneToOne` | EAGER |
| `@ManyToOne` | EAGER |
| `@OneToMany` | LAZY |
| `@ManyToMany` | LAZY |

**When & Why in Real Projects:**
I always override `@ManyToOne` and `@OneToOne` to `LAZY`. Eager loading on these is a silent performance killer — you load an `Order`, which eagerly loads `Customer`, which eagerly loads `Address`, which eagerly loads `Country`... it cascades.

**Real-World Example:**

```java
@Entity
public class Order {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)   // override default EAGER
    private Customer customer;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)  // default LAZY
    private List<OrderItem> items;
}

// In service — explicitly fetch what you need per use case
@Transactional(readOnly = true)
public Order getOrderWithItems(Long orderId) {
    return entityManager.createQuery(
        "SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id", Order.class)
        .setParameter("id", orderId)
        .getSingleResult();
    // items loaded in ONE query via JOIN FETCH
}

@Transactional(readOnly = true)
public Order getOrderSummary(Long orderId) {
    return entityManager.find(Order.class, orderId);
    // items NOT loaded — we don't need them for a summary
}
```

**Common Mistakes:**
- **Leaving `@ManyToOne` as default EAGER** — this is the most common Hibernate performance mistake. It loads the entire object graph on every query.
- **Using `FetchType.EAGER` to "fix" `LazyInitializationException`** — this is a bandaid that causes far worse performance problems. Fix the root cause by ensuring lazy data is loaded within the transaction.
- **Not using `JOIN FETCH` or `@EntityGraph`** — lazy loading without explicit fetching leads to the N+1 problem (covered in Q6).

**Closing:**
My default rule: make **everything LAZY**, then use `JOIN FETCH` or `@EntityGraph` to explicitly load what each use case needs. This gives you control over performance per query rather than a global eager strategy that loads data you don't need.

---

## Q6. What is the N+1 query problem in Hibernate and how can you solve it?

**Summary:**
The N+1 problem is when Hibernate executes 1 query to load N parent entities, then N additional queries to load each parent's association — resulting in N+1 total queries instead of 1 or 2. It's the most common Hibernate performance problem, and I've seen it bring production systems to their knees.

**Concept:**
When you load a list of entities with a lazy association and then iterate over them accessing that association, Hibernate fires a separate SELECT for each entity:

```
SELECT * FROM orders;                    -- 1 query → 100 orders
SELECT * FROM order_items WHERE order_id = 1;   -- query 2
SELECT * FROM order_items WHERE order_id = 2;   -- query 3
...
SELECT * FROM order_items WHERE order_id = 100; -- query 101
-- Total: 101 queries instead of 1-2
```

**When & Why in Real Projects:**
This happens every time you fetch a list and then access a lazy collection in a loop — rendering a list of orders with their items, building a report, serializing to JSON. It's silent — the code works, it's just slow.

**Real-World Example — The Problem:**

```java
List<Order> orders = orderRepository.findAll();  // 1 query
for (Order order : orders) {
    order.getItems().size();  // N queries — one per order
}
```

**Solution 1 — JOIN FETCH (JPQL):**

```java
@Query("SELECT o FROM Order o JOIN FETCH o.items")
List<Order> findAllWithItems();
// Single query: SELECT o.*, i.* FROM orders o JOIN order_items i ON ...
```

**Solution 2 — @EntityGraph (declarative):**

```java
@EntityGraph(attributePaths = {"items", "items.product"})
@Query("SELECT o FROM Order o")
List<Order> findAllWithItemsAndProducts();
```

**Solution 3 — @BatchSize (Hibernate-specific):**

```java
@OneToMany(mappedBy = "order")
@BatchSize(size = 25)  // loads 25 collections at a time with IN clause
private List<OrderItem> items;
// Instead of 100 queries → 4 queries (100/25)
```

**Solution 4 — Subselect fetch:**

```java
@OneToMany(mappedBy = "order")
@Fetch(FetchMode.SUBSELECT)
private List<OrderItem> items;
// Loads ALL collections in a single subselect query
```

**Common Mistakes:**
- **Not detecting it** — enable `spring.jpa.show-sql=true` or use a query counter in tests to catch it early.
- **Using `JOIN FETCH` on multiple collections** — fetching two `@OneToMany` with `JOIN FETCH` causes a Cartesian product. Use `@BatchSize` or `@EntityGraph` for the second collection.
- **Assuming Spring Data's `findAll()` handles this** — it doesn't. Without explicit fetch strategy, lazy collections trigger N+1.

**Closing:**
I treat N+1 as a bug, not a performance tuning topic. In every project, I set up a test utility that counts SQL queries per operation. `JOIN FETCH` for single associations, `@BatchSize` for collections when multiple associations are involved. Detection is key — you can't fix what you can't see.

---

## Q7. What is the difference between get() and load() methods in Hibernate?

**Summary:**
`get()` hits the database immediately and returns `null` if the entity doesn't exist. `load()` returns a **proxy** without hitting the database and throws `ObjectNotFoundException` when you actually access it if it doesn't exist. In JPA terms, `find()` is equivalent to `get()`, and `getReference()` is equivalent to `load()`.

**Concept:**

| Feature | `get()` / `find()` | `load()` / `getReference()` |
|---------|-------------------|-----------------------------|
| DB Hit | Immediately | Deferred (returns proxy) |
| Not Found | Returns `null` | Throws exception on access |
| Return Type | Actual entity or null | Proxy (uninitialized) |
| Use Case | When you need the data | When you only need the reference |

**When & Why in Real Projects:**
`load()` / `getReference()` is useful when you need to set a foreign key relationship without loading the entire parent entity. For example, assigning a `Category` to a `Product` — you don't need all the category data, just its ID to set the FK.

**Real-World Example:**

```java
// get() / find() — loads the full entity
Customer customer = entityManager.find(Customer.class, customerId);
if (customer == null) {
    throw new NotFoundException("Customer not found");
}

// load() / getReference() — just need the reference for FK assignment
@Transactional
public void assignCategory(Long productId, Long categoryId) {
    Product product = entityManager.find(Product.class, productId);

    // No SELECT on categories table — just creates a proxy with the ID
    Category category = entityManager.getReference(Category.class, categoryId);
    product.setCategory(category);

    // Hibernate sets category_id = categoryId in the UPDATE — no need to load Category
}
```

**Common Mistakes:**
- Using `load()` / `getReference()` and then accessing the proxy outside the session — throws `LazyInitializationException`.
- Using `get()` / `find()` when you only need a reference for FK assignment — unnecessary database hit.
- Catching `null` from `load()` — it never returns `null`. It returns a proxy that throws on access if the row doesn't exist.

**Closing:**
My rule: `find()` when I need the data, `getReference()` when I just need to set a foreign key relationship. In practice, `find()` covers 95% of use cases. `getReference()` is a performance optimization for association assignment.

---

## Q8. What are the different Hibernate caching levels?

**Summary:**
Hibernate has two caching levels. **First-level cache** is the persistence context itself — automatic, per-session, always on. **Second-level cache** is shared across sessions, optional, and must be configured explicitly. Proper caching can reduce database load by 70-80% for read-heavy applications.

**Concept:**

| Feature | First-Level Cache (L1) | Second-Level Cache (L2) |
|---------|----------------------|------------------------|
| Scope | Per `Session` / `EntityManager` | Shared across all sessions (SessionFactory) |
| Lifecycle | Lives with the transaction | Lives with the application |
| Configuration | Always enabled, no config | Explicitly enabled with provider (Ehcache, Caffeine, Hazelcast) |
| Eviction | Automatic when session closes | TTL, size-based, or manual |
| What's cached | Entities loaded in current session | Entities, collections, query results |

**First-Level Cache — how it works:**

```java
@Transactional
public void example() {
    Order o1 = entityManager.find(Order.class, 1L);  // SQL SELECT
    Order o2 = entityManager.find(Order.class, 1L);  // NO SQL — served from L1 cache
    System.out.println(o1 == o2);  // true — same instance
}
```

Within a single transaction, Hibernate guarantees **repeatable reads** — the same `find()` returns the same Java instance without hitting the database.

**Second-Level Cache — setup and usage:**

```properties
# application.properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
spring.jpa.properties.hibernate.javax.cache.provider=org.ehcache.jsr107.EhcacheCachingProvider
```

```java
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)  // enables L2 for this entity
public class Product {
    @Id
    private Long id;
    private String name;
    private BigDecimal price;
}

// Session 1
Product p1 = entityManager.find(Product.class, 1L);  // SQL → cached in L2

// Session 2 (different transaction, different user)
Product p2 = entityManager.find(Product.class, 1L);  // NO SQL → served from L2 cache
```

**Cache concurrency strategies:**

| Strategy | Use Case |
|----------|----------|
| `READ_ONLY` | Reference data that never changes (countries, currencies) |
| `NONSTRICT_READ_WRITE` | Data that rarely changes, eventual consistency is OK |
| `READ_WRITE` | Data that changes, needs strong consistency (uses soft locks) |
| `TRANSACTIONAL` | Full JTA transaction support (requires JTA cache like Infinispan) |

**Query Cache (L2 extension):**

```java
@Query("SELECT p FROM Product p WHERE p.category = :cat")
@QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
List<Product> findByCategory(@Param("cat") String category);
```

**When & Why in Real Projects:**
- L1 is free and automatic — it prevents duplicate queries within a request.
- L2 is for reference/lookup data that's read far more than it's written — product catalogs, configuration, country lists.
- Query cache is for queries that run frequently with the same parameters.

**Common Mistakes:**
- **Enabling L2 on frequently updated entities** — cache invalidation overhead outweighs the benefit. Only cache read-heavy entities.
- **Using query cache without entity cache** — the query cache stores entity IDs, not full entities. Without L2 entity cache, each ID triggers a separate SELECT.
- **Not monitoring cache hit rates** — a cache with low hit rate is just wasted memory. Monitor with Hibernate statistics or your cache provider's metrics.
- **Forgetting that `entityManager.clear()` only clears L1** — L2 persists across sessions.

**Closing:**
L1 cache is something I rely on automatically for transactional consistency. L2 cache I enable selectively for read-heavy, rarely-updated entities — with monitoring. The biggest mistake is caching everything; the second biggest is caching nothing.

---

## Q9. What is Dirty Checking in Hibernate and how does it work?

**Summary:**
Dirty checking is Hibernate's mechanism for automatically detecting changes to managed entities and synchronizing them to the database at flush time — **without you calling save() or update()**. It's one of Hibernate's most powerful features, and also one of the most misunderstood.

**Concept:**
When Hibernate loads an entity into the persistence context, it takes a **snapshot** of all field values. At flush time (before transaction commit, before a query, or on manual flush), Hibernate compares the current state of every managed entity against its snapshot. If any field changed, Hibernate generates an UPDATE statement.

```
Load entity → snapshot stored → you modify fields → flush → compare with snapshot → UPDATE
```

**When & Why in Real Projects:**
This is why you can modify entity fields inside a `@Transactional` method and the changes are persisted without calling `save()`. It's not magic — it's dirty checking.

**Real-World Example:**

```java
@Transactional
public void approveOrder(Long orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    order.setStatus("APPROVED");          // just modify the field
    order.setApprovedAt(Instant.now());   // modify another field
    // NO save() call needed — dirty checking generates UPDATE at commit
}
```

Hibernate generates:
```sql
UPDATE orders SET status = 'APPROVED', approved_at = '2026-03-16T...' WHERE id = ?
```

**How it detects changes — deep comparison:**

```java
// Hibernate compares field by field:
// snapshot:  {status="PENDING", approvedAt=null, amount=100.00}
// current:   {status="APPROVED", approvedAt=2026-03-16, amount=100.00}
// dirty fields: status, approvedAt → include in UPDATE
```

**Performance optimization — `@DynamicUpdate`:**

By default, Hibernate includes **all columns** in the UPDATE, even unchanged ones (for prepared statement caching). For wide tables, use:

```java
@Entity
@DynamicUpdate  // only includes changed columns in UPDATE
public class Order { ... }
```

This generates `UPDATE orders SET status=?, approved_at=? WHERE id=?` instead of updating all 20 columns.

**Common Mistakes:**
- **Calling `save()` on an already-managed entity** — it's harmless but unnecessary and signals a misunderstanding of dirty checking.
- **Modifying entities unintentionally** — if you load an entity, modify it for a DTO transformation, and the transaction commits, Hibernate **will** update the database. Use `@Transactional(readOnly = true)` or detach the entity first.
- **Performance with large persistence contexts** — dirty checking compares every managed entity at flush. If you load 10,000 entities, Hibernate checks all 10,000 at flush. Use `clear()` periodically in batch operations.
- **Not understanding flush timing** — dirty checking runs at flush, not at `setStatus()`. If you query the DB before flush, you might not see your own changes unless you flush first.

**Closing:**
Dirty checking is why Hibernate feels magical — you just modify objects and the database follows. But understanding *when* and *how* it works is essential. I rely on it for standard CRUD, and I'm careful about unintentional modifications in read-only flows.

---

## Q10. What is the difference between @OneToOne, @OneToMany, @ManyToOne, and @ManyToMany?

**Summary:**
These annotations define the cardinality of relationships between entities. The key decisions are: which side owns the foreign key (`mappedBy`), which fetch strategy to use, and how cascading behaves. Getting these right determines both correctness and performance.

**Concept:**

| Annotation | Relationship | FK Location | Default Fetch |
|-----------|-------------|-------------|---------------|
| `@OneToOne` | 1 → 1 | Either side (owner has FK) | EAGER |
| `@ManyToOne` | N → 1 | Many side (child has FK column) | EAGER |
| `@OneToMany` | 1 → N | Many side (inverse of `@ManyToOne`) | LAZY |
| `@ManyToMany` | N → N | Join table | LAZY |

**When & Why in Real Projects:**
Every entity relationship you model uses one of these. The critical decisions:
1. **Which side owns the relationship?** — The side with the FK column. Use `mappedBy` on the inverse side.
2. **Fetch type** — Always override `@OneToOne` and `@ManyToOne` to `LAZY`.
3. **Cascade** — Only cascade from parent to child, never the reverse.

**Real-World Example — complete mapping:**

```java
// @ManyToOne / @OneToMany — Order ↔ OrderItems
@Entity
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // owning side — FK in orders table
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // Helper methods to maintain both sides
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}

@Entity
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // owning side — FK in order_items table
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}

// @OneToOne — User ↔ UserProfile
@Entity
public class User {
    @Id private Long id;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile profile;
}

@Entity
public class UserProfile {
    @Id private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")  // owning side — FK here
    private User user;
}

// @ManyToMany — Student ↔ Course
@Entity
public class Student {
    @Id private Long id;

    @ManyToMany
    @JoinTable(
        name = "student_courses",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();
}

@Entity
public class Course {
    @Id private Long id;

    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();
}
```

**Common Mistakes:**
- **Not maintaining both sides of bidirectional relationships** — setting `item.setOrder(order)` but not adding to `order.getItems()`. Hibernate uses the owning side for persistence, but in-memory state becomes inconsistent.
- **Using `List` for `@ManyToMany`** — Hibernate deletes all join table entries and re-inserts on modification. Use `Set` instead.
- **`@OneToOne` lazy loading limitation** — the inverse side (with `mappedBy`) cannot be truly lazy because Hibernate must query to know if the related entity exists (null vs proxy). The owning side (with `@JoinColumn`) can be lazy.
- **Missing `orphanRemoval = true`** — without it, removing an item from a collection only breaks the relationship; the orphan row remains in the database.
- **Cascading `REMOVE` on `@ManyToMany`** — deleting a student should NOT delete courses. Cascade REMOVE is for parent-child relationships only.

**Closing:**
I always make `@ManyToOne` the owning side, override everything to LAZY, use helper methods for bidirectional relationships, and use `Set` over `List` for `@ManyToMany`. These four rules prevent most JPA mapping issues.

---

# Part 2: Scenario-Based Questions

---

## S1. N+1 Query Problem — Detecting and Solving

**Scenario:** You fetch a list of 100 Orders, and each order loads its Customer and Items. You notice hundreds of SQL queries being executed.

**Summary:**
This is a classic N+1 — 1 query for orders, 100 for customers, 100 for items = 201 queries. I'd detect it with SQL logging, then fix it with `JOIN FETCH` for single associations and `@BatchSize` for collections.

**How I'd approach this in production:**

**Step 1 — Detect:**

```properties
# Enable SQL logging
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Better: use a query counter in tests
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

Or use a test assertion:

```java
@Test
void shouldNotHaveNPlusOne() {
    SQLStatementCountValidator.reset();
    orderService.getAllOrdersWithDetails();
    SQLStatementCountValidator.assertSelectCount(2); // expect at most 2 queries
}
```

**Step 2 — Fix:**

```java
// Option A: JOIN FETCH — one query for orders + customers
@Query("SELECT o FROM Order o " +
       "JOIN FETCH o.customer " +
       "JOIN FETCH o.items " +
       "WHERE o.status = :status")
List<Order> findWithDetailsForStatus(@Param("status") String status);

// Option B: @EntityGraph — declarative
@EntityGraph(attributePaths = {"customer", "items"})
List<Order> findByStatus(String status);

// Option C: @BatchSize — Hibernate loads in batches (best for multiple collections)
@Entity
public class Order {
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    @OneToMany(mappedBy = "order")
    @BatchSize(size = 25)   // loads 25 item collections per query
    private List<OrderItem> items;
}
```

**Why not JOIN FETCH everything?** Joining two collections (items + payments) creates a Cartesian product — 100 orders x 5 items x 3 payments = 1500 rows. Use `JOIN FETCH` for one collection and `@BatchSize` for others.

**Closing:**
Detect with SQL logging or query count assertions, fix with `JOIN FETCH` for single associations, `@BatchSize` for multiple collections. I never leave N+1 unfixed — it's a ticking time bomb under load.

---

## S2. LazyInitializationException — Fixing it Properly

**Scenario:** `LazyInitializationException: could not initialize proxy` when accessing a lazy-loaded entity outside the transaction.

**Summary:**
This means you're accessing a lazy proxy after the Hibernate session has closed. The fix is NOT to make it eager — it's to ensure the data is loaded within the transaction boundary.

**How I'd fix this:**

**Root cause:** The entity was loaded in a `@Transactional` method, but the lazy association is accessed *after* the method returns — in the controller, serializer, or view layer.

```java
// PROBLEM
@Transactional
public Order getOrder(Long id) {
    return orderRepository.findById(id).orElseThrow();
    // Transaction ends here — order.items is an uninitialized proxy
}

// In controller — session is closed
order.getItems().size();  // LazyInitializationException!
```

**Fix 1 — JOIN FETCH (Best approach):**

```java
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
Optional<Order> findByIdWithItems(@Param("id") Long id);
```

**Fix 2 — @EntityGraph:**

```java
@EntityGraph(attributePaths = {"items"})
Optional<Order> findById(Long id);
```

**Fix 3 — Initialize within transaction:**

```java
@Transactional(readOnly = true)
public Order getOrderWithItems(Long id) {
    Order order = orderRepository.findById(id).orElseThrow();
    Hibernate.initialize(order.getItems());  // force load within session
    return order;
}
```

**Fix 4 — Use a DTO projection (best for APIs):**

```java
@Query("SELECT new com.app.dto.OrderSummary(o.id, o.status, o.total) FROM Order o WHERE o.id = :id")
OrderSummary findSummaryById(@Param("id") Long id);
// No entity, no proxy, no lazy loading issue
```

**What NOT to do:**
- `FetchType.EAGER` — fixes this one case, destroys performance everywhere else.
- `spring.jpa.open-in-view=true` (Open Session in View) — keeps the session open until the HTTP response is rendered. It "works" but leaks transactions into the controller layer and hides N+1 problems. I always disable this.

**Closing:**
I always disable `open-in-view`, use `JOIN FETCH` or DTOs, and load everything I need within the `@Transactional` boundary. The exception is telling you exactly where your architecture boundary is wrong — listen to it.

---

## S3. Performance Issue with Large Dataset (50,000 Records)

**Scenario:** A query is fetching 50,000 records from the database and the application becomes slow.

**Summary:**
Loading 50K entities into a persistence context is expensive — each one gets a snapshot for dirty checking, lives in the first-level cache, and consumes heap. The solution depends on the use case: pagination for APIs, streaming for processing, and `StatelessSession` for bulk reads.

**How I'd optimize:**

**Option 1 — Pagination (for API responses):**

```java
@Query("SELECT o FROM Order o WHERE o.status = :status")
Page<Order> findByStatus(@Param("status") String status, Pageable pageable);

// Usage
Page<Order> page = orderRepository.findByStatus("ACTIVE",
    PageRequest.of(0, 50, Sort.by("createdAt").descending()));
```

**Option 2 — Keyset pagination (for large offsets):**

```java
// OFFSET-based pagination degrades at high page numbers
// Keyset pagination is O(1) regardless of page depth
@Query("SELECT o FROM Order o WHERE o.id > :lastId ORDER BY o.id ASC")
List<Order> findNextPage(@Param("lastId") Long lastId, Pageable pageable);
```

**Option 3 — Streaming with periodic clear (for processing):**

```java
@Transactional(readOnly = true)
public void processAllOrders() {
    try (Stream<Order> stream = orderRepository.streamAllByStatus("ACTIVE")) {
        AtomicInteger count = new AtomicInteger();
        stream.forEach(order -> {
            processOrder(order);
            if (count.incrementAndGet() % 100 == 0) {
                entityManager.clear();  // clear L1 cache to prevent OOM
            }
        });
    }
}
```

**Option 4 — StatelessSession (for bulk read-only operations):**

```java
Session session = entityManager.unwrap(Session.class);
StatelessSession stateless = session.getSessionFactory().openStatelessSession();
ScrollableResults results = stateless.createQuery("FROM Order WHERE status = 'ACTIVE'")
    .scroll(ScrollMode.FORWARD_ONLY);

while (results.next()) {
    Order order = (Order) results.get(0);
    processOrder(order);
    // No L1 cache, no dirty checking — minimal memory overhead
}
results.close();
stateless.close();
```

**Option 5 — DTO projection (if you don't need full entities):**

```java
@Query("SELECT new com.app.dto.OrderSummary(o.id, o.status, o.total) " +
       "FROM Order o WHERE o.status = :status")
List<OrderSummary> findSummaries(@Param("status") String status);
// Lightweight DTOs — no persistence context, no dirty checking
```

**Closing:**
I never load 50K entities into a persistence context for an API response. Paginate for APIs, stream with periodic `clear()` for batch processing, and use `StatelessSession` or DTO projections for read-only bulk operations.

---

## S4. Duplicate Queries in One Request

**Scenario:** Within a single request, Hibernate is executing the same SQL query multiple times.

**Summary:**
This typically happens when the same entity is fetched via different code paths within a single transaction, or when JPQL/native queries bypass the first-level cache. The L1 cache only works for `find()`/`getById()` lookups by primary key — JPQL queries always hit the database.

**How I'd diagnose and fix:**

**Root cause — JPQL bypasses L1 cache:**

```java
@Transactional
public void process(Long orderId) {
    // Query 1 — JPQL always hits the DB
    Order o1 = orderRepository.findByOrderNumber("ORD-123");

    // Service call that also needs this order
    enrichmentService.enrich(orderId);  // internally runs same query again

    // This would use L1 cache — find() by PK checks persistence context first
    Order o2 = entityManager.find(Order.class, orderId);  // NO SQL if already loaded
}
```

**Fix 1 — Pass the entity instead of re-querying:**

```java
@Transactional
public void process(Long orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    enrichmentService.enrich(order);  // pass the object, don't re-query
    shippingService.calculateShipping(order);
}
```

**Fix 2 — Enable Hibernate query cache for repeated queries:**

```java
@QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
@Query("SELECT o FROM Order o WHERE o.orderNumber = :num")
Order findByOrderNumber(@Param("num") String orderNumber);
```

**Fix 3 — Use `@Transactional` at the right level to share the persistence context:**

```java
// Ensure all calls share the same transaction/session
@Transactional
public OrderResponse handleOrder(Long orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    // All subsequent find(Order.class, orderId) calls within this
    // transaction return the cached L1 instance — zero SQL
    validateOrder(order);
    processPayment(order);
    return buildResponse(order);
}
```

**Closing:**
L1 cache works by primary key only. For repeated JPQL queries, either pass the entity around, enable query caching, or restructure to use `find()` by PK. I always check SQL logs during development to spot duplicate queries early.

---

## S5. Update Without Calling Save — Why Does This Happen?

**Scenario:** You updated an entity field but did not explicitly call `save()`, yet Hibernate still updates the database.

**Summary:**
This is **dirty checking** in action. A managed entity is tracked by Hibernate's persistence context. Any field change is detected at flush time and automatically written to the database. This is by design — it's one of Hibernate's core features.

**How I'd explain this:**

```java
@Transactional
public void updateStatus(Long orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();  // MANAGED
    order.setStatus("SHIPPED");  // modify in memory
    // No save() — but Hibernate will UPDATE at transaction commit
}
```

**What happens internally:**
1. `findById()` loads the entity and stores a **snapshot** of all fields.
2. You modify `status` from `"PENDING"` to `"SHIPPED"`.
3. At transaction commit, Hibernate **flushes** — compares every managed entity against its snapshot.
4. Detects `status` changed → generates `UPDATE orders SET status='SHIPPED' WHERE id=?`.

**When this is a problem — accidental updates:**

```java
@Transactional
public OrderDTO getOrderAsDTO(Long orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    order.setStatus(normalizeStatus(order.getStatus()));  // oops — modified entity!
    return mapper.toDTO(order);
    // At commit, Hibernate UPDATEs the row — unintended side effect
}
```

**Prevention:**

```java
// Fix 1: Use readOnly transaction — hints Hibernate to skip dirty checking
@Transactional(readOnly = true)
public OrderDTO getOrderAsDTO(Long orderId) { ... }

// Fix 2: Detach the entity before modifying
Order order = orderRepository.findById(orderId).orElseThrow();
entityManager.detach(order);
order.setStatus(normalizeStatus(order.getStatus()));  // safe — not tracked

// Fix 3: Use DTO projection — no entity, no dirty checking
@Query("SELECT new com.app.dto.OrderDTO(o.id, o.status) FROM Order o WHERE o.id = :id")
OrderDTO findDTOById(@Param("id") Long id);
```

**Closing:**
Dirty checking is powerful but requires awareness. I use `@Transactional(readOnly = true)` for all read operations to prevent accidental writes, and I never modify a managed entity unless I intend for the change to be persisted.

---

## S6. Multiple Database Transactions — Distributed Transactions

**Scenario:** You have two entities stored in different databases, and both updates must succeed or fail together.

**Summary:**
This is the classic distributed transaction problem. You have three options: XA/JTA transactions (strong consistency, high overhead), the **Saga pattern** (eventual consistency, practical for microservices), or the **Transactional Outbox pattern** (reliable event publishing). In modern microservices, I go with Saga + Outbox.

**How I'd approach this:**

**Option 1 — JTA/XA Two-Phase Commit (for monolith with multiple databases):**

```java
// Using Atomikos or Narayana as JTA transaction manager
@Bean
public PlatformTransactionManager transactionManager() {
    return new JtaTransactionManager(); // coordinates both datasources
}

@Transactional  // JTA manages both databases
public void transfer(TransferRequest req) {
    accountRepository.debit(req.getFromAccount(), req.getAmount());   // DB1
    ledgerRepository.credit(req.getToAccount(), req.getAmount());     // DB2
    // JTA: prepare both → commit both, or rollback both
}
```

Downsides: slow (two-phase commit adds latency), database driver must support XA, complex failure modes.

**Option 2 — Saga Pattern (for microservices, recommended):**

```java
// Choreography-based saga using events
@Service
public class OrderService {

    @Transactional
    public void placeOrder(Order order) {
        orderRepository.save(order);
        eventPublisher.publish(new OrderCreatedEvent(order));  // triggers payment
    }

    @EventListener
    @Transactional
    public void onPaymentFailed(PaymentFailedEvent event) {
        Order order = orderRepository.findById(event.getOrderId()).orElseThrow();
        order.setStatus("CANCELLED");  // compensating transaction
    }
}

@Service
public class PaymentService {  // different database

    @EventListener
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) {
        try {
            paymentRepository.charge(event.getOrderId(), event.getAmount());
            eventPublisher.publish(new PaymentSucceededEvent(event.getOrderId()));
        } catch (Exception e) {
            eventPublisher.publish(new PaymentFailedEvent(event.getOrderId()));
        }
    }
}
```

**Option 3 — Transactional Outbox (reliable event publishing):**

```java
@Transactional
public void placeOrder(Order order) {
    orderRepository.save(order);

    // Write event to outbox table in the SAME database/transaction
    outboxRepository.save(new OutboxEvent(
        "OrderCreated", objectMapper.writeValueAsString(order)));
    // A separate poller/CDC reads outbox and publishes to Kafka
}
```

This guarantees atomicity — the order and the event are in the same transaction. A background process (Debezium CDC or a poller) reads the outbox table and publishes to the message broker.

**Common Mistakes:**
- Using `@Transactional` and expecting it to span two separate databases without JTA — Spring's default transaction manager only handles one datasource.
- Publishing events before committing — if the transaction rolls back, the event is already sent and can't be recalled. Use Outbox pattern.
- Not implementing compensating transactions in Sagas — if step 3 fails, steps 1 and 2 must be rolled back with explicit compensation logic.

**Closing:**
For a monolith with two datasources, I use JTA if strong consistency is required. For microservices, Saga pattern with the Transactional Outbox. I avoid distributed transactions whenever I can by designing services to own their data end-to-end.

---

## S7. Fetch Strategy Optimization — User → Orders → OrderItems

**Scenario:** You have `User → Orders → OrderItems` relationships and loading them causes performance issues.

**Summary:**
The mistake is loading the entire graph eagerly. I design fetch strategies **per use case** — different queries for different API endpoints. No single global strategy works for all cases.

**How I'd design this:**

**Entity mapping — everything LAZY:**

```java
@Entity
public class User {
    @Id private Long id;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    private List<Order> orders;
}

@Entity
public class Order {
    @Id private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    private List<OrderItem> items;
}
```

**Use Case 1 — User profile page (no orders needed):**

```java
@Transactional(readOnly = true)
public User getUserProfile(Long userId) {
    return userRepository.findById(userId).orElseThrow();
    // Orders NOT loaded — exactly what we want
}
```

**Use Case 2 — User with orders (no items needed):**

```java
@Query("SELECT u FROM User u JOIN FETCH u.orders WHERE u.id = :id")
User findWithOrders(@Param("id") Long id);
```

**Use Case 3 — Full order details page (need items):**

```java
// Two queries — avoids Cartesian product
@Transactional(readOnly = true)
public User getUserWithFullOrderDetails(Long userId) {
    User user = entityManager.createQuery(
        "SELECT u FROM User u JOIN FETCH u.orders WHERE u.id = :id", User.class)
        .setParameter("id", userId)
        .getSingleResult();

    // Second query to batch-load items for all fetched orders
    entityManager.createQuery(
        "SELECT o FROM Order o JOIN FETCH o.items WHERE o IN :orders", Order.class)
        .setParameter("orders", user.getOrders())
        .getResultList();

    return user;  // orders.items are now initialized in L1 cache
}
```

**Use Case 4 — API list endpoint (use DTOs):**

```java
@Query("SELECT new com.app.dto.UserOrderSummary(u.id, u.name, COUNT(o), SUM(o.total)) " +
       "FROM User u LEFT JOIN u.orders o WHERE u.status = 'ACTIVE' GROUP BY u.id, u.name")
List<UserOrderSummary> findActiveUserSummaries();
// Lightweight — no entities, no proxies, minimal data transfer
```

**Common Mistakes:**
- `JOIN FETCH` on both `orders` and `items` in one query — creates Cartesian product (User x Orders x Items). Use two separate queries instead.
- One global fetch strategy for all endpoints — a user detail page doesn't need order items, but an order management page does.
- Not using `@BatchSize` as a safety net — even if you forget `JOIN FETCH`, `@BatchSize` reduces N+1 to N/batch queries.

**Closing:**
I never define fetch strategy at the entity level. I define it per use case with `JOIN FETCH`, `@EntityGraph`, or DTO projections. `@BatchSize` is my safety net for cases I miss. This approach gives each endpoint exactly the data it needs — no more, no less.

---

## S8. Pagination Problem — Efficient Pagination with JPA

**Scenario:** Your API returns large data lists, causing high memory usage.

**Summary:**
Standard `OFFSET`-based pagination works for small datasets but degrades badly at high page numbers — the database still scans all skipped rows. For large datasets, I use **keyset pagination** (also called seek method) and always return DTOs instead of full entities.

**How I'd implement this:**

**Basic pagination (fine for small datasets):**

```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByStatus(String status, Pageable pageable);
}

// Usage
Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
Page<Order> page = orderRepository.findByStatus("ACTIVE", pageable);
// page.getContent(), page.getTotalElements(), page.getTotalPages()
```

**The problem with OFFSET at scale:**

```sql
-- Page 1: fast
SELECT * FROM orders ORDER BY created_at DESC LIMIT 20 OFFSET 0;

-- Page 5000: SLOW — DB scans 100,000 rows, discards 99,980
SELECT * FROM orders ORDER BY created_at DESC LIMIT 20 OFFSET 99980;
```

**Keyset pagination (O(1) regardless of page depth):**

```java
@Query("SELECT o FROM Order o WHERE o.createdAt < :cursor ORDER BY o.createdAt DESC")
List<Order> findNextPage(@Param("cursor") Instant cursor, Pageable pageable);

// Usage
Instant cursor = lastItemFromPreviousPage.getCreatedAt();
List<Order> nextPage = orderRepository.findNextPage(cursor, PageRequest.of(0, 20));
```

```sql
-- Always fast — uses index on created_at
SELECT * FROM orders WHERE created_at < '2026-03-16T10:00:00' ORDER BY created_at DESC LIMIT 20;
```

**Slice instead of Page (skip COUNT query):**

```java
// Page<T> runs a COUNT(*) query for totalElements — expensive on large tables
// Slice<T> only checks if there's a next page — much cheaper
Slice<Order> findByStatus(String status, Pageable pageable);
```

**DTO projection for minimal memory:**

```java
@Query("SELECT new com.app.dto.OrderListItem(o.id, o.orderNumber, o.status, o.total, o.createdAt) " +
       "FROM Order o WHERE o.status = :status")
Page<OrderListItem> findListItems(@Param("status") String status, Pageable pageable);
```

**Streaming for export/batch (no pagination needed):**

```java
@Transactional(readOnly = true)
public void exportOrders(OutputStream out) {
    try (Stream<Order> stream = orderRepository.streamAllByStatus("COMPLETED")) {
        stream.forEach(order -> {
            writeToCsv(out, order);
            entityManager.detach(order);  // free memory
        });
    }
}
```

**Closing:**
For API responses, I use `Slice` with keyset pagination for infinite scroll, or `Page` with DTO projections for table views. I never load entities when a DTO suffices, and I switch to keyset pagination when the dataset exceeds a few thousand pages.

---

## S9. Concurrent Update Problem — Optimistic vs Pessimistic Locking

**Scenario:** Two users update the same record at the same time, causing inconsistent data.

**Summary:**
This is the classic lost update problem. Hibernate provides two solutions: **optimistic locking** (using `@Version` — best for most web applications) and **pessimistic locking** (using `SELECT FOR UPDATE` — for critical financial operations). I default to optimistic locking and only use pessimistic when data integrity absolutely requires it.

**Concept:**
- **Optimistic locking** — assumes conflicts are rare. Adds a `@Version` column. On UPDATE, Hibernate checks if the version matches. If someone else updated it first, throws `OptimisticLockException`. The application retries.
- **Pessimistic locking** — locks the row in the database with `SELECT FOR UPDATE`. Other transactions wait until the lock is released. Guarantees exclusive access but reduces throughput.

**Optimistic Locking (recommended for web apps):**

```java
@Entity
public class Order {
    @Id private Long id;

    @Version  // Hibernate manages this — increments on every update
    private Long version;

    private String status;
    private BigDecimal total;
}
```

Hibernate generates:
```sql
UPDATE orders SET status = 'SHIPPED', version = 6 WHERE id = 42 AND version = 5;
-- If 0 rows updated → someone else changed it → OptimisticLockException
```

**Handling the conflict:**

```java
@Service
public class OrderService {

    @Retryable(value = OptimisticLockException.class, maxAttempts = 3)
    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(newStatus);
    }
}

// Or handle manually in the controller
@PutMapping("/orders/{id}")
public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody OrderDTO dto) {
    try {
        orderService.update(id, dto);
        return ResponseEntity.ok().build();
    } catch (OptimisticLockException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body("This record was modified by another user. Please refresh and try again.");
    }
}
```

**Pessimistic Locking (for critical sections):**

```java
// Lock the row — other transactions wait
@Query("SELECT o FROM Order o WHERE o.id = :id")
@Lock(LockModeType.PESSIMISTIC_WRITE)
Order findByIdForUpdate(@Param("id") Long id);

@Transactional
public void deductBalance(Long accountId, BigDecimal amount) {
    Account account = accountRepository.findByIdForUpdate(accountId);  // SELECT ... FOR UPDATE
    if (account.getBalance().compareTo(amount) < 0) {
        throw new InsufficientBalanceException();
    }
    account.setBalance(account.getBalance().subtract(amount));
}
```

**Common Mistakes:**
- Not adding `@Version` to entities that can be concurrently updated — silent last-write-wins behavior.
- Using pessimistic locking everywhere — it serializes access and kills throughput. Only use it for operations like balance deduction where you can't tolerate a retry.
- Not handling `OptimisticLockException` — it bubbles up as a 500 error. Catch it and return a meaningful 409 Conflict response.
- Setting pessimistic lock timeout too high — long-held locks cause cascading waits. Use `@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))`.

**Closing:**
Optimistic locking with `@Version` is my default — it handles 99% of concurrent update scenarios with minimal performance impact. I reserve pessimistic locking for financial operations where a retry isn't acceptable — like balance deductions or seat reservations.

---

## S10. Bulk Update Performance — Updating 1 Million Records

**Scenario:** You need to update 1 million records in the database using Hibernate. How would you optimize this?

**Summary:**
Never load 1 million entities into the persistence context — dirty checking alone will kill your application. For bulk updates, use JPQL/SQL bulk updates, `StatelessSession`, or batch processing with periodic flush and clear. The right approach depends on whether you need entity logic or pure SQL is sufficient.

**How I'd approach this:**

**Option 1 — JPQL/SQL Bulk Update (fastest, no entities loaded):**

```java
@Modifying
@Query("UPDATE Order o SET o.status = 'ARCHIVED' WHERE o.status = 'COMPLETED' AND o.createdAt < :cutoff")
int archiveOldOrders(@Param("cutoff") Instant cutoff);

// Executes a single SQL UPDATE — no entities loaded, no dirty checking
// Returns number of affected rows
```

This is by far the fastest. One SQL statement, handled entirely by the database.

**Caveat:** Bypasses lifecycle callbacks, `@Version`, and second-level cache. You must manually evict the cache:

```java
@Transactional
public int archiveOrders(Instant cutoff) {
    int count = orderRepository.archiveOldOrders(cutoff);
    entityManager.getEntityManagerFactory().getCache().evict(Order.class);
    return count;
}
```

**Option 2 — Batch processing with flush/clear (when entity logic is needed):**

```java
@Transactional
public void updateOrderStatuses() {
    int batchSize = 50;
    List<Order> orders = orderRepository.findByStatus("PENDING");

    for (int i = 0; i < orders.size(); i++) {
        Order order = orders.get(i);
        order.setStatus(calculateNewStatus(order));  // entity logic needed

        if (i % batchSize == 0 && i > 0) {
            entityManager.flush();   // execute accumulated UPDATEs
            entityManager.clear();   // release memory — detach all entities
        }
    }
}
```

Enable JDBC batching:
```properties
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.batch_versioned_data=true
```

**Option 3 — StatelessSession (no L1 cache, no dirty checking):**

```java
public void bulkUpdate() {
    Session session = entityManager.unwrap(Session.class);
    StatelessSession stateless = session.getSessionFactory().openStatelessSession();
    Transaction tx = stateless.beginTransaction();

    ScrollableResults results = stateless
        .createQuery("FROM Order WHERE status = 'PENDING'")
        .scroll(ScrollMode.FORWARD_ONLY);

    int count = 0;
    while (results.next()) {
        Order order = (Order) results.get(0);
        order.setStatus("PROCESSING");
        stateless.update(order);  // immediate UPDATE, no batch

        if (++count % 1000 == 0) {
            log.info("Processed {} records", count);
        }
    }

    tx.commit();
    results.close();
    stateless.close();
}
```

**Option 4 — Native SQL batch with JDBC (maximum control):**

```java
@Transactional
public void bulkUpdateWithJdbc() {
    entityManager.unwrap(Session.class).doWork(connection -> {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE orders SET status = ? WHERE id = ?")) {
            int count = 0;
            for (Long id : orderIds) {
                ps.setString(1, "ARCHIVED");
                ps.setLong(2, id);
                ps.addBatch();
                if (++count % 1000 == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();  // remaining
        }
    });
}
```

**Decision tree:**

```
Need to update 1M records?
├── Can it be a simple SET WHERE? → JPQL/SQL bulk update (Option 1)
├── Need entity logic per record? → Batch with flush/clear (Option 2)
├── Read-heavy with simple updates? → StatelessSession (Option 3)
└── Maximum performance required? → Native JDBC batch (Option 4)
```

**Common Mistakes:**
- Loading all 1M entities with `findAll()` — OOM guaranteed. Stream or paginate.
- Not configuring `hibernate.jdbc.batch_size` — without it, Hibernate executes individual INSERT/UPDATE statements even with flush/clear.
- Forgetting to `clear()` after `flush()` — entities remain in L1 cache, memory grows linearly.
- Using bulk UPDATE and forgetting to evict L2 cache — stale cached data will be served to other requests.

**Closing:**
For pure data updates, JPQL bulk update is unbeatable — one SQL statement. When I need entity logic, I batch with `flush()`/`clear()` every 50-100 records. For extreme cases, I drop to native JDBC. The key is never letting 1 million entities live in the persistence context simultaneously.
