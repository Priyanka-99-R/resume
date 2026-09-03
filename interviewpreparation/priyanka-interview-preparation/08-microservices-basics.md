# Microservices (Basics) — Interview Q&A (Easy Version)

> ## ⚠️ **YOUR HONEST POSITION — read this first**
>
> I checked your code. **RoboGebra is a well-structured MODULAR MONOLITH, not microservices:**
>
> ```
> robogebra-portal (Spring Boot 3.2, ONE deployable):
>    Eureka / service discovery   → 0
>    Feign clients                → 0
>    Circuit breaker / Resilience4j → 0
>    Kafka / RabbitMQ             → 0
>    WebClient (outbound HTTP)    → 10 files ⭐
>    RestTemplate                 → 3 files
>    Domain packages              → exercise, quiz, payment, studygroup,
>                                   learningtimeline, notification… ⭐
> ```
>
> **So DON'T say "we use microservices."** One follow-up — *"how do your services discover each other?"* — and it falls apart.
>
> ### ✅ Say this instead ⭐
>
> > *"RoboGebra is a **modular monolith** — one Spring Boot deployment, but organised into clear domain packages that each own their repositories and services, so the seams are already there if we ever needed to split it. We do make outbound HTTP calls with `WebClient` — to the AI service and to Razorpay — and we use **AWS Cognito** as a separate identity provider and **Firebase** for push, so it's not a single closed box. I understand the microservice patterns — discovery, gateway, circuit breakers, database-per-service, saga — and the trade-offs, but on a team our size a monolith with clean module boundaries has been the right call. Martin Fowler's own advice is 'monolith first', and this is that."*
>
> ⭐ **That answer is STRONGER than claiming microservices.** It shows you can *choose* an architecture instead of following fashion — which is exactly what a lead-level interviewer is probing for.

Every question follows the same shape — **Easiest way to remember → simple explanation → real-world example → Easy memory box.**

---

## 🧠 The easiest way to remember microservices

```
MONOLITH      → one BIG restaurant kitchen 🍳
                  one menu, one team, one gas line.
                  The fryer breaks → the whole kitchen stops 💥

MICROSERVICES → a FOOD COURT 🍱 ⭐
                  separate stalls, separate kitchens, separate owners.
                  The pizza stall closes → biryani keeps selling ✅
                  But now you need signage, a shared payment desk,
                  and someone to coordinate them 😩
```

```
That ONE picture gives you every advantage AND every disadvantage:

✅ independent deployment · independent scaling · fault isolation · tech freedom
❌ network calls · no shared database · harder debugging · DevOps overhead ⭐
```

⭐ **The sentence that wins the topic:**

> *"Microservices trade **code complexity** for **operational complexity**. You don't remove the difficulty — you move it from inside one codebase to the network between many."*

#### Easy memory

```
Monolith = ONE kitchen 🍳 | Microservices = a FOOD COURT 🍱 ⭐

✅ deploy + scale + fail INDEPENDENTLY
❌ the network is now part of your logic 💥 (latency, partial failure)

"You trade code complexity for OPERATIONAL complexity." ⭐
Start with a monolith. Split when a real boundary hurts. ⭐
```

---

## Fundamentals

### Q: What are microservices?
An architectural style where an application is built as a **collection of small, independent services**, each responsible for one business capability, communicating over the network (usually REST or messaging). Each service can be **developed, deployed, and scaled independently**.

### Q: Monolith vs Microservices?
| | Monolith | Microservices |
|---|---|---|
| Codebase | Single, unified | Many small services |
| Deployment | Deploy whole app | Deploy each service independently |
| Scaling | Scale entire app | Scale only the service that needs it |
| Tech stack | Usually one | Each service can use its own |
| Failure | One bug can crash all | Failure is isolated |
| Complexity | Simple to start | More operational complexity |

### Q: What are the advantages of microservices?
- **Independent deployment** — ship one service without redeploying everything.
- **Independent scaling** — scale only the hot service.
- **Technology freedom** — different languages/DBs per service.
- **Fault isolation** — one service failing doesn't bring down the whole system.
- **Team autonomy** — small teams own small services.

### Q: What are the disadvantages / challenges?
- **Distributed system complexity** — network calls, latency, partial failures.
- **Data consistency** — no single database; eventual consistency.
- **Testing & debugging** harder across services.
- **Deployment & monitoring** overhead (many moving parts).
- **DevOps maturity** required (CI/CD, containers, orchestration).

### Q: When should you NOT use microservices?
- Small/simple applications or early-stage products — a monolith is faster to build.
- Small teams without DevOps maturity.
- When the domain boundaries aren't clear yet. *Start with a monolith, split later when needed (see strangler pattern).*

### Q: What are the key characteristics of a microservice?
Single responsibility, loosely coupled, independently deployable, owns its own data, communicates via well-defined APIs, and is **resilient** (handles failures of others gracefully).

### Q: Microservices vs SOA?
Both break apps into services, but **SOA** typically uses a heavy central **ESB (Enterprise Service Bus)** and shared databases, while **microservices** are lighter, smaller, decentralized, each owning its own data, communicating via simple protocols (REST/messaging).

---

## Communication Between Services

### Q: How do microservices communicate?
- **Synchronous** — one service calls another and waits (usually **REST/HTTP**, sometimes gRPC).
- **Asynchronous** — services exchange **messages/events** via a broker (Kafka, RabbitMQ), without waiting.

### Q: Synchronous vs Asynchronous — when to use which?
- **Synchronous (REST):** simple, immediate response needed. Risk: tight coupling, cascading failures.
- **Asynchronous (messaging):** decoupled, resilient, good for events ("OrderPlaced"), background work. Risk: eventual consistency, more complexity.

### Q: What is a message broker?
Middleware that lets services communicate via messages without direct calls. **Kafka** (high-throughput event streaming) and **RabbitMQ** (traditional message queue) are common. It decouples producers from consumers.

### Q: What is an API contract?
The agreed-upon **interface** (endpoints, request/response formats) between services. Keeping contracts stable (and versioning them) lets services evolve independently. Tools like OpenAPI/Swagger document them.

---

## Spring Cloud Building Blocks

### Q: What is service discovery? (Eureka)
In a dynamic environment, service instances come and go with changing IPs. A **service registry** like **Netflix Eureka** lets services **register themselves** and **discover** others by name instead of hardcoding URLs.
```
Service A → asks Eureka "where is PAYMENT-SERVICE?" → gets instance address → calls it
```

### Q: What is an API Gateway and why use it?
A **single entry point** for all client requests that routes them to the right backend service. **Spring Cloud Gateway** is common. Benefits: centralized **authentication, rate limiting, logging, routing, CORS**, and it hides internal service structure from clients.

### Q: What is a Config Server?
A **centralized configuration** service (Spring Cloud Config) that stores config for all microservices (often in Git). Services fetch their config at startup, so you manage settings in one place and across environments (dev/test/prod) without redeploying.

### Q: What is client-side load balancing?
The calling service picks which instance of the target service to call (e.g., **Spring Cloud LoadBalancer**, formerly Ribbon), often using the registry's list of instances — distributing load without a central load balancer.

### Q: What is a Feign client?
A **declarative REST client** in Spring Cloud. You define an interface with annotations, and Feign generates the HTTP call code — making inter-service REST calls clean:
```java
@FeignClient(name = "payment-service")
public interface PaymentClient {
    @GetMapping("/payments/{id}")
    Payment getPayment(@PathVariable Long id);
}
```

### Q: What is a Circuit Breaker and why is it needed?
If a downstream service is slow/failing, repeated calls pile up and can cascade. A **circuit breaker** (e.g., **Resilience4j**, formerly Hystrix) "trips" after repeated failures and **fails fast** (or returns a fallback) instead of waiting — protecting the whole system. States: **Closed → Open → Half-Open**.
```java
@CircuitBreaker(name = "paymentService", fallbackMethod = "fallback")
public Payment getPayment(Long id) { ... }
```

#### The circuit breaker, explained properly ⭐

```
CLOSED    → normal. Calls pass through. Failures are counted. ⭐
   ↓ too many failures
OPEN      → FAIL FAST. Don't even try — return a fallback immediately ⭐
   ↓ after a wait
HALF-OPEN → let ONE test call through
   ↓ it works → CLOSED ✅        ↓ it fails → OPEN again
```

Real-world idea: **the electrical trip switch in your house.** When something shorts, it cuts the circuit *instantly* rather than letting the whole house burn. Then you flip it back and see if the problem is gone.

⭐ **Why "fail fast" is the point** — this is the bit people miss:

```
WITHOUT a circuit breaker:
   the payment service is slow (30s timeouts)
      → every order request WAITS 30 seconds
      → all 200 Tomcat threads are stuck waiting 💥
      → your ENTIRE app is down, because ONE dependency is slow ⭐

WITH a circuit breaker:
   after N failures it OPENS → calls return instantly with a fallback
      → threads are freed → the rest of the app keeps working ✅
```

```
The failure CASCADES without one. That's the whole reason it exists ⭐
```

#### Easy memory

```
CLOSED → OPEN → HALF-OPEN → CLOSED ⭐ (a house trip switch 🔌)
OPEN = FAIL FAST with a fallback ⭐

Why: a SLOW dependency exhausts your thread pool and takes the
     WHOLE app down — the cascade is the danger, not the failure ⭐
Resilience4j (Hystrix is retired)
```

---

## Common Patterns

### Q: What is the API Gateway pattern?
Route all external traffic through one gateway that handles cross-cutting concerns (auth, routing, throttling) and aggregates responses — instead of clients calling many services directly.

### Q: What is "database per service"?
Each microservice **owns its own database**; no other service accesses it directly. This keeps services loosely coupled and independently deployable — but means **no cross-service joins** and the need for eventual consistency.

### Q: What is the Saga pattern?
A way to manage **distributed transactions** across services without a global lock. A business transaction is split into a sequence of **local transactions**; if one fails, **compensating transactions** undo the previous steps. Two styles: **choreography** (events) and **orchestration** (a coordinator).

#### The Saga, explained simply ⭐

```
One database → you have ROLLBACK. ✅
Five databases → there is NO global rollback. ⭐

So instead you do the UNDO YOURSELF, step by step.
```

```
Order Saga:
   1. Payment taken       ✅
   2. Stock reserved      ✅
   3. Shipping booked     ❌ FAILS
        ↓ now run the COMPENSATING transactions, in REVERSE ⭐
   2'. Release the stock
   1'. REFUND the payment ⭐
```

Real-world idea: **booking a holiday.** Flight ✅, hotel ✅, car hire ❌ sold out. Nobody can "roll back" your holiday — you have to **cancel the hotel and cancel the flight**, one at a time. Each cancellation is a compensating transaction.

```
CHOREOGRAPHY → each service listens for events and reacts
                  → no coordinator, but the flow is hard to SEE ⭐
ORCHESTRATION → one coordinator tells each service what to do
                  → easier to follow and debug, but it's a central point ⭐
```

#### Easy memory

```
Saga = many local transactions + COMPENSATING undo steps ⭐
   (a holiday booking — you cancel, you don't "roll back" 🏝️)

CHOREOGRAPHY = events, no boss (hard to trace) ⭐
ORCHESTRATION = a coordinator (easier to debug) ⭐
Needed because there is NO rollback across separate databases ⭐
```

---

### Q: What is CQRS? (brief)
**Command Query Responsibility Segregation** — separate the **write model** (commands) from the **read model** (queries), often with different data stores optimized for each. Useful for high-read systems; adds complexity.

### Q: What is the Strangler pattern?
A migration strategy: incrementally replace a monolith by building new functionality as microservices and **routing traffic gradually** away from the monolith until it's "strangled" out. Lower risk than a big-bang rewrite. *(Relevant to your migration experience.)*

### Q: What are retry and bulkhead patterns? (brief)
- **Retry:** automatically retry a failed call (with backoff) for transient errors.
- **Bulkhead:** isolate resources (e.g., separate thread pools) per dependency so one failing service can't exhaust all resources.

---

## Data & Consistency

### Q: Why does each service own its database?
To keep services **independent and loosely coupled** — a schema change in one service shouldn't break another. The trade-off is you lose ACID joins across services and must handle consistency at the application level.

#### Easy memory — database per service

```
Each service owns its OWN database. Nobody else touches it ⭐

✅ A schema change in one service can't break another
✅ Services deploy independently

❌ NO cross-service JOIN 💥 → you must call the other service, or duplicate data
❌ NO ACID transaction across services → you need a SAGA ⭐
```

Real-world idea: **separate bank accounts per family member.** Nobody can touch yours — but "how much does the family have?" now needs four phone calls instead of one query.

### Q: What is eventual consistency?
In distributed systems you often can't have instant consistency everywhere. **Eventual consistency** means data across services becomes consistent **after some delay** (e.g., via events). Acceptable for many business cases (e.g., an order summary updating moments later).

### Q: Why are distributed transactions hard?
There's no single database to roll back. A transaction spanning services can partially fail, so you need patterns like **Saga** with compensating actions instead of a traditional commit/rollback.

---

## Cross-Cutting Concerns

### Q: How do you handle logging across microservices?
**Centralized logging** — each service ships logs to a central system (e.g., ELK stack: Elasticsearch + Logstash + Kibana, or CloudWatch). This lets you search logs across all services in one place.

### Q: What is distributed tracing?
Tracking a single request as it flows through multiple services using a **correlation/trace ID**. Tools like **Spring Cloud Sleuth + Zipkin** add and propagate the ID so you can see the full path and find bottlenecks.

### Q: How is security handled in microservices?
Typically **token-based (JWT)**. The API Gateway authenticates the user, then the **token is propagated** to downstream services, each validating it. OAuth2 is common for authorization.

### Q: How does Docker fit microservices?
**Docker** packages each service with its dependencies into a **container** that runs identically anywhere. Each microservice → its own image → its own container, enabling consistent, isolated, independently deployable units. *(You used Docker on the Subsea project.)*

### Q: What is Kubernetes (very brief)?
A **container orchestration** platform that automates deploying, scaling, and managing containers across machines. It handles load balancing, self-healing (restarting failed containers), rolling updates, and service discovery — the standard way to run microservices at scale.

---

## Quick Honest Framing for the Interview

### The 30-second answer ⭐

> *"RoboGebra is a **modular monolith** — one Spring Boot 3.2 deployment organised into domain packages that each own their services and repositories, so the boundaries are already drawn. We do talk to external services: `WebClient` for the AI engine and Razorpay, AWS Cognito for identity, Firebase for push. I understand the microservice patterns and the trade-offs, but for our team size a monolith with clean modules has been the right call — you get most of the modularity without paying the operational tax."*

### If they push: *"So you haven't built microservices?"* ⭐

> *"Not as separate deployables, no. What I've done is the part that decides whether microservices would even work — keeping domain boundaries clean, owning a feature from API to UI, and dealing with the failure modes you get once a call leaves your process. On RoboGebra that showed up as a bounded thread pool with a caller-runs rejection policy for our push-notification fan-out, so a slow external service can't exhaust the pool and take the app down. That's the same reasoning behind a bulkhead and a circuit breaker — I've just applied it inside one process."*

⭐ **That is a genuinely strong answer.** It converts "I haven't done X" into "here is the underlying problem X solves, and here's how I've handled it." → [39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)

### ❌ What NOT to say

```
❌ "Yes, we use microservices."
      → "How do your services discover each other?" → over 💥
❌ "We use Eureka and a circuit breaker."
      → you have neither. Don't name a tool you can't discuss ⭐
❌ "Microservices are better."
      → they're a TRADE-OFF. Saying otherwise reads as inexperience ⭐
```

---

## Quick Revision Sheet

```
Monolith = ONE kitchen 🍳 | Microservices = a FOOD COURT 🍱 ⭐
"You trade CODE complexity for OPERATIONAL complexity" ⭐

COMMUNICATION  sync (REST/gRPC — simple, but coupling + cascades)
               async (Kafka/RabbitMQ — decoupled, but eventual consistency)

SPRING CLOUD   Eureka (discovery) | Gateway (one entry point ⭐)
               Config Server | Feign (declarative REST) | Resilience4j

CIRCUIT BREAKER ⭐ CLOSED → OPEN (fail fast) → HALF-OPEN 🔌
   Why: a SLOW dependency exhausts your threads and kills the WHOLE app ⭐

PATTERNS  DB per service (no joins ⭐) | SAGA (compensating undo 🏝️ ⭐)
          CQRS (split read/write) | STRANGLER (migrate gradually ⭐)
          Retry + BULKHEAD (isolated pools ⭐)

CROSS-CUTTING  centralised logs (ELK) | distributed tracing (correlation id ⭐)
               JWT at the gateway | Docker + Kubernetes

⭐ YOUR ANSWER: "RoboGebra is a MODULAR MONOLITH — one deployment,
   clean domain boundaries. I know the patterns and the trade-offs;
   for our team size this was the right call. Monolith first."
```

---

**Related files:** [06 — Spring Boot](./06-spring-boot.md) · [07 — AWS](./07-aws-basics.md) · [32 — Multithreading (bounded pools, bulkheads)](./32-multithreading.md) · [39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)
