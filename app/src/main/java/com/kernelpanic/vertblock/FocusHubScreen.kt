package com.kernelpanic.vertblock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable

// Цветовая палитра на основе макета
val BackgroundColor = Color(0xFF121214)
val SurfaceColor = Color(0xFF1E1E22)
val PrimaryPurple = Color(0xFF8A5BFF)
val TextGray = Color(0xFFA0A0A0)
val DividerColor = Color(0xFF2A2A2E)

@Composable
fun FocusHubScreen(onAvatarClick: () -> Unit = {},
                   onSettingsClick: () -> Unit = {}
                   ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()      // Отступ системного статус бара
    ) {
        TopBar(onAvatarClick = onAvatarClick, onSettingsClick = onSettingsClick)
        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Заголовок
            Text(
                text = "VertBlock",
                color = PrimaryPurple,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.5).sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Главная карточка
            MainScoreCard()

            Spacer(modifier = Modifier.height(24.dp))

            // Средний ряд кнопок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.RemoveRedEye,
                    title = "Watch\nTime"
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.SsidChart, // Замените на нужную иконку графика
                    title = "Question\nStats"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Нижний ряд статистики
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "FOCUS\nDEPTH",
                    value = "0",
                    unit = "%"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "RECALL\nRATE",
                    value = "0",
                    unit = "%"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "ACTIVE\nHOURS",
                    value = "0",
                    unit = "h"
                )
            }
        }
    }
}

@Composable
fun TopBar(onAvatarClick: () -> Unit = {},
           onSettingsClick: () -> Unit = {}
           ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Заглушка для аватара
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceColor)
                .border(1.dp, DividerColor, CircleShape)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.LightGray, modifier = Modifier.size(24.dp))
        }

        Text(
            text = "Focus Hub",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = TextGray
            )
        }
    }
}

@Composable
fun MainScoreCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ATTENTION SCORE",
                color = TextGray,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "0",
                color = Color.White,
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 120.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Бейдж стрика (серии)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .border(1.dp, DividerColor, RoundedCornerShape(percent = 50))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "0 Days Current Streak",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ActionCard(modifier: Modifier = Modifier, icon: ImageVector, title: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, unit: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = TextGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " $unit",
                    color = TextGray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFocusHubScreen() {
    FocusHubScreen()
}