'use client';

import { create } from 'zustand';
import type { Tab, Workspace, Bookmark, ClosedTab, SessionSnapshot, AppSettings, ThemeMode, ScreenView } from './types';

type LayoutMode = 'compact' | 'medium' | 'expanded';

interface WebHubState {
  // Data
  workspaces: Workspace[];
  tabs: Tab[];
  bookmarks: Bookmark[];
  closedTabs: ClosedTab[];
  sessions: SessionSnapshot[];
  settings: AppSettings | null;

  // UI state
  activeTabId: string | null;
  activeWorkspaceId: string | null;
  currentView: ScreenView;
  loading: boolean;
  loaded: boolean;
  layoutMode: LayoutMode;
  showWorkspaceSwitcher: boolean;
  showAddTabSheet: boolean;
  showRecentlyClosed: boolean;
  showQuickSwitcher: boolean;
  showTabSettings: string | null; // tab id
  showSaveSessionDialog: boolean;
  iframeLoadingTabId: string | null;
  searchQuery: string;

  // Actions - data
  loadData: () => Promise<void>;
  setWorkspaces: (w: Workspace[]) => void;
  setTabs: (t: Tab[]) => void;
  setBookmarks: (b: Bookmark[]) => void;
  setClosedTabs: (c: ClosedTab[]) => void;
  setSessions: (s: SessionSnapshot[]) => void;
  setSettings: (s: AppSettings) => void;

  // Actions - tabs
  selectTab: (id: string) => void;
  addTab: (url: string, workspaceId?: string, isIncognito?: boolean) => Promise<void>;
  closeTab: (id: string) => Promise<void>;
  updateTab: (id: string, data: Partial<Tab>) => Promise<void>;
  duplicateTab: (id: string) => Promise<void>;
  moveTabToWorkspace: (tabId: string, workspaceId: string) => Promise<void>;
  hibernateTab: (id: string) => void;
  restoreTab: (id: string) => void;
  reopenClosedTab: (closedTab: ClosedTab) => Promise<void>;
  reorderTabs: (tabIds: string[]) => Promise<void>;

  // Actions - workspaces
  selectWorkspace: (id: string) => void;
  addWorkspace: (name: string) => Promise<void>;
  renameWorkspace: (id: string, name: string) => Promise<void>;
  deleteWorkspace: (id: string) => Promise<void>;
  setDefaultWorkspace: (id: string) => Promise<void>;
  reorderWorkspaces: (ids: string[]) => Promise<void>;

  // Actions - bookmarks
  addBookmark: (url: string, title: string, faviconUrl?: string) => Promise<void>;
  removeBookmark: (id: string) => Promise<void>;

  // Actions - sessions
  saveSession: (name: string) => Promise<void>;
  restoreSession: (sessionId: string) => Promise<void>;
  deleteSession: (id: string) => Promise<void>;

  // Actions - settings
  updateSettings: (data: Partial<AppSettings>) => Promise<void>;

  // Actions - UI
  setCurrentView: (v: ScreenView) => void;
  setLayoutMode: (m: LayoutMode) => void;
  setShowWorkspaceSwitcher: (v: boolean) => void;
  setShowAddTabSheet: (v: boolean) => void;
  setShowRecentlyClosed: (v: boolean) => void;
  setShowQuickSwitcher: (v: boolean) => void;
  setShowTabSettings: (id: string | null) => void;
  setShowSaveSessionDialog: (v: boolean) => void;
  setIframeLoadingTabId: (id: string | null) => void;
  setSearchQuery: (q: string) => void;
}

export const useWebHubStore = create<WebHubState>((set, get) => ({
  workspaces: [],
  tabs: [],
  bookmarks: [],
  closedTabs: [],
  sessions: [],
  settings: null,
  activeTabId: null,
  activeWorkspaceId: null,
  currentView: 'main',
  loading: true,
  loaded: false,
  layoutMode: 'compact',
  showWorkspaceSwitcher: false,
  showAddTabSheet: false,
  showRecentlyClosed: false,
  showQuickSwitcher: false,
  showTabSettings: null,
  showSaveSessionDialog: false,
  iframeLoadingTabId: null,
  searchQuery: '',

  loadData: async () => {
    try {
      const res = await fetch('/api/webhub');
      if (!res.ok) throw new Error('Failed to load');
      const data = await res.json();
      const wss = data.workspaces as Workspace[];
      const tabs = data.tabs as Tab[];
      const settings = data.settings as AppSettings | null;

      // Ensure at least one workspace exists
      let activeWsId = settings?.activeWorkspaceId || wss[0]?.id;
      if (wss.length === 0) {
        const res2 = await fetch('/api/webhub', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ action: 'createWorkspace', name: 'Personal', isDefault: true, position: 0 }),
        });
        const newWs = await res2.json() as Workspace;
        activeWsId = newWs.id;
        set({ workspaces: [newWs] });
      } else {
        set({ workspaces: wss });
      }

      // Init settings if needed
      if (!settings) {
        const res3 = await fetch('/api/webhub', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ action: 'initSettings', activeWorkspaceId: activeWsId }),
        });
        const newSettings = await res3.json() as AppSettings;
        set({ settings: newSettings });
      } else {
        set({ settings });
      }

      const wsTabs = tabs.filter(t => t.workspaceId === activeWsId);
      const activeTab = wsTabs.length > 0 ? wsTabs[0].id : null;

      set({
        tabs,
        bookmarks: data.bookmarks || [],
        closedTabs: data.closedTabs || [],
        sessions: data.sessions || [],
        activeWorkspaceId: activeWsId,
        activeTabId: activeTab,
        currentView: settings?.onboardingDone ? 'main' : 'onboarding',
        loading: false,
        loaded: true,
      });
    } catch (error) {
      console.error('Failed to load data:', error);
      set({ loading: false, loaded: true });
    }
  },

  setWorkspaces: (w) => set({ workspaces: w }),
  setTabs: (t) => set({ tabs: t }),
  setBookmarks: (b) => set({ bookmarks: b }),
  setClosedTabs: (c) => set({ closedTabs: c }),
  setSessions: (s) => set({ sessions: s }),
  setSettings: (s) => set({ settings: s }),

  selectTab: (id) => set({ activeTabId: id }),

  addTab: async (url, workspaceId, isIncognito = false) => {
    const state = get();
    const wsId = workspaceId || state.activeWorkspaceId;
    if (!wsId) return;
    const wsTabs = state.tabs.filter(t => t.workspaceId === wsId);
    const tab = {
      workspaceId: wsId,
      url,
      title: url,
      faviconUrl: `https://www.google.com/s2/favicons?domain=${new URL(url.startsWith('http') ? url : `https://${url}`).hostname}&sz=64`,
      isIncognito,
      position: wsTabs.length,
    };
    const res = await fetch('/api/webhub', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'createTab', ...tab }),
    });
    const newTab = await res.json() as Tab;
    set({ tabs: [...state.tabs, newTab], activeTabId: newTab.id, showAddTabSheet: false });
  },

  closeTab: async (id) => {
    const state = get();
    const tab = state.tabs.find(t => t.id === id);
    if (!tab) return;
    // Add to closed tabs
    await fetch('/api/webhub', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'addClosedTab', tabId: tab.id, url: tab.url, title: tab.title, faviconUrl: tab.faviconUrl }),
    });
    await fetch(`/api/webhub?action=deleteTab&id=${id}`, { method: 'DELETE' });
    const newTabs = state.tabs.filter(t => t.id !== id);
    const wsTabs = newTabs.filter(t => t.workspaceId === state.activeWorkspaceId);
    const newActiveTab = state.activeTabId === id ? (wsTabs.length > 0 ? wsTabs[0].id : null) : state.activeTabId;
    set({ tabs: newTabs, activeTabId: newActiveTab });
  },

  updateTab: async (id, data) => {
    const res = await fetch('/api/webhub', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'updateTab', id, ...data }),
    });
    const updated = await res.json() as Tab;
    const state = get();
    set({ tabs: state.tabs.map(t => t.id === id ? updated : t) });
  },

  duplicateTab: async (id) => {
    const state = get();
    const tab = state.tabs.find(t => t.id === id);
    if (!tab) return;
    await get().addTab(tab.url, tab.workspaceId, tab.isIncognito);
  },

  moveTabToWorkspace: async (tabId, workspaceId) => {
    const state = get();
    const tab = state.tabs.find(t => t.id === tabId);
    if (!tab) return;
    const wsTabs = state.tabs.filter(t => t.workspaceId === workspaceId);
    await get().updateTab(tabId, { workspaceId, position: wsTabs.length });
    if (tab.workspaceId !== workspaceId && state.activeTabId === tabId) {
      const oldWsTabs = state.tabs.filter(t => t.workspaceId === tab.workspaceId && t.id !== tabId);
      set({ activeTabId: oldWsTabs[0]?.id || null });
    }
  },

  hibernateTab: (id) => {
    const state = get();
    set({ tabs: state.tabs.map(t => t.id === id ? { ...t, isHibernated: true } : t) });
  },

  restoreTab: (id) => {
    const state = get();
    set({ tabs: state.tabs.map(t => t.id === id ? { ...t, isHibernated: false } : t) });
  },

  reopenClosedTab: async (closedTab) => {
    await get().addTab(closedTab.url);
    await fetch(`/api/webhub?action=deleteClosedTab&id=${closedTab.id}`, { method: 'DELETE' });
    const state = get();
    set({ closedTabs: state.closedTabs.filter(c => c.id !== closedTab.id) });
  },

  reorderTabs: async (tabIds) => {
    await fetch('/api/webhub', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'reorderTabs', tabIds }),
    });
    const state = get();
    const reordered = tabIds.map((id, idx) => {
      const tab = state.tabs.find(t => t.id === id);
      return tab ? { ...tab, position: idx } : null;
    }).filter(Boolean) as Tab[];
    const others = state.tabs.filter(t => !tabIds.includes(t.id));
    set({ tabs: [...reordered, ...others] });
  },

  selectWorkspace: (id) => {
    const state = get();
    const wsTabs = state.tabs.filter(t => t.workspaceId === id);
    set({
      activeWorkspaceId: id,
      activeTabId: wsTabs[0]?.id || null,
      showWorkspaceSwitcher: false,
    });
    get().updateSettings({ activeWorkspaceId: id });
  },

  addWorkspace: async (name) => {
    const state = get();
    const res = await fetch('/api/webhub', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'createWorkspace', name, position: state.workspaces.length }),
    });
    const ws = await res.json() as Workspace;
    set({ workspaces: [...state.workspaces, ws] });
  },

  renameWorkspace: async (id, name) => {
    const res = await fetch('/api/webhub', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'updateWorkspace', id, name }),
    });
    const ws = await res.json() as Workspace;
    const state = get();
    set({ workspaces: state.workspaces.map(w => w.id === id ? ws : w) });
  },

  deleteWorkspace: async (id) => {
    if (get().workspaces.length <= 1) return; // Keep at least one workspace
    await fetch(`/api/webhub?action=deleteWorkspace&id=${id}`, { method: 'DELETE' });
    const state = get();
    const newWorkspaces = state.workspaces.filter(w => w.id !== id);
    const newTabs = state.tabs.filter(t => t.workspaceId !== id);
    const newActiveWs = newWorkspaces[0]?.id || null;
    const wsTabs = newTabs.filter(t => t.workspaceId === newActiveWs);
    set({
      workspaces: newWorkspaces,
      tabs: newTabs,
      activeWorkspaceId: newActiveWs,
      activeTabId: wsTabs[0]?.id || null,
    });
  },

  setDefaultWorkspace: async (id) => {
    const state = get();
    // Unset all defaults first
    for (const ws of state.workspaces) {
      if (ws.isDefault) {
        await fetch('/api/webhub', {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ action: 'updateWorkspace', id: ws.id, isDefault: false }),
        });
      }
    }
    await fetch('/api/webhub', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'updateWorkspace', id, isDefault: true }),
    });
    set({ workspaces: state.workspaces.map(w => ({ ...w, isDefault: w.id === id })) });
  },

  reorderWorkspaces: async (ids) => {
    await fetch('/api/webhub', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'reorderWorkspaces', workspaceIds: ids }),
    });
    const state = get();
    const reordered = ids.map((id, idx) => {
      const ws = state.workspaces.find(w => w.id === id);
      return ws ? { ...ws, position: idx } : null;
    }).filter(Boolean) as Workspace[];
    set({ workspaces: reordered });
  },

  addBookmark: async (url, title, faviconUrl) => {
    const res = await fetch('/api/webhub', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'createBookmark', url, title, faviconUrl }),
    });
    const bm = await res.json() as Bookmark;
    const state = get();
    set({ bookmarks: [bm, ...state.bookmarks] });
  },

  removeBookmark: async (id) => {
    await fetch(`/api/webhub?action=deleteBookmark&id=${id}`, { method: 'DELETE' });
    const state = get();
    set({ bookmarks: state.bookmarks.filter(b => b.id !== id) });
  },

  saveSession: async (name) => {
    const state = get();
    const data = JSON.stringify({ tabs: state.tabs, workspaces: state.workspaces });
    const res = await fetch('/api/webhub', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'saveSession', name, data }),
    });
    const session = await res.json() as SessionSnapshot;
    set({ sessions: [session, ...state.sessions], showSaveSessionDialog: false });
  },

  restoreSession: async (sessionId) => {
    const state = get();
    const session = state.sessions.find(s => s.id === sessionId);
    if (!session) return;
    try {
      const parsed = JSON.parse(session.data);
      // Restore tabs and workspaces to DB (simplified - just reload)
      await get().loadData();
      set({ currentView: 'main' });
    } catch (e) {
      console.error('Failed to restore session:', e);
    }
  },

  deleteSession: async (id) => {
    await fetch(`/api/webhub?action=deleteSession&id=${id}`, { method: 'DELETE' });
    const state = get();
    set({ sessions: state.sessions.filter(s => s.id !== id) });
  },

  updateSettings: async (data) => {
    const res = await fetch('/api/webhub', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'updateSettings', ...data }),
    });
    const updated = await res.json() as AppSettings;
    set({ settings: updated });
  },

  setCurrentView: (v) => set({ currentView: v }),
  setLayoutMode: (m) => set({ layoutMode: m }),
  setShowWorkspaceSwitcher: (v) => set({ showWorkspaceSwitcher: v }),
  setShowAddTabSheet: (v) => set({ showAddTabSheet: v }),
  setShowRecentlyClosed: (v) => set({ showRecentlyClosed: v }),
  setShowQuickSwitcher: (v) => set({ showQuickSwitcher: v }),
  setShowTabSettings: (id) => set({ showTabSettings: id }),
  setShowSaveSessionDialog: (v) => set({ showSaveSessionDialog: v }),
  setIframeLoadingTabId: (id) => set({ iframeLoadingTabId: id }),
  setSearchQuery: (q) => set({ searchQuery: q }),
}));
