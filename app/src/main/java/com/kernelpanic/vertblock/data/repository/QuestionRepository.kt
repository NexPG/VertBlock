package com.kernelpanic.vertblock.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

data class Question(
    val type: String,
    val difficulty: String,
    val category: String,
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>
)

class QuestionRepository(private val context: Context) {

    private val gson = Gson()

    // Пока захардкодим две темы (потом заменим на реальный выбор пользователя)
    private val selectedCategories = listOf("health", "science")

    suspend fun getRandomQuestion(): Question? {
        return withContext(Dispatchers.IO) {
            try {
                val allQuestions = mutableListOf<Question>()
                for (category in selectedCategories) {
                    val jsonString = loadJsonFromAsset("questions/${category}.json")
                    val listType = object : TypeToken<List<Question>>() {}.type
                    val questions: List<Question> = gson.fromJson(jsonString, listType)
                    allQuestions.addAll(questions)
                }
                if (allQuestions.isEmpty()) null
                else allQuestions.random()
            } catch (e: IOException) {
                e.printStackTrace()
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