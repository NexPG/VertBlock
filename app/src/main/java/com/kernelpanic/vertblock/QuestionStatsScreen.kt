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
private val BarBackgroundColor = Color(0xFF18181A)

// Модель для одной строки статистики
data class StatItem(val label: String, val value: Int)

@Composable
fun QuestionStatsScreen(
    questionStatsState: QuestionStatsState,   // ← теперь принимает состояние
    onNavigateBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {
        // Верхняя панель
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

        // Основной контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            // Общее число ответов
            val formatter = DecimalFormat("#,###")
            Text(
                text = formatter.format(questionStatsState.totalAnswers),
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

            Spacer(modifier = Modifier.weight(0.8f))

            // Карточка Attempt Breakdown
            StatsCard(
                title = "ATTEMPT BREAKDOWN",
                items = questionStatsState.attempts,
                totalValue = questionStatsState.totalAnswers
            )

            Spacer(modifier = Modifier.weight(0.6f))

            // Карточка Top Categories
            StatsCard(
                title = "TOP CATEGORIES",
                items = questionStatsState.categories,
                totalValue = questionStatsState.totalAnswers
            )

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
    val fraction = if (totalValue > 0) {
        (item.value.toFloat() / totalValue.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.label,
            color = TextGray,
            fontSize = 14.sp,
            modifier = Modifier.width(64.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

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
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(50))
                    .background(PrimaryPurple)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

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
    QuestionStatsScreen(questionStatsState = QuestionStatsState())
}