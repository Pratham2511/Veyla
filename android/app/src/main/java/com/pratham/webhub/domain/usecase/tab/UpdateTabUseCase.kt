package com.pratham.webhub.domain.usecase.tab

import com.pratham.webhub.domain.repository.TabRepository
import javax.inject.Inject

class UpdateTabUseCase @Inject constructor(
    private val repository: TabRepository
) {
    suspend operator fun invoke(
        tabId: String,
        url: String? = null,
        title: String? = null,
        faviconUrl: String? = null
    ) {
        repository.updateTab(tabId, url, title, faviconUrl)
    }
}
