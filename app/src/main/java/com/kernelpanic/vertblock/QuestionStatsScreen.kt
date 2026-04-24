package com.kernelpanic.vertblock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

// Палитра приложения
//private val BackgroundColor = Color(0xFF121214)
//private val SurfaceColor = Color(0xFF1E1E22)
//private val PrimaryPurple = Color(0xFF8A5BFF)
//private val TextGray = Color(0xFFA0A0A0)
//private val DividerColor = Color(0xFF2A2A2E)
private val BarBackgroundColor = Color(0xFF18181A) // Чуть темнее карточки для фона полоски

// Модель для одной строки статистики
data class StatItem(val label: String, val value: Int)

// Модель данных для экрана (готова к подключению БД)
data class QuestionStatsState(
    val totalAnswers: Int = 0,
    val attempts: List<StatItem> = listOf(
        StatItem("1st try", 0),
        StatItem("2nd try", 0),
        StatItem("3rd try", 0),
        StatItem("4th try", 0)
    ),
    val categories: List<StatItem> = listOf(
        StatItem("Math", 0),
        StatItem("History", 0),
        StatItem("Science", 0),
        StatItem("Tech", 0),
        StatItem("Health", 0),
        StatItem("Art", 0),
        StatItem("Travel", 0),
        StatItem("Music", 0),
        StatItem("Custom", 0) // Добавленная кастомная тема
    )
)

@Composable
fun QuestionStatsScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Состояние экрана. По умолчанию нули.
    var uiState by remember { mutableStateOf(QuestionStatsState()) }

    /* // РАСКОММЕНТИРУЙ для проверки внешнего вида с данными (сумма = 1284):
    uiState = QuestionStatsState(
        totalAnswers = 1284,
        attempts = listOf(
            StatItem("1st try", 842), StatItem("2nd try", 312),
            StatItem("3rd try", 98), StatItem("4th try", 32)
        ),
        categories = listOf(
            StatItem("Math", 350), StatItem("History", 250),
            StatItem("Science", 200), StatItem("Tech", 150),
            StatItem("Health", 100), StatItem("Art", 80),
            StatItem("Travel", 60), StatItem("Music", 50),
            StatItem("Custom", 44)
        )
    )
    */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {
        // 1. Верхняя панель (Top Bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                    text = "Question Stats",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(48.dp))
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        // Основной контент экрана
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Адаптивный отступ сверху
            Spacer(modifier = Modifier.weight(0.5f))

            // 2. Блок Total Answers
            val formatter = DecimalFormat("#,###")
            Text(
                text = formatter.format(uiState.totalAnswers),
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp
            )
            Text(
                text = "TOTAL ANSWERS",
                color = TextGray,
                fontSize = 12.sp,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Адаптивный отступ между тоталом и первой карточкой
            Spacer(modifier = Modifier.weight(0.8f))

            // 3. Карточка Attempt Breakdown
            StatsCard(
                title = "ATTEMPT BREAKDOWN",
                items = uiState.attempts,
                totalValue = uiState.totalAnswers
            )

            // Адаптивный отступ между карточками
            Spacer(modifier = Modifier.weight(0.6f))

            // 4. Карточка Top Categories
            StatsCard(
                title = "TOP CATEGORIES",
                items = uiState.categories,
                totalValue = uiState.totalAnswers
            )

            // Отступ снизу
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    items: List<StatItem>,
    totalValue: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceColor,
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                color = TextGray,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            items.forEachIndexed { index, statItem ->
                StatRowItem(
                    item = statItem,
                    totalValue = totalValue
                )
                // Отступ между строками (у последней не делаем)
                if (index < items.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun StatRowItem(
    item: StatItem,
    totalValue: Int
) {
    // Защита от деления на 0. Если тотал 0, то прогресс 0f
    val fraction = if (totalValue > 0) {
        (item.value.toFloat() / totalValue.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Жесткая фиксация левого текста для идеального выравнивания
        Text(
            text = item.label,
            color = TextGray,
            fontSize = 14.sp,
            modifier = Modifier.width(64.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Полоска прогресса (занимает всё доступное место между текстами)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(BarBackgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    // fillMaxWidth(fraction) заполнит процент от родителя
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(50))
                    .background(PrimaryPurple)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Жесткая фиксация правого значения для идеального выравнивания по правому краю
        Text(
            text = item.value.toString(),
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewQuestionStatsScreen() {
    QuestionStatsScreen()
}