package com.pratham.webhub.domain.usecase.tab

import com.pratham.webhub.domain.repository.TabRepository
import javax.inject.Inject

class AddTabUseCase @Inject constructor(
    private val repository: TabRepository
) {
    suspend operator fun invoke(
        workspaceId: String,
        url: String,
        title: String? = null,
        faviconUrl: String? = null,
        customName: String? = null,
        isJsEnabled: Boolean = true,
        isAdBlockEnabled: Boolean = true
    ): String = repository.addTab(
        workspaceId = workspaceId,
        url = url,
        title = title,
        faviconUrl = faviconUrl,
        customName = customName,
        isJsEnabled = isJsEnabled,
        isAdBlockEnabled = isAdBlockEnabled
    )
}
