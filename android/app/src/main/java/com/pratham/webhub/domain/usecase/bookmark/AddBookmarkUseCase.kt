package com.pratham.webhub.domain.usecase.bookmark

import com.pratham.webhub.domain.repository.BookmarkRepository
import javax.inject.Inject

class AddBookmarkUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    suspend operator fun invoke(url: String, title: String, faviconUrl: String? = null) {
        repository.addBookmark(url, title, faviconUrl)
    }
}