package com.pratham.webhub.domain.model

import com.pratham.webhub.data.db.entity.WorkspaceEntity

data class Workspace(
    val id: String,
    val name: String,
    val themeMode: String = "system",
    val accentColor: String? = null,
    val position: Int = 0,
    val isDefault: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        fun fromEntity(entity: WorkspaceEntity): Workspace = Workspace(
            id = entity.id,
            name = entity.name,
            themeMode = entity.themeMode,
            accentColor = entity.accentColor,
            position = entity.position,
            isDefault = entity.isDefault,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}

fun Workspace.toEntity(): WorkspaceEntity = WorkspaceEntity(
    id = id,
    name = name,
    themeMode = themeMode,
    accentColor = accentColor,
    position = position,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt
)
