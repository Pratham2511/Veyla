package com.pratham.webhub.domain.usecase.workspace

import com.pratham.webhub.domain.repository.WorkspaceRepository
import javax.inject.Inject

class DeleteWorkspaceUseCase @Inject constructor(
    private val repository: WorkspaceRepository
) {
    suspend operator fun invoke(workspaceId: String) {
        val count = repository.getWorkspaceCount()
        require(count > 1) { "Cannot delete the last workspace" }
        repository.deleteWorkspace(workspaceId)
    }
}
