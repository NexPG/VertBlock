package com.kernelpanic.vertblock

import android.R
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Наша стандартная палитра
//private val BackgroundColor = Color(0xFF121214)
//private val SurfaceColor = Color(0xFF1E1E22)
//private val PrimaryPurple = Color(0xFF8A5BFF)
//private val TextGray = Color(0xFFA0A0A0)
//private val DividerColor = Color(0xFF2A2A2E)

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Получаем контекст для запуска браузера
    val context = LocalContext.current
    val githubUrl = "https://github.com/NexPG/VertBlock"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding() // Отступ для статус-бара
    ) {
        // 1. Верхняя панель (Top Bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Focus Hub",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VERTBLOCK",
                    color = TextGray,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Пустая коробка для правильного центрирования текста (компенсирует кнопку "Назад")
            Spacer(modifier = Modifier.width(48.dp))
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        // 2. Основной контент (Карточка по центру)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceColor,
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Текст разработчика
                    Text(
                        text = "Developed by Kernel Panic",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Копирайт
                    Text(
                        text = "© 2026",
                        color = TextGray,
                        fontSize = 14.sp,
                        letterSpacing = 4.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Заголовок Open Source
                    Text(
                        text = "OPEN SOURCE",
                        color = TextGray,
                        fontSize = 14.sp,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка GitHub
                    IconButton(
                        onClick = {
                            // Логика открытия ссылки в браузере
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple)
                    ) {
                        // ВНИМАНИЕ: Замени R.drawable.ic_github на название твоего файла иконки
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu_help), // Временная заглушка
                            contentDescription = "GitHub Repository",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen()
}