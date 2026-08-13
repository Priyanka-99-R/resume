# 3. Back-End (.NET / C# Web API) — Interview Q&A tied to Subsea

**Honest framing up front:** I'm **Angular-emphasis full-stack**. I **owned Schedule-Manager end to end** — the Angular UI *and* the .NET Web API endpoints behind it. So I can tell you exactly how what I built worked: the controllers, the `DataSourceRequest` handling, the data flow, the error shape. Where I'd defer is deep C# runtime/GC internals — I'd bring in a backend specialist for that. Interviewers respect this framing far more than bluffing.

**Stack:** .NET (C#) Web API, SQL data store, Kendo server-side extensions, containerized with Docker.

---

## Q1. What is a Web API controller and how did you build your endpoints?

**Answer.** A **Web API controller** is a C# class (inheriting `ControllerBase`, marked `[ApiController]`) whose methods (**actions**) handle HTTP requests. Attributes map HTTP verbs and routes to methods:
- **`[HttpGet]`** — read
- **`[HttpPost]`** — create
- **`[HttpPut]`** — update
- **`[HttpDelete]`** — delete
- **`[Route("...")]`** — the URL template

The action returns data (serialized to JSON) or an `IActionResult`/`ActionResult<T>` so I can control the status code.

**In Subsea.** I built the Schedule-Manager endpoints — the schedule GET that the Kendo grid calls, plus create/update for schedule entries and the category/subcategory lookups:

```csharp
[ApiController]
[Route("api/schedule")]
public class ScheduleController : ControllerBase
{
    private readonly IScheduleService _service;
    public ScheduleController(IScheduleService service) => _service = service;

    [HttpGet]
    public DataSourceResult Get([DataSourceRequest] DataSourceRequest request)
        => _service.QuerySchedules().ToDataSourceResult(request);

    [HttpPost]
    public ActionResult<ScheduleDto> Create([FromBody] ScheduleDto dto)
    {
        var created = _service.Create(dto);
        return CreatedAtAction(nameof(Get), new { id = created.Id }, created);   // 201
    }

    [HttpPut("{id}")]
    public IActionResult Update(int id, [FromBody] ScheduleDto dto)
    {
        _service.Update(id, dto);
        return NoContent();   // 204
    }
}
```

`[FromBody]` binds the JSON request body to the DTO; the route/verb attributes map the URL and method.

---

## Q2. Explain the `DataSourceRequest` handling on the .NET side.

**Answer.** This is the server half of my Kendo server-side story (full version in `02-kendo-grid-deep-dive.md`). The **`[DataSourceRequest]`** attribute is a Kendo model binder that deserializes the grid's state from the querystring — page, sort descriptors, filter descriptors, group descriptors — into a `DataSourceRequest` object. Then **`ToDataSourceResult(request)`**, called on an **`IQueryable`**, composes that into a single SQL query (filter → sort → paging) and returns a `DataSourceResult` with the page of `Data` and the total `Total` count.

**In Subsea.** The whole reason the grid stayed fast on large datasets is that this ran against `IQueryable` so paging/sort/filter executed **in SQL**, not in memory — only one page of rows came back. I built that endpoint; it was the backend counterpart to the frontend grid I also owned.

---

## Q3. Interface vs abstract class in C# — when do you use each? *(they've asked this)*

**Answer.** Both let you program against an abstraction, but they answer different questions:

| | Interface | Abstract class |
|---|---|---|
| Represents | a **capability / contract** ("can-do") | a **base type** ("is-a") |
| Implementation | traditionally **none** (pure contract)\* | **can have** implemented members + abstract ones |
| Fields/state | no instance fields | can have fields, constructors, state |
| Inheritance | a class can implement **many** | a class inherits **one** |
| Use when | unrelated types share a capability | related types share a common base + code |

\*Modern C# allows default interface methods, but I treat interfaces as contracts.

**Rule of thumb:** use an **interface** to declare *what* something can do when implementers are otherwise unrelated; use an **abstract class** when you have a family of related types that should share real implementation and only override the parts that differ.

**In Subsea.** My services were defined behind **interfaces** (e.g. `IScheduleService`) — that's a contract, and it's what let me inject them via DI and keep controllers decoupled from the concrete implementation. That interface-based design is also what makes the code testable. I'd reach for an **abstract class** if I had several service types sharing a lot of common logic with a few abstract hooks — a shared base with template behavior.

---

## Q4. Explain `try / catch / finally`. *(they've asked this)*

**Answer.** It's structured exception handling:
- **`try`** — wraps code that might throw.
- **`catch`** — runs only if a matching exception is thrown; where I handle/log/translate it.
- **`finally`** — **always runs**, whether or not an exception was thrown (and even if one is rethrown or the method returns) — for **cleanup**: releasing resources, closing connections, resetting state.

```csharp
public ScheduleDto Update(int id, ScheduleDto dto)
{
    try
    {
        return _repository.Update(id, dto);          // risky work
    }
    catch (NotFoundException ex)
    {
        _logger.LogWarning(ex, "Schedule {Id} not found", id);
        throw;                                        // rethrow → global handler maps it to 404
    }
    finally
    {
        _logger.LogInformation("Update attempt finished for {Id}", id);   // always runs
    }
}
```

**Key point:** `finally` is guaranteed cleanup. In modern C# I'd often prefer a **`using`** statement for `IDisposable` resources (it's `try/finally` with automatic `Dispose()`), but the semantics are the same — deterministic cleanup.

**In Subsea.** I used `try/catch` around risky operations in the service layer, and let exceptions bubble to a **global handler** (next question) rather than swallowing them — so callers got a clean typed error, not a broken response. `finally`/`using` was for making sure resources were released regardless of outcome.

---

## Q5. How did you do global exception handling on the backend?

**Answer.** Rather than `try/catch` in every controller, I centralize it so **one place** turns any unhandled exception into a **uniform JSON error** with the right HTTP status — the frontend then always gets a predictable shape (not a raw 500 stack trace). In .NET this is either **exception-handling middleware** in the pipeline or an **exception filter**.

```csharp
// exception-handling middleware — one place for all unhandled errors
public class ExceptionMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<ExceptionMiddleware> _logger;

    public ExceptionMiddleware(RequestDelegate next, ILogger<ExceptionMiddleware> logger)
    { _next = next; _logger = logger; }

    public async Task Invoke(HttpContext ctx)
    {
        try { await _next(ctx); }                       // run the rest of the pipeline
        catch (NotFoundException ex) { await Write(ctx, 404, ex.Message); }
        catch (ValidationException ex) { await Write(ctx, 400, ex.Message); }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Unhandled exception");
            await Write(ctx, 500, "An unexpected error occurred.");   // no stack trace leaked
        }
    }

    private static Task Write(HttpContext ctx, int status, string message)
    {
        ctx.Response.StatusCode = status;
        ctx.Response.ContentType = "application/json";
        return ctx.Response.WriteAsync(JsonSerializer.Serialize(new { message }));
    }
}
```

**In Subsea.** The Angular side (interceptor + `catchError`) depended on the backend returning a clean `{ message }` with a correct status. This central handler is what produced that — so a validation failure came back as a 400 with a readable message, and the grid/forms showed a toast instead of breaking. The two halves (`01-frontend.md` Q10 and this) are the same story from both ends.

---

## Q6. Dependency injection in .NET — how does it work and did you use it?

**Answer.** .NET has a **built-in DI container**. You **register** services at startup and the framework **injects** them (usually via constructor) wherever they're needed, so classes depend on **abstractions (interfaces)**, not concrete types — which makes them decoupled and testable. Three lifetimes:
- **`AddSingleton`** — one instance for the whole app.
- **`AddScoped`** — one instance per HTTP request.
- **`AddTransient`** — a new instance every time it's resolved.

```csharp
// Program.cs / Startup
builder.Services.AddScoped<IScheduleService, ScheduleService>();
builder.Services.AddScoped<ICategoryService, CategoryService>();
```

```csharp
// injected via constructor — controller depends on the interface, not the implementation
public ScheduleController(IScheduleService service) => _service = service;
```

**In Subsea.** My controllers took their services (`IScheduleService`, category service) via **constructor injection**, registered in the app's startup. It's conceptually the same idea as Angular's `providedIn: 'root'` + constructor injection — which is part of why moving between the two sides felt natural to me. I'd typically register data/service classes as **scoped** (per request) so each request got a clean unit of work.

---

## Q7. How were the layers organized?

**Answer.** Standard layering:
- **Controller** — HTTP concerns only: bind the request, call a service, return a status.
- **Service** — business logic (interface-defined, DI-injected).
- **Repository / data access** — talks to SQL (returns `IQueryable` where I wanted composable queries like the grid's `ToDataSourceResult`).
- **DTOs** — the shapes crossing the API boundary, kept separate from internal entities.

**In Subsea.** Schedule-Manager followed this — thin controllers, an `IScheduleService` for the logic, data access returning `IQueryable` so the Kendo server-side extension could push paging/sort/filter into SQL. Keeping controllers thin also made the endpoints easy to reason about when I was fixing production bugs.

---

## Q8. What are DTOs and why not return the entity directly?

**Answer.** A **DTO (Data Transfer Object)** is a purpose-built shape for the API boundary. I don't return internal entities directly because: (1) it decouples the API contract from the database schema so I can change one without breaking the other, (2) it avoids over-exposing fields the client shouldn't see, and (3) it prevents serialization issues from entity navigation properties/cycles.

**In Subsea.** The schedule and category endpoints exchanged DTOs shaped for exactly what the Angular grid and forms needed — which also kept the payloads lean (part of the grid-performance story: only send the columns the UI displays).

---

### Rapid-fire recap

| Topic | Subsea answer |
|-------|---------------|
| Web API controllers | `[ApiController]` + `[HttpGet/Post/Put]` + `[Route]`; built the Schedule-Manager endpoints |
| `DataSourceRequest` | `[DataSourceRequest]` binds grid state → `ToDataSourceResult(IQueryable)` → SQL paging |
| Interface vs abstract | interface = contract/`IScheduleService` (DI + testable); abstract = shared base + code |
| `try/catch/finally` | try = risky, catch = handle/translate, **finally = always-run cleanup** (or `using`) |
| Global exceptions | middleware/filter → uniform `{ message }` JSON + correct status (feeds the Angular toast) |
| DI | register in startup (scoped/singleton/transient), constructor-inject the interface |
| Layers | thin controller → service (interface) → repo (`IQueryable`) → DTOs out |
| Honest depth | owned endpoints end-to-end; defer deep C# runtime/GC internals |
