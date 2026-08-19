package com.pratham.webhub.domain.repository

import com.pratham.webhub.domain.model.ClosedTab
import com.pratham.webhub.domain.model.Tab
import kotlinx.coroutines.flow.Flow

interface ClosedTabRepository {

    fun getClosedTabs(): Flow<List<ClosedTab>>

    suspend fun insertClosedTab(tab: Tab)

    suspend fun restoreTab(tabId: String): String

    suspend fun clearClosedTabs()
}