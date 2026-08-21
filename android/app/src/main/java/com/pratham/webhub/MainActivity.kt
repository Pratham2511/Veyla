package com.pratham.webhub

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.pratham.webhub.domain.model.AppSettings
import com.pratham.webhub.domain.repository.SettingsRepository
import com.pratham.webhub.domain.usecase.session.AutoRestoreSessionUseCase
import com.pratham.webhub.security.BiometricAuthManager
import com.pratham.webhub.ui.navigation.WebHubNavHost
import com.pratham.webhub.ui.theme.WebHubTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * The launcher activity.
 *
 * Implements a tri-state startup to eliminate the onboarding flash on
 * relaunch — see [SettingsLoadState].
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var biometricAuthManager: BiometricAuthManager

    @Inject
    lateinit var autoRestoreSessionUseCase: AutoRestoreSessionUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Extract URL from share-intent or deep-link
        val sharedUrl = when {
            intent?.action == Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            intent?.data != null -> intent.data.toString()
            else -> null
        }

        // Handle dynamic shortcuts created by the app
        val shortcutUrl = intent.getStringExtra("shortcut_url")
        val initialUrl = sharedUrl ?: shortcutUrl

        enableEdgeToEdge()
        setContent {
            // ── Tri-state startup ────────────────────────────────────────────
            // We must NOT render the onboarding screen as the default fallback
            // while the persisted settings are still loading. Otherwise users
            // who already completed onboarding see a brief onboarding flash
            // every time the app cold-starts.
            //
            // State machine:
            //   Loading / Unknown  →  show a calm branded splash
            //   Loaded              →  route to "main" or "onboarding" based on
            //                          the persisted `hasCompletedOnboarding` flag
            val persistedSettings by produceState<SettingsLoadState>(
                initialValue = SettingsLoadState.Loading,
                producer = {
                    val settings = settingsRepository.getSettings().first()
                    value = SettingsLoadState.Loaded(settings)
                }
            )

            // Trigger auto-restore once settings are loaded and onboarding is
            // complete. This honors the "Auto-restore last session" toggle.
            LaunchedEffect(persistedSettings) {
                val loaded = persistedSettings as? SettingsLoadState.Loaded ?: return@LaunchedEffect
                if (loaded.settings.hasCompletedOnboarding &&
                    loaded.settings.autoRestoreLastSession
                ) {
                    runCatching { autoRestoreSessionUseCase() }
                }
            }

            when (val state = persistedSettings) {
                SettingsLoadState.Loading -> {
                    // Calm branded splash — never renders onboarding UI.
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {}
                }
                is SettingsLoadState.Loaded -> {
                    val settings = state.settings

                    WebHubTheme(
                        darkTheme = when (settings.globalThemeMode) {
                            "dark" -> true
                            "light" -> false
                            else -> isSystemInDarkTheme()
                        }
                    ) {
                        // Biometric lock: if the user has enabled it AND the
                        // device can authenticate, gate the entire NavHost
                        // behind a BiometricPrompt until authentication succeeds.
                        val biometricRequired = settings.isBiometricEnabled &&
                                biometricAuthManager.canAuthenticate()

                        var isAuthenticated by remember { mutableStateOf(!biometricRequired) }
                        var promptTrigger by remember { mutableStateOf(0) }

                        if (biometricRequired && !isAuthenticated) {
                            BiometricLockScreen(
                                activity = this@MainActivity,
                                biometricAuthManager = biometricAuthManager,
                                trigger = promptTrigger,
                                onAuthenticated = { isAuthenticated = true },
                                onRetry = { promptTrigger++ }
                            )
                        } else {
                            val navController = rememberNavController()
                            val startDestination =
                                if (settings.hasCompletedOnboarding) "main" else "onboarding"

                            WebHubNavHost(
                                navController = navController,
                                startDestination = startDestination,
                                onboardingComplete = settings.hasCompletedOnboarding,
                                initialUrl = initialUrl
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

/**
 * Tri-state startup indicator for persisted settings.
 *
 * The UI must not assume onboarding is required until persistence has
 * been resolved — see the onboarding-flash fix in [MainActivity].
 */
sealed class SettingsLoadState {
    /** Persistence has not been resolved yet. */
    data object Loading : SettingsLoadState()

    /** Persisted settings have been read from DataStore. */
    data class Loaded(val settings: AppSettings) : SettingsLoadState()
}

/**
 * Lock screen overlay shown when the user has enabled biometric
 * authentication. Triggers a [BiometricPrompt] on first composition
 * and again whenever [trigger] changes.
 */
@androidx.compose.runtime.Composable
private fun BiometricLockScreen(
    activity: FragmentActivity,
    biometricAuthManager: BiometricAuthManager,
    trigger: Int,
    onAuthenticated: () -> Unit,
    onRetry: () -> Unit,
) {
    LaunchedEffect(trigger) {
        val success = biometricAuthManager.authenticate(
            activity = activity,
            promptTitle = "Unlock Veyla",
            promptSubtitle = "Authenticate to continue"
        )
        if (success) {
            onAuthenticated()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Veyla is locked",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Authenticate to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(32.dp))
                Button(onClick = onRetry) {
                    Text("Unlock")
                }
            }
        }
    }
}
