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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, NotificationHelper.createNotification(this))
        setupOverlay()
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = View(this).apply { setBackgroundColor(0x00000000) }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        overlayView.setOnTouchListener { _, event ->
            handleTouch(event.x.toInt(), event.y.toInt())
            true
        }

        windowManager.addView(overlayView, params)
    }

    private fun handleTouch(x: Int, y: Int) {
        val centerX = overlayView.width / 2
        val centerY = overlayView.height / 2

        // Проверка зоны 300x300px в центре
        if (x in (centerX - 150)..(centerX + 150) &&
            y in (centerY - 150)..(centerY + 150)) {
            vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { windowManager.removeView(overlayView) } catch (_: Exception) {}
    }
}
