package com.pratham.webhub.data.repository

import com.pratham.webhub.data.db.dao.BookmarkDao
import com.pratham.webhub.data.db.entity.BookmarkEntity
import com.pratham.webhub.domain.model.Bookmark
import com.pratham.webhub.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao
) : BookmarkRepository {

    override fun getBookmarks(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarks().map { entities ->
            entities.map { Bookmark.fromEntity(it) }
        }
    }

    override fun isBookmarked(url: String): Flow<Boolean> {
        return kotlinx.coroutines.flow.flow {
            emit(bookmarkDao.existsByUrl(url))
        }
    }

    override suspend fun addBookmark(url: String, title: String, faviconUrl: String?) {
        // Avoid duplicates: remove existing bookmark for this URL first
        bookmarkDao.deleteByUrl(url)
        bookmarkDao.insert(
            BookmarkEntity(
                id = UUID.randomUUID().toString(),
                url = url,
                title = title,
                faviconUrl = faviconUrl,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun removeBookmark(bookmarkId: String) {
        bookmarkDao.delete(bookmarkId)
    }

    override suspend fun getBookmarkIdByUrl(url: String): String? {
        val bookmarks = bookmarkDao.getAllBookmarks().first()
        return bookmarks.firstOrNull { it.url == url }?.id
    }
}
