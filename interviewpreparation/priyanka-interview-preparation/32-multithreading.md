# 🔴 Java Multithreading & Concurrency — Complete Interview Guide

The topic that has now appeared in **Mphasis L1** (*"write a thread-safe singleton"*, *"what is deadlock?"*) and **Virtusa Round 2**. It is asked at every experienced-level Java round, and it's where 4-years candidates get separated from 1-year candidates — because the follow-up is never "what is a thread", it's **"why does that break?"**

> **How this round actually goes:** one definition question to warm up (`Thread` vs `Runnable`), then straight into *"how do you make this thread-safe?"*, *"synchronized vs volatile"*, *"what's a deadlock and how do you avoid it"*, and usually **one live program** — thread-safe singleton, producer–consumer, or print odd/even alternately with two threads. Have those three typed from memory.

---

## Table of contents

- [Part 0 — The vocabulary they expect](#part-0--the-vocabulary-they-expect)
- [Part 1 — Creating threads](#part-1--creating-threads)
- [Part 2 — Thread lifecycle](#part-2--thread-lifecycle)
- [Part 3 — The core problem: race conditions](#part-3--the-core-problem-race-conditions)
- [Part 4 — synchronized](#part-4--synchronized)
- [Part 5 — volatile and the Java Memory Model](#part-5--volatile-and-the-java-memory-model)
- [Part 6 — Atomic classes & CAS](#part-6--atomic-classes--cas)
- [Part 7 — Locks: ReentrantLock, ReadWriteLock](#part-7--locks-reentrantlock-readwritelock)
- [Part 8 — wait / notify and inter-thread communication](#part-8--wait--notify-and-inter-thread-communication)
- [Part 9 — Deadlock, livelock, starvation](#part-9--deadlock-livelock-starvation)
- [Part 10 — ExecutorService & thread pools](#part-10--executorservice--thread-pools)
- [Part 11 — Callable, Future, CompletableFuture](#part-11--callable-future-completablefuture)
- [Part 12 — Concurrent collections](#part-12--concurrent-collections)
- [Part 13 — Thread-safe singleton (the live-coding one)](#part-13--thread-safe-singleton-the-live-coding-one)
- [Part 14 — Classic live-coding programs](#part-14--classic-live-coding-programs)
- [Part 15 — Multithreading in Spring Boot (your real answer)](#part-15--multithreading-in-spring-boot-your-real-answer)
- [Part 16 — Virtual threads (Java 21)](#part-16--virtual-threads-java-21)
- [Part 17 — Predict the output](#part-17--predict-the-output)
- [Part 18 — Rapid-fire table](#part-18--rapid-fire-table)
- [Part 19 — Drills](#part-19--drills)

---

# Part 0 — The vocabulary they expect

| Term | One-line definition |
|---|---|
| **Process** | An independent program with its **own memory space** |
| **Thread** | A unit of execution **inside** a process, sharing its heap; has its own stack and program counter |
| **Concurrency** | Multiple tasks **making progress** in overlapping time (may be interleaved on 1 core) |
| **Parallelism** | Multiple tasks **executing at the same instant** on multiple cores |
| **Race condition** | Result depends on unpredictable thread timing |
| **Critical section** | Code that must be executed by only one thread at a time |
| **Mutual exclusion** | The guarantee that only one thread is in the critical section |
| **Atomicity** | An operation completes fully or not at all — no visible intermediate state |
| **Visibility** | One thread's write becomes observable to another thread |
| **Ordering** | Whether instructions may be reordered by compiler/CPU |
| **Context switch** | The OS saving one thread's state and restoring another's — not free |

> 💡 **The one-liner that impresses:** *"Thread safety is three separate problems — **atomicity, visibility and ordering**. `synchronized` gives you all three; `volatile` gives you visibility and ordering but **not** atomicity; atomics give you atomicity on a single variable. Choosing the wrong one is where concurrency bugs come from."*

**What each thread owns vs shares:**

```
        PROCESS (JVM)
 ┌────────────────────────────────────┐
 │  HEAP  (objects, static fields)    │  ← SHARED by all threads → needs synchronization
 │  Metaspace (class data)            │  ← shared
 ├────────────────────────────────────┤
 │  Thread-1: stack, PC, locals       │  ← private
 │  Thread-2: stack, PC, locals       │  ← private
 └────────────────────────────────────┘
```

**Say it:** *"Local primitives are always thread-safe because they live on the thread's own stack. The moment state is on the heap and shared — an instance field, a static field, a collection — you need synchronization."*

---

# Part 1 — Creating threads

### Q: What are the ways to create a thread?

```java
// 1. extend Thread — uses up your one inheritance slot ❌
class MyThread extends Thread {
    public void run() { System.out.println(getName()); }
}
new MyThread().start();

// 2. implement Runnable — preferred: separates the TASK from the WORKER ✅
class MyTask implements Runnable {
    public void run() { System.out.println("running"); }
}
new Thread(new MyTask()).start();

// 3. lambda (Runnable is a functional interface)
new Thread(() -> System.out.println("running")).start();

// 4. Callable + ExecutorService — returns a value and can throw ✅✅ (real code)
ExecutorService pool = Executors.newFixedThreadPool(4);
Future<Integer> f = pool.submit(() -> 42);
System.out.println(f.get());
pool.shutdown();

// 5. Java 21 virtual threads
Thread.startVirtualThread(() -> System.out.println("virtual"));
```

**Why `Runnable` over `Thread`:** you keep your single inheritance for real modelling, the task can be **reused** across pools and executors, and it separates *what to do* from *how it runs*. In production code you rarely create a `Thread` at all — you submit tasks to an `ExecutorService`.

### Q: `start()` vs `run()` — the most-asked trap

```java
Thread t = new Thread(() -> System.out.println(Thread.currentThread().getName()));
t.start();   // prints "Thread-0"  → NEW thread, run() called by the JVM
t.run();     // prints "main"      → plain method call, NO new thread at all
```

`start()` asks the JVM to allocate an OS thread and invoke `run()` on it. Calling `run()` directly is just a method call on the current thread — **no concurrency happens**, and interviewers love this because it compiles and "works".

### Q: What happens if you call `start()` twice?

**`IllegalThreadStateException`** — a `Thread` object is single-use. Once it has terminated it can never be restarted; you must create a new one (another reason pools exist).

### Q: Daemon vs user thread?

A **daemon** thread does not keep the JVM alive — when the last *user* thread finishes, the JVM exits and daemons are killed abruptly (no `finally`, no cleanup). Must call `setDaemon(true)` **before** `start()`. GC and JIT compiler threads are daemons. Use for background housekeeping, **never** for work that must complete (writing a file, flushing a queue).

---

# Part 2 — Thread lifecycle

```
                 start()                 scheduler picks it
      NEW ─────────────────▶ RUNNABLE ◀──────────────────┐
                                │  │                     │
     run() returns / throws     │  │  synchronized entry │ lock acquired
                                │  └────────▶ BLOCKED ───┘
                                │
                                │  wait() / join() / park()      notify()/notifyAll()/join done
                                ├────────▶ WAITING ──────────────────┐
                                │                                    │
                                │  sleep(t) / wait(t) / join(t)      │ timeout or notify
                                ├────────▶ TIMED_WAITING ────────────┤
                                │                                    │
                                ▼                                    │
                           TERMINATED  ◀──────────────────────────────┘
```

**The six states are exactly `Thread.State`:** `NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED`.

> ⚠️ **Trap:** *"Is there a RUNNING state?"* — **No.** Java collapses ready-to-run and actually-running into **RUNNABLE**; whether it's on a CPU right now is the OS scheduler's business, not the JVM's.

### Q: `sleep()` vs `wait()` — asked in nearly every round

| | `Thread.sleep(ms)` | `obj.wait()` |
|---|---|---|
| Defined in | `Thread` (static) | `Object` |
| Releases the lock? | ❌ **Keeps** every lock it holds | ✅ **Releases** the monitor of that object |
| Needs synchronized block? | No | **Yes** — else `IllegalMonitorStateException` |
| Woken by | Timeout / interrupt | `notify()`, `notifyAll()`, timeout, interrupt |
| State | `TIMED_WAITING` | `WAITING` (or `TIMED_WAITING` with a timeout) |
| Purpose | Pause | **Coordinate** between threads |

**The sentence:** *"`sleep` is a pause that keeps the lock — using it inside a synchronized block blocks everyone else for the whole duration. `wait` releases the lock so another thread can make the condition true and notify you."*

### Q: `join()`, `yield()`, `interrupt()`?

- **`t.join()`** — the *calling* thread waits until `t` terminates. Used to wait for results before aggregating.
- **`Thread.yield()`** — a *hint* to the scheduler that you're willing to give up the CPU. No guarantee; almost never correct in production code.
- **`t.interrupt()`** — **cooperative** cancellation. It does not stop a thread. It sets the interrupt flag; if the thread is in `sleep`/`wait`/`join` it throws `InterruptedException` **and clears the flag**.

```java
// ✅ correct handling — never swallow it
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();   // restore the flag for callers above you
    return;                               // and actually stop doing work
}
```

> ⚠️ `stop()`, `suspend()`, `resume()` are **deprecated and dangerous** — `stop()` releases all locks instantly, leaving shared objects half-modified. The only sanctioned way to stop a thread is to ask it to finish (a `volatile boolean` flag or interruption).

---

# Part 3 — The core problem: race conditions

### Q: Show me a race condition.

```java
class Counter {
    private int count = 0;
    public void increment() { count++; }        // 💣 NOT atomic
    public int get() { return count; }
}

Counter c = new Counter();
ExecutorService pool = Executors.newFixedThreadPool(10);
for (int i = 0; i < 1000; i++) pool.submit(c::increment);
pool.shutdown(); pool.awaitTermination(1, TimeUnit.MINUTES);
System.out.println(c.get());     // expected 1000 — prints 973, 991, 1000, 987...
```

**Why:** `count++` is **three** bytecode operations — read, add, write.

```
Thread A reads  count = 5
Thread B reads  count = 5      ← before A wrote back
Thread A writes count = 6
Thread B writes count = 6      ← A's increment vanished ("lost update")
```

**Three correct fixes — know all three and when to use each:**

```java
public synchronized void increment() { count++; }        // 1. lock — works, coarsest

private final AtomicInteger count = new AtomicInteger(); // 2. CAS — best for one counter
public void increment() { count.incrementAndGet(); }

private final LongAdder count = new LongAdder();         // 3. best under HIGH contention
public void increment() { count.increment(); }           //    (striped cells, read via sum())
```

> ⚠️ **`volatile` does NOT fix this.** `volatile int count; count++;` is still broken — volatile guarantees you read a *fresh* value, not that read-modify-write is atomic. Stating this correctly is a strong signal.

---

# Part 4 — synchronized

### Q: What does `synchronized` actually do?

Every Java object has a **monitor** (intrinsic lock). `synchronized` acquires it on entry and releases it on exit — **including when an exception is thrown**. It provides all three guarantees: **mutual exclusion, visibility** (entering flushes/invalidates caches — the monitor exit *happens-before* the next monitor enter) and **ordering**.

```java
// 1. synchronized instance method → locks THIS
public synchronized void a() { }

// 2. synchronized static method → locks the CLASS object (MyClass.class)
public static synchronized void b() { }

// 3. synchronized block → locks whatever object you name — the preferred form
private final Object lock = new Object();       // dedicated, private lock object
public void c() {
    // unsynchronized preamble
    synchronized (lock) { /* only the critical section */ }
}
```

> 💡 **Say this trade-off:** *"I prefer a synchronized block on a private final lock object over a synchronized method. A synchronized method locks `this`, which any outside code can also lock — so a caller can accidentally (or maliciously) block your class. And a block lets me keep the critical section as small as possible, which matters for throughput."*

### Q: Do an instance method and a static method block each other?

**No.** Different locks: one holds `this`, the other holds `MyClass.class`. Two threads can be inside both simultaneously — a classic subtle bug when a class mixes instance and static synchronized state.

### Q: Is `synchronized` reentrant?

**Yes.** A thread holding a lock can re-acquire it (the JVM keeps a hold count). This is why a synchronized method can call another synchronized method on the same object without self-deadlock.

### Q: What is the cost?

Uncontended locks are cheap in modern JVMs (biased/thin locking historically; still fast paths today). Contention is what costs — threads park, the OS context-switches, caches invalidate. **Rule: hold locks for as short a time as possible, and never do I/O or call unknown code while holding one** (that's how you get lock-order inversions and deadlocks).

---

# Part 5 — volatile and the Java Memory Model

### Q: What does `volatile` guarantee?

1. **Visibility** — every read goes to main memory, every write is published immediately. No thread can see a stale cached copy.
2. **Ordering** — the compiler/CPU may not reorder around a volatile access (a memory barrier). Everything written *before* a volatile write is visible to anyone who reads that volatile after.

**What it does NOT give:** **atomicity** of compound operations (`i++`, `check-then-act`).

### Q: Give the canonical example.

```java
class Worker implements Runnable {
    private volatile boolean running = true;      // ❌ without volatile this can loop FOREVER
    public void run() {
        while (running) { /* work */ }
        System.out.println("stopped");
    }
    public void stop() { running = false; }
}
```

Without `volatile`, the JIT is entitled to hoist the read out of the loop — it can prove nothing *inside* the loop changes `running` — effectively compiling it to `while (true)`. The thread never sees the write and never stops. **This is a real production hang**, and the fix is one keyword.

### Q: volatile vs synchronized vs atomic — the comparison table

| | `volatile` | `synchronized` | `AtomicInteger` |
|---|---|---|---|
| Visibility | ✅ | ✅ | ✅ |
| Ordering | ✅ | ✅ | ✅ |
| Atomicity of `x++` | ❌ | ✅ | ✅ |
| Blocks threads? | ❌ never | ✅ | ❌ (CAS spin) |
| Scope | one variable | a block/method | one variable |
| Cost | cheapest | highest | in between |
| Use for | flags, and the DCL singleton reference | compound state, invariants over multiple fields | counters, sequences, single-reference swaps |

### Q: What is "happens-before"?

The JMM rule that if action A *happens-before* B, then A's effects are visible to B. The ones to name:

- Program order within a single thread.
- Monitor unlock **happens-before** a later lock of the same monitor.
- A volatile write **happens-before** a later volatile read of the same field.
- `Thread.start()` happens-before everything in that thread.
- Everything in a thread happens-before another thread's successful `join()` on it.
- Constructor completion happens-before a `final` field is read (why immutable objects are safely publishable).

> 💡 **Say:** *"Concurrency bugs aren't really about 'two threads at once' — they're about the absence of a happens-before edge. Every synchronization tool is just a way of creating one."*

---

# Part 6 — Atomic classes & CAS

### Q: How do the `Atomic*` classes work without locks?

Via **CAS — compare-and-swap**, a single CPU instruction (`lock cmpxchg` on x86): *"if this memory location still holds the value I expected, replace it with the new value; otherwise tell me you failed."*

```java
// conceptually what incrementAndGet does:
int prev, next;
do {
    prev = get();
    next = prev + 1;
} while (!compareAndSet(prev, next));   // retry until nobody beat us to it
```

This is **optimistic, non-blocking, lock-free**: no thread is ever suspended; a loser just retries.

```java
AtomicInteger  counter = new AtomicInteger(0);   counter.incrementAndGet();
AtomicLong     id      = new AtomicLong();       id.getAndIncrement();
AtomicBoolean  flag    = new AtomicBoolean();    flag.compareAndSet(false, true);   // run-once guard
AtomicReference<Config> cfg = new AtomicReference<>(initial);
cfg.updateAndGet(c -> c.withTimeout(30));        // lambda-based atomic update
```

**Trade-off to state:** under **high contention** CAS retries burn CPU, and a lock can win. That's what `LongAdder` fixes — it spreads updates across per-thread cells and sums them on read; ideal for hot metrics counters, not for a value you must read exactly on every update.

> 💡 **The ABA problem** (if they go deep): a value changes A→B→A between your read and your CAS; the CAS succeeds though the world changed underneath. `AtomicStampedReference` adds a version stamp to detect it.

---

# Part 7 — Locks: ReentrantLock, ReadWriteLock

### Q: Why would you use `ReentrantLock` over `synchronized`?

```java
private final ReentrantLock lock = new ReentrantLock();

lock.lock();
try {
    // critical section
} finally {
    lock.unlock();       // ⚠️ MUST be in finally — synchronized does this for you
}
```

| Capability | `synchronized` | `ReentrantLock` |
|---|---|---|
| Try without blocking | ❌ | ✅ `tryLock()` |
| Timeout | ❌ | ✅ `tryLock(5, SECONDS)` — **deadlock escape hatch** |
| Interruptible while waiting | ❌ | ✅ `lockInterruptibly()` |
| **Fairness** (longest waiter first) | ❌ | ✅ `new ReentrantLock(true)` |
| Multiple condition queues | ❌ (one wait-set) | ✅ `newCondition()` |
| Lock across methods | ❌ (block-scoped) | ✅ |
| Auto-release | ✅ | ❌ you must `finally` |

**Say:** *"`synchronized` first — it's simpler and impossible to leak. I reach for `ReentrantLock` when I need `tryLock` with a timeout, interruptible acquisition, or separate condition queues."*

### Q: `ReadWriteLock`?

```java
ReadWriteLock rw = new ReentrantReadWriteLock();
rw.readLock().lock();    // many readers concurrently
rw.writeLock().lock();   // exclusive — blocks all readers and writers
```

Right for **read-heavy, write-rare** shared state (a cached config, a lookup table). Not free: bookkeeping overhead, and readers can starve writers under a constant read load (use the fair constructor, or prefer `StampedLock`'s optimistic read on Java 8+).

### Q: What are the coordination utilities?

| Class | What it does | Reusable? |
|---|---|---|
| **`CountDownLatch`** | Threads wait until a count reaches 0 (`await`/`countDown`) | ❌ one-shot |
| **`CyclicBarrier`** | N threads wait for each other at a barrier, then all proceed | ✅ resets |
| **`Semaphore`** | Permits — caps concurrent access to a resource | ✅ |
| **`Phaser`** | Flexible multi-phase barrier with dynamic parties | ✅ |
| **`Exchanger`** | Two threads swap objects at a rendezvous | ✅ |

```java
CountDownLatch latch = new CountDownLatch(3);      // wait for 3 services to warm up
// each worker: ... latch.countDown();
latch.await();                                      // main proceeds after all 3

Semaphore sem = new Semaphore(5);                   // max 5 concurrent calls to a fragile API
sem.acquire(); try { callLegacyApi(); } finally { sem.release(); }
```

---

# Part 8 — wait / notify and inter-thread communication

### Q: Rules for `wait`/`notify`?

1. Must hold the object's monitor → call inside `synchronized` on that same object, or you get **`IllegalMonitorStateException`**.
2. **Always wait in a `while` loop**, never an `if` — because of **spurious wakeups** and because another thread may have consumed the condition between the notify and your reacquiring the lock.
3. Prefer **`notifyAll()`** over `notify()` — `notify()` wakes one arbitrary waiter, which may be waiting on a different condition, and the right thread sleeps forever (a "missed signal" hang).

```java
class BoundedBuffer<T> {
    private final Queue<T> q = new LinkedList<>();
    private final int cap;
    BoundedBuffer(int cap) { this.cap = cap; }

    public synchronized void put(T item) throws InterruptedException {
        while (q.size() == cap) wait();          // while, NOT if
        q.add(item);
        notifyAll();
    }

    public synchronized T take() throws InterruptedException {
        while (q.isEmpty()) wait();
        T item = q.poll();
        notifyAll();
        return item;
    }
}
```

> 💡 **The senior answer:** *"I know how to write producer–consumer with wait/notify, but in real code I'd use a `BlockingQueue` — `ArrayBlockingQueue` or `LinkedBlockingQueue` — because `put`/`take` already block correctly and there's no way to get the loop or the notify wrong."*

```java
BlockingQueue<Order> queue = new LinkedBlockingQueue<>(100);
// producer: queue.put(order);      blocks when full
// consumer: Order o = queue.take(); blocks when empty
```

---

# Part 9 — Deadlock, livelock, starvation

### Q: What is a deadlock? (asked verbatim at Mphasis L1)

**Two or more threads each holding a lock the other needs, so none can proceed — forever.**

```java
// 💣 classic lock-ordering deadlock
void transfer(Account from, Account to, BigDecimal amt) {
    synchronized (from) {
        synchronized (to) { from.debit(amt); to.credit(amt); }
    }
}
// Thread 1: transfer(A, B)  → holds A, wants B
// Thread 2: transfer(B, A)  → holds B, wants A   → both stuck
```

**The four Coffman conditions** — all must hold; break any one and deadlock is impossible:

1. **Mutual exclusion** — resources aren't shareable
2. **Hold and wait** — a thread holds one lock while requesting another
3. **No preemption** — locks can't be forcibly taken away
4. **Circular wait** — a cycle in the "waiting for" graph

### Q: How do you prevent it?

| Strategy | How |
|---|---|
| **Global lock ordering** ⭐ | Always acquire locks in the same order — e.g. by account id. Breaks *circular wait*. **This is the answer to give first.** |
| **Timeout** | `tryLock(2, SECONDS)` — back off, release everything, retry. Breaks *hold and wait*. |
| **Single coarse lock** | Fewer locks, no cycles. Costs concurrency. |
| **Avoid nested locks** | Don't call unknown/foreign code while holding a lock. |
| **Immutability / no shared state** | No lock, no deadlock. |

```java
// ✅ ordered acquisition
void transfer(Account a, Account b, BigDecimal amt) {
    Account first  = a.getId() < b.getId() ? a : b;
    Account second = a.getId() < b.getId() ? b : a;
    synchronized (first) {
        synchronized (second) { a.debit(amt); b.credit(amt); }
    }
}
```

### Q: How would you detect one in production?

> *"Take a thread dump — `jstack <pid>`, or jcmd/VisualVM/JMC. The JVM prints a `Found one Java-level deadlock:` section naming both threads and the monitors. That's also how I'd diagnose a hung app generally: dump twice, a minute apart, and compare — threads stuck on the same stack are the problem. `ThreadMXBean.findDeadlockedThreads()` can do it programmatically for a health check."*

### Q: Deadlock vs livelock vs starvation?

- **Deadlock** — everyone blocked, nothing moves, CPU idle.
- **Livelock** — threads keep *responding* to each other and changing state but make no progress (two people stepping aside in a corridor, repeatedly). CPU busy, work zero.
- **Starvation** — a thread is runnable but never scheduled — a low-priority thread against a greedy one, or a writer behind a stream of readers. Fair locks help.

---

# Part 10 — ExecutorService & thread pools

### Q: Why use a pool instead of `new Thread()`?

1. **Thread creation is expensive** — ~1MB stack each, plus an OS call. Pools reuse them.
2. **Bounded concurrency.** `new Thread()` per request means a traffic spike creates 10,000 threads and the JVM dies of `OutOfMemoryError: unable to create new native thread`.
3. **A queue + rejection policy** gives you backpressure instead of collapse.
4. Lifecycle, naming, monitoring, `Future` results — all built in.

```java
ExecutorService pool = Executors.newFixedThreadPool(10);
pool.submit(() -> doWork());
pool.shutdown();                                    // no new tasks; finishes queued work
if (!pool.awaitTermination(30, TimeUnit.SECONDS))
    pool.shutdownNow();                             // interrupt running tasks
```

| Factory | Behaviour | Watch out |
|---|---|---|
| `newFixedThreadPool(n)` | n threads, **unbounded** queue | Queue grows without limit → OOM |
| `newCachedThreadPool()` | Unbounded threads, 60s idle reap | A spike creates thousands of threads |
| `newSingleThreadExecutor()` | One thread, sequential | Fine for ordered work |
| `newScheduledThreadPool(n)` | Delayed/periodic | Replaces `Timer` (which dies on an uncaught exception) |
| `newWorkStealingPool()` | ForkJoinPool, per-thread deques | Good for many small CPU tasks |
| `newVirtualThreadPerTaskExecutor()` | Java 21, a virtual thread per task | Ideal for blocking I/O |

> ⚠️ **The senior answer:** *"In production I configure a `ThreadPoolExecutor` directly rather than using `Executors`, because the factory methods hide either an unbounded queue or unbounded thread creation — both of which fail as OOM under load."*

```java
new ThreadPoolExecutor(
    10, 20,                                    // core, max
    60L, TimeUnit.SECONDS,                     // idle keep-alive for non-core threads
    new ArrayBlockingQueue<>(500),             // BOUNDED queue = backpressure
    new ThreadFactoryBuilder().setNameFormat("order-%d").build(),   // named → readable dumps
    new ThreadPoolExecutor.CallerRunsPolicy()  // rejection: the submitter runs it → throttles input
);
```

**How the pool decides:** core threads first → then **queue** → only when the queue is full does it create up to `max` threads → then the **rejection policy** (`AbortPolicy` default/throws, `CallerRunsPolicy`, `DiscardPolicy`, `DiscardOldestPolicy`).

**Sizing:** CPU-bound ≈ `cores + 1`. I/O-bound ≈ `cores × (1 + waitTime/computeTime)` — much larger, because threads are mostly blocked.

---

# Part 11 — Callable, Future, CompletableFuture

### Q: `Runnable` vs `Callable`?

| | `Runnable` | `Callable<V>` |
|---|---|---|
| Method | `void run()` | `V call() throws Exception` |
| Returns a value | ❌ | ✅ |
| Can throw checked exceptions | ❌ | ✅ |
| Since | 1.0 | 1.5 |

### Q: What's wrong with `Future`?

`future.get()` **blocks**, there's no callback, and you can't chain or combine futures without blocking. `CompletableFuture` (Java 8) fixes all of that.

```java
CompletableFuture<User>  u = CompletableFuture.supplyAsync(() -> userClient.get(id), pool);
CompletableFuture<Order> o = CompletableFuture.supplyAsync(() -> orderClient.get(id), pool);

u.thenCombine(o, Dashboard::new)                     // two parallel calls, combined
 .thenApply(Dashboard::summarize)                    // transform, same thread
 .exceptionally(ex -> Dashboard.empty())             // recover
 .thenAccept(this::render);                          // terminal side effect
```

| Method | Meaning |
|---|---|
| `supplyAsync` / `runAsync` | Start async (with a value / without) |
| `thenApply` vs `thenCompose` | map vs **flatMap** — use `thenCompose` when the function itself returns a `CompletableFuture`, or you get `CF<CF<T>>` |
| `thenCombine` | Wait for **two** and merge |
| `allOf` / `anyOf` | Wait for all / first |
| `exceptionally` / `handle` / `whenComplete` | Recover / handle both outcomes / peek |
| `orTimeout(…)` (Java 9+) | Fail after a deadline |

> ⚠️ **Two traps:** (1) always pass **your own executor** — the default `ForkJoinPool.commonPool()` is shared JVM-wide and sized to `cores - 1`, so one blocking task can starve everything else, including parallel streams. (2) `join()` throws unchecked `CompletionException`; `get()` throws checked `ExecutionException`.

> 💡 **Your project line:** *"On the RoboGebra explanation flow, fetching the student profile and the question metadata were two independent service calls done sequentially. Moving them to `supplyAsync` + `thenCombine` on a dedicated pool cut the endpoint latency to roughly the slower of the two calls instead of their sum."*

---

# Part 12 — Concurrent collections

| Need | Use | Not |
|---|---|---|
| Map | **`ConcurrentHashMap`** | `Hashtable`, `synchronizedMap` |
| List, read-heavy | `CopyOnWriteArrayList` | `Vector` |
| Queue (producer–consumer) | `LinkedBlockingQueue`, `ArrayBlockingQueue` | hand-rolled wait/notify |
| Bounded, no capacity | `SynchronousQueue` (hand-off) | |
| Priority + blocking | `PriorityBlockingQueue` | |
| Delayed tasks | `DelayQueue` | |
| Non-blocking queue/deque | `ConcurrentLinkedQueue/Deque` | |
| Sorted concurrent map | `ConcurrentSkipListMap` | `TreeMap` |

**`ConcurrentHashMap` in one breath:** *"Same structure as `HashMap` — bucket array, chains that treeify at 8 — but it CASes into empty bins and `synchronized`es on the bin's head node for the rest, so lock granularity is one bucket. No nulls. Iterators are weakly consistent, so no `ConcurrentModificationException`. Compound operations still need `putIfAbsent`/`computeIfAbsent`/`merge` to be atomic."* (Full detail in **[31 — HashMap internals](./31-hashmap-internals.md)**.)

**`CopyOnWriteArrayList`:** every write copies the whole backing array. Reads are lock-free and never throw CME. Perfect for **listener lists** — many reads, near-zero writes. Terrible for anything write-heavy (O(n) per add).

### Q: `Collections.synchronizedList` — what's the catch?

Each method is synchronized, but **iteration is not atomic** — you must synchronize manually, and even then you hold a global lock for the whole loop:

```java
List<String> list = Collections.synchronizedList(new ArrayList<>());
synchronized (list) {                     // required, or CME
    for (String s : list) { ... }
}
```

### Q: `ThreadLocal`?

A per-thread copy of a variable — no sharing, therefore no synchronization. Used by Spring for the transaction/`EntityManager` binding, by SLF4J's MDC for correlation ids, and historically for `SimpleDateFormat` (which is not thread-safe).

```java
private static final ThreadLocal<SimpleDateFormat> FMT =
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
```

> ⚠️ **Memory leak warning — say this unprompted:** *"In a thread pool, threads are reused forever, so a `ThreadLocal` value is never garbage collected unless you `remove()` it in a `finally`. In a web container that leaks per-request objects and can eventually OOM — and it also leaks *data* across requests, which is a security issue."* Modern code prefers `java.time` (immutable, thread-safe) over `SimpleDateFormat` entirely.

---

# Part 13 — Thread-safe singleton (the live-coding one)

**Asked at Mphasis L1 — be able to type all four and rank them.**

```java
// 1️⃣ Eager — simple and thread-safe (class loading is guaranteed thread-safe by the JVM)
public class Eager {
    private static final Eager INSTANCE = new Eager();
    private Eager() {}
    public static Eager getInstance() { return INSTANCE; }
}

// 2️⃣ Synchronized accessor — correct, but every call pays for a lock
public class Sync {
    private static Sync instance;
    private Sync() {}
    public static synchronized Sync getInstance() {
        if (instance == null) instance = new Sync();
        return instance;
    }
}

// 3️⃣ Double-checked locking — the one they want, and volatile is the point
public class Dcl {
    private static volatile Dcl instance;      // ⚠️ volatile is NOT optional
    private Dcl() {}
    public static Dcl getInstance() {
        if (instance == null) {                // 1st check — no lock, fast path
            synchronized (Dcl.class) {
                if (instance == null) {        // 2nd check — someone may have won the race
                    instance = new Dcl();
                }
            }
        }
        return instance;
    }
}

// 4️⃣ Bill Pugh holder idiom — lazy, no locking, no volatile. Cleanest.
public class Holder {
    private Holder() {}
    private static class H { static final Holder INSTANCE = new Holder(); }
    public static Holder getInstance() { return H.INSTANCE; }   // class loaded on first use
}

// 5️⃣ Enum — the Effective Java answer; serialization- and reflection-proof
public enum EnumSingleton {
    INSTANCE;
    public void doWork() { }
}
```

### Q: Why is `volatile` mandatory in double-checked locking?

**The killer follow-up.** `instance = new Dcl()` is three steps: allocate memory, run the constructor, assign the reference. The JVM is allowed to **reorder** steps 2 and 3. Another thread doing the first (unlocked) null check can then see a **non-null reference to a half-constructed object** and use it — reading default-valued fields, or crashing. `volatile` inserts the memory barrier that forbids that reordering (and guarantees visibility). Before Java 5, the memory model was too weak for DCL to work at all — which is why the holder idiom became popular.

**What breaks a singleton anyway:** reflection (`setAccessible(true)` on the private constructor), serialization (each deserialize creates a new one unless you add `readResolve()`), and cloning. **Only `enum` is immune to all three.**

> 💡 **Bonus that always lands:** *"A Spring `@Service` is a singleton too, but per **ApplicationContext**, not per JVM — it's a container guarantee, not the GoF pattern. And it's only thread-safe if it's stateless: mutable instance fields on a singleton bean are shared across every concurrent request. That's the single most common thread-safety bug I look for in Spring code."*

---

# Part 14 — Classic live-coding programs

### 1. Print odd and even alternately with two threads

```java
class OddEven {
    private int n = 1;
    private final int max = 10;

    public synchronized void printOdd() throws InterruptedException {
        while (n <= max) {
            while (n % 2 == 0) wait();
            System.out.println("Odd : " + n++);
            notifyAll();
        }
    }
    public synchronized void printEven() throws InterruptedException {
        while (n <= max) {
            while (n % 2 == 1) wait();
            System.out.println("Even: " + n++);
            notifyAll();
        }
    }
}
```

### 2. Producer–consumer with `BlockingQueue` (the version to write first)

```java
BlockingQueue<Integer> q = new ArrayBlockingQueue<>(5);

Thread producer = new Thread(() -> {
    try { for (int i = 1; i <= 20; i++) { q.put(i); System.out.println("produced " + i); } }
    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});

Thread consumer = new Thread(() -> {
    try { while (true) { System.out.println("consumed " + q.take()); } }
    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});
producer.start(); consumer.start();
```

### 3. Run N tasks in parallel and collect results

```java
ExecutorService pool = Executors.newFixedThreadPool(4);
List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2, () -> 3);
List<Future<Integer>> futures = pool.invokeAll(tasks);      // blocks until ALL are done
int sum = 0;
for (Future<Integer> f : futures) sum += f.get();
pool.shutdown();
```

### 4. Thread-safe counter three ways — see Part 3.

### 5. Wait for N services to start

```java
CountDownLatch latch = new CountDownLatch(3);
for (Service s : services) pool.submit(() -> { s.start(); latch.countDown(); });
latch.await(30, TimeUnit.SECONDS);
System.out.println("all services up");
```

---

# Part 15 — Multithreading in Spring Boot (your real answer)

**They will ask "where have you used threads?" — do not say "I haven't".** You have, through the framework:

> *"Most of my concurrency is framework-level rather than raw threads. Every HTTP request in Spring Boot runs on its own Tomcat worker thread, so **any singleton bean with mutable state is a shared-mutable-state bug** — I keep `@Service` beans stateless and pass state as method parameters. Where I did explicit async work: `@Async` with a configured `ThreadPoolTaskExecutor` for fire-and-forget notification sending, and `CompletableFuture` to parallelise two independent downstream calls that had been sequential. And on the data side, optimistic locking with `@Version` for concurrent edits of the same record."*

```java
@Configuration @EnableAsync
class AsyncConfig {
    @Bean("notificationExecutor")
    public Executor executor() {
        ThreadPoolTaskExecutor e = new ThreadPoolTaskExecutor();
        e.setCorePoolSize(5);
        e.setMaxPoolSize(10);
        e.setQueueCapacity(100);                 // bounded
        e.setThreadNamePrefix("notify-");
        e.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return e;
    }
}

@Service
class NotificationService {
    @Async("notificationExecutor")
    public void send(Notification n) { /* runs off the request thread */ }
}
```

**The three `@Async` traps** (excellent follow-up material):

1. **Self-invocation doesn't work** — `@Async` is proxy-based, so calling `this.send(...)` from inside the same bean runs synchronously. Same rule as `@Transactional`.
2. **Must be `public`**, and the return type must be `void`, `Future`, or `CompletableFuture`.
3. **Exceptions vanish** on `void` methods unless you register an `AsyncUncaughtExceptionHandler`.
4. **Context doesn't propagate** — `SecurityContextHolder`, the transaction, and MDC are `ThreadLocal`-based, so the async thread sees none of it unless you configure propagation. `@Transactional` on an `@Async` method starts a **different** transaction.

---

# Part 16 — Virtual threads (Java 21)

> Worth two sentences — it signals you keep current. Don't oversell it if the project is on Java 17.

**Platform threads** map 1:1 to OS threads (~1MB stack, thousands max). **Virtual threads** are JVM-scheduled, cheap (a few hundred bytes, millions possible) and, when they block on I/O, they **unmount** from their carrier thread instead of holding it.

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (var task : tasks) executor.submit(task);
}   // close() waits for all — try-with-resources
```

**Say:** *"They make the simple blocking style scale like reactive code for I/O-bound work — a thread per request becomes viable again. They do **not** speed up CPU-bound work, and `synchronized` blocks used to pin a virtual thread to its carrier, so `ReentrantLock` was preferred there. RoboGebra is on Java 17, so this is reading rather than production experience for me."*

---

# Part 17 — Predict the output

**Q1**
```java
Thread t = new Thread(() -> System.out.println("A"));
t.run();
System.out.println("B");
```
<details><summary>Answer</summary>`A` then `B`, both on **main**. `run()` is a plain method call — no thread is started.</details>

**Q2**
```java
Thread t = new Thread(() -> System.out.println("A"));
t.start();
t.start();
```
<details><summary>Answer</summary>Prints `A`, then throws **`IllegalThreadStateException`** on the second `start()`.</details>

**Q3**
```java
class C { int n = 0; void inc() { n++; } }
// 1000 threads each call inc() once
```
<details><summary>Answer</summary>Anything **≤ 1000**, typically slightly under. Lost updates because `n++` is read-modify-write. Non-deterministic — it may print 1000 on some runs, which is what makes these bugs so hard to catch in testing.</details>

**Q4**
```java
synchronized (this) {
    Thread.sleep(5000);
}
```
<details><summary>Answer</summary>Compiles (if `InterruptedException` is handled) and **holds the lock for the full 5 seconds** — every other thread needing that monitor is blocked. `sleep` does not release locks. This is a design smell, not an error.</details>

**Q5**
```java
public class T {
    public static void main(String[] a) {
        Thread t = new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
            System.out.println("worker done");
        });
        t.setDaemon(true);
        t.start();
        System.out.println("main done");
    }
}
```
<details><summary>Answer</summary>Prints only `main done`. The daemon thread is killed when main (the last user thread) exits — `worker done` never prints. Remove `setDaemon(true)` and both print.</details>

**Q6**
```java
private static int count = 0;
public static synchronized void a() { count++; }
public void b() { synchronized (this) { count++; } }
```
<details><summary>Answer</summary>**Not thread-safe together.** `a()` locks `T.class`, `b()` locks `this` — two different monitors guarding the same static field. Both must lock the same object.</details>

**Q7**
```java
List<Integer> list = new ArrayList<>();
// 10 threads each add 100 elements
System.out.println(list.size());
```
<details><summary>Answer</summary>Usually **< 1000**, and it can also throw `ArrayIndexOutOfBoundsException` or `NullPointerException` — concurrent `add` can interleave with an internal grow and corrupt the array. Use `Collections.synchronizedList`, `CopyOnWriteArrayList`, or per-thread lists merged at the end.</details>

**Q8**
```java
CompletableFuture.supplyAsync(() -> { throw new RuntimeException("boom"); })
                 .thenApply(x -> "never")
                 .exceptionally(ex -> "recovered: " + ex.getMessage());
```
<details><summary>Answer</summary>Result is `recovered: java.util.concurrent.CompletionException: java.lang.RuntimeException: boom`. `thenApply` is **skipped** on the exceptional path, and the original exception arrives **wrapped** in a `CompletionException` — a detail people get wrong. Also: nothing prints unless you `join()` or add a terminal stage.</details>

---

# Part 18 — Rapid-fire table

| Question | Answer |
|---|---|
| `start()` vs `run()` | `start()` creates a thread; `run()` is a normal call on the current thread |
| Thread states | NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED |
| `sleep` vs `wait` | `sleep` keeps locks (Thread, static); `wait` releases the monitor (Object, needs synchronized) |
| `wait` in `if` or `while`? | **`while`** — spurious wakeups and stolen conditions |
| `notify` vs `notifyAll` | `notifyAll` is safe; `notify` can wake the wrong waiter and hang |
| Three thread-safety problems | Atomicity, visibility, ordering |
| What `volatile` gives | Visibility + ordering. **Not** atomicity |
| Does `volatile` fix `i++`? | No — use `AtomicInteger` or a lock |
| What `synchronized` gives | Mutual exclusion + visibility + ordering; reentrant; releases on exception |
| Instance vs static synchronized | Different locks (`this` vs `Class`) — they don't exclude each other |
| CAS | compare-and-swap; lock-free optimistic retry loop behind `Atomic*` |
| `ReentrantLock` over `synchronized` | tryLock, timeout, interruptible, fairness, multiple conditions |
| Deadlock's 4 conditions | Mutual exclusion, hold-and-wait, no preemption, circular wait |
| Best deadlock prevention | Global lock **ordering** |
| Detect a deadlock | `jstack` thread dump → "Found one Java-level deadlock" |
| Livelock | Threads keep changing state but make no progress |
| Why pools | Thread creation cost + bounded concurrency + backpressure |
| `Executors` factory risk | Unbounded queue (fixed) or unbounded threads (cached) → OOM |
| Pool sizing | CPU-bound ≈ cores+1; I/O-bound much larger |
| `Runnable` vs `Callable` | Callable returns a value and can throw checked exceptions |
| `Future` limitation | `get()` blocks, no chaining → use `CompletableFuture` |
| `thenApply` vs `thenCompose` | map vs flatMap |
| CHM lock granularity | CAS on empty bin; `synchronized` on the bin head node |
| `ThreadLocal` risk | Leaks in pooled threads unless `remove()` in a finally |
| `@Async` traps | Self-invocation, must be public, swallowed exceptions, no context propagation |
| Virtual threads | Cheap JVM-scheduled threads; unmount on blocking I/O; Java 21 |
| Stop a thread properly | Interrupt or a `volatile` flag — never `stop()` |

---

# Part 19 — Drills

1. Why is `count++` unsafe but `count = 5` (an int assignment) atomic?
<details><summary>Answer</summary>`count++` is read-modify-write — three steps that can interleave. An `int` assignment is a single atomic store per the JMM. Note the exception: `long`/`double` writes are **not** guaranteed atomic on 32-bit JVMs unless declared `volatile`.</details>

2. Someone writes `volatile int count; count++;` and calls it thread-safe. What do you say?
<details><summary>Answer</summary>Volatile fixes visibility, not atomicity. Two threads can still both read 5 and both write 6. Use `AtomicInteger.incrementAndGet()` or synchronize.</details>

3. Your service singleton has `private SimpleDateFormat fmt = new SimpleDateFormat(...)`. What's the bug?
<details><summary>Answer</summary>`SimpleDateFormat` is mutable and not thread-safe; a Spring singleton is shared across all request threads → garbled or wrong dates, sometimes exceptions. Fix: use `DateTimeFormatter` (immutable, thread-safe), or a local instance, or `ThreadLocal`.</details>

4. Two threads deadlock in production. Walk me through your first five minutes.
<details><summary>Answer</summary>Confirm symptoms (requests hanging, CPU low), `jstack <pid>` twice a minute apart, look for the "Found one Java-level deadlock" block and for threads stuck on the same monitor across both dumps, identify the two lock-acquisition paths, then fix by enforcing a consistent lock order or introducing `tryLock` with a timeout.</details>

5. When is `CopyOnWriteArrayList` the right choice, and when is it a disaster?
<details><summary>Answer</summary>Right for read-mostly, tiny, rarely-written lists — listener/observer registries. A disaster for write-heavy or large lists: every single `add` copies the whole array (O(n)), so a loop of 10k adds is 10k array copies.</details>

6. You have 100 independent REST calls to make, each ~200ms. Sequential = 20s. How do you fix it, and what's the risk?
<details><summary>Answer</summary>Fan out with `CompletableFuture.supplyAsync` on a **dedicated bounded pool** (or a virtual-thread executor on 21), combine with `allOf`. Risks: overwhelming the downstream service (rate-limit / use a `Semaphore`), exhausting connection-pool capacity, and no per-call timeout — add `orTimeout` and a fallback. Never use the default common pool for blocking I/O.</details>

7. Why prefer `BlockingQueue` over `wait/notify` for producer–consumer?
<details><summary>Answer</summary>It's already correct: blocking `put`/`take`, bounded capacity giving backpressure, no chance of a missed signal or an `if` instead of `while`. Less code, no subtle bug surface.</details>

---

**Related files:** [05 — Core Java](./05-java.md) · [30 — Mphasis L2 §C concurrency](./30-mphasis-level2-client-round.md) · [31 — HashMap internals](./31-hashmap-internals.md) · [23 — Predict the Output](./23-java-output-tricky-questions.md)
