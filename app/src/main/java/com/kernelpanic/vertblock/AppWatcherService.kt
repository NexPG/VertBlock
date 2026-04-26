package com.kernelpanic.vertblock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AppWatcherService : AccessibilityService() {

    private var isTracking = false
    private var wasInShorts = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName != "com.google.android.youtube") {
            // Событие НЕ от YouTube — если отслеживаем, то точно вышли
            if (isTracking) {
                isTracking = false
                stopService(Intent(this, TimerService::class.java))
            }
            return
        }

        // Событие от YouTube, проверяем интерфейс
        val root = rootInActiveWindow ?: return
        val isShorts = isYouTubeShorts(root)

        if (isShorts && !isTracking) {
            // Только что вошли в Shorts
            isTracking = true
            val intent = Intent(this, TimerService::class.java)
            startService(intent)
        } else if (!isShorts && isTracking) {
            // Вышли из Shorts (но всё ещё в YouTube)
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

    override fun onInterrupt() {}
}