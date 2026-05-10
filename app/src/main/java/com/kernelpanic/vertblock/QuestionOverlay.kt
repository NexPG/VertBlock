package com.kernelpanic.vertblock

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

private val OverlaySurface = Color(0xFF1E1E22)

@Composable
fun QuestionOverlay(
    question: String = "Place for the question",
    options: List<String> = listOf("answer 1", "answer 2", "answer 3", "answer 4"),
    onAnswerSelected: (String, Int) -> Unit = { _, _ -> }   // второй параметр – номер попытки
) {
    var attemptedAnswers by remember { mutableStateOf<List<String>>(emptyList()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .border(1.dp, DividerColor, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = OverlaySurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VERTBLOCK",
                    color = TextGray,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Knowledge check required to continue",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(PrimaryPurple)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = question,
                    color = PrimaryPurple,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                options.forEach { option ->
                    val isAttempted = attemptedAnswers.contains(option)
                    AnswerOption(
                        text = option,
                        isSelected = isAttempted,
                        onClick = {
                            if (!isAttempted) {
                                val attemptNumber = attemptedAnswers.size + 1
                                attemptedAnswers = attemptedAnswers + option
                                onAnswerSelected(option, attemptNumber)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun AnswerOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryPurple else DividerColor,
        label = "border"
    )

    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryPurple.copy(alpha = 0.1f) else Color.Transparent,
        label = "bg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(animatedBgColor)
            .border(1.dp, animatedBorderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) PrimaryPurple else Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = false)
@Composable
fun PreviewQuestionOverlay() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {
        QuestionOverlay()
    }
}