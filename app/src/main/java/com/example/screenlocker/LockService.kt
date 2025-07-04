package com.example.screenlocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class LockService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var lockView: View

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
        setupLockView()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "lock_channel",
                "Screen Locker",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "lock_channel")
            .setContentTitle("Экран заблокирован")
            .setSmallIcon(R.drawable.ic_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun setupLockView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        lockView = LayoutInflater.from(this).inflate(R.layout.lock_screen, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
        }

        lockView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                stopSelf()
                true
            } else false
        }

        windowManager.addView(lockView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::windowManager.isInitialized && ::lockView.isInitialized) {
            windowManager.removeView(lockView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}