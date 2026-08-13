# ASP.NET MVC — 72 Interview Questions with Answers

> 🎯 **What this file is:** the **spoken Q&A round** — *"what is X?"*, *"explain Y"*, *"how would you do Z?"* — for ASP.NET MVC.
>
> Its sibling **[28 — MVC "What Happens?"](./28-dotnet-mvc-tricky-questions.md)** is the *code* round (*"this action runs — what does the browser get?"*). Most .NET rounds use **both**: theory for the first 20 minutes, then a program on screen. Do 29 first, 28 second.

> 📌 **Framework coverage.** Answers are **ASP.NET Core MVC** first, with **⚖️ In MVC 5** boxes wherever the classic framework differs. *"What changed in Core?"* is itself one of the most-asked questions in this list — you'll find it at **[Q7](#q7-whats-the-difference-between-aspnet-mvc-5-and-aspnet-core-mvc)**.

> ⚠️ **Read [§ 0 — The honesty position](#-0--the-honesty-position-read-this-first) before anything else.** You have Angular + Java/Spring Boot production experience, not .NET production experience. How you frame that in the first two minutes decides how the rest of the round goes.

---

## Table of contents

| # | Section | Questions |
|---|---|---|
| [0](#-0--the-honesty-position-read-this-first) | 🔴 **The honesty position** | How to answer *"have you worked on .NET?"* |
| [★](#-the-15-questions-most-likely-to-be-asked) | ⭐ **The 15 most-likely questions** | If you only have one hour |
| [A](#a--mvc-fundamentals) | MVC fundamentals | Q1–Q9 |
| [B](#b--the-request-lifecycle) | The request lifecycle | Q10–Q13 |
| [C](#c--routing) | Routing | Q14–Q21 |
| [D](#d--controllers--action-results) | Controllers & action results | Q22–Q29 |
| [E](#e--views--razor) | Views & Razor | Q30–Q39 |
| [F](#f--passing-data-to-the-view) | Passing data to the view | Q40–Q44 |
| [G](#g--model-binding--validation) | Model binding & validation | Q45–Q51 |
| [H](#h--filters) | Filters | Q52–Q56 |
| [I](#i--dependency-injection--middleware) | DI & middleware | Q57–Q61 |
| [J](#j--security) | Security | Q62–Q66 |
| [K](#k--entity-framework-performance--testing) | EF Core, performance & testing | Q67–Q72 |
| [L](#l--answer-scripts) | Answer scripts | Self-intro, project walkthrough, closing |
| [M](#m--rapid-fire-table) | Rapid-fire table | 1-line answers |

---

# 🔴 0 — The honesty position (read this first)

**They will ask, in the first five minutes: *"How much .NET have you worked on?"***

> 🟢 **You have a real answer to this — lead with it. Do not undersell.**
>
> **Subsea** was an Angular front-end on a **.NET Web API** backend, containerized with Docker and deployed on **Azure**. You **owned the Schedule-Manager module front to back — the Angular UI *and* the .NET endpoints** — including pushing grid paging/sorting/filtering down into the C# API with Kendo's `DataSourceRequest`, and you coordinated API contracts with the backend team. That is production .NET, shipped.
>
> 🔗 Full detail: **[10 — Projects Deep-Dive § 2 Subsea](./10-projects-deep-dive.md)** — re-read that section before any .NET interview.

### ✅ The script (memorize this shape, use your own words)

> *"I've worked on both sides of a .NET stack. On **Subsea** — a marine equipment and operations platform — the backend was a **.NET Web API** with an Angular SPA in front, running in Docker on Azure. I owned the **Schedule-Manager** module end to end: the Angular feature module and the .NET endpoints behind it. The piece I'd point to is a performance fix — the Kendo grids were client-bound and got sluggish as the datasets grew, so I moved paging, sorting and filtering into the API using `DataSourceRequest` so it translated to SQL paging and only one page crossed the wire.*
>
> *Alongside that, my other backend experience is **Java with Spring Boot**, which maps very closely — filters are Spring's interceptors, middleware is the servlet filter chain, the built-in DI container plays the same role, EF Core sits where JPA does. So I'm comfortable in the architecture from both directions.*
>
> *To be straight about the boundary: my .NET work has been **Web API** — controllers, endpoints, DTOs — rather than **MVC with Razor views**, since the UI was Angular. The routing, model binding, filters and DI are the same stack, and I've been closing the Razor and view-layer gap deliberately."*

**Why this works:** it opens with a real shipped project and a specific technical decision (the one that proves you were actually in the code), backs it with the Spring mapping, and draws the boundary *precisely* — Web API rather than Razor MVC — instead of vaguely conceding "I haven't really done .NET". Precision reads as confidence; vagueness reads as bluffing.

### ⚠️ Two things to nail down before the interview

1. **Which .NET version was Subsea on?** — .NET Framework 4.x with classic Web API, or .NET Core/5+? It changes half the answers in this file, and *"I don't remember which .NET we were on"* is the one answer that damages you. **Check the repo or your notes and know it.**
2. **Was there any Razor, or was it API-only?** If the project had any `.cshtml`, say so. If it was purely JSON endpoints, say that — it's a completely normal shape and nobody penalises it.

> 🔴 **Never bluff a version number or an API signature.** Everything else here is true, so there is nothing you need to invent.

### The mapping table — know it cold

| Spring / Java | ASP.NET Core MVC |
|---|---|
| `@Controller` / `@RestController` | `Controller` / `[ApiController]` |
| `@RequestMapping`, `@GetMapping` | `[Route]`, `[HttpGet]` |
| `@RequestBody` / `@RequestParam` / `@PathVariable` | `[FromBody]` / `[FromQuery]` / `[FromRoute]` |
| `@Valid` + `BindingResult` | Data annotations + `ModelState` |
| `HandlerInterceptor` (`preHandle`/`postHandle`) | Action filter (`OnActionExecuting`/`OnActionExecuted`) |
| `@ControllerAdvice` + `@ExceptionHandler` | Exception filter / `UseExceptionHandler` |
| Servlet `Filter` | Middleware |
| `@Autowired`, `@Service`, `@Component` | Constructor injection, `AddScoped`, `AddSingleton` |
| `@Scope("singleton"/"request")` | `AddSingleton` / `AddScoped` |
| `application.properties` / `@Value` | `appsettings.json` / `IOptions<T>` |
| Spring Data JPA, `EntityManager` | Entity Framework Core, `DbContext` |
| `@Transactional` | `DbContext` scope + `SaveChanges` / explicit transaction |
| Thymeleaf / JSP | Razor |
| Maven / Gradle | NuGet / `.csproj` |
| Tomcat | Kestrel (behind IIS/Nginx) |
| JUnit + Mockito | xUnit + Moq |

> 💡 **Use it live.** Whenever you're asked about a .NET concept you're shaky on, say *"in Spring this is X — is it the same idea here?"* You'll usually be right, and it converts a gap into a conversation between two engineers.

---

# ⭐ The 15 questions most likely to be asked

> If you have one hour before the interview, these are it. Full answers are linked.

| # | Question | The one-line answer |
|---|---|---|
| 1 | [What is MVC?](#q1-what-is-mvc-and-why-use-it) | A separation-of-concerns pattern: Model = data + rules, View = UI, Controller = handles the request and picks the response |
| 2 | [Explain the request lifecycle](#q10-walk-me-through-the-aspnet-core-mvc-request-lifecycle) | Middleware → routing → filters → model binding → action → result → response |
| 3 | [ViewBag vs ViewData vs TempData](#q40-viewbag-vs-viewdata-vs-tempdata-vs-session) | Same request / same request / survives a redirect |
| 4 | [What are filters and their order?](#q52-what-are-filters-and-what-types-are-there) | Authorization → Resource → Action → Exception → Result; Global → Controller → Action, reversed on the way out |
| 5 | [Routing: conventional vs attribute](#q14-what-is-routing-and-what-are-the-two-kinds) | Pattern registered centrally vs `[Route]` on the action |
| 6 | [What is model binding?](#q45-what-is-model-binding-and-where-do-values-come-from) | Maps HTTP request values onto action parameters: Form → Route → Query |
| 7 | [How does validation work?](#q48-how-does-validation-work-end-to-end) | Data annotations → `ModelState.IsValid` → redisplay or save; client validation is UX only |
| 8 | [Partial view vs View Component](#q35-partial-view-vs-view-component-vs-child-action) | Partial = markup only; View Component = markup + its own logic and DI |
| 9 | [What are action results?](#q24-what-are-the-main-action-result-types) | `ViewResult`, `JsonResult`, `RedirectToActionResult`, `FileResult`, `StatusCodeResult`… |
| 10 | [DI lifetimes](#q57-explain-the-three-di-lifetimes) | Transient = per injection, Scoped = per request, Singleton = per app |
| 11 | [What is middleware?](#q59-what-is-middleware-and-how-is-it-different-from-a-filter) | A nested request pipeline; runs for *every* request, not just MVC |
| 12 | [MVC 5 vs Core](#q7-whats-the-difference-between-aspnet-mvc-5-and-aspnet-core-mvc) | Cross-platform, built-in DI, unified MVC+API, middleware instead of HTTP modules |
| 13 | [How do you prevent CSRF?](#q62-how-do-you-prevent-csrf) | `[ValidateAntiForgeryToken]` + the hidden token the tag helper emits |
| 14 | [Layout, `_ViewStart`, sections](#q33-explain-_layout-_viewstart-_viewimports-and-sections) | `_ViewStart` → view → layout; `RenderBody` once, sections optional |
| 15 | [How would you improve performance?](#q70-how-would-you-improve-the-performance-of-an-mvc-application) | Async all the way, `AsNoTracking`, fix N+1, cache, project to DTOs, bundle assets |

---

# A — MVC fundamentals

## Q1. What is MVC and why use it?

**Answer (say this):**
> *"MVC is a presentation-layer pattern that splits an application into three responsibilities: the **Model** holds the data and business rules, the **View** renders the UI, and the **Controller** receives the request, coordinates the model, and chooses which result to return. The benefit is **separation of concerns** — the view has no business logic and the controller has no HTML, so each part is testable and changeable on its own."*

**The third sentence (the trade-off) — this is what scores:**
> *"The cost is more files and more ceremony than putting logic in the page, so for a truly simple CRUD page Razor Pages is often the better fit. MVC pays for itself once there's real logic to keep out of the view."*

### If they push: "who talks to whom?"

```
        ┌──────────────┐
        │   Browser    │
        └──────┬───────┘
               │ HTTP request
               ▼
        ┌──────────────┐  1. updates / queries   ┌──────────────┐
        │  Controller  │ ──────────────────────► │    Model     │
        └──────┬───────┘                          └──────┬───────┘
               │ 2. selects a view                       │
               │    and passes the model                 │
               ▼                                         │
        ┌──────────────┐ ◄───────────────────────────────┘
        │     View     │  3. reads the model to render
        └──────┬───────┘
               │ HTML
               ▼
            Browser
```

**The key rule:** the **View never talks to the Model directly for data-fetching** — the controller hands it what it needs. In web MVC the view is passive.

---

## Q2. What are the advantages of MVC over Web Forms?

| | Web Forms | MVC |
|---|---|---|
| **Markup control** | ViewState, generated IDs | Full control over the HTML |
| **Testability** | Code-behind is tied to the page lifecycle | Controllers are plain classes — unit testable |
| **URLs** | `.aspx` file paths | Clean, routed, SEO-friendly |
| **Statefulness** | Stateful (ViewState, postbacks) | Stateless — matches how HTTP actually works |
| **Separation** | Code-behind mixes concerns | Enforced by the pattern |
| **Front-end fit** | Fights modern JS frameworks | Natural fit for Angular/React |

> 💡 **Tie it to your experience:** *"The last point matters to me — I build Angular front-ends. MVC returning JSON from clean routed URLs is exactly what an Angular app wants; Web Forms' postback model fights it."*

---

## Q3. Is MVC a design pattern or an architectural pattern?

> *"It's an **architectural pattern** for the presentation layer — it structures the whole layer, not one class interaction. People often call it a design pattern loosely, but it's built out of design patterns: the filter pipeline is **Chain of Responsibility**, DI is **Dependency Inversion**, and the view engine selection is a **Strategy**."*

That last sentence connects it to **[17 — SOLID & Design Patterns](./17-solid-design-patterns.md)** and reliably scores.

---

## Q4. What is the "Model" exactly? Domain model vs view model vs DTO

| Type | Purpose | Lives where |
|---|---|---|
| **Domain / entity model** | Business data + rules; maps to DB tables via EF | Domain / Data layer |
| **View model** | Exactly what ONE view needs — may combine several entities, adds display-only fields | Web layer |
| **DTO** | Data crossing a boundary (API request/response) | Contracts / API layer |

**Answer:**
> *"The 'M' in MVC is usually a **view model**, not the entity. I'd never bind a Razor form directly to an EF entity — that's how you get over-posting, where an attacker adds `IsAdmin=true` to the form and the binder happily sets it. A view model only carries the fields the view actually uses, so the extra field has nowhere to land."*

That answers the architecture question *and* the security question at once. (Live demo of the attack: **[28 — Q15](./28-dotnet-mvc-tricky-questions.md)**.)

---

## Q5. MVC vs Razor Pages vs Minimal APIs — when do you use each?

| | Best for | Shape |
|---|---|---|
| **MVC** | Apps with real logic, multiple views per controller, or a mix of pages + APIs | `Controller` + actions + views |
| **Razor Pages** | Page-focused CRUD — one URL, one file pair | `Page.cshtml` + `PageModel` with `OnGet`/`OnPost` |
| **Minimal APIs** | Small JSON APIs, microservices | `app.MapGet("/x", () => …)` in `Program.cs` |
| **Web API (controllers)** | Larger JSON APIs needing filters, versioning, conventions | `[ApiController] : ControllerBase` |

> *"Microsoft's own guidance since .NET 6 is Razor Pages for page-based scenarios and MVC when you need the controller abstraction. They share the same underlying stack — routing, binding, filters, DI are identical — so it's an organisational choice, not a technical one."*

---

## Q6. What's the difference between .NET Framework, .NET Core and .NET 5+?

> *"**.NET Framework** (up to 4.8) is Windows-only, IIS-hosted, and in maintenance mode — no new features. **.NET Core** (1.0–3.1) was the cross-platform rewrite. From **.NET 5** they dropped 'Core' from the name and unified everything into one platform — .NET 6, 8, 9 are all the same lineage. **.NET Standard** was a compatibility spec that let libraries target both; it's effectively legacy now that everything is on .NET 5+."*

**The version fact worth knowing:** **even-numbered releases are LTS** (.NET 6, 8, 10 — three years of support); odd-numbered are STS (18 months). If they ask what you'd start a new project on: *"the current LTS."*

---

## Q7. What's the difference between ASP.NET MVC 5 and ASP.NET Core MVC?

**This is the single most-asked comparison question. Know at least six rows.**

| | ASP.NET MVC 5 | ASP.NET Core MVC |
|---|---|---|
| **Platform** | Windows only, `System.Web` | Cross-platform (Windows/Linux/macOS) |
| **Hosting** | IIS only | Kestrel; IIS/Nginx/Apache as reverse proxy; Docker |
| **Startup** | `Global.asax` + `Web.config` + `RouteConfig` | `Program.cs` (minimal hosting) + `appsettings.json` |
| **DI** | None built in — Unity, Ninject, Autofac | **Built in** — `IServiceCollection` |
| **MVC + Web API** | Two separate stacks, two `Controller` base classes, two filter systems | **Unified** — one `Controller`/`ControllerBase` |
| **Pipeline** | HTTP modules & handlers | **Middleware** |
| **Config** | `Web.config` (XML) | `appsettings.json` + env vars + user secrets, layered |
| **Views** | Razor + ASPX engines; HTML helpers | Razor only; **tag helpers** + view components |
| **Cross-cutting** | `HttpContext.Current` (ambient static) | Injected `IHttpContextAccessor` — no ambient state |
| **Performance** | Good | Substantially faster (Kestrel, less allocation) |
| **Open source** | Partially | Fully, on GitHub |

**Answer out loud (pick four):**
> *"The big ones are: Core is cross-platform and self-hosted on Kestrel instead of IIS-only; dependency injection is built in instead of bolted on; MVC and Web API were merged into one stack instead of two parallel ones; and HTTP modules were replaced by middleware, which is a much simpler nested pipeline. Configuration also moved from `Web.config` XML to layered `appsettings.json` plus environment variables."*

---

## Q8. What is Kestrel, and why is there still IIS in front of it?

> *"**Kestrel** is the cross-platform web server built into ASP.NET Core — it's what actually runs your app. It's fast but deliberately minimal, so in production you usually put a **reverse proxy** in front of it — IIS on Windows, Nginx or Apache on Linux — which handles TLS termination, request filtering, port sharing across sites, and process management/restarts."*

**If they push — "can Kestrel be edge-facing?"** Yes, it's supported and hardened enough for it now, but the reverse proxy still buys you host-level concerns you'd otherwise implement yourself.

---

## Q9. What is `wwwroot`, and how do static files work?

> *"`wwwroot` is the **web root** — the only folder served directly to browsers. Everything else in the project is invisible over HTTP, which is a safe default: your `.cs` files, `appsettings.json` and `Views` folder can't be downloaded."*

```csharp
app.UseStaticFiles();     // serves wwwroot, short-circuits — must come BEFORE UseRouting
```

**The trade-off to volunteer:** *"Because `UseStaticFiles` runs before `UseAuthorization`, anything in `wwwroot` is public — `[Authorize]` doesn't protect it. Files that need protection have to be served through a controller action that checks permissions and returns a `FileResult`."*

---

# B — The request lifecycle

## Q10. Walk me through the ASP.NET Core MVC request lifecycle

**This is the highest-value answer in the entire file.** Almost every other question is a zoom-in on one of these steps, so knowing the sequence lets you locate any question.

```
 1. Browser sends an HTTP request
 2. Kestrel receives it, builds an HttpContext
 3. ── MIDDLEWARE PIPELINE (in order) ──────────────────────
      UseExceptionHandler → UseHttpsRedirection → UseStaticFiles
      → UseRouting  ← selects the endpoint from the route table
      → UseAuthentication (who are you?) → UseAuthorization (allowed?)
      → UseEndpoints / MapControllers  ← executes the endpoint
 4. ── FILTER PIPELINE ────────────────────────────────────
      Authorization filters
      Resource filters (before)
 5.   Controller instantiated (dependencies injected)
 6.   MODEL BINDING — request values → action parameters
 7.   VALIDATION — data annotations populate ModelState
      Action filters (before)
 8.   ═══ THE ACTION METHOD RUNS ═══
      Action filters (after)
      Result filters (before)
 9.   RESULT EXECUTION — the view engine renders Razor to HTML
      Result filters (after)
      Resource filters (after)
10. Response travels back OUT through the middleware, in reverse
11. Kestrel writes it to the wire
```

**The 30-second spoken version:**
> *"The request hits Kestrel, goes through the middleware pipeline where routing selects an endpoint and authentication and authorization run, then into the MVC filter pipeline. MVC creates the controller with its dependencies injected, binds the request values onto the action parameters, validates them into ModelState, runs the action filters and then the action, and the action returns an `IActionResult` which is executed afterwards — for a `ViewResult` that's when Razor actually renders. Then the response unwinds back out through the middleware in reverse order."*

### 🔥 The follow-ups they ask next

**"Where exactly does model binding happen?"** — Between resource filters and action filters. That's *why* an authorization filter can reject a request before you pay the cost of binding it, and why resource filters are where output caching lives.

**"Where does the view render?"** — In step 9, **after the action has already returned**. This is why an exception in a view isn't caught by an exception filter (**[28 — Q39](./28-dotnet-mvc-tricky-questions.md)**) and why a lazy `IQueryable` returned from an action executes inside the view.

---

## Q11. ⚖️ And the MVC 5 lifecycle?

```
Request → IIS → UrlRoutingModule (HTTP module)
        → RouteTable match → MvcRouteHandler → MvcHandler
        → ControllerFactory creates the controller
        → ActionInvoker
             → Authentication filters
             → Authorization filters
             → Model binding
             → Action filters → ACTION → Action filters
             → Result filters → ActionResult executes
                  → ViewEngine (Razor) locates & renders the view
        → Response
```

**The differences worth naming:** MVC 5 uses **HTTP modules and handlers** instead of middleware, has a separate **Authentication filter** type that Core removed (Core does authentication in middleware instead), and everything hangs off `System.Web.HttpContext.Current` rather than an injected context.

---

## Q12. What is `Program.cs` doing in a Core app?

```csharp
var builder = WebApplication.CreateBuilder(args);

// ── 1. REGISTER services into the DI container ──
builder.Services.AddControllersWithViews();
builder.Services.AddDbContext<AppDbContext>(o =>
    o.UseSqlServer(builder.Configuration.GetConnectionString("Default")));
builder.Services.AddScoped<IProductService, ProductService>();

var app = builder.Build();

// ── 2. BUILD the middleware pipeline (order matters!) ──
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts();
}
app.UseHttpsRedirection();
app.UseStaticFiles();
app.UseRouting();
app.UseAuthentication();
app.UseAuthorization();

app.MapControllerRoute("default", "{controller=Home}/{action=Index}/{id?}");

app.Run();
```

**The one-sentence mental model:**
> *"`builder.Services.AddX()` is **registration** — what's available. `app.UseX()` is **execution** — what runs, in order. Nearly every 'why is this null in Core?' bug is a missing `UseX()` or one in the wrong place."*

⚖️ **In MVC 5** this was split across `Global.asax` (`Application_Start`), `RouteConfig.cs`, `FilterConfig.cs`, `BundleConfig.cs` and `Web.config`. Core collapsed all of it into `Program.cs`. (Versions 3.1–5.0 used a `Startup.cs` with `ConfigureServices` and `Configure` — same two halves, different file.)

---

## Q13. How does configuration work in Core?

> *"`IConfiguration` layers multiple sources, and **later sources override earlier ones**: `appsettings.json`, then `appsettings.{Environment}.json`, then user secrets in Development, then environment variables, then command-line args. So the same build runs in every environment and only the config differs — which is what you want for containers."*

```csharp
// Bind a section to a strongly typed class — preferred over magic strings
builder.Services.Configure<SmtpSettings>(builder.Configuration.GetSection("Smtp"));

public class EmailService
{
    public EmailService(IOptions<SmtpSettings> options) { _settings = options.Value; }
}
```

**The trade-off to volunteer:** *"Secrets never go in `appsettings.json` — that's committed to git. In development I'd use **user secrets** (`dotnet user-secrets`), and in production environment variables or Azure Key Vault."*

`IOptions` vs `IOptionsSnapshot` vs `IOptionsMonitor` is a common follow-up — see **[28 — Q62](./28-dotnet-mvc-tricky-questions.md)**.

---

# C — Routing

## Q14. What is routing, and what are the two kinds?

> *"Routing maps an incoming URL to a controller action, and also generates URLs in the other direction so your links don't hard-code paths. There are two styles: **conventional routing**, where you register URL patterns centrally, and **attribute routing**, where you put `[Route]` directly on the controller or action."*

```csharp
// CONVENTIONAL — one pattern serves the whole app
app.MapControllerRoute("default", "{controller=Home}/{action=Index}/{id?}");

// ATTRIBUTE — the URL lives next to the code
[Route("api/[controller]")]
public class ProductsController : ControllerBase
{
    [HttpGet("{id:int}")]                        // GET /api/products/5
    public IActionResult Get(int id) => Ok();
}
```

**When to use which:**
> *"Conventional for a traditional MVC site where URLs follow a predictable `/controller/action` shape. Attribute routing for APIs, where the URLs are resource-shaped and don't match the controller/action naming — and it's the only sensible option for REST versioning."*

---

## Q15. Explain `{controller=Home}/{action=Index}/{id?}`

| Segment | Meaning |
|---|---|
| `{controller=Home}` | Route **parameter** with the **default value** `Home` |
| `{action=Index}` | Defaults to `Index` |
| `{id?}` | **Optional** — the URL matches with or without it |

So `/` → `HomeController.Index()`, `/Products` → `ProductsController.Index()`, `/Products/Details/5` → `Details(5)`.

**⚠️ The trap they follow up with:** an optional route segment does **not** make the C# parameter optional. `Details(int id)` with no id throws in MVC 5 and silently binds `0` in Core — **[28 — Q4](./28-dotnet-mvc-tricky-questions.md)**. The parameter must be `int?` or have a default.

---

## Q16. What are route constraints?

```csharp
[HttpGet("products/{id:int:min(1)}")]        // must be an int ≥ 1
[HttpGet("posts/{slug:alpha:length(3,50)}")] // letters only, 3–50 chars
[HttpGet("archive/{date:datetime}")]
[HttpGet("items/{code:regex(^[A-Z]{{3}}$)}")]
```

> *"A constraint restricts what a segment can match. The important thing to be clear about is that **a constraint is a matching decision, not a validation decision** — if it fails you get a **404**, not a 400 and not a ModelState error. So I use constraints to reject requests that were never for me, and data annotations to reject requests that are for me but wrong."*

Built-ins worth naming: `int`, `bool`, `datetime`, `decimal`, `guid`, `alpha`, `length`, `min`/`max`, `minlength`/`maxlength`, `range`, `regex`, `required`.

---

## Q17. How do you write a custom route constraint?

```csharp
public class EvenConstraint : IRouteConstraint
{
    public bool Match(HttpContext ctx, IRouter route, string key,
                      RouteValueDictionary values, RouteDirection direction)
        => values.TryGetValue(key, out var v)
           && int.TryParse(v?.ToString(), out var n)
           && n % 2 == 0;
}

builder.Services.Configure<RouteOptions>(o => o.ConstraintMap.Add("even", typeof(EvenConstraint)));
// usage: [HttpGet("items/{id:even}")]
```

---

## Q18. What is attribute route token replacement?

```csharp
[Route("api/[controller]/[action]")]
public class ProductsController : Controller
{
    public IActionResult Search() => Ok();     // → /api/Products/Search
}
```

> *"`[controller]`, `[action]` and `[area]` are replaced at startup with the actual names. The benefit is that renaming the class renames the URL automatically — no stale strings. The trade-off is that renaming a class silently breaks every existing link and bookmark, so on a public API I'd write the literal string instead and treat the URL as a contract."*

That trade-off sentence is exactly the "second question" depth your rounds have been lost on.

---

## Q19. What are Areas and when would you use them?

> *"An Area is a sub-application inside the project with its own Controllers, Views and Models folders — the standard use is separating an Admin section from the public site in a large app."*

```csharp
[Area("Admin")]
public class DashboardController : Controller { }

app.MapControllerRoute("areas", "{area:exists}/{controller=Home}/{action=Index}/{id?}");
```
```
/Areas/Admin/Controllers/DashboardController.cs
/Areas/Admin/Views/Dashboard/Index.cshtml
```

**Trade-off:** *"Areas are folder-level separation only — they don't give you independent deployment or independent teams. Past a certain size I'd reach for separate projects or services rather than more areas."*

---

## Q20. How do you generate URLs instead of hard-coding them?

```csharp
Url.Action("Details", "Products", new { id = 5 })       // in a controller
```
```razor
<a asp-controller="Products" asp-action="Details" asp-route-id="5">View</a>   @* tag helper *@
@Html.ActionLink("View", "Details", "Products", new { id = 5 }, null)         @* HTML helper *@
```

> *"Always generate, never hard-code. If the route pattern changes, generated URLs follow automatically and hard-coded ones become 404s. The tag helper form is preferred in Core — the HTML helper has an overload trap where a controller name passed in the wrong position becomes a query-string parameter."* (That trap: **[28 — Q51](./28-dotnet-mvc-tricky-questions.md)**.)

---

## Q21. What is a named route, and why name one?

```csharp
[HttpGet("products/{id}", Name = "ProductDetails")]
public IActionResult Details(int id) => View();

// then:
Url.Link("ProductDetails", new { id = 5 });
return CreatedAtRoute("ProductDetails", new { id = p.Id }, p);   // 201 + Location header
```

> *"A name gives you a stable handle for URL generation that survives route changes. The main real-world use is REST — `CreatedAtRoute` returns a 201 with a `Location` header pointing at the new resource."*

---

# D — Controllers & action results

## Q22. What is a controller, and what makes a method an action?

> *"A controller is a class that groups related actions. In Core it inherits from `Controller` (for views) or `ControllerBase` (for APIs, no view support). **Every public method is an action by default** and is routable — which means a public helper method you forgot to make private is a live endpoint."*

Suppress with `[NonAction]`, or just make it private. (The security angle: **[28 — Q8](./28-dotnet-mvc-tricky-questions.md)** — worth telling as a story.)

**Conventions:** the class name must end in `Controller` (or carry `[Controller]`), and it must be public, non-abstract, non-generic.

---

## Q23. `Controller` vs `ControllerBase` — which do you inherit?

| | `ControllerBase` | `Controller` |
|---|---|---|
| Model binding, validation, `HttpContext`, `User` | ✅ | ✅ |
| `Ok()`, `NotFound()`, `BadRequest()`, `CreatedAtAction()` | ✅ | ✅ |
| `View()`, `PartialView()`, `ViewBag`, `ViewData`, `TempData` | ❌ | ✅ |

> *"`ControllerBase` for APIs — it keeps view-related members off the class you'll never use. `Controller` inherits from it and adds the Razor bits."*

---

## Q24. What are the main action result types?

| Return | Produces | Typical use |
|---|---|---|
| `View()` / `View(model)` | `ViewResult` | Render Razor **with** the layout |
| `PartialView()` | `PartialViewResult` | Fragment, **no** layout — AJAX |
| `Json(obj)` | `JsonResult` | JSON response |
| `Content("text")` | `ContentResult` | Raw text |
| `RedirectToAction("Index")` | `RedirectToActionResult` | 302 to another action |
| `Redirect(url)` / `LocalRedirect(url)` | `RedirectResult` | 302 to a URL (`Local` blocks open-redirect) |
| `File(bytes, type, name)` | `FileResult` | Downloads |
| `Ok()` / `Ok(obj)` | `OkObjectResult` | 200 |
| `NotFound()` | 404 | |
| `BadRequest(ModelState)` | 400 | |
| `Unauthorized()` / `Forbid()` | 401 / 403 | |
| `CreatedAtAction(...)` | 201 + `Location` | REST create |
| `NoContent()` | 204 | REST update/delete |
| `StatusCode(418)` | any status | |
| *(nothing / `void`)* | `EmptyResult` | 200, empty body |

---

## Q25. `IActionResult` vs `ActionResult<T>` vs a concrete type?

```csharp
public IActionResult Get(int id)          // ← flexible: can return Ok() or NotFound()
    => id > 0 ? Ok(new Product()) : NotFound();

public ActionResult<Product> Get2(int id) // ← flexible AND documents the success type
    => id > 0 ? new Product() : NotFound();

public Product Get3(int id) => new Product();   // ← always 200; can't return NotFound()
```

> *"`IActionResult` when the action can return different status codes. **`ActionResult<T>` is the best of both** — you keep the flexibility but the return type documents the success shape, which is what Swagger and API clients read. A concrete return type is fine only when there's genuinely one outcome."*

---

## Q26. Why does the action return a result object instead of writing the response?

> *"Because it makes the action **testable and composable**. `RedirectToAction` doesn't redirect — it returns an object *describing* a redirect, which the framework executes afterwards. So a unit test can assert `Assert.IsType<RedirectToActionResult>(result)` without any HTTP at all. It also means result filters get a chance to run between the action returning and the response being written."*

The practical consequence: code after `return RedirectToAction(...)` is unreachable, whereas MVC 5's `Response.Redirect` **does not stop execution** — **[28 — Q30](./28-dotnet-mvc-tricky-questions.md)**.

---

## Q27. What are action selectors / action method selectors?

| Attribute | Effect |
|---|---|
| `[HttpGet]`, `[HttpPost]`, `[HttpPut]`, `[HttpDelete]`, `[HttpPatch]` | Restrict by verb |
| `[AcceptVerbs("GET","POST")]` | Multiple verbs |
| `[ActionName("Report")]` | Decouple the **routing** name from the **method** name |
| `[NonAction]` | Public method that is *not* an endpoint |

**The classic use of `[ActionName]`:**
```csharp
[HttpGet]  public IActionResult Delete(int id) => View();
[HttpPost, ActionName("Delete")]
public IActionResult DeleteConfirmed(int id) { db.Delete(id); return RedirectToAction("Index"); }
```
> *"Two methods can't have the same name and signature, but both need to be reachable at `/Products/Delete`. `[ActionName]` solves it — this exact pattern is what MVC scaffolding generates."*

---

## Q28. Why must Delete be a POST?

> *"Because GET must be **safe and idempotent**. If deleting is a GET, a search-engine crawler, a browser prefetch, or an `<img src="/Products/Delete/5">` on any other site will silently delete your data — no user interaction needed. Destructive actions are `[HttpPost]` plus `[ValidateAntiForgeryToken]`."*

A genuinely strong answer because it's a real vulnerability, not a convention.

---

## Q29. How do you handle multiple submit buttons on one form?

```html
<button type="submit" name="action" value="save">Save</button>
<button type="submit" name="action" value="delete">Delete</button>
```
```csharp
[HttpPost]
public IActionResult Handle(Product p, string action)
    => action == "delete" ? Delete(p.Id) : Save(p);
```

> *"Simplest approach: give the buttons the same `name` and different `value`, and bind that name as a parameter. The alternative is `asp-page-handler` in Razor Pages, or a custom `ActionNameSelectorAttribute` if you want each button to route to its own action."*

---

# E — Views & Razor

## Q30. What is Razor?

> *"Razor is the view engine — a templating syntax that mixes C# into HTML using `@`. The parser is smart enough to work out where code ends and markup begins, so you rarely need explicit delimiters. It compiles to a C# class, so views are strongly typed and compile-checked when you enable Razor compilation."*

```razor
@model Product                              @* the view's model type *@
@{ var count = Model.Items.Count; }         @* code block *@
<p>@Model.Name</p>                          @* implicit expression *@
<p>@(count + 1)</p>                         @* explicit expression *@
@if (count > 0) { <p>In stock</p> }         @* control flow *@
@foreach (var i in Model.Items) { <li>@i</li> }
@@                                          @* literal @ *@
@* this is a Razor comment *@
```

**The two traps to mention unprompted:** `@count + 1` renders `"5 + 1"` because the implicit expression ends at the space; and **Razor HTML-encodes everything by default**, so `@Model.Name` is XSS-safe and `@Html.Raw` is the only way to opt out. Both are in **[28 — Q44/Q45](./28-dotnet-mvc-tricky-questions.md)**.

---

## Q31. Strongly typed views vs `dynamic` — why does it matter?

```razor
@model ProductViewModel
<p>@Model.Name</p>              @* ✅ compile-checked, IntelliSense, refactor-safe *@
<p>@ViewBag.Name</p>            @* ❌ a typo renders blank, silently *@
```

> *"A strongly typed view declares `@model`, so property access is compile-checked and refactoring renames it everywhere. `ViewBag` is `dynamic`, so a typo just renders an empty string with no error at all — the failure reaches the customer. I use `ViewBag` for something trivial like a page title and a view model for anything the view actually depends on."*

---

## Q32. HTML helpers vs tag helpers

```razor
@Html.TextBoxFor(m => m.Name, new { @class = "form-control" })
@Html.ValidationMessageFor(m => m.Name)

<input asp-for="Name" class="form-control" />
<span asp-validation-for="Name"></span>
```

| | HTML helpers | Tag helpers |
|---|---|---|
| Looks like | C# method calls | HTML attributes |
| Designer/HTML-editor friendly | ❌ | ✅ |
| IntelliSense on attributes | Limited | ✅ |
| Available in | MVC 5 + Core | **Core only** |
| `class` attribute | `new { @class = ... }` (needs `@` escape) | just `class="..."` |

> *"Tag helpers are preferred in Core — the markup stays valid HTML, so a front-end developer can work in it without knowing Razor. Both generate the same output and both read their value from ModelState first."*

**That last clause is the thing to say**, because it sets up the biggest MVC view trap — **[28 — Q43](./28-dotnet-mvc-tricky-questions.md)**: after a POST, `asp-for`/`TextBoxFor` shows the **posted** value even if your code changed the model.

---

## Q33. Explain `_Layout`, `_ViewStart`, `_ViewImports` and sections

| File | Job |
|---|---|
| `_Layout.cshtml` | The site template — `<html>`, nav, footer; calls `@RenderBody()` |
| `_ViewStart.cshtml` | Runs **before every view**; this is where `Layout = "_Layout";` is set. Cascades down folders |
| `_ViewImports.cshtml` | Shared `@using`, `@inject`, `@addTagHelper` — **no output** |
| `@RenderBody()` | Where the view's content is spliced in. **Exactly once** per layout |
| `@RenderSection("Scripts", required: false)` | Optional named slot the view can fill |

**Execution order — the counter-intuitive bit:**
```
_ViewStart.cshtml  →  the VIEW (fully)  →  _Layout.cshtml
```
> *"The view executes **first**, completely, and its output is handed to the layout. So a view can set `ViewBag.Title` and the layout will see it — but not the other way round. That surprises people because the layout looks like the outer thing."* (**[28 — Q48](./28-dotnet-mvc-tricky-questions.md)**)

**⚠️ `RenderSection` is required by default** — a view that doesn't define the section throws. Pass `required: false`.

---

## Q34. How does the view engine find a view?

```
View()  in ProductsController.Details()
   → /Views/Products/Details.cshtml
   → /Views/Shared/Details.cshtml
   → (Razor Pages / area paths if configured)
```

> *"`View()` with no name uses the **action** name — not the method name, so `[ActionName]` changes what it looks for. If it can't find it, the exception helpfully lists every path it searched."*

---

## Q35. Partial view vs View Component vs child action

| | Partial view | View Component |
|---|---|---|
| Has its own logic / DI | ❌ — gets data from the parent | ✅ — its own class with constructor injection |
| Can query a database | ❌ (shouldn't) | ✅ |
| Invoked with | `<partial name="_Row" model="x" />` | `@await Component.InvokeAsync("Cart")` |
| Async | ✅ (`PartialAsync`) | ✅ natively |
| Use for | Reusable **markup** | A self-contained **widget** |

> *"A partial view is markup reuse — a row template, a form fragment. A **view component** is for something that needs its own data, like a shopping-cart summary or a nav menu driven by permissions: it has a class with DI, an `InvokeAsync` method, and its own view. In MVC 5 you'd have used `Html.Action` to call a child action for this; Core removed that and view components are the replacement."*

That last sentence is a strong "what changed in Core?" data point.

**Partial rendering options in Core:**
```razor
<partial name="_Row" model="item" />        @* preferred *@
@await Html.PartialAsync("_Row", item)      @* async *@
@Html.Partial("_Row")                       @* ⚠️ analyzer-flagged: blocks on async *@
@{ Html.RenderPartial("_Row"); }            @* writes direct to output; returns void *@
```

---

## Q36. Why can't a partial view define a `@section`?

> *"Sections belong to the **view↔layout** relationship. A partial isn't rendered by a layout — it's rendered inline into another view — so there's no section slot for it to fill. It's either ignored or throws. If a partial needs a script, the parent view's `@section Scripts` has to carry it, or you use a view component."*

---

## Q37. What does `@inject` do?

```razor
@inject IProductService ProductService
@inject IStringLocalizer<Home> Localizer

<p>@ProductService.GetFeaturedCount()</p>
```

> *"It resolves a service from DI directly into the view. It's handy for genuinely view-level concerns like localization or feature flags — but I'd be careful with it, because pulling data in the view means queries execute during rendering, after the action has returned, where exception filters can't see them."*

---

## Q38. What is `IHtmlHelper.Raw`, and when is it acceptable?

> *"Razor encodes every `@expression`, so `<script>` in user data renders as visible text rather than executing. `Html.Raw` is the only way to opt out — which makes it the standard XSS vector in .NET apps. It's acceptable only for HTML **you** generated, or user HTML **after** sanitising it with something like HtmlSanitizer. `Html.Raw` in a pull request is an automatic review comment for me."*

---

## Q39. How do you build a reusable editor for a type?

```
/Views/Shared/EditorTemplates/DateTime.cshtml
/Views/Shared/DisplayTemplates/Money.cshtml
```
```razor
@Html.EditorFor(m => m.StartDate)                @* picks up DateTime.cshtml automatically *@
@Html.EditorFor(m => m.Items)                    @* renders the template once per item,
                                                    with CORRECT indexed names *@
```

> *"Editor and display templates are convention-based — a file named after the type or specified with `[UIHint]`. The underrated benefit is **collections**: `EditorFor` on a list generates properly indexed input names like `Items[0].Name`, which is exactly what model binding needs. Hand-rolling that inside a `foreach` is the usual cause of forms that post nothing back."*

(The hand-rolled failure: **[28 — Drill 7](./28-dotnet-mvc-tricky-questions.md)**.)

---

# F — Passing data to the view

## Q40. ViewBag vs ViewData vs TempData vs Session

**The table to memorize:**

| | Type | Lifetime | Survives redirect | Cast needed |
|---|---|---|---|---|
| **ViewBag** | `dynamic` | Current request | ❌ | No |
| **ViewData** | `ViewDataDictionary` (`object`) | Current request | ❌ | Yes |
| **TempData** | `ITempDataDictionary` (`object`) | **Until read** (≈ next request) | ✅ | Yes |
| **Session** | `object` / byte[] | Until it expires | ✅ | Yes |
| **View model** | Strongly typed | Current request | ❌ | No — compile-checked |

**Answer:**
> *"`ViewBag` and `ViewData` are literally the **same dictionary** — `ViewBag` is just a `dynamic` wrapper over `ViewData`, so a value set through one is visible through the other. Both last one request. `TempData` is the one designed to cross a redirect, which is what makes the POST-Redirect-GET pattern work. `Session` is per-user and lasts until it expires."*

**The trade-off:** *"All four are untyped, so I use a **view model** for anything the view genuinely depends on and keep `ViewBag` for incidentals like a page title."*

---

## Q41. How does TempData actually work internally?

> *"It's backed by a provider — in Core the default is `CookieTempDataProvider`, an encrypted cookie; MVC 5 always used Session. The mechanic people get wrong is the deletion rule: **reading marks an entry for deletion at the end of the current request** — it isn't removed the instant you read it, and it isn't deleted by age. So you can read the same key twice in one request, and an entry nobody reads carries forward indefinitely."*

```csharp
TempData["Msg"];          // read AND mark for deletion
TempData.Peek("Msg");     // read WITHOUT marking → survives another request
TempData.Keep("Msg");     // un-mark after reading → survives another request
```

**The Core-specific gotcha worth volunteering:** *"With the cookie provider, values must be serializable to simple types — storing a `List<Product>` throws. And a cookie is capped around 4KB. So I store an id, not an object graph."*

---

## Q42. Why doesn't ViewBag survive a redirect?

> *"Because a redirect is a **302** — the browser makes a completely new HTTP request. New request means a new controller instance and a new `ViewData` dictionary, so anything in `ViewBag` is gone. That's precisely the gap `TempData` exists to fill."*

---

## Q43. What's the single most common ViewBag bug you'd expect to see?

> *"A dropdown that works on GET and crashes on POST. The GET action populates `ViewBag.Categories` for the `<select>`; validation fails on POST, the action does `return View(model)` — and `ViewBag.Categories` was never repopulated, so the view throws a `NullReferenceException`. **Every `return View(model)` on a failure path has to repopulate the same data the GET populated.** I'd factor that into a private method so it can't be forgotten."*

This is the highest-signal answer in section F — it's a bug you only know about from building forms.

---

## Q44. When would you use Session, and what's the cost?

> *"Session for genuinely per-user state that has to survive several requests — a multi-step wizard, a cart for an anonymous user. The costs are real: it needs `AddSession()` **and** `UseSession()` in the right order; it's untyped; it consumes server memory; and by default it's **in-process**, so it breaks the moment you scale to more than one instance behind a load balancer or the app pool recycles."*

**The fix to name:** a distributed cache — `AddStackExchangeRedisCache` — so session survives restarts and works across instances. *"Better still, avoid server session for anything that could live in a signed cookie or the database."*

---

# G — Model binding & validation

## Q45. What is model binding, and where do values come from?

> *"Model binding maps values from the HTTP request onto your action parameters — it's why you can write `Save(Product p)` instead of reading `Request.Form` by hand. The default value providers are tried in a fixed order: **Form → Route → Query string**, first one holding the key wins."*

**Force a source explicitly:**
`[FromForm]`, `[FromRoute]`, `[FromQuery]`, `[FromHeader]`, `[FromBody]`, `[FromServices]`

> *"I prefer to be explicit. Implicit precedence is exactly the kind of thing that changes behaviour six months later when someone adds a query parameter with the same name."*

---

## Q46. Why is my model empty when I POST JSON?

> *"Because in a plain MVC controller a complex type binds from **form values**, not the JSON body. You need `[FromBody]` — or the `[ApiController]` attribute, which flips the default so complex types come from the body and simple types from the route/query. That one attribute is behind most 'why is my model null?' questions."*

⚖️ **In MVC 5** this confusion was structural: `System.Web.Mvc` (form-first) and Web API (body-first) were two separate stacks with different `Controller` base classes and different filter types. Core unified them.

---

## Q47. What is over-posting, and how do you prevent it?

```csharp
public class User { public string Name { get; set; } public bool IsAdmin { get; set; } }
// attacker posts:  Name=Ann&IsAdmin=true    → IsAdmin binds. 🔓
```

> *"The binder fills **every public settable property** it finds a matching key for — including ones your form never rendered. An attacker just adds the field by hand. The fix, best first: bind to a **DTO or view model that physically doesn't have the field**; or whitelist with `[Bind("Name")]`. I'd never bind a form directly to an EF entity."*

Blacklisting (`[Bind(Exclude=...)]`) is weakest — a property added later is unprotected by default.

---

## Q48. How does validation work end to end?

```csharp
public class Product
{
    [Required(ErrorMessage = "Name is required")]
    [StringLength(100, MinimumLength = 3)]
    public string Name { get; set; }

    [Range(0.01, 10000)]
    public decimal Price { get; set; }

    [EmailAddress] public string ContactEmail { get; set; }
    [Compare("Password")] public string ConfirmPassword { get; set; }
    [RegularExpression(@"^\d{6}$")] public string Pincode { get; set; }
}
```
```csharp
[HttpPost]
public IActionResult Save(Product p)
{
    if (!ModelState.IsValid) return View(p);     // redisplay with messages
    db.Add(p); db.SaveChanges();
    return RedirectToAction(nameof(Index));      // POST-Redirect-GET
}
```
```razor
<span asp-validation-for="Name" class="text-danger"></span>
<div asp-validation-summary="ModelOnly"></div>
```

**Answer:**
> *"Data annotations on the model are read during model binding and populate `ModelState`. The action checks `ModelState.IsValid`, and on failure returns the same view so the user sees their input plus the messages. On success it redirects, so refresh doesn't re-post."*

**Three things to volunteer that most candidates miss:**

1. **A binding failure does not stop the action.** Posting `abc` to an `int` gives you `0` and an invalid `ModelState` — but the action still runs. If you don't check, you process garbage. (**[28 — Q14](./28-dotnet-mvc-tricky-questions.md)**)
2. **Client validation is UX only.** It's `data-val-*` attributes plus jQuery unobtrusive validation. Anyone can POST with curl, so the server check is the real one.
3. **`[Required]` on a non-nullable `int` can't catch `0`** — `Required` only checks null. Use `[Range(1, …)]`, or make it `int?`.

---

## Q49. How do you write a custom validation attribute?

```csharp
public class FutureDateAttribute : ValidationAttribute
{
    protected override ValidationResult IsValid(object value, ValidationContext ctx)
        => value is DateTime d && d > DateTime.UtcNow
            ? ValidationResult.Success
            : new ValidationResult($"{ctx.DisplayName} must be in the future");
}
```

**And for a rule that spans two properties, `IValidatableObject`:**
```csharp
public class Booking : IValidatableObject
{
    public DateTime Start { get; set; }
    public DateTime End { get; set; }

    public IEnumerable<ValidationResult> Validate(ValidationContext ctx)
    {
        if (End <= Start)
            yield return new ValidationResult("End must be after start", new[] { nameof(End) });
    }
}
```

> *"A `ValidationAttribute` for a rule about **one** property, `IValidatableObject` for a rule **across** properties. `IValidatableObject` only runs after all the attribute-level validations pass, which is usually what you want."*

---

## Q50. What is `[Remote]` validation?

```csharp
[Remote(action: "IsEmailAvailable", controller: "Account")]
public string Email { get; set; }

public IActionResult IsEmailAvailable(string email)
    => Json(!db.Users.Any(u => u.Email == email));
```

> *"It validates against the server without a full postback — the classic 'is this username taken?' check. Two caveats: it's an extra round trip on every field change, so debounce it; and it's **not** a substitute for the server-side check on submit, because of the race between checking and saving. I'd still have a unique constraint in the database."*

---

## Q51. How do you return validation errors from an API?

```csharp
[ApiController]                       // ← automatic 400 with ProblemDetails
[Route("api/[controller]")]
public class ProductsController : ControllerBase { }
```

> *"`[ApiController]` installs a filter that returns a **400 with an RFC 7807 ProblemDetails body automatically, before the action runs** — so an `if (!ModelState.IsValid)` inside the action is dead code. You can customise the shape with `ApiBehaviorOptions.InvalidModelStateResponseFactory`, or suppress it if you want full control."*

---

# H — Filters

## Q52. What are filters, and what types are there?

> *"Filters let you run code at specific stages of the MVC pipeline — they're the cross-cutting-concern mechanism, the equivalent of a Spring `HandlerInterceptor`. There are five types, and they run in a fixed order."*

| Type | Runs | Use for |
|---|---|---|
| **Authorization** | First, before everything | Permission checks |
| **Resource** | Around model binding | Caching, short-circuiting |
| **Action** | Around the action method | Logging, auditing, timing |
| **Exception** | On unhandled exceptions | Turning errors into friendly results |
| **Result** | Around result execution | Adding response headers |

```
Authorization → Resource(before) → [MODEL BINDING] → Action(before)
   → ACTION → Action(after) → Result(before) → [VIEW RENDERS] → Result(after) → Resource(after)
```

**Two things to volunteer:**
- *"Authorization is first because there's no point binding a model for a request you're about to reject."*
- *"Exception filters cover controller creation, binding, action filters and the action — but **not result execution**, so an exception while the view renders escapes them. That needs `UseExceptionHandler` middleware."*

⚖️ **MVC 5** also had an **Authentication** filter type; Core moved authentication into middleware.

---

## Q53. In what order do global, controller and action filters run?

```
IN:   Global → Controller → Action
ACTION METHOD
OUT:  Action → Controller → Global
```

> *"They nest like Russian dolls — reverse order on the way out. And `Order` beats scope: filters are sorted by the `Order` property first, which defaults to 0 for all three scopes, and scope only breaks the tie. So setting `Order = -1` on an action filter makes it run before a global one."*

---

## Q54. Write a custom action filter

```csharp
public class LogActionFilter : IActionFilter
{
    private readonly ILogger<LogActionFilter> _logger;
    public LogActionFilter(ILogger<LogActionFilter> logger) => _logger = logger;

    public void OnActionExecuting(ActionExecutingContext ctx)
        => _logger.LogInformation("Executing {Action}", ctx.ActionDescriptor.DisplayName);

    public void OnActionExecuted(ActionExecutedContext ctx)
        => _logger.LogInformation("Executed, exception: {Ex}", ctx.Exception?.Message ?? "none");
}
```

**Registering it — three scopes:**
```csharp
builder.Services.AddControllersWithViews(o => o.Filters.Add<LogActionFilter>());  // global
[ServiceFilter(typeof(LogActionFilter))]                                          // controller/action, with DI
public class HomeController : Controller { }
```

> *"If the filter needs dependencies injected, register it in DI and apply it with `[ServiceFilter]` or `[TypeFilter]` — a plain attribute can't take constructor injection, because attribute arguments must be compile-time constants."*

**Two traps worth naming:**
- *"Adding a filter **instance** (`o.Filters.Add(new LogFilter())`) makes it a singleton shared across all requests — any mutable field in it is a data race. Use the generic form."*
- *"Setting `context.Result` in `OnActionExecuting` short-circuits: the action is skipped, **and so is that filter's own `OnActionExecuted`**."*

---

## Q55. How do you handle exceptions globally?

**Three layers, and knowing which to use is the question:**

```csharp
// 1. MIDDLEWARE — catches everything, including view rendering. The outermost net.
app.UseExceptionHandler("/Home/Error");

// 2. EXCEPTION FILTER — MVC-aware; has ActionContext. Doesn't cover result execution.
public class ApiExceptionFilter : IExceptionFilter
{
    public void OnException(ExceptionContext ctx)
    {
        ctx.Result = new ObjectResult(new { error = "Something went wrong" }) { StatusCode = 500 };
        ctx.ExceptionHandled = true;
    }
}

// 3. try/catch — only where you can actually recover or add context
```

> *"Middleware for the safety net, because it wraps the whole pipeline. A filter when I need MVC context — which action, which model — to shape the response, typically to return JSON for an API instead of an HTML error page. And local `try/catch` only where I can genuinely do something about it. What I don't do is catch, log and rethrow at every layer — that's just noise in the log."*

**⚠️ `ExceptionHandled = true` without setting `Result` gives the client an empty 200** — a silent failure worse than the 500. Always set both.

---

## Q56. Give a real example of when you'd write each filter type

| Need | Filter |
|---|---|
| Only tenant admins may reach this controller | **Authorization** |
| Return a cached response and skip the action entirely | **Resource** |
| Audit-log every action's arguments and duration | **Action** |
| Convert a `DomainException` into a 422 with a message | **Exception** |
| Add `X-Correlation-Id` to every response | **Result** |
| Rate-limit *all* requests, including static files | **Middleware, not a filter** |

That last row is the one that separates people who've read the docs from people who've shipped.

---

# I — Dependency injection & middleware

## Q57. Explain the three DI lifetimes

| | Instance per | Use for |
|---|---|---|
| **Transient** | Every injection | Cheap, stateless services |
| **Scoped** | HTTP request | `DbContext`, repositories, per-request context |
| **Singleton** | Application lifetime | Caches, config, expensive stateless clients |

```csharp
builder.Services.AddTransient<IEmailFormatter, EmailFormatter>();
builder.Services.AddScoped<IProductRepository, ProductRepository>();
builder.Services.AddSingleton<ICacheService, CacheService>();
```

**The follow-up that always comes: "Which for `DbContext`?"**
> *"**Scoped** — and `AddDbContext` registers it as scoped by default. You get one unit of work and one change tracker per request, which is exactly the EF model. Singleton would be a disaster: `DbContext` isn't thread-safe and the change tracker would grow forever. Transient would break transactions spanning two repositories, because each would get its own context."*

---

## Q58. What is a captive dependency?

```csharp
builder.Services.AddScoped<IRepository, Repository>();
builder.Services.AddSingleton<ICacheService, CacheService>();   // ← injects IRepository
```
```
💥 InvalidOperationException: Cannot consume scoped service 'IRepository' from singleton 'ICacheService'.
```

> *"A singleton outlives every request, so if it captures a scoped service it keeps the **first** request's instance alive forever — a stale `DbContext` shared by every user. Core's scope validation catches it at startup, **but only in the Development environment by default**, so in production the app would start happily and misbehave. I'd turn `ValidateScopes` and `ValidateOnBuild` on explicitly."*

**The fix:** inject `IServiceScopeFactory` and create a scope per unit of work — the same pattern you need for a `DbContext` inside a background `IHostedService`.

---

## Q59. What is middleware, and how is it different from a filter?

```csharp
app.Use(async (ctx, next) =>
{
    // runs on the way IN
    await next();
    // runs on the way OUT
});
```

```
A in → B in → endpoint → B out → A out
```

| | Middleware | Filter |
|---|---|---|
| Scope | **Every** request, including static files | MVC actions only |
| Knows about | `HttpContext` | Action, arguments, model, `ModelState` |
| Registered | `app.UseX()` in `Program.cs` | Globally / on a controller / on an action |
| Short-circuit | Don't call `next()` | Set `context.Result` |

> *"Middleware is the outer, framework-agnostic pipeline — logging, HTTPS redirect, static files, auth, CORS. Filters are MVC-aware and can see the action and the model. Rule of thumb: **if it must run for non-MVC requests too, it's middleware; if it needs to know which action is running, it's a filter.** In Spring terms, middleware is a servlet `Filter` and a filter is a `HandlerInterceptor`."*

---

## Q60. Why does middleware order matter? Give the standard order.

```csharp
app.UseExceptionHandler("/Error");   // outermost — must wrap everything below
app.UseHsts();
app.UseHttpsRedirection();
app.UseStaticFiles();                // before routing — short-circuits for files
app.UseRouting();                    // ← SELECTS the endpoint
app.UseCors();
app.UseAuthentication();             // who are you?
app.UseAuthorization();              // are you allowed?   ← must be AFTER UseRouting
app.MapControllers();                // ← EXECUTES the endpoint
```

**The two rules to state:**
> *"`UseAuthorization` must come **after** `UseRouting`, because it needs to know which endpoint was selected in order to read its `[Authorize]` metadata — put it before and you either get an exception or, worse, authorization silently doesn't run. And `UseExceptionHandler` goes first so it wraps everything downstream."*

**The trade-off worth volunteering:** *"`UseStaticFiles` before `UseAuthorization` means everything in `wwwroot` is public. That's the right default for CSS and images, but files that need protecting have to go through a controller."*

---

## Q61. How do you write custom middleware?

```csharp
public class RequestTimingMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<RequestTimingMiddleware> _logger;

    public RequestTimingMiddleware(RequestDelegate next, ILogger<RequestTimingMiddleware> logger)
        => (_next, _logger) = (next, logger);

    public async Task InvokeAsync(HttpContext ctx)
    {
        var sw = Stopwatch.StartNew();
        await _next(ctx);
        _logger.LogInformation("{Path} took {Ms}ms", ctx.Request.Path, sw.ElapsedMilliseconds);
    }
}

app.UseMiddleware<RequestTimingMiddleware>();
```

**The gotcha to mention:** *"The middleware class itself is effectively a **singleton** — constructor dependencies must be singletons. For a scoped service, take it as a parameter on `InvokeAsync` instead, which is resolved per request."*

```csharp
public async Task InvokeAsync(HttpContext ctx, IMyScopedService svc) { }   // ✅
```

---

# J — Security

## Q62. How do you prevent CSRF?

```razor
<form asp-action="Save" method="post">     @* tag helper injects the token automatically *@
    @Html.AntiForgeryToken()               @* explicit form, MVC 5 style *@
</form>
```
```csharp
[HttpPost]
[ValidateAntiForgeryToken]
public IActionResult Save(Product p) { }

[AutoValidateAntiforgeryToken]             // ← better: applies to ALL unsafe verbs
public class ProductsController : Controller { }
```

> *"CSRF is when another site makes the user's browser send an authenticated request to yours — the cookie goes along automatically. The defence is a token in **two places**: a cookie and a hidden form field, which must match. The attacker's site can cause the cookie to be sent but can't read or forge the form field, because the same-origin policy stops it."*

**Two things to add:**
- *"In Core, `<form asp-action>` injects the token automatically — hand-written `<form>` tags are the usual cause of the resulting 400."*
- *"For AJAX, configure a header name (`o.HeaderName = "X-CSRF-TOKEN"`) and send it from JavaScript. And `SameSite=Lax` cookies are a good defence in depth, not a replacement."*

---

## Q63. How do you prevent XSS and SQL injection?

**XSS:**
> *"Razor HTML-encodes every `@expression` by default, so the common case is safe for free. The risk is `Html.Raw`, and `@Html.Raw(userInput)` is a live vulnerability — I'd only use it on HTML I generated or user HTML that's been through a sanitiser. A Content-Security-Policy header is the second layer."*

**SQL injection:**
> *"EF Core parameterises everything by default, including interpolated `FromSqlInterpolated`. The way people still get hit is string-concatenating into `FromSqlRaw` or `ExecuteSqlRaw`."*

```csharp
db.Users.FromSqlRaw($"SELECT * FROM Users WHERE Name = '{name}'");     // 🔓 injectable
db.Users.FromSqlInterpolated($"SELECT * FROM Users WHERE Name = {name}"); // ✅ parameterised
db.Users.Where(u => u.Name == name);                                     // ✅ best
```

---

## Q64. Authentication vs authorization — and how are they done in Core?

> *"**Authentication** establishes who you are; **authorization** decides what you're allowed to do. In Core they're separate middleware in that order, and `[Authorize]` is what enforces the second."*

| Scheme | Use for |
|---|---|
| **Cookie authentication** | Traditional server-rendered MVC apps |
| **ASP.NET Core Identity** | Full user store — registration, password hashing, 2FA, lockout |
| **JWT bearer** | APIs and SPAs (your Angular front-end) |
| **OpenID Connect / OAuth 2** | External providers — Azure AD, Google, Auth0 |

```csharp
[Authorize]                                    // any authenticated user
[Authorize(Roles = "Admin,Manager")]           // role-based
[Authorize(Policy = "MinimumAge18")]           // policy-based (preferred)
[AllowAnonymous]                               // overrides everything, at any scope
```

**Three authorization models:**
- **Role-based** — simple, but roles multiply badly
- **Claims-based** — checks a claim's value
- **Policy-based** — *"the one I'd default to; it's a named rule you can change centrally instead of hunting `Roles = "..."` strings through the codebase"*

```csharp
builder.Services.AddAuthorization(o =>
    o.AddPolicy("MinimumAge18", p => p.RequireClaim("Age", "18")));
```

**⚠️ The trap:** *"`[AllowAnonymous]` **always wins** — it overrides even a global authorization policy. One left on a controller after debugging opens the whole thing, and no global setting will save you. It's a standard code-review grep."*

---

## Q65. What is CORS and when do you need it?

> *"CORS is a browser rule: JavaScript on `site-a.com` can't read a response from `site-b.com` unless the server explicitly allows it. It matters to me directly — an Angular app on `localhost:4200` calling an API on `localhost:5000` is cross-origin, so without CORS the browser blocks it."*

```csharp
builder.Services.AddCors(o => o.AddPolicy("Spa", p => p
    .WithOrigins("https://myapp.com")      // ⚠️ not AllowAnyOrigin in production
    .AllowAnyHeader()
    .AllowAnyMethod()
    .AllowCredentials()));

app.UseCors("Spa");                        // after UseRouting, before UseAuthorization
```

**Two things to volunteer:** *"CORS is enforced by the **browser**, not the server — it's not a security boundary against curl or Postman. And `AllowAnyOrigin` combined with `AllowCredentials` is rejected outright, because it would let any site read authenticated responses."*

---

## Q66. How would you secure a file upload?

```csharp
[HttpPost]
[RequestSizeLimit(5_000_000)]
public async Task<IActionResult> Upload(IFormFile file)
{
    var safeName = $"{Guid.NewGuid()}{Path.GetExtension(file.FileName)}";
    var path = Path.Combine(_uploadRoot, safeName);
    using var stream = System.IO.File.Create(path);
    await file.CopyToAsync(stream);
    return Ok();
}
```

> *"Four things. **Never trust `file.FileName`** — it can contain `..\..\` and traverse out of your folder, so I generate my own name and keep the original only as metadata. **Limit the size**, or you have a denial-of-service. **Validate the content type and the actual file signature**, not just the extension. And **store uploads outside `wwwroot`**, because anything in `wwwroot` is publicly downloadable and an uploaded `.html` or `.js` becomes stored XSS."*

---

# K — EF Core, performance & testing

## Q67. How does Entity Framework Core fit into MVC?

```csharp
builder.Services.AddDbContext<AppDbContext>(o =>
    o.UseSqlServer(builder.Configuration.GetConnectionString("Default")));   // scoped

public class ProductsController : Controller
{
    private readonly AppDbContext _db;
    public ProductsController(AppDbContext db) => _db = db;

    public async Task<IActionResult> Index()
        => View(await _db.Products.AsNoTracking().ToListAsync());
}
```

> *"EF Core is the ORM — it's to .NET what Hibernate/JPA is to Spring. `DbContext` is registered scoped, so it's one unit of work per request, and it's injected into the controller. Code-first with migrations is the usual workflow: change the model, `dotnet ef migrations add`, `dotnet ef database update`."*

Cross-reference **[24 — ORM, JPA & Hibernate](./24-orm-jpa-hibernate.md)** — the concepts map directly: persistence context ≈ change tracker, `LazyInitializationException` ≈ `ObjectDisposedException` after the scope closes, and N+1 is N+1 in both.

---

## Q68. Do you need a repository pattern on top of EF Core?

**A deliberately opinionated question. Have a position.**

> *"Often not. `DbSet<T>` is already a repository and `DbContext` is already a unit of work, so a generic `IRepository<T>` wrapping them usually just adds a layer that leaks `IQueryable` anyway. I'd add a repository when I want to hide EF from the domain layer, or to make a specific set of queries mockable in tests. What I'd avoid is a generic repository per entity added reflexively because a tutorial said so."*

Having a **position with a caveat** is what a senior answer sounds like. Agreeing with whatever the interviewer implies is not.

---

## Q69. What is the N+1 problem and how do you fix it?

```csharp
var orders = db.Orders.ToList();                  // 1 query
foreach (var o in orders)
    Console.WriteLine(o.Customer.Name);           // + N queries 🐌

var orders = db.Orders.Include(o => o.Customer).ToList();   // ✅ 1 query with a JOIN
```

> *"Fetching a list and then touching a navigation property per row fires one query per row. The fixes are **eager loading** with `Include`, **projection** to a DTO with `Select` so you only fetch the columns you need, or **split queries** when a big `Include` produces a cartesian explosion. Projection is usually the best of the three, because it also stops you over-fetching."*

```csharp
var dto = await db.Orders
    .Select(o => new OrderDto { Id = o.Id, CustomerName = o.Customer.Name })
    .AsNoTracking()
    .ToListAsync();
```

---

## Q70. How would you improve the performance of an MVC application?

**Answer in layers — this structure is what makes it sound experienced:**

| Layer | What I'd do |
|---|---|
| **Measure first** | Profile before guessing — MiniProfiler, Application Insights, EF logging. *"I wouldn't optimise anything I hadn't measured."* |
| **Database** | Fix N+1 with `Include`/projection, `AsNoTracking()` for read-only queries, index the columns you filter on, paginate instead of `ToList()` on everything |
| **Async** | `async`/`await` all the way down — it doesn't make one request faster, it frees the thread so the server handles **more** concurrent requests |
| **Caching** | `IMemoryCache` for per-instance, `IDistributedCache`/Redis across instances, `[ResponseCache]` / output caching for whole responses |
| **Payload** | Return DTOs not entities, enable response compression, paginate APIs |
| **Front-end** | Bundle and minify, `asp-append-version` for cache-busting, CDN for static assets |
| **Views** | Avoid queries in views; prefer view components with their own cached data |

**The sentence that lands:** *"The single biggest win in almost every app I've seen is the data layer — an N+1 or a missing index costs more than every micro-optimisation combined."*

---

## Q71. How do you cache in ASP.NET Core?

| Type | API | Scope |
|---|---|---|
| **In-memory** | `IMemoryCache` | One server instance |
| **Distributed** | `IDistributedCache` (Redis, SQL) | All instances |
| **Response caching** | `[ResponseCache]` + middleware | Client/proxy via HTTP headers |
| **Output caching** (.NET 7+) | `app.UseOutputCache()` | Server-side full responses |

```csharp
public async Task<List<Category>> GetCategoriesAsync()
    => await _cache.GetOrCreateAsync("categories", async entry =>
    {
        entry.AbsoluteExpirationRelativeToNow = TimeSpan.FromMinutes(10);
        return await _db.Categories.AsNoTracking().ToListAsync();
    });
```

**The trade-off to volunteer:** *"`IMemoryCache` is per-instance, so behind a load balancer users get inconsistent data and a cache invalidation only clears one server. That's when you move to Redis. And every cache needs an expiry — an unbounded cache is a memory leak."*

⚖️ **MVC 5** used `[OutputCache]`, which Core replaced with `[ResponseCache]` (header-driven) and later true server-side output caching.

---

## Q72. How do you test an MVC application?

```csharp
// UNIT TEST — the controller is just a class
[Fact]
public async Task Index_ReturnsViewWithProducts()
{
    var mockService = new Mock<IProductService>();
    mockService.Setup(s => s.GetAllAsync())
               .ReturnsAsync(new List<Product> { new() { Name = "Pen" } });

    var controller = new ProductsController(mockService.Object);
    var result = await controller.Index();

    var view = Assert.IsType<ViewResult>(result);
    var model = Assert.IsAssignableFrom<List<Product>>(view.Model);
    Assert.Single(model);
}
```

```csharp
// INTEGRATION TEST — the real pipeline, in memory
public class ProductsApiTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly HttpClient _client;
    public ProductsApiTests(WebApplicationFactory<Program> f) => _client = f.CreateClient();

    [Fact]
    public async Task Get_ReturnsSuccess()
    {
        var response = await _client.GetAsync("/api/products");
        response.EnsureSuccessStatusCode();
    }
}
```

> *"Unit tests for controllers and services — this is the payoff for returning result **objects** instead of writing to the response: I can assert on a `ViewResult` with no HTTP involved. Integration tests with `WebApplicationFactory`, which spins up the real app in memory so routing, filters, binding and DI all actually run — that's where you catch the middleware-order and binding bugs a unit test can't see."*

**Tools to name:** xUnit (or NUnit/MSTest), **Moq** for mocking, **FluentAssertions** for readability, `WebApplicationFactory` for integration, EF Core InMemory or Testcontainers for the database.

> 💡 **Bridge to your experience:** *"Same split as JUnit plus Mockito for units and `@SpringBootTest` with MockMvc for integration — `WebApplicationFactory` is the direct equivalent of `@SpringBootTest`."*

---

# L — Answer scripts

## Script 1 — "Tell me about yourself" (for a .NET role, 60 seconds)

> *"I'm Priyanka, a full-stack developer with 4+ years' experience. My front-end is **Angular** — Angular 16 and 18 with RxJS, NgRx and Ionic, including a hybrid mobile app.*
>
> *On the backend I've worked across two stacks. On **Subsea**, a marine equipment and operations platform, the backend was a **.NET Web API** with Angular in front, in Docker on Azure — I owned the Schedule-Manager module end to end, both the Angular feature module and the .NET endpoints behind it. My other backend experience is **Java with Spring Boot** — REST APIs, dependency injection, layered architecture and JPA.*
>
> *Having done both, what strikes me is how closely they map — filters are Spring's interceptors, middleware is the servlet filter chain, EF Core sits where JPA does. My .NET work has been API-side rather than Razor MVC, since the UI was always Angular, and that's the part I've been deliberately closing.*
>
> *I'm looking for a role where I can keep doing full-stack work — Angular on the front, .NET on the back — which is exactly the shape Subsea had."*

**Why this works:** real shipped .NET first, the second stack second, the honest boundary (API not Razor) third and briefly, and it closes by naming the role shape you've already succeeded in. It never apologises and never overclaims.

---

## Script 2 — Project walkthrough, translated

**For a .NET role, lead with Subsea — it's the .NET one.** Don't default to RoboGebra out of habit.

> *"Subsea is a marine equipment and operations management platform — Angular SPA, **.NET Web API** services, Docker, Azure. I owned the **Schedule-Manager** module front to back. The part I'd highlight is a performance problem: the Kendo grids were client-bound, so as the schedule datasets grew the browser was loading everything and the grid got sluggish. I moved paging, sorting and filtering server-side using Kendo's `DataSourceRequest`, which translates straight into SQL paging, so only one page crossed the wire. Combined with virtual scrolling and trimming the columns to what we actually display, the grid stayed responsive at scale. I also built the dynamic category/subcategory hierarchy as cascading selects driven from the API rather than hardcoded, and refactored the repeated grid configuration into shared components so other modules weren't copy-pasting Kendo setup."*

**Then, if the second project comes up, translate as you go:**

> *"RoboGebra is an education platform — Angular and Ionic on the front, Spring Boot services on the back, MongoDB for storage. In .NET terms that architecture would be an **ASP.NET Core Web API** with controllers and DTOs, **EF Core** where we had the ODM layer, **JWT bearer authentication** where we used tokens, and the cross-cutting logging we did with interceptors would be an **action filter** or middleware."*

> 💡 **Say the translation out loud** for the Java project — it demonstrates the mapping instead of asserting it. But lead with Subsea: for a .NET interviewer, a shipped .NET project beats any amount of mapping.

---

## Script 3 — When you genuinely don't know

> *"I haven't used that specific feature. My understanding is it does X — in Spring the equivalent is Y — but I'd want to confirm that rather than guess. How is it used in your codebase?"*

Three things at once: honesty, demonstrated reasoning, and turning it into a conversation. **Never bluff a .NET version number or an API signature** — it's checkable in seconds and it costs you the round.

---

## Script 4 — Questions to ask them

- *"Is the codebase on .NET Framework or .NET 6/8? Is there a migration in progress?"* — tells you which half of this file matters, and shows you know they're different
- *"MVC with Razor views, or an API with a separate SPA front-end?"* — lets you steer toward your Angular strength
- *"How is the team structured — is the front-end and back-end the same people?"*
- *"What does the testing and code-review process look like?"*
- *"What would the first three months look like for someone joining?"*

---

# M — Rapid-fire table

> Cover the right column. Target: **under 6 minutes**, daily.

| # | Question | Answer |
|---|---|---|
| 1 | What is MVC? | Model = data + rules, View = UI, Controller = handles request, picks result |
| 2 | Why MVC over Web Forms? | Testable, clean URLs, full HTML control, stateless, fits SPAs |
| 3 | Model vs view model vs DTO | Entity / what one view needs / what crosses a boundary |
| 4 | MVC vs Razor Pages | Controller-centric vs page-centric; same underlying stack |
| 5 | .NET Framework vs .NET 5+ | Windows-only legacy vs cross-platform unified; even versions are LTS |
| 6 | Biggest MVC 5 → Core changes | Cross-platform, built-in DI, MVC+API unified, middleware, `appsettings.json` |
| 7 | What is Kestrel? | Core's built-in cross-platform web server; reverse proxy in front |
| 8 | What is `wwwroot`? | The only publicly served folder — and it's **not** covered by `[Authorize]` |
| 9 | Request lifecycle | Middleware → routing → filters → binding → action → result → response |
| 10 | Where does the view render? | Result execution — **after** the action returns |
| 11 | `AddX` vs `UseX` | Register a service vs run it in the pipeline |
| 12 | Config sources | appsettings → appsettings.{Env} → user secrets → env vars → CLI |
| 13 | Conventional vs attribute routing | Central pattern vs `[Route]` on the action |
| 14 | `{id?}` + `int id` | Route optional ≠ parameter optional → MVC5 throws, Core binds 0 |
| 15 | Route constraint fails | **404** — matching, not validation |
| 16 | What are Areas? | Sub-app folders (Admin) with their own controllers/views |
| 17 | Generate URLs how? | `Url.Action` / `asp-action` tag helper — never hard-code |
| 18 | What makes a method an action? | It's **public** on a controller — use `[NonAction]` or private |
| 19 | `Controller` vs `ControllerBase` | Views vs API-only |
| 20 | `IActionResult` vs `ActionResult<T>` | Flexible vs flexible **and** self-documenting |
| 21 | Why return a result object? | Testable, composable; result filters run in between |
| 22 | `[ActionName]` use | Two reachable actions at the same URL (Delete/DeleteConfirmed) |
| 23 | Why must Delete be POST? | GET must be safe — crawlers and `<img src>` would delete data |
| 24 | What is Razor? | View engine mixing C# into HTML with `@`; compiles to a class |
| 25 | Strongly typed vs ViewBag | Compile-checked vs a typo that renders blank |
| 26 | HTML vs tag helpers | Method calls vs HTML-looking attributes; tag helpers are Core-only |
| 27 | `_ViewStart` / `_Layout` / `_ViewImports` | Pre-view setup / template / shared usings |
| 28 | Layout execution order | **View first**, then layout — view can set ViewBag for the layout |
| 29 | `RenderBody` vs `RenderSection` | Once, required / named slot, optional with `required:false` |
| 30 | Partial vs view component | Markup reuse vs a widget with its own logic and DI |
| 31 | `@section` in a partial | Doesn't work — sections are view↔layout |
| 32 | Editor templates | Convention-based; give collections correct indexed names |
| 33 | ViewBag vs ViewData | The **same dictionary** — dynamic wrapper vs `object` |
| 34 | TempData lifetime | Deleted **on read**, not by age; `Peek`/`Keep` to preserve |
| 35 | TempData storage in Core | Encrypted cookie by default — simple types only, ~4KB |
| 36 | ViewBag across a redirect | Gone — 302 means a new request |
| 37 | #1 real ViewBag bug | Dropdown data not repopulated before `return View(model)` |
| 38 | Session cost | Needs `AddSession`+`UseSession`; in-process by default → breaks when scaled |
| 39 | Model binding order | **Form → Route → Query** |
| 40 | JSON posts as empty model | Complex types bind from form — add `[FromBody]` or `[ApiController]` |
| 41 | Over-posting | Binder sets fields your form never rendered → bind to a DTO |
| 42 | Binding failure | Sets default + ModelState error — **the action still runs** |
| 43 | `[Required]` on `int` | Can't catch 0 — use `[Range]` or `int?` |
| 44 | Client validation | UX only — `data-val-*` + jQuery unobtrusive; server is the real check |
| 45 | Cross-property validation | `IValidatableObject` |
| 46 | `[ApiController]` + invalid model | Automatic 400 **before** the action runs |
| 47 | Five filter types | Authorization, Resource, Action, Exception, Result |
| 48 | Filter scope order | Global → Controller → Action in; reversed out; `Order` beats scope |
| 49 | What exception filters miss | **Result execution** — a view exception needs middleware |
| 50 | Filter with dependencies | `[ServiceFilter]` / `[TypeFilter]` — attributes can't take DI |
| 51 | Short-circuit a filter | Set `context.Result` — also skips its own `OnActionExecuted` |
| 52 | DI lifetimes | Transient = per injection, Scoped = per request, Singleton = per app |
| 53 | Lifetime for `DbContext` | **Scoped** — not thread-safe, tracker would grow forever |
| 54 | Captive dependency | Singleton holding a scoped service; validated in Dev only |
| 55 | Middleware vs filter | Every request vs MVC-aware; servlet Filter vs HandlerInterceptor |
| 56 | `UseAuthorization` position | **After** `UseRouting`, before `MapControllers` |
| 57 | Custom middleware DI | Class is a singleton — take scoped services on `InvokeAsync` |
| 58 | CSRF defence | Token in a cookie **and** a form field; `[ValidateAntiForgeryToken]` |
| 59 | XSS defence | Razor auto-encodes; `Html.Raw` is the vector; add CSP |
| 60 | SQL injection in EF | Only via string-concatenated `FromSqlRaw` |
| 61 | AuthN vs AuthZ | Who you are vs what you may do; separate middleware, in that order |
| 62 | Authorization models | Role / claims / **policy** (preferred) |
| 63 | `[AllowAnonymous]` | **Always wins**, overrides global policies |
| 64 | What is CORS? | Browser rule for cross-origin JS; not a defence against curl |
| 65 | Secure file upload | Don't trust `FileName`, limit size, check signature, store outside `wwwroot` |
| 66 | EF Core in MVC | ORM; `DbContext` scoped, injected; code-first + migrations |
| 67 | Repository over EF? | Often unnecessary — `DbSet` is a repo, `DbContext` is a UoW |
| 68 | N+1 fix | `Include`, projection with `Select`, or split queries |
| 69 | Performance priorities | Measure → data layer → async → cache → payload → front-end |
| 70 | Caching options | `IMemoryCache` / `IDistributedCache` / `[ResponseCache]` / output caching |
| 71 | `IMemoryCache` limitation | Per-instance — inconsistent behind a load balancer |
| 72 | Testing an MVC app | xUnit + Moq for units; `WebApplicationFactory` for integration |

---

## 🎤 How to answer in the room

**1. Three sentences, always.** Definition → one concrete thing (from your project or a named example) → one trade-off or failure mode. Your rounds have been lost on the **second** question, not the first — the trade-off sentence *is* the second question, answered before it's asked.

**2. Locate the question in the lifecycle.** *"That's a model-binding question, so we're after routing and before the action."* Naming the stage is the skill; the fact is secondary.

**3. Say "Core or Framework?" when it genuinely matters.** Not dodging — it's the correct question, and interviewers read it as experience.

**4. Use the Spring mapping out loud.** *"In Spring this is a `HandlerInterceptor`; here it's an action filter — same position in the pipeline."* That single sentence converts four years of Java into four years of relevant experience.

**5. Never bluff a version number or an API signature.** Checkable in seconds. *"I'd confirm that rather than guess"* costs you nothing; a confident wrong answer costs you the round.

---

## Related files in this pack

- **[28 — MVC "What Happens?" (62 programs)](./28-dotnet-mvc-tricky-questions.md)** — the code round. **Do this file first, then 28.**
- **[27 — C# / .NET "Predict the Output" (75 programs)](./27-dotnet-output-tricky-questions.md)** — the language round
- **[06 — Spring & Spring Boot](./06-spring-boot.md)** — read alongside § 0's mapping table
- **[24 — ORM, JPA & Hibernate](./24-orm-jpa-hibernate.md)** — EF Core is the same concepts: change tracker, N+1, lazy loading
- **[17 — SOLID & Design Patterns](./17-solid-design-patterns.md)** — filters are Chain of Responsibility; DI is Dependency Inversion
- **[00 — Self Introduction](./00-self-introduction.md)** — adapt with Script 1 above for .NET roles
- **[26 — Companies: Questions Actually Asked](./26-companies-asked-questions.md)** — **log every .NET round here the same day**

> 🔁 **Study order:** § 0 (the honesty position) → the ⭐ 15 most-likely → sections B (lifecycle) and F (ViewBag/TempData), which are the two most-asked areas → then G, H, I. Run the rapid-fire table daily under 6 minutes. **Then move to [file 28](./28-dotnet-mvc-tricky-questions.md)** for the code half — theory gets you through the first twenty minutes, the programs decide the round.
