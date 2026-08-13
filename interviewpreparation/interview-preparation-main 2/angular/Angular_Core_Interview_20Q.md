# 20 Core Angular Interview Questions & Answers (5+ Years)
### Senior Full-Stack / Frontend Developer Perspective
### Target: PwC, Deloitte, Cognizant, Capgemini, Product Companies

---

## Q1. What is Angular architecture and what are its main building blocks?

**Summary:**
Angular is a component-based, TypeScript-first framework built on a modular architecture. The main building blocks are Components, Templates, Directives, Services, Dependency Injection, Modules (or standalone components in modern Angular), and the Router.

**Explanation — How They Fit Together:**

```
┌─────────────────────────────────────────────────────┐
│                    Angular Application               │
│                                                      │
│  ┌──────────┐    ┌──────────────┐    ┌───────────┐  │
│  │  Router   │───▶│  Components  │◀───│  Services │  │
│  └──────────┘    │  (UI Layer)  │    │ (Business  │  │
│                  │              │    │   Logic)   │  │
│                  │  Template    │    └─────┬──────┘  │
│                  │  + Style     │          │         │
│                  │  + Class     │    Dependency      │
│                  └──────┬───────┘    Injection       │
│                         │                            │
│                  ┌──────▼───────┐                    │
│                  │  Directives  │                    │
│                  │  + Pipes     │                    │
│                  └──────────────┘                    │
│                                                      │
│  ┌────────────────────────────────────────────────┐  │
│  │  Modules (NgModule) OR Standalone Components   │  │
│  │  → Organize, bundle, and lazy-load features    │  │
│  └────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

**Each Building Block's Role:**

| Block | Responsibility | Example |
|-------|---------------|---------|
| **Component** | UI + behavior for a view | `ProductListComponent` — shows product grid |
| **Template** | HTML with Angular syntax | `@for`, `@if`, data binding `{{ }}` |
| **Directive** | Extend HTML behavior | `*ngIf`, `*ngFor`, custom `[highlight]` |
| **Pipe** | Transform display data | `{{ price \| currency:'INR' }}` |
| **Service** | Reusable business logic, API calls | `ProductService`, `AuthService` |
| **DI** | Provides services to components | `inject(ProductService)` |
| **Module/Standalone** | Organizes related code | `ProductModule` or standalone imports |
| **Router** | Maps URLs to components | `/products/:id` → `ProductDetailComponent` |

**Modern Angular (17+) Shift:**
```typescript
// OLD: NgModule-based
@NgModule({
  declarations: [ProductListComponent, ProductCardComponent],
  imports: [CommonModule, FormsModule],
  providers: [ProductService]
})
export class ProductModule {}

// NEW: Standalone — each component is self-contained
@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [ProductCardComponent, CurrencyPipe],
  template: `
    @for (product of products(); track product.id) {
      <app-product-card [product]="product" />
    }
  `
})
export class ProductListComponent {
  private productService = inject(ProductService);
  products = toSignal(this.productService.getAll(), { initialValue: [] });
}
```

**Real-World Scenario:**
In every enterprise Angular project I've worked on, the architecture follows: feature-based folder structure, lazy-loaded routes per feature, shared services in `core/`, reusable UI components in `shared/`, and a clear separation between smart components (container — fetches data) and dumb components (presentational — receives `@Input`).

**Closing:**
Angular's architecture enforces structure and scalability. The shift from NgModules to standalone components simplifies the mental model — but the core building blocks remain the same.

---

## Q2. What is the difference between Angular components and directives?

**Summary:**
A component IS a directive — with a template. Components have their own view (HTML + CSS), while directives modify the behavior or appearance of existing DOM elements. Think of components as "building blocks with UI" and directives as "behavior modifiers."

**Explanation:**

```
Directive (base class)
    │
    ├── Component (@Component) — has a template, creates a view
    │     Example: <app-product-card [product]="p" />
    │
    ├── Structural Directive — changes DOM layout (adds/removes elements)
    │     Example: *ngIf, *ngFor, @if, @for
    │
    └── Attribute Directive — changes appearance/behavior of existing element
          Example: [ngClass], [ngStyle], custom [appHighlight]
```

| Feature | Component | Directive |
|---------|-----------|-----------|
| Template | Yes (required) | No |
| Selector | Element (`app-card`) | Attribute (`[appHighlight]`) |
| View Encapsulation | Yes (Shadow DOM / Emulated) | No (operates on host element) |
| Lifecycle Hooks | Full set | Full set |
| Multiple per element | One component per element | Multiple directives per element |

**Practical Example — When I Use Each:**
```typescript
// COMPONENT — has its own template and view
@Component({
  selector: 'app-user-avatar',
  standalone: true,
  template: `
    <img [src]="user.avatarUrl" [alt]="user.name" class="avatar" />
    <span class="status" [class.online]="user.isOnline"></span>
  `
})
export class UserAvatarComponent {
  @Input() user!: User;
}

// DIRECTIVE — modifies an existing element's behavior
@Directive({
  selector: '[appTooltip]',
  standalone: true
})
export class TooltipDirective {
  @Input('appTooltip') tooltipText = '';

  @HostListener('mouseenter')
  show() { /* create tooltip overlay */ }

  @HostListener('mouseleave')
  hide() { /* remove tooltip overlay */ }
}

// USAGE: Component creates UI, directive adds behavior
<app-user-avatar [user]="currentUser" appTooltip="Click to view profile" />
```

**Closing:**
If it needs its own HTML template — make it a component. If it modifies an existing element — make it a directive. This distinction becomes critical when designing reusable component libraries.

---

## Q3. What are the different types of directives in Angular?

**Summary:**
Angular has three types: Component directives (with templates), Structural directives (that change DOM layout by adding/removing elements), and Attribute directives (that change the appearance or behavior of an existing element).

**Explanation with Examples:**

**1. Component Directive (most common):**
```typescript
@Component({
  selector: 'app-alert',
  template: `<div [class]="'alert alert-' + type">
    <ng-content></ng-content>
  </div>`
})
export class AlertComponent {
  @Input() type: 'success' | 'error' | 'warning' = 'success';
}
// <app-alert type="error">Something went wrong</app-alert>
```

**2. Structural Directive (changes DOM structure):**
```typescript
// Custom *appHasRole directive — shows element only if user has required role
@Directive({
  selector: '[appHasRole]',
  standalone: true
})
export class HasRoleDirective {
  private authService = inject(AuthService);

  @Input() set appHasRole(role: string) {
    if (this.authService.hasRole(role)) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    } else {
      this.viewContainer.clear();
    }
  }

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef
  ) {}
}

// Usage
<button *appHasRole="'ADMIN'">Delete User</button>
// Button only exists in DOM if user is ADMIN
```

**3. Attribute Directive (changes behavior/appearance):**
```typescript
// Custom highlight directive
@Directive({
  selector: '[appHighlight]',
  standalone: true
})
export class HighlightDirective {
  @Input() appHighlight = 'yellow';

  @HostListener('mouseenter')
  onMouseEnter() {
    this.el.nativeElement.style.backgroundColor = this.appHighlight;
  }

  @HostListener('mouseleave')
  onMouseLeave() {
    this.el.nativeElement.style.backgroundColor = '';
  }

  constructor(private el: ElementRef) {}
}

// Usage
<p appHighlight="lightblue">Hover over me!</p>
```

**Angular 17+ Built-in Control Flow (Replaces Structural Directives):**
```html
<!-- OLD structural directives -->
<div *ngIf="isLoading; else content">Loading...</div>
<ng-template #content>
  <div *ngFor="let item of items; trackBy: trackById">{{ item.name }}</div>
</ng-template>

<!-- NEW built-in control flow (no imports needed!) -->
@if (isLoading) {
  <div>Loading...</div>
} @else {
  @for (item of items; track item.id) {
    <div>{{ item.name }}</div>
  } @empty {
    <div>No items found</div>
  }
}
```

**Real-World Scenario:**
In an enterprise app, I created a `*appHasPermission` structural directive that checked against a permission matrix. Every button, menu item, and section used it — one directive controlling visibility across the entire app. This was cleaner than putting `*ngIf` with permission checks everywhere.

**Closing:**
Structural directives are power tools — I build custom ones for auth/permission logic. Attribute directives handle cross-cutting UI concerns like tooltips, drag-and-drop, and accessibility. In Angular 17+, the new `@if`/`@for` syntax replaces the need for many built-in structural directives.

---

## Q4. What is Change Detection in Angular and how does it work?

**Summary:**
Change Detection (CD) is Angular's mechanism to keep the DOM in sync with the component's data. When something changes (click, HTTP response, timer), Angular walks the component tree top-to-bottom, compares current values with previous values, and updates the DOM where differences are found.

**Explanation — How It Works Internally:**

```
User clicks a button
    │
    ▼
Zone.js intercepts the async event
    │
    ▼
Zone.js calls ApplicationRef.tick()
    │
    ▼
Angular starts Change Detection from ROOT
    │
    ▼
┌─────────────────────────────────────────┐
│            AppComponent                  │
│  Check bindings: {{ title }} changed?    │──▶ Update DOM if yes
│                                          │
│    ┌──────────────┐  ┌───────────────┐  │
│    │ HeaderComp   │  │ SidebarComp   │  │
│    │ Check...     │  │ Check...      │  │
│    └──────────────┘  └───────────────┘  │
│                                          │
│    ┌──────────────────────────────────┐  │
│    │ ProductListComponent             │  │
│    │  Check bindings...               │  │
│    │                                  │  │
│    │  ┌────────┐ ┌────────┐ ┌──────┐│  │
│    │  │Card 1  │ │Card 2  │ │Card 3││  │
│    │  │Check...│ │Check...│ │Check.││  │
│    │  └────────┘ └────────┘ └──────┘│  │
│    └──────────────────────────────────┘  │
└─────────────────────────────────────────┘
Every component is checked — EVERY TIME (Default strategy)
```

**What Triggers Change Detection:**
- DOM events (click, input, submit)
- HTTP responses (`HttpClient`)
- Timers (`setTimeout`, `setInterval`)
- Promises resolving
- Any async operation patched by Zone.js

**Zone.js — The Engine Behind CD:**
```typescript
// Zone.js monkey-patches browser APIs:
// setTimeout, setInterval, addEventListener, Promise, fetch, XMLHttpRequest

// When any of these complete, Zone.js notifies Angular:
// "Something async just happened — run change detection!"

// This is why Angular "magically" updates the view when data changes
// — Zone.js is watching every async operation
```

**What CD Actually Compares:**
```typescript
@Component({
  template: `
    <h1>{{ title }}</h1>              <!-- CD compares: old title vs new title -->
    <p [class.active]="isActive"></p> <!-- CD compares: old isActive vs new isActive -->
    <app-child [data]="userData">     <!-- CD compares: old reference vs new reference -->
  `
})
```
Angular checks every binding expression in the template. If the value changed → update the DOM node. If not → skip.

**Real-World Scenario:**
In a dashboard with 200 components and 50 WebSocket updates per second, Default CD was running 200 × 50 = 10,000 component checks per second. This caused visible jank. The fix was switching to OnPush + Signals, reducing checks to only the 5-10 components that actually received new data.

**Common Mistakes:**
- Calling functions in templates — `{{ getTotal() }}` runs on EVERY CD cycle. Use pipes or `computed()` signals instead.
- Not understanding Zone.js — developers wonder "why does my view update automatically?" or "why doesn't it update?" The answer is always Zone.js (or lack of it for operations outside the zone).
- Triggering CD in a loop — `ngOnInit` sets a value → triggers CD → CD reads a getter that returns a new object → triggers another CD → `ExpressionChangedAfterItHasBeenCheckedError`.

**Closing:**
Change detection is the heartbeat of Angular. Understanding it is what separates someone who "uses Angular" from someone who "architects Angular applications." Default CD is safe but expensive; OnPush is performant but requires discipline.

---

## Q5. What is the difference between Default Change Detection and OnPush strategy?

**Summary:**
Default checks every component on every async event — safe but expensive. OnPush only checks a component when its `@Input` reference changes, a DOM event originates from it, an async pipe emits, or you manually trigger it. I use OnPush as the standard in all production projects.

**Explanation:**

```
Default Strategy:                    OnPush Strategy:
========================            ========================
Click anywhere in app               Click anywhere in app
    │                                   │
    ▼                                   ▼
Check AppComponent ✓                Check AppComponent ✓
  Check Header ✓                      Check Header ✗ (skipped!)
  Check Sidebar ✓                     Check Sidebar ✗ (skipped!)
  Check ProductList ✓                 Check ProductList ✗ (skipped!)
    Check Card1 ✓                       (entire subtree skipped)
    Check Card2 ✓
    Check Card3 ✓

All 7 components checked            Only 1 component checked
```

**OnPush Triggers — When Angular WILL Check:**

```typescript
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductCardComponent {
  @Input() product!: Product;

  // Trigger 1: @Input REFERENCE changes
  // parent does: this.product = { ...this.product, name: 'New' }  ✅ detects
  // parent does: this.product.name = 'New'                        ❌ missed!

  // Trigger 2: DOM event from THIS component
  onClick() { /* CD runs because event originated here */ }

  // Trigger 3: async pipe emits new value
  // {{ data$ | async }} — async pipe calls markForCheck() internally

  // Trigger 4: Signal changes (Angular 17+)
  count = signal(0);
  // {{ count() }} — signal notifies Angular of change

  // Trigger 5: Manual trigger
  constructor(private cdr: ChangeDetectorRef) {}
  manualUpdate() {
    this.cdr.markForCheck();  // marks dirty, checked in next cycle
    // or
    this.cdr.detectChanges(); // checks immediately
  }
}
```

**Side-by-Side Comparison:**

| Aspect | Default | OnPush |
|--------|---------|--------|
| When it checks | Every async event | Input ref change, events, async pipe, manual |
| Performance | O(n) all components | O(changed) only affected |
| Mutation safe? | Yes (detects mutations) | No (needs new references) |
| Best with | Quick prototyping | Production apps, immutable data |
| Debugging | Easier | Requires understanding of triggers |

**Pattern — OnPush + Immutable Data:**
```typescript
// PARENT — always create new references
updateProduct() {
  // ❌ Mutation — OnPush child won't detect
  this.product.price = 999;

  // ✅ New reference — OnPush child detects
  this.product = { ...this.product, price: 999 };
}

// For arrays:
// ❌ this.items.push(newItem);
// ✅ this.items = [...this.items, newItem];
```

**Pattern — OnPush + Signals (Best in Angular 17+):**
```typescript
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2>{{ product().name }}</h2>
    <p>{{ product().price | currency }}</p>
    <button (click)="updatePrice()">Update</button>
  `
})
export class ProductCardComponent {
  product = signal<Product>({ name: 'Widget', price: 100 });

  updatePrice() {
    this.product.update(p => ({ ...p, price: p.price + 10 }));
    // Signal notifies Angular — only THIS component re-renders
  }
}
```

**Real-World Scenario:**
In a real-time trading app, we had a grid of 500 stock price cells updating via WebSocket. With Default CD, every price update triggered 500 component checks — the UI froze. After switching all components to OnPush and using `markForCheck()` only on the specific cell that received a new price, CD dropped to 1-2 components per update. Frame rate went from 15fps to 60fps.

**Common Mistakes:**
- Using OnPush but mutating objects — UI doesn't update, developer adds `detectChanges()` everywhere as a band-aid.
- Calling `detectChanges()` in a loop — worse than Default CD.
- Forgetting that `async` pipe handles `markForCheck()` automatically — no manual trigger needed when using `async` pipe with OnPush.

**Closing:**
OnPush + immutable data + async pipe (or Signals) is the Angular performance trifecta. I configure it as the default in every project and treat Default CD as a code smell that needs justification.

---

## Q6. What are RxJS Observables and how are they used in Angular?

**Summary:**
Observables are lazy, push-based data streams that can emit multiple values over time. Angular uses them everywhere — HTTP calls, router events, form value changes, and component communication. They're the backbone of async operations in Angular.

**Explanation:**

```
Promise:     Returns ONE value    |---value---|
Observable:  Returns MANY values  |--v1--v2--v3--v4--|

Promise:     Eager (starts immediately)
Observable:  Lazy (nothing happens until subscribe)

Promise:     Not cancellable
Observable:  Cancellable (unsubscribe stops execution)
```

**Where Angular Uses Observables:**

```typescript
// 1. HTTP Client — every call returns Observable
this.http.get<Product[]>('/api/products').subscribe(products => {
  this.products = products;
});

// 2. Router — params, query params, events
this.route.params.subscribe(params => {
  this.productId = params['id'];
});

// 3. Reactive Forms — value/status changes
this.searchForm.get('query')!.valueChanges.pipe(
  debounceTime(300),
  distinctUntilChanged(),
  switchMap(query => this.searchService.search(query))
).subscribe(results => this.results = results);

// 4. Component communication via Service
// See Subject variants — Q8

// 5. ViewChild queries (signal-based in Angular 17+)
// 6. Interceptors — return Observable<HttpEvent>
```

**Creating and Consuming Observables:**
```typescript
// Creating a custom Observable
getStockPrice(symbol: string): Observable<number> {
  return new Observable(subscriber => {
    const ws = new WebSocket(`wss://stocks.example.com/${symbol}`);

    ws.onmessage = (event) => subscriber.next(JSON.parse(event.data).price);
    ws.onerror = (error) => subscriber.error(error);
    ws.onclose = () => subscriber.complete();

    // Teardown logic — runs on unsubscribe
    return () => ws.close();
  });
}

// Consuming with operators
this.getStockPrice('AAPL').pipe(
  filter(price => price > 150),
  map(price => `$${price.toFixed(2)}`),
  takeUntilDestroyed()
).subscribe(formatted => this.displayPrice = formatted);
```

**The async Pipe — Best Practice:**
```html
<!-- Let Angular handle subscribe + unsubscribe automatically -->
@if (products$ | async; as products) {
  @for (product of products; track product.id) {
    <app-product-card [product]="product" />
  }
} @else {
  <app-loading-spinner />
}
```

**Real-World Scenario:**
In a search feature, I chained RxJS operators to handle debouncing, cancellation of stale requests, error recovery, and loading states — all in a single declarative pipeline. Without Observables, this would require manual setTimeout management, abort controllers, and flag variables.

**Common Mistakes:**
- Subscribing inside subscribe (callback hell) — use `switchMap`, `mergeMap` instead.
- Forgetting to unsubscribe — memory leaks. Use `async` pipe or `takeUntilDestroyed()`.
- Subscribing to `HttpClient` and also using `async` pipe — double subscription, double HTTP call.
- Not understanding cold vs hot — `http.get()` is cold (new request per subscribe). Subject is hot (shared).

**Closing:**
Observables are Angular's async primitive. Master the `pipe()` + operator pattern, use `async` pipe in templates, and you'll write clean, leak-free Angular code.

---

## Q7. What is the difference between Observable, Promise, and Subject?

**Summary:**
Promise is a one-shot async value. Observable is a stream that can emit zero to infinite values over time and is cancellable. Subject is a special Observable that can both emit AND be subscribed to — it's used for multicasting events to multiple consumers.

**Explanation:**

| Feature | Promise | Observable | Subject |
|---------|---------|------------|---------|
| Values | Single | Multiple | Multiple |
| Execution | Eager (runs immediately) | Lazy (runs on subscribe) | Hot (multicast) |
| Cancellable | No | Yes (unsubscribe) | Yes |
| Operators | `.then()/.catch()` | `pipe(map, filter...)` | Same as Observable |
| Multicast | N/A | Unicast (1 subscriber = 1 execution) | Multicast (shared) |
| Use Case | One-time async result | Streams, HTTP, events | Event bus, state sharing |

**Practical Demonstration:**

```typescript
// PROMISE — single value, eager
const promise = new Promise(resolve => {
  console.log('Promise executes IMMEDIATELY');
  resolve(42);
});
// "Promise executes IMMEDIATELY" — printed even without .then()

// OBSERVABLE — lazy, cancellable
const observable = new Observable(subscriber => {
  console.log('Observable executes on SUBSCRIBE');
  subscriber.next(1);
  subscriber.next(2);
  setTimeout(() => subscriber.next(3), 1000);
});
// Nothing printed yet — lazy!
const sub = observable.subscribe(val => console.log(val));
// Now: "Observable executes on SUBSCRIBE", 1, 2, ...(1sec)... 3
sub.unsubscribe(); // Cancels — no more values

// SUBJECT — multicast, acts as both Observable and Observer
const subject = new Subject<string>();
subject.subscribe(val => console.log('Subscriber A:', val));
subject.subscribe(val => console.log('Subscriber B:', val));
subject.next('Hello');
// "Subscriber A: Hello"
// "Subscriber B: Hello"  — BOTH receive the same value
```

**Cold (Observable) vs Hot (Subject):**
```typescript
// COLD — each subscriber gets its own execution
const cold$ = this.http.get('/api/data');
cold$.subscribe(); // HTTP call #1
cold$.subscribe(); // HTTP call #2 — separate request!

// HOT — shared execution
const hot$ = this.http.get('/api/data').pipe(shareReplay(1));
hot$.subscribe(); // HTTP call #1
hot$.subscribe(); // No new call — gets cached result
```

**When I Use Each:**
```
Promise  → External library that returns Promise (converted with from())
           → Simple one-shot async (rare in Angular)

Observable → HTTP calls (Angular's HttpClient)
           → Router events, form changes
           → Any data stream with operators

Subject  → Cross-component communication via services
         → Custom event bus
         → Sharing state (BehaviorSubject)
```

**Real-World Scenario:**
In a notification system, I used a `Subject` in a shared service. Multiple components (header badge, sidebar, toast) all subscribed to the same notification stream. When the API pushed a notification, I called `subject.next(notification)` once, and all three components updated simultaneously. A Promise couldn't do this (single value), and a regular Observable would mean separate executions.

**Closing:**
Promise for single async values, Observable for streams with operators, Subject for multicasting to multiple consumers. In Angular, Observable is the default — Promises are the exception.

---

## Q8. What are the different types of Subjects in RxJS?

**Summary:**
There are four Subject types: `Subject` (no replay), `BehaviorSubject` (replays latest, requires initial value), `ReplaySubject` (replays N values), and `AsyncSubject` (emits only the last value on complete). BehaviorSubject is the most used in Angular services for state management.

**Explanation:**

```
Timeline:        emit(A)  emit(B)  emit(C)  [NEW SUBSCRIBER]  emit(D)

Subject:                                     → gets: D
BehaviorSubject:                             → gets: C, D     (latest)
ReplaySubject(2):                            → gets: B, C, D  (last 2)
AsyncSubject:                                → gets: nothing until complete()
                                               on complete() → gets: D (last only)
```

**Subject — Event Bus:**
```typescript
@Injectable({ providedIn: 'root' })
export class EventBusService {
  private events$ = new Subject<AppEvent>();

  emit(event: AppEvent) { this.events$.next(event); }

  on(eventType: string): Observable<AppEvent> {
    return this.events$.pipe(filter(e => e.type === eventType));
  }
}
// Late subscribers miss past events — that's intentional for transient events
```

**BehaviorSubject — Current State (Most Used):**
```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUser$ = new BehaviorSubject<User | null>(null);  // initial: null
  user$ = this.currentUser$.asObservable();

  // Any component subscribing RIGHT NOW gets the current user immediately
  login(creds: Credentials) {
    return this.http.post<User>('/api/login', creds).pipe(
      tap(user => this.currentUser$.next(user))
    );
  }

  // Synchronous access when needed
  get isLoggedIn(): boolean {
    return this.currentUser$.getValue() !== null;
  }
}
```

**ReplaySubject — Message History:**
```typescript
@Injectable({ providedIn: 'root' })
export class ChatService {
  private messages$ = new ReplaySubject<ChatMessage>(50);  // buffer last 50

  send(msg: ChatMessage) { this.messages$.next(msg); }

  getMessages(): Observable<ChatMessage> {
    return this.messages$.asObservable();
    // New subscriber instantly gets last 50 messages, then live updates
  }
}
```

**AsyncSubject — Task Completion:**
```typescript
// Emits ONLY the last value, and ONLY when complete()
const result$ = new AsyncSubject<Report>();

// Long-running computation
generateReport().subscribe({
  next: (partial) => result$.next(partial),  // buffered, not emitted yet
  complete: () => result$.complete()          // NOW emits the last value
});

result$.subscribe(finalReport => console.log('Done:', finalReport));
```

**Decision Guide:**
```
Need current state for late subscribers?        → BehaviorSubject
Need event bus / fire-and-forget?               → Subject
Need message history / replay past N events?    → ReplaySubject
Need only final result after completion?        → AsyncSubject (rare)
```

**Closing:**
`BehaviorSubject` covers 80% of state management use cases in Angular services. `Subject` for events, `ReplaySubject` for history. I rarely use `AsyncSubject` in practice.

---

## Q9. What is Lazy Loading in Angular and how does it improve performance?

**Summary:**
Lazy loading defers loading a route's JavaScript bundle until the user navigates to it. This reduces the initial bundle size and speeds up the first page load. In a large app with 20 features, the user downloads only the code for the page they're visiting — not the entire application.

**Explanation:**

```
WITHOUT Lazy Loading:
Initial download: [App + Dashboard + Products + Admin + Reports + Settings]
                  = 2MB → Slow first paint

WITH Lazy Loading:
Initial download: [App + Dashboard] = 300KB → Fast first paint
Navigate to Products: [Products] = 200KB → loaded on demand
Navigate to Admin: [Admin] = 150KB → loaded on demand
```

**Implementation — Standalone Components (Modern Angular):**
```typescript
// app.routes.ts
export const routes: Routes = [
  { path: '', component: HomeComponent },  // Eager — in main bundle

  // Lazy load a standalone component
  {
    path: 'products',
    loadComponent: () => import('./products/product-list.component')
      .then(m => m.ProductListComponent)
  },

  // Lazy load a set of child routes
  {
    path: 'admin',
    loadChildren: () => import('./admin/admin.routes')
      .then(m => m.ADMIN_ROUTES),
    canActivate: [authGuard]
  }
];

// admin/admin.routes.ts
export const ADMIN_ROUTES: Routes = [
  { path: '', component: AdminDashboardComponent },
  { path: 'users', component: UserManagementComponent },
  { path: 'settings', component: AdminSettingsComponent }
];
```

**Preloading Strategies:**
```typescript
// Load lazy modules in the background AFTER the app starts
provideRouter(
  routes,
  withPreloading(PreloadAllModules)  // preload all lazy routes in background
)

// Custom: Only preload high-priority routes
provideRouter(
  routes,
  withPreloading(SelectivePreloadStrategy)
)
```

**Verifying Lazy Loading Works:**
```
Open Chrome DevTools → Network tab → Navigate to lazy route
You should see a new JS chunk being downloaded:
  chunk-PRODUCTS-XXXX.js  (200 KB)

If you DON'T see a separate chunk → the route is NOT lazy loaded
(likely imported directly somewhere in the app)
```

**Real-World Impact:**
In an enterprise HR application with 25 feature modules, the initial bundle was 4.5MB. After implementing lazy loading for all features except the login and dashboard, the initial bundle dropped to 800KB. First Contentful Paint improved from 4.2s to 1.3s on 3G.

**Common Mistakes:**
- Importing the lazy-loaded component directly in another eager module — breaks lazy loading, silently bundles it in main chunk.
- Not lazy loading the largest features — lazy load by feature size impact, not alphabetically.
- Too granular lazy loading — lazy loading a 5KB component adds more overhead (HTTP request) than it saves.

**Closing:**
Lazy loading is the single most impactful performance optimization for initial load time. Every feature route should be lazy loaded unless it's the landing page. Pair with a preloading strategy for the best perceived performance.

---

## Q10. What are Angular lifecycle hooks and when are they used?

**Summary:**
Lifecycle hooks are methods Angular calls at specific moments in a component's life — creation, rendering, updates, and destruction. The most important are `ngOnInit` (initialization), `ngOnChanges` (input changes), `ngAfterViewInit` (DOM ready), and `ngOnDestroy` (cleanup).

**Explanation — Execution Order:**

```
CREATION PHASE:
  constructor()          → DI injection, NO Angular bindings yet
  ngOnChanges()          → First @Input values received
  ngOnInit()             → Component initialized, @Inputs available
  ngDoCheck()            → Custom change detection
  ngAfterContentInit()   → <ng-content> projected content ready
  ngAfterContentChecked()→ After projected content checked

  ngAfterViewInit()      → Component's view + child views rendered
  ngAfterViewChecked()   → After view checked

UPDATE PHASE (on every CD cycle):
  ngOnChanges()          → @Input reference changed
  ngDoCheck()
  ngAfterContentChecked()
  ngAfterViewChecked()

DESTRUCTION PHASE:
  ngOnDestroy()          → Cleanup before component is removed
```

**The Big 5 — What I Use in Production:**

```typescript
@Component({...})
export class ProductDetailComponent implements
    OnInit, OnChanges, AfterViewInit, OnDestroy {

  @Input() productId!: number;
  @ViewChild('priceChart') chartElement!: ElementRef;

  private destroy$ = new Subject<void>();

  // 1. ngOnChanges — reacts to @Input changes
  ngOnChanges(changes: SimpleChanges) {
    if (changes['productId'] && !changes['productId'].firstChange) {
      this.loadProduct(this.productId);  // reload when parent changes input
    }
  }

  // 2. ngOnInit — one-time setup (MOST USED)
  ngOnInit() {
    this.loadProduct(this.productId);
    this.setupWebSocket();
  }

  // 3. ngAfterViewInit — DOM is ready, safe to access @ViewChild
  ngAfterViewInit() {
    this.initChart(this.chartElement.nativeElement);
  }

  // 4. ngOnDestroy — cleanup subscriptions, timers, listeners
  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    this.chart?.destroy();
  }
}
```

**Lifecycle Hook Cheat Sheet:**

| Hook | When | Use Case |
|------|------|----------|
| `constructor` | Class instantiation | DI only, no logic |
| `ngOnChanges` | Before ngOnInit, then on @Input change | React to parent data changes |
| `ngOnInit` | Once, after first ngOnChanges | HTTP calls, setup, initialization |
| `ngDoCheck` | Every CD cycle | Custom dirty checking (rare) |
| `ngAfterContentInit` | Once, after content projection | Access @ContentChild |
| `ngAfterViewInit` | Once, after view renders | Access @ViewChild, init DOM plugins |
| `ngOnDestroy` | Before component removal | Unsubscribe, cleanup, deregister |

**Common Mistakes:**
- Putting logic in constructor instead of `ngOnInit` — `@Input` values aren't set in the constructor.
- Accessing `@ViewChild` in `ngOnInit` — it's not available until `ngAfterViewInit`.
- Accessing `@ContentChild` in `ngAfterViewInit` — use `ngAfterContentInit` (it runs earlier).
- Forgetting `ngOnDestroy` cleanup — memory leaks from subscriptions and event listeners.
- Heavy logic in `ngDoCheck` or `ngAfterViewChecked` — they run on EVERY CD cycle, kills performance.

**Closing:**
In practice, 90% of my hooks usage is `ngOnInit` for setup and `ngOnDestroy` for cleanup. In Angular 17+ with Signals and `inject()`, even these become less necessary — `effect()` and `DestroyRef` handle most cases in the constructor.

---

## Q11. What is the difference between ngOnInit() and constructor()?

**Summary:**
The constructor is a TypeScript/JavaScript feature for dependency injection. `ngOnInit` is an Angular lifecycle hook where initialization logic goes. The key difference: `@Input` properties are NOT available in the constructor but ARE available in `ngOnInit`.

**Explanation:**

```
Timeline:
  1. constructor()   → Angular creates the class instance
                       → DI injects dependencies
                       → @Input values NOT yet set (undefined)

  2. ngOnChanges()   → @Input values set for the FIRST time

  3. ngOnInit()      → Safe to use @Input values
                     → Safe to make HTTP calls
                     → Safe to initialize logic
```

**Practical Demonstration:**
```typescript
@Component({
  selector: 'app-user-profile',
  template: `<h1>{{ user?.name }}</h1>`
})
export class UserProfileComponent implements OnInit {
  @Input() userId!: number;

  constructor(private userService: UserService) {
    console.log(this.userId);  // ❌ undefined — @Input not set yet
    // this.userService.getUser(this.userId); // ❌ Would call getUser(undefined)
  }

  ngOnInit() {
    console.log(this.userId);  // ✅ has the value from parent
    this.userService.getUser(this.userId).subscribe(user => {
      this.user = user;         // ✅ Correct place for initialization
    });
  }
}
```

**What Goes Where:**

| constructor | ngOnInit |
|-------------|----------|
| Dependency injection | HTTP calls |
| Simple variable assignments | Subscription setup |
| Nothing else | Using @Input values |
| | Setting up intervals/listeners |
| | Complex initialization logic |

**Modern Angular (17+) — The Line Is Blurring:**
```typescript
@Component({...})
export class UserProfileComponent {
  private userService = inject(UserService);  // DI in field declaration
  userId = input.required<number>();          // Signal input

  // With signal inputs, you use effect() or computed() — no ngOnInit needed
  user = toSignal(
    toObservable(this.userId).pipe(
      switchMap(id => this.userService.getUser(id))
    )
  );

  // constructor() — empty or not even declared
  // ngOnInit() — not needed!
}
```

**Real-World Scenario:**
A junior developer put an HTTP call in the constructor that depended on `@Input`. It worked in isolation (hardcoded test data) but failed when used inside `*ngFor` because Angular hadn't set the inputs yet. Moving it to `ngOnInit` fixed the issue immediately.

**Closing:**
Simple rule: constructor for DI, `ngOnInit` for everything else. In Angular 17+ with `inject()` and signal inputs, the constructor becomes almost empty and `ngOnInit` is often replaced by `computed()`/`effect()`.

---

## Q12. What are Reactive Forms and Template-driven Forms? What are the differences?

**Summary:**
Template-driven forms are declared in HTML using `ngModel` — Angular handles the form model implicitly. Reactive forms are built programmatically in TypeScript using `FormGroup`/`FormControl` — you have full control. I use Reactive Forms in every production project for their testability and dynamic capabilities.

**Explanation:**

**Template-Driven Form (Simple, Quick):**
```typescript
@Component({
  imports: [FormsModule],
  template: `
    <form #userForm="ngForm" (ngSubmit)="onSubmit(userForm)">
      <input name="name" [(ngModel)]="user.name" required minlength="3" />
      <input name="email" [(ngModel)]="user.email" required email />

      @if (userForm.controls['name']?.errors?.['minlength']) {
        <span class="error">Name must be at least 3 characters</span>
      }

      <button [disabled]="userForm.invalid">Submit</button>
    </form>
  `
})
export class SimpleFormComponent {
  user = { name: '', email: '' };

  onSubmit(form: NgForm) {
    if (form.valid) console.log(this.user);
  }
}
```

**Reactive Form (Full Control):**
```typescript
@Component({
  imports: [ReactiveFormsModule],
  template: `
    <form [formGroup]="userForm" (ngSubmit)="onSubmit()">
      <input formControlName="name" />
      <input formControlName="email" />

      @if (userForm.get('name')?.hasError('minlength')) {
        <span>Name must be at least 3 characters</span>
      }

      <button [disabled]="userForm.invalid">Submit</button>
    </form>
  `
})
export class UserFormComponent {
  userForm = new FormGroup({
    name: new FormControl('', [Validators.required, Validators.minLength(3)]),
    email: new FormControl('', [Validators.required, Validators.email]),
    address: new FormGroup({
      street: new FormControl(''),
      city: new FormControl(''),
      zip: new FormControl('', [Validators.pattern(/^\d{6}$/)])
    })
  });

  onSubmit() {
    if (this.userForm.valid) {
      console.log(this.userForm.value);
      // { name: 'John', email: 'john@example.com', address: { street: '...', ... } }
    }
  }
}
```

**Comparison:**

| Feature | Template-Driven | Reactive |
|---------|----------------|----------|
| Model creation | Implicit (Angular) | Explicit (developer) |
| Directives | `ngModel`, `ngForm` | `formGroup`, `formControlName` |
| Validation | Template attributes (`required`) | TypeScript (`Validators.required`) |
| Dynamic fields | Difficult | Easy (`FormArray`, add/remove) |
| Testing | Requires DOM rendering | Pure TypeScript tests |
| Async validation | Complex | Simple (returns Observable) |
| Complex forms | Not suited | Designed for it |

**Why Reactive Forms Win for Production:**
```typescript
// Dynamic fields — adding line items to an invoice
invoiceForm = new FormGroup({
  customer: new FormControl('', Validators.required),
  items: new FormArray([])
});

addItem() {
  const items = this.invoiceForm.get('items') as FormArray;
  items.push(new FormGroup({
    description: new FormControl('', Validators.required),
    quantity: new FormControl(1, Validators.min(1)),
    price: new FormControl(0, Validators.min(0))
  }));
}

removeItem(index: number) {
  (this.invoiceForm.get('items') as FormArray).removeAt(index);
}

// Custom async validator
emailValidator(): AsyncValidatorFn {
  return (control) => {
    return this.userService.checkEmailExists(control.value).pipe(
      map(exists => exists ? { emailTaken: true } : null),
      catchError(() => of(null))
    );
  };
}
```

**Real-World Scenario:**
In a loan application form with 5 steps, conditional fields (show "business details" only for business loans), dynamic co-applicant sections (add/remove), and cross-field validation (loan amount can't exceed 5x income), Reactive Forms handled everything cleanly. Template-driven would have been a nightmare of template refs and conditional `ngModel` bindings.

**Closing:**
Template-driven for login forms and simple contact forms. Reactive for everything else. In enterprise apps, I standardize on Reactive Forms exclusively — the testability and dynamic capabilities are non-negotiable.

---

## Q13. What is Dependency Injection (DI) in Angular?

**Summary:**
Dependency Injection is a design pattern where components receive their dependencies from the outside rather than creating them. Angular has a built-in DI framework with a hierarchical injector tree — it's how services, configurations, and even HTTP clients are provided to components.

**Explanation:**

```
WITHOUT DI:
class OrderComponent {
  private orderService = new OrderService(new HttpClient(...), new AuthService(...));
  // ❌ Component creates its own dependencies
  // ❌ Tightly coupled — can't swap, can't test
}

WITH DI:
class OrderComponent {
  constructor(private orderService: OrderService) {}
  // ✅ Angular provides the dependency
  // ✅ Loosely coupled — can swap implementations, easy to mock in tests
}

// Modern Angular (17+):
class OrderComponent {
  private orderService = inject(OrderService);  // functional injection
}
```

**How Angular DI Works:**

```
1. REGISTER a provider (tell Angular HOW to create the service)
2. Component DECLARES a dependency (constructor param or inject())
3. Angular RESOLVES the dependency by walking the injector tree

Injector Tree:
  Root Injector (providedIn: 'root') → Singleton for entire app
      │
      ├── Route Injector (lazy route providers) → Scoped to route
      │
      └── Component Injector (providers: [...]) → New instance per component
              │
              └── Child Component → inherits parent's injector
```

**Provider Registration Methods:**
```typescript
// 1. providedIn: 'root' — recommended, tree-shakable singleton
@Injectable({ providedIn: 'root' })
export class AuthService { }

// 2. Component-level — new instance per component
@Component({
  providers: [LoggerService]  // each component gets its own LoggerService
})
export class DashboardComponent { }

// 3. Route-level — scoped to lazy route
{
  path: 'admin',
  loadComponent: () => import('./admin.component'),
  providers: [AdminService]  // scoped to admin route, destroyed when navigated away
}

// 4. InjectionToken — for non-class values
export const API_URL = new InjectionToken<string>('API_URL');

// Providing a value
providers: [{ provide: API_URL, useValue: 'https://api.example.com' }]

// Injecting it
private apiUrl = inject(API_URL);
```

**Advanced — Swapping Implementations:**
```typescript
// Interface/abstract class
abstract class NotificationChannel {
  abstract send(message: string): Observable<void>;
}

// Production implementation
@Injectable()
class EmailNotificationChannel extends NotificationChannel {
  send(msg: string) { return this.http.post('/api/email', { msg }); }
}

// Test implementation
@Injectable()
class MockNotificationChannel extends NotificationChannel {
  send(msg: string) { console.log('MOCK:', msg); return of(void 0); }
}

// Swap via provider
providers: [
  { provide: NotificationChannel, useClass: EmailNotificationChannel }
  // In tests: { provide: NotificationChannel, useClass: MockNotificationChannel }
]
```

**Real-World Scenario:**
In a multi-tenant SaaS app, each tenant had a different storage backend (S3 vs Azure Blob). We defined a `StorageService` abstract class and provided different implementations based on tenant config using `useFactory`:
```typescript
{
  provide: StorageService,
  useFactory: (config: ConfigService) => {
    return config.cloudProvider === 'aws'
      ? new S3StorageService(config)
      : new AzureBlobStorageService(config);
  },
  deps: [ConfigService]
}
```

**Closing:**
DI is Angular's backbone — everything pluggable, testable, and configurable. Use `providedIn: 'root'` for most services, component-level providers for scoped instances, and `InjectionToken` for configuration values.

---

## Q14. What is the difference between providedIn: 'root' and providers array?

**Summary:**
`providedIn: 'root'` creates a singleton instance available app-wide and is tree-shakable. The `providers` array in a component or module creates a new instance scoped to that context and is NOT tree-shakable. My default is always `providedIn: 'root'` unless I explicitly need scoping.

**Explanation:**

```
providedIn: 'root':
┌────────────────────────────────────┐
│         Root Injector               │
│  AuthService (ONE instance)         │──▶ shared by ALL components
│  ProductService (ONE instance)      │
└────────────────────────────────────┘
→ Tree-shakable: if no component injects it, it's removed from bundle

Component providers: [LoggerService]:
┌──────────────────┐  ┌──────────────────┐
│  ComponentA       │  │  ComponentB       │
│  LoggerService #1 │  │  LoggerService #2 │  ← Different instances!
└──────────────────┘  └──────────────────┘
→ NOT tree-shakable: always included in bundle
```

**Detailed Comparison:**

| Feature | `providedIn: 'root'` | `providers: []` (component) |
|---------|---------------------|---------------------------|
| Scope | App-wide singleton | New instance per component |
| Tree-shaking | Yes | No |
| Instance count | 1 | 1 per component instance |
| Registration | In the service file | In the component/module |
| Lazy route | Still singleton | Scoped if in lazy route providers |
| Use case | Shared services (Auth, API) | Scoped state (form, modal) |

**Practical Example — When to Use Each:**
```typescript
// ✅ providedIn: 'root' — shared, singleton
@Injectable({ providedIn: 'root' })
export class AuthService {
  // One instance for entire app — user is logged in everywhere
}

@Injectable({ providedIn: 'root' })
export class HttpCacheService {
  // One cache shared by all services
}

// ✅ Component-level providers — scoped, isolated
@Component({
  selector: 'app-modal',
  providers: [ModalStateService]  // Each modal gets its own state
})
export class ModalComponent {
  // Opening 3 modals = 3 separate ModalStateService instances
}

// ✅ Component-level — form service per form instance
@Component({
  selector: 'app-user-form',
  providers: [FormValidationService]  // Scoped to this form
})
export class UserFormComponent { }
```

**Tree-Shaking Difference:**
```typescript
// providedIn: 'root' — if NO component ever injects UserAnalyticsService,
// it's completely removed from the production bundle
@Injectable({ providedIn: 'root' })
export class UserAnalyticsService { }

// providers array — ALWAYS included in the module's bundle,
// even if no component uses it
@NgModule({
  providers: [UserAnalyticsService]  // bundled regardless of usage
})
export class AppModule { }
```

**Lazy Route Scoping:**
```typescript
// Service scoped to a lazy-loaded route — destroyed when navigating away
{
  path: 'admin',
  loadChildren: () => import('./admin/admin.routes'),
  providers: [
    AdminDashboardService  // Only exists while /admin is active
  ]
}
```

**Real-World Scenario:**
In a multi-tab dashboard, each tab was a component showing different data with its own filter/sort state. Using `providers: [TabStateService]` at the component level gave each tab its own isolated state. When a tab was closed and reopened, it started fresh. If we'd used `providedIn: 'root'`, all tabs would share one state — changing a filter in Tab 1 would affect Tab 2.

**Closing:**
Default to `providedIn: 'root'`. Switch to component-level `providers` only when you need instance isolation. This decision is about singleton vs scoped lifecycle — make it consciously.

---

## Q15. What is Angular routing and how does the router work?

**Summary:**
Angular Router maps URL paths to components, enabling single-page navigation without full page reloads. It matches the URL against a route configuration, instantiates the component, and renders it inside `<router-outlet>`. It also supports lazy loading, guards, resolvers, and nested routes.

**Explanation — How the Router Works Internally:**

```
User clicks link or types URL: /products/42
    │
    ▼
Router parses URL → matches against route config
    │
    path: 'products/:id' ✅ match found
    │
    ▼
Run Guards (canMatch → canActivate)
    │
    ✅ All passed
    │
    ▼
Run Resolvers (pre-fetch data)
    │
    ▼
Instantiate ProductDetailComponent
    │
    ▼
Render inside <router-outlet></router-outlet>
    │
    ▼
Update browser URL bar (no full page reload)
```

**Route Configuration:**
```typescript
export const routes: Routes = [
  // Basic route
  { path: '', component: HomeComponent },

  // Route with parameter
  { path: 'products/:id', component: ProductDetailComponent },

  // Lazy loaded route
  {
    path: 'admin',
    loadChildren: () => import('./admin/admin.routes').then(m => m.ADMIN_ROUTES),
    canActivate: [authGuard]
  },

  // Nested routes (child routes)
  {
    path: 'dashboard',
    component: DashboardLayoutComponent,
    children: [
      { path: '', component: DashboardHomeComponent },
      { path: 'analytics', component: AnalyticsComponent },
      { path: 'reports', component: ReportsComponent }
    ]
  },

  // Redirect
  { path: 'home', redirectTo: '', pathMatch: 'full' },

  // Wildcard — must be LAST
  { path: '**', component: NotFoundComponent }
];
```

**Navigation Methods:**
```typescript
// Template — routerLink directive
<a routerLink="/products" routerLinkActive="active">Products</a>
<a [routerLink]="['/products', product.id]">{{ product.name }}</a>

// Programmatic — Router service
constructor(private router: Router) {}

goToProduct(id: number) {
  this.router.navigate(['/products', id]);
}

goToSearch(query: string) {
  this.router.navigate(['/search'], {
    queryParams: { q: query, page: 1 }
  });
  // URL: /search?q=angular&page=1
}
```

**Accessing Route Data:**
```typescript
@Component({...})
export class ProductDetailComponent {
  private route = inject(ActivatedRoute);

  // Route params: /products/:id
  productId = this.route.snapshot.paramMap.get('id');

  // Or as Observable (reacts to URL changes without component recreation)
  productId$ = this.route.paramMap.pipe(
    map(params => params.get('id')!)
  );

  // Query params: /search?q=angular
  query$ = this.route.queryParamMap.pipe(
    map(params => params.get('q'))
  );
}
```

**Nested Routes with Multiple Outlets:**
```html
<!-- dashboard-layout.component.html -->
<nav>
  <a routerLink="analytics">Analytics</a>
  <a routerLink="reports">Reports</a>
</nav>

<router-outlet></router-outlet>  <!-- Child components render here -->
```

**Real-World Scenario:**
In an e-commerce app, the routing structure was: `/products` (list with filters as query params), `/products/:id` (detail), `/cart` (lazy loaded), `/checkout` (lazy loaded + auth guard), `/admin` (lazy loaded + admin role guard). Each feature was a lazy-loaded route with its own child routing.

**Closing:**
Angular Router is the navigation backbone. Master route params, lazy loading, guards, and nested routes — these cover 95% of routing needs in production applications.

---

## Q16. What are Route Guards and when would you use them?

**Summary:**
Route Guards are functions that run before or after navigation to control access. They can allow, deny, or redirect navigation. I use them for authentication, authorization, unsaved changes warnings, and data pre-fetching.

**Explanation — Guard Types:**

| Guard | When It Runs | Returns | Use Case |
|-------|-------------|---------|----------|
| `canActivate` | Before navigating TO a route | `boolean \| UrlTree` | Auth/role check |
| `canActivateChild` | Before activating child routes | `boolean \| UrlTree` | Guard all children at once |
| `canDeactivate` | Before navigating AWAY | `boolean` | Unsaved changes prompt |
| `canMatch` | During route matching | `boolean` | Role-based route selection |
| `resolve` | After guards, before render | Data Observable | Pre-fetch data |
| `canLoad` | Before lazy module loads | `boolean` (deprecated, use canMatch) | — |

**Functional Guards (Modern Angular 15+):**

```typescript
// Auth Guard — redirect to login if not authenticated
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isLoggedIn()) return true;

  // Return UrlTree for redirect — better than router.navigate()
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url }
  });
};

// Role Guard — check specific role
export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const requiredRole = route.data['role'] as string;

  if (auth.hasRole(requiredRole)) return true;

  return inject(Router).createUrlTree(['/unauthorized']);
};

// Route config
{
  path: 'admin',
  loadComponent: () => import('./admin.component'),
  canActivate: [authGuard, roleGuard],
  data: { role: 'ADMIN' }
}
```

**Unsaved Changes Guard:**
```typescript
export const unsavedChangesGuard: CanDeactivateFn<{ hasUnsavedChanges(): boolean }> =
  (component) => {
    if (component.hasUnsavedChanges()) {
      return confirm('You have unsaved changes. Discard them?');
    }
    return true;
  };

// Component
@Component({...})
export class EditProfileComponent {
  form = inject(FormBuilder).group({ name: [''], email: [''] });

  hasUnsavedChanges(): boolean {
    return this.form.dirty;
  }
}
```

**Guard Execution Flow:**
```
Navigation: /admin/settings
    │
    ├── canMatch    → Is this route even visible to this user?
    ├── canActivate → Can user access /admin?
    ├── canActivateChild → Can user access /settings under /admin?
    ├── resolve     → Fetch settings data
    └── Component renders ✅

Navigation away:
    └── canDeactivate → Any unsaved changes?
```

**Real-World Scenario:**
In a banking app, we had layered guards: `authGuard` on all routes (must be logged in), `roleGuard` on admin routes (must be ADMIN), `mfaGuard` on transaction routes (must have completed MFA), and `unsavedChangesGuard` on all form pages. Guards composed naturally — each had a single responsibility.

**Closing:**
Functional guards in Angular 15+ are clean, composable, and testable. Use `canActivate` for auth, `canMatch` for role-based routing, `canDeactivate` for form protection. Return `UrlTree` for redirects — never `router.navigate()` inside guards.

---

## Q17. What is the difference between CanActivate, CanDeactivate, and Resolve guards?

**Summary:**
`CanActivate` asks "can the user enter this route?" before loading the component. `CanDeactivate` asks "can the user leave this route?" before navigating away. `Resolve` pre-fetches data after guards pass but before the component renders. They solve three different problems: access control, data loss prevention, and data availability.

**Explanation:**

```
Timeline of a navigation event:

  [User clicks link to /orders/42]
       │
       ▼
  canActivate: Is the user authenticated? Has the right role?
       │ ✅ Yes
       ▼
  resolve: Fetch Order #42 from API
       │ ✅ Data loaded
       ▼
  Component renders with data immediately available
       │
  [User edits the order, clicks a different link]
       │
       ▼
  canDeactivate: Are there unsaved changes? Confirm with user.
       │ ✅ User confirms
       ▼
  Navigate away
```

**Side-by-Side Code:**

```typescript
// CanActivate — "Can you come in?"
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isLoggedIn() || inject(Router).createUrlTree(['/login']);
};

// CanDeactivate — "Can you leave?"
export const saveGuard: CanDeactivateFn<{ isDirty: boolean }> = (component) => {
  return !component.isDirty || confirm('Unsaved changes will be lost. Continue?');
};

// Resolve — "Let me get the data ready for you"
export const orderResolver: ResolveFn<Order> = (route) => {
  const id = route.paramMap.get('id')!;
  return inject(OrderService).getById(id).pipe(
    catchError(() => {
      inject(Router).navigate(['/orders']);
      return EMPTY;
    })
  );
};

// Route config — all three together
{
  path: 'orders/:id/edit',
  component: OrderEditComponent,
  canActivate: [authGuard],           // Step 1: Check access
  resolve: { order: orderResolver },  // Step 2: Fetch data
  canDeactivate: [saveGuard]          // Step 3 (on leave): Check unsaved
}

// Component
@Component({...})
export class OrderEditComponent {
  private route = inject(ActivatedRoute);
  order: Order = this.route.snapshot.data['order'];
  isDirty = false;
}
```

**Comparison Table:**

| Feature | CanActivate | CanDeactivate | Resolve |
|---------|------------|---------------|---------|
| Runs when | Entering a route | Leaving a route | After guards, before render |
| Purpose | Access control | Data loss prevention | Data pre-fetching |
| Has access to | Route, state | Component instance | Route params |
| Returns | `boolean \| UrlTree` | `boolean` | `Observable<T>` |
| Common use | Auth, roles, permissions | Unsaved form warning | Load entity by ID |

**When NOT to Use Resolve:**
```
// ❌ Resolver blocks navigation until data loads — blank screen!
// For slow APIs, use the component with a loading state instead:

@Component({
  template: `
    @if (order(); as order) {
      <app-order-form [order]="order" />
    } @else {
      <app-loading-spinner />
    }
  `
})
export class OrderEditComponent {
  private route = inject(ActivatedRoute);
  private orderService = inject(OrderService);

  order = toSignal(
    this.route.paramMap.pipe(
      switchMap(params => this.orderService.getById(params.get('id')!))
    )
  );
}
// ✅ Navigation is instant, loading spinner shows while data loads
```

**Closing:**
`CanActivate` = bouncer at the door. `Resolve` = waiter who brings your food before you sit down. `CanDeactivate` = "are you sure you want to leave?" popup. Use all three together for a complete route protection strategy, but prefer component-level loading over Resolvers for better UX.

---

## Q18. What is Angular Universal (SSR) and why is it used?

**Summary:**
Angular Universal renders Angular applications on the server, sending fully rendered HTML to the browser. This dramatically improves First Contentful Paint (FCP), makes the app SEO-crawlable, and provides better social media link previews. In Angular 17+, SSR is a first-class feature with non-destructive hydration.

**Explanation — How SSR Works:**

```
WITHOUT SSR (Client-Side Rendering):
Browser request → Server sends empty HTML + JS bundle
    → Browser downloads JS (2MB)
    → Browser executes JS
    → Angular renders the page
    → User sees content (3-5 seconds later)
    → Search engine sees: <app-root></app-root> (empty!)

WITH SSR (Server-Side Rendering):
Browser request → Node.js server runs Angular
    → Server renders full HTML
    → Sends complete HTML to browser
    → User sees content IMMEDIATELY (< 1 second)
    → JS bundle loads in background
    → Angular "hydrates" — attaches event listeners to existing DOM
    → App becomes interactive
    → Search engine sees: Full HTML content ✅
```

**Setting Up SSR in Angular 17+:**
```bash
# New project with SSR
ng new my-app --ssr

# Add SSR to existing project
ng add @angular/ssr
```

```typescript
// app.config.server.ts
import { provideServerRendering } from '@angular/platform-server';
import { provideClientHydration } from '@angular/platform-browser';

export const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(),
    provideClientHydration()  // Non-destructive hydration
  ]
};
```

**Handling Browser-Only APIs:**
```typescript
@Component({...})
export class AnalyticsComponent {
  private platformId = inject(PLATFORM_ID);

  ngOnInit() {
    // localStorage, window, document don't exist on server
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('visited', 'true');
      window.scrollTo(0, 0);
    }
  }
}

// OR use afterNextRender (Angular 17+) — runs only in browser after hydration
export class ChartComponent {
  constructor() {
    afterNextRender(() => {
      // Safe to use DOM APIs here
      this.initD3Chart(document.getElementById('chart'));
    });
  }
}
```

**TransferState — Avoid Duplicate HTTP Calls:**
```typescript
// WITHOUT TransferState:
// Server makes HTTP call → renders HTML → sends to browser
// Browser hydrates → makes SAME HTTP call again! (duplicate)

// WITH TransferState (built into Angular HttpClient with SSR):
// Server makes HTTP call → stores result in TransferState
// Browser checks TransferState first → uses cached data (no duplicate call!)

// Angular 17+ does this automatically with provideClientHydration()
```

**When SSR Is Essential:**

| Scenario | SSR Needed? | Why |
|----------|:-----------:|-----|
| E-commerce product pages | Yes | SEO, social sharing, fast LCP |
| Marketing/landing pages | Yes | SEO, performance |
| Admin dashboard | No | Not public, no SEO need |
| Internal tool | No | Authenticated users, no crawling |
| Blog / CMS | Yes | SEO critical |

**Real-World Scenario:**
An e-commerce client's product pages weren't appearing in Google search results because the crawler saw only `<app-root></app-root>`. After implementing SSR, all product pages were indexed within 2 weeks. LCP improved from 3.8s to 1.2s, and organic traffic increased by 60%.

**Closing:**
SSR is essential for public-facing, SEO-dependent applications. Angular 17+ makes it seamless with non-destructive hydration and automatic TransferState. For internal/admin apps, it's usually unnecessary overhead.

---

## Q19. What is Ahead-of-Time (AOT) compilation and how is it different from JIT compilation?

**Summary:**
AOT compiles Angular templates into optimized JavaScript at build time. JIT compiles templates in the browser at runtime. AOT is the default for production — it produces smaller bundles, catches template errors at build time, and renders faster because there's no compilation step in the browser.

**Explanation:**

```
JIT (Just-In-Time) — Development:
┌─────────────────────────────────────────────┐
│ Source Code (TS + HTML templates)            │
│      │                                      │
│      ▼  Ship to browser                     │
│ Browser downloads:                          │
│   - Application code                        │
│   - Angular compiler (~1MB!)                │
│   - Template HTML files                     │
│      │                                      │
│      ▼  Compile at runtime                  │
│ Templates → JavaScript (in browser)         │
│      │                                      │
│      ▼  Render                              │
│ User sees the page                          │
│                                             │
│ ❌ Larger bundle (compiler included)         │
│ ❌ Slower startup (compilation in browser)   │
│ ❌ Template errors found at RUNTIME          │
└─────────────────────────────────────────────┘

AOT (Ahead-of-Time) — Production:
┌─────────────────────────────────────────────┐
│ Source Code (TS + HTML templates)            │
│      │                                      │
│      ▼  ng build (compile at BUILD time)    │
│ Angular compiler runs:                      │
│   - Templates → optimized JavaScript        │
│   - Type-checks all template expressions    │
│   - Tree-shakes unused code                 │
│      │                                      │
│      ▼  Ship to browser                     │
│ Browser downloads:                          │
│   - Only application runtime code           │
│   - NO compiler needed                      │
│      │                                      │
│      ▼  Render immediately                  │
│ User sees the page (FAST)                   │
│                                             │
│ ✅ Smaller bundle (no compiler)              │
│ ✅ Faster startup (pre-compiled)             │
│ ✅ Template errors caught at BUILD time      │
│ ✅ Better security (no eval/dynamic compile) │
└─────────────────────────────────────────────┘
```

**What AOT Catches at Build Time:**
```typescript
@Component({
  template: `
    <p>{{ user.nmae }}</p>
    <!-- AOT Error: Property 'nmae' does not exist on type 'User'.
         Did you mean 'name'? -->

    <p>{{ getTotal() + 'items' }}</p>
    <!-- AOT Warning: Operator '+' cannot be applied to types 'number' and 'string' -->

    <div *ngIf="isLoading">
    <!-- AOT Error: Missing closing </div> -->
  `
})
```

**Real-World Impact:**

| Metric | JIT | AOT | Improvement |
|--------|-----|-----|:-----------:|
| Bundle size | 4.2 MB | 1.1 MB | 74% smaller |
| First render | 3.2s | 1.1s | 65% faster |
| Template errors found | At runtime | At build | Safer deploys |

**Commands:**
```bash
# AOT (default for production)
ng build              # AOT is default since Angular 9
ng build --configuration production

# JIT (only for development, rarely used now)
ng serve              # Uses JIT by default for faster rebuilds
ng serve --aot        # Force AOT in development
```

**Strict Template Type-Checking (Maximize AOT Benefits):**
```json
// tsconfig.json
{
  "angularCompilerOptions": {
    "strictTemplates": true,          // Full template type checking
    "strictInjectionParameters": true, // DI parameter types
    "strictInputAccessModifiers": true // @Input access levels
  }
}
```

**Closing:**
AOT is non-negotiable for production. It's Angular's default since v9, and with `strictTemplates`, it turns your HTML templates into type-safe code. I always develop with strict mode enabled to catch issues early.

---

## Q20. How do you optimize performance in a large Angular application?

**Summary:**
Performance optimization in Angular is a multi-layer strategy: reduce bundle size (lazy loading, tree-shaking), minimize change detection (OnPush, Signals), optimize rendering (trackBy, virtual scrolling, `@defer`), and improve perceived performance (SSR, preloading, caching). I approach it systematically — measure first, then optimize the biggest bottleneck.

**The Performance Optimization Checklist:**

**1. Bundle Size — Ship Less JavaScript:**
```
□ Lazy load all feature routes
    → loadComponent() / loadChildren()
    → Reduces initial bundle by 50-70%

□ Use standalone components
    → Tree-shakes unused components from shared modules

□ Analyze bundle
    → ng build --stats-json && npx webpack-bundle-analyzer dist/stats.json
    → Identify large third-party libraries

□ Replace heavy libraries
    → moment.js (300KB) → date-fns (tree-shakable, ~10KB per function)
    → lodash (70KB) → native JS or lodash-es (tree-shakable)

□ Use @defer for below-the-fold content
    → Template-level lazy loading
```

**2. Change Detection — Check Less Often:**
```typescript
// ✅ OnPush on every component
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush
})

// ✅ Signals for component state (Angular 17+)
count = signal(0);
items = computed(() => this.allItems().filter(i => i.active));

// ✅ async pipe instead of manual subscribe
{{ data$ | async }}

// ❌ AVOID: Functions in templates (run on every CD cycle)
<p>{{ calculateTotal() }}</p>
// ✅ USE: Pipes or computed signals
<p>{{ items | totalPipe }}</p>
<p>{{ total() }}</p>
```

**3. Rendering — Render Less DOM:**
```html
<!-- ✅ trackBy / track — reuse DOM elements -->
@for (item of items(); track item.id) {
  <app-item-card [item]="item" />
}

<!-- ✅ Virtual Scrolling for 500+ items -->
<cdk-virtual-scroll-viewport itemSize="50">
  <div *cdkVirtualFor="let item of items">{{ item.name }}</div>
</cdk-virtual-scroll-viewport>

<!-- ✅ @defer — lazy render sections -->
@defer (on viewport) {
  <app-heavy-chart [data]="chartData" />
} @placeholder {
  <div class="skeleton"></div>
}
```

**4. Network — Fetch Less, Cache More:**
```typescript
// ✅ HTTP caching with shareReplay
getProducts(): Observable<Product[]> {
  if (!this.products$) {
    this.products$ = this.http.get<Product[]>('/api/products').pipe(
      shareReplay(1)  // Cache the result, share across subscribers
    );
  }
  return this.products$;
}

// ✅ Preloading strategy for lazy routes
provideRouter(routes, withPreloading(PreloadAllModules))

// ✅ Service Worker for offline caching
ng add @angular/pwa
```

**5. Images & Assets:**
```html
<!-- ✅ NgOptimizedImage — automatic lazy loading, srcset, priority -->
<img ngSrc="product.jpg"
     width="400"
     height="300"
     priority          <!-- for above-the-fold images -->
     placeholder="blur" />

<!-- Automatically generates: -->
<!-- srcset with responsive sizes -->
<!-- loading="lazy" for off-screen images -->
<!-- fetchpriority="high" for priority images -->
```

**6. Production Build Optimizations:**
```bash
# Angular CLI handles most optimizations automatically:
ng build --configuration production

# This enables:
# ✅ AOT compilation
# ✅ Tree-shaking (removes dead code)
# ✅ Minification (terser)
# ✅ Bundle splitting (vendor, main, lazy chunks)
# ✅ Differential loading (modern + legacy bundles)
# ✅ CSS purging and minification
# ✅ Source map removal
```

**7. Runtime Performance Profiling:**
```typescript
// Chrome DevTools → Performance tab → Record user interaction
// Look for:
// - Long tasks (> 50ms) → optimize or defer
// - Layout thrashing → batch DOM reads/writes
// - Excessive change detection → switch to OnPush

// Angular DevTools (Chrome Extension)
// → Profiler tab → shows CD cycles per component
// → Component tree → shows which components re-rendered and why
```

**Performance Budget (angular.json):**
```json
{
  "budgets": [
    {
      "type": "initial",
      "maximumWarning": "500kb",
      "maximumError": "1mb"
    },
    {
      "type": "anyComponentStyle",
      "maximumWarning": "2kb",
      "maximumError": "4kb"
    }
  ]
}
```

**Real-World Impact — Before & After:**

| Optimization | Before | After |
|-------------|--------|-------|
| Lazy loading | 4.5MB initial | 800KB initial |
| OnPush + Signals | 200 CD checks/event | 5-10 CD checks/event |
| Virtual scrolling (10K rows) | 8s render | Instant |
| @defer (product page) | LCP 3.2s | LCP 1.4s |
| NgOptimizedImage | LCP 2.8s | LCP 1.6s |
| SSR + Hydration | FCP 3.5s | FCP 0.8s |

**Closing:**
Performance optimization is not a one-time task — it's a discipline. I set performance budgets in `angular.json`, profile with Angular DevTools, and follow the checklist above for every major feature. The biggest wins come from lazy loading, OnPush, and `@defer` — in that order.

---

## Quick Reference Summary

| # | Topic | Key Takeaway | Modern Angular |
|---|-------|-------------|----------------|
| 1 | Architecture | Component-based, DI-driven | Standalone components |
| 2 | Components vs Directives | Components have templates, directives don't | Same |
| 3 | Directive Types | Structural, Attribute, Component | `@if`/`@for` control flow |
| 4 | Change Detection | Zone.js triggers top-down CD | Signals = fine-grained CD |
| 5 | Default vs OnPush | OnPush checks only on input ref change | OnPush + Signals = best |
| 6 | Observables | Lazy, cancellable, multi-value streams | `toSignal()` bridge |
| 7 | Observable vs Promise vs Subject | Promise=single, Observable=stream, Subject=multicast | Same |
| 8 | Subject Types | Behavior (latest), Replay (N), Subject (none) | Same |
| 9 | Lazy Loading | Defer module loading to navigation time | `loadComponent()` |
| 10 | Lifecycle Hooks | ngOnInit for setup, ngOnDestroy for cleanup | `effect()`, `DestroyRef` |
| 11 | ngOnInit vs Constructor | Constructor=DI, ngOnInit=initialization | `inject()` + signals |
| 12 | Forms | Reactive > Template-driven for production | Same |
| 13 | Dependency Injection | Hierarchical injector tree | `inject()` function |
| 14 | providedIn vs providers | root=singleton+tree-shake, providers=scoped | Same |
| 15 | Routing | URL → Guard → Resolve → Component | Functional guards |
| 16 | Route Guards | Control access, prevent data loss | `CanActivateFn` |
| 17 | Guard Types | CanActivate/CanDeactivate/Resolve | `CanMatchFn` added |
| 18 | SSR (Universal) | Server-render for SEO + performance | Non-destructive hydration |
| 19 | AOT vs JIT | AOT=build-time, smaller, faster, safer | `strictTemplates` |
| 20 | Performance | Lazy load, OnPush, @defer, virtual scroll | Signals + @defer |
