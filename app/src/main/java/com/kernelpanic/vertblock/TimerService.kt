package com.kernelpanic.vertblock

import android.app.*
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
    private var remainingSeconds = 15 * 60
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

        val notification = buildNotification(remainingSeconds)
        startForeground(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )

        // Запускаем сессию в базе данных
        serviceScope.launch {
            val session = WatchSessionEntity(startTime = System.currentTimeMillis())
            val sessionId = database.watchSessionDao().insertSession(session)
            currentSession = session.copy(id = sessionId)
        }

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
            .setContentText("До вопроса: ${formatTime(secondsLeft)}")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // временная системная иконка
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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
        serviceScope.launch {
            // Завершаем текущую сессию
            currentSession?.let { session ->
                database.watchSessionDao().updateSession(
                    session.copy(endTime = System.currentTimeMillis(), durationSeconds = (TOTAL_TIME - remainingSeconds).toInt())
                )
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
        private const val TOTAL_TIME = 15 * 60
    }
}