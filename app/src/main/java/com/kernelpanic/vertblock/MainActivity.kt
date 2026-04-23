package com.kernelpanic.vertblock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kernelpanic.vertblock.ui.theme.VertBlockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        setContent {
            VertBlockTheme {
                FocusHubScreen()   // Ваш экран Focus Hub
            }
        }
    }
}