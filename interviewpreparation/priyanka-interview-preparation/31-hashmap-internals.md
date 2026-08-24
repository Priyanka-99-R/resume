# 🔴 HashMap Internals — Buckets, Nodes, Linked Lists & Trees

**This is the file for the exact questions Virtusa Round 2 asked (Aug 2026):**
> *"How does HashMap internally work?"* · *"What is a bucket / bin?"* · *"What is the internal structure of a bucket?"* · *"Does a bucket contain a linked list?"*

You have hit HashMap internals in **three** rounds now (Altimetrik, Mphasis L1, Virtusa R2). It is the single most-asked Java question in your log. This file makes it unloseable.

> **The trap in this question:** most candidates say *"it uses hashing and stores in buckets"* and stop. The interviewer is waiting for **five** specific things: (1) the `Node[] table` array, (2) `hash()` spreading + `(n-1) & hash` indexing, (3) the bucket is a **linked list of `Node` objects**, (4) it **converts to a red-black tree at 8 nodes** *if the table is ≥ 64*, (5) **resize doubles capacity at load factor 0.75**. Say all five and the question is over.

---

## Table of contents

- [Part 0 — The 90-second answer (memorize this)](#part-0--the-90-second-answer-memorize-this)
- [Part 1 — The internal data structure](#part-1--the-internal-data-structure)
- [Part 2 — What exactly is a bucket?](#part-2--what-exactly-is-a-bucket)
- [Part 3 — put() step by step](#part-3--put-step-by-step)
- [Part 4 — get() step by step](#part-4--get-step-by-step)
- [Part 5 — Collisions: linked list → red-black tree](#part-5--collisions-linked-list--red-black-tree)
- [Part 6 — Load factor, threshold and resize](#part-6--load-factor-threshold-and-resize)
- [Part 7 — hashCode() and equals() contract](#part-7--hashcode-and-equals-contract)
- [Part 8 — Java 7 vs Java 8 (the infinite-loop story)](#part-8--java-7-vs-java-8-the-infinite-loop-story)
- [Part 9 — HashMap vs the other Maps](#part-9--hashmap-vs-the-other-maps)
- [Part 10 — Thread safety & ConcurrentHashMap](#part-10--thread-safety--concurrenthashmap)
- [Part 11 — Follow-up questions they ask next](#part-11--follow-up-questions-they-ask-next)
- [Part 12 — Predict the output](#part-12--predict-the-output)
- [Part 13 — Rapid-fire table](#part-13--rapid-fire-table)
- [Part 14 — Drills (cover the answers)](#part-14--drills-cover-the-answers)

---

# Part 0 — The 90-second answer (memorize this)

> *"A `HashMap` is an **array of buckets** — internally `Node<K,V>[] table`. On `put(key, value)` it calls `key.hashCode()`, then applies a spreading function `h ^ (h >>> 16)` so the high bits also influence the result, and finds the bucket index with `(n - 1) & hash`, where `n` is the table length — that works as a fast modulo because the capacity is always a power of two.*
>
> *Each bucket holds a **`Node<K,V>` object** — hash, key, value and a `next` pointer — so a bucket is really a **singly linked list of Nodes**. When two different keys land in the same bucket (a collision), the new node is appended to the end of that list. On lookup, Java walks the list comparing `hash` first and then `equals()`.*
>
> *Since **Java 8**, if one bucket grows to **8 nodes** and the table is at least **64** long, that bucket is converted from a linked list into a **red-black tree**, which drops worst-case lookup from O(n) to O(log n). If it shrinks back to **6** during a resize it turns back into a list.*
>
> *The map also grows: default capacity **16**, load factor **0.75**, so at **12** entries it **doubles** the table to 32 and redistributes the nodes. Average lookup is **O(1)**."*

**If they only let you say one sentence:** *"Array of buckets; each bucket is a linked list of Nodes that becomes a red-black tree after 8 collisions; it doubles in size at 75% full."*

---

# Part 1 — The internal data structure

### Q: What are the actual fields inside `java.util.HashMap`?

```java
public class HashMap<K,V> extends AbstractMap<K,V> implements Map<K,V>, Cloneable, Serializable {

    transient Node<K,V>[] table;   // THE BUCKET ARRAY — allocated lazily on first put()
    transient Set<Map.Entry<K,V>> entrySet;
    transient int size;            // number of key-value mappings (NOT table.length)
    transient int modCount;        // structural modification count → fail-fast iterators
    int threshold;                 // capacity * loadFactor → resize when size exceeds it
    final float loadFactor;        // default 0.75f
}
```

**The constants that interviewers ask for by number:**

| Constant | Value | Meaning |
|---|---|---|
| `DEFAULT_INITIAL_CAPACITY` | `1 << 4` = **16** | Table length created on first `put` |
| `MAXIMUM_CAPACITY` | `1 << 30` | Upper bound on table length |
| `DEFAULT_LOAD_FACTOR` | **0.75f** | Resize when `size > capacity * 0.75` |
| `TREEIFY_THRESHOLD` | **8** | List → red-black tree at this bin size |
| `UNTREEIFY_THRESHOLD` | **6** | Tree → list when it shrinks to this during resize |
| `MIN_TREEIFY_CAPACITY` | **64** | Table must be this long before treeifying; otherwise **resize instead** |

> ⚠️ **The most-missed detail in the whole topic:** 8 alone does **not** treeify. If `table.length < 64`, `treeifyBin()` calls `resize()` instead — because in a small table a long chain usually means *too few buckets*, not *bad hashCode*.

---

### Q: What is a `Node<K,V>`?

The unit of storage. One `Node` = one key–value mapping.

```java
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;   // cached spread hash — computed once, never recomputed
    final K key;      // final: the key reference can never change
    V value;          // mutable: put() on an existing key overwrites this
    Node<K,V> next;   // the linked-list pointer → THIS is what makes a bucket a list
}
```

**Three things to notice out loud in an interview:**

1. **`hash` is cached.** `hashCode()` is called once per key on insert. Resizing and lookups reuse the stored value — that's why a resize is cheap-ish and why an expensive `hashCode()` hurts less than people think.
2. **`key` is `final`.** You can never change which object a node points to as key — which is why *mutating* a key after insertion is a bug (Part 7).
3. **`next` exists on every node**, even when the bucket has one entry (it's just `null`). The linked list is not a separate object — it *is* the chain of `next` pointers.

---

### Q: What is a `TreeNode`?

The tree version of a node, used after treeification:

```java
static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {  // which extends HashMap.Node
    TreeNode<K,V> parent, left, right, prev;   // prev keeps the doubly-linked order
    boolean red;                               // red-black colour bit
}
```

> 💡 **Nice detail to drop:** a `TreeNode` still inherits `next` from `Node`, so a treeified bin is **simultaneously a red-black tree and a doubly-linked list**. That's how it can be untreeified back into a plain list cheaply during a resize.

---

# Part 2 — What exactly is a bucket?

### Q: What is a bucket / bin in a HashMap?

**A bucket is one slot of the internal array — `table[i]`.** It is not a class. It holds a reference to the **first node** of whatever lives at that index:

- `null` → empty bucket
- a `Node` → a **singly linked list** (1 or more entries chained by `next`)
- a `TreeNode` → the **root of a red-black tree** (after 8+ collisions in a ≥64 table)

```
table (Node<K,V>[16])

 index 0 │ null
 index 1 │ ──▶ Node{hash, "Ram", 25, next=null}
 index 2 │ null
 index 3 │ ──▶ Node{h,"Anu",30} ──▶ Node{h,"Kiran",41} ──▶ Node{h,"Zoe",19} ──▶ null
 index 4 │ null                     ↑ collision chain — SAME bucket, different keys
   ...   │
 index 9 │ ──▶ TreeNode root (red-black tree of 9+ entries)
                    ╱        ╲
              TreeNode      TreeNode
```

### Q: So does a bucket contain a linked list? (their exact words)

> *"Yes — by default a bucket is a **singly linked list of `Node` objects**, chained through the `next` field. It's not a `java.util.LinkedList`; it's a hand-rolled chain inside `HashMap`, which avoids an extra object per bucket.*
>
> *But **not always a list** — since Java 8, once a single bucket holds **8** nodes and the table is at least **64** long, that bucket is converted into a **red-black tree** so that a pathological bucket degrades to **O(log n)** instead of **O(n)**. So the precise answer is: **a bucket is either null, a linked list, or a red-black tree.**"*

> ⚠️ **Trap they follow with:** *"Is it a `LinkedList`?"* — **No.** Saying "it stores a `java.util.LinkedList` in each bucket" is a wrong answer that sounds right. It's a chain of `HashMap.Node` objects.

### Q: Why chaining and not open addressing / linear probing?

Java chose **separate chaining**: collisions go into a structure hanging off the bucket. Trade-off answer:

| | Chaining (HashMap) | Open addressing (probe next slot) |
|---|---|---|
| Deletion | Simple — unlink the node | Needs tombstones |
| Load factor > 1 | Still works, just slower | Breaks down badly near 1.0 |
| Memory | One `Node` object per entry | No per-entry object |
| Cache locality | Worse (pointer chasing) | Better |

> `ThreadLocalMap` inside the JDK *does* use linear probing — a good "yes I've read the JDK" remark if the conversation goes there.

---

# Part 3 — put() step by step

### Q: Walk me through `map.put("Ram", 25)`.

**Step 1 — hash the key (spread it).**

```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

- `null` key → hash **0** → always **bucket 0**. (HashMap allows exactly **one** null key.)
- Otherwise `hashCode()` XOR'd with its own upper 16 bits.

**Why the XOR?** Because the index is computed with `(n - 1) & hash`, and for a small table only the **low bits** survive the mask. Two keys differing only in high bits would collide constantly. XOR-ing the high half down mixes those bits in cheaply. This is called **"spreading"** or **perturbation**.

```
hashCode : 1111 1111 1111 1111 0000 0000 0000 0000
h >>> 16 : 0000 0000 0000 0000 1111 1111 1111 1111
XOR      : 1111 1111 1111 1111 1111 1111 1111 1111   ← high bits now affect the index
```

**Step 2 — find the bucket index.**

```java
i = (n - 1) & hash;     // n = table.length, ALWAYS a power of two
```

`&` with `n-1` is equivalent to `hash % n` when `n` is a power of two, but a bitwise AND is far faster than a division. **That is the entire reason capacity is always a power of two.**

> If you pass `new HashMap<>(20)`, Java does **not** use 20 — `tableSizeFor(20)` rounds it **up to the next power of two = 32**.

**Step 3 — place the node.**

```
Is table null or empty?           → resize() to create the 16-slot array (LAZY INIT)
Is table[i] null?                 → table[i] = new Node(hash, key, value, null)   ✅ done
Else there's a collision:
   ├─ first node has same hash AND (key == k || key.equals(k))?  → overwrite value  ✅ done
   ├─ first node is a TreeNode?   → putTreeVal() into the red-black tree
   └─ else walk the linked list:
         ├─ matching key found    → overwrite that node's value
         └─ reached the tail      → append new Node (TAIL insertion since Java 8)
               └─ if the bin now has ≥ 8 nodes → treeifyBin()
                      └─ but if table.length < 64 → resize() instead
Finally: if (++size > threshold) resize();
```

**Step 4 — the return value.** `put()` returns the **old value** if the key existed, else `null`. That's why `map.put(k, v) == null` is a common "was it new?" idiom — and why it's ambiguous when the map legitimately stores `null` values (use `containsKey`).

---

# Part 4 — get() step by step

### Q: How does `get()` find the value?

```java
final Node<K,V> getNode(int hash, Object key) {
    // 1. compute the same spread hash
    // 2. index = (n - 1) & hash
    // 3. always check the FIRST node in the bucket (the common case, no loop)
    // 4. if not it, and next != null:
    //       TreeNode?  → getTreeNode(hash, key)     O(log n)
    //       else       → walk the linked list        O(k)
}
```

**The comparison inside the loop is the part interviewers probe:**

```java
if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
```

Three checks in a deliberate order:

1. **`e.hash == hash`** — an `int` compare, extremely cheap; rejects almost all non-matches instantly.
2. **`k == key`** — reference equality; free hit for interned strings and cached Integers.
3. **`key.equals(k)`** — only now the potentially expensive user code runs.

> 💡 **Say this:** *"`hashCode` narrows it down to a bucket, `equals` decides the actual match — hashCode alone can never confirm equality, because two unequal objects are allowed to share a hash."*

**Complexity:**

| Case | Structure | Lookup |
|---|---|---|
| Good hash distribution | bucket has 0–1 nodes | **O(1)** |
| Heavy collisions, pre-Java 8 / small table | linked list | **O(n)** |
| Heavy collisions, Java 8+, table ≥ 64 | red-black tree | **O(log n)** |

---

# Part 5 — Collisions: linked list → red-black tree

### Q: What is a hash collision and how does HashMap handle it?

**A collision is two different keys mapping to the same bucket index.** It happens for two reasons: genuinely equal `hashCode`s, or different hash codes that collapse to the same index after `(n-1) & hash`.

Handling: the second key is **appended to the bucket's linked list**. Both entries live in the same bucket; `equals()` tells them apart on lookup.

```java
// Demonstration: force a collision deliberately
class BadKey {
    private final String name;
    BadKey(String name) { this.name = name; }
    @Override public int hashCode() { return 1; }              // 💣 everything → bucket 1
    @Override public boolean equals(Object o) {
        return o instanceof BadKey b && b.name.equals(name);
    }
}
// 10 BadKeys → all land in ONE bucket → a 10-node chain → treeified once table ≥ 64
```

### Q: Why was treeification added in Java 8?

**Two reasons — give both:**

1. **Performance under bad hash functions.** A degenerate bucket turned `get()` into a linear scan. With 1,000 colliding keys that's 1,000 comparisons per lookup.
2. **Security — hash-collision DoS.** Attackers could craft thousands of colliding `String` keys and POST them as form/JSON parameters, making a server's map operations quadratic and pinning the CPU. Treeification bounds the damage to O(log n).

### Q: Exactly when does a bin become a tree, and when does it revert?

```
Insert makes a bin reach 8 nodes
        │
        ├── table.length < 64 ?  ──▶ resize() (double the table) and re-split the bin
        │                            (a bigger table usually breaks the chain up naturally)
        │
        └── table.length ≥ 64 ?  ──▶ treeifyBin(): convert the chain to a red-black tree

During a later resize, if a tree bin splits to ≤ 6 nodes ──▶ untreeify back to a linked list
```

**Why 8 and 6 and not 8 and 7?** Two separate reasons, and both score:

- **Why 8:** the JDK source cites a Poisson distribution — with a good `hashCode` and load factor 0.75, the chance of a bucket reaching 8 entries is about **0.00000006**. So trees are meant to be *never* used in healthy maps; they're an insurance policy, and `TreeNode`s are ~2× the size of `Node`s, so you don't want them casually.
- **Why 6, not 7:** the **gap prevents thrashing**. If both thresholds were 8, a bin sitting exactly at the boundary would convert back and forth on every add/remove. The hysteresis gap makes flapping impossible.

### Q: How does a red-black tree order keys that aren't `Comparable`?

Good question to be ready for, because it exposes real reading:

1. Order primarily by **hash value**.
2. If hashes tie and the key implements `Comparable` **and both keys are the same class**, use `compareTo`.
3. Otherwise fall back to `tieBreakOrder()`, which compares class names and then `System.identityHashCode()` — an arbitrary but **consistent** total order, purely to keep the tree balanced.

> So: making your key `Comparable` makes treeified bins behave better, but it is **not required**.

---

# Part 6 — Load factor, threshold and resize

### Q: What is the load factor and why 0.75?

**Load factor = how full the table may get before it grows.** `threshold = capacity × loadFactor`.

Default 16 × 0.75 = **12** → the **13th** entry triggers a resize to capacity 32 (threshold 24).

**It's a space/time trade-off — say it as one:**

| Load factor | Effect |
|---|---|
| Low (e.g. 0.5) | Fewer collisions, faster lookups, **more wasted memory**, more frequent resizes |
| **0.75** | Empirically the sweet spot — good collision behaviour with acceptable memory |
| High (e.g. 1.0) | Dense table, less memory, **longer chains → slower gets** |

### Q: What happens during a resize?

1. New table of **double** the capacity is allocated; threshold doubles too.
2. Every existing node is redistributed into the new table.
3. **The clever Java 8 part:** because capacity doubles, a node's new index is either **the same index** or **index + oldCapacity** — decided by a single bit test:

```java
if ((e.hash & oldCap) == 0)  → stays at index j        (the "lo" list)
else                         → moves to index j + oldCap  (the "hi" list)
```

So Java 8 **never recomputes the hash or the modulo** during resize. It splits each bucket into two lists and attaches them — and it preserves relative order, which matters (Part 8).

```
oldCap = 16, bucket 3 holds A,B,C,D
             │
   split by (hash & 16)
             ├── lo list → new table[3]
             └── hi list → new table[19]   (3 + 16)
```

### Q: Why is resizing expensive, and how do you avoid it?

Resizing is **O(n)** and it happens repeatedly while a map grows: 16 → 32 → 64 → 128… Inserting 1M entries into a default map means ~17 rehash passes.

**Fix — size it up front:**

```java
// You know you'll hold ~1000 entries:
Map<String,User> m = new HashMap<>(1365);   // 1000 / 0.75 ≈ 1334 → tableSizeFor → 2048

// Java 19+ has the ergonomic version — no arithmetic, no mistakes:
Map<String,User> m2 = HashMap.newHashMap(1000);
```

> ⚠️ **Trap:** `new HashMap<>(1000)` does **not** mean "holds 1000 without resizing". It means capacity 1024 → threshold 768 → it resizes at the 769th entry. Always divide by the load factor first.

> 💡 **Real-project line:** *"In the Subsea schedule loader we were building a map of ~40k parts inside a loop and it was resizing all the way up from 16. Pre-sizing the map removed a visible chunk of the load time — it's a one-line fix that people never make."*

---

# Part 7 — hashCode() and equals() contract

### Q: What is the contract?

1. If `a.equals(b)` is true → `a.hashCode() == b.hashCode()` **must** be true.
2. If hash codes are equal → objects **may or may not** be equal (collision is legal).
3. `hashCode()` must be **consistent** — same object, same value, as long as the fields used in `equals` don't change.

### Q: What breaks if I override `equals()` but not `hashCode()`?

**The classic.** Two "equal" objects get different (identity) hash codes → they land in **different buckets** → the map never finds the entry.

```java
class Employee {
    int id;
    Employee(int id) { this.id = id; }
    @Override public boolean equals(Object o) {
        return o instanceof Employee e && e.id == id;
    }
    // ❌ hashCode() NOT overridden
}

Map<Employee,String> map = new HashMap<>();
map.put(new Employee(1), "Priyanka");
System.out.println(map.get(new Employee(1)));   // null  ← equal object, wrong bucket
System.out.println(map.size());                 // 1
map.put(new Employee(1), "Again");
System.out.println(map.size());                 // 2  ← DUPLICATE "equal" keys in one map
```

**The reverse** (override `hashCode` only) doesn't lose data — both land in the same bucket, but `equals` says they're different, so you get two entries. Slower, still "wrong".

### Q: What happens if a key is mutated after being put in the map?

**You orphan the entry.** The node keeps the **old cached hash**, so the map still looks in the old bucket, but `equals()` now fails there — and the object's new hash points elsewhere.

```java
List<String> key = new ArrayList<>(List.of("a"));
Map<List<String>,String> map = new HashMap<>();
map.put(key, "v");
key.add("b");                       // 💣 hashCode changed
System.out.println(map.get(key));   // null — unreachable, but still occupying memory
System.out.println(map.size());     // 1  (a slow memory leak in long-lived maps)
```

**Rule to state:** *"Map keys must be immutable — or at least, the fields used in `equals`/`hashCode` must never change. That's why `String`, `Integer` and records make ideal keys."*

### Q: How should you write `hashCode()` in real code?

```java
public record EmployeeKey(int id, String dept) { }   // record: equals + hashCode generated ✅

// or classic:
@Override public int hashCode() { return Objects.hash(id, dept); }
@Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Employee e)) return false;
    return id == e.id && Objects.equals(dept, e.dept);
}
```

> ⚠️ **Lombok trap they love:** `@Data` on a JPA entity generates `equals`/`hashCode` over **all** fields including the DB-generated `id` and lazy collections — the hash changes after `save()` flushes an id, and touching a lazy field can trigger a query or `LazyInitializationException`. See **[24 — ORM/JPA](./24-orm-jpa-hibernate.md)**.

---

# Part 8 — Java 7 vs Java 8 (the infinite-loop story)

### Q: What changed in HashMap in Java 8?

| | Java 7 | Java 8+ |
|---|---|---|
| Bucket structure | Linked list only | Linked list **→ red-black tree** at 8 |
| Insert position | **Head** insertion | **Tail** insertion |
| Resize transfer | Recomputes index, **reverses** list order | Splits into lo/hi lists, **preserves order** |
| Hash function | 4 shifts + 4 XORs | Single `h ^ (h >>> 16)` |
| Entry class | `Entry<K,V>` | `Node<K,V>` (+ `TreeNode`) |
| Worst-case get | O(n) | O(log n) |

### Q: What was the famous Java 7 HashMap infinite loop?

**Worth telling as a story — it lands well.**

In Java 7, resizing used head insertion, which **reversed** each chain. If two threads resized the same map concurrently, one thread could suspend mid-transfer while the other completed the reversal — leaving two nodes pointing at each other (`A.next = B` and `B.next = A`). A later `get()` would walk that cycle **forever**: a 100% CPU-pinned thread with no exception, no error, nothing in the logs. Teams found it only in thread dumps, stuck in `HashMap.get`.

Java 8's lo/hi split **preserves order** and never reverses, so this specific infinite loop is gone.

> ⚠️ **Do not conclude "so Java 8 HashMap is thread-safe."** It is still **not** thread-safe — you can still lose updates, see a stale `size`, or get a `ConcurrentModificationException`. Java 8 removed one specific catastrophic failure, not the need for synchronization. Interviewers *actively fish* for the wrong conclusion here.

---

# Part 9 — HashMap vs the other Maps

| | **HashMap** | **Hashtable** | **LinkedHashMap** | **TreeMap** | **ConcurrentHashMap** |
|---|---|---|---|---|---|
| Ordering | None (bucket order) | None | **Insertion** (or access) order | **Sorted** by key | None |
| Structure | Node array + list/tree | Node array + list | HashMap + doubly-linked list | Red-black tree | Node array + list/tree |
| Thread-safe | ❌ | ✅ (method-level `synchronized`) | ❌ | ❌ | ✅ (CAS + per-bin lock) |
| null key | 1 allowed | ❌ NPE | 1 allowed | ❌ NPE (needs compare) | ❌ NPE |
| null values | ✅ | ❌ | ✅ | ✅ | ❌ |
| get/put | O(1) | O(1) | O(1) | **O(log n)** | O(1) |
| Since | 1.2 | 1.0 (legacy) | 1.4 | 1.2 | 1.5 |

**Say this about Hashtable:** *"Legacy — it locks the whole object on every method, so it doesn't scale. Nobody should choose it in new code; `ConcurrentHashMap` replaced it."*

**LinkedHashMap bonus — an LRU cache in 5 lines** (a classic follow-up):

```java
class LruCache<K,V> extends LinkedHashMap<K,V> {
    private final int cap;
    LruCache(int cap) { super(cap, 0.75f, true); this.cap = cap; }  // true = ACCESS order
    @Override protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > cap;
    }
}
```

**HashSet:** *"`HashSet` is literally a `HashMap` — it holds one internally and stores every element as a key with a shared dummy `PRESENT` object as the value. That's why `HashSet.add` returns a boolean: it's `map.put(e, PRESENT) == null`."*

---

# Part 10 — Thread safety & ConcurrentHashMap

### Q: Is HashMap thread-safe? What are the options?

**No.** Concurrent `put`s can lose entries, corrupt a chain, or leave `size` wrong. Options:

| Option | How it locks | Verdict |
|---|---|---|
| `Hashtable` | `synchronized` on every method | Legacy, one global lock |
| `Collections.synchronizedMap(map)` | Wrapper, one lock for the whole map | Works, but iteration must be **manually** synchronized |
| **`ConcurrentHashMap`** | CAS for empty bins + `synchronized` on the **bin head** for the rest | ✅ The right answer |

### Q: How does ConcurrentHashMap work internally (Java 8)?

- **Same layout as HashMap** — `Node[] table`, lists that treeify at 8.
- **No more segments.** Java 7 used ~16 `Segment` locks (`ReentrantLock`); Java 8 **removed** them.
- Empty bucket → insert with a **CAS**, no lock at all.
- Non-empty bucket → `synchronized (firstNode)` — so **lock granularity is one bucket**. Two threads writing to different buckets never contend.
- Resize is **cooperative**: threads that find a resize in progress help transfer bins (`ForwardingNode` marks a moved bin).
- `size()` uses a striped `LongAdder`-style counter (`baseCount` + `CounterCell[]`), not a single contended field.
- **Iterators are weakly consistent**, not fail-fast — they never throw `ConcurrentModificationException` and may or may not reflect concurrent updates.
- **No nulls** for keys or values — because `map.get(k) == null` would be ambiguous between "absent" and "mapped to null" in a concurrent setting where `containsKey` can't be atomic with `get`.

> ⚠️ **Trap:** *"Is `ConcurrentHashMap` fully atomic?"* — individual operations are, but **compound** ones aren't. `if (!map.containsKey(k)) map.put(k, v);` is a race. Use the atomic ones: `putIfAbsent`, `computeIfAbsent`, `compute`, `merge`.

```java
// ❌ race                                    // ✅ atomic
if (!map.containsKey(k)) map.put(k, v);       map.putIfAbsent(k, v);
map.put(k, map.get(k) + 1);                   map.merge(k, 1, Integer::sum);
```

### Q: What is a fail-fast iterator?

`HashMap` iterators record `modCount` at creation and compare it on every `next()`. If the map was structurally modified by anything other than the iterator's own `remove()`, they throw **`ConcurrentModificationException`**.

```java
for (String k : map.keySet()) {
    if (k.startsWith("tmp")) map.remove(k);      // 💥 CME
}

// ✅ iterator.remove()
Iterator<String> it = map.keySet().iterator();
while (it.hasNext()) { if (it.next().startsWith("tmp")) it.remove(); }

// ✅ or, simplest
map.keySet().removeIf(k -> k.startsWith("tmp"));
```

> **Say the caveat:** *"Fail-fast is best-effort — it's a bug-detection aid, not a guarantee. You cannot rely on the exception being thrown."*

---

# Part 11 — Follow-up questions they ask next

### Q: Why is HashMap capacity always a power of two?

So that `(n - 1) & hash` is a valid, fast substitute for `hash % n`. With `n = 16`, `n-1 = 1111₂` — a clean low-bit mask. If capacity were 15, the mask `1110₂` could never produce an odd index: **half the table would be unreachable.**

### Q: Can HashMap have a null key? How, if hashing null would NPE?

Yes, exactly one — `hash(null)` is **special-cased to return 0**, so a null key always occupies bucket 0. Null *values* are unrestricted. `TreeMap` and `ConcurrentHashMap` reject nulls.

### Q: What is the time complexity of HashMap operations?

| Op | Average | Worst (Java 8+) |
|---|---|---|
| `get` / `put` / `remove` / `containsKey` | **O(1)** | **O(log n)** (treeified) — O(n) if the table is < 64 |
| `containsValue` | **O(n)** — scans every bucket | O(n) |
| Iteration | O(n + capacity) — walks empty buckets too | same |

> 💡 **Nice depth:** *"Iteration cost depends on **capacity**, not just size. A map that once held a million entries and was then cleared still has a huge table — `clear()` doesn't shrink it. HashMap never shrinks; only a fresh map does."*

### Q: Why is `String` a good HashMap key?

Immutable (hash can't go stale), `hashCode` is **cached** in the `String` object after first computation, `equals` is cheap, and its hash distributes decently. Same reasoning makes records and enums good keys — for enums, `EnumMap` is even better (a plain array indexed by ordinal).

### Q: Is HashMap ordering guaranteed to be stable?

No — iteration order depends on hash values **and current capacity**, so it can change on resize. Never rely on it. Use `LinkedHashMap` for insertion order or `TreeMap` for sorted order.

---

# Part 12 — Predict the output

**Q1**
```java
Map<String,Integer> m = new HashMap<>();
m.put("a", 1); m.put("a", 2);
System.out.println(m.size() + " " + m.get("a"));
```
<details><summary>Answer</summary>

`1 2` — same key (equal hash + equals) → the existing node's **value** is overwritten; size unchanged.
</details>

**Q2**
```java
Map<Integer,String> m = new HashMap<>();
m.put(null, "x");
m.put(null, "y");
System.out.println(m.size() + " " + m.get(null));
```
<details><summary>Answer</summary>

`1 y` — one null key allowed, in bucket 0; the second put overwrites.
</details>

**Q3**
```java
class K {
    int id;
    K(int id) { this.id = id; }
    public boolean equals(Object o) { return o instanceof K k && k.id == id; }
    public int hashCode() { return id; }
}
Map<K,String> m = new HashMap<>();
K k = new K(1);
m.put(k, "one");
k.id = 99;
System.out.println(m.get(k) + " " + m.get(new K(1)) + " " + m.size());
```
<details><summary>Answer</summary>

`null null 1` — the node cached hash **1**, so the entry sits in bucket 1. `m.get(k)` hashes to bucket 99 (empty). `m.get(new K(1))` reaches bucket 1 but `equals` compares against the mutated object whose `id` is now 99 → false. The entry is **unreachable but still counted**. This is the mutable-key leak.
</details>

**Q4**
```java
Map<String,String> m = new HashMap<>();
m.put("k", null);
System.out.println(m.get("k"));
System.out.println(m.get("missing"));
System.out.println(m.containsKey("k"));
```
<details><summary>Answer</summary>

`null`, `null`, `true` — `get` returning null is ambiguous; only `containsKey` distinguishes "present with null value" from "absent". This is exactly why `ConcurrentHashMap` bans null values.
</details>

**Q5**
```java
Map<String,Integer> m = new HashMap<>(3);
System.out.println(m.size());
m.put("a",1);
```
<details><summary>Answer</summary>

`0`. Two points: the table is **not allocated in the constructor** — it's lazily created on the first `put`. And capacity 3 is rounded up by `tableSizeFor` to **4**, so `threshold` becomes 4 × 0.75 = 3.
</details>

**Q6**
```java
Set<Employee> s = new HashSet<>();   // Employee overrides equals() only
s.add(new Employee(1));
s.add(new Employee(1));
System.out.println(s.size());
```
<details><summary>Answer</summary>

`2` — different identity hash codes → different buckets → `equals` is never even consulted. The broken-contract classic.
</details>

**Q7**
```java
Map<String,Integer> m = new HashMap<>();
m.put("A", 1); m.put("B", 2); m.put("C", 3);
for (String k : m.keySet()) if (k.equals("B")) m.remove(k);
System.out.println(m);
```
<details><summary>Answer</summary>

Throws **`ConcurrentModificationException`**… *usually*. With exactly one removal it can sometimes finish silently if the removal happens on the second-to-last element (`hasNext()` returns false before the modCount check runs). That unpredictability is the point: fail-fast is best-effort. Use `removeIf`.
</details>

**Q8**
```java
Map<Integer,String> m = new HashMap<>();
for (int i = 1; i <= 5; i++) m.put(i, "v" + i);
System.out.println(m.keySet());
```
<details><summary>Answer</summary>

`[1, 2, 3, 4, 5]` — and this misleads people. Small `Integer` keys hash to themselves, so `(16-1) & i == i`: they land in buckets 1..5 in order. It is **not** an ordering guarantee — add key 20 and it appears at bucket 4, out of order.
</details>

---

# Part 13 — Rapid-fire table

| Question | One-line answer |
|---|---|
| Internal structure? | `Node<K,V>[] table` — array of buckets |
| What is a bucket? | One array slot: null, a linked list of Nodes, or a red-black tree |
| Fields of a Node? | `final int hash`, `final K key`, `V value`, `Node next` |
| Index formula? | `(n - 1) & hash` |
| Hash function? | `h ^ (h >>> 16)` — spreads high bits into low |
| Why power-of-two capacity? | Makes `&` a valid, fast modulo |
| Default capacity / load factor? | 16 / 0.75 → resizes at 13th entry |
| Treeify threshold? | 8 nodes **and** table length ≥ 64 |
| Untreeify threshold? | 6 |
| Why 8→tree, 6→list (not 7)? | Gap prevents convert/revert thrashing |
| Why tree at all? | O(log n) worst case + hash-collision DoS defence |
| Resize behaviour? | Doubles; splits each bin by `(hash & oldCap)` into lo/hi |
| Null key? | One, hash forced to 0, bucket 0 |
| Thread-safe? | No — use `ConcurrentHashMap` |
| CHM lock granularity? | CAS on empty bin, `synchronized` on the bin's **head node** |
| Java 7 vs 8 insertion? | Head → **tail** insertion |
| Java 7 infinite loop? | Head-insert resize reversed chains → cyclic list under concurrency |
| Iterator type? | Fail-fast (`modCount`); CHM is weakly consistent |
| get/put complexity? | O(1) average, O(log n) worst |
| HashSet internally? | A HashMap with a dummy `PRESENT` value |

---

# Part 14 — Drills (cover the answers)

1. Draw a 16-slot table and place keys with hashes 5, 21, 37. Which bucket, and why?
<details><summary>Answer</summary>All three → bucket **5**: `5 & 15 = 5`, `21 & 15 = 5`, `37 & 15 = 5`. A 3-node chain. After a resize to 32: `5 & 31 = 5`, `21 & 31 = 21`, `37 & 31 = 5` → the chain splits into bucket 5 (two nodes) and bucket 21.</details>

2. A map has 100,000 keys all returning `hashCode() == 7`. What is `get()` complexity in Java 7 and in Java 8?
<details><summary>Answer</summary>Java 7: **O(n)** — a 100k linked list. Java 8: **O(log n)** — the bin treeifies (table well over 64).</details>

3. You need a map for ~500 entries. What capacity do you pass?
<details><summary>Answer</summary>500 / 0.75 = 667 → next power of two is 1024, so `new HashMap<>(667)` (or just `HashMap.newHashMap(500)` on Java 19+). Passing 500 gives capacity 512, threshold 384 → it resizes.</details>

4. Why can't `ConcurrentHashMap` store null values?
<details><summary>Answer</summary>`get()` returning null would be ambiguous between "absent" and "mapped to null", and you can't disambiguate with `containsKey` atomically while other threads mutate the map. Doug Lea calls it a design error in `HashMap` that he refused to repeat.</details>

5. Two threads call `put` on the same `HashMap` at the same time. Name three things that can go wrong.
<details><summary>Answer</summary>(1) Lost update — both read `table[i]` as null and one overwrites the other. (2) `size` becomes wrong — `++size` isn't atomic. (3) Concurrent resize can drop entries or (in Java 7) create a cyclic chain that hangs a CPU forever.</details>

6. Is it safe to use a mutable object as a key if you never mutate it? Is it *good practice*?
<details><summary>Answer</summary>Safe in that instant, poor practice — nothing enforces the discipline and the failure is silent (entry becomes unreachable, no exception). Prefer immutable keys: `String`, `Integer`, enums, records.</details>

7. Explain to a junior why `hashCode` alone can never decide equality.
<details><summary>Answer</summary>A hash maps a huge key space onto 32 bits, so collisions are mathematically unavoidable (pigeonhole principle). The hash narrows the search to a bucket; `equals` decides. That's exactly the two-step lookup in `getNode`.</details>

---

## What to say when you don't know a detail

> *"I know the shape of it — array of buckets, chained Nodes, treeified after 8 collisions, resize at 0.75. I haven't memorised that specific constant, but I'd check `HashMap`'s source; the constants are all at the top of the class."*

That is a **strong** answer. Bluffing a wrong number is worse than that sentence.

---

**Related files:** [05 — Core Java](./05-java.md) · [23 — Predict the Output](./23-java-output-tricky-questions.md) · [32 — Multithreading](./32-multithreading.md) · [26 — Companies: Questions Actually Asked](./26-companies-asked-questions.md)
