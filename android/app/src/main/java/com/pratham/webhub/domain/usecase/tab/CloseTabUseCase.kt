package com.pratham.webhub.domain.usecase.tab

import com.pratham.webhub.domain.repository.ClosedTabRepository
import com.pratham.webhub.domain.repository.TabRepository
import com.pratham.webhub.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CloseTabUseCase @Inject constructor(
    private val tabRepository: TabRepository,
    private val closedTabRepository: ClosedTabRepository,
    private val workspaceRepository: WorkspaceRepository
) {
    suspend operator fun invoke(tabId: String) {
        // Get current tabs from active workspace to find the tab and count remaining
        val activeWorkspaceId = workspaceRepository.getActiveWorkspaceId() ?: return
        val currentTabs = tabRepository.getTabsForWorkspace(activeWorkspaceId).first()
        val tabToClose = currentTabs.find { it.id == tabId } ?: return

        // Save tab state to closed tab history, then delete
        closedTabRepository.insertClosedTab(tabToClose)
        tabRepository.closeTab(tabId)

        // If this was the last tab in the workspace, switch to another workspace
        val remainingCount = tabRepository.getTabCountForWorkspace(tabToClose.workspaceId)
        if (remainingCount == 0) {
            val workspaces = workspaceRepository.getWorkspaces().first()
            val otherWorkspace = workspaces.firstOrNull { it.id != tabToClose.workspaceId }
            if (otherWorkspace != null) {
                workspaceRepository.switchWorkspace(otherWorkspace.id)
            }
        }
    }
}
