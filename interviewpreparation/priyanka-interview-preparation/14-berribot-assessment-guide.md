# Berribot Assessment — Master Prep Guide

**Target role:** Senior Full Stack Developer (**5 years**)
**Stack:** Java 17 · Spring Boot · Angular 16 · MongoDB · MySQL · Microservices · AWS S3

This guide maps the assessment's stack to your study files, flags gaps, and gives you a focused plan + key talking points.

> ⚠️ **Status note (3 Sept 2026).** This guide was written for the **Berribot** assessment in July. Two things have changed since, and they change the answers:
>
> ```
> ❌ "MongoDB is a gap"  →  ✅ It is your CURRENT production database ⭐
>       RoboGebra: 104 @Document · 85 repositories · 27 @Aggregation pipelines
>       plus Mongoose on the Node CRM. Stop treating it as a weakness.
>
> ❌ "Java 17 is a gap"  →  ✅ The portal IS Java 17 / Spring Boot 3.2 ⭐
>       26 files use `record`. You ship on it daily.
> ```
>
> **The stack in this guide still maps well to most Java+Angular roles** — keep using it for the full-stack design section. But read the gap table below with the correction above. See **[13 MongoDB](./13-mongodb.md)** and **[39 RoboGebra Code Examples](./39-robogebra-code-examples.md)**.

---

## 1. Stack → Study-file map

| Stack item | Status vs your resume | Study file(s) |
|---|---|---|
| **Java 17** | You list "Java" — brush up version features | [12-java17-features.md](./12-java17-features.md) + [05-java.md](./05-java.md) |
| **Spring Boot** | ✅ On resume | [06-spring-boot.md](./06-spring-boot.md) |
| **Angular 16** | ✅ Strong — you migrated to Angular 16 | [04-angular.md](./04-angular.md) + Angular 16 section below |
| **MongoDB** | ⚠️ **Gap** — not on resume | [13-mongodb.md](./13-mongodb.md) |
| **MySQL** | ✅ On resume | [06-spring-boot.md](./06-spring-boot.md) (JPA) |
| **Microservices** | ⚠️ Basic | [08-microservices-basics.md](./08-microservices-basics.md) |
| **AWS S3** | ⚠️ Basic | [07-aws-basics.md](./07-aws-basics.md) |
| Self-intro / HR | — | [00](./00-self-introduction.md), [11](./11-hr-behavioral.md) |
| Coding round | — | [09-coding-problems.md](./09-coding-problems.md) |
| Projects | ✅ | [10-projects-deep-dive.md](./10-projects-deep-dive.md) |

---

## 2. Honest gap analysis (prioritize these)

1. **MongoDB** — biggest gap. Learn the document model, indexing, aggregation pipeline, and Spring Data MongoDB (`@Document`, `MongoRepository`, `MongoTemplate`). Your **Neo4j** experience is your bridge — you already think NoSQL.
2. **Java 17 features** — you've used Java 8; learn records, sealed classes, pattern matching, switch expressions, text blocks.
3. **Microservices depth** — you have the basics; be ready for service discovery, API gateway, circuit breaker, inter-service communication.
4. **AWS S3 hands-on** — know buckets, the Spring Boot SDK upload/download flow, presigned URLs (example below).

Everything else (Angular, Spring Boot, MySQL/JPA, REST) is your strength — lead with it.

---

## 3. Likely assessment format (typical for this profile)

> Berribot/product-company assessments for a senior full-stack role usually run in these stages — prepare for each:

1. **Online coding test** (HackerRank/Codility style) — DSA + maybe a small Java/JS task. → [09-coding-problems.md](./09-coding-problems.md)
2. **Technical Round 1 (Backend)** — Java 17, Spring Boot, REST, JPA/MongoDB, design of an API.
3. **Technical Round 2 (Frontend)** — Angular 16, RxJS, state management, component design.
4. **System/Full-stack design** — design a feature end-to-end (Angular → Spring Boot REST → MongoDB/MySQL → S3), microservices split, scaling.
5. **Managerial / project deep-dive** — your real projects, decisions, trade-offs. → [10](./10-projects-deep-dive.md)
6. **HR** — fit, expectations, notice period. → [11](./11-hr-behavioral.md)

---

## 4. Angular 16 — version-specific talking points

Since they pin **Angular 16** (which you migrated to), be ready for:
- **Standalone components** — no NgModule needed; `standalone: true`, import dependencies directly. The future direction of Angular.
- **Angular Signals (developer preview in 16)** — a new reactive primitive for fine-grained change detection: `signal()`, `computed()`, `effect()`. Know it exists and the concept (reactivity without zone.js overhead).
- **Required inputs** — `@Input({ required: true })`.
- **Router data as input bindings** (`withComponentInputBinding`).
- **`takeUntilDestroyed()`** and `DestroyRef` — cleaner unsubscription.
- **Self-closing tags**, **esbuild-based dev server** (faster builds), Vite.
- **NgOptimizedImage** for image performance.

> Talking point: *"I led/contributed to migrating four repos to Angular 16. I dealt with breaking changes, RxJS upgrades, Ivy, and deprecated APIs, and I'm familiar with the new standalone-component direction and the signals preview."*

---

## 5. Full-stack scenario you should be able to whiteboard

**"Design a file-upload + document feature"** (ties your EasyVisa document-portal experience to their stack):

```
[ Angular 16 client ]
   - reactive form, file picker
   - calls REST API; shows progress
        |
        v  (HTTP, JWT auth)
[ Spring Boot service (Java 17) ]
   - @RestController, @Valid DTOs (records)
   - validates, stores metadata
   - uploads file to S3 (presigned URL or SDK)
        |                         |
        v                         v
[ MongoDB ]                  [ AWS S3 ]
  document metadata           actual file bytes
  (filename, type, owner,     (key = userId/uuid)
   s3Key, uploadedAt)
```
Key points to mention: store **metadata in MongoDB/MySQL**, **binary in S3** (never blobs in the DB); use **presigned URLs** so the client uploads/downloads directly from S3; secure with **JWT**; handle large files with multipart; add validation and a global exception handler.

### S3 upload from Spring Boot (be ready to discuss)
```java
@Service
public class S3Service {
    private final S3Client s3;   // AWS SDK v2

    public String upload(MultipartFile file, String key) throws IOException {
        s3.putObject(
            PutObjectRequest.builder().bucket("berribot-docs").key(key).build(),
            RequestBody.fromBytes(file.getBytes())
        );
        return key;
    }

    // Presigned URL so the client downloads directly from S3
    public String presignedDownloadUrl(String key) {
        S3Presigner presigner = S3Presigner.create();
        GetObjectPresignRequest req = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(15))
            .getObjectRequest(b -> b.bucket("berribot-docs").key(key))
            .build();
        return presigner.presignGetObject(req).url().toString();
    }
}
```

---

## 6. MongoDB + MySQL together (polyglot persistence)

They list **both** — expect "when do you use which?"
- **MySQL:** transactional, relational, strong consistency — users, payments, orders, structured relationships.
- **MongoDB:** flexible/nested data, high write volume, evolving schema — activity logs, content, catalogs, dashboards, embedded sub-documents.
- A single Spring Boot app can use **both** (Spring Data JPA + Spring Data MongoDB), each for the right data.

---

## 7. Focused 5-day plan for Berribot

| Day | Focus |
|---|---|
| 1 | **MongoDB** ([13](./13-mongodb.md)) — model, queries, aggregation, Spring Data. This is your gap. |
| 2 | **Java 17** ([12](./12-java17-features.md)) + revise Core Java ([05](./05-java.md)) |
| 3 | **Spring Boot + REST + JPA** ([06](./06-spring-boot.md)) + the S3/full-stack scenario above |
| 4 | **Angular 16** ([04](./04-angular.md)) + the Angular 16 section above |
| 5 | **Microservices** ([08](./08-microservices-basics.md)) + **coding practice** ([09](./09-coding-problems.md)) + **self-intro & projects** ([00](./00-self-introduction.md), [10](./10-projects-deep-dive.md)) |

---

## 8. Senior-level talking points (5-year framing)

At "senior," they expect **ownership and judgment**, not just coding:
- Talk about **design decisions and trade-offs** (why MongoDB vs MySQL, why a service split).
- Mention **mentoring, code reviews, and architecture** input (you "assisted in refining architecture" on Subsea).
- Show **end-to-end ownership** — API → DB → UI → deployment.
- Discuss **non-functionals**: performance, security (JWT), scalability, monitoring.
- Quantify impact where you can (retention from Study Reminders, migration of 4 repos, etc.).

---

## 9. Pre-interview checklist (Berribot edition)

- [ ] Can explain **MongoDB vs MySQL** and when to use each
- [ ] Can write a basic **aggregation pipeline** and a `MongoRepository` interface
- [ ] Can name **5 Java 17 features** and where you'd use them
- [ ] Can describe **Angular 16 standalone components & signals** at a high level
- [ ] Can whiteboard the **upload-to-S3 + metadata-in-DB** flow with presigned URLs
- [ ] Can split a feature into **microservices** and justify the boundaries
- [ ] Self-intro polished; one strong **STAR story** per project ready
- [ ] Honest about MongoDB/Java 17 depth — but concepts solid

> Good luck with the Berribot assessment, Priyanka! Lead with your Angular + Spring Boot strength, bridge MongoDB through your Neo4j NoSQL mindset, and show senior-level ownership.
