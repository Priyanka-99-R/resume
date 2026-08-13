# 10 Advanced Ionic & Capacitor Interview Questions & Answers
### For 5+ Years Senior Mobile / Full-Stack Developer
### Target: PwC, Deloitte, Cognizant, Capgemini, Product Companies

---

## Q1. What is the difference between Ionic with Cordova vs Ionic with Capacitor? Why did you migrate (or would you migrate) to Capacitor?

**Summary:**
Capacitor is Ionic's modern native runtime that replaced Cordova as the default. Unlike Cordova which wraps a WebView inside a native shell, Capacitor treats the web app as a first-class citizen alongside native code. The key differences: Capacitor uses a modern API (Promises, not callbacks), manages native projects in source control, supports any web framework, and has backward compatibility with most Cordova plugins.

**Comparison:**

| Feature | Cordova | Capacitor |
|---------|---------|-----------|
| Native project | Generated, `.gitignore`d | Committed to source control |
| Plugin API | Callbacks | Modern Promises / async-await |
| Framework lock-in | Ionic only | Any (Angular, React, Vue, vanilla) |
| Live Reload | Partial (requires plugin) | Built-in with `--livereload` |
| Cordova plugin support | Native | Backward compatible (most work) |
| Native IDE | Not encouraged | Encouraged (Xcode, Android Studio) |
| Web deployment | Not supported | PWA-first architecture |
| CLI | `cordova build` | `npx cap sync` → open in IDE → build |
| Config | `config.xml` | `capacitor.config.ts` (TypeScript!) |
| Community/Future | Maintenance mode | Actively developed by Ionic team |

**The Workflow Difference:**

```
CORDOVA Workflow:
  1. Write web code
  2. cordova build android → generates Android project (ephemeral)
  3. cordova run android → builds and deploys
  ❌ Native project is auto-generated, not customizable
  ❌ If you need native code changes, you use hooks (fragile)
  ❌ Native project is in .gitignore — not reproducible

CAPACITOR Workflow:
  1. Write web code
  2. ionic build (or ng build) → builds web assets to www/dist
  3. npx cap sync → copies web assets to native projects + installs plugins
  4. npx cap open android → opens in Android Studio
  5. Build and run from Android Studio / Xcode
  ✅ Native projects (android/, ios/) are in git — fully customizable
  ✅ Add native code directly in Android Studio/Xcode
  ✅ Reproducible builds — any team member can clone and build
```

**Migration from Cordova to Capacitor:**

```bash
# Step 1: Install Capacitor
npm install @capacitor/core @capacitor/cli

# Step 2: Initialize
npx cap init "MyApp" "com.company.myapp" --web-dir dist

# Step 3: Add platforms
npx cap add android
npx cap add ios

# Step 4: Migrate Cordova plugins
# Most work out of the box. For Ionic Native wrappers:
npm install @awesome-cordova-plugins/camera  # still works with Capacitor
# OR use Capacitor's native plugin:
npm install @capacitor/camera  # preferred — modern API

# Step 5: Update config
# Move config.xml settings to capacitor.config.ts
```

```typescript
// capacitor.config.ts — typed configuration
import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.company.myapp',
  appName: 'MyApp',
  webDir: 'dist',
  server: {
    androidScheme: 'https',  // Required for modern Android security
    iosScheme: 'capacitor',
  },
  plugins: {
    SplashScreen: {
      launchAutoHide: false,
      androidScaleType: 'CENTER_CROP',
    },
    PushNotifications: {
      presentationOptions: ['badge', 'sound', 'alert'],
    },
  },
};

export default config;
```

**Real-World Scenario:**
We migrated a 3-year-old Ionic/Cordova app to Capacitor. The main drivers: Cordova plugins were unmaintained (camera plugin hadn't been updated in 18 months), the build process was fragile (hooks kept breaking on CI), and we needed to add custom native code for a Bluetooth feature that no Cordova plugin supported. After migration, we added native Kotlin code directly in `android/`, the CI build was reproducible (native projects in git), and plugin updates came from Capacitor's maintained ecosystem.

**Common Mistakes:**
- Not running `npx cap sync` after `ionic build` — web assets aren't copied to native projects, app shows stale version.
- Keeping Cordova plugins when Capacitor equivalents exist — Capacitor plugins have better APIs and are actively maintained.
- Ignoring the `android/` and `ios/` folders in `.gitignore` — they MUST be committed in Capacitor (unlike Cordova).

**Closing:**
Capacitor is the present and future of Ionic. It's not just a Cordova replacement — it's a fundamentally better architecture. Native projects in source control, modern plugin APIs, any-framework support, and the ability to write native code directly. Every new Ionic project should start with Capacitor, and every Cordova project should plan migration.

---

## Q2. How does Capacitor's plugin architecture work? How do you create a custom native plugin?

**Summary:**
Capacitor plugins bridge JavaScript and native code (Swift/Kotlin). The JS side calls a method, Capacitor's bridge serializes it, the native side executes it, and the result comes back as a Promise. I create custom plugins when no existing plugin covers my use case — like integrating a proprietary SDK, hardware-specific features, or platform APIs not yet wrapped.

**Plugin Architecture:**

```
┌─────────────────────────────────────────────────────┐
│                    WEB LAYER                         │
│   TypeScript code calls: Camera.getPhoto()          │
│                     │                                │
│              Capacitor Bridge                        │
│         (serializes call to JSON message)            │
└────────────────────┬────────────────────────────────┘
                     │  JSON message via WebView bridge
                     ▼
┌─────────────────────────────────────────────────────┐
│                 NATIVE LAYER                         │
│                                                      │
│  ┌──────────────────┐    ┌────────────────────────┐ │
│  │  iOS (Swift)      │    │  Android (Kotlin/Java) │ │
│  │  CAPPlugin class  │    │  Plugin class          │ │
│  │  @objc func       │    │  @PluginMethod         │ │
│  │  getPhoto()       │    │  fun getPhoto()        │ │
│  └──────────────────┘    └────────────────────────┘ │
│                                                      │
│        Calls native Camera API                       │
│        Returns result via bridge                     │
└─────────────────────────────────────────────────────┘
```

**Creating a Custom Plugin — Bluetooth Scanner Example:**

**Step 1 — Define the TypeScript Interface:**
```typescript
// src/plugins/bluetooth-scanner.ts
import { registerPlugin } from '@capacitor/core';

export interface BluetoothScannerPlugin {
  startScan(options?: { timeout?: number }): Promise<{ devices: BluetoothDevice[] }>;
  stopScan(): Promise<void>;
  connect(options: { deviceId: string }): Promise<{ connected: boolean }>;
  disconnect(options: { deviceId: string }): Promise<void>;
  // Listener for real-time scan results
  addListener(
    eventName: 'deviceFound',
    listener: (device: BluetoothDevice) => void
  ): Promise<PluginListenerHandle>;
}

export interface BluetoothDevice {
  id: string;
  name: string;
  rssi: number;
}

const BluetoothScanner = registerPlugin<BluetoothScannerPlugin>('BluetoothScanner', {
  web: () => import('./bluetooth-scanner-web').then(m => new m.BluetoothScannerWeb()),
});

export default BluetoothScanner;
```

**Step 2 — Android Implementation (Kotlin):**
```kotlin
// android/app/src/main/java/com/app/plugins/BluetoothScannerPlugin.kt
package com.app.plugins

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.JSObject
import com.getcapacitor.JSArray

@CapacitorPlugin(name = "BluetoothScanner")
class BluetoothScannerPlugin : Plugin() {

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var scanning = false

    @PluginMethod
    fun startScan(call: PluginCall) {
        val timeout = call.getInt("timeout", 10000)  // default 10s

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            call.reject("Bluetooth is not available or not enabled")
            return
        }

        val scanner = bluetoothAdapter.bluetoothLeScanner
        scanning = true

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = JSObject().apply {
                    put("id", result.device.address)
                    put("name", result.device.name ?: "Unknown")
                    put("rssi", result.rssi)
                }
                // Emit event to JS layer in real-time
                notifyListeners("deviceFound", device)
            }
        }

        scanner.startScan(scanCallback)

        // Auto-stop after timeout
        bridge.activity.runOnUiThread {
            android.os.Handler().postDelayed({
                if (scanning) {
                    scanner.stopScan(scanCallback)
                    scanning = false
                }
            }, timeout.toLong())
        }

        call.resolve()
    }

    @PluginMethod
    fun stopScan(call: PluginCall) {
        scanning = false
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(null)
        call.resolve()
    }

    @PluginMethod
    fun connect(call: PluginCall) {
        val deviceId = call.getString("deviceId")
            ?: return call.reject("deviceId is required")

        // Native Bluetooth connection logic...
        val result = JSObject().apply {
            put("connected", true)
        }
        call.resolve(result)
    }
}
```

**Step 3 — Register Plugin in Android:**
```kotlin
// android/app/src/main/java/com/app/MainActivity.kt
import com.app.plugins.BluetoothScannerPlugin

class MainActivity : BridgeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        registerPlugin(BluetoothScannerPlugin::class.java)
        super.onCreate(savedInstanceState)
    }
}
```

**Step 4 — iOS Implementation (Swift):**
```swift
// ios/App/App/Plugins/BluetoothScannerPlugin.swift
import Capacitor
import CoreBluetooth

@objc(BluetoothScannerPlugin)
public class BluetoothScannerPlugin: CAPPlugin, CBCentralManagerDelegate {

    private var centralManager: CBCentralManager?

    @objc func startScan(_ call: CAPPluginCall) {
        centralManager = CBCentralManager(delegate: self, queue: nil)
        // CBCentralManager starts scanning when powered on
        call.resolve()
    }

    @objc func stopScan(_ call: CAPPluginCall) {
        centralManager?.stopScan()
        call.resolve()
    }

    // Delegate callback when device found
    public func centralManager(_ central: CBCentralManager,
                               didDiscover peripheral: CBPeripheral,
                               advertisementData: [String: Any],
                               rssi RSSI: NSNumber) {
        let device: [String: Any] = [
            "id": peripheral.identifier.uuidString,
            "name": peripheral.name ?? "Unknown",
            "rssi": RSSI.intValue
        ]
        notifyListeners("deviceFound", data: device)
    }
}
```

**Step 5 — Usage in Angular/Ionic Component:**
```typescript
import BluetoothScanner from '../plugins/bluetooth-scanner';

@Component({
  template: `
    <ion-button (click)="scan()">Scan for Devices</ion-button>
    <ion-list>
      @for (device of devices(); track device.id) {
        <ion-item (click)="connectTo(device)">
          <ion-label>
            <h2>{{ device.name }}</h2>
            <p>Signal: {{ device.rssi }} dBm</p>
          </ion-label>
        </ion-item>
      }
    </ion-list>
  `
})
export class BluetoothPage {
  devices = signal<BluetoothDevice[]>([]);

  async scan() {
    // Listen for devices in real-time
    await BluetoothScanner.addListener('deviceFound', (device) => {
      this.devices.update(list => {
        const existing = list.find(d => d.id === device.id);
        if (existing) return list.map(d => d.id === device.id ? device : d);
        return [...list, device];
      });
    });

    await BluetoothScanner.startScan({ timeout: 15000 });
  }

  async connectTo(device: BluetoothDevice) {
    const result = await BluetoothScanner.connect({ deviceId: device.id });
    if (result.connected) {
      console.log(`Connected to ${device.name}`);
    }
  }
}
```

**Closing:**
Capacitor's plugin architecture is clean — TypeScript interface on the web, native implementation on each platform, Capacitor bridge handles serialization. I create custom plugins for hardware access, proprietary SDKs, and platform-specific features. The key is `registerPlugin` on the JS side and `@PluginMethod` / `@objc func` on the native side.

---

## Q3. How do you handle navigation and routing in an Ionic Angular app? What is the ion-router-outlet and how does page lifecycle differ from standard Angular?

**Summary:**
Ionic uses Angular Router under the hood but wraps it with `ion-router-outlet` instead of `<router-outlet>`. The critical difference: `ion-router-outlet` caches pages in the DOM when navigating away (for native-like back animations). This means `ngOnInit` fires only ONCE, not on every navigation. For "on page shown" logic, I use Ionic lifecycle hooks: `ionViewWillEnter`, `ionViewDidEnter`, `ionViewWillLeave`, `ionViewDidLeave`.

**Ionic Page Lifecycle vs Angular Lifecycle:**

```
FIRST VISIT to a page:
  constructor()          → DI injection
  ngOnInit()             → One-time setup (fires ONCE — page is cached!)
  ionViewWillEnter()     → Page is about to become visible
  ionViewDidEnter()      → Page is now fully visible
  ngAfterViewInit()      → DOM ready

NAVIGATE AWAY:
  ionViewWillLeave()     → Page is about to be hidden
  ionViewDidLeave()      → Page is now hidden (but STILL IN DOM!)
  ❌ ngOnDestroy() does NOT fire — page is cached, not destroyed

NAVIGATE BACK to same page:
  ❌ ngOnInit() does NOT fire — page was never destroyed
  ✅ ionViewWillEnter()  → Fires every time page becomes visible
  ✅ ionViewDidEnter()   → This is where you refresh data
```

**Practical Example:**

```typescript
@Component({
  selector: 'app-order-list',
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>My Orders</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content>
      <ion-refresher slot="fixed" (ionRefresh)="refreshOrders($event)">
        <ion-refresher-content></ion-refresher-content>
      </ion-refresher>

      @if (loading()) {
        <ion-spinner></ion-spinner>
      }

      <ion-list>
        @for (order of orders(); track order.id) {
          <ion-item [routerLink]="['/orders', order.id]" detail>
            <ion-label>
              <h2>Order #{{ order.id }}</h2>
              <p>{{ order.total | currency }}</p>
            </ion-label>
            <ion-badge slot="end" [color]="getStatusColor(order.status)">
              {{ order.status }}
            </ion-badge>
          </ion-item>
        }
      </ion-list>
    </ion-content>
  `
})
export class OrderListPage implements OnInit, ViewWillEnter {
  orders = signal<Order[]>([]);
  loading = signal(false);

  private orderService = inject(OrderService);

  // ✅ ngOnInit: one-time setup (subscriptions, initial config)
  ngOnInit() {
    console.log('ngOnInit — fires ONCE, page cached after this');
  }

  // ✅ ionViewWillEnter: fires EVERY TIME user navigates to this page
  ionViewWillEnter() {
    console.log('ionViewWillEnter — refresh data here');
    this.loadOrders();
  }

  async loadOrders() {
    this.loading.set(true);
    const orders = await firstValueFrom(this.orderService.getAll());
    this.orders.set(orders);
    this.loading.set(false);
  }

  async refreshOrders(event: RefresherCustomEvent) {
    await this.loadOrders();
    event.target.complete();
  }
}
```

**Routing Configuration:**

```typescript
// app.routes.ts
const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('./tabs/tabs.routes').then(m => m.routes)
  }
];

// tabs.routes.ts
const routes: Routes = [
  {
    path: 'tabs',
    component: TabsPage,
    children: [
      {
        path: 'home',
        loadComponent: () => import('./home/home.page').then(m => m.HomePage)
      },
      {
        path: 'orders',
        loadComponent: () => import('./orders/order-list.page').then(m => m.OrderListPage)
      },
      {
        path: 'profile',
        loadComponent: () => import('./profile/profile.page').then(m => m.ProfilePage)
      },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  }
];

// Detail page — outside tabs (pushes full screen)
// app.routes.ts
{
  path: 'orders/:id',
  loadComponent: () => import('./orders/order-detail.page').then(m => m.OrderDetailPage)
}
```

**Common Mistakes:**
- Fetching data in `ngOnInit` instead of `ionViewWillEnter` — data goes stale because the page is cached and `ngOnInit` doesn't re-fire.
- Not implementing `ViewWillEnter` interface — TypeScript won't warn you about the typo `ionViewWillEnter` vs `ionviewwillenter`.
- Using Angular `<router-outlet>` instead of `<ion-router-outlet>` — breaks page transitions and caching behavior.
- Not unsubscribing — even though `ngOnDestroy` rarely fires, subscriptions created in `ionViewWillEnter` should be cleaned up in `ionViewWillLeave`.

**Closing:**
The #1 Ionic lifecycle mistake is using `ngOnInit` for data loading. Use `ionViewWillEnter` — it fires every time the page appears. `ngOnInit` for one-time setup, `ionViewWillEnter` for data refresh, `ionViewWillLeave` for cleanup. This is the most common interview catch for Ionic developers.

---

## Q4. How do you handle platform-specific code in Ionic? How do you detect and adapt for iOS, Android, and Web?

**Summary:**
Ionic provides the `Platform` service and CSS platform classes to detect and adapt for iOS, Android, and web. For UI differences, Ionic's adaptive styling handles most cases automatically (Material Design on Android, iOS design on iOS). For logic differences, I use `Capacitor.isNativePlatform()` and `Platform.is()`. For deep native differences, I use dependency injection to swap service implementations per platform.

**Detection Methods:**

```typescript
import { Platform } from '@ionic/angular';
import { Capacitor } from '@capacitor/core';

@Component({...})
export class AppComponent {
  private platform = inject(Platform);

  ngOnInit() {
    // Capacitor checks — is this a native app or web browser?
    console.log(Capacitor.isNativePlatform());    // true on device, false in browser
    console.log(Capacitor.getPlatform());          // 'ios' | 'android' | 'web'

    // Ionic Platform checks — more granular
    console.log(this.platform.is('ios'));           // true on iOS device or iOS simulator
    console.log(this.platform.is('android'));       // true on Android device
    console.log(this.platform.is('hybrid'));        // true if running in Capacitor/Cordova
    console.log(this.platform.is('mobileweb'));     // true if mobile browser (not native)
    console.log(this.platform.is('desktop'));       // true if desktop browser
    console.log(this.platform.is('pwa'));           // true if installed PWA

    // Platform-specific initialization
    if (Capacitor.isNativePlatform()) {
      this.initPushNotifications();
      this.initStatusBar();
      this.initDeepLinks();
    }
  }

  private async initStatusBar() {
    if (this.platform.is('ios')) {
      await StatusBar.setStyle({ style: Style.Light });
    } else if (this.platform.is('android')) {
      await StatusBar.setBackgroundColor({ color: '#1976d2' });
    }
    // On web — StatusBar plugin does nothing (gracefully)
  }
}
```

**Platform-Specific Feature Handling:**

```typescript
// Share functionality — different per platform
@Injectable({ providedIn: 'root' })
export class ShareService {

  async share(data: { title: string; text: string; url: string }) {
    if (Capacitor.isNativePlatform()) {
      // Native share sheet (iOS/Android)
      await Share.share({
        title: data.title,
        text: data.text,
        url: data.url,
        dialogTitle: 'Share with...'
      });
    } else if (navigator.share) {
      // Web Share API (modern mobile browsers)
      await navigator.share(data);
    } else {
      // Fallback: copy to clipboard
      await navigator.clipboard.writeText(data.url);
      this.toastService.show('Link copied to clipboard!');
    }
  }
}

// Storage — Capacitor Preferences on native, localStorage on web
@Injectable({ providedIn: 'root' })
export class StorageService {

  async get(key: string): Promise<string | null> {
    if (Capacitor.isNativePlatform()) {
      const { value } = await Preferences.get({ key });
      return value;
    }
    return localStorage.getItem(key);
  }

  async set(key: string, value: string): Promise<void> {
    if (Capacitor.isNativePlatform()) {
      await Preferences.set({ key, value });
    } else {
      localStorage.setItem(key, value);
    }
  }
}
```

**CSS Platform Adaptation:**

```scss
// Ionic automatically adds platform classes to <html>:
// <html class="ios"> or <html class="md"> (Material Design / Android)

// Platform-specific CSS
.header-logo {
  height: 40px;
}

// iOS-specific
.ios .header-logo {
  margin-top: 44px;  // account for iOS status bar
}

// Android-specific
.md .header-logo {
  margin-top: 24px;  // Android status bar is smaller
}

// iOS safe areas (notch, home indicator)
ion-content {
  --padding-top: env(safe-area-inset-top);
  --padding-bottom: env(safe-area-inset-bottom);
}

// Different per platform using CSS variables
:root {
  --app-header-height: 56px;
}

.ios {
  --app-header-height: 44px;  // iOS standard
}
```

**Closing:**
Ionic handles most platform differences automatically through adaptive styling. For logic, use `Capacitor.isNativePlatform()` for native vs web and `Platform.is()` for specific platforms. I wrap platform-specific logic in services so components stay clean. The goal is one codebase with graceful adaptation, not littering components with `if (ios)` checks.

---

## Q5. How do you manage app state, offline data, and API synchronization in an Ionic app?

**Summary:**
I use a Signal-based service layer for state, Capacitor Preferences or SQLite for local persistence, and a sync queue for offline-to-online data reconciliation. The architecture: data flows from API → local cache → UI. When offline, reads come from cache, writes queue locally, and sync when connectivity returns.

**Offline-First Architecture:**

```
┌────────────────────────────────────────────────┐
│                     UI                          │
│   Component reads from → Signal-based Store     │
└───────────────────┬────────────────────────────┘
                    │
                    ▼
┌────────────────────────────────────────────────┐
│              DATA SERVICE LAYER                 │
│                                                 │
│  Online?                                        │
│  ├── YES → Fetch from API → Update local cache  │
│  │         → Update signals → UI refreshes      │
│  │                                              │
│  └── NO  → Read from local cache                │
│           → Writes go to SYNC QUEUE             │
│           → When back online → process queue    │
└───────────────────┬────────────────────────────┘
                    │
         ┌──────────┼──────────┐
         ▼          ▼          ▼
   ┌──────────┐ ┌────────┐ ┌──────────┐
   │  SQLite   │ │ Prefs  │ │ Sync     │
   │  (complex │ │ (KV)   │ │ Queue    │
   │  data)    │ │        │ │ (pending │
   │           │ │        │ │  writes) │
   └──────────┘ └────────┘ └──────────┘
```

**Implementation:**

```typescript
// Network status monitoring
@Injectable({ providedIn: 'root' })
export class NetworkService {
  private online = signal(true);
  readonly isOnline = this.online.asReadonly();

  constructor() {
    // Capacitor Network plugin
    Network.addListener('networkStatusChange', (status) => {
      this.online.set(status.connected);
      if (status.connected) {
        this.syncService.processQueue();  // sync pending changes
      }
    });
  }
}

// Offline-aware data service
@Injectable({ providedIn: 'root' })
export class OrderDataService {
  private orders = signal<Order[]>([]);
  readonly allOrders = this.orders.asReadonly();

  private http = inject(HttpClient);
  private storage = inject(LocalStorageService);
  private network = inject(NetworkService);
  private syncQueue = inject(SyncQueueService);

  async loadOrders(): Promise<void> {
    if (this.network.isOnline()) {
      // Online: fetch from API, cache locally
      try {
        const orders = await firstValueFrom(
          this.http.get<Order[]>('/api/orders')
        );
        this.orders.set(orders);
        await this.storage.set('orders', JSON.stringify(orders));
      } catch (e) {
        // API failed — fall back to cache
        await this.loadFromCache();
      }
    } else {
      // Offline: load from local cache
      await this.loadFromCache();
    }
  }

  private async loadFromCache() {
    const cached = await this.storage.get('orders');
    if (cached) {
      this.orders.set(JSON.parse(cached));
    }
  }

  async createOrder(order: CreateOrderDto): Promise<void> {
    if (this.network.isOnline()) {
      const created = await firstValueFrom(
        this.http.post<Order>('/api/orders', order)
      );
      this.orders.update(list => [...list, created]);
      await this.storage.set('orders', JSON.stringify(this.orders()));
    } else {
      // Offline: save locally and queue for sync
      const tempOrder: Order = {
        ...order,
        id: `temp_${Date.now()}`,
        status: 'PENDING_SYNC',
        createdAt: new Date().toISOString()
      };
      this.orders.update(list => [...list, tempOrder]);
      await this.storage.set('orders', JSON.stringify(this.orders()));

      // Queue for sync when back online
      await this.syncQueue.enqueue({
        action: 'CREATE_ORDER',
        payload: order,
        tempId: tempOrder.id,
        timestamp: Date.now()
      });
    }
  }
}

// Sync queue — processes pending writes when online
@Injectable({ providedIn: 'root' })
export class SyncQueueService {
  private readonly QUEUE_KEY = 'sync_queue';

  async enqueue(item: SyncItem): Promise<void> {
    const queue = await this.getQueue();
    queue.push(item);
    await Preferences.set({ key: this.QUEUE_KEY, value: JSON.stringify(queue) });
  }

  async processQueue(): Promise<void> {
    const queue = await this.getQueue();
    if (queue.length === 0) return;

    for (const item of queue) {
      try {
        await this.processItem(item);
        await this.removeFromQueue(item);
      } catch (e) {
        console.error('Sync failed for item:', item, e);
        // Leave in queue for next attempt
        break;  // Stop processing — maintain order
      }
    }
  }

  private async processItem(item: SyncItem): Promise<void> {
    switch (item.action) {
      case 'CREATE_ORDER':
        const created = await firstValueFrom(
          this.http.post<Order>('/api/orders', item.payload)
        );
        // Replace temp ID with real ID in local cache
        await this.orderDataService.replaceTempOrder(item.tempId, created);
        break;
      // ... other actions
    }
  }
}
```

**Closing:**
Offline-first is not optional for mobile apps — users expect the app to work in elevators, subways, and rural areas. Signal-based store for reactive UI, local SQLite/Preferences for persistence, and a sync queue for deferred writes. Always show the user what's pending sync and handle conflicts gracefully.

---

## Q6. How do you implement push notifications in an Ionic/Capacitor app?

**Summary:**
I use Capacitor's `@capacitor/push-notifications` plugin for native push and Firebase Cloud Messaging (FCM) as the delivery service. The flow: app registers for push → gets a device token → sends token to backend → backend sends push via FCM → native OS delivers notification → app handles tap to navigate.

**Implementation:**

```typescript
// push-notification.service.ts
@Injectable({ providedIn: 'root' })
export class PushNotificationService {

  private router = inject(Router);
  private http = inject(HttpClient);
  private platform = inject(Platform);

  async initialize(): Promise<void> {
    if (!Capacitor.isNativePlatform()) {
      console.log('Push notifications not available on web');
      return;
    }

    // Request permission
    const permission = await PushNotifications.requestPermissions();
    if (permission.receive !== 'granted') {
      console.warn('Push notification permission denied');
      return;
    }

    // Register with APNs (iOS) / FCM (Android)
    await PushNotifications.register();

    // Listen for registration success — get the device token
    PushNotifications.addListener('registration', async (token) => {
      console.log('Push token:', token.value);
      // Send token to your backend
      await firstValueFrom(
        this.http.post('/api/devices/register', {
          token: token.value,
          platform: Capacitor.getPlatform(),  // 'ios' or 'android'
          appVersion: (await App.getInfo()).version
        })
      );
    });

    // Registration failed
    PushNotifications.addListener('registrationError', (error) => {
      console.error('Push registration failed:', error);
    });

    // Notification received while app is in FOREGROUND
    PushNotifications.addListener('pushNotificationReceived', (notification) => {
      console.log('Foreground notification:', notification);
      // Show an in-app toast/banner (OS doesn't show notification when app is open)
      this.showInAppNotification(notification);
    });

    // User TAPPED on a notification (app was in background or killed)
    PushNotifications.addListener('pushNotificationActionPerformed', (action) => {
      console.log('Notification tapped:', action.notification.data);
      // Navigate to relevant page
      const data = action.notification.data;
      if (data.type === 'order_update') {
        this.router.navigate(['/orders', data.orderId]);
      } else if (data.type === 'chat_message') {
        this.router.navigate(['/chat', data.conversationId]);
      }
    });
  }

  private async showInAppNotification(notification: PushNotificationSchema) {
    const toast = await this.toastController.create({
      header: notification.title,
      message: notification.body,
      duration: 4000,
      position: 'top',
      buttons: [
        {
          text: 'View',
          handler: () => {
            // Handle navigation based on notification data
            this.handleNotificationTap(notification.data);
          }
        }
      ]
    });
    await toast.present();
  }
}

// Initialize in app.component.ts
export class AppComponent {
  private pushService = inject(PushNotificationService);

  constructor() {
    this.initializeApp();
  }

  async initializeApp() {
    await this.platform.ready();
    await this.pushService.initialize();
  }
}
```

**Android Setup (Firebase):**
```
1. Create Firebase project → add Android app
2. Download google-services.json → place in android/app/
3. android/build.gradle:
   classpath 'com.google.gms:google-services:4.3.15'
4. android/app/build.gradle:
   apply plugin: 'com.google.gms.google-services'
   implementation 'com.google.firebase:firebase-messaging:23.1.0'
```

**iOS Setup (APNs):**
```
1. Apple Developer → enable Push Notifications capability
2. Create APNs Authentication Key (.p8)
3. Upload .p8 to Firebase project settings → Cloud Messaging → iOS
4. Xcode → Signing & Capabilities → + Push Notifications
5. Xcode → Signing & Capabilities → + Background Modes → Remote notifications
```

**Closing:**
Push notification setup has three parts: native registration (Capacitor plugin), token management (send to backend), and tap handling (deep link to correct page). The trickiest part is handling foreground notifications — iOS/Android don't show system notifications when the app is open, so you need in-app UI.

---

## Q7. How do you optimize performance in an Ionic application? What are common performance pitfalls?

**Summary:**
Ionic apps run in a WebView — performance optimization means minimizing DOM, reducing change detection, lazy loading routes/components, optimizing images, and avoiding heavy operations on the main thread. The biggest pitfalls are large unvirtualized lists, excessive change detection, and heavy initial bundles.

**Top Performance Optimizations:**

**1. Virtual Scrolling for Large Lists:**
```html
<!-- ❌ SLOW: 1000 ion-items in DOM -->
<ion-list>
  @for (item of items; track item.id) {
    <ion-item>{{ item.name }}</ion-item>
  }
</ion-list>

<!-- ✅ FAST: Only visible items rendered (20-30 in DOM) -->
<ion-content>
  <ion-virtual-scroll [items]="items" approxItemHeight="48px">
    <ion-item *virtualItem="let item">
      <ion-label>{{ item.name }}</ion-label>
    </ion-item>
  </ion-virtual-scroll>
</ion-content>

<!-- ✅ ALSO: CDK Virtual Scroll (Angular CDK — more flexible) -->
<cdk-virtual-scroll-viewport itemSize="48" class="ion-content-scroll-host">
  <ion-item *cdkVirtualFor="let item of items">
    {{ item.name }}
  </ion-item>
</cdk-virtual-scroll-viewport>
```

**2. OnPush Change Detection on Every Page:**
```typescript
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ion-content>
      @for (order of orders(); track order.id) {
        <ion-card>{{ order.title }}</ion-card>
      }
    </ion-content>
  `
})
export class OrderListPage {
  orders = signal<Order[]>([]);
  // Signals + OnPush = minimal change detection
}
```

**3. Lazy Load Every Page and Heavy Component:**
```typescript
// Routes — lazy load all pages
{
  path: 'settings',
  loadComponent: () => import('./settings/settings.page').then(m => m.SettingsPage)
}

// Lazy load heavy modals
async openChart() {
  const { ChartModalComponent } = await import('./chart-modal/chart-modal.component');
  const modal = await this.modalCtrl.create({ component: ChartModalComponent });
  await modal.present();
}
```

**4. Image Optimization:**
```html
<!-- Lazy load images below the fold -->
<ion-img [src]="product.image" alt="Product image"></ion-img>
<!-- ion-img lazy loads by default — only loads when visible -->

<!-- For fixed-size images, always set dimensions -->
<ion-img [src]="product.image" style="width: 100px; height: 100px;"></ion-img>
<!-- Prevents layout shift (CLS) -->

<!-- Use WebP format with fallback -->
<picture>
  <source srcset="image.webp" type="image/webp" />
  <ion-img src="image.jpg"></ion-img>
</picture>
```

**5. Avoid Blocking the Main Thread:**
```typescript
// ❌ Heavy computation blocks UI — app freezes
processLargeDataset(data: any[]) {
  return data.map(item => expensiveTransform(item)); // blocks for 2 seconds
}

// ✅ Use Web Worker for heavy processing
async processLargeDataset(data: any[]) {
  const worker = new Worker(new URL('./data.worker', import.meta.url));
  return new Promise((resolve) => {
    worker.onmessage = ({ data }) => {
      resolve(data);
      worker.terminate();
    };
    worker.postMessage(data);
  });
}
```

**6. Reduce Bundle Size:**
```bash
# Analyze what's in your bundle
npx ionic build --prod --stats-json
npx webpack-bundle-analyzer www/stats.json

# Common fixes:
# - Replace moment.js (330KB) with date-fns (~10KB tree-shaken)
# - Import only needed Ionic components (standalone)
# - Lazy load all feature pages
# - Use Capacitor plugins instead of heavy JS polyfills
```

**Performance Checklist:**
```
□ OnPush + Signals on every page/component
□ Virtual scrolling for lists > 50 items
□ Lazy load every route and heavy modals
□ ion-img for all images (built-in lazy loading)
□ No function calls in templates — use pipes/computed
□ Web Workers for heavy data processing
□ Analyze bundle size — target < 500KB initial load
□ Preload critical routes, defer non-critical
□ Use trackBy / track in all @for loops
□ Minimize DOM nodes — max 1500 on screen
```

**Closing:**
Ionic performance is WebView performance. The same Angular optimizations apply (OnPush, lazy loading, virtual scroll) plus mobile-specific ones (image lazy loading, bundle size, avoiding main thread blocking). Profile with Chrome DevTools remote debugging — connect to the device and use the Performance tab.

---

## Q8. How do you handle app updates, versioning, and deployment for Ionic/Capacitor apps?

**Summary:**
Native app updates go through App Store / Play Store (review process, 1-3 days). For instant web-layer updates without store review, I use Capacitor Live Update (formerly AppFlow) or Capgo. Version management uses semantic versioning synced between `package.json`, `capacitor.config.ts`, and native projects. CI/CD automates builds with Fastlane or GitHub Actions.

**Update Strategies:**

```
NATIVE UPDATE (App Store / Play Store):
  → Required when: native code changes, plugin updates, permissions change
  → Process: Build → Submit → Review (1-3 days) → Release
  → User must update from store

LIVE UPDATE (OTA — Over The Air):
  → Works for: web-layer changes (HTML, CSS, JS, Angular code)
  → Process: Build web assets → Push to CDN → App downloads on launch
  → Instant — no store review, no user action needed
  → ⚠️ Cannot change native code, plugins, or permissions
```

**Version Management:**
```typescript
// capacitor.config.ts
const config: CapacitorConfig = {
  appId: 'com.company.myapp',
  appName: 'MyApp',
  webDir: 'dist',
  // Version is managed in native projects:
  // Android: android/app/build.gradle → versionCode + versionName
  // iOS: ios/App/App/Info.plist → CFBundleShortVersionString + CFBundleVersion
};
```

```bash
# Sync version across platforms
# package.json: "version": "2.5.0"

# Android: android/app/build.gradle
android {
  defaultConfig {
    versionCode 25      # Integer — must increment for every store upload
    versionName "2.5.0" # Display version
  }
}

# iOS: ios/App/App/Info.plist
# CFBundleShortVersionString: 2.5.0
# CFBundleVersion: 25

# Automate with a script:
npm version minor  # bumps package.json
# Then update native files (or use a plugin like standard-version)
```

**CI/CD Pipeline with GitHub Actions:**
```yaml
# .github/workflows/build.yml
name: Build and Deploy
on:
  push:
    branches: [main]

jobs:
  build-web:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '20' }
      - run: npm ci
      - run: npm run build -- --configuration production
      - run: npx cap sync

  build-android:
    needs: build-web
    runs-on: ubuntu-latest
    steps:
      - name: Set up JDK
        uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - name: Build APK
        run: cd android && ./gradlew assembleRelease
      - name: Sign APK
        uses: r0adkll/sign-android-release@v1
        with:
          releaseDirectory: android/app/build/outputs/apk/release
          signingKeyBase64: ${{ secrets.KEYSTORE_BASE64 }}
          alias: ${{ secrets.KEY_ALIAS }}
          keyStorePassword: ${{ secrets.KEYSTORE_PASSWORD }}
      - name: Upload to Play Store
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.GOOGLE_PLAY_SA }}
          packageName: com.company.myapp
          releaseFiles: android/app/build/outputs/apk/release/*.apk
          track: internal  # internal → beta → production

  build-ios:
    needs: build-web
    runs-on: macos-latest
    steps:
      - name: Build iOS
        run: cd ios/App && xcodebuild -workspace App.xcworkspace -scheme App -archivePath App.xcarchive archive
      - name: Export IPA
        run: xcodebuild -exportArchive -archivePath App.xcarchive -exportPath . -exportOptionsPlist ExportOptions.plist
      - name: Upload to App Store Connect
        uses: apple-actions/upload-testflight-build@v1
```

**Force Update Strategy (Critical Updates):**
```typescript
// Check minimum version on app start
@Injectable({ providedIn: 'root' })
export class VersionCheckService {

  async checkForUpdate(): Promise<void> {
    if (!Capacitor.isNativePlatform()) return;

    const appInfo = await App.getInfo();
    const currentVersion = appInfo.version;  // "2.5.0"

    const { minimumVersion } = await firstValueFrom(
      this.http.get<{ minimumVersion: string }>('/api/app/version')
    );

    if (this.isVersionLower(currentVersion, minimumVersion)) {
      // Force update — block app usage
      const alert = await this.alertCtrl.create({
        header: 'Update Required',
        message: 'Please update to the latest version to continue.',
        backdropDismiss: false,
        buttons: [{
          text: 'Update Now',
          handler: () => {
            // Open store listing
            if (this.platform.is('android')) {
              Browser.open({ url: 'https://play.google.com/store/apps/details?id=com.company.myapp' });
            } else {
              Browser.open({ url: 'https://apps.apple.com/app/idXXXXXXXXX' });
            }
          }
        }]
      });
      await alert.present();
    }
  }
}
```

**Closing:**
Two-track update strategy: store releases for native changes (1-3 day cycle), live updates for web-layer hotfixes (instant). CI/CD with GitHub Actions or Fastlane automates the build-sign-upload pipeline. Always implement a force-update mechanism for critical security fixes — you can't rely on users updating voluntarily.

---

## Q9. How do you handle secure authentication (biometrics, secure storage, token management) in Ionic/Capacitor?

**Summary:**
I store JWT tokens in Capacitor's encrypted Preferences (not localStorage — accessible to XSS), use the native biometric APIs (Face ID / Fingerprint) for re-authentication, and implement silent token refresh via HTTP interceptors. The auth flow: login → store tokens securely → attach token to API calls → biometric unlock for returning users → refresh token before expiry.

**Secure Token Storage:**

```typescript
// ❌ INSECURE: localStorage is accessible to XSS and WebView debugging
localStorage.setItem('auth_token', token);

// ✅ SECURE: Capacitor Preferences (encrypted on native)
import { Preferences } from '@capacitor/preferences';

@Injectable({ providedIn: 'root' })
export class SecureStorageService {

  async setToken(token: string): Promise<void> {
    await Preferences.set({ key: 'auth_token', value: token });
    // On iOS: stored in NSUserDefaults (encrypted at rest by iOS)
    // On Android: stored in SharedPreferences (encrypted with AndroidKeyStore)
  }

  async getToken(): Promise<string | null> {
    const { value } = await Preferences.get({ key: 'auth_token' });
    return value;
  }

  async clearTokens(): Promise<void> {
    await Preferences.remove({ key: 'auth_token' });
    await Preferences.remove({ key: 'refresh_token' });
  }
}

// For maximum security (banking apps), use @nicknisi/capacitor-secure-storage
// which uses iOS Keychain and Android EncryptedSharedPreferences
```

**Biometric Authentication:**

```typescript
import { BiometricAuth } from '@aparajita/capacitor-biometric-auth';

@Injectable({ providedIn: 'root' })
export class BiometricService {

  async isBiometricAvailable(): Promise<boolean> {
    if (!Capacitor.isNativePlatform()) return false;
    const result = await BiometricAuth.checkBiometry();
    return result.isAvailable;
    // Checks: Face ID, Touch ID, Fingerprint, Iris scan
  }

  async authenticate(reason: string): Promise<boolean> {
    try {
      await BiometricAuth.authenticate({
        reason: reason,  // "Verify your identity to view account"
        cancelTitle: 'Use Password',
        allowDeviceCredential: true,  // fallback to PIN/password
      });
      return true;
    } catch (e) {
      return false;  // User cancelled or biometric failed
    }
  }
}

// Usage — biometric login for returning users
export class LoginPage {
  async ionViewWillEnter() {
    const hasToken = await this.secureStorage.getToken();
    const biometricAvailable = await this.biometricService.isBiometricAvailable();

    if (hasToken && biometricAvailable) {
      const verified = await this.biometricService.authenticate(
        'Verify your identity to access your account'
      );
      if (verified) {
        this.router.navigate(['/home']);  // skip login form
      }
    }
  }
}
```

**Token Refresh Interceptor:**

```typescript
// HTTP interceptor for automatic token management
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const secureStorage = inject(SecureStorageService);
  const authService = inject(AuthService);
  const router = inject(Router);

  return from(secureStorage.getToken()).pipe(
    switchMap(token => {
      if (token) {
        req = req.clone({
          setHeaders: { Authorization: `Bearer ${token}` }
        });
      }
      return next(req);
    }),
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/auth/refresh')) {
        // Token expired — try refresh
        return from(authService.refreshTokens()).pipe(
          switchMap(newToken => {
            return next(req.clone({
              setHeaders: { Authorization: `Bearer ${newToken}` }
            }));
          }),
          catchError(() => {
            // Refresh failed — logout
            authService.logout();
            router.navigate(['/login']);
            return throwError(() => error);
          })
        );
      }
      return throwError(() => error);
    })
  );
};
```

**Closing:**
Mobile auth security: Capacitor Preferences for token storage (encrypted on device), biometric auth for returning users (skip login form), HTTP interceptor for automatic token attachment and refresh. Never use `localStorage` for tokens in a native app — use the platform's secure storage.

---

## Q10. How do you debug and test an Ionic/Capacitor application across platforms?

**Summary:**
I debug with Chrome DevTools remote debugging (Android), Safari Web Inspector (iOS), and Capacitor's `--livereload` for rapid iteration. For testing, I use Jasmine/Karma for unit tests, Cypress or Playwright for E2E tests on the web layer, and Appium or Detox for native-level testing. The key is testing most logic in the browser and only doing device-specific testing for native features.

**Debugging Workflow:**

```
BROWSER (fastest iteration):
  ionic serve
  → Full app in Chrome
  → Chrome DevTools: Elements, Console, Network, Performance
  → Test all web logic, UI, API calls
  → ⚠️ Native plugins won't work (camera, push, biometric)

ANDROID DEVICE (remote debugging):
  1. ionic build
  2. npx cap sync android
  3. npx cap run android --livereload --external
     → Deploys to device with live reload
     → Open chrome://inspect in desktop Chrome
     → See full DevTools for the WebView on device
     → Console logs, network, DOM inspection — everything

iOS DEVICE (Safari Web Inspector):
  1. ionic build
  2. npx cap sync ios
  3. npx cap run ios --livereload --external
     → Deploys to device/simulator
     → Safari → Develop menu → select device → inspect WebView
     → Full Safari DevTools on the device's WebView
```

**Live Reload Configuration:**
```typescript
// capacitor.config.ts — development only
const config: CapacitorConfig = {
  appId: 'com.company.myapp',
  appName: 'MyApp',
  webDir: 'dist',
  server: {
    // Enable for live reload during development
    url: 'http://192.168.1.100:8100',  // your machine's local IP
    cleartext: true  // allow HTTP (dev only — remove for prod!)
  }
};
```

```bash
# Or use the CLI flag (doesn't modify config file)
npx cap run android --livereload --external
# --external: binds to local IP so device can reach it
```

**Debugging Native Plugin Issues:**
```typescript
// Add verbose logging for native calls
import { Capacitor } from '@capacitor/core';

async takePhoto() {
  console.log('Platform:', Capacitor.getPlatform());
  console.log('isNative:', Capacitor.isNativePlatform());

  try {
    const image = await Camera.getPhoto({
      quality: 90,
      source: CameraSource.Camera,
      resultType: CameraResultType.Uri,
    });
    console.log('Photo URI:', image.webPath);
    this.photoUrl = image.webPath;
  } catch (error) {
    console.error('Camera error:', error);
    // Common errors:
    // "User cancelled photos app" — user dismissed camera
    // "Camera is not available" — no camera permission, or simulator
    // Check: android/app/src/main/AndroidManifest.xml for permissions
    // Check: ios/App/App/Info.plist for NSCameraUsageDescription
  }
}
```

**Android Native Logs (logcat):**
```bash
# View all logs from the app
adb logcat | grep -i "capacitor\|chromium\|myapp"

# Filter by tag
adb logcat Capacitor:V *:S
adb logcat CapacitorPlugins:V *:S

# Crash logs
adb logcat *:E | grep -i "fatal\|crash\|exception"
```

**iOS Native Logs:**
```bash
# Xcode → Window → Devices and Simulators → select device → Open Console
# Or use Console.app for simulator
# Filter by your app's bundle identifier
```

**Testing Strategy:**

```
                    Testing Pyramid
                   ┌──────────────┐
                   │   E2E Tests   │  ← Fewest (expensive, slow)
                   │   Cypress /   │     Test critical user flows
                   │   Playwright  │     on web layer
                   ├──────────────┤
                   │ Integration   │  ← Services + Components
                   │    Tests      │     Mock native plugins
                   │  TestBed +    │
                   │  HttpTestKit  │
                   ├──────────────┤
                   │  Unit Tests   │  ← Most tests (fast, cheap)
                   │  Jasmine/Jest │     Pure logic, pipes, utils
                   │  No DOM       │
                   └──────────────┘
```

```typescript
// Unit test — mock Capacitor plugin
describe('PushNotificationService', () => {
  let service: PushNotificationService;

  beforeEach(() => {
    // Mock Capacitor plugin
    spyOn(PushNotifications, 'requestPermissions')
      .and.returnValue(Promise.resolve({ receive: 'granted' }));
    spyOn(PushNotifications, 'register')
      .and.returnValue(Promise.resolve());
    spyOn(PushNotifications, 'addListener')
      .and.callFake((event, callback) => {
        if (event === 'registration') {
          callback({ value: 'mock-fcm-token-123' });
        }
        return Promise.resolve({ remove: () => {} });
      });

    TestBed.configureTestingModule({
      providers: [
        PushNotificationService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(PushNotificationService);
  });

  it('should register and send token to backend', async () => {
    const httpMock = TestBed.inject(HttpTestingController);

    await service.initialize();

    const req = httpMock.expectOne('/api/devices/register');
    expect(req.request.body.token).toBe('mock-fcm-token-123');
    req.flush({ success: true });
  });
});

// E2E test with Cypress (web layer)
describe('Login Flow', () => {
  it('should login and redirect to home', () => {
    cy.visit('/login');
    cy.get('[data-testid="email-input"]').type('user@example.com');
    cy.get('[data-testid="password-input"]').type('password123');
    cy.get('[data-testid="login-button"]').click();
    cy.url().should('include', '/home');
    cy.get('[data-testid="welcome-message"]').should('contain', 'Welcome');
  });
});
```

**Debugging Checklist:**
```
□ Browser first — ionic serve for web logic
□ Remote debug Android — chrome://inspect
□ Remote debug iOS — Safari → Develop → device
□ Live reload — npx cap run android --livereload --external
□ Native logs — adb logcat (Android), Xcode Console (iOS)
□ Network inspection — Chrome DevTools Network tab via remote debug
□ Permissions — check AndroidManifest.xml and Info.plist
□ Plugin issues — check npx cap doctor for configuration problems
□ Build issues — npx cap sync after every ionic build
```

**Closing:**
Debug in the browser for 80% of the work (fastest iteration), then device-test native features with live reload and remote debugging. Test with the pyramid: unit tests for logic, integration tests for services, E2E for critical flows. Mock Capacitor plugins in tests — you don't need a real camera to test your photo upload service.

---

## Quick Reference Summary

| # | Topic | Key Takeaway |
|---|-------|-------------|
| 1 | Cordova vs Capacitor | Capacitor: native projects in git, modern API, any framework, actively maintained |
| 2 | Custom Plugins | TS interface → native Swift/Kotlin → `registerPlugin` + `@PluginMethod` |
| 3 | Navigation & Lifecycle | `ionViewWillEnter` for data refresh (not `ngOnInit`), `ion-router-outlet` caches pages |
| 4 | Platform Detection | `Capacitor.isNativePlatform()` for native vs web, `Platform.is()` for specific platforms |
| 5 | Offline & Sync | Signal store + SQLite/Preferences + sync queue, process queue when online |
| 6 | Push Notifications | `@capacitor/push-notifications` + FCM, handle foreground + tap separately |
| 7 | Performance | OnPush + Signals, virtual scroll, lazy load, `ion-img`, Web Workers |
| 8 | Updates & Deployment | Store releases for native, live updates for web layer, force-update for critical |
| 9 | Secure Auth | Capacitor Preferences (encrypted), biometrics for re-auth, interceptor for token refresh |
| 10 | Debugging & Testing | Browser first, remote debug device, mock native plugins, test pyramid |