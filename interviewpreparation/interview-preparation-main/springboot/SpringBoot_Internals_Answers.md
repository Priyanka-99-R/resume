# Spring Boot Internals — Interview Answers (Senior Java Engineer Perspective)

---

## Q1. What is Spring Boot?

Spring Boot is an opinionated framework built on top of the Spring ecosystem that lets you build production-ready applications with minimal configuration.

In practice, before Spring Boot, setting up a Spring project meant writing tons of XML or Java config — datasource beans, view resolvers, dispatcher servlets, etc. Spring Boot eliminates all that boilerplate. It gives you sensible defaults out of the box: embedded Tomcat, auto-configured beans, and starter dependencies.

**Real-world scenario:** When I need to spin up a new microservice, I go to Spring Initializr, pick my starters (Web, JPA, Security), and within minutes I have a running REST API — no XML, no manual server setup.

**Common mistake:** Developers sometimes think Spring Boot is a different framework from Spring. It's not — it's just a layer on top that makes Spring easier to use.

Spring Boot is essentially the modern way to do Spring development, and in any enterprise project today, it's the default choice.

---

## Q2. Why Spring Boot?

Spring Boot exists because traditional Spring setup was painful — too much boilerplate, too many XML files, and manual server configuration.

The key reasons we use Spring Boot in real projects:
- **Auto-configuration** — it detects what's on your classpath and configures beans automatically
- **Embedded server** — no need to deploy a WAR to an external Tomcat; just run a JAR
- **Starter dependencies** — one dependency pulls in everything you need (e.g., `spring-boot-starter-web` gives you Spring MVC, Jackson, Tomcat, validation)
- **Production-ready features** — health checks, metrics, externalized config out of the box

**Real-world scenario:** In my last project, we migrated from a legacy Spring MVC app with 15+ XML config files to Spring Boot. We cut the configuration code by 80% and deployment became a simple `java -jar app.jar`.

**Edge case:** Spring Boot's opinionated defaults work great 90% of the time, but sometimes you need to fight those defaults for custom use cases — knowing how to override them is key.

Spring Boot dramatically reduces time-to-production, which is why it's the industry standard for Java backend services.

---

## Q3. What is auto-configuration?

Auto-configuration is Spring Boot's mechanism to automatically configure beans based on what libraries are on your classpath — so you don't have to manually define them.

For example, if Spring Boot sees `spring-boot-starter-data-jpa` and an H2 driver on the classpath, it will automatically configure a `DataSource`, `EntityManagerFactory`, and `TransactionManager` without you writing a single `@Bean` definition.

**How it works:** Spring Boot ships with hundreds of auto-configuration classes (like `DataSourceAutoConfiguration`, `JacksonAutoConfiguration`). Each one uses `@Conditional` annotations to check: "Is a certain class present? Is a certain property set? Has the user already defined this bean?" If conditions are met, the bean is created automatically.

**Real-world scenario:** When I add `spring-boot-starter-data-redis` to my project, Spring Boot automatically configures a `RedisTemplate` and `RedisConnectionFactory`. I just inject `RedisTemplate` and start using it.

**Common mistake:** Assuming auto-configuration is magic and not understanding what beans it creates. Use `--debug` flag or `spring.boot.autoconfiguration` report to see what's being configured and what's being skipped.

Auto-configuration is the heart of Spring Boot — understanding it separates junior developers from senior ones.

---

## Q4. How does auto-configuration work internally?

Internally, auto-configuration works through a combination of `@EnableAutoConfiguration`, the `spring.factories` / `AutoConfiguration.imports` file, and `@Conditional` annotations.

Here's the flow:
1. `@SpringBootApplication` includes `@EnableAutoConfiguration`
2. This triggers `AutoConfigurationImportSelector`, which loads all auto-configuration class names from `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (or `spring.factories` in older versions)
3. Each auto-config class is annotated with conditions like `@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`
4. Spring evaluates these conditions — only classes whose conditions pass get loaded into the context

**Real-world scenario:** When debugging why a bean wasn't being created in one of our services, I checked the auto-config report (`/actuator/conditions`) and found that `@ConditionalOnClass(RedisClient.class)` was failing because we had the wrong Redis client dependency.

**Common mistake:** Not understanding the ordering — `@AutoConfigureAfter` and `@AutoConfigureBefore` control the order auto-config classes are processed. If your custom auto-config depends on another, you must declare the ordering.

This is the kind of internal knowledge that helps you debug production issues fast.

---

## Q5. What is `spring.factories`?

`spring.factories` is a metadata file located at `META-INF/spring.factories` that Spring Boot uses to discover and load auto-configuration classes, initializers, and listeners.

It's essentially a key-value mapping where the key is a fully qualified interface name and the value is a comma-separated list of implementation classes. For auto-configuration, the key is `org.springframework.boot.autoconfigure.EnableAutoConfiguration`.

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.example.MyAutoConfiguration,\
  com.example.AnotherAutoConfiguration
```

**Real-world scenario:** When I built a custom Spring Boot starter for our company's internal logging library, I registered my auto-configuration class in `spring.factories` so that any project using our starter would automatically get the logging beans configured.

**Important note:** Starting from Spring Boot 3.x, the preferred mechanism has shifted to `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` — a simpler file with one class per line. But `spring.factories` still works and you'll encounter it in many existing projects.

**Common mistake:** Putting your app's configuration classes in `spring.factories` instead of just using `@Configuration`. `spring.factories` is meant for library/starter auto-configurations, not for your application's own beans.

Understanding `spring.factories` is essential if you ever need to build reusable starters or debug auto-config loading issues.

---

## Q6. What is `@EnableAutoConfiguration`?

`@EnableAutoConfiguration` is the annotation that tells Spring Boot to start its auto-configuration mechanism — scanning and loading all eligible auto-configuration classes.

Under the hood, it imports `AutoConfigurationImportSelector`, which reads the list of auto-config classes from `spring.factories` / `AutoConfiguration.imports`, evaluates all `@Conditional` annotations, and registers the qualifying beans into the application context.

**Real-world scenario:** You rarely use `@EnableAutoConfiguration` directly because `@SpringBootApplication` already includes it. But when I needed fine-grained control — for example, excluding certain auto-configs — I used:

```java
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
```

This was useful when our service didn't need a database but had JPA on the classpath transitively.

**Common mistake:** Using `@EnableAutoConfiguration` alongside `@SpringBootApplication` — that's redundant. `@SpringBootApplication` is already a meta-annotation that includes it.

This annotation is the trigger for everything that makes Spring Boot "magic" — it's the entry point to the auto-config pipeline.

---

## Q7. What is `@SpringBootApplication`?

`@SpringBootApplication` is a convenience meta-annotation that combines three core annotations into one: `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`.

- **`@Configuration`** — marks the class as a source of bean definitions
- **`@EnableAutoConfiguration`** — triggers auto-configuration
- **`@ComponentScan`** — scans the package (and sub-packages) of the annotated class for `@Component`, `@Service`, `@Repository`, `@Controller`, etc.

**Real-world scenario:** In every Spring Boot project, your main class looks like:

```java
@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

**Common mistake:** Placing your main class in a deeply nested package like `com.company.project.config`. Since `@ComponentScan` scans from the annotated class's package downward, beans in `com.company.project.service` would NOT be picked up. Always place your main class at the root package.

**Edge case:** You can customize scanning using `scanBasePackages`:
```java
@SpringBootApplication(scanBasePackages = "com.company")
```

It's a simple annotation, but understanding what it's composed of helps you debug component scanning and auto-config issues.

---

## Q8. How does a Spring Boot application start?

When you call `SpringApplication.run()`, a well-defined startup sequence kicks off that bootstraps the entire application.

**The internal flow:**
1. **Create `SpringApplication` instance** — detects the application type (Servlet, Reactive, or None)
2. **Load `SpringApplicationRunListeners`** — fires startup events
3. **Prepare the Environment** — reads `application.properties/yml`, system properties, env variables
4. **Create the ApplicationContext** — `AnnotationConfigServletWebServerApplicationContext` for web apps
5. **Refresh the context** — this is where bean definitions are loaded, auto-config runs, dependencies are injected, and the embedded server starts
6. **Call `ApplicationRunner` / `CommandLineRunner`** — runs any startup logic you've defined
7. **Application is ready** — fires `ApplicationReadyEvent`

**Real-world scenario:** I've used `ApplicationRunner` to pre-load cache data on startup and `ApplicationReadyEvent` listeners to send a health notification to our monitoring system when a service comes up.

**Common mistake:** Putting heavy initialization logic in `@PostConstruct` — this blocks the entire context refresh. Use `ApplicationRunner` or async event listeners instead for non-critical startup tasks.

Understanding this flow is critical when you need to debug slow startups or ordering issues between beans.

---

## Q9. What is an embedded server in Spring Boot?

An embedded server means the web server (Tomcat, Jetty, or Undertow) runs inside your application as a dependency — you don't deploy to an external server.

In traditional Java EE, you'd build a WAR and deploy it to a standalone Tomcat. With Spring Boot, the server is just another JAR on the classpath. When your app starts, Spring Boot auto-configures and starts the server programmatically.

**Real-world scenario:** In our microservices architecture, every service runs as a self-contained JAR with embedded Tomcat. Deployment is just `java -jar order-service.jar`. This simplifies containerization — our Dockerfile is literally:
```dockerfile
FROM eclipse-temurin:17
COPY target/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Switching servers** is as simple as excluding Tomcat and adding Jetty:
```xml
<dependency>
    <groupId>spring-boot-starter-web</groupId>
    <exclusions>
        <exclusion>spring-boot-starter-tomcat</exclusion>
    </exclusions>
</dependency>
<dependency>spring-boot-starter-jetty</dependency>
```

**Common mistake:** Not tuning the embedded server for production — thread pool size, connection timeouts, and max connections still matter. Defaults are fine for dev, not for high-traffic production.

Embedded servers are a key enabler for the microservices + Docker deployment model that's standard today.

---

## Q10. What are starter dependencies?

Starter dependencies are curated, pre-configured bundles of dependencies that Spring Boot provides so you don't have to manually manage individual library versions.

Instead of adding Spring MVC, Jackson, Tomcat, Hibernate Validator, and logging dependencies one by one (and worrying about version compatibility), you just add:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**Common starters I use daily:**
- `spring-boot-starter-web` — REST APIs
- `spring-boot-starter-data-jpa` — database with Hibernate
- `spring-boot-starter-security` — authentication/authorization
- `spring-boot-starter-test` — JUnit, Mockito, MockMvc all-in-one
- `spring-boot-starter-actuator` — production monitoring

**Real-world scenario:** When building a new microservice, I add 3-4 starters and I'm ready to go. The BOM (`spring-boot-dependencies`) ensures all transitive versions are compatible — no more "dependency hell."

**Common mistake:** Adding individual libraries that conflict with what a starter already provides. For example, adding a different Jackson version alongside `spring-boot-starter-web` can cause serialization issues.

Starters are one of the biggest productivity boosters in Spring Boot — they handle dependency management so you can focus on business logic.

---

## Q11. How do you override auto-configuration?

The simplest way is to define your own `@Bean` — Spring Boot's auto-configuration is designed to back off when it detects a user-defined bean of the same type.

Most auto-config classes use `@ConditionalOnMissingBean`, which means: "Only create this bean if the user hasn't already defined one."

**Real-world scenario:** Spring Boot auto-configures a `DataSource` using `application.properties`. But in one project, we needed a custom `DataSource` with connection pool tuning and multi-tenancy routing:
```java
@Configuration
public class DataSourceConfig {
    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setMaximumPoolSize(50);
        ds.setConnectionTimeout(3000);
        // custom routing logic
        return new TenantRoutingDataSource(ds);
    }
}
```
Spring Boot sees this bean and skips `DataSourceAutoConfiguration` for that bean type.

**Other ways to override:**
- Override properties in `application.properties` (e.g., `server.port=9090`)
- Use `@EnableAutoConfiguration(exclude = ...)` to completely disable a specific auto-config
- Use `@AutoConfigureBefore/@AutoConfigureAfter` for ordering your config

**Common mistake:** Defining a bean with a different name but same type, then wondering why you have two beans and getting `NoUniqueBeanDefinitionException`. Use `@Primary` if you need to keep both.

Knowing how to override auto-config cleanly is essential — it's where you draw the line between Boot's defaults and your custom requirements.

---

## Q12. How do you disable auto-configuration?

You can disable specific auto-configuration classes using the `exclude` attribute on `@SpringBootApplication` or `@EnableAutoConfiguration`.

```java
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    SecurityAutoConfiguration.class
})
public class MyApplication { }
```

Alternatively, you can use the property-based approach:
```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

**Real-world scenario:** In a microservice that only processes messages from Kafka (no database), JPA was on the classpath as a transitive dependency. The app failed to start because `DataSourceAutoConfiguration` couldn't find a datasource URL. Excluding it fixed the issue instantly.

**When to use which approach:**
- `exclude` in annotation — when you always want it disabled for that app
- `spring.autoconfigure.exclude` in properties — when it varies per environment or profile

**Common mistake:** Excluding a parent auto-config but not its related ones. For example, excluding `DataSourceAutoConfiguration` but not `HibernateJpaAutoConfiguration` — the latter may still fail. Check the auto-config dependency chain.

**Pro tip:** Run with `--debug` to see the full auto-configuration report — it shows positive matches (applied) and negative matches (skipped), which tells you exactly what to exclude.

Disabling auto-config is a common requirement in microservice architectures where transitive dependencies pull in things you don't need.

---

## Q13. What are Profiles in Spring Boot?

Profiles are Spring's mechanism for environment-specific configuration — allowing you to have different beans and properties for dev, staging, and production environments.

In practice, you create separate config files:
- `application-dev.yml` — local database, debug logging
- `application-staging.yml` — staging database, moderate logging
- `application-prod.yml` — production database, minimal logging, SSL enabled

You can also conditionally load beans:
```java
@Configuration
@Profile("prod")
public class ProdCacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new RedisCacheManager(...); // Redis in prod
    }
}

@Configuration
@Profile("dev")
public class DevCacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(); // In-memory for dev
    }
}
```

**Real-world scenario:** In our production setup, we use profiles to switch between AWS S3 (prod) and local filesystem storage (dev) for file uploads, and between real payment gateways (prod) and sandbox/mock implementations (dev).

**Common mistake:** Hardcoding environment-specific values without profiles and relying on code changes for each deployment. Also, accidentally running with the wrong profile — always verify with the startup log: `The following profiles are active: prod`.

Profiles are fundamental to any professional Spring Boot project — they keep your code clean and your deployments safe.

---

## Q14. How do you activate profiles?

You activate profiles using `spring.profiles.active`, and there are multiple ways to set it depending on your deployment strategy.

**Common approaches (in order of precedence, high to low):**

1. **Command-line argument** (most common in production):
   ```bash
   java -jar app.jar --spring.profiles.active=prod
   ```

2. **Environment variable** (ideal for Docker/Kubernetes):
   ```bash
   SPRING_PROFILES_ACTIVE=prod java -jar app.jar
   ```

3. **JVM system property:**
   ```bash
   java -Dspring.profiles.active=prod -jar app.jar
   ```

4. **In `application.properties`** (for default profile):
   ```properties
   spring.profiles.active=dev
   ```

5. **Programmatically:**
   ```java
   SpringApplication app = new SpringApplication(MyApp.class);
   app.setAdditionalProfiles("prod");
   app.run(args);
   ```

**Real-world scenario:** In our Kubernetes deployments, we set `SPRING_PROFILES_ACTIVE` as an environment variable in the deployment YAML. This way, the same Docker image runs in any environment — only the profile changes.

**Common mistake:** Setting `spring.profiles.active=dev` in `application.properties` and forgetting to override it in production. The property file value is lowest priority, so command-line or env vars will override it, but it's still risky.

**Pro tip:** You can activate multiple profiles: `spring.profiles.active=prod,redis,aws`

Profile activation is a deployment concern, not a code concern — keep it externalized.

---

## Q15. What is externalized configuration?

Externalized configuration means keeping your application's configurable values outside the source code — so you can change behavior without recompiling or redeploying.

Spring Boot supports multiple external config sources with a well-defined precedence order (highest to lowest):
1. Command-line arguments
2. JVM system properties
3. OS environment variables
4. Profile-specific `application-{profile}.properties`
5. `application.properties` / `application.yml`
6. `@PropertySource` annotations
7. Default properties

**Real-world scenario:** In our microservices, database URLs, API keys, feature flags, and thread pool sizes are all externalized. In Kubernetes, we use ConfigMaps for non-sensitive config and Secrets for credentials:
```yaml
# Kubernetes ConfigMap
data:
  SPRING_DATASOURCE_URL: jdbc:postgresql://db-host:5432/orders
  APP_CACHE_TTL: "300"
```

Spring Boot automatically maps `APP_CACHE_TTL` environment variable to `app.cache.ttl` property (relaxed binding).

**Common mistake:** Hardcoding URLs, timeouts, or feature toggles in Java code. If it might change between environments or needs runtime tuning, externalize it.

**Edge case:** Property precedence can bite you — an environment variable will override your `application.yml` value, which can be confusing during debugging.

Externalized configuration is a Twelve-Factor App principle — it's non-negotiable in production-grade applications.

---

## Q16. `application.yml` vs `application.properties`?

Both serve the same purpose — externalizing configuration — but `application.yml` uses YAML's hierarchical structure while `.properties` uses flat key-value pairs.

**`application.properties` (flat):**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/db
spring.datasource.username=root
spring.datasource.hikari.maximum-pool-size=20
```

**`application.yml` (hierarchical):**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db
    username: root
    hikari:
      maximum-pool-size: 20
```

**Key differences:**
- YAML supports nested structures — less repetition, more readable for deeply nested configs
- YAML supports multi-document files (multiple profiles in one file using `---`)
- Properties files are simpler and less error-prone (no indentation issues)
- YAML supports lists and maps more naturally

**Real-world scenario:** In most of my projects, we use `application.yml` because microservice configs tend to be deeply nested (datasource, security, actuator, custom app properties). YAML makes this much more readable.

**Common mistake:** YAML indentation errors — a misplaced space can silently change which property you're setting. Also, mixing tabs and spaces in YAML will break your application. Use an IDE with YAML support.

**Pro tip:** You can use both in the same project — properties take precedence over YAML. But pick one and be consistent.

Choose YAML for complex configs, properties for simple ones — consistency within the team matters most.

---

## Q17. What is Spring Boot Actuator?

Actuator provides production-ready operational endpoints that let you monitor, manage, and inspect your running Spring Boot application.

Out of the box, it gives you endpoints like:
- `/actuator/health` — application health status (DB, disk, custom checks)
- `/actuator/metrics` — JVM memory, CPU, HTTP request stats, custom metrics
- `/actuator/info` — application info (build version, git commit)
- `/actuator/env` — all configuration properties
- `/actuator/loggers` — view and change log levels at runtime
- `/actuator/threaddump` — thread dump for debugging

**Real-world scenario:** In our production setup, `/health` is used by Kubernetes for liveness and readiness probes. Prometheus scrapes `/actuator/prometheus` for metrics, and we've written custom health indicators for downstream service checks:
```java
@Component
public class PaymentGatewayHealthIndicator implements HealthIndicator {
    public Health health() {
        boolean reachable = paymentClient.ping();
        return reachable ? Health.up().build()
                         : Health.down().withDetail("error", "Gateway unreachable").build();
    }
}
```

**Common mistake:** Exposing all actuator endpoints publicly in production without security. Endpoints like `/env` and `/heapdump` can leak sensitive data.

Actuator is not optional in production — it's how you achieve observability in Spring Boot services.

---

## Q18. How do you secure Actuator endpoints?

You secure Actuator by restricting which endpoints are exposed and who can access them, using a combination of exposure configuration and Spring Security.

**Step 1: Control exposure** — By default, only `/health` is exposed over HTTP. Selectively expose what you need:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
        # Never expose: env, heapdump, shutdown in prod
  endpoint:
    health:
      show-details: when-authorized
```

**Step 2: Secure with Spring Security:**
```java
@Configuration
public class ActuatorSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.requestMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeRequests()
            .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
            .anyRequest().hasRole("ACTUATOR_ADMIN")
            .and().httpBasic();
        return http.build();
    }
}
```

**Step 3: Change the base path** (security by obscurity as additional layer):
```yaml
management:
  server:
    port: 9090  # Separate port, not exposed publicly
  endpoints:
    web:
      base-path: /internal/manage
```

**Real-world scenario:** In our Kubernetes setup, we run actuator on a separate port (9090) that's only accessible within the cluster — external load balancers only route to port 8080. Health endpoints are open for K8s probes, everything else requires authentication.

**Common mistake:** Using `include: "*"` to expose everything and forgetting that endpoints like `/heapdump` can dump your entire JVM memory (including secrets) and `/shutdown` can literally kill your application.

Treat Actuator endpoints like admin panels — lock them down with the principle of least privilege.

---

## Q19. What are Spring Boot DevTools?

DevTools is a Spring Boot module that improves the developer experience by providing automatic restarts, live reload, and development-specific defaults.

Add it with:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

**Key features:**
- **Automatic restart** — when classpath files change (code save), the app restarts using a custom classloader (much faster than a cold restart)
- **LiveReload** — triggers browser refresh when resources change (templates, static files)
- **Relaxed caching** — disables template caching, enables debug logging
- **Remote debugging support** — allows DevTools features over HTTP for remote apps

**Real-world scenario:** During development of a REST API, every time I save a Java file in IntelliJ, DevTools automatically restarts the context in ~2 seconds instead of 15+ seconds for a full restart. For Thymeleaf template changes, it's instant with LiveReload.

**Important:** DevTools is automatically disabled in production — it detects when you run from a packaged JAR (`java -jar`) and deactivates itself. The `optional=true` flag also prevents it from being a transitive dependency.

**Common mistake:** Adding DevTools without `optional=true` or proper scope — it could accidentally end up in production builds. Also, confusing DevTools restart with JRebel-style hot reload — DevTools still restarts the context, just faster.

DevTools is a must-have during development — the feedback loop improvement alone makes it worth it.

---

## Q20. Hot reload vs restart?

These are two different strategies for applying code changes during development — **restart** recreates the Spring ApplicationContext using a fast classloader, while **hot reload (live reload)** refreshes specific resources without restarting the context.

**Restart (DevTools):**
- Triggered when Java classes change on the classpath
- Uses two classloaders: a **base classloader** (for third-party JARs, rarely changes) and a **restart classloader** (for your project classes)
- Only the restart classloader is discarded and recreated — making it much faster (2-5 seconds vs 15-30 seconds cold start)
- All beans are re-created, context is fully refreshed

**Hot Reload / Live Reload:**
- Triggered when static resources or templates change (HTML, CSS, JS)
- No context restart — just the changed resource is picked up
- Browser auto-refreshes via LiveReload protocol
- Near-instant feedback

**Real-world scenario:** When working on a full-stack Spring Boot + Thymeleaf app, I modify a Java controller (triggers restart in ~3 seconds) vs modifying a Thymeleaf template (hot reloaded instantly without restart).

**True Hot Reload (JRebel/DCEVM):**
- Tools like JRebel can hot-swap method bodies, add fields/methods without any restart
- More powerful but commercial (JRebel) or requires special JVM setup (DCEVM)

**Common mistake:** Expecting DevTools to hot-swap code like JRebel — it doesn't. It's a fast restart, not true hot code replacement. Adding new methods or changing method signatures always requires a restart.

Understand the distinction — restart for code changes, hot reload for resources. Use the right tool for the right job.

---

## Q21. WAR vs JAR deployment?

JAR deployment packages your application with an embedded server as a standalone executable, while WAR deployment produces a web archive meant to be deployed on an external application server.

**JAR (Modern, recommended):**
```bash
java -jar myapp.jar
```
- Self-contained — embedded Tomcat/Jetty inside
- Perfect for microservices, Docker, Kubernetes
- Spring Boot's default packaging

**WAR (Legacy/Enterprise):**
- Deployed to external Tomcat, WildFly, WebSphere
- Required when organization mandates shared application servers
- Needs `SpringBootServletInitializer`:
```java
public class MyApp extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MyApp.class);
    }
}
```

**Real-world scenario:** In a greenfield microservice project, we always use JAR. But I've worked with a banking client where compliance required deployment on a hardened WebSphere server — WAR was the only option. Spring Boot supports both, so the same codebase can be packaged either way.

**Common mistake:** Choosing WAR "just because that's how we've always done it." Unless there's a specific infrastructure constraint, JAR is simpler, more portable, and aligned with modern deployment practices.

Go with JAR unless you have a concrete reason for WAR — in 2024+, JAR is the standard.

---

## Q22. What is `@ConfigurationProperties`?

`@ConfigurationProperties` is a type-safe way to bind a group of related configuration properties to a POJO, instead of injecting them one by one with `@Value`.

```yaml
# application.yml
app:
  payment:
    gateway-url: https://api.payment.com
    timeout: 5000
    retry-count: 3
    enabled: true
```

```java
@Component
@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {
    private String gatewayUrl;
    private int timeout;
    private int retryCount;
    private boolean enabled;
    // getters and setters
}
```

Now you inject `PaymentProperties` wherever needed — clean, validated, and testable.

**Why use it over `@Value`:**
- Groups related config logically
- Supports validation with `@Validated` + JSR-303 annotations (`@NotNull`, `@Min`, `@Max`)
- IDE auto-completion when you add `spring-boot-configuration-processor`
- Immutable support with constructor binding
- Much easier to test — just create the POJO in your test

**Real-world scenario:** In our e-commerce platform, each external integration (payment, shipping, notification) has its own `@ConfigurationProperties` class. This keeps config organized and validates at startup — if a required URL is missing, the app fails fast.

**Common mistake:** Forgetting to enable it — either annotate with `@Component` or use `@EnableConfigurationProperties(PaymentProperties.class)` on a config class. Without this, the binding won't happen.

For anything beyond a single property, always prefer `@ConfigurationProperties` over `@Value` — it's the professional approach.

---

## Q23. What is `@Value`?

`@Value` is a Spring annotation that injects a single externalized property value directly into a field, constructor parameter, or method parameter.

```java
@Service
public class NotificationService {
    @Value("${app.notification.sender-email}")
    private String senderEmail;

    @Value("${app.notification.max-retries:3}")  // default value = 3
    private int maxRetries;

    @Value("${app.feature.sms-enabled:false}")
    private boolean smsEnabled;
}
```

**Key features:**
- Supports default values with `:` syntax
- Supports SpEL (Spring Expression Language): `@Value("#{2 * 10}")`
- Can inject system properties: `@Value("${java.home}")`
- Works with constructor injection too

**When to use `@Value` vs `@ConfigurationProperties`:**
- **`@Value`** — for 1-2 isolated properties (a feature flag, a single URL)
- **`@ConfigurationProperties`** — for a group of related properties (database config, API client settings)

**Real-world scenario:** I use `@Value` for simple feature toggles:
```java
@Value("${app.feature.new-checkout:false}")
private boolean newCheckoutEnabled;
```

**Common mistakes:**
- Missing the `${}` syntax — writing `@Value("app.name")` instead of `@Value("${app.name}")` injects the literal string
- Not providing a default value and the property is missing — app fails to start with `IllegalArgumentException`
- Using `@Value` in a class not managed by Spring (missing `@Component`) — injection won't work

`@Value` is fine for quick, simple property injection, but for anything structured, graduate to `@ConfigurationProperties`.

---

## Q24. What is `@ConditionalOnClass`?

`@ConditionalOnClass` is a Spring Boot condition annotation that tells Spring to only load a bean or configuration class if a specified class exists on the classpath.

```java
@Configuration
@ConditionalOnClass(RedisTemplate.class)
public class RedisAutoConfiguration {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        return template;
    }
}
```

This configuration only activates if `RedisTemplate.class` is on the classpath — meaning the Redis dependency is present.

**Why it matters:** This is the backbone of Spring Boot's auto-configuration. It allows Spring Boot to ship hundreds of auto-config classes but only activate the ones relevant to your project.

**Real-world scenario:** When I built a custom Spring Boot starter for our internal caching library, I used `@ConditionalOnClass` to provide different implementations:
```java
@Bean
@ConditionalOnClass(name = "com.hazelcast.core.HazelcastInstance")
public CacheManager hazelcastCacheManager() { ... }

@Bean
@ConditionalOnClass(name = "redis.clients.jedis.Jedis")
public CacheManager redisCacheManager() { ... }
```

The starter auto-detects which cache provider is available and configures accordingly.

**Common mistake:** Using `@ConditionalOnClass` with a class that's always on the classpath (like `String.class`) — the condition is useless. Also, note the `name` attribute (string-based) is safer when the class might not be present, avoiding `ClassNotFoundException` at annotation processing time.

This is a fundamental building block for writing library auto-configurations and understanding how Spring Boot decides what to configure.

---

## Q25. What is `@ConditionalOnProperty`?

`@ConditionalOnProperty` is a condition annotation that loads a bean or configuration only when a specific property is set (or has a specific value) in your configuration.

```java
@Configuration
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true", matchIfMissing = false)
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new RedisCacheManager(...);
    }
}
```

This config only loads if `app.cache.enabled=true` is in your properties.

**Key attributes:**
- `name` / `prefix` — the property key to check
- `havingValue` — the expected value (defaults to anything non-false)
- `matchIfMissing` — what happens if the property is absent (default: `false` — don't load)

**Real-world scenario:** We use this extensively for feature flags and conditional integrations:
```java
// Only enable Kafka consumer if the feature is toggled on
@ConditionalOnProperty(name = "app.kafka.consumer.enabled", havingValue = "true")
@Component
public class OrderEventConsumer { ... }

// Enable scheduled jobs only in the scheduler instance
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = false)
@Configuration
@EnableScheduling
public class SchedulerConfig { ... }
```

This lets us deploy the same codebase but toggle behaviors per environment — one instance processes HTTP, another runs scheduled jobs.

**Common mistake:** Setting `matchIfMissing = true` when you actually want the feature OFF by default. Also, not understanding that `havingValue` is case-sensitive — `"True"` won't match if the property is `"true"`.

`@ConditionalOnProperty` is one of the most powerful tools for building flexible, configurable applications — I use it in almost every project.
