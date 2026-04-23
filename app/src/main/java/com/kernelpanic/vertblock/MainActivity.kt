package com.kernelpanic.vertblock

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.kernelpanic.vertblock.ui.theme.VertBlockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Запускаем Python и получаем приветствие
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val pythonGreeting = py.getModule("hello").callAttr("greet", "VertBlock").toString()

        // 2. Передаём это приветствие в интерфейс
        setContent {
            VertBlockTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = pythonGreeting,   // <-- вместо "Android" подставляем ответ от Python
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",   // теперь отобразится "Hello <ответ от Python>!"
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VertBlockTheme {
        Greeting("Android")
    }
}