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

    NavHost(navController = navController, startDestination = "focus") {
        composable("focus") {
            FocusHubScreen(
                onAvatarClick = { navController.navigate("profile") },
                onSettingsClick = { navController.navigate("settings") },
                onWatchTimeClick = { navController.navigate("watchtime") }
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
    }
}