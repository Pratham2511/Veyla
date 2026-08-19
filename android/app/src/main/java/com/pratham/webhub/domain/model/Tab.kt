package com.pratham.webhub.domain.model

import com.pratham.webhub.data.db.entity.TabEntity

data class Tab(
    val id: String,
    val workspaceId: String,
    val url: String,
    val title: String = "",
    val faviconUrl: String? = null,
    val customName: String? = null,
    val customIconUri: String? = null,
    val isIncognito: Boolean = false,
    val isJsEnabled: Boolean = true,
    val isAdBlockEnabled: Boolean = true,
    val cssOverride: String? = null,
    val userScript: String? = null,
    val position: Int = 0,
    val isHibernated: Boolean = false,
    val savedScrollY: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        fun fromEntity(entity: TabEntity): Tab = Tab(
            id = entity.id,
            workspaceId = entity.workspaceId,
            url = entity.url,
            title = entity.title,
            faviconUrl = entity.faviconUrl,
            customName = entity.customName,
            customIconUri = entity.customIconUri,
            isIncognito = entity.isIncognito,
            isJsEnabled = entity.isJsEnabled,
            isAdBlockEnabled = entity.isAdBlockEnabled,
            cssOverride = entity.cssOverride,
            userScript = entity.userScript,
            position = entity.position,
            isHibernated = entity.isHibernated,
            savedScrollY = entity.savedScrollY,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}

fun Tab.toEntity(): TabEntity = TabEntity(
    id = id,
    workspaceId = workspaceId,
    url = url,
    title = title,
    faviconUrl = faviconUrl,
    customName = customName,
    customIconUri = customIconUri,
    isIncognito = isIncognito,
    isJsEnabled = isJsEnabled,
    isAdBlockEnabled = isAdBlockEnabled,
    cssOverride = cssOverride,
    userScript = userScript,
    position = position,
    isHibernated = isHibernated,
    savedScrollY = savedScrollY,
    createdAt = createdAt,
    updatedAt = updatedAt
)
