# ASP.NET MVC — 62 Tricky "What Happens?" Programs with Answers

> 🔴 **Why this file exists:** your interviews ask **output-based programs**, not theory (*"what does this print?"*). MVC rounds do the same thing in a different shape: *"this action runs — what does the browser get?"*, *"this filter and that filter — which order?"*, *"the model says X but the textbox shows Y — why?"*
>
> This is the MVC twin of **[23 — Java Predict the Output](./23-java-output-tricky-questions.md)**.

**Every question below uses the same format:**

1. **The code**
2. **What actually happens** (or *exception* / *404* / *400*)
3. **The rule** — the one sentence that makes it obvious
4. **The follow-up** they ask next

> 📌 **Framework coverage.** Answers are for **ASP.NET Core MVC** (the default in any new .NET job). Wherever classic **ASP.NET MVC 5** behaves differently there's a **⚖️ In MVC 5** box. Knowing *both* is the point — *"what changed in Core?"* is itself one of the most-asked MVC questions, and answering it proves you understand the mechanism rather than the syntax.

> 💡 **Golden habit — ask these four in order, every time:**
> **(a) Did routing even match this action?** → **(b) Did model binding succeed, and where did the value come from?** → **(c) Which filters ran, in what order?** → **(d) Only then look at the action body.**
>
> Candidates lose MVC questions at (a), (b) and (c). Almost nobody loses them at (d) — but almost everybody *starts* at (d).

---

## Table of contents

| # | Section | What it tests |
|---|---|---|
| [A](#a--routing--action-selection) | Routing & action selection (Q1–Q8) | Route order, attribute vs conventional, ambiguity, optional params |
| [B](#b--model-binding) | Model binding (Q9–Q17) | Source priority, `[FromBody]`, prefixes, collections, silent failures |
| [C](#c--validation--modelstate) | Validation & ModelState (Q18–Q22) | `IsValid` timing, `[Required]` on value types, auto-400 |
| [D](#d--viewbag-viewdata--tempdata) | ViewBag / ViewData / TempData (Q23–Q29) | `dynamic` typos, the shared dictionary, one-request lifetime |
| [E](#e--action-results--redirects) | Action results & redirects (Q30–Q35) | `View` vs `Redirect`, PRG, code after a redirect, `JsonRequestBehavior` |
| [F](#f--filters--execution-order) | Filters & order (Q36–Q42) | Global/controller/action order, short-circuiting, what exception filters miss |
| [G](#g--razor-view-traps) | Razor traps (Q43–Q51) | 🔥 **The `TextBoxFor` / ModelState trap**, `@` parsing, encoding, layout order |
| [H](#h--controller-lifecycle--state) | Controller lifecycle & state (Q52–Q55) | Per-request instances, static fields, `async void`, `HttpContext.Current` |
| [I](#i--di--the-middleware-pipeline-core) | DI & middleware (Q56–Q62) | Lifetimes, captive dependencies, pipeline order, `UseX` placement |
| [J](#j--rapid-fire-answer-table) | Rapid-fire table | 1-line answers for last-minute revision |
| [K](#k--18-self-test-drills-answers-hidden) | 18 self-test drills | Answers hidden — test yourself |
| [L](#l--how-to-answer-these-in-the-actual-interview) | How to answer in the room | The script that turns a quiz into a conversation |

---

# A — Routing & action selection

## Q1. Which route wins?

```csharp
// Program.cs
app.MapControllerRoute(
    name: "blog",
    pattern: "{controller}/{action}/{slug}");

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}");
```

```
GET /Products/Details/5
```

```csharp
public class ProductsController : Controller
{
    public IActionResult Details(int id)   => Content($"id={id}");
    public IActionResult Details(string slug) => Content($"slug={slug}");
}
```

### ✅ What happens

```
💥 AmbiguousMatchException:
   The request matched multiple endpoints.
```

### The rule

> **"Conventional routes are matched in the order they're registered — the *first* match wins and routing stops. But two actions with the same name and the same HTTP verb are ambiguous at *action selection* time, which happens after routing."**

Two separate mechanisms, two separate failures. Route order picks the **route**; action selection picks the **method** — and here it can't.

### 🔥 The follow-ups

**Follow-up 1 — "Fix it without renaming."**
```csharp
[HttpGet("Products/Details/{id:int}")]
public IActionResult Details(int id) => Content($"id={id}");

[HttpGet("Products/Details/{slug}")]
public IActionResult Details(string slug) => Content($"slug={slug}");
```
> The `:int` **route constraint** makes the two mutually exclusive, so only one endpoint can ever match.

**Follow-up 2 — "If I remove the second action, which route matches?"**
> The **`blog`** route — it was registered first and `{controller}/{action}/{slug}` matches `/Products/Details/5` perfectly. `slug` would bind the string `"5"`. **Registration order is the whole answer.** Put specific routes before general ones.

---

## Q2. Attribute routing silently disables conventional routing

```csharp
app.MapControllerRoute("default", "{controller=Home}/{action=Index}/{id?}");

public class ProductsController : Controller
{
    [Route("catalog/items")]
    public IActionResult Index() => Content("Index");

    public IActionResult List() => Content("List");
}
```

```
GET /catalog/items    →  ?
GET /Products/Index   →  ?
GET /Products/List    →  ?
```

### ✅ What happens

```
GET /catalog/items    →  200  "Index"
GET /Products/Index   →  404
GET /Products/List    →  200  "List"
```

### The rule

> **"Once an action has a `[Route]` attribute, it is reachable ONLY through that attribute — conventional routes can no longer reach it. Actions *without* an attribute on the same controller still use conventional routing."**

That mixed state (`List` works conventionally, `Index` doesn't) is exactly what makes this a good interview question. It looks like the whole controller switched over. It didn't.

### 🔥 Follow-up — "And if I put `[Route]` on the controller?"
```csharp
[Route("catalog/[controller]/[action]")]
public class ProductsController : Controller { }
```
> Then **every** action is attribute-routed and the conventional route reaches none of them. `[controller]` and `[action]` are **token replacements** filled in at startup — using them keeps the URL in sync when you rename the class.

---

## Q3. Two actions, one name, different verbs

```csharp
[HttpGet]
public IActionResult Edit(int id) => View();

[HttpPost]
public IActionResult Edit(Product p) => RedirectToAction("Index");
```

### ✅ What happens

```
✅ Works perfectly — no ambiguity
```

### The rule

> **"Action selection filters candidates by HTTP verb FIRST. Two same-named actions are fine as long as no single request could match both."**

⚠️ But this is **not** overloading in the C# sense — the CLR allows it only because the *signatures* differ. Two GET actions with the **same signature** don't compile at all, and two GET actions with **different** signatures give you Q1's `AmbiguousMatchException` at runtime.

### 🔥 Follow-up — "How do you have two GETs with the same name?"
```csharp
[HttpGet]
public IActionResult Edit(int id) => View();

[HttpGet]
[ActionName("EditByName")]          // ← different *action* name, same C# method name
public IActionResult Edit(string name) => View();
```
> `[ActionName]` decouples the **routing name** from the **method name**. Same trick is used to expose `Delete` as a POST-only action named `Delete` while the C# method is `DeleteConfirmed`.

---

## Q4. Missing `id` on a non-nullable parameter

```csharp
// route: {controller=Home}/{action=Index}/{id?}

public IActionResult Details(int id) => Content($"id = {id}");
```

```
GET /Home/Details
```

### ✅ What happens — **and this is where Core and MVC 5 differ completely**

**ASP.NET Core:**
```
200 OK
id = 0
```
The action **runs**, `id` is `0`, and `ModelState` contains an error — which nobody checks, so the bug ships silently.

### ⚖️ In MVC 5
```
💥 ArgumentException:
   The parameters dictionary contains a null entry for parameter 'id' of
   non-nullable type 'System.Int32' for method 'Details(Int32)'.
   An optional parameter must be a reference type, a nullable type,
   or be declared as an optional parameter.
```
> Memorize that error message — it's one of the most-quoted exceptions in ASP.NET MVC, and being able to recite what it *means* ("the route made `id` optional but the method signature didn't") is an instant credibility win.

### The rule

> **"A route can make a segment optional; a non-nullable parameter cannot be optional. MVC 5 throws. Core binds the default value and records a ModelState error instead — so it fails silently unless you check `ModelState.IsValid`."**

### 🔥 Follow-up — "Three ways to fix it."
```csharp
public IActionResult Details(int? id)     { if (id == null) return NotFound(); }  // nullable
public IActionResult Details(int id = 0)  { }                                     // default value
[HttpGet("Home/Details/{id:int}")]                                                // constraint → 404 instead
public IActionResult Details(int id)      { }
```
> The **third** is usually the right one: a request with no id isn't a valid request, so it should 404 at routing, not reach your code at all.

---

## Q5. Route constraint mismatch

```csharp
app.MapControllerRoute("default", "{controller=Home}/{action=Index}/{id:int}");

public IActionResult Details(int id) => Content($"id = {id}");
```

```
GET /Home/Details/abc
GET /Home/Details
```

### ✅ What happens

```
GET /Home/Details/abc   →  404
GET /Home/Details       →  404
```

### The rule

> **"A route constraint is part of MATCHING, not validation. If the constraint fails, the route simply doesn't match — you get a 404, never a 400 and never an exception."**

The second 404 catches people out: `{id:int}` without `?` makes the segment **required**.

### 🔥 Follow-up — "Constraints you'd actually use?"
> `{id:int}`, `{id:guid}`, `{id:min(1)}`, `{slug:alpha}`, `{code:length(6)}`, `{date:datetime}`, `{id:regex(^\\d{{4}}$)}`. And the point worth making: **`:int` is a routing decision, `[Range]` is a validation decision.** Use routing to reject requests that aren't for you; use validation to reject requests that are for you but wrong.

---

## Q6. Route defaults vs the action's default

```csharp
app.MapControllerRoute("default", "{controller=Home}/{action=Index}/{id=99}");

public IActionResult Index(int id = 5) => Content($"id = {id}");
```

```
GET /
```

### ✅ What happens

```
id = 99
```

### The rule

> **"The route default supplies a *value* into route data, and model binding finds it there. The C# default only applies when binding produces nothing at all — and here it produced 99."**

### 🔥 Follow-up — "Remove `=99` from the route. Now?"
> `id = 5` — nothing binds, so the C# default kicks in. **Route defaults win over method defaults**, because they run first.

---

## Q7. Catch-all and the order of `MapControllerRoute`

```csharp
app.MapControllerRoute("catchall", "{*url}",
    defaults: new { controller = "Error", action = "NotFound" });

app.MapControllerRoute("default", "{controller=Home}/{action=Index}/{id?}");
```

```
GET /Products/Index
```

### ✅ What happens

```
Every request goes to Error/NotFound — the whole site is broken
```

### The rule

> **"`{*url}` is a catch-all: it matches everything, including the empty string. Registered first, it shadows every route below it. Conventional routes are evaluated top-down."**

### 🔥 Follow-up — "So where does a catch-all belong?"
> **Last, always.** And in Core the better tool is `app.UseStatusCodePagesWithReExecute("/Error/{0}")` or exception-handling middleware — a catch-all *route* also swallows requests you'd rather see fail honestly.

⚖️ **In MVC 5** the same rule applies to `RouteConfig.RegisterRoutes` — and it's why the generated template always ends with the `Default` route and puts `routes.IgnoreRoute("{resource}.axd/{*pathInfo}")` *first*.

---

## Q8. Every public method is an action

```csharp
public class AccountController : Controller
{
    public IActionResult Index() => View();

    public string BuildConnectionString(string server, string user, string pwd)
        => $"Server={server};User={user};Password={pwd}";
}
```

```
GET /Account/BuildConnectionString?server=db1&user=sa&pwd=secret
```

### ✅ What happens

```
200 OK
Server=db1;User=sa;Password=secret
```

### The rule

> **"Every PUBLIC method on a controller is an action by default and is routable. A helper method you forgot to make private is a public endpoint."**

### 🔥 The follow-ups

**Follow-up 1 — "How do you stop it?"**
> Make it `private`/`protected` (best), or mark it `[NonAction]` if it genuinely must stay public.

**Follow-up 2 — "Why does a `string` return type still work?"**
> Core wraps a non-`IActionResult` return value automatically — a `string` becomes a `ContentResult` with `text/plain`. A `void` action becomes an `EmptyResult` (200, empty body).

**Follow-up 3 — "Is this a real vulnerability?"**
> Yes, and it's a standard finding in .NET security reviews. Combine it with the fact that `[Authorize]` on the *controller* would still protect it, and you've given the complete answer: **defence in depth — make it private AND authorize the controller.**

---

# B — Model binding

## Q9. Same key in the form, the route and the query string

```csharp
// route: {controller}/{action}/{id?}
public IActionResult Save(int id) => Content($"id = {id}");
```

```
POST /Home/Save/1?id=2
Content-Type: application/x-www-form-urlencoded

id=3
```

### ✅ What happens

```
id = 3
```

### The rule

> **"The default value providers are tried in a fixed order: FORM → ROUTE → QUERY STRING. The first one holding the key wins."**

```
Form data      ← highest priority (3)
Route values   ← (1)
Query string   ← lowest priority (2)
```

### 🔥 Follow-up — "How do you force a specific source?"
```csharp
public IActionResult Save([FromQuery] int id) => Content($"id = {id}");   // → 2
public IActionResult Save([FromRoute] int id) => Content($"id = {id}");   // → 1
public IActionResult Save([FromForm]  int id) => Content($"id = {id}");   // → 3
```
> `[FromQuery]`, `[FromRoute]`, `[FromForm]`, `[FromHeader]`, `[FromBody]`, `[FromServices]`. **Being explicit is the right habit** — implicit source precedence is exactly the kind of thing that changes behaviour when someone adds a query parameter months later.

---

## Q10. `[FromBody]` on two parameters

```csharp
[HttpPost]
public IActionResult Save([FromBody] Product p, [FromBody] int quantity) => Ok();
```

### ✅ What happens

```
💥 InvalidOperationException at startup / first request:
   Action has more than one parameter bound from the request body.
```

### The rule

> **"The request body is a forward-only stream that can be read ONCE. At most one parameter may be `[FromBody]`."**

### 🔥 Follow-up — "So how do you send two things?"
> Wrap them in one DTO — which is the design the framework is pushing you toward:
```csharp
public record SaveRequest(Product Product, int Quantity);

[HttpPost]
public IActionResult Save([FromBody] SaveRequest req) => Ok();
```

---

## Q11. Complex type from a form vs from JSON

```csharp
public class Product { public int Id { get; set; } public string Name { get; set; } }

public class ProductsController : Controller     // ← MVC controller, no [ApiController]
{
    [HttpPost]
    public IActionResult Save(Product p) => Content($"{p.Id}/{p.Name}");
}
```

```
Request A:  POST with form fields   Id=1&Name=Pen
Request B:  POST with JSON body     {"Id":1,"Name":"Pen"}
```

### ✅ What happens

```
A →  1/Pen
B →  0/          ← everything is default!
```

### The rule

> **"In an MVC controller, a complex type binds from FORM VALUES by default, never from the JSON body. You must add `[FromBody]` to read JSON."**

### 🔥 Follow-up — "And in an API controller?"
```csharp
[ApiController]                       // ← this changes the default
[Route("api/[controller]")]
public class ProductsController : ControllerBase
{
    [HttpPost]
    public IActionResult Save(Product p) => Ok();   // now binds from the JSON body
}
```
> **`[ApiController]` flips the default inference:** complex types come from the body, simple types from the route/query. That one attribute is behind a large share of "why is my model empty?" questions.

⚖️ **In MVC 5** there are two entirely separate stacks — `System.Web.Mvc` (form-first) and `System.Web.Http` / Web API (`[FromBody]`, body-first), with **different `Controller` base classes and different filter types**. Core unified them, and that unification is a great thing to mention.

---

## Q12. The name has to match — including the prefix

```csharp
public class Order { public Customer Customer { get; set; } }
public class Customer { public string Name { get; set; } }

[HttpPost]
public IActionResult Save(Order order) => Content(order.Customer?.Name ?? "null");
```

```
Form A:   Name=Ann
Form B:   Customer.Name=Ann
Form C:   order.Customer.Name=Ann
```

### ✅ What happens

```
A →  null
B →  Ann
C →  Ann
```

### The rule

> **"Binding is by NAME PATH, not by shape. Nested properties need the dotted path. The top-level parameter name is an OPTIONAL prefix — the binder tries `order.Customer.Name` first, then falls back to `Customer.Name`."**

### 🔥 Follow-up — "How do I force the prefix?"
```csharp
public IActionResult Save([Bind(Prefix = "o")] Order order) { }   // now expects o.Customer.Name
```
> Needed when one form posts two objects of the same type (`billing.` and `shipping.`). And in Razor, `@Html.EditorFor(m => m.Customer.Name)` generates `name="Customer.Name"` **for free** — which is the real reason the helpers exist.

---

## Q13. Binding a collection

```csharp
[HttpPost]
public IActionResult Save(List<Product> items) => Content($"count = {items?.Count}");
```

```
Form A:  items[0].Name=Pen&items[1].Name=Ink
Form B:  items[0].Name=Pen&items[2].Name=Ink
Form C:  items=Pen&items=Ink
```

### ✅ What happens

```
A →  count = 2
B →  count = 1      ← ⚠️ index 2 is silently dropped
C →  count = 0
```

### The rule

> **"Collection indices must be CONSECUTIVE from zero. The binder stops at the first gap. And a repeated simple key only binds to a collection of *simple* types, not complex ones."**

### 🔥 Follow-up — "How do you handle deletes in a dynamic form then?"
```html
<input type="hidden" name="items.Index" value="7" />
<input type="text"   name="items[7].Name" />
```
> The **`.Index` hidden field** lets you use non-consecutive keys — the standard trick for "add/remove row" forms where the user deleted row 1. Knowing this is a strong sign of real MVC form experience rather than tutorial experience.

---

## Q14. Binding failure does **not** stop the action

```csharp
[HttpPost]
public IActionResult Save(int quantity)
{
    return Content($"quantity = {quantity}, valid = {ModelState.IsValid}");
}
```

```
POST  quantity=abc
```

### ✅ What happens

```
quantity = 0, valid = False
```

### The rule

> **"A binding failure sets the parameter to its default and records a ModelState error — it does NOT prevent the action from running. If you don't check `ModelState.IsValid`, you process garbage."**

This is the single most consequential MVC trap on this page, because nothing crashes and nothing logs.

### 🔥 Follow-up — "Make it impossible to forget."
```csharp
// Option 1 — [ApiController] auto-returns 400 before the action runs
[ApiController] public class OrdersController : ControllerBase { }

// Option 2 — a global filter for MVC controllers
public class ValidateModelAttribute : ActionFilterAttribute
{
    public override void OnActionExecuting(ActionExecutingContext c)
    {
        if (!c.ModelState.IsValid) c.Result = new BadRequestObjectResult(c.ModelState);
    }
}
builder.Services.AddControllers(o => o.Filters.Add<ValidateModelAttribute>());
```
> *"I don't rely on every developer remembering `if (!ModelState.IsValid)` — I make it a filter."* That's the answer that sounds like 4 years of experience.

---

## Q15. Over-posting / mass assignment

```csharp
public class User
{
    public int Id { get; set; }
    public string Name { get; set; }
    public bool IsAdmin { get; set; }        // ← not on the form
}

[HttpPost]
public IActionResult Register(User user) { db.Add(user); db.SaveChanges(); return Ok(); }
```

```
POST  Name=Ann&IsAdmin=true
```

### ✅ What happens

```
A user is created with IsAdmin = true. 🔓
```

### The rule

> **"The binder fills EVERY public settable property it finds a matching key for — including ones your form never rendered. Attackers add the extra field by hand."**

### 🔥 Follow-up — "Fixes, best first."
```csharp
// 1. BEST — bind to a DTO that physically cannot carry the field
public record RegisterDto(string Name);

// 2. Whitelist
public IActionResult Register([Bind("Name")] User user) { }

// 3. Blacklist — weakest; a new property added later is unprotected by default
public IActionResult Register([Bind(Exclude = "IsAdmin")] User user) { }
```
> Say **"never bind directly to your EF entity"** out loud. It answers the security question and the architecture question in one sentence.

---

## Q16. Unchecked checkbox

```csharp
public class Settings { public bool SendEmail { get; set; } }
```
```html
<input type="checkbox" name="SendEmail" value="true" />
```

### ✅ What happens

```
Checked   →  SendEmail = true
Unchecked →  the browser posts NOTHING for that field  →  SendEmail = false
```
…which *looks* right, until you realise you can never tell "unchecked" from "field absent" — so a partial update wipes the setting.

### The rule

> **"An unchecked checkbox is not submitted at all by the browser. MVC handles it by rendering a HIDDEN companion input with `value=\"false\"`."**

```html
<!-- what @Html.CheckBoxFor(m => m.SendEmail) actually renders -->
<input type="checkbox" name="SendEmail" value="true" />
<input type="hidden"   name="SendEmail" value="false" />
```
> With both present, the binder takes the **first** value for `true` and falls back to the hidden `false`. **This is why you use `CheckBoxFor` instead of hand-writing the input** — a genuinely good "why do the helpers exist?" answer.

---

## Q17. `[FromServices]` vs constructor injection

```csharp
public IActionResult Report([FromServices] IReportService svc) => Content(svc.Run());
```

### ✅ What happens

```
✅ Works — the service is resolved from DI per request, not bound from the request
```

### The rule

> **"`[FromServices]` resolves the parameter from the DI container instead of the request. Use it when only ONE action needs a heavy dependency, so the controller's constructor doesn't resolve it on every request."**

### 🔥 Follow-up — "Downside?"
> It hides the dependency from the constructor, which makes unit testing and dependency review harder. Default to constructor injection; reach for `[FromServices]` only for the expensive-and-rarely-used case.

---

# C — Validation & ModelState

## Q18. `[Required]` on a non-nullable `int`

```csharp
public class Order
{
    [Required] public int Quantity { get; set; }
}
```

```
POST  Quantity=0
```

### ✅ What happens

```
ModelState.IsValid == true      ← 0 passes [Required]
```

### The rule

> **"`[Required]` only checks for NULL. A non-nullable `int` can never be null — `0` is a perfectly good value — so `[Required]` on it can only ever catch a *missing* field, never a zero."**

### 🔥 Follow-up — "So how do you require a real quantity?"
```csharp
[Range(1, int.MaxValue, ErrorMessage = "Quantity must be at least 1")]
public int Quantity { get; set; }

// or, to distinguish 'not supplied' from 'zero':
[Required] public int? Quantity { get; set; }
```
> Note the second form: **making it nullable is what gives `[Required]` something to do.** Same trap shows up with `DateTime` (`01/01/0001` passes) and `bool` (`false` passes).

---

## Q19. Adding an error after checking `IsValid`

```csharp
[HttpPost]
public IActionResult Save(Product p)
{
    if (ModelState.IsValid)
    {
        if (db.Exists(p.Name))
            ModelState.AddModelError("Name", "Already exists");

        db.Add(p);                       // ← runs anyway
        return RedirectToAction("Index");
    }
    return View(p);
}
```

### ✅ What happens

```
The duplicate is saved. The error message is added and then thrown away by the redirect.
```

### The rule

> **"`ModelState.IsValid` is evaluated at the moment you read it — it is not a latch. Adding an error afterwards changes `IsValid` to `false`, but your code has already committed to the success path."**

### 🔥 Follow-up — "Correct shape?"
```csharp
if (!ModelState.IsValid) return View(p);

if (db.Exists(p.Name))
{
    ModelState.AddModelError("Name", "Already exists");
    return View(p);                      // ← re-check by returning, not by re-reading IsValid
}

db.Add(p);
return RedirectToAction("Index");
```
> **Guard clauses, one return per failure.** Also mention the `""` key: `AddModelError("", "...")` produces a *model-level* error shown by `@Html.ValidationSummary()` rather than next to a field.

---

## Q20. `[ApiController]` returns 400 before your action

```csharp
[ApiController]
[Route("api/[controller]")]
public class OrdersController : ControllerBase
{
    [HttpPost]
    public IActionResult Save(Order o)
    {
        Console.WriteLine("action ran");
        if (!ModelState.IsValid) return BadRequest();
        return Ok();
    }
}
```
with `Order.Name` marked `[Required]`, posting `{}`.

### ✅ What happens

```
400 Bad Request with an RFC 7807 ProblemDetails body
"action ran" is NEVER printed
```

### The rule

> **"`[ApiController]` installs an automatic model-validation filter that short-circuits with a 400 *before* the action executes. Your `ModelState.IsValid` check inside the action is dead code."**

### 🔥 Follow-up — "How would you customise or disable that?"
```csharp
builder.Services.Configure<ApiBehaviorOptions>(o =>
{
    o.SuppressModelStateInvalidFilter = true;                 // turn it off
    // or shape the response:
    o.InvalidModelStateResponseFactory = ctx => new BadRequestObjectResult(
        new { errors = ctx.ModelState.Where(e => e.Value.Errors.Any()) });
});
```

---

## Q21. Client-side validation without the script

```csharp
public class Product { [Required] public string Name { get; set; } }
```
```html
@Html.TextBoxFor(m => m.Name)
@Html.ValidationMessageFor(m => m.Name)
<!-- jquery.validate.unobtrusive.js NOT referenced -->
```

### ✅ What happens

```
The form submits with an empty Name.
The SERVER still rejects it — ModelState.IsValid is false.
```

### The rule

> **"`[Required]` produces `data-val-*` attributes; the *jQuery unobtrusive validation* script turns those into browser-side blocking. Server-side validation runs regardless — client validation is a UX feature, never a security control."**

### 🔥 Follow-up — "Name a validation that only exists on the server."
> `[Remote]` needs a round trip by definition, and any custom `IValidatableObject` / `ValidationAttribute` without a matching client adapter runs **server-only**. Also: *"I never trust client validation, because anyone can POST with curl."*

---

## Q22. Validation doesn't recurse into an unbound child

```csharp
public class Order
{
    [Required] public string Ref { get; set; }
    public Customer Customer { get; set; }        // null when nothing posted for it
}
public class Customer { [Required] public string Name { get; set; } }
```

```
POST  Ref=A1
```

### ✅ What happens

```
ModelState.IsValid == true
```

### The rule

> **"Validation walks the object graph that binding actually produced. `Customer` is null, so there is nothing to validate on it — a `[Required]` inside a null child never fires."**

### 🔥 Follow-up — "Make it fail."
```csharp
[Required] public Customer Customer { get; set; }   // require the child itself
```
> And mention `MvcOptions.MaxValidationDepth` (default 32) — deeply nested or cyclic graphs are cut off, which is another way validation quietly stops.

---

# D — ViewBag, ViewData & TempData

## Q23. ViewBag is `dynamic` — typos are silent

```csharp
public IActionResult Index()
{
    ViewBag.UserName = "Ann";
    return View();
}
```
```razor
<p>Hello @ViewBag.UserNmae</p>       @* typo *@
```

### ✅ What happens

```html
<p>Hello </p>
```
No exception. No warning. No compile error.

### The rule

> **"`ViewBag` is `dynamic` over a dictionary. A missing key returns `null`, and Razor renders `null` as an empty string. Every typo is a silent blank."**

### 🔥 The follow-ups

**Follow-up 1 — "When DOES it throw?"**
```razor
@ViewBag.UserNmae.ToUpper()     @* 💥 RuntimeBinderException: 'object' does not contain a definition for 'ToUpper' *@
@ViewBag.Items.Count            @* 💥 same, if Items was never set *@
```
> Reading is safe; calling a **member on the null** is not — and it's a *runtime binder* exception, which is worth naming precisely.

**Follow-up 2 — "What do you use instead?"**
> **A strongly typed view model.** `@Model.UserName` is compile-checked, refactor-safe, IntelliSense-friendly and testable. *"ViewBag is fine for a page title; anything the view depends on belongs on the model."*

---

## Q24. ViewBag and ViewData are the same storage

```csharp
ViewData["Message"] = "from ViewData";
Console.WriteLine(ViewBag.Message);

ViewBag.Title = "from ViewBag";
Console.WriteLine(ViewData["Title"]);
```

### ✅ What happens

```
from ViewData
from ViewBag
```

### The rule

> **"`ViewBag` is nothing but a `dynamic` wrapper over `ViewData`. One dictionary, two syntaxes — they see each other's values."**

### 🔥 Follow-up — "Then why do both exist, and where does the difference bite?"
```razor
@ViewData["Count"] + 1        @* renders "5 + 1" — Razor ends the expression at ] *@
@(ViewData["Count"] + 1)      @* ❌ COMPILE ERROR: operator '+' cannot be applied to 'object' and 'int' *@
@((int)ViewData["Count"] + 1) @* ✅ 6 *@
@(ViewBag.Count + 1)          @* ✅ 6 — dynamic dispatch resolves it at runtime *@
```
> **`ViewData` is `object` and needs a cast; `ViewBag` is `dynamic` and doesn't.** The trade is compile-time safety for convenience — and `ViewBag` moves the failure from compile time to a customer's browser.

---

## Q25. TempData survives exactly one redirect

```csharp
public IActionResult Save()
{
    TempData["Msg"] = "Saved!";
    return RedirectToAction("Index");
}

public IActionResult Index()
{
    var a = TempData["Msg"];
    var b = TempData["Msg"];
    return Content($"a={a}, b={b}");
}
```

### ✅ What happens

```
a=Saved!, b=Saved!
```
…but on the **next** request, `TempData["Msg"]` is `null`.

### The rule

> **"Reading `TempData` MARKS the entry for deletion at the end of the current request — it doesn't remove it immediately. So repeated reads in the same request all succeed; the value is gone from the request after."**

### 🔥 The follow-ups

**Follow-up 1 — "`Peek` and `Keep`?"**
```csharp
TempData.Peek("Msg");    // read WITHOUT marking for deletion → survives another request
TempData["Msg"];         // read AND mark
TempData.Keep("Msg");    // un-mark after reading → survives another request
TempData.Keep();         // un-mark everything
```

**Follow-up 2 — "Where is it stored?"**
> **Core:** `CookieTempDataProvider` by default (encrypted cookie) — or `SessionStateTempDataProvider` if you register it.
> **⚖️ MVC 5:** always **Session**, so TempData silently breaks if session state is disabled.

**Follow-up 3 — "Store a `List<Product>` in TempData. What happens in Core?"**
```
💥 InvalidOperationException: The 'CookieTempDataProvider' cannot serialize an object of type 'Product'
```
> The cookie provider only serializes simple types (`string`, `int`, `bool`, `DateTime`, `Guid`, and collections of those). Store an **id**, not an object graph — a cookie is also size-limited to ~4KB.

---

## Q26. ViewBag does not survive a redirect

```csharp
public IActionResult Save()
{
    ViewBag.Msg = "Saved!";
    return RedirectToAction("Index");
}

public IActionResult Index() => Content($"Msg = {ViewBag.Msg ?? "null"}");
```

### ✅ What happens

```
Msg = null
```

### The rule

> **"A redirect is a `302` — the browser makes a brand-new HTTP request, which gets a brand-new controller instance and a brand-new `ViewData`. `ViewBag` lives for ONE request; `TempData` is the one designed to cross a redirect."**

### 🔥 Follow-up — "One line comparing all four."

| | Lifetime | Typed? | Survives redirect |
|---|---|---|---|
| **ViewBag** | current request | ❌ `dynamic` | ❌ |
| **ViewData** | current request | ❌ `object` | ❌ |
| **TempData** | until read (≈ next request) | ❌ `object` | ✅ |
| **Session** | until it expires | ❌ `object` | ✅ |
| **View model** | current request | ✅ compile-time | ❌ |

---

## Q27. Setting TempData without ever reading it

```csharp
public IActionResult A() { TempData["X"] = 1; return RedirectToAction("B"); }
public IActionResult B() { return RedirectToAction("C"); }        // doesn't read X
public IActionResult C() => Content($"X = {TempData["X"] ?? "null"}");
```

### ✅ What happens

```
X = 1
```

### The rule

> **"TempData is deleted on READ, not on age. An unread value is automatically carried forward to the next request, and the next, until something reads it."**

Most people answer "null" here because they've memorised "TempData lasts one request". The precise rule is **"until read"**.

---

## Q28. TempData in an AJAX call

```csharp
public IActionResult Save()
{
    TempData["Msg"] = "Saved!";
    return Json(new { ok = true });
}
```
The page then does a client-side `location.reload()`, and the reloaded page shows the message.

### ✅ What happens

```
The message appears — but only because the reload was a real HTTP request.
If the page had updated via JS without reloading, TempData would still be sitting there,
and would surface on some LATER, unrelated navigation.
```

### The rule

> **"TempData is keyed to the browser session, not to a page. Mixing it with AJAX leaks messages onto unrelated pages later."**

### 🔥 Follow-up — "So what do you use for an AJAX flash message?"
> Return the message **in the JSON response** and let the client render it. TempData exists for the **POST-Redirect-GET** pattern specifically — outside PRG it's the wrong tool.

---

## Q29. Session in Core without `UseSession`

```csharp
builder.Services.AddSession();
var app = builder.Build();
app.UseRouting();
app.MapControllers();            // ← UseSession() is missing

// in a controller:
HttpContext.Session.SetString("k", "v");
```

### ✅ What happens

```
💥 InvalidOperationException: Session has not been configured for this application or request.
```

### The rule

> **"In Core, `AddSession()` registers the services and `UseSession()` inserts the middleware. Both are required, and `UseSession()` must come BEFORE the endpoint that uses it."**

### 🔥 Follow-up — "General shape of that rule?"
> It's the whole Core mental model: **`AddX()` in `Services` = registration; `UseX()` in the pipeline = execution, in order.** The same pair applies to authentication, authorization, CORS, response caching and session. Nearly every "why is this null in Core?" question is a missing `UseX()` or one in the wrong place.

---

# E — Action results & redirects

## Q30. Code after `return RedirectToAction`

```csharp
public IActionResult Save()
{
    db.Save();
    return RedirectToAction("Index");
    Log("saved");                          // ← ?
}
```

### ✅ What happens

```
It compiles, with warning CS0162: "Unreachable code detected"
Log() never runs.
```

### The rule

> **"`RedirectToAction` doesn't redirect — it RETURNS an `IActionResult` describing a redirect. The framework executes it after the action returns. So `return` is a normal return, and anything after it is unreachable."**

### ⚖️ In MVC 5 — the trap that actually bites
```csharp
public ActionResult Save()
{
    Response.Redirect("/Home/Index");      // writes the 302 header immediately
    Log("saved");                          // ⚠️ THIS RUNS
    db.DeleteEverything();                 // ⚠️ SO DOES THIS
    return View();
}
```
> **`Response.Redirect` does NOT stop execution.** It sets the header and carries on — the classic "I redirected but the code kept running" bug. `Response.Redirect(url, endResponse: true)` throws a `ThreadAbortException` to stop it, which is itself a well-known anti-pattern.
>
> **The MVC answer: always `return RedirectToAction(...)`, never `Response.Redirect`.**

---

## Q31. Returning `View()` from a POST

```csharp
[HttpPost]
public IActionResult Save(Product p)
{
    if (!ModelState.IsValid) return View(p);
    db.Add(p);
    return View("Index");                  // ← instead of RedirectToAction
}
```

### ✅ What happens

```
The Index view renders, but the URL bar still says  POST /Products/Save
→ pressing F5 re-submits the form and creates a DUPLICATE record
→ the browser shows a "Confirm Form Resubmission" dialog
```

### The rule

> **"`return View()` renders in the SAME request, so the URL doesn't change and the POST stays in browser history. `RedirectToAction` issues a 302 so the browser does a fresh GET."**

### 🔥 Follow-up — "Name the pattern."
> **POST-Redirect-GET (PRG).** POST → validate → save → **redirect** → GET renders. Returning `View()` on the *failure* path is correct (you want the posted values back on screen); returning `View()` on the *success* path is the bug.

---

## Q32. `return Json(data)` on a GET

### ⚖️ This one is a pure MVC 5 question

```csharp
public ActionResult GetUsers()
{
    return Json(db.Users.ToList());        // MVC 5
}
```

### ✅ What happens (MVC 5)

```
💥 InvalidOperationException:
   This request has been blocked because sensitive information could be disclosed
   to third party web sites when this is used in a GET request.
   To allow GET requests, set JsonRequestBehavior to AllowGet.
```

### The fix and the reason
```csharp
return Json(db.Users.ToList(), JsonRequestBehavior.AllowGet);
```

### The rule

> **"MVC 5 blocks JSON on GET by default to mitigate **JSON hijacking** — a cross-site `<script src>` could read a JSON array returned to a GET while the user was authenticated."**

### 🔥 Follow-up — "And in Core?"
> **`JsonRequestBehavior` doesn't exist** — `return Json(data)` just works. The underlying attack was closed by browsers (Array constructor poisoning was fixed) and by SameSite cookies. Knowing *why it was removed* rather than just *that it was removed* is the answer they want.

---

## Q33. `View()` can't find the view

```csharp
public class ProductsController : Controller
{
    public IActionResult Summary() => View();     // Views/Products/Summary.cshtml doesn't exist
}
```

### ✅ What happens

```
💥 InvalidOperationException: The view 'Summary' was not found. The following locations were searched:
   /Views/Products/Summary.cshtml
   /Views/Shared/Summary.cshtml
```

### The rule

> **"`View()` with no name uses the ACTION name, and searches `/Views/{Controller}/` then `/Views/Shared/`. The exception helpfully lists every path it tried."**

### 🔥 Follow-up — "What if the action name was changed with `[ActionName]`?"
```csharp
[ActionName("Report")]
public IActionResult Summary() => View();     // looks for Report.cshtml, not Summary.cshtml
```
> **It searches for the *action* name, not the method name.** A genuinely nasty five-minute debugging session the first time it happens.

---

## Q34. `View()` vs `PartialView()`

```csharp
public IActionResult Row() => View();
public IActionResult RowPartial() => PartialView("Row");
```

### ✅ What happens

```
View()        → renders Row.cshtml INSIDE _Layout (full <html> page)
PartialView() → renders Row.cshtml ALONE (just the fragment)
```

### The rule

> **"`ViewResult` applies `_ViewStart.cshtml`, which is what sets `Layout`. `PartialViewResult` skips `_ViewStart` entirely, so no layout."**

### 🔥 Follow-up — "So an AJAX call returning `View()` gives you…?"
> A full HTML page — `<html>`, `<head>`, navbar and all — injected into your `<div>`. It's the standard "why does my modal contain the whole site?" bug. Use `PartialView()`, or set `Layout = null;` in the view.

---

## Q35. Returning a `Task` from an action without `await`

```csharp
public IActionResult Save()
{
    db.SaveChangesAsync();                 // ← not awaited
    return RedirectToAction("Index");
}
```

### ✅ What happens

```
The redirect is issued immediately.
The save MAY complete… or may be aborted when the request ends and the DbContext is disposed.
Often: 💥 ObjectDisposedException on a background thread, and the record is silently missing.
```

### The rule

> **"An un-awaited `Task` is fire-and-forget. The request completes, the DI scope is disposed, and the scoped `DbContext` disappears out from under the running task."**

### 🔥 Follow-up — "Fix, and the compiler hint you ignored."
```csharp
public async Task<IActionResult> Save()
{
    await db.SaveChangesAsync();
    return RedirectToAction("Index");
}
```
> The compiler emits **CS4014** — *"Because this call is not awaited, execution of the current method continues before the call is completed"*. Treat warnings as errors and this class of bug disappears. For genuine background work, use `IHostedService` / a queue, **not** a fire-and-forget task in a controller.

---

# F — Filters & execution order

## Q36. Global, controller and action filters together

```csharp
builder.Services.AddControllers(o => o.Filters.Add(new LogFilter("GLOBAL")));

[LogFilter("CONTROLLER")]
public class HomeController : Controller
{
    [LogFilter("ACTION")]
    public IActionResult Index() { Console.WriteLine("ACTION METHOD"); return View(); }
}

public class LogFilter : Attribute, IActionFilter
{
    private readonly string _n;
    public LogFilter(string n) => _n = n;
    public void OnActionExecuting(ActionExecutingContext c) => Console.WriteLine($"{_n} executing");
    public void OnActionExecuted(ActionExecutedContext c)   => Console.WriteLine($"{_n} executed");
}
```

### ✅ Output

```
GLOBAL executing
CONTROLLER executing
ACTION executing
ACTION METHOD
ACTION executed
CONTROLLER executed
GLOBAL executed
```

### The rule

> **"Filters nest like Russian dolls. On the way IN it's Global → Controller → Action. On the way OUT it's the exact reverse: Action → Controller → Global."**

### 🔥 Follow-up — "How do you override that order?"
```csharp
[LogFilter("ACTION", Order = -1)]      // lower Order runs FIRST, before Global
```
> **`Order` beats scope.** Filters are sorted by `Order` first (default 0), and scope only breaks ties. Mentioning that the default is 0 for all three scopes — which is *why* scope decides normally — is the complete answer.

---

## Q37. The full Core filter pipeline

```
Which of these runs FIRST — and where does model binding fit?
Authorization filter · Resource filter · Action filter · Exception filter · Result filter
```

### ✅ The order

```
1. Authorization filters      ← reject unauthenticated requests as early as possible
2. Resource filters (before)  ← caching lives here; runs BEFORE model binding
3. ── MODEL BINDING ──
4. Action filters (before)
5. ═══ THE ACTION METHOD ═══
6. Action filters (after)
7. Result filters (before)
8. ── RESULT EXECUTION (the view renders here) ──
9. Result filters (after)
10. Resource filters (after)

   Exception filters wrap steps 3–6.
```

### The rule

> **"Authorization is first because there's no point binding a model for a request you're going to reject. Resource filters bracket model binding, which is why output caching is a resource filter."**

### 🔥 Follow-up — "Where would you put each of these?"
| Need | Filter type |
|---|---|
| Check a permission | **Authorization** |
| Short-circuit with a cached response | **Resource** |
| Log/audit arguments, start a stopwatch | **Action** |
| Turn an exception into a friendly view | **Exception** |
| Add a header to every response | **Result** |
| Anything that must run for non-MVC requests too (static files) | **Middleware**, not a filter |

That last row is the one that separates people who've read the docs from people who've shipped.

---

## Q38. Short-circuiting a filter

```csharp
public class CacheFilter : Attribute, IActionFilter
{
    public void OnActionExecuting(ActionExecutingContext c)
    {
        c.Result = new ContentResult { Content = "from cache" };   // ← set Result
        Console.WriteLine("executing");
    }
    public void OnActionExecuted(ActionExecutedContext c) => Console.WriteLine("executed");
}

[CacheFilter]
public IActionResult Index() { Console.WriteLine("ACTION"); return View(); }
```

### ✅ Output

```
executing
```
Response body: `from cache`. **`ACTION` never prints — and neither does `executed`.**

### The rule

> **"Setting `context.Result` in `OnActionExecuting` short-circuits the pipeline: the action is skipped, and so is the `OnActionExecuted` half of THIS filter. Result filters and the result itself still run."**

The missing `executed` is the part people get wrong — they assume the "after" half always pairs with the "before" half. It doesn't.

### 🔥 Follow-up — "And in an async filter?"
```csharp
public async Task OnActionExecutionAsync(ActionExecutingContext c, ActionExecutionDelegate next)
{
    if (cached) { c.Result = new ContentResult { Content = "hit" }; return; }  // don't call next()
    var executed = await next();                                              // action runs here
}
```
> **Not calling `next()` is how you short-circuit an async filter** — same idea as middleware. And you must never call `next()` twice.

---

## Q39. What exception filters do **not** catch

```csharp
public class MyExceptionFilter : IExceptionFilter
{
    public void OnException(ExceptionContext c)
    {
        Console.WriteLine("caught: " + c.Exception.Message);
        c.Result = new ContentResult { Content = "handled" };
        c.ExceptionHandled = true;
    }
}
```
The exception is thrown **inside the Razor view** (`@Model.Customer.Name` where `Customer` is null).

### ✅ What happens

```
The filter does NOT run. The user gets a 500 / the developer exception page.
```

### The rule

> **"Exception filters cover controller creation, model binding, action filters and the action method. They do NOT cover result execution — so an exception while the VIEW renders escapes them entirely."**

### 🔥 Follow-up — "So what does catch it?"
> **Exception-handling middleware** — `app.UseExceptionHandler("/Error")` — because middleware wraps the whole pipeline including result execution. The rule of thumb: *"filters for MVC-shaped errors, middleware for everything."*

---

## Q40. `[Authorize]` on the controller, `[AllowAnonymous]` on the action

```csharp
[Authorize]
public class AccountController : Controller
{
    [AllowAnonymous] public IActionResult Login()  => View();
                      public IActionResult Profile() => View();
}
```

### ✅ What happens

```
/Account/Login    →  200  (anonymous allowed)
/Account/Profile  →  302 to the login page (or 401/403 for an API)
```

### The rule

> **"`[AllowAnonymous]` always wins — it short-circuits authorization regardless of scope, including a globally registered `AuthorizeFilter`."**

### 🔥 Follow-up — "Why does that make `[AllowAnonymous]` dangerous?"
> Because it overrides even a global authorization policy. One `[AllowAnonymous]` left on a controller during debugging opens the whole controller, and no global setting will save you. It's a standard code-review grep.

---

## Q41. `[ValidateAntiForgeryToken]` without the token

```csharp
[HttpPost]
[ValidateAntiForgeryToken]
public IActionResult Save(Product p) => Ok();
```
The form is hand-written HTML with no `@Html.AntiForgeryToken()`.

### ✅ What happens

```
400 Bad Request
(AntiforgeryValidationException: The required antiforgery cookie "..." is not present)
```

### The rule

> **"The token is two halves — a cookie AND a form field — that must match. It stops CSRF: an attacker's site can make your browser send the cookie, but can't read or forge the form field."**

### 🔥 Follow-up — "How do you send it from AJAX?"
```csharp
services.AddAntiforgery(o => o.HeaderName = "X-CSRF-TOKEN");
```
```js
fetch(url, { method: 'POST', headers: { 'X-CSRF-TOKEN': token } })
```
> And in Core, `<form asp-action="Save">` **injects the hidden token automatically** — which is why hand-written `<form>` tags are the usual cause of this 400.

---

## Q42. `OnActionExecuted` when the action throws

```csharp
public void OnActionExecuted(ActionExecutedContext c)
    => Console.WriteLine($"executed, exception = {c.Exception?.Message ?? "none"}");

public IActionResult Index() => throw new Exception("boom");
```

### ✅ Output

```
executed, exception = boom
```
…and then the exception continues to propagate.

### The rule

> **"`OnActionExecuted` still runs when the action throws — the exception is handed to you on `context.Exception` rather than being thrown past you."**

### 🔥 Follow-up — "How do you swallow it there?"
```csharp
c.ExceptionHandled = true;
c.Result = new ContentResult { Content = "recovered" };
```
> Setting `ExceptionHandled = true` **without** setting `Result` gives the client an empty 200 — a silent failure that's arguably worse than the 500. Always set both.

---

# G — Razor view traps

## Q43. 🔥 The model changed, but the textbox didn't

```csharp
[HttpPost]
public IActionResult Edit(Product p)
{
    p.Name = "CHANGED";
    return View(p);
}
```
```razor
@model Product
@Html.TextBoxFor(m => m.Name)
<p>Model value: @Model.Name</p>
```
The user posted `Name=original`.

### ✅ What renders

```html
<input type="text" name="Name" value="original" />     ← ⚠️ NOT "CHANGED"
<p>Model value: CHANGED</p>
```

### The rule (this is the most-asked MVC view question there is)

> **"HTML helpers read their value from ModelState FIRST, then ViewData, and only then from the model. The posted value is sitting in ModelState, so `TextBoxFor` shows the POSTED value and ignores your change."**

Lookup order:
```
1. ModelState   ← the posted value lives here
2. ViewData / ViewBag
3. The model    ← only reached if the first two have nothing
```

**Why the framework does this:** when validation fails you *want* the user's original input redisplayed next to the error message — not the sanitised version your code produced. It's correct behaviour that looks like a bug.

### 🔥 The follow-ups

**Follow-up 1 — "Force the new value to show."**
```csharp
ModelState.Remove(nameof(Product.Name));   // ✅ surgical — clears just that key
// or
ModelState.Clear();                        // ⚠️ blunt — also destroys all validation errors
```

**Follow-up 2 — "Why does `@Model.Name` show the new value?"**
> Because raw Razor output reads the model **directly** — it doesn't go through the helper's ModelState lookup at all. The two lines disagreeing on the same page is the tell.

**Follow-up 3 — "When have you hit this?"**
> Say it honestly: server-side normalisation (trimming, uppercasing a code, formatting a phone number) that "didn't take" on redisplay. It's the classic real-world encounter, and naming it makes the answer sound lived rather than memorised.

---

## Q44. Razor's `@` parsing

```razor
@{ int count = 5; }

<p>@count + 1</p>
<p>@(count + 1)</p>
<p>Email: user@domain.com</p>
<p>Cost: @@50</p>
```

### ✅ What renders

```html
<p>5 + 1</p>
<p>6</p>
<p>Email: user@domain.com</p>
<p>Cost: @50</p>
```

### The rule

> **"Razor ends an implicit expression at the first character that can't continue it — so `@count` stops before the space. Parentheses make it explicit. `@@` escapes a literal `@`, and Razor is smart enough to leave email addresses alone."**

### 🔥 Follow-up — "What about a method call with arguments?"
```razor
@Model.Items.Count           @* ✅ works — dots and property access continue the expression *@
@Model.GetName("a", "b")     @* ✅ works — parens are part of an implicit expression *@
@Model.Price.ToString("C")   @* ✅ works *@
@if (x) { <p>@x</p> }        @* ✅ code block *@
@:Plain text with @count     @* ✅ @: forces the rest of the line to be literal output *@
```

---

## Q45. HTML encoding and `Html.Raw`

```csharp
var model = new Product { Name = "<script>alert('xss')</script>" };
```
```razor
<p>@Model.Name</p>
<p>@Html.Raw(Model.Name)</p>
<p>@Html.DisplayFor(m => m.Name)</p>
```

### ✅ What renders

```html
<p>&lt;script&gt;alert(&#x27;xss&#x27;)&lt;/script&gt;</p>   ← safe, visible as text
<p><script>alert('xss')</script></p>                        ← 🔓 EXECUTES
<p>&lt;script&gt;...&lt;/script&gt;</p>                      ← safe
```

### The rule

> **"Razor HTML-encodes every `@expression` by default. `Html.Raw` is the ONLY way to opt out — and it is the standard XSS vector in .NET applications."**

### 🔥 Follow-up — "When is `Html.Raw` legitimate?"
> Only for HTML **you generated**, or user HTML **after sanitising it** with a library like `HtmlSanitizer`. Never for raw user input. Add: *"`Html.Raw` in a pull request is an automatic review comment for me."*

---

## Q46. `Html.Partial` vs `Html.RenderPartial`

```razor
@Html.Partial("_Row")
@Html.RenderPartial("_Row")
@{ Html.RenderPartial("_Row"); }
```

### ✅ What happens

```
Line 1 → ✅ renders
Line 2 → ❌ COMPILE ERROR — cannot implicitly convert 'void' to 'object'
Line 3 → ✅ renders (and is faster)
```

### The rule

> **"`Partial` RETURNS an `IHtmlString`, so it works in an `@` expression. `RenderPartial` WRITES straight to the output stream and returns `void`, so it must be called from a code block."**

`RenderPartial` avoids building an intermediate string — marginally faster, meaningfully so inside a big loop.

### 🔥 Follow-up — "And in modern Core?"
```razor
<partial name="_Row" model="item" />           @* tag helper — preferred *@
@await Html.PartialAsync("_Row")               @* async, avoids sync-over-async *@
@await Component.InvokeAsync("Cart")           @* view component — has its own logic + DI *@
```
> `Html.Partial` is **flagged by an analyzer in Core** because it blocks on async rendering. The modern answer is the `<partial>` tag helper, and a **view component** when the fragment needs its own data-fetching logic.

---

## Q47. `@section` inside a partial view

```razor
@* _Row.cshtml (a partial) *@
@section Scripts { <script>console.log('row');</script> }
```

### ✅ What happens

```
💥 InvalidOperationException: The layout page cannot be rendered after
   'RenderBody' has been called. / the section is simply never rendered.
```

### The rule

> **"Sections belong to the view/layout relationship. A partial has no layout, so `@section` inside one has nothing to render into — it's ignored or throws."**

### 🔥 Follow-up — "So how does a partial add a script?"
> Three real options: put the script in the **parent view's** `@section Scripts`; use a **view component** with its own script include; or write to a shared collection (`ViewData` / a tag helper like `<script asp-append-version>`) that the layout renders. Say *"I'd avoid inline scripts in partials"* — it's the answer that shows you've maintained a codebase.

---

## Q48. Layout and view execution order

```razor
@* _Layout.cshtml *@
<title>@ViewBag.Title</title>
@{ ViewBag.FromLayout = "layout"; }
@RenderBody()

@* Index.cshtml *@
@{ ViewBag.Title = "Home"; }
<p>@ViewBag.FromLayout</p>
```

### ✅ What renders

```html
<title>Home</title>            ← the view's value IS visible in the layout
<p></p>                        ← the layout's value is NOT visible in the view
```

### The rule

> **"The VIEW executes first, completely. Its output is then handed to the layout, which executes and calls `RenderBody()` to splice it in. So the view can set values for the layout, but never the other way round."**

This surprises everyone, because the layout *looks* like the outer thing.

### 🔥 Follow-up — "Where does `_ViewStart.cshtml` fit?"
```
_ViewStart.cshtml  →  the View  →  _Layout.cshtml
```
> `_ViewStart` runs **before** the view (that's where `Layout = "_Layout";` is set), and it cascades down folders — a `_ViewStart` in `/Views/Admin/` overrides the one in `/Views/`. `_ViewImports.cshtml` is the sibling that handles `@using`, `@inject` and `@addTagHelper`.

---

## Q49. `RenderSection` required by default

```razor
@* _Layout.cshtml *@
@RenderSection("Scripts")

@* Index.cshtml — has no @section Scripts *@
```

### ✅ What happens

```
💥 InvalidOperationException: The layout page '_Layout.cshtml' cannot find
   the section 'Scripts' in the content page.
```

### The rule

> **"`RenderSection(name)` is REQUIRED by default. Every view using this layout must define the section — one that doesn't, breaks."**

```razor
@RenderSection("Scripts", required: false)          @* ✅ optional *@
@await RenderSectionAsync("Scripts", required: false)   @* ✅ Core, async *@
```

---

## Q50. `DisplayNameFor` on an empty collection

```razor
@model IEnumerable<Product>

<th>@Html.DisplayNameFor(model => model.First().Name)</th>

@foreach (var p in Model) { <tr><td>@p.Name</td></tr> }
```
`Model` is an **empty** list.

### ✅ What happens

```html
<th>Name</th>          ← ✅ renders fine, no exception
```
…even though `Model.First()` on an empty sequence would normally throw `InvalidOperationException`.

### The rule

> **"`DisplayNameFor` never EXECUTES the lambda — it only parses the expression TREE to find the property and read its metadata. So `First()` is never called."**

This is the line the MVC scaffolder generates for every list view, and almost nobody can explain why it doesn't crash.

### 🔥 The follow-ups

**Follow-up 1 — "Where does the name come from?"**
```csharp
[Display(Name = "Product name")] public string Name { get; set; }
```
> From `[Display]`, falling back to the property name. The whole point of the metadata system.

**Follow-up 2 — "So can I use `First()` in `DisplayFor` too?"**
> **No.** `DisplayFor` / `TextBoxFor` **compile and invoke** the expression to get the *value* — so `Model.First()` on an empty list throws there. `DisplayNameFor` reads metadata; everything else reads data. That distinction is the whole question.

---

## Q51. `@Html.ActionLink` with a null route value

```razor
@Html.ActionLink("Edit", "Edit", new { id = Model.Id })
@Html.ActionLink("Edit", "Edit", new { id = (int?)null })
@Html.ActionLink("Edit", "Edit", "Products", new { id = 5 }, null)
```

### ✅ What renders

```html
<a href="/Products/Edit/5">Edit</a>
<a href="/Products/Edit">Edit</a>                 ← null values are simply dropped
<a href="/Products/Edit/5">Edit</a>
```

### The rule

> **"Null route values are omitted from the generated URL. And watch the overloads — the 4-argument form is `(text, action, routeValues, htmlAttributes)`, so passing a controller name there makes it a query string parameter instead."**

```razor
@Html.ActionLink("Edit", "Edit", "Products")
@* ⚠️ 3-arg overload is (text, action, routeValues) → /Products/Edit?Length=8  *@
```

> That `?Length=8` is legendary: `"Products"` was treated as an **object of route values**, and `string` has a `Length` property. If you ever see `?Length=` in a URL, this is why.

### 🔥 Follow-up — "What do you use in Core?"
```razor
<a asp-controller="Products" asp-action="Edit" asp-route-id="5">Edit</a>
```
> **Tag helpers** — no overload ambiguity, real IntelliSense, and the markup stays HTML.

---

# H — Controller lifecycle & state

## Q52. Instance fields don't survive

```csharp
public class CounterController : Controller
{
    private int _count = 0;

    public IActionResult Index()
    {
        _count++;
        return Content($"count = {_count}");
    }
}
```
Called three times.

### ✅ What happens

```
count = 1
count = 1
count = 1
```

### The rule

> **"A controller is instantiated PER REQUEST and disposed at the end of it. Instance state never survives a request."**

### 🔥 Follow-up — "Make it count. Then tell me what breaks."
```csharp
private static int _count = 0;      // now: 1, 2, 3
```
> …and it's **shared across every user and every thread**, so `_count++` is a data race (see Q53). The correct answers are Session (per user), a distributed cache (per app), or the database (durable).

---

## Q53. Static state is shared across all requests

```csharp
public class CartController : Controller
{
    private static List<string> _items = new();

    public IActionResult Add(string item)
    {
        _items.Add(item);
        return Content(string.Join(",", _items));
    }
}
```

### ✅ What happens

```
Every user sees every other user's cart.
Under concurrent load: 💥 IndexOutOfRangeException / corrupted state / lost items
```

### The rule

> **"Static fields live for the lifetime of the process and are shared by all concurrent requests. `List<T>` is not thread-safe, so concurrent `Add` calls can corrupt its internal array."**

### 🔥 Follow-up — "If you genuinely need process-wide shared state?"
> `IMemoryCache` (built for it, with expiry and size limits), `ConcurrentDictionary` (thread-safe), or a **singleton service with proper locking**. And the scaling caveat interviewers wait for: *"none of that survives a restart or works across multiple instances behind a load balancer — that's what a distributed cache like Redis is for."*

---

## Q54. `async void` action

```csharp
public async void Save()                  // ← void, not Task
{
    await db.SaveChangesAsync();
}
```

### ✅ What happens

```
The response completes immediately with 200 and an empty body,
before the save has finished. Any exception CRASHES THE PROCESS —
it cannot be caught by exception middleware or a filter.
```

⚖️ **In MVC 5:**
```
💥 InvalidOperationException: An asynchronous module or handler completed
   while an asynchronous operation was still pending.
```

### The rule

> **"An `async void` method returns nothing the framework can await, so MVC treats the action as finished the moment it hits the first `await`. Exceptions have no `Task` to land in and are raised on the thread pool — which terminates the process."**

Exactly the same rule as **Q67 in [file 27]**, now with an HTTP-shaped consequence.

### 🔥 Follow-up — "The one place `async void` is acceptable?"
> **Event handlers only**, where the framework fixes the signature. An MVC action is never one. Always `public async Task<IActionResult>`.

---

## Q55. `HttpContext` after `ConfigureAwait(false)`

### ⚖️ Classic MVC 5

```csharp
public async Task<ActionResult> Index()
{
    await SomeApiCallAsync().ConfigureAwait(false);
    var user = HttpContext.Current.User.Identity.Name;      // ← ?
    return View();
}
```

### ✅ What happens

```
💥 NullReferenceException — HttpContext.Current is null
```

### The rule

> **"`HttpContext.Current` is stored on the ASP.NET `SynchronizationContext`. `ConfigureAwait(false)` tells the continuation not to resume on that context — so it resumes on a bare thread-pool thread where `HttpContext.Current` is null."**

### 🔥 Follow-up — "And in Core?"
> **`HttpContext.Current` doesn't exist.** Core removed the ambient static entirely. You inject **`IHttpContextAccessor`** where you need it, and inside a controller you just use the `HttpContext` property — which is a field on the controller, not ambient state, so `ConfigureAwait(false)` can't break it.
>
> The deeper point worth saying: *"Core removed the SynchronizationContext, which is also why the classic `.Result` deadlock doesn't happen there."* That ties this question to Q66 in file 27 and shows you understand one mechanism, not two facts.

---

# I — DI & the middleware pipeline (Core)

## Q56. Scoped inside Singleton — the captive dependency

```csharp
builder.Services.AddScoped<IRepository, Repository>();
builder.Services.AddSingleton<ICacheService, CacheService>();

public class CacheService : ICacheService
{
    public CacheService(IRepository repo) { }      // singleton depending on scoped
}
```

### ✅ What happens

```
💥 InvalidOperationException at startup:
   Cannot consume scoped service 'IRepository' from singleton 'ICacheService'.
```

### The rule

> **"A singleton outlives every scope, so capturing a scoped service would keep the FIRST request's instance alive forever — a *captive dependency*. Core's scope validation catches it at startup in Development."**

### 🔥 The follow-ups

**Follow-up 1 — "Why only in Development?"**
> `ValidateScopes` and `ValidateOnBuild` default to **true only in the Development environment**. In Production the app starts happily and you get a stale `DbContext` shared across all users — a genuinely dangerous silent failure. Turn it on explicitly:
> ```csharp
> builder.Host.UseDefaultServiceProvider(o => { o.ValidateScopes = true; o.ValidateOnBuild = true; });
> ```

**Follow-up 2 — "Fix it."**
```csharp
public class CacheService : ICacheService
{
    private readonly IServiceScopeFactory _factory;
    public CacheService(IServiceScopeFactory factory) => _factory = factory;

    public void Refresh()
    {
        using var scope = _factory.CreateScope();
        var repo = scope.ServiceProvider.GetRequiredService<IRepository>();
    }
}
```
> **Inject `IServiceScopeFactory` and create a scope per unit of work.** This is also exactly how you use a `DbContext` inside a background `IHostedService`.

---

## Q57. Transient vs Scoped vs Singleton in one request

```csharp
builder.Services.AddTransient<ITransient, Svc>();
builder.Services.AddScoped<IScoped, Svc>();
builder.Services.AddSingleton<ISingleton, Svc>();

public HomeController(ITransient t1, ITransient t2,
                      IScoped s1,    IScoped s2,
                      ISingleton g1, ISingleton g2) { }
```

### ✅ What happens

```
t1 != t2     ← two different instances, in the SAME request
s1 == s2     ← one instance per request
g1 == g2     ← one instance for the whole application
```
And across two requests: transient differs, **scoped differs**, singleton is the same.

### The rule

> **"Transient = a new instance every time it's INJECTED. Scoped = one per HTTP request. Singleton = one per application lifetime."**

### 🔥 Follow-up — "Which one for `DbContext`, and why?"
> **Scoped** — and `AddDbContext` registers it as scoped by default. It gives you one unit of work and one change-tracker per request, which is exactly the EF Core model. **Singleton would be a disaster**: `DbContext` isn't thread-safe and its change tracker would grow forever. **Transient** would break transactions across repositories, because each repository would get its own context.

---

## Q58. Last registration wins

```csharp
builder.Services.AddScoped<IGreeter, EnglishGreeter>();
builder.Services.AddScoped<IGreeter, FrenchGreeter>();

public HomeController(IGreeter g, IEnumerable<IGreeter> all)
    => Console.WriteLine($"{g.GetType().Name}, count = {all.Count()}");
```

### ✅ Output

```
FrenchGreeter, count = 2
```

### The rule

> **"Registering the same interface twice does NOT replace — both stay in the container. Resolving a single instance gives you the LAST one registered; resolving `IEnumerable<T>` gives you ALL of them, in registration order."**

### 🔥 Follow-up — "How do you actually replace one?"
```csharp
builder.Services.Replace(ServiceDescriptor.Scoped<IGreeter, FrenchGreeter>());
builder.Services.TryAddScoped<IGreeter, EnglishGreeter>();   // only if none registered
```
> `TryAdd*` is what **libraries** use, so your app's registration wins over the library's default. Knowing `TryAdd` exists and why is a strong signal.

---

## Q59. Middleware order

```csharp
app.Use(async (ctx, next) => { Console.WriteLine("A in");  await next(); Console.WriteLine("A out"); });
app.Use(async (ctx, next) => { Console.WriteLine("B in");  await next(); Console.WriteLine("B out"); });
app.Run(async ctx => { Console.WriteLine("terminal"); await ctx.Response.WriteAsync("done"); });
```

### ✅ Output

```
A in
B in
terminal
B out
A out
```

### The rule

> **"Middleware is a nested pipeline, not a list. `await next()` is the seam — everything before it runs on the way in, everything after it runs on the way OUT, in reverse order."**

Same doll-nesting shape as filters (Q36) — say that out loud, it's the same idea at a different layer.

### 🔥 Follow-up — "What if B doesn't call `next()`?"
```
A in
B in
A out
```
> **B short-circuits.** `terminal` never runs. That's exactly how `UseStaticFiles` works: it serves the file and doesn't call `next()`, so MVC never sees the request — which is *why* it belongs early in the pipeline.

---

## Q60. Writing to the response after `next()`

```csharp
app.Use(async (ctx, next) =>
{
    await next();
    ctx.Response.Headers["X-Custom"] = "value";       // ← ?
});
```

### ✅ What happens

```
💥 InvalidOperationException: Headers are read-only, response has already started.
```

### The rule

> **"Once the response body has started being written, headers are already on the wire and cannot be changed. Anything downstream that wrote a body has closed that window."**

### 🔥 Follow-up — "So how do you add a header from middleware?"
```csharp
app.Use(async (ctx, next) =>
{
    ctx.Response.OnStarting(() =>
    {
        ctx.Response.Headers["X-Custom"] = "value";
        return Task.CompletedTask;
    });
    await next();
});
```
> **Register a callback with `OnStarting`**, which fires just before the first byte goes out — or set the header *before* calling `next()`. Also check `ctx.Response.HasStarted` before touching the response in any error handler.

---

## Q61. `UseRouting` / `UseAuthorization` in the wrong order

```csharp
app.UseAuthorization();
app.UseRouting();
app.MapControllers();
```

### ✅ What happens

```
💥 InvalidOperationException:
   Endpoint routing does not support 'IApplicationBuilder.UseAuthorization'
   being called before 'UseRouting'.
   …or [Authorize] is silently NOT enforced.
```

### The rule

> **"`UseRouting` is what SELECTS the endpoint. `UseAuthorization` needs to know which endpoint was selected in order to read its `[Authorize]` metadata — so it must come after `UseRouting` and before `UseEndpoints`/`MapControllers`."**

### The order to memorize

```csharp
app.UseExceptionHandler("/Error");   // outermost — must wrap everything
app.UseHsts();
app.UseHttpsRedirection();
app.UseStaticFiles();                // before routing — short-circuits for files
app.UseRouting();                    // ← selects the endpoint
app.UseCors();
app.UseAuthentication();             // who are you?
app.UseAuthorization();              // are you allowed?  ← must follow UseRouting
app.MapControllers();                // ← executes the endpoint
```

### 🔥 Follow-up — "Why is `UseStaticFiles` before `UseRouting`?"
> Performance — a static file short-circuits before any routing work happens. The trade-off worth naming: **static files served before `UseAuthorization` are not protected by `[Authorize]`**. Anything in `wwwroot` is public. If files need protection, serve them through a controller action.

---

## Q62. `IOptions` vs `IOptionsSnapshot`

```csharp
builder.Services.Configure<MySettings>(builder.Configuration.GetSection("MySettings"));

public HomeController(IOptions<MySettings> a, IOptionsSnapshot<MySettings> b) { }
```
`appsettings.json` is edited while the app is running.

### ✅ What happens

```
a.Value  →  the OLD value (read once at startup, cached forever)
b.Value  →  the NEW value (re-read per request)
```

### The rule

> **"`IOptions<T>` is a SINGLETON computed once. `IOptionsSnapshot<T>` is SCOPED and recomputed per request, so it picks up `reloadOnChange` config edits. `IOptionsMonitor<T>` is a singleton that also gets change notifications — the only one usable inside another singleton."**

### 🔥 Follow-up — "Which do you inject into a singleton service?"
> **`IOptionsMonitor<T>`** — injecting `IOptionsSnapshot<T>` (scoped) into a singleton is exactly the captive-dependency error from Q56.

---

# J — Rapid-fire answer table

> Cover the right column. Target: **under 5 minutes**, daily.

| # | Scenario | Answer | Why (one line) |
|---|---|---|---|
| 1 | Two same-name, same-verb actions | `AmbiguousMatchException` | Routing matched; action selection couldn't |
| 2 | `[Route]` on one action | conventional routes stop reaching it | Attribute routing opts that action out |
| 3 | `[HttpGet]` + `[HttpPost]` same name | ✅ fine | Verb disambiguates |
| 4 | `{id?}` route + `int id` param | Core: `0` · **MVC 5: exception** | Non-nullable can't be optional |
| 5 | `{id:int}` with `/abc` | **404** | Constraints are matching, not validation |
| 6 | Route default vs C# default | route default wins | Binding runs before the C# default applies |
| 7 | `{*url}` registered first | breaks the whole site | Catch-all shadows everything below |
| 8 | Public helper on a controller | it's a routable endpoint | All public methods are actions |
| 9 | Same key in form + route + query | **form** wins | Form → Route → Query |
| 10 | Two `[FromBody]` params | exception | Body is read once |
| 11 | JSON posted to an MVC controller | model is empty | Complex types bind from form unless `[FromBody]`/`[ApiController]` |
| 12 | `Name=Ann` for a nested property | `null` | Needs `Customer.Name` |
| 13 | `items[0]` and `items[2]` | count = **1** | Indices must be consecutive |
| 14 | `quantity=abc` for `int quantity` | `0`, action **still runs** | Binding failure ≠ short-circuit |
| 15 | Extra `IsAdmin=true` posted | it binds 🔓 | Over-posting — bind to a DTO |
| 16 | Unchecked checkbox | field absent → `false` | Hidden companion input supplies false |
| 17 | `[FromServices]` | resolved from DI | Not from the request |
| 18 | `[Required] int Quantity` = 0 | **valid** | Required only checks null |
| 19 | `AddModelError` after `IsValid` | too late, code continues | `IsValid` isn't a latch |
| 20 | `[ApiController]` + invalid model | **400 before the action** | Auto model-validation filter |
| 21 | No unobtrusive JS | server still rejects | Client validation is UX only |
| 22 | `[Required]` inside a null child | valid | Nothing to walk |
| 23 | `@ViewBag.Typo` | blank, no error | `dynamic` returns null |
| 24 | `ViewData["X"]` then `ViewBag.X` | same value | One dictionary |
| 25 | Read `TempData` twice in one request | works both times | Deleted at end of request, not on read |
| 26 | `ViewBag` across a redirect | `null` | New request, new ViewData |
| 27 | `TempData` never read | survives to the next request | Deleted on read, not by age |
| 28 | `TempData` + AJAX | leaks onto a later page | TempData is per-session, not per-page |
| 29 | `AddSession` without `UseSession` | exception | `AddX` registers, `UseX` executes |
| 30 | Code after `return RedirectToAction` | unreachable | It just returns a result object |
| 31 | `Response.Redirect` (MVC 5) | **code keeps running** | Doesn't stop execution |
| 32 | `return View()` from POST | F5 re-submits | Use POST-Redirect-GET |
| 33 | `return Json(x)` on GET (MVC 5) | exception | JSON hijacking guard; removed in Core |
| 34 | `View()` with a missing file | exception listing search paths | `/Views/{Ctrl}/` then `/Views/Shared/` |
| 35 | `PartialView()` | no layout | Skips `_ViewStart` |
| 36 | Un-awaited `SaveChangesAsync` | may never complete | Scope disposed under it |
| 37 | Global + controller + action filters | in: G→C→A · out: A→C→G | Nested, reversed on the way out |
| 38 | Filter sets `context.Result` | action skipped, `OnActionExecuted` skipped too | Short-circuit |
| 39 | Exception thrown in the **view** | exception filter misses it | Filters don't cover result execution |
| 40 | `[Authorize]` + `[AllowAnonymous]` | anonymous **wins** | Overrides every scope |
| 41 | `[ValidateAntiForgeryToken]` no token | **400** | Cookie + form field must match |
| 42 | Action throws | `OnActionExecuted` still runs | Exception arrives on `context.Exception` |
| 43 | 🔥 `p.Name = "X"; return View(p);` | textbox shows the **posted** value | Helpers read ModelState first |
| 44 | `@count + 1` | `5 + 1` | Implicit expression ends at the space |
| 45 | `@Html.Raw(userInput)` | 🔓 XSS | Only way to bypass encoding |
| 46 | `@Html.RenderPartial("x")` | compile error | Returns `void`; needs a code block |
| 47 | `@section` in a partial | ignored / throws | Sections belong to view↔layout |
| 48 | ViewBag set in the view | **visible** in the layout | View executes before the layout |
| 49 | `@RenderSection("Scripts")` | required by default | Pass `required: false` |
| 50 | Lambda capturing `for`'s `i` | all get the last value | One shared variable |
| 51 | `ActionLink("t","a","Products")` | `?Length=8` | 3-arg overload treats it as route values |
| 52 | Instance field on a controller | resets every request | New controller per request |
| 53 | `static List<T>` on a controller | shared + not thread-safe | Process-wide state |
| 54 | `async void` action | empty 200, crash on throw | Nothing to await |
| 55 | `ConfigureAwait(false)` + `HttpContext.Current` | `null` (MVC 5) | Left the SynchronizationContext |
| 56 | Scoped injected into Singleton | startup exception (Dev) | Captive dependency |
| 57 | Two `ITransient` in one ctor | different instances | Transient = per injection |
| 58 | Same interface registered twice | last wins; `IEnumerable` gets both | Registrations append |
| 59 | Two middlewares + terminal | A in, B in, term, B out, A out | Nested pipeline |
| 60 | Set a header after `next()` | exception | Response already started |
| 61 | `UseAuthorization` before `UseRouting` | exception / not enforced | Needs the selected endpoint |
| 62 | `IOptions` vs `IOptionsSnapshot` | stale vs fresh | Singleton vs scoped |

---

# K — 18 self-test drills (answers hidden)

> Cover the answer, commit out loud, then check. **Five a day.**

### Drill 1
```csharp
public IActionResult Index(string name = "guest") => Content(name ?? "null");
```
`GET /Home/Index?name=`
<details><summary>👉 Answer</summary>

**empty string**, not `"guest"`. The key *is* present with an empty value, so binding succeeds with `""` and the C# default never applies. To treat empty as missing you need `string.IsNullOrWhiteSpace(name)` explicitly.
</details>

### Drill 2
```csharp
[HttpPost] public IActionResult Save(Product p) => View(p);
```
Posted: `Name=Ann`. The view has `@Html.HiddenFor(m => m.Id)` and `Id` was never posted.
<details><summary>👉 Answer</summary>

Renders `value="0"` — `Id` is a non-nullable `int`, binding produced nothing, so it's `0`. On the next POST the record updates **entity 0**, i.e. creates a new one. This is why edit forms must round-trip the id in a hidden field *and* validate it server-side.
</details>

### Drill 3
```csharp
public IActionResult A() { TempData["m"] = "x"; return View(); }        // NOT a redirect
public IActionResult B() => Content(TempData["m"]?.ToString() ?? "null");
```
Call `A` then `B`.
<details><summary>👉 Answer</summary>

**`x`** — TempData works across *any* two requests, not just redirects. The redirect is just the usual reason you need it.
</details>

### Drill 4
```razor
@{ var items = new List<string>(); }
<p>@items.FirstOrDefault().Length</p>
```
<details><summary>👉 Answer</summary>

💥 **`NullReferenceException`** at render time. `FirstOrDefault()` on an empty `List<string>` returns `null`. And because it's thrown during **result execution**, an exception *filter* won't catch it (Q39) — only exception middleware will.
</details>

### Drill 5
```csharp
[HttpGet] public IActionResult Delete(int id) { db.Delete(id); return RedirectToAction("Index"); }
```
<details><summary>👉 Answer</summary>

It works — and it's a **serious bug**. A GET must be safe and idempotent. A search-engine crawler, a browser prefetch, or an `<img src="/Products/Delete/5">` on any other website will delete your data. Destructive actions must be `[HttpPost]` + `[ValidateAntiForgeryToken]`.
</details>

### Drill 6
```csharp
[HttpPost]
public IActionResult Save(Product p)
{
    if (!ModelState.IsValid) return View(p);
    db.Add(p);
    return RedirectToAction(nameof(Index));
}
```
The `Product` has `[Required] public string Name` and the user submits an empty name.
<details><summary>👉 Answer</summary>

Returns the **Edit view with the validation message**, and the textbox redisplays the user's empty input — correct behaviour on all three counts. The trap is the *next* question: **`ViewBag`/`ViewData` you set in the GET action are gone**, so any dropdown populated from `ViewBag.Categories` will now throw `NullReferenceException` on redisplay. You must repopulate them before every `return View(p)`. This is the #1 real-world MVC form bug.
</details>

### Drill 7
```razor
@model IEnumerable<Product>
@foreach (var p in Model) { @Html.TextBoxFor(m => p.Name) }
```
<details><summary>👉 Answer</summary>

Every input renders with **`name="p.Name"`** — the same name for every row, and a name that doesn't match anything on POST. Lambda-based helpers read the *expression text*, and here the expression is the loop variable. Fix with an indexed `for` loop (`m => m[i].Name`) or an `EditorTemplate`, which generates correct indexed names automatically.
</details>

### Drill 8
```csharp
public class HomeController : Controller
{
    public HomeController(ILogger<HomeController> l) { }
    public HomeController() { }                    // second constructor
}
```
<details><summary>👉 Answer</summary>

💥 **`InvalidOperationException`** — the DI container requires an **unambiguous** constructor. Multiple public constructors where more than one is satisfiable is an error. Controllers get exactly one constructor.
</details>

### Drill 9
```csharp
[HttpPost] public IActionResult Save([FromBody] string name) => Content(name ?? "null");
```
Body: `name=Ann` with `Content-Type: application/x-www-form-urlencoded`.
<details><summary>👉 Answer</summary>

**415 Unsupported Media Type** (with `[ApiController]`) or `null`. `[FromBody]` uses the configured **input formatters**, which by default handle JSON only. To send a bare string as JSON the body must be `"Ann"` — with the quotes — and `Content-Type: application/json`.
</details>

### Drill 10
```razor
@* _Layout.cshtml *@
@RenderBody()
@RenderBody()
```
<details><summary>👉 Answer</summary>

💥 **`InvalidOperationException`** — `RenderBody` may be called **once**. The body is a forward-only buffer, not a re-readable string. (Sections, by contrast, can only be rendered once each too.)
</details>

### Drill 11
```csharp
public IActionResult Index()
{
    ViewBag.Items = new List<string> { "a" };
    return View();
}
```
```razor
@foreach (var i in ViewBag.Items) { <p>@i</p> }
```
<details><summary>👉 Answer</summary>

It **works** — `foreach` over a `dynamic` resolves at runtime. But `@ViewBag.Items.Where(x => x.Length > 0)` would throw `RuntimeBinderException`, because **extension methods can't be called on `dynamic`**. That one catches almost everybody. Fix: `((List<string>)ViewBag.Items).Where(...)`.
</details>

### Drill 12
```csharp
app.UseEndpoints(e => e.MapControllers());
app.UseAuthentication();
```
<details><summary>👉 Answer</summary>

`UseAuthentication` **never runs for MVC requests** — `MapControllers` is terminal, so the pipeline ends there. `User` is anonymous everywhere and `[Authorize]` rejects every request. No exception, just a totally broken auth system: the worst kind of ordering bug.
</details>

### Drill 13
```csharp
public class Product { public string Name { get; } }      // no setter
```
Posted: `Name=Ann`
<details><summary>👉 Answer</summary>

`Name` stays `null` — **model binding requires a public setter**. Same for `init`-only properties bound from a *form* (they work via the constructor for JSON body binding, but not for form binding). This is why binding to `record` types trips people up.
</details>

### Drill 14
```csharp
[Route("api/[controller]")]
public class ProductsController : ControllerBase
{
    [HttpGet] public IActionResult All() => Ok();
    [HttpGet] public IActionResult Active() => Ok();
}
```
<details><summary>👉 Answer</summary>

💥 **`AmbiguousMatchException`** — both actions map to `GET /api/Products`. `[HttpGet]` with no template doesn't append the action name. Fix: `[HttpGet("all")]` and `[HttpGet("active")]`, or add `[action]` to the controller route.
</details>

### Drill 15
```csharp
public IActionResult Index()
{
    Response.StatusCode = 404;
    return View();
}
```
<details><summary>👉 Answer</summary>

The **Index view renders with a 404 status code**. Setting `StatusCode` doesn't change what's returned — the two are independent. This is actually the standard way to serve a friendly "not found" page that still tells crawlers the truth.
</details>

### Drill 16
```csharp
public class LogFilter : IActionFilter
{
    private int _count;
    public void OnActionExecuting(ActionExecutingContext c) => Console.WriteLine(++_count);
}
builder.Services.AddControllers(o => o.Filters.Add(new LogFilter()));   // ← instance
```
<details><summary>👉 Answer</summary>

Prints `1, 2, 3, …` — adding an **instance** makes it a de-facto singleton shared across all requests, so `_count` accumulates and `++_count` is a **data race**. `o.Filters.Add<LogFilter>()` (the generic form) creates one per request instead. Filter *attributes* are also cached and shared — never put mutable state in a filter.
</details>

### Drill 17
```csharp
[HttpPost]
public async Task<IActionResult> Upload(IFormFile file)
{
    var path = Path.Combine("uploads", file.FileName);
    using var s = System.IO.File.Create(path);
    await file.CopyToAsync(s);
    return Ok();
}
```
<details><summary>👉 Answer</summary>

Works — and is a **path-traversal vulnerability**. `file.FileName` comes from the client and can be `..\..\Windows\System32\x.dll`. Always `Path.GetFileName(file.FileName)`, or better, generate your own name (`Guid.NewGuid()`) and store the original only as metadata. Also validate content type and size.
</details>

### Drill 18
```csharp
public IActionResult Index()
{
    var users = db.Users.Where(u => u.Active);      // IQueryable, not executed
    return View(users);
}
```
```razor
@foreach (var u in Model) { <p>@u.Name</p> }
```
<details><summary>👉 Answer</summary>

Either it works (the query executes during rendering, **after** the action returned) or you get 💥 **`ObjectDisposedException`** if the `DbContext` scope has closed. Either way the DB query runs inside the view, so a slow query looks like a slow *render* and exceptions dodge your exception filter. **Always materialize before returning: `await db.Users.Where(...).ToListAsync()`.** Same deferred-execution rule as LINQ in [file 27, Q57].
</details>

---

# L — How to answer these in the actual interview

**1. Never blurt the answer. Name the stage first.**
> *"This is a model-binding question. The binder tries form, then route, then query string — so the form value 3 wins."*
>
> Saying **which stage of the pipeline** you're in (routing / binding / validation / filters / action / result) is the whole skill. It's also the reason MVC questions feel hard: candidates try to answer them as C# questions.

**2. Trace the request out loud.** For anything you're unsure of:
> *"Request comes in → middleware → routing selects the endpoint → authorization filter → model binding → action filters → action → result filters → the view renders. The exception is in the view, so we're past the action — which means an exception filter won't see it."*
>
> That sentence answers half the questions in section F on its own.

**3. Say "Core or MVC 5?" when it matters.** Q4, Q11, Q30, Q32, Q54 and Q55 have genuinely different answers. Asking *"is this Core or Framework?"* is not dodging — it's the correct question, and interviewers read it as experience.

**4. Name the exception precisely.** *"It errors"* is worth one point. *"`InvalidOperationException` — a singleton can't consume a scoped service, that's a captive dependency, and scope validation only runs in Development by default"* is worth five.

**5. Always give the fix, then the trade-off.** Every answer above has a follow-up for a reason — the rounds you've lost were lost on the *second* question, not the first. Definition → what you'd do → what it costs.

**6. Use your Java/Spring background deliberately.** The concepts map almost one-to-one, and saying so proves you understand mechanisms rather than syntax:

| Spring / Java | ASP.NET Core MVC |
|---|---|
| `@Controller` / `@RestController` | `Controller` / `[ApiController]` |
| `@RequestMapping` / `@GetMapping` | `[Route]` / `[HttpGet]` |
| `@RequestBody` / `@RequestParam` / `@PathVariable` | `[FromBody]` / `[FromQuery]` / `[FromRoute]` |
| `@Valid` + `BindingResult` | `[Required]` + `ModelState` |
| `HandlerInterceptor` | Action filter |
| `@ControllerAdvice` / `@ExceptionHandler` | Exception filter / `UseExceptionHandler` |
| Servlet `Filter` | Middleware |
| `@Autowired`, `@Service`, `@Scope` | Constructor injection, `AddScoped`/`AddSingleton` |
| `@Transactional` | `DbContext` scope / explicit transaction |
| Thymeleaf / JSP | Razor |
| Spring Data JPA | Entity Framework Core |

> *"In Spring this would be a `HandlerInterceptor`; here it's an action filter — same position in the pipeline, and the same `preHandle`/`postHandle` shape."* **That's the sentence that makes 4 years of Java count as 4 years of relevant experience** instead of starting from zero.

**7. If you genuinely don't know:**
> *"My reasoning says X because the binder runs before the action, but that's exactly the kind of thing I'd confirm with a quick integration test using `WebApplicationFactory`."*

---

## Related files in this pack

- **[27 — C# / .NET "Predict the Output"](./27-dotnet-output-tricky-questions.md)** — the language half; several traps here (closures, deferred LINQ, `async void`, DI lifetimes) are that file's rules wearing an HTTP costume
- **[23 — Java "Predict the Output"](./23-java-output-tricky-questions.md)** — the same *style* of question in the language you already know
- **[06 — Spring & Spring Boot](./06-spring-boot.md)** — read alongside section L's mapping table; every Spring concept you know has a named equivalent here
- **[24 — ORM, JPA & Hibernate](./24-orm-jpa-hibernate.md)** — the EF Core equivalents: lazy loading, change tracking, N+1, and `DbContext` scope (Q57 and Drill 18)
- **[17 — SOLID & Design Patterns](./17-solid-design-patterns.md)** — DI, the filter pipeline and middleware are Chain of Responsibility + Dependency Inversion in production
- **[26 — Companies: Questions Actually Asked](./26-companies-asked-questions.md)** — **log every .NET round here the same day**

> 🔁 **Drill routine:** section G first (the `TextBoxFor`/ModelState trap in **Q43** is the single most-asked MVC view question), then sections A/B (routing + binding are where the round starts), then the section J rapid-fire table daily under 5 minutes, then five section K drills each morning.
>
> **Type the code — don't just read it.** Spin up one `dotnet new mvc` project and actually run the ten you find least obvious. Reading gives you recognition; running gives you the answer under pressure.
