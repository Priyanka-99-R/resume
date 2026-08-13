What is oops?
OOP (Object-Oriented Programming) in Java is a programming paradigm where software is designed using objects and classes
It helps organize code in a modular, reusable, and maintainable way.
In OOP, a class defines properties and behaviors, and an object is an instance of that class.

Encapsulation — Hide internal state, expose controlled access via methods.
Inheritance — Reuse common behavior by extending parent classes.
Polymorphism — Same interface, different implementations at runtime.
Abstraction — Define "what" without "how" — hide complexity behind interfaces.











1, How did AI caching help reduce cost?

To reduce AI usage cost and improve performance, we introduced a multi-level caching strategy for AI-generated responses.
In our application, certain solution steps had predefined or frequently used questions. When a user selected one of these default questions, we initially called the AI API to generate the response and stored the result in a database, linking it to the user.
For subsequent users selecting the same question, instead of calling the AI API again, we retrieved the response from the local database and associated it with the current user. This significantly reduced repeated AI calls for identical prompts.
Additionally, we implemented an in-memory cache layer to store frequently accessed AI responses, which further minimized database reads and improved response time.
As a result, we were able to reduce AI API calls, lower operational costs, improve performance, and still deliver consistent responses to users.


2, Functional vs Non-Functional Requirements

🔹 1️⃣ Functional Requirements
📌 Definition

Functional requirements describe what the system should do.

They define the features and business logic of the application.

🧠 Simple Meaning:

👉 Functional = Functionality / Features

📌 Examples (Real Project Based)

If you're building a Mobility Ticketing System (like your domain):

User should be able to register and login

User should be able to book a ticket

System should calculate fare amount

Admin should generate reports

Payment should be processed successfully

These describe behavior of the system.


This is implementing a functional requirement:
👉 "User can book a ticket"

🔹 2️⃣ Non-Functional Requirements (NFR)
📌 Definition

Non-functional requirements describe how the system performs.

They define quality attributes of the system.

🧠 Simple Meaning:

👉 Non-functional = System Quality / Performance

| Type            | Example                               |
| --------------- | ------------------------------------- |
| Performance     | API response time < 2 seconds         |
| Scalability     | Support 10,000 concurrent users       |
| Security        | JWT authentication required           |
| Availability    | 99.9% uptime                          |
| Usability       | Easy UI navigation                    |
| Reliability     | System should not crash under load    |
| Maintainability | Code should follow clean architecture |


| Functional       | Non-Functional                            |
| ---------------- | ----------------------------------------- |
| What system does | How system behaves                        |
| Features         | Quality attributes                        |
| Booking ticket   | Booking should complete within 2 seconds  |
| Login user       | Login should be secure (OAuth/JWT)        |
| Generate report  | Report generation should not exceed 5 sec |





MongoDB Aggregation Pipeline is a framework for processing documents through multiple stages like filtering, grouping, projecting, sorting, and joining, allowing complex data transformations similar to SQL aggregation.



In Spring Boot, caching is implemented using Spring Cache abstraction with annotations like @Cacheable, @CachePut, and @CacheEvict. Caffeine is an in-memory local cache used for single-instance applications with very high speed. Redis is a distributed cache server suitable for microservices architecture and supports persistence and clustering. We typically use Caffeine for local fast caching and Redis for distributed shared caching 


Spring Boot applications are already multi-threaded because embedded servers like Tomcat use thread pools to handle concurrent requests. We avoid manually creating threads because it leads to lifecycle, transaction, and resource management issues. Instead, we use Spring-managed approaches like @Async, ThreadPoolTaskExecutor, or event-driven systems like Kafka for background processing.


@Transactional is used to manage database transactions in Spring.
It ensures that a set of database operations either fully succeed (commit) or fully fail (rollback).

In our project, we used @Transactional in the service layer for operations like user registration where multiple database inserts were required. If any step failed, the entire transaction was rolled back to maintain consistency. We also used REQUIRES_NEW for audit logging to ensure logs are stored even if the main transaction fails.



// Pick which Cognito properties to use based on token's issuer
AwsCognitoBaseProperties selectedProps = resolveProperties(token);
// Build JWKS provider
// 3. Get RSA public key
// 4. Build verifier with clock skew tolerance
Algorithm algorithm = Algorithm.RSA256(publicKey, null);
JWTVerifier verifier = JWT.require(algorithm)
.withIssuer(getIssuer(selectedProps))
.acceptLeeway(60) // Allow 1 minute of clock skew
.build();




Spring Boot cache is an abstraction that allows us to cache data using annotations like @Cacheable. By default, it stores data in memory. Redis is a distributed in-memory data store that can be used as a cache provider. For single-instance applications, in-memory cache is sufficient. For microservices or multiple instances, Redis is preferred because it provides distributed, scalable, and persistent caching


Dependency Injection is a design pattern where dependencies are provided to a class from outside instead of the class creating them.
It helps achieve loose coupling, better testability, and maintainability.
In Spring Boot, DI is implemented using the IoC container and constructor injection is recommended.


Change detection in Angular is the mechanism that updates the DOM when component data changes.
By default Angular checks the entire component tree, but using OnPush strategy improves performance by checking only when input references change.
It is triggered by events, async operations, and observable emissions.


Spring Security is a framework that provides authentication and authorization for Java applications.
It works using a filter chain to validate users and control access based on roles.
It supports JWT, OAuth2, password encryption, and protects against common security attacks.



In our microservices architecture, we used Spring Cloud Gateway as the API Gateway. It acted as a single entry point for all client requests. It handled routing, JWT authentication, logging, and rate limiting. This reduced client complexity and centralized security management.

An API Gateway is a single entry point for all client requests in a microservices architecture. It sits between clients and backend services, handling cross-cutting concerns so individual services don't have to.
Core responsibilities I configure in production:
1. Routing: Routes /api/orders/** to Order Service, /api/users/** to User Service. Clients talk to one URL; the gateway knows where to forward.
2. Authentication & Authorization: Validates JWT tokens or OAuth2 at the gateway level. Backend services receive pre-validated requests with user context in headers — they don't each implement auth independently.
3. Rate Limiting: Protects backend services from abuse. I configure limits like 100 requests/minute per user to prevent any single client from overwhelming the system.
4. Load Balancing: Distributes requests across multiple instances of the same service.
5. Request/Response Transformation: Aggregating responses from multiple services into a single response, or adding/removing headers.
6. Circuit Breaking: Gateway-level circuit breakers for downstream services.

exclute package:

<dependency>
    <groupId>spring-boot-starter-web</groupId>
    <exclusions>
        <exclusion>spring-boot-starter-tomcat</exclusion>
    </exclusions>
</dependency>
<dependency>spring-boot-starter-jetty</dependency>

Large file upload?

* @Value — for 1-2 isolated properties (a feature flag, a single URL)
* @ConfigurationProperties — for a group of related properties (database config, API client settings)

For large files, I avoid uploading directly through application servers. Instead, I generate a pre-signed URL from backend and let the client upload directly to Amazon S3. This reduces memory usage, improves scalability, and prevents timeout issues. For very large files, I implement chunk-based upload with resume capability. Metadata is stored in DB and file processing happens asynchronously using message queues.

File Size	Approach
< 50MB	Normal Multipart
50MB–500MB	Streaming Upload
500MB–5GB	Pre-Signed URL
5GB+	Multipart/Chunk Upload

How do you configure CORS and Security in Spring Boot?
In our project, since frontend and backend run on different origins, we configured global CORS using CorsConfigurationSource and enabled it inside Spring Security. We disabled CSRF for REST APIs and used JWT-based stateless authentication. In microservices, we handled CORS at the API Gateway level to avoid duplicating configuration across services.


What is CSRF and how do you handle it in Spring Boot?
You can say:
CSRF is an attack where a malicious site tricks a logged-in user's browser into sending unwanted requests. It exploits automatically attached session cookies. In Spring Security, CSRF protection is enabled by default and works using CSRF tokens. However, in stateless REST APIs using JWT, we disable CSRF because authentication is header-based and not cookie-based.



What is the difference between @AllArgsConstructor, @RequiredArgsConstructor, and @NoArgsConstructor?
You can answer:
These are Lombok annotations used to generate constructors automatically. @NoArgsConstructor creates a default constructor, mainly required by JPA and serialization frameworks. @AllArgsConstructor generates a constructor with all fields. @RequiredArgsConstructor generates a constructor only for final or @NonNull fields, and it's commonly used in Spring Boot for constructor-based dependency injection.



What is @Configuration ?
@Configuration is used to define a Spring configuration class.
This class contains bean definitions that should be managed by the Spring container.

What is @Bean ?
@Bean is used to create and register an object (bean) inside the Spring container.
Usually placed inside a @Configuration class.



Dependency Injection is a design pattern where dependencies are provided to a class from outside instead of the class creating them. It helps achieve loose coupling, better testability, and maintainability. In Spring Boot, DI is implemented using the IoC container and constructor injection is recommended.




ANgular:


Purpose of Auth Guard in Angular
In Angular, an Auth Guard is used to control access to routes.
It prevents unauthorized users from accessing protected pages.
