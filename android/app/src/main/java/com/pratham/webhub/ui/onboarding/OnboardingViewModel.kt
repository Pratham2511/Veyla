package com.pratham.webhub.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pratham.webhub.domain.repository.SettingsRepository
import com.pratham.webhub.domain.repository.WorkspaceRepository
import com.pratham.webhub.util.SearchEngineHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

/** Supported theme selections shown during onboarding. */
enum class OnboardingTheme(val value: String, val label: String) {
    System("system", "System Default"),
    Light("light", "Light"),
    Dark("dark", "Dark")
}

/** A search-engine option shown during onboarding. */
data class SearchEngineOption(
    val name: String,
    val url: String
)

data class OnboardingUiState(
    val currentStep: Int = 0,           // 0 = Welcome+Theme, 1 = Search engine, 2 = Workspace name
    val selectedTheme: OnboardingTheme = OnboardingTheme.System,
    val selectedSearchEngineUrl: String = SearchEngineHelper.GOOGLE,
    val searchEngines: List<SearchEngineOption> = emptyList(),
    val defaultWorkspaceName: String = "Personal",
    val isCompleting: Boolean = false
) {
    val isLastStep: Boolean get() = currentStep == 2
    val canGoNext: Boolean
        get() = when (currentStep) {
            0 -> true
            1 -> selectedSearchEngineUrl.isNotBlank()
            2 -> defaultWorkspaceName.isNotBlank()
            else -> false
        }
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val workspaceRepository: WorkspaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        OnboardingUiState(
            searchEngines = SearchEngineHelper.getAvailableEngines().map { (name, url) ->
                SearchEngineOption(name, url)
            }
        )
    )
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    // ── Step navigation ──────────────────────────────────────────────────

    fun nextStep() {
        val current = _state.value
        if (!current.canGoNext) return
        if (current.currentStep < 2) {
            _state.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun previousStep() {
        val current = _state.value
        if (current.currentStep > 0) {
            _state.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    // ── Step 0: Theme ─────────────────────────────────────────────────────

    fun selectTheme(theme: OnboardingTheme) {
        _state.update { it.copy(selectedTheme = theme) }
    }

    // ── Step 1: Search engine ─────────────────────────────────────────────

    fun selectSearchEngine(url: String) {
        _state.update { it.copy(selectedSearchEngineUrl = url) }
    }

    // ── Step 2: Workspace name ───────────────────────────────────────────

    fun setDefaultWorkspaceName(name: String) {
        _state.update { it.copy(defaultWorkspaceName = name) }
    }

    // ── Complete ─────────────────────────────────────────────────────────

    /**
     * Persists all onboarding choices and creates the default workspace.
     * The caller should navigate away once the returned flow emits `true`.
     */
    fun complete() {
        val currentState = _state.value
        if (!currentState.canGoNext || currentState.currentStep != 2) return

        viewModelScope.launch {
            _state.update { it.copy(isCompleting = true) }
            try {
                // Persist theme
                settingsRepository.updateGlobalThemeMode(currentState.selectedTheme.value)

                // Persist search engine
                settingsRepository.updateSearchEngineUrl(currentState.selectedSearchEngineUrl)

                // Create the default workspace
                val workspaceId = workspaceRepository.createWorkspace(currentState.defaultWorkspaceName.trim())

                // Set as default & active
                workspaceRepository.setDefaultWorkspace(workspaceId)
                workspaceRepository.switchWorkspace(workspaceId)

                // Mark onboarding as done
                settingsRepository.updateOnboardingCompleted(true)
            } finally {
                _state.update { it.copy(isCompleting = false) }
            }
        }
    }
}
