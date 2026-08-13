# 1. Front-End (Angular) — Interview Q&A tied to Robogebra

**Stack:** Angular 16.2 (`robogebra-web`, `robogebra-backoffice`), Angular 18 + Ionic 8 (`robogebra-mobile`). NgModule architecture, zone.js, RxJS-heavy, service-based state (no NgRx).

---

## Q1. How do you prevent memory leaks in Angular? *(the #1 question)*

**Answer.** Memory leaks in Angular almost always come from **RxJS subscriptions that outlive their component**. If you `subscribe()` and never unsubscribe, the observable keeps a reference to the component, so it's never garbage-collected, and its callback keeps firing after the view is gone. I handle it three ways, in order of preference:

1. **`async` pipe** in the template — Angular subscribes and unsubscribes automatically.
2. **`takeUntil(destroy$)`** for imperative subscriptions — a `Subject` I complete in `ngOnDestroy`.
3. Manual `Subscription.unsubscribe()` only for one-off cases.

**In Robogebra.** The `takeUntil` + `destroy$`/`unSubscribe$` pattern is our standard, in basically every component. Example — `robogebra-web/src/app/app.component.ts`:

```typescript
export class AppComponent implements OnInit, OnDestroy {
  private destroy$: Subject<void> = new Subject<void>();

  ngOnInit(): void {
    this.authService.isLoggedInObservable()
      .pipe(takeUntil(this.destroy$))
      .subscribe(isLoggedIn => { /* handle */ });
  }

  ngOnDestroy(): void {
    this.destroy$.next();      // emits → all takeUntil pipes complete
    this.destroy$.complete();  // releases the Subject itself
  }
}
```

A subtle real one — even our **HTTP interceptor** implements `OnDestroy` and cancels in-flight requests with a `cancelRequests$` subject (`header-authorization.interceptor.ts`), so a token-refresh retry can't leak. And in the template we lean on the `async` pipe (`app.component.html`) so those subscriptions clean themselves up:

```html
<ng-container *ngIf="isLoggedIn$() | async"> ... </ng-container>
```

**Follow-up: "How would you enforce it?"** In Angular 16+ you can use `takeUntilDestroyed()` (from `@angular/core/rxjs-interop`) to drop the boilerplate `destroy$` Subject — that's the direction I'd move us toward.

---

## Q2. Explain the RxJS operators you actually use, and why.

**Answer.** The ones I reach for most and *why*:

- **`switchMap`** — cancel the previous inner observable when a new value arrives. Perfect for "latest wins" like search or token refresh.
- **`combineLatest`** — combine the latest values of several streams into a derived view.
- **`debounceTime`** — rate-limit rapid user input before an expensive call.
- **`BehaviorSubject`** — hold state with a current value; expose read-only via `asObservable()`.
- **`catchError`** — recover/rethrow inside a stream.

**In Robogebra.**

**`switchMap` for silent token refresh** (`header-authorization.interceptor.ts`) — if the access token expired, refresh it and *switch* the original request onto the new token:

```typescript
return this.authService.refreshToken().pipe(
  filter(newAuth => !!newAuth),
  switchMap(newAuth => {
    const refreshed = req.clone({ setHeaders: { Authorization: `Bearer ${newAuth.token}` } });
    return next.handle(refreshed).pipe(takeUntil(this.cancelRequests$));
  }),
  catchError(err => { this.authService.setSessionExpired(true); return throwError(() => err); })
);
```

**`combineLatest`** to decide UI visibility from two streams (`app.component.ts`):

```typescript
return combineLatest([
  this.exerciseSolutionService.getFullScreenStateAsObservable(),
  this.anonymousLessonService.getAnonymousCodeAsObservable()
]).pipe(map(([isFullScreen, anonCode]) => !isFullScreen && !anonCode), takeUntil(this.destroy$));
```

**`debounceTime(500)` + `switchMap`** to throttle expensive worksheet execution (`exercise-solution-executor.component.ts`):

```typescript
this.executeWorksheetSubject$.pipe(
  debounceTime(500),
  switchMap(payload => this.solutionExecutorService.execute(payload)),
  takeUntil(this.unSubscribe$)
).subscribe();
```

**Follow-up: `switchMap` vs `mergeMap` vs `concatMap`?** `switchMap` cancels the previous (latest wins — search, refresh); `mergeMap` runs all in parallel (independent writes); `concatMap` queues them in order (sequential, order matters). I use `switchMap` for the token refresh precisely because I *want* to abandon a stale refresh.

---

## Q3. How does change detection work, and do you use OnPush?

**Answer.** Angular's default change detection runs a dirty-check over the whole component tree whenever something *might* have changed — triggered by zone.js patching async APIs (events, timers, XHR). `ChangeDetectionStrategy.OnPush` narrows that: the component is only checked when an `@Input` reference changes, an event fires in it, or an observable it uses (via `async`) emits — which is a big perf win on large trees.

**In Robogebra (honest).** Our apps run the **default** strategy, not OnPush. Where we need to force a refresh imperatively — e.g. after a payment callback that happens outside Angular's awareness — we inject `ChangeDetectorRef` and call `detectChanges()` (`payments.component.ts`):

```typescript
constructor(private cdr: ChangeDetectorRef) {}
onPaymentComplete(): void { this.updatePaymentStatus(); this.cdr.detectChanges(); }
```

**What I'd improve.** Heavy list components (chapter/exercise lists, analytics) are good OnPush candidates — combined with the `async` pipe and immutable updates, that would cut redundant checks significantly. That's a concrete perf story I can tell.

---

## Q4. Zone.js vs zoneless — what's the difference, and what does your project use?

**Answer.** **zone.js** monkey-patches async browser APIs so Angular *knows* when to run change detection — it's the "magic" behind not having to tell Angular when data changed. **Zoneless** (stable in Angular 19, experimental in 18) removes that: you drop the `zone.js` polyfill and use `provideZonelessChangeDetection()`; Angular then relies on **signals**, the `async` pipe, and explicit `markForCheck()` to know when to update. Benefits: smaller bundle (no zone.js), faster, and no "ran outside Angular" surprises.

**In Robogebra (honest).** We're **zone-based**. `robogebra-web` bootstraps the classic way and ships `zone.js ~0.13`:

```typescript
// main.ts
platformBrowserDynamic().bootstrapModule(AppModule).catch(err => console.error(err));
```

No `provideZonelessChangeDetection` anywhere. So my honest answer is: *"Our app is Angular 16 with zone.js. I understand zoneless and where it's going — to migrate I'd first move hot components to OnPush + signals, replace manual subscriptions with the `async` pipe, then flip the zoneless provider and remove the polyfill."* That shows I know it without pretending we shipped it.

---

## Q5. Standalone components & Signals — do you use them?

**Answer (honest).** Not yet — our codebase is **100% NgModule** (`AppModule`, `SharedModule`, `CoreModule`, `FeaturesModule`) and predates signals. I know both:
- **Standalone components** remove NgModules — a component declares its own `imports`. Simpler, better tree-shaking, `bootstrapApplication()` instead of `bootstrapModule()`.
- **Signals** — a reactive primitive (`signal()`, `computed()`, `effect()`) that makes change detection precise and is the foundation of zoneless.

**In Robogebra.** `app.module.ts` shows the classic structure — feature modules + `APP_INITIALIZER`:

```typescript
@NgModule({
  declarations: [AppComponent],
  imports: [BrowserModule, HttpClientModule, AppRoutingModule, SharedModule, FeaturesModule, CoreModule, ...],
  providers: [{ provide: APP_INITIALIZER, useFactory: initializeAnonymousLesson, deps: [AnonymousLessonService], multi: true }],
  bootstrap: [AppComponent]
})
export class AppModule {}
```

That `APP_INITIALIZER` is itself a good talking point — we use it to bootstrap anonymous-lesson state *before* the app renders.

---

## Q6. How does Dependency Injection work in your app?

**Answer.** Angular DI is hierarchical. I make services singletons with `@Injectable({ providedIn: 'root' })` — tree-shakeable, one instance app-wide — and inject them via the constructor. For things scoped to a module (like interceptors) I register them in that module's `providers`.

**In Robogebra.** `auth.service.ts`:

```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private router: Router, private http: HttpClient,
              private notificationService: NotificationService,
              private busySpinnerService: BusySpinnerService, ...) {}
}
```

The interceptor uses plain `@Injectable()` (no `providedIn`) because it's registered in the module provider array with the `HTTP_INTERCEPTORS` multi-token.

---

## Q7. How are routes protected? Explain your auth guard.

**Answer.** With **route guards** — `CanActivate` decides if a route may be entered. My guard returns an `Observable<boolean>`: it checks login state reactively, and on failure it **stashes the attempted URL** and redirects to login so the user lands back where they wanted after signing in. It also enforces **role-based** access.

**In Robogebra.** Wired in `app-routing.module.ts`:

```typescript
{ path: AppRoutes.DASHBOARD, component: DashboardComponent,
  canActivate: [AuthGuardService],
  resolve: { [AppResolver.USER_SUMMARY]: UserSummaryResolver,
             [AppResolver.SUBSCRIPTION_PLAN]: SubscriptionPlanResolver } }
```

The guard (`core/guard/auth-guard.service.ts`):

```typescript
@Injectable({ providedIn: 'root' })
export class AuthGuardService implements CanActivate {
  canActivate(route, state): Observable<boolean> {
    return this.authService.isLoggedInObservable().pipe(
      tap(isLoggedIn => {
        if (!isLoggedIn) {
          this.authService.setRedirectUrl(state.url);   // remember where they wanted to go
          this.router.navigate(['/login']);              // redirect on failure
        }
      }),
      map(isLoggedIn => {
        if (!isLoggedIn) return false;
        if (this.authService.isLoggedInAsParent()) { /* restrict parent to allowed routes */ }
        return true;
      })
    );
  }
}
```

**The full chain:** route has `canActivate: [AuthGuardService]` → guard asks `AuthService` for login state → if false, save URL + redirect → if true (and role allowed), the route activates. It's async (returns an Observable) so it works even before the auth state has resolved.

---

## Q8. Where do you use Route Resolvers, and why?

**Answer.** A **resolver** pre-fetches data *before* a route activates, so the component never renders in a half-loading state — the data is ready in `route.data` on `ngOnInit`. I also use it to drive a global loading spinner around the navigation.

**In Robogebra.** `core/resolvers/chapter.resolver.ts` pre-loads a chapter before its page opens:

```typescript
@Injectable({ providedIn: 'root' })
export class ChapterResolver {
  resolve(route, state): Observable<ChapterModel> {
    this.resolverService.startResolver();                 // show spinner
    const chapterId = route.params['chapterId'];          // from URL /chapter/:chapterId
    return this.chapterListService.fetchChapterDetailsById(chapterId, false)
      .pipe(finalize(() => this.resolverService.endResolver())); // hide spinner
  }
}
```

Wired with `resolve: { [AppResolver.CHAPTER]: ChapterResolver }`, and the component reads it:

```typescript
this.route.data.subscribe(data => this.chapterData = data[AppResolver.CHAPTER]);
```

**Flow:** navigate to `/chapter/123` → router runs `ChapterResolver` → waits for the HTTP call → *then* activates `ChapterComponent` with data already present. We use resolvers for chapter details, user summary, subscription plan, and domain config on the dashboard/login routes.

**Trade-off I'd mention:** resolvers delay navigation until data arrives, which can feel slow on a bad network — so I only resolve *critical* above-the-fold data and lazy-load the rest inside the component.

---

## Q9. Explain your HTTP interceptor.

**Answer.** An `HttpInterceptor` sits in the pipeline for every request/response. Mine does three jobs: (1) attach the `Authorization: Bearer` header and default `Content-Type`, (2) centralize error handling with `catchError`, and (3) transparently **refresh an expired token and retry** the original request — so components never deal with 401s.

**In Robogebra** (`header-authorization.interceptor.ts`):

```typescript
intercept(req, next): Observable<HttpEvent<any>> {
  let headers = req.headers;
  if (!headers.has('Content-Type') && !(req.body instanceof FormData))
    headers = headers.set('Content-Type', 'application/json');
  const auth = this.authService.authDetails;
  if (auth) headers = headers.set('Authorization', `Bearer ${auth.token}`);
  return this.invokeHttpCall(req.clone({ headers }), next);
}
private invokeHttpCall(req, next) {
  return next.handle(req).pipe(
    takeUntil(this.cancelRequests$),
    catchError(err => this.handleUnAuthResponse(err, req, next)) // 401 → refresh + retry
  );
}
```

Note the **immutability** — `HttpRequest` is read-only, so I `clone()` it to add headers. That's a classic interview point.

---

## Q10. Reactive forms & custom validators?

**Answer.** I use **reactive forms** (`FormBuilder`/`FormGroup`) for anything non-trivial — they're typed, testable, and support dynamic + cross-field validation. For cross-field rules (like password match) I put a **group-level validator**; for conditional rules I swap validators at runtime with `setValidators()` + `updateValueAndValidity()`.

**In Robogebra.** Signup form (`signup.component.ts`) with a group-level password-match validator:

```typescript
this.signupFormGroup = this.formBuilder.group({
  displayName: ['', [Validators.required]],
  userName:    ['', [Validators.required]],
  password:    ['', [Validators.required]],
  confirmPassword: ['', [Validators.required]],
  otp: ['']
}, { validators: this.passwordMatchValidator() });
```

Dynamic validators — the username field validates as **phone or email** depending on the chosen login mode (`auth-validation.service.ts`):

```typescript
if (loginBy === LoginBy.PHONE_NUMBER)
  userNameCtrl.setValidators([Validators.required, this.phoneNumberValidator(getCountryCode)]);
else
  userNameCtrl.setValidators([Validators.required, this.emailValidator()]);
userNameCtrl.updateValueAndValidity();
```

We also react to `valueChanges` (with `takeUntil`) to toggle UI, e.g. detect a phone vs email input live.

---

## Q11. Parent–child component communication?

**Answer.** `@Input()` for parent→child data, `@Output() EventEmitter` for child→parent events. For sibling/cross-tree communication I use a shared service with a `BehaviorSubject`.

**In Robogebra.** The renewal modal (`renew-confirmation-modal.component.ts`) takes config via inputs and emits the decision:

```typescript
@Input() finalAmount: number;
@Input() couponCode: string;
@Output() onPaymentConfirmation = new EventEmitter<boolean>();
onContinue(): void { this.onPaymentConfirmation.emit(true); }
```

```html
<app-renew-confirmation-modal [finalAmount]="amount" (onPaymentConfirmation)="handlePayment($event)">
</app-renew-confirmation-modal>
```

---

## Q12. How do you manage state without NgRx?

**Answer.** With **services holding `BehaviorSubject`s** — the "service with a subject" pattern. Private subject (has a current value), exposed read-only via `asObservable()`, updated only through setter methods. It gives me single-source-of-truth and reactive consumers without NgRx's boilerplate. NgRx makes sense when state is large, shared, and needs time-travel/devtools; for our scale, services are simpler.

**In Robogebra** (`auth.service.ts`):

```typescript
private loggedInBSubject$   = new BehaviorSubject<boolean>(false);
private sessionExpiredBSubject$ = new BehaviorSubject<boolean>(false);

isLoggedInObservable(): Observable<boolean> { return this.loggedInBSubject$.asObservable(); }
setSessionExpired(v: boolean): void { this.sessionExpiredBSubject$.next(v); }
```

Consumers can *read* but can't `next()` into the subject — encapsulation. Same pattern in the mobile app (`exercise-solution-slide.service.ts`).

---

## Q13. Performance optimizations you've applied?

**Answer.** (1) **`trackBy`** on every meaningful `*ngFor` so Angular reuses DOM nodes instead of destroying/recreating on list changes; (2) the **`async` pipe** to avoid manual subscription overhead and leaks; (3) **lazy-loaded** route modules; and going forward, **OnPush** on heavy trees.

**In Robogebra.** `trackBy` in the analytics/progress lists (`chapter-progress-list.component.ts`, `student-analytics.component.ts`):

```typescript
trackByChapterId(_i: number, chapter: ChapterProgressModel): string { return chapter.id; }
```
```html
<div *ngFor="let chapter of chapters; trackBy: trackByChapterId">{{ chapter.name }}</div>
```

And `async`-pipe-driven template sections in `app.component.html` so those streams never need manual cleanup.

---

## Q14. How do you show backend errors to users? (e.g. "email already exists")

**Answer.** I never let raw HTTP errors reach the component. The service `catchError`s, hands the `HttpErrorResponse` to a central `NotificationService`, which extracts the backend's `error.message` and shows a toast. This keeps error UX consistent everywhere.

**In Robogebra.** Signup path — component triggers a subject, service does the POST, error flows to notifications:

```typescript
// auth.service.ts
createUser(payload): Observable<string> {
  return this.http.post(url, payload, { responseType: 'text' }).pipe(
    catchError(err => this.throwErrorMessage(err)),   // → NotificationService
    map(r => r)
  );
}
private throwErrorMessage(err: HttpErrorResponse): Observable<any> {
  this.busySpinnerService.setSaveInProgress(false);
  this.notificationService.handleErrorResponse(err, null, true, true); // shows toast
  return EMPTY;                                         // swallow → stream completes cleanly
}
```

`NotificationService` prioritizes `error.message` from the backend (e.g. *"An account with this email already exists."* — returned by the portal) and toasts it for ~5s. **End-to-end:** duplicate email → backend `APIException` → JSON `{message}` → interceptor/service `catchError` → `NotificationService` toast → spinner stops. (Backend side of this exact flow is in `07-aws.md` / `02-backend.md`.)

---

### Rapid-fire recap

| Topic | Robogebra pattern | File |
|-------|-------------------|------|
| Memory leaks | `takeUntil(destroy$)` + `async` pipe | `app.component.ts`, interceptor |
| RxJS | `switchMap` token refresh, `combineLatest`, `debounceTime` | interceptor, executor |
| Change detection | default + manual `detectChanges()` | `payments.component.ts` |
| Zoneless | **No** — zone.js, Angular 16 (know the migration) | `main.ts` |
| DI | `providedIn: 'root'` + constructor injection | `auth.service.ts` |
| Guards | `CanActivate` → save URL + redirect | `auth-guard.service.ts` |
| Resolvers | pre-fetch chapter before route | `chapter.resolver.ts` |
| Interceptor | header + 401 refresh/retry | `header-authorization.interceptor.ts` |
| Forms | reactive + group/dynamic validators | `signup.component.ts` |
| State | service + `BehaviorSubject` (no NgRx) | `auth.service.ts` |
| Perf | `trackBy`, `async`, lazy load | progress/analytics lists |
