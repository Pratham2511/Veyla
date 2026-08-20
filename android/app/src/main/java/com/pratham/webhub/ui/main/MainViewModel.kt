package com.pratham.webhub.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pratham.webhub.domain.model.AppSettings
import com.pratham.webhub.domain.model.Tab
import com.pratham.webhub.domain.model.Workspace
import com.pratham.webhub.domain.repository.BookmarkRepository
import com.pratham.webhub.domain.repository.SettingsRepository
import com.pratham.webhub.domain.repository.TabRepository
import com.pratham.webhub.domain.repository.WorkspaceRepository
import com.pratham.webhub.domain.usecase.bookmark.AddBookmarkUseCase
import com.pratham.webhub.domain.usecase.bookmark.RemoveBookmarkUseCase
import com.pratham.webhub.domain.usecase.session.SaveSessionUseCase
import com.pratham.webhub.domain.usecase.tab.*
import com.pratham.webhub.domain.usecase.workspace.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class MainUiState(
    val activeWorkspaceId: String? = null,
    val activeTabId: String? = null,
    val workspaces: List<Workspace> = emptyList(),
    val tabs: List<Tab> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = false,
    val showTabOverview: Boolean = false,
    val showWorkspaceSwitcher: Boolean = false,
    val showAddTabSheet: Boolean = false,
    val showQuickSwitcher: Boolean = false,
    val showRecentlyClosed: Boolean = false,
    val showTabSettings: String? = null,
    val isBookmarked: Boolean = false,
    val quickSwitcherQuery: String = ""
) {
    /** Tabs visible in the quick-switcher, filtered by the current query. */
    val filteredTabs: List<Tab>
        get() {
            if (quickSwitcherQuery.isBlank()) return tabs
            val q = quickSwitcherQuery.lowercase()
            return tabs.filter { tab ->
                tab.title.lowercase().contains(q) ||
                        tab.url.lowercase().contains(q) ||
                        (tab.customName?.lowercase()?.contains(q) == true)
            }
        }

    /** The currently active [Tab], or null. */
    val activeTab: Tab?
        get() = tabs.find { it.id == activeTabId }

    /** The currently active [Workspace], or null. */
    val activeWorkspace: Workspace?
        get() = workspaces.find { it.id == activeWorkspaceId }
}

// ── One-shot events ───────────────────────────────────────────────────────────

sealed class MainEvent {
    data class SelectTab(val tabId: String) : MainEvent()
    data class CloseTab(val tabId: String) : MainEvent()
    data class AddTab(val url: String, val customName: String? = null) : MainEvent()
    data object ShowTabOverview : MainEvent()
    data object ShowWorkspaceSwitcher : MainEvent()
    data object ShowAddTab : MainEvent()
    data object ShowQuickSwitcher : MainEvent()
    data object ShowRecentlyClosed : MainEvent()
    data class ToggleBookmark(
        val url: String,
        val title: String,
        val faviconUrl: String? = null
    ) : MainEvent()

    data class SwitchWorkspace(val workspaceId: String) : MainEvent()
    data class CreateWorkspace(val name: String) : MainEvent()
    data class DeleteWorkspace(val workspaceId: String) : MainEvent()
    data class RenameWorkspace(val workspaceId: String, val newName: String) : MainEvent()
    data class HibernateTab(val tabId: String) : MainEvent()
    data class RestoreTab(val tabId: String) : MainEvent()
    data class ReorderTab(val tabId: String, val newPosition: Int) : MainEvent()
    data class UpdateQuickSwitcherQuery(val query: String) : MainEvent()
    data object DismissAll : MainEvent()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    // Tab use cases
    private val addTabUseCase: AddTabUseCase,
    private val closeTabUseCase: CloseTabUseCase,
    private val switchTabUseCase: SwitchTabUseCase,
    private val duplicateTabUseCase: DuplicateTabUseCase,
    private val hibernateTabUseCase: HibernateTabUseCase,
    private val restoreTabUseCase: RestoreTabUseCase,
    private val updateTabUseCase: UpdateTabUseCase,
    private val moveTabUseCase: MoveTabUseCase,
    private val reorderTabUseCase: ReorderTabUseCase,
    // Workspace use cases
    private val createWorkspaceUseCase: CreateWorkspaceUseCase,
    private val deleteWorkspaceUseCase: DeleteWorkspaceUseCase,
    private val renameWorkspaceUseCase: RenameWorkspaceUseCase,
    private val switchWorkspaceUseCase: SwitchWorkspaceUseCase,
    private val setDefaultWorkspaceUseCase: SetDefaultWorkspaceUseCase,
    // Bookmark use cases
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val removeBookmarkUseCase: RemoveBookmarkUseCase,
    // Session use case
    private val saveSessionUseCase: SaveSessionUseCase,
    // Repositories
    private val settingsRepository: SettingsRepository,
    private val tabRepository: TabRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val bookmarkRepository: BookmarkRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    // ── Init: collect flows ───────────────────────────────────────────────

    init {
        // 1. Settings
        settingsRepository.getSettings()
            .onEach { settings ->
                _state.update { it.copy(settings = settings) }
            }
            .launchIn(viewModelScope)

        // 2. Workspaces (always observe all)
        workspaceRepository.getWorkspaces()
            .onEach { workspaces ->
                _state.update { it.copy(workspaces = workspaces) }
            }
            .launchIn(viewModelScope)

        // 3. Determine active workspace, then observe its tabs
        _state
            .map { it.activeWorkspaceId ?: it.settings.activeWorkspaceId }
            .distinctUntilChanged()
            .flatMapLatest { workspaceId ->
                if (workspaceId != null) {
                    tabRepository.getTabsForWorkspace(workspaceId)
                } else {
                    flowOf(emptyList())
                }
            }
            .onEach { tabs ->
                _state.update { current ->
                    // Keep the activeTabId pointing at a valid tab
                    val newActiveId = if (tabs.any { it.id == current.activeTabId }) {
                        current.activeTabId
                    } else {
                        tabs.firstOrNull()?.id
                    }
                    current.copy(tabs = tabs, activeTabId = newActiveId)
                }
            }
            .launchIn(viewModelScope)

        // 4. Bookmark status for the active tab's URL
        _state
            .map { it.activeTab?.url }
            .distinctUntilChanged()
            .flatMapLatest { url ->
                if (url.isNullOrBlank() || url == "about:blank") flowOf(false)
                else bookmarkRepository.isBookmarked(url)
            }
            .onEach { isBookmarked ->
                _state.update { it.copy(isBookmarked = isBookmarked) }
            }
            .launchIn(viewModelScope)

        // 5. Sync activeWorkspaceId from settings on first emission
        viewModelScope.launch {
            val settings = settingsRepository.getSettings().first()
            val wsId = settings.activeWorkspaceId
            if (wsId != null) {
                _state.update { it.copy(activeWorkspaceId = wsId) }
            }
        }
    }

    // ── Event handler ─────────────────────────────────────────────────────

    fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.SelectTab -> handleSelectTab(event)
            is MainEvent.CloseTab -> handleCloseTab(event)
            is MainEvent.AddTab -> handleAddTab(event)
            is MainEvent.ShowTabOverview -> handleShowTabOverview()
            is MainEvent.ShowWorkspaceSwitcher -> handleShowWorkspaceSwitcher()
            is MainEvent.ShowAddTab -> handleShowAddTab()
            is MainEvent.ShowQuickSwitcher -> handleShowQuickSwitcher()
            is MainEvent.ShowRecentlyClosed -> handleShowRecentlyClosed()
            is MainEvent.ToggleBookmark -> handleToggleBookmark(event)
            is MainEvent.SwitchWorkspace -> handleSwitchWorkspace(event)
            is MainEvent.CreateWorkspace -> handleCreateWorkspace(event)
            is MainEvent.DeleteWorkspace -> handleDeleteWorkspace(event)
            is MainEvent.RenameWorkspace -> handleRenameWorkspace(event)
            is MainEvent.HibernateTab -> handleHibernateTab(event)
            is MainEvent.RestoreTab -> handleRestoreTab(event)
            is MainEvent.ReorderTab -> handleReorderTab(event)
            is MainEvent.UpdateQuickSwitcherQuery -> handleUpdateQuickSwitcherQuery(event)
            is MainEvent.DismissAll -> handleDismissAll()
        }
    }

    // ── Private handlers ──────────────────────────────────────────────────

    private fun handleSelectTab(event: MainEvent.SelectTab) {
        viewModelScope.launch {
            switchTabUseCase(event.tabId)
            _state.update { it.copy(activeTabId = event.tabId) }
        }
    }

    private fun handleCloseTab(event: MainEvent.CloseTab) {
        viewModelScope.launch {
            val wasLastTab = _state.value.tabs.size <= 1
            val closedTabId = event.tabId

            closeTabUseCase(closedTabId)

            // If we closed the last tab, auto-create a new empty tab
            if (wasLastTab) {
                val wsId = _state.value.activeWorkspaceId ?: return@launch
                val newTabId = addTabUseCase(
                    workspaceId = wsId,
                    url = "about:blank"
                )
                _state.update { it.copy(activeTabId = newTabId) }
            }
        }
    }

    private fun handleAddTab(event: MainEvent.AddTab) {
        viewModelScope.launch {
            val wsId = _state.value.activeWorkspaceId ?: return@launch
            val newTabId = addTabUseCase(
                workspaceId = wsId,
                url = event.url,
                customName = event.customName
            )
            _state.update { it.copy(activeTabId = newTabId) }
        }
    }

    private fun handleShowTabOverview() {
        _state.update {
            it.copy(
                showTabOverview = true,
                showWorkspaceSwitcher = false,
                showAddTabSheet = false,
                showQuickSwitcher = false,
                showRecentlyClosed = false,
                showTabSettings = null
            )
        }
    }

    private fun handleShowWorkspaceSwitcher() {
        _state.update {
            it.copy(
                showWorkspaceSwitcher = true,
                showTabOverview = false,
                showAddTabSheet = false,
                showQuickSwitcher = false,
                showRecentlyClosed = false
            )
        }
    }

    private fun handleShowAddTab() {
        _state.update {
            it.copy(
                showAddTabSheet = true,
                showTabOverview = false,
                showWorkspaceSwitcher = false,
                showQuickSwitcher = false,
                showRecentlyClosed = false
            )
        }
    }

    private fun handleShowQuickSwitcher() {
        _state.update {
            it.copy(
                showQuickSwitcher = true,
                showTabOverview = false,
                showWorkspaceSwitcher = false,
                showAddTabSheet = false,
                showRecentlyClosed = false,
                quickSwitcherQuery = ""
            )
        }
    }

    private fun handleShowRecentlyClosed() {
        _state.update {
            it.copy(
                showRecentlyClosed = true,
                showTabOverview = false,
                showWorkspaceSwitcher = false,
                showAddTabSheet = false,
                showQuickSwitcher = false
            )
        }
    }

    private fun handleToggleBookmark(event: MainEvent.ToggleBookmark) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.isBookmarked) {
                // Remove – need to find the bookmark ID first
                val bookmarkId = bookmarkRepository.getBookmarkIdByUrl(event.url)
                if (bookmarkId != null) {
                    removeBookmarkUseCase(bookmarkId)
                }
            } else {
                addBookmarkUseCase(
                    url = event.url,
                    title = event.title,
                    faviconUrl = event.faviconUrl
                )
            }
        }
    }

    private fun handleSwitchWorkspace(event: MainEvent.SwitchWorkspace) {
        viewModelScope.launch {
            switchWorkspaceUseCase(event.workspaceId)
            _state.update {
                it.copy(
                    activeWorkspaceId = event.workspaceId,
                    activeTabId = null, // will be set when tabs flow emits
                    showWorkspaceSwitcher = false
                )
            }
        }
    }

    private fun handleCreateWorkspace(event: MainEvent.CreateWorkspace) {
        viewModelScope.launch {
            val newId = createWorkspaceUseCase(event.name)
            switchWorkspaceUseCase(newId)
            _state.update {
                it.copy(
                    activeWorkspaceId = newId,
                    activeTabId = null,
                    showWorkspaceSwitcher = false
                )
            }
        }
    }

    private fun handleDeleteWorkspace(event: MainEvent.DeleteWorkspace) {
        viewModelScope.launch {
            try {
                deleteWorkspaceUseCase(event.workspaceId)
                // After deletion the workspace flow will update the list.
                // If the deleted workspace was active, the CloseTabUseCase already
                // switched to another workspace (see its impl).
            } catch (_: IllegalArgumentException) {
                // Cannot delete the last workspace – ignore
            }
        }
    }

    private fun handleRenameWorkspace(event: MainEvent.RenameWorkspace) {
        viewModelScope.launch {
            renameWorkspaceUseCase(event.workspaceId, event.newName)
        }
    }

    private fun handleHibernateTab(event: MainEvent.HibernateTab) {
        viewModelScope.launch {
            hibernateTabUseCase(event.tabId)
        }
    }

    private fun handleRestoreTab(event: MainEvent.RestoreTab) {
        viewModelScope.launch {
            val newTabId = restoreTabUseCase(event.tabId)
            _state.update {
                it.copy(
                    activeTabId = newTabId,
                    showRecentlyClosed = false
                )
            }
        }
    }

    private fun handleReorderTab(event: MainEvent.ReorderTab) {
        viewModelScope.launch {
            reorderTabUseCase(event.tabId, event.newPosition)
        }
    }

    private fun handleUpdateQuickSwitcherQuery(event: MainEvent.UpdateQuickSwitcherQuery) {
        _state.update { it.copy(quickSwitcherQuery = event.query) }
    }

    private fun handleDismissAll() {
        _state.update {
            it.copy(
                showTabOverview = false,
                showWorkspaceSwitcher = false,
                showAddTabSheet = false,
                showQuickSwitcher = false,
                showRecentlyClosed = false,
                showTabSettings = null
            )
        }
    }

    // ── Public helpers ────────────────────────────────────────────────────

    /** Set the active tab ID from outside (e.g. BrowserViewModel navigating). */
    fun setActiveTabId(tabId: String) {
        _state.update { it.copy(activeTabId = tabId) }
    }

    /** Save the current session with the given name. */
    fun saveCurrentSession(name: String) {
        viewModelScope.launch {
            saveSessionUseCase(name)
        }
    }

    /** Update the bookmark status manually (e.g. after a page loads). */
    fun refreshBookmarkStatus(url: String) {
        viewModelScope.launch {
            if (url.isBlank() || url == "about:blank") return@launch
            bookmarkRepository.isBookmarked(url).collect { isBookmarked ->
                _state.update { it.copy(isBookmarked = isBookmarked) }
                return@collect
            }
        }
    }
}
