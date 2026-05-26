package com.kernelpanic.vertblock

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToInterest: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE) }

    // Сохранённые значения
    var savedName by remember { mutableStateOf(prefs.getString("user_name", "") ?: "") }
    var savedFrequencyMin by remember { mutableStateOf(prefs.getFloat("question_frequency_minutes", 15f)) }
    var savedAvatarUri by remember { mutableStateOf(prefs.getString("avatar_uri", null)) }

    // Редактируемые значения
    var nameInput by remember { mutableStateOf(savedName) }
    var selectedPresetTime by remember { mutableStateOf<Int?>(null) }
    var customTimeInput by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf(savedAvatarUri?.let { Uri.parse(it) }) }

    // Инициализация таймера
    LaunchedEffect(savedFrequencyMin) {
        if (listOf(15f, 30f, 45f).contains(savedFrequencyMin)) {
            selectedPresetTime = savedFrequencyMin.toInt()
            customTimeInput = ""
        } else if (savedFrequencyMin > 0f) {
            selectedPresetTime = null
            customTimeInput = savedFrequencyMin.toString()
        }
    }

    val currentSelectedMinutes = selectedPresetTime?.toFloat() ?: customTimeInput.toFloatOrNull() ?: 0f
    val hasChanges = (nameInput != savedName && nameInput.isNotBlank()) ||
            (currentSelectedMinutes != savedFrequencyMin) ||
            (avatarUri?.toString() != savedAvatarUri)

    // Диалог полноэкранного просмотра
    var showFullscreenDialog by remember { mutableStateOf(false) }

    // Лаунчер выбора фото
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            avatarUri = uri // временно сохраняем локально
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "VERTBLOCK", color = TextGray, fontSize = 12.sp,
                    letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Profile Settings", color = Color.White,
                    fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.width(48.dp))
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(32.dp))

            // Аватар
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                // Большой круг – клик открывает полноэкранный просмотр
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor)
                        .clickable { showFullscreenDialog = avatarUri != null },
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
                        Icon(Icons.Default.Person, contentDescription = "Avatar",
                            tint = TextGray, modifier = Modifier.size(60.dp))
                    }
                }

                // Карандаш
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
                    Icon(Icons.Default.Edit, contentDescription = "Edit",
                        tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // NAME
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
                    if (nameInput.isNotBlank()) Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryPurple)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PREFERENCES
            SectionTitle("PREFERENCES")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
                    .background(SurfaceColor)
                    .clickable { onNavigateToInterest() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween  // ← Добавить
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Psychology, contentDescription = null, tint = PrimaryPurple)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Interest Settings", color = Color.White, fontSize = 16.sp)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // QUESTION FREQUENCY
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
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable { selectedPresetTime = time; customTimeInput = "" }
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

            Spacer(modifier = Modifier.height(12.dp))

            // Custom time input с подписью min
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
                    placeholder = { Text("Custom (mins)", color = Color.Gray, textAlign = TextAlign.Center) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = textFieldColors(),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    trailingIcon = {
                        if (isCustomActive) Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryPurple)
                    }
                )
                Text(text = "min", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Кнопка Save
            Button(
                onClick = {
                    if (hasChanges) {
                        prefs.edit()
                            .putString("user_name", nameInput)
                            .putFloat("question_frequency_minutes", currentSelectedMinutes)
                            .putString("avatar_uri", avatarUri?.toString())
                            .apply()
                        // Обновляем "сохранённые" значения, чтобы кнопка стала серой
                        savedName = nameInput
                        savedFrequencyMin = currentSelectedMinutes
                        savedAvatarUri = avatarUri?.toString()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(115.dp).padding(bottom = 36.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasChanges) PrimaryPurple else SurfaceColor,
                    contentColor = if (hasChanges) Color.White else TextGray
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    // Полноэкранный просмотр аватара
    if (showFullscreenDialog && avatarUri != null) {
        Dialog(
            onDismissRequest = { showFullscreenDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { showFullscreenDialog = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = "Avatar fullscreen",
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentScale = ContentScale.Fit
                )
                // Кнопка закрытия в углу
                IconButton(
                    onClick = { showFullscreenDialog = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title, color = TextGray, fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp,
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