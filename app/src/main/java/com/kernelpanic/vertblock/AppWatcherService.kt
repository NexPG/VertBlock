package com.kernelpanic.vertblock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AppWatcherService : AccessibilityService() {

    private var isTracking = false
    private var wasInShorts = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Игнорируем системные окна: шторка, диалоги и т.д.
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        if (event.packageName != "com.google.android.youtube") {
            if (isTracking) {
                isTracking = false
                stopService(Intent(this, TimerService::class.java))
            }
            return
        }

        val root = rootInActiveWindow ?: return
        val isShorts = isYouTubeShorts(root)

        if (isShorts && !isTracking) {
            isTracking = true
            val intent = Intent(this, TimerService::class.java)
            startService(intent)
        } else if (!isShorts && isTracking) {
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