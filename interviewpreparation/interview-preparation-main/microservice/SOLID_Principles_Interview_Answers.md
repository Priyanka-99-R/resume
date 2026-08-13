# SOLID Principles — Interview Answers (Senior Java/Spring Boot Perspective)
### 5+ Years Experience Level | Practical, Real-World Focus

---

## What are SOLID Principles? (Opening Answer)

**Summary:**
SOLID is a set of five design principles that guide how to write maintainable, scalable, and testable object-oriented code. I don't treat them as academic rules — they're practical guidelines I follow daily while designing services, repositories, and APIs in Spring Boot. They prevent the kind of code rot that makes a 2-year-old project unmaintainable.

```
S — Single Responsibility Principle (SRP)
O — Open/Closed Principle (OCP)
L — Liskov Substitution Principle (LSP)
I — Interface Segregation Principle (ISP)
D — Dependency Inversion Principle (DIP)
```

---

## S — Single Responsibility Principle (SRP)

> **"A class should have only one reason to change."**

**Summary:**
SRP means each class does exactly one job. If a class handles business logic AND sends emails AND writes to the database, it has three reasons to change — and that's a maintenance nightmare. I split responsibilities so that each class is focused, testable, and independently deployable.

**How I Apply It in Real Projects:**

```java
// ❌ VIOLATION — One class doing everything
@Service
public class OrderService {

    public Order createOrder(OrderRequest request) {
        // 1. Validate the order
        if (request.getItems().isEmpty()) {
            throw new ValidationException("Order must have items");
        }

        // 2. Calculate price (business logic)
        double total = request.getItems().stream()
            .mapToDouble(i -> i.getPrice() * i.getQuantity())
            .sum();

        // 3. Save to database
        Order order = new Order();
        order.setTotal(total);
        order.setStatus("CREATED");
        orderRepository.save(order);

        // 4. Send confirmation email
        String emailBody = "Your order #" + order.getId() + " total: $" + total;
        emailSender.send(request.getEmail(), "Order Confirmation", emailBody);

        // 5. Publish event to Kafka
        kafkaTemplate.send("order-events", new OrderCreatedEvent(order.getId()));

        return order;
    }
}
// Problem: Change email template? → touch OrderService
// Problem: Change pricing rules? → touch OrderService
// Problem: Change event format? → touch OrderService
// 5 reasons to change = violation
```

```java
// ✅ FIXED — Each class has ONE responsibility

@Service
public class OrderService {
    private final OrderValidator validator;
    private final PricingService pricingService;
    private final OrderRepository orderRepository;
    private final OrderNotificationService notificationService;
    private final OrderEventPublisher eventPublisher;

    public Order createOrder(OrderRequest request) {
        validator.validate(request);
        double total = pricingService.calculateTotal(request.getItems());

        Order order = Order.builder()
            .items(request.getItems())
            .total(total)
            .status(OrderStatus.CREATED)
            .build();

        Order savedOrder = orderRepository.save(order);

        notificationService.sendConfirmation(savedOrder, request.getEmail());
        eventPublisher.publishOrderCreated(savedOrder);

        return savedOrder;
    }
}

// OrderValidator       → only validation logic
// PricingService       → only pricing calculations
// OrderNotificationService → only email/notification logic
// OrderEventPublisher  → only Kafka event publishing
// OrderService         → only orchestration (coordinator)
```

**Real-World Scenario:**
In a payment processing system, the `PaymentService` was initially handling validation, fraud detection, payment gateway calls, receipt generation, and audit logging — all in one class (800+ lines). When we needed to swap the fraud detection vendor, we had to touch this massive class and risk breaking payment processing. After applying SRP, each concern became its own service. Swapping the fraud vendor was a single-class change with zero risk to the payment flow.

**Common Mistakes:**
- Going too granular — creating a class for every 5 lines of code. SRP means one **reason to change**, not one **method per class**.
- Confusing orchestration with doing everything — `OrderService` coordinating other services is fine. It has one responsibility: orchestrating the order creation workflow.
- Anemic services that just delegate — if `OrderService` only calls `repository.save()`, it's not SRP, it's unnecessary indirection.

**Closing:**
SRP is the foundation of clean architecture. I ask myself: "If this requirement changes, how many classes do I need to modify?" If the answer is more than one, I probably have a responsibility leak.

---

## O — Open/Closed Principle (OCP)

> **"Software entities should be open for extension but closed for modification."**

**Summary:**
OCP means I can add new behavior without changing existing, tested code. In practice, this means using interfaces, strategy pattern, and polymorphism so that new features are added by creating new classes — not by modifying existing ones with `if/else` chains.

**How I Apply It in Real Projects:**

```java
// ❌ VIOLATION — Adding a new payment method requires modifying this class
@Service
public class PaymentProcessor {

    public PaymentResult process(PaymentRequest request) {
        if (request.getMethod().equals("CREDIT_CARD")) {
            // credit card logic...
            return chargeCreditCard(request);
        } else if (request.getMethod().equals("UPI")) {
            // UPI logic...
            return processUpi(request);
        } else if (request.getMethod().equals("NET_BANKING")) {
            // net banking logic...
            return processNetBanking(request);
        }
        // Every new payment method = modify this class
        // Risk of breaking existing payment methods
        throw new UnsupportedPaymentException(request.getMethod());
    }
}
```

```java
// ✅ FIXED — Strategy Pattern: add new payment methods without modifying existing code

// Step 1: Define the contract
public interface PaymentStrategy {
    PaymentResult process(PaymentRequest request);
    String getSupportedMethod();  // "CREDIT_CARD", "UPI", etc.
}

// Step 2: Implement each strategy
@Component
public class CreditCardPayment implements PaymentStrategy {
    @Override
    public PaymentResult process(PaymentRequest request) {
        // credit card specific logic
        return gateway.charge(request.getCardDetails(), request.getAmount());
    }

    @Override
    public String getSupportedMethod() { return "CREDIT_CARD"; }
}

@Component
public class UpiPayment implements PaymentStrategy {
    @Override
    public PaymentResult process(PaymentRequest request) {
        // UPI specific logic
        return upiGateway.collect(request.getUpiId(), request.getAmount());
    }

    @Override
    public String getSupportedMethod() { return "UPI"; }
}

// Step 3: Processor is CLOSED for modification — never touch it again
@Service
public class PaymentProcessor {
    private final Map<String, PaymentStrategy> strategies;

    // Spring injects ALL PaymentStrategy beans automatically
    public PaymentProcessor(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(
                PaymentStrategy::getSupportedMethod,
                Function.identity()
            ));
    }

    public PaymentResult process(PaymentRequest request) {
        PaymentStrategy strategy = strategies.get(request.getMethod());
        if (strategy == null) {
            throw new UnsupportedPaymentException(request.getMethod());
        }
        return strategy.process(request);
    }
}

// Adding Wallet payment? Just create a new class:
@Component
public class WalletPayment implements PaymentStrategy {
    @Override
    public PaymentResult process(PaymentRequest request) { /* ... */ }

    @Override
    public String getSupportedMethod() { return "WALLET"; }
}
// ✅ Zero modification to PaymentProcessor — it automatically picks up the new bean
```

**Real-World Scenario:**
In an e-commerce platform, we started with 2 payment methods. Over 18 months, we added 6 more (PayPal, Apple Pay, crypto, COD, EMI, wallet). Because we designed with OCP using the Strategy pattern, each new method was a new `@Component` class. The `PaymentProcessor` was never modified after its initial implementation — zero risk of breaking existing payment flows.

**Common Mistakes:**
- Overusing OCP for code that won't change — a simple utility class doesn't need a strategy pattern. Apply OCP where change is likely (payment methods, notification channels, export formats).
- Using inheritance when composition works better — favor strategy/interface over deep class hierarchies.
- Thinking OCP means "never modify any code" — it means design for extension at known variation points.

**Closing:**
OCP is about protecting working code from change. When I see a growing `if/else` or `switch` block, that's my signal to refactor into a strategy or plugin pattern. Spring's DI makes this effortless.

---

## L — Liskov Substitution Principle (LSP)

> **"Subtypes must be substitutable for their base types without breaking the program."**

**Summary:**
LSP means if your code works with a parent class, it should work identically with any child class. If a subclass throws unexpected exceptions, ignores parent contracts, or changes expected behavior, it violates LSP. In Java, this means: don't override methods in ways that surprise the caller.

**How I Apply It in Real Projects:**

```java
// ❌ VIOLATION — FixedDepositAccount breaks the contract of BankAccount

public class BankAccount {
    protected double balance;

    public void withdraw(double amount) {
        if (amount <= balance) {
            balance -= amount;
        }
    }

    public double getBalance() {
        return balance;
    }
}

public class FixedDepositAccount extends BankAccount {
    @Override
    public void withdraw(double amount) {
        // ❌ Throws exception that parent class never threw!
        throw new UnsupportedOperationException(
            "Cannot withdraw from fixed deposit before maturity"
        );
    }
}

// Caller code that worked fine with BankAccount:
public void processRefund(BankAccount account, double amount) {
    account.withdraw(amount);  // 💥 CRASHES with FixedDepositAccount
    // Caller assumed withdraw() works — LSP violated
}
```

```java
// ✅ FIXED — Redesign the hierarchy so subtypes honor the contract

// Separate interfaces for different capabilities
public interface Readable {
    double getBalance();
}

public interface Withdrawable extends Readable {
    void withdraw(double amount);
}

public interface Depositable extends Readable {
    void deposit(double amount);
}

// Savings account supports both
public class SavingsAccount implements Withdrawable, Depositable {
    private double balance;

    @Override
    public void withdraw(double amount) {
        if (amount <= balance) balance -= amount;
    }

    @Override
    public void deposit(double amount) {
        balance += amount;
    }

    @Override
    public double getBalance() { return balance; }
}

// Fixed deposit supports deposit and read, but NOT withdraw
public class FixedDepositAccount implements Depositable {
    private double balance;

    @Override
    public void deposit(double amount) {
        balance += amount;
    }

    @Override
    public double getBalance() { return balance; }
    // No withdraw() method — doesn't promise something it can't do
}

// Caller code:
public void processRefund(Withdrawable account, double amount) {
    account.withdraw(amount);  // ✅ Only accepts accounts that CAN withdraw
    // FixedDepositAccount can't be passed here — compile-time safety
}
```

**Another Common Violation — Squares and Rectangles:**
```java
// ❌ Classic violation
public class Rectangle {
    protected int width, height;

    public void setWidth(int w) { this.width = w; }
    public void setHeight(int h) { this.height = h; }
    public int getArea() { return width * height; }
}

public class Square extends Rectangle {
    @Override
    public void setWidth(int w) { this.width = w; this.height = w; }

    @Override
    public void setHeight(int h) { this.width = h; this.height = h; }
}

// Caller expects Rectangle behavior:
Rectangle rect = new Square();
rect.setWidth(5);
rect.setHeight(3);
assert rect.getArea() == 15;  // 💥 FAILS — getArea() returns 9
// Square changed the contract of setWidth/setHeight
```

**Real-World Scenario:**
In a notification system, we had `NotificationSender` as a base class with a `send()` method. A developer created `SmsNotificationSender` that threw `UnsupportedOperationException` for messages longer than 160 characters — something the parent never did. This caused production failures when the notification service tried to send long messages via SMS. The fix was to add a `canSend(Notification n)` method to the interface and check compatibility before sending.

**Common Mistakes:**
- Overriding a method to throw `UnsupportedOperationException` — the most common LSP violation in Java.
- Using inheritance for code reuse when there's no "is-a" relationship — prefer composition.
- Violating postconditions — if the parent guarantees "balance decreases by amount after withdraw," the child must honor that.

**Closing:**
LSP is about honoring contracts. Before extending a class, I ask: "Can every caller of the parent class safely use my subclass without knowing it's a subclass?" If not, the inheritance is wrong — redesign with interfaces and composition.

---

## I — Interface Segregation Principle (ISP)

> **"Clients should not be forced to depend on interfaces they don't use."**

**Summary:**
ISP means don't create fat interfaces that force implementers to stub out methods they don't need. Split large interfaces into smaller, role-specific ones. In Spring Boot, this translates to focused service interfaces rather than god interfaces.

**How I Apply It in Real Projects:**

```java
// ❌ VIOLATION — Fat interface forces unnecessary implementations

public interface UserService {
    User findById(Long id);
    List<User> findAll();
    User save(User user);
    void delete(Long id);
    void sendWelcomeEmail(User user);
    void resetPassword(String email);
    boolean validateCredentials(String username, String password);
    List<User> searchByName(String name);
    void exportToCSV(OutputStream out);
    UserStats getStatistics();
}

// A read-only reporting module only needs findAll() and getStatistics()
// But it's forced to depend on 10 methods including delete and resetPassword!

// A component that only sends emails must implement findById, save, delete...
// Leads to: throw new UnsupportedOperationException() everywhere
```

```java
// ✅ FIXED — Segregated interfaces, each client depends only on what it needs

public interface UserReader {
    User findById(Long id);
    List<User> findAll();
    List<User> searchByName(String name);
}

public interface UserWriter {
    User save(User user);
    void delete(Long id);
}

public interface UserAuthService {
    boolean validateCredentials(String username, String password);
    void resetPassword(String email);
}

public interface UserNotificationService {
    void sendWelcomeEmail(User user);
}

public interface UserReportService {
    void exportToCSV(OutputStream out);
    UserStats getStatistics();
}

// Implementation can implement multiple interfaces
@Service
public class UserServiceImpl implements UserReader, UserWriter, UserAuthService {

    @Override
    public User findById(Long id) { return userRepository.findById(id).orElseThrow(); }

    @Override
    public User save(User user) { return userRepository.save(user); }

    @Override
    public boolean validateCredentials(String username, String password) {
        // auth logic
    }
    // ... other implemented methods
}

// Read-only reporting controller depends ONLY on what it needs:
@RestController
public class UserReportController {
    private final UserReader userReader;          // only read methods
    private final UserReportService reportService; // only report methods

    // Cannot accidentally call delete() or resetPassword()
    // Compile-time safety!
}
```

**Real-World Spring Boot Example — Repository Layer:**
```java
// Spring Data already follows ISP:
// CrudRepository → basic CRUD
// PagingAndSortingRepository → adds pagination
// JpaRepository → adds JPA-specific methods

// You extend only what you need:
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Only extends JpaRepository if you need flush(), saveAll(), etc.
}

public interface ReadOnlyProductRepository extends Repository<Product, Long> {
    Optional<Product> findById(Long id);
    List<Product> findByCategory(String category);
    // No save, no delete — read-only by design
}
```

**Another Practical Example — Event Listeners:**
```java
// ❌ Fat interface
public interface OrderEventListener {
    void onOrderCreated(OrderCreatedEvent event);
    void onOrderPaid(OrderPaidEvent event);
    void onOrderShipped(OrderShippedEvent event);
    void onOrderCancelled(OrderCancelledEvent event);
    void onOrderRefunded(OrderRefundedEvent event);
}
// Inventory service only cares about created and cancelled — forced to implement all 5

// ✅ Segregated — or better yet, use Spring's @EventListener
@Component
public class InventoryEventHandler {
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        inventoryService.reserve(event.getItems());
    }

    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        inventoryService.release(event.getItems());
    }
    // Only listens to events it cares about — ISP by design
}
```

**Common Mistakes:**
- Creating one interface per method (too granular) — ISP means role-based segregation, not method-level segregation. Group related methods.
- Not applying ISP to your own service interfaces — developers carefully use ISP in domain code but create fat service interfaces.
- Marker interfaces with too many methods — if most implementers stub out methods, the interface is too fat.

**Closing:**
ISP prevents "interface pollution." In Spring Boot, I follow this naturally: separate read/write repository interfaces, focused service contracts, and event-driven listeners. If a class implements an interface and throws `UnsupportedOperationException` for some methods, that's a clear ISP violation.

---

## D — Dependency Inversion Principle (DIP)

> **"High-level modules should not depend on low-level modules. Both should depend on abstractions."**

**Summary:**
DIP means your business logic should depend on interfaces, not concrete implementations. In Spring Boot, this is what we do every day with `@Autowired` / `inject` — we inject interfaces, not classes. This makes the system flexible and testable because you can swap implementations without touching business logic.

**How I Apply It in Real Projects:**

```java
// ❌ VIOLATION — High-level OrderService depends directly on low-level MySQLOrderRepository

@Service
public class OrderService {
    // Directly depends on MySQL implementation
    private final MySQLOrderRepository repository = new MySQLOrderRepository();
    // Directly depends on SmtpEmailSender implementation
    private final SmtpEmailSender emailSender = new SmtpEmailSender();

    public Order createOrder(OrderRequest request) {
        Order order = buildOrder(request);
        repository.save(order);               // Tied to MySQL
        emailSender.send(order.getEmail());    // Tied to SMTP
        return order;
    }
}

// Problems:
// Want to switch to PostgreSQL? → Modify OrderService
// Want to switch to SendGrid? → Modify OrderService
// Want to unit test? → Need real MySQL and SMTP server
```

```java
// ✅ FIXED — Both high-level and low-level depend on abstractions

// Step 1: Define abstractions (interfaces)
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
}

public interface NotificationService {
    void sendOrderConfirmation(Order order);
}

// Step 2: Low-level modules implement abstractions
@Repository
public class MySQLOrderRepository implements OrderRepository {
    @Override
    public Order save(Order order) {
        return jdbcTemplate.save(order);  // MySQL-specific
    }
}

@Repository
public class MongoOrderRepository implements OrderRepository {
    @Override
    public Order save(Order order) {
        return mongoTemplate.save(order);  // MongoDB-specific
    }
}

@Component
public class EmailNotificationService implements NotificationService {
    @Override
    public void sendOrderConfirmation(Order order) {
        // SMTP / SendGrid logic
    }
}

@Component
public class SmsNotificationService implements NotificationService {
    @Override
    public void sendOrderConfirmation(Order order) {
        // Twilio SMS logic
    }
}

// Step 3: High-level module depends on abstraction
@Service
public class OrderService {
    private final OrderRepository orderRepository;         // Interface!
    private final NotificationService notificationService; // Interface!

    public OrderService(OrderRepository orderRepository,
                        NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    public Order createOrder(OrderRequest request) {
        Order order = buildOrder(request);
        Order saved = orderRepository.save(order);     // Don't know if it's MySQL or Mongo
        notificationService.sendOrderConfirmation(saved); // Don't know if it's email or SMS
        return saved;
    }
}
```

**The Dependency Direction:**
```
❌ WITHOUT DIP:
OrderService ──────depends on──────▶ MySQLOrderRepository
(high-level)                        (low-level)
Change MySQL → must change OrderService

✅ WITH DIP:
OrderService ──depends on──▶ OrderRepository (interface)
                                    ▲
MySQLOrderRepository ──implements──┘
(low-level)

Both depend on the abstraction (interface)
Change MySQL to Mongo → only change the implementation, OrderService untouched
```

**Spring Boot Makes DIP Natural:**
```java
// Spring's DI container IS the DIP engine
// You declare interfaces, Spring injects the concrete implementation

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    // Spring injects whichever @Repository implementation is available
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}

// Swapping implementations:
// Option 1: @Primary on the preferred implementation
@Primary
@Repository
public class PostgresOrderRepository implements OrderRepository { }

// Option 2: @Profile for environment-specific
@Profile("production")
@Repository
public class PostgresOrderRepository implements OrderRepository { }

@Profile("test")
@Repository
public class InMemoryOrderRepository implements OrderRepository { }

// Option 3: @Qualifier for explicit selection
public OrderService(@Qualifier("postgres") OrderRepository repo) { }
```

**Testing Benefit — The Biggest Win:**
```java
// Unit test — inject a mock, no database needed
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;  // Mock interface

    @Mock
    private NotificationService notificationService;  // Mock interface

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {
        when(orderRepository.save(any())).thenReturn(mockOrder);

        Order result = orderService.createOrder(request);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);
        verify(notificationService).sendOrderConfirmation(any());
    }
}
// ✅ No MySQL, no SMTP, no external dependencies — pure unit test
```

**Real-World Scenario:**
In a fintech project, we started with a single payment gateway (Razorpay). Six months later, the business wanted to add PayU as a fallback. Because `OrderService` depended on a `PaymentGateway` interface (not `RazorpayGateway` directly), we created `PayUGateway implements PaymentGateway`, configured it as a fallback via a `@Primary` + circuit breaker setup, and deployed it without changing a single line in `OrderService`.

**Common Mistakes:**
- Creating interfaces for every class "because DIP" — if a class will only ever have one implementation and it's not a boundary (DB, API, messaging), an interface adds noise. Apply DIP at architectural boundaries.
- Interface + single implementation named `*Impl` everywhere — if there's only one implementation and no plan for another, you might not need the interface yet (YAGNI).
- Confusing DI (Dependency Injection) with DIP (Dependency Inversion) — DI is the mechanism (Spring container), DIP is the design principle (depend on abstractions). You can use DI without DIP (inject concrete classes).

**Closing:**
DIP is the reason Spring applications are testable and flexible. Every service boundary in my code — database, messaging, external APIs, notification channels — is behind an interface. The business logic never knows or cares about the concrete implementation.

---

## How SOLID Works Together — A Complete Example

```java
// A single service demonstrating ALL 5 principles:

// ISP — Small, focused interfaces
public interface OrderReader {
    Optional<Order> findById(Long id);
    List<Order> findByCustomer(Long customerId);
}

public interface OrderWriter {
    Order save(Order order);
}

// DIP — Business logic depends on abstractions
public interface PaymentGateway {
    PaymentResult charge(Money amount, PaymentDetails details);
}

// OCP — New payment gateways via new implementations, not if/else
@Component
public class StripeGateway implements PaymentGateway { /* ... */ }

@Component
public class RazorpayGateway implements PaymentGateway { /* ... */ }

// SRP — Each class has one job
@Service
public class OrderValidator {
    public void validate(OrderRequest request) { /* validation only */ }
}

@Service
public class PricingEngine {
    public Money calculateTotal(List<OrderItem> items) { /* pricing only */ }
}

// LSP — All PaymentGateway implementations honor the contract
// StripeGateway.charge() and RazorpayGateway.charge() both:
// - Accept the same inputs
// - Return PaymentResult (not throw UnsupportedOperationException)
// - Handle errors the same way (PaymentFailedException)

// SRP — OrderService ONLY orchestrates
@Service
public class OrderService {
    private final OrderValidator validator;        // SRP
    private final PricingEngine pricingEngine;     // SRP
    private final OrderWriter orderWriter;         // ISP + DIP
    private final PaymentGateway paymentGateway;   // DIP + OCP + LSP

    public Order createOrder(OrderRequest request) {
        validator.validate(request);
        Money total = pricingEngine.calculateTotal(request.getItems());
        PaymentResult payment = paymentGateway.charge(total, request.getPaymentDetails());

        Order order = Order.builder()
            .items(request.getItems())
            .total(total)
            .paymentId(payment.getTransactionId())
            .status(OrderStatus.CONFIRMED)
            .build();

        return orderWriter.save(order);
    }
}
```

---

## Quick Reference — SOLID Cheat Sheet

| Principle | One-Liner | Violation Smell | Fix |
|-----------|-----------|----------------|-----|
| **S** — Single Responsibility | One class, one reason to change | 500+ line service class | Extract focused services |
| **O** — Open/Closed | Extend, don't modify | Growing `if/else` or `switch` | Strategy pattern + DI |
| **L** — Liskov Substitution | Subtypes honor parent contracts | `throw UnsupportedOperationException` in override | Redesign with proper interfaces |
| **I** — Interface Segregation | Small, role-specific interfaces | Implementing empty/stub methods | Split into focused interfaces |
| **D** — Dependency Inversion | Depend on abstractions | `new ConcreteClass()` in business logic | Inject interfaces via Spring DI |

---

## Common Interview Follow-Up Questions

**Q: "Which SOLID principle do you use the most?"**
> DIP — because Spring Boot's entire architecture is built on it. Every `@Service`, `@Repository`, and `@Component` wired through constructor injection IS Dependency Inversion. It makes every service testable and swappable.

**Q: "Can you over-apply SOLID?"**
> Absolutely. Creating interfaces for every class, splitting a 20-line service into 5 classes, or adding strategy patterns for code that'll never change — that's over-engineering. SOLID is a guideline, not a law. Apply it at boundary points and where change is likely.

**Q: "How does SOLID relate to microservices?"**
> SRP at the service level IS microservices — each service has one business responsibility. OCP is how we design plugin-like integrations. DIP is how we make services testable in isolation. ISP is why we create focused API contracts per consumer (API Gateway, BFF pattern). They scale from class-level to system-level.

**Q: "What happens when SOLID is NOT followed?"**
> I've inherited codebases where one `UserService` class was 3,000 lines with 40 methods. Any change was risky because everything was coupled. Tests were impossible because the class directly instantiated database connections. A simple feature (add SMS notification) took 2 weeks instead of 2 days because of the ripple effects. That's what happens without SOLID.
