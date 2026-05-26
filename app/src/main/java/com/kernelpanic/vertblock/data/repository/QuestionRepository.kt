package com.kernelpanic.vertblock.data

import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kernelpanic.vertblock.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


data class Question(
    val type: String,
    val difficulty: String,
    val category: String,
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>
)

class QuestionRepository(private val context: Context) {

    private suspend fun generateAiQuestion(): Question? {
        return try {
            val customTopic = context
                .getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                .getString("custom_topic", "") ?: ""

            if (customTopic.isBlank()) {
                return null
            }

            val prompt = """
You create quiz questions for a mobile productivity app.

Topic: $customTopic

STRICT CONTENT RULES:
- Do NOT create questions about illegal drugs, substance abuse, or alcohol.
- Do NOT create questions with sexual themes, racism, hate speech, or profanity.
- If the user's topic violates these rules, generate a question about a neutral topic like 'science'.

Generate ONE interesting knowledge question in ENGLISH.

Rules:
- Everything MUST be in English
- short question
- 4 answer options
- only 1 correct answer
- short answers
- no explanations
- no markdown

STRICT FORMAT:

QUESTION: question
CORRECT: answer
WRONG1: answer
WRONG2: answer
WRONG3: answer
        """.trimIndent()

            // 1. Создаем модель Gemini
            val generativeModel = GenerativeModel(
                modelName = "gemini-2.5-flash", // Быстрая и эффективная модель
                apiKey = BuildConfig.GEMINI_API_KEY
            )


            // 2. Отправляем запрос и получаем ответ
            val response = generativeModel.generateContent(prompt)
            val content = response.text ?: return null

            Log.d("GEMINI_RESPONSE", content) // Тег для логов

            // 3. Парсим ответ (эта часть остается без изменений)
            val question = content
                .substringAfter("QUESTION:")
                .substringBefore("CORRECT:")
                .trim()

            val correct = content
                .substringAfter("CORRECT:")
                .substringBefore("WRONG1:")
                .trim()

            val wrong1 = content
                .substringAfter("WRONG1:")
                .substringBefore("WRONG2:")
                .trim()

            val wrong2 = content
                .substringAfter("WRONG2:")
                .substringBefore("WRONG3:")
                .trim()

            val wrong3 = content
                .substringAfter("WRONG3:")
                .trim()

            Question(
                type = "multiple",
                difficulty = "medium",
                category = "custom_ai",
                question = question,
                correct_answer = correct,
                incorrect_answers = listOf(wrong1, wrong2, wrong3)
            )

        } catch (e: Exception) {
            Log.e("GEMINI_ERROR", "Failed", e) // Тег для логов
            null
        }
    }

    private val gson = Gson()

    suspend fun getRandomQuestion(): Question? {
        val prefs = context
            .getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

        val selectedTopicsStr = prefs
            .getString("selected_topics", "") ?: ""

        val selectedCategories = selectedTopicsStr
            .split(",")
            .filter { it.isNotBlank() }

        return withContext(Dispatchers.IO) {
            try {
                Log.d("QUESTION_DEBUG", "Selected categories = $selectedCategories")

                if (selectedCategories.contains("custom_ai")) {
                    Log.d("QUESTION_DEBUG", "Using AI question")
                    return@withContext generateAiQuestion()
                }

                val allQuestions = mutableListOf<Question>()

                for (category in selectedCategories) {
                    val jsonString = loadJsonFromAsset("questions/$category.json")
                    val listType = object : TypeToken<List<Question>>() {}.type
                    val questions: List<Question> = gson.fromJson(jsonString, listType)
                    allQuestions.addAll(questions)
                }

                if (allQuestions.isEmpty()) null
                else allQuestions.random()
            } catch (e: Exception) {
                Log.e("QUESTION_DEBUG", "Question failed", e)
                null
            }
        }
    }

    private fun loadJsonFromAsset(path: String): String {
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }

    fun getShuffledOptions(question: Question): List<String> {
        val options = mutableListOf(question.correct_answer)
        options.addAll(question.incorrect_answers)
        options.shuffle()
        return options
    }
}