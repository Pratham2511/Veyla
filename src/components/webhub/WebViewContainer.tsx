'use client';

import { useEffect, useRef, useState, useCallback } from 'react';
import { useWebHubStore } from '@/lib/store';
import { Progress } from '@/components/ui/progress';
import { Eye, Globe, AlertTriangle } from 'lucide-react';
import { cn } from '@/lib/utils';

function IframeView({ tab }: { tab: { id: string; url: string; title: string; isIncognito: boolean } }) {
  const iframeRef = useRef<HTMLIFrameElement>(null);
  const [progress, setProgress] = useState(0);
  const [done, setDone] = useState(false);
  const intervalRef = useRef<ReturnType<typeof setInterval>>();

  // Start simulated progress on mount
  useEffect(() => {
    let p = 0;
    intervalRef.current = setInterval(() => {
      p += Math.random() * 15 + 5;
      if (p > 90) p = 90;
      setProgress(p);
    }, 200);
    return () => { if (intervalRef.current) clearInterval(intervalRef.current); };
  }, []);

  const handleLoad = useCallback(() => {
    if (intervalRef.current) clearInterval(intervalRef.current);
    setProgress(100);
    setTimeout(() => setDone(true), 300);
  }, []);

  return (
    <div className="w-full h-full relative">
      {!done && <Progress value={progress} className="h-0.5 rounded-none absolute top-0 left-0 right-0 z-50" />}
      {tab.isIncognito && (
        <div className="absolute top-0.5 left-0 right-0 z-40 flex justify-center pointer-events-none">
          <div className="bg-purple-600/90 text-white text-xs px-3 py-0.5 rounded-full flex items-center gap-1">
            <Eye className="h-3 w-3" /> Incognito
          </div>
        </div>
      )}
      <iframe
        ref={iframeRef}
        src={tab.url}
        className={cn('w-full h-full border-0', tab.isIncognito && 'pt-6')}
        sandbox="allow-same-origin allow-scripts allow-popups allow-forms allow-modals allow-downloads"
        allow="camera; microphone; fullscreen"
        referrerPolicy="no-referrer"
        onLoad={handleLoad}
        title={tab.title || tab.url}
      />
    </div>
  );
}

export function WebViewContainer() {
  const { activeTabId, tabs, updateTab, restoreTab } = useWebHubStore();
  const activeTab = tabs.find(t => t.id === activeTabId);

  // Title polling
  useEffect(() => {
    if (!activeTab) return;
    const interval = setInterval(() => {
      const iframe = document.querySelector<HTMLIFrameElement>('iframe[title]');
      if (!iframe?.contentWindow) return;
      try {
        const title = iframe.contentWindow.document.title;
        if (title && title !== activeTab.title) {
          updateTab(activeTab.id, { title });
        }
      } catch {
        // Cross-origin
      }
    }, 2000);
    return () => clearInterval(interval);
  }, [activeTab?.id, activeTab, updateTab]);

  if (!activeTab) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center gap-4 p-8 text-center">
        <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-primary/20 to-primary/5 flex items-center justify-center">
          <Globe className="h-10 w-10 text-primary/60" />
        </div>
        <div>
          <h2 className="text-xl font-semibold mb-1">Welcome to WebHub</h2>
          <p className="text-sm text-muted-foreground max-w-sm">
            Add your first tab to get started. Click the + button above or use the URL bar to navigate to any website.
          </p>
        </div>
      </div>
    );
  }

  if (activeTab.isHibernated) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center gap-3 text-muted-foreground">
        <Eye className="h-12 w-12 opacity-30" />
        <p className="text-sm font-medium">Tab is hibernated</p>
        <p className="text-xs">Click to restore and reload</p>
        <button className="text-xs text-primary hover:underline" onClick={() => restoreTab(activeTab.id)}>
          Restore
        </button>
      </div>
    );
  }

  return (
    <div className="flex-1 overflow-hidden">
      <IframeView key={activeTab.id} tab={activeTab} />
    </div>
  );
}