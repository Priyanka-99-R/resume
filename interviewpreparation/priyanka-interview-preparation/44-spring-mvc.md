# Spring MVC — Interview Q&A (Easy Version)

> **How to use this file:** every question follows the same shape —
> **Easiest way to remember → simple explanation → real-world example → diagram → interview trap → Interview-ready answer → Easy memory box.**
> The **Quick Revision Sheet** at the end is what you read in the last ten minutes before the round.

> **Where this fits:** [06 — Spring & Spring Boot](./06-spring-boot.md) covers IoC/DI, auto-configuration, JPA and Security. **This file is only the web layer** — the DispatcherServlet, the annotations, the request flow, and the traps interviewers actually spring on you.

---

## Contents

1. [The Big Picture](#1-the-big-picture)
2. [The Request Flow — the question that decides the round](#2-the-request-flow--the-question-that-decides-the-round)
3. [Controllers & Mapping Annotations](#3-controllers--mapping-annotations)
4. [Reading the Request](#4-reading-the-request)
5. [Writing the Response](#5-writing-the-response)
6. [Validation](#6-validation)
7. [Exception Handling](#7-exception-handling)
8. [Interceptors, Filters & Cross-Cutting](#8-interceptors-filters--cross-cutting)
9. [Configuration & Contexts](#9-configuration--contexts)
10. [Views, Redirect & Forward](#10-views-redirect--forward)
11. [Files, Sessions, Async](#11-files-sessions-async)
12. [Testing the Web Layer](#12-testing-the-web-layer)
13. [Predict the Output / Trap Questions](#13-predict-the-output--trap-questions)
14. [Rapid-Fire Table](#14-rapid-fire-table)
15. [Quick Revision Sheet](#15-quick-revision-sheet)

---

# 1. The Big Picture

### Q: What is Spring MVC?

The easiest way to remember:

```
Spring MVC = the WEB layer of Spring.
It turns an HTTP request into a Java method call,
and a Java return value back into an HTTP response.
```

That one sentence is the whole framework. Everything else — annotations, converters, resolvers — exists to make those two arrows work.

Real-world idea: **a hotel reception desk.**

```
Guest walks in (HTTP request)
        ↓
RECEPTION decides who should handle this  ← DispatcherServlet
        ↓
Sends the guest to the right department   ← your @Controller method
        ↓
Department gives back a result
        ↓
RECEPTION formats it and hands it to the guest ← JSON / HTML response
```

The guest never talks to the departments directly. **Everything goes through reception.** That is the *Front Controller* pattern, and in Spring MVC reception is called the **DispatcherServlet**.

#### Interview-ready answer

> Spring MVC is Spring's web module, built on the Servlet API. It implements the front-controller pattern: a single servlet, the DispatcherServlet, receives every request, and delegates to the right handler method. My job as a developer is only to write a `@Controller` method and annotate it; Spring handles URL matching, binding request data to method parameters, converting the return value to JSON or a view, and mapping exceptions to responses. The same framework serves both server-rendered pages and REST APIs — `@RestController` is just `@Controller` plus `@ResponseBody`.

#### Easy memory

```
Spring MVC = REQUEST → METHOD → RESPONSE
One front door (DispatcherServlet), many rooms (controllers).
```

---

### Q: What do M, V and C actually mean here?

```
M = Model       → the DATA you want to show          (a User, a List<Order>)
V = View        → how it is PRESENTED                (JSON, HTML, Thymeleaf page)
C = Controller  → the DECIDER — takes input, calls the service, picks the model+view
```

Real-world idea: **a restaurant.**

```
CONTROLLER = the waiter   → takes your order, brings the food
MODEL      = the food     → the actual thing you wanted
VIEW       = the plating  → how it's presented on the plate
```

⚠️ **The trap:** the controller is **not** where business logic lives. Interviewers ask *"what goes in a controller?"* to check exactly this.

```
Controller  → read input, validate, call service, return result   (THIN)
Service     → business rules, transactions                        (THICK)
Repository  → database access                                     (DUMB)
```

#### Interview-ready answer

> Model is the data, View is the presentation, Controller is the coordinator. In practice I keep controllers thin — they extract and validate the request, delegate to a service, and shape the response. Business rules and `@Transactional` boundaries live in the service layer, database access in the repository. That keeps the business logic testable without any web infrastructure.

#### Easy memory

```
Controller = WAITER (thin)
Service    = KITCHEN (thick)
Repository = FRIDGE
```

---

### Q: What is the DispatcherServlet?

The easiest way to remember:

```
DispatcherServlet = the ONE servlet that receives EVERY request
                    and decides who handles it.
```

It is a real `HttpServlet`. In Spring Boot it is registered automatically and mapped to `/` — you never write it.

```
                     ┌──────────────────────┐
   HTTP request ───► │  DispatcherServlet   │ ◄── the FRONT CONTROLLER
                     └──────────┬───────────┘
                                │ asks its helpers
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
  HandlerMapping          HandlerAdapter           ViewResolver
  "WHO handles /users?"   "HOW do I call it?"      "WHICH page/format?"
```

#### The four helpers to name in the interview

| Helper | Question it answers |
|---|---|
| **HandlerMapping** | *Which* controller method matches this URL + method + headers? |
| **HandlerAdapter** | *How* do I actually invoke that handler (annotated method, plain `Controller`, function)? |
| **HandlerExceptionResolver** | Something threw — *what response* should the client get? |
| **ViewResolver** | The handler returned `"home"` — *which* template file is that? |

⚠️ **The trap:** *"Why do we need a HandlerAdapter at all — why doesn't the DispatcherServlet just call the method?"*
Because a handler is not always an annotated method. It can be an old `Controller` interface, an `HttpRequestHandler`, or a functional endpoint. The adapter isolates the DispatcherServlet from *how* the handler is shaped — classic **Adapter pattern**.

#### Interview-ready answer

> The DispatcherServlet is Spring MVC's front controller — a single servlet mapped to `/` that receives every request and orchestrates the work. It doesn't do the work itself: it consults a HandlerMapping to find the matching handler, a HandlerAdapter to invoke it, a set of HttpMessageConverters or a ViewResolver to render the result, and a HandlerExceptionResolver if something is thrown. In Spring Boot it is auto-registered by `DispatcherServletAutoConfiguration`, so I only write controllers.

#### Easy memory

```
DispatcherServlet = RECEPTION 🛎️
Mapping = WHO · Adapter = HOW · ViewResolver = WHAT IT LOOKS LIKE
```

---

### Q: Spring MVC vs Spring Boot — what's the difference?

```
Spring MVC  = the WEB FRAMEWORK      (the engine)
Spring Boot = the SETUP              (the car built around the engine)
```

| | Spring MVC (classic) | Spring Boot |
|---|---|---|
| DispatcherServlet | you register it (`web.xml` / `WebApplicationInitializer`) | auto-registered |
| Server | deploy a WAR to Tomcat | **embedded** Tomcat, run a JAR |
| Jackson, validator, converters | you declare them | auto-configured |
| Config | XML or `@EnableWebMvc` class | `spring-boot-starter-web` + `application.yml` |

⚠️ **The trap everyone falls into:** *"So `@EnableWebMvc` in a Boot app is a good idea?"*
**No.** `@EnableWebMvc` **switches off** Spring Boot's MVC auto-configuration. You suddenly lose static resource handling, the JSON message converters config, error handling defaults. In Boot you implement **`WebMvcConfigurer`** instead — it *adds* to the defaults rather than replacing them.

#### Interview-ready answer

> Spring MVC is the web framework; Spring Boot is opinionated auto-configuration on top of it. With Boot, `spring-boot-starter-web` brings in Spring MVC, Jackson, validation and an embedded Tomcat, and auto-configures the DispatcherServlet — so I write only controllers and properties. The key thing to know is that adding `@EnableWebMvc` to a Boot application disables that auto-configuration; to customise MVC in Boot I implement `WebMvcConfigurer`, which adds to the defaults instead of replacing them.

#### Easy memory

```
Boot = MVC + AUTO-CONFIG + EMBEDDED SERVER
In Boot: WebMvcConfigurer ✅   @EnableWebMvc ❌
```

---

# 2. The Request Flow — the question that decides the round

### Q: Explain the complete Spring MVC request flow.

> This is **the** Spring MVC question. If you can draw it, you pass the web-layer section. Learn the **7 steps**.

```
1  BROWSER          ──► GET /api/users/5
                         │
2  DispatcherServlet ◄───┘   (the front controller — receives everything)
                         │
3  HandlerMapping        │   "who handles GET /api/users/5?"
                         │   → UserController.getUser(Long id)
                         │
4  HandlerAdapter        │   binds path variable 5 → id, runs interceptors preHandle
                         │   INVOKES the method
                         │
5  CONTROLLER            │   calls the service → returns User object
                         │
6a REST  → HttpMessageConverter (Jackson) → JSON in the body ─┐
6b PAGE  → ViewResolver → /WEB-INF/user.jsp → render HTML ────┤
                         │                                     │
7  RESPONSE         ◄────┴─────────────────────────────────────┘
```

#### Say it in one breath

```
Request → DispatcherServlet → HandlerMapping → HandlerAdapter
       → Interceptor.preHandle → Controller → Service → Controller
       → (Converter for JSON | ViewResolver for HTML)
       → Interceptor.postHandle → afterCompletion → Response
```

🧠 **Memory hook — "**D**ispatcher **M**akes **A**ll **C**alls **C**ome **R**ight"**

```
D → Dispatcher
M → Mapping
A → Adapter
C → Controller
C → Converter (or ViewResolver)
R → Response
```

⚠️ **The trap:** *"Where does `@ControllerAdvice` sit in that flow?"*
At **step 6**, as a `HandlerExceptionResolver` — **after** the handler has been found and invoked. That is why an exception thrown in a **security filter** (which runs *before* the DispatcherServlet) is **never** seen by your `@ControllerAdvice`. Same reasoning applies to any `Filter`.

#### Interview-ready answer

> The request hits the DispatcherServlet, which is mapped to `/`. It asks the HandlerMapping which handler matches the URL, HTTP method and headers, and gets back a HandlerExecutionChain — the handler plus its interceptors. Interceptor `preHandle` runs, then the HandlerAdapter resolves the method arguments — path variables, request params, and the body deserialised by an HttpMessageConverter — and invokes the controller. For a REST endpoint the return value goes back through a message converter, typically Jackson, and is written to the response body; for a server-rendered page the logical view name goes to a ViewResolver. Interceptor `postHandle` and then `afterCompletion` run, and the response is flushed. If anything throws, a HandlerExceptionResolver — usually the one backing `@ControllerAdvice` — produces the response instead.

#### Easy memory

```
DISPATCHER → MAPPING → ADAPTER → CONTROLLER → CONVERTER → RESPONSE
Filters are OUTSIDE this. Interceptors are INSIDE.
```

---

### Q: Is the controller a singleton? Is it thread-safe?

```
YES singleton (one instance for the whole app)
     ↓
So it must be STATELESS — no mutable instance fields.
```

Every request runs on its **own thread** through the **same** controller object.

```
Thread-1 ──┐
Thread-2 ──┼──► ONE UserController instance
Thread-3 ──┘
```

⚠️ **The trap:** a field like `private User currentUser;` in a controller is a **live data-leak bug** — request A can see request B's user. Interviewers love this one.

```java
@RestController
class BadController {
    private int counter;          // ❌ shared across ALL requests — race condition
    private User currentUser;     // ❌ user A can see user B's data
}
```

The safe pattern: keep everything in **local variables and method parameters**, and inject dependencies as `final` constructor fields (those are read-only, so they're fine).

#### Interview-ready answer

> Controllers are singleton-scoped by default, so a single instance serves all requests, each on its own thread from the servlet container's pool. That means controllers must be stateless — mutable instance fields are shared across concurrent requests and cause data leaks between users. Request-scoped data belongs in local variables or method parameters; injected collaborators are fine because they're final and read-only.

#### Easy memory

```
ONE controller · MANY threads
👉 NO mutable fields. Locals only.
```

---

# 3. Controllers & Mapping Annotations

### Q: `@Controller` vs `@RestController`?

The easiest way to remember:

```
@RestController = @Controller + @ResponseBody
```

```
@Controller      → return value = a VIEW NAME  ("home" → home.html)
@RestController  → return value = the RESPONSE BODY (serialised to JSON)
```

⚠️ **The classic bug:** you use `@Controller`, return a `User` object, and get **404** or a weird view error. Because Spring treated `"user"` as a *view name*. Fix: `@RestController`, or add `@ResponseBody`.

```java
@Controller
class PageController {
    @GetMapping("/home")
    String home() { return "home"; }          // → resolves templates/home.html
}

@RestController                                // = @Controller + @ResponseBody
class UserApi {
    @GetMapping("/api/users/{id}")
    User get(@PathVariable Long id) { ... }   // → JSON body
}
```

#### Interview-ready answer

> `@RestController` is a convenience annotation that combines `@Controller` and `@ResponseBody`. With plain `@Controller`, a String return value is treated as a logical view name and passed to a ViewResolver; with `@RestController`, every return value is written directly to the response body through an HttpMessageConverter — Jackson for JSON. I use `@RestController` for APIs and `@Controller` only when the application renders server-side templates.

#### Easy memory

```
@Controller     → returns a PAGE NAME
@RestController → returns DATA (JSON)
```

---

### Q: `@RequestMapping` and its shortcuts

```
@RequestMapping = the general one (any method, any attribute)
@GetMapping / @PostMapping / @PutMapping / @PatchMapping / @DeleteMapping
                = shortcuts, since Spring 4.3
```

```java
@RestController
@RequestMapping("/api/users")           // class-level PREFIX
class UserController {

    @GetMapping                          // GET  /api/users
    List<User> all() { ... }

    @GetMapping("/{id}")                 // GET  /api/users/5
    User one(@PathVariable Long id) { ... }

    @PostMapping(consumes = "application/json",
                 produces = "application/json")
    User create(@RequestBody UserDto dto) { ... }
}
```

#### The attributes worth naming

| Attribute | Meaning |
|---|---|
| `value` / `path` | the URL pattern |
| `method` | GET / POST / … |
| `consumes` | what **Content-Type** I accept → mismatch = **415** |
| `produces` | what I can **return** → mismatch with `Accept` = **406** |
| `params` | only if a request param is present (`params = "version=2"`) |
| `headers` | only if a header matches |

⚠️ **The trap:** *"What if two methods map to the same URL?"*
Startup fails with **`Ambiguous mapping`** — a `BeanCreationException`. Spring cannot decide, so it refuses to start rather than pick randomly. (Different `produces`/`consumes`/`params` makes them **not** ambiguous — that's how API versioning by header works.)

#### Interview-ready answer

> `@RequestMapping` maps a request to a handler method and can be narrowed by method, path, headers, params, `consumes` and `produces`. Since 4.3 we use the composed shortcuts — `@GetMapping`, `@PostMapping` and so on — which are more readable and stop accidental method mismatches. I put the shared prefix at class level and the specific path at method level. If two handlers match identically Spring fails at startup with an ambiguous-mapping error, which is deliberate — it's caught at boot, not in production.

#### Easy memory

```
consumes = WHAT I EAT  (Content-Type) → 415 if wrong
produces = WHAT I SERVE (Accept)      → 406 if wrong
```

---

# 4. Reading the Request

### Q: `@PathVariable` vs `@RequestParam` vs `@RequestBody` — the most-asked trio

The easiest way to remember: **look at where the data sits in the URL.**

```
POST  /api/users/5/orders?status=NEW
                    │           │
                    │           └── @RequestParam  → after the ?
                    └────────────── @PathVariable  → part of the PATH

      { "item": "Book", "qty": 2 }   ← @RequestBody → the JSON payload
```

| | Where | Typical use | Missing → |
|---|---|---|---|
| `@PathVariable` | inside the path | **identify a resource** (`/users/5`) | no match → **404** |
| `@RequestParam` | query string / form field | **filter, sort, page** (`?page=2`) | **400** unless `required=false` or a `defaultValue` |
| `@RequestBody` | the request body | **create / update** a resource | **400** |

```java
@GetMapping("/users/{id}/orders")
List<Order> orders(@PathVariable Long id,
                   @RequestParam(defaultValue = "ALL") String status,
                   @RequestParam(required = false) Integer page) { ... }

@PostMapping("/users")
User create(@Valid @RequestBody UserDto dto) { ... }
```

⚠️ **Trap 1 — the name mismatch.** `@PathVariable Long userId` with a path of `/{id}` fails unless the code was compiled with `-parameters` **and** the names match. Be explicit when they differ: `@PathVariable("id") Long userId`.

⚠️ **Trap 2 — `Optional` and primitives.** `@RequestParam(required=false) int page` throws when the param is absent — a primitive can't be null. Use `Integer`, an `Optional<Integer>`, or a `defaultValue`.

⚠️ **Trap 3 — GET with a body.** `@RequestBody` on a `@GetMapping` "works" in Spring but is against HTTP semantics and many proxies drop the body. Use query params.

#### Interview-ready answer

> `@PathVariable` binds a segment of the URI template and is for identifying a resource; `@RequestParam` binds a query-string or form parameter and is for filtering, sorting and pagination; `@RequestBody` binds the whole request body, deserialised by an HttpMessageConverter — Jackson for JSON — and is used for POST and PUT payloads. `@RequestParam` supports `required=false` and `defaultValue`; a missing required param gives 400, while a path that doesn't match gives 404. I always pair `@RequestBody` with `@Valid` so payloads are validated before the method body runs.

#### Easy memory

```
PATH   = WHICH one     (/users/5)
PARAM  = HOW to filter (?status=NEW)
BODY   = the DATA      ({...})
```

---

### Q: What is `@ModelAttribute`? How is it different from `@RequestBody`?

```
@RequestBody     → JSON body    → object   (REST)
@ModelAttribute  → FORM fields  → object   (HTML forms)
```

`@ModelAttribute` does **data binding**: it creates the object, then matches each request parameter name to a setter.

```java
// form: name=Priya&age=28  →
@PostMapping("/register")
String register(@ModelAttribute UserForm form) { ... }
```

It has a **second use** — on a *method*, it puts a value into the model for **every** handler in that controller:

```java
@ModelAttribute("countries")
List<String> countries() { return List.of("India", "UK"); }  // available to every view
```

⚠️ **The trap:** *"which one for a REST API?"* → **`@RequestBody`**. `@ModelAttribute` reads `application/x-www-form-urlencoded` or `multipart`, not JSON.

#### Interview-ready answer

> `@ModelAttribute` binds flat request parameters — form fields or query string — onto an object by matching names to setters, and it's what server-rendered form submissions use. `@RequestBody` deserialises the entire body, normally JSON, through an HttpMessageConverter, and it's what REST APIs use. On a method rather than a parameter, `@ModelAttribute` pre-populates the model for every handler in the controller, which is handy for dropdown reference data.

#### Easy memory

```
FORM  → @ModelAttribute
JSON  → @RequestBody
```

---

### Q: What other things can a handler method take as a parameter?

```java
@GetMapping("/x")
String h(@RequestHeader("Authorization") String auth,   // one header
         @CookieValue("JSESSIONID") String sid,          // one cookie
         HttpServletRequest request,                     // raw servlet request
         HttpSession session,                            // the session
         Principal principal,                            // logged-in user (Security)
         Locale locale,                                  // resolved locale
         Model model,                                    // to add view data
         Pageable pageable) { ... }                      // Spring Data paging
```

🧠 **The rule:** Spring resolves each parameter with an **`HandlerMethodArgumentResolver`**, and you can write your own — that's how `@CurrentUser`-style custom annotations are built. Naming `HandlerMethodArgumentResolver` in the interview shows depth.

#### Easy memory

```
Every parameter is filled by an ARGUMENT RESOLVER.
Custom annotation? → write your own resolver.
```

---

# 5. Writing the Response

### Q: How does a Java object become JSON? (HttpMessageConverter)

The easiest way to remember:

```
HttpMessageConverter = the TRANSLATOR between Java objects and HTTP bytes.
```

```
REQUEST   JSON bytes  ──[MappingJackson2HttpMessageConverter]──►  UserDto object
RESPONSE  User object ──[MappingJackson2HttpMessageConverter]──►  JSON bytes
```

It is chosen by **content negotiation**:

```
Client sends:  Accept: application/json
                  ↓
Spring picks a converter that can WRITE application/json
                  ↓
Jackson serialises the object
```

⚠️ **Trap 1 — 406 Not Acceptable.** The client asked for `Accept: application/xml`, no XML converter is on the classpath → 406. Add `jackson-dataformat-xml` or fix the header.

⚠️ **Trap 2 — 415 Unsupported Media Type.** The client POSTed without `Content-Type: application/json`, so no converter can *read* it → 415.

⚠️ **Trap 3 — Jackson needs a no-arg constructor** (or `@JsonCreator`) to deserialise. A class with only an all-args constructor fails at runtime — the reason Lombok users add `@NoArgsConstructor`.

#### Interview-ready answer

> Serialisation is done by HttpMessageConverters. For `@ResponseBody` return values the DispatcherServlet performs content negotiation against the `Accept` header and picks a converter that can write that media type — normally `MappingJackson2HttpMessageConverter`, which is auto-configured by Boot when Jackson is on the classpath. The same mechanism runs in reverse for `@RequestBody` against `Content-Type`. A read mismatch gives 415, a write mismatch 406.

#### Easy memory

```
Converter = TRANSLATOR
415 = I can't READ what you sent   (Content-Type)
406 = I can't WRITE what you want  (Accept)
```

---

### Q: `ResponseEntity` — why use it instead of returning the object?

```
return user;                   → always 200 OK, no control over headers
return ResponseEntity...       → YOU control status + headers + body
```

```java
@PostMapping("/users")
ResponseEntity<User> create(@Valid @RequestBody UserDto dto) {
    User saved = service.save(dto);
    return ResponseEntity
            .created(URI.create("/api/users/" + saved.getId()))  // 201 + Location
            .body(saved);
}

@GetMapping("/users/{id}")
ResponseEntity<User> get(@PathVariable Long id) {
    return service.find(id)
                  .map(ResponseEntity::ok)                       // 200
                  .orElseGet(() -> ResponseEntity.notFound().build()); // 404
}
```

| Want to… | Use |
|---|---|
| just return data, always 200 | plain object |
| set status (201, 204, 404) | `ResponseEntity` |
| set headers (`Location`, `ETag`) | `ResponseEntity` |
| fixed status, no headers | `@ResponseStatus(HttpStatus.CREATED)` |

⚠️ **The trap:** *"`ResponseEntity` vs `@ResponseStatus`?"* — `@ResponseStatus` is **static** (same status every time, declared on the method or exception class); `ResponseEntity` is **dynamic** (decided per request). And note `@ResponseStatus` on a method is **ignored** if you return a `ResponseEntity` — the entity wins.

#### Interview-ready answer

> `ResponseEntity` wraps the body together with the status code and headers, so the same method can return 200, 201 with a Location header, or 404 depending on the outcome. `@ResponseStatus` is the static alternative — it fixes one status for a method or, more usefully, for a custom exception class. I use `ResponseEntity` in any handler whose status varies, which for a REST API is most of them.

#### Easy memory

```
ResponseEntity  = status + headers + body   (DYNAMIC)
@ResponseStatus = one fixed status          (STATIC)
```

---

# 6. Validation

### Q: How do you validate a request in Spring MVC?

The easiest way to remember:

```
@Valid on the parameter  +  constraints on the DTO  =  automatic 400
```

```java
public class UserDto {
    @NotBlank(message = "name is required")
    private String name;

    @Email
    private String email;

    @Min(18) @Max(100)
    private int age;

    @NotNull @Past
    private LocalDate dob;
}

@PostMapping("/users")
User create(@Valid @RequestBody UserDto dto) { ... }   // invalid → MethodArgumentNotValidException → 400
```

#### The three things interviewers check

**1. `@Valid` vs `@Validated`**

```
@Valid      → JSR-380 (Jakarta). Works on method params. Supports NESTED (@Valid on a field).
@Validated  → SPRING's. Adds validation GROUPS. Put it on the CLASS to validate
              @RequestParam / @PathVariable too.
```

**2. Where the error goes**

```
@RequestBody + @Valid fails  → MethodArgumentNotValidException → 400
@ModelAttribute + @Valid     → BindException
@Validated on params fails   → ConstraintViolationException  (→ 500 unless handled!)
```

**3. `BindingResult` — the trap**

```java
// If you add BindingResult, Spring STOPS throwing and hands YOU the errors.
@PostMapping("/users")
String create(@Valid @ModelAttribute UserDto dto, BindingResult result) {
    if (result.hasErrors()) return "form";   // you must check it yourself
    ...
}
```

⚠️ **The trap:** `BindingResult` **must be the parameter immediately after** the validated object. Put anything between them and Spring throws at startup. And if you add it and *forget* `hasErrors()`, invalid data flows straight into your service — a real production bug.

⚠️ **Nested objects are not validated automatically.** You need `@Valid` on the field too:

```java
public class OrderDto {
    @Valid                       // ← without this, address constraints are SKIPPED
    private AddressDto address;
}
```

#### Interview-ready answer

> I put Bean Validation constraints on the DTO and `@Valid` on the handler parameter. For a `@RequestBody`, a failure raises `MethodArgumentNotValidException`, which I handle in a `@RestControllerAdvice` to return a 400 with a field-to-message map, so the client gets a consistent error shape. `@Validated` is Spring's variant that adds validation groups and, when placed on the class, enables constraints directly on `@RequestParam` and `@PathVariable` — those raise `ConstraintViolationException`, which needs its own handler. Nested objects need `@Valid` on the field to be cascaded.

#### Easy memory

```
@Valid on the PARAM + constraints on the DTO → 400 automatically
BindingResult right AFTER the object → YOU handle it
Nested object? → @Valid on the FIELD too
```

---

# 7. Exception Handling

### Q: How do you handle exceptions in Spring MVC?

There are **three levels** — say all three and you sound senior.

```
1. @ResponseStatus on the exception class  → simplest, one status
2. @ExceptionHandler in a controller       → local to that controller
3. @RestControllerAdvice                   → GLOBAL, the real answer ⭐
```

```java
// 1 — simplest
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException { ... }

// 3 — the production pattern
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ApiError> notFound(UserNotFoundException ex) {
        return ResponseEntity.status(NOT_FOUND)
                             .body(new ApiError("USER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> invalid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exception.class)             // the safety net
    ResponseEntity<ApiError> any(Exception ex) {
        log.error("Unhandled", ex);                // log the stack trace…
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                             .body(new ApiError("INTERNAL", "Something went wrong"));
        //  …but NEVER return the stack trace to the client
    }
}
```

#### The precedence rule

```
MOST SPECIFIC handler wins.
Controller-local @ExceptionHandler BEATS @ControllerAdvice.
Between advices, order with @Order.
```

⚠️ **Trap 1 — the filter gap.** An exception thrown in a **Servlet filter** (e.g. a JWT filter in Spring Security) happens **before** the DispatcherServlet, so `@ControllerAdvice` never sees it. The client gets a bare 500 or the container's HTML error page. Fix: handle it inside the filter, or add a `FilterChainExceptionHandler` early in the chain.

⚠️ **Trap 2 — swallowing.** `@ExceptionHandler(Exception.class)` that returns a friendly message and **doesn't log** hides real bugs. Log the stack trace server-side, return a correlation id to the client.

⚠️ **Trap 3 — `@ControllerAdvice` vs `@RestControllerAdvice`.** The Rest one adds `@ResponseBody`, so handlers return JSON. With plain `@ControllerAdvice` in a REST app, a returned object is treated as a **view name**.

#### The modern option: `ProblemDetail` (Spring 6 / Boot 3)

```java
@ExceptionHandler(UserNotFoundException.class)
ProblemDetail handle(UserNotFoundException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(NOT_FOUND, ex.getMessage());
    pd.setTitle("User not found");
    pd.setProperty("userId", ex.getId());
    return pd;                                   // RFC 7807 application/problem+json
}
```

#### Interview-ready answer

> I centralise it in a `@RestControllerAdvice` with `@ExceptionHandler` methods, one per meaningful exception type, each returning a `ResponseEntity` with a consistent error DTO — code, message, timestamp, path. Domain exceptions map to 404 or 409, `MethodArgumentNotValidException` to a 400 with a field-to-message map, and a catch-all `Exception` handler logs the stack trace and returns a generic 500 with a correlation id, never internal details. Controller-local handlers take precedence over the advice, and on Spring Boot 3 I use `ProblemDetail` so responses follow RFC 7807. The one gap to remember is that exceptions thrown in a servlet filter happen before the DispatcherServlet, so the advice can't see them.

#### Easy memory

```
@RestControllerAdvice = ONE place for ALL errors 🎯
Specific beats general · Log the stack, return a message
Filters happen BEFORE the advice — the advice can't catch them
```

---

### Q: `ResponseStatusException` — when would you use it?

```java
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + id + " not found");
```

```
Custom exception class + @ControllerAdvice → for DOMAIN errors you handle everywhere
ResponseStatusException                    → for ONE-OFF cases, no new class needed
```

⚠️ Don't overuse it: throwing HTTP-specific exceptions from the **service layer** leaks web concerns into business code. Throw a domain exception there, translate it in the advice.

#### Easy memory

```
ResponseStatusException = a quick 404 without writing a class.
Service layer? → domain exception, translate in the advice.
```

---

# 8. Interceptors, Filters & Cross-Cutting

### Q: Filter vs Interceptor vs AOP — the classic comparison

The easiest way to remember: **three security rings around your method.**

```
   ┌──────────────── FILTER (Servlet API) ─ knows nothing about Spring ──────┐
   │   ┌────────── INTERCEPTOR (Spring MVC) ─ knows the HANDLER ──────────┐  │
   │   │   ┌────── AOP @Around ─ knows the METHOD + ARGUMENTS ──────────┐ │  │
   │   │   │                    your controller method                   │ │  │
   │   │   └─────────────────────────────────────────────────────────────┘ │  │
   │   └────────────────────────────────────────────────────────────────────┘  │
   └───────────────────────────────────────────────────────────────────────────┘
```

| | Filter | Interceptor | AOP |
|---|---|---|---|
| Belongs to | Servlet API | Spring MVC | Spring Core |
| Runs | before DispatcherServlet | inside it, around the handler | around any bean method |
| Knows | request/response bytes | which **handler** will run | method + arguments |
| Can modify the body? | yes (wrap request/response) | not easily | yes (return value) |
| Typical use | **CORS, security, compression, logging raw** | **auth checks, timing, adding common model data** | **transactions, logging service methods, retry** |

```java
public class TimingInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        req.setAttribute("start", System.currentTimeMillis());
        return true;                     // ⚠️ false = STOP, request goes no further
    }

    public void postHandle(...) { }      // after handler, BEFORE view render
    public void afterCompletion(...) { } // ALWAYS runs — even if an exception was thrown
}

@Configuration
class WebConfig implements WebMvcConfigurer {
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TimingInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health");
    }
}
```

⚠️ **Trap 1:** `preHandle` returning **`false`** aborts the request — you must write the response yourself, or the client gets an empty 200.
⚠️ **Trap 2:** `postHandle` does **not** run if the handler threw. `afterCompletion` **does** — that's where cleanup and MDC clearing go.
⚠️ **Trap 3:** Spring Security is a **filter chain**, so it runs **before** every interceptor. That's why an authentication failure never reaches your interceptor or your `@ControllerAdvice`.

#### Interview-ready answer

> A filter is a Servlet API component that wraps the entire request outside Spring MVC — it can see and modify the raw request and response, and it's where Spring Security and CORS sit. An interceptor is a Spring MVC concept: it runs inside the DispatcherServlet, has access to the resolved handler, and gives me `preHandle`, `postHandle` and `afterCompletion`. AOP works one level deeper, around any Spring bean method with access to the arguments and return value, and is what `@Transactional` uses. The ordering is filter → interceptor → AOP → controller, so anything thrown in a filter can't be handled by `@ControllerAdvice`.

#### Easy memory

```
FILTER      = OUTSIDE Spring   (security, CORS)
INTERCEPTOR = INSIDE MVC       (knows the handler)
AOP         = around the METHOD (knows the arguments)
preHandle FALSE = stop · afterCompletion ALWAYS runs
```

---

### Q: How do you handle CORS in Spring MVC?

```
CORS = the BROWSER refusing a cross-origin response. It's a browser rule, not a server error.
Your server is fine — the browser blocks the JS from reading it.
```

Three ways:

```java
// 1 — per endpoint / controller
@CrossOrigin(origins = "https://app.example.com")
@RestController class UserApi { ... }

// 2 — global (preferred)
@Configuration
class WebConfig implements WebMvcConfigurer {
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://app.example.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

⚠️ **Trap 1 — the preflight.** A non-simple request (custom headers, PUT/DELETE, JSON content type) triggers an `OPTIONS` **preflight** first. If your security config blocks `OPTIONS`, CORS breaks even though the config looks right.

⚠️ **Trap 2 — `allowCredentials(true)` + `allowedOrigins("*")` is illegal.** The browser rejects it. Use `allowedOriginPatterns("*")` or list the origins.

⚠️ **Trap 3 — with Spring Security you must also enable it there** (`http.cors(...)`), otherwise the security filter chain rejects the request before MVC's CORS handling runs.

#### Easy memory

```
CORS = the BROWSER's rule, not the server's
Preflight = OPTIONS — don't block it
credentials + "*" = ILLEGAL
```

---

# 9. Configuration & Contexts

### Q: How was the DispatcherServlet registered before Spring Boot?

Three generations — knowing all three shows you understand the history:

```
1. web.xml                       (XML, Servlet 2.x)
2. WebApplicationInitializer     (Java, Servlet 3.0+ — no XML)
3. Spring Boot                   (automatic)
```

```xml
<!-- 1 — web.xml -->
<servlet>
  <servlet-name>dispatcher</servlet-name>
  <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
  <init-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>/WEB-INF/spring-servlet.xml</param-value>
  </init-param>
  <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
  <servlet-name>dispatcher</servlet-name>
  <url-pattern>/</url-pattern>
</servlet-mapping>
```

```java
// 2 — Java config, no web.xml
public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    protected Class<?>[] getRootConfigClasses()    { return new Class[]{ RootConfig.class }; }
    protected Class<?>[] getServletConfigClasses() { return new Class[]{ WebConfig.class }; }
    protected String[]   getServletMappings()      { return new String[]{ "/" }; }
}
```

⚠️ **The trap:** *"Why `/` and not `/*`?"*
`/` is the **default servlet mapping** — it handles everything **not** matched by a more specific mapping, and still lets the container's default servlet serve static files. `/*` would swallow JSPs and static resources too and break them.

---

### Q: What are the two application contexts in Spring MVC?

```
ROOT context     (parent)  → services, repositories, datasource, transactions
SERVLET context  (child)   → controllers, view resolvers, MVC infrastructure
```

```
        ┌──────────── ROOT (parent) ────────────┐
        │  @Service  @Repository  DataSource    │
        └──────────────────▲────────────────────┘
                           │ child can see parent
        ┌──────────────────┴────────────────────┐
        │  SERVLET (child): @Controller,         │
        │  ViewResolver, HandlerMapping          │
        └────────────────────────────────────────┘
```

🧠 **The rule that matters: the child sees the parent, the parent does NOT see the child.** So a controller can inject a service, but a service cannot inject a controller.

⚠️ **The classic legacy bug:** component-scanning the **same** package in both configs creates **two copies** of your services — and the transactional/AOP proxy is only applied in one, so `@Transactional` silently stops working. Fix: root scans services, servlet config scans controllers only.

> **In Spring Boot** there is normally just **one** context, so this question is really about legacy apps — but interviewers still ask it, and the parent/child rule is the answer.

#### Easy memory

```
ROOT = services (parent)   ·   SERVLET = controllers (child)
Child sees parent. Parent does NOT see child.
Boot = one context.
```

---

### Q: How do you customise Spring MVC in a Boot app?

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    public void addInterceptors(InterceptorRegistry r)      { ... }  // interceptors
    public void addCorsMappings(CorsRegistry r)             { ... }  // CORS
    public void addResourceHandlers(ResourceHandlerRegistry r){ ... } // static files
    public void addFormatters(FormatterRegistry r)          { ... }  // String → LocalDate etc.
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> r) { ... }
    public void configureMessageConverters(List<HttpMessageConverter<?>> c)  { ... }
}
```

```
WebMvcConfigurer  → ADDS to Boot's defaults              ✅
@EnableWebMvc     → REPLACES them (turns auto-config off) ❌ in Boot
```

#### Easy memory

```
Boot customisation = implement WebMvcConfigurer.
Never @EnableWebMvc in Boot.
```

---

# 10. Views, Redirect & Forward

### Q: What is a ViewResolver?

```
Controller returns "home"   ← a LOGICAL view name
        ↓
ViewResolver: prefix + name + suffix
        ↓
/WEB-INF/views/home.jsp    ← the actual file
```

```properties
spring.mvc.view.prefix=/WEB-INF/views/
spring.mvc.view.suffix=.jsp
```

With **Thymeleaf** (the Boot default) it's `templates/home.html` and nothing to configure.

⚠️ **The trap:** if you have `@RestController`, **no ViewResolver runs at all** — the return value goes to a message converter instead. Mixing them up is why people see `"home"` as a literal JSON string in the browser.

---

### Q: `redirect:` vs `forward:` — the difference in one picture

```
FORWARD  (server-side)                 REDIRECT (client-side)
─────────────────────                  ─────────────────────
browser ──► /save                      browser ──► /save
             │ forwards internally                  │
             ▼                                      ◄── 302 + Location: /list
          /list renders                  browser ──► /list  (SECOND request)
             │                                      ▼
             ◄── ONE response                     renders

URL stays  /save     ❗                URL becomes /list  ✅
Request attributes SURVIVE            New request — attributes are LOST
1 round trip                          2 round trips
```

```java
return "redirect:/users";   // 302
return "forward:/users";    // internal, same request
```

⚠️ **The trap — POST/REDIRECT/GET.** After a successful form POST you **must** redirect, not forward. Otherwise the URL still points at the POST and pressing **refresh re-submits the form** — a duplicate order. Redirect after POST is the fix.

⚠️ **How do you pass data across a redirect** (since attributes are lost)? → **`RedirectAttributes` flash attributes**, stored in the session for exactly one subsequent request:

```java
@PostMapping("/users")
String create(UserForm f, RedirectAttributes ra) {
    service.save(f);
    ra.addFlashAttribute("message", "User created");   // survives ONE redirect
    return "redirect:/users";
}
```

#### Interview-ready answer

> A forward is server-side: the same request is dispatched to another handler, the browser sees one response and the URL doesn't change, so request attributes survive. A redirect sends a 302 with a Location header, and the browser makes a brand-new request — the URL changes and request attributes are gone. The practical rule is post/redirect/get: after a state-changing POST I redirect, so refreshing the result page doesn't resubmit the form. To carry a message across the redirect I use `RedirectAttributes.addFlashAttribute`, which stores it in the session for exactly one request.

#### Easy memory

```
FORWARD  = same request, URL unchanged   (1 trip)
REDIRECT = new request, URL changes      (2 trips)
After POST → REDIRECT (or refresh resubmits!)
Data across a redirect → FLASH attributes
```

---

# 11. Files, Sessions, Async

### Q: How do you handle file upload?

```java
@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) return ResponseEntity.badRequest().body("empty");
    String name = file.getOriginalFilename();
    file.transferTo(Path.of("/uploads/" + UUID.randomUUID() + "-" + name));
    return ResponseEntity.ok("uploaded");
}
```

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

⚠️ **Trap 1 — the size limit.** Exceeding it throws `MaxUploadSizeExceededException`; handle it in your advice or the client gets a raw 500.
⚠️ **Trap 2 — `getOriginalFilename()` is attacker-controlled.** `../../etc/passwd` is a path-traversal attack. Never use it as the stored filename — generate your own.

---

### Q: How do you keep data across requests? (`@SessionAttributes`, scopes)

```
Request scope  → one request        (default for @RequestScope beans)
Session scope  → one user's session
Application    → the whole app (singleton)
```

```java
@Controller
@SessionAttributes("cart")            // keeps "cart" in the HTTP session
class CartController { ... }
```

⚠️ **The trap:** *"Should a REST API use the session?"* → **No.** REST is **stateless**: each request carries its own auth (a JWT) and no server-side session, which is what makes horizontal scaling easy. Server sessions force sticky sessions or a shared session store (Spring Session + Redis).

#### Easy memory

```
REST = STATELESS. Token in, no session.
Session = only for server-rendered apps (or Spring Session + Redis).
```

---

### Q: How does Spring MVC handle async requests?

```
Normal MVC = ONE THREAD per request, BLOCKED until the response is written.
Async MVC  = the servlet thread is RELEASED; another thread finishes the work.
```

```java
@GetMapping("/slow")
Callable<String> slow() {                      // released, run on a task executor
    return () -> service.longCall();
}

@GetMapping("/events")
SseEmitter stream() { ... }                    // server-sent events

@GetMapping("/x")
CompletableFuture<User> x() {                  // most common today
    return CompletableFuture.supplyAsync(() -> service.find());
}
```

⚠️ **The trap:** async MVC frees the **servlet container thread**, but the work still runs on **some** thread — it improves *connection* scalability, not CPU throughput. Truly non-blocking end-to-end is **WebFlux** ([see 06](./06-spring-boot.md#q-spring-webflux-vs-spring-mvc)). And on **Java 21 + Boot 3.2**, `spring.threads.virtual.enabled=true` gives most of the benefit while keeping simple blocking code ([see 40](./40-java21-features.md)).

#### Easy memory

```
MVC       = thread per request (blocks)
Async MVC = servlet thread released, work continues elsewhere
WebFlux   = non-blocking all the way (only if the WHOLE chain is)
Java 21   = virtual threads → blocking code, cheap threads
```

---

# 12. Testing the Web Layer

### Q: How do you test a Spring MVC controller?

```
@WebMvcTest      → loads ONLY the web layer (fast) — services are mocked
@SpringBootTest  → loads the WHOLE context (slow) — for integration tests
```

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean UserService service;        // @MockBean before Boot 3.4

    @Test
    void returnsUser() throws Exception {
        given(service.find(1L)).willReturn(new User(1L, "Priya"));

        mockMvc.perform(get("/api/users/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Priya"));
    }

    @Test
    void rejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"\"}"))
               .andExpect(status().isBadRequest());
    }
}
```

⚠️ **The trap:** `@WebMvcTest` does **not** create your `@Service` beans — if you don't mock a dependency the context fails to start. That's the point: it keeps the test focused on mapping, binding, validation and status codes.

#### Interview-ready answer

> For the web layer I use `@WebMvcTest` with MockMvc: it loads only the MVC infrastructure and the controller under test, so it's fast, and I mock the service layer. That lets me assert exactly what a controller is responsible for — URL mapping, parameter binding, validation returning 400, the right status code and the JSON shape via jsonPath. For a full path through service and repository I use `@SpringBootTest` with a Testcontainers database, but far fewer of those.

#### Easy memory

```
@WebMvcTest + MockMvc = web layer only, services MOCKED
@SpringBootTest       = the whole app (slow, use sparingly)
```

---

# 13. Predict the Output / Trap Questions

> These are asked as *"what happens if…"*. One line each — learn the answer, not the theory.

| # | Situation | What actually happens |
|---|---|---|
| 1 | `@Controller` returning a `User` object | Spring treats `"user"` as a **view name** → 404 / view error. Needs `@ResponseBody` |
| 2 | Two methods mapped to the exact same URL+method | **App fails to start** — `Ambiguous mapping` |
| 3 | POST JSON without `Content-Type: application/json` | **415** Unsupported Media Type |
| 4 | `Accept: application/xml`, only Jackson JSON on classpath | **406** Not Acceptable |
| 5 | Required `@RequestParam` missing | **400** Bad Request |
| 6 | `@PathVariable` doesn't match the URL template | **404** — no handler matched |
| 7 | `@RequestParam(required=false) int page` and param absent | **500** — can't assign null to a primitive. Use `Integer` |
| 8 | Mutable field in a `@RestController` under load | **Data leak between users** — controller is a singleton |
| 9 | `@Valid` on a DTO with a nested object, no `@Valid` on the field | Nested constraints are **silently skipped** |
| 10 | `BindingResult` declared but `hasErrors()` never checked | Invalid data flows through — **no 400 at all** |
| 11 | `BindingResult` not immediately after the validated param | **Startup failure** |
| 12 | Exception thrown in a servlet **filter** | `@ControllerAdvice` **never sees it** → raw 500 |
| 13 | `@ControllerAdvice` (not `@RestControllerAdvice`) in a REST app | Return value treated as a **view name** |
| 14 | `preHandle` returns `false` and writes nothing | Client gets an **empty 200** |
| 15 | Handler throws → does `postHandle` run? | **No.** `afterCompletion` **does** |
| 16 | `@EnableWebMvc` added to a Spring Boot app | Auto-config **disabled** — static resources, converters, error handling break |
| 17 | Forward after a POST, then user hits refresh | **Form re-submitted** — duplicate record. Use redirect |
| 18 | Request attribute read after a `redirect:` | **Gone** — new request. Use flash attributes |
| 19 | `allowCredentials(true)` + `allowedOrigins("*")` | **Browser rejects it** — illegal combination |
| 20 | `@Transactional` on a controller method | Works, but wrong layer — put it on the **service** |
| 21 | Jackson DTO with only an all-args constructor | **Deserialisation fails** — needs a no-arg constructor or `@JsonCreator` |
| 22 | `@ResponseStatus(CREATED)` on a method returning `ResponseEntity` | The **ResponseEntity wins**; the annotation is ignored |
| 23 | Same package component-scanned in both root and servlet contexts (legacy) | **Duplicate beans** — `@Transactional` silently stops working |
| 24 | `@RequestBody` on a `@GetMapping` | Works in Spring, but proxies may drop the body — **don't** |
| 25 | `file.getOriginalFilename()` used directly as the save path | **Path traversal vulnerability** (`../../`) |

---

# 14. Rapid-Fire Table

| Question | One-line answer |
|---|---|
| What pattern is the DispatcherServlet? | **Front Controller** |
| What pattern is the HandlerAdapter? | **Adapter** |
| Default DispatcherServlet mapping | `/` |
| `@RestController` = ? | `@Controller` + `@ResponseBody` |
| Who converts an object to JSON? | `HttpMessageConverter` (Jackson) |
| Who picks the JSP/HTML file? | `ViewResolver` |
| Who finds the handler? | `HandlerMapping` (`RequestMappingHandlerMapping`) |
| Who invokes it? | `HandlerAdapter` |
| Who fills a method parameter? | `HandlerMethodArgumentResolver` |
| Controller scope | **Singleton** — keep it stateless |
| Validation annotation | `@Valid` (Jakarta) / `@Validated` (Spring, adds groups) |
| Global error handling | `@RestControllerAdvice` + `@ExceptionHandler` |
| RFC 7807 error type (Boot 3) | `ProblemDetail` |
| Filter vs interceptor | Filter = outside Spring · Interceptor = inside MVC, knows the handler |
| Interceptor method that always runs | `afterCompletion` |
| Data across a redirect | `RedirectAttributes.addFlashAttribute` |
| Wrong `Content-Type` | **415** |
| Unsupported `Accept` | **406** |
| Missing required param | **400** |
| No handler for the URL | **404** |
| Wrong HTTP method | **405** |
| Customise MVC in Boot | implement `WebMvcConfigurer` (**never** `@EnableWebMvc`) |
| Test the web layer | `@WebMvcTest` + `MockMvc` |
| File upload type | `MultipartFile` |
| Async return types | `Callable`, `DeferredResult`, `CompletableFuture`, `SseEmitter` |
| Two contexts (legacy) | Root = services (parent) · Servlet = controllers (child) |

---

# 15. Quick Revision Sheet

> **Read this in the last ten minutes.**

```
① THE FLOW (draw this if given a whiteboard)

Request → DispatcherServlet → HandlerMapping → HandlerAdapter
       → preHandle → CONTROLLER → Service
       → HttpMessageConverter (JSON) | ViewResolver (HTML)
       → postHandle → afterCompletion → Response

🧠 Dispatcher Makes All Calls Come Right
   D-M-A-C-C-R
```

```
② THE FOUR HELPERS
HandlerMapping           → WHO handles it
HandlerAdapter           → HOW to call it
HttpMessageConverter     → object ⇄ JSON
ViewResolver             → view name → file
```

```
③ THE ANNOTATION MAP
@RestController = @Controller + @ResponseBody
@PathVariable  → /users/5        (WHICH one)
@RequestParam  → ?status=NEW     (HOW to filter)
@RequestBody   → { json }        (the DATA)
@ModelAttribute→ form fields
@Valid         → validate → 400
@RestControllerAdvice → all errors in one place
ResponseEntity → status + headers + body
```

```
④ THE STATUS CODES THEY ASK
400 missing/invalid param or body   404 no handler / no resource
405 wrong HTTP method               406 can't produce your Accept
409 conflict (duplicate)            415 can't read your Content-Type
```

```
⑤ THE FIVE TRAPS THAT WIN THE ROUND
1. Controller is a SINGLETON → no mutable fields (data leak)
2. @ControllerAdvice can't catch FILTER exceptions (Security runs first)
3. @EnableWebMvc in Boot DISABLES auto-configuration
4. After a POST, REDIRECT — a forward means refresh resubmits
5. BindingResult must come IMMEDIATELY after the validated object,
   and you must call hasErrors() yourself
```

```
⑥ THE THREE RINGS
FILTER (outside Spring) → INTERCEPTOR (inside MVC) → AOP (around the method)
preHandle false = STOP · afterCompletion ALWAYS runs
```

```
⑦ THE ONE-LINERS TO SAY OUT LOUD
"Spring MVC turns an HTTP request into a Java method call and the
 return value back into an HTTP response."
"The DispatcherServlet is the front controller — it delegates,
 it doesn't do the work."
"Controllers are thin: read, validate, delegate. Business logic
 and transactions live in the service."
"Controllers are singletons, so they must be stateless."
```

---

**Related files:** [06 — Spring & Spring Boot](./06-spring-boot.md) · [24 — ORM / JPA / Hibernate](./24-orm-jpa-hibernate.md) · [08 — Microservices](./08-microservices-basics.md) · [29 — .NET MVC (the same concepts, other stack)](./29-dotnet-mvc-interview-questions.md)
