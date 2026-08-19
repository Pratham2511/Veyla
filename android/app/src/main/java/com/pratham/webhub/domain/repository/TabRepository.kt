package com.pratham.webhub.domain.repository

import com.pratham.webhub.domain.model.Tab
import kotlinx.coroutines.flow.Flow

interface TabRepository {

    fun getTabsForWorkspace(workspaceId: String): Flow<List<Tab>>

    fun getTab(tabId: String): Flow<Tab?>

    suspend fun addTab(
        workspaceId: String,
        url: String,
        title: String? = null,
        faviconUrl: String? = null,
        customName: String? = null,
        isIncognito: Boolean = false
    ): String

    suspend fun closeTab(tabId: String)

    suspend fun switchTab(tabId: String)

    suspend fun duplicateTab(tabId: String): String

    suspend fun hibernateTab(tabId: String, scrollY: Int = 0)

    suspend fun updateTab(
        tabId: String,
        url: String? = null,
        title: String? = null,
        faviconUrl: String? = null
    )

    suspend fun moveTab(tabId: String, targetWorkspaceId: String)

    suspend fun reorderTab(tabId: String, newPosition: Int)

    suspend fun getLastTabInWorkspace(workspaceId: String): Tab?

    suspend fun getTabCountForWorkspace(workspaceId: String): Int
}