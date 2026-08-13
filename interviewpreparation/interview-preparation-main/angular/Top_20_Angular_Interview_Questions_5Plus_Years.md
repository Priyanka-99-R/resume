# Top 20 Angular Interview Questions (5+ Years Experience)

---

## 1. What is the architecture of Angular applications?

**Summary:** Angular follows a component-based architecture built on modules, components, services, and dependency injection — it's essentially an opinionated MVC framework for building scalable SPAs.

**Explanation:** At its core, Angular apps are a tree of components. Each component has a template (HTML), a class (TypeScript), and styles (CSS). Components are organized into modules (NgModules) or now standalone components. Services handle business logic and API calls, injected via Angular's DI system. The router manages navigation, and RxJS handles async data flows.

**The key layers are:**
- **Components** — UI building blocks
- **Services** — shared business logic, API communication
- **Modules/Standalone** — organizational boundaries
- **Router** — navigation and lazy loading
- **Directives/Pipes** — DOM manipulation and data transformation

**Real-world scenario:** In an e-commerce app, you'd have a `ProductModule` with components like `ProductListComponent`, `ProductDetailComponent`, a `ProductService` for API calls, and pipes for formatting prices.

**Common mistake:** Putting business logic inside components instead of services. This kills reusability and testability.

> Angular's opinionated architecture is actually its strength — it enforces consistency across large teams, which is why enterprises prefer it.

---

## 2. Difference between Modules vs Standalone Components?

**Summary:** NgModules were Angular's original way to organize code into cohesive blocks. Standalone components, introduced in Angular 14+, eliminate the need for modules by making components self-contained and independently importable.

**Explanation:**
- **NgModules** — You declare components, import dependencies, and export what other modules need. Every component *must* belong to exactly one module.
- **Standalone Components** — You set `standalone: true` and import dependencies directly in the component's `imports` array. No module needed.

```typescript
// Module approach
@NgModule({
  declarations: [UserListComponent],
  imports: [CommonModule, HttpClientModule],
  exports: [UserListComponent]
})
export class UserModule {}

// Standalone approach
@Component({
  standalone: true,
  imports: [CommonModule, HttpClient],
  selector: 'app-user-list',
  templateUrl: './user-list.component.html'
})
export class UserListComponent {}
```

**When used:** New Angular projects (v17+) default to standalone. Legacy projects still use modules. Migration is incremental — you can mix both.

**Common mistake:** Thinking you must migrate everything at once. Angular supports hybrid — standalone components can be imported into modules and vice versa.

> Standalone is the future of Angular. It simplifies the mental model and reduces boilerplate, but modules aren't going anywhere for existing enterprise codebases.

---

## 3. What are Signals and how do they differ from RxJS?

**Summary:** Signals are Angular's new reactive primitive (v16+) for synchronous state management. Unlike RxJS Observables, they're simpler, synchronous by default, and tightly integrated with Angular's change detection.

**Explanation:**
- **Signals** — hold a value, notify consumers when it changes. You read with `signal()`, update with `.set()`, `.update()`, `.mutate()`. Computed signals derive from other signals automatically.
- **RxJS** — stream-based, asynchronous, powerful operators for complex async flows (debounce, switchMap, merge).

```typescript
// Signal
count = signal(0);
doubled = computed(() => this.count() * 2);
increment() { this.count.update(v => v + 1); }

// RxJS
count$ = new BehaviorSubject(0);
doubled$ = this.count$.pipe(map(v => v * 2));
increment() { this.count$.next(this.count$.value + 1); }
```

**When to use what:**
- **Signals** — component state, simple derived state, template bindings
- **RxJS** — HTTP calls, WebSockets, complex async orchestration, debouncing user input

**Common mistake:** Trying to replace all RxJS with Signals. They complement each other. HTTP calls and complex async workflows still need RxJS.

> Signals simplify 80% of reactivity needs in Angular. Use them for state, keep RxJS for streams and async orchestration.

---

## 4. What is Zone.js and why Angular is moving to zoneless architecture?

**Summary:** Zone.js is a library that monkey-patches all async APIs (setTimeout, Promises, DOM events) to automatically trigger Angular's change detection. Angular is moving away from it because it adds overhead and makes change detection unpredictable.

**Explanation:** Zone.js wraps every async operation. When any async task completes, Angular runs change detection on the entire component tree. This "magic" is convenient but expensive — even a `setTimeout` in some unrelated service triggers a full CD cycle.

**Why zoneless:**
- **Performance** — No unnecessary CD cycles. Only components that actually changed get checked.
- **Bundle size** — Zone.js adds ~100KB to the bundle.
- **Predictability** — Developers control when CD runs via Signals.
- **SSR/Hybrid rendering** — Easier without Zone.js patching.

```typescript
// Angular 18+ zoneless bootstrap
bootstrapApplication(AppComponent, {
  providers: [provideZonelessChangeDetection()]
});
```

**Real-world impact:** In a dashboard with 50+ components and WebSocket updates, Zone.js would trigger CD on every single message across the entire tree. Zoneless + Signals means only the affected components re-render.

> Zoneless is Angular's path to performance parity with React and Vue. Signals make it possible — they tell Angular exactly what changed, eliminating the need for Zone.js guesswork.

---

## 5. How does Angular change detection work?

**Summary:** Angular's change detection walks the component tree top-down, comparing current values to previous values in each component's template bindings, and updates the DOM where differences are found.

**Explanation:** By default, Angular uses the **Default** strategy — when any event occurs (click, HTTP response, timer), it checks *every* component from root to leaves. Each component's template bindings are compared using simple reference checks (or `===` for primitives).

**Two strategies:**
- **Default** — checks the entire subtree every cycle
- **OnPush** — only checks a component when its `@Input` references change, an event originates from the component, or an Observable with `async` pipe emits

**The flow:**
1. Event triggers (user click, HTTP response, timer)
2. Zone.js notifies Angular
3. Angular starts at the root component
4. Walks down the tree, checking each component's bindings
5. Updates DOM where values differ

**Common mistake:** Mutating objects/arrays instead of creating new references when using OnPush. The component won't detect the change because the reference hasn't changed.

```typescript
// WRONG with OnPush
this.items.push(newItem); // same reference, no CD triggered

// RIGHT with OnPush
this.items = [...this.items, newItem]; // new reference, CD triggered
```

> Understanding change detection is what separates mid-level from senior Angular devs. OnPush should be your default strategy in any production app.

---

## 6. How do you optimize Angular application performance?

**Summary:** Angular performance optimization is a combination of reducing change detection cycles, minimizing bundle size, and optimizing rendering — the biggest wins come from OnPush, lazy loading, and proper RxJS/Signal usage.

**Key strategies:**

1. **OnPush change detection** on all components
2. **Lazy loading** routes and modules
3. **trackBy** in `ngFor` to avoid DOM re-creation
4. **Pure pipes** instead of method calls in templates
5. **Virtual scrolling** (`cdk-virtual-scroll-viewport`) for long lists
6. **Preloading strategies** for likely-needed modules
7. **Web Workers** for heavy computation
8. **SSR/SSG** with Angular Universal for initial load
9. **Image optimization** with `NgOptimizedImage`
10. **Signals** over Observables for synchronous state

**Real-world scenario:** We had a dashboard rendering 10,000 rows. After adding virtual scrolling, OnPush, and trackBy, render time dropped from 4 seconds to under 200ms. Lazy loading reduced initial bundle from 2MB to 400KB.

**Common mistake:** Over-subscribing to Observables without unsubscribing — causing memory leaks and phantom CD cycles. Use `takeUntilDestroyed()` or the `async` pipe.

> Performance optimization isn't a one-time thing. Profile with Angular DevTools and Chrome Performance tab — optimize what the data tells you, not what you assume.

---

## 7. What is OnPush change detection strategy?

**Summary:** OnPush tells Angular to skip change detection for a component unless one of three things happens: an `@Input` reference changes, an event originates from the component or its children, or you manually trigger it.

**Explanation:**

```typescript
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  // ...
})
export class UserCardComponent {
  @Input() user!: User;
}
```

**CD triggers with OnPush:**
1. `@Input()` reference changes (not mutation!)
2. DOM event from within the component (click, keyup)
3. Observable emits via `async` pipe
4. Manual `markForCheck()` or `detectChanges()`
5. Signal value changes (Angular 16+)

**Why use it:** In Default strategy, a component with 100 children means 100 checks per cycle. OnPush can skip entire subtrees that haven't changed.

**Real-world scenario:** A chat app with 200 message components. With Default, every new message triggers CD on all 200. With OnPush, only the new message component gets checked.

**Common mistake:** Using `detectChanges()` everywhere as a bandaid when OnPush "breaks" something. Usually the real problem is mutating objects instead of creating new references.

> OnPush should be your default in every production Angular app. It's the single biggest performance lever you have.

---

## 8. What is trackBy in ngFor and why is it important?

**Summary:** `trackBy` gives Angular a way to identify which items in a list have changed, been added, or removed — so it can reuse existing DOM elements instead of destroying and recreating the entire list.

**Explanation:** Without `trackBy`, Angular uses object identity. If you fetch a new array from an API (even with the same data), every object has a new reference, so Angular destroys and recreates every DOM element.

```typescript
// Template
<div *ngFor="let user of users; trackBy: trackByUserId">
  {{ user.name }}
</div>

// Component
trackByUserId(index: number, user: User): number {
  return user.id;
}
```

**When it matters:** Any list that re-renders — API refreshes, WebSocket updates, filtering, sorting. Without trackBy, a list of 500 items gets 500 DOM deletions and 500 DOM creations on every update.

**Real-world scenario:** A live stock ticker showing 200 symbols, updating every second. Without trackBy, the entire table flickers and performance tanks. With trackBy on the stock symbol, only changed prices update.

**Common mistake:** Using `index` as the trackBy value. This defeats the purpose — if items are reordered, inserted, or deleted, the index changes and Angular recreates elements anyway.

> It's a small addition with huge impact. There's no reason *not* to use trackBy on every `ngFor` in production code.

---

## 9. Difference between pure pipe vs impure pipe?

**Summary:** A pure pipe only runs when its input value reference changes. An impure pipe runs on *every* change detection cycle. Pure is default and performant; impure is rarely needed.

**Explanation:**

```typescript
// Pure (default) — runs only when 'value' reference changes
@Pipe({ name: 'formatCurrency', pure: true })
export class FormatCurrencyPipe implements PipeTransform {
  transform(value: number): string {
    return `$${value.toFixed(2)}`;
  }
}

// Impure — runs every CD cycle
@Pipe({ name: 'filterActive', pure: false })
export class FilterActivePipe implements PipeTransform {
  transform(items: any[]): any[] {
    return items.filter(i => i.active);
  }
}
```

**Key difference:**
- **Pure pipe** — Angular caches the result. Same input = same output, no re-execution. Only primitive value changes or reference changes trigger it.
- **Impure pipe** — No caching. Runs every CD cycle. Necessary when the pipe depends on external state or when the input is mutated rather than replaced.

**When to use impure:** Almost never. If you need an impure pipe, it's usually a sign you should restructure your data flow. The exception is when you genuinely need to react to internal array mutations.

**Common mistake:** Making a filtering pipe impure because "it doesn't update when I push to the array." The real fix is creating a new array reference, not making the pipe impure.

> If you're reaching for `pure: false`, stop and think about whether you should just use immutable data patterns instead. 99% of the time, pure pipes are the answer.

---

## 10. How do you reduce bundle size?

**Summary:** Bundle size reduction comes down to lazy loading, tree-shaking, proper imports, and build configuration — the goal is shipping only the code the user actually needs for the current view.

**Key strategies:**

1. **Lazy loading routes:**
   ```typescript
   { path: 'admin', loadComponent: () => import('./admin/admin.component') }
   ```

2. **Standalone components** — eliminates module overhead

3. **Tree-shakeable providers:**
   ```typescript
   @Injectable({ providedIn: 'root' }) // tree-shakeable
   ```

4. **Selective imports** — `import { map } from 'rxjs/operators'` not `import * as rxjs`

5. **Remove Zone.js** in zoneless apps (~100KB savings)

6. **Analyze with `source-map-explorer`** or `webpack-bundle-analyzer`

7. **Enable production build optimizations:** `ng build --configuration production` (AOT, minification, dead code elimination)

8. **Use `@defer` blocks** (Angular 17+) for below-the-fold content

9. **Replace heavy libraries** — moment.js -> date-fns or native `Intl`

10. **Compression** — Brotli at the server level

**Real-world scenario:** Replaced moment.js (300KB) with date-fns (tree-shaked to 12KB), lazy loaded 4 feature routes, switched to standalone — total bundle went from 1.8MB to 600KB.

> Always measure before optimizing. Run `ng build --stats-json` and analyze. The biggest offenders are usually third-party libraries, not your code.

---

## 11. Difference between Subject, BehaviorSubject, ReplaySubject in RxJS

**Summary:** All three are multicast Observables that act as both observer and observable. The difference is what happens to late subscribers — Subject gets nothing, BehaviorSubject gets the last value, ReplaySubject gets N previous values.

| Feature | Subject | BehaviorSubject | ReplaySubject |
|---|---|---|---|
| Initial value | No | Yes (required) | No |
| Late subscriber gets | Nothing | Last emitted value | Last N values |
| `.value` property | No | Yes | No |

```typescript
// Subject — late subscriber misses previous emissions
const sub = new Subject<string>();
sub.next('A');
sub.subscribe(v => console.log(v)); // prints nothing

// BehaviorSubject — late subscriber gets current value
const bSub = new BehaviorSubject<string>('initial');
bSub.next('A');
bSub.subscribe(v => console.log(v)); // prints 'A'

// ReplaySubject — late subscriber gets last N values
const rSub = new ReplaySubject<string>(2); // buffer size 2
rSub.next('A');
rSub.next('B');
rSub.next('C');
rSub.subscribe(v => console.log(v)); // prints 'B', 'C'
```

**Real-world usage:**
- **Subject** — event bus, button clicks, one-time notifications
- **BehaviorSubject** — current user state, current theme, any "current value" pattern
- **ReplaySubject** — caching API responses, ensuring late components get recent data

**Common mistake:** Using BehaviorSubject when you don't have a meaningful initial value, leading to `null` checks everywhere. Use ReplaySubject(1) instead — same "get latest" behavior but no forced initial value.

> BehaviorSubject is the workhorse for state management in Angular services. Know when to use each, and you'll write cleaner reactive code.

---

## 12. What are Angular Signals vs Observables?

**Summary:** Signals are synchronous, pull-based reactive primitives for state. Observables are asynchronous, push-based streams for events and async data flows. They solve different problems and coexist.

| Aspect | Signals | Observables (RxJS) |
|---|---|---|
| Nature | Synchronous value holder | Async event stream |
| Reading | `signal()` — always has a value | `.subscribe()` — may or may not emit |
| Subscription | Automatic (template) | Manual (must subscribe/unsubscribe) |
| Operators | `computed()`, `effect()` | 100+ operators (map, filter, switchMap) |
| Memory leaks | No subscription to manage | Must unsubscribe |
| Best for | UI state, derived state | HTTP, WebSockets, complex async |

```typescript
// Signal — simple, synchronous
name = signal('John');
greeting = computed(() => `Hello, ${this.name()}`);

// Observable — async stream
searchResults$ = this.searchTerm$.pipe(
  debounceTime(300),
  distinctUntilChanged(),
  switchMap(term => this.http.get(`/api/search?q=${term}`))
);
```

**Interop:**
```typescript
// Observable to Signal
userSignal = toSignal(this.user$, { initialValue: null });

// Signal to Observable
user$ = toObservable(this.userSignal);
```

**When to use what:**
- Template state, counters, toggles, form state → **Signals**
- HTTP calls, WebSockets, complex event composition → **Observables**

> Don't think of it as Signals *replacing* Observables. Think of Signals handling the simple 80% and RxJS handling the complex 20%. Together, they're more powerful than either alone.

---

## 13. Explain Dependency Injection hierarchy

**Summary:** Angular's DI system is hierarchical — injectors form a tree that mirrors the component tree, and when a component requests a dependency, Angular walks up the tree until it finds a provider.

**The injector hierarchy:**
1. **Platform Injector** — platform-level (rarely used directly)
2. **Root Injector** — `providedIn: 'root'` or `providers` in `bootstrapApplication`
3. **Module Injector** — providers in `@NgModule.providers`
4. **Element Injector** — providers in `@Component.providers` or `@Directive.providers`

```typescript
// Root level — singleton across entire app
@Injectable({ providedIn: 'root' })
export class AuthService {}

// Component level — new instance per component
@Component({
  providers: [LoggerService] // each component gets its own LoggerService
})
export class DashboardComponent {}
```

**Resolution order:** Angular checks the element injector first, walks up through parent component injectors, then falls back to module/root injectors.

**Real-world scenario:** A multi-tenant app where each tab represents a different client. Each tab component provides its own `TenantService` instance via component-level providers. All components inside that tab share the same tenant context, completely isolated from other tabs.

**Common mistake:** Providing a service at both root and component level unintentionally, creating two instances when you expected one. Or using `providedIn: 'root'` for everything when some services should be scoped.

> Understanding the injector hierarchy is critical for managing service scope, especially in large apps. It's one of Angular's most powerful features when used correctly.

---

## 14. What are Angular lifecycle hooks?

**Summary:** Lifecycle hooks are methods Angular calls at specific moments in a component's life — from creation to destruction. The most critical ones are `ngOnInit`, `ngOnChanges`, `ngAfterViewInit`, and `ngOnDestroy`.

**The hooks in order:**
1. `constructor` — DI injection (not technically a hook)
2. `ngOnChanges` — when `@Input` values change (runs before `ngOnInit`)
3. `ngOnInit` — component initialization (run once, after first `ngOnChanges`)
4. `ngDoCheck` — custom change detection logic
5. `ngAfterContentInit` — after `<ng-content>` projected
6. `ngAfterContentChecked` — after projected content checked
7. `ngAfterViewInit` — after view and child views initialized
8. `ngAfterViewChecked` — after view and child views checked
9. `ngOnDestroy` — cleanup before destruction

**The ones you'll actually use daily:**
```typescript
export class UserComponent implements OnInit, OnChanges, OnDestroy {
  @Input() userId!: string;

  ngOnChanges(changes: SimpleChanges) {
    // React to @Input changes — reload data when userId changes
    if (changes['userId']) {
      this.loadUser(this.userId);
    }
  }

  ngOnInit() {
    // Initial setup — API calls, subscriptions
  }

  ngOnDestroy() {
    // Cleanup — unsubscribe, clear timers
  }
}
```

**Common mistakes:**
- Calling APIs in `constructor` instead of `ngOnInit` — constructor is for DI only
- Forgetting `ngOnDestroy` cleanup — memory leaks from subscriptions
- Accessing `ViewChild` in `ngOnInit` instead of `ngAfterViewInit`

> You don't need to memorize all 8 hooks. Know `ngOnInit`, `ngOnChanges`, `ngAfterViewInit`, and `ngOnDestroy` cold — they cover 95% of real-world cases.

---

## 15. How does Angular router lazy loading work?

**Summary:** Lazy loading defers the loading of route modules/components until the user navigates to that route, splitting your app into smaller chunks that load on demand.

**Explanation:** Instead of bundling everything into one file, the Angular build system creates separate JavaScript chunks for lazy-loaded routes. When the user navigates, Angular fetches and loads only that chunk.

```typescript
// app.routes.ts
export const routes: Routes = [
  { path: '', component: HomeComponent },

  // Lazy load standalone component
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard.component')
      .then(m => m.DashboardComponent)
  },

  // Lazy load child routes
  {
    path: 'admin',
    loadChildren: () => import('./admin/admin.routes')
      .then(m => m.ADMIN_ROUTES)
  }
];
```

**Preloading strategies:**
```typescript
// Preload all lazy modules after initial load
provideRouter(routes, withPreloading(PreloadAllModules))

// Custom: preload only flagged routes
{ path: 'reports', loadComponent: ..., data: { preload: true } }
```

**Real-world scenario:** An ERP app with 20 feature areas. Without lazy loading: 5MB initial bundle, 8-second load. With lazy loading: 500KB initial, each feature loads 200-400KB on navigation. User sees the dashboard in 2 seconds.

**Common mistake:** Lazy loading too granularly — every tiny component as a separate chunk creates too many HTTP requests. Find the right balance at the feature/route level.

> Lazy loading is non-negotiable for any production Angular app. Combined with preloading strategies, users get fast initial loads without sacrificing navigation speed.

---

## 16. How do you handle state management in Angular?

**Summary:** State management in Angular ranges from simple service-based patterns with BehaviorSubjects/Signals to dedicated libraries like NgRx or NGXS — the right choice depends on your app's complexity.

**Approaches (simplest to most complex):**

**1. Services + Signals (small-medium apps):**
```typescript
@Injectable({ providedIn: 'root' })
export class CartService {
  private items = signal<CartItem[]>([]);

  readonly items$ = this.items.asReadonly();
  readonly total = computed(() =>
    this.items().reduce((sum, i) => sum + i.price, 0)
  );

  addItem(item: CartItem) {
    this.items.update(items => [...items, item]);
  }
}
```

**2. Component Store (medium apps):**
NgRx Component Store — localized state per feature, less boilerplate than full NgRx.

**3. NgRx Store (large enterprise apps):**
Full Redux pattern — actions, reducers, effects, selectors. Best for complex state with many side effects, undo/redo, time-travel debugging.

**When to use what:**
- **< 10 shared state properties** → Service + Signals/BehaviorSubject
- **Complex feature with many side effects** → NgRx Component Store
- **Enterprise with 50+ developers, complex state flows** → NgRx Global Store

**Common mistake:** Reaching for NgRx on day one. It adds significant boilerplate. Start with services + Signals, migrate to NgRx only when you genuinely feel the pain of managing complex state.

> The best state management is the simplest one that works. Services with Signals handle most apps. Save NgRx for when you truly need its power — and you'll know when you do.

---

## 17. How do you implement role-based authentication?

**Summary:** Role-based auth in Angular combines route guards, an auth service, HTTP interceptors for tokens, and structural directives or Signals to control UI visibility — always enforced server-side.

**Key pieces:**

**1. Auth Service:**
```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUser = signal<User | null>(null);

  hasRole(role: string): boolean {
    return this.currentUser()?.roles.includes(role) ?? false;
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }
}
```

**2. Functional Route Guard:**
```typescript
export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    return auth.hasAnyRole(allowedRoles)
      ? true
      : router.createUrlTree(['/unauthorized']);
  };
};

// Usage in routes
{ path: 'admin', component: AdminComponent, canActivate: [roleGuard(['ADMIN'])] }
```

**3. HTTP Interceptor (attach JWT):**
```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getToken();
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;
  return next(authReq);
};
```

**4. UI control:**
```html
@if (authService.hasRole('ADMIN')) {
  <button>Delete User</button>
}
```

**Critical point:** Angular guards and UI hiding are for UX, **not security**. The server must validate roles on every API request. A user can bypass any client-side check.

> Client-side role checks make the UX smooth, but the real authorization lives on the backend. Always implement both layers.

---

## 18. How do you secure Angular apps against XSS attacks?

**Summary:** Angular has built-in XSS protection through automatic sanitization of template bindings. Your job is to not bypass it, handle the edge cases, and implement CSP headers.

**Angular's built-in defenses:**
1. **Auto-sanitization** — all template bindings (`{{ }}`, `[innerHTML]`, etc.) are sanitized by default
2. **No `eval()`** — AOT-compiled templates don't use eval
3. **Strict contextual escaping** — Angular treats HTML, URLs, styles, and resource URLs differently

**What you must do:**

```typescript
// DANGEROUS — bypasses sanitization
element.innerHTML = userInput; // Never do this!

// SAFE — Angular sanitizes automatically
<div [innerHTML]="userInput"></div>

// When you MUST trust HTML (rare), use DomSanitizer explicitly
constructor(private sanitizer: DomSanitizer) {}
trustedHtml = this.sanitizer.bypassSecurityTrustHtml(cleanedHtml);
```

**Additional protections:**
1. **Content Security Policy headers** — `Content-Security-Policy: script-src 'self'`
2. **HttpOnly cookies** for tokens (not localStorage)
3. **Avoid `bypassSecurityTrust*`** unless absolutely necessary and input is validated
4. **Use Angular's `HttpClient`** — it doesn't concatenate URLs unsafely
5. **Sanitize on the server too** — defense in depth

**Common mistake:** Storing JWT in `localStorage` — any XSS vulnerability exposes it. Use HttpOnly cookies instead, which JavaScript can't access.

> Angular does 90% of the XSS protection work for you. Your job is to not undermine it with `bypassSecurityTrust*`, `innerHTML` via native DOM, or storing sensitive data in localStorage.

---

## 19. How do you integrate Angular with microservices APIs?

**Summary:** Integration involves a well-structured HTTP layer with environment-based API URLs, interceptors for cross-cutting concerns, proper error handling, and often an API gateway pattern.

**Architecture:**

```
Angular App → API Gateway → Microservice A (Users)
                          → Microservice B (Orders)
                          → Microservice C (Payments)
```

**Key implementation patterns:**

**1. Environment-based configuration:**
```typescript
// environment.ts
export const environment = {
  userApi: 'https://api.example.com/users',
  orderApi: 'https://api.example.com/orders',
};
```

**2. Service per domain:**
```typescript
@Injectable({ providedIn: 'root' })
export class OrderService {
  private baseUrl = environment.orderApi;

  getOrders() {
    return this.http.get<Order[]>(`${this.baseUrl}/orders`).pipe(
      retry(2),
      catchError(this.handleError)
    );
  }
}
```

**3. Interceptors for cross-cutting concerns:**
- Auth token attachment
- Error handling (401 → redirect to login, 503 → retry)
- Loading indicators
- Request/response logging

**4. Handling different API response formats:**
```typescript
// Adapter pattern — normalize responses
interface ApiResponse<T> { data: T; meta: any; }

getUsers(): Observable<User[]> {
  return this.http.get<ApiResponse<User[]>>(url).pipe(
    map(response => response.data)
  );
}
```

**Common mistake:** Making components call microservices directly. Always go through services — this abstracts the API contract and makes it easy to swap endpoints or add caching.

> The key is keeping a clean separation between your API layer and components. An API gateway simplifies CORS, auth, and routing. Your Angular app shouldn't care which microservice serves the data.

---

## 20. How do you improve Angular build performance in CI/CD?

**Summary:** CI/CD build performance comes down to caching, incremental builds, parallelization, and right-sizing your build configuration — the goal is fast feedback loops for developers.

**Key strategies:**

**1. Cache `node_modules` and Angular build cache:**
```yaml
# GitHub Actions example
- uses: actions/cache@v3
  with:
    path: |
      node_modules
      .angular/cache
    key: ${{ runner.os }}-angular-${{ hashFiles('package-lock.json') }}
```

**2. Use `esbuild` builder (Angular 17+):**
```json
// angular.json
"builder": "@angular-devkit/build-angular:application"
// esbuild is 2-4x faster than webpack
```

**3. Parallelization in CI:**
- Run lint, unit tests, and e2e tests in parallel jobs
- Use test sharding: `ng test --include='**/feature-a/**'` per job

**4. Incremental builds with Nx or Turborepo:**
```bash
# Only build/test affected projects
nx affected --target=build --base=origin/main
```

**5. Docker layer caching:**
```dockerfile
COPY package*.json ./
RUN npm ci              # cached unless package.json changes
COPY . .
RUN ng build --configuration production
```

**6. Skip what you don't need:**
- Disable source maps in CI: `"sourceMap": false`
- Skip tests for documentation-only PRs (path-based triggers)
- Use `--no-progress` to reduce log output

**Real-world impact:** Moving from uncached webpack builds to cached esbuild with Nx affected — CI went from 12 minutes to 3 minutes. Developer PR feedback in under 5 minutes.

**Common mistake:** Running `npm install` instead of `npm ci` in CI. `npm ci` is faster, deterministic, and uses the lockfile exactly as-is.

> Fast CI/CD isn't a luxury — it directly impacts developer productivity and deployment frequency. Invest in caching and incremental builds early; it pays dividends immediately.
