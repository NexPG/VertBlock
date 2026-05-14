package com.kernelpanic.vertblock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kernelpanic.vertblock.database.VertBlockDatabase
import com.kernelpanic.vertblock.repository.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WatchTimeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = VertBlockDatabase.getDatabase(application)
    private val repository = StatsRepository(
        database.watchSessionDao(),
        database.quizResultDao()
    )

    private val _state = MutableStateFlow(WatchTimeState())
    val state: StateFlow<WatchTimeState> = _state

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val total = repository.getTotalSeconds()
            val daily = repository.getDailySeconds()
            val weekly = repository.getWeeklySeconds()
            val monthly = repository.getMonthlySeconds()
            val yearly = repository.getYearlySeconds()
            val percentages = repository.getWeeklyPercentages()
            val mostActiveDay = repository.getMostActiveDay()
            val mostActiveHours = repository.getMostActiveHours()

            _state.value = WatchTimeState(
                totalSeconds = total,
                dailySeconds = daily,
                weeklySeconds = weekly,
                monthlySeconds = monthly,
                yearlySeconds = yearly,
                weeklyPercentages = percentages,
                mostActiveDay = mostActiveDay,
                mostActiveHours = mostActiveHours
            )
        }
    }
}