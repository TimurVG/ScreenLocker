package com.example.screenlocker.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.core.app.NotificationCompat
import com.example.screenlocker.R

class LockService : Service() {
    private val vibrator by lazy { getSystemService(Vibrator::class.java) }
    private var overlayView: View? = null
    private var tapCount = 0
    private val unlockZone = Rect(0, 0, 100, 100)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_LOCK -> showOverlay()
            ACTION_UNLOCK -> hideOverlay()
            ACTION_STOP -> stopSelf()
            else -> startForeground()
        }
        return START_STICKY
    }

    private fun startForeground() {
        val channelId = "lock_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId)
        }
        startForeground(1, NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.notification_title))
            .setSmallIcon(R.drawable.ic_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build())
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
        if (event.action == MotionEvent.ACTION_DOWN &&
            unlockZone.contains(event.x.toInt(), event.y.toInt())) {
            if (++tapCount >= 4) {
                hideOverlay()
                tapCount = 0
            }
        } else {
            tapCount = 0
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
        const val ACTION_LOCK = "LOCK"
        const val ACTION_UNLOCK = "UNLOCK"
        const val ACTION_STOP = "STOP"
    }
}