# HTML & CSS — Interview Q&A (Easy Version)

> Every question follows the same shape — **Easiest way to remember → simple explanation → real-world example → diagram → interview trap → Easy memory box.**
>
> 🔵 Your Angular/Ionic work uses **Ionic CSS variables + Shadow DOM**, **PrimeNG** and **Angular Material** — those specifics are flagged where they matter.

---

## Contents

1. [HTML](#html)
2. [CSS](#css)
3. [Layout](#layout)
4. [Responsive](#responsive)
5. [Modern / Advanced](#modern--advanced)
6. [Quick Revision Sheet](#quick-revision-sheet)

---

## HTML

### Q: What is semantic HTML and why does it matter?

The easiest way to remember:

```
Semantic HTML = tags that describe the MEANING of the content,
                not just how it looks. ⭐
```

```html
<!-- ❌ NON-semantic — a screen reader sees nothing but boxes -->
<div class="header">
  <div class="nav">...</div>
</div>
<div class="main">
  <div class="article">...</div>
</div>

<!-- ✅ SEMANTIC -->
<header>
  <nav>...</nav>
</header>
<main>
  <article>...</article>
</main>
<footer>...</footer>
```

```
<header> <nav> <main> <article> <section> <aside> <footer>
<figure> <figcaption> <time> <mark> <details> <summary>
```

#### Why it matters — three reasons

```
1. ACCESSIBILITY ⭐ — a screen reader can jump straight to <main> or list
                      the <nav> landmarks. With <div> soup it cannot ⭐
2. SEO — search engines weight <h1> and <article> content
3. MAINTAINABILITY — <footer> tells the next developer what it is;
                     <div class="fp-b"> does not
```

Real-world idea: **labelled boxes when you move house.** "KITCHEN" and "BOOKS" versus twenty identical unlabelled boxes. The contents are the same; only one version is usable.

⭐ The strongest single point: **`<main>` gives a keyboard user a "skip to content" target.** Without it, they tab through the entire navigation on every page.

#### Easy memory

```
Semantic = tags with MEANING ⭐ (labelled moving boxes 📦)
header nav main article section aside footer

Why: ACCESSIBILITY ⭐ (screen-reader landmarks) + SEO + readability
<div class="header"> works visually — and tells assistive tech NOTHING ⭐
```

---

### Q: Block vs inline elements?

```
BLOCK        → starts on a NEW LINE, takes the FULL width  ⭐
INLINE       → flows within the text, only as wide as its content ⭐
INLINE-BLOCK → flows inline, but ACCEPTS width/height ⭐
```

```
BLOCK              INLINE
┌──────────────┐   text text [span] text text
│    <div>     │   ↑ no line break, sits in the flow
└──────────────┘
┌──────────────┐
│     <p>      │
└──────────────┘
```

```
BLOCK  : div p h1–h6 ul ol li section article form
INLINE : span a strong em img label input code
```

⚠️ **The trap that gets asked:**

```
An INLINE element IGNORES width, height, and vertical margins ⭐

<span style="width: 200px">   → the width does NOTHING 💥
Fix: display: inline-block ⭐ (or block, or flex)
```

Real-world idea: **paragraphs vs words.** A paragraph occupies its own line; a word sits inside the sentence.

#### Easy memory

```
BLOCK → new line, full width ⭐ (a paragraph)
INLINE → sits in the flow, content-sized ⭐ (a word)
INLINE-BLOCK → in the flow BUT accepts width/height ⭐

⚠️ Inline IGNORES width/height and vertical margins 💥 → use inline-block
```

---

### Q: `<div>` vs `<span>`?

```
<div>  → a BLOCK  container → layout, sections ⭐
<span> → an INLINE container → styling part of a line ⭐
```

```html
<div>A whole section</div>
<p>Only <span class="highlight">this word</span> is highlighted.</p>
```

```
Both are SEMANTICALLY EMPTY ⭐ — they carry no meaning.
Prefer a semantic tag whenever one exists ⭐
```

#### Easy memory

```
div = BLOCK box (layout) | span = INLINE box (part of a line) ⭐
Both mean NOTHING semantically → prefer header/nav/article when they fit ⭐
```

---

### Q: What are `data-*` attributes?

```
data-* = your own custom data on an element, without breaking HTML validity ⭐
```

```html
<button data-user-id="42" data-role="admin">Delete</button>
```

```js
btn.dataset.userId;      // "42"   ⭐ data-user-id → dataset.userId (camelCase)
btn.dataset.role;        // "admin"
```

```css
button[data-role="admin"] { border: 2px solid red; }   /* ⭐ styleable too */
```

⭐ Where they earn their place: **event delegation** — one listener on the parent reads `e.target.dataset.id`. → [01 — JavaScript](./01-javascript.md)

⚠️ In Angular you rarely need them — you have property binding and template variables. Say that:

> *"In Angular I'd bind a property or use a template reference instead; `data-*` matters most in plain DOM code and for hooks that tools like Cypress can target."*

#### Easy memory

```
data-user-id="42" → element.dataset.userId ⭐ (kebab → camelCase)
Valid HTML, styleable with [data-x="y"] ⭐
Main use: event delegation + test hooks. In Angular, prefer bindings ⭐
```

---

### Q: What are meta tags and which matter?

```html
<meta charset="UTF-8">                                        <!-- ⭐ always first -->
<meta name="viewport" content="width=device-width, initial-scale=1.0">  <!-- ⭐ responsive -->
<meta name="description" content="...">                       <!-- SEO snippet -->
<meta http-equiv="Content-Security-Policy" content="...">     <!-- security -->
<meta property="og:title" content="...">                      <!-- ⭐ link previews -->
```

```
charset   ⭐ → without it, non-English characters break (mojibake) 💥
viewport  ⭐ → WITHOUT IT, a mobile browser renders a 980px desktop page
               and zooms out → your media queries never fire 💥
description → the snippet Google shows
og:*      ⭐ → the card WhatsApp/LinkedIn shows when the link is shared
```

⭐ **The viewport tag is the one to emphasise** — it's the single most common reason "my responsive site doesn't work on mobile."

#### Easy memory

```
charset ⭐ (UTF-8, first) | viewport ⭐ (responsive — without it, NOTHING works)
description (SEO) | og:* ⭐ (WhatsApp/LinkedIn previews)
```

---

### Q: `async` vs `defer` on a script tag?

The easiest way to remember:

```
(neither) → download BLOCKS parsing, then executes    🐢
async     → downloads in parallel, executes THE MOMENT it's ready ⚠️ (order NOT guaranteed)
defer     → downloads in parallel, executes AFTER parsing, IN ORDER ⭐
```

```
NORMAL   parse ──■■■■ download ■■■■ execute ── parse       🐢 blocks
async    parse ──────────────────── ⚡execute ── parse      ⚠️ interrupts, any order
defer    parse ────────────────────────────── parse ── execute ⭐ in order
              (downloading in parallel the whole time)
```

```
async → INDEPENDENT scripts: analytics, ads ⭐
defer → scripts that depend on the DOM or on each other ⭐ (the safe default)
```

⚠️ `async` with two scripts where B depends on A is a race condition — whichever downloads first runs first.

#### Easy memory

```
none  → blocks parsing 🐢
async → runs ASAP ⚠️ order NOT guaranteed → analytics
defer ⭐ → runs after parsing, IN ORDER → the safe default
Both download in PARALLEL ⭐
```

---

### Q: `localStorage` vs `sessionStorage` vs cookies?

| | localStorage | sessionStorage | cookies |
|---|---|---|---|
| Lifetime | **forever** until cleared ⭐ | until the **tab** closes ⭐ | an expiry you set |
| Size | ~5–10 MB | ~5–10 MB | **~4 KB** ⚠️ |
| Sent to the server | ❌ | ❌ | ✅ **on every request** ⚠️ |
| Scope | per origin | per **tab** ⭐ | per domain/path |
| Accessible to JS | ✅ | ✅ | ✅ unless **HttpOnly** ⭐ |

```
localStorage   → a user preference, a theme, a cached list ⭐
sessionStorage → per-tab state (a wizard step, a draft) ⭐
cookies        → things the SERVER must see ⭐ (session id, auth)
```

⭐ **The security answer to give:**

```
An access token in localStorage is readable by ANY JavaScript on the page,
so one XSS bug leaks it. 💥

An HttpOnly + Secure + SameSite cookie CANNOT be read by JavaScript ⭐
   → the safer place for a session, at the cost of CSRF protection work
```

#### 🔵 In your RoboGebra mobile app

```
@capacitor/preferences is used instead of localStorage ⭐
   → because the OS can EVICT WebView storage under memory pressure,
     silently logging the user out 💥
```

→ [15 — Ionic](./15-ionic-level1.md)

#### Easy memory

```
localStorage ⭐ forever | sessionStorage ⭐ per TAB | cookies ⭐ sent to the SERVER (4 KB)

⚠️ A token in localStorage is XSS-readable 💥 → HttpOnly cookie is safer ⭐
📱 On native, use Capacitor Preferences — the OS can wipe WebView storage ⭐
```

---

### Q: Common HTML5 input types?

```html
<input type="email">     <input type="tel">      <input type="url">
<input type="number">    <input type="date">     <input type="time">
<input type="range">     <input type="color">    <input type="search">
<input type="file" accept="image/*">
```

⭐ The real benefit on mobile — worth saying:

```
type="email"  → the keyboard shows @ ⭐
type="tel"    → a NUMERIC keypad ⭐
type="number" → a numeric keypad + spinners
      ↓
Using type="text" for a phone number makes the user hunt for digits 💥
```

⚠️ **Never trust client validation:**

```
type="email" is a convenience, not a security control.
Anyone can bypass it with devtools or curl → ALWAYS validate on the server ⭐
```

#### Easy memory

```
email tel url number date time range color file search
📱 The real win: the right MOBILE KEYBOARD ⭐ (tel → numeric keypad)
⚠️ Client validation is UX only — always validate server-side ⭐
```

---

### Q: What are the basics of accessibility (ARIA)?

```
ARIA = extra attributes that tell assistive technology what an element
       IS and what STATE it's in — when the HTML alone can't. ⭐
```

```html
<button aria-label="Close dialog">×</button>          <!-- ⭐ "×" alone means nothing -->
<div role="alert">Save failed</div>                    <!-- announced immediately -->
<input aria-describedby="hint"> <span id="hint">8+ characters</span>
<button aria-expanded="false" aria-controls="menu">Menu</button>   <!-- ⭐ state -->
<img src="chart.png" alt="Sales rose 20% in Q3">       <!-- ⭐ describe, don't label -->
```

⭐ **The first rule of ARIA — say this, it's the answer that shows real knowledge:**

```
"The best ARIA is NO ARIA." ⭐

Use <button> instead of <div role="button" tabindex="0" @keydown...>.
A real <button> is focusable, keyboard-activatable and announced —
for free. ARIA is a patch for when semantic HTML can't express it. ⭐
```

The checklist worth naming:

```
✅ Semantic tags first ⭐
✅ alt text that DESCRIBES (or alt="" for decorative images ⭐)
✅ Keyboard reachable — everything usable with Tab and Enter ⭐
✅ Visible focus outline (never `outline: none` with no replacement 💥)
✅ Colour contrast ≥ 4.5:1 for body text
✅ Labels tied to inputs (<label for> or aria-label)
```

#### Easy memory

```
"The best ARIA is NO ARIA" ⭐ — use a real <button>, not div role="button"
aria-label (name) | role (what it is) | aria-expanded (state) ⭐
alt describes ⭐ | alt="" for decorative
Keyboard + visible focus + 4.5:1 contrast ⭐
```

---

### Q: What is `<!DOCTYPE html>`?

```
It tells the browser to use STANDARDS MODE. ⭐
Leave it out and you get QUIRKS MODE — 1990s box-model behaviour. 💥
```

```
Standards mode → width = the CONTENT width (the real box model) ⭐
Quirks mode    → width INCLUDES padding and border (the old IE model) 💥
                 → every layout shifts
```

It is not an HTML tag and has no attributes — just the first line of the document.

#### Easy memory

```
<!DOCTYPE html> → STANDARDS mode ⭐
Missing it → QUIRKS mode → the OLD box model → layouts break 💥
```

---

### Q: What is an iframe and what are its concerns?

```html
<iframe src="https://x.com" title="Report" sandbox loading="lazy"></iframe>
```

```
✅ Embed third-party content (maps, videos, payment forms)
❌ SECURITY — clickjacking, and the framed site can attempt things ⚠️
❌ Performance — a whole second document to load
❌ SEO — the content isn't part of your page
❌ Accessibility — needs a `title` ⭐
```

```
sandbox           → strip permissions from the framed page ⭐
X-Frame-Options / CSP frame-ancestors → stop OTHERS framing YOU ⭐ (anti-clickjacking)
loading="lazy"    → don't load it until it's near the viewport
```

⭐ **Clickjacking** is the concern to name: an attacker frames your page invisibly over a decoy button, so the user's click lands on *your* "Confirm payment".

#### Easy memory

```
iframe = an embedded document
⚠️ CLICKJACKING ⭐ → defend with X-Frame-Options / CSP frame-ancestors
sandbox to restrict it | title for accessibility ⭐ | loading="lazy"
```

---

### Q: On-page SEO basics?

```
✅ ONE <h1> per page, then a logical h2/h3 hierarchy ⭐
✅ <title> and <meta name="description">
✅ Semantic structure (<main>, <article>) ⭐
✅ alt text on images
✅ Clean, readable URLs
✅ Fast load + mobile-friendly (Core Web Vitals) ⭐
✅ Structured data (schema.org JSON-LD)
```

⚠️ **The SPA problem — worth raising:**

```
An Angular SPA ships an EMPTY <div id="app"></div>.
A crawler that doesn't execute JavaScript sees NOTHING 💥
      ↓
Fix: Angular Universal (SSR) or prerendering ⭐
```

#### Easy memory

```
One <h1> ⭐ | title + description | semantic tags | alt | fast + mobile
⚠️ An SPA renders empty HTML → crawlers see nothing → SSR/prerender ⭐
```

---

## CSS

### Q: Explain the CSS box model.

```
Every element is FOUR nested boxes: ⭐

  ┌─────────── MARGIN ──────────────┐   space OUTSIDE (transparent)
  │  ┌──────── BORDER ───────────┐  │
  │  │  ┌───── PADDING ────────┐ │  │   space INSIDE (takes the background)
  │  │  │  ┌── CONTENT ─────┐  │ │  │
  │  │  │  │  width/height  │  │ │  │
  │  │  │  └────────────────┘  │ │  │
  │  │  └──────────────────────┘ │  │
  │  └───────────────────────────┘  │
  └─────────────────────────────────┘
```

⭐ **The question they actually want — `box-sizing`:**

```css
/* DEFAULT: content-box */
width: 200px; padding: 20px; border: 5px;
→ ACTUAL width = 200 + 40 + 10 = 250px 💥 ⭐

/* border-box ⭐ */
box-sizing: border-box;
width: 200px; padding: 20px; border: 5px;
→ ACTUAL width = 200px ✅ (padding and border grow INWARD)
```

```css
*, *::before, *::after { box-sizing: border-box; }   /* ⭐ every modern reset does this */
```

Real-world idea: **a gift box.** `content-box` measures only the item, so wrapping paper makes the parcel bigger. `border-box` measures the *finished parcel*.

#### Easy memory

```
content → padding → border → margin ⭐

content-box (default) → width = CONTENT only → padding ADDS to it 💥
border-box ⭐         → width = the WHOLE box → padding grows INWARD ✅

Every reset starts with: * { box-sizing: border-box; } ⭐
```

---

### Q: What is margin collapsing?

```
Two VERTICAL margins that touch MERGE into one —
the LARGER wins, they don't add up. ⭐
```

```html
<div style="margin-bottom: 30px">A</div>
<div style="margin-top: 20px">B</div>

<!-- gap = 30px, NOT 50px ⭐ -->
```

```
Only VERTICAL margins collapse. Horizontal ones NEVER do ⭐
```

It happens in three places:

```
1. Between ADJACENT siblings ⭐
2. Between a PARENT and its FIRST/LAST child (the margin "escapes" the parent) 💥
3. On an EMPTY element (its own top and bottom margins collapse together)
```

⭐ Case 2 is the confusing one:

```html
<div class="parent">
  <div style="margin-top: 50px">child</div>
</div>
<!-- the PARENT moves down 50px, not the child 💥 -->
```

Fixes: give the parent `padding`, a `border`, `overflow: hidden`, or make it a **flex/grid container** — margins never collapse inside flex or grid ⭐.

#### Easy memory

```
Adjacent VERTICAL margins MERGE → the LARGER wins (30 + 20 = 30 ⭐, not 50)
Horizontal margins NEVER collapse ⭐

⚠️ A child's margin can ESCAPE and move the PARENT 💥
Fix: padding/border on the parent, or use FLEX/GRID (no collapsing there) ⭐
```

---

### Q: Explain CSS `position` values.

```
static   → the default; in the normal flow; top/left do NOTHING ⭐
relative → in the flow, but nudged from its own position ⭐
absolute → REMOVED from the flow, positioned against the nearest
           POSITIONED ancestor ⭐
fixed    → removed; positioned against the VIEWPORT (stays on scroll)
sticky   → relative until it hits a threshold, then fixed ⭐
```

```
        NORMAL FLOW                REMOVED FROM FLOW
        static, relative           absolute, fixed
        (still occupies space)     (leaves a gap behind it) ⭐
```

⭐ **The rule that makes `absolute` click:**

```
`absolute` positions itself against the nearest ancestor with
position OTHER than static ⭐

No positioned ancestor? → it jumps to the whole PAGE 💥
      ↓
Which is why the pattern is ALWAYS:
   parent { position: relative }
   child  { position: absolute; top: 0; right: 0 }   ⭐
```

`sticky` needs a threshold or it does nothing:

```css
position: sticky; top: 0;      /* ⭐ `top` is REQUIRED */
```

⚠️ And `sticky` silently fails if any ancestor has `overflow: hidden` — a classic waste of an afternoon.

#### Easy memory

```
static (default) | relative (nudge, keeps its space) ⭐
absolute (out of flow, vs the nearest POSITIONED ancestor ⭐)
fixed (vs the VIEWPORT) | sticky (relative → fixed at a threshold ⭐)

THE PATTERN: parent relative + child absolute ⭐
⚠️ sticky needs `top:` and breaks under an ancestor's overflow:hidden 💥
```

---

### Q: What are the main `display` types?

```
block        → new line, full width
inline       → in the flow, ignores width/height ⚠️
inline-block → in the flow, ACCEPTS width/height ⭐
flex         → 1-DIMENSIONAL layout (a row OR a column) ⭐
grid         → 2-DIMENSIONAL layout (rows AND columns) ⭐
none         → REMOVED from the page entirely (no space kept) ⭐
contents     → the box disappears; the children stay (useful in grid)
```

#### Easy memory

```
block | inline ⚠️ | inline-block ⭐ | flex (1-D ⭐) | grid (2-D ⭐) | none (gone ⭐)
```

---

### Q: How is CSS specificity calculated?

```
Count FOUR numbers: (inline, IDs, classes, elements) ⭐
Compare left to right — a higher number always wins.
```

```
inline style          1,0,0,0   ⭐ beats everything except !important
#id                   0,1,0,0   ⭐
.class / [attr] / :hover  0,0,1,0
element / ::before    0,0,0,1
*                     0,0,0,0
```

```css
#nav .item a        →  0,1,1,1     ⭐ WINS
.nav .item a.link   →  0,0,3,1
a                   →  0,0,0,1
```

⭐ **The rule that surprises people:**

```
ONE id beats ANY number of classes. ⭐
   #nav (0,1,0,0)  >  .a.b.c.d.e.f.g.h.i.j (0,0,10,0)

The columns are compared LEFT TO RIGHT, never added up ⭐
```

Tie-break: **the last rule in the source order wins.**

Real-world idea: **military rank.** One General outranks any number of Sergeants — you don't add sergeants together.

#### Easy memory

```
(inline, ID, class, element) — compare LEFT to RIGHT ⭐
inline 1,0,0,0 | #id 0,1,0,0 | .class 0,0,1,0 | tag 0,0,0,1

ONE id beats TEN classes ⭐ (military rank 🎖️ — you don't add up sergeants)
Tie → the LAST rule wins
```

---

### Q: What does `!important` do?

```
!important overrides EVERYTHING except another !important
(and inline !important). ⭐
```

```
The cascade, highest first:
   1. !important on an inline style
   2. !important in a stylesheet ⭐
   3. inline style
   4. #id → .class → element
```

⚠️ Why it's discouraged:

```
It breaks the cascade, so the ONLY way to override it is ANOTHER !important
      ↓
an escalating war of !important across the codebase 💥
```

```
✅ Legitimate uses: overriding a third-party library you can't edit ⭐,
   utility classes (.hidden { display: none !important }), print stylesheets
```

#### 🔵 Relevant to your stack

Overriding **PrimeNG** or **Angular Material** internals is exactly the case where `!important` (or `::ng-deep`, which is deprecated) shows up — worth naming as the honest exception.

#### Easy memory

```
!important beats the cascade ⭐ → the only counter is another !important 💥
Legit: third-party overrides ⭐, utility classes, print styles
Otherwise it's a specificity war
```

---

### Q: Inline vs internal vs external CSS?

```
INLINE   → style="" on the element      → highest specificity ⚠️, unreusable
INTERNAL → <style> in the <head>        → one page only
EXTERNAL → <link> to a .css file        → reusable + CACHED ⭐
```

```
EXTERNAL wins because the browser CACHES it ⭐
   → the second page load costs nothing
```

⭐ The exception worth naming: **critical CSS is deliberately inlined** in the `<head>` so the first paint doesn't wait for a network round trip.

#### Easy memory

```
external ⭐ (reusable + CACHED) > internal > inline ⚠️
Exception: CRITICAL CSS is inlined on purpose, for the first paint ⭐
```

---

### Q: Pseudo-classes vs pseudo-elements?

```
PSEUDO-CLASS   : one colon  → a STATE of an existing element ⭐
PSEUDO-ELEMENT :: two colons → a NEW element that isn't in the HTML ⭐
```

```css
/* pseudo-CLASS — a state */
a:hover  input:focus  li:first-child  li:nth-child(2n)  input:disabled
:not(.active)   li:last-child

/* pseudo-ELEMENT — creates something */
p::before { content: "→"; }     /* ⭐ `content` is REQUIRED */
p::after
p::first-line   p::first-letter   ::placeholder   ::selection
```

⭐ The memory hook: **one colon = a condition, two colons = a creation.**

⚠️ `::before` and `::after` **do nothing without `content`** — even `content: ""`.

#### Easy memory

```
:hover  ONE colon  = a STATE ⭐ (a condition)
::before TWO colons = a NEW element ⭐ (a creation)

⚠️ ::before/::after need `content:` — even content: "" ⭐
```

---

### Q: `em` vs `rem` vs `px` vs `%`?

```
px  → a fixed, absolute size ⭐
em  → relative to the PARENT's font size ⚠️ (it COMPOUNDS)
rem → relative to the ROOT font size ⭐ (predictable — the default choice)
%   → relative to the parent's corresponding property
```

⚠️ **The `em` compounding trap:**

```html
<div style="font-size: 2em">          <!-- 32px (root 16) -->
  <div style="font-size: 2em">        <!-- 64px 💥 not 32 -->
    <div style="font-size: 2em">      <!-- 128px 💥💥 -->
```

```
em MULTIPLIES down the tree ⭐
rem always measures from the ROOT → no surprises ⭐
```

```
Use rem ⭐ → font sizes, spacing (it also respects the user's browser font setting ⭐)
Use em     → padding that should scale WITH this element's own text ⭐
Use px     → borders, hairlines, precise 1px details
Use %      → widths in fluid layouts
```

⭐ The accessibility point: **a user who sets a larger default font size gets it with `rem`, and is ignored by `px`.**

#### Easy memory

```
px = fixed | em = vs the PARENT ⚠️ COMPOUNDS | rem ⭐ = vs the ROOT | % = vs the parent

rem for font sizes and spacing ⭐ (respects the user's browser setting)
em for padding that scales with its own text
```

---

### Q: What is `z-index` and a stacking context?

```
z-index = the front-to-back order — but ONLY within a stacking context ⭐
And it only works on a POSITIONED element (not `static`) ⚠️
```

⭐ **The trap that produces "my z-index: 9999 doesn't work":**

```
A new STACKING CONTEXT is created by:
   position + a z-index other than auto ⭐
   opacity < 1 ⭐⭐  (the sneaky one)
   transform, filter, will-change ⭐
   position: fixed / sticky
```

```
Parent A (z-index: 1)          Parent B (z-index: 2)
   └── child (z-index: 9999)      └── child (z-index: 1)

Parent B's child WINS ⭐ — the children are compared INSIDE
their parents, and the PARENTS are compared first 💥
```

Real-world idea: **floors in a building.** A person standing on a chair on the 1st floor is still below someone lying on the 2nd floor. `z-index` only orders you *within your own floor*.

⚠️ The one that wastes hours: adding `opacity: 0.99` or a `transform` for an animation **creates a stacking context** and silently traps every child behind a sibling.

#### Easy memory

```
z-index works ONLY on POSITIONED elements ⚠️
It's scoped to the STACKING CONTEXT ⭐ (floors in a building 🏢)

Created by: position+z-index | opacity < 1 ⭐⭐ | transform | filter | fixed/sticky
"z-index: 9999 doesn't work" → the PARENT's context is losing ⭐
```

---

## Layout

### Q: Explain Flexbox and its main properties.

```
Flexbox = ONE-DIMENSIONAL layout — a row OR a column. ⭐
```

```css
.container {
  display: flex;
  flex-direction: row | column;              /* ⭐ sets the MAIN axis */
  justify-content: center | space-between;   /* ⭐ along the MAIN axis */
  align-items: center | stretch;             /* ⭐ across the CROSS axis */
  flex-wrap: wrap;
  gap: 16px;                                  /* ⭐ modern spacing */
}
.item { flex: 1; }                            /* grow | shrink | basis */
```

⭐ **The one thing that makes flexbox click:**

```
justify-content → the MAIN axis   (the direction you set) ⭐
align-items     → the CROSS axis  (the other one) ⭐

flex-direction: row    → main = horizontal ↔, cross = vertical ↕
flex-direction: column → main = VERTICAL ↕, cross = HORIZONTAL ↔ ⭐ (they SWAP!)
```

```
That swap is why "justify-content: center didn't centre it horizontally"
— because the direction was `column` 💥 ⭐
```

```
row:     [1][2][3]  →  justify = ↔   align = ↕
column:  [1]
         [2]        →  justify = ↕   align = ↔  ⭐ swapped
         [3]
```

`flex: 1` explained:

```
flex: 1  =  flex-grow: 1; flex-shrink: 1; flex-basis: 0 ⭐
         → "take an equal share of the free space"
```

#### Easy memory

```
Flexbox = ONE dimension (row OR column) ⭐
justify-content = MAIN axis ⭐ | align-items = CROSS axis ⭐
flex-direction: column SWAPS them 💥 ⭐ — the #1 confusion
flex: 1 = grow 1, shrink 1, basis 0 ⭐ | gap for spacing
```

---

### Q: Explain CSS Grid basics.

```
Grid = TWO-DIMENSIONAL layout — rows AND columns at the same time. ⭐
```

```css
.container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);      /* ⭐ 3 equal columns */
  grid-template-columns: 200px 1fr auto;      /* mixed */
  grid-template-rows: auto 1fr auto;
  gap: 16px;
}
.item {
  grid-column: 1 / 3;        /* span columns 1–2 ⭐ */
  grid-area: header;
}
```

⭐ Two things worth knowing beyond the basics:

```
1. `fr` = a fraction of the FREE space ⭐ — it accounts for gaps,
   which percentages don't 💥

2. RESPONSIVE WITH NO MEDIA QUERY ⭐⭐
   grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      → as many columns as fit, each at least 250px, wrapping automatically
```

That second line is the single most impressive thing to write on a whiteboard for a layout question.

Named areas — very readable:

```css
grid-template-areas:
  "header header"
  "sidebar main"
  "footer footer";
```

#### Easy memory

```
Grid = TWO dimensions ⭐ (rows AND columns)
repeat(3, 1fr) | fr = a share of the FREE space ⭐ (percentages ignore gaps 💥)

⭐⭐ repeat(auto-fit, minmax(250px, 1fr)) = responsive with NO media query
grid-template-areas = a readable ASCII layout ⭐
```

---

### Q: Flexbox vs Grid — when to use which?

```
FLEX → ONE direction, content-driven ⭐ (a nav bar, a toolbar, a card's footer)
GRID → TWO directions, layout-driven ⭐ (a page skeleton, a photo gallery)
```

```
FLEX: "put these items in a line and space them"
GRID: "here is the page structure; put things in the cells" ⭐
```

⭐ The line to say: *"Flexbox is content-first — the items decide the layout. Grid is layout-first — the container decides, and the items fill it."*

And they compose: a **grid** page skeleton with **flex** inside each cell is the standard combination.

#### Easy memory

```
FLEX = 1-D, content-first ⭐ (navbar, toolbar, button row)
GRID = 2-D, layout-first ⭐ (page skeleton, gallery, dashboard)
Use BOTH: grid for the page, flex inside each cell ⭐
```

---

### Q: How do you centre a div?

⭐ The interviewer wants **more than one** answer.

```css
/* 1. FLEX — the modern default ⭐ */
.parent { display: flex; justify-content: center; align-items: center; }

/* 2. GRID — the shortest of all ⭐ */
.parent { display: grid; place-items: center; }

/* 3. ABSOLUTE + transform — works without touching the parent's layout ⭐ */
.child { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); }

/* 4. HORIZONTAL only — a block with a known width */
.child { margin: 0 auto; width: 300px; }
```

```
⭐ Why `translate(-50%, -50%)` is needed in #3:
   top: 50% puts the child's TOP EDGE at the middle.
   translate(-50%,-50%) pulls it back by HALF ITS OWN SIZE ⭐
   → and % on transform refers to the ELEMENT's own size, which is
     why it works without knowing the width
```

#### Easy memory

```
place-items: center ⭐ (grid — the shortest)
flex + justify-content + align-items ⭐
absolute + top/left 50% + translate(-50%,-50%) ⭐ (half its OWN size)
margin: 0 auto (horizontal only, needs a width)
```

---

## Responsive

### Q: What are media queries?

```css
@media (min-width: 768px)  { }      /* ⭐ mobile-first: styles ADD as it grows */
@media (max-width: 767px)  { }      /* desktop-first */
@media (orientation: landscape) { }
@media (prefers-color-scheme: dark) { }        /* ⭐ dark mode */
@media (prefers-reduced-motion: reduce) { }    /* ⭐ accessibility */
@media print { }
```

⭐ The last two are the ones that make you sound current:

```
prefers-color-scheme    → respect the user's OS dark mode ⭐
prefers-reduced-motion  → users with vestibular disorders can be made
                          physically unwell by animation ⭐ — disable it
```

#### Easy memory

```
@media (min-width: 768px) ⭐ mobile-first | max-width = desktop-first
prefers-color-scheme ⭐ (dark mode) | prefers-reduced-motion ⭐ (accessibility)
```

---

### Q: What is mobile-first design?

```
Mobile-first = write the MOBILE styles as the base,
               then ADD complexity with min-width. ⭐
```

```css
/* base = MOBILE ⭐ */
.container { padding: 16px; }

@media (min-width: 768px)  { .container { padding: 32px; } }   /* tablet */
@media (min-width: 1024px) { .container { padding: 48px; } }   /* desktop */
```

```
MOBILE-FIRST ⭐              DESKTOP-FIRST
base = simple                base = complex
ADD as it grows ⭐           REMOVE as it shrinks
phone downloads less ⭐      phone parses desktop CSS it will never use 💥
```

⭐ The performance argument is the real one: the constrained device gets the **simplest** stylesheet.

#### Easy memory

```
Mobile-first ⭐ = base is MOBILE, then min-width ADDS ⭐
Why: the weakest device does the LEAST work ⭐
Desktop-first = max-width, subtracting — the phone parses styles it never uses 💥
```

---

### Q: Why is the viewport meta tag needed?

```html
<meta name="viewport" content="width=device-width, initial-scale=1.0">
```

```
WITHOUT it: a mobile browser pretends to be ~980px wide, renders the
            desktop layout, and ZOOMS OUT 💥
            → your media queries NEVER fire ⭐

WITH it:    the viewport = the real device width → media queries work ✅
```

⚠️ Never add `user-scalable=no` — it blocks pinch-zoom, which is an accessibility failure.

#### Easy memory

```
width=device-width, initial-scale=1 ⭐
Without it → the phone fakes 980px and zooms out → media queries never fire 💥
⚠️ Never user-scalable=no (accessibility) ⭐
```

---

### Q: What are responsive units?

```
%     → relative to the parent
vw/vh → 1% of the viewport WIDTH / HEIGHT ⭐
vmin/vmax → the smaller / larger of the two
rem   → relative to the root font size ⭐
ch    → the width of a "0" — great for line length ⭐
clamp(min, preferred, max) ⭐⭐ → fluid, with hard limits
```

```css
font-size: clamp(1rem, 2.5vw, 2rem);      /* ⭐ scales, but never too small or huge */
max-width: 65ch;                           /* ⭐ the readable line length */
```

⭐ `clamp()` is the modern answer to "how do you make typography responsive?" — one line, no media queries.

⚠️ The `vh` mobile trap:

```
100vh on mobile includes the area BEHIND the browser's address bar
   → content gets cut off 💥
Fix: 100dvh (dynamic viewport height) ⭐
```

#### Easy memory

```
% | vw/vh ⭐ | rem ⭐ | ch (line length ⭐) | clamp(min, fluid, max) ⭐⭐
clamp(1rem, 2.5vw, 2rem) = responsive type with NO media query ⭐
⚠️ 100vh is broken on mobile (the address bar) → use 100dvh ⭐
```

---

### Q: How does the Bootstrap grid work?

```
12 COLUMNS ⭐, inside .container → .row → .col-*
```

```html
<div class="container">
  <div class="row">
    <div class="col-12 col-md-6 col-lg-4">A</div>   <!-- ⭐ full → half → third -->
    <div class="col-12 col-md-6 col-lg-4">B</div>
  </div>
</div>
```

```
Breakpoints: sm 576 | md 768 | lg 992 | xl 1200 | xxl 1400
Mobile-first ⭐ — col-md-6 applies from 768px UPWARD ⭐
The numbers in a row should total 12; beyond that it WRAPS ⭐
```

⚠️ Be honest about your stack:

> *"My projects use **PrimeNG** and **Angular Material** rather than Bootstrap, and Ionic has its own grid — but it's the same 12-column, mobile-first idea."*

#### Easy memory

```
12 columns ⭐ | container → row → col-*
sm 576 | md 768 | lg 992 | xl 1200
col-md-6 applies from 768 UPWARD ⭐ (mobile-first)
Your stack: PrimeNG / Material / Ionic grid — same 12-column idea ⭐
```

---

## Modern / Advanced

### Q: What are CSS variables (custom properties)?

```css
:root {
  --primary: #3880ff;
  --spacing: 16px;
}
.button {
  background: var(--primary);
  padding: var(--spacing, 8px);        /* ⭐ with a fallback */
}
```

⭐ **The two things that make them different from Sass variables:**

```
1. They are LIVE at RUNTIME ⭐
      element.style.setProperty('--primary', 'red');   → changes instantly
      (a Sass variable is compiled away and cannot change)

2. They CASCADE and INHERIT ⭐
      .dark-theme { --primary: #fff; }
      → every child using var(--primary) updates automatically ⭐
```

```
That is exactly how DARK MODE is implemented with no duplicate stylesheet ⭐
```

#### 🔵 In your RoboGebra / Ionic work — this is *the* answer ⭐

```
Ionic components use SHADOW DOM.
      ↓
Your normal CSS CANNOT reach inside them 💥
      ↓
Ionic exposes CSS VARIABLES as the styling API ⭐
```

```css
ion-button { --background: #3880ff; --border-radius: 8px; }
:root { --ion-color-primary: #3880ff; }
```

> 🗣️ *"Theming an Ionic app is done entirely through CSS custom properties, because the components use Shadow DOM — a normal selector can't cross that boundary. That was one of the genuinely new things to learn moving from Angular to Ionic: you style through the variables the component exposes, or `::part()`, not by targeting its internals."*

#### Easy memory

```
--name in :root, read with var(--name, fallback) ⭐
LIVE at runtime ⭐ + CASCADES ⭐ (unlike Sass variables)
→ dark mode with no duplicate stylesheet ⭐

🔵 Ionic: Shadow DOM means CSS variables ARE the styling API ⭐
```

---

### Q: Transitions vs animations?

```
TRANSITION → A → B, needs a TRIGGER (hover, a class change) ⭐
ANIMATION  → keyframes, runs BY ITSELF, can LOOP ⭐
```

```css
/* TRANSITION — needs a trigger */
.btn { transition: background 0.3s ease; }
.btn:hover { background: blue; }

/* ANIMATION — self-running */
@keyframes spin { from { transform: rotate(0); } to { transform: rotate(360deg); } }
.loader { animation: spin 1s linear infinite; }        /* ⭐ loops forever */
```

```
transition → 2 states, triggered ⭐
animation  → MANY keyframes, automatic, loopable ⭐
```

⭐ **The performance rule that gets asked:**

```
Animate ONLY `transform` and `opacity` ⭐
   → the GPU handles them (the compositor), 60fps ⚡

Animating width/height/top/left triggers LAYOUT on every frame 🐢💥
```

```css
/* ❌ janky */  transition: left 0.3s;
/* ✅ smooth */ transition: transform 0.3s;
```

#### Easy memory

```
TRANSITION ⭐ = A→B, needs a TRIGGER | ANIMATION ⭐ = keyframes, self-running, loops

⚠️ Animate ONLY transform + opacity ⭐ (GPU, 60fps)
   width/height/top/left → LAYOUT every frame → jank 💥
```

---

### Q: What is the `transform` property?

```css
transform: translate(10px, 20px);
transform: rotate(45deg);
transform: scale(1.5);
transform: skew(10deg);
transform: translate(-50%, -50%) rotate(45deg);   /* ⭐ applied RIGHT to LEFT */
```

⭐ Three points worth knowing:

```
1. transform does NOT affect layout ⭐ — surrounding elements don't move,
   which is exactly why it's cheap to animate
2. Percentages are relative to the ELEMENT'S OWN size ⭐
   → which is why translate(-50%,-50%) centres without knowing the width
3. Multiple transforms apply RIGHT TO LEFT ⭐
```

⚠️ And the side effect: `transform` **creates a stacking context**, which can silently break a nearby `z-index`.

#### Easy memory

```
translate | rotate | scale | skew ⭐
Does NOT affect layout ⭐ → cheap to animate (GPU)
% is relative to the ELEMENT'S OWN size ⭐ → translate(-50%,-50%) centres
Applied RIGHT to LEFT ⭐ | ⚠️ creates a stacking context (z-index surprises)
```

---

### Q: `opacity: 0` vs `visibility: hidden` vs `display: none`?

| | `opacity: 0` | `visibility: hidden` | `display: none` |
|---|---|---|---|
| Takes up space | ✅ | ✅ | ❌ removed ⭐ |
| Clickable | ✅ **YES** ⚠️ | ❌ | ❌ |
| Animatable | ✅ ⭐ | ❌ (it's discrete) | ❌ |
| Read by a screen reader | ✅ ⚠️ | ❌ | ❌ |

⭐ **The two traps:**

```
1. opacity: 0 is STILL CLICKABLE ⭐
      → an invisible button intercepting clicks 💥
      → also still read aloud by a screen reader ⚠️

2. display: none cannot be TRANSITIONED ⭐
      → the standard fade-out is:
        opacity 0 + visibility hidden together ⭐
```

```css
.fade-out { opacity: 0; visibility: hidden; transition: opacity .3s, visibility .3s; }
```

#### Easy memory

```
opacity: 0     → invisible, KEEPS space, STILL CLICKABLE ⚠️⭐, animatable ⭐
visibility: hidden → invisible, keeps space, not clickable
display: none  → GONE, no space ⭐, cannot be animated ⭐

Fade-out = opacity + visibility together ⭐
```

---

### Q: What is BEM naming?

```
BEM = Block __ Element -- Modifier ⭐
```

```css
.card { }                 /* BLOCK — a standalone component */
.card__title { }          /* ELEMENT — a part of the block ⭐ */
.card--featured { }       /* MODIFIER — a variant ⭐ */
.card__title--large { }
```

```
Why: FLAT selectors → specificity stays at 0,0,1,0 ⭐
     → no nesting wars, no !important escalation
     → and the class name tells you exactly where it belongs ⭐
```

⚠️ Honest relevance to your stack:

```
Angular has COMPONENT-SCOPED styles (ViewEncapsulation) ⭐
   → the styles are already isolated, so BEM matters far less.
   It's still valuable for a SHARED global stylesheet.
```

#### Easy memory

```
.block__element--modifier ⭐ (.card__title--large)
Why: FLAT specificity ⭐ + self-documenting names
⚠️ Angular already scopes styles per component → BEM matters less ⭐
```

---

### Q: What causes reflow and repaint?

```
REFLOW (layout)  → recalculating GEOMETRY — EXPENSIVE 🐢 ⭐
REPAINT (paint)  → redrawing pixels, same geometry — cheaper
COMPOSITE        → just moving existing layers — CHEAPEST ⚡ ⭐
```

```
REFLOW    ← width, height, padding, margin, font-size, position,
            adding/removing DOM, reading offsetHeight ⭐
REPAINT   ← color, background, visibility, box-shadow
COMPOSITE ← transform, opacity ⭐⭐  (the GPU — no layout, no paint)
```

⭐ **The killer detail — layout thrashing:**

```js
for (const el of items) {
  el.style.height = el.offsetHeight + 10 + 'px';    // ⭐ READ then WRITE, in a loop
}
```

```
Reading offsetHeight FORCES a synchronous reflow, because the browser
must flush your pending writes to give you an accurate number.
      ↓
Read-write-read-write = a reflow PER ITERATION 💥 ("layout thrashing")
      ↓
Fix: READ everything first, THEN write everything ⭐
```

#### Easy memory

```
REFLOW (geometry) 🐢 > REPAINT (pixels) > COMPOSITE (transform/opacity) ⚡ ⭐

Animate transform + opacity ONLY ⭐
⚠️ LAYOUT THRASHING: reading offsetHeight in a write loop forces a
   reflow every iteration 💥 → batch READS, then WRITES ⭐
```

---

### Q: What is the critical rendering path?

```
HTML  → DOM   ┐
              ├──▶ RENDER TREE → LAYOUT → PAINT → COMPOSITE ⭐
CSS   → CSSOM ┘
```

```
⭐ CSS is RENDER-BLOCKING — nothing paints until the CSSOM is built
⭐ A plain <script> is PARSER-BLOCKING — it stops DOM construction dead
```

How to make it fast:

```
✅ Inline the CRITICAL CSS, load the rest asynchronously ⭐
✅ defer / async on scripts ⭐
✅ Minify and compress; use HTTP/2
✅ Preload key fonts; font-display: swap ⭐ (avoid invisible text)
✅ Lazy-load below-the-fold images (loading="lazy")
```

⭐ Tie it to Core Web Vitals — that's the modern framing:

```
LCP → the largest element painted  (loading speed)
INP → responsiveness to input      (replaced FID in 2024) ⭐
CLS → layout shift                 (set width/height on images! ⭐)
```

#### Easy memory

```
HTML→DOM + CSS→CSSOM → RENDER TREE → LAYOUT → PAINT → COMPOSITE ⭐

CSS is RENDER-blocking ⭐ | a plain <script> is PARSER-blocking ⭐
Fix: critical CSS inline + defer scripts + font-display: swap ⭐
Core Web Vitals: LCP | INP ⭐ (replaced FID) | CLS (size your images! ⭐)
```

---

## Quick Revision Sheet

```
HTML
  Semantic tags ⭐ = accessibility landmarks + SEO (labelled boxes 📦)
  block (new line) | inline (⚠️ ignores width/height) | inline-block ⭐
  async (any order ⚠️) vs defer ⭐ (in order, after parsing)
  localStorage (forever) | sessionStorage (per TAB) | cookie (sent to the server, 4KB)
     ⚠️ a token in localStorage is XSS-readable → HttpOnly cookie ⭐
  "The best ARIA is NO ARIA" ⭐ — use a real <button>
  <!DOCTYPE html> → standards mode (else the OLD box model 💥)

CSS
  box-sizing: border-box ⭐ (else padding ADDS to the width 💥)
  Vertical margins COLLAPSE → the larger wins ⭐ (never horizontal)
  position: parent RELATIVE + child ABSOLUTE ⭐ | sticky needs `top:` ⭐
  Specificity (inline, id, class, tag) — ONE id beats TEN classes ⭐ 🎖️
  :hover ONE colon = state | ::before TWO = a new element (needs `content`) ⭐
  rem ⭐ (root, respects user settings) | em ⚠️ COMPOUNDS | px | %
  z-index only on POSITIONED elements; scoped to the STACKING CONTEXT ⭐
     created by opacity < 1 ⭐⭐ and transform 💥

LAYOUT
  FLEX = 1-D ⭐ | GRID = 2-D ⭐
  justify-content = MAIN axis | align-items = CROSS axis
     ⚠️ flex-direction: column SWAPS them 💥 ⭐
  repeat(auto-fit, minmax(250px, 1fr)) ⭐⭐ = responsive, no media query
  Centre: place-items: center ⭐ | flex | absolute + translate(-50%,-50%) ⭐

RESPONSIVE
  Mobile-first ⭐ = base mobile + min-width (the weakest device does least work)
  viewport meta ⭐ — without it, media queries NEVER fire 💥
  clamp(1rem, 2.5vw, 2rem) ⭐⭐ | ⚠️ 100vh is broken on mobile → 100dvh ⭐

MODERN
  CSS variables: LIVE + CASCADE ⭐ → dark mode
     🔵 Ionic Shadow DOM → variables ARE the styling API ⭐
  Animate ONLY transform + opacity ⭐ (GPU) — never width/top 💥
  opacity: 0 is STILL CLICKABLE ⚠️⭐ | display: none can't be animated ⭐
  Reflow 🐢 > repaint > composite ⚡ | layout thrashing = read/write in a loop 💥
  CSS is render-blocking ⭐ | Core Web Vitals: LCP, INP ⭐, CLS
```

---

**Related files:** [01 — JavaScript](./01-javascript.md) · [04 — Angular](./04-angular.md) · [15 — Ionic (CSS variables + Shadow DOM)](./15-ionic-level1.md) · [25 — Angular Binding & Forms](./25-angular-binding-forms.md)
