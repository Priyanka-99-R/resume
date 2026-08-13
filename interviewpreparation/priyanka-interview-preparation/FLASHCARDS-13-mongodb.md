# 🃏 MongoDB — Rapid-Recall Flashcards

> Cover the **A:** line, say your answer out loud, then check. Full detail in [13-mongodb.md](./13-mongodb.md).
> ⚡ = high-yield for Berribot. Aim to clear all cards in <10 min for the daily habit.

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

**Q26 ⭐ Honest framing line:**
A: "Production DB experience is MySQL/Postgres/Neo4j. I know Mongo's document model, indexing, aggregation pipeline, replica sets/sharding, and Spring Data (`@Document`, `MongoRepository`, `MongoTemplate`). My **Neo4j NoSQL mindset** bridges me in fast."
