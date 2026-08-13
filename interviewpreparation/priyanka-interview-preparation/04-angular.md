# Angular — Interview Q&A

> Primary skill file. Tailored for a Wipro lateral (4+ years) role. Experience covered: Angular (migration to Angular 16), Ionic, NgRx, Angular Formly, reactive forms, RxJS, Kendo UI, custom reusable components, unit testing.

---

## Core Concepts

### Q: What is Angular and how is it different from AngularJS?
Angular is a TypeScript-based, component-driven SPA framework for building scalable web (and via Ionic, mobile) applications. AngularJS (1.x) was JavaScript-based and used controllers, `$scope`, and directives with two-way digest-cycle binding.

Key differences:
- **Language**: Angular uses TypeScript; AngularJS used plain JavaScript.
- **Architecture**: Angular uses Components + Services; AngularJS used Controllers + `$scope`.
- **DI**: Angular has a hierarchical, typed DI system; AngularJS used string-based injection.
- **Binding/CD**: Angular uses unidirectional data flow with zone.js change detection; AngularJS used the digest cycle (`$digest`) which was slower.
- **Mobile**: Angular is mobile-friendly by design; AngularJS was not.
- **CLI & Tooling**: Angular ships a powerful CLI, AOT compilation, lazy loading, and tree-shaking.

### Q: What is a Single Page Application (SPA)?
A SPA loads a single HTML shell once, then dynamically rewrites the view in the browser using JavaScript instead of requesting full new pages from the server. The Angular Router swaps components in/out without full page reloads.

Benefits: fast navigation, rich UX, less server load. Trade-offs: heavier initial bundle, SEO needs SSR (Angular Universal), and routing/state must be handled client-side.

### Q: What is a Component in Angular?
A component is the fundamental building block of UI — a class decorated with `@Component` that controls a section of the screen (a "view"). It bundles a template (HTML), styles, and logic.

```ts
@Component({
  selector: 'app-user-card',
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.scss'],
})
export class UserCardComponent {
  @Input() name = '';
}
```

Every component has a selector (how it's used in templates), a template, and a class that holds data and behavior.

### Q: What is an NgModule?
An `@NgModule` is a container that groups related components, directives, pipes, and services. It tells Angular how to compile and run a section of the app.

```ts
@NgModule({
  declarations: [AppComponent, UserCardComponent], // components/directives/pipes owned by this module
  imports: [BrowserModule, FormsModule],           // other modules this module needs
  providers: [UserService],                        // services
  bootstrap: [AppComponent],                        // root component (root module only)
})
export class AppModule {}
```
- `declarations`: what belongs to this module.
- `imports`: bring in other modules' exported features.
- `exports`: make declarations available to importing modules.
- `providers`: register services.

### Q: What are standalone components and why do they matter?
Introduced in Angular 14 and stabilized by 15/16, standalone components remove the need for NgModules. The component declares its own dependencies via the `imports` array and uses `standalone: true`.

```ts
@Component({
  selector: 'app-user-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `<a [routerLink]="['/user', id]">{{ name }}</a>`,
})
export class UserCardComponent {
  @Input() id!: number;
  @Input() name = '';
}
```
Benefits: less boilerplate, simpler mental model, easier lazy loading (`loadComponent`), better tree-shaking. During my Angular 16 migration this was a major modernization step — bootstrapping with `bootstrapApplication()` instead of `AppModule`.

### Q: What are decorators in Angular?
Decorators are functions prefixed with `@` that attach metadata to classes, properties, methods, or parameters, telling Angular how to process them.

- **Class decorators**: `@Component`, `@NgModule`, `@Injectable`, `@Directive`, `@Pipe`.
- **Property decorators**: `@Input`, `@Output`, `@ViewChild`, `@ContentChild`, `@HostBinding`.
- **Method decorators**: `@HostListener`.
- **Parameter decorators**: `@Inject`, `@Optional`, `@Self`, `@Host`.

### Q: What is the Angular CLI and which commands do you use most?
The Angular CLI scaffolds, builds, tests, and serves Angular apps with best practices baked in.

```bash
ng new my-app            # create a project
ng generate component x  # or ng g c x — scaffold a component
ng g s services/user     # generate a service
ng serve                 # dev server with live reload
ng build --configuration production  # production build (AOT + optimization)
ng test                  # run unit tests (Karma/Jasmine)
ng update                # migrate dependencies/versions
ng add @angular/material # add a library with schematics
```

### Q: Describe a typical Angular project structure.
```
src/
  app/
    components/      # reusable presentational components
    pages/          # routed feature components
    services/       # data/business logic, HTTP
    models/         # interfaces & types
    store/          # NgRx actions/reducers/effects/selectors
    shared/         # shared module, pipes, directives
    app.component.ts
    app.routes.ts (or app-routing.module.ts)
  assets/           # static files
  environments/     # environment.ts / environment.prod.ts
  main.ts           # bootstrap
  styles.scss       # global styles
angular.json        # workspace/build config
```

---

## Components & Templates

### Q: Explain all the Angular lifecycle hooks in order with their use cases.
Hooks run in this order:

1. **`ngOnChanges(changes)`** — called before `ngOnInit` and whenever an `@Input` changes. Use it to react to input changes; receives a `SimpleChanges` object.
2. **`ngOnInit()`** — called once after the first `ngOnChanges`. Best place for initialization: fetch data, set up subscriptions, initialize forms. Inputs are available here (not in the constructor).
3. **`ngDoCheck()`** — runs on every change detection cycle. Use for custom change detection; keep it cheap (runs very often).
4. **`ngAfterContentInit()`** — once after projected content (`ng-content`) is initialized. `@ContentChild` is available here.
5. **`ngAfterContentChecked()`** — after every check of projected content.
6. **`ngAfterViewInit()`** — once after the component's view and child views are initialized. `@ViewChild` is available here (e.g., access a DOM element or child component).
7. **`ngAfterViewChecked()`** — after every check of the component's view.
8. **`ngOnDestroy()`** — just before the component is destroyed. Clean up: unsubscribe from observables, detach event handlers, clear timers. Critical for preventing memory leaks.

```ts
export class ExampleComponent implements OnInit, OnChanges, OnDestroy, AfterViewInit {
  ngOnChanges(changes: SimpleChanges) { /* react to input changes */ }
  ngOnInit() { /* init data, forms, subscriptions */ }
  ngAfterViewInit() { /* access @ViewChild DOM */ }
  ngOnDestroy() { /* unsubscribe, cleanup */ }
}
```

### Q: Why initialize in ngOnInit instead of the constructor?
The constructor is a TypeScript/JS feature used only for dependency injection — when it runs, `@Input` properties are not yet set and the component isn't fully wired up. `ngOnInit` runs after Angular initializes the component and binds inputs, so it's the correct place for setup logic, HTTP calls, and form initialization. This also keeps construction cheap and makes testing easier.

### Q: What are the types of data binding in Angular?
1. **Interpolation** `{{ value }}` — component → view (text).
2. **Property binding** `[property]="value"` — component → view (DOM property).
3. **Event binding** `(event)="handler()"` — view → component.
4. **Two-way binding** `[(ngModel)]="value"` — both directions (syntactic sugar for `[value]` + `(valueChange)`).

```html
<h1>{{ title }}</h1>                       <!-- interpolation -->
<img [src]="imageUrl" />                    <!-- property -->
<button (click)="save()">Save</button>     <!-- event -->
<input [(ngModel)]="name" />               <!-- two-way -->
```

### Q: How does two-way binding actually work under the hood?
`[(ngModel)]="x"` is the "banana in a box" syntax. It expands to a property binding plus an event binding:

```html
<input [ngModel]="x" (ngModelChange)="x = $event" />
```
Any directive/component can support two-way binding if it exposes an `@Input() prop` and a matching `@Output() propChange` EventEmitter.

### Q: What are template reference variables?
A template reference variable (declared with `#`) gives you a reference to a DOM element, component, or directive within the template.

```html
<input #emailInput type="email" />
<button (click)="log(emailInput.value)">Log</button>
```
It's scoped to the template. For component access in the class, use `@ViewChild`.

### Q: What is the difference between ViewChild and ContentChild?
- **`@ViewChild`** queries elements/components/directives in the component's own template (its view). Available in `ngAfterViewInit`.
- **`@ContentChild`** queries projected content passed via `ng-content`. Available in `ngAfterContentInit`.

```ts
@ViewChild('emailInput') emailInput!: ElementRef;
@ViewChild(ChildComponent) child!: ChildComponent;
@ContentChild(HeaderComponent) header!: HeaderComponent;
```
Use `@ViewChildren` / `@ContentChildren` (returning a `QueryList`) for multiple matches.

### Q: What is ng-content and content projection?
Content projection lets a component render markup that the parent supplies, making components reusable (like a slot). `<ng-content>` marks where projected content appears.

```html
<!-- card.component.html -->
<div class="card">
  <ng-content select="[card-header]"></ng-content>
  <ng-content></ng-content>  <!-- default slot -->
</div>
```
```html
<!-- usage -->
<app-card>
  <h2 card-header>Title</h2>
  <p>Body content projected here</p>
</app-card>
```
I used this heavily for custom reusable components — generic cards, modals, and layout wrappers.

### Q: What is the difference between ng-template and ng-container?
- **`<ng-template>`** defines a template that is not rendered until explicitly used (e.g., by `*ngIf` else, or `ngTemplateOutlet`). It's a blueprint.
- **`<ng-container>`** is a logical, non-rendered grouping element — it groups elements or hosts a structural directive without adding an extra DOM node.

```html
<ng-container *ngIf="user; else loading">{{ user.name }}</ng-container>
<ng-template #loading>Loading...</ng-template>

<!-- avoid wrapper div -->
<ng-container *ngFor="let item of items">{{ item }}</ng-container>
```

### Q: What is the difference between structural and attribute directives?
- **Structural directives** change the DOM layout by adding/removing elements. Prefixed with `*` (desugars to `<ng-template>`). Examples: `*ngIf`, `*ngFor`, `*ngSwitch`.
- **Attribute directives** change the appearance or behavior of an existing element. Examples: `ngClass`, `ngStyle`, `ngModel`, and custom directives like `[appHighlight]`.

```ts
@Directive({ selector: '[appHighlight]' })
export class HighlightDirective {
  @HostListener('mouseenter') onEnter() { this.el.nativeElement.style.background = 'yellow'; }
  constructor(private el: ElementRef) {}
}
```

### Q: Explain *ngIf, *ngFor, and trackBy.
`*ngIf` conditionally renders an element (with optional `else`). `*ngFor` iterates over a collection.

```html
<div *ngIf="isLoggedIn; else guest">Welcome</div>
<ng-template #guest>Please log in</ng-template>

<li *ngFor="let item of items; let i = index; trackBy: trackById">
  {{ i }} - {{ item.name }}
</li>
```
**`trackBy`** tells Angular how to identify items so it reuses DOM nodes instead of re-rendering the whole list when the array changes. This is a key performance optimization for large lists.
```ts
trackById(index: number, item: Item) { return item.id; }
```

### Q: What is the difference between ngClass and ngStyle?
- **`ngClass`** adds/removes CSS classes conditionally.
- **`ngStyle`** sets inline styles conditionally.

```html
<div [ngClass]="{ active: isActive, disabled: !isEnabled }">...</div>
<div [ngStyle]="{ color: textColor, 'font-size.px': size }">...</div>
```

### Q: In Angular 17+, what are the new control flow blocks?
Angular 17 introduced built-in control flow that replaces structural directives with a cleaner syntax: `@if / @else`, `@for` (with mandatory `track`), and `@switch`. It's faster and doesn't require importing `CommonModule`.

```html
@if (user) {
  <p>{{ user.name }}</p>
} @else {
  <p>Loading…</p>
}

@for (item of items; track item.id) {
  <li>{{ item.name }}</li>
}
```
(Worth mentioning even from a 16 background to show awareness of the upgrade path.)

---

## Component Communication

### Q: How do parent and child components communicate?
- **Parent → Child**: `@Input()` properties.
- **Child → Parent**: `@Output()` with `EventEmitter`.

```ts
// child
@Input() count = 0;
@Output() countChange = new EventEmitter<number>();
increment() { this.countChange.emit(++this.count); }
```
```html
<!-- parent -->
<app-counter [count]="value" (countChange)="onChange($event)"></app-counter>
```

### Q: How do sibling components communicate?
Through a shared parent (parent mediates via Input/Output) or, more commonly, through a **shared service** holding state in a `Subject`/`BehaviorSubject`. Sibling A pushes; sibling B subscribes.

```ts
@Injectable({ providedIn: 'root' })
export class MessageService {
  private msg = new BehaviorSubject<string>('');
  msg$ = this.msg.asObservable();
  send(m: string) { this.msg.next(m); }
}
```
For complex app-wide state, NgRx is the better answer.

### Q: How can a service act as shared state between components?
A singleton service (`providedIn: 'root'`) is shared across the app. Holding a `BehaviorSubject` inside it lets any component read the current value and react to changes via the exposed observable — a lightweight alternative to NgRx for small/medium state.

---

## Services & Dependency Injection

### Q: What is dependency injection in Angular?
DI is a design pattern where a class receives its dependencies from an external injector rather than creating them itself. Angular's injector creates and supplies service instances based on type and provider configuration.

```ts
@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private http: HttpClient) {}
}

@Component({ /* ... */ })
export class UserComponent {
  constructor(private userService: UserService) {} // injected automatically
}
```
Benefits: loose coupling, testability (swap mocks), and singleton reuse.

### Q: Explain providedIn: 'root' vs module vs component.
- **`providedIn: 'root'`** — single app-wide singleton, tree-shakable (removed if unused). Default and preferred.
- **Provided in a module** (`providers: []` of an NgModule) — one instance per that module's injector; with lazy modules you get a separate instance.
- **Provided in a component** (`providers: []` in `@Component`) — a new instance per component instance (and its children). Use when each component needs its own state.

### Q: What are hierarchical injectors?
Angular has a tree of injectors mirroring the component tree (plus the root/module injectors). When a dependency is requested, Angular walks up from the component's injector until it finds a provider. This means you can override a service at a lower level and child components get the closer instance. Resolution modifiers: `@Self`, `@SkipSelf`, `@Optional`, `@Host`.

### Q: What is a singleton service?
A service with a single shared instance across the application — achieved with `providedIn: 'root'`. All injectors resolve to the same instance, making it ideal for shared state, caching, and cross-component communication.

### Q: What is an injection token and when do you use it?
`InjectionToken` provides a DI key for things that aren't classes — interfaces, primitives, or config objects (which have no runtime type to inject by).

```ts
export const API_URL = new InjectionToken<string>('api.url');

// provide
{ provide: API_URL, useValue: 'https://api.example.com' }

// inject
constructor(@Inject(API_URL) private apiUrl: string) {}
```

---

## Routing

### Q: How do you configure routes in Angular?
Define a `Routes` array mapping paths to components, then register it.

```ts
const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'users/:id', component: UserDetailComponent },
  { path: '**', component: NotFoundComponent }, // wildcard
];
// standalone: provideRouter(routes); NgModule: RouterModule.forRoot(routes)
```
The `<router-outlet>` marks where the matched component renders.

### Q: What is routerLink and how do you navigate programmatically?
`routerLink` is a directive for declarative navigation in templates. For code-driven navigation use the `Router` service.

```html
<a [routerLink]="['/users', id]" routerLinkActive="active">Profile</a>
```
```ts
constructor(private router: Router) {}
goHome() { this.router.navigate(['/home']); }
```

### Q: How do you read route params vs query params?
```ts
constructor(private route: ActivatedRoute) {}

ngOnInit() {
  // route param: /users/:id
  this.route.paramMap.subscribe(p => this.id = p.get('id'));
  // query param: /search?q=angular
  this.route.queryParamMap.subscribe(q => this.term = q.get('q'));
}
```
Use the observable form when the component is reused across param changes; `snapshot` is fine for one-time reads.

### Q: What are child routes?
Nested routes rendered inside a parent component's own `<router-outlet>`. Use the `children` array.

```ts
{
  path: 'dashboard',
  component: DashboardComponent,
  children: [
    { path: 'stats', component: StatsComponent },
    { path: 'settings', component: SettingsComponent },
  ],
}
```

### Q: What are route guards? Explain CanActivate, CanDeactivate, and Resolve.
Guards control navigation.
- **`CanActivate`** — can the user enter this route? (auth checks).
- **`CanActivateChild`** — guards child routes.
- **`CanDeactivate`** — can the user leave? (warn about unsaved form changes).
- **`Resolve`** — pre-fetch data before the route activates so the component loads with data ready.
- **`CanMatch`** (modern) — decide whether a route definition matches at all (great for feature flags / lazy loading).

```ts
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isLoggedIn() ? true : router.parseUrl('/login');
};
```
Angular 15+ favors functional guards (`CanActivateFn`) over class-based ones.

### Q: What is lazy loading and why use it?
Lazy loading loads a feature module/component only when its route is visited, instead of in the initial bundle. This shrinks the initial bundle, speeds up first load, and is essential for large apps.

```ts
// module-based
{ path: 'admin', loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule) }
// standalone (Angular 14+)
{ path: 'admin', loadComponent: () => import('./admin/admin.component').then(c => c.AdminComponent) }
```

---

## Forms

### Q: Template-driven vs reactive forms — what's the difference?
- **Template-driven**: logic lives in the template using `ngModel`; Angular builds the model implicitly. Good for simple forms. Asynchronous, harder to unit test.
- **Reactive (model-driven)**: the form model is defined in the component class (`FormGroup`, `FormControl`). Explicit, synchronous, type-safe, scalable, and easy to test. Preferred for complex/dynamic forms.

I primarily use reactive forms (and Angular Formly on top for dynamic, config-driven forms).

### Q: Explain FormControl, FormGroup, and FormArray.
- **`FormControl`** — a single input field with value and validation state.
- **`FormGroup`** — a collection of controls keyed by name (a form or sub-section).
- **`FormArray`** — a dynamic, indexed list of controls/groups (e.g., add/remove rows).

```ts
form = new FormGroup({
  name: new FormControl('', Validators.required),
  emails: new FormArray([ new FormControl('') ]),
});
addEmail() { (this.form.get('emails') as FormArray).push(new FormControl('')); }
```

### Q: What is FormBuilder?
A service that provides shorthand to build form controls/groups/arrays with less boilerplate.

```ts
constructor(private fb: FormBuilder) {}
form = this.fb.group({
  name: ['', Validators.required],
  address: this.fb.group({ city: [''], zip: [''] }),
  hobbies: this.fb.array([]),
});
```

### Q: How do validators work — built-in, custom, and async?
**Built-in**: `Validators.required`, `minLength`, `maxLength`, `pattern`, `email`, `min`, `max`.

**Custom (sync)** — a function returning an error object or null:
```ts
function noSpaces(c: AbstractControl): ValidationErrors | null {
  return c.value?.includes(' ') ? { noSpaces: true } : null;
}
new FormControl('', [Validators.required, noSpaces]);
```

**Async** — returns an Observable/Promise; useful for server checks like username availability:
```ts
function uniqueUsername(api: ApiService): AsyncValidatorFn {
  return c => api.checkUsername(c.value).pipe(
    map(taken => taken ? { taken: true } : null)
  );
}
new FormControl('', { asyncValidators: [uniqueUsername(api)] });
```

### Q: How do you build dynamic forms? (Angular Formly)
Dynamic forms are generated from configuration/metadata rather than hardcoded markup. **Angular Formly** drives the form from a JSON-like field config array, which is ideal when form structure comes from an API or changes frequently.

```ts
fields: FormlyFieldConfig[] = [
  { key: 'firstName', type: 'input', props: { label: 'First Name', required: true } },
  { key: 'email', type: 'input', props: { label: 'Email', type: 'email' } },
];
```
```html
<form [formGroup]="form">
  <formly-form [form]="form" [fields]="fields" [model]="model"></formly-form>
</form>
```
In my projects I used Formly with custom field types and wrappers to render complex, config-driven forms (including Kendo UI controls), which dramatically reduced repetitive template code.

---

## RxJS & Observables

### Q: What is the difference between an Observable and a Promise?
| Observable | Promise |
|---|---|
| Emits multiple values over time (stream) | Resolves a single value |
| Lazy — runs only when subscribed | Eager — runs immediately |
| Cancellable (unsubscribe) | Not cancellable |
| Rich operators (map, filter, etc.) | `.then`/`.catch` only |

Angular's `HttpClient` returns Observables.

### Q: What is the difference between Subject, BehaviorSubject, and ReplaySubject?
- **`Subject`** — multicast; subscribers only get values emitted after they subscribe. No initial value.
- **`BehaviorSubject`** — requires an initial value and emits the latest/current value immediately to new subscribers. Ideal for state.
- **`ReplaySubject`** — replays a configurable buffer of previous values to new subscribers.
- (`AsyncSubject` — emits only the last value, on completion.)

```ts
const b = new BehaviorSubject<number>(0);
b.subscribe(v => console.log('A', v)); // A 0
b.next(1);                              // A 1
b.subscribe(v => console.log('B', v)); // B 1 (gets current value)
```

### Q: Explain map, filter, switchMap, mergeMap, concatMap, and exhaustMap — when do you use each?
- **`map`** — transform each emitted value.
- **`filter`** — let values through only if they pass a predicate.

The "flattening" operators handle inner observables (e.g., HTTP calls triggered by a stream):
- **`switchMap`** — cancels the previous inner observable when a new value arrives. Best for type-ahead search and routing — you only care about the latest.
- **`mergeMap`** (flatMap) — runs all inner observables concurrently, no ordering. Use for independent parallel work (e.g., multiple uploads).
- **`concatMap`** — queues inner observables and runs them one at a time in order. Use when order matters and operations must not overlap (sequential writes).
- **`exhaustMap`** — ignores new values while the current inner observable is running. Best for preventing duplicate submits (e.g., login button spam).

```ts
this.searchControl.valueChanges.pipe(
  debounceTime(300),
  distinctUntilChanged(),
  switchMap(term => this.api.search(term)) // cancel stale searches
).subscribe(results => this.results = results);
```

### Q: How do you subscribe and unsubscribe? Why does it matter?
You subscribe to consume values. If you never unsubscribe from long-lived observables, the subscription persists after the component is destroyed → **memory leak** and unexpected callbacks.

Cleanup options:
1. **`async` pipe** (preferred) — auto-subscribes/unsubscribes in the template.
2. **`takeUntil`** pattern with `ngOnDestroy`.
3. **`takeUntilDestroyed()`** (Angular 16+) — ties subscription lifetime to the injection context.
4. Manual `subscription.unsubscribe()` in `ngOnDestroy`.

### Q: What is the async pipe and why prefer it?
The `async` pipe subscribes to an observable/promise in the template, returns the latest value, and unsubscribes automatically when the component is destroyed.

```html
<div *ngIf="user$ | async as user">{{ user.name }}</div>
```
It removes manual subscription management, prevents leaks, and works well with OnPush change detection.

### Q: Explain the takeUntil pattern for preventing memory leaks.
Emit on a destroy `Subject` in `ngOnDestroy`, and pipe `takeUntil(destroy$)` into every subscription so they all complete together.

```ts
private destroy$ = new Subject<void>();

ngOnInit() {
  this.service.data$.pipe(takeUntil(this.destroy$)).subscribe(d => this.data = d);
}
ngOnDestroy() {
  this.destroy$.next();
  this.destroy$.complete();
}
```
Angular 16's `takeUntilDestroyed()` is the modern, less boilerplate equivalent.

### Q: How do you handle errors in RxJS?
Use `catchError` to intercept and recover (return a fallback or rethrow), and `retry`/`retryWhen` to re-attempt.

```ts
this.api.getData().pipe(
  retry(2),
  catchError(err => {
    this.toast.error('Failed to load');
    return of([]); // graceful fallback
  })
).subscribe(data => this.data = data);
```

---

## State Management (NgRx)

### Q: What is NgRx and what are its core building blocks?
NgRx is a Redux-style, reactive state management library for Angular built on RxJS. It provides a single immutable store as the source of truth, with a strict unidirectional data flow.

Core pieces:
- **Store** — the single immutable state container.
- **Actions** — describe unique events (`{ type, payload }`).
- **Reducers** — pure functions that take current state + action → new state.
- **Selectors** — pure, memoized functions to query slices of state.
- **Effects** — handle side effects (HTTP, async) by listening to actions and dispatching new ones.

```ts
// action
export const loadUsers = createAction('[Users] Load');
export const loadUsersSuccess = createAction('[Users] Load Success', props<{ users: User[] }>());

// reducer
const reducer = createReducer(initialState,
  on(loadUsersSuccess, (state, { users }) => ({ ...state, users })));

// selector
export const selectUsers = createSelector(selectUserState, s => s.users);

// effect
loadUsers$ = createEffect(() => this.actions$.pipe(
  ofType(loadUsers),
  switchMap(() => this.api.getUsers().pipe(
    map(users => loadUsersSuccess({ users })),
    catchError(() => of(loadUsersFailure()))
  ))
));
```

### Q: What are NgRx effects for?
Effects isolate side effects (HTTP calls, navigation, local storage) from components and reducers. They listen to dispatched actions, perform async work, and dispatch result actions. Reducers stay pure; components just dispatch and select.

### Q: What are selectors and why are they memoized?
Selectors are pure functions that derive/compose data from the store. `createSelector` memoizes the result — it only recomputes when its input slices change, avoiding unnecessary recalculation and triggering change detection efficiently.

### Q: When do you use NgRx vs a simple service with a BehaviorSubject?
Use **NgRx** when: state is large/shared across many features, there are complex interactions, you need time-travel debugging, predictability, and clear separation of side effects. Use a **service + BehaviorSubject** when: state is simple/local, the team is small, and NgRx's boilerplate isn't justified. Don't add NgRx by default — it's overhead for small apps.

---

## HTTP

### Q: How do you use HttpClient?
`HttpClient` provides typed, observable-based HTTP methods. Inject it and call `get/post/put/delete`.

```ts
@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private http: HttpClient) {}
  getUsers(): Observable<User[]> {
    return this.http.get<User[]>('/api/users');
  }
  createUser(u: User) {
    return this.http.post<User>('/api/users', u);
  }
}
```
Requests are lazy — they fire only on subscription (or via `async` pipe).

### Q: What are HTTP interceptors? Give auth and error examples.
Interceptors sit in the HTTP pipeline and can transform every outgoing request and incoming response — ideal for attaching auth tokens, logging, and centralized error handling.

```ts
// functional interceptor (Angular 15+)
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getToken();
  const cloned = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;
  return next(cloned);
};

export const errorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) inject(Router).navigate(['/login']);
      return throwError(() => err);
    })
  );
```
Register via `provideHttpClient(withInterceptors([authInterceptor, errorInterceptor]))`.

### Q: How do you handle HTTP errors gracefully?
Combine interceptor-level global handling (401/403/500, toasts) with local `catchError` for context-specific fallbacks, plus `retry` for transient failures.

```ts
this.userService.getUsers().pipe(
  catchError(err => { this.error = 'Could not load users'; return of([]); })
).subscribe(users => this.users = users);
```

---

## Pipes

### Q: What are pipes? Name common built-in ones.
Pipes transform displayed values in templates without changing the underlying data. Built-ins: `date`, `uppercase`/`lowercase`, `titlecase`, `currency`, `decimal`/`number`, `percent`, `json`, `slice`, `async`, `keyvalue`.

```html
<p>{{ today | date:'dd/MM/yyyy' }}</p>
<p>{{ price | currency:'INR' }}</p>
<p>{{ name | uppercase }}</p>
```

### Q: What is the difference between pure and impure pipes?
- **Pure** (default) — runs only when the input reference changes. Fast and cache-friendly.
- **Impure** (`pure: false`) — runs on every change detection cycle, even if the reference didn't change. Needed for mutable data (e.g., filtering an array mutated in place) but can hurt performance. `async` pipe is impure by design.

### Q: How do you create a custom pipe?
Implement `PipeTransform` and decorate with `@Pipe`.

```ts
@Pipe({ name: 'truncate', standalone: true })
export class TruncatePipe implements PipeTransform {
  transform(value: string, limit = 20): string {
    return value.length > limit ? value.slice(0, limit) + '…' : value;
  }
}
```
```html
{{ longText | truncate:50 }}
```

---

## Performance & Change Detection

### Q: What is change detection? Default vs OnPush.
Change detection is how Angular keeps the DOM in sync with component data. By **default**, after any async event (click, HTTP, timer), Angular checks the entire component tree.

**`OnPush`** tells Angular to skip a component unless: an `@Input` reference changes, an event originates in the component, or an observable bound via `async` pipe emits. This drastically reduces checks in large trees.

```ts
@Component({ changeDetection: ChangeDetectionStrategy.OnPush, /* ... */ })
```
OnPush works best with immutable data and the `async` pipe — a key technique for performant reusable components.

### Q: What is zone.js and its role in change detection?
zone.js monkey-patches async APIs (events, timers, XHR/fetch, promises) so Angular knows when something might have changed state and can trigger change detection automatically — without manual calls. Angular 16+ introduces **signals** and is moving toward optional zoneless change detection for finer control and performance.

### Q: How does trackBy improve performance?
Without `trackBy`, `*ngFor` re-creates all DOM nodes when the array reference changes. With `trackBy` returning a stable identity (like an id), Angular reuses existing DOM nodes for unchanged items and only updates what changed — major win for large/frequently-updated lists.

### Q: What is the difference between AOT and JIT compilation?
- **JIT (Just-in-Time)** — compiles templates in the browser at runtime. Used historically in dev. Larger bundle, slower startup, runtime template errors.
- **AOT (Ahead-of-Time)** — compiles during the build. Smaller, faster, more secure (no eval), and catches template errors at build time. AOT is the **default for production** (and since Angular 9+ with Ivy, default for dev too).

### Q: What techniques do you use to optimize an Angular app?
- Lazy load feature routes (`loadChildren`/`loadComponent`).
- `OnPush` change detection + immutable data + `async` pipe.
- `trackBy` in lists.
- Unsubscribe / use `async` pipe to avoid leaks.
- Pure pipes over method calls in templates.
- AOT + production build (tree-shaking, minification).
- Bundle analysis (`source-map-explorer`), code splitting, preloading strategies.
- Virtual scrolling (CDK) for large lists.
- Defer non-critical work; debounce expensive streams.

---

## Testing

### Q: What tools are used for Angular unit testing?
**Jasmine** is the testing/assertion framework (`describe`, `it`, `expect`, spies), and **Karma** is the test runner that executes tests in a real browser. The Angular CLI wires both up via `ng test`. (Many teams now switch to **Jest** for speed.)

### Q: What is TestBed?
`TestBed` is Angular's primary testing utility — it configures and creates a testing module to instantiate components/services with their dependencies (and mocks).

```ts
describe('UserCardComponent', () => {
  let fixture: ComponentFixture<UserCardComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [UserCardComponent],            // standalone
      providers: [{ provide: UserService, useValue: mockUserService }],
    });
    fixture = TestBed.createComponent(UserCardComponent);
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });
});
```

### Q: How do you test a service that uses HttpClient?
Use `HttpClientTestingModule` and `HttpTestingController` to mock requests and assert on them, avoiding real network calls.

```ts
TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
const httpMock = TestBed.inject(HttpTestingController);
service.getUsers().subscribe(users => expect(users.length).toBe(2));
httpMock.expectOne('/api/users').flush([{}, {}]);
```

---

## Migration & Versioning

### Q: How do you upgrade/migrate Angular versions? (Your Angular 16 migration)
The supported path is incremental, one major version at a time, using the CLI and the official update guide.

```bash
ng update @angular/core @angular/cli
```
`ng update` runs **schematics** that automatically apply code migrations (e.g., updated APIs, renamed symbols, removed deprecations). My approach:
1. Use `update.angular.io` to check breaking changes between versions.
2. Update one major at a time, build and run tests after each.
3. Update third-party libs (Kendo UI, Formly, NgRx, Ionic) to compatible versions.
4. Adopt new features incrementally (standalone components, functional guards/interceptors, `inject()`, `takeUntilDestroyed`).
5. Verify with the unit test suite and manual smoke tests.

### Q: What notable changes came across recent Angular versions?
- **Angular 14** — standalone components (preview), typed reactive forms, `inject()` function, functional route guards.
- **Angular 15** — standalone APIs stabilized, functional `HttpClient` providers/interceptors, directive composition API, `NgOptimizedImage`.
- **Angular 16** — **signals** (developer preview), `takeUntilDestroyed`, required inputs, non-destructive hydration for SSR, esbuild-based dev server, self-closing tags.
- **Angular 17** — new control flow (`@if/@for/@switch`), deferrable views (`@defer`), new branding/docs, Vite + esbuild by default, improved SSR.
- **Angular 18/19** — zoneless change detection (experimental), signal-based APIs maturing, stable material 3.

### Q: What was your overall role in the Angular 16 migration?
Summary answer: I led/contributed to migrating legacy Angular apps to Angular 16 — upgrading incrementally with `ng update`, resolving breaking changes, updating third-party dependencies (Kendo UI, Formly, NgRx, Ionic), refactoring toward standalone components and modern functional guards/interceptors, fixing failing unit tests, and validating performance with OnPush and proper RxJS cleanup. This improved bundle size, type safety (typed forms), and maintainability.

---

## Quick-Fire Bonus

### Q: What is the difference between declarations, imports, and providers in NgModule?
`declarations` = components/directives/pipes that belong to this module; `imports` = other modules whose exported features this module uses; `providers` = services registered with the injector.

### Q: What is the difference between forRoot and forChild?
`RouterModule.forRoot(routes)` is called once in the root module and sets up the router service + root routes. `forChild(routes)` is used in feature/lazy modules and registers additional routes without re-creating the router service.

### Q: What is the inject() function?
A modern alternative to constructor injection that retrieves a dependency from the current injection context — usable in functional guards, interceptors, field initializers, and factories.
```ts
private userService = inject(UserService);
```

### Q: What is the purpose of environment files?
`environment.ts` / `environment.prod.ts` hold per-build configuration (API URLs, feature flags). The CLI swaps them via `fileReplacements` during production builds.

### Q: How does Ionic relate to Angular in your experience?
Ionic is a UI framework for building cross-platform mobile/PWA apps using Angular (its components, routing, and lifecycle integrate with Angular). I built Angular-based Ionic apps using Ionic UI components, lifecycle events (`ionViewWillEnter`, etc.), and Capacitor for native features — reusing Angular services, reactive forms, and NgRx across web and mobile.
