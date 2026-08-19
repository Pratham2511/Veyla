package com.pratham.webhub.domain.usecase.session

import com.pratham.webhub.domain.repository.SessionRepository
import javax.inject.Inject

class SaveSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(name: String) {
        repository.saveSession(name)
    }
}
