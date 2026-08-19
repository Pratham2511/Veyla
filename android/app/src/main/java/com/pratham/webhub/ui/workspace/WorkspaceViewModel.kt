package com.pratham.webhub.ui.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pratham.webhub.domain.model.Workspace
import com.pratham.webhub.domain.repository.TabRepository
import com.pratham.webhub.domain.repository.WorkspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class WorkspaceUiState(
    val workspaces: List<Workspace> = emptyList(),
    val tabCounts: Map<String, Int> = emptyMap(),
    val newWorkspaceName: String = "",
    val editingWorkspaceId: String? = null,
    val editingWorkspaceName: String = "",
    val isCreating: Boolean = false,
    val activeWorkspaceId: String? = null,
    val showDeleteConfirmation: String? = null  // workspaceId
) {
    /** The workspace currently being edited, or null. */
    val editingWorkspace: Workspace?
        get() = workspaces.find { it.id == editingWorkspaceId }
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkspaceViewModel @Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
    private val tabRepository: TabRepository
) : ViewModel() {

    private val _newWorkspaceName = MutableStateFlow("")
    private val _editingWorkspaceId = MutableStateFlow<String?>(null)
    private val _editingWorkspaceName = MutableStateFlow("")
    private val _isCreating = MutableStateFlow(false)
    private val _showDeleteConfirmation = MutableStateFlow<String?>(null)

    /** Tab counts derived from the workspace list. */
    private val tabCountsFlow: Flow<Map<String, Int>> =
        workspaceRepository.getWorkspaces().flatMapLatest { workspaces ->
            if (workspaces.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(workspaces.map { ws ->
                    tabRepository.getTabsForWorkspace(ws.id)
                        .map { it.size }
                        .map { count -> ws.id to count }
                }) { pairs ->
                    @Suppress("UNCHECKED_CAST")
                    (pairs as Array<Pair<String, Int>>).toMap()
                }
            }
        }

    val state: StateFlow<WorkspaceUiState> = combine(
        workspaceRepository.getWorkspaces(),
        tabCountsFlow,
        _newWorkspaceName,
        _editingWorkspaceId,
        _editingWorkspaceName
    ) { workspaces, tabCounts, newName, editId, editName ->
        WorkspaceUiState(
            workspaces = workspaces,
            tabCounts = tabCounts,
            newWorkspaceName = newName,
            editingWorkspaceId = editId,
            editingWorkspaceName = editName,
            isCreating = _isCreating.value,
            activeWorkspaceId = null,
            showDeleteConfirmation = _showDeleteConfirmation.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkspaceUiState()
    )

    // ── Public API ────────────────────────────────────────────────────────

    /** Update the new-workspace name field. */
    fun setNewWorkspaceName(name: String) {
        _newWorkspaceName.value = name
    }

    /** Create a new workspace with the current [WorkspaceUiState.newWorkspaceName]. */
    fun createWorkspace() {
        val name = _newWorkspaceName.value.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            _isCreating.value = true
            try {
                workspaceRepository.createWorkspace(name)
                _newWorkspaceName.value = ""
            } finally {
                _isCreating.value = false
            }
        }
    }

    /** Delete a workspace by ID. Throws if it's the last one. */
    fun deleteWorkspace(workspaceId: String) {
        viewModelScope.launch {
            try {
                workspaceRepository.deleteWorkspace(workspaceId)
                _showDeleteConfirmation.value = null
            } catch (_: IllegalArgumentException) {
                // Cannot delete the last workspace
            }
        }
    }

    /** Rename a workspace. */
    fun renameWorkspace(workspaceId: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            workspaceRepository.renameWorkspace(workspaceId, trimmed)
            _editingWorkspaceId.value = null
            _editingWorkspaceName.value = ""
        }
    }

    /** Set a workspace as the default. */
    fun setDefaultWorkspace(workspaceId: String) {
        viewModelScope.launch {
            workspaceRepository.setDefaultWorkspace(workspaceId)
        }
    }

    /** Switch to a workspace. */
    fun switchWorkspace(workspaceId: String) {
        viewModelScope.launch {
            workspaceRepository.switchWorkspace(workspaceId)
        }
    }

    /** Start editing a workspace's name. */
    fun startEditing(workspaceId: String, currentName: String) {
        _editingWorkspaceId.value = workspaceId
        _editingWorkspaceName.value = currentName
    }

    /** Update the edit-field name while editing. */
    fun setEditingWorkspaceName(name: String) {
        _editingWorkspaceName.value = name
    }

    /** Cancel editing. */
    fun cancelEditing() {
        _editingWorkspaceId.value = null
        _editingWorkspaceName.value = ""
    }

    /** Show the delete-confirmation dialog for a workspace. */
    fun showDeleteConfirmation(workspaceId: String) {
        _showDeleteConfirmation.value = workspaceId
    }

    /** Dismiss the delete-confirmation dialog. */
    fun dismissDeleteConfirmation() {
        _showDeleteConfirmation.value = null
    }
}
