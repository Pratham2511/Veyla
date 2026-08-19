package com.pratham.webhub.domain.usecase.workspace

import com.pratham.webhub.domain.repository.WorkspaceRepository
import javax.inject.Inject

class RenameWorkspaceUseCase @Inject constructor(
    private val repository: WorkspaceRepository
) {
    suspend operator fun invoke(workspaceId: String, newName: String) {
        repository.renameWorkspace(workspaceId, newName)
    }
}
