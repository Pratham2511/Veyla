package com.pratham.webhub.domain.repository

import com.pratham.webhub.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun getSettings(): Flow<AppSettings>

    suspend fun updateActiveWorkspaceId(workspaceId: String?)

    suspend fun updateGlobalThemeMode(themeMode: String)

    suspend fun updateDefaultWorkspaceId(workspaceId: String?)

    suspend fun updateBiometricEnabled(enabled: Boolean)

    suspend fun updateAdBlockEnabled(enabled: Boolean)

    suspend fun updateSearchEngineUrl(url: String)

    suspend fun updateOnboardingCompleted(completed: Boolean)

    /** Sets the default JavaScript-enabled state for newly created tabs. */
    suspend fun updateJsEnabled(enabled: Boolean)

    /** Sets whether Veyla restores the last saved session on launch. */
    suspend fun updateAutoRestoreLastSession(enabled: Boolean)
}