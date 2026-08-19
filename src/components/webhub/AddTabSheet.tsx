'use client';

import { useState } from 'react';
import { Sheet, SheetContent, SheetHeader, SheetTitle } from '@/components/ui/sheet';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Globe, Eye, Search, Sparkles } from 'lucide-react';
import { useWebHubStore } from '@/lib/store';
import { isValidUrl, normalizeUrl, getDomainFromUrl, getFaviconUrl } from '@/lib/types';

const QUICK_SITES = [
  { name: 'Google', url: 'https://www.google.com', icon: '🔍' },
  { name: 'GitHub', url: 'https://github.com', icon: '🐙' },
  { name: 'YouTube', url: 'https://www.youtube.com', icon: '▶️' },
  { name: 'Twitter / X', url: 'https://x.com', icon: '🐦' },
  { name: 'Reddit', url: 'https://www.reddit.com', icon: '🤖' },
  { name: 'Wikipedia', url: 'https://www.wikipedia.org', icon: '📚' },
  { name: 'Notion', url: 'https://www.notion.so', icon: '📝' },
  { name: 'Gmail', url: 'https://mail.google.com', icon: '📧' },
  { name: 'LinkedIn', url: 'https://www.linkedin.com', icon: '💼' },
  { name: 'Stack Overflow', url: 'https://stackoverflow.com', icon: '📋' },
  { name: 'ChatGPT', url: 'https://chat.openai.com', icon: '🤖' },
  { name: 'Figma', url: 'https://www.figma.com', icon: '🎨' },
];

export function AddTabSheet() {
  const { showAddTabSheet, setShowAddTabSheet, addTab, activeWorkspaceId, workspaces, selectWorkspace, settings } = useWebHubStore();
  const [url, setUrl] = useState('');
  const [isIncognito, setIsIncognito] = useState(false);
  const [selectedWorkspace, setSelectedWorkspace] = useState(activeWorkspaceId);
  const [preview, setPreview] = useState<{ title: string; favicon: string } | null>(null);

  const handleUrlChange = (val: string) => {
    setUrl(val);
    if (isValidUrl(val)) {
      const fullUrl = normalizeUrl(val);
      setPreview({
        title: getDomainFromUrl(fullUrl),
        favicon: getFaviconUrl(fullUrl),
      });
    } else {
      setPreview(null);
    }
  };

  const handleAdd = async () => {
    const val = url.trim();
    if (!val) return;

    let finalUrl = normalizeUrl(val);
    if (!finalUrl) {
      // Search
      finalUrl = `${settings?.searchEngineUrl || 'https://www.google.com/search?q='}${encodeURIComponent(val)}`;
    }

    if (selectedWorkspace && selectedWorkspace !== activeWorkspaceId) {
      await selectWorkspace(selectedWorkspace);
    }
    await addTab(finalUrl, selectedWorkspace || activeWorkspaceId || undefined, isIncognito);
    setUrl('');
    setPreview(null);
    setIsIncognito(false);
  };

  const handleQuickAdd = (site: typeof QUICK_SITES[0]) => {
    addTab(site.url, selectedWorkspace || activeWorkspaceId || undefined, isIncognito);
    setShowAddTabSheet(false);
  };

  return (
    <Sheet open={showAddTabSheet} onOpenChange={setShowAddTabSheet}>
      <SheetContent side="bottom" className="max-h-[80vh] rounded-t-2xl">
        <SheetHeader className="text-left px-4 pt-4 pb-2">
          <SheetTitle className="flex items-center gap-2">
            <Globe className="h-5 w-5" />
            Add New Tab
          </SheetTitle>
        </SheetHeader>

        <div className="px-4 space-y-4 pb-4">
          {/* URL Input */}
          <div className="space-y-2">
            <Label htmlFor="url-input" className="text-sm font-medium">URL or Search</Label>
            <div className="flex gap-2">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  id="url-input"
                  placeholder="Enter URL or search query"
                  value={url}
                  onChange={(e) => handleUrlChange(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleAdd()}
                  className="pl-9"
                  autoFocus
                />
              </div>
              <Button onClick={handleAdd} disabled={!url.trim()}>
                Add
              </Button>
            </div>
          </div>

          {/* Preview */}
          {preview && (
            <div className="flex items-center gap-3 p-3 rounded-lg bg-muted/50 border">
              <img src={preview.favicon} alt="" className="h-8 w-8 rounded" onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }} />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate">{preview.title}</p>
                <p className="text-xs text-muted-foreground truncate">{normalizeUrl(url)}</p>
              </div>
              <Sparkles className="h-4 w-4 text-primary shrink-0" />
            </div>
          )}

          {/* Options row */}
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
              <Switch
                id="incognito-toggle"
                checked={isIncognito}
                onCheckedChange={setIsIncognito}
              />
              <Label htmlFor="incognito-toggle" className="text-sm flex items-center gap-1.5 cursor-pointer">
                <Eye className="h-3.5 w-3.5" /> Incognito
              </Label>
            </div>
          </div>

          {/* Quick add sites */}
          <div>
            <p className="text-sm font-medium mb-2">Quick Add</p>
            <div className="grid grid-cols-4 sm:grid-cols-6 gap-2">
              {QUICK_SITES.map(site => (
                <button
                  key={site.url}
                  className="flex flex-col items-center gap-1 p-2 rounded-lg hover:bg-muted transition-colors"
                  onClick={() => handleQuickAdd(site)}
                >
                  <span className="text-xl">{site.icon}</span>
                  <span className="text-[10px] text-muted-foreground truncate w-full text-center">{site.name}</span>
                </button>
              ))}
            </div>
          </div>
        </div>
      </SheetContent>
    </Sheet>
  );
}
