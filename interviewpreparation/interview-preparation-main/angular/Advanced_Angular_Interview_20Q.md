# Top 20 Advanced Angular Interview Questions & Answers
### For 5+ Years Senior Full-Stack / Frontend Developer
### Target: PwC, Deloitte, Cognizant, Capgemini, Product Companies

---

## SECTION 1: Core Architecture & Modern Angular

---

## Q1. Signals vs. RxJS: When should you use Signals for state management versus RxJS Observables for asynchronous streams?

**Summary:**
Signals are for synchronous, reactive state — think of them as a smarter replacement for component-level variables. RxJS is for asynchronous event streams — HTTP calls, WebSockets, complex event orchestration. They complement each other; Signals don't replace RxJS.

**Explanation:**

| Aspect | Signals | RxJS Observables |
|--------|---------|-----------------|
| Nature | Synchronous, pull-based | Asynchronous, push-based |
| Use Case | Component state, UI bindings | HTTP, WebSocket, timers, complex flows |
| Glitch-free | Yes (batched updates) | No (depends on operator chain) |
| Learning Curve | Low | High |
| Change Detection | Fine-grained (no Zone.js needed) | Requires `async` pipe or manual subscription |
| Composability | `computed()`, `effect()` | `pipe()`, `switchMap`, `combineLatest` |

**When I Use Signals:**
```typescript
// Simple component state — counter, toggle, form field values
export class ProductListComponent {
  searchTerm = signal('');
  products = signal<Product[]>([]);

  // Derived state — automatically recalculates when dependencies change
  filteredProducts = computed(() =>
    this.products().filter(p =>
      p.name.toLowerCase().includes(this.searchTerm().toLowerCase())
    )
  );
}
```

**When I Use RxJS:**
```typescript
// Async streams — HTTP with debounce, retry, cancellation
export class SearchService {
  search(term$: Observable<string>): Observable<Product[]> {
    return term$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => this.http.get<Product[]>(`/api/products?q=${term}`)),
      retry(2),
      catchError(() => of([]))
    );
  }
}
```

**Bridging Both — `toSignal()` and `toObservable()`:**
```typescript
// Convert Observable to Signal for template binding
products = toSignal(this.productService.getAll(), { initialValue: [] });

// Convert Signal to Observable for RxJS pipelines
searchTerm$ = toObservable(this.searchTerm);
```

**Real-World Scenario:**
In a dashboard application, I used Signals for UI state (selected tab, filter values, sidebar toggle) and RxJS for the WebSocket connection streaming live data. The WebSocket Observable was converted to a Signal using `toSignal()` for template binding — best of both worlds.

**Common Mistakes:**
- Replacing all RxJS with Signals — Signals can't handle debounce, retry, or complex async orchestration.
- Using RxJS for simple component state — overkill, Signals are cleaner.
- Forgetting `initialValue` in `toSignal()` — it returns `undefined` before the first emission.

**Closing:**
Signals for state, RxJS for streams. In modern Angular 17+, I default to Signals for component state and reach for RxJS only when I need async operators.

---

## Q2. Standalone Components: Explain the benefits of a "Zoneless" Angular application and how moving away from NgModules affects tree-shaking.

**Summary:**
Standalone components remove the NgModule boilerplate — each component declares its own dependencies. This enables better tree-shaking because the compiler knows exactly what each component uses. Zoneless Angular (using Signals) eliminates Zone.js, resulting in smaller bundles and more predictable change detection.

**Explanation:**

**Standalone Components (Angular 15+):**
```typescript
// BEFORE: NgModule-based — everything in declarations array
@NgModule({
  declarations: [ProductListComponent, ProductCardComponent],
  imports: [CommonModule, FormsModule, SharedModule], // imports entire SharedModule
})
export class ProductModule {}

// AFTER: Standalone — component declares its own imports
@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [ProductCardComponent, NgFor, AsyncPipe], // only what it needs
  template: `...`
})
export class ProductListComponent {}
```

**Tree-Shaking Impact:**
With NgModules, if `SharedModule` exports 50 components and you use 2, all 50 are bundled (because the module is the unit of compilation). With standalone, only the directly imported components are included — the compiler can tree-shake the rest.

**Zoneless Angular (Angular 18+ experimental, stable in 19):**
```typescript
// bootstrap without Zone.js
bootstrapApplication(AppComponent, {
  providers: [
    provideExperimentalZonelessChangeDetection() // Angular 18
    // provideZonelessChangeDetection()          // Angular 19
  ]
});
```

**Why Zoneless Matters:**
- Zone.js patches every async API (setTimeout, Promise, addEventListener) — ~15KB gzip added to bundle.
- Zone.js triggers change detection on EVERY async event, even irrelevant ones (e.g., a setTimeout for analytics).
- With Signals, Angular knows exactly which component's state changed — fine-grained updates, no unnecessary checks.

```typescript
// Zoneless + Signals = precise change detection
@Component({
  selector: 'app-counter',
  standalone: true,
  template: `<p>Count: {{ count() }}</p>
             <button (click)="increment()">+1</button>`,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CounterComponent {
  count = signal(0);
  increment() { this.count.update(c => c + 1); } // Only this component re-renders
}
```

**Real-World Scenario:**
When we migrated a 200-component enterprise app from NgModules to standalone, the main bundle size dropped by 22% because tree-shaking could finally remove unused components from shared modules. Switching to zoneless in a new project reduced initial bundle by ~15KB and eliminated phantom change detection cycles that were causing jank on mobile.

**Common Mistakes:**
- Importing `CommonModule` in standalone components — import individual directives (`NgIf`, `NgFor`) or use the new `@if`/`@for` control flow (no import needed).
- Thinking standalone means no lazy loading — you can still lazy load standalone components with `loadComponent`.
- Going zoneless without migrating to Signals — if you rely on Zone.js for change detection (e.g., plain variables in templates), the UI won't update.

**Closing:**
Standalone is the present, zoneless is the future. Every new Angular project I start is standalone-first with `@if`/`@for` control flow, and I evaluate zoneless readiness based on third-party library compatibility.

---

## Q3. Control Value Accessor (CVA): How do you create a custom form component that integrates seamlessly with ReactiveFormsModule?

**Summary:**
`ControlValueAccessor` is the bridge between Angular's form system and a custom component. By implementing it, your component works with `formControlName`, `ngModel`, and all reactive form features like validation and dirty/touched state.

**Explanation:**
The interface requires four methods:
- `writeValue(value)` — called when the form sets a value (e.g., `patchValue`).
- `registerOnChange(fn)` — gives you a callback to notify the form when the value changes.
- `registerOnTouched(fn)` — callback for marking the control as touched.
- `setDisabledState(isDisabled)` — optional, handles enable/disable.

**Real-World Example — Star Rating Component:**
```typescript
@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [NgFor],
  template: `
    <span *ngFor="let star of stars; let i = index"
          (click)="rate(i + 1)"
          (blur)="onTouched()"
          [class.filled]="i < value"
          [class.disabled]="disabled">
      ★
    </span>
  `,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => StarRatingComponent),
      multi: true
    }
  ]
})
export class StarRatingComponent implements ControlValueAccessor {
  stars = [1, 2, 3, 4, 5];
  value = 0;
  disabled = false;

  // These get assigned by Angular's form system
  private onChange: (value: number) => void = () => {};
  onTouched: () => void = () => {};

  // Called when form sets value programmatically
  writeValue(value: number): void {
    this.value = value || 0;
  }

  // Angular gives us the callback to notify form of changes
  registerOnChange(fn: (value: number) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  // User clicks a star
  rate(stars: number): void {
    if (this.disabled) return;
    this.value = stars;
    this.onChange(this.value);   // Notify form
    this.onTouched();            // Mark as touched
  }
}
```

**Usage — works exactly like a native form control:**
```typescript
// Reactive form
this.form = this.fb.group({
  productName: ['', Validators.required],
  rating: [0, [Validators.required, Validators.min(1)]]  // our custom component!
});

// Template
<app-star-rating formControlName="rating"></app-star-rating>
<div *ngIf="form.get('rating')?.errors?.['min']">
  Please rate at least 1 star
</div>
```

**Common Mistakes:**
- Forgetting `multi: true` in the provider — Angular won't register it as a value accessor.
- Not calling `onChange()` when the value changes — form stays out of sync.
- Not calling `onTouched()` — validation messages that depend on `touched` state never appear.
- Mutating the value inside `writeValue()` and accidentally triggering `onChange()` — causes infinite loops.

**Closing:**
CVA is how you make professional reusable form components. Every design system I've built uses it — date pickers, star ratings, tag inputs, rich text editors. Once you master it, you can wrap any UI library into Angular's form ecosystem.

---

## Q4. Change Detection Strategy: Explain the difference between Default and OnPush. How does ChangeDetectorRef manually trigger updates?

**Summary:**
Default change detection checks every component in the tree on every async event. OnPush only checks a component when its `@Input` reference changes, an event fires from the component, or you manually trigger it. OnPush is what I use in production — it's a massive performance win.

**Explanation:**

```
Default Strategy:
Any async event (click, HTTP response, setTimeout)
    → Angular checks EVERY component top-to-bottom
    → Expensive with 200+ components

OnPush Strategy:
Angular only checks this component when:
  1. @Input reference changes (new object, not mutation)
  2. DOM event fires FROM this component or its children
  3. async pipe emits a new value
  4. signal() value changes (Angular 17+)
  5. You manually call markForCheck() or detectChanges()
```

**The Gotcha — Object Mutation:**
```typescript
// Parent component
@Component({ template: `<app-child [user]="user"></app-child>` })
export class ParentComponent {
  user = { name: 'John', age: 30 };

  updateAge() {
    this.user.age = 31;        // ❌ MUTATION — OnPush child won't detect this!
    // this.user = { ...this.user, age: 31 }; // ✅ NEW REFERENCE — OnPush detects
  }
}

// Child component
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChildComponent {
  @Input() user!: User;  // OnPush compares reference, not deep equality
}
```

**Manual Triggers with ChangeDetectorRef:**
```typescript
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent {
  data: DashboardData;

  constructor(
    private cdr: ChangeDetectorRef,
    private wsService: WebSocketService
  ) {}

  ngOnInit() {
    // WebSocket updates don't trigger OnPush change detection
    this.wsService.messages$.subscribe(msg => {
      this.data = msg;
      this.cdr.markForCheck();   // Marks this component + ancestors for check
      // OR
      this.cdr.detectChanges();  // Runs change detection immediately on this subtree
    });
  }
}
```

**`markForCheck()` vs `detectChanges()`:**

| Method | What It Does | When to Use |
|--------|-------------|-------------|
| `markForCheck()` | Marks component dirty, checked in next cycle | Default choice — safe, batched |
| `detectChanges()` | Runs CD immediately on this component's subtree | When you need instant update (rare) |
| `detach()` | Removes component from CD tree entirely | High-perf scenarios (stock tickers, game loops) |

**Real-World Scenario:**
In a trading dashboard with 500+ cells updating via WebSocket, using Default strategy caused visible jank. Switching all components to OnPush and using `markForCheck()` only on cells that received new data reduced CD cycles from 500 components to 5–10 per update.

**Common Mistakes:**
- Using OnPush but mutating objects — UI doesn't update, developers blame Angular.
- Calling `detectChanges()` everywhere as a band-aid — defeats the purpose of OnPush.
- Not using `async` pipe with OnPush — manual subscriptions require manual `markForCheck()`.

**Closing:**
OnPush + immutable data + async pipe is the performance baseline for any serious Angular app. I set OnPush on every component and only use Default if there's a specific reason.

---

## Q5. Dependency Injection (DI) Resolution: Explain the hierarchy of injectors. What are @Host, @Self, @SkipSelf, and @Optional decorators?

**Summary:**
Angular's DI has a hierarchical injector tree — it walks up from the component to the root looking for a provider. The decorators `@Self`, `@SkipSelf`, `@Host`, and `@Optional` control where Angular looks and what happens if it doesn't find the dependency.

**Explanation — Injector Hierarchy:**

```
EnvironmentInjector (root / platform)
    │   → providedIn: 'root' services (singletons)
    │   → providers in bootstrapApplication()
    │
    ├── Route Injector (lazy-loaded route)
    │   → providers in Route config
    │
    └── ElementInjector (component tree)
        │
        AppComponent (providers: [LoggerService])
            │
            ParentComponent (providers: [DataService])
                │
                ChildComponent ← injection request starts here
                                  walks UP until it finds the provider
```

**The Decorators:**

```typescript
// @Self — ONLY look in THIS component's injector. Don't walk up.
constructor(@Self() private logger: LoggerService) {}
// Throws error if LoggerService not provided in THIS component

// @SkipSelf — SKIP this component's injector. Start looking from parent.
constructor(@SkipSelf() private logger: LoggerService) {}
// Useful when component provides its own instance but needs parent's too

// @Optional — Don't throw error if not found. Return null.
constructor(@Optional() private analytics: AnalyticsService) {}
// Safe when a service might not be provided (optional feature)

// @Host — Look up to the HOST component only (not beyond).
constructor(@Host() private config: PanelConfig) {}
// Stops at the host component boundary — used in content projection
```

**Real-World Example — Nested Form with @SkipSelf:**
```typescript
// Parent form group
@Component({
  selector: 'app-user-form',
  providers: [{ provide: FormGroupDirective, useExisting: UserFormComponent }],
  template: `
    <form [formGroup]="form">
      <app-address-form></app-address-form>  <!-- child form -->
    </form>
  `
})
export class UserFormComponent {
  form = new FormGroup({ name: new FormControl('') });
}

// Child form that needs PARENT's FormGroup, not its own
@Component({
  selector: 'app-address-form',
})
export class AddressFormComponent {
  constructor(@SkipSelf() private parentForm: FormGroupDirective) {
    // Gets the parent's form group — skips own injector
  }
}
```

**Real-World Example — @Optional for Feature Toggle:**
```typescript
@Component({...})
export class HeaderComponent {
  constructor(@Optional() private featureFlags: FeatureFlagService) {
    // featureFlags is null if the service isn't provided
    // Useful when this component is used in different apps
    // where FeatureFlagService may or may not exist
    this.showNewUI = this.featureFlags?.isEnabled('new-header') ?? false;
  }
}
```

**Common Mistakes:**
- Providing a service in a component's `providers` array creates a NEW instance per component — not a singleton.
- Using `@Self` when the service is provided at root level — throws `NullInjectorError`.
- Forgetting that lazy-loaded routes get their own injector — services provided in lazy route config are scoped to that route.

**Closing:**
Understanding the injector hierarchy is what separates senior from junior Angular developers. I use `@SkipSelf` in nested forms, `@Optional` for plugin-style architectures, and `@Self` when I need strict scoping. It's the backbone of Angular's architecture.

---

## SECTION 2: Performance & Optimization

---

## Q6. Optimizing Large Lists: How does trackBy work, and when would you implement Virtual Scrolling?

**Summary:**
`trackBy` tells Angular how to identify items in a list so it reuses DOM elements instead of destroying and recreating them. Virtual Scrolling renders only the visible items in the viewport. I use `trackBy` always, and Virtual Scrolling when the list exceeds ~500 items.

**Explanation:**

**Without trackBy:**
```typescript
// Every time the list reference changes, Angular destroys ALL DOM elements
// and recreates them — even if the data is the same
<div *ngFor="let item of items">{{ item.name }}</div>
```

**With trackBy:**
```typescript
// Angular 16 and below
<div *ngFor="let item of items; trackBy: trackById">
  {{ item.name }}
</div>

trackById(index: number, item: Product): number {
  return item.id;  // Angular reuses DOM elements with the same ID
}

// Angular 17+ with @for (trackBy is REQUIRED — enforces best practice)
@for (item of items; track item.id) {
  <div>{{ item.name }}</div>
}
```

**Virtual Scrolling with CDK:**
```typescript
import { ScrollingModule } from '@angular/cdk/scrolling';

@Component({
  standalone: true,
  imports: [ScrollingModule],
  template: `
    <cdk-virtual-scroll-viewport itemSize="50" class="list-viewport">
      <div *cdkVirtualFor="let item of items; trackBy: trackById"
           class="list-item">
        {{ item.name }}
      </div>
    </cdk-virtual-scroll-viewport>
  `,
  styles: [`.list-viewport { height: 400px; }`]
})
export class ProductListComponent {
  items: Product[] = [];  // could be 100,000 items

  trackById(index: number, item: Product): number {
    return item.id;
  }
}
```

**How Virtual Scrolling Works:**
```
Full list: 10,000 items
Viewport: shows ~10 items at a time
DOM: only 10-15 <div> elements exist (with buffer)

User scrolls ↓
    → Top items are removed from DOM
    → New items are added at the bottom
    → Only visible items + small buffer exist in DOM
```

**Real-World Scenario:**
We had an admin panel showing 50,000 audit log entries. Without virtual scrolling, the page froze for 8 seconds on load. With `cdk-virtual-scroll-viewport`, it rendered instantly and scrolled at 60fps — only ~20 DOM elements existed at any time.

**Common Mistakes:**
- Not setting `itemSize` correctly — causes janky scrolling and incorrect scroll position.
- Using virtual scrolling for small lists (< 100 items) — unnecessary complexity.
- Forgetting `trackBy` with `*cdkVirtualFor` — defeats the optimization.
- Variable height items with `itemSize` — use `autosize` strategy or fixed height.

**Closing:**
`trackBy` is non-negotiable — I use it on every list. Virtual scrolling kicks in when I'm dealing with hundreds or thousands of items. Together, they keep Angular apps fast regardless of data volume.

---

## Q7. Lazy Loading & Preloading: How do you implement a custom preloading strategy for feature modules?

**Summary:**
Lazy loading defers loading a route's code until the user navigates to it. Preloading loads lazy modules in the background after the app initializes. A custom preloading strategy lets you control which modules to preload based on priority, user role, or network conditions.

**Explanation:**

**Basic Lazy Loading:**
```typescript
// app.routes.ts
export const routes: Routes = [
  {
    path: 'admin',
    loadComponent: () => import('./admin/admin.component')
      .then(m => m.AdminComponent)  // standalone component lazy loading
  },
  {
    path: 'dashboard',
    loadChildren: () => import('./dashboard/dashboard.routes')
      .then(m => m.DASHBOARD_ROUTES)  // lazy load child routes
  }
];
```

**Built-in Preloading Strategies:**
```typescript
// No preloading (default) — load only on navigation
provideRouter(routes)

// Preload ALL lazy modules after app loads
provideRouter(routes, withPreloading(PreloadAllModules))
```

**Custom Preloading Strategy — Preload by Priority:**
```typescript
// Step 1: Mark routes with custom data
export const routes: Routes = [
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard.component'),
    data: { preload: true }   // ← preload this
  },
  {
    path: 'admin',
    loadComponent: () => import('./admin/admin.component'),
    data: { preload: false }  // ← don't preload (rarely accessed)
  },
  {
    path: 'reports',
    loadComponent: () => import('./reports/reports.component'),
    data: { preload: true }
  }
];

// Step 2: Implement the strategy
@Injectable({ providedIn: 'root' })
export class SelectivePreloadStrategy implements PreloadingStrategy {

  preload(route: Route, load: () => Observable<any>): Observable<any> {
    // Only preload routes marked with data.preload = true
    if (route.data?.['preload']) {
      console.log(`Preloading: ${route.path}`);
      return load();
    }
    return of(null);  // skip
  }
}

// Step 3: Register it
provideRouter(routes, withPreloading(SelectivePreloadStrategy))
```

**Advanced — Network-Aware Preloading:**
```typescript
@Injectable({ providedIn: 'root' })
export class NetworkAwarePreloadStrategy implements PreloadingStrategy {

  preload(route: Route, load: () => Observable<any>): Observable<any> {
    // Check network speed — don't preload on slow connections
    const connection = (navigator as any).connection;
    if (connection) {
      const slowConnection = connection.effectiveType === '2g'
                          || connection.saveData === true;
      if (slowConnection) return of(null);
    }

    // Preload high-priority routes
    if (route.data?.['preload']) {
      return load();
    }
    return of(null);
  }
}
```

**Real-World Scenario:**
In an enterprise CRM, 80% of users go to the Dashboard after login. We preloaded Dashboard and Contacts modules (high priority) but NOT the Admin and Reports modules (used by < 5% of users). This gave instant navigation to common pages while keeping the initial bundle small.

**Common Mistakes:**
- Using `PreloadAllModules` when you have 20+ lazy modules — wastes bandwidth.
- Not lazy loading at all — entire app in one bundle, slow initial load.
- Lazy loading every tiny component — overhead of extra HTTP requests outweighs the benefit.

**Closing:**
Lazy load by default, selectively preload high-traffic routes. Custom preloading strategies let you optimize the perceived performance without over-fetching.

---

## Q8. Angular Universal (SSR) & Hydration: Explain the difference between destructive and non-destructive hydration in Angular 18/19.

**Summary:**
SSR renders Angular on the server and sends HTML to the browser for fast initial paint. Hydration is how the client-side Angular takes over that server-rendered HTML. Destructive hydration (old) destroys and recreates the DOM. Non-destructive hydration (Angular 16+) reuses the existing DOM — no flicker, better performance.

**Explanation:**

**Destructive Hydration (Angular < 16):**
```
1. Server renders HTML → sent to browser → user sees content (fast FCP)
2. Angular JS bundle loads
3. Angular DESTROYS the server-rendered DOM ❌
4. Angular RECREATES the same DOM from scratch
5. Result: Visible flicker, lost scroll position, slow TTI
```

**Non-Destructive Hydration (Angular 16+):**
```
1. Server renders HTML → sent to browser → user sees content (fast FCP)
2. Angular JS bundle loads
3. Angular WALKS the existing DOM and attaches event listeners ✅
4. No destruction, no recreation — seamless takeover
5. Result: No flicker, preserved state, fast TTI
```

**Enabling SSR + Hydration in Angular 18/19:**
```typescript
// server.ts — SSR setup
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';

bootstrapApplication(AppComponent, {
  providers: [
    provideClientHydration(
      withEventReplay()  // Angular 18+ — replays user clicks during hydration
    )
  ]
});
```

**Event Replay (Angular 18):**
```
Problem: User clicks a button BEFORE hydration completes → click is lost
Solution: withEventReplay() captures events during SSR HTML phase
          and replays them after hydration

Timeline:
[Server HTML rendered] → [User clicks Buy] → [Hydration completes] → [Click replayed!]
```

**Incremental Hydration (Angular 19):**
```html
<!-- Only hydrate this section when it enters the viewport -->
@defer (on viewport) {
  <app-comments [postId]="post.id"></app-comments>
}

<!-- This deferred block stays as server-rendered HTML until triggered -->
<!-- Reduces JS execution on initial load dramatically -->
```

**Real-World Scenario:**
We built an e-commerce product page with SSR. With destructive hydration, users reported a "flash" — the page rendered, went blank for 200ms, then reappeared. After upgrading to Angular 17 non-destructive hydration, the flash disappeared completely. LCP improved by 40%, and FID improved because Angular wasn't rebuilding the DOM.

**Common Mistakes:**
- DOM mismatch between server and client renders — Angular warns and falls back to destructive hydration. Common cause: using `Date.now()` or `Math.random()` in templates.
- Not handling `isPlatformBrowser` / `isPlatformServer` — browser APIs (localStorage, window) crash on server.
- Not using `TransferState` — HTTP calls made on server are repeated on client, causing duplicate requests.

**Closing:**
Non-destructive hydration is the default in modern Angular. Combined with `@defer` for incremental hydration and event replay, it delivers SSR-quality FCP with SPA-quality interactivity.

---

## Q9. @defer Blocks: How do the new @defer, @loading, and @error template blocks improve Core Web Vitals (LCP)?

**Summary:**
`@defer` blocks let you lazy-load parts of a template based on triggers like viewport visibility, interaction, or timers. This reduces the initial bundle size and defers non-critical rendering, directly improving LCP and TTI.

**Explanation:**

```html
<!-- Component is NOT loaded until the user scrolls to it -->
@defer (on viewport) {
  <app-comments [postId]="postId"></app-comments>
} @loading (minimum 300ms) {
  <div class="skeleton-loader"></div>
} @placeholder (minimum 200ms) {
  <div>Scroll down to see comments</div>
} @error {
  <div>Failed to load comments. <button (click)="retry()">Retry</button></div>
}
```

**Trigger Types:**

| Trigger | Loads When | Use Case |
|---------|-----------|----------|
| `on viewport` | Element enters viewport | Below-the-fold content |
| `on interaction` | User clicks/focuses the placeholder | Expandable sections |
| `on hover` | Mouse hovers over placeholder | Tooltips, previews |
| `on idle` | Browser is idle | Non-critical widgets |
| `on timer(3s)` | After specified time | Delayed ads, analytics |
| `on immediate` | Immediately (still separate chunk) | Code-split but eager |
| `when condition` | Boolean expression becomes true | Conditional features |

**How It Improves Core Web Vitals:**

```
WITHOUT @defer:
Initial Bundle: [App + Dashboard + Charts + Comments + Sidebar + Analytics]
                = 850KB → Slow LCP, High TTI

WITH @defer:
Initial Bundle: [App + Dashboard]
                = 200KB → Fast LCP, Low TTI

Charts:     loaded on viewport    (+150KB when scrolled)
Comments:   loaded on viewport    (+100KB when scrolled)
Sidebar:    loaded on idle        (+80KB after idle)
Analytics:  loaded on timer(5s)   (+120KB after 5s)
```

**Real-World Example — Product Page:**
```html
<!-- Critical: loads immediately for LCP -->
<app-product-hero [product]="product"></app-product-hero>
<app-product-price [product]="product"></app-product-price>

<!-- Non-critical: defer until visible -->
@defer (on viewport) {
  <app-product-reviews [productId]="product.id"></app-product-reviews>
} @placeholder {
  <div class="review-skeleton"></div>
}

@defer (on viewport) {
  <app-related-products [categoryId]="product.categoryId"></app-related-products>
} @loading (minimum 500ms) {
  <app-spinner></app-spinner>
}

<!-- Load only if user interacts -->
@defer (on interaction) {
  <app-size-guide></app-size-guide>
} @placeholder {
  <button>View Size Guide</button>
}
```

**Common Mistakes:**
- Deferring above-the-fold content — hurts LCP because it's not rendered immediately.
- Not using `@placeholder` — blank space confuses users; show a skeleton.
- Not setting `minimum` on `@loading` — loading state flashes for 50ms, looks glitchy.
- Deferring tiny components — the overhead of a separate chunk isn't worth it for a 2KB component.

**Closing:**
`@defer` is the most impactful Angular feature for performance since lazy routing. I use it for everything below the fold, and it consistently drops LCP by 30-50% on content-heavy pages.

---

## Q10. AOT vs. JIT: What happens internally during the Ahead-of-Time compilation process?

**Summary:**
AOT compiles templates and TypeScript at build time, producing optimized JavaScript. JIT compiles in the browser at runtime. AOT is the default and only choice for production — it catches template errors at build time, produces smaller bundles, and renders faster.

**Explanation:**

```
JIT (Just-In-Time):
Source Code → [Ship to Browser] → Browser downloads compiler + templates
    → Compiles at runtime → Renders
    ❌ Larger bundle (includes Angular compiler ~1MB)
    ❌ Template errors found at runtime
    ❌ Slower startup

AOT (Ahead-of-Time):
Source Code → [ng build] → Compiler runs at build time → Optimized JS
    → Ship only runtime code to browser → Renders
    ✅ Smaller bundle (no compiler shipped)
    ✅ Template errors caught at build time
    ✅ Faster rendering (templates already compiled)
```

**What AOT Does Internally — Step by Step:**

**Step 1 — Analysis:**
```
Angular Compiler (ngc) reads:
├── Component metadata (@Component decorator)
├── Template HTML
├── Style files
└── Module/Route dependencies

It builds a complete dependency graph of:
  - What each component imports
  - What pipes and directives are used in templates
  - Type information for template expressions
```

**Step 2 — Type Checking (Template Type Checking):**
```typescript
// AOT catches this at BUILD TIME:
<p>{{ user.nmae }}</p>
//              ^^^^ Property 'nmae' does not exist on type 'User'

// In JIT, this would silently render as empty string at runtime
```

AOT performs full type checking on template expressions — same as TypeScript strict mode but for HTML templates.

**Step 3 — Code Generation:**
```
Each component's template is converted to optimized JavaScript:

<div *ngIf="showHeader">
  <h1>{{ title | uppercase }}</h1>
</div>

Becomes roughly:
function AppComponent_Template(rf, ctx) {
  if (rf & 1) {  // creation mode
    i0.ɵɵtemplate(0, AppComponent_div_0_Template, 2, 1, "div", 0);
  }
  if (rf & 2) {  // update mode
    i0.ɵɵproperty("ngIf", ctx.showHeader);
  }
}
// No template parsing at runtime — pure JS instruction set
```

**Step 4 — Tree Shaking & Optimization:**
```
AOT output is pure JS → Webpack/esbuild can tree-shake:
  - Unused components removed
  - Unused pipes removed
  - Dead code eliminated
  - Minified and compressed
```

**AOT Strictness Levels (tsconfig.json):**
```json
{
  "angularCompilerOptions": {
    "strictInjectionParameters": true,  // DI type safety
    "strictInputAccessModifiers": true, // @Input access checks
    "strictTemplates": true             // Full template type checking
  }
}
```

**Real-World Impact:**
On a large enterprise app, switching from JIT (dev mode) to AOT (prod):
- Bundle size: 4.2MB → 1.1MB (Angular compiler not shipped)
- First render: 3.2s → 1.1s (no runtime compilation)
- Template bug caught at build: 23 type errors in templates that were silently failing

**Common Mistakes:**
- Running JIT in production — larger bundle, slower, insecure (templates can be manipulated).
- Dynamic component creation with string templates — doesn't work with AOT. Use `ViewContainerRef` instead.
- Calling functions in templates — AOT doesn't prevent it, but it runs on every CD cycle. Use pipes or computed signals.

**Closing:**
AOT is non-negotiable for production. It's not just about performance — it's about type safety, security, and catching bugs before deployment. I always develop with `strictTemplates: true` to get AOT-level checks in development.

---

## SECTION 3: RxJS & State Management

---

## Q11. Higher-Order Mapping Operators: Compare switchMap, mergeMap, concatMap, and exhaustMap with real-world scenarios.

**Summary:**
These four operators all map an outer Observable to an inner Observable, but they differ in how they handle concurrency — whether they cancel, queue, merge, or ignore new emissions. Choosing the wrong one causes bugs that are hard to trace.

**Explanation:**

| Operator | When New Emission Arrives | Concurrent Inners | Real Analogy |
|----------|--------------------------|-------------------|--------------|
| `switchMap` | **Cancels** previous inner | 1 (latest only) | Google search — new keystroke cancels old request |
| `mergeMap` | **Runs** alongside previous | Unlimited | Bulk file upload — all run in parallel |
| `concatMap` | **Queues** after previous completes | 1 (sequential) | ATM queue — one transaction at a time |
| `exhaustMap` | **Ignores** until previous completes | 1 (first only) | Login button — ignore clicks while request is pending |

**switchMap — Type-ahead Search:**
```typescript
// User types: "A" → "An" → "Ang" → "Angular"
// We only care about the latest term — cancel previous HTTP calls
this.searchControl.valueChanges.pipe(
  debounceTime(300),
  distinctUntilChanged(),
  switchMap(term => this.http.get(`/api/search?q=${term}`))
  //       ^^^^^^^^^ "A" request cancelled when "An" arrives
).subscribe(results => this.results = results);
```

**mergeMap — Parallel File Uploads:**
```typescript
// Upload multiple files simultaneously — don't cancel, don't wait
this.files$.pipe(
  mergeMap(file => this.uploadService.upload(file), 3)
  //                                                ^ concurrency limit!
  // Without limit, 100 files = 100 simultaneous HTTP requests
).subscribe(response => this.uploadedFiles.push(response));
```

**concatMap — Sequential Order Processing:**
```typescript
// Process orders one at a time — order matters, no parallel execution
this.orderQueue$.pipe(
  concatMap(order => this.paymentService.charge(order))
  // Each payment completes before the next one starts
  // Guarantees: Order A charged before Order B
).subscribe(receipt => this.receipts.push(receipt));
```

**exhaustMap — Login Button Spam Protection:**
```typescript
// User spam-clicks login — only process the FIRST click
this.loginButton$.pipe(
  exhaustMap(() => this.authService.login(this.credentials))
  // Clicks while login is in progress are IGNORED
  // No duplicate login requests, no race conditions
).subscribe(user => this.router.navigate(['/dashboard']));
```

**Visual Timeline:**
```
Source:      --A-----B-----C-----D-->

switchMap:   --[A--->x]         (A cancelled when B arrives)
               --[B-->x]        (B cancelled when C arrives)
                  --[C->x]      (C cancelled when D arrives)
                     --[D--->|] (only D completes)

mergeMap:    --[A--------->|]
               --[B-------->|]
                  --[C------>|]
                     --[D---->|]  (all run in parallel)

concatMap:   --[A--------->|]
                          [B-------->|]
                                    [C------>|]
                                            [D---->|]  (sequential)

exhaustMap:  --[A--------->|]
               (B ignored)
                  (C ignored)
                     --[D---->|]  (D accepted because A is done)
```

**Common Mistakes:**
- Using `mergeMap` for search — causes race conditions (old response arrives after new one).
- Using `switchMap` for write operations — cancels POST requests, data loss.
- Using `concatMap` for search — slow response blocks all subsequent searches.
- Forgetting `mergeMap` concurrency limit — 1000 parallel HTTP calls overwhelms the server.

**Closing:**
My rule of thumb: `switchMap` for reads/searches, `exhaustMap` for form submissions, `concatMap` for sequential writes, `mergeMap` for parallel operations with a concurrency limit. Getting this right eliminates an entire category of async bugs.

---

## Q12. State Management Patterns: When would you choose NgRx/NGXS over a simple BehaviorSubject-based Service?

**Summary:**
Start with BehaviorSubject services. Adopt NgRx only when you have shared state across many components, complex state transitions, or need time-travel debugging. Most apps don't need NgRx — over-engineering state management is the #1 mistake I see.

**Explanation:**

**BehaviorSubject Service (Simple State):**
```typescript
@Injectable({ providedIn: 'root' })
export class CartService {
  private cartItems = new BehaviorSubject<CartItem[]>([]);
  cartItems$ = this.cartItems.asObservable();

  // Derived state
  cartTotal$ = this.cartItems$.pipe(
    map(items => items.reduce((sum, item) => sum + item.price * item.qty, 0))
  );

  addItem(item: CartItem) {
    const current = this.cartItems.getValue();
    this.cartItems.next([...current, item]);
  }

  removeItem(id: number) {
    const updated = this.cartItems.getValue().filter(i => i.id !== id);
    this.cartItems.next(updated);
  }
}
```
**Pros:** Simple, no boilerplate, easy to understand.
**Cons:** No action history, no devtools, state mutations hard to trace in large apps.

**Signal-Based Service (Angular 17+ — My Current Default):**
```typescript
@Injectable({ providedIn: 'root' })
export class CartService {
  private cartItems = signal<CartItem[]>([]);

  // Public read-only access
  items = this.cartItems.asReadonly();
  total = computed(() =>
    this.cartItems().reduce((sum, item) => sum + item.price * item.qty, 0)
  );
  count = computed(() => this.cartItems().length);

  addItem(item: CartItem) {
    this.cartItems.update(items => [...items, item]);
  }

  removeItem(id: number) {
    this.cartItems.update(items => items.filter(i => i.id !== id));
  }
}
```

**NgRx (Complex, Shared State):**
```typescript
// actions.ts
export const addToCart = createAction('[Cart] Add', props<{ item: CartItem }>());
export const removeFromCart = createAction('[Cart] Remove', props<{ id: number }>());
export const loadCart = createAction('[Cart] Load');
export const loadCartSuccess = createAction('[Cart] Load Success', props<{ items: CartItem[] }>());

// reducer.ts
export const cartReducer = createReducer(
  initialState,
  on(addToCart, (state, { item }) => ({ ...state, items: [...state.items, item] })),
  on(removeFromCart, (state, { id }) => ({
    ...state, items: state.items.filter(i => i.id !== id)
  })),
  on(loadCartSuccess, (state, { items }) => ({ ...state, items, loaded: true }))
);

// effects.ts — side effects (HTTP calls)
loadCart$ = createEffect(() =>
  this.actions$.pipe(
    ofType(loadCart),
    switchMap(() => this.cartApi.getCart().pipe(
      map(items => loadCartSuccess({ items })),
      catchError(error => of(loadCartError({ error })))
    ))
  )
);

// selectors.ts
export const selectCartItems = createSelector(selectCartState, state => state.items);
export const selectCartTotal = createSelector(selectCartItems,
  items => items.reduce((sum, i) => sum + i.price * i.qty, 0)
);
```

**Decision Matrix:**

| Criteria | BehaviorSubject/Signal | NgRx/NGXS |
|----------|----------------------|-----------|
| Team size | 1-5 devs | 5+ devs (enforced patterns) |
| State complexity | Local/feature state | Global shared state across routes |
| Side effects | Simple HTTP calls | Complex chains, optimistic updates |
| Debugging needs | Console.log is enough | Need time-travel, action log |
| Boilerplate tolerance | Low | High (actions, reducers, effects, selectors) |
| Typical app | Dashboard, CRUD forms | Trading platform, collaborative editors |

**Real-World Scenario:**
In a 5-developer team building an internal HR tool, I used Signal-based services. Simple, fast to build, everyone understood it. In a 20-developer team building a real-time trading platform with shared state across 50+ components, we used NgRx — the action log and Redux DevTools were essential for debugging state inconsistencies.

**Closing:**
Don't reach for NgRx because it's "enterprise." Reach for it when the complexity of state interactions justifies the boilerplate. Signals + services cover 80% of apps. NgRx is for the other 20%.

---

## Q13. Memory Leaks: What are the different ways to handle unsubscription?

**Summary:**
Every manual subscription in Angular is a potential memory leak. My preferred approach in order: `async` pipe > `takeUntilDestroyed()` > `DestroyRef` > `takeUntil` pattern. The async pipe is the safest because Angular manages the lifecycle.

**Explanation:**

**1. Async Pipe (Best — Zero Manual Management):**
```typescript
@Component({
  template: `
    <div *ngFor="let user of users$ | async">{{ user.name }}</div>
    <p>Total: {{ (total$ | async) ?? 'Loading...' }}</p>
  `
})
export class UserListComponent {
  users$ = this.userService.getUsers();
  total$ = this.users$.pipe(map(users => users.length));
  // Angular subscribes AND unsubscribes automatically
}
```

**2. takeUntilDestroyed() (Angular 16+ — Best for Imperative Code):**
```typescript
@Component({...})
export class DashboardComponent {
  constructor() {
    // Automatically unsubscribes when component is destroyed
    this.dataService.liveUpdates$.pipe(
      takeUntilDestroyed()  // No boilerplate!
    ).subscribe(data => this.processData(data));
  }
}

// If used outside constructor, inject DestroyRef explicitly:
export class DashboardComponent {
  private destroyRef = inject(DestroyRef);

  ngOnInit() {
    this.dataService.liveUpdates$.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(data => this.processData(data));
  }
}
```

**3. DestroyRef.onDestroy (Angular 16+ — Non-RxJS Cleanup):**
```typescript
export class ChartComponent {
  private destroyRef = inject(DestroyRef);

  ngOnInit() {
    const resizeObserver = new ResizeObserver(entries => {
      this.updateChart(entries);
    });
    resizeObserver.observe(this.chartElement.nativeElement);

    // Clean up non-RxJS resources
    this.destroyRef.onDestroy(() => {
      resizeObserver.disconnect();
    });
  }
}
```

**4. takeUntil Pattern (Pre-Angular 16):**
```typescript
export class OldComponent implements OnDestroy {
  private destroy$ = new Subject<void>();

  ngOnInit() {
    this.service.data$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(data => this.data = data);

    this.otherService.events$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(event => this.handleEvent(event));
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
```

**5. Subscription Collection (Least Preferred):**
```typescript
export class LegacyComponent implements OnDestroy {
  private subs: Subscription[] = [];

  ngOnInit() {
    this.subs.push(
      this.service.data$.subscribe(d => this.data = d),
      this.service.events$.subscribe(e => this.handle(e))
    );
  }

  ngOnDestroy() {
    this.subs.forEach(s => s.unsubscribe());
  }
}
```

**Ranking (Best to Worst):**
```
1. async pipe           — automatic, no code needed
2. takeUntilDestroyed() — one line, Angular 16+
3. DestroyRef           — for non-RxJS cleanup
4. takeUntil pattern    — works everywhere, slightly verbose
5. Manual unsubscribe   — error-prone, easy to forget
```

**Common Mistakes:**
- Subscribing in the component but only using the value in the template — use async pipe instead.
- Placing `takeUntil` before other operators — must be LAST in the pipe chain, otherwise intermediate observables may still leak.
- Forgetting that `HttpClient` observables auto-complete — no unsubscription needed for single HTTP calls.
- Router events, form valueChanges, and WebSocket streams DO need unsubscription.

**Closing:**
In Angular 17+, I use `async` pipe for templates and `takeUntilDestroyed()` for imperative subscriptions. Memory leaks from forgotten subscriptions are the most common Angular bug in enterprise apps — these patterns eliminate them entirely.

---

## Q14. Subject Variants: Difference between Subject, BehaviorSubject, and ReplaySubject.

**Summary:**
All three are multicast Observables that act as both producer and consumer. The difference is what happens when a new subscriber subscribes — Subject gives nothing, BehaviorSubject gives the current value, ReplaySubject gives N previous values.

**Explanation:**

| Feature | Subject | BehaviorSubject | ReplaySubject |
|---------|---------|-----------------|---------------|
| Initial Value | None | Required | None |
| Late Subscriber Gets | Nothing (misses past) | Latest value | Last N values |
| `.getValue()` | No | Yes | No |
| Use Case | Event bus | Current state | Cache/history |

**Visual Timeline:**
```
Emissions:     --A---B---C---[new subscriber]---D---E-->

Subject:       [subscriber gets]                D---E   (missed A, B, C)
BehaviorSubject: [subscriber gets]          C---D---E   (gets latest: C)
ReplaySubject(2): [subscriber gets]     B---C---D---E   (gets last 2: B, C)
ReplaySubject(∞): [subscriber gets] A---B---C---D---E   (gets ALL)
```

**Subject — Event Bus / Fire and Forget:**
```typescript
// Notification service — only care about events AFTER subscribing
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private notification$ = new Subject<Notification>();

  // Components subscribe to get future notifications
  get notifications(): Observable<Notification> {
    return this.notification$.asObservable();
  }

  show(message: string, type: 'success' | 'error') {
    this.notification$.next({ message, type });
  }
}

// If no component is subscribed when show() is called → notification is lost
// This is CORRECT behavior for transient events
```

**BehaviorSubject — Current State:**
```typescript
// Auth service — any component needs to know the CURRENT auth state
@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUser$ = new BehaviorSubject<User | null>(null);

  user$ = this.currentUser$.asObservable();

  // Synchronous access to current value
  get isLoggedIn(): boolean {
    return this.currentUser$.getValue() !== null;
  }

  login(credentials: Credentials): Observable<User> {
    return this.http.post<User>('/api/login', credentials).pipe(
      tap(user => this.currentUser$.next(user))
    );
  }

  logout() {
    this.currentUser$.next(null);
  }
}

// A component that subscribes AFTER login still gets the current user
// Because BehaviorSubject replays the last value to new subscribers
```

**ReplaySubject — History / Cache:**
```typescript
// Chat service — new subscriber gets the last 50 messages
@Injectable({ providedIn: 'root' })
export class ChatService {
  private messages$ = new ReplaySubject<ChatMessage>(50);
  //                                                 ^^ buffer size

  allMessages$ = this.messages$.asObservable();

  sendMessage(msg: ChatMessage) {
    this.messages$.next(msg);
  }
}

// User opens chat tab → immediately sees last 50 messages
// Without ReplaySubject, they'd see an empty chat until new messages arrive

// ReplaySubject with time window:
private events$ = new ReplaySubject<Event>(100, 30000);
// Replays up to 100 events from the last 30 seconds
```

**When to Use What — Decision Tree:**
```
Do you need to emit values to multiple subscribers?
├── No → just use Observable (http.get, etc.)
└── Yes → Subject variant
    │
    Do late subscribers need past values?
    ├── No → Subject (events, notifications)
    └── Yes →
        │
        Need only the LATEST value?
        ├── Yes → BehaviorSubject (auth state, current selection)
        └── No → ReplaySubject (message history, audit log)
```

**Common Mistakes:**
- Using `BehaviorSubject` when there's no meaningful initial value — forces you to use `null` and handle it everywhere. Use `ReplaySubject(1)` instead (no initial value, but replays last emission).
- Exposing the Subject directly — always expose `.asObservable()` to prevent external code from calling `.next()`.
- Forgetting to call `.complete()` — Subjects don't auto-complete. Call it in `ngOnDestroy` or service cleanup.
- Using `getValue()` in reactive pipelines — it breaks the reactive pattern. Use `withLatestFrom` instead.

**Closing:**
`BehaviorSubject` covers 80% of state management needs in Angular services. `Subject` for events, `ReplaySubject` for history. The key is knowing when late subscribers need past data — that's the entire decision.

---

## SECTION 4: Security & Integration (Full Stack Focus)

---

## Q15. HTTP Interceptors: How would you implement a global error handler or a JWT refresh token logic using interceptors?

**Summary:**
HTTP Interceptors are middleware for every HTTP request/response. I use them for attaching JWT tokens, refreshing expired tokens, global error handling, and adding correlation IDs. In Angular 17+, I use functional interceptors — simpler and tree-shakable.

**Explanation:**

**Functional Interceptor (Angular 15+ — Modern Approach):**
```typescript
// auth.interceptor.ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(req);
};

// Register in app config
provideHttpClient(
  withInterceptors([authInterceptor, errorInterceptor, loggingInterceptor])
)
```

**JWT Refresh Token Logic (The Tricky Part):**
```typescript
export const tokenRefreshInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/auth/refresh')) {
        return handle401(req, next, authService, router);
      }
      return throwError(() => error);
    })
  );
};

// Track if refresh is already in progress
let isRefreshing = false;
let refreshSubject = new BehaviorSubject<string | null>(null);

function handle401(
  req: HttpRequest<any>,
  next: HttpHandlerFn,
  authService: AuthService,
  router: Router
): Observable<HttpEvent<any>> {

  if (!isRefreshing) {
    isRefreshing = true;
    refreshSubject.next(null);  // Block other requests

    return authService.refreshToken().pipe(
      switchMap((tokens) => {
        isRefreshing = false;
        refreshSubject.next(tokens.accessToken);  // Unblock waiting requests

        // Retry the original request with new token
        return next(req.clone({
          setHeaders: { Authorization: `Bearer ${tokens.accessToken}` }
        }));
      }),
      catchError((err) => {
        isRefreshing = false;
        authService.logout();
        router.navigate(['/login']);
        return throwError(() => err);
      })
    );
  }

  // Other requests wait for the refresh to complete
  return refreshSubject.pipe(
    filter(token => token !== null),
    take(1),
    switchMap(token => next(req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    })))
  );
}
```

**Why This Is Complex:**
```
Request A → 401 → Start token refresh
Request B → 401 → Don't start ANOTHER refresh! Wait for A's refresh.
Request C → 401 → Also wait.

Token refreshed → Retry A, B, C with new token simultaneously.

If refresh fails → Logout user, cancel all pending requests.
```

**Global Error Handler Interceptor:**
```typescript
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notification = inject(NotificationService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      switch (error.status) {
        case 0:
          notification.show('Network error. Check your connection.', 'error');
          break;
        case 403:
          notification.show('Access denied.', 'error');
          router.navigate(['/unauthorized']);
          break;
        case 500:
          notification.show('Server error. Please try again later.', 'error');
          break;
        case 503:
          notification.show('Service unavailable. Retrying...', 'warning');
          // Could implement retry logic here
          break;
      }
      return throwError(() => error);  // Re-throw so component can handle too
    })
  );
};
```

**Common Mistakes:**
- Not skipping the refresh endpoint in the 401 handler — causes infinite loop (refresh → 401 → refresh → 401...).
- Not queuing concurrent 401 requests — multiple simultaneous token refreshes race and fail.
- Swallowing errors in the interceptor — always re-throw so components can show specific error UI.
- Not cloning the request — `HttpRequest` is immutable; modifying it directly throws an error.

**Closing:**
Interceptors are the backbone of API communication in Angular. The token refresh pattern with request queuing is the most asked interceptor question — get this right and you demonstrate real-world experience.

---

## Q16. Security (XSS/CSRF): How does Angular protect against Cross-Site Scripting, and what is DomSanitizer?

**Summary:**
Angular auto-sanitizes all values bound to the DOM — it treats every value as untrusted by default. This prevents XSS out of the box. `DomSanitizer` lets you explicitly bypass this for trusted content, but you should almost never need it.

**Explanation:**

**Angular's Built-in XSS Protection:**
```typescript
// Angular auto-escapes all template bindings
@Component({
  template: `
    <!-- SAFE: Angular escapes the HTML entities -->
    <p>{{ userInput }}</p>
    <!-- If userInput = "<script>alert('xss')</script>" -->
    <!-- Renders as: &lt;script&gt;alert('xss')&lt;/script&gt; -->

    <!-- SAFE: Angular sanitizes innerHTML -->
    <div [innerHTML]="userComment"></div>
    <!-- Angular strips <script> tags, event handlers, etc. -->
    <!-- <img onerror="alert('xss')"> → <img> (onerror removed) -->
  `
})
export class CommentComponent {
  userInput = '<script>alert("xss")</script>';  // Rendered as text
  userComment = '<b>Bold</b><script>steal()</script>';  // Only <b> survives
}
```

**Sanitization Contexts:**
```
Angular sanitizes differently based on context:
├── HTML       → strips <script>, event handlers, <object>, <embed>
├── Style      → strips url() and expression()
├── URL        → blocks javascript: and data: URLs
└── Resource   → blocks untrusted iframe/object src
```

**DomSanitizer — Bypassing Sanitization (Use With Extreme Caution):**
```typescript
import { DomSanitizer, SafeHtml, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  template: `
    <div [innerHTML]="trustedHtml"></div>
    <iframe [src]="trustedVideoUrl"></iframe>
  `
})
export class ContentComponent {
  trustedHtml: SafeHtml;
  trustedVideoUrl: SafeResourceUrl;

  constructor(private sanitizer: DomSanitizer) {
    // Only bypass when you TRUST the source and MUST render raw HTML
    this.trustedHtml = this.sanitizer.bypassSecurityTrustHtml(
      '<div class="widget"><h2>Trusted CMS Content</h2></div>'
    );

    // Bypass for iframe src (common for embeds)
    this.trustedVideoUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
      'https://www.youtube.com/embed/VIDEO_ID'
    );
  }
}
```

**Creating a Safe Pipe (Reusable):**
```typescript
@Pipe({ name: 'safeHtml', standalone: true })
export class SafeHtmlPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  transform(value: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(value);
  }
}

// Usage: <div [innerHTML]="cmsContent | safeHtml"></div>
```

**CSRF Protection:**
```typescript
// Angular's HttpClient automatically reads XSRF-TOKEN cookie
// and sends it as X-XSRF-TOKEN header

// Configure custom cookie/header names:
provideHttpClient(
  withXsrfConfiguration({
    cookieName: 'MY-XSRF-TOKEN',
    headerName: 'X-MY-XSRF-TOKEN'
  })
)

// Backend (Spring Boot) sets the cookie:
// XSRF-TOKEN=abc123; Path=/; HttpOnly=false
// Angular reads it and sends: X-XSRF-TOKEN: abc123
```

**Common Mistakes:**
- Using `bypassSecurityTrustHtml()` on user input — XSS vulnerability. Only use on trusted content (CMS, admin-created content).
- Using `element.nativeElement.innerHTML = userInput` — bypasses Angular's sanitization entirely. Never do this.
- Disabling sanitization "because it breaks my HTML" — fix the HTML instead.
- Not setting CSRF tokens on the backend — Angular's CSRF protection needs the server to cooperate.

**Closing:**
Angular's auto-sanitization handles 95% of XSS prevention. The remaining 5% where you need `bypassSecurityTrust*` should go through code review. If you find yourself using it on user-provided content, you have a design problem, not a sanitization problem.

---

## Q17. Route Guards: Differentiate between CanActivate, CanMatch, and CanDeactivate. How do you pass data using a Resolver?

**Summary:**
Route guards control navigation access. `CanActivate` checks if a user can visit a route, `CanMatch` prevents even matching the route config, and `CanDeactivate` asks before leaving a page. Resolvers pre-fetch data before the component loads. In Angular 15+, I use functional guards — simpler and tree-shakable.

**Explanation:**

| Guard | When It Runs | Use Case |
|-------|-------------|----------|
| `canActivate` | After route matches, before component loads | Auth check, role check |
| `canMatch` | During route matching itself | Show different component per role |
| `canDeactivate` | When user tries to leave | Unsaved changes warning |
| `canActivateChild` | Before activating child routes | Guard all child routes at once |
| `resolve` | After guards pass, before component renders | Pre-fetch data |

**canActivate — Auth Guard (Functional):**
```typescript
// auth.guard.ts
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  // Save the attempted URL for redirecting after login
  router.navigate(['/login'], {
    queryParams: { returnUrl: state.url }
  });
  return false;
};

// Role-based guard
export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const requiredRole = route.data['role'] as string;

  return authService.hasRole(requiredRole);
};

// Route config
{
  path: 'admin',
  loadComponent: () => import('./admin/admin.component'),
  canActivate: [authGuard, roleGuard],
  data: { role: 'ADMIN' }
}
```

**canMatch — Different Components Per Role:**
```typescript
// Same path, different component based on role
export const adminMatchGuard: CanMatchFn = () => {
  return inject(AuthService).hasRole('ADMIN');
};

export const userMatchGuard: CanMatchFn = () => {
  return inject(AuthService).hasRole('USER');
};

// Routes — first match wins
const routes: Routes = [
  {
    path: 'dashboard',
    loadComponent: () => import('./admin-dashboard.component'),
    canMatch: [adminMatchGuard]  // Only matches if user is admin
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./user-dashboard.component'),
    canMatch: [userMatchGuard]   // Falls through to this if not admin
  }
];
```

**canDeactivate — Unsaved Changes Warning:**
```typescript
export const unsavedChangesGuard: CanDeactivateFn<any> = (component) => {
  // Check if the component has unsaved changes
  if (component.hasUnsavedChanges && component.hasUnsavedChanges()) {
    return confirm('You have unsaved changes. Leave anyway?');
    // Or return a custom dialog Observable
  }
  return true;
};

// Component implements the check
@Component({...})
export class EditFormComponent {
  form = new FormGroup({...});

  hasUnsavedChanges(): boolean {
    return this.form.dirty;
  }
}

// Route
{
  path: 'edit/:id',
  component: EditFormComponent,
  canDeactivate: [unsavedChangesGuard]
}
```

**Resolver — Pre-fetch Data:**
```typescript
// product.resolver.ts
export const productResolver: ResolveFn<Product> = (route) => {
  const productService = inject(ProductService);
  const router = inject(Router);
  const id = route.paramMap.get('id')!;

  return productService.getById(id).pipe(
    catchError(() => {
      router.navigate(['/products']);  // Redirect if product not found
      return EMPTY;
    })
  );
};

// Route config
{
  path: 'products/:id',
  component: ProductDetailComponent,
  resolve: { product: productResolver }
}

// Component — data is available immediately
@Component({...})
export class ProductDetailComponent {
  private route = inject(ActivatedRoute);

  // Option 1: snapshot (one-time)
  product = this.route.snapshot.data['product'] as Product;

  // Option 2: observable (reacts to param changes)
  product$ = this.route.data.pipe(map(data => data['product'] as Product));
}
```

**Guard Execution Order:**
```
User navigates to /admin/settings
    │
    ├── 1. canMatch — Does this route even apply?
    ├── 2. canActivate — Can the user access /admin?
    ├── 3. canActivateChild — Can the user access /settings?
    ├── 4. resolve — Pre-fetch settings data
    └── 5. Component renders with resolved data
```

**Common Mistakes:**
- Blocking navigation with a synchronous Resolver that makes an HTTP call — use loading states in the component instead for better UX. Resolvers show a blank page while loading.
- Not handling Resolver errors — unhandled errors prevent navigation entirely.
- Using `canActivate` when `canMatch` is more appropriate — `canActivate` loads the module code even if access is denied.
- Returning `UrlTree` instead of `false` for redirects — `UrlTree` is the correct way to redirect from guards.

**Closing:**
Functional guards are cleaner than class-based guards and the standard in Angular 15+. I use `canActivate` for auth, `canMatch` for role-based routing, `canDeactivate` for unsaved changes, and Resolvers sparingly — only when the component genuinely can't render without the data.

---

## SECTION 5: Coding & Best Practices

---

## Q18. Component Communication: Beyond @Input and @Output, how do you use @ViewChild and @ContentChild (Content Projection)?

**Summary:**
`@ViewChild` accesses a child component/element defined in the component's own template. `@ContentChild` accesses content projected into the component from outside via `<ng-content>`. Content projection is Angular's equivalent of React's `children` — it's how you build truly reusable container components.

**Explanation:**

**@ViewChild — Accessing Own Template Children:**
```typescript
@Component({
  template: `
    <input #searchInput />
    <app-data-table></app-data-table>
  `
})
export class SearchPageComponent implements AfterViewInit {
  @ViewChild('searchInput') searchInput!: ElementRef<HTMLInputElement>;
  @ViewChild(DataTableComponent) dataTable!: DataTableComponent;

  ngAfterViewInit() {
    // Access native element
    this.searchInput.nativeElement.focus();

    // Call child component method
    this.dataTable.refresh();
  }
}
```

**@ContentChild — Accessing Projected Content:**
```typescript
// REUSABLE CARD COMPONENT
@Component({
  selector: 'app-card',
  template: `
    <div class="card">
      <div class="card-header">
        <ng-content select="[card-title]"></ng-content>
      </div>
      <div class="card-body">
        <ng-content></ng-content>
      </div>
      <div class="card-footer" *ngIf="footerTemplate">
        <ng-content select="[card-footer]"></ng-content>
      </div>
    </div>
  `
})
export class CardComponent implements AfterContentInit {
  @ContentChild('footerTemplate') footerTemplate?: TemplateRef<any>;
  @ContentChildren(ButtonComponent) buttons!: QueryList<ButtonComponent>;

  ngAfterContentInit() {
    // Access projected content
    console.log(`Card has ${this.buttons.length} buttons`);

    // React to dynamic content changes
    this.buttons.changes.subscribe(updatedButtons => {
      console.log('Buttons updated:', updatedButtons.length);
    });
  }
}

// USAGE — content is "projected" from the parent
<app-card>
  <h2 card-title>User Profile</h2>

  <p>This content goes into the default ng-content slot</p>
  <app-button>Edit</app-button>
  <app-button>Delete</app-button>

  <div card-footer>
    <span>Last updated: {{ lastUpdated }}</span>
  </div>
</app-card>
```

**Multi-Slot Content Projection:**
```typescript
// tab-group.component.ts
@Component({
  selector: 'app-tab-group',
  template: `
    <div class="tab-headers">
      @for (tab of tabs; track tab.label) {
        <button (click)="selectTab(tab)"
                [class.active]="tab === activeTab">
          {{ tab.label }}
        </button>
      }
    </div>
    <div class="tab-content">
      <ng-container *ngTemplateOutlet="activeTab?.contentTemplate">
      </ng-container>
    </div>
  `
})
export class TabGroupComponent implements AfterContentInit {
  @ContentChildren(TabComponent) tabs!: QueryList<TabComponent>;
  activeTab?: TabComponent;

  ngAfterContentInit() {
    this.activeTab = this.tabs.first;
  }

  selectTab(tab: TabComponent) {
    this.activeTab = tab;
  }
}

// tab.component.ts
@Component({
  selector: 'app-tab',
  template: `<ng-template #content><ng-content></ng-content></ng-template>`
})
export class TabComponent {
  @Input() label!: string;
  @ViewChild('content') contentTemplate!: TemplateRef<any>;
}

// USAGE
<app-tab-group>
  <app-tab label="Profile">
    <app-user-profile [userId]="userId"></app-user-profile>
  </app-tab>
  <app-tab label="Settings">
    <app-user-settings></app-user-settings>
  </app-tab>
  <app-tab label="Activity">
    <app-activity-log [userId]="userId"></app-activity-log>
  </app-tab>
</app-tab-group>
```

**Signal Queries (Angular 17+ — New Syntax):**
```typescript
// Modern replacement for decorator-based queries
export class SearchPageComponent {
  searchInput = viewChild.required<ElementRef>('searchInput');
  dataTable = viewChild(DataTableComponent);
  buttons = contentChildren(ButtonComponent);

  constructor() {
    // Reactive — no need for AfterViewInit lifecycle hook
    effect(() => {
      console.log('Buttons:', this.buttons().length);
    });
  }
}
```

**When to Use What:**

| Pattern | Use Case |
|---------|----------|
| `@Input/@Output` | Parent-child data flow (most common) |
| `@ViewChild` | Parent needs to call child's method or access its element |
| `@ContentChild` | Reusable component needs to interact with projected content |
| `@ContentChildren` | Querying multiple projected items (tabs, list items) |
| Service with Signal/Subject | Cross-component communication (siblings, unrelated) |
| Content Projection (`ng-content`) | Building layout/container components |

**Common Mistakes:**
- Accessing `@ViewChild` in `ngOnInit` — it's not available until `ngAfterViewInit`.
- Accessing `@ContentChild` in `ngAfterViewInit` — it's available in `ngAfterContentInit` (earlier).
- Using `@ViewChild` when `@ContentChild` is needed — they reference different DOM trees.
- Overusing `@ViewChild` to call child methods — prefer `@Input` bindings for data flow; `@ViewChild` for imperative actions like `.focus()` or `.scrollTo()`.

**Closing:**
Content projection with `@ContentChild` and `@ContentChildren` is how Angular component libraries (Material, PrimeNG) are built. Master this and you can create professional, reusable component APIs.

---

## Q19. Testing: How do you mock a service using SpyObj in Jasmine/Karma or Jest for unit testing a component?

**Summary:**
I mock dependencies using `jasmine.createSpyObj` or Jest's `jest.fn()` to isolate the component under test. The goal is testing the component's behavior, not its dependencies. I provide mocked services via `TestBed`'s dependency injection.

**Explanation:**

**Jasmine/Karma — createSpyObj Approach:**
```typescript
describe('ProductListComponent', () => {
  let component: ProductListComponent;
  let fixture: ComponentFixture<ProductListComponent>;
  let productServiceSpy: jasmine.SpyObj<ProductService>;

  beforeEach(async () => {
    // Create spy with methods you want to mock
    productServiceSpy = jasmine.createSpyObj('ProductService', [
      'getAll', 'delete', 'search'
    ]);

    // Set default return values
    productServiceSpy.getAll.and.returnValue(of([
      { id: 1, name: 'Product A', price: 100 },
      { id: 2, name: 'Product B', price: 200 }
    ]));

    await TestBed.configureTestingModule({
      imports: [ProductListComponent],  // standalone component
      providers: [
        { provide: ProductService, useValue: productServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();  // triggers ngOnInit
  });

  it('should load products on init', () => {
    expect(productServiceSpy.getAll).toHaveBeenCalledTimes(1);
    expect(component.products.length).toBe(2);
  });

  it('should display products in template', () => {
    const compiled = fixture.nativeElement;
    const items = compiled.querySelectorAll('.product-item');
    expect(items.length).toBe(2);
    expect(items[0].textContent).toContain('Product A');
  });

  it('should call delete and refresh list', () => {
    productServiceSpy.delete.and.returnValue(of(void 0));
    productServiceSpy.getAll.and.returnValue(of([
      { id: 2, name: 'Product B', price: 200 }
    ]));

    component.deleteProduct(1);

    expect(productServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(component.products.length).toBe(1);
  });

  it('should handle error gracefully', () => {
    productServiceSpy.getAll.and.returnValue(
      throwError(() => new Error('Server Error'))
    );

    component.loadProducts();

    expect(component.error).toBe('Failed to load products');
    expect(component.products.length).toBe(0);
  });
});
```

**Jest — jest.fn() Approach:**
```typescript
describe('ProductListComponent', () => {
  let component: ProductListComponent;
  let fixture: ComponentFixture<ProductListComponent>;

  const mockProductService = {
    getAll: jest.fn().mockReturnValue(of([
      { id: 1, name: 'Product A', price: 100 }
    ])),
    delete: jest.fn().mockReturnValue(of(void 0)),
    search: jest.fn()
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductListComponent],
      providers: [
        { provide: ProductService, useValue: mockProductService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductListComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    jest.clearAllMocks();  // Reset call counts between tests
  });

  it('should search products with debounce', fakeAsync(() => {
    mockProductService.search.mockReturnValue(of([
      { id: 3, name: 'Widget', price: 50 }
    ]));

    component.searchTerm.setValue('Wid');
    tick(300);  // simulate debounce
    fixture.detectChanges();

    expect(mockProductService.search).toHaveBeenCalledWith('Wid');
  }));
});
```

**Testing Async / Observable Behavior:**
```typescript
// fakeAsync + tick for time-based operations
it('should debounce search input', fakeAsync(() => {
  productServiceSpy.search.and.returnValue(of([]));

  component.onSearch('A');
  component.onSearch('An');
  component.onSearch('Angular');

  tick(300); // wait for debounce

  // Only the last value should trigger a search
  expect(productServiceSpy.search).toHaveBeenCalledTimes(1);
  expect(productServiceSpy.search).toHaveBeenCalledWith('Angular');
}));

// waitForAsync for real async operations
it('should load data', waitForAsync(() => {
  fixture.whenStable().then(() => {
    expect(component.products.length).toBeGreaterThan(0);
  });
}));
```

**Testing HTTP Interceptors:**
```typescript
describe('AuthInterceptor', () => {
  let httpMock: HttpTestingController;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting()
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
    http = TestBed.inject(HttpClient);
  });

  it('should add Authorization header', () => {
    http.get('/api/products').subscribe();

    const req = httpMock.expectOne('/api/products');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush([]);
  });
});
```

**Common Mistakes:**
- Not calling `fixture.detectChanges()` — template doesn't render.
- Forgetting `fakeAsync` + `tick` for debounced/delayed operations — test passes but doesn't actually test the behavior.
- Testing implementation details instead of behavior — "component calls service.getAll" is fragile; "component displays 2 products" is robust.
- Not resetting mocks between tests — stale call counts cause false positives.

**Closing:**
Good component tests mock external dependencies and test behavior, not implementation. `createSpyObj` for Jasmine, `jest.fn()` for Jest — same concept, different syntax. Test what the user sees and experiences.

---

## Q20. Environment Management: How do you handle different API configurations (Dev, QA, Prod) in modern Angular?

**Summary:**
The old `environment.ts` / `environment.prod.ts` pattern is deprecated in Angular 15+. I now use a combination of `APP_INITIALIZER` with runtime configuration loaded from a JSON file, or `define` in `angular.json` for build-time variables. Runtime config is my preferred approach — one build, multiple environments.

**Explanation:**

**Old Way (Build-Time — Deprecated):**
```typescript
// environment.ts — different file per env, replaced at build time
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};
// Problem: Separate BUILD per environment. Can't promote same artifact from QA to Prod.
```

**Modern Way 1 — Runtime Configuration (Recommended):**
```typescript
// assets/config/app-config.json (served as a static file)
{
  "apiUrl": "https://api.example.com",
  "featureFlags": {
    "newDashboard": true,
    "darkMode": false
  },
  "auth": {
    "clientId": "my-app",
    "authority": "https://auth.example.com"
  }
}

// config.service.ts
@Injectable({ providedIn: 'root' })
export class ConfigService {
  private config: AppConfig = {} as AppConfig;

  get apiUrl(): string { return this.config.apiUrl; }
  get featureFlags(): FeatureFlags { return this.config.featureFlags; }

  loadConfig(): Observable<AppConfig> {
    return this.http.get<AppConfig>('/assets/config/app-config.json').pipe(
      tap(config => this.config = config)
    );
  }
}

// app.config.ts — load config BEFORE the app starts
export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(),
    {
      provide: APP_INITIALIZER,
      useFactory: (configService: ConfigService) => {
        return () => configService.loadConfig();
      },
      deps: [ConfigService],
      multi: true
    }
  ]
};
```

**Deployment Strategy:**
```
Build ONCE → Docker image with compiled Angular
Deploy to Dev  → mount /assets/config/app-config.json (dev config)
Deploy to QA   → mount /assets/config/app-config.json (qa config)
Deploy to Prod → mount /assets/config/app-config.json (prod config)

# Kubernetes ConfigMap
apiVersion: v1
kind: ConfigMap
metadata:
  name: angular-config
data:
  app-config.json: |
    {
      "apiUrl": "https://api.prod.example.com",
      "featureFlags": { "newDashboard": true }
    }

# Mount as volume in deployment
volumes:
  - name: config
    configMap:
      name: angular-config
volumeMounts:
  - name: config
    mountPath: /usr/share/nginx/html/assets/config
```

**Modern Way 2 — `define` in angular.json (Build-Time Variables):**
```json
// angular.json
{
  "configurations": {
    "production": {
      "define": {
        "API_URL": "'https://api.prod.example.com'",
        "ENABLE_ANALYTICS": "true"
      }
    },
    "staging": {
      "define": {
        "API_URL": "'https://api.staging.example.com'",
        "ENABLE_ANALYTICS": "false"
      }
    }
  }
}
```

```typescript
// Usage in code
declare const API_URL: string;
declare const ENABLE_ANALYTICS: boolean;

@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseUrl = API_URL;  // Replaced at build time by esbuild
}
```

**Modern Way 3 — File Replacement (Still Works):**
```json
// angular.json
"configurations": {
  "production": {
    "fileReplacements": [{
      "replace": "src/environments/environment.ts",
      "with": "src/environments/environment.prod.ts"
    }]
  }
}
```
This still works but requires a separate build per environment.

**Decision Matrix:**

| Approach | Build per Env | Runtime Changeable | Best For |
|----------|:---:|:---:|----------|
| File replacement | Yes | No | Simple apps, small teams |
| `define` | Yes | No | Build-time constants (feature flags) |
| Runtime JSON + APP_INITIALIZER | No | Yes | Enterprise, Docker/K8s, CI/CD |

**Real-World Scenario:**
In a banking project, compliance required the same binary to be promoted through Dev → QA → UAT → Prod without recompilation. We used runtime JSON config loaded via `APP_INITIALIZER`, mounted via Kubernetes ConfigMaps per environment. API URLs, auth endpoints, and feature flags all changed without rebuilding.

**Common Mistakes:**
- Still using `environment.ts` file replacement in Angular 17+ — works, but requires N builds for N environments.
- Not using `APP_INITIALIZER` — config loads asynchronously, and the app tries to make API calls before config is ready.
- Hardcoding API URLs in services — always use a config service, even in small apps.
- Storing secrets in frontend config — API keys, client secrets should never be in client-side code. Use a backend-for-frontend (BFF) pattern.

**Closing:**
Runtime configuration with `APP_INITIALIZER` is the production-grade approach. One build, many environments — this is how enterprise Angular apps are deployed in Docker and Kubernetes.

---

## Quick Reference: Angular Concepts Summary

| # | Topic | Key Concept | Modern Angular (17+) |
|---|-------|-------------|---------------------|
| 1 | Signals vs RxJS | State vs Streams | `signal()`, `toSignal()` |
| 2 | Standalone + Zoneless | No NgModules, No Zone.js | `standalone: true`, `@if`/`@for` |
| 3 | CVA | Custom form controls | `ControlValueAccessor` |
| 4 | Change Detection | OnPush + immutable | `ChangeDetectionStrategy.OnPush` |
| 5 | DI Hierarchy | Injector tree + decorators | `inject()` function |
| 6 | Large Lists | trackBy + Virtual Scroll | `@for (track item.id)` |
| 7 | Lazy Loading | Custom preloading | `loadComponent()` |
| 8 | SSR + Hydration | Non-destructive + Event Replay | `provideClientHydration()` |
| 9 | @defer | Template lazy loading | `@defer (on viewport)` |
| 10 | AOT | Build-time compilation | `strictTemplates: true` |
| 11 | RxJS Operators | switchMap/mergeMap/concatMap/exhaustMap | Context-dependent |
| 12 | State Management | Signal Service > NgRx (usually) | `signal()` + `computed()` |
| 13 | Memory Leaks | Unsubscription | `takeUntilDestroyed()` |
| 14 | Subject Variants | Subject/Behavior/Replay | Decision by late-subscriber needs |
| 15 | Interceptors | JWT + Error handling | `HttpInterceptorFn` |
| 16 | Security | XSS/CSRF protection | `DomSanitizer`, `withXsrfConfiguration` |
| 17 | Route Guards | canActivate/canMatch/canDeactivate | `CanActivateFn` (functional) |
| 18 | Component Communication | ViewChild/ContentChild/Projection | `viewChild()` signal query |
| 19 | Testing | SpyObj / jest.fn() | `TestBed` + mock providers |
| 20 | Environment Config | Runtime JSON + APP_INITIALIZER | `define` or ConfigMap |
