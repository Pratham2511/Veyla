package com.pratham.webhub.domain.usecase.tab

import com.pratham.webhub.domain.repository.ClosedTabRepository
import javax.inject.Inject

class RestoreTabUseCase @Inject constructor(
    private val repository: ClosedTabRepository
) {
    suspend operator fun invoke(tabId: String): String = repository.restoreTab(tabId)
}
