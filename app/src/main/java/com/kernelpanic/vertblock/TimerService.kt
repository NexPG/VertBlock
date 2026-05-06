package com.kernelpanic.vertblock

import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
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
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.room.Room
import com.kernelpanic.vertblock.data.QuestionRepository
import com.kernelpanic.vertblock.database.QuizResultEntity
import com.kernelpanic.vertblock.database.VertBlockDatabase
import com.kernelpanic.vertblock.database.WatchSessionEntity
import kotlinx.coroutines.*
import android.view.View
import androidx.compose.ui.platform.ViewCompositionStrategy
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.content.pm.ActivityInfo

class TimerService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null

    private val defaultTotalTimeSeconds = 1 * 60
    private var totalTimeSeconds = defaultTotalTimeSeconds
    private var remainingSeconds = defaultTotalTimeSeconds

    private var currentSession: WatchSessionEntity? = null
    private lateinit var database: VertBlockDatabase
    private lateinit var questionRepository: QuestionRepository
    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null

    // ------------- Жизненный цикл и владельцы -------------
    private lateinit var lifecycleRegistry: LifecycleRegistry
    private lateinit var savedStateRegistryController: SavedStateRegistryController
    private val _viewModelStore = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = _viewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    // ----------------------------------------------------

    override fun onCreate() {
        super.onCreate()

        // ---------- Инициализация владельцев в строгом порядке ----------
        lifecycleRegistry = LifecycleRegistry(this)
        savedStateRegistryController = SavedStateRegistryController.create(this)
        savedStateRegistryController.performAttach()   // без аргументов!
        savedStateRegistryController.performRestore(null)

        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        // ----------------------------------------------------------------

        createNotificationChannel()
        database = Room.databaseBuilder(
            applicationContext,
            VertBlockDatabase::class.java,
            "vertblock.db"
        ).build()
        questionRepository = QuestionRepository(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return START_NOT_STICKY
        }

        lifecycleRegistry.currentState = Lifecycle.State.STARTED

        serviceScope.launch {
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
            finishSession()
            showQuestionOverlay()
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

    // --------------- Оверлей с вопросом ---------------

    private fun showQuestionOverlay() {
        val intent = Intent(this, QuestionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)
    }

    private fun hideOverlay() {
        overlayView?.let {
            it.disposeComposition() // Явно уничтожаем UI-дерево Compose
            windowManager.removeView(it)
            overlayView = null
        }
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
    // ----------------------------------------------

    override fun onDestroy() {
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
        _viewModelStore.clear()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 100
        private const val CHANNEL_ID = "timer_channel"
    }
}