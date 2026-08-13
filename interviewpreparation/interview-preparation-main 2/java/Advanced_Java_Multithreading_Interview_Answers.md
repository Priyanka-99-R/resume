# 10 Advanced Java Multithreading Interview Questions (5+ Years)

---

## Q1. What is the difference between `synchronized`, `ReentrantLock`, and `StampedLock`?

**Summary:**
All three are locking mechanisms, but they differ in flexibility and performance. `synchronized` is the simplest, `ReentrantLock` gives you advanced control, and `StampedLock` is optimized for read-heavy workloads with its optimistic locking strategy.

**Concept:**
- `synchronized` is the built-in intrinsic lock — automatic acquire/release, simple but limited.
- `ReentrantLock` is explicit — you get `tryLock()`, timed waits, interruptible locking, fairness policies, and multiple `Condition` objects.
- `StampedLock` (Java 8+) introduces **optimistic reads** — you read without acquiring a lock, then validate. If no writer interfered, you skip locking entirely.

**When & Why in Real Projects:**
- I use `synchronized` for simple, low-contention critical sections — say guarding a counter inside a singleton.
- `ReentrantLock` when I need `tryLock` with timeout — for example, avoiding deadlocks in a payment processing pipeline where two services lock shared resources.
- `StampedLock` in a caching layer where 95% of operations are reads and writes are rare. The optimistic read path avoids lock contention entirely.

**Real-World Example:**
In a configuration cache service, I used `StampedLock`:

```java
StampedLock sl = new StampedLock();

public Config getConfig() {
    long stamp = sl.tryOptimisticRead();
    Config c = this.cachedConfig;
    if (!sl.validate(stamp)) {        // writer interfered
        stamp = sl.readLock();         // fallback to pessimistic
        try { c = this.cachedConfig; }
        finally { sl.unlockRead(stamp); }
    }
    return c;
}
```

This gave us measurably lower latency on read-heavy config lookups compared to `ReentrantReadWriteLock`.

**Common Mistakes:**
- Using `StampedLock` when reentrancy is needed — it's **not reentrant**, and re-acquiring it will deadlock the thread.
- Forgetting to `unlock()` in a `finally` block with `ReentrantLock`.
- Choosing `ReentrantLock` when `synchronized` would suffice — it adds complexity for no benefit.

**Closing:**
I pick the simplest lock that meets the requirement. `synchronized` is my default, `ReentrantLock` for advanced control, and `StampedLock` only when profiling shows read contention is a real bottleneck.

---

## Q2. Explain the Java Memory Model (JMM) and happens-before relationships.

**Summary:**
The Java Memory Model defines the rules for when one thread's writes become visible to another thread. Without understanding happens-before, you'll write code that works on your machine but fails unpredictably in production under concurrency.

**Concept:**
Modern CPUs and the JIT compiler **reorder instructions** and cache values in registers/CPU caches. The JMM says: unless there's a **happens-before** relationship, you have **zero guarantee** that Thread B sees what Thread A wrote.

Key happens-before rules:
- **Monitor unlock → lock:** Releasing a `synchronized` block makes all prior writes visible to the next thread that acquires the same lock.
- **Volatile write → read:** Writing to a `volatile` field flushes all prior writes; reading it pulls them in.
- **Thread.start():** Everything before `start()` is visible to the new thread.
- **Thread.join():** Everything the joined thread did is visible after `join()` returns.
- **Transitivity:** If A happens-before B, and B happens-before C, then A happens-before C.

**When & Why in Real Projects:**
Every time I share mutable state between threads, I'm relying on these rules. For example, publishing a fully-constructed object to another thread — if I don't use `volatile` or a lock, the other thread may see a **partially constructed** object.

**Real-World Example:**
Classic double-checked locking for a singleton:

```java
private static volatile Singleton instance; // volatile is ESSENTIAL here

public static Singleton getInstance() {
    if (instance == null) {
        synchronized (Singleton.class) {
            if (instance == null) {
                instance = new Singleton(); // without volatile, another thread
            }                                // can see a half-initialized object
        }
    }
    return instance;
}
```

Without `volatile`, the JVM can reorder the constructor and the reference assignment — another thread gets a non-null reference to an object whose fields haven't been written yet.

**Common Mistakes:**
- Assuming `volatile` gives atomicity — it only gives **visibility** and **ordering**. `count++` on a volatile int is still a race condition.
- Thinking that because code "works in testing," it's correct — JMM issues are **timing-dependent** and may only surface under load or on different hardware.

**Closing:**
Happens-before is the contract I reason about whenever I share state across threads. If I can't point to a specific rule that guarantees visibility, I assume the code is broken.

---

## Q3. What is false sharing and how do you solve it in Java?

**Summary:**
False sharing is a hardware-level performance killer where two threads modify independent variables that happen to sit on the same CPU cache line. The CPU keeps invalidating and reloading the entire cache line between cores, destroying throughput even though there's no logical contention.

**Concept:**
CPU caches operate on **cache lines** — typically 64 bytes. If Thread A writes field `x` and Thread B writes field `y`, but both fields are within the same 64-byte block, every write forces the other core to reload that entire line from main memory. This is invisible at the Java level — your code looks perfectly correct but runs orders of magnitude slower.

**When & Why in Real Projects:**
I've seen this in high-throughput systems — counters in metrics collectors, ring buffer cursors (like Disruptor), and parallel accumulators. Anywhere you have per-thread hot fields that are updated in tight loops.

**Real-World Example:**
We had two `volatile long` counters tracking throughput on two independent channels. Profiling showed unexpected memory bus saturation. The fix:

```java
import sun.misc.Contended;

public class ChannelCounters {
    @Contended volatile long channel1Count;  // padded to own cache line
    @Contended volatile long channel2Count;  // padded to own cache line
}
// JVM flag: -XX:-RestrictContended
```

After padding, throughput jumped ~3x because cores stopped fighting over the same cache line.

**Common Mistakes:**
- Not knowing this exists — many developers only think about logical contention, not hardware-level contention.
- Forgetting the `-XX:-RestrictContended` JVM flag — without it, `@Contended` is silently ignored outside `java.lang`.
- Over-padding everything — it wastes memory. Only pad fields that are truly hot and written by different threads.

**Closing:**
False sharing is one of those problems you only look for when performance profiling shows unexplained slowness in concurrent code. But once you know it, it's easy to fix with `@Contended` or manual padding.

---

## Q4. How does `CompletableFuture` differ from `Future`? Explain exception handling in a chain.

**Summary:**
`Future` is blocking and compositionless — you call `get()` and your thread sits idle. `CompletableFuture` is fully non-blocking with chaining, composition, and proper exception propagation. It's the backbone of modern async Java code.

**Concept:**
- `Future.get()` blocks the calling thread. No way to attach callbacks or chain operations.
- `CompletableFuture` lets you build **async pipelines**: `thenApply`, `thenCompose`, `thenCombine`, `allOf`, `anyOf`. Each stage runs when the previous one completes — no thread blocking.
- Exceptions propagate **downstream** — intermediate stages are skipped until you handle the exception with `exceptionally()`, `handle()`, or `whenComplete()`.

**When & Why in Real Projects:**
Any time I'm orchestrating multiple async I/O calls — fetching user data from one service, payment data from another, merging results. Instead of blocking on each call sequentially, I compose them.

**Real-World Example:**
Order processing pipeline:

```java
CompletableFuture.supplyAsync(() -> fetchOrder(orderId))
    .thenApply(order -> enrichWithInventory(order))
    .thenApply(order -> calculateShipping(order))
    .exceptionally(ex -> {
        log.error("Order pipeline failed for {}", orderId, ex);
        return createFallbackOrder(orderId);
    })
    .thenAccept(order -> persist(order));
```

If `enrichWithInventory` throws, `calculateShipping` is **skipped**, and `exceptionally` catches it, providing a recovery path.

**Common Mistakes:**
- Using `thenApply` when you should use `thenCompose` — if your function returns another `CompletableFuture`, use `thenCompose` to avoid `CompletableFuture<CompletableFuture<T>>`.
- Ignoring the thread pool — `thenApply` may run on the completing thread or the caller thread. For CPU-intensive work, use `thenApplyAsync(fn, myExecutor)` with a dedicated pool.
- Swallowing exceptions — if you don't attach `exceptionally`/`handle`, failures are silently lost unless someone calls `get()`.

**Closing:**
`CompletableFuture` is my go-to for async orchestration in Java. The key is understanding exception propagation and being intentional about which executor runs each stage.

---

## Q5. What is the ABA problem in lock-free programming?

**Summary:**
The ABA problem is a subtle bug in CAS (Compare-And-Swap) based algorithms where a value changes from A to B and back to A. The CAS succeeds because it sees A, but the underlying state has changed — leading to silent data corruption in lock-free data structures.

**Concept:**
CAS says: "if the current value is A, swap it to C." But between your read and CAS, another thread may have changed A → B → A. Your CAS sees A and proceeds, but the context has completely changed. In a lock-free stack, for example, this can cause you to reconnect nodes that have already been popped and freed.

**When & Why in Real Projects:**
This matters when building or using lock-free data structures — lock-free stacks, queues, or custom concurrent containers. Most developers won't hit this directly because `java.util.concurrent` handles it internally, but if you're building high-performance infra, you must account for it.

**Real-World Example:**
Java provides `AtomicStampedReference` to solve this — it pairs the reference with a version stamp:

```java
AtomicStampedReference<Node> head = new AtomicStampedReference<>(null, 0);

int[] stampHolder = new int[1];
Node current = head.get(stampHolder);
int stamp = stampHolder[0];

// CAS checks BOTH reference AND version — ABA can't fool it
head.compareAndSet(current, newNode, stamp, stamp + 1);
```

Even if the reference reverts to the same value, the stamp increments, so the stale CAS correctly fails.

**Common Mistakes:**
- Using plain `AtomicReference` in lock-free structures where nodes get recycled — this is exactly where ABA strikes.
- Thinking ABA is theoretical — it's rare but real, especially under heavy contention with object pooling.

**Closing:**
If I'm doing CAS on references where the object identity might be reused, I always reach for `AtomicStampedReference`. It's a small cost that prevents an extremely hard-to-debug corruption issue.

---

## Q6. Explain the `ForkJoinPool` work-stealing algorithm.

**Summary:**
`ForkJoinPool` uses a work-stealing algorithm where each worker thread has its own deque. Idle threads steal tasks from busy threads, ensuring all cores stay utilized. It's purpose-built for recursive, divide-and-conquer parallelism.

**Concept:**
- Each worker thread pushes/pops from the **tail** of its own deque (LIFO — better locality, processes smaller subtasks first).
- **Idle threads steal** from the **head** of other workers' deques (FIFO — grabs larger tasks that generate more work).
- This is self-balancing — no central coordinator, minimal contention.

**When & Why in Real Projects:**
- Parallel streams use `ForkJoinPool.commonPool()` under the hood.
- Any task that can be split recursively — processing a large dataset, traversing a tree, parallel merge sort.
- I've used it for parallelizing document indexing where each document can be split into sections and processed independently.

**Real-World Example:**

```java
class SumTask extends RecursiveTask<Long> {
    private final long[] array;
    private final int start, end;

    protected Long compute() {
        if (end - start <= 1000) {
            return sequentialSum(array, start, end);
        }
        int mid = (start + end) / 2;
        SumTask left = new SumTask(array, start, mid);
        left.fork();  // push to deque — may be stolen by idle thread
        long rightResult = new SumTask(array, mid, end).compute();
        return left.join() + rightResult;
    }
}
```

**Common Mistakes:**
- Running blocking I/O in the common pool — it starves parallel streams and unrelated framework code across the entire JVM. Use a separate pool or `ManagedBlocker`.
- Using `ForkJoinPool` for independent I/O tasks — `ThreadPoolExecutor` is better for that. ForkJoin shines on CPU-bound, recursive workloads.
- Forking both subtasks — fork one, compute the other in the current thread. Forking both wastes a thread.

**Closing:**
I use `ForkJoinPool` when tasks naturally decompose into subtasks. For flat, independent I/O work, I stick with `ThreadPoolExecutor`. And I never block in the common pool.

---

## Q7. What is a `Phaser` and how is it better than `CountDownLatch` and `CyclicBarrier`?

**Summary:**
`Phaser` is the most flexible synchronization barrier in Java. Unlike `CountDownLatch` (one-shot) or `CyclicBarrier` (fixed parties), `Phaser` supports dynamic registration/deregistration and multiple named phases — making it ideal for multi-step concurrent workflows where participants vary.

**Concept:**
- **CountDownLatch** — one-time gate. Threads count down, waiting threads proceed when it hits zero. Can't reuse.
- **CyclicBarrier** — reusable barrier but with a **fixed** number of parties set at construction.
- **Phaser** — parties can **register and deregister dynamically** between phases. Each phase has a number, and you can override `onAdvance()` to control termination.

**When & Why in Real Projects:**
I used `Phaser` in a multi-stage data pipeline where worker threads processed batches. Some workers finished early and dropped out, while late-arriving workers joined mid-flight. Neither `CountDownLatch` nor `CyclicBarrier` could handle that.

**Real-World Example:**

```java
Phaser phaser = new Phaser(1); // main thread registered

for (DataSource ds : dataSources) {
    phaser.register();
    executor.submit(() -> {
        loadData(ds);
        phaser.arriveAndAwaitAdvance();  // phase 0: all data loaded

        transform(ds);
        phaser.arriveAndDeregister();    // done, leave the phaser
    });
}

phaser.arriveAndAwaitAdvance(); // wait for phase 0
phaser.arriveAndDeregister();   // main thread leaves
```

**Common Mistakes:**
- Forgetting to `arriveAndDeregister()` — threads that don't deregister will stall the phaser permanently.
- Using `Phaser` when `CountDownLatch` suffices — `Phaser` has more overhead. Use the simplest tool that fits.

**Closing:**
If the number of threads is fixed and I only need one synchronization point, I use `CountDownLatch`. If it's fixed but reusable, `CyclicBarrier`. The moment participants need to come and go dynamically, `Phaser` is the only choice.

---

## Q8. How do you detect and resolve deadlocks in a production Java application?

**Summary:**
Deadlock detection in production comes down to thread dumps and `ThreadMXBean`. Resolution is about disciplined lock ordering, timeouts, and reducing lock scope. I've dealt with this in production, and the key is having the tooling ready before it happens.

**Concept:**
A deadlock occurs when two or more threads each hold a lock the other needs, and neither can proceed. The JVM can detect this for both intrinsic locks and `ReentrantLock`.

**Detection — Three approaches I use:**

1. **Thread dump** — `jstack <pid>` or `kill -3`. The JVM explicitly prints: `"Found one Java-level deadlock"` with the full lock chain.

2. **Programmatic monitoring** — I deploy this in production:

```java
ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
long[] deadlocked = mxBean.findDeadlockedThreads();
if (deadlocked != null) {
    ThreadInfo[] infos = mxBean.getThreadInfo(deadlocked, true, true);
    Arrays.stream(infos).forEach(info -> log.error("Deadlock: {}", info));
    // trigger alert
}
```

I schedule this check every 30 seconds via a daemon thread.

3. **APM tools** — Datadog, New Relic, or Arthas can detect and visualize thread contention.

**Resolution strategies:**
- **Consistent lock ordering** — always acquire lock A before lock B across the entire codebase.
- **`tryLock` with timeout** — `lock.tryLock(500, MILLISECONDS)` — back off instead of waiting forever.
- **Reduce lock granularity** — `ConcurrentHashMap` over synchronized maps, lock striping.
- **Eliminate nested locking** — restructure code so you don't need two locks simultaneously.

**Common Mistakes:**
- Only testing with low concurrency — deadlocks often only surface under production-level load.
- Using `findMonitorDeadlockedThreads()` instead of `findDeadlockedThreads()` — the former only finds intrinsic lock deadlocks, missing `ReentrantLock`.

**Closing:**
I always set up proactive deadlock detection in production services. Finding the deadlock is the easy part — the discipline is in consistent lock ordering and minimizing lock scope from the start.

---

## Q9. What are `VarHandle` and how does it compare to `volatile` and `Atomic*`?

**Summary:**
`VarHandle` (Java 9+) is the supported, fine-grained alternative to `sun.misc.Unsafe` for low-level memory operations. It gives you **access modes** ranging from plain reads to full volatile semantics, letting you choose exactly the level of ordering you need — which matters a lot in performance-sensitive code.

**Concept:**
- `volatile` gives you full memory fence on every read/write — safe but expensive.
- `AtomicInteger` wraps volatile + CAS in a convenient API.
- `VarHandle` exposes **five access modes**: plain, opaque, acquire/release, and volatile — each with different ordering guarantees and performance costs.

```java
class HotCounter {
    int count;
    static final VarHandle COUNT;
    static {
        COUNT = MethodHandles.lookup()
            .findVarHandle(HotCounter.class, "count", int.class);
    }

    void increment() {
        // Acquire/Release — cheaper than volatile, sufficient for many patterns
        int c = (int) COUNT.getAcquire(this);
        COUNT.setRelease(this, c + 1);

        // Or full volatile if needed
        COUNT.getVolatile(this);

        // CAS — same as AtomicInteger but on a plain field
        COUNT.compareAndSet(this, expected, newValue);
    }
}
```

**When & Why in Real Projects:**
In a high-frequency trading system I worked on, replacing `volatile` reads on a hot path with `VarHandle.getAcquire()` reduced latency by ~15%. When you're processing millions of messages per second, the difference between a full fence and an acquire fence is measurable.

**Common Mistakes:**
- Using `VarHandle` when `AtomicInteger` is perfectly fine — premature optimization. Only reach for `VarHandle` when profiling shows contention on memory barriers.
- Getting the access mode wrong — using plain mode when you need ordering will cause subtle concurrency bugs.

**Closing:**
For 99% of code, `volatile` and `Atomic*` are the right choice. I only reach for `VarHandle` when I'm in a performance-critical hot path and I need control over exactly which memory fence I'm paying for.

---

## Q10. Explain `ThreadLocal`, its memory leak risk, and how Scoped Values (Java 21+) improve on it.

**Summary:**
`ThreadLocal` gives each thread its own copy of a variable — essential for things like per-request context in web apps. But in thread pools, it's a memory leak waiting to happen. `ScopedValue` (Java 21+) solves this by binding values to a scope with automatic cleanup.

**Concept:**
Each `Thread` has an internal `ThreadLocalMap`. When you call `threadLocal.set(value)`, it stores the value keyed by the `ThreadLocal` instance (weak reference). The problem: in thread pools, threads live forever. If you forget to call `remove()`, the **value** (strong reference) stays pinned in memory indefinitely.

**When & Why in Real Projects:**
Classic use cases:
- Storing the current user/request context in a web framework (Spring's `SecurityContextHolder` uses it).
- `SimpleDateFormat` — not thread-safe, so you keep one per thread.
- Database connections or transaction contexts.

**Real-World Example — the leak:**

```java
private static final ThreadLocal<UserContext> ctx = new ThreadLocal<>();

public void handleRequest(Request req) {
    ctx.set(buildContext(req));
    try {
        processRequest();
    } finally {
        ctx.remove();  // CRITICAL — without this, the UserContext
    }                   // lives as long as the pool thread (forever)
}
```

I've debugged a production OOM where a `ThreadLocal` held a 50MB object graph, multiplied by 200 pool threads — 10GB leaked.

**`InheritableThreadLocal` problem:**
Child threads inherit the parent's value at creation time. But thread pool threads aren't created per task — they're reused. So inherited values go **stale** and you get cross-request data leakage.

**`ScopedValue` (Java 21+ preview):**

```java
private static final ScopedValue<UserContext> CURRENT_USER = ScopedValue.newInstance();

ScopedValue.runWhere(CURRENT_USER, userCtx, () -> {
    processRequest();  // CURRENT_USER.get() works here
});
// automatically unbound — zero leak risk, no try-finally
```

Benefits:
- **Immutable** within scope — no accidental mutation.
- **Automatic cleanup** — scope ends, value is gone.
- **Virtual-thread friendly** — designed for millions of virtual threads where `ThreadLocal` per thread would be wasteful.

**Common Mistakes:**
- The #1 mistake: forgetting `remove()` in a `finally` block. Every `ThreadLocal.set()` without a corresponding `remove()` is a potential leak in thread pools.
- Using `ThreadLocal` as a global variable substitute — it makes code harder to test and reason about.

**Closing:**
In legacy code, I'm disciplined about `ThreadLocal.remove()` in `finally` blocks. For new projects targeting Java 21+, I use `ScopedValue` — it eliminates the leak class entirely and works naturally with virtual threads.
