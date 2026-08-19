'use client';

import { useState } from 'react';
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from '@/components/ui/sheet';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { Briefcase, Plus, Pencil, Trash2, Star, Check, LayoutGrid } from 'lucide-react';
import { useWebHubStore } from '@/lib/store';
import { cn } from '@/lib/utils';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from '@/components/ui/alert-dialog';

export function WorkspaceSwitcher() {
  const { workspaces, tabs, activeWorkspaceId, selectWorkspace, addWorkspace, renameWorkspace, deleteWorkspace, setDefaultWorkspace, showWorkspaceSwitcher, setShowWorkspaceSwitcher } = useWebHubStore();
  const [newName, setNewName] = useState('');
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState('');

  const handleCreate = async () => {
    if (!newName.trim()) return;
    await addWorkspace(newName.trim());
    setNewName('');
  };

  const handleRename = async (id: string) => {
    if (!editName.trim()) return;
    await renameWorkspace(id, editName.trim());
    setEditingId(null);
  };

  return (
    <Sheet open={showWorkspaceSwitcher} onOpenChange={setShowWorkspaceSwitcher}>
      <SheetContent side="left" className="w-80 p-0">
        <SheetHeader className="p-4 pb-2">
          <SheetTitle className="flex items-center gap-2">
            <LayoutGrid className="h-5 w-5" />
            Workspaces
          </SheetTitle>
        </SheetHeader>

        <div className="px-4 pb-2">
          <div className="flex gap-2">
            <Input
              placeholder="New workspace name"
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleCreate()}
              className="h-8 text-sm"
            />
            <Button size="sm" className="h-8 px-3" onClick={handleCreate} disabled={!newName.trim()}>
              <Plus className="h-4 w-4" />
            </Button>
          </div>
        </div>

        <Separator />

        <ScrollArea className="flex-1 h-[calc(100vh-180px)]">
          <div className="p-2 space-y-1">
            {workspaces.map(ws => {
              const tabCount = tabs.filter(t => t.workspaceId === ws.id).length;
              const isActive = ws.id === activeWorkspaceId;
              const isEditing = editingId === ws.id;

              return (
                <div
                  key={ws.id}
                  className={cn(
                    'group flex items-center gap-2 px-3 py-2.5 rounded-lg cursor-pointer transition-colors',
                    isActive ? 'bg-primary/10 text-primary' : 'hover:bg-muted'
                  )}
                  onClick={() => selectWorkspace(ws.id)}
                >
                  <Briefcase className={cn('h-4 w-4 shrink-0', isActive ? 'text-primary' : 'text-muted-foreground')} />

                  {isEditing ? (
                    <Input
                      value={editName}
                      onChange={(e) => setEditName(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') handleRename(ws.id);
                        if (e.key === 'Escape') setEditingId(null);
                      }}
                      onBlur={() => handleRename(ws.id)}
                      className="h-7 text-sm"
                      autoFocus
                      onClick={(e) => e.stopPropagation()}
                    />
                  ) : (
                    <span className="flex-1 text-sm font-medium truncate">{ws.name}</span>
                  )}

                  <Badge variant="secondary" className="text-xs h-5 px-1.5">
                    {tabCount}
                  </Badge>

                  {ws.isDefault && (
                    <Star className="h-3 w-3 text-yellow-500 fill-yellow-500 shrink-0" />
                  )}

                  {!isEditing && (
                    <div className="flex items-center gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity">
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-6 w-6"
                        onClick={(e) => {
                          e.stopPropagation();
                          setEditingId(ws.id);
                          setEditName(ws.name);
                        }}
                      >
                        <Pencil className="h-3 w-3" />
                      </Button>

                      {!ws.isDefault && (
                        <AlertDialog>
                          <AlertDialogTrigger asChild>
                            <Button
                              variant="ghost"
                              size="icon"
                              className="h-6 w-6 text-destructive hover:text-destructive"
                              onClick={(e) => e.stopPropagation()}
                            >
                              <Trash2 className="h-3 w-3" />
                            </Button>
                          </AlertDialogTrigger>
                          <AlertDialogContent>
                            <AlertDialogHeader>
                              <AlertDialogTitle>Delete workspace?</AlertDialogTitle>
                              <AlertDialogDescription>
                                This will delete &quot;{ws.name}&quot; and all its {tabCount} tab(s). This action cannot be undone.
                              </AlertDialogDescription>
                            </AlertDialogHeader>
                            <AlertDialogFooter>
                              <AlertDialogCancel>Cancel</AlertDialogCancel>
                              <AlertDialogAction className="bg-destructive text-destructive-foreground hover:bg-destructive/90" onClick={() => deleteWorkspace(ws.id)}>
                                Delete
                              </AlertDialogAction>
                            </AlertDialogFooter>
                          </AlertDialogContent>
                        </AlertDialog>
                      )}

                      {!ws.isDefault && (
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-6 w-6"
                          onClick={(e) => {
                            e.stopPropagation();
                            setDefaultWorkspace(ws.id);
                          }}
                          title="Set as default"
                        >
                          <Check className="h-3 w-3" />
                        </Button>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </ScrollArea>
      </SheetContent>
    </Sheet>
  );
}
