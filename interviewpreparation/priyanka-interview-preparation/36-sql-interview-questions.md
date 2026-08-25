# 🗄️ SQL — The Complete Interview File

> **Why this file exists.** SQL appeared for the first time at **Mphasis L1** (a `GROUP BY` with a join) and it is now on the checklist for **every** round — Deloitte's rubric has a SQL row, and every Java/Spring JD you've applied to lists MySQL or Postgres. This file takes the two questions you were actually asked and builds outward until nothing in a normal SQL round is new.
>
> 🔗 Companion sections you already have: **[30 — Mphasis L2 §G](./30-mphasis-level2-client-round.md)** (the compact version) · **[26 — Companies §8](./26-companies-asked-questions.md)** (the exact question Mphasis asked) · **[24 — ORM/JPA](./24-orm-jpa-hibernate.md)** (the N+1 and the SQL Hibernate generates).

---

## 📖 Table of contents

| Part | What's in it |
|---|---|
| **[0](#part-0--the-two-questions-you-were-actually-asked)** | 🔴 The two questions you were actually asked — answered to mid-level depth |
| **[1](#part-1--the-schema-used-in-this-whole-file)** | The schema + sample data used everywhere below (memorise these 3 tables) |
| **[2](#part-2--logical-query-execution-order--the-idea-that-explains-half-the-answers)** | 🔴 Logical execution order — the single idea that explains `WHERE` vs `HAVING`, aliases, and half the traps |
| **[3](#part-3--joins-in-full)** | Joins in full — with row-by-row worked output, **plus §3.6 join cardinality: the `1,1,1,1` vs `1,1,1,1` row-count puzzle** |
| **[4](#part-4--count--the-question-behind-the-question)** | `COUNT(*)` vs `COUNT(col)` vs `COUNT(DISTINCT)` — the join+count trap |
| **[5](#part-5--group-by-having-and-aggregates)** | `GROUP BY`, `HAVING`, aggregates and their NULL behaviour |
| **[6](#part-6--the-salary-questions--every-variant)** | 🔴 **The salary questions** — Nth highest, 5 ways; per-department; above-average; top-3 per dept |
| **[7](#part-7--window-functions)** | 🔴 Window functions — `RANK`/`DENSE_RANK`/`ROW_NUMBER`, `LAG`/`LEAD`, running totals, frames |
| **[8](#part-8--subqueries-in--exists--join)** | Subqueries — scalar, correlated, `IN` vs `EXISTS` vs `JOIN`, and the `NOT IN` NULL bomb |
| **[9](#part-9--duplicates-find-them-delete-them-prevent-them)** | Duplicates — find them, delete them, prevent them |
| **[10](#part-10--nulls-and-three-valued-logic)** | 🔴 NULLs and three-valued logic — where most candidates get caught |
| **[11](#part-11--set-operations)** | `UNION` vs `UNION ALL` vs `INTERSECT` vs `EXCEPT` |
| **[12](#part-12--ctes-and-recursive-ctes)** | CTEs and recursive CTEs — the employee hierarchy question |
| **[13](#part-13--self-joins-and-hierarchies)** | Self joins — employee/manager, consecutive rows |
| **[14](#part-14--dates-strings-and-case)** | Dates, strings, `CASE`, and pivoting without `PIVOT` |
| **[15](#part-15--indexes-and-query-tuning)** | 🔴 Indexes and "make this query faster" — the follow-up that always comes |
| **[16](#part-16--transactions-acid-and-isolation-levels)** | Transactions, ACID, isolation levels, locking, deadlock |
| **[17](#part-17--normalization-keys-and-constraints)** | Normalization, keys, constraints |
| **[18](#part-18--ddl-vs-dml-delete-vs-truncate-vs-drop)** | DDL vs DML, `DELETE` vs `TRUNCATE` vs `DROP` |
| **[19](#part-19--views-stored-procedures-functions-triggers)** | Views, stored procedures, functions, triggers |
| **[20](#part-20--sql-from-the-java--spring-side)** | 🔴 SQL from the Java/Spring side — JPQL, N+1, pagination, SQL injection |
| **[21](#part-21--predict-the-output--23-sql-programs)** | 🔴 **Predict the output — 23 SQL programs with real data** |
| **[22](#part-22--rapid-fire-65-questions)** | Rapid-fire — 65 one-line answers |
| **[23](#part-23--drills-answers-hidden)** | 18 drills with hidden answers |
| **[24](#part-24--one-page-cheat-sheet)** | One-page cheat sheet for 20 minutes before the call |

---

# Part 0 — The two questions you were actually asked

## 🔴 Q0.1 — Mphasis L1: *"Total employees and average salary per department"*

```sql
SELECT  d.dept_id,
        d.dept_name,
        COUNT(e.emp_id)                       AS total_employees,
        COALESCE(ROUND(AVG(e.salary), 2), 0)  AS average_salary
FROM    dept d
LEFT JOIN employee e ON e.dept_id = d.dept_id
GROUP BY d.dept_id, d.dept_name
ORDER BY d.dept_name;
```

**The three sentences that make this a mid-level answer.** Do not just write the query — narrate these while you type:

**① `LEFT JOIN`, not `INNER JOIN`.**
> *"With an inner join a department with zero employees disappears from the result entirely. The question says 'per department', and zero is a valid answer — so I drive from `dept` with a left join."*

**② `COUNT(e.emp_id)`, not `COUNT(*)`.**
> *"This is the subtle one. With a `LEFT JOIN` an empty department still produces one row, with all the employee columns NULL. **`COUNT(*)` counts that row and returns 1**, which is wrong. `COUNT(column)` skips NULLs and correctly returns **0**."*

**③ Every non-aggregated column must be in the `GROUP BY`.**
> *"`dept_name` is selected but not aggregated, so it has to be in `GROUP BY`. MySQL 5.6 was lenient; Postgres, SQL Server and MySQL 5.7+ under `ONLY_FULL_GROUP_BY` reject it."*

**And the NULL average.** `AVG` also ignores NULLs, so an empty department returns `NULL`, not `0`. `COALESCE(..., 0)` is the fix — mention it even if they don't ask.

### The four follow-ups they ask next — have all four ready

| They ask | You answer |
|---|---|
| *"Only departments with more than 5 employees"* | `HAVING COUNT(e.emp_id) > 5` — **not `WHERE`**. *"`WHERE` filters rows before grouping; `HAVING` filters groups after aggregation. An aggregate doesn't exist yet at `WHERE` time."* |
| *"Only permanent employees, but still show every department"* | The condition goes **in the `ON`, not the `WHERE`**: `LEFT JOIN employee e ON e.dept_id = d.dept_id AND e.emp_type = 'PERM'`. *"Putting it in `WHERE` turns the left join back into an inner join, because a NULL row fails the predicate."* 🔴 **This is the single most-asked LEFT JOIN follow-up in the industry.** |
| *"Departments with no employees at all"* | `... LEFT JOIN ... WHERE e.emp_id IS NULL` — the anti-join. |
| *"Sort by headcount descending"* | `ORDER BY total_employees DESC` — *"`ORDER BY` runs after `SELECT`, so it can use the alias. `WHERE` and `GROUP BY` can't."* |

---

## 🔴 Q0.2 — The classic: *"Second-highest salary"*

They will ask this in some form. **The answer they want is `DENSE_RANK`, and the sentence they're listening for is "what about ties".**

```sql
-- ✅ The answer to give first
SELECT DISTINCT salary
FROM (
  SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk
  FROM employee
) t
WHERE rnk = 2;
```

> *"I'd use `DENSE_RANK` rather than `ROW_NUMBER` or `RANK`, because if two people share the top salary, `ROW_NUMBER` would call the second of them the 'second highest' — which is wrong, it's the same salary. `DENSE_RANK` gives both of them rank 1 and the genuinely-second salary rank 2."*

Full five-way comparison, including the versions for a MySQL 5.7 box with no window functions, is in **[Part 6](#part-6--the-salary-questions--every-variant)**. Learn at least two: the `DENSE_RANK` one and the `LIMIT 1 OFFSET 1` one.

---

## 🎯 How to answer *any* SQL question — the 4-step script

Interviewers score **method**, not just the final query. Do these four out loud, in order:

1. **Restate the schema and confirm.** *"So `employee` has `emp_id`, `salary`, `dept_id`, and `dept` has `dept_id`, `dept_name` — and `dept_id` can be NULL for an unassigned employee?"* **Asking about NULLs and duplicates before you write scores immediately.**
2. **Say the shape before the syntax.** *"This is an aggregate per group, so it's a join, a `GROUP BY` on department, and a `HAVING` if we filter on the count."*
3. **Write it, narrating the non-obvious choices** — the left join, the `COUNT(column)`, the alias.
4. **Volunteer the edge case.** *"One thing to watch — if a department has no employees, the average comes back NULL rather than 0."* This one sentence is what separates 3/5 from 5/5 on a rubric.

> 🚫 **Never go silent and never say "I'd have to look it up".** Say the shape even if you're unsure of a function name: *"I'd use a window function here — `RANK` or `DENSE_RANK` partitioned by department — let me write it and we can check the exact syntax."*

---

# Part 1 — The schema used in this whole file

Every query below runs against these three tables. **Memorise them** — if the interviewer doesn't give you a schema, invent this one out loud and they will accept it.

```sql
CREATE TABLE dept (
  dept_id    INT PRIMARY KEY,
  dept_name  VARCHAR(50) NOT NULL,
  location   VARCHAR(50)
);

CREATE TABLE employee (
  emp_id      INT PRIMARY KEY,
  emp_name    VARCHAR(50) NOT NULL,
  salary      DECIMAL(10,2),
  hire_date   DATE,
  dept_id     INT,               -- nullable: an unassigned employee
  manager_id  INT,               -- nullable: the CEO has no manager
  CONSTRAINT fk_dept    FOREIGN KEY (dept_id)    REFERENCES dept(dept_id),
  CONSTRAINT fk_manager FOREIGN KEY (manager_id) REFERENCES employee(emp_id)
);

CREATE TABLE orders (
  order_id    INT PRIMARY KEY,
  emp_id      INT,               -- the salesperson
  order_date  DATE,
  amount      DECIMAL(10,2),
  status      VARCHAR(20)
);
```

### The sample data (used by every "predict the output" question in Part 21)

**`dept`**

| dept_id | dept_name | location |
|---|---|---|
| 10 | Engineering | Chennai |
| 20 | Sales | Bangalore |
| 30 | HR | Chennai |
| 40 | Legal | Pune |

**`employee`**

| emp_id | emp_name | salary | hire_date | dept_id | manager_id |
|---|---|---|---|---|---|
| 1 | Asha | 90000 | 2019-01-15 | 10 | *NULL* |
| 2 | Bala | 75000 | 2020-03-01 | 10 | 1 |
| 3 | Chitra | 75000 | 2020-07-20 | 10 | 1 |
| 4 | Dev | 60000 | 2021-02-10 | 20 | 1 |
| 5 | Esha | 50000 | 2022-05-05 | 20 | 4 |
| 6 | Farid | *NULL* | 2023-01-09 | 30 | 1 |
| 7 | Gita | 45000 | 2023-06-30 | *NULL* | 1 |

> 🔴 **Note the three deliberate landmines**, because interview data always has them:
> - **Dept 40 (Legal) has zero employees** → shows up only with a `LEFT JOIN`.
> - **Farid's salary is NULL** → breaks `COUNT`, `AVG` and `NOT IN`.
> - **Gita's `dept_id` is NULL** → she disappears from any inner join to `dept`.
> - Plus **Bala and Chitra tie at 75,000** → this is what separates `RANK` from `DENSE_RANK` from `ROW_NUMBER`.

---

# Part 2 — Logical query execution order — the idea that explains half the answers

🔴 **If you learn one thing from this file, learn this.** SQL is *written* in one order and *executed* in another. Most SQL interview traps are just this table.

```
  ┌─────────────────────────────────────────────────────────┐
  │  WRITTEN ORDER          │   LOGICAL EXECUTION ORDER      │
  ├─────────────────────────┼────────────────────────────────┤
  │  SELECT      (1st)      │   1. FROM  +  JOIN ... ON      │
  │  FROM        (2nd)      │   2. WHERE                     │
  │  JOIN        (3rd)      │   3. GROUP BY                  │
  │  WHERE       (4th)      │   4. HAVING                    │
  │  GROUP BY    (5th)      │   5. SELECT  (aliases born ↓)  │
  │  HAVING      (6th)      │   6. DISTINCT                  │
  │  ORDER BY    (7th)      │   7. ORDER BY                  │
  │  LIMIT       (8th)      │   8. LIMIT / OFFSET            │
  └─────────────────────────────────────────────────────────┘
```

### The five consequences — every one of them is an interview question

| Consequence | Why | Example |
|---|---|---|
| **`WHERE` cannot use an aggregate** | `WHERE` (2) runs before `GROUP BY` (3), so `COUNT(*)` doesn't exist yet | `WHERE COUNT(*) > 5` ❌ → `HAVING COUNT(*) > 5` ✅ |
| **`WHERE` cannot use a `SELECT` alias** | Aliases are created at step 5 | `WHERE total > 5` where `total` is an alias ❌ *(MySQL bends this in `HAVING`/`ORDER BY`, Postgres doesn't in `WHERE` either)* |
| **`ORDER BY` **can** use a `SELECT` alias** | `ORDER BY` (7) runs after `SELECT` (5) | `ORDER BY total_employees DESC` ✅ |
| **A `WHERE` on the right table kills a `LEFT JOIN`** | The join (1) produces NULL rows, then `WHERE` (2) deletes them | Move the condition into `ON` |
| **`LIMIT` runs last** | So `LIMIT 1` after `ORDER BY salary DESC` really is the max | but `DISTINCT` (6) has already collapsed rows by then |

### The one-liner to say

> *"`WHERE` filters rows before grouping, `HAVING` filters groups after aggregation — because logically `FROM` → `WHERE` → `GROUP BY` → `HAVING` → `SELECT` → `ORDER BY`. That order is also why `ORDER BY` can use a column alias and `WHERE` can't."*

---

# Part 3 — Joins in full

## 3.1 The six joins

| Join | Returns | Mental picture |
|---|---|---|
| `INNER JOIN` | Only rows matching on both sides | the overlap |
| `LEFT [OUTER] JOIN` | All left rows + matches; NULLs where no match | all of A |
| `RIGHT [OUTER] JOIN` | All right rows + matches | all of B |
| `FULL [OUTER] JOIN` | Everything from both sides | A ∪ B — **not in MySQL**, emulate with `LEFT UNION RIGHT` |
| `CROSS JOIN` | Cartesian product — every row × every row | 7 × 4 = 28 rows |
| `SELF JOIN` | A table joined to itself (not a keyword — just an alias) | employee → manager |

```sql
-- FULL OUTER JOIN emulated in MySQL
SELECT d.dept_name, e.emp_name FROM dept d LEFT  JOIN employee e ON e.dept_id = d.dept_id
UNION
SELECT d.dept_name, e.emp_name FROM dept d RIGHT JOIN employee e ON e.dept_id = d.dept_id;
```

## 3.2 Worked output on the real data — this is what they want you to be able to predict

```sql
SELECT d.dept_name, e.emp_name FROM dept d INNER JOIN employee e ON e.dept_id = d.dept_id;
```
→ **6 rows.** Engineering×3, Sales×2, HR×1. **Legal is gone** (no employees) and **Gita is gone** (NULL dept_id).

```sql
SELECT d.dept_name, e.emp_name FROM dept d LEFT JOIN employee e ON e.dept_id = d.dept_id;
```
→ **7 rows.** The 6 above **plus `('Legal', NULL)`**. Gita is *still* gone — she has no dept, and we drove from `dept`.

```sql
SELECT d.dept_name, e.emp_name FROM dept d RIGHT JOIN employee e ON e.dept_id = d.dept_id;
```
→ **7 rows.** The 6 above **plus `(NULL, 'Gita')`**. Legal is gone.

```sql
SELECT COUNT(*) FROM dept CROSS JOIN employee;
```
→ **28**. `4 × 7`.

> 🔴 **The trap question: "Does a `LEFT JOIN` always return at least as many rows as the left table?"**
> **Yes — at least.** It can return *more*: if one left row matches three right rows, you get three output rows. *"A left join guarantees every left row appears at least once, not exactly once."* That row-multiplication is exactly why `SUM` after a join can double-count.

## 3.3 🔴 The `ON` vs `WHERE` question — asked constantly

```sql
-- ❌ Silently becomes an INNER JOIN
SELECT d.dept_name, e.emp_name
FROM dept d LEFT JOIN employee e ON e.dept_id = d.dept_id
WHERE e.salary > 50000;

-- ✅ Keeps every department
SELECT d.dept_name, e.emp_name
FROM dept d LEFT JOIN employee e ON e.dept_id = d.dept_id AND e.salary > 50000;
```

> *"In an outer join, a predicate in `ON` decides **which rows match**; a predicate in `WHERE` filters **the result after the join**. Because unmatched left rows carry NULLs, and `NULL > 50000` is unknown, a `WHERE` on the right-hand table deletes exactly the rows the left join was there to preserve. For an **inner** join it makes no difference — `ON` and `WHERE` are equivalent."*

## 3.4 The anti-join — "find rows in A with no match in B"

Three ways, and you should know which is fastest:

```sql
-- ① LEFT JOIN ... IS NULL   ✅ portable, usually fast
SELECT d.* FROM dept d
LEFT JOIN employee e ON e.dept_id = d.dept_id
WHERE e.emp_id IS NULL;

-- ② NOT EXISTS              ✅ safest — NULL-proof, usually the optimiser's favourite
SELECT d.* FROM dept d
WHERE NOT EXISTS (SELECT 1 FROM employee e WHERE e.dept_id = d.dept_id);

-- ③ NOT IN                  ⚠️ BREAKS if the subquery can return NULL — see Part 10
SELECT d.* FROM dept d
WHERE d.dept_id NOT IN (SELECT dept_id FROM employee);   -- returns ZERO rows here!
```

> 🔴 Query ③ returns **nothing at all** on our data, because Gita's `dept_id` is NULL. **This is the most popular SQL gotcha in interviews** — full explanation in [Part 10](#part-10--nulls-and-three-valued-logic).

## 3.5 Join algorithms — the senior-sounding follow-up

If they ask *"how does the database actually do a join?"*:

| Algorithm | When the optimiser picks it |
|---|---|
| **Nested loop** | One side is small, or there's an index on the join column of the inner table. O(n×m) without an index. |
| **Hash join** | Big unsorted sets, equality join. Builds a hash table on the smaller side. (MySQL 8.0.18+, Postgres always.) |
| **Merge join** | Both inputs already sorted on the join key — e.g. both sides have indexes. |

> *"You don't choose it, the optimiser does — but you influence it, mainly by indexing the join columns and keeping statistics fresh. `EXPLAIN` tells you which one it picked."*

---

## 3.6 🔴 Join cardinality — *"Table A has 1,1,1,1 and Table B has 1,1,1,1 — how many rows?"*

**This is the single most common SQL puzzle in Indian service-company interviews.** They give you two tiny tables with duplicate values and ask for the row count of each join. It looks like a trick; it's actually just multiplication — but you have to know the rule cold, because they'll fire five counts at you in ten seconds.

### 🔴 The headline question

```
Table A          Table B
-------          -------
  id               id
  --               --
   1                1
   1                1
   1                1
   1                1
```

```sql
SELECT COUNT(*) FROM A INNER JOIN B ON A.id = B.id;   -- ?
SELECT COUNT(*) FROM A LEFT  JOIN B ON A.id = B.id;   -- ?
SELECT COUNT(*) FROM A RIGHT JOIN B ON A.id = B.id;   -- ?
SELECT COUNT(*) FROM A FULL  JOIN B ON A.id = B.id;   -- ?
SELECT COUNT(*) FROM A CROSS JOIN B;                  -- ?
```

### ✅ The answer: **16 — every single one of them.**

| Join | Rows | Why |
|---|---|---|
| `INNER` | **16** | Every one of A's 4 rows matches every one of B's 4 rows → 4 × 4 |
| `LEFT` | **16** | A left join adds rows only for **unmatched** left rows. There are none — so it's identical to the inner join |
| `RIGHT` | **16** | Same reasoning from B's side |
| `FULL OUTER` | **16** | Nothing is unmatched on either side |
| `CROSS` | **16** | 4 × 4 — and here it *coincidentally* equals the others because every row matches |

> 🗣️ **Say this, don't just say "16":**
> *"Sixteen for all of them. A join on equality pairs **every** matching left row with **every** matching right row, so with four 1s on each side that's four times four. And the outer joins only **add** rows for values that have no partner on the other side — here nothing is unmatched, so left, right and full all collapse to the same sixteen rows as the inner join."*

🔴 **The follow-up is guaranteed: "So when would LEFT and INNER differ?"** → *"The moment either side has a value the other doesn't. The left join preserves those rows with NULLs; the inner join drops them."* Have Case ③ below ready as your example.

---

### 📐 The formula — compute any of these in 10 seconds

**Group both sides by the join key, then:**

```
INNER  =  Σ  ( countA(v) × countB(v) )        for every value v present in BOTH
LEFT   =  INNER + (number of A rows whose key is not in B)
RIGHT  =  INNER + (number of B rows whose key is not in A)
FULL   =  INNER + unmatched-A rows + unmatched-B rows
CROSS  =  |A| × |B|                            (no ON clause, so nothing to match)
```

> ⚠️ **NULL keys always count as "unmatched"** — `NULL = NULL` is UNKNOWN, so a NULL never joins to anything, **not even to another NULL**. It contributes 0 to the inner join and 1 to its own side's outer join.

---

### 🧮 Six worked cases — cover the answers and do them

**Case ① — the headline: A = `1,1,1,1` · B = `1,1,1,1`**

| | INNER | LEFT | RIGHT | FULL | CROSS |
|---|---|---|---|---|---|
| | **16** | **16** | **16** | **16** | **16** |

`4×4 = 16`; nothing unmatched anywhere.

---

**Case ② — unequal duplicates: A = `1,1,1,1` · B = `1,1,1`**

| | INNER | LEFT | RIGHT | FULL | CROSS |
|---|---|---|---|---|---|
| | **12** | **12** | **12** | **12** | **12** |

`4×3 = 12`. Still all identical — every row on both sides has a partner. **Note: LEFT returns 12, not 4.** *"A left join guarantees each left row appears **at least** once, not exactly once."*

---

**Case ③ — 🔴 the one that actually separates the joins: A = `1,1,2,2` · B = `1,1,3`**

Group first: A has `1×2, 2×2`. B has `1×2, 3×1`.

| | Count | Working |
|---|---|---|
| `INNER` | **4** | only value 1 is in both: `2 × 2 = 4` |
| `LEFT` | **6** | 4 + the two unmatched A rows (`2,2`) → each gets one row with NULL B columns |
| `RIGHT` | **5** | 4 + the one unmatched B row (`3`) |
| `FULL` | **7** | 4 + 2 + 1 |
| `CROSS` | **12** | 4 × 3 — ignores the values entirely |

**This is the case to reach for when they ask you to explain the difference.**

---

**Case ④ — 🔴 with NULLs: A = `1, 1, NULL` · B = `1, NULL`**

| | Count | Working |
|---|---|---|
| `INNER` | **2** | only value 1 matches: `2 × 1 = 2`. **The two NULLs do not match each other** |
| `LEFT` | **3** | 2 + A's NULL row (unmatched) |
| `RIGHT` | **3** | 2 + B's NULL row (unmatched) |
| `FULL` | **4** | 2 + 1 + 1 |
| `CROSS` | **6** | 3 × 2 — a cross join has no `ON`, so NULLs are irrelevant |

> *"The NULL on each side joins to nothing — not even to the NULL on the other side — because `NULL = NULL` is UNKNOWN, not true. So each NULL row survives only in the outer join on its own side."*
> 💡 In MySQL you can force NULLs to match with the null-safe operator: `ON A.id <=> B.id` → then the inner join returns **3**. In Postgres: `ON A.id IS NOT DISTINCT FROM B.id`. Mention it only if they push.

---

**Case ⑤ — 🔴 the empty-table trap: A = `1, 2, 3` · B = *(no rows)***

| | Count |
|---|---|
| `INNER` | **0** |
| `LEFT` | **3** — every A row, with all B columns NULL |
| `RIGHT` | **0** |
| `FULL` | **3** |
| `CROSS` | **0** ⚠️ — `3 × 0 = 0`, **not 3** |

> **The cross-join-with-an-empty-table answer catches a lot of people.** Anything times zero is zero.

---

**Case ⑥ — duplicates on both sides, mixed: A = `1,1,2` · B = `1,1,2,2`**

`INNER = (2×2) + (1×2) = 4 + 2 = ` **6**. Everything matches on both sides, so `LEFT = RIGHT = FULL = ` **6**, and `CROSS = 3 × 4 = ` **12**.

---

### 🔴 3.6.1 The killer follow-up: `COUNT(*)` vs `COUNT(b.id)` on these joins

Go back to **Case ③** (`A = 1,1,2,2` · `B = 1,1,3`) with a `LEFT JOIN`:

```sql
SELECT COUNT(*)      FROM A LEFT JOIN B ON A.id = B.id;   -- 6
SELECT COUNT(b.id)   FROM A LEFT JOIN B ON A.id = B.id;   -- 4  ← the NULL placeholder rows aren't counted
SELECT COUNT(a.id)   FROM A LEFT JOIN B ON A.id = B.id;   -- 6
SELECT COUNT(DISTINCT a.id) FROM A LEFT JOIN B ON A.id = B.id;  -- 2  (values 1 and 2)
```

**This is the same trap as the department-headcount question in [Part 0](#part-0--the-two-questions-you-were-actually-asked)** — `COUNT(*)` counts the placeholder row that a left join manufactures for an unmatched left row; `COUNT(right_column)` doesn't. If you can connect these two questions out loud, you've shown you understand the concept rather than memorised a number.

---

### 🔴 3.6.2 Why this matters in real code — the `SUM` double-count

This puzzle is not academic. It is the number-one cause of wrong numbers in reports:

```sql
-- ❌ Asha's salary is counted once PER ORDER
SELECT e.emp_name, SUM(e.salary) AS total_salary, COUNT(o.order_id) AS orders
FROM   employee e
JOIN   orders o ON o.emp_id = e.emp_id
GROUP BY e.emp_id, e.emp_name;
```

If Asha has 3 orders, the join produces 3 rows for her, so `SUM(e.salary)` returns **3 × 90000 = 270000**. Her salary didn't triple — the join multiplied her row.

**The fixes:**
```sql
-- ① Aggregate the many-side FIRST, then join to the one-side
SELECT e.emp_name, e.salary, COALESCE(o.order_count, 0) AS orders
FROM   employee e
LEFT JOIN (SELECT emp_id, COUNT(*) AS order_count FROM orders GROUP BY emp_id) o
       ON o.emp_id = e.emp_id;

-- ② Or use a correlated scalar subquery per row
SELECT e.emp_name, e.salary,
       (SELECT COUNT(*) FROM orders o WHERE o.emp_id = e.emp_id) AS orders
FROM   employee e;

-- ③ If you must join first, de-duplicate the one-side aggregate
SELECT e.emp_name, MAX(e.salary) AS salary, COUNT(o.order_id) AS orders
FROM   employee e LEFT JOIN orders o ON o.emp_id = e.emp_id
GROUP BY e.emp_id, e.emp_name;
```

> 🗣️ *"The moment I aggregate across two joins to different child tables — say orders and payments — the counts multiply against each other. The safe pattern is to aggregate each child separately in a subquery and then join those single-row-per-parent results together."* **Saying this unprompted reads as production experience, because that's exactly where the bug shows up.**

---

### 3.6.3 The related counting questions they pair with it

**"Three tables — A has 2 rows of `1`, B has 3 rows of `1`, C has 2 rows of `1`. `A JOIN B JOIN C` on the id?"**
→ **12.** `2 × 3 × 2`. *"Joins compose — the multiplication just keeps going, which is why an unfiltered three-way join on a low-cardinality column explodes."*

**"A table has 4 rows all in the same department. `SELECT COUNT(*) FROM employee e1 JOIN employee e2 ON e1.dept_id = e2.dept_id`?"**
→ **16.** Same 4 × 4 — a self join is not special.
→ **With `AND e1.emp_id < e2.emp_id`: 6** — that's `C(4,2)`, each unordered pair once, no self-pairs. *(This is the "pairs of employees in the same department" question from [Part 13](#part-13--self-joins-and-hierarchies).)*
→ **With `AND e1.emp_id <> e2.emp_id`: 12** — each pair twice, in both orders.

**"Does `A LEFT JOIN B` ever return fewer rows than A?"**
→ **Never.** At least `|A|`, possibly many more. *"That's the guarantee — every left row appears at least once."*

**"Is `A LEFT JOIN B` the same as `B RIGHT JOIN A`?"**
→ **Yes, identical results** (column order aside). *"I write left joins exclusively — reading a query is easier when the driving table is always the first one. Right joins mostly exist for symmetry."*

**"`SELECT DISTINCT` on Case ① — how many?"**
→ **1 row.** All 16 output rows are `(1, 1)`. *"Which is exactly why `DISTINCT` after a join is a smell — it hides the fan-out instead of preventing it."*

---

### ⚡ 3.6.4 The 20-second recipe to say out loud

> *"Count how many of each value are on each side. For the values in both, multiply and add up — that's the inner join. Then a left join adds one row for each left row whose value isn't on the right, a right join does the mirror image, and a full outer adds both. A cross join ignores the values completely and is just left count times right count. And NULL never matches anything, including another NULL, so a NULL row is always 'unmatched'."*

---

# Part 4 — `COUNT` — the question behind the question

🔴 **This is the highest-yield 60 seconds in the whole file**, because "join + count" is the shape of the question you were actually asked.

| Expression | Counts | On our data |
|---|---|---|
| `COUNT(*)` | **Rows**, NULLs included | `SELECT COUNT(*) FROM employee` → **7** |
| `COUNT(col)` | Rows where `col` **IS NOT NULL** | `COUNT(salary)` → **6** (Farid's is NULL) |
| `COUNT(DISTINCT col)` | Distinct non-NULL values | `COUNT(DISTINCT salary)` → **5** (75000 counted once, NULL ignored) |
| `COUNT(1)` | Identical to `COUNT(*)` | **7** — *"`COUNT(1)` vs `COUNT(*)` is a myth; every modern optimiser treats them the same. I write `COUNT(*)`."* |

```sql
-- The complete demonstration — run this mentally, it's a classic whiteboard question
SELECT COUNT(*)                AS rows_total,       -- 7
       COUNT(salary)           AS with_salary,      -- 6
       COUNT(DISTINCT salary)  AS distinct_salary,  -- 5
       COUNT(dept_id)          AS with_dept,        -- 6
       SUM(salary)             AS total_salary,     -- 395000
       AVG(salary)             AS avg_salary        -- 65833.33  ← 395000/6, NOT /7
FROM employee;
```

> 🔴 **The killer follow-up: "Why is `AVG(salary)` not `SUM(salary)/COUNT(*)`?"**
> *"Because `AVG` ignores NULLs — it divides by `COUNT(salary)`, which is 6, not by `COUNT(*)` which is 7. If you want NULL treated as zero you have to say so: `SUM(salary)/COUNT(*)` or `AVG(COALESCE(salary,0))`."*

### The `LEFT JOIN` + `COUNT(*)` trap, drawn out

```sql
SELECT d.dept_name, COUNT(*) AS wrong, COUNT(e.emp_id) AS correct
FROM dept d LEFT JOIN employee e ON e.dept_id = d.dept_id
GROUP BY d.dept_name;
```

| dept_name | wrong (`COUNT(*)`) | correct (`COUNT(e.emp_id)`) |
|---|---|---|
| Engineering | 3 | 3 |
| Sales | 2 | 2 |
| HR | 1 | 1 |
| **Legal** | **1** ❌ | **0** ✅ |

> *"Legal has no employees, but the left join still emits one row for it with every employee column NULL. `COUNT(*)` counts rows, so it says 1. `COUNT(e.emp_id)` counts non-NULL values, so it correctly says 0."*

---

# Part 5 — `GROUP BY`, `HAVING` and aggregates

## 5.1 The rule

> **Every column in the `SELECT` list must either be inside an aggregate function, or listed in the `GROUP BY`.**

```sql
-- ❌ Rejected by Postgres / SQL Server / MySQL with ONLY_FULL_GROUP_BY (the default since 5.7)
SELECT dept_id, emp_name, COUNT(*) FROM employee GROUP BY dept_id;
--              ^^^^^^^^ which employee's name? The engine can't know.
```

> *"Old MySQL would silently pick an arbitrary row's `emp_name`, which is a genuine source of production bugs. Since 5.7 `ONLY_FULL_GROUP_BY` is on by default and it errors, matching the standard."*

**The exception worth knowing:** Postgres allows selecting columns that are **functionally dependent on a grouped primary key** — `GROUP BY d.dept_id` lets you select `d.dept_name` because `dept_id` is the PK. Grouping by both is still the safe habit.

## 5.2 `WHERE` vs `HAVING` — the table to recite

| | `WHERE` | `HAVING` |
|---|---|---|
| Runs | **Before** grouping | **After** grouping |
| Operates on | Individual rows | Groups |
| Aggregates allowed | ❌ No | ✅ Yes |
| Can use `SELECT` alias | ❌ No | ⚠️ MySQL yes, standard/Postgres no |
| Uses indexes | ✅ Yes | ❌ No (data is already aggregated) |
| Performance | **Filter here when you can** | Only for aggregate conditions |

```sql
-- Both, correctly used: only 2020+ hires, only departments where they average > 60k
SELECT d.dept_name, AVG(e.salary) AS avg_sal
FROM   dept d JOIN employee e ON e.dept_id = d.dept_id
WHERE  e.hire_date >= '2020-01-01'      -- row filter, before grouping, index-friendly
GROUP BY d.dept_id, d.dept_name
HAVING AVG(e.salary) > 60000            -- group filter, after aggregation
ORDER BY avg_sal DESC;
```

> 🔴 **Performance point worth making unprompted:** *"I push every filter I can into `WHERE` rather than `HAVING`, because `WHERE` reduces the rows before they're aggregated and can use an index. `HAVING` only makes sense for conditions on the aggregate itself."*

## 5.3 Aggregate functions and their NULL behaviour

| Function | NULLs | Empty input returns | Note |
|---|---|---|---|
| `COUNT(*)` | counted | **0** | The only aggregate that never returns NULL |
| `COUNT(col)` | skipped | 0 | |
| `SUM` | skipped | **NULL** ⚠️ | Not 0! Wrap in `COALESCE(SUM(x),0)` |
| `AVG` | skipped | NULL | Divides by the non-NULL count |
| `MIN` / `MAX` | skipped | NULL | Work on strings and dates too |
| `GROUP_CONCAT` (MySQL) / `STRING_AGG` (Postgres) | skipped | NULL | `GROUP_CONCAT(emp_name ORDER BY salary DESC SEPARATOR ', ')` |

## 5.4 Bonus: `GROUP BY` with `ROLLUP` — subtotals in one query

```sql
SELECT d.dept_name, COUNT(*) AS headcount
FROM dept d JOIN employee e ON e.dept_id = d.dept_id
GROUP BY d.dept_name WITH ROLLUP;   -- MySQL; Postgres: GROUP BY ROLLUP(d.dept_name)
```
Adds a final row with `dept_name = NULL` holding the **grand total**. Mention it if they ask "how would you add a total row" — most candidates reach for a `UNION`.

---

# Part 6 — The salary questions — every variant

> 🔴 **The single most-asked family of SQL interview questions.** Learn the shapes, not the memorised text.

## 6.1 Highest salary — the warm-up

```sql
SELECT MAX(salary) FROM employee;                                  -- 90000

-- "…and who earns it?"  MAX alone can't tell you — you need one of these:
SELECT * FROM employee WHERE salary = (SELECT MAX(salary) FROM employee);  -- handles ties ✅
SELECT * FROM employee ORDER BY salary DESC LIMIT 1;                       -- drops ties ⚠️
```

> *"`ORDER BY … LIMIT 1` is fine unless two people tie for the top — then it arbitrarily returns one of them. The subquery version returns both, which is usually what's meant."*

## 6.2 🔴 Nth-highest salary — five ways, with the trade-offs

Take **N = 2**. On our data the distinct salaries descending are `90000, 75000, 60000, 50000, 45000` → **the answer is 75000**.

```sql
-- ① DENSE_RANK  ✅ the answer to lead with (MySQL 8+, Postgres, SQL Server)
SELECT DISTINCT salary FROM (
  SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk FROM employee
) t WHERE rnk = 2;

-- ② LIMIT/OFFSET on distinct salaries  ✅ works on MySQL 5.7, no window functions needed
SELECT DISTINCT salary FROM employee ORDER BY salary DESC LIMIT 1 OFFSET 1;
--   Postgres/standard:  ... OFFSET 1 FETCH NEXT 1 ROWS ONLY;

-- ③ Correlated subquery — "count how many distinct salaries are above me"  ✅ portable, slow
SELECT DISTINCT e.salary FROM employee e
WHERE 1 = (SELECT COUNT(DISTINCT e2.salary) FROM employee e2 WHERE e2.salary > e.salary);
--    generalise: N-1 = (…)

-- ④ MAX of the ones below the MAX  ✅ neat for N=2 only
SELECT MAX(salary) FROM employee
WHERE salary < (SELECT MAX(salary) FROM employee);

-- ⑤ NOT IN / correlated NOT EXISTS — mention but don't lead with it
SELECT MAX(salary) FROM employee
WHERE salary NOT IN (SELECT MAX(salary) FROM employee);
```

### 🔴 Which do you say when?

| Situation | Use |
|---|---|
| Modern DB, they want the *best* answer | ① `DENSE_RANK` |
| They say *"without window functions"* or *"MySQL 5.7"* | ② `LIMIT 1 OFFSET 1` **on `DISTINCT salary`** |
| They say *"without `LIMIT`"* (SQL Server / Oracle flavour) | ③ correlated `COUNT(DISTINCT)` |
| They want it parameterised for any N | ① with `WHERE rnk = :n` |

> ⚠️ **The trap in ②:** `SELECT salary FROM employee ORDER BY salary DESC LIMIT 1 OFFSET 1` **without `DISTINCT`** returns 75000 here by luck — but if Asha and a second person both earned 90000, it would return **90000** and be wrong. **Always say `DISTINCT` in the `LIMIT/OFFSET` version and explain why.**

> ⚠️ **The other trap:** if there is no Nth salary (N=99), ①②③ return **no rows**, while ④⑤ return a single row containing **NULL**. If they ask "what if there is no second-highest?", that's the answer — and `LeetCode 176` specifically wants NULL, which is why the wrapped-subquery form `SELECT (SELECT DISTINCT salary … LIMIT 1 OFFSET 1) AS second` is used there: a scalar subquery with no rows evaluates to NULL.

## 6.3 Highest-paid employee **per department** — the natural escalation

```sql
-- ✅ Window function — handles ties, one pass
SELECT dept_name, emp_name, salary FROM (
  SELECT d.dept_name, e.emp_name, e.salary,
         RANK() OVER (PARTITION BY e.dept_id ORDER BY e.salary DESC) AS rnk
  FROM employee e JOIN dept d ON d.dept_id = e.dept_id
) t
WHERE rnk = 1;
```
→ Engineering **Asha 90000**, Sales **Dev 60000**, HR **Farid** *(the only row in its partition, so it ranks 1 even with a NULL salary)*. **Gita is absent** — her `dept_id` is NULL, so the inner join drops her.

> ⚠️ **Where do NULLs sort?** It differs by engine and it's a legitimate follow-up: **MySQL** treats NULL as lowest (first in `ASC`, last in `DESC`); **Postgres/Oracle** treat it as highest (last in `ASC`, first in `DESC`) and let you say `ORDER BY salary DESC NULLS LAST`. *"If NULLs matter I make it explicit rather than relying on the default."*

```sql
-- ✅ The pre-window-function version — correlated subquery
SELECT e.emp_name, e.salary, e.dept_id
FROM   employee e
WHERE  e.salary = (SELECT MAX(e2.salary) FROM employee e2 WHERE e2.dept_id = e.dept_id);

-- ✅ Or the join-to-derived-table version (usually the fastest of the three)
SELECT e.emp_name, e.salary, e.dept_id
FROM   employee e
JOIN  (SELECT dept_id, MAX(salary) AS max_sal FROM employee GROUP BY dept_id) m
      ON m.dept_id = e.dept_id AND m.max_sal = e.salary;
```

> 🔴 **`RANK` vs `ROW_NUMBER` here matters.** `ROW_NUMBER() = 1` returns **exactly one** employee per department even when two tie for the top. `RANK() = 1` returns **both**. Say which you chose and why: *"I used `RANK` so tied top earners both appear; if the requirement is exactly one row per department I'd switch to `ROW_NUMBER` with a deterministic tiebreak like `ORDER BY salary DESC, emp_id`."*

## 6.4 Top **3** per department

```sql
SELECT dept_name, emp_name, salary FROM (
  SELECT d.dept_name, e.emp_name, e.salary,
         DENSE_RANK() OVER (PARTITION BY e.dept_id ORDER BY e.salary DESC) AS rnk
  FROM employee e JOIN dept d ON d.dept_id = e.dept_id
) t
WHERE rnk <= 3
ORDER BY dept_name, salary DESC;
```
> *"`DENSE_RANK <= 3` means 'the top three salary levels', which can be more than three people if there are ties. `ROW_NUMBER <= 3` means 'exactly three rows'. I'd ask which they want."* **Asking that question is the answer they're scoring.**

## 6.5 Employees earning **more than their department's average**

```sql
-- ✅ Window function — single scan
SELECT emp_name, salary, dept_id FROM (
  SELECT e.*, AVG(salary) OVER (PARTITION BY dept_id) AS dept_avg FROM employee e
) t
WHERE salary > dept_avg;

-- ✅ Correlated subquery — the classic textbook answer
SELECT e.emp_name, e.salary
FROM   employee e
WHERE  e.salary > (SELECT AVG(e2.salary) FROM employee e2 WHERE e2.dept_id = e.dept_id);
```
→ Engineering avg = 80000 → **Asha**. Sales avg = 55000 → **Dev**.

> *"The correlated version re-runs the inner query once per row — fine for thousands of rows, poor for millions. The window-function version computes each department's average once and is what I'd ship."*

## 6.6 Department-level salary questions

```sql
-- Department with the highest total salary bill
SELECT d.dept_name, SUM(e.salary) AS total
FROM dept d JOIN employee e ON e.dept_id = d.dept_id
GROUP BY d.dept_id, d.dept_name
ORDER BY total DESC
LIMIT 1;

-- Departments where the average beats the company average
SELECT dept_id, AVG(salary) AS dept_avg
FROM employee
GROUP BY dept_id
HAVING AVG(salary) > (SELECT AVG(salary) FROM employee);

-- Salary bands — a CASE + GROUP BY question they like
SELECT CASE WHEN salary >= 80000 THEN 'A: 80k+'
            WHEN salary >= 60000 THEN 'B: 60-80k'
            WHEN salary IS NULL  THEN 'D: unknown'
            ELSE                      'C: under 60k' END AS band,
       COUNT(*) AS headcount
FROM employee
GROUP BY band              -- MySQL allows the alias; Postgres: repeat the CASE or use GROUP BY 1
ORDER BY band;
```

## 6.7 The "median salary" question — the hard one they sometimes end on

```sql
-- Postgres / Oracle: built in
SELECT PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY salary) FROM employee;

-- MySQL 8 — the portable window-function trick (works for even and odd counts)
SELECT AVG(salary) AS median FROM (
  SELECT salary,
         ROW_NUMBER() OVER (ORDER BY salary)            AS rn,
         COUNT(*)     OVER ()                           AS cnt
  FROM employee WHERE salary IS NOT NULL
) t
WHERE rn IN (FLOOR((cnt+1)/2), CEIL((cnt+1)/2));
```
> *"There's no `MEDIAN()` in MySQL. The trick is to number the rows in order, then average the middle one — or the middle two when the count is even, which `FLOOR`/`CEIL` of `(n+1)/2` gives you for free."*

---

# Part 7 — Window functions

> **The one-sentence definition to memorise:** *"A window function computes a value across a set of rows **related to the current row**, but unlike `GROUP BY` it **doesn't collapse the rows** — every input row still comes out, with the aggregate attached."*

That contrast — *aggregate collapses, window preserves* — is the answer to "what's the difference between `GROUP BY` and a window function?"

## 7.1 Anatomy

```sql
FUNCTION(...) OVER (
    PARTITION BY dept_id          -- optional: restart the calculation per group
    ORDER BY     salary DESC      -- optional: order within the partition
    ROWS BETWEEN ... AND ...      -- optional: the frame (which rows are in the window)
)
```

- **No `PARTITION BY`** → the whole result set is one window.
- **No `ORDER BY`** → the window is the entire partition (used for `AVG() OVER (PARTITION BY …)`).
- **`ORDER BY` with no frame** → the default frame is `RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW` — which is why `SUM(...) OVER (ORDER BY x)` gives a **running total**, not the grand total. 🔴 *That default is itself an interview question.*

## 7.2 🔴 `ROW_NUMBER` vs `RANK` vs `DENSE_RANK` — the table to have burned in

```sql
SELECT emp_name, salary,
       ROW_NUMBER() OVER (ORDER BY salary DESC) AS rn,
       RANK()       OVER (ORDER BY salary DESC) AS rnk,
       DENSE_RANK() OVER (ORDER BY salary DESC) AS dense,
       NTILE(2)     OVER (ORDER BY salary DESC) AS half
FROM employee WHERE salary IS NOT NULL;
```

| emp_name | salary | `ROW_NUMBER` | `RANK` | `DENSE_RANK` | `NTILE(2)` |
|---|---|---|---|---|---|
| Asha | 90000 | 1 | 1 | 1 | 1 |
| Bala | 75000 | 2 | **2** | **2** | 1 |
| Chitra | 75000 | 3 | **2** | **2** | 1 |
| Dev | 60000 | 4 | **4** ← gap | **3** ← no gap | 2 |
| Esha | 50000 | 5 | 5 | 4 | 2 |
| Gita | 45000 | 6 | 6 | 5 | 2 |

**The one-liner:**
> *"`ROW_NUMBER` is always unique and arbitrary between ties. `RANK` gives ties the same number and then **skips** — 1,2,2,4. `DENSE_RANK` gives ties the same number and **doesn't skip** — 1,2,2,3. For 'Nth highest salary' you want `DENSE_RANK`, because you're ranking salary *levels*, not people."*

## 7.3 The functions worth knowing, with a use for each

| Function | Does | Real use |
|---|---|---|
| `ROW_NUMBER()` | 1,2,3… | Deduplication (keep `rn = 1`), stable pagination |
| `RANK()` / `DENSE_RANK()` | ranking with ties | Nth highest, top-N per group |
| `NTILE(n)` | split into n buckets | Salary quartiles, percentile bands |
| `LAG(col, 1)` / `LEAD(col, 1)` | previous / next row's value | Month-over-month change, gaps between dates |
| `SUM/AVG/COUNT/MIN/MAX … OVER` | aggregate without collapsing | Running total, % of department total |
| `FIRST_VALUE` / `LAST_VALUE` | first/last in the window | "compare each employee to the top earner" |
| `NTH_VALUE(col, 2)` | the nth row of the window | second-highest, inline |

## 7.4 The four window queries they actually ask

```sql
-- ① Running total of orders by date
SELECT order_date, amount,
       SUM(amount) OVER (ORDER BY order_date
                         ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_total
FROM orders;

-- ② Each employee's salary as a % of their department's total
SELECT emp_name, salary, dept_id,
       ROUND(100.0 * salary / SUM(salary) OVER (PARTITION BY dept_id), 1) AS pct_of_dept
FROM employee;

-- ③ Month-over-month change (LAG)
SELECT month, revenue,
       LAG(revenue) OVER (ORDER BY month)                          AS prev_month,
       revenue - LAG(revenue) OVER (ORDER BY month)                AS change,
       ROUND(100.0 * (revenue - LAG(revenue) OVER (ORDER BY month))
                   / LAG(revenue) OVER (ORDER BY month), 2)        AS pct_change
FROM monthly_revenue;

-- ④ Deduplicate: keep the newest row per email
DELETE FROM users WHERE id IN (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY email ORDER BY created_at DESC) AS rn
    FROM users
  ) t WHERE rn > 1
);
```

> ⚠️ **MySQL's "you can't specify target table for update in FROM clause" error** bites on ④ — the extra `SELECT … FROM (…) t` wrapper is exactly the workaround. Mention it: *"MySQL won't let you read the same table you're deleting from in a subquery, so you wrap it in a derived table, which forces materialisation."*

## 7.5 🔴 The `LAST_VALUE` trap

```sql
-- ❌ Returns the CURRENT row, not the last one
SELECT emp_name, LAST_VALUE(salary) OVER (PARTITION BY dept_id ORDER BY salary) FROM employee;

-- ✅ Give it the full frame
SELECT emp_name, LAST_VALUE(salary) OVER (
         PARTITION BY dept_id ORDER BY salary
         ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) FROM employee;
```
> *"Because with an `ORDER BY` and no explicit frame the window ends at the current row, so 'last value' is the current value. `FIRST_VALUE` looks fine by accident. The fix is to state the frame."*

## 7.6 Where window functions may **not** appear

🔴 **You cannot use a window function in `WHERE`, `GROUP BY` or `HAVING`** — they're evaluated at the `SELECT` step, after all of those.

```sql
SELECT * FROM employee WHERE ROW_NUMBER() OVER (ORDER BY salary) = 2;   -- ❌ error
```
**The fix is always the same:** wrap it in a subquery or CTE and filter on the outside. *"That's exactly why every top-N query you see has a derived table around it."*

---

# Part 8 — Subqueries: `IN` / `EXISTS` / `JOIN`

## 8.1 The three kinds

| Kind | Returns | Example |
|---|---|---|
| **Scalar** | one value | `WHERE salary > (SELECT AVG(salary) FROM employee)` |
| **Row / column (multi-row)** | a list | `WHERE dept_id IN (SELECT dept_id FROM dept WHERE location='Chennai')` |
| **Correlated** | re-evaluated per outer row — references the outer query | `WHERE EXISTS (SELECT 1 FROM orders o WHERE o.emp_id = e.emp_id)` |
| **Derived table / inline view** | a table in `FROM` | `FROM (SELECT … ) t` |

> **"What's a correlated subquery?"** → *"One that references a column from the outer query, so it can't be run once up-front — logically it re-executes for each outer row. That makes it powerful but potentially O(n) subquery executions; good optimisers rewrite many of them into joins."*

## 8.2 `IN` vs `EXISTS` vs `JOIN` — the comparison they want

```sql
-- Employees who have placed at least one order
SELECT * FROM employee e WHERE e.emp_id IN     (SELECT o.emp_id FROM orders o);              -- IN
SELECT * FROM employee e WHERE EXISTS (SELECT 1 FROM orders o WHERE o.emp_id = e.emp_id);    -- EXISTS
SELECT DISTINCT e.* FROM employee e JOIN orders o ON o.emp_id = e.emp_id;                    -- JOIN
```

| | `IN` | `EXISTS` | `JOIN` |
|---|---|---|---|
| Semantics | value ∈ list | at least one matching row | combine rows |
| Stops early | no | **yes** — short-circuits on the first match | n/a |
| Best when | the subquery result is **small** | the subquery is **large** / correlated | you need **columns from both** tables |
| NULL-safe | ⚠️ `NOT IN` breaks on NULL | ✅ `NOT EXISTS` is safe | ✅ |
| Duplicates | none introduced | none introduced | ⚠️ **multiplies rows** — needs `DISTINCT` |

> *"In modern MySQL 8 and Postgres the optimiser usually rewrites all three into the same plan, so I choose on readability and NULL-safety: `EXISTS` when I only need to test existence, a join when I need the other table's columns. `NOT IN` I avoid on any nullable column."*

## 8.3 The `DISTINCT`-after-join smell

> 🔴 If your answer needs `SELECT DISTINCT` after a join, **say why**: *"The join multiplies rows when an employee has several orders, so I either `DISTINCT` it or switch to `EXISTS`, which doesn't multiply in the first place — `EXISTS` is the cleaner intent here."* Interviewers notice this.

---

# Part 9 — Duplicates: find them, delete them, prevent them

```sql
-- ① Find duplicate emails
SELECT email, COUNT(*) AS cnt
FROM   users
GROUP BY email
HAVING COUNT(*) > 1;

-- ② Show the full duplicate rows, not just the key
SELECT * FROM users
WHERE email IN (SELECT email FROM users GROUP BY email HAVING COUNT(*) > 1)
ORDER BY email;

-- ③ Delete duplicates, keeping the LOWEST id — the self-join classic
DELETE u1 FROM users u1
JOIN users u2 ON u1.email = u2.email AND u1.id > u2.id;

-- ④ Same thing with a window function — clearer, and lets you choose "keep the newest"
DELETE FROM users WHERE id IN (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY email ORDER BY created_at DESC) rn FROM users
  ) t WHERE rn > 1
);

-- ⑤ Prevent it happening again
ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);
```

> 🔴 **The follow-up: "how would you do it if there's no unique id?"**
> *"Postgres gives you the hidden `ctid`; Oracle gives you `ROWID`. In MySQL with no PK I'd create a copy of the table with `SELECT DISTINCT`, or add a surrogate key first. And then I'd add the unique constraint so the question can't come back."*

> 🔴 **"Duplicate on a combination of columns?"** → `PARTITION BY first_name, last_name, dob`, or `GROUP BY` all three. Same shape.

---

# Part 10 — NULLs and three-valued logic

🔴 **This is where most candidates get caught.** SQL logic is **three-valued**: `TRUE`, `FALSE`, and **`UNKNOWN`**. NULL means *"unknown"*, not *"empty"* and not *"zero"*.

## 10.1 The rules

| Expression | Result | Why |
|---|---|---|
| `NULL = NULL` | **UNKNOWN** (not TRUE) | Two unknowns aren't known to be equal |
| `NULL <> 5` | UNKNOWN | |
| `NULL + 10` | **NULL** | Any arithmetic with NULL is NULL |
| `'abc' || NULL` (Postgres) | **NULL** | Concatenation too. MySQL's `CONCAT` also returns NULL; `CONCAT_WS` skips them |
| `WHERE x = NULL` | matches **nothing** | must use `IS NULL` |
| `NULL AND FALSE` | **FALSE** | ← the one that surprises people |
| `NULL OR TRUE` | **TRUE** | |
| `NULL AND TRUE` | UNKNOWN | |
| `COUNT(*)` | counts NULL rows | the only aggregate that does |
| `GROUP BY` | **all NULLs form one group** | even though `NULL = NULL` is unknown |
| `ORDER BY` | NULLs cluster at one end | MySQL: first in `ASC`. Postgres: last in `ASC` |
| `UNIQUE` constraint | usually allows **many** NULLs | because they're not "equal" to each other |
| `DISTINCT` | treats all NULLs as **one** value | |

> **Only rows where the `WHERE` clause evaluates to `TRUE` are returned.** `UNKNOWN` is discarded exactly like `FALSE` — that one sentence explains most NULL surprises.

## 10.2 🔴 The `NOT IN` bomb — the most popular SQL gotcha in interviews

```sql
SELECT * FROM dept WHERE dept_id NOT IN (SELECT dept_id FROM employee);
```
**On our data this returns ZERO rows** — even though Legal (40) genuinely has no employees.

**Why:** the subquery returns `{10,10,10,20,20,30,NULL}` (Gita). `NOT IN` expands to
`dept_id <> 10 AND dept_id <> 20 AND dept_id <> 30 AND dept_id <> NULL`.
That last term is **UNKNOWN**, and `TRUE AND UNKNOWN = UNKNOWN` — so no row is ever TRUE.

**Three fixes:**
```sql
WHERE dept_id NOT IN (SELECT dept_id FROM employee WHERE dept_id IS NOT NULL)   -- ① filter
WHERE NOT EXISTS (SELECT 1 FROM employee e WHERE e.dept_id = dept.dept_id)      -- ② ✅ preferred
LEFT JOIN employee e ON e.dept_id = d.dept_id WHERE e.emp_id IS NULL            -- ③ anti-join
```
> *"`NOT EXISTS` is my default because it's NULL-safe by construction — it asks 'does a matching row exist', which is a two-valued question."*

## 10.3 The NULL-handling functions

| Function | Engine | Does |
|---|---|---|
| `COALESCE(a, b, c)` | **standard, all engines** | first non-NULL argument. Use this one. |
| `IFNULL(a, b)` | MySQL | 2-arg COALESCE |
| `ISNULL(a, b)` | SQL Server | 2-arg COALESCE |
| `NVL(a, b)` | Oracle | 2-arg COALESCE |
| `NULLIF(a, b)` | all | returns NULL if `a = b` — **the divide-by-zero guard**: `x / NULLIF(y, 0)` |
| `IS DISTINCT FROM` | Postgres | NULL-safe `<>` |
| `<=>` | MySQL | NULL-safe `=` — `NULL <=> NULL` is **TRUE** |

## 10.4 The NULL question that catches everyone

> **"`SELECT COUNT(*) FROM employee WHERE salary > 50000 OR salary <= 50000` — does that return 7?"**
>
> **No — 6.** The condition looks exhaustive, but **Farid's salary is NULL**, so both `NULL > 50000` and `NULL <= 50000` evaluate to UNKNOWN, `UNKNOWN OR UNKNOWN` is UNKNOWN, and the row is discarded. Everyone else passes one side or the other.
>
> *"A NULL row satisfies neither a condition nor its negation. If I want it included I have to say so explicitly — `OR salary IS NULL`. This is the reason a `WHERE status <> 'CLOSED'` filter silently drops every row where `status` is NULL, which is a real production bug I'd watch for."*

---

# Part 11 — Set operations

| Operator | Does | Duplicates | Sorting cost |
|---|---|---|---|
| `UNION` | combine + **remove duplicates** | removed | ⚠️ implicit sort/hash — expensive |
| `UNION ALL` | combine, keep everything | kept | ✅ cheap — **use this unless you need dedup** |
| `INTERSECT` | rows in both | removed | (MySQL 8.0.31+, Postgres always) |
| `EXCEPT` / `MINUS` | in the first, not the second | removed | `EXCEPT` = standard/Postgres, `MINUS` = Oracle |

**Rules:** same number of columns, compatible types, column names come from the **first** query, and a single `ORDER BY` goes at the very **end** and applies to the whole result.

```sql
SELECT emp_name, 'current' AS src FROM employee
UNION ALL
SELECT emp_name, 'archived'      FROM employee_archive
ORDER BY emp_name;                 -- applies to the combined result
```

> 🔴 **The interview line:** *"`UNION` has to deduplicate, which means an implicit sort or hash over the whole result. If I know the sets are disjoint — or duplicates are fine — I use `UNION ALL`, and on large result sets that difference is significant."*

**`UNION` vs `JOIN`:** *"A join combines **columns** side by side; a union stacks **rows** on top of each other."*

---

# Part 12 — CTEs and recursive CTEs

## 12.1 Plain CTE — `WITH`

```sql
WITH dept_stats AS (
    SELECT dept_id, COUNT(*) AS headcount, AVG(salary) AS avg_sal
    FROM   employee
    GROUP BY dept_id
)
SELECT d.dept_name, s.headcount, ROUND(s.avg_sal, 2) AS avg_sal
FROM   dept_stats s
JOIN   dept d ON d.dept_id = s.dept_id
WHERE  s.headcount > 1;
```

**Why use one?** Readability, referencing the same intermediate result twice, and replacing deeply nested subqueries. **Multiple CTEs** chain with commas, and a later one can reference an earlier one.

> **"CTE vs subquery vs temp table vs view?"**
> - **Subquery** — inline, single use, can get unreadable when nested.
> - **CTE** — named, reusable within the one statement, self-documenting. In **Postgres ≤ 11** a CTE was an **optimisation fence** (always materialised); from **12** it inlines unless you say `MATERIALIZED`. MySQL 8 may materialise or merge.
> - **Temp table** — persists for the session, can be **indexed**, worth it when you reuse a big intermediate result across several statements.
> - **View** — a stored query definition, permanent, no data of its own. A **materialized view** does store data and needs refreshing.

## 12.2 🔴 Recursive CTE — the employee hierarchy question

```sql
WITH RECURSIVE org_chart AS (
    -- anchor: the top of the tree
    SELECT emp_id, emp_name, manager_id, 1 AS level,
           CAST(emp_name AS CHAR(500)) AS path
    FROM   employee
    WHERE  manager_id IS NULL

    UNION ALL

    -- recursive member: joins the CTE back to the base table
    SELECT e.emp_id, e.emp_name, e.manager_id, oc.level + 1,
           CONCAT(oc.path, ' > ', e.emp_name)
    FROM   employee e
    JOIN   org_chart oc ON e.manager_id = oc.emp_id
)
SELECT level, emp_name, path FROM org_chart ORDER BY path;
```

Output on our data:

| level | emp_name | path |
|---|---|---|
| 1 | Asha | Asha |
| 2 | Bala | Asha > Bala |
| 2 | Chitra | Asha > Chitra |
| 2 | Dev | Asha > Dev |
| 3 | Esha | Asha > Dev > Esha |
| 2 | Farid | Asha > Farid |
| 2 | Gita | Asha > Gita |

**The four things to say:**
1. **Anchor member** — the non-recursive seed (`manager_id IS NULL`).
2. **`UNION ALL`** — joins the recursive member; `UNION` would dedupe and cost a sort.
3. **Termination** — it stops when the recursive member returns no rows. **A cycle in the data means infinite recursion**, so guard it: MySQL's `cte_max_recursion_depth` (default 1000), or carry a `level` and add `WHERE level < 10`, or track the path and exclude nodes already in it.
4. **`RECURSIVE` keyword** — required in MySQL and Postgres, **not used** in SQL Server (just `WITH`).

> **Other uses to mention:** generating a date series to fill gaps in a report, exploding a bill-of-materials, walking a category tree.

---

# Part 13 — Self joins and hierarchies

```sql
-- ① Every employee with their manager's name — LEFT so the CEO still appears
SELECT e.emp_name AS employee, COALESCE(m.emp_name, '— none —') AS manager
FROM   employee e
LEFT JOIN employee m ON m.emp_id = e.manager_id;

-- ② Employees who earn MORE than their manager
SELECT e.emp_name, e.salary, m.emp_name AS manager, m.salary AS mgr_salary
FROM   employee e
JOIN   employee m ON m.emp_id = e.manager_id
WHERE  e.salary > m.salary;

-- ③ Managers with more than 2 direct reports
SELECT m.emp_name, COUNT(*) AS reports
FROM   employee e
JOIN   employee m ON m.emp_id = e.manager_id
GROUP BY m.emp_id, m.emp_name
HAVING COUNT(*) > 2;

-- ④ Employees who are NOT managers (nobody reports to them)
SELECT e.emp_name FROM employee e
WHERE NOT EXISTS (SELECT 1 FROM employee r WHERE r.manager_id = e.emp_id);

-- ⑤ Pairs of employees in the same department (each pair once, no self-pairs)
SELECT a.emp_name, b.emp_name, a.dept_id
FROM   employee a
JOIN   employee b ON a.dept_id = b.dept_id AND a.emp_id < b.emp_id;
--                                             ^^^^^^^^^^^^^^^^^^^ the classic trick
```

> 🔴 **`a.emp_id < b.emp_id` is the detail they're checking for in ⑤.** With `<>` you'd get each pair twice in both orders; with nothing you'd also pair every employee with themselves. Say it out loud.

**Consecutive rows** — the "three consecutive days" / "logged in 3 days running" family:
```sql
-- Consecutive rows using LAG
SELECT * FROM (
  SELECT id, num,
         LAG(num, 1) OVER (ORDER BY id) AS prev1,
         LAG(num, 2) OVER (ORDER BY id) AS prev2
  FROM logs
) t
WHERE num = prev1 AND num = prev2;
```

---

# Part 14 — Dates, strings and `CASE`

## 14.1 Dates

```sql
-- Employees hired in 2023
WHERE hire_date >= '2023-01-01' AND hire_date < '2024-01-01'   -- ✅ sargable, uses the index
WHERE YEAR(hire_date) = 2023                                    -- ❌ function on the column kills the index

-- Tenure in years
SELECT emp_name, TIMESTAMPDIFF(YEAR, hire_date, CURDATE()) AS years   -- MySQL
SELECT emp_name, DATE_PART('year', AGE(CURRENT_DATE, hire_date))      -- Postgres

-- Last 30 days
WHERE order_date >= CURDATE() - INTERVAL 30 DAY          -- MySQL
WHERE order_date >= CURRENT_DATE - INTERVAL '30 days'    -- Postgres

-- Group by month
SELECT DATE_FORMAT(order_date, '%Y-%m') AS ym, SUM(amount)  -- MySQL
FROM orders GROUP BY ym ORDER BY ym;
SELECT DATE_TRUNC('month', order_date) AS ym, SUM(amount)   -- Postgres
FROM orders GROUP BY 1 ORDER BY 1;
```

🔴 **The rule to say:** *"Never wrap the indexed column in a function — rewrite it as a range. `YEAR(hire_date)=2023` forces a full scan; `hire_date >= '2023-01-01' AND hire_date < '2024-01-01'` is **sargable** and seeks the index."* The word *sargable* ("Search-ARGument-able") lands well.

⚠️ **`BETWEEN` with datetimes:** `BETWEEN '2023-01-01' AND '2023-12-31'` **misses everything on 31 Dec after midnight**, because the end is `00:00:00`. Use the half-open range `>= start AND < next_start`.

## 14.2 Strings

| Task | MySQL | Postgres |
|---|---|---|
| Concatenate | `CONCAT(a,b)` / `CONCAT_WS('-',a,b)` | `a \|\| b` |
| Length | `LENGTH` (bytes) / `CHAR_LENGTH` | `LENGTH` |
| Substring | `SUBSTRING(s, 1, 3)` | same |
| Position | `LOCATE('x', s)` | `POSITION('x' IN s)` |
| Upper/lower/trim | `UPPER` `LOWER` `TRIM` | same |
| Replace | `REPLACE(s,'a','b')` | same |
| Split | `SUBSTRING_INDEX(email,'@',-1)` | `SPLIT_PART(email,'@',2)` |
| Case sensitivity | ⚠️ default collation is **case-insensitive** | **case-sensitive**; use `ILIKE` |

```sql
-- Domain from an email — a small live question they like
SELECT SUBSTRING_INDEX(email, '@', -1) AS domain, COUNT(*)
FROM users GROUP BY domain ORDER BY 2 DESC;
```

## 14.3 `CASE` — and pivoting without `PIVOT`

```sql
-- ① Conditional aggregation — headcount per band in one row
SELECT COUNT(CASE WHEN salary >= 70000 THEN 1 END) AS high,
       COUNT(CASE WHEN salary <  70000 THEN 1 END) AS low,
       SUM(CASE WHEN salary IS NULL THEN 1 ELSE 0 END) AS unknown
FROM employee;

-- ② Pivot: departments as columns
SELECT DATE_FORMAT(o.order_date,'%Y-%m') AS ym,
       SUM(CASE WHEN o.status='PAID'      THEN o.amount ELSE 0 END) AS paid,
       SUM(CASE WHEN o.status='CANCELLED' THEN o.amount ELSE 0 END) AS cancelled
FROM orders o
GROUP BY ym ORDER BY ym;

-- ③ Custom sort order
ORDER BY CASE status WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 ELSE 3 END, order_date;
```

> 🔴 **"Conditional aggregation" is the phrase to use** — `SUM(CASE WHEN … THEN 1 ELSE 0 END)` is how you pivot in any database without a `PIVOT` clause, and it's a genuinely common real-world pattern. Note `COUNT(CASE WHEN … THEN 1 END)` works too because the implicit `ELSE NULL` isn't counted.

---

# Part 15 — Indexes and query tuning

🔴 **"How would you speed up a slow query?" is asked in almost every backend round.** Have a *method*, not a list of tricks.

## 15.1 The answer script — say it in this order

> *"First I'd **measure, not guess** — run `EXPLAIN ANALYZE` and look at what the plan actually does: is it a full table scan, how many rows is it estimating versus returning, and where is the time going. Then, in order: **① is there an index on what I filter, join and sort by** — and is it usable, or am I wrapping the column in a function; **② am I selecting more than I need** — `SELECT *`, or rows I then throw away; **③ is this actually an N+1** coming from the ORM rather than one slow query; **④ can I reduce the work** — filter earlier, avoid `SELECT DISTINCT` after a fan-out join, replace a correlated subquery with a join or window function. Only after that would I look at caching, denormalisation or pagination changes."*

## 15.2 What an index is

> *"Usually a **B+ tree**: sorted, balanced, and it lets the engine **seek** to a range instead of scanning the table. The cost is slower writes — every `INSERT`/`UPDATE`/`DELETE` maintains every index — plus disk and memory. So I index what I filter, join and sort on, not everything."*

| Type | What it is |
|---|---|
| **Clustered** | **Is** the table — rows are physically stored in the index's order. **One per table** (the PK in InnoDB). |
| **Non-clustered / secondary** | A separate structure holding the key + a pointer back (in InnoDB, the **PK value**, so a secondary-index lookup costs a second seek — "bookmark lookup"). |
| **Composite** | Multiple columns, order matters — see the leftmost rule. |
| **Covering** | Contains every column the query needs → **index-only scan**, never touches the table. `EXPLAIN` shows *"Using index"*. |
| **Unique** | Index + constraint. |
| **Partial / filtered** | `WHERE deleted = false` — Postgres/SQL Server only. |
| **Full-text / hash / GIN** | For text search and specialised lookups. |

## 15.3 🔴 The leftmost-prefix rule — the composite-index question

Index on `(dept_id, salary, hire_date)`:

| Query | Uses the index? |
|---|---|
| `WHERE dept_id = 10` | ✅ |
| `WHERE dept_id = 10 AND salary > 50000` | ✅ both columns |
| `WHERE dept_id = 10 ORDER BY salary` | ✅ — and the sort is free |
| `WHERE salary > 50000` | ❌ **no** — skips the leading column |
| `WHERE salary > 50000 AND hire_date > '2020-01-01'` | ❌ |
| `WHERE dept_id = 10 AND hire_date > '2020-01-01'` | ⚠️ partial — seeks on `dept_id`, then filters |

> *"A composite index is sorted by the first column, then the second within that — like a phone book sorted by surname then first name. You can look up 'everyone called Kumar', and 'Kumar, Priya', but not 'everyone called Priya'."* **That phone-book line is worth memorising.**

## 15.4 When an index is silently ignored

| Cause | Fix |
|---|---|
| Function on the column: `WHERE YEAR(d) = 2023`, `WHERE UPPER(name) = 'X'` | Rewrite as a range, or add a **functional/expression index** |
| Leading wildcard: `LIKE '%abc'` | Full-text index, or store a reversed column. `LIKE 'abc%'` **is** indexable |
| Implicit type cast: `WHERE varchar_col = 123` | Match the types |
| `OR` across different columns | Rewrite as `UNION`, or index both and hope for an index merge |
| Low selectivity — a `gender` or `is_active` flag | Don't index it alone; make it part of a composite |
| Stale statistics | `ANALYZE TABLE` |
| The optimiser judges a scan cheaper (returning >~20% of rows) | Often correct — leave it |

## 15.5 Reading `EXPLAIN`

```sql
EXPLAIN SELECT ...            -- the plan
EXPLAIN ANALYZE SELECT ...    -- the plan + actual execution times (MySQL 8.0.18+, Postgres)
```

| MySQL `type` | Meaning |
|---|---|
| `system` / `const` / `eq_ref` | 🟢 best — one row via a PK/unique lookup |
| `ref` | 🟢 good — non-unique index lookup |
| `range` | 🟡 fine — an index range scan |
| `index` | 🟠 full **index** scan — better than a table scan, still everything |
| `ALL` | 🔴 **full table scan** — the thing you're usually hunting |

Also read: **`rows`** (the estimate — if it's wildly off, statistics are stale), **`key`** (which index was chosen, `NULL` = none), and **`Extra`**: *"Using index"* = covering ✅, *"Using filesort"* / *"Using temporary"* = a sort or temp table you may be able to design away.

## 15.6 The rest of the tuning toolkit

- **Avoid `SELECT *`** — more I/O, breaks covering indexes, and breaks when a column is added.
- **Pagination:** `LIMIT 20 OFFSET 100000` still walks 100,020 rows. Use **keyset pagination**: `WHERE id > :last_seen ORDER BY id LIMIT 20`.
- **Batch, don't loop** — one `INSERT … VALUES (…),(…),(…)` beats 1,000 round trips; in JPA that's `saveAll` + `hibernate.jdbc.batch_size`.
- **`EXISTS` over `IN`** for large correlated subqueries; **`UNION ALL`** over `UNION`.
- **Denormalise deliberately** for read-heavy reporting — *"a reporting table that duplicates a department name is a legitimate trade, not a mistake."*
- **Partitioning** for very large tables (by date range), and **archiving** old rows.
- **Connection pooling** (HikariCP) — *"a slow endpoint is sometimes pool exhaustion, not a slow query."*

---

# Part 16 — Transactions, ACID and isolation levels

## 16.1 ACID — with a one-line example each (they ask for examples, not definitions)

| | Means | Your example |
|---|---|---|
| **A**tomicity | All or nothing | *"A funds transfer is a debit and a credit. If the credit fails, the debit is rolled back — you can't have half of it."* |
| **C**onsistency | The DB moves from one valid state to another; constraints hold | *"An order can't reference a customer id that doesn't exist — the FK stops the transaction committing."* |
| **I**solation | Concurrent transactions don't corrupt each other | *"Two people book the last seat at once; isolation decides whether one of them fails cleanly."* |
| **D**urability | Once committed, it survives a crash | *"Committed means written to the write-ahead log and fsynced, so a power cut doesn't lose it."* |

## 16.2 🔴 The three read phenomena and the four isolation levels

| Phenomenon | What happens |
|---|---|
| **Dirty read** | You read another transaction's **uncommitted** change; it then rolls back |
| **Non-repeatable read** | You read the same **row** twice and get different values (someone committed an `UPDATE` between) |
| **Phantom read** | You run the same **query** twice and get different **rows** (someone committed an `INSERT` matching your `WHERE`) |

| Isolation level | Dirty | Non-repeatable | Phantom | Notes |
|---|---|---|---|---|
| `READ UNCOMMITTED` | ✅ possible | ✅ | ✅ | Practically never used |
| `READ COMMITTED` | ❌ | ✅ | ✅ | **Default in Postgres, Oracle, SQL Server** |
| `REPEATABLE READ` | ❌ | ❌ | ⚠️ | **Default in MySQL/InnoDB** — and InnoDB's next-key locks prevent most phantoms too |
| `SERIALIZABLE` | ❌ | ❌ | ❌ | As if transactions ran one at a time. Safest, most contention |

```sql
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
-- Spring:
@Transactional(isolation = Isolation.READ_COMMITTED)
```

> 🔴 **The follow-up that wins the topic:** *"The default differs — MySQL is `REPEATABLE READ`, Postgres is `READ COMMITTED`. That's worth knowing because the same Spring Boot service can behave differently on the two databases."*

## 16.3 Optimistic vs pessimistic locking — the practical one for a Spring dev

| | Pessimistic | Optimistic |
|---|---|---|
| Idea | Lock the row up front: `SELECT … FOR UPDATE` | No lock; a **`@Version` column** detects conflicts at write time |
| Cost | Blocks other transactions, risks deadlock | Cheap reads; the loser retries |
| Use when | Contention is **high**, conflicts are expensive (inventory, payments) | Contention is **low** — most web CRUD |
| JPA | `@Lock(LockModeType.PESSIMISTIC_WRITE)` | `@Version` → `OptimisticLockException` |

> *"On RoboGebra-style CRUD I'd default to optimistic locking with a `@Version` column — conflicts are rare, and I'd surface a 409 to the client rather than hold a database lock across a user's thinking time."*

## 16.4 Deadlock at the database level

> *"Two transactions each hold a lock the other wants. InnoDB **detects** the cycle and kills the cheaper transaction with a deadlock error — so my job is to **catch it and retry**, and to reduce how often it happens: **always acquire locks in the same order**, keep transactions short, don't do HTTP calls inside a transaction, and use the narrowest isolation level that's correct."*

🔗 This is the database twin of the Java-level deadlock question in **[32 — Multithreading](./32-multithreading.md)**. If you get asked "what's a deadlock", **ask which one they mean** — that's a good look.

**Savepoints** — worth one line: `SAVEPOINT s1; … ROLLBACK TO s1;` lets you undo part of a transaction. In Spring that's `Propagation.NESTED`.

---

# Part 17 — Normalization, keys and constraints

## 17.1 Normal forms — with the one example that shows each

| Form | Rule | Violation → fix |
|---|---|---|
| **1NF** | Atomic values; no repeating groups | A `phone_numbers` column holding `'9x, 8y'` → a separate `phone` table |
| **2NF** | 1NF **+ no partial dependency** on part of a composite key | In `order_item(order_id, product_id, qty, product_name)`, `product_name` depends only on `product_id` → move it to `product` |
| **3NF** | 2NF **+ no transitive dependency** (non-key → non-key) | In `employee(emp_id, dept_id, dept_name)`, `dept_name` depends on `dept_id`, not on `emp_id` → move it to `dept` |
| **BCNF** | Stricter 3NF: every determinant is a candidate key | Rare in interviews — one sentence is enough |

> **The line to close on:** *"3NF is my default for a transactional schema — it removes update anomalies. But I denormalise deliberately for read-heavy paths, reporting tables and dashboards, and I treat that as a documented trade, not an accident."*

**Denormalisation trade-off:** faster reads and fewer joins, at the cost of duplicated data you must keep in sync.

## 17.2 Keys

| Key | Meaning |
|---|---|
| **Super key** | Any set of columns that uniquely identifies a row |
| **Candidate key** | A minimal super key |
| **Primary key** | The candidate key you chose. **Unique + `NOT NULL`**, one per table |
| **Alternate key** | The candidate keys you didn't choose |
| **Unique key** | Unique, but **allows NULL** (and in most engines, multiple NULLs) |
| **Composite key** | A PK made of more than one column |
| **Foreign key** | References another table's PK/unique key — enforces referential integrity |
| **Surrogate vs natural** | Auto-increment/UUID vs a real-world value (email, PAN). Surrogate is usually safer — natural keys change |

🔴 **"Primary key vs unique key"** — the answer they want: *"Both enforce uniqueness. A primary key can't be NULL and there's only one; a unique key allows NULLs and you can have several. In InnoDB the PK is also the **clustered index**, so it determines physical row order — which is why a random UUID PK hurts insert performance and a sequential id doesn't."* That last clause is the mid-level differentiator.

## 17.3 Constraints and `ON DELETE`

`NOT NULL` · `UNIQUE` · `PRIMARY KEY` · `FOREIGN KEY` · `CHECK` (MySQL enforces it from 8.0.16) · `DEFAULT`

```sql
ALTER TABLE employee
  ADD CONSTRAINT fk_dept FOREIGN KEY (dept_id) REFERENCES dept(dept_id)
  ON DELETE SET NULL ON UPDATE CASCADE;
```

| `ON DELETE` | Effect on child rows when the parent is deleted |
|---|---|
| `RESTRICT` / `NO ACTION` | Block the delete (the default) |
| `CASCADE` | Delete the children too — ⚠️ *"powerful and easy to regret; I prefer explicit deletes in the service layer"* |
| `SET NULL` | Null out the FK (the column must be nullable) |

---

# Part 18 — DDL vs DML, `DELETE` vs `TRUNCATE` vs `DROP`

| Category | Commands | Transactional? |
|---|---|---|
| **DDL** — Data **Definition** | `CREATE` `ALTER` `DROP` `TRUNCATE` `RENAME` | Auto-commits in MySQL/Oracle; **transactional in Postgres** |
| **DML** — Data **Manipulation** | `SELECT` `INSERT` `UPDATE` `DELETE` `MERGE` | ✅ |
| **DCL** — Data **Control** | `GRANT` `REVOKE` | |
| **TCL** — Transaction **Control** | `COMMIT` `ROLLBACK` `SAVEPOINT` | |

| | `DELETE` | `TRUNCATE` | `DROP` |
|---|---|---|---|
| Type | DML | DDL | DDL |
| Removes | Selected rows | **All** rows | Rows **+ the table itself** |
| `WHERE` | ✅ | ❌ | ❌ |
| Rollback | ✅ | ❌ in MySQL/Oracle (auto-commit) | ❌ in MySQL/Oracle |
| Fires triggers | ✅ | ❌ | ❌ |
| Resets `AUTO_INCREMENT` | ❌ | ✅ | — |
| Speed | Slow — row by row, logged | **Fast** — deallocates pages | Fast |
| Table/structure survives | ✅ | ✅ | ❌ |

> *"`DELETE` is row-by-row and logged so it can be rolled back and fires triggers. `TRUNCATE` deallocates the pages, so it's far faster but it's DDL — no `WHERE`, no triggers, and in MySQL it auto-commits. `DROP` removes the table definition as well."*

---

# Part 19 — Views, stored procedures, functions, triggers

| Object | What it is | When you'd use it |
|---|---|---|
| **View** | A stored `SELECT`. No data of its own — resolved at query time | Simplify a repeated join; expose a subset of columns as a security boundary |
| **Materialized view** | A view whose results **are** stored; needs `REFRESH` | Expensive reporting aggregates. Postgres/Oracle — MySQL has no native support |
| **Stored procedure** | Compiled SQL block, can have `OUT` params, may not return a value | Bulk/batch operations that would otherwise be many round trips |
| **Function** | Returns a value, usable **inside** a `SELECT` | A reusable calculation |
| **Trigger** | Fires automatically on `INSERT`/`UPDATE`/`DELETE` | Audit trails, `updated_at` maintenance |

```sql
CREATE VIEW v_dept_summary AS
SELECT d.dept_id, d.dept_name, COUNT(e.emp_id) AS headcount, AVG(e.salary) AS avg_sal
FROM dept d LEFT JOIN employee e ON e.dept_id = d.dept_id
GROUP BY d.dept_id, d.dept_name;
```

**"Can you update through a view?"** → *"Only a simple one — single table, no aggregate, no `DISTINCT`, no `GROUP BY`. Anything with an aggregate is read-only. Postgres can make complex views updatable with an `INSTEAD OF` trigger."*

**Procedure vs function** — the table they ask for:

| | Procedure | Function |
|---|---|---|
| Returns | via `OUT` params / result sets | a single value (or table) |
| Callable in `SELECT` | ❌ | ✅ |
| Can do DML | ✅ | usually restricted |
| Invoked by | `CALL p()` | inside an expression |

> 🔴 **The opinion to have ready** — because they *will* ask "would you put business logic in the database?":
> *"I keep business logic in the application, in the service layer, so it's version-controlled, unit-testable and portable. Stored procedures are hard to diff and hard to test. I'd use one where it genuinely wins — a bulk operation where moving the data to the app is the bottleneck. Triggers I use very sparingly: they're invisible at the call site, so someone debugging the application never thinks to look for them."* **Naming that "invisible side effect" cost is what makes this sound like experience.**

---

# Part 20 — SQL from the Java / Spring side

🔴 **In a fullstack Java round the SQL question often arrives dressed as a Spring question.** These are the crossovers.

## 20.1 The N+1 problem — the most-asked ORM question anywhere

```java
List<Dept> depts = deptRepo.findAll();            // 1 query
for (Dept d : depts) d.getEmployees().size();     // + N queries, one per department
```

**Detect it:** turn on `spring.jpa.show-sql=true` (or better, use a query counter in tests) and watch one screen of near-identical `SELECT`s go by.

**Fix it — four ways, know all four:**

| Fix | How |
|---|---|
| `JOIN FETCH` | `@Query("SELECT d FROM Dept d LEFT JOIN FETCH d.employees")` |
| `@EntityGraph` | `@EntityGraph(attributePaths = "employees")` on the repository method |
| Batch fetching | `@BatchSize(size = 25)` — turns N queries into N/25 `IN` queries |
| A projection / DTO query | Select only the columns you need — often the best answer for a read endpoint |

> ⚠️ **The `JOIN FETCH` + pagination trap:** *"`JOIN FETCH` with `Pageable` makes Hibernate fetch everything and paginate in memory — it logs `HHH000104: firstResult/maxResults specified with collection fetch`. The fix is two queries: page the ids, then fetch the collection for those ids."* 🔗 Full depth in **[24 — ORM/JPA](./24-orm-jpa-hibernate.md)**.

## 20.2 JPQL vs native SQL vs Criteria

| | Operates on | Use when |
|---|---|---|
| **JPQL** / HQL | **Entities and fields**, not tables and columns | Most queries — portable across databases |
| **Native SQL** | Real tables | Window functions, `WITH RECURSIVE`, vendor-specific features, heavy tuning |
| **Criteria API** | Type-safe builder | Dynamic queries — a search form with 6 optional filters |
| **Derived queries** | Method name | `findByDeptIdAndSalaryGreaterThan(…)` — simple lookups |

```java
@Query(value = "SELECT * FROM employee WHERE salary > :min", nativeQuery = true)
List<Employee> findRicherThan(@Param("min") BigDecimal min);
```

> *"I reach for native SQL when the query is genuinely SQL-shaped — a window function or a recursive CTE — and I accept that I've traded portability for it. And I map it to a DTO projection rather than an entity, so Hibernate doesn't try to manage rows I only want to read."*

## 20.3 🔴 SQL injection — the security question inside the SQL question

```java
// ❌ Vulnerable — string concatenation
String sql = "SELECT * FROM users WHERE email = '" + email + "'";
// email = "x' OR '1'='1"  →  returns every user

// ✅ Parameterised / prepared statement — the value is never parsed as SQL
jdbcTemplate.query("SELECT * FROM users WHERE email = ?", rowMapper, email);

// ✅ JPA named parameters
@Query("SELECT u FROM User u WHERE u.email = :email")
```

> *"The real fix is **parameterised queries**, not input sanitising — with a bind parameter the value is sent separately from the statement and can never be interpreted as SQL. Escaping by hand is a losing game. Beyond that: least-privilege database accounts, validate input anyway, and never surface raw SQL errors to the client."*
> ⚠️ **What binding can't parameterise:** table and column names, and `ORDER BY` directions. If those are dynamic, **whitelist** them against a fixed set — never concatenate.

## 20.4 `@Transactional` — the three traps

1. **Self-invocation doesn't work.** A `this.method()` call inside the same bean bypasses the proxy, so the annotation does nothing.
2. **Default rollback is on `RuntimeException` only.** A checked exception commits unless you say `@Transactional(rollbackFor = Exception.class)`.
3. **Don't wrap remote calls.** An HTTP call inside a transaction holds a database connection and any row locks for the whole round trip.

🔗 Full treatment in **[30 §D4](./30-mphasis-level2-client-round.md)** and **[24](./24-orm-jpa-hibernate.md)**.

## 20.5 Connection pooling — one paragraph

> *"Opening a database connection is expensive, so Spring Boot ships **HikariCP** and reuses them. The pool size is a ceiling on concurrency — if every request holds a connection while it makes a slow external call, you exhaust the pool and requests queue up looking like a 'slow database' when the database is idle. That's why I keep transactions short and outside I/O."*

---

# Part 21 — Predict the output — 23 SQL programs

> **Run these against the sample data in [Part 1](#part-1--the-schema-used-in-this-whole-file).** Cover the answer, commit to a number, then check. **This is the format that has cost you interviews before** ([23 — Java Output](./23-java-output-tricky-questions.md)) — do it the same way here.

---

**① **
```sql
SELECT COUNT(*), COUNT(salary), COUNT(dept_id), COUNT(DISTINCT dept_id) FROM employee;
```
<details><summary>Answer</summary>

**`7, 6, 6, 3`** — 7 rows; Farid's salary is NULL; Gita's dept_id is NULL; distinct non-NULL departments are 10, 20, 30.
</details>

---

**② **
```sql
SELECT AVG(salary) FROM employee;
```
<details><summary>Answer</summary>

**`65833.33`** — `395000 / 6`, **not** `/7`. `AVG` ignores NULLs. To divide by 7: `SUM(salary)/COUNT(*)` → `56428.57`.
</details>

---

**③ **
```sql
SELECT COUNT(*) FROM dept d LEFT JOIN employee e ON e.dept_id = d.dept_id;
```
<details><summary>Answer</summary>

**7** — 6 matched rows (Gita has no dept, so she isn't reachable from `dept`) **+ 1 row for Legal** with NULL employee columns.
</details>

---

**④ **
```sql
SELECT * FROM dept WHERE dept_id NOT IN (SELECT dept_id FROM employee);
```
<details><summary>Answer</summary>

**Zero rows.** 🔴 The subquery contains a NULL (Gita), so `dept_id <> NULL` is UNKNOWN and the whole `AND` chain can never be TRUE. Expected Legal; got nothing. Fix with `NOT EXISTS` or `WHERE dept_id IS NOT NULL` inside the subquery.
</details>

---

**⑤ **
```sql
SELECT d.dept_name, COUNT(*) FROM dept d
LEFT JOIN employee e ON e.dept_id = d.dept_id
WHERE e.salary > 50000
GROUP BY d.dept_name;
```
<details><summary>Answer</summary>

**Engineering 3, Sales 1.** Two things happened: the `WHERE` on the right table **silently turned the left join into an inner join** (Legal vanishes), and Farid's NULL salary fails the predicate (HR vanishes). Esha at exactly 50000 fails `>`. To keep every department, move the condition into the `ON`.
</details>

---

**⑥ **
```sql
SELECT emp_name, salary,
       RANK()       OVER (ORDER BY salary DESC) r,
       DENSE_RANK() OVER (ORDER BY salary DESC) dr
FROM employee WHERE salary IS NOT NULL;
```
<details><summary>Answer</summary>

| emp_name | salary | r | dr |
|---|---|---|---|
| Asha | 90000 | 1 | 1 |
| Bala | 75000 | 2 | 2 |
| Chitra | 75000 | 2 | 2 |
| Dev | 60000 | **4** | **3** |
| Esha | 50000 | 5 | 4 |
| Gita | 45000 | 6 | 5 |

`RANK` skips after a tie, `DENSE_RANK` doesn't.
</details>

---

**⑦ **
```sql
SELECT salary FROM employee ORDER BY salary DESC LIMIT 1 OFFSET 1;
```
<details><summary>Answer</summary>

**`75000`** — correct here **by luck**. Because the top salary is unique, row 2 happens to be the second-highest. If Asha had a twin on 90000, this would return **90000**. **Always add `DISTINCT`** for the "Nth highest" question.
</details>

---

**⑧ **
```sql
SELECT COUNT(*) FROM employee e JOIN dept d ON d.dept_id = e.dept_id;
```
<details><summary>Answer</summary>

**6** — Gita's NULL `dept_id` matches nothing (`NULL = 10` is UNKNOWN), so she's dropped by the inner join.
</details>

---

**⑨ **
```sql
SELECT dept_id, COUNT(*) FROM employee GROUP BY dept_id;
```
<details><summary>Answer</summary>

**4 groups: `10→3`, `20→2`, `30→1`, `NULL→1`.** 🔴 `GROUP BY` puts all NULLs in **one group**, even though `NULL = NULL` is UNKNOWN. It's a documented exception — grouping uses "not distinct from", not `=`.
</details>

---

**⑩ **
```sql
SELECT SUM(salary) FROM employee WHERE dept_id = 40;
```
<details><summary>Answer</summary>

**One row containing `NULL`** — not zero rows, and not `0`. An aggregate with no `GROUP BY` always returns exactly one row, and `SUM` over an empty set is NULL. Use `COALESCE(SUM(salary), 0)`.
(But `SELECT COUNT(*) …` on the same filter returns **0**, because `COUNT` never returns NULL.)
</details>

---

**⑪ **
```sql
SELECT emp_name FROM employee WHERE dept_id <> 10;
```
<details><summary>Answer</summary>

**Dev, Esha, Farid** — 3 rows. 🔴 **Gita is missing**: `NULL <> 10` is UNKNOWN, not TRUE. This is the everyday version of the NULL trap — a `<>` filter silently drops NULL rows. Fix: `WHERE dept_id <> 10 OR dept_id IS NULL`.
</details>

---

**⑫ **
```sql
SELECT dept_id, AVG(salary) FROM employee GROUP BY dept_id HAVING AVG(salary) > 60000;
```
<details><summary>Answer</summary>

**`10 → 80000`** only. Sales (20) averages 55000. HR (30) has only Farid with a NULL salary, so `AVG` is NULL and `NULL > 60000` is UNKNOWN → excluded. Gita's NULL group averages 45000.
</details>

---

**⑬ **
```sql
SELECT COUNT(*) FROM employee e1, employee e2 WHERE e1.dept_id = e2.dept_id;
```
<details><summary>Answer</summary>

**14.** Old-style comma join = cross join filtered by `WHERE`. Per department, each employee pairs with every employee in the same department: Engineering 3×3=9, Sales 2×2=4, HR 1×1=1 → 14. Gita's NULL matches nothing. *"I'd write it as an explicit `JOIN … ON` — comma joins make it far too easy to forget the condition and produce a cartesian product."*
</details>

---

**⑭ **
```sql
SELECT emp_name, LAST_VALUE(salary) OVER (ORDER BY salary) FROM employee WHERE salary IS NOT NULL;
```
<details><summary>Answer</summary>

**Each row returns its own salary**, not the maximum. With an `ORDER BY` and no explicit frame, the default is `RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW`, so "last value" is the current row. Fix with `ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING`.
</details>

---

**⑮ **
```sql
SELECT 'a' = 'A';           -- MySQL (default collation)
```
<details><summary>Answer</summary>

**`1` (true) in MySQL** — the default collation is case-**insensitive**. In **Postgres it's false**. This bites when a service is developed on MySQL and deployed on Postgres, or when you assume `WHERE email = ?` is case-sensitive. Use an explicit collation, or normalise to lower case on write.
</details>

---

**⑯ **
```sql
SELECT COUNT(*) FROM (SELECT DISTINCT salary FROM employee) t;
```
<details><summary>Answer</summary>

**6.** `DISTINCT` treats all NULLs as one value and **keeps** it: `{90000, 75000, 60000, 50000, 45000, NULL}`. Contrast with `COUNT(DISTINCT salary)` = **5**, which drops NULL. Same data, two different answers — that contrast is the whole question.
</details>

---

**⑰ **
```sql
UPDATE employee SET salary = salary * 1.1;   -- how many rows change?
```
<details><summary>Answer</summary>

**6 rows change; 7 are "matched".** Farid's `NULL * 1.1` is NULL, so his value is unchanged — MySQL reports `Rows matched: 7  Changed: 6`. **And there's no `WHERE`** — always run the `SELECT` first, and wrap it in a transaction on production. *"Before an UPDATE without a WHERE I'd run `BEGIN`, check the affected count, then `COMMIT`."*
</details>

---

**⑱ **
```sql
SELECT e.emp_name, m.emp_name AS mgr FROM employee e JOIN employee m ON m.emp_id = e.manager_id;
```
<details><summary>Answer</summary>

**6 rows** — everyone except **Asha**, whose `manager_id` is NULL. To include the CEO, make it a `LEFT JOIN` and `COALESCE` the manager name. This is the standard self-join follow-up.
</details>

---

**⑲ Join cardinality.** `A(id)` holds `1, 1, 2` · `B(id)` holds `1, 2, 2`.
```sql
SELECT COUNT(*) FROM A INNER JOIN B ON A.id = B.id;
SELECT COUNT(*) FROM A LEFT  JOIN B ON A.id = B.id;
SELECT COUNT(*) FROM A CROSS JOIN B;
```
<details><summary>Answer</summary>

**4, 4, 9.** Value 1: `2 × 1 = 2`. Value 2: `1 × 2 = 2`. Inner = **4**. Every row on both sides matched, so LEFT is also **4**. Cross = `3 × 3 =` **9**.
</details>

---

**⑳ Now with NULLs.** `A(id)` holds `1, 2, 3, NULL` · `B(id)` holds `1, 1, NULL`.
<details><summary>Answer</summary>

| Join | Count | Working |
|---|---|---|
| `INNER` | **2** | only value 1 is in both: `1 × 2` |
| `LEFT` | **5** | 2 + unmatched A rows `2, 3, NULL` |
| `RIGHT` | **3** | 2 + B's unmatched `NULL` |
| `FULL` | **6** | 2 + 3 + 1 |
| `CROSS` | **12** | `4 × 3` |

The two NULLs never meet — `NULL = NULL` is UNKNOWN.
</details>

---

**㉑ Counting on a left join.** `A(id)` = `1,1,2,2` · `B(id)` = `1,1,3`.
```sql
SELECT COUNT(*), COUNT(b.id), COUNT(DISTINCT a.id) FROM A a LEFT JOIN B b ON a.id = b.id;
```
<details><summary>Answer</summary>

**`6, 4, 2`.** Six output rows (4 matched + 2 placeholder rows for the unmatched `2`s). `COUNT(b.id)` skips those two NULL placeholders → 4. Distinct `a.id` values are 1 and 2 → 2. **Same trap as the department-headcount question.**
</details>

---

**㉒ The double-count.** Asha earns 90000 and has 3 orders.
```sql
SELECT e.emp_name, SUM(e.salary) FROM employee e JOIN orders o ON o.emp_id = e.emp_id
WHERE e.emp_name = 'Asha' GROUP BY e.emp_id, e.emp_name;
```
<details><summary>Answer</summary>

**270000** — not 90000. The join produces one row per order, so her salary is summed three times. Fix: aggregate `orders` in a subquery first, or use `MAX(e.salary)`. **This is the real-world version of the `1,1,1,1` puzzle.**
</details>

---

**㉓ Self join.** A department has exactly 4 employees.
```sql
SELECT COUNT(*) FROM employee e1 JOIN employee e2 ON e1.dept_id = e2.dept_id;              -- ?
SELECT COUNT(*) FROM employee e1 JOIN employee e2 ON e1.dept_id = e2.dept_id
                                                 AND e1.emp_id <> e2.emp_id;               -- ?
SELECT COUNT(*) FROM employee e1 JOIN employee e2 ON e1.dept_id = e2.dept_id
                                                 AND e1.emp_id <  e2.emp_id;               -- ?
```
<details><summary>Answer</summary>

**16, 12, 6.** `4×4 = 16` including self-pairs. `<>` removes the 4 self-pairs → 12, but each pair appears in both orders. `<` gives each unordered pair exactly once → `C(4,2) = 6`. **`<` is the answer they want for "list pairs of colleagues".**
</details>

---

# Part 22 — Rapid-fire: 65 questions

| # | Question | One-line answer |
|---|---|---|
| 1 | `WHERE` vs `HAVING` | `WHERE` filters rows before grouping; `HAVING` filters groups after aggregation |
| 2 | Can `WHERE` use an aggregate? | No — it runs before `GROUP BY`, the aggregate doesn't exist yet |
| 3 | Can `ORDER BY` use a `SELECT` alias? | Yes — it runs after `SELECT`. `WHERE` can't |
| 4 | Logical execution order | FROM → WHERE → GROUP BY → HAVING → SELECT → DISTINCT → ORDER BY → LIMIT |
| 5 | `COUNT(*)` vs `COUNT(col)` | `*` counts rows including NULLs; `col` counts non-NULL values only |
| 6 | `COUNT(1)` vs `COUNT(*)` | Identical — the optimiser treats them the same |
| 7 | Which aggregate never returns NULL? | `COUNT` — `SUM`/`AVG`/`MIN`/`MAX` return NULL on an empty set |
| 8 | Does `AVG` ignore NULLs? | Yes — it divides by the non-NULL count |
| 9 | INNER vs LEFT JOIN | Inner keeps only matches; left keeps every left row with NULLs where none |
| 10 | `ON` vs `WHERE` in an outer join | `ON` decides matching; `WHERE` filters after — a `WHERE` on the right table turns it into an inner join |
| 11 | Does `LEFT JOIN` return exactly the left row count? | **At least** — more if a left row matches several right rows |
| 12 | FULL OUTER JOIN in MySQL | Not supported — `LEFT … UNION … RIGHT` |
| 13 | Self join | A table joined to itself with two aliases — employee/manager |
| 14 | Cross join row count | left × right |
| 15 | Anti-join, 3 ways | `LEFT JOIN … IS NULL`, `NOT EXISTS`, `NOT IN` (⚠️ NULL-unsafe) |
| 16 | Why does `NOT IN` break? | A NULL in the list makes every comparison UNKNOWN → zero rows |
| 17 | `IN` vs `EXISTS` | `IN` for small lists; `EXISTS` short-circuits and is NULL-safe |
| 18 | Correlated subquery | References the outer query, so it re-evaluates per outer row |
| 19 | `UNION` vs `UNION ALL` | `UNION` deduplicates (implicit sort); `UNION ALL` doesn't and is faster |
| 20 | `UNION` vs `JOIN` | Union stacks rows; join combines columns |
| 21 | `ROW_NUMBER` vs `RANK` vs `DENSE_RANK` | 1,2,3 · 1,2,2,4 (gap) · 1,2,2,3 (no gap) |
| 22 | Which for Nth-highest salary? | `DENSE_RANK` — you're ranking salary levels, not people |
| 23 | Window function vs `GROUP BY` | `GROUP BY` collapses rows; a window function keeps them |
| 24 | Window function in `WHERE`? | ❌ — wrap it in a subquery/CTE and filter outside |
| 25 | Default window frame with `ORDER BY` | `RANGE UNBOUNDED PRECEDING → CURRENT ROW` — hence running totals |
| 26 | `LAG` / `LEAD` | Previous / next row's value — month-over-month deltas |
| 27 | `NTILE(4)` | Splits the window into 4 buckets — quartiles |
| 28 | CTE | A named `WITH` subquery — readable, reusable within the statement |
| 29 | Recursive CTE | `WITH RECURSIVE`: anchor + `UNION ALL` + recursive member — hierarchies |
| 30 | CTE vs temp table | CTE = one statement, no index; temp table = session-scoped, indexable |
| 31 | View | A stored `SELECT` with no data of its own |
| 32 | Materialized view | Stores results; needs refreshing. Not in MySQL |
| 33 | Can you update a view? | Only a simple single-table one — no aggregates or `GROUP BY` |
| 34 | Procedure vs function | Function returns a value and works inside `SELECT`; procedure is `CALL`ed |
| 35 | Trigger | Auto-fires on DML — good for audit, bad for hidden business logic |
| 36 | `DELETE` vs `TRUNCATE` | DML, `WHERE`, rollback, triggers vs DDL, all rows, fast, auto-commits |
| 37 | `TRUNCATE` vs `DROP` | Truncate empties, drop removes the table definition too |
| 38 | DDL / DML / DCL / TCL | CREATE-ALTER-DROP / SELECT-INSERT-UPDATE-DELETE / GRANT-REVOKE / COMMIT-ROLLBACK |
| 39 | ACID | Atomicity · Consistency · Isolation · Durability |
| 40 | Dirty read | Reading uncommitted data that then rolls back |
| 41 | Non-repeatable vs phantom | Same **row** changes value vs same **query** returns new rows |
| 42 | Default isolation level | MySQL `REPEATABLE READ`; Postgres/Oracle/SQL Server `READ COMMITTED` |
| 43 | Optimistic vs pessimistic locking | `@Version` conflict detection vs `SELECT … FOR UPDATE` |
| 44 | DB deadlock | Two transactions hold each other's locks; the engine kills one — fix by consistent lock ordering + retry |
| 45 | 1NF / 2NF / 3NF | Atomic values / no partial dependency on a composite key / no transitive dependency |
| 46 | When denormalise? | Read-heavy reporting — a deliberate, documented trade |
| 47 | Primary vs unique key | PK: one, `NOT NULL`, clustered in InnoDB. Unique: many, allows NULLs |
| 48 | Surrogate vs natural key | Auto-id/UUID vs a real-world value — natural keys change, so prefer surrogate |
| 49 | `ON DELETE CASCADE` | Deletes child rows — powerful, easy to regret |
| 50 | What is an index? | A B+ tree that lets you seek instead of scan — costs write speed and space |
| 51 | Clustered vs non-clustered | Clustered *is* the table's physical order (one); non-clustered points back to it |
| 52 | Leftmost-prefix rule | Index `(a,b)` helps `WHERE a` and `a AND b`, **not** `b` alone |
| 53 | Covering index | Contains every column the query needs → index-only scan |
| 54 | When is an index ignored? | Function on the column, leading `%` wildcard, implicit cast, low selectivity, stale stats |
| 55 | Sargable | A predicate that can use an index — `col >= x`, not `f(col) = x` |
| 56 | How do you diagnose a slow query? | `EXPLAIN ANALYZE` → look for `type: ALL`, bad row estimates, filesort |
| 57 | Why avoid `SELECT *`? | Extra I/O, defeats covering indexes, breaks when columns change |
| 58 | Why is `OFFSET 100000` slow? | It still walks all the skipped rows — use keyset pagination on an indexed column |
| 59 | Preventing SQL injection | Parameterised/prepared statements — never string concatenation; whitelist dynamic identifiers |
| 60 | N+1 problem | One query per parent row from the ORM — fix with `JOIN FETCH`, `@EntityGraph`, `@BatchSize` or a DTO projection |
| 61 | A has four `1`s, B has four `1`s — inner join rows? | **16** — every match pairs with every match: 4 × 4 |
| 62 | …and left, right, full, cross? | **All 16.** Nothing is unmatched, so the outer joins add nothing, and cross is also 4 × 4 |
| 63 | The cardinality formula | `INNER = Σ(countA(v) × countB(v))` over shared values; `LEFT = INNER +` unmatched A rows; `CROSS =` rowsA × rowsB |
| 64 | Do two NULL keys join to each other? | **No** — `NULL = NULL` is UNKNOWN. Each NULL row is unmatched on its own side |
| 65 | `SUM(salary)` after joining orders returns 3× too much — why? | The join fanned the employee row out once per order. Aggregate the child table in a subquery first |

---

# Part 23 — Drills (answers hidden)

> **Write the SQL on paper first.** Reading these does nothing; typing them is the whole point.

**D1.** For each department, show the name, the headcount and the highest salary — **including departments with nobody in them**.
<details><summary>Answer</summary>

```sql
SELECT d.dept_name, COUNT(e.emp_id) AS headcount, MAX(e.salary) AS top_salary
FROM   dept d
LEFT JOIN employee e ON e.dept_id = d.dept_id
GROUP BY d.dept_id, d.dept_name;
```
`MAX` returns NULL for an empty department — wrap in `COALESCE` if they want 0.
</details>

**D2.** Find the **3rd-highest** salary, handling ties, without using `LIMIT`.
<details><summary>Answer</summary>

```sql
SELECT DISTINCT salary FROM (
  SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) rnk FROM employee
) t WHERE rnk = 3;
-- or, no window functions:
SELECT DISTINCT e.salary FROM employee e
WHERE 2 = (SELECT COUNT(DISTINCT e2.salary) FROM employee e2 WHERE e2.salary > e.salary);
```
</details>

**D3.** List employees who earn **more than their manager**.
<details><summary>Answer</summary>

```sql
SELECT e.emp_name, e.salary, m.emp_name AS manager, m.salary AS mgr_salary
FROM employee e JOIN employee m ON m.emp_id = e.manager_id
WHERE e.salary > m.salary;
```
</details>

**D4.** For each employee show their salary and the **department average**, on the same row.
<details><summary>Answer</summary>

```sql
SELECT emp_name, salary, dept_id,
       AVG(salary) OVER (PARTITION BY dept_id) AS dept_avg
FROM employee;
```
A `GROUP BY` can't do this — it would collapse the rows. That's the point of the question.
</details>

**D5.** Find employees who have **never placed an order**.
<details><summary>Answer</summary>

```sql
SELECT e.* FROM employee e
WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.emp_id = e.emp_id);
```
`NOT IN` is the wrong answer here if `orders.emp_id` is nullable.
</details>

**D6.** Monthly revenue with the **previous month** and the **% change**.
<details><summary>Answer</summary>

```sql
WITH m AS (
  SELECT DATE_FORMAT(order_date,'%Y-%m') AS ym, SUM(amount) AS revenue
  FROM orders GROUP BY ym
)
SELECT ym, revenue,
       LAG(revenue) OVER (ORDER BY ym) AS prev,
       ROUND(100.0*(revenue - LAG(revenue) OVER (ORDER BY ym))
                  / NULLIF(LAG(revenue) OVER (ORDER BY ym),0), 2) AS pct
FROM m ORDER BY ym;
```
`NULLIF(...,0)` guards the divide-by-zero — say that out loud.
</details>

**D7.** Delete duplicate users by email, keeping the **most recently created**.
<details><summary>Answer</summary>

```sql
DELETE FROM users WHERE id IN (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY email ORDER BY created_at DESC) rn FROM users
  ) t WHERE rn > 1
);
```
Then `ALTER TABLE users ADD UNIQUE (email);` so it can't recur.
</details>

**D8.** Departments where **every** employee earns more than 50,000.
<details><summary>Answer</summary>

```sql
SELECT d.dept_name
FROM dept d JOIN employee e ON e.dept_id = d.dept_id
GROUP BY d.dept_id, d.dept_name
HAVING MIN(e.salary) > 50000;
```
The trick is turning "for all" into `MIN(...) > x`. The alternative is `NOT EXISTS (… salary <= 50000)`. ⚠️ Watch NULL salaries — `MIN` ignores them, so a department of NULLs would pass; add `COUNT(*) = COUNT(e.salary)` if that matters.
</details>

**D9.** Top **2 earners per department**, ties included.
<details><summary>Answer</summary>

```sql
SELECT * FROM (
  SELECT e.*, DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) rnk FROM employee e
) t WHERE rnk <= 2;
```
</details>

**D10.** The full reporting chain for each employee, with their level in the hierarchy.
<details><summary>Answer</summary>
Recursive CTE — see [Part 12.2](#122--recursive-cte--the-employee-hierarchy-question).
</details>

**D11.** Count orders by status **as columns**, one row per month.
<details><summary>Answer</summary>

```sql
SELECT DATE_FORMAT(order_date,'%Y-%m') AS ym,
       SUM(CASE WHEN status='PAID'      THEN 1 ELSE 0 END) AS paid,
       SUM(CASE WHEN status='PENDING'   THEN 1 ELSE 0 END) AS pending,
       SUM(CASE WHEN status='CANCELLED' THEN 1 ELSE 0 END) AS cancelled
FROM orders GROUP BY ym ORDER BY ym;
```
Conditional aggregation — the portable pivot.
</details>

**D12.** Employees hired in the **last 90 days**, in a way that uses an index on `hire_date`.
<details><summary>Answer</summary>

```sql
SELECT * FROM employee WHERE hire_date >= CURDATE() - INTERVAL 90 DAY;
```
**Not** `WHERE DATEDIFF(CURDATE(), hire_date) <= 90` — a function on the column is not sargable.
</details>

**D13.** This runs in 8 seconds. What do you do?
```sql
SELECT * FROM orders o JOIN employee e ON e.emp_id = o.emp_id
WHERE YEAR(o.order_date) = 2026 AND e.dept_id = 10;
```
<details><summary>Answer</summary>

1. `EXPLAIN ANALYZE` first — confirm it's a full scan before changing anything.
2. **`YEAR(o.order_date)` is not sargable** → `o.order_date >= '2026-01-01' AND o.order_date < '2027-01-01'`.
3. Index `orders(order_date, emp_id)` and `employee(dept_id, emp_id)`.
4. Drop `SELECT *` — name the columns, which may make an index covering.
5. Re-measure. *"And I'd check it isn't actually an N+1 from the ORM producing 8 seconds of small queries rather than one slow one."*
</details>

**D14.** Second-highest salary **per department**, ties collapsed.
<details><summary>Answer</summary>

```sql
SELECT dept_id, salary FROM (
  SELECT DISTINCT dept_id, salary,
         DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) rnk
  FROM employee WHERE salary IS NOT NULL
) t WHERE rnk = 2;
```
</details>

**D15.** Why might `SELECT COUNT(*) FROM employee WHERE dept_id != 10` return fewer rows than you expect?
<details><summary>Answer</summary>
NULL `dept_id` rows are excluded — `NULL != 10` is UNKNOWN, not TRUE. Add `OR dept_id IS NULL`. If you can explain this fluently you're ahead of most candidates.
</details>

**D16.** `A(id)` = `1,1,1` and `B(id)` = `1,1`. Give the row count for inner, left, right, full and cross — then change B to `1,1,4` and give them again.
<details><summary>Answer</summary>

**B = `1,1`:** inner `3×2` = **6**; left **6**; right **6**; full **6**; cross `3×2` = **6**. Everything matches, so all five are the same.

**B = `1,1,4`:** inner still **6** (the `4` matches nothing); left **6** (no unmatched A rows); right **7** (6 + the unmatched `4`); full **7**; cross `3×3` = **9**.
</details>

**D17.** For `A = 1,1,2,2` and `B = 1,1,3` with a `LEFT JOIN`, what do `COUNT(*)`, `COUNT(b.id)` and `COUNT(DISTINCT a.id)` return?
<details><summary>Answer</summary>
**6, 4, 2.** The two unmatched `2` rows produce placeholder rows with NULL `b.id` — counted by `COUNT(*)`, skipped by `COUNT(b.id)`. Distinct `a.id` values are 1 and 2.
</details>

**D18.** This report shows every employee's salary roughly tripled. What happened and how do you fix it?
```sql
SELECT e.emp_name, SUM(e.salary) AS salary, COUNT(o.order_id) AS orders
FROM employee e JOIN orders o ON o.emp_id = e.emp_id
GROUP BY e.emp_id, e.emp_name;
```
<details><summary>Answer</summary>
The join fans each employee out to one row per order, so `SUM(e.salary)` adds the same salary once per order. Fix by aggregating the child table first:

```sql
SELECT e.emp_name, e.salary, COALESCE(o.cnt, 0) AS orders
FROM employee e
LEFT JOIN (SELECT emp_id, COUNT(*) AS cnt FROM orders GROUP BY emp_id) o ON o.emp_id = e.emp_id;
```
Or `MAX(e.salary)` instead of `SUM`. ⚠️ It gets worse with **two** child tables — orders and payments multiply against each other.
</details>

---

# Part 24 — One-page cheat sheet

> **Read this at T-20 minutes. Nothing else.**

### 🎯 The five sentences that carry the whole topic

1. **Execution order** — *"FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY → LIMIT. That's why `WHERE` can't use an aggregate and `ORDER BY` can use an alias."*
2. **The `LEFT JOIN` pair** — *"`COUNT(*)` counts the placeholder row and returns 1 for an empty group; `COUNT(column)` skips NULLs and returns 0. And a `WHERE` on the right-hand table turns a left join back into an inner join — the condition belongs in the `ON`."*
3. **Ranking** — *"`ROW_NUMBER` 1,2,3 · `RANK` 1,2,2,4 · `DENSE_RANK` 1,2,2,3. For Nth-highest salary you want `DENSE_RANK`, because you're ranking salary levels."*
4. **Join cardinality** — *"Four 1s joined to four 1s is sixteen rows — and left, right, full and cross all give sixteen too, because nothing is unmatched. Outer joins only **add** rows for values with no partner on the other side."*
5. **NULL** — *"NULL means unknown, so `NULL = NULL` is UNKNOWN, and only `TRUE` rows survive a `WHERE`. That's why `NOT IN` with a NULL returns nothing and why `<> 10` drops NULL rows."*

### ⚡ Queries to be able to write cold

```sql
-- Aggregate per group, keeping empty groups
SELECT d.dept_name, COUNT(e.emp_id), COALESCE(ROUND(AVG(e.salary),2),0)
FROM dept d LEFT JOIN employee e ON e.dept_id = d.dept_id
GROUP BY d.dept_id, d.dept_name HAVING COUNT(e.emp_id) > 0;

-- Nth highest
SELECT DISTINCT salary FROM (SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) r FROM employee) t WHERE r = 2;

-- Top-N per group
SELECT * FROM (SELECT e.*, DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) r FROM employee e) t WHERE r <= 3;

-- Above own group's average
SELECT * FROM (SELECT e.*, AVG(salary) OVER (PARTITION BY dept_id) a FROM employee e) t WHERE salary > a;

-- Duplicates
SELECT email, COUNT(*) FROM users GROUP BY email HAVING COUNT(*) > 1;

-- Anti-join (rows with no match)
SELECT d.* FROM dept d WHERE NOT EXISTS (SELECT 1 FROM employee e WHERE e.dept_id = d.dept_id);

-- Self join with the manager
SELECT e.emp_name, COALESCE(m.emp_name,'—') FROM employee e LEFT JOIN employee m ON m.emp_id = e.manager_id;

-- Running total
SELECT order_date, amount, SUM(amount) OVER (ORDER BY order_date ROWS UNBOUNDED PRECEDING) FROM orders;
```

### 🗣️ Lines worth having word-perfect

- *"I'd use a `LEFT JOIN` here because a department with zero employees is still a valid answer."*
- *"`COUNT(*)` would count the empty placeholder row — I want `COUNT(e.emp_id)`."*
- *"`WHERE` filters rows before grouping; `HAVING` filters groups after aggregation."*
- *"I'd push that filter into `WHERE` rather than `HAVING` so it can use an index."*
- *"Never wrap the indexed column in a function — I'd rewrite it as a date range so it stays sargable."*
- *"Before I optimise anything I'd run `EXPLAIN ANALYZE` — measure, don't guess."*
- *"`NOT EXISTS` rather than `NOT IN`, because `NOT IN` breaks silently if the subquery returns a NULL."*
- *"Parameterised queries, not escaping — the value is sent separately from the statement so it can never be parsed as SQL."*

### 🚫 The five mistakes that cost marks

1. Writing `COUNT(*)` in a `LEFT JOIN` aggregate.
2. Putting the right-table filter in `WHERE` instead of `ON`.
3. `ROW_NUMBER` for "second-highest salary" (breaks on ties) — or `LIMIT 1 OFFSET 1` **without `DISTINCT`**.
   - …and answering "4" for `A LEFT JOIN B` when both sides hold four 1s. **It's 16.**
4. Forgetting non-aggregated columns in `GROUP BY`.
5. Going silent. **Say the shape first** — *"this is a group-by with a having, let me write it"* — then write it.

---

**Related files:** [30 — Mphasis L2 §G](./30-mphasis-level2-client-round.md) · [26 — Companies §8](./26-companies-asked-questions.md) · [24 — ORM/JPA & N+1](./24-orm-jpa-hibernate.md) · [13 — MongoDB](./13-mongodb.md) · [06 — Spring Boot](./06-spring-boot.md) · [22 — Java Streams](./22-java-streams-coding-problems.md) *(the same problems — group by department, second-highest salary — in Java)*
