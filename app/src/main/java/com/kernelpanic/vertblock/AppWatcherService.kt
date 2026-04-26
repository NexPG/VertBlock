package com.kernelpanic.vertblock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AppWatcherService : AccessibilityService() {

    private var isTracking = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        // 1. Игнорируем события от системных пакетов (шторка, диалоги и т.д.)
        if (isSystemPackage(packageName)) {
            return
        }

        // 2. Если событие не от YouTube, и мы в режиме отслеживания — останавливаем таймер
        if (packageName != "com.google.android.youtube") {
            if (isTracking) {
                isTracking = false
                stopService(Intent(this, TimerService::class.java))
            }
            return
        }

        // 3. Событие от YouTube. Проверяем, находимся ли мы в Shorts
        val root = rootInActiveWindow ?: return
        val isShorts = isYouTubeShorts(root)

        if (isShorts && !isTracking) {
            // Заходим в Shorts — запускаем таймер
            isTracking = true
            val intent = Intent(this, TimerService::class.java)
            startService(intent)
        } else if (!isShorts && isTracking) {
            // Вышли из Shorts (но всё еще в YouTube)
            isTracking = false
            stopService(Intent(this, TimerService::class.java))
        }
    }

    private fun isYouTubeShorts(node: AccessibilityNodeInfo): Boolean {
        node.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_watch_fragment_root")
            ?.let { if (it.isNotEmpty()) return true }
        node.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_recycler")
            ?.let { if (it.isNotEmpty()) return true }
        return false
    }

    // Метод для определения системных пакетов
    private fun isSystemPackage(packageName: String): Boolean {
        return when (packageName) {
            "com.android.systemui",           // Шторка уведомлений, статус-бар
            "com.android.settings",           // Настройки
            "com.android.launcher",           // Лаунчер
            "com.google.android.apps.nexuslauncher", // Pixel Launcher
            "android" -> true                 // Системный процесс
            else -> false
        }
    }

    override fun onInterrupt() {}
}