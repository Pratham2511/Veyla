package com.pratham.webhub.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pratham.webhub.domain.model.ClosedTab
import com.pratham.webhub.domain.model.Tab
import com.pratham.webhub.domain.model.Workspace
import com.pratham.webhub.domain.repository.ClosedTabRepository
import com.pratham.webhub.domain.repository.TabRepository
import com.pratham.webhub.domain.repository.WorkspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class TabOverviewState(
    val tabs: List<Tab> = emptyList(),
    val closedTabs: List<ClosedTab> = emptyList(),
    val workspaces: List<Workspace> = emptyList(),
    val workspaceId: String? = null,
    val searchQuery: String = ""
) {
    /** Tabs filtered by the search query. */
    val filteredTabs: List<Tab>
        get() {
            if (searchQuery.isBlank()) return tabs
            val q = searchQuery.lowercase()
            return tabs.filter { tab ->
                tab.title.lowercase().contains(q) ||
                        tab.url.lowercase().contains(q) ||
                        (tab.customName?.lowercase()?.contains(q) == true)
            }
        }
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TabOverviewViewModel @Inject constructor(
    private val tabRepository: TabRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val closedTabRepository: ClosedTabRepository
) : ViewModel() {

    private val _workspaceId = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    val state: StateFlow<TabOverviewState> = combine(
        _workspaceId.flatMapLatest { wsId ->
            if (wsId != null) tabRepository.getTabsForWorkspace(wsId)
            else flowOf(emptyList())
        },
        closedTabRepository.getClosedTabs(),
        workspaceRepository.getWorkspaces(),
        _searchQuery
    ) { tabs, closedTabs, workspaces, query ->
        TabOverviewState(
            tabs = tabs.sortedBy { it.position },
            closedTabs = closedTabs,
            workspaces = workspaces,
            workspaceId = _workspaceId.value,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TabOverviewState()
    )

    // ── Public API ────────────────────────────────────────────────────────

    /** Set the workspace whose tabs should be displayed. */
    fun setWorkspace(workspaceId: String) {
        _workspaceId.value = workspaceId
    }

    /** Update the search/filter query. */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** Close a tab by [tabId]. */
    fun closeTab(tabId: String) {
        viewModelScope.launch {
            tabRepository.closeTab(tabId)
        }
    }

    /** Duplicate a tab by [tabId]; returns the new tab ID. */
    fun duplicateTab(tabId: String) {
        viewModelScope.launch {
            tabRepository.duplicateTab(tabId)
        }
    }

    /** Move a tab to a different workspace. */
    fun moveTab(tabId: String, targetWorkspaceId: String) {
        viewModelScope.launch {
            tabRepository.moveTab(tabId, targetWorkspaceId)
        }
    }

    /** Hibernate a tab to free its WebView memory. */
    fun hibernateTab(tabId: String, scrollY: Int = 0) {
        viewModelScope.launch {
            tabRepository.hibernateTab(tabId, scrollY)
        }
    }

    /** Restore a recently-closed tab. */
    fun restoreClosedTab(closedTabId: String) {
        viewModelScope.launch {
            closedTabRepository.restoreTab(closedTabId)
        }
    }

    /** Clear all recently-closed tab history. */
    fun clearClosedTabs() {
        viewModelScope.launch {
            closedTabRepository.clearClosedTabs()
        }
    }

    /** Reorder a tab within the workspace. */
    fun reorderTab(tabId: String, newPosition: Int) {
        viewModelScope.launch {
            tabRepository.reorderTab(tabId, newPosition)
        }
    }
}
