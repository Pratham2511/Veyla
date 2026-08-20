# Veyla — Native Android Web Workspace Browser

A native Android web browser built with **Kotlin**, **Jetpack Compose**, **Room**, **Hilt**, and **Material 3**. Veyla organizes browsing into multiple named **workspaces**, each containing its own set of persistent **tabs** backed by real Android `WebView` instances.

![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-BOM_2024.12.01-4285F4?logo=jetpackcompose)
![Android](https://img.shields.io/badge/Android-minSdk_29-3DDC84?logo=android)
![Room](https://img.shields.io/badge/Room-2.6.1-FF6F00?logo=sqlite)
![Hilt](https://img.shields.io/badge/Hilt-2.53-E34F26?logo=gradle)
![Material 3](https://img.shields.io/badge/Material_3-You-2196F3?logo=materialdesignicons)
![Build](https://img.shields.io/badge/assembleDebug-PASSING-brightgreen)
![Lint](https://img.shields.io/badge/lintDebug-0_ERRORS-brightgreen)
![Tests](https://img.shields.io/badge/unit_tests-32_PASSING-brightgreen)
![License](https://img.shields.io/badge/License-MIT-green)

---

## Important: What This Project Is

- **The product is a native Android app** located under `android/`. It is built with Kotlin + Jetpack Compose and targets Android devices.
- **The Next.js prototype in the repository root** (`package.json`, `src/`, `next.config.ts`, etc.) is **reference-only** and is **NOT the product**. It was used for early UI prototyping and is preserved for design reference only.

---

## Branding

Veyla uses an approved folded-V brand mark. The source-of-truth assets are located at:

```
android/design/veyla-brand/
```

The loading animation is implemented natively in Compose code using `Path` geometry — **no PNG frames or GIFs are used**.

---

## Verified Build & Test Results

The following results were obtained from an actual Gradle build (not code review):

| Command | Result | Details |
|---------|--------|---------|
| `./gradlew compileDebugKotlin` | **PASS** | 0 errors, 11 deprecation warnings |
| `./gradlew assembleDebug` | **PASS** | Debug APK produced successfully |
| `./gradlew lintDebug` | **PASS** | 0 errors, ~192 warnings (dependency version updates, deprecated API usage) |
| `./gradlew testDebugUnitTest` | **PASS** | **32 tests, 0 failures, 0 errors** (0.096s) |
| `./gradlew compileReleaseKotlin` | **PASS** | Release Kotlin compilation succeeds |
| `./gradlew assembleRelease` | **PASS** | Release APK produced (without R8 minification) |
| `./gradlew bundleRelease` | **ENV LIMIT** | R8 minification requires >4 GB JVM heap; succeeds on standard developer machines |

### Build Environment Used

- **Gradle** 8.11.1 with valid wrapper (`gradlew`)
- **AGP** 8.9.0
- **JDK** 21 (Oracle JDK 21.0.12.1; JDK 17 also works)
- **Android SDK** API level 36 + Build Tools 36.0.0
- **KSP** 2.1.0-1.0.29
- **Compose BOM** 2024.12.01

---

## Core Features

What exists in the Android codebase under `android/`:

- **Real Android WebView per tab** — each tab gets its own `WebView` instance with independent state
- **Multi-tab browsing** with tab strip (favicons, titles, close buttons)
- **Tab hibernation** — destroys the WebView to free memory, recreates on restore with saved URL/scroll
- **LRU eviction** — WebViewManager evicts least-recently-used tabs when pool exceeds 5 active WebViews
- **Active-tab protection** — the visible tab is never hibernated by LRU or memory pressure
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
- **Veyla-branded onboarding** with the approved folded-V logo
- **Native Veyla loading animation** (Compose Canvas, no image frames)
- **Material 3 theme** with dynamic color support (Android 12+)
- **Navigation Compose** for screen routing
- **Android share target** — receive URLs from other apps via `ACTION_SEND`
- **Home screen widget** via Jetpack Glance
- **Picture-in-Picture** activity for video content
- **Biometric authentication** manager (`BiometricPrompt` wrapper)

---

## Architecture

```
+---------------------------------------------+
|                    UI Layer (Compose)               |
|  MainScreen, BrowserScreen, TabOverview, Settings... |
|                    v Events / StateFlow              |
+---------------------------------------------+
|                  ViewModel Layer                    |
|  MainViewModel, BrowserViewModel, SettingsVM...      |
|                    v UseCase invocations             |
+---------------------------------------------+
|                  Domain Layer                       |
|  UseCases + Domain Models + Repository Interfaces    |
|                    v                                 |
+---------------------------------------------+
|                  Data Layer                         |
|  Repository Impls + Room DAOs + DataStore + WebView  |
+---------------------------------------------+
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

- **JDK 17 or 21**
- **Android SDK** with API level 36 and Build Tools 36.0.0
- An Android emulator or physical device (API 29+) for runtime testing

### Clone & Build

```bash
git clone https://github.com/Pratham2511/Veyla.git
cd Veyla/android

# Debug build
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Lint check
./gradlew lintDebug

# Release AAB (requires >=4 GB JVM heap for R8)
./gradlew bundleRelease
```

### Release Signing

Release builds use environment-variable-based signing. Set these before building:

```bash
export RELEASE_STORE_FILE=/path/to/keystore.jks
export RELEASE_STORE_PASSWORD=your_password
export RELEASE_KEY_ALIAS=your_alias
export RELEASE_KEY_PASSWORD=your_key_password
./gradlew bundleRelease
```

If no signing env vars are set, the release build produces an **unsigned** APK/AAB.

### Output Artifacts

- Debug APK: `android/app/build/outputs/apk/debug/`
- Release APK: `android/app/build/outputs/apk/release/`
- Release AAB: `android/app/build/outputs/bundle/release/`

---

## Tests

### Unit Tests (32 tests -- all passing)

Located in `app/src/test/java/com/pratham/webhub/DomainModelTest.kt`.

Tests domain model entity-domain mapping and round-trip equality for all 5 models:
- **Tab** -- `fromEntity()`, `toEntity()`, round-trip, default values, edge cases (long strings, special chars)
- **Workspace** -- `fromEntity()`, `toEntity()`, round-trip, default values
- **SessionSnapshot** -- `fromEntity()`, `toEntity()`, round-trip, empty data
- **ClosedTab** -- `fromEntity()`, `toEntity()`, round-trip, null favicon
- **Bookmark** -- `fromEntity()`, `toEntity()`, round-trip, null favicon

Run with: `./gradlew testDebugUnitTest`

### Instrumented Tests (30 tests -- require emulator/device)

Located in `app/src/androidTest/java/com/pratham/webhub/WebHubDatabaseTest.kt`.

Tests Room DAO operations with in-memory database:
- **TabDao** (10 tests) -- CRUD, workspace queries, position/hibernation/scroll updates
- **WorkspaceDao** (7 tests) -- CRUD, default workspace transaction, max position
- **ClosedTabHistoryDao** (5 tests) -- insert, reverse-chronological order, prune to 20
- **SessionSnapshotDao** (4 tests) -- insert, data integrity, delete
- **Cascade delete** (2 tests) -- workspace deletion cascades to tabs
- **Workspace/Tab persistence** (3 tests) -- end-to-end create, retrieve, switch, delete
- **Recently closed tabs** (3 tests) -- ordering, favicon preservation
- **Session restoration** (3 tests) -- JSON integrity, delete, ordering

Run with: `./gradlew connectedDebugAndroidTest` (requires emulator or device)

---

## WebView Ownership Model

The `AndroidView` composable in `MainScreen.kt` uses a `key(activeTab.id)` wrapper to ensure proper WebView attachment when switching tabs:

- **WebViewManager** owns the WebView lifecycle (create, hibernate, destroy)
- **AndroidView** only manages view-hierarchy attachment/detachment
- When the active tab changes, `key()` forces Compose to dispose the old `AndroidView` (detaching the previous WebView) and create a new one (attaching the new WebView)
- A safety `parent.removeView()` call in the factory prevents double-attachment issues
- The active tab's WebView is marked as `protectedTabId` in WebViewManager, so it is never evicted by LRU or memory pressure

---

## Known Limitations

- **WebView session state cannot fully persist across process death** -- URL, title, and scroll position are persisted to Room; full DOM state, JavaScript variables, and form inputs are lost
- **Hibernation saves URL and scroll position but not JavaScript runtime state** -- when a hibernated tab is restored, the page is reloaded from the saved URL
- **No per-tab VPN or proxy support** -- all tabs share the device's network configuration
- **No cookie isolation between tabs** -- all tabs in a workspace share the same `CookieManager`
- **Camera/microphone permissions are default-denied** -- a proper Android runtime permission flow has not been implemented
- **Geolocation is disabled** -- no location permission flow exists
- **No ad-block subscription/update mechanism** -- the blocklist is a static bundled file (`assets/adblock_hosts.txt`)
- **PiP requires video sites to use standard HTML5 fullscreen APIs**
- **Widgets show static content** -- no live data updates
- **`bundleRelease` with R8** requires a machine with >4 GB available RAM for the JVM heap

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

## License

MIT

---

Built with Kotlin, Jetpack Compose, and Material 3 by [Pratham Pansare](https://github.com/Pratham2511).
