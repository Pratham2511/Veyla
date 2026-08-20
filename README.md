# WebHub — Native Android Web Workspace Browser

A native Android web browser built with **Kotlin**, **Jetpack Compose**, **Room**, **Hilt**, and **Material 3**. WebHub organizes browsing into multiple named **workspaces**, each containing its own set of persistent **tabs** backed by real Android `WebView` instances.

![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-BOM_2025.02-4285F4?logo=jetpackcompose)
![Android](https://img.shields.io/badge/Android-minSdk_29-3DDC84?logo=android)
![Room](https://img.shields.io/badge/Room-2.6.1-FF6F00?logo=sqlite)
![Hilt](https://img.shields.io/badge/Hilt-2.53-E34F26?logo=gradle)
![Material 3](https://img.shields.io/badge/Material_3-You-2196F3?logo=materialdesignicons)
![License](https://img.shields.io/badge/License-MIT-green)

---

## Important: What This Project Is

- **The product is a native Android app** located under `android/`. It is built with Kotlin + Jetpack Compose and targets Android devices.
- **The Next.js prototype in the repository root** (`package.json`, `src/`, `next.config.ts`, etc.) is **reference-only** and is **NOT the product**. It was used for early UI prototyping and is preserved for design reference only.

---

## Incognito Mode

**Incognito mode is NOT part of the current WebHub product scope.** While the domain model includes an `isIncognito` flag on tabs and some UI scaffolding references it, there is no functioning incognito mode — no incognito `WebView` configuration, no isolated cookie/profile handling, and no incognito entry point in the UI. This should not be considered a supported feature.

---

## Core Features

What exists in the Android codebase under `android/`:

- **Real Android WebView per tab** — each tab gets its own `WebView` instance with independent state
- **Multi-tab browsing** with tab strip (favicons, titles, close buttons)
- **Tab hibernation** — destroys the WebView to free memory, recreates on restore with saved URL/scroll
- **Per-tab settings** — toggle JavaScript, ad blocking per tab
- **Tab overview grid** — visual card grid of all open tabs
- **Quick switcher overlay** — floating search to find and switch between tabs
- **Multiple named workspaces** — create, rename, delete, switch, set default
- **Workspace switcher** bottom sheet
- **Add tab sheet** with URL validation and workspace selection
- **Global bookmarks** — save, delete, search, open in new tab
- **Recently closed tabs** panel (capped at 20 entries)
- **Tab settings sheet** — per-tab configuration
- **Omnibox** with URL detection, search integration, SSL indicator, loading progress
- **Host-based ad/tracker blocking** via `shouldInterceptRequest()` with bundled blocklist
- **URL normalization** and search engine integration
- **Session save/restore** — named snapshots as JSON serialization
- **3-step onboarding wizard** (theme, search engine, workspace name)
- **Settings screen** (appearance, browser, privacy, sessions, about)
- **Material 3 theme** with dynamic color support (Android 12+)
- **Navigation Compose** for screen routing
- **Android share target** — receive URLs from other apps via `ACTION_SEND`
- **Home screen widget** via Jetpack Glance
- **Picture-in-Picture** activity for video content
- **Biometric authentication** manager (`BiometricPrompt` wrapper)

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                    UI Layer (Compose)               │
│  MainScreen, BrowserScreen, TabOverview, Settings... │
│                    ↕ Events / StateFlow              │
├─────────────────────────────────────────────────────┤
│                  ViewModel Layer                    │
│  MainViewModel, BrowserViewModel, SettingsVM...      │
│                    ↕ UseCase invocations             │
├─────────────────────────────────────────────────────┤
│                  Domain Layer                       │
│  UseCases + Domain Models + Repository Interfaces    │
│                    ↕                                 │
├─────────────────────────────────────────────────────┤
│                  Data Layer                         │
│  Repository Impls + Room DAOs + DataStore + WebView  │
└─────────────────────────────────────────────────────┘
```

- **MVVM** with unidirectional data flow (`StateFlow` / `MutableStateFlow`)
- **Repository pattern** — ViewModels never access Room or DataStore directly
- **Use cases** encapsulate business logic (20 total)
- **Hilt** for compile-time dependency injection
- **Room** over SQLite for structured persistence
- **DataStore (Preferences)** for app configuration
- **Coroutines + Flow** for all async/reactive data
- **WebViewManager** for tab-to-WebView lifecycle (creation, destruction, hibernation, LRU eviction)

---

## Android Targets

| Property | Value |
|----------|-------|
| minSdk | 29 (Android 10) |
| targetSdk | 36 |
| compileSdk | 36 |
| Release ABI | arm64-v8a only |
| Distribution | Android App Bundle (.aab) |

---

## Build Instructions

### Prerequisites

- **Android Studio Iguana** (2023.2.1) or newer
- **JDK 17**
- **Android SDK** with API level 36
- An Android emulator or physical device (API 29+)

### Clone & Open

```bash
git clone https://github.com/Pratham2511/Webhub.git
cd Webhub/android
```

Open the `android/` directory in Android Studio and wait for Gradle sync to complete.

### Build & Run via Gradle

```bash
# If gradlew needs to be regenerated:
gradle wrapper --gradle-version 8.11.1

# Debug build
./gradlew assembleDebug
./gradlew installDebug

# Release AAB
./gradlew bundleRelease
```

Output AAB at `android/app/build/outputs/bundle/release/app-release.aab`.

---

## Feature Verification Status

### Implemented and Code-Reviewed

The following exist as source code in `android/` and have been reviewed for correctness against Android APIs and architecture patterns. **No runtime verification has been performed** (see next section).

**Architecture layers:**
- All four layers: data, domain, UI, WebView engine
- Hilt DI modules: `DatabaseModule`, `DataStoreModule`, `RepositoryModule`

**Data layer:**
- Room database with 5 entities (`TabEntity`, `WorkspaceEntity`, `BookmarkEntity`, `ClosedTabHistoryEntity`, `SessionSnapshotEntity`)
- 5 DAOs with Flow-based reactive queries (`TabDao`, `WorkspaceDao`, `BookmarkDao`, `ClosedTabHistoryDao`, `SessionSnapshotDao`)
- DataStore (Preferences) for app settings

**Domain layer:**
- 20 UseCases: 9 tab (add, close, switch, duplicate, hibernate, restore, update, move, reorder), 5 workspace (create, delete, rename, switch, set default), 3 bookmark (add, remove, is bookmarked), 3 session (save, restore, auto-restore)
- 6 repository interfaces + 6 repository implementations
- Domain models: `Tab`, `Workspace`, `Bookmark`, `ClosedTab`, `SessionSnapshot`, `AppSettings`

**UI layer:**
- MVVM with `StateFlow`/`Flow` reactive state management
- Material 3 theme with dynamic color support
- Navigation Compose routing
- Onboarding wizard (3-step: theme, search engine, workspace name)
- Settings screen (appearance, browser, privacy, sessions, about)
- Bookmarks screen with search/filter
- Omnibox with SSL indicator and loading progress
- Tab overview grid
- Tab strip with favicons, titles, close buttons
- Quick switcher overlay
- Workspace switcher bottom sheet
- Add tab sheet
- Recently closed tabs sheet
- Tab settings sheet (per-tab configuration)

**WebView engine:**
- `WebViewManager` with LRU eviction and memory trim response
- Real Android WebView per tab (creation, destruction, hibernation lifecycle)
- `WebViewFactory` for WebView configuration
- `WebHubWebViewClient` for page lifecycle, favicon extraction, external intent handling
- `WebHubChromeClient` for progress, fullscreen video, permission requests
- `AdBlocker` with host-based `shouldInterceptRequest()` blocking
- `UrlNormalizer` for URL normalization and domain extraction
- `SearchEngineHelper` for search engine presets

**Android integrations (code exists):**
- Android share target (`ACTION_SEND` for receiving URLs)
- Home screen widget (Jetpack Glance `WebHubWidgetReceiver`)
- Picture-in-Picture activity (`PipActivity`)
- Biometric auth manager (`BiometricAuthManager`)
- Session save/restore (JSON serialization)

### Implemented but NOT Runtime Verified

**No Android SDK or emulator was available during development.** The following could not be verified:

- Could not run `assembleDebug`, `lint`, `test`, or `bundleRelease`
- Could not verify WebView browsing, page loading, tab switching, or any runtime behavior
- Could not verify tab persistence, session save/restore, or database operations at runtime
- Could not verify workspace switching, bookmark CRUD, or recently closed tabs
- Android integrations (share target, widget, PiP, biometric) exist in code but are **not runtime-tested**
- Ad blocking logic is code-reviewed but not verified against real ad networks
- All features listed above are **code-reviewed only** — they compile against the correct API surfaces but have not been executed on a device

### NOT Implemented / Post-MVP

The following are explicitly **not** part of the current codebase:

- **E2EE sync** across devices
- **Container profiles** (Firefox-like container isolation)
- **Proxy / Tor** support
- **Community templates** or template sharing
- **Monetization** (ads, IAP, subscriptions)
- **Per-tab camera/microphone permission flow** — currently default-deny, no proper Android permission request dialog
- **Geolocation support** — currently disabled; no location permission flow
- **Proper database migrations** — schema is v1 with `exportSchema = false` and `fallbackToDestructiveMigration`; no incremental migration path
- **Unit tests** — test dependencies are declared in `build.gradle.kts` but no `src/test/` or `src/androidTest/` source directories exist

---

## Known Limitations

- **WebView session state cannot fully persist across process death** — URL, title, and scroll position are persisted to Room; full DOM state, JavaScript variables, and form inputs are lost
- **Hibernation saves URL and scroll position but not JavaScript runtime state** — when a hibernated tab is restored, the page is reloaded from the saved URL
- **No per-tab VPN or proxy support** — all tabs share the device's network configuration
- **No cookie isolation between tabs** — all tabs in a workspace share the same `CookieManager`
- **Camera/microphone permissions are default-denied** — the code exists to deny these at the WebView level; a proper Android runtime permission flow has not been implemented
- **Geolocation is disabled** — no location permission flow exists
- **No ad-block subscription/update mechanism** — the blocklist is a static bundled file (`assets/adblock_hosts.txt`) with no way to update it at runtime
- **PiP requires video sites to use standard HTML5 fullscreen APIs** — sites using custom video players may not trigger PiP
- **Widgets show static content** — no live data updates; future work to add real-time tab/workspace info

---

## Security

| Measure | Status |
|---------|--------|
| SSL errors | Rejected by default (no silent bypass) |
| File access | Disabled (`setAllowFileAccess(false)`) |
| Safe Browsing | Enabled (`setSafeBrowsingEnabled(true)`) |
| Mixed content | Blocked (`setMixedContentMode(MIXED_CONTENT_NEVER_ALLOW)`) |
| JavaScript bridge | No unsafe `addJavascriptInterface` exposed to page content |
| Intent: URLs | Validated against safe-package whitelist before dispatch |
| Permissions (camera, mic, location) | Default-denied at WebView level |
| Cleartext traffic | Disabled (`usesCleartextTraffic=false`) |

---

## Build & Test Commands

```bash
cd android

# Debug build
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests (none written yet)
./gradlew test

# Run instrumented tests (none written yet)
./gradlew connectedAndroidTest

# Lint check
./gradlew lint

# Release AAB (requires signing config)
./gradlew bundleRelease

# Clean build
./gradlew clean assembleDebug
```

---

## Project Structure

```
android/
├── app/src/main/java/com/pratham/webhub/
│   ├── WebHubApplication.kt          # @HiltAndroidApp Application
│   ├── MainActivity.kt               # Single-activity, share intent handler
│   ├── di/                           # Hilt DI modules
│   │   ├── DatabaseModule.kt         # Room database + DAOs
│   │   ├── DataStoreModule.kt        # Preferences DataStore
│   │   └── RepositoryModule.kt       # Interface → Impl bindings
│   ├── data/
│   │   ├── db/
│   │   │   ├── WebHubDatabase.kt     # Room database (v1, 5 entities)
│   │   │   ├── entity/               # TabEntity, WorkspaceEntity, BookmarkEntity,
│   │   │   │                         # ClosedTabHistoryEntity, SessionSnapshotEntity
│   │   │   ├── dao/                  # TabDao, WorkspaceDao, BookmarkDao,
│   │   │   │                         # ClosedTabHistoryDao, SessionSnapshotDao
│   │   │   └── converter/            # Room TypeConverters
│   │   └── repository/               # 6 repository implementations
│   ├── domain/
│   │   ├── model/                    # Tab, Workspace, Bookmark, ClosedTab,
│   │   │                             # SessionSnapshot, AppSettings
│   │   ├── repository/               # 6 repository interfaces
│   │   └── usecase/                  # 20 use cases organized by feature
│   │       ├── tab/                  # Add, Close, Switch, Duplicate, Hibernate,
│   │       │                         # Restore, Update, Move, Reorder
│   │       ├── workspace/            # Create, Delete, Rename, Switch, SetDefault
│   │       ├── bookmark/             # Add, Remove, IsBookmarked
│   │       └── session/              # Save, Restore, AutoRestore
│   ├── ui/
│   │   ├── theme/                    # Material 3 theme, colors, typography
│   │   ├── navigation/               # NavHost, route definitions
│   │   ├── main/                     # MainScreen + MainViewModel
│   │   ├── browser/                  # BrowserViewModel + PipActivity
│   │   ├── overview/                 # Tab overview grid + ViewModel
│   │   ├── workspace/                # Workspace switcher sheet + ViewModel
│   │   ├── addtab/                   # Add tab sheet + ViewModel
│   │   ├── bookmarks/                # Bookmarks screen + ViewModel
│   │   ├── settings/                 # Settings screen + ViewModel
│   │   ├── onboarding/               # 3-step onboarding + ViewModel
│   │   ├── components/               # Omnibox, TabStrip, QuickSwitcherOverlay,
│   │   │                             # TabSettingsSheet, RecentlyClosedSheet
│   │   └── widget/                   # Glance home screen widget
│   ├── webview/                      # Browser engine
│   │   ├── WebViewManager.kt         # Tab-to-WebView lifecycle, LRU eviction
│   │   ├── WebViewFactory.kt         # WebView creation + configuration
│   │   ├── WebHubWebViewClient.kt    # Page lifecycle, ad blocking, favicon
│   │   ├── WebHubChromeClient.kt     # Progress, fullscreen video, permissions
│   │   └── AdBlocker.kt             # Host-based ad/tracker blocker
│   ├── security/
│   │   ├── BiometricAuthManager.kt  # BiometricPrompt wrapper
│   │   └── SslErrorHandler.kt       # SSL error handling
│   └── util/
│       ├── UrlNormalizer.kt          # URL normalization, domain extraction
│       └── SearchEngineHelper.kt     # Search engine presets
├── app/src/main/res/
│   ├── values/                       # colors.xml, strings.xml, themes.xml
│   ├── xml/                          # widget info
│   └── assets/                       # adblock_hosts.txt
├── app/build.gradle.kts              # App module (all dependencies)
├── build.gradle.kts                  # Root build config (plugin versions)
├── settings.gradle.kts               # Plugin repositories + modules
└── gradle.properties                 # JVM args, AndroidX flags

# Next.js prototype (reference only — NOT the product)
├── src/                              # React/Next.js prototype source
├── package.json                      # Node.js dependencies for prototype
├── next.config.ts                    # Next.js configuration
└── ...
```

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Kotlin | 2.1.0 |
| UI | Jetpack Compose + Material 3 | BOM 2025.02.00 |
| Architecture | MVVM + Repository + UseCase | — |
| DI | Hilt | 2.53.1 |
| Database | Room over SQLite | 2.6.1 |
| Preferences | DataStore (Preferences) | 1.1.2 |
| Browser Engine | Android WebView (per tab) | — |
| Navigation | Navigation Compose | 2.8.5 |
| State | StateFlow + MutableStateFlow | — |
| Async | Kotlin Coroutines + Flow | 1.9.0 |
| Theming | Material You (dynamic color) | — |
| Images | Coil Compose | 2.7.0 |
| Widgets | Jetpack Glance | 1.1.1 |
| Security | BiometricPrompt | 1.1.0 |
| Permissions | Accompanist Permissions | 0.37.0 |
| Build | Gradle | 8.11.1 |
| Build | AGP | 8.9.0 |
| Build | KSP | 2.1.0-1.0.29 |
| JDK | Java | 17 |

---

## Database Schema (Room)

5 Room entities + 1 Preferences DataStore for settings:

| Entity | Purpose |
|--------|---------|
| `TabEntity` | Browser tabs with URL, title, position, workspace ID, hibernation state, per-tab settings |
| `WorkspaceEntity` | Named workspace containers with theme and settings |
| `BookmarkEntity` | Saved URLs with titles |
| `ClosedTabHistoryEntity` | Recently closed tabs for undo-restore (capped at 20) |
| `SessionSnapshotEntity` | Serialized session JSON for save/restore |
| App settings (DataStore) | Global config: theme, search engine, biometric, ad block, onboarding complete |

**Note:** Database is at schema version 1. `exportSchema` is `false`. Destructive migration is used as fallback — there is no incremental migration path for future schema changes.

---

## License

MIT

---

Built with Kotlin, Jetpack Compose, and Material 3 by [Pratham Pansare](https://github.com/Pratham2511).
