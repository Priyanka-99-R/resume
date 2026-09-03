# 🗓️ Study Plan — IQVIA (Angular / Ionic Technical Lead)

> ⚠️ **This plan replaces the old July/Berribot one.** Rewritten **3 Sept 2026** for your actual next interview.
>
> **Target:** IQVIA — Angular / Ionic **Technical Lead**
> **Their checklist:** ~**75% frontend** (Angular · TS/JS · RxJS/NgRx · Ionic+Capacitor) + REST/Security · MongoDB/SQL · Microservices · Git/CI-CD/AWS · **Tech Lead** · AI-assisted dev
> **Main file:** **[38 — IQVIA Full Prep Pack](./38-iqvia-technical-lead-prep.md)** — everything below feeds into it.

---

## 🧠 The one thing that decides this round

```
This role is 75% YOUR STRONGEST GROUND. ⭐

Altimetrik was lost on Spring depth. IQVIA is Angular + Ionic —
which is what you build every day. Do NOT over-prepare backend
at the cost of the frontend lead answers. ⭐
```

### The three habits — apply them to every answer

```
1. DEFINITION → then ONE thing from YOUR code → then ONE trade-off ⭐
      "OnPush skips checking unless an input reference changes.
       We use it on the progress lists — not globally, because on a
       mature codebase that ships silent bugs wherever an array is mutated."

2. Say the NUMBER, not the adjective ⭐
      ❌ "we handle subscriptions carefully"
      ✅ "over four hundred takeUntil calls" ⭐

3. When you haven't used it, say the MIGRATION PLAN ⭐
      ❌ "yes we use signals"
      ✅ "studied, not shipped — I'd start with signal inputs on leaf components"
```

---

## 📅 The plan — 7 days (compress or stretch to your window)

### Day 1 — Your story + your evidence ⭐

```
□ [00 Self-Intro] — the 60-second version, out loud, 5 times ⭐
□ [39 RoboGebra Code Examples] — the whole file
□ [38 IQVIA] §"YOUR EVIDENCE" table — one real file per area ⭐
□ Fill in the [COMPANY] blanks: "Why IQVIA?" + "What do you know about IQVIA?"

GOAL: the 60-second intro with NO notes, ending on
      "I built the Ionic mobile app for iOS and Android" ⭐
```

### Day 2 — Angular core (their biggest area)

```
□ [04 Angular] — lifecycle · DI · routing/guards · change detection ⭐
□ [38 IQVIA] Part 1 (all of it — 1.1 → 1.13)
□ Re-read the SIGNALS honesty guard ⭐ (you have 0 usages)

GOAL: lifecycle order cold · OnPush's 4 triggers · 5 sources of memory leaks
      Say the guard answer for signals out loud ⭐
```

### Day 3 — RxJS + state

```
□ [20 RxJS] — the four flattening operators + WHERE catchError goes ⭐
□ [21 NgRx] — the bank-ledger model + "why we chose NOT to use it" ⭐
□ [38 IQVIA] Part 3

GOAL: draw the 4 marble diagrams from memory ⭐
      write the typeahead pipeline (debounceTime → filter → switchMap) cold ⭐
      say the "BehaviorSubject not NgRx, deliberately" answer ⭐
```

### Day 4 — Ionic + Capacitor ⭐ YOUR DIFFERENTIATOR

```
□ [15 Ionic] — lifecycle ⭐ · CSS variables/Shadow DOM · storage · plugins
□ [38 IQVIA] Part 4
□ Memorise your 19 plugins and the 3 with a story ⭐

GOAL: ngOnInit vs ionViewWillEnter, with the STALE LIST bug ⭐
      "Preferences over localStorage — the OS can evict WebView storage" ⭐
      Rehearse the mobile-app story until it's smooth ⭐⭐
```

⭐ **Most Angular candidates cannot answer this section at all. It's on their list. This is where you win.**

### Day 5 — TypeScript + REST/Security + data

```
□ [03 TypeScript] — generics ⭐ · type guards · utility types · strict flags
□ [38 IQVIA] Parts 2, 5, 6
□ [13 MongoDB] — the N+1 → aggregation story ⭐
□ [36 SQL] — the 3 master ideas + joins + GROUP BY/HAVING

GOAL: explain PageResponse<T> + fromJSON<U> ⭐
      the JWT/Cognito/JWKS answer ⭐
      the N+1 aggregation story, three questions deep ⭐
```

### Day 6 — Tech Lead + architecture ⭐

```
□ [38 IQVIA] Part 9 (Technical Lead) + Part 10 (AI-assisted dev) ⭐
□ [10 Projects] — the CAPS frame + your 3 hard-problem stories
□ [08 Microservices] — the "modular monolith, monolith first" answer ⭐
□ [07 AWS] — S3 presigned URLs · Cognito · LocalStack · Jenkins ⭐

GOAL: draw YOUR architecture from memory (4 clients → Spring Boot →
      MongoDB/S3/Cognito/Firebase/Razorpay) ⭐
      The Angular 16 migration as your LEADERSHIP story ⭐
      Code review, mentoring, tech-debt and estimation answers ready
```

### Day 7 — Mock + the honesty pass ⭐

```
□ Full dry run, out loud: intro → Angular → RxJS → Ionic → lead → questions
□ Re-read the FOUR "do not claim" items ⭐
□ [11 HR] — the STAR stories + your questions for them
□ Re-read [18] the version numbers — say them without pausing ⭐

GOAL: nothing surprises you. You know what you'll say when you DON'T know.
```

---

## ⚠️ The four things to never claim — read this the morning of

```
❌ SIGNALS          → 0 usages  → "studied, not shipped; I'd start with signal inputs" ⭐
❌ STANDALONE       → 2 only    → "new code is standalone-first; legacy converts when touched" ⭐
❌ NgRx on RoboGebra→ 0 usages  → "that was EasyVisa; here we chose BehaviorSubject deliberately" ⭐
❌ MICROSERVICES    → monolith  → "modular monolith. Monolith first." ⭐

And the stack facts:
   RoboGebra = MONGODB (not MySQL) ⭐
   Angular 16.2 web · 18.2 mobile · Ionic 8.7.5 · Capacitor 6.2.1
   Java 17 · Spring Boot 3.2.0
```

⭐ **Every one of these is stronger as an honest answer.** For a *lead* role they are testing judgement, not vocabulary — "we chose not to, and here's the trade-off" beats the buzzword every time.

---

## 🎯 Interview-day checklist

```
□ 60-second intro, no notes, ending on the IONIC MOBILE APP ⭐
□ "Why IQVIA?" and "What do you know about IQVIA?" — SPECIFIC, not generic ⭐
□ Three stories rehearsed:
     Ionic mobile app ⭐⭐ | N+1 aggregation fix ⭐⭐ | Angular 16 migration ⭐
□ Can draw YOUR architecture on a whiteboard ⭐
□ Version numbers without pausing ⭐
□ The healthcare line, used once early:
     "I'd treat auditability and PHI handling as functional requirements,
      not afterthoughts" ⭐
□ 2–3 questions ready for them ⭐
□ The four "do not claim" items firmly in mind ⭐
```

---

## 📚 If you only have ONE day

```
MORNING   [00] intro out loud ⭐ · [39] your evidence ⭐ · [38] the EVIDENCE table
MIDDAY    [38] Part 1 Angular · Part 3 RxJS · Part 4 IONIC ⭐⭐
AFTERNOON [38] Part 9 Tech Lead ⭐ · the 3 stories rehearsed
EVENING   the four "do not claim" items · version numbers · your questions ⭐
```

---

## 🔁 After the interview — do this the same day

Add the round to **[26 — Companies: Questions Actually Asked](./26-companies-asked-questions.md)** while it's fresh:

```
□ Every question they asked, in their words
□ What you answered — and what you WISH you'd answered ⭐
□ Anything you couldn't answer → that's your next study item ⭐
□ The format (rounds, duration, who was on the call)
```

⭐ That log is why this pack keeps getting sharper. **The same ~25 topics repeat across companies** — lifecycle hooks have now come up in five rounds, interceptors in four.

---

**Related files:** [38 — IQVIA Prep](./38-iqvia-technical-lead-prep.md) ⭐ · [39 — RoboGebra Code Examples](./39-robogebra-code-examples.md) · [00 — Self-Introduction](./00-self-introduction.md) · [26 — Companies Asked](./26-companies-asked-questions.md) · [18 — RoboGebra Versions](./18-robogebra-technical-versions.md)
