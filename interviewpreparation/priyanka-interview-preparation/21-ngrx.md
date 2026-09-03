# NgRx — Interview Questions & Complete Guide

State management for Angular, the Redux way. Companion to [20 — RxJS & Operators](./20-rxjs-operators.md) — NgRx is built entirely on RxJS, so read that first if the operators aren't solid.

---

## ⚠️ Read this before anything else — your honesty position

**NgRx is on your resume. RoboGebra does not use it.**

I checked every `package.json` in `robogebra-workspace/` — there is **no `@ngrx/*` package** in the web, mobile or backoffice apps. RoboGebra state is services holding `BehaviorSubject`s ([18 — Technical Versions](./18-robogebra-technical-versions.md)).

So when NgRx comes up, **attribute it correctly**:

> ✅ *"I used NgRx on **EasyVisa**, where the visa application had deeply nested forms and shared state across many modules. On RoboGebra we deliberately went simpler — services with `BehaviorSubject` and the `async` pipe — because the state is mostly per-feature and NgRx would have been ceremony without payoff."*

That answer is **stronger** than claiming NgRx everywhere. It shows you can choose a tool rather than apply one reflexively — and interviewers actively probe for that. What you must not do is describe RoboGebra as an NgRx app; a follow-up question about your store structure will expose it.

---

## Table of contents

- [⚡ 60-second cheat sheet](#-60-second-cheat-sheet)
- [Part 1 — Why NgRx exists](#part-1--why-ngrx-exists)
- [Part 2 — The data flow](#part-2--the-data-flow)
- [Part 3 — Actions](#part-3--actions)
- [Part 4 — Reducers](#part-4--reducers)
- [Part 5 — Selectors](#part-5--selectors)
- [Part 6 — Effects](#part-6--effects)
- [Part 7 — Wiring the store up](#part-7--wiring-the-store-up)
- [Part 8 — @ngrx/entity](#part-8--ngrxentity)
- [Part 9 — The Facade pattern](#part-9--the-facade-pattern)
- [Part 10 — Full worked example](#part-10--full-worked-example-easyvisa-document-portal)
- [Part 11 — ComponentStore & SignalStore](#part-11--componentstore--signalstore)
- [Part 12 — Router Store & DevTools](#part-12--router-store--devtools)
- [Part 13 — When NOT to use NgRx](#part-13--when-not-to-use-ngrx)
- [Part 14 — Common mistakes](#part-14--common-mistakes)
- [Part 15 — Interview Q&A](#part-15--interview-qa)
- [Part 16 — Rapid-fire](#part-16--rapid-fire)

---

## ⚡ 60-second cheat sheet

| Question | Answer |
|---|---|
| **What is NgRx?** | Redux-inspired reactive state management for Angular, built on RxJS |
| **The core building blocks?** | **Store, Actions, Reducers, Selectors, Effects** |
| **The three principles?** | Single source of truth · state is read-only · changes via pure reducer functions |
| **What is an Action?** | A plain object `{ type, ...payload }` describing that **something happened** |
| **What is a Reducer?** | A **pure function** `(state, action) => newState` |
| **What is a Selector?** | A **memoized** query function for reading a slice of state |
| **What is an Effect?** | A side-effect handler — listens for actions, does async work, dispatches new actions |
| **Where does HTTP go?** | **Effects.** Never in a reducer |
| **Which RxJS operator in effects?** | `concatMap` for writes, `switchMap` for reads/search, `exhaustMap` for submits, `mergeMap` for independent work |
| **Why do selectors matter?** | Memoization — they don't recompute unless their inputs change |
| **`@ngrx/entity`?** | Normalized CRUD collections — `{ ids: [], entities: {} }` + a generated adapter |
| **When NOT to use it?** | Small apps, mostly-local state, small teams — the boilerplate isn't free |
| **NgRx version?** | Tracks Angular majors — Angular 16 → NgRx 16 |

---

## 🧠 The easiest way to remember ALL of NgRx

One analogy carries the entire library — **a bank**.

```
You NEVER edit the balance directly.
You submit a SLIP saying what happened.
The bank applies a RULE and writes a NEW ledger entry.
The balance is DERIVED from the ledger.
```

```
Component  → the customer          "I want to withdraw ₹500"
ACTION     → the deposit/withdrawal SLIP ⭐   (what HAPPENED — never a command)
REDUCER    → the bank's RULE      (pure: old balance + slip → NEW balance) ⭐
STORE      → the LEDGER            (one source of truth, never overwritten)
SELECTOR   → the BALANCE ENQUIRY   (derived, and CACHED until something changes) ⭐
EFFECT     → the BACK OFFICE       (calls the other bank, then files another slip) ⭐
```

⭐ Three consequences fall straight out of the analogy, and they're the three most-asked questions:

```
1. Why must a reducer be PURE?
      A bank rule that phoned someone would be unauditable ⭐
2. Why NEVER mutate state?
      You don't erase a ledger line — you add a new one ⭐
3. Why do effects exist?
      Anything that talks to the outside world happens in the BACK OFFICE,
      never inside the rule ⭐
```

#### Easy memory

```
NgRx = a BANK LEDGER 🏦
Action = the SLIP (what happened) ⭐ | Reducer = the RULE (pure) ⭐
Store = the LEDGER | Selector = the BALANCE ENQUIRY (memoised) ⭐
Effect = the BACK OFFICE (async, then files another slip) ⭐

Never edit the ledger. Add a new entry. ⭐
```

---

# Part 1 — Why NgRx exists

### Q: What problem does NgRx solve?

In a mid-size Angular app, shared state gets messy:

- The same data is fetched by several components, each with its own copy
- Component A changes something, component B doesn't know
- `@Input`/`@Output` chains get passed through five layers ("prop drilling")
- Services with `BehaviorSubject`s multiply until nobody knows who mutates what
- Reproducing a bug is guesswork because state changes are untraceable

**NgRx centralizes application state in one immutable store** and makes every change go through an explicit, logged, replayable action.

### Q: What are the three Redux principles?

1. **Single source of truth** — the whole app's state lives in one object tree.
2. **State is read-only** — you never mutate it; you dispatch an action describing what happened.
3. **Changes are made by pure functions** — reducers take the previous state and an action and return **new** state.

### Q: What do you actually gain?

| Benefit | Why |
|---|---|
| **Predictability** | Same state + same action → same result, always |
| **Traceability** | Every change is a named action visible in DevTools |
| **Time-travel debugging** | Replay actions, jump to any previous state |
| **Testability** | Reducers and selectors are pure functions — trivial unit tests, no mocks |
| **Performance** | Immutable state + memoized selectors → `OnPush` change detection works properly |
| **Decoupling** | Components dispatch and select; they don't know where data comes from |

---

# Part 2 — The data flow

**Strictly unidirectional.** Be able to draw this on a whiteboard.

```
      ┌────────────────┐
      │   COMPONENT    │
      │                │
      │  dispatch() ───┼──────────► ACTION ──────────┐
      │                │        { type: '[Docs]      │
      │  select()  ◄───┼───┐       Load' }           │
      └────────────────┘   │                         ▼
                           │                 ┌───────────────┐
                           │                 │    REDUCER    │  pure fn
                           │                 │ (state, act)  │  no side effects
                           │                 │  => newState  │
                           │                 └───────┬───────┘
                           │                         │
                           │                         ▼
                           │                 ┌───────────────┐
                           └─────────────────┤     STORE     │  single immutable
                              SELECTOR       │  (app state)  │  state tree
                              (memoized)     └───────────────┘
                                                     ▲
                                                     │ dispatches result action
                           ┌─────────────────────────┴──────────┐
      ACTION ─────────────►│              EFFECT                 │
                           │  listens for actions, does async    │
                           │  work (HTTP), dispatches success/   │
                           │  failure actions                    │───► HTTP / API
                           └─────────────────────────────────────┘
```

**Say it out loud like this:**
> *"A component dispatches an action. The reducer handles it synchronously and produces new state. If the action needs async work, an effect picks it up, calls the service, and dispatches a success or failure action — which the reducer then handles. Components read state through memoized selectors. Data flows one way, so any state change is traceable to a single named action."*

#### Why "unidirectional" actually matters ⭐

```
TWO-WAY / ad-hoc state:
   Component A sets user.name
   Component B sets user.name
   A service sets user.name
        ↓
   The name is wrong. WHO changed it? 🤷 → you add console.logs everywhere 💥

UNIDIRECTIONAL:
   EVERY change is a NAMED ACTION in DevTools, in order, with the state
   before and after ⭐
        ↓
   "The name changed on [Profile] Update Success at 14:03." ✅
```

⭐ That traceability — not the code structure — is the actual reason NgRx exists. Say *that* when asked "why NgRx?".

#### Easy memory

```
dispatch(ACTION) → REDUCER (pure, sync) → STORE → SELECTOR (memoised) → component
                      ↑                                    async work
                      └──── EFFECT dispatches success/failure ⭐

ONE direction ⭐ → every change is a NAMED, replayable event → real traceability
```

---

# Part 3 — Actions

An action is **a plain object with a `type`** describing that something happened.

```ts
import { createAction, props } from '@ngrx/store';

export const loadDocuments = createAction(
  '[Document Portal] Load Documents'
);

export const loadDocumentsSuccess = createAction(
  '[Document API] Load Documents Success',
  props<{ documents: Document[] }>()
);

export const loadDocumentsFailure = createAction(
  '[Document API] Load Documents Failure',
  props<{ error: string }>()
);

export const uploadDocument = createAction(
  '[Document Portal] Upload Document',
  props<{ file: File; panelId: string }>()
);
```

### The naming convention — `[Source] Event`

| Part | Meaning |
|---|---|
| `[Source]` | **Where it came from** — a page, a component, an API |
| `Event` | **What happened**, in past tense |

```
✅ '[Login Page] Login Submitted'
✅ '[Auth API] Login Success'
✅ '[Document Portal] Document Deleted'

❌ 'LOGIN'                        — no source, no event
❌ '[Auth] Set User'              — a command, not an event
❌ '[Docs] Load'  (reused everywhere)  — you lose traceability
```

### 🎯 The senior insight: actions are events, not commands

> *"Actions should describe **what happened**, not **what to do**. `'[Login Page] Login Submitted'` rather than `'Set Loading True'`. Unique, source-prefixed actions mean the DevTools log reads like a story of what the user did, and multiple reducers or effects can react to the same event independently."*

This is the **Observer pattern** — one event, many independent listeners ([17 — SOLID & Design Patterns](./17-solid-design-patterns.md)).

### `createActionGroup` (NgRx 15+) — less boilerplate

```ts
export const DocumentActions = createActionGroup({
  source: 'Document Portal',
  events: {
    'Load Documents': emptyProps(),
    'Load Documents Success': props<{ documents: Document[] }>(),
    'Load Documents Failure': props<{ error: string }>(),
  },
});

// Usage: DocumentActions.loadDocuments()
```

---

# Part 4 — Reducers

A reducer is a **pure function** that takes the current state and an action and returns **new** state.

```ts
import { createReducer, on } from '@ngrx/store';

export interface DocumentState {
  documents: Document[];
  loading: boolean;
  error: string | null;
  selectedId: string | null;
}

export const initialState: DocumentState = {
  documents: [],
  loading: false,
  error: null,
  selectedId: null,
};

export const documentReducer = createReducer(
  initialState,

  on(loadDocuments, state => ({
    ...state,
    loading: true,
    error: null,
  })),

  on(loadDocumentsSuccess, (state, { documents }) => ({
    ...state,
    documents,
    loading: false,
  })),

  on(loadDocumentsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  on(documentDeleted, (state, { id }) => ({
    ...state,
    documents: state.documents.filter(d => d.id !== id),   // new array, not splice
  })),
);
```

### The rules a reducer must obey

| Rule | Why |
|---|---|
| **Pure** — same inputs, same output | Predictability, time-travel |
| **No mutation** — always return a new object | Change detection + DevTools rely on reference changes |
| **No side effects** — no HTTP, no `console.log`, no `Date.now()`, no `Math.random()` | Reproducibility |
| **Handle unknown actions** by returning state unchanged | `createReducer` does this for you |

### ❌ The mutation bug (asked constantly)

```ts
// WRONG — mutates existing state
on(addDocument, (state, { doc }) => {
  state.documents.push(doc);          // ❌ same array reference
  return state;                        // ❌ same state reference
});
// Result: OnPush components never update, DevTools shows nothing, tests pass misleadingly

// RIGHT
on(addDocument, (state, { doc }) => ({
  ...state,
  documents: [...state.documents, doc],
}));
```

> 💬 *"Reducers must return a new reference. Angular's `OnPush` change detection and NgRx's memoized selectors both compare by reference — mutate in place and the UI simply doesn't update, which is a maddening bug to trace."*

**Immutable update cheat sheet:**
```ts
[...arr, item]                                  // add
arr.filter(x => x.id !== id)                    // remove
arr.map(x => x.id === id ? { ...x, ...changes } : x)   // update
{ ...obj, key: value }                          // object property
{ ...state, nested: { ...state.nested, k: v } } // nested
```

### `createFeature` (NgRx 13+) — reducer + selectors together

```ts
export const documentFeature = createFeature({
  name: 'documents',
  reducer: createReducer(initialState, /* ...on() handlers */),
});

// Auto-generates: selectDocumentsState, selectDocuments, selectLoading, selectError...
export const { selectDocuments, selectLoading, selectError } = documentFeature;
```

---

# Part 5 — Selectors

A selector is a **memoized** function for reading a slice of state.

```ts
import { createFeatureSelector, createSelector } from '@ngrx/store';

// 1. Grab the feature slice
export const selectDocumentState =
  createFeatureSelector<DocumentState>('documents');

// 2. Simple derived selectors
export const selectAllDocuments = createSelector(
  selectDocumentState,
  state => state.documents
);

export const selectLoading = createSelector(
  selectDocumentState,
  state => state.loading
);

// 3. Compose selectors
export const selectApprovedDocuments = createSelector(
  selectAllDocuments,
  docs => docs.filter(d => d.status === 'APPROVED')
);

// 4. Combine multiple selectors
export const selectDocumentSummary = createSelector(
  selectAllDocuments,
  selectApprovedDocuments,
  (all, approved) => ({
    total: all.length,
    approved: approved.length,
    pending: all.length - approved.length,
  })
);

// 5. Selector with a runtime argument (props)
export const selectDocumentsByPanel = (panelId: string) => createSelector(
  selectAllDocuments,
  docs => docs.filter(d => d.panelId === panelId)
);
```

### 🔑 Memoization — the whole point of selectors

`createSelector` caches its last inputs and last result. If the inputs are **reference-equal** to last time, it returns the cached result **without re-running the projector**.

```
selectAllDocuments returns the SAME array reference
        ↓
selectApprovedDocuments: inputs unchanged → skips the filter, returns cached array
        ↓
component gets the SAME reference → OnPush skips re-render
```

That's why an unrelated state change (say `loading` flipping) doesn't cause an expensive `filter` or `sort` to re-run across your whole component tree.

> 💬 **The answer:** *"Selectors are memoized — `createSelector` caches on input reference equality, so an expensive derivation only recomputes when its actual inputs change. Combined with `OnPush`, that's what keeps an NgRx app fast. It also means reducers must never mutate — mutation defeats the reference check and memoization silently returns stale data."*

#### Real-world idea

```
A memoised selector = a CACHED bill total 🧾

Nothing in the basket changed → don't add it all up again;
hand back the same total, with the SAME reference.
      ↓
Same reference → an OnPush component doesn't re-render ⭐
```

⚠️ And the trap that follows immediately:

```
MUTATE the array in a reducer → the reference is UNCHANGED
      ↓
memoisation thinks nothing changed → returns the STALE cached result
      ↓
the UI silently doesn't update 💥
```

⭐ That is the deep reason "never mutate" is a rule, and it's a much better answer than "because Redux says so."

#### Easy memory

```
createSelector CACHES on INPUT REFERENCE equality ⭐ (a cached bill total 🧾)
Unchanged inputs → the projector is SKIPPED → the same reference back
      → OnPush skips the re-render ⭐

⚠️ Mutation keeps the same reference → memoisation returns STALE data 💥
   → THAT is why reducers must be immutable ⭐
```

### Using selectors in a component

```ts
export class DocumentListComponent {
  documents$ = this.store.select(selectAllDocuments);
  loading$   = this.store.select(selectLoading);
  summary$   = this.store.select(selectDocumentSummary);

  constructor(private store: Store) {}

  ngOnInit() {
    this.store.dispatch(loadDocuments());
  }

  onDelete(id: string) {
    this.store.dispatch(documentDeleted({ id }));
  }
}
```

```html
<app-spinner *ngIf="loading$ | async"></app-spinner>

<div *ngFor="let doc of documents$ | async">
  {{ doc.name }}
  <button (click)="onDelete(doc.id)">Delete</button>
</div>
```

> ⚠️ Use `store.select(...)` + the `async` pipe. **Never** `store.select(x).subscribe(v => this.x = v)` without unsubscribing — the store is an infinite stream.

---

# Part 6 — Effects

**Effects handle everything impure**: HTTP, routing, localStorage, timers, toasts.

#### The easiest way to remember

```
An EFFECT listens for an action, does the messy real-world work,
and then dispatches ANOTHER action with the result. ⭐

Action IN → side effect → Action OUT
```

```
[Docs] Load                    (dispatched by the component)
      ↓ the effect hears it
   HTTP GET /documents         ← the impure part lives HERE, never in a reducer ⭐
      ↓
[Docs] Load Success  or  [Docs] Load Failure
      ↓
   the reducer handles it → the store updates → the UI reacts ✅
```

Real-world idea: the **back office of the bank.** The counter rule (reducer) is instant and never phones anyone. Anything involving another institution goes to the back office, which comes back with its own slip.

⭐ The two operator choices that get asked here:

```
LOAD  (a read)  → switchMap  ⭐ cancel the stale request
SAVE  (a write) → concatMap  ⭐ NEVER cancel a write
```

→ the same rule as [20 — RxJS](./20-rxjs-operators.md).

```ts
import { createEffect, Actions, ofType } from '@ngrx/effects';

@Injectable()
export class DocumentEffects {

  loadDocuments$ = createEffect(() =>
    this.actions$.pipe(
      ofType(loadDocuments),                              // 1. listen for this action
      switchMap(() =>                                     // 2. do the async work
        this.documentService.getAll().pipe(
          map(documents => loadDocumentsSuccess({ documents })),   // 3. success action
          catchError(error => of(loadDocumentsFailure({           // 4. failure action
            error: error.message
          })))
        )
      )
    )
  );

  constructor(
    private actions$: Actions,
    private documentService: DocumentService
  ) {}
}
```

### 🔥 The two rules that get asked

**Rule 1 — `catchError` goes INSIDE the inner observable.**

```ts
// ❌ WRONG — the effect DIES on the first error and never runs again
this.actions$.pipe(
  ofType(loadDocuments),
  switchMap(() => this.service.getAll()),
  map(docs => loadDocumentsSuccess({ documents: docs })),
  catchError(err => of(loadDocumentsFailure({ error: err })))   // outer = terminal
);

// ✅ RIGHT — the inner stream absorbs the error, the effect keeps listening
this.actions$.pipe(
  ofType(loadDocuments),
  switchMap(() => this.service.getAll().pipe(
    map(docs => loadDocumentsSuccess({ documents: docs })),
    catchError(err => of(loadDocumentsFailure({ error: err })))
  ))
);
```

Same principle as [20 — RxJS, Part 7](./20-rxjs-operators.md#part-7--error-handling): an error terminates a stream. `actions$` is infinite — kill it and that feature stops responding for the rest of the session, with no error in the console.

**Rule 2 — pick the right flattening operator.**

| Operator | Use for | Example |
|---|---|---|
| **`switchMap`** | Reads/search — cancel the previous | Load list, typeahead |
| **`concatMap`** | Writes — preserve order, never cancel | Save, update, delete |
| **`exhaustMap`** | Submissions — ignore duplicates | Login, submit form |
| **`mergeMap`** | Independent parallel work | Bulk file uploads |

> ⚠️ **Never `switchMap` a save effect.** If two saves fire quickly, the first request gets cancelled — potentially after the server already started processing it. `concatMap` is the safe default for anything that writes.

### Non-dispatching effects

Some effects only cause a side effect and dispatch nothing:

```ts
navigateAfterSave$ = createEffect(() =>
  this.actions$.pipe(
    ofType(saveDocumentSuccess),
    tap(() => this.router.navigate(['/documents']))
  ),
  { dispatch: false }              // ← required, or NgRx tries to dispatch `undefined`
);

showErrorToast$ = createEffect(() =>
  this.actions$.pipe(
    ofType(loadDocumentsFailure, saveDocumentFailure),   // ofType takes multiple actions
    tap(({ error }) => this.toastr.error(error))
  ),
  { dispatch: false }
);
```

### Reading state inside an effect — `concatLatestFrom`

```ts
import { concatLatestFrom } from '@ngrx/operators';

saveDocument$ = createEffect(() =>
  this.actions$.pipe(
    ofType(saveDocument),
    concatLatestFrom(() => this.store.select(selectCurrentUser)),   // lazily reads state
    concatMap(([action, user]) =>
      this.service.save(action.document, user.id).pipe(
        map(saved => saveDocumentSuccess({ document: saved })),
        catchError(e => of(saveDocumentFailure({ error: e.message })))
      )
    )
  )
);
```
> 💡 `concatLatestFrom` is preferred over `withLatestFrom` because it evaluates the selector **lazily** — only when the action actually arrives, not on every subscription.

### Effect for initial load

```ts
init$ = createEffect(() =>
  this.actions$.pipe(
    ofType(ROOT_EFFECTS_INIT),          // fires once when effects are initialized
    map(() => loadAppConfig())
  )
);
```

---

# Part 7 — Wiring the store up

### NgModule style (what your Angular 16 apps would use)

```ts
// app.module.ts — root
@NgModule({
  imports: [
    StoreModule.forRoot({}, {
      runtimeChecks: {
        strictStateImmutability: true,      // throws if you mutate state
        strictActionImmutability: true,
        strictStateSerializability: true,
        strictActionSerializability: true,
      }
    }),
    EffectsModule.forRoot([]),
    StoreDevtoolsModule.instrument({
      maxAge: 25,
      logOnly: environment.production,
    }),
  ],
})
export class AppModule {}
```

```ts
// documents.module.ts — lazy-loaded feature
@NgModule({
  imports: [
    StoreModule.forFeature('documents', documentReducer),
    EffectsModule.forFeature([DocumentEffects]),
  ],
})
export class DocumentsModule {}
```

> 🎯 **`forRoot` vs `forFeature`:** `forRoot` sets up the store once in the root injector. `forFeature` registers a lazily-loaded slice under a key — the reducer is added to the store dynamically when the feature module loads. Same distinction as `RouterModule.forRoot`/`forFeature`.

> 💡 **`runtimeChecks`** are a great detail to mention — they catch accidental mutation at development time instead of letting it become a silent UI bug.

### Standalone style (NgRx 15+, Angular 15+)

```ts
bootstrapApplication(AppComponent, {
  providers: [
    provideStore(),
    provideEffects([]),
    provideStoreDevtools({ maxAge: 25 }),
  ],
});

// route-level feature state
export const routes: Routes = [{
  path: 'documents',
  providers: [
    provideState(documentFeature),
    provideEffects(DocumentEffects),
  ],
  loadComponent: () => import('./documents.component'),
}];
```

---

# Part 8 — @ngrx/entity

For collections, `@ngrx/entity` gives you **normalized state plus generated CRUD helpers**.

### The shape it produces

```ts
{
  ids: ['d1', 'd2', 'd3'],                     // ordered
  entities: {                                   // O(1) lookup by id
    d1: { id: 'd1', name: 'Passport.pdf' },
    d2: { id: 'd2', name: 'I-140.pdf' },
    d3: { id: 'd3', name: 'Photo.jpg' },
  }
}
```

**Why normalize?** A plain array means `O(n)` lookup and awkward updates (`map` with a ternary on every change). A keyed dictionary gives `O(1)` lookup, no duplication, and much simpler updates.

### Setup

```ts
export interface DocumentState extends EntityState<Document> {
  loading: boolean;
  selectedId: string | null;
}

export const adapter: EntityAdapter<Document> = createEntityAdapter<Document>({
  selectId: doc => doc.id,
  sortComparer: (a, b) => a.name.localeCompare(b.name),
});

export const initialState: DocumentState = adapter.getInitialState({
  loading: false,
  selectedId: null,
});

export const documentReducer = createReducer(
  initialState,
  on(loadDocumentsSuccess, (state, { documents }) =>
    adapter.setAll(documents, { ...state, loading: false })),
  on(documentAdded, (state, { document }) =>
    adapter.addOne(document, state)),
  on(documentUpdated, (state, { update }) =>
    adapter.updateOne(update, state)),         // { id, changes: {...} }
  on(documentDeleted, (state, { id }) =>
    adapter.removeOne(id, state)),
);

// Adapter-generated selectors
const { selectAll, selectEntities, selectIds, selectTotal } = adapter.getSelectors();

export const selectDocumentState = createFeatureSelector<DocumentState>('documents');
export const selectAllDocuments = createSelector(selectDocumentState, selectAll);
export const selectDocumentTotal = createSelector(selectDocumentState, selectTotal);
```

**Adapter methods:** `addOne` · `addMany` · `setAll` · `setOne` · `updateOne` · `updateMany` · `upsertOne` · `upsertMany` · `removeOne` · `removeMany` · `removeAll` · `map`

---

# Part 9 — The Facade pattern

A facade service wraps the store so components never touch actions or selectors directly.

```ts
@Injectable({ providedIn: 'root' })
export class DocumentFacade {
  // Reads
  readonly documents$ = this.store.select(selectAllDocuments);
  readonly loading$   = this.store.select(selectLoading);
  readonly summary$   = this.store.select(selectDocumentSummary);

  constructor(private store: Store) {}

  // Writes
  load(): void { this.store.dispatch(loadDocuments()); }
  upload(file: File, panelId: string): void {
    this.store.dispatch(uploadDocument({ file, panelId }));
  }
  delete(id: string): void { this.store.dispatch(documentDeleted({ id })); }
}
```

```ts
// Component — clean, and doesn't know NgRx exists
export class DocumentListComponent {
  documents$ = this.facade.documents$;
  loading$   = this.facade.loading$;

  constructor(private facade: DocumentFacade) {}

  ngOnInit() { this.facade.load(); }
  onDelete(id: string) { this.facade.delete(id); }
}
```

| ✅ Pros | ❌ Cons |
|---|---|
| Components decoupled from NgRx | One more layer |
| Easy to swap state library later | Can hide the action flow, hurting traceability |
| Much simpler component tests (mock the facade) | Teams sometimes turn it into a dumping ground |

> 💬 *"This is the Facade pattern from GoF — one simplified interface over a subsystem ([17](./17-solid-design-patterns.md)). It's also what I'd use in a plain Angular app: a service exposing `BehaviorSubject`-backed observables is the same idea with less machinery — which is essentially what we do on RoboGebra."*

---

# Part 10 — Full worked example: EasyVisa document portal

The complete slice, end to end. This is your reference implementation.

### `document.actions.ts`
```ts
export const DocumentActions = createActionGroup({
  source: 'Document Portal',
  events: {
    'Enter':                   emptyProps(),
    'Load Documents':          emptyProps(),
    'Load Documents Success':  props<{ documents: Document[] }>(),
    'Load Documents Failure':  props<{ error: string }>(),
    'Upload Document':         props<{ file: File; panelId: string }>(),
    'Upload Document Success': props<{ document: Document }>(),
    'Upload Document Failure': props<{ error: string }>(),
    'Delete Document':         props<{ id: string }>(),
    'Delete Document Success': props<{ id: string }>(),
    'Panel Selected':          props<{ panelId: string }>(),
  },
});
```

### `document.reducer.ts`
```ts
export interface DocumentState extends EntityState<Document> {
  loading: boolean;
  uploading: boolean;
  error: string | null;
  selectedPanelId: string | null;
}

export const adapter = createEntityAdapter<Document>();

export const initialState: DocumentState = adapter.getInitialState({
  loading: false, uploading: false, error: null, selectedPanelId: null,
});

export const documentFeature = createFeature({
  name: 'documents',
  reducer: createReducer(
    initialState,
    on(DocumentActions.loadDocuments, s => ({ ...s, loading: true, error: null })),
    on(DocumentActions.loadDocumentsSuccess, (s, { documents }) =>
      adapter.setAll(documents, { ...s, loading: false })),
    on(DocumentActions.loadDocumentsFailure, (s, { error }) =>
      ({ ...s, loading: false, error })),

    on(DocumentActions.uploadDocument, s => ({ ...s, uploading: true })),
    on(DocumentActions.uploadDocumentSuccess, (s, { document }) =>
      adapter.addOne(document, { ...s, uploading: false })),
    on(DocumentActions.uploadDocumentFailure, (s, { error }) =>
      ({ ...s, uploading: false, error })),

    on(DocumentActions.deleteDocumentSuccess, (s, { id }) =>
      adapter.removeOne(id, s)),
    on(DocumentActions.panelSelected, (s, { panelId }) =>
      ({ ...s, selectedPanelId: panelId })),
  ),
});
```

### `document.selectors.ts`
```ts
const { selectAll } = adapter.getSelectors();

export const selectDocumentState = documentFeature.selectDocumentsState;
export const selectAllDocuments  = createSelector(selectDocumentState, selectAll);
export const selectLoading       = documentFeature.selectLoading;
export const selectSelectedPanel = documentFeature.selectSelectedPanelId;

export const selectDocumentsForSelectedPanel = createSelector(
  selectAllDocuments,
  selectSelectedPanel,
  (docs, panelId) => panelId ? docs.filter(d => d.panelId === panelId) : docs
);

export const selectPanelCompletion = createSelector(
  selectDocumentsForSelectedPanel,
  docs => ({
    total:    docs.length,
    approved: docs.filter(d => d.status === 'APPROVED').length,
    isComplete: docs.length > 0 && docs.every(d => d.status === 'APPROVED'),
  })
);
```

### `document.effects.ts`
```ts
@Injectable()
export class DocumentEffects {

  load$ = createEffect(() => this.actions$.pipe(
    ofType(DocumentActions.enter, DocumentActions.loadDocuments),
    switchMap(() => this.api.getDocuments().pipe(          // read → switchMap
      map(documents => DocumentActions.loadDocumentsSuccess({ documents })),
      catchError(e => of(DocumentActions.loadDocumentsFailure({ error: e.message })))
    ))
  ));

  upload$ = createEffect(() => this.actions$.pipe(
    ofType(DocumentActions.uploadDocument),
    concatMap(({ file, panelId }) =>                        // write → concatMap
      this.api.upload(file, panelId).pipe(
        map(document => DocumentActions.uploadDocumentSuccess({ document })),
        catchError(e => of(DocumentActions.uploadDocumentFailure({ error: e.message })))
      ))
  ));

  delete$ = createEffect(() => this.actions$.pipe(
    ofType(DocumentActions.deleteDocument),
    concatMap(({ id }) => this.api.delete(id).pipe(
      map(() => DocumentActions.deleteDocumentSuccess({ id })),
      catchError(e => of(DocumentActions.loadDocumentsFailure({ error: e.message })))
    ))
  ));

  notifyError$ = createEffect(() => this.actions$.pipe(
    ofType(DocumentActions.loadDocumentsFailure, DocumentActions.uploadDocumentFailure),
    tap(({ error }) => this.toastr.error(error))
  ), { dispatch: false });

  constructor(
    private actions$: Actions,
    private api: DocumentApiService,
    private toastr: ToastrService
  ) {}
}
```

### Unit tests — the payoff for all this structure
```ts
// Reducer test — pure function, zero mocks
it('sets loading on loadDocuments', () => {
  const state = documentReducer(initialState, DocumentActions.loadDocuments());
  expect(state.loading).toBe(true);
});

// Selector test — call the projector directly
it('counts approved documents', () => {
  const result = selectPanelCompletion.projector([
    { status: 'APPROVED' }, { status: 'PENDING' }
  ] as Document[]);
  expect(result.approved).toBe(1);
  expect(result.isComplete).toBe(false);
});

// Effect test — marble testing
it('dispatches success on load', () => {
  actions$ = hot('-a', { a: DocumentActions.loadDocuments() });
  api.getDocuments.and.returnValue(cold('-b|', { b: mockDocs }));
  expect(effects.load$).toBeObservable(
    hot('--c', { c: DocumentActions.loadDocumentsSuccess({ documents: mockDocs }) })
  );
});
```

> 💬 **Strong closing line:** *"`.projector()` is how you test a selector without building a whole state object — you call the projection function with its inputs directly. That testability is the main thing you buy with NgRx's boilerplate."*

---

# Part 11 — ComponentStore & SignalStore

### `@ngrx/component-store` — local state, no global store

For state that belongs to **one component or feature**, not the whole app.

```ts
interface QuizState { currentIndex: number; answers: Record<string, string>; }

@Injectable()
export class QuizStore extends ComponentStore<QuizState> {
  constructor() { super({ currentIndex: 0, answers: {} }); }

  // selectors
  readonly currentIndex$ = this.select(s => s.currentIndex);
  readonly answers$      = this.select(s => s.answers);

  // updaters (synchronous, like reducers)
  readonly next = this.updater(state => ({ ...state, currentIndex: state.currentIndex + 1 }));
  readonly setAnswer = this.updater((state, a: { q: string; v: string }) => ({
    ...state, answers: { ...state.answers, [a.q]: a.v }
  }));

  // effects (async)
  readonly submit = this.effect<void>(trigger$ => trigger$.pipe(
    withLatestFrom(this.answers$),
    exhaustMap(([, answers]) => this.api.submit(answers).pipe(
      tapResponse(r => this.router.navigate(['/result', r.id]),
                  e => this.toastr.error('Submit failed'))
    ))
  ));
}
```

Provided at the **component** level, so it's created and destroyed with the component. No actions, no global store, far less boilerplate.

| Global Store | ComponentStore |
|---|---|
| App-wide, shared state | Local/feature state |
| Actions + reducers + effects + selectors | Updaters + effects + selectors |
| DevTools time-travel | No actions to time-travel |
| Survives navigation | Dies with the component |

### `@ngrx/signals` SignalStore (NgRx 17+) — the modern direction

```ts
export const QuizStore = signalStore(
  { providedIn: 'root' },
  withState({ currentIndex: 0, answers: {} as Record<string, string> }),
  withComputed(({ answers }) => ({
    answeredCount: computed(() => Object.keys(answers()).length),
  })),
  withMethods(store => ({
    next: () => patchState(store, s => ({ currentIndex: s.currentIndex + 1 })),
  })),
);
```

> 💬 **Worth one sentence, honestly framed:** *"NgRx is moving toward the Signal Store — signals-based, far less boilerplate, no actions or reducers. I've read about it but haven't shipped it; our apps are Angular 16 and 18 with NgModules, so signals-first architecture wasn't on the table."* Don't overclaim here.

---

# Part 12 — Router Store & DevTools

### `@ngrx/router-store`
Puts router state into the store so route params/query params become selectable:

```ts
StoreModule.forRoot({ router: routerReducer }),
StoreRouterConnectingModule.forRoot(),

export const selectRouteParams = createSelector(selectRouterState, r => r.state.params);
export const selectApplicationId = createSelector(selectRouteParams, p => p['applicationId']);
```

### `@ngrx/store-devtools`
The Redux DevTools browser extension gives you:
- Every dispatched action in order, with its payload
- The full state tree before and after each action
- A diff view of exactly what changed
- **Time travel** — click any past action to restore that state
- Export/import state, so a tester can send you the exact state that broke

> 🎯 Great practical answer: *"DevTools is genuinely the biggest day-to-day benefit. A QA can export the store state at the moment of a bug and I can import it and reproduce it exactly — that turned 'cannot reproduce' tickets into fixable ones."*

---

# Part 13 — When NOT to use NgRx

**Being able to argue against NgRx is a senior signal.** Interviewers ask this deliberately.

### ❌ Don't use NgRx when

- The app is small or mostly CRUD screens
- State is **local** to a component — a form, a modal, a toggle
- Only one or two components share the data
- The team is small and unfamiliar with Redux — the learning curve is real
- You'd be writing 4 files (action, reducer, selector, effect) to store one boolean

### ✅ Do use NgRx when

- Many unrelated components read and write the same state
- State must survive navigation between routes
- You need an audit trail / undo-redo / time-travel debugging
- The team is large enough that convention beats individual judgement
- There's genuinely complex state interaction — optimistic updates, offline sync, websockets

### Alternatives on the spectrum

| Approach | Fits |
|---|---|
| Plain service + `BehaviorSubject` | ⭐ Small–medium shared state — **what RoboGebra uses** |
| `@ngrx/component-store` | Local/feature state with async work |
| Angular Signals (16+) | Synchronous local state, fine-grained reactivity |
| Full NgRx Store | Large app, many teams, complex shared state |

> 💬 **Your best answer to "would you use NgRx?":** *"It depends on how much state is genuinely shared. On EasyVisa the visa application state was read and written across many modules, so NgRx paid for itself in traceability. On RoboGebra most state is per-feature, so we used services with `BehaviorSubject` and the `async` pipe — the same unidirectional discipline without four files per feature. I'd rather start simple and introduce NgRx when the pain is real than pay the boilerplate tax up front."*

---

# Part 14 — Common mistakes

### ❌ 1. Mutating state in a reducer
Covered in [Part 4](#part-4--reducers). Enable `runtimeChecks` to catch it.

### ❌ 2. HTTP calls in reducers
Reducers must be pure. All async work belongs in effects.

### ❌ 3. `catchError` in the outer effect pipe
Kills the effect permanently. See [Part 6](#part-6--effects).

### ❌ 4. `switchMap` on a save effect
Cancels in-flight writes. Use `concatMap`.

### ❌ 5. Putting everything in the store
Form values mid-edit, modal open/closed, hover state — these don't belong in global state. Store what's **shared** and **needs to survive**.

### ❌ 6. Reusing one action across sources

```ts
// ❌ Now you can't tell which screen triggered it
export const loadData = createAction('[App] Load Data');

// ✅ Distinct actions, both handled by the same effect via ofType(a, b)
export const dashboardOpened = createAction('[Dashboard Page] Opened');
export const refreshClicked  = createAction('[Dashboard Page] Refresh Clicked');
```

### ❌ 7. Selecting state inside a subscribe instead of using `async`

```ts
// ❌ leaks unless you unsubscribe
this.store.select(selectDocuments).subscribe(d => this.docs = d);

// ✅
docs$ = this.store.select(selectDocuments);   // + async pipe
```

### ❌ 8. Deriving data in the component instead of a selector
Put derivation in a selector so it's memoized and testable — not in a getter that re-runs on every change detection cycle.

### ❌ 9. Dispatching from inside a reducer
Impossible by design, and the impulse means the action modelling is wrong. Use an effect.

---

# Part 15 — Interview Q&A

### Q: Explain the NgRx data flow.
> *"Unidirectional. A component dispatches an action describing what happened. Reducers handle it synchronously and return new immutable state. Effects listen to the same action stream for anything async — HTTP, routing, notifications — and dispatch follow-up success or failure actions that reducers then handle. Components read state through memoized selectors, usually with the async pipe. Nothing writes to the store directly; every change traces back to one named action."*

### Q: Reducer vs Effect?
> *"A reducer is a pure synchronous function — state in, new state out, no side effects. An effect is where impurity lives: HTTP, router navigation, localStorage, toasts. Effects don't touch state directly; they dispatch actions that reducers handle."*

### Q: Why must reducers be pure?
> *"Predictability and tooling. Same state plus same action must always give the same result, which is what makes time-travel debugging and replay work. And purity plus immutability is what lets memoized selectors and `OnPush` change detection use reference equality — mutate state and both silently break."*

### Q: What is selector memoization and why does it matter?
> *(See [Part 5](#part-5--selectors).)*

### Q: How do you handle errors in effects?
> *"`catchError` inside the inner observable, returning a failure action via `of(...)`. If you put it in the outer pipe the error terminates the `actions$` stream and the effect stops working for the rest of the session — silently, with no console error. I also usually have one non-dispatching effect that listens to all the failure actions and shows a toast, so error presentation is in one place."*

### Q: `forRoot` vs `forFeature`?
> *"`forRoot` initializes the store and effects once in the root injector. `forFeature` registers a state slice and its effects for a lazily-loaded module — the reducer is added to the store dynamically when that module loads, so feature state doesn't cost anything until the user goes there."*

### Q: What is `@ngrx/entity` for?
> *"Normalized collections. Instead of an array you keep `{ ids, entities }`, giving O(1) lookup by id and no duplication. `createEntityAdapter` generates `addOne`, `updateOne`, `removeOne`, `setAll` and matching selectors, which removes most of the repetitive immutable-update code from reducers."*

### Q: How do you test NgRx code?
> *"Reducers are pure functions — call them with a state and an action and assert on the result, no mocks. Selectors are tested with `.projector()`, passing inputs directly rather than constructing a full state tree. Effects are tested with `jasmine-marbles`, mocking the service and asserting the emitted action stream. That testability is really the return on NgRx's boilerplate."*

### Q: NgRx vs a service with a BehaviorSubject?
> *(See [Part 13](#part-13--when-not-to-use-ngrx) — this is the question where you cite EasyVisa vs RoboGebra.)*

### Q: How does NgRx improve performance?
> *"Two ways. Immutable state means `OnPush` components can skip change detection by reference comparison. And memoized selectors mean expensive derivations — filtering, sorting, aggregating — only recompute when their actual inputs change, not on every tick."*

---

# Part 16 — Rapid-fire

| Question | Answer |
|---|---|
| NgRx in one line | Redux for Angular, built on RxJS |
| The 5 building blocks | Store, Actions, Reducers, Selectors, Effects |
| The 3 principles | Single source of truth · read-only state · pure reducers |
| Action shape | `{ type: '[Source] Event', ...props }` |
| Action naming | `[Source] Event`, past tense, unique per source |
| Actions are… | Events, not commands |
| Reducer signature | `(state, action) => newState` |
| Reducer must be | Pure, immutable, side-effect free |
| Where does HTTP go? | Effects |
| Effect operator for reads | `switchMap` |
| Effect operator for writes | `concatMap` |
| Effect operator for submits | `exhaustMap` |
| `catchError` placement | Inside the inner observable |
| Effect with no dispatch | `{ dispatch: false }` |
| Read state in an effect | `concatLatestFrom` |
| Selector superpower | Memoization on input reference equality |
| Test a selector | `.projector(inputs)` |
| Test an effect | `jasmine-marbles` (`hot`/`cold`) |
| `createFeatureSelector` | Grabs a named feature slice |
| `createFeature` | Reducer + auto-generated selectors (NgRx 13+) |
| `createActionGroup` | Grouped actions, less boilerplate (NgRx 15+) |
| `@ngrx/entity` state shape | `{ ids: [], entities: {} }` |
| `forRoot` vs `forFeature` | Root store vs lazy-loaded slice |
| `runtimeChecks` | Dev-time guards against mutation/non-serializable state |
| ComponentStore | Local state, no global actions |
| SignalStore | NgRx 17+, signals-based, minimal boilerplate |
| DevTools killer feature | Time travel + exportable state |
| Biggest downside | Boilerplate — 4 files to change one value |
| NgRx version | Tracks Angular majors (Angular 16 → NgRx 16) |
| Facade pattern | Service wrapping dispatch + select |

---

## ⏱️ One-day drill

**Morning** — Parts 2–6. Draw the data-flow diagram from memory. Write an action, reducer, selector and effect for one feature without looking.

**Afternoon** — Parts 10 and 13. Rehearse the full EasyVisa slice, then rehearse the **"why NgRx on EasyVisa but not RoboGebra"** answer until it's natural. That comparison is the single most valuable thing in this file — it turns a potential gap into evidence of engineering judgement.

> **Final reminder:** never describe RoboGebra as an NgRx app. NgRx = **EasyVisa**. RoboGebra = **services with `BehaviorSubject`**. Being precise about which project used what is exactly the credibility that gets offers.
