# 🔴 HashMap Internals — Easy Version

**This is the file for the exact questions Virtusa Round 2 asked (Aug 2026):**
> *"How does HashMap internally work?"* · *"What is a bucket / bin?"* · *"What is the internal structure of a bucket?"* · *"Does a bucket contain a linked list?"*

You have hit HashMap internals in **three** rounds now (Altimetrik, Mphasis L1, Virtusa R2). It is the single most-asked Java question in your log.

> **The trap in this question:** most candidates say *"it uses hashing and stores in buckets"* and stop. The interviewer is waiting for **five** specific things:
> 1. the `Node[] table` array
> 2. `hash()` spreading + `(n-1) & hash` indexing
> 3. a bucket is a **linked list of `Node` objects**
> 4. it becomes a **red-black tree at 8 nodes** *if the table is ≥ 64*
> 5. **resize doubles capacity at load factor 0.75**
>
> Say all five and the question is over.

---

## Table of contents

- [Part 0 — The 90-second answer (memorise this)](#part-0--the-90-second-answer-memorise-this)
- [Part 1 — The picture that explains everything](#part-1--the-picture-that-explains-everything)
- [Part 2 — The internal data structure](#part-2--the-internal-data-structure)
- [Part 3 — What exactly is a bucket?](#part-3--what-exactly-is-a-bucket)
- [Part 4 — put() step by step](#part-4--put-step-by-step)
- [Part 5 — get() step by step](#part-5--get-step-by-step)
- [Part 6 — Collisions: linked list → red-black tree](#part-6--collisions-linked-list--red-black-tree)
- [Part 7 — Load factor, threshold and resize](#part-7--load-factor-threshold-and-resize)
- [Part 8 — hashCode() and equals() contract](#part-8--hashcode-and-equals-contract)
- [Part 9 — Java 7 vs Java 8 (the infinite-loop story)](#part-9--java-7-vs-java-8-the-infinite-loop-story)
- [Part 10 — HashMap vs the other Maps](#part-10--hashmap-vs-the-other-maps)
- [Part 11 — Thread safety & ConcurrentHashMap](#part-11--thread-safety--concurrenthashmap)
- [Part 12 — Follow-up questions they ask next](#part-12--follow-up-questions-they-ask-next)
- [Part 13 — Predict the output](#part-13--predict-the-output)
- [Part 14 — Rapid-fire table](#part-14--rapid-fire-table)
- [Part 15 — Drills](#part-15--drills)

---

# Part 0 — The 90-second answer (memorise this)

> *"A `HashMap` is an **array of buckets** — internally `Node<K,V>[] table`. On `put(key, value)` it calls `key.hashCode()`, then applies a spreading function `h ^ (h >>> 16)` so the high bits also influence the result, and finds the bucket index with `(n - 1) & hash`, where `n` is the table length — that works as a fast modulo because the capacity is always a power of two.*
>
> *Each bucket holds a **`Node<K,V>` object** — hash, key, value and a `next` pointer — so a bucket is really a **singly linked list of Nodes**. When two different keys land in the same bucket (a collision), the new node is appended to the end of that list. On lookup, Java walks the list comparing `hash` first and then `equals()`.*
>
> *Since **Java 8**, if one bucket grows to **8 nodes** and the table is at least **64** long, that bucket is converted from a linked list into a **red-black tree**, which drops worst-case lookup from O(n) to O(log n). If it shrinks back to **6** during a resize it turns back into a list.*
>
> *The map also grows: default capacity **16**, load factor **0.75**, so at **12** entries it **doubles** the table to 32 and redistributes the nodes. Average lookup is **O(1)**."*

**If they only let you say one sentence:**

> *"Array of buckets; each bucket is a linked list of Nodes that becomes a red-black tree after 8 collisions; it doubles in size at 75% full."*

---

# Part 1 — The picture that explains everything

The easiest way to remember the whole topic:

```
hashCode() → WHERE to look   (which bucket)
equals()   → WHICH one it is (which entry in that bucket)
```

Real-world idea: a **postal address**.

```
PIN code    → tells the postman which AREA      → hashCode()
House name  → identifies the exact HOUSE there  → equals()
```

Many houses share a PIN code — that is a **collision**, and it is completely normal. The postman goes to the right area, then reads the name boards one by one.

Second real-world idea, for the structure itself: a **library**.

```
Subject shelf → hashCode() picks the shelf
Book title    → equals() finds the exact book on that shelf

One shelf with 3 books   → a small linked list, walk it
One shelf with 500 books → sort them into a tree so you can binary-search ⭐
```

And the picture of the actual memory:

```
table (Node<K,V>[16])

 index 0 │ null
 index 1 │ ──▶ Node{hash, "Ram", 25, next=null}
 index 2 │ null
 index 3 │ ──▶ Node{"Anu",30} ──▶ Node{"Kiran",41} ──▶ Node{"Zoe",19} ──▶ null
 index 4 │ null                  ↑ collision chain — SAME bucket, different keys
   ...   │
 index 9 │ ──▶ TreeNode root (red-black tree of 9+ entries)
                    ╱        ╲
              TreeNode      TreeNode
 index 15│ null
```

Keep this diagram in your head. Every question below is just a detail of this picture.

---

# Part 2 — The internal data structure

### Q: What are the actual fields inside `java.util.HashMap`?

```java
public class HashMap<K,V> extends AbstractMap<K,V> implements Map<K,V>, Cloneable, Serializable {

    transient Node<K,V>[] table;   // THE BUCKET ARRAY — created lazily on the first put()
    transient Set<Map.Entry<K,V>> entrySet;
    transient int size;            // number of key-value mappings (NOT table.length)
    transient int modCount;        // structural modification count → fail-fast iterators
    int threshold;                 // capacity × loadFactor → resize when size exceeds it
    final float loadFactor;        // default 0.75f
}
```

⭐ Notice `size` and `table.length` are **different things**. `size` is how many entries you stored; `table.length` is how many buckets exist. Interviewers ask this to check you're not guessing.

#### The constants they ask for by number

| Constant | Value | Meaning |
|---|---|---|
| `DEFAULT_INITIAL_CAPACITY` | `1 << 4` = **16** | table length created on the first `put` |
| `MAXIMUM_CAPACITY` | `1 << 30` | upper bound on table length |
| `DEFAULT_LOAD_FACTOR` | **0.75f** | resize when `size > capacity × 0.75` |
| `TREEIFY_THRESHOLD` | **8** | list → red-black tree at this bin size |
| `UNTREEIFY_THRESHOLD` | **6** | tree → list when it shrinks to this during a resize |
| `MIN_TREEIFY_CAPACITY` | **64** | the table must be this long before treeifying; otherwise **resize instead** |

> ⚠️ **The most-missed detail in the whole topic:** 8 alone does **not** treeify.
>
> ```
> bucket reaches 8 nodes
>         │
>    table.length < 64 ?  → resize() instead  ⭐
>    table.length ≥ 64 ?  → treeify
> ```
>
> Why? In a small table, a long chain usually means **too few buckets**, not a bad `hashCode`. Growing the table is the cheaper cure.

---

### Q: What is a `Node<K,V>`?

The unit of storage. **One Node = one key-value mapping.**

```java
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;   // cached spread hash — computed ONCE, never recomputed
    final K key;      // final: the key reference can never change
    V value;          // mutable: put() on an existing key overwrites this
    Node<K,V> next;   // the linked-list pointer → THIS is what makes a bucket a list
}
```

Picture one node:

```
┌──────────────────────────────┐
│ hash  = 96727                │  ← computed once and cached ⭐
│ key   = "Ram"      (final)   │  ← reference can never change
│ value = 25         (mutable) │  ← put() overwrites this
│ next  = ──────────────────────▶ the next Node in this bucket (or null)
└──────────────────────────────┘
```

**Three things to say out loud in the interview:**

```
1. `hash` is CACHED
      → hashCode() is called once per key, on insert
      → resizing and lookups reuse the stored value
      → that's why a resize is cheap-ish, and why an expensive hashCode()
        hurts less than people assume

2. `key` is FINAL
      → you can never change WHICH object a node points to as its key
      → which is exactly why MUTATING a key after insertion is a bug (Part 8)

3. `next` exists on EVERY node, even when the bucket has one entry (it's just null)
      → the linked list is not a separate object — it IS the chain of `next` pointers
```

---

### Q: What is a `TreeNode`?

The tree version of a node, used after treeification:

```java
static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {  // which extends HashMap.Node
    TreeNode<K,V> parent, left, right, prev;   // prev keeps the doubly-linked order
    boolean red;                               // the red-black colour bit
}
```

> 💡 **Nice detail to drop:** a `TreeNode` still inherits `next` from `Node`, so a treeified bin is **simultaneously a red-black tree AND a doubly-linked list**. That is how it can be untreeified back into a plain list cheaply during a resize — the list is still right there.

---

# Part 3 — What exactly is a bucket?

### Q: What is a bucket / bin in a HashMap?

The easiest way to remember:

```
A bucket = ONE SLOT of the internal array → table[i]

It is NOT a class. It is just an array position.
```

That slot holds a reference to the **first node** of whatever lives there:

```
table[i] = null      → empty bucket
table[i] = Node      → a SINGLY LINKED LIST (1 or more entries chained by `next`)
table[i] = TreeNode  → the ROOT of a red-black tree (after 8+ collisions, table ≥ 64)
```

```
        ┌─────────┐
table[3]│  Node   │──▶│  Node   │──▶│  Node   │──▶ null
        │ "Anu"   │   │ "Kiran" │   │ "Zoe"   │
        └─────────┘   └─────────┘   └─────────┘
             └──────── ONE bucket ────────┘
```

Real-world idea: a **shelf in a shoe rack**. The shelf itself is just a space. What's on it might be nothing, a row of boxes, or a neatly sorted arrangement.

---

### Q: So does a bucket contain a linked list? (their exact words)

> *"Yes — by default a bucket is a **singly linked list of `Node` objects**, chained through the `next` field. It's not a `java.util.LinkedList`; it's a hand-rolled chain inside `HashMap`, which avoids an extra object per bucket.*
>
> *But it is **not always a list** — since Java 8, once a single bucket holds **8** nodes and the table is at least **64** long, that bucket is converted into a **red-black tree**, so a pathological bucket degrades to **O(log n)** instead of **O(n)**. So the precise answer is: **a bucket is either null, a linked list, or a red-black tree.**"*

> ⚠️ **Trap they follow with:** *"Is it a `LinkedList`?"*
>
> **No.** Saying "it stores a `java.util.LinkedList` in each bucket" is a wrong answer that *sounds* right. It is a chain of `HashMap.Node` objects, with the `next` pointer built into the node itself.

```
java.util.LinkedList  → a separate object, with its own header and Node class ❌
HashMap bucket        → the entries chain THEMSELVES via `next`               ✅
```

---

### Q: Why chaining and not open addressing / linear probing?

Java chose **separate chaining**: collisions go into a structure hanging off the bucket. The alternative, open addressing, would put the colliding entry in the *next free slot* of the array.

| | Chaining (HashMap) | Open addressing (probe the next slot) |
|---|---|---|
| Deletion | simple — unlink the node | needs "tombstone" markers |
| Load factor > 1 | still works, just slower | breaks down badly near 1.0 |
| Memory | one `Node` object per entry | no per-entry object |
| Cache locality | worse (pointer chasing) | better |

Real-world idea:

```
Chaining        → two families share a PIN code; both keep their own house
Open addressing → the second family is told "go live in the next empty house"
                  → now deleting the first family confuses every future search
```

> 💡 `ThreadLocalMap` inside the JDK *does* use linear probing — a good "yes, I've read the JDK" remark if the conversation goes there.

---

# Part 4 — put() step by step

### Q: Walk me through `map.put("Ram", 25)`.

Four steps. Learn them in order.

#### Step 1 — hash the key (spread it)

```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

```
null key → hash 0 → ALWAYS bucket 0   (HashMap allows exactly ONE null key)
otherwise → hashCode() XOR its own upper 16 bits
```

**Why the XOR?** Because the index is computed with `(n-1) & hash`, and for a small table **only the low bits survive the mask**. Two keys differing only in their high bits would collide constantly. XOR-ing the high half down mixes those bits in, very cheaply. This is called **spreading** (or perturbation).

```
hashCode : 1111 1111 1111 1111 0000 0000 0000 0000
h >>> 16 : 0000 0000 0000 0000 1111 1111 1111 1111
XOR      : 1111 1111 1111 1111 1111 1111 1111 1111
                                 ↑ high bits now influence the index ⭐
```

Real-world idea: shuffling a deck before dealing. Without the shuffle, cards that were together stay together.

#### Step 2 — find the bucket index

```java
i = (n - 1) & hash;     // n = table.length, ALWAYS a power of two
```

```
n = 16   →  n-1 = 15  =  1111₂    ← a clean 4-bit mask
hash & 1111  keeps only the last 4 bits → a value 0..15 ✅
```

`&` with `n-1` is exactly the same as `hash % n` when `n` is a power of two, but a bitwise AND is far faster than a division. **That is the entire reason capacity is always a power of two.**

> If you pass `new HashMap<>(20)`, Java does **not** use 20 — `tableSizeFor(20)` rounds it **up to the next power of two = 32**.

#### Step 3 — place the node

```
Is table null or empty?           → resize() creates the 16-slot array (LAZY INIT)
Is table[i] null?                 → table[i] = new Node(hash, key, value, null)  ✅ done
Else there's a COLLISION:
   ├─ does the first node have the same hash AND (key == k || key.equals(k))?
   │        → overwrite its value  ✅ done
   ├─ is the first node a TreeNode?
   │        → putTreeVal() into the red-black tree
   └─ else walk the linked list:
            ├─ matching key found → overwrite that node's value
            └─ reached the tail   → append a new Node (TAIL insertion since Java 8)
                    └─ if the bin now has ≥ 8 nodes → treeifyBin()
                            └─ but if table.length < 64 → resize() instead ⭐
Finally:  if (++size > threshold) resize();
```

Draw it as the decision it is:

```
             put(key, value)
                   │
            hash → bucket i
                   │
        ┌──────────┴──────────┐
     empty?                 occupied?
        │                      │
  store the node      ┌────────┴────────┐
                  same key?          different key?
                      │                  │
              overwrite value      append to the chain
                                          │
                                    8 nodes reached?
                                          │
                                 table ≥ 64 → TREEIFY
                                 table < 64 → RESIZE
```

#### Step 4 — the return value

```java
V old = map.put("Ram", 25);
```

```
Key already existed → returns the OLD value
Key was new         → returns null
```

That is why `map.put(k, v) == null` is the common "was it new?" idiom — and also why it is **ambiguous** when the map legitimately stores `null` values. Use `containsKey()` when that matters.

---

# Part 5 — get() step by step

### Q: How does `get()` find the value?

```java
final Node<K,V> getNode(int hash, Object key) {
    // 1. compute the SAME spread hash
    // 2. index = (n - 1) & hash
    // 3. always check the FIRST node in the bucket (the common case — no loop needed)
    // 4. if that isn't it, and next != null:
    //       TreeNode?  → getTreeNode(hash, key)     O(log n)
    //       else       → walk the linked list       O(k)
}
```

```
"Ram"
  ↓
hashCode()  →  spread  →  (n-1) & hash  →  bucket 7      ← STEP 1: WHERE
  ↓
walk bucket 7
  ↓
hash == hash?  →  == ?  →  equals()                      ← STEP 2: WHICH
  ↓
return the value
```

### The comparison inside the loop — the part interviewers probe

```java
if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
```

Three checks, in a **deliberate order**, cheapest first:

```
1. e.hash == hash      → an int compare, extremely cheap
                         → rejects almost every non-match instantly ⚡
2. k == key            → reference equality
                         → a free hit for interned Strings and cached Integers
3. key.equals(k)       → only NOW does the potentially expensive user code run
```

Real-world idea: airport security. They check your **boarding pass** (instant), then your **face against the photo** (quick), and only then do a full **bag search** (slow).

> 💡 **Say this:** *"`hashCode` narrows it down to a bucket, `equals` decides the actual match — `hashCode` alone can never confirm equality, because two unequal objects are allowed to share a hash."*

### Complexity

| Case | Structure | Lookup |
|---|---|---|
| Good hash distribution | bucket has 0–1 nodes | **O(1)** ⚡ |
| Heavy collisions, pre-Java 8 or small table | linked list | **O(n)** 🐢 |
| Heavy collisions, Java 8+, table ≥ 64 | red-black tree | **O(log n)** 🙂 |

---

# Part 6 — Collisions: linked list → red-black tree

### Q: What is a hash collision and how does HashMap handle it?

```
A collision = two DIFFERENT keys landing in the SAME bucket index.
```

It happens for two reasons:

```
1. The keys genuinely have equal hashCodes
2. Different hash codes collapse to the same index after (n-1) & hash
   → e.g. hash 5 and hash 21 both give index 5 in a 16-slot table
```

Handling: the second key is **appended to the bucket's linked list**. Both entries live in the same bucket, and `equals()` tells them apart on lookup.

You can force a collision to see it:

```java
class BadKey {
    private final String name;
    BadKey(String name) { this.name = name; }

    @Override public int hashCode() { return 1; }              // 💣 everything → bucket 1
    @Override public boolean equals(Object o) {
        return o instanceof BadKey b && b.name.equals(name);
    }
}
// 10 BadKeys → ALL land in ONE bucket → a 10-node chain → treeified once table ≥ 64
```

⭐ Important: a collision is **not a bug and not data loss**. Nothing is overwritten — the entries simply share a bucket. Only an *equal key* overwrites a value.

---

### Q: Why was treeification added in Java 8?

**Two reasons — give both.**

```
1. PERFORMANCE under bad hash functions
   A degenerate bucket turned get() into a linear scan.
   1,000 colliding keys → 1,000 comparisons per lookup 🐢

2. SECURITY — hash-collision DoS ⭐ (this one impresses)
   Attackers could craft thousands of colliding String keys and POST them
   as form or JSON parameters. The server's map operations became quadratic
   and the CPU pinned at 100%.
   Treeification bounds the damage to O(log n).
```

That second point is the one most candidates don't know. It is worth saying.

---

### Q: Exactly when does a bin become a tree, and when does it revert?

```
An insert makes a bin reach 8 nodes
        │
        ├── table.length < 64 ?  ──▶ resize() — double the table and re-split the bin
        │                            (a bigger table usually breaks the chain up naturally)
        │
        └── table.length ≥ 64 ?  ──▶ treeifyBin(): convert the chain to a red-black tree

Later, during a resize, if a tree bin splits down to ≤ 6 nodes
        ──▶ untreeify back to a plain linked list
```

### Why 8 and 6, and not 8 and 7? ⭐

Two separate reasons, and **both** score:

**Why 8?**

```
The JDK source cites a Poisson distribution.
With a good hashCode and load factor 0.75, the chance of any bucket
reaching 8 entries is about 0.00000006.

→ Trees are meant to NEVER be used in a healthy map.
→ They are an insurance policy, not the normal path.
→ And a TreeNode is roughly 2× the size of a Node, so you don't want
  them casually.
```

**Why 6, not 7?**

```
The GAP prevents thrashing.

If both thresholds were 8, a bin sitting exactly at the boundary would
convert list → tree → list → tree on every single add and remove. 💥

The gap (8 up, 6 down) is hysteresis — flapping becomes impossible. ✅
```

Real-world idea: a **thermostat**. It switches the AC on at 26°C and off at 24°C, not both at 25°C — otherwise it would click on and off every few seconds.

---

### Q: How does a red-black tree order keys that aren't `Comparable`?

A good question to be ready for, because it shows real reading:

```
1. Order primarily by the HASH VALUE
2. If hashes tie AND the key implements Comparable AND both keys are the
   same class → use compareTo()
3. Otherwise → tieBreakOrder(): compare class names, then
   System.identityHashCode() — an arbitrary but CONSISTENT total order,
   purely to keep the tree balanced
```

> So: making your key `Comparable` makes treeified bins behave better, but it is **not required**.

---

# Part 7 — Load factor, threshold and resize

### Q: What is the load factor and why 0.75?

The easiest way to remember:

```
Load factor = how FULL the table may get before it grows.

threshold = capacity × loadFactor
```

```
Default: 16 × 0.75 = 12
        → the 13th entry triggers a resize to capacity 32 (threshold 24)
```

It is a **space/time trade-off** — say it as one:

| Load factor | Effect |
|---|---|
| Low (e.g. 0.5) | fewer collisions, faster lookups, **more wasted memory**, resizes more often |
| **0.75** | empirically the sweet spot — good collision behaviour, acceptable memory |
| High (e.g. 1.0) | dense table, less memory, **longer chains → slower gets** |

Real-world idea: a **car park**.

```
25% full → find a space instantly, but you built a huge car park 💸
75% full → a little searching, sensible size ✅
100% full → you circle for ten minutes 🐢
```

---

### Q: What happens during a resize?

```
1. A new table of DOUBLE the capacity is allocated; the threshold doubles too
2. Every existing node is redistributed into the new table
3. ⭐ The clever Java 8 part:
```

Because the capacity doubles, a node's new index is either **the same index** or **index + oldCapacity** — decided by a single bit test:

```java
if ((e.hash & oldCap) == 0)  → stays at index j            (the "lo" list)
else                         → moves to index j + oldCap   (the "hi" list)
```

```
oldCap = 16, bucket 3 holds A, B, C, D
             │
   split by (hash & 16)
             ├── lo list → new table[3]
             └── hi list → new table[19]      (3 + 16)
```

So Java 8 **never recomputes the hash or the modulo** during a resize. It splits each bucket into two lists and attaches them — and it **preserves relative order**, which matters a lot (see Part 9).

---

### Q: Why is resizing expensive, and how do you avoid it?

```
A resize is O(n), and it happens REPEATEDLY while a map grows:
16 → 32 → 64 → 128 → 256 → ...

Inserting 1M entries into a default map means about 17 rehash passes. 🐢
```

**The fix — size it up front:**

```java
// You know you'll hold ~1000 entries:
Map<String,User> m = new HashMap<>(1365);   // 1000 / 0.75 ≈ 1334 → rounded to 2048

// Java 19+ has the ergonomic version — no arithmetic, no mistakes:
Map<String,User> m2 = HashMap.newHashMap(1000);
```

> ⚠️ **Trap:** `new HashMap<>(1000)` does **not** mean "holds 1000 without resizing".
>
> ```
> capacity 1000 → rounded up to 1024
> threshold = 1024 × 0.75 = 768
> → it resizes at the 769th entry 💥
> ```
>
> Always **divide by the load factor first**.

> 💡 **Real-project line:** *"In the Subsea schedule loader we were building a map of ~40k parts inside a loop and it was resizing all the way up from 16. Pre-sizing the map removed a visible chunk of the load time — it's a one-line fix that people never make."*

---

# Part 8 — hashCode() and equals() contract

### Q: What is the contract?

```
1. a.equals(b) is TRUE  →  a.hashCode() == b.hashCode()  MUST be true ✅

2. hashCodes equal      →  objects MAY OR MAY NOT be equal  (collisions are legal)

3. hashCode() must be CONSISTENT — the same object returns the same value,
   as long as the fields used in equals() don't change
```

The one-line version:

```
equals() TRUE → hashCode SAME    ✅ guaranteed
hashCode SAME → equals() TRUE?   ❌ not guaranteed
```

---

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
```

```java
Map<Employee,String> map = new HashMap<>();
map.put(new Employee(1), "Priyanka");

System.out.println(map.get(new Employee(1)));   // null ← equal object, WRONG bucket
System.out.println(map.size());                 // 1

map.put(new Employee(1), "Again");
System.out.println(map.size());                 // 2 ← DUPLICATE "equal" keys in one map 💥
```

```
Bucket 100 │ Employee(1) → "Priyanka"     ← stored here

Bucket 500 │ (searched with the second Employee(1)) → empty → null 💥
```

The postman went to the wrong PIN code, so he never even read the name boards.

**The reverse mistake** (overriding `hashCode` only) does not lose data — both objects land in the same bucket, but `equals` says they're different, so you get two entries. Slower, and still wrong.

---

### Q: What happens if a key is mutated after being put in the map?

**You orphan the entry.**

The node keeps the **old cached hash**, so the map still looks in the old bucket, but `equals()` now fails there — and the object's new hash points somewhere else entirely.

```java
List<String> key = new ArrayList<>(List.of("a"));
Map<List<String>,String> map = new HashMap<>();

map.put(key, "v");
key.add("b");                       // 💣 the hashCode just changed

System.out.println(map.get(key));   // null — unreachable, but still occupying memory
System.out.println(map.size());     // 1   (a slow memory leak in long-lived maps)
```

```
put:  hash was 97 → stored in bucket 1
key mutated → its hash is now 3105
get:  looks in bucket 1  (the CACHED hash) → equals() fails → null
      the entry is invisible forever, but never garbage collected 💥
```

**Rule to state:**

> *"Map keys must be immutable — or at least, the fields used in `equals`/`hashCode` must never change. That's why `String`, `Integer` and records make ideal keys."*

---

### Q: How should you write `hashCode()` in real code?

```java
public record EmployeeKey(int id, String dept) { }   // record → equals + hashCode generated ✅
```

Or the classic form:

```java
@Override public int hashCode() {
    return Objects.hash(id, dept);
}

@Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Employee e)) return false;
    return id == e.id && Objects.equals(dept, e.dept);
}
```

⭐ The golden rule: **both methods must use exactly the same fields.**

> ⚠️ **Lombok trap they love:** `@Data` on a JPA entity generates `equals`/`hashCode` over **all** fields, including the DB-generated `id` and lazy collections.
>
> ```
> new entity → id is null → hash X → put into a HashSet
> save() flushes → id becomes 42 → hash Y  💥 the entity is now unreachable in the set
> touching a lazy collection → triggers a query, or LazyInitializationException
> ```
>
> See **[24 — ORM/JPA](./24-orm-jpa-hibernate.md)**.

---

# Part 9 — Java 7 vs Java 8 (the infinite-loop story)

### Q: What changed in HashMap in Java 8?

| | Java 7 | Java 8+ |
|---|---|---|
| Bucket structure | linked list only | linked list **→ red-black tree** at 8 |
| Insert position | **head** insertion | **tail** insertion |
| Resize transfer | recomputes the index, **reverses** list order | splits into lo/hi lists, **preserves** order |
| Hash function | 4 shifts + 4 XORs | a single `h ^ (h >>> 16)` |
| Entry class | `Entry<K,V>` | `Node<K,V>` (+ `TreeNode`) |
| Worst-case get | O(n) | O(log n) |

---

### Q: What was the famous Java 7 HashMap infinite loop?

**Tell this as a story — it lands very well.**

```
Java 7 resize used HEAD insertion, which REVERSED each chain.

Thread A starts transferring bucket 3:  A → B → C
Thread B does the same at the same time and completes the reversal
Thread A wakes up mid-transfer, holding stale pointers
        ↓
Result:  A.next = B   and   B.next = A     ← a CYCLE 🔁
        ↓
A later get() walks that cycle FOREVER
```

The symptom was brutal:

```
100% CPU on one thread
No exception. No error. Nothing in the logs.
```

Teams only found it by taking a thread dump and seeing a thread stuck inside `HashMap.get`.

Java 8's lo/hi split **preserves order** and never reverses, so this specific infinite loop is gone.

> ⚠️ **Do NOT conclude "so Java 8 HashMap is thread-safe."**
>
> It is still **not** thread-safe. You can still lose updates, see a stale `size`, or get a `ConcurrentModificationException`. Java 8 removed one specific catastrophic failure, not the need for synchronization.
>
> Interviewers *actively fish* for the wrong conclusion here.

---

# Part 10 — HashMap vs the other Maps

| | **HashMap** | **Hashtable** | **LinkedHashMap** | **TreeMap** | **ConcurrentHashMap** |
|---|---|---|---|---|---|
| Ordering | none (bucket order) | none | **insertion** (or access) order | **sorted** by key | none |
| Structure | Node array + list/tree | Node array + list | HashMap + doubly-linked list | red-black tree | Node array + list/tree |
| Thread-safe | ❌ | ✅ (method-level `synchronized`) | ❌ | ❌ | ✅ (CAS + per-bin lock) |
| null key | 1 allowed | ❌ NPE | 1 allowed | ❌ NPE (needs compare) | ❌ NPE |
| null values | ✅ | ❌ | ✅ | ✅ | ❌ |
| get/put | O(1) | O(1) | O(1) | **O(log n)** | O(1) |
| Since | 1.2 | 1.0 (legacy) | 1.4 | 1.2 | 1.5 |

**Say this about Hashtable:**

> *"Legacy — it locks the whole object on every method, so it doesn't scale. Nobody should choose it in new code; `ConcurrentHashMap` replaced it."*

---

### LinkedHashMap bonus — an LRU cache in 5 lines ⭐

A classic follow-up, and a genuinely useful thing to know:

```java
class LruCache<K,V> extends LinkedHashMap<K,V> {

    private final int cap;

    LruCache(int cap) {
        super(cap, 0.75f, true);      // true = ACCESS order, not insertion order ⭐
        this.cap = cap;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > cap;          // evict the least recently used
    }
}
```

```
access order = true
      ↓
every get() moves that entry to the END of the internal linked list
      ↓
the entry at the FRONT is the least recently used
      ↓
removeEldestEntry() drops it when the cache is full ✅
```

---

### HashSet — the one-liner that impresses

> *"`HashSet` is literally a `HashMap`. It holds one internally and stores every element as a **key**, with a shared dummy `PRESENT` object as the value. That's why `HashSet.add` returns a boolean — it's just `map.put(e, PRESENT) == null`."*

```java
private static final Object PRESENT = new Object();

public boolean add(E e) {
    return map.put(e, PRESENT) == null;
}
```

---

# Part 11 — Thread safety & ConcurrentHashMap

### Q: Is HashMap thread-safe? What are the options?

**No.** Concurrent `put`s can lose entries, corrupt a chain, or leave `size` wrong.

| Option | How it locks | Verdict |
|---|---|---|
| `Hashtable` | `synchronized` on every method | legacy, one global lock ❌ |
| `Collections.synchronizedMap(map)` | wrapper, one lock for the whole map | works, but iteration must be **manually** synchronized |
| **`ConcurrentHashMap`** | CAS for empty bins + `synchronized` on the **bin head** | ✅ the right answer |

```
Hashtable / synchronizedMap
   ┌────────────────────────────────┐
   │ 🔒 ONE lock for the WHOLE map   │  → all threads queue 🐢
   └────────────────────────────────┘

ConcurrentHashMap
   bucket 2  🔒 Thread A works ✅
   bucket 9  🔒 Thread B works ✅   ← at the SAME time
   bucket 14 🔒 Thread C works ✅
```

Real-world idea: a bank with one key for the whole building versus a separate lock on each counter.

---

### Q: How does ConcurrentHashMap work internally (Java 8)?

```
• SAME layout as HashMap — Node[] table, lists that treeify at 8

• NO more segments ⭐
      Java 7 used ~16 Segment locks (ReentrantLock)
      Java 8 REMOVED them entirely

• Empty bucket → insert with a CAS (compare-and-swap), NO lock at all ⚡

• Non-empty bucket → synchronized (firstNode)
      → lock granularity is ONE BUCKET
      → two threads writing to different buckets never contend

• Resize is COOPERATIVE
      threads that find a resize in progress HELP transfer bins
      (a ForwardingNode marks a bin that has already moved)

• size() uses a striped LongAdder-style counter (baseCount + CounterCell[]),
  not a single contended field

• Iterators are WEAKLY CONSISTENT, not fail-fast
      → they never throw ConcurrentModificationException
      → they may or may not reflect concurrent updates

• NO nulls for keys or values
      → because map.get(k) == null would be ambiguous between "absent" and
        "mapped to null", and containsKey() can't be atomic with get()
        while other threads are mutating
```

---

### Q: Is `ConcurrentHashMap` fully atomic?

> ⚠️ **Trap.** Individual operations are atomic, but **compound** ones are not.

```java
// ❌ RACE                                   // ✅ ATOMIC
if (!map.containsKey(k)) map.put(k, v);      map.putIfAbsent(k, v);

map.put(k, map.get(k) + 1);                  map.merge(k, 1, Integer::sum);

if (map.get(k) == null)                      map.computeIfAbsent(k, key -> load(key));
    map.put(k, load(k));
```

```
Thread A: containsKey → false
Thread B: containsKey → false      ← both saw "absent"
Thread A: put(v1)
Thread B: put(v2)                  ← one update is silently lost 💥
```

The atomic methods to name: **`putIfAbsent`, `computeIfAbsent`, `compute`, `merge`.**

---

### Q: What is a fail-fast iterator?

```
HashMap iterators record modCount when created,
and compare it on EVERY next().

Mismatch → ConcurrentModificationException 💥
```

```java
for (String k : map.keySet()) {
    if (k.startsWith("tmp")) map.remove(k);      // 💥 CME
}
```

```java
// ✅ iterator.remove()
Iterator<String> it = map.keySet().iterator();
while (it.hasNext()) {
    if (it.next().startsWith("tmp")) it.remove();
}

// ✅ or, simplest
map.keySet().removeIf(k -> k.startsWith("tmp"));
```

> **Say the caveat:** *"Fail-fast is best-effort — it's a bug-detection aid, not a guarantee. You cannot rely on the exception being thrown."*

---

# Part 12 — Follow-up questions they ask next

### Q: Why is HashMap capacity always a power of two?

```
So that (n - 1) & hash is a valid, FAST substitute for hash % n.
```

```
n = 16 → n-1 = 15 = 1111₂    ← a clean low-bit mask, every index reachable ✅

n = 15 → n-1 = 14 = 1110₂    ← the last bit is always 0
                              → NO ODD INDEX can ever be produced
                              → half the table is unreachable 💥
```

That is the killer detail: with a non-power-of-two capacity, **half your buckets would never be used.**

---

### Q: Can HashMap have a null key? How, if hashing null would NPE?

```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    //     ↑ SPECIAL-CASED — it never calls hashCode() on null ⭐
}
```

```
Exactly ONE null key allowed → always bucket 0
Null VALUES → unrestricted, as many as you like

TreeMap → ❌ (it must compare keys)
ConcurrentHashMap → ❌ (ambiguous get())
```

---

### Q: What is the time complexity of HashMap operations?

| Operation | Average | Worst (Java 8+) |
|---|---|---|
| `get` / `put` / `remove` / `containsKey` | **O(1)** | **O(log n)** (treeified) — O(n) if the table is < 64 |
| `containsValue` | **O(n)** — scans every bucket | O(n) |
| Iteration | O(n + capacity) — it walks empty buckets too | same |

> 💡 **Nice depth:** *"Iteration cost depends on **capacity**, not just size. A map that once held a million entries and was then cleared still has a huge table — `clear()` doesn't shrink it. HashMap **never** shrinks; only creating a fresh map does."*

---

### Q: Why is `String` a good HashMap key?

```
1. IMMUTABLE          → the hash can never go stale ⭐
2. hashCode is CACHED → computed once, stored inside the String object
3. equals is cheap    → length check first, then characters
4. Good distribution  → the 31-based polynomial hash spreads well
```

The same reasoning makes **records** and **enums** good keys. For enums, `EnumMap` is even better — it is a plain array indexed by `ordinal()`, with no hashing at all.

---

### Q: Is HashMap ordering guaranteed to be stable?

**No.** Iteration order depends on hash values **and the current capacity**, so it can change on a resize.

```
Need insertion order? → LinkedHashMap
Need sorted order?    → TreeMap
Never rely on HashMap's order.
```

---

# Part 13 — Predict the output

**Q1**
```java
Map<String,Integer> m = new HashMap<>();
m.put("a", 1); m.put("a", 2);
System.out.println(m.size() + " " + m.get("a"));
```
<details><summary>Answer</summary>

`1 2`

Same key (equal hash + equals) → the existing node's **value** is overwritten; the size is unchanged.
</details>

**Q2**
```java
Map<Integer,String> m = new HashMap<>();
m.put(null, "x");
m.put(null, "y");
System.out.println(m.size() + " " + m.get(null));
```
<details><summary>Answer</summary>

`1 y`

One null key is allowed, always in bucket 0; the second `put` overwrites it.
</details>

**Q3** ⭐ (the mutable-key classic)
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

`null null 1`

```
put   → cached hash 1 → entry stored in bucket 1
k.id = 99

m.get(k)        → hashes to bucket 99 → EMPTY → null
m.get(new K(1)) → reaches bucket 1, but equals() compares against the
                  MUTATED object whose id is now 99 → false → null
m.size()        → still 1
```

The entry is **unreachable but still counted** — the mutable-key leak.
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

`null`, `null`, `true`

`get()` returning null is **ambiguous** — only `containsKey()` distinguishes "present with a null value" from "absent". This is exactly why `ConcurrentHashMap` bans null values.
</details>

**Q5**
```java
Map<String,Integer> m = new HashMap<>(3);
System.out.println(m.size());
m.put("a",1);
```
<details><summary>Answer</summary>

`0`

Two points worth making:
1. The table is **not allocated in the constructor** — it is lazily created on the first `put`.
2. Capacity 3 is rounded up by `tableSizeFor` to **4**, so the threshold becomes 4 × 0.75 = 3.
</details>

**Q6**
```java
Set<Employee> s = new HashSet<>();   // Employee overrides equals() ONLY
s.add(new Employee(1));
s.add(new Employee(1));
System.out.println(s.size());
```
<details><summary>Answer</summary>

`2`

Different identity hash codes → different buckets → `equals` is never even consulted. The broken-contract classic.
</details>

**Q7**
```java
Map<String,Integer> m = new HashMap<>();
m.put("A", 1); m.put("B", 2); m.put("C", 3);
for (String k : m.keySet()) if (k.equals("B")) m.remove(k);
System.out.println(m);
```
<details><summary>Answer</summary>

Throws **`ConcurrentModificationException`** … *usually*.

With exactly one removal it can sometimes finish silently, if the removal happens on the second-to-last element — `hasNext()` returns false before the `modCount` check runs.

That unpredictability **is the point**: fail-fast is best-effort. Use `removeIf`.
</details>

**Q8**
```java
Map<Integer,String> m = new HashMap<>();
for (int i = 1; i <= 5; i++) m.put(i, "v" + i);
System.out.println(m.keySet());
```
<details><summary>Answer</summary>

`[1, 2, 3, 4, 5]` — and this misleads people.

Small `Integer` keys hash to themselves, so `(16-1) & i == i`: they land in buckets 1..5, in order.

It is **not** an ordering guarantee. Add key 20 and it appears at bucket 4, out of order.
</details>

---

# Part 14 — Rapid-fire table

| Question | One-line answer |
|---|---|
| Internal structure? | `Node<K,V>[] table` — an array of buckets |
| What is a bucket? | one array slot: null, a linked list of Nodes, or a red-black tree |
| Fields of a Node? | `final int hash`, `final K key`, `V value`, `Node next` |
| Index formula? | `(n - 1) & hash` |
| Hash function? | `h ^ (h >>> 16)` — spreads high bits into the low ones |
| Why power-of-two capacity? | makes `&` a valid, fast modulo (and keeps every index reachable) |
| Default capacity / load factor? | 16 / 0.75 → resizes on the 13th entry |
| Treeify threshold? | 8 nodes **and** table length ≥ 64 |
| Untreeify threshold? | 6 |
| Why 8 up, 6 down (not 7)? | the gap prevents convert/revert thrashing |
| Why tree at all? | O(log n) worst case + hash-collision DoS defence |
| Resize behaviour? | doubles; splits each bin by `(hash & oldCap)` into lo/hi lists |
| Null key? | one, hash forced to 0, bucket 0 |
| Thread-safe? | ❌ — use `ConcurrentHashMap` |
| CHM lock granularity? | CAS on an empty bin, `synchronized` on the bin's **head node** |
| Java 7 vs 8 insertion? | head → **tail** insertion |
| Java 7 infinite loop? | head-insert resize reversed chains → cyclic list under concurrency |
| Iterator type? | fail-fast (`modCount`); CHM is weakly consistent |
| get/put complexity? | O(1) average, O(log n) worst |
| HashSet internally? | a HashMap with a dummy `PRESENT` value |

---

# Part 15 — Drills

Cover the answers and say them out loud.

**1. Draw a 16-slot table and place keys with hashes 5, 21, 37. Which bucket, and why?**
<details><summary>Answer</summary>

All three → **bucket 5**:

```
5  & 15 = 5
21 & 15 = 5
37 & 15 = 5
```

A 3-node chain. After a resize to 32:

```
5  & 31 = 5
21 & 31 = 21
37 & 31 = 5
```

→ the chain splits into bucket 5 (two nodes) and bucket 21.
</details>

**2. A map has 100,000 keys all returning `hashCode() == 7`. What is `get()` complexity in Java 7 and in Java 8?**
<details><summary>Answer</summary>

Java 7: **O(n)** — a 100,000-node linked list.

Java 8: **O(log n)** — the bin treeifies (the table is well over 64).
</details>

**3. You need a map for ~500 entries. What capacity do you pass?**
<details><summary>Answer</summary>

500 / 0.75 = 667 → the next power of two is 1024, so `new HashMap<>(667)` (or simply `HashMap.newHashMap(500)` on Java 19+).

Passing 500 gives capacity 512 and threshold 384 → it resizes before you finish.
</details>

**4. Why can't `ConcurrentHashMap` store null values?**
<details><summary>Answer</summary>

`get()` returning null would be ambiguous between "absent" and "mapped to null", and you cannot disambiguate with `containsKey` atomically while other threads mutate the map.

Doug Lea calls it a design error in `HashMap` that he refused to repeat.
</details>

**5. Two threads call `put` on the same `HashMap` at the same time. Name three things that can go wrong.**
<details><summary>Answer</summary>

1. **Lost update** — both read `table[i]` as null and one overwrites the other.
2. **`size` becomes wrong** — `++size` is not atomic.
3. **Concurrent resize** can drop entries or, in Java 7, create a cyclic chain that pins a CPU forever.
</details>

**6. Is it safe to use a mutable object as a key if you never mutate it? Is it *good practice*?**
<details><summary>Answer</summary>

Safe in that instant, but poor practice — nothing enforces the discipline, and the failure is **silent**: the entry becomes unreachable with no exception at all.

Prefer immutable keys: `String`, `Integer`, enums, records.
</details>

**7. Explain to a junior why `hashCode` alone can never decide equality.**
<details><summary>Answer</summary>

A hash maps a huge key space onto 32 bits, so collisions are mathematically unavoidable — the pigeonhole principle.

The hash narrows the search to a bucket; `equals` decides the match. That is exactly the two-step lookup inside `getNode`.
</details>

---

## Quick Revision Sheet

```
STRUCTURE
  Node<K,V>[] table  →  bucket = ONE array slot
  bucket = null | linked list of Nodes | red-black tree
  Node = {final hash, final key, value, next}

put(k,v)
  hash = h ^ (h >>> 16)          ← spread the high bits
  index = (n-1) & hash           ← fast modulo (capacity is a power of 2)
  empty → store | same key → overwrite | else → append to the tail

get(k)
  hash → bucket → compare: hash == , then == , then equals()
  hashCode = WHERE ⭐  |  equals = WHICH ⭐

NUMBERS
  capacity 16 | load factor 0.75 | resize at 12 (the 13th entry)
  treeify at 8 nodes AND table ≥ 64  |  untreeify at 6
  resize: doubles, splits by (hash & oldCap) into lo / hi, preserves order

CONTRACT
  equals TRUE → hashCode SAME ✅ | hashCode SAME → equals? ❌
  override equals → ALWAYS override hashCode, using the SAME fields
  MUTABLE KEY = orphaned entry = silent leak 💥

JAVA 7 → 8
  head → TAIL insertion; resize no longer reverses chains
  → the Java 7 concurrent infinite loop is gone
  → but HashMap is STILL not thread-safe ⚠️

THREADS
  ConcurrentHashMap: CAS on empty bins + synchronized on the bin head
  no segments (Java 8), no nulls, weakly consistent iterators
  compound ops need putIfAbsent / computeIfAbsent / merge

HashSet = a HashMap with a dummy PRESENT value
LinkedHashMap(cap, 0.75f, true) + removeEldestEntry = LRU cache
```

---

## What to say when you don't know a detail

> *"I know the shape of it — array of buckets, chained Nodes, treeified after 8 collisions, resize at 0.75. I haven't memorised that specific constant, but I'd check `HashMap`'s source; the constants are all at the top of the class."*

That is a **strong** answer. Bluffing a wrong number is much worse than that sentence.

---

**Related files:** [05 — Core Java](./05-java.md) · [23 — Predict the Output](./23-java-output-tricky-questions.md) · [32 — Multithreading](./32-multithreading.md) · [26 — Companies: Questions Actually Asked](./26-companies-asked-questions.md)
