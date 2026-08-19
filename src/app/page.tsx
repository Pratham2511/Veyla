'use client';

import { useEffect, useState } from 'react';
import { ThemeProvider } from 'next-themes';
import { TooltipProvider } from '@/components/ui/tooltip';
import { Button } from '@/components/ui/button';
import { useWebHubStore } from '@/lib/store';

import { OmniboxBar } from '@/components/webhub/OmniboxBar';
import { TabStrip } from '@/components/webhub/TabStrip';
import { WebViewContainer } from '@/components/webhub/WebViewContainer';
import { WorkspaceSwitcher } from '@/components/webhub/WorkspaceSwitcher';
import { AddTabSheet } from '@/components/webhub/AddTabSheet';
import { TabOverviewScreen } from '@/components/webhub/TabOverviewScreen';
import { SettingsScreen } from '@/components/webhub/SettingsScreen';
import { BookmarksScreen } from '@/components/webhub/BookmarksScreen';
import { OnboardingScreen } from '@/components/webhub/OnboardingScreen';
import { QuickSwitcher } from '@/components/webhub/QuickSwitcher';
import { RecentlyClosedSheet } from '@/components/webhub/RecentlyClosedSheet';
import { TabSettingsSheet } from '@/components/webhub/TabSettingsSheet';
import {
  LayoutGrid,
  Settings,
  Bookmark,
  Plus,
  Grid2X2,
  PanelLeft,
} from 'lucide-react';
import { cn } from '@/lib/utils';

type LayoutMode = 'compact' | 'medium' | 'expanded';

function useLayoutMode() {
  const [mode, setMode] = useState<LayoutMode>('compact');
  useEffect(() => {
    const update = () => {
      const w = window.innerWidth;
      if (w >= 1280) setMode('expanded');
      else if (w >= 768) setMode('medium');
      else setMode('compact');
    };
    update();
    window.addEventListener('resize', update);
    return () => window.removeEventListener('resize', update);
  }, []);
  return mode;
}

function WebHubApp() {
  const {
    loadData,
    loading,
    loaded,
    currentView,
    setCurrentView,
    activeWorkspaceId,
    workspaces,
    tabs,
    setShowWorkspaceSwitcher,
    setShowAddTabSheet,
    setShowQuickSwitcher,
    setShowSaveSessionDialog,
  } = useWebHubStore();

  const layoutMode = useLayoutMode();
  const activeWorkspace = workspaces.find(w => w.id === activeWorkspaceId);
  const wsTabCount = tabs.filter(t => t.workspaceId === activeWorkspaceId).length;

  useEffect(() => {
    if (!loaded) loadData();
  }, [loaded, loadData]);

  // Keyboard shortcut for quick switcher (Ctrl/Cmd + K)
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        setShowQuickSwitcher(true);
      }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [setShowQuickSwitcher]);

  if (loading || !loaded) {
    return (
      <div className="h-screen flex items-center justify-center bg-background">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-2 border-primary border-t-transparent rounded-full animate-spin" />
          <p className="text-sm text-muted-foreground">Loading WebHub...</p>
        </div>
      </div>
    );
  }

  if (currentView === 'onboarding') {
    return <OnboardingScreen />;
  }

  // Full-screen views
  if (currentView === 'overview') return <TabOverviewScreen />;
  if (currentView === 'settings') return <SettingsScreen />;
  if (currentView === 'bookmarks') return <BookmarksScreen />;

  return (
    <div className="h-screen flex flex-col bg-background overflow-hidden">
      {/* Top Bar */}
      <header className="flex items-center gap-2 px-2 py-1.5 border-b shrink-0 bg-background">
        {/* Workspace selector button */}
        <Button
          variant="ghost"
          size="sm"
          className="gap-1.5 h-8 px-2 shrink-0"
          onClick={() => setShowWorkspaceSwitcher(true)}
        >
          <PanelLeft className="h-4 w-4" />
          <span className="hidden sm:inline text-sm font-medium max-w-[120px] truncate">
            {activeWorkspace?.name || 'Workspace'}
          </span>
          <span className="hidden sm:inline text-xs text-muted-foreground">({wsTabCount})</span>
        </Button>

        {/* Omnibox */}
        <OmniboxBar />

        {/* Right actions */}
        <div className="flex items-center gap-0.5 shrink-0">
          <Button
            variant={currentView === 'overview' ? 'secondary' : 'ghost'}
            size="icon"
            className="h-8 w-8"
            onClick={() => setCurrentView('overview')}
            title="Tab overview"
          >
            <Grid2X2 className="h-4 w-4" />
          </Button>
          <Button
            variant={currentView === 'bookmarks' ? 'secondary' : 'ghost'}
            size="icon"
            className="h-8 w-8"
            onClick={() => setCurrentView('bookmarks')}
            title="Bookmarks"
          >
            <Bookmark className="h-4 w-4" />
          </Button>
          <Button
            variant={currentView === 'settings' ? 'secondary' : 'ghost'}
            size="icon"
            className="h-8 w-8"
            onClick={() => setCurrentView('settings')}
            title="Settings"
          >
            <Settings className="h-4 w-4" />
          </Button>
        </div>
      </header>

      {/* Tab Strip */}
      <TabStrip />

      {/* Content Area */}
      <WebViewContainer />

      {/* Bottom bar for compact layout */}
      {layoutMode === 'compact' && (
        <div className="flex items-center justify-center gap-1 px-2 py-1 border-t shrink-0">
          <Button variant="ghost" size="sm" className="h-8 text-xs gap-1" onClick={() => setShowWorkspaceSwitcher(true)}>
            <LayoutGrid className="h-3.5 w-3.5" /> Workspaces
          </Button>
          <Button variant="ghost" size="sm" className="h-8 text-xs gap-1" onClick={() => setShowAddTabSheet(true)}>
            <Plus className="h-3.5 w-3.5" /> New Tab
          </Button>
          <Button variant="ghost" size="sm" className="h-8 text-xs gap-1" onClick={() => setCurrentView('bookmarks')}>
            <Bookmark className="h-3.5 w-3.5" /> Bookmarks
          </Button>
          <Button variant="ghost" size="sm" className="h-8 text-xs gap-1" onClick={() => setShowSaveSessionDialog(true)}>
            Save Session
          </Button>
        </div>
      )}

      {/* Side panels for medium/expanded layout */}
      {(layoutMode === 'medium' || layoutMode === 'expanded') && (
        <div className={cn(
          'border-t shrink-0 flex overflow-hidden',
          layoutMode === 'expanded' && 'border-l border-t-0 w-56 flex-col'
        )}>
          <nav className="flex flex-col gap-0.5 p-1.5">
            <Button variant="ghost" size="sm" className="justify-start h-8 text-xs gap-2" onClick={() => setShowWorkspaceSwitcher(true)}>
              <LayoutGrid className="h-3.5 w-3.5" /> Workspaces
            </Button>
            <Button variant="ghost" size="sm" className="justify-start h-8 text-xs gap-2" onClick={() => setShowAddTabSheet(true)}>
              <Plus className="h-3.5 w-3.5" /> New Tab
            </Button>
            <Button variant="ghost" size="sm" className="justify-start h-8 text-xs gap-2" onClick={() => setCurrentView('bookmarks')}>
              <Bookmark className="h-3.5 w-3.5" /> Bookmarks
            </Button>
            <Button variant="ghost" size="sm" className="justify-start h-8 text-xs gap-2" onClick={() => setShowSaveSessionDialog(true)}>
              Save Session
            </Button>
          </nav>
        </div>
      )}

      {/* Overlays */}
      <WorkspaceSwitcher />
      <AddTabSheet />
      <QuickSwitcher />
      <RecentlyClosedSheet />
      <TabSettingsSheet />
    </div>
  );
}

export default function Page() {
  return (
    <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
      <TooltipProvider delayDuration={300}>
        <WebHubApp />
      </TooltipProvider>
    </ThemeProvider>
  );
}