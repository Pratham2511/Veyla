'use client';

import { useState, useRef, useCallback } from 'react';
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Search } from 'lucide-react';
import { useWebHubStore } from '@/lib/store';
import { getDomainFromUrl, getFaviconUrl } from '@/lib/types';
import { cn } from '@/lib/utils';

export function QuickSwitcher() {
  const { showQuickSwitcher, setShowQuickSwitcher, tabs, selectTab, setCurrentView, activeTabId, restoreTab } = useWebHubStore();
  const [query, setQuery] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);
  const [selectedIndex, setSelectedIndex] = useState(0);

  const filtered = query
    ? tabs.filter(t => {
        const q = query.toLowerCase();
        return t.title.toLowerCase().includes(q) || t.url.toLowerCase().includes(q) || (t.customName || '').toLowerCase().includes(q);
      })
    : tabs;

  const handleOpenChange = useCallback((open: boolean) => {
    setShowQuickSwitcher(open);
    if (open) {
      setQuery('');
      setSelectedIndex(0);
      setTimeout(() => inputRef.current?.focus(), 100);
    }
  }, [setShowQuickSwitcher]);

  const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setQuery(e.target.value);
    setSelectedIndex(0);
  }, []);

  const handleSelect = useCallback((tabId: string, isHibernated: boolean) => {
    if (isHibernated) restoreTab(tabId);
    selectTab(tabId);
    setCurrentView('main');
    setShowQuickSwitcher(false);
  }, [restoreTab, selectTab, setCurrentView, setShowQuickSwitcher]);

  return (
    <Dialog open={showQuickSwitcher} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-lg p-0 gap-0 top-[20%] translate-y-0">
        <DialogTitle className="sr-only">Quick Tab Switcher</DialogTitle>
        <div className="flex items-center border-b px-3">
          <Search className="h-4 w-4 text-muted-foreground shrink-0" />
          <Input
            ref={inputRef}
            value={query}
            onChange={handleChange}
            onKeyDown={(e) => {
              if (e.key === 'ArrowDown') {
                e.preventDefault();
                setSelectedIndex(i => Math.min(i + 1, filtered.length - 1));
              }
              if (e.key === 'ArrowUp') {
                e.preventDefault();
                setSelectedIndex(i => Math.max(i - 1, 0));
              }
              if (e.key === 'Enter' && filtered[selectedIndex]) {
                handleSelect(filtered[selectedIndex].id, filtered[selectedIndex].isHibernated);
              }
              if (e.key === 'Escape') setShowQuickSwitcher(false);
            }}
            placeholder="Search tabs..."
            className="border-0 focus-visible:ring-0 h-10"
          />
        </div>
        <ScrollArea className="max-h-[300px]">
          {filtered.length === 0 ? (
            <div className="p-8 text-center text-sm text-muted-foreground">No tabs found</div>
          ) : (
            <div className="p-1">
              {filtered.map((tab, i) => {
                const isActive = tab.id === activeTabId;
                const favicon = tab.faviconUrl || getFaviconUrl(tab.url);
                return (
                  <button
                    key={tab.id}
                    className={cn(
                      'w-full flex items-center gap-3 px-3 py-2 rounded-md text-left transition-colors',
                      i === selectedIndex ? 'bg-accent' : 'hover:bg-muted/50',
                      isActive && 'font-medium'
                    )}
                    onClick={() => handleSelect(tab.id, tab.isHibernated)}
                    onMouseEnter={() => setSelectedIndex(i)}
                  >
                    <img src={favicon} alt="" className="h-4 w-4 rounded-sm shrink-0" onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }} />
                    <div className="flex-1 min-w-0">
                      <p className="text-sm truncate">{tab.customName || tab.title || getDomainFromUrl(tab.url)}</p>
                      <p className="text-xs text-muted-foreground truncate">{getDomainFromUrl(tab.url)}</p>
                    </div>
                    {isActive && <span className="text-xs text-primary shrink-0">Active</span>}
                    {tab.isHibernated && <span className="text-xs text-muted-foreground shrink-0">Sleeping</span>}
                  </button>
                );
              })}
            </div>
          )}
        </ScrollArea>
      </DialogContent>
    </Dialog>
  );
}
