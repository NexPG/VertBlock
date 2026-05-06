package com.kernelpanic.vertblock

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.room.Room
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.kernelpanic.vertblock.data.QuestionRepository
import com.kernelpanic.vertblock.database.QuizResultEntity
import com.kernelpanic.vertblock.database.VertBlockDatabase
import com.kernelpanic.vertblock.database.WatchSessionEntity
import kotlinx.coroutines.*

class TimerService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null

    private val DEFAULT_TOTAL_TIME = 1 * 60 // значение по умолчанию, позже заменим настройкой
    private var totalTimeSeconds = DEFAULT_TOTAL_TIME
    private var remainingSeconds = DEFAULT_TOTAL_TIME

    private var currentSession: WatchSessionEntity? = null
    private lateinit var database: VertBlockDatabase
    private lateinit var questionRepository: QuestionRepository
    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null

    // Для Lifecycle
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    private val savedStateRegistry = SavedStateRegistry(this)
    override val savedStateRegistryOwner: SavedStateRegistryOwner get() = this

    // BroadcastReceiver для перезапуска таймера после ответа
    private val restartReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_RESTART_TIMER) {
                // Получаем результат ответа (опционально)
                val isCorrect = intent.getBooleanExtra("is_correct", false)
                val attempts = intent.getIntExtra("attempts", 1)
                val category = intent.getStringExtra("category") ?: "unknown"
                val question = intent.getStringExtra("question") ?: "unknown"
                val userAnswer = intent.getStringExtra("user_answer") ?: "unknown"

                // Сохраняем результат в фоне
                serviceScope.launch {
                    database.quizResultDao().insertResult(
                        QuizResultEntity(
                            question = question,
                            correctAnswer = intent.getStringExtra("correct_answer") ?: "",
                            userAnswer = userAnswer,
                            attempts = attempts,
                            category = category
                        )
                    )
                }

                // Перезапускаем таймер с полным временем
                remainingSeconds = totalTimeSeconds
                saveProgress()
                updateNotification(remainingSeconds)
                startTimer()
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
        )
            .fallbackToDestructiveMigration()
            .build()
        questionRepository = QuestionRepository(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        savedStateRegistry.performRestore(null)

        // Регистрируем BroadcastReceiver
        registerReceiver(restartReceiver, IntentFilter(ACTION_RESTART_TIMER))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        lifecycleRegistry.currentState = Lifecycle.State.STARTED

        // Основная логика только при первом запуске или перезапуске
        if (intent == null || intent.action != ACTION_RESTART_TIMER) {
            serviceScope.launch {
                val activeSession = database.watchSessionDao().getActiveSession()
                if (activeSession != null) {
                    remainingSeconds = activeSession.durationSeconds
                    totalTimeSeconds = activeSession.startTime.toInt() // временно
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
            // Таймер истёк – показываем вопрос
            showQuestionOverlay()
        }
    }

    private fun showQuestionOverlay() {
        // Проверка разрешения (обычно уже есть)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                stopSelf()
                return
            }
        }

        serviceScope.launch {
            val question = questionRepository.getRandomQuestion()
            if (question == null) {
                stopSelf()
                return@launch
            }

            val options = questionRepository.getShuffledOptions(question)

            withContext(Dispatchers.Main) {
                // Создаём ComposeView для оверлея
                overlayView = ComposeView(this@TimerService).apply {
                    setContent {
                        QuestionOverlay(
                            question = question.question,
                            options = options,
                            onAnswerSelected = { selectedAnswer ->
                                val isCorrect = selectedAnswer == question.correct_answer
                                // Прячем оверлей
                                hideOverlay()
                                // Отправляем Intent на перезапуск с результатами
                                val restartIntent = Intent(this@TimerService, TimerService::class.java).apply {
                                    action = ACTION_RESTART_TIMER
                                    putExtra("is_correct", isCorrect)
                                    putExtra("attempts", if (isCorrect) 1 else 2) // упрощённо, потом доработаем
                                    putExtra("category", question.category)
                                    putExtra("question", question.question)
                                    putExtra("user_answer", selectedAnswer)
                                    putExtra("correct_answer", question.correct_answer)
                                }
                                startService(restartIntent)
                            }
                        )
                    }
                }

                // Настройка параметров окна
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    else
                        WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                )
                params.gravity = Gravity.CENTER

                windowManager.addView(overlayView, params)
            }
        }
    }

    private fun hideOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }

    // Остальные методы без изменений
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
                NotificationManager.IMPORTANCE_LOW
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
        hideOverlay()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 100
        private const val CHANNEL_ID = "timer_channel"
        const val ACTION_RESTART_TIMER = "com.kernelpanic.vertblock.RESTART_TIMER"
        const val ACTION_SHOW_QUESTION = "com.kernelpanic.vertblock.SHOW_QUESTION"
    }
}