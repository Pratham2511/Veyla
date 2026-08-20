package com.pratham.webhub.domain.usecase.session

import com.pratham.webhub.domain.repository.SessionRepository
import javax.inject.Inject

class RestoreSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(snapshotId: String) {
        repository.restoreSession(snapshotId)
    }
}
