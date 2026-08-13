# Full Stack Senior Interview Preparation (5+ YOE)
## Core Java + Spring Boot + Microservices + Angular + DB + Coding

---

# PART 1: Architecture & Migration

---

## Q1: Can you explain your end-to-end project architecture (Angular to MySQL)?

**Summary:**
In my current project, we follow a layered architecture — Angular as the SPA frontend communicating with Spring Boot microservices via REST APIs, secured with JWT, and persisting data in MySQL through JPA/Hibernate.

**How It Works:**

```
Angular (SPA) → API Gateway (Spring Cloud Gateway) → Spring Boot Microservices → MySQL
     ↓                    ↓                                  ↓
  Lazy-loaded         JWT Validation                   JPA / Hibernate
  Modules             Rate Limiting                    Connection Pool (HikariCP)
  HttpInterceptor     Load Balancing                   Flyway Migrations
```

- **Frontend (Angular 16+):** Modularized with lazy loading. `HttpInterceptor` attaches JWT tokens to every outgoing request. We use reactive forms, RxJS for async data, and Angular Material for UI.
- **API Gateway:** Spring Cloud Gateway handles routing, JWT validation, rate limiting, and cross-cutting concerns.
- **Microservices (Spring Boot 3.x):** Each service owns its domain — User Service, Order Service, Notification Service. They communicate via REST for sync calls and Kafka for async events.
- **Data Layer:** Each microservice has its own MySQL schema (Database-per-service pattern). We use Spring Data JPA with HikariCP connection pooling. Flyway handles schema versioning.
- **Security:** JWT-based stateless authentication. Angular stores the token in `HttpOnly` cookies or localStorage and passes it via `Authorization: Bearer` header.

**Real-World Example:**
When a user places an order — Angular sends a POST to the API Gateway → routes to Order Service → persists in MySQL → publishes a Kafka event → Notification Service consumes and sends email.

**Closing:**
This architecture gives us independent deployability, clear domain boundaries, and horizontal scalability — which is exactly what microservices are designed for.

---

## Q2: Why is Java 17 the minimum requirement for Spring Boot 3.x?

**Summary:**
Spring Boot 3.x mandates Java 17 because it's built on Spring Framework 6, which dropped support for older Java versions to leverage modern language features, performance improvements, and long-term support.

**Key Reasons:**

1. **Jakarta EE 9+ Migration:** Spring Boot 3 moved from `javax.*` to `jakarta.*` namespace, which requires Jakarta EE 9+ — and that ecosystem targets Java 17+.
2. **Language Features:** Java 17 brings sealed classes, pattern matching, records, text blocks, and enhanced switch — Spring Framework 6 internally uses these.
3. **LTS (Long-Term Support):** Java 17 is an LTS release (supported until 2029). Spring chose to align with a stable, well-supported baseline.
4. **Performance:** JVM improvements in Java 17 — better garbage collection (ZGC, Shenandoah), improved startup time, and lower memory footprint.
5. **Security:** Older Java versions have known CVEs. Java 17 has stronger encapsulation and security defaults.

**Real-World Impact:**
When we upgraded from Spring Boot 2.7 to 3.2, we first had to upgrade from Java 11 to 17. We refactored deprecated APIs, updated Dockerfiles, CI/CD pipelines, and tested thoroughly.

**Closing:**
Java 17 is not just a version bump — it's a strategic choice by the Spring team to modernize the entire ecosystem and benefit from 5+ years of JVM evolution.

---

## Q3: What specific challenges did you face with the Jakarta namespace migration (javax to jakarta)?

**Summary:**
The `javax` to `jakarta` migration was one of the most impactful changes in Spring Boot 3. Every import referencing `javax.persistence`, `javax.servlet`, `javax.validation` had to change to `jakarta.*`.

**Challenges We Faced:**

1. **Mass Import Changes:** Hundreds of files had `javax.*` imports. We used IDE refactoring tools and OpenRewrite recipes to automate this, but still needed manual review.
   ```java
   // Before
   import javax.persistence.Entity;
   import javax.validation.constraints.NotNull;

   // After
   import jakarta.persistence.Entity;
   import jakarta.validation.constraints.NotNull;
   ```

2. **Third-Party Library Incompatibility:** Many libraries (like older Swagger/SpringFox, Apache CXF, certain JDBC drivers) still used `javax.*` internally. We had to:
   - Replace SpringFox with SpringDoc OpenAPI
   - Upgrade Hibernate Validator, Jackson, etc.
   - Some libraries needed forked/patched versions

3. **Custom Filters and Servlets:** Our custom `javax.servlet.Filter` implementations broke. Every servlet-related class needed migration.

4. **Test Dependencies:** JUnit extensions, Mockito configurations, and integration test setups that relied on `javax` annotations needed updates.

5. **Runtime Errors, Not Compile Errors:** Some `javax` references were in XML configs, `persistence.xml`, or string-based references — these only failed at runtime.

**How We Handled It:**
We used **OpenRewrite** (`org.openrewrite.java.migrate.jakarta`) to automate 80% of changes. The remaining 20% was manual — especially XML configs and third-party library replacements. We created a migration checklist and ran extensive integration tests.

**Closing:**
The Jakarta migration is not just find-and-replace — it's a full ecosystem compatibility check. Planning and tooling like OpenRewrite saved us weeks of effort.

---

## Q4: How did you handle Hibernate compatibility and Spring Security changes during the upgrade?

**Summary:**
Hibernate 6 and Spring Security 6 introduced breaking changes in Spring Boot 3. Hibernate changed its ID generation strategy and query behavior, while Spring Security deprecated the `WebSecurityConfigurerAdapter` entirely.

**Hibernate Changes:**

1. **ID Generation Strategy Changed:**
   ```java
   // Hibernate 5 default: IDENTITY
   // Hibernate 6 default: SEQUENCE (even for MySQL)
   // Fix: Explicitly set strategy
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   ```
   Without this, Hibernate 6 tried to use sequences on MySQL, which failed.

2. **HQL/JPQL Changes:** Hibernate 6 has stricter query parsing. Some implicit joins and type casts that worked in Hibernate 5 broke.

3. **Type Mapping:** `java.sql.Date` and `java.util.Date` handling changed. We moved to `java.time.LocalDate` / `LocalDateTime` across the board.

**Spring Security Changes:**

```java
// OLD (Spring Security 5) — DEPRECATED
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()...
    }
}

// NEW (Spring Security 6) — Component-based
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        );
        return http.build();
    }
}
```

- `authorizeRequests()` → `authorizeHttpRequests()`
- `antMatchers()` → `requestMatchers()`
- `WebSecurityConfigurerAdapter` → `SecurityFilterChain` bean

**How We Handled It:**
We upgraded incrementally — first Spring Boot 2.7 (which had deprecation warnings pointing to new APIs), then to 3.x. This gave us a smooth migration path. We wrote integration tests for every security endpoint before upgrading.

**Closing:**
The key lesson — never do a big-bang upgrade. Use the deprecation phase in 2.7 as a bridge, fix warnings first, then jump to 3.x confidently.

---

# PART 2: Java 8 & Streams API

---

## Q5: Explain the internal working of the Stream API pipeline. How does Lazy Evaluation work?

**Summary:**
The Stream API works as a pipeline of operations. Intermediate operations are lazy — they don't execute until a terminal operation triggers the pipeline. This avoids unnecessary computation and enables short-circuit optimization.

**How It Works Internally:**

```
Source → [filter] → [map] → [sorted] → collect (terminal)
         ↑ lazy      ↑ lazy   ↑ lazy     ↑ triggers execution
```

1. **Source:** A stream is created from a collection, array, or generator.
2. **Intermediate Operations:** `filter()`, `map()`, `sorted()`, `distinct()` — these return a new Stream and do nothing immediately. They are recorded as a pipeline of operations.
3. **Terminal Operation:** `collect()`, `forEach()`, `count()`, `reduce()` — this triggers the actual processing.

**Lazy Evaluation in Action:**

```java
List<String> names = employees.stream()
    .filter(e -> {
        System.out.println("Filtering: " + e.getName());
        return e.getSalary() > 50000;
    })
    .map(e -> {
        System.out.println("Mapping: " + e.getName());
        return e.getName().toUpperCase();
    })
    .findFirst()   // terminal — short-circuits
    .orElse("NONE");
```

**Output (only processes until first match):**
```
Filtering: Alice    → salary 40000, skipped
Filtering: Bob      → salary 60000, passes
Mapping: Bob        → "BOB"
```
It **stops immediately** after finding the first match. Alice's map was never called, and remaining employees were never touched.

**Why This Matters:**
- **Performance:** On a list of 1 million employees, `findFirst()` might only process 5 elements.
- **No intermediate collections:** Without streams, you'd create a filtered list, then a mapped list — wasting memory. Streams process element-by-element through the pipeline.

**Closing:**
Lazy evaluation is the core performance advantage of Streams. It means "build the recipe first, cook only when someone orders." This makes streams efficient even on large datasets.

---

## Q6: What is the fundamental difference between Intermediate and Terminal operations?

**Summary:**
Intermediate operations transform the stream and are lazy (return a Stream). Terminal operations consume the stream, trigger execution, and produce a result or side effect. A stream can only have one terminal operation.

**Comparison:**

| Aspect | Intermediate | Terminal |
|--------|-------------|----------|
| Returns | Stream (allows chaining) | Non-stream result (List, int, void) |
| Execution | Lazy — nothing happens | Eager — triggers pipeline |
| Examples | `filter`, `map`, `sorted`, `distinct`, `flatMap` | `collect`, `forEach`, `count`, `reduce`, `findFirst` |
| Count | Multiple allowed | Only one per stream |
| Stream after | Still open | Closed (cannot reuse) |

**Key Point:**

```java
// This does NOTHING — no terminal operation
employees.stream()
    .filter(e -> e.getSalary() > 50000)
    .map(Employee::getName);
// Stream is created but never executed!

// This executes
List<String> names = employees.stream()
    .filter(e -> e.getSalary() > 50000)
    .map(Employee::getName)
    .collect(Collectors.toList()); // terminal triggers execution
```

**Common Mistake:**
Using `forEach()` for transformation instead of `map()` + `collect()`. `forEach` is a terminal operation meant for side effects (like logging), not for building new collections.

**Short-Circuit Terminals:** `findFirst()`, `findAny()`, `anyMatch()` — these don't process the entire stream. They stop as soon as the answer is known.

**Closing:**
Think of intermediate operations as "instructions on a conveyor belt" and terminal operations as "pressing the start button." Nothing moves until you press start.

---

## Q7: Coding Challenge — Stream to count employees, sort by salary (desc) then name (asc)?

**Summary:**
This is a classic stream problem combining counting, multi-field sorting, and collection.

**Solution:**

```java
public class Employee {
    private String name;
    private double salary;
    private String department;
    // constructors, getters
}

// Count employees
long totalCount = employees.stream().count();
System.out.println("Total Employees: " + totalCount);

// Sort by salary descending, then name ascending
List<Employee> sorted = employees.stream()
    .sorted(
        Comparator.comparingDouble(Employee::getSalary).reversed()
            .thenComparing(Employee::getName)
    )
    .collect(Collectors.toList());

// Print result
sorted.forEach(e ->
    System.out.println(e.getName() + " - " + e.getSalary())
);
```

**Output Example:**
```
Total Employees: 5
Bob - 90000.0
Charlie - 75000.0
Alice - 75000.0      ← same salary, sorted by name (A before C)
David - 60000.0
Eve - 50000.0
```

**Bonus — Group by department, count, and sort within each group:**

```java
Map<String, List<Employee>> grouped = employees.stream()
    .sorted(Comparator.comparingDouble(Employee::getSalary).reversed()
        .thenComparing(Employee::getName))
    .collect(Collectors.groupingBy(Employee::getDepartment));
```

**Common Mistake:**
Using `Comparator.comparingDouble(...).reversed().thenComparing(...)` — the `reversed()` only reverses the salary part, not the name. This is correct. But if you write `Comparator.comparing(...).thenComparing(...).reversed()`, it reverses BOTH — which is wrong.

**Closing:**
Multi-field sorting with Comparator chaining is a very common interview and real-world requirement. The key is understanding that `reversed()` placement matters.

---

# PART 3: Microservices & Design Patterns

---

## Q8: Which design patterns have you actually implemented in a Microservices context?

**Summary:**
In microservices, I've practically used Singleton, Factory, Builder, and Dependency Injection — not just theoretically but to solve real architectural problems like service creation, configuration management, and object construction.

**1. Singleton — Configuration & Connection Pooling**
```java
@Configuration
public class AppConfig {
    // Spring beans are singleton by default
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```
In microservices, `RestTemplate`, `WebClient`, `KafkaTemplate`, and config classes are all singletons. Creating multiple instances would waste connections and memory.

**2. Factory Pattern — Dynamic Service Selection**
```java
@Component
public class NotificationFactory {
    private final Map<String, NotificationService> serviceMap;

    public NotificationFactory(List<NotificationService> services) {
        serviceMap = services.stream()
            .collect(Collectors.toMap(NotificationService::getType, Function.identity()));
    }

    public NotificationService getService(String type) {
        return serviceMap.get(type); // "EMAIL", "SMS", "PUSH"
    }
}
```
Used when the notification type is dynamic — determined at runtime based on user preferences.

**3. Builder Pattern — Complex Object Construction**
```java
// Used heavily for DTOs, API responses, and test data
OrderResponse response = OrderResponse.builder()
    .orderId(order.getId())
    .status("CONFIRMED")
    .items(mapItems(order.getItems()))
    .timestamp(LocalDateTime.now())
    .build();
```
Lombok's `@Builder` makes this trivial. We use it for every DTO and response object — avoids telescoping constructors.

**4. Dependency Injection — The Backbone of Spring**
```java
@Service
@RequiredArgsConstructor  // Constructor injection via Lombok
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final KafkaProducer kafkaProducer;
}
```
Constructor injection is the preferred approach — makes dependencies explicit, supports immutability, and is testable with Mockito.

**Closing:**
These aren't just patterns from a textbook — they're the daily building blocks of production microservices. The key is knowing when each pattern solves a real problem versus when it's over-engineering.

---

## Q9: What is the Circuit Breaker pattern, and how does Resilience4j prevent cascading failures?

**Summary:**
The Circuit Breaker pattern prevents cascading failures in microservices by stopping calls to a failing downstream service. Resilience4j implements this with three states — Closed, Open, and Half-Open — acting like an electrical circuit breaker.

**How It Works:**

```
CLOSED (normal) → failure threshold exceeded → OPEN (reject all calls)
                                                    ↓
                                              wait duration
                                                    ↓
                                              HALF-OPEN (allow few test calls)
                                                    ↓
                                        success → CLOSED | failure → OPEN
```

- **Closed:** All calls go through. Failures are counted.
- **Open:** All calls are immediately rejected (fail-fast) with a fallback response. No network call is made.
- **Half-Open:** After a wait period, a few test requests are allowed. If they succeed → Closed. If they fail → Open again.

**Implementation with Resilience4j:**

```java
@Service
public class PaymentService {

    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    public PaymentResponse processPayment(PaymentRequest request) {
        return paymentClient.charge(request); // calls external payment API
    }

    private PaymentResponse paymentFallback(PaymentRequest request, Throwable t) {
        // Queue for retry, return pending status
        kafkaProducer.send("payment-retry-topic", request);
        return PaymentResponse.builder()
            .status("PENDING")
            .message("Payment queued for processing")
            .build();
    }
}
```

**application.yml Configuration:**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentService:
        slidingWindowSize: 10
        failureRateThreshold: 50       # Open after 50% failures
        waitDurationInOpenState: 30s    # Wait 30s before half-open
        permittedNumberOfCallsInHalfOpenState: 3
```

**Why It Prevents Cascading Failures:**
Without a circuit breaker, if Payment Service is down, Order Service keeps sending requests → threads pile up → Order Service becomes slow → API Gateway times out → Frontend hangs → entire system collapses. Circuit breaker cuts the chain immediately.

**Closing:**
In production microservices, Circuit Breaker is not optional — it's a survival mechanism. Combined with retries, timeouts, and bulkheads, Resilience4j gives you a complete fault-tolerance toolkit.

---

## Q10: Async vs. Sync — When would you choose Kafka/RabbitMQ over a standard REST call?

**Summary:**
Use synchronous REST when you need an immediate response and the caller must wait. Use asynchronous messaging (Kafka/RabbitMQ) when operations can be decoupled, you need reliability, or you want to prevent tight coupling between services.

**Decision Matrix:**

| Criteria | REST (Sync) | Kafka/RabbitMQ (Async) |
|----------|------------|----------------------|
| Response needed immediately? | Yes | No |
| Caller must wait for result? | Yes | No |
| Failure tolerance? | Low — fails if service is down | High — message persists in queue |
| Coupling | Tight — knows the endpoint | Loose — knows only the topic |
| Throughput | Limited by latency | High — millions of events/sec |
| Use case | GET user profile, login | Send email, process payment, audit log |

**When I Use REST:**
- User login/authentication (need immediate response)
- Fetching data to display on screen
- Validations that must happen before proceeding

**When I Use Kafka:**
- **Order placed → Send confirmation email** (user doesn't need to wait for email)
- **Audit logging** (every action logged asynchronously — zero impact on latency)
- **Data sync across services** (Order Service publishes event → Inventory Service consumes)
- **Spike handling** (Black Friday: 10K orders/sec → Kafka buffers → services process at their pace)

**Real-World Example:**
```java
// Sync — user needs immediate validation
OrderResponse order = orderService.createOrder(request); // REST call to inventory

// Async — email can be sent later
kafkaTemplate.send("order-events", new OrderCreatedEvent(order.getId()));

// NotificationService consumes when ready
@KafkaListener(topics = "order-events")
public void handleOrderCreated(OrderCreatedEvent event) {
    emailService.sendConfirmation(event);
}
```

**Kafka vs RabbitMQ:**
- **Kafka:** High throughput, log-based, replay capability, event sourcing. Best for event streaming.
- **RabbitMQ:** Traditional message broker, better for task queues, routing, and request-reply patterns.

**Closing:**
The rule of thumb — if the user is staring at a loading spinner waiting for the result, use REST. If the work can happen in the background, use messaging. Most production systems use both.

---

# PART 4: Spring Boot & Security

---

## Q11: Walk through the complete Spring MVC flow (from DispatcherServlet to DB)

**Summary:**
Every HTTP request in Spring Boot flows through a well-defined pipeline: Client → Filter Chain → DispatcherServlet → HandlerMapping → Controller → Service → Repository → Database, and back the same way.

**Complete Flow:**

```
Client (Angular)
    ↓ HTTP Request
[Tomcat / Embedded Server]
    ↓
[Filter Chain] → Security Filter, CORS Filter, Logging Filter
    ↓
[DispatcherServlet] → Front Controller (single entry point)
    ↓
[HandlerMapping] → Finds which Controller method handles this URL
    ↓
[HandlerAdapter] → Invokes the controller method
    ↓
[@RestController] → Receives request, validates input
    ↓
[@Service] → Business logic, transaction management
    ↓
[@Repository] → Spring Data JPA / Hibernate
    ↓
[Hibernate] → Generates SQL, manages entity lifecycle
    ↓
[HikariCP Connection Pool] → Provides DB connection
    ↓
[MySQL Database] → Executes query, returns result
    ↓ (Response travels back up)
[Entity → DTO mapping] → MapStruct / manual mapping
    ↓
[Jackson] → Serializes Java object to JSON
    ↓
[@RestController] → Returns ResponseEntity
    ↓
[DispatcherServlet] → Sends HTTP response
    ↓
Client receives JSON response
```

**Step-by-Step Detail:**

1. **DispatcherServlet:** The front controller. Every request hits this first. It delegates to the right handler.
2. **HandlerMapping:** Maps URL patterns to controller methods using `@RequestMapping`, `@GetMapping`, etc.
3. **Interceptors:** `preHandle()` runs before the controller (for logging, auth checks).
4. **Controller:** Receives the request, extracts `@PathVariable`, `@RequestBody`, calls the service layer.
5. **Service:** Contains business logic. `@Transactional` manages DB transactions here.
6. **Repository:** `JpaRepository` provides CRUD methods. Custom queries via `@Query`.
7. **Hibernate:** Converts entities to SQL, manages the persistence context (first-level cache).
8. **Response:** Entity is mapped to DTO, serialized to JSON by Jackson, and returned.

**Closing:**
Understanding this flow is critical for debugging — when a request fails, you can pinpoint exactly where in the pipeline the problem occurred. This is what separates a senior developer from a junior.

---

## Q12: How do @RestController, @Service, and @Repository differ in terms of Application Context?

**Summary:**
All three are Spring-managed beans registered in the Application Context, but they serve different layers, have different stereotype semantics, and `@Repository` has special exception translation that the others don't.

**Key Differences:**

| Annotation | Layer | Purpose | Special Behavior |
|-----------|-------|---------|-----------------|
| `@RestController` | Presentation | Handles HTTP requests, returns JSON | Combines `@Controller` + `@ResponseBody` |
| `@Service` | Business Logic | Contains business rules, orchestration | No special behavior — purely semantic |
| `@Repository` | Data Access | Interacts with database | **Exception Translation** — converts JDBC/JPA exceptions to Spring's `DataAccessException` |

**In the Application Context:**

```java
// All three are singleton beans by default
// Spring component scan picks them up and registers them

@RestController  // → registered as a bean, mapped to URL endpoints
@RequestMapping("/api/orders")
public class OrderController { }

@Service  // → registered as a bean, no extra processing
public class OrderService { }

@Repository  // → registered as a bean + PersistenceExceptionTranslationPostProcessor
public interface OrderRepository extends JpaRepository<Order, Long> { }
```

**Why @Repository is Special:**
```java
// Without @Repository — raw JDBC exception
org.hibernate.exception.ConstraintViolationException

// With @Repository — translated to Spring exception
org.springframework.dao.DataIntegrityViolationException
```
This means your service layer catches Spring exceptions, not vendor-specific ones — making your code database-agnostic.

**Common Mistake:**
Using `@Component` for everything. While it works, you lose the semantic clarity and `@Repository`'s exception translation. Always use the specific stereotype.

**Closing:**
These annotations aren't just labels — they define the architecture layer, enable specific post-processing, and make the codebase self-documenting. A senior developer respects the layered architecture.

---

## Q13: How have you implemented JWT and Role-Based Access Control (RBAC)?

**Summary:**
In our project, we use JWT for stateless authentication and Spring Security with role-based access control. The flow is: user logs in → server generates JWT with roles → Angular stores it → every request carries the token → Spring Security filter validates and authorizes.

**Complete Flow:**

```
Login: POST /api/auth/login (username, password)
    → AuthenticationManager verifies credentials
    → Generate JWT with claims (userId, roles, expiry)
    → Return JWT to Angular

Every Request: GET /api/orders
    → Angular HttpInterceptor adds "Authorization: Bearer <jwt>"
    → JwtAuthenticationFilter extracts and validates token
    → SecurityContext populated with user + roles
    → @PreAuthorize checks role
    → Controller processes request
```

**JWT Generation:**
```java
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).toList());

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
            .compact();
    }
}
```

**JWT Filter:**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            String username = jwtService.extractUsername(token);

            if (username != null && jwtService.isTokenValid(token)) {
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null,
                        jwtService.extractRoles(token));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
```

**RBAC — Role-Based Access:**
```java
@RestController
@RequestMapping("/api")
public class OrderController {

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<Order> getOrders() { ... }

    @DeleteMapping("/orders/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteOrder(@PathVariable Long id) { ... }
}
```

**Angular Interceptor:**
```typescript
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
    intercept(req: HttpRequest<any>, next: HttpHandler) {
        const token = localStorage.getItem('jwt');
        if (token) {
            req = req.clone({
                setHeaders: { Authorization: `Bearer ${token}` }
            });
        }
        return next.handle(req);
    }
}
```

**Closing:**
JWT + RBAC is the industry standard for securing microservices. The key decisions are — token expiry strategy, refresh tokens, and where to store the token on the frontend (HttpOnly cookies are more secure than localStorage).

---

# PART 5: Angular Frontend

---

## Q14: How do you optimize Angular performance using Lazy Loading and AOT compilation?

**Summary:**
Lazy loading reduces the initial bundle size by loading feature modules on demand. AOT compilation compiles templates at build time instead of runtime, eliminating the Angular compiler from the bundle and catching template errors early.

**Lazy Loading:**

```typescript
// app-routing.module.ts
const routes: Routes = [
    { path: '', component: HomeComponent },
    {
        path: 'orders',
        loadChildren: () => import('./orders/orders.module')
            .then(m => m.OrdersModule)  // loaded only when user navigates to /orders
    },
    {
        path: 'admin',
        loadChildren: () => import('./admin/admin.module')
            .then(m => m.AdminModule)   // admin module never loaded for regular users
    }
];
```

**Impact:**
- Without lazy loading: Single `main.js` bundle = 2.5MB (everything loaded upfront)
- With lazy loading: `main.js` = 800KB + `orders.chunk.js` = 400KB (loaded on demand)
- First page load is 3x faster

**AOT Compilation:**

| Aspect | JIT (Just-In-Time) | AOT (Ahead-Of-Time) |
|--------|----|----|
| When | Runtime (browser) | Build time (CI/CD) |
| Bundle size | Larger (includes Angular compiler) | Smaller (compiler excluded) |
| Error detection | Runtime | Build time |
| Rendering speed | Slower first render | Faster first render |
| Production? | No | Yes (default in `ng build`) |

```bash
# AOT is default for production builds
ng build --configuration=production
# This enables: AOT, tree shaking, minification, dead code elimination
```

**Other Optimizations We Use:**
- **OnPush Change Detection** — reduces unnecessary re-renders
- **trackBy in ngFor** — prevents DOM recreation
- **Virtual Scrolling** — for large lists (CDK `cdk-virtual-scroll-viewport`)
- **Preloading Strategy** — `PreloadAllModules` loads lazy modules in background after initial load

**Closing:**
Lazy loading and AOT are the two biggest performance wins in Angular. Lazy loading reduces what the user downloads, and AOT reduces what the browser has to compile. Both are enabled by default in production builds — but structuring your app into proper feature modules is on you.

---

## Q15: What is the difference between default Change Detection and OnPush strategy?

**Summary:**
Default change detection checks every component in the tree on every event. OnPush only checks a component when its `@Input` reference changes or an event originates from it — dramatically reducing unnecessary checks.

**Default Change Detection:**
```
Any event (click, HTTP response, setTimeout) triggers:
    → Check AppComponent
        → Check HeaderComponent
            → Check NavComponent
        → Check OrderListComponent (100 items)
            → Check OrderItemComponent × 100
        → Check FooterComponent
```
Every component is checked, even if nothing changed. For a page with 500 components, this is expensive.

**OnPush Change Detection:**
```typescript
@Component({
    selector: 'app-order-item',
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `<div>{{ order.name }} - {{ order.total }}</div>`
})
export class OrderItemComponent {
    @Input() order: Order;
}
```

**OnPush only triggers re-check when:**
1. `@Input()` reference changes (not mutation — must be new object)
2. An event originates from this component or its children
3. An `Observable` with `async` pipe emits a new value
4. You manually call `markForCheck()` or `detectChanges()`

**Critical Gotcha:**
```typescript
// This WILL NOT trigger OnPush change detection:
this.order.name = 'Updated'; // mutating same reference

// This WILL trigger:
this.order = { ...this.order, name: 'Updated' }; // new reference
```

**Real-World Impact:**
In our project, a dashboard page had 200+ components rendering real-time data. With default detection, every WebSocket message caused all 200 components to re-check. After switching to OnPush, only the affected components updated — UI went from janky to smooth.

**Closing:**
OnPush is not premature optimization — it's the recommended strategy for any component that receives data via `@Input`. Combined with immutable data patterns, it's the foundation of performant Angular apps.

---

## Q16: Why is trackBy important when rendering large lists in Angular?

**Summary:**
Without `trackBy`, Angular destroys and recreates the entire DOM for a list whenever the data changes. With `trackBy`, Angular tracks each item by a unique identifier and only updates the DOM elements that actually changed.

**Without trackBy:**
```html
<div *ngFor="let order of orders">
    {{ order.name }}
</div>
```
When the list changes (e.g., one item added), Angular:
1. Destroys ALL existing DOM elements
2. Recreates ALL DOM elements from scratch
3. For 1000 items → 1000 destroy + 1000 create = very slow

**With trackBy:**
```html
<div *ngFor="let order of orders; trackBy: trackByOrderId">
    {{ order.name }}
</div>
```
```typescript
trackByOrderId(index: number, order: Order): number {
    return order.id;
}
```
When the list changes, Angular:
1. Compares items by `id`
2. Only creates/destroys DOM for items that were added/removed
3. Existing items are reused — no DOM recreation

**Performance Difference:**

| Scenario (1000 items, 1 added) | Without trackBy | With trackBy |
|---|---|---|
| DOM operations | 1001 destroy + 1001 create | 1 create |
| Time | ~200ms | ~2ms |
| Flicker | Yes | No |

**When It Matters Most:**
- Lists refreshed via polling or WebSocket (every few seconds)
- Sorting/filtering that returns new array references
- Paginated lists where most items remain the same
- Lists with complex child components (forms, charts)

**Common Mistake:**
Using `index` as the track-by value — this defeats the purpose because when items are reordered or inserted, indices shift and Angular still recreates DOM.

```typescript
// BAD — index shifts when items are added/removed
trackByIndex(index: number): number { return index; }

// GOOD — unique, stable identifier
trackByOrderId(index: number, order: Order): number { return order.id; }
```

**Closing:**
`trackBy` is a one-line addition that can transform list rendering performance from unusable to smooth. It's a mandatory practice for any list that changes dynamically.

---

# PART 6: SQL — Performance & Optimization

---

## Q17 (9.1): How do you optimize slow SQL queries?

**Summary:**
Optimizing slow queries follows a systematic approach: identify the slow query, analyze the execution plan, check indexing, rewrite the query, and validate the improvement. It's not guesswork — it's data-driven.

**My Optimization Checklist:**

1. **Check the execution plan** (`EXPLAIN ANALYZE`) — see if it's doing a full table scan
2. **Add proper indexes** — on WHERE, JOIN, ORDER BY, GROUP BY columns
3. **Avoid SELECT *** — fetch only needed columns
4. **Rewrite subqueries as JOINs** — JOINs are usually faster
5. **Use pagination** — never fetch millions of rows at once
6. **Check for N+1 queries** — use `JOIN FETCH` in JPA
7. **Partition large tables** — range or hash partitioning
8. **Use query caching** — Spring Cache or Redis for read-heavy queries
9. **Denormalize for read performance** — materialized views or summary tables

**Real-World Example:**
```sql
-- SLOW: Full table scan (no index on department)
SELECT * FROM employees WHERE department = 'Engineering' ORDER BY salary DESC;
-- Execution time: 2.3 seconds (500K rows)

-- Fix 1: Add composite index
CREATE INDEX idx_dept_salary ON employees(department, salary DESC);

-- Fix 2: Select only needed columns
SELECT id, name, salary FROM employees
WHERE department = 'Engineering'
ORDER BY salary DESC
LIMIT 20;
-- Execution time: 12ms
```

**Closing:**
Query optimization is 80% indexing and query rewriting, 15% schema design, and 5% database tuning. Always start with `EXPLAIN` — never optimize blind.

---

## Q18 (9.2): How do you identify slow queries in production?

**Summary:**
In production, I use MySQL's slow query log, Spring Boot Actuator metrics, and APM tools to identify queries that exceed acceptable response times.

**Methods I've Used:**

1. **MySQL Slow Query Log:**
```sql
-- Enable slow query log
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;  -- log queries > 1 second
SET GLOBAL slow_query_log_file = '/var/log/mysql/slow.log';

-- Analyze with mysqldumpslow
mysqldumpslow -t 10 /var/log/mysql/slow.log
```

2. **Spring Boot / Hibernate Logging:**
```yaml
# application.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        generate_statistics: true
logging:
  level:
    org.hibernate.stat: DEBUG
    org.hibernate.SQL: DEBUG
```

3. **P6Spy / Datasource Proxy:** Logs actual SQL with bind parameters and execution time.

4. **APM Tools (Production):**
   - **New Relic / Datadog:** Automatically tracks DB queries, shows slow ones in dashboards
   - **Spring Boot Actuator + Micrometer:** Exposes metrics on query duration
   - Custom logging with `StopWatch`:
```java
StopWatch sw = new StopWatch();
sw.start();
List<Order> orders = orderRepository.findByStatus("PENDING");
sw.stop();
if (sw.getTotalTimeMillis() > 500) {
    log.warn("Slow query: findByStatus took {}ms", sw.getTotalTimeMillis());
}
```

**Closing:**
In production, you need proactive monitoring — not waiting for users to complain. Slow query logs + APM tools are your first line of defense.

---

## Q19 (9.3): What is an EXPLAIN plan and how do you read it?

**Summary:**
`EXPLAIN` shows how MySQL will execute a query — which indexes it uses, how many rows it scans, and the join strategy. Reading it tells you exactly why a query is slow and how to fix it.

**Example:**
```sql
EXPLAIN SELECT e.name, d.department_name
FROM employees e
JOIN departments d ON e.dept_id = d.id
WHERE e.salary > 50000;
```

**Key Columns to Read:**

| Column | What It Tells You | Good vs Bad |
|--------|------------------|-------------|
| `type` | Join type | `const` > `eq_ref` > `ref` > `range` > `index` > **`ALL` (full scan = BAD)** |
| `key` | Index used | Should NOT be NULL |
| `rows` | Estimated rows scanned | Lower is better |
| `Extra` | Additional info | `Using index` = good, `Using filesort` = bad, `Using temporary` = bad |
| `filtered` | % of rows after filter | Higher is better (100% = perfect filter) |

**Red Flags:**
- `type: ALL` → Full table scan. Add an index.
- `key: NULL` → No index used. Check WHERE/JOIN columns.
- `Extra: Using filesort` → Can't use index for sorting. Add composite index matching ORDER BY.
- `Extra: Using temporary` → Temp table for GROUP BY. Review query structure.
- `rows: 500000` when you expect 50 → Index not selective enough.

**EXPLAIN ANALYZE (MySQL 8+):**
```sql
EXPLAIN ANALYZE SELECT ...;
-- Shows ACTUAL execution time and rows, not just estimates
```

**Closing:**
`EXPLAIN` is the X-ray for SQL queries. A senior developer doesn't guess about performance — they read the execution plan, identify the bottleneck, and fix it surgically.

---

## Q20 (9.4): Difference between EXISTS vs IN

**Summary:**
`EXISTS` checks for the existence of rows (returns TRUE/FALSE early), while `IN` compares against a list of values. `EXISTS` is generally faster for large subqueries because it short-circuits, while `IN` is simpler and faster for small, known lists.

**Comparison:**

```sql
-- Using IN: Evaluates the entire subquery first, then matches
SELECT * FROM employees e
WHERE e.dept_id IN (SELECT d.id FROM departments d WHERE d.location = 'NYC');

-- Using EXISTS: Stops at first match per row (correlated subquery)
SELECT * FROM employees e
WHERE EXISTS (
    SELECT 1 FROM departments d
    WHERE d.id = e.dept_id AND d.location = 'NYC'
);
```

| Aspect | IN | EXISTS |
|--------|-----|--------|
| Execution | Subquery runs once, builds list | Subquery runs per outer row, short-circuits |
| NULL handling | `IN` fails with NULLs in list | `EXISTS` handles NULLs correctly |
| Best when | Subquery returns small result set | Subquery returns large result set or outer table is small |
| Readability | More intuitive | Slightly more complex |

**When to Use What:**
- `IN` → Small, known list: `WHERE status IN ('ACTIVE', 'PENDING')`
- `EXISTS` → Checking relationship with large table: "Find employees who have at least one order"

**Closing:**
In practice, modern MySQL optimizers often convert `IN` to `EXISTS` internally. But understanding the difference matters for interviews and for edge cases where the optimizer doesn't optimize.

---

## Q21 (9.5): Which is faster: JOIN or Subquery? (and WHY)

**Summary:**
JOINs are generally faster than subqueries because the optimizer can choose efficient join algorithms (hash join, merge join, nested loop), while subqueries — especially correlated ones — may execute row-by-row.

**Why JOINs Win:**

```sql
-- Subquery (potentially slow — executed per row)
SELECT e.name, e.salary,
    (SELECT d.name FROM departments d WHERE d.id = e.dept_id) as dept_name
FROM employees e;

-- JOIN (optimized — single pass)
SELECT e.name, e.salary, d.name as dept_name
FROM employees e
JOIN departments d ON e.dept_id = d.id;
```

**The optimizer can:**
- Use hash joins or merge joins (not possible with correlated subqueries)
- Read both tables in a single pass
- Leverage indexes on join columns effectively

**When Subqueries Are OK:**
- `EXISTS` for existence checks (can be faster than LEFT JOIN + IS NOT NULL)
- Non-correlated subqueries in `WHERE ... IN (SELECT ...)` — optimizer often rewrites as join anyway
- When you need scalar results in SELECT

**Closing:**
Default to JOINs. Use subqueries only when they express the intent more clearly and you've verified performance is acceptable. Always check with `EXPLAIN`.

---

## Q22 (9.6): How to optimize queries on very large tables?

**Summary:**
For tables with millions/billions of rows, standard indexing isn't enough. You need partitioning, proper index design, query rewriting, and sometimes architectural changes like read replicas or caching.

**Strategies:**

1. **Table Partitioning:**
```sql
-- Range partition by date
ALTER TABLE orders PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);
-- Query only scans relevant partition
SELECT * FROM orders WHERE created_at >= '2025-01-01';
```

2. **Covering Indexes:**
```sql
-- Index covers all columns in the query — no table lookup needed
CREATE INDEX idx_covering ON orders(status, created_at, total_amount);
SELECT status, created_at, total_amount FROM orders WHERE status = 'SHIPPED';
```

3. **Keyset Pagination (instead of OFFSET):**
```sql
-- OFFSET is slow for deep pages
SELECT * FROM orders ORDER BY id LIMIT 20 OFFSET 1000000; -- scans 1M rows

-- Keyset is fast regardless of page depth
SELECT * FROM orders WHERE id > 1000000 ORDER BY id LIMIT 20; -- index seek
```

4. **Read Replicas:** Route read-heavy queries to replicas, writes to primary.
5. **Archiving:** Move old data to archive tables. Keep active table lean.
6. **Summary Tables / Materialized Views:** Pre-compute aggregations for dashboards.

**Closing:**
Large table optimization is about reducing the dataset the query touches. Partitioning, keyset pagination, and covering indexes are the three most impactful strategies.

---

## Q23 (9.7): Pagination — OFFSET vs Keyset pagination

**Summary:**
OFFSET-based pagination is simple but degrades with deep pages because it scans and discards rows. Keyset (cursor-based) pagination uses a WHERE clause with the last seen value, making it consistently fast regardless of page number.

**OFFSET Pagination:**
```sql
-- Page 1: Fast
SELECT * FROM orders ORDER BY id LIMIT 20 OFFSET 0;

-- Page 50,000: SLOW — scans 1,000,000 rows, returns 20
SELECT * FROM orders ORDER BY id LIMIT 20 OFFSET 999980;
```
MySQL reads 1,000,000 rows, throws away 999,980, and returns 20. Execution time grows linearly with page depth.

**Keyset Pagination:**
```sql
-- Page 1
SELECT * FROM orders ORDER BY id LIMIT 20;
-- last_id = 20

-- Next page: fast — index seek directly to id > 20
SELECT * FROM orders WHERE id > 20 ORDER BY id LIMIT 20;
-- last_id = 40

-- Any depth — constant speed
SELECT * FROM orders WHERE id > 999980 ORDER BY id LIMIT 20;
```

| Aspect | OFFSET | Keyset |
|--------|--------|--------|
| Deep page speed | Degrades badly | Constant |
| Implementation | Simple | Slightly complex |
| Jump to page N? | Yes | No (sequential only) |
| Use case | Admin panels, small data | Infinite scroll, APIs, large data |

**In Spring Boot:**
```java
// OFFSET (Spring Data default)
Page<Order> orders = orderRepository.findAll(PageRequest.of(pageNum, 20));

// Keyset (custom)
@Query("SELECT o FROM Order o WHERE o.id > :lastId ORDER BY o.id")
List<Order> findNextPage(@Param("lastId") Long lastId, Pageable pageable);
```

**Closing:**
For user-facing APIs with large datasets, keyset pagination is the way to go. OFFSET is fine for admin dashboards with small datasets and "jump to page" requirements.

---

## Q24 (9.8): Why is SELECT * considered bad practice?

**Summary:**
`SELECT *` fetches all columns regardless of need, increasing network transfer, memory usage, and preventing index-only scans. In production systems with wide tables, this is a measurable performance hit.

**Why It's Bad:**

1. **Network overhead:** Table has 50 columns, you need 3. You're transferring 17x more data.
2. **Breaks covering indexes:** A covering index on `(status, name)` avoids table lookup. `SELECT *` forces a table lookup for remaining columns.
3. **Schema coupling:** If someone adds a BLOB column, your query now returns megabytes of data you don't need.
4. **Hibernate lazy loading:** `SELECT *` loads all eager fields including relationships — potential N+1 problem.

```sql
-- BAD
SELECT * FROM employees WHERE department = 'Engineering';
-- Returns: id, name, email, phone, address, photo_blob, bio_text, ...

-- GOOD
SELECT id, name, salary FROM employees WHERE department = 'Engineering';
-- Returns only what the UI needs
```

**Exception:** In development/debugging, `SELECT *` is fine. In production code — always specify columns.

**Closing:**
`SELECT *` is a lazy habit that costs real performance. Explicit column selection is a hallmark of production-quality SQL.

---

## Q25 (9.9): How do you handle millions of records efficiently?

**Summary:**
Handling millions of records requires a combination of batch processing, streaming, proper indexing, pagination, and sometimes architectural patterns like CQRS. You never load millions of records into memory at once.

**Strategies:**

1. **Batch Processing:**
```java
@Transactional
public void processLargeData() {
    int batchSize = 1000;
    int page = 0;
    List<Order> batch;

    do {
        batch = orderRepository.findByStatus("PENDING",
            PageRequest.of(page++, batchSize));
        batch.forEach(this::processOrder);
        entityManager.flush();
        entityManager.clear(); // prevent memory leak in persistence context
    } while (!batch.isEmpty());
}
```

2. **Stream API (Hibernate):**
```java
@Query("SELECT o FROM Order o WHERE o.status = :status")
Stream<Order> streamByStatus(@Param("status") String status);

// Process without loading all into memory
try (Stream<Order> stream = orderRepository.streamByStatus("PENDING")) {
    stream.forEach(this::processOrder);
}
```

3. **Spring Batch:** For scheduled ETL jobs — reader/processor/writer pattern with chunk-based processing.

4. **Bulk Operations:**
```sql
-- Don't update row by row
-- DO batch update
UPDATE orders SET status = 'ARCHIVED' WHERE created_at < '2024-01-01' LIMIT 10000;
-- Run in a loop until affected rows = 0
```

5. **CQRS Pattern:** Separate read and write models. Write to normalized tables, read from denormalized/pre-computed views.

**Closing:**
The golden rule — never `SELECT * FROM big_table` without `WHERE`, `LIMIT`, or pagination. Process in chunks, clear the persistence context, and use streaming for read-heavy operations.

---

# PART 7: SQL — Scenario-Based Questions

---

## Q26 (10.1): Fetch duplicate rows but keep only one

```sql
-- Find duplicates based on email
SELECT email, COUNT(*) as cnt
FROM employees
GROUP BY email
HAVING COUNT(*) > 1;

-- Fetch all duplicate rows with details (keep the one with lowest id)
SELECT * FROM employees e1
WHERE e1.id NOT IN (
    SELECT MIN(e2.id) FROM employees e2 GROUP BY e2.email
)
AND e1.email IN (
    SELECT email FROM employees GROUP BY email HAVING COUNT(*) > 1
);
```

---

## Q27 (10.2): Delete duplicate records safely

```sql
-- Keep the row with the lowest id, delete the rest
DELETE e1 FROM employees e1
INNER JOIN employees e2
ON e1.email = e2.email
AND e1.id > e2.id;

-- Alternative using CTE (MySQL 8+)
WITH duplicates AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY email ORDER BY id) as rn
    FROM employees
)
DELETE FROM employees WHERE id IN (
    SELECT id FROM duplicates WHERE rn > 1
);
```

**Safety tip:** Always run the SELECT version first to verify which rows will be deleted. Use a transaction so you can rollback.

---

## Q28 (10.3): Swap two column values without temp variable

```sql
-- Swap first_name and last_name
UPDATE employees
SET first_name = last_name, last_name = first_name;
```
In MySQL, a single UPDATE statement evaluates the right side using the **original** values before applying any changes. So this swap works atomically — no temp variable needed.

---

## Q29 (10.4): Find missing IDs in a sequence

```sql
-- Using a recursive CTE to generate the full sequence
WITH RECURSIVE seq AS (
    SELECT MIN(id) as id FROM orders
    UNION ALL
    SELECT id + 1 FROM seq WHERE id < (SELECT MAX(id) FROM orders)
)
SELECT s.id as missing_id
FROM seq s
LEFT JOIN orders o ON s.id = o.id
WHERE o.id IS NULL;

-- Simpler approach: Find gaps
SELECT a.id + 1 as missing_start
FROM orders a
LEFT JOIN orders b ON a.id + 1 = b.id
WHERE b.id IS NULL AND a.id < (SELECT MAX(id) FROM orders);
```

---

## Q30 (10.5): Fetch last 10 records without using LIMIT

```sql
-- Using ROW_NUMBER (MySQL 8+)
SELECT * FROM (
    SELECT *, ROW_NUMBER() OVER (ORDER BY id DESC) as rn
    FROM employees
) ranked
WHERE rn <= 10;

-- Using subquery
SELECT * FROM employees
WHERE id >= (SELECT MAX(id) - 9 FROM employees)
ORDER BY id;
-- Note: This assumes sequential IDs with no gaps
```

---

## Q31 (10.6): Employees who never got a salary hike

```sql
-- Employees with same salary in all records
SELECT employee_id, employee_name
FROM salary_history
GROUP BY employee_id, employee_name
HAVING MAX(salary) = MIN(salary);

-- Using NOT EXISTS
SELECT DISTINCT e.id, e.name
FROM employees e
WHERE NOT EXISTS (
    SELECT 1 FROM salary_history s1
    JOIN salary_history s2 ON s1.employee_id = s2.employee_id
    WHERE s1.employee_id = e.id
    AND s2.salary > s1.salary
    AND s2.effective_date > s1.effective_date
);
```

---

## Q32 (10.7): Find highest salary without using MAX()

```sql
-- Using ORDER BY + LIMIT
SELECT salary FROM employees ORDER BY salary DESC LIMIT 1;

-- Without LIMIT — using subquery
SELECT DISTINCT salary FROM employees e1
WHERE NOT EXISTS (
    SELECT 1 FROM employees e2 WHERE e2.salary > e1.salary
);

-- Using ALL
SELECT DISTINCT salary FROM employees
WHERE salary >= ALL (SELECT salary FROM employees);
```

---

## Q33 (10.8): Records common in two tables

```sql
-- Using INNER JOIN
SELECT a.* FROM table_a a
INNER JOIN table_b b ON a.id = b.id;

-- Using INTERSECT (MySQL 8.0.31+)
SELECT * FROM table_a
INTERSECT
SELECT * FROM table_b;

-- Using EXISTS
SELECT * FROM table_a a
WHERE EXISTS (SELECT 1 FROM table_b b WHERE b.id = a.id);
```

---

## Q34 (10.9): Records unique to each table

```sql
-- Records in A but NOT in B
SELECT a.* FROM table_a a
LEFT JOIN table_b b ON a.id = b.id
WHERE b.id IS NULL;

-- Records in B but NOT in A
SELECT b.* FROM table_b b
LEFT JOIN table_a a ON b.id = a.id
WHERE a.id IS NULL;

-- All unique records (symmetric difference)
SELECT * FROM table_a WHERE id NOT IN (SELECT id FROM table_b)
UNION ALL
SELECT * FROM table_b WHERE id NOT IN (SELECT id FROM table_a);

-- Using EXCEPT (MySQL 8.0.31+)
SELECT * FROM table_a EXCEPT SELECT * FROM table_b;
```

---

## Q35 (10.10): Consecutive login days problem

```sql
-- Find users with 3+ consecutive login days
WITH login_groups AS (
    SELECT user_id, login_date,
        login_date - INTERVAL ROW_NUMBER() OVER (
            PARTITION BY user_id ORDER BY login_date
        ) DAY as grp
    FROM (SELECT DISTINCT user_id, DATE(login_time) as login_date FROM logins) daily
)
SELECT user_id, MIN(login_date) as streak_start,
       MAX(login_date) as streak_end,
       COUNT(*) as consecutive_days
FROM login_groups
GROUP BY user_id, grp
HAVING COUNT(*) >= 3
ORDER BY consecutive_days DESC;
```

**How it works:** Subtracting a sequential row number from the date creates a constant value for consecutive dates. Group by that constant to find streaks.

---

# PART 8: SQL in Real Projects (Java / Spring Boot)

---

## Q36 (11.1): How is SQL used in your current project?

**Summary:**
In my project, SQL is used through Spring Data JPA for standard CRUD, custom JPQL/native queries for complex business logic, Flyway for schema migrations, and direct SQL for reporting/analytics dashboards.

**How We Use It:**

1. **Spring Data JPA Repositories:**
```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Derived query — Spring generates SQL
    List<Order> findByStatusAndCreatedAtAfter(String status, LocalDateTime date);

    // Custom JPQL for complex logic
    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.userId = :userId")
    List<Order> findOrdersWithItems(@Param("userId") Long userId);

    // Native SQL for performance-critical queries
    @Query(value = "SELECT * FROM orders WHERE status = 'PENDING' " +
           "AND created_at < NOW() - INTERVAL 24 HOUR", nativeQuery = true)
    List<Order> findStaleOrders();
}
```

2. **Flyway Migrations:**
```sql
-- V1__create_orders_table.sql
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status_created (status, created_at)
);
```

3. **Reporting:** Native queries or dedicated read-only datasource for dashboards, analytics, and exports.

**Closing:**
We let JPA handle 80% of the queries (simple CRUD), write JPQL for 15% (joins, complex filters), and use native SQL for the remaining 5% (performance-critical, database-specific features).

---

## Q37 (11.2): How do you handle DB transactions in Spring Boot?

**Summary:**
We use `@Transactional` from Spring for declarative transaction management. It wraps the method in a database transaction — if any exception occurs, the entire transaction rolls back, ensuring data consistency.

**Basic Usage:**
```java
@Service
public class OrderService {

    @Transactional
    public Order placeOrder(OrderRequest request) {
        Order order = orderRepository.save(new Order(request));
        inventoryService.deductStock(request.getItems());  // if this fails, order save also rolls back
        paymentService.charge(request.getPayment());
        return order;
    }
}
```

**Key Configurations:**

```java
// Read-only optimization (no dirty checking, can use read replica)
@Transactional(readOnly = true)
public List<Order> getOrders() { ... }

// Specific rollback rules
@Transactional(rollbackFor = Exception.class)  // rollback on checked exceptions too
public void processPayment() { ... }

// Propagation — nested transaction behavior
@Transactional(propagation = Propagation.REQUIRES_NEW)  // independent transaction
public void sendNotification() { ... }

// Isolation level
@Transactional(isolation = Isolation.READ_COMMITTED)
public void generateReport() { ... }
```

**Common Mistake — Self-Invocation:**
```java
@Service
public class OrderService {
    public void processAll() {
        for (Order order : orders) {
            processOne(order); // @Transactional is IGNORED here!
        }
    }

    @Transactional
    public void processOne(Order order) { ... }
}
```
`@Transactional` works through AOP proxies. Calling a `@Transactional` method from within the same class bypasses the proxy. **Fix:** Extract to a separate service or inject self-reference.

**Closing:**
`@Transactional` is deceptively simple. The common pitfalls — self-invocation, checked exceptions not rolling back by default, and propagation misunderstanding — are what interviewers test.

---

## Q38 (11.3): What is the N+1 problem and how did you fix it?

**Summary:**
The N+1 problem occurs when fetching a parent entity triggers N additional queries to load its children — one for the parent (1) and one for each child (N). In a list of 100 orders with items, you get 101 queries instead of 1 or 2.

**The Problem:**
```java
@Entity
public class Order {
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItem> items;
}

// This triggers N+1
List<Order> orders = orderRepository.findAll(); // 1 query: SELECT * FROM orders
for (Order order : orders) {
    order.getItems().size(); // N queries: SELECT * FROM order_items WHERE order_id = ?
}
// 100 orders = 101 queries!
```

**Solutions:**

1. **JOIN FETCH (Best for JPA):**
```java
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.status = :status")
List<Order> findOrdersWithItems(@Param("status") String status);
// Single query with JOIN — 1 query instead of 101
```

2. **@EntityGraph:**
```java
@EntityGraph(attributePaths = {"items"})
List<Order> findByStatus(String status);
```

3. **@BatchSize (Hibernate):**
```java
@OneToMany(mappedBy = "order")
@BatchSize(size = 20)  // loads items in batches of 20 instead of 1-by-1
private List<OrderItem> items;
// 100 orders = 1 + 5 queries (batches of 20) instead of 101
```

4. **DTO Projection (Best performance):**
```java
@Query("SELECT new com.app.dto.OrderSummary(o.id, o.status, i.productName, i.quantity) " +
       "FROM Order o JOIN o.items i WHERE o.userId = :userId")
List<OrderSummary> findOrderSummaries(@Param("userId") Long userId);
```

**How I Detected It:**
Enabled Hibernate statistics (`hibernate.generate_statistics=true`) and saw 500+ queries for a single API call. Used Spring Boot Actuator and P6Spy to log actual query counts.

**Closing:**
N+1 is the most common JPA performance killer. `JOIN FETCH` is the go-to fix. In every project I join, the first thing I check is query count per API call.

---

## Q39 (11.4): How indexing improved performance in your project?

**Summary:**
In our project, a key API was taking 3+ seconds due to full table scans on a 2-million-row orders table. After analyzing with `EXPLAIN` and adding targeted composite indexes, we brought it down to under 50ms.

**The Scenario:**
```sql
-- Dashboard API: "Show pending orders for the last 7 days, sorted by creation date"
SELECT id, user_id, total, status, created_at
FROM orders
WHERE status = 'PENDING'
AND created_at >= NOW() - INTERVAL 7 DAY
ORDER BY created_at DESC
LIMIT 20;
```

**Before Indexing:**
```
EXPLAIN: type=ALL, rows=2000000, Extra=Using where; Using filesort
Execution time: 3.2 seconds
```

**After Adding Composite Index:**
```sql
CREATE INDEX idx_orders_status_created ON orders(status, created_at DESC);
```
```
EXPLAIN: type=range, key=idx_orders_status_created, rows=1847, Extra=Using index condition
Execution time: 12ms
```

**Index Design Principles We Follow:**
1. **Leftmost prefix rule:** `INDEX(status, created_at)` supports queries filtering on `status` alone or `status + created_at`, but NOT `created_at` alone.
2. **Cardinality matters:** Put high-cardinality columns first in composite indexes.
3. **Don't over-index:** Each index slows down INSERT/UPDATE. We keep it under 5-6 indexes per table.
4. **Monitor unused indexes:** `SELECT * FROM sys.schema_unused_indexes;`

**Closing:**
Proper indexing is the single biggest performance lever for SQL. A well-placed composite index can turn a 3-second query into a 10ms query. But always validate with `EXPLAIN` — not all indexes are used.

---

## Q40 (11.5): Deadlock issue you faced and how you resolved it?

**Summary:**
We encountered a deadlock in production when two concurrent transactions tried to update the same rows in opposite order. MySQL detected the deadlock and killed one transaction. We fixed it by ensuring consistent lock ordering and reducing transaction scope.

**The Scenario:**
```
Transaction A:                          Transaction B:
UPDATE orders SET status='SHIPPED'      UPDATE inventory SET qty=qty-1
WHERE id=100;                           WHERE product_id=50;
  ↓ (holds lock on orders:100)            ↓ (holds lock on inventory:50)
UPDATE inventory SET qty=qty-1          UPDATE orders SET status='SHIPPED'
WHERE product_id=50;                    WHERE id=100;
  ↓ WAITING for inventory:50 lock        ↓ WAITING for orders:100 lock

💀 DEADLOCK — both waiting for each other
```

**How We Fixed It:**

1. **Consistent Lock Ordering:** Always acquire locks in the same order (orders first, then inventory):
```java
@Transactional
public void shipOrder(Long orderId) {
    // Always: Order first, then Inventory
    Order order = orderRepository.findByIdWithLock(orderId); // SELECT ... FOR UPDATE
    order.setStatus("SHIPPED");
    orderRepository.save(order);

    inventoryService.deductStock(order.getItems()); // locks inventory second
}
```

2. **Reduced Transaction Scope:**
```java
// BAD — long transaction holding locks
@Transactional
public void processOrder(Long orderId) {
    validate(orderId);          // 200ms
    callExternalApi();          // 500ms — holding DB lock during HTTP call!
    updateOrder(orderId);
}

// GOOD — minimize time locks are held
public void processOrder(Long orderId) {
    validate(orderId);          // no transaction
    ExternalResult result = callExternalApi();  // no transaction
    updateOrderInTransaction(orderId, result);  // short transaction
}
```

3. **Retry on Deadlock:**
```java
@Retryable(value = DeadlockLoserDataAccessException.class, maxAttempts = 3)
@Transactional
public void updateOrder(Long orderId) { ... }
```

**Closing:**
Deadlocks are inevitable in concurrent systems. The fix is always: consistent lock ordering, shortest possible transactions, and retry logic for the rare cases that still occur.

---

## Q41 (11.6): Pagination strategy used in real APIs?

**Summary:**
In our REST APIs, we use Spring Data's `Pageable` for standard OFFSET pagination with small datasets, and keyset/cursor-based pagination for large datasets and infinite scroll UIs.

**Standard Pagination (Spring Data):**
```java
@GetMapping("/orders")
public Page<OrderDTO> getOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Order> orders = orderRepository.findByStatus("ACTIVE", pageable);
    return orders.map(orderMapper::toDTO);
}
```

**API Response:**
```json
{
    "content": [...],
    "totalElements": 5420,
    "totalPages": 271,
    "number": 0,
    "size": 20,
    "first": true,
    "last": false
}
```

**Cursor-Based Pagination (for large datasets / infinite scroll):**
```java
@GetMapping("/feed")
public CursorPage<PostDTO> getFeed(@RequestParam(required = false) Long cursor,
                                    @RequestParam(defaultValue = "20") int limit) {
    List<Post> posts;
    if (cursor == null) {
        posts = postRepository.findTopPosts(PageRequest.of(0, limit + 1));
    } else {
        posts = postRepository.findPostsAfterId(cursor, PageRequest.of(0, limit + 1));
    }

    boolean hasNext = posts.size() > limit;
    if (hasNext) posts = posts.subList(0, limit);

    Long nextCursor = hasNext ? posts.get(posts.size() - 1).getId() : null;
    return new CursorPage<>(posts.stream().map(postMapper::toDTO).toList(), nextCursor);
}
```

**Closing:**
OFFSET pagination for admin panels and small datasets. Cursor pagination for user-facing feeds and large tables. The choice depends on the UX pattern and data volume.

---

## Q42 (11.7): Data consistency vs performance — how do you balance?

**Summary:**
In microservices, you can't have both perfect consistency and maximum performance. I use strong consistency for financial/critical operations and eventual consistency for non-critical flows — choosing the right trade-off per use case.

**The Spectrum:**

```
Strong Consistency ←————————————→ Eventual Consistency
(Slower, Safer)                    (Faster, Scalable)

Bank transfers          Order status          Analytics
Payment processing      Inventory count       Notifications
User authentication     Search index          Recommendation
```

**Strategies I Use:**

1. **Strong Consistency (ACID) — for critical data:**
```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public void transferMoney(Long from, Long to, BigDecimal amount) {
    Account sender = accountRepository.findByIdWithLock(from);
    Account receiver = accountRepository.findByIdWithLock(to);
    sender.debit(amount);
    receiver.credit(amount);
    // Both succeed or both fail — ACID guarantees
}
```

2. **Eventual Consistency (Saga Pattern) — for distributed operations:**
```java
// Order Service
public void placeOrder(OrderRequest request) {
    Order order = orderRepository.save(new Order(request));
    kafkaTemplate.send("order-created", new OrderCreatedEvent(order));
    // Inventory Service will eventually deduct stock
    // If it fails → compensating transaction (cancel order)
}
```

3. **Optimistic Locking — balance between safety and performance:**
```java
@Entity
public class Product {
    @Version
    private Long version; // auto-managed by Hibernate
    private Integer stock;
}
// Two concurrent updates — one succeeds, one gets OptimisticLockException → retry
```

4. **Caching with TTL — trade freshness for speed:**
```java
@Cacheable(value = "products", key = "#id")
public Product getProduct(Long id) {
    return productRepository.findById(id).orElseThrow();
}
// Cache expires after 5 min — stale data is acceptable for product catalog
```

**Closing:**
There's no one-size-fits-all answer. The key is understanding your business requirements — "can the user tolerate seeing stale data for 5 seconds?" If yes, use caching and eventual consistency. If it's money, use strong consistency. A senior developer makes this trade-off consciously, not accidentally.

---

> **Final Note:** These answers are structured for a 2-3 minute verbal response per question. Practice speaking them aloud — confidence comes from preparation, not memorization. Focus on the "why" behind each answer, not just the "what."
