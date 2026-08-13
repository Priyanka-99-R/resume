# SPRING BOOT INTERVIEW QUESTIONS - SENIOR LEVEL ANSWERS (5-8 YOE)

## BASIC QUESTIONS (1-20)

--------------------------------------------------
### Question 1: What is Spring Boot?

**1. Definition:**
Spring Boot is an opinionated framework built on top of Spring Framework that simplifies the bootstrapping and development of production-ready Spring applications through auto-configuration, embedded servers, and starter dependencies.

**2. Why it matters:**
In production, development velocity and consistency are critical. Spring Boot eliminates boilerplate configuration, enforces best practices, and provides production-ready features (metrics, health checks) out of the box—reducing time-to-market significantly.

**3. Internal working:**
- Spring Boot uses `SpringApplication.run()` which initializes `ApplicationContext`
- Auto-configuration classes are loaded via `spring.factories` (or `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` in Spring Boot 3.x)
- `@Conditional` annotations determine which beans to create based on classpath, properties, and existing beans
- Embedded server (Tomcat/Jetty/Undertow) is initialized as part of the application context

**4. Real-world example:**
In a payment gateway microservice, Spring Boot allowed our team to spin up a new service in hours instead of days. Auto-configured DataSource, connection pooling (HikariCP), and actuator endpoints meant we focused on business logic while getting production-ready infrastructure for free.

**5. Pitfalls:**
- Blindly accepting auto-configuration without understanding what's being configured
- Not excluding unnecessary auto-configurations, leading to bloated applications
- Ignoring actuator security in production environments

**6. Follow-up questions:**
- How would you debug what auto-configurations are being applied?
- How do you customize embedded server thread pools for high-traffic scenarios?
- What's the difference between Spring Boot 2.x and 3.x auto-configuration loading?

**7. Senior summary:**
"Spring Boot is an opinionated layer over Spring that provides auto-configuration, embedded servers, and production-ready features. It dramatically reduces boilerplate while still allowing full customization when needed. I leverage it to build microservices quickly while maintaining production-grade standards."

--------------------------------------------------
### Question 2: Why Spring Boot over Spring MVC?

**1. Definition:**
Spring MVC is just the web layer of Spring Framework requiring extensive XML/Java configuration. Spring Boot is a complete solution that auto-configures Spring MVC along with all supporting infrastructure.

**2. Why it matters:**
In enterprise environments, configuration consistency across teams and environments is crucial. Spring Boot's convention-over-configuration approach ensures standardized setups while reducing human error in configuration.

**3. Internal working:**
Spring MVC alone requires:
- Manual DispatcherServlet configuration
- ViewResolver setup
- HandlerMapping configuration
- External server deployment (WAR to Tomcat)

Spring Boot auto-configures all of this via `WebMvcAutoConfiguration`, plus adds:
- Embedded server
- Default error handling
- Static resource handling
- JSON serialization (Jackson)

**4. Real-world example:**
Migrating a legacy Spring MVC banking application (500+ lines of XML config) to Spring Boot reduced configuration to ~50 lines of properties. Deployment shifted from WAR files on external Tomcat to self-contained JARs, simplifying our CI/CD pipeline.

**5. Pitfalls:**
- Assuming Spring Boot replaces Spring—it's still Spring underneath
- Over-customizing and fighting against Boot's conventions
- Not understanding that you can still use XML config if needed

**6. Follow-up questions:**
- When would you choose Spring MVC without Boot?
- How do you override Spring Boot's default MVC configurations?

**7. Senior summary:**
"Spring Boot eliminates the configuration overhead of Spring MVC by auto-configuring the web layer, providing embedded servers, and establishing sensible defaults. It's not a replacement but an enhancement that lets teams focus on business logic rather than plumbing."

--------------------------------------------------
### Question 3: What problems does Spring Boot solve?

**1. Definition:**
Spring Boot addresses: (1) Configuration complexity, (2) Dependency management hell, (3) Deployment complexity, (4) Production readiness, and (5) Development-to-production parity.

**2. Why it matters:**
Enterprise applications often fail not due to business logic but due to misconfiguration, dependency conflicts, or environment differences. Spring Boot systematically eliminates these failure modes.

**3. Internal working:**
- **Starter POMs**: Curated, tested dependency sets (no version conflicts)
- **Auto-configuration**: Conditional bean creation based on classpath scanning
- **Embedded servers**: Same runtime in dev and prod
- **Externalized config**: Property sources hierarchy for environment-specific overrides
- **Actuator**: Production monitoring built-in

**4. Real-world example:**
In an e-commerce platform, we had "works on my machine" issues constantly. After Spring Boot adoption:
- Starter dependencies eliminated JAR hell
- Embedded Tomcat meant identical behavior across environments
- Actuator gave us health checks for Kubernetes probes

**5. Pitfalls:**
- Treating starters as magic—not understanding transitive dependencies
- Not leveraging externalized configuration properly (hardcoding values)
- Ignoring the actuator for production monitoring

**6. Follow-up questions:**
- How do you handle dependency conflicts when a starter brings in unwanted versions?
- How does Spring Boot's property source hierarchy work?

**7. Senior summary:**
"Spring Boot solves configuration complexity through auto-configuration, dependency management through starters, deployment complexity through embedded servers, and production readiness through actuator. It essentially codifies best practices so teams don't reinvent the wheel."

--------------------------------------------------
### Question 4: What is auto-configuration?

**1. Definition:**
Auto-configuration is Spring Boot's mechanism to automatically configure beans based on classpath contents, existing beans, and properties—eliminating explicit configuration for common scenarios.

**2. Why it matters:**
It enables the "just add dependency and it works" experience while still allowing full override capability. This dramatically accelerates development without sacrificing flexibility.

**3. Internal working:**
1. `@EnableAutoConfiguration` triggers scanning of `META-INF/spring.factories` (or `.imports` file in 3.x)
2. Each auto-configuration class uses `@Conditional*` annotations:
   - `@ConditionalOnClass`: Only if class exists on classpath
   - `@ConditionalOnMissingBean`: Only if you haven't defined this bean
   - `@ConditionalOnProperty`: Only if property is set
3. Order controlled via `@AutoConfigureBefore`/`@AutoConfigureAfter`
4. Debug with `--debug` flag or `debug=true` property

**4. Real-world example:**
Adding `spring-boot-starter-data-redis` to a session caching service:
- `RedisAutoConfiguration` detects Jedis/Lettuce on classpath
- Creates `RedisConnectionFactory` with sensible defaults
- `RedisTemplate` bean auto-created
- Override by simply defining your own `RedisConnectionFactory` bean

**5. Pitfalls:**
- Not knowing which auto-configurations are active (use actuator `/conditions` endpoint)
- Creating beans that conflict with auto-configured ones without proper `@Primary`
- Assuming auto-configuration means zero configuration—production often needs tuning

**6. Follow-up questions:**
- How would you create your own auto-configuration for a custom library?
- Explain the role of `@ConditionalOnMissingBean` in auto-configuration classes.

**7. Senior summary:**
"Auto-configuration uses conditional bean registration to automatically configure components based on classpath, properties, and existing beans. It follows a 'back-off' pattern—if you define a bean, auto-configuration doesn't override it. I always check the conditions report to understand what's being auto-configured."

--------------------------------------------------
### Question 5: What is @SpringBootApplication?

**1. Definition:**
`@SpringBootApplication` is a meta-annotation combining `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`—serving as the entry point for Spring Boot applications.

**2. Why it matters:**
It establishes the component scanning base package and triggers auto-configuration. Misplacing this annotation is a common source of "bean not found" errors.

**3. Internal working:**
```java
@SpringBootConfiguration  // Same as @Configuration
@EnableAutoConfiguration  // Triggers auto-config loading
@ComponentScan           // Scans current package and below
public @interface SpringBootApplication
```

Key behaviors:
- Component scanning starts from the annotated class's package
- `exclude` and `excludeName` attributes allow disabling specific auto-configurations
- `scanBasePackages` overrides default scanning behavior

**4. Real-world example:**
In a multi-module Maven project, placing `@SpringBootApplication` in `com.company.app` while services were in `com.company.services` caused beans to not be found. Solution: Either move the main class to `com.company` or explicitly set `scanBasePackages`.

**5. Pitfalls:**
- Placing main class in a sub-package, missing sibling package components
- Not understanding that it's just a convenience annotation
- Using both `@SpringBootApplication` and explicit `@ComponentScan` incorrectly

**6. Follow-up questions:**
- How would you exclude a specific auto-configuration?
- What happens if you have two classes with `@SpringBootApplication`?

**7. Senior summary:**
"@SpringBootApplication combines @Configuration, @EnableAutoConfiguration, and @ComponentScan. It should be placed in a root package so component scanning finds all beans. I use its exclude attribute to disable unwanted auto-configurations like DataSourceAutoConfiguration when not needed."

--------------------------------------------------
### Question 6: Difference between Spring and Spring Boot?

**1. Definition:**
Spring is the core dependency injection and enterprise integration framework. Spring Boot is an opinionated project that simplifies Spring application setup with auto-configuration, starters, and embedded servers.

**2. Why it matters:**
Understanding this distinction is crucial because Spring Boot doesn't replace Spring—it enhances it. All Spring concepts (DI, AOP, transactions) work identically in Boot.

**3. Internal working:**

| Aspect | Spring | Spring Boot |
|--------|--------|-------------|
| Configuration | Manual (XML/Java) | Auto + Manual |
| Dependencies | Individual selection | Curated starters |
| Server | External container | Embedded |
| Production features | Manual setup | Actuator built-in |
| Build | WAR typically | JAR typically |

**4. Real-world example:**
A legacy Spring application required 15+ config files, explicit dependency versions, and external Tomcat. The same functionality in Spring Boot: one `application.yml`, one starter dependency, and `mvn spring-boot:run`.

**5. Pitfalls:**
- Thinking Spring Boot is a different framework (it's still Spring)
- Believing Spring Boot can't do something because it's "opinionated"
- Not leveraging Spring expertise when using Boot

**6. Follow-up questions:**
- Can you use XML configuration in Spring Boot?
- How do you migrate a Spring MVC app to Spring Boot?

**7. Senior summary:**
"Spring is the foundational DI and enterprise framework. Spring Boot is a productivity layer that provides auto-configuration, embedded servers, and production features. The core programming model is identical—Boot just eliminates boilerplate. Any Spring knowledge directly applies to Boot."

--------------------------------------------------
### Question 7: What is embedded server?

**1. Definition:**
An embedded server is a servlet container (Tomcat, Jetty, Undertow) packaged within the application JAR, starting as part of the application rather than requiring external deployment.

**2. Why it matters:**
Embedded servers enable the "build once, run anywhere" promise. The same JAR runs identically on a developer laptop and in Kubernetes, eliminating environment-specific deployment issues.

**3. Internal working:**
1. Spring Boot's `spring-boot-starter-web` includes `spring-boot-starter-tomcat` by default
2. `ServletWebServerApplicationContext` detects embedded server on classpath
3. `TomcatServletWebServerFactory` creates and configures Tomcat instance
4. Server starts as part of `ApplicationContext.refresh()`
5. Shutdown hooks ensure graceful termination

**4. Real-world example:**
Our microservices platform runs 200+ Spring Boot services in Kubernetes. Each service is a self-contained JAR with embedded Tomcat. Docker images are simply `FROM openjdk` + `COPY app.jar`. No Tomcat installation, no WAR deployment, no server configuration drift.

**5. Pitfalls:**
- Not tuning thread pools for production load (defaults may be insufficient)
- Ignoring graceful shutdown configuration (`server.shutdown=graceful`)
- Not configuring appropriate connection timeouts

**6. Follow-up questions:**
- How do you switch from Tomcat to Undertow?
- How do you configure thread pool sizes for high-concurrency scenarios?

**7. Senior summary:**
"Embedded servers package the servlet container within the application JAR, eliminating external deployment. This enables consistent dev-to-prod parity and simplifies containerization. I always tune thread pools, connection limits, and enable graceful shutdown for production deployments."

--------------------------------------------------
### Question 8: Which servers Spring Boot supports?

**1. Definition:**
Spring Boot supports Tomcat (default), Jetty, and Undertow as embedded servlet containers, plus Netty for reactive applications.

**2. Why it matters:**
Different servers have different performance characteristics. Undertow excels at high concurrency, Jetty at low memory footprint, Tomcat at ecosystem compatibility.

**3. Internal working:**

| Server | Best For | Key Characteristic |
|--------|----------|-------------------|
| Tomcat | General purpose, ecosystem | Mature, well-documented |
| Jetty | Low memory, HTTP/2 | Lightweight, async |
| Undertow | High concurrency | Non-blocking, fast |
| Netty | Reactive/WebFlux | Event-loop, non-blocking |

Switching servers:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-undertow</artifactId>
</dependency>
```

**4. Real-world example:**
A real-time bidding platform needed to handle 50K concurrent connections. Switched from Tomcat to Undertow, reducing memory per connection and improving p99 latency by 30%.

**5. Pitfalls:**
- Switching servers without benchmarking for your specific workload
- Assuming Undertow/Jetty are always faster (depends on use case)
- Not adjusting configuration when switching (each has different tuning parameters)

**6. Follow-up questions:**
- When would you choose Netty over servlet-based servers?
- How do thread models differ between Tomcat and Undertow?

**7. Senior summary:**
"Spring Boot supports Tomcat, Jetty, Undertow for servlet workloads, and Netty for reactive. Server choice depends on workload: Tomcat for compatibility, Undertow for high concurrency, Netty for reactive. I benchmark with realistic load before committing to a server choice."

--------------------------------------------------
### Question 9: Default port of Spring Boot?

**1. Definition:**
Spring Boot's default server port is 8080 for web applications and 8443 for HTTPS when SSL is configured.

**2. Why it matters:**
Understanding default ports is essential for container orchestration, firewall rules, and load balancer configuration. Port conflicts are a common deployment issue.

**3. Internal working:**
- Port configured via `server.port` property
- `0` means random available port (useful for testing)
- `-1` disables the web server entirely
- At startup, Spring Boot binds to the port during `WebServerInitializedEvent`

**4. Real-world example:**
In Kubernetes, we override `server.port=8080` to maintain consistency but use different service ports (80) at the Service level. Health probes hit `8080/actuator/health`. For local development with multiple services, we use random ports (`server.port=0`) and service discovery.

**5. Pitfalls:**
- Hardcoding port instead of externalizing it
- Not handling port-already-in-use errors gracefully
- Forgetting management port separation (`management.server.port`)

**6. Follow-up questions:**
- How do you run multiple Spring Boot apps locally without port conflicts?
- What's the purpose of `management.server.port`?

**7. Senior summary:**
"Default is 8080, changed via `server.port` property. In production, I externalize this through environment variables. For microservices, I often use port 0 with service discovery or separate management port for actuator endpoints for security isolation."

--------------------------------------------------
### Question 10: How to change server port?

**1. Definition:**
Server port is changed via `server.port` property in `application.properties/yml`, command-line argument, or environment variable.

**2. Why it matters:**
Port configuration strategy affects deployment flexibility. Proper externalization enables the same artifact to run in different environments without rebuilding.

**3. Internal working:**
Property source hierarchy (highest to lowest priority):
1. Command line: `--server.port=9090`
2. OS environment: `SERVER_PORT=9090`
3. `application-{profile}.properties`
4. `application.properties`
5. Default (8080)

```yaml
# application.yml
server:
  port: ${PORT:8080}  # Environment variable with fallback
```

**4. Real-world example:**
Our deployment strategy:
- Local dev: `application-local.yml` with port 8080
- Docker: `SERVER_PORT` env var from orchestrator
- Kubernetes: ConfigMap sets port, but container always exposes 8080 internally
- Cloud Run/Heroku: `PORT` env var set by platform

**5. Pitfalls:**
- Hardcoding ports in properties files committed to source control
- Not understanding property source precedence
- Using different ports in different environments unnecessarily

**6. Follow-up questions:**
- How does Spring Boot relax property binding work?
- What's the difference between `server.port` and `management.server.port`?

**7. Senior summary:**
"I use environment variables with fallbacks: `server.port=${PORT:8080}`. This allows platform-specific ports (Cloud Run, Kubernetes) while maintaining sensible defaults. Never hardcode ports in committed configuration—externalize for deployment flexibility."

--------------------------------------------------
### Question 11: What is starter dependency?

**1. Definition:**
Starter dependencies are curated, pre-tested sets of transitive dependencies that provide all required libraries for a specific functionality with compatible versions.

**2. Why it matters:**
Dependency management is a major source of production issues. Starters eliminate version conflicts and ensure all required libraries are included—no more missing dependencies at runtime.

**3. Internal working:**
A starter typically contains:
- No code (usually)
- `pom.xml` with dependencies
- Auto-configuration trigger

Example `spring-boot-starter-data-jpa` includes:
- Hibernate (ORM)
- Spring Data JPA
- HikariCP (connection pool)
- Spring JDBC
- Transaction management

The BOM (`spring-boot-dependencies`) manages all versions centrally.

**4. Real-world example:**
Adding database support to a service:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```
This single line brings in ~15 compatible dependencies. Without starters, you'd manually manage versions and likely face `NoSuchMethodError` due to version mismatches.

**5. Pitfalls:**
- Not understanding transitive dependencies (use `mvn dependency:tree`)
- Overriding starter versions without testing compatibility
- Including multiple starters with conflicting dependencies

**6. Follow-up questions:**
- How do you override a transitive dependency version in a starter?
- How would you create a custom starter for your organization?

**7. Senior summary:**
"Starters are curated dependency sets that provide everything needed for specific functionality with tested-compatible versions. They eliminate JAR hell and ensure consistency. I always check the dependency tree to understand what's being pulled in and exclude unnecessary transitive dependencies."

--------------------------------------------------
### Question 12: Why spring-boot-starter-parent?

**1. Definition:**
`spring-boot-starter-parent` is a parent POM that provides dependency management, plugin configuration, and resource filtering defaults for Spring Boot projects.

**2. Why it matters:**
Consistent build configuration across projects reduces maintenance burden. The parent POM ensures all Spring Boot projects in an organization build identically.

**3. Internal working:**
Parent provides:
- `spring-boot-dependencies` BOM (version management)
- Plugin configurations (compiler, surefire, resources)
- Property defaults (`java.version`, encoding)
- Resource filtering for `application.properties`
- Executable JAR packaging

Alternative without parent:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>3.2.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**4. Real-world example:**
Enterprise projects often have a corporate parent POM. We import Spring Boot BOM instead:
```xml
<parent>
    <groupId>com.company</groupId>
    <artifactId>corporate-parent</artifactId>
</parent>
<!-- Import Spring Boot BOM -->
```
This maintains corporate governance while getting Spring Boot dependency management.

**5. Pitfalls:**
- Overriding parent properties without understanding impact
- Not realizing you can use BOM import instead of parent
- Forgetting to configure executable JAR manually when not using parent

**6. Follow-up questions:**
- How do you use Spring Boot without the parent POM?
- What does the `repackage` goal do?

**7. Senior summary:**
"The parent POM provides version management, build plugins, and resource filtering. In enterprise settings where a corporate parent is required, I import the `spring-boot-dependencies` BOM instead. This gives version management without mandating the parent."

--------------------------------------------------
### Question 13: application.properties vs application.yml

**1. Definition:**
Both are configuration files; `.properties` uses key=value format while `.yml` uses hierarchical YAML syntax. They're functionally equivalent—Spring Boot reads both.

**2. Why it matters:**
Configuration readability and maintainability affect operations. YAML's hierarchical structure reduces repetition for nested properties but introduces whitespace sensitivity issues.

**3. Internal working:**
Processing order:
1. `application.properties` outside JAR
2. `application.yml` outside JAR
3. `application.properties` inside JAR
4. `application.yml` inside JAR

Both support:
- Profile-specific files (`application-prod.yml`)
- Placeholder resolution (`${VARIABLE:default}`)
- Spring profiles in YAML (`---` separator)

**4. Real-world example:**
```yaml
# application.yml - Complex nested config
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```
vs
```properties
# application.properties - Same config
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

YAML is cleaner for nested config; properties better for simple key-values.

**5. Pitfalls:**
- YAML whitespace/indentation errors (invisible bugs)
- Mixing both formats causing confusion
- Not knowing both can coexist (properties takes precedence for same key)

**6. Follow-up questions:**
- How do you define multiple profiles in a single YAML file?
- What's the loading order when both files exist?

**7. Senior summary:**
"Functionally equivalent—I prefer YAML for complex nested configurations due to readability, but properties for simple settings. YAML's whitespace sensitivity can cause subtle bugs. In teams, I enforce one format for consistency."

--------------------------------------------------
### Question 14: How to run Spring Boot application?

**1. Definition:**
Spring Boot applications run via: IDE run, Maven/Gradle plugin, executable JAR, or as a deployed WAR (legacy).

**2. Why it matters:**
Different execution methods suit different stages (dev, CI, production). Understanding each ensures efficient development workflows and proper production deployment.

**3. Internal working:**

| Method | Use Case | Command |
|--------|----------|---------|
| IDE | Development | Run/Debug main class |
| Maven | Dev/CI | `mvn spring-boot:run` |
| Gradle | Dev/CI | `./gradlew bootRun` |
| JAR | Production | `java -jar app.jar` |
| WAR | Legacy servers | Deploy to external Tomcat |

The `spring-boot-maven-plugin` creates an uber-JAR with:
- Application classes in `BOOT-INF/classes`
- Dependencies in `BOOT-INF/lib`
- Spring Boot loader in root

**4. Real-world example:**
Our CI/CD pipeline:
1. Build: `mvn clean package -DskipTests`
2. Test: `mvn verify`
3. Docker: `COPY target/*.jar app.jar`
4. Run: `java -XX:+UseContainerSupport -jar app.jar`

JVM flags for containers: `-XX:MaxRAMPercentage=75.0`

**5. Pitfalls:**
- Using `mvn spring-boot:run` in production (meant for dev)
- Not setting proper JVM memory flags for containers
- Missing the executable permission on JAR in some environments

**6. Follow-up questions:**
- How do you pass JVM arguments when running the JAR?
- What's the difference between the executable JAR and regular JAR?

**7. Senior summary:**
"Development uses IDE or Maven plugin for hot reload. Production uses executable JAR with proper JVM flags. For containers, I ensure JVM respects container memory limits with `-XX:+UseContainerSupport`. The JAR is self-contained—no external dependencies needed."

--------------------------------------------------
### Question 15: What is CommandLineRunner?

**1. Definition:**
`CommandLineRunner` is a functional interface that executes code after the application context is initialized, receiving command-line arguments as a String array.

**2. Why it matters:**
Initialization logic (cache warming, data loading, connectivity checks) must run at the right lifecycle point. CommandLineRunner ensures dependencies are injected before execution.

**3. Internal working:**
```java
@Component
public class StartupRunner implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // Executed after ApplicationContext is ready
        // args contains command-line arguments
    }
}
```

Execution order:
1. ApplicationContext created
2. All beans initialized
3. `CommandLineRunner` and `ApplicationRunner` beans executed
4. Application fully started

Multiple runners ordered via `@Order` annotation.

**4. Real-world example:**
In a pricing service:
```java
@Component
@Order(1)
public class CacheWarmer implements CommandLineRunner {
    @Autowired
    private PricingCache cache;

    @Override
    public void run(String... args) {
        log.info("Warming pricing cache...");
        cache.loadAllPrices();  // Prevent cold-start latency
    }
}
```

**5. Pitfalls:**
- Putting long-running tasks that block application startup
- Not handling exceptions (crashes the app if uncaught)
- Confusing with `@PostConstruct` (different lifecycle point)

**6. Follow-up questions:**
- What's the difference between `CommandLineRunner` and `@PostConstruct`?
- How do you order multiple `CommandLineRunner` beans?

**7. Senior summary:**
"CommandLineRunner executes after full context initialization with access to command-line args. I use it for startup tasks like cache warming or connectivity verification. Unlike @PostConstruct, it runs after ALL beans are ready, preventing dependency issues."

--------------------------------------------------
### Question 16: What is ApplicationRunner?

**1. Definition:**
`ApplicationRunner` is similar to `CommandLineRunner` but receives `ApplicationArguments` instead of a raw String array, providing parsed access to option arguments.

**2. Why it matters:**
When command-line arguments follow conventions (`--key=value`), `ApplicationRunner` provides cleaner access than manual parsing.

**3. Internal working:**
```java
@Component
public class AppRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        // --server.port=8080 --debug
        args.getOptionNames();           // [server.port, debug]
        args.getOptionValues("debug");   // []
        args.containsOption("debug");    // true
        args.getNonOptionArgs();         // remaining args
    }
}
```

**4. Real-world example:**
A data migration tool:
```java
@Component
public class MigrationRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        if (args.containsOption("migrate")) {
            String version = args.getOptionValues("version").get(0);
            migrationService.migrateTo(version);
        }
    }
}
```
Run: `java -jar app.jar --migrate --version=2.0`

**5. Pitfalls:**
- Using when simple string args suffice (over-engineering)
- Not understanding it runs at the same lifecycle point as CommandLineRunner
- Forgetting that non-option args are separate from option args

**6. Follow-up questions:**
- When would you choose ApplicationRunner over CommandLineRunner?
- Can you have both in the same application?

**7. Senior summary:**
"ApplicationRunner provides parsed command-line arguments with option/non-option separation. I use it when the app accepts `--key=value` style arguments. For simple args, CommandLineRunner is sufficient. Both run at the same lifecycle point."

--------------------------------------------------
### Question 17: What is Actuator?

**1. Definition:**
Spring Boot Actuator provides production-ready features: health checks, metrics, environment info, HTTP tracing, and management endpoints for monitoring and managing applications.

**2. Why it matters:**
Production observability is non-negotiable. Actuator provides standardized endpoints that integrate with monitoring tools (Prometheus, Grafana), orchestrators (Kubernetes), and APM solutions.

**3. Internal working:**
Key endpoints:
| Endpoint | Purpose |
|----------|---------|
| `/health` | Liveness/readiness probes |
| `/metrics` | Micrometer metrics |
| `/info` | Build info, git commit |
| `/env` | Environment properties |
| `/loggers` | Dynamic log level changes |
| `/threaddump` | Thread analysis |

Secured by default in Spring Boot 2.x+. Exposing:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: when_authorized
```

**4. Real-world example:**
Kubernetes integration:
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
```
Custom health indicator for database:
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        return database.isAvailable()
            ? Health.up().build()
            : Health.down().withDetail("error", "DB unreachable").build();
    }
}
```

**5. Pitfalls:**
- Exposing sensitive endpoints (env, heapdump) without authentication
- Not separating management port from application port
- Heavy health checks affecting application performance

**6. Follow-up questions:**
- How do you secure actuator endpoints?
- How would you add custom metrics to Micrometer?

**7. Senior summary:**
"Actuator provides production observability: health probes, metrics, dynamic log levels. I always configure it with security, separate management port, and integrate with Prometheus/Grafana. Health endpoints are critical for Kubernetes liveness/readiness probes."

--------------------------------------------------
### Question 18: How to enable Actuator endpoints?

**1. Definition:**
Add `spring-boot-starter-actuator` dependency and configure exposure via `management.endpoints.web.exposure.include` property.

**2. Why it matters:**
Endpoints are disabled by default for security. Explicitly enabling only necessary endpoints follows the principle of least privilege.

**3. Internal working:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
        exclude: env,heapdump
      base-path: /actuator  # default
  server:
    port: 8081  # Separate management port
  endpoint:
    health:
      show-details: when_authorized
      probes:
        enabled: true  # Kubernetes probes
```

Security configuration:
```java
@Bean
public SecurityFilterChain actuatorSecurity(HttpSecurity http) {
    http.securityMatcher("/actuator/**")
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health/**").permitAll()
            .requestMatchers("/actuator/**").hasRole("ADMIN"));
    return http.build();
}
```

**4. Real-world example:**
Production configuration for a payment service:
- Health: Public (for load balancer)
- Metrics: Internal network only (Prometheus scraping)
- Env/heapdump: Admin role only, separate management port
- Loggers: Enabled for dynamic debug level changes

**5. Pitfalls:**
- Using `include: *` in production (security risk)
- Not separating management port in sensitive environments
- Forgetting that `/shutdown` endpoint can kill your app

**6. Follow-up questions:**
- How do you secure the `/env` endpoint which shows properties?
- How would you enable the shutdown endpoint safely?

**7. Senior summary:**
"Add actuator starter, explicitly enable needed endpoints via `management.endpoints.web.exposure.include`. Use separate management port in production, secure sensitive endpoints. Only `/health` should be public; everything else requires authentication."

--------------------------------------------------
### Question 19: Difference between @Component and @Bean?

**1. Definition:**
`@Component` is a class-level annotation for auto-detection via component scanning. `@Bean` is a method-level annotation in `@Configuration` classes for explicit bean definition.

**2. Why it matters:**
Choosing the right approach affects code organization, testability, and third-party library integration. Understanding both is essential for proper Spring development.

**3. Internal working:**

| Aspect | @Component | @Bean |
|--------|------------|-------|
| Level | Class | Method |
| Detection | Component scanning | Explicit declaration |
| Third-party | Cannot annotate | Can wrap any class |
| Customization | Limited | Full control |
| Conditional | @Conditional on class | @Conditional on method |

```java
// @Component - class you own
@Component
public class EmailService { }

// @Bean - third-party or complex instantiation
@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .build();
    }
}
```

**4. Real-world example:**
A payment gateway integration:
```java
// Can't add @Component to Stripe SDK classes
@Configuration
public class PaymentConfig {
    @Bean
    public StripeClient stripeClient(@Value("${stripe.api-key}") String apiKey) {
        return new StripeClient(apiKey);  // Third-party class
    }
}
```

**5. Pitfalls:**
- Using @Component for everything, losing explicit configuration benefits
- Forgetting that @Bean methods are proxied (don't call directly from same class)
- Not understanding @Bean method interception requires @Configuration (not @Component)

**6. Follow-up questions:**
- What's the difference between @Configuration and @Component with @Bean methods?
- How does CGLIB proxying affect @Bean method calls?

**7. Senior summary:**
"@Component for your own classes discovered via scanning, @Bean for third-party classes or complex instantiation logic. @Bean in @Configuration classes enables proxied method calls for singleton semantics. I use @Bean when I need explicit control over bean creation."

--------------------------------------------------
### Question 20: What is Dependency Injection?

**1. Definition:**
Dependency Injection (DI) is a design pattern where objects receive their dependencies from external sources rather than creating them, enabling loose coupling and testability.

**2. Why it matters:**
DI is foundational to testable, maintainable code. It enables mocking in tests, swapping implementations without code changes, and following SOLID principles.

**3. Internal working:**
Spring DI mechanisms:
1. **Constructor injection** (preferred): Dependencies as constructor params
2. **Setter injection**: Via setter methods
3. **Field injection**: Via `@Autowired` on fields (discouraged)

Spring's IoC container:
1. Creates beans based on configuration
2. Resolves dependencies via type matching
3. Handles circular dependencies (for setter/field injection)
4. Manages bean lifecycle

```java
// Constructor injection - preferred
@Service
public class OrderService {
    private final PaymentService paymentService;
    private final InventoryService inventoryService;

    public OrderService(PaymentService payment, InventoryService inventory) {
        this.paymentService = payment;
        this.inventoryService = inventory;
    }
}
```

**4. Real-world example:**
Testing with DI:
```java
@Test
void shouldProcessOrder() {
    // Easy mocking because of DI
    PaymentService mockPayment = mock(PaymentService.class);
    InventoryService mockInventory = mock(InventoryService.class);

    OrderService service = new OrderService(mockPayment, mockInventory);
    // Test without real payment processing
}
```

**5. Pitfalls:**
- Field injection making classes untestable without reflection
- Circular dependencies indicating design problems
- Over-injection (too many dependencies = class doing too much)

**6. Follow-up questions:**
- Why is constructor injection preferred over field injection?
- How does Spring handle circular dependencies?

**7. Senior summary:**
""DI inverts control—objects receive dependencies rather than creating them. This enables testability, loose coupling, and configuration flexibility. I always use constructor injection for mandatory dependencies as it makes dependencies explicit and enables easy testing without Spring context."

---

## INTERMEDIATE QUESTIONS (21-40) - Most rejections happen here

--------------------------------------------------
### Question 21: What is auto-configuration internally?

**1. Definition:**
Internally, auto-configuration is a mechanism where Spring Boot loads configuration classes from `META-INF/spring.factories` (or `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` in 3.x) and conditionally creates beans based on classpath, properties, and existing beans.

**2. Why it matters:**
Understanding the internals helps debug "why is this bean created/not created" issues—a common production debugging scenario.

**3. Internal working:**
1. `@EnableAutoConfiguration` imports `AutoConfigurationImportSelector`
2. Selector reads auto-configuration class names from:
   - Spring Boot 2.x: `META-INF/spring.factories`
   - Spring Boot 3.x: `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
3. Classes filtered by `@Conditional*` annotations
4. Remaining classes processed by `ConfigurationClassParser`
5. `@AutoConfigureOrder`, `@AutoConfigureBefore`, `@AutoConfigureAfter` control ordering

Debug with:
```bash
java -jar app.jar --debug
# Or
logging.level.org.springframework.boot.autoconfigure=DEBUG
```

**4. Real-world example:**
Debugging why HikariCP wasn't being configured:
```
# Check conditions report
/actuator/conditions

# Found: DataSourceAutoConfiguration
#   - @ConditionalOnClass found HikariDataSource
#   - @ConditionalOnMissingBean - MATCHED (no existing DataSource)
#   BUT spring.datasource.url was missing!
```
Fix: Added `spring.datasource.url` property.

**5. Pitfalls:**
- Not checking the conditions report when beans aren't created
- Creating a bean that shadows auto-configured one unintentionally
- Not understanding the order dependencies between auto-configurations

**6. Follow-up questions:**
- How would you create your own auto-configuration?
- What's the purpose of @AutoConfigureAfter?

**7. Senior summary:**
"Auto-configuration loads classes from spring.factories/imports files, filters them through @Conditional annotations, and creates beans that don't conflict with user-defined ones. I use `--debug` flag and `/actuator/conditions` to diagnose configuration issues."

--------------------------------------------------
### Question 22: How does Spring Boot decide which beans to create?

**1. Definition:**
Spring Boot uses `@Conditional*` annotations to evaluate conditions at configuration time, creating beans only when all conditions are met.

**2. Why it matters:**
This is the core mechanism enabling "just add dependency and it works." Understanding conditions explains why a bean exists or doesn't exist.

**3. Internal working:**
Condition evaluation hierarchy:
```java
@ConditionalOnClass(DataSource.class)      // Class on classpath?
@ConditionalOnProperty(name="spring.datasource.url")  // Property set?
@ConditionalOnMissingBean(DataSource.class)  // User didn't define one?
public DataSource dataSource() { ... }
```

Key conditionals:
| Annotation | Checks |
|------------|--------|
| `@ConditionalOnClass` | Class exists on classpath |
| `@ConditionalOnMissingClass` | Class doesn't exist |
| `@ConditionalOnBean` | Bean exists in context |
| `@ConditionalOnMissingBean` | Bean doesn't exist |
| `@ConditionalOnProperty` | Property has specific value |
| `@ConditionalOnResource` | Resource exists |
| `@ConditionalOnWebApplication` | Is a web app |

**4. Real-world example:**
Custom conditional for feature flags:
```java
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnFeatureCondition.class)
public @interface ConditionalOnFeature {
    String value();
}

public class OnFeatureCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String feature = metadata.getAnnotationAttributes(
            ConditionalOnFeature.class.getName()).get("value").toString();
        return context.getEnvironment()
            .getProperty("features." + feature, Boolean.class, false);
    }
}
```

**5. Pitfalls:**
- Conditions evaluated at configuration time, not runtime
- Order of condition evaluation matters
- `@ConditionalOnMissingBean` checked after user configs processed

**6. Follow-up questions:**
- How would you create a custom condition?
- What's the difference between @Conditional and @Profile?

**7. Senior summary:**
"Spring Boot evaluates @Conditional annotations during configuration processing. The 'back-off' pattern uses @ConditionalOnMissingBean to let user beans take precedence. Conditions are evaluated once at startup—not dynamically. Debug with conditions actuator endpoint."

--------------------------------------------------
### Question 23: What is spring.factories?

**1. Definition:**
`spring.factories` is a properties file in `META-INF/` that maps interface types to implementation classes, enabling service discovery for auto-configuration, listeners, and other extension points.

**2. Why it matters:**
It's the extension mechanism for Spring Boot. Custom starters, auto-configurations, and framework extensions all use this file.

**3. Internal working:**
Location: `META-INF/spring.factories`
```properties
# Auto-configuration
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.example.MyAutoConfiguration,\
  com.example.AnotherAutoConfiguration

# Application listeners
org.springframework.context.ApplicationListener=\
  com.example.MyListener

# Failure analyzers
org.springframework.boot.diagnostics.FailureAnalyzer=\
  com.example.MyFailureAnalyzer
```

**Spring Boot 3.x change:**
Auto-configurations moved to:
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
```
com.example.MyAutoConfiguration
com.example.AnotherAutoConfiguration
```

**4. Real-world example:**
Creating a company-wide observability starter:
```properties
# META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.company.observability.TracingAutoConfiguration,\
  com.company.observability.MetricsAutoConfiguration
```
Now any project adding this dependency gets tracing and metrics configured automatically.

**5. Pitfalls:**
- Format is strict—no spaces around `=` or after `\`
- Spring Boot 3.x migration requires moving to .imports file
- Multiple JARs can contribute to same key (they merge)

**6. Follow-up questions:**
- How do you migrate spring.factories to Spring Boot 3.x?
- What other extension points use spring.factories?

**7. Senior summary:**
"spring.factories enables service discovery for auto-configuration and extensions. It maps interface types to implementations loaded at startup. In Spring Boot 3.x, auto-configurations moved to dedicated .imports files. I use this for creating reusable starters."

--------------------------------------------------
### Question 24: What is @EnableAutoConfiguration?

**1. Definition:**
`@EnableAutoConfiguration` triggers Spring Boot's auto-configuration mechanism by importing `AutoConfigurationImportSelector`, which loads and applies auto-configuration classes.

**2. Why it matters:**
It's the switch that enables the "magic" of Spring Boot. Understanding it helps when you need to disable or customize auto-configuration behavior.

**3. Internal working:**
```java
@Target(ElementType.TYPE)
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
    String[] exclude() default {};
    String[] excludeName() default {};
}
```

Processing flow:
1. `@Import` triggers `AutoConfigurationImportSelector`
2. Selector calls `getCandidateConfigurations()`
3. Loads classes from spring.factories/imports
4. Filters through exclusions and conditions
5. Orders configurations
6. Returns list for Spring to process

**4. Real-world example:**
Disabling specific auto-configurations:
```java
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    SecurityAutoConfiguration.class
})
public class Application { }
```
Or via properties:
```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

**5. Pitfalls:**
- Excluding too much, breaking dependent auto-configurations
- Not realizing @SpringBootApplication includes this
- Excluding by class name that might change between versions

**6. Follow-up questions:**
- How do you find out which auto-configurations are being applied?
- What's the difference between exclude and @ConditionalOnMissingBean?

**7. Senior summary:**
"@EnableAutoConfiguration imports AutoConfigurationImportSelector which loads configurations from spring.factories. It's included in @SpringBootApplication. Use exclude attribute or spring.autoconfigure.exclude property to disable unwanted configurations."

--------------------------------------------------
### Question 25: What is @ConfigurationProperties?

**1. Definition:**
`@ConfigurationProperties` binds external configuration (properties/YAML) to a type-safe Java object, enabling structured configuration with validation and IDE support.

**2. Why it matters:**
Type-safe configuration prevents runtime errors from typos. It enables validation at startup, IDE auto-completion, and documentation generation.

**3. Internal working:**
```java
@ConfigurationProperties(prefix = "payment.gateway")
@Validated
public class PaymentProperties {
    @NotBlank
    private String apiKey;

    @Min(1000) @Max(30000)
    private int timeout = 5000;

    private Retry retry = new Retry();

    public static class Retry {
        private int maxAttempts = 3;
        private Duration backoff = Duration.ofSeconds(1);
    }
    // getters and setters
}
```

```yaml
payment:
  gateway:
    api-key: ${PAYMENT_API_KEY}
    timeout: 10000
    retry:
      max-attempts: 5
      backoff: 2s
```

Enable with:
- `@EnableConfigurationProperties(PaymentProperties.class)` or
- `@ConfigurationPropertiesScan`

**4. Real-world example:**
A rate limiting configuration:
```java
@ConfigurationProperties(prefix = "ratelimit")
public record RateLimitProperties(
    boolean enabled,
    int requestsPerMinute,
    Map<String, Integer> endpointLimits
) {}
```
Benefits: Immutable (record), validated, documented, IDE support.

**5. Pitfalls:**
- Forgetting to enable via @EnableConfigurationProperties
- Using @Value for complex nested config (hard to maintain)
- Not adding spring-boot-configuration-processor for IDE support

**6. Follow-up questions:**
- How does relaxed binding work?
- How do you generate configuration metadata for IDE support?

**7. Senior summary:**
"@ConfigurationProperties provides type-safe, validated configuration binding with IDE support. I use it for any non-trivial configuration instead of @Value. Add spring-boot-configuration-processor for metadata generation. Records make properties immutable."

--------------------------------------------------
### Question 26: Difference between @Value and @ConfigurationProperties?

**1. Definition:**
`@Value` injects individual properties into fields. `@ConfigurationProperties` binds a group of properties to a structured object.

**2. Why it matters:**
Choosing the right approach affects maintainability. @Value is fine for simple cases; complex configuration demands @ConfigurationProperties.

**3. Internal working:**

| Aspect | @Value | @ConfigurationProperties |
|--------|--------|-------------------------|
| Granularity | Single value | Grouped properties |
| Type safety | SpEL parsing | Compile-time binding |
| Validation | Manual | @Validated integration |
| Relaxed binding | No | Yes (kebab-case, etc.) |
| IDE support | Limited | Full with processor |
| Meta-data | None | Generated |

```java
// @Value - simple, single values
@Value("${app.name}")
private String appName;

@Value("${app.timeout:5000}")
private int timeout;

// @ConfigurationProperties - structured
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String name;
    private int timeout;
    private Security security;
    // Hierarchical, validated, documented
}
```

**4. Real-world example:**
A service with multiple integrations:
```java
// Bad - scattered @Value across codebase
@Value("${stripe.api-key}") private String stripeKey;
@Value("${stripe.timeout}") private int stripeTimeout;
@Value("${stripe.retry}") private int stripeRetry;

// Good - centralized configuration
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {
    private String apiKey;
    private Duration timeout;
    private int retry;
    // All Stripe config in one place
}
```

**5. Pitfalls:**
- Using @Value for more than 2-3 related properties
- Missing properties with @Value fails silently with defaults
- SpEL expressions in @Value can be complex and error-prone

**6. Follow-up questions:**
- Can you use SpEL expressions in @ConfigurationProperties?
- How does relaxed binding work (app-name vs appName)?

**7. Senior summary:**
"@Value for simple, single values; @ConfigurationProperties for structured configuration. I use @ConfigurationProperties for anything beyond basic settings—it provides validation, relaxed binding, IDE support, and keeps related configuration together."

--------------------------------------------------
### Question 27: How profiles work in Spring Boot?

**1. Definition:**
Profiles are named logical groups of configuration that can be activated to customize application behavior for different environments (dev, staging, prod).

**2. Why it matters:**
Same artifact, different environments. Profiles enable environment-specific configuration without code changes or rebuilding.

**3. Internal working:**
Profile-specific files:
- `application-dev.yml`
- `application-prod.yml`
- `application-{profile}.yml`

Loading order (later overrides earlier):
1. `application.yml` (base)
2. `application-{active-profile}.yml`
3. External configuration

Profile-specific beans:
```java
@Configuration
@Profile("dev")
public class DevConfig {
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder().build();
    }
}

@Configuration
@Profile("prod")
public class ProdConfig {
    @Bean
    public DataSource dataSource() {
        return createProductionPool();
    }
}
```

**4. Real-world example:**
Multi-environment setup:
```yaml
# application.yml (base)
spring:
  application:
    name: payment-service

# application-dev.yml
spring:
  datasource:
    url: jdbc:h2:mem:dev
logging:
  level:
    root: DEBUG

# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}/payments
logging:
  level:
    root: WARN
```

**5. Pitfalls:**
- Hardcoding profile in code instead of externalizing
- Too many profile-specific differences (indicates deployment issues)
- Sensitive data in profile files committed to source control

**6. Follow-up questions:**
- How do you activate multiple profiles simultaneously?
- What's the difference between @Profile and @ConditionalOnProperty?

**7. Senior summary:**
"Profiles group configuration by environment. I use base application.yml for common settings, profile-specific files for overrides. Activate via spring.profiles.active environment variable in production. Keep profile differences minimal—mostly connection strings and log levels."

--------------------------------------------------
### Question 28: How to activate profiles?

**1. Definition:**
Profiles are activated via `spring.profiles.active` property, which can be set through environment variables, command-line arguments, properties files, or programmatically.

**2. Why it matters:**
Profile activation strategy affects deployment flexibility. Externalizing profile selection enables the same artifact to run anywhere.

**3. Internal working:**
Activation methods (priority order):
```bash
# 1. Command line argument
java -jar app.jar --spring.profiles.active=prod

# 2. JVM system property
java -Dspring.profiles.active=prod -jar app.jar

# 3. Environment variable
SPRING_PROFILES_ACTIVE=prod java -jar app.jar

# 4. In application.properties (not recommended for prod profile)
spring.profiles.active=dev

# 5. Programmatically
SpringApplication app = new SpringApplication(MyApp.class);
app.setAdditionalProfiles("prod");
app.run(args);
```

Multiple profiles:
```bash
SPRING_PROFILES_ACTIVE=prod,metrics,feature-x
```

Profile groups (Spring Boot 2.4+):
```yaml
spring:
  profiles:
    group:
      prod: [prod-db, prod-security, prod-logging]
```

**4. Real-world example:**
Kubernetes deployment:
```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
        - name: payment-service
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod,kubernetes"
            - name: DB_HOST
              valueFrom:
                configMapKeyRef:
                  name: db-config
                  key: host
```

**5. Pitfalls:**
- Setting profile in application.properties defeats the purpose
- Forgetting that profile order matters for property conflicts
- Using profiles for feature flags (use @ConditionalOnProperty instead)

**6. Follow-up questions:**
- What happens when multiple active profiles define the same property?
- How do profile groups work?

**7. Senior summary:**
"Activate via SPRING_PROFILES_ACTIVE environment variable in production—never hardcode in properties. Multiple profiles can be active simultaneously, with later profiles overriding earlier ones. I use profile groups to bundle related configurations."

--------------------------------------------------
### Question 29: What is @Profile?

**1. Definition:**
`@Profile` is an annotation that marks a component, configuration, or bean to only be registered when specific profile(s) are active.

**2. Why it matters:**
It enables environment-specific beans without runtime conditionals. Different implementations can be swapped based on environment.

**3. Internal working:**
```java
@Service
@Profile("dev")
public class MockPaymentService implements PaymentService {
    // Returns success without calling real gateway
}

@Service
@Profile("prod")
public class RealPaymentService implements PaymentService {
    // Calls actual payment gateway
}
```

Expression support:
```java
@Profile("!prod")           // NOT prod
@Profile({"dev", "test"})   // dev OR test
@Profile("prod & feature-x") // prod AND feature-x (Spring 5.1+)
```

**4. Real-world example:**
A notification service with environment-specific implementations:
```java
@Service
@Profile("prod")
public class SmsNotificationService implements NotificationService {
    @Autowired
    private TwilioClient twilio;
    // Real SMS sending
}

@Service
@Profile("!prod")
public class LoggingNotificationService implements NotificationService {
    public void send(Notification n) {
        log.info("Would send: {}", n);  // Log instead of send
    }
}
```

**5. Pitfalls:**
- Creating too many profile-specific beans (consider strategy pattern)
- Using @Profile when @ConditionalOnProperty is more appropriate
- Forgetting that missing profile means bean never registers

**6. Follow-up questions:**
- Can you use NOT logic with @Profile?
- When would you prefer @ConditionalOnProperty over @Profile?

**7. Senior summary:**
"@Profile conditionally registers beans based on active profiles. I use it for environment-specific implementations like mock services in dev. For feature flags or fine-grained control, I prefer @ConditionalOnProperty as it's more flexible."

--------------------------------------------------
### Question 30: How to override auto-configured beans?

**1. Definition:**
Define your own bean of the same type—Spring Boot's auto-configuration uses `@ConditionalOnMissingBean` and will back off when it finds your bean.

**2. Why it matters:**
Production often requires customization beyond defaults. Understanding the override mechanism enables fine-tuning without disabling entire auto-configurations.

**3. Internal working:**
Auto-configuration pattern:
```java
@Configuration
@ConditionalOnClass(DataSource.class)
public class DataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean  // Backs off if user defines one
    public DataSource dataSource(DataSourceProperties properties) {
        return createDefaultDataSource(properties);
    }
}
```

Your override:
```java
@Configuration
public class CustomDataSourceConfig {

    @Bean  // No @ConditionalOnMissingBean - always created
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(50);
        config.setConnectionTimeout(5000);
        // Custom configuration
        return new HikariDataSource(config);
    }
}
```

**4. Real-world example:**
Custom RestTemplate with timeouts and interceptors:
```java
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(30))
            .additionalInterceptors(new LoggingInterceptor())
            .errorHandler(new CustomErrorHandler())
            .build();
    }
}
```
This overrides any auto-configured RestTemplate.

**5. Pitfalls:**
- Partially overriding—some auto-config beans may still be created
- Not checking what the auto-configuration provides before overriding
- Breaking other auto-configurations that depend on the default bean

**6. Follow-up questions:**
- What if you only want to customize, not replace, an auto-configured bean?
- How does @Primary work with auto-configuration?

**7. Senior summary:**
"Define your own bean of the same type—auto-configuration backs off via @ConditionalOnMissingBean. For customization without full replacement, use customizer callbacks like WebMvcConfigurer or RestTemplateCustomizer. Always understand what you're overriding."

--------------------------------------------------
### Question 31: What is externalized configuration?

**1. Definition:**
Externalized configuration is the practice of keeping configuration outside the application code, allowing the same build to run in different environments without modification.

**2. Why it matters:**
It's a 12-factor app principle. Same artifact in dev, staging, and prod eliminates "works in dev, fails in prod" issues.

**3. Internal working:**
Property source hierarchy (highest to lowest priority):
1. Command-line arguments (`--server.port=9090`)
2. JNDI attributes
3. Java System properties (`-Dserver.port=9090`)
4. OS environment variables (`SERVER_PORT=9090`)
5. `application-{profile}.properties` outside JAR
6. `application-{profile}.properties` inside JAR
7. `application.properties` outside JAR
8. `application.properties` inside JAR
9. `@PropertySource` annotations
10. Default properties

External locations:
- `./config/` subdirectory
- Current directory
- `classpath:/config/`
- `classpath:/`

**4. Real-world example:**
Kubernetes ConfigMap and Secrets:
```yaml
# ConfigMap for non-sensitive config
spring:
  datasource:
    url: jdbc:postgresql://db:5432/app

---
# Secret mounted as environment variables
DB_USERNAME: (from Secret)
DB_PASSWORD: (from Secret)
```

Application using both:
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

**5. Pitfalls:**
- Not understanding property source precedence
- Secrets in properties files committed to git
- Overcomplicating with too many property sources

**6. Follow-up questions:**
- How do you reference secrets securely in configuration?
- What's the config directory search order?

**7. Senior summary:**
"Externalized configuration separates config from code. Spring Boot's property source hierarchy allows overrides at every level—from defaults to command-line. I use environment variables for secrets and config files for non-sensitive settings."

--------------------------------------------------
### Question 32: How environment variables work in Spring Boot?

**1. Definition:**
Environment variables are automatically mapped to Spring properties using relaxed binding—`MY_APP_TIMEOUT` becomes `my.app.timeout` or `my-app-timeout`.

**2. Why it matters:**
Environment variables are the standard configuration mechanism in containerized environments. Understanding the mapping enables proper property naming.

**3. Internal working:**
Relaxed binding rules:
```
Environment Variable    →  Property Name
---------------------------------------------
SERVER_PORT            →  server.port
SPRING_DATASOURCE_URL  →  spring.datasource.url
MY_APP_RETRY_COUNT     →  my.app.retry-count
                          my.app.retryCount
                          my-app-retry-count
```

Conventions:
- Replace `.` with `_`
- Replace `-` with `_`
- Use UPPERCASE
- List/array items: `MY_PROP_0=value1`, `MY_PROP_1=value2`

```yaml
# All equivalent in application.yml
my:
  app:
    retry-count: 3      # Kebab case
    retryCount: 3       # Camel case
    retry_count: 3      # Underscore
```

**4. Real-world example:**
Docker Compose with environment variables:
```yaml
services:
  payment-service:
    image: payment-service:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/payments
      - SPRING_DATASOURCE_USERNAME=${DB_USER}
      - SPRING_DATASOURCE_PASSWORD=${DB_PASS}
      - PAYMENT_GATEWAY_API_KEY=${STRIPE_KEY}
      - PAYMENT_GATEWAY_TIMEOUT=10000
```

**5. Pitfalls:**
- Case sensitivity on Linux vs. Windows
- Forgetting the underscore replacement for dots
- Environment variables with special characters not working

**6. Follow-up questions:**
- How do you bind environment variables to list properties?
- What's the precedence between env vars and property files?

**7. Senior summary:**
"Environment variables map to properties via relaxed binding: uppercase, underscores replacing dots. `DATABASE_URL` becomes `database.url`. They take precedence over property files but not command-line arguments. Essential for container-based deployments."

--------------------------------------------------
### Question 33: What is @ConditionalOnProperty?

**1. Definition:**
`@ConditionalOnProperty` is a condition that registers a bean only if a specific property has a specific value (or exists/doesn't exist).

**2. Why it matters:**
It enables feature flags, optional features, and configuration-driven architecture without code changes.

**3. Internal working:**
```java
@Bean
@ConditionalOnProperty(
    name = "feature.new-checkout",
    havingValue = "true",
    matchIfMissing = false
)
public CheckoutService newCheckoutService() {
    return new NewCheckoutService();
}

@Bean
@ConditionalOnProperty(
    name = "feature.new-checkout",
    havingValue = "false",
    matchIfMissing = true  // Default when property missing
)
public CheckoutService legacyCheckoutService() {
    return new LegacyCheckoutService();
}
```

Attributes:
- `name`/`value`: Property name(s)
- `havingValue`: Expected value (default: not "false")
- `matchIfMissing`: Match if property undefined
- `prefix`: Property prefix

**4. Real-world example:**
Feature toggling in production:
```java
@Configuration
public class FeatureConfig {

    @Bean
    @ConditionalOnProperty(
        prefix = "features",
        name = "async-processing",
        havingValue = "true"
    )
    public OrderProcessor asyncProcessor() {
        return new AsyncOrderProcessor();
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "features",
        name = "async-processing",
        havingValue = "false",
        matchIfMissing = true
    )
    public OrderProcessor syncProcessor() {
        return new SyncOrderProcessor();
    }
}
```
Flip features without redeployment: `features.async-processing=true`

**5. Pitfalls:**
- Forgetting `matchIfMissing` logic (defaults to false)
- Using for complex boolean expressions (consider custom Condition)
- Property typos silently falling to default

**6. Follow-up questions:**
- How do you implement AND/OR logic with multiple properties?
- What's the difference between @ConditionalOnProperty and @Profile?

**7. Senior summary:**
"@ConditionalOnProperty enables property-driven bean registration for feature flags and optional components. I use it over @Profile for fine-grained control and runtime flexibility. Set matchIfMissing carefully—it determines default behavior."

--------------------------------------------------
### Question 34: What is @ConditionalOnClass?

**1. Definition:**
`@ConditionalOnClass` registers a bean only if specified class(es) are present on the classpath at configuration time.

**2. Why it matters:**
It enables optional integrations—if a library is present, configure it automatically. This is the foundation of starter dependencies.

**3. Internal working:**
```java
@Configuration
@ConditionalOnClass(RedisTemplate.class)  // Only if Redis client on classpath
public class RedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        return template;
    }
}
```

Works with:
- `value`: Class array (must be loadable)
- `name`: Class names as strings (for optional dependencies)

**4. Real-world example:**
Supporting multiple cache providers:
```java
@Configuration
public class CacheAutoConfiguration {

    @Configuration
    @ConditionalOnClass(RedisCache.class)
    static class RedisCacheConfig {
        @Bean
        public CacheManager cacheManager(RedisConnectionFactory cf) {
            return RedisCacheManager.create(cf);
        }
    }

    @Configuration
    @ConditionalOnClass(CaffeineCacheManager.class)
    @ConditionalOnMissingBean(CacheManager.class)
    static class CaffeineCacheConfig {
        @Bean
        public CacheManager cacheManager() {
            return new CaffeineCacheManager();
        }
    }
}
```
Add redis dependency → Redis cache auto-configured.

**5. Pitfalls:**
- Using `value` for classes from optional dependencies (use `name` instead)
- Not combining with @ConditionalOnMissingBean for proper back-off
- Class loading issues with modular applications

**6. Follow-up questions:**
- Why use `name` attribute vs `value` attribute?
- How do you handle optional dependencies that may not be on classpath?

**7. Senior summary:**
"@ConditionalOnClass enables classpath-driven configuration—if library present, configure it. Combined with @ConditionalOnMissingBean, it's the pattern for auto-configuration. Use `name` attribute for optional dependencies to avoid ClassNotFoundException."

--------------------------------------------------
### Question 35: What is @ConditionalOnMissingBean?

**1. Definition:**
`@ConditionalOnMissingBean` registers a bean only if no bean of the specified type is already defined, enabling the "back-off" pattern for auto-configuration.

**2. Why it matters:**
This is what makes auto-configuration customizable. User beans always win—auto-configuration provides defaults only when needed.

**3. Internal working:**
```java
@Configuration
public class JacksonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean  // User's ObjectMapper takes precedence
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.build();  // Default configuration
    }
}

// User's configuration - no @ConditionalOnMissingBean
@Configuration
public class CustomJacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
```

Attributes:
- `value`/`type`: Bean class(es)
- `name`: Bean name(s)
- `annotation`: Beans with specific annotation
- `search`: SearchStrategy for bean lookup scope

**4. Real-world example:**
Custom transaction manager overriding default:
```java
// Auto-configuration provides default
@Bean
@ConditionalOnMissingBean(PlatformTransactionManager.class)
public PlatformTransactionManager transactionManager(DataSource ds) {
    return new DataSourceTransactionManager(ds);
}

// Your custom transaction manager
@Bean
@Primary  // Good practice when overriding
public PlatformTransactionManager transactionManager(
        EntityManagerFactory emf) {
    JpaTransactionManager tm = new JpaTransactionManager(emf);
    tm.setDefaultTimeout(30);
    return tm;
}
```

**5. Pitfalls:**
- Condition checked after user configs but before other auto-configs
- Bean name matching is exact (case-sensitive)
- @Primary doesn't affect @ConditionalOnMissingBean evaluation

**6. Follow-up questions:**
- When is the condition evaluated relative to user beans?
- How does SearchStrategy affect bean lookup?

**7. Senior summary:**
"@ConditionalOnMissingBean implements the back-off pattern—auto-configuration yields to user-defined beans. It's evaluated after @Configuration classes but before other auto-configurations. Define your bean without this annotation to override defaults."

--------------------------------------------------
### Question 36: How to disable auto-configuration?

**1. Definition:**
Disable via `@SpringBootApplication(exclude=...)`, `spring.autoconfigure.exclude` property, or by defining your own beans that trigger `@ConditionalOnMissingBean`.

**2. Why it matters:**
Not all auto-configurations are needed. Disabling unnecessary ones reduces startup time, memory footprint, and potential conflicts.

**3. Internal working:**
```java
// Method 1: Exclude in annotation
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class Application { }

// Method 2: Exclude by name (for optional dependencies)
@SpringBootApplication(excludeName = {
    "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
```

```yaml
# Method 3: Properties file
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
```

**4. Real-world example:**
API gateway that doesn't need database:
```java
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    FlywayAutoConfiguration.class,
    LiquibaseAutoConfiguration.class
})
public class GatewayApplication { }
```

Check what's being configured:
```bash
java -jar app.jar --debug | grep -i "autoconfiguration"
```

**5. Pitfalls:**
- Excluding a configuration that others depend on
- Not knowing which auto-configuration enables what
- Using excludeName for classes that might be renamed

**6. Follow-up questions:**
- How do you find out which auto-configurations are active?
- What's the impact of excluding SecurityAutoConfiguration?

**7. Senior summary:**
"Disable via exclude attribute in @SpringBootApplication or spring.autoconfigure.exclude property. Use --debug flag to see what's being configured. Be careful about dependencies between auto-configurations—excluding one might break another."

--------------------------------------------------
### Question 37: What is exclude in @SpringBootApplication?

**1. Definition:**
The `exclude` attribute in `@SpringBootApplication` disables specific auto-configuration classes, preventing them from being applied.

**2. Why it matters:**
Explicit exclusion is clearer than relying on conditions. It documents intent and prevents auto-configuration from being applied accidentally.

**3. Internal working:**
```java
@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class
    },
    excludeName = {
        "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration"
    }
)
public class Application { }
```

`exclude` vs `excludeName`:
- `exclude`: Type-safe, compile-time checked
- `excludeName`: String-based, for optional/conditional dependencies

Internally, exclusions are processed by `AutoConfigurationImportSelector` before conditions are evaluated.

**4. Real-world example:**
Microservice with custom security:
```java
@SpringBootApplication(exclude = {
    // Disable default security, we use custom OAuth2
    SecurityAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class,

    // Disable default error handling
    ErrorMvcAutoConfiguration.class
})
public class PaymentService { }
```

**5. Pitfalls:**
- Using exclude for classes not on classpath throws exception
- Forgetting dependent auto-configurations still run
- Over-excluding, then wondering why things don't work

**6. Follow-up questions:**
- When would you use excludeName instead of exclude?
- How do you find all available auto-configuration classes?

**7. Senior summary:**
"exclude in @SpringBootApplication prevents specific auto-configurations from running. Use type-safe exclude for required dependencies, excludeName for optional ones. It's more explicit than relying on conditions to disable features."

--------------------------------------------------
### Question 38: What is DevTools?

**1. Definition:**
Spring Boot DevTools is a module that provides development-time features like automatic restart, live reload, and sensible development defaults.

**2. Why it matters:**
Developer productivity directly impacts delivery speed. Hot reload and automatic restart eliminate the manual restart cycle, saving significant development time.

**3. Internal working:**
Key features:
1. **Automatic restart**: Monitors classpath, restarts on change
2. **LiveReload**: Browser refresh on resource changes
3. **Property defaults**: Disables caching, enables debug features
4. **Remote debugging**: Development on remote servers

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

Restart vs. Reload:
- Base classloader: Third-party JARs (not reloaded)
- Restart classloader: Your code (reloaded on change)

**4. Real-world example:**
Development workflow:
```yaml
# application-dev.yml
spring:
  devtools:
    restart:
      enabled: true
      additional-paths: src/main/resources
      exclude: static/**,templates/**
    livereload:
      enabled: true
```
Change Java code → auto-restart (~2s)
Change HTML/CSS → browser auto-refresh

**5. Pitfalls:**
- DevTools in production (it's auto-disabled in JARs, but verify)
- Slow restarts due to large classpath (use excludes)
- Conflicts with other hot-reload tools (JRebel)

**6. Follow-up questions:**
- How is DevTools automatically disabled in production?
- What's the difference between restart and hot-swap?

**7. Senior summary:**
"DevTools accelerates development with auto-restart, live reload, and dev-friendly defaults. It uses two classloaders to restart only your code, not dependencies. Automatically disabled in production JARs. Essential for modern Spring Boot development."

--------------------------------------------------
### Question 39: Hot reload vs Restart

**1. Definition:**
Hot reload (hot swap) replaces individual classes in a running JVM. Restart loads a new classloader with all application classes. Hot reload is faster but limited; restart is slower but handles all changes.

**2. Why it matters:**
Understanding the difference helps set realistic expectations. Hot reload fails silently for structural changes, causing confusion.

**3. Internal working:**

| Aspect | Hot Reload (JVM HotSwap) | DevTools Restart |
|--------|--------------------------|------------------|
| Speed | ~100ms | ~2-5s |
| What works | Method body changes | All changes |
| What fails | Signature changes, new classes | Nothing |
| Mechanism | JVM debug agent | Classloader swap |
| State | Preserved | Lost |

DevTools restart:
1. Monitors classpath for changes
2. Throws away restart classloader
3. Creates new restart classloader
4. Reloads all application classes
5. Reinitializes ApplicationContext

**4. Real-world example:**
Development scenarios:
```java
// Hot reload works: Change method implementation
public String greet() {
    return "Hello";  // Change to "Hi" - hot reload works
}

// Hot reload fails: Add new method
public String newMethod() {  // Requires restart
    return "New";
}

// Hot reload fails: Change method signature
public String greet(String name) {  // Requires restart
    return "Hello " + name;
}
```

For complex changes, consider JRebel (commercial) for true hot reload.

**5. Pitfalls:**
- Expecting hot reload to handle structural changes
- Not realizing DevTools restart loses in-memory state
- Debug mode hot swap ≠ DevTools restart

**6. Follow-up questions:**
- How does JRebel differ from DevTools?
- When would restart fail to pick up changes?

**7. Senior summary:**
"Hot reload (JVM HotSwap) is fast but only handles method body changes. DevTools restart is slower but handles all changes by swapping classloaders. For development, DevTools restart is reliable; for maximum productivity with no restart, consider JRebel."

--------------------------------------------------
### Question 40: Difference between WAR and JAR deployment?

**1. Definition:**
JAR deployment packages an embedded server inside the application (self-contained). WAR deployment requires an external servlet container like Tomcat or WebLogic.

**2. Why it matters:**
Deployment strategy affects infrastructure, operations, and scalability. JAR is the modern approach for microservices and containers; WAR is for enterprise environments requiring external servers.

**3. Internal working:**

| Aspect | JAR (Embedded) | WAR (External) |
|--------|---------------|----------------|
| Server | Inside app | External Tomcat/WebLogic |
| Startup | `java -jar app.jar` | Deploy to container |
| Port config | application.properties | server.xml |
| Clustering | App-level | Container-level |
| Memory | Per-app JVM | Shared JVM |
| Docker | Simple | Complex |

WAR conversion:
```java
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(
            SpringApplicationBuilder builder) {
        return builder.sources(Application.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

```xml
<packaging>war</packaging>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <scope>provided</scope>  <!-- Container provides this -->
</dependency>
```

**4. Real-world example:**
Enterprise migration:
- Legacy: 10 WARs on 2 WebLogic clusters
- Modern: 10 JARs in Kubernetes, each with own resources
- Benefit: Independent scaling, isolated failures, simpler deployments

Some enterprises still require WAR for:
- Shared container management
- Legacy SSO integration
- Compliance requirements

**5. Pitfalls:**
- Mixing embedded and provided server dependencies
- Forgetting SpringBootServletInitializer for WAR
- Container classloader conflicts

**6. Follow-up questions:**
- Can a Spring Boot WAR still run standalone?
- How do you handle logging conflicts between container and app?

**7. Senior summary:**
""JAR with embedded server is the modern default—self-contained, container-friendly, independently scalable. WAR is for enterprise environments requiring external servers. Spring Boot WARs can run both ways. I default to JAR unless there's a specific enterprise requirement."

---

## REST + MVC QUESTIONS (41-61)

--------------------------------------------------
### Question 41: Difference between @Controller and @RestController?

**1. Definition:**
`@Controller` returns view names for template resolution. `@RestController` combines `@Controller` + `@ResponseBody`, returning data directly serialized to JSON/XML.

**2. Why it matters:**
Using the wrong annotation causes confusing errors—view resolver failures or unwanted response wrapping. Proper usage defines your API contract.

**3. Internal working:**
```java
@Controller
public class ViewController {
    @GetMapping("/page")
    public String page(Model model) {
        model.addAttribute("data", getData());
        return "page";  // Returns view name → ViewResolver → HTML
    }

    @GetMapping("/api/data")
    @ResponseBody  // Explicit - return JSON
    public Data getData() {
        return new Data();
    }
}

@RestController  // = @Controller + @ResponseBody on all methods
public class ApiController {
    @GetMapping("/api/data")
    public Data getData() {
        return new Data();  // Automatically serialized to JSON
    }
}
```

`@RestController` internally:
```java
@Controller
@ResponseBody
public @interface RestController { }
```

**4. Real-world example:**
Mixed application with UI and API:
```java
@Controller
@RequestMapping("/admin")
public class AdminViewController {
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", statsService.get());
        return "admin/dashboard";  // Thymeleaf template
    }
}

@RestController
@RequestMapping("/api/v1/admin")
public class AdminApiController {
    @GetMapping("/stats")
    public ResponseEntity<Stats> getStats() {
        return ResponseEntity.ok(statsService.get());
    }
}
```

**5. Pitfalls:**
- Using @Controller for REST APIs and forgetting @ResponseBody
- Using @RestController for MVC views (returns literal string, not view)
- Not understanding that @RestController affects ALL methods in class

**6. Follow-up questions:**
- Can you mix @ResponseBody and view returns in @Controller?
- How does content negotiation work with @RestController?

**7. Senior summary:**
"@RestController is @Controller + @ResponseBody applied to all methods—for REST APIs returning data. @Controller is for MVC applications returning view names. I use @RestController for pure APIs, @Controller for template-based UIs, sometimes mixing both in the same application."

--------------------------------------------------
### Question 42: Difference between @RequestParam and @PathVariable?

**1. Definition:**
`@PathVariable` extracts values from URI path segments (`/users/{id}`). `@RequestParam` extracts values from query parameters (`/users?id=123`).

**2. Why it matters:**
RESTful design uses path variables for resource identification and query params for filtering/pagination. Proper usage affects API discoverability and cacheability.

**3. Internal working:**
```java
// PathVariable - part of resource identifier
@GetMapping("/users/{userId}/orders/{orderId}")
public Order getOrder(
    @PathVariable Long userId,
    @PathVariable("orderId") Long order) {  // Custom name binding
    return orderService.find(userId, order);
}

// RequestParam - filtering, pagination
@GetMapping("/orders")
public Page<Order> getOrders(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(required = false) String status,
    @RequestParam List<String> tags) {  // Multi-value: ?tags=a&tags=b
    return orderService.findAll(page, size, status, tags);
}
```

Matrix variables (less common):
```java
// /cars;color=red;year=2020
@GetMapping("/cars")
public List<Car> getCars(@MatrixVariable String color, @MatrixVariable int year)
```

**4. Real-world example:**
E-commerce product API:
```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    // GET /products/123 - specific product
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) { }

    // GET /products?category=electronics&minPrice=100&sort=price
    @GetMapping
    public Page<Product> searchProducts(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(defaultValue = "name") String sort,
        Pageable pageable) { }
}
```

**5. Pitfalls:**
- Using query params for resource identification (not RESTful)
- Forgetting `required = false` for optional query params
- Path variable name mismatch with method parameter

**6. Follow-up questions:**
- How do you handle optional path variables?
- What happens if a required @RequestParam is missing?

**7. Senior summary:**
"@PathVariable for resource identification in URL path, @RequestParam for query parameters used in filtering and pagination. Path variables are mandatory by default; query params can have defaults. Follow REST conventions—identifiers in path, filters in query string."

--------------------------------------------------
### Question 43: What is @RequestBody?

**1. Definition:**
`@RequestBody` binds the HTTP request body to a method parameter, deserializing JSON/XML content into a Java object using configured HttpMessageConverters.

**2. Why it matters:**
It's the primary mechanism for receiving structured data in REST APIs. Understanding its deserialization behavior is crucial for proper API design and error handling.

**3. Internal working:**
```java
@PostMapping("/orders")
public Order createOrder(@RequestBody @Valid OrderRequest request) {
    // request is deserialized from JSON body
    return orderService.create(request);
}
```

Processing flow:
1. Request received with Content-Type header
2. `RequestResponseBodyMethodProcessor` handles @RequestBody
3. Appropriate `HttpMessageConverter` selected (Jackson for JSON)
4. Body deserialized to target type
5. If @Valid present, validation triggered
6. Object passed to method

**4. Real-world example:**
Complex order creation:
```java
public record OrderRequest(
    @NotNull Long customerId,
    @NotEmpty List<OrderItem> items,
    @Valid ShippingAddress address,
    String couponCode
) {}

@PostMapping("/orders")
public ResponseEntity<Order> createOrder(
    @RequestBody @Valid OrderRequest request) {

    Order order = orderService.create(request);
    return ResponseEntity
        .created(URI.create("/orders/" + order.getId()))
        .body(order);
}
```

Request:
```json
{
  "customerId": 123,
  "items": [{"productId": 456, "quantity": 2}],
  "address": {"street": "123 Main", "city": "NYC"}
}
```

**5. Pitfalls:**
- Missing Content-Type header causing 415 error
- Not handling deserialization exceptions globally
- Forgetting @Valid for nested object validation
- Request body can only be read once (streaming)

**6. Follow-up questions:**
- How do you handle deserialization errors gracefully?
- Can you have multiple @RequestBody parameters?

**7. Senior summary:**
"@RequestBody deserializes HTTP body to Java objects using HttpMessageConverters. Always combine with @Valid for input validation. Handle deserialization errors with @ExceptionHandler. The body is consumed once—can't read multiple times."

--------------------------------------------------
### Question 44: What is @ResponseBody?

**1. Definition:**
`@ResponseBody` indicates that the method return value should be serialized directly to the HTTP response body, not interpreted as a view name.

**2. Why it matters:**
It's what makes REST APIs return JSON instead of trying to render templates. Understanding the serialization pipeline helps debug response issues.

**3. Internal working:**
```java
@Controller
public class MixedController {

    @GetMapping("/api/user")
    @ResponseBody  // Return as JSON
    public User getUser() {
        return userService.getCurrent();
    }

    @GetMapping("/user")
    public String userPage() {
        return "user/profile";  // View name
    }
}
```

Serialization flow:
1. Method returns object
2. `RequestResponseBodyMethodProcessor` intercepts
3. Content negotiation determines format (Accept header)
4. `HttpMessageConverter` serializes (Jackson → JSON)
5. Response written with Content-Type header

@RestController makes this implicit for all methods.

**4. Real-world example:**
Custom serialization control:
```java
@Controller
public class ReportController {

    @GetMapping("/report/json")
    @ResponseBody
    public Report getReportJson() {
        return reportService.generate();  // JSON
    }

    @GetMapping("/report/html")
    public String getReportHtml(Model model) {
        model.addAttribute("report", reportService.generate());
        return "report/view";  // HTML template
    }

    @GetMapping(value = "/report/csv", produces = "text/csv")
    @ResponseBody
    public byte[] getReportCsv() {
        return reportService.generateCsv();  // CSV bytes
    }
}
```

**5. Pitfalls:**
- Returning void with @ResponseBody (empty response, not void)
- Not setting produces attribute for non-JSON formats
- Circular references in objects causing serialization failure

**6. Follow-up questions:**
- How does content negotiation work?
- How do you customize JSON serialization for specific endpoints?

**7. Senior summary:**
"@ResponseBody bypasses view resolution, serializing return values directly to response body. Content type determined by Accept header and produces attribute. Implicit in @RestController. Use for REST endpoints returning data, not views."

--------------------------------------------------
### Question 45: How JSON conversion happens?

**1. Definition:**
Spring Boot uses Jackson's `ObjectMapper` through `MappingJackson2HttpMessageConverter` to serialize objects to JSON and deserialize JSON to objects.

**2. Why it matters:**
JSON handling affects performance, security, and API contract. Understanding Jackson configuration helps solve serialization issues and optimize performance.

**3. Internal working:**
```
Request → HttpMessageConverter → Object
Object → HttpMessageConverter → Response

For JSON:
MappingJackson2HttpMessageConverter wraps ObjectMapper

ObjectMapper configuration:
- SerializationFeature (null handling, date format)
- DeserializationFeature (unknown properties, null handling)
- Modules (JavaTimeModule for dates)
```

Spring Boot auto-configuration:
```yaml
spring:
  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      fail-on-unknown-properties: false
    default-property-inclusion: non_null
    date-format: yyyy-MM-dd'T'HH:mm:ss.SSSZ
```

**4. Real-world example:**
Custom ObjectMapper for API:
```java
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .addModule(new Jdk8Module())
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .build();
    }
}
```

**5. Pitfalls:**
- Multiple ObjectMapper beans causing inconsistent serialization
- Not registering JavaTimeModule for Java 8 dates
- Circular references causing infinite loops
- @JsonIgnore vs @JsonProperty conflicts

**6. Follow-up questions:**
- How do you handle circular references?
- How do you use different ObjectMappers for different endpoints?

**7. Senior summary:**
"Jackson ObjectMapper handles JSON conversion via MappingJackson2HttpMessageConverter. Configure via spring.jackson.* properties or custom ObjectMapper bean. Always register JavaTimeModule for dates, disable fail-on-unknown-properties for forward compatibility."

--------------------------------------------------
### Question 46: Role of Jackson in Spring Boot?

**1. Definition:**
Jackson is the default JSON processing library in Spring Boot, providing serialization (object→JSON), deserialization (JSON→object), and data binding through `ObjectMapper`.

**2. Why it matters:**
Jackson is involved in almost every REST request/response. Understanding its features enables proper API design, performance optimization, and security hardening.

**3. Internal working:**
Jackson components:
- `ObjectMapper`: Main API for all operations
- `JsonParser`: Low-level streaming read
- `JsonGenerator`: Low-level streaming write
- `@JsonProperty`, `@JsonIgnore`, `@JsonFormat`: Annotations

Auto-configured by `JacksonAutoConfiguration`:
```java
@Bean
@ConditionalOnMissingBean
public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
    return builder.createXmlMapper(false).build();
}
```

**4. Real-world example:**
API with custom serialization:
```java
public class Order {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    @JsonIgnore  // Don't expose internal data
    private String internalReference;

    @JsonProperty("customer")  // Rename in JSON
    private String customerName;

    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal amount;
}

// Custom serializer
public class MoneySerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal value, JsonGenerator gen,
                          SerializerProvider provider) throws IOException {
        gen.writeString(value.setScale(2, RoundingMode.HALF_UP).toString());
    }
}
```

**5. Pitfalls:**
- Exposing internal fields unintentionally (@JsonIgnore)
- Date format inconsistencies across APIs
- Performance issues with large objects (consider streaming)
- Security: polymorphic deserialization vulnerabilities

**6. Follow-up questions:**
- How do you handle polymorphic types?
- What's the performance difference between ObjectMapper and streaming API?

**7. Senior summary:**
"Jackson is Spring Boot's default JSON library—ObjectMapper handles all serialization. Use annotations like @JsonFormat, @JsonIgnore, @JsonProperty for control. Configure globally via spring.jackson.* or custom ObjectMapper. Be aware of security implications with polymorphic deserialization."

--------------------------------------------------
### Question 47: What is HttpMessageConverter?

**1. Definition:**
`HttpMessageConverter` is a strategy interface for converting HTTP request bodies to objects and objects to HTTP response bodies, supporting multiple formats (JSON, XML, form data).

**2. Why it matters:**
Content negotiation relies on converters. Understanding the converter chain explains format selection and enables custom formats.

**3. Internal working:**
```java
public interface HttpMessageConverter<T> {
    boolean canRead(Class<?> clazz, MediaType mediaType);
    boolean canWrite(Class<?> clazz, MediaType mediaType);
    List<MediaType> getSupportedMediaTypes();
    T read(Class<? extends T> clazz, HttpInputMessage input);
    void write(T t, MediaType contentType, HttpOutputMessage output);
}
```

Default converters (order matters):
1. `ByteArrayHttpMessageConverter` - byte[]
2. `StringHttpMessageConverter` - String
3. `FormHttpMessageConverter` - form data
4. `MappingJackson2HttpMessageConverter` - JSON
5. `MappingJackson2XmlHttpMessageConverter` - XML (if on classpath)

**4. Real-world example:**
Custom converter for CSV:
```java
@Component
public class CsvHttpMessageConverter
        extends AbstractHttpMessageConverter<List<Object>> {

    public CsvHttpMessageConverter() {
        super(new MediaType("text", "csv"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    @Override
    protected List<Object> readInternal(Class<? extends List<Object>> clazz,
                                         HttpInputMessage input) {
        // Parse CSV to List
    }

    @Override
    protected void writeInternal(List<Object> objects,
                                  HttpOutputMessage output) {
        // Write List as CSV
    }
}
```

Usage:
```java
@GetMapping(value = "/export", produces = "text/csv")
public List<User> exportUsers() {
    return userService.findAll();
}
```

**5. Pitfalls:**
- Converter order affects selection (first match wins)
- Not configuring appropriate converters for content types
- Performance issues with large bodies (streaming converters)

**6. Follow-up questions:**
- How do you add a custom HttpMessageConverter?
- How does content negotiation use converters?

**7. Senior summary:**
"HttpMessageConverter handles format conversion between HTTP bodies and Java objects. Spring Boot auto-configures converters for JSON, XML, form data. Custom converters enable proprietary formats. Converter selection based on Content-Type (read) and Accept headers (write)."

--------------------------------------------------
### Question 48: How to handle validation in Spring Boot?

**1. Definition:**
Spring Boot uses Bean Validation (JSR-380) with Hibernate Validator, triggered via `@Valid` or `@Validated` annotations, with errors handled through `BindingResult` or exception handlers.

**2. Why it matters:**
Input validation is the first line of defense against bad data. Proper validation prevents database errors, security issues, and business logic failures.

**3. Internal working:**
```java
// DTO with validation
public record CreateUserRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank @Size(min = 8, max = 100)
    String password,

    @Min(18) @Max(120)
    Integer age,

    @Valid  // Cascade validation to nested object
    Address address
) {}

// Controller
@PostMapping("/users")
public ResponseEntity<?> createUser(
    @RequestBody @Valid CreateUserRequest request) {
    // If validation fails, MethodArgumentNotValidException thrown
    return ResponseEntity.ok(userService.create(request));
}
```

Common annotations:
- `@NotNull`, `@NotBlank`, `@NotEmpty`
- `@Size`, `@Min`, `@Max`
- `@Email`, `@Pattern`
- `@Past`, `@Future`
- `@Valid` (cascading)

**4. Real-world example:**
Global exception handling for validation:
```java
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (e1, e2) -> e1  // Handle duplicates
            ));

        return ResponseEntity.badRequest()
            .body(new ValidationErrorResponse("Validation failed", errors));
    }
}
```

**5. Pitfalls:**
- Forgetting @Valid on nested objects
- Not handling validation exceptions globally
- Over-validating in controllers vs. service layer
- Using @NotNull when @NotBlank is needed for strings

**6. Follow-up questions:**
- How do you create custom validators?
- What's the difference between @Valid and @Validated?

**7. Senior summary:**
"Use @Valid on @RequestBody to trigger Bean Validation. Handle MethodArgumentNotValidException in @ControllerAdvice for consistent error responses. Use @Valid for cascading. Custom validators via @Constraint. Validate at API boundary, trust internal code."

--------------------------------------------------
### Question 49: Difference between @Valid and @Validated?

**1. Definition:**
`@Valid` is JSR-380 standard, triggers cascaded validation. `@Validated` is Spring's extension, enabling validation groups for conditional validation logic.

**2. Why it matters:**
Validation groups enable different validation rules for create vs. update operations. Understanding both annotations enables sophisticated validation strategies.

**3. Internal working:**
```java
// Validation groups
public interface OnCreate {}
public interface OnUpdate {}

public record UserRequest(
    @Null(groups = OnCreate.class)
    @NotNull(groups = OnUpdate.class)
    Long id,

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    String name,

    @NotBlank(groups = OnCreate.class)
    String password
) {}

// Usage with @Validated
@RestController
@Validated  // Enable method-level validation
public class UserController {

    @PostMapping
    public User create(
        @RequestBody @Validated(OnCreate.class) UserRequest request) {
        // id must be null, password required
    }

    @PutMapping("/{id}")
    public User update(
        @PathVariable Long id,
        @RequestBody @Validated(OnUpdate.class) UserRequest request) {
        // id must not be null, password optional
    }
}
```

| Aspect | @Valid | @Validated |
|--------|--------|------------|
| Origin | JSR-380 | Spring |
| Groups | No | Yes |
| Method params | Yes (nested) | Yes |
| Class-level | No | Yes |

**4. Real-world example:**
Partial update validation:
```java
@PatchMapping("/users/{id}")
public User partialUpdate(
    @PathVariable Long id,
    @RequestBody @Validated(OnPatch.class) UserPatchRequest request) {
    // Only validates fields present in patch
}
```

**5. Pitfalls:**
- Using @Valid when groups are needed
- Forgetting to specify groups (no validation runs)
- Complex group inheritance becoming unmaintainable

**6. Follow-up questions:**
- Can you use both @Valid and @Validated together?
- How do group sequences work?

**7. Senior summary:**
"@Valid is standard JSR-380, sufficient for most cases. @Validated is Spring's extension adding validation groups for conditional validation. I use groups for create/update scenarios where different rules apply. Both trigger MethodArgumentNotValidException on failure."

--------------------------------------------------
### Question 50: How to create custom exception?

**1. Definition:**
Custom exceptions extend `RuntimeException` (or appropriate base), carry domain-specific information, and are handled via `@ExceptionHandler` or `@ControllerAdvice` for proper HTTP responses.

**2. Why it matters:**
Well-designed exceptions improve debugging, provide clear error messages to clients, and separate business logic from error handling.

**3. Internal working:**
```java
// Custom exception
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceType;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceType, Object id) {
        super(String.format("%s with id %s not found", resourceType, id));
        this.resourceType = resourceType;
        this.resourceId = id;
    }
}

// Business exception with error code
public class PaymentFailedException extends RuntimeException {
    private final String errorCode;
    private final String providerMessage;

    public PaymentFailedException(String code, String provider, String msg) {
        super("Payment failed: " + msg);
        this.errorCode = code;
        this.providerMessage = msg;
    }
}
```

**4. Real-world example:**
Complete exception handling:
```java
// Domain exceptions
public class OrderException extends RuntimeException {
    private final ErrorCode errorCode;
    public enum ErrorCode { INVALID_STATE, INSUFFICIENT_STOCK, PAYMENT_FAILED }
}

// Handler
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ErrorResponse> handleOrderError(OrderException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case INVALID_STATE -> HttpStatus.CONFLICT;
            case INSUFFICIENT_STOCK -> HttpStatus.UNPROCESSABLE_ENTITY;
            case PAYMENT_FAILED -> HttpStatus.PAYMENT_REQUIRED;
        };
        return ResponseEntity.status(status)
            .body(new ErrorResponse(ex.getErrorCode().name(), ex.getMessage()));
    }
}

public record ErrorResponse(String code, String message, Instant timestamp) {
    public ErrorResponse(String code, String message) {
        this(code, message, Instant.now());
    }
}
```

**5. Pitfalls:**
- Exposing internal exception details to clients
- Not using appropriate HTTP status codes
- Creating too many exception classes (over-engineering)
- Catching and re-throwing without adding value

**6. Follow-up questions:**
- How do you log exceptions appropriately?
- How do you handle exceptions in async operations?

**7. Senior summary:**
"Create domain exceptions extending RuntimeException with relevant context (error codes, resource info). Handle in @ControllerAdvice with proper HTTP status mapping. Never expose stack traces to clients. I categorize exceptions by HTTP status family for consistent handling."

--------------------------------------------------
### Question 51: What is @ControllerAdvice?

**1. Definition:**
`@ControllerAdvice` is a specialization of `@Component` that enables global exception handling, model attribute binding, and data binding initialization across all controllers.

**2. Why it matters:**
Centralized exception handling ensures consistent error responses, reduces code duplication, and makes error handling testable.

**3. Internal working:**
```java
@RestControllerAdvice  // = @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    // Exception handling
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest().body(toError(ex));
    }

    // Global model attribute
    @ModelAttribute("currentUser")
    public User currentUser(@AuthenticationPrincipal UserDetails user) {
        return userService.findByUsername(user.getUsername());
    }

    // Data binder initialization
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new LocalDateEditor());
    }
}
```

Scoping options:
```java
@ControllerAdvice(assignableTypes = {ApiController.class})
@ControllerAdvice(basePackages = "com.example.api")
@ControllerAdvice(annotations = RestController.class)
```

**4. Real-world example:**
Comprehensive API error handling:
```java
@RestControllerAdvice(annotations = RestController.class)
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = extractFieldErrors(ex);
        return ResponseEntity.badRequest()
            .body(ApiError.validation(errors));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(NOT_FOUND)
            .body(ApiError.notFound(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);  // Log full stack trace
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
            .body(ApiError.internal("An unexpected error occurred"));
    }
}
```

**5. Pitfalls:**
- Multiple @ControllerAdvice with conflicting handlers (order matters)
- Not scoping @ControllerAdvice (applies to ALL controllers)
- Catching Exception.class without logging
- Missing ResponseBody in error handlers (use @RestControllerAdvice)

**6. Follow-up questions:**
- How do you order multiple @ControllerAdvice classes?
- How do you test @ControllerAdvice handlers?

**7. Senior summary:**
"@ControllerAdvice enables global exception handling, model attributes, and binders. I use @RestControllerAdvice for APIs with scoping by package or annotation. Always log unexpected exceptions, never expose internal details. @Order controls precedence when multiple handlers exist."

--------------------------------------------------
### Question 52: What is @ControllerAdvice? (Repeated - Advanced)

**1. Definition:**
Beyond basic exception handling, `@ControllerAdvice` is a cross-cutting mechanism that intercepts controller calls for AOP-like functionality without full AspectJ.

**2. Why it matters:**
It enables separation of concerns—controllers focus on business logic while advice handles cross-cutting concerns like error handling, logging, and response modification.

**3. Internal working:**
Processing order:
1. Request enters `DispatcherServlet`
2. `@ModelAttribute` methods from advice called
3. `@InitBinder` methods initialize binders
4. Controller method executed
5. If exception, `@ExceptionHandler` searched (controller first, then advice)

Multiple advice ordering:
```java
@ControllerAdvice
@Order(1)  // Higher priority
public class SecurityExceptionHandler { }

@ControllerAdvice
@Order(2)  // Lower priority
public class BusinessExceptionHandler { }
```

**4. Real-world example:**
Advanced use—response wrapping:
```java
@RestControllerAdvice
public class ResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                           Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.getParameterType().equals(ApiResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                   MethodParameter returnType,
                                   MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> converterType,
                                   ServerHttpRequest request,
                                   ServerHttpResponse response) {
        // Wrap all responses in standard format
        return ApiResponse.success(body);
    }
}
```

**5. Pitfalls:**
- ResponseBodyAdvice affecting all responses including errors
- Order conflicts between multiple advice classes
- Performance impact of complex advice logic

**6. Follow-up questions:**
- How do you implement response modification with @ControllerAdvice?
- What's RequestBodyAdvice used for?

**7. Senior summary:**
"@ControllerAdvice is Spring's AOP-lite for web layer—handles exceptions, modifies requests/responses, adds global model attributes. Use ResponseBodyAdvice for response wrapping, RequestBodyAdvice for request preprocessing. Order with @Order when multiple advice classes exist."

--------------------------------------------------
### Question 53: What is global exception handling?

**1. Definition:**
Global exception handling centralizes error handling logic in `@ControllerAdvice` classes, ensuring consistent error responses and reducing duplicate error handling code.

**2. Why it matters:**
Consistent error responses improve API usability, simplify client integration, and make debugging easier. Centralized handling ensures security-sensitive exceptions don't leak information.

**3. Internal working:**
Exception resolution order:
1. `@ExceptionHandler` in the controller
2. `@ExceptionHandler` in `@ControllerAdvice`
3. `DefaultHandlerExceptionResolver` (Spring defaults)
4. Response error handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        // Override Spring's default validation handling
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(DataAccessException ex) {
        log.error("Database error", ex);
        return ResponseEntity.status(SERVICE_UNAVAILABLE)
            .body(ErrorResponse.of("DATABASE_ERROR", "Service temporarily unavailable"));
    }
}
```

**4. Real-world example:**
Production-ready exception handling:
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAll(Exception ex, WebRequest request) {
        String traceId = MDC.get("traceId");
        log.error("Unhandled exception [traceId={}]", traceId, ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
        );
        problem.setProperty("traceId", traceId);
        problem.setInstance(URI.create(request.getDescription(false)));

        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(problem);
    }
}
```

**5. Pitfalls:**
- Catching and swallowing exceptions without logging
- Returning internal exception messages to clients
- Not including correlation IDs in error responses
- Forgetting to handle async exceptions differently

**6. Follow-up questions:**
- How do you handle exceptions in @Async methods?
- What's ProblemDetail in Spring 6?

**7. Senior summary:**
"Global exception handling via @ControllerAdvice centralizes error handling, ensuring consistent responses and proper logging. Include trace IDs for debugging, never expose internal details. I use ProblemDetail (RFC 7807) for standard error format in modern APIs."

--------------------------------------------------
### Question 54: What is ResponseEntity?

**1. Definition:**
`ResponseEntity<T>` represents the full HTTP response—status code, headers, and body—giving complete control over the response sent to clients.

**2. Why it matters:**
Fine-grained response control is essential for proper REST APIs—setting location headers, cache controls, custom status codes, and conditional responses.

**3. Internal working:**
```java
@GetMapping("/orders/{id}")
public ResponseEntity<Order> getOrder(@PathVariable Long id) {
    return orderService.findById(id)
        .map(order -> ResponseEntity.ok()
            .header("X-Custom-Header", "value")
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
            .lastModified(order.getModifiedAt())
            .body(order))
        .orElse(ResponseEntity.notFound().build());
}

@PostMapping("/orders")
public ResponseEntity<Order> createOrder(@RequestBody @Valid OrderRequest request) {
    Order created = orderService.create(request);
    return ResponseEntity
        .created(URI.create("/orders/" + created.getId()))
        .body(created);
}
```

Builder methods:
- `ok()`, `created(URI)`, `accepted()`, `noContent()`
- `badRequest()`, `notFound()`, `status(HttpStatus)`
- `headers()`, `contentType()`, `body()`

**4. Real-world example:**
Conditional requests for efficiency:
```java
@GetMapping("/products/{id}")
public ResponseEntity<Product> getProduct(
        @PathVariable Long id,
        @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

    Product product = productService.findById(id);
    String etag = "\"" + product.getVersion() + "\"";

    if (etag.equals(ifNoneMatch)) {
        return ResponseEntity.status(NOT_MODIFIED)
            .eTag(etag)
            .build();
    }

    return ResponseEntity.ok()
        .eTag(etag)
        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
        .body(product);
}
```

**5. Pitfalls:**
- Overusing ResponseEntity when simple return types suffice
- Forgetting to include body in non-204 responses
- Not using proper status codes (201 for create, 204 for no content)

**6. Follow-up questions:**
- When would you return the POJO directly vs ResponseEntity?
- How do you handle conditional GETs with ETags?

**7. Senior summary:**
"ResponseEntity provides complete HTTP response control—status, headers, body. Use for proper REST semantics: 201 Created with Location, 304 Not Modified, cache headers. For simple 200 OK responses, returning the object directly is cleaner."

--------------------------------------------------
### Question 55: How do you return proper HTTP status codes?

**1. Definition:**
Proper status codes are returned via `ResponseEntity`, `@ResponseStatus` annotation, or exception handlers, following REST semantics for each operation type.

**2. Why it matters:**
Status codes are the primary communication mechanism in REST. Clients rely on them for control flow—retry logic, error handling, cache behavior.

**3. Internal working:**
```java
// Method 1: ResponseEntity
@PostMapping("/users")
public ResponseEntity<User> create(@RequestBody UserRequest request) {
    User user = userService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .location(URI.create("/users/" + user.getId()))
        .body(user);
}

// Method 2: @ResponseStatus on method
@DeleteMapping("/users/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void delete(@PathVariable Long id) {
    userService.delete(id);
}

// Method 3: @ResponseStatus on exception
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException { }
```

Common status codes:
| Code | Meaning | Use Case |
|------|---------|----------|
| 200 | OK | Successful GET, PUT |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Validation failure |
| 401 | Unauthorized | Not authenticated |
| 403 | Forbidden | Not authorized |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | State conflict |
| 422 | Unprocessable | Business rule failure |
| 500 | Internal Error | Unexpected error |

**4. Real-world example:**
Comprehensive status code handling:
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable Long id) {
        return orderService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());  // 404
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody @Valid OrderRequest req) {
        Order order = orderService.create(req);
        return ResponseEntity.created(
            URI.create("/api/orders/" + order.getId()))
            .body(order);  // 201
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        try {
            orderService.cancel(id);
            return ResponseEntity.noContent().build();  // 204
        } catch (InvalidStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)  // 409
                .body(new ErrorResponse("INVALID_STATE", e.getMessage()));
        }
    }
}
```

**5. Pitfalls:**
- Using 200 for everything (lazy but wrong)
- 500 for client errors (should be 4xx)
- Not distinguishing 401 vs 403
- Missing Location header on 201

**6. Follow-up questions:**
- When would you use 422 vs 400?
- How do you handle 202 Accepted for async operations?

**7. Senior summary:**
"Use ResponseEntity for explicit status control, @ResponseStatus for simple cases. Follow REST semantics: 201 for create with Location, 204 for delete, 4xx for client errors, 5xx for server errors. Proper status codes enable client retry logic and caching."

--------------------------------------------------
### Question 56: Difference between PUT and PATCH?

**1. Definition:**
PUT replaces the entire resource with the request payload. PATCH applies partial modifications to a resource, updating only specified fields.

**2. Why it matters:**
Choosing correctly affects bandwidth, idempotency, and conflict resolution. PUT is simpler but wasteful; PATCH is efficient but requires careful implementation.

**3. Internal working:**
```java
// PUT - full replacement (idempotent)
@PutMapping("/users/{id}")
public User updateUser(@PathVariable Long id, @RequestBody @Valid User user) {
    user.setId(id);
    return userService.replace(user);  // Replace entire entity
}

// PATCH - partial update
@PatchMapping("/users/{id}")
public User patchUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
    return userService.patch(id, updates);
}
```

| Aspect | PUT | PATCH |
|--------|-----|-------|
| Idempotent | Yes | Not guaranteed |
| Body | Complete resource | Partial changes |
| Missing fields | Set to null/default | Unchanged |
| Bandwidth | Higher | Lower |

**4. Real-world example:**
Implementing PATCH properly:
```java
@PatchMapping("/users/{id}")
public ResponseEntity<User> patchUser(
        @PathVariable Long id,
        @RequestBody JsonMergePatch patch) {

    User user = userService.findById(id);
    User patched = applyPatch(patch, user);
    User saved = userService.save(patched);

    return ResponseEntity.ok(saved);
}

private User applyPatch(JsonMergePatch patch, User user) {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode patched = patch.apply(mapper.valueToTree(user));
    return mapper.treeToValue(patched, User.class);
}
```

JSON Merge Patch (RFC 7396):
```json
{
  "email": "new@email.com",
  "phone": null
}
```
Sets email, removes phone, leaves other fields unchanged.

**5. Pitfalls:**
- Using PUT when PATCH is appropriate (wasting bandwidth)
- PATCH without null handling (ambiguity)
- Not validating partial updates properly
- Assuming PATCH is idempotent (depends on implementation)

**6. Follow-up questions:**
- How do you handle null in PATCH—remove field or ignore?
- What's JSON Patch vs JSON Merge Patch?

**7. Senior summary:**
"PUT replaces the entire resource, PATCH updates specific fields. Use JSON Merge Patch (RFC 7396) for simple PATCH operations. PUT is idempotent, PATCH may not be. I use PATCH for large resources where sending the full object is wasteful."

--------------------------------------------------
### Question 57: How does file upload work in Spring Boot?

**1. Definition:**
File uploads use `multipart/form-data` content type, handled via `MultipartFile` parameter or `@RequestPart`, with configurable size limits and storage options.

**2. Why it matters:**
File handling affects memory usage, security, and user experience. Proper implementation prevents OOM errors and ensures secure file processing.

**3. Internal working:**
```java
@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<FileResponse> uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("metadata") String metadata) {

    if (file.isEmpty()) {
        throw new BadRequestException("File is empty");
    }

    String filename = StringUtils.cleanPath(file.getOriginalFilename());
    Path targetPath = uploadDir.resolve(filename);
    Files.copy(file.getInputStream(), targetPath, REPLACE_EXISTING);

    return ResponseEntity.ok(new FileResponse(filename, file.getSize()));
}
```

Configuration:
```yaml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 50MB
      file-size-threshold: 2KB  # Threshold before writing to disk
      location: /tmp/uploads    # Temp storage location
```

**4. Real-world example:**
Production file upload with validation:
```java
@PostMapping("/documents")
public ResponseEntity<Document> uploadDocument(
        @RequestPart("file") MultipartFile file,
        @RequestPart("metadata") @Valid DocumentMetadata metadata) {

    // Validate file type
    String contentType = file.getContentType();
    if (!ALLOWED_TYPES.contains(contentType)) {
        throw new UnsupportedMediaTypeException("File type not allowed");
    }

    // Validate file size
    if (file.getSize() > MAX_FILE_SIZE) {
        throw new FileTooLargeException("Max size is " + MAX_FILE_SIZE);
    }

    // Scan for viruses (production requirement)
    virusScanner.scan(file.getInputStream());

    // Store in cloud storage
    String url = cloudStorage.upload(file, metadata);

    return ResponseEntity.created(URI.create(url))
        .body(new Document(metadata, url));
}
```

**5. Pitfalls:**
- Not limiting file size (OOM, disk exhaustion)
- Trusting client-provided filename (path traversal attack)
- Storing files in application directory
- Not scanning for malware

**6. Follow-up questions:**
- How do you handle large file uploads without memory issues?
- How do you implement resumable uploads?

**7. Senior summary:**
"File uploads use MultipartFile with configurable size limits. Always validate file type, size, and sanitize filenames. For large files, stream to storage instead of loading into memory. In production, scan for malware and store in cloud storage, not local filesystem."

--------------------------------------------------
### Question 58: How multipart request handling works?

**1. Definition:**
Multipart requests split HTTP body into multiple parts, each with its own content type, enabling mixed content uploads (files + JSON) in a single request.

**2. Why it matters:**
Understanding multipart handling enables proper API design for complex uploads and debugging upload issues.

**3. Internal working:**
Multipart structure:
```
POST /upload HTTP/1.1
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary

------WebKitFormBoundary
Content-Disposition: form-data; name="file"; filename="doc.pdf"
Content-Type: application/pdf

[binary data]
------WebKitFormBoundary
Content-Disposition: form-data; name="metadata"
Content-Type: application/json

{"title": "My Document"}
------WebKitFormBoundary--
```

Spring handling:
1. `MultipartResolver` parses request
2. Parts stored as `MultipartFile` or temp files
3. `@RequestPart` binds parts to parameters
4. After request, temp files cleaned up

```java
@PostMapping("/products")
public Product createProduct(
        @RequestPart("image") MultipartFile image,
        @RequestPart("product") @Valid ProductRequest product) {
    // image is file part
    // product is JSON part deserialized
}
```

**4. Real-world example:**
Mixed upload with multiple files:
```java
@PostMapping("/listings")
public Listing createListing(
        @RequestPart("photos") List<MultipartFile> photos,
        @RequestPart("details") @Valid ListingDetails details,
        @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {

    // Validate total upload size
    long totalSize = Stream.concat(photos.stream(),
            Optional.ofNullable(attachments).stream().flatMap(List::stream))
        .mapToLong(MultipartFile::getSize)
        .sum();

    if (totalSize > MAX_TOTAL_SIZE) {
        throw new RequestTooLargeException();
    }

    List<String> photoUrls = photos.stream()
        .map(storage::upload)
        .toList();

    return listingService.create(details, photoUrls);
}
```

**5. Pitfalls:**
- @RequestParam for JSON parts (doesn't deserialize)
- Forgetting temp file cleanup on errors
- Not handling partial upload failures
- Content-Type mismatch between part and expected type

**6. Follow-up questions:**
- How do you send JSON alongside a file in multipart?
- What's the difference between @RequestParam and @RequestPart?

**7. Senior summary:**
"Multipart requests separate parts by boundary, each with its own Content-Type. Use @RequestPart for typed parts (files or JSON), @RequestParam for simple form fields. Spring's MultipartResolver handles parsing and temp file management. Clean up properly on errors."

--------------------------------------------------
### Question 59: How pagination works in Spring Boot?

**1. Definition:**
Spring Data provides `Pageable` parameter binding and `Page<T>` results, automatically handling page number, size, and sorting for database queries.

**2. Why it matters:**
Pagination is essential for performance and UX with large datasets. Proper implementation prevents memory issues and provides consistent API behavior.

**3. Internal working:**
```java
@GetMapping("/products")
public Page<Product> getProducts(
        @PageableDefault(size = 20, sort = "name") Pageable pageable) {
    return productRepository.findAll(pageable);
}

// Request: GET /products?page=0&size=10&sort=price,desc&sort=name,asc
```

Configuration:
```yaml
spring:
  data:
    web:
      pageable:
        default-page-size: 20
        max-page-size: 100
        one-indexed-parameters: false  # page starts at 0
        page-parameter: page
        size-parameter: size
      sort:
        sort-parameter: sort
```

Page response includes:
- `content`: List of items
- `totalElements`: Total count
- `totalPages`: Number of pages
- `number`: Current page (0-indexed)
- `size`: Page size

**4. Real-world example:**
Custom pagination response:
```java
@GetMapping("/orders")
public ResponseEntity<PagedResponse<OrderDto>> getOrders(
        @RequestParam(required = false) String status,
        Pageable pageable) {

    Page<Order> page = orderRepository.findByStatus(status, pageable);

    PagedResponse<OrderDto> response = new PagedResponse<>(
        page.getContent().stream().map(OrderDto::from).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages()
    );

    return ResponseEntity.ok()
        .header("X-Total-Count", String.valueOf(page.getTotalElements()))
        .body(response);
}

public record PagedResponse<T>(
    List<T> data,
    int page,
    int size,
    long total,
    int totalPages
) {}
```

**5. Pitfalls:**
- Not limiting max page size (memory/performance issues)
- COUNT queries on large tables (use keyset pagination)
- Offset pagination performance degradation at high page numbers
- Exposing internal entity directly (use DTOs)

**6. Follow-up questions:**
- What's the difference between offset and keyset pagination?
- How do you handle pagination with complex joins?

**7. Senior summary:**
"Spring Data's Pageable provides automatic page/size/sort binding. Configure max-page-size to prevent abuse. For large datasets, consider keyset pagination over offset due to performance. Always use DTOs in API responses, not entities."

--------------------------------------------------
### Question 60: How sorting works in Spring Boot?

**1. Definition:**
Sorting is handled via `Sort` parameter or as part of `Pageable`, supporting multiple fields with configurable direction (ASC/DESC).

**2. Why it matters:**
Proper sorting implementation affects query performance and user experience. Understanding the mechanisms prevents SQL injection and inefficient queries.

**3. Internal working:**
```java
// Via Pageable
@GetMapping("/products")
public Page<Product> getProducts(Pageable pageable) {
    // Request: ?sort=price,desc&sort=name,asc
    return productRepository.findAll(pageable);
}

// Direct Sort parameter
@GetMapping("/products")
public List<Product> getProducts(Sort sort) {
    return productRepository.findAll(sort);
}

// Programmatic Sort
Sort sort = Sort.by(
    Sort.Order.desc("price"),
    Sort.Order.asc("name").ignoreCase()
);
```

Sort safety:
```java
@GetMapping("/products")
public Page<Product> getProducts(
        @SortDefault.SortDefaults({
            @SortDefault(sort = "price", direction = DESC),
            @SortDefault(sort = "name", direction = ASC)
        }) Pageable pageable) {

    // Whitelist allowed sort fields
    Set<String> allowedFields = Set.of("price", "name", "createdAt");
    pageable.getSort().forEach(order -> {
        if (!allowedFields.contains(order.getProperty())) {
            throw new BadRequestException("Invalid sort field: " + order.getProperty());
        }
    });

    return productRepository.findAll(pageable);
}
```

**4. Real-world example:**
Complex sorting with null handling:
```java
@GetMapping("/users")
public Page<User> getUsers(
        @RequestParam(required = false) String sortBy,
        @RequestParam(defaultValue = "ASC") Sort.Direction direction,
        Pageable pageable) {

    Sort sort = sortBy != null
        ? Sort.by(direction, sortBy).and(Sort.by("id"))  // Stable sort
        : Sort.by("createdAt").descending();

    Pageable sortedPageable = PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sort
    );

    return userRepository.findAll(sortedPageable);
}
```

**5. Pitfalls:**
- Allowing arbitrary sort fields (SQL injection risk)
- Sorting on non-indexed columns (performance)
- Unstable sort causing inconsistent pagination
- Case-sensitive sorting when case-insensitive expected

**6. Follow-up questions:**
- How do you ensure stable sorting for pagination?
- How do you sort by nested properties?

**7. Senior summary:**
"Sort via Pageable URL parameters or explicit Sort objects. Whitelist allowed sort fields for security. Add a secondary sort on unique field (ID) for stable pagination. Ensure sorted columns are indexed for performance."

--------------------------------------------------
### Question 61: How to handle CORS in Spring Boot?

**1. Definition:**
CORS (Cross-Origin Resource Sharing) is configured via `@CrossOrigin`, `WebMvcConfigurer`, or `CorsFilter` to allow browsers to make cross-origin requests to your API.

**2. Why it matters:**
APIs consumed by browser-based clients need proper CORS configuration. Misconfiguration either blocks legitimate requests or creates security vulnerabilities.

**3. Internal working:**
```java
// Method 1: @CrossOrigin on controller
@RestController
@CrossOrigin(origins = "https://example.com")
public class ApiController {
    @CrossOrigin(origins = "https://other.com")  // Override
    @GetMapping("/data")
    public Data getData() { }
}

// Method 2: Global configuration
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("https://example.com", "https://app.example.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}

// Method 3: CorsFilter (for Spring Security)
@Bean
public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://example.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return new CorsFilter(source);
}
```

**4. Real-world example:**
Production CORS with Spring Security:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Environment-specific origins
        config.setAllowedOrigins(List.of(
            "https://app.example.com",
            "https://admin.example.com"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Request-Id"));
        config.setExposedHeaders(List.of("X-Total-Count", "X-Request-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

**5. Pitfalls:**
- Using `allowedOrigins("*")` with `allowCredentials(true)` (forbidden by browsers)
- CORS config not applied due to Spring Security filter order
- Forgetting OPTIONS method for preflight requests
- Not exposing custom headers the client needs

**6. Follow-up questions:**
- What's a preflight request and when does it occur?
- How do you handle CORS with Spring Security?

**7. Senior summary:**
""Configure CORS via WebMvcConfigurer or CorsFilter (for Spring Security). Never use wildcard origins with credentials. Explicitly list allowed origins, methods, and headers. Configure with Spring Security using cors() DSL to ensure proper filter ordering."

---

## DATA + JPA QUESTIONS (62-81) - VERY IMPORTANT

--------------------------------------------------
### Question 62: Difference between JPA and Hibernate?

**1. Definition:**
JPA (Jakarta Persistence API) is a specification defining ORM standards. Hibernate is an implementation of JPA that provides additional features beyond the spec.

**2. Why it matters:**
Understanding this distinction enables portable code (using JPA APIs) while leveraging Hibernate-specific features when needed.

**3. Internal working:**
```
JPA Specification:
- EntityManager
- @Entity, @Table, @Column
- JPQL query language
- Criteria API
- Lifecycle annotations

Hibernate Implementation:
- Session (extends EntityManager)
- SessionFactory
- HQL (superset of JPQL)
- Hibernate-specific annotations (@BatchSize, @Where)
- Second-level caching
- Schema generation
```

Spring Data JPA:
```java
// Uses JPA API through Hibernate
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String name;
}

// Spring Data repository (uses JPA EntityManager internally)
public interface UserRepository extends JpaRepository<User, Long> { }
```

**4. Real-world example:**
Portable code with JPA, Hibernate extensions when needed:
```java
// Portable JPA
@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;
}

// Hibernate-specific optimization
@Entity
@BatchSize(size = 25)  // Hibernate-specific
@Where(clause = "deleted = false")  // Hibernate-specific
public class Product {
    // Batch fetching and soft delete filter
}
```

**5. Pitfalls:**
- Coupling to Hibernate APIs unnecessarily
- Assuming JPA annotations work the same in other providers
- Not understanding when Hibernate extensions are beneficial

**6. Follow-up questions:**
- What Hibernate features aren't in JPA spec?
- How would you switch from Hibernate to EclipseLink?

**7. Senior summary:**
"JPA is the specification, Hibernate is the implementation. Use JPA APIs for portability. Use Hibernate-specific features (@BatchSize, @Fetch, @Where) when needed for optimization. Spring Data JPA abstracts both, using EntityManager under the hood."

--------------------------------------------------
### Question 63: What is Spring Data JPA?

**1. Definition:**
Spring Data JPA is a Spring module that simplifies JPA-based data access by providing repository abstractions, derived queries, and query methods that eliminate boilerplate code.

**2. Why it matters:**
It dramatically reduces data access code—no DAO implementations needed for standard operations. Focus on domain logic, not CRUD boilerplate.

**3. Internal working:**
```java
// Interface only - Spring generates implementation
public interface UserRepository extends JpaRepository<User, Long> {

    // Derived query - parsed from method name
    List<User> findByEmailAndStatus(String email, Status status);

    // Custom JPQL
    @Query("SELECT u FROM User u WHERE u.createdAt > :date")
    List<User> findRecentUsers(@Param("date") LocalDateTime date);

    // Native SQL
    @Query(value = "SELECT * FROM users WHERE email LIKE %?1%", nativeQuery = true)
    List<User> searchByEmail(String pattern);
}
```

Spring Data creates proxy at runtime:
1. `@EnableJpaRepositories` triggers scanning
2. `JpaRepositoryFactoryBean` creates repository implementations
3. `SimpleJpaRepository` provides base CRUD operations
4. Query methods parsed and converted to JPQL/Criteria

**4. Real-world example:**
Complex repository with specifications:
```java
public interface OrderRepository extends JpaRepository<Order, Long>,
        JpaSpecificationExecutor<Order> {

    // Derived query with pagination
    Page<Order> findByCustomerIdAndStatusIn(
        Long customerId, List<Status> statuses, Pageable pageable);

    // Modifying query
    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id IN :ids")
    int updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") Status status);

    // Entity graph for fetch optimization
    @EntityGraph(attributePaths = {"customer", "items"})
    Optional<Order> findWithDetailsById(Long id);
}
```

**5. Pitfalls:**
- Over-relying on derived queries for complex logic
- Not understanding the generated SQL
- Missing @Modifying on update/delete queries
- Forgetting @Transactional for modifying operations

**6. Follow-up questions:**
- How does query derivation work?
- What's the difference between findBy and getBy?

**7. Senior summary:**
"Spring Data JPA generates repository implementations at runtime, eliminating boilerplate. Use derived queries for simple cases, @Query for complex ones. JpaSpecificationExecutor enables dynamic queries. Always verify generated SQL for performance."

--------------------------------------------------
### Question 64: CrudRepository vs JpaRepository?

**1. Definition:**
`CrudRepository` provides basic CRUD operations. `JpaRepository` extends it with JPA-specific features: batching, flushing, pagination, and query by example.

**2. Why it matters:**
Choosing the right interface affects available operations. JpaRepository is typically preferred for JPA applications; CrudRepository for generic Spring Data usage.

**3. Internal working:**
```
Repository (marker)
    └── CrudRepository (basic CRUD)
            └── PagingAndSortingRepository (pagination)
                    └── JpaRepository (JPA-specific)

CrudRepository<T, ID>:
- save(entity)
- saveAll(entities)
- findById(id)
- existsById(id)
- findAll()
- count()
- delete(entity)
- deleteById(id)

JpaRepository<T, ID> adds:
- flush()
- saveAndFlush(entity)
- deleteAllInBatch()
- deleteInBatch(entities)
- findAll(Sort)
- findAll(Pageable)
- findAll(Example)
- getOne(id) / getById(id) / getReferenceById(id)
```

**4. Real-world example:**
```java
// For batch operations, use JpaRepository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // saveAndFlush - immediate persist (needed before ID generation)
    default Product createAndGetId(Product product) {
        return saveAndFlush(product);  // ID available immediately
    }

    // deleteAllInBatch - single DELETE query
    default void bulkDelete(List<Long> ids) {
        deleteAllByIdInBatch(ids);  // Much faster than individual deletes
    }

    // getReferenceById - proxy without SELECT
    default void assignToCategory(Long productId, Long categoryId) {
        Product product = getReferenceById(productId);  // No SELECT
        Category category = categoryRepository.getReferenceById(categoryId);
        product.setCategory(category);
        // Single UPDATE, no SELECT queries
    }
}
```

**5. Pitfalls:**
- Using CrudRepository when batch operations are needed
- `getById/getReferenceById` throws on access, not on call
- Forgetting that deleteInBatch bypasses cascade/lifecycle callbacks

**6. Follow-up questions:**
- When would getReferenceById cause LazyInitializationException?
- What's the difference between delete() and deleteInBatch()?

**7. Senior summary:**
"JpaRepository extends CrudRepository with JPA features: batching, flushing, pagination. Use deleteAllInBatch for bulk deletes (single query), getReferenceById for FK assignments without loading. Most JPA applications should use JpaRepository."

--------------------------------------------------
### Question 65: How transactions work internally?

**1. Definition:**
Spring transactions use AOP proxies to intercept method calls, start transactions via `PlatformTransactionManager`, and commit/rollback based on method outcome.

**2. Why it matters:**
Understanding transaction internals explains proxy limitations, propagation behavior, and isolation level effects—critical for avoiding data inconsistencies.

**3. Internal working:**
```
Method call → Proxy intercepts → TransactionInterceptor →
    → PlatformTransactionManager.getTransaction() →
    → (gets Connection from DataSource) →
    → setAutoCommit(false) →
    → Execute method →
    → commit() or rollback() →
    → Return connection to pool

Key components:
- @Transactional annotation
- TransactionInterceptor (AOP advice)
- PlatformTransactionManager (abstraction)
- DataSourceTransactionManager (JDBC)
- JpaTransactionManager (JPA)
```

Connection handling:
```java
@Transactional
public void processOrder(Order order) {
    // Connection obtained, autocommit=false
    orderRepository.save(order);
    // Same connection reused
    inventoryService.reserve(order);
    // Same connection (propagation REQUIRED)
    // Method ends → commit
}
```

**4. Real-world example:**
Transaction flow debugging:
```java
@Service
@Slf4j
public class OrderService {

    @Transactional
    public void createOrder(OrderRequest request) {
        log.debug("TX active: {}", TransactionSynchronizationManager.isActualTransactionActive());
        log.debug("TX name: {}", TransactionSynchronizationManager.getCurrentTransactionName());

        // Both operations share the same transaction
        Order order = orderRepository.save(new Order(request));
        paymentService.charge(order);  // Same TX if REQUIRED propagation

        // If paymentService throws, both are rolled back
    }
}
```

**5. Pitfalls:**
- Calling @Transactional method from same class (proxy bypass)
- Catching exceptions, preventing rollback
- Long transactions holding connections
- Not understanding connection pooling impact

**6. Follow-up questions:**
- What happens when a @Transactional method calls another @Transactional method?
- How does transaction synchronization work with multiple resources?

**7. Senior summary:**
"Transactions work via AOP proxies—TransactionInterceptor manages lifecycle through PlatformTransactionManager. Connection borrowed from pool, autocommit disabled, same connection used for all operations in transaction. Self-invocation bypasses proxy, breaking transaction handling."

--------------------------------------------------
### Question 66: What is @Transactional?

**1. Definition:**
`@Transactional` is a declarative transaction management annotation that defines transaction boundaries, propagation, isolation, timeout, and rollback rules.

**2. Why it matters:**
Proper transaction configuration ensures data consistency, proper isolation, and resource management. Misconfiguration leads to data corruption or performance issues.

**3. Internal working:**
```java
@Transactional(
    propagation = Propagation.REQUIRED,  // Default
    isolation = Isolation.READ_COMMITTED, // Default varies by DB
    timeout = 30,                         // Seconds
    readOnly = false,                     // Default
    rollbackFor = Exception.class,        // Rollback on checked exceptions
    noRollbackFor = BusinessException.class
)
public void processPayment(Payment payment) {
    // All operations in single transaction
}
```

Placement rules:
```java
@Service
@Transactional(readOnly = true)  // Class-level default
public class OrderService {

    public Order findById(Long id) {
        // Uses class-level: readOnly = true
        return orderRepository.findById(id).orElseThrow();
    }

    @Transactional  // Override: readOnly = false
    public Order create(OrderRequest request) {
        return orderRepository.save(new Order(request));
    }
}
```

**4. Real-world example:**
Service layer transaction patterns:
```java
@Service
@Transactional(readOnly = true)  // Safe default
public class AccountService {

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = accountRepository.findByIdWithLock(fromId);  // Pessimistic lock
        Account to = accountRepository.findByIdWithLock(toId);

        from.debit(amount);
        to.credit(amount);
        // Both saved on commit, or neither (atomicity)
    }

    @Transactional(timeout = 5, rollbackFor = Exception.class)
    public void processWithExternalCall(Long id) {
        // Timeout prevents long transactions
        accountRepository.process(id);
        externalService.notify();  // If this fails, rollback
    }
}
```

**5. Pitfalls:**
- Using on private methods (AOP doesn't work)
- Forgetting rollbackFor for checked exceptions
- readOnly=true with write operations (may silently fail)
- Self-invocation within the same class

**6. Follow-up questions:**
- What's the difference between class-level and method-level @Transactional?
- How do you handle transactions in async methods?

**7. Senior summary:**
"@Transactional defines transaction boundaries with configurable propagation, isolation, timeout, and rollback rules. Apply at service layer, not repository. Use readOnly=true as default for queries. Remember: only works on public methods via proxy calls."

--------------------------------------------------
### Question 67: Transaction propagation types?

**1. Definition:**
Propagation defines how transactions relate when a transactional method calls another transactional method—join existing, create new, or execute without transaction.

**2. Why it matters:**
Propagation choice affects atomicity scope. Wrong propagation can cause unexpected commits/rollbacks or nested transaction complexity.

**3. Internal working:**

| Propagation | Existing TX | No TX | Use Case |
|-------------|-------------|-------|----------|
| REQUIRED | Join | Create | Default, most common |
| REQUIRES_NEW | Suspend, create new | Create | Audit logs, must commit independently |
| NESTED | Create savepoint | Create | Partial rollback |
| MANDATORY | Join | Throw exception | Must be called from TX |
| SUPPORTS | Join | Run without TX | Read methods |
| NOT_SUPPORTED | Suspend | Run without TX | Long-running reads |
| NEVER | Throw exception | Run without TX | Must not be in TX |

```java
@Transactional(propagation = Propagation.REQUIRED)  // Default
public void processOrder(Order order) {
    orderRepository.save(order);  // Same TX

    auditService.log(order);  // See below
}

@Service
public class AuditService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Order order) {
        // New TX - commits even if caller rolls back
        auditRepository.save(new AuditLog(order));
    }
}
```

**4. Real-world example:**
Audit logging that must persist regardless of outcome:
```java
@Service
public class PaymentService {

    @Transactional
    public void processPayment(Payment payment) {
        try {
            // Main transaction
            paymentRepository.save(payment);
            gatewayService.charge(payment);
        } catch (GatewayException e) {
            // Log audit BEFORE rollback
            auditService.logFailure(payment, e);  // REQUIRES_NEW
            throw e;  // Main TX rolls back, audit persists
        }
    }
}

@Service
public class AuditService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(Payment payment, Exception error) {
        // Independent transaction - always commits
        auditRepository.save(new FailureLog(payment, error));
    }
}
```

**5. Pitfalls:**
- REQUIRES_NEW with long transactions (connection exhaustion)
- NESTED not supported by all transaction managers
- Assuming REQUIRES_NEW uses same connection (it doesn't)
- Savepoint rollback with NESTED not working with JPA/Hibernate

**6. Follow-up questions:**
- What happens to nested transaction if outer fails?
- How does REQUIRES_NEW affect connection pool?

**7. Senior summary:**
"REQUIRED is the default—join or create. Use REQUIRES_NEW for operations that must commit independently (audit logs). NESTED creates savepoints for partial rollback. REQUIRES_NEW suspends outer TX and needs another connection—mind the pool."

--------------------------------------------------
### Question 68: Transaction isolation levels?

**1. Definition:**
Isolation levels define how transactions are isolated from concurrent transactions, trading consistency guarantees against concurrency performance.

**2. Why it matters:**
Wrong isolation causes data anomalies (dirty reads, phantom reads) or excessive locking (deadlocks, performance issues). Choose based on consistency requirements.

**3. Internal working:**

| Isolation | Dirty Read | Non-Repeatable Read | Phantom Read |
|-----------|------------|---------------------|--------------|
| READ_UNCOMMITTED | Possible | Possible | Possible |
| READ_COMMITTED | Prevented | Possible | Possible |
| REPEATABLE_READ | Prevented | Prevented | Possible |
| SERIALIZABLE | Prevented | Prevented | Prevented |

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public BigDecimal getAccountBalance(Long accountId) {
    // Highest isolation - full consistency
}

@Transactional(isolation = Isolation.READ_COMMITTED)
public List<Order> getOrders() {
    // Default for most DBs - good for reads
}
```

Database defaults:
- PostgreSQL: READ_COMMITTED
- MySQL InnoDB: REPEATABLE_READ
- Oracle: READ_COMMITTED
- SQL Server: READ_COMMITTED

**4. Real-world example:**
Financial transfer requiring serializable:
```java
@Service
public class AccountService {

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        // SERIALIZABLE prevents phantom reads
        // No concurrent transfer can see intermediate state

        Account from = accountRepository.findById(fromId).orElseThrow();
        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        Account to = accountRepository.findById(toId).orElseThrow();
        from.debit(amount);
        to.credit(amount);
        // Both accounts saved atomically
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<Transaction> getTransactionHistory(Long accountId) {
        // Lower isolation is fine for read-only reporting
        return transactionRepository.findByAccountId(accountId);
    }
}
```

**5. Pitfalls:**
- Using SERIALIZABLE everywhere (major performance hit)
- Not considering database defaults
- Mixing isolation levels incorrectly
- Assuming application-level locking replaces database isolation

**6. Follow-up questions:**
- How does isolation level interact with optimistic locking?
- What's the performance impact of SERIALIZABLE?

**7. Senior summary:**
"Isolation levels control visibility of concurrent changes. READ_COMMITTED is default for most DBs—prevents dirty reads. Use SERIALIZABLE for financial operations. Higher isolation = more locking = lower throughput. Consider optimistic locking as alternative."

--------------------------------------------------
### Question 69: Lazy vs Eager loading?

**1. Definition:**
Lazy loading defers relationship fetching until accessed. Eager loading fetches relationships immediately with the parent entity.

**2. Why it matters:**
Loading strategy dramatically affects performance. Eager causes over-fetching; lazy causes N+1 queries. Neither is universally correct—choose per use case.

**3. Internal working:**
```java
@Entity
public class Order {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)  // Loaded with order
    private Customer customer;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)  // Default
    private List<OrderItem> items;  // Proxy until accessed
}
```

JPA defaults:
- `@OneToMany`, `@ManyToMany`: LAZY
- `@ManyToOne`, `@OneToOne`: EAGER

Lazy loading mechanics:
```java
Order order = orderRepository.findById(1L).get();
// SQL: SELECT * FROM orders WHERE id = 1

order.getItems().size();  // Triggers lazy load
// SQL: SELECT * FROM order_items WHERE order_id = 1
```

**4. Real-world example:**
Strategic fetch configuration:
```java
@Entity
public class Product {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // Override default EAGER
    private Category category;  // Don't always need this

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<Review> reviews;  // Definitely don't load by default
}

// When you need related data, use JOIN FETCH
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    @EntityGraph(attributePaths = {"category", "reviews"})
    Optional<Product> findWithDetailsById(Long id);
}
```

**5. Pitfalls:**
- LazyInitializationException after session closes
- Eager loading causing Cartesian product
- N+1 queries with lazy loading in loops
- Using EAGER to "fix" LazyInitializationException (wrong solution)

**6. Follow-up questions:**
- How do you avoid LazyInitializationException?
- What's the Cartesian product problem with multiple eager collections?

**7. Senior summary:**
"Always use LAZY as default—fetch when needed with JOIN FETCH or @EntityGraph. EAGER causes over-fetching and Cartesian products. Handle LazyInitializationException by fetching within transaction, not by switching to EAGER. Design queries around use cases."

--------------------------------------------------
### Question 70: What is N+1 problem?

**1. Definition:**
N+1 is a performance anti-pattern where fetching N entities results in 1 query for parents plus N queries for their relationships, instead of a single join query.

**2. Why it matters:**
N+1 is the most common JPA performance issue. With 100 entities, you get 101 queries instead of 1—devastating for performance.

**3. Internal working:**
```java
// N+1 problem example
List<Order> orders = orderRepository.findAll();  // 1 query
for (Order order : orders) {
    // Each access triggers a new query = N queries
    log.info("Customer: {}", order.getCustomer().getName());
}
// Total: 1 + N queries!

// SQL:
// SELECT * FROM orders  -- 1 query
// SELECT * FROM customers WHERE id = 1  -- N queries
// SELECT * FROM customers WHERE id = 2
// SELECT * FROM customers WHERE id = 3
// ...
```

**4. Real-world example:**
Solutions to N+1:
```java
// Solution 1: JOIN FETCH in JPQL
@Query("SELECT o FROM Order o JOIN FETCH o.customer")
List<Order> findAllWithCustomers();
// Single query: SELECT o.*, c.* FROM orders o JOIN customers c

// Solution 2: @EntityGraph
@EntityGraph(attributePaths = {"customer", "items"})
List<Order> findAll();

// Solution 3: @BatchSize on entity
@Entity
public class Order {
    @ManyToOne(fetch = FetchType.LAZY)
    @BatchSize(size = 25)  // Hibernate fetches 25 at a time
    private Customer customer;
}

// Solution 4: Default batch fetch size
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 25

// SQL with batch size:
// SELECT * FROM orders
// SELECT * FROM customers WHERE id IN (1, 2, 3, ... 25)
// Instead of 25 separate queries
```

**5. Pitfalls:**
- JOIN FETCH with pagination (HHH000104 warning)
- Multiple JOIN FETCHes causing Cartesian product
- Not monitoring actual SQL in development
- Using @BatchSize as only solution (JOIN FETCH is still better)

**6. Follow-up questions:**
- How do you detect N+1 queries in your application?
- What's the Cartesian product problem with multiple JOIN FETCHes?

**7. Senior summary:**
"N+1 is 1 query for parents + N queries for relationships. Fix with JOIN FETCH for specific queries, @EntityGraph for repository methods, or global batch-fetch-size. Monitor SQL in development using spring.jpa.show-sql or P6Spy. JOIN FETCH is most efficient for known use cases."

--------------------------------------------------
### Question 71: How to solve N+1 problem?

**1. Definition:**
Solutions include JOIN FETCH in JPQL, @EntityGraph annotations, Hibernate @BatchSize, or global batch fetch configuration—each with different trade-offs.

**2. Why it matters:**
Choosing the right solution depends on use case—ad-hoc queries, consistent patterns, or global optimization. Wrong choice leads to over-fetching or complexity.

**3. Internal working:**

```java
// 1. JOIN FETCH - explicit, optimal for specific query
@Query("SELECT o FROM Order o " +
       "JOIN FETCH o.customer " +
       "JOIN FETCH o.items " +
       "WHERE o.status = :status")
List<Order> findByStatusWithDetails(@Param("status") Status status);

// 2. @EntityGraph - reusable, declarative
@NamedEntityGraph(
    name = "Order.withDetails",
    attributeNodes = {
        @NamedAttributeNode("customer"),
        @NamedAttributeNode(value = "items", subgraph = "items-subgraph")
    },
    subgraphs = @NamedSubgraph(
        name = "items-subgraph",
        attributeNodes = @NamedAttributeNode("product")
    )
)
@Entity
public class Order { }

@EntityGraph("Order.withDetails")
List<Order> findByStatus(Status status);

// 3. @BatchSize - Hibernate specific, good default
@Entity
@BatchSize(size = 25)
public class Customer { }

// 4. Global batch fetch
spring.jpa.properties.hibernate.default_batch_fetch_size=25
```

**4. Real-world example:**
Different solutions for different scenarios:
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // List view - only need customer name
    @Query("SELECT o FROM Order o JOIN FETCH o.customer")
    List<Order> findAllForListView();

    // Detail view - need everything
    @EntityGraph(attributePaths = {"customer", "items", "items.product", "payments"})
    Optional<Order> findDetailedById(Long id);

    // Report - specific fields only (DTO projection, no entities)
    @Query("SELECT new com.app.OrderSummary(o.id, o.total, c.name) " +
           "FROM Order o JOIN o.customer c")
    List<OrderSummary> findSummaries();  // No lazy loading issues!
}
```

**5. Pitfalls:**
- JOIN FETCH on collections with pagination (loads all, paginates in memory)
- Multiple collection JOIN FETCHes (Cartesian product)
- @BatchSize requiring hibernate-specific dependency
- DTO projection requiring constructor matching

**6. Follow-up questions:**
- How do you handle JOIN FETCH with pagination?
- When would you use DTO projection instead of entity fetching?

**7. Senior summary:**
"Use JOIN FETCH for specific queries, @EntityGraph for repository methods, batch-fetch-size for global optimization, DTO projection for read-only views. Avoid JOIN FETCH with pagination on collections. Multiple collections require separate queries or batch fetching."

--------------------------------------------------
### Question 72: JPQL vs Native Query?

**1. Definition:**
JPQL (Jakarta Persistence Query Language) is object-oriented, portable, works with entity mappings. Native queries are raw SQL, database-specific, needed for DB-specific features.

**2. Why it matters:**
JPQL provides type safety and portability. Native queries offer full SQL power. Choosing correctly affects maintainability and performance.

**3. Internal working:**
```java
// JPQL - entity-oriented
@Query("SELECT o FROM Order o WHERE o.customer.name = :name")
List<Order> findByCustomerName(@Param("name") String name);

// Native - SQL
@Query(value = "SELECT * FROM orders WHERE customer_id IN " +
       "(SELECT id FROM customers WHERE name = ?1)",
       nativeQuery = true)
List<Order> findByCustomerNameNative(String name);

// Native for DB-specific features
@Query(value = "SELECT * FROM orders WHERE data @> :jsonPath::jsonb",
       nativeQuery = true)
List<Order> findByJsonPath(@Param("jsonPath") String jsonPath);  // PostgreSQL jsonb
```

| Aspect | JPQL | Native Query |
|--------|------|--------------|
| Syntax | Entity/property names | Table/column names |
| Portability | Yes | No |
| Type safety | Higher | Lower |
| DB features | Limited | Full |
| Performance | Good | Can be better |

**4. Real-world example:**
When to use each:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {

    // JPQL - standard queries, type-safe
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max")
    List<Product> findByPriceRange(
        @Param("min") BigDecimal min,
        @Param("max") BigDecimal max);

    // JPQL - JOIN FETCH works with entity model
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.reviews " +
           "WHERE p.category.name = :category")
    List<Product> findByCategoryWithReviews(@Param("category") String category);

    // Native - complex SQL, window functions
    @Query(value = """
        SELECT * FROM (
            SELECT *, RANK() OVER (PARTITION BY category_id ORDER BY sales DESC) as rank
            FROM products
        ) ranked WHERE rank <= 10
        """, nativeQuery = true)
    List<Product> findTop10ByCategory();

    // Native - full-text search (PostgreSQL)
    @Query(value = "SELECT * FROM products " +
           "WHERE to_tsvector('english', name || ' ' || description) " +
           "@@ plainto_tsquery('english', :search)",
           nativeQuery = true)
    List<Product> fullTextSearch(@Param("search") String search);
}
```

**5. Pitfalls:**
- Native queries not mapping to entity correctly
- Forgetting nativeQuery = true
- Using native for simple queries (unnecessary complexity)
- JPQL not supporting all SQL features (window functions)

**6. Follow-up questions:**
- How do you map native query results to DTOs?
- Can you use native queries with pagination?

**7. Senior summary:**
"JPQL for entity-oriented, portable queries—use for most cases. Native SQL for DB-specific features (JSON, full-text, window functions). Native queries still map to entities but using column names. Prefer JPQL unless you need SQL-specific capabilities."

--------------------------------------------------
### Question 73: How @Query works?

**1. Definition:**
`@Query` defines custom JPQL or native SQL queries on repository methods, overriding the default query derivation with explicit query logic.

**2. Why it matters:**
Complex queries can't be expressed as derived query methods. @Query provides explicit control while maintaining repository abstraction.

**3. Internal working:**
```java
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Positional parameters
    @Query("SELECT o FROM Order o WHERE o.status = ?1 AND o.total > ?2")
    List<Order> findByStatusAndMinTotal(Status status, BigDecimal minTotal);

    // Named parameters
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt > :date")
    List<Order> findRecentByStatus(
        @Param("status") Status status,
        @Param("date") LocalDateTime date);

    // Modifying queries (UPDATE, DELETE)
    @Modifying
    @Query("UPDATE Order o SET o.status = :newStatus WHERE o.status = :oldStatus")
    int updateStatus(@Param("oldStatus") Status old, @Param("newStatus") Status newStatus);

    // With pagination
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId")
    Page<Order> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    // DTO projection
    @Query("SELECT new com.app.OrderSummary(o.id, o.total, o.status) FROM Order o")
    List<OrderSummary> findAllSummaries();
}
```

**4. Real-world example:**
Advanced query patterns:
```java
public interface UserRepository extends JpaRepository<User, Long> {

    // SpEL expression
    @Query("SELECT u FROM #{#entityName} u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    // Collection parameter
    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findByIds(@Param("ids") Collection<Long> ids);

    // Complex with subquery
    @Query("SELECT u FROM User u WHERE u.orderCount > " +
           "(SELECT AVG(u2.orderCount) FROM User u2)")
    List<User> findAboveAverageOrderCount();

    // Native with result set mapping
    @Query(value = "SELECT u.id, u.name, COUNT(o.id) as order_count " +
                   "FROM users u LEFT JOIN orders o ON u.id = o.user_id " +
                   "GROUP BY u.id, u.name",
           nativeQuery = true)
    List<Object[]> findUsersWithOrderCounts();  // Manual mapping needed
}
```

**5. Pitfalls:**
- Forgetting @Modifying on UPDATE/DELETE queries
- Missing @Transactional for modifying queries
- SpEL expressions not working without proper setup
- Native query result mapping complexity

**6. Follow-up questions:**
- How do you use dynamic queries with @Query?
- What's the difference between positional and named parameters?

**7. Senior summary:**
"@Query enables explicit JPQL/SQL on repository methods. Use named parameters for readability. @Modifying required for UPDATE/DELETE. Combine with Pageable for pagination. For dynamic queries, consider Specifications or Querydsl instead."

--------------------------------------------------
### Question 74: Pagination with JPA?

**1. Definition:**
JPA pagination uses `Pageable` parameter and `Page<T>` return type, generating LIMIT/OFFSET SQL and count queries for total elements.

**2. Why it matters:**
Efficient pagination is crucial for performance. Understanding the underlying queries helps optimize for large datasets.

**3. Internal working:**
```java
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Automatic pagination
    Page<Product> findByCategory(String category, Pageable pageable);

    // Slice - no count query (more efficient for "load more")
    Slice<Product> findByBrand(String brand, Pageable pageable);

    // With @Query
    @Query("SELECT p FROM Product p WHERE p.price > :minPrice")
    Page<Product> findExpensiveProducts(@Param("minPrice") BigDecimal min, Pageable pageable);
}

// Usage
Pageable pageable = PageRequest.of(0, 20, Sort.by("price").descending());
Page<Product> page = productRepository.findByCategory("Electronics", pageable);

page.getContent();       // List<Product>
page.getTotalElements(); // Total count
page.getTotalPages();    // Calculated pages
page.hasNext();          // More pages?
```

Generated SQL:
```sql
-- Main query
SELECT * FROM products WHERE category = ? ORDER BY price DESC LIMIT 20 OFFSET 0

-- Count query (for Page, not Slice)
SELECT COUNT(*) FROM products WHERE category = ?
```

**4. Real-world example:**
Optimized pagination for large tables:
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Custom count query (optimized)
    @Query(
        value = "SELECT o FROM Order o WHERE o.status = :status",
        countQuery = "SELECT COUNT(o.id) FROM Order o WHERE o.status = :status"
    )
    Page<Order> findByStatus(@Param("status") Status status, Pageable pageable);

    // Keyset pagination for very large datasets
    @Query("SELECT o FROM Order o WHERE o.id > :lastId ORDER BY o.id")
    Slice<Order> findNextPage(@Param("lastId") Long lastId, Pageable pageable);

    // Projection to avoid loading full entities
    @Query("SELECT new com.app.OrderSummary(o.id, o.status, o.total) " +
           "FROM Order o WHERE o.customerId = :customerId")
    Page<OrderSummary> findSummariesByCustomer(
        @Param("customerId") Long customerId, Pageable pageable);
}
```

**5. Pitfalls:**
- Count queries on large tables (slow)
- OFFSET pagination performance degradation on high pages
- JOIN FETCH with pagination (in-memory pagination warning)
- Not using Slice when count isn't needed

**6. Follow-up questions:**
- What's the difference between Page and Slice?
- How do you implement keyset pagination?

**7. Senior summary:**
"Page includes count query, Slice doesn't—use Slice for 'load more' UX. Custom countQuery optimizes count performance. For very large datasets, use keyset pagination (WHERE id > lastId) instead of OFFSET. Projection reduces data transfer."

--------------------------------------------------
### Question 75: Optimistic vs Pessimistic locking?

**1. Definition:**
Optimistic locking uses version checking at commit time, assuming conflicts are rare. Pessimistic locking uses database locks, preventing concurrent access.

**2. Why it matters:**
Wrong locking strategy either causes lost updates (optimistic without retry) or deadlocks/throughput issues (pessimistic overuse).

**3. Internal working:**
```java
// Optimistic locking
@Entity
public class Product {
    @Id
    private Long id;

    @Version  // Automatic version check on update
    private Long version;

    private BigDecimal price;
}

// Pessimistic locking
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
}
```

| Aspect | Optimistic | Pessimistic |
|--------|------------|-------------|
| Lock time | At commit | At read |
| Conflicts | Detected at commit | Prevented |
| Performance | Better for low contention | Better for high contention |
| Deadlocks | Not possible | Possible |
| Retry needed | Yes | No |

**4. Real-world example:**
Implementing both patterns:
```java
@Service
public class InventoryService {

    // Optimistic - most operations
    @Transactional
    @Retryable(value = OptimisticLockException.class, maxAttempts = 3)
    public void updateStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId).orElseThrow();
        product.adjustStock(quantity);
        // If version changed, OptimisticLockException thrown
    }

    // Pessimistic - critical financial operations
    @Transactional
    public void reserveStock(Long productId, int quantity) {
        // SELECT ... FOR UPDATE
        Product product = productRepository.findByIdWithLock(productId);
        if (product.getStock() < quantity) {
            throw new InsufficientStockException();
        }
        product.setStock(product.getStock() - quantity);
        // Lock released on commit
    }
}

// Lock modes
@Lock(LockModeType.PESSIMISTIC_READ)   // Shared lock
@Lock(LockModeType.PESSIMISTIC_WRITE)  // Exclusive lock
@Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)  // Exclusive + version increment
```

**5. Pitfalls:**
- OptimisticLockException without retry logic
- Pessimistic locks held too long (deadlocks)
- Mixing lock types incorrectly
- Forgetting @Version is checked at flush, not at read

**6. Follow-up questions:**
- How do you implement retry for optimistic locking?
- When would you use PESSIMISTIC_FORCE_INCREMENT?

**7. Senior summary:**
"Optimistic uses @Version for conflict detection at commit—needs retry logic. Pessimistic uses SELECT FOR UPDATE—prevents conflicts but risks deadlocks. Use optimistic by default, pessimistic for high-contention critical operations like inventory or account balance."

--------------------------------------------------
### Question 76: What is @Version?

**1. Definition:**
`@Version` marks a field for optimistic locking—JPA automatically checks this value on update and throws `OptimisticLockException` if it changed since reading.

**2. Why it matters:**
@Version is the simplest way to prevent lost updates in concurrent scenarios. Understanding its behavior is essential for data integrity.

**3. Internal working:**
```java
@Entity
public class Order {
    @Id
    private Long id;

    @Version
    private Long version;  // Or Integer, Timestamp

    private Status status;
}
```

Update flow:
```sql
-- Read
SELECT id, version, status FROM orders WHERE id = 1
-- Returns: version = 5

-- Update (includes version check)
UPDATE orders SET status = 'COMPLETED', version = 6
WHERE id = 1 AND version = 5

-- If 0 rows updated → OptimisticLockException
```

Supported types:
- `int`/`Integer`
- `long`/`Long`
- `short`/`Short`
- `Timestamp` (less reliable due to precision)

**4. Real-world example:**
Handling version conflicts:
```java
@RestController
public class OrderController {

    @PutMapping("/orders/{id}")
    public ResponseEntity<?> updateOrder(
            @PathVariable Long id,
            @RequestBody OrderUpdateRequest request) {
        try {
            Order updated = orderService.update(id, request);
            return ResponseEntity.ok(updated);
        } catch (OptimisticLockException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONCURRENT_UPDATE",
                    "Order was modified by another user. Please refresh and retry."));
        }
    }
}

// Client can send version to detect conflicts early
public record OrderUpdateRequest(
    Long version,  // Client's known version
    Status status,
    String notes
) {}

@Service
public class OrderService {
    @Transactional
    public Order update(Long id, OrderUpdateRequest request) {
        Order order = orderRepository.findById(id).orElseThrow();

        // Early detection
        if (!order.getVersion().equals(request.version())) {
            throw new StaleDataException("Order has been modified");
        }

        order.setStatus(request.status());
        return orderRepository.save(order);
    }
}
```

**5. Pitfalls:**
- Not handling OptimisticLockException
- Using Timestamp version (clock skew issues)
- Expecting version check on read (it's at flush/commit)
- Detached entities losing version info

**6. Follow-up questions:**
- What happens if you manually modify the version field?
- How does @Version work with merge() vs save()?

**7. Senior summary:**
"@Version enables automatic optimistic locking—JPA includes version in WHERE clause on updates. Throws OptimisticLockException on conflict. Use Long for simplicity. Always handle the exception with user-friendly message and retry option."

--------------------------------------------------
### Question 77: Entity lifecycle states?

**1. Definition:**
JPA entities exist in four states: New (transient), Managed (persistent), Detached, and Removed—determining how the persistence context tracks changes.

**2. Why it matters:**
Understanding state transitions explains when changes are persisted, why LazyInitializationException occurs, and how merge() vs persist() differ.

**3. Internal working:**
```
                    persist()
    NEW (Transient) ─────────> MANAGED (Persistent)
           │                        │  ▲
           │                  flush │  │ find/query
           │                        ▼  │
           │              [Database Sync]
           │                        │
           │            detach/clear/close
           │                        │
           ▼                        ▼
        [GC]                   DETACHED
           ▲                        │
           │              merge()   │
           │                        ▼
           │                   MANAGED
           │                        │
           │                 remove()
           │                        │
           │                        ▼
           └──────────────────  REMOVED
                     flush()        │
                                    ▼
                            [Deleted from DB]
```

```java
// NEW - not associated with persistence context
Order order = new Order();  // NEW

// MANAGED - tracked for changes
Order saved = entityManager.persist(order);  // MANAGED
Order found = entityManager.find(Order.class, 1L);  // MANAGED

// DETACHED - no longer tracked
entityManager.detach(order);  // DETACHED
entityManager.clear();  // All entities DETACHED

// REMOVED - scheduled for deletion
entityManager.remove(order);  // REMOVED
```

**4. Real-world example:**
State transitions in practice:
```java
@Service
public class OrderService {

    @Transactional
    public Order createOrder(OrderRequest request) {
        Order order = new Order(request);  // NEW

        orderRepository.save(order);  // MANAGED
        // Changes are tracked - dirty checking

        order.setStatus(Status.PROCESSING);  // Change tracked
        // No explicit save needed!

        return order;  // Still MANAGED until TX ends
    }  // TX commits → flush → order saved with PROCESSING status

    public Order getAndModify(Long id) {
        Order order = orderRepository.findById(id).get();  // MANAGED
        // TX ends here (no @Transactional)

        order.setStatus(Status.SHIPPED);  // DETACHED - change NOT tracked!
        orderRepository.save(order);  // merge() called internally
        // Now changes are persisted
    }
}
```

**5. Pitfalls:**
- Modifying detached entities expecting auto-save
- LazyInitializationException on detached entity relationships
- Calling persist() on detached entity (exception)
- Not understanding dirty checking only works on managed entities

**6. Follow-up questions:**
- What's the difference between persist() and merge()?
- How does dirty checking work?

**7. Senior summary:**
"Entities are New (transient), Managed (persistent/tracked), Detached, or Removed. Dirty checking only works on Managed entities. After transaction ends, entities become Detached. Use merge() for detached entities, persist() for new ones."

--------------------------------------------------
### Question 78: What is dirty checking?

**1. Definition:**
Dirty checking is JPA's mechanism to automatically detect changes to managed entities and generate UPDATE statements at flush time without explicit save calls.

**2. Why it matters:**
It eliminates explicit update calls for managed entities but can cause unexpected updates if not understood. Understanding it prevents both missed updates and unexpected ones.

**3. Internal working:**
```java
@Transactional
public void updateOrder(Long orderId, Status newStatus) {
    Order order = orderRepository.findById(orderId).get();  // Snapshot saved

    order.setStatus(newStatus);  // Entity modified

    // No save() call needed!
}  // TX commits → flush → diff detected → UPDATE generated
```

How it works:
1. When entity loaded, Hibernate stores a snapshot of original values
2. At flush time, compares current state with snapshot
3. If different, generates UPDATE for changed columns
4. `@DynamicUpdate` limits UPDATE to only changed columns

```java
@Entity
@DynamicUpdate  // Only update changed columns
public class Order {
    // Without: UPDATE orders SET status=?, total=?, ... WHERE id=?
    // With: UPDATE orders SET status=? WHERE id=?
}
```

**4. Real-world example:**
Intentional vs unintentional dirty checking:
```java
@Service
public class ProductService {

    // Intentional - modify and auto-save
    @Transactional
    public void updatePrice(Long productId, BigDecimal newPrice) {
        Product product = productRepository.findById(productId).get();
        product.setPrice(newPrice);  // Will be saved automatically
    }

    // Unintentional - be careful!
    @Transactional
    public ProductDto getProductWithCalculation(Long id) {
        Product product = productRepository.findById(id).get();

        // Oops! This modifies the entity
        product.setViewCount(product.getViewCount() + 1);

        // Even though we only wanted to return data,
        // an UPDATE will be issued!

        return ProductDto.from(product);
    }

    // Solution: Read-only transaction
    @Transactional(readOnly = true)
    public ProductDto getProductReadOnly(Long id) {
        Product product = productRepository.findById(id).get();
        product.setViewCount(product.getViewCount() + 1);  // Ignored!
        return ProductDto.from(product);
    }
}
```

**5. Pitfalls:**
- Unintentional modifications in read operations
- Performance impact of snapshot comparison on large entities
- Assuming all changes are tracked (only managed entities)
- Not using readOnly=true for pure read operations

**6. Follow-up questions:**
- How does @DynamicUpdate affect dirty checking?
- When does flush happen in a transaction?

**7. Senior summary:**
"Dirty checking compares entity state at flush with original snapshot, auto-generating UPDATEs. Use @Transactional(readOnly=true) for reads to disable it. @DynamicUpdate limits UPDATE to changed columns only. Be careful—any modification to managed entity persists!"

--------------------------------------------------
### Question 79: Difference between save() and saveAndFlush()?

**1. Definition:**
`save()` persists the entity but defers SQL execution until flush. `saveAndFlush()` immediately synchronizes with the database, executing SQL right away.

**2. Why it matters:**
Understanding the difference is critical for operations requiring immediate ID generation, sequence access, or when you need to ensure data is in the database before continuing.

**3. Internal working:**
```java
// save() - deferred write
Order order = new Order();
orderRepository.save(order);
// Entity is managed, but SQL may not be executed yet
// SQL executed at: flush, commit, or before query

// saveAndFlush() - immediate write
Order order = new Order();
Order saved = orderRepository.saveAndFlush(order);
// SQL executed immediately
// saved.getId() is guaranteed to be populated
```

When flush happens automatically:
1. Transaction commit
2. Before JPQL/native query execution
3. Manual `entityManager.flush()` call
4. `saveAndFlush()` / `flush()` repository methods

**4. Real-world example:**
When saveAndFlush is needed:
```java
@Service
public class OrderService {

    // Need ID immediately for external service
    @Transactional
    public void createAndNotify(OrderRequest request) {
        Order order = new Order(request);
        Order saved = orderRepository.saveAndFlush(order);

        // ID is guaranteed to exist now
        notificationService.sendOrderCreated(saved.getId());
    }

    // Batch processing - flush periodically
    @Transactional
    public void importProducts(List<ProductData> products) {
        for (int i = 0; i < products.size(); i++) {
            productRepository.save(new Product(products.get(i)));

            if (i % 50 == 0) {
                entityManager.flush();
                entityManager.clear();  // Prevent memory issues
            }
        }
    }

    // Trigger database constraint check immediately
    @Transactional
    public void createUnique(String email) {
        User user = new User(email);
        try {
            userRepository.saveAndFlush(user);  // Unique constraint checked NOW
        } catch (DataIntegrityViolationException e) {
            throw new EmailExistsException(email);
        }
    }
}
```

**5. Pitfalls:**
- Using saveAndFlush everywhere (unnecessary overhead)
- Expecting save() to fail immediately on constraint violation
- Forgetting that flush doesn't commit
- Memory issues with large batches without periodic flush/clear

**6. Follow-up questions:**
- When is save() sufficient vs saveAndFlush() required?
- What's the relationship between flush and commit?

**7. Senior summary:**
"save() defers SQL until flush/commit; saveAndFlush() executes immediately. Use saveAndFlush when you need the generated ID immediately, want constraint checking before continuing, or need to synchronize with external systems. flush != commit—transaction can still rollback."

--------------------------------------------------
### Question 80: How auditing works in JPA?

**1. Definition:**
JPA auditing automatically populates created/modified timestamps and user information using `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy` annotations.

**2. Why it matters:**
Audit fields are required for almost every production entity—knowing who changed what and when. Automatic auditing eliminates manual code and ensures consistency.

**3. Internal working:**
```java
// Enable auditing
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getName);
    }
}

// Base auditable entity
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String modifiedBy;
}

// Entities extend base
@Entity
public class Order extends BaseEntity {
    @Id @GeneratedValue
    private Long id;
    private Status status;
}
```

**4. Real-world example:**
Advanced auditing with custom resolver:
```java
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
public class AuditConfig {

    @Bean
    public DateTimeProvider dateTimeProvider(Clock clock) {
        return () -> Optional.of(LocalDateTime.now(clock));
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            // System operations
            if (isSystemContext()) {
                return Optional.of("SYSTEM");
            }

            // Batch jobs
            if (isBatchJob()) {
                return Optional.of("BATCH:" + getBatchJobName());
            }

            // Regular user
            return getCurrentUser();
        };
    }
}

// Audit history table (Hibernate Envers)
@Entity
@Audited  // Enables revision history
public class Product extends BaseEntity {
    private String name;
    private BigDecimal price;
}

// Query audit history
AuditReader reader = AuditReaderFactory.get(entityManager);
List<Number> revisions = reader.getRevisions(Product.class, productId);
Product oldVersion = reader.find(Product.class, productId, revisionNumber);
```

**5. Pitfalls:**
- Forgetting @EntityListeners on entity
- AuditorAware returning empty for system/batch operations
- Not making createdAt/createdBy updatable=false
- Auditing not working without @EnableJpaAuditing

**6. Follow-up questions:**
- How do you audit changes to entity content, not just metadata?
- How does Hibernate Envers differ from Spring Data auditing?

**7. Senior summary:**
"Spring Data auditing auto-populates audit fields via @EntityListeners. @CreatedDate/@LastModifiedDate for timestamps, @CreatedBy/@LastModifiedBy for users (via AuditorAware). Extend a @MappedSuperclass for consistency. For full change history, use Hibernate Envers."

--------------------------------------------------
### Question 81: How to handle large data efficiently?

**1. Definition:**
Large data handling uses techniques like pagination, streaming, batch processing, stateless sessions, and native queries to prevent memory exhaustion and optimize performance.

**2. Why it matters:**
Loading 1 million records into memory crashes applications. Proper techniques enable processing any data volume while maintaining reasonable memory and performance.

**3. Internal working:**
```java
// WRONG - loads all into memory
List<Order> allOrders = orderRepository.findAll();  // OOM!

// RIGHT - pagination
Page<Order> page = orderRepository.findAll(PageRequest.of(0, 1000));
while (page.hasNext()) {
    process(page.getContent());
    page = orderRepository.findAll(page.nextPageable());
}

// RIGHT - streaming (requires transaction)
@Transactional(readOnly = true)
public void processLargeData() {
    try (Stream<Order> stream = orderRepository.streamAll()) {
        stream.forEach(this::process);
    }
}

// RIGHT - batch insert with clear
@Transactional
public void bulkInsert(List<ProductData> data) {
    int batchSize = 50;
    for (int i = 0; i < data.size(); i++) {
        entityManager.persist(new Product(data.get(i)));

        if (i > 0 && i % batchSize == 0) {
            entityManager.flush();
            entityManager.clear();  // Release memory
        }
    }
}
```

**4. Real-world example:**
Production data export:
```java
@Service
public class DataExportService {

    // Stream to file without loading all in memory
    @Transactional(readOnly = true)
    public void exportToFile(Path outputPath) {
        try (Stream<Order> stream = orderRepository.streamAllOrders();
             BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            stream
                .map(this::toCsv)
                .forEach(line -> writeLine(writer, line));
        }
    }

    // Batch processing with stateless session (Hibernate)
    public void processBatch() {
        StatelessSession session = sessionFactory.openStatelessSession();
        Transaction tx = session.beginTransaction();

        ScrollableResults results = session
            .createQuery("FROM Order WHERE status = :status", Order.class)
            .setParameter("status", Status.PENDING)
            .setFetchSize(1000)
            .scroll(ScrollMode.FORWARD_ONLY);

        int count = 0;
        while (results.next()) {
            Order order = (Order) results.get(0);
            process(order);
            session.update(order);

            if (++count % 1000 == 0) {
                tx.commit();
                tx = session.beginTransaction();
            }
        }

        tx.commit();
        session.close();
    }
}

// Repository with stream support
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o")
    @QueryHints(@QueryHint(name = HINT_FETCH_SIZE, value = "1000"))
    Stream<Order> streamAllOrders();
}
```

**5. Pitfalls:**
- Streaming without transaction (connection closed)
- Not closing Stream (resource leak)
- Batch inserts without hibernate.jdbc.batch_size config
- forgetting to clear EntityManager in batch operations

**6. Follow-up questions:**
- What's the difference between Stream and ScrollableResults?
- How do you configure Hibernate batch insert size?

**7. Senior summary:**
""For large data: paginate for UI, stream for processing, batch with flush/clear for inserts. Enable JDBC batching via hibernate.jdbc.batch_size. StatelessSession bypasses persistence context for true streaming. Always close streams and monitor memory."

---

## SECURITY QUESTIONS (82-101) - 5+ YOE KILLER QUESTIONS

--------------------------------------------------
### Question 82: What is Spring Security?

**1. Definition:**
Spring Security is a comprehensive security framework providing authentication, authorization, protection against common exploits (CSRF, session fixation), and integration with various authentication mechanisms.

**2. Why it matters:**
Security is non-negotiable in production. Spring Security provides battle-tested implementations of complex security patterns that would be error-prone to implement manually.

**3. Internal working:**
```
Request → FilterChain → SecurityFilterChain →
    [CsrfFilter → AuthenticationFilter → AuthorizationFilter → ...]
    → Controller

Key components:
- SecurityFilterChain: Configures filter chain
- AuthenticationManager: Verifies credentials
- UserDetailsService: Loads user data
- SecurityContext: Holds authentication
- AccessDecisionManager: Authorization decisions
```

Spring Boot auto-configuration:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
            );
        return http.build();
    }
}
```

**4. Real-world example:**
Multi-layer security configuration:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())  // Stateless API
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults());
        return http.build();
    }
}
```

**5. Pitfalls:**
- Disabling CSRF without understanding implications
- Over-permissive security rules
- Not securing actuator endpoints
- Mixing authentication methods incorrectly

**6. Follow-up questions:**
- How do you configure multiple SecurityFilterChains?
- What's the order of security filters?

**7. Senior summary:**
"Spring Security provides comprehensive authentication/authorization via filter chain architecture. Configure with SecurityFilterChain bean, use @EnableMethodSecurity for method-level security. Separate API (stateless/JWT) and web (session/form) security configs."

--------------------------------------------------
### Question 83: How authentication works?

**1. Definition:**
Authentication verifies user identity by extracting credentials from the request, validating them against a UserDetailsService, and storing the result in SecurityContext.

**2. Why it matters:**
Understanding the flow enables custom authentication schemes, proper error handling, and debugging authentication issues.

**3. Internal working:**
```
Request with credentials
    ↓
AuthenticationFilter (extracts credentials)
    ↓
AuthenticationManager.authenticate()
    ↓
AuthenticationProvider.authenticate()
    ↓
UserDetailsService.loadUserByUsername()
    ↓
Password verification (PasswordEncoder)
    ↓
Authentication object created
    ↓
SecurityContextHolder.setAuthentication()
    ↓
Request proceeds (authenticated)
```

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPassword())  // Already encoded
            .authorities(user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList())
            .accountLocked(!user.isActive())
            .build();
    }
}
```

**4. Real-world example:**
Custom authentication with additional validation:
```java
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication auth) {
        String username = auth.getName();
        String password = auth.getCredentials().toString();

        User user = userService.findByEmail(username);

        // Custom validations
        if (user.isLocked()) {
            throw new LockedException("Account locked due to too many failed attempts");
        }
        if (user.requiresMfaVerification()) {
            throw new MfaRequiredException(user.getMfaToken());
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            userService.recordFailedAttempt(user);
            throw new BadCredentialsException("Invalid credentials");
        }

        userService.resetFailedAttempts(user);

        return new UsernamePasswordAuthenticationToken(
            user, null, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
            .isAssignableFrom(authentication);
    }
}
```

**5. Pitfalls:**
- Exposing whether username exists (security info leak)
- Not locking accounts after failed attempts
- Storing passwords in plain text
- Session fixation after authentication

**6. Follow-up questions:**
- How do you implement multi-factor authentication?
- What's the difference between AuthenticationProvider and UserDetailsService?

**7. Senior summary:**
"Authentication flows through filters → AuthenticationManager → AuthenticationProvider → UserDetailsService. Passwords verified via PasswordEncoder. Result stored in SecurityContext. Custom providers enable MFA, account locking, and custom validation logic."

--------------------------------------------------
### Question 84: How authorization works?

**1. Definition:**
Authorization determines if an authenticated user has permission to access a resource, using URL-based rules, method-level annotations, or custom access decisions.

**2. Why it matters:**
Authentication proves identity; authorization enforces access control. Proper authorization prevents privilege escalation and unauthorized data access.

**3. Internal working:**
```java
// URL-based authorization
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
    .requestMatchers(HttpMethod.DELETE, "/api/**").hasAuthority("DELETE_PRIVILEGE")
    .anyRequest().authenticated()
);

// Method-level authorization
@EnableMethodSecurity
public class SecurityConfig { }

@Service
public class OrderService {

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteOrder(Long id) { }

    @PreAuthorize("#userId == authentication.principal.id")
    public List<Order> getOrdersForUser(Long userId) { }

    @PostAuthorize("returnObject.owner == authentication.name")
    public Order getOrder(Long id) { }

    @PreAuthorize("@accessChecker.canAccess(#orderId)")
    public void processOrder(Long orderId) { }
}
```

Authorization flow:
```
AuthorizationFilter intercepts request
    ↓
AuthorizationManager.check()
    ↓
Check against configured rules
    ↓
AccessDeniedException if unauthorized
```

**4. Real-world example:**
Complex authorization with custom logic:
```java
@Component
public class OrderAccessChecker {

    public boolean canAccess(Long orderId, Authentication auth) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return false;

        UserPrincipal user = (UserPrincipal) auth.getPrincipal();

        // Admin can access all
        if (user.hasRole("ADMIN")) return true;

        // Owner can access their orders
        if (order.getCustomerId().equals(user.getId())) return true;

        // Support staff can access orders in their region
        if (user.hasRole("SUPPORT") &&
            order.getRegion().equals(user.getRegion())) return true;

        return false;
    }
}

@RestController
public class OrderController {

    @GetMapping("/orders/{id}")
    @PreAuthorize("@orderAccessChecker.canAccess(#id, authentication)")
    public Order getOrder(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @DeleteMapping("/orders/{id}")
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('USER') and @orderAccessChecker.isOwner(#id, authentication))")
    public void deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
    }
}
```

**5. Pitfalls:**
- Over-relying on URL patterns (methods can be missed)
- Not using method security for service layer
- Forgetting to enable @EnableMethodSecurity
- SPEL injection in dynamic authorization expressions

**6. Follow-up questions:**
- What's the difference between @Secured, @PreAuthorize, and @RolesAllowed?
- How do you test authorization rules?

**7. Senior summary:**
"Authorization uses URL patterns for coarse control, @PreAuthorize/@PostAuthorize for method-level. Custom beans in SpEL enable complex logic. Layer URL security (broad) with method security (specific). Test authorization rules explicitly."

--------------------------------------------------
### Question 85: JWT authentication flow?

**1. Definition:**
JWT (JSON Web Token) authentication is a stateless mechanism where the server issues a signed token on login, and the client includes it in subsequent requests for authentication.

**2. Why it matters:**
JWTs enable stateless, scalable APIs—no server-side session storage. Critical for microservices where requests may hit different instances.

**3. Internal working:**
```
1. Client sends credentials to /auth/login
2. Server validates credentials
3. Server generates JWT with claims (user ID, roles, expiry)
4. Server signs JWT with secret key
5. Client stores JWT (localStorage/cookie)
6. Client sends JWT in Authorization header
7. Server validates signature and extracts claims
8. Server authenticates based on claims

JWT Structure:
Header.Payload.Signature

{
  "alg": "HS256"
}
.
{
  "sub": "user@example.com",
  "roles": ["USER", "ADMIN"],
  "exp": 1735689600,
  "iat": 1735686000
}
.
HMACSHA256(header + "." + payload, secret)
```

**4. Real-world example:**
Complete JWT implementation:
```java
@Configuration
@EnableWebSecurity
public class JwtSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws IOException, ServletException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                Claims claims = jwtService.validateToken(jwt);
                UserDetails user = userDetailsService.loadUserByUsername(claims.getSubject());

                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException e) {
                // Invalid token - continue as unauthenticated
            }
        }

        chain.doFilter(request, response);
    }
}

@Service
public class JwtService {
    private final SecretKey key = Keys.hmacShaKeyFor(secretBytes);

    public String generateToken(UserDetails user) {
        return Jwts.builder()
            .subject(user.getUsername())
            .claim("roles", user.getAuthorities())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
            .signWith(key)
            .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
```

**5. Pitfalls:**
- Storing sensitive data in JWT payload (it's base64, not encrypted)
- Short secret keys (use at least 256 bits)
- No token revocation strategy
- Storing JWT in localStorage (XSS vulnerable)

**6. Follow-up questions:**
- How do you revoke a JWT before expiry?
- What's the difference between symmetric and asymmetric JWT signing?

**7. Senior summary:**
"JWT enables stateless authentication—server issues signed token, client presents it. Validate signature and expiry on each request. Use strong secrets, short expiry times with refresh tokens. Store in httpOnly cookie to prevent XSS theft."

--------------------------------------------------
### Question 86: Stateless vs Stateful authentication?

**1. Definition:**
Stateful authentication stores session data on the server (e.g., HttpSession). Stateless authentication includes all necessary data in each request (e.g., JWT).

**2. Why it matters:**
Choice affects scalability, complexity, and security characteristics. Microservices favor stateless; traditional web apps often use stateful.

**3. Internal working:**

| Aspect | Stateful (Session) | Stateless (JWT) |
|--------|-------------------|-----------------|
| Server storage | Yes (session store) | No |
| Scalability | Requires sticky sessions or shared store | Unlimited horizontal scale |
| Revocation | Easy (invalidate session) | Difficult (need blacklist) |
| Data freshness | Always current | Stale until token expires |
| Security | Session ID theft | Token theft |

```java
// Stateful configuration
http.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    .maximumSessions(1)
    .sessionRegistry(sessionRegistry())
);

// Stateless configuration
http.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
);
```

**4. Real-world example:**
Hybrid approach for different endpoints:
```java
@Configuration
public class SecurityConfig {

    // API - stateless JWT
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Web UI - stateful session
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**")
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .expiredUrl("/login?expired"))
            .formLogin(Customizer.withDefaults());
        return http.build();
    }
}
```

**5. Pitfalls:**
- Using stateless without considering revocation needs
- Stateful without distributed session store in clustered env
- Mixing session and JWT in confusing ways
- JWT expiry too long (stale permissions)

**6. Follow-up questions:**
- How do you handle logout with stateless authentication?
- What's the trade-off between short and long JWT expiry?

**7. Senior summary:**
"Stateful uses server-side sessions—easy revocation but requires session store for scaling. Stateless uses tokens—infinitely scalable but revocation requires additional mechanisms. Choose based on architecture: JWT for APIs/microservices, sessions for traditional web apps."

--------------------------------------------------
### Question 87: OAuth2 basics?

**1. Definition:**
OAuth2 is an authorization framework that enables third-party applications to access resources on behalf of users without sharing credentials, using access tokens.

**2. Why it matters:**
OAuth2 is the standard for API authorization and social login. Understanding flows enables proper integration with identity providers.

**3. Internal working:**
OAuth2 roles:
- **Resource Owner**: User who owns the data
- **Client**: Application requesting access
- **Authorization Server**: Issues tokens (Keycloak, Auth0)
- **Resource Server**: API with protected resources

Common flows:
```
Authorization Code (web apps):
1. User clicks "Login with Google"
2. Redirect to Google authorization endpoint
3. User approves
4. Google redirects back with authorization code
5. Server exchanges code for access token
6. Server uses token to access resources

Client Credentials (server-to-server):
1. Service sends client_id + client_secret
2. Authorization server returns access token
3. Service uses token to call other services
```

**4. Real-world example:**
Spring Security OAuth2 configuration:
```java
// OAuth2 Login (social login)
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService))
            .successHandler(oAuth2SuccessHandler)
        );
    return http.build();
}

// OAuth2 Resource Server (API protection)
@Bean
public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
    http
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwtAuthenticationConverter(jwtAuthConverter())
            )
        );
    return http.build();
}

// application.yml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: email, profile
      resourceserver:
        jwt:
          issuer-uri: https://auth.example.com/realms/myapp
```

**5. Pitfalls:**
- Using implicit flow (deprecated, insecure)
- Not validating token issuer
- Storing tokens insecurely
- Confusion between authentication (OpenID Connect) and authorization (OAuth2)

**6. Follow-up questions:**
- What's the difference between OAuth2 and OpenID Connect?
- When would you use Client Credentials vs Authorization Code flow?

**7. Senior summary:**
"OAuth2 is authorization framework using access tokens. Authorization Code flow for user-facing apps, Client Credentials for service-to-service. Spring Security provides both client (oauth2Login) and resource server (oauth2ResourceServer) support. OpenID Connect adds authentication layer."

--------------------------------------------------
### Question 88: How to secure REST APIs?

**1. Definition:**
REST API security combines authentication (JWT/OAuth2), authorization (role/scope-based), transport security (HTTPS), input validation, and protection against common attacks.

**2. Why it matters:**
APIs are prime attack targets. Layered security defends against various attack vectors—authentication bypass, injection, data exposure.

**3. Internal working:**
Security layers:
```
1. Transport: HTTPS/TLS
2. Authentication: JWT, OAuth2, API Keys
3. Authorization: Roles, Scopes, Resource-based
4. Input Validation: Bean Validation, sanitization
5. Rate Limiting: Prevent abuse
6. Output: No sensitive data exposure
```

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ApiSecurityConfig {

    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())  // Stateless API
            .cors(cors -> cors.configurationSource(corsConfig()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(customEntryPoint)
                .accessDeniedHandler(customAccessDenied)
            );
        return http.build();
    }
}
```

**4. Real-world example:**
Comprehensive API security:
```java
@RestController
@RequestMapping("/api/v1")
public class SecureApiController {

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public UserDto getUser(@PathVariable Long id) {
        // Authorization checked
        return userService.findById(id);
    }

    @PostMapping("/orders")
    @RateLimited(requests = 100, period = "1m")  // Custom annotation
    public Order createOrder(@RequestBody @Valid OrderRequest request) {
        // Input validated
        return orderService.create(request);
    }
}

// Security headers
@Component
public class SecurityHeadersFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Cache-Control", "no-store");
        chain.doFilter(req, res);
    }
}
```

**5. Pitfalls:**
- HTTPS in production but HTTP in dev (behavior differences)
- Verbose error messages exposing system info
- Not validating Content-Type header
- Missing rate limiting

**6. Follow-up questions:**
- How do you prevent enumeration attacks?
- What security headers should every API return?

**7. Senior summary:**
"Secure APIs with layers: HTTPS transport, JWT/OAuth2 authentication, role-based authorization, input validation, rate limiting, security headers. Disable CSRF for stateless APIs but keep it for session-based. Return consistent error messages without exposing internals."

--------------------------------------------------
### Question 89: Role-based access control?

**1. Definition:**
RBAC assigns permissions to roles (ADMIN, USER) rather than individuals. Users are assigned roles, and access decisions are made based on role membership.

**2. Why it matters:**
RBAC simplifies permission management—manage roles, not individual user permissions. Scales well and is easier to audit.

**3. Internal working:**
```java
// Role definition in UserDetails
public class CustomUserDetails implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toList());
    }
}

// URL-based RBAC
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
    .requestMatchers("/api/**").hasAuthority("API_ACCESS")
);

// Method-level RBAC
@PreAuthorize("hasRole('ADMIN')")
public void adminOperation() { }

@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public void userOperation() { }
```

Note: Spring Security automatically adds "ROLE_" prefix for hasRole() checks.

**4. Real-world example:**
Hierarchical roles with permissions:
```java
@Entity
public class Role {
    @Id
    private Long id;
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Permission> permissions;
}

@Entity
public class Permission {
    @Id
    private Long id;
    private String name;  // READ_USERS, WRITE_USERS, DELETE_USERS
}

// Custom authority mapping
@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmailWithRoles(username);

        List<GrantedAuthority> authorities = new ArrayList<>();

        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            for (Permission perm : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(perm.getName()));
            }
        }

        return new CustomUserPrincipal(user, authorities);
    }
}

// Usage with permissions
@PreAuthorize("hasAuthority('DELETE_USERS')")
public void deleteUser(Long id) { }

@PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_ORDERS')")
public void cancelOrder(Long id) { }
```

**5. Pitfalls:**
- Role explosion (too many fine-grained roles)
- Hardcoding roles in code (hard to change)
- Not considering role hierarchy
- Mixing ROLE_ prefix inconsistently

**6. Follow-up questions:**
- How do you implement role hierarchy?
- When would you use permissions instead of roles?

**7. Senior summary:**
"RBAC assigns permissions via roles, not individuals. Spring Security uses GrantedAuthority with ROLE_ prefix. Combine roles (broad) with permissions (specific) for flexibility. Use role hierarchy for inheritance. Store in database for dynamic management."

--------------------------------------------------
### Question 90: Filter vs Interceptor?

**1. Definition:**
Filters are Servlet API components operating on raw requests/responses. Interceptors are Spring MVC components with access to handler information and model.

**2. Why it matters:**
Choosing correctly affects what information is available and when in the request lifecycle. Security typically uses filters; cross-cutting MVC concerns use interceptors.

**3. Internal working:**
```
Request → Filter₁ → Filter₂ → ... → DispatcherServlet →
         → Interceptor.preHandle() →
         → Controller →
         → Interceptor.postHandle() →
         → View rendering →
         → Interceptor.afterCompletion() →
         ← Filter₂ ← Filter₁ ← Response
```

| Aspect | Filter | Interceptor |
|--------|--------|-------------|
| API | Servlet | Spring MVC |
| Access | Request/Response | Handler, ModelAndView |
| Scope | All requests | Spring MVC requests only |
| Order | Before DispatcherServlet | After DispatcherServlet |
| Exception handling | Limited | Full Spring exception handling |

```java
// Filter
@Component
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        // Before - raw servlet objects
        chain.doFilter(req, res);
        // After
    }
}

// Interceptor
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // Handler info available
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            log.info("Calling: {}", method.getMethod().getName());
        }
        return true;  // Continue chain
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) {
        // After controller, before view
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler,
                                 Exception ex) {
        // After complete request (including view)
    }
}
```

**4. Real-world example:**
Using both appropriately:
```java
// Security - Filter (before Spring MVC)
@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) {
        String apiKey = request.getHeader("X-API-Key");
        if (!isValidApiKey(apiKey)) {
            response.setStatus(401);
            return;
        }
        chain.doFilter(request, response);
    }
}

// Audit logging - Interceptor (handler info available)
@Component
public class AuditInterceptor implements HandlerInterceptor {
    @Override
    public void afterCompletion(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler,
                                 Exception ex) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            if (method.hasMethodAnnotation(Audited.class)) {
                auditService.log(request, response, method);
            }
        }
    }
}
```

**5. Pitfalls:**
- Using interceptor for security (too late in chain)
- Filter for logic requiring Spring context (not available early)
- Not registering interceptor with WebMvcConfigurer
- Filter ordering issues

**6. Follow-up questions:**
- Can an interceptor prevent a request from reaching the controller?
- How do you order multiple filters?

**7. Senior summary:**
"Filters for security and low-level concerns (before Spring MVC). Interceptors for MVC-aware cross-cutting concerns (audit, logging). Filters see raw servlet; interceptors see handler info. Security filters must run before DispatcherServlet."

--------------------------------------------------
### Question 91: What is CSRF?

**1. Definition:**
CSRF (Cross-Site Request Forgery) is an attack where a malicious site tricks a user's browser into making unwanted requests to a site where they're authenticated, using their existing session.

**2. Why it matters:**
CSRF can lead to unauthorized state changes—fund transfers, password changes, data deletion. Protection is essential for session-based applications.

**3. Internal working:**
Attack scenario:
```
1. User logs into bank.com, session cookie set
2. User visits evil.com
3. evil.com has: <img src="https://bank.com/transfer?to=attacker&amount=1000">
4. Browser sends request WITH session cookie
5. Bank.com sees valid session, executes transfer
```

Protection (Synchronizer Token Pattern):
```java
// Spring Security default for session-based apps
http.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
);

// In HTML forms
<form method="post" action="/transfer">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    ...
</form>

// In AJAX (from meta tag or cookie)
fetch('/api/transfer', {
    method: 'POST',
    headers: {
        'X-CSRF-TOKEN': csrfToken
    }
});
```

**4. Real-world example:**
Configuring CSRF properly:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                // Cookie-based token for SPAs
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // Custom handler
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                // Ignore specific paths (webhooks)
                .ignoringRequestMatchers("/webhook/**")
            );
        return http.build();
    }

    // For stateless APIs - disable CSRF
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())  // Safe for stateless
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
```

**5. Pitfalls:**
- Disabling CSRF for session-based apps
- Not including token in AJAX requests
- Storing CSRF token in localStorage (defeats purpose)
- Thinking CSRF protects against XSS (it doesn't)

**6. Follow-up questions:**
- Why is CSRF not needed for stateless APIs?
- What's the difference between CSRF token in cookie vs session?

**7. Senior summary:**
"CSRF exploits session cookies to forge requests. Spring Security uses synchronizer tokens—server verifies token matches session. Safe to disable for stateless JWT APIs (no session to exploit). Always enable for session-based apps."

--------------------------------------------------
### Question 92: CORS vs CSRF?

**1. Definition:**
CORS controls which origins can make cross-origin requests to your server. CSRF prevents malicious sites from making authenticated requests on behalf of users.

**2. Why it matters:**
Both involve cross-origin scenarios but solve different problems. Confusing them leads to security vulnerabilities or broken functionality.

**3. Internal working:**

| Aspect | CORS | CSRF |
|--------|------|------|
| Purpose | Allow cross-origin access | Prevent forged requests |
| Direction | Who can call my API | Stop malicious callers |
| Mechanism | Browser preflight + headers | Token verification |
| Configuration | Server whitelist | Token generation/validation |
| When needed | APIs consumed by other domains | Session-based auth |

```java
// CORS - "who can call me"
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://app.example.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}

// CSRF - "verify it's really my user"
http.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
);
```

**4. Real-world example:**
SPA with API backend:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS - allow frontend domain
            .cors(cors -> cors.configurationSource(corsConfig()))

            // CSRF - frontend sends token in header
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
            )

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            );

        return http.build();
    }
}

// Frontend (React) sends both:
// 1. Cookie with session ID (CORS allows it)
// 2. CSRF token in header (proves it's not forged)
axios.defaults.withCredentials = true;  // Send cookies
axios.defaults.headers.common['X-CSRF-TOKEN'] = getCsrfToken();
```

**5. Pitfalls:**
- Thinking CORS protects against CSRF (it doesn't)
- Disabling CSRF because CORS is configured (wrong)
- allowedOrigins("*") with allowCredentials(true) (forbidden)
- Not understanding both can be needed simultaneously

**6. Follow-up questions:**
- Can CORS prevent CSRF attacks?
- Why can't you use wildcard origins with credentials?

**7. Senior summary:**
"CORS controls access—which domains can call your API. CSRF prevents forgery—verifies requests come from legitimate users. Both needed for session-based apps with cross-origin frontends. CORS allows the call; CSRF verifies authenticity."

--------------------------------------------------
### Question 93: How password encoding works?

**1. Definition:**
Password encoding uses one-way hashing with salt to store passwords securely. Spring Security's `PasswordEncoder` handles hashing on storage and verification on login.

**2. Why it matters:**
Plain text or weak hashing leads to mass credential theft on breach. Proper encoding limits damage—attackers get hashes, not passwords.

**3. Internal working:**
```java
// Configure encoder
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);  // Work factor 12
}

// Registration
String rawPassword = "user123";
String encoded = passwordEncoder.encode(rawPassword);
// $2a$12$K7L1OJ45/N7IzVQT8vWeEO...

// Login verification
boolean matches = passwordEncoder.matches(rawPassword, storedHash);
```

BCrypt internals:
```
$2a$12$K7L1OJ45/N7IzVQT8vWeEOgKL3p.../...
 │  │  │                          │
 │  │  └─ Salt (22 chars)         └─ Hash
 │  └─ Work factor (2^12 iterations)
 └─ Algorithm identifier
```

**4. Real-world example:**
Upgrading password encoding:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    // DelegatingPasswordEncoder for migration
    Map<String, PasswordEncoder> encoders = new HashMap<>();
    encoders.put("bcrypt", new BCryptPasswordEncoder(12));
    encoders.put("scrypt", SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8());
    encoders.put("argon2", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());

    DelegatingPasswordEncoder encoder =
        new DelegatingPasswordEncoder("argon2", encoders);

    // Handle legacy passwords without prefix
    encoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder(10));

    return encoder;
}

// Stored passwords:
// Old: $2a$10$... (bcrypt, no prefix - uses default)
// Migrated: {bcrypt}$2a$12$... (bcrypt with prefix)
// New: {argon2}... (argon2)

// Re-encode on login if using old encoder
@Component
public class PasswordUpgradeService {
    public void upgradePasswordIfNeeded(User user, String rawPassword) {
        if (passwordEncoder.upgradeEncoding(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
        }
    }
}
```

**5. Pitfalls:**
- Using MD5 or SHA-1 (too fast, easily cracked)
- Low work factor (benchmark for ~1 second hash time)
- Not salting (enables rainbow tables)
- Comparing encoded values directly (use matches())

**6. Follow-up questions:**
- Why is BCrypt better than SHA-256 for passwords?
- How do you migrate from weak to strong password encoding?

**7. Senior summary:**
"Use BCrypt, SCrypt, or Argon2—adaptive algorithms designed for passwords. Work factor should make hashing take ~1 second. Use DelegatingPasswordEncoder for algorithm migration. Never compare hashes directly—use matches() which handles timing attacks."

--------------------------------------------------
### Question 94: What is SecurityFilterChain?

**1. Definition:**
`SecurityFilterChain` is a bean defining a set of security filters and their configuration for matching requests. It replaced the deprecated `WebSecurityConfigurerAdapter` in Spring Security 5.7+.

**2. Why it matters:**
It's the modern way to configure security. Multiple SecurityFilterChains enable different security policies for different parts of your application.

**3. Internal working:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // First filter chain - API security
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")  // Only matches /api paths
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    // Second filter chain - Web security
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/css/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
            );

        return http.build();
    }
}
```

Filter chain selection:
1. Iterate through beans by @Order
2. First chain where securityMatcher matches handles request
3. If no matcher specified, matches all requests

**4. Real-world example:**
Complex multi-tenant security:
```java
@Configuration
@EnableWebSecurity
public class MultiSecurityConfig {

    // Health endpoints - no security
    @Bean
    @Order(0)
    public SecurityFilterChain healthSecurityChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/health/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    // Admin API - strict security
    @Bean
    @Order(1)
    public SecurityFilterChain adminApiChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/admin/**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().hasRole("ADMIN"))
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                .jwtAuthenticationConverter(adminJwtConverter())));
        return http.build();
    }

    // Public API - rate-limited, basic auth
    @Bean
    @Order(2)
    public SecurityFilterChain publicApiChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/public/**")
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    // Default - form login
    @Bean
    @Order(3)
    public SecurityFilterChain defaultChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated())
            .formLogin(Customizer.withDefaults());
        return http.build();
    }
}
```

**5. Pitfalls:**
- Order matters—first match wins
- Missing securityMatcher makes chain match everything
- Duplicate filter chains for same paths
- Not building the chain (forgetting .build())

**6. Follow-up questions:**
- How does securityMatcher differ from authorizeHttpRequests?
- Can a request match multiple SecurityFilterChains?

**7. Senior summary:**
"SecurityFilterChain is the modern security configuration approach—one bean per security policy. Use @Order to control precedence, securityMatcher to scope paths. First matching chain handles the request. Enables clean separation of API vs web security."

--------------------------------------------------
### Question 95: How custom authentication works?

**1. Definition:**
Custom authentication involves implementing `AuthenticationProvider` to handle non-standard authentication mechanisms like multi-factor, LDAP, or external identity systems.

**2. Why it matters:**
Real applications often need custom authentication logic—MFA, account locking, external providers. Understanding the extension points enables these requirements.

**3. Internal working:**
```java
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        String mfaCode = ((CustomAuthenticationToken) authentication).getMfaCode();

        // Load user
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new BadCredentialsException("User not found");
        }

        // Validate password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            userService.recordFailedAttempt(user);
            throw new BadCredentialsException("Invalid password");
        }

        // Validate MFA
        if (user.isMfaEnabled()) {
            if (!mfaService.validate(user, mfaCode)) {
                throw new BadCredentialsException("Invalid MFA code");
            }
        }

        // Success
        userService.clearFailedAttempts(user);
        return new UsernamePasswordAuthenticationToken(
            user, null, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
```

**4. Real-world example:**
Complete MFA flow:
```java
// Custom token with MFA code
public class MfaAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final String mfaCode;

    public MfaAuthenticationToken(String username, String password, String mfaCode) {
        super(username, password);
        this.mfaCode = mfaCode;
    }
}

// Custom filter to create token
public class MfaAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                  HttpServletResponse response) {
        String username = obtainUsername(request);
        String password = obtainPassword(request);
        String mfaCode = request.getParameter("mfaCode");

        MfaAuthenticationToken token =
            new MfaAuthenticationToken(username, password, mfaCode);

        return getAuthenticationManager().authenticate(token);
    }
}

// Configuration
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authenticationProvider(customAuthenticationProvider)
        .addFilterAt(mfaAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

**5. Pitfalls:**
- Not handling all exception types (information leak)
- Timing attacks in credential validation
- Forgetting to register custom provider
- supports() method not matching token type

**6. Follow-up questions:**
- How do you chain multiple AuthenticationProviders?
- How do you handle partial authentication (MFA step)?

**7. Senior summary:**
"Custom authentication needs AuthenticationProvider implementation. Override authenticate() for validation logic, supports() to declare token types handled. Register with AuthenticationManagerBuilder. For MFA, use custom token types and staged authentication."

--------------------------------------------------
### Question 96: How to secure Actuator endpoints?

**1. Definition:**
Actuator endpoints are secured via SecurityFilterChain configuration, typically separating management security from main application security.

**2. Why it matters:**
Actuator exposes sensitive data—environment variables, heap dumps, shutdown. Unsecured actuators have led to major breaches.

**3. Internal working:**
```java
@Configuration
@EnableWebSecurity
public class ActuatorSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(auth -> auth
                // Health and info - public (for load balancers)
                .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                // Prometheus metrics - internal network only
                .requestMatchers(EndpointRequest.to("prometheus"))
                    .access(hasIpAddress("10.0.0.0/8").or(hasIpAddress("192.168.0.0/16")))
                // Everything else - admin only
                .anyRequest().hasRole("ACTUATOR_ADMIN")
            )
            .httpBasic(Customizer.withDefaults());  // Simple auth for actuator

        return http.build();
    }
}
```

Configuration:
```yaml
management:
  server:
    port: 8081  # Separate port
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /management
  endpoint:
    health:
      show-details: when_authorized
      probes:
        enabled: true
    env:
      show-values: never  # Never show sensitive values
```

**4. Real-world example:**
Production actuator security:
```java
@Configuration
public class ActuatorConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> auth
                // Kubernetes probes
                .requestMatchers("/actuator/health/liveness").permitAll()
                .requestMatchers("/actuator/health/readiness").permitAll()
                // Prometheus scraping - service account
                .requestMatchers("/actuator/prometheus")
                    .hasRole("METRICS_SCRAPER")
                // Admin operations - require MFA
                .requestMatchers("/actuator/loggers/**", "/actuator/env/**")
                    .access(hasRole("ADMIN").and(hasMfaVerified()))
                // Thread dump, heap dump - super admin only
                .requestMatchers("/actuator/threaddump", "/actuator/heapdump")
                    .hasRole("SUPER_ADMIN")
                .anyRequest().hasRole("ADMIN")
            )
            .httpBasic(basic -> basic.realmName("Actuator"));

        return http.build();
    }
}

// Custom authorization for MFA
private static AuthorizationManager<RequestAuthorizationContext> hasMfaVerified() {
    return (auth, context) -> {
        Authentication authentication = auth.get();
        if (authentication instanceof CustomAuthentication custom) {
            return new AuthorizationDecision(custom.isMfaVerified());
        }
        return new AuthorizationDecision(false);
    };
}
```

**5. Pitfalls:**
- Exposing actuator on same port as application
- /env endpoint showing secrets
- /heapdump downloadable without auth
- Not using EndpointRequest matchers (path changes break security)

**6. Follow-up questions:**
- How do you expose actuator only to internal network?
- Which actuator endpoints are most dangerous?

**7. Senior summary:**
"Secure actuator with separate SecurityFilterChain using EndpointRequest matchers. Health/info can be public for probes; everything else needs authentication. Use separate management port, hide sensitive endpoint values, and restrict heapdump/threaddump to super admins only."

--------------------------------------------------
### Question 97: What is OncePerRequestFilter?

**1. Definition:**
`OncePerRequestFilter` is a base class ensuring filter logic executes exactly once per request, even if the request is forwarded or included internally.

**2. Why it matters:**
Standard filters can execute multiple times on internal forwards/includes. OncePerRequestFilter prevents duplicate processing for authentication, logging, and other once-per-request logic.

**3. Internal working:**
```java
public abstract class OncePerRequestFilter extends GenericFilterBean {

    @Override
    public final void doFilter(ServletRequest request, ServletResponse response,
                                FilterChain chain) {
        String alreadyFilteredAttribute = getAlreadyFilteredAttributeName();

        if (request.getAttribute(alreadyFilteredAttribute) != null) {
            // Already executed - skip
            chain.doFilter(request, response);
        } else {
            // Mark as executed
            request.setAttribute(alreadyFilteredAttribute, Boolean.TRUE);
            try {
                doFilterInternal((HttpServletRequest) request,
                                  (HttpServletResponse) response, chain);
            } finally {
                request.removeAttribute(alreadyFilteredAttribute);
            }
        }
    }

    protected abstract void doFilterInternal(HttpServletRequest request,
                                              HttpServletResponse response,
                                              FilterChain chain);
}
```

**4. Real-world example:**
JWT authentication filter:
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Claims claims = jwtService.validateToken(token);

                UserDetails userDetails = userDetailsService
                    .loadUserByUsername(claims.getSubject());

                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                auth.setDetails(new WebAuthenticationDetailsSource()
                    .buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException e) {
                logger.debug("Invalid JWT token: {}", e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip filter for certain paths
        return request.getServletPath().startsWith("/public/");
    }
}
```

**5. Pitfalls:**
- Extending Filter instead (runs multiple times on forwards)
- Not calling chain.doFilter() (breaks chain)
- Heavy processing without shouldNotFilter optimization
- Throwing exceptions instead of continuing chain for optional auth

**6. Follow-up questions:**
- When would doFilter be called multiple times for one request?
- How do you skip the filter for certain paths?

**7. Senior summary:**
"OncePerRequestFilter guarantees exactly-once execution per request, handling forwards/includes automatically. Use for authentication, logging, and other once-per-request logic. Override shouldNotFilter() to skip paths. Always continue the chain—don't block for optional processing."

--------------------------------------------------
### Question 98: Session management in Spring Security?

**1. Definition:**
Session management controls session creation, concurrency, fixation protection, and timeout—critical for session-based authentication security.

**2. Why it matters:**
Poor session management leads to session hijacking, fixation attacks, and resource exhaustion from too many concurrent sessions.

**3. Internal working:**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .sessionManagement(session -> session
            // Session creation policy
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)

            // Session fixation protection
            .sessionFixation(fixation -> fixation.migrateSession())

            // Concurrent sessions
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false)  // Expire old session
            .expiredUrl("/login?expired")
            .sessionRegistry(sessionRegistry())
        )
        .logout(logout -> logout
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
        );

    return http.build();
}

@Bean
public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
}
```

Session creation policies:
- `ALWAYS`: Always create session
- `IF_REQUIRED`: Create if needed (default)
- `NEVER`: Never create, use existing
- `STATELESS`: Never create or use

**4. Real-world example:**
Complete session management:
```java
@Configuration
@EnableWebSecurity
public class SessionSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().migrateSession()
                .maximumSessions(2)
                .maxSessionsPreventsLogin(true)  // Block new login
                .expiredSessionStrategy(event -> {
                    HttpServletResponse response = event.getResponse();
                    response.setStatus(401);
                    response.getWriter().write("Session expired");
                })
                .sessionRegistry(sessionRegistry())
            )
            .rememberMe(remember -> remember
                .tokenRepository(persistentTokenRepository())
                .tokenValiditySeconds(14 * 24 * 60 * 60)  // 14 days
            );

        return http.build();
    }

    // For listing active sessions
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    // Admin endpoint to view/kill sessions
    @GetMapping("/admin/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SessionInfo> getActiveSessions() {
        return sessionRegistry.getAllPrincipals().stream()
            .flatMap(principal -> sessionRegistry
                .getAllSessions(principal, false).stream())
            .map(SessionInfo::from)
            .toList();
    }

    @DeleteMapping("/admin/sessions/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void expireSession(@PathVariable String sessionId) {
        sessionRegistry.getSessionInformation(sessionId).expireNow();
    }
}
```

**5. Pitfalls:**
- Session fixation not enabled (vulnerable to attack)
- Unlimited concurrent sessions (resource exhaustion)
- Remember-me without persistent token (less secure)
- Not invalidating session on logout

**6. Follow-up questions:**
- What is session fixation attack?
- How do you share sessions across multiple servers?

**7. Senior summary:**
"Session management controls creation, concurrency, and fixation protection. migrateSession() prevents fixation attacks. Limit concurrent sessions with maximumSessions(). Use SessionRegistry for admin session management. For clusters, use Redis-backed session store."

--------------------------------------------------
### Question 99: Token expiration handling?

**1. Definition:**
Token expiration handling involves setting appropriate expiry times, detecting expired tokens, and providing refresh mechanisms to maintain user sessions without re-authentication.

**2. Why it matters:**
Short-lived tokens limit damage from theft. Proper expiration handling balances security with user experience—users shouldn't re-login frequently.

**3. Internal working:**
```java
// Token with expiration
public String generateToken(UserDetails user) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY);

    return Jwts.builder()
        .subject(user.getUsername())
        .issuedAt(now)
        .expiration(expiry)
        .signWith(secretKey)
        .compact();
}

// Validation with expiry check
public Claims validateToken(String token) {
    try {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    } catch (ExpiredJwtException e) {
        throw new TokenExpiredException("Token has expired");
    } catch (JwtException e) {
        throw new InvalidTokenException("Invalid token");
    }
}
```

**4. Real-world example:**
Complete token lifecycle management:
```java
@Service
public class TokenService {

    private static final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000;  // 15 mins
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000;  // 7 days

    public TokenPair generateTokenPair(User user) {
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        // Store refresh token for revocation capability
        refreshTokenRepository.save(new RefreshToken(
            user.getId(),
            hash(refreshToken),
            Instant.now().plusMillis(REFRESH_TOKEN_VALIDITY)
        ));

        return new TokenPair(accessToken, refreshToken);
    }

    public TokenPair refreshAccessToken(String refreshToken) {
        // Validate refresh token
        Claims claims = validateRefreshToken(refreshToken);

        // Check if token is revoked
        if (!refreshTokenRepository.existsByUserIdAndHash(
                claims.get("userId", Long.class), hash(refreshToken))) {
            throw new TokenRevokedException("Refresh token has been revoked");
        }

        // Generate new pair (refresh token rotation)
        User user = userService.findById(claims.get("userId", Long.class));
        refreshTokenRepository.deleteByHash(hash(refreshToken));  // Revoke old
        return generateTokenPair(user);
    }

    public void revokeAllTokens(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }
}

@RestController
public class AuthController {

    @PostMapping("/auth/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue("refresh_token") String refreshToken) {
        try {
            TokenPair tokens = tokenService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createRefreshCookie(tokens.refreshToken()))
                .body(new TokenResponse(tokens.accessToken()));
        } catch (TokenExpiredException e) {
            return ResponseEntity.status(401)
                .body(new TokenResponse(null, "Refresh token expired"));
        }
    }
}
```

**5. Pitfalls:**
- Access tokens too long-lived (security risk)
- Refresh tokens without rotation (theft risk)
- No token revocation mechanism
- Storing refresh token in localStorage (XSS vulnerable)

**6. Follow-up questions:**
- What's refresh token rotation?
- How do you handle token expiration in SPAs?

**7. Senior summary:**
"Short-lived access tokens (15 mins) with longer refresh tokens (days). Store refresh tokens server-side for revocation. Rotate refresh tokens on use. Handle expiration gracefully—silent refresh in SPA, redirect for server-rendered apps. Use httpOnly cookies for refresh tokens."

--------------------------------------------------
### Question 100: Refresh token flow?

**1. Definition:**
Refresh token flow uses a long-lived refresh token to obtain new access tokens without re-authentication, maintaining user sessions while keeping access tokens short-lived.

**2. Why it matters:**
It balances security (short access token lifetime) with UX (users don't re-authenticate frequently). Enables token revocation and session management.

**3. Internal working:**
```
1. User logs in with credentials
2. Server returns:
   - Short-lived access token (15 mins)
   - Long-lived refresh token (7 days)
3. Access token used for API calls
4. When access token expires (401):
   - Client sends refresh token to /auth/refresh
   - Server validates refresh token
   - Server issues new access token (and optionally new refresh token)
5. If refresh token expired → user must re-authenticate
```

```java
// Token storage recommendations
// Access token: Memory (JS variable) - never localStorage
// Refresh token: httpOnly cookie - not accessible to JS
```

**4. Real-world example:**
Secure refresh implementation:
```java
@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = (User) auth.getPrincipal();
        TokenPair tokens = tokenService.generateTokenPair(user);

        ResponseCookie refreshCookie = ResponseCookie
            .from("refresh_token", tokens.refreshToken())
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/auth/refresh")
            .maxAge(Duration.ofDays(7))
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(new LoginResponse(tokens.accessToken(), user.toDto()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null) {
            return ResponseEntity.status(401).body("No refresh token");
        }

        try {
            TokenPair tokens = tokenService.refreshAccessToken(refreshToken);

            // Rotate refresh token
            ResponseCookie newRefreshCookie = ResponseCookie
                .from("refresh_token", tokens.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .build();

            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefreshCookie.toString())
                .body(new RefreshResponse(tokens.accessToken()));

        } catch (TokenException e) {
            // Clear invalid cookie
            ResponseCookie clearCookie = ResponseCookie
                .from("refresh_token", "")
                .maxAge(0)
                .build();

            return ResponseEntity.status(401)
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body("Invalid refresh token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        if (refreshToken != null) {
            tokenService.revokeToken(refreshToken);
        }

        ResponseCookie clearCookie = ResponseCookie
            .from("refresh_token", "")
            .maxAge(0)
            .path("/auth/refresh")
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
            .body("Logged out");
    }
}
```

SPA client handling:
```javascript
// Axios interceptor for automatic refresh
axios.interceptors.response.use(
    response => response,
    async error => {
        if (error.response?.status === 401 && !error.config._retry) {
            error.config._retry = true;

            try {
                const response = await axios.post('/auth/refresh');
                // New access token in response
                setAccessToken(response.data.accessToken);

                // Retry original request
                error.config.headers.Authorization =
                    `Bearer ${response.data.accessToken}`;
                return axios(error.config);
            } catch (refreshError) {
                // Refresh failed - redirect to login
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);
```

**5. Pitfalls:**
- Refresh token in localStorage (XSS theft)
- No token rotation (stolen token valid until expiry)
- Refresh endpoint not rate-limited
- Not invalidating refresh tokens on password change

**6. Follow-up questions:**
- What is refresh token rotation and why is it important?
- How do you handle refresh token theft?

**7. Senior summary:**
"Refresh tokens extend sessions without re-authentication. Store in httpOnly cookies, rotate on use, store server-side for revocation. Implement silent refresh in SPA with axios interceptor. Invalidate all refresh tokens on password change or logout."

--------------------------------------------------
### Question 101: Spring Security best practices?

**1. Definition:**
Security best practices include defense in depth, least privilege, secure defaults, proper secret management, and regular security audits.

**2. Why it matters:**
Security is only as strong as the weakest link. Following best practices systematically reduces attack surface and limits breach impact.

**3. Internal working:**

**Authentication:**
```java
// Use strong password encoding
@Bean
public PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
}

// Account lockout after failed attempts
// Rate limiting on authentication endpoints
// MFA for sensitive operations
```

**Authorization:**
```java
// Default deny, explicit allow
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/public/**").permitAll()
    .anyRequest().authenticated()  // Default: deny
);

// Method-level security for sensitive operations
@PreAuthorize("hasRole('ADMIN') and @accessChecker.canModify(#id)")
public void modifyResource(Long id) { }
```

**4. Real-world example:**
Production security configuration:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ProductionSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Headers
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self'"))
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true))
            )

            // CSRF for session-based
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))

            // Session security
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(3)
                .sessionRegistry(sessionRegistry()))

            // Authentication
            .formLogin(form -> form
                .loginPage("/login")
                .failureHandler(authenticationFailureHandler())
                .successHandler(authenticationSuccessHandler()))

            // Logout
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .clearAuthentication(true))

            // Exception handling
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(customEntryPoint())
                .accessDeniedHandler(customAccessDenied()));

        return http.build();
    }

    // Log failed attempts
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String ip = request.getRemoteAddr();
            String username = request.getParameter("username");
            securityAuditService.logFailedLogin(ip, username, exception.getMessage());

            response.sendRedirect("/login?error");
        };
    }
}
```

**Checklist:**
- [ ] HTTPS everywhere (HSTS enabled)
- [ ] Strong password policy and encoding
- [ ] Account lockout and rate limiting
- [ ] CSRF protection for session-based apps
- [ ] Secure session management
- [ ] Proper CORS configuration
- [ ] Security headers (CSP, X-Frame-Options, etc.)
- [ ] Input validation and output encoding
- [ ] Secure secret management (not in code)
- [ ] Regular dependency updates
- [ ] Security logging and monitoring
- [ ] Actuator endpoints secured
- [ ] Error messages don't leak info

**5. Pitfalls:**
- Security through obscurity
- Overly permissive CORS
- Secrets in source control
- Not updating dependencies
- No security logging

**6. Follow-up questions:**
- How do you audit security configurations?
- What's your approach to security testing?

**7. Senior summary:**
"Defense in depth: multiple security layers. Secure defaults with explicit exceptions. Store secrets in vault, not code. Enable security headers, enforce HTTPS. Log security events, update dependencies regularly. Regular security audits and penetration testing.""
