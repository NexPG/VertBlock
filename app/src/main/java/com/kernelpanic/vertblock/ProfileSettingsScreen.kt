package com.kernelpanic.vertblock

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Используем палитру из Focus Hub
// val BackgroundColor = Color(0xFF121214)
// val SurfaceColor = Color(0xFF1E1E22)
// val PrimaryPurple = Color(0xFF8A5BFF)
// val TextGray = Color(0xFFA0A0A0)
// val DividerColor = Color(0xFF2A2A2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToInterest: () -> Unit = {}
) {
    // === ИМИТАЦИЯ СОХРАНЕННЫХ ДАННЫХ (в будущем заменишь на DataStore) ===
    var savedName by remember { mutableStateOf("") }
    var savedFrequency by remember { mutableIntStateOf(15) }

    // === ЛОКАЛЬНЫЕ СОСТОЯНИЯ ЭКРАНА (то, что мы редактируем) ===
    var nameInput by remember { mutableStateOf(savedName) }
    var selectedPresetTime by remember { mutableStateOf<Int?>(savedFrequency) }
    var customTimeInput by remember { mutableStateOf("") }

    // Логика: есть ли несохраненные изменения?
    val currentSelectedTime = selectedPresetTime ?: customTimeInput.toIntOrNull() ?: savedFrequency
    val hasChanges = (nameInput != savedName && nameInput.isNotBlank()) || (currentSelectedTime != savedFrequency)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding() // Отступ для статус-бара, как ты просил
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
                    text = "Profile Settings",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Пустая коробка для баланса центрирования
            Spacer(modifier = Modifier.width(48.dp))
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 2. Аватарка
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor)
                        .clickable {
                            // TODO: Открыть галерею для выбора фото
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = TextGray,
                        modifier = Modifier.size(60.dp)
                    )
                }

                // Иконка редактирования (карандаш)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomCenter)
                        .offset(x = 40.dp, y = 8.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple)
                        .border(2.dp, BackgroundColor, CircleShape)
                        .clickable { /* TODO: Тоже открыть галерею */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 3. Поле Name
            SectionTitle("NAME")
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                placeholder = { Text("enter your name", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                trailingIcon = {
                    if (nameInput.isNotBlank()) {
                        Icon(Icons.Default.Check, contentDescription = "Valid", tint = PrimaryPurple)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Поле Preferences (Interest Settings)
            SectionTitle("PREFERENCES")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
                    .background(SurfaceColor)
                    .clickable { onNavigateToInterest() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Psychology, contentDescription = null, tint = PrimaryPurple)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Interest Settings", color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Поле Question Frequency
            SectionTitle("QUESTION FREQUENCY")

            // Кастомный Segmented Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
                    .background(SurfaceColor)
                    .padding(4.dp)
            ) {
                listOf(15, 30, 45).forEach { time ->
                    val isSelected = selectedPresetTime == time
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) PrimaryPurple else Color.Transparent,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable {
                                selectedPresetTime = time
                                customTimeInput = "" // Сбрасываем кастомное время
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$time mins",
                            color = if (isSelected) Color.White else TextGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кастомный ввод времени
            val isCustomActive = selectedPresetTime == null && customTimeInput.isNotBlank()
            OutlinedTextField(
                value = customTimeInput,
                onValueChange = {
                    customTimeInput = it
                    if (it.isNotBlank()) selectedPresetTime = null // Сбрасываем "плитку"
                },
                placeholder = {
                    Text("Custom (mins)", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                trailingIcon = {
                    if (isCustomActive) {
                        Icon(Icons.Default.Check, contentDescription = "Valid custom time", tint = PrimaryPurple)
                    }
                }
            )

            // Отступ, который выталкивает кнопку "Save Changes" в самый низ
            Spacer(modifier = Modifier.weight(1f))

            // 6. Кнопка сохранения
            Button(
                onClick = {
                    if (hasChanges) {
                        // Имитация сохранения данных
                        savedName = nameInput
                        savedFrequency = currentSelectedTime
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .padding(bottom = 36.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasChanges) PrimaryPurple else SurfaceColor,
                    contentColor = if (hasChanges) Color.White else TextGray
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Вспомогательный компонент для заголовков секций
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = TextGray,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileSettingsScreen() {
    ProfileSettingsScreen()
}