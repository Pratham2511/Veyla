package com.pratham.webhub.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
 * Sealed class representing every top-level screen in Veyla.
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
 * The top-level NavHost for Veyla.
 *
 * Uses short fade transitions to eliminate the visual flash that occurs
 * when navigating between screens (Phase 12 fix). The fade is fast enough
 * (220ms) to feel immediate while preventing the blank intermediate frame
 * that Compose's default NavHost transition can produce when destination
 * screens have different background colors or take a frame to compose.
 */
@Composable
fun WebHubNavHost(
    navController: NavHostController,
    startDestination: String = WebHubScreen.DEFAULT_START,
    onboardingComplete: Boolean = true,
    initialUrl: String? = null,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        // Smooth cross-fade so no blank frame is visible between screens.
        enterTransition = { fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(180)) },
    ) {
        composable(WebHubScreen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(WebHubScreen.Main.createRoute(initialUrl)) {
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
