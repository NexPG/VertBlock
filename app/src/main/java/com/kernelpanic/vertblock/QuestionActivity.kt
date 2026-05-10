package com.kernelpanic.vertblock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
// ВАЖНО: Импортируй класс Question и делегаты state
import com.kernelpanic.vertblock.data.Question
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.kernelpanic.vertblock.data.QuestionRepository
import com.kernelpanic.vertblock.database.VertBlockDatabase
import com.kernelpanic.vertblock.database.QuizResultEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuestionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val repository = QuestionRepository(this)

        // Исправляем получение базы данных (см. пункт 2 ниже)
        val database = VertBlockDatabase.getDatabase(this)

        setContent {
            // Теперь Question? будет распознан правильно
            var questionData by remember { mutableStateOf<Question?>(null) }

            LaunchedEffect(Unit) {
                questionData = repository.getRandomQuestion()
            }

            questionData?.let { question ->
                QuestionOverlay(
                    question = question.question,
                    options = repository.getShuffledOptions(question),
                    onAnswerSelected = { answer ->
                        if (answer == question.correct_answer) {
                            CoroutineScope(Dispatchers.IO).launch {
                                // Сохраняем результат перед закрытием
                                database.quizResultDao().insertResult(
                                    QuizResultEntity(
                                        question = question.question,
                                        correctAnswer = question.correct_answer,
                                        userAnswer = answer,
                                        attempts = 1, // Или твоя логика подсчета
                                        category = question.category
                                    )
                                )
                                finish()
                            }
                        }
                    }
                )
            }
        }
    }
}