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
        // Анимация появления нового экрана
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth / 4 }, // выезд на четверть экрана
                animationSpec = tween(durationMillis = 350)
            ) + fadeIn(animationSpec = tween(200))
        },
        // Анимация ухода текущего экрана
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 6 }, // небольшой сдвиг влево
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(200))
        },
        // Анимация возврата предыдущего экрана
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 6 }, // появление слева
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(200))
        },
        // Анимация ухода текущего экрана при жесте "Назад"
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth / 4 }, // уход вправо
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(200))
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