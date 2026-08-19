package com.pratham.webhub.domain.usecase.workspace

import com.pratham.webhub.domain.repository.WorkspaceRepository
import javax.inject.Inject

class SwitchWorkspaceUseCase @Inject constructor(
    private val repository: WorkspaceRepository
) {
    suspend operator fun invoke(workspaceId: String) {
        repository.switchWorkspace(workspaceId)
    }
}
