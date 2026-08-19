package com.pratham.webhub.data.repository

import com.pratham.webhub.data.db.dao.ClosedTabHistoryDao
import com.pratham.webhub.data.db.dao.TabDao
import com.pratham.webhub.data.db.entity.ClosedTabHistoryEntity
import com.pratham.webhub.data.db.entity.TabEntity
import com.pratham.webhub.domain.model.Tab
import com.pratham.webhub.domain.repository.TabRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TabRepositoryImpl @Inject constructor(
    private val tabDao: TabDao,
    private val closedTabHistoryDao: ClosedTabHistoryDao
) : TabRepository {

    override fun getTabsForWorkspace(workspaceId: String): Flow<List<Tab>> {
        return tabDao.getTabsByWorkspace(workspaceId).map { entities ->
            entities.map { Tab.fromEntity(it) }
        }
    }

    override fun getTab(tabId: String): Flow<Tab?> {
        return tabDao.getTabById(tabId).map { entity ->
            entity?.let { Tab.fromEntity(it) }
        }
    }

    override suspend fun addTab(
        workspaceId: String,
        url: String,
        title: String?,
        faviconUrl: String?,
        customName: String?,
        isIncognito: Boolean
    ): String {
        val now = System.currentTimeMillis()
        val existingTabs = tabDao.getTabsByWorkspace(workspaceId).first()
        val maxPosition = existingTabs.maxOfOrNull { it.position } ?: -1

        val tab = TabEntity(
            id = UUID.randomUUID().toString(),
            workspaceId = workspaceId,
            url = url,
            title = title ?: "",
            faviconUrl = faviconUrl,
            customName = customName,
            isIncognito = isIncognito,
            position = maxPosition + 1,
            createdAt = now,
            updatedAt = now
        )
        tabDao.insert(tab)
        return tab.id
    }

    override suspend fun closeTab(tabId: String) {
        val entity = tabDao.getTabById(tabId).first() ?: return

        closedTabHistoryDao.insert(
            ClosedTabHistoryEntity(
                id = UUID.randomUUID().toString(),
                tabId = entity.id,
                url = entity.url,
                title = entity.title,
                faviconUrl = entity.faviconUrl,
                closedAt = System.currentTimeMillis()
            )
        )

        closedTabHistoryDao.pruneOldEntries()
        tabDao.delete(tabId)
    }

    override suspend fun switchTab(tabId: String) {
        val now = System.currentTimeMillis()
        val entity = tabDao.getTabById(tabId).first() ?: return
        tabDao.update(entity.copy(updatedAt = now))
    }

    override suspend fun duplicateTab(tabId: String): String {
        val original = tabDao.getTabById(tabId).first() ?: throw IllegalArgumentException("Tab not found: $tabId")

        val now = System.currentTimeMillis()
        val existingTabs = tabDao.getTabsByWorkspace(original.workspaceId).first()
        val maxPosition = existingTabs.maxOfOrNull { it.position } ?: -1

        val duplicated = original.copy(
            id = UUID.randomUUID().toString(),
            position = maxPosition + 1,
            createdAt = now,
            updatedAt = now
        )
        tabDao.insert(duplicated)
        return duplicated.id
    }

    override suspend fun hibernateTab(tabId: String, scrollY: Int) {
        tabDao.updateScrollY(tabId, scrollY)
        tabDao.updateHibernation(tabId, true)
    }

    override suspend fun updateTab(
        tabId: String,
        url: String?,
        title: String?,
        faviconUrl: String?
    ) {
        val entity = tabDao.getTabById(tabId).first() ?: return

        val updated = entity.copy(
            url = url ?: entity.url,
            title = title ?: entity.title,
            faviconUrl = faviconUrl ?: entity.faviconUrl,
            updatedAt = System.currentTimeMillis()
        )
        tabDao.update(updated)
    }

    override suspend fun moveTab(tabId: String, targetWorkspaceId: String) {
        val entity = tabDao.getTabById(tabId).first() ?: return

        val targetTabs = tabDao.getTabsByWorkspace(targetWorkspaceId).first()
        val maxPosition = targetTabs.maxOfOrNull { it.position } ?: -1

        tabDao.update(entity.copy(workspaceId = targetWorkspaceId, position = maxPosition + 1))
    }

    override suspend fun reorderTab(tabId: String, newPosition: Int) {
        tabDao.updatePosition(tabId, newPosition)
    }

    override suspend fun getLastTabInWorkspace(workspaceId: String): Tab? {
        val tabs = tabDao.getTabsByWorkspace(workspaceId).first()
        return tabs.lastOrNull()?.let { Tab.fromEntity(it) }
    }

    override suspend fun getTabCountForWorkspace(workspaceId: String): Int {
        return tabDao.getTabsByWorkspace(workspaceId).first().size
    }
}
