package com.kernelpanic.vertblock

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class InterestTopic(
    val id: String,
    val title: String,
    val icon: ImageVector
)

val topicsList = listOf(
    InterestTopic("tech", "TECH", Icons.Default.Code),
    InterestTopic("art", "ART", Icons.Default.Palette),
    InterestTopic("health", "HEALTH", Icons.Default.FitnessCenter),
    InterestTopic("science", "SCIENCE", Icons.Default.Science),
    InterestTopic("history", "HISTORY", Icons.Default.History),
    InterestTopic("travel", "TRAVEL", Icons.Default.Flight),
    InterestTopic("math", "MATH", Icons.Default.Functions),
    InterestTopic("nature", "NATURE", Icons.Default.Park),

    InterestTopic(
        id = "custom_ai",
        title = "CUSTOM",
        icon = Icons.Default.Edit
    )
)

@Composable
fun InterestSettingsScreen(
    onNavigateBack: () -> Unit = {}
) {

    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences(
            "profile_prefs",
            Context.MODE_PRIVATE
        )
    }

    val savedTopicsStr =
        prefs.getString("selected_topics", "")
            ?: ""

    val savedTopics =
        savedTopicsStr
            .split(",")
            .filter { it.isNotBlank() }
            .toSet()

    var selectedTopics by remember {
        mutableStateOf(savedTopics)
    }

    var customTopic by remember {
        mutableStateOf(
            prefs.getString(
                "custom_topic",
                ""
            ) ?: ""
        )
    }

    var apiKey by remember {
        mutableStateOf(
            prefs.getString("user_api_key", "") ?: ""
        )
    }
    var showApiKey by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            prefs.edit()
                .putString(
                    "selected_topics",
                    selectedTopics.joinToString(",")
                )
                .apply()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {

        // TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onNavigateBack
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment =
                    Alignment.CenterHorizontally
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

            Spacer(
                modifier = Modifier.width(48.dp)
            )
        }

        HorizontalDivider(
            color = DividerColor,
            thickness = 1.dp
        )

        Text(
            text =
                "Choose topics that spark your curiosity to\npersonalize your focus hub.",
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp,
                    vertical = 24.dp
                ),
            lineHeight = 24.sp
        )

        // GRID
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 1.dp,
                    bottom = 38.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            val rows = topicsList.chunked(2)

            rows.forEach { row ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    row.forEach { topic ->

                        val isSelected =
                            selectedTopics.contains(topic.id)

                        InterestTile(
                            modifier =
                                Modifier.weight(1f),
                            topic = topic,
                            isSelected = isSelected,
                            onClick = {

                                if (topic.id == "custom_ai") {

                                    customTopic = ""

                                    prefs.edit()
                                        .putString(
                                            "custom_topic",
                                            ""
                                        )
                                        .apply()
                                }

                                selectedTopics =
                                    if (isSelected)
                                        selectedTopics - topic.id
                                    else
                                        selectedTopics + topic.id
                            }
                        )
                    }
                }
            }

            // CUSTOM TOPIC INPUT
            if (selectedTopics.contains("custom_ai")) {

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customTopic,
                    onValueChange = {
                        customTopic = it
                        prefs.edit()
                            .putString("custom_topic", it)
                            .apply()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    placeholder = {
                        Text("Например: Artificial Intelligence")
                    },
                    label = {
                        Text("Your custom topic")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = PrimaryPurple,
                        unfocusedLabelColor = TextGray
                    )
                )

                // API KEY INPUT
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = {
                        apiKey = it
                        prefs.edit()
                            .putString("user_api_key", it)
                            .apply()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    placeholder = {
                        Text("Enter your Gemini API Key")
                    },
                    label = {
                        Text("Your API Key (optional)")
                    },
                    singleLine = true,
                    visualTransformation = if (showApiKey)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                imageVector = if (showApiKey)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                                contentDescription = if (showApiKey) "Hide key" else "Show key",
                                tint = TextGray
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = PrimaryPurple,
                        unfocusedLabelColor = TextGray
                    )
                )
            }
        }
    }
}

@Composable
fun InterestTile(
    modifier: Modifier = Modifier,
    topic: InterestTopic,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    val backgroundColor by
    animateColorAsState(
        targetValue =
            if (isSelected)
                PrimaryPurple.copy(alpha = 0.15f)
            else
                SurfaceColor,

        animationSpec = tween(300),
        label = "bgColor"
    )

    val contentColor by
    animateColorAsState(
        targetValue =
            if (isSelected)
                PrimaryPurple
            else
                TextGray,

        animationSpec = tween(300),
        label = "contentColor"
    )

    val borderColor by
    animateColorAsState(
        targetValue =
            if (isSelected)
                PrimaryPurple
            else
                Color.Transparent,

        animationSpec = tween(300),
        label = "borderColor"
    )

    Box(
        modifier = modifier
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(backgroundColor)
            .border(
                1.dp,
                borderColor,
                RoundedCornerShape(16.dp)
            )
            .clickable {
                onClick()
            }
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement =
                Arrangement.Center,
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = topic.icon,
                contentDescription =
                    topic.title,
                tint = contentColor,
                modifier =
                    Modifier.size(36.dp)
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Text(
                text = topic.title,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight =
                    FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }

        if (isSelected) {

            Box(
                modifier = Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .padding(12.dp)
                    .size(20.dp)
                    .background(
                        PrimaryPurple,
                        CircleShape
                    ),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Check,
                    contentDescription =
                        "Selected",
                    tint = Color.White,
                    modifier =
                        Modifier.size(14.dp)
                )
            }
        }
    }
}