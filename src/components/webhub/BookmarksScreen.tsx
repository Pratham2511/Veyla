'use client';

import { ScrollArea } from '@/components/ui/scroll-area';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Bookmark, ExternalLink, Trash2, Star } from 'lucide-react';
import { useWebHubStore } from '@/lib/store';
import { getDomainFromUrl, getFaviconUrl } from '@/lib/types';
import { AnimatePresence, motion } from 'framer-motion';

export function BookmarksScreen() {
  const { bookmarks, removeBookmark, addTab, setCurrentView, setShowAddTabSheet, activeWorkspaceId } = useWebHubStore();

  return (
    <div className="flex-1 flex flex-col">
      <div className="flex items-center justify-between px-4 py-3 border-b">
        <div>
          <h2 className="text-lg font-semibold">Bookmarks</h2>
          <p className="text-xs text-muted-foreground">{bookmarks.length} saved bookmark{bookmarks.length !== 1 ? 's' : ''}</p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={() => setCurrentView('main')}>
            Back
          </Button>
          <Button size="sm" onClick={() => setShowAddTabSheet(true)}>
            Add Tab
          </Button>
        </div>
      </div>

      <ScrollArea className="flex-1">
        {bookmarks.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-64 gap-3 text-muted-foreground">
            <Bookmark className="h-12 w-12 opacity-30" />
            <p className="text-sm">No bookmarks yet</p>
            <p className="text-xs">Bookmark tabs from the URL bar</p>
          </div>
        ) : (
          <div className="p-4 space-y-1">
            <AnimatePresence>
              {bookmarks.map((bm, i) => {
                const favicon = bm.faviconUrl || getFaviconUrl(bm.url);
                return (
                  <motion.div
                    key={bm.id}
                    layout
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, x: -20 }}
                    transition={{ delay: i * 0.02 }}
                  >
                    <div className="group flex items-center gap-3 p-2.5 rounded-lg hover:bg-muted transition-colors">
                      <img
                        src={favicon}
                        alt=""
                        className="h-5 w-5 rounded-sm shrink-0 bg-muted"
                        onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }}
                      />
                      <div className="flex-1 min-w-0 cursor-pointer" onClick={() => { addTab(bm.url); setCurrentView('main'); }}>
                        <p className="text-sm font-medium truncate">{bm.title}</p>
                        <p className="text-xs text-muted-foreground truncate">{getDomainFromUrl(bm.url)}</p>
                      </div>
                      <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-7 w-7"
                          onClick={() => { addTab(bm.url); setCurrentView('main'); }}
                        >
                          <ExternalLink className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-7 w-7 text-destructive hover:text-destructive"
                          onClick={() => removeBookmark(bm.id)}
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </div>
                  </motion.div>
                );
              })}
            </AnimatePresence>
          </div>
        )}
      </ScrollArea>
    </div>
  );
}