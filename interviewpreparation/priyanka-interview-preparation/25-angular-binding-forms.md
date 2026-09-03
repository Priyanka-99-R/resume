# Angular — Data Binding & Forms (Deep Dive)

> 🎯 **Why this file exists:** binding and forms are the two things an Angular interviewer *always* drills, because they separate people who wrote real screens from people who read a tutorial. [04 — Angular](./04-angular.md) covers them in about 10 questions; this file goes all the way down — including **`setValue` vs `patchValue`**, **`value` vs `getRawValue()`**, **`ControlValueAccessor`**, and a **"what happens?" section** in the same style as [23](./23-java-output-tricky-questions.md), because your rounds are output-based.

> ⚠️ **Version note — attribute your work correctly.** RoboGebra web is **Angular 16.2**; the Ionic app is **Angular 18.2** (Ionic 8.7.5). **Angular Formly is EasyVisa, not RoboGebra.** Features below are tagged with the version that introduced them — don't claim `model()` signals on a 16.2 codebase. *(See [18 — RoboGebra Technical Stack](./18-robogebra-technical-versions.md).)*

---

## Table of contents

| # | Section | What it covers |
|---|---|---|
| [A](#a--data-binding--the-four-types) | The 4 binding types | Syntax table, direction, when each is used |
| [B](#b--property-vs-attribute-binding) | Property vs attribute | The `[attr.colspan]` question, `[disabled]`, class & style binding |
| [C](#c--two-way-binding--how-it-actually-works) | Two-way binding | Desugaring `[(x)]`, custom two-way `@Input`/`@Output`, `model()` |
| [D](#d--input--output-deep-dive) | `@Input` / `@Output` | Setters vs `ngOnChanges`, `required`, `transform`, signal inputs |
| [E](#e--binding-gotchas--change-detection) | Binding gotchas | `ExpressionChangedAfterItHasBeenChecked`, function calls in templates, OnPush |
| [F](#f--template-driven-vs-reactive-forms) | TD vs Reactive | The full comparison table + when you'd pick each |
| [G](#g--reactive-forms--the-building-blocks) | Reactive building blocks | `FormControl`, `FormGroup`, `FormArray`, `FormBuilder`, **typed forms** |
| [H](#h--reading--writing-form-state) | Reading & writing state | `setValue` vs `patchValue`, `value` vs `getRawValue()`, `reset`, `updateOn` |
| [I](#i--control-state--the-css-classes) | Control state | pristine/dirty/touched/valid/pending/disabled + the ng- classes |
| [J](#j--validation) | Validation | Built-in, custom, **cross-field**, **async**, dynamic validators, error display |
| [K](#k--formarray--dynamic-forms) | `FormArray` & dynamic forms | Add/remove rows, nested groups, Formly (EasyVisa) |
| [L](#l--controlvalueaccessor--custom-form-controls) | **`ControlValueAccessor`** | The senior question — full working implementation |
| [M](#m--what-happens--18-binding--forms-traps) | 🔴 **What happens? (18 traps)** | Output-style questions on binding & forms |
| [N](#n--testing-forms) | Testing forms | `TestBed`, setting values, async validators |
| [O](#o--rapid-fire-answer-table) | Rapid-fire table | 1-line answers |
| [P](#p--12-self-test-drills-answers-hidden) | 12 drills | Answers hidden |
| [Q](#q--answer-scripts-say-these-out-loud) | Answer scripts | Word-for-word answers for 6 likely questions |

---

## 🧠 The easiest way to remember ALL of this file

Two pictures cover binding and forms completely.

### Binding — follow the ARROWS

```
        CLASS                          TEMPLATE
          │   {{ }}  and  [ ]   ──────────▶   data flows DOWN ⬇️
          │
          ◀──────────    ( )   ──────────     events flow UP ⬆️

        [( )]  = both directions = "banana in a box" 🍌📦
```

### Forms — WHERE does the truth live?

```
TEMPLATE-DRIVEN → the form's truth lives in the HTML   → ngModel
REACTIVE ⭐     → the form's truth lives in the CLASS  → FormGroup

The deciding line: "reactive forms are testable WITHOUT rendering the template." ⭐
```

### The five traps this whole file exists to teach ⭐

```
1. [disabled]="x" on a REACTIVE control → Angular warns and it doesn't work ⭐
2. setValue = STRICT (throws) | patchValue = SILENT (a typo does nothing) ⭐
3. form.value EXCLUDES disabled controls → getRawValue() includes them ⭐⭐
4. valueChanges + setValue inside it = an INFINITE LOOP ⭐
5. Changing a bound value too late → ExpressionChangedAfterItHasBeenChecked ⭐
```

⭐ If you remember nothing else from this file, remember **#3** — it silently wipes a database column.

#### Easy memory

```
Binding: {{ }} [ ] down ⬇️ | ( ) up ⬆️ | [( )] both 🍌📦
Forms: truth in the HTML (template-driven) vs truth in the CLASS (reactive ⭐)

THE five traps: [disabled] | setValue/patchValue | getRawValue ⭐⭐
                valueChanges loop | ExpressionChanged...
```

---

# A — Data binding: the four types

### Q: What are the types of data binding in Angular?

| Type | Syntax | Direction | Example |
|---|---|---|---|
| **Interpolation** | `{{ }}` | Component → View | `<h1>{{ title }}</h1>` |
| **Property binding** | `[prop]` | Component → View | `<img [src]="imageUrl">` |
| **Event binding** | `(event)` | View → Component | `<button (click)="save()">` |
| **Two-way binding** | `[(ngModel)]` | Both | `<input [(ngModel)]="name">` |

> 💬 **The line they want:** *"Interpolation and property binding are one-way from component to view, event binding is one-way from view to component, and two-way binding is just those two combined — `[(x)]` desugars to `[x]` plus `(xChange)`."*

### Q: Interpolation vs property binding — is there a difference?
For strings they're equivalent: `<img src="{{url}}">` ≡ `<img [src]="url">`. But:
- Interpolation **always stringifies** — binding an object gives `[object Object]`.
- Property binding can pass **non-string values** — objects, arrays, booleans, numbers. `[disabled]="false"` works; `disabled="{{false}}"` sets the attribute to the *string* `"false"`, which is truthy in the DOM.

✅ **Rule: use property binding for anything that isn't text.**

### Q: What is `$event`?
The payload of the event. For DOM events it's the native event object; for `@Output()` it's whatever the child emitted.
```html
<input (input)="onInput($event)">                 <!-- native InputEvent -->
<app-child (saved)="onSaved($event)"></app-child> <!-- the emitted value -->
```

### Q: What's the safe navigation operator?
`?.` — guards against `null`/`undefined` in a template so it renders nothing instead of throwing.
```html
{{ user?.address?.city }}
{{ items?.length ?? 0 }}       <!-- ?? nullish coalescing, Angular 12+ -->
<div *ngIf="user as u">{{ u.name }}</div>
```
`$any(expr)` is the escape hatch that turns off template type-checking for one expression — use it rarely.

---

# B — Property vs attribute binding

### Q: What's the difference between an HTML attribute and a DOM property?

**Attributes are the initial value in the HTML; properties are the current state of the DOM object.** Angular binding works on **properties**, not attributes.

```html
<input value="hello">
<!-- attribute stays "hello" forever; the .value PROPERTY changes as the user types -->
```

### Q: So when do you need `[attr.x]`?
When the element has **no matching DOM property** — Angular would throw *"Can't bind to 'x' since it isn't a known property"*.

```html
<td [attr.colspan]="span"></td>          <!-- colSpan property exists but attr form is clearer -->
<button [attr.aria-label]="label">       <!-- ARIA has NO DOM property → attr required -->
<svg><rect [attr.width]="w"></rect></svg><!-- SVG has no width property -->
<div [attr.data-id]="id">                <!-- data-* attributes -->
```
> 💬 *"`[attr.]` is for attributes with no DOM property — ARIA, `data-*`, SVG, and `colspan`. Everything else binds to the property directly."*

Setting `[attr.x]="null"` **removes** the attribute — useful for conditional ARIA.

### Q: Class and style binding — all the forms

```html
<!-- single class, toggled by a boolean -->
<div [class.active]="isActive">

<!-- multiple classes from an object -->
<div [ngClass]="{ active: isActive, disabled: isDisabled }">
<div [ngClass]="isActive ? 'active' : 'inactive'">
<div [ngClass]="['a', 'b']">

<!-- single style, with a unit -->
<div [style.width.px]="width">
<div [style.background-color]="color">

<!-- multiple styles -->
<div [ngStyle]="{ 'font-size.px': size, color: color }">
```

| | `[class.x]` / `[style.x]` | `ngClass` / `ngStyle` |
|---|---|---|
| What | Built-in **binding syntax** | **Directives** |
| Best for | One known class/style | A dynamic set |
| Cost | Cheaper | Object is diffed each CD cycle |

✅ **Prefer `[class.x]` when you know the class name** — it's faster and clearer. Reach for `ngClass` when the set is dynamic.

### ⚠️ The `[disabled]` trap
```html
<button disabled="{{ isDisabled }}">     <!-- ❌ always disabled — "false" is a non-empty string -->
<button [disabled]="isDisabled">         <!-- ✅ real boolean -->
```

---

# C — Two-way binding — how it actually works

### Q: How does `[(ngModel)]` work under the hood?

The "banana in a box" `[(x)]` is **pure syntactic sugar**. Angular desugars it into a property binding plus an event binding whose name is the property + **`Change`**:

```html
<input [(ngModel)]="name">
<!-- desugars to -->
<input [ngModel]="name" (ngModelChange)="name = $event">
```

### Q: How do you create your own two-way bindable property?

**The convention: `@Input() x` + `@Output() xChange`.** The `Change` suffix is not optional — that's what the desugaring looks for.

```ts
@Component({
  selector: 'app-counter',
  template: `
    <button (click)="decrement()">−</button>
    {{ count }}
    <button (click)="increment()">+</button>
  `,
})
export class CounterComponent {
  @Input() count = 0;
  @Output() countChange = new EventEmitter<number>();

  increment() { this.count++; this.countChange.emit(this.count); }
  decrement() { this.count--; this.countChange.emit(this.count); }
}
```
```html
<app-counter [(count)]="total"></app-counter>   <!-- ✅ works -->
```

### 🆕 `model()` — signal-based two-way binding *(Angular 17.2+ — the Ionic app on 18.2, **not** the 16.2 web app)*
```ts
export class CounterComponent {
  count = model(0);                      // input + output in one
  increment() { this.count.update(c => c + 1); }   // auto-emits
}
```

> 💬 **Say this:** *"`[(x)]` is sugar for `[x]` plus `(xChange)` — so any component becomes two-way bindable just by naming the output `<inputName>Change`. From Angular 17.2 there's `model()`, which is a writable signal that is an input and an output at once. RoboGebra web is on 16.2, so there I'd use the `@Input`/`@Output` pair; the Ionic app is on 18.2 where `model()` is available."*

### ⚠️ Two-way binding vs the store
Two-way binding is fine for local UI state. For shared/app state it hides *who* changed *what* — that's why NgRx (EasyVisa) pushes you to one-way data flow with explicit actions.

---

# D — `@Input` / `@Output` deep dive

### Q: How do you react to an `@Input` change?

**Two ways — know both and when to pick each.**

```ts
// 1. Setter — for ONE input, runs immediately on set
@Input()
set userId(value: string) {
  this._userId = value;
  this.loadUser(value);
}
private _userId!: string;

// 2. ngOnChanges — for MULTIPLE inputs, or when you need the previous value / first-change flag
ngOnChanges(changes: SimpleChanges) {
  if (changes['userId'] && !changes['userId'].firstChange) {
    this.loadUser(changes['userId'].currentValue);
    console.log(changes['userId'].previousValue);
  }
}
```

| | Setter | `ngOnChanges` |
|---|---|---|
| Runs | Immediately on assignment | Once per CD cycle, before `ngOnInit` |
| Previous value | You track it yourself | `SimpleChange.previousValue` ✅ |
| Multiple inputs | One setter each | All in one object ✅ |
| First change? | Track manually | `.firstChange` ✅ |

### ⚠️ `ngOnChanges` only fires for **template-bound** inputs and only when the **reference** changes
```ts
this.items.push(newItem);        // ❌ same array reference — ngOnChanges does NOT fire
this.items = [...this.items, newItem];   // ✅ new reference — fires
```
This is exactly why **`OnPush` components need immutable updates**.

### 🆕 Input options *(Angular 16+ — available on RoboGebra web 16.2)*
```ts
@Input({ required: true }) userId!: string;                 // compile error if not provided
@Input({ transform: booleanAttribute }) disabled = false;   // <app-x disabled> works like HTML
@Input({ transform: numberAttribute }) count = 0;
@Input('aliasName') internalName!: string;                  // alias — avoid, hurts readability
```

### 🆕 Signal inputs *(Angular 17.1+ — the Ionic app only)*
```ts
userId = input.required<string>();
count   = input(0, { transform: numberAttribute });
total   = computed(() => this.count() * 2);      // reacts automatically, no ngOnChanges
```

### Q: `@Output` best practices
```ts
@Output() saved = new EventEmitter<User>();      // name it as a past-tense event, not "onSave"
this.saved.emit(user);
```
- Don't prefix with `on` — the template already reads `(saved)="onSaved()"`.
- Emit **data**, not DOM events.
- `EventEmitter` extends `Subject`, but **only use it for `@Output`** — for anything else use a `Subject` directly.

---

# E — Binding gotchas & change detection

### 🔴 Q: What is `ExpressionChangedAfterItHasBeenCheckedError`?

**The most-asked Angular binding error.** In **dev mode only**, Angular runs change detection a second time and verifies nothing changed. If a bound value changed *during* or *after* the first pass, it throws.

```ts
@Component({ template: `{{ value }}` })
export class BadComponent implements AfterViewInit {
  value = 'initial';
  ngAfterViewInit() {
    this.value = 'changed';     // ❌ view was already checked → throws
  }
}
```

**Common causes:**
1. Changing a bound value in `ngAfterViewInit` / `ngAfterViewChecked`.
2. A **child** changing a **parent's** state during the parent's check (data flows top-down; Angular calls this unidirectional data flow).
3. A getter or method in the template returning a **new object/array each call**:
   ```html
   {{ getItems() }}   <!-- returns a fresh array every call → never "equal" → throws -->
   ```

**Fixes:**
- Move the change to `ngOnInit`.
- `cdr.detectChanges()` to run CD again immediately, or `cdr.markForCheck()`.
- Defer a tick: `Promise.resolve().then(() => this.value = 'changed')` or `setTimeout`.
- **Best:** restructure so the value is computed before the view is checked.

> 💬 *"It only appears in dev mode, and it's Angular telling me I've broken unidirectional data flow — something changed after the view was already checked. I fix the data flow rather than papering over it with `setTimeout`."*

### ⚠️ Never call functions in templates (for anything expensive)
```html
{{ calculateTotal() }}      <!-- ❌ runs on EVERY change detection cycle — dozens of times -->
```
Fixes: compute in `ngOnInit`/a setter, use a **pure pipe**, use a `computed()` signal (16+), or `OnPush`.

### Q: How does binding interact with `OnPush`?
An `OnPush` component is re-checked only when:
1. an **`@Input` reference** changes,
2. an event fires **from its own template**,
3. an **`async` pipe** in its template emits,
4. you call `markForCheck()` explicitly.

> 💬 *"With `OnPush` I have to treat inputs as immutable — mutating an object in place won't trigger a re-render because the reference didn't change."*

### Q: What's the `async` pipe and why is it the right way to bind an Observable?
```html
<div *ngIf="user$ | async as user">{{ user.name }}</div>
```
It subscribes, renders each emission, **unsubscribes automatically on destroy**, and calls `markForCheck()` so it works with `OnPush`. No manual `takeUntil` needed. *(Full detail in [20 — RxJS](./20-rxjs-operators.md).)*

---

# F — Template-driven vs Reactive forms

### 🔴 The comparison table (memorize this)

| | **Template-driven** | **Reactive (model-driven)** |
|---|---|---|
| Module | `FormsModule` | `ReactiveFormsModule` |
| Form model | Created **implicitly** by directives in the template | Created **explicitly** in the class |
| Source of truth | The template | **The component class** |
| Data flow | **Asynchronous** | **Synchronous** ✅ |
| Directives | `ngModel`, `ngForm`, `ngModelGroup` | `formControl`, `formGroup`, `formControlName`, `formArrayName` |
| Validation | Directives in the template (`required`, `minlength`) | **Functions** in the class (`Validators.required`) |
| Dynamic fields | Hard | **Easy** (`FormArray`) ✅ |
| Type safety | None | **Typed forms** (Angular 14+) ✅ |
| Testability | Needs a fixture + `whenStable()` | **Unit-testable without the DOM** ✅ |
| Scales to | Small, simple forms (login, a search box) | Complex, dynamic, conditional forms ✅ |
| Mutability | Mutable — the directive mutates your model | **Immutable** — `valueChanges` emits a new value |

### ✅ The answer they want
> *"Template-driven forms build the model implicitly from directives in the template, are asynchronous, and suit simple forms. Reactive forms create the model explicitly in the class, are synchronous and immutable, and give you typed forms, `FormArray` for dynamic fields, and easy unit testing. **I use reactive forms for anything non-trivial** — on EasyVisa the visa forms were reactive and rendered from JSON config through Angular Formly, with `fieldGroup` for nesting and `fieldArray` for repeatable sections."*

### Q: Can you mix them?
Technically in the same app, yes — but **never on the same control**. `[(ngModel)]` together with `formControlName` throws an error (it was deprecated in Angular 6 and removed in v11+).

---

# G — Reactive forms — the building blocks

### The three classes (all extend `AbstractControl`)

```
AbstractControl
├── FormControl   — a single field
├── FormGroup     — an object of controls  { name, email }
└── FormArray     — a list of controls     [ c0, c1, c2 ]
```

```ts
// verbose form
form = new FormGroup({
  name:  new FormControl('', [Validators.required, Validators.minLength(3)]),
  email: new FormControl('', [Validators.required, Validators.email]),
  address: new FormGroup({
    city: new FormControl(''),
    zip:  new FormControl(''),
  }),
  phones: new FormArray([ new FormControl('') ]),
});
```

### `FormBuilder` — the same thing, less noise
```ts
constructor(private fb: FormBuilder) {}

form = this.fb.group({
  name:  ['', [Validators.required, Validators.minLength(3)]],
  email: ['', [Validators.required, Validators.email], [this.emailTakenValidator()]],
  //      ↑ initial   ↑ sync validators                  ↑ async validators
  address: this.fb.group({ city: [''], zip: [''] }),
  phones:  this.fb.array([ this.fb.control('') ]),
});
```

```html
<form [formGroup]="form" (ngSubmit)="onSubmit()">
  <input formControlName="name">

  <div formGroupName="address">
    <input formControlName="city">
  </div>

  <div formArrayName="phones">
    <div *ngFor="let ctrl of phones.controls; let i = index">
      <input [formControlName]="i">
    </div>
  </div>

  <button type="submit" [disabled]="form.invalid">Save</button>
</form>
```

### 🔴 Q: What are typed forms? *(Angular 14+ — available on both your codebases)*

Before v14 every form value was `any`. Now `FormGroup` infers its value type:

```ts
form = this.fb.group({
  name:  ['', Validators.required],
  age:   [0],
});

this.form.value;                  // { name?: string | null; age?: number | null }
this.form.controls.name;          // FormControl<string | null>
this.form.get('nmae');            // ❌ compile error — typo caught
```

**Two things to know:**
1. Values are `| null` because `reset()` sets controls to `null` — unless you use **`nonNullable`**:
   ```ts
   name: this.fb.nonNullable.control('', Validators.required);   // FormControl<string>
   // reset() now restores '' instead of null
   // or: new FormControl('', { nonNullable: true, validators: [Validators.required] })
   ```
   `this.fb.nonNullable.group({...})` makes the whole group non-nullable.
2. `value` is **`Partial`** because disabled controls are excluded — use `getRawValue()` for the complete typed value.

**Opting out during migration:** `UntypedFormGroup` / `UntypedFormControl` — that's what `ng update` generates so nothing breaks.

> 💬 *"Typed forms landed in Angular 14 and the migration just renames everything to `Untyped*` so nothing breaks; you then opt in file by file. The two things that surprise people are that values are nullable because `reset()` sets `null` — which `nonNullable` fixes — and that `.value` is a `Partial` because disabled controls are omitted."*

---

# H — Reading & writing form state

### 🔴 Q: `setValue()` vs `patchValue()` — the most-asked forms question

| | `setValue()` | `patchValue()` |
|---|---|---|
| Requires | **Every** control, exactly matching the structure | Any **subset** |
| Extra/missing key | **Throws an error** ✅ (strict) | **Silently ignored** ⚠️ |
| Use for | Full replacement — safer, catches structure drift | Partial updates (patching a server response) |

```ts
form = this.fb.group({ name: [''], email: [''] });

this.form.setValue({ name: 'Priyanka' });
// ❌ Error: Must supply a value for form control with name: 'email'

this.form.patchValue({ name: 'Priyanka' });        // ✅ email untouched
this.form.patchValue({ nmae: 'typo' });            // ⚠️ silently does nothing — no error!
```

> 💬 *"`setValue` is strict — it throws if the shape doesn't match, which is exactly what I want when I'm loading a record into a form, because a renamed field fails loudly. `patchValue` ignores unknown keys silently, so I use it only for genuine partial updates."*

#### Real-world idea

```
setValue   → filling in an OFFICIAL FORM: every box must be completed,
             and an unknown box is rejected at the counter ✅ ⭐

patchValue → editing a few lines of a document: everything else is left alone,
             and a misspelt heading is simply… ignored ⚠️
```

⭐ The trap is `patchValue`'s **silence**:

```ts
this.form.patchValue({ nmae: 'typo' });     // ⚠️ no error, no warning, no change
```

```
The field just never updates.
No console message. You debug the API, the service, the template…
and the bug is a typo in a key. 💥
```

**That is why `setValue` is safer for loading a record** — a renamed backend field fails loudly instead of silently blanking a screen.

#### Easy memory

```
setValue ⭐   → STRICT → every control required → a typo THROWS ✅ (an official form)
patchValue    → PARTIAL → a subset is fine → a typo is SILENT ⚠️ 💥

Loading a record? → setValue (fail loudly)
Genuine partial update? → patchValue
```

### 🔴 Q: `value` vs `getRawValue()`

**`value` excludes disabled controls. `getRawValue()` includes them.**

```ts
form = this.fb.group({
  name:  ['Priyanka'],
  email: [{ value: 'p@x.com', disabled: true }],
});

this.form.value;         // { name: 'Priyanka' }              ⚠️ email missing!
this.form.getRawValue(); // { name: 'Priyanka', email: 'p@x.com' } ✅
```

> ⚠️ **The bug this causes:** you disable a field, submit `form.value`, and the API wipes that column because the key wasn't sent. **If you disable fields, submit `getRawValue()`.**

#### 🔴 Trace the bug — this is worth memorising ⭐

```
1. You disable the `email` field because the user can't edit it
2. The user saves
3. form.value  →  { name: 'Priyanka' }        ← `email` is NOT in the object ⭐
4. PUT /users/1 with that body
5. The backend treats a MISSING key as "set it to null"
6. The user's email is now EMPTY in the database 💥
```

```
No exception. No red text. No failing test.
Just a column quietly blanked in production. ⭐
```

Real-world idea: a form where the greyed-out boxes are **cut off the page** before it's posted. The clerk assumes those fields were meant to be cleared.

#### The three fixes

```
1. form.getRawValue()                    ⭐ include disabled controls
2. Use readonly instead of disabled      (readonly stays in .value ⭐)
3. Send a PATCH, not a PUT               (a missing key means "don't change it")
```

⭐ Option 2 is a genuinely good point to raise: **`readonly` is a display concern, `disabled` is a form-state concern** — and people reach for `disabled` when they only wanted the field to look uneditable.

#### Easy memory

```
form.value      → EXCLUDES disabled controls ⚠️
form.getRawValue() → INCLUDES them ⭐

THE BUG: disable a field → submit .value → the API blanks that column 💥
FIX: getRawValue() ⭐ | or `readonly` instead of `disabled` | or PATCH not PUT
```

### Q: `valueChanges` and `statusChanges`
```ts
this.form.valueChanges                      // Observable<T> — every value change
  .pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
  .subscribe(v => this.autoSave(v));

this.form.get('country')!.valueChanges       // per-control
  .subscribe(c => this.loadStates(c));

this.form.statusChanges                      // 'VALID' | 'INVALID' | 'PENDING' | 'DISABLED'
  .subscribe(s => console.log(s));
```
⚠️ **Always unsubscribe** (`takeUntil` or `takeUntilDestroyed()` in v16+) — a form subscription is a classic memory leak.

### ⚠️ The infinite loop
```ts
this.form.valueChanges.subscribe(() => {
  this.form.patchValue({ total: this.calc() });   // ❌ fires valueChanges again → infinite loop
});
```
**Fix:** `patchValue(..., { emitEvent: false })`.

### Q: `updateOn` — controlling *when* the value updates
```ts
name = new FormControl('', { updateOn: 'blur' });          // 'change' (default) | 'blur' | 'submit'
form = this.fb.group({...}, { updateOn: 'submit' });        // for the whole group
```
Great for expensive async validators — validate on blur instead of every keystroke.

### Q: `reset()`
```ts
this.form.reset();                                   // all controls → null, pristine + untouched
this.form.reset({ name: 'Priyanka', email: '' });    // reset to specific values
```
⚠️ `reset()` sets `null`, **not `''`** — which is why bound `<input>`s go empty but your typed value becomes `null`. `nonNullable` controls reset to their **initial** value instead.

### Q: Enable/disable programmatically
```ts
this.form.get('email')!.disable();                    // emits valueChanges
this.form.get('email')!.disable({ emitEvent: false });
this.form.enable();
```
⚠️ **Never** use `[disabled]="cond"` in the template with reactive forms — Angular logs a warning and the state can desync. Do it in the class.

---

# I — Control state & the CSS classes

| Property | Meaning | Opposite |
|---|---|---|
| `valid` | Passes all validators | `invalid` |
| `pending` | An **async validator** is running | — |
| `disabled` | Excluded from `value` **and from validation** | `enabled` |
| `pristine` | Value never changed by the user | `dirty` |
| `touched` | The control has been blurred | `untouched` |

Angular mirrors these onto the element as CSS classes:
`ng-valid` / `ng-invalid` / `ng-pending` / `ng-pristine` / `ng-dirty` / `ng-touched` / `ng-untouched`

```css
input.ng-invalid.ng-touched { border-color: red; }   /* only show red AFTER they leave the field */
```

### 🔴 The disabled-control trap
**A disabled control is excluded from validation, so an invalid-but-disabled control makes the form `VALID`.**
```ts
form.get('email')!.setValidators(Validators.required);
form.get('email')!.disable();
form.valid;   // true ✅ — even though email is empty and required
```

### Q: How do you show errors only when appropriate?
```html
<input formControlName="email" [class.is-invalid]="isInvalid('email')">
<div class="error" *ngIf="isInvalid('email')">
  <span *ngIf="form.get('email')?.errors?.['required']">Email is required</span>
  <span *ngIf="form.get('email')?.errors?.['email']">Enter a valid email</span>
</div>
```
```ts
isInvalid(name: string): boolean {
  const c = this.form.get(name);
  return !!c && c.invalid && (c.dirty || c.touched);
}
```

### Q: How do you show all errors when the user hits Submit without touching anything?
```ts
onSubmit() {
  if (this.form.invalid) {
    this.form.markAllAsTouched();     // ✅ turns on every error message at once
    return;
  }
  this.api.save(this.form.getRawValue()).subscribe();
}
```
Related: `markAsTouched()`, `markAsDirty()`, `markAsPristine()`, `updateValueAndValidity()`.

---

# J — Validation

### Built-in validators
`required`, `requiredTrue` (checkboxes — terms & conditions), `min`, `max`, `minLength`, `maxLength`, `pattern`, `email`, `nullValidator`, `compose`, `composeAsync`.

```ts
age: [null, [Validators.required, Validators.min(18), Validators.max(120)]],
terms: [false, Validators.requiredTrue],      // ⚠️ NOT Validators.required — false is "empty"
```

### ⚠️ `Validators.pattern` is auto-anchored
`Validators.pattern('\\d+')` becomes `^\d+$`. If you pass a **RegExp object** it is **not** anchored — a common source of "why does my partial match pass?"

### Custom sync validator
```ts
export function noWhitespace(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const isBlank = (control.value || '').trim().length === 0;
    return isBlank ? { whitespace: true } : null;      // null = VALID
  };
}
// usage
name: ['', [Validators.required, noWhitespace()]]
```
> 📏 **The rule to say out loud:** *"A validator returns `null` when the control is valid, and an error object when it isn't. It's counter-intuitive the first time — null means success."*

### 🔴 Cross-field validator (goes on the **FormGroup**)
```ts
export const passwordsMatch: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const pwd = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return pwd === confirm ? null : { passwordMismatch: true };
};

form = this.fb.group({
  password: ['', Validators.required],
  confirmPassword: ['', Validators.required],
}, { validators: passwordsMatch });               // ← second argument
```
```html
<div *ngIf="form.errors?.['passwordMismatch'] && form.get('confirmPassword')?.touched">
  Passwords do not match
</div>
```
> ⚠️ The error lands on the **group**, not the control — `form.errors`, not `form.get('confirmPassword').errors`.

### 🔴 Async validator (server-side uniqueness)
```ts
emailTaken(): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    if (!control.value) return of(null);
    return timer(400).pipe(                              // debounce
      switchMap(() => this.api.checkEmail(control.value)),
      map(taken => (taken ? { emailTaken: true } : null)),
      catchError(() => of(null)),                        // never block the user on an API failure
      first()                                            // ✅ MUST complete
    );
  };
}
// third argument position:
email: ['', [Validators.required, Validators.email], [this.emailTaken()]]
```

**Four things interviewers check here:**
1. Async validators go in the **third** argument (or `{ asyncValidators: [...] }`).
2. They run **only after the sync validators pass** — no wasted API calls on an invalid email.
3. The control's status is **`PENDING`** while in flight — disable submit on `form.invalid || form.pending`.
4. The Observable **must complete** (`first()` / `take(1)`) or the status stays `PENDING` forever. ⚠️ This is the classic bug.

### Dynamic validators
```ts
const phone = this.form.get('phone')!;
if (contactByPhone) {
  phone.setValidators([Validators.required, Validators.pattern(/^\d{10}$/)]);
} else {
  phone.clearValidators();
}
phone.updateValueAndValidity();      // ⚠️ REQUIRED — without this nothing re-evaluates
```
> ⚠️ **`setValidators` alone does nothing** until you call `updateValueAndValidity()`. Guaranteed follow-up question.

### `setErrors` — manual server-side errors
```ts
this.api.save(v).subscribe({
  error: err => {
    if (err.status === 409) {
      this.form.get('email')!.setErrors({ serverTaken: true });
    }
  }
});
```

---

# K — `FormArray` & dynamic forms

### Q: When do you use `FormArray`?
When the **number of controls isn't known at compile time** — repeatable rows: phone numbers, line items, applicants on a visa case.

```ts
form = this.fb.group({
  name:   ['', Validators.required],
  phones: this.fb.array([ this.createPhone() ]),
});

get phones(): FormArray {                                   // ✅ getter for the template
  return this.form.get('phones') as FormArray;
}

createPhone(): FormGroup {
  return this.fb.group({
    type:   ['mobile'],
    number: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
  });
}

addPhone()            { this.phones.push(this.createPhone()); }
removePhone(i: number){ this.phones.removeAt(i); }
clearPhones()         { this.phones.clear(); }
```

```html
<div formArrayName="phones">
  <div *ngFor="let group of phones.controls; let i = index" [formGroupName]="i">
    <select formControlName="type">
      <option value="mobile">Mobile</option>
      <option value="home">Home</option>
    </select>
    <input formControlName="number">
    <button type="button" (click)="removePhone(i)">Remove</button>
    <div *ngIf="group.get('number')?.touched && group.get('number')?.invalid">
      10-digit number required
    </div>
  </div>
</div>
<button type="button" (click)="addPhone()">Add phone</button>
```

**Three `FormArray` gotchas:**
1. `form.get('phones')` returns `AbstractControl` — **cast to `FormArray`** (that's what the getter is for).
2. Inside `*ngFor` use `[formGroupName]="i"` for groups, `[formControlName]="i"` for plain controls — **the index, in brackets**.
3. Iterate `phones.controls`, not `phones.value`.

### Q: How do you build fully dynamic forms from config? *(Your EasyVisa story)*

**Angular Formly** — render a form from a JSON field config instead of hand-writing templates.

```ts
fields: FormlyFieldConfig[] = [{
  key: 'applicants',
  type: 'repeat',                                     // fieldArray → repeatable section
  fieldArray: {
    fieldGroup: [                                     // nesting
      { key: 'firstName', type: 'input', props: { label: 'First name', required: true } },
      { key: 'hasEmployer', type: 'checkbox', props: { label: 'Employed?' } },
      {
        key: 'employer', type: 'input',
        props: { label: 'Employer' },
        expressions: {                                // conditional show/hide + dynamic required
          hide: '!model.hasEmployer',
          'props.required': 'model.hasEmployer',
        },
      },
    ],
  },
}];
```
```html
<form [formGroup]="form" (ngSubmit)="submit()">
  <formly-form [form]="form" [fields]="fields" [model]="model"></formly-form>
</form>
```

> 💬 **Your answer:** *"On EasyVisa the visa forms were huge, conditional and repeatable — a section could have N applicants each with nested address and employment history, and fields appeared based on earlier answers. Hardcoding that was unmaintainable, so I drove it from a JSON field config with Formly: `fieldGroup` for nesting, `fieldArray` for repeatable sections, and `expressions` for conditional show/hide and dynamic required. Underneath it's still reactive forms — Formly builds the same `FormGroup` tree — so validity aggregates up from the nested groups to the submit button automatically. A new form section became a config change instead of a new component."*
>
> ⚠️ **Attribution:** Formly = **EasyVisa**. RoboGebra has **no Formly and no NgRx**.

---

# L — `ControlValueAccessor` — custom form controls

> 🔴 **This is the senior-level forms question.** If you can implement it on a whiteboard, you're clearly above the "I used `formControlName`" level.

### Q: What is `ControlValueAccessor`?

The **bridge between Angular's form API and a custom component**. It's how Angular knows how to write a value into your component and how to hear about changes coming out of it. Implement it and your component works with `formControlName`, `ngModel`, validators, `touched`/`dirty` — everything.

#### The easiest way to remember it

```
Angular already knows how to talk to <input>.
It has NO IDEA how to talk to YOUR component.

ControlValueAccessor is the TRANSLATOR between them. ⭐
```

```
   FORM API                CVA (the translator)            YOUR COMPONENT
   ────────                ────────────────────            ──────────────
   patchValue() ──────▶    writeValue(v)          ──────▶  show the value ⬇️
   .disable()   ──────▶    setDisabledState(true) ──────▶  grey it out
   listens      ◀──────    onChange(v)            ◀──────  the user clicked ⬆️
   listens      ◀──────    onTouched()            ◀──────  the user blurred
```

Real-world idea: an **interpreter at a meeting.** Angular speaks "form API"; your star-rating component speaks "clicks and stars". The CVA translates in both directions — and once it's in place, the two sides behave as if they always understood each other.

⭐ The memory hook for the four methods — **two in, two out**:

```
IN  (forms → you):  writeValue ⬇️ | setDisabledState ⬇️
OUT (you → forms):  registerOnChange ⬆️ | registerOnTouched ⬆️
```

⚠️ And the two provider details that are always forgotten:

```
forwardRef  → the class isn't defined yet when the decorator is evaluated ⭐
multi: true → NG_VALUE_ACCESSOR is a MULTI provider; without it you REPLACE
              every other accessor in the app 💥
```

### The four methods

| Method | Direction | Purpose |
|---|---|---|
| `writeValue(v)` | **Forms → your component** | Angular pushes a value in (`patchValue`, `reset`, initial value) |
| `registerOnChange(fn)` | **Your component → forms** | Save `fn`; call it whenever the value changes |
| `registerOnTouched(fn)` | Your component → forms | Save `fn`; call it on blur to mark `touched` |
| `setDisabledState(isDisabled)` | Forms → your component | Called by `.disable()` / `.enable()` |

### Full working implementation — a star-rating control

```ts
import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-rating',
  template: `
    <span *ngFor="let star of stars; let i = index"
          (click)="select(i + 1)"
          (blur)="onTouched()"
          [class.filled]="i < value"
          [class.disabled]="disabled">★</span>
  `,
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => RatingComponent),   // forwardRef: the class isn't defined yet
    multi: true,                                      // ⚠️ multi:true is mandatory
  }],
})
export class RatingComponent implements ControlValueAccessor {
  @Input() max = 5;
  get stars() { return Array(this.max); }

  value = 0;
  disabled = false;

  private onChange: (v: number) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(value: number): void { this.value = value ?? 0; }
  registerOnChange(fn: (v: number) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void { this.disabled = isDisabled; }

  select(v: number): void {
    if (this.disabled) return;
    this.value = v;
    this.onChange(v);      // ✅ tell the form
    this.onTouched();
  }
}
```

```html
<!-- now it's a first-class form control -->
<app-rating formControlName="rating" [max]="5"></app-rating>
```

**Three things that trip people up:**
1. **`multi: true`** — `NG_VALUE_ACCESSOR` is a multi-provider; without it you break every other accessor.
2. **`forwardRef`** — the class is referenced inside its own decorator, before it exists.
3. **`writeValue` must not call `onChange`** — that's an infinite loop.

### Bonus: make the component validate itself
```ts
providers: [
  { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => RatingComponent), multi: true },
  { provide: NG_VALIDATORS,     useExisting: forwardRef(() => RatingComponent), multi: true },
]
// ...
validate(control: AbstractControl): ValidationErrors | null {
  return this.value > 0 ? null : { ratingRequired: true };
}
```

---

# M — What happens? (18 binding & forms traps)

> 🔴 Same format as [23](./23-java-output-tricky-questions.md): **the code → what happens → the rule → the follow-up.**

---

## Q1. `setValue` with a missing control

```ts
form = this.fb.group({ name: [''], email: [''] });
this.form.setValue({ name: 'Priyanka' });
```
### ✅ What happens
**Runtime error:** `Must supply a value for form control with name: 'email'.`
### 📏 The rule
`setValue` requires the **exact** structure. `patchValue` would have worked.
### 🔁 Follow-up: *"And `patchValue({ nmae: 'typo' })`?"*
**No error, no effect** — unknown keys are silently ignored. That's the real danger of `patchValue`.

---

## Q2. Submitting a form with a disabled field

```ts
form = this.fb.group({
  name:  ['Priyanka'],
  email: [{ value: 'p@x.com', disabled: true }],
});
console.log(this.form.value);
```
### ✅ What happens
```
{ name: 'Priyanka' }
```
**`email` is missing.**
### 📏 The rule
`value` **excludes disabled controls**; `getRawValue()` includes them.
### 🔁 Follow-up: *"What bug does this cause in production?"*
You PATCH the API with `form.value` and the disabled field's column gets wiped, because the key wasn't sent.

---

## Q3. Is this form valid?

```ts
form = this.fb.group({ email: ['', Validators.required] });
this.form.get('email')!.disable();
console.log(this.form.valid);
```
### ✅ What happens
```
true
```
### 📏 The rule
**Disabled controls are excluded from validation entirely.** The form's status ignores them.
### 🔁 Follow-up: *"How do you make it read-only but still validated?"*
Use the `readonly` HTML attribute (or CSS `pointer-events`) instead of `disable()` — the control stays enabled in the form model.

---

## Q4. `[disabled]` with reactive forms

```html
<input formControlName="email" [disabled]="isReadOnly">
```
### ✅ What happens
Angular logs: *"It looks like you're using the disabled attribute with a reactive form directive"*, and the state can **desync** from the form model.
### 📏 The rule
With reactive forms the **model owns the disabled state** — call `control.disable()` / `.enable()` in the class.

---

## Q5. The infinite loop

```ts
this.form.valueChanges.subscribe(v => {
  this.form.patchValue({ total: v.price * v.qty });
});
```
### ✅ What happens
**Infinite loop** — the patch fires `valueChanges`, which patches again. The tab freezes / stack overflow.
### 📏 The rule
Pass **`{ emitEvent: false }`** to any programmatic update inside a `valueChanges` handler.
### 🔁 Follow-up: *"A cleaner way?"*
Derive `total` as a getter or a `computed()` signal instead of storing it in the form at all.

---

## Q6. Async validator that never completes

```ts
emailTaken(): AsyncValidatorFn {
  return c => this.api.checkEmail(c.value).pipe(map(t => t ? { taken: true } : null));
}
```
### ✅ What happens
If `checkEmail` returns a **Subject/long-lived Observable**, the control stays **`PENDING` forever** and the submit button never enables.
### 📏 The rule
An async validator's Observable **must complete**. Add `first()` or `take(1)`.
### 🔁 Follow-up: *"How do you disable submit correctly with async validators?"*
`[disabled]="form.invalid || form.pending"`.

---

## Q7. `setValidators` with no follow-up call

```ts
this.form.get('phone')!.setValidators(Validators.required);
console.log(this.form.get('phone')!.valid);   // phone is empty
```
### ✅ What happens
```
true
```
— the validator hasn't run yet.
### 📏 The rule
`setValidators` / `clearValidators` / `addValidators` only **register**. You must call **`updateValueAndValidity()`** to re-evaluate.

---

## Q8. `ExpressionChangedAfterItHasBeenCheckedError`

```ts
@Component({ template: `{{ label }}` })
export class C implements AfterViewInit {
  label = 'a';
  ngAfterViewInit() { this.label = 'b'; }
}
```
### ✅ What happens
**`ExpressionChangedAfterItHasBeenCheckedError`** — in **dev mode only**. In production the value just updates.
### 📏 The rule
Dev mode runs a second verification CD pass; changing a bound value after the view was checked breaks unidirectional data flow.
### 🔁 Follow-up: *"Fixes?"*
Move it to `ngOnInit`; `cdr.detectChanges()`; defer with `Promise.resolve().then(...)`. Best: fix the data flow.

---

## Q9. Mutating an array bound to an `OnPush` child

```ts
// parent
this.items.push(newItem);
```
```html
<app-list [items]="items"></app-list>   <!-- child is ChangeDetectionStrategy.OnPush -->
```
### ✅ What happens
**The child does not re-render**, and `ngOnChanges` does **not** fire.
### 📏 The rule
`OnPush` and `ngOnChanges` both compare by **reference**. Mutation keeps the same reference.
### 🔁 Follow-up: *"Fix?"*
`this.items = [...this.items, newItem];` — immutable update.

---

## Q10. `disabled="{{ flag }}"` where `flag === false`

```html
<button disabled="{{ isDisabled }}">Save</button>
```
### ✅ What happens
The button is **always disabled**, even when `isDisabled` is `false`.
### 📏 The rule
Interpolation produces the **string** `"false"`, and the presence of the `disabled` attribute is what disables a button. Use `[disabled]="isDisabled"`.

---

## Q11. `ngModel` + `formControlName` together

```html
<input formControlName="name" [(ngModel)]="name">
```
### ✅ What happens
**Runtime error** — `ngModel` cannot be used to register form controls with a parent `formGroup` directive. *(Deprecated in v6, removed in v11+.)*
### 📏 The rule
One control, one directive. Reactive **or** template-driven, never both.

---

## Q12. `{{ }}` on an object

```html
<p>{{ user }}</p>          <!-- user = { name: 'Priyanka' } -->
```
### ✅ What happens
```
[object Object]
```
### 📏 The rule
Interpolation stringifies. Use `{{ user | json }}` for debugging, or bind the property you actually want.

---

## Q13. `reset()` on a typed form

```ts
form = this.fb.group({ name: [''] });
this.form.reset();
console.log(this.form.value.name);
```
### ✅ What happens
```
null
```
— **not `''`**.
### 📏 The rule
`reset()` sets controls to `null`. Use `this.fb.nonNullable.control('')` to reset back to the **initial value** instead, which also removes `| null` from the type.

---

## Q14. Required checkbox

```ts
terms: [false, Validators.required]
```
### ✅ What happens
The form is invalid when unchecked — **but also stays invalid logic-wise for the wrong reason**, and any *falsy-but-valid* value (like `0` on a number field) is rejected too.
### 📏 The rule
For "must be ticked" use **`Validators.requiredTrue`**. `Validators.required` treats `false`, `''`, `null` and `[]` as empty.

---

## Q15. Removing a `FormArray` row inside a loop

```ts
for (let i = 0; i < this.phones.length; i++) {
  if (!this.phones.at(i).value.number) this.phones.removeAt(i);
}
```
### ✅ What happens
**Rows are skipped** — removing at `i` shifts everything left while `i` still increments.
### 📏 The rule
Iterate **backwards** (`for (let i = len - 1; i >= 0; i--)`) or collect indices first. *(Same class of bug as `ConcurrentModificationException` in Java — see [23](./23-java-output-tricky-questions.md).)*

---

## Q16. Cross-field error lookup

```ts
form = this.fb.group({ pwd: [''], confirm: [''] }, { validators: passwordsMatch });
```
```html
<div *ngIf="form.get('confirm')?.errors?.['passwordMismatch']">Mismatch</div>
```
### ✅ What happens
**The message never shows**, even when the passwords differ.
### 📏 The rule
A validator on the **group** puts the error on the **group**: `form.errors?.['passwordMismatch']`. To attach it to the control you'd have to call `setErrors` on the control yourself.

---

## Q17. Calling a method in a template

```html
<div *ngFor="let item of getFilteredItems()">{{ item.name }}</div>
```
### ✅ What happens
`getFilteredItems()` runs on **every change detection cycle** — often dozens of times per second on mouse move. And if it returns a new array each call inside a dev-mode-checked binding, you can also get `ExpressionChangedAfterItHasBeenCheckedError`.
### 📏 The rule
Templates re-evaluate every CD cycle. Precompute in the class, use a **pure pipe**, or a `computed()` signal.

---

## Q18. Two-way binding with the wrong output name

```ts
@Input() count = 0;
@Output() countChanged = new EventEmitter<number>();   // ← "Changed", not "Change"
```
```html
<app-counter [(count)]="total"></app-counter>
```
### ✅ What happens
**Compile error:** *"Can't bind to 'countChange' since it isn't a known property"* — or the value simply never flows back up.
### 📏 The rule
`[(x)]` desugars to `[x]` + `(xChange)`. The output must be named **exactly** `<input>Change`.

---

# N — Testing forms

Reactive forms are testable **without touching the DOM** — that's one of their selling points.

```ts
describe('UserFormComponent', () => {
  let component: UserFormComponent;
  let fixture: ComponentFixture<UserFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UserFormComponent],
      imports: [ReactiveFormsModule],
    }).compileComponents();

    fixture = TestBed.createComponent(UserFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('is invalid when empty', () => {
    expect(component.form.valid).toBeFalse();
  });

  it('requires a valid email', () => {
    const email = component.form.get('email')!;
    email.setValue('not-an-email');
    expect(email.hasError('email')).toBeTrue();
    email.setValue('p@x.com');
    expect(email.valid).toBeTrue();
  });

  it('flags mismatched passwords on the group', () => {
    component.form.patchValue({ pwd: 'abc123', confirm: 'xyz789' });
    expect(component.form.errors?.['passwordMismatch']).toBeTrue();
  });

  it('submits the raw value including disabled controls', () => {
    const spy = spyOn(component['api'], 'save').and.returnValue(of({}));
    component.form.patchValue({ name: 'Priyanka', email: 'p@x.com' });
    component.form.get('email')!.disable();
    component.onSubmit();
    expect(spy).toHaveBeenCalledWith(jasmine.objectContaining({ email: 'p@x.com' }));
  });
});
```

**Async validators:** use `fakeAsync` + `tick()`, and assert the `PENDING` → `VALID` transition.
**Template-driven forms** need `fixture.whenStable()` because `ngModel` updates asynchronously — another reason reactive forms are easier to test.

---

# O — Rapid-fire answer table

| Question | One-line answer |
|---|---|
| Types of binding? | Interpolation, property, event, two-way |
| Interpolation vs property binding? | Interpolation always stringifies; property binding passes real objects/booleans |
| `[attr.x]` vs `[x]`? | `[attr.]` for attributes with no DOM property — ARIA, `data-*`, SVG, `colspan` |
| How does `[(x)]` work? | Sugar for `[x]` + `(xChange)` |
| Custom two-way binding? | `@Input() x` + `@Output() xChange`; or `model()` in v17.2+ |
| `[class.x]` vs `ngClass`? | Binding syntax for one known class vs a directive for a dynamic set |
| React to an `@Input` change? | An input **setter**, or `ngOnChanges` when you need the previous value / multiple inputs |
| Why doesn't `ngOnChanges` fire? | The reference didn't change — you mutated instead of replacing |
| `ExpressionChangedAfterItHasBeenChecked`? | Dev-mode-only check; a bound value changed after the view was checked. Fix the data flow |
| TD vs Reactive forms? | Model in the template, async, simple forms **vs** model in the class, sync, typed, dynamic, testable |
| `FormControl`/`FormGroup`/`FormArray`? | One field / an object of controls / a list of controls — all extend `AbstractControl` |
| What is `FormBuilder`? | A helper that builds the same tree with less boilerplate: `fb.group({...})` |
| Typed forms? | Angular 14+; the value type is inferred. `Untyped*` is the migration escape hatch |
| Why is the value nullable? | `reset()` sets `null` — use `nonNullable` to reset to the initial value instead |
| `setValue` vs `patchValue`? | Strict full structure (throws) vs partial (unknown keys silently ignored) |
| `value` vs `getRawValue()`? | `value` **omits disabled controls**; `getRawValue()` includes them |
| Is a form with an invalid disabled control valid? | **Yes** — disabled controls are excluded from validation |
| What does a validator return? | `null` if valid, an error object if invalid |
| Cross-field validation? | A `ValidatorFn` on the **FormGroup**; the error lands on `form.errors` |
| Async validator rules? | Third argument, runs after sync validators pass, status is `PENDING`, **must complete** |
| Changed validators don't apply? | You forgot `updateValueAndValidity()` |
| Show all errors on submit? | `form.markAllAsTouched()` |
| Dynamic number of fields? | `FormArray` + `formArrayName` + `[formGroupName]="i"` |
| `ControlValueAccessor`? | The bridge that makes a custom component a form control — `writeValue`, `registerOnChange`, `registerOnTouched`, `setDisabledState`, provided via `NG_VALUE_ACCESSOR` with `multi: true` |
| `updateOn`? | `'change'` (default), `'blur'`, `'submit'` — controls when value/validation update |
| Form memory leak? | Unsubscribed `valueChanges` — use `takeUntil` / `takeUntilDestroyed()` / the `async` pipe |

---

# P — 12 self-test drills (answers hidden)

**1.** `form.setValue({ name: 'a' })` on a group with `name` and `email`. What happens?

<details><summary>👉 Answer</summary>

Throws: *Must supply a value for form control with name: 'email'*. `patchValue` would silently succeed.
</details>

**2.** You disabled `email` and submit `form.value`. What's in the payload?

<details><summary>👉 Answer</summary>

**No `email` key.** Disabled controls are excluded from `value` — use `getRawValue()`.
</details>

**3.** `[(size)]` on your component doesn't work. What did you name the output?

<details><summary>👉 Answer</summary>

Anything other than **`sizeChange`**. `[(x)]` desugars to `[x]` + `(xChange)`.
</details>

**4.** A validator returns `{ required: true }`. Is the control valid?

<details><summary>👉 Answer</summary>

**Invalid.** Validators return `null` for valid and an error object for invalid — the reverse of what most people guess.
</details>

**5.** Why can't you bind `[aria-label]`?

<details><summary>👉 Answer</summary>

There's no DOM **property** called `aria-label`. Use `[attr.aria-label]`.
</details>

**6.** Your submit button is permanently disabled with `form.invalid || form.pending`. What's the likely bug?

<details><summary>👉 Answer</summary>

An **async validator whose Observable never completes** — status stays `PENDING`. Add `first()` / `take(1)`.
</details>

**7.** You call `setValidators` on a control and nothing changes. Why?

<details><summary>👉 Answer</summary>

You didn't call **`updateValueAndValidity()`**.
</details>

**8.** Where does a `passwordsMatch` group validator put its error?

<details><summary>👉 Answer</summary>

On the **group** — `form.errors?.['passwordMismatch']`, not on `confirmPassword.errors`.
</details>

**9.** Name the four `ControlValueAccessor` methods.

<details><summary>👉 Answer</summary>

`writeValue`, `registerOnChange`, `registerOnTouched`, `setDisabledState`. Registered with `NG_VALUE_ACCESSOR`, `useExisting: forwardRef(...)`, **`multi: true`**.
</details>

**10.** `this.items.push(x)` and the `OnPush` child doesn't update. Fix?

<details><summary>👉 Answer</summary>

Replace the reference: `this.items = [...this.items, x]`. `OnPush` and `ngOnChanges` compare by reference.
</details>

**11.** How do you show validation errors for a form the user submitted without touching?

<details><summary>👉 Answer</summary>

`this.form.markAllAsTouched()` before returning from `onSubmit()`.
</details>

**12.** `Validators.required` on a terms-and-conditions checkbox — what's wrong?

<details><summary>👉 Answer</summary>

`false` counts as empty in a confusing way and the intent is unclear — use **`Validators.requiredTrue`**, which specifically requires `true`.
</details>

---

# Q — Answer scripts (say these out loud)

### 1. "Explain data binding in Angular." *(40 s)*
> *"There are four types. Interpolation and property binding go component to view — one-way. Event binding goes view to component. Two-way binding with the banana-in-a-box syntax is just those two combined: `[(ngModel)]` desugars to `[ngModel]` plus `(ngModelChange)`. That's also how I make my own components two-way bindable — an `@Input` called `x` and an `@Output` called `xChange`. The one distinction I'd add is property versus attribute binding: Angular binds to DOM **properties**, so for things with no property — ARIA, `data-*`, SVG, `colspan` — I use `[attr.]`."*

### 2. "Template-driven vs reactive forms — which do you use?" *(45 s)*
> *"Reactive, for anything beyond a login box. Template-driven builds the form model implicitly from directives in the template and updates asynchronously, which is fine for something tiny. Reactive forms create the model explicitly in the class, so the data flow is synchronous and immutable, I get typed forms from Angular 14, `FormArray` for dynamic fields, custom and cross-field validators as plain functions, and I can unit test the whole form without rendering the DOM. On EasyVisa the visa forms were reactive and rendered from a JSON config through Angular Formly — nested `fieldGroup`s and repeatable `fieldArray`s with conditional expressions."*

### 3. "`setValue` vs `patchValue`?" *(25 s)*
> *"`setValue` needs the exact structure — every control, no extras — and throws if it doesn't match, which is what I want when loading a record, because a renamed field fails loudly instead of silently. `patchValue` takes a subset and **ignores unknown keys without any error**, so I use it only for genuine partial updates. And when I submit, I use `getRawValue()` rather than `value`, because `value` drops disabled controls and that quietly wipes columns on a PATCH."*

### 4. "How do you do async validation?" *(40 s)*
> *"An `AsyncValidatorFn` in the third argument position, returning an Observable of `ValidationErrors | null`. I debounce it with a `timer` and `switchMap` so I'm not hitting the API on every keystroke, `catchError` to `of(null)` so an API outage doesn't block the user, and `first()` so the Observable **completes** — if it doesn't, the control stays `PENDING` forever and the submit button never enables. While it's in flight the status is `PENDING`, so the button binds to `form.invalid || form.pending`. Async validators only run after the sync ones pass, so I don't waste calls on a malformed email."*

### 5. "How would you build a form with a repeatable section?" *(40 s)*
> *"`FormArray`. I expose a typed getter that casts `form.get('phones')` to `FormArray`, a factory method that returns a `FormGroup` for one row, and `push`/`removeAt` for add and delete. In the template it's `formArrayName` on the wrapper, `*ngFor` over `array.controls`, and `[formGroupName]="i"` on each row — the index in brackets. Validity aggregates automatically, so the submit button just binds to `form.invalid`. The one bug I watch for is removing rows in a forward loop, which skips entries — I iterate backwards."*

### 6. "How do you make a custom component work with `formControlName`?" *(45 s)*
> *"Implement `ControlValueAccessor` and register it with the `NG_VALUE_ACCESSOR` token using `useExisting`, a `forwardRef` because the class isn't defined yet inside its own decorator, and `multi: true` because it's a multi-provider. Then four methods: `writeValue` for values Angular pushes in, `registerOnChange` where I store the callback and call it whenever my value changes, `registerOnTouched` which I call on blur so `touched` works, and `setDisabledState` so `.disable()` reaches my component. The trap is calling the onChange callback from inside `writeValue` — that's an infinite loop. If the component should validate itself, I also provide `NG_VALIDATORS` and implement `validate`."*

---

## ✅ Before the interview, make sure you can

- [ ] Name the **4 binding types** and say which direction each goes
- [ ] Explain **`[attr.x]` vs `[x]`** with a concrete example (ARIA, `colspan`, SVG)
- [ ] Desugar **`[(ngModel)]`** on the spot and write a custom two-way binding
- [ ] Explain **`ExpressionChangedAfterItHasBeenCheckedError`** and give 2 fixes
- [ ] Give the **template-driven vs reactive** table from memory
- [ ] Explain **`setValue` vs `patchValue`** *and* **`value` vs `getRawValue()`**
- [ ] Say why a form with an **invalid disabled control is `VALID`**
- [ ] Write a **custom validator**, a **cross-field validator**, and an **async validator** on paper
- [ ] Remember **`updateValueAndValidity()`** after changing validators
- [ ] Build a **`FormArray`** with add/remove in the class *and* the template
- [ ] Implement **`ControlValueAccessor`** — all 4 methods + `multi: true` + `forwardRef`
- [ ] Answer 14 of the 18 traps in [section M](#m--what-happens--18-binding--forms-traps) cold
- [ ] Attribute correctly: **Formly & NgRx = EasyVisa. RoboGebra = Angular 16.2 web + Angular 18.2 / Ionic 8 mobile**

---

**Related files:** [04 — Angular](./04-angular.md) · [20 — RxJS & Operators](./20-rxjs-operators.md) · [21 — NgRx](./21-ngrx.md) · [15 — Ionic Level 1](./15-ionic-level1.md) · [18 — RoboGebra Technical Stack](./18-robogebra-technical-versions.md)
