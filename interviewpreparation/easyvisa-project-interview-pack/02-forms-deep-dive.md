# 2. Forms Deep Dive (Angular Formly) — the EasyVisa signature file

**This is my strongest story. If I get one deep-dive question, I want it to be this one.**

**Stack:** Angular **reactive forms** as the foundation, **Angular Formly** for JSON-driven dynamic forms, `fieldGroup` for nesting, `fieldArray` for repeatable sections, `expressions` for conditional logic and dynamic validation, custom Formly field types for reuse.

> The headline: *Visa forms are huge, conditional, and repeatable. Hand-coding each one is unmaintainable. I made forms into JSON configuration instead of code — new forms and new conditional rules became data, not new components.*

---

## Q1. What is Angular Formly and why did you choose it?

**Answer.** Formly is a library that **renders Angular reactive forms from a JSON configuration** instead of hand-written template markup. You describe the fields — key, type, label, validators, conditional rules — as an array of `FormlyFieldConfig` objects, and Formly builds the underlying `FormGroup` and the UI for you.

I chose it because **visa forms are the worst case for hand-coding**:
- They're **huge** — dozens to hundreds of fields.
- They're **conditional** — fields appear/disappear based on earlier answers ("Are you employed?" → show employer fields).
- They're **repeatable** — a case can have N applicants, each with nested address and employment histories, each of which can itself repeat.
- They **change** — immigration rules and form requirements shift, and I didn't want a code release for every field tweak.

Hand-coding that means a giant, brittle template per form and a new component every time. With Formly, a new form or a new rule is a **config change**.

**The trade-off I'd volunteer:** steeper initial learning curve and some complexity writing custom field types, versus huge long-term savings. At EasyVisa's scale of forms, it clearly paid off. If I only had two or three simple forms, plain reactive forms would've been simpler and Formly would've been over-engineering.

---

## Q2. Reactive vs template-driven forms — which and why?

**Answer.**
- **Template-driven** — form logic lives in the template with `ngModel`; Angular builds the model implicitly. Fine for small, simple forms. Hard to test, hard to do dynamic/cross-field logic.
- **Reactive** — I build the form model explicitly in TypeScript (`FormGroup`/`FormControl`), so the **model is the source of truth**. It's typed, testable, synchronous to read, and supports dynamic controls and cross-field validation.

**In EasyVisa.** Reactive, without question — and Formly itself is built on reactive forms, so under the hood every Formly form *is* a `FormGroup`. Template-driven would never have handled nested, repeatable, conditional visa forms. Because the model was explicit and central, I could serialize the whole form to save a **draft** and rehydrate it later — the form's data and its state were one object.

---

## Q3. Show me a JSON-driven Formly form. How does the config map to UI?

**Answer.** Each field is a `FormlyFieldConfig`: `key` (where it lives in the model), `type` (which field component renders it), `props` (label, placeholder, required — formerly `templateOptions`), plus `validators` and `expressions`.

**In EasyVisa** — a simplified applicant section:

```typescript
const applicantFields: FormlyFieldConfig[] = [
  { key: 'firstName', type: 'input',
    props: { label: 'First name', required: true } },
  { key: 'dateOfBirth', type: 'datepicker',
    props: { label: 'Date of birth', required: true } },
  { key: 'hasEmployer', type: 'checkbox',
    props: { label: 'Currently employed?' } },
  { key: 'employerName', type: 'input',
    props: { label: 'Employer name' },
    expressions: { hide: 'model.hasEmployer === false' } },  // conditional visibility
];
```

```html
<form [formGroup]="form">
  <formly-form [form]="form" [fields]="applicantFields" [model]="model"></formly-form>
  <button [disabled]="form.invalid">Submit</button>
</form>
```

The `model` object *is* the form data; `form` is the underlying `FormGroup` Formly populates. Change the config array and the UI changes — no template edits.

---

## Q4. How did you nest forms? Explain `fieldGroup`.

**Answer.** `fieldGroup` creates a **nested group of fields** — it maps to a nested `FormGroup`. If I give it a `key`, the nested fields live under that key in the model; without a key it's a purely visual grouping.

**In EasyVisa.** An applicant had a nested **address** and **employment** structure, so I nested groups:

```typescript
{
  key: 'address',                     // nested FormGroup → model.address.*
  fieldGroup: [
    { key: 'line1', type: 'input',  props: { label: 'Street', required: true } },
    { key: 'city',  type: 'input',  props: { label: 'City',   required: true } },
    { key: 'country', type: 'select', props: {
        label: 'Country', required: true, options: this.countryOptions } },
  ]
}
```

This produced `model.address.line1`, `model.address.city`, etc. — clean, nested state that mirrored the real domain shape. Nested groups also let validation aggregate naturally (see Q7).

---

## Q5. Repeatable sections — explain `fieldArray`. This is the hard part.

**Answer.** `fieldArray` renders a **repeatable list of a field template** — it maps to a `FormArray`. Formly gives you add/remove for each entry, and each entry can itself be a `fieldGroup` with nested fields (and even nested `fieldArray`s). This is how I handled "N applicants, each with M previous addresses."

**In EasyVisa** — the standout structure, repeatable applicants each containing repeatable address history:

```typescript
const fields: FormlyFieldConfig[] = [{
  key: 'applicants',
  type: 'repeat',                     // my custom repeating-section field type (Q8)
  fieldArray: {
    fieldGroup: [
      { key: 'firstName', type: 'input',
        props: { label: 'First name', required: true } },
      { key: 'hasEmployer', type: 'checkbox',
        props: { label: 'Currently employed?' } },
      { key: 'employerName', type: 'input',
        props: { label: 'Employer' },
        expressions: { hide: 'model.hasEmployer === false' } },
      {
        key: 'addressHistory',
        type: 'repeat',               // nested repeatable INSIDE each applicant
        fieldArray: {
          fieldGroup: [
            { key: 'line1',   type: 'input', props: { label: 'Street', required: true } },
            { key: 'fromDate', type: 'datepicker', props: { label: 'From', required: true } },
            { key: 'toDate',   type: 'datepicker', props: { label: 'To' } },
          ]
        }
      }
    ]
  }
}];
```

This yields `model.applicants[i].addressHistory[j].line1` — arbitrarily nested, all from JSON. The **hard parts** were:
1. Keeping the nested reactive-form **state valid as sections were added/removed** — Formly rebuilds the underlying `FormArray`/`FormGroup`, and I had to make sure validation and conditional expressions re-evaluated correctly on each add/remove.
2. **Performance** with many repeated groups — I kept field types lean and avoided heavy work in expressions.
3. Making the **custom `repeat` field type** reusable so every repeatable section (applicants, addresses, dependents) used the same add/remove UI.

---

## Q6. How did the conditional show/hide and dynamic validation work?

**Answer.** Formly `expressions` — functions (or expression strings) evaluated against the current `model`/`field` that drive properties reactively. The common ones:
- `expressions: { hide: '...' }` — show/hide a field.
- `expressions: { 'props.required': '...' }` — make a field conditionally required.
- `expressions: { 'props.disabled': '...' }` — enable/disable.

When a hidden field is hidden, Formly also removes its control from validation, so a hidden "employer" field can't block submit.

**In EasyVisa** — conditional *and* dynamically required together:

```typescript
{
  key: 'employerName',
  type: 'input',
  props: { label: 'Employer name' },
  expressions: {
    hide: 'model.hasEmployer === false',
    'props.required': 'model.hasEmployer === true',   // required only when shown
  }
}
```

Function form when the rule was more complex (cross-field):

```typescript
{
  key: 'visaExpiryDate',
  type: 'datepicker',
  props: { label: 'Visa expiry' },
  expressions: {
    'props.required': (field) => field.model?.currentlyInCountry === true,
    hide: (field) => !field.model?.hasExistingVisa,
  }
}
```

This is the core of why Formly won: **conditional logic became configuration**. A new "show X when Y" rule was one line in JSON, not new component code and new tests.

---

## Q7. How did validation bubble up so the submit button disabled correctly?

**Answer.** Because Formly builds a real reactive-forms tree, validity **aggregates automatically**: a `FormArray` is invalid if any entry is invalid, a `FormGroup` is invalid if any child control is invalid, up to the root `FormGroup`. So binding `[disabled]="form.invalid"` on submit reflects the entire nested tree.

The subtlety with **dynamic/nested** forms is making sure:
- Hidden fields don't count (Formly removes their controls — handled).
- Newly added `fieldArray` entries register their validators immediately.
- Custom async/visa-specific rules surface their errors at the **right nesting level** so the attorney sees which applicant/section is invalid, not just "form invalid."

**In EasyVisa.** I relied on Formly's nested `FormGroup`/`FormArray` aggregation for the plumbing and wrote **custom validators** for visa-specific rules (date ordering, identifier formats, cross-field consistency). Then I surfaced errors at the correct level — an error message rendered inside the specific applicant's card, while the top-level submit stayed disabled until the whole tree was valid.

```typescript
// a custom validator wired into a Formly field
export function dateRangeValidator(control: AbstractControl): ValidationErrors | null {
  const { fromDate, toDate } = control.value ?? {};
  return fromDate && toDate && new Date(fromDate) > new Date(toDate)
    ? { dateRange: true } : null;
}

{ key: 'addressHistory', type: 'repeat',
  fieldArray: { validators: { validation: [dateRangeValidator] }, fieldGroup: [ /* ... */ ] } }
```

```html
<button type="submit" [disabled]="form.invalid">Submit case</button>
```

**One-liner to land it:** "Because it's reactive forms underneath, validity bubbles from every nested group and array up to the root, so a single `form.invalid` binding correctly gates a 200-field, conditionally-shown, repeatable form."

---

## Q8. You wrote custom Formly field types — why and how?

**Answer.** Formly ships basic types, but real apps need custom ones. A custom type is just an Angular component that extends `FieldType` and renders `field`/`formControl`. Registering it makes `type: 'my-type'` usable in any config.

**In EasyVisa.** My most valuable one was the **`repeat`** type powering every repeatable section — a consistent add/remove UI for applicants, address history, dependents, etc. I also wrapped specialized inputs (date pickers, the document/photo control tied to the cropper) as Formly types so forms could reference them by name:

```typescript
@Component({
  selector: 'formly-repeat-section',
  template: `
    <div *ngFor="let f of field.fieldGroup; let i = index" class="repeat-row">
      <formly-field [field]="f"></formly-field>
      <button type="button" (click)="remove(i)">Remove</button>
    </div>
    <button type="button" (click)="add()">Add {{ field.props?.addLabel }}</button>
  `
})
export class RepeatSectionType extends FieldArrayType {}
```

```typescript
// registered once, reused everywhere
FormlyModule.forRoot({
  types: [
    { name: 'repeat', component: RepeatSectionType },
    { name: 'datepicker', component: DatePickerType },
  ],
  validators: [{ name: 'dateRange', validation: dateRangeValidator }],
})
```

The payoff: a whole new repeatable, validated section was **JSON referencing existing types** — that's the reuse story that made new visa forms fast to build.

---

## Q9. How did you manage the form's state, drafts, and prefill?

**Answer.** With reactive/Formly forms the **`model` object is the single source of truth**. Prefill = set the model. Save draft = serialize the model. Restore = set the model back. No manual field-by-field wiring.

**In EasyVisa.** I kept the Formly `model` as one state object, so:
- **Prefill** an existing case = load the case JSON into `model`.
- **Save draft** = persist the `model` (attorneys filled these over multiple sessions).
- **Restore** = hydrate `model` and Formly rebuilt the exact form state, including repeated sections.

```typescript
saveDraft(): void {
  this.caseService.saveDraft(this.caseId, this.model)   // model IS the state
    .pipe(takeUntil(this.destroy$)).subscribe();
}
```

That "the model is the state" property is a direct benefit of choosing reactive forms + Formly, and it made a genuinely useful feature (multi-session draft editing) almost free.

---

## Q10. If the requirements/forms changed, how disruptive was it?

**Answer.** That's exactly what the design optimized for. Because forms were JSON config and conditional logic was `expressions`, most "changes" — add a field, make one conditionally required, add a new repeatable section — were **config edits reusing existing field types**, not new components. That kept mid-sprint form changes cheap.

**In EasyVisa.** When to change requirements, my honest "what I'd do differently" is I'd have the **form JSON served from the backend** rather than shipped in the frontend build — then non-developers could adjust forms without a release. We shipped configs in the app; making them backend-driven was the logical next step and would've fully realized the "forms as data" vision.

---

### Rapid-fire recap

| Concept | Formly mechanism | EasyVisa use |
|---------|------------------|--------------|
| JSON-driven form | `FormlyFieldConfig[]` | every visa form rendered from config |
| Nesting | `fieldGroup` (→ nested `FormGroup`) | applicant → address / employment |
| Repeatable | `fieldArray` (→ `FormArray`) | N applicants, each with N addresses (nested repeat) |
| Conditional show/hide | `expressions: { hide }` | employer fields shown only if employed |
| Dynamic required | `expressions: { 'props.required' }` | fields required only when visible |
| Validation bubbling | reactive-forms aggregation | `form.invalid` gates the submit button |
| Custom rules | custom validators | date ranges, identifier formats |
| Reuse | custom field types (`repeat`, pickers) | new section = JSON, not new code |
| State/drafts | `model` = single source of truth | prefill, multi-session draft save/restore |
| Foundation | reactive forms (not template-driven) | typed, testable, scales to nesting |
