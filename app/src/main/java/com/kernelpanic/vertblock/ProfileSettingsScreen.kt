package com.kernelpanic.vertblock

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

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
    var savedFrequencyMin by remember { mutableStateOf(prefs.getFloat("question_frequency_minutes", 15f)) }
    var savedAvatarUri by remember { mutableStateOf(prefs.getString("avatar_uri", null)) }

    // Локальные состояния для редактирования
    var nameInput by remember { mutableStateOf(savedName) }
    var selectedPresetTime by remember { mutableStateOf<Int?>(null) }
    var customTimeInput by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf(savedAvatarUri?.let { Uri.parse(it) }) }

    // Инициализация пресета/кастомного времени
    LaunchedEffect(savedFrequencyMin) {
        if (listOf(15f, 30f, 45f).contains(savedFrequencyMin)) {
            selectedPresetTime = savedFrequencyMin.toInt()
            customTimeInput = ""
        } else if (savedFrequencyMin > 0f) {
            selectedPresetTime = null
            customTimeInput = savedFrequencyMin.toString() // 0.5 -> "0.5"
        }
    }

    val currentSelectedMinutes = selectedPresetTime?.toFloat() ?: customTimeInput.toFloatOrNull() ?: 0f
    val hasChanges = (nameInput != savedName && nameInput.isNotBlank()) ||
            (currentSelectedMinutes != savedFrequencyMin) ||
            (avatarUri?.toString() != savedAvatarUri)

    // Лаунчер для выбора нового аватара
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            avatarUri = uri
            // Меняем только локально, сохранение будет по кнопке Save
        }
    }

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

            // Аватар
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Большой круг: клик открывает просмотр
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor)
                        .clickable {
                            avatarUri?.let { uri ->
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "image/*")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUri != null) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = TextGray,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                // Кнопка редактирования (карандаш)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomCenter)
                        .offset(x = 40.dp, y = 8.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple)
                        .border(2.dp, BackgroundColor, CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
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

            // Имя (текст по центру)
            SectionTitle("NAME")
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                placeholder = { Text("enter your name", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                trailingIcon = {
                    if (nameInput.isNotBlank()) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryPurple)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Preferences
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

            // Частота вопросов — пресеты с анимацией
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

            // Кастомное время + подпись "min"
            Column(modifier = Modifier.fillMaxWidth()) {
                val isCustomActive = selectedPresetTime == null && customTimeInput.isNotBlank()
                OutlinedTextField(
                    value = customTimeInput,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*\\.?\\d{0,1}$"))) {
                            customTimeInput = it
                            if (it.isNotBlank()) selectedPresetTime = null
                        }
                    },
                    placeholder = {
                        Text("Custom (mins)", color = Color.Gray, textAlign = TextAlign.Center)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = textFieldColors(),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    trailingIcon = {
                        if (isCustomActive)
                            Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryPurple)
                    }
                )
                Text(
                    text = "min",
                    color = TextGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            // Отступ, выталкивающий кнопку вниз
            Spacer(modifier = Modifier.weight(1f))

            // Кнопка сохранения (как раньше)
            Button(
                onClick = {
                    if (hasChanges) {
                        // Сохраняем всё в SharedPreferences
                        prefs.edit()
                            .putString("user_name", nameInput)
                            .putFloat("question_frequency_minutes", currentSelectedMinutes)
                            .putString("avatar_uri", avatarUri?.toString())
                            .apply()
                        // Обновляем локальные «сохранённые» значения, чтобы кнопка стала серой
                        savedName = nameInput
                        savedFrequencyMin = currentSelectedMinutes
                        savedAvatarUri = avatarUri?.toString()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)          // как раньше
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
                    // цвет наследуется от contentColor кнопки – больше не перебиваем его вручную
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

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryPurple,
    unfocusedBorderColor = TextGray,
    cursorColor = PrimaryPurple,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
)