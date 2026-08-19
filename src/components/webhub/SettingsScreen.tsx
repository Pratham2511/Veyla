'use client';

import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { ScrollArea } from '@/components/ui/scroll-area';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Sun, Moon, Monitor, Shield, Search, Globe, Trash2, Save, FolderOpen, RotateCcw, Plus, X } from 'lucide-react';
import { useWebHubStore } from '@/lib/store';
import { useTheme } from 'next-themes';
import type { ThemeMode } from '@/lib/types';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
  DialogClose,
} from '@/components/ui/dialog';

const SEARCH_ENGINES = [
  { name: 'Google', url: 'https://www.google.com/search?q=' },
  { name: 'DuckDuckGo', url: 'https://duckduckgo.com/?q=' },
  { name: 'Bing', url: 'https://www.bing.com/search?q=' },
  { name: 'Brave Search', url: 'https://search.brave.com/search?q=' },
  { name: 'Ecosia', url: 'https://www.ecosia.org/search?q=' },
];

export function SettingsScreen() {
  const { settings, updateSettings, setCurrentView, saveSession, showSaveSessionDialog, setShowSaveSessionDialog, sessions, deleteSession, tabs, workspaces, closedTabs } = useWebHubStore();
  const { theme, setTheme } = useTheme();
  const [sessionName, setSessionName] = useState('');
  const [customSeUrl, setCustomSeUrl] = useState(settings?.searchEngineUrl || '');

  const currentTheme = (theme === 'system' ? 'system' : theme) as ThemeMode;

  const handleThemeChange = (mode: string) => {
    setTheme(mode);
    updateSettings({ themeMode: mode });
  };

  const handleSearchEngine = (url: string) => {
    setCustomSeUrl(url);
    updateSettings({ searchEngineUrl: url });
  };

  const handleSaveSession = async () => {
    if (!sessionName.trim()) return;
    await saveSession(sessionName.trim());
    setSessionName('');
  };

  const handleCompleteOnboarding = async () => {
    await updateSettings({ onboardingDone: true });
    setCurrentView('main');
  };

  return (
    <div className="flex-1 flex flex-col">
      <div className="flex items-center justify-between px-4 py-3 border-b">
        <div>
          <h2 className="text-lg font-semibold">Settings</h2>
          <p className="text-xs text-muted-foreground">Customize your WebHub experience</p>
        </div>
        <Button variant="outline" size="sm" onClick={() => setCurrentView('main')}>
          Back
        </Button>
      </div>

      <ScrollArea className="flex-1">
        <div className="max-w-2xl mx-auto p-4 space-y-4">
          {/* Appearance */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-base flex items-center gap-2">
                <Sun className="h-4 w-4" /> Appearance
              </CardTitle>
              <CardDescription>Customize how WebHub looks</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <Label>Theme</Label>
                <div className="flex items-center gap-1 bg-muted rounded-lg p-1">
                  {([['light', <Sun key="l" className="h-3.5 w-3.5" />], ['dark', <Moon key="d" className="h-3.5 w-3.5" />], ['system', <Monitor key="s" className="h-3.5 w-3.5" />]] as const).map(([mode, icon]) => (
                    <Button
                      key={mode}
                      variant={currentTheme === mode ? 'default' : 'ghost'}
                      size="sm"
                      className="h-8 gap-1.5 text-xs"
                      onClick={() => handleThemeChange(mode)}
                    >
                      {icon} {mode.charAt(0).toUpperCase() + mode.slice(1)}
                    </Button>
                  ))}
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Search */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-base flex items-center gap-2">
                <Search className="h-4 w-4" /> Search Engine
              </CardTitle>
              <CardDescription>Choose your default search engine for the omnibox</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="grid grid-cols-2 gap-2">
                {SEARCH_ENGINES.map(se => (
                  <button
                    key={se.name}
                    className={`flex items-center gap-2 p-2.5 rounded-lg border text-left transition-colors ${
                      settings?.searchEngineUrl === se.url ? 'border-primary bg-primary/5' : 'hover:bg-muted'
                    }`}
                    onClick={() => handleSearchEngine(se.url)}
                  >
                    <Globe className="h-4 w-4 text-muted-foreground shrink-0" />
                    <span className="text-sm font-medium">{se.name}</span>
                  </button>
                ))}
              </div>
              <div className="space-y-1.5">
                <Label className="text-xs">Custom search URL</Label>
                <div className="flex gap-2">
                  <Input
                    placeholder="https://example.com/search?q="
                    value={customSeUrl}
                    onChange={(e) => setCustomSeUrl(e.target.value)}
                    className="h-8 text-sm font-mono"
                  />
                  <Button size="sm" variant="outline" className="h-8" onClick={() => handleSearchEngine(customSeUrl)}>
                    Set
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Privacy & Security */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-base flex items-center gap-2">
                <Shield className="h-4 w-4" /> Privacy & Security
              </CardTitle>
              <CardDescription>Control your privacy settings</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <Label>Ad & Tracker Blocking</Label>
                  <p className="text-xs text-muted-foreground">Block known ad and tracker domains</p>
                </div>
                <Switch
                  checked={settings?.adBlockEnabled ?? true}
                  onCheckedChange={(v) => updateSettings({ adBlockEnabled: v })}
                />
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <div>
                  <Label>Biometric Lock</Label>
                  <p className="text-xs text-muted-foreground">Require authentication to open WebHub</p>
                </div>
                <Switch
                  checked={settings?.isBiometricEnabled ?? false}
                  onCheckedChange={(v) => updateSettings({ isBiometricEnabled: v })}
                />
              </div>
            </CardContent>
          </Card>

          {/* Sessions */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-base flex items-center gap-2">
                <FolderOpen className="h-4 w-4" /> Session Management
              </CardTitle>
              <CardDescription>Save and restore your workspace sessions</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              <Dialog open={showSaveSessionDialog} onOpenChange={setShowSaveSessionDialog}>
                <DialogTrigger asChild>
                  <Button size="sm" className="gap-1.5">
                    <Save className="h-3.5 w-3.5" /> Save Current Session
                  </Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Save Session</DialogTitle>
                  </DialogHeader>
                  <Input
                    placeholder="Session name (e.g., Morning Routine)"
                    value={sessionName}
                    onChange={(e) => setSessionName(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSaveSession()}
                    autoFocus
                  />
                  <DialogFooter>
                    <DialogClose asChild>
                      <Button variant="outline">Cancel</Button>
                    </DialogClose>
                    <Button onClick={handleSaveSession} disabled={!sessionName.trim()}>Save</Button>
                  </DialogFooter>
                </DialogContent>
              </Dialog>

              {sessions.length > 0 && (
                <div className="space-y-2">
                  {sessions.map(s => (
                    <div key={s.id} className="flex items-center justify-between p-2 rounded-lg bg-muted/50">
                      <div>
                        <p className="text-sm font-medium">{s.name}</p>
                        <p className="text-xs text-muted-foreground">{new Date(s.createdAt).toLocaleDateString()}</p>
                      </div>
                      <div className="flex gap-1">
                        <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => {
                          const parsed = JSON.parse(s.data);
                          // Simple restore indicator
                        }}>
                          <RotateCcw className="h-3.5 w-3.5" />
                        </Button>
                        <Button variant="ghost" size="icon" className="h-7 w-7 text-destructive" onClick={() => deleteSession(s.id)}>
                          <Trash2 className="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Data Management */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-base flex items-center gap-2">
                <Trash2 className="h-4 w-4" /> Data Management
              </CardTitle>
              <CardDescription>Manage your stored data</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium">Tabs</p>
                  <p className="text-xs text-muted-foreground">{tabs.length} tabs across {workspaces.length} workspaces</p>
                </div>
                <Badge variant="secondary">{tabs.length}</Badge>
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium">Recently Closed</p>
                  <p className="text-xs text-muted-foreground">{closedTabs.length} recently closed tabs</p>
                </div>
                <Badge variant="secondary">{closedTabs.length}</Badge>
              </div>
            </CardContent>
          </Card>

          {/* About */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-base">About WebHub</CardTitle>
            </CardHeader>
            <CardContent className="text-sm text-muted-foreground space-y-1">
              <p>WebHub organizes your websites into persistent, workspace-grouped multi-tab containers.</p>
              <p className="text-xs">Version 1.0.0 (Web Prototype)</p>
            </CardContent>
          </Card>

          <div className="h-8" />
        </div>
      </ScrollArea>
    </div>
  );
}
