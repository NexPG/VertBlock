package com.kernelpanic.vertblock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kernelpanic.vertblock.database.VertBlockDatabase
import com.kernelpanic.vertblock.repository.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class QuestionStatsState(
    val totalAnswers: Int = 0,
    val attempts: List<StatItem> = listOf(
        StatItem("1st try", 0),
        StatItem("2nd try", 0),
        StatItem("3rd try", 0),
        StatItem("4th try", 0)
    ),
    val categories: List<StatItem> = emptyList()
)

class QuestionStatsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = VertBlockDatabase.getDatabase(application)
    private val repository = StatsRepository(
        database.watchSessionDao(),
        database.quizResultDao()
    )

    private val _state = MutableStateFlow(QuestionStatsState())
    val state: StateFlow<QuestionStatsState> = _state

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            val total = repository.getTotalAnswers()
            val attempts = repository.getAttemptsBreakdown()
            val categories = repository.getTopCategories()
            _state.value = QuestionStatsState(
                totalAnswers = total,
                attempts = attempts,
                categories = categories
            )
        }
    }
}