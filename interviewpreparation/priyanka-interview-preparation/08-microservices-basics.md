# Microservices (Basics) — Interview Q&A

> **Note:** Microservices is listed as a **basic** skill. Focus on core concepts and honest, conceptual answers. Tie examples to Spring Boot / Spring Cloud where natural, since that's your backend stack.

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

---

## Common Patterns

### Q: What is the API Gateway pattern?
Route all external traffic through one gateway that handles cross-cutting concerns (auth, routing, throttling) and aggregates responses — instead of clients calling many services directly.

### Q: What is "database per service"?
Each microservice **owns its own database**; no other service accesses it directly. This keeps services loosely coupled and independently deployable — but means **no cross-service joins** and the need for eventual consistency.

### Q: What is the Saga pattern?
A way to manage **distributed transactions** across services without a global lock. A business transaction is split into a sequence of **local transactions**; if one fails, **compensating transactions** undo the previous steps. Two styles: **choreography** (events) and **orchestration** (a coordinator).

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

> "I've worked primarily on the application side — building REST APIs with Spring Boot and consuming them from Angular, and I used Docker on the Subsea project. I understand microservices concepts — independent deployment, service discovery, API gateway, circuit breakers, database-per-service, and the trade-offs around consistency. I'm comfortable picking these up hands-on with Spring Cloud."
