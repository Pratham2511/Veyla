'use client';

import { Sheet, SheetContent, SheetHeader, SheetTitle } from '@/components/ui/sheet';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { RotateCcw, ExternalLink, Trash2 } from 'lucide-react';
import { useWebHubStore } from '@/lib/store';
import { getDomainFromUrl, getFaviconUrl } from '@/lib/types';
import { AnimatePresence, motion } from 'framer-motion';

export function RecentlyClosedSheet() {
  const { showRecentlyClosed, setShowRecentlyClosed, closedTabs, reopenClosedTab } = useWebHubStore();

  return (
    <Sheet open={showRecentlyClosed} onOpenChange={setShowRecentlyClosed}>
      <SheetContent side="bottom" className="max-h-[60vh] rounded-t-2xl">
        <SheetHeader className="text-left px-4 pt-4 pb-2">
          <SheetTitle className="flex items-center gap-2 text-base">
            <RotateCcw className="h-4 w-4" />
            Recently Closed
          </SheetTitle>
        </SheetHeader>

        <ScrollArea className="max-h-[40vh]">
          {closedTabs.length === 0 ? (
            <div className="p-8 text-center text-sm text-muted-foreground">
              No recently closed tabs
            </div>
          ) : (
            <div className="px-4 pb-4 space-y-1">
              <AnimatePresence>
                {closedTabs.map((ct, i) => {
                  const favicon = ct.faviconUrl || getFaviconUrl(ct.url);
                  return (
                    <motion.div
                      key={ct.id}
                      layout
                      initial={{ opacity: 0, x: -10 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0, x: 10 }}
                      transition={{ delay: i * 0.03 }}
                    >
                      <div className="group flex items-center gap-3 p-2 rounded-lg hover:bg-muted transition-colors">
                        <img src={favicon} alt="" className="h-4 w-4 rounded-sm shrink-0" onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }} />
                        <div className="flex-1 min-w-0">
                          <p className="text-sm truncate">{ct.title || getDomainFromUrl(ct.url)}</p>
                          <p className="text-xs text-muted-foreground truncate">{getDomainFromUrl(ct.url)}</p>
                        </div>
                        <Button
                          variant="outline"
                          size="sm"
                          className="h-7 text-xs gap-1 opacity-0 group-hover:opacity-100 transition-opacity"
                          onClick={() => reopenClosedTab(ct)}
                        >
                          <ExternalLink className="h-3 w-3" /> Reopen
                        </Button>
                      </div>
                    </motion.div>
                  );
                })}
              </AnimatePresence>
            </div>
          )}
        </ScrollArea>
      </SheetContent>
    </Sheet>
  );
}