package com.pratham.webhub.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workspaces",
    indices = [
        Index("position")
    ]
)
data class WorkspaceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val themeMode: String = "system",
    val accentColor: String? = null,
    val position: Int = 0,
    val isDefault: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
