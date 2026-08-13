# System Design & Production — Interview Answers (Senior Java Engineer Perspective)

---

## Q1. What is system design?

System design is the process of defining the architecture, components, data flow, and infrastructure of a software system to meet scalability, reliability, and performance requirements.

In an interview context, system design means taking a vague requirement — like "design a URL shortener" or "design a food delivery platform" — and breaking it down into concrete components: APIs, databases, caching layers, message queues, load balancers, and how they all interact.

**Key things I focus on during a system design discussion:**
- **Requirements clarification** — functional vs non-functional (latency, throughput, availability)
- **Capacity estimation** — QPS, storage, bandwidth
- **High-level architecture** — services, data stores, communication patterns
- **Deep dive** — database schema, API design, caching strategy, failure handling
- **Trade-offs** — CAP theorem, consistency vs availability, SQL vs NoSQL

**Real-world scenario:** When we designed our order management system, we started with a monolith. As traffic grew to 10K orders/min, we broke it into microservices — Order Service, Payment Service, Inventory Service — with Kafka for async communication, Redis for caching, and PostgreSQL for persistence.

**Common mistake:** Jumping straight into implementation details without clarifying requirements. Always ask: "What are the expected read/write ratios? What's the target latency? What's the expected scale?"

System design is about making the right trade-offs for your specific constraints — there's no single right answer.

---

## Q2. Cache vs DB?

Cache is for speed — it stores frequently accessed data in-memory for sub-millisecond reads. Database is for durability — it persists data reliably to disk with ACID guarantees.

**Cache (Redis, Caffeine, Memcached):**
- In-memory, blazing fast (~0.1ms reads)
- Volatile — data can be lost on restart
- Limited capacity (bounded by RAM)
- Used for: session data, hot query results, rate limit counters, leaderboards

**Database (PostgreSQL, MySQL, MongoDB):**
- Disk-based, durable
- Slower (~1-10ms reads, depends on indexing)
- Virtually unlimited capacity
- Source of truth for all data

**Real-world scenario:** In our e-commerce platform, product catalog reads hit Redis first (cache-aside pattern). If cache misses, we read from PostgreSQL and populate the cache with a 5-minute TTL. This reduced our average API latency from 45ms to 3ms and cut database load by 80%.

**Common patterns:**
- **Cache-aside** — app checks cache first, falls back to DB, writes to cache
- **Write-through** — writes go to cache and DB simultaneously
- **Write-behind** — writes go to cache first, async flush to DB

**Common mistake:** Treating cache as the source of truth. If your app breaks when the cache is cleared, you have a design problem. Also, not setting TTL — stale cache is one of the hardest bugs to debug.

Use cache to accelerate reads, but always design your system to function (maybe slower) without it.

---

## Q3. How do you detect memory leaks?

Memory leak detection starts with monitoring heap usage trends and then drilling down with heap dumps and profiling tools — if your heap usage keeps climbing over time without dropping after GC, you likely have a leak.

**Step-by-step approach I follow:**

1. **Monitor** — Watch GC metrics and heap usage via Grafana/Prometheus. A healthy app shows a sawtooth pattern (heap grows, GC drops it). A leak shows a steadily rising baseline.

2. **Heap dump** — Capture with:
   ```bash
   jmap -dump:live,format=b,file=heapdump.hprof <pid>
   # Or JVM flag for auto-dump on OOM:
   -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/
   ```

3. **Analyze** — Open in Eclipse MAT (Memory Analyzer Tool) or VisualVM:
   - Look at the **Dominator Tree** — shows which objects retain the most memory
   - Check **Leak Suspects Report** — MAT auto-identifies likely leaks
   - Examine **GC Roots** — trace why objects aren't being collected

4. **Common leak sources in Java:**
   - Static collections that keep growing (`static Map`, `static List`)
   - Unclosed resources (DB connections, streams, HTTP clients)
   - Listeners/callbacks registered but never removed
   - ThreadLocal variables not cleaned up
   - Large session caches without eviction

**Real-world scenario:** We had an OOM in production every 3 days. Heap dump analysis showed millions of `AuditLog` objects held by a static `ArrayList` in a utility class. The developer was logging audit entries in-memory "temporarily" but never flushing them. Fix: switched to writing directly to a Kafka topic.

**Common mistake:** Restarting the service as a "fix" — that's masking the problem. Also, using `-Xmx` increase as a solution — you're just buying time before the next OOM.

Memory leaks are inevitable in long-running Java apps — the skill is in detecting and fixing them quickly.

---

## Q4. What is rate limiting?

Rate limiting is a technique to control the number of requests a client can make to your API within a given time window — protecting your system from abuse, DDoS attacks, and resource exhaustion.

In practice, you define rules like: "Each API key can make 100 requests per minute" or "Each IP can make 10 login attempts per hour."

**Where to implement it:**
- **API Gateway level** (Kong, AWS API Gateway) — most common, catches traffic at the edge
- **Application level** (Spring filter, interceptor) — finer-grained control per endpoint
- **Load balancer level** (Nginx `limit_req`) — simple but coarse

**Real-world scenario:** In our payment API, we rate-limit each merchant to 500 requests/second. Without this, one merchant's batch processing spike could starve other merchants of resources. We implemented it using Redis with a sliding window counter:
```java
String key = "rate:" + merchantId + ":" + currentWindow;
long count = redis.incr(key);
redis.expire(key, 60);
if (count > 500) throw new RateLimitExceededException();
```

**What to return when rate limited:**
- HTTP `429 Too Many Requests`
- Include `Retry-After` header
- Include `X-RateLimit-Remaining` and `X-RateLimit-Limit` headers

**Common mistake:** Only rate-limiting by IP — this breaks for users behind NAT/corporate proxies where thousands of users share one IP. Use API keys or user IDs for authenticated endpoints.

Rate limiting is a must-have for any public-facing API — without it, one bad actor can take down your entire system.

---

## Q5. What are the common rate limiting algorithms?

The two most widely used algorithms are Token Bucket and Sliding Window, each with different trade-offs for burst handling and precision.

**1. Token Bucket:**
- A bucket holds N tokens, refilled at a fixed rate
- Each request consumes one token; if bucket is empty, request is rejected
- **Allows short bursts** — if bucket is full, a burst of N requests goes through instantly
- Used by: AWS API Gateway, Stripe, Google Cloud
```
Bucket capacity: 10 tokens
Refill rate: 2 tokens/second
→ Allows burst of 10, then sustained 2 req/sec
```

**2. Sliding Window Log:**
- Keeps a log of each request timestamp
- Counts requests within the past window (e.g., last 60 seconds)
- **Most accurate** — no boundary issues
- **Memory-heavy** — stores every request timestamp
- Better for strict rate limits (e.g., financial APIs)

**3. Sliding Window Counter (Hybrid):**
- Splits time into fixed windows, uses weighted average at boundaries
- Good balance of accuracy and memory efficiency
- Most practical for production use

**4. Fixed Window Counter:**
- Counts requests in fixed time windows (e.g., 0:00–1:00, 1:00–2:00)
- Simple but has **boundary spike problem** — 100 requests at 0:59 + 100 at 1:00 = 200 in 2 seconds
- Fine for non-critical use cases

**Real-world scenario:** In our API gateway, we use Token Bucket for public endpoints (allows legitimate traffic bursts) and Sliding Window for payment APIs (strict, precise limits). Implementation uses Redis with Lua scripts for atomicity.

**Common mistake:** Using Fixed Window without understanding the boundary problem — you can get 2x your limit in a 1-second span at window boundaries.

Token Bucket is the default choice for most systems — it handles real-world traffic patterns (bursty) better than strict counting approaches.

---

## Q6. What is optimistic locking?

Optimistic locking is a concurrency control strategy where you allow multiple transactions to read and work with data simultaneously, but check for conflicts at write time using a version number — if someone else modified the data first, your update is rejected.

The assumption is "conflicts are rare" — so don't lock upfront, just detect conflicts when they happen.

**How it works in practice (JPA/Hibernate):**
```java
@Entity
public class Product {
    @Id
    private Long id;
    private int stock;

    @Version
    private int version;  // Hibernate manages this automatically
}
```

When you update, Hibernate generates:
```sql
UPDATE product SET stock = 9, version = 2
WHERE id = 1 AND version = 1;
```
If another transaction already incremented the version to 2, this update affects 0 rows → Hibernate throws `OptimisticLockException`.

**When to use optimistic locking:**
- Read-heavy workloads where conflicts are rare
- Shopping carts, profile updates, content management
- When you can't afford to hold DB locks (high throughput systems)

**Real-world scenario:** In our CMS, multiple editors can open the same article. When they save, the version check prevents silent overwrites. If a conflict is detected, we show a merge dialog — similar to how Google Docs or Git handles conflicts.

**Common mistake:** Not handling `OptimisticLockException` — your code must catch it and retry or notify the user. Also, using optimistic locking for high-contention scenarios (like inventory during flash sales) — it leads to excessive retries and poor UX.

Optimistic locking is ideal when conflicts are the exception, not the rule — it gives you concurrency without the performance cost of locks.

---

## Q7. What is pessimistic locking?

Pessimistic locking is a concurrency control strategy where you lock the data before reading it, preventing anyone else from modifying it until you're done — it assumes conflicts will happen and blocks them upfront.

**How it works in JPA:**
```java
@Query("SELECT p FROM Product p WHERE p.id = :id")
@Lock(LockModeType.PESSIMISTIC_WRITE)
Product findByIdForUpdate(@Param("id") Long id);
```

This generates:
```sql
SELECT * FROM product WHERE id = 1 FOR UPDATE;
```
The row is locked at the database level — other transactions trying to read/write this row will wait until the lock is released (when the transaction commits or rolls back).

**When to use pessimistic locking:**
- High-contention scenarios where conflicts are frequent
- Financial transactions (balance deductions, transfers)
- Inventory management during flash sales
- Any operation where retrying on conflict is unacceptable

**Real-world scenario:** In our wallet service, when deducting a user's balance, we use `SELECT FOR UPDATE` to lock the row. This guarantees that two concurrent deductions can't both read balance=100 and each deduct 80, leaving -60. One waits, reads the updated balance, and fails gracefully if insufficient.

**Common mistake:**
- Holding locks too long — do minimal work inside the transaction. No API calls, no heavy computations while holding a lock
- **Deadlocks** — if Transaction A locks Row 1 then Row 2, and Transaction B locks Row 2 then Row 1, both wait forever. Always lock rows in a consistent order
- Using pessimistic locking for read-heavy, low-conflict scenarios — it kills throughput

Pessimistic locking is the right choice when data integrity is non-negotiable and conflicts are expected — just keep your critical sections short.

---

## Q8. What is a race condition?

A race condition occurs when two or more threads access shared data concurrently, and the final outcome depends on the unpredictable timing of thread execution — leading to data corruption or incorrect behavior.

**Classic example — inventory problem:**
```
Thread A: reads stock = 10
Thread B: reads stock = 10
Thread A: sets stock = 10 - 1 = 9  (writes 9)
Thread B: sets stock = 10 - 1 = 9  (writes 9)
// Two items sold, but stock only decreased by 1!
```

**Common race conditions in Java backend systems:**
- **Check-then-act:** `if (map.containsKey(key)) { map.get(key).update() }` — another thread may remove the key between check and act
- **Read-modify-write:** `counter++` is NOT atomic — it's read, increment, write (three operations)
- **Lazy initialization:** `if (instance == null) instance = new Singleton()` — two threads can both pass the null check

**Solutions I've used:**
- **synchronized blocks** — for in-process thread safety
- **AtomicInteger / ConcurrentHashMap** — lock-free thread-safe structures
- **Database locks** — for cross-instance race conditions
- **Distributed locks (Redis/Redisson)** — for microservice-level race conditions
- **Idempotency keys** — prevent duplicate processing

**Real-world scenario:** We had a bug where two API requests to "apply coupon" for the same order ran concurrently. Both checked "coupon not applied" and both applied it — giving the user a double discount. Fix: we used a Redis distributed lock with the key `lock:coupon:{orderId}`.

**Common mistake:** Thinking race conditions only happen in multi-threaded code. In microservices, two instances of the same service processing the same event is a race condition — and `synchronized` won't help because it's cross-process.

Race conditions are some of the hardest bugs to reproduce — think about concurrency during design, not after bugs appear.

---

## Q9. How do you handle booking system concurrency issues?

The core challenge in a booking system is preventing double-booking — two users shouldn't be able to book the same slot/seat/room at the same time. The solution depends on your scale and architecture.

**Approach 1: Database-level lock (single instance)**
```sql
BEGIN;
SELECT * FROM slots WHERE slot_id = 101 AND status = 'AVAILABLE' FOR UPDATE;
UPDATE slots SET status = 'BOOKED', user_id = 42 WHERE slot_id = 101;
COMMIT;
```
`FOR UPDATE` locks the row — any concurrent transaction waits.

**Approach 2: Unique constraint (simple & elegant)**
```sql
ALTER TABLE bookings ADD CONSTRAINT unique_slot UNIQUE (slot_id, booking_date);
```
Concurrent inserts for the same slot — one succeeds, the other gets a constraint violation. Handle the exception gracefully.

**Approach 3: Distributed lock (microservices)**
```java
RLock lock = redisson.getLock("booking:" + slotId);
try {
    lock.lock(10, TimeUnit.SECONDS);
    // Check availability and book
} finally {
    lock.unlock();
}
```

**Approach 4: Optimistic locking with retry (low contention)**
```java
@Version
private int version;
// Update with version check — retry on OptimisticLockException
```

**Real-world scenario:** In our hotel booking system, we use a combination: unique constraint on `(room_id, check_in_date)` as the safety net, plus Redis distributed lock for the booking flow to give users a clean UX instead of a raw constraint violation error. During flash sales (high contention), we use a queue-based approach — requests go into a Kafka queue and are processed sequentially per slot.

**Common mistake:** Relying only on application-level checks (`if available then book`) without database-level enforcement. This always breaks under concurrent load.

In booking systems, the database is your last line of defense — always have a constraint or lock at the data layer.

---

## Q10. How do you handle double booking?

Double booking occurs when two transactions simultaneously book the same resource because they both read "available" before either writes "booked." The solution is atomic transactions — making the check-and-book operation indivisible.

**Strategy 1: Atomic SQL (preferred)**
```sql
UPDATE slots
SET status = 'BOOKED', user_id = :userId
WHERE slot_id = :slotId AND status = 'AVAILABLE';
-- Check affected rows: 1 = success, 0 = already booked
```
This is a single atomic statement — no gap between check and update.

**Strategy 2: Unique index as guard rail**
```sql
CREATE UNIQUE INDEX idx_booking ON bookings(resource_id, time_slot);
```
Even if your application has a bug, the database prevents duplicates.

**Strategy 3: Idempotency key**
```java
@Transactional
public Booking createBooking(String idempotencyKey, BookingRequest req) {
    // Check if booking with this key already exists
    Optional<Booking> existing = repo.findByIdempotencyKey(idempotencyKey);
    if (existing.isPresent()) return existing.get(); // Return same result
    // Proceed with booking...
}
```

**Real-world scenario:** In our appointment scheduling system, a user double-clicked the "Book" button, firing two API requests simultaneously. Without idempotency, both requests succeeded. Fix: we added an idempotency key (UUID generated client-side) and a unique constraint on `(doctor_id, appointment_time)`. Now: the first request books successfully, the second returns the same booking or a conflict error.

**Defense in depth approach:**
1. Client-side: disable button after click
2. API level: idempotency key check
3. Application level: transaction with lock
4. Database level: unique constraint

**Common mistake:** Only implementing client-side prevention (disable button). Users can bypass the UI, use curl, or network retries can duplicate requests. Always enforce at the server and database level.

Prevention is multi-layered — client, API, application, and database — never rely on a single layer.

---

## Q11. How do you size a thread pool?

Thread pool sizing depends on the nature of your tasks (CPU-bound vs I/O-bound) and your hardware — there's no universal magic number.

**Formula-based approach:**

**CPU-bound tasks** (computation, data processing):
```
Threads = Number of CPU cores + 1
```
More threads than cores just adds context-switching overhead.

**I/O-bound tasks** (DB calls, HTTP calls, file I/O):
```
Threads = CPU cores × (1 + Wait Time / Service Time)
```
Example: 8 cores, task spends 200ms waiting for DB and 50ms computing:
```
Threads = 8 × (1 + 200/50) = 8 × 5 = 40 threads
```

**Real-world scenario:** In our order processing service:
- API handler thread pool: 200 threads (I/O-heavy — calls to payment, inventory, notification services)
- Report generation pool: 4 threads (CPU-heavy — aggregation and PDF generation)
- Async email pool: 20 threads (I/O-bound, moderate concurrency)

**Spring Boot configuration:**
```yaml
server:
  tomcat:
    threads:
      max: 200      # Max worker threads
      min-spare: 20 # Min idle threads
```

**For custom pools:**
```java
@Bean
public ExecutorService orderProcessingPool() {
    return new ThreadPoolExecutor(
        10,   // core pool size
        50,   // max pool size
        60L, TimeUnit.SECONDS,  // idle thread timeout
        new LinkedBlockingQueue<>(1000),  // queue capacity
        new ThreadPoolExecutor.CallerRunsPolicy()  // rejection policy
    );
}
```

**Common mistakes:**
- Setting max threads too high — leads to excessive memory usage (each thread ~1MB stack) and context switching
- Using unbounded queues (`new LinkedBlockingQueue<>()`) — hides backpressure, eventually causes OOM
- Same pool for everything — CPU tasks and I/O tasks should use separate pools to avoid I/O tasks starving CPU tasks

**Pro tip:** Always load test your thread pool settings under realistic conditions. Formulas give you a starting point, but production traffic is the real test.

Thread pool sizing is an art informed by science — start with the formula, tune with real metrics.

---

## Q12. How do you debug a latency spike?

When a latency spike hits production, I follow a systematic approach: identify the scope, correlate with metrics, trace the slow path, and fix the root cause.

**My first 10 minutes:**

1. **Scope it** — Is it all endpoints or specific ones? All users or specific regions? Started when?
   ```bash
   # Check recent error rates and latency percentiles
   curl localhost:8080/actuator/metrics/http.server.requests
   ```

2. **Check the usual suspects:**
   - **Garbage Collection** — GC pauses cause sudden spikes
     ```bash
     jstat -gcutil <pid> 1000  # GC stats every second
     ```
   - **Database** — slow queries, connection pool exhaustion
     ```sql
     SELECT * FROM pg_stat_activity WHERE state = 'active' ORDER BY duration DESC;
     ```
   - **External service** — downstream service degradation
   - **Thread pool saturation** — all threads busy, requests queuing
     ```bash
     jstack <pid> | grep -c "RUNNABLE"
     ```

3. **Distributed tracing** — If you have Zipkin/Jaeger, find the slow traces and identify which service/span is the bottleneck.

4. **Correlate with deployments** — "Did we deploy anything in the last hour?" Latency spikes right after a deployment → likely a code change.

**Real-world scenario:** We had intermittent 5-second latency spikes every few minutes. Metrics showed normal DB query times but elevated GC pause times. Root cause: a daily report job was loading 500K records into memory for aggregation, triggering Full GC pauses that froze the entire JVM. Fix: moved the report to a separate service with its own JVM.

**Common mistake:** Assuming the database is always the problem. GC, DNS resolution, connection pool exhaustion, thread contention, and even Linux TCP settings can cause latency spikes.

**Key tools:** Grafana dashboards, distributed tracing (Jaeger), `jstack` for thread dumps, GC logs, APM tools (New Relic, Datadog).

Latency debugging is about data, not guessing — instrument everything, and the metrics will tell you where to look.

---

## Q13. CPU bottleneck vs memory bottleneck — how to differentiate?

CPU bottleneck means your threads are busy computing, while memory bottleneck means your JVM is spending too much time in garbage collection or running out of heap. The diagnostic tools are different: thread dump for CPU, heap dump for memory.

**CPU Bottleneck:**
- **Symptoms:** CPU usage consistently near 100%, high load average, slow response across all endpoints
- **Diagnosis:** Thread dump (`jstack`)
  ```bash
  jstack <pid> > threaddump.txt
  # Look for threads in RUNNABLE state doing computation
  ```
  - Multiple thread dumps 5 seconds apart — threads stuck in the same method = hot spot
  - `top -H -p <pid>` — shows CPU usage per thread, match with thread dump
- **Common causes:** Infinite loops, inefficient algorithms (O(n^2) on large datasets), regex backtracking, unintended full-table scans processed in Java
- **Fix:** Optimize the hot code path, add caching, offload to async processing

**Memory Bottleneck:**
- **Symptoms:** GC pauses increasing, heap usage climbing, eventual OOM, CPU spikes from GC activity
- **Diagnosis:** Heap dump + GC logs
  ```bash
  jmap -dump:live,format=b,file=heap.hprof <pid>
  # Enable GC logging:
  -Xlog:gc*:file=gc.log:time,uptime,level
  ```
  - Eclipse MAT → Leak Suspects Report, Dominator Tree
  - GC log analysis → check frequency and duration of Full GC
- **Common causes:** Memory leaks (static collections, unclosed resources), oversized caches, loading too much data from DB, large session objects
- **Fix:** Fix the leak, set cache limits, use pagination for large queries, tune `-Xmx/-Xms`

**Real-world scenario:** Our service had high CPU. Thread dump showed 80 threads stuck in `Pattern.compile()` inside a loop — regex was being compiled on every request instead of being cached as a static constant. One-line fix (precompile the pattern) dropped CPU from 95% to 20%.

**Common mistake:** Confusing GC-induced CPU spikes with actual CPU bottlenecks. If `top` shows high CPU but `jstack` shows threads in GC-related states, it's a memory problem manifesting as CPU usage.

Thread dump = CPU story. Heap dump = memory story. Always pick the right tool for the right symptom.

---

## Q14. What are distributed transactions?

A distributed transaction spans multiple services or databases, where either all operations succeed or all are rolled back — maintaining data consistency across service boundaries. The modern approach is the Saga pattern.

**Why traditional 2PC (Two-Phase Commit) doesn't work well in microservices:**
- Requires a central coordinator — single point of failure
- Holds locks across services — kills throughput
- Tight coupling between services
- Doesn't work across different database technologies

**Saga Pattern — the preferred approach:**

A saga is a sequence of local transactions, where each service completes its own transaction and publishes an event. If any step fails, compensating transactions undo the previous steps.

**Two types:**

**1. Choreography-based (event-driven):**
```
Order Service → creates order → publishes OrderCreated
Payment Service → listens → charges payment → publishes PaymentCompleted
Inventory Service → listens → reserves stock → publishes StockReserved
Notification Service → listens → sends confirmation
```
If payment fails → publishes PaymentFailed → Order Service listens → cancels order.

**2. Orchestration-based (central coordinator):**
```java
public class OrderSagaOrchestrator {
    public void execute(OrderRequest request) {
        createOrder(request);         // Step 1
        chargePayment(request);       // Step 2 — if fails → cancelOrder()
        reserveInventory(request);    // Step 3 — if fails → refundPayment(), cancelOrder()
        sendConfirmation(request);    // Step 4
    }
}
```

**Real-world scenario:** In our e-commerce platform, placing an order involves: Order Service (create order), Payment Service (charge card), Inventory Service (reserve stock), Shipping Service (create shipment). We use orchestration-based saga with a state machine. If payment fails, compensating actions run: cancel order, release reserved stock. All steps are idempotent to handle retries safely.

**Common mistake:** Trying to use `@Transactional` across microservice calls — it doesn't span HTTP/gRPC boundaries. Also, not making compensating actions idempotent — if a compensation retry fires twice, it shouldn't cause issues.

Sagas trade strong consistency for availability and scalability — embrace eventual consistency with proper compensation logic.

---

## Q15. What is eventual consistency?

Eventual consistency means that after a write, all replicas/services will eventually converge to the same state — but reads immediately after the write might return stale data. It's a trade-off: you sacrifice immediate consistency for higher availability and performance.

**Strong consistency:** After a write, every subsequent read sees the new value. (Single DB, synchronous replication)

**Eventual consistency:** After a write, reads might return old data for a short period, but eventually all nodes agree. (Distributed systems, async replication)

**Where I've used it:**
- **Database replicas:** Write to master, read from replica. Replica might be milliseconds behind.
- **Microservice communication:** Order Service creates an order, Notification Service gets the event 200ms later via Kafka.
- **Cache invalidation:** DB updated, cache TTL hasn't expired yet — stale read for a few seconds.
- **Search index:** Product updated in DB, Elasticsearch index updated async — search shows old data briefly.

**Real-world scenario:** In our social media feed service, when a user posts content, the post is immediately visible to the author (read-your-own-writes). But followers see the post 1-2 seconds later because the feed fanout happens asynchronously via Kafka. This is acceptable — users don't notice a 2-second delay in their feed.

**Techniques to manage eventual consistency:**
- **Read-your-own-writes:** Route the writing user's reads to the master for N seconds
- **Causal consistency:** Ensure dependent events are processed in order
- **Versioning:** Clients include a version token; if the replica is behind, redirect to master

**Common mistake:** Assuming all operations need strong consistency. Most user-facing reads are fine with 1-2 seconds of staleness. Only financial transactions (balance, payments) truly need strong consistency.

Eventual consistency is not a bug — it's a deliberate design decision that enables systems to scale horizontally.

---

## Q16. What is an API Gateway?

An API Gateway is a single entry point for all client requests that routes traffic to the appropriate backend microservice while handling cross-cutting concerns like authentication, rate limiting, and load balancing.

Instead of clients calling 10 different microservices directly, they call one gateway which fans out to the right service.

**What an API Gateway handles:**
- **Request routing** — `/api/orders/*` → Order Service, `/api/users/*` → User Service
- **Authentication/Authorization** — validate JWT tokens before forwarding
- **Rate limiting** — per client, per endpoint throttling
- **Load balancing** — distribute across service instances
- **Request/Response transformation** — aggregate responses from multiple services
- **SSL termination** — handle HTTPS at the gateway, HTTP internally
- **Logging & monitoring** — centralized access logs, request tracing

**Popular implementations:**
- **Spring Cloud Gateway** — reactive, Java-native, Spring ecosystem integration
- **Kong** — Lua/Nginx-based, plugin ecosystem, open source
- **AWS API Gateway** — managed, serverless-friendly
- **Nginx/Envoy** — lower-level, high performance

**Real-world scenario:** In our microservices platform, Spring Cloud Gateway handles all external traffic. It validates JWT tokens, applies per-tenant rate limits, routes requests to the correct service via service discovery (Eureka), and attaches a correlation ID for distributed tracing. Internal service-to-service calls bypass the gateway.

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1
            - AddRequestHeader=X-Request-Source, gateway
```

**Common mistake:** Making the gateway do too much business logic — it should be thin. Also, the gateway becomes a single point of failure, so it must be highly available (multiple instances behind a load balancer).

An API Gateway is the front door of your microservices architecture — keep it thin, fast, and resilient.

---

## Q17. What is the circuit breaker pattern?

The circuit breaker pattern prevents cascading failures in distributed systems by stopping calls to a failing downstream service and returning a fallback response instead — giving the failing service time to recover.

It works like an electrical circuit breaker with three states:

**CLOSED** (normal) → requests flow through normally. Failures are counted.
**OPEN** (tripped) → when failure threshold is reached, all requests are immediately rejected/returned with fallback. No calls to the failing service.
**HALF-OPEN** (testing) → after a timeout, a few test requests are allowed through. If they succeed → back to CLOSED. If they fail → back to OPEN.

**Implementation with Resilience4j (modern Spring Boot):**
```java
@CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
public PaymentResponse processPayment(PaymentRequest request) {
    return paymentClient.charge(request);  // External call
}

public PaymentResponse paymentFallback(PaymentRequest request, Throwable t) {
    // Queue for retry, return pending status
    retryQueue.add(request);
    return PaymentResponse.pending("Payment queued for processing");
}
```

```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentService:
        failure-rate-threshold: 50         # Open when 50% fail
        wait-duration-in-open-state: 30s   # Stay open for 30s
        sliding-window-size: 10            # Evaluate last 10 calls
        permitted-number-of-calls-in-half-open-state: 3
```

**Real-world scenario:** Our Order Service calls the Inventory Service. When Inventory goes down, without a circuit breaker, Order Service threads pile up waiting for timeouts (30 seconds each), exhausting the thread pool, and the Order Service itself goes down — cascading failure. With a circuit breaker, after 5 failures, the circuit opens, Order Service immediately returns "Inventory check pending, order will be confirmed shortly," and our system stays alive.

**Common mistake:** Not defining meaningful fallbacks — throwing an exception as a "fallback" defeats the purpose. Also, setting thresholds too aggressively (open after 2 failures) causes flapping. Tune based on actual failure patterns.

Circuit breaker is the seatbelt of microservices — you hope you don't need it, but when you do, it saves everything.

---

## Q18. Retry vs fallback?

Retry re-attempts the same operation hoping the failure is transient, while fallback provides an alternative path when the operation is expected to keep failing. They complement each other and are often used together.

**Retry:**
- Repeats the same call after a delay
- Best for transient failures: network blips, temporary DB connection issues, 503s
- Must be combined with: exponential backoff, max attempts, jitter
```java
@Retry(name = "paymentService", fallbackMethod = "paymentFallback")
public PaymentResponse charge(PaymentRequest req) {
    return paymentClient.charge(req);  // Retries up to 3 times
}
```
```yaml
resilience4j:
  retry:
    instances:
      paymentService:
        max-attempts: 3
        wait-duration: 500ms
        exponential-backoff-multiplier: 2  # 500ms, 1s, 2s
        retry-exceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
```

**Fallback:**
- Provides an alternative response when the primary path fails
- Best for: degraded but functional user experience
- Examples: return cached data, use a secondary service, queue for later processing

```java
public PaymentResponse paymentFallback(PaymentRequest req, Throwable t) {
    log.warn("Payment service unavailable, queueing for retry: {}", t.getMessage());
    asyncRetryQueue.enqueue(req);
    return PaymentResponse.pending();
}
```

**Used together (typical pattern):**
```
Request → Retry (3 attempts with backoff) → Still failing? → Fallback
```

**Real-world scenario:** In our notification service, sending an SMS via primary provider (Twilio) uses retry with backoff (handles transient API errors). If all retries fail, the fallback switches to a secondary provider (AWS SNS). If that also fails, the final fallback queues the message for batch retry later.

**Common mistakes:**
- Retrying non-idempotent operations — retrying a payment charge without an idempotency key can double-charge
- Retrying on 400 Bad Request — client errors won't succeed on retry, only retry server errors (5xx) and timeouts
- Retry without backoff — hammering a struggling service makes it worse
- No maximum retries — infinite retry = infinite resource consumption

Retry for transient failures, fallback for persistent ones — always ask "is this failure temporary or systemic?"

---

## Q19. What are common caching pitfalls?

Caching is powerful but introduces complexity — stale data, cache stampedes, and inconsistency are the most common problems I've dealt with in production.

**1. Stale data (most common):**
- Cache holds old data after the database is updated
- Fix: Set appropriate TTL, use cache-aside with invalidation
```java
public void updateProduct(Product product) {
    productRepo.save(product);
    cache.evict("product:" + product.getId());  // Invalidate immediately
}
```

**2. Cache stampede (thundering herd):**
- When a popular cache key expires, hundreds of requests simultaneously hit the database
- Fix: Lock-based rebuild — only one request rebuilds the cache, others wait
```java
public Product getProduct(Long id) {
    Product p = cache.get("product:" + id);
    if (p == null) {
        Lock lock = lockProvider.getLock("rebuild:product:" + id);
        if (lock.tryLock()) {
            try {
                p = productRepo.findById(id);
                cache.put("product:" + id, p, Duration.ofMinutes(5));
            } finally {
                lock.unlock();
            }
        } else {
            // Wait briefly and retry from cache
        }
    }
    return p;
}
```

**3. Cache penetration:**
- Querying for data that doesn't exist in cache OR database — every request hits the DB
- Fix: Cache null results with short TTL, or use a Bloom filter

**4. Cache-database inconsistency:**
- Update DB then cache eviction fails — cache has old data
- Fix: Use cache-aside (always write to DB, let reads populate cache), or event-driven invalidation via CDC (Change Data Capture)

**5. Over-caching:**
- Caching everything, including rarely accessed data — wastes memory
- Fix: Only cache hot data, monitor cache hit rates

**Real-world scenario:** During a product launch, our most popular product page cache key expired. 10,000 concurrent requests hit PostgreSQL simultaneously — database CPU spiked to 100% and queries timed out. Fix: implemented cache stampede protection with a distributed lock and "stale-while-revalidate" pattern — serve slightly stale data while one thread rebuilds the cache.

**Common mistake:** Not monitoring cache hit ratios. If your hit rate is below 80%, you're paying the memory cost of caching without the performance benefit.

Caching is a trade-off between speed and freshness — understand the pitfalls before adding a cache layer.

---

## Q20. What is LRU eviction?

LRU (Least Recently Used) is a cache eviction policy that removes the item that hasn't been accessed for the longest time when the cache reaches its capacity limit.

**How it works:**
- Every time an item is accessed (read or write), it moves to the "most recently used" position
- When the cache is full and a new item needs to be inserted, the item at the "least recently used" position is evicted
- Implemented internally using a HashMap + Doubly Linked List for O(1) get/put operations

**Example:**
```
Cache capacity: 3
PUT A → [A]
PUT B → [B, A]
PUT C → [C, B, A]
GET A → [A, C, B]    ← A moves to front (most recent)
PUT D → [D, A, C]    ← B evicted (least recently used)
```

**In Java / Spring Boot:**

Using Caffeine (recommended for local cache):
```java
@Bean
public CacheManager cacheManager() {
    CaffeineCacheManager manager = new CaffeineCacheManager("products");
    manager.setCaffeine(Caffeine.newBuilder()
        .maximumSize(10_000)       // LRU eviction after 10K entries
        .expireAfterWrite(Duration.ofMinutes(10))
        .recordStats());           // Enable hit/miss metrics
    return manager;
}
```

Redis uses an approximated LRU (`maxmemory-policy allkeys-lru`).

**Other eviction policies for comparison:**
- **LFU** (Least Frequently Used) — evicts items with fewest accesses. Better for workloads with consistent hot items.
- **FIFO** (First In First Out) — evicts oldest item regardless of access. Simple but less effective.
- **TTL-based** — evicts items after a time-to-live expires. Good for freshness guarantees.

**Real-world scenario:** In our product catalog cache (Caffeine, 50K entries), LRU naturally keeps trending/popular products in cache and evicts rarely viewed products. During Black Friday, the hot items stay cached while long-tail products get evicted — exactly the behavior we want.

**Common mistake:** Using LRU when your access pattern has periodic bursts (e.g., a report that scans all items once a day). The scan pollutes the cache and evicts actually-hot items. LFU or segmented LRU is better in that case.

LRU is the default eviction strategy for good reason — it works well for most access patterns where recent access predicts future access.

---

## Q21. What are Redis use cases?

Redis is an in-memory data structure store that I use as a distributed cache, session store, rate limiter, message broker, and more — it's the Swiss Army knife of backend systems.

**Primary use cases I've implemented:**

**1. Distributed caching:**
```java
@Cacheable(value = "products", key = "#id")
public Product getProduct(Long id) {
    return productRepo.findById(id).orElseThrow();
}
```
Sub-millisecond reads, shared across all service instances.

**2. Session management:**
```yaml
spring:
  session:
    store-type: redis
```
Sticky sessions aren't needed — any instance can serve any user.

**3. Rate limiting:**
```java
String key = "ratelimit:" + clientId + ":" + currentMinute;
Long count = redis.opsForValue().increment(key);
redis.expire(key, 60, TimeUnit.SECONDS);
if (count > 100) throw new RateLimitExceededException();
```

**4. Distributed locks (Redisson):**
```java
RLock lock = redisson.getLock("order-processing:" + orderId);
lock.lock(10, TimeUnit.SECONDS);
```

**5. Pub/Sub and event notifications:**
Real-time notifications, chat systems, live dashboards.

**6. Leaderboards/Ranking (Sorted Sets):**
```java
redis.opsForZSet().add("leaderboard", "player1", 1500.0);
redis.opsForZSet().reverseRangeWithScores("leaderboard", 0, 9); // Top 10
```

**7. Queue (List as queue):**
Simple task queues with `LPUSH` / `BRPOP`.

**Real-world scenario:** In our platform, Redis handles:
- Product cache (reduces DB load by 85%)
- User sessions (enables zero-downtime deployments)
- Rate limiting per API key (protects against abuse)
- Distributed locks for idempotent payment processing
All from a single Redis Cluster — ~0.5ms average latency.

**Common mistake:** Using Redis as the primary database — it's in-memory and not designed for data durability as a primary store. Also, not setting `maxmemory` and eviction policies — Redis will consume all available RAM and get killed by the OS.

Redis is in every production stack I've worked with — learn it well, it solves a surprising number of distributed systems problems.

---

## Q22. What is the impact of GC on performance?

Garbage Collection pauses can directly impact application latency — when GC runs, application threads are paused (stop-the-world), and no requests are processed during that time. Large heaps make GC pauses longer and less predictable.

**How GC affects your application:**
- **Minor GC (Young Gen):** Fast (5-50ms), frequent, usually not noticeable
- **Major/Full GC (Old Gen):** Slow (100ms to several seconds), pauses entire application
- **G1GC mixed collections:** More predictable but still cause pauses

**Real-world impact:**
```
Normal request latency: 10ms
During Full GC pause: request waits 2 seconds → timeout → user sees error
```

**GC tuning I've done in production:**

```bash
# Use G1GC (default in Java 11+) with target pause time
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200        # Target max pause
-XX:G1HeapRegionSize=16m        # Region size for large heaps

# For ultra-low latency (Java 17+):
-XX:+UseZGC                      # Sub-millisecond pauses
# Or
-XX:+UseShenandoahGC             # Another low-pause collector
```

**Key metrics to monitor:**
- GC pause time and frequency
- Heap usage after Full GC (should drop significantly — if not, memory leak)
- GC throughput (% of time spent in app code vs GC — aim for >95%)

**Real-world scenario:** Our order processing service with `-Xmx8g` had unpredictable 3-5 second pauses during Full GC, causing request timeouts. Solutions applied:
1. Reduced object allocation by reusing objects and avoiding unnecessary `String` concatenation
2. Switched from CMS to G1GC with `-XX:MaxGCPauseMillis=100`
3. Reduced heap to 4GB (less to scan) and scaled horizontally
4. Result: max GC pause dropped from 5s to 150ms

**Common mistake:** Setting `-Xmx` too large thinking "more memory = better." A 32GB heap means GC has 32GB to scan — pauses can be 10+ seconds. It's often better to have 4 instances with 4GB each than 1 instance with 16GB.

GC tuning is a critical production skill — monitor GC metrics from day one, not when users start complaining.

---

## Q23. How do you prevent OutOfMemoryError?

OOM prevention is about proactive design — limiting what goes into memory, tuning JVM settings, and monitoring heap usage before it becomes critical.

**Prevention strategies:**

**1. Limit in-memory data structures:**
```java
// BAD: Unbounded cache
Map<String, Object> cache = new HashMap<>();

// GOOD: Bounded cache with eviction
Cache<String, Object> cache = Caffeine.newBuilder()
    .maximumSize(10_000)
    .expireAfterWrite(Duration.ofMinutes(5))
    .build();
```

**2. Stream large datasets — don't load into memory:**
```java
// BAD: Loading 1M records into a List
List<Order> orders = orderRepo.findAll();

// GOOD: Stream/paginate
@Query("SELECT o FROM Order o")
Stream<Order> streamAll();  // Processes one at a time

// Or paginate
Page<Order> page = orderRepo.findAll(PageRequest.of(0, 100));
```

**3. Tune JVM settings:**
```bash
-Xms2g -Xmx4g                          # Set min/max heap
-XX:+HeapDumpOnOutOfMemoryError         # Auto dump on OOM
-XX:HeapDumpPath=/var/logs/heap.hprof   # Dump location
-XX:MaxMetaspaceSize=256m               # Limit metaspace
```

**4. Close resources properly:**
```java
// Always use try-with-resources
try (InputStream is = new FileInputStream(file);
     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
    // process
}
```

**5. Be careful with ThreadLocal:**
```java
// Always clean up in thread pools
threadLocal.remove();  // In finally block or filter cleanup
```

**6. Monitor and alert:**
```yaml
# Prometheus alert
- alert: HighHeapUsage
  expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.85
  for: 5m
```

**Real-world scenario:** Our reporting service was OOMing when generating large Excel exports. Fix: instead of building the entire workbook in memory, we used Apache POI's streaming API (`SXSSFWorkbook`) which flushes rows to disk after a configurable window (100 rows). Memory usage dropped from 2GB to 50MB for a 500K row export.

**Common mistake:** Only increasing `-Xmx` as the solution. If you have a memory leak, more memory just delays the OOM — it doesn't fix it. Find and fix the root cause.

OOM prevention is about discipline — paginate queries, bound caches, close resources, and monitor heap. Make it a habit, not an afterthought.

---

## Q24. What monitoring tools do you use?

In production, I use a combination of Prometheus for metrics collection, Grafana for visualization, and distributed tracing (Jaeger/Zipkin) for request flow analysis — together they give full observability.

**The observability stack I've built:**

**1. Prometheus (Metrics):**
- Pull-based metrics collection — scrapes `/actuator/prometheus` endpoint
- Time-series database for JVM, HTTP, custom business metrics
```java
// Custom business metric
Counter orderCounter = Counter.builder("orders.created")
    .tag("channel", "web")
    .register(meterRegistry);
orderCounter.increment();
```

**2. Grafana (Visualization):**
- Dashboards for: JVM health, API latency (p50/p95/p99), error rates, throughput
- Alerting rules: "Alert if p99 latency > 2s for 5 minutes"
- My essential dashboards: JVM overview, API performance, database pool, business KPIs

**3. Distributed Tracing (Jaeger/Zipkin):**
- Traces requests across microservices with correlation IDs
- Identifies: which service is slow, where time is spent, failure points
- Spring Boot integration with Micrometer Tracing (formerly Spring Cloud Sleuth)

**4. Centralized Logging (ELK/Loki):**
- Elasticsearch + Logstash + Kibana (ELK) or Grafana Loki
- Structured JSON logs with trace IDs for correlation
```java
log.info("Order created", kv("orderId", orderId), kv("userId", userId));
```

**5. Alerting (PagerDuty/OpsGenie):**
- Prometheus AlertManager → PagerDuty for on-call notifications
- Critical: OOM risk, error rate spike, service down
- Warning: high latency, connection pool exhaustion, disk usage

**Real-world setup:**
```yaml
# application.yml - expose metrics
management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus, metrics
  metrics:
    tags:
      application: order-service
      environment: production
```

**Key metrics I always monitor:**
- `http_server_requests_seconds` — API latency percentiles
- `jvm_memory_used_bytes` — heap usage
- `hikaricp_connections_active` — DB connection pool usage
- `system_cpu_usage` — CPU utilization
- Custom: `orders.created`, `payments.failed` — business metrics

**Common mistake:** Collecting metrics but not setting up alerts — dashboards are useless if nobody is watching them at 3 AM. Also, alert fatigue — too many non-critical alerts and teams start ignoring them.

Observability is not optional in production — if you can't measure it, you can't manage it.

---

## Q25. What do you do in the first 30 minutes of a production issue?

The first 30 minutes of a production issue follow a disciplined process: stabilize, assess, diagnose, and communicate. Panic is the enemy — process is your friend.

**Minute 0-5: Acknowledge and assess impact:**
- Check monitoring dashboards — what's the blast radius?
- Is it affecting all users or a subset? All endpoints or specific ones?
- Are error rates spiking? Is the service completely down or degraded?
- Start a war room (Slack channel/call) and assign an incident commander

**Minute 5-10: Stabilize (stop the bleeding):**
- **Recent deployment?** → Rollback immediately. Don't debug in production under pressure.
  ```bash
  kubectl rollout undo deployment/order-service
  ```
- **Traffic spike?** → Scale up instances, enable rate limiting
- **Single instance down?** → Verify load balancer has removed it, restart if needed
- **Downstream dependency down?** → Verify circuit breakers are tripping

**Minute 10-20: Diagnose:**
- Check logs for errors — grep for stack traces around the time the issue started
  ```bash
  kubectl logs order-service-pod --since=30m | grep ERROR
  ```
- Check metrics — CPU, memory, GC, DB connections, thread pool utilization
- Check distributed traces — find failed/slow traces, identify the bottleneck service
- Check recent changes — deployments, config changes, infrastructure changes, database migrations

**Minute 20-30: Act on findings:**
- If root cause is identified → apply fix, deploy, verify
- If root cause is unclear but system is stabilized → document findings, continue investigation
- If still unstable → escalate, bring in more engineers, consider broader rollback

**Communication throughout:**
```
[Incident] Order Service - Elevated Error Rates
Status: Investigating
Impact: ~15% of order submissions failing
Current action: Rolled back deployment v2.3.1 → v2.3.0
Next update: 15 minutes
```

**Real-world scenario:** 10 PM alert: "Order Service error rate 40%." My process:
1. Dashboard check: payment endpoint returning 500s, started 8 minutes ago
2. Recent deployment: yes, 12 minutes ago → immediate rollback
3. Error rate dropped to 0% within 2 minutes of rollback
4. Root cause (next day): new code had a null pointer when a rarely-used payment provider returned an unexpected response format
5. Fix: added null safety, wrote a test for that edge case, redeployed

**Common mistakes:**
- Debugging for 30 minutes before rolling back an obvious bad deployment — rollback first, debug later
- Not communicating — stakeholders need updates even if it's "still investigating"
- Making multiple changes at once — change one thing, observe, then change another
- Not capturing evidence (thread dumps, heap dumps, logs) before restarting — you lose the crime scene

In a production incident, your job is to restore service first, find root cause second. Speed of recovery beats perfection of diagnosis.
