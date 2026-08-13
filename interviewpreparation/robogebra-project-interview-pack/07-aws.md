# 7. AWS & Cloud Services — Interview Q&A tied to Robogebra

**Services used:** Cognito (auth), S3 (assets), SES (email), SNS (push), Lambda (serverless content), plus Firebase for real-time/push. Region: `ap-south-1` (Mumbai).

---

## Q1. How does authentication use AWS Cognito? Why three user pools?

**Answer.** **Cognito** is a managed identity provider — it handles signup, OTP/MFA, password policies, and token issuance, so we don't build/secure that ourselves. We run **three separate user pools** for clean multi-tenant isolation:

| Pool | Segment | Why separate |
|------|---------|--------------|
| `user` | B2C individual students/teachers | consumer signups |
| `email-user` | email-based signups | alternate login flow |
| `institute-user` | B2B institute admins/teachers | school/coaching namespace, different policies |

**Flow:** frontend → portal `/auth/*` → portal validates credentials against the right Cognito pool → portal issues its **own JWT** (custom claims: userId, role, permissions) → frontend stores it → subsequent requests carry `Authorization: Bearer` → portal's `JwtAuthenticationFilter` validates (Cognito or our SSO JWT). OAuth2 SSO (Google/LinkedIn) is wired via Spring Security for one-click onboarding.

**Why separate pools instead of one with groups?** Different segments have different password/MFA policies, signup flows, and blast-radius isolation — a config change or breach in one pool can't affect another. That's the senior justification.

---

## Q2. Where does S3 fit, and how do you serve files securely?

**Answer.** **S3** stores user-generated assets and published textbook content (buckets `robogebra-dev-assets`, `robogebra-core-content`). We never make objects public — the backend generates **pre-signed URLs** with a short expiry (5 min) so the client can fetch/upload directly to S3 without the file passing through our servers. `dev`/`prod` path prefixes separate environments; a CDN fronts assets for delivery.

**Classic interview design (upload flow):** client asks portal for a **pre-signed PUT URL** → uploads the file straight to S3 → stores only the **object key/metadata** in MongoDB → later reads use a pre-signed **GET** URL. Keeps large binaries out of the app tier and out of the DB.

---

## Q3. What other AWS services do you use and why?

**Answer.**
- **SES** — transactional email (OTP, payment receipts, notifications), sender `noreply@robogebra.ai`.
- **SNS** — push notifications / alerts.
- **Lambda** — serverless functions for content processing (interactive answer/graph generation, semantic search, coupon validation) — event-driven, scales to zero, isolates spiky ML-ish work from the main API.
- **Firebase** — real-time data + cloud messaging (complements SNS for device push).

The theme: **offload cross-cutting/spiky work to managed services** so the portal stays focused and responsive.

---

## Q4. Tie it together: the signup + auth journey on AWS.

**Answer.**
1. User submits signup (email/phone) → portal `/auth/create-user`.
2. Portal calls **Cognito** (correct pool) to create the user + trigger **OTP** (delivered via SES email / WhatsApp).
3. Duplicate? Portal's find-or-create throws `APIException(400, "An account with this email already exists.")` (see `02-backend.md`/`04-spring.md`), unless it's a valid **link-Cognito-to-existing-SSO-user** case.
4. User verifies OTP → Cognito confirms → portal issues its **JWT**.
5. Every later request: `Authorization: Bearer <JWT>` → `JwtAuthenticationFilter` validates (Cognito SDK or SSO issuer check) → sets security context + request attributes → controller reads the user via `@RequestAttribute`.
6. Cognito SDK errors are mapped to HTTP statuses (`NotAuthorizedException → 401`, `TooManyRequestsException → 400`) so failures are typed, not 500s.

---

## Q5. Config & secrets across environments?

**Answer.** Config lives in `application.yml` with **environment-variable placeholders + defaults** (`${VAR:default}`); real secrets (Cognito client secrets, JWT secret, AWS keys, OAuth client secrets) come from the environment, never committed. Region pinned to `ap-south-1`. This keeps the same artifact deployable across dev/prod with different env values.

```yaml
aws:
  cognito:
    user:            { region: ap-south-1, userPoolId: ..., clientId: ..., clientSecretId: ... }
    institute-user:  { region: ap-south-1, userPoolId: ..., clientId: ..., clientSecretId: ... }
auth:
  jwt: { secret: ${AUTH_JWT_SECRET:...}, expiry: 86400000, issuer: ${ISSUER:robogebra-auth} }
```

---

## Q6. If asked about scaling/resilience on AWS.

**Answer.** Stateless services scale horizontally (containers/K8s or ECS); S3 + CloudFront for static/media; Lambda auto-scales for bursty content jobs; Cognito & SES are managed (no ops burden). Outbound calls (to CRM/AI) get timeouts + retries + circuit breakers; a failing AI/Lambda path degrades gracefully rather than breaking the student flow. Multi-AZ Mongo + indexes for data-tier resilience.

---

### Rapid-fire recap

| Service | Use in Robogebra |
|---------|------------------|
| **Cognito** | Auth — 3 pools (user / email-user / institute-user) + OAuth2 SSO |
| **S3** | Assets & textbook content; pre-signed URLs; dev/prod prefixes |
| **SES** | Transactional email (OTP, receipts) |
| **SNS** | Push notifications |
| **Lambda** | Serverless content processing (interactive answer/graph, search, coupon) |
| **Firebase** | Real-time + device push |
| Secrets | `${ENV:default}` in `application.yml`, region `ap-south-1` |
