# 🔴 Java Multithreading & Concurrency — Easy Version

The topic that has now appeared in **Mphasis L1** (*"write a thread-safe singleton"*, *"what is deadlock?"*) and **Virtusa Round 2**. It is asked at every experienced-level Java round, and it's where 4-year candidates get separated from 1-year candidates — because the follow-up is never "what is a thread", it's **"why does that break?"**

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

### The one idea that organises the whole topic ⭐

```
Thread safety is THREE separate problems:

1. ATOMICITY  → "did my whole operation finish without interruption?"
2. VISIBILITY → "can the other thread SEE my change?"
3. ORDERING   → "did the CPU run my lines in the order I wrote them?"
```

And each tool solves a different subset:

```
synchronized   → ✅ atomicity  ✅ visibility  ✅ ordering    (all three)
volatile       → ❌ atomicity  ✅ visibility  ✅ ordering    (NOT atomicity!)
AtomicInteger  → ✅ atomicity on ONE variable, lock-free
```

> 💡 **The one-liner that impresses:** *"Thread safety is three separate problems — atomicity, visibility and ordering. `synchronized` gives you all three; `volatile` gives you visibility and ordering but **not** atomicity; atomics give you atomicity on a single variable. Choosing the wrong one is where concurrency bugs come from."*

---

### The terms

| Term | One-line definition |
|---|---|
| **Process** | an independent program with its **own memory space** |
| **Thread** | a unit of execution **inside** a process, sharing its heap; has its own stack and program counter |
| **Concurrency** | multiple tasks **making progress** in overlapping time (may be interleaved on 1 core) |
| **Parallelism** | multiple tasks **executing at the same instant** on multiple cores |
| **Race condition** | the result depends on unpredictable thread timing |
| **Critical section** | code that must be executed by only one thread at a time |
| **Mutual exclusion** | the guarantee that only one thread is in the critical section |
| **Atomicity** | an operation completes fully or not at all — no visible half-done state |
| **Visibility** | one thread's write becomes observable to another thread |
| **Ordering** | whether instructions may be reordered by the compiler or CPU |
| **Context switch** | the OS saving one thread's state and restoring another's — not free |

**Concurrency vs parallelism — the easy picture:**

```
CONCURRENCY (1 cook, 2 dishes)
  chop → stir → chop → stir → chop     ← switching between tasks

PARALLELISM (2 cooks, 2 dishes)
  cook A: chop chop chop
  cook B: stir stir stir               ← genuinely at the same time
```

---

### What each thread owns vs shares

```
        PROCESS (the JVM)
 ┌────────────────────────────────────┐
 │  HEAP  (objects, static fields)    │  ← SHARED → needs synchronization ⚠️
 │  Metaspace (class data)            │  ← shared
 ├────────────────────────────────────┤
 │  Thread-1: stack, PC, locals       │  ← PRIVATE ✅
 │  Thread-2: stack, PC, locals       │  ← PRIVATE ✅
 └────────────────────────────────────┘
```

Real-world idea: a **shared office**.

```
Your own desk drawer (stack)  → nobody else can touch it → always safe ✅
The shared printer (heap)     → everyone uses it → you need a queue/lock ⚠️
```

**Say it:** *"Local primitives are always thread-safe because they live on the thread's own stack. The moment state is on the heap and shared — an instance field, a static field, a collection — you need synchronization."*

---

# Part 1 — Creating threads

### Q: What are the ways to create a thread?

```java
// 1. extend Thread — uses up your ONE inheritance slot ❌
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

// 4. Callable + ExecutorService — returns a value, can throw ✅✅ (real production code)
ExecutorService pool = Executors.newFixedThreadPool(4);
Future<Integer> f = pool.submit(() -> 42);
System.out.println(f.get());
pool.shutdown();

// 5. Java 21 virtual threads
Thread.startVirtualThread(() -> System.out.println("virtual"));
```

#### Why `Runnable` over `Thread`?

```
1. Java allows only ONE parent class — don't waste it on Thread
2. Separates WHAT to do (Runnable) from WHO runs it (Thread)
3. The task can be REUSED across pools and executors ⭐
```

Real-world idea: a **delivery company**.

```
extends Thread      → every parcel becomes its own delivery boy 😩
implements Runnable → parcels (tasks) go to a pool of delivery boys ✅
```

In production you rarely create a `Thread` at all — you submit tasks to an `ExecutorService`.

---

### Q: `start()` vs `run()` — the most-asked trap ⭐

```java
Thread t = new Thread(() -> System.out.println(Thread.currentThread().getName()));

t.start();   // prints "Thread-0"  → a NEW thread; the JVM calls run() on it ✅
t.run();     // prints "main"      → a plain method call, NO new thread at all ❌
```

```
start()
   ↓
JVM asks the OS for a new thread + a new stack
   ↓
that new thread calls run()          ← real concurrency ✅

run()
   ↓
just an ordinary method call on the CURRENT thread   ← nothing concurrent ❌
```

Interviewers love this because the wrong version **compiles and appears to work**.

---

### Q: What happens if you call `start()` twice?

**`IllegalThreadStateException`.**

```java
t.start();
t.start();      // 💥
```

A `Thread` object is **single-use**. Once it has terminated it can never be restarted — you must create a new one. (Another reason thread pools exist: they reuse the *thread*, not the *Thread object's task*.)

Real-world idea: a **used train ticket**. It got you there once; you cannot travel on it again.

---

### Q: Daemon vs user thread?

```
USER thread   → keeps the JVM ALIVE until it finishes ✅
DAEMON thread → does NOT keep the JVM alive; killed abruptly when the
                last user thread ends (no `finally`, no cleanup) ⚠️
```

```java
Thread t = new Thread(() -> cleanupLoop());
t.setDaemon(true);       // ⚠️ must be called BEFORE start()
t.start();
```

The GC and JIT compiler threads are daemons.

```
✅ Use daemons for: background housekeeping, monitoring, cache refresh
❌ NEVER for:      writing a file, flushing a queue, anything that must finish
```

Real-world idea: the **office cleaner**. When the last employee leaves, the building closes and the cleaner goes home — even mid-mop.

---

# Part 2 — Thread lifecycle

The easiest way to remember:

```
NEW → RUNNABLE → (BLOCKED / WAITING / TIMED_WAITING) → TERMINATED
```

```
                 start()                 scheduler picks it
      NEW ─────────────────▶ RUNNABLE ◀──────────────────┐
                                │  │                     │
     run() returns / throws     │  │  synchronized entry │ lock acquired
                                │  └────────▶ BLOCKED ───┘
                                │
                                │  wait() / join() / park()   notify()/notifyAll()/join done
                                ├────────▶ WAITING ──────────────────┐
                                │                                    │
                                │  sleep(t) / wait(t) / join(t)      │ timeout or notify
                                ├────────▶ TIMED_WAITING ────────────┤
                                │                                    │
                                ▼                                    │
                           TERMINATED  ◀──────────────────────────────┘
```

**The six states are exactly `Thread.State`:**

```
NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
```

Real-world idea — a **doctor's clinic**:

```
NEW           → you decide to visit the doctor (haven't left home)
RUNNABLE      → you're in the waiting room with a token
BLOCKED       → the consulting room is occupied by another patient (forced wait)
TIMED_WAITING → the nurse says "come back in 10 minutes"
WAITING       → "wait until we call your name" (no fixed time)
TERMINATED    → consultation over; the token cannot be reused
```

> ⚠️ **Trap:** *"Is there a RUNNING state?"* — **No.** Java collapses "ready to run" and "actually on a CPU" into **RUNNABLE**; which one it is right now is the OS scheduler's business, not the JVM's.

### BLOCKED vs WAITING — the follow-up

```
BLOCKED → wants to ENTER a synchronized block, but someone holds the lock
          → wakes automatically when the lock is free
          → the thread did NOT choose this

WAITING → called wait() / join() and gave up voluntarily
          → needs notify() / notifyAll() to wake up
          → the thread CHOSE to wait
```

---

### Q: `sleep()` vs `wait()` — asked in nearly every round ⭐

The one line that answers it:

```
sleep() KEEPS the lock 🔒        wait() RELEASES the lock 🔓
```

| | `Thread.sleep(ms)` | `obj.wait()` |
|---|---|---|
| Defined in | `Thread` (static) | `Object` |
| Releases the lock? | ❌ **keeps** every lock it holds | ✅ **releases** that object's monitor |
| Needs a synchronized block? | no | **yes** — else `IllegalMonitorStateException` |
| Woken by | timeout / interrupt | `notify()`, `notifyAll()`, timeout, interrupt |
| State | `TIMED_WAITING` | `WAITING` (or `TIMED_WAITING` with a timeout) |
| Purpose | pause | **coordinate** between threads |

Real-world idea:

```
sleep() → you fall asleep INSIDE the bathroom with the door locked 🔒
          nobody else can get in for the whole duration

wait()  → you step OUT, unlock the door, and wait to be called 🔓
          someone else can go in and change the situation
```

**The sentence:** *"`sleep` is a pause that keeps the lock — using it inside a synchronized block blocks everyone else for the whole duration. `wait` releases the lock so another thread can make the condition true and notify you."*

---

### Q: `join()`, `yield()`, `interrupt()`?

```java
t.join();            // the CALLING thread waits until t finishes
Thread.yield();      // a HINT: "I'm willing to give up the CPU" — no guarantee
t.interrupt();       // COOPERATIVE cancellation — it does NOT stop the thread
```

**`join()`** — waiting for results before aggregating:

```
main ──▶ starts t1, t2
main ──▶ t1.join()  → waits here until t1 finishes
main ──▶ t2.join()  → waits here until t2 finishes
main ──▶ now safely reads both results ✅
```

**`interrupt()`** is the one people misunderstand:

```
interrupt() does NOT kill the thread. ⭐

It sets a FLAG. If the thread is in sleep/wait/join it throws
InterruptedException — AND CLEARS the flag.
```

```java
// ✅ correct handling — never swallow it
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();   // restore the flag for callers above you
    return;                               // and actually STOP doing work
}
```

```java
// ❌ the classic bug — swallowing it
catch (InterruptedException e) { }        // the cancellation signal is lost forever
```

Real-world idea: `interrupt()` is a **polite knock on the door** saying "please wrap up". It is not someone dragging you out.

> ⚠️ `stop()`, `suspend()` and `resume()` are **deprecated and dangerous**. `stop()` releases all locks instantly, leaving shared objects half-modified. The only sanctioned way to stop a thread is to ask it to finish — a `volatile boolean` flag or interruption.

---

# Part 3 — The core problem: race conditions

### Q: Show me a race condition.

```java
class Counter {
    private int count = 0;
    public void increment() { count++; }        // 💣 NOT atomic
    public int get() { return count; }
}
```

```java
Counter c = new Counter();
ExecutorService pool = Executors.newFixedThreadPool(10);
for (int i = 0; i < 1000; i++) pool.submit(c::increment);
pool.shutdown();
pool.awaitTermination(1, TimeUnit.MINUTES);

System.out.println(c.get());     // expected 1000 — prints 973, 991, 1000, 987...
```

**Why?** `count++` is **three** bytecode operations:

```
1. READ  count
2. ADD   1
3. WRITE count
```

And two threads can slot into each other's gaps:

```
Thread A reads  count = 5
Thread B reads  count = 5      ← before A wrote back
Thread A writes count = 6
Thread B writes count = 6      ← A's increment VANISHED ("lost update") 💥
```

Real-world idea: **two people editing the same Excel file on a shared drive.** Both open the version with 5 rows, both add a row, both save. The file ends up with 6 rows, not 7. One person's work is silently gone.

⭐ Note it prints 1000 *sometimes*. That non-determinism is exactly what makes these bugs escape testing and appear in production.

---

### Three correct fixes — know all three and when to use each

```java
// 1. LOCK — works, coarsest, simplest
public synchronized void increment() { count++; }

// 2. CAS — best for a single counter ⭐
private final AtomicInteger count = new AtomicInteger();
public void increment() { count.incrementAndGet(); }

// 3. LongAdder — best under HIGH contention
private final LongAdder count = new LongAdder();
public void increment() { count.increment(); }     // read with count.sum()
```

```
Few threads / simple           → synchronized
One counter, moderate traffic  → AtomicInteger ⭐
Hot metrics counter, many threads → LongAdder (striped cells)
```

> ⚠️ **`volatile` does NOT fix this.**
>
> ```java
> volatile int count;
> count++;        // STILL BROKEN 💥
> ```
>
> `volatile` guarantees you read a *fresh* value — not that read-modify-write is atomic. Stating this correctly is a strong signal.

---

# Part 4 — synchronized

### Q: What does `synchronized` actually do?

The easiest way to remember:

```
Every Java object has a MONITOR (an intrinsic lock — one key 🔑).

synchronized = take the key on entry, give it back on exit
               (including when an exception is thrown ✅)
```

It provides **all three** guarantees:

```
1. MUTUAL EXCLUSION → only one thread inside at a time
2. VISIBILITY       → releasing the lock flushes your changes to main memory,
                      acquiring it refreshes your view  ⭐
3. ORDERING         → no reordering across the lock boundary
```

Point 2 is the one candidates forget, and it is half the value of `synchronized`.

Real-world idea: a **single toilet with one key**. One person inside at a time; and whatever they did in there is visible to the next person.

---

### The three forms

```java
// 1. synchronized instance method → locks THIS
public synchronized void a() { }

// 2. synchronized static method → locks the CLASS object (MyClass.class)
public static synchronized void b() { }

// 3. synchronized block → locks whatever object you name — the PREFERRED form ⭐
private final Object lock = new Object();       // dedicated, private lock object
public void c() {
    // unsynchronized preamble — stays parallel
    synchronized (lock) {
        // ONLY the critical section
    }
}
```

```
Method-level → locks the ENTIRE method → slower 🐢
Block-level  → locks only the risky lines → faster ⚡
```

> 💡 **Say this trade-off:** *"I prefer a synchronized block on a private final lock object over a synchronized method. A synchronized method locks `this`, which any outside code can also lock — so a caller can accidentally block your class. And a block lets me keep the critical section as small as possible, which matters for throughput."*

Common mistakes:

```java
synchronized ("lock") { }          // ❌ String literals are POOLED and shared globally
private Integer lock = 0;
synchronized (lock) { lock++; }    // ❌ the lock object itself changes → broken

private final Object lock = new Object();
synchronized (lock) { }            // ✅ correct
```

---

### Q: Do an instance method and a static method block each other?

**No.** Different locks.

```java
public synchronized void a()        { count++; }   // locks THIS
public static synchronized void b() { count++; }   // locks MyClass.class
```

```
Thread 1 inside a()  🔑 this
Thread 2 inside b()  🔑 MyClass.class

Two DIFFERENT keys → both run at the same time → the shared field is corrupted 💥
```

A classic subtle bug when a class mixes instance and static synchronized state. Both must lock the **same** object.

---

### Q: Is `synchronized` reentrant?

**Yes.** A thread already holding a lock can acquire it again — the JVM keeps a hold count.

```java
synchronized void a() {
    b();                      // ✅ no self-deadlock
}
synchronized void b() { }
```

```
Thread enters a() → hold count 1
Thread enters b() → hold count 2       (same thread, same lock — allowed)
b() returns       → hold count 1
a() returns       → hold count 0 → lock released
```

Without reentrancy, a synchronized method could never call another synchronized method on the same object.

---

### Q: What is the cost?

```
Uncontended lock → cheap on modern JVMs (fast paths, historically biased locking)
CONTENDED lock   → expensive: threads park, the OS context-switches,
                   CPU caches invalidate 🐢
```

**Rules:**

```
✅ Hold locks for as SHORT a time as possible
❌ Never do I/O while holding a lock
❌ Never call unknown / external code while holding a lock
        (that's how you get lock-order inversions and deadlocks)
```

```java
// ❌ locks for 5 seconds to protect a 1-microsecond operation
synchronized void process() {
    callSlowApi();       // 5 seconds 🐢
    count++;
}

// ✅
void process() {
    callSlowApi();
    synchronized (lock) { count++; }
}
```

---

# Part 5 — volatile and the Java Memory Model

### Q: What does `volatile` guarantee?

```
✅ VISIBILITY — every read goes to main memory, every write is published
               immediately; no thread can see a stale cached copy
✅ ORDERING   — the compiler/CPU may not reorder around a volatile access
               (it inserts a memory barrier)

❌ ATOMICITY  — compound operations like i++ or check-then-act are STILL broken
```

The picture:

```
        MAIN MEMORY
        running = false
              │
      ┌───────┴────────┐
      ↓                ↓
  CPU Core 1       CPU Core 2
  cache copy       cache copy      ← WITHOUT volatile, each thread reads its own
                                     stale copy forever 💥

  WITH volatile → both go straight to MAIN MEMORY every time ✅
```

Real-world idea: a **shared office whiteboard**.

```
Without volatile → everyone keeps a personal notepad copy → they go stale
With volatile    → everyone must read the whiteboard every time ✅
```

---

### Q: Give the canonical example.

```java
class Worker implements Runnable {

    private volatile boolean running = true;   // ❌ without volatile this loops FOREVER

    public void run() {
        while (running) { /* work */ }
        System.out.println("stopped");
    }

    public void stop() { running = false; }
}
```

Why does it hang without `volatile`? Because the JIT compiler is **entitled** to hoist the read out of the loop — nothing *inside* the loop modifies `running`, so it effectively compiles to:

```java
if (running) { while (true) { /* work */ } }     // 💥 never re-reads the flag
```

The thread never sees the write and never stops.

```
This is a REAL production hang. The fix is one keyword.
```

---

### Q: volatile vs synchronized vs atomic — the comparison table

| | `volatile` | `synchronized` | `AtomicInteger` |
|---|---|---|---|
| Visibility | ✅ | ✅ | ✅ |
| Ordering | ✅ | ✅ | ✅ |
| Atomicity of `x++` | ❌ | ✅ | ✅ |
| Blocks threads? | ❌ never | ✅ | ❌ (CAS spin) |
| Scope | one variable | a block/method | one variable |
| Cost | cheapest | highest | in between |
| Use for | flags, and the DCL singleton reference | compound state, invariants over several fields | counters, sequences, single-reference swaps |

The decision line:

```
A simple FLAG (one writes, others read)        → volatile ⭐
A COUNTER (x++)                                → AtomicInteger ⭐
SEVERAL fields that must change together       → synchronized ⭐
```

---

### Q: What is "happens-before"?

The easiest way to remember:

```
"A happens-before B" = whatever A did is GUARANTEED VISIBLE to B.

Every synchronization tool exists only to create one of these edges.
```

The rules worth naming:

```
• Program order within a single thread
• Monitor UNLOCK happens-before a later LOCK of the same monitor
• A volatile WRITE happens-before a later volatile READ of the same field
• Thread.start() happens-before everything in that thread
• Everything in a thread happens-before another thread's successful join() on it
• Constructor completion happens-before a `final` field is read
      → which is why immutable objects are safely publishable ⭐
```

> 💡 **Say:** *"Concurrency bugs aren't really about 'two threads at once' — they're about the absence of a happens-before edge. Every synchronization tool is just a way of creating one."*

---

# Part 6 — Atomic classes & CAS

### Q: How do the `Atomic*` classes work without locks?

Via **CAS — compare-and-swap**, a single CPU instruction (`lock cmpxchg` on x86):

```
"If this memory location STILL holds the value I expected,
 replace it with the new value. Otherwise, tell me I failed."
```

```java
// conceptually, what incrementAndGet does:
int prev, next;
do {
    prev = get();
    next = prev + 1;
} while (!compareAndSet(prev, next));   // retry until nobody beat us to it
```

```
Thread A: reads 5 → wants to write 6 → is it still 5? YES → write ✅
Thread B: reads 5 → wants to write 6 → is it still 5? NO (it's 6) → RETRY
          reads 6 → wants to write 7 → is it still 6? YES → write ✅

Nothing was lost, and nobody was ever BLOCKED ⚡
```

This is **optimistic, non-blocking, lock-free**: no thread is ever suspended; a loser simply retries.

Real-world idea: booking the **last cinema seat online**. You click, the system checks "is seat 7 still free?" — if someone beat you, it says "try again" rather than making you stand in a queue.

```java
AtomicInteger  counter = new AtomicInteger(0);   counter.incrementAndGet();
AtomicLong     id      = new AtomicLong();       id.getAndIncrement();
AtomicBoolean  flag    = new AtomicBoolean();    flag.compareAndSet(false, true);  // run-once guard
AtomicReference<Config> cfg = new AtomicReference<>(initial);
cfg.updateAndGet(c -> c.withTimeout(30));        // lambda-based atomic update
```

**Trade-off to state:**

```
LOW contention  → CAS wins (no blocking) ⚡
HIGH contention → CAS retries burn CPU; a lock can actually win 🐢
                  → that's what LongAdder fixes: it spreads updates across
                    per-thread cells and sums them on read
```

`LongAdder` is ideal for hot metrics counters — but not for a value you must read exactly on every update.

> 💡 **The ABA problem** (if they go deep): a value changes A → B → A between your read and your CAS, so the CAS succeeds even though the world changed underneath. `AtomicStampedReference` adds a version stamp to detect it.

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

⚠️ That `finally` is the whole risk. Forget it and the lock is **never released** — the application freezes with no exception.

| Capability | `synchronized` | `ReentrantLock` |
|---|---|---|
| Try without blocking | ❌ | ✅ `tryLock()` |
| Timeout | ❌ | ✅ `tryLock(5, SECONDS)` — **deadlock escape hatch** ⭐ |
| Interruptible while waiting | ❌ | ✅ `lockInterruptibly()` |
| **Fairness** (longest waiter first) | ❌ | ✅ `new ReentrantLock(true)` |
| Multiple condition queues | ❌ (one wait-set) | ✅ `newCondition()` |
| Lock across methods | ❌ (block-scoped) | ✅ |
| Auto-release | ✅ | ❌ you must use `finally` |

Real-world idea — a **restaurant table**:

```
synchronized → you stand at the door waiting for a table, forever
tryLock()    → "is a table free?" → no → you go somewhere else ✅
```

**Say:** *"`synchronized` first — it's simpler and impossible to leak. I reach for `ReentrantLock` when I need `tryLock` with a timeout, interruptible acquisition, or separate condition queues."*

---

### Q: `ReadWriteLock`?

```java
ReadWriteLock rw = new ReentrantReadWriteLock();

rw.readLock().lock();    // MANY readers concurrently ✅
rw.writeLock().lock();   // exclusive — blocks all readers and writers
```

```
Readers:  R R R R R      ← all at once, they don't disturb each other
Writer:        W         ← alone; everyone else waits
```

Right for **read-heavy, write-rare** shared state — a cached config, a lookup table.

Not free, though: there is bookkeeping overhead, and readers can **starve** writers under a constant read load. Use the fair constructor, or prefer `StampedLock`'s optimistic read (Java 8+).

Real-world idea: a **notice board**. Any number of people can read it at once, but the person updating it needs everyone to step back.

---

### Q: What are the coordination utilities?

| Class | What it does | Reusable? |
|---|---|---|
| **`CountDownLatch`** | threads wait until a count reaches 0 (`await` / `countDown`) | ❌ one-shot |
| **`CyclicBarrier`** | N threads wait for each other at a barrier, then all proceed | ✅ resets |
| **`Semaphore`** | permits — caps concurrent access to a resource | ✅ |
| **`Phaser`** | flexible multi-phase barrier with dynamic parties | ✅ |
| **`Exchanger`** | two threads swap objects at a rendezvous | ✅ |

```java
CountDownLatch latch = new CountDownLatch(3);      // wait for 3 services to warm up
// each worker: ... latch.countDown();
latch.await();                                      // main proceeds after all 3

Semaphore sem = new Semaphore(5);                   // max 5 concurrent calls to a fragile API
sem.acquire();
try { callLegacyApi(); } finally { sem.release(); }
```

Real-world ideas:

```
CountDownLatch → a rocket launch countdown: 3… 2… 1… GO (one-shot)
CyclicBarrier  → a tour group: the bus leaves only when all 10 people are back
                 (and it does this at every stop — reusable)
Semaphore      → a parking lot with 5 spaces: 6th car waits for someone to leave
```

---

# Part 8 — wait / notify and inter-thread communication

### Q: Rules for `wait` / `notify`?

**Three rules. State all three.**

```
1. You must HOLD the object's monitor
      → call inside synchronized on that same object
      → otherwise: IllegalMonitorStateException 💥

2. ALWAYS wait in a WHILE loop, never an IF          ⭐⭐
      → spurious wakeups are legal
      → another thread may consume the condition between the notify
        and your reacquiring the lock

3. Prefer notifyAll() over notify()
      → notify() wakes ONE arbitrary waiter, which may be waiting on a
        different condition → the right thread sleeps forever (a hang)
```

```java
class BoundedBuffer<T> {

    private final Queue<T> q = new LinkedList<>();
    private final int cap;
    BoundedBuffer(int cap) { this.cap = cap; }

    public synchronized void put(T item) throws InterruptedException {
        while (q.size() == cap) wait();          // while, NOT if ⭐
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

The flow:

```
PRODUCER                          CONSUMER
   │ buffer FULL                     │
   │ wait() 🔓 releases the lock     │
   │                                 │ takes the lock 🔒
   │                                 │ consumes an item
   │                                 │ notifyAll() 📢
   │ ← wakes up                      │ releases the lock 🔓
   │ RE-CHECKS the while condition   │
   │ produces ✅                     │
```

Real-world idea: a **restaurant kitchen counter**. The chef puts a plate down and rings the bell 🔔; the waiter, who was waiting, hears it and collects. If the counter is full the chef waits; if it's empty the waiter waits.

> 💡 **The senior answer:** *"I know how to write producer–consumer with wait/notify, but in real code I'd use a `BlockingQueue` — `ArrayBlockingQueue` or `LinkedBlockingQueue` — because `put`/`take` already block correctly and there's no way to get the loop or the notify wrong."*

```java
BlockingQueue<Order> queue = new LinkedBlockingQueue<>(100);
// producer: queue.put(order);        blocks when full
// consumer: Order o = queue.take();  blocks when empty
```

---

# Part 9 — Deadlock, livelock, starvation

### Q: What is a deadlock? (asked verbatim at Mphasis L1)

```
Two or more threads, each holding a lock the other needs,
so none can ever proceed. 🔒↔🔒
```

```java
// 💣 classic lock-ordering deadlock
void transfer(Account from, Account to, BigDecimal amt) {
    synchronized (from) {
        synchronized (to) {
            from.debit(amt);
            to.credit(amt);
        }
    }
}
// Thread 1: transfer(A, B)  → holds A, wants B
// Thread 2: transfer(B, A)  → holds B, wants A   → both stuck forever
```

```
Thread 1                          Thread 2
   │ holds A 🔒                      │ holds B 🔒
   │ WANTS B ────────────────────────┤
   ├──────────────────────── WANTS A │
   ↓                                 ↓
waits forever                   waits forever   💀
```

Real-world idea: **two people sharing one plate of food.** A grabs the spoon and waits for the fork; B grabs the fork and waits for the spoon. Neither will let go. Nobody eats.

---

### The four Coffman conditions

All four must hold — break **any one** and deadlock becomes impossible:

```
1. MUTUAL EXCLUSION → resources aren't shareable
2. HOLD AND WAIT    → a thread holds one lock while requesting another
3. NO PREEMPTION    → locks can't be forcibly taken away
4. CIRCULAR WAIT    → a cycle in the "waiting for" graph   ← easiest to break ⭐
```

---

### Q: How do you prevent it?

| Strategy | How |
|---|---|
| **Global lock ordering** ⭐ | always acquire locks in the same order — e.g. by account id. Breaks *circular wait*. **Give this answer first.** |
| **Timeout** | `tryLock(2, SECONDS)` — back off, release everything, retry. Breaks *hold and wait*. |
| **Single coarse lock** | fewer locks, no cycles. Costs concurrency. |
| **Avoid nested locks** | never call unknown / foreign code while holding a lock. |
| **Immutability / no shared state** | no lock, no deadlock. |

```java
// ✅ ordered acquisition
void transfer(Account a, Account b, BigDecimal amt) {

    Account first  = a.getId() < b.getId() ? a : b;
    Account second = a.getId() < b.getId() ? b : a;

    synchronized (first) {
        synchronized (second) {
            a.debit(amt);
            b.credit(amt);
        }
    }
}
```

```
Both threads now take the LOWER id first, always.
A cycle can never form ✅
```

Back to the dinner table: agree that **everyone picks up the spoon before the fork**. Now the second person just waits for the spoon, and nobody is stuck holding half a set.

---

### Q: How would you detect one in production?

> *"Take a thread dump — `jstack <pid>`, or jcmd / VisualVM / JMC. The JVM prints a `Found one Java-level deadlock:` section naming both threads and the monitors. That's also how I'd diagnose a hung app generally: dump twice, a minute apart, and compare — threads stuck on the same stack are the problem. `ThreadMXBean.findDeadlockedThreads()` can do it programmatically for a health check."*

```bash
jps                 # find the process id
jstack <pid>        # → "Found one Java-level deadlock:"
```

---

### Q: Deadlock vs livelock vs starvation?

```
DEADLOCK   → everyone blocked, nothing moves, CPU IDLE 💤
LIVELOCK   → threads keep reacting to each other and changing state,
             but make no progress. CPU BUSY, work ZERO 🔁
STARVATION → a thread is runnable but never scheduled 😴
```

Real-world ideas:

```
Deadlock   → two cars nose-to-nose in a one-lane road, both refusing to reverse
Livelock   → two people in a corridor stepping aside at the same time,
             again and again, never passing 🔁
Starvation → a low-priority thread against a greedy one, or a writer
             stuck behind an endless stream of readers → use a FAIR lock
```

---
# Part 10 — ExecutorService & thread pools

### Q: Why use a pool instead of `new Thread()`?

```
1. Thread creation is EXPENSIVE — ~1MB stack each, plus an OS call
2. BOUNDED concurrency — new Thread() per request means a traffic spike
   creates 10,000 threads and the JVM dies with
   "OutOfMemoryError: unable to create new native thread" 💥
3. A queue + rejection policy gives BACKPRESSURE instead of collapse
4. Lifecycle, naming, monitoring and Future results — all built in
```

```
        TASK QUEUE
   [t1][t2][t3][t4] ... [t10000]
              │
              ↓ picked up one by one
   ┌────┬────┬────┬────┬────┐
   │ T1 │ T2 │ T3 │ T4 │ T5 │   ← only 5 threads, reused forever ✅
   └────┴────┴────┴────┴────┘
```

Real-world idea: a **restaurant**.

```
Without a pool → hire a NEW waiter for every customer, then fire him ❌
With a pool    → keep 5 waiters; each serves customer after customer ✅
```

```java
ExecutorService pool = Executors.newFixedThreadPool(10);
pool.submit(() -> doWork());

pool.shutdown();                                    // no new tasks; finish queued work
if (!pool.awaitTermination(30, TimeUnit.SECONDS))
    pool.shutdownNow();                             // interrupt running tasks
```

⚠️ If you never call `shutdown()`, the JVM **never exits** — the pool's non-daemon threads keep it alive.

---

### The factory methods, and what each one hides

| Factory | Behaviour | Watch out |
|---|---|---|
| `newFixedThreadPool(n)` | n threads, **unbounded** queue | the queue grows without limit → OOM |
| `newCachedThreadPool()` | unbounded threads, 60s idle reap | a spike creates thousands of threads |
| `newSingleThreadExecutor()` | one thread, sequential | fine for ordered work |
| `newScheduledThreadPool(n)` | delayed / periodic | replaces `Timer` (which dies on an uncaught exception) |
| `newWorkStealingPool()` | ForkJoinPool, per-thread deques | good for many small CPU tasks |
| `newVirtualThreadPerTaskExecutor()` | Java 21, a virtual thread per task | ideal for blocking I/O |

> ⚠️ **The senior answer:** *"In production I configure a `ThreadPoolExecutor` directly rather than using `Executors`, because the factory methods hide either an unbounded queue or unbounded thread creation — both of which fail as OOM under load."*

```java
new ThreadPoolExecutor(
    10, 20,                                    // core, max
    60L, TimeUnit.SECONDS,                     // idle keep-alive for non-core threads
    new ArrayBlockingQueue<>(500),             // BOUNDED queue = backpressure ⭐
    new ThreadFactoryBuilder().setNameFormat("order-%d").build(),   // named → readable dumps
    new ThreadPoolExecutor.CallerRunsPolicy()  // rejection: the submitter runs it → throttles input
);
```

---

### How the pool actually decides — the order surprises people ⭐

```
Task arrives
     │
     ├─ fewer than CORE threads?      → create a new thread ✅
     │
     ├─ else, is the QUEUE not full?  → QUEUE it  ⭐ (note: queue BEFORE max threads)
     │
     ├─ else, fewer than MAX threads? → create a new thread
     │
     └─ else                          → REJECTION POLICY
```

That middle step catches people out: the pool prefers **queuing** over growing to `max`. So with an unbounded queue, `maxPoolSize` is **never reached** — which is exactly why `newFixedThreadPool` can OOM on the queue rather than on threads.

The rejection policies:

```
AbortPolicy (default)  → throws RejectedExecutionException
CallerRunsPolicy       → the submitting thread runs it → natural throttling ⭐
DiscardPolicy          → silently drops the task 😬
DiscardOldestPolicy    → drops the oldest queued task
```

**Sizing:**

```
CPU-bound  ≈ cores + 1
I/O-bound  ≈ cores × (1 + waitTime / computeTime)   → much larger,
             because the threads are mostly blocked waiting
```

---

# Part 11 — Callable, Future, CompletableFuture

### Q: `Runnable` vs `Callable`?

```
Runnable → returns NOTHING, cannot throw a checked exception
Callable → RETURNS a value, CAN throw a checked exception ⭐
```

| | `Runnable` | `Callable<V>` |
|---|---|---|
| Method | `void run()` | `V call() throws Exception` |
| Returns a value | ❌ | ✅ |
| Can throw checked exceptions | ❌ | ✅ |
| Since | 1.0 | 1.5 |

Real-world idea: a **dry cleaner**.

```
Runnable → you drop off clothes and walk away, expecting nothing back
Callable → you drop off clothes and get a TOKEN (Future)
           → later you show the token and collect (future.get())
```

```java
Future<Integer> f = pool.submit(() -> 42);   // returns IMMEDIATELY (the token)
// ... do other work ...
Integer result = f.get();                    // NOW it blocks until ready
```

---

### Q: What's wrong with `Future`?

```
future.get() BLOCKS.
There's no callback.
You can't chain or combine futures without blocking. 🐢
```

`CompletableFuture` (Java 8) fixes all of that.

```java
CompletableFuture<User>  u = CompletableFuture.supplyAsync(() -> userClient.get(id), pool);
CompletableFuture<Order> o = CompletableFuture.supplyAsync(() -> orderClient.get(id), pool);

u.thenCombine(o, Dashboard::new)                     // two PARALLEL calls, combined ⭐
 .thenApply(Dashboard::summarize)                    // transform
 .exceptionally(ex -> Dashboard.empty())             // recover
 .thenAccept(this::render);                          // terminal side effect
```

```
SEQUENTIAL                       PARALLEL (thenCombine)
user call   200ms                user call  ─┐
order call  200ms                             ├─ 200ms total ⚡
──────────────────               order call ─┘
total 400ms 🐢
```

| Method | Meaning |
|---|---|
| `supplyAsync` / `runAsync` | start async (with a value / without) |
| `thenApply` vs `thenCompose` | **map vs flatMap** — use `thenCompose` when the function itself returns a `CompletableFuture`, or you get `CF<CF<T>>` |
| `thenCombine` | wait for **two** and merge |
| `allOf` / `anyOf` | wait for all / for the first |
| `exceptionally` / `handle` / `whenComplete` | recover / handle both outcomes / peek |
| `orTimeout(…)` (Java 9+) | fail after a deadline |

> ⚠️ **Two traps:**
>
> 1. **Always pass your own executor.** The default `ForkJoinPool.commonPool()` is shared JVM-wide and sized to `cores - 1`, so one blocking task can starve everything else — including every parallel stream in the application.
> 2. **`join()` throws unchecked `CompletionException`; `get()` throws checked `ExecutionException`.**

> 💡 **Your project line:** *"On the RoboGebra explanation flow, fetching the student profile and the question metadata were two independent service calls done sequentially. Moving them to `supplyAsync` + `thenCombine` on a dedicated pool cut the endpoint latency to roughly the slower of the two calls instead of their sum."*

---

# Part 12 — Concurrent collections

| Need | Use | Not |
|---|---|---|
| Map | **`ConcurrentHashMap`** | `Hashtable`, `synchronizedMap` |
| List, read-heavy | `CopyOnWriteArrayList` | `Vector` |
| Queue (producer–consumer) | `LinkedBlockingQueue`, `ArrayBlockingQueue` | hand-rolled wait/notify |
| Direct hand-off, no capacity | `SynchronousQueue` | |
| Priority + blocking | `PriorityBlockingQueue` | |
| Delayed tasks | `DelayQueue` | |
| Non-blocking queue/deque | `ConcurrentLinkedQueue` / `Deque` | |
| Sorted concurrent map | `ConcurrentSkipListMap` | `TreeMap` |

**`ConcurrentHashMap` in one breath:**

> *"Same structure as `HashMap` — bucket array, chains that treeify at 8 — but it CASes into empty bins and `synchronized`es on the bin's head node for the rest, so lock granularity is one bucket. No nulls. Iterators are weakly consistent, so no `ConcurrentModificationException`. Compound operations still need `putIfAbsent` / `computeIfAbsent` / `merge` to be atomic."*

(Full detail in **[31 — HashMap internals](./31-hashmap-internals.md)**.)

```
Hashtable          🔒 ONE lock for the whole map      → everyone queues 🐢
ConcurrentHashMap  🔒 one lock per BUCKET             → parallel writes ⚡
```

**`CopyOnWriteArrayList`:**

```
Every WRITE copies the entire backing array. O(n) per add 🐢
Every READ is lock-free and never throws CME ⚡
```

```
✅ Perfect for: listener lists, config caches — many reads, near-zero writes
❌ Terrible for: anything write-heavy or large
```

---

### Q: `Collections.synchronizedList` — what's the catch?

Each individual method is synchronized, but **iteration is not atomic**:

```java
List<String> list = Collections.synchronizedList(new ArrayList<>());

synchronized (list) {                     // required manually, or CME
    for (String s : list) { ... }
}
```

And even then you hold **one global lock** for the entire loop — no better than `Hashtable`. Prefer a real concurrent collection.

---

### Q: `ThreadLocal`?

```
ThreadLocal = a PRIVATE COPY of a variable for each thread.
              No sharing → no synchronization needed.
```

```java
private static final ThreadLocal<SimpleDateFormat> FMT =
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
```

```
Thread 1 → its own SimpleDateFormat
Thread 2 → its own SimpleDateFormat      ← never shared, never corrupted ✅
```

Real-world idea: instead of everyone sharing one office pen (and fighting over it), **each person gets their own pen**.

Used by Spring for the transaction / `EntityManager` binding, by SLF4J's MDC for correlation ids, and historically for `SimpleDateFormat` (which is not thread-safe).

> ⚠️ **Memory leak warning — say this unprompted:**
>
> *"In a thread pool, threads are reused forever, so a `ThreadLocal` value is never garbage collected unless you `remove()` it in a `finally`. In a web container that leaks per-request objects and can eventually OOM — and it also leaks **data** across requests, which is a security issue."*

```java
try {
    CONTEXT.set(userContext);
    doWork();
} finally {
    CONTEXT.remove();          // ⭐ mandatory in a pooled environment
}
```

Modern code prefers `java.time` (`DateTimeFormatter` — immutable and thread-safe) over `SimpleDateFormat` entirely.

---

# Part 13 — Thread-safe singleton (the live-coding one)

**Asked at Mphasis L1 — be able to type all five and rank them.**

```java
// 1️⃣ EAGER — simple and thread-safe (class loading is guaranteed thread-safe by the JVM)
public class Eager {
    private static final Eager INSTANCE = new Eager();
    private Eager() {}
    public static Eager getInstance() { return INSTANCE; }
}

// 2️⃣ SYNCHRONIZED accessor — correct, but EVERY call pays for a lock 🐢
public class Sync {
    private static Sync instance;
    private Sync() {}
    public static synchronized Sync getInstance() {
        if (instance == null) instance = new Sync();
        return instance;
    }
}

// 3️⃣ DOUBLE-CHECKED LOCKING — the one they want, and volatile is the point ⭐
public class Dcl {
    private static volatile Dcl instance;      // ⚠️ volatile is NOT optional
    private Dcl() {}
    public static Dcl getInstance() {
        if (instance == null) {                // 1st check — no lock, fast path ⚡
            synchronized (Dcl.class) {
                if (instance == null) {        // 2nd check — someone may have won the race
                    instance = new Dcl();
                }
            }
        }
        return instance;
    }
}

// 4️⃣ BILL PUGH HOLDER idiom — lazy, no locking, no volatile. Cleanest. ⭐
public class Holder {
    private Holder() {}
    private static class H { static final Holder INSTANCE = new Holder(); }
    public static Holder getInstance() { return H.INSTANCE; }   // inner class loaded on first use
}

// 5️⃣ ENUM — the Effective Java answer; serialization- and reflection-proof ⭐
public enum EnumSingleton {
    INSTANCE;
    public void doWork() { }
}
```

How to rank them out loud:

```
Eager   → fine if the object is cheap and always needed
Sync    → correct but slow (locks on EVERY call, forever)
DCL     → the classic answer; volatile is mandatory
Holder  → lazy + fast + no volatile → the cleanest lazy version ⭐
Enum    → the safest of all (immune to reflection and serialization)
```

---

### Q: Why is `volatile` mandatory in double-checked locking?

**The killer follow-up.**

```java
instance = new Dcl();
```

That single line is actually **three** steps:

```
1. allocate memory
2. run the constructor
3. assign the reference to `instance`
```

The JVM is **allowed to reorder steps 2 and 3**:

```
1. allocate memory
3. assign the reference      ← `instance` is now NON-NULL...
2. run the constructor       ← ...but the object isn't built yet! 💥
```

Now another thread doing the **first, unlocked** null check sees a non-null reference to a **half-constructed object** and uses it — reading default-valued fields, or crashing.

```
Thread B: if (instance == null)  → false (it was just assigned)
Thread B: instance.getConfig()   → returns null 💥 the constructor hadn't run
```

`volatile` inserts the memory barrier that **forbids that reordering** (and guarantees visibility).

> Before Java 5 the memory model was too weak for DCL to work at all — which is exactly why the Bill Pugh holder idiom became popular.

---

### What breaks a singleton anyway?

```
1. REFLECTION     → setAccessible(true) on the private constructor → a second instance
2. SERIALIZATION  → each deserialize creates a new one, unless you add readResolve()
3. CLONING        → override clone() to throw

Only ENUM is immune to all three ⭐
```

> 💡 **Bonus that always lands:** *"A Spring `@Service` is a singleton too, but per **ApplicationContext**, not per JVM — it's a container guarantee, not the GoF pattern. And it's only thread-safe if it's stateless: mutable instance fields on a singleton bean are shared across every concurrent request. That's the single most common thread-safety bug I look for in Spring code."*

---

# Part 14 — Classic live-coding programs

Type these from memory before the interview. They are the three that actually get asked.

### 1. Print odd and even alternately with two threads

```java
class OddEven {

    private int n = 1;
    private final int max = 10;

    public synchronized void printOdd() throws InterruptedException {
        while (n <= max) {
            while (n % 2 == 0) wait();          // not my turn → release the lock
            System.out.println("Odd : " + n++);
            notifyAll();                        // wake the other thread
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

```
n=1 → odd thread prints, n becomes 2, notifyAll
n=2 → odd thread sees n%2==0 → wait 🔓
      even thread wakes, prints, n becomes 3, notifyAll
n=3 → even waits, odd prints...      ← perfect alternation ✅
```

The key insight to explain: **the `while` is what makes it alternate.** Each thread parks itself when it isn't its turn.

---

### 2. Producer–consumer with `BlockingQueue` (the version to write FIRST)

```java
BlockingQueue<Integer> q = new ArrayBlockingQueue<>(5);

Thread producer = new Thread(() -> {
    try {
        for (int i = 1; i <= 20; i++) {
            q.put(i);                                  // BLOCKS when full ✅
            System.out.println("produced " + i);
        }
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});

Thread consumer = new Thread(() -> {
    try {
        while (true) System.out.println("consumed " + q.take());   // BLOCKS when empty ✅
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});

producer.start();
consumer.start();
```

Write this first, then say: *"and if you'd like the manual version, here it is with wait/notify"* — showing both is the strongest possible answer.

---

### 3. Run N tasks in parallel and collect results

```java
ExecutorService pool = Executors.newFixedThreadPool(4);

List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2, () -> 3);
List<Future<Integer>> futures = pool.invokeAll(tasks);      // blocks until ALL are done

int sum = 0;
for (Future<Integer> f : futures) sum += f.get();

pool.shutdown();
```

---

### 4. Thread-safe counter — three ways

See Part 3: `synchronized`, `AtomicInteger`, `LongAdder`.

---

### 5. Wait for N services to start

```java
CountDownLatch latch = new CountDownLatch(3);

for (Service s : services)
    pool.submit(() -> { s.start(); latch.countDown(); });

latch.await(30, TimeUnit.SECONDS);
System.out.println("all services up");
```

---

# Part 15 — Multithreading in Spring Boot (your real answer)

**They will ask "where have you used threads?" — do not say "I haven't".** You have, through the framework.

> *"Most of my concurrency is framework-level rather than raw threads. Every HTTP request in Spring Boot runs on its own Tomcat worker thread, so **any singleton bean with mutable state is a shared-mutable-state bug** — I keep `@Service` beans stateless and pass state as method parameters. Where I did explicit async work: `@Async` with a configured `ThreadPoolTaskExecutor` for fire-and-forget notification sending, and `CompletableFuture` to parallelise two independent downstream calls that had been sequential. And on the data side, optimistic locking with `@Version` for concurrent edits of the same record."*

```
      Tomcat thread pool (default ~200)
   ┌──────┬──────┬──────┬──────┐
   │ req1 │ req2 │ req3 │ req4 │   ← 4 threads, all calling the SAME @Service object
   └───┬──┴───┬──┴───┬──┴───┬──┘
       └──────┴──────┴──────┘
                 ↓
        @Service (ONE singleton instance)
        private int counter;        ← 💥 shared mutable state across all requests
```

```java
@Configuration
@EnableAsync
class AsyncConfig {

    @Bean("notificationExecutor")
    public Executor executor() {
        ThreadPoolTaskExecutor e = new ThreadPoolTaskExecutor();
        e.setCorePoolSize(5);
        e.setMaxPoolSize(10);
        e.setQueueCapacity(100);                 // bounded ⭐
        e.setThreadNamePrefix("notify-");
        e.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return e;
    }
}

@Service
class NotificationService {
    @Async("notificationExecutor")
    public void send(Notification n) { /* runs OFF the request thread */ }
}
```

### 🔵 In your RoboGebra code — this is the answer, verbatim

**File:** `robogebra-portal/src/main/java/com/robogebra/cms/config/AsyncConfig.java`

Your own codebase already contains a textbook thread-pool configuration. Use it instead of a generic answer:

```java
@Bean(NOTIFICATION_TASK_EXECUTOR)
public ThreadPoolTaskExecutor notificationTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(500);                     // BOUNDED ⭐
    executor.setThreadNamePrefix("push-");              // readable thread dumps ⭐
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());  // backpressure ⭐
    executor.setWaitForTasksToCompleteOnShutdown(true); // graceful shutdown ⭐
    executor.setAwaitTerminationSeconds(30);
    return executor;
}
```

> 🗣️ *"We had one shared `@Async` pool and push-notification fan-out was starving it — one announcement touches every teacher, and each device is a blocking FCM round trip, so analytics events queued behind it. I gave push its own executor with a bounded 500-item queue and `CallerRunsPolicy`, so a backlog slows the submitter instead of growing an unbounded queue until the heap gives out — a late push beats an OOM. I also had to declare Boot's own `applicationTaskExecutor` by hand, because `TaskExecutorConfiguration` is `@ConditionalOnMissingBean(Executor.class)` — declaring any executor backs the auto-configured one out, and then every unqualified `@Async` silently resolves to the notification pool."*

⭐ That last sentence is a genuinely senior observation and doubles as your Spring auto-configuration answer.

Full detail: **[39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)**.

---

### The four `@Async` traps ⭐ (excellent follow-up material)

```
1. SELF-INVOCATION doesn't work
      @Async is proxy-based, so calling this.send(...) from inside the same
      bean runs SYNCHRONOUSLY. Same rule as @Transactional.

2. Must be PUBLIC, and the return type must be void, Future or CompletableFuture

3. EXCEPTIONS VANISH on void methods
      unless you register an AsyncUncaughtExceptionHandler

4. CONTEXT DOESN'T PROPAGATE
      SecurityContextHolder, the transaction, and MDC are ThreadLocal-based,
      so the async thread sees NONE of it unless you configure propagation.
      @Transactional on an @Async method starts a DIFFERENT transaction.
```

That last one is the source of many real production bugs — worth saying unprompted.

---

> 📘 **Full Java 21 file:** virtual threads in the context of everything else 21 added (pattern matching for `switch`, record patterns, sequenced collections, structured concurrency) → **[40 — Java 21 Features](./40-java21-features.md)** ⭐

# Part 16 — Virtual threads (Java 21)

> Worth two sentences — it signals you keep current. Don't oversell it if the project is on Java 17.

```
PLATFORM thread → maps 1:1 to an OS thread → ~1MB stack → thousands max
VIRTUAL thread  → JVM-scheduled, a few hundred bytes → MILLIONS possible
                  and when it blocks on I/O it UNMOUNTS from its carrier
                  thread instead of holding it ⭐
```

```
Platform threads, 10,000 requests
   → 10,000 OS threads → ~10 GB → dead 💥

Virtual threads, 10,000 requests
   → 10,000 virtual threads on ~8 carrier threads → fine ✅
```

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (var task : tasks) executor.submit(task);
}   // close() waits for all — try-with-resources
```

**Say:** *"They make the simple blocking style scale like reactive code for I/O-bound work — a thread per request becomes viable again. They do **not** speed up CPU-bound work, and `synchronized` blocks used to pin a virtual thread to its carrier, so `ReentrantLock` was preferred there. RoboGebra is on Java 17, so this is reading rather than production experience for me."*

That last sentence is the honest framing that scores better than pretending.

---

# Part 17 — Predict the output

**Q1**
```java
Thread t = new Thread(() -> System.out.println("A"));
t.run();
System.out.println("B");
```
<details><summary>Answer</summary>

`A` then `B`, **both on the main thread**.

`run()` is a plain method call — no thread is started at all.
</details>

**Q2**
```java
Thread t = new Thread(() -> System.out.println("A"));
t.start();
t.start();
```
<details><summary>Answer</summary>

Prints `A`, then throws **`IllegalThreadStateException`** on the second `start()`.

A `Thread` object is single-use.
</details>

**Q3**
```java
class C { int n = 0; void inc() { n++; } }
// 1000 threads each call inc() once
```
<details><summary>Answer</summary>

Anything **≤ 1000**, typically slightly under.

Lost updates, because `n++` is read-modify-write. It is non-deterministic — it may print exactly 1000 on some runs, which is precisely what makes these bugs so hard to catch in testing.
</details>

**Q4**
```java
synchronized (this) {
    Thread.sleep(5000);
}
```
<details><summary>Answer</summary>

It compiles (once `InterruptedException` is handled) and **holds the lock for the full 5 seconds** — every other thread needing that monitor is blocked.

`sleep` does not release locks. This is a design smell, not an error.
</details>

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
<details><summary>Answer</summary>

Prints only `main done`.

The daemon thread is killed when main — the last user thread — exits, so `worker done` never prints. Remove `setDaemon(true)` and both print.
</details>

**Q6**
```java
private static int count = 0;
public static synchronized void a() { count++; }
public void b() { synchronized (this) { count++; } }
```
<details><summary>Answer</summary>

**Not thread-safe together.**

`a()` locks `T.class`; `b()` locks `this`. Two different monitors guarding the **same static field** → they don't exclude each other. Both must lock the same object.
</details>

**Q7**
```java
List<Integer> list = new ArrayList<>();
// 10 threads each add 100 elements
System.out.println(list.size());
```
<details><summary>Answer</summary>

Usually **< 1000**, and it can also throw `ArrayIndexOutOfBoundsException` or `NullPointerException` — a concurrent `add` can interleave with the internal array grow and corrupt it.

Use `Collections.synchronizedList`, `CopyOnWriteArrayList`, or per-thread lists merged at the end.
</details>

**Q8**
```java
CompletableFuture.supplyAsync(() -> { throw new RuntimeException("boom"); })
                 .thenApply(x -> "never")
                 .exceptionally(ex -> "recovered: " + ex.getMessage());
```
<details><summary>Answer</summary>

Result is `recovered: java.util.concurrent.CompletionException: java.lang.RuntimeException: boom`.

Two details people get wrong:
1. `thenApply` is **skipped** entirely on the exceptional path.
2. The original exception arrives **wrapped** in a `CompletionException`.

Also: nothing prints at all unless you `join()` or add a terminal stage.
</details>

---

# Part 18 — Rapid-fire table

| Question | Answer |
|---|---|
| `start()` vs `run()` | `start()` creates a thread; `run()` is a normal call on the current thread |
| Thread states | NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED |
| `sleep` vs `wait` | `sleep` keeps locks (Thread, static); `wait` releases the monitor (Object, needs synchronized) |
| `wait` in `if` or `while`? | **`while`** — spurious wakeups and stolen conditions |
| `notify` vs `notifyAll` | `notifyAll` is safe; `notify` can wake the wrong waiter and hang |
| The three thread-safety problems | atomicity, visibility, ordering |
| What `volatile` gives | visibility + ordering. **Not** atomicity |
| Does `volatile` fix `i++`? | ❌ — use `AtomicInteger` or a lock |
| What `synchronized` gives | mutual exclusion + visibility + ordering; reentrant; releases on exception |
| Instance vs static synchronized | different locks (`this` vs `Class`) — they don't exclude each other |
| CAS | compare-and-swap; a lock-free optimistic retry loop behind `Atomic*` |
| `ReentrantLock` over `synchronized` | tryLock, timeout, interruptible, fairness, multiple conditions |
| Deadlock's 4 conditions | mutual exclusion, hold-and-wait, no preemption, circular wait |
| Best deadlock prevention | global lock **ordering** ⭐ |
| Detect a deadlock | `jstack` thread dump → "Found one Java-level deadlock" |
| Livelock | threads keep changing state but make no progress |
| Why pools | thread creation cost + bounded concurrency + backpressure |
| `Executors` factory risk | unbounded queue (fixed) or unbounded threads (cached) → OOM |
| Pool sizing | CPU-bound ≈ cores+1; I/O-bound much larger |
| Pool decision order | core threads → **queue** → max threads → rejection policy |
| `Runnable` vs `Callable` | `Callable` returns a value and can throw checked exceptions |
| `Future` limitation | `get()` blocks, no chaining → use `CompletableFuture` |
| `thenApply` vs `thenCompose` | map vs flatMap |
| CHM lock granularity | CAS on an empty bin; `synchronized` on the bin's head node |
| `ThreadLocal` risk | leaks in pooled threads unless `remove()` in a `finally` |
| `@Async` traps | self-invocation, must be public, swallowed exceptions, no context propagation |
| Virtual threads | cheap JVM-scheduled threads; unmount on blocking I/O; Java 21 |
| Stop a thread properly | interrupt, or a `volatile` flag — never `stop()` |
| Safest singleton | `enum` (immune to reflection + serialization) |
| Why `volatile` in DCL | forbids reordering → no half-constructed object leaks |

---

# Part 19 — Drills

Cover the answers and say them out loud.

**1. Why is `count++` unsafe but `count = 5` (an int assignment) atomic?**
<details><summary>Answer</summary>

`count++` is read-modify-write — three steps that can interleave. An `int` assignment is a single atomic store per the JMM.

Note the exception: `long` and `double` writes are **not** guaranteed atomic on 32-bit JVMs unless declared `volatile`.
</details>

**2. Someone writes `volatile int count; count++;` and calls it thread-safe. What do you say?**
<details><summary>Answer</summary>

`volatile` fixes **visibility**, not **atomicity**. Two threads can still both read 5 and both write 6.

Use `AtomicInteger.incrementAndGet()` or synchronize.
</details>

**3. Your service singleton has `private SimpleDateFormat fmt = new SimpleDateFormat(...)`. What's the bug?**
<details><summary>Answer</summary>

`SimpleDateFormat` is mutable and not thread-safe, and a Spring singleton is shared across all request threads → garbled or wrong dates, sometimes exceptions.

Fix: use `DateTimeFormatter` (immutable, thread-safe), or a local instance, or a `ThreadLocal`.
</details>

**4. Two threads deadlock in production. Walk me through your first five minutes.**
<details><summary>Answer</summary>

1. Confirm the symptoms — requests hanging, CPU low.
2. `jstack <pid>` twice, a minute apart.
3. Look for the "Found one Java-level deadlock" block, and for threads stuck on the same monitor across **both** dumps.
4. Identify the two lock-acquisition paths.
5. Fix by enforcing a consistent lock order, or introduce `tryLock` with a timeout.
</details>

**5. When is `CopyOnWriteArrayList` the right choice, and when is it a disaster?**
<details><summary>Answer</summary>

**Right** for read-mostly, small, rarely-written lists — listener and observer registries.

**A disaster** for write-heavy or large lists: every single `add` copies the whole array (O(n)), so a loop of 10,000 adds means 10,000 array copies.
</details>

**6. You have 100 independent REST calls to make, each ~200ms. Sequential = 20s. How do you fix it, and what's the risk?**
<details><summary>Answer</summary>

Fan out with `CompletableFuture.supplyAsync` on a **dedicated bounded pool** (or a virtual-thread executor on Java 21), and combine with `allOf`.

Risks: overwhelming the downstream service (rate-limit, or gate with a `Semaphore`), exhausting the connection pool, and having no per-call timeout — add `orTimeout` and a fallback.

Never use the default common pool for blocking I/O.
</details>

**7. Why prefer `BlockingQueue` over `wait`/`notify` for producer–consumer?**
<details><summary>Answer</summary>

It is already correct: blocking `put`/`take`, a bounded capacity giving backpressure, and no chance of a missed signal or an `if` where a `while` was needed.

Less code, and no subtle bug surface.
</details>

---

## Quick Revision Sheet

```
THE FRAME
  Thread safety = ATOMICITY + VISIBILITY + ORDERING
  synchronized → all 3 | volatile → visibility+ordering only | Atomic → atomicity on 1 var

CREATING
  implements Runnable > extends Thread  (inheritance slot + pools + lambda)
  start() = NEW thread ✅  |  run() = same thread ❌
  start() twice → IllegalThreadStateException
  daemon → killed when the last user thread ends (no cleanup)

LIFECYCLE
  NEW → RUNNABLE → (BLOCKED / WAITING / TIMED_WAITING) → TERMINATED
  BLOCKED = waiting for a LOCK (forced) | WAITING = called wait()/join() (chosen)
  sleep() KEEPS the lock 🔒  |  wait() RELEASES it 🔓
  interrupt() = a polite request; ALWAYS restore the flag in the catch

RACE CONDITION
  count++ = read + add + write → lost updates
  Fix: synchronized | AtomicInteger ⭐ | LongAdder (high contention)
  volatile does NOT fix it ⚠️

LOCKS
  synchronized: instance→this, static→Class, block→your own private final object ⭐
  reentrant ✅ | releases on exception ✅
  ReentrantLock adds: tryLock(timeout) ⭐, interruptible, fairness, conditions
  ReadWriteLock: many readers, one writer

wait/notify — 3 RULES
  1. inside synchronized  2. use WHILE not IF  3. prefer notifyAll()
  Real code: use BlockingQueue instead ⭐

DEADLOCK
  4 conditions; break CIRCULAR WAIT with consistent LOCK ORDERING ⭐
  or tryLock(timeout). Detect with jstack.
  livelock = busy but no progress | starvation = never scheduled

POOLS
  new Thread() per task → OOM. Pool = reuse + bounded + backpressure
  Decision order: core → QUEUE → max → rejection policy ⭐
  Production: ThreadPoolExecutor + BOUNDED queue + CallerRunsPolicy
  ALWAYS shutdown()

ASYNC
  Runnable (void) vs Callable (value + checked exception → Future)
  CompletableFuture: supplyAsync / thenApply / thenCompose / thenCombine /
                     allOf / exceptionally — ALWAYS pass your own executor ⚠️

SINGLETON
  Eager | synchronized | DCL (volatile mandatory!) | Holder ⭐ | Enum (safest) ⭐
  volatile in DCL prevents seeing a HALF-CONSTRUCTED object

SPRING
  Every request = its own thread → @Service must be STATELESS ⭐
  @Async traps: self-invocation, must be public, swallowed exceptions,
                no SecurityContext/transaction/MDC propagation
```

---

**Related files:** [05 — Core Java](./05-java.md) · [30 — Mphasis L2 §C concurrency](./30-mphasis-level2-client-round.md) · [31 — HashMap internals](./31-hashmap-internals.md) · [23 — Predict the Output](./23-java-output-tricky-questions.md)
