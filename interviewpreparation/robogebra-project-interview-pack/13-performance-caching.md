# 13. Performance & Caching — Q&A tied to Robogebra

Depth questions on "how do you make it fast" — answered with real project levers: Caffeine cache, Mongo indexing, pagination, `@DBRef` N+1, and frontend perf.

---

## Q1. How do you cache, and what do you cache?

**Answer.** In-process **Caffeine** cache (Spring cache abstraction) for data that's **read-often, changes-rarely** — books, chapters, exercises. Per-group **TTL + max size**, and the whole cache is **toggleable by property** (so we can disable it per environment). Content that rarely changes but is read on every page load is the ideal cache candidate; user-specific/volatile data is not.

**In Robogebra** (`CacheConfig`) — conditional Caffeine manager with per-group TTL:

```java
@Configuration @EnableCaching
public class CacheConfig {
  @Bean @ConditionalOnProperty(prefix="cache", name="enabled", havingValue="true")
  public CacheManager cacheManager() {
    List<CaffeineCache> caches = cacheProperties.getManager().stream()
      .flatMap(g -> g.getCacheNames().stream().map(name -> new CaffeineCache(name,
        Caffeine.newBuilder().expireAfterWrite(g.getTtlMinutes(), TimeUnit.MINUTES)
                .maximumSize(g.getMaxSize()).recordStats().build())))
      .toList();
    ...
  }
  @Bean @ConditionalOnProperty(prefix="cache", name="enabled", havingValue="false")
  public CacheManager noOpCacheManager() { return new NoOpCacheManager(); }
}
```

TTLs in config: ~1440 min (24h) for books/chapters/exercises, ~60 min for subscriptions, max ~10k entries.

**Follow-up: cache invalidation?** The two hard problems in CS 🙂. We use **TTL (expire-after-write)** as the baseline, and `@CacheEvict` on writes for correctness where staleness matters. In-process Caffeine is per-instance; for a shared cache across instances I'd move to **Redis**.

---

## Q2. How do you keep MongoDB queries fast?

**Answer.** **Indexes matched to query patterns**, one round-trip designs, and pagination. `@Indexed` on filtered fields, `@CompoundIndex` for multi-field queries and uniqueness (order matches query + sort). I avoid N+1 by fetching in bulk.

**In Robogebra.**
- Unique compound `(klass_id, target_id)` on visibility — fast lookups + integrity.
- `(chapter.$id, order)` on exercises so "chapter's exercises, sorted" is index-backed.
- The visibility check does **one** `findByKlassIdInAndTargetIdIn(...)` for all classes × targets instead of N queries (see `05-database.md` Q5).

**Follow-up: `@DBRef` N+1 risk?** `@DBRef` resolves with extra queries, so a list of exercises each dereferencing book/chapter can explode into N+1. On hot read paths I'd store just the id or use an `$lookup` aggregation to join server-side.

---

## Q3. How do you handle large result sets?

**Answer.** **Pagination** with Spring Data `Pageable`/`PageRequest` — never return unbounded lists. The repository takes a `Pageable`, returns a `Page<T>`, and I map entities→models while preserving page metadata.

**In Robogebra** (`SolutionStepFeedbackService`) — paged query + `Page.map`:

```java
return solutionStepFeedbackRepository
        .findByReviewedUser_Id(userId, pageable)     // Pageable in
        .map(SolutionStepFeedbackEntity::toModel);    // Page<Model> out, metadata kept
```

`PageableConstants` centralizes default page sizes. Controllers accept page/size params and pass a `PageRequest` down.

---

## Q4. Async & non-blocking — where does it help perf?

**Answer.** Move slow, non-critical I/O **off the request thread** with `@Async` so response latency isn't hostage to a third party. Notifications (OTP/email/WhatsApp), last-active-timestamp updates, and analytics writes are async. For outbound HTTP we use non-blocking `WebClient` with timeouts. Heavy/spiky compute (AI solution generation, interactive graphs) is offloaded to **Lambda** so it scales independently.

---

## Q5. Frontend performance levers?

**Answer.**
- **`trackBy`** on `*ngFor` → Angular reuses DOM nodes instead of destroy/recreate (used in analytics/progress lists).
- **`async` pipe** → no manual subscriptions, fewer leaks, CD tied to emissions.
- **Lazy-loaded** feature modules → smaller initial bundle.
- **`debounceTime`** on rapid input → fewer expensive calls (worksheet executor).
- **`OnPush` + immutability** (improvement I'd push) → skip dirty-checking untouched subtrees.
- **Resolvers** → data ready on navigation, no layout thrash.
- Silent **token refresh** in the interceptor → no failed-request round-trips visible to the user.

---

## Q6. How would you find and fix a performance problem?

**Answer.** **Measure first.** Backend: enable Mongo profiler / `explain()` to find collection scans → add/adjust indexes; check for N+1 from `@DBRef`; add caching for hot reads; paginate. Frontend: Chrome DevTools performance + Angular DevTools to find excessive change detection or large bundles → OnPush, `trackBy`, lazy-load, code-split. I optimize the **proven** bottleneck, not a guess, and confirm with before/after numbers.

---

### Rapid-fire recap

| Lever | Robogebra implementation | File |
|------|--------------------------|------|
| Caching | Caffeine, per-group TTL, toggleable, NoOp fallback | `CacheConfig.java` |
| Indexing | unique compound `(klass_id,target_id)`, `(chapter,order)` | entities |
| One round-trip | `findByKlassIdInAndTargetIdIn` bulk fetch | `SolutionVisibilityService.java` |
| Pagination | `Pageable` → `Page.map(toModel)` | `SolutionStepFeedbackService.java` |
| Async | `@Async` notifications, `WebClient`, Lambda offload | `NotificationServiceFactory.java` |
| Frontend | `trackBy`, `async`, lazy-load, `debounceTime` | lists, interceptor |
