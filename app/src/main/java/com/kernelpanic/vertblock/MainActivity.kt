package com.kernelpanic.vertblock

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kernelpanic.vertblock.ui.theme.VertBlockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        setContent {
            VertBlockTheme {
                // Запрос разрешения на уведомления при старте
                RequestNotificationPermission()
                AppNavigation()
            }
        }
    }

    // Функция открытия системных настроек Accessibility Service
    fun openAccessibilitySettings() {
        val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { _ ->
            // Разрешение запрошено, результат не важен
        }

        val context = LocalContext.current
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            LaunchedEffect(Unit) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        }
    ) {
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
        composable("questionstats") {   // исправлено: две 's'
            QuestionStatsScreen(onNavigateBack = dropUnlessResumed { navController.popBackStack() })
        }
    }
}