# WebHub — Native Android Web Workspace Browser

A feature-rich, tab-based web workspace **Android application** built with **Kotlin**, **Jetpack Compose**, **Room**, **Hilt**, and **Material You**. WebHub brings a desktop-class browsing experience to Android with multi-tab management, workspaces, privacy controls, session persistence, and more.

![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-BOM_2025.02-4285F4?logo=jetpackcompose)
![Android](https://img.shields.io/badge/Android-minSdk_29-3DDC84?logo=android)
![Room](https://img.shields.io/badge/Room-2.7-FF6F00?logo=sqlite)
![Hilt](https://img.shields.io/badge/Hilt-2.53-E34F26?logo=gradle)
![Material 3](https://img.shields.io/badge/Material_3-You-2196F3?logo=materialdesignicons)
![License](https://img.shields.io/badge/License-MIT-green)

---

## Overview

WebHub is a **native Android browser** that organizes your browsing into multiple named **workspaces**, each containing its own set of persistent **tabs**. Each tab is a real Android `WebView` with its own state, settings, and lifecycle.

The project was originally prototyped as a Next.js web application (preserved in `web-prototype/` for reference), and has been fully converted to a native Android implementation.

---

## Features

### Tab Management
- **Real Android WebView per tab** — each tab gets its own `WebView` instance with isolated state
- **Multi-tab browsing** with a scrollable tab strip featuring favicons, titles, and close buttons
- **Tab hibernation** — actually destroys the WebView to free memory, recreates on restore with saved state
- **Per-tab settings** — toggle JavaScript, ad blocking, apply custom CSS overrides and user scripts
- **Custom tab names and icons** for personal organization
- **Recently closed tabs** history (limited to 20) with one-tap restore
- **Quick Switcher** — floating search overlay to fuzzy-search and switch between tabs
- **Tab overview grid** — visual card grid of all open tabs with thumbnails
- **Drag-and-drop tab reordering** with touch
- **Duplicate, move, and rename tabs**

### Workspaces
- **Multiple named workspaces** to separate browsing contexts (Work, Personal, Finance, Research, etc.)
- **Workspace switcher** bottom sheet with create, rename, delete, and set-default actions
- **Per-workspace theme** support (light, dark, or system)
- **Isolated tab groups** — closing a workspace cascades and removes all its tabs
- **Guard against deleting the last workspace**
- **Workspace rail** on tablet/foldable screens (adaptive layout)

### Privacy & Security
- **Incognito mode** per tab with minimized persistent storage and cache clearing
- **Cookie isolation** strategy for incognito tabs
- **Ad and tracker blocking** via host-based `shouldInterceptRequest()` with bundled blocklist
- **JavaScript control** — disable JS on a per-tab basis
- **Custom CSS overrides** — inject user-defined styles into any page
- **User scripts** — attach JavaScript to run on specific tabs
- **HTTPS enforcement** — rejects invalid SSL certificates by default, no silent bypass
- **Secure WebView defaults** — file access disabled, Safe Browsing enabled, mixed content blocked
- **Android Keystore-backed** secure storage for sensitive data
- **Biometric authentication** via `BiometricPrompt` (fingerprint, face unlock, device credential)

### Bookmarks
- **Room-backed global bookmarks** with one-click save from any tab
- **Swipe-to-delete** with undo snackbar
- **Search and filter** bookmarks
- **Open in new tab** directly from bookmarks

### Session & Persistence
- **Room over SQLite** — all tabs, workspaces, bookmarks, and settings survive app restarts and process death
- **Proto DataStore** for app configuration (theme, search engine, biometric, etc.)
- **Named session snapshots** — save/restore complete browsing sessions as JSON
- **Automatic session restoration** on app relaunch
- **Session import/export** support

### Android Integration
- **Android Share Target** — receive URLs from other apps via share sheet
- **Home screen widgets** via Jetpack Glance
- **Dynamic shortcuts** via `ShortcutManagerCompat` for workspace/tab shortcuts
- **Picture-in-Picture** support for video content
- **Predictive back** API support

### UI / UX
- **Material You / Material 3** with dynamic color support (Android 12+)
- **Adaptive layout** — phone (bottom sheets), tablet (navigation rail), foldable (multi-pane)
- **Dark / Light / System theme** with proper Compose theming
- **3-step onboarding wizard** (theme, search engine, workspace name)
- **Omnibox** with URL detection, search integration, SSL indicator, and loading progress
- **Fullscreen video** handling via `WebChromeClient` custom view
- **Accessibility** — content descriptions, semantic traversal, TalkBack support, 48dp touch targets

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

- **MVVM** with **unidirectional data flow**
- **Repository pattern** — ViewModels never access Room or DataStore directly
- **Use cases** encapsulate business logic
- **Hilt** for dependency injection
- **Coroutines + Flow** for async/reactive data
- **WebViewManager** for tab-to-WebView lifecycle (creation, destruction, hibernation, LRU eviction)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository + UseCase |
| DI | Hilt 2.53 |
| Database | Room 2.7 over SQLite |
| Preferences | Proto DataStore |
| Browser Engine | Android WebView (per tab) |
| Navigation | Navigation Compose |
| State | StateFlow + MutableStateFlow |
| Async | Kotlin Coroutines + Flow |
| Theming | Material You (dynamic color) |
| Images | Coil Compose |
| Widgets | Jetpack Glance |
| Security | BiometricPrompt, Android Keystore |
| Build | Gradle 8.11 + KSP + AGP 8.9 |

---

## Android Targets

| Property | Value |
|----------|-------|
| minSdk | 29 (Android 10) |
| targetSdk | 36 |
| compileSdk | 36 |
| Release ABI | arm64-v8a |
| Distribution | Android App Bundle (.aab) |

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
│   │   │   ├── WebHubDatabase.kt     # Room database definition
│   │   │   ├── entity/               # TabEntity, WorkspaceEntity, BookmarkEntity,
│   │   │   │                         # ClosedTabHistoryEntity, SessionSnapshotEntity
│   │   │   ├── dao/                  # TabDao, WorkspaceDao, BookmarkDao,
│   │   │   │                         # ClosedTabHistoryDao, SessionSnapshotDao
│   │   │   └── converter/            # Room TypeConverters
│   │   ├── datastore/                # (Proto DataStore serializers)
│   │   └── repository/               # Repository implementations
│   ├── domain/
│   │   ├── model/                    # Tab, Workspace, Bookmark, ClosedTab,
│   │   │                             # SessionSnapshot, AppSettings
│   │   ├── repository/               # Repository interfaces
│   │   └── usecase/                  # 20 use cases organized by feature
│   │       ├── tab/                  # Add, Close, Switch, Duplicate, Hibernate,
│   │       │                         # Restore, Update, Move, Reorder
│   │       ├── workspace/            # Create, Delete, Rename, Switch, SetDefault
│   │       ├── bookmark/             # Add, Remove, IsBookmarked
│   │       └── session/              # Save, Restore, AutoRestore
│   ├── ui/
│   │   ├── theme/                    # Material 3 theme, colors, typography
│   │   ├── navigation/               # NavHost, route definitions
│   │   ├── main/                     # MainScreen + MainViewModel (central orchestrator)
│   │   ├── browser/                  # BrowserViewModel + PiP Activity
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
│   │   ├── WebHubWebViewClient.kt    # Page lifecycle, ad blocking, favicon extraction
│   │   ├── WebHubChromeClient.kt     # Progress, fullscreen video, permissions
│   │   └── AdBlocker.kt             # Host-based ad/tracker blocker
│   ├── security/
│   │   ├── BiometricAuthManager.kt  # BiometricPrompt wrapper
│   │   └── SslErrorHandler.kt       # SSL error handling (secure defaults)
│   └── util/
│       ├── UrlNormalizer.kt          # URL normalization, domain extraction
│       └── SearchEngineHelper.kt     # Search engine presets
├── app/src/main/res/
│   ├── values/                       # colors.xml, strings.xml, themes.xml
│   ├── xml/                          # widget info
│   └── assets/                       # adblock_hosts.txt
├── build.gradle.kts                  # Root build config
├── app/build.gradle.kts              # App module (all dependencies)
├── settings.gradle.kts               # Plugin repositories + modules
└── gradle.properties                 # JVM args, AndroidX flags
```

---

## Database Schema (Room)

5 Room entities + 1 DataStore for settings:

| Entity | Purpose |
|--------|---------|
| `TabEntity` | Browser tabs with URL, title, privacy flags, hibernation, per-tab settings |
| `WorkspaceEntity` | Named workspace containers with theme/accent/settings |
| `BookmarkEntity` | Saved URLs with titles and favicons |
| `ClosedTabHistoryEntity` | Recently closed tabs for undo-restore (capped at 20) |
| `SessionSnapshotEntity` | Serialized session JSON for save/restore |
| `AppSettings` (DataStore) | Global config: theme, search engine, biometric, ad block, onboarding |

---

## Getting Started

### Prerequisites

- **Android Studio Hedgehog** (2023.1.1) or newer
- **JDK 17**
- **Android SDK** with API level 36
- An Android emulator or physical device (API 29+)

### Setup

```bash
git clone https://github.com/Pratham2511/Webhub.git
cd Webhub/android
```

### Generate Gradle Wrapper

```bash
# If gradlew is a placeholder, regenerate it:
gradle wrapper --gradle-version 8.11.1
```

### Open in Android Studio

1. Open Android Studio
2. Select **Open an existing project**
3. Navigate to the `android/` directory
4. Wait for Gradle sync to complete

### Build & Run

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Or use Android Studio's **Run** button.

### Generate Release AAB

```bash
./gradlew bundleRelease
```

The output will be at `app/build/outputs/bundle/release/app-release.aab`.

---

## WebView Browser Engine

The core of WebHub is its `WebViewManager` which manages a pool of real Android `WebView` instances:

- **One WebView per active tab** — not shared, not iframes
- **LRU eviction** — when memory is low or max WebViews exceeded, least-recently-used tabs are hibernated
- **Real hibernation** — WebView is destroyed and state is saved; recreated on restore
- **Per-tab configuration** — JavaScript, ad blocking, CSS overrides, and user scripts are applied per WebView
- **Secure defaults** — Safe Browsing enabled, mixed content blocked, file access disabled
- **Renderer crash recovery** — catches `onRenderProcessGone` and offers reload
- **Favicon extraction** — uses JavaScript to extract `<link rel="icon">` from loaded pages
- **External intent handling** — `tel:`, `mailto:`, and `intent:` URLs are routed to system apps
- **Fullscreen video** — `WebChromeClient` custom view with PiP support
- **Memory trim response** — responds to Android `onTrimMemory` callbacks

---

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| Native Android (not PWA/Capacitor/RN) | Real WebView lifecycle control, proper Android integration |
| One WebView per tab | True tab isolation, independent back/forward history |
| Room over SQLite | Type-safe queries, Flow integration, migration support |
| DataStore for settings | Type-safe, coroutine-native, replaces SharedPreferences |
| Hilt + KSP | Compile-time DI, faster than kapt |
| Host-based ad blocking | No JavaScript injection needed, works at network level |
| Material You dynamic color | Follows Android 12+ theming conventions |
| Predictive back API | Future-proof navigation handling |

---

## What's Been Implemented

- [x] Native Kotlin Android project with Jetpack Compose + Material 3
- [x] Real Android WebView browser engine (one per tab)
- [x] WebViewManager with lifecycle, LRU eviction, and memory management
- [x] Multi-tab browsing with tab strip (favicons, titles, close, incognito indicator)
- [x] Workspace management (create, rename, delete, switch, set default)
- [x] Tab hibernation with actual WebView destruction and recreation
- [x] Per-tab settings (JavaScript, ad block, CSS override, user script, incognito)
- [x] Omnibox with URL detection, search, SSL indicator, loading progress
- [x] Tab overview grid with search and tab cards
- [x] Quick switcher overlay (floating search)
- [x] Workspace switcher bottom sheet
- [x] Add tab sheet with URL validation, workspace selection, incognito toggle
- [x] Global bookmarks with swipe-to-delete and undo
- [x] Recently closed tabs panel (limited to 20 entries)
- [x] Tab settings sheet (per-tab configuration)
- [x] Room persistence (5 entities, 5 DAOs with Flow queries)
- [x] DataStore configuration (theme, search engine, biometric, ad block)
- [x] 3-step onboarding wizard (theme, search engine, workspace)
- [x] Settings screen (appearance, browser, privacy, sessions, about)
- [x] Host-based ad and tracker blocking (bundled blocklist)
- [x] HTTPS security (reject invalid certificates by default)
- [x] Biometric authentication (BiometricPrompt + Keystore)
- [x] Android share target (receive URLs from other apps)
- [x] Home screen widget (Jetpack Glance)
- [x] Dynamic shortcuts (ShortcutManagerCompat)
- [x] Picture-in-Picture support for video content
- [x] Adaptive layout (phone bottom sheets, tablet rail, foldable multi-pane)
- [x] Dark / Light / System theme with Material You dynamic color
- [x] MVVM architecture with UseCases, Repositories, Hilt DI
- [x] Session save/restore (named snapshots + auto-restore)
- [x] Back press handling (predictive back: dismiss UI → WebView back → close tab)
- [x] Accessibility (content descriptions, 48dp touch targets, semantic order)
- [x] ProGuard rules for release builds

---

## License

MIT

---

Built with Kotlin, Jetpack Compose, and Material 3 by [Pratham Pansare](https://github.com/Pratham2511).
