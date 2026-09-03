# MongoDB — Interview Q&A (Easy Version)

> ## ✅ **POSITIONING FIX — read this first**
>
> An older version of this file said MongoDB was a *gap* on your resume. **That is wrong, and it was costing you marks.**
>
> **RoboGebra runs on MongoDB.** Verified in your own workspace:
>
> ```
> robogebra-portal (Java 17 / Spring Boot 3.2)
>    104 × @Document          entity classes
>     85 × @Repository        MongoRepository interfaces
>     27 × @Aggregation       pipelines
>        × @CompoundIndex     compound indexes on hot query paths
>        × MongoTemplate      in the persistence services
>
> robogebra-crm (Node / Express)
>    Mongoose 8.8 — 10+ schemas with validators, plugins and hooks
> ```
>
> **You have production MongoDB experience on your current project, in two languages.** Say so with confidence. See **[39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)**.

Every question follows the same shape — **Easiest way to remember → simple explanation → real-world example → diagram → interview trap → Easy memory box** — with 🔵 blocks showing your real code.

---

## Contents

1. [Fundamentals](#fundamentals)
2. [CRUD Operations](#crud-operations)
3. [Schema Design](#schema-design)
4. [Indexing & Performance](#indexing--performance)
5. [Aggregation](#aggregation)
6. [Scaling & Reliability](#scaling--reliability)
7. [Spring Data MongoDB](#spring-data-mongodb)
8. [Mongoose (Node)](#mongoose-node)
9. [Your Answer Scripts](#your-answer-scripts)
10. [Quick Revision Sheet](#quick-revision-sheet)

---

## Fundamentals

### Q: What is MongoDB?

The easiest way to remember:

```
MongoDB = a database that stores JSON-like DOCUMENTS instead of table rows. ⭐
```

```
SQL                          MongoDB
───                          ───────
database    →                database
table       →                COLLECTION
row         →                DOCUMENT ⭐
column      →                field
JOIN        →                embed, or $lookup
primary key →                _id
```

Real-world idea: a **filing cabinet vs a spreadsheet.**

```
SPREADSHEET (SQL) → every row must have the SAME columns.
                    Adding a column means changing every row.

FOLDER (MongoDB)  → each folder holds whatever papers that case needs.
                    A new case can carry an extra document; the others don't care ⭐
```

#### Easy memory

```
Document = a row 📄 | Collection = a table 📁 | _id = the primary key
JSON-like (BSON), FLEXIBLE schema, scales HORIZONTALLY ⭐
```

---

### Q: What is a document and a collection?

```json
// one DOCUMENT in the "employees" collection
{
  "_id": ObjectId("64f..."),
  "name": "Priyanka",
  "role": "Full Stack Developer",
  "skills": ["Angular", "Java", "Spring Boot"],     // ⭐ an ARRAY inside a row
  "address": { "city": "Chennai", "country": "India" }   // ⭐ a NESTED object
}
```

⭐ Look at what a single document does that a SQL row cannot:

```
"skills" is an ARRAY        → in SQL that's a second table + a join
"address" is an OBJECT      → in SQL that's a third table + another join

MongoDB reads all three in ONE trip ⭐
```

That is the whole value proposition: **model for how you READ.**

#### Easy memory

```
Document = a BSON object; can hold ARRAYS and NESTED objects ⭐
Collection = a group of documents, NO fixed schema
One document can replace three SQL tables + two joins ⭐
```

---

### Q: What is BSON?

```
BSON = Binary JSON — MongoDB's storage format.
       JSON + extra TYPES + optimised for speed and size. ⭐
```

```
JSON has: string, number, boolean, null, array, object
BSON adds: ObjectId ⭐, Date ⭐, Binary, int32/int64, Decimal128, Timestamp
```

⭐ Why the extra types matter:

```
JSON has ONE "number" type → you cannot tell an int from a double,
and money in a float is a rounding bug waiting to happen 💥

BSON has Decimal128 for money, and a real Date type instead of a string ⭐
```

#### Easy memory

```
BSON = Binary JSON ⭐ → adds ObjectId, Date, Decimal128, Binary
Why: JSON has one number type; BSON can store money and dates properly ⭐
```

---

### Q: What is `_id`?

```
_id = the automatic PRIMARY KEY on every document.
      By default a 12-byte ObjectId. ⭐
```

```
ObjectId("64f3a1b2c9e77a0012ab34cd")
          └──────┘└────┘└──┘└────┘
          4 bytes  5 bytes 3 bytes
          TIMESTAMP ⭐ random  counter
```

⭐ Two useful consequences:

```
1. An ObjectId CONTAINS its creation time
      → sorting by _id ≈ sorting by creation date, for free ⭐
      → you can extract the date: ObjectId.getTimestamp()

2. It's generated CLIENT-side, so inserts don't need a round trip
   to ask the database for an id ⭐
      (compare with SQL IDENTITY, which blocks batch inserts — [24](./24-orm-jpa-hibernate.md))
```

#### Easy memory

```
_id = the automatic primary key, a 12-byte ObjectId ⭐
It EMBEDS a timestamp → sort by _id ≈ sort by createdAt ⭐
Generated client-side → no round trip (unlike SQL auto-increment) ⭐
You can supply your own _id.
```

---

### Q: SQL vs NoSQL (MySQL vs MongoDB)?

| | MySQL (relational) | MongoDB (document) |
|---|---|---|
| Data model | tables, rows, columns | collections, documents |
| Schema | fixed, predefined | flexible, dynamic ⭐ |
| Relationships | joins, foreign keys | **embed** or **reference** ⭐ |
| Scaling | vertical (bigger server) | **horizontal** (sharding) ⭐ |
| Transactions | strong ACID | ACID per document; multi-doc since v4.0 ⭐ |
| Query language | SQL | JSON-shaped query language |
| Best for | structured, relational, reporting | flexible, nested, evolving, high write volume |

The honest way to frame the choice:

```
Ask: "does the read pattern have a natural DOCUMENT boundary?"

An invoice with its line items → ONE document ✅ read in one trip
A social graph / heavy reporting → SQL joins or a graph DB ✅
```

⚠️ **The mistake to avoid in the answer:** saying "MongoDB is faster." It isn't inherently faster — it's faster *for reads that match your document shape*, and slower for anything needing joins across collections.

#### Easy memory

```
SQL   → fixed schema, JOINS, vertical scaling, reporting
Mongo → flexible schema, EMBED, horizontal SHARDING ⭐

Choose by the READ PATTERN, not by fashion ⭐
⚠️ Never say "MongoDB is faster" — say "faster for reads shaped like the document"
```

---

### Q: What is ACID vs BASE?

```
ACID (relational) → Atomicity, Consistency, Isolation, Durability → strong guarantees
BASE (many NoSQL) → Basically Available, Soft state, Eventual consistency
                    → favours availability and scale ⭐
```

⭐ Where MongoDB actually sits — be precise, this gets probed:

```
SINGLE-document operations  → ALWAYS atomic ✅ (always were)
MULTI-document transactions → supported since v4.0 ⭐
                              BUT they require a REPLICA SET ⚠️
```

#### 🔵 In your RoboGebra code

```
robogebra-portal uses @Transactional in 130 places —
   on MongoDB, which means the deployment MUST be a replica set ⭐
```

> 🗣️ *"`@Transactional` on MongoDB isn't the free lunch it is on a relational database — multi-document transactions need a replica set, and they carry real overhead. Most of our write paths are single-document, which Mongo makes atomic by itself, so we only reach for a multi-document transaction where an operation genuinely spans collections."*

⭐ That answer proves you've used it rather than read about it.

#### Easy memory

```
ACID = strong guarantees | BASE = availability + eventual consistency
MongoDB: single-document ALWAYS atomic ✅
         multi-document transactions since v4.0 — need a REPLICA SET ⚠️ ⭐
Good schema design usually REMOVES the need for a transaction ⭐
```

---

## CRUD Operations

### Q: Basic CRUD in the shell?

```js
// CREATE
db.employees.insertOne({ name: "Priyanka", role: "Developer", exp: 4 });
db.employees.insertMany([{ name: "A" }, { name: "B" }]);

// READ
db.employees.find({ role: "Developer" });
db.employees.findOne({ name: "Priyanka" });
db.employees.find({ exp: { $gte: 4 } });                 // exp >= 4
db.employees.find({ role: "Developer" }, { name: 1, _id: 0 });   // ⭐ PROJECTION

// UPDATE
db.employees.updateOne({ name: "Priyanka" }, { $set: { exp: 5 } });
db.employees.updateMany({ role: "Developer" }, { $inc: { exp: 1 } });

// DELETE
db.employees.deleteOne({ name: "B" });
db.employees.deleteMany({ exp: { $lt: 2 } });
```

⚠️ **The trap that destroys data — worth volunteering:**

```js
db.employees.updateOne({ name: "Priyanka" }, { exp: 5 });          // ❌ NO $set
```

```
Without $set, MongoDB REPLACES the entire document.
      → name, role and every other field are GONE 💥
      → the document is now just { _id, exp: 5 }
```

**Always use an update operator.**

#### Easy memory

```
insertOne/Many | find/findOne | updateOne/Many | deleteOne/Many
Projection: find(filter, { name: 1, _id: 0 }) ⭐

⚠️ updateOne without $set REPLACES the whole document 💥 ALWAYS use $set ⭐
```

---

### Q: Common query operators?

```
COMPARISON  $eq $ne $gt $gte $lt $lte $in $nin
LOGICAL     $and $or $not $nor
ELEMENT     $exists $type
ARRAY       $all $elemMatch ⭐ $size
UPDATE      $set ⭐ $unset $inc $push $pull $addToSet ⭐
```

⭐ The two array operators worth knowing properly:

```js
// $elemMatch — ALL conditions must match the SAME array element ⭐
db.orders.find({ items: { $elemMatch: { product: "Pen", qty: { $gt: 5 } } } });
// without $elemMatch, one element could match "Pen" and a DIFFERENT one match qty > 5 💥

// $addToSet vs $push
{ $push:     { skills: "Java" } }   // adds, even if it's already there
{ $addToSet: { skills: "Java" } }   // adds ONLY if absent ⭐ (a set)
```

#### Easy memory

```
$gte $in $exists | $and $or | $set ⭐ $inc $push $pull
$elemMatch ⭐ = all conditions on the SAME array element
$addToSet ⭐ = push only if it isn't already there
```

---

## Schema Design

### Q: Embedding vs referencing — how do you model relationships?

⭐ **This is the single most important MongoDB design question.**

The easiest way to remember:

```
EMBED     → the data is read TOGETHER and is BOUNDED   → "contains"
REFERENCE → the data is SHARED, LARGE, or grows forever → "relates to"
```

```json
// EMBEDDING — one read, no join ⭐
{ "name": "Order1",
  "items": [ {"product":"Pen","qty":2}, {"product":"Book","qty":1} ] }

// REFERENCING — a second lookup needed
{ "name": "Order1", "customerId": ObjectId("...") }
```

```
EMBED                          REFERENCE
─────                          ─────────
1 query ⚡                      2 queries (or a $lookup) 🐢
data duplicated                data stored once ⭐
update in many places 💥       update in ONE place ✅
⚠️ 16 MB document limit        no size limit
```

#### The decision rule to state

```
ONE-TO-FEW   (an order's items, an address)     → EMBED ⭐
ONE-TO-MANY  (a user's 10,000 orders)           → REFERENCE ⭐
MANY-TO-MANY (students ↔ courses)               → REFERENCE
Changes independently / shared                   → REFERENCE
Read together, always                            → EMBED
```

Real-world idea:

```
EMBED     → a passport: photo, name and visa pages are all IN the book 📕
REFERENCE → a library card: it points to books it doesn't contain 🎫
```

⚠️ **The 16 MB limit is the killer constraint:**

```
Embedding an UNBOUNDED array (comments, logs, events)
      → the document grows forever
      → at 16 MB every write FAILS 💥
      → and long before that, every read drags the whole array over the wire
```

#### 🔵 In your RoboGebra code — you use `@DBRef` (referencing) ⭐

**File:** `domain/exercisesolution/repository/model/ExerciseSolutionEntity.java`

```java
@Document(collection = "exercise_solution")
@CompoundIndexes({
        @CompoundIndex(name = "exercise_item_id_index", def = "{'exercise_item.$id': 1}")
})
public class ExerciseSolutionEntity {
    @Id private String id;

    @Indexed
    @DBRef                                 // ⭐ a REFERENCE, not embedded
    @Field("exercise_item")
    private ExerciseItemEntity exerciseItem;

    @Field("step_output_json")             // ⭐ a large AI payload, embedded as-is
    private Object stepOutputJson;
    @Field("animated_step_output_json")
    private Object animatedStepOutputJson;
}
```

> 🗣️ *"Solutions **reference** their exercise item with `@DBRef`, because the same item is shared and changes independently — embedding it would duplicate it across every solution. But the AI-generated step output is **embedded** as a nested object, because it's meaningless outside its solution and is always read with it. That's the embed-versus-reference decision made per field rather than per entity."*

⭐ **That is a genuinely senior answer** — most candidates treat it as an all-or-nothing choice.

⚠️ And the `@DBRef` caveat to volunteer:

```
@DBRef is convenient, but Spring Data resolves it with an EXTRA QUERY per
reference — which is exactly the N+1 problem again.
      ↓
Which is why we also have 27 @Aggregation pipelines that fetch in one trip ⭐
```

#### Easy memory

```
EMBED ⭐     → read together + BOUNDED → 1 query 📕 (a passport)
REFERENCE ⭐ → shared / large / unbounded → 2 queries 🎫 (a library card)

⚠️ 16 MB document limit → NEVER embed an unbounded array 💥
⚠️ @DBRef costs an extra query per reference → N+1 ⭐

Decide PER FIELD, not per entity ⭐ (your ExerciseSolutionEntity does both)
```

---

### Q: Does schema-less mean no design?

```
NO. It means the DATABASE doesn't enforce the schema — YOU still design it. ⭐

And you design it around your QUERY PATTERNS, not around normalisation. ⭐
```

```
SQL design:    "what is the correct normalised model?"  → then write queries
Mongo design:  "what queries do I need?"                → then shape the document ⭐
```

⚠️ Flexible schema is a **migration** tool, not a licence:

```
Same collection, three different shapes because nobody agreed on one
      → every read needs defensive checks
      → "schema-less" becomes "schema in the application code, undocumented" 💥
```

⭐ In practice you enforce a schema *somewhere*: `@Document` classes in Spring, a Mongoose schema in Node, or MongoDB's own JSON-schema validation.

#### Easy memory

```
Schema-less ≠ design-less ⭐
Model for how you READ, not for normalisation ⭐
Enforce the schema in the app: @Document / Mongoose schema / $jsonSchema validation
```

---

## Indexing & Performance

### Q: What are indexes in MongoDB?

```
An index = a sorted B-tree that lets Mongo JUMP to matching documents
           instead of scanning the whole collection. ⭐
```

```js
db.employees.createIndex({ name: 1 });            // 1 = ascending
db.employees.createIndex({ role: 1, exp: -1 });   // COMPOUND ⭐
db.employees.createIndex({ email: 1 }, { unique: true });
```

```
WITHOUT an index → COLLSCAN → read all 1,000,000 documents 🐢
WITH an index    → IXSCAN   → jump straight to the matches ⚡
```

Real-world idea: the **index at the back of a book.** Without it, you read every page.

⚠️ Indexes are not free:

```
✅ Reads get much faster
❌ Every WRITE must also update every index 🐢
❌ Indexes consume RAM — and Mongo wants them in memory ⭐
```

⭐ **The compound-index rule that gets asked — the ESR / prefix rule:**

```
An index on { role: 1, exp: -1 } can serve:
   find({ role })                ✅ (a PREFIX of the index)
   find({ role, exp })           ✅
   find({ exp })                 ❌ NOT the prefix → no index used 💥
```

```
Order the fields: Equality → Sort → Range  ("ESR") ⭐
```

#### 🔵 In your RoboGebra code — real compound indexes ⭐

```java
@CompoundIndexes({
    @CompoundIndex(name = "exercise_item_id_index", def = "{'exercise_item.$id': 1}")
})
// and in SelfQuizAttemptEntity:
@CompoundIndex(name = "user_exercise_index", def = "{'user.$id': 1, 'exercise.$id': 1}")
```

> 🗣️ *"The quiz-attempt collection has a compound index on `user` plus `exercise`, because the hot query is always 'this user's attempts at this exercise'. Field order matters — with user first, the same index also serves 'all attempts by this user', but it would not serve a query on exercise alone."*

⭐ That last clause proves you understand the **prefix rule** rather than just having added an index.

#### Easy memory

```
Index = a B-tree → IXSCAN ⚡ instead of COLLSCAN 🐢 (a book's index 📖)
createIndex({ a: 1, b: -1 }) — COMPOUND

⚠️ PREFIX RULE ⭐ {a,b} serves find(a) and find(a,b) — but NOT find(b) 💥
⚠️ Order: Equality → Sort → Range (ESR) ⭐
⚠️ Every index slows WRITES and consumes RAM
```

---

### Q: Types of indexes?

```
SINGLE FIELD   { name: 1 }
COMPOUND ⭐    { role: 1, exp: -1 }        → the prefix rule applies
MULTIKEY       automatic on ARRAY fields ⭐ (one index entry per element)
TEXT           full-text search
GEOSPATIAL     2dsphere — "near me"
HASHED         for sharding
TTL ⭐          auto-DELETE documents after n seconds
PARTIAL        index only documents matching a filter
```

⭐ **TTL is the one worth volunteering** — it does something SQL can't do natively:

```js
db.sessions.createIndex({ createdAt: 1 }, { expireAfterSeconds: 3600 });
// → MongoDB DELETES the document itself, one hour later ⭐
```

Perfect for sessions, OTPs, password-reset tokens and cache entries — no cleanup job needed.

#### Easy memory

```
single | COMPOUND ⭐ | multikey (arrays, automatic) | text | geo | hashed
TTL ⭐ = auto-delete after n seconds → sessions, OTPs, tokens (no cron job!)
partial = index only some documents
```

---

### Q: How do you analyse query performance?

```js
db.employees.find({ name: "Priyanka" }).explain("executionStats");
```

```
What to look for:
   COLLSCAN  ❌ full collection scan → add an index
   IXSCAN    ✅ an index was used ⭐

   totalDocsExamined  vs  nReturned
        examined 1,000,000 → returned 10   = a terrible query 💥
        examined 10        → returned 10   = perfect ⭐
```

⭐ **`totalDocsExamined ≈ nReturned` is the target** — that ratio is the single most useful number, and quoting it sounds like experience.

Also:

```
db.collection.getIndexes()          // what indexes exist
db.setProfilingLevel(1, 100)        // log every query slower than 100 ms ⭐
```

#### Easy memory

```
.explain("executionStats") ⭐
COLLSCAN ❌ → IXSCAN ✅
Target: totalDocsExamined ≈ nReturned ⭐
Profiler: db.setProfilingLevel(1, 100) → log slow queries
```

---

## Aggregation

### Q: What is the aggregation pipeline?

```
A pipeline = documents flowing through STAGES, like a factory line. ⭐
Each stage transforms the output of the previous one.
```

```js
db.orders.aggregate([
  { $match: { status: "completed" } },                    // WHERE  ⭐ put this FIRST
  { $group: { _id: "$customerId", total: { $sum: "$amount" } } },   // GROUP BY
  { $sort:  { total: -1 } },                              // ORDER BY
  { $limit: 10 }
]);
```

```
[all orders]
     ↓ $match     ← filter EARLY so later stages handle less ⭐
[completed only]
     ↓ $group
[one row per customer, with a total]
     ↓ $sort → $limit
[the top 10] ✅
```

```
SQL                          Aggregation
───                          ───────────
WHERE      →                 $match ⭐
GROUP BY   →                 $group
HAVING     →                 $match AFTER $group ⭐
ORDER BY   →                 $sort
LIMIT      →                 $limit
SELECT     →                 $project
JOIN       →                 $lookup
```

⭐ **The performance rule:** `$match` and `$limit` as early as possible, because `$match` can use an index — but only while it's still at the front of the pipeline.

#### The stages to know

```
$match ⭐ $group $project $sort $limit $skip
$unwind ⭐ (one document per array element)
$lookup ⭐ (a left outer join)
$count $addFields $facet
```

⭐ `$unwind` is the one people can't explain:

```js
{ name: "Priya", skills: ["Java", "Angular"] }
      ↓ $unwind: "$skills"
{ name: "Priya", skills: "Java" }
{ name: "Priya", skills: "Angular" }      // ⭐ ONE document became TWO
```

Use it when you need to group or filter *by an array element*.

#### 🔵 In your RoboGebra code — 27 real pipelines ⭐

**File:** `domain/exerciseitem/repository/ExerciseItemRepository.java`

```java
@Aggregation(pipeline = {
        "{ '$match': { 'exercise.$id': { '$in': ?0 } } }",     // ⭐ ?0 = the first parameter
        "{ '$sort':  { 'displayOrder': 1 } }"
})
List<ExerciseItemEntity> findAllByExerciseIds(List<ObjectId> exerciseIds);
```

> 🗣️ **The N+1 story — your best MongoDB answer:** *"Loading a chapter's exercises and then fetching each exercise's items one at a time is one query plus N — the classic N+1, and `@DBRef` makes it easy to fall into. I replaced it with a single `@Aggregation` pipeline that `$match`es on `exercise.$id` with `$in` and sorts by `displayOrder`, so it's one round trip instead of N. It's the same fix as a JPA `JOIN FETCH`, just written as a pipeline."*

⭐ Note `?0` — that's how Spring Data binds method parameters into the pipeline.

#### Easy memory

```
Pipeline = a factory line 🏭 — each STAGE transforms the previous output
$match ⭐ (WHERE) → $group (GROUP BY) → $sort → $limit
$unwind ⭐ = one document PER array element | $lookup = a left join

⚠️ $match FIRST — only then can it use an index ⭐
Your N+1 fix: $match + $in in ONE pipeline instead of N queries ⭐
```

---

## Scaling & Reliability

### Q: What is replication / a replica set?

```
Replica set = several servers holding the SAME data.
              ONE primary (writes) + secondaries (copies). ⭐
```

```
        ┌─────────────┐
        │  PRIMARY    │ ← all WRITES go here ⭐
        └──────┬──────┘
       replicates│
      ┌─────────┴─────────┐
      ▼                   ▼
┌───────────┐       ┌───────────┐
│ SECONDARY │       │ SECONDARY │  ← can serve READS
└───────────┘       └───────────┘

Primary dies 💥 → an ELECTION promotes a secondary → automatic failover ✅ ⭐
```

Real-world idea: a **manager and two deputies.** If the manager is unavailable, the team votes one deputy in and work continues.

⭐ The point that connects back to transactions:

```
Multi-document transactions REQUIRE a replica set ⭐
   → which is why even a single-node dev setup is often run as a
     one-member replica set
```

#### Easy memory

```
Replica set = SAME data, many servers → PRIMARY (writes) + SECONDARIES ⭐
Primary fails → ELECTION → automatic failover ✅
Purpose: HIGH AVAILABILITY (and read scaling)
⭐ Required for multi-document transactions
```

---

### Q: What is sharding?

```
Sharding = SPLITTING the data across servers by a SHARD KEY. ⭐
           Horizontal scaling for size and write throughput.
```

```
                mongos (router) ⭐
                      │
      ┌───────────────┼───────────────┐
      ▼               ▼               ▼
   SHARD 1         SHARD 2         SHARD 3
   users A–H       users I–P       users Q–Z
```

⭐ **Everything depends on the shard key**, and picking it badly is unfixable-in-place:

```
A BAD shard key (e.g. a timestamp)
      → every new write goes to the SAME shard
      → a "hot shard" doing all the work while the others idle 💥

A GOOD shard key → high cardinality + even distribution + matches your queries ⭐
```

#### Replication vs sharding — the one-liner

```
REPLICATION → COPIES the same data   → availability ⭐
SHARDING    → SPLITS different data  → capacity + write throughput ⭐
(production runs BOTH: each shard is itself a replica set)
```

#### Easy memory

```
Replication = COPIES (availability) ⭐ | Sharding = SPLITS (scale) ⭐
mongos routes the queries
⚠️ A bad shard key → a HOT SHARD doing all the work 💥
Production: each shard is itself a replica set
```

---

## Spring Data MongoDB

### Q: How do you use MongoDB with Spring Boot?

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/robogebra
```

```java
@Document(collection = "exercise_solution")     // ⭐ the collection name
@CompoundIndexes({
    @CompoundIndex(name = "exercise_item_id_index", def = "{'exercise_item.$id': 1}")
})
public class ExerciseSolutionEntity {

    @Id
    private String id;                          // ⭐ String, not Long — it's an ObjectId

    @Indexed
    @DBRef                                      // ⭐ a reference to another document
    @Field("exercise_item")                     // ⭐ maps to a snake_case field name
    private ExerciseItemEntity exerciseItem;

    @Field("step_output_json")
    private Object stepOutputJson;
}
```

```
@Document   → class → COLLECTION
@Id         → the _id field (use String)
@Field      → rename the stored field ⭐ (Java camelCase ↔ Mongo snake_case)
@Indexed    → a single-field index
@CompoundIndex → a multi-field index ⭐
@DBRef      → a reference to another document ⚠️ (costs an extra query)
```

⭐ The Java-side difference from JPA worth naming:

```
JPA    → @Entity needs a no-arg constructor, and there IS a persistence context
Mongo  → @Document maps a document, and there is NO persistence context ⭐
         → NO dirty checking → you MUST call save() explicitly ⭐
```

#### Easy memory

```
@Document(collection) | @Id (String) | @Field ⭐ | @Indexed | @CompoundIndex | @DBRef ⚠️

⭐ NO persistence context, NO dirty checking → you MUST call save() ⭐
   (unlike JPA, where a managed entity saves itself — [24](./24-orm-jpa-hibernate.md))
```

---

### Q: What is `MongoRepository`?

```java
@Repository
public interface ExerciseItemRepository extends MongoRepository<ExerciseItemEntity, String> {

    // DERIVED queries — Spring writes them from the METHOD NAME ⭐
    Optional<ExerciseItemEntity> findByExercise_IdAndIndex(String exerciseId, String index);
    List<ExerciseItemEntity> findAllByExerciseId(String exerciseId);
    Boolean existsByExercise_IdAndIndex(String exerciseId, String index);
    void deleteAllByExercise_Id(String exerciseId);
}
```

⭐ Note `findByExercise_Id` — the **underscore** traverses into a nested/referenced object.

```
Exactly the same programming model as JpaRepository ⭐
   → declare an interface, Spring generates the implementation
```

#### 🔵 In your RoboGebra code

```
85 repository interfaces extending MongoRepository ⭐
```

> 🗣️ *"The repository model is identical to Spring Data JPA — that's the point of Spring Data. Which is also why I can move between them: the derived-query syntax, paging and sorting are the same, and what changes is the engine underneath — no persistence context, no lazy loading, no dirty checking."*

⭐ That sentence answers *both* "do you know Mongo?" and "could you pick up JPA?" at once.

#### Easy memory

```
MongoRepository<T, String> → CRUD + derived queries, like JpaRepository ⭐
findByExercise_Id → the UNDERSCORE traverses into a nested object ⭐
Same programming model as JPA; a completely different engine ⭐
```

---

### Q: `MongoRepository` vs `MongoTemplate`?

```
MongoRepository → HIGH level → derived queries → 90% of the work ⭐
MongoTemplate   → LOW level  → dynamic criteria, bulk ops, complex aggregation ⭐
```

```java
// MongoTemplate — when the query is built at RUNTIME ⭐
Query query = new Query(Criteria.where("role").is("Developer").and("exp").gte(4));
List<Employee> list = mongoTemplate.find(query, Employee.class);

// dynamic filters — impossible with a derived method name
if (city != null) query.addCriteria(Criteria.where("city").is(city));
```

⭐ The rule: **a method name can't be dynamic.** The moment filters depend on which parameters the user supplied, you need `MongoTemplate` (or Criteria/`Query` by hand).

#### 🔵 In your RoboGebra code

`MongoTemplate` appears in the persistence services — `ExerciseSolutionPersistenceService`, `LearningTimelineService`, `SolutionPipelinePersistenceService`.

> 🗣️ *"Repositories handle the standard access; the persistence services drop to `MongoTemplate` for the cases a derived method name can't express — dynamic filters, partial updates, and bulk operations."*

#### Easy memory

```
MongoRepository ⭐ → derived queries → the default
MongoTemplate ⭐   → dynamic Criteria, bulk ops, complex aggregation

Rule: a method NAME can't be dynamic → user-driven filters need MongoTemplate ⭐
```

---

## Mongoose (Node)

### Q: What is Mongoose and how does it differ from Spring Data MongoDB?

```
Mongoose = the ODM for Node.
           Same idea as Spring Data MongoDB, in JavaScript. ⭐
```

#### 🔵 In your RoboGebra CRM — real code ⭐

**File:** `robogebra-crm/src/models/user.model.js` (Mongoose 8.8, Express 4.21)

```js
const userSchema = mongoose.Schema({
  name:  { type: String, required: true, trim: true },
  email: {
    type: String, required: true, unique: true, trim: true, lowercase: true,
    validate(value) {                                    // ⭐ SCHEMA-LEVEL validation
      if (!validator.isEmail(value)) throw new Error('Invalid email');
    },
  },
  password: { type: String, required: true, minlength: 6 },
  role: { type: String, enum: roles, default: 'admin' },  // ⭐ enum + default
  isEmailVerified: { type: Boolean, default: false },
});
```

⭐ Three Mongoose features visible in that one file, and each is a talking point:

```
1. SCHEMA VALIDATION in the model — required, unique, minlength, a custom validator ⭐
      → MongoDB itself doesn't enforce this; Mongoose does, in the app layer
2. PLUGINS — `paginate` is imported from ./plugins → reusable schema behaviour ⭐
3. bcryptjs — password hashing, almost certainly in a pre('save') HOOK ⭐
```

```
Mongoose HOOKS (middleware):
   pre('save')  → hash the password before it's written ⭐
   post('save') → send a welcome email
```

Your other CRM models: `institute`, `transaction`, `coupon`, `affiliate`, `payout`, `webhook`, `token`, `feedback`, **`counter`**.

⭐ `counter.model.js` is worth calling out — it's the classic **auto-increment sequence** pattern, because MongoDB has no `AUTO_INCREMENT`:

```
Need a human-readable invoice number (INV-1001)?
   → a `counters` collection + findOneAndUpdate with $inc, atomically ⭐
```

#### Spring Data MongoDB vs Mongoose

| | Spring Data MongoDB | Mongoose |
|---|---|---|
| Language | Java | JavaScript |
| Model | `@Document` class | `mongoose.Schema` |
| Validation | Bean Validation (`@Valid`) | **in the schema** ⭐ |
| Repository | `MongoRepository` interface | `Model.find()` |
| Hooks | lifecycle events | `pre` / `post` middleware ⭐ |
| Population | `@DBRef` | `.populate()` ⭐ |

#### Easy memory

```
Mongoose = the Node ODM ⭐ — schema, validators, plugins, pre/post HOOKS
Your CRM: 10+ schemas, bcrypt password hashing, a paginate plugin
counter.model.js ⭐ = the auto-increment pattern ($inc), since Mongo has none

@DBRef (Spring) ≈ .populate() (Mongoose) ⭐
```

---

## Your Answer Scripts

### 🗣️ "Have you worked with MongoDB?" (30 seconds) ⭐

> *"Yes — it's the database on my current project. RoboGebra's Spring Boot backend uses **Spring Data MongoDB**: around a hundred `@Document` classes, eighty-five repositories, and twenty-seven `@Aggregation` pipelines for the queries derived methods can't express. I've also worked with **Mongoose** on the Node CRM service, so I've used the document model from both Java and JavaScript."*

⭐ Confident, specific, and every number is verifiable.

### 🗣️ "What's the hardest MongoDB problem you've solved?" (45 seconds) ⭐

> *"An N+1 query. We load a chapter's exercises and then each exercise's items — and with `@DBRef`, Spring Data resolves each reference with its own query, so a chapter with fifty exercises meant fifty-one round trips. I replaced it with a single `@Aggregation` pipeline that `$match`es on `exercise.$id` with `$in` and sorts by `displayOrder` — one query instead of N. The lesson I took from it is that the document model doesn't remove the N+1 problem, it just changes what it looks like."*

### 🗣️ "How do you decide embed vs reference?" (30 seconds) ⭐

> *"By the read pattern and whether the data is bounded. Read together and bounded — embed; shared, large or unbounded — reference. In our solution entity we do both: the exercise item is a `@DBRef` because it's shared across solutions and changes independently, while the AI-generated step output is embedded, because it's meaningless outside its solution and always read with it. The hard constraint is the 16 MB document limit, so an unbounded array is never embedded."*

### 🗣️ If they ask about relational databases too

> *"I've worked relationally as well — MySQL and Postgres, and Neo4j on EasyVisa. Having used a document store, a relational store and a graph store makes the trade-offs concrete rather than theoretical: I pick by the access pattern, not by preference."*

---

## Quick Revision Sheet

```
BASICS
  Document 📄 = row | Collection 📁 = table | _id = ObjectId (EMBEDS a timestamp ⭐)
  BSON = JSON + ObjectId/Date/Decimal128 ⭐
  Single-doc writes are ALWAYS atomic; multi-doc transactions need a REPLICA SET ⭐

CRUD
  insertOne | find(filter, projection) | updateOne($set) | deleteOne
  ⚠️ updateOne WITHOUT $set REPLACES the whole document 💥
  $elemMatch ⭐ = same array element | $addToSet ⭐ = push if absent

SCHEMA DESIGN ⭐ (the most important question)
  EMBED     → read together + BOUNDED   → 1 query 📕
  REFERENCE → shared / large / unbounded → 2 queries 🎫
  ⚠️ 16 MB limit → never embed an unbounded array 💥
  ⚠️ @DBRef = an extra query per reference → N+1 ⭐
  Decide PER FIELD (your ExerciseSolutionEntity does both) ⭐
  Model for how you READ ⭐

INDEXES
  COLLSCAN ❌ → IXSCAN ✅ | .explain("executionStats") ⭐
  Target: totalDocsExamined ≈ nReturned ⭐
  ⚠️ PREFIX RULE: {a,b} serves find(a) and find(a,b), NOT find(b) 💥
  ESR: Equality → Sort → Range ⭐
  TTL index ⭐ = auto-delete (sessions, OTPs) — no cron job

AGGREGATION
  $match ⭐ FIRST (so it can use an index) → $group → $sort → $limit
  $unwind ⭐ = one doc per array element | $lookup = left join
  Your 27 pipelines; ?0 binds a method parameter ⭐

SCALING
  Replication = COPIES → availability ⭐ (primary + election)
  Sharding = SPLITS → capacity ⭐ | ⚠️ a bad shard key = a HOT SHARD 💥

SPRING DATA
  @Document | @Id (String) | @Field ⭐ | @Indexed | @CompoundIndex | @DBRef ⚠️
  ⭐ NO persistence context, NO dirty checking → you MUST call save()
  MongoRepository (derived) ⭐ vs MongoTemplate (dynamic Criteria) ⭐

MONGOOSE
  Schema validators + plugins + pre/post HOOKS ⭐ | .populate() ≈ @DBRef
  counter.model.js = the auto-increment pattern ($inc) ⭐

YOUR POSITION ⭐
  "MongoDB is the database on my current project — 104 @Document classes,
   85 repositories, 27 aggregation pipelines, plus Mongoose on the Node CRM."
```

---

**Related files:** [24 — ORM/JPA/Hibernate (ORM vs ODM)](./24-orm-jpa-hibernate.md) · [06 — Spring Boot](./06-spring-boot.md) · [36 — SQL](./36-sql-interview-questions.md) · [39 — RoboGebra Code Examples](./39-robogebra-code-examples.md) · [FLASHCARDS-13-mongodb.md](./FLASHCARDS-13-mongodb.md)
