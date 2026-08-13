================================================================================
                              HASHMAP
================================================================================

Persona: Senior Java Backend Engineer with 5+ years of real-world experience.
Tone: Confident, practical, senior-level.

Each answer covers: deep internals, multiple code examples, production scenarios,
follow-up points, edge cases, and a confident closing line.

--------------------------------------------------------------------------------
Q5: What is HashMap?
--------------------------------------------------------------------------------

HashMap is the most commonly used key-value data structure in Java, and honestly,
it is probably the single most important class you need to understand deeply as a
backend engineer. It provides average O(1) time for get and put operations by
combining an array-based structure with a hashing mechanism, and I have used it
in virtually every production system I have worked on — from caching layers to
request routing to configuration management.

DEEP TECHNICAL EXPLANATION:

Internally, HashMap maintains a transient array called table of type Node<K,V>[].
Each Node is a static inner class containing four fields: final int hash,
final K key, V value, and Node<K,V> next. This next pointer is what forms the
linked list chain when collisions happen at the same bucket index.

When you call put(key, value), the first thing HashMap does is compute a hash.
But it does NOT simply use key.hashCode(). Instead, it applies a hash spreading
function:

    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

This XORs the upper 16 bits with the lower 16 bits. The reason is brilliant —
since the bucket index is calculated as (n - 1) & hash (where n is the array
length), and n is always a power of 2, only the lower bits of the hash actually
participate in index calculation. Without spreading, keys whose hashCode values
differ only in their upper bits would collide in the same bucket. This spreading
ensures that information from the upper bits influences the lower bits.

The index calculation (n - 1) & hash is equivalent to hash % n, but bitwise AND
is significantly faster than modulo division. This is precisely why HashMap
capacity MUST always be a power of 2. If you pass a non-power-of-2 value to the
constructor, the tableSizeFor() method rounds it up:

    static final int tableSizeFor(int cap) {
        int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

The default initial capacity is 16 (DEFAULT_INITIAL_CAPACITY = 1 << 4), and the
default load factor is 0.75 (DEFAULT_LOAD_FACTOR = 0.75f). The threshold for
resizing is capacity * loadFactor. So with defaults, the first resize triggers
when size exceeds 12 (16 * 0.75).

When a resize occurs, HashMap creates a new array of double the size and rehashes
every entry. In Java 8, the rehashing is optimized — since the new capacity is
double, each node either stays at the same index or moves to index + oldCapacity.
This is determined by checking a single bit: (e.hash & oldCap). If that bit is 0,
the node stays; if 1, it moves. This avoids recalculating the full index.

Null key handling: HashMap allows exactly one null key. When key is null, the hash
function returns 0, so the null key always goes to bucket index 0. This is
different from Hashtable and ConcurrentHashMap, which do NOT allow null keys.

REAL-WORLD PRODUCTION SCENARIOS:

1. In a payment gateway system I worked on, we used HashMap to cache merchant
   configurations loaded from the database on startup. The map was keyed by
   merchantId (String) and held the full MerchantConfig object. We initialized
   it with new HashMap<>(512, 0.75f) because we knew we had roughly 350 active
   merchants, and 512 * 0.75 = 384, avoiding an unnecessary resize.

2. In an event-driven microservice, we used HashMap as a local routing table
   mapping event types (enums) to handler functions. Since enums have excellent
   hashCode implementations and the set was fixed at startup, HashMap gave us
   O(1) dispatch with zero collisions.

3. In a high-throughput order processing system, we encountered a performance
   degradation because a developer initialized HashMap with default capacity
   for a map that would eventually hold 50,000 entries. This caused 12 resize
   operations (16 -> 32 -> 64 -> ... -> 65536), each requiring a full rehash.
   Pre-sizing to new HashMap<>(65536) or even new HashMap<>(70000) (which
   tableSizeFor rounds to 131072) eliminated the resizes entirely.

CODE EXAMPLES:

Example 1 — Correct Usage with Pre-Sizing:

    // We know we will store approximately 1000 entries
    // capacity needed: 1000 / 0.75 = 1334, next power of 2 = 2048
    Map<String, UserSession> sessionCache = new HashMap<>(2048);

    // Or use Guava's helper which does the math for you:
    Map<String, UserSession> sessionCache = Maps.newHashMapWithExpectedSize(1000);

Example 2 — Incorrect Usage (Common Mistake):

    // BAD: Using mutable object as key without proper equals/hashCode
    Map<List<String>, String> badMap = new HashMap<>();
    List<String> key = new ArrayList<>(Arrays.asList("a", "b"));
    badMap.put(key, "value1");
    key.add("c"); // Mutating the key changes its hashCode!
    System.out.println(badMap.get(key)); // Returns null! Entry is lost.

    // The entry is still in the map but at the OLD bucket index.
    // The new hashCode maps to a different bucket, so get() cannot find it.

Example 3 — Examining Internal State via Reflection (Debugging):

    Map<String, Integer> map = new HashMap<>();
    for (int i = 0; i < 20; i++) {
        map.put("key" + i, i);
    }

    // Use reflection to inspect the internal table
    Field tableField = HashMap.class.getDeclaredField("table");
    tableField.setAccessible(true);
    Object[] table = (Object[]) tableField.get(map);
    System.out.println("Internal array length: " + table.length); // 32
    // After inserting 20 entries: threshold was 16*0.75=12, so resize happened
    // at 13th entry. New capacity = 32, threshold = 32*0.75 = 24.

    int nonNullBuckets = 0;
    for (Object node : table) {
        if (node != null) nonNullBuckets++;
    }
    System.out.println("Occupied buckets: " + nonNullBuckets);

COMMON MISTAKES, EDGE CASES, AND GOTCHAS:

1. Not overriding hashCode when overriding equals: This is the single most
   common HashMap bug. Two objects can be equals() but land in different buckets
   because they have different hashCode values. The contract states: if
   a.equals(b), then a.hashCode() == b.hashCode(). Violating this means put()
   and get() use different buckets, and your entries silently vanish.

2. Using float or double as keys: Floating-point equality is unreliable.
   0.1 + 0.2 != 0.3 in IEEE 754, so these make terrible HashMap keys. Use
   BigDecimal with explicit scale if you need decimal keys.

3. Forgetting that iteration order is NOT guaranteed: HashMap does not maintain
   insertion order. If you need insertion order, use LinkedHashMap. If you need
   sorted order, use TreeMap. I have seen bugs in production where code relied
   on HashMap iterating in a particular order and it worked on Java 8 but broke
   after upgrading to Java 11 because the internal hashing changed slightly.

4. load factor misconceptions: Setting load factor to 1.0 does NOT mean "fill
   every bucket." It means "resize when size equals capacity." You can still
   have collisions with load factor 1.0. A load factor below 0.75 wastes memory
   for marginal speed gain. A load factor above 0.75 increases collision chains.
   The default 0.75 is a well-researched sweet spot.

5. Resizing under high load: Each resize is O(n) because every entry must be
   rehashed and redistributed. In a latency-sensitive service, an unexpected
   resize of a large map can cause a latency spike. I have seen P99 latency
   jump from 5ms to 200ms during a resize of a 500K-entry map.

6. Serialization pitfall: HashMap implements Serializable, but the internal
   table is transient. When deserialized, it rebuilds from the entries. If the
   hashCode implementation is different on the deserializing JVM (different
   String hash seed, for example), entries may end up in different buckets.
   This is by design but can cause confusion.

FOLLOW-UP POINTS THE INTERVIEWER MIGHT ASK:

- "What is the time complexity of HashMap in the worst case?" — Before Java 8,
  worst case for get() was O(n) when all entries hash to the same bucket. In
  Java 8+, it is O(log n) because long chains are converted to Red-Black Trees.

- "Why is the default load factor 0.75 and not 0.5 or 1.0?" — It is a tradeoff
  between space and time. At 0.75, the average chain length under ideal hashing
  is about 0.5 entries per bucket, which keeps lookups fast while not wasting
  too much memory on empty buckets.

- "Can HashMap keys be null?" — Yes, exactly one null key is allowed. It always
  goes to bucket 0. Multiple null keys overwrite each other. Null values are
  also allowed for any number of keys.

- "What happens if two threads put into a HashMap simultaneously?" — You get
  undefined behavior: lost updates, corrupted internal state, and in Java 7,
  potentially an infinite loop during resize. This is covered in Q7.

HashMap is foundational. If you understand it at this level — the hash spreading,
power-of-2 capacity, resize mechanics, and practical pre-sizing — you demonstrate
that you think about performance at the systems level, not just at the API level.

--------------------------------------------------------------------------------
Q6: How does HashMap handle collisions?
--------------------------------------------------------------------------------

Hash collisions are inevitable in any hash-based data structure, and HashMap
handles them through a dual strategy: chaining with linked lists for short chains,
and automatic conversion to Red-Black Trees when chains grow long. This dual
approach was introduced in Java 8 and is one of the most significant internal
improvements to the Collections framework. I have encountered real collision
scenarios in production, and understanding this mechanism is critical for
diagnosing performance problems.

DEEP TECHNICAL EXPLANATION:

A collision occurs when two different keys produce the same bucket index after
the hash spreading and bitwise AND: (hash1 & (n-1)) == (hash2 & (n-1)). Note
that this does NOT require equal hashCode values — two different hashCodes can
still land in the same bucket because the index calculation only uses the lower
bits.

When a collision happens during put(), HashMap chains the new Node onto the
existing bucket. In Java 8+, this uses TAIL INSERTION — the new node is appended
at the end of the linked list. In Java 7, it used HEAD INSERTION — the new node
was inserted at the front. This distinction matters enormously for thread safety
(covered in Q7).

The structure of the chain is a singly-linked list of Node<K,V> objects:

    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        Node<K,V> next;
    }

When searching for a key in a bucket, HashMap first checks the hash (int
comparison, very fast), then checks reference equality (==), and only then calls
equals(). This three-stage check is an optimization: most non-matching nodes are
filtered by the hash check alone without invoking the potentially expensive
equals() method.

TREEIFICATION — The Java 8 Innovation:

When a single bucket's chain length reaches TREEIFY_THRESHOLD (8), AND the
overall table capacity is at least MIN_TREEIFY_CAPACITY (64), the linked list
is converted into a Red-Black Tree. The Node objects are replaced with TreeNode
objects:

    static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
        TreeNode<K,V> parent;
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev; // for unlinking during deletion
        boolean red;
    }

The constants involved:
    TREEIFY_THRESHOLD  = 8   // chain length to trigger tree conversion
    UNTREEIFY_THRESHOLD = 6  // tree node count to revert to linked list
    MIN_TREEIFY_CAPACITY = 64 // minimum table size for treeification

Why 8? The probability of 8 or more entries in a single bucket under random
hashing follows a Poisson distribution. With load factor 0.75, the probability
of a bucket having 8 entries is approximately 0.00000006 (6 in 100 million).
So if a chain reaches 8, it almost certainly means either bad hashCode
implementation or a deliberate attack, not random chance.

Why is MIN_TREEIFY_CAPACITY = 64? If the table is small (say, capacity 16), a
chain of 8 out of 16 buckets means the table is very full. In this case, it is
more efficient to RESIZE the table rather than treeify. Treeification is reserved
for cases where the table is already reasonably large but a specific bucket has
an unusually long chain.

Red-Black Tree ordering: The tree needs a total ordering of keys. It uses this
priority:
    1. Compare by hash value (int comparison)
    2. If hashes are equal and keys implement Comparable, use compareTo()
    3. If still tied, use System.identityHashCode() as a tiebreaker
    4. In rare cases, it uses tieBreakOrder() which compares class names first,
       then identity hash codes

This is why implementing Comparable on your key class improves HashMap performance
when treeification occurs — it provides a stable, meaningful ordering.

UNTREEIFICATION occurs when a tree shrinks below UNTREEIFY_THRESHOLD (6) during
remove() operations or during resize when a tree is split. The gap between 8 and
6 prevents pathological oscillation between list and tree forms.

HASH COLLISION ATTACKS (HashDoS):

Before Java 8, HashMap was vulnerable to algorithmic complexity attacks. An
attacker could craft HTTP request parameters with keys that all hash to the same
bucket. With Java 7's linked list-only approach, a map with n colliding keys
degraded to O(n) per lookup. Inserting 100,000 colliding keys would make each
put() O(100,000), effectively creating an O(n^2) denial of service.

The Java 8 treeification mitigates this: even with all keys in one bucket, the
tree provides O(log n) lookups. 100,000 entries in a tree means approximately
17 comparisons per lookup instead of 100,000. This was a MAJOR motivation for
the treeification change, alongside the general performance improvement.

PERFORMANCE IMPLICATIONS:

    Linked List: get/put = O(k) where k is chain length
    Red-Black Tree: get/put = O(log k) where k is tree size
    No collision: get/put = O(1)

    Benchmark (approximations for a bucket with 1000 colliding keys):
    Linked List lookup: ~500 comparisons average (linear scan)
    Red-Black Tree lookup: ~10 comparisons (log2(1000) ≈ 10)

    Treeification cost: O(k log k) to build the tree from k nodes
    This is a one-time cost amortized over subsequent lookups.

REAL-WORLD PRODUCTION SCENARIOS:

1. In a REST API service, we noticed extremely slow request parsing. Investigation
   revealed that an attacker was sending requests with thousands of specially
   crafted query parameter names that all hashed to the same bucket in the
   internal parameter map. The service was running Java 7. Upgrading to Java 8
   reduced the attack impact by orders of magnitude due to treeification. We
   also added a maximum parameter count limit in our web framework configuration.

2. In an analytics platform, a developer used a custom key class with a very
   poor hashCode — it returned the string length. Since most analytics keys were
   similar-length strings (10-15 characters), nearly all entries ended up in 5-6
   buckets. With 200,000 entries, each bucket had 30,000-40,000 entries. Even
   with treeification, the tree lookups were noticeably slower than O(1).
   Fixing the hashCode to use a proper algorithm (based on all characters)
   eliminated the problem completely.

3. In a caching layer, we observed that after a map resize, the number of tree
   nodes dropped significantly. This is because resize splits each bucket's
   entries across two buckets in the new table. A tree with 20 entries might
   split into two chains of 10, each below the TREEIFY_THRESHOLD, so they
   revert to linked lists. This is actually desired behavior — resize naturally
   reduces collision density.

CODE EXAMPLES:

Example 1 — Demonstrating Treeification:

    // Create keys that all hash to the same bucket
    Map<Object, Integer> map = new HashMap<>();
    // All these keys will collide in bucket 0 for a capacity-16 map
    for (int i = 0; i < 20; i++) {
        map.put(new CollidingKey(i), i); // Custom key where hash() always returns 0
    }

    // After 8 insertions into the same bucket AND if capacity >= 64,
    // the chain converts to a Red-Black Tree.
    // We can verify via reflection:
    Field tableField = HashMap.class.getDeclaredField("table");
    tableField.setAccessible(true);
    Object[] table = (Object[]) tableField.get(map);
    System.out.println(table[0].getClass().getName());
    // Output: java.util.HashMap$TreeNode (not HashMap$Node)

    // Custom colliding key class:
    static class CollidingKey implements Comparable<CollidingKey> {
        int id;
        CollidingKey(int id) { this.id = id; }
        @Override public int hashCode() { return 0; } // All collide
        @Override public boolean equals(Object o) {
            return o instanceof CollidingKey && ((CollidingKey) o).id == this.id;
        }
        @Override public int compareTo(CollidingKey other) {
            return Integer.compare(this.id, other.id);
        }
    }

Example 2 — Showing Performance Difference (Linked List vs Tree):

    // Benchmark: 100,000 entries with collisions
    Map<CollidingKey, Integer> map = new HashMap<>(128); // capacity >= 64

    long start = System.nanoTime();
    for (int i = 0; i < 100_000; i++) {
        map.put(new CollidingKey(i), i);
    }
    long insertTime = System.nanoTime() - start;
    System.out.println("Insert time: " + insertTime / 1_000_000 + " ms");

    start = System.nanoTime();
    for (int i = 0; i < 100_000; i++) {
        map.get(new CollidingKey(i));
    }
    long lookupTime = System.nanoTime() - start;
    System.out.println("Lookup time: " + lookupTime / 1_000_000 + " ms");

    // With Comparable on CollidingKey: lookup ~ 50ms (tree, O(log n) per lookup)
    // Without Comparable: still tree, but ordering uses identityHashCode
    // With Java 7 (no tree): lookup ~ 5000ms+ (linked list, O(n) per lookup)

Example 3 — How NOT to Handle Collisions (Bad hashCode):

    // TERRIBLE hashCode — causes massive collisions
    class BadKey {
        String name;
        int age;
        @Override public int hashCode() { return 1; } // All land in same bucket!
        @Override public boolean equals(Object o) {
            if (!(o instanceof BadKey)) return false;
            BadKey other = (BadKey) o;
            return Objects.equals(name, other.name) && age == other.age;
        }
    }

    // CORRECT hashCode — distributes well
    class GoodKey {
        String name;
        int age;
        @Override public int hashCode() {
            return Objects.hash(name, age); // Uses 31-based polynomial hash
        }
        @Override public boolean equals(Object o) {
            if (!(o instanceof GoodKey)) return false;
            GoodKey other = (GoodKey) o;
            return Objects.equals(name, other.name) && age == other.age;
        }
    }

COMMON MISTAKES, EDGE CASES, AND GOTCHAS:

1. Assuming treeification always happens at chain length 8: It does NOT happen
   if the table capacity is less than 64. In that case, the table resizes instead.
   So a brand-new HashMap with default capacity 16 will resize at least twice
   (16 -> 32 -> 64) before any treeification can occur.

2. Not implementing Comparable on key classes: When treeification occurs and
   keys do not implement Comparable, the tree uses identityHashCode for ordering.
   This provides correct behavior but is less deterministic and slightly slower
   than a proper Comparable implementation. If you know your keys might collide,
   implementing Comparable gives the tree a stable ordering.

3. Confusing hashCode collision with bucket collision: Two keys can have
   different hashCode values but still land in the same bucket because the index
   calculation (hash & (n-1)) masks out upper bits. The hash spreading function
   mitigates this but does not eliminate it.

4. Overlooking the UNTREEIFY_THRESHOLD gap: The hysteresis between 8 (treeify)
   and 6 (untreeify) prevents thrashing. If both were 8, a bucket oscillating
   between 7 and 8 entries would constantly convert back and forth between list
   and tree, wasting CPU on tree construction.

5. Forgetting that resize reduces collision density: After a resize, each bucket's
   entries are split across two buckets. A tree with 12 entries might become two
   chains of 6, both below the threshold, reverting to linked lists. This means
   treeification in HashMap is self-correcting — as the table grows, trees
   naturally dissolve.

FOLLOW-UP POINTS THE INTERVIEWER MIGHT ASK:

- "Why TREEIFY_THRESHOLD = 8 specifically?" — Poisson distribution analysis. With
  load factor 0.75, the probability of 8+ collisions in one bucket is about 6 in
  100 million under random hashing. Choosing 8 means we only treeify when
  something unusual is happening (bad hash or attack).

- "What if the key class has a bad compareTo that is inconsistent with equals?"
  — The tree will still function because HashMap uses compareTo only for ordering,
  not for equality. Equality is always determined by equals(). However, an
  inconsistent compareTo may degrade tree balancing performance.

- "Does treeification change the iteration order?" — No. TreeNode maintains a
  doubly-linked list (next/prev) alongside the tree pointers specifically to
  preserve iteration order when iterating over the map.

- "Can you force a HashMap to never treeify?" — Not through the public API. But
  since treeification requires MIN_TREEIFY_CAPACITY = 64, a tiny map (capacity
  < 64) will resize rather than treeify. In practice, you should not try to
  avoid treeification — it is a safety mechanism, not a performance problem.

Understanding collision handling at this depth — from the basic chaining to the
treeification thresholds, the Poisson distribution reasoning, and the HashDoS
mitigation — separates engineers who read the JavaDoc from those who actually
understand the implementation and can diagnose real performance issues in
production.

--------------------------------------------------------------------------------
Q7: Why is HashMap not thread-safe?
--------------------------------------------------------------------------------

HashMap is explicitly designed to be a single-threaded data structure, and using
it under concurrent access without external synchronization leads to catastrophic
bugs — not just wrong results, but corrupted internal state, infinite loops, and
data that silently vanishes. I have personally debugged a production incident
where a shared HashMap caused a thread to spin in an infinite loop, consuming
100% CPU on one core and eventually triggering an application health check
failure.

DEEP TECHNICAL EXPLANATION:

HashMap is not thread-safe because none of its internal operations are atomic or
protected by synchronization. Multiple threads can interleave at any point during
put(), get(), resize(), or tree operations. Let me break down the specific failure
modes:

FAILURE MODE 1 — THE JAVA 7 INFINITE LOOP (HEAD INSERTION RESIZE BUG):

This is the most famous HashMap concurrency bug. In Java 7, HashMap used HEAD
INSERTION when building chains — new nodes were inserted at the front of the
linked list. During resize(), the transfer() method iterated through each bucket
and re-inserted nodes into the new table using head insertion. This REVERSED the
order of nodes in each chain.

If two threads resize simultaneously:
    Thread A reads chain: A -> B -> C
    Thread B reads chain: A -> B -> C
    Thread A completes resize: C -> B -> A (head insertion reverses order)
    Thread B continues with stale references, creating: B -> A -> B (circular!)

The result is a circular linked list. Any subsequent get() or put() that hits this
bucket enters an infinite loop, calling node.next forever, never reaching null.
The thread spins at 100% CPU, the request never completes, and eventually the
application becomes unresponsive.

Here is the problematic Java 7 transfer code (simplified):

    // Java 7 — HashMap.transfer() — DANGEROUS under concurrency
    void transfer(Entry[] newTable, boolean rehash) {
        for (Entry<K,V> e : table) {
            while (e != null) {
                Entry<K,V> next = e.next;     // Thread A reads next
                // CONTEXT SWITCH — Thread B completes resize, reversing chain
                int i = indexFor(e.hash, newTable.length);
                e.next = newTable[i];          // Circular reference created!
                newTable[i] = e;
                e = next;
            }
        }
    }

FAILURE MODE 2 — JAVA 8 FIX (TAIL INSERTION) BUT STILL UNSAFE:

Java 8 changed to TAIL INSERTION, which eliminates the infinite loop because the
chain order is preserved during resize — no reversal means no circular reference.
However, HashMap in Java 8 is STILL NOT THREAD-SAFE. The infinite loop fix was a
side effect of a cleaner design, not a concurrency fix. The following problems
remain:

FAILURE MODE 3 — LOST UPDATES:

When two threads simultaneously put() entries that map to the same bucket:

    Thread A: reads bucket, finds it empty
    Thread B: reads bucket, finds it empty (same instant)
    Thread A: inserts Node(key1, value1) at bucket
    Thread B: inserts Node(key2, value2) at bucket, OVERWRITING Thread A's node

Thread A's entry is silently lost. No exception, no warning — the data simply
disappears. In a production system, this can cause phantom data loss that is
nearly impossible to reproduce because it depends on precise thread timing.

Similarly, if two threads both try to put() with the same key:

    Thread A: checks bucket, key not found, creates new node
    Thread B: checks bucket, key not found (hasn't seen A's node yet), creates new node
    Both nodes end up in the chain — DUPLICATE KEYS in the same map!

This violates HashMap's fundamental contract. get() will return whichever node
it encounters first, and the other entry becomes a ghost — present in the map but
inaccessible via the normal API.

FAILURE MODE 4 — CONCURRENT MODIFICATION EXCEPTION (FAIL-FAST ITERATORS):

HashMap uses a modCount field to detect structural modifications during iteration.
If you iterate over a HashMap while another thread modifies it, the iterator
detects that modCount has changed and throws ConcurrentModificationException:

    // Thread 1                          // Thread 2
    for (Map.Entry<K,V> e : map) {      map.put("newKey", "value");
        // Processing...                 // modCount incremented
        // ConcurrentModificationException thrown!
    }

But this is a BEST-EFFORT detection. The modCount check is not synchronized, so
it is possible for the corruption to happen WITHOUT triggering the exception.
The fail-fast behavior is a debugging aid, not a safety guarantee.

FAILURE MODE 5 — DATA VISIBILITY (MEMORY MODEL ISSUES):

Even if operations do not interleave, the Java Memory Model does not guarantee
that writes by one thread are visible to other threads without a happens-before
relationship. A thread could put() a value into a HashMap, but another thread
calling get() with the same key might see null — or see a partially constructed
value object — because the writes to the Node's fields have not been flushed from
the CPU cache to main memory. Without volatile, synchronized, or other memory
barriers, there is no happens-before between the put() and get() calls.

FAILURE MODE 6 — CORRUPTED TREEIFICATION:

In Java 8+, if two threads trigger treeification on the same bucket simultaneously,
the tree construction (which involves multiple pointer manipulations — setting
parent, left, right, color, rebalancing) can produce a corrupted tree. A corrupted
Red-Black Tree can cause get() to miss entries, return wrong values, or throw
NullPointerException deep in the tree traversal code.

REAL-WORLD PRODUCTION SCENARIOS:

1. I debugged a production issue where a Spring Boot service had a @Component
   holding a plain HashMap as a local cache, injected into multiple request
   handler threads. Under high load, one thread spun in an infinite loop during
   resize (Java 7). The thread dump showed the thread stuck at HashMap.get()
   in the transfer method. CPU was at 100% on one core. The fix was switching
   to ConcurrentHashMap, but the deeper fix was using a proper caching library
   (Caffeine) with TTL and bounded size.

2. In a financial reporting service, two threads were simultaneously building
   a summary map from different data partitions, then merging into a shared
   HashMap. Lost updates caused certain accounts to disappear from reports
   intermittently — maybe once every 200 runs. The bug took two weeks to find
   because it was non-deterministic and the data volumes in dev were too small
   to trigger concurrent access. The fix was using ConcurrentHashMap with
   merge() for atomic combine operations.

3. A batch processing job used HashMap in a fork-join pool. Subtasks accumulated
   results into a shared HashMap. Under the parallel stream's work-stealing
   scheduler, multiple threads hit the same map simultaneously. The symptom was
   incorrect result counts — the map reported 47,000 entries when the correct
   count was 50,000. The lost 3,000 entries were overwritten during concurrent
   put() operations. We fixed it with Collectors.toConcurrentMap() in the stream
   pipeline.

CODE EXAMPLES:

Example 1 — Demonstrating Lost Updates:

    Map<String, Integer> sharedMap = new HashMap<>();
    int threadCount = 10;
    int insertsPerThread = 10_000;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int t = 0; t < threadCount; t++) {
        final int threadId = t;
        executor.submit(() -> {
            for (int i = 0; i < insertsPerThread; i++) {
                sharedMap.put("thread" + threadId + "_key" + i, i);
            }
            latch.countDown();
        });
    }
    latch.await();
    System.out.println("Expected: " + (threadCount * insertsPerThread));
    System.out.println("Actual: " + sharedMap.size());
    // Output: Expected: 100000
    //         Actual: 87342 (or some number < 100000, varies each run)
    // Entries are LOST due to concurrent put() without synchronization.

Example 2 — ConcurrentModificationException:

    Map<String, String> map = new HashMap<>();
    map.put("a", "1");
    map.put("b", "2");
    map.put("c", "3");

    // BAD: modifying map during iteration
    try {
        for (String key : map.keySet()) {
            if (key.equals("b")) {
                map.remove(key); // Throws ConcurrentModificationException
            }
        }
    } catch (ConcurrentModificationException e) {
        System.out.println("Caught CME: " + e.getMessage());
    }

    // CORRECT: use Iterator.remove()
    Iterator<String> it = map.keySet().iterator();
    while (it.hasNext()) {
        if (it.next().equals("b")) {
            it.remove(); // Safe — iterator updates modCount internally
        }
    }

    // ALSO CORRECT in Java 8+: use removeIf()
    map.keySet().removeIf(key -> key.equals("b"));

Example 3 — Demonstrating Visibility Problem:

    Map<String, String> sharedMap = new HashMap<>();

    // Thread 1: writer
    new Thread(() -> {
        sharedMap.put("status", "READY");
        // No memory barrier — this write may not be visible to other threads
    }).start();

    // Thread 2: reader (may never see "READY" without happens-before)
    new Thread(() -> {
        Thread.sleep(100); // Even with sleep, visibility is NOT guaranteed
        String status = sharedMap.get("status");
        // status could be null due to CPU cache not being flushed
        System.out.println("Status: " + status); // Might print null!
    }).start();

    // FIX: Use ConcurrentHashMap which provides happens-before guarantees
    // for put() -> get() pairs on the same key.

COMMON MISTAKES, EDGE CASES, AND GOTCHAS:

1. "It works in testing so it must be safe": Concurrency bugs in HashMap are
   probabilistic. They depend on thread timing, CPU count, JVM implementation,
   and load. A HashMap might work perfectly in unit tests with 2 threads but
   fail catastrophically in production with 200 concurrent requests. Never use
   the absence of test failures as evidence of thread safety.

2. Wrapping HashMap with Collections.synchronizedMap() and forgetting compound
   operations: synchronizedMap() synchronizes individual get/put/remove calls,
   but compound operations like "check-then-act" are NOT atomic:

       Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
       // STILL BROKEN — two threads can both see null and both put:
       if (syncMap.get(key) == null) {   // Thread A and B both pass this check
           syncMap.put(key, compute());  // Both put, one overwrites the other
       }
       // FIX: Use ConcurrentHashMap.computeIfAbsent() for atomic check-then-act

3. Using HashMap in a lambda captured by a parallel stream: Parallel streams
   execute in a ForkJoinPool. If your lambda captures a HashMap and writes to it,
   multiple threads will write concurrently. Use Collectors.toConcurrentMap()
   or collect into a regular map with Collectors.toMap() (which is safe because
   the stream framework handles the merging).

4. Assuming volatile HashMap reference makes it safe: Declaring the reference as
   volatile (volatile Map<K,V> map) only ensures visibility of the reference
   itself, NOT the internal state of the HashMap. After map = new HashMap<>(old),
   another thread sees the new reference, but the contents may not be fully
   visible. You need a fully synchronized or concurrent structure.

5. Overlooking the Java 7 to Java 8 behavioral change: If you upgrade from
   Java 7 to Java 8, the infinite loop bug during resize goes away due to tail
   insertion, but ALL other concurrency issues remain. Upgrading Java version
   is NOT a fix for HashMap thread safety.

FOLLOW-UP POINTS THE INTERVIEWER MIGHT ASK:

- "How would you make a HashMap thread-safe?" — Three options: (1) Wrap with
  Collections.synchronizedMap() for simple cases. (2) Use ConcurrentHashMap
  for high-throughput concurrent access. (3) Use external locking
  (ReadWriteLock) if you need custom synchronization semantics.

- "What is the difference between fail-fast and fail-safe iterators?" —
  HashMap uses fail-fast: it throws ConcurrentModificationException on
  structural modification detection. ConcurrentHashMap uses weakly consistent
  (sometimes called fail-safe): it never throws CME but may not reflect all
  modifications made after the iterator was created.

- "Can you trigger the infinite loop in Java 7 in a test?" — It is extremely
  difficult to reproduce reliably because it requires a specific thread
  interleaving during resize. Tools like jcstress or thread-weaver can help
  by controlling thread scheduling, but in practice, the bug manifests
  randomly under high load.

- "Is Hashtable thread-safe?" — Yes, Hashtable synchronizes every method. But
  it is a coarse-grained lock on the entire table, making it much slower than
  ConcurrentHashMap under contention. Hashtable is effectively deprecated in
  favor of ConcurrentHashMap.

The takeaway is simple but critical: never share a HashMap across threads without
proper synchronization. ConcurrentHashMap exists specifically for this purpose.
The cost of using ConcurrentHashMap over HashMap in single-threaded code is
negligible, but the cost of using HashMap in multi-threaded code is catastrophic.

--------------------------------------------------------------------------------
Q8: ConcurrentHashMap advantage?
--------------------------------------------------------------------------------

ConcurrentHashMap is the gold standard for thread-safe, high-throughput key-value
storage in Java, and it is what I reach for every single time I need a shared map
in a concurrent environment. Its key advantage is that it provides full thread
safety WITHOUT the brutal performance penalty of locking the entire map — it
achieves this through fine-grained locking and lock-free reads, giving you
near-HashMap performance under concurrent access.

DEEP TECHNICAL EXPLANATION:

The implementation has changed dramatically between Java 7 and Java 8, and
understanding both versions is important because interviewers frequently ask
about the evolution.

JAVA 7 — SEGMENT-BASED LOCKING:

Java 7's ConcurrentHashMap divided the internal table into an array of Segment
objects. Each Segment was essentially a miniature HashMap with its own lock
(extending ReentrantLock). The default concurrency level was 16, meaning 16
Segments, meaning 16 threads could write concurrently without contention (as long
as they hit different segments).

    ConcurrentHashMap
    └── Segment[] segments (default 16)
        ├── Segment[0]  — has its own lock and HashEntry[] table
        ├── Segment[1]  — has its own lock and HashEntry[] table
        ├── ...
        └── Segment[15] — has its own lock and HashEntry[] table

A put() operation: (1) computed the hash, (2) determined the segment index from
the hash's upper bits, (3) locked ONLY that segment, (4) performed the insertion.
A get() operation was LOCK-FREE because the HashEntry values were volatile.

The downsides of the Java 7 approach:
- Fixed segment count at construction time (could not grow dynamically)
- Memory overhead of 16 Segment objects even for small maps
- Two-level lookup: first find the segment, then find the bucket within it
- Limited maximum concurrency (capped at segment count)

JAVA 8 — CAS + NODE-LEVEL SYNCHRONIZED:

Java 8 completely redesigned ConcurrentHashMap. There are NO segments. Instead,
the internal structure is a flat Node<K,V>[] array (same as HashMap), but
concurrency is achieved through a combination of CAS (Compare-And-Swap) operations
and synchronized blocks on individual bucket head nodes.

    ConcurrentHashMap (Java 8+)
    └── Node<K,V>[] table
        ├── table[0] = Node → Node → Node (synchronized on head node for writes)
        ├── table[1] = null (empty bucket)
        ├── table[2] = TreeBin → TreeNode tree (synchronized on TreeBin for writes)
        └── ...

How put() works in Java 8:

    1. Compute hash using spread(): (h ^ (h >>> 16)) & HASH_BITS
       HASH_BITS = 0x7fffffff ensures the hash is always positive.

    2. If the target bucket is empty:
       Use CAS to atomically set the first node: casTabAt(tab, i, null, newNode)
       No locking needed! This is the fast path for empty buckets.

    3. If the bucket is not empty:
       synchronized (headNode) {
           // Walk the chain or tree, find existing key or append new node
           // Treeify if chain exceeds TREEIFY_THRESHOLD
       }
       Only the HEAD NODE of that bucket is locked — all other buckets remain
       accessible to other threads.

    4. If the map is currently resizing (headNode.hash == MOVED, a ForwardingNode):
       The current thread HELPS with the resize before completing its put().
       This is cooperative resizing — multiple threads share the resize work.

How get() works in Java 8:

    1. Compute hash, find bucket.
    2. Read the head node using volatile semantics (Unsafe.getObjectVolatile).
    3. Walk the chain or tree WITHOUT ANY LOCKING.
    4. This is completely lock-free because:
       - Node.val and Node.next are declared volatile
       - Writes by put() are visible to get() through volatile semantics
       - There is a happens-before relationship between put() and subsequent get()

WHY CAS IS PREFERRED OVER SEGMENT LOCKING:

CAS (Compare-And-Swap) is a CPU-level atomic instruction (CMPXCHG on x86). It
attempts to set a value only if the current value matches the expected value —
all in a single atomic CPU operation. Benefits:
- No thread blocking: if CAS fails, the thread retries (spin) rather than
  parking/unparking, which is expensive due to context switching.
- No lock object overhead: no need for ReentrantLock objects per segment.
- Granularity: locking at the individual bucket level (per-node) rather than
  per-segment gives finer-grained concurrency.
- Dynamic: no fixed concurrency level at construction time.

VOLATILE READS FOR GET():

All get() operations in Java 8 ConcurrentHashMap are lock-free. The table array
is accessed via Unsafe.getObjectVolatile(), and Node.val and Node.next are
volatile. This means:
- get() never blocks, even during a resize.
- get() always sees the most recent completed put() for the same key.
- get() provides a happens-before guarantee per the Java Memory Model.

COOPERATIVE RESIZING:

When ConcurrentHashMap resizes, it divides the old table into chunks and assigns
each chunk to a thread. If a thread performing put() encounters a ForwardingNode
(indicating resize in progress), it joins the resize effort by claiming and
migrating a chunk before completing its own operation. This distributes the resize
cost across multiple threads, avoiding the "stop-the-world" resize that HashMap
suffers.

COMPUTEIFABSENT ATOMICITY:

ConcurrentHashMap.computeIfAbsent(key, mappingFunction) is atomic for a given
key. The bucket is locked while the mapping function executes. This is the
correct way to implement lazy initialization in a concurrent map:

    // Atomic: only one thread computes the value for a given key
    cache.computeIfAbsent(userId, id -> expensiveDatabaseLookup(id));

Warning: The mapping function MUST NOT modify the map itself (not even other
keys), or you risk a deadlock. In Java 9+, the JDK added detection for this
recursive update and throws IllegalStateException.

SIZE() IS APPROXIMATE:

ConcurrentHashMap.size() (and mappingCount()) may return an approximate value
during concurrent modifications. Internally, it uses a baseCount with a
CounterCell[] array (similar to LongAdder) to distribute counting across threads
and reduce contention. The count is eventually consistent — it will be exact if
no concurrent modifications are happening at the time of the call.

WEAKLY CONSISTENT ITERATORS:

Iterators over ConcurrentHashMap are weakly consistent:
- They NEVER throw ConcurrentModificationException.
- They reflect the state of the map at the time the iterator was created or later.
- They may or may not reflect concurrent modifications made after creation.
- They traverse each element at most once (no duplicates).

This is a deliberate design tradeoff: strong consistency would require locking
the entire map during iteration, destroying the concurrency benefits.

COMPARISON WITH Collections.synchronizedMap():

    Feature                    ConcurrentHashMap       synchronizedMap
    ─────────────────────────────────────────────────────────────────────
    Lock granularity           Per-bucket (Java 8)     Entire map
    Read locking               Lock-free (volatile)    Full lock on every read
    Concurrent reads           Unlimited               One at a time
    Concurrent writes          Limited by bucket count One at a time
    Compound operations        computeIfAbsent, etc.   Not atomic
    Iterator behavior          Weakly consistent       Fail-fast
    null keys/values           NOT allowed             Allowed
    Throughput under contention High                   Very low

Performance characteristics (approximate, 16-core machine, 100K entries):
    - 90% reads, 10% writes:
      ConcurrentHashMap: ~50M ops/sec
      synchronizedMap:    ~3M ops/sec (17x slower)
    - 50% reads, 50% writes:
      ConcurrentHashMap: ~30M ops/sec
      synchronizedMap:    ~2M ops/sec (15x slower)

REAL-WORLD PRODUCTION SCENARIOS:

1. In a high-frequency trading system, we used ConcurrentHashMap to maintain a
   live order book with 500+ threads reading current prices and 10-20 threads
   updating prices from market data feeds. The lock-free reads were critical —
   any blocking on read would add latency to order decision-making. We measured
   P99 read latency at 200 nanoseconds with ConcurrentHashMap vs 15 microseconds
   with synchronizedMap under the same load.

2. In a session management service handling 50,000 concurrent WebSocket
   connections, we used ConcurrentHashMap<String, WebSocketSession> as the
   session registry. The computeIfAbsent() method was used for session creation
   to ensure exactly one session per connection ID, even when duplicate connect
   messages arrived simultaneously. The weakly consistent iterator was used for
   periodic health-check sweeps without blocking session creation/destruction.

3. In a distributed cache warming scenario, multiple threads fetched data from
   different database shards and populated a shared ConcurrentHashMap. The
   cooperative resizing was visible in thread dumps — during the initial bulk
   load, we could see 8-10 threads simultaneously participating in the resize
   operation, completing it in a fraction of the time a single-threaded resize
   would have taken.

CODE EXAMPLES:

Example 1 — Atomic Compute Operations:

    ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    // Thread-safe increment — atomic per key
    counters.computeIfAbsent("api_calls", k -> new AtomicLong(0)).incrementAndGet();

    // Thread-safe merge — combine values atomically
    ConcurrentHashMap<String, Long> wordCounts = new ConcurrentHashMap<>();
    // Multiple threads processing different documents:
    wordCounts.merge("java", 1L, Long::sum);  // Atomic: old + 1, or 1 if absent

    // computeIfAbsent for lazy cache population
    ConcurrentHashMap<String, UserProfile> profileCache = new ConcurrentHashMap<>();
    UserProfile profile = profileCache.computeIfAbsent(userId, id -> {
        // Only ONE thread executes this for a given userId
        return userRepository.findById(id).orElseThrow();
    });

Example 2 — Incorrect Usage (Common Mistake with Check-Then-Act):

    ConcurrentHashMap<String, List<String>> map = new ConcurrentHashMap<>();

    // BAD: Non-atomic check-then-act despite using ConcurrentHashMap
    if (!map.containsKey("events")) {               // Thread A checks: absent
        map.put("events", new ArrayList<>());        // Thread A puts
    }                                                // Thread B also checked: absent
    map.get("events").add("click");                  // Thread B puts, overwrites A's list!
                                                     // Thread A's add goes to overwritten list

    // CORRECT: Use computeIfAbsent for atomic initialization
    map.computeIfAbsent("events", k -> new CopyOnWriteArrayList<>()).add("click");

    // NOTE: The List itself must also be thread-safe (CopyOnWriteArrayList)
    // because multiple threads will call .add() on the same list concurrently.

Example 3 — ConcurrentHashMap vs synchronizedMap Benchmark:

    int threadCount = 16;
    int opsPerThread = 1_000_000;

    // ConcurrentHashMap benchmark
    ConcurrentHashMap<Integer, Integer> concMap = new ConcurrentHashMap<>();
    long concStart = System.nanoTime();
    ExecutorService pool = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    for (int t = 0; t < threadCount; t++) {
        final int tid = t;
        pool.submit(() -> {
            for (int i = 0; i < opsPerThread; i++) {
                int key = ThreadLocalRandom.current().nextInt(10_000);
                if (i % 10 < 9) concMap.get(key);  // 90% reads
                else concMap.put(key, tid);          // 10% writes
            }
            latch.countDown();
        });
    }
    latch.await();
    long concTime = System.nanoTime() - concStart;
    System.out.printf("ConcurrentHashMap: %d ms%n", concTime / 1_000_000);

    // synchronizedMap benchmark (same workload)
    Map<Integer, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
    // ... same test code with syncMap ...
    // Typical result: ConcurrentHashMap is 10-20x faster under this workload.

COMMON MISTAKES, EDGE CASES, AND GOTCHAS:

1. Null keys and null values are NOT allowed: Unlike HashMap,
   ConcurrentHashMap throws NullPointerException for null keys or null values.
   This is by design — null is ambiguous in concurrent contexts. Does get()
   returning null mean "key absent" or "value is null"? In a concurrent map,
   you cannot distinguish these cases safely. If you need nullable values, wrap
   them in Optional or use a sentinel object.

2. computeIfAbsent deadlock with recursive updates: If the mapping function
   passed to computeIfAbsent attempts to update the SAME ConcurrentHashMap
   (even a different key that happens to land in the same bucket), it can
   deadlock because the bucket lock is already held:

       // DEADLOCK RISK in Java 8 (IllegalStateException in Java 9+):
       map.computeIfAbsent("key1", k -> {
           map.computeIfAbsent("key2", k2 -> "val2"); // If same bucket: deadlock
           return "val1";
       });

3. Assuming size() is exact during concurrent operations: size() returns a
   long-term consistent count but may be off by a few during active writes.
   If you need an exact count for a business decision, you need external
   synchronization. For monitoring and logging purposes, the approximate count
   is perfectly fine.

4. Using ConcurrentHashMap but wrapping with synchronizedMap: I have seen code
   that does Collections.synchronizedMap(new ConcurrentHashMap<>()). This is
   pointless and harmful — it adds a global lock on top of the fine-grained
   concurrency, negating all benefits. Use one or the other, never both.

5. Forgetting that values in the map must also be thread-safe: ConcurrentHashMap
   guarantees safe access to the map structure, but NOT to the values stored in
   it. If you store a mutable ArrayList as a value, concurrent threads can still
   corrupt it. Values must be immutable or thread-safe (CopyOnWriteArrayList,
   AtomicLong, etc.).

6. Iterating and expecting a snapshot: ConcurrentHashMap iterators are weakly
   consistent. If you need a point-in-time snapshot for business logic, you must
   create an explicit copy: new HashMap<>(concurrentMap). This copy operation
   itself is safe because ConcurrentHashMap's internal reads are properly
   synchronized.

FOLLOW-UP POINTS THE INTERVIEWER MIGHT ASK:

- "When would you use synchronizedMap over ConcurrentHashMap?" — Almost never
  in modern Java. The only edge case is if you absolutely need null keys or values,
  or if you need to lock the entire map for a compound operation that spans
  multiple keys (and you cannot restructure the logic to use compute/merge).

- "How does ConcurrentHashMap.size() maintain its count?" — It uses a distributed
  counter scheme inspired by LongAdder. A baseCount is updated via CAS. Under
  contention, it falls back to a CounterCell[] array where each thread increments
  its own cell, reducing CAS contention. size() sums baseCount + all CounterCells.

- "What is the memory overhead of ConcurrentHashMap vs HashMap?" — Slightly
  higher due to volatile node fields and the ForwardingNode mechanism during
  resize. In practice, the difference is negligible (a few bytes per node). The
  concurrency benefit far outweighs the memory cost.

- "Can ConcurrentHashMap replace all uses of synchronized blocks?" — No. It
  only provides atomic operations on individual keys. If your business logic
  requires atomicity across MULTIPLE keys (e.g., transfer balance from account
  A to account B), you still need external synchronization or a transactional
  approach.

- "What is ConcurrentHashMap's initial capacity and load factor?" — Same defaults
  as HashMap: capacity 16, load factor 0.75. You can pre-size it the same way.
  The concurrencyLevel parameter exists for backward compatibility but is
  essentially ignored in Java 8+ (internally it only affects initial sizing).

ConcurrentHashMap is one of the finest pieces of concurrent engineering in the
JDK. When you use it correctly — leveraging compute, merge, and computeIfAbsent
for atomic compound operations, storing thread-safe or immutable values, and
understanding that iteration is weakly consistent — you get a data structure
that scales linearly with core count while maintaining correctness guarantees
that would take thousands of lines of manual synchronization to replicate.
