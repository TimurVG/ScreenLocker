package com.timurvg.screenlocker.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.*
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.timurvg.screenlocker.R

class LockService : Service() {
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val vibrator by lazy { getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    private var tapCount = 0

    private val centerZone by lazy {
        val displayMetrics = resources.displayMetrics
        val centerX = displayMetrics.widthPixels / 2
        val centerY = displayMetrics.heightPixels / 2
        Rect(centerX - 150, centerY - 150, centerX + 150, centerY + 150)
    }

    private val unlockZone by lazy {
        Rect(0, 0, 100, 100)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_LOCK -> showOverlay()
            ACTION_UNLOCK -> hideOverlay()
            else -> startForegroundService()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = createNotificationChannel()
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.notification_title))
            .setSmallIcon(R.drawable.ic_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "lock_channel"
        val channel = NotificationChannel(
            channelId,
            "Screen Locker",
            NotificationManager.IMPORTANCE_LOW
        )
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
        return channelId
    }

    private fun showOverlay() {
        if (overlayView == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_lock, null).apply {
                setOnTouchListener { _, event -> handleTouch(event) }
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP
                alpha = 0.05f
            }

            windowManager.addView(overlayView, params)
            vibrate(300)
        }
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        when {
            unlockZone.contains(event.x.toInt(), event.y.toInt()) -> {
                if (event.action == MotionEvent.ACTION_DOWN && ++tapCount >= 4) {
                    hideOverlay()
                    tapCount = 0
                }
            }
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
            windowManager.removeView(it)
            overlayView = null
            vibrate(300)
        }
    }

    private fun vibrate(durationMs: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs,
                VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }

    companion object {
        const val ACTION_LOCK = "LOCK"
        const val ACTION_UNLOCK = "UNLOCK"
        const val FOREGROUND_SERVICE_TYPE_LOCATION = 8
    }
}