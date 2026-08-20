package com.pratham.webhub.domain.usecase.session

import com.pratham.webhub.domain.repository.SessionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AutoRestoreSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke() {
        val lastSession = repository.getLastSession().first() ?: return
        repository.restoreSession(lastSession.id)
    }
}
