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
import androidx.compose.animation.slideOutHorizontally
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween

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
        enterTransition = { EnterTransition.None },   // новый экран появляется мгновенно
        exitTransition = { ExitTransition.None },     // старый экран не анимируется при уходе вперёд
        popEnterTransition = { EnterTransition.None },// возвращаемый экран уже на месте
        popExitTransition = {                         // текущий экран уезжает вправо
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        }
    ) {
        // ... ваши маршруты остаются без изменений
        composable("focus") {
            FocusHubScreen(
                onAvatarClick = dropUnlessResumed { navController.navigate("profile") },
                onSettingsClick = dropUnlessResumed { navController.navigate("settings") },
                onWatchTimeClick = dropUnlessResumed { navController.navigate("watchtime") },
                onQuestionStatsClick = dropUnlessResumed { navController.navigate("questionstats") }
            )
        }
        composable("profile") {
            ProfileSettingsScreen(
                onNavigateBack = dropUnlessResumed { navController.popBackStack() },
                onNavigateToInterest = dropUnlessResumed { navController.navigate("interests") }
            )
        }
        composable("interests") {
            InterestSettingsScreen(
                onNavigateBack = dropUnlessResumed { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = dropUnlessResumed { navController.popBackStack() }
            )
        }
        composable("watchtime") {
            WatchTimeScreen(onNavigateBack = dropUnlessResumed { navController.popBackStack() })
        }
        composable("questionstats") {   // новый маршрут
            QuestionStatsScreen(onNavigateBack = dropUnlessResumed { navController.popBackStack() })
        }
    }
}