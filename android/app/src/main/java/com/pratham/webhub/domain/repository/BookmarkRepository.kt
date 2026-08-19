package com.pratham.webhub.domain.repository

import com.pratham.webhub.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {

    fun getBookmarks(): Flow<List<Bookmark>>

    fun isBookmarked(url: String): Flow<Boolean>

    suspend fun addBookmark(url: String, title: String, faviconUrl: String? = null)

    suspend fun removeBookmark(bookmarkId: String)

    suspend fun getBookmarkIdByUrl(url: String): String?
}