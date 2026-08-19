package com.pratham.webhub.domain.usecase.tab

import com.pratham.webhub.domain.repository.TabRepository
import javax.inject.Inject

class ReorderTabUseCase @Inject constructor(
    private val repository: TabRepository
) {
    suspend operator fun invoke(tabId: String, newPosition: Int) {
        repository.reorderTab(tabId, newPosition)
    }
}
