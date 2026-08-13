# Spring & Spring Boot — Interview Q&A

---

## Spring Core

### Q: What is the Spring Framework?
Spring is a lightweight, open-source Java framework for building enterprise applications. Its core feature is **Inversion of Control (IoC)**: the framework manages object creation and wiring instead of your code doing it manually. It provides modules for DI, AOP, transactions, data access (JDBC/JPA), MVC, security, and more, letting you write loosely coupled, testable code.

Key benefits: loose coupling, testability (easy mocking), declarative transactions/security, and a huge ecosystem.

### Q: What is IoC (Inversion of Control) and DI (Dependency Injection)?
**IoC** means control of object lifecycle and dependency wiring is inverted — handed to the Spring container instead of being done in your code with `new`.

**DI** is the pattern Spring uses to implement IoC: the container *injects* an object's dependencies rather than the object creating them.

```java
// Without DI — tight coupling
class OrderService {
    private PaymentService payment = new PaymentService(); // hard-coded
}

// With DI — container injects it
@Service
class OrderService {
    private final PaymentService payment;
    OrderService(PaymentService payment) { this.payment = payment; } // injected
}
```
Result: `OrderService` doesn't know how `PaymentService` is built, so it's easy to swap/mock.

### Q: ApplicationContext vs BeanFactory?
Both are IoC containers, but:

| BeanFactory | ApplicationContext |
|---|---|
| Basic container, lazy init by default | Superset of BeanFactory |
| No enterprise features | Eager singleton init, event publishing, i18n, AOP, annotation support |
| Rarely used directly | Standard choice in real apps |

`ApplicationContext` is what you use in practice (Spring Boot creates one for you). `BeanFactory` is the lower-level foundation.

### Q: What are the bean scopes in Spring?
- **singleton** (default) — one shared instance per container.
- **prototype** — new instance every time the bean is requested.
- **request** — one instance per HTTP request (web only).
- **session** — one instance per HTTP session (web only).
- **application** — one per ServletContext.
- **websocket** — one per WebSocket session.

```java
@Service
@Scope("prototype")
public class ReportBuilder { }
```

### Q: Explain the Spring bean lifecycle.
1. Container instantiates the bean.
2. Populates dependencies (DI).
3. Aware interfaces called (`BeanNameAware`, `ApplicationContextAware`).
4. `@PostConstruct` / `InitializingBean.afterPropertiesSet()` / custom init.
5. Bean is ready to use.
6. On shutdown: `@PreDestroy` / `DisposableBean.destroy()` / custom destroy.

```java
@Component
public class CacheManager {
    @PostConstruct
    public void init()    { /* warm up cache */ }
    @PreDestroy
    public void cleanup() { /* flush cache */ }
}
```

### Q: @Component vs @Service vs @Repository vs @Controller?
All are stereotype annotations — specializations of `@Component` that mark a class as a Spring-managed bean. They differ in *intent* (and some add behavior):

- **@Component** — generic Spring bean.
- **@Service** — business/service layer (semantic only).
- **@Repository** — DAO layer; adds **exception translation** (converts JDBC/JPA exceptions to Spring's `DataAccessException`).
- **@Controller** — web MVC controller (returns views); `@RestController` = `@Controller` + `@ResponseBody`.

Use the most specific one for readability and to get layer-specific behavior.

### Q: @Autowired — field vs constructor vs setter injection? Which is best and why?
- **Field injection** — `@Autowired` on the field. Concise but hard to test, hides dependencies, can't be `final`.
- **Setter injection** — good for optional dependencies.
- **Constructor injection** — *recommended*. Dependencies are explicit, can be `final` (immutable), guarantees the bean is fully initialized, and makes unit testing trivial (just pass mocks to the constructor). Since Spring 4.3 `@Autowired` is optional on a single constructor.

```java
@Service
public class OrderService {
    private final PaymentService payment;
    private final InventoryService inventory;

    // constructor injection — no @Autowired needed for single ctor
    public OrderService(PaymentService payment, InventoryService inventory) {
        this.payment = payment;
        this.inventory = inventory;
    }
}
```

### Q: What is @Qualifier?
When multiple beans of the same type exist, `@Autowired` is ambiguous. `@Qualifier` names the exact bean to inject.

```java
@Service
public class NotificationService {
    private final MessageSender sender;
    public NotificationService(@Qualifier("smsSender") MessageSender sender) {
        this.sender = sender;
    }
}
```

### Q: What is @Primary?
Marks one bean as the default when multiple candidates of a type exist, so you don't need `@Qualifier` everywhere.

```java
@Bean
@Primary
public MessageSender emailSender() { return new EmailSender(); }

@Bean
public MessageSender smsSender()  { return new SmsSender(); }
// emailSender is injected by default; use @Qualifier("smsSender") for the other
```

### Q: @Configuration and @Bean?
`@Configuration` marks a class as a source of bean definitions. `@Bean` methods explicitly create beans — useful for third-party classes you can't annotate with `@Component`.

```java
@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### Q: What is AOP (Aspect-Oriented Programming)?
AOP lets you modularize **cross-cutting concerns** (logging, security, transactions, metrics) that span many classes, keeping them out of business logic.

Key terms: **Aspect** (the module), **Advice** (action: `@Before`, `@After`, `@Around`), **Pointcut** (where it applies), **JoinPoint** (a point in execution).

```java
@Aspect
@Component
public class LoggingAspect {
    @Around("execution(* com.app.service.*.*(..))")
    public Object logTime(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = jp.proceed();
        log.info("{} took {} ms", jp.getSignature(), System.currentTimeMillis() - start);
        return result;
    }
}
```
`@Transactional` itself is implemented with AOP.

---

## Spring Boot

### Q: What is Spring Boot and what are its advantages over Spring?
Spring Boot is an opinionated layer on top of Spring that removes boilerplate configuration and lets you build production-ready, stand-alone apps quickly.

Advantages:
- **Auto-configuration** — sensible defaults based on the classpath.
- **Starters** — curated dependency bundles (no version juggling).
- **Embedded server** — runs as a `java -jar`, no external Tomcat.
- **No XML** — convention over configuration.
- **Actuator** — built-in production monitoring/health.

### Q: How does auto-configuration work?
Driven by `@EnableAutoConfiguration` (inside `@SpringBootApplication`). At startup Spring Boot scans `META-INF/spring/...AutoConfiguration.imports` for auto-config classes, then applies them conditionally using `@Conditional` annotations:

- `@ConditionalOnClass` — a class is on the classpath.
- `@ConditionalOnMissingBean` — you haven't defined your own.
- `@ConditionalOnProperty` — a property is set.

Example: if `spring-boot-starter-data-jpa` and an H2 driver are on the classpath and you defined no `DataSource`, Boot auto-configures one. Your own beans always win, so you can override defaults.

### Q: What are Spring Boot starters?
Starters are convenient dependency descriptors that pull in a coherent set of libraries for a feature, with compatible versions managed for you.

Examples: `spring-boot-starter-web` (REST + embedded Tomcat + Jackson), `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-webflux`, `spring-boot-starter-test`.

### Q: What does @SpringBootApplication consist of?
It's a meta-annotation combining three:
- **@Configuration** — class can define beans.
- **@EnableAutoConfiguration** — turns on auto-configuration.
- **@ComponentScan** — scans the current package and sub-packages for components.

```java
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

### Q: application.properties vs application.yml?
Both configure the app externally; just different formats. YAML is hierarchical and cleaner for nested config; properties is flat key-value. If both exist, properties take precedence for the same key.

```properties
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/shop
```
```yaml
server:
  port: 8081
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shop
```

### Q: What are Spring profiles?
Profiles let you define environment-specific configuration (dev, test, prod). Use `application-{profile}.properties` and activate with `spring.profiles.active`.

```properties
# application-prod.properties
spring.datasource.url=jdbc:postgresql://prod-db:5432/shop
```
```java
@Service
@Profile("dev")
public class MockPaymentService implements PaymentService { }
```
Activate: `--spring.profiles.active=prod` or via env var.

### Q: How does the embedded server work and how do you change it?
Boot embeds a servlet container (Tomcat by default) so the app runs as an executable jar. To switch to Jetty or Undertow, exclude Tomcat from the web starter and add the other starter.

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <exclusions><exclusion>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
  </exclusion></exclusions>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

### Q: How do you change the server port?
```properties
server.port=8082
```
Or `server.port=0` for a random free port, or via command line: `java -jar app.jar --server.port=9090`.

### Q: What is Spring Boot Actuator?
Actuator exposes production-ready endpoints for monitoring and management over HTTP/JMX: `/actuator/health`, `/actuator/metrics`, `/actuator/info`, `/actuator/env`, `/actuator/loggers`.

```properties
management.endpoints.web.exposure.include=health,info,metrics
```
Add `spring-boot-starter-actuator`. Useful with Prometheus/Grafana and load-balancer health checks.

### Q: What is CommandLineRunner?
A functional interface whose `run()` executes once after the application context is ready — handy for startup tasks (seeding data, warming caches). `ApplicationRunner` is similar but gives parsed `ApplicationArguments`.

```java
@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository repo;
    public DataSeeder(UserRepository repo) { this.repo = repo; }

    @Override
    public void run(String... args) {
        repo.save(new User("admin"));
    }
}
```

---

## REST APIs

### Q: @RestController vs @Controller?
`@Controller` is for traditional MVC returning view names; methods need `@ResponseBody` to return data. `@RestController` = `@Controller` + `@ResponseBody`, so every method returns serialized data (JSON) directly in the response body — ideal for REST APIs.

### Q: What are the request mapping annotations?
`@RequestMapping` is the general one; the HTTP-specific shortcuts are cleaner:
- `@GetMapping` — read
- `@PostMapping` — create
- `@PutMapping` — full update
- `@PatchMapping` — partial update
- `@DeleteMapping` — delete

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/{id}")  public User get(@PathVariable Long id) { ... }
    @PostMapping          public User create(@RequestBody User u) { ... }
}
```

### Q: @PathVariable vs @RequestParam vs @RequestBody?
- **@PathVariable** — binds a value from the URI path: `/users/42` -> `id=42`.
- **@RequestParam** — binds a query parameter: `/users?status=active` -> `status="active"`.
- **@RequestBody** — deserializes the request body (JSON) into an object, used for POST/PUT.

```java
@GetMapping("/users/{id}")
public User get(@PathVariable Long id,
                @RequestParam(defaultValue = "false") boolean detailed) { ... }

@PostMapping("/users")
public User create(@RequestBody @Valid UserDto dto) { ... }
```

### Q: What is ResponseEntity and why use it?
`ResponseEntity<T>` represents the full HTTP response — status code, headers, and body. Use it for explicit control over status codes and headers instead of returning a bare object.

```java
@PostMapping("/users")
public ResponseEntity<User> create(@RequestBody @Valid UserDto dto) {
    User saved = service.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED)
                         .header("Location", "/api/users/" + saved.getId())
                         .body(saved);
}
```

### Q: Which HTTP status codes should a REST API use?
- **200 OK** — successful GET/PUT.
- **201 Created** — successful POST that creates a resource.
- **204 No Content** — successful DELETE / update with no body.
- **400 Bad Request** — validation/malformed input.
- **401 Unauthorized** — not authenticated.
- **403 Forbidden** — authenticated but not allowed.
- **404 Not Found** — resource missing.
- **409 Conflict** — duplicate/version conflict.
- **500 Internal Server Error** — unexpected server error.

### Q: How do you handle exceptions globally?
Use `@RestControllerAdvice` with `@ExceptionHandler` methods to centralize error handling and return consistent error responses, instead of try/catch in every controller.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseEntity.badRequest().body(new ApiError(400, msg));
    }
}
```

### Q: How do you do request validation?
Add Bean Validation annotations on the DTO and trigger them with `@Valid` on the controller parameter. Failures throw `MethodArgumentNotValidException` (handle it in `@RestControllerAdvice`).

```java
public class UserDto {
    @NotBlank private String name;
    @Email    private String email;
    @Min(18)  private int age;
}

@PostMapping("/users")
public User create(@RequestBody @Valid UserDto dto) { ... }
```
Requires `spring-boot-starter-validation`.

### Q: What is idempotency in REST?
An operation is idempotent if repeating it produces the same result as doing it once.
- **GET, PUT, DELETE** — idempotent (PUT sets the same state; deleting an already-deleted resource yields the same final state).
- **POST** — not idempotent (each call creates a new resource).

To make POST idempotent for safety (e.g. payments), use an **idempotency key** header that the server stores and de-duplicates.

### Q: What are some REST API best practices?
- Use nouns and plurals for resources: `/api/users`, `/api/users/{id}/orders`.
- Use HTTP methods/verbs correctly (don't put actions in URLs).
- Return proper status codes and consistent error bodies.
- Support pagination, filtering, sorting for collections.
- Version the API.
- Stateless (no server session); use tokens.
- Use DTOs, never expose entities directly.

### Q: How do you version a REST API?
Common approaches:
- **URI versioning** — `/api/v1/users` (most common, explicit).
- **Header versioning** — `Accept: application/vnd.app.v1+json`.
- **Request param** — `/api/users?version=1`.

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller { ... }
```

---

## Spring Data JPA

### Q: What is Spring Data JPA?
Spring Data JPA is an abstraction over JPA/Hibernate that eliminates boilerplate DAO code. You declare a repository *interface* and Spring generates the implementation at runtime, including CRUD, derived queries, pagination, and sorting.

### Q: JpaRepository vs CrudRepository vs PagingAndSortingRepository?
A hierarchy:
- **CrudRepository** — basic CRUD (`save`, `findById`, `findAll`, `delete`).
- **PagingAndSortingRepository** — adds pagination and sorting.
- **JpaRepository** — adds JPA-specific features (`flush`, `saveAll`, batch deletes, `findAll` returning `List`). Usually you extend `JpaRepository`.

```java
public interface UserRepository extends JpaRepository<User, Long> { }
```

### Q: Explain @Entity, @Id, and @GeneratedValue.
- **@Entity** — maps the class to a database table.
- **@Id** — marks the primary key field.
- **@GeneratedValue** — how the PK is generated: `IDENTITY` (DB auto-increment, common in MySQL), `SEQUENCE` (DB sequence, common in Postgres), `AUTO`, `TABLE`.

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}
```

### Q: What are derived query methods?
Spring Data parses the method name and generates the query automatically.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByStatus(String status);
    User findByEmail(String email);
    List<User> findByAgeGreaterThanAndStatus(int age, String status);
    List<User> findByNameContainingIgnoreCase(String name);
    long countByStatus(String status);
}
```

### Q: When and how do you use @Query (JPQL vs native)?
Use `@Query` when derived methods get unwieldy or you need custom logic. JPQL works on entities/fields; native SQL works on tables/columns.

```java
// JPQL — uses entity names
@Query("SELECT u FROM User u WHERE u.status = :status")
List<User> findActive(@Param("status") String status);

// Native SQL
@Query(value = "SELECT * FROM users WHERE age > ?1", nativeQuery = true)
List<User> findOlderThan(int age);

// Modifying query
@Modifying
@Transactional
@Query("UPDATE User u SET u.status = :s WHERE u.id = :id")
int updateStatus(@Param("id") Long id, @Param("s") String s);
```

### Q: Explain JPA relationships.
- **@OneToOne** — e.g. User ↔ Profile.
- **@OneToMany / @ManyToOne** — e.g. one Department has many Employees. `@ManyToOne` is the owning side (holds the FK).
- **@ManyToMany** — e.g. Students ↔ Courses, via a join table.

```java
@Entity
public class Department {
    @Id @GeneratedValue private Long id;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<Employee> employees = new ArrayList<>();
}

@Entity
public class Employee {
    @Id @GeneratedValue private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
}
```

### Q: Lazy vs Eager loading?
- **EAGER** — related entity is loaded immediately with the parent.
- **LAZY** — related entity is loaded only when accessed (a proxy is used until then).

Defaults: `@ManyToOne`/`@OneToOne` are EAGER; `@OneToMany`/`@ManyToMany` are LAZY. Prefer **LAZY** for collections to avoid loading unnecessary data; fetch what you need explicitly. Accessing a lazy field outside a transaction causes `LazyInitializationException`.

### Q: What is the N+1 query problem and how do you fix it?
When you load N parent entities and then trigger 1 query per parent to load its lazy association — resulting in N+1 queries.

Fixes:
- **JOIN FETCH** in JPQL.
- **@EntityGraph**.
- Batch fetching (`@BatchSize` / `hibernate.default_batch_fetch_size`).

```java
@Query("SELECT d FROM Department d JOIN FETCH d.employees")
List<Department> findAllWithEmployees();

// or
@EntityGraph(attributePaths = "employees")
List<Department> findAll();
```

### Q: What is @Transactional and how does propagation work?
`@Transactional` declaratively wraps a method in a database transaction — commit on success, rollback on a runtime exception. Implemented via AOP proxy.

**Propagation** controls how an existing transaction is handled:
- **REQUIRED** (default) — join existing or create new.
- **REQUIRES_NEW** — always start a new, suspend the current.
- **SUPPORTS** — join if one exists, else run non-transactional.
- **MANDATORY** — must run inside an existing one.
- **NESTED** — nested transaction with savepoints.

```java
@Transactional
public void transfer(Long from, Long to, BigDecimal amt) {
    accountRepo.debit(from, amt);
    accountRepo.credit(to, amt); // both commit or both roll back
}
```
Note: by default rollback happens only on unchecked (`RuntimeException`) exceptions; use `rollbackFor` for checked ones. Self-invocation bypasses the proxy.

### Q: How do you implement pagination?
Pass a `Pageable` to a repository method and return a `Page<T>` (which includes total count, total pages, etc.).

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByStatus(String status, Pageable pageable);
}

// usage
Pageable pageable = PageRequest.of(0, 20, Sort.by("name").ascending());
Page<User> page = userRepository.findByStatus("active", pageable);
List<User> content = page.getContent();
long total = page.getTotalElements();
```

---

## Security & Misc

### Q: Spring Security basics — authentication vs authorization?
- **Authentication (authn)** — verifying *who you are* (login, credentials, token).
- **Authorization (authz)** — verifying *what you're allowed to do* (roles/permissions).

Spring Security works as a filter chain in front of the app. You configure it via a `SecurityFilterChain` bean.

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(c -> c.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults());
    return http.build();
}
```

### Q: Explain the JWT authentication flow at a high level.
1. Client sends credentials to `/login`.
2. Server validates and returns a signed **JWT** (header.payload.signature).
3. Client stores the token and sends it on each request: `Authorization: Bearer <token>`.
4. A filter validates the signature/expiry on each request and sets the `SecurityContext` — no server-side session (stateless).

JWT is self-contained (carries claims like user id and roles) and signed so it can't be tampered with. Good for stateless, scalable REST APIs.

### Q: DTO vs Entity — why separate them?
- **Entity** — maps to a DB table; tied to persistence.
- **DTO (Data Transfer Object)** — shapes data for the API layer.

Reasons to separate: avoid exposing internal schema, prevent over-posting / unwanted field updates, decouple API contract from DB changes, avoid lazy-loading serialization issues, and tailor request/response payloads. Map between them manually or with MapStruct.

```java
public record UserDto(Long id, String name, String email) {}

UserDto toDto(User u) {
    return new UserDto(u.getId(), u.getName(), u.getEmail());
}
```

### Q: How do you handle CORS in Spring Boot?
CORS controls which origins (domains) can call your API from a browser. Configure per-controller or globally.

```java
// Per controller/method
@CrossOrigin(origins = "https://app.example.com")
@RestController
public class UserController { }

// Global
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://app.example.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```
With Spring Security, configure CORS there too (`http.cors(...)`).

---

## Reactive (Spring WebFlux)

### Q: Spring WebFlux vs Spring MVC?
- **Spring MVC** — synchronous, blocking, thread-per-request, built on the Servlet API. Simple and great for most apps.
- **Spring WebFlux** — asynchronous, non-blocking, reactive (Project Reactor), runs on Netty by default. Uses few threads to handle many concurrent connections (event loop). Better for high-concurrency, streaming, or I/O-bound workloads where downstream calls are also non-blocking.

Use WebFlux when you need scalability under heavy concurrent I/O; otherwise MVC is simpler.

### Q: Mono vs Flux?
Both are reactive publisher types from Project Reactor representing async streams:
- **Mono<T>** — emits **0 or 1** element (e.g. a single user, or a completion).
- **Flux<T>** — emits **0..N** elements (e.g. a stream/list of users).

They are lazy — nothing happens until subscribed (the framework subscribes for you).

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public Mono<User> getById(@PathVariable String id) {
        return userService.findById(id);   // 0 or 1
    }

    @GetMapping
    public Flux<User> getAll() {
        return userService.findAll();      // 0..N, can stream
    }
}
```
