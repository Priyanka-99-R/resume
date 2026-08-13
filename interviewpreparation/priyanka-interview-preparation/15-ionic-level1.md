# Ionic — Level 1 Interview Q&A

> 🎯 **Built for:** *Priyanka — Java with Angular Ionic — Level 1 — Virtual Interview — Codeboard Technology*
>
> **Level 1 = fundamentals round.** They will NOT ask you to architect an offline-sync engine. They will ask: *what is Ionic, how is it different from Angular, which components have you used, how does it become a mobile app, what plugins did you use.* Answer crisply and always land it on **RoboGebra** (your Ionic project, Oct 2024 – Present).
>
> For the harder follow-ups, see `../interview-preparation/Advanced_Ionic_Capacitor_Interview_10Q.md`.

---

## 🔑 Your 30-second Ionic positioning statement

> "I've used Ionic with Angular on **RoboGebra**, an AI-driven math learning platform. It's an Ionic + Angular front end talking to Java/Spring Boot APIs with MongoDB. I built the learning dashboard, the Study Reminder module with local notifications, the Study List, and the quiz module. Because Ionic sits on top of Angular, I reused everything I already know — components, services, RxJS, reactive forms, routing — and got a single codebase that runs as a web app and as a native Android/iOS build through Capacitor."

Memorize that. It answers "do you know Ionic?" in one breath and immediately gives them three follow-up hooks.

---

## Section A — Ionic Fundamentals

### Q1. What is Ionic?
Ionic is an **open-source UI toolkit for building cross-platform mobile and desktop apps using web technologies** — HTML, CSS and JavaScript/TypeScript. You write one codebase and ship it to **Android, iOS, PWA and desktop**.

Key points to say:
- It gives you a large library of **pre-built, mobile-optimized UI components** (`ion-button`, `ion-list`, `ion-modal`, …) that automatically adapt their look to the platform (Material Design on Android, iOS styling on iOS).
- It is **framework-agnostic today** — works with Angular, React, Vue, or plain JavaScript. Internally the components are **Web Components** built with Stencil.
- Ionic handles the **UI layer**. The **native layer** (camera, storage, notifications) is handled by **Capacitor** (or older Cordova).

### Q2. Ionic vs Angular — what's the difference?
This is the #1 Level-1 question for your profile. Answer:

| | Angular | Ionic |
|---|---|---|
| What it is | A full **application framework** — components, DI, routing, forms, HTTP, change detection | A **UI component library + native runtime** for mobile |
| Provides | App architecture and logic | Mobile-styled UI components + access to native device features |
| Runs on | Browser | Browser, Android, iOS, desktop |
| Relationship | Ionic **uses** Angular (or React/Vue) underneath | Ionic is built *on top of* your framework |

**One-liner:** *"Angular is the framework, Ionic is the mobile UI layer and native bridge on top of it. An Ionic-Angular app IS an Angular app — same modules, services, routing and RxJS — just with Ionic's components and Capacitor for native access."*

### Q3. Native vs Hybrid vs Cross-platform apps?
- **Native** — written in the platform's own language (Kotlin/Java for Android, Swift/Objective-C for iOS). Best performance and deepest device access, but you maintain **two separate codebases**.
- **Hybrid** — web app (HTML/CSS/JS) running inside a **WebView** wrapped in a native shell. One codebase, faster and cheaper to build. **Ionic is here.**
- **Cross-platform (compiled)** — e.g. React Native / Flutter. One codebase that renders **actual native UI widgets** (React Native) or its own engine (Flutter), so performance is closer to native.

### Q4. Advantages of Ionic?
1. **Single codebase** for Android, iOS and web → less cost and faster delivery.
2. **Web skills reused** — any Angular developer is productive immediately.
3. **Rich pre-built UI** that follows each platform's design language automatically.
4. **Live reload in the browser** — you develop 90% of the app with `ionic serve`, no emulator needed.
5. **Capacitor plugin ecosystem** for camera, geolocation, storage, notifications, etc.
6. **PWA out of the box** — the same build deploys to the web.

### Q5. Limitations / disadvantages of Ionic?
Say these honestly — Level-1 interviewers respect it:
- Runs in a **WebView**, so heavy graphics/animation-intensive apps (3D games, video editing) perform worse than native.
- **Large lists / heavy DOM** can lag — you mitigate with `ion-virtual-scroll` / virtual scrolling and `trackBy`.
- Dependency on **third-party plugins** for native features; a badly maintained plugin can block you.
- App **bundle size** is larger than a lean native app.
- Debugging native-layer issues still needs Xcode/Android Studio knowledge.

### Q6. What is Capacitor?
**Capacitor is Ionic's native runtime.** It takes your built web app, drops it into a native Android/iOS project, and gives you a **JavaScript API to call native device features**.

- Modern **Promise/async-await** based plugin API.
- The `android/` and `ios/` folders are **real native projects committed to git** — you can open them in Android Studio/Xcode and add native code.
- Backward compatible with most **Cordova** plugins.
- It has replaced Cordova as the default; **Cordova is in maintenance mode**.

### Q7. Cordova vs Capacitor?
| | Cordova | Capacitor |
|---|---|---|
| Native project | Auto-generated, gitignored | Committed to source control, editable |
| Plugin API | Callback-based | Promise / async-await |
| Config file | `config.xml` | `capacitor.config.ts` |
| Framework support | Ionic-oriented | Any web framework |
| PWA / web target | Not supported | First-class |
| Status | Maintenance mode | Actively developed |

### Q8. How does an Ionic app actually run on a phone?
1. You build the web app → static assets in `www/` (Angular) or `dist/`.
2. `npx cap sync` copies those assets into the native `android/` and `ios/` projects and installs plugins.
3. The native app launches a **WebView** that loads `index.html` from the local file system (no network needed).
4. When your JS calls something like `Camera.getPhoto()`, **Capacitor bridges** that call to the native Java/Kotlin or Swift code, which returns the result back to JavaScript as a Promise.

---

## Section B — Setup, CLI & Project Structure

### Q9. How do you create an Ionic project?
```bash
npm install -g @ionic/cli          # install the CLI
ionic start myApp blank --type=angular   # create (blank | tabs | sidemenu | list)
cd myApp
ionic serve                        # run in the browser with live reload
```

### Q10. Common Ionic CLI commands you should be able to rattle off
```bash
ionic serve                 # dev server in the browser, live reload
ionic serve --lab           # side-by-side iOS + Android preview
ionic generate page home    # scaffold page / component / service / guard
ionic build                 # production web build
ionic build --prod

npx cap add android         # add native platform
npx cap sync                # build assets + copy to native + update plugins
npx cap copy                # copy web assets only (faster)
npx cap open android        # open in Android Studio
ionic capacitor run android -l --external   # run on device with live reload
```

### Q11. What's in an Ionic-Angular project?
```
src/
 ├── app/
 │    ├── app.module.ts          # IonicModule.forRoot()
 │    ├── app-routing.module.ts  # lazy-loaded page routes
 │    ├── pages/                 # each page = a folder (ts/html/scss/module)
 │    ├── services/              # API + business logic
 │    └── shared/                # reusable components, pipes, guards
 ├── assets/                     # images, icons, i18n files
 ├── theme/variables.scss        # Ionic CSS variables / theming
 ├── global.scss
 └── index.html
capacitor.config.ts              # app id, name, webDir, plugin config
android/  ios/                   # native projects (Capacitor)
```

### Q12. What is `IonicModule.forRoot()`?
It's imported once in `AppModule` to **initialize Ionic** — registering the components, setting global config (mode, animations, back-button behaviour) and providing Ionic's singleton services. Feature/page modules import plain `IonicModule` (no `forRoot`). Same pattern as `RouterModule.forRoot()` vs `forChild()`.

> With **standalone components** (Angular 15+/Ionic 7+), you instead use `provideIonicAngular()` in `bootstrapApplication` and import components individually.

---

## Section C — Components & UI

### Q13. Which Ionic components have you used? *(Be ready to name 10+)*
- **Layout:** `ion-app`, `ion-header`, `ion-toolbar`, `ion-title`, `ion-content`, `ion-footer`, `ion-grid`/`ion-row`/`ion-col`
- **Navigation:** `ion-tabs`, `ion-menu`, `ion-back-button`, `ion-segment`
- **Lists & data:** `ion-list`, `ion-item`, `ion-label`, `ion-avatar`, `ion-thumbnail`, `ion-item-sliding`, `ion-virtual-scroll`, `ion-infinite-scroll`, `ion-refresher`
- **Forms:** `ion-input`, `ion-textarea`, `ion-select`, `ion-checkbox`, `ion-radio`, `ion-toggle`, `ion-range`, `ion-datetime`, `ion-searchbar`
- **Overlays:** `ion-modal`, `ion-popover`, `ion-alert`, `ion-toast`, `ion-loading`, `ion-action-sheet`
- **Misc:** `ion-button`, `ion-icon`, `ion-card`, `ion-badge`, `ion-chip`, `ion-spinner`, `ion-progress-bar`, `ion-fab`, `ion-skeleton-text`, `ion-accordion`

> 💡 From RoboGebra you can honestly cite: `ion-card` + `ion-grid` for the dashboard, `ion-list`/`ion-item-sliding` for the Study List, `ion-datetime` for reminder scheduling, `ion-modal` + `ion-alert` + `ion-toast` for quiz feedback, `ion-segment` for switching between short and detailed answers.

### Q14. `ion-content` — why is it required?
`ion-content` is the **scrollable area** of a page. It provides the scroll container, safe-area padding, and hosts `ion-refresher`, `ion-infinite-scroll` and the fullscreen behaviour. Without it your page won't scroll properly on device.

```html
<ion-header>
  <ion-toolbar><ion-title>Study List</ion-title></ion-toolbar>
</ion-header>

<ion-content class="ion-padding">
  <ion-refresher slot="fixed" (ionRefresh)="doRefresh($event)">
    <ion-refresher-content></ion-refresher-content>
  </ion-refresher>
  ...
</ion-content>
```

### Q15. Grid system in Ionic?
A 12-column **flexbox** grid: `ion-grid` → `ion-row` → `ion-col`. Responsive breakpoints `size`, `size-sm`, `size-md`, `size-lg`, `size-xl`.
```html
<ion-grid>
  <ion-row>
    <ion-col size="12" size-md="6">Left</ion-col>
    <ion-col size="12" size-md="6">Right</ion-col>
  </ion-row>
</ion-grid>
```

### Q16. Alert vs Toast vs Loading vs ActionSheet vs Modal?
| Component | Use it for |
|---|---|
| **Alert** | Ask a question / confirm — has buttons, blocks the UI |
| **Toast** | Short non-blocking feedback message ("Saved successfully") |
| **Loading** | Blocking spinner while an async call runs |
| **ActionSheet** | Menu of actions sliding up from the bottom |
| **Modal** | A full page shown over the current page (forms, details) |
| **Popover** | Small contextual overlay anchored to an element |

```ts
constructor(private toastCtrl: ToastController,
            private alertCtrl: AlertController,
            private loadingCtrl: LoadingController,
            private modalCtrl: ModalController) {}

async showToast(msg: string) {
  const t = await this.toastCtrl.create({ message: msg, duration: 2000, position: 'bottom' });
  await t.present();
}

async confirmDelete() {
  const alert = await this.alertCtrl.create({
    header: 'Delete reminder?',
    message: 'This cannot be undone.',
    buttons: [
      { text: 'Cancel', role: 'cancel' },
      { text: 'Delete', role: 'destructive', handler: () => this.delete() }
    ]
  });
  await alert.present();
}
```

### Q17. How do you open a modal and get data back?
```ts
async openQuiz() {
  const modal = await this.modalCtrl.create({
    component: QuizPage,
    componentProps: { topicId: 42 }        // pass data IN
  });
  await modal.present();

  const { data, role } = await modal.onWillDismiss();   // get data BACK
  if (role === 'confirm') { this.score = data.score; }
}
```
Inside `QuizPage`: receive with `@Input() topicId!: number;` and close with
`this.modalCtrl.dismiss({ score: 8 }, 'confirm')`.

### Q18. Infinite scroll and pull-to-refresh?
```html
<ion-refresher slot="fixed" (ionRefresh)="refresh($event)">
  <ion-refresher-content pullingText="Pull to refresh"></ion-refresher-content>
</ion-refresher>

<ion-list>
  <ion-item *ngFor="let q of questions; trackBy: trackById">{{ q.title }}</ion-item>
</ion-list>

<ion-infinite-scroll (ionInfinite)="loadMore($event)">
  <ion-infinite-scroll-content loadingText="Loading..."></ion-infinite-scroll-content>
</ion-infinite-scroll>
```
```ts
refresh(ev: any) {
  this.api.getQuestions().subscribe(d => { this.questions = d; ev.target.complete(); });
}
loadMore(ev: any) {
  this.page++;
  this.api.getQuestions(this.page).subscribe(d => {
    this.questions.push(...d);
    ev.target.complete();
    if (!d.length) { ev.target.disabled = true; }   // no more data
  });
}
```
**Always call `event.target.complete()`** — otherwise the spinner never stops. That's the follow-up they ask.

---

## Section D — Navigation & Routing

### Q19. How does navigation work in Ionic Angular?
It uses the **standard Angular Router** — no special Ionic navigation API needed (the old `NavController.push()` stack was Ionic 3).
```ts
// routing module — lazy loaded pages
const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', loadChildren: () => import('./pages/home/home.module').then(m => m.HomePageModule) },
  { path: 'study-list/:id', loadChildren: () => import('./pages/study/study.module').then(m => m.StudyPageModule) },
];
```
```ts
this.router.navigate(['/study-list', id]);                 // go
this.router.navigate(['/home'], { queryParams: { tab: 1 } });
// read it back
this.route.snapshot.paramMap.get('id');
this.route.paramMap.subscribe(p => this.id = p.get('id'));
```

### Q20. What is `NavController` then?
Ionic's thin wrapper over the Angular Router that adds **native-feeling page transitions and stack direction**:
- `navCtrl.navigateForward('/details')` — slides in from the right (push)
- `navCtrl.navigateBack('/home')` — slides back (pop)
- `navCtrl.navigateRoot('/login')` — resets the stack (use after logout)

### Q21. How do you pass data between pages?
1. **Route params** — `/study/:id` for IDs.
2. **Query params** — `{ queryParams: { mode: 'edit' } }`.
3. **A shared service** (best for objects) — a service with a `BehaviorSubject`.
4. **Router state** — `navigate(['/x'], { state: { item } })`, read via `router.getCurrentNavigation()?.extras.state`.
5. **Modal `componentProps`** — for modals/popovers.

> ⚠️ Never pass large objects through the URL. Interviewers like hearing "IDs in the URL, objects through a service."

### Q22. Tabs vs Side menu?
- **`ion-tabs`** — 3–5 top-level destinations, always visible at the bottom, each tab keeps its own navigation stack. Configured with a parent route holding child routes.
- **`ion-menu`** — a drawer for many/secondary destinations; opened with `ion-menu-button` or `menuCtrl.open()`.

### Q23. How do you handle the Android hardware back button?
```ts
this.platform.backButton.subscribeWithPriority(10, () => {
  if (this.router.url === '/home') { App.exitApp(); }   // @capacitor/app
  else { this.navCtrl.back(); }
});
```

---

## Section E — Lifecycle Hooks ⚠️ *High-probability question*

### Q24. Ionic page lifecycle hooks vs Angular lifecycle hooks?
Ionic **caches pages** in the navigation stack, so a page can be *left and re-entered without being destroyed*. That's why Ionic adds its own hooks.

| Hook | When it fires |
|---|---|
| `ionViewWillEnter` | Just **before** the page becomes active — fires **every time** you enter |
| `ionViewDidEnter` | After the page is fully active and the transition finished |
| `ionViewWillLeave` | Just before leaving the page |
| `ionViewDidLeave` | After the page has been left |
| `ngOnInit` | **Once**, when the component is created (may be cached afterwards) |
| `ngOnDestroy` | When the component is actually destroyed / removed from the stack |

**The key interview point:**
> "`ngOnInit` runs only once because Ionic caches the page. So one-time setup goes in `ngOnInit`, but **anything that must be fresh every time the user comes back to the page — like refreshing a list after an edit — goes in `ionViewWillEnter`.**"

```ts
export class StudyListPage {
  ngOnInit() { this.buildForm(); }                    // once
  ionViewWillEnter() { this.loadStudyItems(); }       // every entry — data stays fresh
  ionViewWillLeave() { this.sub?.unsubscribe(); }
}
```

---

## Section F — Native Features / Capacitor Plugins

### Q25. How do you access native device features?
Install a Capacitor plugin, import it, call it — it returns a Promise.
```bash
npm install @capacitor/camera
npx cap sync
```
```ts
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';

async takePhoto() {
  const photo = await Camera.getPhoto({
    quality: 80,
    resultType: CameraResultType.Uri,
    source: CameraSource.Camera
  });
  this.imgSrc = photo.webPath;
}
```

### Q26. Which Capacitor plugins do you know?
`@capacitor/camera`, `@capacitor/geolocation`, `@capacitor/preferences` (key-value storage), `@capacitor/filesystem`, `@capacitor/network`, `@capacitor/push-notifications`, `@capacitor/local-notifications`, `@capacitor/share`, `@capacitor/splash-screen`, `@capacitor/status-bar`, `@capacitor/app`, `@capacitor/haptics`, `@capacitor/device`, `@capacitor/browser`, `@capacitor/toast`.

> 💡 **RoboGebra hook:** the Study Reminder module maps directly onto `@capacitor/local-notifications` for scheduled reminders and `@capacitor/preferences` for the user's reminder settings. Have that ready.

### Q27. How do you check whether the app is running on a device or in the browser?
```ts
import { Platform } from '@ionic/angular';
import { Capacitor } from '@capacitor/core';

constructor(private platform: Platform) {}

ngOnInit() {
  this.platform.ready().then(() => {
    if (Capacitor.isNativePlatform()) { /* device-only code */ }
    if (this.platform.is('android')) { }
    if (this.platform.is('ios')) { }
    if (this.platform.is('mobileweb')) { }
  });
}
```
Also `Capacitor.isPluginAvailable('Camera')` before calling a plugin — this is the safe pattern, because plugins that need native code will fail in `ionic serve`.

### Q28. How do you store data locally?
| Option | Use for |
|---|---|
| `@capacitor/preferences` | Small key-value data — tokens, settings, flags |
| `@ionic/storage-angular` | Key-value with an IndexedDB/SQLite driver, larger data |
| SQLite plugin | Structured/relational offline data, large datasets |
| `localStorage` | Web only — **not reliable on native**, can be cleared by the OS |

```ts
import { Preferences } from '@capacitor/preferences';
await Preferences.set({ key: 'token', value: jwt });
const { value } = await Preferences.get({ key: 'token' });
await Preferences.remove({ key: 'token' });
```

### Q29. How do you handle permissions?
Plugins expose `checkPermissions()` and `requestPermissions()`. You also declare them natively: `AndroidManifest.xml` for Android, `Info.plist` usage descriptions for iOS (iOS **rejects the build** without a usage description string).

---

## Section G — Theming & Styling

### Q30. How do you theme an Ionic app?
Ionic is styled with **CSS custom properties (variables)**, defined in `src/theme/variables.scss`.
```scss
:root {
  --ion-color-primary: #3880ff;
  --ion-color-primary-contrast: #ffffff;
  --ion-background-color: #ffffff;
}
```
Override per-component too:
```scss
ion-button { --background: #6c3; --border-radius: 12px; }
```
> Because Ionic components use **Shadow DOM**, plain CSS selectors often don't reach inside them — **that's why you style through CSS variables** (or `::part()`). This is a classic follow-up.

### Q31. Dark mode?
```scss
@media (prefers-color-scheme: dark) {
  body { --ion-background-color: #121212; --ion-text-color: #fff; }
}
```
Or toggle a class manually: `document.body.classList.toggle('dark', enabled)`.

### Q32. What is `mode` in Ionic?
`mode` decides the platform styling: `'md'` (Material Design) or `'ios'`. By default Ionic picks it from the device. You can force it globally:
```ts
IonicModule.forRoot({ mode: 'md' })
```
or per component: `<ion-button mode="ios">`.

### Q33. Ionic utility CSS classes?
`ion-padding`, `ion-margin`, `ion-text-center`, `ion-float-right`, `ion-hide`, `ion-hide-md-down`, `ion-justify-content-center`, `ion-align-items-center`.

---

## Section H — Working with APIs

### Q34. How do you call a REST API in Ionic?
Exactly like Angular — `HttpClient` in a service:
```ts
@Injectable({ providedIn: 'root' })
export class StudyService {
  private base = environment.apiUrl;     // Spring Boot backend
  constructor(private http: HttpClient) {}

  getStudyList(): Observable<StudyItem[]> {
    return this.http.get<StudyItem[]>(`${this.base}/study-list`)
      .pipe(catchError(this.handleError));
  }
  create(item: StudyItem) { return this.http.post<StudyItem>(`${this.base}/study-list`, item); }
  update(id: string, item: StudyItem) { return this.http.put<StudyItem>(`${this.base}/study-list/${id}`, item); }
  delete(id: string) { return this.http.delete(`${this.base}/study-list/${id}`); }
}
```
Add a **JWT interceptor** for the auth header, and a **loading indicator** around slow calls.

### Q35. CORS works in `ionic serve` but breaks on device — why?
On the browser, the origin is `http://localhost:8100`. On a device the WebView origin is different (`capacitor://localhost` on iOS, `http://localhost` on Android), so the server's CORS config must allow it — or you avoid CORS entirely because the native WebView isn't subject to the same rules once configured. Fixes: proxy in `ionic.config.json` during development, correct **CORS configuration on the Spring Boot side** (`@CrossOrigin` / a `WebMvcConfigurer`), and `server.androidScheme` in `capacitor.config.ts`.

> This is a great answer for a **Java + Ionic** interview because it shows you understand both ends.

---

## Section I — Build & Deployment

### Q36. Steps to build and run an Ionic app on Android?
```bash
ionic build --prod            # 1. build web assets
npx cap add android           # 2. once, adds the native project
npx cap sync android          # 3. copy assets + sync plugins
npx cap open android          # 4. opens Android Studio
# 5. Build > Generate Signed Bundle/APK  → upload to Play Console
```
For iOS the same, but you need **macOS + Xcode**, a signing certificate and a provisioning profile, then upload via Xcode/Transporter to App Store Connect.

### Q37. What's in `capacitor.config.ts`?
```ts
const config: CapacitorConfig = {
  appId: 'com.robogebra.app',
  appName: 'RoboGebra',
  webDir: 'www',
  server: { androidScheme: 'https' },
  plugins: { SplashScreen: { launchShowDuration: 2000 } }
};
```

### Q38. `cap copy` vs `cap sync` vs `cap update`?
- `copy` — copies web assets + config into the native projects (fastest, use after a plain code change).
- `update` — updates the native dependencies/plugins.
- `sync` = `copy` + `update`. Use after installing a plugin.

### Q39. Can an Ionic app be a PWA?
Yes — that's a headline Ionic feature. `ng add @angular/pwa` adds a service worker and manifest, then `ionic build --prod` gives you a deployable PWA from the same codebase.

---

## Section J — Resume-linked questions *(expect these)*

### Q40. "You mention Ionic on your resume — what exactly did you build with it?"
> "On **RoboGebra**, an AI-driven math learning platform, the front end is **Ionic + Angular** and the backend is **Java/Spring Boot with MongoDB**. My main modules were:
> - the **personalized learning dashboard** — progress analytics and learning timelines, built with `ion-card`/`ion-grid` and Chart rendering;
> - the **Study Reminder system** — flexible scheduling with `ion-datetime`, notification delivery, and full REST CRUD APIs;
> - the **Study List module** — create/organize/track goals, with `ion-item-sliding` for swipe actions and real-time sync;
> - the **Quiz module** — dynamic questions, instant evaluation and solution walkthroughs in modals;
> - plus the **bilingual (Tamil/English) AI explanation** views with step-by-step reasoning."

### Q41. "Since Ionic is just Angular, what did you actually have to learn?"
> "Three things: the **Ionic page lifecycle** — `ionViewWillEnter` versus `ngOnInit`, because pages are cached; **styling through CSS variables** instead of normal CSS, because the components use Shadow DOM; and the **Capacitor build cycle** — build, `cap sync`, then run from Android Studio, plus handling code paths that only work on a real device."

### Q42. "Any challenge you faced in the Ionic app?"
Pick one and tell it in STAR form. A safe, true-to-your-work example:
> **S** — the Study List needed to feel instant, but every edit re-fetched from the server and the list flickered.
> **T** — make updates feel real-time without hammering the API.
> **A** — moved the list into a service-held `BehaviorSubject` so all screens shared one stream, refreshed on `ionViewWillEnter` instead of `ngOnInit`, added `trackBy` on the `*ngFor`, and used optimistic UI updates with rollback on API error.
> **R** — no more flicker, fewer API calls, and the list stayed consistent across the dashboard and the detail page.

*(Adjust the details to what you actually did — never invent numbers.)*

### Q43. "Did you publish to the Play Store / App Store?"
Answer honestly. If you didn't own the release, say:
> "I worked on the app build and tested on device through Android Studio; the store release was handled by our lead — but I know the flow: signed bundle, versionCode bump, Play Console internal track, then production."

---

## ⚡ Rapid-fire round (say the answer in one line)

| Question | Answer |
|---|---|
| Latest Ionic version family? | Ionic 7/8 (Angular standalone support, `ion-input` label props) |
| Ionic components are built with? | **Stencil** — compiled to Web Components |
| Default native runtime? | **Capacitor** |
| Which folder holds web assets after build? | `www/` (Angular) |
| How to preview both platforms at once? | `ionic serve --lab` |
| Component for a scrollable page body? | `ion-content` |
| Hook that fires on every page entry? | `ionViewWillEnter` |
| Storage plugin for key-value? | `@capacitor/preferences` |
| How to style inside a shadow-DOM component? | CSS variables / `::part()` |
| Force Material styling? | `mode: 'md'` |
| Navigate with a forward animation? | `navCtrl.navigateForward()` |
| Stop the infinite-scroll spinner? | `event.target.complete()` |
| Detect native vs web? | `Capacitor.isNativePlatform()` / `platform.is('android')` |
| Grid columns? | 12 |
| Is Ionic free? | Yes — MIT licensed open source (paid add-ons like Appflow exist) |

---

## ✅ Ionic checklist before the call

- [ ] Can explain **Ionic vs Angular** in 20 seconds
- [ ] Can explain **`ngOnInit` vs `ionViewWillEnter`** and *why* it matters (page caching)
- [ ] Can name **10+ Ionic components** without pausing
- [ ] Can describe the **build → `cap sync` → Android Studio** flow
- [ ] Can name **5 Capacitor plugins** and one you used
- [ ] Know **theming = CSS variables** because of Shadow DOM
- [ ] Have the **RoboGebra Ionic story** ready with 4 concrete modules
- [ ] Have **one STAR challenge** from the Ionic app

---

**Next:** [16 — Codeboard Technology Level-1 Interview Guide](./16-codeboard-technology-level1.md)
