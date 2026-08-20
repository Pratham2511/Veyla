package com.pratham.webhub.data.repository

import com.pratham.webhub.data.db.dao.ClosedTabHistoryDao
import com.pratham.webhub.data.db.dao.TabDao
import com.pratham.webhub.data.db.dao.WorkspaceDao
import com.pratham.webhub.data.db.entity.TabEntity
import com.pratham.webhub.domain.model.ClosedTab
import com.pratham.webhub.domain.model.Tab
import com.pratham.webhub.domain.repository.ClosedTabRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClosedTabRepositoryImpl @Inject constructor(
    private val closedTabHistoryDao: ClosedTabHistoryDao,
    private val tabDao: TabDao,
    private val workspaceDao: WorkspaceDao
) : ClosedTabRepository {

    override fun getClosedTabs(): Flow<List<ClosedTab>> {
        return closedTabHistoryDao.getRecentClosedTabs().map { entities ->
            entities.map { ClosedTab.fromEntity(it) }
        }
    }

    override suspend fun insertClosedTab(tab: Tab) {
        closedTabHistoryDao.insert(
            com.pratham.webhub.data.db.entity.ClosedTabHistoryEntity(
                id = UUID.randomUUID().toString(),
                tabId = tab.id,
                url = tab.url,
                title = tab.title,
                faviconUrl = tab.faviconUrl,
                closedAt = System.currentTimeMillis()
            )
        )
        closedTabHistoryDao.pruneOldEntries()
    }

    override suspend fun restoreTab(tabId: String): String {
        val closedTabs = closedTabHistoryDao.getRecentClosedTabs().first()
        val closedEntry = closedTabs.firstOrNull { it.tabId == tabId }
            ?: throw IllegalArgumentException("Closed tab not found: $tabId")

        // Determine the workspace to restore into: use the default workspace
        val workspaces = workspaceDao.getAllWorkspaces().first()
        val targetWorkspace = workspaces.firstOrNull { it.isDefault } ?: workspaces.first()

        val now = System.currentTimeMillis()
        val existingTabs = tabDao.getTabsByWorkspace(targetWorkspace.id).first()
        val maxPosition = existingTabs.maxOfOrNull { it.position } ?: -1

        val restoredTab = TabEntity(
            id = UUID.randomUUID().toString(),
            workspaceId = targetWorkspace.id,
            url = closedEntry.url,
            title = closedEntry.title,
            faviconUrl = closedEntry.faviconUrl,
            position = maxPosition + 1,
            createdAt = now,
            updatedAt = now
        )
        tabDao.insert(restoredTab)

        // Remove from closed tab history
        closedTabHistoryDao.delete(closedEntry.id)

        return restoredTab.id
    }

    override suspend fun clearClosedTabs() {
        closedTabHistoryDao.deleteAll()
    }
}
