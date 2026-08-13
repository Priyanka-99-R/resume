# 7 Essential Java Design Patterns Interview Questions (5+ Years)

---

## Q1. Explain the Singleton Pattern. How do you implement it correctly in Java?

**Summary:**
Singleton ensures only one instance of a class exists throughout the application lifecycle. I've used it for configuration managers, connection pools, and caching layers — anywhere you need a single shared resource with global access.

**Concept:**
The idea is simple — private constructor, static instance, public access method. But the devil is in the implementation. In a multithreaded environment, a naive Singleton can create multiple instances, defeating the entire purpose. There are several approaches, each with trade-offs.

**When & Why in Real Projects:**
- **Database connection pools** — you don't want multiple pools competing for connections.
- **Application configuration** — load once from a file/env, share everywhere.
- **Logging frameworks** — `LoggerFactory` internally uses Singleton-like patterns.
- **Caches** — a single Guava/Caffeine cache instance shared across services.

**Real-World Example:**

**Approach 1 — Enum Singleton (Recommended by Joshua Bloch):**

```java
public enum AppConfig {
    INSTANCE;

    private final Properties properties = new Properties();

    AppConfig() {
        try (InputStream is = getClass().getResourceAsStream("/app.properties")) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}

// Usage
String dbUrl = AppConfig.INSTANCE.get("db.url");
```

Why enum? The JVM guarantees exactly one instance, handles serialization automatically, and prevents reflection attacks. It's the simplest thread-safe Singleton.

**Approach 2 — Double-Checked Locking (when you need lazy initialization with a class hierarchy):**

```java
public class ConnectionPool {
    private static volatile ConnectionPool instance;  // volatile is CRITICAL

    private final HikariDataSource dataSource;

    private ConnectionPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost/mydb");
        config.setMaximumPoolSize(10);
        this.dataSource = new HikariDataSource(config);
    }

    public static ConnectionPool getInstance() {
        if (instance == null) {                      // first check (no locking)
            synchronized (ConnectionPool.class) {
                if (instance == null) {              // second check (with lock)
                    instance = new ConnectionPool();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
```

**Approach 3 — Bill Pugh / Static Inner Class (lazy + thread-safe without `volatile`):**

```java
public class CacheManager {
    private CacheManager() {}

    private static class Holder {
        private static final CacheManager INSTANCE = new CacheManager();
    }

    public static CacheManager getInstance() {
        return Holder.INSTANCE;  // loaded only when getInstance() is first called
    }
}
```

The inner class is not loaded until `getInstance()` is called, so initialization is lazy. The JVM's class loading mechanism guarantees thread safety.

**Common Mistakes:**
- **Missing `volatile`** in double-checked locking — without it, a thread can see a partially constructed object due to instruction reordering.
- **Ignoring serialization** — if your Singleton implements `Serializable`, deserialization creates a new instance. Fix with `readResolve()`:
  ```java
  private Object readResolve() { return instance; }
  ```
- **Reflection attacks** — `Constructor.setAccessible(true)` bypasses the private constructor. Enum Singleton is immune to this.
- **Using Singleton as a global variable dump** — it should hold shared *resources*, not random state. Overuse leads to tight coupling and untestable code.

**Closing:**
For new code, I default to Enum Singleton. If I need inheritance or lazy initialization, I use the Bill Pugh pattern. Double-checked locking is fine but unnecessary complexity in most cases. And in Spring applications, I just use `@Component` — the container manages the singleton scope.

---

## Q2. Explain the Factory Pattern. What is the difference between Factory Method and Abstract Factory?

**Summary:**
Factory Pattern encapsulates object creation logic so the caller doesn't need to know the concrete class being instantiated. Factory Method uses a single method to decide which class to create; Abstract Factory provides a family of related objects. I use factories constantly — in payment processing, notification systems, and anywhere I need to decouple creation from usage.

**Concept:**
Instead of scattering `new ConcreteClass()` across your codebase, you centralize creation in a factory. When a new type is added, you modify the factory — not every caller. This is the **Open/Closed Principle** in action.

- **Simple Factory** — a single method with conditional logic (technically not a GoF pattern, but widely used).
- **Factory Method** — defines an interface for creating objects, but lets subclasses decide which class to instantiate.
- **Abstract Factory** — produces *families* of related objects without specifying concrete classes.

**When & Why in Real Projects:**
- **Payment gateways** — based on the region or payment type, create the right processor (Stripe, Razorpay, PayPal).
- **Notification systems** — based on user preference, create an EmailNotifier, SMSNotifier, or PushNotifier.
- **Database drivers** — `DriverManager.getConnection()` is a factory — you pass a URL, it returns the right `Connection` implementation.
- **Document parsers** — based on file extension, create a PDF parser, Excel parser, or CSV parser.

**Real-World Example:**

**Simple Factory — Payment Processor:**

```java
public class PaymentProcessorFactory {

    public static PaymentProcessor create(PaymentMethod method) {
        return switch (method) {
            case CREDIT_CARD -> new StripeProcessor();
            case UPI         -> new RazorpayProcessor();
            case PAYPAL      -> new PayPalProcessor();
        };
    }
}

// Usage — caller doesn't know or care about concrete classes
PaymentProcessor processor = PaymentProcessorFactory.create(order.getPaymentMethod());
processor.charge(order.getAmount());
```

**Factory Method — report generation where subclasses decide the format:**

```java
public abstract class ReportGenerator {

    // Factory method — subclasses decide what to create
    protected abstract ReportFormatter createFormatter();

    public final byte[] generate(ReportData data) {
        ReportFormatter formatter = createFormatter();
        return formatter.format(data);
    }
}

public class PdfReportGenerator extends ReportGenerator {
    @Override
    protected ReportFormatter createFormatter() {
        return new PdfFormatter();
    }
}

public class ExcelReportGenerator extends ReportGenerator {
    @Override
    protected ReportFormatter createFormatter() {
        return new ExcelFormatter();
    }
}
```

**Abstract Factory — UI component families for different themes:**

```java
public interface UIFactory {
    Button createButton();
    TextField createTextField();
    Dialog createDialog();
}

public class DarkThemeFactory implements UIFactory {
    public Button createButton()       { return new DarkButton(); }
    public TextField createTextField() { return new DarkTextField(); }
    public Dialog createDialog()       { return new DarkDialog(); }
}

public class LightThemeFactory implements UIFactory {
    public Button createButton()       { return new LightButton(); }
    public TextField createTextField() { return new LightTextField(); }
    public Dialog createDialog()       { return new LightDialog(); }
}

// Usage — swap the entire family by changing one line
UIFactory factory = user.prefersDark() ? new DarkThemeFactory() : new LightThemeFactory();
Button btn = factory.createButton();
```

**Common Mistakes:**
- **Overusing factories** — if you only have one implementation and don't foresee more, `new MyClass()` is fine. Don't abstract prematurely.
- **Bloated switch/if-else in Simple Factory** — when the factory grows to 20+ cases, consider using a `Map<Type, Supplier<T>>` or a registry pattern instead.
- **Confusing Factory Method with Simple Factory** — Factory Method uses *inheritance* (subclasses override the creation method). Simple Factory is just a static method with conditionals.

**Closing:**
I reach for Factory when I have multiple implementations behind a common interface and the decision of *which* to use is based on runtime data. Simple Factory covers 80% of cases. Abstract Factory is for when you need to swap entire families of related objects together.

---

## Q3. Explain the Builder Pattern. When do you prefer it over constructors or setters?

**Summary:**
Builder Pattern constructs complex objects step-by-step, separating construction from representation. I use it whenever an object has more than 3-4 parameters, especially when some are optional. It eliminates telescoping constructors and gives you immutable objects with readable construction code.

**Concept:**
Instead of a constructor with 10 parameters (where you can't tell which `null` goes where) or a mutable object with setters (which allows partially constructed state), Builder lets you chain named methods and finalize with `.build()`, which returns a fully validated, often immutable object.

**When & Why in Real Projects:**
- **DTOs and request/response objects** — API requests with many optional fields.
- **Configuration objects** — database config, HTTP client config, cache config.
- **Domain objects with validation** — the `build()` method validates invariants before construction.
- **Test data** — builders make test setup readable and flexible.

**Real-World Example:**

**Custom Builder — Order with validation:**

```java
public class Order {
    private final String orderId;
    private final String customerId;
    private final List<LineItem> items;
    private final BigDecimal discount;
    private final String shippingAddress;
    private final String couponCode;

    private Order(Builder builder) {
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.items = List.copyOf(builder.items);  // immutable copy
        this.discount = builder.discount;
        this.shippingAddress = builder.shippingAddress;
        this.couponCode = builder.couponCode;
    }

    public static class Builder {
        // Required
        private final String orderId;
        private final String customerId;

        // Optional with defaults
        private List<LineItem> items = new ArrayList<>();
        private BigDecimal discount = BigDecimal.ZERO;
        private String shippingAddress;
        private String couponCode;

        public Builder(String orderId, String customerId) {
            this.orderId = orderId;
            this.customerId = customerId;
        }

        public Builder items(List<LineItem> items)       { this.items = items; return this; }
        public Builder discount(BigDecimal discount)     { this.discount = discount; return this; }
        public Builder shippingAddress(String address)   { this.shippingAddress = address; return this; }
        public Builder couponCode(String code)           { this.couponCode = code; return this; }

        public Order build() {
            if (items.isEmpty()) throw new IllegalStateException("Order must have at least one item");
            if (shippingAddress == null) throw new IllegalStateException("Shipping address is required");
            return new Order(this);
        }
    }
}

// Usage — readable, self-documenting
Order order = new Order.Builder("ORD-123", "CUST-456")
    .items(lineItems)
    .discount(new BigDecimal("10.00"))
    .shippingAddress("123 Main St")
    .couponCode("SAVE20")
    .build();
```

**Lombok Builder — for when you don't need custom validation:**

```java
@Builder
@Value  // makes it immutable
public class NotificationRequest {
    String userId;
    String channel;
    String message;
    @Builder.Default Priority priority = Priority.NORMAL;
    @Singular List<String> tags;
}

// Usage
NotificationRequest req = NotificationRequest.builder()
    .userId("user-1")
    .channel("email")
    .message("Your order shipped")
    .tag("transactional")
    .tag("shipping")
    .build();
```

**Builder in JDK and libraries:**

```java
// HttpClient (Java 11+)
HttpClient client = HttpClient.newBuilder()
    .version(HttpClient.Version.HTTP_2)
    .connectTimeout(Duration.ofSeconds(5))
    .followRedirects(HttpClient.Redirect.NORMAL)
    .build();

// Spring WebClient
WebClient client = WebClient.builder()
    .baseUrl("https://api.example.com")
    .defaultHeader("Authorization", "Bearer " + token)
    .build();
```

**Common Mistakes:**
- **Not validating in `build()`** — the whole point is to guarantee the object is valid upon construction. An invalid object should never be returned.
- **Making the built object mutable** — if you expose setters on the target class, the Builder loses its value. Make fields `final`.
- **Using Builder for simple objects** — a class with 2-3 required fields and no optionals doesn't need a Builder. A constructor is fine.
- **Forgetting `@Builder.Default` with Lombok** — without it, Lombok ignores your field initializers and defaults to `null`/`0`.

**Closing:**
My rule of thumb: more than 3 parameters, or mix of required and optional fields — use a Builder. It makes code readable at the call site and guarantees valid, immutable objects. With Lombok's `@Builder`, the boilerplate cost is near zero.

---

## Q4. Explain the Strategy Pattern. How is it used in real projects?

**Summary:**
Strategy Pattern lets you define a family of interchangeable algorithms and select one at runtime without changing the client code. It's the OOP way of eliminating if-else/switch chains. I use it for pricing rules, validation logic, sorting strategies, and anything where behavior varies by context.

**Concept:**
You extract the varying behavior into a **strategy interface**, create concrete implementations for each variant, and inject the right one at runtime. The client talks to the interface — it doesn't know or care which concrete strategy is executing.

This is fundamentally about **composition over inheritance** — instead of subclassing to change behavior, you swap a component.

**When & Why in Real Projects:**
- **Pricing/discount calculation** — different rules for regular, premium, VIP customers.
- **Payment processing** — different payment methods (credit card, UPI, wallet).
- **Sorting/filtering** — user selects the sort strategy at runtime.
- **File compression** — ZIP, GZIP, LZ4 based on file type or user choice.
- **Authentication** — OAuth, JWT, API key — each is a strategy.

**Real-World Example:**

**Pricing strategy for an e-commerce platform:**

```java
// Strategy interface
public interface PricingStrategy {
    BigDecimal calculatePrice(BigDecimal basePrice, int quantity);
}

// Concrete strategies
public class RegularPricing implements PricingStrategy {
    public BigDecimal calculatePrice(BigDecimal basePrice, int quantity) {
        return basePrice.multiply(BigDecimal.valueOf(quantity));
    }
}

public class PremiumPricing implements PricingStrategy {
    public BigDecimal calculatePrice(BigDecimal basePrice, int quantity) {
        BigDecimal total = basePrice.multiply(BigDecimal.valueOf(quantity));
        return total.multiply(new BigDecimal("0.90"));  // 10% discount
    }
}

public class WholesalePricing implements PricingStrategy {
    public BigDecimal calculatePrice(BigDecimal basePrice, int quantity) {
        BigDecimal rate = quantity > 100 ? new BigDecimal("0.70") : new BigDecimal("0.80");
        return basePrice.multiply(BigDecimal.valueOf(quantity)).multiply(rate);
    }
}

// Context — uses whatever strategy is injected
public class ShoppingCart {
    private PricingStrategy pricingStrategy;

    public void setPricingStrategy(PricingStrategy strategy) {
        this.pricingStrategy = strategy;
    }

    public BigDecimal checkout(BigDecimal basePrice, int quantity) {
        return pricingStrategy.calculatePrice(basePrice, quantity);
    }
}

// Usage
ShoppingCart cart = new ShoppingCart();
cart.setPricingStrategy(resolvePricingFor(currentUser));  // runtime decision
BigDecimal total = cart.checkout(itemPrice, qty);
```

**Modern Java approach — Strategy with lambdas (since strategies are often single-method interfaces):**

```java
@FunctionalInterface
public interface CompressionStrategy {
    byte[] compress(byte[] data);
}

public class FileProcessor {
    private final CompressionStrategy strategy;

    public FileProcessor(CompressionStrategy strategy) {
        this.strategy = strategy;
    }

    public byte[] process(byte[] raw) {
        // ... processing logic ...
        return strategy.compress(raw);
    }
}

// Usage — no need for concrete classes, just pass lambdas
FileProcessor gzipProcessor = new FileProcessor(data -> gzipCompress(data));
FileProcessor lz4Processor  = new FileProcessor(data -> lz4Compress(data));
// or method references
FileProcessor zipProcessor  = new FileProcessor(ZipUtils::compress);
```

**Strategy with Spring — inject via DI:**

```java
@Component("email")
public class EmailNotifier implements NotificationStrategy { ... }

@Component("sms")
public class SmsNotifier implements NotificationStrategy { ... }

@Service
public class NotificationService {
    private final Map<String, NotificationStrategy> strategies;

    // Spring injects all NotificationStrategy beans as a map keyed by bean name
    public NotificationService(Map<String, NotificationStrategy> strategies) {
        this.strategies = strategies;
    }

    public void notify(String channel, String message) {
        NotificationStrategy strategy = strategies.get(channel);
        if (strategy == null) throw new IllegalArgumentException("Unknown channel: " + channel);
        strategy.send(message);
    }
}
```

**Common Mistakes:**
- **Keeping the if-else and wrapping it in a "strategy"** — if your factory to select the strategy is a giant switch, you've just moved the problem. Use a registry map or Spring's DI-based approach.
- **Overusing it for two cases** — if you have exactly two behaviors and it's unlikely to grow, a simple if-else is more readable than three new classes.
- **Stateful strategies** — strategies should ideally be stateless and reusable. If they hold mutable state, you can't safely share them.

**Closing:**
Strategy is one of the patterns I use most frequently. It naturally emerges when I see a growing if-else chain for behavior selection. With Java's functional interfaces and lambdas, lightweight strategies don't even need dedicated classes.

---

## Q5. Explain the Observer Pattern. How does it relate to event-driven systems?

**Summary:**
Observer Pattern defines a one-to-many dependency where when one object (the subject) changes state, all its dependents (observers) are notified automatically. It's the foundation of event-driven architecture — from UI event listeners to Spring's `ApplicationEvent` system to message queues.

**Concept:**
The subject maintains a list of observers. When something interesting happens, it iterates through the list and calls a notification method on each observer. This decouples the **producer** of events from the **consumers** — the subject doesn't know or care what the observers do with the notification.

**When & Why in Real Projects:**
- **Domain events** — when an order is placed, notify inventory, billing, and shipping services independently.
- **UI frameworks** — button click listeners, form change handlers.
- **Spring's event system** — `ApplicationEventPublisher` and `@EventListener`.
- **Cache invalidation** — when data changes, notify all cache instances to evict.
- **Audit logging** — observe entity changes and log them without polluting business logic.

**Real-World Example:**

**Custom Observer — Order lifecycle events:**

```java
// Observer interface
public interface OrderEventListener {
    void onOrderEvent(OrderEvent event);
}

// Subject
public class OrderService {
    private final List<OrderEventListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(OrderEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(OrderEventListener listener) {
        listeners.remove(listener);
    }

    public void placeOrder(Order order) {
        // ... business logic ...
        order.setStatus(Status.PLACED);
        notifyListeners(new OrderEvent(OrderEventType.PLACED, order));
    }

    private void notifyListeners(OrderEvent event) {
        for (OrderEventListener listener : listeners) {
            listener.onOrderEvent(event);
        }
    }
}

// Concrete observers — completely decoupled from each other
public class InventoryObserver implements OrderEventListener {
    public void onOrderEvent(OrderEvent event) {
        if (event.getType() == OrderEventType.PLACED) {
            reserveStock(event.getOrder().getItems());
        }
    }
}

public class NotificationObserver implements OrderEventListener {
    public void onOrderEvent(OrderEvent event) {
        if (event.getType() == OrderEventType.PLACED) {
            sendConfirmationEmail(event.getOrder().getCustomerEmail());
        }
    }
}
```

**Spring's built-in Observer pattern — the approach I prefer in Spring apps:**

```java
// Event
public class OrderPlacedEvent extends ApplicationEvent {
    private final Order order;
    public OrderPlacedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
    public Order getOrder() { return order; }
}

// Publisher
@Service
public class OrderService {
    private final ApplicationEventPublisher publisher;

    public OrderService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void placeOrder(Order order) {
        // ... business logic ...
        publisher.publishEvent(new OrderPlacedEvent(this, order));
    }
}

// Observers — Spring discovers these automatically
@Component
public class InventoryListener {
    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        reserveStock(event.getOrder().getItems());
    }
}

@Component
public class AuditListener {
    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        auditLog.record("Order placed: " + event.getOrder().getId());
    }
}

// Async observer — doesn't block the publisher
@Component
public class AnalyticsListener {
    @Async
    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        analyticsService.trackConversion(event.getOrder());
    }
}
```

**Common Mistakes:**
- **Synchronous observers blocking the publisher** — if one observer is slow (e.g., sending email), it blocks the entire order placement. Use `@Async` or an async event bus.
- **Observer throwing exceptions** — one failing observer can prevent other observers from being notified. Wrap notification calls in try-catch or use a framework that handles this.
- **Memory leaks** — in non-Spring setups, forgetting to remove observers (especially in UI or long-lived subjects) keeps them in memory. Use `WeakReference` or explicit lifecycle management.
- **Ordering assumptions** — observers should be independent. If observer B depends on observer A running first, you have hidden coupling.

**Closing:**
In Spring applications, I always use `ApplicationEventPublisher` and `@EventListener` — it gives me the Observer pattern with zero boilerplate and built-in async support. For cross-service events, this naturally evolves into message queues (Kafka, RabbitMQ), which is Observer at the distributed level.

---

## Q6. Explain the Proxy Pattern. What are the different types and how is it used in Java?

**Summary:**
Proxy Pattern provides a surrogate or placeholder for another object to control access to it. Java uses proxies extensively — Spring AOP, Hibernate lazy loading, security checks, caching, remote calls. Every time you use `@Transactional` or `@Cacheable` in Spring, you're using a proxy.

**Concept:**
A proxy implements the same interface as the real object. The client talks to the proxy thinking it's the real thing. The proxy adds behavior (logging, security, caching, lazy loading) before/after delegating to the real object.

Types:
- **Virtual Proxy** — delays expensive object creation until first use (lazy loading).
- **Protection Proxy** — controls access based on permissions.
- **Remote Proxy** — represents an object in a different address space (RMI, gRPC stubs).
- **Caching Proxy** — caches results of expensive operations.
- **Logging/Audit Proxy** — adds cross-cutting concerns without modifying the original class.

**When & Why in Real Projects:**
- **Spring AOP** — `@Transactional`, `@Cacheable`, `@Async`, `@Secured` all work through proxies.
- **Hibernate lazy loading** — `@OneToMany(fetch = LAZY)` returns a proxy collection that loads data on first access.
- **API rate limiting** — proxy wraps an HTTP client and adds throttling.
- **Circuit breakers** — Resilience4j wraps service calls in a proxy that short-circuits on failures.

**Real-World Example:**

**Static Proxy — caching wrapper:**

```java
public interface ProductService {
    Product getById(String id);
}

public class ProductServiceImpl implements ProductService {
    public Product getById(String id) {
        // expensive database call
        return productRepository.findById(id).orElseThrow();
    }
}

public class CachingProductProxy implements ProductService {
    private final ProductService delegate;
    private final Map<String, Product> cache = new ConcurrentHashMap<>();

    public CachingProductProxy(ProductService delegate) {
        this.delegate = delegate;
    }

    public Product getById(String id) {
        return cache.computeIfAbsent(id, delegate::getById);
    }
}

// Usage — client doesn't know it's using a proxy
ProductService service = new CachingProductProxy(new ProductServiceImpl());
```

**JDK Dynamic Proxy — generic logging proxy for any interface:**

```java
public class LoggingProxy implements InvocationHandler {
    private final Object target;

    private LoggingProxy(Object target) {
        this.target = target;
    }

    public static <T> T create(T target, Class<T> iface) {
        return iface.cast(Proxy.newProxyInstance(
            iface.getClassLoader(),
            new Class[]{iface},
            new LoggingProxy(target)
        ));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("Calling {}.{}({})", target.getClass().getSimpleName(),
                 method.getName(), Arrays.toString(args));
        long start = System.nanoTime();
        try {
            Object result = method.invoke(target, args);
            log.info("{} completed in {}ms", method.getName(),
                     (System.nanoTime() - start) / 1_000_000);
            return result;
        } catch (InvocationTargetException e) {
            log.error("{} threw {}", method.getName(), e.getCause().getMessage());
            throw e.getCause();
        }
    }
}

// Usage
OrderService proxied = LoggingProxy.create(new OrderServiceImpl(), OrderService.class);
proxied.placeOrder(order);  // automatically logged
```

**CGLIB Proxy (what Spring uses for classes without interfaces):**

```java
Enhancer enhancer = new Enhancer();
enhancer.setSuperclass(OrderServiceImpl.class);
enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
    if (requiresAuth(method)) {
        checkPermission();
    }
    return proxy.invokeSuper(obj, args);
});

OrderServiceImpl proxied = (OrderServiceImpl) enhancer.create();
```

Spring uses JDK dynamic proxies for interface-based beans and CGLIB for class-based beans (default since Spring Boot 2.0).

**Common Mistakes:**
- **Self-invocation in Spring proxies** — calling `this.method()` inside a `@Transactional` class bypasses the proxy. The transaction annotation is ignored because the call doesn't go through the proxy. Fix: inject the bean into itself or use `AopContext.currentProxy()`.
  ```java
  @Service
  public class OrderService {
      @Transactional
      public void placeOrder(Order o) {
          // this.validate(o);  // BUG: bypasses proxy, no transaction
          ((OrderService) AopContext.currentProxy()).validate(o);  // works
      }

      @Transactional(propagation = REQUIRES_NEW)
      public void validate(Order o) { ... }
  }
  ```
- **Hibernate lazy loading outside a session** — accessing a lazy proxy after the session closes throws `LazyInitializationException`. Use `@Transactional` to keep the session open, or fetch eagerly when needed.
- **JDK Dynamic Proxy requires an interface** — if your class doesn't implement an interface, JDK proxy won't work. Use CGLIB instead.

**Closing:**
Proxy is one of the most practically important patterns in Java because Spring and Hibernate are built on it. Understanding how proxies work — especially the self-invocation pitfall — has saved me hours of debugging in production Spring applications.

---

## Q7. Explain the Dependency Injection Pattern. How does Spring implement it?

**Summary:**
Dependency Injection means a class receives its dependencies from the outside rather than creating them internally. It's the backbone of Spring — it makes code loosely coupled, testable, and modular. I don't write `new Service()` in business logic; I let the container wire it.

**Concept:**
Without DI, classes create their own dependencies — `new EmailService()`, `new UserRepository()`. This creates **tight coupling**: you can't swap implementations, can't test in isolation, and changes cascade across the codebase.

With DI, you **declare what you need** (via constructor, setter, or field), and an external framework (the IoC container) provides it. The class doesn't know or care where its dependency comes from.

Three injection types:
- **Constructor injection** (recommended) — dependencies provided at construction time. Object is always in a valid state.
- **Setter injection** — optional dependencies that can be changed after construction.
- **Field injection** (`@Autowired` on fields) — convenient but discourages immutability and makes testing harder.

**When & Why in Real Projects:**
- **Every Spring application** — services, repositories, controllers are all wired via DI.
- **Swapping implementations** — inject an interface, switch between `RealPaymentGateway` and `MockPaymentGateway` without changing business logic.
- **Testing** — inject mocks directly via constructors. No reflection hacks needed.
- **Configuration** — inject environment-specific beans (dev/staging/prod datasources).

**Real-World Example:**

**Constructor Injection — the right way:**

```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;

    // Spring auto-injects — no @Autowired needed with single constructor
    public OrderService(OrderRepository orderRepository,
                        PaymentGateway paymentGateway,
                        NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
    }

    public void placeOrder(Order order) {
        paymentGateway.charge(order.getTotal());
        orderRepository.save(order);
        notificationService.sendConfirmation(order);
    }
}
```

**Why constructor injection is superior:**
- Fields can be `final` — immutable and thread-safe.
- Object is always fully initialized — no `NullPointerException` from partially injected state.
- Easy to test — just pass mocks in the constructor, no Spring context needed:

```java
@Test
void testPlaceOrder() {
    OrderRepository mockRepo = mock(OrderRepository.class);
    PaymentGateway mockPayment = mock(PaymentGateway.class);
    NotificationService mockNotif = mock(NotificationService.class);

    OrderService service = new OrderService(mockRepo, mockPayment, mockNotif);
    service.placeOrder(testOrder);

    verify(mockPayment).charge(testOrder.getTotal());
    verify(mockRepo).save(testOrder);
}
```

**Interface-based DI — swapping implementations:**

```java
public interface PaymentGateway {
    void charge(BigDecimal amount);
}

@Component
@Profile("prod")
public class StripeGateway implements PaymentGateway {
    public void charge(BigDecimal amount) {
        // real Stripe API call
    }
}

@Component
@Profile("dev")
public class FakeGateway implements PaymentGateway {
    public void charge(BigDecimal amount) {
        log.info("FAKE charge: {}", amount);  // no real charge in dev
    }
}
```

Spring injects the right implementation based on the active profile — zero code changes.

**How Spring implements DI internally:**
1. **Component scanning** — `@ComponentScan` discovers `@Component`, `@Service`, `@Repository`, `@Controller` classes.
2. **Bean definitions** — Spring creates `BeanDefinition` metadata for each class, including its dependencies.
3. **Dependency resolution** — Spring builds a dependency graph, topologically sorts it, and instantiates beans in the right order.
4. **Injection** — via reflection (constructor, setter, or field). For constructor injection, Spring calls the constructor with resolved dependencies.
5. **Lifecycle callbacks** — `@PostConstruct`, `InitializingBean.afterPropertiesSet()`, then the bean is ready.

**Common Mistakes:**
- **Field injection in production code** — `@Autowired private Service service;` hides dependencies, makes testing painful (requires reflection or Spring context), and allows `null` fields.
- **Circular dependencies** — Service A depends on Service B, which depends on Service A. Spring detects this at startup with constructor injection (which is good — it forces you to fix the design). Field injection hides circular deps until runtime.
- **Injecting too many dependencies** — a constructor with 8+ parameters is a code smell. The class is doing too much. Refactor into smaller, focused services.
- **Using `new` inside DI-managed beans** — if you `new` a service manually, Spring doesn't manage it — no proxies, no transactions, no AOP. If it needs to be managed, let Spring create it.

**Closing:**
DI is the single most important pattern in enterprise Java. I always use constructor injection — it gives me immutability, easy testing, and fails fast on circular dependencies. The Spring container handles the wiring; my code just declares what it needs.
