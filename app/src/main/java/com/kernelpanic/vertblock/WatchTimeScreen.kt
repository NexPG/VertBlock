package com.kernelpanic.vertblock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import androidx.compose.foundation.shape.CircleShape

// Палитра приложения
//private val BackgroundColor = Color(0xFF121214)
//private val SurfaceColor = Color(0xFF1E1E22)
//private val PrimaryPurple = Color(0xFF8A5BFF)
//private val TextGray = Color(0xFFA0A0A0)
//private val DividerColor = Color(0xFF2A2A2E)

// Модель данных, готовая для работы с БД/ViewModel
data class WatchTimeState(
    val totalHours: Float = 0f,
    val dailyHours: Float = 0f,
    val weeklyHours: Float = 0f,
    val monthlyHours: Float = 0f,
    val yearlyHours: Float = 0f,
    // Проценты по дням недели (от ПН до ВС). Сумма должна быть 100 (или 0 по умолчанию)
    val weeklyPercentages: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val mostActiveDay: String = "None",
    val mostActiveHours: Float = 0f
)

@Composable
fun WatchTimeScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Состояние экрана. Сейчас все по нулям, как ты просил.
    // Когда подключишь БД, просто передавай сюда данные из ViewModel.
    var uiState by remember { mutableStateOf(WatchTimeState()) }

    /* // Раскомментируй этот блок, чтобы увидеть, как выглядит заполненный график:
    uiState = WatchTimeState(
        totalHours = 2840f, dailyHours = 4.2f, weeklyHours = 28.5f, monthlyHours = 114.8f, yearlyHours = 1392f,
        weeklyPercentages = listOf(15f, 30f, 45f, 25f, 60f, 35f, 20f), // Имитация данных графика
        mostActiveDay = "Friday", mostActiveHours = 5.4f
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
                    text = "Watch Time",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(48.dp))
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 2. Карточка TOTAL WATCH TIME
            TotalWatchTimeCard(uiState.totalHours)

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Сетка со статистикой (Daily, Weekly, Monthly, Yearly)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TimeStatCard(modifier = Modifier.weight(1f), title = "Daily", value = uiState.dailyHours)
                TimeStatCard(modifier = Modifier.weight(1f), title = "Weekly", value = uiState.weeklyHours)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TimeStatCard(modifier = Modifier.weight(1f), title = "Monthly", value = uiState.monthlyHours)
                TimeStatCard(modifier = Modifier.weight(1f), title = "Yearly", value = uiState.yearlyHours)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. График Activity Insights
            ActivityInsightsCard(
                percentages = uiState.weeklyPercentages,
                mostActiveDay = uiState.mostActiveDay,
                mostActiveHours = uiState.mostActiveHours
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TotalWatchTimeCard(totalHours: Float) {
    val formatter = DecimalFormat("#,###")
    val formattedHours = formatter.format(totalHours.toInt())

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceColor,
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Декоративная иконка на фоне справа
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = DividerColor.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(120.dp)
                    .offset(x = 20.dp, y = 20.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TOTAL WATCH TIME",
                    color = PrimaryPurple,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formattedHours,
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " hrs",
                        color = TextGray,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TimeStatCard(modifier: Modifier = Modifier, title: String, value: Float) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                color = TextGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${value}h",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActivityInsightsCard(
    percentages: List<Float>,
    mostActiveDay: String,
    mostActiveHours: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Заголовок карточки графика
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SsidChart, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Activity Insights", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Text("AVERAGE / MONTH", color = TextGray, fontSize = 12.sp, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Сам график (Canvas)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val maxPercent = percentages.maxOrNull() ?: 1f
                // Если все нули, предотвращаем деление на 0
                val safeMax = if (maxPercent == 0f) 1f else maxPercent

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val colWidth = width / 7f

                    val path = Path().apply {
                        moveTo(0f, height) // Начинаем с левого нижнего угла

                        for (i in 0 until 7) {
                            val percent = percentages.getOrElse(i) { 0f }
                            // Высчитываем высоту колонки относительно максимума
                            val colHeight = height * (percent / safeMax)
                            val y = height - colHeight
                            val xStart = i * colWidth
                            val xEnd = (i + 1) * colWidth

                            lineTo(xStart, y)
                            lineTo(xEnd, y)
                        }

                        lineTo(width, height) // В правый нижний угол
                        close()
                    }

                    drawPath(
                        path = path,
                        color = PrimaryPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Подписи дней недели
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val days = listOf("M", "T", "W", "T", "F", "S", "S")
                days.forEach { day ->
                    Text(text = day, color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Подвал графика (Disclaimer и Most active)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryPurple))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Activity based on last month", color = TextGray, fontSize = 12.sp)
                }

                val activeText = if (mostActiveHours == 0f) "No data yet" else "Most active: $mostActiveDay (${mostActiveHours}h)"
                Text(activeText, color = TextGray, fontSize = 12.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWatchTimeScreen() {
    WatchTimeScreen()
}