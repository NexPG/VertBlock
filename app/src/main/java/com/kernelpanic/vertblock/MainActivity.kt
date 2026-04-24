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
            // Здесь передаём колбэк, который будет вызван при нажатии на аватар
            FocusHubScreen(onAvatarClick = {
                navController.navigate("profile")
            })
        }
        composable("profile") {
            ProfileSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("interests") {
            InterestSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}