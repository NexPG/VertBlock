package com.kernelpanic.vertblock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kernelpanic.vertblock.database.VertBlockDatabase
import com.kernelpanic.vertblock.repository.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FocusHubState(
    val attentionScore: Int = 0,
    val timeWellSpent: String = "0m",
    val streakDays: Int = 0,
    val brainFoodTopics: Int = 0,
    // данные для графика (пока не используются, но пусть будут)
    val activityData: List<Float> = listOf(0f, 0f, 0f, 0f)
)

class FocusHubViewModel(application: Application) : AndroidViewModel(application) {
    private val database = VertBlockDatabase.getDatabase(application)
    private val repository = StatsRepository(
        database.watchSessionDao(),
        database.quizResultDao()
    )

    private val _state = MutableStateFlow(FocusHubState())
    val state: StateFlow<FocusHubState> = _state

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val totalSeconds = repository.getTotalWatchTimeSeconds()
            val firstTryCount = repository.getFirstTryCount()
            val totalAnswers = repository.getTotalAnswers()
            val streak = repository.getStreakDays()
            val topicsToday = repository.getTodaysTopics()

            // Время с пользой: переводим секунды в читаемый вид
            val minutes = totalSeconds / 60
            val timeString = if (minutes < 60) "${minutes}m" else "${minutes / 60}h ${minutes % 60}m"

            // Точность с первой попытки
            val accuracy = if (totalAnswers > 0) firstTryCount.toFloat() / totalAnswers else 0f

            // Brain Food (количество уникальных тем)
            val brainFood = topicsToday.size

            // Attention Score по новой формуле
            // 40% точность, 30% разнообразие, 30% время (нормированное)
            val diversityScore = (brainFood.coerceAtMost(5) / 5f) * 100
            val timeScore = (totalSeconds / 60f).coerceAtMost(120f) / 120f * 100
            val score = (accuracy * 40f + diversityScore * 30f + timeScore * 30f).toInt()
                .coerceIn(0, 100)

            _state.value = FocusHubState(
                attentionScore = score,
                timeWellSpent = timeString,
                streakDays = streak,
                brainFoodTopics = brainFood
            )
        }
    }
}