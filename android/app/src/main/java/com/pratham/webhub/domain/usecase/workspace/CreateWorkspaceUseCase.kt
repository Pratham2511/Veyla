package com.pratham.webhub.domain.usecase.workspace

import com.pratham.webhub.domain.repository.WorkspaceRepository
import javax.inject.Inject

class CreateWorkspaceUseCase @Inject constructor(
    private val repository: WorkspaceRepository
) {
    suspend operator fun invoke(name: String): String = repository.createWorkspace(name)
}
