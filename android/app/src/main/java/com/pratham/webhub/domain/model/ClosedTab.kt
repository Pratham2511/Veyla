package com.pratham.webhub.domain.model

import com.pratham.webhub.data.db.entity.ClosedTabHistoryEntity

data class ClosedTab(
    val id: String,
    val tabId: String,
    val url: String,
    val title: String,
    val faviconUrl: String? = null,
    val closedAt: Long
) {
    companion object {
        fun fromEntity(entity: ClosedTabHistoryEntity): ClosedTab = ClosedTab(
            id = entity.id,
            tabId = entity.tabId,
            url = entity.url,
            title = entity.title,
            faviconUrl = entity.faviconUrl,
            closedAt = entity.closedAt
        )
    }
}

fun ClosedTab.toEntity(): ClosedTabHistoryEntity = ClosedTabHistoryEntity(
    id = id,
    tabId = tabId,
    url = url,
    title = title,
    faviconUrl = faviconUrl,
    closedAt = closedAt
)