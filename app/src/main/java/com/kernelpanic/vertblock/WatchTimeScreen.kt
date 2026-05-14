package com.kernelpanic.vertblock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Палитра приложения
//private val BackgroundColor = Color(0xFF121214)
//private val SurfaceColor = Color(0xFF1E1E22)
//private val PrimaryPurple = Color(0xFF8A5BFF)
//private val TextGray = Color(0xFFA0A0A0)
//private val DividerColor = Color(0xFF2A2A2E)

data class WatchTimeState(
    val totalSeconds: Int = 0,
    val dailySeconds: Int = 0,
    val weeklySeconds: Int = 0,
    val monthlySeconds: Int = 0,
    val yearlySeconds: Int = 0,
    val weeklyPercentages: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val mostActiveDay: String = "None",
    val mostActiveHours: Float = 0f
)

@Composable
fun WatchTimeScreen(
    watchTimeState: WatchTimeState,
    onNavigateBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {
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
            Spacer(modifier = Modifier.height(12.dp))

            TotalWatchTimeCard(watchTimeState.totalSeconds)

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TimeStatCard(modifier = Modifier.weight(1f), title = "Daily", seconds = watchTimeState.dailySeconds)
                    TimeStatCard(modifier = Modifier.weight(1f), title = "Weekly", seconds = watchTimeState.weeklySeconds)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TimeStatCard(modifier = Modifier.weight(1f), title = "Monthly", seconds = watchTimeState.monthlySeconds)
                    TimeStatCard(modifier = Modifier.weight(1f), title = "Yearly", seconds = watchTimeState.yearlySeconds)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.padding(bottom = 36.dp)) {
                    ActivityInsightsCard(
                        percentages = watchTimeState.weeklyPercentages,
                        mostActiveDay = watchTimeState.mostActiveDay,
                        mostActiveHours = watchTimeState.mostActiveHours
                    )
                }
            }
        }
    }
}

@Composable
fun TotalWatchTimeCard(totalSeconds: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceColor,
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
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
                Text(
                    text = formatWatchTime(totalSeconds),
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TimeStatCard(modifier: Modifier = Modifier, title: String, seconds: Int) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        border = BorderStroke(1.dp, DividerColor)
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
                text = formatWatchTime(seconds),
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
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceColor,
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val maxPercent = percentages.maxOrNull() ?: 1f
                val safeMax = if (maxPercent == 0f) 1f else maxPercent

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val topPadding = 12.dp.toPx()
                    val bottomPadding = 4.dp.toPx()
                    val availableHeight = height - topPadding - bottomPadding
                    val colWidth = width / 7f
                    val barWidth = colWidth * 0.66f
                    val cornerRadius = 8.dp.toPx()

                    for (i in 0 until 7) {
                        val percent = percentages.getOrElse(i) { 0f }
                        val barHeight = availableHeight * (percent / safeMax)
                        val x = i * colWidth + (colWidth - barWidth) / 2f
                        val y = topPadding + (availableHeight - barHeight)

                        drawRoundRect(
                            color = PrimaryPurple,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                        )
                        drawRoundRect(
                            color = DividerColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

fun formatWatchTime(totalSeconds: Int): String {
    if (totalSeconds <= 0) return "0s"
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> String.format("%dh %dm", hours, minutes)
        minutes > 0 -> String.format("%dm %ds", minutes, seconds)
        else -> String.format("%ds", seconds)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWatchTimeScreen() {
    WatchTimeScreen(watchTimeState = WatchTimeState())
}