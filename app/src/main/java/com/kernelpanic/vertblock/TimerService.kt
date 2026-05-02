package com.kernelpanic.vertblock

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.kernelpanic.vertblock.database.VertBlockDatabase
import com.kernelpanic.vertblock.database.WatchSessionEntity
import kotlinx.coroutines.*

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null

    private val DEFAULT_TOTAL_TIME = 15 * 60 // значение по умолчанию, позже заменим настройкой
    private var totalTimeSeconds = DEFAULT_TOTAL_TIME
    private var remainingSeconds = DEFAULT_TOTAL_TIME

    private var currentSession: WatchSessionEntity? = null
    private lateinit var database: VertBlockDatabase

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        database = Room.databaseBuilder(
            applicationContext,
            VertBlockDatabase::class.java,
            "vertblock.db"
        ).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        // Запускаем корутину для восстановления/создания сессии, затем стартуем таймер
        serviceScope.launch {
            // 1. Пытаемся восстановить активную сессию (незавершённую)
            val activeSession = database.watchSessionDao().getActiveSession()
            if (activeSession != null) {
                // Восстанавливаем оставшееся время из сохранённого remainingSeconds
                remainingSeconds = activeSession.durationSeconds // используем durationSeconds для хранения оставшегося времени
                totalTimeSeconds = activeSession.startTime.toInt() // заглушка, позже будем хранить totalTime отдельно
                currentSession = activeSession
            } else {
                // Создаём новую сессию
                val session = WatchSessionEntity(
                    startTime = System.currentTimeMillis(),
                    durationSeconds = remainingSeconds, // здесь durationSeconds - оставшееся время
                    appName = "youtube_shorts"
                )
                val sessionId = database.watchSessionDao().insertSession(session)
                currentSession = session.copy(id = sessionId)
            }

            // Запускаем foreground с начальным уведомлением
            val notification = buildNotification(remainingSeconds)
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
            startTimer()
        }
        return START_STICKY
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
                updateNotification(remainingSeconds)
                // Периодически сохраняем оставшееся время в сессию (каждые 5 секунд для производительности)
                if (remainingSeconds % 5 == 0) {
                    saveProgress()
                }
            }
            // Таймер истёк – завершаем сессию
            finishSession()
            stopSelf()
        }
    }

    private fun updateNotification(secondsLeft: Int) {
        val notification = buildNotification(secondsLeft)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(secondsLeft: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VertBlock")
            .setContentText("Next question in ${formatTime(secondsLeft)}")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setStyle(NotificationCompat.BigTextStyle().bigText("There is time left until the next question: ${formatTime(secondsLeft)}"))
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VertBlock Timer",
                NotificationManager.IMPORTANCE_LOW // тихий канал, без всплывания
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

    private suspend fun saveProgress() {
        currentSession?.let { session ->
            database.watchSessionDao().updateSession(
                session.copy(durationSeconds = remainingSeconds) // durationSeconds используется как оставшееся время
            )
        }
    }

    private suspend fun finishSession() {
        currentSession?.let { session ->
            database.watchSessionDao().updateSession(
                session.copy(
                    endTime = System.currentTimeMillis(),
                    durationSeconds = 0 // или можно записать totalTimeSeconds - elapsed, но сейчас обнуляем
                )
            )
        }
    }

    override fun onDestroy() {
        serviceScope.launch {
            saveProgress() // сохраняем текущий прогресс
            if (remainingSeconds <= 0) {
                finishSession()
            }
        }
        timerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 100
        private const val CHANNEL_ID = "timer_channel"
    }
}