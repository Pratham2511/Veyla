package com.pratham.webhub.domain.model

data class AppSettings(
    val activeWorkspaceId: String? = null,
    val globalThemeMode: String = "system",
    val defaultWorkspaceId: String? = null,
    val isBiometricEnabled: Boolean = false,
    val adBlockEnabled: Boolean = true,
    val searchEngineUrl: String = "https://www.google.com/search?q=",
    val hasCompletedOnboarding: Boolean = false
)