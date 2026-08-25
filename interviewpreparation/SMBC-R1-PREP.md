# SMBC Global Services — R1 Technical Interview Prep
**Today, 2–3 PM · Webex · Backend (Java/Maven)**

Your two assessments are Java 17 + Maven + Spring Boot multi-module projects, so the
**Backend Roles** prerequisites are the ones that apply to you.

---

## 1. Environment — verified, nothing to install

| Requirement | Status |
|---|---|
| JDK | Java 17.0.18 (Corretto) ✅ |
| Build tool | Maven 3.8.1 ✅ |
| IDE | IntelliJ IDEA + VS Code ✅ |
| Starter project (class returning true + test) | Built, 12 tests green ✅ |
| ATM assessment | 82 tests green, runs **offline** ✅ |
| Canvas assessment | 76 tests green, runs **offline** ✅ |

Offline matters: if interview wifi drops, every build still works. Dependencies are cached.

### The starter project they asked for
`interviewpreparation/smbc-interview-starter/`

- `Sample.java` → `isReady()` returns `true`
- `SampleTest.java` → JUnit 5 + AssertJ, 12 tests

```bash
cd /Users/safi/workspace/resume/interviewpreparation/smbc-interview-starter
mvn test
```

Use `Sample.java` as the scratch class for anything they ask you to live-code.

---

## 2. Do this before 2 PM (30 min)

1. **Open IntelliJ now**, import all three projects, let indexing finish.
   Cold indexing on a shared screen is the classic time-waster.
2. **Run each suite once** so Maven is warm — a first run that downloads is 2 minutes of silence.
3. **Increase IDE font size** to ~16–18pt. Webex compresses; the interviewer must read your code.
4. **Close Slack/WhatsApp/mail.** Share a *single window*, not the whole desktop.
5. **Have a terminal open** in each project dir, ready to run tests.

---

## 3. ⚠️ The ATM demo trap — read this

Your very first instinct demo **fails**:

```
$ login Alice
$ deposit 100
$ transfer Bob 50
Error: No such customer: Bob        <- looks like a bug on screen
```

That is **your deliberate design decision** (README §6): a transfer to a name that never
logged in is refused, so a typo like `transfer Alcie 500` can't open an account and book a
real debt against it. But under interview pressure it reads as a defect.

**Demo this script instead** — it hits the strongest parts of your design:

```
login Bob
logout
login Alice
deposit 210
transfer Bob 30       -> Transferred $30, balance $180
withdraw 500          -> refused: a withdrawal has nobody to owe
deposit 10.999        -> refused, not rounded
logout
exit
```

If it *does* come up, say the line out loud: *"That's intentional — I refuse transfers to
unknown customers so a mistyped name can't create an account with a debt against it. It's a
one-line change in `Bank.transfer` if the intent was the opposite."* That turns a
looks-like-a-bug moment into a design answer.

**Canvas demo** (safe, replays the brief exactly):
```
C 20 4
L 1 2 6 2
R 14 1 18 3
B 10 3 o
Q
```

---

## 4. Your 60-second pitch (memorise the shape, not the words)

> "Both are three Maven modules with dependencies pointing only inward. The domain module
> has **no `<dependencies>` section at all** — no Spring, no JSON — so the business rules
> can't accidentally acquire a framework dependency. The CLI and web modules can't see each
> other, so either could be deleted without touching a line of business logic. The payoff is
> in the tests: the domain tests run in under a second with no container and no mocking."

Then stop. Let them pick the thread.

---

## 5. The questions they will ask — your answers are already in your README

### ATM

**"Why `BigDecimal` and not `double`?"**
`double` can't represent `0.10`, so balances drift — ten deposits of ten cents wouldn't make
a dollar, and `MoneyTest` asserts exactly that case. A `long` of cents would be correct but
scatters `/100` and `*100` across every call site, where the first omission is a silent
hundredfold error. `Money` wraps `BigDecimal` at scale 2, is immutable, and **cannot be
negative** — debt direction is an enum, so a dropped minus sign can't invert an obligation.

**"Why do obligations net per pair?"**
The brief's own sample forces it: Alice transfers $30 to Bob and *her balance doesn't move*.
The only reading that produces that is netting — Bob owed her $40, so $30 cancels against it
and no cash moves. Tracked separately, she'd be owed $40 while owing $30: different balance,
different display.

**"Why is settlement iterative, not recursive?"**
Paying a creditor raises *their* balance, which may let them pay *theirs*. Netting kills
two-party cycles but not three-party ones, so it's an explicit work queue — recursion risks
stack overflow. It terminates because a customer is only enqueued after a strictly positive
payment, and every payment strictly reduces the finite total debt. `BankTest` has a three-way
cycle.

**"Why is the lock in the web module and not in `Bank`?"**
A Spring bean is a singleton shared across request threads, so two concurrent transfers
interleave a read with someone else's write and lose money. But the CLI is one person at one
terminal with no contention — making the domain synchronise charges it for a problem it
doesn't have and buries a deployment concern in business rules. **The adapter that introduces
concurrency is the adapter that pays for it.**

*Follow-up they'll ask: "why one coarse lock?"* — per-account locking means acquiring two
locks for a transfer, which is the classic lock-ordering deadlock. At a scale where that
mattered the answer is a database with row-level locking, not cleverer monitors.

**"Why do amounts cross the wire as strings?"**
As a JSON number, a JavaScript client parses it into a `double` — reintroducing exactly the
rounding error the domain took care to avoid. There's a test for it.

**"Why unchecked exceptions?"**
Every violation extends `AtmException` with a customer-safe message, so each adapter
translates the whole family at one point: one `catch` in the shell, one
`@RestControllerAdvice` in web. Checked exceptions push `throws` through layers that can't act
on them, which produces the empty `catch` block that hides them. Unexpected exceptions are
deliberately **not** caught — a defect should surface.

### Canvas

**"Is your bucket fill four-way or eight-way?"** ← your strongest answer, land it
Four-way. And the interesting part: the brief's sample output **does not settle it** — both
sealed regions there are closed diagonally too, so that example gives an identical picture
either way. I checked rather than assuming. They differ only when a wall touches at a corner,
so I wrote `DrawingTest.doesNotLeakThroughDiagonals` to pin it. Four-way because it's what
paint programs do, and it's conservative: being wrongly contained is easy to see and undo,
wrongly flooding the canvas destroys work.

**"Why a queue instead of recursive flood fill?"**
On a 1000×1000 canvas recursion can be a million frames deep; the JVM gives out around ten
thousand. An `ArrayDeque` moves that depth to the heap. No visited-set needed either —
painting a cell changes its colour so it can't match the target twice. Filling with the
colour already there is caught up front as a no-op, which is what stops the loop spinning.

**"How would you add a circle?"** ← they love this one
Three steps: one new class implementing `Drawing`; one `case` in `CommandParser`; one method
in `CanvasController` if it needs HTTP. `Canvas` doesn't change — it holds and paints pixels
and knows nothing about shapes. Neither shell changes — they only know how to apply a
`Drawing`. That's why `Command.Draw` carries a `Drawing` rather than raw coordinates, and why
`Drawing` is deliberately not sealed.

**"Why is the border rendered, not stored?"**
Storing it means every shape has to remember not to overwrite it and the fill treats it as a
wall by convention. Keeping it out means the canvas contains only what the user drew — which
is what lets the web front end lay out cells without stripping a frame first.

**"Drawing is all-or-nothing?"**
`R 3 3 20 4` on a 10×4 canvas draws *nothing*. Shapes validate their full extent before
painting one cell. A half-drawn rectangle is state a user can't undo and can't explain.

### On testing (they explicitly flagged unit tests)

- `SpecificationExampleTest` is the **acceptance test** — every ambiguous reading of the brief
  was settled by making it pass without special cases. Break it and you've broken a requirement.
- `AtmShellTest.reproducesTheSampleTranscript` drives the real entry point and compares the
  whole transcript line by line. *"The unit tests prove the arithmetic; this proves the product."*
- That's **why `AtmShell` takes a `Reader` and `Writer` instead of reaching for `System.in`** —
  testability was a design input, not an afterthought. Great line, use it.
- Web tests deliberately **don't** re-test business rules — same assertions in a slower harness.
  They assert only what the HTTP layer can get wrong.

---

## 6. Live coding — expect a small TDD kata

They said "write and execute unit tests," so they want to watch your **loop**, not your
cleverness. Work in `smbc-interview-starter`.

**Say your process out loud:**
1. "Let me restate the problem and check the edge cases with you." — *always do this first*
2. Write the **failing test first**, run it, show it red.
3. Simplest code that passes. Run green.
4. *Then* refactor, tests still green.

**Name the edges before you code** — this is what separates candidates:
null, empty, zero, negative, single element, duplicates, overflow, off-by-one.
You already did this in both assessments; say so.

Likely asks: FizzBuzz, string reverse/palindrome, anagram check, two-sum, word frequency,
balanced brackets, a stack with `min()`, or a small bank-account class (they may lean on your
ATM). Any of these you can do — the marks are in the tests and the talking.

**If you get stuck:** say what you're thinking. Silence is the only real failure.
*"I'm choosing between a map and sorting here — map is O(n) but sorting reads clearer; I'll
start with sorting and optimise if we need to."*

---

## 7. Likely core-Java questions (SMBC is a bank — expect fundamentals)

Skim these; you'll be fine:
- `HashMap` internals (buckets, hashCode/equals contract, treeify at 8)
- `ArrayList` vs `LinkedList`; `HashMap` vs `ConcurrentHashMap`
- `String` immutability, string pool, `StringBuilder`
- `equals`/`hashCode` contract — why overriding one demands the other
- Checked vs unchecked exceptions — **you have a real opinion here, from your own code**
- `final`, `static`, interface default methods
- Java 8 streams, `Optional`, lambdas
- Immutability and thread safety — **again, you lived this with `Money` and the lock decision**
- Spring: `@RestController` vs `@Controller`, DI, bean scopes, singleton beans and shared state

Notice how many of these you can answer *from your own code* rather than from a textbook.
Always prefer that: **"In my ATM I hit exactly this —"** is worth more than a definition.

---

## 8. Questions to ask them

- What does the team's stack look like — Spring Boot, and what for persistence?
- Is this closer to greenfield services or modernising existing systems?
- How does the team handle testing — is there a coverage bar, or is it judgement?
- What would my first three months look like?

---

## 9. Two minutes before the call

- [ ] IntelliJ open, all 3 projects indexed, font enlarged
- [ ] Terminal open in each project dir
- [ ] `mvn test` run once in all three (warm)
- [ ] Notifications off, single window shared
- [ ] Webex audio/video tested
- [ ] READMEs open in a tab — your design arguments are *already written down*
- [ ] Water

Your work here is genuinely strong — the READMEs argue design decisions better than most
senior candidates do out loud. The job today is just to **say what you already wrote**.
