package com.timurg.screenlocker

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.view.*

class LockService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private val vibrator by lazy { getSystemService(Vibrator::class.java) }
    private var tapCount = 0
    private var lastTapTime = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, NotificationHelper.createNotification(this))
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = View(this).apply { setBackgroundColor(Color.TRANSPARENT) }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        overlayView.setOnTouchListener { _, event ->
            handleTouch(event.x, event.y)
            true
        }

        windowManager.addView(overlayView, params)
    }

    private fun handleTouch(x: Float, y: Float) {
        if (SystemClock.elapsedRealtime() - lastTapTime > 300) tapCount = 0
        lastTapTime = SystemClock.elapsedRealtime()
        tapCount++

        when {
            // Блокировка (2 тапа в центр 300x300)
            tapCount == 2 && x in (overlayView.width/2-150)..(overlayView.width/2+150)
                    && y in (overlayView.height/2-150)..(overlayView.height/2+150) -> {
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                setupOverlay()
            }
            // Разблокировка (4 тапа в угол 100x100)
            tapCount >= 4 && x <= 100 && y <= 100 -> {
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { windowManager.removeView(overlayView) } catch (_: Exception) {}
    }
}