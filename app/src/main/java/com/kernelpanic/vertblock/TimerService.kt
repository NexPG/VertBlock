package com.kernelpanic.vertblock

import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null
    private var remainingSeconds = 15 * 60 // 15 минут по умолчанию (позже заменим настройкой)

    // ✅ НОВОЕ: репозиторий для сохранения прогресса
    private lateinit var timerRepository: TimerRepository

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // ✅ НОВОЕ: инициализируем репозиторий и читаем сохранённое время
        timerRepository = TimerRepository(this)
        serviceScope.launch {
            timerRepository.remainingSeconds.collect { savedSeconds ->
                // Если таймер ещё не запущен и есть сохранённое значение — используем его
                if (remainingSeconds == 15 * 60 && savedSeconds > 0) {
                    remainingSeconds = savedSeconds
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val notification = buildNotification(remainingSeconds)
        startForeground(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
        startTimer()
        return START_STICKY
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
                updateNotification(remainingSeconds)
            }
            // Здесь будет логика показа вопроса
            stopSelf()
        }
    }

    private fun updateNotification(secondsLeft: Int) {
        val notification = buildNotification(secondsLeft)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(secondsLeft: Int): Notification {
        val progress = (secondsLeft * 100) / TOTAL_TIME
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VertBlock")
            .setContentText("Before the question: ${formatTime(secondsLeft)}")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE) // <-- Сомнительная строка
            .setPriority(NotificationCompat.PRIORITY_HIGH) // <-- Сомнительная строка
            .setProgress(TOTAL_TIME, progress, false)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VertBlock Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows remaining time until next question"
                enableVibration(false)
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    override fun onDestroy() {
        // ✅ НОВОЕ: сохраняем оставшееся время перед остановкой
        serviceScope.launch {
            timerRepository.saveRemaining(remainingSeconds)
        }
        timerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 100
        private const val CHANNEL_ID = "timer_channel"
        private const val TOTAL_TIME = 15 * 60
    }
}