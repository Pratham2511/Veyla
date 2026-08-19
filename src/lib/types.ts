export interface Tab {
  id: string;
  workspaceId: string;
  url: string;
  title: string;
  faviconUrl?: string;
  customName?: string;
  customIconUri?: string;
  isIncognito: boolean;
  isJsEnabled: boolean;
  isAdBlockEnabled: boolean;
  cssOverride?: string;
  userScript?: string;
  position: number;
  isHibernated: boolean;
  savedScrollY: number;
  createdAt: string;
  updatedAt: string;
}

export interface Workspace {
  id: string;
  name: string;
  themeMode: string;
  accentColor?: string;
  position: number;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Bookmark {
  id: string;
  url: string;
  title: string;
  faviconUrl?: string;
  createdAt: string;
}

export interface ClosedTab {
  id: string;
  tabId: string;
  url: string;
  title: string;
  faviconUrl?: string;
  closedAt: string;
}

export interface SessionSnapshot {
  id: string;
  name: string;
  data: string;
  createdAt: string;
}

export interface AppSettings {
  id: string;
  activeWorkspaceId?: string;
  themeMode: string;
  isBiometricEnabled: boolean;
  adBlockEnabled: boolean;
  searchEngineUrl: string;
  onboardingDone: boolean;
  updatedAt: string;
}

export type ThemeMode = 'light' | 'dark' | 'system';
export type ScreenView = 'main' | 'overview' | 'settings' | 'bookmarks' | 'onboarding' | 'sessions';

export function getDomainFromUrl(url: string): string {
  try {
    const u = new URL(url.startsWith('http') ? url : `https://${url}`);
    return u.hostname.replace('www.', '');
  } catch {
    return url;
  }
}

export function isValidUrl(str: string): boolean {
  try {
    const u = new URL(str.startsWith('http') ? str : `https://${str}`);
    return u.hostname.includes('.');
  } catch {
    return false;
  }
}

export function normalizeUrl(input: string): string {
  if (input.startsWith('http://') || input.startsWith('https://')) return input;
  if (input.includes('.') && !input.includes(' ')) return `https://${input}`;
  return '';
}

export function getFaviconUrl(url: string): string {
  try {
    const u = new URL(url);
    return `https://www.google.com/s2/favicons?domain=${u.hostname}&sz=64`;
  } catch {
    return '';
  }
}
