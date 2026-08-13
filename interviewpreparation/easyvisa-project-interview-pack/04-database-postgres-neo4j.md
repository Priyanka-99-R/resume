# 4. Databases (PostgreSQL + Neo4j) — Interview Q&A tied to EasyVisa

**Honest framing:** I was frontend-focused. I understood the **data architecture** and consumed data that came from both stores through the Grails REST APIs. I did not write Cypher or tune Postgres day-to-day — so my answers explain *why we had two databases, what each was for, and how the shapes showed up in the UI I built*. Knowing the "why" of a polyglot-persistence design is exactly the system-understanding interviewers want.

**Stack:** **PostgreSQL** for relational/transactional data (via GORM/Hibernate) + **Neo4j** graph database for the relationships between applicants, petitioners, and cases.

---

## Q1. Why two databases? (the whole point — nail this)

**Answer.** This is **polyglot persistence** — use the right store for the shape of the data:

- **PostgreSQL** held the **core relational, transactional data**: case records, applicant details, document metadata, statuses. This data needs **ACID transactions, integrity constraints, and reliable updates** — a relational DB is exactly right for it.
- **Neo4j** (a **graph database**) modeled the **relationships**: applicants ↔ petitioners ↔ cases ↔ employers/family links. Visa cases are fundamentally a **graph of connected entities**, and questions like *"who is connected to this case, and how, several hops out?"* are **native in a graph** and **painful with deep relational joins**.

**The one-liner:** *Postgres for the records and transactions; Neo4j for the web of relationships. We stored each kind of data where the queries were natural.*

---

## Q2. What is a graph database? Nodes and relationships?

**Answer.** A graph DB stores data as **nodes** (entities) and **relationships/edges** (the connections between them), where **relationships are first-class citizens** — they're stored directly, with their own type and properties, not derived at query time from foreign keys.

- **Node** — an entity, e.g. an `Applicant`, `Petitioner`, or `Case`, with properties (name, id).
- **Relationship** — a typed, directed edge, e.g. `(:Applicant)-[:APPLIES_FOR]->(:Case)` or `(:Petitioner)-[:SPONSORS]->(:Applicant)`, and edges can have properties too (e.g. a `since` date).

Because connections are stored as real edges, **traversing** them ("follow this applicant's links") is a pointer hop, not a join — so multi-hop relationship queries stay fast even as depth grows.

**In EasyVisa.** The nodes were applicants, petitioners, cases (and related parties like employers/family), and the edges captured how they connected. That's the natural shape of immigration data — a case isn't one row, it's a small network of people and roles.

---

## Q3. Show me some Cypher. What does a query look like?

**Answer.** Cypher is Neo4j's query language, and it's **visual** — you draw the pattern in ASCII art: `(node)-[:REL]->(node)`.

Find everyone connected to a given case:

```cypher
MATCH (c:Case { caseNumber: 'EV-2023-001' })<-[r]-(person)
RETURN person, type(r)
```

Traverse further — who is connected to this case's applicants, two hops out (family, petitioners, employers):

```cypher
MATCH (c:Case { caseNumber: 'EV-2023-001' })<-[:APPLIES_FOR]-(a:Applicant)
MATCH (a)-[rel]-(related)
RETURN a, type(rel), related
```

Find shared petitioners across cases (a fraud/relationship-insight style query):

```cypher
MATCH (p:Petitioner)-[:SPONSORS]->(:Applicant)-[:APPLIES_FOR]->(c:Case)
RETURN p, collect(c.caseNumber) AS cases
```

**In EasyVisa (honest).** Cypher lived on the Grails backend — I didn't author it day-to-day. But I understood what these traversals returned because I **rendered them**: the "related parties on a case" views I built in Angular were backed by Neo4j traversals on the server. So I knew the shape of the graph data coming down and modeled my UI around it.

---

## Q4. Why is "who's connected to this case" painful in SQL but natural in a graph?

**Answer.** In a relational model, relationships are **implicit** — encoded as foreign keys — so answering "everyone connected to this case, N hops out" means **repeated JOINs** (often self-joins through junction tables), one JOIN per hop. As the depth grows, the SQL gets complex and slow, and **variable-depth** ("as far as the connections go") is genuinely hard to express in standard SQL.

In a graph, relationships are **explicit edges**, so the same question is a **traversal**: start at the case node and follow edges. Variable depth is trivial (`-[:REL*1..3]-`), and performance depends on how many nodes you actually touch, not on the total table size.

```sql
-- Relational: 2 hops already needs multiple joins through junction tables
SELECT p.*
FROM cases c
JOIN case_applicants ca ON ca.case_id = c.id
JOIN applicants a       ON a.id = ca.applicant_id
JOIN applicant_petitioner ap ON ap.applicant_id = a.id
JOIN petitioners p      ON p.id = ap.petitioner_id
WHERE c.case_number = 'EV-2023-001';
```

```cypher
-- Graph: the same idea, plus arbitrary depth for free
MATCH (c:Case { caseNumber: 'EV-2023-001' })-[*1..3]-(connected)
RETURN DISTINCT connected
```

**In EasyVisa.** The "related parties" and connection views were the reason Neo4j existed. Expressing them as ever-deeper joins would have been slow and unmaintainable; as graph traversals they were natural. That's the concrete justification for the two-database design.

---

## Q5. When would you choose a graph DB vs a relational DB?

**Answer.**
- **Relational (Postgres):** structured, tabular data; strong transactional integrity; aggregations and reporting; data where relationships are shallow and known. → **case records, statuses, document metadata, anything needing ACID.**
- **Graph (Neo4j):** data where the **relationships are the value** and you traverse them deeply or at variable depth — social networks, fraud rings, recommendations, and exactly this: **networks of people connected to cases.**

The mature answer is **polyglot persistence**: it's not either/or. EasyVisa used **both**, storing each kind of data where its queries were natural — transactional records in Postgres, the relationship web in Neo4j.

**In EasyVisa.** If I were asked to defend it: putting relationships in Neo4j didn't mean abandoning Postgres — the authoritative transactional records stayed relational for integrity, and Neo4j gave us fast, expressive relationship queries. Right tool per job.

---

## Q6. How did the two stores stay consistent, and how did you consume them?

**Answer (honest).** The **backend owned the coordination** — the Grails services wrote to Postgres for the record data and maintained the corresponding graph in Neo4j so relationship queries reflected the current records. From the **frontend**, I didn't see two databases; I saw **REST endpoints**: case/applicant/document data (Postgres-backed) and "related parties / connections" data (Neo4j-backed) came through the same API surface as JSON.

**In EasyVisa.** My job was to know **which data came from where** so I coordinated the right contracts with the backend team — record-shaped data (forms, statuses, documents) versus relationship-shaped data (the connection graph views). I consumed both as typed Observables and rendered them; the dual-write consistency was a server-side concern I understood but didn't own.

---

### Rapid-fire recap

| Question | Answer |
|----------|--------|
| Why two DBs | polyglot persistence — Postgres for records/transactions, Neo4j for relationships |
| Postgres role | ACID transactional data: cases, applicants, documents, statuses |
| Neo4j role | the graph of applicants ↔ petitioners ↔ cases ↔ related parties |
| Graph DB basics | nodes + first-class relationship edges; traversal not joins |
| Cypher | `MATCH (a)-[:REL]->(b) RETURN ...` — pattern matching in ASCII art |
| SQL pain | N hops = N joins; variable depth is hard; graph traversal is native |
| Graph vs relational | relationships-are-the-value & deep traversal → graph; integrity/tabular → relational |
| My honest role | frontend; understood the architecture, consumed both via REST, knew which data came from where |
