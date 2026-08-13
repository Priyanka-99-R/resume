# MongoDB — Interview Q&A

> **⚠️ Gap alert:** MongoDB is on the **Berribot stack** but **not on your current resume** (you list MySQL, Postgres, Neo4j). Expect questions. This file gives you solid fundamentals + Spring Data MongoDB so you can speak confidently. Be honest about hands-on depth, but know the concepts cold.

---

## Fundamentals

### Q: What is MongoDB?
A **NoSQL, document-oriented database**. Instead of tables and rows, it stores data as flexible **JSON-like documents** (BSON) inside **collections**. It's schema-less, horizontally scalable, and great for rapidly evolving data and large volumes.

### Q: What is a document and a collection?
- **Document:** a record stored as a BSON (Binary JSON) object — key-value pairs, can be nested. The MongoDB equivalent of a "row."
- **Collection:** a group of documents — the equivalent of a "table" (but with no fixed schema).
```json
// A document in the "employees" collection
{
  "_id": ObjectId("64f..."),
  "name": "Priyanka",
  "role": "Full Stack Developer",
  "skills": ["Angular", "Java", "Spring Boot"],
  "address": { "city": "Chennai", "country": "India" }
}
```

### Q: What is BSON?
**Binary JSON** — MongoDB's storage format. It extends JSON with extra types (ObjectId, Date, binary, int/long/decimal) and is optimized for speed and space.

### Q: What is `_id`?
A unique primary key automatically added to every document. By default it's a 12-byte **ObjectId** (containing a timestamp), but you can set your own value.

### Q: SQL vs NoSQL (MySQL vs MongoDB) — the key interview question
| | MySQL (SQL/Relational) | MongoDB (NoSQL/Document) |
|---|---|---|
| Data model | Tables, rows, columns | Collections, documents |
| Schema | Fixed, predefined | Flexible, dynamic |
| Relationships | Joins, foreign keys | Embedding or referencing |
| Scaling | Vertical (mostly) | Horizontal (sharding) |
| Transactions | Strong ACID | ACID (multi-doc since v4.0) |
| Query language | SQL | MongoDB Query Language (JSON) |
| Best for | Structured, relational data | Flexible, large-scale, evolving data |

### Q: When would you choose MongoDB over MySQL?
- **MongoDB:** flexible/changing schema, hierarchical/nested data, high write throughput, horizontal scale, content management, real-time analytics, catalogs.
- **MySQL:** strong relational integrity, complex joins, financial/transactional consistency, well-defined stable schema.
- *Many systems use both (polyglot persistence) — e.g., MySQL for orders, MongoDB for product catalog/logs.*

### Q: What is ACID vs BASE?
- **ACID** (relational): Atomicity, Consistency, Isolation, Durability — strong guarantees.
- **BASE** (many NoSQL): Basically Available, Soft state, Eventual consistency — favors availability/scale over immediate consistency.
- MongoDB is ACID at the **single-document** level always, and supports **multi-document ACID transactions** since v4.0.

---

## CRUD Operations

### Q: Basic CRUD in MongoDB shell?
```js
// Create
db.employees.insertOne({ name: "Priyanka", role: "Developer", exp: 4 });
db.employees.insertMany([{ name: "A" }, { name: "B" }]);

// Read
db.employees.find({ role: "Developer" });
db.employees.findOne({ name: "Priyanka" });
db.employees.find({ exp: { $gte: 4 } });        // experience >= 4

// Update
db.employees.updateOne({ name: "Priyanka" }, { $set: { exp: 5 } });
db.employees.updateMany({ role: "Developer" }, { $inc: { exp: 1 } });

// Delete
db.employees.deleteOne({ name: "B" });
db.employees.deleteMany({ exp: { $lt: 2 } });
```

### Q: Common query operators?
- Comparison: `$eq`, `$ne`, `$gt`, `$gte`, `$lt`, `$lte`, `$in`, `$nin`
- Logical: `$and`, `$or`, `$not`, `$nor`
- Element: `$exists`, `$type`
- Array: `$all`, `$elemMatch`, `$size`
- Update: `$set`, `$unset`, `$inc`, `$push`, `$pull`, `$addToSet`

---

## Schema Design

### Q: Embedding vs Referencing — how do you model relationships?
- **Embedding** (nested documents): store related data inside the parent. Best for **one-to-few**, data read together, that doesn't change independently.
```json
{ "name": "Order1", "items": [ {"product":"Pen","qty":2}, {"product":"Book","qty":1} ] }
```
- **Referencing** (store the `_id` of another document): best for **one-to-many / many-to-many**, large or independently-changing data. Requires a second lookup (or `$lookup`).
```json
{ "name": "Order1", "customerId": ObjectId("...") }
```

### Q: What's the rule of thumb for embedding vs referencing?
- **Embed** when data is accessed together and bounded in size ("contains" relationship).
- **Reference** when data is large, shared, or grows unbounded, or needs to be queried on its own.
- Watch the **16 MB document size limit** — don't embed unbounded arrays.

### Q: Does schema-less mean no design?
No — you still design your documents intentionally around **query patterns** ("model for how you read"). Schema flexibility helps evolution, but a bad data model still causes problems.

---

## Indexing & Performance

### Q: What are indexes in MongoDB?
Data structures (B-trees) that speed up queries by avoiding full collection scans. Without an index, MongoDB scans every document.
```js
db.employees.createIndex({ name: 1 });          // ascending index
db.employees.createIndex({ role: 1, exp: -1 }); // compound index
```

### Q: Types of indexes?
Single-field, **compound** (multiple fields), **multikey** (on array fields), **text** (full-text search), **geospatial**, **hashed** (for sharding), and **TTL** (auto-expire documents after a time).

### Q: How do you analyze query performance?
Use `explain()`:
```js
db.employees.find({ name: "Priyanka" }).explain("executionStats");
```
Look for `COLLSCAN` (bad — full scan) vs `IXSCAN` (good — index used).

---

## Aggregation

### Q: What is the aggregation pipeline?
A framework to process and transform documents through **stages** (like a Unix pipe). Used for grouping, filtering, computing — the equivalent of SQL `GROUP BY` + functions.
```js
db.orders.aggregate([
  { $match: { status: "completed" } },          // filter (WHERE)
  { $group: { _id: "$customerId",               // group by
              total: { $sum: "$amount" } } },
  { $sort: { total: -1 } },                      // ORDER BY
  { $limit: 10 }
]);
```

### Q: Common aggregation stages?
`$match`, `$group`, `$project`, `$sort`, `$limit`, `$skip`, `$unwind` (flatten arrays), `$lookup` (join with another collection), `$count`, `$addFields`.

---

## Scaling & Reliability

### Q: What is replication / a replica set?
A **replica set** is a group of MongoDB servers with the same data: one **primary** (handles writes) and multiple **secondaries** (copies). If the primary fails, an automatic **election** promotes a secondary — providing **high availability** and redundancy.

### Q: What is sharding?
**Horizontal scaling** — splitting data across multiple servers (shards) based on a **shard key**. Each shard holds a subset of the data, allowing the database to handle huge datasets and high throughput. A `mongos` router directs queries to the right shard.

### Q: Replication vs Sharding?
- **Replication:** copies of the **same** data → availability & read scaling.
- **Sharding:** **splits** data across servers → write scaling & large datasets.

### Q: Do MongoDB transactions exist?
Yes — **multi-document ACID transactions** since v4.0 (on replica sets). Single-document operations are always atomic. Use transactions sparingly (they add overhead); good schema design often avoids needing them.

---

## Spring Data MongoDB (most relevant for Berribot)

### Q: How do you use MongoDB with Spring Boot?
Add `spring-boot-starter-data-mongodb`, configure the URI, and use `@Document` entities with `MongoRepository`.
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/berribot
```
```java
@Document(collection = "employees")
public class Employee {
    @Id
    private String id;
    private String name;
    private String role;
    private List<String> skills;
    // getters/setters
}
```

### Q: What is MongoRepository?
A Spring Data interface giving you CRUD + derived query methods out of the box (like `JpaRepository` but for Mongo):
```java
public interface EmployeeRepository extends MongoRepository<Employee, String> {
    List<Employee> findByRole(String role);
    List<Employee> findBySkillsContaining(String skill);
    List<Employee> findByExpGreaterThan(int exp);
}
```

### Q: MongoRepository vs MongoTemplate?
- **MongoRepository:** high-level, derived query methods — quick and clean for standard CRUD.
- **MongoTemplate:** lower-level, fine-grained control — complex queries, aggregations, bulk ops, dynamic criteria.
```java
Query query = new Query(Criteria.where("role").is("Developer").and("exp").gte(4));
List<Employee> list = mongoTemplate.find(query, Employee.class);
```

### Q: How do you run aggregations in Spring Data?
Via `MongoTemplate` with the `Aggregation` builder:
```java
Aggregation agg = Aggregation.newAggregation(
    Aggregation.match(Criteria.where("status").is("completed")),
    Aggregation.group("customerId").sum("amount").as("total")
);
AggregationResults<Document> results =
    mongoTemplate.aggregate(agg, "orders", Document.class);
```

### Q: How would you map a MySQL-backed feature to MongoDB?
Identify the read pattern. If data is hierarchical and read together (e.g., a learning dashboard with nested progress), **embed** it in one document for a single fast read instead of multiple SQL joins. If entities are shared and large, **reference** them.

---

## Honest Framing for the Interview

> "My production database experience is with **MySQL, Postgres, and Neo4j**. I understand MongoDB's document model, indexing, the aggregation pipeline, and replica sets/sharding, and I've worked with Spring Data MongoDB concepts — `@Document`, `MongoRepository`, and `MongoTemplate`. I'm confident I can be productive with it quickly, and I already think in terms of NoSQL/graph modeling from my Neo4j work."

**Tip:** Your **Neo4j** experience is a genuine advantage here — it shows you already think beyond relational tables, which is exactly the mindset MongoDB needs.
