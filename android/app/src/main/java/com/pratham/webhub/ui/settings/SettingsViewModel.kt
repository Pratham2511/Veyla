package com.pratham.webhub.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pratham.webhub.domain.model.AppSettings
import com.pratham.webhub.domain.model.SessionSnapshot
import com.pratham.webhub.domain.repository.SessionRepository
import com.pratham.webhub.domain.repository.SettingsRepository
import com.pratham.webhub.security.BiometricAuthManager
import com.pratham.webhub.util.SearchEngineHelper
import com.pratham.webhub.webview.AdBlocker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val searchEngines: List<Pair<String, String>> = emptyList(),
    val currentSearchEngineName: String = "Google",
    val sessions: List<SessionSnapshot> = emptyList(),
    val biometricAvailable: Boolean = false,
    val isClearingData: Boolean = false
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val biometricAuthManager: BiometricAuthManager,
    private val sessionRepository: SessionRepository,
    private val adBlocker: AdBlocker
) : ViewModel() {

    private val _isClearingData = MutableStateFlow(false)

    val state: StateFlow<SettingsUiState> = combine(
        settingsRepository.getSettings(),
        sessionRepository.getSessionSnapshots()
    ) { settings, sessions ->
        SettingsUiState(
            settings = settings,
            searchEngines = SearchEngineHelper.getAvailableEngines(),
            currentSearchEngineName = SearchEngineHelper.getEngineNameByUrl(settings.searchEngineUrl)
                ?: "Google",
            sessions = sessions,
            biometricAvailable = biometricAuthManager.canAuthenticate(),
            isClearingData = _isClearingData.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    // ── Theme ────────────────────────────────────────────────────────────

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.updateGlobalThemeMode(mode)
        }
    }

    // ── Search Engine ─────────────────────────────────────────────────────

    fun setSearchEngine(engineUrl: String) {
        viewModelScope.launch {
            settingsRepository.updateSearchEngineUrl(engineUrl)
        }
    }

    // ── Ad Block ─────────────────────────────────────────────────────────

    fun setAdBlockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateAdBlockEnabled(enabled)
            adBlocker.setAdBlockEnabled(enabled)
        }
    }

    // ── Biometric ────────────────────────────────────────────────────────

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateBiometricEnabled(enabled)
        }
    }

    // ── Sessions ─────────────────────────────────────────────────────────

    fun saveSession(name: String) {
        viewModelScope.launch {
            sessionRepository.saveSession(name)
        }
    }

    fun restoreSession(snapshotId: String) {
        viewModelScope.launch {
            sessionRepository.restoreSession(snapshotId)
        }
    }

    fun deleteSession(snapshotId: String) {
        viewModelScope.launch {
            sessionRepository.deleteSession(snapshotId)
        }
    }

    // ── Clear Browsing Data ───────────────────────────────────────────────

    /**
     * Clears browsing data by deleting all session snapshots.
     * In a full implementation this would also clear cookies, cache,
     * WebStorage, etc. via the [android.webkit.WebStorage] and
     * [android.webkit.CookieManager] APIs.
     */
    fun clearBrowsingData() {
        viewModelScope.launch {
            _isClearingData.value = true
            try {
                // Clear all saved sessions
                sessionRepository.getSessionSnapshots().first().forEach { session ->
                    sessionRepository.deleteSession(session.id)
                }
                // Reset ad-blocker counters
                adBlocker.resetBlockedCount()
            } finally {
                _isClearingData.value = false
            }
        }
    }
}