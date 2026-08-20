package com.pratham.webhub.ui.addtab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pratham.webhub.domain.model.AppSettings
import com.pratham.webhub.domain.model.Workspace
import com.pratham.webhub.domain.repository.SettingsRepository
import com.pratham.webhub.domain.repository.WorkspaceRepository
import com.pratham.webhub.util.UrlNormalizer
import com.pratham.webhub.util.UrlNormalizer.UrlResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class AddTabUiState(
    val urlInput: String = "",
    val urlResult: UrlResult? = null,
    val customName: String = "",
    val selectedWorkspaceId: String? = null,
    val workspaces: List<Workspace> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val isValid: Boolean = false
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class AddTabViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val workspaceRepository: WorkspaceRepository
) : ViewModel() {

    private val _urlInput = MutableStateFlow("")
    private val _customName = MutableStateFlow("")
    private val _selectedWorkspaceId = MutableStateFlow<String?>(null)

    // Nested combines to handle more than 5 flows
    private val userInputsFlow = combine(
        _urlInput,
        _customName,
        _selectedWorkspaceId
    ) { urlInput, customName, selectedWsId ->
        UserInputs(urlInput, customName, selectedWsId)
    }

    private data class UserInputs(
        val urlInput: String,
        val customName: String,
        val selectedWorkspaceId: String?
    )

    val state: StateFlow<AddTabUiState> = combine(
        settingsRepository.getSettings(),
        workspaceRepository.getWorkspaces(),
        userInputsFlow
    ) { settings, workspaces, inputs ->
        val urlResult = UrlNormalizer.normalize(inputs.urlInput, settings.searchEngineUrl)
        val effectiveWorkspaceId = inputs.selectedWorkspaceId ?: settings.activeWorkspaceId

        AddTabUiState(
            urlInput = inputs.urlInput,
            urlResult = urlResult,
            customName = inputs.customName,
            selectedWorkspaceId = effectiveWorkspaceId,
            workspaces = workspaces,
            settings = settings,
            isValid = urlResult.url.isNotBlank() && urlResult.url != "about:blank"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddTabUiState()
    )

    // ── Public API ────────────────────────────────────────────────────────

    fun setUrlInput(input: String) {
        _urlInput.value = input
    }

    fun setCustomName(name: String) {
        _customName.value = name
    }

    fun setSelectedWorkspace(workspaceId: String?) {
        _selectedWorkspaceId.value = workspaceId
    }

    /** Clear all input fields (e.g. after successful tab creation). */
    fun clear() {
        _urlInput.value = ""
        _customName.value = ""
        _selectedWorkspaceId.value = null
    }
}
