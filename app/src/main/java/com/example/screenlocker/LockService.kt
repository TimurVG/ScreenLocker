package com.timurvg.screenlocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat

class LockService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var tapCount = 0
    private var lastTapTime = 0L
    private val tapDelay = 300L
    private val cornerSize = 100
    private val centerSize = 300

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, params)
        setupGestureDetectors()
    }

    private fun setupGestureDetectors() {
        overlayView.setOnTouchListener { v, event ->
            val x = event.x.toInt()
            val y = event.y.toInt()
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels

            // Проверка центральной зоны (300x300px)
            val centerLeft = (screenWidth - centerSize) / 2
            val centerTop = (screenHeight - centerSize) / 2
            val inCenter = x in centerLeft..(centerLeft + centerSize) &&
                    y in centerTop..(centerTop + centerSize)

            // Проверка левого верхнего угла (100x100px)
            val inCorner = x in 0..cornerSize && y in 0..cornerSize

            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTime < tapDelay) {
                        tapCount++
                    } else {
                        tapCount = 1
                    }
                    lastTapTime = currentTime

                    if (inCenter && tapCount == 2) {
                        // Двойной тап в центре - блокировка
                        lockScreen()
                        tapCount = 0
                    } else if (inCorner && tapCount == 4) {
                        // Четверной тап в углу - разблокировка
                        unlockScreen()
                        tapCount = 0
                    }
                }
            }
            true
        }
    }

    private fun lockScreen() {
        val params = overlayView.layoutParams as WindowManager.LayoutParams
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        windowManager.updateViewLayout(overlayView, params)
        Toast.makeText(this, "Экран заблокирован", Toast.LENGTH_SHORT).show()
    }

    private fun unlockScreen() {
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "lock_channel",
                "Screen Locker",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "lock_channel")
            .setContentTitle("Screen Locker")
            .setContentText("Сервис блокировки экрана активен")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }
}