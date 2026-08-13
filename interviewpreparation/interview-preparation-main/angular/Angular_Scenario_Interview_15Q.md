# 15 Angular Scenario-Based Interview Questions & Answers
### Senior Full-Stack / Frontend Developer (5+ Years)
### "Here's the problem — how would you solve it?"

---

## Scenario 1: Large Table Performance

> **You have a table with 10,000 rows in an Angular application and the UI becomes slow. How would you optimize the performance?**

**Summary:**
10,000 DOM elements is the root cause — the browser chokes on rendering and change detection. My approach is layered: Virtual Scrolling to render only visible rows, OnPush to minimize CD, trackBy to prevent DOM recreation, and server-side pagination as the ideal long-term solution.

**My Step-by-Step Fix:**

**Step 1 — Immediate: Virtual Scrolling (biggest impact)**
```typescript
// Only ~20 rows exist in the DOM at any time, regardless of data size
import { ScrollingModule } from '@angular/cdk/scrolling';

@Component({
  standalone: true,
  imports: [ScrollingModule],
  template: `
    <cdk-virtual-scroll-viewport itemSize="48" class="table-viewport">
      <table>
        <thead>
          <tr><th>ID</th><th>Name</th><th>Email</th><th>Status</th></tr>
        </thead>
        <tbody>
          <tr *cdkVirtualFor="let row of rows; trackBy: trackById">
            <td>{{ row.id }}</td>
            <td>{{ row.name }}</td>
            <td>{{ row.email }}</td>
            <td>{{ row.status }}</td>
          </tr>
        </tbody>
      </table>
    </cdk-virtual-scroll-viewport>
  `,
  styles: [`.table-viewport { height: 600px; }`],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DataTableComponent {
  rows = signal<User[]>([]);

  trackById(index: number, row: User): number {
    return row.id;
  }
}
```

**Step 2 — Change Detection: OnPush + trackBy**
```
Without trackBy + Default CD:
  Data refreshes → Angular destroys 10,000 <tr> elements → recreates 10,000
  Change Detection checks 10,000 bindings every cycle

With trackBy + OnPush:
  Data refreshes → Angular compares by ID → reuses unchanged rows
  CD only runs when @Input reference changes
```

**Step 3 — Server-Side Pagination (ideal for production)**
```typescript
// Don't load 10,000 rows at all — fetch 20 at a time
loadPage(page: number, size: number, sort: string): Observable<PageResponse<User>> {
  return this.http.get<PageResponse<User>>('/api/users', {
    params: { page: page.toString(), size: size.toString(), sort }
  });
}

// Backend returns:
// { content: [...20 rows], totalElements: 10000, totalPages: 500 }
```

**Step 4 — Additional Optimizations**
```
□ Avoid function calls in template — {{ getFullName(row) }} runs every CD cycle
  → Use pipes or precompute values in the data model
□ Use @defer for columns that have heavy rendering (charts, images)
□ Debounce search/filter inputs — don't filter on every keystroke
□ Detach CD entirely for real-time grids — use ChangeDetectorRef.detach()
  and manually trigger detectChanges() on a requestAnimationFrame loop
```

**Decision Matrix:**

| Data Size | Solution |
|-----------|----------|
| < 100 rows | No optimization needed |
| 100–500 rows | trackBy + OnPush |
| 500–5,000 rows | Virtual Scrolling + trackBy + OnPush |
| 5,000+ rows | Server-side pagination (+ virtual scrolling for UX) |

**Closing:**
I never render 10,000 DOM elements. Virtual scrolling for client-side data, server-side pagination for production. Combined with OnPush and trackBy, even massive datasets feel instant.

---

## Scenario 2: Change Detection Issue

> **Your component is not updating the UI when data changes in a service. What could be the reason and how would you fix it?**

**Summary:**
This is almost always an OnPush change detection issue — the component uses OnPush but the data is being mutated instead of replaced, or the update happens outside Angular's zone. I diagnose by checking the CD strategy, how the data flows, and whether Zone.js is aware of the change.

**My Diagnostic Checklist:**

**Cause 1 — OnPush + Object Mutation (Most Common):**
```typescript
// SERVICE
@Injectable({ providedIn: 'root' })
export class UserService {
  private user: User = { name: 'John', role: 'admin' };

  updateRole(role: string) {
    this.user.role = role;  // ❌ MUTATION — same object reference
  }

  getUser(): User {
    return this.user;
  }
}

// COMPONENT (OnPush)
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<p>{{ user.role }}</p>`
})
export class ProfileComponent implements OnInit {
  user!: User;

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.user = this.userService.getUser();
    // user reference NEVER changes → OnPush never re-checks → UI stale
  }
}
```

**Fix 1 — Use Observables + async pipe:**
```typescript
// SERVICE — expose Observable
@Injectable({ providedIn: 'root' })
export class UserService {
  private userSubject = new BehaviorSubject<User>({ name: 'John', role: 'admin' });
  user$ = this.userSubject.asObservable();

  updateRole(role: string) {
    const current = this.userSubject.getValue();
    this.userSubject.next({ ...current, role });  // ✅ New reference
  }
}

// COMPONENT — async pipe triggers markForCheck() automatically
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (user$ | async; as user) {
      <p>{{ user.role }}</p>
    }
  `
})
export class ProfileComponent {
  user$ = inject(UserService).user$;
  // ✅ async pipe handles subscribe + unsubscribe + markForCheck
}
```

**Fix 2 — Use Signals (Angular 17+):**
```typescript
// SERVICE
@Injectable({ providedIn: 'root' })
export class UserService {
  private user = signal<User>({ name: 'John', role: 'admin' });
  readonly currentUser = this.user.asReadonly();

  updateRole(role: string) {
    this.user.update(u => ({ ...u, role }));  // ✅ Signal notifies Angular
  }
}

// COMPONENT
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<p>{{ userService.currentUser().role }}</p>`
})
export class ProfileComponent {
  userService = inject(UserService);
  // ✅ Signal automatically marks component for check when value changes
}
```

**Cause 2 — Update Outside Angular's Zone:**
```typescript
// ❌ WebSocket callback runs outside Zone.js
ngOnInit() {
  this.socket.onmessage = (event) => {
    this.data = JSON.parse(event.data);
    // Zone.js didn't intercept this → no CD triggered → UI stale
  };
}

// ✅ Fix — bring it back into the zone
constructor(private ngZone: NgZone) {}

ngOnInit() {
  this.socket.onmessage = (event) => {
    this.ngZone.run(() => {
      this.data = JSON.parse(event.data);  // Now Angular knows about this
    });
  };
}
```

**Cause 3 — Manual Subscription Without markForCheck:**
```typescript
// ❌ OnPush + manual subscribe without notifying CD
ngOnInit() {
  this.userService.user$.subscribe(user => {
    this.user = user;
    // OnPush doesn't know this changed!
  });
}

// ✅ Fix — use markForCheck
constructor(private cdr: ChangeDetectorRef) {}

ngOnInit() {
  this.userService.user$.pipe(takeUntilDestroyed()).subscribe(user => {
    this.user = user;
    this.cdr.markForCheck();  // Tell OnPush to re-check
  });
}

// ✅ Better fix — just use async pipe (handles markForCheck internally)
```

**Quick Diagnosis Flow:**
```
UI not updating?
├── Is component OnPush?
│   ├── Yes → Is @Input reference changing? (new object, not mutation?)
│   │   ├── No → Create new reference: { ...old, newProp }
│   │   └── Yes → Is it an Observable with async pipe?
│   │       ├── No → Add async pipe or markForCheck()
│   │       └── Yes → Check if Observable actually emits (tap + console.log)
│   └── No (Default) → Is update happening outside Angular zone?
│       ├── Yes → Wrap in NgZone.run()
│       └── No → Check for errors in console, verify data flow
```

**Closing:**
90% of "UI not updating" issues are OnPush + mutation. The fix is always the same: use immutable updates, async pipe, or Signals. I default to Signals in Angular 17+ — they eliminate this entire category of bugs.

---

## Scenario 3: Lazy Loading

> **Your Angular application has many modules and large bundle size. How would you implement lazy loading to improve performance?**

**Summary:**
Lazy loading splits the app into chunks loaded on demand when the user navigates. I lazy load every feature route except the landing page, use a selective preloading strategy for high-traffic routes, and verify with bundle analyzer.

**Implementation:**

**Step 1 — Lazy Load Routes (Standalone Components):**
```typescript
// app.routes.ts
export const routes: Routes = [
  // Eager — in main bundle (landing page, login)
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },

  // Lazy — separate chunks, loaded on navigation
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard.component')
      .then(m => m.DashboardComponent)
  },
  {
    path: 'products',
    loadChildren: () => import('./products/product.routes')
      .then(m => m.PRODUCT_ROUTES)
  },
  {
    path: 'admin',
    loadChildren: () => import('./admin/admin.routes')
      .then(m => m.ADMIN_ROUTES),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'reports',
    loadComponent: () => import('./reports/reports.component')
      .then(m => m.ReportsComponent)
  }
];

// products/product.routes.ts — nested lazy routes
export const PRODUCT_ROUTES: Routes = [
  { path: '', component: ProductListComponent },
  {
    path: ':id',
    loadComponent: () => import('./product-detail/product-detail.component')
      .then(m => m.ProductDetailComponent)
  }
];
```

**Step 2 — Selective Preloading (Preload High-Traffic Routes):**
```typescript
// Mark routes for preloading
{
  path: 'dashboard',
  loadComponent: () => import('./dashboard/dashboard.component'),
  data: { preload: true }   // ← preload in background
},
{
  path: 'admin',
  loadChildren: () => import('./admin/admin.routes'),
  data: { preload: false }  // ← don't preload (rarely used)
}

// Custom strategy
@Injectable({ providedIn: 'root' })
export class SelectivePreloadStrategy implements PreloadingStrategy {
  preload(route: Route, load: () => Observable<any>): Observable<any> {
    return route.data?.['preload'] ? load() : of(null);
  }
}

// Register
provideRouter(routes, withPreloading(SelectivePreloadStrategy))
```

**Step 3 — Template-Level Lazy Loading with @defer:**
```html
<!-- Lazy load heavy components WITHIN a page -->
@defer (on viewport) {
  <app-analytics-chart [data]="chartData"></app-analytics-chart>
} @placeholder {
  <div class="chart-skeleton"></div>
}

@defer (on interaction) {
  <app-comment-section [postId]="post.id"></app-comment-section>
} @placeholder {
  <button>Load Comments</button>
}
```

**Step 4 — Verify with Bundle Analyzer:**
```bash
# Build with stats
ng build --stats-json

# Analyze
npx webpack-bundle-analyzer dist/my-app/stats.json

# Check for:
# ✅ Separate chunks for each lazy route
# ❌ Large third-party libs in main bundle (move to lazy or replace)
# ❌ Shared module imported eagerly pulling in lazy code
```

**Before & After:**
```
BEFORE (no lazy loading):
main.js          — 3.2 MB  (everything in one file)
Initial load: 4.5s on 3G

AFTER (lazy loading + preloading):
main.js          — 280 KB  (core + home page)
dashboard.js     — 150 KB  (loaded on navigate / preloaded)
products.js      — 200 KB  (loaded on navigate / preloaded)
admin.js         — 180 KB  (loaded on navigate)
reports.js       — 120 KB  (loaded on navigate)
Initial load: 1.2s on 3G
```

**Common Mistakes:**
- Importing a lazy component directly in an eager component — silently breaks lazy loading.
- `SharedModule` importing heavy dependencies — every lazy module that imports `SharedModule` gets them too. Keep shared modules lightweight.
- Not lazy loading at all — the single biggest Angular performance fix most teams miss.

**Closing:**
Lazy loading is the #1 performance optimization for Angular bundle size. I lazy load every feature route, selectively preload common ones, and use `@defer` for within-page lazy loading. Always verify with bundle analyzer.

---

## Scenario 4: Memory Leak Problem

> **You notice that your Angular application memory usage keeps increasing after navigating between pages. How would you identify and fix the memory leak?**

**Summary:**
Memory leaks in Angular are almost always caused by forgotten subscriptions, unremoved event listeners, or retained DOM references. I identify them using Chrome DevTools heap snapshots and fix them with `takeUntilDestroyed()`, async pipe, and proper cleanup.

**Step 1 — Identify the Leak (Chrome DevTools):**
```
1. Open Chrome DevTools → Memory tab
2. Navigate to Page A → take Heap Snapshot #1
3. Navigate to Page B → Navigate back to Page A → Navigate to Page B again
4. Take Heap Snapshot #2
5. Compare: If snapshot #2 is significantly larger than #1
   → components from Page A are not being garbage collected
6. Filter by "Detached" → shows DOM nodes that should be gone but aren't
```

**Step 2 — Common Leak Sources and Fixes:**

**Leak 1 — Unsubscribed Observables (Most Common):**
```typescript
// ❌ LEAK — subscription lives forever
@Component({...})
export class DashboardComponent implements OnInit {
  ngOnInit() {
    this.dataService.liveUpdates$.subscribe(data => {
      this.data = data;
      // Component destroyed → subscription still active → leak
    });

    setInterval(() => this.refresh(), 5000);
    // Component destroyed → interval still firing → leak
  }
}

// ✅ FIX — takeUntilDestroyed (Angular 16+)
@Component({...})
export class DashboardComponent {
  constructor() {
    this.dataService.liveUpdates$.pipe(
      takeUntilDestroyed()  // Auto-unsubscribes on destroy
    ).subscribe(data => this.data = data);
  }
}

// ✅ BETTER FIX — async pipe (automatic lifecycle management)
@Component({
  template: `
    @if (data$ | async; as data) {
      <app-chart [data]="data"></app-chart>
    }
  `
})
export class DashboardComponent {
  data$ = this.dataService.liveUpdates$;
}
```

**Leak 2 — Event Listeners Not Removed:**
```typescript
// ❌ LEAK
ngOnInit() {
  window.addEventListener('resize', this.onResize);
  document.addEventListener('click', this.handleClick);
}

// ✅ FIX — Clean up in onDestroy
private destroyRef = inject(DestroyRef);

ngOnInit() {
  window.addEventListener('resize', this.onResize);

  this.destroyRef.onDestroy(() => {
    window.removeEventListener('resize', this.onResize);
  });
}

// ✅ BETTER — Use @HostListener (Angular handles cleanup)
@HostListener('window:resize', ['$event'])
onResize(event: Event) {
  this.width = window.innerWidth;
}
```

**Leak 3 — Closures Retaining Component References:**
```typescript
// ❌ LEAK — closure retains 'this' after component destroyed
ngOnInit() {
  this.thirdPartyLib.onUpdate((data) => {
    this.processData(data);  // 'this' = destroyed component, held in memory
  });
}

// ✅ FIX — Deregister callback
ngOnInit() {
  this.callbackId = this.thirdPartyLib.onUpdate((data) => {
    this.processData(data);
  });
}

// cleanup
destroyRef = inject(DestroyRef);
constructor() {
  this.destroyRef.onDestroy(() => {
    this.thirdPartyLib.offUpdate(this.callbackId);
  });
}
```

**Leak 4 — Growing Collections in Services:**
```typescript
// ❌ LEAK — service is a singleton, array grows forever
@Injectable({ providedIn: 'root' })
export class EventLogService {
  private logs: LogEntry[] = [];  // NEVER cleared

  addLog(entry: LogEntry) {
    this.logs.push(entry);  // Grows with every page visit
  }
}

// ✅ FIX — Cap the size or use a circular buffer
addLog(entry: LogEntry) {
  this.logs.push(entry);
  if (this.logs.length > 1000) {
    this.logs = this.logs.slice(-500);  // Keep last 500
  }
}
```

**Prevention Checklist:**
```
□ Use async pipe instead of .subscribe() wherever possible
□ Use takeUntilDestroyed() for imperative subscriptions
□ Remove event listeners in DestroyRef.onDestroy()
□ Unregister from third-party library callbacks
□ Don't store unbounded data in singleton services
□ Clear setInterval / setTimeout in cleanup
□ Disconnect ResizeObserver, MutationObserver, IntersectionObserver
```

**Closing:**
90% of Angular memory leaks are forgotten subscriptions. `async` pipe and `takeUntilDestroyed()` eliminate them by design. I always check heap snapshots in DevTools during development when dealing with navigation-heavy apps.

---

## Scenario 5: Parent–Child Communication

> **You need to pass data from a parent component to multiple child components and also receive events back. What Angular mechanisms would you use?**

**Summary:**
`@Input()` for parent-to-child data flow, `@Output()` with `EventEmitter` for child-to-parent events. In Angular 17+, I use signal inputs and model signals for two-way binding. For deeply nested components, I use a shared service instead of prop drilling.

**Pattern 1 — @Input + @Output (Standard):**
```typescript
// PARENT
@Component({
  template: `
    <app-filter
      [categories]="categories"
      [selectedCategory]="activeCategory()"
      (categoryChange)="onCategoryChange($event)">
    </app-filter>

    @for (product of filteredProducts(); track product.id) {
      <app-product-card
        [product]="product"
        [isWishlisted]="wishlist().has(product.id)"
        (addToCart)="onAddToCart($event)"
        (toggleWishlist)="onToggleWishlist($event)">
      </app-product-card>
    }

    <app-cart-summary
      [cartItems]="cartItems()"
      [total]="cartTotal()">
    </app-cart-summary>
  `
})
export class ProductPageComponent {
  activeCategory = signal('all');
  cartItems = signal<CartItem[]>([]);

  filteredProducts = computed(() =>
    this.activeCategory() === 'all'
      ? this.products()
      : this.products().filter(p => p.category === this.activeCategory())
  );

  cartTotal = computed(() =>
    this.cartItems().reduce((sum, item) => sum + item.price * item.qty, 0)
  );

  onCategoryChange(category: string) {
    this.activeCategory.set(category);
  }

  onAddToCart(product: Product) {
    this.cartItems.update(items => [...items, { ...product, qty: 1 }]);
  }
}

// CHILD — ProductCardComponent
@Component({
  selector: 'app-product-card',
  template: `
    <div class="card">
      <h3>{{ product.name }}</h3>
      <p>{{ product.price | currency }}</p>
      <button (click)="addToCart.emit(product)">Add to Cart</button>
      <button (click)="toggleWishlist.emit(product.id)">
        {{ isWishlisted ? '❤' : '♡' }}
      </button>
    </div>
  `
})
export class ProductCardComponent {
  @Input({ required: true }) product!: Product;
  @Input() isWishlisted = false;
  @Output() addToCart = new EventEmitter<Product>();
  @Output() toggleWishlist = new EventEmitter<number>();
}
```

**Pattern 2 — Signal Inputs + Model Signal (Angular 17+):**
```typescript
// CHILD — two-way binding with model()
@Component({
  selector: 'app-filter',
  template: `
    <select [value]="selectedCategory()" (change)="onSelect($event)">
      @for (cat of categories(); track cat) {
        <option [value]="cat">{{ cat }}</option>
      }
    </select>
  `
})
export class FilterComponent {
  categories = input.required<string[]>();       // read-only signal input
  selectedCategory = model.required<string>();   // two-way signal binding

  onSelect(event: Event) {
    this.selectedCategory.set((event.target as HTMLSelectElement).value);
    // Automatically propagates back to parent — no EventEmitter needed!
  }
}

// PARENT — two-way binding with model signal
<app-filter
  [categories]="categories"
  [(selectedCategory)]="activeCategory">  <!-- two-way binding -->
</app-filter>
```

**Pattern 3 — Shared Service (Avoid Deep Prop Drilling):**
```typescript
// When data needs to reach deeply nested children
// Parent → Child → GrandChild → GreatGrandChild
// Passing @Input through 4 levels = prop drilling nightmare

// ✅ Shared service
@Injectable()  // NOT providedIn: 'root' — scoped to parent
export class ProductPageState {
  activeCategory = signal('all');
  cartItems = signal<CartItem[]>([]);
  cartTotal = computed(() =>
    this.cartItems().reduce((sum, i) => sum + i.price * i.qty, 0)
  );
}

// Parent provides the service
@Component({
  providers: [ProductPageState],  // Scoped instance
  template: `<app-filter /><app-product-list /><app-cart-summary />`
})
export class ProductPageComponent {}

// Any descendant injects it directly — no @Input chain needed
@Component({ selector: 'app-deeply-nested-price' })
export class DeeplyNestedPriceComponent {
  state = inject(ProductPageState);
  // Access state.cartTotal() directly — no prop drilling
}
```

**When to Use What:**
```
1-2 levels deep → @Input + @Output (simple, explicit)
2+ levels deep  → Shared service with signals (avoid prop drilling)
Two-way binding → model() signal (Angular 17+)
Sibling comms   → Shared service (see Scenario 6)
```

**Closing:**
`@Input` + `@Output` for direct parent-child. Signal `model()` for two-way binding. Shared service for deep nesting. I never prop-drill more than 2 levels — that's the signal to switch to a service.

---

## Scenario 6: Component Communication (No Parent Relationship)

> **Two components that do not have a parent-child relationship need to communicate. How would you implement this?**

**Summary:**
I use a shared service with Signals or BehaviorSubject as the communication channel. The service acts as a mediator — one component writes, the other reacts. No direct coupling between the components.

**Implementation — Signal-Based Shared Service (Angular 17+):**
```typescript
// Shared service — the mediator
@Injectable({ providedIn: 'root' })
export class CartStateService {
  // Private writable signal
  private items = signal<CartItem[]>([]);

  // Public read-only signals
  readonly cartItems = this.items.asReadonly();
  readonly cartCount = computed(() => this.items().length);
  readonly cartTotal = computed(() =>
    this.items().reduce((sum, i) => sum + i.price * i.qty, 0)
  );

  addItem(product: Product) {
    this.items.update(items => {
      const existing = items.find(i => i.id === product.id);
      if (existing) {
        return items.map(i =>
          i.id === product.id ? { ...i, qty: i.qty + 1 } : i
        );
      }
      return [...items, { ...product, qty: 1 }];
    });
  }

  removeItem(productId: number) {
    this.items.update(items => items.filter(i => i.id !== productId));
  }

  clearCart() {
    this.items.set([]);
  }
}

// COMPONENT A — Product page (WRITES to cart)
@Component({
  selector: 'app-product-card',
  template: `
    <button (click)="addToCart()">Add to Cart</button>
  `
})
export class ProductCardComponent {
  @Input({ required: true }) product!: Product;
  private cartService = inject(CartStateService);

  addToCart() {
    this.cartService.addItem(this.product);
  }
}

// COMPONENT B — Header badge (READS from cart) — completely unrelated in DOM
@Component({
  selector: 'app-header-cart-badge',
  template: `
    <span class="badge">{{ cartService.cartCount() }}</span>
  `
})
export class HeaderCartBadgeComponent {
  cartService = inject(CartStateService);
}

// COMPONENT C — Cart sidebar (READS + WRITES)
@Component({
  selector: 'app-cart-sidebar',
  template: `
    @for (item of cartService.cartItems(); track item.id) {
      <div>
        {{ item.name }} × {{ item.qty }}
        <button (click)="cartService.removeItem(item.id)">Remove</button>
      </div>
    }
    <p>Total: {{ cartService.cartTotal() | currency }}</p>
  `
})
export class CartSidebarComponent {
  cartService = inject(CartStateService);
}
```

**Alternative — BehaviorSubject (Pre-Angular 17):**
```typescript
@Injectable({ providedIn: 'root' })
export class CartStateService {
  private items$ = new BehaviorSubject<CartItem[]>([]);
  cartItems$ = this.items$.asObservable();
  cartCount$ = this.cartItems$.pipe(map(items => items.length));

  addItem(product: Product) {
    const current = this.items$.getValue();
    this.items$.next([...current, { ...product, qty: 1 }]);
  }
}

// Component uses async pipe
@Component({
  template: `<span>{{ cartCount$ | async }}</span>`
})
export class HeaderCartBadgeComponent {
  cartCount$ = inject(CartStateService).cartCount$;
}
```

**Alternative — Event Bus for Fire-and-Forget:**
```typescript
// For transient events (notifications, toasts) — not persistent state
@Injectable({ providedIn: 'root' })
export class EventBusService {
  private events = new Subject<AppEvent>();

  emit(event: AppEvent) { this.events.next(event); }

  on<T extends AppEvent>(eventType: string): Observable<T> {
    return this.events.pipe(
      filter(e => e.type === eventType)
    ) as Observable<T>;
  }
}
```

**When to Use What:**
```
Shared persistent state (cart, auth, theme)  → Signal-based service
Transient events (toast, notification)       → Subject-based event bus
Complex state with actions/effects           → NgRx (large teams)
```

**Closing:**
A shared service with Signals is my default for sibling communication. It's simple, testable, and the components stay completely decoupled — neither knows the other exists. They both just talk to the service.

---

## Scenario 7: Form Validation Scenario

> **You have a complex form with dynamic fields and validations. Would you use Template-driven forms or Reactive forms? Why?**

**Summary:**
Reactive Forms — without hesitation. Dynamic fields require programmatic control over the form model, which only Reactive Forms provide. Template-driven forms can't handle `FormArray` (add/remove fields dynamically), cross-field validation, or conditional validators.

**Why Reactive Forms Win Here:**

```typescript
@Component({
  imports: [ReactiveFormsModule],
  template: `
    <form [formGroup]="orderForm" (ngSubmit)="onSubmit()">
      <!-- Static fields -->
      <input formControlName="customerName" placeholder="Customer Name" />
      <select formControlName="orderType" (change)="onOrderTypeChange()">
        <option value="standard">Standard</option>
        <option value="express">Express</option>
        <option value="bulk">Bulk</option>
      </select>

      <!-- Conditional field — appears only for express orders -->
      @if (orderForm.get('orderType')?.value === 'express') {
        <input formControlName="deliveryDate" type="date" />
      }

      <!-- Dynamic line items — add/remove at runtime -->
      <h3>Order Items</h3>
      @for (item of lineItems.controls; track item; let i = $index) {
        <div [formGroupName]="i" class="line-item">
          <input formControlName="productName" placeholder="Product" />
          <input formControlName="quantity" type="number" />
          <input formControlName="price" type="number" />
          <span>Subtotal: {{ getSubtotal(i) | currency }}</span>
          <button type="button" (click)="removeItem(i)">Remove</button>
        </div>
        <!-- Per-item validation messages -->
        @if (item.get('quantity')?.hasError('min')) {
          <small class="error">Quantity must be at least 1</small>
        }
      }

      <button type="button" (click)="addItem()">+ Add Item</button>

      <!-- Cross-field validation -->
      @if (orderForm.hasError('minItems')) {
        <div class="error">Order must have at least 1 item</div>
      }

      <p>Total: {{ total | currency }}</p>
      <button [disabled]="orderForm.invalid">Submit Order</button>
    </form>
  `
})
export class OrderFormComponent {
  private fb = inject(FormBuilder);

  orderForm = this.fb.group({
    customerName: ['', [Validators.required, Validators.minLength(2)]],
    orderType: ['standard', Validators.required],
    deliveryDate: [''],
    items: this.fb.array([], [this.minItemsValidator(1)])  // Dynamic array!
  });

  get lineItems(): FormArray {
    return this.orderForm.get('items') as FormArray;
  }

  get total(): number {
    return this.lineItems.controls.reduce((sum, item) => {
      const qty = item.get('quantity')?.value || 0;
      const price = item.get('price')?.value || 0;
      return sum + qty * price;
    }, 0);
  }

  // Dynamic field — add a line item
  addItem() {
    this.lineItems.push(this.fb.group({
      productName: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      price: [0, [Validators.required, Validators.min(0.01)]]
    }));
  }

  removeItem(index: number) {
    this.lineItems.removeAt(index);
  }

  // Conditional validation — express orders MUST have delivery date
  onOrderTypeChange() {
    const deliveryDate = this.orderForm.get('deliveryDate')!;
    if (this.orderForm.get('orderType')?.value === 'express') {
      deliveryDate.addValidators(Validators.required);
    } else {
      deliveryDate.clearValidators();
    }
    deliveryDate.updateValueAndValidity();
  }

  // Cross-field validator — minimum items
  minItemsValidator(min: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const array = control as FormArray;
      return array.length >= min ? null : { minItems: { required: min, actual: array.length } };
    };
  }

  getSubtotal(index: number): number {
    const item = this.lineItems.at(index);
    return (item.get('quantity')?.value || 0) * (item.get('price')?.value || 0);
  }

  onSubmit() {
    if (this.orderForm.valid) {
      console.log(this.orderForm.value);
    } else {
      this.orderForm.markAllAsTouched();  // Show all validation errors
    }
  }
}
```

**What Reactive Forms Give You That Template-Driven Can't:**
```
✅ FormArray — dynamic add/remove fields at runtime
✅ Conditional validators — add/remove validators based on other fields
✅ Cross-field validation — validate across multiple fields
✅ Pure TypeScript testing — no DOM needed
✅ Programmatic control — setValue, patchValue, reset
✅ Observable streams — valueChanges, statusChanges
✅ Async validators — check email availability via API
```

**Closing:**
For anything beyond a login form, Reactive Forms are the answer. Dynamic fields, conditional validation, cross-field rules, and testability — Reactive Forms handle all of it cleanly. I standardize on Reactive Forms across every project.

---

## Scenario 8: Route Security

> **Some routes should only be accessible to authenticated users. How would you implement this in Angular?**

**Summary:**
I use a functional `canActivate` guard that checks the auth token in the `AuthService`. Unauthenticated users are redirected to `/login` with the return URL preserved for post-login redirect.

**Implementation:**

```typescript
// auth.service.ts
@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUser = signal<User | null>(null);
  readonly isAuthenticated = computed(() => this.currentUser() !== null);

  constructor() {
    // Restore session from storage on app start
    const stored = localStorage.getItem('auth_token');
    if (stored && !this.isTokenExpired(stored)) {
      this.currentUser.set(this.decodeToken(stored));
    }
  }

  isLoggedIn(): boolean {
    const token = localStorage.getItem('auth_token');
    return !!token && !this.isTokenExpired(token);
  }

  private isTokenExpired(token: string): boolean {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 < Date.now();
  }
}

// auth.guard.ts
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  // Redirect to login with return URL
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url }
  });
};

// app.routes.ts
export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', component: HomeComponent },

  // Protected routes
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard.component'),
    canActivate: [authGuard]
  },
  {
    path: 'profile',
    loadComponent: () => import('./profile/profile.component'),
    canActivate: [authGuard]
  },
  {
    path: 'settings',
    loadChildren: () => import('./settings/settings.routes'),
    canActivate: [authGuard]  // Guards all child routes too
  }
];

// login.component.ts — redirect after login
export class LoginComponent {
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  onLoginSuccess() {
    const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
    this.router.navigateByUrl(returnUrl);
  }
}
```

**Closing:**
Functional guard + `UrlTree` redirect + return URL preservation. Simple, clean, and covers 90% of route security needs. Combine with the HTTP interceptor (Scenario 10) for complete auth protection.

---

## Scenario 9: Role-Based Authorization

> **You have Admin and User roles in your application. How would you restrict certain pages only for Admin users?**

**Summary:**
I create a `roleGuard` that reads the required role from route data and checks it against the user's role from the JWT. For UI elements, I use a structural directive `*appHasRole` to show/hide based on role.

**Implementation:**

```typescript
// role.guard.ts
export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const requiredRoles = route.data['roles'] as string[];

  const userRole = authService.getUserRole();

  if (requiredRoles.includes(userRole)) {
    return true;
  }

  return router.createUrlTree(['/unauthorized']);
};

// Routes
{
  path: 'admin',
  loadChildren: () => import('./admin/admin.routes'),
  canActivate: [authGuard, roleGuard],
  data: { roles: ['ADMIN'] }
},
{
  path: 'manager-panel',
  loadComponent: () => import('./manager/manager.component'),
  canActivate: [authGuard, roleGuard],
  data: { roles: ['ADMIN', 'MANAGER'] }  // Multiple roles allowed
}

// Structural directive — hide UI elements by role
@Directive({ selector: '[appHasRole]', standalone: true })
export class HasRoleDirective {
  private authService = inject(AuthService);
  private templateRef = inject(TemplateRef<any>);
  private viewContainer = inject(ViewContainerRef);
  private rendered = false;

  @Input() set appHasRole(roles: string | string[]) {
    const roleArray = Array.isArray(roles) ? roles : [roles];
    const userRole = this.authService.getUserRole();

    if (roleArray.includes(userRole) && !this.rendered) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.rendered = true;
    } else if (!roleArray.includes(userRole) && this.rendered) {
      this.viewContainer.clear();
      this.rendered = false;
    }
  }
}

// Template usage
<button *appHasRole="'ADMIN'" routerLink="/admin">Admin Panel</button>
<button *appHasRole="['ADMIN', 'MANAGER']" (click)="exportReport()">
  Export Report
</button>
```

**Important — Server-Side Validation:**
```
⚠️ Angular guards and directives are CLIENT-SIDE only.
A tech-savvy user can bypass them via browser DevTools.

ALWAYS enforce role checks on the backend:
  → API returns 403 if user doesn't have the required role
  → Angular guards are UX convenience, not security enforcement
```

**Closing:**
Route guard for page-level access, structural directive for element-level visibility, and always enforce on the backend. Client-side guards are UX, not security.

---

## Scenario 10: HTTP Error Handling

> **You are calling multiple APIs in your Angular application. How would you handle global API errors like 401, 403, or 500?**

**Summary:**
I use a functional HTTP interceptor that catches errors globally — 401 triggers token refresh, 403 redirects to unauthorized page, 500 shows a toast notification. Individual components can still handle specific errors on top of this.

**Implementation:**

```typescript
// error.interceptor.ts
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notification = inject(NotificationService);
  const router = inject(Router);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {

      switch (error.status) {
        case 0:
          notification.error('Unable to connect to server. Check your network.');
          break;

        case 401:
          // Token expired — attempt refresh
          if (!req.url.includes('/auth/refresh')) {
            return authService.refreshToken().pipe(
              switchMap(newToken => {
                // Retry original request with new token
                const retryReq = req.clone({
                  setHeaders: { Authorization: `Bearer ${newToken}` }
                });
                return next(retryReq);
              }),
              catchError(() => {
                authService.logout();
                router.navigate(['/login'], {
                  queryParams: { returnUrl: router.url, reason: 'session_expired' }
                });
                return throwError(() => error);
              })
            );
          }
          break;

        case 403:
          notification.error('You do not have permission to perform this action.');
          router.navigate(['/unauthorized']);
          break;

        case 404:
          // Don't show global error — let component handle it
          break;

        case 422:
          // Validation errors — let component handle form-specific errors
          break;

        case 500:
        case 502:
        case 503:
          notification.error('Something went wrong on the server. Please try again.');
          break;

        case 429:
          notification.warning('Too many requests. Please slow down.');
          break;
      }

      return throwError(() => error);  // Re-throw for component-level handling
    })
  );
};

// Register
provideHttpClient(
  withInterceptors([authInterceptor, errorInterceptor])
)
```

**Component-Level Error Handling (on top of global):**
```typescript
// Component handles SPECIFIC errors (404 for "not found" UI)
this.productService.getById(id).pipe(
  catchError((error: HttpErrorResponse) => {
    if (error.status === 404) {
      this.notFound = true;  // Show "Product not found" UI
    }
    return EMPTY;  // Don't re-throw — handled locally
  })
).subscribe(product => this.product = product);
```

**Closing:**
Global interceptor handles auth (401), permissions (403), and server errors (500). Individual components handle domain-specific errors (404, 422). Always re-throw from the interceptor so both layers can act.

---

## Scenario 11: State Management

> **Your application has many components sharing data. How would you manage application state effectively?**

**Summary:**
For most apps, Signal-based services are enough. I use a simple reactive store pattern — one service per feature domain with signals for state and computed for derived data. I reach for NgRx only when the team is large (10+) and state interactions are genuinely complex.

**My Default — Signal-Based Store:**

```typescript
// Lightweight store pattern — no library needed
@Injectable({ providedIn: 'root' })
export class ProductStore {
  // Private state
  private products = signal<Product[]>([]);
  private loading = signal(false);
  private error = signal<string | null>(null);
  private selectedId = signal<number | null>(null);

  // Public read-only selectors
  readonly allProducts = this.products.asReadonly();
  readonly isLoading = this.loading.asReadonly();
  readonly errorMessage = this.error.asReadonly();
  readonly selectedProduct = computed(() => {
    const id = this.selectedId();
    return id ? this.products().find(p => p.id === id) ?? null : null;
  });
  readonly productCount = computed(() => this.products().length);

  private http = inject(HttpClient);

  // Actions
  loadProducts() {
    this.loading.set(true);
    this.error.set(null);

    this.http.get<Product[]>('/api/products').pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: (products) => this.products.set(products),
      error: (err) => this.error.set('Failed to load products')
    });
  }

  selectProduct(id: number) {
    this.selectedId.set(id);
  }

  addProduct(product: Product) {
    this.http.post<Product>('/api/products', product).subscribe(saved => {
      this.products.update(list => [...list, saved]);
    });
  }

  deleteProduct(id: number) {
    this.http.delete(`/api/products/${id}`).subscribe(() => {
      this.products.update(list => list.filter(p => p.id !== id));
    });
  }
}

// Any component — just inject and use
@Component({
  template: `
    @if (store.isLoading()) {
      <app-spinner />
    } @else {
      @for (product of store.allProducts(); track product.id) {
        <app-product-card
          [product]="product"
          [selected]="product.id === store.selectedProduct()?.id"
          (click)="store.selectProduct(product.id)"
          (delete)="store.deleteProduct(product.id)" />
      }
    }
  `
})
export class ProductListComponent {
  store = inject(ProductStore);

  constructor() {
    this.store.loadProducts();
  }
}
```

**Decision Guide:**
```
< 10 components sharing state  → Signal-based service (above pattern)
10-30 components, 1-5 devs     → Signal-based store with clear actions
30+ components, 5+ devs        → NgRx (enforced patterns, DevTools, effects)
```

**Closing:**
Signal-based stores cover 80% of Angular apps. Simple, no boilerplate, fully reactive. I only reach for NgRx when team size and state complexity genuinely demand it.

---

## Scenario 12: API Request Optimization

> **Multiple components are calling the same API repeatedly. How would you avoid duplicate API calls?**

**Summary:**
I use `shareReplay(1)` to cache the Observable and share it across multiple subscribers. For more control, I implement a simple caching layer in the service with TTL (time-to-live) for invalidation.

**Solution 1 — shareReplay (Quick and Effective):**
```typescript
@Injectable({ providedIn: 'root' })
export class ProductService {
  private products$?: Observable<Product[]>;

  getAll(): Observable<Product[]> {
    if (!this.products$) {
      this.products$ = this.http.get<Product[]>('/api/products').pipe(
        shareReplay(1)  // Cache the result, share with all subscribers
      );
    }
    return this.products$;
  }

  // Invalidate cache when data changes
  invalidateCache() {
    this.products$ = undefined;
  }

  create(product: Product): Observable<Product> {
    return this.http.post<Product>('/api/products', product).pipe(
      tap(() => this.invalidateCache())  // Clear cache after mutation
    );
  }
}
```

**Solution 2 — Signal-Based Cache with TTL:**
```typescript
@Injectable({ providedIn: 'root' })
export class CachedProductService {
  private products = signal<Product[]>([]);
  private lastFetch = 0;
  private readonly TTL = 5 * 60 * 1000;  // 5 minutes
  private loading = false;

  readonly allProducts = this.products.asReadonly();

  private http = inject(HttpClient);

  getAll(): Observable<Product[]> {
    const now = Date.now();
    if (this.products().length > 0 && (now - this.lastFetch) < this.TTL) {
      return of(this.products());  // Return cached data
    }

    if (this.loading) return of(this.products());  // Prevent concurrent fetches

    this.loading = true;
    return this.http.get<Product[]>('/api/products').pipe(
      tap(products => {
        this.products.set(products);
        this.lastFetch = Date.now();
        this.loading = false;
      })
    );
  }
}
```

**Solution 3 — HTTP Interceptor Cache (For GET Requests):**
```typescript
export const cacheInterceptor: HttpInterceptorFn = (req, next) => {
  // Only cache GET requests
  if (req.method !== 'GET') return next(req);

  const cache = inject(HttpCacheService);
  const cached = cache.get(req.urlWithParams);

  if (cached) return of(cached);

  return next(req).pipe(
    tap(event => {
      if (event instanceof HttpResponse) {
        cache.set(req.urlWithParams, event, 300_000);  // 5 min TTL
      }
    })
  );
};
```

**Closing:**
`shareReplay(1)` for simple cases, signal-based cache with TTL for fine control, HTTP interceptor cache for app-wide caching. Always invalidate on mutations (POST, PUT, DELETE).

---

## Scenario 13: Loading Indicators

> **You want to display a global loading spinner while API calls are running. How would you implement this?**

**Summary:**
I use an HTTP interceptor to track active requests and a shared `LoadingService` with a signal. The spinner shows when any request is in flight and hides when all complete.

**Implementation:**

```typescript
// loading.service.ts
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private activeRequests = signal(0);
  readonly isLoading = computed(() => this.activeRequests() > 0);

  show() { this.activeRequests.update(c => c + 1); }
  hide() { this.activeRequests.update(c => Math.max(0, c - 1)); }
}

// loading.interceptor.ts
export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  // Skip loading for silent/background requests
  if (req.headers.has('X-Skip-Loading')) {
    return next(req.clone({ headers: req.headers.delete('X-Skip-Loading') }));
  }

  const loading = inject(LoadingService);
  loading.show();

  return next(req).pipe(
    finalize(() => loading.hide())
  );
};

// Register
provideHttpClient(withInterceptors([loadingInterceptor]))

// app.component.ts — global spinner
@Component({
  selector: 'app-root',
  template: `
    @if (loading.isLoading()) {
      <div class="loading-overlay">
        <div class="spinner"></div>
      </div>
    }
    <router-outlet />
  `
})
export class AppComponent {
  loading = inject(LoadingService);
}

// For background requests that shouldn't show spinner:
this.http.get('/api/analytics', {
  headers: new HttpHeaders({ 'X-Skip-Loading': 'true' })
});
```

**Closing:**
Interceptor-based loading with request counting handles concurrent API calls cleanly. The `X-Skip-Loading` header lets background requests opt out. Simple, global, and automatic.

---

## Scenario 14: Secure Token Storage

> **Your application uses JWT authentication. Where would you store the token securely in Angular?**

**Summary:**
There's no perfect option — each has trade-offs. I prefer `HttpOnly cookies` set by the backend (immune to XSS). If cookies aren't an option, I use `localStorage` with strong XSS defenses (Angular's built-in sanitization + CSP headers).

**Options Comparison:**

| Storage | XSS Safe? | CSRF Safe? | Persists? | Best For |
|---------|:---------:|:----------:|:---------:|----------|
| HttpOnly Cookie | Yes | No (need CSRF token) | Yes | Most secure — recommended |
| localStorage | No (JS can read it) | Yes | Yes | Common, needs XSS protection |
| sessionStorage | No | Yes | No (tab only) | Short sessions |
| In-memory (variable) | Yes | Yes | No (lost on refresh) | Highest security, worst UX |

**Recommended: HttpOnly Cookie (Backend Sets It)**
```
// Backend (Spring Boot) sets the cookie on login response:
Set-Cookie: access_token=eyJhbG...; HttpOnly; Secure; SameSite=Strict; Path=/api

// Angular NEVER sees or touches the token
// Browser automatically sends it with every request to /api
// XSS attack CANNOT read it — HttpOnly blocks JavaScript access

// CSRF protection:
// Backend also sets a non-HttpOnly CSRF token cookie
// Angular reads it and sends it as X-XSRF-TOKEN header (automatic)
```

```typescript
// Angular config — just enable credentials
provideHttpClient(
  withXsrfConfiguration({
    cookieName: 'XSRF-TOKEN',
    headerName: 'X-XSRF-TOKEN'
  })
)

// HttpClient sends withCredentials for cross-origin if needed
this.http.get('/api/data', { withCredentials: true });
```

**If HttpOnly Cookies Aren't an Option — localStorage:**
```typescript
@Injectable({ providedIn: 'root' })
export class TokenService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly REFRESH_KEY = 'refresh_token';

  saveTokens(access: string, refresh: string): void {
    localStorage.setItem(this.TOKEN_KEY, access);
    localStorage.setItem(this.REFRESH_KEY, refresh);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  clearTokens(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
  }

  isTokenExpired(): boolean {
    const token = this.getAccessToken();
    if (!token) return true;
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 < Date.now();
  }
}
```

**Hardening localStorage Approach:**
```
□ Angular's built-in XSS sanitization (auto-escapes bindings) — never bypass
□ Content Security Policy headers — block inline scripts
   Content-Security-Policy: script-src 'self';
□ Short-lived access tokens (15 min) + refresh token rotation
□ Never store sensitive data in the token payload
□ Always use HTTPS
□ Subresource Integrity (SRI) for CDN scripts
```

**Closing:**
HttpOnly cookies are the most secure — the token is invisible to JavaScript. If you must use localStorage, lean on Angular's XSS protection, short-lived tokens, and CSP headers. Never store tokens in sessionStorage for multi-tab apps.

---

## Scenario 15: Application Performance Optimization

> **Your Angular application is slow in production. What techniques would you use to optimize the performance?**

**Summary:**
I approach this systematically: measure first (Lighthouse, Chrome DevTools), then optimize in order of impact — bundle size, change detection, rendering, network, and runtime. The biggest wins are always lazy loading, OnPush, and @defer.

**Step 1 — Measure Before Optimizing:**
```
Tools:
├── Lighthouse (Chrome DevTools → Lighthouse tab)
│   → LCP, FID, CLS scores + actionable recommendations
├── Chrome Performance tab
│   → Record user interaction, identify long tasks (> 50ms)
├── Angular DevTools (Chrome extension)
│   → Profiler → see CD cycles per component
├── Bundle Analyzer
│   → ng build --stats-json && npx webpack-bundle-analyzer dist/stats.json
└── Runtime: Performance.mark() / Performance.measure()
```

**Step 2 — Fix by Category (Highest Impact First):**

**A. Bundle Size (Biggest Impact on Load Time):**
```
□ Lazy load ALL feature routes
    loadComponent: () => import('./feature.component')

□ Use @defer for below-the-fold content
    @defer (on viewport) { <app-heavy-chart /> }

□ Replace heavy libraries:
    moment.js (300KB) → date-fns (~10KB tree-shaken)
    lodash (70KB) → lodash-es or native JS
    chart.js (200KB) → @defer load it when visible

□ Analyze and remove unused code
    → Bundle analyzer shows what's bloating main.js

□ Performance budgets in angular.json
    "budgets": [{ "type": "initial", "maximumError": "1mb" }]
```

**B. Change Detection (Biggest Impact on Runtime):**
```typescript
// ✅ OnPush on EVERY component
@Component({ changeDetection: ChangeDetectionStrategy.OnPush })

// ✅ Signals for state (fine-grained updates)
count = signal(0);
filtered = computed(() => this.items().filter(i => i.active));

// ✅ async pipe for Observables (auto markForCheck)
{{ data$ | async }}

// ❌ NEVER: functions in templates
<p>{{ calculateTotal() }}</p>  // Runs every CD cycle!
// ✅ Use pipe or computed signal
<p>{{ items | totalPipe }}</p>
<p>{{ total() }}</p>
```

**C. Rendering (Biggest Impact on UI Responsiveness):**
```html
<!-- ✅ track in @for -->
@for (item of items(); track item.id) { ... }

<!-- ✅ Virtual scrolling for large lists -->
<cdk-virtual-scroll-viewport itemSize="48">
  <div *cdkVirtualFor="let item of items">{{ item.name }}</div>
</cdk-virtual-scroll-viewport>

<!-- ✅ NgOptimizedImage for images -->
<img ngSrc="hero.jpg" width="800" height="400" priority />

<!-- ✅ @defer for heavy components -->
@defer (on viewport) {
  <app-analytics-dashboard />
} @placeholder {
  <div class="skeleton"></div>
}
```

**D. Network (Reduce API Overhead):**
```typescript
// ✅ Cache API responses
getProducts(): Observable<Product[]> {
  if (!this.cache$) {
    this.cache$ = this.http.get<Product[]>('/api/products').pipe(
      shareReplay(1)
    );
  }
  return this.cache$;
}

// ✅ Preload critical routes in background
provideRouter(routes, withPreloading(SelectivePreloadStrategy))

// ✅ SSR for SEO-critical pages
provideClientHydration()

// ✅ Service Worker for offline + caching
ng add @angular/pwa
```

**E. Production Build Settings:**
```json
// angular.json — production configuration
{
  "outputHashing": "all",
  "aot": true,
  "optimization": true,
  "sourceMap": false,
  "extractLicenses": true,
  "budgets": [
    { "type": "initial", "maximumWarning": "500kb", "maximumError": "1mb" },
    { "type": "anyComponentStyle", "maximumWarning": "2kb" }
  ]
}
```

**Performance Checklist Summary:**
```
Bundle Size:
  □ Lazy loading all feature routes
  □ @defer for below-the-fold components
  □ Tree-shake heavy libraries
  □ Set and enforce performance budgets

Change Detection:
  □ OnPush on every component
  □ Signals / async pipe for reactivity
  □ No functions in templates — use pipes/computed

Rendering:
  □ track in @for loops
  □ Virtual scrolling for 500+ items
  □ NgOptimizedImage for all images

Network:
  □ shareReplay for cached API responses
  □ Preload high-traffic lazy routes
  □ SSR for public-facing pages
  □ Service Worker for caching

Build:
  □ AOT compilation (default)
  □ Production optimizations enabled
  □ Source maps disabled in production
```

**Real-World Impact:**

| Optimization | Metric | Before | After |
|-------------|--------|--------|-------|
| Lazy loading | Initial bundle | 3.2 MB | 600 KB |
| OnPush + Signals | CD cycles/event | 200 | 5-10 |
| @defer | LCP | 3.1s | 1.3s |
| Virtual scrolling | Render time (10K rows) | 8s | Instant |
| NgOptimizedImage | LCP | 2.8s | 1.5s |
| SSR + Hydration | FCP | 3.5s | 0.8s |
| shareReplay caching | API calls | 15/page | 3/page |

**Closing:**
Performance optimization is systematic — measure, identify the bottleneck, fix, verify. The biggest wins are always: lazy loading (bundle), OnPush + Signals (CD), and @defer (rendering). I set performance budgets in `angular.json` to prevent regression and profile regularly with Angular DevTools.

---

## Quick Reference — Scenario Decision Matrix

| Scenario | First Action | Key Pattern | Tool/API |
|----------|-------------|-------------|----------|
| 1. Large table | Virtual scrolling | CDK ScrollingModule | `cdk-virtual-scroll-viewport` |
| 2. UI not updating | Check OnPush + mutation | Signals / async pipe | `ChangeDetectionStrategy.OnPush` |
| 3. Large bundle | Lazy load routes | `loadComponent()` | Bundle analyzer |
| 4. Memory leak | Heap snapshot | `takeUntilDestroyed()` | Chrome DevTools Memory |
| 5. Parent-child comms | @Input + @Output | Signal inputs, `model()` | `EventEmitter` |
| 6. Sibling comms | Shared service | Signal-based store | `inject(SharedService)` |
| 7. Complex forms | Reactive Forms | FormArray + validators | `ReactiveFormsModule` |
| 8. Route security | Auth guard | `canActivate` | `CanActivateFn` |
| 9. Role-based access | Role guard + directive | `canActivate` + `*appHasRole` | Route `data.roles` |
| 10. Global errors | HTTP interceptor | Error interceptor | `HttpInterceptorFn` |
| 11. State management | Signal-based store | Service + signals | `signal()` + `computed()` |
| 12. Duplicate APIs | Cache in service | `shareReplay(1)` | TTL-based cache |
| 13. Loading spinner | Interceptor + counter | `LoadingService` | `finalize()` |
| 14. Token storage | HttpOnly cookies | Backend-set cookie | `withXsrfConfiguration()` |
| 15. Slow app | Measure → fix bottleneck | Lazy + OnPush + @defer | Lighthouse + Angular DevTools |
