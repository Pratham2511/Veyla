'use client';

import { useState } from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Badge } from '@/components/ui/badge';
import { X, Plus, RotateCcw, Eye, ExternalLink } from 'lucide-react';
import { useWebHubStore } from '@/lib/store';
import { getDomainFromUrl, getFaviconUrl } from '@/lib/types';
import { cn } from '@/lib/utils';
import { AnimatePresence, motion } from 'framer-motion';

export function TabOverviewScreen() {
  const { tabs, activeTabId, activeWorkspaceId, selectTab, closeTab, setShowAddTabSheet, setCurrentView, hibernateTab, restoreTab } = useWebHubStore();
  const wsTabs = tabs.filter(t => t.workspaceId === activeWorkspaceId).sort((a, b) => a.position - b.position);

  return (
    <div className="flex-1 flex flex-col">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-3 border-b">
        <div>
          <h2 className="text-lg font-semibold">Tab Overview</h2>
          <p className="text-xs text-muted-foreground">{wsTabs.length} tab{wsTabs.length !== 1 ? 's' : ''} in workspace</p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={() => setCurrentView('main')}>
            Back to Browsing
          </Button>
          <Button size="sm" onClick={() => setShowAddTabSheet(true)}>
            <Plus className="h-4 w-4 mr-1" /> New Tab
          </Button>
        </div>
      </div>

      {/* Tab grid */}
      <ScrollArea className="flex-1">
        <div className="p-4 grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
          <AnimatePresence>
            {wsTabs.map((tab, i) => {
              const isActive = tab.id === activeTabId;
              const displayName = tab.customName || tab.title || getDomainFromUrl(tab.url);
              const favicon = tab.faviconUrl || getFaviconUrl(tab.url);

              return (
                <motion.div
                  key={tab.id}
                  layout
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.8 }}
                  transition={{ delay: i * 0.02 }}
                >
                  <Card
                    className={cn(
                      'cursor-pointer group hover:shadow-md transition-all overflow-hidden',
                      isActive && 'ring-2 ring-primary'
                    )}
                    onClick={() => {
                      selectTab(tab.id);
                      if (tab.isHibernated) restoreTab(tab.id);
                      setCurrentView('main');
                    }}
                  >
                    {/* Thumbnail area */}
                    <div className="relative aspect-video bg-muted/30 border-b">
                      <div className="absolute inset-0 flex flex-col items-center justify-center gap-1 p-2">
                        <img src={favicon} alt="" className="h-8 w-8 rounded-md shadow-sm bg-white" onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }} />
                        <span className="text-[10px] text-muted-foreground text-center truncate w-full">{getDomainFromUrl(tab.url)}</span>
                      </div>

                      {/* Badges */}
                      <div className="absolute top-1.5 right-1.5 flex gap-1">
                        {tab.isIncognito && (
                          <Badge variant="secondary" className="h-4 px-1 text-[9px] bg-purple-600 text-white">
                            <Eye className="h-2.5 w-2.5 mr-0.5" /> Incognito
                          </Badge>
                        )}
                        {tab.isHibernated && (
                          <Badge variant="secondary" className="h-4 px-1 text-[9px]">
                            <RotateCcw className="h-2.5 w-2.5 mr-0.5" /> Sleeping
                          </Badge>
                        )}
                      </div>

                      {/* Actions overlay */}
                      <div className="absolute inset-0 bg-black/0 group-hover:bg-black/40 transition-colors flex items-center justify-center opacity-0 group-hover:opacity-100">
                        <div className="flex gap-2">
                          <Button
                            variant="secondary"
                            size="icon"
                            className="h-8 w-8"
                            onClick={(e) => { e.stopPropagation(); selectTab(tab.id); setCurrentView('main'); }}
                          >
                            <ExternalLink className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="secondary"
                            size="icon"
                            className="h-8 w-8"
                            onClick={(e) => { e.stopPropagation(); closeTab(tab.id); }}
                          >
                            <X className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>
                    </div>

                    <CardContent className="p-2.5">
                      <div className="flex items-center gap-1.5">
                        <img src={favicon} alt="" className="h-3.5 w-3.5 rounded-sm shrink-0" onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }} />
                        <p className="text-xs font-medium truncate">{displayName}</p>
                      </div>
                    </CardContent>
                  </Card>
                </motion.div>
              );
            })}
          </AnimatePresence>

          {/* Add tab card */}
          <Card
            className="cursor-pointer border-dashed hover:border-primary/50 transition-colors"
            onClick={() => setShowAddTabSheet(true)}
          >
            <div className="aspect-video flex items-center justify-center">
              <div className="flex flex-col items-center gap-1 text-muted-foreground">
                <Plus className="h-8 w-8" />
                <span className="text-xs">Add Tab</span>
              </div>
            </div>
          </Card>
        </div>
      </ScrollArea>
    </div>
  );
}