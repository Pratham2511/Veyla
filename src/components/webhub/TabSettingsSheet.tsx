'client';

import { Sheet, SheetContent, SheetHeader, SheetTitle } from '@/components/ui/sheet';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Separator } from '@/components/ui/separator';
import { Textarea } from '@/components/ui/textarea';
import { Settings, Trash2, Copy, Eye, Code, Paintbrush } from 'lucide-react';
import { useWebHubStore } from '@/lib/store';
import { getDomainFromUrl } from '@/lib/types';

export function TabSettingsSheet() {
  const { showTabSettings, setShowTabSettings, tabs, updateTab, closeTab, duplicateTab } = useWebHubStore();
  const tab = tabs.find(t => t.id === showTabSettings);
  if (!tab) return null;

  return (
    <Sheet open={!!showTabSettings} onOpenChange={() => setShowTabSettings(null)}>
      <SheetContent side="right" className="w-80 p-0">
        <SheetHeader className="p-4 pb-2">
          <SheetTitle className="flex items-center gap-2 text-base">
            <Settings className="h-4 w-4" />
            Tab Settings
          </SheetTitle>
          <p className="text-xs text-muted-foreground truncate">{tab.customName || tab.title || getDomainFromUrl(tab.url)}</p>
        </SheetHeader>

        <div className="px-4 space-y-4 pb-4">
          {/* Custom Name */}
          <div className="space-y-1.5">
            <Label className="text-sm">Custom Name</Label>
            <Input
              value={tab.customName || ''}
              onChange={(e) => updateTab(tab.id, { customName: e.target.value || null })}
              placeholder={getDomainFromUrl(tab.url)}
              className="h-8 text-sm"
            />
          </div>

          <Separator />

          {/* Toggles */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Code className="h-3.5 w-3.5 text-muted-foreground" />
                <Label className="text-sm">JavaScript</Label>
              </div>
              <Switch
                checked={tab.isJsEnabled}
                onCheckedChange={(v) => updateTab(tab.id, { isJsEnabled: v })}
              />
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Eye className="h-3.5 w-3.5 text-muted-foreground" />
                <Label className="text-sm">Ad Blocking</Label>
              </div>
              <Switch
                checked={tab.isAdBlockEnabled}
                onCheckedChange={(v) => updateTab(tab.id, { isAdBlockEnabled: v })}
              />
            </div>
          </div>

          <Separator />

          {/* CSS Override */}
          <div className="space-y-1.5">
            <Label className="text-sm flex items-center gap-1.5">
              <Paintbrush className="h-3.5 w-3.5" /> CSS Override
            </Label>
            <Textarea
              value={tab.cssOverride || ''}
              onChange={(e) => updateTab(tab.id, { cssOverride: e.target.value || null })}
              placeholder="body { background: #1a1a1a !important; }"
              className="text-xs font-mono min-h-[80px]"
            />
          </div>

          {/* User Script */}
          <div className="space-y-1.5">
            <Label className="text-sm flex items-center gap-1.5">
              <Code className="h-3.5 w-3.5" /> User Script
            </Label>
            <Textarea
              value={tab.userScript || ''}
              onChange={(e) => updateTab(tab.id, { userScript: e.target.value || null })}
              placeholder="// JavaScript to inject on page load"
              className="text-xs font-mono min-h-[80px]"
            />
          </div>

          <Separator />

          {/* Actions */}
          <div className="space-y-2">
            <Button variant="outline" size="sm" className="w-full gap-1.5" onClick={() => { duplicateTab(tab.id); setShowTabSettings(null); }}>
              <Copy className="h-3.5 w-3.5" /> Duplicate Tab
            </Button>
            <Button variant="destructive" size="sm" className="w-full gap-1.5" onClick={() => { closeTab(tab.id); setShowTabSettings(null); }}>
              <Trash2 className="h-3.5 w-3.5" /> Close Tab
            </Button>
          </div>
        </div>
      </SheetContent>
    </Sheet>
  );
}