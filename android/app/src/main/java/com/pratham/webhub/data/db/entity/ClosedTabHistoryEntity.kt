package com.pratham.webhub.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "closed_tab_history",
    indices = [
        Index("closedAt")
    ]
)
data class ClosedTabHistoryEntity(
    @PrimaryKey val id: String,
    val tabId: String,
    val url: String,
    val title: String,
    val faviconUrl: String? = null,
    val closedAt: Long
)
