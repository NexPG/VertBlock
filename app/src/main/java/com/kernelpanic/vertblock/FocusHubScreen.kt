package com.kernelpanic.vertblock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.core.net.toUri
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.collectAsState

// Цветовая палитра
val BackgroundColor = Color(0xFF121214)
val SurfaceColor = Color(0xFF1E1E22)
val PrimaryPurple = Color(0xFF8A5BFF)
val TextGray = Color(0xFFA0A0A0)
val DividerColor = Color(0xFF2A2A2E)

@Composable
fun FocusHubScreen(
    focusHubState: FocusHubState,
    onAvatarClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onWatchTimeClick: () -> Unit = {},
    onQuestionStatsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var showAccessibilityDialog by remember { mutableStateOf(false) }

    // Проверяем при каждом возобновлении (Lifecycle)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == androidx.lifecycle.Lifecycle.State.RESUMED) {
            showAccessibilityDialog = !AccessibilityUtils.isAccessibilityServiceEnabled(context, AppWatcherService::class.java)
        }
    }

    if (showAccessibilityDialog) {
        Dialog(
            onDismissRequest = { /* Не позволяем закрыть просто так, если хотим обязательный доступ */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfaceColor,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AccessibilityNew,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Accessibility Service Required",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "To ensure the app works properly, you must grant accessibility service access to VertBlock.",
                        color = TextGray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            if (context is MainActivity) {
                                context.openAccessibilitySettings()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Grant Access", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
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

            Text(
                text = "VertBlock",
                color = PrimaryPurple,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.5).sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            MainScoreCard(
                score = focusHubState.attentionScore,
                streakDays = focusHubState.streakDays
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.RemoveRedEye,
                    title = "Watch\nTime",
                    onClick = onWatchTimeClick
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.SsidChart,
                    title = "Question\nStats",
                    onClick = onQuestionStatsClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Нижние три метрики
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "TIME\nWELL SPENT",
                    value = focusHubState.timeWellSpent,
                    unit = ""
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "STREAK",
                    value = focusHubState.streakDays.toString(),
                    unit = "d"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "BRAIN\nFOOD",
                    value = focusHubState.brainFoodTopics.toString(),
                    unit = "topics"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
// ------------------- Вспомогательные composable -------------------

@Composable
fun TopBar(onAvatarClick: () -> Unit, onSettingsClick: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE) }
    val avatarUriStr = prefs.getString("avatar_uri", null)
    val avatarUri = avatarUriStr?.let { it.toUri() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceColor)
                .border(1.dp, DividerColor, CircleShape)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            if (avatarUri != null) {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = "Profile",
                    tint = Color.LightGray, modifier = Modifier.size(24.dp))
            }
        }

        Text(text = "Focus Hub", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)

        IconButton(onClick = onSettingsClick) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = TextGray)
        }
    }
}

@Composable
fun MainScoreCard(score: Int, streakDays: Int) {
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
                text = score.toString(),
                color = Color.White,
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 120.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                    text = "$streakDays Days Current Streak",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ActionCard(modifier: Modifier = Modifier, icon: ImageVector, title: String, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, color = Color.White, fontSize = 16.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, unit: String) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = SurfaceColor) {
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
                Text(text = value, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                if (unit.isNotEmpty()) {
                    Text(text = " $unit", color = TextGray, fontSize = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
        }
    }
}

@Composable
fun ActivityChart(data: List<Float>) {
    val colors = listOf(PrimaryPurple, Color(0xFFBB86FC), Color(0xFF6200EE), Color(0xFF3700B3))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { index, value ->
            val maxVal = data.maxOrNull() ?: 1f
            val heightFraction = if (maxVal > 0) value / maxVal else 0f
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .height((heightFraction * 80).dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(colors[index % colors.size])
            )
        }
    }
}