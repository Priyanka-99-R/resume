# AWS (Basics) — Interview Q&A (Easy Version)

> ## ✅ **POSITIONING — read this first**
>
> The old version of this file treated AWS as theory. **It isn't — RoboGebra runs on it.** Verified in your code:
>
> ```
> robogebra-portal/src/main/java/com/robogebra/cms/
>    config/AwsConfig.java          → S3 client wiring
>    config/CognitoConfig.java      → AWS COGNITO authentication ⭐
>    common/properties/S3Properties.java, AwsProperties.java,
>                     AwsUserCognitoProperties.java,
>                     AwsInstituteCognitoProperties.java   ← THREE user pools ⭐
>    common/model/S3UploadResult.java
>
> Real APIs in use:  PutObjectRequest · GeneratePresignedUrlRequest ⭐
>                    AWSCognitoIdentityProvider · AuthenticationResultType
> pom.xml:           aws-java-sdk-s3 · aws-java-sdk-cognitoidp · lambda
> docker/:           LOCALSTACK scripts ⭐ (AWS emulated locally for dev)
> jenkins/:          Jenkinsfile-CI + Jenkinsfile-CD ⭐
> ```
>
> **Say "I use S3 and Cognito in production", not "I know AWS basics".** ⭐

Every question follows the same shape — **Easiest way to remember → simple explanation → real-world example → Easy memory box** — with 🔵 blocks where it's in your code.

---

## 🧠 The easiest way to remember AWS

Think of it as **renting a building instead of constructing one**.

```
EC2     → the COMPUTER you rent          🖥️  (a rented office)
S3      → the FILING CABINET             📦  (infinite storage)
RDS     → the managed DATABASE           🗄️  (someone else does backups)
Lambda  → a worker you pay PER TASK      ⚡  (no office at all)
IAM     → the SECURITY DESK              🔐  (who may open which door)
CloudWatch → the CCTV + alarm system     📊
Cognito → the RECEPTION that checks IDs  🪪 ⭐ (what YOU use)
```

```
The one line that ties it together:
   EC2 = you manage the server ⭐
   Lambda = you manage NOTHING but the code ⭐
   Everything in between is "how much do you want to manage?"
```

#### Easy memory

```
EC2 🖥️ compute | S3 📦 storage | RDS 🗄️ database | Lambda ⚡ functions
IAM 🔐 permissions | CloudWatch 📊 monitoring | Cognito 🪪 auth ⭐

"Rent, don't build." Managed ↔ control is the whole trade-off ⭐
```

---

## Cloud & AWS Fundamentals

### Q: What is cloud computing?
On-demand delivery of computing resources (servers, storage, databases, networking) over the internet, with pay-as-you-go pricing. Instead of buying and maintaining physical hardware, you rent resources from a provider like AWS and scale up or down as needed.

### Q: What are the benefits of cloud computing?
- **No upfront cost** — pay only for what you use.
- **Elasticity / scalability** — scale resources up or down on demand.
- **High availability & reliability** — data centers across the globe.
- **Speed & agility** — provision resources in minutes.
- **Managed services** — AWS handles patching, backups, hardware.

### Q: What is IaaS, PaaS, and SaaS?
- **IaaS (Infrastructure as a Service):** you manage OS, runtime, app — provider manages hardware. *Example: EC2.*
- **PaaS (Platform as a Service):** provider manages OS & runtime, you deploy code. *Example: Elastic Beanstalk, AWS Lambda (close to it).*
- **SaaS (Software as a Service):** fully managed software you just use. *Example: Gmail, Salesforce.*

### Q: What is AWS?
Amazon Web Services — the world's most widely used cloud platform, offering 200+ services for compute, storage, databases, networking, ML, and more, on a pay-as-you-go model.

### Q: What is a Region and an Availability Zone (AZ)?
- **Region:** a geographic area (e.g., `ap-south-1` Mumbai) containing multiple data centers.
- **Availability Zone:** one or more isolated data centers within a Region, with independent power/networking.
- You deploy across **multiple AZs** for high availability — if one AZ fails, the others keep running.

### Q: What is the AWS Free Tier?
A set of services free for 12 months or always-free within limits — e.g., 750 hours/month of a `t2.micro` EC2 instance, 5 GB S3 storage, and 1M Lambda requests/month. Great for learning and small projects.

### Q: What is the AWS Shared Responsibility Model?
- **AWS is responsible for security *of* the cloud** — physical hardware, networking, data centers.
- **You are responsible for security *in* the cloud** — your data, access management (IAM), OS patching (on EC2), encryption, and app security.

---

## IAM (Identity & Access Management)

### Q: What is IAM?
A service to securely control **who** (authentication) can do **what** (authorization) in your AWS account. It manages users, groups, roles, and permission policies — and it's free.

### Q: What are IAM users, roles, and policies?
- **User:** a person or app with long-term credentials.
- **Group:** a collection of users sharing permissions.
- **Role:** temporary credentials assumed by a user, service, or app (e.g., an EC2 instance assumes a role to access S3 — no hardcoded keys).
- **Policy:** a JSON document defining allowed/denied actions on resources.

### Q: What is the principle of least privilege?
Grant only the **minimum permissions** needed to do a task — nothing more. It limits the blast radius if credentials are compromised.

---

### 🔵 In your RoboGebra code — Cognito is your identity provider ⭐

**Files:** `config/CognitoConfig.java` · `AwsUserCognitoProperties` · `AwsInstituteCognitoProperties` · `AwsEmailUserCognitoProperties`

```
THREE separate Cognito user pools ⭐
   users · institutes · email-based users
```

```java
AWSCognitoIdentityProvider          // the client
AuthenticationResultType            // the tokens Cognito returns
UserNotFoundException, LimitExceededException   // ⭐ real error handling
```

> 🗣️ *"Authentication runs through **AWS Cognito** rather than a hand-rolled user table. Cognito issues the JWT, and our API validates it against Cognito's **JWKS** endpoint using `jwks-rsa` — so the service verifies tokens without ever holding a signing secret. We run three separate user pools, because institute accounts and individual users have different attributes and policies."*

⭐ Two strong points in there: **JWKS validation instead of a shared secret**, and **separate pools for separate identity domains**.

---

### 🔵 LocalStack — the detail that makes you sound like you've actually shipped on AWS ⭐

**Files:** `docker/localstack-create-pool.sh` · `localstack-delete-user.sh` · `docker-compose.dev.yml`

```
LocalStack = AWS, emulated on your own machine 🐳 ⭐
   → S3 buckets and Cognito pools exist LOCALLY
   → developers don't need real AWS credentials
   → no shared dev account for people to break
   → tests run offline, and cost nothing ⭐
```

> 🗣️ *"For local development we run **LocalStack** in Docker, so S3 and Cognito are emulated on the developer's machine. There are scripts to create and tear down the Cognito pool, which means a new developer gets a working environment without real AWS credentials, and nobody is sharing a dev account they can break."*

⭐ Almost nobody mentions LocalStack. It signals real day-to-day AWS work rather than tutorial knowledge.

---

### 🔵 CI/CD — Jenkins ⭐

```
jenkins/Jenkinsfile-CI   → build + test on every push
jenkins/Jenkinsfile-CD   → deploy
docker/                  → containerised, so the runtime is identical everywhere
```

> 🗣️ *"CI and CD are Jenkins pipelines, with the app containerised via Docker — so what runs in dev is the same image that runs deployed. That's also why LocalStack matters: the container needs an S3 and a Cognito to talk to, whether it's on my laptop or in the cloud."*

---

## EC2 (Elastic Compute Cloud)

### Q: What is EC2?
A service providing **resizable virtual servers (instances)** in the cloud. You choose the OS, CPU, RAM, and storage, and you fully control the instance — ideal for hosting applications like a Spring Boot backend.

### Q: What is an AMI?
**Amazon Machine Image** — a template containing the OS, software, and configuration used to launch an EC2 instance. You can use AWS-provided AMIs or create your own.

### Q: What are EC2 instance types?
Families optimized for different workloads:
- **General purpose** (t3, m5) — balanced, for web/app servers.
- **Compute optimized** (c5) — CPU-heavy work.
- **Memory optimized** (r5) — in-memory databases, caching.
- **Storage optimized** (i3) — high disk I/O.

### Q: What is a Security Group?
A **virtual firewall** at the instance level controlling inbound and outbound traffic by port, protocol, and IP. It's **stateful** — if inbound is allowed, the response is automatically allowed out. *Example: allow port 22 (SSH) from your IP and 8080 for your app.*

### Q: Security Group vs NACL?
- **Security Group:** instance-level, **stateful**, allow-rules only.
- **NACL (Network ACL):** subnet-level, **stateless**, supports allow *and* deny rules.

### Q: What is a key pair?
A public/private key used to securely SSH into a Linux EC2 instance. AWS stores the public key; you keep the private `.pem` file.

### Q: What is an Elastic IP?
A **static, public IPv4 address** you can attach to an instance. Unlike the default public IP (which changes on stop/start), an Elastic IP stays the same.

### Q: When would you use EC2 vs Lambda?
- **EC2:** long-running apps, full control over the server, predictable/steady load. *Example: a Spring Boot API running 24/7.*
- **Lambda:** short, event-driven tasks, no server management, spiky/occasional workloads. *Example: resizing an image when uploaded to S3.*

---

## S3 (Simple Storage Service)

### Q: What is S3?
**Object storage** for any amount of data — files (objects) stored in **buckets**. Highly durable (99.999999999% — "11 nines"), scalable, and accessed via HTTP/REST APIs. Common for images, backups, logs, and static websites.

### Q: What are buckets and objects?
- **Bucket:** a globally-unique-named container for storing objects.
- **Object:** the actual file plus metadata, identified by a **key** (its name/path).

### Q: What are S3 storage classes?
- **S3 Standard** — frequent access.
- **S3 Standard-IA / One Zone-IA** — infrequent access, cheaper.
- **S3 Glacier / Glacier Deep Archive** — archival, lowest cost, slower retrieval.
- **S3 Intelligent-Tiering** — auto-moves data between tiers based on usage.

### Q: Can S3 host a website?
Yes — **static website hosting** serves HTML/CSS/JS directly from a bucket. *This is exactly how you'd deploy an Angular build* — upload the `dist/` folder to S3, often fronted by CloudFront (CDN) for speed and HTTPS.

### Q: What is S3 versioning?
Keeps **multiple versions** of an object so you can recover from accidental deletes/overwrites. Once enabled, deleting an object just adds a "delete marker."

### 🔵 In your RoboGebra code — S3, for real ⭐

**Files:** `config/AwsConfig.java` · `common/properties/S3Properties.java` · `common/model/S3UploadResult.java`

```java
// the two S3 APIs actually in your codebase:
PutObjectRequest              // uploading files
GeneratePresignedUrlRequest   // ⭐ time-limited download links
```

> 🗣️ *"We use S3 for learning assets and user uploads. Files are uploaded with `PutObjectRequest` from the Spring service, and downloads go through **presigned URLs** rather than making the bucket public or proxying the bytes through our API. That means the client fetches straight from S3 with a URL that expires, so the bucket stays private and our server never becomes a file-serving bottleneck."*

⭐ **That is the single best S3 answer** — it shows you understand *why* presigned URLs exist, not just that they do.

### Q: What is a presigned URL?
A time-limited URL that grants temporary access to a private S3 object without making the bucket public — useful for secure downloads/uploads.

### Q: S3 vs EBS vs EFS?
- **S3:** object storage, accessed over HTTP, unlimited scale.
- **EBS (Elastic Block Store):** a virtual hard disk attached to **one** EC2 instance.
- **EFS (Elastic File System):** a shared file system mountable by **many** EC2 instances.

---

## Lambda (Serverless)

### Q: What is AWS Lambda?
A **serverless compute** service that runs your code in response to events — no servers to provision or manage. You upload a function, AWS runs and scales it automatically, and you pay only for execution time.

### Q: How does Lambda work / what triggers it?
An **event source** invokes the function — e.g., an S3 upload, an API Gateway HTTP request, a DynamoDB change, or a scheduled CloudWatch event. Lambda spins up, runs your handler, and shuts down.

### Q: What is a cold start?
The delay when Lambda initializes a new execution environment for the first request (or after idle). Subsequent "warm" invocations are faster. Choosing a lighter runtime and smaller package reduces it.

### Q: What are typical Lambda use cases?
Image/file processing on upload, lightweight REST APIs (with API Gateway), scheduled jobs, real-time stream processing, and backend automation.

### Q: What are Lambda's limitations?
- Max execution timeout (15 minutes).
- Limited memory/temp storage (configurable, with caps).
- Stateless — no persistent local state between invocations.
- Cold starts for latency-sensitive apps.

---

## RDS (Relational Database Service)

### Q: What is RDS?
A **managed relational database** service. AWS handles provisioning, patching, backups, and scaling, so you focus on your data. Supports **MySQL, PostgreSQL** (both in your stack), MariaDB, Oracle, SQL Server, and Amazon Aurora.

### Q: RDS vs running a database on EC2?
- **RDS:** AWS manages backups, patching, failover, monitoring — less operational work.
- **DB on EC2:** full control and customization, but **you** manage everything (backups, patching, HA). Use only when you need special configs RDS doesn't support.

### Q: What is Multi-AZ in RDS?
A **standby replica in another AZ** for **high availability**. AWS automatically fails over to the standby if the primary fails. It's for **resilience, not read scaling**.

### Q: What is a Read Replica?
A **read-only copy** of the database used to **scale read traffic** (e.g., reporting/dashboards). It's for **performance**, not automatic failover.

### Q: Multi-AZ vs Read Replica — what's the difference?
- **Multi-AZ:** disaster recovery / high availability (automatic failover, synchronous).
- **Read Replica:** scaling reads (asynchronous, can serve read queries).

### Q: What about RDS backups?
RDS supports **automated daily backups** (with point-in-time recovery) and **manual snapshots** you trigger yourself.

---

## CloudWatch

### Q: What is CloudWatch?
AWS's **monitoring and observability** service. It collects **metrics, logs, and events** from AWS resources and your apps, and lets you set **alarms** and dashboards.

### Q: Metrics vs Logs vs Alarms?
- **Metrics:** numeric data over time (CPU %, memory, request count).
- **Logs:** text output from apps/services (e.g., your Spring Boot logs via CloudWatch Logs).
- **Alarms:** trigger actions when a metric crosses a threshold (e.g., email/SNS when CPU > 80%, or auto-scale).

### Q: How would you monitor an application with CloudWatch?
Send app logs to **CloudWatch Logs**, watch key **metrics** (CPU, latency, error rate), set **alarms** for thresholds, and visualize everything on a **dashboard**. Alarms can notify via SNS or trigger auto-scaling.

### Q: CloudWatch vs CloudTrail?
- **CloudWatch:** monitors **performance & health** (what's happening *in* resources).
- **CloudTrail:** records **API calls / who did what** (auditing & governance).

---

## Putting It Together

### Q: How do these services fit a typical web application?
A simple architecture for your stack:
```
GENERIC reference architecture (the one to draw on a whiteboard):

[ Angular app ]  →  S3 + CloudFront (static hosting + CDN, HTTPS)
       |
       v  (REST API calls)
[ Spring Boot API ] → runs on EC2 (behind a Load Balancer)
       |
       v
[ MySQL / Postgres ] → Amazon RDS (Multi-AZ for HA)

Monitoring/logs across all → CloudWatch (metrics, logs, alarms)
Occasional tasks (e.g. image processing) → Lambda triggered by S3
Access control everywhere → IAM (least privilege)
```

#### 🔵 And YOUR actual architecture ⭐

```
[ Angular web ] [ Ionic mobile ] [ Admin portal ] [ React CRM ]
        │              │                │               │
        └──────────────┴────────┬───────┴───────────────┘
                                ▼   REST + JWT
                    [ Spring Boot 3.2 / Java 17 ]
                                │
        ┌───────────────┬───────┴────────┬──────────────────┐
        ▼               ▼                ▼                  ▼
   [ MongoDB ]    [ AWS S3 ] ⭐    [ AWS Cognito ] ⭐   [ Firebase FCM ]
   documents      assets +          auth, 3 user        push
                  PRESIGNED URLs    pools + JWKS        notifications
                                                             │
                                      [ Razorpay ] ⭐ payments
                                      [ Quartz ]   scheduled jobs

   Local dev: LocalStack (S3 + Cognito emulated in Docker) ⭐
   CI/CD:     Jenkins (Jenkinsfile-CI / -CD) + Docker ⭐
```

> 🗣️ *"Four clients talk to one Spring Boot API over REST with JWT. Persistence is MongoDB; S3 holds assets and serves them through presigned URLs; Cognito is the identity provider; Firebase handles push; Razorpay handles payments; Quartz runs the scheduled jobs. Locally the AWS pieces are emulated with LocalStack, and CI/CD is Jenkins with Docker images."*

⭐ Being able to draw *your own* architecture beats reciting a textbook one.

### Q: How do you keep AWS costs low?
- Use the **right instance size** (don't over-provision); stop unused instances.
- Use **Auto Scaling** so capacity matches demand.
- Pick the **right S3 storage class** and lifecycle rules.
- Use **Reserved/Savings Plans** for steady workloads.
- Set **billing alarms** in CloudWatch.

### Q: What are basic AWS security best practices?
- Enable **MFA** and never use the root account for daily work.
- Apply **least privilege** IAM policies; use **roles** instead of hardcoded keys.
- **Encrypt** data at rest (S3, RDS) and in transit (HTTPS/TLS).
- Keep **security groups** tight (open only required ports).
- Enable **CloudTrail** for auditing.


---

## Quick Revision Sheet

```
THE MODEL  "Rent, don't build." Managed ↔ control is the trade-off ⭐
   EC2 🖥️ compute | S3 📦 storage | RDS 🗄️ database | Lambda ⚡ per-invocation
   IAM 🔐 permissions | CloudWatch 📊 monitoring | Cognito 🪪 auth ⭐

REGION vs AZ   Region = a city | AZ = a separate data centre in it
               Multi-AZ = survive one building failing ⭐

IAM   user (a person) | role (assumed, TEMPORARY credentials ⭐) | policy (JSON)
      Least privilege ⭐ | roles > hard-coded keys ⭐

EC2 vs LAMBDA  EC2 = always on, you patch it | Lambda = per request, cold start ⭐
SG vs NACL     SG = instance level, STATEFUL ⭐ | NACL = subnet, stateless

S3    buckets + objects | storage classes | versioning
      PRESIGNED URL ⭐ = private bucket + a time-limited direct link
RDS   Multi-AZ = HIGH AVAILABILITY (standby, no reads) ⭐
      Read replica = SCALE READS ⭐ — different things!

CloudWatch = metrics/logs/alarms (what happened INSIDE) ⭐
CloudTrail = WHO did what in the account (audit) ⭐

🔵 YOURS: S3 (PutObject + presigned URLs ⭐) · Cognito (3 pools + JWKS ⭐)
          LocalStack for local dev ⭐ · Jenkins CI/CD + Docker ⭐
          "I use S3 and Cognito in production", not "I know AWS basics" ⭐
```

---

**Related files:** [06 — Spring Boot](./06-spring-boot.md) · [08 — Microservices](./08-microservices-basics.md) · [39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)
