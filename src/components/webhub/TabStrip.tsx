'use client';

import { useRef } from 'react';
import { Button } from '@/components/ui/button';
import { ScrollArea, ScrollBar } from '@/components/ui/scroll-area';
import { X, Plus, EyeOff, GripVertical } from 'lucide-react';
import { useWebHubStore } from '@/lib/store';
import { getDomainFromUrl, getFaviconUrl } from '@/lib/types';
import { ContextMenu, ContextMenuContent, ContextMenuItem, ContextMenuSeparator, ContextMenuTrigger, ContextMenuSub, ContextMenuSubContent, ContextMenuSubTrigger } from '@/components/ui/context-menu';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from '@dnd-kit/core';
import {
  SortableContext,
  horizontalListSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { cn } from '@/lib/utils';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip';

function TabChip({ tabId }: { tabId: string }) {
  const { tabs, activeTabId, selectTab, closeTab, duplicateTab, moveTabToWorkspace, workspaces, activeWorkspaceId, hibernateTab, restoreTab, setShowTabSettings } = useWebHubStore();
  const tab = tabs.find(t => t.id === tabId);
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: tabId });
  const style = { transform: CSS.Transform.toString(transform), transition, zIndex: isDragging ? 50 : undefined };

  if (!tab) return null;
  const isActive = tab.id === activeTabId;
  const displayName = tab.customName || tab.title || getDomainFromUrl(tab.url);
  const favicon = tab.customIconUri || tab.faviconUrl || getFaviconUrl(tab.url);

  return (
    <ContextMenu>
      <ContextMenuTrigger>
        <div
          ref={setNodeRef}
          style={style}
          className={cn(
            'group flex items-center gap-1.5 px-2.5 py-1.5 rounded-t-lg cursor-pointer select-none shrink-0 transition-all max-w-[160px] min-w-[100px]',
            isActive
              ? 'bg-background text-foreground border-t-2 border-x border-b-0 border-primary'
              : 'bg-muted/40 text-muted-foreground hover:bg-muted/70 border-t-2 border-transparent',
            tab.isIncognito && 'border-t-purple-500',
            tab.isHibernated && 'opacity-60',
            isDragging && 'opacity-50 shadow-lg'
          )}
          onClick={() => selectTab(tab.id)}
        >
          <div {...attributes} {...listeners} className="cursor-grab active:cursor-grabbing opacity-0 group-hover:opacity-100 transition-opacity">
            <GripVertical className="h-3 w-3" />
          </div>
          <img
            src={favicon}
            alt=""
            className="h-4 w-4 rounded-sm shrink-0 bg-muted"
            onError={(e) => { (e.target as HTMLImageElement).src = 'data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2216%22 height=%2216%22><rect width=%2216%22 height=%2216%22 fill=%22%23666%22 rx=%222%22/></svg>'; }}
          />
          <span className="text-xs truncate flex-1">{displayName}</span>
          {tab.isIncognito && <EyeOff className="h-3 w-3 text-purple-400 shrink-0" />}
          {tab.isHibernated && (
            <Tooltip>
              <TooltipTrigger asChild>
                <EyeOff className="h-3 w-3 text-muted-foreground shrink-0" />
              </TooltipTrigger>
              <TooltipContent>Hibernated - click to restore</TooltipContent>
            </Tooltip>
          )}
          <Button
            variant="ghost"
            size="icon"
            className="h-5 w-5 shrink-0 opacity-0 group-hover:opacity-100 transition-opacity"
            onClick={(e) => { e.stopPropagation(); closeTab(tab.id); }}
          >
            <X className="h-3 w-3" />
          </Button>
        </div>
      </ContextMenuTrigger>
      <ContextMenuContent>
        <ContextMenuItem onClick={() => duplicateTab(tab.id)}>Duplicate Tab</ContextMenuItem>
        {tab.isHibernated ? (
          <ContextMenuItem onClick={() => restoreTab(tab.id)}>Restore Tab</ContextMenuItem>
        ) : (
          <ContextMenuItem onClick={() => hibernateTab(tab.id)}>Hibernate Tab</ContextMenuItem>
        )}
        <ContextMenuItem onClick={() => setShowTabSettings(tab.id)}>Tab Settings</ContextMenuItem>
        <ContextMenuSub>
          <ContextMenuSubTrigger>Move to Workspace</ContextMenuSubTrigger>
          <ContextMenuSubContent>
            {workspaces.filter(w => w.id !== activeWorkspaceId).map(ws => (
              <ContextMenuItem key={ws.id} onClick={() => moveTabToWorkspace(tab.id, ws.id)}>{ws.name}</ContextMenuItem>
            ))}
          </ContextMenuSubContent>
        </ContextMenuSub>
        <ContextMenuSeparator />
        <ContextMenuItem className="text-destructive" onClick={() => closeTab(tab.id)}>Close Tab</ContextMenuItem>
      </ContextMenuContent>
    </ContextMenu>
  );
}

export function TabStrip() {
  const { tabs, activeWorkspaceId, setShowAddTabSheet, setCurrentView, setShowRecentlyClosed, reorderTabs, setShowQuickSwitcher } = useWebHubStore();
  const wsTabs = tabs.filter(t => t.workspaceId === activeWorkspaceId).sort((a, b) => a.position - b.position);
  const tabIds = wsTabs.map(t => t.id);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    useSensor(KeyboardSensor)
  );

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (over && active.id !== over.id) {
      const oldIndex = tabIds.indexOf(active.id as string);
      const newIndex = tabIds.indexOf(over.id as string);
      const newOrder = [...tabIds];
      newOrder.splice(oldIndex, 1);
      newOrder.splice(newIndex, 0, active.id as string);
      reorderTabs(newOrder);
    }
  };

  return (
    <div className="flex items-center border-b bg-muted/30">
      <ScrollArea className="flex-1">
        <div className="flex items-end min-h-[38px] px-1 gap-0.5">
          <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
            <SortableContext items={tabIds} strategy={horizontalListSortingStrategy}>
              {wsTabs.map(tab => (
                <TabChip key={tab.id} tabId={tab.id} />
              ))}
            </SortableContext>
          </DndContext>
        </div>
        <ScrollBar orientation="horizontal" className="h-1" />
      </ScrollArea>
      <div className="flex items-center gap-0.5 px-1 shrink-0">
        <Tooltip>
          <TooltipTrigger asChild>
            <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => setShowQuickSwitcher(true)}>
              <svg className="h-3.5 w-3.5" viewBox="0 0 16 16" fill="currentColor"><rect x="1" y="1" width="6" height="6" rx="1" /><rect x="9" y="1" width="6" height="6" rx="1" /><rect x="1" y="9" width="6" height="6" rx="1" /><rect x="9" y="9" width="6" height="6" rx="1" /></svg>
            </Button>
          </TooltipTrigger>
          <TooltipContent>Tab overview</TooltipContent>
        </Tooltip>
        <Tooltip>
          <TooltipTrigger asChild>
            <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => setShowRecentlyClosed(true)}>
              <svg className="h-3.5 w-3.5" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M2 8a6 6 0 0 1 12 0" strokeLinecap="round" /><path d="M10 8a4 4 0 0 1-8 0" strokeLinecap="round" /><path d="M4 8V4h4" strokeLinecap="round" strokeLinejoin="round" /></svg>
            </Button>
          </TooltipTrigger>
          <TooltipContent>Recently closed</TooltipContent>
        </Tooltip>
        <Tooltip>
          <TooltipTrigger asChild>
            <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => setShowAddTabSheet(true)}>
              <Plus className="h-4 w-4" />
            </Button>
          </TooltipTrigger>
          <TooltipContent>New tab</TooltipContent>
        </Tooltip>
      </div>
    </div>
  );
}
