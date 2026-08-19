package com.pratham.webhub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pratham.webhub.ui.main.MainScreen
import com.pratham.webhub.ui.onboarding.OnboardingScreen
import com.pratham.webhub.ui.bookmarks.BookmarksScreen
import com.pratham.webhub.ui.settings.SettingsScreen

/**
 * Sealed class representing every top-level screen in WebHub.
 */
sealed class WebHubScreen(val route: String) {

    /** First-launch onboarding flow. */
    data object Onboarding : WebHubScreen("onboarding")

    /** Main browser screen (home / tabs). */
    data object Main : WebHubScreen("main") {
        const val URL_ARG = "url"
        fun createRoute(url: String? = null): String =
            if (url != null) "main?$URL_ARG=$url" else "main"
    }

    /** Full-screen bookmarks list. */
    data object Bookmarks : WebHubScreen("bookmarks")

    /** Full-screen settings. */
    data object Settings : WebHubScreen("settings")

    companion object {
        const val DEFAULT_START = "main"
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    return navController.currentBackStackEntry?.destination?.route
}

/**
 * The top-level NavHost for WebHub.
 */
@Composable
fun WebHubNavHost(
    navController: NavHostController,
    startDestination: String = WebHubScreen.DEFAULT_START,
    onboardingComplete: Boolean = true,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(WebHubScreen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(WebHubScreen.Main.route) {
                        popUpTo(WebHubScreen.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = WebHubScreen.Main.route + "?${WebHubScreen.Main.URL_ARG}={${WebHubScreen.Main.URL_ARG}}",
            arguments = listOf(
                navArgument(WebHubScreen.Main.URL_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString(WebHubScreen.Main.URL_ARG)
            MainScreen(
                initialUrl = url,
                onNavigateToBookmarks = {
                    navController.navigate(WebHubScreen.Bookmarks.route)
                },
                onNavigateToSettings = {
                    navController.navigate(WebHubScreen.Settings.route)
                },
            )
        }

        composable(WebHubScreen.Bookmarks.route) {
            BookmarksScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(WebHubScreen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
