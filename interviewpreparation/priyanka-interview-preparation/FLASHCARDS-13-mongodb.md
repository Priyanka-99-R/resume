# 🃏 MongoDB — Rapid-Recall Flashcards

> Cover the **A:** line, say your answer out loud, then check. Full detail in [13-mongodb.md](./13-mongodb.md).
> ⚡ = high-yield in every round · 🔵 = from YOUR code. Aim to clear all cards in <10 min as a daily habit.

---

**Q1 ⚡ What is MongoDB?**
A: A NoSQL, document-oriented DB. Stores flexible JSON-like documents (BSON) in collections. Schema-less, horizontally scalable.

**Q2 Document vs Collection?**
A: Document = a BSON record (≈ row, can be nested). Collection = group of documents (≈ table, no fixed schema).

**Q3 What is BSON?**
A: Binary JSON — MongoDB's storage format. Adds types (ObjectId, Date, binary, int/long/decimal); optimized for speed/space.

**Q4 What is `_id`?**
A: Auto-added unique primary key. Default = 12-byte ObjectId (has a timestamp); can override with your own value.

**Q5 ⚡ MySQL vs MongoDB — key differences?**
A: Tables/rows vs collections/documents · fixed schema vs flexible · joins/FKs vs embed/reference · vertical vs horizontal (sharding) scaling · SQL vs JSON query language.

**Q6 ⚡ When choose MongoDB over MySQL?**
A: Mongo → flexible/evolving schema, nested data, high write throughput, horizontal scale, catalogs/logs. MySQL → strong relational integrity, complex joins, transactional consistency. Often use **both** (polyglot persistence).

**Q7 ACID vs BASE?**
A: ACID (relational) = Atomicity, Consistency, Isolation, Durability. BASE (NoSQL) = Basically Available, Soft state, Eventual consistency. Mongo = ACID per single doc always; multi-doc ACID since v4.0.

**Q8 CRUD methods?**
A: insertOne/insertMany · find/findOne · updateOne/updateMany (`$set`, `$inc`) · deleteOne/deleteMany.

**Q9 Common query operators?**
A: Comparison `$eq $ne $gt $gte $lt $lte $in $nin` · Logical `$and $or $not $nor` · Element `$exists $type` · Array `$all $elemMatch $size` · Update `$set $unset $inc $push $pull $addToSet`.

**Q10 ⚡ Embedding vs Referencing?**
A: Embed = nested data in parent → one-to-few, read together, bounded size. Reference = store other doc's `_id` → one-to-many/many-to-many, large or independently-changing data (needs 2nd lookup / `$lookup`).

**Q11 Rule of thumb embed vs reference?**
A: Embed when accessed together + bounded ("contains"). Reference when large/shared/unbounded or queried on its own. Mind the **16 MB** document limit.

**Q12 Does schema-less mean no design?**
A: No — design documents around **query patterns** ("model for how you read"). Flexibility aids evolution; a bad model still hurts.

**Q13 ⚡ What are indexes?**
A: B-tree structures that avoid full collection scans. `createIndex({name:1})` ascending, `{role:1, exp:-1}` compound.

**Q14 Types of indexes?**
A: Single-field, compound, multikey (arrays), text, geospatial, hashed (sharding), TTL (auto-expire).

**Q15 How to analyze query performance?**
A: `explain("executionStats")`. Look for `IXSCAN` (good, index used) vs `COLLSCAN` (bad, full scan).

**Q16 ⚡ What is the aggregation pipeline?**
A: Stages that transform docs like a Unix pipe (≈ SQL GROUP BY + functions). e.g. `$match` → `$group` → `$sort` → `$limit`.

**Q17 Common aggregation stages?**
A: `$match $group $project $sort $limit $skip $unwind` (flatten arrays) `$lookup` (join) `$count $addFields`.

**Q18 Replica set?**
A: Group of servers with same data: one primary (writes) + secondaries (copies). Primary fails → automatic election promotes a secondary → high availability.

**Q19 Sharding?**
A: Horizontal scaling — split data across shards by a **shard key**; `mongos` router directs queries. For huge datasets / high write throughput.

**Q20 Replication vs Sharding?**
A: Replication = copies of **same** data → availability + read scaling. Sharding = **splits** data → write scaling + large datasets.

**Q21 ⚡ Spring Data MongoDB setup?**
A: Add `spring-boot-starter-data-mongodb`, set `spring.data.mongodb.uri`, annotate entities `@Document(collection="...")` with `@Id`, use `MongoRepository`.

**Q22 ⚡ What is MongoRepository?**
A: Spring Data interface = CRUD + derived query methods (`findByRole`, `findBySkillsContaining`, `findByExpGreaterThan`). Like JpaRepository, for Mongo.

**Q23 ⚡ MongoRepository vs MongoTemplate?**
A: Repository = high-level derived queries, quick standard CRUD. Template = low-level, fine-grained control for complex queries, aggregations, bulk ops, dynamic criteria.

**Q24 Aggregations in Spring Data?**
A: Via `MongoTemplate` + `Aggregation` builder: `Aggregation.newAggregation(match(...), group(...).sum(...).as(...))` then `mongoTemplate.aggregate(...)`.

**Q25 Map a MySQL feature to MongoDB?**
A: Find the read pattern. Hierarchical + read together (e.g. dashboard w/ nested progress) → embed in one doc = single fast read vs many joins. Shared + large → reference.

**Q26 ⭐⭐ Honest framing line — REWRITTEN (the old one was wrong):**
A: **"Yes — MongoDB is the database on my current project.** RoboGebra's Spring Boot backend uses Spring Data MongoDB: around a hundred `@Document` classes, eighty-five repositories, and twenty-seven `@Aggregation` pipelines. I've also used **Mongoose** on the Node CRM, so I've worked the document model from both Java and JavaScript."

> ⚠️ The previous version of this card said *"my production DB experience is MySQL/Postgres/Neo4j"* and treated Mongo as a gap. **That was wrong and it was costing marks.** See [13-mongodb.md](./13-mongodb.md).

---

## 🔵 Your-code cards — the ones that actually win the round ⭐

**Q27 ⭐⭐ Tell me about a hard MongoDB problem you solved.**
A: **An N+1.** Loading a chapter's exercises then fetching each exercise's items one at a time = 1 + N queries — and `@DBRef` makes it easy to fall into, because Spring Data resolves each reference with its own query. Fifty exercises meant fifty-one round trips. I replaced it with one `@Aggregation` pipeline: `$match` on `exercise.$id` with `$in`, then `$sort` by `displayOrder`. **"A document database doesn't remove the N+1 problem — it just changes what it looks like."** ⭐

**Q28 ⭐ How do you decide embed vs reference?**
A: Read together + **bounded** → embed. Shared / large / **unbounded** → reference. And decide **per field, not per entity** ⭐ — our `ExerciseSolutionEntity` uses `@DBRef` for the shared exercise item (it changes independently) but **embeds** the AI step output (meaningless outside its solution, always read with it). Hard limit: **16 MB per document**, so never embed an unbounded array.

**Q29 ⭐ What's the cost of `@DBRef`?**
A: An **extra query per reference** — that's the N+1 trap. It's convenient for modelling, expensive at read time. Where it hurts, replace it with an aggregation pipeline.

**Q30 ⭐ Do you use indexes? Give a real one.**
A: Yes — `@CompoundIndex(name = "user_exercise_index", def = "{'user.$id': 1, 'exercise.$id': 1}")` on quiz attempts, because the hot query is always *"this user's attempts at this exercise"*. **Field order matters:** with `user` first the same index also serves *"all attempts by this user"* — but it would **not** serve a query on `exercise` alone (the **prefix rule** ⭐).

**Q31 ⭐ `@Transactional` on MongoDB — anything different?**
A: Yes. Multi-document transactions need a **replica set** (Mongo 4.0+) and carry real overhead — it's not the free lunch it is on a relational DB. Single-document writes are **always atomic**, so most of our write paths don't need a transaction at all.

**Q32 ⭐ Spring Data MongoDB vs Spring Data JPA — the difference that matters?**
A: **No persistence context, no dirty checking, no lazy loading.** In JPA an entity loaded inside a transaction saves itself at commit; in Mongo **you must call `save()` explicitly**. The repository *programming model* is identical — which is why moving between them is easy.

**Q33 What does `?0` mean in an `@Aggregation` pipeline?**
A: It binds the **first method parameter** into the pipeline. `"{ '$match': { 'exercise.$id': { '$in': ?0 } } }"` takes the `List<ObjectId>` argument.

**Q34 ⭐ Mongoose — what do you know?**
A: The Node ODM, used on our CRM (Mongoose 8.8 + Express). Schema-level **validators**, reusable **plugins**, and `pre`/`post` **hooks** — password hashing happens in a `pre('save')`. `.populate()` is the rough equivalent of `@DBRef`. Also `counter.model.js` — the **auto-increment sequence pattern**, since Mongo has no `AUTO_INCREMENT` ⭐.

**Q35 ⭐ MongoDB numbers you can quote:**
A: 104 `@Document` · 85 `MongoRepository` interfaces · 27 `@Aggregation` pipelines · 130 `@Transactional` · `@CompoundIndex` on the hot paths · plus 10+ Mongoose schemas on the Node CRM.

---

> ⭐ **The one-liner for the whole topic:** *"MongoDB is my current production database, in two languages. The interesting part isn't the CRUD — it's knowing that N+1 and index prefixes still apply, they just look different."*
