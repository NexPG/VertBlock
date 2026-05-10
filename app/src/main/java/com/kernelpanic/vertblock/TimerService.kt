package com.kernelpanic.vertblock

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.kernelpanic.vertblock.data.QuestionRepository
import com.kernelpanic.vertblock.database.VertBlockDatabase
import com.kernelpanic.vertblock.database.WatchSessionEntity
import kotlinx.coroutines.*

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null

    private val defaultTotalTimeSeconds = 1 * 60
    private var totalTimeSeconds = defaultTotalTimeSeconds
    private var remainingSeconds = defaultTotalTimeSeconds

    private var currentSession: WatchSessionEntity? = null
    private lateinit var database: VertBlockDatabase

    // Приёмник для перезапуска таймера после ответа
    private val restartReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_RESTART_TIMER) {
                // Создаём новую сессию и перезапускаем таймер
                remainingSeconds = totalTimeSeconds
                serviceScope.launch {
                    createAndStartNewSession()
                    val notification = buildNotification(remainingSeconds)
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                    )
                    startTimer()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        database = Room.databaseBuilder(
            applicationContext,
            VertBlockDatabase::class.java,
            "vertblock.db"
        ).build()

        registerReceiver(
            restartReceiver,
            IntentFilter(ACTION_RESTART_TIMER),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return START_NOT_STICKY
        }

        serviceScope.launch {

            // Читаем сохранённую частоту (в минутах) из профиля
            val prefs = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
            val freqMinutes = prefs.getFloat("question_frequency_minutes", 15f)
            totalTimeSeconds = (freqMinutes * 60).toInt()
            remainingSeconds = totalTimeSeconds   // сбрасываем на полное время при каждом запуске

            val activeSession = database.watchSessionDao().getActiveSession()
            if (activeSession != null) {
                remainingSeconds = activeSession.durationSeconds
                currentSession = activeSession
            } else {
                val session = WatchSessionEntity(
                    startTime = System.currentTimeMillis(),
                    durationSeconds = remainingSeconds,
                    appName = "youtube_shorts"
                )
                val sessionId = database.watchSessionDao().insertSession(session)
                currentSession = session.copy(id = sessionId)
            }

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
                if (remainingSeconds % 5 == 0) {
                    saveProgress()
                }
            }
            // Таймер истёк – завершаем сессию и показываем вопрос
            finishSession()
            val intent = Intent(this@TimerService, QuestionActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }

    private fun updateNotification(secondsLeft: Int) {
        val notification = buildNotification(secondsLeft)
        val manager = getSystemService(NotificationManager::class.java)
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
        val channel = NotificationChannel(
            CHANNEL_ID,
            "VertBlock Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows remaining time until next question"
            enableVibration(false)
            setSound(null, null)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private suspend fun saveProgress() {
        currentSession?.let { session ->
            database.watchSessionDao().updateSession(
                session.copy(durationSeconds = remainingSeconds)
            )
        }
    }

    private suspend fun finishSession() {
        currentSession?.let { session ->
            database.watchSessionDao().updateSession(
                session.copy(
                    endTime = System.currentTimeMillis(),
                    durationSeconds = 0
                )
            )
        }
        currentSession = null
    }

    private suspend fun createAndStartNewSession() {
        val session = WatchSessionEntity(
            startTime = System.currentTimeMillis(),
            durationSeconds = remainingSeconds,
            appName = "youtube_shorts"
        )
        val sessionId = database.watchSessionDao().insertSession(session)
        currentSession = session.copy(id = sessionId)
    }

    override fun onDestroy() {
        unregisterReceiver(restartReceiver)
        serviceScope.launch {
            saveProgress()
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
        const val ACTION_RESTART_TIMER = "com.kernelpanic.vertblock.RESTART_TIMER"
    }
}