# 1. Front-End (Angular) — Interview Q&A tied to EasyVisa

**Stack:** Angular (NgModule architecture), RxJS-heavy, reactive forms + **Angular Formly** for dynamic forms, `HttpClient` against a Grails/Groovy REST backend. I owned the **document portal** and the **dynamic forms**, built reusable components, customized an image cropper, wrote Jasmine/Karma unit tests, and drove an Angular version upgrade.

> Golden rule for every answer below: define it cleanly, then land it with **"In EasyVisa, I…"**.

---

## Q1. Explain Angular lifecycle hooks — which did you actually use?

**Answer.** Lifecycle hooks let me run code at defined moments in a component's life:

- **`ngOnChanges(changes)`** — runs when an `@Input` reference changes (before `ngOnInit` and on every input update).
- **`ngOnInit()`** — once, after the first inputs are set. Where I do initial data loads and form setup.
- **`ngAfterViewInit()`** — after the component's view (and `@ViewChild` refs) are ready. Needed when I touch a child component/DOM element directly.
- **`ngOnDestroy()`** — right before Angular tears the component down. Where I clean up subscriptions.

**In EasyVisa.** In the document portal upload panels I used all the key ones concretely:
- `ngOnInit` to build the Formly config and fetch the already-uploaded documents for the case.
- `ngOnChanges` because each accordion panel took the document-type config as an `@Input` — when the selected case changed, `ngOnChanges` reset the panel's state.
- `ngAfterViewInit` for the **image cropper** — I needed the cropper's `@ViewChild` to exist before wiring the crop output.
- `ngOnDestroy` to `next()` + `complete()` my `destroy$` subject so in-flight upload progress subscriptions didn't leak.

```typescript
export class DocumentUploadPanelComponent implements OnInit, OnChanges, OnDestroy {
  @Input() documentType!: DocumentTypeConfig;
  private destroy$ = new Subject<void>();

  ngOnInit(): void { this.loadExistingDocuments(); }
  ngOnChanges(): void { this.resetPanelState(); }      // case/type input changed
  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }
}
```

---

## Q2. How do components communicate in your app?

**Answer.** Three mechanisms depending on the relationship:
- **Parent → child:** `@Input()`.
- **Child → parent:** `@Output() EventEmitter`.
- **Siblings / across the tree:** a shared service holding a `BehaviorSubject`.

**In EasyVisa.** The document portal was a parent "case documents" component containing many accordion **upload-panel** children. The parent passed each panel its config via `@Input`, and each panel emitted back up when a file finished uploading so the parent could update the case's overall completeness indicator:

```typescript
// upload-panel.component.ts
@Input() documentType!: DocumentTypeConfig;
@Input() caseId!: string;
@Output() documentUploaded = new EventEmitter<UploadedDocument>();

onUploadComplete(doc: UploadedDocument): void { this.documentUploaded.emit(doc); }
```

```html
<app-upload-panel *ngFor="let type of documentTypes; trackBy: trackByType"
                  [documentType]="type" [caseId]="caseId"
                  (documentUploaded)="onDocumentUploaded($event)">
</app-upload-panel>
```

For state that many unrelated components needed — the currently selected case, the logged-in attorney — I used a **shared service + `BehaviorSubject`** rather than threading inputs through every level.

---

## Q3. Observable vs Subject vs BehaviorSubject — explain the difference and where you used each.

**Answer.**
- **`Observable`** — a lazy stream you subscribe to; it produces values. It's *unicast* by default (each subscriber gets its own execution). This is what `HttpClient` and `valueChanges` return.
- **`Subject`** — both an Observable *and* an Observer. It's **multicast** — I can `.next()` values into it and every subscriber gets them. But a late subscriber misses values emitted before it subscribed.
- **`BehaviorSubject`** — a Subject that **remembers the last value** and replays it immediately to any new subscriber. It requires an initial value. Perfect for "current state."

**In EasyVisa.**
- **Observable** everywhere I called the Grails APIs (`this.http.get<Case>(...)`) and on reactive form `valueChanges`.
- **Subject** for my `destroy$` unsubscribe signal and for "trigger this action" streams (e.g. an upload-request subject feeding a debounced pipe).
- **BehaviorSubject** for shared current state — the selected case and the current attorney/session — exposed read-only via `asObservable()` so consumers could read the latest value but only the service could push:

```typescript
@Injectable({ providedIn: 'root' })
export class CaseContextService {
  private selectedCase$ = new BehaviorSubject<VisaCase | null>(null);
  readonly selectedCase = this.selectedCase$.asObservable();      // read-only out
  setSelectedCase(c: VisaCase): void { this.selectedCase$.next(c); }
}
```

**Follow-up: "Why BehaviorSubject over Subject there?"** Because a component that loads *after* the case was selected still needs the current case immediately — BehaviorSubject replays it; a plain Subject would leave that component with nothing until the next change.

---

## Q4. How do you prevent memory leaks in Angular? *(the #1 question)*

**Answer.** Leaks in Angular almost always come from **RxJS subscriptions that outlive the component**. If I `subscribe()` and never clean up, the stream keeps a reference to the component so it's never garbage-collected, and its callback keeps firing after the view is gone. Three tools, in order of preference:
1. **`async` pipe** in the template — Angular subscribes and unsubscribes for me.
2. **`takeUntil(destroy$)`** for imperative subscriptions — a `Subject` I complete in `ngOnDestroy`.
3. Manual `Subscription.unsubscribe()` only for one-offs.

**In EasyVisa.** `takeUntil(destroy$)` was my standard in every component, and it mattered most in the upload panels because an upload progress stream is long-lived — if the attorney navigated away mid-upload, I did **not** want a dead component still receiving progress events:

```typescript
this.uploadService.upload(file, this.caseId)
  .pipe(takeUntil(this.destroy$))
  .subscribe(event => this.handleProgress(event));

ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }
```

In templates I leaned on the **async pipe** so those subscriptions never needed manual cleanup:

```html
<div *ngIf="selectedCase$ | async as case">{{ case.applicantName }}</div>
```

**Follow-up: "Anything newer?"** In modern Angular you can drop the boilerplate subject with `takeUntilDestroyed()` from `@angular/core/rxjs-interop` — that's the direction I'd move the codebase.

---

## Q5. How do you restrict multiple / duplicate API calls? (switchMap, debounce)

**Answer.** Two problems, two tools:
- **Too many calls from rapid input** (typing in a search/validation field) → **`debounceTime`** to wait for a pause before firing.
- **Stale responses arriving out of order / redundant in-flight calls** → **`switchMap`**, which cancels the previous inner request when a new value arrives, so **only the latest wins**.

Together they're the canonical "search-as-you-type" pattern, and they also stop double-submits.

**In EasyVisa.** Some visa form fields validated against the backend (e.g. checking an identifier/reference number). Firing on every keystroke would hammer the Grails API and could show a stale result. So:

```typescript
this.referenceControl.valueChanges.pipe(
  debounceTime(400),              // wait for the user to stop typing
  distinctUntilChanged(),         // ignore no-op changes
  switchMap(value =>              // cancel the previous validation call
    this.caseService.validateReference(value).pipe(
      catchError(() => of({ valid: false }))
    )
  ),
  takeUntil(this.destroy$)
).subscribe(result => this.referenceStatus = result);
```

I also used `switchMap` when the selected case changed — switching the "load this case's documents" call so an old, slow case-load couldn't overwrite the newly selected case's data.

**Follow-up: `switchMap` vs `mergeMap` vs `concatMap`?** `switchMap` cancels the previous (latest wins — search, validation, selection). `mergeMap` runs all in parallel (independent uploads). `concatMap` queues them in order (when order matters). For **document uploads** I actually wanted a throttled-parallel behavior, not switchMap — I never want a second file to *cancel* the first.

---

## Q6. Walk me through the large document-upload code.

**Answer.** Attorneys upload many documents per case. Uploading naively blocks the UI and risks timeouts, and I need a **per-file progress bar** and isolated error handling. The key is `HttpClient` with `reportProgress: true` and `observe: 'events'`, then reading the event stream:

```typescript
uploadDocument(file: File, caseId: string): Observable<UploadState> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('caseId', caseId);

  return this.http.post('/api/documents', formData, {
    reportProgress: true,
    observe: 'events'
  }).pipe(
    map((event: HttpEvent<any>) => {
      switch (event.type) {
        case HttpEventType.UploadProgress:
          return { status: 'uploading',
                   progress: Math.round(100 * event.loaded / (event.total ?? 1)) };
        case HttpEventType.Response:
          return { status: 'done', document: event.body };
        default:
          return { status: 'pending', progress: 0 };
      }
    }),
    catchError(err => of({ status: 'error', message: this.toUserMessage(err) }))
  );
}
```

**In EasyVisa.** Each accordion panel (grouped by document type) subscribed to this with `takeUntil(destroy$)` and rendered its own progress bar. Crucially, **each panel had its own progress/error state**, so if one file failed, that panel showed the error while the others kept going — one bad upload didn't sink the case. Uploads were concurrent but throttled so I didn't fire 20 requests at once.

**Follow-up: "What would you improve?"** For very large files the right move is **chunked/resumable upload** — split the file, upload chunks, resume on failure. I flagged that as the next iteration; we shipped the progress-stream version first because it covered the real file sizes attorneys used.

---

## Q7. You customized an image cropper — why and how?

**Answer.** Certain visa documents — applicant photos in particular — must meet strict dimension, aspect-ratio, and format requirements. I didn't want attorneys pre-editing images in another tool, so I wrapped and customized an **Angular image-cropper** library to enforce those rules in-app.

**In EasyVisa.** I configured the cropper to lock the aspect ratio and output size, then **post-processed the output blob** before it entered the upload flow — so what got cropped was exactly what got uploaded, already compliant:

```typescript
// wired the cropper's output into the same upload pipeline as Q6
onImageCropped(event: ImageCroppedEvent): void {
  const croppedBlob = event.blob!;                          // enforced aspect + size
  const file = new File([croppedBlob], 'applicant-photo.png', { type: 'image/png' });
  this.uploadService.uploadDocument(file, this.caseId)
    .pipe(takeUntil(this.destroy$))
    .subscribe(state => this.handleUpload(state));
}
```

The customization was mostly (1) constraining the crop box to the required aspect ratio, (2) forcing the output dimensions/format, and (3) feeding the resulting blob straight into the document upload. Wrapping it in my own component also made it a **reusable** control other parts of the app could drop in.

---

## Q8. You built reusable components — give an example and the benefit.

**Answer.** I extracted anything used in more than one place into shared, configuration-driven components. The benefit is consistency and speed — a new use case becomes a config change, and a bug gets fixed once.

**In EasyVisa.** My reusable pieces were:
- The **upload-panel** component (config: document type, accept rules, required flag).
- Custom **Formly field types** (see `02-forms-deep-dive.md`) — the biggest reuse win, because a whole new form section became JSON, not new code.
- The **image-cropper** wrapper.
- Common form controls and layout wrappers.

```typescript
// a shared, configurable upload panel — reused per document type across the app
@Component({ selector: 'app-upload-panel', templateUrl: './upload-panel.component.html' })
export class UploadPanelComponent {
  @Input() documentType!: DocumentTypeConfig;   // label, accept, required, multiple
  @Input() caseId!: string;
  @Output() documentUploaded = new EventEmitter<UploadedDocument>();
}
```

The payoff: adding a new document type to the portal meant adding a config entry, not writing a new component — and every panel behaved identically (progress, errors, validation).

---

## Q9. How does change detection work, and did you use OnPush?

**Answer.** Angular's default strategy dirty-checks the component tree whenever something *might* have changed — triggered by zone.js patching async APIs (events, timers, XHR). `ChangeDetectionStrategy.OnPush` narrows that: the component is only checked when an `@Input` **reference** changes, an event fires inside it, or an `async`-piped observable it uses emits.

**In EasyVisa (honest).** We ran mostly the **default** strategy. The document portal, with many panels and live upload progress, is exactly the kind of tree that benefits from **OnPush + immutable updates + the async pipe** — that's a concrete perf improvement I can speak to. Where I needed to force a refresh after something Angular didn't see (e.g. a callback from the cropper library), I injected `ChangeDetectorRef` and called `detectChanges()`. Being honest that we were default-strategy, and knowing exactly which components I'd move to OnPush and why, reads better than claiming we were fully optimized.

---

## Q10. How did you handle state, APIs, and errors overall?

**Answer.** Feature services wrapping the Grails REST endpoints, Observables consumed via the **async pipe**, **reactive forms** as the source of truth for form state, and **BehaviorSubject** services for shared state. Errors were handled per-call with `catchError` mapping to user-friendly messages, plus a shared **HTTP interceptor** for auth headers and global failures.

**In EasyVisa.** For the forms specifically, I kept the **Formly model as the single state object**, which made **draft save/restore** trivial — I could serialize the whole nested model, store it, and rehydrate the form later. That's a nice architectural point: the form's data and its state were the same object.

```typescript
// error handling per call, consistent user messaging
getCase(id: string): Observable<VisaCase> {
  return this.http.get<VisaCase>(`/api/cases/${id}`).pipe(
    catchError(err => { this.notifications.show(this.toUserMessage(err)); return EMPTY; })
  );
}
```

(Global exception handling / interceptor detail is covered in `05-behavioral-and-stars.md` and the forms/backend files.)

---

## Q11. Route guards, lazy loading, resolvers — did you use them?

**Answer.**
- **Guards (`CanActivate`)** decide whether a route can be entered — I used them so only authenticated attorneys reached case routes.
- **Lazy loading** — feature modules loaded on demand (`loadChildren`) so the initial bundle stayed small.
- **Resolvers** pre-fetch data *before* a route activates, so the component never renders half-loaded.

**In EasyVisa.** Case detail and the document portal were **lazy-loaded feature modules**. A **resolver** pre-loaded the case (and its document metadata) before the portal opened, so the accordion rendered fully populated rather than flashing empty and then filling in:

```typescript
{
  path: 'cases/:caseId',
  loadChildren: () => import('./case/case.module').then(m => m.CaseModule),
  canActivate: [AuthGuard],
  resolve: { case: CaseResolver }
}
```

**Trade-off I'd mention:** resolvers delay navigation until the data arrives, so I only resolved the *critical* above-the-fold case data and lazy-loaded the rest inside the component.

---

## Q12. The Angular version upgrade — how did you approach it?

**Answer.** Incrementally and safely — never jump multiple majors at once. I used the official `ng update` path, going one major at a time, building and running the test suite at each step, and fixing breaking changes and deprecations as they surfaced. The Angular Update Guide told me exactly what changed per version.

**In EasyVisa.** I drove the upgrade: bumped one major, resolved the compilation/deprecation issues (RxJS operator imports, typing changes, deprecated APIs), got the Jasmine/Karma suite green, verified the document portal and Formly forms manually, then moved to the next major. Having **unit tests on the components and form logic** is exactly what made this safe — the tests caught regressions the compiler didn't.

---

## Q13. How did you test the frontend?

**Answer.** **Jasmine + Karma** unit tests for components and services. I tested component logic, form/validation behavior, and service methods (mocking `HttpClient` with `HttpTestingController`). I aimed to cover the branching logic — especially the conditional form rules, where regressions hide.

**In EasyVisa.** I wrote unit tests for all my UI components and services, which is what made the version upgrade and ongoing refactors safe. The highest-value tests were around the **Formly conditional logic** — asserting that a field hid/showed and that `required` toggled correctly as the model changed — because that logic is easy to break silently. I also documented specs and user manuals so other devs could adopt the reusable pieces.

---

### Rapid-fire recap

| Topic | EasyVisa pattern |
|-------|------------------|
| Lifecycle hooks | `ngOnInit` load, `ngOnChanges` reset panel on input change, `ngAfterViewInit` for cropper, `ngOnDestroy` complete `destroy$` |
| Component comms | `@Input`/`@Output` (portal ↔ panels), BehaviorSubject service (selected case) |
| Observable/Subject/BehaviorSubject | HTTP/`valueChanges` / `destroy$` + triggers / current case + session |
| Memory leaks | `takeUntil(destroy$)` + async pipe |
| Restrict API calls | `debounceTime` + `switchMap` (latest wins) |
| Uploads | `HttpClient` `reportProgress` + `observe:'events'`, per-panel state |
| Image cropper | customized lib, enforced aspect/size, blob → upload pipeline |
| Reusable components | upload-panel, custom Formly types, cropper wrapper |
| Change detection | default (know OnPush plan), `detectChanges()` after lib callbacks |
| Guards/lazy/resolver | `CanActivate` on case routes, lazy feature modules, `CaseResolver` |
| Upgrade | incremental `ng update`, tests green each step |
| Testing | Jasmine/Karma, focus on conditional form logic |
