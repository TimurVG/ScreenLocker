package com.timurvg.screenlocker

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat

class LockService : Service() {
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var vibrator: Vibrator? = null
    private var tapCount = 0
    private var lastTapTime = 0L

    // Константы согласно ТЗ
    private val tapDelay = 300L
    private val centerAreaSize = 300
    private val cornerAreaSize = 100

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        startForeground(1, NotificationHelper.createNotification(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {} // Режим ожидания
            "STOP" -> stopSelf()
            "LOCK" -> showOverlay()
            "UNLOCK" -> removeOverlay()
        }
        return START_STICKY
    }

    private fun showOverlay() {
        if (overlayView != null) return

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null).apply {
            setOnTouchListener { _, event ->
                handleTouch(event.x, event.y)
                true
            }
        }

        windowManager = getSystemService(WINDOW_MANAGER_SERVICE) as WindowManager
        windowManager?.addView(overlayView, createOverlayParams())
    }

    private fun createOverlayParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            alpha = 0.05f // 5% прозрачность
            gravity = Gravity.TOP or Gravity.START
        }
    }

    private fun handleTouch(x: Float, y: Float) {
        val currentTime = System.currentTimeMillis()

        // Центральная зона 300x300px (блокировка)
        if (isInCenterArea(x, y)) {
            if (currentTime - lastTapTime < tapDelay) {
                if (++tapCount == 2) {
                    confirmAction(300) { showOverlay() }
                }
            } else {
                tapCount = 1
            }
        }
        // Угловая зона 100x100px (разблокировка)
        else if (isInCornerArea(x, y)) {
            if (currentTime - lastTapTime < tapDelay) {
                if (++tapCount == 4) {
                    confirmAction(300) { removeOverlay() }
                }
            } else {
                tapCount = 1
            }
        }

        lastTapTime = currentTime
    }

    private fun isInCenterArea(x: Float, y: Float): Boolean {
        val centerX = resources.displayMetrics.widthPixels / 2
        val centerY = resources.displayMetrics.heightPixels / 2
        return x in (centerX - centerAreaSize/2)..(centerX + centerAreaSize/2) &&
                y in (centerY - centerAreaSize/2)..(centerY + centerAreaSize/2)
    }

    private fun isInCornerArea(x: Float, y: Float): Boolean {
        return x in 0f..cornerAreaSize.toFloat() &&
                y in 0f..cornerAreaSize.toFloat()
    }

    private fun confirmAction(duration: Long, action: () -> Unit) {
        vibrate(duration)
        action()
        tapCount = 0
    }

    private fun vibrate(durationMs: Long) {
        vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }

    override fun onDestroy() {
        removeOverlay()
        stopForeground(true)
        super.onDestroy()
    }
}