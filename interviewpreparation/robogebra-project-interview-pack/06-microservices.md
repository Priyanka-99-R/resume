# 6. Microservices & Architecture — Q&A tied to Robogebra

**Reality:** a **microservices hybrid** — a monolithic learning core (portal) plus independent services (CRM, admin/AI) and several separate frontends. Be honest that it's *pragmatic hybrid*, not textbook pure microservices — that's the senior answer.

---

## Q1. Describe the overall architecture of Robogebra.

**Answer.** Three backend services and multiple frontends, each owning a business domain:

- **robogebra-portal** (Spring Boot 3.2, port 3000) — the monolithic core: all student/teacher learning flows, content, solutions, progress, payments-entry. MongoDB `robogebra_cms_db`.
- **robogebra-admin-portal** (Spring Boot, port 4000) — content authoring + **AI generation** (calls Python ML services). Separated so heavy content pipelines don't affect the student-facing API.
- **robogebra-crm** (Node.js/Express, port 3001) — B2B: institutes, classes, students, coupons, subscriptions. Own DB `robogebra-coupon-mgmt`.
- **Frontends:** `robogebra-web` (Angular) & `robogebra-mobile` (Ionic) for students/teachers; `robogebra-backoffice` (Angular) for ops; `robogebra-crm-web` (React) for CRM admins; `robogebra-website` (static).

**One-line pitch:** *"Robogebra is a modular microservices system for a two-sided market — students and institutions. The portal is our monolithic learning core; the CRM is an independent service for B2B; AI/content is isolated in the admin-portal. Each frontend is deployed independently. It's a pragmatic hybrid that balances startup speed with the ability to scale the busy parts separately."*

---

## Q2. Is this "real" microservices? Monolith or microservices?

**Answer (honest, senior).** It's a **hybrid**: the portal is a **modular monolith** (one deployable, internally split into 60+ domains), while CRM and admin-portal are **separate deployable services with their own databases**. So we get microservice benefits (independent scaling/deploy of CRM & AI, DB-per-service, tech-fit — Node for CRM, Spring for core) without the full operational cost of decomposing the learning core prematurely. I'd only split the portal further when a specific domain needs independent scaling or team ownership — not for its own sake.

---

## Q3. How do the services communicate?

**Answer.** Mostly **synchronous REST** over HTTPS, with JWT/service tokens; cron for time-based workflows.

- **Portal → CRM** (REST/WebClient): coupon validation & redemption, subscription/institute checks. e.g. portal calls CRM's `/v1/coupons/redeem` during payment.
- **Portal → Admin-portal / Python AI**: solution generation, interactive content — REST calls to ML endpoints.
- **Frontends → backends**: JWT bearer token on every request.
- **Service-to-service auth**: portal exposes `@BypassJwtAuth` endpoints for trusted callers (webhooks, CRM) that use a service token instead of a user JWT.
- **Time-based**: Quartz/cron jobs (e.g. nightly subscription activation ~12:05 AM IST).

**Trade-off:** synchronous REST is simple and consistent but couples availability. For non-critical events (analytics, notifications) I'd move to async/queue; critical money paths stay synchronous for immediate consistency.

---

## Q4. How is authentication handled across services? *(also see 07-aws.md)*

**Answer.** **AWS Cognito** (three user pools: `user`, `email-user`, `institute-user`) authenticates identity; the **portal issues/validates JWTs** and enforces permissions. Frontends store the JWT and send it as `Authorization: Bearer`. The portal's `JwtAuthenticationFilter` validates (SSO JWT or Cognito), sets the security context, and puts the resolved user on the request. OAuth2 SSO (Google/LinkedIn) is supported for frictionless onboarding.

Three pools = clean **multi-tenant** isolation: B2C consumers vs email signups vs institute (B2B) admins/teachers each have their own namespace.

---

## Q5. How do you handle data consistency across services?

**Answer.** **Database-per-service** (portal's CMS DB vs CRM's coupon-mgmt DB) avoids accidental coupling but means no cross-service transaction. So: keep each write inside one service's DB (atomic there), use **synchronous REST** for the few cross-service critical actions (coupon redemption during payment), accept **eventual consistency** for analytics, and use idempotent operations + retries. For true cross-service workflows I'd use the **saga** pattern (compensating actions) rather than a distributed transaction.

---

## Q6. How would you scale and make this resilient?

**Answer.**
- **Stateless services** → horizontal scaling behind a load balancer / K8s HPA.
- **Caching** — Caffeine in-process for rarely-changing content (books/chapters/exercises), with property-toggle + TTLs.
- **MongoDB** — indexes for high-cardinality queries; sharding if needed.
- **CDN + S3** for media/assets.
- **Resilience** — timeouts + retries + circuit breakers on outbound calls (portal→CRM, portal→AI); AI failure should degrade gracefully (cached/basic answer), not break the student flow.
- **Observability** — logs, PostHog product analytics (prod).

---

## Q7. Whiteboard the system.

```
                      CLIENTS
  ┌───────────────┬───────────────┬──────────────────────────┐
  │ Web (Angular) │ Mobile (Ionic)│ Backoffice(Angular)/CRM(React)
  └──────┬────────┴──────┬────────┴─────────────┬────────────┘
         │ HTTPS + JWT    │                      │
         ▼                ▼                      ▼
  ┌─────────────────────────────────────┐   ┌──────────────────┐
  │  robogebra-portal (Spring Boot:3000) │   │ robogebra-crm    │
  │  • /auth/*  /api/books /exercises    │◄──┤ (Node/Express:3001)
  │  • /api/exercise-solutions           │REST│ institutes,      │
  │  • /api/solution-visibility          │   │ coupons, subs    │
  │  • payments / webhooks               │   │ DB: coupon-mgmt  │
  │  Auth: Cognito(3 pools)+JWT          │   └──────────────────┘
  │  DB: robogebra_cms_db (MongoDB)      │
  └───────┬───────────────────┬─────────┘
          │                   │ REST
          │                   ▼
          │        ┌───────────────────────────┐
          │        │ admin-portal (Spring:4000) │→ Python AI (solutions)
          │        └───────────────────────────┘
          ▼
   AWS: Cognito · S3 · SES · SNS · Lambda      External: Razorpay, Firebase, WhatsApp, LiveKit
```

---

## Q8. Example end-to-end flow (student pays with a coupon).

**Answer.** Frontend opens Razorpay → gets `payment_id` → `POST /api/payment` to portal → portal verifies with Razorpay → **portal calls CRM `/v1/coupons/redeem`** to validate/redeem the coupon → portal writes `Payment` + `Subscription` → a nightly cron activates the subscription. This is the concrete "services talk over REST, each owns its data" story.

---

### Rapid-fire recap

| Topic | Robogebra reality |
|-------|-------------------|
| Style | Microservices **hybrid** (modular-monolith core + separate CRM/AI) |
| Services | portal (Spring:3000), admin-portal (Spring:4000), CRM (Node:3001) |
| Comms | synchronous REST + JWT/service token; cron for scheduled |
| Auth | Cognito 3 pools + portal-issued JWT + OAuth2 |
| Data | DB-per-service; eventual consistency + REST for critical paths |
| Scale | stateless + Caffeine cache + Mongo indexes + CDN + circuit breakers |
