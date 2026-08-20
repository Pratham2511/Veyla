package com.pratham.webhub.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tabs",
    foreignKeys = [
        ForeignKey(
            entity = WorkspaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["workspaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("workspaceId"),
        Index(value = ["workspaceId", "position"])
    ]
)
data class TabEntity(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val url: String,
    val title: String = "",
    val faviconUrl: String? = null,
    val customName: String? = null,
    val customIconUri: String? = null,
    val isJsEnabled: Boolean = true,
    val isAdBlockEnabled: Boolean = true,
    val cssOverride: String? = null,
    val userScript: String? = null,
    val position: Int = 0,
    val isHibernated: Boolean = false,
    val savedScrollY: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)
