import { NextResponse } from 'next/server';
import { db } from '@/lib/db';

// GET all data
export async function GET() {
  try {
    const [workspaces, tabs, bookmarks, closedTabs, sessions, settings] = await Promise.all([
      db.workspace.findMany({ orderBy: { position: 'asc' } }),
      db.tab.findMany({ orderBy: { position: 'asc' } }),
      db.bookmark.findMany({ orderBy: { createdAt: 'desc' } }),
      db.closedTabHistory.findMany({ orderBy: { closedAt: 'desc' }, take: 20 }),
      db.sessionSnapshot.findMany({ orderBy: { createdAt: 'desc' } }),
      db.appSettings.findUnique({ where: { id: 'singleton' } }),
    ]);
    return NextResponse.json({ workspaces, tabs, bookmarks, closedTabs, sessions, settings });
  } catch (error) {
    console.error('GET error:', error);
    return NextResponse.json({ error: 'Failed to load data' }, { status: 500 });
  }
}

// POST - create operations
export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { action } = body;

    switch (action) {
      case 'createWorkspace': {
        const { name, themeMode, accentColor, position, isDefault } = body;
        const workspace = await db.workspace.create({
          data: { name, themeMode: themeMode || 'system', accentColor, position: position ?? 0, isDefault: isDefault ?? false },
        });
        return NextResponse.json(workspace);
      }
      case 'createTab': {
        const { workspaceId, url, title, faviconUrl, customName, isIncognito, position } = body;
        const tab = await db.tab.create({
          data: { workspaceId, url, title: title || url, faviconUrl, customName, isIncognito: isIncognito ?? false, position: position ?? 0 },
        });
        return NextResponse.json(tab);
      }
      case 'createBookmark': {
        const { url, title, faviconUrl } = body;
        const bookmark = await db.bookmark.create({ data: { url, title, faviconUrl } });
        return NextResponse.json(bookmark);
      }
      case 'addClosedTab': {
        const { tabId, url, title, faviconUrl } = body;
        const closed = await db.closedTabHistory.create({ data: { tabId, url, title, faviconUrl } });
        const count = await db.closedTabHistory.count();
        if (count > 20) {
          const oldest = await db.closedTabHistory.findMany({ orderBy: { closedAt: 'asc' }, take: count - 20 });
          await db.closedTabHistory.deleteMany({ where: { id: { in: oldest.map(o => o.id) } } });
        }
        return NextResponse.json(closed);
      }
      case 'saveSession': {
        const { name, data } = body;
        const session = await db.sessionSnapshot.create({ data: { name, data } });
        return NextResponse.json(session);
      }
      case 'initSettings': {
        const existing = await db.appSettings.findUnique({ where: { id: 'singleton' } });
        if (existing) return NextResponse.json(existing);
        const settings = await db.appSettings.create({ data: { id: 'singleton' } });
        return NextResponse.json(settings);
      }
      default:
        return NextResponse.json({ error: 'Unknown action' }, { status: 400 });
    }
  } catch (error) {
    console.error('POST error:', error);
    return NextResponse.json({ error: 'Failed to create' }, { status: 500 });
  }
}

// PUT - update operations
export async function PUT(request: Request) {
  try {
    const body = await request.json();
    const { action } = body;

    switch (action) {
      case 'updateWorkspace': {
        const { id, ...data } = body;
        delete data.action;
        const workspace = await db.workspace.update({ where: { id }, data });
        return NextResponse.json(workspace);
      }
      case 'updateTab': {
        const { id, ...data } = body;
        delete data.action;
        const tab = await db.tab.update({ where: { id }, data });
        return NextResponse.json(tab);
      }
      case 'updateSettings': {
        const { ...data } = body;
        delete data.action;
        const settings = await db.appSettings.upsert({
          where: { id: 'singleton' },
          update: data,
          create: { id: 'singleton', ...data },
        });
        return NextResponse.json(settings);
      }
      case 'reorderTabs': {
        const { tabIds } = body as { tabIds: string[] };
        await Promise.all(tabIds.map((id, index) => db.tab.update({ where: { id }, data: { position: index } })));
        return NextResponse.json({ success: true });
      }
      case 'reorderWorkspaces': {
        const { workspaceIds } = body as { workspaceIds: string[] };
        await Promise.all(workspaceIds.map((id, index) => db.workspace.update({ where: { id }, data: { position: index } })));
        return NextResponse.json({ success: true });
      }
      default:
        return NextResponse.json({ error: 'Unknown action' }, { status: 400 });
    }
  } catch (error) {
    console.error('PUT error:', error);
    return NextResponse.json({ error: 'Failed to update' }, { status: 500 });
  }
}

// DELETE operations
export async function DELETE(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const action = searchParams.get('action');

    switch (action) {
      case 'deleteTab': {
        const id = searchParams.get('id');
        if (!id) return NextResponse.json({ error: 'Missing id' }, { status: 400 });
        await db.tab.delete({ where: { id } });
        return NextResponse.json({ success: true });
      }
      case 'deleteWorkspace': {
        const id = searchParams.get('id');
        if (!id) return NextResponse.json({ error: 'Missing id' }, { status: 400 });
        await db.workspace.delete({ where: { id } });
        return NextResponse.json({ success: true });
      }
      case 'deleteBookmark': {
        const id = searchParams.get('id');
        if (!id) return NextResponse.json({ error: 'Missing id' }, { status: 400 });
        await db.bookmark.delete({ where: { id } });
        return NextResponse.json({ success: true });
      }
      case 'deleteClosedTab': {
        const id = searchParams.get('id');
        if (!id) return NextResponse.json({ error: 'Missing id' }, { status: 400 });
        await db.closedTabHistory.delete({ where: { id } });
        return NextResponse.json({ success: true });
      }
      case 'deleteSession': {
        const id = searchParams.get('id');
        if (!id) return NextResponse.json({ error: 'Missing id' }, { status: 400 });
        await db.sessionSnapshot.delete({ where: { id } });
        return NextResponse.json({ success: true });
      }
      case 'clearClosedTabs': {
        await db.closedTabHistory.deleteMany();
        return NextResponse.json({ success: true });
      }
      default:
        return NextResponse.json({ error: 'Unknown action' }, { status: 400 });
    }
  } catch (error) {
    console.error('DELETE error:', error);
    return NextResponse.json({ error: 'Failed to delete' }, { status: 500 });
  }
}
