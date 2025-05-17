package com.example.screenlocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlin.math.abs

class ScreenLockerService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var isLocked = false
    private val secretGesture = listOf("DOWN", "DOWN", "DOWN", "LEFT", "UP")
    private val userGestures = mutableListOf<String>()
    private var startX = 0f
    private var startY = 0f

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
        setupOverlay()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "screen_locker_channel",
                "Screen Locker",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "screen_locker_channel")
            .setContentTitle("Screen Locker")
            .setContentText("Сервис блокировки экрана активен")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = View(this).apply {
            layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )

            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.x
                        startY = event.y
                        if (event.pointerCount >= 3 && !isLocked) {
                            lockScreen()
                            true
                        } else {
                            false
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (isLocked) {
                            detectGesture(event)
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            }
        }

        try {
            windowManager.addView(overlayView, overlayView.layoutParams)
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    private fun detectGesture(event: MotionEvent) {
        val endX = event.x
        val endY = event.y
        val diffX = endX - startX
        val diffY = endY - startY

        if (abs(diffX) > abs(diffY)) {
            if (diffX > 0) {
                addGesture("RIGHT")
            } else {
                addGesture("LEFT")
            }
        } else {
            if (diffY > 0) {
                addGesture("DOWN")
            } else {
                addGesture("UP")
            }
        }
        startX = endX
        startY = endY
    }

    private fun addGesture(direction: String) {
        userGestures.add(direction)
        if (userGestures.size >= secretGesture.size) {
            val lastGestures = userGestures.takeLast(secretGesture.size)
            if (lastGestures == secretGesture) {
                unlockScreen()
            }
        }
    }

    private fun lockScreen() {
        isLocked = true
        vibrate(200)
        Toast.makeText(this, "Экран заблокирован. Сделайте секретный жест для разблокировки", Toast.LENGTH_SHORT).show()
    }

    private fun unlockScreen() {
        isLocked = false
        vibrate(300)
        userGestures.clear()
        Toast.makeText(this, "Экран разблокирован", Toast.LENGTH_SHORT).show()
    }

    private fun vibrate(durationMs: Long) {
        (getSystemService(VIBRATOR_SERVICE) as? Vibrator)?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                it.vibrate(durationMs)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            windowManager.removeView(overlayView)
        } catch (e: Exception) {
            // Игнорируем ошибки при удалении view
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}