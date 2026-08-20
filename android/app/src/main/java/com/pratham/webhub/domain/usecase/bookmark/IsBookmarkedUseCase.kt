package com.pratham.webhub.domain.usecase.bookmark

import com.pratham.webhub.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsBookmarkedUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    operator fun invoke(url: String): Flow<Boolean> = repository.isBookmarked(url)
}
