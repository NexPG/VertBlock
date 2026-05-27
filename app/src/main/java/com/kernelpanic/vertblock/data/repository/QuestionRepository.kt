package com.kernelpanic.vertblock.data

import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
        var retries = 2
        while (retries >= 0) {
            try {
                val customTopic = context
                    .getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                    .getString("custom_topic", "") ?: ""

                if (customTopic.isBlank()) {
                    return null
                }

                val prompt = """
You are a professional quiz master.
Topic: ${customTopic.uppercase()}

STRICT CONTENT RULES:
- PROHIBITED: Drugs, alcohol, sex, racism, hate speech, profanity, LGBT, Harm yourself and others.
- IF THE TOPIC IS PROHIBITED: Ignore the topic and generate a question about "What can you do today to become better tomorrow?".

[TASK]
Generate ONE unique and interesting quiz question.
Topic: ${customTopic.uppercase()}

[RULES]
- Language: same as the Topic.
- Avoid common/boring facts.
- Short question, 4 short answer options.
- Exactly one correct answer.
- NO markdown, NO explanations.

[FORMAT]
QUESTION: [Text]
CORRECT: [Text]
WRONG1: [Text]
WRONG2: [Text]
WRONG3: [Text]
        """.trimIndent()

                // 1. Создаем модель Gemini
                val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                val apiKey = prefs.getString("user_api_key", "")?.ifBlank { null }
                if (apiKey.isNullOrBlank()) {
                    Log.e("GEMINI_ERROR", "API key is empty. Enter your key in Interest Settings.")
                    return null
                }

                val config = generationConfig {
                    temperature = 1.4f
                    topK = 40
                    topP = 0.9f
                }

                val generativeModel = GenerativeModel(
                    modelName = "gemini-flash-lite-latest",
                    apiKey = apiKey,
                    generationConfig = config
                )


                // 2. Отправляем запрос и получаем ответ
                val response = generativeModel.generateContent(prompt)
                val content = response.text ?: return null

                Log.d("GEMINI_RESPONSE", content) // Тег для логов

                // 3. Парсим ответ
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

                return Question(
                    type = "multiple",
                    difficulty = "medium",
                    category = "custom",
                    question = question,
                    correct_answer = correct,
                    incorrect_answers = listOf(wrong1, wrong2, wrong3)
                )

            } catch (e: Exception) {
                Log.e("GEMINI_ERROR", "Attempt failed, retries left: $retries", e)
                if (retries == 0) return null
                retries--
            }
        }
        return null
    }

    private val gson = Gson()

    suspend fun getRandomQuestion(): Question? {
        val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val selectedTopicsStr = prefs.getString("selected_topics", "") ?: ""
        val selectedCategories = selectedTopicsStr.split(",")
            .filter { it.isNotBlank() }
            .shuffled() // Перемешиваем, чтобы пробовать в разном порядке

        if (selectedCategories.isEmpty()) return null

        return withContext(Dispatchers.IO) {
            try {
                Log.d("QUESTION_DEBUG", "Categories to try: $selectedCategories")

                // Идем по списку тем, пока не найдем подходящий вопрос
                for (category in selectedCategories) {
                    if (category == "custom") {
                        val aiQuestion = generateAiQuestion()
                        if (aiQuestion != null) return@withContext aiQuestion
                    } else {
                        try {
                            val jsonString = loadJsonFromAsset("questions/$category.json")
                            val listType = object : TypeToken<List<Question>>() {}.type
                            val questions: List<Question> = gson.fromJson(jsonString, listType)
                            if (questions.isNotEmpty()) {
                                return@withContext questions.random()
                            }
                        } catch (e: Exception) {
                            Log.e("QUESTION_DEBUG", "Failed to load category: $category", e)
                        }
                    }
                }
                null
            } catch (e: Exception) {
                Log.e("QUESTION_DEBUG", "Question selection failed", e)
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