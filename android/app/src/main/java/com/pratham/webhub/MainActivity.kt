package com.pratham.webhub

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.pratham.webhub.domain.model.AppSettings
import com.pratham.webhub.domain.repository.SettingsRepository
import com.pratham.webhub.ui.navigation.WebHubNavHost
import com.pratham.webhub.ui.theme.WebHubTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

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
            val settings by settingsRepository.getSettings().collectAsState(initial = AppSettings())

            WebHubTheme(
                darkTheme = when (settings.globalThemeMode) {
                    "dark" -> true
                    "light" -> false
                    else -> isSystemInDarkTheme()
                }
            ) {
                val navController = rememberNavController()
                val startDestination = if (settings.hasCompletedOnboarding) "main" else "onboarding"

                WebHubNavHost(
                    navController = navController,
                    startDestination = startDestination,
                    onboardingComplete = settings.hasCompletedOnboarding
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle new share intents while the app is already running
        setIntent(intent)
    }
}
