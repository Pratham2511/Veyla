# WebHub — Web Workspace Browser

A feature-rich, tab-based web workspace application built as a web prototype using **Next.js 16**, **TypeScript**, **Prisma ORM**, **Zustand**, and **shadcn/ui**. WebHub brings a desktop-class browsing experience with multi-tab management, workspaces, privacy controls, and session persistence — inspired by Android’s WebHub concept.

![Next.js](https://img.shields.io/badge/Next.js-16-black?logo=next.js)
![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?logo=typescript)
![Prisma](https://img.shields.io/badge/Prisma-6-2D3748?logo=prisma)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4-06B6D4?logo=tailwindcss)
![License](https://img.shields.io/badge/License-MIT-green)

---

## Features

### Tab Management
- **Multi-tab browsing** with an interactive tab strip featuring favicons, titles, and close buttons
- **Drag-and-drop tab reordering** powered by `@dnd-kit`
- **Tab hibernation** — freeze inactive tabs to save memory; restore with a single click
- **Per-tab settings** — toggle JavaScript, ad blocking, apply custom CSS overrides and user scripts
- **Custom tab names and icons** for personal organization
- **Recently closed tabs** history with one-tap restore
- **Quick Switcher** (Ctrl+K / ⌘K) — fuzzy-search across all open tabs and switch instantly

### Workspaces
- **Multiple named workspaces** to separate browsing contexts (e.g., Work, Personal, Research)
- **Workspace switcher** sidebar with create, rename, delete, and set-default actions
- **Per-workspace theme** support (light, dark, or system)
- **Isolated tab groups** — closing a workspace cascades and removes all its tabs

### Privacy & Security
- **Incognito mode** per tab — opt individual tabs into private browsing
- **Ad blocking** toggle globally and per-tab
- **JavaScript control** — disable JS on a per-tab basis when needed
- **Custom CSS overrides** — inject user-defined styles into any page
- **User scripts** — attach JavaScript to run on specific tabs

### Bookmarks
- **Global bookmarks** with one-click save from any tab
- **Quick-access bookmark panel** with favicons and domain labels
- **Open bookmark in new tab** or navigate directly

### Session & Persistence
- **SQLite-backed persistence** via Prisma ORM — all tabs, workspaces, bookmarks, and settings survive page reloads
- **Session snapshots** — save and restore complete browsing sessions
- **Onboarding wizard** for first-time setup (search engine, theme, default workspace)

### UI / UX
- **Adaptive layout** — compact, medium, and expanded breakpoints for different screen sizes
- **Dark / Light / System theme** via `next-themes`
- **Smooth animations** with `framer-motion` (tab transitions, sheet slides, tab strip animations)
- **shadcn/ui component library** (Dialog, Sheet, ContextMenu, AlertDialog, ScrollArea, Tooltip, and more)
- **Omnibox bar** with URL detection, search integration, and visual state feedback
- **Tab overview grid** for a visual birds-eye view of all open tabs
- **Right-click context menus** on tabs for quick actions

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Next.js 16 (Turbopack) |
| Language | TypeScript 5 |
| Styling | Tailwind CSS 4 + shadcn/ui |
| State | Zustand (50+ actions) |
| Database | SQLite via Prisma ORM |
| Drag & Drop | @dnd-kit/core + @dnd-kit/sortable |
| Animations | Framer Motion |
| Theming | next-themes (dark/light/system) |
| Icons | Lucide React |
| UI Primitives | Radix UI |

## Project Structure

```
├── prisma/
│   └── schema.prisma          # 6-table DB schema
├── src/
│   ├── app/
│   │   ├── api/webhub/route.ts  # Unified REST API (CRUD)
│   │   ├── globals.css
│   │   ├── layout.tsx
│   │   └── page.tsx             # Main app shell + keyboard shortcuts
│   ├── components/
│   │   ├── ui/                  # shadcn/ui primitives
│   │   └── webhub/              # App-specific components
│   │       ├── AddTabSheet.tsx
│   │       ├── BookmarksScreen.tsx
│   │       ├── OmniboxBar.tsx
│   │       ├── OnboardingScreen.tsx
│   │       ├── QuickSwitcher.tsx
│   │       ├── RecentlyClosedSheet.tsx
│   │       ├── SettingsScreen.tsx
│   │       ├── TabOverviewScreen.tsx
│   │       ├── TabSettingsSheet.tsx
│   │       ├── TabStrip.tsx
│   │       ├── WebViewContainer.tsx
│   │       └── WorkspaceSwitcher.tsx
│   ├── hooks/
│   └── lib/
│       ├── db.ts                # Prisma client singleton
│       ├── store.ts             # Zustand store (all state + actions)
│       ├── types.ts             # TypeScript types + URL utilities
│       └── utils.ts             # cn() helper
├── public/
└── package.json
```

## Database Schema

6 SQLite tables managed by Prisma:

| Model | Purpose |
|-------|---------|
| `Workspace` | Named workspace containers with theme/accent settings |
| `Tab` | Individual browser tabs with URL, title, privacy flags, hibernation state |
| `Bookmark` | Saved URLs with titles and favicons |
| `ClosedTabHistory` | Recently closed tabs for undo-restore |
| `SessionSnapshot` | Serialized session state for save/restore |
| `AppSettings` | Global app configuration (theme, search engine, onboarding state) |

## Getting Started

### Prerequisites

- **Node.js** 18+ or **Bun**
- **npm**, **yarn**, or **bun** package manager

### Installation

```bash
git clone https://github.com/Pratham2511/Webhub.git
cd Webhub
npm install
```

### Setup Database

```bash
npx prisma db push
npx prisma generate
```

### Run Development Server

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

### Build for Production

```bash
npm run build
npm start
```

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl + K` / `⌘ + K` | Open Quick Switcher |
| `Enter` (in Quick Switcher) | Switch to selected tab |
| `↑` / `↓` (in Quick Switcher) | Navigate tab list |
| `Escape` | Close dialogs / switcher |

## API

A unified REST API at `/api/webhub` handles all CRUD operations:

- `GET` — Fetch tabs, workspaces, bookmarks, settings, closed tabs, sessions
- `POST` — Create tabs, workspaces, bookmarks, session snapshots
- `PUT` — Update tabs, workspaces, settings, restore tabs/sessions
- `DELETE` — Remove tabs, workspaces, bookmarks

## What's Been Built So Far

- Full onboarding wizard with search engine selection and theme picker
- Multi-tab browsing with iframe-based web content loading
- Drag-and-drop tab reordering in the tab strip
- Workspace management (create, rename, delete, switch, set default)
- Quick Switcher with keyboard navigation (Ctrl+K)
- Tab hibernation and restore with loading indicators
- Per-tab settings (JS toggle, ad block, custom CSS, user scripts, incognito)
- Global bookmarks system with save/remove/open
- Recently closed tabs panel with restore capability
- Tab overview grid for visual tab management
- App settings screen (theme, search engine, ad block defaults)
- Right-click context menus on tabs
- Persistent storage with SQLite + Prisma
- Light/dark/system theme support
- Responsive adaptive layout (compact/medium/expanded)
- Smooth Framer Motion animations throughout

## License

MIT

---

Built with Next.js, Tailwind CSS, and shadcn/ui by [Pratham Pansare](https://github.com/Pratham2511).
