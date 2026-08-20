package com.pratham.webhub.domain.model

import com.pratham.webhub.data.db.entity.BookmarkEntity
import com.pratham.webhub.data.db.entity.ClosedTabHistoryEntity
import com.pratham.webhub.data.db.entity.SessionSnapshotEntity
import com.pratham.webhub.data.db.entity.TabEntity
import com.pratham.webhub.data.db.entity.WorkspaceEntity
import org.junit.Assert.*
import org.junit.Test

/**
 * Pure JVM unit tests for domain model mapping logic.
 * These tests verify that fromEntity() and toEntity() conversions are correct
 * and that default values are properly preserved.
 */
class DomainModelTest {

    // ──────────────────────────────────────────────
    // Tab domain model tests
    // ──────────────────────────────────────────────

    @Test
    fun `Tab fromEntity maps all fields correctly`() {
        val entity = TabEntity(
            id = "tab-1",
            workspaceId = "ws-1",
            url = "https://example.com",
            title = "Example",
            faviconUrl = "https://example.com/favicon.ico",
            customName = "My Custom Tab",
            customIconUri = "content://media/custom_icon.png",
            isJsEnabled = false,
            isAdBlockEnabled = false,
            cssOverride = "body { background: red }",
            userScript = "console.log('hello')",
            position = 3,
            isHibernated = true,
            savedScrollY = 450,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val domain = Tab.fromEntity(entity)

        assertEquals("tab-1", domain.id)
        assertEquals("ws-1", domain.workspaceId)
        assertEquals("https://example.com", domain.url)
        assertEquals("Example", domain.title)
        assertEquals("https://example.com/favicon.ico", domain.faviconUrl)
        assertEquals("My Custom Tab", domain.customName)
        assertEquals("content://media/custom_icon.png", domain.customIconUri)
        assertFalse(domain.isJsEnabled)
        assertFalse(domain.isAdBlockEnabled)
        assertEquals("body { background: red }", domain.cssOverride)
        assertEquals("console.log('hello')", domain.userScript)
        assertEquals(3, domain.position)
        assertTrue(domain.isHibernated)
        assertEquals(450, domain.savedScrollY)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.updatedAt)
    }

    @Test
    fun `Tab fromEntity preserves default values`() {
        val entity = TabEntity(
            id = "tab-defaults",
            workspaceId = "ws-1",
            url = "https://example.com",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val domain = Tab.fromEntity(entity)

        assertEquals("", domain.title)
        assertNull(domain.faviconUrl)
        assertNull(domain.customName)
        assertNull(domain.customIconUri)
        assertTrue(domain.isJsEnabled)
        assertTrue(domain.isAdBlockEnabled)
        assertNull(domain.cssOverride)
        assertNull(domain.userScript)
        assertEquals(0, domain.position)
        assertFalse(domain.isHibernated)
        assertEquals(0, domain.savedScrollY)
    }

    @Test
    fun `Tab toEntity maps all fields correctly`() {
        val domain = Tab(
            id = "tab-2",
            workspaceId = "ws-2",
            url = "https://test.com",
            title = "Test Page",
            faviconUrl = "https://test.com/icon.png",
            customName = "Custom",
            customIconUri = "content://icon",
            isJsEnabled = false,
            isAdBlockEnabled = true,
            cssOverride = "* { display: none }",
            userScript = "alert(1)",
            position = 5,
            isHibernated = true,
            savedScrollY = 999,
            createdAt = 3000L,
            updatedAt = 4000L
        )

        val entity = domain.toEntity()

        assertEquals("tab-2", entity.id)
        assertEquals("ws-2", entity.workspaceId)
        assertEquals("https://test.com", entity.url)
        assertEquals("Test Page", entity.title)
        assertEquals("https://test.com/icon.png", entity.faviconUrl)
        assertEquals("Custom", entity.customName)
        assertEquals("content://icon", entity.customIconUri)
        assertFalse(entity.isJsEnabled)
        assertTrue(entity.isAdBlockEnabled)
        assertEquals("* { display: none }", entity.cssOverride)
        assertEquals("alert(1)", entity.userScript)
        assertEquals(5, entity.position)
        assertTrue(entity.isHibernated)
        assertEquals(999, entity.savedScrollY)
        assertEquals(3000L, entity.createdAt)
        assertEquals(4000L, entity.updatedAt)
    }

    @Test
    fun `Tab round-trip entity-to-domain-to-entity preserves all fields`() {
        val original = TabEntity(
            id = "tab-rt",
            workspaceId = "ws-rt",
            url = "https://roundtrip.com",
            title = "Round Trip",
            faviconUrl = "https://roundtrip.com/fav.ico",
            customName = "RT Name",
            customIconUri = "content://rt/icon",
            isJsEnabled = false,
            isAdBlockEnabled = false,
            cssOverride = "html { color: blue }",
            userScript = "// script",
            position = 7,
            isHibernated = true,
            savedScrollY = 1234,
            createdAt = 5000L,
            updatedAt = 6000L
        )

        val domain = Tab.fromEntity(original)
        val roundTripped = domain.toEntity()

        assertEquals(original, roundTripped)
    }

    @Test
    fun `Tab round-trip domain-to-entity-to-domain preserves all fields`() {
        val original = Tab(
            id = "tab-rt2",
            workspaceId = "ws-rt2",
            url = "https://roundtrip2.com",
            title = "RT2",
            faviconUrl = "https://roundtrip2.com/f.png",
            customName = "RT2 Name",
            customIconUri = "content://rt2/i",
            isJsEnabled = true,
            isAdBlockEnabled = false,
            cssOverride = ".cls { x: 1 }",
            userScript = "var x = 1;",
            position = 2,
            isHibernated = false,
            savedScrollY = 0,
            createdAt = 7000L,
            updatedAt = 8000L
        )

        val entity = original.toEntity()
        val roundTripped = Tab.fromEntity(entity)

        assertEquals(original, roundTripped)
    }

    @Test
    fun `Tab data class equality works correctly`() {
        val tab1 = Tab(
            id = "t1", workspaceId = "w1", url = "https://a.com",
            createdAt = 1L, updatedAt = 2L
        )
        val tab2 = Tab(
            id = "t1", workspaceId = "w1", url = "https://a.com",
            createdAt = 1L, updatedAt = 2L
        )
        val tab3 = Tab(
            id = "t3", workspaceId = "w1", url = "https://a.com",
            createdAt = 1L, updatedAt = 2L
        )

        assertEquals(tab1, tab2)
        assertNotEquals(tab1, tab3)
    }

    @Test
    fun `Tab copy creates independent instance`() {
        val tab = Tab(
            id = "tab-copy", workspaceId = "ws-copy", url = "https://copy.com",
            title = "Original", position = 1,
            createdAt = 100L, updatedAt = 200L
        )

        val copied = tab.copy(title = "Modified")

        assertEquals("Original", tab.title)
        assertEquals("Modified", copied.title)
        assertEquals(tab.id, copied.id)
    }

    // ──────────────────────────────────────────────
    // Workspace domain model tests
    // ──────────────────────────────────────────────

    @Test
    fun `Workspace fromEntity maps all fields correctly`() {
        val entity = WorkspaceEntity(
            id = "ws-1",
            name = "Personal",
            themeMode = "dark",
            accentColor = "#FF5722",
            position = 2,
            isDefault = true,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val domain = Workspace.fromEntity(entity)

        assertEquals("ws-1", domain.id)
        assertEquals("Personal", domain.name)
        assertEquals("dark", domain.themeMode)
        assertEquals("#FF5722", domain.accentColor)
        assertEquals(2, domain.position)
        assertTrue(domain.isDefault)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.updatedAt)
    }

    @Test
    fun `Workspace fromEntity preserves default values`() {
        val entity = WorkspaceEntity(
            id = "ws-def",
            name = "Default WS",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val domain = Workspace.fromEntity(entity)

        assertEquals("system", domain.themeMode)
        assertNull(domain.accentColor)
        assertEquals(0, domain.position)
        assertFalse(domain.isDefault)
    }

    @Test
    fun `Workspace toEntity maps all fields correctly`() {
        val domain = Workspace(
            id = "ws-2",
            name = "Work",
            themeMode = "light",
            accentColor = "#2196F3",
            position = 5,
            isDefault = false,
            createdAt = 3000L,
            updatedAt = 4000L
        )

        val entity = domain.toEntity()

        assertEquals("ws-2", entity.id)
        assertEquals("Work", entity.name)
        assertEquals("light", entity.themeMode)
        assertEquals("#2196F3", entity.accentColor)
        assertEquals(5, entity.position)
        assertFalse(entity.isDefault)
        assertEquals(3000L, entity.createdAt)
        assertEquals(4000L, entity.updatedAt)
    }

    @Test
    fun `Workspace round-trip entity-to-domain-to-entity preserves all fields`() {
        val original = WorkspaceEntity(
            id = "ws-rt",
            name = "Round Trip",
            themeMode = "dark",
            accentColor = "#00BCD4",
            position = 3,
            isDefault = true,
            createdAt = 5000L,
            updatedAt = 6000L
        )

        val domain = Workspace.fromEntity(original)
        val roundTripped = domain.toEntity()

        assertEquals(original, roundTripped)
    }

    @Test
    fun `Workspace round-trip domain-to-entity-to-domain preserves all fields`() {
        val original = Workspace(
            id = "ws-rt2",
            name = "RT2",
            themeMode = "light",
            accentColor = "#E91E63",
            position = 1,
            isDefault = false,
            createdAt = 7000L,
            updatedAt = 8000L
        )

        val entity = original.toEntity()
        val roundTripped = Workspace.fromEntity(entity)

        assertEquals(original, roundTripped)
    }

    @Test
    fun `Workspace default themeMode is system`() {
        val ws = Workspace(
            id = "ws-theme",
            name = "Theme Test",
            createdAt = 1L,
            updatedAt = 2L
        )
        assertEquals("system", ws.themeMode)
    }

    // ──────────────────────────────────────────────
    // SessionSnapshot domain model tests
    // ──────────────────────────────────────────────

    @Test
    fun `SessionSnapshot fromEntity maps all fields correctly`() {
        val entity = SessionSnapshotEntity(
            id = "session-1",
            name = "Morning Session",
            data = "{\"tabs\":[{\"url\":\"https://example.com\"}]}",
            createdAt = 1000L
        )

        val domain = SessionSnapshot.fromEntity(entity)

        assertEquals("session-1", domain.id)
        assertEquals("Morning Session", domain.name)
        assertEquals("{\"tabs\":[{\"url\":\"https://example.com\"}]}", domain.data)
        assertEquals(1000L, domain.createdAt)
    }

    @Test
    fun `SessionSnapshot toEntity maps all fields correctly`() {
        val domain = SessionSnapshot(
            id = "session-2",
            name = "Evening Session",
            data = "{\"tabs\":[]}",
            createdAt = 2000L
        )

        val entity = domain.toEntity()

        assertEquals("session-2", entity.id)
        assertEquals("Evening Session", entity.name)
        assertEquals("{\"tabs\":[]}", entity.data)
        assertEquals(2000L, entity.createdAt)
    }

    @Test
    fun `SessionSnapshot round-trip entity-to-domain-to-entity`() {
        val original = SessionSnapshotEntity(
            id = "ss-rt",
            name = "Round Trip Session",
            data = "{\"workspaceId\":\"ws-1\",\"tabs\":[]}",
            createdAt = 9999L
        )

        val domain = SessionSnapshot.fromEntity(original)
        val roundTripped = domain.toEntity()

        assertEquals(original, roundTripped)
    }

    @Test
    fun `SessionSnapshot round-trip domain-to-entity-to-domain`() {
        val original = SessionSnapshot(
            id = "ss-rt2",
            name = "RT2 Session",
            data = "{}",
            createdAt = 8888L
        )

        val entity = original.toEntity()
        val roundTripped = SessionSnapshot.fromEntity(entity)

        assertEquals(original, roundTripped)
    }

    @Test
    fun `SessionSnapshot can hold large JSON data`() {
        val largeData = buildString {
            append("{\"tabs\":[")
            for (i in 1..100) {
                if (i > 1) append(",")
                append("{\"url\":\"https://site$i.com\",\"title\":\"Site $i\"}")
            }
            append("]}")
        }

        val snapshot = SessionSnapshot(
            id = "ss-large",
            name = "Large Session",
            data = largeData,
            createdAt = 10000L
        )

        val entity = snapshot.toEntity()
        val roundTripped = SessionSnapshot.fromEntity(entity)

        assertEquals(largeData, roundTripped.data)
    }
    // ──────────────────────────────────────────────
    // ClosedTab domain model tests
    // ──────────────────────────────────────────────

    @Test
    fun `ClosedTab fromEntity maps all fields correctly`() {
        val entity = ClosedTabHistoryEntity(
            id = "ct-1",
            tabId = "tab-1",
            url = "https://closed.com",
            title = "Closed Tab",
            faviconUrl = "https://closed.com/fav.ico",
            closedAt = 5000L
        )

        val domain = ClosedTab.fromEntity(entity)

        assertEquals("ct-1", domain.id)
        assertEquals("tab-1", domain.tabId)
        assertEquals("https://closed.com", domain.url)
        assertEquals("Closed Tab", domain.title)
        assertEquals("https://closed.com/fav.ico", domain.faviconUrl)
        assertEquals(5000L, domain.closedAt)
    }

    @Test
    fun `ClosedTab fromEntity preserves null faviconUrl`() {
        val entity = ClosedTabHistoryEntity(
            id = "ct-2",
            tabId = "tab-2",
            url = "https://noclosed.com",
            title = "No Favicon",
            faviconUrl = null,
            closedAt = 6000L
        )

        val domain = ClosedTab.fromEntity(entity)
        assertNull(domain.faviconUrl)
    }

    @Test
    fun `ClosedTab round-trip preserves all fields`() {
        val original = ClosedTab(
            id = "ct-rt",
            tabId = "tab-rt",
            url = "https://rt.com",
            title = "RT",
            faviconUrl = "https://rt.com/f.png",
            closedAt = 7000L
        )

        val entity = original.toEntity()
        val roundTripped = ClosedTab.fromEntity(entity)

        assertEquals(original, roundTripped)
    }

    // ──────────────────────────────────────────────
    // Bookmark domain model tests
    // ──────────────────────────────────────────────

    @Test
    fun `Bookmark fromEntity maps all fields correctly`() {
        val entity = BookmarkEntity(
            id = "bm-1",
            url = "https://bookmark.com",
            title = "My Bookmark",
            faviconUrl = "https://bookmark.com/icon.png",
            createdAt = 3000L
        )

        val domain = Bookmark.fromEntity(entity)

        assertEquals("bm-1", domain.id)
        assertEquals("https://bookmark.com", domain.url)
        assertEquals("My Bookmark", domain.title)
        assertEquals("https://bookmark.com/icon.png", domain.faviconUrl)
        assertEquals(3000L, domain.createdAt)
    }

    @Test
    fun `Bookmark fromEntity preserves null faviconUrl`() {
        val entity = BookmarkEntity(
            id = "bm-2",
            url = "https://nofav.com",
            title = "No Favicon",
            faviconUrl = null,
            createdAt = 4000L
        )

        val domain = Bookmark.fromEntity(entity)
        assertNull(domain.faviconUrl)
    }

    @Test
    fun `Bookmark round-trip preserves all fields`() {
        val original = Bookmark(
            id = "bm-rt",
            url = "https://rtbm.com",
            title = "RT Bookmark",
            faviconUrl = "https://rtbm.com/f.png",
            createdAt = 8000L
        )

        val entity = original.toEntity()
        val roundTripped = Bookmark.fromEntity(entity)

        assertEquals(original, roundTripped)
    }

    @Test
    fun `Bookmark round-trip with null favicon preserves null`() {
        val original = Bookmark(
            id = "bm-nf",
            url = "https://nullfav.com",
            title = "Null Favicon",
            faviconUrl = null,
            createdAt = 9000L
        )

        val entity = original.toEntity()
        val roundTripped = Bookmark.fromEntity(entity)

        assertEquals(original, roundTripped)
        assertNull(roundTripped.faviconUrl)
    }

    // ──────────────────────────────────────────────
    // Edge cases
    // ──────────────────────────────────────────────

    @Test
    fun `Tab with empty URL is valid for domain model`() {
        val tab = Tab(
            id = "empty-url",
            workspaceId = "ws",
            url = "",
            createdAt = 1L,
            updatedAt = 2L
        )
        assertEquals("", tab.url)
    }

    @Test
    fun `Tab with very long URL and title`() {
        val longUrl = "https://example.com/" + "a".repeat(10000)
        val longTitle = "X".repeat(5000)

        val tab = Tab(
            id = "long-tab",
            workspaceId = "ws",
            url = longUrl,
            title = longTitle,
            createdAt = 1L,
            updatedAt = 2L
        )

        assertEquals(longUrl.length, tab.url.length)
        assertEquals(longTitle.length, tab.title.length)

        val entity = tab.toEntity()
        val roundTripped = Tab.fromEntity(entity)
        assertEquals(longUrl, roundTripped.url)
        assertEquals(longTitle, roundTripped.title)
    }

    @Test
    fun `Workspace with special characters in name`() {
        val ws = Workspace(
            id = "ws-special",
            name = "Work\n\t\"Quotes\" & <Tags>",
            createdAt = 1L,
            updatedAt = 2L
        )

        val entity = ws.toEntity()
        val roundTripped = Workspace.fromEntity(entity)
        assertEquals(ws.name, roundTripped.name)
    }

    @Test
    fun `SessionSnapshot with empty data string`() {
        val snapshot = SessionSnapshot(
            id = "ss-empty",
            name = "Empty Data",
            data = "",
            createdAt = 1L
        )

        val entity = snapshot.toEntity()
        val roundTripped = SessionSnapshot.fromEntity(entity)
        assertEquals("", roundTripped.data)
    }

    @Test
    fun `Multiple tabs with same workspace have different identities`() {
        val tab1 = Tab(
            id = "tab-a", workspaceId = "ws-1", url = "https://a.com",
            position = 0, createdAt = 1L, updatedAt = 2L
        )
        val tab2 = Tab(
            id = "tab-b", workspaceId = "ws-1", url = "https://b.com",
            position = 1, createdAt = 3L, updatedAt = 4L
        )

        assertEquals(tab1.workspaceId, tab2.workspaceId)
        assertNotEquals(tab1, tab2)
        assertNotEquals(tab1.id, tab2.id)
    }

    @Test
    fun `Tab boolean flags can be independently set`() {
        val jsEnabledAdBlocked = Tab(
            id = "t1", workspaceId = "w1", url = "https://a.com",
            isJsEnabled = true, isAdBlockEnabled = true,
            createdAt = 1L, updatedAt = 2L
        )
        val jsEnabledAdAllowed = Tab(
            id = "t2", workspaceId = "w1", url = "https://a.com",
            isJsEnabled = true, isAdBlockEnabled = false,
            createdAt = 1L, updatedAt = 2L
        )
        val jsDisabledAdBlocked = Tab(
            id = "t3", workspaceId = "w1", url = "https://a.com",
            isJsEnabled = false, isAdBlockEnabled = true,
            createdAt = 1L, updatedAt = 2L
        )
        val jsDisabledAdAllowed = Tab(
            id = "t4", workspaceId = "w1", url = "https://a.com",
            isJsEnabled = false, isAdBlockEnabled = false,
            createdAt = 1L, updatedAt = 2L
        )

        assertTrue(jsEnabledAdBlocked.isJsEnabled && jsEnabledAdBlocked.isAdBlockEnabled)
        assertTrue(jsEnabledAdAllowed.isJsEnabled && !jsEnabledAdAllowed.isAdBlockEnabled)
        assertTrue(!jsDisabledAdBlocked.isJsEnabled && jsDisabledAdBlocked.isAdBlockEnabled)
        assertTrue(!jsDisabledAdAllowed.isJsEnabled && !jsDisabledAdAllowed.isAdBlockEnabled)
    }

    @Test
    fun `Tab position ordering is preserved through round-trip`() {
        val tabs = (0..9).map { i ->
            Tab(
                id = "tab-pos-$i",
                workspaceId = "ws-pos",
                url = "https://$i.com",
                position = i,
                createdAt = i.toLong(),
                updatedAt = (i + 100).toLong()
            )
        }

        val entities = tabs.map { it.toEntity() }
        val roundTripped = entities.map { Tab.fromEntity(it) }

        val positions = roundTripped.map { it.position }
        assertEquals((0..9).toList(), positions)
    }
}
