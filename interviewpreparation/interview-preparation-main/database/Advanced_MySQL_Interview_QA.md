# Advanced MySQL Interview Questions & Answers (5+ Years)
### Senior Java / Spring Boot Backend Developer Perspective
### Conceptual (10) + Scenario-Based (4) = 14 Questions

---

## Q1. What is the difference between InnoDB and MyISAM storage engines in MySQL?

**Summary:**
InnoDB is the default and production-standard engine — it supports transactions, row-level locking, foreign keys, and crash recovery. MyISAM is legacy — no transactions, table-level locking, and no crash recovery. I've never used MyISAM in production in the last 5 years. InnoDB is the only sensible choice for any transactional application.

**Comparison:**

| Feature | InnoDB | MyISAM |
|---------|--------|--------|
| Transactions | Yes (ACID compliant) | No |
| Locking | Row-level | Table-level |
| Foreign Keys | Yes | No |
| Crash Recovery | Yes (redo log, doublewrite buffer) | No (table corruption risk) |
| Full-Text Search | Yes (since MySQL 5.6) | Yes |
| Caching | Buffer pool (data + index) | Key cache (index only) |
| Storage | Clustered index (data stored with PK) | Heap (data stored separately) |
| COUNT(*) | Scans index (slower) | Stored metadata (instant) |
| Concurrent Reads/Writes | Excellent (row locks) | Poor (table locks) |
| MVCC | Yes | No |

**How InnoDB Stores Data — Clustered Index:**
```
InnoDB Table (data stored IN the primary key B+ tree):

Primary Key B+ Tree:
       [50]
      /    \
   [20,30] [70,80]
   /  |  \   |   \
  [20: full row data]  ← data IS the leaf node
  [30: full row data]
  [50: full row data]
  [70: full row data]
  [80: full row data]

→ PK lookup = single B+ tree traversal = O(log n) = FAST
→ Range scan on PK (WHERE id BETWEEN 20 AND 50) = sequential read = FAST

MyISAM Table (data stored separately, index points to row offset):
  Index File (.MYI):  PK → row offset (pointer)
  Data File (.MYD):   row offset → actual row data
  → Two lookups: index → pointer → data file = slower
```

**When MyISAM Might Still Appear:**
```
→ MySQL system tables (pre-8.0)
→ Legacy applications not yet migrated
→ Read-only analytics tables (rare, ClickHouse is better now)
→ COUNT(*) without WHERE (MyISAM returns instantly from metadata)
```

**Real-World Scenario:**
Inherited a legacy application using MyISAM tables. Under concurrent write load (50+ users), table-level locking caused massive contention — writes queued behind each other, average response time was 8 seconds. Migrated to InnoDB with `ALTER TABLE t ENGINE=InnoDB` — response time dropped to 200ms because row-level locking allowed concurrent writes to different rows.

**Closing:**
InnoDB is the only engine for production applications. Row-level locking, ACID transactions, crash recovery, and foreign keys are non-negotiable requirements. If someone suggests MyISAM, the answer is always "migrate to InnoDB."

---

## Q2. What are the different types of indexes in MySQL and when would you use them?

**Summary:**
Indexes are data structures (B+ trees or hash tables) that speed up data retrieval at the cost of slower writes and additional storage. The main types: Primary Key, Unique, Composite, Covering, Full-Text, and Spatial. Choosing the right index for the right query is the most impactful performance skill a backend developer can have.

**Index Types:**

| Type | What It Does | When to Use |
|------|-------------|-------------|
| **Primary Key** | Unique, NOT NULL, clustered index | Every table (auto-increment ID or natural key) |
| **Unique** | Ensures no duplicate values | Email, username, phone — business uniqueness |
| **Composite** | Index on multiple columns | Multi-column WHERE/ORDER BY queries |
| **Covering** | Index contains all columns the query needs | High-frequency queries — avoids table lookup |
| **Full-Text** | Text search with relevance scoring | Search functionality (LIKE '%keyword%' alternative) |
| **Prefix** | Index first N characters of a string | Long VARCHAR/TEXT columns |
| **Spatial** | R-tree index for geographic data | Location-based queries (lat/lng) |

**Composite Index — The Most Important Concept:**
```sql
-- Composite index on (status, created_at, customer_id)
CREATE INDEX idx_orders_status_date_cust
  ON orders (status, created_at, customer_id);

-- ✅ USES the index (leftmost prefix rule):
WHERE status = 'ACTIVE'
WHERE status = 'ACTIVE' AND created_at > '2026-01-01'
WHERE status = 'ACTIVE' AND created_at > '2026-01-01' AND customer_id = 123

-- ❌ DOES NOT use the index (skips leftmost column):
WHERE created_at > '2026-01-01'                    -- skips 'status'
WHERE customer_id = 123                             -- skips 'status' and 'created_at'
WHERE created_at > '2026-01-01' AND customer_id = 123  -- skips 'status'

-- This is the LEFTMOST PREFIX RULE:
-- Index (A, B, C) can be used for:  A  |  A,B  |  A,B,C
-- But NOT for:                       B  |  C    |  B,C
```

**Covering Index — Fastest Possible Query:**
```sql
-- Query: get order IDs and totals for active orders
SELECT order_id, total_amount
FROM orders
WHERE status = 'ACTIVE' AND created_at > '2026-01-01';

-- Covering index includes ALL columns the query touches
CREATE INDEX idx_covering ON orders (status, created_at, order_id, total_amount);

-- MySQL reads ONLY the index — never touches the table data
-- EXPLAIN shows: "Using index" (no table lookup = fastest possible)
```

**Full-Text Index — Search Without LIKE '%...%':**
```sql
-- ❌ LIKE with leading wildcard — NEVER uses index, full table scan
SELECT * FROM products WHERE name LIKE '%wireless mouse%';

-- ✅ Full-text index — uses inverted index, fast
ALTER TABLE products ADD FULLTEXT INDEX ft_name_desc (name, description);

SELECT *, MATCH(name, description) AGAINST('wireless mouse' IN BOOLEAN MODE) AS score
FROM products
WHERE MATCH(name, description) AGAINST('wireless mouse' IN BOOLEAN MODE)
ORDER BY score DESC;
```

**Index Guidelines:**
```
✅ DO index:
  → Columns in WHERE clauses (filter conditions)
  → Columns in JOIN conditions (foreign keys)
  → Columns in ORDER BY / GROUP BY
  → High-cardinality columns (many distinct values)

❌ DON'T index:
  → Low-cardinality columns (boolean, status with 2-3 values) — alone
  → Columns rarely used in WHERE
  → Small tables (< 1000 rows) — full scan is faster than index
  → Every column "just in case" — indexes slow down INSERT/UPDATE/DELETE
```

**Closing:**
The right index can turn a 30-second query into a 5ms query. Composite indexes with the leftmost prefix rule, covering indexes to eliminate table lookups, and EXPLAIN to verify the optimizer uses them — this is 80% of MySQL performance tuning.

---

## Q3. What is the difference between Clustered Index and Non-Clustered Index?

**Summary:**
A Clustered Index determines the physical order of data on disk — the table data IS the index (in InnoDB, this is the Primary Key). A Non-Clustered (Secondary) Index is a separate structure that stores the indexed columns plus a pointer back to the clustered index. Each table has exactly ONE clustered index but can have many non-clustered indexes.

**How They Work in InnoDB:**

```
CLUSTERED INDEX (Primary Key):
  B+ Tree where leaf nodes contain THE ACTUAL ROW DATA

  Root: [50]
        /    \
     [20,30]  [70,80]
     /  |  \    |   \
  ┌──────────┐ ┌──────────┐ ┌──────────┐
  │PK=20     │ │PK=30     │ │PK=50     │  ← Full row stored here
  │name=John │ │name=Jane │ │name=Bob  │
  │email=... │ │email=... │ │email=... │
  │age=30    │ │age=25    │ │age=35    │
  └──────────┘ └──────────┘ └──────────┘

  → One per table (the primary key)
  → Data is physically sorted by PK
  → PK lookup = single tree traversal = fastest possible


NON-CLUSTERED (Secondary) INDEX on 'email':
  B+ Tree where leaf nodes contain: indexed column + PRIMARY KEY value

  Root: [jane@...]
        /         \
  ┌────────────┐  ┌────────────┐
  │email=bob@  │  │email=john@ │
  │PK=50       │  │PK=20       │  ← stores PK, NOT row data
  └────────────┘  └────────────┘

  Lookup: WHERE email = 'john@...'
    Step 1: Search secondary index → find PK=20
    Step 2: Search clustered index with PK=20 → find full row
    This second lookup is called a "BOOKMARK LOOKUP" or "TABLE LOOKUP"
```

**Why This Matters for Performance:**

```sql
-- FAST: Primary key lookup (clustered index only)
SELECT * FROM users WHERE id = 123;
-- 1 tree traversal → done

-- MEDIUM: Secondary index lookup (two tree traversals)
SELECT * FROM users WHERE email = 'john@example.com';
-- Traversal 1: secondary index → finds PK=123
-- Traversal 2: clustered index → finds full row

-- FAST: Covering index (secondary index only, no bookmark lookup)
SELECT email, name FROM users WHERE email = 'john@example.com';
-- If index is: (email, name) → all data is IN the index
-- 1 tree traversal → done (no table lookup needed)
-- EXPLAIN: "Using index"
```

| Feature | Clustered (PK) | Non-Clustered (Secondary) |
|---------|----------------|--------------------------|
| Count per table | Exactly 1 | Up to 64 |
| Leaf content | Full row data | Indexed columns + PK pointer |
| Physical order | Determines row order on disk | Separate structure |
| Lookup speed | Fastest | Requires bookmark lookup to PK |
| Insert overhead | Minimal (append for auto-increment) | Each index updated on insert |

**Closing:**
In InnoDB, the Primary Key IS the clustered index — choose it wisely (auto-increment integer is best for write performance). Every secondary index stores the PK value, so a smaller PK means smaller secondary indexes. Understanding this structure is the foundation of all MySQL performance tuning.

---

## Q4. What is the ACID property in database transactions?

**Summary:**
ACID ensures database transactions are reliable. **Atomicity** — all or nothing. **Consistency** — data follows rules before and after. **Isolation** — concurrent transactions don't interfere. **Durability** — committed data survives crashes. InnoDB guarantees all four. These aren't theoretical concepts — I rely on them every time I write `@Transactional` in Spring Boot.

**Each Property Explained:**

```
A — ATOMICITY: "All or nothing"
  Transfer $500 from Account A to Account B:
    1. Debit Account A  (-$500)    ✅
    2. Credit Account B  (+$500)   ❌ Server crashes here!
  → Atomicity: BOTH changes roll back. Account A gets $500 back.
  → Without it: $500 disappears from A but never reaches B.

C — CONSISTENCY: "Data always follows rules"
  Rule: Account balance >= 0
  → Transaction that makes balance -100 is REJECTED
  → Foreign key constraints, unique constraints, check constraints enforced
  → Database moves from one valid state to another valid state

I — ISOLATION: "Concurrent transactions don't see each other's mess"
  Transaction 1: Reading total inventory count
  Transaction 2: Updating 100 product quantities simultaneously
  → Isolation: Transaction 1 sees either ALL old values or ALL new values
  → Without it: Transaction 1 reads 50 old + 50 new = wrong count (phantom reads)

D — DURABILITY: "Committed = permanent, even if power fails"
  → COMMIT returns → data is written to redo log (WAL) on disk
  → Even if server crashes 1ms after COMMIT, data is recoverable
  → InnoDB uses doublewrite buffer + redo log for durability
```

**Spring Boot @Transactional — ACID in Practice:**
```java
@Service
public class TransferService {

    @Transactional  // ← Guarantees ACID
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Account from = accountRepository.findById(fromAccountId)
            .orElseThrow(() -> new AccountNotFoundException(fromAccountId));
        Account to = accountRepository.findById(toAccountId)
            .orElseThrow(() -> new AccountNotFoundException(toAccountId));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
            // Consistency: balance can't go negative
        }

        from.debit(amount);   // Atomicity: if credit fails,
        to.credit(amount);    //   this debit is rolled back too

        accountRepository.save(from);
        accountRepository.save(to);

        // If exception occurs anywhere above → entire transaction rolls back
        // If method completes → COMMIT → Durable (survives crash)
    }
}
```

**Closing:**
ACID is why we use relational databases for financial data, inventory, and anything where data integrity matters. InnoDB guarantees all four properties. In Spring Boot, `@Transactional` is the primary way I leverage ACID — one annotation protects against partial updates, data corruption, and crash-related data loss.

---

## Q5. What are the different transaction isolation levels in MySQL?

**Summary:**
Isolation levels control how much one transaction can see another's uncommitted or committed changes. From least to most isolated: Read Uncommitted, Read Committed, Repeatable Read (MySQL default), and Serializable. Higher isolation = more consistency but lower concurrency. InnoDB defaults to Repeatable Read, which prevents dirty reads and non-repeatable reads using MVCC (Multi-Version Concurrency Control).

**The Four Levels:**

| Isolation Level | Dirty Read | Non-Repeatable Read | Phantom Read | Performance |
|----------------|:----------:|:-------------------:|:------------:|:-----------:|
| Read Uncommitted | Yes | Yes | Yes | Fastest |
| Read Committed | No | Yes | Yes | Fast |
| **Repeatable Read** (default) | No | No | No* | Good |
| Serializable | No | No | No | Slowest |

*InnoDB's Repeatable Read also prevents phantom reads using gap locks — unlike the SQL standard.

**What Each Problem Looks Like:**

```
DIRTY READ (Read Uncommitted):
  Tx1: UPDATE accounts SET balance = 0 WHERE id = 1;  (not committed)
  Tx2: SELECT balance FROM accounts WHERE id = 1;  → reads 0 ❌
  Tx1: ROLLBACK;  → balance is actually still 1000
  → Tx2 read data that NEVER existed. Dangerous.

NON-REPEATABLE READ (Read Committed):
  Tx1: SELECT balance FROM accounts WHERE id = 1;  → 1000
  Tx2: UPDATE accounts SET balance = 500 WHERE id = 1; COMMIT;
  Tx1: SELECT balance FROM accounts WHERE id = 1;  → 500 ❌
  → Same query, different result within same transaction.

PHANTOM READ (below Serializable):
  Tx1: SELECT COUNT(*) FROM orders WHERE status = 'PENDING';  → 10
  Tx2: INSERT INTO orders (status) VALUES ('PENDING'); COMMIT;
  Tx1: SELECT COUNT(*) FROM orders WHERE status = 'PENDING';  → 11 ❌
  → New rows appeared between two identical queries.
```

**InnoDB MVCC — How Repeatable Read Works:**
```
MVCC (Multi-Version Concurrency Control):
  → Each transaction gets a SNAPSHOT of the database at the time it starts
  → Reads see the snapshot, not other transactions' changes
  → Writes create NEW versions of rows (old versions kept for other snapshots)
  → No read locks needed — readers and writers don't block each other

Timeline:
  T=0:  Tx1 starts (snapshot taken: balance = 1000)
  T=1:  Tx2 starts, updates balance to 500, COMMITS
  T=2:  Tx1 reads balance → still sees 1000 (from its snapshot)
  T=3:  Tx1 commits
  → Tx1 consistently saw 1000 throughout — Repeatable Read guaranteed
```

**Setting Isolation Level:**
```sql
-- Session level
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- Transaction level
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
START TRANSACTION;
-- ...
COMMIT;
```

```yaml
# Spring Boot application.yml
spring:
  jpa:
    properties:
      hibernate:
        connection:
          isolation: 2  # 1=Read Uncommitted, 2=Read Committed, 4=Repeatable Read, 8=Serializable
```

```java
// Per-method isolation in Spring
@Transactional(isolation = Isolation.READ_COMMITTED)
public List<Order> getOrders() { ... }

@Transactional(isolation = Isolation.SERIALIZABLE)
public void transferFunds(Long from, Long to, BigDecimal amount) { ... }
```

**When to Use Each:**
```
Read Uncommitted → Almost never. Only for rough analytics where accuracy doesn't matter.
Read Committed   → When you need to see other committed changes mid-transaction.
                   Used by PostgreSQL as default. Good for reporting.
Repeatable Read  → Default. Best for most OLTP applications.
                   Consistent reads within a transaction.
Serializable     → Financial transactions requiring absolute consistency.
                   Slowest due to range locks.
```

**Closing:**
Repeatable Read (InnoDB default) is correct for 95% of applications. It uses MVCC for non-blocking reads and gap locks to prevent phantoms. I only change isolation level when there's a specific need — Read Committed for reporting (see latest data), Serializable for critical financial operations.

---

## Q6. What is the difference between WHERE and HAVING clauses?

**Summary:**
`WHERE` filters individual rows BEFORE grouping. `HAVING` filters groups AFTER aggregation. If you're not using `GROUP BY`, you probably don't need `HAVING`. Think of it as: `WHERE` is a row filter, `HAVING` is a group filter.

**Execution Order:**
```
FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY → LIMIT

1. FROM orders                           -- get all rows from table
2. WHERE status = 'COMPLETED'            -- filter rows BEFORE grouping
3. GROUP BY customer_id                  -- group remaining rows
4. HAVING COUNT(*) > 5                   -- filter GROUPS after aggregation
5. SELECT customer_id, COUNT(*) as cnt  -- pick columns
6. ORDER BY cnt DESC                    -- sort
7. LIMIT 10                             -- return top 10
```

**Practical Examples:**
```sql
-- WHERE: filter rows before grouping
-- "Total sales per customer, but only for completed orders"
SELECT customer_id, SUM(total_amount) AS total_sales
FROM orders
WHERE status = 'COMPLETED'       -- ← filters rows (before grouping)
GROUP BY customer_id;

-- HAVING: filter groups after aggregation
-- "Customers who have placed more than 5 orders"
SELECT customer_id, COUNT(*) AS order_count
FROM orders
GROUP BY customer_id
HAVING COUNT(*) > 5;            -- ← filters groups (after aggregation)

-- BOTH: filter rows, then filter groups
-- "Customers with more than $10,000 in completed orders"
SELECT customer_id, SUM(total_amount) AS total_sales
FROM orders
WHERE status = 'COMPLETED'       -- ① filter rows: only completed orders
GROUP BY customer_id
HAVING SUM(total_amount) > 10000;-- ② filter groups: only big spenders
```

| Feature | WHERE | HAVING |
|---------|-------|--------|
| Filters | Individual rows | Groups (after GROUP BY) |
| Runs | Before GROUP BY | After GROUP BY |
| Aggregates | Cannot use (SUM, COUNT, AVG) | Can use aggregates |
| Performance | Faster (reduces data early) | Slower (processes all groups first) |
| Index usage | Can use indexes | Cannot use indexes |

**Performance Tip:**
```sql
-- ❌ SLOW: Filtering in HAVING when WHERE works
SELECT customer_id, SUM(total_amount)
FROM orders
GROUP BY customer_id
HAVING status = 'COMPLETED';  -- status is per-row, not aggregate!

-- ✅ FAST: Use WHERE for row-level filters
SELECT customer_id, SUM(total_amount)
FROM orders
WHERE status = 'COMPLETED'    -- filter rows first, group less data
GROUP BY customer_id;
```

**Closing:**
`WHERE` for row filters (uses indexes, fast), `HAVING` for aggregate filters (post-grouping). Always push as much filtering as possible into `WHERE` — fewer rows to group means faster queries.

---

## Q7. What is the difference between INNER JOIN, LEFT JOIN, RIGHT JOIN, and FULL JOIN?

**Summary:**
`INNER JOIN` returns only matching rows from both tables. `LEFT JOIN` returns all rows from the left table plus matches from the right (NULL if no match). `RIGHT JOIN` is the reverse. `FULL JOIN` returns all rows from both tables (MySQL doesn't support it natively — use UNION). In production, I use `INNER JOIN` and `LEFT JOIN` 99% of the time.

**Visual Representation:**
```
Table A: customers          Table B: orders
┌────┬───────┐             ┌────┬─────────┬───────────┐
│ id │ name  │             │ id │ cust_id │ total     │
├────┼───────┤             ├────┼─────────┼───────────┤
│ 1  │ John  │             │101 │ 1       │ 500       │
│ 2  │ Jane  │             │102 │ 1       │ 300       │
│ 3  │ Bob   │             │103 │ 2       │ 700       │
│ 4  │ Alice │             │104 │ 99      │ 100       │ ← orphan (no customer 99)
└────┴───────┘             └────┴─────────┴───────────┘
   Bob (3) has no orders        Order 104 has no matching customer
   Alice (4) has no orders
```

```sql
-- INNER JOIN: Only rows that match in BOTH tables
SELECT c.name, o.id, o.total
FROM customers c INNER JOIN orders o ON c.id = o.cust_id;

-- Result:
-- John  | 101 | 500
-- John  | 102 | 300
-- Jane  | 103 | 700
-- (Bob, Alice excluded — no orders)
-- (Order 104 excluded — no matching customer)


-- LEFT JOIN: ALL from left (customers) + matching from right (orders)
SELECT c.name, o.id, o.total
FROM customers c LEFT JOIN orders o ON c.id = o.cust_id;

-- Result:
-- John  | 101 | 500
-- John  | 102 | 300
-- Jane  | 103 | 700
-- Bob   | NULL| NULL    ← included with NULLs (no orders)
-- Alice | NULL| NULL    ← included with NULLs (no orders)


-- RIGHT JOIN: ALL from right (orders) + matching from left (customers)
SELECT c.name, o.id, o.total
FROM customers c RIGHT JOIN orders o ON c.id = o.cust_id;

-- Result:
-- John  | 101 | 500
-- John  | 102 | 300
-- Jane  | 103 | 700
-- NULL  | 104 | 100    ← included (orphan order, no customer)


-- FULL OUTER JOIN (MySQL workaround — not natively supported):
SELECT c.name, o.id, o.total
FROM customers c LEFT JOIN orders o ON c.id = o.cust_id
UNION
SELECT c.name, o.id, o.total
FROM customers c RIGHT JOIN orders o ON c.id = o.cust_id;
```

**Real-World Use Cases:**

```sql
-- LEFT JOIN: "Show all customers and their order count (including 0)"
SELECT c.name, COUNT(o.id) AS order_count
FROM customers c
LEFT JOIN orders o ON c.id = o.cust_id
GROUP BY c.id, c.name;
-- Bob: 0, Alice: 0 — included because LEFT JOIN

-- LEFT JOIN with NULL check: "Find customers who NEVER ordered"
SELECT c.name
FROM customers c
LEFT JOIN orders o ON c.id = o.cust_id
WHERE o.id IS NULL;
-- Bob, Alice — efficient way to find unmatched rows

-- INNER JOIN: "Show orders with customer details" (only valid orders)
SELECT c.name, o.id, o.total
FROM orders o
INNER JOIN customers c ON o.cust_id = c.id;
-- Excludes orphan order 104 — only orders with valid customers
```

**Closing:**
`INNER JOIN` when you need only matched data. `LEFT JOIN` when you need all rows from the primary table even without matches (most common in reports — "all customers with or without orders"). Always index the JOIN columns (foreign keys) — unindexed joins on large tables cause full table scans.

---

## Q8. What is a deadlock in MySQL and how can it be resolved?

**Summary:**
A deadlock occurs when two transactions hold locks that each other needs — neither can proceed. MySQL automatically detects deadlocks and kills one transaction (the "victim") with a rollback. Prevention is better than detection: always access tables and rows in a consistent order, keep transactions short, and use appropriate isolation levels.

**How Deadlock Happens:**

```
Timeline:
  T=0: Tx1 → UPDATE accounts SET balance = 500 WHERE id = 1;  (locks row 1)
  T=1: Tx2 → UPDATE accounts SET balance = 300 WHERE id = 2;  (locks row 2)
  T=2: Tx1 → UPDATE accounts SET balance = 800 WHERE id = 2;  (⏳ waiting for Tx2's lock on row 2)
  T=3: Tx2 → UPDATE accounts SET balance = 600 WHERE id = 1;  (⏳ waiting for Tx1's lock on row 1)

  💀 DEADLOCK!
  Tx1 holds row 1, wants row 2
  Tx2 holds row 2, wants row 1
  → Neither can proceed

  MySQL detects this → kills Tx2 (smaller transaction) → Tx2 gets:
  ERROR 1213: Deadlock found when trying to get lock; try restarting transaction
  → Tx1 proceeds and commits
```

**How to Detect Deadlocks:**

```sql
-- See latest deadlock info
SHOW ENGINE INNODB STATUS;
-- Look for "LATEST DETECTED DEADLOCK" section

-- Monitor deadlocks in real-time
SELECT * FROM information_schema.INNODB_TRX;   -- active transactions
SELECT * FROM information_schema.INNODB_LOCKS;  -- current locks (MySQL 5.7)
SELECT * FROM performance_schema.data_locks;    -- current locks (MySQL 8.0+)

-- Enable deadlock logging
SET GLOBAL innodb_print_all_deadlocks = ON;
-- Logs every deadlock to MySQL error log
```

**Prevention Strategies:**

```java
// Strategy 1: Always access rows in the SAME ORDER
@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    // ✅ Always lock the lower ID first — consistent ordering prevents deadlock
    Long firstId = Math.min(fromId, toId);
    Long secondId = Math.max(fromId, toId);

    Account first = accountRepository.findByIdWithLock(firstId);   // SELECT ... FOR UPDATE
    Account second = accountRepository.findByIdWithLock(secondId);

    // Now proceed with the actual transfer
    if (fromId.equals(firstId)) {
        first.debit(amount);
        second.credit(amount);
    } else {
        second.debit(amount);
        first.credit(amount);
    }
}

// Strategy 2: Keep transactions SHORT — acquire, process, release quickly
@Transactional
public void processOrder(OrderRequest request) {
    // ❌ DON'T: call external API while holding a lock
    // paymentGateway.charge(request); // takes 2-5 seconds!

    // ✅ DO: prepare data first, then do DB work in a short transaction
    PaymentResult result = paymentGateway.charge(request); // outside transaction
    orderRepository.save(order);  // short DB operation
}

// Strategy 3: Retry on deadlock
@Retryable(
    retryFor = DeadlockLoserDataAccessException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 100, multiplier = 2)
)
@Transactional
public void updateInventory(Long productId, int quantity) {
    inventoryRepository.decrementStock(productId, quantity);
}
```

```sql
-- Strategy 4: Use SELECT ... FOR UPDATE with NOWAIT or SKIP LOCKED
-- Instead of waiting for a lock (and risking deadlock):

-- NOWAIT: Fail immediately if lock is held
SELECT * FROM inventory WHERE product_id = 123 FOR UPDATE NOWAIT;
-- Returns error immediately if row is locked — no deadlock possible

-- SKIP LOCKED: Skip locked rows (for queue-like processing)
SELECT * FROM tasks WHERE status = 'PENDING' LIMIT 10 FOR UPDATE SKIP LOCKED;
-- Returns only unlocked rows — multiple workers process without deadlock
```

**Prevention Checklist:**
```
□ Access tables and rows in consistent order across all transactions
□ Keep transactions as short as possible (no external API calls while holding locks)
□ Use appropriate indexes — unindexed WHERE clauses cause table-level locks
□ Implement retry logic for deadlock exceptions
□ Use SELECT ... FOR UPDATE NOWAIT or SKIP LOCKED where appropriate
□ Monitor deadlock frequency — more than 1/hour indicates a design problem
□ Consider optimistic locking (@Version) for low-contention scenarios
```

**Closing:**
Deadlocks are normal in concurrent systems — MySQL handles them automatically. The goal is prevention: consistent lock ordering, short transactions, proper indexes, and retry logic. If deadlocks are frequent, it's a design problem, not a MySQL problem.

---

## Q9. What is query optimization and how do you improve slow queries in MySQL?

**Summary:**
Query optimization is identifying why a query is slow and fixing it — through indexing, query rewriting, schema changes, or configuration tuning. My first tool is always `EXPLAIN` — it shows the query execution plan. 80% of slow queries are fixed by adding the right index.

**Step-by-Step Optimization Process:**

**Step 1 — Find Slow Queries:**
```sql
-- Enable slow query log
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;  -- log queries taking > 1 second
SET GLOBAL log_queries_not_using_indexes = 'ON';

-- Check slow log
SHOW VARIABLES LIKE 'slow_query_log_file';
-- /var/log/mysql/slow.log
```

**Step 2 — Analyze with EXPLAIN:**
```sql
EXPLAIN SELECT o.id, o.total_amount, c.name
FROM orders o
JOIN customers c ON o.customer_id = c.id
WHERE o.status = 'PENDING'
AND o.created_at > '2026-01-01'
ORDER BY o.created_at DESC
LIMIT 20;
```

```
+----+------+----------+------+------+------+---------+------+--------+-----------------------------+
| id | type | table    | type | key  | rows | filtered| Extra                                    |
+----+------+----------+------+------+------+---------+------+--------+-----------------------------+
| 1  |SIMPLE| o        | ALL  | NULL |500000|  1.5%   | Using where; Using filesort               |
| 1  |SIMPLE| c        | ALL  | NULL |50000 | 100%    | Using where                               |
+----+------+----------+------+------+------+---------+------+--------+-----------------------------+

RED FLAGS:
  type = ALL       → Full table scan (scanning 500,000 rows!)
  key = NULL       → No index used
  Using filesort   → Sorting in memory/disk instead of using an index
  rows = 500000    → Way too many rows scanned for 20 results
```

**Step 3 — Fix It:**
```sql
-- Add composite index matching WHERE + ORDER BY
CREATE INDEX idx_orders_status_date
  ON orders (status, created_at DESC);

-- Add index on JOIN column
CREATE INDEX idx_orders_customer_id
  ON orders (customer_id);

-- Re-run EXPLAIN:
+----+------+----------+------+--------------------+------+---------+----------+
| id | type | table    | type | key                | rows | filtered| Extra    |
+----+------+----------+------+--------------------+------+---------+----------+
| 1  |SIMPLE| o        | range| idx_orders_status  | 150  | 100%    | Using idx|
| 1  |SIMPLE| c        | eq_ref| PRIMARY           | 1    | 100%    |          |
+----+------+----------+------+--------------------+------+---------+----------+

FIXED:
  type = range    → Index range scan (only relevant rows)
  key = idx_...   → Index is being used
  rows = 150      → Scanning 150 rows instead of 500,000
  No filesort     → Index provides sort order
```

**Common Slow Query Patterns and Fixes:**

```sql
-- ❌ Pattern 1: Function on indexed column (kills index)
WHERE YEAR(created_at) = 2026
-- ✅ Fix: Range condition
WHERE created_at >= '2026-01-01' AND created_at < '2027-01-01'

-- ❌ Pattern 2: Leading wildcard LIKE
WHERE name LIKE '%john%'
-- ✅ Fix: Full-text index or trailing wildcard
WHERE MATCH(name) AGAINST('john')
-- OR
WHERE name LIKE 'john%'  -- uses index (no leading wildcard)

-- ❌ Pattern 3: SELECT * (fetches unnecessary columns)
SELECT * FROM orders WHERE status = 'PENDING'
-- ✅ Fix: Select only needed columns (enables covering index)
SELECT id, total_amount, created_at FROM orders WHERE status = 'PENDING'

-- ❌ Pattern 4: N+1 query problem (from JPA/Hibernate)
-- Hibernate executes: 1 query for orders + N queries for each customer
-- ✅ Fix: JOIN FETCH or @EntityGraph
@Query("SELECT o FROM Order o JOIN FETCH o.customer WHERE o.status = :status")
List<Order> findByStatusWithCustomer(@Param("status") String status);

-- ❌ Pattern 5: Unindexed JOIN column
SELECT * FROM orders o JOIN products p ON o.product_id = p.id;
-- If product_id is not indexed → full table scan on orders
-- ✅ Fix: Always index foreign keys
CREATE INDEX idx_orders_product_id ON orders (product_id);

-- ❌ Pattern 6: ORDER BY + LIMIT without supporting index
SELECT * FROM orders ORDER BY created_at DESC LIMIT 20;
-- Without index: sorts ALL rows, then returns 20
-- ✅ Fix: Index on sort column
CREATE INDEX idx_orders_created_at ON orders (created_at DESC);
```

**Closing:**
`EXPLAIN` is the single most important MySQL command after `SELECT`. Look for `type=ALL` (full scan), `key=NULL` (no index), `Using filesort` (sort not from index), and high `rows` counts. The fix is almost always a composite index matching the WHERE + ORDER BY pattern.

---

## Q10. What is the difference between DELETE, TRUNCATE, and DROP?

**Summary:**
`DELETE` removes rows one by one with logging and can be rolled back (DML). `TRUNCATE` removes all rows instantly by dropping and recreating the table structure (DDL). `DROP` removes the entire table including structure and data permanently. Use `DELETE` for selective removal, `TRUNCATE` to reset a table, and `DROP` to permanently remove a table.

**Comparison:**

| Feature | DELETE | TRUNCATE | DROP |
|---------|--------|----------|------|
| Type | DML (Data Manipulation) | DDL (Data Definition) | DDL |
| Removes | Specific rows (WHERE) or all | All rows | Entire table (structure + data) |
| WHERE clause | Yes | No | No |
| Rollback | Yes (within transaction) | No (auto-commits) | No (auto-commits) |
| Speed | Slow (row by row, logs each) | Fast (drops + recreates) | Fast |
| Auto-increment | Continues from last value | Resets to 1 | N/A (table gone) |
| Triggers | Fires DELETE triggers | Does NOT fire triggers | Does NOT fire triggers |
| Foreign keys | Checked per row | Fails if FK references exist | Fails if FK references exist |
| Disk space | Doesn't release immediately | Releases immediately | Releases immediately |
| Transaction log | Full logging (each row) | Minimal logging | Minimal logging |

```sql
-- DELETE: remove specific rows (rollback-safe)
DELETE FROM orders WHERE status = 'CANCELLED' AND created_at < '2025-01-01';
-- Can be rolled back if inside a transaction
-- Fires triggers, logs each row

-- TRUNCATE: remove ALL rows, reset table
TRUNCATE TABLE temp_import_data;
-- Instant, resets auto-increment
-- Cannot be rolled back
-- Use for: clearing staging/temp tables

-- DROP: remove the entire table
DROP TABLE IF EXISTS temp_import_data;
-- Table and all data gone permanently
-- All indexes, triggers, constraints also removed
```

**When to Use Each:**

```
DELETE → Remove specific rows based on a condition
         "Delete orders older than 2 years"
         When you need triggers to fire
         When you need rollback capability

TRUNCATE → Clear ALL data from a table quickly
           "Reset the staging table before next import"
           When auto-increment should reset
           When you don't need row-by-row logging

DROP → Permanently remove a table you no longer need
       "Remove the migration_backup table after verification"
       During schema cleanup
```

**Closing:**
`DELETE` for surgical removal with safety. `TRUNCATE` for fast table reset. `DROP` for permanent removal. In production, I always prefer `DELETE` with a `WHERE` clause inside a transaction — `TRUNCATE` and `DROP` are irreversible and should be used with extreme caution.

---

## SCENARIO-BASED QUESTIONS

---

## Scenario 1: A query is running very slow in production. How would you debug it?

**Summary:**
I follow a systematic process: identify the query (slow log), analyze its plan (EXPLAIN), check for missing indexes, examine the data volume, look for locking issues, and optimize. I never guess — I let EXPLAIN tell me exactly what's wrong.

**My Step-by-Step Debugging Process:**

**Step 1 — Identify the Slow Query:**
```sql
-- Check currently running queries
SHOW FULL PROCESSLIST;
-- Look for queries with high Time values

-- Or check the slow query log
-- /var/log/mysql/slow.log shows:
-- Query_time: 12.345  Lock_time: 0.002  Rows_sent: 20  Rows_examined: 1,500,000
-- SELECT o.*, c.name FROM orders o JOIN customers c ...
```

**Step 2 — Analyze with EXPLAIN ANALYZE (MySQL 8.0+):**
```sql
EXPLAIN ANALYZE
SELECT o.id, o.total_amount, c.name, c.email
FROM orders o
JOIN customers c ON o.customer_id = c.id
WHERE o.status = 'PENDING'
  AND o.created_at > '2026-01-01'
ORDER BY o.created_at DESC
LIMIT 20;

-- Output shows ACTUAL execution time and rows:
-- -> Limit: 20 row(s)  (actual time=8523.45..8523.46 rows=20 loops=1)
--   -> Sort: o.created_at DESC  (actual time=8523.44..8523.44 rows=20 loops=1)
--     -> Nested loop join  (cost=245678 rows=500000)
--       -> Table scan on o  (cost=50000 rows=500000) ← PROBLEM: full table scan
--       -> Single-row index lookup on c using PRIMARY
```

**Step 3 — Identify the Problem:**
```
Common problems visible in EXPLAIN:

| EXPLAIN Output | Problem | Fix |
|---------------|---------|-----|
| type = ALL | Full table scan | Add index |
| key = NULL | No index used | Add or fix index |
| rows = 500000 | Too many rows scanned | Better index / filter |
| Using filesort | Sort not from index | Composite index with ORDER BY column |
| Using temporary | Temp table for GROUP BY | Index covering GROUP BY columns |
| Using where; Using join buffer | Unindexed JOIN | Index on FK column |
```

**Step 4 — Apply the Fix:**
```sql
-- For this query, add a composite index:
CREATE INDEX idx_orders_status_date ON orders (status, created_at DESC);

-- Verify improvement:
EXPLAIN ANALYZE
-- Same query...
-- → Limit: 20  (actual time=0.85..0.92 rows=20 loops=1)
--   → Index range scan using idx_orders_status_date
--     (actual time=0.84..0.89 rows=20 loops=1)

-- Result: 8523ms → 0.92ms (9,264x improvement)
```

**Step 5 — Check for Non-Query Issues:**
```sql
-- Lock contention
SELECT * FROM performance_schema.data_locks;
SHOW ENGINE INNODB STATUS;  -- check for lock waits

-- Table statistics
SHOW TABLE STATUS LIKE 'orders';
-- Check: Rows, Data_length, Index_length
-- If data_length is huge, might need partitioning

-- Buffer pool efficiency
SHOW GLOBAL STATUS LIKE 'Innodb_buffer_pool_read%';
-- If buffer_pool_reads >> buffer_pool_read_requests → not enough RAM for cache

-- Connection issues
SHOW GLOBAL STATUS LIKE 'Threads%';
SHOW GLOBAL STATUS LIKE 'Max_used_connections';
```

**Closing:**
Debugging slow queries is systematic: slow log → EXPLAIN → identify full scans / missing indexes → add composite index → verify. 80% of slow queries are fixed with the right index. The remaining 20% need query rewrites, schema changes, or infrastructure tuning.

---

## Scenario 2: How do you design indexes for a large table with millions of records?

**Summary:**
I design indexes based on actual query patterns, not guesses. I analyze the top 10 most frequent queries, design composite indexes that cover them, follow the leftmost prefix rule, and use covering indexes for critical queries. For millions of records, the wrong index strategy means minutes of downtime per query.

**My Index Design Process:**

**Step 1 — Collect Query Patterns:**
```sql
-- Most frequent queries on the orders table (from application code/logs):

-- Q1: Order list by customer (most frequent — called on every user login)
SELECT id, total, status, created_at FROM orders
WHERE customer_id = ? AND status = ? ORDER BY created_at DESC LIMIT 20;

-- Q2: Admin order search
SELECT * FROM orders WHERE status = 'PENDING' AND created_at > ?;

-- Q3: Revenue report
SELECT DATE(created_at) AS day, SUM(total) FROM orders
WHERE created_at BETWEEN ? AND ? GROUP BY DATE(created_at);

-- Q4: Order detail (by primary key — already indexed)
SELECT * FROM orders WHERE id = ?;
```

**Step 2 — Design Indexes for Each Pattern:**
```sql
-- For Q1: (customer_id, status, created_at DESC) — covers WHERE + ORDER BY
-- Also covers: WHERE customer_id = ? (leftmost prefix)
CREATE INDEX idx_orders_cust_status_date
  ON orders (customer_id, status, created_at DESC);

-- For Q2: (status, created_at) — different leading column than Q1
-- Q1's index can't serve Q2 (Q2 doesn't filter by customer_id)
CREATE INDEX idx_orders_status_date
  ON orders (status, created_at);

-- For Q3: (created_at) — for range scan + GROUP BY
-- Actually, idx_orders_status_date can serve this too (created_at is second column)
-- But only if we add a status filter. For pure date range, we need:
CREATE INDEX idx_orders_created_at
  ON orders (created_at);

-- Make Q1 a COVERING INDEX (includes SELECT columns — no table lookup):
CREATE INDEX idx_orders_cust_status_date_cover
  ON orders (customer_id, status, created_at DESC, id, total_amount);
-- EXPLAIN for Q1 now shows "Using index" — fastest possible
```

**Step 3 — Verify All Queries Use Indexes:**
```sql
-- Run EXPLAIN for each query and verify:
-- ✅ type = ref, range, or index (NOT ALL)
-- ✅ key = your index name (NOT NULL)
-- ✅ rows = reasonable number (NOT millions)
-- ✅ Extra: "Using index" for covering index queries
```

**Index Design Rules for Large Tables:**
```
1. COMPOSITE INDEX COLUMN ORDER:
   Equality columns FIRST → Range columns LAST
   ✅ (status, customer_id, created_at)  — status= AND customer_id= AND created_at>
   ❌ (created_at, status, customer_id)  — range on first column breaks rest

2. COVERING INDEX for top queries:
   Include SELECT columns in the index to avoid table lookup
   → Doubles read speed for critical queries

3. DON'T over-index:
   Each index slows down INSERT/UPDATE/DELETE
   5M rows × 10 indexes = significant write overhead
   Target: 3-5 well-designed indexes, not 15 single-column indexes

4. MONITOR index usage:
   SELECT * FROM sys.schema_unused_indexes;  -- indexes nobody uses
   SELECT * FROM sys.schema_index_statistics; -- usage counts per index
   → Remove unused indexes to speed up writes

5. PARTIAL INDEX (prefix) for long strings:
   CREATE INDEX idx_email ON users (email(50));
   -- Index first 50 chars — sufficient for most lookups, saves space
```

**Closing:**
Index design starts with query analysis, not guessing. Composite indexes ordered by selectivity (equality first, range last), covering indexes for hot queries, and regular cleanup of unused indexes. For a 100M row table, the difference between the right and wrong index is 50ms vs 5 minutes.

---

## Scenario 3: How do you handle database locking issues in high-traffic applications?

**Summary:**
Locking issues in high-traffic apps manifest as slow queries, timeouts, and deadlocks. My approach: use optimistic locking for low-contention scenarios, pessimistic locking with row-level locks for high-contention, keep transactions short, and use `SKIP LOCKED` for queue-like patterns.

**Strategy 1 — Optimistic Locking (Low Contention):**
```java
// JPA @Version — no database lock held. Checks version on UPDATE.
@Entity
public class Product {
    @Id
    private Long id;
    private String name;
    private int stockQuantity;

    @Version
    private Long version;  // Incremented on every update
}

// When two transactions update the same product:
// Tx1: reads product (version=5), updates stock, commits → version becomes 6 ✅
// Tx2: reads product (version=5), updates stock, tries to commit
//   → UPDATE products SET stock=?, version=6 WHERE id=? AND version=5
//   → 0 rows affected (version is now 6, not 5)
//   → OptimisticLockException thrown → retry

@Retryable(retryFor = OptimisticLockException.class, maxAttempts = 3)
@Transactional
public void updateStock(Long productId, int quantity) {
    Product product = productRepository.findById(productId).orElseThrow();
    product.setStockQuantity(product.getStockQuantity() - quantity);
    productRepository.save(product);
    // If another transaction updated this row, retry automatically
}
```

**Strategy 2 — Pessimistic Locking (High Contention):**
```java
// SELECT ... FOR UPDATE — holds an exclusive lock until transaction ends
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Account findByIdWithLock(@Param("id") Long id);
}

// SKIP LOCKED — for job/task queue processing
@Query(value = "SELECT * FROM tasks WHERE status = 'PENDING' " +
               "ORDER BY priority DESC LIMIT 10 FOR UPDATE SKIP LOCKED",
       nativeQuery = true)
List<Task> findPendingTasksForProcessing();
// Multiple workers call this simultaneously — each gets different rows
// No waiting, no deadlocks
```

**Strategy 3 — Reduce Lock Duration:**
```java
// ❌ LONG transaction — holds lock while calling external API
@Transactional
public void processOrder(Long orderId) {
    Order order = orderRepository.findByIdWithLock(orderId); // lock acquired
    PaymentResult result = paymentGateway.charge(order);     // 2-5 seconds holding lock!
    order.setPaymentId(result.getId());
    orderRepository.save(order);                              // lock released on commit
}

// ✅ SHORT transaction — external call OUTSIDE transaction
public void processOrder(Long orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    PaymentResult result = paymentGateway.charge(order);  // no lock held

    updateOrderPayment(orderId, result);  // short, focused transaction
}

@Transactional
public void updateOrderPayment(Long orderId, PaymentResult result) {
    Order order = orderRepository.findByIdWithLock(orderId); // lock for ~5ms
    order.setPaymentId(result.getId());
    order.setStatus(OrderStatus.PAID);
    orderRepository.save(order);  // lock released
}
```

**Strategy 4 — Atomic Updates (No Lock Needed):**
```sql
-- Instead of: read → modify in Java → write back (requires lock)
-- Use: single atomic UPDATE (no application-level lock needed)

-- ❌ Requires locking
SELECT stock FROM products WHERE id = 123 FOR UPDATE;
-- Java: newStock = stock - 1
UPDATE products SET stock = ? WHERE id = 123;

-- ✅ Atomic — no lock needed, MySQL handles concurrency
UPDATE products SET stock = stock - 1 WHERE id = 123 AND stock > 0;
-- Returns affected rows = 1 (success) or 0 (out of stock)
-- Single statement = atomic, no race condition
```

**Closing:**
Optimistic locking for low contention (most reads, rare conflicts). Pessimistic locking for high contention (bank transfers, inventory decrements). Atomic SQL updates when possible (eliminates locking entirely). Always keep transactions short — never hold locks while calling external services.

---

## Scenario 4: What is database sharding and when would you use it?

**Summary:**
Sharding splits a single large database into multiple smaller databases (shards), each holding a subset of data. It's a horizontal scaling strategy used when a single database can't handle the write load, data volume, or query throughput. I've used it for tables exceeding 500M+ rows where vertical scaling (bigger server) was no longer viable.

**How Sharding Works:**

```
BEFORE SHARDING (Single DB):
┌───────────────────────────────────┐
│         Single MySQL Server        │
│    orders table: 2 billion rows    │
│    Writes: 50,000/sec (maxed out) │
│    Storage: 4TB (disk full)        │
│    Queries: increasingly slow      │
└───────────────────────────────────┘

AFTER SHARDING (4 Shards):
┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│   Shard 0    │ │   Shard 1    │ │   Shard 2    │ │   Shard 3    │
│ customer_id  │ │ customer_id  │ │ customer_id  │ │ customer_id  │
│ % 4 == 0     │ │ % 4 == 1     │ │ % 4 == 2     │ │ % 4 == 3     │
│ 500M rows    │ │ 500M rows    │ │ 500M rows    │ │ 500M rows    │
│ 12,500 w/sec │ │ 12,500 w/sec │ │ 12,500 w/sec │ │ 12,500 w/sec │
└──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
   Each shard is a separate MySQL instance (or cluster)
```

**Sharding Strategies:**

```
1. HASH-BASED (most common):
   shard = hash(customer_id) % number_of_shards
   ✅ Even distribution
   ❌ Resharding (adding shards) requires data migration

2. RANGE-BASED:
   Shard 0: customer_id 1-1,000,000
   Shard 1: customer_id 1,000,001-2,000,000
   ✅ Easy to understand, range queries on shard key work
   ❌ Hot spots (newest shard gets all writes)

3. DIRECTORY/LOOKUP-BASED:
   Lookup table: customer_id → shard_id
   ✅ Flexible mapping, easy to move customers between shards
   ❌ Lookup table is a single point of failure
```

**Choosing a Shard Key:**
```
Good shard key:
  ✅ High cardinality (many distinct values) — customer_id, tenant_id
  ✅ Frequently in WHERE clauses — queries hit ONE shard, not all
  ✅ Evenly distributes data and writes
  ✅ Immutable (doesn't change) — avoids cross-shard migration

Bad shard key:
  ❌ Low cardinality — status (3 values = 3 shards, uneven)
  ❌ Monotonically increasing — created_at (newest shard overloaded)
  ❌ Rarely in queries — shard key not in WHERE = query ALL shards
```

**Application-Level Sharding with Spring Boot:**
```java
// Shard router — determines which database to use
@Component
public class ShardRouter {
    private final Map<Integer, DataSource> shardDataSources;
    private final int totalShards;

    public DataSource getShardFor(Long customerId) {
        int shardId = (int) (customerId % totalShards);
        return shardDataSources.get(shardId);
    }
}

// AbstractRoutingDataSource — Spring's built-in shard routing
public class ShardRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return ShardContext.getCurrentShardId();
        // Returns: "shard_0", "shard_1", etc.
    }
}

// Usage in service
@Service
public class OrderService {
    public Order getOrder(Long customerId, Long orderId) {
        ShardContext.setCurrentShard(customerId);  // set shard context
        return orderRepository.findById(orderId);   // routed to correct shard
    }
}
```

**What Sharding Breaks:**
```
❌ Cross-shard JOINs — can't JOIN tables on different servers
   → Denormalize or use application-level joins

❌ Cross-shard transactions — no distributed ACID
   → Use Saga pattern for cross-shard operations

❌ Global unique IDs — auto-increment doesn't work across shards
   → Use Snowflake IDs, UUIDs, or centralized ID generator

❌ Aggregation queries — SUM across all shards requires scatter-gather
   → Use a separate analytics database (CQRS pattern)

❌ Schema changes — must be applied to ALL shards
   → Use migration tools that target all shards (Flyway with multi-tenant support)
```

**Alternatives Before Sharding (Exhaust These First):**
```
1. Read replicas — offload reads to replicas (handles 80% of scaling needs)
2. Caching — Redis for hot data (reduces DB load by 70-90%)
3. Query optimization — proper indexes (often the real problem)
4. Vertical scaling — bigger server (up to 128 cores, 2TB RAM)
5. Table partitioning — split table by date range (stays in one DB)
6. Archive old data — move historical data to cold storage

Only shard when ALL of the above are insufficient.
```

**When Sharding Is Justified:**
```
→ Single table exceeds 500M+ rows and growing
→ Write throughput exceeds single server capacity (100K+ writes/sec)
→ Data volume exceeds single server storage (multi-TB)
→ Multi-tenant SaaS where tenant isolation is required
→ Geographic distribution (EU data stays in EU, US data in US)
```

**Real-World Scenario:**
In a SaaS platform, the `events` table hit 2 billion rows. Queries slowed to 30+ seconds even with indexes. Vertical scaling was already maxed (64 cores, 512GB RAM). We sharded by `tenant_id` into 16 shards — each shard had ~125M rows, queries returned in <100ms. The shard key was perfect because every query already included `tenant_id` in the WHERE clause (multi-tenant isolation). We used Vitess (open-source sharding middleware) to handle routing transparently.

**Closing:**
Sharding is a last resort — it adds massive complexity. Exhaust indexing, caching, read replicas, and partitioning first. When you must shard, choose the shard key based on your query patterns (it must be in every WHERE clause), and accept the trade-offs: no cross-shard joins, no cross-shard transactions, and operational overhead of managing multiple databases.

---

## Quick Reference Summary

| # | Topic | Key Takeaway |
|---|-------|-------------|
| 1 | InnoDB vs MyISAM | InnoDB always — transactions, row locks, crash recovery |
| 2 | Index Types | Composite index + leftmost prefix rule = 80% of optimization |
| 3 | Clustered vs Non-Clustered | PK = clustered (data in index). Secondary = pointer to PK |
| 4 | ACID | Atomicity, Consistency, Isolation, Durability — `@Transactional` |
| 5 | Isolation Levels | Repeatable Read (default) — MVCC for non-blocking reads |
| 6 | WHERE vs HAVING | WHERE = row filter (before GROUP BY). HAVING = group filter (after) |
| 7 | JOINs | INNER = matched only. LEFT = all left + matches. Index FK columns |
| 8 | Deadlocks | Consistent lock order + short transactions + retry logic |
| 9 | Query Optimization | EXPLAIN → find ALL/NULL → add composite index → verify |
| 10 | DELETE/TRUNCATE/DROP | DELETE = safe rollback. TRUNCATE = fast reset. DROP = permanent |
| S1 | Slow Query Debug | slow log → EXPLAIN ANALYZE → index → verify |
| S2 | Index Design | Query patterns → composite (equality first, range last) → covering |
| S3 | Locking Issues | Optimistic for low contention, pessimistic for high, atomic SQL when possible |
| S4 | Sharding | Last resort. Exhaust indexes + cache + replicas first. Shard by tenant/customer ID |
