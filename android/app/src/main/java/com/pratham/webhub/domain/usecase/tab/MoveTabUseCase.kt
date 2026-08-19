package com.pratham.webhub.domain.usecase.tab

import com.pratham.webhub.domain.repository.TabRepository
import javax.inject.Inject

class MoveTabUseCase @Inject constructor(
    private val repository: TabRepository
) {
    suspend operator fun invoke(tabId: String, targetWorkspaceId: String) {
        repository.moveTab(tabId, targetWorkspaceId)
    }
}
