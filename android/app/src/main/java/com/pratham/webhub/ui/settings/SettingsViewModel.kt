package com.pratham.webhub.ui.settings

import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebStorage
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

    // ── JavaScript (default for new tabs) ────────────────────────────────

    /**
     * Sets the default JavaScript-enabled state for newly created tabs.
     * Existing tabs are NOT retroactively reconfigured — they keep the
     * JS setting they were created with.
     */
    fun setJsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateJsEnabled(enabled)
        }
    }

    // ── Auto-restore last session ────────────────────────────────────────

    /**
     * When enabled, Veyla restores the most recent saved session on the
     * next cold-start. The restore itself is triggered by MainActivity
     * on launch.
     */
    fun setAutoRestoreLastSession(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateAutoRestoreLastSession(enabled)
        }
    }

    // ── Biometric ────────────────────────────────────────────────────────

    /**
     * Toggles the biometric lock. The setting is enforced by
     * [com.pratham.webhub.MainActivity], which gates the entire NavHost
     * behind a BiometricPrompt on cold-start when `isBiometricEnabled`
     * is true and the device can authenticate.
     */
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
     * Clears browsing data:
     *  - All saved session snapshots
     *  - WebView cookies (all hosts)
     *  - WebStorage (localStorage, IndexedDB, etc.)
     *  - Ad-block counters
     */
    fun clearBrowsingData() {
        viewModelScope.launch {
            _isClearingData.value = true
            try {
                // 1. Delete all saved session snapshots
                sessionRepository.getSessionSnapshots().first().forEach { session ->
                    sessionRepository.deleteSession(session.id)
                }

                // 2. Clear WebView cookies (all hosts)
                val cookieManager = CookieManager.getInstance()
                cookieManager.removeAllCookies(null)
                cookieManager.flush()

                // 3. Clear WebStorage (localStorage / IndexedDB / WebSQL)
                WebStorage.getInstance().deleteAllData()

                // 4. Reset ad-block counters
                adBlocker.resetBlockedCount()
            } catch (e: Exception) {
                Log.w("SettingsViewModel", "clearBrowsingData failed", e)
            } finally {
                _isClearingData.value = false
            }
        }
    }
}
