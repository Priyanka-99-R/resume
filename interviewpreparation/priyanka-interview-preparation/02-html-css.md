# HTML & CSS — Interview Q&A

## HTML

### Q: What is semantic HTML and why does it matter?
Semantic HTML uses tags that describe the meaning of the content rather than just its appearance — `<header>`, `<nav>`, `<main>`, `<article>`, `<section>`, `<aside>`, `<footer>`, `<figure>`. It matters because:
- **Accessibility**: screen readers can navigate landmarks (e.g. jump to `<nav>` or `<main>`).
- **SEO**: search engines understand page structure and rank content better.
- **Maintainability**: code is self-documenting vs a sea of `<div>`s.

```html
<!-- Non-semantic -->
<div class="header"><div class="nav">...</div></div>

<!-- Semantic -->
<header><nav>...</nav></header>
```

### Q: What is the difference between block and inline elements?
- **Block** elements start on a new line and take the full available width; height/width and vertical margins/padding apply. Examples: `<div>`, `<p>`, `<h1>`, `<ul>`, `<section>`.
- **Inline** elements flow within text, take only as much width as their content, and ignore width/height and top/bottom margins. Examples: `<span>`, `<a>`, `<strong>`, `<img>` (inline-block-ish).
- **inline-block** flows inline but respects width/height/margins.

### Q: What is the difference between `<div>` and `<span>`?
Both are generic, non-semantic containers with no default styling. `<div>` is a **block** element used to group larger sections/layout; `<span>` is an **inline** element used to style or target a small piece of text inside a line.

```html
<div>A block container</div>
<p>Highlight <span class="warn">this word</span> only.</p>
```

### Q: What are `data-*` attributes?
Custom attributes to store extra data on an element without misusing class/id. Read in JS via `dataset` or in CSS via attribute selectors.

```html
<button data-user-id="42" data-role="admin">Edit</button>
```
```js
el.dataset.userId   // "42"
el.dataset.role     // "admin"
```
Commonly used to bind config to elements (e.g. analytics, widget options).

### Q: What are meta tags and which are important?
`<meta>` tags live in `<head>` and provide metadata about the document. Key ones:
- `charset` — character encoding.
- `viewport` — responsive scaling (essential for mobile).
- `description` — SEO snippet shown in search results.
- Open Graph (`og:title`, `og:image`) — social media previews.

```html
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="Concise page summary for SEO.">
```

### Q: What is the difference between `async` and `defer` on a script tag?
Both download the script without blocking HTML parsing. The difference is execution timing:
- **async**: executes as soon as it's downloaded, possibly before parsing finishes; order not guaranteed. Good for independent scripts (analytics).
- **defer**: executes only after HTML parsing completes, in document order. Good for scripts that depend on the DOM or each other.

```html
<script src="analytics.js" async></script>
<script src="app.js" defer></script>
```
Without either, the script blocks parsing while it downloads and runs.

### Q: localStorage vs sessionStorage vs cookies?
| Feature | localStorage | sessionStorage | cookies |
|---|---|---|---|
| Capacity | ~5–10 MB | ~5 MB | ~4 KB |
| Lifetime | Until cleared | Until tab closes | Set by `expires`/`max-age` |
| Sent to server | No | No | Yes, on every request |
| Scope | Per origin | Per tab | Per domain/path |

Use localStorage for persistent client-only data, sessionStorage for per-tab temporary data, cookies for auth tokens/server-read data (prefer `HttpOnly`, `Secure`, `SameSite`).

### Q: What are common HTML5 form input types?
`text`, `email`, `password`, `number`, `tel`, `url`, `date`, `time`, `color`, `range`, `checkbox`, `radio`, `file`, `search`. They give built-in validation and mobile keyboards.

```html
<input type="email" required>
<input type="number" min="1" max="10" step="1">
<input type="date">
```
Pair with `required`, `pattern`, `min`/`max` for native validation and always use `<label for>` for accessibility.

### Q: What are the basics of accessibility (ARIA)?
ARIA (Accessible Rich Internet Applications) adds semantics for assistive tech when native HTML isn't enough. Rules of thumb:
- **Use native elements first** (`<button>` over `<div role="button">`).
- `aria-label` / `aria-labelledby` name an element.
- `role` describes a widget's purpose.
- `aria-hidden="true"` hides decorative content from screen readers.
- `aria-live` announces dynamic updates.

```html
<button aria-label="Close dialog">&times;</button>
<div role="alert" aria-live="assertive">Saved!</div>
```

### Q: What is `<!DOCTYPE html>` and why is it needed?
It tells the browser to render in **standards mode** rather than quirks mode (legacy IE-compatible behavior). `<!DOCTYPE html>` is the short HTML5 doctype. Without it, the box model and CSS can behave inconsistently. It must be the very first line of the document.

### Q: What is an iframe and what are its concerns?
An `<iframe>` embeds another HTML document inside the current page (e.g. maps, videos, payment widgets). Concerns:
- **Security**: use the `sandbox` attribute to restrict capabilities and `allow` to whitelist features.
- **Performance**: each iframe loads a separate document; use `loading="lazy"`.
- **SEO**: content inside iframes isn't attributed to your page.

```html
<iframe src="https://example.com" sandbox="allow-scripts" loading="lazy" title="Embedded report"></iframe>
```

### Q: What are some on-page SEO basics?
- One descriptive `<title>` and a `<meta name="description">` per page.
- A single `<h1>`, with logical `<h2>`/`<h3>` hierarchy.
- Semantic structure and descriptive `alt` text on images.
- Clean, readable URLs and `<a>` text (avoid "click here").
- Canonical tags to avoid duplicate content, fast load times, mobile-friendliness, and structured data (schema.org).

## CSS

### Q: Explain the CSS box model.
Every element is a box with four layers, from inside out: **content** → **padding** → **border** → **margin**. Total rendered width depends on `box-sizing`:
- `content-box` (default): width = content only; padding/border add on top.
- `border-box`: width includes content + padding + border (more predictable).

```css
*, *::before, *::after { box-sizing: border-box; }
```

### Q: What is margin collapsing?
When vertical margins of adjacent or parent/child block elements touch, they **collapse** into a single margin equal to the larger value (not the sum). It happens between adjacent siblings, between empty blocks, and parent/first-child. It does **not** happen horizontally, with fl/grid items, or when separated by padding/border/overflow.

```css
/* two stacked <p> with margin 20px and 30px → gap is 30px, not 50px */
```

### Q: Explain CSS position values.
- **static**: default; normal flow, ignores top/left/etc.
- **relative**: offset from its normal position; still occupies original space; creates a positioning context.
- **absolute**: removed from flow; positioned relative to nearest positioned ancestor.
- **fixed**: removed from flow; positioned relative to the viewport; stays on scroll.
- **sticky**: hybrid — acts relative until a scroll threshold, then sticks like fixed within its container.

```css
.header { position: sticky; top: 0; }
```

### Q: What are the main CSS display types?
- `block` / `inline` / `inline-block` — flow behavior (see HTML section).
- `none` — removes element from layout entirely.
- `flex` — one-dimensional flexible layout.
- `grid` — two-dimensional layout.
- `table`, `contents` — less common specialized values.

### Q: How is CSS specificity calculated?
Specificity is a 4-part value (a, b, c, d):
- a = inline styles
- b = IDs
- c = classes, attributes, pseudo-classes
- d = elements, pseudo-elements

Higher wins; ties go to the later rule. The universal selector `*` adds nothing.

```css
#nav .item a   /* (0,1,1,1) */
.item a        /* (0,0,1,1) → loses */
```

### Q: What does `!important` do and when should you use it?
`!important` overrides normal specificity and cascade, forcing a declaration to win (an `!important` later in the cascade beats an earlier one). Avoid it — it breaks the natural cascade and is hard to override. Legitimate uses: overriding third-party/library styles (e.g. Bootstrap, Kendo) you can't change, or utility classes. Prefer raising specificity properly first.

### Q: Inline vs internal vs external CSS?
- **Inline**: `style` attribute on the element — highest specificity, not reusable, hard to maintain.
- **Internal**: `<style>` block in `<head>` — page-scoped, no extra request.
- **External**: `<link>` to a `.css` file — reusable, cacheable, best for real projects.

```html
<link rel="stylesheet" href="styles.css">
```

### Q: Difference between pseudo-classes and pseudo-elements?
- **Pseudo-class** (single colon `:`) targets a **state** of an element: `:hover`, `:focus`, `:nth-child()`, `:first-child`, `:checked`.
- **Pseudo-element** (double colon `::`) styles a **part** of an element or inserts generated content: `::before`, `::after`, `::first-line`, `::placeholder`.

```css
a:hover { color: blue; }
.card::before { content: "★"; }
```

### Q: em vs rem vs px vs % ?
- **px**: absolute, fixed size.
- **em**: relative to the **parent's** font-size (compounds when nested).
- **rem**: relative to the **root** (`<html>`) font-size — predictable, great for scalable typography/spacing.
- **%**: relative to the parent's corresponding dimension.

```css
html { font-size: 16px; }
.title { font-size: 2rem; }   /* 32px */
.box   { width: 50%; }        /* half of parent width */
```

### Q: What is z-index and stacking context?
`z-index` controls the front-to-back order of positioned/flex/grid items. It only works within the same **stacking context**. A new stacking context is created by, e.g., a positioned element with a `z-index`, `opacity < 1`, `transform`, `filter`, or `position: fixed`. A child's z-index can never escape its parent's stacking context, which is the usual cause of "z-index not working".

```css
.modal { position: fixed; z-index: 1000; }
```

## Layout

### Q: Explain Flexbox and its main properties.
Flexbox is a one-dimensional layout system (a row OR a column). On the container:
- `display: flex`
- `flex-direction` — row | column
- `justify-content` — alignment along the main axis
- `align-items` — alignment along the cross axis
- `flex-wrap` — wrap onto multiple lines
- `gap` — spacing between items

On items: `flex-grow`, `flex-shrink`, `flex-basis` (shorthand `flex`), `align-self`.

```css
.container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}
```

### Q: Explain CSS Grid basics.
Grid is a two-dimensional layout system (rows AND columns simultaneously). Define tracks on the container and place items into the grid.

```css
.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);  /* 3 equal columns */
  grid-template-rows: auto;
  gap: 20px;
}
.feature { grid-column: span 2; }  /* item spans 2 columns */
```
`fr` = fraction of available space; `repeat()`, `minmax()`, and `auto-fit/auto-fill` enable responsive grids.

### Q: Flexbox vs Grid — when to use which?
- **Flexbox**: one-dimensional layouts — navbars, toolbars, button groups, distributing items in a single row/column. Content-driven sizing.
- **Grid**: two-dimensional layouts — page layouts, card galleries, dashboards where you control rows and columns together. Layout-driven.

They compose well: use Grid for the overall page, Flexbox for components inside cells.

### Q: How do you center a div?
Multiple ways:

```css
/* Flexbox (most common) */
.parent { display: flex; justify-content: center; align-items: center; }

/* Grid */
.parent { display: grid; place-items: center; }

/* Absolute + transform */
.child {
  position: absolute; top: 50%; left: 50%;
  transform: translate(-50%, -50%);
}

/* Horizontal block centering */
.child { margin: 0 auto; width: 300px; }
```

## Responsive

### Q: What are media queries?
Media queries apply CSS conditionally based on device characteristics — width, height, orientation, resolution, or `prefers-color-scheme`. They're the core of responsive design.

```css
@media (max-width: 768px) {
  .sidebar { display: none; }
}
@media (min-width: 1024px) {
  .container { max-width: 960px; }
}
```

### Q: What is mobile-first design?
Write base styles for small screens first, then layer enhancements for larger screens using `min-width` media queries. Benefits: simpler base CSS, better performance on mobile, and progressive enhancement. (Bootstrap is mobile-first.)

```css
.card { width: 100%; }                 /* mobile default */
@media (min-width: 768px) {
  .card { width: 50%; }                /* tablet+ */
}
```

### Q: Why is the viewport meta tag needed?
Without it, mobile browsers render at a default ~980px width and scale down, so media queries don't trigger correctly. The viewport meta tells the browser to match the device's width and set initial zoom.

```html
<meta name="viewport" content="width=device-width, initial-scale=1.0">
```

### Q: What are responsive units?
Units that scale with context instead of being fixed:
- `%` — relative to parent.
- `rem`/`em` — relative to font-size.
- `vw`/`vh` — 1% of viewport width/height.
- `vmin`/`vmax` — relative to smaller/larger viewport dimension.
- `clamp(min, preferred, max)` — fluid sizing with bounds.

```css
.title { font-size: clamp(1.5rem, 4vw, 3rem); }
```

### Q: How does the Bootstrap grid system work?
Bootstrap uses a 12-column, flexbox-based, mobile-first grid:
- `.container` (or `.container-fluid`) wraps the grid.
- `.row` is a flex row of columns.
- `.col-*` define widths; columns in a row should add up to 12.
- Responsive breakpoint infixes: `col-sm-`, `col-md-`, `col-lg-`, `col-xl-`, `col-xxl-`.

```html
<div class="container">
  <div class="row">
    <div class="col-12 col-md-8">Main</div>
    <div class="col-12 col-md-4">Sidebar</div>
  </div>
</div>
```
On mobile each is full width (12); from `md` up, they split 8/4. Use `g-*` for gutters and offset/order utilities for fine control.

## Modern / Advanced

### Q: What are CSS variables (custom properties)?
Reusable values declared with `--name` and read with `var()`. Unlike preprocessor variables, they're live in the DOM, cascade, inherit, and can be changed at runtime via JS or media queries — great for theming.

```css
:root {
  --primary: #0d6efd;
  --space: 8px;
}
.btn { background: var(--primary); padding: var(--space); }
```
```js
document.documentElement.style.setProperty('--primary', '#198754');
```

### Q: Transitions vs animations?
- **Transitions** animate a property between two states, triggered by a change (e.g. `:hover`). Simple, one-shot.
- **Animations** use `@keyframes` for multi-step, looping, or self-starting motion with fine control.

```css
.btn { transition: background 0.3s ease; }

@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.5; } }
.loader { animation: pulse 1s infinite; }
```

### Q: What is the CSS transform property?
`transform` applies 2D/3D visual changes — `translate`, `scale`, `rotate`, `skew` — without affecting document flow (no reflow). It's GPU-accelerated and composited, so it's the performant way to animate position/size.

```css
.card:hover { transform: scale(1.05) rotate(2deg); }
```

### Q: opacity vs visibility vs display:none — differences?
| Property | In layout? | Clickable? | Animatable? |
|---|---|---|---|
| `opacity: 0` | Yes (space kept) | Yes | Yes |
| `visibility: hidden` | Yes (space kept) | No | Limited |
| `display: none` | No (removed) | No | No |

Use `display:none` to fully remove, `visibility:hidden` to hide but keep space, `opacity:0` for fade animations.

### Q: What is BEM naming?
BEM (Block, Element, Modifier) is a CSS naming convention for predictable, scoped class names:
- **Block** — standalone component: `.card`
- **Element** — a part of the block (double underscore): `.card__title`
- **Modifier** — a variant (double hyphen): `.card--featured`

```html
<div class="card card--featured">
  <h2 class="card__title">...</h2>
</div>
```
It keeps specificity flat and avoids naming collisions.

### Q: What causes reflow and repaint?
- **Reflow (layout)**: recalculating element geometry/positions. Triggered by changing width/height, adding/removing DOM nodes, font changes, or reading layout properties like `offsetHeight`. Expensive.
- **Repaint**: redrawing pixels without layout changes — e.g. `color`, `background`, `visibility`.

Minimize reflows: batch DOM changes, animate `transform`/`opacity` (compositor-only) instead of `top`/`width`, and avoid layout thrashing (read-then-write loops).

### Q: What is the critical rendering path?
The sequence the browser follows to turn HTML/CSS/JS into pixels:
1. Parse HTML → **DOM**.
2. Parse CSS → **CSSOM**.
3. Combine into the **Render Tree**.
4. **Layout** (compute geometry).
5. **Paint** and **Composite**.

CSS is render-blocking and JS can block parsing, so optimize by minimizing/inlining critical CSS, deferring non-critical JS (`async`/`defer`), and reducing resource sizes to render faster.
