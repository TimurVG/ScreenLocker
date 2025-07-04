package com.timurvg.screenlocker.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.timurvg.screenlocker.R

class LockService : Service() {
    private val vibrator by lazy { getSystemService(Vibrator::class.java) }
    private var overlayView: View? = null
    private var tapCount = 0
    private val centerZone = Rect().apply {
        val size = 300
        set((resources.displayMetrics.widthPixels - size) / 2,
            (resources.displayMetrics.heightPixels - size) / 2,
            (resources.displayMetrics.widthPixels + size) / 2,
            (resources.displayMetrics.heightPixels + size) / 2)
    }
    private val unlockZone = Rect(0, 0, 100, 100)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startForeground()
            ACTION_LOCK -> showOverlay()
            ACTION_UNLOCK -> hideOverlay()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun startForeground() {
        val notification = NotificationCompat.Builder(this, "lock_channel")
            .setContentTitle(getString(R.string.notification_title))
            .setSmallIcon(R.drawable.ic_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    private fun showOverlay() {
        if (overlayView == null) {
            overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_lock, null).apply {
                setOnTouchListener { _, event -> handleTouch(event) }
            }

            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP
                alpha = 0.05f
                (getSystemService(WINDOW_SERVICE) as WindowManager).addView(overlayView, this)
            }
            vibrate(300)
        }
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        when {
            // Разблокировка (4 тапа в угол)
            unlockZone.contains(event.x.toInt(), event.y.toInt()) -> {
                if (event.action == MotionEvent.ACTION_DOWN && ++tapCount >= 4) {
                    hideOverlay()
                    tapCount = 0
                }
            }
            // Блокировка (2 тапа в центр)
            centerZone.contains(event.x.toInt(), event.y.toInt()) -> {
                if (event.action == MotionEvent.ACTION_DOWN && ++tapCount >= 2) {
                    showOverlay()
                    tapCount = 0
                }
            }
            else -> tapCount = 0
        }
        return true
    }

    private fun hideOverlay() {
        overlayView?.let {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it)
            overlayView = null
            vibrate(300)
        }
    }

    private fun vibrate(durationMs: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs,
                VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(durationMs)
        }
    }

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "START"
        const val ACTION_LOCK = "LOCK"
        const val ACTION_UNLOCK = "UNLOCK"
        const val ACTION_STOP = "STOP"
    }
}