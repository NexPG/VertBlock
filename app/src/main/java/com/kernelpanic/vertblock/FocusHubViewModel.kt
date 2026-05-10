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
    val activityData: List<Float> = listOf(0f, 0f, 0f, 0f)
)

class FocusHubViewModel(application: Application) : AndroidViewModel(application) {
    private val database = VertBlockDatabase.getDatabase(application)
    private val repository = StatsRepository(database.watchSessionDao())

    private val _state = MutableStateFlow(FocusHubState())
    val state: StateFlow<FocusHubState> = _state

    init {
        loadData()
    }

    fun loadData(period: String = "daily") {
        viewModelScope.launch {
            val totalSeconds = repository.getTotalWatchTimeSeconds()
            val answered = repository.getQuestionsAnsweredToday()
            val firstCorrect = repository.getFirstTryCorrectToday()
            val topics = repository.getTopicsTouchedToday()
            val streak = repository.getStreakDays()

            val accuracy = if (answered > 0) firstCorrect.toFloat() / answered else 0f
            val diversityScore = (topics.size.coerceAtMost(5) / 5f) * 100
            val timeScore = (totalSeconds / 60f).coerceAtMost(120f) / 120f * 100
            val score = (accuracy * 40f + diversityScore * 30f + timeScore * 30f).toInt().coerceIn(0, 100)

            val minutes = totalSeconds / 60
            val timeString = if (minutes < 60) "${minutes}m" else "${minutes / 60}h ${minutes % 60}m"

            val graphData = when (period) {
                "daily" -> listOf(10f, 25f, 18f, 30f)
                "weekly" -> listOf(60f, 70f, 55f, 80f)
                "monthly" -> listOf(200f, 180f, 220f, 210f)
                "yearly" -> listOf(2400f, 2500f, 2300f, 2600f)
                else -> listOf(0f, 0f, 0f, 0f)
            }

            _state.value = FocusHubState(
                attentionScore = score,
                timeWellSpent = timeString,
                streakDays = streak,
                brainFoodTopics = topics.size,
                activityData = graphData
            )
        }
    }
}