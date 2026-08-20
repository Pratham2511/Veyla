package com.pratham.webhub.domain.usecase.tab

import com.pratham.webhub.domain.repository.TabRepository
import javax.inject.Inject

class DuplicateTabUseCase @Inject constructor(
    private val repository: TabRepository
) {
    suspend operator fun invoke(tabId: String): String = repository.duplicateTab(tabId)
}
