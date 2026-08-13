# Advanced Microservices Scenario-Based Interview Questions
### For 5+ Years Java / Spring Boot Developer
### "What would you do if..." — Architecture Thinking Under Pressure

---

## Scenario 1: A downstream service is intermittently timing out in production, causing cascading failures. How do you diagnose and fix it?

**Summary:**
This is a classic cascading failure problem. My immediate response is: contain the blast radius first, then diagnose. I'd use circuit breaker + distributed tracing to isolate and pinpoint the issue.

**My Step-by-Step Approach:**

**Step 1 — Contain (minutes 0–5):**
- Check Grafana dashboards — is it one instance or all? Is it the downstream service itself or a dependency of that service?
- If the circuit breaker is not already open, verify Resilience4j config. Ensure fallback is returning gracefully degraded responses.
- If bulkhead is not in place, the timeout is likely exhausting the calling service's thread pool. Reduce the timeout aggressively (e.g., from 10s to 3s) via Spring Cloud Config refresh — no redeployment needed.

**Step 2 — Diagnose (minutes 5–30):**
- Pull the trace ID from a failed request → open in Jaeger/Zipkin → identify exactly which span is slow.
- Check the downstream service's metrics: thread pool saturation? GC pauses? DB connection pool exhaustion?
- Check Kubernetes pod metrics: is it CPU/memory throttled? Is a node under pressure?
- Check the database: slow query log, lock contention, missing index.

**Step 3 — Fix:**
- If DB query issue → add index, optimize query, add read replica.
- If thread pool exhaustion → increase pool size short-term, add bulkhead long-term.
- If the downstream's dependency is the real culprit → apply circuit breaker at that level too.
- If GC pressure → analyze heap dump, tune JVM flags, check for memory leaks.

**Step 4 — Prevent:**
- Add alerting on p99 latency before it reaches timeout threshold.
- Implement bulkhead pattern so one slow downstream doesn't kill calls to other services.
- Add circuit breaker if not present. Run chaos engineering tests to validate.

**What Interviewers Want to Hear:**
That you think in layers — contain first, diagnose second, fix third, prevent fourth. Not just "add a circuit breaker."

---

## Scenario 2: Two microservices need the same data. Service A owns it. Service B needs it for every request. How do you architect this?

**Summary:**
This is a data coupling problem. The answer depends on latency requirements and consistency tolerance. I'd use event-driven data replication — Service A publishes change events, Service B maintains a local read-only copy.

**Option Analysis:**

| Approach | Pros | Cons | When to Use |
|----------|------|------|-------------|
| Sync REST call | Simple, always fresh | Latency, coupling, availability risk | Rarely needed, low frequency |
| Event-driven local copy | Fast reads, decoupled | Eventual consistency | High frequency reads |
| Shared cache (Redis) | Fast, shared | Cache invalidation complexity | Read-heavy, tolerance for staleness |
| API Gateway aggregation | No data duplication | N+1 call risk, latency | Dashboard/BFF scenarios |

**My Preferred Approach — Event-Driven Replication:**

```
Service A (owns Customer data)
    │
    ├── DB: customers table (source of truth)
    │
    └── Publishes: CustomerCreated, CustomerUpdated events → Kafka
                                                                │
Service B (needs Customer data)                                 │
    │                                                           │
    ├── Consumes events ◄───────────────────────────────────────┘
    │
    └── DB: customer_cache table (read-only projection)
```

Service B stores only the fields it needs (e.g., `customerId`, `name`, `tier`) — not the entire customer entity. This is the **bounded context** principle in action.

**Handling Edge Cases:**
- **Stale data** — For 99% of use cases, a few seconds of staleness is fine. If Service B absolutely needs fresh data (e.g., credit check), it makes a sync call with caching + short TTL.
- **Initial data load** — On first deployment, Service B consumes all historical events from Kafka (compact topic) or uses a one-time bulk sync endpoint from Service A.
- **Schema evolution** — Use Avro + Schema Registry so Service A can evolve events without breaking Service B.

**What Interviewers Want to Hear:**
That you don't default to "just call the API" — you think about coupling, latency, and failure modes. And that you understand eventual consistency trade-offs.

---

## Scenario 3: Your Kafka consumer is lagging behind — millions of unprocessed messages. What do you do?

**Summary:**
Consumer lag means messages are being produced faster than consumed. My approach: triage the cause, scale consumption, and prevent recurrence.

**Immediate Triage:**
```
1. Is the consumer DOWN or just SLOW?
   ├── DOWN → Fix and restart. Messages are retained in Kafka.
   └── SLOW → Why?
       ├── Slow DB writes → Batch inserts, async processing
       ├── Slow external API call → Circuit breaker, async decoupling
       ├── GC pauses → Tune JVM, reduce object allocation
       └── Single partition → Can't parallelize (see below)
```

**Short-Term Fix — Scale Consumers:**
- Increase consumer instances up to the number of Kafka partitions. If topic has 10 partitions, you can have max 10 consumers in the same group.
- If partitions are the bottleneck, increase partition count (careful — this rebalances keys).

**Medium-Term Fix — Optimize Processing:**
```java
// BEFORE: Processing one message at a time
@KafkaListener(topics = "orders")
public void consume(OrderEvent event) {
    orderRepository.save(mapToEntity(event));  // 1 DB call per message
}

// AFTER: Batch processing
@KafkaListener(topics = "orders", batch = "true")
public void consumeBatch(List<OrderEvent> events) {
    List<OrderEntity> entities = events.stream()
        .map(this::mapToEntity).toList();
    orderRepository.saveAll(entities);  // 1 DB call for N messages
}
```

- Enable batch consumption — process 500 messages in one DB batch insert instead of 500 individual inserts.
- Move slow processing to a separate thread pool (don't block the consumer thread).
- If the consumer calls an external API, decouple it — write to a local DB first, process asynchronously.

**Long-Term Prevention:**
- Alert on consumer lag (e.g., lag > 10,000 for 5 minutes → PagerDuty).
- Auto-scale consumers using KEDA (Kubernetes Event-Driven Autoscaler) based on consumer lag metrics.
- Set appropriate `max.poll.records` and `max.poll.interval.ms` to prevent consumer group rebalancing.

**What Interviewers Want to Hear:**
That you understand Kafka's partition-consumer relationship, that you optimize before scaling, and that you set up monitoring to catch lag before it becomes millions.

---

## Scenario 4: You need to deploy a breaking database schema change without downtime. How?

**Summary:**
This requires a multi-phase deployment strategy — expand, migrate, contract. Never make breaking schema changes in a single release.

**The Three-Phase Approach:**

**Phase 1 — Expand (backward compatible):**
- Add the new column/table alongside the old one.
- Deploy the new code that writes to BOTH old and new columns.
- Old code still works — it reads/writes the old column.

```sql
-- Phase 1: Add new column, keep old one
ALTER TABLE customers ADD COLUMN first_name VARCHAR(100);
ALTER TABLE customers ADD COLUMN last_name VARCHAR(100);
-- 'name' column still exists and is still written to
```

```java
// Code writes to both
customer.setName(fullName);              // old column
customer.setFirstName(firstName);         // new column
customer.setLastName(lastName);           // new column
```

**Phase 2 — Migrate:**
- Backfill existing data from old column to new columns.
- Deploy code that reads from new columns but still writes to both.
- Verify data integrity.

```sql
-- Backfill script (run in batches to avoid locking)
UPDATE customers
SET first_name = SPLIT_PART(name, ' ', 1),
    last_name = SPLIT_PART(name, ' ', 2)
WHERE first_name IS NULL
LIMIT 10000;  -- batch it
```

**Phase 3 — Contract (after verification):**
- Remove code that reads/writes the old column.
- Drop the old column in a subsequent release.

```sql
-- Phase 3: Only after all services use new columns
ALTER TABLE customers DROP COLUMN name;
```

**Why Three Phases?**
During deployment, both old and new versions of the service run simultaneously (rolling update). If the new code requires a column that doesn't exist yet, or the old code requires a column that was dropped — crash.

**Flyway/Liquibase Integration:**
- Each phase is a separate migration file.
- Phase 1 migration deploys with the code that writes to both.
- Phase 3 migration deploys only after Phase 2 is verified and all old instances are gone.

**What Interviewers Want to Hear:**
That you understand rolling deployments mean two versions of code coexist, so the schema must be compatible with both at every step.

---

## Scenario 5: A production incident — one microservice is consuming 100% CPU. Walk me through your investigation.

**Summary:**
High CPU is usually caused by infinite loops, excessive GC, unoptimized queries triggering heavy serialization, or a sudden traffic spike. My approach: observe, isolate, profile, fix.

**Step 1 — Observe (don't restart yet):**
```bash
# Check if it's one pod or all pods
kubectl top pods -n production | grep order-service

# Check if traffic spiked
# → Look at Grafana request rate dashboard

# Check GC activity
kubectl exec -it order-service-pod -- jstat -gcutil <PID> 1000
# If Old Gen is 100% and Full GC runs constantly → memory leak
```

**Step 2 — Capture Diagnostics Before Restart:**
```bash
# Thread dump — shows what threads are doing RIGHT NOW
kubectl exec -it order-service-pod -- jstack <PID> > thread_dump.txt
# Take 3 dumps 5 seconds apart — threads stuck in the same place = the culprit

# Heap dump — if GC is the suspect
kubectl exec -it order-service-pod -- jmap -dump:live,format=b,file=/tmp/heap.hprof <PID>
# Download and analyze with Eclipse MAT or VisualVM
```

**Step 3 — Analyze Thread Dumps:**
Common findings:
- **100 threads stuck at the same line** → contention (synchronized block, DB connection pool exhausted).
- **Thread in infinite loop** → code bug (usually a while loop with a broken exit condition).
- **All threads in `GC` state** → memory issue, analyze heap dump.
- **Threads blocked on I/O** → downstream service timeout, connection pool exhaustion.

**Step 4 — Common Root Causes and Fixes:**

| Root Cause | Evidence | Fix |
|-----------|----------|-----|
| Infinite loop | Thread dump shows same stack trace spinning | Fix the loop condition, deploy hotfix |
| Memory leak → GC thrashing | Heap dump shows growing collection | Fix the leak (unclosed streams, static maps) |
| Traffic spike | Request rate 10x normal | Auto-scale, add rate limiting |
| Regex backtracking | Thread in `Pattern.match` | Fix the regex (avoid nested quantifiers) |
| Inefficient serialization | Threads in Jackson/Gson | Exclude large fields, use pagination |
| Missing DB index | Threads blocked in JDBC | Add index, optimize query |

**Step 5 — Mitigate if Fix Takes Time:**
- Scale out — add more pods to distribute load.
- Rate limit — throttle at the API Gateway.
- Restart the offending pod (last resort, after capturing diagnostics).

**What Interviewers Want to Hear:**
That you capture diagnostics BEFORE restarting, that you take multiple thread dumps for comparison, and that you think systematically about CPU vs GC vs I/O causes.

---

## Scenario 6: Your services are running fine in staging but failing in production. Same code, same config. What do you check?

**Summary:**
"Same config" is almost never true. The differences are usually in data volume, traffic patterns, infrastructure, or hidden environment-specific settings.

**My Diagnostic Checklist:**

**1. Configuration Drift:**
```
□ Compare actual running config, not source config
  → kubectl exec pod -- curl localhost:8080/actuator/env
  → Are environment variables different? (DB endpoints, feature flags)
□ Secrets/Vault — different credentials or expired tokens?
□ Config Server — is production profile actually being loaded?
  → Check: spring.profiles.active in /actuator/info
```

**2. Data Differences:**
```
□ Production has millions of rows; staging has thousands
  → A query that runs in 10ms with 1K rows takes 30s with 10M rows
  → Missing index that doesn't matter with small data
□ Data patterns — null values, special characters, edge cases
  → Staging test data is "clean"; production data is messy
□ Schema differences — migration that ran in staging but failed silently in prod
```

**3. Infrastructure Differences:**
```
□ Network policies — service mesh rules, firewall rules
□ Resource limits — CPU/memory limits different in prod K8s manifests
□ DNS resolution — different service mesh config, timeouts
□ SSL/TLS — certificates expired or different CA chain
□ Load balancer — session affinity, timeout settings
```

**4. Traffic Patterns:**
```
□ Concurrency — staging gets 10 req/sec, prod gets 10,000
  → Connection pool exhaustion
  → Thread pool saturation
  → Race conditions that never trigger under low load
□ Payload sizes — production requests are 10x larger
```

**5. External Dependencies:**
```
□ Third-party APIs — sandbox vs production endpoints
□ Different rate limits in production
□ Different response formats or latencies
```

**Real-World Example:**
We had a service that worked perfectly in staging but threw `ConnectionPoolExhaustion` in production. Root cause: staging had HikariCP `maximumPoolSize=10` (sufficient for 50 req/sec). Production had the same config but 5,000 req/sec. We increased the pool size and added connection pool metrics to Grafana alerting.

**What Interviewers Want to Hear:**
That you systematically check config, data, infra, traffic, and dependencies — not just "check the logs."

---

## Scenario 7: A critical service goes down. You have no fallback. How do you recover, and how do you prevent this in the future?

**Summary:**
Immediate recovery is about restoring service ASAP — rollback, restart, scale. Prevention is about eliminating single points of failure through redundancy, graceful degradation, and circuit breakers.

**Immediate Recovery (War Room Mode):**

```
Minute 0-2: DETECT
├── Alert fires (PagerDuty/OpsGenie)
├── Check: Is it the service or its dependency?
└── Check: Is it one pod, one node, or all pods?

Minute 2-5: CONTAIN
├── If bad deployment → kubectl rollout undo deployment/order-service
├── If one pod → K8s should auto-restart (liveness probe)
├── If node failure → K8s reschedules pods to healthy nodes
├── If dependency failure → can we survive without it? (read below)
└── Communicate: Incident channel in Slack, assign roles (IC, comms)

Minute 5-15: RECOVER
├── If DB issue → failover to replica, check connection
├── If OOM → increase memory limits, restart
├── If config issue → rollback config, refresh
└── If unknown → scale up replicas while investigating

Minute 15+: STABILIZE
├── Monitor recovery metrics
├── Verify all health checks pass
└── Stand down alert, schedule post-mortem
```

**Prevention Architecture:**

```java
// 1. Multi-instance deployment (never run 1 replica in prod)
spec:
  replicas: 3
  strategy:
    rollingUpdate:
      maxUnavailable: 1

// 2. Pod Disruption Budget
apiVersion: policy/v1
kind: PodDisruptionBudget
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: order-service

// 3. Anti-affinity (spread across nodes)
affinity:
  podAntiAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      - topologyKey: kubernetes.io/hostname
```

**Post-Mortem Action Items:**
- Add fallback/degradation path for this service.
- Implement circuit breaker in all callers.
- Add PodDisruptionBudget if missing.
- Ensure minimum 3 replicas across 2+ availability zones.
- Add synthetic monitoring (health check pings every 30s).
- Update runbook for this service.

**What Interviewers Want to Hear:**
That you have a structured incident response process, that you think about rollback before debugging, and that you drive blameless post-mortems with concrete prevention items.

---

## Scenario 8: You're designing a notification service that sends emails, SMS, and push notifications. How do you architect it?

**Summary:**
I'd build it as an event-driven service with a priority queue, channel-specific adapters, and retry/dead-letter handling. Key principles: async, pluggable, reliable.

**Architecture:**

```
Producers (Order, Auth, Marketing services)
    │
    └── Publish NotificationRequested event → Kafka
                                                │
Notification Service                            │
    │                                           │
    ├── Consumer ◄──────────────────────────────┘
    │     │
    │     ├── Priority Router
    │     │     ├── HIGH (OTP, payment alerts) → Immediate processing
    │     │     ├── MEDIUM (order updates) → Batched processing
    │     │     └── LOW (marketing) → Scheduled/throttled
    │     │
    │     ├── Channel Router
    │     │     ├── EmailAdapter → SendGrid / SES
    │     │     ├── SmsAdapter → Twilio / SNS
    │     │     └── PushAdapter → Firebase FCM
    │     │
    │     └── Template Engine (Thymeleaf/Freemarker)
    │
    ├── Notification DB (status tracking, audit)
    │
    ├── Retry Queue (failed → retry with exponential backoff)
    │
    └── Dead Letter Queue (max retries exceeded → manual review)
```

**Key Design Decisions:**

```java
// Strategy pattern for channels
public interface NotificationChannel {
    void send(NotificationRequest request);
    boolean supports(ChannelType type);
}

@Component
public class EmailChannel implements NotificationChannel {
    public void send(NotificationRequest req) {
        String body = templateEngine.render(req.getTemplate(), req.getParams());
        emailClient.send(req.getRecipient(), req.getSubject(), body);
    }
    public boolean supports(ChannelType type) { return type == EMAIL; }
}

// Router selects channels
@Service
public class NotificationRouter {
    private final List<NotificationChannel> channels;

    public void route(NotificationRequest req) {
        for (ChannelType type : req.getChannels()) {
            channels.stream()
                .filter(ch -> ch.supports(type))
                .findFirst()
                .ifPresent(ch -> ch.send(req));
        }
    }
}
```

**Reliability Considerations:**
- **Idempotency** — Deduplicate by `notificationId` to prevent sending the same SMS twice on retry.
- **Rate limiting per channel** — Twilio has rate limits; queue and throttle outbound SMS.
- **User preferences** — Check opt-in/opt-out before sending.
- **DLQ monitoring** — Alert if DLQ depth > 0 for high-priority notifications.
- **Audit trail** — Log every notification attempt with status (SENT, FAILED, RETRYING) for compliance.

**What Interviewers Want to Hear:**
That you think about reliability (retries, DLQ, idempotency), scalability (priority queues, batching), and extensibility (strategy pattern for new channels).

---

## Scenario 9: Two teams are deploying changes to services that interact with each other. How do you prevent integration breakage?

**Summary:**
This is a coordination and contract testing problem. I use Consumer-Driven Contract Testing (Pact), API versioning, and a well-defined deployment pipeline with integration gates.

**My Multi-Layer Strategy:**

**Layer 1 — Contract Testing (Development Time):**
```
Consumer (Order Service) defines:
  "I expect GET /api/users/{id} to return { id, name, email }"
      │
      └── Pact file (contract) → shared via Pact Broker
                                      │
Provider (User Service) verifies:     │
  "Can I fulfill this contract?" ◄────┘
```

If the provider breaks the contract, their CI build fails BEFORE merging. No surprise breakages in production.

```java
// Consumer side (Order Service test)
@Pact(consumer = "order-service")
public RequestResponsePact userDetailsPact(PactDslWithProvider builder) {
    return builder
        .given("user 123 exists")
        .uponReceiving("a request for user 123")
        .path("/api/users/123")
        .method("GET")
        .willRespondWith()
        .status(200)
        .body(newJsonBody(body -> {
            body.integerType("id", 123);
            body.stringType("name", "John");
            body.stringType("email", "john@example.com");
        }).build())
        .toPact();
}
```

**Layer 2 — API Versioning & Backward Compatibility:**
- Rule: Only additive changes (new fields, new endpoints). Never remove or rename existing fields.
- If breaking change is needed → new API version, run both in parallel.
- OpenAPI spec is committed in the repo — PR review includes API diff.

**Layer 3 — Integration Testing in CI/CD:**
```
PR Merge → Unit Tests → Contract Tests → Deploy to Staging
                                              │
                                    Integration Tests (E2E)
                                              │
                                    ✅ Pass → Deploy to Prod (Canary)
                                    ❌ Fail → Block deployment
```

**Layer 4 — Communication & Process:**
- Shared Slack channel `#api-changes` for announcing breaking changes.
- RFC process for major API changes — both teams review before implementation.
- Feature flags to decouple deployment from release.

**What Interviewers Want to Hear:**
That you use automated contract testing to catch issues early, that you enforce backward compatibility as a rule, and that you have a process for coordinating cross-team changes.

---

## Scenario 10: You need to migrate from a synchronous REST architecture to an event-driven architecture. How do you approach it?

**Summary:**
This is a gradual migration, not a big-bang switch. I use the Strangler Fig pattern — introduce events alongside REST calls, migrate one flow at a time, and decommission REST calls once events are proven.

**Phase-by-Phase Approach:**

**Phase 1 — Dual Write (Foundation):**
```
BEFORE:
Order Service --REST--> Payment Service --REST--> Notification Service

AFTER Phase 1:
Order Service --REST--> Payment Service
      │
      └── ALSO publishes OrderPlaced event → Kafka
                                               │
                              Notification Service (now consumes from Kafka)
```
- Start with non-critical flows (notifications, analytics).
- Keep the REST call to Payment intact — it's critical and needs sync response.
- Notification Service switches from REST to Kafka consumer.

**Phase 2 — Event-First for Suitable Flows:**
```
Order Service publishes OrderPlaced → Kafka
    │
    ├── Inventory Service consumes → reserves stock → publishes StockReserved
    ├── Notification Service consumes → sends confirmation
    └── Analytics Service consumes → records metrics

Order Service --REST--> Payment Service (still sync — needs immediate response)
```

**Phase 3 — Full Event-Driven with Saga:**
```
OrderSagaOrchestrator
    │
    ├── Step 1: Create Order → OrderCreated event
    ├── Step 2: Reserve Inventory → InventoryReserved / InventoryFailed
    ├── Step 3: Process Payment → PaymentCompleted / PaymentFailed
    ├── Step 4: Confirm Order → OrderConfirmed
    └── Compensation: If Step 3 fails → Release Inventory → Cancel Order
```

**Key Infrastructure Changes:**
```
1. Set up Kafka cluster (3+ brokers for HA)
2. Schema Registry (Avro/Protobuf for event schemas)
3. Debezium CDC for Outbox pattern
4. Consumer lag monitoring (Grafana + Burrow)
5. Dead Letter Queue handling per consumer group
```

**What Stays Synchronous:**
- Queries that need immediate responses (GET /orders/{id}).
- Operations requiring immediate validation (check if user exists before creating order).
- External-facing APIs (clients expect request-response).

**Migration Checklist Per Flow:**
```
□ Define event schema (Avro + Schema Registry)
□ Implement Outbox pattern in producer
□ Build idempotent consumer
□ Set up DLQ and alerting
□ Run both REST and event paths in parallel (shadow mode)
□ Compare results for consistency
□ Switch traffic to event path
□ Decommission REST call (after 2-week bake period)
```

**What Interviewers Want to Hear:**
That you migrate incrementally, start with non-critical flows, keep sync where it's needed, and validate with parallel running before cutting over.

---

## Scenario 11: Your microservices application has a memory leak in production. How do you find and fix it?

**Summary:**
Memory leaks in Java manifest as increasing Old Gen usage, frequent Full GC, and eventually `OutOfMemoryError`. I capture heap dumps from the running process, analyze with Eclipse MAT, and trace the leak to its root allocation.

**Detection — How I Know It's a Leak:**
```
Signs:
├── Grafana shows heap usage trending upward over hours/days (sawtooth pattern rising)
├── GC frequency increasing — Full GC happening every few minutes
├── Pod restarts due to OOMKilled in K8s
└── Response latency increasing (GC pauses)

Verify:
$ kubectl exec -it pod -- jstat -gcutil <PID> 5000
  S0     S1     E      O      M     CCS    YGC   YGCT    FGC   FGCT    CGC   CGCT     GCT
  0.00  45.23  67.12  89.45  95.30  92.10  1234  12.34   45   23.45   120   5.67   41.46
                       ^^^^^ Old Gen climbing to 100%
```

**Capture (Before the Pod Crashes):**
```bash
# Heap dump
kubectl exec -it pod -- jmap -dump:live,format=b,file=/tmp/heap.hprof <PID>
kubectl cp pod:/tmp/heap.hprof ./heap.hprof

# Or configure JVM to dump on OOM automatically
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heap.hprof
```

**Analysis with Eclipse MAT:**
```
1. Open heap.hprof in Eclipse MAT
2. Run "Leak Suspects Report" — it highlights the biggest memory consumers
3. Check "Dominator Tree" — shows which objects retain the most memory
4. Common findings:
   ├── HashMap/ConcurrentHashMap growing unbounded
   │   → Cache without eviction policy (missing TTL/max size)
   ├── List/ArrayList accumulating objects
   │   → Processing loop adding to a list but never clearing
   ├── Unclosed resources (DB connections, HTTP connections, streams)
   │   → Missing try-with-resources
   └── Static collections
       → Static Map used as cache, never evicted
```

**Top 5 Common Leak Patterns in Spring Boot:**

| Pattern | Example | Fix |
|---------|---------|-----|
| Unbounded cache | `static Map<String, Object> cache` | Use Caffeine with `maximumSize` and `expireAfterWrite` |
| Unclosed resources | `InputStream` not closed in catch block | `try-with-resources` |
| Listener/callback accumulation | Registering event listeners without deregistering | Use `@PreDestroy` cleanup |
| ThreadLocal not cleaned | ThreadLocal in servlet filter | `remove()` in finally block |
| Large session state | Storing large objects in HTTP session | Externalize session to Redis, minimize session data |

**Fix Example:**
```java
// LEAK: Unbounded cache
private static final Map<String, UserProfile> cache = new ConcurrentHashMap<>();

public UserProfile getUser(String id) {
    return cache.computeIfAbsent(id, this::loadFromDb);  // grows forever!
}

// FIX: Bounded cache with eviction
private final Cache<String, UserProfile> cache = Caffeine.newBuilder()
    .maximumSize(10_000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();

public UserProfile getUser(String id) {
    return cache.get(id, this::loadFromDb);
}
```

**What Interviewers Want to Hear:**
That you capture diagnostics before restarting, that you use proper tooling (MAT, jstat) to analyze, and that you know the common leak patterns in Spring Boot applications.

---

## Scenario 12: Your team is asked to support multi-tenancy in an existing microservices application. How do you design it?

**Summary:**
Multi-tenancy means a single application instance serves multiple customers (tenants) with data isolation. I'd implement it using a discriminator column approach with Hibernate filters, tenant-aware routing, and a shared infrastructure with logical isolation.

**Multi-Tenancy Models:**

| Model | Isolation | Cost | Complexity | When to Use |
|-------|-----------|------|------------|-------------|
| Separate DB per tenant | Highest | Highest | High | Regulated industries (banking, healthcare) |
| Separate schema per tenant | High | Medium | Medium | Medium-sized SaaS with compliance needs |
| Shared schema + tenant column | Lowest | Lowest | Lowest | Most SaaS applications |

**My Preferred Approach — Shared Schema + Discriminator:**

```java
// 1. Tenant Context (extracted from JWT or subdomain)
public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        currentTenant.set(tenantId);
    }
    public static String getTenantId() {
        return currentTenant.get();
    }
    public static void clear() {
        currentTenant.remove();
    }
}

// 2. Servlet Filter — extracts tenant from JWT
@Component
public class TenantFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, ...) {
        String tenantId = extractTenantFromJwt(req);
        TenantContext.setTenantId(tenantId);
        try {
            filterChain.doFilter(req, res);
        } finally {
            TenantContext.clear();  // Prevent ThreadLocal leak!
        }
    }
}

// 3. Hibernate Filter — automatic tenant scoping on all queries
@Entity
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Order {
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    // ... other fields
}

// 4. Aspect to enable filter on every request
@Aspect
@Component
public class TenantAspect {
    @Autowired private EntityManager entityManager;

    @Before("execution(* com.app.repository.*.*(..))")
    public void setTenantFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter")
               .setParameter("tenantId", TenantContext.getTenantId());
    }
}
```

**Cross-Cutting Concerns:**
- **Kafka** — Include `tenantId` in event headers. Consumers filter or route by tenant.
- **Redis cache** — Prefix keys with tenantId: `tenant123:user:456`.
- **Logging** — Add `tenantId` to MDC so every log line is tenant-scoped.
- **Rate limiting** — Per-tenant limits to prevent noisy neighbor.

**Data Isolation Validation:**
- Write integration tests that create data for Tenant A and verify Tenant B cannot see it.
- Add a database-level row-level security policy as a safety net (PostgreSQL RLS).

**What Interviewers Want to Hear:**
That you think about tenant isolation at every layer (DB, cache, messaging, logging), that you handle ThreadLocal cleanup to prevent cross-tenant data leaks, and that you consider the noisy neighbor problem.

---

## Scenario 13: You need to design a microservices system that handles 50,000 requests per second. What's your approach?

**Summary:**
50K RPS requires horizontal scaling, async processing, caching at multiple layers, and database optimization. The key is to handle reads and writes differently — reads are cheap (cache them), writes are expensive (queue them).

**Architecture for 50K RPS:**

```
                        CDN (static content, API response caching)
                              │
                        Load Balancer (L4/L7)
                              │
                    ┌─────────┼─────────┐
                    │         │         │
               API Gateway  Gateway   Gateway   (3+ instances, auto-scaled)
                    │
            ┌───────┼────────┬─────────┐
            │       │        │         │
         Service  Service  Service   Service    (auto-scaled per service)
            │       │        │         │
         Cache    Cache    Cache     Cache      (local Caffeine + shared Redis)
            │       │        │         │
            └───────┼────────┴─────────┘
                    │
              ┌─────┼─────┐
              │     │     │
           Primary Read   Read                  (DB: 1 writer + N read replicas)
              │   Replica Replica
              │
           Kafka                                (async writes, event processing)
```

**Layer-by-Layer Strategy:**

**1. CDN + API Gateway (shed load early):**
- Cache GET responses at CDN level (Cache-Control headers).
- Rate limiting at gateway — block abuse before it reaches services.
- Response compression (gzip) — reduces bandwidth by 70%.

**2. Application Layer (horizontal scaling):**
```yaml
# Kubernetes HPA — auto-scale based on CPU/custom metrics
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  minReplicas: 10
  maxReplicas: 100
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          averageUtilization: 60
    - type: Pods
      pods:
        metric:
          name: http_requests_per_second
        target:
          averageValue: 1000  # 1K RPS per pod × 50 pods = 50K
```

**3. Caching (most important optimization):**
```
L1: Local cache (Caffeine) — 0ms latency, per-instance
    → Hot data: user sessions, config, feature flags
    → Size: 10K-50K entries, TTL: 5 minutes

L2: Distributed cache (Redis Cluster) — 1-2ms latency, shared
    → Frequently read data: product catalog, user profiles
    → Size: millions of entries, TTL: 15-60 minutes

L3: Database (PostgreSQL/MySQL) — 5-50ms latency
    → Source of truth, only hit on cache miss
```

**4. Database (the bottleneck):**
- Read replicas — route all SELECT queries to replicas.
- Connection pooling — HikariCP with tuned pool size (`connections = CPU cores * 2 + disk spindles`).
- Partition hot tables by date/tenant.
- For write-heavy: write to Kafka first, batch-write to DB async.

**5. Async Processing (absorb write spikes):**
- Writes go to Kafka → consumers batch-process to DB.
- This turns 50K write RPS into batched inserts of 1K rows/sec.

**Load Testing Validation:**
```bash
# Gatling / k6 load test
k6 run --vus 5000 --duration 10m load_test.js
# Monitor: p99 < 200ms, error rate < 0.1%, no pod restarts
```

**What Interviewers Want to Hear:**
That you design for 50K RPS by caching reads, queuing writes, auto-scaling horizontally, and testing with realistic load — not by over-provisioning hardware.

---

## Scenario 14: An upstream team pushes a change that breaks your service's API contract in production. How do you handle it, and how do you prevent it from happening again?

**Summary:**
Immediate action: rollback or feature-flag the broken consumer path. Prevention: contract testing, API governance, and backward compatibility enforcement in CI.

**Immediate Response:**

```
Minute 0: Alert fires — 500 errors spiking on our service
    │
    ├── Check: Is it our code or incoming data?
    │     → Logs show deserialization errors on responses from upstream
    │     → Upstream changed their API response format
    │
    ├── Short-term fix options:
    │     1. Ask upstream to rollback (fastest if they can)
    │     2. Deploy our hotfix — add @JsonIgnoreProperties(ignoreUnknown = true)
    │        and handle the missing/changed field gracefully
    │     3. Feature flag — disable the feature that depends on upstream
    │
    └── Communicate: Post in incident channel, tag upstream team
```

**Defensive Coding (Tolerant Reader Pattern):**
```java
// FRAGILE: Breaks if upstream adds/removes fields
@JsonIgnoreProperties  // ← MISSING! Any unknown field = crash

// RESILIENT: Tolerant Reader
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {
    private Long id;
    private String name;
    private String email;       // If upstream removes this, we get null — not a crash
    @JsonProperty("email")
    private String emailAddress; // Handle field rename gracefully
}
```

**Prevention — Multi-Layer Defense:**

**Layer 1 — Consumer-Driven Contract Tests (Pact):**
- We define what we expect from upstream.
- Upstream verifies against our contract in their CI.
- If they break it, THEIR build fails.

**Layer 2 — API Compatibility Checks in CI:**
```bash
# OpenAPI breaking change detection in upstream's CI
openapi-diff previous-api.yaml current-api.yaml
# Fails on: removed fields, changed types, removed endpoints
# Passes on: added fields, added endpoints (additive changes)
```

**Layer 3 — Backward Compatibility Policy:**
- All teams agree: APIs are backward compatible by default.
- Breaking changes require: RFC → consumer notification → new version → deprecation period → removal.
- API versioning is mandatory for breaking changes.

**Layer 4 — Integration Testing Gate:**
- Staging environment runs integration tests with all dependent services before any production deployment.
- Canary deployment with metric analysis catches issues that slip through.

**What Interviewers Want to Hear:**
That you code defensively (Tolerant Reader), that you use automated contract testing, and that you push for organizational standards around API governance — not just blame the upstream team.

---

## Scenario 15: You're building a system that needs to process exactly-once semantics with Kafka. Is it possible? How do you achieve it?

**Summary:**
True exactly-once is extremely hard in distributed systems. Kafka provides exactly-once semantics (EOS) within the Kafka ecosystem, but end-to-end exactly-once requires idempotent producers + transactional consumers + idempotent sinks.

**Understanding the Problem:**

```
Exactly-once = At-least-once delivery + Idempotent processing

Why it's hard:
Producer → Kafka: Message could be duplicated if ack is lost
Kafka → Consumer: Message could be reprocessed after consumer crash
Consumer → DB: DB write could be duplicated on retry
```

**Layer 1 — Idempotent Producer (Kafka-side):**
```properties
# Prevents duplicate messages from producer retries
enable.idempotence=true
acks=all
retries=Integer.MAX_VALUE
max.in.flight.requests.per.connection=5
```
Kafka assigns a producer ID and sequence number. Broker deduplicates by (producerId, sequence).

**Layer 2 — Transactional Producer + Consumer (Kafka-to-Kafka):**
```java
// Exactly-once within Kafka: read from topic A, process, write to topic B
properties.put("transactional.id", "order-processor-1");
producer.initTransactions();

while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
    producer.beginTransaction();
    try {
        for (ConsumerRecord<String, String> record : records) {
            ProducerRecord<String, String> output = process(record);
            producer.send(output);
        }
        producer.sendOffsetsToTransaction(offsets, consumerGroupId);
        producer.commitTransaction();
    } catch (Exception e) {
        producer.abortTransaction();
    }
}
```
This atomically commits the output message AND the consumer offset — so if it crashes, it reprocesses from the last committed offset without duplicates.

**Layer 3 — Idempotent Sink (Kafka-to-Database):**
```java
// Kafka EOS doesn't extend to external systems. You MUST make the sink idempotent.

@KafkaListener(topics = "payments")
@Transactional
public void processPayment(PaymentEvent event) {
    // Check if already processed
    if (processedEventRepository.existsByEventId(event.getEventId())) {
        log.info("Duplicate event {}, skipping", event.getEventId());
        return;
    }

    // Process
    paymentService.charge(event);

    // Record as processed (in the SAME transaction as the business operation)
    processedEventRepository.save(new ProcessedEvent(event.getEventId()));

    // Commit offset manually after successful processing
}
```

**The Key Insight:**
```
                        Kafka EOS covers this
                    ┌──────────────────────────┐
                    │                          │
Producer ──────► Kafka Topic A ──────► Kafka Topic B
                                          │
                                          │ ← This part requires YOUR idempotency
                                          ▼
                                       Database
```

**What Interviewers Want to Hear:**
That you understand exactly-once is a spectrum — Kafka handles it within its ecosystem, but you must build idempotent consumers for external sinks. And that you know the difference between idempotent producer, transactional processing, and idempotent consumption.

---

## Quick Reference: Scenario Decision Matrix

| Scenario | First Action | Key Pattern | Tool/Tech |
|----------|-------------|-------------|-----------|
| Cascading timeout | Circuit breaker + trace | Bulkhead + CB | Resilience4j + Jaeger |
| Shared data need | Evaluate consistency needs | Event-driven replication | Kafka + local projection |
| Consumer lag | Check consumer health | Batch + scale | Kafka partitions + KEDA |
| Breaking schema change | Expand-migrate-contract | Blue-green compatible | Flyway + rolling deploy |
| 100% CPU | Thread dump before restart | Profile + fix | jstack + MAT |
| Staging vs prod diff | Config + data comparison | Actuator + metrics | kubectl + Grafana |
| Critical service down | Rollback first | Redundancy + PDB | K8s + auto-scaling |
| Notification system | Event-driven + adapters | Strategy + DLQ | Kafka + channel adapters |
| Cross-team breakage | Contract testing | Pact + tolerant reader | Pact Broker + CI gate |
| REST to event migration | Strangler Fig | Dual write → cutover | Kafka + Outbox |
| Memory leak | Heap dump + MAT | Fix allocation pattern | jmap + Eclipse MAT |
| Multi-tenancy | Tenant discriminator | Hibernate filter + TLS | ThreadLocal + RLS |
| 50K RPS | Cache + async + scale | L1/L2 cache + Kafka | Caffeine + Redis + HPA |
| Upstream breaks contract | Tolerant Reader + rollback | Pact + API governance | Jackson + Pact |
| Exactly-once Kafka | Idempotent sink | EOS + dedup table | Kafka TX + processed_events |
