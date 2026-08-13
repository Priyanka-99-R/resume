# 12. System Design — walkthroughs tied to Robogebra

Senior rounds ask you to design something on a whiteboard. Best strategy: **design a feature you actually built.** These are ready-to-draw walkthroughs.

---

## Design 1 — Solution Visibility (the feature you built end-to-end)

**Prompt framing:** *"Teachers want to control when their class can see a solution — at chapter, exercise, or single-question level. Design it."*

**Requirements.**
- Teacher enables/disables a solution for chosen classes, at 3 granularities (chapter / exercise / question).
- Student's app checks access before showing a solution.
- Most-specific rule wins; default = visible; students outside an institute unaffected.

**Data model (MongoDB).** One collection `solution_visibility`, one row per *(class, target)*:

```
{ klassId, targetId, status: ENABLED|DISABLED, createdAt, updatedAt }
unique compound index (klass_id, target_id)   // one rule per class per target → upsert, no dupes
```

**Write path (teacher).** `PATCH /api/solution-visibility/status {level, targetId, klassIds[], status}` → validate teacher role → validate target exists → **upsert** one row per class.

**Read path (student).** `GET /exercise-items/{id}/solution-access` →
1. Resolve student's `klassIds` (enrollment).
2. Build precedence `[itemId, exerciseId, chapterId]`.
3. **One** query for all classes × all three targets; `groupingBy(targetId)`.
4. Walk precedence most-specific-first; first level with a rule decides (`anyMatch ENABLED`); else open.

**Why these choices (say this).** Unique index = data-integrity + upsert semantics. Single query + in-memory grouping = one round trip, O(1) lookups (vs N queries). Precedence list = the "specific overrides general" product rule expressed as data. Default-open + no-class-skip = safe for non-institute users.

**Scale/extend.** Cache per-class rules (rarely change); add an exercise-level access endpoint by reusing the precedence resolver; audit via timestamps.

```
Teacher UI ──PATCH status──▶ Portal ──upsert(klass,target)──▶ solution_visibility
Student UI ──GET access───▶ Portal ──1 query [item,ex,chapter]──▶ precedence walk ──▶ allowed?
```

---

## Design 2 — File upload to S3 (classic full-stack design)

**Prompt:** *"Users upload images/assets. Design upload + serving."*

**Answer — pre-signed URLs, file never touches the app tier.**
1. Client asks portal: *"I want to upload X"* → portal returns a **pre-signed PUT URL** (short expiry) + the object key.
2. Client `PUT`s the bytes **directly to S3**.
3. Client tells portal the key → portal stores **only key + metadata** in MongoDB (not the binary).
4. To display, portal returns a pre-signed **GET** URL (5-min expiry).

**Why:** keeps large binaries out of the app server and the DB, scales with S3, and stays private (no public buckets). Add CloudFront for delivery, validate content-type/size, virus-scan async via Lambda if needed. This is exactly Robogebra's model (`robogebra-dev-assets` / `robogebra-core-content`, 5-min signed URLs).

```
Client ─req URL─▶ Portal ─presign─▶ (PUT direct)─▶ S3
Client ─save key─▶ Portal ─▶ Mongo{key,meta};  read: Portal ─presigned GET─▶ Client
```

---

## Design 3 — Notifications (multi-channel, async)

**Prompt:** *"Send OTP / payment / welcome messages over email, SMS, WhatsApp."*

**Answer — Factory + async.** A `NotificationServiceFactory` picks the channel (WhatsApp if the username is a phone number, else email via SES), and methods are `@Async` so the request thread never blocks on a third-party call. Real Robogebra code:

```java
@Async public void sendOtp(String userName, String otp) { getNotificationService(userName).sendOTP(userName, otp); }
private NotificationService getNotificationService(String u) {
    return u.matches(PHONE_PATTERN) ? whatsAppNotificationService : emailNotificationService;
}
```

**Scale:** move to a queue (SNS/SQS) for retries + backpressure; make sends idempotent; dead-letter failures.

---

## Design 4 — "How would you scale Robogebra for 10× users?"

**Answer — walk the tiers.**
- **App tier:** services are **stateless** (JWT, no session) → horizontal autoscale behind a load balancer.
- **DB tier:** Mongo — right indexes (compound, high-cardinality), read replicas, shard hot collections; watch `@DBRef` N+1 (prefer `$lookup`/embed for hot reads).
- **Cache:** Caffeine in-process for rarely-changing content (books/chapters) with TTLs; Redis if we need a shared cache.
- **Media:** S3 + CloudFront CDN.
- **Async:** offload spiky/ML work to Lambda + queues; notifications async.
- **Resilience:** timeouts + retries + circuit breakers on portal→CRM / portal→AI; graceful degradation (cached/basic answer if AI down).
- **Observability:** metrics, logs, PostHog; capacity alarms.

---

## Design framework (say this out loud in any design round)

1. **Clarify** requirements + scale (reads vs writes, consistency needs).
2. **Data model** first (it drives everything).
3. **APIs** (endpoints, verbs, status codes).
4. **Core algorithm** (e.g., the precedence resolution).
5. **Scale & failure** (cache, index, async, circuit breakers, degradation).
6. **Trade-offs** — always name what you gave up (e.g., Mongo flexibility vs multi-doc ACID).
