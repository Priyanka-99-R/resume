# Angular — Interview Q&A (Easy Version)

> **Your core skill.** Every question follows the same shape —
> **Easiest way to remember → simple explanation → real-world example → diagram → interview trap → Interview-ready answer → Easy memory box.**
>
> 🔵 Wherever a topic appears in your own codebase, there's an **"In your RoboGebra code"** block with the real file. Full index: **[39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)**.

**Your real Angular stack:**

```
robogebra-mobile     → Angular 18.2 + Ionic 8.7 + Capacitor 6.2 + RxJS 7.8 + TS 5.4
robogebra-web        → Angular 16.2 + Material 16 + PrimeNG 16 + TS 5.1
robogebra-backoffice → Angular 16.2 + PrimeNG 16.9 + TS 4.9
```

---

## Contents

1. [Core Concepts](#core-concepts)
2. [Components & Templates](#components--templates)
3. [Component Communication](#component-communication)
4. [Services & Dependency Injection](#services--dependency-injection)
5. [Routing](#routing)
6. [Forms](#forms)
7. [RxJS & Observables](#rxjs--observables)
8. [State Management (NgRx)](#state-management-ngrx)
9. [HTTP](#http)
10. [Pipes](#pipes)
11. [Performance & Change Detection](#performance--change-detection)
12. [Testing](#testing)
13. [Migration & Versioning](#migration--versioning)
14. [Quick Revision Sheet](#quick-revision-sheet)

---

## Core Concepts

### Q: What is Angular and how is it different from AngularJS?

The easiest way to remember:

```
AngularJS (1.x) → JavaScript, $scope, two-way digest cycle → SLOW, rewritten
Angular  (2+)   → TypeScript, components, one-way data flow → a COMPLETE rewrite ⭐
```

They are **not** versions of the same thing. Angular 2 was written from scratch in 2016.

| | AngularJS (1.x) | Angular (2+) |
|---|---|---|
| Language | JavaScript | **TypeScript** |
| Building block | controllers + `$scope` | **components** |
| Data flow | two-way everywhere (digest cycle) | one-way down, events up ⭐ |
| DI | string-based, minification-fragile | hierarchical, type-based |
| Mobile | not designed for it | first-class (Ionic/Capacitor) |
| Performance | digest loop re-checks everything | Ivy + change detection strategies |

Real-world idea: **a house rebuilt on the same plot.** Same address, entirely new structure — you can't "upgrade" one into the other room by room.

#### Interview-ready answer

> Angular is a TypeScript-based front-end framework for building single-page applications, and it is a complete rewrite of AngularJS rather than a new version of it. AngularJS used controllers and `$scope` with a two-way digest cycle that re-checked everything and became a performance bottleneck. Angular is component-based with one-way data flow down and events up, a hierarchical type-based dependency injection system, and the Ivy compiler. It also ships with routing, HTTP, forms and testing built in, whereas React needs those assembled from separate libraries.

#### Easy memory

```
AngularJS = JS + $scope + digest cycle (slow)
Angular   = TypeScript + COMPONENTS + one-way flow ⭐ — a REWRITE, not an upgrade
```

---

### Q: What is a Single Page Application (SPA)?

The easiest way to remember:

```
SPA = the browser loads ONE HTML page, and JavaScript swaps the content.
      The page NEVER fully reloads. ⭐
```

```
TRADITIONAL (MPA)              SPA
──────────────────             ───
click a link                   click a link
   ↓                              ↓
FULL page request              only a JSON API call
   ↓                              ↓
white flash, full reload 🐢    the router swaps a component ⚡
   ↓                              ↓
everything re-downloads        only DATA moves
```

Real-world idea: a **flip-book vs a whiteboard.**

```
MPA → tear out the page and put in a whole new one every time
SPA → wipe one section of the whiteboard and rewrite it ✅
```

Trade-off worth naming:

```
✅ Fast navigation, app-like feel, less bandwidth after the first load
❌ A big FIRST load, and SEO needs SSR (Angular Universal)
```

#### Easy memory

```
SPA = ONE page, JS swaps the content, no full reload ⭐
✅ fast after load | ❌ heavy first load + SEO needs SSR
```

---

### Q: What is a Component in Angular?

The easiest way to remember:

```
Component = TEMPLATE (HTML) + CLASS (TypeScript) + STYLES (CSS)
            → one self-contained piece of the screen.
```

```typescript
@Component({
  selector: 'app-user-card',              // the HTML tag you write
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.scss']
})
export class UserCardComponent {
  @Input() user!: User;                    // data IN  ⬇️
  @Output() selected = new EventEmitter<User>();   // event OUT ⬆️
}
```

```html
<app-user-card [user]="currentUser" (selected)="onSelect($event)"></app-user-card>
```

```
      ┌──────────────────────────┐
      │   UserCardComponent      │
      │  ┌────────────────────┐  │
 IN ──┼─▶│ @Input()  user     │  │
      │  ├────────────────────┤  │
      │  │ template + styles  │  │
      │  ├────────────────────┤  │
 OUT ◀┼──│ @Output() selected │  │
      │  └────────────────────┘  │
      └──────────────────────────┘
```

Real-world idea: a **LEGO brick.** It has a fixed shape, plugs into others in defined ways, and can be reused anywhere.

#### 🔵 In your RoboGebra code

```
robogebra-mobile → 163 components
   @Input()  → 252 usages
   @Output() → 69 usages
```

#### Easy memory

```
Component = template + class + styles → one UI piece 🧱
@Input()  = data DOWN ⬇️ [prop]="value"
@Output() = events UP ⬆️ (event)="handler($event)"
```

---

### Q: What is an NgModule?

The easiest way to remember:

```
NgModule = a BOX that groups related components, directives, pipes and services,
           and tells Angular how they fit together.
```

```typescript
@NgModule({
  declarations: [AppComponent, UserCardComponent],   // what BELONGS to this module
  imports:      [BrowserModule, HttpClientModule],   // other modules I NEED
  providers:    [UserService],                       // services available here
  exports:      [UserCardComponent],                 // what OTHERS may use
  bootstrap:    [AppComponent]                       // the root component (root module only)
})
export class AppModule { }
```

⭐ The four words, in one line each:

```
declarations → MINE      (components/directives/pipes I own)
imports      → BORROWED  (other modules whose exports I use)
providers    → SERVICES  (registered with the injector)
exports      → SHARED    (what I let other modules use)
```

Real-world idea: a **department in a company.**

```
declarations → the staff who work in this department
imports      → other departments you collaborate with
exports      → the staff you lend out to other departments
providers    → the shared equipment
```

⚠️ **The #1 NgModule error:**

```
"'app-user-card' is not a known element"
      ↓
The component is declared, but NOT EXPORTED from its module,
or its module isn't IMPORTED where you're using it. ⭐
```

#### Easy memory

```
declarations = MINE | imports = BORROWED | exports = SHARED | providers = SERVICES

"not a known element" → you forgot to EXPORT it, or to IMPORT its module ⭐
```

---

### Q: What are standalone components and why do they matter?

The easiest way to remember:

```
Standalone component = a component that declares its OWN imports
                       and needs NO NgModule. ⭐
```

```typescript
@Component({
  standalone: true,                                  // ⭐
  selector: 'app-user-card',
  imports: [CommonModule, RouterLink],               // its OWN dependencies
  template: `<a [routerLink]="['/user', user.id]">{{ user.name }}</a>`
})
export class UserCardComponent { }
```

```
BEFORE (NgModule)                    AFTER (standalone)
─────────────────                    ──────────────────
component.ts                         component.ts  ← declares its own imports ✅
+ module.ts       ← boilerplate 😩
+ remember to declare it
+ remember to export it
+ remember to import the module
```

Bootstrapping without any module at all:

```typescript
bootstrapApplication(AppComponent, {
  providers: [provideRouter(routes), provideHttpClient()]
});
```

Real-world idea: **a freelancer vs an employee.** A freelancer brings their own laptop and tools and can be dropped into any project; an employee needs the department to provide everything.

#### 🔵 In your RoboGebra code — be honest here ⭐

```
robogebra-mobile is Angular 18.2, but still largely NgModule-based:
   standalone: true      →  2 components   (migration started)
   inject()              → 30 usages       (adopted ✅)
   takeUntilDestroyed()  →  5 usages       (adopted ✅)
   signal()              →  0              ⚠️ not adopted
```

> 🗣️ *"We're on Angular 18 but the app is still mostly NgModule-based — it grew from Angular 12, and a big-bang standalone migration was never worth the regression risk on a shipping product. We've adopted the pieces that are cheap and safe: `inject()` in new code, `takeUntilDestroyed()` for teardown, and standalone for new components. New work is standalone-first; the legacy modules get converted when we're already touching them."*

⭐ **That is the right answer.** Claiming full adoption invites one follow-up you can't back up. Describing a *staged migration policy* is what a lead is expected to have.

#### Easy memory

```
Standalone = no NgModule; the component declares its OWN imports ⭐
bootstrapApplication() + provideRouter() + provideHttpClient()
Default for new components since Angular 17; the DEFAULT in v19

Your answer: "New code is standalone-first; legacy modules convert when touched." ⭐
```

---

### Q: What are decorators in Angular?

The easiest way to remember:

```
A decorator = a function starting with @ that attaches METADATA to a class,
              telling Angular what that class IS and how to use it.
```

```
@Component  → "this is a UI piece"        (has a template)
@Directive  → "this changes an element"   (no template)
@Pipe       → "this transforms a value"   ({{ x | myPipe }})
@Injectable → "this can be injected"      (a service)
@NgModule   → "this groups things"
@Input / @Output → property metadata
```

```typescript
@Component({ selector: 'app-x', template: '...' })
class X { }
//  ↑ without the decorator this is just a plain TypeScript class —
//    Angular would have no idea what to do with it
```

Real-world idea: a **label on a box.** The box is the same; the label ("FRAGILE", "PERISHABLE") tells the handler how to treat it.

#### Easy memory

```
Decorator = @metadata attached to a class
@Component (has a template) | @Directive (no template) | @Pipe | @Injectable | @NgModule
Without it, Angular sees only a plain class.
```

---

### Q: What is the Angular CLI and which commands do you use most?

```bash
ng new my-app --standalone --routing --style=scss
ng serve                      # dev server with live reload
ng generate component user    # or: ng g c user
ng g s core/services/auth     # service
ng g guard core/guard/auth    # guard
ng g pipe shared/pipes/safe   # pipe
ng build --configuration production
ng test                       # Karma + Jasmine
ng update @angular/core @angular/cli    # ⭐ runs migration SCHEMATICS
ng add @angular/material      # installs AND configures
```

⭐ The two worth explaining:

```
ng update → not just a version bump; it runs SCHEMATICS that
            AUTOMATICALLY REWRITE your code for breaking changes ⭐
ng add    → not just npm install; it also runs the library's setup schematic
            (imports the module, adds styles, updates angular.json)
```

#### Easy memory

```
ng new | ng serve | ng g c | ng build --configuration production | ng test
ng update ⭐ = version bump + automatic code MIGRATIONS (schematics)
ng add    ⭐ = install + configure (not just npm install)
```

---

### Q: Describe a typical Angular project structure.

The structure your own app uses — quote it, it's a good one:

```
src/app/
├── core/                 ← singletons, loaded ONCE
│   ├── guard/            ← route guards
│   ├── interceptors/     ← HTTP interceptors
│   ├── resolvers/        ← route data pre-fetch
│   ├── services/         ← app-wide services
│   ├── model/            ← interfaces / DTOs
│   └── config/
├── features/             ← one folder PER FEATURE (lazy-loadable)
│   └── pages/
│       ├── auth/  books/  quiz/  study-list/  settings/ ...
└── shared/               ← reusable dumb components, pipes, directives
    ├── components/  header/  footer/  modal/  side-menu/ ...
```

```
core/    → things you want EXACTLY ONE of      (services, interceptors, guards)
features/→ things a USER navigates to           (lazy loaded)
shared/  → things you REUSE everywhere          (dumb components, pipes)
```

⭐ The rule that keeps it clean: **`core` is imported once by the root module; `shared` is imported by many feature modules.** If a service ends up in `shared` and gets provided per-module, you accidentally create multiple instances.

#### 🔵 In your RoboGebra code

This is exactly `robogebra-mobile/src/app/` — with 89 services, 163 components, 27 resolvers, 4 guards and 3 interceptors.

#### Easy memory

```
core/ = ONCE (services, guards, interceptors, resolvers)
features/ = per-feature, LAZY loaded
shared/ = reused dumb components + pipes

⚠️ Never provide a singleton service from `shared` — you'll get multiple instances.
```

---

## Components & Templates

### Q: Explain all the Angular lifecycle hooks in order.

⭐ **This has now come up in 5 separate rounds** ([26 — Companies](./26-companies-asked-questions.md)). Know the order cold.

The easiest way to remember — **the order is the life of the component**:

```
constructor          → the class is created (NO inputs yet ⚠️)
ngOnChanges          → an @Input changed (runs BEFORE ngOnInit, and on every change)
ngOnInit             → inputs are ready → DO YOUR SETUP HERE ⭐
ngDoCheck            → every change detection run (expensive ⚠️)
ngAfterContentInit   → projected <ng-content> is ready
ngAfterContentChecked
ngAfterViewInit      → the template + @ViewChild are ready ⭐
ngAfterViewChecked
ngOnDestroy          → CLEAN UP HERE ⭐ (unsubscribe!)
```

```
       constructor
            ↓
      ngOnChanges  ←──────────┐ (every @Input change)
            ↓                  │
        ngOnInit  ⭐ once      │
            ↓                  │
        ngDoCheck ─────────────┘
            ↓
   ngAfterContentInit → ngAfterContentChecked
            ↓
   ngAfterViewInit ⭐ → ngAfterViewChecked
            ↓
        ngOnDestroy ⭐
```

| Hook | When | Use it for |
|---|---|---|
| `ngOnChanges` | an `@Input` changes | react to input changes |
| `ngOnInit` ⭐ | once, after the first `ngOnChanges` | **initial data fetch, subscriptions** |
| `ngDoCheck` | every CD cycle | custom change detection (rare, costly) |
| `ngAfterContentInit` | after `<ng-content>` projection | access `@ContentChild` |
| `ngAfterViewInit` ⭐ | after the view + children render | access `@ViewChild`, DOM measurement |
| `ngOnDestroy` ⭐ | just before destruction | **unsubscribe, clear timers, remove listeners** |

Real-world idea: **a person's life.**

```
constructor       → born
ngOnInit          → school starts — now you can actually do things ⭐
ngAfterViewInit   → you can see and use everything around you
ngOnDestroy       → handing back your ID card on the last day ⭐
```

#### The three most-asked follow-ups

**1. Why `ngOnInit` and not the constructor?**

```
constructor → the class exists, but @Input values are NOT set yet ⚠️
              → this.user is undefined 💥

ngOnInit    → Angular has set every @Input ✅ → safe to use them
```

Also: the constructor's job is dependency injection only. Doing HTTP there makes the class impossible to test without side effects.

**2. `ngAfterViewInit` + updating a value = `ExpressionChangedAfterItHasBeenCheckedError`**

```
Angular already checked the view.
You change a bound value in ngAfterViewInit.
      → in dev mode Angular re-checks and sees a DIFFERENT value → throws ⭐
Fix: setTimeout(), or cdr.detectChanges()
```

**3. What must go in `ngOnDestroy`?**

```
✅ unsubscribe manual subscriptions (or use takeUntil / async pipe)
✅ clearInterval / clearTimeout
✅ remove window / document event listeners
✅ complete any Subjects you own
```

#### 🔵 In your RoboGebra code

```
ngOnDestroy   → 100 components
takeUntil($destroy) → 426 usages ⭐
```

#### Easy memory

```
constructor → ngOnChanges → ngOnInit ⭐ → ngDoCheck
→ AfterContentInit/Checked → AfterViewInit ⭐/Checked → ngOnDestroy ⭐

@Inputs are NOT ready in the constructor → setup goes in ngOnInit ⭐
@ViewChild is only ready in ngAfterViewInit ⭐
Cleanup ALWAYS in ngOnDestroy ⭐
Changing a bound value in AfterViewInit → ExpressionChangedAfterItHasBeenChecked 💥
```

---

### Q: What are the types of data binding in Angular?

The easiest way to remember — **by the brackets**:

```
{{ value }}          INTERPOLATION      → class ➜ template
[prop]="value"       PROPERTY binding   → class ➜ template
(event)="handler()"  EVENT binding      → template ➜ class
[(ngModel)]="value"  TWO-WAY            → both directions ⭐
```

```
        CLASS                        TEMPLATE
          │  {{ }}  and  [ ]   ──────────▶   (data flows DOWN)
          │
          ◀──────────    ( )   ──────────    (events flow UP)

        [( )]  = both = "banana in a box" 🍌📦
```

```html
<h1>{{ title }}</h1>                          <!-- interpolation -->
<img [src]="imageUrl" [alt]="title">          <!-- property -->
<button (click)="save()">Save</button>        <!-- event -->
<input [(ngModel)]="name">                    <!-- two-way -->
<div [class.active]="isActive">               <!-- class binding -->
<div [style.color]="color">                   <!-- style binding -->
<app-child [user]="user" (selected)="onSel($event)">  <!-- component I/O -->
```

⭐ The memory hook for `[(ngModel)]`: **"banana in a box"** — the parentheses look like a banana inside the square box.

#### Easy memory

```
{{ }}  interpolation  ⬇️
[ ]    property       ⬇️
( )    event          ⬆️
[( )]  two-way        ⬇️⬆️  "banana in a box" 🍌📦
```

---

### Q: How does two-way binding actually work under the hood?

The easiest way to remember:

```
[(ngModel)] is NOT magic. It's just [ngModel] + (ngModelChange) in one shorthand. ⭐
```

```html
<!-- what you write -->
<input [(ngModel)]="name">

<!-- what it actually means -->
<input [ngModel]="name" (ngModelChange)="name = $event">
```

```
   name  ──[ngModel]──▶  the input's value      (down)
   name  ◀─(ngModelChange)──  the user types    (up)
```

⭐ And it works on **your own components** too — the naming convention is the trick:

```typescript
@Input()  value!: string;
@Output() valueChange = new EventEmitter<string>();   // ⭐ MUST be <input-name> + "Change"
```

```html
<app-counter [(value)]="count"></app-counter>   <!-- now works! -->
```

> **The rule:** an `@Input` called `x` plus an `@Output` called `xChange` gives you `[(x)]` for free.

#### Easy memory

```
[(ngModel)] = [ngModel] + (ngModelChange)  — just a shorthand ⭐

Custom two-way: @Input() value + @Output() valueChange
                → the "Change" suffix is what makes [(value)] work ⭐
Needs FormsModule for ngModel.
```

---

### Q: What are template reference variables?

```
#name = a variable that points at an ELEMENT or COMPONENT in the template.
```

```html
<input #phoneInput type="text">
<button (click)="call(phoneInput.value)">Call</button>

<app-child #child></app-child>
<button (click)="child.reset()">Reset</button>     <!-- call a child's method ⭐ -->

<video #player></video>
<button (click)="player.play()">Play</button>
```

```
#var on an HTML element  → the DOM element
#var on a component      → the COMPONENT INSTANCE ⭐
```

⚠️ Scope: a template reference variable is only visible **inside its own template**, and not inside a different `*ngIf`/`*ngFor` embedded view.

#### Easy memory

```
#name → element (or COMPONENT INSTANCE) reference, usable in the template only
<input #x> then x.value | <app-child #c> then c.method() ⭐
```

---

### Q: `@ViewChild` vs `@ContentChild`?

The easiest way to remember:

```
@ViewChild    → something in MY OWN template          ← ready in ngAfterViewInit ⭐
@ContentChild → something PROJECTED INTO me via <ng-content> ← ngAfterContentInit ⭐
```

```html
<!-- parent.component.html -->
<app-card>
    <app-badge></app-badge>      ← PROJECTED content → @ContentChild in AppCard
</app-card>

<!-- card.component.html -->
<div class="card">
    <h2 #title></h2>             ← CardComponent's OWN view → @ViewChild
    <ng-content></ng-content>
</div>
```

```typescript
@ViewChild('title') title!: ElementRef;             // my own template
@ContentChild(BadgeComponent) badge!: BadgeComponent; // projected in

@ViewChildren(ItemComponent) items!: QueryList<ItemComponent>;   // many
```

```
VIEW    = what I wrote in MY template        → ngAfterViewInit
CONTENT = what the PARENT put inside my tags → ngAfterContentInit ⭐
         (content is checked FIRST, then view)
```

⚠️ `{ static: true }` vs `{ static: false }`:

```
static: true  → available in ngOnInit, but ONLY if the element is not inside *ngIf ⭐
static: false → (default) available in ngAfterViewInit
```

#### Easy memory

```
@ViewChild    → MY template     → ngAfterViewInit ⭐
@ContentChild → PROJECTED in    → ngAfterContentInit ⭐
Content is resolved BEFORE view.
static: true → usable in ngOnInit (only if not inside *ngIf)
```

---

### Q: What is `ng-content` and content projection?

```
Content projection = let the PARENT decide what goes INSIDE your component.
                     (React calls this `children`.)
```

```html
<!-- card.component.html -->
<div class="card">
  <div class="card-header"><ng-content select="[header]"></ng-content></div>
  <div class="card-body">  <ng-content></ng-content></div>
  <div class="card-footer"><ng-content select="[footer]"></ng-content></div>
</div>
```

```html
<!-- usage -->
<app-card>
  <h2 header>Title</h2>
  <p>Any body content the parent wants</p>
  <button footer>OK</button>
</app-card>
```

```
Without projection → the card must know about EVERY possible body 😩
With projection    → the card owns the FRAME; the parent owns the CONTENT ✅
```

Real-world idea: a **picture frame.** The frame doesn't care what picture goes in it.

#### Easy memory

```
<ng-content> = a SLOT the parent fills 🖼️
select="[header]" → multi-slot projection
Projected content is queried with @ContentChild, ready in ngAfterContentInit ⭐
```

---

### Q: `ng-template` vs `ng-container`?

```
ng-template  → a template that renders NOTHING until something uses it ⭐
ng-container → an invisible GROUPING tag; renders its children, but no element
```

```html
<!-- ng-template — NOT rendered by itself -->
<ng-template #loading>
  <app-spinner></app-spinner>
</ng-template>

<div *ngIf="data; else loading">{{ data }}</div>     <!-- ⭐ used via `else` -->
```

```html
<!-- ng-container — avoids a useless wrapper <div> -->
<ng-container *ngIf="user">
  <h2>{{ user.name }}</h2>
  <p>{{ user.email }}</p>
</ng-container>
<!-- output: just the h2 and p — NO extra div ⭐ -->
```

⭐ The other big use — **two structural directives on one element is illegal**:

```html
<div *ngIf="show" *ngFor="let x of list">   <!-- ❌ compile error -->

<ng-container *ngIf="show">                  <!-- ✅ -->
  <div *ngFor="let x of list">{{ x }}</div>
</ng-container>
```

#### Easy memory

```
ng-template  → defined but NOT rendered until referenced (*ngIf...else) ⭐
ng-container → groups elements with NO extra DOM node ⭐
              → also how you combine *ngIf and *ngFor
```

---

### Q: Structural vs attribute directives?

```
STRUCTURAL → changes the DOM STRUCTURE (adds/removes elements) → prefixed with *
ATTRIBUTE  → changes the APPEARANCE or BEHAVIOUR of an existing element
```

```
Structural: *ngIf  *ngFor  *ngSwitchCase   ← the * is sugar for <ng-template>
Attribute : ngClass  ngStyle  ngModel      ← no *
```

```html
<!-- what you write -->
<div *ngIf="show">Hello</div>

<!-- what Angular actually creates ⭐ -->
<ng-template [ngIf]="show">
  <div>Hello</div>
</ng-template>
```

⭐ That desugaring explains why you can't put two structural directives on one element — there'd be two competing `<ng-template>` wrappers.

Custom attribute directive:

```typescript
@Directive({ selector: '[appHighlight]' })
export class HighlightDirective {
  @HostListener('mouseenter') onEnter() { this.el.nativeElement.style.background = 'yellow'; }
  constructor(private el: ElementRef) {}
}
```

#### Easy memory

```
STRUCTURAL (*) → adds/removes DOM → *ngIf *ngFor *ngSwitchCase
   the * is SUGAR for <ng-template> ⭐ → which is why two on one element fails
ATTRIBUTE      → changes look/behaviour → ngClass ngStyle ngModel
```

---

### Q: Explain `*ngIf`, `*ngFor` and `trackBy`.

```html
<div *ngIf="user; else noUser">{{ user.name }}</div>
<ng-template #noUser>No user</ng-template>

<li *ngFor="let item of items; let i = index; trackBy: trackById">
  {{ i }} — {{ item.name }}
</li>
```

```typescript
trackById(index: number, item: Item): number {
  return item.id;                 // ⭐ a STABLE identity
}
```

#### Why `trackBy` matters — the whole point ⭐

```
WITHOUT trackBy:
   the array reference changes → Angular assumes EVERYTHING is new
   → it DESTROYS every DOM node and rebuilds them 💥
   → you lose focus, scroll position, and animations, on every refresh

WITH trackBy:
   Angular matches items by your key → reuses the unchanged DOM nodes ✅
   → only the genuinely changed rows re-render ⚡
```

```
1000-row list refreshed by polling:
   without trackBy → 1000 DOM nodes destroyed + recreated, every poll 🐢
   with trackBy    → 0 nodes touched if nothing changed ⚡
```

`*ngFor` local variables worth knowing:

```
index, first, last, even, odd
```

#### 🔵 In your RoboGebra code

```
trackBy → 55 usages in robogebra-mobile ⭐
```

> 🗣️ *"We use `trackBy` on every list that refreshes — the chapter progress list and the study-material results especially. Without it, a poll or a filter change re-creates every row, which loses scroll position and makes the list flicker."*

#### Easy memory

```
*ngIf="x; else tpl" + <ng-template #tpl>
*ngFor="let i of list; let idx = index; trackBy: trackById"

trackBy = give each row a STABLE id → Angular REUSES DOM nodes ⭐
Without it: the whole list is destroyed and rebuilt on every change 💥
```

---

### Q: `ngClass` vs `ngStyle`?

```
ngClass → applies CSS CLASSES
ngStyle → applies INLINE STYLES
```

```html
<div [ngClass]="{ active: isActive, disabled: isDisabled }"></div>
<div [ngStyle]="{ color: textColor, 'font-size.px': size }"></div>

<!-- single-value shortcuts (preferred — faster) ⭐ -->
<div [class.active]="isActive"></div>
<div [style.color]="textColor"></div>
```

⭐ Prefer classes over inline styles: classes are cacheable, themeable and CSP-friendly.

#### Easy memory

```
ngClass → CSS classes  | [class.active]="x"  (single)
ngStyle → inline style | [style.color]="x"   (single)
Prefer CLASSES — themeable and cacheable ⭐
```

---

### Q: In Angular 17+, what are the new control flow blocks?

```
*ngIf     →  @if / @else if / @else
*ngFor    →  @for (…; track …)     ← `track` is MANDATORY ⭐
*ngSwitch →  @switch / @case / @default
new       →  @defer                ← LAZY-LOAD part of a template ⭐
```

```html
@if (user) {
  <p>{{ user.name }}</p>
} @else {
  <p>Loading…</p>
}

@for (item of items; track item.id) {          <!-- track is REQUIRED ⭐ -->
  <li>{{ item.name }}</li>
} @empty {
  <li>No items</li>                            <!-- ⭐ built-in empty state -->
}

@defer (on viewport) {                          <!-- loads only when scrolled to ⭐ -->
  <app-heavy-chart />
} @placeholder {
  <div class="skeleton"></div>
}
```

Why it's better:

```
✅ No import needed (CommonModule isn't required)
✅ `track` is MANDATORY → the trackBy performance bug becomes impossible ⭐
✅ Built-in @empty
✅ Better type narrowing
✅ @defer gives you lazy loading at the TEMPLATE level, not just the route level ⭐
```

⚠️ Be honest: your apps are Angular 16/18 and mostly still use `*ngIf` / `*ngFor`. Say you know the new syntax and would adopt it in new templates.

#### Easy memory

```
@if / @else | @for (…; track x) ⭐ MANDATORY track | @switch | @empty
@defer (on viewport | on interaction | on idle) ⭐ = lazy load PART of a template
No CommonModule import needed.
```

---

## Component Communication

### Q: How do parent and child components communicate?

The easiest way to remember:

```
Parent → Child  : @Input()   ⬇️  data down
Child  → Parent : @Output()  ⬆️  events up
```

```typescript
// child
@Input()  user!: User;
@Output() selected = new EventEmitter<User>();

select() { this.selected.emit(this.user); }
```

```html
<!-- parent -->
<app-child [user]="currentUser" (selected)="onSelected($event)"></app-child>
```

```
       PARENT
    [user]  │  ▲ (selected)
      data  ▼  │  event
       CHILD
```

Other (weaker) options:

```
@ViewChild → the parent calls a child's method directly (tight coupling ⚠️)
a SERVICE  → for anything not directly related ⭐ (see the next question)
```

⭐ The rule: **data down, events up.** A child should never mutate an object the parent owns — emit and let the parent decide.

#### Easy memory

```
@Input() ⬇️ data down | @Output() + EventEmitter ⬆️ events up
"Data down, events up" ⭐
@ViewChild for direct access (tighter coupling)
```

---

### Q: How do sibling components communicate?

```
Siblings CANNOT talk directly.

Option 1 → through the shared PARENT (@Output up, then @Input down)
Option 2 → through a SHARED SERVICE with a Subject ⭐ (the real answer)
```

```typescript
@Injectable({ providedIn: 'root' })
export class MessageService {
  private messageSubject$ = new BehaviorSubject<string>('');
  message$ = this.messageSubject$.asObservable();      // ⭐ read-only outside

  send(msg: string) { this.messageSubject$.next(msg); }
}
```

```
   SIBLING A ──send()──▶ ┌─────────────────┐
                          │ MessageService  │ (a root singleton)
   SIBLING B ◀─message$── └─────────────────┘
```

Real-world idea: two colleagues who don't share a manager use a **shared noticeboard**, not a chain of messages up and back down.

#### 🔵 In your RoboGebra code

This is the dominant pattern in your app — **122 `BehaviorSubject` usages.**

**File:** `core/services/user-summary.service.ts`

```typescript
@Injectable({ providedIn: 'root' })
export class UserSummaryService {
    private userSummarySubject$ = new BehaviorSubject<UserSummaryGroupDTO>(null);

    getUserSummarySubject(): Observable<UserSummaryGroupDTO> {
        return this.userSummarySubject$.asObservable();   // ⭐ expose READ-ONLY
    }
    getUserSummary(): UserSummaryGroupDTO {
        return this.userSummarySubject$.getValue();       // ⭐ synchronous read
    }
}
```

> 🗣️ *"The subject is kept private and only `asObservable()` is exposed, so no component can call `.next()` and mutate global state from anywhere. We also expose a synchronous `getValue()` accessor, because the institute-code HTTP interceptor needs the value without subscribing."*

#### Easy memory

```
Siblings → a SHARED SERVICE with a BehaviorSubject ⭐
Keep the subject PRIVATE; expose asObservable() ⭐
BehaviorSubject because a late subscriber still needs the CURRENT value.
```

---

## Services & Dependency Injection

### Q: What is dependency injection in Angular?

```
DI = you DECLARE what you need; Angular CREATES it and HANDS it to you.
     You never write `new`. ⭐
```

```typescript
@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private http: HttpClient) {}          // ⭐ injected, not `new`
}

@Component({ ... })
export class UserComponent {
  constructor(private userService: UserService) {}  // ⭐ injected
}

// modern alternative (Angular 14+)
private userService = inject(UserService);
```

```
Angular's INJECTOR
      │ creates HttpClient
      │ creates UserService(HttpClient)
      │ creates UserComponent(UserService)
      ↓
Everything wired automatically ✅
```

Why it matters:

```
✅ Testability — inject a mock instead of the real service
✅ Singletons  — one HTTP client, one auth state
✅ Loose coupling — swap the implementation without touching the consumer
```

#### 🔵 In your RoboGebra code

`inject()` is used **30 times** in `robogebra-mobile` — modern DI adopted alongside constructor injection.

#### Easy memory

```
DI = declare it in the constructor (or inject()), Angular provides it ⭐
@Injectable({ providedIn: 'root' }) = an app-wide, tree-shakable singleton
Benefit: testability (inject mocks) + singletons + loose coupling
```

---

### Q: `providedIn: 'root'` vs module vs component?

```
providedIn: 'root'   → ONE instance app-wide ⭐ + TREE-SHAKABLE (removed if unused)
providers: [] in a module    → one instance per module injector
providers: [] in a component → a NEW instance per component instance ⭐
```

```typescript
@Injectable({ providedIn: 'root' })     // singleton, tree-shakable ⭐
export class AuthService {}

@Component({
  providers: [DrawerStateService]        // ⭐ each component gets its OWN
})
export class DrawerComponent {}
```

```
root      → 1 instance for the whole app          (auth, config, cache)
component → 1 instance PER COMPONENT INSTANCE     (per-widget state)
```

⚠️ **The lazy-loading trap:**

```
A service provided in a LAZY-LOADED module gets its OWN instance,
separate from the root one 💥
   → two "singletons", two different states
Fix: providedIn: 'root' ⭐
```

#### Easy memory

```
providedIn: 'root' ⭐ → app-wide singleton + tree-shakable → the default choice
component providers   → a fresh instance per component (per-widget state)
⚠️ a service provided in a LAZY module = a SECOND instance 💥
```

---

### Q: What are hierarchical injectors?

```
Angular has a TREE of injectors that mirrors the component tree.
A lookup walks UP the tree until it finds a provider. ⭐
```

```
    Root injector          (providedIn: 'root')
          ↑
    Module injector        (module providers)
          ↑
    Component injector     (component providers)
          ↑
    Child component        ← the lookup starts HERE and walks UP
```

```
The NEAREST provider wins ⭐
```

Modifiers worth naming:

```
@Optional()  → don't throw if it's missing (inject null)
@Self()      → look ONLY in this component's own injector
@SkipSelf()  → skip mine, start at the parent
@Host()      → stop at the host component
```

#### Easy memory

```
Injectors form a TREE mirroring the components; lookup walks UP; NEAREST wins ⭐
@Optional / @Self / @SkipSelf / @Host modify the search.
```

---

### Q: What is an injection token, and when do you use it?

```
An interface disappears at runtime (TypeScript types are erased),
so it CANNOT be a DI key.

InjectionToken gives you a runtime key for non-class values. ⭐
```

```typescript
export const API_URL = new InjectionToken<string>('api.url');

providers: [{ provide: API_URL, useValue: 'https://api.robogebra.com' }]

constructor(@Inject(API_URL) private apiUrl: string) {}
// or: private apiUrl = inject(API_URL);
```

```
Use a token for: config strings, feature flags, an interface-typed dependency,
                 or a third-party object you don't own ⭐
```

#### Easy memory

```
InjectionToken = a DI key for non-class values (strings, config, interfaces) ⭐
Because interfaces DON'T EXIST at runtime.
{ provide: TOKEN, useValue: … } + @Inject(TOKEN) or inject(TOKEN)
```

---

## Routing

### Q: How do you configure routes in Angular?

```typescript
const routes: Routes = [
  { path: '',        component: HomeComponent },
  { path: 'books',   component: BookListComponent, canActivate: [AuthGuard] },
  { path: 'books/:id', component: BookDetailComponent, resolve: { book: BookResolver } },
  { path: 'admin',   loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule) },
  { path: '**',      component: PageNotFoundComponent }        // ⭐ MUST be last
];
```

⚠️ **Order matters — the router takes the FIRST match:**

```
{ path: '**' } placed FIRST → every route becomes 404 💥
Always put the wildcard LAST ⭐
```

#### 🔵 In your RoboGebra code

`core/AppRoutes.ts` holds route path constants, referenced by every guard and redirect — so a renamed route is a compile error, not a silent broken link. Good practice to mention.

#### Easy memory

```
path | component | canActivate | resolve | loadChildren | ** (LAST ⭐)
The router takes the FIRST match → order matters
Keep route paths in a constants file → renames become compile errors ⭐
```

---

### Q: `routerLink` and programmatic navigation?

```html
<a routerLink="/books" routerLinkActive="active">Books</a>
<a [routerLink]="['/books', book.id]" [queryParams]="{ page: 2 }">Detail</a>
```

```typescript
this.router.navigate(['/books', id], { queryParams: { page: 2 } });
this.router.navigateByUrl('/books/5');
this.router.navigate(['../'], { relativeTo: this.route });   // relative ⭐
```

⚠️ **Never use `href` for internal links:**

```
href       → a FULL page reload → the whole SPA restarts 💥
routerLink → the router swaps the component ✅
```

#### Easy memory

```
routerLink (template) | router.navigate([...]) (code)
routerLinkActive="active" for styling
⚠️ href on an internal link = FULL RELOAD 💥
```

---

### Q: Route params vs query params?

```
/books/5            → ROUTE param   → identifies WHICH resource ⭐
/books?page=2&sort=name → QUERY param → optional filters/options ⭐
```

```typescript
// snapshot — fine if the component is RECREATED each time
const id = this.route.snapshot.paramMap.get('id');

// observable — REQUIRED if you navigate between /books/5 → /books/6
//              without leaving the component ⭐
this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe(params => {
  this.loadBook(params.get('id'));
});

this.route.queryParamMap.subscribe(q => this.page = +q.get('page'));
```

⚠️ **The classic bug:**

```
Using .snapshot, then navigating /books/5 → /books/6
   → Angular REUSES the component, ngOnInit does NOT run again
   → the page still shows book 5 💥
Fix: subscribe to paramMap ⭐
```

#### Easy memory

```
/books/5 → paramMap (WHICH resource) | ?page=2 → queryParamMap (options)
snapshot = read ONCE | paramMap.subscribe = reacts to param changes ⭐
⚠️ snapshot + navigating between ids of the same route = stale page 💥
```

---

### Q: What are route guards?

```
CanActivate      → may they ENTER this route?      ⭐ (auth)
CanActivateChild → may they enter the CHILDREN?
CanDeactivate    → may they LEAVE?                 ⭐ (unsaved changes)
Resolve          → PRE-FETCH data before the route activates ⭐
CanMatch         → should this route even be CONSIDERED? (replaces CanLoad)
```

```
Navigation requested
     ↓
CanMatch → CanActivate → CanActivateChild → Resolve → component renders ✅
     ↓ any returns false / a UrlTree
navigation is CANCELLED
```

Modern functional style (Angular 15+):

```typescript
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isLoggedIn() ? true : router.createUrlTree(['/login']);
};
```

#### 🔵 In your RoboGebra code — the real guard ⭐

**File:** `core/guard/auth-guard.service.ts` (4 guards, 27 resolvers)

```typescript
canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.authService.isLoggedInObservable().pipe(
        tap(isLoggedIn => {
            if (!isLoggedIn) {
                this.authService.setRedirectUrl(state.url);   // ⭐ remember the destination
                this.router.navigate(['/' + AppRoutes.LOGIN]);
            }
        }),
        map(isLoggedIn => {
            if (!isLoggedIn) return false;
            if (this.authService.isLoggedInAsParent()) {       // ⭐ ROLE-based routing
                const targetPath = state.url.split('?')[0].replace(/^\//, '');
                const isAllowed = PARENT_ALLOWED_ROUTES.some(r =>
                    targetPath === r || targetPath.startsWith(r + '/'));
                if (!isAllowed) { this.router.navigate(['/' + AppRoutes.PARENT_DASHBOARD]); return false; }
            }
            return true;
        })
    );
}
```

> 🗣️ *"The guard returns an `Observable<boolean>` rather than a plain boolean, because login state is itself async — the router waits for it to emit. It stores the requested URL before redirecting, so after login the user lands where they originally wanted instead of on a generic home page. On top of authentication we do role-based routing: a parent account is only allowed into the parent routes and gets bounced to the dashboard otherwise."*

Your other guards: `login-guard` (keeps a logged-in user off `/login`), `public-preview-guard`, `anonymous-solution-guard`.

⭐ **Resolvers:** 27 of them, including `chapter-access-ruler.resolver.ts` — *"the access-ruler resolvers decide what a user may see before the route activates, so the component never renders a 'no access' flash."*

#### Easy memory

```
CanActivate (enter) ⭐ | CanDeactivate (leave — unsaved changes) ⭐
Resolve (pre-fetch) ⭐ | CanMatch (should this route be considered)

Return true | false | a UrlTree (redirect) | or an Observable of those ⭐
Modern: functional guards with inject()
```

---

### Q: What is lazy loading and why use it?

```
Lazy loading = don't download a feature's code until the user navigates to it. ⭐
```

```typescript
// module-based
{ path: 'admin', loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule) }

// standalone (Angular 14+)
{ path: 'admin', loadComponent: () => import('./admin/admin.component').then(m => m.AdminComponent) }
```

```
EAGER (everything in main.js)        LAZY
   main.js = 4 MB                      main.js  = 1 MB ⚡ (first paint is fast)
   ↓                                    admin.js = 1 MB (only if you visit /admin)
   3s to first paint 🐢                 books.js = 1 MB
```

Real-world idea: a **restaurant menu.** They don't cook every dish the moment you sit down.

Preloading strategies:

```typescript
RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules })
// downloads lazy chunks in the BACKGROUND after the app boots ⭐
// → fast first paint AND instant navigation
```

#### Easy memory

```
loadChildren (module) / loadComponent (standalone) → separate chunk per feature ⭐
Benefit: smaller initial bundle → faster first paint
PreloadAllModules = fast start + instant later navigation ⭐
```

---

## Forms

### Q: Template-driven vs reactive forms?

The easiest way to remember:

```
TEMPLATE-DRIVEN → the logic lives in the HTML  → ngModel  → simple forms
REACTIVE        → the logic lives in the CLASS → FormGroup → everything else ⭐
```

| | Template-driven | Reactive ⭐ |
|---|---|---|
| Module | `FormsModule` | `ReactiveFormsModule` |
| Where the model lives | the template | the **class** |
| Setup | `[(ngModel)]` | `FormGroup` / `FormControl` |
| Validation | directives in HTML | functions in TypeScript |
| Async / dynamic | painful | easy ⭐ |
| Testable without the DOM | ❌ | ✅ ⭐ |
| Best for | login, a 2-field search | everything real |

```typescript
// REACTIVE
this.form = this.fb.group({
  name:  ['', [Validators.required, Validators.minLength(3)]],
  email: ['', [Validators.required, Validators.email]]
});
```

```html
<form [formGroup]="form" (ngSubmit)="onSubmit()">
  <input formControlName="name">
  <div *ngIf="form.get('name')?.touched && form.get('name')?.errors?.['required']">
    Name is required
  </div>
</form>
```

⭐ The deciding line: **"reactive forms are testable without rendering the template."**

#### 🔵 In your RoboGebra code

```
Validators.* → 67 usages
FormBuilder  → 2 usages  (most forms build FormGroup explicitly)
```

**File:** `features/pages/settings/profile/parent-info/parent-info.component.ts` — an async availability check with `debounceTime` + `distinctUntilChanged` before validating.

#### Easy memory

```
Template-driven → ngModel → logic in HTML → simple forms
REACTIVE ⭐     → FormGroup/FormControl → logic in the CLASS → testable, dynamic

"Reactive forms are testable without rendering the template." ⭐
```

---

### Q: Explain `FormControl`, `FormGroup` and `FormArray`.

```
FormControl → ONE field
FormGroup   → a fixed OBJECT of controls
FormArray   → a DYNAMIC LIST of controls (add/remove at runtime) ⭐
```

```typescript
this.form = new FormGroup({
  name:  new FormControl(''),
  address: new FormGroup({                     // nested group
    city: new FormControl(''),
    zip:  new FormControl('')
  }),
  phones: new FormArray([                      // ⭐ dynamic
    new FormControl('')
  ])
});

get phones() { return this.form.get('phones') as FormArray; }
addPhone()    { this.phones.push(new FormControl('')); }
removePhone(i: number) { this.phones.removeAt(i); }
```

```
FormGroup (form)
 ├── FormControl  name
 ├── FormGroup    address
 │      ├── FormControl city
 │      └── FormControl zip
 └── FormArray    phones          ← grows and shrinks at runtime ⭐
        ├── FormControl [0]
        └── FormControl [1]
```

The control state flags you should be able to name:

```
valid / invalid | touched / untouched | dirty / pristine | pending (async)
```

⭐ `touched` vs `dirty`: **touched** = they focused and left it; **dirty** = they changed the value. Error messages usually use `touched`.

#### Easy memory

```
FormControl = 1 field | FormGroup = an object | FormArray = a dynamic LIST ⭐
State: valid | touched (focused+left) | dirty (value changed) | pending
Show errors on `touched`, not on `dirty` ⭐
```

---

### Q: What is `FormBuilder`?

```
FormBuilder = a shorthand so you stop typing `new FormControl` everywhere.
```

```typescript
// without
this.form = new FormGroup({
  name: new FormControl('', [Validators.required])
});

// with FormBuilder ✅
this.form = this.fb.group({
  name: ['', [Validators.required]],        //  [initial value, sync validators, async validators]
  email: ['', [Validators.required, Validators.email]]
});
```

⭐ Angular 14+ adds `NonNullableFormBuilder`, so `reset()` returns to the initial value rather than `null` — a nice detail:

```typescript
constructor(private fb: NonNullableFormBuilder) {}
```

#### Easy memory

```
fb.group({ name: ['', [Validators.required]] })
Array = [initialValue, syncValidators, asyncValidators] ⭐
NonNullableFormBuilder → reset() restores the initial value, not null
```

---

### Q: How do validators work — built-in, custom and async?

```typescript
// BUILT-IN
Validators.required | minLength(3) | maxLength(50) | email | pattern(/.../) | min | max

// CUSTOM (sync) — return null when VALID ⭐
export function noSpaces(control: AbstractControl): ValidationErrors | null {
  return control.value?.includes(' ') ? { noSpaces: true } : null;
}

// CROSS-FIELD — goes on the GROUP, not the control ⭐
export function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  return group.get('password')?.value === group.get('confirm')?.value
       ? null : { mismatch: true };
}
this.form = this.fb.group({ password: [''], confirm: [''] }, { validators: passwordsMatch });

// ASYNC — returns an Observable ⭐
export class EmailValidator implements AsyncValidator {
  validate(control: AbstractControl): Observable<ValidationErrors | null> {
    return this.http.get(`/api/check-email?e=${control.value}`).pipe(
      map(taken => (taken ? { emailTaken: true } : null)),
      catchError(() => of(null))
    );
  }
}
```

⭐ **The rule everyone forgets: return `null` when the value is VALID.** Returning an object means "there is an error".

```
null            → VALID ✅
{ errorKey: … } → INVALID ❌
```

⚠️ Async validators run **only after** all sync validators pass, and the control's state is `PENDING` in between — so disable the submit button on `form.pending` too.

#### Easy memory

```
Built-in: required, minLength, email, pattern
Custom sync: (control) => null (VALID ⭐) | { key: true } (invalid)
Cross-field: put the validator on the GROUP ⭐
Async: returns an Observable → state is PENDING ⭐ → disable submit on pending too
```

---

### Q: How do you build dynamic forms? (Angular Formly)

```
Dynamic form = the fields come from CONFIG (often JSON from the backend),
               not from hard-coded HTML. ⭐
```

```typescript
fields: FormlyFieldConfig[] = [
  { key: 'name',  type: 'input',  props: { label: 'Name', required: true } },
  { key: 'email', type: 'input',  props: { label: 'Email', type: 'email' } },
  { key: 'dob',   type: 'datepicker', props: { label: 'Date of birth' } }
];
```

```html
<form [formGroup]="form"><formly-form [form]="form" [fields]="fields" [model]="model"></formly-form></form>
```

```
JSON config → Formly looks up the `type` in a REGISTRY → renders that component
      ↓
This is the STRATEGY / FACTORY pattern ⭐ → [17 — Design Patterns](./17-solid-design-patterns.md)
```

> 🗣️ **Your EasyVisa answer:** *"Formly is essentially a strategy/factory pattern — a JSON config picks the field type and a registry maps that type to a component. In EasyVisa I registered custom field types for our nested reactive forms, which meant a new form field was a **config change rather than a template change** — the backend could add a field without a front-end release."*

⭐ That last clause is the business value, and it's what makes the answer land.

#### Easy memory

```
Dynamic forms = fields from CONFIG, not hard-coded HTML
Formly: key + type + props → a registry maps type → component
= the STRATEGY/FACTORY pattern ⭐
Value: a new field is a CONFIG change, not a release ⭐
```

---
## RxJS & Observables

> Deep dive on operators: **[20 — RxJS Operators](./20-rxjs-operators.md)**.

### Q: Observable vs Promise?

The easiest way to remember:

```
Promise    → ONE value,   eager,  cannot be cancelled
Observable → MANY values, lazy,   CAN be cancelled ⭐
```

| | Promise | Observable |
|---|---|---|
| Values | exactly **one** | **0..∞** (a stream) |
| Execution | **eager** — runs the moment it's created | **lazy** — nothing runs until `subscribe()` ⭐ |
| Cancellable | ❌ | ✅ `unsubscribe()` ⭐ |
| Operators | `.then` chains | 100+ operators (`map`, `switchMap`, `debounceTime`…) |
| Retry | manual | `retry()`, `retryWhen()` |

```
Promise    ──────────[value]────▶|          one delivery, already dispatched

Observable ──[v1]──[v2]──[v3]───▶|          a subscription, cancellable ⭐
```

Real-world idea:

```
Promise    → ordering ONE pizza. Once you order, it's coming. 🍕
Observable → a MAGAZINE subscription: many issues, and you can cancel ⭐ 📬
```

⭐ The cancellation point is the one that matters in Angular: a user navigating away should cancel the in-flight HTTP request, and only Observables can do that.

#### Easy memory

```
Promise = 1 value, EAGER, not cancellable  🍕
Observable = many values, LAZY, CANCELLABLE ⭐ 📬
"Nothing happens until you subscribe."
```

---

### Q: `Subject` vs `BehaviorSubject` vs `ReplaySubject`?

The easiest way to remember — **what does a LATE subscriber get?**

```
Subject         → NOTHING (only future values)      → an event/notification
BehaviorSubject → the CURRENT value ⭐              → state (needs an initial value)
ReplaySubject   → the last N values                 → history
AsyncSubject    → only the FINAL value, on complete → rare
```

```
Subject:          A──B──[you subscribe]──C──D     you get: C, D
BehaviorSubject:  A──B──[you subscribe]──C──D     you get: B ⭐, C, D
ReplaySubject(2): A──B──[you subscribe]──C──D     you get: A, B, C, D
```

```typescript
const s  = new Subject<string>();               // no initial value
const bs = new BehaviorSubject<string>('init'); // ⭐ REQUIRES an initial value
const rs = new ReplaySubject<string>(2);        // buffers the last 2
```

Real-world idea:

```
Subject         → a LIVE broadcast: join late, you miss what happened 📻
BehaviorSubject → a WhatsApp group's pinned message: you see it on joining ⭐
ReplaySubject   → the last 2 messages replayed when you join
```

⭐ **Why `BehaviorSubject` is the right choice for state:** a component created later (a lazy-loaded page, a modal) still needs the *current* user, not just the *next* change.

#### 🔵 In your RoboGebra code

**122 `BehaviorSubject` usages** — the whole state layer. `user-summary.service.ts` keeps five of them (user summary, banner visibility, expired modal, institute modal, welcome modal).

#### Easy memory

```
Subject         → late subscriber gets NOTHING       (live radio 📻)
BehaviorSubject → gets the CURRENT value ⭐          (pinned message)
ReplaySubject(n)→ gets the last n values

State → BehaviorSubject ⭐ (a late component still needs the current user)
```

---

### Q: `map`, `filter`, `switchMap`, `mergeMap`, `concatMap`, `exhaustMap` — when do you use each?

The easiest way to remember the four **flattening** operators by **what they do with a NEW value while the previous inner observable is still running**:

```
switchMap  → CANCEL the previous  ⭐ search / latest-wins
mergeMap   → run BOTH in parallel   → independent, order doesn't matter
concatMap  → QUEUE it, run in order ⭐ order matters (saves, writes)
exhaustMap → IGNORE the new one ⭐   → login button double-click
```

```
Input:      A────B────C

switchMap :  A✗   B✗   C───▶      only the LAST survives ⭐
mergeMap  :  A───────▶
             B──────▶            all in parallel, any order
             C─────▶
concatMap :  A───▶B───▶C───▶     strictly in order ⭐
exhaustMap:  A──────────▶        B and C IGNORED while A runs ⭐
```

Real-world ideas:

```
switchMap  → changing the TV channel: the old one stops instantly 📺
mergeMap   → several taps filling several buckets at once 🚰
concatMap  → a queue at a counter: one at a time, in order 🎫
exhaustMap → a lift: pressing the button again while the doors close does nothing 🛗
```

```typescript
// SEARCH — cancel the stale request ⭐
searchTerm$.pipe(debounceTime(300), distinctUntilChanged(),
                 switchMap(term => this.api.search(term)));

// SAVE — order matters, never lose one ⭐
saveClicks$.pipe(concatMap(data => this.api.save(data)));

// LOGIN — ignore double clicks ⭐
loginClicks$.pipe(exhaustMap(creds => this.auth.login(creds)));
```

⭐ The **why** behind `switchMap` for search (say this): *"it prevents the out-of-order-response bug, where a slow query for 'ma' lands after a fast one for 'maths' and overwrites the correct results."*

#### 🔵 In your RoboGebra code

```
switchMap → 246 usages | map → 496 | filter → 485
mergeMap / concatMap / exhaustMap → 0 ⭐
```

> 🗣️ Be honest and precise: *"We use `switchMap` heavily — in the auth interceptor and in every search — because latest-wins is almost always what you want for reads. We haven't needed `concatMap` or `exhaustMap` yet, but I know where they belong: `concatMap` for ordered writes, `exhaustMap` to stop a double-submitted login."*

#### Easy memory

```
switchMap  → CANCEL previous ⭐ (TV channel 📺) → search, latest wins
mergeMap   → PARALLEL         (many taps 🚰)  → independent work
concatMap  → QUEUE in order ⭐ (a ticket queue 🎫) → ordered writes
exhaustMap → IGNORE new ⭐     (a lift 🛗)      → double-click protection
```

---

### Q: How do you subscribe and unsubscribe? Why does it matter?

```
An unsubscribed Observable keeps its subscription — and the component — ALIVE.
That's a MEMORY LEAK. 💥
```

```
Component destroyed
      ↓ but the subscription is still active
the callback still runs → it references `this` → the component can't be GC'd
      ↓
100 navigations later → 100 dead components in memory → the app crawls 💥
```

The four ways to prevent it, best first:

```
1. `async` pipe ⭐⭐ — Angular subscribes AND unsubscribes for you
2. takeUntilDestroyed() ⭐ (Angular 16+) — no boilerplate
3. takeUntil(this.destroy$) ⭐ — the classic pattern
4. subscription.unsubscribe() in ngOnDestroy — manual, easy to forget
```

⭐ **Which observables do NOT need unsubscribing?**

```
HttpClient calls → they COMPLETE after one emission ✅
route params on a component destroyed with the route ✅

Everything long-lived DOES: a Subject, an interval, valueChanges,
a router event stream, a window event ⭐
```

That nuance ("`HttpClient` completes, so it self-cleans") is a good thing to volunteer.

#### Easy memory

```
Not unsubscribing = a MEMORY LEAK (the component can't be garbage collected) 💥

BEST → async pipe ⭐⭐ (subscribes AND unsubscribes)
then → takeUntilDestroyed() (v16+) → takeUntil(destroy$) → manual unsubscribe

HttpClient COMPLETES → no leak. Subjects/intervals/valueChanges DO leak ⭐
```

---

### Q: What is the `async` pipe and why prefer it?

```
async pipe = subscribe + unsubscribe + trigger change detection,
             all handled by Angular. ⭐
```

```typescript
// component — no subscribe, no ngOnDestroy ⭐
users$ = this.userService.getUsers();
```

```html
<div *ngFor="let user of users$ | async">{{ user.name }}</div>

<!-- with loading and error states -->
<ng-container *ngIf="users$ | async as users; else loading">
  <div *ngFor="let user of users">{{ user.name }}</div>
</ng-container>
<ng-template #loading><app-spinner/></ng-template>
```

```
✅ No manual subscribe        ✅ No ngOnDestroy needed
✅ No leak possible           ✅ Marks OnPush components for check ⭐
```

⚠️ **The trap:** using `| async` twice on the same observable creates **two subscriptions** → two HTTP calls.

```html
<!-- ❌ TWO subscriptions, TWO HTTP calls 💥 -->
<div>{{ (user$ | async)?.name }}</div>
<div>{{ (user$ | async)?.email }}</div>

<!-- ✅ subscribe ONCE with `as` -->
<ng-container *ngIf="user$ | async as user">
  <div>{{ user.name }}</div>
  <div>{{ user.email }}</div>
</ng-container>
```

(Or add `shareReplay(1)` to the stream.)

#### 🔵 In your RoboGebra code

`async` appears **311 times** in `robogebra-mobile` templates and code.

#### Easy memory

```
async pipe = subscribe + unsubscribe + mark for check ⭐ — the DEFAULT choice
⚠️ Two `| async` on the same stream = TWO subscriptions = TWO HTTP calls 💥
   Fix: `*ngIf="x$ | async as x"` or shareReplay(1) ⭐
```

---

### Q: Explain the `takeUntil` pattern.

```typescript
export class MyComponent implements OnDestroy {
  private readonly destroy$ = new Subject<void>();

  ngOnInit() {
    this.service.data$.pipe(
      takeUntil(this.destroy$)          // ⭐ MUST BE LAST in the pipe
    ).subscribe(data => this.data = data);
  }

  ngOnDestroy() {
    this.destroy$.next();               // emit → every takeUntil completes
    this.destroy$.complete();           // ⭐ clean up the Subject itself
  }
}
```

```
destroy$.next()
      ↓
EVERY takeUntil(destroy$) in the component completes at once ✅
one line of cleanup, however many subscriptions you have ⭐
```

⚠️ **`takeUntil` must be the LAST operator:**

```typescript
.pipe(takeUntil(this.destroy$), switchMap(...))   // ❌ switchMap's inner subscription leaks
.pipe(switchMap(...), takeUntil(this.destroy$))   // ✅
```

Angular 16+ removes the boilerplate entirely:

```typescript
this.service.data$.pipe(takeUntilDestroyed()).subscribe(...);   // ⭐ in an injection context
```

#### 🔵 In your RoboGebra code

```
takeUntil          → 426 usages ⭐
takeUntilDestroyed →   5 usages (the newer code)
ngOnDestroy        → 100 components
```

Even the HTTP interceptor uses it — `cancelRequests$` lets logout cancel every in-flight request at once:

```typescript
return next.handle(req).pipe(
    takeUntil(this.cancelRequests$),     // ⭐ mass-cancel on logout
    catchError(error => this.handleUnAuthResponse(error, req, next))
);
```

#### Easy memory

```
private destroy$ = new Subject<void>();
.pipe(..., takeUntil(this.destroy$))          ← LAST operator ⭐
ngOnDestroy() { destroy$.next(); destroy$.complete(); }

One emit cancels EVERY subscription in the component ⭐
Angular 16+: takeUntilDestroyed() — no boilerplate
```

---

### Q: How do you handle errors in RxJS?

```
catchError → CATCH and RECOVER (return a fallback observable) ⭐
retry(n)   → resubscribe n times
retryWhen  → conditional / delayed retry
finalize   → ALWAYS runs, success or error ⭐ (hide the spinner)
```

```typescript
this.http.get<User[]>('/api/users').pipe(
  retry(2),                                       // try twice more
  catchError(err => {
    this.notify.error('Could not load users');
    return of([]);                                // ⭐ RECOVER with a fallback
  }),
  finalize(() => this.loading = false)            // ⭐ always
).subscribe(users => this.users = users);
```

⚠️ **The critical rule:**

```
An ERROR TERMINATES the observable. Permanently. ⭐

So if the error escapes to the outer stream, the stream is DEAD —
a search box would stop working after the first failed request 💥
```

```typescript
// ❌ one failure kills the search forever
searchTerm$.pipe(switchMap(t => this.api.search(t)), catchError(() => of([])))

// ✅ catch INSIDE the inner observable → the outer stream survives ⭐
searchTerm$.pipe(switchMap(t => this.api.search(t).pipe(catchError(() => of([])))))
```

⭐ **Where to put `catchError` is the real question here** — inside the inner observable, not outside it.

#### 🔵 In your RoboGebra code

```
catchError → 338 usages | finalize → 254 usages ⭐
```

`finalize` at 254 usages is the spinner-hiding pattern — mention that: *"`finalize` guarantees the busy spinner is hidden whether the call succeeded or failed."*

#### Easy memory

```
catchError → recover with a fallback: return of([]) ⭐
retry(n) | retryWhen | finalize → ALWAYS runs (hide the spinner) ⭐

⚠️ An error KILLS the stream permanently.
   Put catchError INSIDE the switchMap's inner observable ⭐
   or one failure ends the search box forever 💥
```

---

## State Management (NgRx)

> Deep dive: **[21 — NgRx](./21-ngrx.md)**.

### Q: What is NgRx and what are its building blocks?

```
NgRx = Redux for Angular — ONE immutable store, changed only by dispatched actions.
```

```
Component
    │ dispatch(action)
    ▼
  ACTION ──▶ REDUCER ──▶ STORE (one immutable state tree)
    │        (pure fn)        │
    ▼                         │ selector (memoised)
  EFFECT (side effects:       ▼
   HTTP, router) ──▶ a new  Component
                     ACTION
```

```
Action   → "what happened"      (an event, past tense: [Books] Load Success)
Reducer  → a PURE function (state, action) => newState — no side effects ⭐
Store    → the single source of truth, immutable
Selector → a MEMOISED read of a slice ⭐
Effect   → where the impure stuff lives: HTTP, router, storage ⭐
```

```typescript
export const loadBooks        = createAction('[Books] Load');
export const loadBooksSuccess = createAction('[Books] Load Success', props<{ books: Book[] }>());

export const booksReducer = createReducer(initialState,
  on(loadBooks,        state => ({ ...state, loading: true })),      // ⭐ new object, never mutate
  on(loadBooksSuccess, (state, { books }) => ({ ...state, books, loading: false }))
);

loadBooks$ = createEffect(() => this.actions$.pipe(
  ofType(loadBooks),
  switchMap(() => this.api.getBooks().pipe(
    map(books => loadBooksSuccess({ books })),
    catchError(err => of(loadBooksFailure({ err })))     // ⭐ never let an effect die
  ))
));
```

Real-world idea: a **bank ledger.** You never edit a past entry; you add a new transaction, and the balance is derived.

#### Easy memory

```
Action (what happened) → Reducer (PURE, returns a NEW state) → Store → Selector (memoised)
Effects = side effects (HTTP, router) → dispatch another action ⭐
NEVER mutate state — always spread into a new object ⭐
```

---

### Q: What are effects and why memoise selectors?

```
EFFECT   → keeps side effects OUT of reducers, so reducers stay pure and testable ⭐
SELECTOR → memoised: if the input slice hasn't changed, it returns the CACHED result
           without recomputing ⭐
```

```typescript
export const selectBooks = createSelector(selectBooksState, s => s.books);

export const selectExpensiveBooks = createSelector(
  selectBooks,
  books => books.filter(b => b.price > 500).sort(...)   // recomputed ONLY if books changed ⭐
);
```

```
Any unrelated state change → the selector returns the CACHED array (same reference)
      ↓
An OnPush component bound to it does NOT re-render ⭐
```

⭐ That link — **memoised selector → stable reference → `OnPush` skips the re-render** — is the answer that shows you understand *why* it matters.

#### Easy memory

```
Effects = side effects out of reducers → reducers stay PURE and testable ⭐
Selectors are MEMOISED → same input, same reference → OnPush skips re-render ⭐
```

---

### Q: NgRx vs a service with a `BehaviorSubject`?

```
Service + BehaviorSubject → simple, little code → MOST apps ⭐
NgRx                      → many features share state, you need traceability
```

| Use a service + BehaviorSubject | Use NgRx |
|---|---|
| state used by a few related components | state shared across many unrelated features |
| a small/medium app | a large app, several teams |
| you want minimal boilerplate | you need time-travel debugging + a strict audit trail |
| simple CRUD | complex flows, optimistic updates, undo |

#### 🔵 In your RoboGebra code — the honest answer ⭐

```
robogebra-mobile: NgRx → NOT used.  BehaviorSubject services → 122 usages.
```

> 🗣️ *"We deliberately use `BehaviorSubject`-backed services rather than NgRx. `BehaviorSubject` gives the two things we actually needed: a current value on subscribe, and a synchronous `getValue()` for callers that can't subscribe — the institute-code HTTP interceptor, for instance. NgRx would have added actions, reducers, effects and selectors for state that only a handful of components read. I'd reach for NgRx when state is shared across many unrelated features and I need time-travel debugging or a strict audit trail — I've worked with it, but on this app it would have been ceremony."*

⭐ **Do not pretend to use NgRx.** "I chose not to, and here's the trade-off" is a *stronger* answer than "yes we use it".

#### Easy memory

```
Service + BehaviorSubject ⭐ → most apps, minimal boilerplate
NgRx → many features + traceability + time-travel debugging

Your answer: "We chose BehaviorSubject services deliberately — NgRx would
have been ceremony for state a handful of components read." ⭐
```

---

## HTTP

### Q: How do you use `HttpClient`?

```typescript
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly base = '/api/users';
  constructor(private http: HttpClient) {}

  getAll(page = 0): Observable<User[]> {
    const params = new HttpParams().set('page', page).set('size', 20);
    return this.http.get<User[]>(this.base, { params });      // ⭐ typed generic
  }
  create(user: User): Observable<User> { return this.http.post<User>(this.base, user); }
  update(id: number, user: User)       { return this.http.put<User>(`${this.base}/${id}`, user); }
  delete(id: number)                    { return this.http.delete<void>(`${this.base}/${id}`); }
}
```

```
✅ Returns an OBSERVABLE (cancellable, retryable, composable)
✅ Typed with a generic → get<User[]>()
✅ JSON parsed automatically
✅ Interceptors apply to every call ⭐
✅ LAZY — nothing is sent until you subscribe ⭐
```

⚠️ The classic beginner bug:

```typescript
this.http.get('/api/users');           // ❌ NO REQUEST IS SENT — nobody subscribed 💥
this.http.get('/api/users').subscribe(...)  // ✅
```

#### Easy memory

```
http.get<T>(url, { params, headers }) → an OBSERVABLE, lazy ⭐
No subscribe = NO REQUEST 💥
Typed generics + automatic JSON + interceptors on every call
```

---

### Q: What are HTTP interceptors?

```
Interceptor = middleware for EVERY HTTP request and response.
              One place for auth tokens, errors, logging and retries. ⭐
```

```
Request  → [ auth ] → [ logging ] → [ error ] → server
Response ← [ auth ] ← [ logging ] ← [ error ] ← server
                (they run in REGISTRATION ORDER for requests,
                 and in REVERSE for responses ⭐)
```

#### 🔵 In your RoboGebra code — your real interceptor ⭐

**File:** `core/interceptors/header-authorization.interceptor.ts`

This one file demonstrates **six** interview topics at once:

```typescript
intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (this.isAuthEndpoint(req.url)) {              // 1. don't attach a token to /auth 🐔🥚
        return this.invokeHttpCall(req, next);
    }

    let updatedHeader: HttpHeaders = req.headers;
    if (!req.headers.has('Content-Type') && !(req.body instanceof FormData)) {
        updatedHeader = updatedHeader.set("Content-Type", 'application/json');   // 2. FormData guard ⭐
    }

    return this.authService.getAuthDetailsObservable().pipe(
        switchMap((authDetails: AuthDetails) => {                                // 3. the token is ASYNC
            if (authDetails != null) {
                updatedHeader = updatedHeader.set("Authorization", `Bearer ${authDetails.token}`);
            }
            const cloned = req.clone({ headers: updatedHeader });                // 4. IMMUTABLE clone ⭐
            return this.invokeHttpCall(cloned, next);
        })
    );
}

private invokeHttpCall(req, next) {
    return next.handle(req).pipe(
        takeUntil(this.cancelRequests$),                                         // 5. mass-cancel on logout ⭐
        catchError(err => this.handleUnAuthResponse(err, req, next))             // 6. 401 → refresh → retry
    );
}
```

**The six things to say about it:**

```
1. HttpRequest is IMMUTABLE → you MUST req.clone(); mutating it silently does nothing ⭐
2. FormData guard — setting Content-Type manually BREAKS multipart file uploads,
   because the BROWSER has to generate the multipart boundary itself ⭐⭐
3. switchMap because the token itself arrives asynchronously (from native storage)
4. Auth endpoints are skipped, or login would need a token to get a token 🐔🥚
5. takeUntil(cancelRequests$) lets LOGOUT cancel every in-flight request at once ⭐
6. catchError centralises 401 → refresh-token → retry in ONE place, instead of
   in every one of the 89 services
```

⭐ **Point 2 is the one that makes interviewers sit up** — it's a real bug most people have shipped and never diagnosed.

Modern functional style (Angular 15+):

```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getToken();
  return next(token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req);
};
provideHttpClient(withInterceptors([authInterceptor]));
```

#### Easy memory

```
Interceptor = middleware for EVERY request/response ⭐
Uses: auth token | error handling | logging | retry | loading spinner

⚠️ HttpRequest is IMMUTABLE → req.clone() ⭐
⚠️ Don't set Content-Type on FormData → it breaks file uploads ⭐⭐
⚠️ Skip the /auth endpoints, or login needs a token to get a token 🐔🥚
Requests run in registration order; responses in REVERSE ⭐
```

---

### Q: How do you handle HTTP errors gracefully?

```typescript
// in an error interceptor — ONE place for the whole app ⭐
catchError((error: HttpErrorResponse) => {
  if (error.status === 0)        this.notify.error('Network error — are you offline?');
  else if (error.status === 401) this.auth.logout();          // token expired
  else if (error.status === 403) this.router.navigate(['/forbidden']);
  else if (error.status === 404) this.notify.error('Not found');
  else if (error.status >= 500)  this.notify.error('Server error, please try again');
  return throwError(() => error);      // ⭐ rethrow so the caller can also react
})
```

⭐ `status === 0` is the detail worth knowing:

```
status 0 = the request never reached the server
         → offline, DNS failure, CORS block, or a cancelled request ⭐
```

That is especially relevant on mobile — pair it with `@capacitor/network`.

#### Easy memory

```
Handle centrally in an ERROR INTERCEPTOR, not in 89 services ⭐
0 = network/CORS ⭐ | 401 = logout | 403 = forbidden | 404 | 5xx = retry message
Rethrow with throwError(() => error) so callers can still react ⭐
```

---

## Pipes

### Q: What are pipes? Name the common built-in ones.

```
A pipe TRANSFORMS a value in the template. It never changes the source data. ⭐
```

```html
{{ name | uppercase }}
{{ price | currency:'INR' }}
{{ date | date:'dd/MM/yyyy' }}
{{ obj | json }}
{{ items | slice:0:5 }}
{{ ratio | percent }}
{{ n | number:'1.2-2' }}
{{ data$ | async }}                          ⭐ the important one
{{ name | uppercase | slice:0:10 }}          <!-- chained, left to right -->
```

#### Easy memory

```
uppercase | lowercase | titlecase | date | currency | number | percent
json | slice | keyvalue | async ⭐
Pipes TRANSFORM for display; they never mutate the source ⭐
```

---

### Q: Pure vs impure pipes?

```
PURE (default) → re-runs ONLY when the input REFERENCE changes ⚡
IMPURE         → re-runs on EVERY change detection cycle 🐢 ⚠️
```

```typescript
@Pipe({ name: 'filter', pure: false })   // ⚠️ runs on every CD cycle
```

```
Pure pipe on a list: push an item (SAME array reference)
      → the pipe does NOT re-run → the UI looks stale 💥
Fix: create a NEW array (immutability), don't make the pipe impure ⭐
```

```
`async` and `keyvalue` are IMPURE by necessity.
Everything you write should be PURE. ⭐
```

⚠️ An impure pipe over a 1,000-item list can run thousands of times per second. If you need filtering, do it in the component or with a memoised selector.

#### Easy memory

```
PURE ⭐ → only when the INPUT REFERENCE changes → fast
IMPURE  → EVERY change detection cycle → slow, use only if unavoidable

Mutating an array won't re-run a pure pipe → create a NEW array ⭐
async and keyvalue are impure by design.
```

---

### Q: How do you create a custom pipe?

```typescript
@Pipe({ name: 'truncate', standalone: true })
export class TruncatePipe implements PipeTransform {
  transform(value: string, limit = 20, suffix = '…'): string {
    if (!value) return '';
    return value.length > limit ? value.slice(0, limit) + suffix : value;
  }
}
```

```html
{{ description | truncate:50 }}
{{ description | truncate:50:' [more]' }}
```

⚠️ **The security pipe every Angular dev writes — and the warning to give with it:**

```typescript
@Pipe({ name: 'safeHtml' })
export class SafeHtmlPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}
  transform(html: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(html);   // ⚠️ bypasses XSS protection!
  }
}
```

> 🗣️ *"I'd only use `bypassSecurityTrust*` on content we control or have sanitised server-side — the name is a deliberate warning. Angular escapes interpolation by default, and that default is what prevents XSS."*

#### 🔵 In your RoboGebra code

There is a ` sanitization.service.ts` in `core/services/` — mention it as the place that centralises this decision rather than scattering `bypassSecurityTrust` calls.

#### Easy memory

```
@Pipe({ name: 'x' }) + implements PipeTransform + transform(value, ...args)
Extra pipe args after the colon: {{ x | truncate:50:'…' }}
⚠️ bypassSecurityTrustHtml DISABLES XSS protection — only for trusted content ⭐
```

---

## Performance & Change Detection

### Q: What is change detection? Default vs `OnPush`?

```
Change detection = how Angular keeps the DOM in sync with your data.

DEFAULT → after ANY async event, check the ENTIRE component tree 🐢
OnPush  → check this component ONLY when something it depends on changes ⚡
```

```
DEFAULT: one click anywhere
              ↓
      Angular checks EVERY component in the tree
      (1000 components = 1000 checks, most pointless) 🐢

OnPush: this component is re-checked ONLY when
      1. an @Input REFERENCE changes ⭐
      2. an event fires INSIDE it (click, etc.)
      3. an `async` pipe in its template emits ⭐
      4. you call cdr.markForCheck() manually
```

```typescript
@Component({ changeDetection: ChangeDetectionStrategy.OnPush, ... })
```

⚠️ **The trap that catches everyone:**

```typescript
// ❌ MUTATING — the reference is unchanged → OnPush does NOT re-render 💥
this.items.push(newItem);

// ✅ NEW reference → OnPush sees the change
this.items = [...this.items, newItem];
```

```
OnPush + mutation = a UI that silently doesn't update ⭐
→ which is exactly why OnPush pairs with IMMUTABLE data and the async pipe
```

#### 🔵 In your RoboGebra code — selective, not global ⭐

```
ChangeDetectionStrategy.OnPush → 6 components:
   learning-progress/chapter-progress-list/     ← a long list
   learning-progress/chapter-detail-modal/
   settings/payments/
   books/exercise-solution/.../solution-step-ai-assistant-editor/
   shared/video-splash-screen/
```

> 🗣️ *"`OnPush` is applied selectively rather than globally — on the long progress lists and the AI-assistant editor, where default change detection was re-checking on every unrelated event. Turning it on everywhere at once on a mature codebase is a good way to ship silent UI bugs, because any component that mutates an input array stops updating."*

⭐ That last sentence is a genuinely experienced answer.

#### Easy memory

```
DEFAULT → checks the WHOLE tree on any async event 🐢
OnPush ⭐ → only on: @Input REFERENCE change | internal event | async pipe | markForCheck

⚠️ MUTATION doesn't change the reference → OnPush won't update 💥
   this.items.push(x)      ❌
   this.items = [...x]     ✅
Pair OnPush with IMMUTABLE data + the async pipe ⭐
```

---

### Q: What is zone.js and its role?

```
zone.js MONKEY-PATCHES every async API — setTimeout, addEventListener,
XHR/fetch, promises — so Angular knows "something might have changed"
and can run change detection AUTOMATICALLY. ⭐
```

```
You click a button
      ↓
zone.js notices the patched addEventListener fired
      ↓
Angular runs change detection → the DOM updates ✅
(you never call anything yourself — that's the magic zone.js provides)
```

⭐ The cost, and where Angular is going:

```
zone.js patches EVERYTHING, so change detection runs even for
events that changed nothing → wasted work.

Angular 16+ → SIGNALS: the framework knows EXACTLY what changed
Angular 18+ → experimental ZONELESS change detection ⭐
```

Escaping the zone for hot code:

```typescript
this.ngZone.runOutsideAngular(() => {
  // e.g. a mousemove or animation loop — no change detection per frame ⭐
});
```

#### Easy memory

```
zone.js monkey-patches async APIs → Angular auto-runs change detection ⭐
Cost: it fires even when nothing changed
Future: signals (v16+) → zoneless (v18 experimental)
runOutsideAngular() for high-frequency events (mousemove, animation) ⭐
```

---

### Q: What are signals? (Angular 16+)

```
A signal = a value that KNOWS who is reading it,
           so Angular can update EXACTLY those places — no tree-walking. ⭐
```

```typescript
count  = signal(0);                              // writable
double = computed(() => this.count() * 2);       // ⭐ auto-recalculates
constructor() { effect(() => console.log(this.count())); }  // ⭐ runs on change

increment() { this.count.update(v => v + 1); }
```

```html
{{ count() }}        <!-- ⭐ note the parentheses — a signal is a FUNCTION -->
```

```
zone.js  → "something happened somewhere — re-check the whole tree" 🐢
signals  → "count changed → update exactly these 3 bindings" ⚡
```

Real-world idea: **a newspaper subscription vs shouting in the street.** Signals deliver to exactly the people who asked.

#### 🔵 In your RoboGebra code — be honest ⭐

```
signal() / computed() / effect() → 0 usages ⚠️
```

> 🗣️ *"Signals I've studied but haven't shipped — `robogebra-mobile` is on Angular 18 but grew from Angular 12, and the change-detection model is still zone-based. The migration path I'd take is signal inputs on leaf components first, since those are the lowest-risk and give the clearest win, then work upward. What signals really unlock is zoneless change detection, and that's not something you turn on halfway."*

⭐ Naming a **migration order** is what a technical lead is expected to have. Claiming adoption invites one follow-up you cannot answer.

#### Easy memory

```
signal(0) | computed(() => …) | effect(() => …)
Read with PARENTHESES: count() ⭐
.set() | .update(fn)

Why: fine-grained updates instead of walking the tree → the path to ZONELESS ⭐
Your honest line: "studied, not shipped — I'd migrate leaf components first." ⭐
```

---

### Q: AOT vs JIT compilation?

```
JIT (Just-in-Time)  → compile templates in the BROWSER, at runtime → dev only, historically
AOT (Ahead-of-Time) → compile at BUILD time ⭐ → the default since Angular 9 (Ivy)
```

| | JIT | AOT ⭐ |
|---|---|---|
| When | in the browser | at build time |
| Bundle size | larger (ships the compiler) | smaller ⭐ |
| Startup | slower | faster ⭐ |
| Template errors | found at **runtime** 💥 | found at **build** time ⭐ |
| Security | uses `eval` | no `eval` — CSP-friendly ⭐ |

⭐ The best reason: **template type errors become build failures instead of a blank page in production.**

#### Easy memory

```
AOT ⭐ = compiled at BUILD time → smaller, faster, template errors caught at build,
         no eval (CSP-safe). The DEFAULT since Angular 9 (Ivy).
JIT = compiled in the browser → legacy.
```

---

### Q: What techniques do you use to optimise an Angular app?

Group them — a list in random order sounds memorised:

```
BUNDLE SIZE
  ✅ Lazy-load feature routes (loadChildren / loadComponent) ⭐
  ✅ AOT + production build (tree-shaking, minification) — the default
  ✅ Analyse with source-map-explorer; @defer for heavy widgets (v17+)
  ✅ Preload strategies for instant later navigation

RENDERING
  ✅ OnPush + immutable data + the async pipe ⭐
  ✅ trackBy on every list ⭐
  ✅ Virtual scrolling (CDK) for long lists
  ✅ Pure pipes instead of method calls in templates ⭐

MEMORY / NETWORK
  ✅ Unsubscribe — async pipe or takeUntil ⭐
  ✅ debounceTime + switchMap on search ⭐
  ✅ shareReplay(1) for data fetched by several components
  ✅ Pagination instead of loading everything
```

⚠️ The one people never mention — **method calls in templates**:

```html
{{ getTotal() }}      <!-- ❌ runs on EVERY change detection cycle 💥 -->
{{ total }}           <!-- ✅ a field -->
{{ items | totalPipe }} <!-- ✅ a PURE pipe — cached ⭐ -->
```

#### 🔵 In your RoboGebra code

```
trackBy 55 | OnPush 6 (selective) | debounceTime 13 | takeUntil 426 | async 311
```

#### Easy memory

```
BUNDLE   → lazy routes ⭐ | AOT | @defer | source-map-explorer
RENDER   → OnPush + immutable + async ⭐ | trackBy ⭐ | virtual scroll | pure pipes
MEMORY   → unsubscribe ⭐ | debounce+switchMap ⭐ | shareReplay(1) | paginate

⚠️ NEVER call a method in a template — it runs on every CD cycle 💥
```

---

## Testing

### Q: What tools are used for Angular unit testing?

```
Jasmine → the FRAMEWORK   (describe / it / expect / spyOn)
Karma   → the RUNNER       (executes the tests in a real browser)
TestBed → Angular's utility for building a testing module ⭐
```

Many teams now swap Karma for **Jest** (faster, no browser, better watch mode).

#### 🔵 In your RoboGebra code

```
karma + jasmine configured in all three Angular apps
*.spec.ts alongside services and guards
   (auth.service.spec.ts, auth-guard.service.spec.ts,
    header-authorization.interceptor.spec.ts …)
```

⭐ Note *what* is tested: services, guards and the interceptor — the logic-heavy, non-visual parts. That's the right instinct, and worth saying.

---

### Q: What is `TestBed`?

```
TestBed = a miniature NgModule for tests — you declare the component
          and swap real dependencies for mocks. ⭐
```

```typescript
describe('UserCardComponent', () => {
  let fixture: ComponentFixture<UserCardComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [UserCardComponent],                                     // standalone
      providers: [{ provide: UserService, useValue: mockUserService }]  // ⭐ inject a mock
    });
    fixture = TestBed.createComponent(UserCardComponent);
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });
});
```

```
fixture.detectChanges()      → run change detection ⭐ (or the template never renders)
fixture.nativeElement        → the DOM
fixture.componentInstance    → the class
```

⚠️ Forgetting `fixture.detectChanges()` is the #1 reason a template assertion fails for no visible reason.

---

### Q: How do you test a service that uses `HttpClient`?

```typescript
TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
const httpMock = TestBed.inject(HttpTestingController);

service.getUsers().subscribe(users => expect(users.length).toBe(2));

httpMock.expectOne('/api/users').flush([{}, {}]);   // ⭐ assert AND respond
afterEach(() => httpMock.verify());                 // ⭐ no unexpected calls
```

```
expectOne(url) → asserts EXACTLY ONE request was made to that URL ⭐
flush(data)    → supplies the fake response
verify()       → fails the test if any request went unanswered ⭐
```

#### Easy memory

```
Jasmine (framework) + Karma (runner) + TestBed (module) ⭐
fixture.detectChanges() or the template never renders ⚠️
HTTP: HttpClientTestingModule + HttpTestingController
      expectOne(url).flush(data) + verify() ⭐
```

---

## Migration & Versioning

### Q: How do you upgrade Angular versions?

```
ONE major version at a time. Never jump. ⭐
```

```bash
ng update @angular/core @angular/cli        # runs SCHEMATICS that rewrite your code ⭐
```

Your process, as a sequence:

```
1. Check update.angular.io for the breaking changes between the two versions
2. ng update ONE major at a time → build → run the test suite after each step ⭐
3. Update third-party libraries to compatible versions
      (Ionic, PrimeNG, Material, Formly, NgRx — often the real blocker ⚠️)
4. Fix failing tests and deprecation warnings
5. THEN adopt new features incrementally
      (standalone, functional guards/interceptors, inject(), takeUntilDestroyed)
```

⭐ **The sentence that matters: "migrate first, modernise second."** Get it green on the new version *before* changing any code style.

⚠️ **The real blocker is almost never Angular itself** — it's a third-party library that hasn't released a compatible version yet. Saying that shows you've actually done one.

---

### Q: What changed across recent Angular versions?

```
14 → standalone (preview), TYPED reactive forms ⭐, inject(), functional guards
15 → standalone stabilised, functional HttpClient providers, NgOptimizedImage
16 → SIGNALS (preview) ⭐, takeUntilDestroyed ⭐, required inputs, esbuild dev server
17 → new control flow @if/@for ⭐, @defer ⭐, Vite + esbuild by default
18 → zoneless change detection (experimental) ⭐, signal APIs maturing, Material 3
19 → standalone is the DEFAULT, incremental hydration
```

The through-line to state:

```
Angular is moving AWAY from zone.js and NgModules, toward
SIGNALS + STANDALONE. Everything from 14 onward is a step on that path. ⭐
```

#### 🔵 In your RoboGebra code

```
robogebra-mobile     → Angular 18.2  (Ionic 8.7, Capacitor 6.2)
robogebra-web        → Angular 16.2
robogebra-backoffice → Angular 16.2  (TypeScript 4.9)
```

> 🗣️ *"The mobile app is on Angular 18 and the two web apps are on 16. We upgrade one major at a time and the real constraint is always the ecosystem — Ionic and PrimeNG dictate the pace more than Angular does."*

#### Easy memory

```
14 typed forms + inject() | 15 standalone stable | 16 SIGNALS + takeUntilDestroyed ⭐
17 @if/@for + @defer ⭐ | 18 zoneless (experimental) | 19 standalone by DEFAULT

Direction: away from zone.js and NgModules → toward SIGNALS + STANDALONE ⭐
"Migrate first, modernise second." ⭐ The blocker is usually a third-party lib.
```

---

## Quick-Fire Bonus

### Q: `declarations` vs `imports` vs `providers`?

```
declarations → MINE     (components/directives/pipes this module owns)
imports      → BORROWED (other modules whose EXPORTS I use)
providers    → SERVICES (registered with the injector)
exports      → SHARED   (what I let other modules use)
```

### Q: `forRoot` vs `forChild`?

```
RouterModule.forRoot(routes)  → ONCE, in the root module
                                → creates the Router SERVICE + the root routes ⭐
RouterModule.forChild(routes) → in feature/lazy modules
                                → adds routes WITHOUT recreating the router ⭐
```

⚠️ Calling `forRoot` twice creates a second Router service and breaks navigation.

### Q: What is the `inject()` function?

```typescript
private userService = inject(UserService);      // ⭐ no constructor needed
```

```
Works in: field initialisers, functional guards, functional interceptors, factories
Must run in an INJECTION CONTEXT ⚠️ (not inside ngOnInit or a callback)
```

#### 🔵 30 usages in `robogebra-mobile`.

### Q: What are environment files for?

```
environment.ts       → dev   (API URL, feature flags)
environment.prod.ts  → prod
```

The CLI swaps them via `fileReplacements` in `angular.json` at build time.

⚠️ **Never put a secret in an environment file** — it ships to the browser and anyone can read it.

### Q: How does Ionic relate to Angular in your experience?

> 🗣️ *"Ionic is a UI and native-bridge layer on top of Angular — its components, routing and lifecycle integrate with Angular's, so services, reactive forms and RxJS are all reused. On RoboGebra the mobile app is Ionic 8 with Capacitor 6 on Angular 18, with about nineteen native plugins including push notifications via Firebase, native Razorpay checkout, network detection and native `Preferences` storage. Ionic adds its own lifecycle events on top of Angular's — `ionViewWillEnter` fires on every entry, whereas `ngOnInit` only runs once because Ionic keeps pages alive in the navigation stack."*

⭐ That last sentence — **`ionViewWillEnter` vs `ngOnInit`** — is *the* Ionic + Angular question. → [15 — Ionic](./15-ionic-level1.md)

---

## Quick Revision Sheet

```
CORE
  Angular = TypeScript + COMPONENTS + one-way flow (a REWRITE of AngularJS)
  NgModule: declarations MINE | imports BORROWED | exports SHARED | providers SERVICES
  Standalone = no NgModule; your app: 2 usages ⚠️ "new code is standalone-first" ⭐
  core/ (once) | features/ (lazy) | shared/ (reusable)

TEMPLATES
  Lifecycle: constructor → ngOnChanges → ngOnInit ⭐ → ngDoCheck
             → AfterContentInit → AfterViewInit ⭐ → ngOnDestroy ⭐
  @Inputs NOT ready in the constructor ⭐ | @ViewChild only in AfterViewInit ⭐
  Binding: {{ }} | [prop] | (event) | [(banana in a box)] 🍌📦
  [(ngModel)] = [ngModel] + (ngModelChange); custom = @Input x + @Output xChange ⭐
  ng-template (not rendered until used) | ng-container (no extra DOM) ⭐
  trackBy ⭐ = stable id → REUSE DOM nodes (55 usages in your app)
  v17: @if / @for (track MANDATORY ⭐) / @defer

DI
  providedIn:'root' ⭐ = singleton + tree-shakable
  ⚠️ a service provided in a LAZY module = a SECOND instance 💥
  InjectionToken for non-class values (interfaces don't exist at runtime) ⭐

ROUTING
  ** LAST ⭐ | paramMap.subscribe, not snapshot, when reusing a component ⭐
  Guards: CanActivate ⭐ / CanDeactivate / Resolve; return true|false|UrlTree
  Your guard: async Observable + setRedirectUrl + ROLE-based routing ⭐
  Lazy: loadChildren / loadComponent + PreloadAllModules

FORMS
  Reactive ⭐ = logic in the CLASS = testable without the DOM
  FormControl | FormGroup | FormArray (dynamic) ⭐
  Validator returns NULL when VALID ⭐ | cross-field goes on the GROUP ⭐
  Formly = strategy/factory → a new field is CONFIG, not a release ⭐

RxJS
  Promise = 1, eager, uncancellable | Observable = many, LAZY, cancellable ⭐
  Subject (nothing) | BehaviorSubject (CURRENT ⭐) | ReplaySubject (last n)
  switchMap CANCEL ⭐ | mergeMap parallel | concatMap ORDER ⭐ | exhaustMap IGNORE ⭐
  async pipe ⭐⭐ > takeUntilDestroyed > takeUntil(destroy$) ⭐ (LAST operator!)
  catchError INSIDE the inner observable ⭐ or one error kills the stream 💥
  finalize = always runs (hide the spinner)

STATE
  BehaviorSubject services ⭐ (your app: 122; NgRx: 0 — a deliberate choice) ⭐
  NgRx: Action → Reducer (PURE) → Store → Selector (MEMOISED) + Effects

HTTP
  Lazy — no subscribe, NO request 💥
  Interceptor: req.clone() (IMMUTABLE ⭐) | FormData Content-Type trap ⭐⭐
               skip /auth 🐔🥚 | takeUntil mass-cancel | 401 → refresh
  status 0 = network/CORS ⭐

PERFORMANCE
  OnPush ⭐ = @Input REFERENCE change | internal event | async pipe | markForCheck
  ⚠️ MUTATION doesn't change the reference → the UI silently won't update 💥
  zone.js patches async APIs → signals → zoneless (your app: 0 signals — be honest ⭐)
  AOT = build-time, errors caught at build, no eval
  ⚠️ NEVER call a method in a template ⭐

TESTING
  Jasmine + Karma + TestBed | fixture.detectChanges() ⚠️
  HttpClientTestingModule + expectOne().flush() + verify() ⭐

MIGRATION
  ng update ONE major at a time ⭐ | "migrate first, modernise second" ⭐
  The blocker is usually a third-party library, not Angular.
```

---

**Related files:** [03 — TypeScript](./03-typescript.md) · [20 — RxJS Operators](./20-rxjs-operators.md) · [21 — NgRx](./21-ngrx.md) · [25 — Angular Binding & Forms](./25-angular-binding-forms.md) · [15 — Ionic](./15-ionic-level1.md) · [39 — RoboGebra Code Examples](./39-robogebra-code-examples.md) · [38 — IQVIA Prep](./38-iqvia-technical-lead-prep.md)
