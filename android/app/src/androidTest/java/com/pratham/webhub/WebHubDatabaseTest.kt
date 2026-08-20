package com.pratham.webhub

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.pratham.webhub.data.db.WebHubDatabase
import com.pratham.webhub.data.db.dao.ClosedTabHistoryDao
import com.pratham.webhub.data.db.dao.SessionSnapshotDao
import com.pratham.webhub.data.db.dao.TabDao
import com.pratham.webhub.data.db.dao.WorkspaceDao
import com.pratham.webhub.data.db.entity.ClosedTabHistoryEntity
import com.pratham.webhub.data.db.entity.SessionSnapshotEntity
import com.pratham.webhub.data.db.entity.TabEntity
import com.pratham.webhub.data.db.entity.WorkspaceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Room DAO tests for WebHub.
 * Uses an in-memory database so no data persists between test runs.
 */
@RunWith(AndroidJUnit4::class)
class WebHubDatabaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: WebHubDatabase
    private lateinit var tabDao: TabDao
    private lateinit var workspaceDao: WorkspaceDao
    private lateinit var closedTabHistoryDao: ClosedTabHistoryDao
    private lateinit var sessionSnapshotDao: SessionSnapshotDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, WebHubDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        tabDao = db.tabDao()
        workspaceDao = db.workspaceDao()
        closedTabHistoryDao = db.closedTabHistoryDao()
        sessionSnapshotDao = db.sessionSnapshotDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // ════════════════════════════════════════════════
    // TabDao Tests
    // ════════════════════════════════════════════════

    @Test
    fun tabDao_insertAndGetById() = runBlocking {
        val workspace = WorkspaceEntity(
            id = "ws-1", name = "Work", createdAt = 100L, updatedAt = 200L
        )
        workspaceDao.insert(workspace)

        val tab = TabEntity(
            id = "tab-1", workspaceId = "ws-1", url = "https://example.com",
            title = "Example", createdAt = 100L, updatedAt = 200L
        )
        tabDao.insert(tab)

        val retrieved = tabDao.getTabById("tab-1").first()
        assertThat(retrieved).isNotNull()
        assertThat(retrieved!!.id).isEqualTo("tab-1")
        assertThat(retrieved.url).isEqualTo("https://example.com")
        assertThat(retrieved.title).isEqualTo("Example")
    }

    @Test
    fun tabDao_insertAndGetTabsByWorkspace() = runBlocking {
        val ws = WorkspaceEntity(
            id = "ws-1", name = "Work", createdAt = 100L, updatedAt = 200L
        )
        workspaceDao.insert(ws)

        val tab1 = TabEntity(
            id = "tab-1", workspaceId = "ws-1", url = "https://a.com",
            position = 0, createdAt = 100L, updatedAt = 200L
        )
        val tab2 = TabEntity(
            id = "tab-2", workspaceId = "ws-1", url = "https://b.com",
            position = 1, createdAt = 150L, updatedAt = 250L
        )
        tabDao.insert(tab1)
        tabDao.insert(tab2)

        val tabs = tabDao.getTabsByWorkspace("ws-1").first()
        assertThat(tabs).hasSize(2)
        assertThat(tabs[0].id).isEqualTo("tab-1")  // ordered by position ASC
        assertThat(tabs[1].id).isEqualTo("tab-2")
    }

    @Test
    fun tabDao_getTabsByWorkspace_excludesHibernated() = runBlocking {
        val ws = WorkspaceEntity(
            id = "ws-1", name = "Work", createdAt = 100L, updatedAt = 200L
        )
        workspaceDao.insert(ws)

        val activeTab = TabEntity(
            id = "tab-active", workspaceId = "ws-1", url = "https://active.com",
            isHibernated = false, position = 0, createdAt = 100L, updatedAt = 200L
        )
        val hibernatedTab = TabEntity(
            id = "tab-hib", workspaceId = "ws-1", url = "https://hib.com",
            isHibernated = true, position = 1, createdAt = 100L, updatedAt = 200L
        )
        tabDao.insert(activeTab)
        tabDao.insert(hibernatedTab)

        val tabs = tabDao.getTabsByWorkspace("ws-1").first()
        assertThat(tabs).hasSize(1)
        assertThat(tabs[0].id).isEqualTo("tab-active")
    }

    @Test
    fun tabDao_getAllTabs() = runBlocking {
        val ws = WorkspaceEntity(
            id = "ws-1", name = "Work", createdAt = 100L, updatedAt = 200L
        )
        workspaceDao.insert(ws)

        tabDao.insert(TabEntity(
            id = "tab-1", workspaceId = "ws-1", url = "https://a.com",
            createdAt = 100L, updatedAt = 200L
        ))
        tabDao.insert(TabEntity(
            id = "tab-2", workspaceId = "ws-1", url = "https://b.com",
            createdAt = 150L, updatedAt = 250L
        ))

        val allTabs = tabDao.getAllTabs().first()
        assertThat(allTabs).hasSize(2)
    }

    @Test
    fun tabDao_updateUrlAndTitle() = runBlocking {
        val ws = WorkspaceEntity(
            id = "ws-1", name = "Work", createdAt = 100L, updatedAt = 200L
        )
        workspaceDao.insert(ws)

        tabDao.insert(TabEntity(
            id = "tab-1", workspaceId = "ws-1", url = "https://old.com",
            title = "Old Title", createdAt = 100L, updatedAt = 200L
        ))

        tabDao.updateUrlAndTitle("tab-1", "https://new.com", "New Title")

        val updated = tabDao.getTabById("tab-1").first()
        assertThat(updated).isNotNull()
        assertThat(updated!!.url).isEqualTo("https://new.com")
        assertThat(updated.title).isEqualTo("New Title")
    }

    @Test
    fun tabDao_updatePosition() = runBlocking {
        val ws = WorkspaceEntity(
            id = "ws-1", name = "Work", createdAt = 100L, updatedAt = 200L
        )
        workspaceDao.insert(ws)

        tabDao.insert(TabEntity(
            id = "tab-1", workspaceId = "ws-1", url = "https://a.com",
            position = 0, createdAt = 100L, updatedAt = 200L
        ))

        tabDao.updatePosition("tab-1", 5)

        val updated = tabDao.getTabById("tab-1").first()
        assertThat(updated).isNotNull()
        assertThat(updated!!.position).isEqualTo(5)
    }

    @Test
    fun tabDao_updateHibernation() = runBlocking {
        val ws = WorkspaceEntity(
            id = "ws-1", name = "Work", createdAt = 100L, updatedAt = 200L
        )
        workspaceDao.insert(ws)

        tabDao.insert(TabEntity(
            id = "tab-1", workspaceId = "ws-1", url = "https://a.com",
            isHibernated = false, createdAt = 100L, updatedAt = 200L
        ))

        tabDao.updateHibernation("tab-1", true)

        val updated = tabDao.getTabById("tab-1").first()
        assertThat(updated).isNotNull()
        assertThat(updated!!.isHibernated).isTrue()
    }

    @Test
    fun tabDao_updateScrollY() = runBlocking {
        val ws = WorkspaceEntity(
            id = "ws-1", name = "Work", createdAt = 100L, updatedAt = 200L
        )
        workspaceDao.insert(ws)

        tabDao.insert(TabEntity(
            id = "tab-1", workspaceId = "ws-1", url = "https://a.com",
            createdAt = 100L, updatedAt = 200L
        ))

        tabDao.updateScrollY("tab-1", 1234)

        val updated = tabDao.getTabById("tab-1").first()
        assertThat(updated).isNotNull()
        assertThat(updated!!.savedScrollY).isEqualTo(1234)
    }

    @Test
    fun tabDao_delete() = runBlocking {
        val ws = WorkspaceEntity(
            id = "ws-1", name = "Work", createdAt = 100L, updatedAt = 200L
        )
        workspaceDao.insert(ws)

        tabDao.insert(TabEntity(
            id = "tab-1", workspaceId = "ws-1", url = "https://a.com",
            createdAt = 100L, updatedAt = 200L
        ))

        tabDao.delete("tab-1")

        val retrieved = tabDao.getTabById("tab-1").first()
        assertThat(retrieved).isNull()
    }

    @Test
    fun tabDao_deleteByWorkspace() = runBlocking {
        val ws1 = WorkspaceEntity(
            id = "ws-1", name = "Work", createdAt = 100L, updatedAt = 200L
        )
        val ws2 = WorkspaceEntity(
            id = "ws-2", name = "Personal", createdAt = 100L, updatedAt = 200L
        )
        workspaceDao.insert(ws1)
        workspaceDao.insert(ws2)

        tabDao.insert(TabEntity(
            id = "tab-1", workspaceId = "ws-1", url = "https://a.com",
            createdAt = 100L, updatedAt = 200L
        ))
        tabDao.insert(TabEntity(
            id = "tab-2", workspaceId = "ws-1", url = "https://b.com",
            createdAt = 150L, updatedAt = 250L
        ))
        tabDao.insert(TabEntity(
            id = "tab-3", workspaceId = "ws-2", url = "https://c.com",
            createdAt = 200L, updatedAt = 300L
        ))

        tabDao.deleteByWorkspace("ws-1")

        val ws1Tabs = tabDao.getTabsByWorkspace("ws-1").first()
        val ws2Tabs = tabDao.getTabsByWorkspace("ws-2").first()
        assertThat(ws1Tabs).isEmpty()
        assertThat(ws2Tabs).hasSize(1)
    }

    @Test
    fun tabDao_insertReplacesOnConflict() = runBlocking {
        val ws = WorkspaceEntity(
            id = "ws-1", name = "Work", createdAt = 100L, updatedAt = 200L
        )
        workspaceDao.insert(ws)

        tabDao.insert(TabEntity(
            id = "tab-1", workspaceId = "ws-1", url = "https://old.com",
            title = "Old", createdAt = 100L, updatedAt = 200L
        ))

        tabDao.insert(TabEntity(
            id = "tab-1", workspaceId = "ws-1", url = "https://new.com",
            title = "New", createdAt = 100L, updatedAt = 300L
        ))

        val allTabs = tabDao.getAllTabs().first()
        assertThat(allTabs).hasSize(1)
        assertThat(allTabs[0].url).isEqualTo("https://new.com")
        assertThat(allTabs[0].title).isEqualTo("New")
        assertThat(allTabs[0].updatedAt).isEqualTo(300L)
    }

    // ════════════════════════════════════════════════
    // WorkspaceDao Tests
    // ════════════════════════════════════════════════

    @Test
    fun workspaceDao_insertAndGetAll() = runBlocking {
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-1", name = "Work", position = 0,
            createdAt = 100L, updatedAt = 200L
        ))
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-2", name = "Personal", position = 1,
            createdAt = 150L, updatedAt = 250L
        ))

        val workspaces = workspaceDao.getAllWorkspaces().first()
        assertThat(workspaces).hasSize(2)
        assertThat(workspaces[0].name).isEqualTo("Work")
        assertThat(workspaces[1].name).isEqualTo("Personal")
    }

    @Test
    fun workspaceDao_getById() = runBlocking {
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-1", name = "Work", createdAt = 100L, updatedAt = 200L
        ))

        val ws = workspaceDao.getWorkspaceById("ws-1").first()
        assertThat(ws).isNotNull()
        assertThat(ws!!.name).isEqualTo("Work")
    }

    @Test
    fun workspaceDao_getById_returnsNullForNonExistent() = runBlocking {
        val ws = workspaceDao.getWorkspaceById("non-existent").first()
        assertThat(ws).isNull()
    }

    @Test
    fun workspaceDao_update() = runBlocking {
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-1", name = "Old Name", createdAt = 100L, updatedAt = 200L
        ))

        workspaceDao.update(WorkspaceEntity(
            id = "ws-1", name = "New Name", createdAt = 100L, updatedAt = 300L
        ))

        val ws = workspaceDao.getWorkspaceById("ws-1").first()
        assertThat(ws).isNotNull()
        assertThat(ws!!.name).isEqualTo("New Name")
        assertThat(ws.updatedAt).isEqualTo(300L)
    }

    @Test
    fun workspaceDao_delete() = runBlocking {
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-1", name = "To Delete", createdAt = 100L, updatedAt = 200L
        ))

        workspaceDao.delete("ws-1")

        val ws = workspaceDao.getWorkspaceById("ws-1").first()
        assertThat(ws).isNull()
    }

    @Test
    fun workspaceDao_setDefaultWorkspace() = runBlocking {
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-1", name = "Work", isDefault = true,
            createdAt = 100L, updatedAt = 200L
        ))
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-2", name = "Personal", isDefault = false,
            createdAt = 150L, updatedAt = 250L
        ))

        // Switch default from ws-1 to ws-2
        workspaceDao.setDefaultWorkspace("ws-2")

        val ws1 = workspaceDao.getWorkspaceById("ws-1").first()!!
        val ws2 = workspaceDao.getWorkspaceById("ws-2").first()!!

        assertThat(ws1.isDefault).isFalse()
        assertThat(ws2.isDefault).isTrue()
    }

    @Test
    fun workspaceDao_getMaxPosition() = runBlocking {
        assertThat(workspaceDao.getMaxPosition()).isNull()

        workspaceDao.insert(WorkspaceEntity(
            id = "ws-1", name = "Work", position = 2,
            createdAt = 100L, updatedAt = 200L
        ))
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-2", name = "Personal", position = 5,
            createdAt = 150L, updatedAt = 250L
        ))
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-3", name = "Gaming", position = 3,
            createdAt = 200L, updatedAt = 300L
        ))

        val maxPos = workspaceDao.getMaxPosition()
        assertThat(maxPos).isEqualTo(5)
    }

    @Test
    fun workspaceDao_getMaxPosition_returnsNullWhenEmpty() = runBlocking {
        val maxPos = workspaceDao.getMaxPosition()
        assertThat(maxPos).isNull()
    }

    // ════════════════════════════════════════════════
    // ClosedTabHistoryDao Tests
    // ════════════════════════════════════════════════

    @Test
    fun closedTabHistoryDao_insertAndGetRecent() = runBlocking {
        val baseTime = 1000L
        for (i in 1..5) {
            closedTabHistoryDao.insert(ClosedTabHistoryEntity(
                id = "ct-$i",
                tabId = "tab-$i",
                url = "https://site$i.com",
                title = "Site $i",
                closedAt = baseTime + i
            ))
        }

        val recent = closedTabHistoryDao.getRecentClosedTabs().first()
        assertThat(recent).hasSize(5)
        // Should be in descending order by closedAt
        assertThat(recent[0].id).isEqualTo("ct-5")
        assertThat(recent[1].id).isEqualTo("ct-4")
        assertThat(recent[4].id).isEqualTo("ct-1")
    }

    @Test
    fun closedTabHistoryDao_delete() = runBlocking {
        closedTabHistoryDao.insert(ClosedTabHistoryEntity(
            id = "ct-1", tabId = "tab-1",
            url = "https://a.com", title = "A", closedAt = 1000L
        ))

        closedTabHistoryDao.delete("ct-1")

        val recent = closedTabHistoryDao.getRecentClosedTabs().first()
        assertThat(recent).isEmpty()
    }

    @Test
    fun closedTabHistoryDao_deleteAll() = runBlocking {
        for (i in 1..5) {
            closedTabHistoryDao.insert(ClosedTabHistoryEntity(
                id = "ct-$i", tabId = "tab-$i",
                url = "https://$i.com", title = "Tab $i", closedAt = i.toLong()
            ))
        }

        closedTabHistoryDao.deleteAll()

        val recent = closedTabHistoryDao.getRecentClosedTabs().first()
        assertThat(recent).isEmpty()
    }

    @Test
    fun closedTabHistoryDao_pruneOldEntries_keepsOnly20MostRecent() = runBlocking {
        val baseTime = 1000L
        for (i in 1..25) {
            closedTabHistoryDao.insert(ClosedTabHistoryEntity(
                id = "ct-$i", tabId = "tab-$i",
                url = "https://site$i.com", title = "Site $i",
                faviconUrl = "https://site$i.com/favicon.ico",
                closedAt = baseTime + i
            ))
        }

        closedTabHistoryDao.pruneOldEntries()

        val remaining = closedTabHistoryDao.getRecentClosedTabs().first()
        assertThat(remaining).hasSize(20)
        // The 20 most recent have the highest closedAt values (21-40, offset by baseTime)
        assertThat(remaining[0].id).isEqualTo("ct-25")
        assertThat(remaining[19].id).isEqualTo("ct-6")
        // ct-5 through ct-1 should be pruned
        assertThat(remaining.none { it.id == "ct-5" }).isTrue()
        assertThat(remaining.none { it.id == "ct-1" }).isTrue()
    }

    @Test
    fun closedTabHistoryDao_getRecentClosedTabs_limit20() = runBlocking {
        for (i in 1..25) {
            closedTabHistoryDao.insert(ClosedTabHistoryEntity(
                id = "ct-$i", tabId = "tab-$i",
                url = "https://site$i.com", title = "Site $i",
                closedAt = i.toLong()
            ))
        }

        val recent = closedTabHistoryDao.getRecentClosedTabs().first()
        assertThat(recent).hasSize(20)
    }

    // ════════════════════════════════════════════════
    // SessionSnapshotDao Tests
    // ════════════════════════════════════════════════

    @Test
    fun sessionSnapshotDao_insertAndGetAll() = runBlocking {
        val json1 = "{\"workspaceId\":\"ws-1\",\"tabs\":[{\"url\":\"https://a.com\"}]}"
        val json2 = "{\"workspaceId\":\"ws-2\",\"tabs\":[{\"url\":\"https://b.com\"}]}"

        sessionSnapshotDao.insert(SessionSnapshotEntity(
            id = "ss-1", name = "Morning", data = json1, createdAt = 1000L
        ))
        sessionSnapshotDao.insert(SessionSnapshotEntity(
            id = "ss-2", name = "Evening", data = json2, createdAt = 2000L
        ))

        val sessions = sessionSnapshotDao.getAllSessions().first()
        assertThat(sessions).hasSize(2)
        // Ordered by createdAt DESC
        assertThat(sessions[0].id).isEqualTo("ss-2")
        assertThat(sessions[1].id).isEqualTo("ss-1")
    }

    @Test
    fun sessionSnapshotDao_dataIntegrity() = runBlocking {
        val json = buildString {
            append("{\"workspaceId\":\"ws-1\",\"tabs\":[")
            for (i in 1..10) {
                if (i > 1) append(",")
                append("{\"url\":\"https://site$i.com\",\"title\":\"Site $i\",\"scrollY\":$i\"00\"}")
            }
            append("]}")
        }

        sessionSnapshotDao.insert(SessionSnapshotEntity(
            id = "ss-integrity", name = "Integrity Test",
            data = json, createdAt = 5000L
        ))

        val sessions = sessionSnapshotDao.getAllSessions().first()
        assertThat(sessions).hasSize(1)
        assertThat(sessions[0].data).isEqualTo(json)
        assertThat(sessions[0].name).isEqualTo("Integrity Test")
    }

    @Test
    fun sessionSnapshotDao_delete() = runBlocking {
        sessionSnapshotDao.insert(SessionSnapshotEntity(
            id = "ss-1", name = "To Delete",
            data = "{}", createdAt = 1000L
        ))

        sessionSnapshotDao.delete("ss-1")

        val sessions = sessionSnapshotDao.getAllSessions().first()
        assertThat(sessions).isEmpty()
    }

    @Test
    fun sessionSnapshotDao_insertReplacesOnConflict() = runBlocking {
        sessionSnapshotDao.insert(SessionSnapshotEntity(
            id = "ss-1", name = "Old Name",
            data = "{\"old\":true}", createdAt = 1000L
        ))

        sessionSnapshotDao.insert(SessionSnapshotEntity(
            id = "ss-1", name = "New Name",
            data = "{\"new\":true}", createdAt = 2000L
        ))

        val sessions = sessionSnapshotDao.getAllSessions().first()
        assertThat(sessions).hasSize(1)
        assertThat(sessions[0].name).isEqualTo("New Name")
        assertThat(sessions[0].data).isEqualTo("{\"new\":true}")
    }

    // ════════════════════════════════════════════════
    // Workspace-Tab Relationship Tests
    // ════════════════════════════════════════════════

    @Test
    fun workspaceDeletion_cascadesToTabs() = runBlocking {
        // Create workspace and tabs
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-cascade", name = "Cascade Test",
            createdAt = 100L, updatedAt = 200L
        ))

        for (i in 1..3) {
            tabDao.insert(TabEntity(
                id = "tab-cascade-$i",
                workspaceId = "ws-cascade",
                url = "https://cascade$i.com",
                position = i - 1,
                createdAt = 100L + i,
                updatedAt = 200L + i
            ))
        }

        // Verify tabs exist
        val tabsBefore = tabDao.getTabsByWorkspace("ws-cascade").first()
        assertThat(tabsBefore).hasSize(3)

        // Delete workspace
        workspaceDao.delete("ws-cascade")

        // Verify workspace is gone
        val ws = workspaceDao.getWorkspaceById("ws-cascade").first()
        assertThat(ws).isNull()

        // Verify all tabs are cascade-deleted
        val tabsAfter = tabDao.getAllTabs().first()
        assertThat(tabsAfter).isEmpty()
    }

    @Test
    fun workspaceDeletion_cascadeDoesNotAffectOtherWorkspaces() = runBlocking {
        // Create two workspaces
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-keep", name = "Keep Me",
            createdAt = 100L, updatedAt = 200L
        ))
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-delete", name = "Delete Me",
            createdAt = 100L, updatedAt = 200L
        ))

        // Add tabs to both
        tabDao.insert(TabEntity(
            id = "tab-keep-1",
            workspaceId = "ws-keep",
            url = "https://keep.com",
            createdAt = 100L, updatedAt = 200L
        ))
        tabDao.insert(TabEntity(
            id = "tab-delete-1",
            workspaceId = "ws-delete",
            url = "https://delete.com",
            createdAt = 100L, updatedAt = 200L
        ))

        // Delete only ws-delete
        workspaceDao.delete("ws-delete")

        // Verify ws-keep and its tab still exist
        val keptWs = workspaceDao.getWorkspaceById("ws-keep").first()
        assertThat(keptWs).isNotNull()

        val keptTabs = tabDao.getTabsByWorkspace("ws-keep").first()
        assertThat(keptTabs).hasSize(1)
        assertThat(keptTabs[0].id).isEqualTo("tab-keep-1")

        // Verify deleted workspace's tab is gone
        val allTabs = tabDao.getAllTabs().first()
        assertThat(allTabs).hasSize(1)
    }

    // ════════════════════════════════════════════════
    // Workspace/Tab Persistence Tests
    // ════════════════════════════════════════════════

    @Test
    fun workspaceAndTab_persistence_endToEnd() = runBlocking {
        // Create workspace
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-e2e", name = "E2E Test",
            themeMode = "dark", accentColor = "#FF5722",
            position = 0, createdAt = 100L, updatedAt = 200L
        ))

        // Add tabs
        val tabIds = (1..3).map { i ->
            val id = "tab-e2e-$i"
            tabDao.insert(TabEntity(
                id = id,
                workspaceId = "ws-e2e",
                url = "https://e2e$i.com",
                title = "E2E Tab $i",
                faviconUrl = "https://e2e$i.com/favicon.ico",
                position = i - 1,
                createdAt = (100L + i * 10),
                updatedAt = (200L + i * 10)
            ))
            id
        }

        // Verify all tabs for workspace
        val tabs = tabDao.getTabsByWorkspace("ws-e2e").first()
        assertThat(tabs).hasSize(3)
        assertThat(tabs.map { it.id }).containsExactly(*tabIds.toTypedArray()).inOrder()
        assertThat(tabs[0].title).isEqualTo("E2E Tab 1")
        assertThat(tabs[1].title).isEqualTo("E2E Tab 2")
        assertThat(tabs[2].title).isEqualTo("E2E Tab 3")
    }

    @Test
    fun switchWorkspaceContext_showsDifferentTabs() = runBlocking {
        // Create two workspaces
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-a", name = "Workspace A",
            createdAt = 100L, updatedAt = 200L
        ))
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-b", name = "Workspace B",
            createdAt = 100L, updatedAt = 200L
        ))

        // Add tabs to workspace A
        tabDao.insert(TabEntity(
            id = "tab-a1", workspaceId = "ws-a",
            url = "https://a1.com", title = "A Tab 1",
            position = 0, createdAt = 100L, updatedAt = 200L
        ))
        tabDao.insert(TabEntity(
            id = "tab-a2", workspaceId = "ws-a",
            url = "https://a2.com", title = "A Tab 2",
            position = 1, createdAt = 110L, updatedAt = 210L
        ))

        // Add tabs to workspace B
        tabDao.insert(TabEntity(
            id = "tab-b1", workspaceId = "ws-b",
            url = "https://b1.com", title = "B Tab 1",
            position = 0, createdAt = 200L, updatedAt = 300L
        ))

        // Switch to workspace A
        val tabsA = tabDao.getTabsByWorkspace("ws-a").first()
        assertThat(tabsA).hasSize(2)
        assertThat(tabsA.map { it.id }).containsExactly("tab-a1", "tab-a2").inOrder()

        // Switch to workspace B
        val tabsB = tabDao.getTabsByWorkspace("ws-b").first()
        assertThat(tabsB).hasSize(1)
        assertThat(tabsB[0].id).isEqualTo("tab-b1")
    }

    @Test
    fun deleteWorkspace_verifiesTabsGone() = runBlocking {
        workspaceDao.insert(WorkspaceEntity(
            id = "ws-del", name = "To Delete",
            createdAt = 100L, updatedAt = 200L
        ))

        tabDao.insert(TabEntity(
            id = "tab-del-1", workspaceId = "ws-del",
            url = "https://del.com", title = "Del Tab",
            position = 0, createdAt = 100L, updatedAt = 200L
        ))

        // Verify tab exists
        assertThat(tabDao.getTabsByWorkspace("ws-del").first()).hasSize(1)

        // Delete workspace
        workspaceDao.delete("ws-del")

        // Verify tab is gone (cascade delete)
        assertThat(tabDao.getTabsByWorkspace("ws-del").first()).isEmpty()
        assertThat(tabDao.getTabById("tab-del-1").first()).isNull()
    }

    // ════════════════════════════════════════════════
    // Recently Closed Tabs - Chronological Order
    // ════════════════════════════════════════════════

    @Test
    fun recentlyClosedTabs_returnsInReverseChronologicalOrder() = runBlocking {
        val timestamps = listOf(5000L, 1000L, 3000L, 2000L, 4000L)
        timestamps.forEachIndexed { index, ts ->
            closedTabHistoryDao.insert(ClosedTabHistoryEntity(
                id = "ct-chrono-$index",
                tabId = "tab-chrono-$index",
                url = "https://chrono$index.com",
                title = "Chrono $index",
                closedAt = ts
            ))
        }

        val recent = closedTabHistoryDao.getRecentClosedTabs().first()
        assertThat(recent).hasSize(5)
        // Verify descending order by closedAt
        for (i in 0 until recent.size - 1) {
            assertThat(recent[i].closedAt).isAtLeast(recent[i + 1].closedAt)
        }
        // First entry should have the highest timestamp (5000)
        assertThat(recent[0].closedAt).isEqualTo(5000L)
        // Last entry should have the lowest timestamp (1000)
        assertThat(recent[4].closedAt).isEqualTo(1000L)
    }

    @Test
    fun recentlyClosedTabs_withFaviconUrl_preservesFavicon() = runBlocking {
        closedTabHistoryDao.insert(ClosedTabHistoryEntity(
            id = "ct-fav", tabId = "tab-fav",
            url = "https://fav.com", title = "Has Favicon",
            faviconUrl = "https://fav.com/icon.png",
            closedAt = 1000L
        ))

        val recent = closedTabHistoryDao.getRecentClosedTabs().first()
        assertThat(recent).hasSize(1)
        assertThat(recent[0].faviconUrl).isEqualTo("https://fav.com/icon.png")
    }

    @Test
    fun recentlyClosedTabs_withoutFaviconUrl_isNull() = runBlocking {
        closedTabHistoryDao.insert(ClosedTabHistoryEntity(
            id = "ct-nofav", tabId = "tab-nofav",
            url = "https://nofav.com", title = "No Favicon",
            faviconUrl = null,
            closedAt = 1000L
        ))

        val recent = closedTabHistoryDao.getRecentClosedTabs().first()
        assertThat(recent).hasSize(1)
        assertThat(recent[0].faviconUrl).isNull()
    }

    // ════════════════════════════════════════════════
    // Session Restoration Tests
    // ════════════════════════════════════════════════

    @Test
    fun sessionSnapshot_insertAndRetrieveWithJsonData() = runBlocking {
        val sessionJson = """
            {
                "workspaceId": "ws-restore",
                "workspaceName": "Restored Session",
                "tabs": [
                    {
                        "url": "https://restored1.com",
                        "title": "Restored Tab 1",
                        "scrollY": 500
                    },
                    {
                        "url": "https://restored2.com",
                        "title": "Restored Tab 2",
                        "scrollY": 0
                    }
                ]
            }
        """.trimIndent()

        sessionSnapshotDao.insert(SessionSnapshotEntity(
            id = "ss-restore",
            name = "Auto-saved session",
            data = sessionJson,
            createdAt = System.currentTimeMillis()
        ))

        val sessions = sessionSnapshotDao.getAllSessions().first()
        assertThat(sessions).hasSize(1)
        assertThat(sessions[0].data).isEqualTo(sessionJson)
        assertThat(sessions[0].data).contains("https://restored1.com")
        assertThat(sessions[0].data).contains("Restored Tab 2")
        assertThat(sessions[0].data).contains("\"scrollY\": 500")
    }

    @Test
    fun sessionSnapshot_deleteAndVerifyGone() = runBlocking {
        sessionSnapshotDao.insert(SessionSnapshotEntity(
            id = "ss-gone", name = "Will Be Deleted",
            data = "{}", createdAt = 1000L
        ))
        sessionSnapshotDao.insert(SessionSnapshotEntity(
            id = "ss-keep", name = "Will Stay",
            data = "{}", createdAt = 2000L
        ))

        sessionSnapshotDao.delete("ss-gone")

        val sessions = sessionSnapshotDao.getAllSessions().first()
        assertThat(sessions).hasSize(1)
        assertThat(sessions[0].id).isEqualTo("ss-keep")
    }

    @Test
    fun sessionSnapshot_multipleSnapshots_orderedByCreationTime() = runBlocking {
        sessionSnapshotDao.insert(SessionSnapshotEntity(
            id = "ss-oldest", name = "Oldest", data = "{}", createdAt = 1000L
        ))
        sessionSnapshotDao.insert(SessionSnapshotEntity(
            id = "ss-newest", name = "Newest", data = "{}", createdAt = 3000L
        ))
        sessionSnapshotDao.insert(SessionSnapshotEntity(
            id = "ss-middle", name = "Middle", data = "{}", createdAt = 2000L
        ))

        val sessions = sessionSnapshotDao.getAllSessions().first()
        assertThat(sessions).hasSize(3)
        assertThat(sessions[0].id).isEqualTo("ss-newest")
        assertThat(sessions[1].id).isEqualTo("ss-middle")
        assertThat(sessions[2].id).isEqualTo("ss-oldest")
    }
}
