'use client';

import { useState, useRef, useCallback } from 'react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Lock, Globe, Search, X, Bookmark, Star } from 'lucide-react';
import { useWebHubStore } from '@/lib/store';
import { isValidUrl, normalizeUrl, getDomainFromUrl } from '@/lib/types';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';

export function OmniboxBar() {
  const { activeTabId, tabs, settings, addTab, activeWorkspaceId, updateTab, addBookmark, removeBookmark, bookmarks } = useWebHubStore();
  const [isFocused, setIsFocused] = useState(false);
  const [inputValue, setInputValue] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);
  const activeTab = tabs.find(t => t.id === activeTabId);

  const isBookmarked = activeTab ? bookmarks.some(b => b.url === activeTab.url) : false;
  const isHttps = activeTab?.url.startsWith('https://');
  const isHttp = activeTab?.url.startsWith('http://');
  // When not focused, show domain; when focused, show the controlled input
  const displayValue = !isFocused && activeTab ? getDomainFromUrl(activeTab.url) : (!isFocused ? 'Search or enter URL' : inputValue);

  const handleFocus = useCallback(() => {
    setIsFocused(true);
    if (activeTab) {
      try { setInputValue(new URL(activeTab.url).href); } catch { setInputValue(activeTab.url); }
    } else {
      setInputValue('');
    }
  }, [activeTab]);

  const handleBlur = useCallback(() => {
    setTimeout(() => setIsFocused(false), 150);
  }, []);

  const handleSubmit = useCallback(async () => {
    const val = inputValue.trim();
    if (!val) return;
    const url = normalizeUrl(val);
    if (url) {
      if (activeTab) {
        await updateTab(activeTab.id, { url, title: getDomainFromUrl(url) });
      } else {
        await addTab(url, activeWorkspaceId || undefined);
      }
    } else {
      const searchUrl = `${settings?.searchEngineUrl || 'https://www.google.com/search?q='}${encodeURIComponent(val)}`;
      if (activeTab) {
        await updateTab(activeTab.id, { url: searchUrl, title: val });
      } else {
        await addTab(searchUrl, activeWorkspaceId || undefined);
      }
    }
    setIsFocused(false);
    inputRef.current?.blur();
  }, [inputValue, activeTab, activeWorkspaceId, settings?.searchEngineUrl, addTab, updateTab]);

  const handleBookmark = async () => {
    if (!activeTab) return;
    if (isBookmarked) {
      const bm = bookmarks.find(b => b.url === activeTab.url);
      if (bm) await removeBookmark(bm.id);
    } else {
      await addBookmark(activeTab.url, activeTab.title || activeTab.url, activeTab.faviconUrl);
    }
  };

  return (
    <div className="flex items-center gap-1.5 flex-1 min-w-0">
      <div className={
        `flex items-center gap-1.5 px-3 py-1.5 rounded-full flex-1 min-w-0 transition-colors bg-muted/50 border ${isFocused ? 'border-primary/50 bg-background' : 'border-transparent'} max-w-2xl mx-auto w-full`
      }>
        {activeTab && (
          <Tooltip>
            <TooltipTrigger asChild>
              <div className="shrink-0">
                {isHttps ? (
                  <Lock className="h-3.5 w-3.5 text-green-500" />
                ) : isHttp ? (
                  <Globe className="h-3.5 w-3.5 text-amber-500" />
                ) : (
                  <Search className="h-3.5 w-3.5 text-muted-foreground" />
                )}
              </div>
            </TooltipTrigger>
            <TooltipContent>
              {isHttps ? 'Secure connection (HTTPS)' : isHttp ? 'Not secure (HTTP)' : 'Search'}
            </TooltipContent>
          </Tooltip>
        )}
        {!activeTab && <Search className="h-3.5 w-3.5 text-muted-foreground shrink-0" />}

        <Input
          ref={inputRef}
          value={displayValue}
          onChange={(e) => {
            if (isFocused) setInputValue(e.target.value);
          }}
          onFocus={handleFocus}
          onBlur={handleBlur}
          onKeyDown={(e) => {
            if (e.key === 'Enter') handleSubmit();
            if (e.key === 'Escape') {
              setIsFocused(false);
              inputRef.current?.blur();
            }
          }}
          className="border-0 bg-transparent focus-visible:ring-0 h-7 text-sm px-1 font-mono"
          placeholder="Search or enter URL"
        />

        {isFocused && inputValue && (
          <Button
            variant="ghost"
            size="icon"
            className="h-6 w-6 shrink-0"
            onClick={() => setInputValue('')}
          >
            <X className="h-3 w-3" />
          </Button>
        )}

        {activeTab && !isFocused && (
          <Tooltip>
            <TooltipTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className="h-6 w-6 shrink-0"
                onClick={handleBookmark}
              >
                {isBookmarked ? (
                  <Star className="h-3.5 w-3.5 fill-yellow-500 text-yellow-500" />
                ) : (
                  <Bookmark className="h-3.5 w-3.5" />
                )}
              </Button>
            </TooltipTrigger>
            <TooltipContent>{isBookmarked ? 'Remove bookmark' : 'Bookmark this page'}</TooltipContent>
          </Tooltip>
        )}
      </div>
    </div>
  );
}
