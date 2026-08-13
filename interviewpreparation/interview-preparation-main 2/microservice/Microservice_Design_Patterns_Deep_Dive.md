# Microservice Design Patterns — Deep Dive Interview Answers
### Senior Java / Spring Boot (5+ Years)
### Patterns: API Gateway, Service Discovery, Circuit Breaker, Saga, Database per Service, Event-Driven, CQRS

---

## 1. API Gateway Pattern

---

### What is the API Gateway pattern?

**Summary:**
API Gateway is a single entry point that sits between clients and microservices. It handles routing, authentication, rate limiting, response aggregation, and protocol translation. In Spring Boot, I use Spring Cloud Gateway. Think of it as the "receptionist" of your microservices architecture — every external request goes through it first.

**How It Works:**

```
                         ┌─────────────────┐
  Mobile App ──────────▶ │                 │ ──▶ User Service (port 8081)
                         │   API GATEWAY   │
  Web App ─────────────▶ │  (port 8080)    │ ──▶ Order Service (port 8082)
                         │                 │
  Third-Party API ─────▶ │  Routes, Auth,  │ ──▶ Product Service (port 8083)
                         │  Rate Limit,    │
                         │  Load Balance   │ ──▶ Payment Service (port 8084)
                         └─────────────────┘
```

**What the Gateway Does:**

| Responsibility | Without Gateway | With Gateway |
|----------------|-----------------|--------------|
| Routing | Client knows every service URL | Client knows ONE URL |
| Authentication | Each service validates JWT | Gateway validates once, forwards |
| Rate Limiting | Implemented per service | Centralized at gateway |
| CORS | Configured per service | One place |
| Load Balancing | Client-side or external | Gateway handles it |
| Response Aggregation | Client makes multiple calls | Gateway combines responses |
| Protocol Translation | Client must speak each protocol | Gateway translates (gRPC → REST) |

**Spring Cloud Gateway Implementation:**

```yaml
# application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://USER-SERVICE    # lb:// enables load balancing via service discovery
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
            - name: CircuitBreaker
              args:
                name: userServiceCB
                fallbackUri: forward:/fallback/users

        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@apiKeyResolver}"

        - id: product-service
          uri: lb://PRODUCT-SERVICE
          predicates:
            - Path=/api/products/**
            - Method=GET,POST
          filters:
            - StripPrefix=1
            - AddRequestHeader=X-Request-Source, api-gateway
```

**Custom Global Filter — JWT Authentication:**

```java
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider tokenProvider;

    // Paths that don't need authentication
    private static final List<String> OPEN_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/products"  // public product listing
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip auth for open paths
        if (OPEN_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // Extract and validate JWT
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = tokenProvider.validateToken(token);

            // Forward user info to downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", claims.getSubject())
                .header("X-User-Role", claims.get("role", String.class))
                .header("X-Correlation-Id", UUID.randomUUID().toString())
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (JwtException e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }
}
```

**Rate Limiter with Redis:**

```java
@Bean
public KeyResolver apiKeyResolver() {
    // Rate limit per API key (for external partners)
    return exchange -> Mono.just(
        exchange.getRequest().getHeaders().getFirst("X-API-Key")
    );
}

@Bean
public KeyResolver userKeyResolver() {
    // Rate limit per user (from JWT)
    return exchange -> Mono.just(
        exchange.getRequest().getHeaders().getFirst("X-User-Id")
    );
}

@Bean
public KeyResolver ipKeyResolver() {
    // Rate limit per IP (for anonymous users)
    return exchange -> Mono.just(
        Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
            .getAddress().getHostAddress()
    );
}
```

**BFF Pattern (Backend For Frontend) — Multiple Gateways:**

```
Mobile App ──────▶ Mobile Gateway ──────▶  (lightweight responses,
                   (strips images,          smaller payloads)
                    paginates aggressively)
                                        ├──▶ User Service
Web App ─────────▶ Web Gateway ─────────┤
                   (full responses,     ├──▶ Order Service
                    aggregates data)    │
                                        └──▶ Product Service
Partner API ─────▶ Partner Gateway
                   (strict rate limits,
                    API key auth, versioned)
```

**Real-World Scenario:**
In a fintech project, our API Gateway handled: JWT validation for mobile/web clients, API key validation for banking partners, rate limiting partners to 100 req/sec, converting SOAP responses from a legacy banking system to JSON for mobile apps, and aggregating user + account + transaction data into a single dashboard response. Without the gateway, each service would have duplicated auth logic, and clients would need to make 3 separate calls for the dashboard.

**Common Mistakes:**
- Putting business logic in the gateway — it should be a thin routing/security layer. Business logic belongs in services.
- Single gateway becoming a bottleneck — horizontally scale it; or use BFF pattern with separate gateways per client type.
- Not implementing circuit breakers in the gateway — when a downstream service is down, the gateway should return a fallback, not hang.
- Hardcoding service URLs — use service discovery (`lb://SERVICE-NAME`) for dynamic routing.

**Closing:**
API Gateway is the front door of microservices — it centralizes cross-cutting concerns so services focus on business logic. I use Spring Cloud Gateway for reactive routing and combine it with Eureka for discovery, Resilience4j for circuit breaking, and Redis for rate limiting. Keep it thin, scalable, and focused.

---

## 2. Service Discovery Pattern

---

### What is Service Discovery and why is it needed in microservices?

**Summary:**
Service Discovery allows microservices to find each other dynamically without hardcoded URLs. When services scale up/down or instances restart with new IPs, the service registry keeps track of all healthy instances. I've used Eureka with Spring Cloud and Kubernetes-native DNS discovery in containerized environments.

**Why It's Needed:**

```
WITHOUT Service Discovery:
  Order Service → http://192.168.1.15:8082/api/users/123
  ❌ What if User Service moves to a different IP?
  ❌ What if we add 5 more instances for load?
  ❌ What if an instance crashes?

WITH Service Discovery:
  Order Service → lb://USER-SERVICE/api/users/123
  ✅ Registry resolves "USER-SERVICE" to healthy instances
  ✅ Load balancer picks one
  ✅ Dead instances are automatically removed
```

**How It Works — Client-Side Discovery (Eureka):**

```
┌───────────────────────────────────────────────────────┐
│               Eureka Server (Registry)                 │
│                                                        │
│  USER-SERVICE:                                         │
│    → 192.168.1.10:8081 (UP, last heartbeat: 5s ago)  │
│    → 192.168.1.11:8081 (UP, last heartbeat: 3s ago)  │
│    → 192.168.1.12:8081 (DOWN, missed 3 heartbeats)   │
│                                                        │
│  ORDER-SERVICE:                                        │
│    → 192.168.1.20:8082 (UP)                           │
│    → 192.168.1.21:8082 (UP)                           │
│                                                        │
│  PRODUCT-SERVICE:                                      │
│    → 192.168.1.30:8083 (UP)                           │
└───────────────────────────────────────────────────────┘

1. Service starts → REGISTERS with Eureka ("I'm USER-SERVICE at 192.168.1.10:8081")
2. Every 30s → sends HEARTBEAT ("I'm still alive")
3. Missed 3 heartbeats → Eureka marks instance as DOWN
4. Caller fetches registry → gets list of UP instances → load balances
```

**Eureka Server Setup:**

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

```yaml
# eureka-server application.yml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false   # Server doesn't register itself
    fetch-registry: false
  server:
    enable-self-preservation: true  # Don't evict all instances during network issues
    eviction-interval-timer-in-ms: 5000
```

**Eureka Client (Every Microservice):**

```yaml
# user-service application.yml
spring:
  application:
    name: USER-SERVICE   # This is the name used for discovery

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    registry-fetch-interval-seconds: 15  # refresh registry cache
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10    # heartbeat interval
    lease-expiration-duration-in-seconds: 30  # evict after 30s no heartbeat
    metadata-map:
      zone: us-east-1a   # useful for zone-aware load balancing
```

**Calling Another Service — Using Discovery:**

```java
// Option 1: WebClient with LoadBalancer (Recommended for reactive)
@Configuration
public class WebClientConfig {
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}

@Service
public class OrderService {
    private final WebClient.Builder webClientBuilder;

    public Mono<UserDto> getUser(Long userId) {
        return webClientBuilder.build()
            .get()
            .uri("http://USER-SERVICE/api/users/{id}", userId)
            //     ^^^^^^^^^^^^ resolved by service discovery
            .retrieve()
            .bodyToMono(UserDto.class);
    }
}

// Option 2: OpenFeign Client (Declarative — simpler for REST)
@FeignClient(name = "USER-SERVICE", fallbackFactory = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable Long id);

    @GetMapping("/api/users/search")
    List<UserDto> searchUsers(@RequestParam String query);
}

// Feign automatically uses Eureka to resolve USER-SERVICE
// and load-balances across instances
```

**Eureka vs Consul vs Kubernetes:**

| Feature | Eureka | Consul | Kubernetes DNS |
|---------|--------|--------|----------------|
| Type | Client-side discovery | Client/Server-side | Server-side (DNS) |
| Health Check | Heartbeat from client | Server-side active checks | Readiness/Liveness probes |
| Config Management | No | Yes (KV store) | ConfigMaps/Secrets |
| Multi-Datacenter | Limited | Yes (native) | Federation |
| Best For | Spring Cloud on VMs | Multi-platform, hybrid | K8s-native apps |
| Extra Component | Yes (Eureka Server) | Yes (Consul agent) | No (built into K8s) |

**Kubernetes Service Discovery (No Eureka Needed):**

```yaml
# Kubernetes Service — acts as a built-in registry
apiVersion: v1
kind: Service
metadata:
  name: user-service    # DNS: user-service.default.svc.cluster.local
spec:
  selector:
    app: user-service
  ports:
    - port: 8081
      targetPort: 8081
  type: ClusterIP

# Any pod can call: http://user-service:8081/api/users/123
# K8s DNS resolves it and load balances across pods
```

```yaml
# Spring Boot in K8s — no Eureka dependency
spring:
  application:
    name: order-service
  cloud:
    discovery:
      enabled: false   # disable Eureka
    kubernetes:
      discovery:
        enabled: true  # use K8s native discovery
```

**High Availability — Eureka Server Cluster:**

```yaml
# Eureka Server 1 (peers with Server 2)
eureka:
  client:
    service-url:
      defaultZone: http://eureka-2:8761/eureka/

# Eureka Server 2 (peers with Server 1)
eureka:
  client:
    service-url:
      defaultZone: http://eureka-1:8761/eureka/

# Services register with both — if one goes down, the other serves
```

**Real-World Scenario:**
In an e-commerce platform running on EC2, we had 15 microservices with 2-5 instances each. Eureka tracked 40+ instances. When auto-scaling added Order Service instances during Black Friday (from 3 to 12), the API Gateway and all other services discovered the new instances within 15 seconds through Eureka's registry cache refresh — zero configuration changes, zero downtime.

**Common Mistakes:**
- Using Eureka in Kubernetes — redundant. K8s provides service discovery natively.
- Not configuring health checks properly — dead instances stay in the registry for up to 90 seconds (default eviction delay + cache lag).
- Single Eureka Server — always deploy 2+ for HA. Eureka peers replicate registrations.
- Not handling service-down scenarios — when a service has zero healthy instances, callers should have circuit breakers and fallbacks.
- Ignoring Eureka's self-preservation mode — during network partitions, Eureka stops evicting instances to prevent mass de-registration. This can leave stale entries. Understand this behavior before disabling it.

**Closing:**
Service Discovery eliminates hardcoded URLs and enables dynamic scaling. On VMs, Eureka with Spring Cloud is the standard. On Kubernetes, use native DNS discovery — simpler, fewer moving parts. The key is combining discovery with load balancing and circuit breaking for a resilient architecture.

---

## 3. Circuit Breaker Pattern

---

### What is the Circuit Breaker pattern and how do you implement it?

**Summary:**
Circuit Breaker prevents cascading failures by stopping calls to a failing downstream service. When failures cross a threshold, the circuit "opens" and returns a fallback immediately — no waiting for timeouts. It protects system resources and gives the failing service time to recover. I use Resilience4j in Spring Boot — it's the modern replacement for Netflix Hystrix.

**How It Works — Three States:**

```
               failure rate > threshold
    ┌──────────────────────────────────────┐
    │                                      ▼
┌───────┐    success    ┌──────┐    wait duration    ┌───────────┐
│ CLOSED │◄─────────────│ HALF │◄────────────────────│   OPEN    │
│       │              │ OPEN │                      │           │
│ Normal│              │Trial │                      │ Fallback  │
│ calls │              │calls │                      │ returned  │
└───────┘              └──────┘                      └───────────┘
                         │  failure                         ▲
                         └────────────────────────────────┘

CLOSED:    All calls pass through. Failures are counted.
           If failure rate > 50% in last 10 calls → OPEN

OPEN:      All calls return fallback immediately. No network call.
           After 30 seconds → HALF-OPEN

HALF-OPEN: Allow 5 trial calls through.
           If trials succeed → CLOSED (recovered!)
           If trials fail → OPEN (still broken)
```

**Resilience4j Implementation:**

```yaml
# application.yml
resilience4j:
  circuitbreaker:
    instances:
      paymentService:
        register-health-indicator: true
        sliding-window-type: COUNT_BASED
        sliding-window-size: 10                  # evaluate last 10 calls
        failure-rate-threshold: 50               # open if 50% fail
        wait-duration-in-open-state: 30s         # stay open for 30s
        permitted-number-of-calls-in-half-open-state: 5  # trial calls
        minimum-number-of-calls: 5               # don't evaluate until 5 calls
        slow-call-duration-threshold: 3s         # 3s+ = slow call
        slow-call-rate-threshold: 80             # open if 80% are slow
        record-exceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
          - org.springframework.web.client.HttpServerErrorException
        ignore-exceptions:
          - com.app.exception.BusinessValidationException  # 4xx = not a failure
```

```java
@Service
@Slf4j
public class PaymentService {

    private final PaymentGatewayClient paymentClient;

    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService")
    @TimeLimiter(name = "paymentService")
    public CompletableFuture<PaymentResponse> processPayment(PaymentRequest request) {
        log.info("Calling payment gateway for order: {}", request.getOrderId());
        return CompletableFuture.supplyAsync(() ->
            paymentClient.charge(request)
        );
    }

    // Fallback — called when circuit is OPEN or call fails after retries
    private CompletableFuture<PaymentResponse> paymentFallback(
            PaymentRequest request, Throwable throwable) {

        log.warn("Payment circuit breaker activated for order: {}. Reason: {}",
            request.getOrderId(), throwable.getMessage());

        // Option 1: Queue for later processing
        paymentRetryQueue.enqueue(request);

        return CompletableFuture.completedFuture(
            PaymentResponse.builder()
                .status(PaymentStatus.PENDING)
                .message("Payment queued for processing. You will be notified.")
                .orderId(request.getOrderId())
                .build()
        );
    }
}
```

**Combining Resilience Patterns (Defense in Depth):**

```java
// Execution order: TimeLimiter → CircuitBreaker → Retry → Bulkhead → Method

@CircuitBreaker(name = "inventoryService", fallbackMethod = "inventoryFallback")
@Retry(name = "inventoryService")
@Bulkhead(name = "inventoryService")
@TimeLimiter(name = "inventoryService")
public CompletableFuture<StockResponse> checkStock(String productId) {
    return CompletableFuture.supplyAsync(() ->
        inventoryClient.getStock(productId)
    );
}
```

```yaml
# Retry configuration
resilience4j:
  retry:
    instances:
      paymentService:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2    # 1s → 2s → 4s
        retry-exceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
        ignore-exceptions:
          - com.app.exception.BusinessValidationException

  # Timeout configuration
  timelimiter:
    instances:
      paymentService:
        timeout-duration: 3s
        cancel-running-future: true

  # Bulkhead — isolate thread pools
  bulkhead:
    instances:
      inventoryService:
        max-concurrent-calls: 20
        max-wait-duration: 500ms
```

**Monitoring Circuit Breaker State:**

```java
// Expose via Actuator
@Bean
public HealthIndicator circuitBreakerHealth(CircuitBreakerRegistry registry) {
    return new CircuitBreakersHealthIndicator(registry);
}

// Actuator endpoint: /actuator/health
// Shows: { "circuitBreakers": { "paymentService": "CLOSED" } }

// Event listener — alert when circuit opens
@Bean
public RegistryEventConsumer<CircuitBreaker> circuitBreakerEventConsumer() {
    return new RegistryEventConsumer<>() {
        @Override
        public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> event) {
            event.getAddedEntry().getEventPublisher()
                .onStateTransition(e -> {
                    log.warn("Circuit breaker '{}' transitioned: {} → {}",
                        e.getCircuitBreakerName(),
                        e.getStateTransition().getFromState(),
                        e.getStateTransition().getToState());

                    if (e.getStateTransition().getToState() == CircuitBreaker.State.OPEN) {
                        alertingService.sendAlert(
                            "Circuit OPEN: " + e.getCircuitBreakerName()
                        );
                    }
                });
        }
    };
}
```

**Real-World Scenario:**
Our Order Service called an external payment gateway that experienced intermittent 30-second timeouts. Without circuit breaker, each timeout consumed a thread for 30 seconds — with 100 concurrent orders, all Tomcat threads were exhausted and the Order Service itself went down. After adding Resilience4j circuit breaker (threshold: 50%, window: 10, open duration: 30s), the circuit opened after 5 failures, fallback queued payments for retry, and the Order Service stayed healthy. When the gateway recovered, the half-open trial calls succeeded and the circuit closed automatically.

**Common Mistakes:**
- Not defining a meaningful fallback — throwing an exception from the fallback defeats the purpose. Return a degraded response (cached data, default values, "queued for processing").
- Treating business validation errors (4xx) as circuit breaker failures — a 400 Bad Request is a client error, not a downstream failure. Use `ignore-exceptions` for these.
- Not combining with retry — circuit breaker alone doesn't handle transient failures. Retry handles the brief ones; circuit breaker handles prolonged outages.
- Setting thresholds too low — a window of 3 calls with 50% threshold means 2 failures open the circuit. Too aggressive.
- Not monitoring state transitions — if you don't alert on circuit-open events, you won't know about downstream problems until customers report them.

**Closing:**
Circuit Breaker is the #1 resilience pattern in microservices. I layer it: Timeout → Retry (with backoff) → Circuit Breaker → Bulkhead → Fallback. Resilience4j integrates seamlessly with Spring Boot, and I always configure monitoring + alerts for state transitions.

---

## 4. Saga Pattern

---

### What is the Saga pattern and how does it handle distributed transactions?

**Summary:**
Saga manages distributed transactions across multiple microservices without using 2-Phase Commit. It breaks a transaction into a sequence of local transactions, each publishing an event or calling the next step. If any step fails, compensating transactions undo the previous steps. There are two types: Choreography (event-based, decentralized) and Orchestration (coordinator-based, centralized).

**Why Not 2PC (2-Phase Commit)?**

```
2PC (Distributed Transaction):
  Coordinator locks resources across ALL services → waits for all → commit/rollback
  ❌ Locks held across network → performance killer
  ❌ Coordinator is single point of failure
  ❌ Doesn't work with NoSQL, message brokers, or external APIs
  ❌ Violates microservices autonomy

Saga (Eventual Consistency):
  Each service commits locally → publishes event → next service reacts
  ✅ No distributed locks
  ✅ No coordinator bottleneck (choreography)
  ✅ Works with any database/broker
  ✅ Each service is autonomous
  ⚠️ Eventually consistent (not immediately consistent)
```

**Order Placement Saga Example:**

```
Happy Path:
  Order Service    → Create Order (PENDING)
  Payment Service  → Charge Payment
  Inventory Service → Reserve Stock
  Notification Service → Send Confirmation
  Order Service    → Update Order (CONFIRMED)

Failure Path (Payment fails):
  Order Service    → Create Order (PENDING)
  Payment Service  → Charge Payment ❌ FAILED
  ↓ Compensating Transactions:
  Order Service    → Cancel Order (CANCELLED)   ← compensate step 1
```

**Approach 1 — Choreography (Event-Based):**

```
Each service listens for events and reacts independently.
No central coordinator — services communicate through Kafka events.

Order Service                Payment Service             Inventory Service
    │                             │                            │
    │ ──OrderCreated──▶           │                            │
    │                    Process payment                       │
    │                             │                            │
    │           ◀──PaymentCompleted──                          │
    │                             │ ──PaymentCompleted──▶      │
    │                                              Reserve stock
    │                                                          │
    │                                    ◀──StockReserved──────│
    │ ◀────────────────StockReserved───────────────────────────│
    │                                                          │
  Confirm Order                                                │

If Payment Fails:
    │           ◀──PaymentFailed──                             │
  Cancel Order                                                 │
```

```java
// ORDER SERVICE — publishes initial event
@Service
@Transactional
public class OrderService {

    public Order createOrder(OrderRequest request) {
        Order order = Order.builder()
            .customerId(request.getCustomerId())
            .items(request.getItems())
            .totalAmount(calculateTotal(request.getItems()))
            .status(OrderStatus.PENDING)
            .build();

        Order saved = orderRepository.save(order);

        // Publish event to Kafka (using Outbox pattern for reliability)
        outboxRepository.save(OutboxEvent.builder()
            .aggregateId(saved.getId().toString())
            .eventType("OrderCreated")
            .payload(objectMapper.writeValueAsString(new OrderCreatedEvent(saved)))
            .build());

        return saved;
    }

    // Listen for downstream events
    @KafkaListener(topics = "payment-events")
    public void handlePaymentEvent(PaymentEvent event) {
        if (event.getStatus() == PaymentStatus.COMPLETED) {
            orderRepository.updateStatus(event.getOrderId(), OrderStatus.PAYMENT_CONFIRMED);
        } else if (event.getStatus() == PaymentStatus.FAILED) {
            // COMPENSATING TRANSACTION — cancel the order
            orderRepository.updateStatus(event.getOrderId(), OrderStatus.CANCELLED);
            log.warn("Order {} cancelled due to payment failure", event.getOrderId());
        }
    }

    @KafkaListener(topics = "inventory-events")
    public void handleInventoryEvent(InventoryEvent event) {
        if (event.getStatus() == InventoryStatus.RESERVED) {
            orderRepository.updateStatus(event.getOrderId(), OrderStatus.CONFIRMED);
        } else if (event.getStatus() == InventoryStatus.INSUFFICIENT) {
            // COMPENSATING TRANSACTION — refund payment, cancel order
            paymentEventPublisher.publish(new RefundRequestedEvent(event.getOrderId()));
            orderRepository.updateStatus(event.getOrderId(), OrderStatus.CANCELLED);
        }
    }
}
```

```java
// PAYMENT SERVICE — reacts to OrderCreated, publishes result
@Service
public class PaymentEventHandler {

    @KafkaListener(topics = "order-events")
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            PaymentResult result = paymentGateway.charge(
                event.getCustomerId(),
                event.getTotalAmount()
            );

            paymentRepository.save(Payment.builder()
                .orderId(event.getOrderId())
                .amount(event.getTotalAmount())
                .transactionId(result.getTransactionId())
                .status(PaymentStatus.COMPLETED)
                .build());

            kafkaTemplate.send("payment-events", new PaymentEvent(
                event.getOrderId(), PaymentStatus.COMPLETED, result.getTransactionId()
            ));

        } catch (PaymentException e) {
            kafkaTemplate.send("payment-events", new PaymentEvent(
                event.getOrderId(), PaymentStatus.FAILED, null
            ));
        }
    }

    // Compensating transaction — handle refund requests
    @KafkaListener(topics = "payment-events", groupId = "payment-refund")
    public void handleRefundRequested(RefundRequestedEvent event) {
        Payment payment = paymentRepository.findByOrderId(event.getOrderId());
        paymentGateway.refund(payment.getTransactionId());
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
    }
}
```

**Approach 2 — Orchestration (Coordinator-Based):**

```
A central Saga Orchestrator controls the flow.
Better for complex workflows with 5+ steps.

                    ┌─────────────────────────┐
                    │   OrderSagaOrchestrator   │
                    │                           │
                    │ Step 1: Create Order      │
                    │ Step 2: Process Payment   │
                    │ Step 3: Reserve Inventory │
                    │ Step 4: Send Notification │
                    │ Step 5: Confirm Order     │
                    └────────┬────────────────┘
                             │
            ┌────────────────┼────────────────┐
            ▼                ▼                ▼
       Order Service   Payment Service   Inventory Service
```

```java
@Service
@Slf4j
public class OrderSagaOrchestrator {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;

    @Transactional
    public OrderResult executeOrderSaga(OrderRequest request) {
        String sagaId = UUID.randomUUID().toString();
        log.info("Starting order saga: {}", sagaId);

        Order order = null;
        PaymentResult payment = null;
        InventoryReservation reservation = null;

        try {
            // Step 1: Create Order
            order = orderService.createOrder(request);
            log.info("Saga {}: Order created - {}", sagaId, order.getId());

            // Step 2: Process Payment
            payment = paymentService.processPayment(
                order.getId(), order.getTotalAmount(), request.getPaymentDetails()
            );
            log.info("Saga {}: Payment processed - {}", sagaId, payment.getTransactionId());

            // Step 3: Reserve Inventory
            reservation = inventoryService.reserveStock(order.getId(), request.getItems());
            log.info("Saga {}: Inventory reserved", sagaId);

            // Step 4: Confirm Order
            orderService.confirmOrder(order.getId());

            // Step 5: Send Notification (non-critical — don't compensate if fails)
            try {
                notificationService.sendOrderConfirmation(order);
            } catch (Exception e) {
                log.warn("Saga {}: Notification failed, order still confirmed", sagaId);
            }

            log.info("Saga {}: Completed successfully", sagaId);
            return OrderResult.success(order);

        } catch (PaymentException e) {
            // COMPENSATE: Cancel order
            log.error("Saga {}: Payment failed, compensating", sagaId);
            if (order != null) orderService.cancelOrder(order.getId());
            return OrderResult.failure("Payment failed: " + e.getMessage());

        } catch (InsufficientStockException e) {
            // COMPENSATE: Refund payment, cancel order
            log.error("Saga {}: Inventory failed, compensating", sagaId);
            if (payment != null) paymentService.refund(payment.getTransactionId());
            if (order != null) orderService.cancelOrder(order.getId());
            return OrderResult.failure("Insufficient stock: " + e.getMessage());

        } catch (Exception e) {
            // COMPENSATE: Full rollback
            log.error("Saga {}: Unexpected failure, full compensate", sagaId, e);
            if (reservation != null) inventoryService.releaseStock(reservation.getId());
            if (payment != null) paymentService.refund(payment.getTransactionId());
            if (order != null) orderService.cancelOrder(order.getId());
            return OrderResult.failure("Order failed: " + e.getMessage());
        }
    }
}
```

**Choreography vs Orchestration:**

| Feature | Choreography | Orchestration |
|---------|-------------|---------------|
| Coordination | Decentralized (events) | Centralized (orchestrator) |
| Coupling | Loose | Slightly tighter |
| Complexity | Hard to trace with 5+ services | Clear flow, easy to trace |
| Single point of failure | None | Orchestrator (deploy HA) |
| Debugging | Distributed tracing needed | Clear error at orchestrator |
| Best for | Simple flows (2-3 steps) | Complex flows (5+ steps) |
| Tooling | Kafka + event listeners | Dedicated saga framework |

**Real-World Scenario:**
In an e-commerce platform, the order flow involved 5 services: Order → Payment → Inventory → Shipping → Notification. We used orchestration because the flow had conditional logic (different shipping partners based on region) and complex compensation (partial refunds for partially shipped orders). The orchestrator logged every step, making it trivial to debug failed orders — we could see exactly which step failed and what compensations ran.

**Common Mistakes:**
- Forgetting compensating transactions — data becomes permanently inconsistent.
- Not making steps idempotent — retries cause duplicates (charging twice, reserving double stock).
- Using choreography for complex flows — becomes "event spaghetti" where nobody knows the full flow.
- Not handling partial compensation — if compensation itself fails (refund API down), you need a dead-letter queue and manual resolution process.
- Treating Saga as instant — it's eventually consistent. The UI must handle intermediate states ("Processing your order...").

**Closing:**
Saga is the go-to pattern for distributed transactions in microservices. Choreography for simple flows (2-3 steps), orchestration for complex ones (5+ steps). The key is idempotent steps, reliable compensating transactions, and proper state tracking. Combined with the Outbox pattern for reliable event publishing, it's production-proven.

---

## 5. Database per Service Pattern

---

### What is the Database per Service pattern? How do you handle cross-service queries?

**Summary:**
Each microservice owns its own database — no other service can directly access it. This ensures loose coupling, independent deployability, and technology freedom (PostgreSQL for one, MongoDB for another). For cross-service data needs, I use API Composition for simple queries and CQRS with event-driven projections for complex ones.

**Why Each Service Needs Its Own Database:**

```
❌ SHARED DATABASE (Monolithic approach):
  Order Service ──────┐
  User Service ───────┼──▶ [Shared PostgreSQL]
  Product Service ────┘
  Problems:
    → Schema change in User table breaks Order Service
    → Can't scale databases independently
    → Can't use different DB technologies
    → Tight coupling — "distributed monolith"

✅ DATABASE PER SERVICE:
  Order Service ──────▶ [Order DB - PostgreSQL]
  User Service ───────▶ [User DB - PostgreSQL]
  Product Service ────▶ [Product DB - MongoDB]    ← different tech!
  Search Service ─────▶ [Search DB - Elasticsearch]
  Analytics Service ──▶ [Analytics DB - ClickHouse]
```

**Handling Cross-Service Queries:**

**Option 1 — API Composition (Simple Aggregation):**

```java
// Dashboard needs: user info + recent orders + payment summary
// An aggregator service calls each service and combines results

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UserClient userClient;
    private final OrderClient orderClient;
    private final PaymentClient paymentClient;

    @GetMapping("/{userId}")
    public DashboardResponse getDashboard(@PathVariable Long userId) {
        // Parallel calls for performance
        CompletableFuture<UserDto> userFuture =
            CompletableFuture.supplyAsync(() -> userClient.getUser(userId));
        CompletableFuture<List<OrderDto>> ordersFuture =
            CompletableFuture.supplyAsync(() -> orderClient.getRecentOrders(userId));
        CompletableFuture<PaymentSummaryDto> paymentFuture =
            CompletableFuture.supplyAsync(() -> paymentClient.getSummary(userId));

        CompletableFuture.allOf(userFuture, ordersFuture, paymentFuture).join();

        return DashboardResponse.builder()
            .user(userFuture.join())
            .recentOrders(ordersFuture.join())
            .paymentSummary(paymentFuture.join())
            .build();
    }
}
```

**Option 2 — Event-Driven Data Replication:**

```
User Service (Source of Truth)
    │
    ├── DB: users table
    │
    └── Publishes: UserCreated, UserUpdated → Kafka
                                                │
Order Service (Needs user data for order display)│
    │                                           │
    ├── Consumes events ◄───────────────────────┘
    │
    └── DB: user_cache table (id, name, email only)
            ↑ read-only projection, only fields it needs
```

```java
// ORDER SERVICE — maintains local copy of user data
@Service
public class UserDataReplicator {

    @KafkaListener(topics = "user-events")
    @Transactional
    public void handleUserEvent(UserEvent event) {
        switch (event.getType()) {
            case "UserCreated":
            case "UserUpdated":
                userCacheRepository.upsert(UserCache.builder()
                    .userId(event.getUserId())
                    .name(event.getName())
                    .email(event.getEmail())
                    .build());
                break;
            case "UserDeleted":
                userCacheRepository.deleteByUserId(event.getUserId());
                break;
        }
    }
}

// Now Order Service can JOIN with its own user_cache table
// No network call to User Service for every order query!
```

**Option 3 — CQRS Query Service (see Pattern #7 below):**

```
Events from all services → Kafka → Query Service → Denormalized Read DB
Dashboard queries go to Query Service — single source, fast reads
```

**Choosing the Right DB per Service:**

| Service | Best Database | Why |
|---------|-------------|-----|
| User Service | PostgreSQL | Relational, ACID, complex queries |
| Product Catalog | MongoDB | Flexible schema, nested categories |
| Order Service | PostgreSQL | Transactions, joins, financial data |
| Search Service | Elasticsearch | Full-text search, faceted queries |
| Analytics Service | ClickHouse / TimescaleDB | Columnar, time-series aggregations |
| Session Service | Redis | In-memory, TTL, fast reads |
| Notification Log | Cassandra | High-write throughput, time-ordered |

**Real-World Scenario:**
In an e-commerce platform, Order Service needed the customer's name and address for displaying order history. Instead of calling User Service for every order list query (N+1 problem), we replicated `customer_name` and `shipping_address` into the Order Service's database via Kafka events. The order list page loaded in 50ms instead of 800ms — all data was local.

**Common Mistakes:**
- "Temporarily" sharing a database between two services — it becomes permanent and creates tight coupling.
- Not handling eventual consistency in the UI — when User Service updates a name, Order Service's cache is stale for a few seconds. Show "processing" or accept the brief inconsistency.
- Over-fetching via API Composition — calling 10 services to assemble one response. Use CQRS with a denormalized read store instead.
- Choosing the wrong database — using MongoDB for financial transactions or PostgreSQL for full-text search.

**Closing:**
Database per service is a non-negotiable microservices principle. It enables independent deployment, scaling, and technology selection. For cross-service data: API Composition for simple cases, event-driven replication for frequent reads, CQRS for complex queries. The key is accepting eventual consistency and designing the UI for it.

---

## 6. Event-Driven Architecture (EDA)

---

### What is Event-Driven Architecture and when do you use it instead of REST?

**Summary:**
Event-Driven Architecture is where services communicate by publishing and consuming events through a message broker (Kafka/RabbitMQ) instead of making synchronous HTTP calls. The producer doesn't know or care who consumes the event. I use EDA when services don't need an immediate response, when I need loose coupling, and when I need to broadcast state changes to multiple consumers.

**Synchronous REST vs Event-Driven:**

```
SYNCHRONOUS (REST):
Order Service ──HTTP──▶ Payment Service ──HTTP──▶ Inventory Service
  │                         │                         │
  │ Waits for response...   │ Waits for response...   │
  ◀─────────────────────────◀─────────────────────────┘
  ❌ Coupled — Order knows about Payment, Inventory
  ❌ Slow — sequential calls
  ❌ Fragile — if Inventory is down, entire flow fails

ASYNCHRONOUS (Event-Driven):
Order Service → publishes "OrderPlaced" → Kafka
                                            │
                           ┌────────────────┼────────────────┐
                           ▼                ▼                ▼
                     Payment Service   Inventory Service   Notification Service
                     (consumes)        (consumes)          (consumes)
  ✅ Decoupled — Order doesn't know about consumers
  ✅ Fast — Order returns immediately after publishing
  ✅ Resilient — if Notification is down, it catches up later
  ✅ Extensible — add Analytics consumer without changing Order
```

**When to Use Each:**

| Use REST When | Use Events When |
|--------------|-----------------|
| Need immediate response | Fire-and-forget |
| Query/Read operations | State change notifications |
| Client needs the result now | Multiple consumers need the data |
| Simple request-response | Temporal decoupling (process later) |
| External-facing APIs | Internal service communication |

**Implementation with Spring Boot + Kafka:**

**Producer — Publishing Events:**

```java
// Event class
@Data
@Builder
public class OrderCreatedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private Long orderId;
    private Long customerId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
}

// Producer using Transactional Outbox Pattern
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;

    public Order createOrder(OrderRequest request) {
        // 1. Save order in DB
        Order order = orderRepository.save(Order.from(request));

        // 2. Write event to outbox table (SAME transaction as order)
        outboxRepository.save(OutboxEvent.builder()
            .id(UUID.randomUUID().toString())
            .aggregateType("Order")
            .aggregateId(order.getId().toString())
            .eventType("OrderCreated")
            .payload(toJson(OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OrderCreated")
                .timestamp(LocalDateTime.now())
                .orderId(order.getId())
                .customerId(request.getCustomerId())
                .items(request.getItems())
                .totalAmount(order.getTotalAmount())
                .build()))
            .build());

        // 3. Debezium CDC or polling publisher reads outbox → publishes to Kafka
        // This guarantees: if order is saved, event WILL be published (eventually)

        return order;
    }
}
```

**Why Outbox Pattern — Not Direct Kafka Publish?**

```
❌ DANGEROUS — Direct publish:
  1. orderRepository.save(order);     // DB commit ✅
  2. kafkaTemplate.send("orders", event); // App crashes here! ❌
  // Order exists but event was NEVER published — inconsistency!

✅ SAFE — Outbox pattern:
  1. orderRepository.save(order);     // DB commit ✅
  2. outboxRepository.save(event);    // SAME transaction ✅
  // Debezium CDC reads the outbox table → publishes to Kafka
  // If app crashes, Debezium picks up unpublished events later
```

**Consumer — Processing Events:**

```java
@Service
@Slf4j
public class PaymentEventHandler {

    private final PaymentService paymentService;
    private final ProcessedEventRepository processedEventRepo;

    @KafkaListener(
        topics = "order-events",
        groupId = "payment-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        // IDEMPOTENCY CHECK — prevent duplicate processing
        if (processedEventRepo.existsByEventId(event.getEventId())) {
            log.info("Event {} already processed, skipping", event.getEventId());
            return;
        }

        // Process
        PaymentResult result = paymentService.charge(
            event.getCustomerId(),
            event.getTotalAmount()
        );

        // Mark as processed (in SAME transaction)
        processedEventRepo.save(new ProcessedEvent(
            event.getEventId(), LocalDateTime.now()
        ));

        // Publish result event
        kafkaTemplate.send("payment-events", PaymentCompletedEvent.builder()
            .orderId(event.getOrderId())
            .transactionId(result.getTransactionId())
            .status(result.isSuccess() ? "COMPLETED" : "FAILED")
            .build());

        log.info("Processed payment for order {}: {}", event.getOrderId(), result.getStatus());
    }
}
```

**Kafka Configuration:**

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: payment-service
      auto-offset-reset: earliest
      enable-auto-commit: false     # manual commit for at-least-once
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.app.events"
        max.poll.records: 100
        max.poll.interval.ms: 300000
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all                     # wait for all replicas
      retries: 3
      properties:
        enable.idempotence: true    # prevent duplicate messages
```

**Kafka vs RabbitMQ — When to Use Each:**

| Feature | Kafka | RabbitMQ |
|---------|-------|----------|
| Model | Log-based (append-only) | Queue-based (consume & delete) |
| Retention | Configurable (days/forever) | Until consumed |
| Replay | Yes (re-read from any offset) | No (once consumed, gone) |
| Throughput | Millions/sec | Thousands/sec |
| Ordering | Per partition | Per queue |
| Consumer groups | Native (parallel consumers) | Competing consumers |
| Best for | Event streaming, audit, replay | Task queues, routing, RPC |
| Use case | Order events, CDC, analytics | Email jobs, notifications, work queues |

**Dead Letter Queue (DLQ) — Handling Failed Events:**

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, Object>
        kafkaListenerContainerFactory() {

    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());

    // Retry 3 times, then send to DLQ
    factory.setCommonErrorHandler(new DefaultErrorHandler(
        new DeadLetterPublishingRecoverer(kafkaTemplate),
        new FixedBackOff(1000L, 3)  // 1s between retries, max 3
    ));

    return factory;
}

// Monitor DLQ
@KafkaListener(topics = "order-events.DLT", groupId = "dlq-monitor")
public void handleDLQ(ConsumerRecord<String, String> record) {
    log.error("Dead letter event: topic={}, value={}", record.topic(), record.value());
    alertingService.sendAlert("DLQ event received: " + record.value());
    // Manual review and reprocessing needed
}
```

**Real-World Scenario:**
In an e-commerce platform, when a user placed an order, we published an `OrderPlaced` event. Five services consumed it independently: Payment (charge card), Inventory (reserve stock), Notification (send email), Analytics (track conversion), and Recommendation Engine (update model). Adding the Recommendation Engine required zero changes to the Order Service — we just deployed a new consumer. When the Notification Service was down for maintenance, it caught up with all missed events when it restarted — no lost emails.

**Common Mistakes:**
- Publishing events directly to Kafka after DB commit — use the Outbox pattern to guarantee at-least-once delivery.
- Not making consumers idempotent — network retries and rebalances cause duplicate delivery. Always deduplicate by event ID.
- Using events for queries — "get me user #123" should be REST. Events are for state changes ("user #123 updated their address").
- Ignoring consumer lag — if a consumer falls behind by millions of messages, you need monitoring and alerts (Burrow, Grafana).
- Not handling out-of-order events — Kafka guarantees order within a partition, but if you have multiple partitions, events for different entities may arrive out of order.

**Closing:**
Event-Driven Architecture is the backbone of scalable microservices. Use it for state changes, multi-consumer notifications, and temporal decoupling. REST for queries and synchronous needs. Always combine with the Outbox pattern for reliability, idempotent consumers for safety, and DLQ for failed events.

---

## 7. CQRS (Command Query Responsibility Segregation)

---

### What is CQRS and when should you use it?

**Summary:**
CQRS separates the write model (commands) from the read model (queries) into different services, models, and often different databases. Writes go to a normalized relational DB optimized for consistency. Reads go to a denormalized DB optimized for query performance. The read model is updated asynchronously via events. I use CQRS when read and write patterns are fundamentally different — which they are in most enterprise applications.

**Why Separate Reads and Writes?**

```
TRADITIONAL (Same Model):
  Write: INSERT INTO orders (customer_id, product_id, qty, price) VALUES (...)
  Read:  SELECT o.*, c.name, c.email, p.name, p.image
         FROM orders o
         JOIN customers c ON o.customer_id = c.id
         JOIN products p ON o.product_id = p.id
         JOIN payments pay ON pay.order_id = o.id
         WHERE c.id = 123 ORDER BY o.created_at DESC
  ❌ Complex joins across multiple tables
  ❌ Read performance degrades with data growth
  ❌ Can't independently scale reads and writes
  ❌ Same schema serves two different needs poorly

CQRS (Separate Models):
  Write: OrderService → normalized PostgreSQL (orders, line_items tables)
  Read:  QueryService → denormalized Elasticsearch or materialized view

  Write DB:                           Read DB:
  ┌─────────┐  ┌──────────┐         ┌──────────────────────────────┐
  │ orders   │  │ items    │         │ order_views                  │
  │ id       │  │ id       │   ──▶   │ order_id                     │
  │ cust_id  │  │ order_id │ events  │ customer_name                │
  │ status   │  │ prod_id  │         │ customer_email               │
  │ total    │  │ qty      │         │ product_names (JSON array)   │
  └─────────┘  │ price    │         │ total_amount                 │
               └──────────┘         │ payment_status               │
                                    │ created_at                   │
   Normalized, ACID                 └──────────────────────────────┘
   3NF, foreign keys                 Denormalized, fast reads
                                     No joins needed!
```

**CQRS Architecture:**

```
┌──────────────────────────────────────────────────────────┐
│                       CLIENTS                             │
│    POST /api/orders (command)    GET /api/orders (query)  │
└──────────┬──────────────────────────────┬────────────────┘
           │                              │
           ▼                              ▼
┌──────────────────┐          ┌──────────────────────┐
│ COMMAND SERVICE   │          │ QUERY SERVICE         │
│ (Write Side)      │          │ (Read Side)           │
│                   │          │                       │
│ Validates         │          │ Fast, denormalized    │
│ Business rules    │          │ No business logic     │
│ Writes to DB      │          │ Pre-computed views    │
│ Publishes events  │          │                       │
└────────┬──────────┘          └──────────┬────────────┘
         │                                │
         ▼                                ▼
┌──────────────────┐          ┌──────────────────────┐
│ PostgreSQL        │          │ Elasticsearch         │
│ (Write-optimized) │──events─▶│ (Read-optimized)      │
│ Normalized        │  Kafka   │ Denormalized          │
│ ACID transactions │          │ Fast full-text search  │
└──────────────────┘          └──────────────────────┘
```

**Implementation — Command Side (Writes):**

```java
// Command — represents user intent
@Data
@Builder
public class CreateOrderCommand {
    private Long customerId;
    private List<OrderItemDto> items;
    private PaymentDetails paymentDetails;
    private ShippingAddress shippingAddress;
}

// Command Handler
@Service
@Transactional
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final OrderValidator validator;
    private final PricingService pricingService;

    public Long createOrder(CreateOrderCommand command) {
        // 1. Validate
        validator.validate(command);

        // 2. Apply business rules
        BigDecimal total = pricingService.calculateTotal(command.getItems());
        if (total.compareTo(BigDecimal.valueOf(10000)) > 0) {
            throw new OrderLimitExceededException("Order exceeds $10,000 limit");
        }

        // 3. Create and save (write-optimized, normalized)
        Order order = Order.builder()
            .customerId(command.getCustomerId())
            .status(OrderStatus.CREATED)
            .totalAmount(total)
            .shippingAddress(command.getShippingAddress())
            .createdAt(LocalDateTime.now())
            .build();

        List<OrderItem> items = command.getItems().stream()
            .map(dto -> OrderItem.builder()
                .productId(dto.getProductId())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .build())
            .toList();

        order.setItems(items);
        Order saved = orderRepository.save(order);

        // 4. Publish event for read model update
        outboxRepository.save(OutboxEvent.of(
            "Order", saved.getId().toString(), "OrderCreated",
            new OrderCreatedEvent(saved)
        ));

        return saved.getId();
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.cancel(); // business rule: can only cancel if not shipped

        orderRepository.save(order);

        outboxRepository.save(OutboxEvent.of(
            "Order", orderId.toString(), "OrderCancelled",
            new OrderCancelledEvent(orderId, LocalDateTime.now())
        ));
    }
}
```

**Implementation — Read Side (Queries):**

```java
// Read model — denormalized, pre-joined
@Document(indexName = "order-views")
@Data
public class OrderView {
    @Id
    private String orderId;
    private String customerName;
    private String customerEmail;
    private List<OrderItemView> items;  // embedded, no joins needed
    private BigDecimal totalAmount;
    private String status;
    private String paymentStatus;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
public class OrderItemView {
    private String productName;
    private String productImage;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}

// Projection Builder — listens to events, builds read model
@Service
@Slf4j
public class OrderViewProjection {

    private final OrderViewRepository orderViewRepository;  // Elasticsearch
    private final UserClient userClient;
    private final ProductClient productClient;

    @KafkaListener(topics = "order-events", groupId = "order-view-projection")
    public void handleOrderEvent(OrderEvent event) {
        switch (event.getType()) {
            case "OrderCreated" -> buildOrderView((OrderCreatedEvent) event);
            case "OrderCancelled" -> updateStatus(event.getOrderId(), "CANCELLED");
            case "OrderShipped" -> updateStatus(event.getOrderId(), "SHIPPED");
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "order-view-projection")
    public void handlePaymentEvent(PaymentEvent event) {
        orderViewRepository.findById(event.getOrderId().toString())
            .ifPresent(view -> {
                view.setPaymentStatus(event.getStatus());
                view.setUpdatedAt(LocalDateTime.now());
                orderViewRepository.save(view);
            });
    }

    private void buildOrderView(OrderCreatedEvent event) {
        // Enrich with data from other services
        UserDto user = userClient.getUser(event.getCustomerId());
        List<ProductDto> products = productClient.getProducts(
            event.getItems().stream().map(OrderItem::getProductId).toList()
        );

        OrderView view = OrderView.builder()
            .orderId(event.getOrderId().toString())
            .customerName(user.getName())
            .customerEmail(user.getEmail())
            .items(buildItemViews(event.getItems(), products))
            .totalAmount(event.getTotalAmount())
            .status("CREATED")
            .paymentStatus("PENDING")
            .shippingAddress(event.getShippingAddress())
            .createdAt(event.getTimestamp())
            .updatedAt(event.getTimestamp())
            .build();

        orderViewRepository.save(view);
        log.info("Order view created for order {}", event.getOrderId());
    }
}

// Query Service — fast reads, no joins, no business logic
@RestController
@RequestMapping("/api/orders")
public class OrderQueryController {

    private final OrderViewRepository orderViewRepository;

    @GetMapping("/customer/{customerId}")
    public List<OrderView> getCustomerOrders(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return orderViewRepository.findByCustomerIdOrderByCreatedAtDesc(
            customerId, PageRequest.of(page, size)
        );
        // Single query on denormalized data — no joins, sub-millisecond response
    }

    @GetMapping("/search")
    public List<OrderView> searchOrders(@RequestParam String query) {
        return orderViewRepository.searchByCustomerNameOrProductName(query);
        // Full-text search on Elasticsearch — something PostgreSQL can't do efficiently
    }
}
```

**When to Use CQRS:**

```
✅ USE CQRS when:
  → Read and write patterns are very different
  → Read requires data from multiple services (dashboard, reports)
  → Need to scale reads and writes independently
  → Read needs a different data store (Elasticsearch, Redis, ClickHouse)
  → Complex reporting queries slow down the write database

❌ DON'T USE CQRS when:
  → Simple CRUD application
  → Read and write models are similar
  → Strong consistency is required (reads must always be up-to-date)
  → Small team — operational overhead of two databases + event pipeline
```

**CQRS with Event Sourcing (Advanced):**

```
Regular CQRS:
  Write: Save current STATE to DB
  Read:  Denormalized projections

CQRS + Event Sourcing:
  Write: Save EVENTS (not state) to event store
  Read:  Replay events to build any projection

Event Store (append-only):
  | EventId | AggregateId | Type            | Data                           | Timestamp |
  |---------|-------------|-----------------|--------------------------------|-----------|
  | 1       | order-123   | OrderCreated    | {customer: 1, items: [...]}    | 10:00:01  |
  | 2       | order-123   | PaymentReceived | {transactionId: "txn-456"}     | 10:00:05  |
  | 3       | order-123   | ItemShipped     | {trackingId: "TRACK-789"}      | 10:05:00  |
  | 4       | order-123   | OrderDelivered  | {deliveredAt: "2026-03-16"}    | 11:30:00  |

  Current state = replay all events for order-123
  Can build ANY view by replaying events differently
  Full audit trail for free
```

**Real-World Scenario:**
In a healthcare platform, the patient record write model was normalized (demographics, conditions, medications in separate tables with proper constraints). The read model for the doctor's dashboard was a single denormalized document in Elasticsearch containing everything about the patient — demographics, recent vitals, active medications, upcoming appointments — all from different services. The write side handled 100 writes/sec with ACID transactions. The read side handled 10,000 queries/sec from Elasticsearch. CQRS let us scale them independently.

**Common Mistakes:**
- Using CQRS for a simple CRUD app — unnecessary complexity. Start without CQRS; add it when read/write patterns diverge.
- Not handling eventual consistency in the UI — after creating an order, the read model may not reflect it for 100-500ms. Show "Order submitted, processing..." or redirect with a slight delay.
- Not monitoring projection lag — if the read model falls behind the write model by millions of events, users see stale data. Alert on consumer lag.
- Rebuilding read models without a plan — when you change the read model schema, you need to replay all events. Plan for this from day one (keep events in Kafka with long retention or store in an event log).

**Closing:**
CQRS is the answer when a single model can't efficiently serve both reads and writes. Write side handles business rules and consistency. Read side handles query performance and flexibility. Connected by events. Combined with Event Sourcing, you get a complete audit trail and the ability to build any view from historical data. It's powerful but complex — use it when the problem demands it, not because it's architecturally elegant.

---

## Pattern Relationships — How They Work Together

```
┌─────────────────────────────────────────────────────────────────────┐
│                     CLIENT (Mobile / Web)                           │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│                      API GATEWAY (Pattern 1)                         │
│  Routes, JWT Auth, Rate Limiting, Circuit Breaker                    │
│  → Uses SERVICE DISCOVERY (Pattern 2) to find downstream services    │
└──────────┬─────────────────┬──────────────────┬─────────────────────┘
           │                 │                  │
           ▼                 ▼                  ▼
    ┌──────────┐     ┌──────────┐      ┌────────────┐
    │  Order   │     │ Payment  │      │  Product   │
    │ Service  │     │ Service  │      │  Service   │
    │          │     │          │      │            │
    │ Own DB   │     │ Own DB   │      │  Own DB    │  ← DATABASE PER
    │ (Pattern │     │ (Pattern │      │  (Pattern  │    SERVICE (5)
    │   5)     │     │   5)     │      │    5)      │
    └──┬───────┘     └──┬───────┘      └────────────┘
       │                │
       │    SAGA PATTERN (Pattern 4)
       │    Order Saga: create → pay → reserve → confirm
       │    Compensation: cancel ← refund ← release
       │                │
       ▼                ▼
    ┌──────────────────────────────────────────────┐
    │              KAFKA (Event Bus)                 │
    │         EVENT-DRIVEN (Pattern 6)              │
    │                                               │
    │  Topics: order-events, payment-events,        │
    │          inventory-events, user-events         │
    └──────────────────┬───────────────────────────┘
                       │
                       ▼
              ┌────────────────┐
              │  Query Service  │  ← CQRS (Pattern 7)
              │  (Read Model)   │
              │                 │
              │  Elasticsearch  │    Denormalized projections
              │  / Redis        │    Fast reads, no joins
              └────────────────┘

    All inter-service calls use CIRCUIT BREAKER (Pattern 3)
    → Timeout → Retry → Circuit Breaker → Fallback
```

---

## Quick Reference — Pattern Decision Guide

| Pattern | Problem It Solves | Key Technology | When to Use |
|---------|-------------------|---------------|-------------|
| **API Gateway** | Clients calling multiple services | Spring Cloud Gateway, Kong | Always (entry point for every microservice system) |
| **Service Discovery** | Hardcoded URLs, dynamic scaling | Eureka (VMs), K8s DNS (containers) | Always (services need to find each other) |
| **Circuit Breaker** | Cascading failures from downstream | Resilience4j | Always (every inter-service call should have one) |
| **Saga** | Distributed transactions across services | Kafka + Orchestrator | When multi-service transaction consistency is needed |
| **Database per Service** | Tight coupling via shared database | PostgreSQL, MongoDB, Elasticsearch | Always (core microservices principle) |
| **Event-Driven** | Synchronous coupling, broadcast needs | Kafka, RabbitMQ | When services don't need immediate response |
| **CQRS** | Complex queries across multiple services | Elasticsearch, Redis, ClickHouse | When read/write patterns diverge significantly |

---

## Common Interview Follow-Up Questions

**Q: "Which patterns are always needed vs. situational?"**
> **Always:** API Gateway, Service Discovery, Circuit Breaker, Database per Service — these are foundational. **Situational:** Saga (only for multi-service transactions), CQRS (only when read/write patterns differ significantly), Event-Driven (when async communication is appropriate).

**Q: "How do you handle eventual consistency?"**
> Accept it. Design the UI for it — show "processing" states, use optimistic UI updates, poll or push via WebSocket for completion status. In most cases, 100-500ms of inconsistency is invisible to users. For the rare cases requiring immediate consistency, use synchronous calls with compensating transactions.

**Q: "What's the biggest challenge with microservices?"**
> Distributed data management. The moment you split the database, you lose joins and transactions. Every pattern in this document — Saga, CQRS, Event-Driven, Database per Service — exists because of this single challenge. The second biggest is observability — distributed tracing, centralized logging, and metrics become essential.

**Q: "How do you decide when to break a monolith into microservices?"**
> I follow DDD bounded contexts. If two teams work on the same codebase and step on each other, if deployments require coordinating across teams, or if one module needs to scale independently — those are signals. I start by extracting the most independent, highest-change-frequency module first using the Strangler Fig pattern.
