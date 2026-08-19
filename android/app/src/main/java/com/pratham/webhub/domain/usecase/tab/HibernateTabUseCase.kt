package com.pratham.webhub.domain.usecase.tab

import com.pratham.webhub.domain.repository.TabRepository
import javax.inject.Inject

class HibernateTabUseCase @Inject constructor(
    private val repository: TabRepository
) {
    suspend operator fun invoke(tabId: String, scrollY: Int = 0) {
        repository.hibernateTab(tabId, scrollY)
    }
}
