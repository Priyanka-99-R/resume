# RoboGebra — Technical Stack & Versions (from the actual codebase)

Every number below was read from the real repos in `/Users/safi/workspace/robogebra-workspace/` — `package.json`, `pom.xml`, `angular.json`, `tsconfig.json`, `capacitor.config.ts`, `variables.gradle`, `application.yml`, `Dockerfile`. **Nothing here is guessed.** Audit date: **31 July 2026**.

> **Why this file matters:** "Which version of Angular/Java/Spring Boot are you using?" is asked in almost every screening call. Vague answers ("latest", "Angular 17 or 18 I think") kill credibility instantly. Precise answers with a *reason* land you as someone who actually ships.

---

## Table of contents

- [⚡ The 60-second cheat sheet](#-the-60-second-cheat-sheet)
- [System architecture — 8 repos](#system-architecture--8-repos)
- [1. robogebra-web (frontend)](#1-robogebra-web--frontend--student-app)
- [2. robogebra-mobile (Ionic)](#2-robogebra-mobile--ionic-hybrid-app)
- [3. robogebra-backoffice (admin)](#3-robogebra-backoffice--admin-ui)
- [4. robogebra-portal (backend)](#4-robogebra-portal--main-java-backend)
- [5. robogebra-admin-portal (admin backend)](#5-robogebra-admin-portal--admin-java-backend)
- [6. robogebra-crm (management)](#6-robogebra-crm--management-backend-nodejs)
- [7. robogebra-crm-web (management UI)](#7-robogebra-crm-web--management-ui-react)
- [8. robogebra-website (marketing)](#8-robogebra-website--marketing-site)
- [Master version table](#master-version-table)
- [🚨 Traps in your own stack](#-traps-in-your-own-stack--read-this-twice)
- [Interview answer scripts](#interview-answer-scripts)
- [Numbers worth quoting](#numbers-worth-quoting)

---

## ⚡ The 60-second cheat sheet

Memorize this block. It answers 90% of stack questions.

| Question | Your answer |
|---|---|
| **Java version?** | **Java 17** (LTS) — `<java.version>17</java.version>`, runtime is Amazon Corretto 17.0.18 |
| **Spring Boot version?** | **3.2.0** — so Spring Framework 6.1 and Spring Security 6.2 |
| **Build tool?** | **Maven** (wrapper `mvnw`), Maven 3.8.1 |
| **Database?** | **MongoDB** — Spring Data MongoDB, `MongoRepository`, no SQL/Hibernate |
| **Angular version (web + backoffice)?** | **Angular 16.2** |
| **Angular version (mobile)?** | **Angular 18.2** — the mobile app is on a newer line |
| **Ionic version?** | **Ionic (Angular) 8.7.5** with **Capacitor 6.2.1** |
| **TypeScript?** | **5.1.3** web · **4.9.5** backoffice · **5.4.5** mobile |
| **RxJS?** | **7.8** everywhere |
| **State management?** | **RxJS services with `BehaviorSubject`** — no NgRx in RoboGebra |
| **Standalone components?** | **No — NgModule-based** across all three Angular apps |
| **API docs?** | **springdoc-openapi 2.2.0** (Swagger UI at `/swagger-ui.html`) |
| **Auth?** | **JWT** + **OAuth2 social login** (Google, LinkedIn) + AWS Cognito |
| **Deployment?** | **Docker** (`eclipse-temurin:17-jre`) + **Jenkins** CI/CD pipelines |
| **Cloud?** | **AWS** — S3, Cognito, Lambda, SES, SNS |

---

## System architecture — 8 repos

```
                     ┌──────────────────────────────────────────┐
   STUDENT           │  robogebra-web        Angular 16.2       │  :4200
                     │  robogebra-mobile     Ionic 8 + Ang 18   │  :8100 → iOS/Android
                     └────────────────┬─────────────────────────┘
                                      │  REST + JWT
                     ┌────────────────▼─────────────────────────┐
   CORE BACKEND      │  robogebra-portal                        │  :3000
                     │  Spring Boot 3.2.0 · Java 17 · MongoDB   │
                     └───┬──────────┬──────────┬────────────┬───┘
                         │          │          │            │
                  ┌──────▼───┐ ┌────▼────┐ ┌───▼─────┐ ┌────▼────────┐
                  │ MongoDB  │ │  AWS    │ │ Python  │ │ robogebra-  │
                  │          │ │ S3 /    │ │ AI      │ │ crm  :8085  │
                  │          │ │ Cognito │ │ service │ │ Node/Express│
                  │          │ │ Lambda  │ │(external)│ │ + Mongoose  │
                  └──────────┘ │ SES/SNS │ └─────────┘ └────▲────────┘
                               └─────────┘                  │
                     ┌──────────────────────────────────────┴───┐
   INTERNAL UIs      │  robogebra-backoffice  Angular 16.2      │  content admin
                     │  robogebra-crm-web     React 18 + Vite   │  CRM/management
                     │  robogebra-admin-portal Spring Boot :4000│  admin APIs
                     │  robogebra-website     Parcel (static)   │  marketing
                     └──────────────────────────────────────────┘
```

**Wiring you can quote** (from the environment files):
- `robogebra-web` / `robogebra-mobile` → `API_URL` = portal on **:3000**
- `robogebra-backoffice` → `API_URL` :3000 **and** `ADMIN_API_URL` :4000 (talks to *both* backends)
- `robogebra-portal` → CRM on :8085 (coupons), Node exercise service :8082, Python AI assistant, voice-token server, LiveKit

---

## 1. robogebra-web — frontend / student app

**Path:** `robogebra-workspace/robogebra-web` · **573 TS files, 182 templates, 8 NgModules**

### Core framework

| Package | Version |
|---|---|
| `@angular/core` (and common/forms/router/animations/compiler) | **^16.2.0** |
| `@angular/cli` | ~16.2.1 |
| `@angular-devkit/build-angular` | ^16.2.1 |
| `@angular/cdk`, `@angular/material` | ^16.2.0 |
| **TypeScript** | **~5.1.3** |
| **RxJS** | ~7.8.0 |
| **zone.js** | ~0.13.0 |

### Build configuration (`angular.json` / `tsconfig.json`)

- Builder: `@angular-devkit/build-angular:browser` *(the classic browser builder, not esbuild/`application`)*
- Compile target & module: **ES2022**
- `strict: true`, but `strictNullChecks: false` and `strictPropertyInitialization: false`
- Initial bundle budget: **8 MB** warning/error; component style budget 100 KB
- Build configurations: `production`, `dev`, `development`

### UI libraries (three design systems coexist)

| Library | Version | Used for |
|---|---|---|
| VMware **Clarity** — `@clr/angular`, `@clr/ui` | ^15.14.6 | Core layout/components |
| Clarity Core — `@cds/angular`, `@cds/core` | ^6.9.2 | Web components |
| **PrimeNG** + primeicons | ^16.1.0 / 7.0.0 | Rich widgets |
| **Angular Material** | ^16.2.0 | Selected components |
| `@angular-slider/ngx-slider`, `ng-circle-progress`, `ngx-spinner` 16.0.2, `angular-svg-icon` 16.1.0 | | |

### Domain-specific libraries (the interesting ones)

| Package | Version | Why it's there |
|---|---|---|
| **mathlive** | ^0.101.0 | Interactive math input/editing |
| **katex** | ^0.16.11 | LaTeX formula rendering |
| **svg-pan-zoom** | ^3.6.1 | Interactive graph pan/zoom |
| **chart.js** | ^4.5.1 | Progress analytics on the dashboard |
| **livekit-client** | ^2.13.3 | Real-time voice AI assistant (WebRTC) |
| **wavesurfer.js** | ^7.9.5 | Audio waveform playback |
| `@gumlet/player.js` | ^3.0.3 | Video player integration |
| **firebase** / `@angular/fire` | ^10.1.0 / ^7.0.0 | Auth + messaging |
| **razorpay** | ^2.9.4 | Payments |
| `@ngx-translate/core` / `http-loader` | ^15.0.0 / ^8.0.0 | **Tamil/English i18n** |
| `ngx-quill` | 22.1.0 | Rich text |
| **posthog-js** | 1.268.8 | Product analytics |
| `dompurify` | ^3.2.6 | XSS sanitization of AI-generated HTML |
| `intl-tel-input` 18.5.3, `ng-otp-input` 1.9.3 | | Phone/OTP login |
| `moment` 2.30.1, `jquery` 3.7.1, `shortid` | | |

### Testing
Jasmine ~4.6 · Karma ~6.4 · karma-coverage 2.2

### Architecture
`src/app/` → `core/` (services, guards, interceptors, resolvers, models, `AppEndPoints.ts`, `AppRoutes.ts`) · `features/` (pages, robo-chat) · `shared/` · `components/`
Interceptors: `header-authorization.interceptor`, `anonymous-lesson.interceptor`
Guards: `auth-guard`, `login-guard`, `public-preview-guard`

---

## 2. robogebra-mobile — Ionic hybrid app

**Path:** `robogebra-workspace/robogebra-mobile` · **502 TS files, 5 NgModules**

> ⚠️ **This app is on Angular 18, not 16.** Know that and know why you'd say it.

### Core framework

| Package | Version |
|---|---|
| **`@ionic/angular`** | **8.7.5** |
| `@ionic/angular-toolkit` | ^12.1.1 |
| **`@angular/core`** and friends | **^18.2.0** |
| `@angular/cli` / `build-angular` | ^18.2.0 |
| `@angular/material`, `@angular/cdk` | ^18.2.0 |
| **TypeScript** | **~5.4.5** |
| **RxJS** | ~7.8.0 |
| **zone.js** | ~0.14.10 |

### Capacitor (native bridge)

| Package | Version |
|---|---|
| **`@capacitor/core`** | **6.2.1** |
| `@capacitor/android`, `@capacitor/ios` | 6.2.1 |
| `@capacitor/cli` | 6.2.0 |

**Plugins in use:** `app` 6.0.1 · `browser` 6.0.3 · `device` 6.0.2 · `haptics` 6.0.1 · `keyboard` 6.0.3 · `network` 6.0.3 · `preferences` 6.0.3 · `push-notifications` 6.0.4 · `screen-orientation` 6.0.3 · `splash-screen` 6.0.4 · `status-bar` 6.0.1 · `@capacitor-firebase/messaging` 6.3.1 · `@capacitor-community/firebase-analytics` 6.0.0 · `@capawesome/capacitor-app-update` 6.1.0 · `capacitor-native-settings` 6.0.1 · `capacitor-razorpay`

### `capacitor.config.ts`

```ts
appId:   'io.robogebra.one'   // Android   ('io.robogebra.app' for iOS)
appName: 'Robogebra'
webDir:  'www'
server:  { androidScheme: 'https' }
plugins: PushNotifications (badge/sound/alert, ic_notification, #E83E6C), SplashScreen
```

### Android build

| Setting | Value |
|---|---|
| **minSdkVersion** | **22** (Android 5.1 Lollipop) |
| **compileSdkVersion / targetSdkVersion** | **36** |
| **Gradle** | **8.2.1** |
| versionCode / versionName | 34 / "1.0" |

### Mobile-specific libraries
firebase **11.1.0** + `@angular/fire` 18.0 · **mathlive 0.103.0** (newer than web) · katex 0.16.11 · quill 2.0.3 / ngx-quill 26.0.10 · **swiper 11.1.15** · posthog-js 1.268.8 · `@ngx-translate/core` 15.0.0 · `angular-svg-icon` 18.0.1 · dompurify 3.2.6

### Tooling (only app with linting configured)
**ESLint 9.17** · `@angular-eslint/*` 18.4.2 · `typescript-eslint` 8.18.2 · eslint-plugin-import / jsdoc / prefer-arrow · Jasmine 5.5 · Karma 6.4.4

### Dev server
`ng serve --port 8100` (Ionic convention)

---

## 3. robogebra-backoffice — admin UI

**Path:** `robogebra-workspace/robogebra-backoffice` · **497 TS files, 5 NgModules**

### Core framework

| Package | Version |
|---|---|
| `@angular/core` | **^16.2.12** |
| `@angular/cli` | ~16.2.16 |
| **TypeScript** | **~4.9.5** ← *oldest TS in the workspace* |
| RxJS | ~7.8.2 |
| zone.js | ~0.13.3 |
| Target / module | ES2022 / es2020, `strict: true` |

### UI stack (heaviest of the three Angular apps)

| Library | Version |
|---|---|
| Clarity `@clr/angular`, `@clr/ui` | ~15.14.8 |
| `@cds/angular`, `@cds/core`, `@cds/city` | ^6.4.4 / 1.1.0 |
| `@porscheinformatik/clr-addons` | ^15.13.0 |
| **PrimeNG** | **16.9.1** |
| Angular Material / CDK | ^16.2.0 |
| `@swimlane/ngx-datatable` | ^20.1.0 |
| `ngx-bootstrap` / `@ng-bootstrap/ng-bootstrap` | ^11.0.2 / ^15.1.2 |
| `@ng-select/ng-select` | ^11.2.0 |
| FontAwesome (angular 0.10, free 5.15) | |

### Content-authoring libraries (this is a CMS)

| Package | Version | Purpose |
|---|---|---|
| **codemirror** | ^5.65.18 | In-browser code editing |
| **jsoneditor** / `ang-jsoneditor` | ^9.1.8 / ^3.1.1 | JSON content editing |
| `@kolkov/angular-editor` | 3.0.0-beta.2 | WYSIWYG |
| `ngx-quill` / `quill` | 23.0.3 / ^1.3.7 | Rich text |
| `ngx-highlightjs` / `prismjs` / prism-themes | ^4.1.3 / ^1.29.0 | Syntax highlighting |
| **mathlive** / **katex** | ^0.101.0 / ^0.16.11 | Math authoring |
| `@svgdotjs/svg.js` | ^3.1.2 | SVG manipulation |
| fullcalendar | 5.10.2 | Scheduling |
| `ngx-file-drop` 16.0.0, `ngx-filesize`, `file-saver` 2.0.5 | | Uploads |
| `ngx-color-picker` 13.0.0, `ngx-chips` 3.0.0, `ngx-toastr` 15.2.2 | | |
| `@ngx-translate/core` | **^14.0.0** ← older than web/mobile (15.0.0) | |
| lodash 4.17.21, moment 2.29.1, `short-uuid`, `http-status-codes` | | |

### Guards worth mentioning
Six `CanDeactivate` guards protecting unsaved work in the generators (`solution-generator`, `solution-pipeline`, `solution-regenerator`, `solution-source`, `toc-generator`, `preview`) — a good concrete answer to *"have you used route guards?"*

### Dev setup
`proxy.config.json` for local API proxying · Karma/Jasmine 4.x

---

## 4. robogebra-portal — main Java backend

**Path:** `robogebra-workspace/robogebra-portal` · **1,472 Java files across 70 domain packages**

### Platform

| Item | Version |
|---|---|
| **Spring Boot** (`spring-boot-starter-parent`) | **3.2.0** |
| ⤷ implies Spring Framework | 6.1.x |
| ⤷ implies Spring Security | 6.2.x |
| **Java** | **17** (`<java.version>17</java.version>`) |
| Local JDK | Amazon Corretto **17.0.18** LTS |
| Build | **Maven** (`mvnw` wrapper), Maven 3.8.1 |
| GroupId / Artifact | `com.robogebra.cms` / `robogebra` 0.0.1-SNAPSHOT |
| Server port | **3000** |

### Spring starters in use

`spring-boot-starter-web` · `-security` · `-validation` · `-webflux` (for `WebClient`) · `-data-mongodb` · `-mail` · `-thymeleaf` · `-cache` · `-aop` · `-oauth2-client` · `-test`

### Key dependencies

| Dependency | Version | Purpose |
|---|---|---|
| **springdoc-openapi-starter-webmvc-ui** | **2.2.0** | Swagger UI `/swagger-ui.html`, docs `/api-docs` |
| **Lombok** | Boot-managed | Boilerplate reduction (`@AllArgsConstructor` etc.) |
| **AWS SDK v1** — `aws-java-sdk-s3`, `aws-java-sdk-cognitoidp` | 1.12.523 | S3 storage, Cognito |
| **AWS SDK v2** — `cognitoidentityprovider`, `ses`, `sns` | 2.20.109 | Email, SMS, auth |
| **AWS SDK v2** — `lambda` | 2.20.0 | Invokes `interactive-answer-dev`, `interactive-graph-dev`, `semantic_search`, `validate-coupon` |
| **firebase-admin** | 9.3.0 | Push notifications |
| **Quartz Scheduler** | 2.3.2 | Background jobs |
| **Caffeine** | 3.2.2 | In-memory cache behind `@Cacheable` |
| **java-jwt** (Auth0) / **jwks-rsa** | 4.4.0 / 0.22.1 | JWT verification |
| **jjwt** (api/impl/jackson) | 0.11.5 | JWT issuing |
| **razorpay-java** | 1.4.3 | Payments |
| **iText** kernel / html2pdf | 7.2.1 / 4.0.1 | PDF generation |
| **Apache POI** `poi-ooxml` | 5.2.5 | Excel export |
| **opencsv** | 5.9 *(and a stale 4.1 entry)* | CSV import |
| Jakarta servlet-api / annotation-api / validation-api | 5.0.0 / 2.1.0 / 3.0.2 | |
| **Testcontainers** `junit-jupiter` | 1.17.6 | Integration tests |
| `spring-security-test` | Boot-managed | |
| `netty-resolver-dns-native-macos` | 4.1.82.Final | Apple-Silicon dev workaround |

### Data layer
**MongoDB** — `spring-boot-starter-data-mongodb`, URI `mongodb://localhost:27017/robogebra_cms_db`, `auto-index-creation: true`.
Repositories extend **`MongoRepository<T, String>`** with derived queries (`findByQuizDefinition_IdOrderByOrderAsc`) and `@Aggregation` pipelines for join-style reads that avoid N+1 DBRef loads.

### Security architecture (strong talking point)
`SecurityConfig` is **Spring Security 6 style** — a `SecurityFilterChain` `@Bean`, no deprecated `WebSecurityConfigurerAdapter`. It composes **~15 custom filters** in ordered groups:

`LoggingFilter` → `FilterChainExceptionHandler` → `PreferredLanguageFilter` → `AnonymousContentAuthenticationFilter` → `CrmServiceAuthenticationFilter` → `InternalServiceAuthenticationFilter` → `WebhookAuthorizationFilter` → `JwtAuthenticationFilter` → `MultiLoginDetectionFilter` → `BookAccessRestrictionFilter` → `FeatureUsageLimitFilter`

Plus `JwtAuthenticationEntryPoint`, `CustomUserDetailsService`, `PermissionInterceptor`, and OAuth2 handlers (`SocialAuthSuccessHandler` / `FailureHandler`, `HttpCookieOAuth2AuthorizationRequestRepository`, a custom `LinkedInAuthorizationRequestResolver`).

**Auth methods:** JWT (24 h expiry, `max-user-login: 3`) · **Google + LinkedIn OAuth2/OIDC** · AWS Cognito.

> 💬 This is your best backend answer. *"The filter chain is Chain of Responsibility — each filter has one concern and passes the request on. That's why adding LinkedIn SSO didn't touch JWT auth at all."* Ties straight into [17 — SOLID & Design Patterns](./17-solid-design-patterns.md).

### `@Configuration` classes (44 total)
`SecurityConfig` · `CacheConfig` · `CorsConfig` · `AwsConfig` · `CognitoConfig` · `FireBaseConfig` · `QuartzConfig` · `MailConfig` · `OpenApiConfig` · `WebClientConfig` · `WebMvcConfig` · `AuditingConfig` · `RazorPaymentConfig` · `EventTrackingConfig`

### Package layout (domain-driven)
```
com.robogebra.cms
├── config/          44 @Configuration classes
├── security/        ~22 filters, handlers, UserDetailsService
├── common/          util, model, service, exception, annotation, properties, importer
└── domain/          70 feature packages, each:
    quiz/ ├── controller/   @RestController
          ├── service/      @Service (business logic)
          ├── repository/   MongoRepository + @Aggregation
          └── model/        entities + DTOs
```
Sample domains: `quiz`, `selfquiz`, `sectionquiz`, `robochat`, `voiceaiassistant`, `exercisesolution`, `exercisesolutionworkout`, `solutionpipeline`, `learningtimeline`, `parentanalytics`, `studymaterials`, `subscription`, `payment`, `institute`, `institutionlearning`, `usagequota`, `notification`, `webhook`, `refreshtoken`, `otp`, `favourites`, `learningasset`

### Deployment
```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/robogebra-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]
```
CI/CD: **Jenkins** — `jenkins/Jenkinsfile-CI` and `jenkins/Jenkinsfile-CD`. Profiles: `application.yml`, `application-dev.yml`, `application-prod.yml`. Logging via `logback-spring.xml`.

### External services it calls
Python AI assistant (`/api/ai_assistant/generate/text|image|video`, `/api/semantic-search/execute`, `/api/robochat/chat`) · voice-token server (LiveKit) · CRM (coupon validation/redeem) · Node exercise service :8082 · Point2Space Studio

---

## 5. robogebra-admin-portal — admin Java backend

**Path:** `robogebra-workspace/robogebra-admin-portal` · **218 Java files** · Port **4000**

Same platform as the portal, slimmer dependency set:

| Item | Version |
|---|---|
| Spring Boot | **3.2.0** |
| Java | **17** |
| Artifact | `robogebra-admin` 0.0.1-SNAPSHOT |

**Starters:** web, security, validation, webflux, data-mongodb, mail, thymeleaf, test
**Extras:** springdoc-openapi 2.2.0 · Lombok · AWS SDK v1 (S3, Cognito) 1.12.523 · razorpay-java 1.4.3 · opencsv 4.1 · Testcontainers 1.17.6 · Docker

**How to describe it:** *"A separate Spring Boot service for admin-only operations, so admin traffic and student traffic scale and deploy independently — the backoffice UI points at both `:3000` and `:4000`."*

---

## 6. robogebra-crm — management backend (Node.js)

**Path:** `robogebra-workspace/robogebra-crm` · **166 JS files** · Port **8085**

> Not Java. Be upfront: *"The CRM is a Node/Express service — I've worked across it, but my primary backend work is the Java/Spring Boot portal."*

| Item | Version |
|---|---|
| Node engine | `>=12.0.0` (local machine runs v22.12.0) |
| **Express** | ^4.21.2 |
| **Mongoose** (MongoDB ODM) | ^8.8.0 |
| **passport** / **passport-jwt** | ^0.7.0 / ^4.0.1 |
| jsonwebtoken / bcryptjs | ^9.0.2 / ^2.4.3 |
| **Joi** (validation) | ^17.13.3 |
| helmet / cors / compression | ^8.1.0 / ^2.8.5 / ^1.8.1 |
| express-rate-limit / express-mongo-sanitize / xss-clean | ^5.5.1 / ^2.2.0 / ^0.1.4 |
| winston / morgan | ^3.17.0 / ^1.10.1 |
| multer / nodemailer / axios | ^2.0.2 / ^6.10.1 / ^1.11.0 |
| swagger-jsdoc / swagger-ui-express | ^6.2.8 / ^4.6.3 |
| **PM2** (process manager) | ^5.4.3 |
| **Jest** / supertest / mongodb-memory-server | ^30.1.3 / ^7.1.4 / ^10.2.0 |
| ESLint (airbnb-base) / Prettier / Husky | ^7.32.0 / ^2.8.8 / ^9.1.7 |

**Structure:** `src/{routes, controllers, services, models, middlewares, validations, config, utils, seeders}` — a clean layered API, same controller→service→model separation as the Spring app.
**Deployment:** Docker + `docker-compose.{dev,test,prod}.yml`, PM2 via `ecosystem.config.json`. Live at `testcrm.api.robogebra.ai`.
**Domain:** customers, affiliates, orders, coupons (the portal calls it for coupon validation/redemption).

---

## 7. robogebra-crm-web — management UI (React)

**Path:** `robogebra-workspace/robogebra-crm-web` · **129 TS/TSX files**

> Not Angular. Your resume already lists React/Formik, so this is consistent — but be clear it's a **React + Vite** app, not Angular.

| Item | Version |
|---|---|
| **React** / react-dom | ^18.3.1 |
| **Vite** | ^5.4.19 |
| `@vitejs/plugin-react-swc` | ^3.11.0 |
| **TypeScript** | ^5.8.3 ← *newest TS in the workspace* |
| **Tailwind CSS** | ^3.4.17 (+ `@tailwindcss/typography`, `tailwindcss-animate`) |
| **shadcn/ui on Radix UI** | ~28 `@radix-ui/react-*` primitives, v1.x–2.x |
| **TanStack React Query** | ^5.83.0 (+ devtools 5.87.4) |
| **react-router-dom** | ^6.30.1 |
| **react-hook-form** + **zod** + `@hookform/resolvers` | ^7.61.1 / ^3.25.76 / ^3.10.1 |
| **recharts** | ^2.15.4 |
| lucide-react / sonner / cmdk / vaul / embla-carousel | icons, toasts, command palette, drawer, carousel |
| date-fns / axios / next-themes | ^3.6.0 / ^1.11.0 / ^0.3.0 |
| ESLint / typescript-eslint | ^9.32.0 / ^8.38.0 |

**Note:** the package name is `vite_react_shadcn_ts` and `lovable-tagger` is a devDependency — the project was scaffolded with Lovable. Don't claim you hand-built the scaffold; talk about the features you built inside it.

---

## 8. robogebra-website — marketing site

**Path:** `robogebra-workspace/robogebra-website`

| Item | Version |
|---|---|
| **Parcel** bundler | ^2.13.3 |
| svgo | ^3.3.2 |

Static multi-page HTML: `index`, `about-us`, `leadership`, `team`, `contact-us`, `terms`, `policy`. No framework.

---

## Master version table

| Technology | Version | Where |
|---|---|---|
| **Java** | **17** (Corretto 17.0.18) | portal, admin-portal |
| **Spring Boot** | **3.2.0** | portal, admin-portal |
| Spring Framework | 6.1.x (via Boot) | portal, admin-portal |
| Spring Security | 6.2.x (via Boot) | portal, admin-portal |
| Spring Data MongoDB | via Boot 3.2.0 | portal, admin-portal |
| Maven | 3.8.1 + `mvnw` wrapper | portal, admin-portal |
| springdoc-openapi | 2.2.0 | portal, admin-portal |
| **MongoDB** | server local 27017; Mongoose 8.8 on CRM | all backends |
| **Angular** | **16.2** | web, backoffice |
| **Angular** | **18.2** | mobile |
| **Ionic Angular** | **8.7.5** | mobile |
| **Capacitor** | **6.2.1** | mobile |
| **TypeScript** | 5.1.3 / 4.9.5 / 5.4.5 / 5.8.3 | web / backoffice / mobile / crm-web |
| RxJS | 7.8 | all Angular apps |
| zone.js | 0.13 / 0.14 | Ang 16 apps / mobile |
| Angular Material + CDK | 16.2 / 18.2 | web+backoffice / mobile |
| Clarity (`@clr`) | 15.14 | web, backoffice |
| PrimeNG | 16.1 / 16.9.1 | web / backoffice |
| ngx-translate | 15.0 / 14.0 | web+mobile / backoffice |
| KaTeX | 0.16.11 | web, mobile, backoffice |
| MathLive | 0.101.0 / 0.103.0 | web+backoffice / mobile |
| Firebase JS SDK | 10.1 / 11.1 | web / mobile |
| firebase-admin (Java) | 9.3.0 | portal |
| **React** | 18.3.1 | crm-web |
| **Vite** | 5.4.19 | crm-web |
| Tailwind CSS | 3.4.17 | crm-web |
| TanStack Query | 5.83.0 | crm-web |
| **Node.js** | engine `>=12`, local v22.12.0, npm 11.6.0 | crm |
| Express | 4.21.2 | crm |
| Jest | 30.1.3 | crm |
| Jasmine / Karma | 4.6+5.5 / 6.4 | Angular apps |
| Gradle | 8.2.1 | mobile/android |
| Android SDK | min 22, compile/target 36 | mobile |
| Docker base image | `eclipse-temurin:17-jre` | portal |
| CI/CD | Jenkins (CI + CD pipelines) | portal, web |
| AWS SDK | v1 1.12.523 · v2 2.20.109 | portal |
| Quartz | 2.3.2 | portal |
| Caffeine | 3.2.2 | portal |
| Testcontainers | 1.17.6 | portal, admin-portal |
| Parcel | 2.13.3 | website |

---

## 🚨 Traps in your own stack — read this twice

These are the follow-ups that catch people out. Each one has a *correct, honest* answer.

### 1. "You said Angular 16 — but your mobile app is 18?"
**Yes, and say why:** *"The web and backoffice are on 16.2; the Ionic mobile app was built later on Angular 18.2 with Ionic 8 and Capacitor 6. They're separate repos with separate release cycles, so we upgraded the newer app first rather than doing a big-bang upgrade of everything."* That's a strong answer — it shows you understand incremental migration.

### 2. "Angular 16+ — so you use standalone components?"
**No, and you must not bluff this.** All three Angular apps are **NgModule-based** — I counted **zero** components with `standalone: true`. Web has 8 NgModules, mobile 5, backoffice 5.
**Say:** *"Standalone components have been available since 14 and are the default from 17, but our apps are all NgModule-based — they predate the shift and there was no business case to migrate. I understand the model: standalone components declare their own `imports`, you bootstrap with `bootstrapApplication`, and routes can lazy-load with `loadComponent` instead of `loadChildren`."* Then read [04-angular.md](./04-angular.md) on standalone before the call.

### 3. "How do you manage state — NgRx?"
**RoboGebra has no NgRx and no Angular Formly.** Your resume lists both — they're from **EasyVisa**, and you must attribute them correctly.
**Say:** *"In RoboGebra we use RxJS-based service state — shared services with `BehaviorSubject`, `async` pipe in templates, and `takeUntil` for unsubscribe. I used NgRx and Angular Formly on EasyVisa, where the dynamic nested forms justified them."*

### 4. "Spring Boot 3 — what changed from Spring Boot 2?"
Have this ready, it's the #1 Boot 3 question:
- **`javax.*` → `jakarta.*`** (Jakarta EE 9+) — the single biggest migration cost
- **Java 17 minimum** (Boot 2 supported Java 8)
- **Spring Security 6** — `WebSecurityConfigurerAdapter` removed; you configure a **`SecurityFilterChain` bean** (which is exactly what your `SecurityConfig` does)
- Native image support via **GraalVM AOT**
- **Micrometer** observability/tracing built in
- springdoc **2.x** required (1.x doesn't work with Boot 3)
- Trailing-slash matching disabled by default

### 5. "You listed MySQL/Postgres — but what does RoboGebra use?"
**MongoDB only.** No SQL database, no Hibernate, no JPA entities in this codebase.
**Say:** *"RoboGebra is MongoDB with Spring Data MongoDB — `MongoRepository`, derived query methods and `@Aggregation` pipelines. I used Postgres and Neo4j on EasyVisa."*

> ⚠️ There *is* a `spring-data-jpa` dependency in the POM, but there's no JPA starter, no datasource, and no `@Entity` classes — it's a leftover. **Don't say "we use JPA."** If asked, "there's an unused JPA dependency on the classpath from an earlier direction" is a fine, honest answer.

### 6. "Java 17 — which Java 17 features do you actually use?"
Be precise; the codebase is conservative:
- **`record`** — 4 files use it
- **`switch` expressions** — ~20 files
- **Sealed classes** — none
- **Text blocks** — none
- Heavy use of **Streams** (198 files) and **`Optional`** (92 files) — Java 8 features
**Say:** *"We're on Java 17 as the platform, but the codebase leans on Java 8 idioms — streams and Optional everywhere — with records and switch expressions used where they fit. I know records, sealed classes, pattern matching for `instanceof` and text blocks; we just haven't retrofitted the older code."* Then revise [12-java17-features.md](./12-java17-features.md).

### 7. "@Transactional with MongoDB?"
125 files use `@Transactional`. Know that **MongoDB multi-document transactions require a replica set** (they don't work on a standalone `mongod`). If pushed, say you know transactions are supported from MongoDB 4.0 on replica sets and 4.2 for sharded clusters, and that the document model means most operations are single-document and atomic by nature.

### 8. "Both AWS SDK v1 and v2?"
Yes — v1 `1.12.523` for S3 and Cognito, v2 `2.20.109` for Cognito/SES/SNS/Lambda. Honest framing: *"The service predates SDK v2, so newer integrations use v2 while the older S3 code is still on v1 — a migration we haven't finished."* That reads as awareness, not sloppiness.

### 9. "TypeScript 4.9 in backoffice?"
It's the oldest in the workspace. Fine answer: *"The backoffice lags the other apps — TS 4.9.5 and ngx-translate 14 — because it's an internal tool, so upgrades are prioritised behind the customer-facing apps."*

### 10. "Which build system does the Angular app use?"
`@angular-devkit/build-angular:**browser**` — the **webpack** builder, not the newer esbuild-based `application` builder (which became the default in Angular 17). Worth knowing the distinction if they probe build performance.

---

## Interview answer scripts

### 📌 "Walk me through your tech stack."
> *"RoboGebra is a microservice-style platform. The student-facing web app is Angular 16.2 with TypeScript 5.1, and the mobile app is Ionic 8 on Angular 18 with Capacitor 6, shipping to iOS and Android from the same codebase. The main backend is Spring Boot 3.2 on Java 17 with MongoDB — about 70 domain modules, each with controller, service, repository and model layers. There's a separate admin Spring Boot service on port 4000, a Node/Express CRM for customers and coupons, and a React 18 + Vite UI on top of that CRM. Auth is JWT plus Google and LinkedIn OAuth2. It's deployed as Docker images on a temurin-17 base through Jenkins CI/CD, with S3, Cognito, Lambda, SES and SNS on AWS."*

### 📌 "Which version of Angular and why not the latest?"
> *"Web and backoffice are on 16.2, the mobile app on 18.2. We upgrade per-repo when there's a reason rather than chasing releases — the mobile app went to 18 because Ionic 8 and Capacitor 6 pair with it. Angular 16 gave us the things we actually wanted: required inputs, `takeUntilDestroyed`, and the DestroyRef API."*

### 📌 "Spring Boot version and what you get from it."
> *"3.2.0, which pulls in Spring Framework 6.1 and Spring Security 6.2. The big consequences are Java 17 as the floor, the `jakarta.*` namespace instead of `javax.*`, and Security 6's component-based config — our `SecurityConfig` is a `SecurityFilterChain` bean composing about fifteen custom filters rather than the old `WebSecurityConfigurerAdapter`."*

### 📌 "Tell me about the mobile app." *(your strongest resume line)*
> *"I built the Ionic app from the existing Angular web codebase. It's `@ionic/angular` 8.7.5 on Angular 18.2, with Capacitor 6.2.1 as the native bridge — push notifications through Firebase Messaging, plus preferences, network, keyboard, screen orientation, haptics and in-app update plugins. Android targets SDK 36 with a minimum of 22, built with Gradle 8.2.1; we're at version code 34. Payments go through the Razorpay Capacitor plugin. The shared pieces — KaTeX and MathLive for math rendering, ngx-translate for Tamil/English — carried over from the web app."*

### 📌 "How big is the codebase you work on?"
> *"The Java backend alone is about 1,500 files across 70 domain modules — roughly 100 REST controllers and 200 services. The web app is ~570 TypeScript files, mobile ~500, backoffice ~500."*

---

## Numbers worth quoting

| Metric | Value |
|---|---|
| Java files (portal) | **1,472** |
| Domain packages (portal) | **70** |
| `@RestController` | **101** |
| `@Service` | **199** |
| `@Repository` | **84** |
| `@Configuration` | **44** |
| `@Transactional` | **125** |
| `@Cacheable` | **16** |
| `@Async` | **8** |
| `@ControllerAdvice` | **1** (global exception handling) |
| Java files (admin-portal) | **218** |
| TS files — web / mobile / backoffice | **573 / 502 / 497** |
| HTML templates (web) | **182** |
| JS files (CRM) | **166** |
| TS/TSX files (crm-web) | **129** |
| Custom security filters | **~15** |
| Android version code | **34** |

---

---

## ✅ Re-audit — 3 September 2026

> This file was first audited on **31 July 2026**. I re-read the repos on **3 Sept 2026**. **Every version number below was confirmed unchanged.** The counts have grown slightly (the codebase is active) and there are new figures worth quoting.

### Confirmed unchanged ✅

```
Java 17 · Spring Boot 3.2.0 · MongoDB (no JPA) ✅
Angular 16.2 (web + backoffice) · Angular 18.2 (mobile) ✅
Ionic 8.7.5 · Capacitor 6.2.1 · RxJS 7.8 ✅
TypeScript 5.4 (mobile) / 5.1 (web) / 4.9 (backoffice) ✅
```

### Updated counts (3 Sept 2026) ⭐

| Metric | 31 Jul | **3 Sept** |
|---|---|---|
| `@RestController` | 101 | **110** |
| `@Service` | 199 | **207** |
| `@Repository` | 84 | **85** |
| `@Transactional` | 125 | **130** |
| `@Async` | 8 | **12** |
| `@Document` (MongoDB) | — | **104** ⭐ |
| `@Aggregation` pipelines | — | **27** ⭐ |
| `@Valid` | — | **92** |
| Java `record` | — | **26** |

### New figures worth quoting — the frontend ⭐

```
robogebra-mobile (Angular 18.2 / Ionic 8.7.5):
   163 components · 89 services · 27 route resolvers · 4 guards · 3 interceptors
   19 Capacitor plugins ⭐

   RxJS usage:  map 496 · filter 485 · takeUntil 426 ⭐ · catchError 338
                finalize 254 · switchMap 246 · BehaviorSubject 122 ⭐
                forkJoin 22 · combineLatest 21 · debounceTime 13

   Angular:     async pipe 311 · @Input 252 · ngOnDestroy 100
                trackBy 55 ⭐ · inject() 30 · OnPush 6
                takeUntilDestroyed 5 · standalone 2 ⚠️ · signals 0 ⚠️
```

⭐ **Quote a count, not an adjective.** "Over four hundred `takeUntil` calls" is unforgeable; "we handle subscriptions carefully" is what everyone says.

### The seven "do NOT claim" items — confirmed by count ⭐

```
signals              → 0  ⚠️  "studied, not shipped"
standalone components→ 2  ⚠️  "new code is standalone-first"
NgRx                 → 0  ⚠️  that's EasyVisa
Angular Formly       → 0  ⚠️  that's EasyVisa
JPA / Hibernate      → 0  ⚠️  it's Spring Data MongoDB (an ODM)
MySQL / Postgres     → 0  ⚠️  MongoDB only
Eureka/Feign/circuit breaker/Kafka → 0 ⚠️  modular MONOLITH
```

⚠️ **The `spring-data-jpa` line in the portal's `pom.xml` is unused** — no JPA starter, no `DataSource`, no `@Entity`. It is the single most dangerous trap in your own stack. Confirmed still present on 3 Sept.

### New findings not in the original audit ⭐

```
AWS       S3 (PutObjectRequest + GeneratePresignedUrlRequest ⭐)
          Cognito with THREE user pools ⭐ + JWKS validation (jwks-rsa)
DEV       LocalStack in Docker — S3 + Cognito emulated locally ⭐
CI/CD     Jenkinsfile-CI + Jenkinsfile-CD ⭐
CONFIG    AsyncConfig.java — a dedicated bounded push pool with
          CallerRunsPolicy, and Boot's applicationTaskExecutor
          redeclared by hand (@ConditionalOnMissingBean back-off) ⭐⭐
SECURITY  FilterChainExceptionHandler — because @ControllerAdvice
          does NOT catch exceptions thrown in a security filter ⭐
INDEXES   @CompoundIndex on the hot query paths, e.g.
          {'user.$id': 1, 'exercise.$id': 1} on quiz attempts ⭐
```

→ All of these are written up as answers in **[39 — RoboGebra Code Examples](./39-robogebra-code-examples.md)**.

## ✅ Pre-interview checklist for this file

- [ ] Can say **Angular 16.2 / Ionic 8.7.5 / Capacitor 6.2.1 / Angular 18.2 / Spring Boot 3.2.0 / Java 17 / MongoDB** without pausing
- [ ] Have the **"why two Angular versions"** answer ready
- [ ] Will **not** claim standalone components, NgRx, Formly, JPA, MySQL or Postgres on RoboGebra
- [ ] Can name **five Spring Boot 3 changes** from Boot 2
- [ ] Can describe the **security filter chain** as Chain of Responsibility ([17](./17-solid-design-patterns.md))
- [ ] Can explain **MongoRepository derived queries + `@Aggregation`** ([13-mongodb.md](./13-mongodb.md))
- [ ] Know the honest limits of your **Java 17 feature usage** ([12](./12-java17-features.md))
- [ ] Can draw the **8-repo architecture diagram** from memory

> **Golden rule:** quote the version, then give the *reason*. "Angular 16.2, because…" beats "Angular 16" every time — and it beats "the latest one" by a mile.
