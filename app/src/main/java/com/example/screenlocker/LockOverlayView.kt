package com.example.screenlocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.view.GestureDetectorCompat
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class ScreenLockerService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: LockOverlayView
    private var isLocked = false
    private lateinit var gestureDetector: GestureDetectorCompat
    private val gestureListener = GestureListener()

    inner class GestureListener {
        private val circles = mutableListOf<Circle>()
        private var lastGestureTime = 0L

        fun handleTouch(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    circles.add(Circle(event.x, event.y))
                    overlayView.updateGesturePoints(circles.last().getPoints())
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    circles.lastOrNull()?.let {
                        it.addPoint(event.x, event.y)
                        overlayView.updateGesturePoints(it.getPoints())
                    }
                    checkGestures()
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    if (System.currentTimeMillis() - lastGestureTime < 1000) return false
                    if (circles.size == 1 && circles[0].isComplete() && !isLocked) {
                        lockScreen()
                    }
                    return true
                }
            }
            return false
        }

        private fun checkGestures() {
            if (circles.size >= 2 && circles.all { it.isComplete() } && isLocked) {
                unlockScreen()
                circles.clear()
            }
        }

        private fun lockScreen() {
            isLocked = true
            overlayView.setLockState(true)
            lastGestureTime = System.currentTimeMillis()
            Toast.makeText(this@ScreenLockerService, "Экран заблокирован", Toast.LENGTH_SHORT).show()
        }

        private fun unlockScreen() {
            isLocked = false
            overlayView.setLockState(false)
            lastGestureTime = System.currentTimeMillis()
            Toast.makeText(this@ScreenLockerService, "Экран разблокирован", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate() {
        super.onCreate()
        gestureDetector = GestureDetectorCompat(applicationContext, object : GestureDetector.SimpleOnGestureListener())
        setupOverlay()
        createNotificationChannel()
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LockOverlayView(this).apply {
            setOnTouchListener { _, event ->
                if (gestureDetector.onTouchEvent(event)) return@setOnTouchListener true
                gestureListener.handleTouch(event)
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        windowManager.addView(overlayView, params)
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

            val notification = NotificationCompat.Builder(this, "screen_locker_channel")
                .setContentTitle("Screen Locker")
                .setContentText("Сервис блокировки экрана активен")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()

            startForeground(1, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            windowManager.removeView(overlayView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}