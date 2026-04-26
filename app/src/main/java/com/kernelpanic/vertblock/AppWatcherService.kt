package com.kernelpanic.vertblock

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AppWatcherService : AccessibilityService() {

    private var isTracking = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.DEFAULT
        }
        setServiceInfo(info)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return

        // 1. Игнорируем системные пакеты
        if (isSystemPackage(packageName)) return

        // 2. Если мы не в YouTube, а таймер запущен — останавливаем
        if (packageName != "com.google.android.youtube") {
            if (isTracking) {
                isTracking = false
                stopService(Intent(this, TimerService::class.java))
            }
            return
        }

        // 3. Мы в YouTube — запускаем повторные проверки для поиска Shorts
        if (rootInActiveWindow != null) {
            checkForShortsWithRetry(4)  // 4 попытки
        }
    }

    private fun checkForShortsWithRetry(retryCount: Int = 0) {
        val root = rootInActiveWindow
        if (root == null) {
            if (retryCount > 0) {
                handler.postDelayed({ checkForShortsWithRetry(retryCount - 1) }, 300)
            }
            return
        }

        val isShorts = isYouTubeShorts(root)

        if (isShorts && !isTracking) {
            // Заходим в Shorts — запускаем таймер
            isTracking = true
            val intent = Intent(this, TimerService::class.java)
            startService(intent)
        } else if (!isShorts && isTracking) {
            // Вышли из Shorts — останавливаем таймер
            isTracking = false
            stopService(Intent(this, TimerService::class.java))
        } else if (!isShorts && !isTracking && retryCount > 0) {
            // Shorts еще не обнаружены, пробуем снова
            handler.postDelayed({ checkForShortsWithRetry(retryCount - 1) }, 300)
        }
    }

    private fun isYouTubeShorts(node: AccessibilityNodeInfo): Boolean {
        node.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_watch_fragment_root")
            ?.let { if (it.isNotEmpty()) return true }
        node.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_recycler")
            ?.let { if (it.isNotEmpty()) return true }
        return false
    }

    private fun isSystemPackage(packageName: String): Boolean {
        return when (packageName) {
            "com.android.systemui",           // Шторка уведомлений, статус-бар
            "com.android.settings",           // Настройки
            "com.android.launcher",           // Лаунчер
            "android" -> true                 // Системный процесс
            else -> false
        }
    }

    override fun onInterrupt() {}
}