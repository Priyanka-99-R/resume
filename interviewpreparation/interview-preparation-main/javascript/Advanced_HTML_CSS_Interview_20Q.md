# 20 Advanced HTML & CSS Interview Questions & Answers
### For 5+ Years Senior Full-Stack / Frontend Developer
### Target: PwC, Deloitte, Cognizant, Capgemini, Product Companies

---

## SECTION 1: HTML — Semantics, Accessibility & APIs

---

## Q1. What is Semantic HTML? Why does it matter for SEO and Accessibility?

**Summary:**
Semantic HTML uses elements that describe their meaning — `<header>`, `<nav>`, `<main>`, `<article>`, `<section>`, `<aside>`, `<footer>` — instead of generic `<div>` and `<span>` for everything. It matters because screen readers, search engines, and browsers use these tags to understand page structure. It's not optional — it's a production requirement.

**Non-Semantic vs Semantic:**
```html
<!-- ❌ DIV SOUP — means nothing to screen readers or Google -->
<div class="header">
  <div class="nav">
    <div class="nav-item">Home</div>
    <div class="nav-item">About</div>
  </div>
</div>
<div class="content">
  <div class="article">
    <div class="title">SOLID Principles</div>
    <div class="text">...</div>
  </div>
  <div class="sidebar">...</div>
</div>
<div class="footer">...</div>

<!-- ✅ SEMANTIC — browser, screen reader, and Google all understand this -->
<header>
  <nav aria-label="Main navigation">
    <ul>
      <li><a href="/">Home</a></li>
      <li><a href="/about">About</a></li>
    </ul>
  </nav>
</header>
<main>
  <article>
    <h1>SOLID Principles</h1>
    <p>...</p>
  </article>
  <aside aria-label="Related content">...</aside>
</main>
<footer>
  <p>&copy; 2026 Company Name</p>
</footer>
```

**Semantic Elements Cheat Sheet:**

| Element | Purpose | Replaces |
|---------|---------|----------|
| `<header>` | Page or section header | `<div class="header">` |
| `<nav>` | Navigation links | `<div class="nav">` |
| `<main>` | Primary content (one per page) | `<div class="content">` |
| `<article>` | Self-contained content (blog post, card) | `<div class="post">` |
| `<section>` | Thematic group of content | `<div class="section">` |
| `<aside>` | Sidebar, related content | `<div class="sidebar">` |
| `<footer>` | Page or section footer | `<div class="footer">` |
| `<figure>` / `<figcaption>` | Image with caption | `<div class="image-wrapper">` |
| `<time>` | Date/time | `<span class="date">` |
| `<mark>` | Highlighted/relevant text | `<span class="highlight">` |
| `<details>` / `<summary>` | Collapsible content | Custom JS accordion |

**Why It Matters:**

```
SEO:
  Google parses <article>, <h1>-<h6>, <nav> to understand page structure
  → Better ranking for well-structured pages
  → Featured snippets pull from <article> and <h1>

Accessibility:
  Screen readers announce: "Navigation, 5 items" for <nav>
  Users can jump to <main> content directly (skip nav)
  → 15% of users rely on assistive technology

Developer Experience:
  Semantic HTML is self-documenting
  → <nav> is clearer than <div class="nav-wrapper-outer">
```

**Real-World Scenario:**
In an e-commerce project, our product pages used divs for everything. Google wasn't showing rich snippets, and Lighthouse accessibility score was 42. After migrating to semantic HTML (`<article>` for products, `<nav>` for breadcrumbs, proper heading hierarchy), the accessibility score jumped to 95 and we saw a 15% improvement in organic search clicks from rich snippets.

**Closing:**
Semantic HTML is the foundation of professional web development. It's not about being "correct" — it's about SEO ranking, accessibility compliance (WCAG), and code readability. I use semantic elements first and only fall back to `<div>`/`<span>` for purely presentational wrappers.

---

## Q2. What are HTML5 APIs that senior developers should know? (Storage, Web Workers, WebSocket, etc.)

**Summary:**
HTML5 isn't just markup — it introduced powerful browser APIs. The ones I use regularly: Web Storage (localStorage/sessionStorage), Web Workers for background processing, WebSocket for real-time communication, Intersection Observer for lazy loading, and the History API for SPA routing.

**Key HTML5 APIs:**

**1. Web Storage — localStorage vs sessionStorage:**
```javascript
// localStorage — persists until cleared (across tabs, sessions)
localStorage.setItem('theme', 'dark');
localStorage.getItem('theme');  // 'dark'
localStorage.removeItem('theme');

// sessionStorage — cleared when tab closes
sessionStorage.setItem('formDraft', JSON.stringify(formData));

// Limits: ~5-10MB per origin, synchronous (blocks main thread)
// ⚠️ Never store sensitive data (tokens, passwords) — accessible to XSS
```

| Feature | localStorage | sessionStorage | Cookies |
|---------|-------------|----------------|---------|
| Persistence | Until cleared | Tab session | Expiry date |
| Size limit | ~5-10 MB | ~5-10 MB | ~4 KB |
| Sent with requests | No | No | Yes (automatic) |
| Accessible from JS | Yes | Yes | Yes (unless HttpOnly) |
| Scope | Same origin, all tabs | Same origin, same tab | Same origin + path |

**2. Intersection Observer — Lazy Loading & Infinite Scroll:**
```javascript
// Lazy load images when they enter viewport
const observer = new IntersectionObserver((entries) => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      const img = entry.target;
      img.src = img.dataset.src;  // load actual image
      observer.unobserve(img);     // stop observing
    }
  });
}, { rootMargin: '200px' });  // start loading 200px before viewport

document.querySelectorAll('img[data-src]').forEach(img => {
  observer.observe(img);
});
```

**3. History API — SPA Navigation:**
```javascript
// Push state without page reload (how Angular Router works internally)
history.pushState({ page: 'products' }, '', '/products');

// Listen for back/forward button
window.addEventListener('popstate', (event) => {
  console.log('Navigated to:', event.state);
  renderPage(event.state.page);
});
```

**4. Geolocation API:**
```javascript
navigator.geolocation.getCurrentPosition(
  (position) => {
    console.log(position.coords.latitude, position.coords.longitude);
  },
  (error) => console.error(error),
  { enableHighAccuracy: true, timeout: 5000 }
);
```

**5. Broadcast Channel — Cross-Tab Communication:**
```javascript
// Tab 1: send logout event
const channel = new BroadcastChannel('auth');
channel.postMessage({ type: 'logout' });

// Tab 2: listen and react
const channel = new BroadcastChannel('auth');
channel.onmessage = (event) => {
  if (event.data.type === 'logout') {
    window.location.href = '/login';  // logout all tabs
  }
};
```

**Closing:**
These APIs are the building blocks that frameworks abstract away. Angular Router uses the History API. `@defer (on viewport)` uses Intersection Observer. Understanding them helps debug framework behavior and build custom solutions when needed.

---

## Q3. What is the difference between `<script>`, `<script async>`, and `<script defer>`?

**Summary:**
Default `<script>` blocks HTML parsing while it downloads and executes. `async` downloads in parallel but executes immediately (blocking parsing briefly). `defer` downloads in parallel and executes after HTML is fully parsed. I use `defer` for most scripts and `async` for independent scripts like analytics.

**Visual Timeline:**
```
HTML Parsing:  ████████████████████████████████████████

<script>
HTML:          ████████──────────────█████████████████
Script:                 ↓download↓↓exec↓
               Parsing STOPS     Then resumes
               ❌ Blocks parsing during download AND execution

<script async>
HTML:          ██████████████████──────██████████████
Script:            ↓ download ↓    ↓exec↓
               Downloads in parallel, pauses parsing only for execution
               ⚠️ Execution order NOT guaranteed

<script defer>
HTML:          ████████████████████████████████████ → DOMContentLoaded
Script:            ↓ download ↓                   ↓exec↓
               Downloads in parallel, executes AFTER parsing
               ✅ Execution order IS guaranteed (in document order)
```

**When to Use Each:**
```html
<!-- defer — most scripts (app bundles, frameworks) -->
<!-- Executes after DOM is ready, maintains order -->
<script defer src="vendor.js"></script>
<script defer src="app.js"></script>
<!-- vendor.js ALWAYS runs before app.js -->

<!-- async — independent scripts (analytics, ads) -->
<!-- Don't depend on DOM or other scripts -->
<script async src="analytics.js"></script>
<script async src="ad-tracker.js"></script>
<!-- Execution order NOT guaranteed — that's fine for analytics -->

<!-- inline scripts at end of body — legacy approach -->
<body>
  ...content...
  <script>
    // Only option for inline scripts that need DOM
    // defer/async don't apply to inline scripts
  </script>
</body>

<!-- type="module" — automatically deferred -->
<script type="module" src="app.js"></script>
<!-- ES modules are deferred by default -->
```

**Decision Guide:**

| Script Type | Use | Example |
|-------------|-----|---------|
| `defer` | App code that needs DOM + order | Angular bundle, jQuery + plugins |
| `async` | Independent, no DOM dependency | Google Analytics, error tracking |
| `type="module"` | ES modules | Modern app entry point |
| No attribute (inline) | Critical inline code | CSS critical path, config |

**Closing:**
`defer` is my default for all external scripts — it downloads in parallel and executes in order after the DOM is ready. `async` only for truly independent scripts. Never use a bare `<script>` in `<head>` without `defer` or `async` — it blocks the entire page rendering.

---

## Q4. What is Accessibility (a11y)? How do you build accessible web applications?

**Summary:**
Accessibility (a11y) means building websites that everyone can use — including people with visual, motor, auditory, and cognitive disabilities. It's not optional charity work — it's a legal requirement in many countries (ADA, WCAG 2.1 AA). In practice, it means semantic HTML, proper ARIA, keyboard navigation, color contrast, and focus management.

**The Four WCAG Principles (POUR):**
```
P — Perceivable:  Content visible/hearable (alt text, captions, contrast)
O — Operable:     Navigable by keyboard and assistive tech (focus, no traps)
U — Understandable: Clear language, predictable behavior, error guidance
R — Robust:       Works with current and future assistive technologies
```

**Practical Implementation:**

**1. Images — Always have alt text:**
```html
<!-- Informative image — describe what it shows -->
<img src="chart.png" alt="Bar chart showing Q4 revenue increased 23% year-over-year" />

<!-- Decorative image — empty alt to skip it -->
<img src="divider.png" alt="" role="presentation" />

<!-- Complex image — use figcaption -->
<figure>
  <img src="architecture.png" alt="Microservices architecture diagram" />
  <figcaption>System architecture showing 5 services communicating via Kafka</figcaption>
</figure>
```

**2. Keyboard Navigation — Everything must be keyboard accessible:**
```html
<!-- ✅ Natively focusable — no extra work needed -->
<button>Submit</button>
<a href="/about">About</a>
<input type="text" />

<!-- ❌ NOT focusable by default — needs tabindex -->
<div class="card" onclick="openCard()">...</div>

<!-- ✅ Fixed — made focusable and interactive -->
<div class="card"
     tabindex="0"
     role="button"
     (click)="openCard()"
     (keydown.enter)="openCard()"
     (keydown.space)="openCard()">
  ...
</div>

<!-- ✅ BEST — just use a button! -->
<button class="card" (click)="openCard()">...</button>
```

**3. ARIA Attributes — When HTML isn't enough:**
```html
<!-- Live regions — announce dynamic content to screen readers -->
<div aria-live="polite" aria-atomic="true">
  {{ statusMessage }}
  <!-- Screen reader announces: "Order placed successfully" -->
</div>

<!-- Labels for form controls -->
<label for="email">Email Address</label>
<input id="email" type="email" aria-required="true" />

<!-- Or use aria-label when no visible label -->
<button aria-label="Close dialog">×</button>

<!-- Describe relationships -->
<input id="password" type="password" aria-describedby="pwd-hint" />
<small id="pwd-hint">Must be at least 8 characters</small>

<!-- Navigation landmarks -->
<nav aria-label="Main navigation">...</nav>
<nav aria-label="Footer links">...</nav>
<!-- Screen reader differentiates: "Main navigation" vs "Footer links" -->
```

**4. Focus Management — Modals, SPAs, Dynamic Content:**
```javascript
// When opening a modal — move focus into it
openModal() {
  this.dialog.nativeElement.showModal();
  this.dialog.nativeElement.querySelector('input')?.focus();
}

// When closing — return focus to trigger element
closeModal() {
  this.dialog.nativeElement.close();
  this.triggerButton.nativeElement.focus(); // return focus
}

// Skip Navigation Link — essential for keyboard users
<a href="#main-content" class="skip-link">Skip to main content</a>
// CSS: visually hidden until focused
.skip-link {
  position: absolute;
  left: -9999px;
}
.skip-link:focus {
  left: 10px;
  top: 10px;
  z-index: 9999;
}
```

**5. Color Contrast — WCAG AA Minimum:**
```css
/* WCAG AA requires: */
/* Normal text: 4.5:1 contrast ratio */
/* Large text (18px+ bold or 24px+): 3:1 contrast ratio */

/* ❌ FAIL — light gray on white (ratio ~2:1) */
.low-contrast { color: #aaa; background: #fff; }

/* ✅ PASS — dark gray on white (ratio ~7:1) */
.good-contrast { color: #333; background: #fff; }

/* Don't rely on color alone */
/* ❌ Red text for errors (colorblind users can't see it) */
/* ✅ Red text + icon + label for errors */
.error {
  color: #d32f2f;
  &::before { content: '⚠ '; }
}
```

**Accessibility Testing Checklist:**
```
□ Run Lighthouse Accessibility audit (target: 90+)
□ Navigate entire page with keyboard only (Tab, Enter, Escape, Arrow keys)
□ Test with screen reader (VoiceOver on Mac, NVDA on Windows)
□ Check color contrast with DevTools or axe extension
□ Verify all images have appropriate alt text
□ Ensure focus is visible on all interactive elements
□ Test at 200% zoom — content shouldn't break
□ Install axe DevTools extension for automated checks
```

**Closing:**
Accessibility is not a feature — it's a quality requirement like security or performance. I use semantic HTML first, add ARIA only when HTML semantics aren't sufficient, test with keyboard navigation, and run Lighthouse audits. The rule is: if you can't `Tab` to it and `Enter` to activate it, it's broken.

---

## Q5. What are `<meta>` tags and how do they impact SEO and mobile responsiveness?

**Summary:**
Meta tags provide metadata to browsers, search engines, and social media platforms. The viewport meta tag is essential for responsive design. Open Graph tags control social media previews. Robots meta controls search indexing. Getting these right is the difference between a professional and amateur web page.

**Essential Meta Tags:**
```html
<head>
  <!-- Character encoding — always first -->
  <meta charset="UTF-8" />

  <!-- Viewport — REQUIRED for responsive design -->
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />

  <!-- SEO — title and description -->
  <title>SOLID Principles — Senior Java Interview Guide</title>
  <meta name="description"
        content="Master SOLID principles with real-world Java examples for 5+ years interview preparation." />

  <!-- Robots — control search engine behavior -->
  <meta name="robots" content="index, follow" />
  <!-- noindex, nofollow — hide from search engines -->
  <!-- noindex, follow — don't index but follow links -->

  <!-- Open Graph — Facebook, LinkedIn previews -->
  <meta property="og:title" content="SOLID Principles Interview Guide" />
  <meta property="og:description" content="Real-world Java examples..." />
  <meta property="og:image" content="https://example.com/og-image.jpg" />
  <meta property="og:url" content="https://example.com/solid-principles" />
  <meta property="og:type" content="article" />

  <!-- Twitter Card -->
  <meta name="twitter:card" content="summary_large_image" />
  <meta name="twitter:title" content="SOLID Principles" />
  <meta name="twitter:image" content="https://example.com/twitter-image.jpg" />

  <!-- Theme color — browser address bar color on mobile -->
  <meta name="theme-color" content="#1976d2" />

  <!-- Canonical — prevent duplicate content issues -->
  <link rel="canonical" href="https://example.com/solid-principles" />

  <!-- Security -->
  <meta http-equiv="Content-Security-Policy"
        content="default-src 'self'; script-src 'self'" />
</head>
```

**Viewport Tag — Why It Matters:**
```html
<!-- WITHOUT viewport meta tag: -->
<!-- Mobile browser renders page at desktop width (980px) -->
<!-- User sees tiny, zoomed-out page — unusable on phone -->

<!-- WITH viewport meta tag: -->
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<!-- Browser width matches device width — responsive CSS works correctly -->
<!-- This single tag is what makes media queries functional on mobile -->
```

**Closing:**
Meta tags are the bridge between your HTML and the outside world — search engines, social platforms, and mobile browsers. The viewport tag enables responsive design, Open Graph controls how links appear when shared, and the description tag is your elevator pitch to Google.

---

## SECTION 2: CSS — Layout, Modern Features & Architecture

---

## Q6. Explain the CSS Box Model. What is the difference between `content-box` and `border-box`?

**Summary:**
Every HTML element is a box with four layers: content, padding, border, and margin. `content-box` (default) means width/height applies only to the content — padding and border add to the total size. `border-box` includes padding and border in the declared width/height. I always use `border-box` globally — it makes layout math intuitive.

**The Box Model:**
```
┌───────────────────────────────────────────┐
│                 MARGIN                     │ ← Space between elements (transparent)
│  ┌─────────────────────────────────────┐  │
│  │             BORDER                   │  │ ← Visible border
│  │  ┌───────────────────────────────┐  │  │
│  │  │          PADDING               │  │  │ ← Space inside border
│  │  │  ┌─────────────────────────┐  │  │  │
│  │  │  │       CONTENT            │  │  │  │ ← Actual content (text, images)
│  │  │  │     width × height       │  │  │  │
│  │  │  └─────────────────────────┘  │  │  │
│  │  └───────────────────────────────┘  │  │
│  └─────────────────────────────────────┘  │
└───────────────────────────────────────────┘
```

**content-box vs border-box:**
```css
/* content-box (DEFAULT) — width = content only */
.box-content {
  box-sizing: content-box;
  width: 200px;
  padding: 20px;
  border: 5px solid;
  /* Actual rendered width: 200 + 20 + 20 + 5 + 5 = 250px */
  /* You said 200px but it's actually 250px — confusing! */
}

/* border-box — width = content + padding + border */
.box-border {
  box-sizing: border-box;
  width: 200px;
  padding: 20px;
  border: 5px solid;
  /* Actual rendered width: 200px (padding and border fit inside) */
  /* Content area: 200 - 20 - 20 - 5 - 5 = 150px */
  /* You said 200px, it IS 200px — predictable! */
}
```

**Global Reset — Every Project:**
```css
/* Apply border-box to everything — industry standard */
*, *::before, *::after {
  box-sizing: border-box;
}

/* Now width: 50% means 50% of parent — including padding and border */
/* No more "why is my element overflowing?" debugging */
```

**Why border-box Matters in Real Layouts:**
```css
/* Without border-box — layout breaks */
.column {
  width: 50%;
  padding: 20px;
  /* Actual width: 50% + 40px → overflows parent! */
}

/* With border-box — layout works */
.column {
  box-sizing: border-box;
  width: 50%;
  padding: 20px;
  /* Actual width: 50% exactly. Padding fits inside. */
}
```

**Closing:**
`border-box` is the first thing I set in every project. It makes the box model intuitive — `width: 200px` means 200px total, not 200px plus surprise padding and border. The default `content-box` is a legacy behavior that causes layout bugs.

---

## Q7. Flexbox vs Grid: When do you use each? Explain with real layout examples.

**Summary:**
Flexbox is one-dimensional — it lays out items in a row OR a column. Grid is two-dimensional — it controls rows AND columns simultaneously. I use Flexbox for component-level alignment (navbar, card content, button groups) and Grid for page-level layouts (entire page structure, dashboards, galleries).

**The Decision:**
```
Need to align items in ONE direction (row or column)?
  → Flexbox

Need to control BOTH rows and columns at once?
  → Grid

Building a nav bar, card footer, centering content?
  → Flexbox

Building a page layout, dashboard, image gallery?
  → Grid
```

**Flexbox — One Direction:**
```css
/* Navbar — horizontal alignment */
.navbar {
  display: flex;
  justify-content: space-between;  /* spread items across row */
  align-items: center;             /* vertically center */
  gap: 1rem;
}

/* Card content — vertical stack with footer pushed down */
.card {
  display: flex;
  flex-direction: column;
  height: 300px;
}
.card-body { flex: 1; }      /* grows to fill available space */
.card-footer { flex: none; }  /* stays at bottom, natural height */

/* Perfect centering */
.center {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}
```

**Grid — Two Dimensions:**
```css
/* Page layout — header, sidebar, main, footer */
.page {
  display: grid;
  grid-template-areas:
    "header  header"
    "sidebar main"
    "footer  footer";
  grid-template-columns: 250px 1fr;
  grid-template-rows: auto 1fr auto;
  min-height: 100vh;
}

.header  { grid-area: header; }
.sidebar { grid-area: sidebar; }
.main    { grid-area: main; }
.footer  { grid-area: footer; }

/* Responsive — collapse to single column on mobile */
@media (max-width: 768px) {
  .page {
    grid-template-areas:
      "header"
      "main"
      "sidebar"
      "footer";
    grid-template-columns: 1fr;
  }
}
```

```css
/* Dashboard — responsive card grid */
.dashboard {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  /* Auto-fills columns: 1 on mobile, 2 on tablet, 3+ on desktop */
  /* No media queries needed! */
}

/* Image gallery with featured item */
.gallery {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: repeat(2, 200px);
  gap: 0.5rem;
}
.gallery .featured {
  grid-column: span 2;
  grid-row: span 2;
}
```

**Comparison:**

| Feature | Flexbox | Grid |
|---------|---------|------|
| Direction | 1D (row OR column) | 2D (row AND column) |
| Best for | Component internals | Page layouts |
| Content-driven | Yes (items dictate layout) | No (grid dictates placement) |
| Gap support | Yes | Yes |
| Overlap items | Not easily | Easy with grid areas |
| Auto-fill responsive | No | `auto-fit` + `minmax()` |

**Real-World Scenario:**
In a dashboard project, I used Grid for the overall page layout (sidebar + main area) and the card grid (`auto-fit` for responsive columns). Inside each card, I used Flexbox for the card header (icon + title + action button in a row) and card body (vertical stack with footer pushed to bottom). Grid for macro layout, Flexbox for micro alignment.

**Closing:**
Grid and Flexbox aren't competing — they complement each other. Grid for page structure and multi-dimensional layouts, Flexbox for alignment within components. In modern CSS, I use both in every project.

---

## Q8. How do CSS Media Queries work? What is a Mobile-First approach?

**Summary:**
Media queries apply CSS rules conditionally based on viewport width, device type, orientation, or user preferences. Mobile-first means writing base styles for mobile, then adding complexity for larger screens with `min-width` queries. It produces smaller CSS for mobile (which loads the base) and is the industry standard.

**Mobile-First vs Desktop-First:**
```css
/* ❌ DESKTOP-FIRST — starts wide, overrides for mobile */
.container {
  width: 1200px;           /* desktop default */
  display: grid;
  grid-template-columns: 250px 1fr 250px;
}

@media (max-width: 768px) {
  .container {
    width: 100%;            /* override everything for mobile */
    grid-template-columns: 1fr;
  }
}

/* ✅ MOBILE-FIRST — starts simple, enhances for desktop */
.container {
  width: 100%;              /* mobile default — simple */
}

@media (min-width: 768px) {
  .container {
    max-width: 1200px;     /* tablet: add max-width */
    margin: 0 auto;
  }
}

@media (min-width: 1024px) {
  .container {
    display: grid;          /* desktop: add grid */
    grid-template-columns: 250px 1fr 250px;
  }
}
```

**Standard Breakpoints:**
```css
/* Mobile: 0 - 767px (base styles, no query needed) */

/* Tablet */
@media (min-width: 768px) { }

/* Desktop */
@media (min-width: 1024px) { }

/* Large desktop */
@media (min-width: 1440px) { }
```

**Modern Media Queries — User Preferences:**
```css
/* Dark mode */
@media (prefers-color-scheme: dark) {
  :root {
    --bg: #1a1a1a;
    --text: #e0e0e0;
  }
}

/* Reduced motion — respect user's OS setting */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}

/* Hover capability — touch vs mouse */
@media (hover: hover) {
  .card:hover { transform: scale(1.02); }
  /* Only apply hover effects on devices that support hover */
}

/* Container Queries (CSS 2023) — query parent, not viewport */
.card-container {
  container-type: inline-size;
}

@container (min-width: 400px) {
  .card { flex-direction: row; }  /* horizontal layout when container is wide */
}

@container (max-width: 399px) {
  .card { flex-direction: column; }  /* vertical layout when container is narrow */
}
```

**Closing:**
Mobile-first is not a preference — it's a methodology that results in better performance (mobile loads less CSS) and simpler progressive enhancement. Container queries are the next evolution — responsive to the component's container, not the viewport.

---

## Q9. Explain CSS Specificity. How does the cascade determine which styles apply?

**Summary:**
Specificity is how the browser decides which CSS rule wins when multiple rules target the same element. It's calculated as a weight system: inline styles > IDs > classes/attributes > elements. Understanding this prevents `!important` abuse and makes debugging style conflicts trivial.

**Specificity Calculation:**
```
Format: (Inline, ID, Class, Element)

/* Element selectors — (0, 0, 0, 1) */
p { }                           /* 0,0,0,1 */
div p { }                       /* 0,0,0,2 */

/* Class, attribute, pseudo-class — (0, 0, 1, 0) */
.card { }                       /* 0,0,1,0 */
[type="text"] { }               /* 0,0,1,0 */
:hover { }                      /* 0,0,1,0 */
.card.active { }                /* 0,0,2,0 */
.sidebar .card:hover { }        /* 0,0,3,0 */

/* ID selectors — (0, 1, 0, 0) */
#header { }                     /* 0,1,0,0 */
#header .nav .link { }          /* 0,1,2,1 */

/* Inline styles — (1, 0, 0, 0) */
<div style="color: red">        /* 1,0,0,0 */

/* !important — overrides everything (avoid!) */
.text { color: red !important; } /* Wins over inline styles */
```

**Specificity Examples — Which Wins?**
```css
/* Rule 1 */ .card .title { color: blue; }    /* 0,0,2,0 */
/* Rule 2 */ .card h2 { color: green; }       /* 0,0,1,1 */
/* Rule 3 */ h2.title { color: red; }         /* 0,0,1,1 */

/* On <div class="card"><h2 class="title">Hello</h2></div> */
/* Rule 1 wins: 0,0,2,0 > 0,0,1,1 */
/* Rule 2 vs Rule 3: same specificity → Rule 3 wins (appears later in CSS) */
```

**The Cascade Order (when specificity is equal):**
```
1. Source order: Last rule wins
2. Specificity: Higher specificity wins
3. Origin: Author styles > user styles > browser defaults
4. !important: Reverses origin order
5. @layer: Lower-priority layers lose to higher-priority layers

/* CSS Layers (modern) — control cascade priority */
@layer base, components, utilities;

@layer base {
  p { color: black; }
}

@layer utilities {
  .text-red { color: red; } /* Wins over base because utilities layer is later */
}
```

**Best Practices:**
```css
/* ✅ Keep specificity low and consistent */
.card { }
.card-header { }
.card-title { }
/* All at (0,0,1,0) — easy to override, predictable */

/* ❌ Avoid specificity wars */
#sidebar .widget .list > li > a.active:hover { }
/* (0,1,4,2) — nightmare to override */

/* ❌ NEVER use !important unless overriding third-party CSS */
.my-class { color: red !important; }
/* Now the only way to override is ANOTHER !important — escalation spiral */
```

**`:where()` and `:is()` — Specificity Control:**
```css
/* :is() — takes the highest specificity of its arguments */
:is(.card, #hero) .title { }  /* Specificity of #hero applies: 0,1,1,0 */

/* :where() — ZERO specificity (always) */
:where(.card, #hero) .title { }  /* Specificity: 0,0,0,1 */
/* Great for resets and defaults that are easy to override */
```

**Closing:**
Keep specificity low and flat — one or two class selectors maximum. Never use IDs for styling. Never use `!important` (except overriding third-party libraries). Modern CSS with `@layer` and `:where()` gives explicit cascade control without specificity wars.

---

## Q10. What are CSS Custom Properties (Variables)? How are they different from SCSS variables?

**Summary:**
CSS Custom Properties are native variables that cascade, can be overridden per element/media query, and are accessible via JavaScript. SCSS variables are compile-time only — they're replaced with static values during build. CSS variables are dynamic and runtime — that's the key difference.

**CSS Custom Properties:**
```css
/* Define at :root for global scope */
:root {
  --primary: #1976d2;
  --secondary: #424242;
  --spacing-sm: 0.5rem;
  --spacing-md: 1rem;
  --spacing-lg: 2rem;
  --border-radius: 8px;
  --font-body: 'Inter', system-ui, sans-serif;
}

/* Use with var() */
.button {
  background: var(--primary);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius);
  font-family: var(--font-body);
}

/* Fallback values */
.card {
  background: var(--card-bg, #ffffff);
  /* Uses #ffffff if --card-bg is not defined */
}
```

**Dynamic Theming — Where CSS Variables Shine:**
```css
/* Light theme (default) */
:root {
  --bg: #ffffff;
  --text: #1a1a1a;
  --surface: #f5f5f5;
  --primary: #1976d2;
}

/* Dark theme — override at runtime */
[data-theme="dark"] {
  --bg: #121212;
  --text: #e0e0e0;
  --surface: #1e1e1e;
  --primary: #90caf9;
}

/* Components use variables — theme switch is instant, no reload */
body { background: var(--bg); color: var(--text); }
.card { background: var(--surface); }
.button { background: var(--primary); }

/* Toggle theme with one attribute change */
document.documentElement.setAttribute('data-theme', 'dark');
// EVERY component updates instantly — zero CSS changes
```

**CSS Variables vs SCSS Variables:**

| Feature | CSS Variables | SCSS Variables |
|---------|-------------|----------------|
| Runtime? | Yes (live in browser) | No (compiled away) |
| Cascade | Yes (inherited, overridable) | No (flat substitution) |
| JS access | Yes (`getComputedStyle`) | No |
| Media query override | Yes | No |
| Scoped to element | Yes | No |
| Calculated at | Runtime | Build time |

```css
/* CSS variable responds to media queries — SCSS can't do this */
:root {
  --columns: 1;
}

@media (min-width: 768px) {
  :root { --columns: 2; }
}

@media (min-width: 1024px) {
  :root { --columns: 3; }
}

.grid {
  grid-template-columns: repeat(var(--columns), 1fr);
}
```

**JavaScript Access:**
```javascript
// Read
getComputedStyle(document.documentElement).getPropertyValue('--primary');

// Write — instant theme change
document.documentElement.style.setProperty('--primary', '#ff5722');
```

**Closing:**
CSS variables are runtime, dynamic, and cascade — SCSS variables are compile-time and static. I use CSS variables for theming, spacing scales, and anything that changes at runtime. SCSS for mixins, functions, and build-time logic. Both together is the optimal approach.

---

## Q11. Explain CSS Positioning: static, relative, absolute, fixed, and sticky.

**Summary:**
Positioning controls how an element is placed relative to its normal document flow. `static` is default (normal flow). `relative` offsets from its normal position. `absolute` positions relative to the nearest positioned ancestor. `fixed` positions relative to the viewport. `sticky` switches between relative and fixed based on scroll.

**All Five Positions:**

```css
/* STATIC (default) — normal document flow, no offset */
.static { position: static; }
/* top/left/right/bottom/z-index have NO effect */

/* RELATIVE — offset from its normal position, space is preserved */
.relative {
  position: relative;
  top: 20px;    /* moves DOWN 20px from where it would normally be */
  left: 10px;   /* moves RIGHT 10px */
  /* Original space in the document is STILL occupied */
}

/* ABSOLUTE — positioned relative to nearest positioned ancestor */
.parent {
  position: relative; /* becomes the reference point */
}
.absolute-child {
  position: absolute;
  top: 0;
  right: 0;
  /* Removed from document flow — no space occupied */
  /* Positioned relative to .parent */
}

/* FIXED — positioned relative to the VIEWPORT, ignores scroll */
.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  /* Stays at top of screen even when user scrolls */
}

/* STICKY — relative until scroll threshold, then becomes fixed */
.sticky-nav {
  position: sticky;
  top: 0;
  /* Scrolls normally until it reaches top of viewport */
  /* Then "sticks" to top: 0 */
  /* Unsticks when its parent scrolls out of view */
}
```

**Visual — How Each Works:**
```
Normal flow:  [A] [B] [C]

Relative B (top: 20px):
  [A] [  ] [C]       ← original space preserved
        [B]           ← visually shifted down

Absolute B:
  [A][C]              ← space collapsed
  [B] ← floating, positioned to ancestor

Fixed B:
  [A][C]              ← space collapsed
  [B] ← locked to viewport, doesn't scroll

Sticky B:
  Scrolling... [A] [B] [C]  ← normal flow
  More scroll... [B]         ← stuck at top
                [C]
  Parent out of view... [B] leaves too
```

**Real-World Patterns:**

```css
/* Badge on avatar (absolute inside relative) */
.avatar-wrapper {
  position: relative;
  display: inline-block;
}
.online-badge {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: green;
  border-radius: 50%;
}

/* Sticky table header */
.table thead th {
  position: sticky;
  top: 0;
  background: white;
  z-index: 10;
}

/* Modal overlay (fixed) */
.modal-overlay {
  position: fixed;
  inset: 0;  /* shorthand for top:0 right:0 bottom:0 left:0 */
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
}
```

**Common Mistakes:**
- `absolute` without a `relative` ancestor — positions relative to `<html>` (the viewport), not the parent. Always set `position: relative` on the containing element.
- `sticky` not working — common cause: parent has `overflow: hidden` or `overflow: auto`. Sticky requires the parent to have a visible overflow.
- `z-index` on non-positioned elements — `z-index` only works on positioned elements (`relative`, `absolute`, `fixed`, `sticky`), not `static`.

**Closing:**
`relative` as an anchor for `absolute` children. `fixed` for persistent UI (headers, modals). `sticky` for scroll-aware elements (table headers, sidebar nav). Understanding positioning and stacking context is essential for building complex UIs without hacks.

---

## Q12. What are CSS Pseudo-Elements and Pseudo-Classes? Give real-world examples.

**Summary:**
Pseudo-classes select elements based on their state (`:hover`, `:focus`, `:nth-child`). Pseudo-elements create virtual elements in the DOM for styling (`::before`, `::after`, `::placeholder`). I use pseudo-classes for interactivity and pseudo-elements for decorative content without adding markup.

**Pseudo-Classes — Element States:**
```css
/* User interaction states */
a:hover { color: blue; }          /* mouse over */
a:active { color: red; }          /* while clicking */
input:focus { outline: 2px solid blue; }  /* focused */
input:focus-visible { outline: 2px solid blue; }  /* keyboard focus only */

/* Form states */
input:valid { border-color: green; }
input:invalid { border-color: red; }
input:disabled { opacity: 0.5; }
input:required { border-left: 3px solid red; }
input:placeholder-shown { border-style: dashed; }

/* Structural */
li:first-child { font-weight: bold; }
li:last-child { border-bottom: none; }
tr:nth-child(even) { background: #f9f9f9; }  /* zebra stripes */
tr:nth-child(odd) { background: #ffffff; }
li:nth-child(3n) { color: red; }  /* every 3rd item */
p:not(.special) { color: gray; }  /* all p except .special */

/* Modern pseudo-classes */
:is(h1, h2, h3) { font-weight: bold; }  /* matches any */
:has(.error) { border: 2px solid red; }  /* parent selector! (CSS 2023) */
.card:has(img) { padding-top: 0; }       /* card WITH image — no top padding */
```

**Pseudo-Elements — Virtual DOM Nodes:**
```css
/* ::before and ::after — inject content without HTML */
.required-label::after {
  content: ' *';
  color: red;
}

/* Decorative elements */
.section-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 1em;
  background: var(--primary);
  margin-right: 0.5rem;
  vertical-align: middle;
}

/* Quote styling */
blockquote::before {
  content: '"';
  font-size: 3rem;
  color: var(--primary);
  position: absolute;
  left: -1rem;
  top: -0.5rem;
}

/* Custom checkbox */
.checkbox::before {
  content: '';
  width: 20px;
  height: 20px;
  border: 2px solid #999;
  border-radius: 3px;
  display: inline-block;
}
.checkbox:checked::before {
  content: '✓';
  background: var(--primary);
  color: white;
}

/* Input placeholder */
input::placeholder {
  color: #999;
  font-style: italic;
}

/* Selection highlight */
::selection {
  background: var(--primary);
  color: white;
}

/* Scrollbar styling (Webkit) */
::-webkit-scrollbar { width: 8px; }
::-webkit-scrollbar-thumb { background: #888; border-radius: 4px; }
```

**Real-World Example — Tooltip with Pure CSS:**
```css
.tooltip {
  position: relative;
}

.tooltip::after {
  content: attr(data-tooltip);     /* reads from HTML attribute */
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  padding: 0.5rem 1rem;
  background: #333;
  color: white;
  border-radius: 4px;
  font-size: 0.875rem;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
}

.tooltip:hover::after {
  opacity: 1;
}
```
```html
<button class="tooltip" data-tooltip="Save your changes">Save</button>
```

**The `:has()` Selector — Parent Selector (Game Changer):**
```css
/* Style a form-group differently when its input has an error */
.form-group:has(input:invalid) {
  border-color: red;
}

/* Style a card differently if it contains an image */
.card:has(img) {
  grid-row: span 2;
}

/* Show a different layout if sidebar is present */
.page:has(.sidebar) .main {
  grid-column: 2;
}
```

**Closing:**
Pseudo-classes for state-based styling, pseudo-elements for decorative content without markup pollution. The `:has()` selector is the most powerful addition to CSS in years — it's the parent selector we've wanted for 20 years. I use it in every modern project.

---

## Q13. What are CSS Transitions and Animations? When do you use each?

**Summary:**
Transitions animate property changes between two states (triggered by hover, focus, class change). Animations run keyframe-based sequences that can loop, reverse, and play without user interaction. I use transitions for micro-interactions (hover effects, modal appearance) and animations for attention-grabbing elements (loading spinners, onboarding).

**Transitions — State A → State B:**
```css
/* Smooth hover effect */
.button {
  background: var(--primary);
  color: white;
  transform: scale(1);
  transition: background 0.3s ease, transform 0.2s ease;
  /* Only listed properties animate — others change instantly */
}

.button:hover {
  background: var(--primary-dark);
  transform: scale(1.05);
}

/* Transition shorthand */
.card {
  transition: all 0.3s ease;
  /* ⚠️ 'all' transitions every property — use specific properties for performance */
}

/* Better — only transition what changes */
.card {
  opacity: 1;
  transform: translateY(0);
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.card.entering {
  opacity: 0;
  transform: translateY(20px);
}
```

**Transition Timing Functions:**
```css
/* ease      — slow start, fast middle, slow end (default) */
/* linear    — constant speed */
/* ease-in   — slow start, fast end */
/* ease-out  — fast start, slow end */
/* ease-in-out — slow start, slow end */
/* cubic-bezier(x1, y1, x2, y2) — custom curve */

.modal {
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  /* Slight overshoot effect — modal "bounces" into place */
}
```

**Animations — Keyframe Sequences:**
```css
/* Loading spinner */
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.spinner {
  animation: spin 1s linear infinite;
}

/* Fade-in on page load */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-content {
  animation: fadeInUp 0.6s ease-out forwards;
}

/* Multi-step animation */
@keyframes pulse {
  0%   { transform: scale(1); opacity: 1; }
  50%  { transform: scale(1.1); opacity: 0.8; }
  100% { transform: scale(1); opacity: 1; }
}

.notification-badge {
  animation: pulse 2s ease-in-out infinite;
}

/* Skeleton loading shimmer */
@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.skeleton {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
```

**Performance — What to Animate:**
```css
/* ✅ GPU-accelerated — smooth 60fps */
transform: translate(), scale(), rotate();
opacity: 0 → 1;

/* ❌ Triggers layout recalculation — janky */
width, height, top, left, margin, padding;
/* These cause the browser to recalculate layout for every frame */

/* Example — animate transform, not left */
/* ❌ Slow */
.slide { left: 0; transition: left 0.3s; }
.slide.open { left: 250px; }

/* ✅ Fast */
.slide { transform: translateX(0); transition: transform 0.3s; }
.slide.open { transform: translateX(250px); }
```

**Respect User Preferences:**
```css
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
```

**When to Use Each:**

| Feature | Transitions | Animations |
|---------|------------|------------|
| Trigger | Needs state change (hover, class) | Runs automatically |
| States | Only A → B | Multiple keyframes |
| Looping | No | Yes (`infinite`) |
| Complexity | Simple | Complex multi-step |
| Use case | Hover effects, modal, toggling | Spinners, shimmer, entrance |

**Closing:**
Transitions for interactive state changes, animations for autonomous movement. Always animate `transform` and `opacity` for performance — never `width`, `height`, or `top`. Always respect `prefers-reduced-motion`.

---

## Q14. What is the `z-index` and Stacking Context? Why does `z-index` sometimes not work?

**Summary:**
`z-index` controls the front-to-back layering order of elements, but it only works within a stacking context. A stacking context is created by positioned elements with `z-index`, `opacity < 1`, `transform`, and other CSS properties. The #1 reason `z-index: 9999` doesn't work is that the element's parent has a lower stacking context than another branch of the DOM tree.

**How Stacking Contexts Work:**
```
Think of stacking contexts as FOLDERS on a desk:
- z-index sorts WITHIN a folder
- A page inside Folder A can never be above Folder B
  if Folder B is on top of Folder A

Document
├── Header (z-index: 10)         ← Stacking context
│   ├── Logo (z-index: 999)      ← 999 only within Header
│   └── Nav (z-index: 1)
├── Main (z-index: 1)            ← Stacking context
│   └── Modal (z-index: 99999)   ← STILL behind Header!
│                                   Because Main(1) < Header(10)
└── Footer (z-index: 5)
```

**What Creates a New Stacking Context:**
```css
/* Any of these on a positioned element creates a new stacking context */
position: relative/absolute/fixed + z-index (any value)
opacity less than 1
transform (any value, even translateZ(0))
filter (any value)
will-change: transform, opacity
isolation: isolate       /* explicitly creates one */
position: fixed/sticky   /* always creates one */
mix-blend-mode (not normal)
```

**Why z-index Doesn't Work — Debugging:**
```html
<!-- z-index: 9999 on the modal, but it's BEHIND the header -->
<header style="position: relative; z-index: 10;">
  Header content
</header>

<main style="position: relative; z-index: 1;">
  <!-- This modal is z-index: 9999 WITHIN main's stacking context -->
  <!-- main has z-index: 1, header has z-index: 10 -->
  <!-- So modal (inside z-index:1 context) < header (z-index: 10) -->
  <div class="modal" style="position: fixed; z-index: 9999;">
    I'm behind the header! 😤
  </div>
</main>

<!-- FIX: Move modal to root level, or fix stacking contexts -->
```

**Best Practices — z-index Scale:**
```css
/* Define a scale to prevent z-index wars */
:root {
  --z-dropdown: 100;
  --z-sticky: 200;
  --z-overlay: 300;
  --z-modal: 400;
  --z-toast: 500;
  --z-tooltip: 600;
}

.dropdown { z-index: var(--z-dropdown); }
.sticky-header { z-index: var(--z-sticky); }
.modal-overlay { z-index: var(--z-overlay); }
.modal { z-index: var(--z-modal); }
.toast { z-index: var(--z-toast); }
.tooltip { z-index: var(--z-tooltip); }

/* isolation: isolate — creates stacking context without z-index */
.card {
  isolation: isolate;
  /* Internal z-index won't leak out and affect other cards */
}
```

**Closing:**
`z-index` works within stacking contexts, not globally. When it "doesn't work," the parent's stacking context is the problem, not the z-index value. Use a defined scale, use `isolation: isolate` to contain z-index scope, and move modals/overlays to the document root to avoid stacking context issues.

---

## Q15. What are CSS Preprocessors (SCSS/SASS)? What features do they add?

**Summary:**
SCSS is a CSS superset that adds variables, nesting, mixins, functions, loops, and partials — features that make large-scale CSS maintainable. It compiles to standard CSS. While CSS now has native variables and nesting, SCSS still wins for mixins, functions, `@extend`, and build-time logic.

**Key SCSS Features:**

**1. Nesting — Mirrors HTML Structure:**
```scss
// SCSS
.card {
  padding: 1rem;
  border-radius: 8px;

  &-header {  // BEM: .card-header
    font-size: 1.25rem;
    font-weight: bold;
  }

  &-body {
    padding: 1rem 0;
  }

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  &.active {  // .card.active
    border-color: var(--primary);
  }
}

/* Native CSS nesting (2023) — now supported in browsers */
.card {
  padding: 1rem;

  & .title { font-weight: bold; }
  &:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
}
```

**2. Mixins — Reusable Style Blocks:**
```scss
// Define a mixin
@mixin flex-center($direction: row) {
  display: flex;
  flex-direction: $direction;
  justify-content: center;
  align-items: center;
}

@mixin responsive($breakpoint) {
  @if $breakpoint == tablet { @media (min-width: 768px) { @content; } }
  @if $breakpoint == desktop { @media (min-width: 1024px) { @content; } }
  @if $breakpoint == wide { @media (min-width: 1440px) { @content; } }
}

@mixin truncate($lines: 1) {
  @if $lines == 1 {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  } @else {
    display: -webkit-box;
    -webkit-line-clamp: $lines;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

// Use
.hero {
  @include flex-center(column);
  min-height: 100vh;
}

.sidebar {
  display: none;
  @include responsive(desktop) {
    display: block;
    width: 250px;
  }
}

.card-description {
  @include truncate(3);  // Limit to 3 lines with ellipsis
}
```

**3. Functions — Computed Values:**
```scss
// Spacing scale function
@function spacing($multiplier) {
  @return $multiplier * 0.25rem;
}

.card {
  padding: spacing(4);     // 1rem
  margin-bottom: spacing(6); // 1.5rem
  gap: spacing(2);          // 0.5rem
}

// Color manipulation
.button {
  background: $primary;
  &:hover {
    background: darken($primary, 10%);
  }
  &:active {
    background: darken($primary, 20%);
  }
}
```

**4. Partials & File Organization:**
```
styles/
├── _variables.scss      // $primary, $spacing, $breakpoints
├── _mixins.scss         // @mixin flex-center, @mixin responsive
├── _reset.scss          // CSS reset/normalize
├── _typography.scss     // Font scales, heading styles
├── components/
│   ├── _button.scss
│   ├── _card.scss
│   └── _modal.scss
├── layouts/
│   ├── _header.scss
│   ├── _sidebar.scss
│   └── _grid.scss
└── main.scss            // @use all partials
```

```scss
// main.scss
@use 'variables' as *;
@use 'mixins' as *;
@use 'reset';
@use 'typography';
@use 'components/button';
@use 'components/card';
@use 'layouts/header';
```

**5. @extend — Share Rules (Use Sparingly):**
```scss
%button-base {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-primary {
  @extend %button-base;
  background: var(--primary);
  color: white;
}

.btn-secondary {
  @extend %button-base;
  background: transparent;
  border: 2px solid var(--primary);
  color: var(--primary);
}
```

**SCSS vs Native CSS (2024):**

| Feature | SCSS | Native CSS |
|---------|------|-----------|
| Variables | `$var` (compile-time) | `var(--prop)` (runtime) |
| Nesting | Full support | Supported (2023+) |
| Mixins | `@mixin` + `@include` | Not available |
| Functions | `@function` | Limited (`calc`, `clamp`) |
| Color functions | `darken`, `lighten` | `color-mix()` (2023) |
| Loops | `@for`, `@each`, `@while` | Not available |
| Imports | `@use`, `@forward` | `@import` (no partials) |

**Closing:**
SCSS is still essential for large projects — mixins, functions, and build-time logic have no native CSS equivalent. But I use CSS custom properties for theming (runtime) and SCSS for everything that's compile-time. They complement each other.

---

## Q16. How do you implement a responsive design system with a consistent spacing and typography scale?

**Summary:**
I build a design system using CSS custom properties for a spacing scale (based on 4px/8px grid), a type scale (modular scale for font sizes), and utility classes. This ensures visual consistency across the entire application without developers guessing pixel values.

**Spacing Scale (8px Grid):**
```css
:root {
  --space-1: 0.25rem;   /* 4px */
  --space-2: 0.5rem;    /* 8px */
  --space-3: 0.75rem;   /* 12px */
  --space-4: 1rem;      /* 16px */
  --space-5: 1.5rem;    /* 24px */
  --space-6: 2rem;      /* 32px */
  --space-8: 3rem;      /* 48px */
  --space-10: 4rem;     /* 64px */
  --space-12: 6rem;     /* 96px */
}

/* Usage — consistent spacing everywhere */
.card {
  padding: var(--space-4);
  margin-bottom: var(--space-5);
  gap: var(--space-3);
}

.section {
  padding: var(--space-10) var(--space-4);
}
```

**Typography Scale:**
```css
:root {
  --text-xs: 0.75rem;     /* 12px */
  --text-sm: 0.875rem;    /* 14px */
  --text-base: 1rem;      /* 16px */
  --text-lg: 1.125rem;    /* 18px */
  --text-xl: 1.25rem;     /* 20px */
  --text-2xl: 1.5rem;     /* 24px */
  --text-3xl: 1.875rem;   /* 30px */
  --text-4xl: 2.25rem;    /* 36px */

  --leading-tight: 1.25;
  --leading-normal: 1.5;
  --leading-relaxed: 1.75;
}

h1 { font-size: var(--text-4xl); line-height: var(--leading-tight); }
h2 { font-size: var(--text-3xl); line-height: var(--leading-tight); }
h3 { font-size: var(--text-2xl); line-height: var(--leading-tight); }
body { font-size: var(--text-base); line-height: var(--leading-normal); }
small { font-size: var(--text-sm); }

/* Responsive typography with clamp() */
h1 {
  font-size: clamp(1.875rem, 4vw, 3rem);
  /* Min: 30px, Preferred: 4vw, Max: 48px */
  /* Smoothly scales between breakpoints — no media queries! */
}
```

**Color System:**
```css
:root {
  /* Brand colors */
  --primary-50: #e3f2fd;
  --primary-100: #bbdefb;
  --primary-500: #2196f3;   /* main */
  --primary-700: #1976d2;
  --primary-900: #0d47a1;

  /* Semantic colors */
  --color-success: #4caf50;
  --color-warning: #ff9800;
  --color-error: #f44336;
  --color-info: #2196f3;

  /* Neutral scale */
  --gray-50: #fafafa;
  --gray-100: #f5f5f5;
  --gray-200: #eeeeee;
  --gray-500: #9e9e9e;
  --gray-700: #616161;
  --gray-900: #212121;
}
```

**Responsive Container with clamp():**
```css
.container {
  width: min(100% - 2rem, 1200px);
  margin-inline: auto;
  /* On mobile: full width minus 1rem padding each side */
  /* On desktop: max 1200px, centered */
  /* No media queries needed! */
}
```

**Closing:**
A design system with CSS custom properties ensures consistency, speeds up development, and makes theming trivial. Define spacing, typography, and colors once — use everywhere. `clamp()` and `min()`/`max()` reduce the need for media queries.

---

## Q17. What is BEM naming convention? What CSS architecture methodologies do you use?

**Summary:**
BEM (Block–Element–Modifier) is a naming convention that makes CSS classes predictable, scalable, and self-documenting. Block is the component, Element is a part of the block, Modifier is a variation. I use BEM for component styling in Angular/React projects because it prevents class name collisions and makes CSS grep-friendly.

**BEM Structure:**
```
Block:    .card
Element:  .card__title      (part of card)
Modifier: .card--featured    (variation of card)

Pattern:  .block__element--modifier
```

**Real Example:**
```html
<!-- BLOCK: card -->
<div class="card card--featured">
  <!-- ELEMENTS: parts of card -->
  <img class="card__image" src="hero.jpg" alt="Hero" />
  <div class="card__content">
    <h2 class="card__title">Product Name</h2>
    <p class="card__description">Description here...</p>
    <span class="card__price card__price--sale">$29.99</span>
  </div>
  <div class="card__footer">
    <button class="card__button card__button--primary">Buy Now</button>
    <button class="card__button card__button--secondary">Add to Cart</button>
  </div>
</div>
```

```scss
// SCSS with BEM
.card {
  border: 1px solid var(--gray-200);
  border-radius: var(--space-2);
  overflow: hidden;

  // Modifier — featured variant
  &--featured {
    border-color: var(--primary-500);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  // Elements
  &__image {
    width: 100%;
    height: 200px;
    object-fit: cover;
  }

  &__content {
    padding: var(--space-4);
  }

  &__title {
    font-size: var(--text-xl);
    margin-bottom: var(--space-2);
  }

  &__price {
    font-weight: 700;
    color: var(--gray-900);

    &--sale {
      color: var(--color-error);
    }
  }

  &__button {
    &--primary { background: var(--primary-500); color: white; }
    &--secondary { background: transparent; border: 1px solid var(--primary-500); }
  }
}
```

**BEM Rules:**
```
✅ .card__title             — element of card
✅ .card--large             — modifier of card
✅ .card__title--highlighted — modifier of element

❌ .card__content__title    — NEVER nest elements!
   Instead: .card__title    (flat structure)

❌ .card__title .span       — don't mix BEM with tag selectors
```

**Why BEM Works at Scale:**
```
Without BEM:
  .title { }       — Which title? Page? Card? Modal? Collision!
  .active { }      — Active what? Too generic.
  .header .nav a {} — Tightly coupled to HTML structure.

With BEM:
  .card__title { }      — Unambiguous, self-documenting
  .tab--active { }      — Clear which component
  .nav__link { }        — No HTML structure dependency
```

**Angular Component Encapsulation vs BEM:**
```
Angular uses ViewEncapsulation (emulated Shadow DOM) which scopes styles
automatically. BEM is still useful because:
1. It makes HTML readable — class="card__title" is self-documenting
2. It works in global stylesheets (themes, layouts)
3. It prevents specificity issues
4. It's framework-agnostic — works if you move to React, Vue, etc.
```

**Closing:**
BEM is simple, scalable, and grep-friendly — `grep card__title` finds every usage. I use it in every project for component CSS, combined with utility classes for one-off spacing/alignment. It's not glamorous, but it's the most reliable CSS naming convention at scale.

---

## Q18. What are the new CSS features (2023-2024) every senior developer should know?

**Summary:**
CSS has evolved dramatically — container queries, `:has()` parent selector, native nesting, `subgrid`, `color-mix()`, `@layer`, and `@scope` are now production-ready. These features reduce JavaScript dependency and enable patterns that were previously impossible with CSS alone.

**1. Container Queries — Responsive to Parent, Not Viewport:**
```css
/* Define a container */
.card-wrapper {
  container-type: inline-size;
  container-name: card;
}

/* Query the container's width, not the viewport */
@container card (min-width: 400px) {
  .card {
    display: flex;
    flex-direction: row;  /* horizontal layout when container is wide */
  }
}

@container card (max-width: 399px) {
  .card {
    flex-direction: column;  /* vertical layout when container is narrow */
  }
}

/* Same card component, different layout based on WHERE it's placed */
/* In sidebar (narrow) → vertical. In main content (wide) → horizontal */
```

**2. `:has()` — The Parent Selector:**
```css
/* Style parent based on child state */
.form-group:has(input:invalid) {
  border-left: 3px solid red;
}

/* Card with image gets different layout */
.card:has(img) {
  grid-template-rows: 200px 1fr;
}

/* Navigation changes when on a page with sidebar */
body:has(.sidebar) .main-content {
  margin-left: 250px;
}

/* Style a label when its input is focused */
label:has(+ input:focus) {
  color: var(--primary);
  font-weight: bold;
}
```

**3. Native CSS Nesting:**
```css
/* No preprocessor needed! */
.card {
  padding: 1rem;
  background: white;

  & .title {
    font-size: 1.25rem;
  }

  &:hover {
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  }

  @media (min-width: 768px) {
    & { padding: 2rem; }
  }
}
```

**4. Subgrid — Child Inherits Parent's Grid:**
```css
/* Parent grid */
.product-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

/* Child card aligns to parent's grid tracks */
.product-card {
  display: grid;
  grid-template-rows: subgrid;
  grid-row: span 3;  /* title, description, and price all align across cards */
}
/* All card titles line up, all prices line up — even with different content lengths */
```

**5. `color-mix()` — Native Color Manipulation:**
```css
.button {
  background: var(--primary);
}

.button:hover {
  background: color-mix(in srgb, var(--primary), black 20%);
  /* Darkens by mixing with 20% black — no SCSS darken() needed! */
}

.button:active {
  background: color-mix(in srgb, var(--primary), black 40%);
}

/* Create semi-transparent versions */
.overlay {
  background: color-mix(in srgb, var(--primary) 30%, transparent);
}
```

**6. `@layer` — Cascade Control:**
```css
/* Define layer order — first listed = lowest priority */
@layer reset, base, components, utilities;

@layer reset {
  * { margin: 0; padding: 0; box-sizing: border-box; }
}

@layer base {
  body { font-family: system-ui; }
  a { color: var(--primary); }
}

@layer components {
  .card { padding: 1rem; }
  .button { padding: 0.5rem 1rem; }
}

@layer utilities {
  .mt-4 { margin-top: 1rem !important; }
  .text-center { text-align: center !important; }
}

/* Utilities ALWAYS win over components (by layer order, not specificity) */
/* No more specificity wars! */
```

**7. `@scope` — Scoped Styles:**
```css
@scope (.card) to (.card__footer) {
  /* These styles only apply INSIDE .card but NOT inside .card__footer */
  p { color: gray; }
  a { color: var(--primary); }
}
```

**8. View Transitions — Native Page Transitions:**
```css
/* Smooth page transitions without JavaScript frameworks */
::view-transition-old(root) {
  animation: fade-out 0.3s ease;
}

::view-transition-new(root) {
  animation: fade-in 0.3s ease;
}
```

**Closing:**
Container queries and `:has()` are the biggest CSS advancements in a decade. Container queries make truly reusable components. `:has()` eliminates JavaScript for parent-based styling. Native nesting and `color-mix()` reduce SCSS dependency. These features are production-ready now.

---

## Q19. How do you optimize CSS performance for large-scale applications?

**Summary:**
CSS performance optimization targets three areas: reducing what's loaded (critical CSS, dead code removal), reducing what's calculated (efficient selectors, avoiding layout thrash), and reducing what's painted (GPU-accelerated animations, `contain` property). The biggest wins are removing unused CSS and animating only `transform`/`opacity`.

**1. Remove Unused CSS (Biggest Impact):**
```
Problem: Average website ships 80-90% unused CSS

Tools:
  PurgeCSS — scans HTML/JS files, removes unused selectors
  Angular CLI — tree-shakes component styles automatically

Tailwind CSS approach:
  → Scans all templates at build time
  → Only includes classes that are actually used
  → 3MB → 10KB (typical reduction)
```

**2. Critical CSS — Inline Above-the-Fold Styles:**
```html
<!-- Inline critical CSS for instant first paint -->
<head>
  <style>
    /* Only styles needed for above-the-fold content */
    body { margin: 0; font-family: system-ui; }
    .hero { min-height: 100vh; display: flex; align-items: center; }
    .nav { position: fixed; top: 0; width: 100%; }
  </style>
  <!-- Load full CSS asynchronously -->
  <link rel="preload" href="styles.css" as="style" onload="this.rel='stylesheet'" />
</head>
```

**3. Efficient Selectors:**
```css
/* CSS selectors are read RIGHT to LEFT */

/* ❌ Slow — browser finds ALL spans, then filters by ancestors */
.page .content .card .meta span { }

/* ✅ Fast — single class lookup */
.card-meta-text { }

/* ❌ Expensive — universal + attribute selectors */
* { box-sizing: border-box; }     /* Acceptable for reset only */
[class*="col-"] { float: left; }   /* Scans every element */

/* ✅ Specific class names are fastest */
.col-6 { width: 50%; }
```

**4. GPU-Accelerated Animations:**
```css
/* ✅ Runs on GPU — smooth 60fps */
.animate {
  transform: translateX(100px);
  opacity: 0.5;
  /* These only trigger COMPOSITE — no layout/paint */
}

/* ❌ Triggers layout recalculation — janky */
.animate-bad {
  left: 100px;     /* triggers LAYOUT */
  width: 200px;    /* triggers LAYOUT */
  background: red;  /* triggers PAINT */
}

/* Hint browser about upcoming animation */
.will-animate {
  will-change: transform, opacity;
  /* Promotes to own compositor layer ahead of time */
  /* ⚠️ Use sparingly — each layer consumes GPU memory */
}
```

**5. CSS `contain` Property — Limit Browser Work:**
```css
/* Tell browser this element's internals don't affect the rest of the page */
.card {
  contain: layout style paint;
  /* layout: changes inside don't cause reflow outside */
  /* style: counters/quotes don't escape */
  /* paint: content won't draw outside element bounds */
}

/* content-visibility: auto — skip rendering off-screen content */
.long-list-item {
  content-visibility: auto;
  contain-intrinsic-size: 0 100px;
  /* Browser skips rendering until element enters viewport */
  /* Like virtual scrolling but in pure CSS */
}
```

**6. Reduce Reflows/Repaints:**
```javascript
// ❌ Triggers layout recalculation multiple times
element.style.width = '100px';
const height = element.offsetHeight; // FORCE REFLOW
element.style.height = height + 'px';
const width = element.offsetWidth;   // FORCE REFLOW AGAIN

// ✅ Batch reads, then writes
const height = element.offsetHeight;
const width = element.offsetWidth;
// All reads done — now write
element.style.width = '100px';
element.style.height = height + 'px';
```

**7. Font Optimization:**
```css
/* Prevent FOUT (Flash of Unstyled Text) / FOIT (Flash of Invisible Text) */
@font-face {
  font-family: 'CustomFont';
  src: url('font.woff2') format('woff2');
  font-display: swap;  /* Show fallback immediately, swap when loaded */
}

/* Preload critical fonts */
<link rel="preload" href="font.woff2" as="font" type="font/woff2" crossorigin />

/* Use system fonts as primary — zero loading time */
body {
  font-family: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
}
```

**Performance Checklist:**
```
□ Remove unused CSS (PurgeCSS / tree-shaking)
□ Inline critical CSS for above-the-fold content
□ Animate only transform and opacity
□ Use contain on isolated components
□ Use content-visibility: auto for long lists
□ Preload critical fonts with font-display: swap
□ Minimize selector complexity (flat class names)
□ Use CSS layers to control cascade without specificity wars
□ Avoid layout-triggering properties in animations
□ Batch DOM reads/writes to prevent forced reflow
```

**Closing:**
CSS performance comes down to: load less (dead code removal, critical CSS), calculate less (efficient selectors, `contain`), and paint less (GPU animations, `content-visibility`). The biggest wins are removing unused CSS and using `transform`/`opacity` for all animations.

---

## Q20. What is the difference between `rem`, `em`, `px`, `vh`/`vw`, and when do you use each?

**Summary:**
`px` is absolute (fixed size). `rem` is relative to root font size (consistent scaling). `em` is relative to parent font size (compounds). `vh`/`vw` are relative to viewport dimensions. I use `rem` for spacing and font sizes (consistent, accessible), `px` for borders and shadows (shouldn't scale), and `vh`/`vw` for viewport-dependent layouts.

**Unit Comparison:**

| Unit | Relative To | Scales With | Use Case |
|------|------------|-------------|----------|
| `px` | Nothing (absolute) | Nothing | Borders, shadows, precise control |
| `rem` | Root `<html>` font-size | Browser settings | Spacing, font sizes, media queries |
| `em` | Parent element font-size | Parent changes | Component-scoped scaling |
| `%` | Parent element dimension | Parent size | Widths, padding relative to parent |
| `vw` | 1% of viewport width | Window resize | Full-width sections |
| `vh` | 1% of viewport height | Window resize | Full-height heroes |
| `dvh` | Dynamic viewport height | Address bar show/hide | Mobile full-height (iOS) |
| `ch` | Width of "0" character | Font changes | Input widths (max characters) |

**rem — My Default for Everything:**
```css
/* Root: 16px (default) — 1rem = 16px */
html { font-size: 16px; }

/* If user sets browser font-size to 20px (accessibility): */
/* All rem values scale proportionally — content stays readable */

body { font-size: 1rem; }         /* 16px (or user's setting) */
h1 { font-size: 2.25rem; }        /* 36px */
.card { padding: 1.5rem; }        /* 24px */
.container { max-width: 75rem; }   /* 1200px */
.gap { gap: 0.5rem; }             /* 8px */
```

**Why rem Over px for Font Sizes:**
```css
/* ❌ px — ignores user's browser font-size setting */
h1 { font-size: 36px; }
/* User sets 150% font size for accessibility → still 36px. Bad. */

/* ✅ rem — respects user's setting */
h1 { font-size: 2.25rem; }
/* User sets 150% font size → h1 becomes 54px. Accessible! */
```

**em — Relative to Parent (Compounds!):**
```css
.parent { font-size: 1.25rem; }  /* 20px */
.child { font-size: 1.5em; }     /* 1.5 × 20px = 30px */
.grandchild { font-size: 1.5em; }/* 1.5 × 30px = 45px! — compounding! */

/* ✅ Good use of em — component-relative spacing */
.button {
  font-size: 1rem;
  padding: 0.5em 1em;    /* Padding scales with button's own font-size */
}
.button--large {
  font-size: 1.25rem;    /* Padding automatically grows proportionally */
}
```

**Viewport Units — vh, vw, dvh:**
```css
/* Full-height hero section */
.hero {
  min-height: 100vh;     /* 100% of viewport height */
}

/* ⚠️ Mobile problem: 100vh includes hidden address bar area */
/* User sees content "behind" the address bar */

/* ✅ Fix: dvh (dynamic viewport height) */
.hero {
  min-height: 100dvh;   /* Adjusts when mobile address bar shows/hides */
}

/* svh — small viewport (address bar visible) */
/* lvh — large viewport (address bar hidden) */
/* dvh — dynamic (adjusts in real-time) */

/* Responsive text with viewport units */
.hero-title {
  font-size: clamp(2rem, 5vw, 4rem);
  /* Min: 2rem, Preferred: 5vw, Max: 4rem */
  /* Smoothly scales without breakpoints */
}
```

**My Decision Framework:**
```
Font sizes:           rem (accessible, scales with user settings)
Spacing (margin, padding, gap): rem (consistent scale)
Borders:              px (1px border shouldn't scale)
Shadows:              px (shadow size is visual, not content)
Widths:               %, rem, or fr (responsive)
Max-width:            rem or px (container shouldn't grow forever)
Full-height sections: dvh (mobile-safe)
Media queries:        rem (scales with user font-size)
Component-relative:   em (button padding scales with its font-size)
```

**clamp() — Responsive Without Media Queries:**
```css
/* clamp(minimum, preferred, maximum) */

/* Font sizes */
h1 { font-size: clamp(1.5rem, 4vw, 3rem); }

/* Container padding */
.section { padding: clamp(1rem, 5vw, 4rem); }

/* Gap */
.grid { gap: clamp(0.5rem, 2vw, 2rem); }
```

**Closing:**
`rem` is the default unit for a senior developer — it scales with user accessibility settings, provides consistent spacing, and works in media queries. `px` only for borders and shadows. `dvh` instead of `vh` for mobile. `clamp()` replaces most media queries for fluid sizing. Getting units right is the difference between a rigid layout and a truly responsive, accessible application.

---

## Quick Reference Summary

| # | Topic | Key Takeaway |
|---|-------|-------------|
| 1 | Semantic HTML | Use `<article>`, `<nav>`, `<main>` — SEO + accessibility |
| 2 | HTML5 APIs | Storage, Intersection Observer, History, Broadcast Channel |
| 3 | Script Loading | `defer` by default, `async` for analytics, never bare `<script>` |
| 4 | Accessibility | Semantic HTML + ARIA + keyboard nav + contrast + focus mgmt |
| 5 | Meta Tags | Viewport for responsive, OG for social, robots for SEO |
| 6 | Box Model | `border-box` globally — always, every project |
| 7 | Flexbox vs Grid | Flex for 1D alignment, Grid for 2D layouts |
| 8 | Media Queries | Mobile-first `min-width`, container queries for components |
| 9 | Specificity | Keep low + flat, use `:where()` and `@layer`, avoid `!important` |
| 10 | CSS Variables | Runtime, cascade, JS-accessible — themes + design tokens |
| 11 | Positioning | `relative` as anchor, `absolute` for overlays, `sticky` for scroll |
| 12 | Pseudo Selectors | `:has()` parent selector, `::before/after` for decoration |
| 13 | Animations | `transform`/`opacity` only, respect `prefers-reduced-motion` |
| 14 | z-index | Stacking contexts, not global — use a defined scale |
| 15 | SCSS | Mixins + functions + nesting — complement CSS variables |
| 16 | Design System | Spacing scale + type scale + color tokens with CSS vars |
| 17 | BEM | `.block__element--modifier` — predictable, scalable CSS |
| 18 | Modern CSS (2023-24) | Container queries, `:has()`, native nesting, `color-mix()`, `@layer` |
| 19 | CSS Performance | Remove unused CSS, GPU animations, `contain`, `content-visibility` |
| 20 | CSS Units | `rem` default, `px` borders, `dvh` mobile, `clamp()` fluid |
