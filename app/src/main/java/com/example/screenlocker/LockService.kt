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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        startForeground(1, NotificationHelper.createNotification(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {}
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

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager?.addView(overlayView, WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            alpha = 0.05f
            gravity = Gravity.TOP or Gravity.START
        })
    }

    private fun handleTouch(x: Float, y: Float) {
        // Реализацию жестов добавьте здесь
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }

    private fun vibrate(duration: Long) {
        vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }
}