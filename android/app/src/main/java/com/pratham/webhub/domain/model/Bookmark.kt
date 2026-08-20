package com.pratham.webhub.domain.model

import com.pratham.webhub.data.db.entity.BookmarkEntity

data class Bookmark(
    val id: String,
    val url: String,
    val title: String,
    val faviconUrl: String? = null,
    val createdAt: Long
) {
    companion object {
        fun fromEntity(entity: BookmarkEntity): Bookmark = Bookmark(
            id = entity.id,
            url = entity.url,
            title = entity.title,
            faviconUrl = entity.faviconUrl,
            createdAt = entity.createdAt
        )
    }
}

fun Bookmark.toEntity(): BookmarkEntity = BookmarkEntity(
    id = id,
    url = url,
    title = title,
    faviconUrl = faviconUrl,
    createdAt = createdAt
)
