# 5. Database (MongoDB) — Interview Q&A tied to Robogebra

**Stack:** MongoDB via Spring Data MongoDB. Portal DB `robogebra_cms_db`; CRM has its own `robogebra-coupon-mgmt` (database-per-service). No SQL/JPA in the core learning platform.

---

## Q1. Why MongoDB (document DB) over a relational DB here?

**Answer.** Our core domain is **content** — books → chapters → exercises → items → solutions → step-by-step details. It's naturally hierarchical, deeply nested, and varies by exercise **type** (numeric vs algebra vs MCQ have different shapes). A document model fits that flexible, evolving schema far better than rigid tables + many joins. We also want horizontal scaling and fast reads of whole content trees. Where we need relational-style integrity (payments, coupons) that lives in the **CRM** service with its own DB.

**Trade-off I'd volunteer:** Mongo trades multi-table ACID/joins for flexibility. We accept eventual consistency for analytics, and use **synchronous REST + careful modeling** for the money paths. If the domain were transaction-heavy and highly relational, I'd pick Postgres.

---

## Q2. How do you model documents and relationships? `@DBRef`?

**Answer.** `@Document` maps a class to a collection. For relationships I choose **embed vs reference**: embed when the child is owned and read together; **reference** (`@DBRef`, storing the related id) when entities are large, shared, or queried independently.

**In Robogebra** (`exercise/repository/model/ExerciseEntity.java`) — an exercise **references** its book and chapter:

```java
@Document(collection = "exercise")
@CompoundIndexes({
  @CompoundIndex(name = "exercise_name_index_book_id_chapter_id",
                 def = "{'name':1,'index':1,'book.id':1,'chapter.id':1}", unique = true),
  @CompoundIndex(name = "chapter_id_order_index", def = "{'chapter.$id':1,'order':1}")
})
public class ExerciseEntity {
  @Id private String id;
  @Indexed @DBRef @Field("book")    private BookEntity book;
  @Indexed @DBRef @Field("chapter") private ChapterEntity chapter;
}
```

Book/Chapter are referenced (not embedded) because they're large and shared across many exercises.

**Follow-up: `@DBRef` downsides?** It doesn't join server-side — Spring resolves it with extra queries, so overusing it causes N+1. For hot read paths I'd store just the id or use an `$lookup` aggregation instead.

---

## Q3. How do repositories and queries work in Spring Data Mongo?

**Answer.** Extend `MongoRepository<Entity, IdType>` — you get CRUD free. Add **derived query methods** (Spring parses the method name into a query), or `@Aggregation` for pipelines, or `@Query` for raw filters.

**In Robogebra.** Derived queries (`SolutionVisibilityRepository`):

```java
public interface SolutionVisibilityRepository extends MongoRepository<SolutionVisibilityEntity, String> {
    Optional<SolutionVisibilityEntity> findByKlassIdAndTargetId(String klassId, String targetId);
    List<SolutionVisibilityEntity> findByKlassIdInAndTargetIdIn(List<String> klassIds, List<String> targetIds);
}
```

Aggregation pipeline (`ExerciseRepository`) — match + sort in the DB:

```java
@Aggregation(pipeline = {
    "{ '$match': { 'chapter.$id': ObjectId(?0) } }",
    "{ '$sort':  { 'order': 1 } }"
})
List<ExerciseEntity> findAllByChapterId(String chapterId);
```

`findByKlassIdInAndTargetIdIn` is the one that powers the whole visibility check — it fetches all candidate rows for many classes and many targets in **one** round trip.

---

## Q4. Explain indexing in your schema and why it matters.

**Answer.** Indexes turn collection scans into fast lookups. I use `@Indexed` on frequently-filtered fields and `@CompoundIndex` for multi-field queries and **uniqueness constraints**. The compound index order should match query + sort order.

**In Robogebra.** The visibility collection has a **unique compound index** `(klass_id, target_id)` — this both speeds lookups *and* enforces "one rule per class per target" at the DB level (so enable/disable is an upsert, never a duplicate):

```java
@CompoundIndex(name = "klass_id_target_id_idx", def = "{'klass_id':1,'target_id':1}", unique = true)
```

Exercises index `(chapter.$id, order)` so "get chapter's exercises sorted" is index-backed. We enable `auto-index-creation: true` in dev.

---

## Q5. Show a real query-design decision you made.

**Answer.** The **solution-visibility precedence check** is my favorite DB story. A student's access to a solution depends on the most-specific rule among *item → exercise → chapter*, across *all* their classes. Naively that's several queries and nested loops. Instead:

1. Build the precedence id list once: `List.of(exerciseItemId, exerciseId, chapterId)`.
2. **One** query for all classes × all three targets: `findByKlassIdInAndTargetIdIn(klassIds, precedence)`.
3. `groupingBy(targetId)` into a `Map` in memory.
4. Walk precedence most-specific-first; first level that has a rule decides (`anyMatch ENABLED`); default open.

```java
Map<String, List<SolutionVisibilityEntity>> byTarget =
    persistence.findByKlassIdsAndTargetIds(klassIds, precedence)
               .stream().collect(Collectors.groupingBy(SolutionVisibilityEntity::getTargetId));
for (String targetId : precedence) {
    var rows = byTarget.get(targetId);
    if (rows != null && !rows.isEmpty())
        return rows.stream().anyMatch(r -> r.getStatus() == SolutionVisibilityStatus.ENABLED);
}
return true;
```

**One DB round trip, O(1) lookups, correct override semantics.** That's a concrete "how I think about queries + data structures together" answer.

---

## Q6. Audit fields, soft deletes, timestamps?

**Answer.** Spring Data auditing (`@CreatedDate`, `@LastModifiedDate`) auto-stamps documents; soft-delete flags instead of hard deletes where history matters.

**In Robogebra.** Entities carry `@CreatedDate`/`@LastModifiedDate` (`created_at`/`updated_at`) managed automatically, and the visibility model uses an **upsert** (`findByKlassIdAndTargetId(...).map(update).orElseGet(create)`) so a teacher toggling a class just flips the existing row's status.

---

## Q7. MongoDB vs MySQL — the classic comparison.

**Answer.**

| | MongoDB (us) | MySQL |
|---|---|---|
| Model | Documents (JSON/BSON), flexible schema | Tables, fixed schema |
| Relations | Embed / `@DBRef` / `$lookup` | Foreign keys + joins |
| Transactions | Per-doc atomic; multi-doc needs replica set | Full ACID |
| Scaling | Horizontal (sharding) | Vertical primarily |
| Fits | Nested, evolving content | Highly relational, transactional |

We use Mongo because content is hierarchical and schema-flexible; a payments/coupons system would be a fair place for SQL (and the CRM keeps that concern separate).

---

### Rapid-fire recap

| Topic | Robogebra example | File |
|-------|-------------------|------|
| Document + refs | `@Document`, `@DBRef` book/chapter | `ExerciseEntity.java` |
| Repositories | derived + `@Aggregation` | `SolutionVisibilityRepository.java`, `ExerciseRepository.java` |
| Indexing | unique compound `(klass_id,target_id)` | `SolutionVisibilityEntity.java` |
| Query design | 1 query + `groupingBy` + precedence | `SolutionVisibilityService.java` |
| Auditing/upsert | `@CreatedDate`, find-or-create | visibility model |
