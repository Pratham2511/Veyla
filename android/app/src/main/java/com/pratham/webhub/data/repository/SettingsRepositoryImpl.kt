package com.pratham.webhub.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pratham.webhub.domain.model.AppSettings
import com.pratham.webhub.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val ACTIVE_WORKSPACE_ID = stringPreferencesKey("active_workspace_id")
        val GLOBAL_THEME_MODE = stringPreferencesKey("global_theme_mode")
        val DEFAULT_WORKSPACE_ID = stringPreferencesKey("default_workspace_id")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val AD_BLOCK_ENABLED = booleanPreferencesKey("ad_block_enabled")
        val SEARCH_ENGINE_URL = stringPreferencesKey("search_engine_url")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val JS_ENABLED = booleanPreferencesKey("js_enabled")
        val AUTO_RESTORE_LAST_SESSION = booleanPreferencesKey("auto_restore_last_session")
    }

    override fun getSettings(): Flow<AppSettings> {
        return dataStore.data.map { prefs ->
            AppSettings(
                activeWorkspaceId = prefs[Keys.ACTIVE_WORKSPACE_ID],
                globalThemeMode = prefs[Keys.GLOBAL_THEME_MODE] ?: "system",
                defaultWorkspaceId = prefs[Keys.DEFAULT_WORKSPACE_ID],
                isBiometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
                adBlockEnabled = prefs[Keys.AD_BLOCK_ENABLED] ?: true,
                searchEngineUrl = prefs[Keys.SEARCH_ENGINE_URL]
                    ?: "https://www.google.com/search?q=",
                hasCompletedOnboarding = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
                isJsEnabled = prefs[Keys.JS_ENABLED] ?: true,
                autoRestoreLastSession = prefs[Keys.AUTO_RESTORE_LAST_SESSION] ?: false
            )
        }
    }

    override suspend fun updateActiveWorkspaceId(workspaceId: String?) {
        dataStore.edit { prefs ->
            if (workspaceId != null) {
                prefs[Keys.ACTIVE_WORKSPACE_ID] = workspaceId
            } else {
                prefs.remove(Keys.ACTIVE_WORKSPACE_ID)
            }
        }
    }

    override suspend fun updateGlobalThemeMode(themeMode: String) {
        dataStore.edit { prefs ->
            prefs[Keys.GLOBAL_THEME_MODE] = themeMode
        }
    }

    override suspend fun updateDefaultWorkspaceId(workspaceId: String?) {
        dataStore.edit { prefs ->
            if (workspaceId != null) {
                prefs[Keys.DEFAULT_WORKSPACE_ID] = workspaceId
            } else {
                prefs.remove(Keys.DEFAULT_WORKSPACE_ID)
            }
        }
    }

    override suspend fun updateBiometricEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.BIOMETRIC_ENABLED] = enabled
        }
    }

    override suspend fun updateAdBlockEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.AD_BLOCK_ENABLED] = enabled
        }
    }

    override suspend fun updateSearchEngineUrl(url: String) {
        dataStore.edit { prefs ->
            prefs[Keys.SEARCH_ENGINE_URL] = url
        }
    }

    override suspend fun updateOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun updateJsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.JS_ENABLED] = enabled
        }
    }

    override suspend fun updateAutoRestoreLastSession(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.AUTO_RESTORE_LAST_SESSION] = enabled
        }
    }
}
