package com.pratham.webhub.domain.usecase.bookmark

import com.pratham.webhub.domain.repository.BookmarkRepository
import javax.inject.Inject

class RemoveBookmarkUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    suspend operator fun invoke(bookmarkId: String) {
        repository.removeBookmark(bookmarkId)
    }
}
