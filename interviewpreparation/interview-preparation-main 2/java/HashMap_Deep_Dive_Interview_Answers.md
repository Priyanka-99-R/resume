# HashMap Deep Dive — 40 Interview Q&A (Senior-Level, Interview Perspective)

---

## Q1. What is HashMap?

**Summary:** HashMap is a hash-table-based implementation of the Map interface that stores key-value pairs and provides O(1) average-time complexity for put, get, and remove operations.

**Practical Explanation:** Internally, HashMap maintains an array of `Node<K,V>` (called buckets). When you `put(key, value)`, it computes the key's `hashCode()`, determines the bucket index, and stores the entry there. On `get(key)`, it repeats the hash computation to jump directly to the right bucket — no scanning needed.

**When/Why Used:** It's the most commonly used Map implementation in Java. Caching, lookup tables, counting occurrences, grouping data, storing configuration — any time you need fast key-based access. In Spring applications, it's used everywhere internally — bean definitions, request parameter mapping, property sources.

**Real-World Scenario:** Caching user profiles by userId for quick lookup during request processing:
```java
Map<Long, UserProfile> userCache = new HashMap<>(1024);
userCache.put(userId, profile);
UserProfile cached = userCache.get(userId); // O(1)
```

**Common Mistakes:** Not overriding `hashCode()` and `equals()` on custom key objects — leads to "phantom" entries you can never retrieve. Using HashMap in multi-threaded code without synchronization — causes data corruption silently.

**Closing:** HashMap is the workhorse of Java collections — understanding its internals is what separates a senior developer from a junior one.

---

## Q2. How HashMap works internally?

**Summary:** HashMap uses an array of buckets (Node array), where the bucket index is determined by hashing the key. Each bucket can hold a linked list (or a red-black tree in Java 8+) of entries to handle collisions.

**Practical Explanation:** The internal flow of `put(key, value)`:
1. Compute `key.hashCode()` and apply a spread function: `hash = hashCode ^ (hashCode >>> 16)` — this mixes high and low bits.
2. Determine bucket index: `index = hash & (capacity - 1)` — bitwise AND since capacity is a power of 2.
3. If bucket is empty → create new `Node` and place it there.
4. If bucket is occupied → traverse the linked list/tree:
   - If a node with the same key exists (`equals()` returns true) → replace the value.
   - Otherwise → append a new node to the end of the list.
5. If the list length exceeds 8 (and capacity >= 64) → convert to red-black tree.
6. If total size exceeds threshold (`capacity * loadFactor`) → resize (double the array).

For `get(key)`:
1. Compute hash → find bucket index.
2. Traverse nodes in that bucket, comparing with `equals()`.
3. Return value if found, `null` if not.

**When/Why Used:** This knowledge is essential for writing performant code — knowing the internal mechanism helps you choose proper initial capacity, write good `hashCode()` methods, and diagnose performance issues.

**Real-World Scenario:** We had a production issue where a HashMap-backed cache was extremely slow. Profiling revealed all keys had the same `hashCode()` (a badly implemented custom key class), so all entries went into one bucket — degrading from O(1) to O(n). Fixing the `hashCode()` distribution resolved it instantly.

**Common Mistakes:** Not understanding the spread function — even a perfect `hashCode()` gets further processed. Thinking HashMap preserves insertion order (it doesn't — use `LinkedHashMap`). Not accounting for the Node object overhead in memory calculations.

**Closing:** Interviewers love this question because it touches hashing, data structures, equals/hashCode contract, and performance — it's a complete test of your fundamentals.

---

## Q3. What is hash collision?

**Summary:** A hash collision occurs when two different keys produce the same bucket index after hashing — they end up in the same bucket and must be stored together.

**Practical Explanation:** Since the bucket array has a finite size (say 16), and keys are potentially infinite, multiple keys will inevitably map to the same index. For example, if `"Aa".hashCode()` and `"BB".hashCode()` both resolve to bucket index 5, that's a collision. The bucket then holds both entries in a linked list (or tree).

**When/Why Used:** Understanding collisions is critical for designing good `hashCode()` methods. A poor hash function causes many collisions, degrading HashMap performance from O(1) to O(n). The JVM's spread function `hash ^ (hash >>> 16)` helps distribute keys more evenly.

**Real-World Scenario:** In a system indexing millions of products by SKU, a naive `hashCode()` that only used the first two characters caused massive clustering — all SKUs starting with "PR" collided. Switching to a proper hash using all characters (`Objects.hash(sku)`) eliminated the bottleneck.

**Common Mistakes:** Thinking collisions are bugs — they're normal and expected. The goal is to minimize them, not eliminate them. Also, confusing `hashCode()` collision (same hashCode value) with bucket collision (same bucket index) — the spread function and capacity mask can cause bucket collisions even with different hashCodes.

**Closing:** Some collisions are inevitable — what matters is distributing keys uniformly across buckets to keep bucket chains short and lookups fast.

---

## Q4. How collisions handled in Java 7?

**Summary:** In Java 7, HashMap handles collisions using a singly linked list within each bucket — new entries are added at the head of the list (head insertion).

**Practical Explanation:** When two keys hash to the same bucket, Java 7 creates a linked list of `Entry<K,V>` nodes at that bucket index. On `put()`, the new entry is inserted at the head of the list (prepend). On `get()`, the list is traversed linearly, checking `equals()` on each node until the key is found.

**When/Why Used:** This was the standard collision resolution from Java 1.2 through Java 7. While simple to implement, it has a fatal flaw — if many keys collide, the linked list grows long, and lookup degrades to O(n).

**Real-World Scenario:** A well-known Denial-of-Service attack vector (HashDoS) exploited this — attackers crafted HTTP request parameters with keys that all hashed to the same bucket, turning HashMap operations into O(n) and overwhelming the server. This was a major motivation for the Java 8 treeification improvement.

**Common Mistakes:** Head insertion in Java 7 causes a critical concurrency bug — during concurrent resize, the linked list can form a cycle, causing `get()` to loop infinitely. This is the infamous Java 7 HashMap infinite loop bug. It was fixed in Java 8 by switching to tail insertion.

**Closing:** Java 7's linked list approach is simple but vulnerable — understanding its limitations explains exactly why Java 8 introduced treeification.

---

## Q5. How collisions handled in Java 8?

**Summary:** In Java 8, HashMap starts with a linked list for collisions but converts to a balanced red-black tree when a bucket's chain exceeds 8 nodes — improving worst-case from O(n) to O(log n).

**Practical Explanation:** Java 8 introduced two key changes:
1. **Tail insertion** instead of head insertion — eliminates the infinite loop bug during concurrent resize.
2. **Treeification** — when a bucket's linked list grows beyond `TREEIFY_THRESHOLD` (8) and the table capacity is >= 64, the list is converted into a self-balancing red-black tree. This brings worst-case lookup from O(n) to O(log n).

The `Node<K,V>` is replaced with `TreeNode<K,V>` which extends `LinkedHashMap.Entry` and maintains left/right/parent pointers.

**When/Why Used:** This is the current behavior in all modern Java (8+). It protects against pathological hash distributions and HashDoS attacks while maintaining O(1) average case.

**Real-World Scenario:** In a high-traffic API, we had keys with deliberately poor hash distribution (external input used as keys). In Java 7, this would've been catastrophic. With Java 8's treeification, even worst-case buckets stayed at O(log n) — the system remained responsive.

**Common Mistakes:** Assuming treeification happens at exactly 8 entries — it also requires table capacity >= 64 (otherwise it resizes the table first). Also, for treeification to work efficiently, keys should implement `Comparable` — otherwise TreeNodes fall back to comparing by `hashCode()` and `identityHashCode`, which works but is less optimal.

**Closing:** Java 8's collision handling is a brilliant balance between simplicity (linked list for small buckets) and performance (tree for large buckets) — it's one of the most impactful internal improvements in Java's history.

---

## Q6. When does treeification occur?

**Summary:** Treeification occurs when a single bucket has more than 8 nodes (TREEIFY_THRESHOLD) AND the total table capacity is at least 64 (MIN_TREEIFY_CAPACITY).

**Practical Explanation:** Two conditions must be met:
1. Bucket chain length > `TREEIFY_THRESHOLD` (8)
2. Table capacity >= `MIN_TREEIFY_CAPACITY` (64)

If the chain exceeds 8 but capacity is below 64, HashMap resizes the table instead — the assumption being that a small table with long chains just needs more buckets. Only when the table is already large enough does it convert to a tree.

**When/Why Used:** This is automatic and internal — developers don't trigger it manually. But understanding these thresholds helps when designing capacity planning and hash functions.

**Real-World Scenario:** If you initialize a HashMap with default capacity 16 and insert keys that all collide, the first 8 inserts create a linked list. On the 9th insert, instead of treeifying, the HashMap resizes to 32, then 64. Only after capacity reaches 64 does treeification actually kick in.

**Common Mistakes:** Thinking treeification always happens at 8 — the capacity check is often overlooked. Also, each treeified `TreeNode` consumes roughly twice the memory of a regular `Node` — so treeification trades memory for speed.

**Closing:** These thresholds are carefully chosen by the JDK team — 8 is a statistical sweet spot where the probability of a linked list that long under random hashing is extremely low (about 0.00000006).

---

## Q7. When does untreeification occur?

**Summary:** Untreeification converts a red-black tree back to a linked list when the node count in a bucket drops below `UNTREEIFY_THRESHOLD` (6), typically during resize or removal.

**Practical Explanation:** During resize, entries are redistributed across the new (doubled) buckets. If a formerly treeified bucket splits and the resulting bucket has 6 or fewer nodes, it's converted back to a simpler linked list. The threshold is 6 (not 8) to provide hysteresis — avoiding repeated tree/list oscillation when node count hovers around the boundary.

**When/Why Used:** This is an internal optimization. The gap between treeify (8) and untreeify (6) prevents "thrashing" — if both thresholds were 8, adding and removing a single entry would constantly convert between tree and list.

**Real-World Scenario:** A cache backed by HashMap grows during peak traffic (treeification happens on hot buckets) and shrinks during off-peak as entries expire (untreeification recovers memory). The hysteresis ensures stable behavior during transitions.

**Common Mistakes:** Thinking untreeification happens during `remove()` on every deletion — it primarily occurs during resize operations. The threshold gap of 2 (8 vs 6) is deliberate engineering, not arbitrary.

**Closing:** The treeify/untreeify balance is a great example of JDK engineering — optimizing for real-world workload patterns rather than theoretical edge cases.

---

## Q8. Why capacity is power of 2?

**Summary:** HashMap's capacity is always a power of 2 so that bucket index calculation can use `hash & (capacity - 1)` instead of the expensive modulo operation — this is a bitwise optimization.

**Practical Explanation:** To find a bucket index from a hash, you need `hash % capacity`. Modulo is slow. But when `capacity` is a power of 2, `capacity - 1` produces a bitmask of all 1s (e.g., 16 - 1 = 15 = `0b1111`). Then `hash & (capacity - 1)` is equivalent to `hash % capacity` but much faster — a single CPU instruction.

```
capacity = 16     → binary: 10000
capacity - 1 = 15 → binary: 01111
hash & 01111      → keeps last 4 bits → value 0-15 (valid bucket indices)
```

**When/Why Used:** Every `put()` and `get()` call computes the bucket index, so this optimization matters enormously at scale — millions of operations per second.

**Real-World Scenario:** If you pass a non-power-of-2 initial capacity (e.g., `new HashMap<>(100)`), HashMap automatically rounds it up to the next power of 2 (128). Understanding this prevents wasting memory — `new HashMap<>(17)` actually allocates 32 buckets.

**Common Mistakes:** Specifying arbitrary initial capacities and assuming that's what you get. Also, not understanding that this power-of-2 constraint is why the spread function `hash ^ (hash >>> 16)` exists — it ensures high bits influence the index even when only low bits are used by the mask.

**Closing:** This is a beautiful low-level optimization — a single design choice (power-of-2 capacity) enables bitwise indexing, motivates the spread function, and simplifies resize (entries either stay or move exactly `oldCapacity` positions).

---

## Q9. Load factor meaning?

**Summary:** The load factor (default 0.75) determines when the HashMap resizes — it's the ratio of entries to capacity at which the table doubles in size to maintain performance.

**Practical Explanation:** `threshold = capacity * loadFactor`. When `size >= threshold`, the HashMap resizes. With default capacity 16 and load factor 0.75, resize happens at 12 entries. Lower load factor = more empty buckets = fewer collisions but more memory. Higher load factor = denser table = more collisions but less memory.

**When/Why Used:** The default 0.75 is a well-tested balance between time (collision probability) and space (memory usage). Adjust only when you have strong reasons — memory-constrained environments might use higher (0.9), latency-critical systems might use lower (0.5).

**Real-World Scenario:** For a cache that stores 10,000 entries and rarely changes, we set initial capacity to `10000 / 0.75 + 1 ≈ 13334` to avoid any resize during population:
```java
Map<String, Config> configCache = new HashMap<>(13334, 0.75f);
```
This prevents the expensive resize operations during bulk loading.

**Common Mistakes:** Setting load factor too low (like 0.1) wastes huge amounts of memory. Setting it too high (like 1.0) means the table is nearly full before resizing — heavily degraded performance. Also, forgetting that load factor is set at construction time and cannot be changed later.

**Closing:** The 0.75 default is the right choice 99% of the time — only change it with data backing your decision.

---

## Q10. When resize happens?

**Summary:** HashMap resizes (doubles its capacity) when the number of entries exceeds `capacity * loadFactor` — this is called the threshold.

**Practical Explanation:** With default settings (capacity=16, loadFactor=0.75), the threshold is 12. After the 12th entry, `put()` triggers a resize:
1. Create a new `Node[]` array of double the size (16 → 32).
2. Redistribute every existing entry to the new array (rehashing).
3. Replace the old array with the new one.

This is O(n) — every entry must be repositioned. The new index is either the same or `oldIndex + oldCapacity` (because of the power-of-2 optimization).

**When/Why Used:** Resize is automatic and unavoidable as data grows. But knowing it happens helps us prevent it when possible — pre-sizing the HashMap to avoid multiple resizes during bulk operations.

**Real-World Scenario:** Loading 1 million entries into a default HashMap causes ~20 resizes (16→32→64→...→2M). Each resize rehashes all entries. Pre-sizing avoids this:
```java
// Prevents ALL resizes
int expectedSize = 1_000_000;
Map<String, Data> map = new HashMap<>((int)(expectedSize / 0.75) + 1);
```

**Common Mistakes:** Not pre-sizing when the approximate size is known. Also, assuming resize is free — in latency-sensitive applications, a sudden resize of a large HashMap can cause a noticeable latency spike in the request that triggers it.

**Closing:** Resize is the single biggest performance concern with HashMap — pre-sizing is a simple optimization that prevents expensive rehashing during bulk operations.

---

## Q11. What is rehashing?

**Summary:** Rehashing is the process of recalculating bucket indices for all existing entries when the HashMap resizes — every entry is repositioned in the new, larger bucket array.

**Practical Explanation:** When the array doubles, the bitmask changes (e.g., `0b01111` → `0b11111` for 16→32). Each entry's bucket index is recalculated: `newIndex = hash & (newCapacity - 1)`. In Java 8, this is optimized — entries in each old bucket either stay at the same index or move to `oldIndex + oldCapacity`. This is determined by checking a single bit: `hash & oldCapacity`.

**When/Why Used:** Rehashing is automatic during resize. It's needed because the bucket index depends on capacity — when capacity changes, indices change.

**Real-World Scenario:** During a load test, we noticed periodic latency spikes every time the HashMap doubled. Thread dumps showed the application thread stuck in `HashMap.resize()`. Solution: pre-calculate the expected capacity and initialize the HashMap accordingly.

**Common Mistakes:** In Java 7, rehashing under concurrent access could create circular linked lists (infinite loops). Java 8's tail-insertion fixed this. Also, rehashing is why `HashMap` is not efficient for concurrent writes — the entire table is being restructured.

**Closing:** Rehashing is expensive but necessary — understanding its cost justifies the effort of pre-sizing HashMaps in performance-critical code.

---

## Q12. Worst-case complexity Java 7?

**Summary:** In Java 7, HashMap's worst-case time complexity is O(n) — when all keys hash to the same bucket, forming a single long linked list.

**Practical Explanation:** Java 7 uses linked lists for collision resolution. If all `n` keys land in one bucket, every `get()`, `put()`, and `remove()` must traverse the entire list. This degrades HashMap to essentially a linked list — destroying its O(1) promise.

**When/Why Used:** This is a theoretical worst case, but it became a real security concern with HashDoS attacks where attackers crafted inputs to force worst-case collisions.

**Real-World Scenario:** In 2011, the HashDoS attack exploited this — web frameworks parsed query parameters into HashMaps. Attackers sent requests with thousands of parameters that all hashed to the same bucket, causing O(n^2) total processing time (n insertions × O(n) per insertion), overwhelming servers.

**Common Mistakes:** Assuming O(1) is guaranteed — it's only the average case. In Java 7, there's no protection against pathological hash distributions.

**Closing:** Java 7's O(n) worst case is the primary reason Java 8 introduced treeification — reducing worst case to O(log n) was a critical security and performance fix.

---

## Q13. Worst-case complexity Java 8?

**Summary:** In Java 8, HashMap's worst-case time complexity improved to O(log n) thanks to red-black tree conversion when bucket chains exceed 8 nodes.

**Practical Explanation:** When a bucket's linked list grows beyond 8 entries, it converts to a balanced red-black tree. Tree operations (search, insert, delete) are O(log n) in the worst case. So even if all keys collide into one bucket, you get O(log n) instead of O(n).

**When/Why Used:** This improvement is automatic — you benefit from it just by using Java 8+. It provides a safety net against poor hash distributions and malicious inputs.

**Real-World Scenario:** After migrating a service from Java 7 to Java 8, we stress-tested with worst-case hash inputs. Java 7 took 45 seconds for 50,000 colliding inserts. Java 8 completed the same operation in under 1 second — the O(n) to O(log n) improvement is dramatic at scale.

**Common Mistakes:** Thinking O(log n) is the normal case — it's the worst case. Normal operations with good hash distribution are still O(1). Also, the tree requires keys to be comparable for optimal performance — if keys don't implement `Comparable`, the tree uses `identityHashCode` as a tiebreaker, which works but isn't ideal.

**Closing:** O(log n) worst case makes Java 8+ HashMap production-safe against both accidental and malicious hash collisions — it's one of the most important improvements in the collections framework.

---

## Q14. Role of hashCode()?

**Summary:** `hashCode()` determines which bucket an entry goes into — it's the first step in locating where a key-value pair is stored or retrieved.

**Practical Explanation:** When you call `map.put(key, value)` or `map.get(key)`, HashMap:
1. Calls `key.hashCode()`.
2. Applies the spread function: `hash = hashCode ^ (hashCode >>> 16)`.
3. Computes bucket index: `index = hash & (capacity - 1)`.

A good `hashCode()` distributes keys uniformly across buckets. A bad one clusters keys, causing collisions and degrading performance.

**When/Why Used:** Every HashMap operation starts with `hashCode()`. Its quality directly determines HashMap performance. In production, the difference between a good and bad `hashCode()` can be O(1) vs O(n) per operation.

**Real-World Scenario:**
```java
// BAD hashCode — all objects go to same bucket
@Override
public int hashCode() { return 42; } // O(n) everything

// GOOD hashCode — uses all meaningful fields
@Override
public int hashCode() {
    return Objects.hash(id, name, department);
}
```

**Common Mistakes:** Using mutable fields in `hashCode()` — if the field changes after insertion, the entry becomes unreachable (wrong bucket). Inconsistency between `equals()` and `hashCode()` — if two objects are `equals()`, they MUST have the same `hashCode()`. Not overriding `hashCode()` when overriding `equals()`.

**Closing:** `hashCode()` is the performance engine of HashMap — invest time in getting it right, and let Lombok's `@EqualsAndHashCode` or `Objects.hash()` do the heavy lifting.

---

## Q15. Role of equals()?

**Summary:** `equals()` determines key identity within a bucket — after `hashCode()` locates the bucket, `equals()` finds the exact key among potentially colliding entries.

**Practical Explanation:** When multiple keys hash to the same bucket, HashMap traverses the chain and calls `equals()` on each key to find the exact match. The flow is:
1. `hashCode()` → bucket index (which bucket?)
2. `equals()` → key match (which entry in the bucket?)

Without proper `equals()`, HashMap can't distinguish between colliding keys.

**When/Why Used:** Every `get()`, `put()` (checking for existing key), `containsKey()`, and `remove()` uses `equals()` to identify the exact key within a bucket. Both `hashCode()` and `equals()` must be overridden consistently.

**Real-World Scenario:**
```java
// Without equals() override
User u1 = new User(1, "Alice");
User u2 = new User(1, "Alice");
map.put(u1, "admin");
map.get(u2); // Returns null! u1.equals(u2) is false (reference check)

// With equals() override based on id
map.get(u2); // Returns "admin" — correct!
```

**Common Mistakes:** Overriding `equals()` with wrong signature — `equals(User other)` instead of `equals(Object other)` creates an overload, not an override. Not checking `null` and type in `equals()`. Using `equals()` with fields not included in `hashCode()` — violates the contract.

**Closing:** `hashCode()` gets you to the door, `equals()` gets you through it — both must work together for HashMap to function correctly.

---

## Q16. Mutable key problem?

**Summary:** Using mutable objects as HashMap keys is dangerous — if a key's fields change after insertion, its hash changes, making the entry unreachable in its original bucket.

**Practical Explanation:** HashMap stores the entry in a bucket based on the key's `hashCode()` at insertion time. If you mutate the key afterward, `hashCode()` returns a different value, so `get()` looks in a different bucket — the entry is effectively lost. It's still in memory (memory leak) but unretrievable.

**When/Why Used:** This is a critical design consideration. Always use immutable keys — `String`, `Integer`, `Long`, or custom immutable objects. If you must use a mutable key, never modify it while it's in the map.

**Real-World Scenario:**
```java
List<String> key = new ArrayList<>(List.of("a", "b"));
Map<List<String>, String> map = new HashMap<>();
map.put(key, "value");

System.out.println(map.get(key)); // "value" — works

key.add("c"); // MUTATED the key!
System.out.println(map.get(key)); // null — entry is LOST
System.out.println(map.size());   // 1 — entry still exists, unreachable
```

**Common Mistakes:** Using `Date`, `List`, `StringBuilder`, or any mutable object as a HashMap key without understanding the consequences. Also, this is a sneaky cause of memory leaks — entries accumulate but can never be retrieved or removed.

**Closing:** This is one of the most dangerous bugs in Java — always use immutable keys, and make it a code review rule on your team.

---

## Q17. Null key handling?

**Summary:** HashMap allows exactly one null key — it's always stored at bucket index 0 since `null.hashCode()` can't be called.

**Practical Explanation:** HashMap special-cases null keys. When the key is null, it skips `hashCode()` computation and directly uses bucket index 0. Only one null key is allowed — if you `put(null, "a")` then `put(null, "b")`, the value is replaced just like any duplicate key.

**When/Why Used:** Useful when "no key" is a valid state in your data. However, many modern Java developers consider null keys a code smell and prefer `Optional` or explicit sentinel values.

**Real-World Scenario:**
```java
Map<String, String> config = new HashMap<>();
config.put(null, "default_value");  // Stored at bucket 0
config.put("key1", "value1");

String value = config.getOrDefault(someKey, config.get(null));
```

**Common Mistakes:** Assuming all Map implementations allow null keys — `ConcurrentHashMap`, `Hashtable`, and `TreeMap` (if using Comparator that doesn't handle null) do NOT allow null keys. Also, `map.get(key)` returning `null` is ambiguous — does the key not exist, or is the value `null`? Use `containsKey()` to disambiguate.

**Closing:** HashMap's null key support is a design decision, not a recommendation — in production code, avoid null keys and use `ConcurrentHashMap` when thread safety is needed, which disallows nulls entirely.

---

## Q18. Duplicate key insertion?

**Summary:** When you insert a duplicate key, HashMap replaces the old value with the new value and returns the previous value — no duplicate keys are ever stored.

**Practical Explanation:** On `put(key, value)`, HashMap finds the bucket, traverses the chain, and checks `equals()` on each existing key. If a match is found, it updates the value in that node and returns the old value. The key itself is NOT replaced — only the value.

**When/Why Used:** This behavior is fundamental to using HashMap as a lookup table. It's how we update cached values, overwrite configurations, and maintain the latest state for a given key.

**Real-World Scenario:**
```java
Map<String, Integer> wordCount = new HashMap<>();
for (String word : words) {
    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
    // Each put replaces the old count with count + 1
}

// Or using merge (cleaner)
wordCount.merge(word, 1, Integer::sum);
```

**Common Mistakes:** Not checking the return value of `put()` — it returns the previous value (or null if key was new), which is useful for detecting overwrites. Also, using `putIfAbsent()` when you actually want to update existing values, or using `put()` when you want insert-only behavior.

**Closing:** Understanding `put()` semantics — replace-on-duplicate — is basic but critical. Use `putIfAbsent()`, `computeIfAbsent()`, or `merge()` when you need different behavior.

---

## Q19. HashMap thread safety?

**Summary:** HashMap is NOT thread-safe — concurrent reads and writes without external synchronization can cause data corruption, infinite loops (Java 7), and lost updates.

**Practical Explanation:** HashMap has no internal locking. When multiple threads concurrently modify a HashMap:
- Entries can be lost (two threads writing to the same bucket).
- `size` counter can become inconsistent.
- Resize under concurrency can corrupt the internal structure.
- In Java 7, concurrent resize can cause infinite loops (circular linked list).

**When/Why Used:** Use HashMap only in single-threaded contexts or with external synchronization. For concurrent access, use `ConcurrentHashMap`.

**Real-World Scenario:** In a Spring singleton `@Service` bean (shared across all request threads), storing data in a plain `HashMap` field is a concurrency bug:
```java
@Service
public class RateLimiter {
    // BUG — shared mutable state, no synchronization
    private Map<String, Integer> requestCounts = new HashMap<>();

    // FIX
    private Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
}
```

**Common Mistakes:** Thinking "read-only HashMap is safe" — it IS safe if the map is fully populated before any thread reads it AND never modified afterward (effectively immutable). But even `Collections.synchronizedMap()` only synchronizes individual operations — compound operations (check-then-act) still need external locking. Prefer `ConcurrentHashMap`.

**Closing:** Never use HashMap in multi-threaded code with writes — `ConcurrentHashMap` exists for exactly this purpose and should be your default choice.

---

## Q20. Resize issue in Java 7?

**Summary:** In Java 7, concurrent resize of HashMap can cause an infinite loop — two threads simultaneously rehashing a linked list can create a circular reference, causing `get()` to loop forever.

**Practical Explanation:** Java 7 uses head insertion during rehashing. When two threads simultaneously resize:
1. Thread A reads the linked list: A → B → null.
2. Thread B completes resize first, reversing the list: B → A → null.
3. Thread A resumes with stale references, creates: A → B → A (cycle).
4. Any subsequent `get()` hitting this bucket enters an infinite loop — 100% CPU, thread never returns.

**When/Why Used:** This is a well-documented Java 7 bug that caused production outages at many companies. It's the #1 reason never to use HashMap concurrently.

**Real-World Scenario:** A production application's CPU suddenly spiked to 100%. Thread dumps showed a thread stuck in `HashMap.get()` — infinite loop. Root cause: a HashMap in a singleton service was being written to by multiple Tomcat request threads. The fix was simple — switch to `ConcurrentHashMap`.

**Common Mistakes:** Thinking Java 8 makes HashMap thread-safe — Java 8 fixed the infinite loop (tail insertion doesn't cause cycles) but HashMap is still not thread-safe. Concurrent writes can still lose data. Also, this bug only manifests under very specific timing — it can pass all testing and only appear under production load.

**Closing:** This is one of the most infamous bugs in Java history — it's a perfect example of why "it works in testing" doesn't mean "it's correct." Always use `ConcurrentHashMap` for shared maps.

---

## Q21. ConcurrentModificationException?

**Summary:** `ConcurrentModificationException` is thrown when a collection is structurally modified while being iterated — it's a fail-fast mechanism to detect unsafe concurrent modification.

**Practical Explanation:** HashMap maintains an internal `modCount` (modification count). When you create an iterator, it snapshots `modCount`. During iteration, if `modCount` changes (due to `put()`, `remove()`, `clear()`), the iterator throws `ConcurrentModificationException`. This happens even in single-threaded code.

**When/Why Used:** It's a safety net — not a guarantee. It detects most accidental modifications during iteration. "Concurrent" here doesn't necessarily mean multi-threaded — it means "at the same time as iteration."

**Real-World Scenario:**
```java
// THROWS ConcurrentModificationException
for (Map.Entry<String, User> entry : userMap.entrySet()) {
    if (entry.getValue().isInactive()) {
        userMap.remove(entry.getKey()); // Modifies map during iteration!
    }
}

// FIX — use Iterator.remove()
Iterator<Map.Entry<String, User>> it = userMap.entrySet().iterator();
while (it.hasNext()) {
    if (it.next().getValue().isInactive()) {
        it.remove(); // Safe — iterator handles it
    }
}

// OR — use removeIf (Java 8+, cleanest)
userMap.entrySet().removeIf(e -> e.getValue().isInactive());
```

**Common Mistakes:** Catching and ignoring this exception — it indicates a real bug. Thinking it only happens with multi-threading — single-threaded code triggers it too. Also, note that `ConcurrentHashMap` NEVER throws this exception — it uses a weakly consistent iterator.

**Closing:** When you see this exception, don't suppress it — restructure your code to use `Iterator.remove()`, `removeIf()`, or collect keys to remove in a separate pass.

---

## Q22. Fail-fast iterator?

**Summary:** A fail-fast iterator immediately throws `ConcurrentModificationException` when it detects the underlying collection has been structurally modified — it fails quickly rather than producing unpredictable results.

**Practical Explanation:** HashMap, ArrayList, HashSet all use fail-fast iterators. They track `modCount` and check it on every `next()` call. If the count doesn't match the snapshot taken at iterator creation, it throws immediately. This is a "best-effort" mechanism — not guaranteed under concurrent modification.

**When/Why Used:** Fail-fast behavior helps catch bugs early during development. The alternative — silently returning wrong data or skipping entries — would be far worse.

**Real-World Scenario:**
```java
// Fail-fast — HashMap iterator
Map<String, String> map = new HashMap<>();
// modification during iteration → ConcurrentModificationException

// Fail-safe — ConcurrentHashMap iterator
Map<String, String> map = new ConcurrentHashMap<>();
// modification during iteration → works fine (weakly consistent)
```

**Fail-safe alternatives:**
- `ConcurrentHashMap` — uses weakly consistent iterators
- `CopyOnWriteArrayList` — iterates over a snapshot
- `Collections.unmodifiableMap()` — prevents modification entirely

**Common Mistakes:** Relying on fail-fast for thread safety — it's a debugging aid, not a synchronization mechanism. The Javadoc explicitly says fail-fast behavior "cannot be guaranteed" and should only be used to detect bugs. Also, `for-each` loops hide the iterator — the exception can be confusing for developers who don't understand what's happening underneath.

**Closing:** Fail-fast is your friend during development — it surfaces bugs early. In concurrent environments, switch to fail-safe collections like `ConcurrentHashMap`.

---

## Q23. keySet vs entrySet?

**Summary:** `entrySet()` is more efficient for iterating over both keys and values because it avoids a separate `get()` lookup per key — `keySet()` requires an extra hash lookup for each value.

**Practical Explanation:**
- `keySet()` — returns `Set<K>`. To get values, you call `map.get(key)` for each key — that's an extra O(1) hash computation per entry.
- `entrySet()` — returns `Set<Map.Entry<K,V>>`. Each entry already holds both key and value — no additional lookup needed.
- `values()` — returns `Collection<V>`. Use when you only need values.

**When/Why Used:** Always use `entrySet()` when you need both keys and values during iteration. It's faster and more idiomatic.

**Real-World Scenario:**
```java
// SLOWER — extra get() per key
for (String key : map.keySet()) {
    String value = map.get(key); // Redundant hash lookup
    process(key, value);
}

// FASTER — direct access to both key and value
for (Map.Entry<String, String> entry : map.entrySet()) {
    process(entry.getKey(), entry.getValue()); // No extra lookup
}

// MODERN — forEach with lambda
map.forEach((key, value) -> process(key, value));
```

**Common Mistakes:** Using `keySet()` + `get()` out of habit — it's O(1) per `get()` but still unnecessary overhead at scale. In a map with 1 million entries, that's 1 million extra hash computations. Also, modifications to `keySet()` or `entrySet()` reflect back to the map — `keySet().remove(key)` actually removes from the map.

**Closing:** `entrySet()` or `forEach()` — pick one and make it a team standard. There's never a reason to use `keySet()` + `get()` for iteration.

---

## Q24. Memory overhead?

**Summary:** HashMap has significant memory overhead — each entry creates a `Node` object with hash, key, value, and next pointer, plus the bucket array itself has empty slots.

**Practical Explanation:** For each entry, HashMap stores a `Node<K,V>` object containing:
- `int hash` (4 bytes)
- `K key` reference (8 bytes on 64-bit)
- `V value` reference (8 bytes)
- `Node<K,V> next` pointer (8 bytes)
- Object header (~16 bytes)

Total: ~44-48 bytes per Node, plus the actual key and value objects. The bucket array (`Node[]`) with default 0.75 load factor means ~25% of buckets are always empty.

**When/Why Used:** Memory awareness matters when storing millions of entries. A HashMap with 1 million entries uses ~50-80 MB just for the HashMap structure (excluding key/value objects). For memory-critical applications, consider alternatives.

**Real-World Scenario:** An in-memory cache with 10 million String-to-String entries consumed over 1 GB of heap just for the HashMap overhead. We switched to an off-heap solution (Chronicle Map) for the hot data and kept HashMap only for smaller working sets.

**Common Mistakes:** Not accounting for Node overhead when estimating memory — developers calculate `keys + values` and forget the per-entry Node cost. Also, small HashMaps (5-10 entries) have disproportionate overhead — the minimum bucket array is 16 slots. For very small maps, consider `Map.of()` (unmodifiable, optimized) or even simple arrays.

**Closing:** HashMap trades memory for speed — know the tradeoff, and for large-scale data, evaluate alternatives like EnumMap, array-based maps, or off-heap solutions.

---

## Q25. HashMap vs LinkedHashMap?

**Summary:** `HashMap` has no ordering guarantees; `LinkedHashMap` maintains insertion order (or access order) by linking entries with a doubly-linked list — at a small memory cost.

**Practical Explanation:**
| Feature | HashMap | LinkedHashMap |
|---------|---------|---------------|
| Order | No guarantee | Insertion order (default) or access order |
| Performance | Slightly faster | ~5-10% overhead for linked list maintenance |
| Memory | Less | Extra prev/next pointers per entry |
| Use case | General purpose | When order matters |

LinkedHashMap extends HashMap and adds `before`/`after` pointers to each entry, forming a doubly-linked list. Access-order mode (`new LinkedHashMap<>(16, 0.75f, true)`) moves accessed entries to the tail — perfect for LRU caches.

**When/Why Used:** JSON serialization where field order matters, maintaining config property order, implementing LRU caches, or when iteration order must match insertion order.

**Real-World Scenario:**
```java
// LRU Cache using LinkedHashMap
Map<String, Data> lruCache = new LinkedHashMap<>(100, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, Data> eldest) {
        return size() > MAX_CACHE_SIZE; // Auto-evict oldest
    }
};
```

**Common Mistakes:** Using HashMap and then wondering why JSON output has random field order. Assuming LinkedHashMap is significantly slower — the overhead is minimal. Forgetting access-order mode exists — it's the secret sauce for LRU caching.

**Closing:** Default to HashMap, but reach for LinkedHashMap whenever order matters — it's a drop-in replacement with predictable iteration.

---

## Q26. HashMap vs TreeMap?

**Summary:** `HashMap` is O(1) unordered; `TreeMap` is O(log n) and keeps keys sorted — choose based on whether you need sorted iteration or pure speed.

**Practical Explanation:**
| Feature | HashMap | TreeMap |
|---------|---------|---------|
| Ordering | None | Natural order or Comparator |
| get/put | O(1) average | O(log n) always |
| Null keys | One allowed | Not allowed (with natural ordering) |
| Implementation | Hash table | Red-black tree |
| Memory | Node + array | TreeNode (left/right/parent/color) |

**When/Why Used:** HashMap for general-purpose key-value storage (99% of cases). TreeMap when you need sorted keys, range queries (`subMap()`, `headMap()`, `tailMap()`), or first/last operations (`firstKey()`, `lastKey()`).

**Real-World Scenario:**
```java
// TreeMap for sorted leaderboard
TreeMap<Integer, String> leaderboard = new TreeMap<>(Comparator.reverseOrder());
leaderboard.put(score, playerName);

// Get top 10 players
leaderboard.entrySet().stream().limit(10).forEach(System.out::println);

// Range query — scores between 80 and 100
SortedMap<Integer, String> topScorers = leaderboard.subMap(80, 101);
```

**Common Mistakes:** Using TreeMap when you don't need sorting — O(log n) is slower than O(1) for every operation. Using HashMap then sorting keys separately when TreeMap gives you sorted iteration for free. Not implementing `Comparable` or providing a `Comparator` — `TreeMap` throws `ClassCastException` at runtime.

**Closing:** HashMap is the default; TreeMap is specialized — use it when you genuinely need sorted keys or range operations.

---

## Q27. HashMap vs Hashtable?

**Summary:** `Hashtable` is synchronized and legacy (Java 1.0); `HashMap` is unsynchronized and modern — there's virtually no reason to use `Hashtable` in modern Java.

**Practical Explanation:**
| Feature | HashMap | Hashtable |
|---------|---------|-----------|
| Thread-safe | No | Yes (synchronized) |
| Null key/value | Allowed | NOT allowed |
| Performance | Faster | Slower (synchronization overhead) |
| Iterator | Fail-fast | Fail-fast (Enumerator is fail-safe) |
| Inheritance | AbstractMap | Dictionary (legacy) |
| Since | Java 1.2 | Java 1.0 |

**When/Why Used:** Never use `Hashtable` in new code. For thread-safe maps, use `ConcurrentHashMap` — it's faster than `Hashtable` because it uses fine-grained locking instead of synchronizing every method.

**Real-World Scenario:** Legacy codebases sometimes still have `Hashtable`. During modernization, we replace:
```java
// Legacy
Hashtable<String, String> config = new Hashtable<>();

// Modern — if thread safety needed
ConcurrentHashMap<String, String> config = new ConcurrentHashMap<>();

// Modern — if single-threaded
HashMap<String, String> config = new HashMap<>();
```

**Common Mistakes:** Using `Hashtable` "for safety" — `ConcurrentHashMap` is both safer and faster. Using `Collections.synchronizedMap(new HashMap<>())` — it's basically the same as Hashtable (method-level synchronization) and has the same compound-operation problems.

**Closing:** `Hashtable` is dead — if you see it in code, it's a refactoring opportunity. `ConcurrentHashMap` is its modern, superior replacement.

---

## Q28. Why HashMap not synchronized?

**Summary:** HashMap is not synchronized by design — adding synchronization to every method would penalize the majority of use cases (single-threaded) for the minority (multi-threaded).

**Practical Explanation:** Most HashMap usage is thread-local — within a method, within a request, within a single thread. Adding `synchronized` to every `get()` and `put()` adds monitor acquisition/release overhead even when no contention exists. Java's philosophy is "pay only for what you use."

**When/Why Used:** When the JDK team redesigned collections in Java 1.2, they made the deliberate choice to unsynchronize everything (replacing `Vector` with `ArrayList`, `Hashtable` with `HashMap`). Developers opt-in to synchronization only when needed.

**Real-World Scenario:** A benchmarked comparison on a single thread:
- `HashMap.get()`: ~10-20 nanoseconds
- `Hashtable.get()`: ~40-60 nanoseconds
- `ConcurrentHashMap.get()`: ~15-25 nanoseconds (CAS, no lock for reads)

In a loop of 10 million operations, that synchronization overhead adds up to hundreds of milliseconds for zero benefit.

**Common Mistakes:** Wrapping HashMap with `Collections.synchronizedMap()` as a reflex — it adds the same overhead as Hashtable. Better to analyze: if reads dominate, `ConcurrentHashMap` is ideal. If you need a thread-local map, plain `HashMap` is fastest.

**Closing:** Non-synchronized by default was the right design call — it lets developers choose the appropriate concurrency strategy rather than paying a universal performance tax.

---

## Q29. Why ConcurrentHashMap?

**Summary:** `ConcurrentHashMap` provides thread-safe, high-performance concurrent access without locking the entire map — it's the standard choice for shared maps in multi-threaded Java applications.

**Practical Explanation:** Unlike `Hashtable` (which locks the entire map for every operation), `ConcurrentHashMap` uses fine-grained locking:
- **Java 7:** Segment-based locking (16 segments by default, 16 concurrent writers).
- **Java 8+:** Bucket-level locking with CAS (Compare-And-Swap) for lock-free reads, synchronized only on individual buckets for writes.

Reads are completely lock-free (volatile reads). This means hundreds of threads can read concurrently without blocking.

**When/Why Used:** Any shared Map in a multi-threaded application — caches, rate limiters, session stores, metrics counters, shared configuration.

**Real-World Scenario:**
```java
@Service
public class RateLimiter {
    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    public boolean allowRequest(String clientId) {
        AtomicInteger count = counters.computeIfAbsent(clientId, k -> new AtomicInteger(0));
        return count.incrementAndGet() <= MAX_REQUESTS_PER_MINUTE;
    }
}
```

**Common Mistakes:** Using compound operations non-atomically — `if (!map.containsKey(key)) map.put(key, value)` is NOT atomic even with `ConcurrentHashMap`. Use `putIfAbsent()` or `computeIfAbsent()` instead. Also, iterators are weakly consistent — they reflect some but not necessarily all updates made after iterator creation.

**Closing:** `ConcurrentHashMap` is one of the most important classes in `java.util.concurrent` — it should be your default choice for any shared map.

---

## Q30. Java 7 ConcurrentHashMap?

**Summary:** Java 7's `ConcurrentHashMap` uses segment-based locking — the map is divided into 16 segments, each independently lockable, allowing up to 16 concurrent write operations.

**Practical Explanation:** The internal structure is an array of `Segment` objects, each being essentially a mini-HashMap with its own lock. When a thread writes, it only locks the specific segment that contains the target bucket. Other segments remain accessible to other threads.

- Default: 16 segments (concurrencyLevel=16).
- Each segment has its own `HashEntry[]` array, count, and threshold.
- Reads are lock-free (using volatile reads on `HashEntry.value`).
- Writes lock only the target segment.

**When/Why Used:** This was the gold standard for concurrent maps from Java 5 through Java 7. The segment approach was a significant improvement over `Hashtable`'s single-lock model.

**Real-World Scenario:** With 16 segments, up to 16 threads can write simultaneously (as long as they target different segments). In a web server with 200 request threads, this meant far less contention than `Hashtable`, though still some blocking when two threads targeted the same segment.

**Common Mistakes:** Assuming `size()` is exact — it requires locking all segments and can be expensive. Setting `concurrencyLevel` too high wastes memory. Also, segment-based locking was replaced in Java 8 because it had memory overhead (each segment is a full ReentrantLock) and the fixed 16-segment limit created uneven distribution.

**Closing:** Java 7's segment locking was clever but had limitations — Java 8's per-bucket approach is strictly better in both memory usage and concurrency.

---

## Q31. Java 8 ConcurrentHashMap?

**Summary:** Java 8's `ConcurrentHashMap` dropped segment-based locking in favor of per-bucket `synchronized` blocks + CAS operations — offering finer granularity, better memory efficiency, and higher concurrency.

**Practical Explanation:** Key changes in Java 8:
- **No more Segments** — the internal structure is a flat `Node[]` array, same as HashMap.
- **Reads:** Completely lock-free using `volatile` reads (Unsafe/VarHandle).
- **Writes:** CAS (Compare-And-Swap) for the first node in an empty bucket; `synchronized` on the first node for existing buckets.
- **Treeification:** Same as HashMap — buckets with 8+ nodes convert to red-black trees.
- **Concurrency:** Can scale to the number of buckets (thousands+), not limited to 16.

**When/Why Used:** This is the current implementation in Java 8+ and is significantly better than Java 7's version. More concurrent writers, less memory overhead, and better scalability.

**Real-World Scenario:**
```java
// Lock-free read
Node<K,V> e = tabAt(tab, i); // volatile read, no lock

// CAS for empty bucket
casTabAt(tab, i, null, newNode); // atomic, no lock

// Synchronized only when bucket already occupied
synchronized (firstNodeInBucket) {
    // insert/update within this bucket only
}
```

**Common Mistakes:** Thinking `ConcurrentHashMap` serializes all writes — it only blocks writes to the SAME bucket. Two threads writing to different buckets proceed fully in parallel. Also, `size()` is still an approximation under concurrent modification — use `mappingCount()` for a `long` result on very large maps.

**Closing:** Java 8's `ConcurrentHashMap` is a masterclass in concurrent data structure design — per-bucket locking + CAS gives near-HashMap performance with full thread safety.

---

## Q32. Why no null in ConcurrentHashMap?

**Summary:** `ConcurrentHashMap` disallows null keys and values because `null` creates ambiguity in concurrent contexts — `get()` returning `null` could mean "key not found" or "value is null," and you can't distinguish these atomically.

**Practical Explanation:** With `HashMap`, you can call `containsKey()` after `get()` to check. But in a concurrent map, between your `get()` and `containsKey()` calls, another thread might insert or remove the key — the result is unreliable. Doug Lea (the author) chose to eliminate this ambiguity entirely by banning null.

```java
// With HashMap (single-threaded, safe)
if (map.get(key) == null) {
    if (map.containsKey(key)) { /* value is null */ }
    else { /* key doesn't exist */ }
}

// With ConcurrentHashMap — between get() and containsKey(),
// another thread might change the map! Ambiguous and unsafe.
```

**When/Why Used:** This is a design decision you must know when migrating from `HashMap` to `ConcurrentHashMap`. If your code stores null values, you'll need to use a sentinel object instead.

**Real-World Scenario:**
```java
// Migration from HashMap to ConcurrentHashMap
// Before
map.put("config", null); // null means "use default"

// After — use sentinel
private static final String USE_DEFAULT = "__DEFAULT__";
concurrentMap.put("config", USE_DEFAULT);
```

**Common Mistakes:** Attempting `concurrentMap.put(key, null)` — throws `NullPointerException` at runtime, not compile time. Not realizing that `HashMap.getOrDefault()` and `ConcurrentHashMap.getOrDefault()` behave differently with null values.

**Closing:** The no-null rule in `ConcurrentHashMap` is a conscious trade-off — clarity over flexibility. It forces cleaner API design and eliminates an entire category of concurrency bugs.

---

## Q33. computeIfAbsent use?

**Summary:** `computeIfAbsent()` atomically checks if a key exists and computes the value only if absent — it's the thread-safe, idiomatic way to implement lazy initialization in a Map.

**Practical Explanation:** `map.computeIfAbsent(key, k -> expensiveComputation(k))` does:
1. Check if key exists.
2. If absent, compute the value using the provided function.
3. Store and return it.
4. If present, return the existing value.

In `ConcurrentHashMap`, this entire operation is atomic — no race condition between check and insert.

**When/Why Used:** Caching, multimap construction, lazy initialization, memoization — any "get or create" pattern.

**Real-World Scenario:**
```java
// Building a multimap (key → list of values)
Map<String, List<Order>> ordersByCustomer = new HashMap<>();
for (Order order : orders) {
    ordersByCustomer.computeIfAbsent(order.getCustomerId(), k -> new ArrayList<>())
        .add(order);
}

// Thread-safe cache with ConcurrentHashMap
ConcurrentHashMap<String, ExpensiveObject> cache = new ConcurrentHashMap<>();
ExpensiveObject obj = cache.computeIfAbsent(key, k -> createExpensiveObject(k));
// Guaranteed: createExpensiveObject called at most once per key
```

**Common Mistakes:** Confusing `computeIfAbsent()` with `putIfAbsent()` — `putIfAbsent()` requires you to compute the value upfront (even if it won't be used), while `computeIfAbsent()` only computes when needed. Also, the mapping function should not modify the map itself — it can cause deadlock in `ConcurrentHashMap`.

**Closing:** `computeIfAbsent()` is one of the most useful Map methods — it replaces the verbose check-then-put pattern with a single atomic, readable call.

---

## Q34. putIfAbsent use?

**Summary:** `putIfAbsent()` inserts a key-value pair only if the key is not already present — it's the atomic "insert if new" operation that prevents overwriting existing values.

**Practical Explanation:** `map.putIfAbsent(key, value)` does:
- If key doesn't exist → inserts the pair and returns `null`.
- If key exists → does nothing and returns the existing value.

Unlike `computeIfAbsent()`, the value is computed eagerly — even if the key already exists, you pay the cost of creating the value.

**When/Why Used:** Setting default values, first-write-wins semantics, preventing duplicate registrations.

**Real-World Scenario:**
```java
// Register handler only if not already registered
ConcurrentHashMap<String, EventHandler> handlers = new ConcurrentHashMap<>();
EventHandler existing = handlers.putIfAbsent("orderCreated", new OrderHandler());
if (existing != null) {
    log.warn("Handler already registered for orderCreated");
}

// vs computeIfAbsent — value computed lazily
handlers.computeIfAbsent("orderCreated", event -> new OrderHandler());
// OrderHandler only created if key is absent (more efficient)
```

**Common Mistakes:** Using `putIfAbsent()` when the value is expensive to create — use `computeIfAbsent()` instead for lazy computation. Not using the return value — it tells you whether the insert happened. Also, `putIfAbsent()` returns `null` on success, which is unintuitive.

**Closing:** Use `putIfAbsent()` when you already have the value in hand; use `computeIfAbsent()` when the value is expensive to compute or should only be created on demand.

---

## Q35. HashMap in caching?

**Summary:** HashMap is commonly used as a simple in-memory cache for fast O(1) key-based lookups — but production caches need eviction, expiration, and thread-safety on top.

**Practical Explanation:** At its core, a cache is a key-value store with fast lookups — exactly what HashMap provides. But a raw HashMap lacks eviction (unbounded growth), TTL (entries never expire), thread-safety, and metrics. Production caches use specialized libraries built on similar principles.

**When/Why Used:** Simple, single-threaded, bounded caches for method-local lookups. For anything more, use Caffeine, Guava Cache, or distributed caches (Redis, Hazelcast).

**Real-World Scenario:**
```java
// Simple in-memory cache (suitable for small, static data)
private final Map<String, Config> configCache = new HashMap<>();

public Config getConfig(String key) {
    return configCache.computeIfAbsent(key, k -> loadFromDatabase(k));
}

// Production cache — use Caffeine
Cache<String, Config> cache = Caffeine.newBuilder()
    .maximumSize(10_000)
    .expireAfterWrite(Duration.ofMinutes(5))
    .recordStats()
    .build();
```

**Common Mistakes:** Using a plain HashMap as a production cache without eviction — it grows unbounded and causes OOM. Not considering thread safety — singleton service + HashMap + concurrent requests = data corruption. Not adding TTL — stale data served indefinitely.

**Closing:** HashMap is the foundation of caching, but always use a proper caching library (Caffeine is the gold standard) for production systems.

---

## Q36. LRU cache role?

**Summary:** An LRU (Least Recently Used) cache evicts the least recently accessed entry when the cache is full — it's implemented using a HashMap for O(1) lookup combined with a doubly-linked list for O(1) eviction ordering.

**Practical Explanation:** The structure:
- **HashMap** maps keys to linked list nodes → O(1) lookup.
- **Doubly-linked list** tracks access order → the head is least recently used, tail is most recently used.
- On `get()`: move the node to the tail (most recent).
- On `put()` when full: remove the head node (least recent) and insert the new node at tail.

All operations are O(1).

**When/Why Used:** Database query result caching, API response caching, session management, connection pooling — anywhere you have limited memory and want to keep the most useful data.

**Real-World Scenario:**
```java
// Simple LRU using LinkedHashMap
Map<String, Data> lruCache = new LinkedHashMap<>(maxSize, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, Data> eldest) {
        return size() > maxSize;
    }
};

// Or implement from scratch (common interview question)
class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, Node<K, V>> map = new HashMap<>();
    private final DoublyLinkedList<K, V> list = new DoublyLinkedList<>();
    // get(), put() both O(1)
}
```

**Common Mistakes:** Implementing LRU with only a HashMap (no ordering) or only a LinkedList (O(n) lookup). Using `Collections.synchronizedMap(LinkedHashMap)` for concurrent LRU — use Caffeine instead. Not considering cache sizing properly — too small = poor hit rate, too large = memory waste.

**Closing:** LRU is one of the most common interview questions AND one of the most practical data structures — knowing how HashMap + doubly-linked list work together is essential.

---

## Q37. Memory leak via HashMap?

**Summary:** HashMap can cause memory leaks when entries accumulate but are never removed — strong references to keys/values prevent garbage collection, even when the data is logically no longer needed.

**Practical Explanation:** Common memory leak patterns with HashMap:
1. **Unbounded growth** — entries added but never removed (missing eviction policy).
2. **Mutable keys** — mutated after insertion, become unreachable but not GC'd.
3. **Forgotten references** — static/singleton HashMap holds references to objects that should have been released.
4. **Listener/callback maps** — registrations without deregistrations.

**When/Why Used:** Memory leak detection is a critical debugging skill. Use heap dump analysis (Eclipse MAT, JProfiler) to identify retained objects.

**Real-World Scenario:**
```java
// MEMORY LEAK — session map never cleaned up
@Service
public class SessionManager {
    private final Map<String, UserSession> sessions = new HashMap<>();

    public void createSession(String token, UserSession session) {
        sessions.put(token, session); // Entries added...
    }
    // No removeSession() or expiration! Map grows forever.

    // FIX — use WeakHashMap or add TTL-based eviction
    private final Map<String, UserSession> sessions =
        Collections.synchronizedMap(new WeakHashMap<>());
    // Or better: use Caffeine with expireAfterAccess
}
```

**Common Mistakes:** Using `static HashMap` without lifecycle management. Not profiling memory in production. Thinking `WeakHashMap` solves everything — it only weakly references keys, not values (values are strongly referenced). Also, `ThreadLocal` + thread pools = classic leak — entries survive across requests.

**Closing:** Memory leaks with HashMap are silent killers — the application degrades over days/weeks until OOM. Regular heap dump analysis and proper eviction policies are your defense.

---

## Q38. Optimize HashMap?

**Summary:** Optimize HashMap by setting proper initial capacity to avoid resizing, writing good `hashCode()` methods to minimize collisions, and choosing the right load factor.

**Practical Explanation:** Key optimization strategies:
1. **Initial capacity:** If you know the expected size, set `capacity = expectedSize / loadFactor + 1`. Prevents expensive resize operations.
2. **hashCode() quality:** Use all meaningful fields, distribute evenly. `Objects.hash()` is reliable. Avoid `return constant`.
3. **Load factor:** 0.75 is optimal for most cases. Lower means fewer collisions but more memory.
4. **Key type:** Use immutable keys with good hash distribution — `String`, `Integer`, `Long`.

**When/Why Used:** Optimization matters for large HashMaps (100K+ entries) or hot-path code where HashMap operations are called millions of times.

**Real-World Scenario:**
```java
// Before optimization — 20 resizes during bulk load
Map<String, Data> map = new HashMap<>(); // default capacity 16
for (int i = 0; i < 1_000_000; i++) {
    map.put(keys[i], data[i]);
}

// After optimization — zero resizes
Map<String, Data> map = new HashMap<>(1_400_000); // 1M / 0.75 + buffer
for (int i = 0; i < 1_000_000; i++) {
    map.put(keys[i], data[i]);
}
// Result: ~30% faster bulk loading, no GC pressure from discarded arrays
```

**Common Mistakes:** Over-optimizing small maps (under 1000 entries) — the JVM handles them fine with defaults. Micro-tuning load factor without profiling. Using `HashMap.size()` to calculate new HashMap capacity — you need to account for load factor.

**Closing:** Pre-sizing and good `hashCode()` are the two biggest wins — they're simple to implement and yield measurable performance improvements for large datasets.

---

## Q39. Millions of records?

**Summary:** When storing millions of records in a HashMap, pre-size the capacity, ensure excellent hash distribution, tune JVM heap and GC, and consider whether HashMap is still the right data structure.

**Practical Explanation:** At scale, every per-entry overhead multiplies:
- 10M entries × ~48 bytes/Node = ~480 MB just for Nodes.
- Plus keys + values + bucket array.
- GC pressure increases significantly.

Strategies:
1. **Pre-size:** `new HashMap<>((int)(expectedSize / 0.75) + 1)` — avoid all resizes.
2. **Tune GC:** Use G1GC or ZGC, increase heap (`-Xmx`).
3. **Minimize object creation:** Use primitive-specialized maps (Eclipse Collections `IntObjectHashMap`, Koloboke).
4. **Consider alternatives:** Off-heap maps (Chronicle Map), databases, Redis.

**When/Why Used:** Big data processing, large in-memory caches, analytics, batch jobs processing millions of records.

**Real-World Scenario:**
```java
// Storing 50M user sessions — standard HashMap is too expensive
// Option 1: Primitive-specialized map (avoids boxing)
IntObjectHashMap<Session> sessions = new IntObjectHashMap<>(50_000_000);

// Option 2: Off-heap (Chronicle Map)
ChronicleMap<Long, Session> sessions = ChronicleMap
    .of(Long.class, Session.class)
    .entries(50_000_000)
    .averageValueSize(256)
    .createPersistedTo(new File("/data/sessions.dat"));

// Option 3: Just use Redis
// sessions stored externally, no heap pressure
```

**Common Mistakes:** Loading everything into memory when only a subset is actively used — consider pagination or lazy loading. Not monitoring GC — large HashMaps cause long GC pauses. Auto-boxing primitives as keys (`int` → `Integer`) wastes significant memory at scale.

**Closing:** At millions of records, you're doing capacity planning, not just coding — the right answer often involves architectural decisions beyond just HashMap configuration.

---

## Q40. HashMap vs Redis?

**Summary:** HashMap is an in-memory, single-JVM data structure; Redis is a distributed, persistent, feature-rich external cache — use HashMap for local, single-instance data and Redis for shared, distributed state.

**Practical Explanation:**
| Feature | HashMap | Redis |
|---------|---------|-------|
| Scope | Single JVM | Distributed across network |
| Persistence | Lost on restart | Configurable (RDB/AOF) |
| Data types | Key-Value only | Strings, Lists, Sets, Hashes, Sorted Sets, Streams |
| TTL/Expiration | Manual | Built-in |
| Memory limit | JVM heap | Configurable, eviction policies |
| Network | No network hop | Network latency (~0.5-1ms) |
| Scalability | Single node | Clustering, replication, sentinel |

**When/Why Used:**
- **HashMap:** Local caching within a single service instance, method-local computations, small lookup tables that don't need sharing.
- **Redis:** Session management across multiple instances, distributed rate limiting, shared cache invalidation, pub/sub messaging, leaderboards.

**Real-World Scenario:**
```java
// L1 cache — HashMap (local, fastest)
private final Map<String, Product> l1Cache = new ConcurrentHashMap<>();

// L2 cache — Redis (shared across instances)
@Cacheable(value = "products", key = "#productId")
public Product getProduct(String productId) {
    return productRepository.findById(productId);
}

// Multi-level caching strategy
public Product getProduct(String id) {
    Product p = l1Cache.get(id);           // ~10ns, local
    if (p == null) {
        p = redisTemplate.opsForValue().get("product:" + id);  // ~1ms, network
        if (p == null) {
            p = productRepository.findById(id);  // ~10ms, database
            redisTemplate.opsForValue().set("product:" + id, p, Duration.ofMinutes(30));
        }
        l1Cache.put(id, p);
    }
    return p;
}
```

**Common Mistakes:** Using HashMap for data that needs to survive restarts — it's purely in-memory. Using Redis for data that never needs sharing — unnecessary network latency and infrastructure complexity. Not considering cache invalidation across instances — local HashMap caches can serve stale data when another instance updates the source.

**Closing:** HashMap and Redis are complementary, not competitors — the best architectures use both in a tiered caching strategy (L1 local + L2 distributed).

---

*End of HashMap Deep Dive — 40 Interview Q&A*
