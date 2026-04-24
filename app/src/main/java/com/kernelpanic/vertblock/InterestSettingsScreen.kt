package com.kernelpanic.vertblock

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

// Палитра приложения
//private val BackgroundColor = Color(0xFF121214)
//private val SurfaceColor = Color(0xFF1E1E22)
//private val PrimaryPurple = Color(0xFF8A5BFF)
//private val TextGray = Color(0xFFA0A0A0)
//private val DividerColor = Color(0xFF2A2A2E)

// Модель данных для темы
data class InterestTopic(
    val id: String,
    val title: String,
    val icon: ImageVector
)

// Список всех доступных тем
val topicsList = listOf(
    InterestTopic("tech", "TECH", Icons.Default.Code),
    InterestTopic("art", "ART", Icons.Default.Palette),
    InterestTopic("health", "HEALTH", Icons.Default.FitnessCenter),
    InterestTopic("science", "SCIENCE", Icons.Default.Science),
    InterestTopic("history", "HISTORY", Icons.Default.History),
    InterestTopic("travel", "TRAVEL", Icons.Default.Flight),
    InterestTopic("music", "MUSIC", Icons.Default.MusicNote),
    InterestTopic("nature", "NATURE", Icons.Default.Park)
)

@Composable
fun InterestSettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Хранилище выбранных тем. Используем Set, чтобы элементы не повторялись
    var selectedTopics by remember { mutableStateOf(setOf<String>()) }

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
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
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
                    text = "Interest Settings",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Пустая коробка для правильного центрирования текста
            Spacer(modifier = Modifier.width(48.dp))
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        // 2. Описание экрана
        Text(
            text = "Choose topics that spark your curiosity to\npersonalize your focus hub.",
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            lineHeight = 24.sp
        )

        // 3. Сетка с плитками (LazyVerticalGrid)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 столбца
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(topicsList) { topic ->
                val isSelected = selectedTopics.contains(topic.id)

                InterestTile(
                    topic = topic,
                    isSelected = isSelected,
                    onClick = {
                        // Логика добавления/удаления из списка выбранных
                        selectedTopics = if (isSelected) {
                            selectedTopics - topic.id
                        } else {
                            selectedTopics + topic.id
                        }

                        // TODO: Здесь можно сразу сохранять выбранные данные в DataStore или БД
                        println("Текущие выбранные темы: $selectedTopics")
                    }
                )
            }
        }
    }
}

@Composable
fun InterestTile(
    topic: InterestTopic,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Анимация плавного изменения цветов при клике
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else SurfaceColor,
        animationSpec = tween(300), label = "bgColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryPurple else TextGray,
        animationSpec = tween(300), label = "contentColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryPurple else Color.Transparent,
        animationSpec = tween(300), label = "borderColor"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f) // Делает плитку квадратной
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = topic.icon,
                contentDescription = topic.title,
                tint = contentColor,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = topic.title,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }

        // Галочка в правом верхнем углу (появляется только если выбрано)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(20.dp)
                    .background(PrimaryPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewInterestSettingsScreen() {
    InterestSettingsScreen()
}