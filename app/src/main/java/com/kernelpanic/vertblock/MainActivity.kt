package com.kernelpanic.vertblock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kernelpanic.vertblock.ui.theme.VertBlockTheme
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        setContent {
            VertBlockTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "focus",
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth / 4 },
                animationSpec = tween(durationMillis = 200)
            ) // Никакого fadeIn!
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 6 },
                animationSpec = tween(durationMillis = 200)
            ) // Никакого fadeOut!
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 6 },
                animationSpec = tween(durationMillis = 200)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth / 4 },
                animationSpec = tween(durationMillis = 200)
            )
        }
    ) {
        // ... ваши маршруты остаются без изменений
        composable("focus") {
            FocusHubScreen(
                onAvatarClick = { navController.navigate("profile") },
                onSettingsClick = { navController.navigate("settings") },
                onWatchTimeClick = { navController.navigate("watchtime") },
                onQuestionStatsClick = { navController.navigate("questionstats") }
            )
        }
        composable("profile") {
            ProfileSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInterest = { navController.navigate("interests") }
            )
        }
        composable("interests") {
            InterestSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("watchtime") {
            WatchTimeScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("questionstats") {   // новый маршрут
            QuestionStatsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}