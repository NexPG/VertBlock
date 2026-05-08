package com.kernelpanic.vertblock

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Палитра из Focus Hub
//val BackgroundColor = Color(0xFF121214)
//val SurfaceColor = Color(0xFF1E1E22)
//val PrimaryPurple = Color(0xFF8A5BFF)
//val TextGray = Color(0xFFA0A0A0)
//val DividerColor = Color(0xFF2A2A2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToInterest: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE) }

    // Загружаем сохранённые данные
    var savedName by remember { mutableStateOf(prefs.getString("user_name", "") ?: "") }
    var savedFrequency by remember { mutableIntStateOf(prefs.getInt("question_frequency", 15)) }

    // Локальные состояния для редактирования
    var nameInput by remember { mutableStateOf(savedName) }
    var selectedPresetTime by remember { mutableStateOf<Int?>(savedFrequency) }
    var customTimeInput by remember { mutableStateOf("") }

    val currentSelectedTime = selectedPresetTime ?: customTimeInput.toIntOrNull() ?: savedFrequency
    val hasChanges =
        (nameInput != savedName && nameInput.isNotBlank()) || (currentSelectedTime != savedFrequency)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {
        // Верхняя панель (такая же, как была)
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
                    text = "Profile Settings",
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
            Spacer(modifier = Modifier.height(32.dp))

            // Аватарка (оставим как есть, без сохранения выбора)
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor)
                        .clickable { /* TODO: выбор фото */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = TextGray,
                        modifier = Modifier.size(60.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomCenter)
                        .offset(x = 40.dp, y = 8.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple)
                        .border(2.dp, BackgroundColor, CircleShape)
                        .clickable { /* TODO */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Имя
            SectionTitle("NAME")
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                placeholder = { Text("enter your name", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = TextGray,
                    cursorColor = PrimaryPurple,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                trailingIcon = {
                    if (nameInput.isNotBlank()) Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = PrimaryPurple
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Preferences (переход в Interest Settings)
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
                Text(
                    "Interest Settings",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextGray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Частота вопросов
            SectionTitle("QUESTION FREQUENCY")
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
                    val bgColor = if (isSelected) PrimaryPurple else Color.Transparent
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable {
                                selectedPresetTime = time
                                customTimeInput = ""
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

            OutlinedTextField(
                value = customTimeInput,
                onValueChange = {
                    customTimeInput = it
                    if (it.isNotBlank()) selectedPresetTime = null
                },
                placeholder = {
                    Text(
                        "Custom (mins)",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = TextGray,
                    cursorColor = PrimaryPurple,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                trailingIcon = {
                    if (selectedPresetTime == null && customTimeInput.isNotBlank())
                        Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryPurple)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Кнопка Save
            Button(
                onClick = {
                    if (hasChanges) {
                        prefs.edit()
                            .putString("user_name", nameInput)
                            .putInt("question_frequency", currentSelectedTime)
                            .apply()
                        savedName = nameInput
                        savedFrequency = currentSelectedTime
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 36.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasChanges) PrimaryPurple else SurfaceColor,
                    contentColor = if (hasChanges) Color.White else TextGray
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    "Save Changes",
                    color = Color.White,   // ← явно задаём белый цвет
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

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