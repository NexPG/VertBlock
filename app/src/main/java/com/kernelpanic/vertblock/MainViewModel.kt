package com.kernelpanic.vertblock

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val timerRepository = TimerRepository(application)
    private val _timeLeft = MutableStateFlow(0)
    val timeLeft = _timeLeft.asStateFlow()

    init {
        viewModelScope.launch {
            timerRepository.remainingSeconds.collect { seconds ->
                _timeLeft.value = seconds
            }
        }
    }

    fun startTimer() {
        val intent = Intent(getApplication(), TimerService::class.java)
        getApplication<Application>().startService(intent)
    }

    fun stopTimer() {
        val intent = Intent(getApplication(), TimerService::class.java)
        getApplication<Application>().stopService(intent)
    }
}