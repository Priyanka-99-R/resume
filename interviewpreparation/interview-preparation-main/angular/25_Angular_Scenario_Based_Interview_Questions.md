# 25 Angular Scenario-Based Interview Questions (5+ Years Experience)

---

## Performance Scenarios

---

### 1. Your Angular application becomes slow when displaying 10,000 records. How would you optimize it?

**Summary:** The core fix is to never render 10,000 DOM elements at once. Virtual scrolling, pagination, or server-side filtering are the real solutions — not just making rendering faster.

**My approach in order of impact:**

**1. Virtual Scrolling (CDK):**
```typescript
import { CdkVirtualScrollViewport } from '@angular/cdk/scrolling';

// Template
<cdk-virtual-scroll-viewport itemSize="48" class="viewport">
  <div *cdkVirtualFor="let item of items; trackBy: trackById">
    {{ item.name }}
  </div>
</cdk-virtual-scroll-viewport>
```
This renders only ~20-30 visible items in the DOM at any time, regardless of the list size. Went from 10,000 DOM nodes to ~30.

**2. OnPush + trackBy:**
```typescript
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ListComponent {
  trackById(index: number, item: Item): number {
    return item.id;
  }
}
```

**3. Server-side pagination/filtering:**
```typescript
loadPage(page: number) {
  this.http.get(`/api/items?page=${page}&size=50`).subscribe(data => {
    this.items = data;
  });
}
```
Don't fetch 10,000 records if the user only sees 50 at a time.

**4. Defer heavy content:**
```html
@defer (on viewport) {
  <app-heavy-chart [data]="item.metrics"></app-heavy-chart>
} @placeholder {
  <div class="skeleton"></div>
}
```

**5. Web Workers for computation:**
If sorting/filtering 10K records on the client, offload to a Web Worker so the UI thread stays responsive.

**Real-world result:** Dashboard with 15,000 transaction rows — virtual scrolling + server pagination brought render time from 6 seconds to under 200ms. Users never noticed the data wasn't all in the DOM.

**Common mistake:** Trying to optimize rendering of 10,000 DOM elements instead of questioning why 10,000 elements exist in the DOM at all.

> The best DOM node is one that doesn't exist. Virtualize, paginate, or filter server-side — pick at least one.

---

### 2. An API is being called multiple times unnecessarily in your component. How would you prevent this?

**Summary:** This almost always happens because of multiple subscriptions to the same Observable, template method calls triggering on every CD cycle, or missing `shareReplay`. The fix depends on the root cause.

**Scenario 1 — Multiple subscribers to the same HTTP call:**
```typescript
// PROBLEM: Each async pipe creates a new subscription = new HTTP call
// Template: {{ user$ | async }} and {{ user$ | async }} = 2 API calls

// FIX: shareReplay
user$ = this.http.get<User>('/api/user').pipe(
  shareReplay({ bufferSize: 1, refCount: true })
);
```

**Scenario 2 — Method call in template:**
```typescript
// PROBLEM: getUser() called on every change detection cycle
// <div>{{ getUser() }}</div>

// FIX: Use a Signal or assign to a property in ngOnInit
user = signal<User | null>(null);

ngOnInit() {
  this.http.get<User>('/api/user').subscribe(u => this.user.set(u));
}
// Template: {{ user()?.name }}
```

**Scenario 3 — Component reinitializing (route changes):**
```typescript
// FIX: Cache in a service
@Injectable({ providedIn: 'root' })
export class UserService {
  private cache = new Map<string, User>();

  getUser(id: string): Observable<User> {
    if (this.cache.has(id)) {
      return of(this.cache.get(id)!);
    }
    return this.http.get<User>(`/api/users/${id}`).pipe(
      tap(user => this.cache.set(id, user))
    );
  }
}
```

**Scenario 4 — Parent re-renders child, child calls API in ngOnInit:**
```typescript
// FIX: Move the API call to the parent, pass data via @Input
// Or use OnPush on the child so it doesn't reinitialize unnecessarily
```

**How I debug it:** Open Chrome DevTools Network tab, filter by XHR, reproduce the issue. Look for duplicate requests to the same endpoint. Then trace back to the subscription source.

**Common mistake:** Using `shareReplay` without `refCount: true` — this keeps the subscription alive even after all subscribers unsubscribe, causing memory leaks.

> Before fixing, identify *why* it's called multiple times. The symptom is the same but the fix depends entirely on the root cause.

---

### 3. A component re-renders frequently even when data does not change. How do you fix it?

**Summary:** This is almost always a change detection issue — either the component is using Default strategy, or something is triggering unnecessary CD cycles. The fix is OnPush, immutable data, and eliminating CD triggers.

**Step 1 — Diagnose with Angular DevTools:**
Open the Profiler tab in Angular DevTools. Record a session and see which components are being checked and why. Look for components that show CD runs without actual data changes.

**Step 2 — Switch to OnPush:**
```typescript
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MyComponent {
  @Input() data!: Data; // Only re-renders when reference changes
}
```

**Step 3 — Eliminate common CD triggers:**

```typescript
// PROBLEM: Method calls in template — called every CD cycle
<div>{{ formatDate(item.date) }}</div>

// FIX: Use a pure pipe
<div>{{ item.date | dateFormat }}</div>

// PROBLEM: setInterval or setTimeout triggering Zone.js
setInterval(() => this.checkSomething(), 1000);

// FIX: Run outside Angular zone
constructor(private ngZone: NgZone) {}
ngOnInit() {
  this.ngZone.runOutsideAngular(() => {
    setInterval(() => {
      // Only trigger CD when actually needed
      if (this.hasChanged()) {
        this.ngZone.run(() => this.update());
      }
    }, 1000);
  });
}
```

**Step 4 — Ensure immutable data patterns:**
```typescript
// WRONG: Mutating existing object — OnPush won't detect
this.user.name = 'New Name';

// RIGHT: Create new reference
this.user = { ...this.user, name: 'New Name' };
```

**Step 5 — Use Signals for fine-grained reactivity:**
```typescript
userName = signal('John');
// Template: {{ userName() }}
// Only this binding updates, not the entire component template
```

**Common mistake:** Adding `ngDoCheck` or `detectChanges()` as a workaround. This masks the real problem and can make performance worse.

> The answer is always: OnPush + immutable data + pure pipes. If a component re-renders without data changes, something is violating these principles.

---

### 4. You notice high change detection cycles in Angular DevTools. What steps would you take?

**Summary:** High CD cycles mean Angular is doing more work than necessary. The investigation starts with profiling, then systematically eliminating triggers — it's detective work with a clear methodology.

**My systematic approach:**

**Step 1 — Profile and identify the offenders:**
- Open Angular DevTools → Profiler tab
- Record while interacting with the app
- Sort components by "number of checks" — the top ones are your targets
- Note which user actions trigger the most cycles

**Step 2 — Categorize the triggers:**

| Trigger | Symptom | Fix |
|---|---|---|
| Default CD strategy | Every component checked every cycle | Switch to OnPush |
| Template method calls | CD runs the method every cycle | Pure pipes or computed signals |
| Frequent DOM events | mousemove, scroll firing CD | `runOutsideAngular` |
| setInterval/setTimeout | Zone.js patches trigger CD | Run outside zone |
| Excessive async pipe | Multiple subscriptions | `shareReplay`, single subscription |
| Third-party libraries | jQuery/chart libs triggering events | Run outside zone |

**Step 3 — Apply fixes progressively:**

```typescript
// 1. OnPush on all components
@Component({ changeDetection: ChangeDetectionStrategy.OnPush })

// 2. Move frequent events outside Zone
this.ngZone.runOutsideAngular(() => {
  fromEvent(window, 'scroll').pipe(
    throttleTime(100),
    filter(() => this.isNearBottom())
  ).subscribe(() => {
    this.ngZone.run(() => this.loadMore());
  });
});

// 3. Replace template methods with pipes or signals
// Before: {{ calculateTotal(items) }}
// After:  {{ items | calculateTotal }}
// Or:     {{ total() }}  // computed signal
```

**Step 4 — Consider zoneless (Angular 18+):**
```typescript
bootstrapApplication(AppComponent, {
  providers: [provideZonelessChangeDetection()]
});
// Now CD only runs when Signals change — maximum control
```

**Step 5 — Verify the fix:**
Re-profile with Angular DevTools. Compare before/after CD cycle counts. Target: components should only be checked when their data actually changes.

**Common mistake:** Randomly adding `ChangeDetectorRef.detach()` everywhere without understanding why CD is running. This creates bugs where the UI doesn't update when it should.

> Treat high CD cycles like a performance bug — profile first, hypothesize, fix, measure. Never guess.

---

### 5. Your application bundle size becomes 8MB. How do you reduce it?

**Summary:** An 8MB bundle means you're shipping code the user doesn't need upfront. The fix is a combination of analysis, lazy loading, tree-shaking, and replacing bloated dependencies.

**Step 1 — Analyze what's in the bundle:**
```bash
ng build --configuration production --stats-json
npx webpack-bundle-analyzer dist/my-app/stats.json
# Or
npx source-map-explorer dist/my-app/main.*.js
```
This gives you a visual treemap. Typically 60-70% of the problem is third-party libraries.

**Step 2 — Lazy load routes (biggest single win):**
```typescript
// Before: Everything in main bundle
import { AdminModule } from './admin/admin.module';

// After: Loaded on demand
{
  path: 'admin',
  loadChildren: () => import('./admin/admin.routes').then(m => m.ADMIN_ROUTES)
}
```
If you have 10 feature areas, only the landing page needs to be in the initial bundle.

**Step 3 — Replace heavy libraries:**

| Library | Size | Replacement | Size |
|---|---|---|---|
| moment.js | ~300KB | date-fns (tree-shaked) | ~12KB |
| lodash | ~70KB | lodash-es (tree-shaked) | ~5KB used |
| chart.js (full) | ~200KB | Lightweight alternative or lazy load | varies |
| Angular Material (full) | ~500KB | Import only used modules | ~50KB |

**Step 4 — Tree-shake properly:**
```typescript
// WRONG: Imports entire library
import * as _ from 'lodash';

// RIGHT: Import only what you use
import { debounce } from 'lodash-es';
```

**Step 5 — Use `@defer` for below-the-fold content (Angular 17+):**
```html
@defer (on viewport) {
  <app-heavy-dashboard-widget />
} @placeholder {
  <div class="skeleton-loader"></div>
}
```

**Step 6 — Remove Zone.js if using Signals (saves ~100KB):**
```typescript
bootstrapApplication(AppComponent, {
  providers: [provideZonelessChangeDetection()]
});
```

**Step 7 — Ensure production build optimizations:**
```bash
ng build --configuration production
# Enables: AOT, minification, dead code elimination, tree-shaking
```

**Step 8 — Enable compression at the server:**
- Brotli compression can reduce 8MB to ~1.5MB over the wire
- gzip as fallback

**Real-world result:** 8MB app analyzed — moment.js (300KB), full lodash (70KB), unoptimized Angular Material (400KB), zero lazy loading. After fixes: initial bundle 1.2MB, lazy chunks totaling 2MB loaded on demand. With Brotli: 350KB initial transfer.

> Always start with the bundle analyzer. Don't guess — the data will tell you exactly where those 8MB are hiding.

---

## Architecture Scenarios

---

### 6. You need to share data between multiple unrelated components. How would you implement it?

**Summary:** A shared service with Signals or BehaviorSubject is the cleanest solution for unrelated components. It acts as a single source of truth without coupling the components to each other.

**Approach 1 — Shared Service with Signals (preferred, Angular 16+):**
```typescript
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private _notifications = signal<Notification[]>([]);
  readonly notifications = this._notifications.asReadonly();
  readonly unreadCount = computed(() =>
    this._notifications().filter(n => !n.read).length
  );

  add(notification: Notification) {
    this._notifications.update(list => [notification, ...list]);
  }

  markAsRead(id: string) {
    this._notifications.update(list =>
      list.map(n => n.id === id ? { ...n, read: true } : n)
    );
  }
}

// Component A — Header (shows count)
@Component({ template: `<span>{{ notifService.unreadCount() }}</span>` })
export class HeaderComponent {
  notifService = inject(NotificationService);
}

// Component B — Sidebar (shows list)
@Component({ template: `
  @for (n of notifService.notifications(); track n.id) {
    <div (click)="notifService.markAsRead(n.id)">{{ n.message }}</div>
  }
`})
export class SidebarComponent {
  notifService = inject(NotificationService);
}
```

**Approach 2 — BehaviorSubject (pre-Signals or complex async):**
```typescript
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private theme$ = new BehaviorSubject<'light' | 'dark'>('light');
  currentTheme$ = this.theme$.asObservable();

  toggle() {
    this.theme$.next(this.theme$.value === 'light' ? 'dark' : 'light');
  }
}
```

**When NOT to use these:**
- **Parent-child** → Use `@Input()` / `@Output()` — simpler, explicit data flow
- **Complex enterprise state** → Use NgRx — when you need time-travel debugging, devtools, effects
- **One-time event** → Use a Subject (not BehaviorSubject)

**Common mistake:** Using `@Output()` with EventEmitter and bubbling events through 5 levels of components. If components are more than 2 levels apart, use a service.

> A shared service is the Angular-idiomatic way to communicate between unrelated components. It's simple, testable, and doesn't couple your component tree.

---

### 7. You need global state management for a large Angular application. Which approach would you choose?

**Summary:** For a large enterprise app with complex state, I'd use NgRx Store with a clear folder structure — but I'd only introduce it where the complexity justifies it, keeping simpler features on service-based state.

**My decision framework:**

| Complexity | Approach | When |
|---|---|---|
| Simple | Services + Signals | < 5 shared state slices, minimal side effects |
| Medium | NgRx Component Store | Feature-level state with moderate side effects |
| Complex | NgRx Global Store | Cross-feature state, many async flows, audit needs |

**For a large app, I'd use NgRx with this structure:**

```
src/
  app/
    store/
      auth/
        auth.actions.ts
        auth.reducer.ts
        auth.effects.ts
        auth.selectors.ts
      orders/
        orders.actions.ts
        orders.reducer.ts
        orders.effects.ts
        orders.selectors.ts
```

**Example — Auth state:**
```typescript
// auth.actions.ts
export const login = createAction('[Auth] Login', props<{ credentials: Credentials }>());
export const loginSuccess = createAction('[Auth] Login Success', props<{ user: User }>());
export const loginFailure = createAction('[Auth] Login Failure', props<{ error: string }>());

// auth.reducer.ts
export const authReducer = createReducer(
  initialState,
  on(loginSuccess, (state, { user }) => ({ ...state, user, isAuthenticated: true })),
  on(loginFailure, (state, { error }) => ({ ...state, error, isAuthenticated: false }))
);

// auth.effects.ts
login$ = createEffect(() => this.actions$.pipe(
  ofType(login),
  exhaustMap(({ credentials }) =>
    this.authService.login(credentials).pipe(
      map(user => loginSuccess({ user })),
      catchError(error => of(loginFailure({ error: error.message })))
    )
  )
));

// auth.selectors.ts
export const selectCurrentUser = createSelector(selectAuthState, state => state.user);
export const selectIsAdmin = createSelector(selectCurrentUser, user => user?.role === 'ADMIN');
```

**But I wouldn't put everything in NgRx:**
- Form state → Reactive Forms (local to component)
- UI state (modal open/close) → Component signals
- Simple shared state (theme, language) → Service + Signal
- Complex async flows with side effects → NgRx

**Why NgRx for large apps:**
- **Predictability** — Unidirectional data flow, immutable state
- **Debugging** — Time-travel debugging with Redux DevTools
- **Team scalability** — Clear patterns, every developer knows where state lives
- **Testability** — Pure functions (reducers, selectors) are trivial to test

**Common mistake:** Putting *everything* in NgRx, including form state and UI toggles. This creates massive boilerplate for no benefit. NgRx is for shared, complex, server-synced state.

> Use the right tool at the right level. NgRx for the complex core, services with Signals for the simple periphery. Don't force everything through one pattern.

---

### 8. A large Angular project becomes difficult to maintain. How would you restructure the architecture?

**Summary:** I'd restructure around domain-driven feature boundaries with clear separation between core, shared, and feature code — combined with strict linting rules and an Nx monorepo for enforcement.

**The target architecture:**

```
src/
  app/
    core/                    # Singletons — loaded once at root
      guards/
      interceptors/
      services/              # AuthService, ConfigService
      core.module.ts

    shared/                  # Reusable, stateless components/pipes/directives
      components/
        data-table/
        confirm-dialog/
      pipes/
      directives/
      shared.module.ts

    features/                # Domain-bounded feature areas
      user-management/
        components/
        services/
        models/
        user-management.routes.ts
      order-processing/
        components/
        services/
        models/
        order-processing.routes.ts
      dashboard/
        ...

    layouts/                 # Shell layouts (admin, public)
      admin-layout/
      public-layout/
```

**Step-by-step migration approach:**

**Step 1 — Audit and categorize:**
Map every component, service, and module into core, shared, or a feature domain. Identify cross-cutting concerns.

**Step 2 — Enforce boundaries:**
```json
// .eslintrc.json (or use Nx enforce-module-boundaries)
{
  "rules": {
    "@nx/enforce-module-boundaries": [
      "error",
      {
        "depConstraints": [
          { "sourceTag": "type:feature", "onlyDependOnLibsWithTags": ["type:shared", "type:core"] },
          { "sourceTag": "type:shared", "onlyDependOnLibsWithTags": ["type:shared"] },
          { "sourceTag": "type:core", "onlyDependOnLibsWithTags": ["type:shared"] }
        ]
      }
    ]
  }
}
```
Features can depend on shared and core, but never on other features.

**Step 3 — Break circular dependencies:**
If Feature A imports from Feature B, extract the shared piece into `shared/` or create an event-based communication via a service.

**Step 4 — Migrate to standalone components:**
Reduces module boilerplate, makes dependencies explicit per-component.

**Step 5 — Introduce lazy loading per feature:**
Each feature area becomes a lazy-loaded route, reducing coupling and initial load time.

**Step 6 — Standardize patterns:**
- One service per API domain
- Feature-level state management (Component Store or service)
- Shared UI components are stateless and input-driven
- Naming conventions enforced via linting

**Common mistake:** Doing a "big bang" restructure. Migrate incrementally — one feature at a time. Keep the app working at every step.

> Architecture problems are people problems. The restructure only works if you enforce boundaries with tooling (Nx, ESLint) — otherwise, developers will cross boundaries again within a month.

---

### 9. You want to migrate from NgModules to standalone components. What steps would you follow?

**Summary:** Migrate incrementally from leaf components inward, using Angular's migration schematic as a starting point, and keep the app working at every step — never do a big-bang migration.

**Step-by-step approach:**

**Step 1 — Run Angular's migration schematic:**
```bash
ng generate @angular/core:standalone
```
This automated tool handles three phases:
1. Convert components, directives, and pipes to standalone
2. Remove unnecessary NgModules
3. Bootstrap the app using standalone APIs

**Step 2 — Start with leaf components (no children):**
```typescript
// Before
@Component({
  selector: 'app-button',
  templateUrl: './button.component.html'
})
export class ButtonComponent {}

// After
@Component({
  selector: 'app-button',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './button.component.html'
})
export class ButtonComponent {}
```
Remove the component from its module's `declarations` and add it to the module's `imports` instead.

**Step 3 — Move to parent components:**
```typescript
@Component({
  standalone: true,
  imports: [ButtonComponent, CommonModule, RouterModule],
  // ...
})
export class FormComponent {}
```

**Step 4 — Convert routes to standalone:**
```typescript
// Before
{ path: 'admin', loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule) }

// After
{ path: 'admin', loadChildren: () => import('./admin/admin.routes').then(m => m.ADMIN_ROUTES) }
```

**Step 5 — Migrate bootstrap:**
```typescript
// Before (main.ts)
platformBrowserDynamic().bootstrapModule(AppModule);

// After (main.ts)
bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAnimations()
  ]
});
```

**Step 6 — Remove empty modules:**
Once all components are standalone, delete modules that serve no purpose.

**Key rules:**
- Migrate one feature at a time
- Run tests after each migration step
- Standalone and module-based components can coexist — no rush
- Use `importProvidersFrom()` for libraries that still need NgModules

**Common mistake:** Trying to migrate the entire app in one PR. This makes code review impossible and creates merge conflicts for everyone.

> Incremental migration is the only sane approach. Angular explicitly supports hybrid mode — use it. One feature per sprint, tests green at every step.

---

### 10. Multiple developers are working on the same Angular project. How would you organize the folder structure?

**Summary:** Organize by feature domain with clear boundaries between core, shared, and feature code. Use an Nx monorepo or strict linting rules so that developers can work on separate features without stepping on each other.

**The structure I'd implement:**

```
src/
  app/
    core/                           # App-wide singletons (loaded once)
      auth/
        auth.service.ts
        auth.guard.ts
        auth.interceptor.ts
      config/
        app-config.service.ts
      layout/
        header.component.ts
        sidebar.component.ts

    shared/                         # Reusable, stateless building blocks
      components/
        data-table/
        search-bar/
        confirm-dialog/
      directives/
        click-outside.directive.ts
      pipes/
        date-format.pipe.ts
      models/
        api-response.model.ts
      shared.module.ts              # Or barrel export for standalone

    features/                       # One folder per business domain
      user-management/
        components/
          user-list/
          user-detail/
          user-form/
        services/
          user.service.ts
        models/
          user.model.ts
        store/                      # Feature-level state if needed
        user-management.routes.ts
      inventory/
        components/
        services/
        models/
        inventory.routes.ts
      reporting/
        ...

    app.component.ts
    app.routes.ts
    app.config.ts
```

**Key principles:**

1. **Feature isolation:** Developer A works in `features/user-management/`, Developer B in `features/inventory/`. Minimal conflicts.

2. **Shared is stateless:** Shared components take `@Input()` and emit `@Output()`. No service injection, no business logic.

3. **Core is singleton:** Services in `core/` are `providedIn: 'root'`. Auth, config, layout — things that exist once.

4. **Barrel exports per feature:**
```typescript
// features/user-management/index.ts
export { UserManagementRoutes } from './user-management.routes';
export { UserService } from './services/user.service';
```

5. **Enforce boundaries with tooling:**
```bash
# Nx workspace — each feature is a library
nx generate @nx/angular:library user-management --directory=features
nx generate @nx/angular:library shared --directory=libs
```

6. **Naming conventions:**
- Components: `user-list.component.ts`
- Services: `user.service.ts`
- Models: `user.model.ts`
- Routes: `feature-name.routes.ts`

**For a team of 10+ developers, use Nx:**
- Each feature is a separate library with its own `project.json`
- `nx affected` only builds/tests changed features
- Module boundary rules prevent cross-feature imports
- Parallel builds and caching speed up CI

**Common mistake:** Organizing by technical type (`components/`, `services/`, `pipes/`) at the top level. This forces developers to touch multiple top-level folders for every feature — creating merge conflicts.

> Structure by domain, not by file type. A developer should be able to build an entire feature without leaving their feature folder. That's the test of a good architecture.

---

## Routing Scenarios

---

### 11. How do you protect routes based on user roles (Admin/User)?

**Summary:** Use functional route guards that check the user's role from the auth service and redirect unauthorized users — but always remember this is a UX convenience, not a security boundary.

**Implementation:**

**1. Auth Service with role checking:**
```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUser = signal<User | null>(null);

  isAuthenticated(): boolean {
    return this.currentUser() !== null;
  }

  hasRole(role: string): boolean {
    return this.currentUser()?.roles.includes(role) ?? false;
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.some(r => this.hasRole(r));
  }
}
```

**2. Reusable functional guards:**
```typescript
// auth.guard.ts
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.isAuthenticated() ? true : router.createUrlTree(['/login']);
};

// role.guard.ts
export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (!auth.isAuthenticated()) {
      return router.createUrlTree(['/login']);
    }

    return auth.hasAnyRole(allowedRoles)
      ? true
      : router.createUrlTree(['/unauthorized']);
  };
};
```

**3. Apply to routes:**
```typescript
export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'unauthorized', component: UnauthorizedComponent },

  // Any authenticated user
  {
    path: 'dashboard',
    canActivate: [authGuard],
    component: DashboardComponent
  },

  // Admin only
  {
    path: 'admin',
    canActivate: [roleGuard(['ADMIN'])],
    loadComponent: () => import('./admin/admin.component')
      .then(m => m.AdminComponent)
  },

  // Admin or Manager
  {
    path: 'reports',
    canActivate: [roleGuard(['ADMIN', 'MANAGER'])],
    loadChildren: () => import('./reports/reports.routes')
      .then(m => m.REPORT_ROUTES)
  },

  // Protect all children of a route
  {
    path: 'settings',
    canActivateChild: [authGuard],
    children: [
      { path: 'profile', component: ProfileComponent },
      { path: 'security', component: SecurityComponent, canActivate: [roleGuard(['ADMIN'])] }
    ]
  }
];
```

**4. Hide UI elements based on role:**
```html
@if (authService.hasRole('ADMIN')) {
  <a routerLink="/admin">Admin Panel</a>
}
```

**Critical reminder:** Guards only protect the client-side route. The backend API **must** validate the JWT and check roles on every request. A determined user can bypass any client-side guard.

**Common mistake:** Only checking `isAuthenticated` in guards without checking specific roles. This gives every logged-in user access to admin pages.

> Guards are for user experience — show the right pages to the right people. Real security is server-side. Implement both layers, test both layers.

---

### 12. How would you implement lazy loading for feature modules?

**Summary:** Lazy loading splits your app into chunks that load on demand when the user navigates to a route. With standalone components, it's as simple as `loadComponent` or `loadChildren` in your route config.

**Standalone approach (Angular 17+):**

```typescript
// app.routes.ts
export const routes: Routes = [
  { path: '', component: HomeComponent },

  // Lazy load a single component
  {
    path: 'profile',
    loadComponent: () => import('./features/profile/profile.component')
      .then(m => m.ProfileComponent)
  },

  // Lazy load a feature with child routes
  {
    path: 'admin',
    loadChildren: () => import('./features/admin/admin.routes')
      .then(m => m.ADMIN_ROUTES)
  }
];

// features/admin/admin.routes.ts
export const ADMIN_ROUTES: Routes = [
  { path: '', component: AdminDashboardComponent },
  { path: 'users', component: UserManagementComponent },
  { path: 'settings', component: AdminSettingsComponent }
];
```

**Module-based approach (legacy):**
```typescript
{
  path: 'admin',
  loadChildren: () => import('./features/admin/admin.module')
    .then(m => m.AdminModule)
}
```

**Verifying it works:**
```bash
ng build --configuration production
# Check the output — you should see separate chunk files:
# main.js          (initial)
# chunk-ADMIN.js   (lazy)
# chunk-PROFILE.js (lazy)
```

In Chrome DevTools Network tab, navigate to `/admin` and you'll see the chunk file load on demand.

**Nested lazy loading:**
```typescript
// admin.routes.ts
export const ADMIN_ROUTES: Routes = [
  { path: '', component: AdminDashboardComponent },
  {
    path: 'analytics',
    loadChildren: () => import('./analytics/analytics.routes')
      .then(m => m.ANALYTICS_ROUTES)
  }
];
```

**Common mistake:** Importing a lazy-loaded component or module anywhere else in the app. If `AdminComponent` is imported in `AppModule`, the bundler includes it in the main bundle — defeating lazy loading entirely.

```typescript
// WRONG: This breaks lazy loading!
import { AdminComponent } from './features/admin/admin.component';
```

> Lazy loading is the single biggest optimization for initial load time. Every feature route should be lazy loaded — no exceptions.

---

### 13. You want to redirect users after login based on their role. How would you implement this?

**Summary:** After successful authentication, read the user's role from the JWT/response and use `Router.navigate()` to send them to the appropriate landing page. The routing logic lives in the auth service or login component, not in guards.

**Implementation:**

```typescript
// auth.service.ts
@Injectable({ providedIn: 'root' })
export class AuthService {
  private router = inject(Router);

  login(credentials: Credentials): Observable<User> {
    return this.http.post<AuthResponse>('/api/auth/login', credentials).pipe(
      tap(response => {
        this.storeToken(response.token);
        this.currentUser.set(response.user);
        this.redirectByRole(response.user);
      })
    );
  }

  private redirectByRole(user: User): void {
    const roleRouteMap: Record<string, string> = {
      'ADMIN': '/admin/dashboard',
      'MANAGER': '/manager/dashboard',
      'USER': '/user/home',
      'VIEWER': '/reports'
    };

    const targetRoute = roleRouteMap[user.role] || '/home';
    this.router.navigate([targetRoute]);
  }
}
```

**Login component:**
```typescript
@Component({ ... })
export class LoginComponent {
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);

  loginForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', Validators.required)
  });

  onSubmit() {
    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value).subscribe({
        error: (err) => this.errorMessage = err.message
      });
    }
  }
}
```

**Handle return URL (deep linking):**
```typescript
// If user tried to access /admin/reports before login, redirect them back after
private redirectByRole(user: User): void {
  const returnUrl = this.route.snapshot.queryParams['returnUrl'];

  if (returnUrl && this.canAccessRoute(user, returnUrl)) {
    this.router.navigateByUrl(returnUrl);
    return;
  }

  const roleRouteMap: Record<string, string> = { ... };
  this.router.navigate([roleRouteMap[user.role] || '/home']);
}

// In the auth guard — save the attempted URL
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.isAuthenticated()
    ? true
    : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
```

**Common mistake:** Hardcoding role checks in the login component. Extract the role-to-route mapping into a configuration so it's easy to change without modifying component logic.

> Always handle the return URL case — users hate being sent to a generic dashboard when they bookmarked a specific page. Redirect them back to where they were trying to go.

---

### 14. How do you preload important modules for faster navigation?

**Summary:** Angular's preloading strategies load lazy modules in the background after the initial app loads, so when the user navigates, the module is already cached. Use `PreloadAllModules` for small apps or a custom strategy for selective preloading.

**Approach 1 — Preload all (simple, works for most apps):**
```typescript
// app.config.ts
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withPreloading(PreloadAllModules))
  ]
};
```
After the initial load, Angular silently fetches all lazy chunks in the background. Good for apps with < 20 lazy routes.

**Approach 2 — Custom selective preloading (large apps):**
```typescript
// Flag routes for preloading
export const routes: Routes = [
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard.component'),
    data: { preload: true }  // Preload this
  },
  {
    path: 'admin',
    loadComponent: () => import('./admin/admin.component'),
    data: { preload: false } // Don't preload (rarely accessed)
  },
  {
    path: 'settings',
    loadComponent: () => import('./settings/settings.component')
    // No preload flag = don't preload
  }
];

// Custom preloading strategy
@Injectable({ providedIn: 'root' })
export class SelectivePreloadStrategy implements PreloadingStrategy {
  preload(route: Route, load: () => Observable<any>): Observable<any> {
    return route.data?.['preload'] ? load() : of(null);
  }
}

// Register it
provideRouter(routes, withPreloading(SelectivePreloadStrategy))
```

**Approach 3 — Role-based preloading:**
```typescript
@Injectable({ providedIn: 'root' })
export class RoleBasedPreloadStrategy implements PreloadingStrategy {
  private auth = inject(AuthService);

  preload(route: Route, load: () => Observable<any>): Observable<any> {
    const requiredRoles = route.data?.['roles'] as string[] | undefined;

    if (!requiredRoles) return load(); // No role restriction, preload

    return this.auth.hasAnyRole(requiredRoles) ? load() : of(null);
  }
}
```
An admin user preloads admin routes; a regular user doesn't waste bandwidth on routes they'll never access.

**Approach 4 — Network-aware preloading:**
```typescript
@Injectable({ providedIn: 'root' })
export class NetworkAwarePreloadStrategy implements PreloadingStrategy {
  preload(route: Route, load: () => Observable<any>): Observable<any> {
    const connection = (navigator as any).connection;

    // Don't preload on slow connections
    if (connection && connection.saveData) return of(null);
    if (connection && connection.effectiveType === '2g') return of(null);

    return route.data?.['preload'] ? load() : of(null);
  }
}
```

**How to verify:** Open Chrome DevTools → Network tab → After initial load, watch for lazy chunk files loading in the background without any navigation.

**Common mistake:** Using `PreloadAllModules` in an app with 50+ lazy routes and heavy chunks. This downloads megabytes of JavaScript the user may never need. Use selective preloading instead.

> Preload the modules users are *likely* to visit next. Dashboard user? Preload the top 3 menu items. Admin? Preload admin routes. Don't preload everything blindly.

---

## API Integration Scenarios

---

### 15. Multiple components call the same API repeatedly. How would you optimize it?

**Summary:** Cache the response in a service using `shareReplay`, a Signal-based cache, or an in-memory cache with TTL. The key principle: the API call lives in the service, not in components.

**Approach 1 — `shareReplay` for the same Observable:**
```typescript
@Injectable({ providedIn: 'root' })
export class ConfigService {
  private config$: Observable<AppConfig> | null = null;

  getConfig(): Observable<AppConfig> {
    if (!this.config$) {
      this.config$ = this.http.get<AppConfig>('/api/config').pipe(
        shareReplay({ bufferSize: 1, refCount: true })
      );
    }
    return this.config$;
  }
}
```
Multiple components subscribing to `getConfig()` share the same HTTP request.

**Approach 2 — Signal-based cache with manual invalidation:**
```typescript
@Injectable({ providedIn: 'root' })
export class UserService {
  private usersCache = signal<User[] | null>(null);
  private loading = signal(false);

  readonly users = this.usersCache.asReadonly();

  loadUsers(): void {
    if (this.usersCache() !== null || this.loading()) return;

    this.loading.set(true);
    this.http.get<User[]>('/api/users').subscribe(users => {
      this.usersCache.set(users);
      this.loading.set(false);
    });
  }

  invalidateCache(): void {
    this.usersCache.set(null);
  }
}
```

**Approach 3 — Cache with TTL (time-to-live):**
```typescript
@Injectable({ providedIn: 'root' })
export class CachedApiService {
  private cache = new Map<string, { data: any; timestamp: number }>();
  private TTL = 5 * 60 * 1000; // 5 minutes

  get<T>(url: string): Observable<T> {
    const cached = this.cache.get(url);
    if (cached && Date.now() - cached.timestamp < this.TTL) {
      return of(cached.data as T);
    }

    return this.http.get<T>(url).pipe(
      tap(data => this.cache.set(url, { data, timestamp: Date.now() }))
    );
  }

  invalidate(url: string): void {
    this.cache.delete(url);
  }
}
```

**Approach 4 — HTTP Interceptor-level caching (for GET requests):**
```typescript
export const cachingInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.method !== 'GET') return next(req);

  const cache = inject(HttpCacheService);
  const cached = cache.get(req.urlWithParams);

  if (cached) return of(cached);

  return next(req).pipe(
    tap(event => {
      if (event instanceof HttpResponse) {
        cache.set(req.urlWithParams, event);
      }
    })
  );
};
```

**Common mistake:** Caching without invalidation. After a POST/PUT/DELETE that modifies data, you must invalidate the relevant cache — otherwise the UI shows stale data.

> Pick the simplest caching strategy that fits. `shareReplay` for single-resource, Signal cache for feature-level, TTL cache for API-wide. Always plan for invalidation.

---

### 16. You need to handle global HTTP errors. How would you implement it?

**Summary:** Use an HTTP interceptor to catch errors globally — handle 401s with token refresh/redirect, 403s with "access denied", 5xx with user-friendly messages, and let component-specific errors pass through.

**Implementation:**

```typescript
// error.interceptor.ts
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      switch (error.status) {
        case 0:
          // Network error — no internet or server down
          toastService.error('Unable to connect to the server. Please check your connection.');
          break;

        case 401:
          // Unauthorized — token expired or invalid
          authService.logout();
          router.navigate(['/login'], {
            queryParams: { returnUrl: router.url, reason: 'session_expired' }
          });
          break;

        case 403:
          // Forbidden — authenticated but not authorized
          toastService.error('You do not have permission to perform this action.');
          break;

        case 404:
          // Not found — let component handle this (might be expected)
          break;

        case 422:
          // Validation error — let component handle for form display
          break;

        case 429:
          // Rate limited
          toastService.warning('Too many requests. Please slow down.');
          break;

        case 500:
        case 502:
        case 503:
          // Server errors
          toastService.error('Something went wrong on our end. Please try again later.');
          break;
      }

      return throwError(() => error);
    })
  );
};
```

**Register the interceptor:**
```typescript
// app.config.ts
export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor]))
  ]
};
```

**Token refresh on 401 (advanced):**
```typescript
export const authRefreshInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/auth/refresh')) {
        return authService.refreshToken().pipe(
          switchMap(newToken => {
            const retryReq = req.clone({
              setHeaders: { Authorization: `Bearer ${newToken}` }
            });
            return next(retryReq);
          }),
          catchError(() => {
            authService.logout();
            return throwError(() => error);
          })
        );
      }
      return throwError(() => error);
    })
  );
};
```

**Allow components to handle specific errors:**
```typescript
// Component can still catch errors for specific handling
this.userService.updateUser(user).subscribe({
  next: () => this.toastService.success('User updated'),
  error: (err) => {
    if (err.status === 422) {
      this.validationErrors = err.error.errors; // Show field-level errors
    }
    // 500, 401, etc. already handled by interceptor
  }
});
```

**Common mistake:** Swallowing errors in the interceptor (not rethrowing). Components need the error too — for loading states, retry buttons, or specific error messages.

> The interceptor handles the global response (toasts, redirects), but always rethrows so components can handle their own concerns. Two layers of error handling, each with its own job.

---

### 17. You need to attach JWT tokens to every API request. How would you implement this?

**Summary:** Use an HTTP interceptor to automatically clone every outgoing request and attach the Authorization header — with logic to skip auth endpoints and handle token refresh.

**Implementation:**

```typescript
// auth.interceptor.ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  // Skip token for public endpoints
  const publicUrls = ['/api/auth/login', '/api/auth/register', '/api/auth/refresh'];
  if (publicUrls.some(url => req.url.includes(url))) {
    return next(req);
  }

  // Skip if calling external APIs
  if (!req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }

  const token = authService.getAccessToken();
  if (!token) {
    return next(req);
  }

  const authReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(authReq);
};
```

**Register it:**
```typescript
// app.config.ts
export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([authInterceptor, errorInterceptor])
    )
  ]
};
```

**Auth service for token management:**
```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'access_token';
  private readonly REFRESH_KEY = 'refresh_token';

  getAccessToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isTokenExpired(): boolean {
    const token = this.getAccessToken();
    if (!token) return true;

    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 < Date.now();
  }

  refreshToken(): Observable<string> {
    return this.http.post<AuthResponse>('/api/auth/refresh', {
      refreshToken: localStorage.getItem(this.REFRESH_KEY)
    }).pipe(
      tap(response => {
        localStorage.setItem(this.TOKEN_KEY, response.accessToken);
      }),
      map(response => response.accessToken)
    );
  }
}
```

**Full interceptor with token refresh:**
```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  const publicUrls = ['/auth/login', '/auth/register', '/auth/refresh'];
  if (publicUrls.some(url => req.url.includes(url))) {
    return next(req);
  }

  let token = authService.getAccessToken();

  if (token && authService.isTokenExpired()) {
    // Token expired — try refresh before sending request
    return authService.refreshToken().pipe(
      switchMap(newToken => {
        const authReq = req.clone({
          setHeaders: { Authorization: `Bearer ${newToken}` }
        });
        return next(authReq);
      }),
      catchError(() => {
        authService.logout();
        return throwError(() => new Error('Session expired'));
      })
    );
  }

  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(req);
};
```

**Common mistake:** Not handling concurrent requests during token refresh. If 5 requests fire while the token is being refreshed, you get 5 refresh calls. Use a `BehaviorSubject` to queue requests while refresh is in progress.

> Interceptors make auth invisible to the rest of your app. Components and services never think about tokens — they just call APIs and the interceptor handles the rest.

---

## UI / UX Scenarios

---

### 18. Your UI freezes while loading large data. What strategy would you use?

**Summary:** The UI freezes because JavaScript is blocking the main thread. The fix is a combination of showing immediate feedback (skeleton screens), offloading computation (Web Workers), and rendering progressively (virtual scrolling, `@defer`).

**Strategy 1 — Skeleton screens for perceived performance:**
```html
@if (loading()) {
  <div class="skeleton-list">
    @for (i of skeletonItems; track i) {
      <div class="skeleton-row animate-pulse"></div>
    }
  </div>
} @else {
  <app-data-table [data]="data()" />
}
```
Users perceive the app as faster when they see structure immediately.

**Strategy 2 — Virtual scrolling (don't render what's not visible):**
```html
<cdk-virtual-scroll-viewport itemSize="50" class="list-viewport">
  <div *cdkVirtualFor="let item of items; trackBy: trackById">
    <app-list-item [item]="item" />
  </div>
</cdk-virtual-scroll-viewport>
```

**Strategy 3 — Web Workers for heavy computation:**
```typescript
// heavy-computation.worker.ts
addEventListener('message', ({ data }) => {
  const result = data.items
    .filter(item => item.active)
    .sort((a, b) => b.score - a.score)
    .map(item => ({ ...item, rank: calculateRank(item) }));
  postMessage(result);
});

// Component
processData(items: Item[]) {
  const worker = new Worker(new URL('./heavy-computation.worker', import.meta.url));
  worker.onmessage = ({ data }) => {
    this.processedItems.set(data);
  };
  worker.postMessage({ items });
}
```

**Strategy 4 — Progressive loading with `@defer`:**
```html
<!-- Load critical content immediately -->
<app-summary-card [data]="summaryData()" />

<!-- Load charts only when user scrolls to them -->
@defer (on viewport) {
  <app-analytics-chart [data]="chartData()" />
} @loading (minimum 300ms) {
  <div class="chart-skeleton"></div>
} @placeholder {
  <div class="chart-placeholder">Scroll to view analytics</div>
}
```

**Strategy 5 — Chunked rendering:**
```typescript
async renderInChunks(items: Item[], chunkSize = 100) {
  for (let i = 0; i < items.length; i += chunkSize) {
    const chunk = items.slice(i, i + chunkSize);
    this.renderedItems.update(current => [...current, ...chunk]);
    // Yield to the main thread between chunks
    await new Promise(resolve => setTimeout(resolve, 0));
  }
}
```

**Strategy 6 — `runOutsideAngular` for non-UI work:**
```typescript
this.ngZone.runOutsideAngular(() => {
  // Heavy processing without triggering change detection
  const processed = this.heavyTransform(rawData);
  this.ngZone.run(() => this.data.set(processed));
});
```

**Common mistake:** Adding a spinner and thinking the problem is solved. A spinner over a frozen UI is still a frozen UI — the user can't even scroll. The real fix is not blocking the main thread.

> The main thread is sacred. Never block it with computation or render thousands of DOM nodes. Show structure immediately, load data progressively, compute off-thread.

---

### 19. How would you implement infinite scrolling?

**Summary:** Infinite scrolling uses either the Intersection Observer API or CDK virtual scroll with a "load more" trigger to fetch and append data as the user scrolls near the bottom.

**Approach 1 — Intersection Observer (lightweight, no dependencies):**

```typescript
@Component({
  template: `
    <div class="list">
      @for (item of items(); track item.id) {
        <app-list-item [item]="item" />
      }
    </div>

    @if (loading()) {
      <div class="spinner">Loading...</div>
    }

    <!-- Invisible sentinel element -->
    <div #scrollAnchor class="scroll-anchor"></div>
  `
})
export class InfiniteListComponent implements AfterViewInit, OnDestroy {
  @ViewChild('scrollAnchor') scrollAnchor!: ElementRef;

  items = signal<Item[]>([]);
  loading = signal(false);
  page = signal(1);
  hasMore = signal(true);
  private observer!: IntersectionObserver;

  private dataService = inject(DataService);
  private destroyRef = inject(DestroyRef);

  ngAfterViewInit() {
    this.observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && !this.loading() && this.hasMore()) {
          this.loadMore();
        }
      },
      { threshold: 0.1 }
    );
    this.observer.observe(this.scrollAnchor.nativeElement);
  }

  loadMore() {
    this.loading.set(true);
    this.dataService.getItems(this.page(), 20).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(response => {
      this.items.update(current => [...current, ...response.items]);
      this.hasMore.set(response.hasMore);
      this.page.update(p => p + 1);
      this.loading.set(false);
    });
  }

  ngOnDestroy() {
    this.observer.disconnect();
  }
}
```

**Approach 2 — CDK Virtual Scroll (better for huge lists):**
```typescript
@Component({
  template: `
    <cdk-virtual-scroll-viewport itemSize="72" (scrolledIndexChange)="onScroll($event)">
      <div *cdkVirtualFor="let item of items(); trackBy: trackById">
        <app-list-item [item]="item" />
      </div>
    </cdk-virtual-scroll-viewport>
  `
})
export class VirtualInfiniteListComponent {
  @ViewChild(CdkVirtualScrollViewport) viewport!: CdkVirtualScrollViewport;

  items = signal<Item[]>([]);
  private loading = false;

  onScroll(index: number) {
    const end = this.viewport.getRenderedRange().end;
    const total = this.items().length;

    // Load more when user is within 5 items of the end
    if (end >= total - 5 && !this.loading) {
      this.loadMore();
    }
  }
}
```

**Backend API design for infinite scroll:**
```typescript
// Cursor-based pagination (better than offset for infinite scroll)
getItems(cursor: string | null, limit: number): Observable<PagedResponse> {
  const params: any = { limit };
  if (cursor) params.cursor = cursor;

  return this.http.get<PagedResponse>('/api/items', { params });
}

// Response
interface PagedResponse {
  items: Item[];
  nextCursor: string | null;
  hasMore: boolean;
}
```

**Common mistakes:**
- Using offset-based pagination — if items are added/deleted while scrolling, the user sees duplicates or misses items. Use cursor-based pagination.
- Not handling the "no more data" case — the scroll trigger fires endlessly with empty API calls.
- Not showing a loading state — users think the app froze.

> Intersection Observer is the modern way — no scroll event listeners, no performance issues. Combine with cursor-based pagination and virtual scrolling for the best user experience.

---

### 20. How do you optimize Angular forms with large dynamic fields?

**Summary:** Large dynamic forms become slow due to excessive change detection, validator re-runs, and DOM bloat. The fix is OnPush, lazy validation, virtual rendering for massive forms, and smart form architecture.

**Strategy 1 — Use Reactive Forms with FormArray:**
```typescript
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DynamicFormComponent {
  form = new FormGroup({
    items: new FormArray<FormGroup>([])
  });

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  addField(config: FieldConfig) {
    const group = new FormGroup({
      label: new FormControl(config.label),
      value: new FormControl(config.defaultValue, config.validators),
      type: new FormControl(config.type)
    });
    this.items.push(group);
  }

  removeField(index: number) {
    this.items.removeAt(index);
  }
}
```

**Strategy 2 — Render fields dynamically with trackBy:**
```html
<form [formGroup]="form">
  <div formArrayName="items">
    @for (field of items.controls; track field; let i = $index) {
      <app-dynamic-field
        [formGroupName]="i"
        [fieldConfig]="fieldConfigs()[i]"
        changeDetection="OnPush"
      />
    }
  </div>
</form>
```

**Strategy 3 — Debounce validation for expensive validators:**
```typescript
// Don't validate on every keystroke
this.form.get('email')?.valueChanges.pipe(
  debounceTime(400),
  distinctUntilChanged()
).subscribe(value => {
  this.validateEmail(value);
});

// Or set updateOn to 'blur' for fields that don't need live validation
new FormControl('', {
  validators: [Validators.required],
  updateOn: 'blur' // Validate only when user leaves the field
});
```

**Strategy 4 — Sectioned forms with accordion/tabs:**
```typescript
// Break a 100-field form into sections
// Only validate and render the active section
interface FormSection {
  title: string;
  fields: FieldConfig[];
  formGroup: FormGroup;
}

sections = signal<FormSection[]>([
  { title: 'Personal Info', fields: [...], formGroup: this.createPersonalForm() },
  { title: 'Address', fields: [...], formGroup: this.createAddressForm() },
  { title: 'Preferences', fields: [...], formGroup: this.createPreferencesForm() }
]);
```

```html
@for (section of sections(); track section.title) {
  <mat-expansion-panel>
    <mat-expansion-panel-header>{{ section.title }}</mat-expansion-panel-header>
    <app-form-section [formGroup]="section.formGroup" [fields]="section.fields" />
  </mat-expansion-panel>
}
```

**Strategy 5 — Virtual scroll for truly massive forms (100+ fields):**
```html
<cdk-virtual-scroll-viewport itemSize="72">
  <div *cdkVirtualFor="let field of fieldConfigs(); trackBy: trackByFieldId">
    <app-dynamic-field [config]="field" [control]="getControl(field.id)" />
  </div>
</cdk-virtual-scroll-viewport>
```

**Common mistakes:**
- Putting the entire FormArray in a single component — every keystroke triggers CD for all fields
- Using `updateOn: 'change'` (default) for fields with async validators — fires HTTP on every keystroke
- Not using OnPush on the individual field components

> Break large forms into sections, use OnPush on field components, debounce validation, and use `updateOn: 'blur'` for most fields. Users don't need live validation on every keystroke.

---

## Security Scenarios

---

### 21. How would you prevent Cross-Site Scripting (XSS) in Angular?

**Summary:** Angular auto-sanitizes template bindings by default. Your job is to not bypass that protection, implement Content Security Policy headers, and be disciplined about where you trust user input.

**What Angular does automatically:**
```typescript
// Angular sanitizes this automatically — safe
<div>{{ userInput }}</div>
<div [innerHTML]="userInput"></div>  // Sanitized — scripts stripped

// This outputs: <script>alert('xss')</script> as TEXT, not executed
userInput = '<script>alert("xss")</script>';
```

**What you must NOT do:**
```typescript
// DANGEROUS: Bypasses Angular's sanitizer
document.getElementById('output').innerHTML = userInput; // Direct DOM = no sanitization

// DANGEROUS: Only use when absolutely necessary and input is validated
this.sanitizer.bypassSecurityTrustHtml(userInput); // Last resort
```

**Defense layers:**

**1. Never bypass sanitization without validation:**
```typescript
// If you MUST render trusted HTML (e.g., from a CMS)
sanitizeHtml(html: string): SafeHtml {
  // First sanitize with a library like DOMPurify
  const clean = DOMPurify.sanitize(html, { ALLOWED_TAGS: ['b', 'i', 'p', 'a'] });
  return this.sanitizer.bypassSecurityTrustHtml(clean);
}
```

**2. Content Security Policy headers (server-side):**
```
Content-Security-Policy:
  default-src 'self';
  script-src 'self';
  style-src 'self' 'unsafe-inline';
  img-src 'self' data: https:;
  connect-src 'self' https://api.example.com;
```
This prevents inline scripts from executing even if injected.

**3. Secure token storage:**
```typescript
// WRONG: XSS can read localStorage
localStorage.setItem('token', jwt);

// RIGHT: HttpOnly cookie — JavaScript can't access it
// Set via server: Set-Cookie: token=jwt; HttpOnly; Secure; SameSite=Strict
```

**4. Sanitize URLs:**
```typescript
// Angular sanitizes [href] bindings, but be explicit with user-provided URLs
<a [href]="userUrl">Link</a>  // Angular blocks javascript: URLs

// For extra safety
isValidUrl(url: string): boolean {
  try {
    const parsed = new URL(url);
    return ['http:', 'https:'].includes(parsed.protocol);
  } catch {
    return false;
  }
}
```

**5. Validate on the server too:**
Every user input that's stored must be sanitized server-side before storage. Client-side is a UX layer, not a security layer.

**Common mistakes:**
- Using `[innerHTML]` with `bypassSecurityTrustHtml` without sanitizing the input first
- Constructing HTML strings with template literals that include user data
- Storing JWTs in localStorage instead of HttpOnly cookies

> Angular's built-in XSS protection is excellent — it covers 95% of cases. Your job is to not bypass it, add CSP headers, and never trust user input at any layer.

---

### 22. How would you secure API communication between Angular and backend?

**Summary:** Secure API communication requires HTTPS everywhere, JWT with HttpOnly cookies, CSRF protection, input validation, and CORS configuration — it's a multi-layer defense strategy.

**Layer 1 — HTTPS only:**
```typescript
// Enforce HTTPS in the interceptor
export const httpsInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith('https') && !req.url.startsWith('http://localhost')) {
    const secureReq = req.clone({ url: req.url.replace('http://', 'https://') });
    return next(secureReq);
  }
  return next(req);
};
```

**Layer 2 — JWT with HttpOnly cookies:**
```typescript
// Angular side — send cookies automatically
provideHttpClient(withInterceptors([...]),
  // Enable withCredentials for cookie-based auth
)

// Or per-request
this.http.get('/api/data', { withCredentials: true });

// Server sets: Set-Cookie: token=jwt; HttpOnly; Secure; SameSite=Strict; Path=/api
```
HttpOnly means JavaScript can never read the token — XSS can't steal it.

**Layer 3 — CSRF protection:**
```typescript
// Angular's HttpClient automatically reads XSRF-TOKEN cookie and sends X-XSRF-TOKEN header
provideHttpClient(
  withXsrfConfiguration({
    cookieName: 'XSRF-TOKEN',
    headerName: 'X-XSRF-TOKEN'
  })
)
```

**Layer 4 — CORS configuration (server-side):**
```
// Only allow your Angular app's origin
Access-Control-Allow-Origin: https://app.example.com
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: Content-Type, Authorization, X-XSRF-TOKEN
Access-Control-Allow-Credentials: true
```

**Layer 5 — Request/response validation:**
```typescript
// Validate API responses match expected shape
interface ApiResponse<T> {
  data: T;
  status: string;
}

getUsers(): Observable<User[]> {
  return this.http.get<ApiResponse<User[]>>('/api/users').pipe(
    map(response => {
      if (!Array.isArray(response.data)) {
        throw new Error('Invalid response format');
      }
      return response.data;
    })
  );
}
```

**Layer 6 — Rate limiting awareness:**
```typescript
// Handle 429 Too Many Requests in interceptor
if (error.status === 429) {
  const retryAfter = error.headers.get('Retry-After');
  toastService.warning(`Rate limited. Try again in ${retryAfter} seconds.`);
}
```

**Layer 7 — Sensitive data handling:**
```typescript
// Never log sensitive data
export const loggingInterceptor: HttpInterceptorFn = (req, next) => {
  // Don't log auth endpoints
  if (req.url.includes('/auth')) return next(req);

  console.log(`${req.method} ${req.url}`);
  // Never: console.log(req.body) — might contain passwords
  return next(req);
};
```

**Security checklist:**
- [ ] All APIs over HTTPS
- [ ] JWTs in HttpOnly Secure cookies
- [ ] CSRF tokens enabled
- [ ] CORS restricted to known origins
- [ ] Input validation (client + server)
- [ ] Rate limiting on sensitive endpoints
- [ ] No sensitive data in URL params (visible in logs)
- [ ] API versioning for backward-compatible changes

**Common mistake:** Relying only on Angular-side security. If the backend accepts requests from any origin without authentication, no amount of Angular security matters.

> Security is layers. No single measure is sufficient. HTTPS + HttpOnly cookies + CSRF + CORS + server-side validation — each layer catches what the others miss.

---

## Testing Scenarios

---

### 23. How do you write unit tests for Angular services?

**Summary:** Use Angular's `TestBed` with `HttpClientTestingModule` for services that make HTTP calls, and simple class instantiation for pure logic services. Mock dependencies, test behavior, not implementation.

**Testing an HTTP service:**
```typescript
describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        UserService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Ensure no unexpected HTTP calls
  });

  it('should fetch users', () => {
    const mockUsers: User[] = [
      { id: 1, name: 'John', email: 'john@test.com' },
      { id: 2, name: 'Jane', email: 'jane@test.com' }
    ];

    service.getUsers().subscribe(users => {
      expect(users.length).toBe(2);
      expect(users[0].name).toBe('John');
    });

    const req = httpMock.expectOne('/api/users');
    expect(req.request.method).toBe('GET');
    req.flush(mockUsers);
  });

  it('should handle error when fetching users', () => {
    service.getUsers().subscribe({
      error: (err) => {
        expect(err.status).toBe(500);
      }
    });

    const req = httpMock.expectOne('/api/users');
    req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
  });

  it('should create a user', () => {
    const newUser = { name: 'Alice', email: 'alice@test.com' };

    service.createUser(newUser).subscribe(user => {
      expect(user.id).toBeDefined();
      expect(user.name).toBe('Alice');
    });

    const req = httpMock.expectOne('/api/users');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newUser);
    req.flush({ id: 3, ...newUser });
  });
});
```

**Testing a pure logic service (no HTTP):**
```typescript
describe('CalculationService', () => {
  let service: CalculationService;

  beforeEach(() => {
    service = new CalculationService(); // No TestBed needed
  });

  it('should calculate total with tax', () => {
    expect(service.calculateTotal(100, 0.1)).toBe(110);
  });

  it('should return 0 for negative values', () => {
    expect(service.calculateTotal(-50, 0.1)).toBe(0);
  });
});
```

**Testing a service with dependencies:**
```typescript
describe('OrderService', () => {
  let service: OrderService;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUserId']);
    authServiceSpy.getCurrentUserId.and.returnValue('user-123');

    TestBed.configureTestingModule({
      providers: [
        OrderService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceSpy }
      ]
    });

    service = TestBed.inject(OrderService);
  });

  it('should fetch orders for the current user', () => {
    const httpMock = TestBed.inject(HttpTestingController);

    service.getMyOrders().subscribe();

    const req = httpMock.expectOne('/api/users/user-123/orders');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
```

**Common mistakes:**
- Testing implementation details (checking private methods) instead of public behavior
- Not calling `httpMock.verify()` in afterEach — misses unexpected HTTP calls
- Forgetting to flush the mock request — the subscribe callback never runs

> Test what the service *does*, not how it does it. HTTP services: verify correct URL, method, body, and response handling. Logic services: verify inputs produce correct outputs.

---

### 24. How do you mock API calls in Angular tests?

**Summary:** Angular provides `HttpClientTestingModule` with `HttpTestingController` to intercept and mock HTTP calls in tests. You control exactly what the API "returns" without hitting a real server.

**Basic mocking with HttpTestingController:**
```typescript
describe('ProductService', () => {
  let service: ProductService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProductService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  // Mock a successful response
  it('should return products', () => {
    const mockProducts = [{ id: 1, name: 'Widget', price: 9.99 }];

    service.getProducts().subscribe(products => {
      expect(products).toEqual(mockProducts);
    });

    const req = httpMock.expectOne('/api/products');
    req.flush(mockProducts); // Respond with mock data
  });

  // Mock an error response
  it('should handle 404 error', () => {
    service.getProduct(999).subscribe({
      error: (err) => {
        expect(err.status).toBe(404);
      }
    });

    const req = httpMock.expectOne('/api/products/999');
    req.flush('Not found', { status: 404, statusText: 'Not Found' });
  });

  // Mock with request verification
  it('should send correct query params', () => {
    service.searchProducts('widget', 1, 20).subscribe();

    const req = httpMock.expectOne(r =>
      r.url === '/api/products/search' &&
      r.params.get('q') === 'widget' &&
      r.params.get('page') === '1' &&
      r.params.get('size') === '20'
    );
    req.flush({ items: [], total: 0 });
  });

  // Mock multiple sequential requests
  it('should load product then reviews', () => {
    service.getProductWithReviews(1).subscribe(result => {
      expect(result.product.name).toBe('Widget');
      expect(result.reviews.length).toBe(2);
    });

    const productReq = httpMock.expectOne('/api/products/1');
    productReq.flush({ id: 1, name: 'Widget' });

    const reviewReq = httpMock.expectOne('/api/products/1/reviews');
    reviewReq.flush([{ rating: 5 }, { rating: 4 }]);
  });

  // Mock network error
  it('should handle network failure', () => {
    service.getProducts().subscribe({
      error: (err) => {
        expect(err.error instanceof ProgressEvent).toBeTrue();
      }
    });

    const req = httpMock.expectOne('/api/products');
    req.error(new ProgressEvent('error'));
  });
});
```

**Mocking at the service level (for component tests):**
```typescript
describe('ProductListComponent', () => {
  let mockProductService: jasmine.SpyObj<ProductService>;

  beforeEach(() => {
    mockProductService = jasmine.createSpyObj('ProductService', ['getProducts']);
    mockProductService.getProducts.and.returnValue(
      of([{ id: 1, name: 'Widget', price: 9.99 }])
    );

    TestBed.configureTestingModule({
      imports: [ProductListComponent],
      providers: [
        { provide: ProductService, useValue: mockProductService }
      ]
    });
  });

  it('should display products', () => {
    const fixture = TestBed.createComponent(ProductListComponent);
    fixture.detectChanges();

    const items = fixture.nativeElement.querySelectorAll('.product-item');
    expect(items.length).toBe(1);
    expect(items[0].textContent).toContain('Widget');
  });

  it('should show loading state', () => {
    mockProductService.getProducts.and.returnValue(NEVER); // Never completes

    const fixture = TestBed.createComponent(ProductListComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.spinner')).toBeTruthy();
  });
});
```

**Common mistakes:**
- Using `expectOne` when the service makes multiple calls to the same URL — use `match()` instead
- Not flushing the request — the Observable never emits and assertions in `subscribe` never run
- Mocking too much — if you mock everything, you're not testing anything real

> Use `HttpTestingController` for service tests (tests the actual HTTP layer). Use spy services for component tests (isolates the component from the API layer). Each level tests its own responsibility.

---

### 25. How do you test components with dependency injection?

**Summary:** Override providers in `TestBed` with mocks or spies. The goal is to isolate the component from its dependencies so you're testing the component's behavior, not the services behind it.

**Basic component with injected service:**
```typescript
// The component
@Component({
  selector: 'app-user-profile',
  template: `
    @if (user()) {
      <h1>{{ user()!.name }}</h1>
      <p>{{ user()!.email }}</p>
      <button (click)="onDelete()">Delete</button>
    } @else {
      <p class="loading">Loading...</p>
    }
  `
})
export class UserProfileComponent implements OnInit {
  private userService = inject(UserService);
  private router = inject(Router);
  user = signal<User | null>(null);

  ngOnInit() {
    this.userService.getCurrentUser().subscribe(u => this.user.set(u));
  }

  onDelete() {
    this.userService.deleteUser(this.user()!.id).subscribe(() => {
      this.router.navigate(['/users']);
    });
  }
}
```

**Testing with mocked dependencies:**
```typescript
describe('UserProfileComponent', () => {
  let fixture: ComponentFixture<UserProfileComponent>;
  let component: UserProfileComponent;
  let mockUserService: jasmine.SpyObj<UserService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockUser: User = { id: 1, name: 'John Doe', email: 'john@test.com' };

  beforeEach(async () => {
    mockUserService = jasmine.createSpyObj('UserService', [
      'getCurrentUser', 'deleteUser'
    ]);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    mockUserService.getCurrentUser.and.returnValue(of(mockUser));
    mockUserService.deleteUser.and.returnValue(of(void 0));

    await TestBed.configureTestingModule({
      imports: [UserProfileComponent],
      providers: [
        { provide: UserService, useValue: mockUserService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserProfileComponent);
    component = fixture.componentInstance;
  });

  it('should display user name after loading', () => {
    fixture.detectChanges(); // Triggers ngOnInit

    const heading = fixture.nativeElement.querySelector('h1');
    expect(heading.textContent).toBe('John Doe');
  });

  it('should show loading state before data arrives', () => {
    mockUserService.getCurrentUser.and.returnValue(NEVER);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.loading')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('h1')).toBeNull();
  });

  it('should call deleteUser and navigate on delete', () => {
    fixture.detectChanges();

    const deleteBtn = fixture.nativeElement.querySelector('button');
    deleteBtn.click();
    fixture.detectChanges();

    expect(mockUserService.deleteUser).toHaveBeenCalledWith(1);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/users']);
  });
});
```

**Testing with `useClass` for complex mocks:**
```typescript
class MockAuthService {
  private user = signal<User | null>({ id: 1, name: 'Test', roles: ['ADMIN'] });

  isAuthenticated() { return true; }
  hasRole(role: string) { return this.user()?.roles.includes(role) ?? false; }
  getCurrentUser() { return of(this.user()); }
}

TestBed.configureTestingModule({
  imports: [AdminDashboardComponent],
  providers: [
    { provide: AuthService, useClass: MockAuthService }
  ]
});
```

**Testing with `useFactory` for conditional mocks:**
```typescript
TestBed.configureTestingModule({
  providers: [
    {
      provide: AuthService,
      useFactory: () => {
        const mock = jasmine.createSpyObj('AuthService', ['hasRole']);
        mock.hasRole.and.callFake((role: string) => role === 'ADMIN');
        return mock;
      }
    }
  ]
});
```

**Testing components with `@Input()` and `@Output()`:**
```typescript
describe('AlertComponent', () => {
  it('should emit close event when dismiss clicked', () => {
    const fixture = TestBed.createComponent(AlertComponent);
    const component = fixture.componentInstance;

    component.message = 'Test alert';
    component.type = 'warning';
    fixture.detectChanges();

    let emitted = false;
    component.closed.subscribe(() => emitted = true);

    const closeBtn = fixture.nativeElement.querySelector('.close-btn');
    closeBtn.click();

    expect(emitted).toBeTrue();
  });
});
```

**Testing with `ActivatedRoute`:**
```typescript
TestBed.configureTestingModule({
  imports: [UserDetailComponent],
  providers: [
    {
      provide: ActivatedRoute,
      useValue: {
        params: of({ id: '42' }),
        snapshot: { paramMap: convertToParamMap({ id: '42' }) }
      }
    },
    { provide: UserService, useValue: mockUserService }
  ]
});
```

**Common mistakes:**
- Forgetting to call `fixture.detectChanges()` — the template isn't rendered and ngOnInit doesn't run
- Over-mocking — if you mock everything the component uses, the test proves nothing
- Testing internal state instead of rendered output — test what the user sees, not component properties

> Mock dependencies at the boundary (services, router), but test the component through its template. If the user can't see it or interact with it, it's probably not worth testing directly.
